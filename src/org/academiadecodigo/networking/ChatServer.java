package org.academiadecodigo.networking;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ChatServer
 */
public class ChatServer {

    private Socket clientSocket;
    private ServerSocket serverSocket;
    private BufferedReader inputBufferedReader;
    private LinkedList <ChatHandler> list;
    private ExecutorService threadPool;

    public ChatServer(int port) {

        list = new LinkedList<>();

        threadPool = Executors.newCachedThreadPool();

        try {

            // bind the socket to specified port
            System.out.println("Binding to port " + port);
            serverSocket = new ServerSocket(port);

            System.out.println("Server started: " + serverSocket);

            while (true) {
                // block waiting for a client to connect
                System.out.println("Waiting for a client connection");
                clientSocket = serverSocket.accept();

                // handle client connection
                System.out.println("Client accepted: " + clientSocket);

                ChatHandler client = new ChatHandler(clientSocket);

                list.add(client);

                threadPool.submit(client);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * ChatServer entry point
     *
     * @param args ChatServer port number
     */
    public static void main(String args[]) {

        // exit application if no port number is specified
        if (args.length == 0) {
            System.out.println("Usage: java ChatServer [port]");
            System.exit(1);
        }

        try {
            // try to create an instance of the ChatServer at port specified at args[0]
            new ChatServer(Integer.parseInt(args[0]));

        } catch (NumberFormatException ex) {
            // write an error message if an invalid port was specified by the user
            System.out.println("Invalid port number " + args[0]);
        }

    }

    /**
     * Instantiate a buffered reader from the input stream of the client socket
     *
     * @throws IOException
     */
    public void setupSocketStream() throws IOException {
        inputBufferedReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }


    /**
     * Closes the client socket and the buffered input reader
     */
    public void close() {

        try {

            if (clientSocket != null) {
                System.out.println("Closing client connection");
                clientSocket.close();
            }

            if (serverSocket != null) {
                System.out.println("Closing server socket");
                serverSocket.close();
            }


        } catch (IOException ex) {

            System.out.println("Error closing connection: " + ex.getMessage());

        }

    }

    public class ChatHandler implements Runnable {

        private Socket clientSocket;
        private String name;
        private PrintWriter output;
        private BufferedReader input;

        private ChatHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
            name = "User " + (list.size() + 1);
            System.out.println(name + " is now connected");
            try {
                output = new PrintWriter(this.clientSocket.getOutputStream(), true);
                input = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            while (true) {
                try {
                    String line = input.readLine();
                    sendMessage(name + " : " + line);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void sendMessage(String message) {
            for (ChatHandler client : list) {
                client.output.println(message);
                client.output.flush();
            }
        }

        public String getName() {
            return name;
        }
    }

}


