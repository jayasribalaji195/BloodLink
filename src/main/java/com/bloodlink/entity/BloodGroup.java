package com.bloodlink.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "blood_groups", indexes = {
        @Index(name = "idx_blood_group_name", columnList = "name", unique = true)
})
public class BloodGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 10)
    private String name;

    @Column(length = 255)
    private String description;

    @OneToMany(mappedBy = "bloodGroup", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Donor> donors = new ArrayList<>();

    public BloodGroup() {
    }

    public BloodGroup(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public BloodGroup(Long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Donor> getDonors() {
        return donors;
    }

    public void setDonors(List<Donor> donors) {
        this.donors = donors;
    }

    @Override
    public String toString() {
        return "BloodGroup{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
