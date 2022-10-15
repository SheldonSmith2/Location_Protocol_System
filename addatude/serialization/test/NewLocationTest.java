/*
 * Sheldon Smith
 * Course: CSI 4321
 */
import addatude.serialization.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class NewLocationTest {
    //The character set for getting the bytes
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    //The class to hold the constructor tests
    @DisplayName("Constructor Tests")
    @Nested
    class ConstructorTests{
        //Tests valid parameters in order to create a new NewLocation
        @DisplayName("Valid Constructor")
        @ParameterizedTest(name = "id = {0}")
        @ValueSource(longs = {1L, 100L, 0, 99999})
        public void validConstructorTest(long id) throws ValidationException {
            //Create a new instance of NewLocation
            LocationRecord record = new LocationRecord(1, "10.0", "10.0", "Cashion", "Study");
            NewLocation loc = new NewLocation(id, record);

            //Check that the NewLocation has correct data in fields
            assertNotNull(loc);
            assertEquals(record, loc.getLocationRecord());
            assertEquals(id, loc.getMapId());
            assertEquals("NEW", loc.getOperation());
        }

        //The test to check for ValidationExceptions with there are bad parameters (not null)
        @DisplayName("Invalid Constructor")
        @ParameterizedTest(name = "id = {0}")
        @ValueSource(longs = {-1L, -100L, 100000})
        public void invalidConstructorTest(long id) throws ValidationException {
            LocationRecord record = new LocationRecord(1, "10.0", "10.0", "Cashion", "Study");

            //Check that exception is thrown when id is negative
            assertThrows(ValidationException.class, () -> new NewLocation(id, record));
        }

        //The test to check for ValidationException if null is passed for message
        @DisplayName("Null Constructor")
        @Test
        public void nullConstructorTest(){
            //Check that exception is thrown when null is passed in as MessageLocation
            assertThrows(ValidationException.class, () -> new NewLocation(1, null));
        }
    }

    //The test to check that the toString method is working properly
    @DisplayName("Valid toString")
    @ParameterizedTest(name = "mapID = {0} userID = {1} lng = {2} lat = {3} name = {4} desc = {5} fullStr = {6}")
    @CsvSource({"501, 1, 10.0, 10.0, Cashion, Study, NewLocation: map=501 1:Cashion-Study (10.0?10.0)",
            "101, 2, 15.3, 29.1, Moody, Fun, NewLocation: map=101 2:Moody-Fun (15.3?29.1)"})
    public void validToStringTest(long mapID, long userid, String lng, String lat, String name, String desc, String fullStr) throws ValidationException {
        //Create a LocationRecord to be used to create NewLocation
        LocationRecord record = new LocationRecord(userid, lng, lat, name, desc);
        NewLocation loc = new NewLocation(mapID, record);
        fullStr = fullStr.replace("?", ",");

        //Check that the returned string matches expected string
        assertEquals(fullStr, loc.toString());
    }

    @DisplayName("Encode Test")
    @Nested
    class encodeTests {
        //The test to check that the encode function works properly
        @DisplayName("Valid Encode Tests")
        @ParameterizedTest(name = "mapID = {0} userID = {1} lng = {2} lat = {3} name = {4} desc = {5} encodeStr = {6}")
        @CsvSource({"501, 1, 10.0, 10.0, Cashion, Study, ADDATUDEv1 501 NEW 1 10.0 10.0 7 Cashion5 Study",
                "101, 2, 15.3, 29.1, Moody, Fun, ADDATUDEv1 101 NEW 2 15.3 29.1 5 Moody3 Fun"})
        public void encodeTest(long mapID, long userid, String lng, String lat, String name, String desc, String encodeStr) throws ValidationException, IOException {
            //Test that the encode function work correctly
            var bOut = new ByteArrayOutputStream();
            var out = new MessageOutput(bOut);
            LocationRecord record = new LocationRecord(userid, lng, lat, name, desc);

            //Encode the NewLocation
            new NewLocation(mapID, record).encode(out);
            String newEncodeStr = encodeStr + "\r\n";

            //Check that the bytes match
            assertArrayEquals(newEncodeStr.getBytes(CHARSET), bOut.toByteArray());
        }

        @DisplayName("Null Encode")
        @Test
        public void nullEncode(){
            assertThrows(NullPointerException.class, () -> new NewLocation(1,new LocationRecord(1, "1.1", "1.1", "test", "test")).encode(null));
        }
    }

    @DisplayName("Decode Tests")
    @Nested
    class decodeTests {
        //The test to check that the decode function works properly
        @DisplayName("Valid Decode Tests")
        @ParameterizedTest(name = "mapID = {0} userID = {1} lng = {2} lat = {3} name = {4} desc = {5} decodeStr = {6}")
        @CsvSource({"501, 1, 10.0, 10.0, Cashion, Study, ADDATUDEv1 501 NEW 1 10.0 10.0 7 Cashion5 Study",
                "101, 2, 15.3, 29.1, Moody, Fun, ADDATUDEv1 101 NEW 2 15.3 29.1 5 Moody3 Fun"})
        public void decodeTest(long mapID, long userid, String lng, String lat, String name, String desc, String decodeStr) throws ValidationException {
            //Create a NewLocation by passing a MessageInput
            String newDecodeStr = decodeStr + "\r\n";
            MessageInput mi = new MessageInput(new ByteArrayInputStream(newDecodeStr.getBytes(CHARSET)));
            NewLocation loc = (NewLocation) Message.decode(mi);
            LocationRecord record = new LocationRecord(userid, lng, lat, name, desc);
            assertNotNull(loc);

            //Check that the fields were set correctly
            assertEquals(mapID, loc.getMapId());
            assertEquals("NEW", loc.getOperation());
            assertEquals(record, loc.getLocationRecord());
        }

        @DisplayName("Null Decode")
        @Test
        public void nullDecode(){
            assertThrows(NullPointerException.class, () -> new NewLocation(1,new LocationRecord(1, "1.1", "1.1", "test", "test")).decode(null));
        }

        @DisplayName("Invalid Decode Test")
        @ParameterizedTest(name = "decodeStr = {0}")
        @ValueSource(strings = {"ADDATDEv1 501 NEW 1 10.0 10.0 7 Cashion5 Study", "ADDATUDEv1 100000 NEW 1 10.0 10.0 7 Cashion5 Study",
                "ADDATUDEv1 -1 NEW 1 10.0 10.0 7 Cashion5 Study", "ADDATUDEv1 501 NEW 1 10.0 10.0 7 Cashion-1 Study",
                "ADDATUDEv1 NEW 1 10.0 10.0 7 Cashion5 Study", "ADDATUDEv1 NEW 1 ", "ADDATUDEv1 NEW 1 10.0"})
        public void invalidDecodeTest(String decodeStr) {
            //Decode an Error by passing a MessageInput
            String newDecodeStr = decodeStr + "\r\n";
            MessageInput mi = new MessageInput(new ByteArrayInputStream(newDecodeStr.getBytes()));
            assertThrows(ValidationException.class, () -> NewLocation.decode(mi));
        }
    }

    //The class to test the mapID setter
    @DisplayName("MapID Setter Tests")
    @Nested
    class mapIDSetterTests{
        //The test for valid parameters for setting the mapID
        @DisplayName("Valid setMapID")
        @ParameterizedTest(name = "id = {0}")
        @ValueSource(longs = {1, 501, 101, 0, 99999})
        public void validSetMapIDTest(long id) throws ValidationException {
            LocationRecord record = new LocationRecord(1, "10.0", "10.0", "Cashion", "Study");
            //Create a Message instance
            NewLocation loc = new NewLocation(501, record);
            loc.setMapId(id);

            //Check that the id is set properly
            assertEquals(id, loc.getMapId());
        }

        //The test is to check for negative id exceptions
        @DisplayName("Invalid setMapID")
        @ParameterizedTest(name = "id = {0}")
        @ValueSource(longs = {-1, -501, 100000})
        public void invalidSetMapIDTest(long id) throws ValidationException {
            LocationRecord record = new LocationRecord(1, "10.0", "10.0", "Cashion", "Study");
            NewLocation loc = new NewLocation(501, record);

            //Check that exception is thrown when mapID is negative
            assertThrows(ValidationException.class, () -> loc.setMapId(id));
        }
    }

    //The class to test the LocationRecord setter
    @DisplayName("setLocationRecord Tests")
    @Nested
    class SetLocationRecordTests{
        //The test for valid parameters for setting the message
        @DisplayName("Valid setLocationRecord")
        @ParameterizedTest(name = "userid = {0}, lat = {1}, lng = {2}, name = {3}, desc = {4}")
        @CsvSource({"123, 12.34, 1.67, Hankamer, Class", "1, -12.0, -90.0, Hankamer, Class"})
        public void validSetLocationRecordTest(Long userid, String lng, String lat, String name, String desc) throws ValidationException {
            LocationRecord record = new LocationRecord(1, "10.0", "10.0", "Cashion", "Study");
            //Create instance of NewLocation
            NewLocation loc = new NewLocation(1, record);
            LocationRecord newRec = new LocationRecord(userid, lng, lat, name, desc);

            //Set the LocationRecord and check that it matches expected record
            loc.setLocationRecord(newRec);
            assertEquals(loc.getLocationRecord(), newRec);
        }

        //The test to check that a ValidationException is throw when null is passed to setter
        @DisplayName("Invalid setLocationRecord")
        @Test
        public void invalidSetLocationRecordTest() throws ValidationException {
            LocationRecord record = new LocationRecord(1, "10.0", "10.0", "Cashion", "Study");
            NewLocation loc = new NewLocation(1, record);

            //Check that exception is thrown when null is passed as LocationRecord
            assertThrows(ValidationException.class, () -> loc.setLocationRecord(null));
        }
    }

    //Class to test the equals function
    @DisplayName("Equals Tests")
    @Nested
    class equalsTests{
        //Test that the equals method is correct
        @DisplayName("Valid Equals Test")
        @Test
        public void validEquals() throws ValidationException {
            //Create two instances of LocationRecord with the same data and test that they are equal
            LocationRecord rec1 = new LocationRecord(1, "10.0", "10.0", "Moody", "Study");
            LocationRecord rec2 = new LocationRecord(1, "10.0", "10.0", "Moody", "Study");
            NewLocation loc = new NewLocation(1, rec1);
            NewLocation loc2 = new NewLocation(1, rec2);
            assertEquals(loc, loc2);
        }

        //The test to make sure equals does not work when the data does not match
        @DisplayName("Invalid Equals Test")
        @ParameterizedTest(name = "id = {0} lng = {1} lat = {2} name = {3} desc = {4}")
        @CsvSource({"2, 10.0, 10.0, Moody, Study", "1, 11.0, 10.0, Moody, Study", "1, 10.0, 11.0, Moody, Study", "1, 10.0, 10.0, Cashion, Study", "1, 10.0, 10.0, Moody, Work"})
        public void invalidEquals(long id, String lng, String lat, String name, String desc) throws ValidationException {
            LocationRecord rec1 = new LocationRecord(1, "10.0", "10.0", "Moody", "Study");
            //Create a LocationRecord with one of the fields different from the base case
            LocationRecord rec2 = new LocationRecord(id,lng,lat,name,desc);
            NewLocation loc = new NewLocation(1, rec1);
            NewLocation loc2 = new NewLocation(1, rec2);
            assertNotEquals(loc, loc2);
        }
    }

    //Class to test the equals function
    @DisplayName("hashCode Tests")
    @Nested
    class hashCodeTests{
        //Create two instances of LocationRecord with the same data and test that the hashes are the same
        @DisplayName("Valid hashCode Test")
        @Test
        public void validHashCode() throws ValidationException {
            LocationRecord rec1 = new LocationRecord(1, "10.0", "10.0", "Moody", "Study");
            LocationRecord rec2 = new LocationRecord(1, "10.0", "10.0", "Moody", "Study");
            NewLocation loc = new NewLocation(1, rec1);
            NewLocation loc2 = new NewLocation(1, rec2);
            assertEquals(loc.hashCode(), loc2.hashCode());
        }

        //Create two instances of LocationRecord with different data and test that the hashes are different
        @DisplayName("Invalid hashCode Test")
        @ParameterizedTest(name = "id = {0} lng = {1} lat = {2} name = {3} desc = {4}")
        @CsvSource({"2, 10.0, 10.0, Moody, Study", "1, 11.0, 10.0, Moody, Study", "1, 10.0, 11.0, Moody, Study", "1, 10.0, 10.0, Cashion, Study", "1, 10.0, 10.0, Moody, Work"})
        public void invalidHashCode(long id, String lng, String lat, String name, String desc) throws ValidationException {
            LocationRecord rec1 = new LocationRecord(1, "10.0", "10.0", "Moody", "Study");
            //Create a LocationRecord with one of the fields different from the base case
            LocationRecord rec2 = new LocationRecord(id,lng,lat,name,desc);
            NewLocation loc = new NewLocation(1, rec1);
            NewLocation loc2 = new NewLocation(1, rec2);
            assertNotEquals(loc.hashCode(), loc2.hashCode());
        }
    }
}
