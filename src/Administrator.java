public class Administrator {

    String adminId;
    String name;
    String department;

    Administrator(String adminId, String name, String department) {
        this.adminId = adminId;
        this.name = name;
        this.department = department;

    }

    void displayAdministrator() {
        System.out.println("Administrator ID: " + adminId);
        System.out.println("Name: " + name);
        System.out.println("Department: " + department);
    }
}
