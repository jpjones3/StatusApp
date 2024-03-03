package com.triplewhitefox.statusapp.controller;

import com.google.gson.Gson;
import com.triplewhitefox.statusapp.model.Application;
import com.triplewhitefox.statusapp.model.Environment;
import com.triplewhitefox.statusapp.model.Instance;
import com.triplewhitefox.statusapp.model.Status;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class StatusController {

    @Value("${myapp.environmentsConfig}")
    private String environmentsConfig;

    private final Application myApp;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss a z");

    public StatusController(@Value("${myapp.environmentsConfig}") final String environmentsConfigProp) {
        //When object is instantiated, first the constructor is executed and then the values of the properties are set and @Value is executed
        //For this reason, property has been added to constructor here

        //Load in the data on our servers in each of our environments
        ClassLoader classLoader = getClass().getClassLoader();
        StringBuilder jsonSB = new StringBuilder();

        try (InputStream inputStream = classLoader.getResourceAsStream(environmentsConfigProp)) {
            assert inputStream != null;
            try (InputStreamReader streamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
                 BufferedReader reader = new BufferedReader(streamReader)) {

                String line;
                while ((line = reader.readLine()) != null) {
                    jsonSB.append(line);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Gson gson = new Gson();
        myApp = gson.fromJson(jsonSB.toString(), Application.class);
    }

    //a scheduled process to check the status every 'fixedRete' ms. The value of 60000 checks every 1 minute
    @Scheduled(fixedRate = 60000)
    public void checkStatuses() {

        ArrayList<String> envs = myApp.getEnvironmentNames();
        //for each of our environments
        for (String name : envs) {
            Environment env = myApp.getEnvironment(name);
            ArrayList<Instance> instances = env.getInstances();

            //and each instance within that environment
            for (Instance instance : instances) {

                boolean isUp = false;
                try {
                    //check the status by making a GET request
                    URL url = new URL(instance.getPingUrl());
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(5000); //set timeout to 5 seconds
                    if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        isUp = true;
                    }
                } catch (IOException e) {
                    System.err.println("URL request failed:");
                    e.printStackTrace();
                }
                instance.setUp(isUp);
                instance.setLastChecked(new Date());
            }
        }
    }

    @GetMapping("status")
    public String status(Map<String, Object> model, @ModelAttribute("status") Status status, @RequestParam(required = false, defaultValue = "") String name) {

        ArrayList<String> environmentNames = myApp.getEnvironmentNames();

        if (name.length() == 0) {
            name = environmentNames.get(0);
        }

        if (myApp.hasEnvironment(name)) {
            Environment env = myApp.getEnvironment(name);
            ArrayList<Instance> instances = env.getInstances();
            TreeMap<String, Instance> statusMap = new TreeMap<>();

            Date lastUpdated = null;

            for (Instance instance : instances) {

                String instanceName = instance.getName();

                lastUpdated = instance.getLastChecked();

                statusMap.put(instanceName, instance);
            }
            model.put("currentEnv", status.getName());
            model.put("statusList", statusMap);
            model.put("lastUpdated", (lastUpdated == null ? "NA" : dateFormat.format(lastUpdated)));
        }
        model.put("environments", environmentNames);
        return "status";
    }

    @GetMapping("dashboard")
    public String dashboard(Map<String, Object> model) {

        TreeMap<String, HashMap<String, String>> statusMap = new TreeMap<>();

        ArrayList<String> envs = myApp.getEnvironmentNames();

        Date lastUpdated = null;

        for (String s : envs) {

            HashMap<String, String> instanceMap = new HashMap<>();

            Environment env = myApp.getEnvironment(s);
            ArrayList<Instance> instances = env.getInstances();

            for (Instance instance : instances) {

                String instanceName = instance.getName().toLowerCase();
                instanceMap.put(instanceName, instance.isUp() ? "up" : "down");
                if (lastUpdated == null || lastUpdated.after(instance.getLastChecked())) {
                    lastUpdated = instance.getLastChecked();
                }
            }
            statusMap.put(s, instanceMap);
        }
        model.put("statusList", statusMap);
        model.put("lastUpdated", (lastUpdated == null ? "NA" : dateFormat.format(lastUpdated)));
        return "dashboard";
    }
}
