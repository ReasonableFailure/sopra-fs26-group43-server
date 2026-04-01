package ch.uzh.ifi.hase.soprafs26.entity;
import jakarta.persistence.*;

@Inheritance(strategy = InheritanceType.JOINED)
@Entity
abstract public class Player {
    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
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

}
