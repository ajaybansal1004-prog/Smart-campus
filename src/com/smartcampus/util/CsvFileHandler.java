package com.smartcampus.util;

import com.smartcampus.model.Student;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles CSV File Reading and Writing for Student entities using BufferedReader,
 * BufferedWriter, StringBuilder, and try-with-resources.
 * Scoped specifically to Student Management persistence and import/export.
 */
public class CsvFileHandler {

    private static void ensureParentDir(String filePath) {
        File file = new File(filePath);
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
    }

    // --- STUDENT CSV OPERATIONS ---
    public static void writeStudentsToCsv(String filePath, List<Student> students) throws IOException {
        ensureParentDir(filePath);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("student_id,full_name,email,department,semester,gpa,phone");
            writer.newLine();
            for (Student s : students) {
                StringBuilder sb = new StringBuilder();
                sb.append(s.getId()).append(",")
                  .append(s.getFullName().replace(",", " ")).append(",")
                  .append(s.getEmail()).append(",")
                  .append(s.getDepartment().replace(",", " ")).append(",")
                  .append(s.getSemester()).append(",")
                  .append(s.getGpa()).append(",")
                  .append(s.getPhone());
                writer.write(sb.toString());
                writer.newLine();
            }
        }
    }

    public static List<Student> readStudentsFromCsv(String filePath) throws IOException {
        List<Student> list = new ArrayList<>();
        File file = new File(filePath);
        if (!file.exists()) {
            return list;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = reader.readLine(); // skip header
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(",");
                if (parts.length >= 7) {
                    list.add(new Student(
                            parts[0].trim(),
                            parts[1].trim(),
                            parts[2].trim(),
                            parts[3].trim(),
                            Integer.parseInt(parts[4].trim()),
                            Double.parseDouble(parts[5].trim()),
                            parts[6].trim()
                    ));
                }
            }
        }
        return list;
    }
}
