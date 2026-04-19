package com.aspas;

import com.aspas.repository.mongo.SalesTransactionRepository;
import com.aspas.repository.mongo.PartSalesAggregate;
import com.aspas.service.JITService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;

@SpringBootApplication
public class ReportTest {
    public static void main(String[] args) {
        SpringApplication.run(ReportTest.class, args).close();
    }

    @Bean
    public CommandLineRunner runData(SalesTransactionRepository repo, JITService jitService, com.aspas.config.BusinessDateBounds bounds) {
        return args -> {
            System.out.println("====== TEST START ======");
            
            LocalDateTime endDate = LocalDateTime.now();
            LocalDateTime startDate = endDate.minusDays(7).with(LocalTime.MIN);
            Date d0 = bounds.toMongoDate(startDate);
            Date d1 = bounds.toMongoDate(endDate);
            System.out.println("d0=" + d0 + " d1=" + d1);
            
            String partNumber = "SP-BRK-001";
            
            long cnt = repo.countByPartNumber(partNumber);
            System.out.println("Count for " + partNumber + ": " + cnt);
            
            long cntRange = repo.countByDateRange(startDate, endDate);
            System.out.println("Count in range: " + cntRange);

            List<PartSalesAggregate> result = repo.aggregateSalesForPart(partNumber, d0, d1);
            if(result != null && !result.isEmpty()) {
                System.out.println("Aggregate TotalQty: " + result.get(0).getTotalQty());
            } else {
                System.out.println("Aggregate returned empty or null");
            }
            
            System.out.println("JITService test: " + jitService.calculateThresholdForPart(partNumber));
            
            System.out.println("====== TEST END ======");
        };
    }
}
