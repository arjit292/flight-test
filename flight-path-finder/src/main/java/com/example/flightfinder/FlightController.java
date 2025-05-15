package com.example.flightfinder;

import com.example.flightfinder.FlightSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class FlightController {
    @Autowired
    private FlightSearchService service;

    @GetMapping("/fastest-paths")
    public List<Map<String, Map<String, Integer>>> getPaths(@RequestParam String from, @RequestParam String to) {
        return service.findFastestPaths(from.toUpperCase(), to.toUpperCase());
    }
}


