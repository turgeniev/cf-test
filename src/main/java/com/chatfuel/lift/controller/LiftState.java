package com.chatfuel.lift.controller;

/**
 * Lift lifecycle state.
 */
public enum LiftState {
    /**
     * Lift is waiting when a button is pressed.
     */
    IDLE,
    /**
     * Selecting which direction to move - up or down.
     */
    SELECT_DIRECTION,
    /**
     * Lift is moving up.
     */
    MOVE_UP,
    /**
     * Lift is moving down.
     */
    MOVE_DOWN,
}
