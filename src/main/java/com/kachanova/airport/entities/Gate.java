package com.kachanova.airport.entities;

// Immutable gate description. Occupancy is tracked by the Airport coordinator, not here.
public record Gate(String id) {

    public Gate {
        if (id == null || id.strip().isEmpty()) {
            throw new IllegalArgumentException("gate id must not be blank");
        }
    }
}