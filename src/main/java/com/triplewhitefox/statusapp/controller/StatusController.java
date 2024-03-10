package com.triplewhitefox.statusapp.controller;

import com.google.gson.Gson;
import com.triplewhitefox.statusapp.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class StatusController {

    @Value("${myapp.environmentsConfig}")
    private String environmentsConfig;

    private final Application myApp;

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss a z");
    @Autowired
    private ResourceLoader resourceLoader;

    //public StatusController(WebServiceClient webServiceClient, @Value("${myapp.environmentsConfig}") final String environmentsConfigProp) {
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

                int isUp = Instance.STATUS_DOWN;
                try {
                    //check the status by making a GET request
                    URL url = new URL(instance.getPingUrl());
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    con.setConnectTimeout(5000); //set timeout to 5 seconds
                    if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        isUp = Instance.STATUS_UP;

                        if (!instance.getResponseValidationRegex().equals("")) {
                            // read response
                            StringBuilder response = new StringBuilder();
                            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
                            String line;
                            while ((line = reader.readLine()) != null) {
                                response.append(line);
                            }

                            // check response against expected result
                            if (!response.toString().matches(instance.getResponseValidationRegex())) {
                                isUp = Instance.STATUS_UNHEALTHY;
                            }
                        }
                    }
                } catch (IOException e) {
                    System.err.println("URL request failed:");
                    e.printStackTrace();
                }
                instance.setUp(isUp);
                instance.setLastChecked(new Date());
            }

            //and each web service within that environment
            ArrayList<WebService> webServices = env.getWebServices();
            for (WebService webService: webServices) {
                System.out.println(webService.getName());
                int isUp = Instance.STATUS_DOWN;

                String filePath = webService.getRequestPayloadFile();
                try {
                    // load XML request resource from WEB-INF/config directory
                    Resource resource = resourceLoader.getResource("WEB-INF/config/" + filePath);

                    // read the content of the XML request resource
                    BufferedReader fileReader = new BufferedReader(new InputStreamReader(resource.getInputStream()));
                    StringBuilder content = new StringBuilder();
                    String fileLine;
                    while ((fileLine = fileReader.readLine()) != null) {
                        content.append(fileLine).append("\n");
                    }
                    fileReader.close();
                    String soapXml = content.toString();

                    // SOAP endpoint URL
                    String endPoint = webService.getEndPoint();
                    URL url = new URL(endPoint);

                    // Create HTTP connection
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("Content-Type", "text/xml;charset=UTF-8");
                    connection.setDoOutput(true);

                    // Send SOAP request
                    OutputStream outputStream = connection.getOutputStream();
                    outputStream.write(soapXml.getBytes("UTF-8"));
                    outputStream.flush();

                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        isUp = Instance.STATUS_UP;
                        System.out.println("HTTP status = " + connection.getResponseCode());
                        if (!webService.getResponseValidationRegex().equals("")) {

                            // read SOAP response
                            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                            String line;
                            StringBuilder response = new StringBuilder();
                            while ((line = reader.readLine()) != null) {
                                response.append(line);
                            }

                            // check response against expected result
                            if (!response.toString().matches(webService.getResponseValidationRegex())) {
                                isUp = Instance.STATUS_UNHEALTHY;
                            }

                            reader.close();
                        }
                    }

                    // Close resources
                    outputStream.close();
                    connection.disconnect();

                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("IOException");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                webService.setUp(isUp);
                webService.setLastChecked(new Date());
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

            Date lastUpdated = null;

            //copy the instances to the model used by the UI
            TreeMap<String, Instance> statusMap = new TreeMap<>();
            for (Instance instance : env.getInstances()) {
                String instanceName = instance.getName();
                lastUpdated = instance.getLastChecked();
                statusMap.put(instanceName, instance);
            }
            //copy the web services to the model used by the UI
            TreeMap<String, WebService> wsStatusMap = new TreeMap<>();
            for (WebService webservice : env.getWebServices()) {
                String wsName = webservice.getName();
                wsStatusMap.put(wsName, webservice);
            }
            model.put("currentEnv", status.getName());
            model.put("statusList", statusMap);
            model.put("wsStatusList", wsStatusMap);
            model.put("lastUpdated", (lastUpdated == null ? "NA" : dateFormat.format(lastUpdated)));
        }
        model.put("environments", environmentNames);
        return "status";
    }

    @GetMapping("dashboard")
    public String dashboard(Map<String, Object> model) {

        TreeMap<String, HashMap<String, Integer>> statusMap = new TreeMap<>();

        ArrayList<String> envs = myApp.getEnvironmentNames();

        Date lastUpdated = null;

        for (String s : envs) {

            HashMap<String, Integer> instanceMap = new HashMap<>();

            Environment env = myApp.getEnvironment(s);
            ArrayList<Instance> instances = env.getInstances();

            for (Instance instance : instances) {

                String instanceName = instance.getName().toLowerCase();
                instanceMap.put(instanceName, instance.isUp());
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
