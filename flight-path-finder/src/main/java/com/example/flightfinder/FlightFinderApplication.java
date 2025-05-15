
package com.example.flightfinder;


import ch.qos.logback.core.util.TimeUtil;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;

@SpringBootApplication
public class FlightFinderApplication {
    public static void main(String[] args) {
        SpringApplication.run(FlightFinderApplication.class, args);
    }
}




