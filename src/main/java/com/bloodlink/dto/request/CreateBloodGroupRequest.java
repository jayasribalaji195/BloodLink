package com.bloodlink.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateBloodGroupRequest {

    @NotBlank(message = "Blood group name is required")
    @Size(max = 10, message = "Blood group name cannot exceed 10 characters")
    private String name;

    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;

    public CreateBloodGroupRequest() {
    }

    public CreateBloodGroupRequest(String name, String description) {
        this.name = name;
        this.description = description;
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
}
