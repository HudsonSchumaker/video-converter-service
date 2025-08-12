package br.schumaker.fcs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class FileConversionServiceApp {
	public static void main(String[] args) {
		SpringApplication.run(FileConversionServiceApp.class, args);
	}
}
