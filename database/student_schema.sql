-- ============================================================================
-- MEMBER 1: STUDENT MANAGEMENT MODULE - DATABASE SCHEMA & SEED DATA
-- Course: Programming in Java (25CSE0202)
-- ============================================================================

CREATE DATABASE IF NOT EXISTS smart_campus_db;
USE smart_campus_db;

-- 1. STUDENTS TABLE
DROP TABLE IF EXISTS students;
CREATE TABLE students (
    student_id VARCHAR(15) PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    department VARCHAR(60) NOT NULL,
    semester INT NOT NULL CHECK (semester BETWEEN 1 AND 8),
    gpa DECIMAL(4, 2) NOT NULL DEFAULT 0.00 CHECK (gpa BETWEEN 0.00 AND 10.00),
    phone VARCHAR(20) NOT NULL
);

-- Initial seed data for student demonstration
INSERT INTO students (student_id, full_name, email, department, semester, gpa, phone) VALUES
('STU101', 'Aarav Sharma', 'aarav.sharma@university.edu', 'Computer Science', 3, 8.95, '9876543210'),
('STU102', 'Diya Nair', 'diya.nair@university.edu', 'Computer Science', 3, 9.20, '9876543211'),
('STU103', 'Rohan Verma', 'rohan.verma@university.edu', 'Electronics', 3, 8.15, '9876543212'),
('STU104', 'Ananya Iyer', 'ananya.iyer@university.edu', 'Information Technology', 3, 9.40, '9876543213'),
('STU105', 'Kabir Mehta', 'kabir.mehta@university.edu', 'Mechanical', 5, 7.85, '9876543214');
