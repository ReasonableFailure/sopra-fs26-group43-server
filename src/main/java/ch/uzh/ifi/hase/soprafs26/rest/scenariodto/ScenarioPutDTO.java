package ch.uzh.ifi.hase.soprafs26.rest.scenariodto;

import ch.uzh.ifi.hase.soprafs26.constant.ScenarioStatus;

public class ScenarioPutDTO {
    private String title;
    private String description;
    private ScenarioStatus status;
    private Integer dayNumber;
    private Integer exchangeRate;

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

    public Integer getDayNumber() {
        return dayNumber;
    }

    public void setDayNumber(Integer dayNumber) {
        this.dayNumber = dayNumber;
    }

    public Integer getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(Integer exchangeRate) {
        this.exchangeRate = exchangeRate;
    }
}
