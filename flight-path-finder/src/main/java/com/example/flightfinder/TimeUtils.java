package com.example.flightfinder;

public class TimeUtils {
    public static int hhmmToMinutes(String hhmm) {
        hhmm = String.format("%04d", Integer.parseInt(hhmm));

        int hours = Integer.parseInt(hhmm.substring(0, 2));
        int minutes = Integer.parseInt(hhmm.substring(2));
        return hours * 60 + minutes;
    }

    public static int calculateDuration(int start, int end) {
        if (end < start) {
            return (end + 1440) - start; // across midnight
        }
        return end - start;
    }
}