package com.triplewhitefox.statusapp.model;

import java.util.Date;

public class Instance {

    public static int STATUS_UP = 0;
    public static int STATUS_DOWN = 1;
    public static int STATUS_UNHEALTHY = 2;

    private String name;
    private String url;
    private String pingUrl;
    private String responseValidationRegex;
    private String consoleUrl;
    private Date lastChecked;
    private int isUp;

    public Instance(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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

    public String getPingUrl() {
        return pingUrl;
    }

    public void setPingUrl(String pingUrl) {
        this.pingUrl = pingUrl;
    }

    public String getConsoleUrl() {
        return consoleUrl;
    }

    public void setConsoleUrl(String consoleUrl) {
        this.consoleUrl = consoleUrl;
    }

    public void setUp(int up) {
        isUp = up;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getResponseValidationRegex() {
        return responseValidationRegex;
    }

    public void setResponseValidationRegex(String responseValidationRegex) {
        this.responseValidationRegex = responseValidationRegex;
    }
}
