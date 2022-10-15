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
 * Represents a new location
 *
 * @version 1.1
 * @author Sheldon Smith
 */
public class NewLocation extends Message{
    /** The string that contains the operation type */
    public static final String OPERATION = "NEW";

    /** The variable to hold the LocationRecord */
    private LocationRecord locationRecord;

    /**
     * Constructs new location using set values
     *
     * @param mapId  ID for message map
     * @param location new location
     * @throws ValidationException if validation fails (e.g., null location record)
     */
    public NewLocation(long mapId, LocationRecord location) throws ValidationException{
        //Set the values using the setters
        this.setLocationRecord(location);
        this.setMapId(mapId);
    }

    /**
     * Constructs new location from a MessageInput
     *
     * @param in the MessageInput to read from
     * @param mapID ID for message map
     * @throws ValidationException if validation fails
     * @throws IOException if read fails
     */
    public NewLocation(MessageInput in, long mapID) throws ValidationException, IOException {
        //Read in a LocationRecord by passing the MessageInput
        LocationRecord record = new LocationRecord(in);
        in.checkForEOS();

        //Return the correct NewLocation object
        this.setMapId(mapID);
        this.setLocationRecord(record);
    }

    /**
     * Returns a String representation<br>
     * NewLocation:_map=&lt;mapID&gt;_&lt;newloc&gt;<br><br>
     *
     * Underscore indicates a space. You must follow the spacing, etc. precisely.<br><br>
     *
     * For example<br>
     * NewLocation: map=501 101:Stadium-Champions (75.04,65.3)<br>
     *
     * @return string representation
     */
    @Override
    public String toString(){
        return "NewLocation: map=" + this.getMapId() + " " + this.locationRecord.toString();
    }

    /**
     * Returns location
     *
     * @return location record
     */
    public LocationRecord getLocationRecord(){
        return this.locationRecord;
    }

    /**
     * Sets location
     *
     * @param lr new location
     * @return this object with new value
     * @throws ValidationException if null location record
     */
    public NewLocation setLocationRecord(LocationRecord lr) throws ValidationException{
        //Check that the location record is not null
        if (Objects.isNull(lr)){
            throw new ValidationException("Bad Argument", "LocationRecord is null");
        }

        //Set value and return self
        this.locationRecord = new LocationRecord(lr.getUserId(), lr.getLongitude(), lr.getLatitude(), lr.getLocationName(), lr.getLocationDescription());
        return this;
    }

    /**
     * Serializes NewLocation to MessageOutput
     *
     * @param out serialization output destination
     * @throws IOException if I/O problem
     * @throws NullPointerException if out is null
     */
    @Override
    public void encode(MessageOutput out) throws IOException {
        //Start the encode NewLocation
        String str = PROTOCOL_HEADER + " " + this.mapID + " NEW ";
        out.write(str.getBytes(StandardCharsets.UTF_8));

        //Encode the LocationRecord in the NewLocation
        this.locationRecord.encode(out);
        out.write("\r\n".getBytes(StandardCharsets.UTF_8));
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
        NewLocation that = (NewLocation) o;
        return locationRecord.equals(that.locationRecord);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), locationRecord);
    }
}
