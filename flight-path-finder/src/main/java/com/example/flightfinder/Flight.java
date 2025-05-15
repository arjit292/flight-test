package com.example.flightfinder;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Flight {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String flightNo;
    private String fromAirport;
    private String toAirport;
    private int startTime;
    private int endTime;
    private int duration;

    public Flight() {}

    public Flight(String flightNo, String fromAirport, String toAirport, int startTime, int endTime, int duration) {
        this.flightNo = flightNo;
        this.fromAirport = fromAirport;
        this.toAirport = toAirport;
        this.startTime = startTime;
        this.endTime = endTime;
        this.duration = duration;
    }

    public String getFlightNo() { return flightNo; }
    public String getFromAirport() { return fromAirport; }
    public String getToAirport() { return toAirport; }
    public int getStartTime() { return startTime; }
    public int getEndTime() { return endTime; }
    public int getDuration() { return duration; }
}