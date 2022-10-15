/************************************************
 *
 * Author: Sheldon
 * Assignment: Program 2
 * Class: CSI 4321
 *
 ************************************************/
package addatude.serialization;

/**
 * Validation functions for multiple inputs for AddATude protocol
 *
 * @version 1.1
 * @author Sheldon Smith
 */
public class Validation {
    /** the mapID variable for the superclasses of Message */
    final static String illegalCharPattern = ".*\\p{C}.*";
    final static String doublePattern = "^-?[0-9]+\\.[0-9]+$";
    final static String backslash = "\n";
    /** The variable to hold the minimum value of an integer */
    final static int MIN_INT = 0;
    /** The variable to hold the maximum value of an integer */
    final static int MAX_INT = 99999;

    final static int MAX_DOUBLE_LENGTH = 10;
    final static int MAX_LNG = 180;
    final static int MIN_LNG = -180;
    final static int MAX_LAT = 90;
    final static int MIN_LAT = -90;

    /**
     * Checks whether the given long is valid
     *
     * @param num the number to be validated
     * @return whether the long is valid
     */
    public static boolean isInvalidNumber(long num){
        return (num > MAX_INT || num < MIN_INT);
    }

    /**
     * Checks whether the given string is valid
     *
     * @param str the string to be validated
     * @return whether the string is valid
     */
    public static boolean isInvalidString(String str){
        return (str == null || str.matches(illegalCharPattern) || str.length() > MAX_INT || str.contains(backslash));
    }

    /**
     * Checks whether the given latitude is valid
     *
     * @param lat the latitude to be validated
     * @return whether the latitude is valid
     */
    public static boolean isInvalidLatitude(String lat){
        return (lat == null || !lat.matches(doublePattern) || !(Double.parseDouble(lat) >= MIN_LAT) || !(Double.parseDouble(lat) <= MAX_LAT) || lat.length() > MAX_DOUBLE_LENGTH);
    }

    /**
     * Checks whether the given longitude is valid
     *
     * @param lng the longitude to be validated
     * @return whether the longitude is valid
     */
    public static boolean isInvalidLongitude(String lng){
        return (lng == null || !lng.matches(doublePattern) || !(Double.parseDouble(lng) >= MIN_LNG) || !(Double.parseDouble(lng) <= MAX_LNG) || lng.length() > MAX_DOUBLE_LENGTH);
    }
}
