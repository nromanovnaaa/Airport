package com.kachanova.airport.lifecycle;

import com.kachanova.airport.coordination.Airport;
import com.kachanova.airport.entities.Aircraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.TimeUnit;

// Aircraft arrives at the airport and waits until a gate is free.
public final class Approaching implements AircraftPhase {

    private static final Logger log = LogManager.getLogger(Approaching.class);

    private static final Approaching INSTANCE = new Approaching();

    private Approaching() { }

    public static Approaching instance() {
        return INSTANCE;
    }

    @Override
    public String name() {
        return "APPROACHING";
    }

    @Override
    public AircraftPhase proceed(Aircraft aircraft, Airport airport) throws InterruptedException {
        log.info("Aircraft {} is approaching the airport", aircraft.id());
        TimeUnit.MILLISECONDS.sleep(200);

        String gateId = airport.acquireGate(aircraft);
        log.info("Aircraft {} docked at gate {}", aircraft.id(), gateId);
        TimeUnit.MILLISECONDS.sleep(200);

        return Servicing.instance();
    }
}