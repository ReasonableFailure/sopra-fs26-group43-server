package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;

import ch.uzh.ifi.hase.soprafs26.constant.UserStatus;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * Internal User Representation
 * This class composes the internal representation of the user and defines how
 * the user is stored in the database.
 * Every variable will be mapped into a database field with the @Column
 * annotation
 * - nullable = false -> this cannot be left empty
 * - unique = true -> this value must be unique across the database -> composes
 * the primary key
 */
@Entity
@Table(name = "users")
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable=false, unique=true)
    private Long id;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = true, unique = true)
    private String token;

    @Column(nullable = false)
    private UserStatus status;

    @Column(nullable = false)
    private Date creationDate;

    @Column(nullable = true)
    private String bio;

    @Column(nullable=false)
    private boolean isPlaying;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UserStatus getStatus() {
        return status;
    }

    public void setStatus(UserStatus status) {
        this.status = status;
    }

    public Date getCreationDate(){return this.creationDate;}

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getBio(){
        return this.bio;
    }

    public void setBio(String bioText){
        this.bio = bioText;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void setPlaying(boolean playing) {
        isPlaying = playing;
    }
}
