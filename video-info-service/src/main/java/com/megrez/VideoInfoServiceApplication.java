package com.megrez;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class VideoInfoServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(VideoInfoServiceApplication.class, args);
    }
}
