package com.smartcampus.dao;

import com.smartcampus.exception.DatabaseOperationException;
import com.smartcampus.model.Faculty;
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
 * Data Access Object for Faculty records.
 */
public class FacultyDAO implements CrudRepository<Faculty, String> {
    private static final String CSV_PATH = "data/faculty.csv";

    public FacultyDAO() {
        initializeSampleDataIfEmpty();
    }

    private void initializeSampleDataIfEmpty() {
        if (!DatabaseConnection.isMysqlAvailable()) {
            try {
                List<Faculty> existing = CsvFileHandler.readFacultyFromCsv(CSV_PATH);
                if (existing.isEmpty()) {
                    List<Faculty> seed = new ArrayList<>();
                    seed.add(new Faculty("FAC201", "Dr. Meera Kulkarni", "meera.k@university.edu", "Computer Science", "Associate Professor", "Object-Oriented Systems & Java"));
                    seed.add(new Faculty("FAC202", "Prof. Rajeshwari Rao", "rajeshwari.r@university.edu", "Computer Science", "Assistant Professor", "Database Management Systems"));
                    seed.add(new Faculty("FAC203", "Dr. Vikramaditya Sen", "vikram.s@university.edu", "Electronics", "Professor", "Embedded Systems"));
                    seed.add(new Faculty("FAC204", "Prof. Neha Deshmukh", "neha.d@university.edu", "Information Technology", "Assistant Professor", "Data Structures & Algorithms"));
                    CsvFileHandler.writeFacultyToCsv(CSV_PATH, seed);
                }
            } catch (IOException ignored) {
            }
        }
    }

    @Override
    public void save(Faculty faculty) throws DatabaseOperationException {
        if (DatabaseConnection.isMysqlAvailable()) {
            String sql = "INSERT INTO faculty (faculty_id, full_name, email, department, designation, specialization) VALUES (?, ?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, faculty.getId());
                ps.setString(2, faculty.getFullName());
                ps.setString(3, faculty.getEmail());
                ps.setString(4, faculty.getDepartment());
                ps.setString(5, faculty.getDesignation());
                ps.setString(6, faculty.getSpecialization());
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new DatabaseOperationException("Error saving faculty to MySQL: " + e.getMessage(), e);
            }
        } else {
            try {
                List<Faculty> list = CsvFileHandler.readFacultyFromCsv(CSV_PATH);
                list.add(faculty);
                CsvFileHandler.writeFacultyToCsv(CSV_PATH, list);
            } catch (IOException e) {
                throw new DatabaseOperationException("Error saving faculty to CSV: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public Faculty findById(String facultyId) throws DatabaseOperationException {
        if (DatabaseConnection.isMysqlAvailable()) {
            String sql = "SELECT * FROM faculty WHERE faculty_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, facultyId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapRow(rs);
                    }
                }
            } catch (SQLException e) {
                throw new DatabaseOperationException("Error finding faculty in MySQL: " + e.getMessage(), e);
            }
            return null;
        } else {
            try {
                for (Faculty f : CsvFileHandler.readFacultyFromCsv(CSV_PATH)) {
                    if (f.getId().equalsIgnoreCase(facultyId)) return f;
                }
                return null;
            } catch (IOException e) {
                throw new DatabaseOperationException("Error reading faculty CSV: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public List<Faculty> findAll() throws DatabaseOperationException {
        if (DatabaseConnection.isMysqlAvailable()) {
            List<Faculty> list = new ArrayList<>();
            String sql = "SELECT * FROM faculty ORDER BY faculty_id";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            } catch (SQLException e) {
                throw new DatabaseOperationException("Error listing faculty: " + e.getMessage(), e);
            }
            return list;
        } else {
            try {
                return CsvFileHandler.readFacultyFromCsv(CSV_PATH);
            } catch (IOException e) {
                throw new DatabaseOperationException("Error reading faculty CSV: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public boolean update(Faculty faculty) throws DatabaseOperationException {
        if (DatabaseConnection.isMysqlAvailable()) {
            String sql = "UPDATE faculty SET full_name=?, email=?, department=?, designation=?, specialization=? WHERE faculty_id=?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, faculty.getFullName());
                ps.setString(2, faculty.getEmail());
                ps.setString(3, faculty.getDepartment());
                ps.setString(4, faculty.getDesignation());
                ps.setString(5, faculty.getSpecialization());
                ps.setString(6, faculty.getId());
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                throw new DatabaseOperationException("Error updating faculty: " + e.getMessage(), e);
            }
        } else {
            try {
                List<Faculty> list = CsvFileHandler.readFacultyFromCsv(CSV_PATH);
                boolean updated = false;
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).getId().equalsIgnoreCase(faculty.getId())) {
                        list.set(i, faculty);
                        updated = true;
                        break;
                    }
                }
                if (updated) CsvFileHandler.writeFacultyToCsv(CSV_PATH, list);
                return updated;
            } catch (IOException e) {
                throw new DatabaseOperationException("Error updating faculty CSV: " + e.getMessage(), e);
            }
        }
    }

    @Override
    public boolean delete(String facultyId) throws DatabaseOperationException {
        if (DatabaseConnection.isMysqlAvailable()) {
            String sql = "DELETE FROM faculty WHERE faculty_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, facultyId);
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                throw new DatabaseOperationException("Error deleting faculty (may be assigned to courses): " + e.getMessage(), e);
            }
        } else {
            try {
                List<Faculty> list = CsvFileHandler.readFacultyFromCsv(CSV_PATH);
                boolean removed = list.removeIf(f -> f.getId().equalsIgnoreCase(facultyId));
                if (removed) CsvFileHandler.writeFacultyToCsv(CSV_PATH, list);
                return removed;
            } catch (IOException e) {
                throw new DatabaseOperationException("Error deleting faculty CSV: " + e.getMessage(), e);
            }
        }
    }

    private Faculty mapRow(ResultSet rs) throws SQLException {
        return new Faculty(
                rs.getString("faculty_id"),
                rs.getString("full_name"),
                rs.getString("email"),
                rs.getString("department"),
                rs.getString("designation"),
                rs.getString("specialization")
        );
    }
}
