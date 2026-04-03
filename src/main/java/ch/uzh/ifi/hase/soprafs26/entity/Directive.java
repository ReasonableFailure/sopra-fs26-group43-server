package ch.uzh.ifi.hase.soprafs26.entity;

import ch.uzh.ifi.hase.soprafs26.constant.CommsStatus;
import jakarta.persistence.*;

@Entity
public class Directive extends Communication {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommsStatus status;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Character creator;

    public CommsStatus getStatus() {
        return status;
    }

    public void setStatus(CommsStatus status) {
        this.status = status;
    }

    public Character getCreator() {
        return creator;
    }

    public void setCreator(Character creator) {
        this.creator = creator;
    }
}