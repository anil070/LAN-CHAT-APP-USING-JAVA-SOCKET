import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Date;
import java.text.SimpleDateFormat;
public class Client extends JFrame {
    private JTextArea chatArea;
    private JTextField messageField;
    private JButton sendButton;

    private JTextArea clientListArea;

    private Socket socket;

    private BufferedReader br;

    private PrintWriter out;

    private String clientName;

    JPanel mainPanel = new JPanel(new GridLayout(1, 2));
    public Client() {
        createUI();
        establishConnection();
        startReading();
    }
    private void createUI() {
        setTitle("Gup-Shup App");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        // Define custom colors for the UI
        Color cream = new Color(210, 210, 231); // Cream color
        Color purple = new Color(212, 203, 226); // Lighter purple
        // Color darkPurple = new Color(122, 42, 94); // Darker purple
        Color teal = new Color(152, 119, 211); // Teal
        Color foreground = new Color(51, 51, 51); // Darker foreground

        // Apply custom theme to components
       UIManager.put("Panel.background", cream); // Home screen background color
        UIManager.put("TextArea.background", purple); // Chat area background color
        UIManager.put("TextArea.foreground", foreground);
        UIManager.put("TextField.background", Color.WHITE); // Text field background color set to white
        UIManager.put("TextField.foreground", foreground);
        UIManager.put("Button.background", teal);
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Label.foreground", foreground);
        // Create components
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setBackground(purple); // Match the chat area background color
        messageField = new JTextField();
        messageField.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
        messageField.setForeground(Color.GRAY);
        chatArea.addKeyListener(new KeyAdapter() {

            @Override

            public void keyPressed(KeyEvent e) {
                if (e.getKeyChar() == '/') {
                    // Set the focus on the message field
                    messageField.requestFocusInWindow();
               }
            }

        });
        messageField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        sendButton = new JButton("Send");
        sendButton.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
        sendButton.setBackground(teal); // Change button background color
        sendButton.setForeground(Color.WHITE);
       sendButton.setFocusPainted(false); // Remove the border when button is focused
        sendButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, 2), // Add a black border
                BorderFactory.createEmptyBorder(5, 15, 5, 15) // Add padding around the text
        ));
        // Add a hover effect to the button
        sendButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                sendButton.setBackground(teal.darker()); // Darken the background color on hover
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                sendButton.setBackground(teal); // Restore the original background color on exit
            }
        });
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });
        clientListArea = new JTextArea(8, 20);
        clientListArea.setEditable(false);
        clientListArea.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));
        JScrollPane clientListScrollPane = new JScrollPane(clientListArea);
        TitledBorder clientListBorder = BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.BLACK, 2), "Joined Members");
        clientListBorder.setTitleColor(foreground);
        clientListScrollPane.setBorder(clientListBorder);



        JPanel inputPanel = new JPanel(new BorderLayout());

        inputPanel.setBackground(cream); // Match the home screen background color

        inputPanel.add(messageField, BorderLayout.CENTER);

        inputPanel.add(sendButton, BorderLayout.EAST);



        JPanel chatPanel = new JPanel(new BorderLayout());

        chatPanel.setBackground(purple); // Match the chat area background color

        chatPanel.add(scrollPane, BorderLayout.CENTER);

        chatPanel.add(inputPanel, BorderLayout.SOUTH);



        // Add a titled border to the chat panel

        TitledBorder chatPanelBorder = BorderFactory.createTitledBorder(

                BorderFactory.createLineBorder(Color.BLACK, 2),

                "Chat (Type **/p recipient_name** to send message privately)");

        chatPanelBorder.setTitleColor(foreground);

        chatPanel.setBorder(chatPanelBorder);



        JPanel mainPanel = new JPanel(new GridLayout(1, 2));

        mainPanel.setBackground(cream); // Match the home screen background color

        mainPanel.add(chatPanel);

        mainPanel.add(clientListScrollPane);



        add(mainPanel);

        setVisible(true);

    }



    private void establishConnection() {
        try {
            String serverAddress = "192.168.0.45"; // Replace with the server's address
            int portNumber = 9999; // Replace with the server's port number
            socket = new Socket(serverAddress, portNumber);
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream());
            clientName = JOptionPane.showInputDialog("Enter your username:");
            out.println(clientName);
            out.flush();
        } catch (IOException e) {
            displayError("Connection failed.");
            // mainPanel.setVisible(false);
            e.printStackTrace();
        }
    }
    private void startReading() {
        new Thread(new Runnable() {
            @Override
           public void run() {
                try {
                    while (true) {
                        String message = br.readLine();
                        if (message != null) {
                            if (message.equals("---Client Names---")) {
                                updateClientList();
                            } else {
                               displayMessage(message);
                            }
                        }
                    }
                } catch (IOException e) {
                    displayError("Connection closed unexpectedly.");
                }
            }
        }).start();
    }
   private void sendMessage() {
        String content = messageField.getText();
        if (!content.isEmpty()) {
            out.println(content);
            out.flush();
            messageField.setText("");
        }
    }
    private String getCurrentTimestamp() {
        long currentTimeMillis = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(new Date(currentTimeMillis));
    }
    private void displayMessage(String message) {
        SwingUtilities.invokeLater(new Runnable() {
           String timestamp = "[" + getCurrentTimestamp() + "] ";
            @Override
            public void run() {
                chatArea.append(timestamp + " " + message + "\n");
            }
        });
    }

   private void updateClientList() {
        try {
            StringBuilder clientList = new StringBuilder();
            while (true) {
                String clientName = br.readLine();
                if (clientName.equals("---End of Client Names---")) {
                    break;
                }
                clientList.append(clientName).append("\n");
            }
            clientListArea.setText(clientList.toString());
        } catch (IOException e) {
            displayError("Error while updating client list.");
        }
    }
    private void displayError(String message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JOptionPane.showMessageDialog(Client.this, message, "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new Client();
            }
        });

    }
}
