import java.io.*;
import java.net.Socket;

public class ClientHandler extends Thread {

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private String username;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            // Expect the opening registration payload to establish the username handshake
            username = in.readLine();

            if (username == null || username.trim().isEmpty() || Server.clients.containsKey(username)) {
                socket.close();
                return;
            }

            Server.clients.put(username, this);
            Server.broadcastUsers();

            String line;
            while ((line = in.readLine()) != null) {
                // Incoming structural format expected: TO:message
                String[] parts = line.split(":", 2);
                if (parts.length == 2) {
                    Server.sendPrivate(username, parts[0], parts[1]);
                }
            }

        } catch (Exception e) {
            System.out.println((username != null ? username : "Unknown client") + " disconnected");
        } finally {
            // Guarantee resource teardown and state synchronization on drop
            if (username != null) {
                Server.clients.remove(username);
                Server.broadcastUsers();
            }
            try {
                socket.close();
            } catch (IOException e) {
                // Already closing
            }
        }
    }

    public void send(String msg) {
        if (out != null) {
            out.println(msg);
        }
    }
}