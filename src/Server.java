/* Server.java */
import java.net.*;
import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;

/**
 * Klasa Server odpowiedzialna za przyjmowanie klienta(logowanie) i danie mu możliwości korzystania z jego zasobów tzn.
 * pobieranie listy zalogowanych użytkowników i ich parametrów(nazwa, port, godzina zalogowania)
 * oraz zestawianie użytkowników do wspólnej konwersacji
 */
// The Server
public class Server {
    private static int uniqueID;
    private ArrayList<ClientThread> clientList; //Keep track of all clients
    private SimpleDateFormat theDate; //For displaying time and date
    private int port; //The port to listen on
    private boolean control; //Decides whether to keep going

    //Constructor that is given a specified port
    public Server(int port) {
        this.port = port; //Set the port
        theDate = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy"); //Set the time
        clientList = new ArrayList<ClientThread>();
    }

    /**
     * metoda odpowiedzialna za nasłuchwianie na odpowiednim porcie przychodzących połączeń,
     * tworzenie socketów oraz strumieni do nowo połączonych użytkowników oraz dodanie ich do listy wątków
     */
    //Start the connection
    public void start() {
        control = true;
        //Create the socket and wait for a client to request
        try {
            ServerSocket serverSocket = new ServerSocket(port);

            //Loop forever to listen for client requests
            while(control){
                display("Server is listening for Clients on port " + port + ".");
                Socket socket = serverSocket.accept(); //Pause and wait for connection
                System.out.println(socket.getPort());

                //If listening has stopped
                if(!control) break;
                ClientThread newThread = new ClientThread(socket);
                clientList.add(newThread);
                newThread.start();

            }
            //Closing the server down due to break in loop
            try {
                serverSocket.close();
                for(int i = 0; i < clientList.size(); i++) {
                    ClientThread temp = clientList.get(i);
                    try {
                        temp.Input.close();
                        temp.Output.close();
                        temp.in.close();
                        temp.socket.close();
                    }
                    catch(IOException IOe){
                        //For safety
                    }
                }
            }
            catch(Exception e) {
                display ("Closing the Server and Clients: " + e);
            }

        }
        catch (IOException e) {
            String message = theDate.format(new Date()) + " Error on new ServerSocket: " + e + "\n";
            display(message);
        }
    }

    /**
     * @param prompt wiadomość do wyświetlenia w konsoli serwera
     * metoda do wyświetlenia daty wraz z wiadomością w konsoli serwera
     */
    //Display an event to the console (Server Only)
    private void display(String prompt) {
        String time = theDate.format(new Date()) + " " + prompt;
        System.out.println(time);
    }

    /**
     * @param source nazwa użytkownika, który wywołał komendę "request"
     * @param target nazwa użytkownika, do którego source wysyła zapytanie o potwierdzenie połączenia
     */
    private void request(String source, String target) throws InterruptedException{
        ClientThread origin = null;
        ClientThread destination = null;
        //Find the ClientThread of the Soruce
        for(int i = 0; i < clientList.size(); i++) {
            origin = clientList.get(i);
            //If the Source was found
            if(origin.username.equalsIgnoreCase(source)){
                break;
            }
        }

        // Find the ClientThread of the Target
        for(int i = 0; i < clientList.size(); i++) {
            destination = clientList.get(i);
            //If the target was found
            if(destination.username.equalsIgnoreCase(target)){
                destination.Output.println("request//" + source);
                System.out.println(source + "//request to " + target);
                System.out.println("Attempting to read...");

                destination.beingRequested = true;


                //Loop until response found
                while(true){
                    Thread.sleep(1);
                    if(destination.requestResponse != null){
                        if(destination.requestResponse.equalsIgnoreCase("Y")){
                            //params for destination clinet
                            destination.Output.println("Connection Established.");
                            destination.Output.println(origin.Port);
                            destination.Output.println(origin.username);


                            //params for origin client
                            origin.Output.println("Connection Accepted.");
                            origin.Output.println(destination.Port);
                            origin.Output.println(destination.username);

                            destination.beingRequested = false;

                            //set chatting variables
                            destination.chatting = true;
                            origin.chatting = true;

                            destination.chatWith = origin.username;
                            origin.chatWith = destination.username;

                            destination.requestResponse = null;
                            break;
                        }else if (destination.requestResponse.equalsIgnoreCase("N")){
                            destination.beingRequested = false;
                            destination.requestResponse = null;
                            break;
                        }
                    }
                }
                System.out.println("Reading done.");

                break;
            }
        }
    }

    /**
     * metoda usuwająca użytkownika z listy wątków
     * @param id id użytkownika usuwanego z listy wątków
     */
    //For removing a client who requested LOGOUT
    synchronized void remove(int id) {
        for(int i = 0; i < clientList.size(); i++) {
            ClientThread temp = clientList.get(i);
            //It was found
            if(temp.id == id) {
                clientList.remove(i);
                return;
            }
        }
    }

    /**
     * przypisanie portu na jakim serwer będzie nasłuchiwał i utworzenie obiektu Server
     */
    public static void main(String[] args){
        int portNumber = 5000; //Listen on this port
        //Create a new Server and start it
        Server server = new Server(portNumber);
        server.start();
    }

    /**
     * wewnętrzna klasa dziedzicząca po klasie thread, reprezentuje wątek klienta
     */
    //Threads for each instance of a client
    class ClientThread extends Thread {
        Socket socket; // The listening/talk socket connection
        InputStream in;
        OutputStream out;
        BufferedReader Input;
        PrintWriter Output;
        int id; //Unique ID
        String username; //Username
        String date;
        SimpleDateFormat date2;
        String Address;
        int Port;
        Boolean duplicate = false; //Duplicate flag
        Boolean beingRequested = false; //A client is attempting to connect
        String requestResponse = null; //Client's response to request
        boolean chatting; //to check than client chatting now
        String chatWith; //client chat with who

        /**
         * konstruktor w którym tworzone są streamy od podanego w parametrze socketu i przypisanie do zmiennych
         * sprawdzenie przesłanego loginu od klienta czy już istnieje i odesłanie wartości zmiennej logicznej
         * rozesłanie do wszystkich zalogowanych użytkowników nazwy, portu i daty zalogowania nowego użytkownika
         * @param socket socket połączonego klienta
         */
        //Constructor
        ClientThread(Socket socket) {
            id = ++uniqueID;

            System.out.println("id: " + id);
            this.socket = socket;
            //Create the data streams
            System.out.println("Thread is attempting to create Data Streams.");
            try {
                in = socket.getInputStream();
                out = socket.getOutputStream();
                Output = new PrintWriter(out,true);
                Input = new BufferedReader(new InputStreamReader(in));
                Address = socket.getInetAddress().toString();
                Port = socket.getPort();
                System.out.println("Address is: " + Address);
                System.out.println("Port is: " + Port);

                // Read the username and check duplicate username
                while(!duplicate){
                    username = Input.readLine();
                    for(int i = 0; i < clientList.size(); i++){
                        if(username.equalsIgnoreCase(clientList.get(i).username)) {
                            duplicate = true;
                        }
                    }
                    if(!duplicate) break;
                    display("Username conflict detected.");
                    Output.println("false");
                    duplicate = false; //Reset the boolean to check the new username given
                }
                Output.println("true");
                display(username + " just connected.");
                Output.println(Port);
                chatting = false;
            }
            catch (IOException e) {
                display("Exception creating new Input/output Streams: " + e);
                return;
            }
            date2 = new SimpleDateFormat("HH:mm:ss dd.MM.yyyy");
            date = date2.format(new Date());

            for(int i = 0; i < clientList.size(); ++i) {
                if(!(username.equals(clientList.get(i).username))){
                    clientList.get(i).Output.println(username + "//" + Port + "//" + date); // send to all that client login
                }
            }
        }

        /**
         * ciało wątku obsługującego przysłane wiadomości i komendy od zalogowanych użytkowników
         * po otrzymaniu odpowiedniej komendy wywołanie metod odpowiedzialnych za zamknięcie strumieni, socketów oraz usunięcie
         * klienta z klisty wątków
         */
        //Run this forever, read and broadcast
        public void run() {
            boolean keepGoing = true;
            String found = null;
            while(keepGoing) {
                try {
                    found = Input.readLine(); //read from client
                    if(beingRequested == true &&
                            (found.equalsIgnoreCase("Y") || found.equalsIgnoreCase("N"))){  //check that client response to request
                        requestResponse = found; //set response
                    }
                }
                catch (IOException e) {
                    display(username + " Exception reading Stream: " + e);
                    break;
                }
                //Parse the message recieved from the client
                String[] separated = found.split(" ");
                    if(separated[0].equalsIgnoreCase("Request")){
                        if(separated.length == 2){
                            for(int i = 0; i < clientList.size(); i++) {
                                //If the Source was found
                                if(clientList.get(i).username.equalsIgnoreCase(separated[1])){
                                    if(!clientList.get(i).chatting) { //check that found client is chatting now
                                        try {
                                            request(username, separated[1]);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        Output.println(clientList.get(i).username + "//nowChatting");
                                    }
                                }
                            }
                        }
                    }else if(separated[0].equalsIgnoreCase("noChatting") && separated.length == 1){ //comand to set user no chat
                        chatting = false;
                        for(int i = 0; i < clientList.size(); i++) {
                            //If the Source was found
                            if(clientList.get(i).username.equalsIgnoreCase(chatWith)){
                                clientList.get(i).chatting=false;
                                clientList.get(i).Output.println("noChatting");
                            }
                        }
                    } else if(separated[0].equalsIgnoreCase("LOGOUT") && separated.length == 1){ // logout user and close streams,socket
                        display(username + " has disconnected from the Server.");
                        for(int i = 0; i < clientList.size(); ++i) {
                            if(!(username.equals(clientList.get(i).username))){
                                clientList.get(i).Output.println(username + "//LOGOUT");
                            }
                        }
                        close();
                        break;
                    }else if(separated[0].equalsIgnoreCase("WHO") && separated.length == 1){ //send list of online users
                        for(int i = 0; i < clientList.size(); ++i) {
                            if(!(username.equals(clientList.get(i).username))){
                                Output.println(clientList.get(i).username + "//" + clientList.get(i).Port + "//" + clientList.get(i).date);
                            }
                        }
                    }
            }
            //Clean up the user list
            remove(id);
            close();
        }

        /**
         * metoda zamykająca strumienie oraz socket przypisany do wątku
         */
        // try to close everything
        private void close() {
            // try to close the connection
            try {
                if(Output != null) Output.close();
            }
            catch(Exception e) {}
            try {
                if(Input != null) Input.close();
            } catch(Exception e) {}
            try {
                if(in != null) in.close();
            }
            catch(Exception e) {}
            try {
                if(socket != null) socket.close();
            }
            catch (Exception e) {}
        }

        private String read(String msg) throws IOException{
            int k = 0;
            StringBuffer sb = new StringBuffer();
            while ((k = in.read()) != -1 && k != '\n')
                sb.append((char) k);

            msg = sb.toString();
            return msg;
        }

        /**
         * @param toSend wiadomości do wysłania
         * @return zwracana wartość logiczna, status wykonania metody
         */
        // Write the string to the Client Output
        private boolean writeMsg(String toSend) throws IOException {
            if(!socket.isConnected()){
                close();
                return false;
            }
            Output.println(toSend);
            return true;
        }
    }
}