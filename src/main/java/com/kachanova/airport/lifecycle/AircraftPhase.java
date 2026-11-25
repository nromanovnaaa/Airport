package com.kachanova.airport.lifecycle;

import com.kachanova.airport.coordination.Airport;
import com.kachanova.airport.entities.Aircraft;

// State of an aircraft within its service cycle.
// "sealed" tells the compiler: “ONLY the classes that I explicitly listed
// can implement this interface. No one else!.
public sealed interface AircraftPhase permits Approaching, Servicing, Departed {

    // Name of the phase for logging.
    String name();

    // Performs the work of this phase and returns the next phase.
    AircraftPhase proceed(Aircraft aircraft, Airport airport) throws InterruptedException;
}