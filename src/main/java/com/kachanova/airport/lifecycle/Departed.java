package com.kachanova.airport.lifecycle;

import com.kachanova.airport.coordination.Airport;
import com.kachanova.airport.entities.Aircraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// Final phase: the aircraft leaves the airport.
// Returns itself, so the worker loop can stop when it sees DEPARTED.
public final class Departed implements AircraftPhase {

    private static final Logger log = LogManager.getLogger(Departed.class);

    private static final Departed INSTANCE = new Departed();

    private Departed() { }

    public static Departed instance() {
        return INSTANCE;
    }

    @Override
    public String name() {
        return "DEPARTED";
    }

    @Override
    public AircraftPhase proceed(Aircraft aircraft, Airport airport) {
        log.info("Aircraft {} departed from the airport", aircraft.id());
        return this;
    }
}
