package com.meetflow.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MeetflowBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(MeetflowBackendApplication.class, args);
        System.out.println("=================================================");
        System.out.println("🚀 MeetFlow Spring Boot Backend iniciado en http://localhost:8080");
        System.out.println("📊 Consola H2 disponible en http://localhost:8080/h2-console");
        System.out.println("=================================================");
    }
}
