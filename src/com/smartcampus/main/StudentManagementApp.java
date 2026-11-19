package com.smartcampus.main;

import com.smartcampus.exception.DatabaseOperationException;
import com.smartcampus.exception.InvalidStudentException;
import com.smartcampus.model.Student;
import com.smartcampus.service.StudentService;
import com.smartcampus.util.DatabaseConnection;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Dedicated Module Runner for Member 1: Student Management.
 * Extracts and demonstrates all student-specific operations:
 * Registration, Updating, Deletion, ID/Name Sorting, O(1) Cache Search,
 * Department Grouping (TreeMap), and CSV Import/Export.
 */
public class StudentManagementApp {
    private static final Scanner scanner = new Scanner(System.in);
    private static final StudentService studentService = new StudentService();

    public static void main(String[] args) {
        System.out.println("==================================================================");
        System.out.println("       MEMBER 1 MODULE: STUDENT MANAGEMENT SYSTEM (25CSE0202)     ");
        System.out.println("==================================================================");
        System.out.println(DatabaseConnection.getConnectionStatusMessage());

        boolean running = true;
        while (running) {
            printStudentMenu();
            String ch = scanner.nextLine().trim();

            try {
                switch (ch) {
                    case "1":
                        System.out.print("Student ID (e.g., STU106): ");
                        String id = scanner.nextLine();
                        System.out.print("Full Name: ");
                        String name = scanner.nextLine();
                        System.out.print("Email: ");
                        String email = scanner.nextLine();
                        System.out.print("Department: ");
                        String dept = scanner.nextLine();
                        System.out.print("Semester (1-8): ");
                        int sem = Integer.parseInt(scanner.nextLine().trim());
                        System.out.print("GPA (0.0 - 10.0): ");
                        double gpa = Double.parseDouble(scanner.nextLine().trim());
                        System.out.print("Phone (10 digits): ");
                        String phone = scanner.nextLine();

                        studentService.addStudent(id, name, email, dept, sem, gpa, phone);
                        System.out.println("[SUCCESS] Student registered successfully!");
                        break;

                    case "2":
                        List<Student> byId = studentService.getAllStudentsSortedById();
                        printStudentTable(byId);
                        break;

                    case "3":
                        List<Student> byName = studentService.getAllStudentsSortedByName();
                        printStudentTable(byName);
                        break;

                    case "4":
                        System.out.print("Enter Student ID to search: ");
                        Student found = studentService.searchStudentById(scanner.nextLine());
                        System.out.println("[FOUND] " + found);
                        break;

                    case "5":
                        System.out.print("Enter existing Student ID to update: ");
                        String uId = scanner.nextLine();
                        Student existing = studentService.searchStudentById(uId);
                        System.out.println("Current Record: " + existing);
                        System.out.print("New Full Name: ");
                        String uName = scanner.nextLine();
                        System.out.print("New Email: ");
                        String uEmail = scanner.nextLine();
                        System.out.print("New Department: ");
                        String uDept = scanner.nextLine();
                        System.out.print("New Semester (1-8): ");
                        int uSem = Integer.parseInt(scanner.nextLine().trim());
                        System.out.print("New GPA (0.0 - 10.0): ");
                        double uGpa = Double.parseDouble(scanner.nextLine().trim());
                        System.out.print("New Phone (10 digits): ");
                        String uPhone = scanner.nextLine();

                        studentService.updateStudent(uId, uName, uEmail, uDept, uSem, uGpa, uPhone);
                        System.out.println("[SUCCESS] Student updated successfully!");
                        break;

                    case "6":
                        System.out.print("Enter Student ID to delete: ");
                        studentService.deleteStudent(scanner.nextLine());
                        System.out.println("[SUCCESS] Student deleted successfully!");
                        break;

                    case "7":
                        Map<String, List<Student>> grouped = studentService.groupStudentsByDepartment();
                        grouped.forEach((d, list) -> {
                            System.out.println("\nDepartment: " + d + " (" + list.size() + " students)");
                            list.forEach(s -> System.out.println("  - " + s));
                        });
                        break;

                    case "8":
                        String exportPath = "data/exported_students.csv";
                        studentService.exportStudentsCsv(exportPath);
                        System.out.println("[SUCCESS] Exported students to " + exportPath);
                        break;

                    case "9":
                        System.out.print("Enter CSV path to import (e.g., data/sample_import.csv): ");
                        String importPath = scanner.nextLine().trim();
                        int count = studentService.importStudentsCsv(importPath);
                        System.out.println("[SUCCESS] Imported " + count + " new valid student records.");
                        break;

                    case "0":
                        running = false;
                        System.out.println("Exiting Student Management Module. Goodbye!");
                        break;

                    default:
                        System.out.println("[ERROR] Invalid choice. Please select from 0 to 9.");
                }
            } catch (NumberFormatException e) {
                System.out.println("[INPUT ERROR] Please enter a valid numeric value for Semester or GPA.");
            } catch (InvalidStudentException e) {
                System.out.println("[VALIDATION ERROR] " + e.getMessage());
            } catch (DatabaseOperationException | IOException e) {
                System.out.println("[PERSISTENCE ERROR] " + e.getMessage());
            }
        }
        scanner.close();
    }

    private static void printStudentMenu() {
        System.out.println("\n----------------- STUDENT MANAGEMENT MENU -----------------");
        System.out.println(" 1. Register New Student");
        System.out.println(" 2. Display All Students (Sorted by ID - Comparable)");
        System.out.println(" 3. Display All Students (Sorted by Name - Comparator)");
        System.out.println(" 4. Search Student by ID (O(1) Cache + DAO)");
        System.out.println(" 5. Update Student Details");
        System.out.println(" 6. Delete Student");
        System.out.println(" 7. View Students Grouped by Department (TreeMap)");
        System.out.println(" 8. Export Students to CSV File");
        System.out.println(" 9. Import Students from CSV File (Iterator)");
        System.out.println(" 0. Exit");
        System.out.print("Select option: ");
    }

    private static void printStudentTable(List<Student> students) {
        if (students.isEmpty()) {
            System.out.println("No student records found.");
            return;
        }
        System.out.println("\n-------------------------------------------------------------------------------------------------------------");
        students.forEach(System.out::println);
        System.out.println("-------------------------------------------------------------------------------------------------------------");
    }
}
