/*
 * Sheldon Smith
 * Course: CSI 4321
 */

import notifi.serialization.NoTiFiACK;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static notifi.serialization.NoTiFiMessage.decode;
import static org.junit.jupiter.api.Assertions.*;

public class NoTiFiACKTest {
    @DisplayName("Constructor Tests")
    @Nested
    class constructorTests{
        @DisplayName("Valid Constructor")
        @ParameterizedTest(name = "msgId = {0}")
        @ValueSource(ints = {0, 1, 40, 255})
        public void validConstructor(int msgId){
            NoTiFiACK ack = new NoTiFiACK(msgId);
            assertEquals(msgId, ack.getMsgId());
            assertEquals(3, ack.getCode());
        }

        @DisplayName("Invalid Constructor")
        @ParameterizedTest(name = "msgId = {0}")
        @ValueSource(ints = {-1, 256, 1000})
        public void invalidConstructor(int msgId){
            assertThrows(IllegalArgumentException.class, () -> new NoTiFiACK(msgId));
        }
    }

    @DisplayName("toString Tests")
    @ParameterizedTest(name = "msgId = {0} string = {1}")
    @CsvSource({"0, ACK: msgid=0", "4, ACK: msgid=4", "255, ACK: msgid=255"})
    public void toStringTest(int msgId, String str){
        NoTiFiACK ack = new NoTiFiACK(msgId);
        assertEquals(str, ack.toString());
    }

    static class ValidArrays implements ArgumentsProvider {
        @Override
        public Stream<Arguments> provideArguments(ExtensionContext extensionContext) {
            List<Arguments> list = new ArrayList<>();
            list.add(Arguments.of(new byte[]{0x33, 10}, 10));
            list.add(Arguments.of(new byte[]{0x33, 45}, 45));
            list.add(Arguments.of(new byte[]{0x33, 124}, 124));
            return list.stream();
        }
    }

    @DisplayName("Encode Tests")
    @Nested
    class encodeTests{
        @DisplayName("Valid Encode")
        @ParameterizedTest(name = "array = {0}, msgID = {1}")
        @ArgumentsSource(ValidArrays.class)
        public void validEncode(byte[] arr, int msgID){
            NoTiFiACK ack = new NoTiFiACK(msgID);
            assertArrayEquals(arr, ack.encode());
        }
    }

    @DisplayName("Decode Tests")
    @Nested
    class decodeTests{
        @DisplayName("Valid Decode")
        @ParameterizedTest(name = "array = {0}, msgID = {1}")
        @ArgumentsSource(ValidArrays.class)
        public void validDecode(byte[] arr, int msgID){

            NoTiFiACK ack = (NoTiFiACK) decode(arr);
            assertEquals(3, ack.getCode());
            assertEquals(msgID, ack.getMsgId());
        }

        static class InvalidArrays implements ArgumentsProvider {
            @Override
            public Stream<Arguments> provideArguments(ExtensionContext extensionContext) {
                List<Arguments> list = new ArrayList<>();
                list.add(Arguments.of(new byte[]{0x33, 10, 5}));
                list.add(Arguments.of(new byte[]{0x33}));

                return list.stream();
            }
        }

        @DisplayName("Incorrect Size Decode")
        @ParameterizedTest(name = "arr = {0}")
        @ArgumentsSource(InvalidArrays.class)
        public void invalidDecode(byte[] arr){
            assertThrows(IllegalArgumentException.class, () -> decode(arr));
        }
    }
}
