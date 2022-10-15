/************************************************
 *
 * Author: Sheldon
 * Assignment: Program 4
 * Class: CSI 4321
 *
 ************************************************/
package notifi.serialization;

import addatude.serialization.Validation;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * NoTiFi location addition
 *
 * @author Sheldon Smith
 * @version 1.0
 */
public class NoTiFiLocationAddition extends NoTiFiMessage{
    /** The specific code for the LocationAddition */
    public static final int CODE = 1;
    /**The variable to hold the user id*/
    private int userID;
    /**The variables to hold the location's longitude and latitude*/
    private double longitude, latitude;
    /**The variables to hold the location's name and description*/
    private String locationName, locationDescription;

    /**Constant to hold the minimum size of a location addition packet*/
    private final int MIN_SIZE_LOCADD = 24;
    /**Constant to hold the maximum length of a string*/
    public static final int MAX_STRING_LENGTH = 255;

    /**
     * Constructs NoTiFi add location message
     *
     * @param msgId message id
     * @param userId user id
     * @param longitude longitude of new location
     * @param latitude latitude of new location
     * @param locationName name of new location
     * @param locationDescription description of new location
     * @throws IllegalArgumentException if validation fails
     */
    public NoTiFiLocationAddition(int msgId, int userId, double longitude, double latitude, String locationName, String locationDescription) {
        super(msgId);
        setUserId(userId);
        setLongitude(longitude);
        setLatitude(latitude);
        setLocationName(locationName);
        setLocationDescription(locationDescription);
    }

    /**
     * Constructs NoTiFi add location message from byte array
     *
     * @param msgId message ID
     * @param pkt byte array
     * @throws IllegalArgumentException if validation fails
     */
    public NoTiFiLocationAddition(int msgId, byte[] pkt) throws IllegalArgumentException{
        //Set the message id
        super(msgId);

        if (pkt.length < MIN_SIZE_LOCADD){
            throw new IllegalArgumentException("Packet is less that minimum size of LocationAddition");
        }
        int currIndex = HEADER_SIZE;
        byte[] temp = new byte[4];
        //Read in 4-bytes for userID into a new byte array
        try {
            System.arraycopy(pkt, currIndex, temp, 0, 4);
        } catch (ArrayIndexOutOfBoundsException e){
            throw new IllegalArgumentException("Array check out of bounds", e);
        }
        //Increment the current index appropriately
        currIndex+=4;

        int userId;
        try {
            //Convert the byte array to int for the userID
            userId = byteArrayToInt(temp, "Big");
        } catch (Exception e){
            throw new IllegalArgumentException("Error converting userID", e);
        }

        double lng = 0, lat = 0;
        //Find the values for longitude and latitude
        for (int i = 0; i < 2; i++){
            //Read in 8 bytes for double
            temp = new byte[8];
            //Read in 8-bytes for the double into a new byte array
            try {
                System.arraycopy(pkt, currIndex, temp, 0, 8);
            } catch (ArrayIndexOutOfBoundsException e){
                throw new IllegalArgumentException("Array check out of bounds", e);
            }

            //Increment the current index appropriately
            currIndex+=8;

            //Set to appropriate double value
            try{
                switch (i) {
                    case 0 ->
                            //Convert the byte array (IEEE 754) to double for the longitude
                            lng = byteArrayToDouble(temp);
                    case 1 ->
                            //Convert the byte array (IEEE 754) to double for the longitude
                            lat = byteArrayToDouble(temp);
                }
            } catch (Exception e){
                throw new IllegalArgumentException("Error converting double value", e);
            }
        }

        String name = "", desc = "";
        //Find the values for locationName and locationDescription
        for (int i = 0; i < 2; i++){
            //Convert from byte to int for the length of the string
            int length;
            try {
                length = byteArrayToInt(new byte[]{0, 0, 0, pkt[currIndex]}, "Big");
            } catch (IndexOutOfBoundsException e){
                throw new IllegalArgumentException("Index out of range reading location name", e);
            }
            currIndex++;

            StringBuilder tempStr = new StringBuilder();
            //Loop through the bytes for the string
            for (int j = 0; j < length; j++){
                //Convert from byte to char and add it to the temp string
                try {
                    tempStr.append((char) pkt[j + currIndex]);
                } catch (IndexOutOfBoundsException e){
                    throw new IllegalArgumentException("Index out of range reading location name", e);
                }
            }
            currIndex += length;
            //Set the appropriate value ofr either the name or description
            switch (i) {
                case 0 -> name = tempStr.toString();
                case 1 -> desc = tempStr.toString();
            }
        }

        if (pkt.length > MIN_SIZE_LOCADD + name.length() + desc.length()){
            throw new IllegalArgumentException("Byte array too large");
        }

        //Set the attributes of the NoTiFi location addition
        setUserId(userId);
        setLongitude(lng);
        setLatitude(lat);
        setLocationName(name);
        setLocationDescription(desc);
    }

    public byte[] encode(){
        //Encode the header into byte array
        byte[] totalBytes = encodeHeader(CODE);

        //Convert userID to 4-byte big endian int and add to byte array
        totalBytes = combineArrays(totalBytes, intToByteArray(userID, "Big"));

        //Convert lng to 8-byte IEEE 754 floating point and add to byte array
        totalBytes = combineArrays(totalBytes, doubleToByteArray(longitude));

        //Convert lat to 8-byte IEEE 754 floating point and add to byte array
        totalBytes = combineArrays(totalBytes, doubleToByteArray(latitude));

        //Convert name length to one byte int and add to byte array
        totalBytes = combineArrays(totalBytes, new byte[]{(byte) locationName.length()});
        //Convert name string to bytes and add to byte array
        totalBytes = combineArrays(totalBytes, locationName.getBytes(StandardCharsets.UTF_8));

        //Convert name length to one byte int and add to byte array
        totalBytes = combineArrays(totalBytes, new byte[]{(byte) locationDescription.length()});
        //Convert description string to bytes and add to byte array
        totalBytes = combineArrays(totalBytes, locationDescription.getBytes(StandardCharsets.UTF_8));

        //Convert the byte array
        return totalBytes;
    }

    /**
     * Returns a String representation<br><br>
     * Addition:_msgid=&lt;msgid&gt;_userid=&lt;userID&gt;:&lt;locationName&gt;-&lt;locationDescription&gt;_(&lt;longitude&gt;,&lt;latitude&gt;)<br><br>
     * Underscore indicates a space. You <strong>must</strong> follow the spacing, etc. precisely. longitude/latitude must be rounded.<br><br>
     * For example<br><br>
     * Addition: msgid=253 101:Stadium-Champions (75,-65)
     *
     * @return string representation
     */
    @Override
    public String toString(){
        return "Addition: msgid=" + msgID + " " + this.userID + ":" +this.locationName + "-" +
                this.locationDescription + " (" + Math.round(this.longitude) + "," + Math.round(this.latitude) + ")";
    }

    /**
     * Returns user ID
     *
     * @return user ID
     */
    public int getUserId(){
        return this.userID;
    }

    /**
     * Sets user ID
     *
     * @param userId new user ID
     * @return this object with new value
     * @throws IllegalArgumentException if user ID out of range
     */
    public NoTiFiLocationAddition setUserId(int userId) throws IllegalArgumentException{
        //Check that the number is valid
        if (Validation.isInvalidNumber(userId)){
            throw new IllegalArgumentException("Invalid userID");
        }
        this.userID = userId;
        return this;
    }

    /**
     * Returns longitude
     *
     * @return longitude
     */
    public double getLongitude(){
        return this.longitude;
    }

    /**
     * Sets longitude
     *
     * @param longitude new longitude
     * @return this object with new value
     * @throws IllegalArgumentException if longitude out of range
     */
    public NoTiFiLocationAddition setLongitude(double longitude) throws IllegalArgumentException{
        //Check that the longitude is valid
        if (Validation.isInvalidLongitude(String.valueOf(longitude))){
            throw new IllegalArgumentException("Longitude out of range");
        }
        this.longitude = longitude;
        return this;
    }

    /**
     * Returns latitude
     *
     * @return latitude
     */
    public double getLatitude(){
        return this.latitude;
    }

    /**
     * Sets latitude
     *
     * @param latitude new latitude
     * @return this object with new value
     * @throws IllegalArgumentException if latitude out of range
     */
    public NoTiFiLocationAddition setLatitude(double latitude) throws IllegalArgumentException{
        //Check that the latitude is valid
        if (Validation.isInvalidLatitude(String.valueOf(latitude))){
            throw new IllegalArgumentException("Latitude out of range");
        }
        this.latitude = latitude;
        return this;
    }

    /**
     * Returns location name
     *
     * @return location name
     */
    public String getLocationName(){
        return this.locationName;
    }

    /**
     * Sets location name
     *
     * @param locationName new location name
     * @return this object with new value
     * @throws IllegalArgumentException if name null, too long, or contains illegal characters
     */
    public NoTiFiLocationAddition setLocationName(String locationName) throws IllegalArgumentException{
        //Check that the location name is valid
        if (Validation.isInvalidString(locationName) || locationName.length() > MAX_STRING_LENGTH  || !locationName.matches(ASCII_REGEX)){
            throw new IllegalArgumentException("locationName is invalid");
        }
        this.locationName = locationName;
        return this;
    }

    /**
     * Returns location description
     *
     * @return location description
     */
    public String getLocationDescription(){
        return this.locationDescription;
    }

    /**
     * Sets location description
     *
     * @param locationDescription new location description
     * @return this object with new value
     * @throws IllegalArgumentException if description null, too long, or non-ASCII
     */
    public NoTiFiLocationAddition setLocationDescription(String locationDescription) throws IllegalArgumentException{
        //Check that the location description is valid
        if (Validation.isInvalidString(locationDescription) || locationDescription.length() > MAX_STRING_LENGTH || !locationDescription.matches(ASCII_REGEX)){
            throw new IllegalArgumentException("locationDescription is invalid");
        }
        this.locationDescription = locationDescription;
        return this;
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
        NoTiFiLocationAddition that = (NoTiFiLocationAddition) o;
        return userID == that.userID && Double.compare(that.longitude, longitude) == 0 && Double.compare(that.latitude, latitude) == 0 && locationName.equals(that.locationName) && locationDescription.equals(that.locationDescription);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), userID, longitude, latitude, locationName, locationDescription);
    }
}
