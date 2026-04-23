package axl.itmo.server.utils;

import axl.itmo.common.model.Color;
import axl.itmo.common.model.Coordinates;
import axl.itmo.common.model.Location;
import axl.itmo.common.model.Person;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Scanner;

/**
 * Handles user input and output for server console mode.
 * Provides colored messages and utilities to read and validate Person fields.
 */
public class Console {
    private final Scanner scanner;
    private final boolean isScript;

    // ANSI Color codes
    public static final String RESET = "\u001B[0m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String CYAN = "\u001B[36m";

    /**
     * Creates a new Console.
     *
     * @param scanner input scanner (not null)
     * @param isScript true if running from a script file, false for interactive
     */
    public Console(Scanner scanner, boolean isScript) {
        this.scanner = scanner;
        this.isScript = isScript;
    }

    /**
     * Reads next input line.
     * @return next line, or null if no more input
     */
    public String readLine() {
        if (scanner.hasNextLine()) {
            return scanner.nextLine();
        }
        return null;
    }

    /**
     * Prints a message without a newline.
     * @param message text to print
     */
    public void print(String message) {
        System.out.print(message);
    }

    /**
     * Prints a message with a newline.
     * @param message text to print
     */
    public void println(String message) {
        System.out.println(message);
    }

    /**
     * Prints an error message in red.
     * @param message error text
     */
    public void printError(String message) {
        System.out.println(RED + "Error: " + message + RESET);
    }

    /**
     * Prints a success message in green.
     * @param message success text
     */
    public void printSuccess(String message) {
        System.out.println(GREEN + message + RESET);
    }

    /**
     * Prints an informational message in cyan.
     * @param message info text
     */
    public void printInfo(String message) {
        System.out.println(CYAN + message + RESET);
    }

    /**
     * Prints a prompt in blue.
     * @param prompt prompt text
     */
    public void printPrompt(String prompt) {
        System.out.print(BLUE + prompt + RESET);
    }

    /**
     * Interactively reads a Person object from console input.
     *
     * @param id the person ID
     * @return the person object, or null if cancelled
     */
    public Person readPerson(int id) {
        try {
            printPrompt("Enter person name: ");
            String name = readLine();
            if (name == null || name.trim().isEmpty()) {
                printError("Name cannot be empty.");
                return null;
            }

            printPrompt("Enter X coordinate: ");
            String xStr = readLine();
            if (xStr == null) return null;
            double x = Double.parseDouble(xStr);
            if (x > 862) {
                printError("X coordinate must be <= 862");
                return null;
            }

            printPrompt("Enter Y coordinate: ");
            String yStr = readLine();
            if (yStr == null) return null;
            long y = Long.parseLong(yStr);

            Coordinates coordinates = new Coordinates(x, y);

            printPrompt("Enter height (must be > 0): ");
            String heightStr = readLine();
            if (heightStr == null) return null;
            float height = Float.parseFloat(heightStr);
            if (height <= 0) {
                printError("Height must be greater than 0");
                return null;
            }

            printPrompt("Enter birthday (yyyy-MM-dd): ");
            String birthdayStr = readLine();
            if (birthdayStr == null) return null;
            Date birthday = null;
            if (!birthdayStr.trim().isEmpty()) {
                try {
                    birthday = new SimpleDateFormat("yyyy-MM-dd").parse(birthdayStr);
                } catch (ParseException e) {
                    printError("Invalid date format. Using null.");
                }
            }

            printPrompt("Enter passport ID (or press Enter for none): ");
            String passportID = readLine();
            if (passportID == null) return null;
            if (passportID.trim().isEmpty()) {
                passportID = null;
            }

            printPrompt("Enter eye color (" + Color.nameList() + ") or press Enter for none: ");
            String colorStr = readLine();
            if (colorStr == null) return null;
            Color eyeColor = null;
            if (!colorStr.trim().isEmpty()) {
                try {
                    eyeColor = Color.valueOf(colorStr.toUpperCase());
                } catch (IllegalArgumentException e) {
                    printError("Invalid color. Using null.");
                }
            }

            printPrompt("Enter location name: ");
            String locName = readLine();
            if (locName == null) return null;

            printPrompt("Enter location X coordinate: ");
            String locXStr = readLine();
            if (locXStr == null) return null;
            double locX = Double.parseDouble(locXStr);

            printPrompt("Enter location Y coordinate: ");
            String locYStr = readLine();
            if (locYStr == null) return null;
            long locY = Long.parseLong(locYStr);

            printPrompt("Enter location Z coordinate: ");
            String locZStr = readLine();
            if (locZStr == null) return null;
            double locZ = Double.parseDouble(locZStr);

            Location location = new Location(locName, locX, locY, locZ);

            return new Person(id, name, coordinates, LocalDateTime.now(), height, birthday, passportID, eyeColor, location);
        } catch (NumberFormatException e) {
            printError("Invalid number format: " + e.getMessage());
            return null;
        }
    }

    /**
     * Reads a float value from console with validation.
     */
    public float readFloat(String prompt) {
        while (true) {
            try {
                printPrompt(prompt);
                String line = readLine();
                if (line == null) return -1;
                return Float.parseFloat(line);
            } catch (NumberFormatException e) {
                printError("Please enter a valid number.");
            }
        }
    }

    /**
     * Reads an integer value from console with validation.
     */
    public int readInt(String prompt) {
        while (true) {
            try {
                printPrompt(prompt);
                String line = readLine();
                if (line == null) return -1;
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                printError("Please enter a valid integer.");
            }
        }
    }

    /**
     * Reads a string value from console.
     */
    public String readString(String prompt) {
        printPrompt(prompt);
        return readLine();
    }
}

