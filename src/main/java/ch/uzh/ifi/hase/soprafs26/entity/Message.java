package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;

@Entity
public class Message extends Directive {

    @ManyToOne
    @JoinColumn(nullable = false)
    private Character recipient;

    public Character getRecipient() {
        return recipient;
    }

    public void setRecipient(Character recipient) {
        this.recipient = recipient;
    }
}