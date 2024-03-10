package com.triplewhitefox.statusapp.model;

import java.util.ArrayList;

public class Environment {

    private String name;
    private ArrayList<Instance> instances;
    private ArrayList<WebService> webServices;

    public Environment(String name) {
        this.name = name;
        instances = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setInstances(ArrayList<Instance> instances) {
        this.instances = instances;
    }

    public ArrayList<WebService> getWebServices() {
        return webServices;
    }

    public void setWebServices(ArrayList<WebService> webServices) {
        this.webServices = webServices;
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
