/************************************************
 *
 * Author: Sheldon
 * Assignment: Program 2
 * Class: CSI 4321
 *
 ************************************************/
package addatude.app.server;

import addatude.serialization.*;
import addatude.serialization.Error;

import mapservice.MapManager;
import mapservice.MapBoxObserver;
import mapservice.Location;
import mapservice.MemoryMapManager;
import notifi.app.server.NoTiFiServer;

import java.net.*;  // for Socket, ServerSocket, and InetAddress
import java.io.*;   // for IOException and Input/OutputStream
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.*;

import static java.lang.System.exit;

/**
 * The server for the project
 *
 * @version 1.1
 * @author Sheldon Smith
 */
public class Server {
    private static final Logger logger = Logger.getLogger(Server.class.getName());
    static final int VALID_MAP_ID = 345;
    static final String VALID_MAP_NAME = "Class Map";
    static List<String[]> passwordFileInfoList = new ArrayList<>();
    /**The variable to hold the LocationResponse information*/
    public static LocationResponse res;
    /**The variable that controls adding locations to the map*/
    public static MapManager mapMgr;
    /**Variable to be used to lock the location response during modification*/
    public static final Object lock = new Object();

    /**The NoTiFiServer*/
    public static NoTiFiServer noTiFiServer;

    static {
        try {
            res = new LocationResponse(VALID_MAP_ID, VALID_MAP_NAME);

            //Set up the logger to send information to "server.log" file
            FileHandler fh = new FileHandler("server.log");
            fh.setFormatter(new SimpleFormatter());
            logger.addHandler(fh);
            logger.setUseParentHandlers(false);
            //logger.setLevel(Level.SEVERE);

            //Create map manager in memory
            mapMgr = new MemoryMapManager();
        } catch (ValidationException | IOException e) {exit(1);}
    }

    /**
     * File containing JS-encoded array of locations
     */
    private static final String LOCATIONFILE = "markers.js";

    /**
     * The main function that runs the server
     *
     * @param args the arguments from the command line
     */
    public static void main(String[] args) {
                //Test that the correct number of arguments is given
        if (args.length != 3) {
            logger.log(Level.SEVERE, () -> "Bad Parameter(s): <Port> <Num Threads> <Password File>");
            return;
        }

        //Read in the input from the command line
        int servPort, numThreads;
        try {
            servPort = Integer.parseInt(args[0]);
            numThreads = Integer.parseInt(args[1]);
        } catch (NumberFormatException e){
            //If error converting to integer, log exception and exit
            logger.log(Level.SEVERE, e, () -> "Illegal Argument");
            return;
        }

        //Start the NoTiFIServer
        noTiFiServer = new NoTiFiServer(servPort);
        noTiFiServer.start();

        String passwordFile = args[2];
        readInUserRecords(passwordFile);

        //Register listener to update MapBox location file
        mapMgr.register(new MapBoxObserver(LOCATIONFILE, mapMgr));

        try {
            // Create a server socket to accept client connection requests
            ServerSocket servSock = new ServerSocket(servPort);

            //Create the thread pool
            ExecutorService threadPool = Executors.newFixedThreadPool(numThreads);

            while (true) { // Run forever, accepting and servicing client connections
                Socket clientSock = servSock.accept();     // Get client connection
                //Execute the connection through the thread pool
                threadPool.execute(new ClientThread(clientSock));
            }
        } catch (IOException e){
            logger.log(Level.SEVERE, e, () -> "Unable to Create ServerSocket");
        } catch (IllegalArgumentException e){
            logger.log(Level.SEVERE, e, () -> "Invalid Argument for Thread Pool");
        }
        /* NOT REACHED */
    }

    /**
     * reads in the userID - user name pairs from the file and stores them
     *
     * @param filename the name of the file
     */
    public static void readInUserRecords(String filename){
        //Try to open the password file
        logger.log(Level.INFO, () -> "Parsing the Password File");
        try {
            BufferedReader fileScanner = new BufferedReader(new FileReader(filename));

            String line;
            //Read in records into record list
            while ((line = fileScanner.readLine()) != null){
                String[] tempArr = line.split(":", 0);
                try {
                    if (tempArr.length == 3 && !"".equals(tempArr[2]) && !"".equals(tempArr[1]) &&
                            !Validation.isInvalidNumber(Integer.parseInt(tempArr[0])) && !tempArr[2].contains(":")) {
                        //Add to the list of records
                        passwordFileInfoList.add(new String[]{tempArr[0], tempArr[1]});
                    } else {
                        logger.log(Level.SEVERE, () -> "Problem Reading Password File");
                        exit(1);
                    }
                } catch (NumberFormatException e){ //Catch NumberFormatException if thrown during parse
                    logger.log(Level.SEVERE, e, () -> "Invalid UserID in File");
                    exit(1);
                }
            }
        } catch (FileNotFoundException e){
            //If unable to open file, log exception and exit
            logger.log(Level.SEVERE, e, () -> "Password File Not Found");
            exit(1);
        } catch (IOException e) {
            logger.log(Level.SEVERE, e, () -> "Error Reading File");
            exit(1);
        }
    }
}

/**
 * The class that handles a single client connection to the server
 */
class ClientThread implements Runnable{
    MessageInput in;
    MessageOutput out;
    SocketAddress clientAddress;
    Socket socket;
    Logger logger;

    final int TIMEOUT_SECONDS = 60;

    /**
     * constructs a ClientThread
     *
     * @param socket the socket of the connection
     */
    public ClientThread(Socket socket){
        this.socket = socket;
    }

    public void setUpThread(Socket socket){
        //Set thread attributes
        this.logger = Logger.getLogger(Server.class.getName());
        clientAddress = socket.getRemoteSocketAddress();
        logger.log(Level.INFO, () -> "Handling client at " + clientAddress);

        try {
            //Set timeout to be 60 seconds
            socket.setSoTimeout(TIMEOUT_SECONDS * 1000);

            //Create the input and output streams to the client
            this.in = new MessageInput(socket.getInputStream());
            this.out = new MessageOutput(socket.getOutputStream());
        } catch (IOException e) {
            logger.log(Level.SEVERE, e, () -> "Unable to Establish Connection to Client at " + clientAddress);
        }
    }

    /**
     * The main function for a client connection
     */
    @Override
    public void run() {
        setUpThread(socket);
        //Wait to receive message
        while (true) {
            Message msg;
            try {
                //Decode message
                msg = Message.decode(in);
            } catch (ValidationException e) {
                try {
                    //Check if the SocketTimeout ran out
                    if (e.getCause() != null && Objects.equals(e.getCause().getClass(), SocketTimeoutException.class) && in.read() != -1) {
                        //Log that the timout ran out
                        logger.log(Level.WARNING, () -> "Socket Timeout Reached for Client at " + clientAddress);
                        Error er = new Error(0, "Invalid message: " + e.getCause().getMessage());
                        er.encode(out);
                    } else if (in.read() != -1) { //Check if client has not closed connection
                        logger.log(Level.SEVERE, e, () -> "Validation Error for Client at " + clientAddress);
                        Error er = new Error(0, "Invalid message: " + e.getMessage());
                        er.encode(out);
                    }

                    //Close the connection
                    socket.close();
                } catch (IOException | ValidationException ex){
                    logger.log(Level.INFO, () -> "Connection Closed for Client at " + clientAddress);
                }
                return;
            }

            try {
                //Check if message has valid mapID and userID
                if (checkValid(msg)) {
                    if (msg.getClass().isAssignableFrom(NewLocation.class)) {
                        logger.log(Level.INFO, () -> "Received NewLocation from Client at " + clientAddress);
                        NewLocation loc = (NewLocation) msg;
                        LocationRecord rec = loc.getLocationRecord();
                        String name = checkIfValidUserID(rec.getUserId());
                        rec.setLocationName(name + ": " + rec.getLocationName());
                        logger.log(Level.INFO, () -> "Client at " + clientAddress + " submitted location record with " +
                                rec.getUserId() + ", " + rec.getLongitude() + ", " + rec.getLatitude() + ", " +
                                rec.getLocationName() + ", " + rec.getLocationDescription());

                        synchronized (Server.lock) {
                            //Send the new location to the NoTiFiServer
                            Server.noTiFiServer.sendLocation(rec);

                            //Add/update current location of user
                            Server.mapMgr.addLocation(new Location(rec.getLocationName(), String.valueOf(rec.getLongitude()), String.valueOf(rec.getLatitude()), rec.getLocationDescription(), Location.Color.BLUE));

                            //Delete the current location of user from the list if one present
                            Server.res.deleteLocation(rec.getUserId());

                            logger.log(Level.INFO, () -> "Sending LocationResponse to client at " + clientAddress);
                            //Update the LocationResponse and encode/send to client
                            Server.res.addLocationRecord(rec);
                            Server.res.encode(out);
                        }
                    } else if (msg.getClass().isAssignableFrom(LocationRequest.class)) {
                        synchronized (Server.lock) {
                            logger.log(Level.INFO, () -> "Received LocationRequest from Client at " + clientAddress);
                            //encode/send the LocationResponse to client
                            logger.log(Level.INFO, () -> "Sending LocationResponse to client at " + clientAddress);
                            Server.res.encode(out);
                        }
                    } else if (msg.getClass().isAssignableFrom(LocalLocationRequest.class)){
                        logger.log(Level.INFO, () -> "Received LocalLocationRequest from Client at " + clientAddress);
                        LocalLocationRequest req = (LocalLocationRequest) msg;

                        LocationResponse res = new LocationResponse(req.getMapId(), "");

                        //Calculate which location is closest, and add to response
                        if (Server.res.getLocationRecordList().size() > 0){
                            res.addLocationRecord(findClosestLocation(Double.parseDouble(req.getLongitude()), Double.parseDouble(req.getLatitude())));
                        }

                        logger.log(Level.INFO, () -> "Sending LocationResponse to client at " + clientAddress);
                        //Encode/send LocationResponse to client
                        res.encode(out);
                    }else {
                        //Unexpected message type
                        String type;
                        logger.log(Level.INFO, () -> "Received Invalid Type from Client at " + clientAddress);

                        //Find which class the message is
                        if (msg.getClass().isAssignableFrom(LocationResponse.class)){
                            type = "LocationResponse";
                        } else {
                            type = "Error";
                        }
                        logger.log(Level.INFO, () -> "Sending LocationResponse to client at " + clientAddress);
                        //Create Error and encode/send it to the client
                        Error er = new Error(msg.getMapId(), "Unexpected message type: " + type);
                        er.encode(out);
                    }
                }
            } catch (ValidationException | IOException e){
                //Log exception
                logger.log(Level.SEVERE, e, () -> "Exception When Encoding/Sending Message for Client at " + clientAddress);
            }
        }
    }

    /**
     * find the closest location to the given point
     *
     * @param baseLng the latitude
     * @param baseLat the longitude
     * @return the closest point
     */
    public LocationRecord findClosestLocation(double baseLng, double baseLat) {
        LocationRecord closest = null;
        double minDistance = 0;
        //Loop through the list of location records
        for (int i = 0; i < Server.res.getLocationRecordList().size(); i++){
            LocationRecord rec = Server.res.getLocationRecordList().get(i);

            //Find the lat and lng of the current location
            double currLat = Double.parseDouble(rec.getLatitude());
            double currLng = Double.parseDouble(rec.getLongitude());

            //Find the distance between the two points
            double currDist = Math.sqrt((currLng - baseLng) * (currLng - baseLng) + (currLat - baseLat) * (currLat - baseLat));

            //If this is the first location, make it the closest
            if (i == 0){
                minDistance = currDist;
                closest = rec;
            } else if (currDist < minDistance){
                //If the new distance is shorter that the current shortest, update
                minDistance = currDist;
                closest = rec;
            }
        }
        //Return closest location
        return closest;
    }

    /**
     * check if the message has valid data
     *
     * @param msg the message to check
     * @return whether the message is valid
     * @throws ValidationException if problem creating error
     * @throws IOException if problem during encode
     */
    public boolean checkValid(Message msg) throws ValidationException, IOException {
        boolean isValid = true;
        //Check if the given mapID is valid
        if (msg.getMapId() != Server.VALID_MAP_ID) {
            //If not valid, create an Error and encode/send to client
            logger.log(Level.WARNING, () -> "No such map: " + msg.getMapId() + " given by client at " + clientAddress);
            Error er = new Error(msg.getMapId(), "No such map: " + msg.getMapId());
            er.encode(out);
            isValid = false;
        } else if (msg.getClass().isAssignableFrom(NewLocation.class)) {
            NewLocation loc = (NewLocation) msg;
            //Check if the userID is valid
            if (checkIfValidUserID(loc.getLocationRecord().getUserId()) == null) {
                logger.log(Level.WARNING, () -> "No such user: " + loc.getLocationRecord().getUserId() +
                        " given by client at " + clientAddress);
                //If not valid, create an Error and encode/send to client
                Error er = new Error(msg.getMapId(), "No such user: " + loc.getLocationRecord().getUserId());
                er.encode(out);
                isValid = false;
            }
        }
        //Return whether the message has valid data
        return isValid;
    }

    /**
     * check if the given userID is valid
     *
     * @param id the userID to check
     * @return the user name if found or null if invalid
     */
    public String checkIfValidUserID(long id){
        //Loop through to check if the userID is valid
        for (int i = 0; i < Server.passwordFileInfoList.size(); i++){
            //If found return the user name
            if (Integer.parseInt(Server.passwordFileInfoList.get(i)[0]) == id){
                return Server.passwordFileInfoList.get(i)[1];
            }
        }
        //Return null if userID is not found in list
        return null;
    }
}
