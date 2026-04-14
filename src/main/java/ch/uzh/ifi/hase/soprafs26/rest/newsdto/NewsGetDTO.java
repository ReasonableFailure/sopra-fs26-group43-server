package ch.uzh.ifi.hase.soprafs26.rest.newsdto;

import java.time.Instant;

public class NewsGetDTO {

    private Long id;
    private String title;
    private String body;
    private Instant createdAt;
    private String postURI;
    private Long authorId;
    private Integer likes;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public String getPostURI() { return postURI; }
    public void setPostURI(String postURI) { this.postURI = postURI; }

    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }

    public Integer getLikes() { return likes; }
    public void setLikes(Integer likes) { this.likes = likes; }
}