package com.hireflow.candidatures;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ServiceCandidaturesApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceCandidaturesApplication.class, args);
    }
}
