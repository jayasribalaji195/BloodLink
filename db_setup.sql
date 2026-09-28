-- =====================================================================
-- BloodLink — Blood Donor Registry & Search System Database Setup Script
-- DBMS: MySQL 8.x
-- Database Name: bloodlink_db
-- =====================================================================

CREATE DATABASE IF NOT EXISTS `bloodlink_db` 
  DEFAULT CHARACTER SET utf8mb4 
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE `bloodlink_db`;

-- Drop existing tables if re-initializing (in reverse FK order)
DROP TABLE IF EXISTS `donation_records`;
DROP TABLE IF EXISTS `donors`;
DROP TABLE IF EXISTS `blood_groups`;

-- =====================================================================
-- Table: blood_groups
-- =====================================================================
CREATE TABLE `blood_groups` (
  `id` BIGINT NOT NULL AUTO-INCREMENT,
  `name` VARCHAR(10) NOT NULL,
  `description` VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_blood_group_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
-- Table: donors
-- =====================================================================
CREATE TABLE `donors` (
  `id` BIGINT NOT NULL AUTO-INCREMENT,
  `name` VARCHAR(100) NOT NULL,
  `email` VARCHAR(150) NOT NULL,
  `phone` VARCHAR(20) NOT NULL,
  `blood_group_id` BIGINT NOT NULL,
  `city` VARCHAR(100) NOT NULL,
  `last_donation_date` DATE DEFAULT NULL,
  `available` BOOLEAN NOT NULL DEFAULT TRUE,
  `active` BOOLEAN NOT NULL DEFAULT TRUE,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_donor_email` (`email`),
  KEY `idx_donor_city` (`city`),
  KEY `idx_donor_blood_group` (`blood_group_id`),
  KEY `idx_donor_active_available` (`active`, `available`),
  KEY `idx_donor_last_donation` (`last_donation_date`),
  CONSTRAINT `fk_donor_blood_group` FOREIGN KEY (`blood_group_id`) REFERENCES `blood_groups` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
-- Table: donation_records
-- =====================================================================
CREATE TABLE `donation_records` (
  `id` BIGINT NOT NULL AUTO-INCREMENT,
  `donor_id` BIGINT NOT NULL,
  `donation_date` DATE NOT NULL,
  `notes` VARCHAR(500) DEFAULT NULL,
  `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_donation_donor_id` (`donor_id`),
  KEY `idx_donation_date` (`donation_date`),
  CONSTRAINT `fk_donation_donor` FOREIGN KEY (`donor_id`) REFERENCES `donors` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================================
-- Sample Data Insertion
-- =====================================================================

-- 1. Insert 8 Standard Blood Groups
INSERT INTO `blood_groups` (`id`, `name`, `description`) VALUES
(1, 'A+',  'Can donate to A+, AB+; can receive from A+, A-, O+, O-'),
(2, 'A-',  'Can donate to A+, A-, AB+, AB-; can receive from A-, O-'),
(3, 'B+',  'Can donate to B+, AB+; can receive from B+, B-, O+, O-'),
(4, 'B-',  'Can donate to B+, B-, AB+, AB-; can receive from B-, O-'),
(5, 'AB+', 'Universal recipient for red blood cells; can donate to AB+'),
(6, 'AB-', 'Can donate to AB+, AB-; can receive from AB-, A-, B-, O-'),
(7, 'O+',  'Can donate to O+, A+, B+, AB+; can receive from O+, O-'),
(8, 'O-',  'Universal red blood cell donor; can donate to all blood groups');

-- 2. Insert Donors (Demonstrating Available, In Cooldown, and Inactive)
INSERT INTO `donors` (`id`, `name`, `email`, `phone`, `blood_group_id`, `city`, `last_donation_date`, `available`, `active`, `created_at`, `updated_at`) VALUES
(1, 'Arun Kumar', 'arun.kumar@example.com', '9876543210', 1, 'Coimbatore', DATE_SUB(CURDATE(), INTERVAL 120 DAY), TRUE, TRUE, NOW(), NOW()),
(2, 'Kavitha Raj', 'kavitha.raj@example.com', '9876543211', 7, 'Coimbatore', DATE_SUB(CURDATE(), INTERVAL 150 DAY), TRUE, TRUE, NOW(), NOW()),
(3, 'Vignesh S', 'vignesh.s@example.com', '9876543212', 7, 'Coimbatore', NULL, TRUE, TRUE, NOW(), NOW()),
(4, 'Priya Sharma', 'priya.sharma@example.com', '9876543213', 3, 'Chennai', DATE_SUB(CURDATE(), INTERVAL 105 DAY), TRUE, TRUE, NOW(), NOW()),
(5, 'Rahul Verma', 'rahul.verma@example.com', '9876543214', 5, 'Bangalore', DATE_SUB(CURDATE(), INTERVAL 95 DAY), TRUE, TRUE, NOW(), NOW()),
(6, 'Ananya Iyer', 'ananya.iyer@example.com', '9876543215', 8, 'Coimbatore', DATE_SUB(CURDATE(), INTERVAL 200 DAY), TRUE, TRUE, NOW(), NOW()),
(7, 'Suresh Nair', 'suresh.nair@example.com', '9876543216', 2, 'Kochi', DATE_SUB(CURDATE(), INTERVAL 110 DAY), TRUE, TRUE, NOW(), NOW()),
(8, 'Deepak Patel', 'deepak.patel@example.com', '9876543217', 4, 'Mumbai', DATE_SUB(CURDATE(), INTERVAL 130 DAY), TRUE, TRUE, NOW(), NOW()),
(9, 'Meera Krishnan', 'meera.krishnan@example.com', '9876543218', 6, 'Chennai', NULL, TRUE, TRUE, NOW(), NOW()),
(10, 'Siddharth Menon', 'siddharth.m@example.com', '9876543219', 7, 'Coimbatore', DATE_SUB(CURDATE(), INTERVAL 25 DAY), FALSE, TRUE, NOW(), NOW()),
(11, 'Sneha Reddy', 'sneha.reddy@example.com', '9876543220', 1, 'Bangalore', DATE_SUB(CURDATE(), INTERVAL 60 DAY), FALSE, TRUE, NOW(), NOW()),
(12, 'Karthik Raja', 'karthik.raja@example.com', '9876543221', 3, 'Coimbatore', DATE_SUB(CURDATE(), INTERVAL 10 DAY), FALSE, TRUE, NOW(), NOW()),
(13, 'Rajesh G', 'rajesh.g@example.com', '9876543222', 7, 'Coimbatore', DATE_SUB(CURDATE(), INTERVAL 180 DAY), FALSE, FALSE, NOW(), NOW());

-- 3. Insert Historical Donation Records
INSERT INTO `donation_records` (`id`, `donor_id`, `donation_date`, `notes`, `created_at`) VALUES
(1, 1, DATE_SUB(CURDATE(), INTERVAL 120 DAY), 'Rotary Club Blood Drive at Coimbatore Medical College', NOW()),
(2, 2, DATE_SUB(CURDATE(), INTERVAL 150 DAY), 'Annual Blood Donation Camp at PSG Hospitals', NOW()),
(3, 4, DATE_SUB(CURDATE(), INTERVAL 105 DAY), 'Emergency voluntary donation at Apollo Hospital', NOW()),
(4, 5, DATE_SUB(CURDATE(), INTERVAL 95 DAY), 'Corporate donation drive at Tech Park', NOW()),
(5, 6, DATE_SUB(CURDATE(), INTERVAL 200 DAY), 'Universal donor emergency response at KMCH', NOW()),
(6, 7, DATE_SUB(CURDATE(), INTERVAL 110 DAY), 'Red Cross voluntary blood donation', NOW()),
(7, 8, DATE_SUB(CURDATE(), INTERVAL 130 DAY), 'Lions Club blood donation camp', NOW()),
(8, 10, DATE_SUB(CURDATE(), INTERVAL 25 DAY), 'Emergency platelet and whole blood donation (Recent - In Cooldown)', NOW()),
(9, 11, DATE_SUB(CURDATE(), INTERVAL 60 DAY), 'Campus blood donation camp (In Cooldown)', NOW()),
(10, 12, DATE_SUB(CURDATE(), INTERVAL 10 DAY), 'Routine voluntary donation (Recently donated - In Cooldown)', NOW()),
(11, 13, DATE_SUB(CURDATE(), INTERVAL 180 DAY), 'Old historical donation; donor relocated / deactivated', NOW());
