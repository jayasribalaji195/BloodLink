package com.bloodlink.service.impl;

import com.bloodlink.dto.request.CreateDonorRequest;
import com.bloodlink.dto.request.UpdateDonorRequest;
import com.bloodlink.dto.response.BloodGroupCountResponse;
import com.bloodlink.dto.response.DonorResponse;
import com.bloodlink.entity.BloodGroup;
import com.bloodlink.entity.Donor;
import com.bloodlink.exception.BusinessRuleException;
import com.bloodlink.exception.DuplicateResourceException;
import com.bloodlink.exception.ResourceNotFoundException;
import com.bloodlink.repository.BloodGroupRepository;
import com.bloodlink.repository.DonorRepository;
import com.bloodlink.service.DonorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class DonorServiceImpl implements DonorService {

    private static final Logger log = LoggerFactory.getLogger(DonorServiceImpl.class);

    private final DonorRepository donorRepository;
    private final BloodGroupRepository bloodGroupRepository;

    public DonorServiceImpl(DonorRepository donorRepository, BloodGroupRepository bloodGroupRepository) {
        this.donorRepository = donorRepository;
        this.bloodGroupRepository = bloodGroupRepository;
    }

    @Override
    public DonorResponse registerDonor(CreateDonorRequest request) {
        String trimmedEmail = request.getEmail().trim().toLowerCase();

        // Rule 11: Duplicate email check
        if (donorRepository.existsByEmailIgnoreCase(trimmedEmail)) {
            throw new DuplicateResourceException("A donor with email '" + trimmedEmail + "' already exists.");
        }

        // Rule 10: Blood group existence check
        BloodGroup bloodGroup = bloodGroupRepository.findById(request.getBloodGroupId())
                .orElseThrow(() -> new ResourceNotFoundException("Blood group not found with ID: " + request.getBloodGroupId()));

        // Rule 8: Donation date validation
        if (request.getLastDonationDate() != null && request.getLastDonationDate().isAfter(LocalDate.now())) {
            throw new BusinessRuleException("Last donation date cannot be in the future.");
        }

        Donor donor = new Donor();
        donor.setName(request.getName().trim());
        donor.setEmail(trimmedEmail);
        donor.setPhone(request.getPhone().trim());
        donor.setBloodGroup(bloodGroup);
        donor.setCity(request.getCity().trim());
        donor.setLastDonationDate(request.getLastDonationDate());
        donor.setActive(true);

        // Rules 1, 3: Calculate initial availability based on cooldown
        boolean inCooldown = isInCooldown(request.getLastDonationDate());
        donor.setAvailable(!inCooldown);

        Donor savedDonor = donorRepository.save(donor);
        log.info("Registered new donor: {} (ID: {}), inCooldown: {}, available: {}",
                savedDonor.getName(), savedDonor.getId(), inCooldown, savedDonor.isAvailable());

        return mapToResponse(savedDonor);
    }

    @Override
    public List<DonorResponse> getAllDonors() {
        List<Donor> donors = donorRepository.findAll();
        donors.forEach(this::checkAndSyncDonorAvailability);
        return donors.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public DonorResponse getDonorById(Long id) {
        Donor donor = getDonorEntityById(id);
        checkAndSyncDonorAvailability(donor);
        return mapToResponse(donor);
    }

    @Override
    @Transactional(readOnly = true)
    public Donor getDonorEntityById(Long id) {
        return donorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Donor not found with ID: " + id));
    }

    @Override
    public DonorResponse updateDonor(Long id, UpdateDonorRequest request) {
        Donor donor = getDonorEntityById(id);

        String trimmedEmail = request.getEmail().trim().toLowerCase();
        if (donorRepository.existsByEmailIgnoreCaseAndIdNot(trimmedEmail, id)) {
            throw new DuplicateResourceException("Another donor with email '" + trimmedEmail + "' already exists.");
        }

        BloodGroup bloodGroup = bloodGroupRepository.findById(request.getBloodGroupId())
                .orElseThrow(() -> new ResourceNotFoundException("Blood group not found with ID: " + request.getBloodGroupId()));

        if (request.getLastDonationDate() != null && request.getLastDonationDate().isAfter(LocalDate.now())) {
            throw new BusinessRuleException("Last donation date cannot be in the future.");
        }

        donor.setName(request.getName().trim());
        donor.setEmail(trimmedEmail);
        donor.setPhone(request.getPhone().trim());
        donor.setBloodGroup(bloodGroup);
        donor.setCity(request.getCity().trim());
        donor.setLastDonationDate(request.getLastDonationDate());

        if (request.getActive() != null) {
            donor.setActive(request.getActive());
        }

        // Re-evaluate availability
        if (request.getAvailable() != null) {
            // If in cooldown, availability must remain false regardless
            if (isInCooldown(donor.getLastDonationDate())) {
                donor.setAvailable(false);
            } else {
                donor.setAvailable(request.getAvailable());
            }
        } else {
            checkAndSyncDonorAvailability(donor);
        }

        Donor updated = donorRepository.save(donor);
        return mapToResponse(updated);
    }

    @Override
    public void deactivateDonor(Long id) {
        Donor donor = getDonorEntityById(id);
        donor.setActive(false);
        donor.setAvailable(false);
        donorRepository.save(donor);
        log.info("Soft-deactivated donor with ID: {}", id);
    }

    @Override
    public List<DonorResponse> searchDonors(String bloodGroup, String city) {
        String bg = (bloodGroup != null && !bloodGroup.isBlank()) ? bloodGroup.trim() : null;
        String ct = (city != null && !city.isBlank()) ? city.trim() : null;

        List<Donor> activeDonors = donorRepository.searchActiveDonors(bg, ct);

        // Auto-sync availability and apply strict business rules:
        // - active == true
        // - available == true
        // - NOT inside the 90-day cooldown period
        return activeDonors.stream()
                .peek(this::checkAndSyncDonorAvailability)
                .filter(d -> d.isActive() && d.isAvailable() && !isInCooldown(d.getLastDonationDate()))
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getDonorCountsByBloodGroup() {
        List<BloodGroup> bloodGroups = bloodGroupRepository.findAll();
        Map<String, Long> counts = new LinkedHashMap<>();
        for (BloodGroup bg : bloodGroups) {
            long count = donorRepository.countByBloodGroupIdAndActiveTrue(bg.getId());
            counts.put(bg.getName(), count);
        }
        return counts;
    }

    @Override
    public Map<String, Long> getAvailableDonorCountsByBloodGroup() {
        // Sync before counting to make sure expired cooldowns reflect availability
        List<Donor> activeDonors = donorRepository.findByActiveTrue();
        activeDonors.forEach(this::checkAndSyncDonorAvailability);

        List<BloodGroup> bloodGroups = bloodGroupRepository.findAll();
        Map<String, Long> counts = new LinkedHashMap<>();
        for (BloodGroup bg : bloodGroups) {
            long count = donorRepository.countByBloodGroupIdAndActiveTrueAndAvailableTrue(bg.getId());
            counts.put(bg.getName(), count);
        }
        return counts;
    }

    @Override
    public BloodGroupCountResponse getBloodGroupCountSummary() {
        Map<String, Long> registered = getDonorCountsByBloodGroup();
        Map<String, Long> available = getAvailableDonorCountsByBloodGroup();

        long totalRegistered = registered.values().stream().mapToLong(Long::longValue).sum();
        long totalAvailable = available.values().stream().mapToLong(Long::longValue).sum();

        return new BloodGroupCountResponse(registered, available, totalRegistered, totalAvailable);
    }

    @Override
    public boolean isInCooldown(LocalDate lastDonationDate) {
        if (lastDonationDate == null) {
            return false;
        }
        LocalDate cooldownEndDate = lastDonationDate.plusDays(COOLDOWN_DAYS);
        return cooldownEndDate.isAfter(LocalDate.now());
    }

    @Override
    public long getDaysUntilEligible(LocalDate lastDonationDate) {
        if (lastDonationDate == null || !isInCooldown(lastDonationDate)) {
            return 0;
        }
        LocalDate cooldownEndDate = lastDonationDate.plusDays(COOLDOWN_DAYS);
        long days = ChronoUnit.DAYS.between(LocalDate.now(), cooldownEndDate);
        return Math.max(0, days);
    }

    @Override
    public LocalDate getNextEligibleDate(LocalDate lastDonationDate) {
        if (lastDonationDate == null) {
            return LocalDate.now();
        }
        return lastDonationDate.plusDays(COOLDOWN_DAYS);
    }

    /**
     * Feature 5: Auto-Re-enable donor availability after 90-day cooldown expires.
     * If the donor is active and cooldown has passed, but donor is currently flagged unavailable,
     * this method automatically restores donor.available = true and persists the change.
     */
    @Override
    public boolean checkAndSyncDonorAvailability(Donor donor) {
        if (donor == null || !donor.isActive()) {
            return false;
        }

        boolean inCooldown = isInCooldown(donor.getLastDonationDate());

        if (inCooldown && donor.isAvailable()) {
            // Still in cooldown, cannot be available
            donor.setAvailable(false);
            donorRepository.save(donor);
            log.info("Donor ID {} is within 90-day cooldown. Auto-adjusted available = false", donor.getId());
            return false;
        } else if (!inCooldown && !donor.isAvailable()) {
            // Cooldown expired! Auto-re-enable
            donor.setAvailable(true);
            donorRepository.save(donor);
            log.info("Donor ID {} cooldown expired. Auto-restored available = true", donor.getId());
            return true;
        }

        return donor.isAvailable();
    }

    @Override
    public DonorResponse mapToResponse(Donor donor) {
        boolean inCooldown = isInCooldown(donor.getLastDonationDate());
        long daysRemaining = getDaysUntilEligible(donor.getLastDonationDate());
        LocalDate nextEligible = getNextEligibleDate(donor.getLastDonationDate());

        return new DonorResponse(
                donor.getId(),
                donor.getName(),
                donor.getEmail(),
                donor.getPhone(),
                donor.getBloodGroup() != null ? donor.getBloodGroup().getId() : null,
                donor.getBloodGroup() != null ? donor.getBloodGroup().getName() : null,
                donor.getCity(),
                donor.getLastDonationDate(),
                donor.isAvailable(),
                donor.isActive(),
                inCooldown,
                daysRemaining,
                nextEligible,
                donor.getCreatedAt(),
                donor.getUpdatedAt()
        );
    }
}
