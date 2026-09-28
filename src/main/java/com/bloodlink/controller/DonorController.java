package com.bloodlink.controller;

import com.bloodlink.dto.request.CreateDonorRequest;
import com.bloodlink.dto.request.UpdateDonorRequest;
import com.bloodlink.dto.response.BloodGroupCountResponse;
import com.bloodlink.dto.response.DonorResponse;
import com.bloodlink.service.DonorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/donors")
@Tag(name = "Donor Management & Search", description = "Endpoints for registering, searching, updating, and querying blood donors")
@CrossOrigin(origins = "*")
public class DonorController {

    private final DonorService donorService;

    public DonorController(DonorService donorService) {
        this.donorService = donorService;
    }

    @PostMapping
    @Operation(summary = "Register a new donor", description = "Registers a volunteer donor. Checks email uniqueness and initial 90-day cooldown.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Donor registered successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "404", description = "Blood group not found"),
            @ApiResponse(responseCode = "409", description = "Email already registered")
    })
    public ResponseEntity<DonorResponse> registerDonor(@Valid @RequestBody CreateDonorRequest request) {
        DonorResponse response = donorService.registerDonor(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all donors", description = "Retrieves all donors (both active and inactive) with dynamic cooldown indicators.")
    public ResponseEntity<List<DonorResponse>> getAllDonors() {
        return ResponseEntity.ok(donorService.getAllDonors());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get donor by ID", description = "Retrieves a single donor's details including cooldown status.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Donor retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Donor not found")
    })
    public ResponseEntity<DonorResponse> getDonorById(@PathVariable Long id) {
        return ResponseEntity.ok(donorService.getDonorById(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update donor", description = "Updates a donor's information.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Donor updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "404", description = "Donor or blood group not found"),
            @ApiResponse(responseCode = "409", description = "Email already used by another donor")
    })
    public ResponseEntity<DonorResponse> updateDonor(@PathVariable Long id,
                                                     @Valid @RequestBody UpdateDonorRequest request) {
        return ResponseEntity.ok(donorService.updateDonor(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deactivate donor", description = "Soft-deactivates a donor by marking active = false and available = false.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Donor deactivated successfully"),
            @ApiResponse(responseCode = "404", description = "Donor not found")
    })
    public ResponseEntity<Void> deactivateDonor(@PathVariable Long id) {
        donorService.deactivateDonor(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    @Operation(
            summary = "Search eligible donors",
            description = "Searches for donors by blood group and/or city. Returns ONLY active, available donors who are NOT in the 90-day cooldown period."
    )
    public ResponseEntity<List<DonorResponse>> searchDonors(
            @Parameter(description = "Blood group name, e.g. 'O+' or 'O%2B'")
            @RequestParam(required = false) String bloodGroup,
            @Parameter(description = "City name, e.g. 'Coimbatore'")
            @RequestParam(required = false) String city
    ) {
        return ResponseEntity.ok(donorService.searchDonors(bloodGroup, city));
    }

    @GetMapping("/count-by-blood-group")
    @Operation(
            summary = "Total registered donors by blood group",
            description = "Returns the count of all active registered donors grouped by blood group."
    )
    public ResponseEntity<Map<String, Long>> getDonorCountsByBloodGroup() {
        return ResponseEntity.ok(donorService.getDonorCountsByBloodGroup());
    }

    @GetMapping("/available-count-by-blood-group")
    @Operation(
            summary = "Currently available donors by blood group",
            description = "Returns the count of currently available donors (excluding those in 90-day cooldown) grouped by blood group."
    )
    public ResponseEntity<Map<String, Long>> getAvailableDonorCountsByBloodGroup() {
        return ResponseEntity.ok(donorService.getAvailableDonorCountsByBloodGroup());
    }

    @GetMapping("/count-summary")
    @Operation(
            summary = "Complete blood group donor count summary",
            description = "Returns both registered counts and available counts side-by-side along with totals."
    )
    public ResponseEntity<BloodGroupCountResponse> getBloodGroupCountSummary() {
        return ResponseEntity.ok(donorService.getBloodGroupCountSummary());
    }
}
