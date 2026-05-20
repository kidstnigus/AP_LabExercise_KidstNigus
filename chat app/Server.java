import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class Server {

    // Centralized port allocation
    public static final int PORT = 1234;

    // Map linking active usernames to their persistent communication threads
    public static Map<String, ClientHandler> clients = new ConcurrentHashMap<>();

    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = new ServerSocket(PORT);
        System.out.println("ChatIT Server running on port " + PORT);

        try {
            while (true) {
                Socket socket = serverSocket.accept();
                new ClientHandler(socket).start();
            }
        } finally {
            serverSocket.close();
        }
    }

    // Pushes an updated comma-separated roster of active handles to all nodes
    public static void broadcastUsers() {
        String users = String.join(",", clients.keySet());
        for (ClientHandler ch : clients.values()) {
            ch.send("USERS:" + users);
        }
    }

    // Extracts the active session matching the destination key to route packets
    public static void sendPrivate(String from, String to, String msg) {
        ClientHandler receiver = clients.get(to);
        if (receiver != null) {
            receiver.send("MSG:" + from + ":" + msg);
        }
    }
}