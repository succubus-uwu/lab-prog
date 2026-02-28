package axl.itmo.utils;

import axl.itmo.model.Color;
import axl.itmo.model.Coordinates;
import axl.itmo.model.Location;
import axl.itmo.model.Person;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Scanner;

/**
 * Handles user input and output for interactive mode and script mode.
 * Provides colored messages and utilities to read and validate Person fields.
 */
public class Console {
    /**
     * Scanner used as the input source (STDIN or file for scripts).
     */
    private final Scanner scanner;
    /**
     * Whether the console operates in script mode (non-interactive prompts, fail-fast on errors).
     */
    private final boolean isScript;

    // ANSI Color codes
    /** Reset color code. */
    public static final String RESET = "\u001B[0m";
    /** Red color code used for errors. */
    public static final String RED = "\u001B[31m";
    /** Green color code used for success messages. */
    public static final String GREEN = "\u001B[32m";
    /** Yellow color code used for hints. */
    public static final String YELLOW = "\u001B[33m";
    /** Blue color code used for prompts. */
    public static final String BLUE = "\u001B[34m";
    /** Cyan color code used for info messages. */
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
     * Reads all fields needed to construct a Person.
     * @param id identifier to assign to the person
     * @return constructed Person or null if input is invalid or exhausted
     */
    public Person readPerson(int id) {
        String name = readName();
        if (name == null) return null;
        Coordinates coordinates = readCoordinates();
        if (coordinates == null) return null;
        float height = readHeight();
        if (height <= 0) return null;
        Date birthday = readBirthday();
        if (birthday == null) return null;
        String passportID = readPassportID();
        Color eyeColor = readEyeColor();
        Location location = readLocation();
        if (location == null) return null;

        return new Person(id, name, coordinates, LocalDateTime.now(), height, birthday, passportID, eyeColor, location);
    }

    /**
     * Reads a non-empty name string.
     * @return name or null on failure
     */
    private String readName() {
        String name;
        while (true) {
            if (!isScript) print(BLUE + "Enter name: " + RESET);
            name = readLine();
            if (name == null) return null;
            name = name.trim();
            if (name.isEmpty()) {
                printError("Name cannot be empty.");
                if (isScript) return null;
            } else {
                return name;
            }
        }
    }

    /**
     * Reads valid coordinates (x <= 862, y <= 812).
     * @return Coordinates or null on failure
     */
    private Coordinates readCoordinates() {
        double x;
        long y;
        while (true) {
            try {
                if (!isScript) print(BLUE + "Enter coordinate X (max 862): " + RESET);
                String line = readLine();
                if (line == null) return null;
                x = Double.parseDouble(line.trim());
                if (x > 862) {
                    printError("X must be <= 862.");
                    if (isScript) return null;
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                printError("Invalid number format.");
                if (isScript) return null;
            }
        }
        while (true) {
            try {
                if (!isScript) print(BLUE + "Enter coordinate Y (max 812): " + RESET);
                String line = readLine();
                if (line == null) return null;
                y = Long.parseLong(line.trim());
                if (y > 812) {
                    printError("Y must be <= 812.");
                    if (isScript) return null;
                    continue;
                }
                break;
            } catch (NumberFormatException e) {
                printError("Invalid number format.");
                if (isScript) return null;
            }
        }
        return new Coordinates(x, y);
    }

    /**
     * Reads a positive height value (> 0).
     * @return height or 0 on failure (script mode)
     */
    private float readHeight() {
        float height;
        while (true) {
            try {
                if (!isScript) print(BLUE + "Enter height (> 0): " + RESET);
                String line = readLine();
                if (line == null) return 0;
                height = Float.parseFloat(line.trim());
                if (height <= 0) {
                    printError("Height must be > 0.");
                    if (isScript) return 0;
                    continue;
                }
                return height;
            } catch (NumberFormatException e) {
                printError("Invalid number format.");
                if (isScript) return 0;
            }
        }
    }

    /**
     * Reads a valid birthday in dd-MM-yyyy format.
     * @return Date or null on failure
     */
    private Date readBirthday() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
        formatter.setLenient(false);
        while (true) {
            if (!isScript) print(BLUE + "Enter birthday (dd-MM-yyyy): " + RESET);
            String line = readLine();
            if (line == null) return null;
            String value = line.trim();

            String[] parts = value.split("-");
            if (parts.length != 3) {
                printError("Invalid date format. Use dd-MM-yyyy.");
                if (isScript) return null;
                continue;
            }
            try {
                int dd = Integer.parseInt(parts[0]);
                int mm = Integer.parseInt(parts[1]);
                int yyyy = Integer.parseInt(parts[2]);
                if (mm < 1 || mm > 12) {
                    printError("Month must be between 01 and 12.");
                    if (isScript) return null;
                    continue;
                }
                if (dd < 1 || dd > 31) {
                    printError("Day must be between 01 and 31.");
                    if (isScript) return null;
                    continue;
                }
                // Окончательная строгая проверка календаря
                return formatter.parse(value);
            } catch (NumberFormatException | ParseException e) {
                printError("Invalid date.");
                if (isScript) return null;
            }
        }
    }

    /**
     * Reads an optional passport ID. Empty input maps to null.
     * @return passport ID string or null
     */
    private String readPassportID() {
        String passportID;
        while (true) {
            if (!isScript) print(BLUE + "Enter passport ID (can be null, empty for null): " + RESET);
            passportID = readLine();
            if (passportID == null) return null;
            passportID = passportID.trim();
            if (passportID.isEmpty()) return null;
            return passportID;
        }
    }

    /**
     * Reads an optional eye color from the Color enum. Empty input maps to null.
     * @return Color value or null
     */
    private Color readEyeColor() {
        while (true) {
            if (!isScript) {
                println(YELLOW + "Available colors: " + Color.nameList() + RESET);
                print(BLUE + "Enter eye color (can be null, empty for null): " + RESET);
            }
            String line = readLine();
            if (line == null) return null;
            line = line.trim().toUpperCase();
            if (line.isEmpty()) return null;
            try {
                return Color.valueOf(line);
            } catch (IllegalArgumentException e) {
                printError("Invalid color.");
                if (isScript) return null;
            }
        }
    }

    /**
     * Reads a valid Location (x, y, z floats and non-empty name with max length 732).
     * @return Location or null on failure
     */
    private Location readLocation() {
        float x, y, z;
        String name;
        while (true) {
            try {
                if (!isScript) print(BLUE + "Enter location X: " + RESET);
                String line = readLine();
                if (line == null) return null;
                x = Float.parseFloat(line.trim());
                break;
            } catch (NumberFormatException e) {
                printError("Invalid number format.");
                if (isScript) return null;
            }
        }
        while (true) {
            try {
                if (!isScript) print(BLUE + "Enter location Y: " + RESET);
                String line = readLine();
                if (line == null) return null;
                y = Float.parseFloat(line.trim());
                break;
            } catch (NumberFormatException e) {
                printError("Invalid number format.");
                if (isScript) return null;
            }
        }
        while (true) {
            try {
                if (!isScript) print(BLUE + "Enter location Z: " + RESET);
                String line = readLine();
                if (line == null) return null;
                z = Float.parseFloat(line.trim());
                break;
            } catch (NumberFormatException e) {
                printError("Invalid number format.");
                if (isScript) return null;
            }
        }
        while (true) {
            if (!isScript) print(BLUE + "Enter location name (max 732 chars): " + RESET);
            name = readLine();
            if (name == null) return null;
            name = name.trim();
            if (name.length() > 732) {
                printError("Name too long.");
                if (isScript) return null;
                continue;
            }
            if (name.isEmpty()) {
                printError("Name cannot be empty.");
                if (isScript) return null;
                continue;
            }
            return new Location(x, y, z, name);
        }
    }
}
