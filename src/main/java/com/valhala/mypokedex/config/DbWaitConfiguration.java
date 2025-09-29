package com.valhala.mypokedex.config;

import io.micronaut.context.annotation.ConfigurationProperties;

@ConfigurationProperties("wait.for.db")
public class DbWaitConfiguration {
    private boolean enabled = true;
    private int maxRetries = 30;
    private int waitSeconds = 2;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public int getWaitSeconds() {
        return waitSeconds;
    }

    public void setWaitSeconds(int waitSeconds) {
        this.waitSeconds = waitSeconds;
    }
}
