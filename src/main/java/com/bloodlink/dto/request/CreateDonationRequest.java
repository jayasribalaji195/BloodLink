package com.bloodlink.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public class CreateDonationRequest {

    @NotNull(message = "Donor ID is required")
    private Long donorId;

    @NotNull(message = "Donation date is required")
    @PastOrPresent(message = "Donation date cannot be in the future")
    private LocalDate donationDate;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;

    public CreateDonationRequest() {
    }

    public CreateDonationRequest(Long donorId, LocalDate donationDate, String notes) {
        this.donorId = donorId;
        this.donationDate = donationDate;
        this.notes = notes;
    }

    public Long getDonorId() {
        return donorId;
    }

    public void setDonorId(Long donorId) {
        this.donorId = donorId;
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
}
