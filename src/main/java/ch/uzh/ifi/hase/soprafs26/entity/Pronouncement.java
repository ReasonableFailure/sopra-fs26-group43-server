package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;

import java.util.Set;
import java.util.HashSet;

@Entity
public class Pronouncement extends NewsStory {

    @ManyToOne
    @JoinColumn(nullable = false)
    private Role author;

    @Column(nullable = false)
    private int likes;

    @ManyToMany
    @JoinTable(
        name = "pronouncement_likes",
        joinColumns = @JoinColumn(name = "pronouncement_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> likedBy = new HashSet<>();

    public Role getAuthor() {
        return author;
    }

    public void setAuthor(Role author) {
        this.author = author;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public Set<Role> getLikedBy() {
        return likedBy;
    }
    
    public void setLikedBy(Set<Role> likedBy) {
        this.likedBy = likedBy;
    }

    @Override
    public String formatSelf() {
        return getTitle() + ": " + getBody() + "\n-" + author.getName();
    }

    public int calculateActionPoints() {
        int exchangeRate = this.getScenario().getExchangeRate();
        if (exchangeRate <= 0) {
            throw new IllegalArgumentException("exchangeRate must exceed 0");
        }
        return likes / exchangeRate;
    }
}