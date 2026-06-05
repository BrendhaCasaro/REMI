package com.brendhacasaro.remi_central;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RemiCentralApplication {

    public static void main(String[] args) {
        SpringApplication.run(RemiCentralApplication.class, args);
    }

}
