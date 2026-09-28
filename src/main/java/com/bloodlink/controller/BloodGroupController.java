package com.bloodlink.controller;

import com.bloodlink.dto.request.CreateBloodGroupRequest;
import com.bloodlink.dto.request.UpdateBloodGroupRequest;
import com.bloodlink.dto.response.BloodGroupResponse;
import com.bloodlink.service.BloodGroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/blood-groups")
@Tag(name = "Blood Group Management", description = "Endpoints for managing blood groups (A+, A-, B+, B-, AB+, AB-, O+, O-)")
@CrossOrigin(origins = "*")
public class BloodGroupController {

    private final BloodGroupService bloodGroupService;

    public BloodGroupController(BloodGroupService bloodGroupService) {
        this.bloodGroupService = bloodGroupService;
    }

    @PostMapping
    @Operation(summary = "Create a new blood group", description = "Creates a new blood group entry. Blood group name must be unique.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Blood group created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error in request body"),
            @ApiResponse(responseCode = "409", description = "Blood group with same name already exists")
    })
    public ResponseEntity<BloodGroupResponse> createBloodGroup(@Valid @RequestBody CreateBloodGroupRequest request) {
        BloodGroupResponse response = bloodGroupService.createBloodGroup(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all blood groups", description = "Retrieves all blood groups with current registered and available donor counts.")
    public ResponseEntity<List<BloodGroupResponse>> getAllBloodGroups() {
        return ResponseEntity.ok(bloodGroupService.getAllBloodGroups());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get blood group by ID", description = "Retrieves a blood group by its unique database ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Blood group retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Blood group not found")
    })
    public ResponseEntity<BloodGroupResponse> getBloodGroupById(@PathVariable Long id) {
        return ResponseEntity.ok(bloodGroupService.getBloodGroupById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update blood group", description = "Updates an existing blood group's details.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Blood group updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "Blood group not found"),
            @ApiResponse(responseCode = "409", description = "Conflict with existing blood group name")
    })
    public ResponseEntity<BloodGroupResponse> updateBloodGroup(@PathVariable Long id,
                                                               @Valid @RequestBody UpdateBloodGroupRequest request) {
        return ResponseEntity.ok(bloodGroupService.updateBloodGroup(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete blood group", description = "Deletes a blood group if no registered donors depend on it.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Blood group deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Blood group not found"),
            @ApiResponse(responseCode = "409", description = "Cannot delete blood group linked to donors")
    })
    public ResponseEntity<Void> deleteBloodGroup(@PathVariable Long id) {
        bloodGroupService.deleteBloodGroup(id);
        return ResponseEntity.noContent().build();
    }
}
