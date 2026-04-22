package ch.uzh.ifi.hase.soprafs26.rest.playerdto;

public class RolePostDTO {
    private String name;
    private String title;
    private String description;
    private String secret;
    private Long scenarioId;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSecret() { return secret; }
    public void setSecret(String secret) { this.secret = secret; }

    public Long getScenarioId() { return scenarioId; }
    public void setScenarioId(Long scenarioId) { this.scenarioId = scenarioId; }
}
