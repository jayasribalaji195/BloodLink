package com.bloodlink.config;

import com.bloodlink.entity.BloodGroup;
import com.bloodlink.entity.DonationRecord;
import com.bloodlink.entity.Donor;
import com.bloodlink.repository.BloodGroupRepository;
import com.bloodlink.repository.DonationRecordRepository;
import com.bloodlink.repository.DonorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final BloodGroupRepository bloodGroupRepository;
    private final DonorRepository donorRepository;
    private final DonationRecordRepository donationRecordRepository;

    public DataInitializer(BloodGroupRepository bloodGroupRepository,
                           DonorRepository donorRepository,
                           DonationRecordRepository donationRecordRepository) {
        this.bloodGroupRepository = bloodGroupRepository;
        this.donorRepository = donorRepository;
        this.donationRecordRepository = donationRecordRepository;
    }

    @Override
    public void run(String... args) {
        if (bloodGroupRepository.count() > 0) {
            log.info("Database already contains data. Skipping initial seeding.");
            return;
        }

        log.info("Initializing BloodLink demo data...");

        // 1. Seed Blood Groups
        Map<String, BloodGroup> bgMap = new HashMap<>();
        String[][] bloodGroupData = {
                {"A+", "Can donate to A+, AB+; can receive from A+, A-, O+, O-"},
                {"A-", "Can donate to A+, A-, AB+, AB-; can receive from A-, O-"},
                {"B+", "Can donate to B+, AB+; can receive from B+, B-, O+, O-"},
                {"B-", "Can donate to B+, B-, AB+, AB-; can receive from B-, O-"},
                {"AB+", "Universal recipient for red blood cells; can donate to AB+"},
                {"AB-", "Can donate to AB+, AB-; can receive from AB-, A-, B-, O-"},
                {"O+", "Can donate to O+, A+, B+, AB+; can receive from O+, O-"},
                {"O-", "Universal red blood cell donor; can donate to all blood groups"}
        };

        for (String[] data : bloodGroupData) {
            BloodGroup bg = new BloodGroup(data[0], data[1]);
            bgMap.put(data[0], bloodGroupRepository.save(bg));
        }
        log.info("Seeded 8 standard blood groups.");

        LocalDate today = LocalDate.now();

        // 2. Seed Donors & Historical Donations
        // (A) Available Donors (Last donation > 90 days ago or never donated)
        createDonorWithDonation("Arun Kumar", "arun.kumar@example.com", "9876543210", bgMap.get("A+"),
                "Coimbatore", today.minusDays(120), true, true,
                "Rotary Club Blood Drive at Coimbatore Medical College");

        createDonorWithDonation("Kavitha Raj", "kavitha.raj@example.com", "9876543211", bgMap.get("O+"),
                "Coimbatore", today.minusDays(150), true, true,
                "Annual Blood Donation Camp at PSG Hospitals");

        createDonorWithoutDonation("Vignesh S", "vignesh.s@example.com", "9876543212", bgMap.get("O+"),
                "Coimbatore", true, true);

        createDonorWithDonation("Priya Sharma", "priya.sharma@example.com", "9876543213", bgMap.get("B+"),
                "Chennai", today.minusDays(105), true, true,
                "Emergency voluntary donation at Apollo Hospital");

        createDonorWithDonation("Rahul Verma", "rahul.verma@example.com", "9876543214", bgMap.get("AB+"),
                "Bangalore", today.minusDays(95), true, true,
                "Corporate donation drive at Tech Park");

        createDonorWithDonation("Ananya Iyer", "ananya.iyer@example.com", "9876543215", bgMap.get("O-"),
                "Coimbatore", today.minusDays(200), true, true,
                "Universal donor emergency response at KMCH");

        createDonorWithDonation("Suresh Nair", "suresh.nair@example.com", "9876543216", bgMap.get("A-"),
                "Kochi", today.minusDays(110), true, true,
                "Red Cross voluntary blood donation");

        createDonorWithDonation("Deepak Patel", "deepak.patel@example.com", "9876543217", bgMap.get("B-"),
                "Mumbai", today.minusDays(130), true, true,
                "Lions Club blood donation camp");

        createDonorWithoutDonation("Meera Krishnan", "meera.krishnan@example.com", "9876543218", bgMap.get("AB-"),
                "Chennai", true, true);

        // (B) Donors within 90-day Cooldown (available = false)
        createDonorWithDonation("Siddharth Menon", "siddharth.m@example.com", "9876543219", bgMap.get("O+"),
                "Coimbatore", today.minusDays(25), false, true,
                "Emergency platelet and whole blood donation (Recent - In Cooldown)");

        createDonorWithDonation("Sneha Reddy", "sneha.reddy@example.com", "9876543220", bgMap.get("A+"),
                "Bangalore", today.minusDays(60), false, true,
                "Campus blood donation camp (In Cooldown)");

        createDonorWithDonation("Karthik Raja", "karthik.raja@example.com", "9876543221", bgMap.get("B+"),
                "Coimbatore", today.minusDays(10), false, true,
                "Routine voluntary donation (Recently donated - In Cooldown)");

        // (C) Inactive Donor (active = false)
        createDonorWithDonation("Rajesh G", "rajesh.g@example.com", "9876543222", bgMap.get("O+"),
                "Coimbatore", today.minusDays(180), false, false,
                "Old historical donation; donor relocated / deactivated");

        log.info("=================================================================");
        log.info("BloodLink initialized successfully!");
        log.info("Registered Donors: {}", donorRepository.count());
        log.info("Active Donors:     {}", donorRepository.countByActiveTrue());
        log.info("Available Donors:  {}", donorRepository.countByActiveTrueAndAvailableTrue());
        log.info("Donation Records:  {}", donationRecordRepository.count());
        log.info("Web Application:   http://localhost:8080/");
        log.info("Swagger UI:        http://localhost:8080/swagger-ui/index.html");
        log.info("=================================================================");
    }

    private void createDonorWithDonation(String name, String email, String phone, BloodGroup bloodGroup,
                                         String city, LocalDate donationDate, boolean available, boolean active,
                                         String notes) {
        Donor donor = new Donor();
        donor.setName(name);
        donor.setEmail(email);
        donor.setPhone(phone);
        donor.setBloodGroup(bloodGroup);
        donor.setCity(city);
        donor.setLastDonationDate(donationDate);
        donor.setAvailable(available);
        donor.setActive(active);

        Donor savedDonor = donorRepository.save(donor);

        DonationRecord record = new DonationRecord(savedDonor, donationDate, notes);
        donationRecordRepository.save(record);
    }

    private void createDonorWithoutDonation(String name, String email, String phone, BloodGroup bloodGroup,
                                            String city, boolean available, boolean active) {
        Donor donor = new Donor();
        donor.setName(name);
        donor.setEmail(email);
        donor.setPhone(phone);
        donor.setBloodGroup(bloodGroup);
        donor.setCity(city);
        donor.setLastDonationDate(null);
        donor.setAvailable(available);
        donor.setActive(active);

        donorRepository.save(donor);
    }
}
