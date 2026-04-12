package com.aspas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling      // Enables System Clock actor (@Scheduled cron jobs)
@EnableJpaAuditing     // Enables automatic timestamp management
public class AspasApplication {

    public static void main(String[] args) {
        SpringApplication.run(AspasApplication.class, args);

        System.out.println("============================================");
        System.out.println("  ASPAS Backend Started Successfully");
        System.out.println("  Swagger UI : http://localhost:8080/swagger-ui.html");
        System.out.println("  API Docs   : http://localhost:8080/api-docs");
        System.out.println("============================================");
    }
}