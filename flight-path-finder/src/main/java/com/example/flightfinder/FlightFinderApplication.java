
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

    @Bean
    CommandLineRunner loadFlights(FlightRepository repository) {
        return args -> {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("testfile.csv");
            try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                br.readLine(); // skip header
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split(",");
                    if (parts.length < 5) continue;
                    String flightNo = parts[0].trim();
                    String from = parts[1].trim();
                    String to = parts[2].trim();
                    int start = TimeUtils.hhmmToMinutes(parts[3].trim());
                    int end = TimeUtils.hhmmToMinutes(parts[4].trim());
                    int duration = TimeUtils.calculateDuration(start, end);
                    repository.save(new Flight(flightNo, from, to, start, end, duration));
                }
            }
        };
    }
}




