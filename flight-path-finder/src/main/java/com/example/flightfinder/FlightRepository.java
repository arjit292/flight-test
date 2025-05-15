package com.example.flightfinder;

import com.example.flightfinder.Flight;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface FlightRepository extends JpaRepository<Flight, Long> {
    List<Flight> findByFromAirport(String from);
    List<Flight> findByToAirport(String to);
    List<Flight> findByFromAirportAndToAirport(String from, String to);
}