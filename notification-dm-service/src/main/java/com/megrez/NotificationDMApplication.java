package com.megrez;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class NotificationDMApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotificationDMApplication.class, args);
    }
}
