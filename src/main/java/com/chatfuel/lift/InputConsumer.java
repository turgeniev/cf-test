package com.chatfuel.lift;

import com.chatfuel.lift.controller.LiftControls;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.function.Function;

/**
 * Consumes input from a user.
 */
public class InputConsumer {

    private final List<Function<String, Boolean>> consumers = new ArrayList<>();
    private final int floorsCount;

    public InputConsumer(LiftControls liftControls) {
        this.floorsCount = liftControls.getFloorsCount();

        // outside button consumer
        consumers.add((String input) -> {
            if (input.matches("\\d+")) {
                try {
                    liftControls.outsideButtonPressed(
                            Integer.parseInt(input)
                    );
                } catch (IllegalArgumentException e) {
                    System.out.println(e.getMessage());
                }
                return true;
            }
            return false;
        });

        // inside button consumer
        consumers.add((String input) -> {
            if (input.matches("\\[\\d+]")) {
                try {
                    liftControls.insideButtonPressed(
                            Integer.parseInt(
                                    input.substring(1, input.length() - 1)
                            )
                    );
                } catch (IllegalArgumentException e) {
                    System.out.println(e.getMessage());
                }
                return true;
            }
            return false;
        });

        // must be the last consumer, since always return true
        // unrecognized input, try again
        consumers.add((String input) -> {
            showTryAgainMessage(input);
            return true;
        });
    }

    public void consumeInput(InputStream source) {

        showWelcomeMessage();

        final Scanner scanner = new Scanner(source);

        while (true) {
            //            System.out.print("> "); // command prompt

            final String input = scanner.next();

            if (isExit(input)) {
                return; // QUIT !
            }

            consume(input);
        }
    }

    private void consume(String input) {
        for (Function<String, Boolean> consumer : consumers) {
            if (consumer.apply(input)) {
                return;
            }
        }
    }

    private static boolean isExit(String input) {
        return input.matches("(?i)exit|quit");
    }

    private void showWelcomeMessage() {
        System.out.println("Choose floor number from 1 to " + floorsCount + " to go to.");
        System.out.println("Type");
        System.out.println(" - floor number for buttons outside the lift, i.e. 1 or 2 or 3 ... ");
        System.out.println(" or");
        System.out.println(" - floor number in square brackets for buttons inside the lift, i.e. [1] or [2] or [3] ...");
        System.out.println(" or");
        System.out.println(" - quit or exit to end the application");
        System.out.println("and hit Enter.");
    }

    private static void showTryAgainMessage(String input) {
        System.out.println("Input '" + input + "' is not recognized as valid floor number, please try again...");
    }
}
