/************************************************
 *
 * Author: Sheldon
 * Assignment: Program 4
 * Class: CSI 4321
 *
 ************************************************/
package notifi.serialization;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * NoTiFi error
 *
 * @author Sheldon Smith
 * @version 1.0
 */
public class NoTiFiError extends NoTiFiMessage{
    /** The specific code for the Error */
    public static final int CODE = 2;
    /**The variable to hold the error message*/
    private String errorMessage;

    /**A regex constant to match an illegal character in a string*/
    final static String illegalCharPattern = ".*\\p{C}.*";
    /**Max size of error string (max size of packet minus 8 byte for UDP header,
     * 20 byte for IP packet header, 2 bytes for NoTiFi header)*/
    public final static int MAX_PACKET = 65535 - 8 - 2 - 20;

    /**
     * Constructs NoTiFi error from values
     *
     * @param msgId message ID
     * @param errorMessage error message
     * @throws IllegalArgumentException if validation fails
     */
    public NoTiFiError(int msgId, String errorMessage) throws IllegalArgumentException{
        super(msgId);
        setErrorMessage(errorMessage);
    }

    /**
     * Constructs NoTiFi error from byte array
     *
     * @param msgId message ID
     * @param pkt the byte array
     * @throws IllegalArgumentException if validation fails
     */
    public NoTiFiError(int msgId, byte[] pkt) throws IllegalArgumentException{
        //Set the message id
        super(msgId);

        //Create a string to hold the error message
        StringBuilder erMsg = new StringBuilder();

        //Loop through the rest of the packet
        for (int i = HEADER_SIZE; i < pkt.length; i++){
            //Convert the byte to char and add it to the error message
            erMsg.append((char) pkt[i]);
        }

        //Set the attributes of the NoTiFi error
        setErrorMessage(erMsg.toString());
    }

    public byte[] encode(){
        //Encode the header as a byte array
        byte[] totalBytes = encodeHeader(CODE);

        //Convert the error message to bytes and add to the byte array
        totalBytes = combineArrays(totalBytes, errorMessage.getBytes(StandardCharsets.UTF_8));

        //Return the byte array
        return totalBytes;
    }

    /**
     * Returns a String representation<br><br>
     * Error:_msgid=&lt;msgid&gt;_&lt;error message&gt;<br><br>
     * Underscore indicates a space. You <strong>must</strong> follow the spacing, etc. precisely<br><br>
     * For example<br><br>
     * Error: msgid=253 Bad Stuff
     *
     * @return string representation
     */
    @Override
    public String toString(){
        return "Error: msgid=" + msgID + " " + this.errorMessage;
    }

    /**
     * Set error message
     *
     * @param errorMessage error message
     * @return this object with new value
     * @throws IllegalArgumentException if error message is null or contains illegal characters
     */
    public NoTiFiError setErrorMessage(String errorMessage) throws IllegalArgumentException{
        //Check if the message is not null or contains an illegal character
        if (errorMessage == null || errorMessage.length() >= MAX_PACKET || errorMessage.matches(illegalCharPattern)
                || !errorMessage.matches(ASCII_REGEX)){
            throw new IllegalArgumentException("Invalid error message");
        }
        this.errorMessage = errorMessage;
        return this;
    }

    /**
     * Get error message
     *
     * @return error message
     */
    public String getErrorMessage(){
        return this.errorMessage;
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
        NoTiFiError that = (NoTiFiError) o;
        return errorMessage.equals(that.errorMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), errorMessage);
    }
}
