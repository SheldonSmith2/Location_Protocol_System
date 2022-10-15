/************************************************
 *
 * Author: Sheldon
 * Assignment: Program 7
 * Class: CSI 4321
 *
 ************************************************/
package notifi.app.client;

import notifi.serialization.*;

import java.io.IOException;
import java.net.*;
import java.util.Scanner;

import static java.lang.System.exit;
import static notifi.serialization.NoTiFiMessage.decode;

/**
 * The NoTiFi Multicast client to handle UDP packets
 *
 * @author Sheldon Smith
 * @version 1.0
 */
public class NoTiFiMulticastClient {
    /**The variable to hold the users input*/
    public static String input;

    /**
     * The main program to control the client for NoTiFI
     *
     * @param args arguments from the command line
     */
    public static void main(String[] args) {
        //Check that correct number of arguments were given
        if (args.length != 2){
            throw new IllegalArgumentException("Parameter(s): <Multicast Address> <Port>");
        }

        MulticastSocket socket;
        InetAddress address;
        InetSocketAddress socketAddr;

        try {
            //Set the port for the server
            int port = Integer.parseInt(args[1]);

            //Create the  address
            address = InetAddress.getByName(args[0]);

            //Create the multicast socket
            socket = new MulticastSocket(port);
            socketAddr = new InetSocketAddress(address, 0);
            if (!socketAddr.getAddress().isMulticastAddress()) { // Test if multicast address
                System.err.println("Not a multicast address");
                return;
            }

            //Join the group
            socket.joinGroup(socketAddr, null);
        } catch (NumberFormatException e){
            //If the port is not an integer, then print error and exit
            System.err.println("Illegal Given Port: " + args[1]);
            return;
        } catch (IOException e) {
            //Print error if problem setting up socket and address
            System.err.println("Unable to create datagram socket/address " + e);
            return;
        }

        //Create and start a thread to control receiving messages from the server
        MessageThread registerThread = new MessageThread(socket);
        registerThread.start();

        //Create a scanner to receive input from the user
        Scanner scanner = new Scanner(System.in);
        do {
            //Read in input if the user entered anything
            input = scanner.nextLine();
        } while (!"quit".equals(input)); //Exit the program if the user entered "quit"

        //Close the datagram socket
        try {
            //Leave the multicast group
            socket.leaveGroup(socketAddr, null);
        } catch (IOException e) {
            exit(1);
        }
        socket.close();
    }
}

/**
 * A thread class to control managing messages from the server
 */
class MessageThread extends Thread{
    /**The datagram socket to send the packets through*/
    private final MulticastSocket socket;

    /**The variable to hold the size of the packet*/
    public final static int MAX_PACKET = 65600;

    /**
     * Constructs a MessageControlThread
     *
     * @param socket the socket to send the packets
     */
    public MessageThread(MulticastSocket socket){
        this.socket = socket;
    }

    /**
     * The main function for the thread class
     */
    public void run(){
        //Create a buffer of bytes to hold the responses from the server
        byte[] buffer = new byte[MAX_PACKET];
        DatagramPacket rcvdPacket = new DatagramPacket(buffer, buffer.length);

        //Loop forever or until user quits via the main program
        while(true) {
            try {
                //Receive a packet from the server
                socket.receive(rcvdPacket);

                //Create a new byte array to hold only the data that the server sent
                byte[] newArray = new byte[rcvdPacket.getLength()];
                System.arraycopy(rcvdPacket.getData(), 0, newArray, 0, rcvdPacket.getLength());

                //Decode the payload from the received packet
                NoTiFiMessage msg = decode(newArray);

                //If the correct ACK has been given, handle message normally
                if (msg.getClass().isAssignableFrom(NoTiFiLocationAddition.class)) {
                    //Print the location addition
                    NoTiFiLocationAddition locAdd = (NoTiFiLocationAddition) msg;
                    System.out.println(locAdd);
                    //Check if the message is an error message
                } else if (msg.getClass().isAssignableFrom(NoTiFiError.class)) {
                    NoTiFiError error = (NoTiFiError) msg;
                    //Print the error message
                    System.err.println(error.getErrorMessage());
                } else {
                    //Print that the message was not a type that was expected
                    System.err.println("Unexpected message type");
                }

            } catch (IOException e){
                if (!"quit".equals(NoTiFiClient.input)) {
                    exit(1); //Exit program if exception occurs during read
                } else {
                    exit(0);
                }
            } catch (IllegalArgumentException e) { //If the message was invalid, print error
                if (!"quit".equals(NoTiFiClient.input)) {
                    System.err.println("Unable to parse message: " + e.getMessage());
                }
            }
        }
    }
}

