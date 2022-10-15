/************************************************
 *
 * Author: Sheldon
 * Assignment: Program 5
 * Class: CSI 4321
 *
 ************************************************/
package notifi.app.client;

import notifi.serialization.*;

import java.io.IOException;
import java.net.*;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

import static java.lang.System.exit;
import static notifi.serialization.NoTiFiMessage.decode;

/**
 * The NoTiFi client to handle UDP packets
 *
 * @author Sheldon Smith
 * @version 1.0
 */
public class NoTiFiClient {
    /**The variable to hold the users input*/
    public static String input;

    /**The variable to hold the size of the packet*/
    public final static int MAX_PACKET = 65600;

    /**The minimum value of message id*/
    private final static int MIN_ID = 0;
    /**The maximum value of message id*/
    private final static int MAX_ID = 255;

    /**
     * The main program to control the client for NoTiFI
     *
     * @param args arguments from the command line
     */
    public static void main(String[] args) {
        //Check that correct number of arguments were given
        if (args.length != 3){
            throw new IllegalArgumentException("Parameter(s): <Server> <Port> <Local IP>");
        }

        //Set the server name and local client port from the arguments
        String serverName = args[0];
        String localClientIP = args[2];
        InetAddress serverAddress;
        int serverPort;

        DatagramSocket socket;
        try {
            //Set the port for the server
            serverPort = Integer.parseInt(args[1]);

            //Create the socket and address
            socket = new DatagramSocket();
            serverAddress = InetAddress.getByName(serverName);
        } catch (NumberFormatException e){
            //If the port is not an integer, then print error and exit
            System.err.println("Illegal Given Port: " + args[1]);
            return;
        } catch (SocketException | UnknownHostException e) {
            //Print error if problem setting up socket and address
            System.err.println("Unable to create datagram socket/address");
            return;
        }

        //Create a register message
        NoTiFiRegister reg;
        try {
            //Set the register msg id to be random, address from given argument, and the port given from the socket
            reg = new NoTiFiRegister(ThreadLocalRandom.current().nextInt(MIN_ID, MAX_ID),
                    (Inet4Address) InetAddress.getByName(localClientIP), socket.getLocalPort());
        } catch (UnknownHostException e){
            System.err.println("Invalid local IP");
            return;
        }
        //Encode the register message
        byte[] payload = reg.encode();
        //Set up the packet to be sent
        DatagramPacket packet = new DatagramPacket(payload, payload.length, serverAddress, serverPort);

        //Create a buffer of bytes to hold the responses from the server
        byte[] buffer = new byte[MAX_PACKET];
        DatagramPacket rcvdPacket = new DatagramPacket(buffer, buffer.length, serverAddress, serverPort);

        //Create and start a thread to control receiving messages from the server
        MessageControlThread registerThread = new MessageControlThread(packet, reg.getMsgId(), socket, rcvdPacket);
        registerThread.start();

        //Create a scanner to receive input from the user
        Scanner scanner = new Scanner(System.in);
        do {
            //Read in input if the user entered anything
            input = scanner.nextLine();
        } while (!"quit".equals(input)); //Exit the program if the user entered "quit"

        //Close the datagram socket
        socket.close();
    }
}

/**
 * A thread class to control managing messages from the server
 */
class MessageControlThread extends Thread{
    /**The correct msgID to verify the received ACK*/
    private final int correctMsgID;
    /**The register packet to send to the server*/
    private final DatagramPacket regPacket;
    /**The datagram socket to send the packets through*/
    private final DatagramSocket socket;

    private final DatagramPacket rcvdPacket;

    /**The variable to hold the number of seconds to wait before retransmitting the message*/
    private static final int TIMEOUT_SECS = 3;

    /**
     * Constructs a MessageControlThread
     *
     * @param p the register packet to send to the server
     * @param id the correct message id to validate ACK
     * @param socket the socket to send the packets
     */
    public MessageControlThread(DatagramPacket p, int id, DatagramSocket socket, DatagramPacket rcvd){
        this.regPacket = p;
        this.correctMsgID = id;
        this.socket = socket;
        this.rcvdPacket = rcvd;
    }

    /**
     * The main function for the thread class
     */
    public void run(){
        boolean ackValid = false, shouldRetransmit = true;
        int numTimeout = 0;

        //Loop forever or until user quits via the main program
        while(true) {
            try {
                //Only send register packet if the correct ACK hasn't been received
                if (!ackValid && shouldRetransmit) {
                    //Set the timeout timer to be 3 seconds
                    socket.setSoTimeout(TIMEOUT_SECS * 1000);

                    //Send a register message to the server
                    socket.send(regPacket);
                }

                //Receive a packet from the server
                socket.receive(rcvdPacket);
                shouldRetransmit = false;

                //Create a new byte array to hold only the data that the server sent
                byte[] newArray = new byte[rcvdPacket.getLength()];
                System.arraycopy(rcvdPacket.getData(), 0, newArray, 0, rcvdPacket.getLength());

                //Decode the payload from the received packet
                NoTiFiMessage msg = decode(newArray);

                //Check if the received message is an ACK
                if (!ackValid && msg.getClass().isAssignableFrom(NoTiFiACK.class)){
                    NoTiFiACK ack = (NoTiFiACK) msg;
                    if (ack.getMsgId() == correctMsgID) { //If the received ACK with correct id, set boolean to true
                        ackValid = true;
                    } else {
                        //If receive ACK with incorrect id, print "Unexpected MSG ID" to console
                        System.err.println("Unexpected MSG ID");
                    }
                } else {
                    //If the correct ACK has been given, handle message normally
                    handleOtherMessage(msg);
                }
            } catch (SocketTimeoutException e){ //If no correct ACK for 3 secs, resend register msg
                //Check whether the correct ACK has been received
                if (!ackValid) {
                    numTimeout++;
                    shouldRetransmit = true;
                    //If this is the second time to timeout, print error message and exit
                    if (numTimeout >= 2) {
                        System.err.println("Unable to register");
                        exit(1);
                    }
                }
            } catch (IOException e){
                exit(1); //Exit program if exception occurs during read
            } catch (IllegalArgumentException e) { //If the message was invalid, print error
                if (!"quit".equals(NoTiFiClient.input)) {
                    System.err.println("Unable to parse message: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Handle the passed NoTiFIMessage
     *
     * @param message the message to handle
     */
    public void handleOtherMessage(NoTiFiMessage message){
        //Check if the message is a location addition
        if (message.getClass().isAssignableFrom(NoTiFiLocationAddition.class)) {
            //Print the location addition
            NoTiFiLocationAddition locAdd = (NoTiFiLocationAddition) message;
            System.out.println(locAdd);
            //Check if the message is an error message
        } else if (message.getClass().isAssignableFrom(NoTiFiError.class)) {
            NoTiFiError error = (NoTiFiError) message;
            //Print the error message
            System.err.println(error.getErrorMessage());
        } else {
            //Print that the message was not a type that was expected
            System.err.println("Unexpected message type");
        }
    }
}

