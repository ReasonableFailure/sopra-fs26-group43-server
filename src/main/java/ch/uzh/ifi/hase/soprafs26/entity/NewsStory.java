package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;

@Entity
public class NewsStory extends Communication {

    @Column(nullable = true)
    private String mastodonStatusId;

    public String getMastodonStatusId() {
        return mastodonStatusId;
    }

    public void setMastodonStatusId(String mastodonStatusId) {
        this.mastodonStatusId = mastodonStatusId;
    }

    public String formatSelf() {
        return getTitle() + ": " + getBody();
    }
}