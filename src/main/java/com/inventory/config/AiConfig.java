package com.inventory.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "ai.api")
public class AiConfig {
    private String url;
    private String key;
    private String model;

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getKey() { return key; }
    public void setKey(String key) { this.key = key; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
}