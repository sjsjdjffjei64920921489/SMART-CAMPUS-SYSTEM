import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class Database {

        private static final String URL = System.getenv().getOrDefault("SMARTCAMPUS_DB_URL",
            "jdbc:mysql://localhost:3306/smartcampus?useSSL=false&serverTimezone=UTC");
        private static final String USER = System.getenv().getOrDefault("SMARTCAMPUS_DB_USER", "root");
        private static final String PASSWORD = System.getenv().getOrDefault("SMARTCAMPUS_DB_PASSWORD", "");

    private Database() {
    }

    static Connection connect() throws SQLException {
        if (PASSWORD.isBlank()) {
            throw new SQLException("SMARTCAMPUS_DB_PASSWORD is not set. Set it before starting the application.");
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    static void initialize() throws SQLException {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS users ("
                    + "userId VARCHAR(20) PRIMARY KEY, username VARCHAR(50) UNIQUE NOT NULL, "
                    + "password VARCHAR(100) NOT NULL, role VARCHAR(20) NOT NULL, studentId VARCHAR(20), "
                    + "lecturerId VARCHAR(20))");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS departments ("
                    + "departmentId INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100) UNIQUE NOT NULL)");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS programs ("
                    + "programId INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100) UNIQUE NOT NULL, "
                    + "department VARCHAR(100) NOT NULL)");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS students ("
                    + "studentId VARCHAR(20) PRIMARY KEY, name VARCHAR(100) NOT NULL, "
                    + "program VARCHAR(100) NOT NULL, department VARCHAR(100) NOT NULL)");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS lecturers ("
                    + "lecturerId VARCHAR(20) PRIMARY KEY, name VARCHAR(100) NOT NULL, "
                    + "department VARCHAR(100) NOT NULL)");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS courses ("
                    + "courseCode VARCHAR(20) PRIMARY KEY, courseName VARCHAR(100) NOT NULL, "
                    + "creditUnits INT NOT NULL, lecturerId VARCHAR(20))");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS registrations ("
                    + "studentId VARCHAR(20), courseCode VARCHAR(20), courseName VARCHAR(100) NOT NULL, "
                    + "PRIMARY KEY (studentId, courseCode))");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS attendance ("
                    + "studentId VARCHAR(20), courseCode VARCHAR(20), classesAttended INT NOT NULL, "
                    + "totalClasses INT NOT NULL, PRIMARY KEY (studentId, courseCode))");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS results ("
                    + "studentId VARCHAR(20), courseCode VARCHAR(20), courseworkMark DOUBLE NOT NULL, "
                    + "examMark DOUBLE NOT NULL, totalMark DOUBLE NOT NULL, grade VARCHAR(5) NOT NULL, "
                    + "PRIMARY KEY (studentId, courseCode))");
                    statement.executeUpdate("CREATE TABLE IF NOT EXISTS academicYears ("
                        + "academicYearId INT AUTO_INCREMENT PRIMARY KEY, yearName VARCHAR(20) UNIQUE NOT NULL)");
                    statement.executeUpdate("CREATE TABLE IF NOT EXISTS semesters ("
                        + "semesterId INT AUTO_INCREMENT PRIMARY KEY, semesterName VARCHAR(30) NOT NULL, "
                        + "academicYear VARCHAR(20) NOT NULL, UNIQUE (semesterName, academicYear))");
                    statement.executeUpdate("CREATE TABLE IF NOT EXISTS fees ("
                        + "feeId INT AUTO_INCREMENT PRIMARY KEY, studentId VARCHAR(20) NOT NULL, "
                        + "academicYear VARCHAR(20) NOT NULL, semesterName VARCHAR(30) NOT NULL, "
                        + "amount DOUBLE NOT NULL, status VARCHAR(20) NOT NULL DEFAULT 'UNPAID')");
                    ensureColumn(connection, "users", "lecturerId", "VARCHAR(20)");
        }
        seedDefaults();
    }

    private static void seedDefaults() throws SQLException {
        String sql = "INSERT IGNORE INTO users (userId, username, password, role, studentId, lecturerId) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(sql)) {
            addUser(statement, "U001", "admin", "admin123", "ADMIN", null, null);
            addUser(statement, "U002", "lecturer", "lecturer123", "LECTURER", null, "LEC001");
            addUser(statement, "U003", "student", "student123", "STUDENT", "STU001", null);
            statement.executeBatch();
        }
        addStudent("STU001", "Demo Student", "Bachelor of Computer Science", "Computing");
        addLecturer("LEC001", "Demo Lecturer", "Computing");
        addCourse("CSC101", "Object Oriented Programming", 3, "LEC001");
    }

    private static void addUser(PreparedStatement statement, String id, String username,
                                String password, String role, String studentId, String lecturerId) throws SQLException {
        statement.setString(1, id);
        statement.setString(2, username);
        statement.setString(3, hashPassword(password));
        statement.setString(4, role);
        statement.setString(5, studentId);
        statement.setString(6, lecturerId);
        statement.addBatch();
    }

    static UserAccount authenticate(String username, String password) throws SQLException {
        String sql = "SELECT userId, username, password, role, studentId, lecturerId FROM users WHERE username = ?";
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next() && passwordMatches(password, result.getString("password"))) {
                    if (!result.getString("password").equals(hashPassword(password))) {
                        executeUpdate("UPDATE users SET password = ? WHERE userId = ?",
                                hashPassword(password), result.getString("userId"));
                }
                    return new UserAccount(result.getString("userId"), result.getString("username"),
                            result.getString("role"), result.getString("studentId"), result.getString("lecturerId"));
                }
            }
        }
        return null;
    }

    static void addStudent(String studentId, String name, String program, String department) throws SQLException {
        executeUpdate("INSERT IGNORE INTO students (studentId, name, program, department) VALUES (?, ?, ?, ?)",
                studentId, name, program, department);
    }

    static void addLecturer(String lecturerId, String name, String department) throws SQLException {
        executeUpdate("INSERT IGNORE INTO lecturers (lecturerId, name, department) VALUES (?, ?, ?)",
                lecturerId, name, department);
    }

    static void addCourse(String courseCode, String courseName, int creditUnits, String lecturerId) throws SQLException {
        executeUpdate("INSERT IGNORE INTO courses (courseCode, courseName, creditUnits, lecturerId) VALUES (?, ?, ?, ?)",
                courseCode, courseName, creditUnits, lecturerId);
    }

    static void addDepartment(String name) throws SQLException {
        executeUpdate("INSERT INTO departments (name) VALUES (?)", name);
    }

    static void addProgram(String name, String department) throws SQLException {
        executeUpdate("INSERT INTO programs (name, department) VALUES (?, ?)", name, department);
    }

    static void addAcademicYear(String yearName) throws SQLException {
        executeUpdate("INSERT INTO academicYears (yearName) VALUES (?)", yearName);
    }

    static void addSemester(String semesterName, String academicYear) throws SQLException {
        executeUpdate("INSERT INTO semesters (semesterName, academicYear) VALUES (?, ?)", semesterName, academicYear);
    }

    static void addUser(String userId, String username, String password, String role,
                        String studentId, String lecturerId) throws SQLException {
        if (!role.equals("ADMIN") && !role.equals("LECTURER") && !role.equals("STUDENT")) {
            throw new IllegalArgumentException("Role must be ADMIN, LECTURER, or STUDENT.");
        }
        executeUpdate("INSERT INTO users (userId, username, password, role, studentId, lecturerId) "
                + "VALUES (?, ?, ?, ?, ?, ?)", userId, username, hashPassword(password), role, studentId, lecturerId);
    }

    static void recordFee(String studentId, String academicYear, String semester,
                          double amount, String status) throws SQLException {
        if (amount < 0 || (!status.equals("PAID") && !status.equals("UNPAID"))) {
            throw new IllegalArgumentException("Amount must be positive and status must be PAID or UNPAID.");
        }
        executeUpdate("INSERT INTO fees (studentId, academicYear, semesterName, amount, status) "
                        + "VALUES (?, ?, ?, ?, ?)", studentId, academicYear, semester, amount, status);
    }

    private static void ensureColumn(Connection connection, String table, String column, String definition)
            throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
        } catch (SQLException exception) {
            if (!exception.getMessage().toLowerCase().contains("duplicate column")) {
                throw exception;
            }
        }
    }

    static void registerCourse(String studentId, String courseCode) throws SQLException {
        String findCourse = "SELECT courseName FROM courses WHERE courseCode = ?";
        String courseName;
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(findCourse)) {
            statement.setString(1, courseCode);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new SQLException("Course does not exist.");
                }
                courseName = result.getString("courseName");
            }
        }
        executeUpdate("INSERT INTO registrations (studentId, courseCode, courseName) VALUES (?, ?, ?)",
                studentId, courseCode, courseName);
    }

    static void saveAttendance(String lecturerId, String studentId, String courseCode,
                               int attended, int total) throws SQLException {
        if (attended < 0 || total <= 0 || attended > total) {
            throw new IllegalArgumentException("Attendance must satisfy 0 <= attended <= total and total > 0.");
        }
        ensureLecturerOwnsCourse(lecturerId, courseCode);
        executeUpdate("INSERT INTO attendance (studentId, courseCode, classesAttended, totalClasses) VALUES (?, ?, ?, ?) "
                        + "ON DUPLICATE KEY UPDATE classesAttended = VALUES(classesAttended), totalClasses = VALUES(totalClasses)",
                studentId, courseCode, attended, total);
    }

    static void saveResult(String lecturerId, String studentId, String courseCode,
                           double coursework, double exam) throws SQLException {
        if (coursework < 0 || coursework > 40 || exam < 0 || exam > 60) {
            throw new IllegalArgumentException("Coursework must be 0-40 and examination must be 0-60.");
        }
        ensureLecturerOwnsCourse(lecturerId, courseCode);
        double total = coursework + exam;
        String grade = total >= 80 ? "A" : total >= 70 ? "B" : total >= 60 ? "C" : total >= 50 ? "D" : "F";
        executeUpdate("INSERT INTO results (studentId, courseCode, courseworkMark, examMark, totalMark, grade) "
                        + "VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE courseworkMark = VALUES(courseworkMark), "
                        + "examMark = VALUES(examMark), totalMark = VALUES(totalMark), grade = VALUES(grade)",
                studentId, courseCode, coursework, exam, total, grade);
    }

    private static void ensureLecturerOwnsCourse(String lecturerId, String courseCode) throws SQLException {
        String sql = "SELECT COUNT(*) FROM courses WHERE courseCode = ? AND lecturerId = ?";
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, courseCode);
            statement.setString(2, lecturerId);
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                if (result.getInt(1) == 0) {
                    throw new SQLException("You are not assigned to this course.");
                }
            }
        }
    }

    static void printQuery(String sql, Object... values) throws SQLException {
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < values.length; i++) {
                statement.setObject(i + 1, values[i]);
            }
            try (ResultSet result = statement.executeQuery()) {
                int columns = result.getMetaData().getColumnCount();
                while (result.next()) {
                    for (int i = 1; i <= columns; i++) {
                        System.out.print(result.getMetaData().getColumnLabel(i) + ": " + result.getString(i));
                        if (i < columns) {
                            System.out.print(" | ");
                        }
                    }
                    System.out.println();
                }
            }
        }
    }

    static String queryJson(String sql, Object... values) throws SQLException {
        StringBuilder json = new StringBuilder("[");
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < values.length; i++) {
                statement.setObject(i + 1, values[i]);
            }
            try (ResultSet result = statement.executeQuery()) {
                int columns = result.getMetaData().getColumnCount();
                boolean firstRow = true;
                while (result.next()) {
                    if (!firstRow) {
                        json.append(',');
                    }
                    json.append('{');
                    for (int i = 1; i <= columns; i++) {
                        if (i > 1) {
                            json.append(',');
                        }
                        String name = result.getMetaData().getColumnLabel(i);
                        json.append('"').append(jsonEscape(name)).append("\":");
                        json.append('"').append(jsonEscape(result.getString(i))).append('"');
                    }
                    json.append('}');
                    firstRow = false;
                }
            }
        }
        return json.append(']').toString();
    }

    private static String jsonEscape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\r", "\\r").replace("\n", "\\n");
    }

    private static void executeUpdate(String sql, Object... values) throws SQLException {
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(sql)) {
            for (int i = 0; i < values.length; i++) {
                statement.setObject(i + 1, values[i]);
            }
            statement.executeUpdate();
        }
    }

    private static boolean passwordMatches(String password, String storedPassword) {
        return storedPassword.equals(hashPassword(password)) || storedPassword.equals(password);
    }

    private static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hash = new StringBuilder();
            for (byte value : bytes) {
                hash.append(String.format("%02x", value));
            }
            return hash.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    static final class UserAccount {
        final String userId;
        final String username;
        final String role;
        final String studentId;
        final String lecturerId;

        UserAccount(String userId, String username, String role, String studentId, String lecturerId) {
            this.userId = userId;
            this.username = username;
            this.role = role;
            this.studentId = studentId;
            this.lecturerId = lecturerId;
        }
    }
}