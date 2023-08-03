import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
class Server {
    List<Socket> clientSockets = new ArrayList<>();
    List<String> clientNames = new ArrayList<>();
    public void start() {
        try {
           try (ServerSocket server = new ServerSocket(9999)) {
               System.out.println("Server is ready to accept connections...");
                while (true) {
                    Socket socket = server.accept();
                    System.out.println("New client connected: " + socket);
                    clientSockets.add(socket);
                    new Thread(() -> handleClient(socket)).start();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
  private boolean isValidName(String name) {

        // Check if the name contains any spaces

        return !name.contains(" ");

    }



    private void handleClient(Socket clientSocket) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            // Ask the user to enter their name for identification
            String name = in.readLine();
            if (!isValidName(name) || name.equals("null")) {
               out.println("ERROR: Invalid name. Your name contains spaces or is null.");
                out.println("Please choose a different name.");
                clientSocket.close();
                return;
            }
            // Check if the name already exists
            synchronized (this) {
                if (clientNames.contains(name)) {
                    out.println("ERROR: Name already in use.");
                    out.println("Please choose a different name.");
                    clientSocket.close();
                    return;
                } else {
                    clientNames.add(name);
                    System.out.println("Client identified as: " + name);
                    broadcastNames();
                }
            }
            while (true) {
                String msg = in.readLine();
                if (msg == null || msg.equals("exit")) {
                    // out.println( + name);
                    broadcastMessage(name, " disconnected.....");
                    clientSocket.close();
                    synchronized (this) {
                        clientNames.remove(name);
                        broadcastNames();
                    }
                    break;
                } else if (msg.startsWith("/p")) {
                   try{
                        String[] parts = msg.split(" ", 3);
                        String recipient = parts[1];
                        String personalMsg = parts[2];
                        boolean flag = false;
                       for (String names : clientNames) {
                            if (msg.contains(names)) {
                                flag = true;
                                break;
                            } else {
                                // out.println("Not in the group");
                                flag = false;
                            }
                        }
                        if (flag) {
                            sendPersonalMessage(name, recipient, personalMsg);
                            // Show the personal message on the sender's screen
                            out.println(personalMsg);
                            System.out.println("(Personal) " + name + " to " + recipient + ": " + personalMsg);
                        } else {
                            out.println("Recipient Not Found");
                        }
                    }catch(Exception e)
                    {
                       out.println("Please provide the message:");
                        e.printStackTrace();
                    }
                } else {
                    System.out.println("Received from " + name + ": " + msg);
                    // Use the same method to broadcast messages
                   broadcastMessage(name, msg);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void sendMessageToClient(Socket clientSocket, String message) {
        try {
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
            out.println(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void broadcastMessage(String senderName, String message) {
        for (Socket clientSocket : clientSockets) {
            sendMessageToClient(clientSocket, senderName + ": " + message);
        }
    }

    private void sendPersonalMessage(String senderName, String recipient, String message) {
        for (Socket clientSocket : clientSockets) {
            if (clientNames.contains(recipient)) {
                if (clientNames.get(clientSockets.indexOf(clientSocket)).equals(recipient)) {
                   sendMessageToClient(clientSocket,
                            senderName + " has sent a private message to you :  " + message);
                }
            }
        }
    }
    private void broadcastNames() {
        for (Socket clientSocket : clientSockets) {
           try {
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                out.println("---Client Names---");
                for (String name : clientNames) {
                    out.println(name);
                }
                out.println("---End of Client Names---");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("Server is going to start");
        new Server().start();
    }
}
