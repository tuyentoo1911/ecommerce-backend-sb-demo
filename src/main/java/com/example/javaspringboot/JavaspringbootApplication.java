package com.example.javaspringboot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.example")
public class JavaspringbootApplication {

	public static void main(String[] args) {
		SpringApplication.run(JavaspringbootApplication.class, args);
	}

}
