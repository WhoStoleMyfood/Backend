package com.example.whostolemyfood;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class WhostolemyfoodApplication {

	public static void main(String[] args) {
		SpringApplication.run(WhostolemyfoodApplication.class, args);
	}
}