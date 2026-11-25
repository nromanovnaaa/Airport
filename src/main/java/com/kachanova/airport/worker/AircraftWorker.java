package com.kachanova.airport.worker;

import com.kachanova.airport.coordination.Airport;
import com.kachanova.airport.entities.Aircraft;
import com.kachanova.airport.lifecycle.AircraftPhase;
import com.kachanova.airport.lifecycle.Approaching;
import com.kachanova.airport.lifecycle.Departed;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Callable;

// A thread-worker that drives a single aircraft through its lifecycle phases.
// Implements Callable so it can be submitted to an ExecutorService and return a result.
public class AircraftWorker implements Callable<Void> {

    private static final Logger log = LogManager.getLogger(AircraftWorker.class);

    private final Aircraft aircraft;
    private final Airport airport;

    public AircraftWorker(Aircraft aircraft, Airport airport) {
        this.aircraft = aircraft;
        this.airport = airport;
    }

    @Override
    public Void call() throws Exception {
        log.info("Aircraft {} entered the airspace", aircraft.id());

        // Set the initial state
        AircraftPhase currentPhase = Approaching.instance();

        // Run the state machine until the aircraft departs
        while (!(currentPhase instanceof Departed)) {
            currentPhase = currentPhase.proceed(aircraft, airport);
        }

        log.info("Aircraft {} successfully completed its flight cycle", aircraft.id());
        return null; // Callable<Void> must return null
    }
}
// Threads are created via Callable. It will return null, since the execution process
// itself is important to us, not the result.
//The State loop — we start with Approaching.instance(), then we call proceed()
// in the loop. The cycle turns until the current phase becomes Departed.
// This is a classic implementation of the State pattern without cross-references
// (we don't do aircraft.setState(new Approaching()), but simply return
// the next phase object from the method