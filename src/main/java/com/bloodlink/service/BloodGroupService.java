package com.bloodlink.service;

import com.bloodlink.dto.request.CreateBloodGroupRequest;
import com.bloodlink.dto.request.UpdateBloodGroupRequest;
import com.bloodlink.dto.response.BloodGroupResponse;

import java.util.List;

public interface BloodGroupService {

    BloodGroupResponse createBloodGroup(CreateBloodGroupRequest request);

    List<BloodGroupResponse> getAllBloodGroups();

    BloodGroupResponse getBloodGroupById(Long id);

    BloodGroupResponse updateBloodGroup(Long id, UpdateBloodGroupRequest request);

    void deleteBloodGroup(Long id);
}
