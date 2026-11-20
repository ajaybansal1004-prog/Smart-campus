package com.smartcampus.dao;

import com.smartcampus.exception.DatabaseOperationException;
import com.smartcampus.model.CampusEvent;
import com.smartcampus.util.CsvFileHandler;
import com.smartcampus.util.DatabaseConnection;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Data Access Object for Campus Events.
 * Uses LinkedList for chronological event schedule management.
 */
public class EventDAO {
    private static final String CSV_PATH = "data/events.csv";

    public EventDAO() {
        initializeSampleDataIfEmpty();
    }

    private void initializeSampleDataIfEmpty() {
        if (!DatabaseConnection.isMysqlAvailable()) {
            try {
                List<CampusEvent> existing = CsvFileHandler.readEventsFromCsv(CSV_PATH);
                if (existing.isEmpty()) {
                    List<CampusEvent> seed = new ArrayList<>();
                    seed.add(new CampusEvent("EVT301", "Annual Tech Symposium - Innovate 2026", "2026-11-25", "Main Auditorium", "CSE Department", "Inter-college coding hackathon and project showcase."));
                    seed.add(new CampusEvent("EVT302", "Database & JDBC Hands-on Workshop", "2026-12-02", "Computer Lab 3", "ACM Student Chapter", "Practical workshop on relational schema design and JDBC."));
                    seed.add(new CampusEvent("EVT303", "Winter Cultural & Sports Fest", "2026-12-15", "University Sports Complex", "Student Council", "Annual track events and evening cultural performances."));
                    CsvFileHandler.writeEventsToCsv(CSV_PATH, seed);
                }
            } catch (IOException ignored) {
            }
        }
    }

    public void save(CampusEvent event) throws DatabaseOperationException {
        if (DatabaseConnection.isMysqlAvailable()) {
            String sql = "INSERT INTO campus_events (event_id, event_name, event_date, venue, organizer, description) VALUES (?, ?, ?, ?, ?, ?)";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, event.getEventId());
                ps.setString(2, event.getEventName());
                ps.setDate(3, Date.valueOf(event.getEventDate()));
                ps.setString(4, event.getVenue());
                ps.setString(5, event.getOrganizer());
                ps.setString(6, event.getDescription());
                ps.executeUpdate();
            } catch (SQLException e) {
                throw new DatabaseOperationException("Error adding campus event: " + e.getMessage(), e);
            }
        } else {
            try {
                List<CampusEvent> list = CsvFileHandler.readEventsFromCsv(CSV_PATH);
                list.add(event);
                CsvFileHandler.writeEventsToCsv(CSV_PATH, list);
            } catch (IOException e) {
                throw new DatabaseOperationException("Error saving event CSV: " + e.getMessage(), e);
            }
        }
    }

    public LinkedList<CampusEvent> findAll() throws DatabaseOperationException {
        LinkedList<CampusEvent> events = new LinkedList<>();
        if (DatabaseConnection.isMysqlAvailable()) {
            String sql = "SELECT * FROM campus_events ORDER BY event_date ASC";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    events.add(new CampusEvent(
                            rs.getString("event_id"),
                            rs.getString("event_name"),
                            rs.getDate("event_date").toString(),
                            rs.getString("venue"),
                            rs.getString("organizer"),
                            rs.getString("description")
                    ));
                }
            } catch (SQLException e) {
                throw new DatabaseOperationException("Error loading events: " + e.getMessage(), e);
            }
        } else {
            try {
                events.addAll(CsvFileHandler.readEventsFromCsv(CSV_PATH));
            } catch (IOException e) {
                throw new DatabaseOperationException("Error reading events CSV: " + e.getMessage(), e);
            }
        }
        return events;
    }

    public boolean delete(String eventId) throws DatabaseOperationException {
        if (DatabaseConnection.isMysqlAvailable()) {
            String sql = "DELETE FROM campus_events WHERE event_id = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, eventId);
                return ps.executeUpdate() > 0;
            } catch (SQLException e) {
                throw new DatabaseOperationException("Error deleting event: " + e.getMessage(), e);
            }
        } else {
            try {
                List<CampusEvent> list = CsvFileHandler.readEventsFromCsv(CSV_PATH);
                boolean removed = list.removeIf(ev -> ev.getEventId().equalsIgnoreCase(eventId));
                if (removed) CsvFileHandler.writeEventsToCsv(CSV_PATH, list);
                return removed;
            } catch (IOException e) {
                throw new DatabaseOperationException("Error deleting event CSV: " + e.getMessage(), e);
            }
        }
    }
}
