package com.bloodlink.controller;

import com.bloodlink.dto.request.CreateDonationRequest;
import com.bloodlink.dto.response.DonationRecordResponse;
import com.bloodlink.service.DonationRecordService;
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
@Tag(name = "Donation Records", description = "Endpoints for recording donations and viewing donation history")
@CrossOrigin(origins = "*")
public class DonationRecordController {

    private final DonationRecordService donationRecordService;

    public DonationRecordController(DonationRecordService donationRecordService) {
        this.donationRecordService = donationRecordService;
    }

    @PostMapping("/api/donations")
    @Operation(
            summary = "Record a new blood donation",
            description = "Records a new donation for a donor, automatically updates donor's lastDonationDate, and sets available = false for the 90-day cooldown period."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Donation recorded successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error or invalid donation date"),
            @ApiResponse(responseCode = "404", description = "Donor not found"),
            @ApiResponse(responseCode = "409", description = "Donor is inactive or still within the 90-day cooldown period")
    })
    public ResponseEntity<DonationRecordResponse> recordDonation(@Valid @RequestBody CreateDonationRequest request) {
        DonationRecordResponse response = donationRecordService.recordDonation(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/api/donations")
    @Operation(summary = "Get all donation records", description = "Retrieves all donation records ordered by donation date descending.")
    public ResponseEntity<List<DonationRecordResponse>> getAllDonations() {
        return ResponseEntity.ok(donationRecordService.getAllDonations());
    }

    @GetMapping("/api/donations/{id}")
    @Operation(summary = "Get donation record by ID", description = "Retrieves a single donation record by its unique database ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Donation record retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Donation record not found")
    })
    public ResponseEntity<DonationRecordResponse> getDonationById(@PathVariable Long id) {
        return ResponseEntity.ok(donationRecordService.getDonationById(id));
    }

    @GetMapping("/api/donors/{donorId}/donations")
    @Operation(summary = "Get donation history for a donor", description = "Retrieves all previous donation records for a specific donor.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Donation history retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Donor not found")
    })
    public ResponseEntity<List<DonationRecordResponse>> getDonationHistoryByDonorId(@PathVariable Long donorId) {
        return ResponseEntity.ok(donationRecordService.getDonationsByDonorId(donorId));
    }
}
