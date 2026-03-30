package ch.uzh.ifi.hase.soprafs26.entity;

import jakarta.persistence.*;

@Entity
public class Pronouncement extends NewsStory {

    @ManyToOne
    @JoinColumn(nullable = false)
    private Character author;

    @Column(nullable = false)
    private int likes;

    public Character getAuthor() {
        return author;
    }

    public void setAuthor(Character author) {
        this.author = author;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public String formatSelf() {
        return getTitle() + ": " + getBody() + "\n-" + author.getName();
    }

    public int calculateActionPoints(int exchangeRate) {
        if (exchangeRate <= 0) {
            throw new IllegalArgumentException("exchangeRate must exceed 0");
        }
        return likes / exchangeRate;
    }
}