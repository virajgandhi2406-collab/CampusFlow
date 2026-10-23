package com.campusflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class CampusFlowApplication {

    public static void main(String[] args) {
        // Enforce consistent UTC timezone application-wide
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        SpringApplication.run(CampusFlowApplication.class, args);
    }
}
