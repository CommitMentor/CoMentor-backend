package com.knu.coment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CoMentApplication {

    public static void main(String[] args) {
        SpringApplication.run(CoMentApplication.class, args);
    }

}
