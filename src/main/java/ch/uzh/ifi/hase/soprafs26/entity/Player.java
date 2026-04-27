package ch.uzh.ifi.hase.soprafs26.entity;
import jakarta.persistence.*;

import java.io.Serial;
import java.io.Serializable;

@Inheritance(strategy = InheritanceType.JOINED)
@Entity
@Table(name="players")
abstract public class Player implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable=false, unique=true)
    private Long id;

    @Column(nullable = false,unique = true)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "userId")
    private User user;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
