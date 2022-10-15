/************************************************
 *
 * Author: Sheldon
 * Assignment: Program 4
 * Class: CSI 4321
 *
 ************************************************/
package notifi.serialization;

import java.net.*;
import java.util.HexFormat;
import java.util.Objects;

/**
 * NoTiFi Register
 *
 * @author Sheldon Smith
 * @version 1.0
 */
public class NoTiFiRegister extends NoTiFiMessage{
    /** The specific code for the Register */
    public static final int CODE = 0;
    /**The variable to hold the address*/
    private Inet4Address address;
    /**The variable to hold the port*/
    private int port;
    /**The variable to hold the socket address*/
    private InetSocketAddress socketAddress;

    /**Constant to hold the max number of the port*/
    private final int MAX_PORT = 65535;
    /**Constant to hold the min number of the port*/
    private final int MIN_PORT = 0;
    /**Constant to hold to size of a register*/
    private final int MIN_SIZE_REG = 8;

    /**
     * Constructs NoTiFi register message
     *
     * @param msgId message ID
     * @param address address to register
     * @param port port to register
     * @throws IllegalArgumentException if validation fails
     */
    public NoTiFiRegister(int msgId, Inet4Address address, int port) throws IllegalArgumentException{
        super(msgId);
        setAddress(address);
        setPort(port);
        socketAddress = new InetSocketAddress(address, port);
    }

    /**
     * Constructs NoTiFi register message from byte array
     *
     * @param msgId message ID
     * @param pkt byte array
     * @throws IllegalArgumentException if validation fails
     */
    public NoTiFiRegister(int msgId, byte[] pkt) throws IllegalArgumentException{
        //Set the message id
        super(msgId);

        //Check that the packet length is the appropriate size
        if (pkt.length != MIN_SIZE_REG){
            throw new IllegalArgumentException("Incorrect number of bytes of NoTiFIRegister");
        }

        //Read in 4 bytes for address
        byte[] addressArr = new byte[ADDRESS_SIZE];
        System.arraycopy(pkt, 2, addressArr, 0, ADDRESS_SIZE);

        //Create array to hold reversed address
        byte[] b = new byte[addressArr.length];
        int size = addressArr.length;

        //Reverse the address since it was encoded in little endian
        for (byte value : addressArr) {
            b[size - 1] = value;
            size = size - 1;
        }
        Inet4Address address;
        try {
            //Create a Inet4Address from the byte array
            address = (Inet4Address) Inet4Address.getByAddress(b);
        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Invalid address for NoTiFiRegister");
        }

        //Read in 2 bytes and convert from little endian to int
        byte[] portArr = {0, 0, 0, 0};
        System.arraycopy(pkt, HEADER_SIZE+ADDRESS_SIZE, portArr, 0, 2);
        //Convert the byte array into the integer for the port
        int port = byteArrayToInt(portArr, "Little");

        //Set the attributes of the NoTiFI register
        setAddress(address);
        setPort(port);

        //Create the socket address from the address and port
        socketAddress = new InetSocketAddress(address, port);
    }

    public byte[] encode(){
        //Encode the header into byte array
        byte[] totalBytes = encodeHeader(CODE);

        //Split the address into the 4 sections
        String[] addressParts = address.getHostAddress().split("\\.", 0);

        StringBuilder hexString = new StringBuilder();
        //Encode each part of the address in reverse order (little endian)
        for (int i = 3; i >= 0; i--){
            hexString.append(HexFormat.of().toHexDigits(Integer.parseInt(addressParts[i]), 2));
        }

        //Combine the header with the address byte array
        totalBytes = combineArrays(totalBytes, HexFormat.of().parseHex(hexString.toString()));

        //Encode the port as an integer
        byte[] temp = intToByteArray(port, "Little");

        //Only keep the most significant two bytes
        byte[] temp2 = new byte[2];
        temp2[0] = temp[0];
        temp2[1] = temp[1];

        //Add port bytes to the byte array
        totalBytes = combineArrays(totalBytes, temp2);

        //Return the byte array
        return totalBytes;
    }

    /**
     * Returns a String representation<br><br>
     * Register:_msgid=&lt;msgid&gt;_address=&lt;address&gt;_port=&lt;port&gt;<br><br>
     * Underscore indicates a space. You <strong>must</strong> follow the spacing, etc. precisely<br><br>
     * For example<br><br>
     * Register: msgid=253 address=1.2.3.4 port=4321
     *
     * @return string representation
     */
    @Override
    public String toString(){
        return "Register: msgid=" + msgID + " address=" + this.address.getHostAddress() + " port=" + this.port;
    }

    /**
     * Get register address
     *
     * @return register address
     */
    public Inet4Address getAddress(){
        return this.address;
    }

    /**
     * Set register address
     *
     * @param address register address
     * @return this object with new value
     * @throws IllegalArgumentException if address is null
     */
    public NoTiFiRegister setAddress(Inet4Address address) throws IllegalArgumentException{
        try {
            this.address = Objects.requireNonNull(address);
        } catch (NullPointerException e){
            throw new IllegalArgumentException("Address cannot be null", e);
        }
        this.socketAddress = new InetSocketAddress(this.address, this.port);
        return this;
    }

    /**
     * Get register port
     *
     * @return register port
     */
    public int getPort(){
        return this.port;
    }

    /**
     * Set register port
     *
     * @param port register port
     * @return this object with new value
     * @throws IllegalArgumentException if port is out of range [0...65535]
     */
    public NoTiFiRegister setPort(int port) throws IllegalArgumentException{
        if (port > MAX_PORT || port < MIN_PORT){
            throw new IllegalArgumentException("Port out of range");
        }
        this.port = port;
        this.socketAddress = new InetSocketAddress(this.address, this.port);
        return this;
    }

    /**
     * Get address
     *
     * @return register address
     */
    public InetSocketAddress getSocketAddress(){
        return this.socketAddress;
    }

    @Override
    public int getCode() {
        return CODE;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        NoTiFiRegister that = (NoTiFiRegister) o;
        return port == that.port && address.equals(that.address) && socketAddress.equals(that.socketAddress);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), address, port, socketAddress);
    }
}
