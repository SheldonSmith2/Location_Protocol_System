/************************************************
 *
 * Author: Sheldon
 * Assignment: Program 6
 * Class: CSI 4321
 *
 ************************************************/
package addatude.serialization;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Represents a location location request
 *
 * @author Sheldon Smith
 * @version 1.0
 */
public class LocalLocationRequest extends Message{
    /** The string that contains the operation type */
    public static final String OPERATION = "LOCAL";

    /**The variables to hold the latitude and longitude*/
    private String latitude, longitude;

    /**
     * Constructs local location request using set values
     *
     * @param mapId ID for message map
     * @param longitude longitude of center of request
     * @param latitude latitude of center of request
     * @throws ValidationException if validation fails (e.g., null longitude, etc.)
     */
    public LocalLocationRequest(long mapId, String longitude, String latitude) throws ValidationException {
        this.setMapId(mapId);
        this.setLatitude(latitude);
        this.setLongitude(longitude);
    }

    /**
     * Constructs local location request using a MessageInput
     *
     * @param in MessageInput to read from
     * @param mapID ID for message map
     * @throws ValidationException if validation fails
     * @throws IOException if read fails
     */
    public LocalLocationRequest(MessageInput in, long mapID) throws ValidationException, IOException{
        //Loop twice to decode the longitude and latitude
        for (int i = 0; i < 2; i++){
            String str = in.readUntilSpace();
            //Set the correct attribute
            switch (i) {
                case 0 -> this.setLongitude(str);
                case 1 -> this.setLatitude(str);
            }
        }
        in.checkForEOS();

        //Set the map id
        this.setMapId(mapID);
    }

    /**
     * Returns a String representation<br>
     * LocalLocationRequest:_map=&lt;mapID&gt;_at_(&lt;longitude&gt;,&lt;latitude&gt;)<br><br>
     *
     * Underscore indicates a space. You must follow the spacing, etc. precisely.<br><br>
     *
     * For example<br>
     * LocalLocationRequest: map=501 at (35.3,45.4)<br>
     *
     * @return string representation
     */
    @Override
    public String toString(){
        return "LocalLocationRequest: map=" + mapID + " at (" + this.longitude + "," + this.latitude + ")";
    }

    /**
     * Return longitude
     *
     * @return longitude
     */
    public String getLongitude(){
        return this.longitude;
    }

    /**
     * Sets longitude
     *
     * @param longitude new longitude
     * @return this object with new value
     * @throws ValidationException if validation fails
     */
    public LocalLocationRequest setLongitude(String longitude) throws ValidationException{
        //Check that the longitude is valid
        if (Validation.isInvalidLongitude(longitude)){
            throw new ValidationException(longitude, "Invalid Longitude");
        }
        //Set the value and return self
        this.longitude = Objects.requireNonNull(longitude);

        return this;
    }

    /**
     * Returns latitude
     *
     * @return latitude
     */
    public String getLatitude(){
        return this.latitude;
    }

    /**
     * Sets latitude
     *
     * @param latitude new latitude
     * @return this object with new value
     * @throws ValidationException if validation fails
     */
    public LocalLocationRequest setLatitude(String latitude) throws ValidationException{
        //Check that the latitude is valid
        if (Validation.isInvalidLatitude(latitude)){
            throw new ValidationException(latitude, "Invalid Latitude");
        }
        //Set the value and return self
        this.latitude = Objects.requireNonNull(latitude);
        return this;
    }

    @Override
    public void encode(MessageOutput out) throws IOException {
        //Encode the protocol header
        out.write(PROTOCOL_HEADER.getBytes(StandardCharsets.UTF_8));

        String str = " " + this.mapID + " LOCAL ";
        out.write(str.getBytes(StandardCharsets.UTF_8));

        //Encode the longitude and latitude
        str = this.longitude + " ";
        out.write(str.getBytes(StandardCharsets.UTF_8));

        str = this.latitude + " \r\n";
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
        LocalLocationRequest request = (LocalLocationRequest) o;
        return latitude.equals(request.latitude) && longitude.equals(request.longitude);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), latitude, longitude);
    }
}
