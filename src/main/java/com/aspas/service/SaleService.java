package com.aspas.service;

import com.aspas.exception.InsufficientStockException;
import com.aspas.exception.PartNotFoundException;
import com.aspas.model.document.SalesTransactionDoc;
import com.aspas.model.dto.SaleRequestDTO;
import com.aspas.model.dto.SaleResponseDTO;
import com.aspas.model.entity.SparePart;
import com.aspas.repository.jpa.SparePartRepository;
import com.aspas.repository.mongo.SalesTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SaleService {

    private final SparePartRepository sparePartRepository;
    private final SalesTransactionRepository salesTransactionRepository;

    @Transactional
    public SaleResponseDTO processSale(SaleRequestDTO request) {

        String partNumber = request.getPartNumber();
        int quantity = request.getQuantity();

        log.info("═══ PROCESS 1.0: Processing sale — Part: {}, Qty: {} ═══",
            partNumber, quantity);

        SparePart part = sparePartRepository.findByPartNumber(partNumber)
            .orElseThrow(() -> {
                log.error("Part not found: {}", partNumber);
                return new PartNotFoundException(partNumber);
            });

        double unitPrice = part.getUnitPrice();
        log.debug("  Msg #2-3: Part found — {} [{}], Price: ₹{}, Stock: {}",
            part.getPartNumber(), part.getPartName(), unitPrice, part.getCurrentQuantity());

        if (part.getCurrentQuantity() < quantity) {
            log.error("  Insufficient stock: requested {}, available {}",
                quantity, part.getCurrentQuantity());
            throw new InsufficientStockException(
                partNumber, quantity, part.getCurrentQuantity()
            );
        }

        part.updateQuantity(-quantity);
        sparePartRepository.save(part);
        log.debug("  Msg #4-5: Stock updated — New quantity: {}", part.getCurrentQuantity());

        String transactionId = generateTransactionId();
        LocalDateTime now = LocalDateTime.now();

        SalesTransactionDoc transaction = new SalesTransactionDoc(
            transactionId,
            now,
            part.getPartNumber(),
            part.getPartName(),
            quantity,
            unitPrice
        );
        log.debug("  Msg #6: Transaction created — ID: {}, Revenue: ₹{}",
            transactionId, transaction.getRevenueAmount());

        transaction.logSale();
        salesTransactionRepository.save(transaction);
        log.debug("  Msg #7: Transaction logged to MongoDB");

        SaleResponseDTO response = SaleResponseDTO.builder()
            .transactionId(transactionId)
            .transactionDate(now)
            .partNumber(part.getPartNumber())
            .partName(part.getPartName())
            .quantitySold(quantity)
            .unitPrice(unitPrice)
            .revenueAmount(transaction.getRevenueAmount())
            .message("Sale processed successfully")
            .build();

        log.info("═══ PROCESS 1.0 COMPLETE: TXN {} — Revenue: ₹{} ═══",
            transactionId, transaction.getRevenueAmount());

        return response;
    }

    private String generateTransactionId() {
        String datePart = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String uuidPart = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "TXN-" + datePart + "-" + uuidPart;
    }
}