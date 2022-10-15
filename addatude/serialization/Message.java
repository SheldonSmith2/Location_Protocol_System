/************************************************
 *
 * Author: Sheldon
 * Assignment: Program 1
 * Class: CSI 4321
 *
 ************************************************/
package addatude.serialization;

import java.io.IOException;
import java.util.Objects;

/**
 * Represents an AddATude Message
 *
 * @version 1.1
 * @author Sheldon Smith
 */
public abstract class Message {
    /** The variable to hold the protocol header */
    protected static final String PROTOCOL_HEADER = "ADDATUDEv1";

    /** The variable to hold to mapID */
    protected long mapID;

    /**
     * Serializes message to MessageOutput
     *
     * @param out serialization output destination
     * @throws IOException if I/O problem
     * @throws NullPointerException if out is null
     */
    public abstract void encode(MessageOutput out) throws IOException;

    /**
     * Deserializes message from byte source
     *
     * @param in deserialization input source
     * @return a specific message resulting from deserialization
     * @throws ValidationException if validation failure
     */
    public static Message decode(MessageInput in) throws ValidationException{
        //Check that the MessageInput is not null
        if (Objects.isNull(in)){
            throw new NullPointerException();
        }
        Message msg;
        try {
            msg = readEncodedMessage(in);
        } catch (IOException e) {
            //Throw a ValidationException if an IOException occurs
            throw new ValidationException("IOException", "Error reading MessageInput", e);
        }

        //Return the Message instance
        return msg;
    }

    /**
     * Reads through a MessageInput and creates the correct subclass of Message
     *
     * @param in the MessageInput to be read from
     * @throws ValidationException if error in setting fields
     * @throws IOException if there is an error while reading
     * @return the newly created subclass of Message
     */
    public static Message readEncodedMessage(MessageInput in) throws IOException, ValidationException {
        //Initialize variables
        long mapID;
        String operation;
        String str;

        //Read in the first bytes of the protocol
        str = in.readUntilSpace();
        if (!str.equals(PROTOCOL_HEADER)){
            throw new ValidationException(str, "Incorrect Protocol during decode");
        }

        //Read in the mapID
        str = in.readUntilSpace();
        try {
            mapID = Long.parseLong(str);
        } catch (NumberFormatException e){
            //Throw ValidationException if a NumberFormatException occurs
            throw new ValidationException("NumberFormatException", "Converting to number error", new NumberFormatException());
        }

        //Read in the operation
        operation = in.readUntilSpace();

        //Based on the operation, read in the rest of the message
        switch (operation) {
            case NewLocation.OPERATION -> {
                //Return the correct NewLocation object
                return new NewLocation(in, mapID);
            }
            case LocationRequest.OPERATION -> {
                //Return the correct LocationRequest
                return new LocationRequest(in, mapID);
            }
            case LocationResponse.OPERATION -> {
                //Return the LocationResponse
                return new LocationResponse(in, mapID);
            }
            case Error.OPERATION -> {
                return new Error(in, mapID);
            }
            case LocalLocationRequest.OPERATION -> {
                return new LocalLocationRequest(in, mapID);
            }
        }
        //If none of the operations fit, then throw a ValidationException
        throw new ValidationException(operation, "Invalid format");
    }

    /**
     * Returns map ID
     *
     * @return map ID
     */
    public final long getMapId(){
        return mapID;
    }

    /**
     * Sets map ID
     *
     * @param mapId new map ID
     * @return this object with new value
     * @throws ValidationException if validation fails
     */
    public final Message setMapId(long mapId) throws ValidationException{
        //Check that the mapId is within the bounds
        if (Validation.isInvalidNumber(mapId)){
            throw new ValidationException("Bad Argument", "Invalid MapID");
        }

        //Set the value and return self
        this.mapID = mapId;
        return this;
    }

    /**
     * Returns message operation (e.g., NEW)
     *
     * @return message operation
     */
    public abstract String getOperation();

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return mapID == message.mapID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mapID);
    }
}
