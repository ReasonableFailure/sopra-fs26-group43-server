package ch.uzh.ifi.hase.soprafs26.rest.playerdto;

import ch.uzh.ifi.hase.soprafs26.constant.ScenarioStatus;

import java.time.LocalDateTime;

public class EngagementGetDTO {
    private Long scenarioId;
    private String scenarioTitle;
    private ScenarioStatus scenarioStatus;
    private LocalDateTime finishTime;
    private String roleType;
    private Long playerId;
    private String characterName;
    /**
     * The player's role-typed token (Director/Backroomer/Role).
     * Returned only because GET /users/{userid}/engagements is restricted
     * to the user themselves, so this is never leaked to other users.
     * The client uses it to restore per-scenario role-token localStorage
     * after a re-login so "Resume" works.
     */
    private String token;

    public Long getScenarioId() { return scenarioId; }
    public void setScenarioId(Long scenarioId) { this.scenarioId = scenarioId; }

    public String getScenarioTitle() { return scenarioTitle; }
    public void setScenarioTitle(String scenarioTitle) { this.scenarioTitle = scenarioTitle; }

    public ScenarioStatus getScenarioStatus() { return scenarioStatus; }
    public void setScenarioStatus(ScenarioStatus scenarioStatus) { this.scenarioStatus = scenarioStatus; }

    public LocalDateTime getFinishTime() { return finishTime; }
    public void setFinishTime(LocalDateTime finishTime) { this.finishTime = finishTime; }

    public String getRoleType() { return roleType; }
    public void setRoleType(String roleType) { this.roleType = roleType; }

    public Long getPlayerId() { return playerId; }
    public void setPlayerId(Long playerId) { this.playerId = playerId; }

    public String getCharacterName() { return characterName; }
    public void setCharacterName(String characterName) { this.characterName = characterName; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
