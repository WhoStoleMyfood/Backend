package com.example.whostolemyfood;

import jakarta.persistence.EntityListeners;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

//@EnableJpaAuditing
@EnableCaching
@SpringBootApplication
//@EntityListeners(AuditingEntityListener.class)
public class WhostolemyfoodApplication {

	public static void main(String[] args) {
		SpringApplication.run(WhostolemyfoodApplication.class, args);
	}
}