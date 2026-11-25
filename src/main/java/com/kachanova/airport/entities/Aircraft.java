package com.kachanova.airport.entities;

// Immutable aircraft description. Knows only its own data, no references to other entities.
public record Aircraft(String id, int capacity) {

    public Aircraft {
        if (id == null || id.strip().isEmpty()) {
            throw new IllegalArgumentException("aircraft id must not be blank");
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("aircraft capacity must be positive");
        }
    }
}