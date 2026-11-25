package com.kachanova.airport;

import com.kachanova.airport.config.AirportConfig;
import com.kachanova.airport.coordination.Airport;
import com.kachanova.airport.entities.Aircraft;
import com.kachanova.airport.worker.AircraftWorker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

// Application entry point.
// Loads configuration, initializes the Airport singleton,
// and runs aircraft workers in a multithreaded environment.
public class Main {

    private static final Logger log = LogManager.getLogger(Main.class);

    public static void main(String[] args) throws InterruptedException {
        log.info("Starting Airport Simulation");

        // 1. Load configuration from file
        AirportConfig config = AirportConfig.load();

        // 2. Initialize the thread-safe Singleton
        Airport airport = Airport.getInstance(config);

        // 3. Create a list of aircraft (in a real app, this could also be read from config)
        List<Aircraft> aircrafts = List.of(
                new Aircraft("SU-100", 150),
                new Aircraft("S7-200", 180),
                new Aircraft("AFL-300", 120),
                new Aircraft("U6-400", 160)
        );

        // 4. Create a fixed thread pool. The number of threads can be adjusted.
        ExecutorService executor = Executors.newFixedThreadPool(aircrafts.size());

        try {
            // 5. Submit each aircraft as a Callable task
            for (Aircraft aircraft : aircrafts) {
                executor.submit(new AircraftWorker(aircraft, airport));
            }

            // 6. Shut down the executor to reject new tasks
            executor.shutdown();

            // 7. Wait for all threads to finish their work
            log.info("Waiting for all aircraft to complete their cycles...");
            if (executor.awaitTermination(1, TimeUnit.MINUTES)) {
                log.info("All aircraft serviced successfully.");
            } else {
                log.warn("Timeout: some aircraft did not finish in time.");
            }

        } finally {
            // 8. Force shutdown if something went wrong
            if (!executor.isTerminated()) {
                executor.shutdownNow();
            }
        }

        log.info("Simulation finished. Final passengers in terminal: {}", airport.passengersInTerminal());
    }
}