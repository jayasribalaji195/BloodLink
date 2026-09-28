package com.bloodlink.service;

import com.bloodlink.dto.request.CreateDonationRequest;
import com.bloodlink.dto.response.DonationRecordResponse;
import com.bloodlink.entity.BloodGroup;
import com.bloodlink.entity.DonationRecord;
import com.bloodlink.entity.Donor;
import com.bloodlink.exception.BusinessRuleException;
import com.bloodlink.exception.ResourceNotFoundException;
import com.bloodlink.repository.DonationRecordRepository;
import com.bloodlink.repository.DonorRepository;
import com.bloodlink.service.impl.DonationRecordServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DonationRecordServiceTest {

    @Mock
    private DonationRecordRepository donationRecordRepository;

    @Mock
    private DonorRepository donorRepository;

    @Mock
    private DonorService donorService;

    @InjectMocks
    private DonationRecordServiceImpl donationRecordService;

    private Donor donor;
    private BloodGroup bloodGroup;

    @BeforeEach
    void setUp() {
        bloodGroup = new BloodGroup(1L, "O+", "Universal recipient/donor compatibility");
        donor = new Donor("Kavitha", "kavitha@example.com", "9876543210", bloodGroup, "Coimbatore", LocalDate.now().minusDays(100));
        donor.setId(1L);
        donor.setActive(true);
        donor.setAvailable(true);
    }

    @Test
    @DisplayName("Should successfully record donation and update donor status to unavailable")
    void testRecordDonation_Success() {
        LocalDate donationDate = LocalDate.now();
        CreateDonationRequest request = new CreateDonationRequest(1L, donationDate, "Voluntary camp donation");

        when(donorRepository.findById(1L)).thenReturn(Optional.of(donor));
        when(donorService.isInCooldown(donor.getLastDonationDate())).thenReturn(false);
        when(donationRecordRepository.save(any(DonationRecord.class))).thenAnswer(invocation -> {
            DonationRecord dr = invocation.getArgument(0);
            dr.setId(100L);
            return dr;
        });

        DonationRecordResponse response = donationRecordService.recordDonation(request);

        assertNotNull(response);
        assertEquals(100L, response.getId());
        assertEquals(1L, response.getDonorId());
        assertEquals(donationDate, response.getDonationDate());

        // Verify donor state was updated
        assertEquals(donationDate, donor.getLastDonationDate());
        assertFalse(donor.isAvailable(), "Donor must be marked unavailable after new donation");
        verify(donorRepository, times(1)).save(donor);
    }

    @Test
    @DisplayName("Should reject donation if donor is already inside 90-day cooldown")
    void testRecordDonation_RejectIfInCooldown() {
        donor.setLastDonationDate(LocalDate.now().minusDays(20));
        CreateDonationRequest request = new CreateDonationRequest(1L, LocalDate.now(), "Attempted early donation");

        when(donorRepository.findById(1L)).thenReturn(Optional.of(donor));
        when(donorService.isInCooldown(donor.getLastDonationDate())).thenReturn(true);
        when(donorService.getNextEligibleDate(any())).thenReturn(LocalDate.now().plusDays(70));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> donationRecordService.recordDonation(request));

        assertTrue(ex.getMessage().contains("cooldown period"));
        verify(donationRecordRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject donation if donor is inactive")
    void testRecordDonation_RejectInactiveDonor() {
        donor.setActive(false);
        CreateDonationRequest request = new CreateDonationRequest(1L, LocalDate.now(), "Inactive donation");

        when(donorRepository.findById(1L)).thenReturn(Optional.of(donor));

        assertThrows(BusinessRuleException.class, () -> donationRecordService.recordDonation(request));
        verify(donationRecordRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException for non-existent donor")
    void testRecordDonation_DonorNotFound() {
        CreateDonationRequest request = new CreateDonationRequest(999L, LocalDate.now(), "Notes");

        when(donorRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> donationRecordService.recordDonation(request));
    }

    @Test
    @DisplayName("Should reject future donation date")
    void testRecordDonation_FutureDate() {
        CreateDonationRequest request = new CreateDonationRequest(1L, LocalDate.now().plusDays(3), "Future donation");

        when(donorRepository.findById(1L)).thenReturn(Optional.of(donor));

        assertThrows(BusinessRuleException.class, () -> donationRecordService.recordDonation(request));
    }
}
