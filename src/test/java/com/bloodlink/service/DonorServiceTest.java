package com.bloodlink.service;

import com.bloodlink.dto.request.CreateDonorRequest;
import com.bloodlink.dto.response.DonorResponse;
import com.bloodlink.entity.BloodGroup;
import com.bloodlink.entity.Donor;
import com.bloodlink.exception.BusinessRuleException;
import com.bloodlink.exception.DuplicateResourceException;
import com.bloodlink.exception.ResourceNotFoundException;
import com.bloodlink.repository.BloodGroupRepository;
import com.bloodlink.repository.DonorRepository;
import com.bloodlink.service.impl.DonorServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DonorServiceTest {

    @Mock
    private DonorRepository donorRepository;

    @Mock
    private BloodGroupRepository bloodGroupRepository;

    @InjectMocks
    private DonorServiceImpl donorService;

    private BloodGroup bloodGroupOPlus;
    private Donor eligibleDonor;
    private Donor cooldownDonor;

    @BeforeEach
    void setUp() {
        bloodGroupOPlus = new BloodGroup(1L, "O+", "Can donate to O+, A+, B+, AB+");

        eligibleDonor = new Donor("Ravi Kumar", "ravi@example.com", "9876543210",
                bloodGroupOPlus, "Coimbatore", LocalDate.now().minusDays(100));
        eligibleDonor.setId(1L);
        eligibleDonor.setAvailable(true);
        eligibleDonor.setActive(true);

        cooldownDonor = new Donor("Suresh M", "suresh@example.com", "9876543211",
                bloodGroupOPlus, "Coimbatore", LocalDate.now().minusDays(30));
        cooldownDonor.setId(2L);
        cooldownDonor.setAvailable(false);
        cooldownDonor.setActive(true);
    }

    @Test
    @DisplayName("Should correctly calculate 90-day cooldown status")
    void testCooldownCalculation() {
        LocalDate donatedRecently = LocalDate.now().minusDays(45);
        LocalDate donatedLongAgo = LocalDate.now().minusDays(95);

        assertTrue(donorService.isInCooldown(donatedRecently), "Donor with 45 days past donation should be in cooldown");
        assertFalse(donorService.isInCooldown(donatedLongAgo), "Donor with 95 days past donation should not be in cooldown");
        assertFalse(donorService.isInCooldown(null), "Donor with null donation date should not be in cooldown");

        long daysRemaining = donorService.getDaysUntilEligible(donatedRecently);
        assertEquals(45, daysRemaining, "Should have 45 days remaining until eligible");
    }

    @Test
    @DisplayName("Should register new donor successfully when valid")
    void testRegisterDonor_Success() {
        CreateDonorRequest request = new CreateDonorRequest("Arun", "arun@example.com", "9876543210",
                1L, "Coimbatore", LocalDate.now().minusDays(100));

        when(donorRepository.existsByEmailIgnoreCase("arun@example.com")).thenReturn(false);
        when(bloodGroupRepository.findById(1L)).thenReturn(Optional.of(bloodGroupOPlus));
        when(donorRepository.save(any(Donor.class))).thenAnswer(invocation -> {
            Donor d = invocation.getArgument(0);
            d.setId(10L);
            return d;
        });

        DonorResponse response = donorService.registerDonor(request);

        assertNotNull(response);
        assertEquals("Arun", response.getName());
        assertEquals("arun@example.com", response.getEmail());
        assertTrue(response.isAvailable(), "Donor with donation > 90 days ago should be marked available");
        assertFalse(response.isInCooldown());
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when email already exists")
    void testRegisterDonor_DuplicateEmail() {
        CreateDonorRequest request = new CreateDonorRequest("Duplicate", "ravi@example.com", "9876543210",
                1L, "Coimbatore", null);

        when(donorRepository.existsByEmailIgnoreCase("ravi@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> donorService.registerDonor(request));
        verify(donorRepository, never()).save(any(Donor.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when blood group does not exist")
    void testRegisterDonor_InvalidBloodGroup() {
        CreateDonorRequest request = new CreateDonorRequest("John", "john@example.com", "9876543210",
                999L, "Coimbatore", null);

        when(donorRepository.existsByEmailIgnoreCase("john@example.com")).thenReturn(false);
        when(bloodGroupRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> donorService.registerDonor(request));
    }

    @Test
    @DisplayName("Should throw BusinessRuleException when last donation date is in future")
    void testRegisterDonor_FutureDonationDate() {
        CreateDonorRequest request = new CreateDonorRequest("Future", "future@example.com", "9876543210",
                1L, "Coimbatore", LocalDate.now().plusDays(2));

        when(donorRepository.existsByEmailIgnoreCase("future@example.com")).thenReturn(false);
        when(bloodGroupRepository.findById(1L)).thenReturn(Optional.of(bloodGroupOPlus));

        assertThrows(BusinessRuleException.class, () -> donorService.registerDonor(request));
    }

    @Test
    @DisplayName("Auto-restore availability when cooldown expires")
    void testCheckAndSyncDonorAvailability_AutoRestore() {
        Donor donor = new Donor("Test Donor", "test@example.com", "9876543210",
                bloodGroupOPlus, "Coimbatore", LocalDate.now().minusDays(92));
        donor.setId(5L);
        donor.setActive(true);
        donor.setAvailable(false); // was previously marked unavailable

        when(donorRepository.save(donor)).thenReturn(donor);

        boolean isAvailable = donorService.checkAndSyncDonorAvailability(donor);

        assertTrue(isAvailable, "Donor availability should be restored to true after cooldown expired");
        assertTrue(donor.isAvailable());
        verify(donorRepository, times(1)).save(donor);
    }

    @Test
    @DisplayName("Search donors should exclude donors in cooldown or inactive")
    void testSearchDonors_ExcludesCooldownAndInactive() {
        Donor inactiveDonor = new Donor("Inactive", "in@example.com", "9876543212",
                bloodGroupOPlus, "Coimbatore", LocalDate.now().minusDays(120));
        inactiveDonor.setId(3L);
        inactiveDonor.setActive(false);
        inactiveDonor.setAvailable(false);

        when(donorRepository.searchActiveDonors("O+", "Coimbatore"))
                .thenReturn(List.of(eligibleDonor, cooldownDonor));

        List<DonorResponse> results = donorService.searchDonors("O+", "Coimbatore");

        assertEquals(1, results.size(), "Should return only eligible donor");
        assertEquals("Ravi Kumar", results.get(0).getName());
    }
}
