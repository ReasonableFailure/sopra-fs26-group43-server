package ch.uzh.ifi.hase.soprafs26.rest.newsdto;

public class NewsPostDTO {

    private String title;
    private String body;
    private String postURI;
    private Long scenarioId;
    private Long authorId;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public String getPostURI() { return postURI; }
    public void setPostURI(String postURI) { this.postURI = postURI; }

    public Long getScenarioId() { return scenarioId; }
    public void setScenarioId(Long scenarioId) { this.scenarioId = scenarioId; }

    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }
}