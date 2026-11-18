package com.hireflow.offres;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient

@SpringBootApplication
public class ServiceOffresApplication {

    public static void main(String[] args) {
        SpringApplication.run(ServiceOffresApplication.class, args);
    }
}
