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

public class LocationRequestTest {
    //The character set for getting the bytes
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    //The class to hold the constructor tests
    @DisplayName("Constructor Tests")
    @Nested
    class ConstructorTests{
        //Tests valid parameters in order to create a new LocationRequest
        @DisplayName("Valid Constructor")
        @ParameterizedTest(name = "id = {0}")
        @ValueSource(longs = {0, 1, 501, 99999})
        public void validConstructorTest(long id) throws ValidationException {
            //Create a new instance of LocationRequest
            LocationRequest req = new LocationRequest(id);

            //Check that the fields were set properly
            assertEquals(id, req.getMapId());
            assertEquals("ALL", req.getOperation());
        }

        //The test to check for ValidationExceptions with there are bad parameters
        @DisplayName("Invalid Constructor")
        @ParameterizedTest(name = "id = {0}")
        @ValueSource(longs = {-1L, -100L, 100000L})
        public void invalidConstructorTest(long id) {
            //Check that exception is thrown when a negative id is input
            assertThrows(ValidationException.class, () -> new LocationRequest(id));
        }
    }

    //The test to check that the toString method is working properly
    @DisplayName("Valid toString")
    @ParameterizedTest(name = "id = {0} fullStr = {1}")
    @CsvSource({"501, LocationRequest: map=501", "101, LocationRequest: map=101"})
    public void validToStringTest(long id, String fullStr) throws ValidationException {
        //Create LocationRequest with id
        LocationRequest req = new LocationRequest(id);

        //Check that the returned string matches expected string
        assertEquals(fullStr, req.toString());
    }

    @DisplayName("Encode Tests")
    @Nested
    class encodeTests {
        //The test to check that the encode function works properly
        @DisplayName("Valid Encode Test")
        @ParameterizedTest(name = "id = {0} encodeStr = {1}")
        @CsvSource({"501, ADDATUDEv1 501 ALL", "101, ADDATUDEv1 101 ALL"})
        public void encodeTest(long id, String encodeStr) throws ValidationException, IOException {
            //Test that the encode function work correctly
            var bOut = new ByteArrayOutputStream();
            var out = new MessageOutput(bOut);

            //Encode the LocationRequest
            new LocationRequest(id).encode(out);
            String newEncodeStr = encodeStr + " \r\n";

            //Check that the bytes match
            assertArrayEquals(newEncodeStr.getBytes(CHARSET), bOut.toByteArray());
        }

        @DisplayName("Null Encode")
        @Test
        public void nullEncode(){
            assertThrows(NullPointerException.class, () -> new LocationRequest(1).encode(null));
        }
    }

    @DisplayName("Decode Tests")
    @Nested
    class decodeTests {
        //The test to check that the decode function works properly
        @DisplayName("Valid Decode Test")
        @ParameterizedTest(name = "id = {0} decodeStr = {1}")
        @CsvSource({"501, ADDATUDEv1 501 ALL", "101, ADDATUDEv1 101 ALL"})
        public void decodeTest(long id, String decodeStr) throws ValidationException {
            //Decode a LocationRequest by passing a MessageInput
            String newDecodeStr = decodeStr + " \r\n";
            MessageInput mi = new MessageInput(new ByteArrayInputStream(newDecodeStr.getBytes(CHARSET)));
            LocationRequest req = (LocationRequest) Message.decode(mi);
            assertNotNull(req);

            //Check that the fields were set correctly
            assertEquals(id, req.getMapId());
            assertEquals("ALL", req.getOperation());
        }

        @DisplayName("Null Decode")
        @Test
        public void nullDecode(){
            assertThrows(NullPointerException.class, () -> new LocationRequest(1).decode(null));
        }

        @DisplayName("Invalid Decode Test")
        @ParameterizedTest(name = "decodeStr = {0}")
        @ValueSource(strings = {"ADDATUEv1 501 ALL", "ADDATUDEv1 100000 ALL", "ADDATUDEv1 -1 ALL", "ADDATUDEv1 ALL"})
        public void invalidDecodeTest(String decodeStr) {
            //Decode an Error by passing a MessageInput
            String newDecodeStr = decodeStr + "\r\n";
            MessageInput mi = new MessageInput(new ByteArrayInputStream(newDecodeStr.getBytes()));
            Exception ex = assertThrows(ValidationException.class, () -> LocationRequest.decode(mi));
            assertNotNull(ex.getMessage());
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
            //Create a LocationRequest instance
            LocationRequest req = new LocationRequest(501);
            req.setMapId(id);

            //Check that the id is set properly
            assertEquals(id, req.getMapId());
        }

        //The test is to check for negative id exceptions
        @DisplayName("Invalid setMapID")
        @ParameterizedTest(name = "id = {0}")
        @ValueSource(longs = {-1, -501, 100000})
        public void invalidSetMapIDTest(long id) throws ValidationException {
            LocationRequest req = new LocationRequest(501);

            //Check that exception is thrown when mapID is negative
            assertThrows(ValidationException.class, () -> req.setMapId(id));
        }
    }
}
