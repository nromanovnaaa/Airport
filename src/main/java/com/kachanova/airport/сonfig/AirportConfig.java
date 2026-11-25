package com.kachanova.airport.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

// Immutable configuration loaded from airport-config.properties on the classpath.
// Knows only its own data: terminal and gates description, no references to entities or coordinators.
public record AirportConfig(
        String terminalId,
        int terminalCapacity,
        List<String> gates
) {

    private static final String RESOURCE = "airport-config.properties";

    public AirportConfig {
        if (terminalId == null || terminalId.strip().isEmpty()) {
            throw new IllegalArgumentException("terminal id must not be blank");
        }
        if (terminalCapacity <= 0) {
            throw new IllegalArgumentException("terminal capacity must be positive");
        }
        if (gates == null || gates.isEmpty()) {
            throw new IllegalArgumentException("at least one gate must be configured");
        }
        gates = List.copyOf(gates);
    }

    // Loads configuration from the classpath resource.
    // Data in the file is assumed to be correct, but the record still validates it.
    public static AirportConfig load() {
        Properties props = new Properties();
        try (InputStream in = AirportConfig.class.getClassLoader().getResourceAsStream(RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("configuration resource not found: " + RESOURCE);
            }
            props.load(in);
        } catch (IOException e) {
            throw new IllegalStateException("failed to read configuration: " + RESOURCE, e);
        }

        String terminalId = props.getProperty("terminal.id");
        int terminalCapacity = Integer.parseInt(props.getProperty("terminal.capacity").strip());

        List<String> gates = props.stringPropertyNames().stream()
                .filter(key -> key.startsWith("gate."))
                .sorted()
                .map(props::getProperty)
                .map(String::strip)
                .toList();

        return new AirportConfig(terminalId, terminalCapacity, gates);
    }
}