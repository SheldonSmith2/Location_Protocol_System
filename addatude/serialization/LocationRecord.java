/************************************************
 *
 * Author: Sheldon
 * Assignment: Program 0
 * Class: CSI 4321
 *
 ************************************************/
package addatude.serialization;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 *
 * Represents a location record and provides serialization/deserialization
 *
 * @author Sheldon Smith
 * @version 1.1
 */
public class LocationRecord implements Comparable<LocationRecord> {
    /**The variable to hold the userID*/
    private long userID;
    /**The variable to hold the location's latitude and longitude*/
    private String latitude, longitude;
    /**The variable to hold the location's name and description*/
    private String locationName, locationDescription;

    /**
     * Constructs location record with set values
     *
     * @param  userId  ID for user
     * @param  longitude position of location
     * @param  latitude  position of location
     * @param  locationName name of location
     * @param  locationDescription description of location
     * @throws addatude.serialization.ValidationException if validation fails (e.g., null longitude, etc.)
     */
    public LocationRecord(long userId, String longitude, String latitude, String locationName, String locationDescription)
            throws addatude.serialization.ValidationException{
        //Set the designated fields using the set methods
        setUserId(userId);
        setLongitude(longitude);
        setLatitude(latitude);
        setLocationName(locationName);
        setLocationDescription(locationDescription);
    }

    /**
     * Constructs location record with null values
     */
    public LocationRecord(){

    }

    /**
     * Constructs location record using deserialization
     *
     * @param  in  deserialization input source
     * @throws NullPointerException  if in is null
     * @throws addatude.serialization.ValidationException if validation failure (e.g., illegal user ID), I/O problem, etc.
     */
    public LocationRecord(MessageInput in) throws addatude.serialization.ValidationException, NullPointerException {
        //Check if the MessageInput is null
        if (Objects.isNull(in)){
            throw new NullPointerException("Variable 'in' cannot be null");
        }
        //Read in the bytes from the MessageInput
        try {
            this.readLocationRecord(in);
        }catch (IOException e){
            //Throw a ValidationException is an IOException occurs
            throw new ValidationException("IOException", "Reading failed for LocationRecord", e);
        }
    }

    /**
     * Reads through a MessageInput and populates the given LocationRecord fields
     *
     * @param in the MessageInput to be read from
     * @throws ValidationException if error in setting fields
     * @throws IOException if read fails
     * @return the number of bytes read
     */
    public int readLocationRecord(MessageInput in) throws ValidationException, IOException {
        //Initialize variables
        int numRead = 0, currByte;

        //Read in the userID
        String str = in.readUntilSpace();
        try {
            this.setUserId(Long.parseLong(str));
        } catch (NumberFormatException e){
            throw new ValidationException(str, "Error converting to long", e);
        }

        //Read in longitude
        str = in.readUntilSpace();
        this.setLongitude(str);

        //Read in latitude
        str = in.readUntilSpace();
        this.setLatitude(str);

        try {
            //Find the strings for the name and description
            for (int i = 0; i < 2; i++) {
                //Find how many characters need to be read
                str = in.readUntilSpace();
                //Check that the number is not negative
                int num = Integer.parseInt(str);
                if (Validation.isInvalidNumber(num)) {
                    throw new ValidationException(str, "The number cannot be negative");
                }
                //Reset the string
                StringBuilder currStr = new StringBuilder();
                numRead++;
                //Read in the correct number of bytes for the string
                for (int j = 0; j < num; j++) {
                    currByte = in.read();
                    if (currByte == -1) {
                        throw new IOException();
                    }
                    //Add character to string
                    currStr.append((char) currByte);
                    numRead++;
                }

                //Check that string does not contain \n
                if (currStr.toString().contains("\n")) {
                    throw new IOException();
                }

                //Check which field should be set
                if (i == 0) {
                    this.setLocationName(currStr.toString());
                } else {
                    this.setLocationDescription(currStr.toString());
                }
            }
        } catch (NumberFormatException e) {
            //Throw a ValidationException if a NumberFormatException occurs
            throw new ValidationException("NumberFormatException", "Error converting number", e);
        }

        //Return the number of bytes read
        return numRead;
    }

    /**
     * Serializes location record
     *
     * @param  out  serialization output destination
     * @throws NullPointerException  if out is null
     * @throws IOException  if I/O problem
     */
    public void encode(MessageOutput out) throws IOException{
        //Check is the MessageOutput is null
        if (Objects.isNull(out)){
            throw new NullPointerException("out cannot be null");
        }
        //Create the encoded LocationRecord string
        String str = this.userID + " ";
        out.write(str.getBytes(StandardCharsets.UTF_8));

        str = this.longitude + " ";
        out.write(str.getBytes(StandardCharsets.UTF_8));

        str = this.latitude + " ";
        out.write(str.getBytes(StandardCharsets.UTF_8));

        str = this.locationName.length() + " " + this.locationName;
        out.write(str.getBytes(StandardCharsets.UTF_8));

        str = this.locationDescription.length() + " " + this.locationDescription;
        out.write(str.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Returns a String representation of the following form:<br><br>
     * &lt;userID&gt;:&lt;locationName&gt;-&lt;locationDescription&gt;_(&lt;longitude&gt;,&lt;latitude&gt;)<br><br>
     * Underscore indicates a space. You must follow the spacing, etc. precisely.<br><br>
     * For example<br><br>
     * 101:Stadium-Champions (75,65)<br>
     *
     * @return string representation
     */
    @Override
    public String toString(){
        //Return the correct string representation
        return this.userID + ":" + this.locationName + "-" + this.locationDescription + " (" +
                this.longitude + "," + this.latitude + ")";
    }

    /**
     * Returns user ID
     *
     * @return user ID
     */
    public final long getUserId(){
        return this.userID;
    }

    /**
     * Sets user ID
     *
     * @param userId new user ID
     * @return LocationRecord with new value
     * @throws addatude.serialization.ValidationException if validation fails (userid invalid, etc.)
     */
    public final LocationRecord setUserId(long userId) throws addatude.serialization.ValidationException{
        //Check if the userId is within the bounds
        if (Validation.isInvalidNumber(userId)){
            throw new ValidationException(String.valueOf(userId), "Invalid userID");
        }

        //Set the value and return self
        this.userID = userId;
        return this;
    }

    /**
     * Returns longitude
     *
     * @return longitude
     */
    public final String getLongitude(){
        return this.longitude;
    }

    /**
     * Sets longitude
     *
     * @param longitude  new longitude
     * @return LocationRecord with new value
     * @throws addatude.serialization.ValidationException if validation fails (longitude invalid, etc.)
     */
    public final LocationRecord setLongitude(String longitude) throws addatude.serialization.ValidationException{
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
    public final String getLatitude(){
        return this.latitude;
    }

    /**
     * Sets latitude
     *
     * @param latitude new latitude
     * @return LocationRecord with new value
     * @throws addatude.serialization.ValidationException if validation fails (latitude invalid, etc.)
     */
    public final LocationRecord setLatitude(String latitude) throws addatude.serialization.ValidationException{
        //Check that the latitude is valid
        if (Validation.isInvalidLatitude(latitude)){
            throw new ValidationException(latitude, "Invalid Latitude");
        }
        //Set the value and return self
        this.latitude = Objects.requireNonNull(latitude);
        return this;
    }

    /**
     * Returns location name
     *
     * @return location name
     */
    public final String getLocationName(){
        return locationName;
    }

    /**
     * Sets location name
     *
     * @param locationName new location name
     * @return LocationRecord with new value
     * @throws addatude.serialization.ValidationException if validation fails (name==null, etc.)
     */
    public final LocationRecord setLocationName(String locationName) throws addatude.serialization.ValidationException{
        //Check to make sure locationName is valid
        if (Validation.isInvalidString(locationName)){
            throw new ValidationException(locationName, "Invalid locationName");
        }

        //Set the value and return self
        this.locationName = Objects.requireNonNull(locationName);
        return this;
    }

    /**
     * Returns location description
     *
     * @return location description
     */
    public final String getLocationDescription(){
        return this.locationDescription;
    }

    /**
     * Sets location description
     *
     * @param locationDescription  new location description
     * @return LocationRecord with new value
     * @throws addatude.serialization.ValidationException  if validation fails (name==null, etc.
     */
    public final LocationRecord setLocationDescription(String locationDescription) throws addatude.serialization.ValidationException{
        //Check to make sure locationDescription is not null
        if (Validation.isInvalidString(locationDescription)){
            throw new ValidationException(locationDescription, "Invalid locationDescription");
        }

        //Set the value and return self
        this.locationDescription = Objects.requireNonNull(locationDescription);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        //Check that two LocationRecords match
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocationRecord that = (LocationRecord) o;
        //Return whether they equal or not
        return userID == that.userID && latitude.equals(that.latitude) && longitude.equals(that.longitude) && locationName.equals(that.locationName) && locationDescription.equals(that.locationDescription);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userID, latitude, longitude, locationName, locationDescription);
    }

    @Override
    public int compareTo(LocationRecord o) {
        return this.getLocationName().compareTo(o.getLocationName());
    }
}

