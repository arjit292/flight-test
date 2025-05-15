package com.example.flightfinder;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Component
public class FlightDataLoader {

    @Autowired
    private FlightRepository repository;
    @Autowired
    private ResourceLoader resourceLoader;


    @PostConstruct
    public void loadCsvData() throws IOException {

        Resource resource = resourceLoader.getResource("classpath:testfile.csv");
        if (!resource.exists()) {
            throw new RuntimeException("CSV file not found in classpath");
        }
        InputStream inputStream = resource.getInputStream();
        if (inputStream == null) {
            throw new RuntimeException("CSV file not found in resources: testfile.csv");
        }

        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line = br.readLine(); // Skip header
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
    }
}
