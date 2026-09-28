import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class WebServer {

    private static final int PORT = 8080;
    private static final Path WEB_ROOT = Path.of("web");
    private static final Map<String, Database.UserAccount> SESSIONS = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {
        Database.initialize();
        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/api/login", WebServer::login);
        server.createContext("/api/dashboard", WebServer::dashboard);
        server.createContext("/", WebServer::staticFile);
        server.setExecutor(null);
        server.start();
        System.out.println("Smart Campus web portal running at http://localhost:" + PORT);
    }

    private static void login(HttpExchange exchange) throws IOException {
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            send(exchange, 405, "{\"error\":\"POST required\"}", "application/json");
            return;
        }
        Map<String, String> form = parseForm(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
        try {
            Database.UserAccount account = Database.authenticate(form.getOrDefault("username", ""),
                    form.getOrDefault("password", ""));
            if (account == null) {
                send(exchange, 401, "{\"error\":\"Invalid username or password\"}", "application/json");
                return;
            }
            String token = UUID.randomUUID().toString();
            SESSIONS.put(token, account);
            String response = "{\"token\":\"" + token + "\",\"userId\":\"" + escape(account.userId) + "\",\"username\":\""
                    + escape(account.username) + "\",\"role\":\"" + escape(account.role)
                    + "\",\"studentId\":\"" + escape(account.studentId) + "\",\"lecturerId\":\""
                    + escape(account.lecturerId) + "\"}";
            send(exchange, 200, response, "application/json");
        } catch (SQLException exception) {
            send(exchange, 500, "{\"error\":\"Database error\"}", "application/json");
        }
    }

    private static void dashboard(HttpExchange exchange) throws IOException {
        Map<String, String> query = parseForm(exchange.getRequestURI().getRawQuery());
        Database.UserAccount account = SESSIONS.get(query.get("token"));
        if (account == null) {
            send(exchange, 401, "{\"error\":\"Login required\"}", "application/json");
            return;
        }
        String role = account.role;
        String id = "STUDENT".equals(role) ? account.studentId : account.lecturerId;
        try {
            String response;
            if ("ADMIN".equals(role)) {
                response = "{\"students\":" + Database.queryJson("SELECT COUNT(*) AS total FROM students")
                        + ",\"courses\":" + Database.queryJson("SELECT COUNT(*) AS total FROM courses")
                        + ",\"results\":" + Database.queryJson("SELECT COUNT(*) AS total FROM results") + "}";
            } else if ("LECTURER".equals(role)) {
                response = "{\"courses\":" + Database.queryJson("SELECT courseCode, courseName, creditUnits "
                        + "FROM courses WHERE lecturerId = ?", id) + "}";
            } else if ("STUDENT".equals(role)) {
                response = "{\"profile\":" + Database.queryJson("SELECT studentId, name, program, department "
                        + "FROM students WHERE studentId = ?", id)
                        + ",\"courses\":" + Database.queryJson("SELECT courseCode, courseName "
                        + "FROM registrations WHERE studentId = ?", id)
                        + ",\"results\":" + Database.queryJson("SELECT courseCode, totalMark, grade "
                        + "FROM results WHERE studentId = ?", id)
                        + ",\"fees\":" + Database.queryJson("SELECT academicYear, semesterName, amount, status "
                        + "FROM fees WHERE studentId = ?", id) + "}";
            } else {
                send(exchange, 400, "{\"error\":\"Unknown role\"}", "application/json");
                return;
            }
            send(exchange, 200, response, "application/json");
        } catch (SQLException exception) {
            send(exchange, 500, "{\"error\":\"Database error\"}", "application/json");
        }
    }

    private static void staticFile(HttpExchange exchange) throws IOException {
        String requested = exchange.getRequestURI().getPath();
        if ("/".equals(requested)) {
            requested = "/index.html";
        }
        Path file = WEB_ROOT.resolve(requested.substring(1)).normalize();
        if (!file.startsWith(WEB_ROOT) || !Files.exists(file) || Files.isDirectory(file)) {
            send(exchange, 404, "Not found", "text/plain");
            return;
        }
        String type = requested.endsWith(".css") ? "text/css" : requested.endsWith(".js")
                ? "application/javascript" : "text/html";
        send(exchange, 200, Files.readString(file), type);
    }

    private static Map<String, String> parseForm(String encoded) {
        Map<String, String> values = new HashMap<>();
        if (encoded == null || encoded.isBlank()) {
            return values;
        }
        for (String pair : encoded.split("&")) {
            String[] parts = pair.split("=", 2);
            String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
            String value = parts.length == 2 ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8) : "";
            values.put(key, value);
        }
        return values;
    }

    private static void send(HttpExchange exchange, int status, String body, String contentType) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType + "; charset=utf-8");
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private static String escape(String value) {
        return value == null ? "" : value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
