package com.smartcampus.dao;

import com.smartcampus.exception.DatabaseOperationException;
import com.smartcampus.model.Attendance;
import com.smartcampus.util.CsvFileHandler;
import com.smartcampus.util.DatabaseConnection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Attendance records.
 * Demonstrates JDBC basic transaction handling (setAutoCommit / commit / rollback).
 */
public class AttendanceDAO {
    private static final String CSV_PATH = "data/attendance.csv";

    public AttendanceDAO() {
        initializeSampleDataIfEmpty();
    }

    private void initializeSampleDataIfEmpty() {
        if (!DatabaseConnection.isMysqlAvailable()) {
            try {
                List<Attendance> existing = CsvFileHandler.readAttendanceFromCsv(CSV_PATH);
                if (existing.isEmpty()) {
                    List<Attendance> seed = new ArrayList<>();
                    seed.add(new Attendance(1, "STU101", "25CSE0202", "2026-11-10", "PRESENT"));
                    seed.add(new Attendance(2, "STU102", "25CSE0202", "2026-11-10", "PRESENT"));
                    seed.add(new Attendance(3, "STU103", "25ECE0201", "2026-11-10", "ABSENT"));
                    seed.add(new Attendance(4, "STU104", "25ITE0203", "2026-11-11", "PRESENT"));
                    seed.add(new Attendance(5, "STU101", "25CSE0204", "2026-11-11", "PRESENT"));
                    seed.add(new Attendance(6, "STU102", "25CSE0204", "2026-11-11", "ABSENT"));
                    CsvFileHandler.writeAttendanceToCsv(CSV_PATH, seed);
                }
            } catch (IOException ignored) {
            }
        }
    }

    public void save(Attendance attendance) throws DatabaseOperationException {
        if (DatabaseConnection.isMysqlAvailable()) {
            String sql = "INSERT INTO attendance (student_id, course_code, attendance_date, status) VALUES (?, ?, ?, ?)";
            try (Connection conn = DatabaseConnection.getConnection()) {
                conn.setAutoCommit(false); // Basic transaction management
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, attendance.getStudentId());
                    ps.setString(2, attendance.getCourseCode());
                    ps.setDate(3, Date.valueOf(attendance.getAttendanceDate()));
                    ps.setString(4, attendance.getStatus());
                    ps.executeUpdate();
                    conn.commit();
                } catch (SQLException ex) {
                    conn.rollback();
                    throw ex;
                } finally {
                    conn.setAutoCommit(true);
                }
            } catch (SQLException e) {
                throw new DatabaseOperationException("Error recording attendance in MySQL: " + e.getMessage(), e);
            }
        } else {
            try {
                List<Attendance> list = CsvFileHandler.readAttendanceFromCsv(CSV_PATH);
                int nextId = list.stream().mapToInt(Attendance::getAttendanceId).max().orElse(0) + 1;
                attendance.setAttendanceId(nextId);
                list.add(attendance);
                CsvFileHandler.writeAttendanceToCsv(CSV_PATH, list);
            } catch (IOException e) {
                throw new DatabaseOperationException("Error saving attendance CSV: " + e.getMessage(), e);
            }
        }
    }

    public List<Attendance> findAll() throws DatabaseOperationException {
        if (DatabaseConnection.isMysqlAvailable()) {
            List<Attendance> list = new ArrayList<>();
            String sql = "SELECT * FROM attendance ORDER BY attendance_date DESC, attendance_id DESC";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Attendance(
                            rs.getInt("attendance_id"),
                            rs.getString("student_id"),
                            rs.getString("course_code"),
                            rs.getDate("attendance_date").toString(),
                            rs.getString("status")
                    ));
                }
            } catch (SQLException e) {
                throw new DatabaseOperationException("Error fetching attendance: " + e.getMessage(), e);
            }
            return list;
        } else {
            try {
                return CsvFileHandler.readAttendanceFromCsv(CSV_PATH);
            } catch (IOException e) {
                throw new DatabaseOperationException("Error reading attendance CSV: " + e.getMessage(), e);
            }
        }
    }
}
