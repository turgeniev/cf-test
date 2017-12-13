package com.chatfuel.lift.controller;

/**
 */
public class LoggingLiftListener implements LiftListener {
    
    @Override
    public void atFloor(int floorNumber) {
        log("Lift at floor: " + floorNumber);
    }

    @Override
    public void doorOpening() {
        log("Door is opening...");
    }

    @Override
    public void doorClosed() {
        log("Door is closed.");
    }
    
    private static void log(String message) {
        System.out.println(message);
    }
}
