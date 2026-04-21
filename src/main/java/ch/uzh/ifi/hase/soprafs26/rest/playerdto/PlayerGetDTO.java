package ch.uzh.ifi.hase.soprafs26.rest.playerdto;

public class PlayerGetDTO {
    private String authToken;
    private Long id;

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
