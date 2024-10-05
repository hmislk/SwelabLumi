package org.carecode.mw.lims.mw.sweLabLumi;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;


public class AnalyzerCommunicator {

    private static final char ENQ = 0x05; // Enquiry
    private static final char ACK = 0x06; // Acknowledgement
    private static final char EOT = 0x04; // End of Transmission
    private static final char STX = 0x02; // Start of Text
    private static final char ETX = 0x03; // End of Text

    public static void startServer() {
        int middlewarePort = SettingsLoader.getSettings().getAnalyzerDetails().getAnalyzerPort();

        try (ServerSocket serverSocket = new ServerSocket(middlewarePort)) {
            System.out.println("Server started, listening on port: " + middlewarePort);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                 OutputStream out = clientSocket.getOutputStream()) {
                StringBuilder messageBuffer = new StringBuilder();
                int charRead;
                while ((charRead = in.read()) != -1) {
                    char receivedChar = (char) charRead;
                    if (receivedChar == ENQ) {
                        // Respond with ACK
                        out.write(ACK);
                        out.flush();
                        System.out.println("Received ENQ, sent ACK");
                    } else if (receivedChar == EOT) {
                        // End of transmission, process message but keep connection open
                        String message = messageBuffer.toString();
                        System.out.println("Received message: " + message);
                        handleMessage(message, out);
                        messageBuffer.setLength(0); // Clear the buffer
                    } else {
                        // Append received character to buffer
                        messageBuffer.append(receivedChar);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    clientSocket.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void handleMessage(String message, OutputStream out) {
        try {
            // Process message and determine appropriate response
            // For demonstration, just sending ACK after processing the message
            // In a real implementation, you might need to parse and handle the message appropriately

            // Send ACK after processing the message
            out.write(ACK);
            out.flush();
            System.out.println("Sent ACK after processing message");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
