package com.bloodlink.dto.response;

public class BloodGroupResponse {

    private Long id;
    private String name;
    private String description;
    private long totalDonors;
    private long availableDonors;

    public BloodGroupResponse() {
    }

    public BloodGroupResponse(Long id, String name, String description, long totalDonors, long availableDonors) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.totalDonors = totalDonors;
        this.availableDonors = availableDonors;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getTotalDonors() {
        return totalDonors;
    }

    public void setTotalDonors(long totalDonors) {
        this.totalDonors = totalDonors;
    }

    public long getAvailableDonors() {
        return availableDonors;
    }

    public void setAvailableDonors(long availableDonors) {
        this.availableDonors = availableDonors;
    }
}
