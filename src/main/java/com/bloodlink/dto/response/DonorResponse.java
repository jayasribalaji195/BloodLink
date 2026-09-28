package com.bloodlink.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DonorResponse {

    private Long id;
    private String name;
    private String email;
    private String phone;
    private Long bloodGroupId;
    private String bloodGroup;
    private String city;
    private LocalDate lastDonationDate;
    private boolean available;
    private boolean active;
    private boolean inCooldown;
    private long daysUntilEligible;
    private LocalDate nextEligibleDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public DonorResponse() {
    }

    public DonorResponse(Long id, String name, String email, String phone, Long bloodGroupId, String bloodGroup,
                         String city, LocalDate lastDonationDate, boolean available, boolean active,
                         boolean inCooldown, long daysUntilEligible, LocalDate nextEligibleDate,
                         LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.bloodGroupId = bloodGroupId;
        this.bloodGroup = bloodGroup;
        this.city = city;
        this.lastDonationDate = lastDonationDate;
        this.available = available;
        this.active = active;
        this.inCooldown = inCooldown;
        this.daysUntilEligible = daysUntilEligible;
        this.nextEligibleDate = nextEligibleDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Long getBloodGroupId() {
        return bloodGroupId;
    }

    public void setBloodGroupId(Long bloodGroupId) {
        this.bloodGroupId = bloodGroupId;
    }

    public String getBloodGroup() {
        return bloodGroup;
    }

    public void setBloodGroup(String bloodGroup) {
        this.bloodGroup = bloodGroup;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public LocalDate getLastDonationDate() {
        return lastDonationDate;
    }

    public void setLastDonationDate(LocalDate lastDonationDate) {
        this.lastDonationDate = lastDonationDate;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isInCooldown() {
        return inCooldown;
    }

    public void setInCooldown(boolean inCooldown) {
        this.inCooldown = inCooldown;
    }

    public long getDaysUntilEligible() {
        return daysUntilEligible;
    }

    public void setDaysUntilEligible(long daysUntilEligible) {
        this.daysUntilEligible = daysUntilEligible;
    }

    public LocalDate getNextEligibleDate() {
        return nextEligibleDate;
    }

    public void setNextEligibleDate(LocalDate nextEligibleDate) {
        this.nextEligibleDate = nextEligibleDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
