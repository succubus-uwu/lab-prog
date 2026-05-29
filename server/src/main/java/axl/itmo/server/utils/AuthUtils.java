package axl.itmo.server.utils;

import axl.itmo.common.model.User;
import axl.itmo.server.persistence.DatabaseHandler;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Utility class for user authentication and registration.
 */
public class AuthUtils {
    private final DatabaseHandler dbHandler;

    public AuthUtils(DatabaseHandler dbHandler) {
        this.dbHandler = dbHandler;
    }

    /**
     * Hashes a password using SHA-512.
     * @param password plain password
     * @return hashed password
     */
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-512 not available", e);
        }
    }

    /**
     * Registers a new user.
     * @param login user login
     * @param password plain password
     * @return true if registered successfully, false if login exists
     * @throws SQLException if database error
     */
    public boolean registerUser(String login, String password) throws SQLException {
        String hashed = hashPassword(password);
        String sql = "INSERT INTO users (login, password_hash) VALUES (?, ?)";

        try (Connection conn = dbHandler.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, login);
            stmt.setString(2, hashed);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            if (e.getSQLState().equals("23505")) {
                return false;
            }
            throw e;
        }
    }

    /**
     * Authenticates a user.
     * @param login user login
     * @param password plain password
     * @return User object if authenticated, null otherwise
     * @throws SQLException if database error
     */
    public User authenticateUser(String login, String password) throws SQLException {
        String hashed = hashPassword(password);
        String sql = "SELECT id, login, password_hash FROM users WHERE login = ? AND password_hash = ?";

        try (Connection conn = dbHandler.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, login);
            stmt.setString(2, hashed);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(rs.getLong("id"), rs.getString("login"), rs.getString("password_hash"));
            }
            return null;
        }
    }

    /**
     * Gets user by login.
     * @param login user login
     * @return User object or null
     * @throws SQLException if database error
     */
    public User getUserByLogin(String login) throws SQLException {
        String sql = "SELECT id, login, password_hash FROM users WHERE login = ?";

        try (Connection conn = dbHandler.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, login);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(rs.getLong("id"), rs.getString("login"), rs.getString("password_hash"));
            }
            return null;
        }
    }
}