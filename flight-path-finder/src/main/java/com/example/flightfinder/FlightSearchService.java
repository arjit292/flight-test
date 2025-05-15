package com.example.flightfinder;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class FlightSearchService {
    @Autowired
    private FlightRepository repo;

    public List<Map<String, Map<String, Integer>>> findFastestPaths(String from, String to) {
        Map<String, Map<String, Integer>> directMap = new HashMap<>();
        Map<String, Map<String, Integer>> oneStopMap = new LinkedHashMap<>();
        List<Map<String, Map<String, Integer>>> result = new ArrayList<>();

        // Direct flights
        List<Flight> direct = repo.findByFromAirportAndToAirport(from, to);
        Map<String, Integer> directFlights = new HashMap<>();
        for (Flight f : direct) {
            directFlights.put(f.getFlightNo(), f.getDuration());
        }
        if (!directFlights.isEmpty()) {
            directMap.put(from + "_" + to, directFlights);
            result.add(directMap);
        }

        // One-stop flights
        List<Flight> firstLegs = repo.findByFromAirport(from);
        List<Flight> secondLegs = repo.findByToAirport(to);
        List<OneStopFlight> oneStops = new ArrayList<>();

        for (Flight f1 : firstLegs) {
            for (Flight f2 : secondLegs) {
                String via = f1.getToAirport();

                if (!via.equals(from) && !via.equals(to) && via.equals(f2.getFromAirport())) {

                    int adjustedF2Start = f2.getStartTime();
                    if (adjustedF2Start < f1.getEndTime()) {
                        adjustedF2Start += 1440; // wrap around
                    }

                    if (adjustedF2Start >= f1.getEndTime() + 120) {
                        int totalDuration = f1.getDuration()
                                + (adjustedF2Start - f1.getEndTime())
                                + f2.getDuration();

                        oneStops.add(new OneStopFlight(from, via, to,
                                f1.getFlightNo(), f2.getFlightNo(), totalDuration));
                    }
                }
            }
        }

        Set<String> directFlightNos = direct.stream()
                .map(Flight::getFlightNo)
                .collect(Collectors.toSet());

        oneStops.sort(Comparator.comparingInt(o -> o.totalDuration));
        Set<String> usedVias = new HashSet<>();
        int remainingSlots = 5 - directFlights.size();

        for (OneStopFlight f : oneStops) {
            if (remainingSlots <= 0) break;
            if (directFlightNos.contains(f.flight2)) continue;
            if (usedVias.contains(f.via)) continue;

            usedVias.add(f.via);
            String key = f.src + "_" + f.via + "_" + f.dst;
            String flights = f.flight1 + "_" + f.flight2;
            oneStopMap.put(key, Map.of(flights, f.totalDuration));
            remainingSlots--;
        }

        if (!oneStopMap.isEmpty()) result.add(oneStopMap);
        return result;
    }

    record OneStopFlight(String src, String via, String dst, String flight1, String flight2, int totalDuration) {}
}