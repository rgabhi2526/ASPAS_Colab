package com.aspas.scheduler;

import com.aspas.model.dto.OrderResponseDTO;
import com.aspas.service.SystemControllerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@RequiredArgsConstructor
@Slf4j
public class EndOfDayScheduler {

    private final SystemControllerService systemController;

    private static final DateTimeFormatter DATETIME_FORMAT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Scheduled(cron = "0 55 23 * * *")
    public void executeEndOfDayProcess() {

        LocalDateTime startTime = LocalDateTime.now();

        log.info("╔══════════════════════════════════════════════════════════╗");
        log.info("║  SYSTEM CLOCK: End-of-Day Process Triggered             ║");
        log.info("║  Time: {}                              ║", startTime.format(DATETIME_FORMAT));
        log.info("╠══════════════════════════════════════════════════════════╣");

        try {

            OrderResponseDTO order = systemController.triggerEndOfDayOrder();

            LocalDateTime endTime = LocalDateTime.now();
            long durationMs = java.time.Duration.between(startTime, endTime).toMillis();

            log.info("║                                                          ║");
            log.info("║  ✓ Order Generated Successfully                          ║");
            log.info("║  Order ID    : {}                                        ║",
                order.getOrderId());
            log.info("║  Items       : {}                                        ║",
                order.getTotalItems());
            log.info("║  Is Printed  : {}                                        ║",
                order.getIsPrinted());
            log.info("║  Duration    : {} ms                                     ║",
                durationMs);
            log.info("╚══════════════════════════════════════════════════════════╝");

        } catch (Exception e) {
            log.error("║                                                          ║");
            log.error("║  ✗ End-of-Day Process FAILED                             ║");
            log.error("║  Error: {}                                               ║",
                e.getMessage());
            log.error("╚══════════════════════════════════════════════════════════╝");
            log.error("Stack trace:", e);
        }
    }

    @Scheduled(cron = "0 0 * * * *")
    public void schedulerHeartbeat() {
        log.debug("[SCHEDULER HEARTBEAT] System Clock active at {}",
            LocalDateTime.now().format(DATETIME_FORMAT));
    }
}