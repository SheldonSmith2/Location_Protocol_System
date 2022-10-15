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

/**
 * Represents a location request
 *
 * @version 1.1
 * @author Sheldon Smith
 */
public class LocationRequest extends Message{
    /** The string that contains the operation type */
    public static final String OPERATION = "ALL";

    /**
     * Constructs location request using set values
     *
     * @param mapId ID for message map
     * @throws ValidationException if validation fails
     */
    public LocationRequest(long mapId) throws ValidationException{
        //Set the values
        this.setMapId(mapId);
    }

    /**
     * Constructs location request using a MessageInput
     *
     * @param in the MessageInput to read from
     * @param mapID ID for message map
     * @throws ValidationException if validation fails
     * @throws IOException if read fails
     */
    public LocationRequest(MessageInput in, long mapID) throws ValidationException, IOException {
        //Return the correct LocationRequest
        in.checkForEOS();
        this.setMapId(mapID);
    }

    /**
     * Returns a String representation<br>
     * LocationRequest:_map=&lt;mapID&gt;<br><br>
     *
     * Underscore indicates a space. You must follow the spacing, etc. precisely.<br><br>
     *
     * For example<br>
     * LocationRequest: map=501<br>
     *
     * @return string representation
     */
    @Override
    public String toString(){
        return "LocationRequest: map=" + this.getMapId();
    }

    /**
     * Serializes LocationRequest to MessageOutput
     *
     * @param out serialization output destination
     * @throws IOException if I/O problem
     * @throws NullPointerException if out is null
     */
    @Override
    public void encode(MessageOutput out) throws IOException {
        //Encode the string in parts
        out.write(PROTOCOL_HEADER.getBytes(StandardCharsets.UTF_8));
        String str = " " + this.mapID + " ALL \r\n";
        out.write(str.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String getOperation() {
        return OPERATION;
    }
}
