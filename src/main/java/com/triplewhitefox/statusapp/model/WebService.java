package com.triplewhitefox.statusapp.model;

import java.util.Date;

public class WebService {

    private String name;
    private String endPoint;
    private String requestPayload;
    private String requestPayloadFile;
    private String responseValidationRegex;
    private Date lastChecked;
    private int isUp;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getLastChecked() {
        return lastChecked;
    }

    public void setLastChecked(Date lastChecked) {
        this.lastChecked = lastChecked;
    }

    public int isUp() {
        return isUp;
    }

    public void setUp(int up) {
        isUp = up;
    }

    public String getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public String getRequestPayload() {
        return requestPayload;
    }

    public void setRequestPayload(String requestPayload) {
        this.requestPayload = requestPayload;
    }

    public String getRequestPayloadFile() {
        return requestPayloadFile;
    }

    public void setRequestPayloadFile(String requestPayloadFile) {
        this.requestPayloadFile = requestPayloadFile;
    }

    public String getResponseValidationRegex() {
        return responseValidationRegex;
    }

    public void setResponseValidationRegex(String responseValidationRegex) {
        this.responseValidationRegex = responseValidationRegex;
    }
}
