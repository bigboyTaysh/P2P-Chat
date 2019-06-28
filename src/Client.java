/* Client.java */

import javax.swing.*;
import java.net.*;
import java.io.*;
import java.util.concurrent.TimeUnit;

/**
 * klasa reprezentująca klienta łączącego się z serwerem oraz innymi użytkownikami
 */
public class Client {
    //For the I/O
    private static BufferedReader Input;
    private static InputStream in;
    private static String username;
    private static int port;
    private static userWindow userFrame;
    private PrintWriter Output;
    private Socket socket;
    private DatagramSocket datagramSocket;
    private String server;
    private int P2Pport;
    private boolean messageToServer;
    private userChat userChat;

    private ListenFromServer listenFromServer;

    /**
     * konstruktor przypisujący parametry do zmiennych
     *
     * @param server   adres servera
     * @param port     port na którym nasłuchuje server
     * @param username nazwa użytkownika
     */
    Client(String server, int port, String username) {
        this.server = server;
        this.port = port;
        this.username = username;
    }

    /**
     * wywołanie okna logowania
     * przypisanie nazwy użytownika
     * utworzenie obkietku klasy Client
     * utworzenie okna użytkownika
     * połączenie między użytkownikami
     */
    public static void main(String[] args) throws InterruptedException, IOException {
        loginWindow frame = new loginWindow();
        frame.setContentPane(frame.getLoginPanel());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        // default values
        port = 5000;
        String serverAddress = "localhost";
        String userName;

        while (true) {
            userName = checkUsername(frame);
            TimeUnit.MILLISECONDS.sleep(1);
            if (!userName.equals("") && !userName.contains(" ")) {
                break;
            } else {
                frame.alert("Nieprawidłowy login!");
            }
        }

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
        userFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        userFrame.pack();
        userFrame.setResizable(false);
        userFrame.setLocationRelativeTo(null);
        userFrame.setVisible(true);

        client.sendMessage("who");

        String request;
        while (true) {
            request = checkConnection(userFrame, client);
            TimeUnit.MILLISECONDS.sleep(1);
            if (request != "" && request != null) {
                System.out.println("Request to " + request);
                client.sendMessage("request " + request);
            }
        }
    }

    /**
     * metoda pobierająca podaną nazwę użytkownika po kliknięciu przycisku
     *
     * @param frame obiekt klasy loginWindow - okno logowania
     */
    private static String checkUsername(loginWindow frame) throws InterruptedException {
        String userName;
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

    /**
     * pobiera i zwraca nazwę zaznaczonego użytkonika na liście po kliknięciu na przycisk lub
     * wylogowuje użytkownika z serwera i zamyka program
     *
     * @param userFrame obiekt klasy userWindow - okno użytkownika
     * @param client    obiekt klasy Client
     * @return nazwa zaznaczonego uzytkownika
     */
    private static String checkConnection(userWindow userFrame, Client client) throws InterruptedException, IOException {
        String userName;
        while (true) {
            TimeUnit.MILLISECONDS.sleep(1);
            //Thread.sleep(1);
            if (userFrame.isClicked()) {
                userName = userFrame.getUserName();
                userFrame.setClicked(false);
                break;
            } else if (userFrame.isClicked3()) {
                client.getListenFromServer().interrupt();
                userName = null;
                userFrame.setClicked3(false);
                userFrame.dispose();
                client.Output.println("LOGOUT");
                client.disconnect();
                System.exit(0);
                break;
            }
        }
        return userName;
    }

    /**
     * metoda sprawdzająca wynik potwiedzenia próby połączenia od innego użytkownika
     *
     * @param userFrame obiekt klasy userWindow
     * @return wartość logiczna
     */
    private static boolean checkRequest(userWindow userFrame) throws InterruptedException {
        boolean check;
        while (true) {
            TimeUnit.MILLISECONDS.sleep(1);
            //Thread.sleep(1);
            if (userFrame.isClicked2()) {
                check = userFrame.isRequset();
                if (check) {
                    userFrame.setClicked2(false);
                    userFrame.setRequset(false);
                    return true;
                } else {
                    userFrame.setClicked2(false);
                    return false;
                }
            }
        }
    }

    /**
     * połączenie z serverem
     * utworzenie socketu i streamów klieta
     * przypisanie nazwy użytkownika po zatwierdzeniu jej przez serwer
     * utworzenie obiektu klasy ListenFromServer dziedziczącej po klasie Thread
     * odpowiedzialnej za nasłuchiwaniu przychodzących wiadomości i komend od serwera
     *
     * @param frame obiekt klasy loginWindow
     * @return wartość logiczna
     */
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
                System.out.println(username);
                messageToServer = true;
            } else {
                frame.alert("Ta nazwa użytkownika jest już zajęta!");
            }

            while (!messageToServer) {
                if (!username.equals(newUsername)) {
                    Output.println(newUsername);
                    conflict = Input.readLine();
                    if (conflict.equalsIgnoreCase("true")) {
                        username = newUsername;
                        P2Pport = Integer.parseInt(Input.readLine());
                        System.out.println("P2P port: " + P2Pport);
                        System.out.println(username);
                        messageToServer = true;
                        break;
                    } else {
                        frame.alert("Ta nazwa użytkownika jest już zajęta!");
                    }
                }
                while (true) {
                    newUsername = checkUsername(frame);
                    TimeUnit.MILLISECONDS.sleep(1);
                    if (!newUsername.equals("") && !newUsername.contains(" ")) {
                        break;
                    } else {
                        frame.alert("Nieprawidłowy login!");
                    }
                }
            }
        } catch (IOException eIO) {
            display("Exception during login : " + eIO);
            disconnect();
            return false;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        listenFromServer = new ListenFromServer();
        listenFromServer.start();
        return true;
    }

    /**
     * metoda zwracajaca obiekt klasy ListenFromServer
     *
     * @return obiekt klasy ListenFromServer
     */
    public ListenFromServer getListenFromServer() {
        return listenFromServer;
    }

    /**
     * metoda wysyła do serwera odpowiedź użytkwonika na próbę połączenia
     */
    private void setRequest() throws InterruptedException {
        boolean check;
        while (true) {
            check = checkRequest(userFrame);
            if (check) {
                System.out.println("Request to true");
                Output.println("Y");
                break;
            } else {
                System.out.println("Request to false");
                Output.println("N");
                break;
            }
        }
    }

    /**
     * metoda sprawdza czy użytkownik(bądź połączony użytkownik) zamknął okno chatu
     */
    private void checkDispose() throws InterruptedException, IOException {
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
                return;
            }
        }
    }

    /**
     * wyświetlanie wiadomości na konsoli
     *
     * @param msg wiadomość do wyświetlenia
     */
    private void display(String msg) {
        System.out.println(msg);
    }

    /**
     * wysłanie wiadomości do serwera
     *
     * @param msg wiadomość do wysłania
     */
    // Sends a message to the server
    void sendMessage(String msg) {
        Output.println(msg);
    }

    /**
     * metoda zamyka strumienie i sockety klienta
     */
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
        try {
            if (datagramSocket != null) datagramSocket.close();
        } catch (Exception e) {
        } // not much else I can do
    }

    /**
     * klasa dziedzicząca po klasie Thread odpowiedzialna za nasłuchiwanie przychodzących wiadomości i komend od serwera
     * odpowiednio reaguje na przychodzące wiadomości/komendy
     * tworzy okno czatu po zatwiedzeni połączenia z innym użytkownikiem
     */
    class ListenFromServer extends Thread {
        public void run() {
            String msg;
            String[] users = new String[0];

            while (true) {
                try {
                    msg = Input.readLine();
                    if (msg != null) {
                        users = msg.split("//");

                        if (msg.equals("Connection Established.")) {
                            msg = Input.readLine();
                            int portFrom = Integer.parseInt(msg);

                            msg = Input.readLine();
                            String to = msg;

                            System.out.println("port: " + P2Pport);
                            System.out.println("portFrom: " + portFrom);

                            datagramSocket = new DatagramSocket(P2Pport);

                            userChat = new userChat(datagramSocket, portFrom);
                            userChat.setTitle(to);
                            userChat.setContentPane(userChat.getChatPanel());
                            userChat.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                            userChat.pack();
                            userChat.setResizable(false);
                            userChat.setLocationRelativeTo(null);
                            userChat.setVisible(true);

                            checkDispose();

                        } else if (msg.equals("Connection Accepted.")) {
                            msg = Input.readLine();
                            int portFrom = Integer.parseInt(msg);

                            msg = Input.readLine();
                            String to = msg;

                            System.out.println("port: " + P2Pport);
                            System.out.println("portFrom: " + portFrom);

                            datagramSocket = new DatagramSocket(P2Pport);

                            userChat = new userChat(datagramSocket, portFrom);
                            userChat.setTitle(to);
                            userChat.setContentPane(userChat.getChatPanel());
                            userChat.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                            userChat.pack();
                            userChat.setResizable(false);
                            userChat.setLocationRelativeTo(null);
                            userChat.setVisible(true);

                            checkDispose();

                        } else if (users.length == 3) {
                            userFrame.addUsers(users);
                        } else if (users.length == 2 && users[0].equals("request")) {
                            userFrame.request(users[1], username);
                            setRequest();
                        } else if (users.length == 2 && users[1].equals("nowChatting")) {
                            userFrame.alert(users[0] + " już z kimś pisze!");
                        } else if (users.length == 2 && users[1].equals("LOGOUT")) {
                            userFrame.removeUser(users[0]);
                        }
                    }
                } catch (IOException e) {
                    break;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}