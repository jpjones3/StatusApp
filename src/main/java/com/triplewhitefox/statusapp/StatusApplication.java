package com.triplewhitefox.statusapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan
public class StatusApplication extends SpringBootServletInitializer {

	public static void main(String[] args) {
		SpringApplication.run(StatusApplication.class, args);
	}

}
