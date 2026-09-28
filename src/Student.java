import java.util.ArrayList;

public class Student {

    String studentId;
    String name;
    String program;
    String department;

    ArrayList<Course> courses = new ArrayList<>();

    Student(String studentId, String name, String program, String department) {
        this.studentId = studentId;
        this.name = name;
        this.program = program;
        this.department = department;
    }

    void registerCourse(Course course) {
        courses.add(course);
    }

    void displayStudent() {
        System.out.println("Student ID: " + studentId);
        System.out.println("Name: " + name);
        System.out.println("Program: " + program);
        System.out.println("Department: " + department);

        System.out.println("Registered Courses:");

        for (Course course : courses) {
            System.out.println("- " + course.courseCode + " : " + course.courseName);
        }
    }
}