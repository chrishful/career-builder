package dev.chrishful.career.builder;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CareerBuilderApplication {

	public static void main(String[] args) {
		SpringApplication.run(CareerBuilderApplication.class, args);
	}

    @Value("${webhook.secret}")
    private String rawSecret;

    @Bean
    public String webhookSecret() {
        return rawSecret;
    }
}
