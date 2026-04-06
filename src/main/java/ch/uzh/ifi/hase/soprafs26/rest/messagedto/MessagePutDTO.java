package ch.uzh.ifi.hase.soprafs26.rest.messagedto;

import ch.uzh.ifi.hase.soprafs26.constant.CommsStatus;

public class MessagePutDTO {

    private CommsStatus status;

    public CommsStatus getStatus() {
        return status;
    }

    public void setStatus(CommsStatus status) {
        this.status = status;
    }
}