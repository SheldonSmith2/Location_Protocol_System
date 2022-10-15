/************************************************
 *
 * Author: Sheldon
 * Assignment: Program 1
 * Class: CSI 4321
 *
 ************************************************/
package addatude.serialization;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Represents an error message
 *
 * @version 1.1
 * @author Sheldon Smith
 */
public class Error extends Message{
    /** The string that contains the operation type */
    public static final String OPERATION = "ERROR";
    /** The variable to hold the error message */
    private String message;

    /**
     * Constructs error message using set values
     *
     * @param mapId ID for message map
     * @param errorMessage error message
     * @throws ValidationException if validation fails
     */
    public Error(long mapId, String errorMessage) throws ValidationException{
        //Set the values using the setters
        this.setErrorMessage(errorMessage);
        this.setMapId(mapId);
    }

    /**
     * Constructs error message using a MessageInput
     *
     * @param in MessageInput to read from
     * @param mapID ID for message map
     * @throws ValidationException if validation fails
     * @throws IOException if read fails
     */
    public Error(MessageInput in, long mapID) throws ValidationException, IOException{
        int currByte;

        String str = in.readUntilSpace();
        //Parse integer and check that the number is not negative
        int msgLength;
        try {
            msgLength = Integer.parseInt(str);
        } catch (NumberFormatException e) {
            throw new ValidationException(str, "Error converting " + str + " to number in decode", e);
        }
        if (msgLength < 0) {
            throw new ValidationException(str, "The number cannot be negative");
        }

        StringBuilder currStr = new StringBuilder();
        //Read in the message
        for (int i = 0; i < msgLength; i++) {
            currByte = in.read();
            in.checkValidRead(currByte);
            currStr.append((char) currByte);
        }
        in.checkForEOS();
        //Return the Error

        this.setMapId(mapID);
        this.setErrorMessage(currStr.toString());
    }

    /**
     * Returns a String representation<br>
     * Error:_map=&lt;mapID&gt;_error=&lt;errorMsg&gt;<br><br>
     *
     * Underscore indicates a space. You must follow the spacing, etc. precisely.<br><br>
     *
     * For example<br>
     * Error: map=501 error=bad stuff<br>
     *
     * @return string representation
     */
    @Override
    public String toString(){
        return "Error: map=" + this.getMapId() + " error=" + this.message;
    }

    /**
     * Return error message
     *
     * @return error message
     */
    public String getErrorMessage(){
        return this.message;
    }

    /**
     * Set error message
     *
     * @param errorMessage new error message
     * @return this object with new value
     * @throws ValidationException if validation fails (e.g., null, illegal character, etc.)
     */
    public Error setErrorMessage(String errorMessage) throws ValidationException{
        //Check that the errorMessage is valid
        if (Validation.isInvalidString(errorMessage)){
            throw new ValidationException(errorMessage, "errorMessage cannot be null");
        }
        //Set the message and return self
        this.message = Objects.requireNonNull(errorMessage);
        return this;
    }

    /**
     * Serializes Error to MessageOutput
     *
     * @param out serialization output destination
     * @throws IOException if I/O problem
     * @throws NullPointerException if out is null
     */
    @Override
    public void encode(MessageOutput out) throws IOException {
        //Encode the ERROR in parts
        out.write(PROTOCOL_HEADER.getBytes(StandardCharsets.UTF_8));

        String str = " " + this.mapID + " ERROR ";
        out.write(str.getBytes(StandardCharsets.UTF_8));

        str = this.message.length() + " " + this.message + "\r\n";
        out.write(str.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String getOperation() {
        return OPERATION;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Error error = (Error) o;
        return message.equals(error.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), message);
    }
}
