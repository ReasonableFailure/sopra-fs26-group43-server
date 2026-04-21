package ch.uzh.ifi.hase.soprafs26.rest.scenariodto;

public class ScenarioPostDTO {
    private String description;
    private String title;
    private int exchangeRate;
    private int startingMessageCount;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(int exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {}

    public int getStartingMessageCount() {
        return startingMessageCount;
    }

    public void setStartingMessageCount(int startingMessageCount) {
        this.startingMessageCount = startingMessageCount;
    }
}
