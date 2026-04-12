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

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final SparePartRepository sparePartRepository;
    private final OrderListRepository orderListRepository;
    private final VendorService vendorService;
    private final JITService jitService;

    @Transactional
    public OrderResponseDTO generateDailyOrder() {

        LocalDate today = LocalDate.now();

        log.info("═══ PROCESS 3.0: Generating Daily Order for {} ═══", today);

        if (orderListRepository.existsByOrderDate(today)) {
            log.warn("  Order already exists for today: {}", today);
            OrderList existing = orderListRepository.findByOrderDate(today)
                .orElseThrow(() -> new OrderNotFoundException("Order not found for: " + today));
            return buildOrderResponse(existing);
        }

        log.info("  ┌── <<include>> Process 2.0: JIT Threshold Calculation");
        int thresholdsUpdated = jitService.calculateJITThresholds();
        log.info("  └── JIT complete: {} thresholds updated", thresholdsUpdated);

        OrderList orderList = OrderList.builder()
            .orderDate(today)
            .totalItems(0)
            .isPrinted(false)
            .orderItems(new ArrayList<>())
            .build();

        log.debug("  Msg #15: OrderList created for date: {}", today);

        List<SparePart> allParts = sparePartRepository.findAll();
        log.info("  Scanning {} parts against JIT thresholds...", allParts.size());

        for (SparePart part : allParts) {

            boolean belowThreshold = part.checkThreshold();

            if (belowThreshold) {

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

                log.debug("    Msg #19-20: Vendor found — {} at {}",
                    vendor.getVendorName(), vendor.getVendorAddress());

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

                orderList.addOrderItem(orderItem);
                log.debug("    Msg #22: Item added to order list");
            }
        }

        log.info("  Msg #23: Generating print output...");

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

        log.info("═══ PROCESS 3.0 COMPLETE: Order #{} — {} items to reorder ═══",
            orderList.getOrderId(), orderList.getTotalItemsCount());

        return buildOrderResponse(orderList);
    }

    public OrderResponseDTO getOrderById(Long orderId) {
        OrderList order = orderListRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));
        return buildOrderResponse(order);
    }

    public OrderResponseDTO getOrderByDate(LocalDate date) {
        OrderList order = orderListRepository.findByOrderDate(date)
            .orElseThrow(() -> new OrderNotFoundException(
                "No order found for date: " + date
            ));
        return buildOrderResponse(order);
    }

    public List<OrderResponseDTO> getAllOrders() {
        return orderListRepository.findAllByOrderByOrderDateDesc()
            .stream()
            .map(this::buildOrderResponse)
            .collect(Collectors.toList());
    }

    public String getOrderPrintText(Long orderId) {
        OrderList order = orderListRepository.findById(orderId)
            .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (order.getPrintText() != null) {
            return order.getPrintText();
        }

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