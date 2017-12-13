package com.chatfuel.lift;

import com.chatfuel.lift.controller.LiftController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Application entry point.
 */
@SpringBootApplication
public class LiftApp implements CommandLineRunner {
    
    @Autowired
    private InputConsumer inputCollector;
    @Autowired
    private LiftController liftController; 
    
    public static void main(String[] args) {
        SpringApplication.run(LiftApp.class, args);
    }

    @Override
    public void run(String... args) {
        liftController.start();
        try {
            inputCollector.consumeInput(System.in);
        } finally {
            liftController.stop();
        }
    }
}
