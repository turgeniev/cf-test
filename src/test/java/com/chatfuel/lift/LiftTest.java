package com.chatfuel.lift;

import com.chatfuel.lift.controller.ControllerProps;
import com.chatfuel.lift.controller.LiftController;
import com.chatfuel.lift.controller.LiftListener;
import com.chatfuel.lift.controller.LoggingLiftListener;
import com.chatfuel.lift.model.BasicLift;
import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

/**
 * Lift behavior test.
 */
public class LiftTest {

    private LiftController liftController;
    private LiftListener liftListener;

    private void setup(ControllerProps props) {
        liftController = new LiftController(new BasicLift(), props);
        liftListener = Mockito.spy(new LoggingLiftListener());
        liftController.setLiftListener(liftListener);
        liftController.start();
    }

    @After
    public void tearDown() {
        liftController.stop();
    }

    @Test
    public void openDoorOn_1st_Floor() {
        setup(ControllerPropsBuilder.defaultProps());

        liftController.outsideButtonPressed(1);

        verify(liftListener, timeout(1000)).atFloor(1);
        assertDoorOpenCloseTime(2, TimeUnit.SECONDS);
    }

    @Test
    public void from_1st_To_5th_Floor() {
        setup(ControllerPropsBuilder.defaultProps());

        liftController.insideButtonPressed(5);

        verify(liftListener, timeout(1000)).atFloor(1);
        
        for (int i = 2; i <= 5; i++) {
            Stopwatch timer = Stopwatch.start();
            // lift should arrive to the next floor no later than given timeout expires
            verify(liftListener, timeout(2200)).atFloor(i);
            timer.assertDurationCloseTo(2, TimeUnit.SECONDS);
        }

        assertDoorOpenCloseTime(2, TimeUnit.SECONDS);
    }

    private void assertDoorOpenCloseTime(long duration, TimeUnit timeUnit) {
        long expectedTimeMillis = timeUnit.toMillis(duration);
        
        // door opening is being initiated
        verify(liftListener, timeout(1000)).doorOpening();
        Stopwatch timer = Stopwatch.start();

        // door is closed after doorOpenCloseTime
        verify(liftListener, timeout(expectedTimeMillis + 1000)).doorClosed();
        timer.assertDurationCloseTo(duration, timeUnit);
    }

}
