package com.bloodlink.service;

import com.bloodlink.dto.request.CreateDonorRequest;
import com.bloodlink.dto.request.UpdateDonorRequest;
import com.bloodlink.dto.response.BloodGroupCountResponse;
import com.bloodlink.dto.response.DonorResponse;
import com.bloodlink.entity.Donor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface DonorService {

    int COOLDOWN_DAYS = 90;

    DonorResponse registerDonor(CreateDonorRequest request);

    List<DonorResponse> getAllDonors();

    DonorResponse getDonorById(Long id);

    Donor getDonorEntityById(Long id);

    DonorResponse updateDonor(Long id, UpdateDonorRequest request);

    void deactivateDonor(Long id);

    List<DonorResponse> searchDonors(String bloodGroup, String city);

    Map<String, Long> getDonorCountsByBloodGroup();

    Map<String, Long> getAvailableDonorCountsByBloodGroup();

    BloodGroupCountResponse getBloodGroupCountSummary();

    boolean isInCooldown(LocalDate lastDonationDate);

    long getDaysUntilEligible(LocalDate lastDonationDate);

    LocalDate getNextEligibleDate(LocalDate lastDonationDate);

    boolean checkAndSyncDonorAvailability(Donor donor);

    DonorResponse mapToResponse(Donor donor);
}
