package com.chatfuel.lift.controller;

/**
 * Lift state change listener.
 */
public interface LiftListener {

    /**
     * Called when lift is at the certain floor.
     * @param floorNumber which floor lift is at 
     */
    void atFloor(int floorNumber);

    /**
     * Called when door starts to open.
     */
    void doorOpening();

    /**
     * Called when door is closed.
     */
    void doorClosed();

    /**
     * Null-object.
     */
    LiftListener NOOP = new LiftListener() {
        @Override
        public void atFloor(int floorNumber) {
        }

        @Override
        public void doorOpening() {
        }

        @Override
        public void doorClosed() {
        }
    };
}
