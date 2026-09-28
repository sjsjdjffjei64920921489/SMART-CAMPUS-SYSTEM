public class Attendance {

    Student student;
    Course course;
    int classesAttended;
    int totalClasses;

    Attendance(Student student, Course course, int classesAttended, int totalClasses) {
        this.student = student;
        this.course = course;
        this.classesAttended = classesAttended;
        this.totalClasses = totalClasses;
    }

    double calculatePercentage() {
        return (classesAttended * 100.0) / totalClasses;
    }

    void displayAttendance() {
        System.out.println("Student: " + student.name);
        System.out.println("Course: " + course.courseName);
        System.out.println("Classes Attended: " + classesAttended);
        System.out.println("Total Classes: " + totalClasses);
        System.out.println("Attendance: " + calculatePercentage() + "%");
    }
}