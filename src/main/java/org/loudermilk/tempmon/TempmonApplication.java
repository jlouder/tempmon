package org.loudermilk.tempmon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TempmonApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(TempmonApplication.class, args);
	}

}
