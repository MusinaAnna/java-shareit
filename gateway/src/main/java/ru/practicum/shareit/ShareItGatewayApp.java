package ru.practicum.shareit;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

@SpringBootApplication(scanBasePackages = "ru.practicum.shareit")
public class ShareItGatewayApp {

    public static void main(String[] args) {
        SpringApplication.run(ShareItGatewayApp.class, args);
    }

    @Bean
    public CommandLineRunner checkBeans(ApplicationContext ctx) {
        return args -> {
            System.out.println("===== All beans =====");
            for (String name : ctx.getBeanDefinitionNames()) {
                System.out.println(name);
            }
            System.out.println("=====================");
        };
    }
}
