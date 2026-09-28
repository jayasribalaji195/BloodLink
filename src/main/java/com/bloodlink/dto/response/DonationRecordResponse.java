package com.bloodlink.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DonationRecordResponse {

    private Long id;
    private Long donorId;
    private String donorName;
    private String donorBloodGroup;
    private String donorCity;
    private LocalDate donationDate;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDate nextEligibleDate;

    public DonationRecordResponse() {
    }

    public DonationRecordResponse(Long id, Long donorId, String donorName, String donorBloodGroup,
                                  String donorCity, LocalDate donationDate, String notes,
                                  LocalDateTime createdAt, LocalDate nextEligibleDate) {
        this.id = id;
        this.donorId = donorId;
        this.donorName = donorName;
        this.donorBloodGroup = donorBloodGroup;
        this.donorCity = donorCity;
        this.donationDate = donationDate;
        this.notes = notes;
        this.createdAt = createdAt;
        this.nextEligibleDate = nextEligibleDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDonorId() {
        return donorId;
    }

    public void setDonorId(Long donorId) {
        this.donorId = donorId;
    }

    public String getDonorName() {
        return donorName;
    }

    public void setDonorName(String donorName) {
        this.donorName = donorName;
    }

    public String getDonorBloodGroup() {
        return donorBloodGroup;
    }

    public void setDonorBloodGroup(String donorBloodGroup) {
        this.donorBloodGroup = donorBloodGroup;
    }

    public String getDonorCity() {
        return donorCity;
    }

    public void setDonorCity(String donorCity) {
        this.donorCity = donorCity;
    }

    public LocalDate getDonationDate() {
        return donationDate;
    }

    public void setDonationDate(LocalDate donationDate) {
        this.donationDate = donationDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDate getNextEligibleDate() {
        return nextEligibleDate;
    }

    public void setNextEligibleDate(LocalDate nextEligibleDate) {
        this.nextEligibleDate = nextEligibleDate;
    }
}
