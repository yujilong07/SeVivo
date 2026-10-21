package com.meilong.messenger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SeVivo {
    public static void main(String[] args) {
        SpringApplication.run(SeVivo.class, args);
    }
}
