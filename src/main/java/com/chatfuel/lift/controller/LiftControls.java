package com.chatfuel.lift.controller;

/**
 * Lift controls exposed to a user.
 */
public interface LiftControls {

    /**
     * Handles of pressing button which is inside the lift.
     * @param floorNumber which button is pressed
     * @throws IllegalArgumentException invalid floor number
     */
    void insideButtonPressed(int floorNumber) throws IllegalArgumentException;

    /**
     * Handles of pressing button which is outside the lift.
     * @param floorNumber which button is pressed
     * @throws IllegalArgumentException invalid floor number
     */
    void outsideButtonPressed(int floorNumber) throws IllegalArgumentException;

    /**
     * Number of floors lift can travel to.
     * @return total number of floors
     */
    int getFloorsCount();
    
}
