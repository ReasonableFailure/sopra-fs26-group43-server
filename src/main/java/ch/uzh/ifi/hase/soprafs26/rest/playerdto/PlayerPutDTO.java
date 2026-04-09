package ch.uzh.ifi.hase.soprafs26.rest.playerdto;

public class PlayerPutDTO {

    private Long associatedUserId;

    public Long getAssociatedUserId() {
        return associatedUserId;
    }
    public void setAssociatedUserId(Long associatedUserId){
        this.associatedUserId = associatedUserId;
    }
}
