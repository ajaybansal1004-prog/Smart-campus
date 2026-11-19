package com.smartcampus.dao;

import com.smartcampus.exception.DatabaseOperationException;
import com.smartcampus.model.Student;
import com.smartcampus.util.CsvFileHandler;
import com.smartcampus.util.DatabaseConnection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Student records.
 * Uses JDBC PreparedStatement + ResultSet when MySQL is active,
 * and seamlessly falls back to CSV File Handling when offline.
 */
public class StudentDAO implements CrudRepository<Student, String> {
    private static final String CSV_PATH = "data/students.csv";

    public StudentDAO() {
        initializeSampleDataIfEmpty();
    }

    private void initializeSampleDataIfEmpty() {
        if (!DatabaseConnection.isMysqlAvailable()) {
            
            try {
                List<Student> existing = CsvFileHandler.readStudentsFromCsv(CSV_PATH);
                if (existing.isEmpty()) {
                    List<Student> seed = new ArrayList<>();
                    seed.add(new Student("STU101", "Aarav Sharma", "aarav.sharma@university.edu", "Computer Science", 3, 8.95, "9876543210"));
                    seed.add(new Student("STU102", "Diya Nair", "diya.nair@university.edu", "Computer Science", 3, 9.20, "9876543211"));
                    seed.add(new Student("STU103", "Rohan Verma", "rohan.verma@university.edu", "Electronics", 3, 8.15, "9876543212"));
                    seed.add(new Student("STU104", "Ananya Iyer", "ananya.iyer@university.edu", "Information Technology", 3, 9.40, "9876543213"));
                    seed.add(new Student("STU105", "Kabir Mehta", "kabir.mehta@university.edu", "Mechanical", 5, 7.85, "9876543214"));
                    CsvFileHandler.writeStudentsToCsv(CSV_PATH, seed);
                }
            } catch (IOException ignored) {
            }
        }
    }

    @Override
    public void save(Student student) throws DatabaseOperationException {
        if (DatabaseConnection.isMysqlAvailable()) {
            String sql = "INSERT INTO students (student_id, full_name, email, department, semester, gpa, phone) VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, student.getId());
                ps.setString(2, student.getFullName());
                ps.setString(3, student.getEmail());
                ps.setString(4, student.getDepartment());
                ps.setInt(5, student.getSemester());
                ps.setDouble(6, student.getGpa());
                ps.setString(7, student.getPhone());
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new DatabaseOperationException("Failed to insert student into MySQL: " + e.getMessage(), e);
            }
        } else {
            try {
                List<Student> students = CsvFileHandler.readStudentsFromCsv(CSV_PATH);
                students.add(student);
                CsvFileHandler.writeStudentsToCsv(CSV_PATH, students);
            } catch (IOException e) {
                throw new DatabaseOperationException("Failed to save student to CSV: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public Student findById(String studentId) throws DatabaseOperationException {
        if (DatabaseConnection.isMysqlAvailable()) {
            String sql = "SELECT * FROM students WHERE student_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, studentId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapRow(rs);
                    }
                }
            } catch (SQLException e) {
                throw new DatabaseOperationException("Error searching student in MySQL: " + e.getMessage(), e);
            }
            return null;
        } else {
            try {
                for (Student s : CsvFileHandler.readStudentsFromCsv(CSV_PATH)) {
                    if (s.getId().equalsIgnoreCase(studentId)) {
                        return s;
                    }
                }
                return null;
            } catch (IOException e) {
                throw new DatabaseOperationException("Error reading students CSV: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public List<Student> findAll() throws DatabaseOperationException {
        if (DatabaseConnection.isMysqlAvailable()) {
            List<Student> list = new ArrayList<>();
            String sql = "SELECT * FROM students ORDER BY student_id";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            } catch (SQLException e) {
                throw new DatabaseOperationException("Error fetching all students from MySQL: " + e.getMessage(), e);
            }
            return list;
        } else {
            try {
                return CsvFileHandler.readStudentsFromCsv(CSV_PATH);
            } catch (IOException e) {
                throw new DatabaseOperationException("Error reading students CSV: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public boolean update(Student student) throws DatabaseOperationException {
        if (DatabaseConnection.isMysqlAvailable()) {
            String sql = "UPDATE students SET full_name=?, email=?, department=?, semester=?, gpa=?, phone=? WHERE student_id=?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, student.getFullName());
                ps.setString(2, student.getEmail());
                ps.setString(3, student.getDepartment());
                ps.setInt(4, student.getSemester());
                ps.setDouble(5, student.getGpa());
                ps.setString(6, student.getPhone());
                ps.setString(7, student.getId());
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                throw new DatabaseOperationException("Error updating student in MySQL: " + e.getMessage(), e);
            }
        } else {
            try {
                List<Student> list = CsvFileHandler.readStudentsFromCsv(CSV_PATH);
                boolean updated = false;
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).getId().equalsIgnoreCase(student.getId())) {
                        list.set(i, student);
                        updated = true;
                        break;
                    }
                }
                if (updated) {
                    CsvFileHandler.writeStudentsToCsv(CSV_PATH, list);
                }
                return updated;
            } catch (IOException e) {
                throw new DatabaseOperationException("Error updating student in CSV: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public boolean delete(String studentId) throws DatabaseOperationException {
        if (DatabaseConnection.isMysqlAvailable()) {
            String sql = "DELETE FROM students WHERE student_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, studentId);
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                throw new DatabaseOperationException("Error deleting student in MySQL: " + e.getMessage(), e);
            }
        } else {
            try {
                List<Student> list = CsvFileHandler.readStudentsFromCsv(CSV_PATH);
                boolean removed = list.removeIf(s -> s.getId().equalsIgnoreCase(studentId));
                if (removed) {
                    CsvFileHandler.writeStudentsToCsv(CSV_PATH, list);
                }
                return removed;
            } catch (IOException e) {
                throw new DatabaseOperationException("Error deleting student in CSV: " + e.getMessage(), e);
            }
        }
    }

    private Student mapRow(ResultSet rs) throws SQLException {
        return new Student(
                rs.getString("student_id"),
                rs.getString("full_name"),
                rs.getString("email"),
                rs.getString("department"),
                rs.getInt("semester"),
                rs.getDouble("gpa"),
                rs.getString("phone")
        );
    }
}
