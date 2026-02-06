package com.byteCore.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ByteCoreApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ByteCoreApiApplication.class, args);
	}

}
