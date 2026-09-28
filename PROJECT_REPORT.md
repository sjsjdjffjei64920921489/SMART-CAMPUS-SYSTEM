# Smart Campus Student Management and Academic Information System

## Technology

- Java
- MySQL
- JDBC
- JDK HTTP Server
- HTML, CSS, JavaScript, Bootstrap
- Visual Studio Code

## Implemented Features

- Role-based login for administrator, lecturer, and student
- Student, lecturer, department, program, and course management
- Academic year and semester management
- User account creation by administrators
- Course registration
- Attendance recording with percentage calculation
- Coursework and examination mark entry
- Automatic total marks and grades
- GPA and transcript views
- Student fee records and payment status
- Lecturer access restricted to assigned courses
- Administrative academic reports
- Browser-based role dashboards

## Demo Accounts

| Role | Username | Password |
| --- | --- | --- |
| Administrator | admin | admin123 |
| Lecturer | lecturer | lecturer123 |
| Student | student | student123 |

Change these credentials before production use. Passwords are currently stored simply for coursework demonstration; a production system should use password hashing such as BCrypt or Argon2.

## Setup

1. Create the `smartcampus` database in MySQL using `database/schema.sql`.
2. Set the database environment variables before starting the application. On Windows PowerShell:
	`$env:SMARTCAMPUS_DB_PASSWORD="your-mysql-password"`
	Optional variables are `SMARTCAMPUS_DB_URL` and `SMARTCAMPUS_DB_USER`.
3. Ensure `lib/mysql-connector-j-26.7.0.jar` is available.
4. Compile with `javac --add-modules jdk.httpserver -cp "lib\\mysql-connector-j-26.7.0.jar" -d out src\\*.java`.
5. Run the console version with `java -cp "out;lib\\mysql-connector-j-26.7.0.jar" Main` on Windows.
6. Run the web version with `java --add-modules jdk.httpserver -cp "out;lib\\mysql-connector-j-26.7.0.jar" WebServer`.
7. Open `http://localhost:8080` in a browser.

## Future Improvements

- Password hashing and environment-based configuration
- Spring Boot REST API and expanded HTML/Bootstrap CRUD frontend
- Pagination, search, and exportable PDF transcripts
- Foreign-key migration scripts and automated tests
