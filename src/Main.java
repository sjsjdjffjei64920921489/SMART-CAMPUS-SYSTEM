import java.sql.SQLException;
import java.util.Scanner;

public class Main {

    private static final Scanner INPUT = new Scanner(System.in);

    public static void main(String[] args) {
        try {
            Database.initialize();
            System.out.println("SMART CAMPUS STUDENT MANAGEMENT SYSTEM");
            System.out.println("Database connection established.");
            loginLoop();
        } catch (SQLException exception) {
            System.out.println("Could not start the system: " + exception.getMessage());
            System.out.println("Check that MySQL is running, the smartcampus database exists, and credentials are correct.");
        } finally {
            INPUT.close();
        }
    }

    private static void loginLoop() throws SQLException {
        while (true) {
            System.out.println("\n1. Login");
            System.out.println("2. Exit");
            String choice = prompt("Choose an option: ");
            if ("2".equals(choice)) {
                return;
            }
            if (!"1".equals(choice)) {
                System.out.println("Invalid option.");
                continue;
            }

            String username = prompt("Username: ");
            String password = prompt("Password: ");
            Database.UserAccount account = Database.authenticate(username, password);
            if (account == null) {
                System.out.println("Invalid username or password.");
                continue;
            }

            System.out.println("Login successful. Role: " + account.role);
            if ("ADMIN".equals(account.role)) {
                administratorMenu();
            } else if ("LECTURER".equals(account.role)) {
                lecturerMenu(account.lecturerId);
            } else {
                studentMenu(account.studentId);
            }
        }
    }

    private static void administratorMenu() {
        while (true) {
            System.out.println("\n--- ADMINISTRATOR MENU ---");
            System.out.println("1. Add student");
            System.out.println("2. Add lecturer");
            System.out.println("3. Add department");
            System.out.println("4. Add program");
            System.out.println("5. Add course");
            System.out.println("6. Add academic year");
            System.out.println("7. Add semester");
            System.out.println("8. Create user account");
            System.out.println("9. Record student fee");
            System.out.println("10. View students");
            System.out.println("11. View lecturers");
            System.out.println("12. View courses");
            System.out.println("13. View academic report");
            System.out.println("0. Logout");
            String choice = prompt("Choose an option: ");
            try {
                switch (choice) {
                    case "1":
                        Database.addStudent(prompt("Student ID: "), prompt("Name: "),
                                prompt("Program: "), prompt("Department: "));
                        System.out.println("Student saved.");
                        break;
                    case "2":
                        Database.addLecturer(prompt("Lecturer ID: "), prompt("Name: "), prompt("Department: "));
                        System.out.println("Lecturer saved.");
                        break;
                    case "3":
                        Database.addDepartment(prompt("Department name: "));
                        System.out.println("Department saved.");
                        break;
                    case "4":
                        Database.addProgram(prompt("Program name: "), prompt("Department: "));
                        System.out.println("Program saved.");
                        break;
                    case "5":
                        Database.addCourse(prompt("Course code: "), prompt("Course name: "),
                                integerPrompt("Credit units: "), prompt("Lecturer ID: "));
                        System.out.println("Course saved.");
                        break;
                    case "6":
                        Database.addAcademicYear(prompt("Academic year (for example 2026/2027): "));
                        System.out.println("Academic year saved.");
                        break;
                    case "7":
                        Database.addSemester(prompt("Semester name: "), prompt("Academic year: "));
                        System.out.println("Semester saved.");
                        break;
                    case "8":
                        Database.addUser(prompt("User ID: "), prompt("Username: "), prompt("Password: "),
                                prompt("Role (ADMIN/LECTURER/STUDENT): "), optionalPrompt("Student ID (blank if none): "),
                                optionalPrompt("Lecturer ID (blank if none): "));
                        System.out.println("User account created.");
                        break;
                    case "9":
                        Database.recordFee(prompt("Student ID: "), prompt("Academic year: "),
                                prompt("Semester: "), decimalPrompt("Amount: "), prompt("Status (PAID/UNPAID): "));
                        System.out.println("Fee record saved.");
                        break;
                    case "10":
                        Database.printQuery("SELECT * FROM students ORDER BY studentId");
                        break;
                    case "11":
                        Database.printQuery("SELECT * FROM lecturers ORDER BY lecturerId");
                        break;
                    case "12":
                        Database.printQuery("SELECT * FROM courses ORDER BY courseCode");
                        break;
                    case "13":
                        printReport();
                        break;
                    case "0":
                        return;
                    default:
                        System.out.println("Invalid option.");
                }
            } catch (Exception exception) {
                System.out.println("Operation failed: " + exception.getMessage());
            }
        }
    }

    private static void lecturerMenu(String lecturerId) {
        while (true) {
            System.out.println("\n--- LECTURER MENU ---");
            System.out.println("1. View assigned courses");
            System.out.println("2. View registered students");
            System.out.println("3. Record attendance");
            System.out.println("4. Enter coursework and examination marks");
            System.out.println("5. View student performance");
            System.out.println("0. Logout");
            String choice = prompt("Choose an option: ");
            try {
                switch (choice) {
                    case "1":
                        Database.printQuery("SELECT courseCode, courseName, creditUnits, lecturerId FROM courses "
                                + "WHERE lecturerId = ?", lecturerId);
                        break;
                    case "2":
                        Database.printQuery("SELECT r.studentId, r.courseCode, r.courseName FROM registrations r "
                                + "JOIN courses c ON c.courseCode = r.courseCode WHERE c.lecturerId = ?", lecturerId);
                        break;
                    case "3":
                        Database.saveAttendance(lecturerId, prompt("Student ID: "), prompt("Course code: "),
                                integerPrompt("Classes attended: "), integerPrompt("Total classes: "));
                        System.out.println("Attendance saved.");
                        break;
                    case "4":
                        Database.saveResult(lecturerId, prompt("Student ID: "), prompt("Course code: "),
                                decimalPrompt("Coursework mark (0-40): "), decimalPrompt("Exam mark (0-60): "));
                        System.out.println("Result saved and grade calculated.");
                        break;
                    case "5":
                        Database.printQuery("SELECT r.* FROM results r JOIN courses c ON c.courseCode = r.courseCode "
                                + "WHERE c.lecturerId = ? ORDER BY r.studentId, r.courseCode", lecturerId);
                        break;
                    case "0":
                        return;
                    default:
                        System.out.println("Invalid option.");
                }
            } catch (Exception exception) {
                System.out.println("Operation failed: " + exception.getMessage());
            }
        }
    }

    private static void studentMenu(String studentId) {
        while (true) {
            System.out.println("\n--- STUDENT MENU ---");
            System.out.println("1. View profile");
            System.out.println("2. Register for a course");
            System.out.println("3. View registered courses");
            System.out.println("4. View attendance");
            System.out.println("5. View results and GPA");
            System.out.println("6. View academic transcript");
            System.out.println("7. View fees");
            System.out.println("0. Logout");
            String choice = prompt("Choose an option: ");
            try {
                switch (choice) {
                    case "1":
                        Database.printQuery("SELECT * FROM students WHERE studentId = ?", studentId);
                        break;
                    case "2":
                        Database.registerCourse(studentId, prompt("Course code: "));
                        System.out.println("Course registration saved.");
                        break;
                    case "3":
                        Database.printQuery("SELECT * FROM registrations WHERE studentId = ?", studentId);
                        break;
                    case "4":
                        Database.printQuery("SELECT *, (classesAttended * 100.0 / totalClasses) AS percentage "
                                + "FROM attendance WHERE studentId = ?", studentId);
                        break;
                    case "5":
                        Database.printQuery("SELECT * FROM results WHERE studentId = ?", studentId);
                        printGpa(studentId);
                        break;
                    case "6":
                        Database.printQuery("SELECT s.name, s.program, r.courseCode, r.courseName, "
                                + "x.totalMark, x.grade FROM students s JOIN registrations r ON s.studentId = r.studentId "
                                + "LEFT JOIN results x ON r.studentId = x.studentId AND r.courseCode = x.courseCode "
                                + "WHERE s.studentId = ?", studentId);
                        break;
                            case "7":
                            Database.printQuery("SELECT * FROM fees WHERE studentId = ? ORDER BY academicYear, semesterName", studentId);
                            break;
                    case "0":
                        return;
                    default:
                        System.out.println("Invalid option.");
                }
            } catch (Exception exception) {
                System.out.println("Operation failed: " + exception.getMessage());
            }
        }
    }

    private static void printGpa(String studentId) throws SQLException {
        Database.printQuery("SELECT ROUND(AVG(CASE grade WHEN 'A' THEN 5 WHEN 'B' THEN 4 "
                + "WHEN 'C' THEN 3 WHEN 'D' THEN 2 ELSE 0 END), 2) AS GPA FROM results WHERE studentId = ?", studentId);
    }

    private static void printReport() throws SQLException {
        System.out.println("\nStudents: ");
        Database.printQuery("SELECT COUNT(*) AS totalStudents FROM students");
        System.out.println("Courses: ");
        Database.printQuery("SELECT COUNT(*) AS totalCourses FROM courses");
        System.out.println("Results: ");
        Database.printQuery("SELECT COUNT(*) AS totalResults FROM results");
    }

    private static String prompt(String message) {
        System.out.print(message);
        return INPUT.nextLine().trim();
    }

    private static String optionalPrompt(String message) {
        String value = prompt(message);
        return value.isEmpty() ? null : value;
    }

    private static int integerPrompt(String message) {
        while (true) {
            try {
                return Integer.parseInt(prompt(message));
            } catch (NumberFormatException exception) {
                System.out.println("Enter a whole number.");
            }
        }
    }

    private static double decimalPrompt(String message) {
        while (true) {
            try {
                return Double.parseDouble(prompt(message));
            } catch (NumberFormatException exception) {
                System.out.println("Enter a valid number.");
            }
        }
    }
}