package com.kachanova.airport.entities;

// Immutable terminal description. Current passenger count is tracked by the Airport coordinator, not here.
public record Terminal(String id, int capacity) {

    public Terminal {
        if (id == null || id.strip().isEmpty()) {
            throw new IllegalArgumentException("terminal id must not be blank");
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("terminal capacity must be positive");
        }
    }
}