package com.bloodlink.service.impl;

import com.bloodlink.dto.request.CreateBloodGroupRequest;
import com.bloodlink.dto.request.UpdateBloodGroupRequest;
import com.bloodlink.dto.response.BloodGroupResponse;
import com.bloodlink.entity.BloodGroup;
import com.bloodlink.exception.BusinessRuleException;
import com.bloodlink.exception.DuplicateResourceException;
import com.bloodlink.exception.ResourceNotFoundException;
import com.bloodlink.repository.BloodGroupRepository;
import com.bloodlink.repository.DonorRepository;
import com.bloodlink.service.BloodGroupService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class BloodGroupServiceImpl implements BloodGroupService {

    private final BloodGroupRepository bloodGroupRepository;
    private final DonorRepository donorRepository;

    public BloodGroupServiceImpl(BloodGroupRepository bloodGroupRepository, DonorRepository donorRepository) {
        this.bloodGroupRepository = bloodGroupRepository;
        this.donorRepository = donorRepository;
    }

    @Override
    public BloodGroupResponse createBloodGroup(CreateBloodGroupRequest request) {
        String trimmedName = request.getName().trim().toUpperCase();

        if (bloodGroupRepository.existsByNameIgnoreCase(trimmedName)) {
            throw new DuplicateResourceException("Blood group '" + trimmedName + "' already exists.");
        }

        BloodGroup bloodGroup = new BloodGroup(trimmedName, request.getDescription());
        BloodGroup saved = bloodGroupRepository.save(bloodGroup);
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BloodGroupResponse> getAllBloodGroups() {
        return bloodGroupRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BloodGroupResponse getBloodGroupById(Long id) {
        BloodGroup bloodGroup = bloodGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blood group not found with ID: " + id));
        return mapToResponse(bloodGroup);
    }

    @Override
    public BloodGroupResponse updateBloodGroup(Long id, UpdateBloodGroupRequest request) {
        BloodGroup bloodGroup = bloodGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blood group not found with ID: " + id));

        String trimmedName = request.getName().trim().toUpperCase();
        if (bloodGroupRepository.existsByNameIgnoreCaseAndIdNot(trimmedName, id)) {
            throw new DuplicateResourceException("Another blood group with name '" + trimmedName + "' already exists.");
        }

        bloodGroup.setName(trimmedName);
        bloodGroup.setDescription(request.getDescription());
        BloodGroup updated = bloodGroupRepository.save(bloodGroup);
        return mapToResponse(updated);
    }

    @Override
    public void deleteBloodGroup(Long id) {
        BloodGroup bloodGroup = bloodGroupRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blood group not found with ID: " + id));

        if (donorRepository.existsByBloodGroupId(id)) {
            throw new BusinessRuleException("Cannot delete blood group '" + bloodGroup.getName() +
                    "' because registered donors are linked to it.");
        }

        bloodGroupRepository.delete(bloodGroup);
    }

    private BloodGroupResponse mapToResponse(BloodGroup bloodGroup) {
        long totalDonors = donorRepository.countByBloodGroupIdAndActiveTrue(bloodGroup.getId());
        long availableDonors = donorRepository.countByBloodGroupIdAndActiveTrueAndAvailableTrue(bloodGroup.getId());

        return new BloodGroupResponse(
                bloodGroup.getId(),
                bloodGroup.getName(),
                bloodGroup.getDescription(),
                totalDonors,
                availableDonors
        );
    }
}
