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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
    public OrderResponseDTO generateDailyOrder() {

        LocalDate today = LocalDate.now();

        log.info("═══ PROCESS 3.0: Generating Daily Order for {} ═══", today);

        // Check if order already generated today
        if (orderListRepository.existsByOrderDate(today)) {
            log.warn("  Order already exists for today: {}", today);
            OrderList existing = orderListRepository.findByOrderDate(today)
                .orElseThrow(() -> new OrderNotFoundException("Order not found for: " + today));
            return buildOrderResponse(existing);
        }

        // ─────────────────────────────────────────────
        // SEQUENCE DIAGRAM: Message #10-14
        // <<include>> Process 2.0: Calculate JIT Thresholds
        // ─────────────────────────────────────────────
        log.info("  ┌── <<include>> Process 2.0: JIT Threshold Calculation");
        int thresholdsUpdated = jitService.calculateJITThresholds();
        log.info("  └── JIT complete: {} thresholds updated", thresholdsUpdated);

        // ─────────────────────────────────────────────
        // SEQUENCE DIAGRAM: Message #15
        // SC → OL : <<create>> OrderList(systemDate)
        // ─────────────────────────────────────────────
        OrderList orderList = OrderList.builder()
            .orderDate(today)
            .totalItems(0)
            .isPrinted(false)
            .orderItems(new ArrayList<>())
            .build();

        log.debug("  Msg #15: OrderList created for date: {}", today);

        // ─────────────────────────────────────────────
        // SEQUENCE DIAGRAM: Messages #16-22
        // LOOP: for each SparePart
        // ─────────────────────────────────────────────
        List<SparePart> allParts = sparePartRepository.findAll();
        log.info("  Scanning {} parts against JIT thresholds...", allParts.size());

        for (SparePart part : allParts) {

            // ─────────────────────────────────────────
            // SEQUENCE DIAGRAM: Message #17-18
            // <<include>> UC-06: Check Inventory vs Threshold
            // SC → SP : checkThreshold()
            // SP → SC : isBelowThreshold
            // ─────────────────────────────────────────
            boolean belowThreshold = part.checkThreshold();

            // ─────────────────────────────────────────
            // SEQUENCE DIAGRAM: OPT [isBelowThreshold == true]
            // ─────────────────────────────────────────
            if (belowThreshold) {

                log.debug("  ⚠ Part {} [{}] BELOW threshold: stock={}, threshold={}",
                    part.getPartNumber(), part.getPartName(),
                    part.getCurrentQuantity(), part.getThresholdValue());

                // Calculate reorder quantity
                int reorderQty = JITCalculator.calculateReorderQuantity(
                    part.getThresholdValue(),
                    part.getCurrentQuantity()
                );

                // ─────────────────────────────────────
                // SEQUENCE DIAGRAM: Message #19-20
                // <<include>> UC-04: Fetch Vendor Address
                // SC → V : getVendorAddress()
                // V → SC : address
                // ─────────────────────────────────────
                Vendor vendor;
                try {
                    vendor = vendorService.getVendorForOrder(part.getPartId());
                } catch (Exception e) {
                    log.warn("    No vendor found for part {}, skipping", part.getPartNumber());
                    continue;
                }

                log.debug("    Msg #19-20: Vendor found — {} at {}",
                    vendor.getVendorName(), vendor.getVendorAddress());

                // ─────────────────────────────────────
                // SEQUENCE DIAGRAM: Message #21
                // SC → OI : <<create>> OrderItem
                // ─────────────────────────────────────
                OrderItem orderItem = OrderItem.builder()
                    .partNumber(part.getPartNumber())
                    .partName(part.getPartName())
                    .requiredQuantity(reorderQty)
                    .vendorName(vendor.getVendorName())
                    .vendorAddress(vendor.getVendorAddress())
                    .partId(part.getPartId())
                    .vendorId(vendor.getVendorId())
                    .build();

                log.debug("    Msg #21: OrderItem created — Part: {}, Qty: {}, Vendor: {}",
                    part.getPartNumber(), reorderQty, vendor.getVendorName());

                // ─────────────────────────────────────
                // SEQUENCE DIAGRAM: Message #22
                // SC → OL : addOrderItem(item)
                // ─────────────────────────────────────
                orderList.addOrderItem(orderItem);
                log.debug("    Msg #22: Item added to order list");
            }
        }

        // ─────────────────────────────────────────────
        // SEQUENCE DIAGRAM: Message #23
        // SC → OL : print()
        // OL → OL : formatOutput()  (Printable interface)
        // ─────────────────────────────────────────────
        log.info("  Msg #23: Generating print output...");

        // Save first to get orderId for formatting
        orderList = orderListRepository.save(orderList);

        // Generate formatted text using OrderFormatter utility
        String formattedOutput = OrderFormatter.formatOrderList(
            orderList.getOrderId(),
            orderList.getOrderDate(),
            orderList.getOrderItems(),
            orderList.getCreatedAt()
        );

        // Store formatted text and mark as printed
        orderList.setPrintText(formattedOutput);
        orderList.setIsPrinted(true);
        orderList = orderListRepository.save(orderList);

        // Print to console (backend output)
        System.out.println(formattedOutput);

        log.info("═══ PROCESS 3.0 COMPLETE: Order #{} — {} items to reorder ═══",
            orderList.getOrderId(), orderList.getTotalItemsCount());

        return buildOrderResponse(orderList);
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
    public OrderResponseDTO getOrderByDate(LocalDate date) {
        OrderList order = orderListRepository.findByOrderDate(date)
            .orElseThrow(() -> new OrderNotFoundException(
                "No order found for date: " + date
            ));
        return buildOrderResponse(order);
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
            .orderDate(order.getOrderDate())
            .totalItems(order.getTotalItemsCount())
            .isPrinted(order.getIsPrinted())
            .printText(order.getPrintText())
            .createdAt(order.getCreatedAt())
            .items(itemDTOs)
            .build();
    }
}