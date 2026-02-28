package axl.itmo.utils;

import axl.itmo.model.Person;
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
            System.out.println("\u001B[31mEnvironment variable " + envVariable + " is not set.\u001B[0m");
            return new LinkedList<>();
        }

        File file = new File(fileName);
        if (!file.exists()) {
            System.out.println("\u001B[31mFile not found: " + fileName + "\u001B[0m");
            return new LinkedList<>();
        }

        if (!file.canRead()) {
            System.out.println("\u001B[31mFile is not readable: " + fileName + "\u001B[0m");
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
            System.out.println("\u001B[31mError reading file: " + e.getMessage() + "\u001B[0m");
            return new LinkedList<>();
        } catch (com.google.gson.JsonSyntaxException e) {
            System.out.println("\u001B[31mError parsing JSON: " + e.getMessage() + "\u001B[0m");
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
            System.out.println("\u001B[31mEnvironment variable " + envVariable + " is not set. Cannot save.\u001B[0m");
            return;
        }
        
        File file = new File(fileName);
        if (file.exists() && !file.canWrite()) {
            System.out.println("\u001B[31mFile is not writable: " + fileName + "\u001B[0m");
            return;
        }

        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(fileName))) {
            gson.toJson(collection, writer);
        } catch (IOException e) {
            System.out.println("\u001B[31mError writing to file: " + e.getMessage() + "\u001B[0m");
        }
    }
}
