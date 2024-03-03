package com.triplewhitefox.statusapp.model;

import java.util.ArrayList;

public class Environment {

    private String name;
    private ArrayList<Instance> instances;

    public Environment(String name) {
        this.name = name;
        instances = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Instance> getInstances() {
        return instances;
    }

    public void addInstance(Instance instance) {
        instances.add(instance);
    }

}
