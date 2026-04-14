package ch.uzh.ifi.hase.soprafs26.rest.directivedto;

import ch.uzh.ifi.hase.soprafs26.constant.CommsStatus;

public class DirectivePutDTO {

    private CommsStatus status;

    private String response;

    public CommsStatus getStatus() {
        return status;
    }

    public void setStatus(CommsStatus status) {
        this.status = status;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}