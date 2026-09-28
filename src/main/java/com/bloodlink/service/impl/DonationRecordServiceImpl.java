package com.bloodlink.service.impl;

import com.bloodlink.dto.request.CreateDonationRequest;
import com.bloodlink.dto.response.DonationRecordResponse;
import com.bloodlink.entity.DonationRecord;
import com.bloodlink.entity.Donor;
import com.bloodlink.exception.BusinessRuleException;
import com.bloodlink.exception.ResourceNotFoundException;
import com.bloodlink.repository.DonationRecordRepository;
import com.bloodlink.repository.DonorRepository;
import com.bloodlink.service.DonationRecordService;
import com.bloodlink.service.DonorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class DonationRecordServiceImpl implements DonationRecordService {

    private static final Logger log = LoggerFactory.getLogger(DonationRecordServiceImpl.class);

    private final DonationRecordRepository donationRecordRepository;
    private final DonorRepository donorRepository;
    private final DonorService donorService;

    public DonationRecordServiceImpl(DonationRecordRepository donationRecordRepository,
                                     DonorRepository donorRepository,
                                     DonorService donorService) {
        this.donationRecordRepository = donationRecordRepository;
        this.donorRepository = donorRepository;
        this.donorService = donorService;
    }

    @Override
    public DonationRecordResponse recordDonation(CreateDonationRequest request) {
        // Rule 9: Donor must exist
        Donor donor = donorRepository.findById(request.getDonorId())
                .orElseThrow(() -> new ResourceNotFoundException("Donor not found with ID: " + request.getDonorId()));

        // Rule 5: Donor must be active
        if (!donor.isActive()) {
            throw new BusinessRuleException("Cannot record blood donation for an inactive donor.");
        }

        // Rule 8: Donation date cannot be in future
        if (request.getDonationDate().isAfter(LocalDate.now())) {
            throw new BusinessRuleException("Donation date cannot be in the future.");
        }

        // Rule 1: Cooldown check before accepting new donation
        if (donorService.isInCooldown(donor.getLastDonationDate())) {
            LocalDate nextEligible = donorService.getNextEligibleDate(donor.getLastDonationDate());
            throw new BusinessRuleException("Donor is still within the 90-day cooldown period. Next eligible date: " + nextEligible);
        }

        if (donor.getLastDonationDate() != null && request.getDonationDate().isBefore(donor.getLastDonationDate())) {
            throw new BusinessRuleException("New donation date (" + request.getDonationDate() +
                    ") cannot precede donor's last donation date (" + donor.getLastDonationDate() + ").");
        }

        // Create DonationRecord
        DonationRecord donationRecord = new DonationRecord();
        donationRecord.setDonor(donor);
        donationRecord.setDonationDate(request.getDonationDate());
        donationRecord.setNotes(request.getNotes() != null ? request.getNotes().trim() : null);

        DonationRecord savedRecord = donationRecordRepository.save(donationRecord);

        // Rule 2 & Rule 3: Update donor's last donation date and mark unavailable
        donor.setLastDonationDate(request.getDonationDate());
        donor.setAvailable(false);
        donorRepository.save(donor);

        log.info("Recorded donation record ID: {} for donor ID: {}. Donor marked unavailable for 90-day cooldown.",
                savedRecord.getId(), donor.getId());

        return mapToResponse(savedRecord);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DonationRecordResponse> getAllDonations() {
        return donationRecordRepository.findAllByOrderByDonationDateDesc().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DonationRecordResponse getDonationById(Long id) {
        DonationRecord record = donationRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Donation record not found with ID: " + id));
        return mapToResponse(record);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DonationRecordResponse> getDonationsByDonorId(Long donorId) {
        // Validate donor exists
        if (!donorRepository.existsById(donorId)) {
            throw new ResourceNotFoundException("Donor not found with ID: " + donorId);
        }

        return donationRecordRepository.findByDonorIdOrderByDonationDateDesc(donorId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private DonationRecordResponse mapToResponse(DonationRecord record) {
        Donor donor = record.getDonor();
        LocalDate nextEligible = record.getDonationDate().plusDays(DonorService.COOLDOWN_DAYS);

        return new DonationRecordResponse(
                record.getId(),
                donor != null ? donor.getId() : null,
                donor != null ? donor.getName() : null,
                (donor != null && donor.getBloodGroup() != null) ? donor.getBloodGroup().getName() : null,
                donor != null ? donor.getCity() : null,
                record.getDonationDate(),
                record.getNotes(),
                record.getCreatedAt(),
                nextEligible
        );
    }
}
