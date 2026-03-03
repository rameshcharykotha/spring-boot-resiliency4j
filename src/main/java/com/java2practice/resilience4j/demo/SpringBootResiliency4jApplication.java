package com.java2practice.resilience4j.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class SpringBootResiliency4jApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootResiliency4jApplication.class, args);
	}

	// Expose a RestTemplate bean for performing REST calls
	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

}
