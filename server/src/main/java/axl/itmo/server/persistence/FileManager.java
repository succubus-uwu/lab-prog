package axl.itmo.server.persistence;

import axl.itmo.common.model.Person;
import axl.itmo.common.util.LocalDateTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.util.LinkedList;

/**
 * Handles reading and writing the collection to a file.
 */
public class FileManager {
    private final String envVariable;
    private final Gson gson;

    public FileManager(String envVariable) {
        this.envVariable = envVariable;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .setPrettyPrinting()
                .create();
    }

    /**
     * Reads the collection from the file specified in the environment variable.
     * @return LinkedList of Person objects.
     */
    public LinkedList<Person> readCollection() {
        String fileName = System.getenv(envVariable);
        if (fileName == null) {
            System.out.println("Environment variable " + envVariable + " is not set.");
            return new LinkedList<>();
        }

        File file = new File(fileName);
        if (!file.exists()) {
            System.out.println("File not found: " + fileName);
            return new LinkedList<>();
        }

        if (!file.canRead()) {
            System.out.println("File is not readable: " + fileName);
            return new LinkedList<>();
        }

        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
             InputStreamReader reader = new InputStreamReader(bis)) {

            Type collectionType = new TypeToken<LinkedList<Person>>() {}.getType();
            LinkedList<Person> collection = gson.fromJson(reader, collectionType);

            if (collection == null) {
                return new LinkedList<>();
            }
            return collection;
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
            return new LinkedList<>();
        } catch (com.google.gson.JsonSyntaxException e) {
            System.out.println("Error parsing JSON: " + e.getMessage());
            return new LinkedList<>();
        }
    }

    /**
     * Writes the collection to the file specified in the environment variable.
     * @param collection The collection to write.
     */
    public void writeCollection(LinkedList<Person> collection) {
        String fileName = System.getenv(envVariable);
        if (fileName == null) {
            System.out.println("Environment variable " + envVariable + " is not set. Cannot save.");
            return;
        }

        File file = new File(fileName);
        if (file.exists() && !file.canWrite()) {
            System.out.println("File is not writable: " + fileName);
            return;
        }

        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(fileName))) {
            gson.toJson(collection, writer);
        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
        }
    }
}

