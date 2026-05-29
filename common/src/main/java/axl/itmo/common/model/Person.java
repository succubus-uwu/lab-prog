package axl.itmo.common.model;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Objects;

/**
 * Represents a person with various attributes.
 * Implements Serializable for network transmission and Comparable for sorting.
 */
public class Person implements Comparable<Person>, Serializable {

    @Serial
    private static final long serialVersionUID = 1345689L;

    /**
     * Unique positive identifier of the person.
     */
    private int id;
    /**
     * Non-empty person name.
     */
    private String name;
    /**
     * Coordinates associated with the person.
     */
    private Coordinates coordinates;
    /**
     * Timestamp of person creation. Assigned at insertion time.
     */
    private LocalDateTime creationDate;
    /**
     * Person's height, must be greater than 0.
     */
    private float height;
    /**
     * Person's birthday date.
     */
    private Date birthday;
    /**
     * Passport identifier; may be null but if present must be unique in collection.
     */
    private String passportID;
    /**
     * Person's eye color.
     */
    private Color eyeColor;
    /**
     * Person's location description.
     */
    private Location location;
    
    /**
     * The ID of the user who owns this person object.
     */
    private long ownerId;

    /**
     * Constructs a person with all fields specified.
     *
     * @param id unique identifier
     * @param name non-empty name
     * @param coordinates coordinates of the person
     * @param creationDate creation timestamp
     * @param height height value (> 0)
     * @param birthday birthday date
     * @param passportID passport identifier (may be null)
     * @param eyeColor eye color (may be null)
     * @param location location data
     */
    public Person(int id, String name, Coordinates coordinates, LocalDateTime creationDate, float height, Date birthday, String passportID, Color eyeColor, Location location) {
        this.id = id;
        this.name = name;
        this.coordinates = coordinates;
        this.creationDate = creationDate;
        this.height = height;
        this.birthday = birthday;
        this.passportID = passportID;
        this.eyeColor = eyeColor;
        this.location = location;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public String getPassportID() {
        return passportID;
    }

    public void setPassportID(String passportID) {
        this.passportID = passportID;
    }

    public Color getEyeColor() {
        return eyeColor;
    }

    public void setEyeColor(Color eyeColor) {
        this.eyeColor = eyeColor;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(long ownerId) {
        this.ownerId = ownerId;
    }

    @Override
    public int compareTo(Person o) {
        return this.name.compareTo(o.name);
    }

    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", coordinates=" + coordinates +
                ", creationDate=" + creationDate +
                ", height=" + height +
                ", birthday=" + birthday +
                ", passportID='" + passportID + '\'' +
                ", eyeColor=" + eyeColor +
                ", location=" + location +
                ", ownerId=" + ownerId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return id == person.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}