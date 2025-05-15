package com.example.flightfinder;

import com.fasterxml.jackson.databind.ObjectMapper;

public class FlightQueryContext {
    public String from;
    public String to;
    public String departureDate;
    public String returnDate;
    public String tripType;

    public String toJson() {
        return String.format("{\"from\":\"%s\",\"to\":\"%s\",\"departureDate\":\"%s\",\"returnDate\":\"%s\",\"tripType\":\"%s\"}",
                nullSafe(from), nullSafe(to), nullSafe(departureDate), nullSafe(returnDate), nullSafe(tripType));
    }

    public static FlightQueryContext fromJson(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(json, FlightQueryContext.class);
        } catch (Exception e) {
            e.printStackTrace();
            return new FlightQueryContext();
        }
    }

    private static String nullSafe(String value) {
        return value == null ? "" : value;
    }
}