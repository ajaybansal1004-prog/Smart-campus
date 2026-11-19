# Member 1 — Student Management Module
**Project:** Smart Campus Management System (Course: Programming in Java - 25CSE0202)  
**Assigned Member:** Member 1  
**Module Focus:** Student Lifecycle Management, Student Persistence, In-Memory Caching, Custom Sorting, and CSV Import/Export.

---

## 1. Assigned Module Overview
The **Student Management Module** forms the foundation of the Smart Campus Management System. It is responsible for handling all operations, persistence, validation, and domain modeling related to students enrolled at the university.

Member 1 owns the complete Student subsystem, including:
- Domain modeling using Java OOP concepts (inheritance from `Person`, encapsulation, constructors, method overriding).
- Custom exception handling for student business rules (`InvalidStudentException`).
- Data access object layer (`StudentDAO`) supporting dual-mode persistence (MySQL JDBC with `PreparedStatement` and local CSV fallback).
- In-memory performance optimization via `HashMap` ($O(1)$ lookups) and `TreeMap` (alphabetical department grouping).
- Natural ordering with `Comparable<Student>` (ID sorting) and custom ordering with `Comparator<Student>` (`StudentNameComparator`).
- Bulk student operations via CSV import and export using `BufferedReader`, `BufferedWriter`, and `Iterator`.
- Dedicated command-line interface (`StudentManagementApp`) for independent testing and execution.

---

## 2. List of Assigned Files & Explanations

### A. Core Module Files (Owned by Member 1)
| File Path | Component | Description & Key Responsibilities |
| :--- | :--- | :--- |
| `src/com/smartcampus/model/Person.java` | Base Model | Abstract base class encapsulating campus identity (`id`, `fullName`, `email`, `department`) and defining abstract method `getRoleSummary()` for runtime polymorphism. |
| `src/com/smartcampus/model/Student.java` | Core Entity | Subclass of `Person` representing university students. Adds `semester`, `gpa`, and `phone`. Implements `Comparable<Student>` for natural ordering by Student ID and overrides `getRoleSummary()`. |
| `src/com/smartcampus/exception/InvalidStudentException.java` | Custom Exception | Checked custom exception thrown when student validation checks fail (e.g., malformed ID, invalid email, out-of-range GPA/semester). |
| `src/com/smartcampus/dao/StudentDAO.java` | Data Access Object | Implements `CrudRepository<Student, String>`. Provides CRUD operations against MySQL via JDBC `PreparedStatement` / `ResultSet`, and seamlessly falls back to local CSV storage (`data/students.csv`). |
| `src/com/smartcampus/service/StudentService.java` | Business Service | Implements student business workflows. Features an in-memory `HashMap` cache for fast $O(1)$ lookups, `TreeMap` for department grouping, sorting routines, and CSV bulk import/export. |
| `src/com/smartcampus/util/InputValidator.java` | Validation Utility | Enforces strict validation on student ID format, name length, email structure, semester range ($1-8$), GPA bounds ($0.0-10.0$), and 10-digit phone number regex. |
| `src/com/smartcampus/util/StudentNameComparator.java` | Sorting Utility | Implements `Comparator<Student>` to sort student collections alphabetically by full name (case-insensitive). |
| `src/com/smartcampus/main/StudentManagementApp.java` | Dedicated Runner | Standalone interactive CLI application extracting all student-specific menus and features, allowing Member 1's module to be tested independently. |
| `database/student_schema.sql` | Database DDL | MySQL schema script containing the `students` table definition, primary key constraints, check constraints, and initial seed data. |
| `data/sample_import.csv` | Test Resource | Sample CSV data for verifying bulk student import via `StudentService.importStudentsCsv()`. |

### B. Shared Dependencies Included for Independent Execution
To ensure Member 1's module can compile and run independently without requiring external libraries or unfinished peer modules, the following minimal shared dependencies are included:
| File Path | Role | Reason for Inclusion |
| :--- | :--- | :--- |
| `src/com/smartcampus/dao/CrudRepository.java` | Generic Interface | Standard generic CRUD contract (`save`, `findById`, `findAll`, `update`, `delete`) implemented directly by `StudentDAO`. |
| `src/com/smartcampus/exception/DatabaseOperationException.java` | Shared Exception | Checked exception wrapping low-level SQL or I/O persistence failures thrown across `StudentDAO` and `StudentService`. |
| `src/com/smartcampus/util/DatabaseConnection.java` | Database Utility | Manages JDBC connection pooling/testing to MySQL (`localhost:3306/smart_campus_db`). Detects if MySQL is live or falls back to CSV. |
| `src/com/smartcampus/util/CsvFileHandler.java` | Persistence Engine | Scoped specifically to student CSV reading/writing (`readStudentsFromCsv`, `writeStudentsToCsv`) using `BufferedReader` and `BufferedWriter`. |

---

## 3. Features Handled by Member 1
1. **Student Registration:** Validates input parameters and inserts new records into MySQL or CSV storage. Automatically populates in-memory cache.
2. **Student Search ($O(1)$ Lookup):** Searches by Student ID in $O(1)$ time via `HashMap<String, Student>`. If absent from cache, queries the database.
3. **Natural Sorting by Student ID:** Sorts student lists using `Comparable<Student>` (`compareTo`).
4. **Custom Sorting by Full Name:** Sorts student lists alphabetically using `StudentNameComparator` (`Comparator<Student>`).
5. **Student Updating:** Updates existing student attributes with validation checks and synchronizes both cache and persistent storage.
6. **Student Deletion:** Removes students by ID from database/CSV and evicts the entry from the cache.
7. **Department Grouping:** Aggregates students by department using Java 8 Streams (`Collectors.groupingBy`) into a sorted `TreeMap<String, List<Student>>`.
8. **Bulk CSV Export:** Exports all student records sorted by ID to a designated CSV file using `BufferedWriter`.
9. **Bulk CSV Import:** Parses input CSV files line-by-line using `BufferedReader` and `Iterator<Student>`, skipping duplicates or invalid entries.

---

## 4. Dependencies on Other Modules
- **Dependency on Member 3 (Database & Infrastructure):**  
  In the unified project, `DatabaseConnection.java`, `CrudRepository.java`, and centralized database schema management are maintained by Member 3. Member 1 connects to the common `smart_campus_db` database using the shared connection utility.
- **Consumer Modules (Member 2 & Member 3):**  
  - Member 2's `CampusAcademicService` calls `StudentDAO.findById()` to verify student existence before logging attendance records.
  - Member 3's `ReportService` queries `StudentDAO.findAll()` to generate campus dashboard statistics, honors student reports, and polymorphic directory listings.

---

## 5. How This Module Fits into the Complete Project
In the complete Smart Campus Management System, the student is the primary actor. Courses are taken by students, attendance is marked against student IDs, and reports evaluate student academic metrics. Member 1 provides the core Student domain model and data access services upon which Member 2 (Attendance) and Member 3 (Campus Analytics) build.

---

## 6. How to Compile and Run Member 1 Code

### Prerequisites
- JDK 8 or higher (JDK 17/21 recommended).
- Optional: MySQL 8.0 running on `localhost:3306` with database `smart_campus_db` (otherwise, local CSV mode is automatically activated).

### Step-by-Step Instructions
```bash
# 1. Navigate to Member 1's folder
cd Team_Division/Member_1_Student_Management

# 2. Compile all Java sources
mkdir -p out
javac -d out src/com/smartcampus/model/*.java \
             src/com/smartcampus/exception/*.java \
             src/com/smartcampus/util/*.java \
             src/com/smartcampus/dao/*.java \
             src/com/smartcampus/service/*.java \
             src/com/smartcampus/main/*.java

# 3. Run the standalone Student Management Application
java -cp out com.smartcampus.main.StudentManagementApp
```

### Testing the Module
- Select Option `1` to add a student (e.g., ID: `STU108`, Name: `Kavya Patel`, GPA: `9.15`).
- Select Option `2` to view students sorted by Student ID.
- Select Option `3` to view students sorted alphabetically by Name.
- Select Option `4` to search for `STU101`.
- Select Option `7` to view students grouped by department.
- Select Option `9` and enter `data/sample_import.csv` to test bulk CSV import.
