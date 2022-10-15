/************************************************
 *
 * Author: Sheldon
 * Assignment: Program 6
 * Class: CSI 4321
 *
 ************************************************/
package notifi.app.server;

import addatude.app.server.Server;
import addatude.serialization.LocationRecord;
import notifi.app.client.NoTiFiClient;
import notifi.serialization.*;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

import static notifi.serialization.NoTiFiMessage.decode;

/**
 * The server that controls NoTiFi messages
 *
 * @author Sheldon Smith
 * @version 1.0
 */
public class NoTiFiServer extends Thread {
    private static final List<InetSocketAddress> registeredClients = new ArrayList<>();
    private static final Logger logger = Logger.getLogger(Server.class.getName());
    private static DatagramSocket socket;

    /**Constant to check if character is ASCII*/
    private static final String ASCII_REGEX = "[^\\x00-\\x7F]";

    /**
     * Set up the server
     *
     * @param serverPort the server port
     */
    public NoTiFiServer(int serverPort){
        try {
            //Create the server socket
            socket = new DatagramSocket(serverPort);
        } catch (SocketException e) {
            logger.log(Level.SEVERE, e, () -> "Unable to set up socket");
        }
    }

    /**
     * The main function to run the NoTiFi server
     */
    public void run(){
        //Create a datagram packet to hold the received packets from clients
        byte[] buffer = new byte[NoTiFiClient.MAX_PACKET];
        DatagramPacket rcvdPacket = new DatagramPacket(buffer, buffer.length);

        //Loop forever
        while (true) {
            try {
                //Receive a packet from a client
                socket.receive(rcvdPacket);

            } catch (IOException e) {
                //Log if there was an error reading from the socket
                logger.log(Level.SEVERE, e, () -> "Problem reading from socket");
                return;
            }

            //Create a new byte array to hold only the data that the client sent
            byte[] newArray = new byte[rcvdPacket.getLength()];
            System.arraycopy(rcvdPacket.getData(), 0, newArray, 0, rcvdPacket.getLength());

            try {
                //Decode the received message
                NoTiFiMessage message = decode(newArray);

                //Check if the message is a NoTiFiRegister message
                if (message.getClass().isAssignableFrom(NoTiFiRegister.class)) {
                    //Log the received register packet
                    logger.log(Level.INFO, () -> "Received register packet from client at " + rcvdPacket.getAddress() + ":" + rcvdPacket.getPort());

                    NoTiFiRegister reg = (NoTiFiRegister) message;

                    //Check if the address is a multicast address
                    if (reg.getAddress().isMulticastAddress()){
                        //Send error message to client
                        logger.log(Level.WARNING, () -> "Received register with bad address from client at " + rcvdPacket.getAddress());
                        sendError("Bad address", reg.getMsgId(), rcvdPacket.getAddress(), rcvdPacket.getPort());
                        //Check if the register port and the packet port are the same
                    } else if (reg.getPort() != rcvdPacket.getPort()){
                        logger.log(Level.WARNING, () -> "Received register with incorrect port from client at " + rcvdPacket.getAddress());
                        sendError("Incorrect port", reg.getMsgId(), rcvdPacket.getAddress(), rcvdPacket.getPort());
                        //Check if the register is already registered
                    } else if (registeredClients.contains(reg.getSocketAddress())){
                        logger.log(Level.WARNING, () -> "Received register already registered from client at " + rcvdPacket.getAddress());

                        sendError("Already registered", reg.getMsgId(), rcvdPacket.getAddress(), rcvdPacket.getPort());
                    } else {
                        logger.log(Level.INFO, () -> "Received valid register from client at " + rcvdPacket.getAddress());

                        //Add the register to the list
                        registeredClients.add(reg.getSocketAddress());
                        //Check if the register address and the source address match
                        if (reg.getAddress() != rcvdPacket.getAddress()){
                            logger.log(Level.WARNING, () -> "Register and Source Address mismatch: " + reg.getAddress() + " " + rcvdPacket.getAddress());
                        }
                        //Create an ACK with messageID of the register that it received
                        byte[] sendData = new NoTiFiACK(reg.getMsgId()).encode();
                        try {
                            //Send the ACK back to the client
                            socket.send(new DatagramPacket(sendData, sendData.length, rcvdPacket.getAddress(), rcvdPacket.getPort()));
                        } catch (IOException e) {
                            logger.log(Level.SEVERE, e, () -> "Problem sending packet to client at " + rcvdPacket.getAddress());
                        }
                    }
                } else {
                    //Log that server received unexpected message
                    logger.log(Level.WARNING, () -> "Received unexpected message from client at " + rcvdPacket.getAddress() + ":" + rcvdPacket.getPort());
                    sendError("Unexpected message type: " + message.getCode(), message.getMsgId(), rcvdPacket.getAddress(), rcvdPacket.getPort());
                }
            } catch (IllegalArgumentException e){
                //If problem decoding message, log error
                logger.log(Level.SEVERE, e, () -> "Received invalid message from client at " + rcvdPacket.getAddress() + ":" + rcvdPacket.getPort());
                sendError("Unable to parse: " + e.getMessage(), 0, rcvdPacket.getAddress(), rcvdPacket.getPort());
            }
        }
    }

    /**
     * Send an error message to a client
     *
     * @param msg the error message
     * @param msgID the message ID
     * @param address the client address
     * @param port the client port
     */
    public static void sendError(String msg, int msgID, InetAddress address, int port) {
        //Create the error message
        NoTiFiError error = new NoTiFiError(msgID, msg);
        byte[] sendPayload = error.encode();
        try {
            //Send the error message to the client
            socket.send(new DatagramPacket(sendPayload, sendPayload.length, address, port));
        } catch (IOException ex) {
            logger.log(Level.SEVERE, ex, () -> "Problem sending packet");
        }
    }

    /**
     * Send location to all the registered clients
     *
     * @param locRec the location record to send
     */
    public void sendLocation(LocationRecord locRec){
        logger.log(Level.INFO, () -> "Received LocationRecord from AddATude server");

        String name, desc;
        //Truncate location name if needed
        if (locRec.getLocationName().length() > NoTiFiLocationAddition.MAX_STRING_LENGTH){
            name = locRec.getLocationName().substring(0, NoTiFiLocationAddition.MAX_STRING_LENGTH);
        } else {
            name = locRec.getLocationName();
        }

        //Truncate location description is needed
        if (locRec.getLocationDescription().length() > NoTiFiLocationAddition.MAX_STRING_LENGTH){
            desc = locRec.getLocationDescription().substring(0, NoTiFiLocationAddition.MAX_STRING_LENGTH);
        } else {
            desc = locRec.getLocationDescription();
        }

        //Replace non-ASCII characters with ?
        name = name.replaceAll(ASCII_REGEX, "?");
        desc = desc.replaceAll(ASCII_REGEX, "?");

        //Create NoTiFi Location Addition with random msgID
        NoTiFiLocationAddition locAdd = new NoTiFiLocationAddition(ThreadLocalRandom.current().nextInt(0, 255),
                (int) locRec.getUserId(), Double.parseDouble(locRec.getLongitude()), Double.parseDouble(locRec.getLatitude()),
                name, desc);

        //Encode the location addition
        byte[] payload = locAdd.encode();
        //Loop through the registered client
        logger.log(Level.INFO, () -> "Sending LocationAddition to all registered clients");
        for (InetSocketAddress registeredClient : registeredClients) {
            try {
                //Send the packet to the client
                socket.send(new DatagramPacket(payload, payload.length, registeredClient));
            } catch (IOException e) {
                logger.log(Level.SEVERE, e, () -> "Send error for client at " + registeredClient.getAddress());
            }
        }
    }

}
