package com.chatfuel.lift;

import com.chatfuel.lift.controller.ControllerProps;

/**
 */
public class ControllerPropsBuilder {

    private final ControllerProps props;

    private ControllerPropsBuilder() {
        props = new ControllerProps();
        props.setFloorsCount(5);
        props.setFloorHeightMeters(3f);
        props.setLiftSpeedMetersPerSecond(1.5f);
        props.setDoorOpenCloseTimeMillis(2000);

    }
    
    static ControllerProps defaultProps() {
        return instance().props;
    }
    
    static ControllerPropsBuilder instance() {
        return new ControllerPropsBuilder();
    }
    
    ControllerPropsBuilder setCurrentFloor(int currentFloor) {
        props.setCurrentFloor(currentFloor);
        return this;
    }

    ControllerPropsBuilder setFloorsCount(int floorsCount) {
        props.setFloorsCount(floorsCount);
        return this;
    }

    ControllerPropsBuilder setFloorHeightMeters(float floorHeightMeters) {
        props.setFloorHeightMeters(floorHeightMeters);
        return this;
    }

    ControllerPropsBuilder setLiftSpeedMetersPerSecond(float speedMetersPerSecond) {
        props.setLiftSpeedMetersPerSecond(speedMetersPerSecond);
        return this;
    }

    ControllerPropsBuilder setDoorOpenCloseTimeMillis(int doorOpenCloseTimeMillis) {
        props.setDoorOpenCloseTimeMillis(doorOpenCloseTimeMillis);
        return this;
    }

    ControllerProps build() {
        return props;
    }
}
