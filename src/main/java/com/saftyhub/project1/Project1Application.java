package com.saftyhub.project1;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;


@EntityScan(basePackages = "com.saftyhub.project1.model")
@EnableJpaRepositories(basePackages = "com.saftyhub.project1.repository")
@SpringBootApplication
public class Project1Application {
    public static void main(String[] args) {
        SpringApplication.run(Project1Application.class, args);
    }
}
