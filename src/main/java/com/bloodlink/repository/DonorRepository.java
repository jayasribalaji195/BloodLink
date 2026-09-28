package com.bloodlink.repository;

import com.bloodlink.entity.Donor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DonorRepository extends JpaRepository<Donor, Long> {

    Optional<Donor> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);

    List<Donor> findByActiveTrue();

    List<Donor> findByBloodGroupId(Long bloodGroupId);

    List<Donor> findByCityIgnoreCase(String city);

    List<Donor> findByBloodGroupIdAndActiveTrue(Long bloodGroupId);

    List<Donor> findByCityIgnoreCaseAndActiveTrue(String city);

    List<Donor> findByBloodGroupIdAndCityIgnoreCaseAndActiveTrue(Long bloodGroupId, String city);

    List<Donor> findByBloodGroupNameIgnoreCaseAndActiveTrue(String bloodGroupName);

    List<Donor> findByBloodGroupNameIgnoreCaseAndCityIgnoreCaseAndActiveTrue(String bloodGroupName, String city);

    long countByActiveTrue();

    long countByActiveTrueAndAvailableTrue();

    long countByBloodGroupIdAndActiveTrue(Long bloodGroupId);

    long countByBloodGroupIdAndActiveTrueAndAvailableTrue(Long bloodGroupId);

    boolean existsByBloodGroupId(Long bloodGroupId);

    @Query("SELECT d FROM Donor d WHERE d.active = true " +
           "AND (:bloodGroupName IS NULL OR LOWER(d.bloodGroup.name) = LOWER(:bloodGroupName)) " +
           "AND (:city IS NULL OR LOWER(d.city) = LOWER(:city))")
    List<Donor> searchActiveDonors(@Param("bloodGroupName") String bloodGroupName, @Param("city") String city);
}
