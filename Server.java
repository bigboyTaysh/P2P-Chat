/* Server.java */
import java.net.*;
import java.io.*;
import java.util.*;
import java.text.SimpleDateFormat;

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

    //Display an event to the console (Server Only)
    private void display(String prompt) {
        String time = theDate.format(new Date()) + " " + prompt;
        System.out.println(time);
    }

    private synchronized void broadcast(String message) {
        String time = theDate.format(new Date());
        String toSend = time + " " + message;
        System.out.println(toSend);
        for(int i = clientList.size(); --i >= 0;) {
            ClientThread temp = clientList.get(i);
            if(!temp.writeMsg(toSend)){
                clientList.remove(i);
                display("Disconnected Client " + temp.username + " removed from list.");
            }
        }
    }

    private void request(String source, String target) throws InterruptedException {
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
                /*
                try{
                    destination.answer = destination.Input.readLine(); //This is where the program hangs
                }catch(IOException e){
                    System.out.println("Exception on reading from target: " + e);
                }
                */
                destination.beingRequested = true;


                //Loop until response found
                while(true){
                    Thread.sleep(1);
                    if(destination.requestResponse != null){
                        if(destination.requestResponse.equalsIgnoreCase("Y")){
                            destination.Output.println("Connection Established.");
                            destination.Output.println(origin.Port);
                            destination.Output.println(origin.username);



                            origin.Output.println("Connection Accepted.");
                            origin.Output.println(destination.Port);
                            origin.Output.println(destination.username);

                            destination.beingRequested = false;
                            origin.beingRequested = false;

                            destination.chatting = true;
                            origin.chatting = true;

                            destination.chatWith = origin.username;
                            origin.chatWith = destination.username;
                            break;
                        }else if (destination.requestResponse.equalsIgnoreCase("N")){
                            destination.beingRequested = false;
                            break;
                        }
                    }
                }
                System.out.println("Reading done.");

                break;
            }
        }
    }

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

    public static void main(String[] args){
        int portNumber = 5000; //Listen on this port
        //Create a new Server and start it
        Server server = new Server(portNumber);
        server.start();
    }

    //Threads for each instance of a client
    class ClientThread extends Thread {
        Socket socket; // The listening/talk socket connection
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
        String answer;
        boolean chatting;
        String chatWith;


        //Constructor
        ClientThread(Socket socket) {

            id = ++uniqueID;

            System.out.println("id: " + id);
            this.socket = socket;
            //Create the data streams
            System.out.println("Thread is attempting to create Data Streams.");
            try {
                Output = new PrintWriter(socket.getOutputStream(),true);
                Input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                Address = socket.getInetAddress().toString();
                Port = socket.getPort();
                System.out.println("Address is: " + Address);
                System.out.println("Port is: " + Port);

                // Read the username
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
                    clientList.get(i).writeMsg(username + "//" + Port + "//" + date);
                }
            }
        }

        //Run this forever, read and broadcast
        public void run() {
            boolean keepGoing = true;
            String found = null;
            while(keepGoing) {
                try {
                    found = Input.readLine();

                    if(beingRequested == true &&
                            (found.equalsIgnoreCase("Y") || found.equalsIgnoreCase("N"))){
                        requestResponse = found;
                        found = Input.readLine();
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
                        String requestUsername;
                        for(int i = 0; i < clientList.size(); i++) {
                            //If the Source was found
                            if(clientList.get(i).username.equalsIgnoreCase(separated[1])){
                                if(!clientList.get(i).chatting) {
                                    try {
                                        request(username, separated[1]);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    Output.println(clientList.get(i).username + "//nowChatting");
                                }
                                break;
                            }
                        }
                    }else {
                        Output.println("[SERVER] Correct usage is <request [username]>");
                    }
                }else if(separated[0].equalsIgnoreCase("noChatting") && separated.length == 1){
                    chatting=false;
                    for(int i = 0; i < clientList.size(); i++) {
                        //If the Source was found
                        if(clientList.get(i).username.equalsIgnoreCase(chatWith)){
                            clientList.get(i).chatting=false;
                            System.out.println("zamknij okno " + clientList.get(i).username);
                            clientList.get(i).Output.println("noChatting");
                        }
                    }
                } else if(separated[0].equalsIgnoreCase("LOGOUT") && separated.length == 1){
                    display(username + " has disconnected from the Server.");
                    keepGoing = false;
                }else if(separated[0].equalsIgnoreCase("WHO") && separated.length == 1){
                    for(int i = 0; i < clientList.size(); ++i) {
                        if(!(username.equals(clientList.get(i).username))){
                            ClientThread eachClient = clientList.get(i);
                            writeMsg(eachClient.username + "//" + eachClient.Port + "//" + eachClient.date);
                        }
                    }
                }else {
                    //broadcast(username + ": " + found);
                }
            }
            //Clean up the user list
            remove(id);
            close();
        }

        // try to close everything
        private void close() {
            // try to close the connection
            try {
                if(Output != null) Output.close();
            }
            catch(Exception e) {}
            try {
                if(Input != null) Input.close();
            }
            catch(Exception e) {};
            try {
                if(socket != null) socket.close();
            }
            catch (Exception e) {}
        }

        // Write the string to the Client Output
        private boolean writeMsg(String toSend) {
            if(!socket.isConnected()){
                close();
                return false;
            }
            Output.println(toSend);
            return true;
        }
    }
}