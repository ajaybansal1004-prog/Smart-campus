package com.smartcampus.service;

import com.smartcampus.dao.StudentDAO;
import com.smartcampus.exception.DatabaseOperationException;
import com.smartcampus.exception.InvalidStudentException;
import com.smartcampus.model.Student;
import com.smartcampus.util.CsvFileHandler;
import com.smartcampus.util.InputValidator;
import com.smartcampus.util.StudentNameComparator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Service layer for Student Management.
 * Demonstrates HashMap O(1) lookup, TreeMap sorted department grouping,
 * Comparable & Comparator sorting, Iterator traversal, and Stream API.
 */
public class StudentService {
    private final StudentDAO studentDAO;
    private final Map<String, Student> studentCache = new HashMap<>();

    public StudentService() {
        this.studentDAO = new StudentDAO();
        refreshCache();
    }

    public void refreshCache() {
        studentCache.clear();
        try {
            List<Student> all = studentDAO.findAll();
            for (Student s : all) {
                studentCache.put(s.getId().toUpperCase(), s);
            }
        } catch (DatabaseOperationException e) {
            System.err.println("Warning: Could not refresh student cache: " + e.getMessage());
        }
    }

    public void addStudent(String id, String name, String email, String department,
                           int semester, double gpa, String phone)
            throws InvalidStudentException, DatabaseOperationException {
        InputValidator.validateStudentData(id, name, email, department, semester, gpa, phone);
        String normalizedId = id.trim().toUpperCase();
        if (studentCache.containsKey(normalizedId) || studentDAO.findById(normalizedId) != null) {
            throw new InvalidStudentException("Duplicate Student ID detected: " + normalizedId + " already exists.");
        }
        Student student = new Student(normalizedId, name.trim(), email.trim(),
                department.trim(), semester, gpa, phone.trim());
        studentDAO.save(student);
        studentCache.put(normalizedId, student);
    }

    public List<Student> getAllStudentsSortedById() throws DatabaseOperationException {
        List<Student> list = new ArrayList<>(studentDAO.findAll());
        Collections.sort(list); // Uses Comparable<Student>
        return list;
    }

    public List<Student> getAllStudentsSortedByName() throws DatabaseOperationException {
        List<Student> list = new ArrayList<>(studentDAO.findAll());
        list.sort(new StudentNameComparator()); // Uses Comparator<Student>
        return list;
    }

    public Student searchStudentById(String studentId) throws InvalidStudentException, DatabaseOperationException {
        if (studentId == null || studentId.trim().isEmpty()) {
            throw new InvalidStudentException("Search Student ID cannot be empty.");
        }
        String key = studentId.trim().toUpperCase();
        if (studentCache.containsKey(key)) {
            return studentCache.get(key);
        }
        Student found = studentDAO.findById(key);
        if (found == null) {
            throw new InvalidStudentException("No student found with ID: " + key);
        }
        studentCache.put(key, found);
        return found;
    }

    public void updateStudent(String id, String name, String email, String department,
                              int semester, double gpa, String phone)
            throws InvalidStudentException, DatabaseOperationException {
        InputValidator.validateStudentData(id, name, email, department, semester, gpa, phone);
        String normalizedId = id.trim().toUpperCase();
        if (studentDAO.findById(normalizedId) == null) {
            throw new InvalidStudentException("Cannot update. Student ID not found: " + normalizedId);
        }
        Student updated = new Student(normalizedId, name.trim(), email.trim(),
                department.trim(), semester, gpa, phone.trim());
        if (studentDAO.update(updated)) {
            studentCache.put(normalizedId, updated);
        }
    }

    public void deleteStudent(String studentId) throws InvalidStudentException, DatabaseOperationException {
        if (studentId == null || studentId.trim().isEmpty()) {
            throw new InvalidStudentException("Student ID cannot be empty.");
        }
        String normalizedId = studentId.trim().toUpperCase();
        if (!studentDAO.delete(normalizedId)) {
            throw new InvalidStudentException("Cannot delete. Student ID not found: " + normalizedId);
        }
        studentCache.remove(normalizedId);
    }

    public Map<String, List<Student>> groupStudentsByDepartment() throws DatabaseOperationException {
        // TreeMap keeps department names sorted alphabetically
        return studentDAO.findAll().stream()
                .collect(Collectors.groupingBy(Student::getDepartment, TreeMap::new, Collectors.toList()));
    }

    public void exportStudentsCsv(String exportPath) throws DatabaseOperationException, IOException {
        List<Student> students = getAllStudentsSortedById();
        CsvFileHandler.writeStudentsToCsv(exportPath, students);
    }

    public int importStudentsCsv(String importPath) throws IOException, DatabaseOperationException {
        List<Student> imported = CsvFileHandler.readStudentsFromCsv(importPath);
        int addedCount = 0;
        Iterator<Student> iterator = imported.iterator();
        while (iterator.hasNext()) {
            Student s = iterator.next();
            try {
                addStudent(s.getId(), s.getFullName(), s.getEmail(), s.getDepartment(),
                        s.getSemester(), s.getGpa(), s.getPhone());
                addedCount++;
            } catch (InvalidStudentException ignored) {
                // Skip invalid or duplicate records during bulk CSV import
            }
        }
        return addedCount;
    }
}
