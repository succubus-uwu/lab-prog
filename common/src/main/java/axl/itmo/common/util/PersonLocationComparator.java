package axl.itmo.common.util;

import axl.itmo.common.model.Person;

import java.util.Comparator;

/**
 * Comparator for sorting Person objects by location.
 * Sorts by location name, then by location coordinates if names are equal.
 */
public class PersonLocationComparator implements Comparator<Person> {
    /**
     * Compares two Person objects by their location.
     * First compares by location name (null-safe), then by X, Y, Z coordinates if names are equal.
     *
     * @param p1 first person
     * @param p2 second person
     * @return comparison result
     */
    @Override
    public int compare(Person p1, Person p2) {
        if (p1 == null && p2 == null) return 0;
        if (p1 == null) return -1;
        if (p2 == null) return 1;

        if (p1.getLocation() == null && p2.getLocation() == null) return 0;
        if (p1.getLocation() == null) return -1;
        if (p2.getLocation() == null) return 1;

        // Compare by location name
        int nameComparison = p1.getLocation().getName().compareTo(p2.getLocation().getName());
        if (nameComparison != 0) return nameComparison;

        // Compare by X coordinate
        int xComparison = Double.compare(p1.getLocation().getX(), p2.getLocation().getX());
        if (xComparison != 0) return xComparison;

        // Compare by Y coordinate
        int yComparison = Long.compare(p1.getLocation().getY(), p2.getLocation().getY());
        if (yComparison != 0) return yComparison;

        // Compare by Z coordinate
        return Double.compare(p1.getLocation().getZ(), p2.getLocation().getZ());
    }
}

