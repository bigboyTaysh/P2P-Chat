/* Client.java */

import javax.swing.*;
import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Client {
    //For the I/O
    private static BufferedReader Input;
    private static InputStream in;
    private PrintWriter Output;
    private Socket socket;

    private DatagramSocket datagramSocket;

    private String server;
    private static String username;
    private static int port;
    private int P2Pport;
    private boolean messageToServer;

    private static userWindow userFrame;

    Client(String server, int port, String username) {
        this.server = server;
        this.port = port;
        this.username = username;
    }

    private static String checkUsername(loginWindow frame, String userName) throws InterruptedException {
        while (true) {
            TimeUnit.MILLISECONDS.sleep(1);
            //Thread.sleep(1);
            if (frame.isClicked()) {
                userName = frame.getUserName();
                frame.setClicked(false);
                break;
            }
        }
        return userName;
    }

    private static String checkConnection(userWindow userFrame) throws InterruptedException {
        String userName;
        while (true) {
            TimeUnit.MILLISECONDS.sleep(1);
            //Thread.sleep(1);
            if (userFrame.isClicked()) {
                userName = userFrame.getUserName();
                userFrame.setClicked(false);
                break;
            }
        }
        return userName;
    }

    private static boolean checkRequest(userWindow userFrame) throws InterruptedException {
        while (true) {

            TimeUnit.MILLISECONDS.sleep(1);
            //Thread.sleep(1);
            if (userFrame.isClicked2()) {
                if(userFrame.isRequset()){
                    userFrame.setClicked2(false);
                    userFrame.setRequset(false);
                    return true;
                } else {
                    userFrame.setClicked2(false);
                    userFrame.setRequset(false);
                    return false;
                }
            }
        }
    }

    private void setRequest() throws InterruptedException {
            if (checkRequest(userFrame)) {
                System.out.println("Request to true");
                Output.println("Y");
            } else {
                System.out.println("Request to false");
                Output.println("N");
            }
    }



    private void checkDispose(userChat userChat) throws InterruptedException, IOException {
        String msg;
        while (true) {
            TimeUnit.MILLISECONDS.sleep(1);
            //Thread.sleep(1);
            if (userChat.isDispose()) {
                sendMessage("noChatting");
                datagramSocket.close();
                return;
            }
            if (in.available() != 0) {
                msg = Input.readLine();
                System.out.println(msg);
                if (msg.equals("noChatting")) {
                    userChat.dispose();
                    datagramSocket.close();
                    return;
                }
            }
        }
    }


    public static void main(String[] args) throws InterruptedException, IOException {
        loginWindow frame = new loginWindow();
        frame.setContentPane(frame.getLoginPanel());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);

        // default values
        port = 5000;
        String serverAddress = "localhost";
        String userName = "Anonymous";

        userName = checkUsername(frame, userName);

        // create the Client object
        Client client = new Client(serverAddress, port, userName);
        // test if we can start the connection to the Server
        // if it failed nothing we can do
        while (!client.start(frame)) {
            frame.alert("Błąd logowania!");
            return;
        }

        frame.setVisible(false);
        userFrame = new userWindow();
        userFrame.setUsernameLabel(username);
        userFrame.setContentPane(userFrame.getUserPanel());
        userFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        userFrame.pack();
        userFrame.setVisible(true);

        client.sendMessage("who");

        String request;
        while(true) {
            TimeUnit.MILLISECONDS.sleep(1);
            if ((request = checkConnection(userFrame)) != "" && request != null) {
                System.out.println("Request to " + request);
                client.sendMessage("request " + request);
            }
        }

        /*
        // wait for messages from user
        Scanner scan = new Scanner(System.in);
        // loop forever for message from the user
        while (true) {
            System.out.print("> ");
            // read message from user
            String msg = scan.nextLine();
            // split for request function
            String[] request = msg.split(" ");
            // logout if message is LOGOUT
            if (msg.equalsIgnoreCase("LOGOUT")) {
                client.sendMessage(msg);
                break;
            }
            client.sendMessage(msg);
        }

        // done disconnect
        client.disconnect();
        */
    }

    public boolean start(loginWindow frame) {
        //Attempt to connect to server
        try {
            socket = new Socket(server, port);
        } catch (Exception ec) {
            display("Error connecting to server: " + ec);
            return false;
        }
        String message = "Connection Accepted " + socket.getInetAddress() + ":" + socket.getPort() + "\n";
        display(message);


        //Create the data streams
        try {
            in = socket.getInputStream();
            Input = new BufferedReader(new InputStreamReader(in));
            Output = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException eIO) {
            display("Exception when creating new Streams: " + eIO);
            return false;
        }
        //Send the username to the server and check for any issues
        try {
            String newUsername = username;

            Output.println(username);
            String conflict = Input.readLine();
            if (conflict.equalsIgnoreCase("true")) {
                P2Pport = Integer.parseInt(Input.readLine());
                System.out.println("P2P port: " + P2Pport);
                datagramSocket = new DatagramSocket(P2Pport);
                System.out.println(username);
                messageToServer = true;
            } else {
                frame.alert("Ta nazwa użytkownika jest już zajęta!");
            }

            while (!messageToServer) {
                if(!username.equals(newUsername)){
                    Output.println(newUsername);
                    conflict = Input.readLine();
                    if (conflict.equalsIgnoreCase("true")) {
                        username=newUsername;
                        P2Pport = Integer.parseInt(Input.readLine());
                        System.out.println("P2P port: " + P2Pport);
                        datagramSocket = new DatagramSocket(P2Pport);
                        System.out.println(username);
                        messageToServer = true;
                        break;
                    } else {
                        frame.alert("Ta nazwa użytkownika jest już zajęta!");
                    }
                }
                //username = newUser.next();
                newUsername = checkUsername(frame,username);
            }
        } catch (IOException eIO) {
            display("Exception during login : " + eIO);
            disconnect();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        new ListenFromServer().start();
        return true;
    }

    private void display(String msg) {
        System.out.println(msg);
    }

    // Sends a message to the server
    void sendMessage(String msg) {
        Output.println(msg);
    }

    // An issue occurs and all streams are closed
    private void disconnect() {
        try {
            if (Input != null) Input.close();
        } catch (Exception e) {
        } // not much else I can do
        try {
            if (Output != null) Output.close();
        } catch (Exception e) {
        } // not much else I can do
        try {
            if (socket != null) socket.close();
        } catch (Exception e) {
        } // not much else I can do
    }

    class ListenFromServer extends Thread {
        public void run() {
            while (messageToServer) {
                try {
                    String msg = Input.readLine();
                    String[] users = msg.split("//");
                    System.out.println(msg);

                    if (msg.equals("Connection Established.")) {
                        msg = Input.readLine();
                        System.out.println(msg);
                        int portFrom = Integer.parseInt(msg);

                        msg = Input.readLine();
                        System.out.println(msg);
                        String to = msg;

                        System.out.println("port: " + P2Pport);
                        System.out.println("portFrom: " + portFrom);

                        userChat userChat = new userChat(datagramSocket, P2Pport, portFrom);
                        userChat.setTitle(to);
                        userChat.setContentPane(userChat.getChatPanel());
                        userChat.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                        userChat.pack();
                        userChat.setVisible(true);

                        checkDispose(userChat);

                    } else if (msg.equals("Connection Accepted.")) {
                        msg = Input.readLine();
                        System.out.println(msg);
                        int portFrom = Integer.parseInt(msg);

                        msg = Input.readLine();
                        System.out.println(msg);
                        String to = msg;

                        System.out.println("port: " + P2Pport);
                        System.out.println("portFrom: " + portFrom);

                        userChat userChat = new userChat(datagramSocket, P2Pport, portFrom);
                        userChat.setTitle(to);
                        userChat.setContentPane(userChat.getChatPanel());
                        userChat.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                        userChat.pack();
                        userChat.setVisible(true);

                        checkDispose(userChat);

                    } else if (users.length == 3){
                        userFrame.addUsers(users);

                    } else if (users.length == 2 && users[0].equals("request")){
                        userFrame.request(users[1], username);
                        setRequest();
                    } else if(users.length == 2 && users[1].equals("nowChatting")){
                        userFrame.alert(users[0] + " już z kimś pisze!");
                    }/* else if(msg.equals("noChatting")){
                        System.out.println("zamknij");
                        userChat.dispose();
                    } */
                } catch (IOException e) {
                    display("Server has closed the connection: " + e);
                    break;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class ListenFromClient extends Thread {
        public void run() {
            while (true) {
                try {
                    String msg = Input.readLine();
                    String[] users = msg.split("//");
                    System.out.println(msg);

                    if (msg.equals("Connection Established.")) {
                        msg = Input.readLine();
                        System.out.println(msg);
                        int portFrom = Integer.parseInt(msg);

                        msg = Input.readLine();
                        System.out.println(msg);
                        String to = msg;

                        System.out.println("port: " + port);
                        System.out.println("portFrom: " + portFrom);


                        userChat userChat = new userChat(datagramSocket, port, portFrom);
                        userChat.setTitle(to);
                        userChat.setContentPane(userChat.getChatPanel());
                        userChat.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                        userChat.pack();
                        userChat.setVisible(true);

                        checkDispose(userChat);

                    } else if (msg.equals("Connection Accepted.")) {
                        msg = Input.readLine();
                        System.out.println(msg);
                        int portFrom = Integer.parseInt(msg);

                        msg = Input.readLine();
                        System.out.println(msg);
                        String to = msg;

                        System.out.println("port: " + port);
                        System.out.println("portFrom: " + portFrom);
                        datagramSocket = new DatagramSocket(port);

                        userChat userChat = new userChat(datagramSocket, port, portFrom);
                        userChat.setTitle(to);
                        userChat.setContentPane(userChat.getChatPanel());
                        userChat.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                        userChat.pack();
                        userChat.setVisible(true);

                        checkDispose(userChat);

                    } else if (users.length == 3){
                        userFrame.addUsers(users);

                    } else if (users.length == 2 && users[0].equals("request")){
                        userFrame.request(users[1], username);
                        setRequest();
                    } else if(users.length == 2 && users[1].equals("nowChatting")){
                        userFrame.alert(users[0] + " już z kimś pisze!");
                    }/* else if(msg.equals("noChatting")){
                        System.out.println("zamknij");
                        userChat.dispose();
                    } */
                } catch (IOException e) {
                    display("Server has closed the connection: " + e);
                    break;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}