package ch.uzh.ifi.hase.soprafs26.rest.scenariodto;

public class ScenarioGetDTO {

    private Long id;
    private String title;
    private String description;
    private Boolean active;
    private int day;
    private int exchangeRate;

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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean isActive) {
        this.active = isActive;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(int exchangeRate) {
        this.exchangeRate = exchangeRate;
    }
}
