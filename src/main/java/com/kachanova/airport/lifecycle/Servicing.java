package com.kachanova.airport.lifecycle;

import com.kachanova.airport.coordination.Airport;
import com.kachanova.airport.entities.Aircraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.TimeUnit;

// Aircraft is serviced at a gate: passengers unboard, then board.
// The gate is released after servicing is complete.
public final class Servicing implements AircraftPhase {

    private static final Logger log = LogManager.getLogger(Servicing.class);

    private static final Servicing INSTANCE = new Servicing();

    private Servicing() { }

    public static Servicing instance() {
        return INSTANCE;
    }

    @Override
    public String name() {
        return "SERVICING";
    }

    @Override
    public AircraftPhase proceed(Aircraft aircraft, Airport airport) throws InterruptedException {
        log.info("Aircraft {} starts servicing", aircraft.id());

        int unboarded = airport.unboardPassengers(aircraft);
        log.info("Aircraft {} unboarded {} passengers", aircraft.id(), unboarded);
        TimeUnit.MILLISECONDS.sleep(300);

        int boarded = airport.boardPassengers(aircraft);
        log.info("Aircraft {} boarded {} passengers", aircraft.id(), boarded);
        TimeUnit.MILLISECONDS.sleep(300);

        String gateId = airport.gateOf(aircraft);
        if (gateId != null) {
            airport.releaseGate(gateId);
        }

        return Departed.instance();
    }
}