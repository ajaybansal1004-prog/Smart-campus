package com.smartcampus.util;

import com.smartcampus.exception.InvalidStudentException;

/**
 * Utility class providing static student validation methods using String handling and Regex.
 * Component assigned to Member 1: Student Management.
 */
public class InputValidator {

    public static void validateStudentData(String id, String name, String email,
                                           String department, int semester, double gpa, String phone)
            throws InvalidStudentException {
        if (id == null || id.trim().isEmpty() || !id.trim().matches("[A-Za-z0-9]{3,15}")) {
            throw new InvalidStudentException("Student ID must be 3-15 alphanumeric characters (e.g., STU101).");
        }
        if (name == null || name.trim().length() < 2) {
            throw new InvalidStudentException("Full name must contain at least 2 characters.");
        }
        if (email == null || !email.contains("@") || !email.contains(".")) {
            throw new InvalidStudentException("Invalid email address format.");
        }
        if (department == null || department.trim().isEmpty()) {
            throw new InvalidStudentException("Department cannot be empty.");
        }
        if (semester < 1 || semester > 8) {
            throw new InvalidStudentException("Semester must be between 1 and 8.");
        }
        if (gpa < 0.0 || gpa > 10.0) {
            throw new InvalidStudentException("GPA must be between 0.00 and 10.00.");
        }
        if (phone == null || !phone.trim().matches("\\d{10}")) {
            throw new InvalidStudentException("Phone number must be exactly 10 digits.");
        }
    }
}
