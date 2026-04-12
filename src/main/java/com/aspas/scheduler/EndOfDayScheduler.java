package com.aspas.scheduler;

import com.aspas.model.dto.OrderResponseDTO;
import com.aspas.service.SystemControllerService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ================================================================
 * EndOfDayScheduler — System Clock Actor
 * ================================================================
 *
 * UML Traceability:
 *   - Use Case Diagram: "<<system>> System Clock" actor
 *   - Sequence Diagram: Message #9 "Clock → SC : triggerEndOfDayOrder()"
 *   - DFD: System Clock triggers P3.0 at end of day
 *
 * This class represents the "System Clock" actor from the
 * Use Case Diagram. It automatically triggers the end-of-day
 * order generation process.
 *
 * Schedule:
 *   - Runs daily at 23:55 (11:55 PM)
 *   - Triggers the complete P3.0 flow:
 *       1. <<include>> P2.0 JIT threshold recalculation
 *       2. Inventory scan against thresholds
 *       3. Vendor address lookup for below-threshold parts
 *       4. Order list generation and print
 *
 * Configuration:
 *   - Enabled by @EnableScheduling in AspasApplication.java
 *   - Thread pool configured in SchedulerConfig.java
 *
 * ================================================================
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EndOfDayScheduler {

    private final SystemControllerService systemController;

    private static final DateTimeFormatter DATETIME_FORMAT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * End-of-day automated order generation.
     *
     * UML Traceability:
     *   Sequence Diagram → Message #9: "Clock → SC : triggerEndOfDayOrder()"
     *   Use Case Diagram: System Clock → UC-03 (via UC-02 <<include>>)
     *
     * Cron expression: "0 55 23 * * *"
     *   - Second: 0
     *   - Minute: 55
     *   - Hour  : 23 (11 PM)
     *   - Day   : * (every day)
     *   - Month : * (every month)
     *   - DOW   : * (every day of week)
     *
     * This runs DAILY at 23:55 and triggers the complete
     * end-of-day order generation pipeline.
     */
    @Scheduled(cron = "0 55 23 * * *")
    public void executeEndOfDayProcess() {

        LocalDateTime startTime = LocalDateTime.now();

        log.info("╔══════════════════════════════════════════════════════════╗");
        log.info("║  SYSTEM CLOCK: End-of-Day Process Triggered             ║");
        log.info("║  Time: {}                              ║", startTime.format(DATETIME_FORMAT));
        log.info("╠══════════════════════════════════════════════════════════╣");

        try {
            // ─────────────────────────────────────────────
            // SEQUENCE DIAGRAM: Message #9
            // Clock → SC : triggerEndOfDayOrder()
            //
            // This internally triggers:
            //   Message #10-14 : <<include>> P2.0 JIT calculation
            //   Message #15    : <<create>> OrderList
            //   Message #16-22 : LOOP scan + order items
            //   Message #23    : print()
            // ─────────────────────────────────────────────
            List<OrderResponseDTO> orders = systemController.triggerEndOfDayOrder();

            LocalDateTime endTime = LocalDateTime.now();
            long durationMs = java.time.Duration.between(startTime, endTime).toMillis();

            int lines = orders.stream().mapToInt(o -> o.getTotalItems() != null ? o.getTotalItems() : 0).sum();
            log.info("║                                                          ║");
            log.info("║  ✓ EOD orders generated                                  ║");
            log.info("║  Vendor lists: {}                                        ║",
                orders.size());
            log.info("║  Total line items: {}                                    ║",
                lines);
            log.info("║  First order ID : {}                                     ║",
                orders.isEmpty() ? "—" : orders.get(0).getOrderId());
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

    /**
     * Health check log — runs every hour to confirm scheduler is alive.
     *
     * Cron: "0 0 * * * *" → every hour at minute 0
     */
    @Scheduled(cron = "0 0 * * * *")
    public void schedulerHeartbeat() {
        log.debug("[SCHEDULER HEARTBEAT] System Clock active at {}",
            LocalDateTime.now().format(DATETIME_FORMAT));
    }
}