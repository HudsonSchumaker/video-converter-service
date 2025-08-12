package com.example.file_conversion_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class FileConversionServiceApplication {
	public static void main(String[] args) {
		SpringApplication.run(FileConversionServiceApplication.class, args);
	}
}
