/*
 * Sheldon Smith
 * Course: CSI 4321
 *
 * Brainstorm Buddy: Malik Mohamedali
 */

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;
import addatude.serialization.*;
import org.junit.jupiter.api.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class LocationRecordTest {
    //A function to create a simple record for later use
    public LocationRecord setup() throws ValidationException {
        return new LocationRecord(1234, "4.321", "4.321", "Moody", "Study");
    }

    //Nested tests to test the constructors
    @DisplayName("Contructor Tests")
    @Nested
    class Constructor {
        @ParameterizedTest(name = "id = {0}, lat = {1}, lng = {2}, name = {3}, desc = {4}")
        @DisplayName("Valid First Constructor")
        @CsvSource({"123, 12.34, 1.67, Hankamer, Class", "1, -12.0, -90.0, Hankamer, Class", "0, -12.321, -90.0, Hankamer, Class", "99999, -15.0, -84.0, Hankamer, Class"})
        public void testFirstConstructor(Long id, String lat, String lng, String name, String desc) throws ValidationException {
            //Create a LocationRecord with the given data
            LocationRecord newRecord = new LocationRecord(id, lng, lat, name, desc);
            assertNotNull(newRecord);

            //Check that each field is set properly
            assertEquals(id, newRecord.getUserId());
            assertEquals(lat, newRecord.getLatitude());
            assertEquals(lng, newRecord.getLongitude());
            assertEquals(name, newRecord.getLocationName());
            assertEquals(desc, newRecord.getLocationDescription());
        }

        @DisplayName("Test Empty Constructor")
        @Test
        public void testEmptyConstructor(){
            LocationRecord rec = new LocationRecord();
            assertNull(rec.getLongitude());
            assertNull(rec.getLatitude());
            assertNull(rec.getLocationName());
            assertNull(rec.getLocationDescription());
        }

        @DisplayName("Valid Second Constructor")
        @ParameterizedTest(name = "id = {0}  lng = {1}  lat = {2}  name = {3}  desc = {4}")
        @CsvSource({"1, 1.2, 3.4, BU, Baylor, 1 1.2 3.4 2 BU6 Baylor", "2, 34.5, 87.3, Moody, Study, 2 34.5 87.3 5 Moody5 Study",
                "99999, 34.5, 87.3, Moody, Study, 99999 34.5 87.3 5 Moody5 Study", "123, 12.34, 1.67, , Class, 123 12.34 1.67 0 5 Class",
                "5, 5.0, -10.0, h?re, ther?, 5 5.0 -10.0 4 h?re5 ther?"})
        public void testSecondConstructor(long id, String lng, String lat, String name, String desc, String fullStr) throws ValidationException {
            //Create a LocationRecord by passing a MessageInput
            MessageInput mi = new MessageInput(new ByteArrayInputStream(fullStr.getBytes()));
            LocationRecord record2 = new LocationRecord(mi);
            assertNotNull(record2);

            if (name == null){
                name = "";
            }

            //Check that the fields were set correctly
            assertEquals(id, record2.getUserId());
            assertEquals(lng, record2.getLongitude());
            assertEquals(lat, record2.getLatitude());
            assertEquals(name, record2.getLocationName());
            assertEquals(desc, record2.getLocationDescription());
        }

        @ParameterizedTest(name = "id = {0}, lat = {1}, lng = {2}, name = {3}, desc = {4}")
        @DisplayName("First Constructor Exceptions")
        @CsvSource({"123, 12.34, 1.67, , Class", "-1, -12.0, -90.0, Hankamer, Class", "1, 200.0, -90.0, Hankamer, Class",
                "1, 100.0, -91.0, Hankamer, Class", "1, 100.0, -90.0, Han©kamer, Class", "1, 100.0, -90.0, Hankamer, Cl©ass", "100000, 12.34, 1.67, Test, Class"})
        public void testConstructorExceptions(Long id, String lat, String lng, String name, String desc){
            //Check that an exception is thrown when a bad field is given
            ValidationException ex = assertThrows(ValidationException.class, () -> new LocationRecord(id, lng, lat, name, desc));
            assertNotNull(ex.getMessage());
        }

        @DisplayName("Second Constructor Null")
        @Test
        public void testConstructorExceptions2() {
            //Check that an exception is through when input is null
            assertThrows(NullPointerException.class, () -> new LocationRecord(null));
        }

        @DisplayName("Second Constructor No Bytes")
        @Test
        public void testConstructorExceptions3() {
            //Check that an exception is through when input is empty
            MessageInput mi = new MessageInput(new ByteArrayInputStream("".getBytes()));
            ValidationException ex = assertThrows(ValidationException.class, () -> new LocationRecord(mi));
            assertEquals("IOException", ex.getInvalidToken());
            assertEquals("Reading failed for LocationRecord", ex.getMessage());
        }

        @ParameterizedTest(name = "str = {0}")
        @DisplayName("Second Constructor Exceptions")
        @ValueSource(strings = {"-1 -12.0 -90.0 8 Hankamer5 Class", "1 200.0 -90.0 8 Hankamer5 Class" ,
                "1 100.0 -91.0 8 Hankamer5 Class", "1 100.0 -90.0 9 Han\u0001kamer5 Class", "1 100.0 -90.0 8 Hankamer6 C\u0001lass",
                "-1 -12.0 -90.0 -1 Hankamer5 Class", "-1 -12.0 -90.0 8 Hankamer-5 Class", "-1 -12.0 -90.0 -1 Hankamer5 Class",
                "5 5.0 -10.0 4 here5 ther", "5", "5 5.0", "5 5.0 5.0", "5 5.0 5.0 4", "5 5.0 5.0 4 te", "5 5.0 5.0 4 test",
                "5 5.0 5.0 4 test4", "5 5.0 5.0 4 test4 te", "5 5.0 5.0 4 t\nst4 test"})
        public void testConstructorExceptions4(String str){
            //Check that an exception is thrown when a bad field is given
            MessageInput mi = new MessageInput(new ByteArrayInputStream(str.getBytes()));
            assertThrows(ValidationException.class, () -> new LocationRecord(mi));
        }

    }

    //Test to test the encode function
    @DisplayName("Valid Encode Test")
    @ParameterizedTest(name = "id = {0}  lng = {1}  lat = {2}  name = {3}  desc = {4}")
    @CsvSource({"1, 1.2, 3.4, BU, Baylor, 1 1.2 3.4 2 BU6 Baylor", "2, 34.5, 87.3, Moody, Study, 2 34.5 87.3 5 Moody5 Study"})
    public void testEncode(long id, String lng, String lat, String name, String desc, String encodeStr) throws ValidationException, IOException {
        //Test that the encode function work correctly
        var bOut = new ByteArrayOutputStream();
        var out = new MessageOutput(bOut);
        new LocationRecord(id, lng, lat, name, desc).encode(out);
        assertArrayEquals(encodeStr.getBytes(), bOut.toByteArray());
    }

    @Test
    @DisplayName("Null Encode Test")
    public void testEncodeNull() throws ValidationException {
        LocationRecord record = setup();
        //Check that assertion is thrown when null is given
        assertThrows(NullPointerException.class, () -> record.encode(null));
    }

    //Function to test the toString function
    @DisplayName("toString Test")
    @ParameterizedTest(name = "id = {0}  lng = {1}  lat = {2}  name = {3}  desc = {4}")
    @CsvSource({"1, 1.2, 3.4, BU, Baylor, 1:BU-Baylor (1.2?3.4)", "2, 34.5, 87.3, Moody, Study, 2:Moody-Study (34.5?87.3)"})
    public void testToString(long id, String lng, String lat, String name, String desc, String expected) throws ValidationException {
        LocationRecord record = new LocationRecord(id, lng, lat, name, desc);
        expected = expected.replace("?", ",");
        //Check that string is formatted correctly
        assertEquals(expected, record.toString());
    }

    //Class to test the userID setter function
    @DisplayName("UserID Setter")
    @Nested
    class UserIDSetter{
        @DisplayName("Valid setID Test")
        @ParameterizedTest(name = "id = {0}")
        @ValueSource(longs = {1L, 60L, 12354L, 99999L, 0L})
        public void testSetUserIDValid(long id) throws ValidationException {
            LocationRecord record = setup();
            record.setUserId(id);

            //Check that the field was set properly
            assertEquals(id, record.getUserId());
            record.setUserId(id).setUserId(id);
            assertEquals(id, record.getUserId());
        }

        @DisplayName("Invalid setID Test")
        @ParameterizedTest(name = "id = {0}")
        @ValueSource(longs = {-1L, -123L, 100000})
        public void testSetUserIDInvalid(long id) throws ValidationException {
            LocationRecord record = setup();
            Long currID = record.getUserId();

            //Check that exception is thrown when invalid data is given
            assertThrows(ValidationException.class, () -> record.setUserId(id));
            assertEquals(currID, record.getUserId());
        }
    }

    //Class to test the latitude setter function
    @DisplayName("Latitude Setter")
    @Nested
    class LatitudeSetter{
        @DisplayName("Valid setLatitude Test")
        @ParameterizedTest(name = "lat = {0}")
        @ValueSource(strings = {"-90.0", "0.0", "12.0", "90.0"})
        public void testSetLatitudeValid(String lat) throws ValidationException {
            LocationRecord record = setup();
            record.setLatitude(lat);

            //Check that the field was set properly
            assertEquals(lat, record.getLatitude());
            record.setLatitude(lat).setLatitude(lat);
            assertEquals(lat, record.getLatitude());
        }

        @DisplayName("Invalid setLatitude Test")
        @ParameterizedTest(name = "lat = {0}")
        @ValueSource(strings = {"-91.0000000", "bad", "60","91.2", "-90.4"})
        public void testSetLatitudeInvalid(String lat) throws ValidationException {
            LocationRecord record = setup();
            String currLat = record.getLatitude();

            //Check that exception is thrown when invalid data is given
            assertThrows(ValidationException.class, () -> record.setLatitude(lat));
            assertEquals(currLat, record.getLatitude());

        }

        @DisplayName("Null setLatitude Test")
        @Test
        public void testSetLatitudeNull() throws ValidationException {
            LocationRecord record = setup();
            String currLat = record.getLatitude();

            //Check that exception is thrown when invalid data is given
            assertThrows(ValidationException.class, () -> record.setLatitude(null));
            assertEquals(currLat, record.getLatitude());
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
            LocationRecord record = setup();
            record.setLongitude(lng);

            //Check that the field was set properly
            assertEquals(lng, record.getLongitude());
            record.setLongitude(lng).setLongitude(lng);
            assertEquals(lng, record.getLongitude());
        }

        @DisplayName("Invalid setLongitude Test")
        @ParameterizedTest(name = "lng = {0}")
        @ValueSource(strings = {"-181.00000000", "test", "90","181.2", "-180.4"})
        public void testSetLongitudeInvalid(String lng) throws ValidationException {
            LocationRecord record = setup();
            String currLng = record.getLongitude();

            //Check that exception is thrown when invalid data is given
            assertThrows(ValidationException.class, () -> record.setLongitude(lng));
            assertEquals(currLng, record.getLongitude());
        }

        @DisplayName("Null setLongitude Test")
        @Test
        public void testSetLongitudeNull() throws ValidationException {
            LocationRecord record = setup();
            String currLng = record.getLongitude();

            //Check that exception is thrown when invalid data is given
            assertThrows(ValidationException.class, () -> record.setLongitude(null));
            assertEquals(currLng, record.getLongitude());
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

    //Class to test the name setter function
    @DisplayName("Name Setter")
    @Nested
    class NameSetter{
        @DisplayName("Valid setRecordName Test")
        @ParameterizedTest(name = "name = {0}")
        @ArgumentsSource(ValidStrings.class)
        public void testSetRecordNameValid(String name) throws ValidationException {
            LocationRecord record = setup();
            record.setLocationName(name);

            //Check that the field was set properly
            assertNotNull(record.getLocationName());
            assertEquals(name, record.getLocationName());
            record.setLocationName(name).setLocationName(name);
            assertNotNull(record.getLocationName());
            assertEquals(name, record.getLocationName());
        }

        @DisplayName("Invalid setRecordName Test")
        @ParameterizedTest(name = "name = {0}")
        @ArgumentsSource(InvalidStrings.class)
        public void testSetRecordNameInvalid(String name) throws ValidationException {
            LocationRecord record = setup();
            String currName = record.getLocationName();

            //Check that exception is thrown when invalid data is given
            assertThrows(ValidationException.class, () -> record.setLocationName(name));
            assertEquals(currName, record.getLocationName());
        }

        @DisplayName("Null setRecordName Test")
        @Test
        public void testSetRecordNameNull() throws ValidationException {
            LocationRecord record = setup();
            String currName = record.getLocationName();

            //Check that exception is thrown when invalid data is given
            assertThrows(ValidationException.class, () -> record.setLocationName(null));
            assertEquals(currName, record.getLocationName());
        }
    }

    //Class to test the description setter function
    @DisplayName("Description Setter")
    @Nested
    class DescriptionSetter{
        @DisplayName("Valid setRecordDescription Test")
        @ParameterizedTest(name = "desc = {0}")
        @ArgumentsSource(ValidStrings.class)
        public void testSetRecordDescriptionValid(String desc) throws ValidationException {
            LocationRecord record = setup();
            record.setLocationDescription(desc);

            //Check that the field was set properly
            assertNotNull(record.getLocationDescription());
            assertEquals(desc, record.getLocationDescription());
            record.setLocationDescription(desc).setLocationDescription(desc);
            assertNotNull(record.getLocationDescription());
            assertEquals(desc, record.getLocationDescription());
        }

        @DisplayName("Invalid setRecordDescription Test")
        @ParameterizedTest(name = "desc = {0}")
        @ArgumentsSource(InvalidStrings.class)
        public void testSetRecordDescriptionInvalid(String desc) throws ValidationException {
            LocationRecord record = setup();
            String currDesc = record.getLocationDescription();

            //Check that exception is thrown when invalid data is given
            assertThrows(ValidationException.class, () -> record.setLocationDescription(desc));
            assertEquals(currDesc, record.getLocationDescription());
        }

        @DisplayName("Null setRecordDescription Test")
        @Test
        public void testSetRecordDescriptionNull() throws ValidationException {
            LocationRecord record = setup();
            String currDesc = record.getLocationDescription();

            //Check that exception is thrown when invalid data is given
            assertThrows(ValidationException.class, () -> record.setLocationDescription(null));
            assertEquals(currDesc, record.getLocationDescription());
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
            assertEquals(rec1, rec2);
        }

        //The test to make sure equals does not work when the data does not match
        @DisplayName("Invalid Equals Test")
        @ParameterizedTest(name = "id = {0} lng = {1} lat = {2} name = {3} desc = {4}")
        @CsvSource({"2, 10.0, 10.0, Moody, Study", "1, 11.0, 10.0, Moody, Study", "1, 10.0, 11.0, Moody, Study", "1, 10.0, 10.0, Cashion, Study", "1, 10.0, 10.0, Moody, Work"})
        public void invalidEquals(long id, String lng, String lat, String name, String desc) throws ValidationException {
            LocationRecord rec1 = new LocationRecord(1, "10.0", "10.0", "Moody", "Study");
            //Create a LocationRecord with one of the fields different from the base case
            LocationRecord rec2 = new LocationRecord(id,lng,lat,name,desc);
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
            LocationRecord rec1 = new LocationRecord(1, "10.0", "10.0", "Moody", "Study");
            LocationRecord rec2 = new LocationRecord(1, "10.0", "10.0", "Moody", "Study");
            assertEquals(rec1.hashCode(), rec2.hashCode());
        }

        //Create two instances of LocationRecord with different data and test that the hashes are different
        @DisplayName("Invalid hashCode Test")
        @ParameterizedTest(name = "id = {0} lng = {1} lat = {2} name = {3} desc = {4}")
        @CsvSource({"2, 10.0, 10.0, Moody, Study", "1, 11.0, 10.0, Moody, Study", "1, 10.0, 11.0, Moody, Study", "1, 10.0, 10.0, Cashion, Study", "1, 10.0, 10.0, Moody, Work"})
        public void invalidHashCode(long id, String lng, String lat, String name, String desc) throws ValidationException {
            LocationRecord rec1 = new LocationRecord(1, "10.0", "10.0", "Moody", "Study");
            LocationRecord rec2 = new LocationRecord(id,lng,lat,name,desc);
            assertNotEquals(rec1.hashCode(), rec2.hashCode());
        }
    }

    //Test that the correct response takes place for the compareTo function
    @DisplayName("compareTo Test")
    @ParameterizedTest(name = "id = {0} lng = {1} lat = {2} name = {3} desc = {4} result = {5}")
    @CsvSource({"2, 10.0, 10.0, Mood, Study, 1", "2, 10.0, 10.0, Moody, Study, 0", "3, 10.0, 10.0, Moody2, Fun, -1"})
    public void invalidHashCode(long id, String lng, String lat, String name, String desc, int result) throws ValidationException {
        LocationRecord rec1 = new LocationRecord(2, "10.0", "10.0", "Moody", "Study");
        //Create LocationRecord with varying names
        LocationRecord rec2 = new LocationRecord(id,lng,lat,name,desc);
        assertEquals(result, rec1.compareTo(rec2));
    }

    @DisplayName("readLocationRecord Number Exception")
    @ParameterizedTest(name = "str = {0}")
    @ValueSource(strings = {"A 1.2 3.4 2 BU6 Baylor", "2 34.5 87.3 h Moody5 Study", "2 34.5 87.3 5 Moodyy Study"})
    public void readLocationRecord(String str) throws ValidationException {
        MessageInput in = new MessageInput(new ByteArrayInputStream(str.getBytes()));
        LocationRecord rec = new LocationRecord(1, "1.1", "1.1", "", "");
        assertThrows(ValidationException.class, () -> rec.readLocationRecord(in));
    }
}
