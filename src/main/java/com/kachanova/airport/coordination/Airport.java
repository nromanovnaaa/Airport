package com.kachanova.airport.coordination;

import com.kachanova.airport.config.AirportConfig;
import com.kachanova.airport.entities.Aircraft;
import com.kachanova.airport.entities.Gate;
import com.kachanova.airport.entities.Terminal;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

// Thread-safe Singleton and the single synchronization point of the app.
// Keeps "aircraft <-> gate" and "passengers in terminal" relations by id only.
// No cross-references between entities: entities know only their own fields,
// all relations live here, in maps keyed by id.
public final class Airport {

    private static final Logger log = LogManager.getLogger(Airport.class);

    private static final ReentrantLock INIT_LOCK = new ReentrantLock();
    private static Airport instance;

    private final Terminal terminal;
    private final List<Gate> gates;

    // gate id -> id of the aircraft occupying it
    private final Map<String, String> gateOccupancy = new HashMap<>();

    // guards gateOccupancy and the gate waiting queue
    private final ReentrantLock gateLock = new ReentrantLock(true);
    private final Condition gateFreed = gateLock.newCondition();

    // guards the terminal passenger counter and capacity check
    private final ReentrantLock terminalLock = new ReentrantLock(true);

    // limits the number of passengers present in the terminal at once
    private final Semaphore terminalCapacity;

    // current number of passengers in the terminal
    private final AtomicInteger passengersInTerminal = new AtomicInteger(0);

    private Airport(AirportConfig config) {
        this.terminal = new Terminal(config.terminalId(), config.terminalCapacity());
        this.gates = config.gates().stream().map(Gate::new).toList();
        this.terminalCapacity = new Semaphore(config.terminalCapacity(), true);
        log.info("Airport initialized: terminal={} capacity={} gates={}",
                terminal.id(), terminal.capacity(), gates.size());
    }

    public static Airport getInstance(AirportConfig config) {
        if (instance == null) {
            INIT_LOCK.lock();
            try {
                if (instance == null) {
                    instance = new Airport(config);
                }
            } finally {
                INIT_LOCK.unlock();
            }
        }
        return instance;
    }
    // Gates

    // Occupies any free gate for the given aircraft.
    // Blocks until a free gate appears.
    // Returns the id of the occupied gate.
    public String acquireGate(Aircraft aircraft) throws InterruptedException {
        gateLock.lock();
        try {
            while (true) {
                for (Gate gate : gates) {
                    if (!gateOccupancy.containsKey(gate.id())) {
                        gateOccupancy.put(gate.id(), aircraft.id());
                        log.info("Aircraft {} acquired gate {}", aircraft.id(), gate.id());
                        return gate.id();
                    }
                }
                log.info("Aircraft {} waits for a free gate", aircraft.id());
                gateFreed.await();
            }
        } finally {
            gateLock.unlock();
        }
    }

    // Releases the gate and wakes waiting aircraft.
    public void releaseGate(String gateId) {
        gateLock.lock();
        try {
            String aircraftId = gateOccupancy.remove(gateId);
            if (aircraftId != null) {
                log.info("Aircraft {} released gate {}", aircraftId, gateId);
                gateFreed.signalAll();
            }
        } finally {
            gateLock.unlock();
        }
    }

    // Finds the gate id occupied by the given aircraft.
    // Returns null if the aircraft does not occupy any gate.
    public String gateOf(Aircraft aircraft) {
        gateLock.lock();
        try {
            for (Map.Entry<String, String> entry : gateOccupancy.entrySet()) {
                if (entry.getValue().equals(aircraft.id())) {
                    return entry.getKey();
                }
            }
            return null;
        } finally {
            gateLock.unlock();
        }
    }
    // Terminal and passengers
    // Moves passengers from the aircraft into the terminal (unboarding).
    // Never exceeds either the aircraft or the terminal capacity.
    // Returns the actual number of unboarded passengers.
    public int unboardPassengers(Aircraft aircraft) throws InterruptedException {
        terminalLock.lock();
        try {
            int free = terminal.capacity() - passengersInTerminal.get();
            int toUnboard = Math.min(aircraft.capacity(), free);
            if (toUnboard <= 0) {
                log.info("Aircraft {}: terminal is full, no unboarding", aircraft.id());
                return 0;
            }
            terminalCapacity.acquire(toUnboard);
            passengersInTerminal.addAndGet(toUnboard);
            log.info("Aircraft {} unboarded {} passengers, terminal now {}",
                    aircraft.id(), toUnboard, passengersInTerminal.get());
            return toUnboard;
        } finally {
            terminalLock.unlock();
        }
    }

    // Boards passengers from the terminal into the aircraft.
    // Never exceeds the aircraft capacity.
    // Returns the actual number of boarded passengers.
    public int boardPassengers(Aircraft aircraft) {
        terminalLock.lock();
        try {
            int available = passengersInTerminal.get();
            int toBoard = Math.min(aircraft.capacity(), available);
            if (toBoard <= 0) {
                log.info("Aircraft {}: no passengers in terminal to board", aircraft.id());
                return 0;
            }
            passengersInTerminal.addAndGet(-toBoard);
            terminalCapacity.release(toBoard);
            log.info("Aircraft {} boarded {} passengers, terminal now {}",
                    aircraft.id(), toBoard, passengersInTerminal.get());
            return toBoard;
        } finally {
            terminalLock.unlock();
        }
    }

    public int passengersInTerminal() {
        return passengersInTerminal.get();
    }
}