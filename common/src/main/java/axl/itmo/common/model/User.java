package axl.itmo.common.model;

import java.io.Serial;
import java.io.Serializable;

/**
 * Represents a user with login and password hash.
 */
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 123456789L;

    private long id;
    private String login;
    private String passwordHash;

    /**
     * Default constructor.
     */
    public User() {}

    /**
     * Constructor with all fields.
     * @param id user ID
     * @param login user login
     * @param passwordHash hashed password
     */
    public User(long id, String login, String passwordHash) {
        this.id = id;
        this.login = login;
        this.passwordHash = passwordHash;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}