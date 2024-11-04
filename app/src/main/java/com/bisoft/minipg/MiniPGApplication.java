package com.bisoft.minipg;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

@SpringBootApplication
 class MinipgApplication {

	@Bean
	public static PropertySourcesPlaceholderConfigurer createPropertyConfigurer()
	{
		PropertySourcesPlaceholderConfigurer propertyConfigurer = new PropertySourcesPlaceholderConfigurer();
		propertyConfigurer.setTrimValues(true);
		return propertyConfigurer;
	}
	
	public static void main(String[] args) {
		SpringApplication.run(MinipgApplication.class, args);
	}

}
