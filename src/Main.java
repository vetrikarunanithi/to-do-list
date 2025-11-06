import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * Simple To-Do List Backend (Beginner Version)
 * --------------------------------------------
 * This program runs a small local web server that works with your frontend.
 * It can:
 *   - Add tasks
 *   - Delete tasks
 *   - Mark tasks completed / uncompleted
 *   - Show all tasks
 *   - Clear completed tasks
 */

public class Main {

    // A class to represent each task
    static class Task {
        int id;
        String text;
        boolean completed;

        Task(int id, String text) {
            this.id = id;
            this.text = text;
            this.completed = false;
        }
    }

    // Store all tasks in memory
    static List<Task> tasks = new ArrayList<>();
    static int nextId = 1;

    public static void main(String[] args) {
        try {
            int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);


            System.out.println("=================================");
            System.out.println("  To-Do List Server Started");
            System.out.println("  Visit: http://localhost:8080");
            System.out.println("=================================");

            // Setup routes
            server.createContext("/tasks", Main::getTasks);
            server.createContext("/add", Main::addTask);
            server.createContext("/delete", Main::deleteTask);
            server.createContext("/toggle", Main::toggleTask);
            server.createContext("/clear-completed", Main::clearCompleted);

            server.setExecutor(null);
            server.start();
        } catch (IOException e) {
            System.out.println("Error starting server: " + e.getMessage());
        }
    }

    // Show all tasks
    private static void getTasks(HttpExchange exchange) throws IOException {
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < tasks.size(); i++) {
            Task t = tasks.get(i);
            json.append("{")
                .append("\"id\":").append(t.id).append(",")
                .append("\"text\":\"").append(escapeJson(t.text)).append("\",")
                .append("\"completed\":").append(t.completed)
                .append("}");
            if (i < tasks.size() - 1) json.append(",");
        }
        json.append("]");
        sendResponse(exchange, json.toString());
    }

    // Add a new task
    private static void addTask(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        if (query != null && query.startsWith("task=")) {
            String text = decodeUrl(query.substring(5));
            Task newTask = new Task(nextId++, text);
            tasks.add(newTask);
            sendResponse(exchange, "{\"status\":\"ok\"}");
        } else {
            sendResponse(exchange, "{\"error\":\"No task text given\"}");
        }
    }

    // Delete a task
    private static void deleteTask(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        if (query != null && query.startsWith("id=")) {
            int id = Integer.parseInt(query.substring(3));
            tasks.removeIf(t -> t.id == id);
            sendResponse(exchange, "{\"status\":\"ok\"}");
        } else {
            sendResponse(exchange, "{\"error\":\"Invalid ID\"}");
        }
    }

    // Toggle task completion
    private static void toggleTask(HttpExchange exchange) throws IOException {
        String query = exchange.getRequestURI().getQuery();
        if (query != null && query.startsWith("id=")) {
            int id = Integer.parseInt(query.substring(3));
            for (Task t : tasks) {
                if (t.id == id) {
                    t.completed = !t.completed;
                    break;
                }
            }
            sendResponse(exchange, "{\"status\":\"ok\"}");
        } else {
            sendResponse(exchange, "{\"error\":\"Invalid ID\"}");
        }
    }

    // Remove completed tasks
    private static void clearCompleted(HttpExchange exchange) throws IOException {
        tasks.removeIf(t -> t.completed);
        sendResponse(exchange, "{\"status\":\"ok\"}");
    }

    // Utility: send JSON response
    private static void sendResponse(HttpExchange exchange, String response) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    // Utility: decode URL parameters
    private static String decodeUrl(String str) {
        try {
            return java.net.URLDecoder.decode(str, "UTF-8");
        } catch (Exception e) {
            return str;
        }
    }

    // Utility: escape JSON text safely
    private static String escapeJson(String str) {
        return str.replace("\"", "\\\"")
                  .replace("\\", "\\\\");
    }
}
