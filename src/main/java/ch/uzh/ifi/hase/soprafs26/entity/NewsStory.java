package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;

@Entity
public class NewsStory extends Communication {

    @Column(nullable = false)
    private String postURI;

    public String getPostURI() {
        return postURI;
    }

    public void setPostURI(String postURI) {
        this.postURI = postURI;
    }

    public String formatSelf() {
        return getTitle() + ": " + getBody();
    }
}