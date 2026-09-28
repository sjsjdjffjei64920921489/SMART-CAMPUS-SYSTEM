public class Result {

    Student student;
    Course course;
    double courseworkMark;
    double examMark;

    Result(Student student, Course course, double courseworkMark, double examMark) {
        this.student = student;
        this.course = course;
        this.courseworkMark = courseworkMark;
        this.examMark = examMark;
    }

    double calculateTotal() {
        return courseworkMark + examMark;
    }

    String calculateGrade() {

        double total = calculateTotal();

        if (total >= 80) {
            return "A";
        } else if (total >= 70) {
            return "B";
        } else if (total >= 60) {
            return "C";
        } else if (total >= 50) {
            return "D";
        } else {
            return "F";
        }
    }

    void displayResult() {
        System.out.println("Student: " + student.name);
        System.out.println("Course: " + course.courseName);
        System.out.println("Coursework Mark: " + courseworkMark);
        System.out.println("Exam Mark: " + examMark);
        System.out.println("Total Mark: " + calculateTotal());
        System.out.println("Grade: " + calculateGrade());
    }
}
