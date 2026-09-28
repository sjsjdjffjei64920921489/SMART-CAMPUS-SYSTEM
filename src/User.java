public class User {

    String userId;
    String username;
    String password;
    String role;

    User(String userId, String username, String password, String role) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    void displayUser() {
        System.out.println("User ID: " + userId);
        System.out.println("Username: " + username);
        System.out.println("Password: " + password);
        System.out.println("Role: " + role);
    }

    boolean login(String username, String password) {

        if (this.username.equals(username) &&
            this.password.equals(password)) {

            return true;
        }

        return false;
    }
}
