package com.example.flightfinder;

public class ChatResponse {
    public FlightQueryContext context;
    public String reply;

    public ChatResponse(FlightQueryContext context, String reply) {
        this.context = context;
        this.reply = reply;
    }
}
