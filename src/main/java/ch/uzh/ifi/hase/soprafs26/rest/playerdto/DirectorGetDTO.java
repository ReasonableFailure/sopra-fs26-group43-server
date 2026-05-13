package ch.uzh.ifi.hase.soprafs26.rest.playerdto;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DirectorGetDTO {
    private Long userId;
    private String token;

    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }

    public Long getUserId() {
        return userId;
    }
    public void setUserId(Long userId) {
        this.userId = userId;

    }
    
}
