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
    private Role creator;

    @Column(nullable = true)
    private String response;

    public CommsStatus getStatus() {
        return status;
    }

    public void setStatus(CommsStatus status) {
        this.status = status;
    }

    public Role getCreator() {
        return creator;
    }

    public void setCreator(Role creator) {
        this.creator = creator;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}