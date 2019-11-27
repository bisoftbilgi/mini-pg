package com.bisoft.minipg;

 import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.bisoft.minipg.service.MiniPGService;

@SpringBootApplication
public class MiniPGApplication implements CommandLineRunner {
 
	@Autowired
	MiniPGService minipgService;
 

	public static void main(String[] args) {
		SpringApplication.run(MiniPGApplication.class, args);
	}

	public void run(String... args) throws Exception {
		minipgService.start();
	}
}
