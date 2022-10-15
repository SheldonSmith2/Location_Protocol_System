/*
 * Sheldon Smith
 * Course: CSI 4321
 */

import addatude.serialization.Error;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import addatude.serialization.*;
import org.junit.jupiter.api.*;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

public class LocalLocationRequestTest {
    //Nested tests to test the constructors
    @DisplayName("Contructor Tests")
    @Nested
    class Constructor {
        @ParameterizedTest(name = "mapid = {0}, lat = {1}, lng = {2}")
        @DisplayName("Valid Constructor")
        @CsvSource({"123, 12.34, 1.67", "1, -12.0, -90.0", "0, -12.321, -90.0", "99999, -15.0, -84.0"})
        public void testFirstConstructor(Long mapid, String lat, String lng) throws ValidationException {
            //Create a LocationRecord with the given data
            LocalLocationRequest request = new LocalLocationRequest(mapid, lng, lat);
            assertNotNull(request);

            //Check that each field is set properly
            assertEquals(mapid, request.getMapId());
            assertEquals(lat, request.getLatitude());
            assertEquals(lng, request.getLongitude());
            assertEquals("LOCAL", request.getOperation());
        }

        @ParameterizedTest(name = "mapid = {0}, lat = {1}, lng = {2}")
        @DisplayName("First Constructor Exceptions")
        @CsvSource({"-1, -12.0, -90.0", "1, 200.0, -90.0", "1, 100.0, -91.0", "100000, 12.34, 1.67"})
        public void testConstructorExceptions(Long mapid, String lat, String lng){
            //Check that an exception is thrown when a bad field is given
            ValidationException ex = assertThrows(ValidationException.class, () -> new LocalLocationRequest(mapid, lng, lat));
            assertNotNull(ex.getMessage());
        }

    }

    //Function to test the toString function
    @DisplayName("toString Test")
    @ParameterizedTest(name = "mapid = {0}  lng = {1}  lat = {2}")
    @CsvSource({"1, 1.2, 3.4, LocalLocationRequest: map=1 at (1.2?3.4)", "345, 15.6, 83.45, LocalLocationRequest: map=345 at (15.6?83.45)"})
    public void testToString(long mapid, String lng, String lat, String expected) throws ValidationException {
        LocalLocationRequest req = new LocalLocationRequest(mapid, lng, lat);
        expected = expected.replace("?", ",");
        //Check that string is formatted correctly
        assertEquals(expected, req.toString());
    }

    //Class to test the latitude setter function
    @DisplayName("Latitude Setter")
    @Nested
    class LatitudeSetter{
        @DisplayName("Valid setLatitude Test")
        @ParameterizedTest(name = "lat = {0}")
        @ValueSource(strings = {"-90.0", "0.0", "12.0", "90.0"})
        public void testSetLatitudeValid(String lat) throws ValidationException {
            LocalLocationRequest req = new LocalLocationRequest(0, "1.1", "1.1");
            req.setLatitude(lat);

            //Check that the field was set properly
            assertEquals(lat, req.getLatitude());
            req.setLatitude(lat).setLatitude(lat);
            assertEquals(lat, req.getLatitude());
        }

        @DisplayName("Invalid setLatitude Test")
        @ParameterizedTest(name = "lat = {0}")
        @ValueSource(strings = {"-91.0000000", "bad", "60","91.2", "-90.4"})
        public void testSetLatitudeInvalid(String lat) throws ValidationException {
            LocalLocationRequest req = new LocalLocationRequest(0, "1.1", "1.1");
            String currLat = req.getLatitude();

            //Check that exception is thrown when invalid data is given
            assertThrows(ValidationException.class, () -> req.setLatitude(lat));
            assertEquals(currLat, req.getLatitude());

        }

        @DisplayName("Null setLatitude Test")
        @Test
        public void testSetLatitudeNull() throws ValidationException {
            LocalLocationRequest req = new LocalLocationRequest(0, "1.1", "1.1");
            String currLat = req.getLatitude();

            //Check that exception is thrown when invalid data is given
            assertThrows(ValidationException.class, () -> req.setLatitude(null));
            assertEquals(currLat, req.getLatitude());
        }
    }

    //Class to test the longitude setter function
    @DisplayName("Longitude Setter")
    @Nested
    class LongitudeSetter{
        @DisplayName("Valid setLongitude Test")
        @ParameterizedTest(name = "lng = {0}")
        @ValueSource(strings = {"-180.0", "0.0", "12.0", "180.0"})
        public void testSetLongitudeValid(String lng) throws ValidationException {
            LocalLocationRequest req = new LocalLocationRequest(0, "1.1", "1.1");
            req.setLongitude(lng);

            //Check that the field was set properly
            assertEquals(lng, req.getLongitude());
            req.setLongitude(lng).setLongitude(lng);
            assertEquals(lng, req.getLongitude());
        }

        @DisplayName("Invalid setLongitude Test")
        @ParameterizedTest(name = "lng = {0}")
        @ValueSource(strings = {"-181.00000000", "test", "90","181.2", "-180.4"})
        public void testSetLongitudeInvalid(String lng) throws ValidationException {
            LocalLocationRequest req = new LocalLocationRequest(0, "1.1", "1.1");
            String currLng = req.getLongitude();

            //Check that exception is thrown when invalid data is given
            assertThrows(ValidationException.class, () -> req.setLongitude(lng));
            assertEquals(currLng, req.getLongitude());
        }

        @DisplayName("Null setLongitude Test")
        @Test
        public void testSetLongitudeNull() throws ValidationException {
            LocalLocationRequest req = new LocalLocationRequest(0, "1.1", "1.1");
            String currLng = req.getLongitude();

            //Check that exception is thrown when invalid data is given
            assertThrows(ValidationException.class, () -> req.setLongitude(null));
            assertEquals(currLng, req.getLongitude());
        }
    }

    @DisplayName("Encode Tests")
    @Nested
    class encodeTests {
        //The test to check that the encode function works properly
        @DisplayName("Valid Encode Test")
        @ParameterizedTest(name = "mapid= {0} lng = {1} lat = {2} encodedStr = {3}")
        @CsvSource({"345, 23.45, 5.12, ADDATUDEv1 345 LOCAL 23.45 5.12", "1, 1.23, -4.56, ADDATUDEv1 1 LOCAL 1.23 -4.56", "10, -7.7, 8.8, ADDATUDEv1 10 LOCAL -7.7 8.8"})
        public void encodeTest(long mapid, String lng, String lat, String encodeStr) throws ValidationException, IOException {
            //Create a MessageOutput instance
            var bOut = new ByteArrayOutputStream();
            var out = new MessageOutput(bOut);

            //Encode the Error
            new LocalLocationRequest(mapid, lng, lat).encode(out);
            String newEncodeStr = encodeStr + " \r\n";

            //Check that the bytes match
            assertArrayEquals(newEncodeStr.getBytes(), bOut.toByteArray());
        }
    }


    @DisplayName("Decode Tests")
    @Nested
    class decodeTests {
        //The test to check that the decode function works properly
        @DisplayName("Valid Decode Test")
        @ParameterizedTest(name = "mapid= {0} lng = {1} lat = {2} encodedStr = {3}")
        @CsvSource({"345, 23.45, 5.12, ADDATUDEv1 345 LOCAL 23.45 5.12", "1, 1.23, -4.56, ADDATUDEv1 1 LOCAL 1.23 -4.56", "10, -7.7, 8.8, ADDATUDEv1 10 LOCAL -7.7 8.8 "})
        public void decodeTest(long mapid, String lng, String lat, String decodeStr) throws ValidationException {
            //Decode an Error by passing a MessageInput
            String newDecodeStr = decodeStr + " \r\n";
            MessageInput mi = new MessageInput(new ByteArrayInputStream(newDecodeStr.getBytes()));
            LocalLocationRequest req = (LocalLocationRequest) Message.decode(mi);
            assertNotNull(req);

            //Check that the fields were set correctly
            assertEquals(mapid, req.getMapId());
            assertEquals("LOCAL", req.getOperation());
            assertEquals(lng, req.getLongitude());
            assertEquals(lat, req.getLatitude());
        }

        @DisplayName("Invalid Decode Test")
        @ParameterizedTest(name = "decodeStr = {0}")
        @ValueSource(strings = {"ADDATUDEv1 10 LOCAL", "ADDATUDEv1 10 LOCAL 1", "ADDATUDEv1 10 LOCAL 1.0",
                "ADDATUDEv1 10 LOCAL 1.0 ", "ADDATUDEv1 10 LOCAL 1.0 3", "ADDATUDEv1 10 LOCAL 1.0 1.1111111111 "})
        public void invalidDecodeTest(String decodeStr) {
            //Decode an Error by passing a MessageInput
            String newDecodeStr = decodeStr + " \r\n";
            MessageInput mi = new MessageInput(new ByteArrayInputStream(newDecodeStr.getBytes()));
            assertThrows(ValidationException.class, () -> LocalLocationRequest.decode(mi));
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
            LocalLocationRequest rec1 = new LocalLocationRequest(1, "10.0", "10.0");
            LocalLocationRequest rec2 = new LocalLocationRequest(1, "10.0", "10.0");
            assertEquals(rec1, rec2);
        }

        //The test to make sure equals does not work when the data does not match
        @DisplayName("Invalid Equals Test")
        @ParameterizedTest(name = "mapid = {0} lng = {1} lat = {2}")
        @CsvSource({"2, 10.0, 10.0", "1, 11.0, 10.0", "1, 10.0, 11.0", "1, 10.5, 10.0", "1, 10.0, 10.03"})
        public void invalidEquals(long id, String lng, String lat) throws ValidationException {
            LocalLocationRequest rec1 = new LocalLocationRequest(1, "10.0", "10.0");
            //Create a LocationRecord with one of the fields different from the base case
            LocalLocationRequest rec2 = new LocalLocationRequest(id,lng,lat);
            assertNotEquals(rec1, rec2);
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
            LocalLocationRequest rec1 = new LocalLocationRequest(1, "10.0", "10.0");
            LocalLocationRequest rec2 = new LocalLocationRequest(1, "10.0", "10.0");
            assertEquals(rec1.hashCode(), rec2.hashCode());
        }

        //Create two instances of LocationRecord with different data and test that the hashes are different
        @DisplayName("Invalid hashCode Test")
        @ParameterizedTest(name = "mapid = {0} lng = {1} lat = {2}")
        @CsvSource({"2, 10.0, 10.0", "1, 11.0, 10.0", "1, 10.0, 11.0", "1, 10.5, 10.0", "1, 10.0, 10.03"})
        public void invalidHashCode(long id, String lng, String lat) throws ValidationException {
            LocalLocationRequest rec1 = new LocalLocationRequest(1, "10.0", "10.0");
            LocalLocationRequest rec2 = new LocalLocationRequest(id,lng,lat);
            assertNotEquals(rec1.hashCode(), rec2.hashCode());
        }
    }
}
