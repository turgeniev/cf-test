package com.chatfuel.lift;

import com.chatfuel.lift.controller.ControllerProps;
import com.chatfuel.lift.controller.LiftController;
import com.chatfuel.lift.controller.LiftControls;
import com.chatfuel.lift.controller.LoggingLiftListener;
import com.chatfuel.lift.model.BasicLift;
import com.chatfuel.lift.model.Lift;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring application configuration.
 */
@Configuration
@EnableConfigurationProperties(ControllerProps.class)
public class AppConfig {
    
    @Bean
    public Lift lift() {
        return new BasicLift();
    }
    
    @Bean
    public LiftController liftController(Lift lift, ControllerProps props) {
        final LiftController liftController = new LiftController(lift, props);
        liftController.setLiftListener(new LoggingLiftListener());
        return liftController;
    }
    
    @Bean
    public InputConsumer inputCollector(LiftControls liftControls) {
        return new InputConsumer(liftControls);
    }
}
