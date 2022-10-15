/*
 * Sheldon Smith
 * Course: CSI 4321
 */
import addatude.serialization.*;
import addatude.serialization.Error;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class ErrorTest {
    //The class to hold the constructor tests
    @DisplayName("Constructor Tests")
    @Nested
    class ConstructorTests{
        //Tests valid parameters in order to create a new Error
        @DisplayName("Valid Constructor")
        @ParameterizedTest(name = "id = {0} message = {1}")
        @CsvSource({"123, bad input", "2, validation error", "0, checking", "99999, message"})
        public void validConstructorTest(long id, String msg) throws ValidationException {
            //Create a new instance of Error
            Error er = new Error(id, msg);
            assertNotNull(er);

            //Check that the error has correct data in fields
            assertEquals(msg, er.getErrorMessage());
            assertEquals("ERROR", er.getOperation());
            assertEquals(id, er.getMapId());
        }

        //The test to check for ValidationExceptions with there are bad parameters (not null)
        @DisplayName("Invalid Constructor")
        @ParameterizedTest(name = "id = {0} message = {1}")
        @CsvSource({"-1, bad input", "2, ", "100000, bad input"})
        public void invalidConstructorTest(long id, String msg){
            //Check that exception is thrown when id is negative or message is empty
            assertThrows(ValidationException.class, () -> new Error(id, msg));
        }

        //The test to check for ValidationException if null is passed for message
        @DisplayName("Null Constructor")
        @Test
        public void nullConstructorTest(){
            //Check that exception is thrown when null is passed in as a message
            assertThrows(ValidationException.class, () -> new Error(12, null));
        }
    }

    //The test to check that the toString method is working properly
    @DisplayName("Valid toString")
    @ParameterizedTest(name = "id = {0} msg = {1} fullStr = {2}")
    @CsvSource({"501, bad input, Error: map=501 error=bad input", "101, something went wrong, Error: map=101 error=something went wrong"})
    public void validToStringTest(long id, String msg, String fullStr) throws ValidationException {
        //Create error with id and message arguments
        Error er = new Error(id, msg);

        //Check that the returned string matches expected string
        assertEquals(fullStr, er.toString());
    }

    @DisplayName("Encode Tests")
    @Nested
    class encodeTests {
        //The test to check that the encode function works properly
        @DisplayName("Valid Encode Test")
        @ParameterizedTest(name = "id= {0} msg = {1} encodedStr = {2}")
        @CsvSource({"5, bad error, ADDATUDEv1 5 ERROR 9 bad error", "501, something went wrong, ADDATUDEv1 501 ERROR 20 something went wrong"})
        public void encodeTest(long id, String msg, String encodeStr) throws ValidationException, IOException {
            //Create a MessageOutput instance
            var bOut = new ByteArrayOutputStream();
            var out = new MessageOutput(bOut);

            //Encode the Error
            new Error(id, msg).encode(out);
            String newEncodeStr = encodeStr + "\r\n";

            //Check that the bytes match
            assertArrayEquals(newEncodeStr.getBytes(), bOut.toByteArray());
        }

        @DisplayName("Null Encode")
        @Test
        public void nullEncode(){
            assertThrows(NullPointerException.class, () -> new Error(1,"test").encode(null));
        }
    }


    @DisplayName("Decode Tests")
    @Nested
    class decodeTests {
        //The test to check that the decode function works properly
        @DisplayName("Valid Decode Test")
        @ParameterizedTest(name = "id = {0} msg = {1} decodeStr = {2}")
        @CsvSource({"5, error, ADDATUDEv1 5 ERROR 5 error", "501, something went wrong, ADDATUDEv1 501 ERROR 20 something went wrong"})
        public void decodeTest(long id, String msg, String decodeStr) throws ValidationException {
            //Decode an Error by passing a MessageInput
            String newDecodeStr = decodeStr + "\r\n";
            MessageInput mi = new MessageInput(new ByteArrayInputStream(newDecodeStr.getBytes()));
            Error er = (Error) Message.decode(mi);
            assertNotNull(er);

            //Check that the fields were set correctly
            assertEquals(id, er.getMapId());
            assertEquals("ERROR", er.getOperation());
            assertEquals(msg, er.getErrorMessage());
        }

        @DisplayName("Null Decode")
        @Test
        public void nullDecode(){
            assertThrows(NullPointerException.class, () -> new Error(1, "test").decode(null));
        }

        @DisplayName("Invalid Decode Test")
        @ParameterizedTest(name = "decodeStr = {0}")
        @ValueSource(strings = {"ADDATUDEv 5 ERROR 5 error", "ADDATUDEv1 100000 ERROR 20 something went wrong",
                "ADDATUDEv1 -1 ERROR 20 something went wrong", "ADDATUDEv1 5 ERROR -1 error", "ADDATUDEv1 5",
                "ADDATUDEv1 5 ERROR", "ADDATUDEv1 5 ERROR 5 ", "ADDATUDEv1 5 ERROR 5 err", "ADDA"})
        public void invalidDecodeTest(String decodeStr) {
            //Decode an Error by passing a MessageInput
            String newDecodeStr = decodeStr + "\r\n";
            MessageInput mi = new MessageInput(new ByteArrayInputStream(newDecodeStr.getBytes()));
            assertThrows(ValidationException.class, () -> Error.decode(mi));
        }

        @DisplayName("Premature EoS Decode Test")
        @ParameterizedTest(name = "decodeStr = {0}")
        @ValueSource(strings = {"ADDATUDEv 5 ERROR\r\n 5 error", "ADDATUDEv1 100000 ERROR 20 somet\rhing went wrong",
                "ADDAT\rUDEv1 -1 ERROR 20 something went wrong", "ADDATU\r\nDEv1 5 ERROR -1 error", "ADDATUDEv1 5\r\n"})
        public void eosInvalidDecodeTest(String decodeStr) {
            //Decode an Error by passing a MessageInput
            String newDecodeStr = decodeStr + "\r\n";
            MessageInput mi = new MessageInput(new ByteArrayInputStream(newDecodeStr.getBytes()));
            assertThrows(ValidationException.class, () -> Error.decode(mi));
        }
    }

    //The class to test the mapID setter
    @DisplayName("MapID Setter Tests")
    @Nested
    class mapIDSetterTests{
        //The test for valid parameters for setting the mapID
        @DisplayName("Valid setMapID")
        @ParameterizedTest(name = "id = {0}")
        @ValueSource(longs = {0, 1, 501, 101, 99999})
        public void validSetMapIDTest(long id) throws ValidationException {
            //Create an Error instance
            Error er = new Error(501, "test");
            er.setMapId(id);

            //Check that the id is set properly
            assertEquals(id, er.getMapId());
        }

        //The test is to check for negative id exceptions
        @DisplayName("Invalid setMapID")
        @ParameterizedTest(name = "id = {0}")
        @ValueSource(longs = {-1, -501, 100000})
        public void invalidSetMapIDTest(long id) throws ValidationException {
            Error er = new Error(501, "test");

            //Check that exception is thrown when mapID is negative
            assertThrows(ValidationException.class, () -> er.setMapId(id));
        }
    }

    static class InvalidStrings implements ArgumentsProvider {
        @Override
        public Stream<Arguments> provideArguments(ExtensionContext extensionContext) {
            List<Arguments> list = new ArrayList<>();
            list.add(Arguments.of("nam\u0001"));
            list.add(Arguments.of("\u0001this is bad"));
            list.add(Arguments.of("Sri\u0001ng"));
            list.add(Arguments.of("Sri\nng"));
            list.add(Arguments.of("Sring\n"));
            list.add(Arguments.of((Object) null));
            list.add(Arguments.of(Stream.generate(() -> "a").limit(100000).collect(Collectors.joining())));
            return list.stream();
        }
    }

    static class ValidStrings implements ArgumentsProvider {
        @Override
        public Stream<Arguments> provideArguments(ExtensionContext extensionContext) {
            List<Arguments> list = new ArrayList<>();
            list.add(Arguments.of(""));
            list.add(Arguments.of("test"));
            list.add(Arguments.of("this is a longer name"));
            list.add(Arguments.of(Stream.generate(() -> "a").limit(99999).collect(Collectors.joining())));
            return list.stream();
        }
    }

    //The class to test the Error message setter
    @DisplayName("setErrorMessage Tests")
    @Nested
    class SetErrorMessageTests{
        //The test for valid parameters for setting the message
        @DisplayName("Valid setErrorMessage")
        @ParameterizedTest
        @ArgumentsSource(ValidStrings.class)
        public void validSetErrorMessageTest(String msg) throws ValidationException {
            //Create a base Error instance
            Error er = new Error(501, "test");

            //Set the message and check that it matches expected string
            er.setErrorMessage(msg);
            assertEquals(msg, er.getErrorMessage());
            er.setErrorMessage(msg).setErrorMessage(msg);
            assertEquals(msg, er.getErrorMessage());
        }

        //The test to check that a ValidationException is throw when null is passed to setter
        @DisplayName("Null setErrorMessage")
        @Test
        public void nullSetErrorMessageTest() throws ValidationException {
            Error er = new Error(501, "test");
            assertThrows(ValidationException.class, () -> er.setErrorMessage(null));
        }

        //The test is to check for bad character and empty string exceptions
        @DisplayName("Bad Character setErrorMessage")
        @ParameterizedTest(name = "message = {0}")
        @ArgumentsSource(InvalidStrings.class)
        public void invalidSetErrorMessageTest(String msg) throws ValidationException {
            //Check that an exception is thrown when empty string or bad character is set for message
            Error er = new Error(501, "test");
            assertThrows(ValidationException.class, () -> er.setErrorMessage(msg));
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
            Error er = new Error(1, "error message");
            Error er2 = new Error(1, "error message");
            assertEquals(er, er2);
        }

        //The test to make sure equals does not work when the data does not match
        @DisplayName("Invalid Equals Test")
        @ParameterizedTest(name = "id = {0} msg = {1}")
        @CsvSource({"2, bad message", "1, test"})
        public void invalidEquals(long id, String msg) throws ValidationException {
            Error er = new Error(1, "correct message");
            //Create a LocationRecord with one of the fields different from the base case
            Error er2 = new Error(id,msg);
            assertNotEquals(er, er2);
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
            Error er = new Error(1, "error message");
            Error er2 = new Error(1, "error message");
            assertEquals(er.hashCode(), er2.hashCode());
        }

        //Create two instances of LocationRecord with different data and test that the hashes are different
        @DisplayName("Invalid hashCode Test")
        @ParameterizedTest(name = "id = {0} msg = {1}")
        @CsvSource({"2, bad message", "1, test"})
        public void invalidHashCode(long id, String msg) throws ValidationException {
            Error er = new Error(1, "correct message");
            Error er2 = new Error(id,msg);
            assertNotEquals(er.hashCode(), er2.hashCode());
        }
    }
}
