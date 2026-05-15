package ch.uzh.ifi.hase.soprafs26.rest.playerdto;

public class DirectorGetDTO {
    private Long id;
    private String token;

    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;

    }
    
}
