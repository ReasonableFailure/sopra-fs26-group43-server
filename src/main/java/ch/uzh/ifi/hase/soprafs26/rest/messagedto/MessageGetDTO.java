package ch.uzh.ifi.hase.soprafs26.rest.messagedto;

import ch.uzh.ifi.hase.soprafs26.constant.CommsStatus;
import java.time.Instant;

public class MessageGetDTO {

    private Long id;
    private String title;
    private String body;
    private Instant createdAt;
    private CommsStatus status;
    private Long creatorId;
    private Long recipientId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public CommsStatus getStatus() {
        return status;
    }

    public void setStatus(CommsStatus status) {
        this.status = status;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public Long getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(Long recipientId) {
        this.recipientId = recipientId;
    }
}