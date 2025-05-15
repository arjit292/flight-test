package com.example.flightfinder;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class FlightQueryContexts {
        @JsonProperty("from")
        public String from;
        @JsonProperty("to")
        public String to;
        @JsonProperty("departureDate")
        public String departureDate;
        @JsonProperty("returnDate")
        public String returnDate;
        @JsonProperty("tripType")
        public String tripType;
}
