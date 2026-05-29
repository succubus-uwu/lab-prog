package axl.itmo.server.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.concurrent.CopyOnWriteArrayList;

import axl.itmo.common.model.Person;
import axl.itmo.common.model.Coordinates;
import axl.itmo.common.model.Location;
import axl.itmo.common.model.Color;

/**
 * Handles database connections to PostgreSQL.
 */
public class DatabaseHandler {
    private static final String HOST = System.getenv().getOrDefault("DB_HOST", "localhost");
    private static final String DATABASE = System.getenv().getOrDefault("DB_NAME", "studs");
    private static final String URL = "jdbc:postgresql://" + HOST + ":5432/" + DATABASE;
    private static final String USER = System.getenv().getOrDefault("DB_USER", "postgres");
    private static final String PASSWORD = System.getenv().getOrDefault("DB_PASSWORD", "");

    private Connection connection;

    /**
     * Establishes a connection to the database.
     * @throws SQLException if connection fails
     */
    public void connect() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
        }
    }

    /**
     * Returns the current connection.
     * @return Connection object
     * @throws SQLException if connection is not established
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connect();
        }
        return connection;
    }

    /**
     * Closes the connection.
     */
    public void close() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }

    /**
     * Loads all persons from the database.
     * @return LinkedList of Person objects
     * @throws SQLException if database error
     */
    public LinkedList<Person> loadPersons() throws SQLException {
        LinkedList<Person> persons = new LinkedList<>();
        String sql = "SELECT p.id, p.name, p.creation_date, p.height, p.birthday, p.passport_id, p.eye_color, p.owner_id, " +
                "c.x as coord_x, c.y as coord_y, " +
                "l.name as loc_name, l.x as loc_x, l.y as loc_y, l.z as loc_z " +
                "FROM persons p " +
                "LEFT JOIN coordinates c ON p.coordinates_id = c.id " +
                "LEFT JOIN locations l ON p.location_id = l.id " +
                "ORDER BY p.id";

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Coordinates coordinates = null;
                if (rs.getObject("coord_x") != null) {
                    coordinates = new Coordinates(rs.getDouble("coord_x"), rs.getLong("coord_y"));
                }

                Location location = null;
                if (rs.getObject("loc_name") != null || rs.getObject("loc_x") != null) {
                    location = new Location(rs.getString("loc_name"), rs.getDouble("loc_x"), rs.getLong("loc_y"), rs.getDouble("loc_z"));
                }

                Color eyeColor = null;
                String colorStr = rs.getString("eye_color");
                if (colorStr != null) {
                    eyeColor = Color.valueOf(colorStr);
                }

                Person person = new Person(
                    rs.getInt("id"),
                    rs.getString("name"),
                    coordinates,
                    rs.getTimestamp("creation_date").toLocalDateTime(),
                    rs.getFloat("height"),
                    rs.getDate("birthday"),
                    rs.getString("passport_id"),
                    eyeColor,
                    location
                );

                person.setOwnerId(rs.getLong("owner_id"));

                persons.add(person);
            }
        }
        return persons;
    }

    /**
     * Saves a person to the database.
     * @param person the person to save
     * @param ownerId the owner user ID
     * @throws SQLException if database error
     */
    public void savePerson(Person person, long ownerId) throws SQLException {
        Connection conn = getConnection();
        conn.setAutoCommit(false);
        try {
            Long coordId = null;
            if (person.getCoordinates() != null) {
                coordId = insertCoordinates(person.getCoordinates(), conn);
            }

            Long locId = null;
            if (person.getLocation() != null) {
                locId = insertLocation(person.getLocation(), conn);
            }

            String sql = "INSERT INTO persons (name, coordinates_id, creation_date, height, birthday, passport_id, eye_color, location_id, owner_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, person.getName());
                stmt.setObject(2, coordId);
                stmt.setTimestamp(3, java.sql.Timestamp.valueOf(person.getCreationDate()));
                stmt.setFloat(4, person.getHeight());
                stmt.setDate(5, person.getBirthday() != null ? new java.sql.Date(person.getBirthday().getTime()) : null);
                stmt.setString(6, person.getPassportID());
                stmt.setString(7, person.getEyeColor() != null ? person.getEyeColor().name() : null);
                stmt.setObject(8, locId);
                stmt.setLong(9, ownerId);

                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    int generatedId = rs.getInt(1);
                    person.setId(generatedId);
                }
            }

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    private Long insertCoordinates(Coordinates coordinates, Connection conn) throws SQLException {
        String sql = "INSERT INTO coordinates (x, y) VALUES (?, ?) RETURNING id";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, coordinates.getX());
            stmt.setLong(2, coordinates.getY());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            }
        }
        return null;
    }

    private Long insertLocation(Location location, Connection conn) throws SQLException {
        String sql = "INSERT INTO locations (name, x, y, z) VALUES (?, ?, ?, ?) RETURNING id";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, location.getName());
            stmt.setDouble(2, location.getX());
            stmt.setLong(3, location.getY());
            stmt.setDouble(4, location.getZ());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            }
        }
        return null;
    }

    /**
     * Updates a person in the database.
     * @param person the updated person
     * @param ownerId the owner user ID checking if it belongs to him
     * @throws SQLException if database error
     */
    public void updatePerson(Person person, long ownerId) throws SQLException {
        // For simplicity, assuming coordinates and location are not updated, or handle if needed
        String sql = "UPDATE persons SET name = ?, height = ?, birthday = ?, passport_id = ?, eye_color = ? WHERE id = ? AND owner_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, person.getName());
            stmt.setFloat(2, person.getHeight());
            stmt.setDate(3, person.getBirthday() != null ? new java.sql.Date(person.getBirthday().getTime()) : null);
            stmt.setString(4, person.getPassportID());
            stmt.setString(5, person.getEyeColor() != null ? person.getEyeColor().name() : null);
            stmt.setLong(6, person.getId());
            stmt.setLong(7, ownerId);
            int updated = stmt.executeUpdate();
            if (updated == 0) {
                throw new SQLException("No row updated. Probably permission denied or not found.");
            }
        }
    }

    /**
     * Deletes a person from the database.
     * @param id the person ID
     * @param ownerId the user ID requesting deletion
     * @throws SQLException if database error
     */
    public void deletePerson(long id, long ownerId) throws SQLException {
        String sql = "DELETE FROM persons WHERE id = ? AND owner_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            stmt.setLong(2, ownerId);
            int deleted = stmt.executeUpdate();
            if (deleted == 0) {
                throw new SQLException("No row deleted. Probably permission denied or not found.");
            }
        }
    }

    /**
     * Saves all persons in the collection to the database (for initial load or full save).
     * Note: This is for compatibility, but individual operations should use specific methods.
     * @param persons the list of persons
     * @throws SQLException if database error
     */
    public void savePersons(LinkedList<Person> persons) throws SQLException {
        // For now, do nothing as changes are persisted individually
    }

    /**
     * Deletes all persons from the database belonging to the specified user.
     * @param ownerId the user ID
     * @throws SQLException if database error
     */
    public void deletePersonsByOwner(long ownerId) throws SQLException {
        String sql = "DELETE FROM persons WHERE owner_id = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, ownerId);
            stmt.executeUpdate();
        }
    }
}