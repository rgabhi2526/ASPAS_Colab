package com.aspas.service;

import com.aspas.exception.OrderNotFoundException;
import com.aspas.model.dto.OrderResponseDTO;
import com.aspas.model.entity.OrderItem;
import com.aspas.model.entity.OrderList;
import com.aspas.model.entity.SparePart;
import com.aspas.model.entity.Vendor;
import com.aspas.repository.jpa.OrderListRepository;
import com.aspas.repository.jpa.SparePartRepository;
import com.aspas.util.JITCalculator;
import com.aspas.util.OrderFormatter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ================================================================
 * OrderService — DFD Process 3.0: Generate Daily Orders
 * ================================================================
 *
 * UML Traceability:
 *   - DFD Process: P3.0 Generate Daily Orders
 *   - Sequence Diagram: Messages #9, #15 through #23
 *   - Use Cases:
 *       UC-03: Generate Daily Orders (base)
 *       UC-02: Calculate JIT Thresholds (<<include>>)
 *       UC-04: Fetch Vendor Address (<<include>>)
 *       UC-06: Check Inventory vs Threshold (<<include>>)
 *   - Class Diagram: SystemController.triggerEndOfDayOrder()
 *
 * Complete Sequence Diagram flow:
 *   Msg #9  : Clock/Owner → SC : triggerEndOfDayOrder()
 *   Msg #10 : <<include>> JIT calculation (delegated to JITService)
 *   Msg #15 : SC → OL : <<create>> OrderList
 *   Msg #16-22 : LOOP for each SparePart
 *     Msg #17 : SC → SP : checkThreshold()         (<<include>> UC-06)
 *     Msg #18 : SP → SC : isBelowThreshold
 *     OPT [isBelowThreshold == true]:
 *       Msg #19 : SC → V : getVendorAddress()       (<<include>> UC-04)
 *       Msg #20 : V → SC : address
 *       Msg #21 : SC → OI : <<create>> OrderItem
 *       Msg #22 : SC → OL : addOrderItem(item)
 *   Msg #23 : SC → OL : print()
 *
 * ════════════════════════════════════════════════════════════════
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final SparePartRepository sparePartRepository;
    private final OrderListRepository orderListRepository;
    private final VendorService vendorService;
    private final JITService jitService;

    /**
     * Generate the end-of-day order list.
     *
     * This method implements the COMPLETE sequence for DFD P3.0
     * including all three <<include>> use cases.
     *
     * @return response containing the generated order
     */
    @Transactional
    public List<OrderResponseDTO> generateDailyOrder() {

        LocalDate today = LocalDate.now();

        log.info("═══ PROCESS 3.0: Generating Daily Order for {} ═══", today);

        if (orderListRepository.countByOrderDate(today) > 0) {
            log.warn("  Order(s) already exist for today: {}", today);
            return orderListRepository.findAllByOrderDateOrderByOrderIdAsc(today).stream()
                .map(this::buildOrderResponse)
                .collect(Collectors.toList());
        }

        log.info("  ┌── <<include>> Process 2.0: JIT Threshold Calculation");
        int thresholdsUpdated = jitService.calculateJITThresholds();
        log.info("  └── JIT complete: {} thresholds updated", thresholdsUpdated);

        List<SparePart> allParts = sparePartRepository.findAll();
        log.info("  Scanning {} parts against JIT thresholds...", allParts.size());

        Map<Long, List<OrderItem>> itemsByVendor = new LinkedHashMap<>();

        for (SparePart part : allParts) {
            if (!part.checkThreshold()) {
                continue;
            }

            log.debug("  ⚠ Part {} [{}] BELOW threshold: stock={}, threshold={}",
                part.getPartNumber(), part.getPartName(),
                part.getCurrentQuantity(), part.getThresholdValue());

            int reorderQty = JITCalculator.calculateReorderQuantity(
                part.getThresholdValue(),
                part.getCurrentQuantity()
            );

            Vendor vendor;
            try {
                vendor = vendorService.getVendorForOrder(part.getPartId());
            } catch (Exception e) {
                log.warn("    No vendor found for part {}, skipping", part.getPartNumber());
                continue;
            }

            String vAddr = vendor.getVendorAddress() != null ? vendor.getVendorAddress() : "";

            OrderItem orderItem = OrderItem.builder()
                .partNumber(part.getPartNumber())
                .partName(part.getPartName())
                .requiredQuantity(reorderQty)
                .vendorName(vendor.getVendorName())
                .vendorAddress(vAddr)
                .partId(part.getPartId())
                .vendorId(vendor.getVendorId())
                .build();

            itemsByVendor
                .computeIfAbsent(vendor.getVendorId(), k -> new ArrayList<>())
                .add(orderItem);
        }

        List<OrderResponseDTO> responses = new ArrayList<>();

        if (itemsByVendor.isEmpty()) {
            OrderList emptyList = OrderList.builder()
                .orderDate(today)
                .vendorId(null)
                .totalItems(0)
                .isPrinted(false)
                .orderItems(new ArrayList<>())
                .build();
            emptyList = orderListRepository.save(emptyList);
            String formattedOutput = OrderFormatter.formatOrderList(
                emptyList.getOrderId(),
                emptyList.getOrderDate(),
                emptyList.getOrderItems(),
                emptyList.getCreatedAt()
            );
            emptyList.setPrintText(formattedOutput);
            emptyList.setIsPrinted(true);
            emptyList = orderListRepository.save(emptyList);
            System.out.println(formattedOutput);
            responses.add(buildOrderResponse(emptyList));
            log.info("═══ PROCESS 3.0 COMPLETE: No reorders — placeholder Order #{} ═══",
                emptyList.getOrderId());
            return responses;
        }

        log.info("  Msg #23: Creating {} vendor order list(s)...", itemsByVendor.size());

        for (Map.Entry<Long, List<OrderItem>> entry : itemsByVendor.entrySet()) {
            Long vendorKey = entry.getKey();
            List<OrderItem> lineItems = entry.getValue();

            OrderList orderList = OrderList.builder()
                .orderDate(today)
                .vendorId(vendorKey)
                .totalItems(0)
                .isPrinted(false)
                .orderItems(new ArrayList<>())
                .build();

            for (OrderItem oi : lineItems) {
                orderList.addOrderItem(oi);
            }

            orderList = orderListRepository.save(orderList);

            String formattedOutput = OrderFormatter.formatOrderList(
                orderList.getOrderId(),
                orderList.getOrderDate(),
                orderList.getOrderItems(),
                orderList.getCreatedAt()
            );
            orderList.setPrintText(formattedOutput);
            orderList.setIsPrinted(true);
            orderList = orderListRepository.save(orderList);
            System.out.println(formattedOutput);

            log.info("  ✓ Order #{} — vendorId {} — {} line item(s)",
                orderList.getOrderId(), vendorKey, orderList.getTotalItemsCount());
            responses.add(buildOrderResponse(orderList));
        }

        log.info("═══ PROCESS 3.0 COMPLETE: {} vendor order list(s) ═══", responses.size());
        return responses;
    }

    /**
     * Get an existing order by its ID.
     *
     * @param orderId order database ID
     * @return order response
     */
    public OrderResponseDTO getOrderById(Long orderId) {
        OrderList order = orderListRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
        return buildOrderResponse(order);
    }

    /**
     * Get an order by date.
     *
     * @param date order date
     * @return order response
     */
    public List<OrderResponseDTO> getOrdersByDate(LocalDate date) {
        List<OrderList> lists = orderListRepository.findAllByOrderDateOrderByOrderIdAsc(date);
        if (lists.isEmpty()) {
            throw new OrderNotFoundException("No order found for date: " + date);
        }
        return lists.stream().map(this::buildOrderResponse).collect(Collectors.toList());
    }

    /**
     * Get all orders.
     *
     * @return list of all order responses
     */
    public List<OrderResponseDTO> getAllOrders() {
        return orderListRepository.findAllByOrderByOrderDateDesc()
            .stream()
            .map(this::buildOrderResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get the print-ready text for an order.
     *
     * @param orderId order ID
     * @return formatted text string
     */
    public String getOrderPrintText(Long orderId) {
        OrderList order = orderListRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getPrintText() != null) {
            return order.getPrintText();
        }

        // Regenerate print text if not stored
        String text = OrderFormatter.formatOrderList(
            order.getOrderId(),
            order.getOrderDate(),
            order.getOrderItems(),
            order.getCreatedAt()
        );
        order.setPrintText(text);
        order.setIsPrinted(true);
        orderListRepository.save(order);
        return text;
    }

    /**
     * Build OrderResponseDTO from OrderList entity.
     */
    private OrderResponseDTO buildOrderResponse(OrderList order) {

        List<OrderResponseDTO.OrderItemDTO> itemDTOs = order.getOrderItems()
            .stream()
            .map(item -> OrderResponseDTO.OrderItemDTO.builder()
                .itemId(item.getItemId())
                .partNumber(item.getPartNumber())
                .partName(item.getPartName())
                .requiredQuantity(item.getRequiredQuantity())
                .vendorName(item.getVendorName())
                .vendorAddress(item.getVendorAddress())
                .build()
            )
            .collect(Collectors.toList());

        return OrderResponseDTO.builder()
            .orderId(order.getOrderId())
            .vendorId(order.getVendorId())
            .orderDate(order.getOrderDate())
            .totalItems(order.getTotalItemsCount())
            .isPrinted(order.getIsPrinted())
            .printText(order.getPrintText())
            .createdAt(order.getCreatedAt())
            .items(itemDTOs)
            .build();
    }
}