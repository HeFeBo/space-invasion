package com.hefebo.invasion_v2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class InvasionV2Application {

	public static void main(String[] args) {
		SpringApplication.run(InvasionV2Application.class, args);
	}

}
