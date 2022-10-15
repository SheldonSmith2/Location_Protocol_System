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
import java.util.*;

/**
 * Represents a location response message
 *
 * @version 1.1
 * @author Sheldon Smith
 */
public class LocationResponse extends Message{
    /** The string that contains the operation type */
    public static final String OPERATION = "RESPONSE";

    /** The variable to hold the mapName */
    private String mapName;
    /** The variable to hold the list of LocationRecords */
    private List<LocationRecord> locationRecordList = new ArrayList<>();

    /**
     * Contructs location response using set values
     *
     * @param mapId ID for message map
     * @param mapName name of location
     * @throws ValidationException if validation fails
     */
    public LocationResponse(long mapId, String mapName) throws ValidationException{
        //Set the values using setters
        this.setMapName(mapName);
        this.setMapId(mapId);
    }

    /**
     * Constructs location response using a MessageInput
     *
     * @param in the MessageInput to read from
     * @param mapID ID for message map
     * @throws IOException if read fails
     * @throws ValidationException if validation fails
     */
    public LocationResponse(MessageInput in, long mapID) throws IOException, ValidationException {
        StringBuilder mapName = new StringBuilder();
        int numLocations, nameLength;

        //Read in the length of the mapName
        String str = in.readUntilSpace();
        nameLength = Integer.parseInt(str);
        //Check that the length is not negative
        if (nameLength < 0) {
            throw new ValidationException(str, "The number cannot be negative");
        }
        //Read in the mapName
        for (int i = 0; i < nameLength; i++) {
            int curr = in.read();
            in.checkValidRead(curr);
            mapName.append((char) curr);
        }

        //Read in the number of locations
        str = in.readUntilSpace();
        numLocations = Integer.parseInt(str);
        if (Validation.isInvalidNumber(numLocations)) {
            throw new ValidationException(String.valueOf(numLocations), "The number of locations is out of bounds");
        }

        //Create the LocationResponse
        this.setMapId(mapID);
        this.setMapName(mapName.toString());

        //Loop through the rest of the input stream and create the LocationRecords
        for (int i = 0; i < numLocations; i++) {
            LocationRecord rec = new LocationRecord(in);

            //Add the LocationRecord to the list
            this.addLocationRecord(rec);
        }
        in.checkForEOS();
    }

    /**
     * Returns a String representation<br>
     * LocationResponse:_map=&lt;mapID&gt;_&lt;mapName&gt;_&lt;mapName&gt;_[&lt;loc1&gt;,...,&lt;locN&gt;]<br><br>
     *
     * Underscore indicates a space. You must follow the spacing, etc. precisely.<br><br>
     *
     * For example<br>
     * LocationResponse: map=501 Baylor [101:Stadium-Champions (75.04,65.3),102:Library-Study location (35.2,25.0)]<br>
     *
     * @return string representation
     */
    @Override
    public String toString(){
        //Start creating string for LocationResponse
        StringBuilder str = new StringBuilder("LocationResponse: map=" + this.getMapId() + " " + this.mapName + " [");

        //For each record in the list, add the string representation to the overall string
        for (int i = 0; i < this.locationRecordList.size(); i++){
            str.append(this.locationRecordList.get(i).toString());
            if (i != this.locationRecordList.size()-1){
                str.append(",");
            }
        }
        //Return the string representation
        return str + "]";
    }

    /**
     * Returns map name
     *
     * @return map name
     */
    public String getMapName(){
        return this.mapName;
    }

    /**
     * Sets map name
     *
     * @param mapName new name
     * @return this object with new value
     * @throws ValidationException if validation fails (i.e., mapName is null)
     */
    public LocationResponse setMapName(String mapName) throws ValidationException{
        //Check that the mapName is valid
        if (Validation.isInvalidString(mapName)){
            throw new ValidationException(mapName, "The mapName cannot be null");
        }

        //Set the value and return self
        this.mapName = Objects.requireNonNull(mapName);
        return this;
    }

    /**
     * Returns (possibly empty) list of map locations
     *
     * @return map locations
     */
    public List<LocationRecord> getLocationRecordList(){
        List<LocationRecord> newList = new ArrayList<>();
        for (LocationRecord locationRecord : this.locationRecordList) {
            newList.add(locationRecord);
        }
        return newList;
    }

    /**
     * Adds new location
     *
     * @param location new location to add
     * @return this object with new value
     * @throws ValidationException if validation fails (i.e., null location)
     */
    public LocationResponse addLocationRecord(LocationRecord location) throws ValidationException{
        //Check that the location is not null
        if (Objects.isNull(location)){
            throw new ValidationException("Bad Argument", "Validation failed for adding LocationRecord");
        }

        //Add location to list and return self
        this.locationRecordList.add(location);
        return this;
    }

    /**
     * deletes a location from the list
     *
     * @param userID the userID to delete if found
     */
    public void deleteLocation(long userID){
        boolean found = false;
        //Loop through the LocationRecord list
        for (int i = 0; i < locationRecordList.size() && !found; i++){
            //If found, delete from list
            if (locationRecordList.get(i).getUserId() == userID){
                locationRecordList.remove(i);
                found = true;
            }
        }
    }

    /**
     * Serializes LocationResponse to MessageOutput
     *
     * @param out serialization output destination
     * @throws IOException if I/O problem
     * @throws NullPointerException if out is null
     */
    @Override
    public void encode(MessageOutput out) throws IOException {
        //Encode LocationResponse is parts
        String str = PROTOCOL_HEADER + " " + this.mapID;
        out.write(str.getBytes(StandardCharsets.UTF_8));

        str = " RESPONSE " + this.mapName.length();
        out.write(str.getBytes(StandardCharsets.UTF_8));

        str = " " + this.mapName + this.locationRecordList.size() + " ";
        out.write(str.getBytes(StandardCharsets.UTF_8));

        //Loop through the records in the list and encode them
        for (LocationRecord locationRecord : this.locationRecordList) {
            locationRecord.encode(out);
        }
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
        LocationResponse that = (LocationResponse) o;
        return mapName.equals(that.mapName) && locationRecordList.equals(that.locationRecordList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), mapName, locationRecordList);
    }
}
