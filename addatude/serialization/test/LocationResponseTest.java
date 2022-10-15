/*
 * Sheldon Smith
 * Course: CSI 4321
 */
import addatude.serialization.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.*;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class LocationResponseTest {
    //The character set for getting the bytes
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    static class LocationRecordsToString implements ArgumentsProvider {
        //Function to return different amounts of LocationRecord lists to test toString function
        @Override
        public Stream<Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
            List<Arguments> list = new ArrayList<>();
            //The first list with nothing in it along with expected string
            list.add(Arguments.of(new ArrayList<>(), "LocationResponse: map=1 test []"));
            List<LocationRecord> oneElementList = new ArrayList<>();
            oneElementList.add(new LocationRecord(1, "10.0", "10.0", "Cashion", "Study"));
            //The second list with one LocationRecord in it along with expected string
            list.add(Arguments.of(oneElementList, "LocationResponse: map=1 test [1:Cashion-Study (10.0,10.0)]"));
            List<LocationRecord> twoElementList = new ArrayList<>();
            twoElementList.add(new LocationRecord(1, "10.0", "10.0", "Cashion", "Study"));
            twoElementList.add(new LocationRecord(2, "37.4", "81.1", "Moody", "Fun"));
            //The third list with two LocationRecords in it along with expected string
            list.add(Arguments.of(twoElementList, "LocationResponse: map=1 test [1:Cashion-Study (10.0,10.0),2:Moody-Fun (37.4,81.1)]"));
            return list.stream();
        }
    }

    static class LocationRecordsEncodeDecode implements ArgumentsProvider {
        //Function to return different amounts of LocationRecord lists to test encode/decode functions
        @Override
        public Stream<Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
            List<Arguments> list = new ArrayList<>();
            //The first list with nothing in it along with expected string
            list.add(Arguments.of(new ArrayList<>(), 501, "test", "ADDATUDEv1 501 RESPONSE 4 test0 "));
            List<LocationRecord> oneElementList = new ArrayList<>();
            oneElementList.add(new LocationRecord(1, "10.0", "10.0", "Cashion", "Study"));
            list.add(Arguments.of(oneElementList, 101, "example name", "ADDATUDEv1 101 RESPONSE 12 example name1 1 10.0 10.0 7 Cashion5 Study"));
            //The second list with one LocationRecord in it along with expected string
            List<LocationRecord> twoElementList = new ArrayList<>();
            twoElementList.add(new LocationRecord(1, "10.0", "10.0", "Cashion", "Study"));
            twoElementList.add(new LocationRecord(2, "37.4", "81.1", "Moody", "Fun"));
            //The third list with two LocationRecords in it along with expected string
            list.add(Arguments.of(twoElementList, 51, "test", "ADDATUDEv1 51 RESPONSE 4 test2 1 10.0 10.0 7 Cashion5 Study2 37.4 81.1 5 Moody3 Fun"));
            return list.stream();
        }
    }

    static class BadLocationRecords implements ArgumentsProvider {
        //Function to return list of LocationRecords with null to test that exceptions are thrown
        @Override
        public Stream<Arguments> provideArguments(ExtensionContext extensionContext) throws Exception {
            List<Arguments> list = new ArrayList<>();
            List<LocationRecord> firstList = new ArrayList<>();
            firstList.add(null);
            //First list with just a null entry
            list.add(Arguments.of(firstList, 0));
            List<LocationRecord> secondList = new ArrayList<>();
            secondList.add(new LocationRecord(1, "10.0", "10.0", "Cashion", "Study"));
            secondList.add(null);
            //Second list with null in second position
            list.add(Arguments.of(secondList, 1));
            return list.stream();
        }
    }

    //The class to hold the constructor tests
    @DisplayName("Constructor Tests")
    @Nested
    class ConstructorTests{
        //Tests valid parameters in order to create a new LocationResponse
        @DisplayName("Valid Constructor")
        @ParameterizedTest(name = "id = {0} name = {1}")
        @CsvSource({"501, name", "2, test", "0, test", "99999, test"})
        public void validConstructorTest(long id, String name) throws ValidationException {
            //Create a new instance of LocationResponse
            LocationResponse res = new LocationResponse(id, name);

            //Check that the fields were set properly
            assertEquals(res.getMapName(), name);
            assertEquals(res.getMapId(), id);
            assertEquals(res.getOperation(), "RESPONSE");
            assertEquals(res.getLocationRecordList().size(), 0);
            assertTrue(res.getLocationRecordList().isEmpty());
        }

        //The test to check for ValidationExceptions with there are bad parameters (not null)
        @DisplayName("Invalid Constructor")
        @ParameterizedTest(name = "id = {0} name = {1}")
        @CsvSource({"-501, name", "2, ", "100000, test"})
        public void invalidConstructorTest(long id, String name) {
            //Check that exception is thrown when id is negative or message is empty
            assertThrows(ValidationException.class, () -> new LocationResponse(id, name));
        }

        //The test to check for ValidationException if null is passed for mapName
        @DisplayName("Null Constructor")
        @Test
        public void nullConstructorTest() {
            //Check that exception is thrown when null is passed in as a mapName
            assertThrows(ValidationException.class, () -> new LocationResponse(1, null));
        }
    }

    //The test to check that the toString method is working properly
    @DisplayName("toString Tests")
    @ParameterizedTest(name = "fullStr = {1}")
    @ArgumentsSource(LocationRecordsToString.class)
    public void validToStringTestNone(List<LocationRecord> recordList, String str) throws ValidationException {
        //Create an instance of LocationResponse
        LocationResponse res = new LocationResponse(1, "test");

        //Loop through list gotten from function and add LocationRecords to LocationResponse
        recordList.forEach(r -> {
            try {
                res.addLocationRecord(r);
            } catch (ValidationException e) {
                e.printStackTrace();
            }
        });
        //Check that the returned string matches expected string
        assertEquals(str, res.toString());
    }

    @DisplayName("Encode Tests")
    @Nested
    class encodeTests {
        //The test to check that the encode function works properly
        @DisplayName("Valid Encode Tests")
        @ParameterizedTest(name = "name = {2} encodeStr = {3}")
        @ArgumentsSource(LocationRecordsEncodeDecode.class)
        public void validEncodeTestNone(List<LocationRecord> recordList, long id, String name, String encodeStr) throws ValidationException, IOException {
            //Test that the encode function work correctly
            var bOut = new ByteArrayOutputStream();
            var out = new MessageOutput(bOut);

            //Loop through list gotten from function and add LocationRecords to LocationResponse
            LocationResponse res = new LocationResponse(id, name);
            recordList.forEach(r -> {
                try {
                    res.addLocationRecord(r);
                } catch (ValidationException e) {
                    e.printStackTrace();
                }
            });

            //Encode the LocationResponse
            res.encode(out);
            String newEncodeStr = encodeStr + "\r\n";

            //Check that the bytes match
            assertArrayEquals(newEncodeStr.getBytes(CHARSET), bOut.toByteArray());
        }

        @DisplayName("Null Encode")
        @Test
        public void nullEncode(){
            assertThrows(NullPointerException.class, () -> new LocationResponse(1,"test").encode(null));
        }
    }

    @DisplayName("Decode Tests")
    @Nested
    class decodeTests {
        //The test to check that the decode function works properly
        @DisplayName("Valid Decode Tests")
        @ParameterizedTest(name = "name = {2} encodeStr = {3}")
        @ArgumentsSource(LocationRecordsEncodeDecode.class)
        public void validDecodeTestNone(List<LocationRecord> recordList, long id, String name, String decodeStr) throws ValidationException {
            //Decode a LocationResponse by passing a MessageInput
            String newDecodeStr = decodeStr + "\r\n";
            MessageInput mi = new MessageInput(new ByteArrayInputStream(newDecodeStr.getBytes(CHARSET)));
            LocationResponse res = (LocationResponse) Message.decode(mi);
            assertNotNull(res);

            //Check that the fields were set correctly
            assertEquals(id, res.getMapId());
            assertEquals("RESPONSE", res.getOperation());
            assertEquals(name, res.getMapName());

            //Check that the locationRecordList is correct
            List<LocationRecord> newList = res.getLocationRecordList();
            assertEquals(recordList.size(), newList.size());
            Collections.sort(recordList);
            Collections.sort(newList);
            assertIterableEquals(recordList, newList);
        }

        @DisplayName("Null Decode")
        @Test
        public void nullDecode(){
            assertThrows(NullPointerException.class, () -> new LocationResponse(1,"test").decode(null));
        }

        @DisplayName("Invalid Decode")
        @ParameterizedTest(name = "string = {0}")
        @ValueSource(strings = {"ADDATUDEv1 101 RESPONSE 12 example name100000 1 10.0 10.0 7 Cashion5 Study",
                "ADDATUDEv1 101 RESPONSE 12 example name-1 1 10.0 10.0 7 Cashion5 Study",
                "ADDATUDEv1 101 RESPONSE 12 example name1 1 10.0 10.0 7 Cashion-5 Study",
                "ADDATUDEv1 101 RESPONSE -12 example name1 1 10.0 10.0 7 Cashion5 Study",
                "ADDATDEv1 101 RESPONSE 12 example name1 1 10.0 10.0 7 Cashion5 Study",
                "ADDATUDEv1 101 RESPONSE 12", "ADDATUDEv1 101 RESPONSE 12 err", "ADDATUDEv1 101 RESPONSE 12 example name",
                "ADDATUDEv1 101 RESPONSE 12 example name1", "ADDATUDEv1 101 RESPONSE 12 example name1 1 10.0"})
        public void invalidDecode(String str){
            String newDecodeStr = str + "\r\n";
            MessageInput mi = new MessageInput(new ByteArrayInputStream(newDecodeStr.getBytes()));
            assertThrows(ValidationException.class, () -> new LocationResponse(1,"test").decode(mi));
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
            //Create a LocationResponse instance
            LocationResponse res = new LocationResponse(501, "test");
            res.setMapId(id);

            //Check that the id is set properly
            assertEquals(id, res.getMapId());
        }

        //The test is to check for negative id exceptions
        @DisplayName("Invalid setMapID")
        @ParameterizedTest(name = "id = {0}")
        @ValueSource(longs = {-1, -501, 100000})
        public void invalidSetMapIDTest(long id) throws ValidationException {
            LocationResponse res = new LocationResponse(501, "test");

            //Check that exception is thrown when mapID is negative
            assertThrows(ValidationException.class, () -> res.setMapId(id));
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

    //The class to test the mapName setter
    @DisplayName("setMapName Tests")
    @Nested
    class SetMapNameTests{
        //The test for valid parameters for setting the mapName
        @DisplayName("Valid setMapName")
        @ParameterizedTest(name = "name = {0}")
        @ArgumentsSource(ValidStrings.class)
        public void validSetMapNameTest(String name) throws ValidationException {
            //Create an instance of LocationResponse
            LocationResponse res = new LocationResponse(1, "testing");
            res.setMapName(name);

            //Set the message and check that it matches expected string
            assertEquals(res.getMapName(), name);

            //Set the message and check that it matches expected string
            res.setMapName(" ").setMapName(name);
            assertEquals(res.getMapName(), name);
        }

        //The test to check that a ValidationException is throw when null is passed to setter
        @DisplayName("Null setMapName")
        @Test
        public void nullSetMapNameTest() throws ValidationException {
            //Create LocationResponse instance
            LocationResponse res = new LocationResponse(1, "testing");
            String origName = res.getMapName();

            //Check that exception is thrown when null is passed
            assertThrows(ValidationException.class, () -> res.setMapName(null));
            assertEquals(origName, res.getMapName());
        }

        //The test is check for bad character and empty string exceptions
        @DisplayName("Invalid setMapName")
        @ParameterizedTest(name = "name = {0}")
        @ArgumentsSource(InvalidStrings.class)
        public void invalidSetMapNameTest(String name) throws ValidationException {
            //Create LocationResponse instance
            LocationResponse res = new LocationResponse(1, "testing");
            String origName = res.getMapName();

            //Check that exception is thrown when empty string or invalid character is passed
            assertThrows(ValidationException.class, () -> res.setMapName(name));
            assertEquals(origName, res.getMapName());
        }
    }

    //The class to test the addLocationRecord tests
    @DisplayName("addLocationRecord Tests")
    @Nested
    class AddLocationRecordTests{
        @DisplayName("Valid addLocationRecord")
        @ParameterizedTest
        @ArgumentsSource(LocationRecordsToString.class)
        public void validAddLocationRecordTest(List<LocationRecord> recordList) throws ValidationException {
            LocationResponse res = new LocationResponse(1, "test");

            //Loop through list gotten from function and add LocationRecords to LocationResponse
            recordList.forEach(r -> {
                try {
                    res.addLocationRecord(r);
                } catch (ValidationException e) {
                    e.printStackTrace();
                }
            });

            //Check that the two lists are the same
            List<LocationRecord> newRecordList = res.getLocationRecordList();
            Collections.sort(recordList);
            Collections.sort(newRecordList);
            assertIterableEquals(recordList, newRecordList);
        }

        @DisplayName("Invalid addLocationRecord")
        @ParameterizedTest
        @ArgumentsSource(BadLocationRecords.class)
        public void invalidAddLocationRecordTest(List<LocationRecord> recordList, int badindex) throws ValidationException {
            LocationResponse res = new LocationResponse(1, "test");

            //Loop through list gotten from function and add LocationRecords to LocationResponse
            //Expecting an exception to be thrown due to null value in list
            for (int i = 0; i < recordList.size(); i++){
                int finalI = i;
                if (i == badindex){
                    assertThrows(ValidationException.class, () -> res.addLocationRecord(recordList.get(finalI)));
                } else {
                    res.addLocationRecord(recordList.get(finalI));
                }
            }
        }
    }

    @DisplayName("Delete Location Test")
    @Test
    public void deleteLocation() throws ValidationException {
        LocationResponse res = new LocationResponse(25, "Map");
        res.addLocationRecord(new LocationRecord(1, "1.1", "1.1", "", ""));
        res.deleteLocation(1);
        assertEquals(0, res.getLocationRecordList().size());
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
            LocationResponse res = new LocationResponse(1, "message");
            LocationResponse res2 = new LocationResponse(1, "message");
            assertEquals(res, res2);
        }

        //The test to make sure equals does not work when the data does not match
        @DisplayName("Invalid Equals Test")
        @ParameterizedTest(name = "id = {0} msg = {1}")
        @CsvSource({"2, bad message", "1, test"})
        public void invalidEquals(long id, String msg) throws ValidationException {
            LocationResponse res = new LocationResponse(id, msg);
            //Create a LocationRecord with one of the fields different from the base case
            LocationResponse res2 = new LocationResponse(1, "message");
            assertNotEquals(res, res2);
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
            LocationResponse res = new LocationResponse(1, "message");
            LocationResponse res2 = new LocationResponse(1, "message");
            assertEquals(res.hashCode(), res2.hashCode());
        }

        //Create two instances of LocationRecord with different data and test that the hashes are different
        @DisplayName("Invalid hashCode Test")
        @ParameterizedTest(name = "id = {0} msg = {1}")
        @CsvSource({"2, bad message", "1, test"})
        public void invalidHashCode(long id, String msg) throws ValidationException {
            LocationResponse res = new LocationResponse(id, msg);
            LocationResponse res2 = new LocationResponse(1, "message");
            assertNotEquals(res.hashCode(), res2.hashCode());
        }
    }
}
