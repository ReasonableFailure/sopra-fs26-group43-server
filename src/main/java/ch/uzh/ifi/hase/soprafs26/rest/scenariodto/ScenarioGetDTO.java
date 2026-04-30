package ch.uzh.ifi.hase.soprafs26.rest.scenariodto;

import ch.uzh.ifi.hase.soprafs26.constant.ScenarioStatus;

public class ScenarioGetDTO {

    private Long id;
    private String title;
    private String description;
    private ScenarioStatus status;
    private int dayNumber;
    private int exchangeRate;
    private String directorToken;
    private String mastodonProfileUrl;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ScenarioStatus getStatus() {
        return status;
    }

    public void setStatus(ScenarioStatus status) {
        this.status = status;
    }

    public int getDayNumber() {
        return dayNumber;
    }

    public void setDayNumber(int dayNumber) {
        this.dayNumber = dayNumber;
    }

    public int getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(int exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public String getDirectorToken() {
        return directorToken;
    }

    public void setDirectorToken(String directorToken) {
        this.directorToken = directorToken;
    }

    public String getMastodonProfileUrl() {
        return mastodonProfileUrl;
    }

    public void setMastodonProfileUrl(String mastodonProfileUrl) {
        this.mastodonProfileUrl = mastodonProfileUrl;
    }
}
