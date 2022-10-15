/*
 * Sheldon Smith
 * Course: CSI 4321
 */
import notifi.serialization.NoTiFiLocationAddition;
import notifi.serialization.NoTiFiMessage;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class NoTiFiLocationAdditionTest {
    @DisplayName("Constructor Tests")
    @Nested
    class constructorTests{
        @DisplayName("Valid Constructor")
        @ParameterizedTest(name = "msgId = {0} userId = {1} lng = {2} lat = {3} name = {4} desc = {5}")
        @CsvSource({"0, 3, 6.6, 6.6, Name, Desc", "1, 10, 180.0, 90.0, Test Name, Test Desc", "255, 1, -180.0, -90.0, Test Name, Test Desc"})
        public void validConstructor(int msgId, int userId, double lng, double lat, String name, String desc){
            NoTiFiLocationAddition locAddition = new NoTiFiLocationAddition(msgId, userId, lng, lat, name, desc);
            assertEquals(msgId, locAddition.getMsgId());
            assertEquals(userId, locAddition.getUserId());
            assertEquals(lng, locAddition.getLongitude());
            assertEquals(lat, locAddition.getLatitude());
            assertEquals(name, locAddition.getLocationName());
            assertEquals(desc, locAddition.getLocationDescription());
            assertEquals(1, locAddition.getCode());
        }

        @DisplayName("Invalid Constructor")
        @ParameterizedTest(name = "msgId = {0} userId = {1} lng = {2} lat = {3} name = {4} desc = {5}")
        @CsvSource({"-1, 3, 6.6, 6.6, Name, Desc", "1, 10, 180.0, 90.0, Test N\u0001ame, Test Desc", "255, 1, -181.0, -90.0, Test Name, Test Desc"})
        public void invalidConstructor(int msgId, int userId, double lng, double lat, String name, String desc){
            assertThrows(IllegalArgumentException.class, () -> new NoTiFiLocationAddition(msgId, userId, lng, lat, name, desc));
        }
    }

    @DisplayName("toString Test")
    @Test
    public void toStringTest(){
        NoTiFiLocationAddition locAddition = new NoTiFiLocationAddition(253, 1, 4.4, 5.5, "Moody", "Study");
        assertEquals("Addition: msgid=253 1:Moody-Study (4,6)", locAddition.toString());
    }

    static class ValidArrays implements ArgumentsProvider {
        @Override
        public Stream<Arguments> provideArguments(ExtensionContext extensionContext) {
            List<Arguments> list = new ArrayList<>();
            list.add(Arguments.of(new byte[]{0x31, 10, 0, 0, 0, 40, -102, -103, -103, -103, -103, -103, -15, 63, 61, 10, -41, -93,
                    112, 61, 18, 64, 4, 78, 97, 109, 101, 4, 68, 101, 115, 99}, 10, 40, 1.1, 4.56, "Name", "Desc"));
            list.add(Arguments.of(new byte[]{0x31, 45, 0, 0, 0, 1, (byte) 0xAE, 0x47, (byte) 0xE1, 0x7A, 0x14, (byte) 0xAE,
                            0x28, 0x40, (byte) 0xBA, 0x49, 0x0C, 0x02, 0x2B, 0x47, 0x56, (byte) 0xC0, 5, 0x4D, 0x6F, 0x6F,
                            0x64, 0x79, 8, 0x53, 0x74, 0x75, 0x64, 0x79, 0x69, 0x6E, 0x67},
                    45, 1, 12.34, -89.112, "Moody", "Studying"));
            list.add(Arguments.of(new byte[]{0x31, 124, 0, 0, 0, 25, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0x80, 0x66,
                    0x40, (byte) 0x9A, (byte) 0x99, (byte) 0x99, (byte) 0x99, (byte) 0x99, (byte) 0x99, 0x45, 0x40, 9,
                    0x41, 0x70, 0x61, 0x72, 0x74, 0x6D, 0x65, 0x6E, 0x74, 4, 0x54, 0x65, 0x73, 0x74},
                    124, 25, 180.0, 43.2, "Apartment", "Test"));
            list.add(Arguments.of(new byte[]{0x31, 10, 0, 0, 0, 40, -102, -103, -103, -103, -103, -103, -15, 63, 61, 10, -41, -93,
                    112, 61, 18, 64, 0, 0}, 10, 40, 1.1, 4.56, "", ""));
            return list.stream();
        }
    }

    @DisplayName("Encode Tests")
    @Nested
    class encodeTests{
        @DisplayName("Valid Encode")
        @ParameterizedTest(name = "msgID = {1}, userID = {2}, name = {5}, desc = {6}")
        @ArgumentsSource(ValidArrays.class)
        public void validEncode(byte[] arr, int msgID, int userID, double lng, double lat, String name, String desc){
            NoTiFiLocationAddition locAdd = new NoTiFiLocationAddition(msgID, userID, lng, lat,name, desc);
            assertArrayEquals(arr, locAdd.encode());
        }
    }

    @DisplayName("Decode Tests")
    @Nested
    class decodeTests{
        @DisplayName("Valid Decode")
        @ParameterizedTest(name = "msgID = {1}, userID = {2}, name = {5}, desc = {6}")
        @ArgumentsSource(ValidArrays.class)
        public void validDecode(byte[] arr, int msgID, int userID, double lng, double lat, String name, String desc){
            NoTiFiLocationAddition locAdd = (NoTiFiLocationAddition) NoTiFiMessage.decode(arr);

            assertEquals(1, locAdd.getCode());
            assertEquals(msgID, locAdd.getMsgId());
            assertEquals(userID, locAdd.getUserId());
            assertEquals(lng, locAdd.getLongitude());
            assertEquals(lat, locAdd.getLatitude());
            assertEquals(name, locAdd.getLocationName());
            assertEquals(desc, locAdd.getLocationDescription());
        }
    }
}
