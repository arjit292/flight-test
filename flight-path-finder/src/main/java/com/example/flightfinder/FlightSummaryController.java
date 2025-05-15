package com.example.flightfinder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.text.DecimalFormat;
import java.util.*;

@RestController
public class FlightSummaryController {

    @Autowired
    private FlightRepository repo;

    private static final Map<String, String> airportNames = new HashMap<>();
    static {
        airportNames.put("IXC", "Chandigarh International Airport");
        airportNames.put("DEL", "Indira Gandhi International Airport");
        airportNames.put("GAU", "Lokpriya Gopinath Bordoloi Airport");
        airportNames.put("BLR", "Kempegowda International Airport");
        airportNames.put("HYD", "Rajiv Gandhi International Airport");
        airportNames.put("CCU", "Netaji Subhas Chandra Bose International Airport");
        airportNames.put("ATQ", "Sri Guru Ram Dass Jee International Airport");
        airportNames.put("BOM", "Chhatrapati Shivaji Maharaj International Airport");
        airportNames.put("PNQ", "Pune International Airport");
        airportNames.put("MAA", "Chennai International Airport");
        airportNames.put("PAT", "Jay Prakash Narayan Airport");
    }

    // One-way flight summary endpoint
    @GetMapping("/api/flight-summary")
    public List<Map<String, Object>> getFlightSummary(@RequestParam String from, @RequestParam String to, @RequestParam(required = false) String departureDate) {
        return findFlightOptions(from, to);
    }

    // Round-trip flight summary endpoint
    @GetMapping("/api/roundtrip-summary")
    public Map<String, List<Map<String, Object>>> getRoundTripSummary(@RequestParam String from,
                                                                      @RequestParam String to,
                                                                      @RequestParam(required = false) String departureDate,
                                                                      @RequestParam(required = false) String returnDate) {
        Map<String, List<Map<String, Object>>> result = new LinkedHashMap<>();
        result.put("onward", findFlightOptions(from, to));
        result.put("return", findFlightOptions(to, from));
        return result;
    }

    private List<Map<String, Object>> findFlightOptions(String from, String to) {
        List<Map<String, Object>> result = new ArrayList<>();

        List<Flight> direct = repo.findByFromAirportAndToAirport(from, to);
        List<Flight> firstLegs = repo.findByFromAirport(from);
        List<Flight> secondLegs = repo.findByToAirport(to);

        List<SummaryRoute> oneStops = new ArrayList<>();

        for (Flight f1 : firstLegs) {
            for (Flight f2 : secondLegs) {
                if (f1.getToAirport().equals(f2.getFromAirport()) &&
                        !f1.getToAirport().equals(from) && !f1.getToAirport().equals(to)) {

                    int adjustedF2Start = f2.getStartTime();
                    if (adjustedF2Start < f1.getEndTime()) adjustedF2Start += 1440;

                    if (adjustedF2Start >= f1.getEndTime() + 120) {
                        int layover = adjustedF2Start - f1.getEndTime();
                        int duration = f1.getDuration() + layover + f2.getDuration();

                        oneStops.add(new SummaryRoute(
                                List.of(f1, f2), duration, layover));
                    }
                }
            }
        }

        oneStops.sort(Comparator.comparingInt(r -> r.duration));

        Set<String> usedVias = new HashSet<>();
        int totalNeeded = 5 - Math.min(5, direct.size());

        for (SummaryRoute route : oneStops) {
            String via = route.legs.get(0).getToAirport();
            if (usedVias.contains(via)) continue;
            usedVias.add(via);

            Map<String, Object> routeMap = new LinkedHashMap<>();
            routeMap.put("stops", 1);
            routeMap.put("totalDurationMinutes", route.duration);
            routeMap.put("layoverMinutes", route.layover);

            List<Map<String, Object>> legs = new ArrayList<>();
            for (Flight f : route.legs) {
                Map<String, Object> leg = new LinkedHashMap<>();
                leg.put("from", f.getFromAirport());
                leg.put("fromName", airportNames.getOrDefault(f.getFromAirport(), "Unknown"));
                leg.put("to", f.getToAirport());
                leg.put("toName", airportNames.getOrDefault(f.getToAirport(), "Unknown"));
                leg.put("flightNo", f.getFlightNo());
                leg.put("startTime", formatTime(f.getStartTime()));
                leg.put("endTime", formatTime(f.getEndTime()));
                legs.add(leg);
            }
            routeMap.put("legs", legs);
            result.add(routeMap);

            if (result.size() >= totalNeeded) break;
        }

        for (Flight f : direct) {
            Map<String, Object> routeMap = new LinkedHashMap<>();
            routeMap.put("stops", 0);
            routeMap.put("totalDurationMinutes", f.getDuration());
            routeMap.put("layoverMinutes", 0);

            Map<String, Object> leg = new LinkedHashMap<>();
            leg.put("from", f.getFromAirport());
            leg.put("fromName", airportNames.getOrDefault(f.getFromAirport(), "Unknown"));
            leg.put("to", f.getToAirport());
            leg.put("toName", airportNames.getOrDefault(f.getToAirport(), "Unknown"));
            leg.put("flightNo", f.getFlightNo());
            leg.put("startTime", formatTime(f.getStartTime()));
            leg.put("endTime", formatTime(f.getEndTime()));

            routeMap.put("legs", List.of(leg));
            result.add(routeMap);
        }

        result.sort(Comparator.comparingInt(r -> (Integer) r.get("totalDurationMinutes")));
        return result.subList(0, Math.min(result.size(), 5));
    }

    private String formatTime(int minutesSinceMidnight) {
        int hours = (minutesSinceMidnight / 60) % 24;
        int minutes = minutesSinceMidnight % 60;
        return String.format("%02d:%02d", hours, minutes);
    }

    record SummaryRoute(List<Flight> legs, int duration, int layover) {}
}
