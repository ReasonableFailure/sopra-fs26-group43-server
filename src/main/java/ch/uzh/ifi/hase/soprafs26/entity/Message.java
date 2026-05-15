package ch.uzh.ifi.hase.soprafs26.entity;

import ch.uzh.ifi.hase.soprafs26.constant.CommsStatus;
import jakarta.persistence.*;

@Entity
public class Message extends Communication {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CommsStatus status;

    /**
     * Whether the recipient has seen this message in their conversation
     * view. Only meaningful once status == ACCEPTED, since recipients
     * never see PENDING/REJECTED/FAILED messages. Flipped from false to
     * true the first time the recipient hits GET /messages/between/{a}/{b}
     * with their Role token.
     */
    @Column(nullable = false)
    private boolean seenByRecipient;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Role creator;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Role recipient;

    @Override
    public void applyStats(Role role) {
        creator.setNumberMessages(
            creator.getNumberMessages() + 1
        );
    }

    public CommsStatus getStatus() {
        return status;
    }

    public void setStatus(CommsStatus status) {
        this.status = status;
    }

    public boolean isSeenByRecipient() {
        return seenByRecipient;
    }

    public void setSeenByRecipient(boolean seenByRecipient) {
        this.seenByRecipient = seenByRecipient;
    }

    public Role getCreator() {
        return creator;
    }

    public void setCreator(Role creator) {
        this.creator = creator;
    }

    public Role getRecipient() {
        return recipient;
    }

    public void setRecipient(Role recipient) {
        this.recipient = recipient;
    }
}