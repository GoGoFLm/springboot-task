package com.iask.seo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


/**
 * 
 * @author flm
 *
 */
@SpringBootApplication
@EnableScheduling
public class Application {
	
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}