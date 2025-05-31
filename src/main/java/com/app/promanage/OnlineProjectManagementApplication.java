package com.app.promanage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class OnlineProjectManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(OnlineProjectManagementApplication.class, args);
	}

}
