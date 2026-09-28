package com.bloodlink.service;

import com.bloodlink.dto.request.CreateDonationRequest;
import com.bloodlink.dto.response.DonationRecordResponse;

import java.util.List;

public interface DonationRecordService {

    DonationRecordResponse recordDonation(CreateDonationRequest request);

    List<DonationRecordResponse> getAllDonations();

    DonationRecordResponse getDonationById(Long id);

    List<DonationRecordResponse> getDonationsByDonorId(Long donorId);
}
