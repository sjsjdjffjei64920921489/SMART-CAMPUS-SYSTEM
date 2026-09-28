public class Lecturer {

    String lecturerId;
    String name;
    String department;

    
    Lecturer(String lecturerId, String name, String department) {
        this.lecturerId = lecturerId;
        this.name = name;
        this.department = department;
    }


    void displayLecturer() {
        System.out.println("Lecturer ID: " + lecturerId);
        System.out.println("Name: " + name);
        System.out.println("Department: " + department);
    }
}
