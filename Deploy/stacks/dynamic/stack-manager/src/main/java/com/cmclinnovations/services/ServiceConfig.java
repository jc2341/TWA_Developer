package com.cmclinnovations.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.dockerjava.api.model.HostConfig;

public class ServiceConfig {

    private final String name;
    private final Map<String, URI> endpoints;
    private final String username;
    private final String passwordFile;

    // Docker specific settings
    private final String image;
    private final Map<String, String> environment;

    @JsonProperty("HostConfig")
    private final HostConfig dockerHostConfig;

    public ServiceConfig() {
        name = null;
        endpoints = new HashMap<>();
        username = null;
        passwordFile = null;

        image = null;
        environment = new HashMap<>();
        dockerHostConfig = new HostConfig();
    }

    public String getName() {
        return name;
    }

    public Map<String, URI> getEndpoints() {
        return endpoints;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordFile() {
        return passwordFile;
    }

    public String getPassword() throws IOException {
        final String password;
        if (null == passwordFile) {
            password = "";
        } else {
            try (BufferedReader infile = Files.newBufferedReader(Paths.get(passwordFile))) {
                if (null == (password = infile.readLine())) {
                    throw new IllegalArgumentException("The password file '" + passwordFile
                            + "' specified for the container '" + name + "' is empty.");
                }
            }
        }
        return password;
    }

    public String getImage() {
        return image;
    }

    public Map<String, String> getEnvironment() {
        return environment;
    }

    public HostConfig getDockerHostConfig() {
        return dockerHostConfig;
    }
}
