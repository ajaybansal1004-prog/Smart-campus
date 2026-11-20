package com.smartcampus.dao;

import com.smartcampus.exception.DatabaseOperationException;
import com.smartcampus.model.Course;
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
 * Data Access Object for Course records.
 */
public class CourseDAO implements CrudRepository<Course, String> {
    private static final String CSV_PATH = "data/courses.csv";

    public CourseDAO() {
        initializeSampleDataIfEmpty();
    }

    private void initializeSampleDataIfEmpty() {
        if (!DatabaseConnection.isMysqlAvailable()) {
            try {
                List<Course> existing = CsvFileHandler.readCoursesFromCsv(CSV_PATH);
                if (existing.isEmpty()) {
                    List<Course> seed = new ArrayList<>();
                    seed.add(new Course("25CSE0202", "Programming in Java", "Computer Science", 4, "FAC201"));
                    seed.add(new Course("25CSE0204", "Database Management Systems", "Computer Science", 4, "FAC202"));
                    seed.add(new Course("25ECE0201", "Digital Logic Design", "Electronics", 3, "FAC203"));
                    seed.add(new Course("25ITE0203", "Data Structures in Practice", "Information Technology", 4, "FAC204"));
                    CsvFileHandler.writeCoursesToCsv(CSV_PATH, seed);
                }
            } catch (IOException ignored) {
            }
        }
    }

    @Override
    public void save(Course course) throws DatabaseOperationException {
        if (DatabaseConnection.isMysqlAvailable()) {
            String sql = "INSERT INTO courses (course_code, course_name, department, credits, faculty_id) VALUES (?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, course.getCourseCode());
                ps.setString(2, course.getCourseName());
                ps.setString(3, course.getDepartment());
                ps.setInt(4, course.getCredits());
                ps.setString(5, course.getFacultyId());
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new DatabaseOperationException("Error inserting course: " + e.getMessage(), e);
            }
        } else {
            try {
                List<Course> list = CsvFileHandler.readCoursesFromCsv(CSV_PATH);
                list.add(course);
                CsvFileHandler.writeCoursesToCsv(CSV_PATH, list);
            } catch (IOException e) {
                throw new DatabaseOperationException("Error saving course CSV: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public Course findById(String courseCode) throws DatabaseOperationException {
        if (DatabaseConnection.isMysqlAvailable()) {
            String sql = "SELECT * FROM courses WHERE course_code = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, courseCode);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return mapRow(rs);
                }
            } catch (SQLException e) {
                throw new DatabaseOperationException("Error searching course: " + e.getMessage(), e);
            }
            return null;
        } else {
            try {
                for (Course c : CsvFileHandler.readCoursesFromCsv(CSV_PATH)) {
                    if (c.getCourseCode().equalsIgnoreCase(courseCode)) return c;
                }
                return null;
            } catch (IOException e) {
                throw new DatabaseOperationException("Error reading courses CSV: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public List<Course> findAll() throws DatabaseOperationException {
        if (DatabaseConnection.isMysqlAvailable()) {
            List<Course> list = new ArrayList<>();
            String sql = "SELECT * FROM courses ORDER BY course_code";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            } catch (SQLException e) {
                throw new DatabaseOperationException("Error listing courses: " + e.getMessage(), e);
            }
            return list;
        } else {
            try {
                return CsvFileHandler.readCoursesFromCsv(CSV_PATH);
            } catch (IOException e) {
                throw new DatabaseOperationException("Error reading courses CSV: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public boolean update(Course course) throws DatabaseOperationException {
        if (DatabaseConnection.isMysqlAvailable()) {
            String sql = "UPDATE courses SET course_name=?, department=?, credits=?, faculty_id=? WHERE course_code=?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, course.getCourseName());
                ps.setString(2, course.getDepartment());
                ps.setInt(3, course.getCredits());
                ps.setString(4, course.getFacultyId());
                ps.setString(5, course.getCourseCode());
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                throw new DatabaseOperationException("Error updating course: " + e.getMessage(), e);
            }
        } else {
            try {
                List<Course> list = CsvFileHandler.readCoursesFromCsv(CSV_PATH);
                boolean updated = false;
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).getCourseCode().equalsIgnoreCase(course.getCourseCode())) {
                        list.set(i, course);
                        updated = true;
                        break;
                    }
                }
                if (updated) CsvFileHandler.writeCoursesToCsv(CSV_PATH, list);
                return updated;
            } catch (IOException e) {
                throw new DatabaseOperationException("Error updating course CSV: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public boolean delete(String courseCode) throws DatabaseOperationException {
        if (DatabaseConnection.isMysqlAvailable()) {
            String sql = "DELETE FROM courses WHERE course_code = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, courseCode);
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                throw new DatabaseOperationException("Error deleting course: " + e.getMessage(), e);
            }
        } else {
            try {
                List<Course> list = CsvFileHandler.readCoursesFromCsv(CSV_PATH);
                boolean removed = list.removeIf(c -> c.getCourseCode().equalsIgnoreCase(courseCode));
                if (removed) CsvFileHandler.writeCoursesToCsv(CSV_PATH, list);
                return removed;
            } catch (IOException e) {
                throw new DatabaseOperationException("Error deleting course CSV: " + e.getMessage(), e);
            }
        }
    }

    private Course mapRow(ResultSet rs) throws SQLException {
        return new Course(
                rs.getString("course_code"),
                rs.getString("course_name"),
                rs.getString("department"),
                rs.getInt("credits"),
                rs.getString("faculty_id")
        );
    }
}
