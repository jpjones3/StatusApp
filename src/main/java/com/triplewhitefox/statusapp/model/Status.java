package com.triplewhitefox.statusapp.model;

import javax.validation.constraints.NotEmpty;

public class Status {

    @NotEmpty
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
