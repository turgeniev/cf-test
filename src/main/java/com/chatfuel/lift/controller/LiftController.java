package com.chatfuel.lift.controller;

import com.chatfuel.lift.model.Lift;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static com.chatfuel.lift.controller.LiftState.IDLE;
import static com.chatfuel.lift.controller.LiftState.MOVE_DOWN;
import static com.chatfuel.lift.controller.LiftState.MOVE_UP;
import static com.chatfuel.lift.controller.LiftState.SELECT_DIRECTION;

/**
 * Lift controller logic.
 *
 * Lift respects "people first" rule.
 * 
 * First lift executes requests (buttons pressed) from the inside of the cabin:
 * - first pressed button determines the direction
 * - when moving up or down lift stops at the floor if floor number is pressed inside or outside the cabin
 * - lift doesn't change direction util all internal requests in selected direction are handled
 * 
 * Lift starts processing external requests only when all internal requests are handled.
 * 
 * If button is pressed inside the cabin while lift is moving for outside request
 * then lift processes internal request first (outside request is postponed).
 */
public class LiftController implements LiftControls, Runnable {

    private final Lift lift;
    private final BlockingQueue<ButtonPressed> requests = new LinkedBlockingQueue<>(); // unbounded blocking queue
    private final Thread controllerThread;
    private final int floorsCount;
    private final int doorOpenCloseTimeMillis;
    private final int floorTravelTimeMillis;

    private Map<Integer, ButtonPressed> insidePressed = new HashMap<>();
    private Map<Integer, ButtonPressed> outsidePressed = new HashMap<>();
    private LiftState state = IDLE;
    private int currentFloor;

    private LiftListener liftListener = LiftListener.NOOP;

    public LiftController(Lift lift, ControllerProps props) {
        System.out.println(props);
        this.currentFloor = props.getCurrentFloor();
        this.lift = lift;
        this.floorsCount = props.getFloorsCount();
        this.doorOpenCloseTimeMillis = props.getDoorOpenCloseTimeMillis();
        this.floorTravelTimeMillis = props.getFloorTravelTimeMillis();

        controllerThread = new Thread(this, "Lift-controller-thread");
        controllerThread.setDaemon(false);
    }

    public void setLiftListener(LiftListener liftListener) {
        this.liftListener = (liftListener == null ? LiftListener.NOOP : liftListener);
    }

    // ========================================================

    @Override
    public void insideButtonPressed(int floorNumber) throws IllegalArgumentException {
        assertValid(floorNumber);
        requests.add(ButtonPressed.inside(floorNumber));
    }

    @Override
    public void outsideButtonPressed(int floorNumber) throws IllegalArgumentException {
        assertValid(floorNumber);
        requests.add(ButtonPressed.outside(floorNumber));
    }

    @Override
    public int getFloorsCount() {
        return floorsCount;
    }

    private void assertValid(int floorNumber) {
        if (floorNumber < 1 || floorNumber > floorsCount) {
            throw new IllegalArgumentException(
                    "Floor number must be between 1 and " + floorsCount + ", but was " + floorNumber
            );
        }
    }

    // ========================================================

    public void start() {
        controllerThread.start();
    }

    public void stop() {
        controllerThread.interrupt();
    }

    // ========================================================

    @Override
    public void run() {
        try {
            // provide initial floor number
            liftListener.atFloor(currentFloor);
            // working loop
            while (!Thread.currentThread().isInterrupted()) {

                switch (state) {
                    case IDLE:
                        state = waitForRequests();
                        break;
                    case SELECT_DIRECTION:
                        state = selectDirection();
                        break;
                    case MOVE_DOWN:
                        state = moveDown();
                        break;
                    case MOVE_UP:
                        state = moveUp();
                        break;
                }
            }
        } catch (InterruptedException e) {
            // finalize execution and exit
        }
    }

    private LiftState waitForRequests() throws InterruptedException {
        collect(requests.take());
        collectRequests();
        return SELECT_DIRECTION;
    }

    private void collect(ButtonPressed buttonPressed) {
        (buttonPressed.inside ? insidePressed : outsidePressed)
                .putIfAbsent(buttonPressed.floorNumber, buttonPressed);
    }

    private void collectRequests() {
        List<ButtonPressed> drainedTo = new ArrayList<>();
        requests.drainTo(drainedTo);
        drainedTo.forEach(this::collect);
    }

    private LiftState selectDirection() {
        if (!insidePressed.isEmpty()) { // handle inside  first, to let people out of a cabin
            return selectDirection(insidePressed);
        } else if (!outsidePressed.isEmpty()) {
            return selectDirection(outsidePressed);
        } else {
            return IDLE;
        }
    }

    private LiftState selectDirection(Map<Integer, ButtonPressed> floorsPressed) {
        ButtonPressed floorToGo = floorsPressed.values()
                .stream()
                .min(Comparator.comparing(ButtonPressed::getTime))
                .get(); // selectDirection() is called only if floorsPressed is not empty

        if (floorToGo.floorNumber == currentFloor) {
            floorsPressed.remove(floorToGo.floorNumber);
            openLift();
            return SELECT_DIRECTION;
        } else {
            return (floorToGo.floorNumber < currentFloor) ? MOVE_DOWN : MOVE_UP;
        }
    }

    private LiftState moveDown() {
        lift.down();
        while (currentFloor > minFloor()) {
            waitFor(floorTravelTimeMillis);
            currentFloor--;
            liftListener.atFloor(currentFloor);
            if (insidePressed.containsKey(currentFloor) || outsidePressed.containsKey(currentFloor)) {
                lift.stop();
                insidePressed.remove(currentFloor);
                outsidePressed.remove(currentFloor);
                openLift();
                lift.down();
            }
            // collecting requests only after lift moved one floor (to avoid hanging) 
            collectRequests();
        }
        return insidePressed.isEmpty() && outsidePressed.isEmpty() ? IDLE : SELECT_DIRECTION;
    }

    private int minFloor() {
        return Math.min(
                minFloor(insidePressed.keySet()),
                minFloor(outsidePressed.keySet())
        );
    }

    private int minFloor(Set<Integer> set) {
        return set.stream()
                .min(Integer::compareTo)
                .orElse(floorsCount);
    }

    private LiftState moveUp() {
        lift.up();
        while (currentFloor < maxFloor()) {
            waitFor(floorTravelTimeMillis);
            currentFloor++;
            liftListener.atFloor(currentFloor);
            if (insidePressed.containsKey(currentFloor) || outsidePressed.containsKey(currentFloor)) {
                lift.stop();
                insidePressed.remove(currentFloor);
                outsidePressed.remove(currentFloor);
                openLift();
                lift.up();
            }
            // collecting requests only after lift moved one floor (to avoid hanging) 
            collectRequests();
        }
        return insidePressed.isEmpty() && outsidePressed.isEmpty() ? IDLE : SELECT_DIRECTION;
    }

    private int maxFloor() {
        return Math.max(
                maxFloor(insidePressed.keySet()),
                maxFloor(outsidePressed.keySet())
        );
    }

    private static int maxFloor(Set<Integer> set) {
        return set.stream()
                .max(Integer::compareTo)
                .orElse(1);
    }

    // ========================================================

    private void openLift() {
        liftListener.doorOpening();
        lift.openCloseDoor();
        waitFor(doorOpenCloseTimeMillis);
        liftListener.doorClosed();
    }

    private static void waitFor(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    // ========================================================

    static class ButtonPressed {
        private final int floorNumber;
        private final long time;
        private final boolean inside;

        static ButtonPressed inside(int floorNumber) {
            return new ButtonPressed(floorNumber, System.currentTimeMillis(), true);
        }

        static ButtonPressed outside(int floorNumber) {
            return new ButtonPressed(floorNumber, System.currentTimeMillis(), false);
        }

        private ButtonPressed(int floorNumber, long time, boolean inside) {
            this.floorNumber = floorNumber;
            this.time = time;
            this.inside = inside;
        }

        long getTime() {
            return time;
        }

    }
}
