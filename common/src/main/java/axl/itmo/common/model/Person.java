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

    /**
     * Returns the identifier.
     * @return id value
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the identifier.
     * @param id new id value
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the name.
     * @return name string
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     * @param name non-empty name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the coordinates.
     * @return coordinates instance
     */
    public Coordinates getCoordinates() {
        return coordinates;
    }

    /**
     * Sets the coordinates.
     * @param coordinates coordinates instance
     */
    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    /**
     * Returns the creation timestamp.
     * @return LocalDateTime of creation
     */
    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    /**
     * Sets the creation timestamp.
     * @param creationDate LocalDateTime value
     */
    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    /**
     * Returns the height.
     * @return height value
     */
    public float getHeight() {
        return height;
    }

    /**
     * Sets the height.
     * @param height height value (> 0)
     */
    public void setHeight(float height) {
        this.height = height;
    }

    /**
     * Returns the birthday date.
     * @return Date of birth
     */
    public Date getBirthday() {
        return birthday;
    }

    /**
     * Sets the birthday date.
     * @param birthday date value
     */
    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    /**
     * Returns the passport ID.
     * @return passport identifier or null
     */
    public String getPassportID() {
        return passportID;
    }

    /**
     * Sets the passport ID.
     * @param passportID identifier value
     */
    public void setPassportID(String passportID) {
        this.passportID = passportID;
    }

    /**
     * Returns the eye color.
     * @return eye color enum
     */
    public Color getEyeColor() {
        return eyeColor;
    }

    /**
     * Sets the eye color.
     * @param eyeColor color enum value
     */
    public void setEyeColor(Color eyeColor) {
        this.eyeColor = eyeColor;
    }

    /**
     * Returns the location.
     * @return location instance
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Sets the location.
     * @param location location instance
     */
    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    /**
     * Compares persons by name in lexicographical order.
     * @param o other person
     * @return comparison result
     */
    public int compareTo(Person o) {
        return this.name.compareTo(o.name);
    }

    @Override
    /**
     * Returns a string representation of the person with all fields.
     * @return string with field values
     */
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
                '}';
    }

    @Override
    /**
     * Equality is based on the unique id only.
     * @param o other object
     * @return true if ids are equal
     */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Person person = (Person) o;
        return id == person.id;
    }

    @Override
    /**
     * Hash code is computed from the id.
     * @return hash code value
     */
    public int hashCode() {
        return Objects.hash(id);
    }
}

