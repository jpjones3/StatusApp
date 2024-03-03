package com.triplewhitefox.statusapp.model;

import java.util.ArrayList;

public class Application {
    private String name;
    private ArrayList<Environment> environments;

    public Application(String name) {
        this.name = name;
        environments = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Environment> getEnvironments() {
        return environments;
    }

    public void addEnvironment(Environment environment) {
        environments.add(environment);
    }

    public Environment getEnvironment(String name) {
        Environment env = null;
        boolean found = false;

        int i = 0;
        do {
            if (environments.get(i).getName().equals(name)) {
                found = true;
                env = environments.get(i);
            }
            i++;
        } while (!found && i < environments.size());

        return env;
    }

    public boolean hasEnvironment(String name) {

        boolean found = false;
        int i = 0;
        do {
            if (environments.get(i).getName().equals(name)) {
                found = true;
            }
            i++;
        } while (!found && i < environments.size());

        return found;
    }

    public ArrayList<String> getEnvironmentNames() {
        ArrayList<String> names = new ArrayList<>();

        for (int i = 0; i < environments.size(); i++) {
            names.add(environments.get(i).getName());
        }
        return names;
    }
}
