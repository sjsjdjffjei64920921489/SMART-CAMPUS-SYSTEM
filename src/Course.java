public class Course {

    String courseCode;
    String courseName;
    int creditUnits;
    Lecturer lecturer;

    Course(String courseCode, String courseName, int creditUnits) {
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.creditUnits = creditUnits;
    }

    void assignLecturer(Lecturer lecturer) {
        this.lecturer = lecturer;
    }

    void displayCourse() {
        System.out.println("Course Code: " + courseCode);
        System.out.println("Course Name: " + courseName);
        System.out.println("Credit Units: " + creditUnits);

        if (lecturer != null) {
            System.out.println("Lecturer: " + lecturer.name);
        }
    }
}
