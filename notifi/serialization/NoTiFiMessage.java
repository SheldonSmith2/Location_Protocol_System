/************************************************
 *
 * Author: Sheldon
 * Assignment: Program 4
 * Class: CSI 4321
 *
 ************************************************/
package notifi.serialization;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Objects;

/**
 * NoTiFi message
 *
 * @author Sheldon Smith
 * @version 1.0
 */
public abstract class NoTiFiMessage {
    /** The variable to hold the message ID */
    protected int msgID;
    /** The specific version for all messages */
    protected static final int VERSION = 3;

    private final int MAX_ID = 255;
    private final int MIN_ID = 0;
    /** The size of an address in bytes*/
    protected static final int ADDRESS_SIZE = 4;
    /** The size of the header in bytes */
    protected static final int HEADER_SIZE = 2;
    /** The regex used to determine if a string is all ASCII */
    protected static final String ASCII_REGEX = "\\A\\p{ASCII}*\\z";

    /**
     * Constructs message with id
     *
     * @param msgID the message id
     */
    public NoTiFiMessage(int msgID){
        this.setMsgId(msgID);
    }

    /**
     * Deserializes from byte array
     *
     * @param pkt byte array to deserialize
     * @return a specific NoTiFi message resulting from deserialization
     * @throws IllegalArgumentException if validation fails, etc.
     */
    public static NoTiFiMessage decode(byte[] pkt) throws IllegalArgumentException{
        //Check that the packet is at least 2 bytes for the header
        if (pkt.length < HEADER_SIZE){
            throw new IllegalArgumentException("The byte array is to small to decode");
        }

        int msgID, readVersion, messageCode;
        try{
            //Read in the first 4-bits and convert to version
            readVersion = pkt[0] >> 4;
            //Read in the next 4-bits and convert to code
            messageCode = pkt[0] & 0x0F;

            //Read in the next byte and convert to messageID
            msgID = byteArrayToInt(new byte[]{0, 0, 0, pkt[1]}, "Big");
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Validation error parsing integer", e);
        }

        //Check that the version number is correct
        if (readVersion != VERSION){
            throw new IllegalArgumentException("Incorrect version during decode");
        }

        //Decide which type of NoTiFi to decode based on code
        switch (messageCode) {
            case NoTiFiError.CODE -> {
                //Return the correct NoTiFiError
                return new NoTiFiError(msgID, pkt);
            }
            case NoTiFiRegister.CODE -> {
                //Return the correct NoTiFiRegister
                return new NoTiFiRegister(msgID, pkt);
            }
            case NoTiFiACK.CODE -> {
                //Check that there is not any extra items in the packet
                if (pkt.length != HEADER_SIZE){
                    throw new IllegalArgumentException("Incorrect number of bytes for NoTiFiACK");
                }
                //Return the correct NoTiFiACK
                return new NoTiFiACK(msgID);
            }
            case NoTiFiLocationAddition.CODE -> {
                //Return the correct NoTiFiLocationAddition
                return new NoTiFiLocationAddition(msgID, pkt);
            }
        }
        //If the code does not match any of the packet types, then throw exception
        throw new IllegalArgumentException("Invalid message code");
    }

    /**
     * Serializes message
     *
     * @return serialized message bytes
     */
    public abstract byte[] encode();

    /**
     * Encodes the header
     *
     * @param code the code to be encoded
     * @return serialized header bytes
     */
    public byte[] encodeHeader(int code){
        //Encode the version
        byte[] arr = new byte[2];
        //Shift the version over by 4 and add the code to it
        arr[0] = (byte) ((VERSION << 4) + code);
        arr[1] = (byte) this.msgID;

        //Return the bytes of the header
        return arr;
    }

    /**
     * Set message ID
     *
     * @param msgId message ID
     * @return this object with new value
     * @throws IllegalArgumentException if invalid message ID
     */
    public NoTiFiMessage setMsgId(int msgId) throws IllegalArgumentException{
        if (msgId < MIN_ID || msgId > MAX_ID){
            throw new IllegalArgumentException("Invalid message ID");
        }
        msgID = msgId;
        return this;
    }

    /**
     * Get message ID
     *
     * @return message ID
     */
    public int getMsgId(){
        return msgID;
    }

    /**
     * Get operation code
     *
     * @return operation code
     */
    public abstract int getCode();

    /**
     * Combine two arrays
     *
     * @param array1 first array
     * @param array2 second array
     * @return the combined arrays
     */
    public byte[] combineArrays(byte[] array1, byte[] array2){
        //Find the lengths of the arrays
        int aLen = array1.length;
        int bLen = array2.length;
        byte[] result = new byte[aLen + bLen];

        //Copy the arrays over into the result array
        System.arraycopy(array1, 0, result, 0, aLen);
        System.arraycopy(array2, 0, result, aLen, bLen);

        //Return the combined arrays
        return result;
    }

    /**
     * converts a byte array to an integer
     *
     * @param b the byte array to be converted
     * @param type either little or big endian
     * @return the integer from the byte array
     */
    public static int byteArrayToInt(byte[] b, String type) {
        final ByteBuffer bb = ByteBuffer.wrap(b);
        //Set the encoding order based on the option
        if ("Little".equals(type)){
            bb.order(ByteOrder.LITTLE_ENDIAN);
        } else {
            bb.order(ByteOrder.BIG_ENDIAN);
        }
        //Get the integer from the byte buffer and return
        return bb.getInt();
    }

    /**
     * Converts integer to a byte array
     *
     * @param i the integer to be converted
     * @param type how to encode integer (little or big endian)
     * @return the created byte array
     */
    public static byte[] intToByteArray(int i, String type) {
        final ByteBuffer bb = ByteBuffer.allocate(4);
        //Set the encoding order depending on the option
        if ("Little".equals(type)){
            bb.order(ByteOrder.LITTLE_ENDIAN);
        } else {
            bb.order(ByteOrder.BIG_ENDIAN);
        }
        //Put the integer into the byte buffer
        bb.putInt(i);
        return bb.array();
    }

    /**
     * Convert a double into byte array
     *
     * @param value the double to be converted
     * @return the created byte array
     */
    public static byte[] doubleToByteArray(double value) {
        byte[] bytes = new byte[8];
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        //Set the encoding order to Little Endian
        bb.order(ByteOrder.LITTLE_ENDIAN);
        //Put the double into the byte buffer
        bb.putDouble(value);
        return bytes;
    }

    /**
     * Convert byte array to a double
     *
     * @param bytes the byte array to be converted
     * @return the found double from bytes
     */
    public static double byteArrayToDouble(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        //Set the encoding order to Little Endian
        bb.order(ByteOrder.LITTLE_ENDIAN);
        return bb.getDouble();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NoTiFiMessage that = (NoTiFiMessage) o;
        return msgID == that.msgID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(msgID);
    }
}
