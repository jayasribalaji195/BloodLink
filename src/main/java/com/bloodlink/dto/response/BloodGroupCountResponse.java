package com.bloodlink.dto.response;

import java.util.Map;

public class BloodGroupCountResponse {

    private Map<String, Long> registeredCount;
    private Map<String, Long> availableCount;
    private long totalRegistered;
    private long totalAvailable;

    public BloodGroupCountResponse() {
    }

    public BloodGroupCountResponse(Map<String, Long> registeredCount, Map<String, Long> availableCount, long totalRegistered, long totalAvailable) {
        this.registeredCount = registeredCount;
        this.availableCount = availableCount;
        this.totalRegistered = totalRegistered;
        this.totalAvailable = totalAvailable;
    }

    public Map<String, Long> getRegisteredCount() {
        return registeredCount;
    }

    public void setRegisteredCount(Map<String, Long> registeredCount) {
        this.registeredCount = registeredCount;
    }

    public Map<String, Long> getAvailableCount() {
        return availableCount;
    }

    public void setAvailableCount(Map<String, Long> availableCount) {
        this.availableCount = availableCount;
    }

    public long getTotalRegistered() {
        return totalRegistered;
    }

    public void setTotalRegistered(long totalRegistered) {
        this.totalRegistered = totalRegistered;
    }

    public long getTotalAvailable() {
        return totalAvailable;
    }

    public void setTotalAvailable(long totalAvailable) {
        this.totalAvailable = totalAvailable;
    }
}
