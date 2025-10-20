package com.br.pixservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages = "com.br.*")
@SpringBootApplication
public class PixServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(PixServiceApplication.class, args);
	}

}
