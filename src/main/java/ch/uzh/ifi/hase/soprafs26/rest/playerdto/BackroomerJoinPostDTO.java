package ch.uzh.ifi.hase.soprafs26.rest.playerdto;

/**
 * Body of POST /scenarios/{id}/backroomers - a user attempting to become
 * a backroomer must supply the director-set join code.
 */
public class BackroomerJoinPostDTO {
    private String code;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
