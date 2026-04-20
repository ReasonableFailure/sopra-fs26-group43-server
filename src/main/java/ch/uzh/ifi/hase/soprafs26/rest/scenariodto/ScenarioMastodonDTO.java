package ch.uzh.ifi.hase.soprafs26.rest.scenariodto;

public class ScenarioMastodonDTO {

    private String mastodonBaseUrl;

    private String mastodonAccessToken;

    public String getMastodonBaseUrl() {
        return mastodonBaseUrl;
    }

    public void setMastodonBaseUrl(String mastodonBaseUrl) {
        this.mastodonBaseUrl = mastodonBaseUrl;
    }

    public String getMastodonAccessToken() {
        return mastodonAccessToken;
    }

    public void setMastodonAccessToken(String mastodonAccessToken) {
        this.mastodonAccessToken = mastodonAccessToken;
    }
}