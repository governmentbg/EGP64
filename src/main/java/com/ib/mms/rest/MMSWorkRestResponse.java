package com.ib.mms.rest;

/**
 * Super class for all responses into the REST application holding status and message
 * @author nikolai.kosev
 */
public class MMSWorkRestResponse {
    public enum STATUS{
        ERROR,WARNING, SUCCESS
    }

    private STATUS status;
    private String message;

    public MMSWorkRestResponse(){}

    public MMSWorkRestResponse(STATUS status, String message) {
        this.status = status;
        this.message = message;
    }

    public STATUS getStatus() {
        return status;
    }

    public void setStatus(STATUS status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


}
