/************************************************
 *
 * Author: Sheldon
 * Assignment: Program 2
 * Class: CSI 4321
 *
 ************************************************/
package addatude.app.client;

import addatude.serialization.*;
import addatude.serialization.Error;

import java.io.*;
import java.net.Socket;
import java.util.Objects;

import static java.lang.System.exit;

/**
 * The client for the project
 *
 * @version 1.1
 * @author Sheldon Smith
 */
public class Client {
    private static Socket socket;
    private static InputStream inSocket;
    private static OutputStream outSocket;

    /**
     * The main function that runs the client side interactions
     *
     * @param args the arguments from the command line
     */
    public static void main(String[] args) {
        //Create a list of strings to keep track of the input
        final String[] inputList = {"Operation", "Map ID", "User ID", "Longitude", "Latitude", "Location Name", "Location Description"};
        int currInput = 0;

        // Test for correct number of arguments
        if (args.length != 2) {
            throw new IllegalArgumentException("Parameter(s): <Server> <Port>");
        }

        String server = args[0];  // Server name or IP address
        int servPort;
        try {
            servPort = Integer.parseInt(args[1]); // Server port
        } catch (NumberFormatException e){
            System.err.println("Illegal Given Port: " + args[1]);
            return;
        }

        //Start connection to the server
        startConnection(server, servPort);

        MessageInput in = new MessageInput(inSocket);

        //Create variable to hold shouldContinue response
        String shouldContinue = null;

        //Start the protocol loop
        do {
            //Create a reader to get input from standard in
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String operation = null, name = null, desc = null, lng = null, lat = null;
            long mapID = 0, userID = 0;
            do {
                //Prompt the user to enter an option
                System.out.print(inputList[currInput] + "> ");
                String response = "";
                try {
                    //Read in the response from the user
                    response = reader.readLine();
                } catch (IOException e){
                    System.err.println("Error reading from standard in");
                    exit(1);
                }
                //Figure out which input that the user is entering
                switch (currInput){
                    case 0:
                        //Check that the operation is valid
                        if (!"ALL".equals(response) && !"NEW".equals(response) && !"LOCAL".equals(response)){
                            System.err.println("Unknown operation");
                            currInput--;
                        } else {
                            //Set the operation if valid
                            operation = response;
                        }
                        break;
                    case 1:
                    case 2:
                        //Check validation for mapID and userID
                        try{
                            long num = Long.parseLong(response);
                            if (Validation.isInvalidNumber(num)){
                                System.err.println("Illegal " + inputList[currInput]);
                                currInput--;
                            } else if (currInput == 1){ //Set the correct value if valid
                                mapID = num;
                                if ("ALL".equals(operation)){currInput = 7;}
                            } else {
                                userID = num;
                            }

                            //Skip to prompting for longitude if LOCAL operation
                            if (currInput == 1 && "LOCAL".equals(operation)){
                                currInput = 2;
                            }
                        } catch (NumberFormatException e){
                            //If an exception is thrown, print an error message
                            System.err.println("Illegal " + inputList[currInput]);
                            currInput--;
                        }
                        break;
                    case 3:
                        //Check that the entered longitude is valid
                        if (Validation.isInvalidLongitude(response)){
                            System.err.println("Illegal Longitude");
                            currInput--;
                        } else {
                            //Set the longitude if valid
                            lng = response;
                        }
                        break;
                    case 4:
                        //Check that the entered latitude is valid
                        if (Validation.isInvalidLatitude(response)){
                            System.err.println("Illegal Latitude");
                            currInput--;
                        } else {
                            //Set the latitude if valid
                            lat = response;
                        }
                        if ("LOCAL".equals(operation)){currInput = 7;}
                        break;
                    case 5:
                    case 6:
                        //Check that the entered name/description is valid
                        if (Validation.isInvalidString(response)){
                            System.err.println("Illegal " + inputList[currInput]);
                            currInput--;
                        } else if (currInput == 5){ //Set the correct value
                            name = response;
                        } else {
                            desc = response;
                        }
                        break;
                }
                //Increment which input that the user is inputting
                currInput++;
            } while (currInput < 7);

            try {
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                //If the operation is NEW, create a NewLocation
                if ("NEW".equals(operation)) {
                    //Create a new location with the entered values
                    NewLocation loc = new NewLocation(mapID, new LocationRecord(userID, lng, lat, name, desc));

                    //Encode the NewLocation
                    loc.encode(new MessageOutput(bout));
                } else if ("ALL".equals(operation)) {
                    //Create a LocationRequest
                    LocationRequest req = new LocationRequest(mapID);

                    //Encode the LocationRequest
                    req.encode(new MessageOutput(bout));
                } else {
                    //Create LocalLocationRequest
                    LocalLocationRequest req = new LocalLocationRequest(mapID, lng, lat);
                    //Encode the LocalLocationRequest
                    req.encode(new MessageOutput(bout));
                }
                //Send message to server
                outSocket.write(bout.toByteArray());

                //Get the server's response and interpret response
                interpretResponse(in);
            } catch (ValidationException | IOException e){
                System.err.println("Unable to communicate: " + e.getMessage());
                closeConnection();
                exit(1);
            }

            //Prompt the user if they would like to continue
            System.out.print("Continue (y)> ");
            try {
                shouldContinue = reader.readLine();
            } catch (IOException e){
                System.err.println("Error reading from standard in");
                closeConnection();
                exit(1);
            }
            currInput = 0;
        }while (Objects.equals(shouldContinue, "y"));

        closeConnection();  // Close the socket and its streams
    }

    /**
     * Start a connection to the server given the server and port
     *
     * @param server the server's name or ip address
     * @param servPort the server's port
     */
    public static void startConnection(String server, int servPort) {
        // Create socket that is connected to server on specified port
        try {
            socket = new Socket(server, servPort);

            //Create the input streams to communicate with the server
            inSocket = socket.getInputStream();
            outSocket = socket.getOutputStream();
        } catch (IOException e){
            //If unable to connect, print error and exit
            System.err.println("Unable to communicate: " + e.getMessage());
            closeConnection();
            exit(1);
        }
    }

    /**
     * Interprets the passed in message
     *
     * @param in the MessageInput to decode
     */
    public static void interpretResponse(MessageInput in) {
        //Create a message input with the byte[]
        try {
            //Try to decode the message from the server
            Message resMessage = Message.decode(in);

            //If the message is a LocationResponse, then output the LocationRecords
            if (resMessage.getClass().isAssignableFrom(LocationResponse.class)){
                //Decode LocationResponse
                LocationResponse res = (LocationResponse) resMessage;

                //Print Response
                System.out.println(res);
            } else if (resMessage.getClass().isAssignableFrom(Error.class)){
                //If the message is an Error, then print the error
                Error er = (Error) resMessage;
                System.err.println("Error: " + er.getErrorMessage());
            } else {
                if (resMessage.getClass().isAssignableFrom(NewLocation.class)){
                    NewLocation loc = (NewLocation) resMessage;
                    System.err.println("Unexpected message: " + loc);
                } else if (resMessage.getClass().isAssignableFrom(LocationRequest.class)){
                    LocationRequest req = (LocationRequest) resMessage;
                    System.err.println("Unexpected message: " + req);
                }
            }
        } catch (ValidationException e){
            //If the message is invalid, then print an error and exit
            System.err.println("Invalid message: " + e.getMessage());
            closeConnection();
            exit(1);
        }
    }

    /**
     * close the connection to the server
     */
    public static void closeConnection() {
        //Close the connection to the server and the streams
        try {
            inSocket.close();
            outSocket.close();
            socket.close();
        } catch (IOException e){
            //If an error occurs, print message and exit
            System.err.println("Unable to communicate: " + e.getMessage());
            exit(1);
        }
    }
}