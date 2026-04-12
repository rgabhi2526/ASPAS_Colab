package com.aspas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling      
@EnableJpaAuditing     
public class AspasApplication {

    public static void main(String[] args) {
        SpringApplication.run(AspasApplication.class, args);

        System.out.println("============================================");
        System.out.println("  ASPAS Backend Started Successfully");
        System.out.println("  Swagger UI : http:");
        System.out.println("  API Docs   : http:");
        System.out.println("============================================");
    }
}