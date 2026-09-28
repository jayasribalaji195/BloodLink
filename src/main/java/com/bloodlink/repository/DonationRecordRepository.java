package com.bloodlink.repository;

import com.bloodlink.entity.DonationRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DonationRecordRepository extends JpaRepository<DonationRecord, Long> {

    List<DonationRecord> findByDonorIdOrderByDonationDateDesc(Long donorId);

    List<DonationRecord> findAllByOrderByDonationDateDesc();

    Optional<DonationRecord> findFirstByDonorIdOrderByDonationDateDesc(Long donorId);

    long countByDonorId(Long donorId);
}
