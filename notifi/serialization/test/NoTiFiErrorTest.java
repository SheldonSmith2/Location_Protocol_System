/*
 * Sheldon Smith
 * Course: CSI 4321
 */
import notifi.serialization.NoTiFiError;
import notifi.serialization.NoTiFiMessage;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class NoTiFiErrorTest {
    @DisplayName("Constructor Tests")
    @Nested
    class constructorTests{
        @DisplayName("Valid Constructor")
        @ParameterizedTest(name = "msgId = {0} errorMsg = {1}")
        @CsvSource({"0, Bad Error", "1, Test", "255, this is an error"})
        public void validConstructor(int msgId, String errorMessage){
            NoTiFiError er = new NoTiFiError(msgId, errorMessage);
            assertEquals(msgId, er.getMsgId());
            assertEquals(errorMessage, er.getErrorMessage());
            assertEquals(2, er.getCode());
        }

        @DisplayName("Invalid Constructor")
        @ParameterizedTest(name = "msgId = {0} errorMsg = {1}")
        @CsvSource({"-1, Bad Error", "1, Sri\u0001ng", "255, this is an e\u0001rror", "256, test"})
        public void invalidConstructor(int msgId, String errorMessage){
            assertThrows(IllegalArgumentException.class, () -> new NoTiFiError(msgId, errorMessage));
        }
    }

    @DisplayName("toString Tests")
    @ParameterizedTest(name = "msgId = {0} msg = {1} string = {2}")
    @CsvSource({"0, bad, Error: msgid=0 bad", "4, error, Error: msgid=4 error", "255, this is an error, Error: msgid=255 this is an error"})
    public void toStringTest(int msgId, String msg, String str) {
        NoTiFiError er = new NoTiFiError(msgId, msg);
        assertEquals(str, er.toString());
    }

    @DisplayName("setErrorMessage Tests")
    @Nested
    class setErrorMessageTests{
        @DisplayName("Valid setErrorMessage")
        @ParameterizedTest(name = "errorMsg = {0}")
        @ValueSource(strings = {"this is an error, bad error, ERROR"})
        public void validConstructor(String errorMessage){
            NoTiFiError er = new NoTiFiError(1, "errorMessage");
            er.setErrorMessage(errorMessage);
            assertEquals(errorMessage, er.getErrorMessage());
        }

        @DisplayName("Invalid setErrorMessage")
        @ParameterizedTest(name = "errorMsg = {0}")
        @ValueSource(strings = {"Sri\u0001ng", "this is an e\u0001rror"})
        public void invalidConstructor(String errorMessage){
            NoTiFiError er = new NoTiFiError(1, "errorMessage");
            assertThrows(IllegalArgumentException.class, () -> er.setErrorMessage(errorMessage));
        }
    }

    static class ValidArrays implements ArgumentsProvider {
        @Override
        public Stream<Arguments> provideArguments(ExtensionContext extensionContext) {
            List<Arguments> list = new ArrayList<>();
            list.add(Arguments.of(new byte[]{0x32, 10, 0x45, 0x72, 0x72, 0x6f, 0x72, 0x20, 0x4d, 0x65, 0x73, 0x73, 0x61, 0x67, 0x65}, 10, "Error Message"));
            list.add(Arguments.of(new byte[]{0x32, 45, 84, 104, 105, 115, 32, 105, 115, 32, 97, 32, 116, 101, 115, 116}, 45, "This is a test"));
            list.add(Arguments.of(new byte[]{0x32, 124, 0x53, 0x6f, 0x6d, 0x65, 0x74, 0x68, 0x69, 0x6e, 0x67, 0x20, 0x62,
                    0x61, 0x64, 0x20, 0x68, 0x61, 0x73, 0x20, 0x6f, 0x63, 0x63, 0x75, 0x72, 0x72, 0x65, 0x64}, 124, "Something bad has occurred"));
            list.add(Arguments.of(new byte[]{0x32, 124}, 124, ""));
            list.add(Arguments.of(new byte[]{50, 0, 49, 50, 51}, 0, "123"));
            return list.stream();
        }
    }

    @DisplayName("Encode Tests")
    @Nested
    class encodeTests{
        @DisplayName("Valid Encode")
        @ParameterizedTest(name = "msgID = {1}, errorMsg = {2}")
        @ArgumentsSource(ValidArrays.class)
        public void validEncode(byte[] arr, int msgID, String erMsg){
            NoTiFiError er = new NoTiFiError(msgID, erMsg);
            assertArrayEquals(arr, er.encode());
        }
    }

    @DisplayName("Decode Tests")
    @Nested
    class decodeTests{
        @DisplayName("Valid Decode")
        @ParameterizedTest(name = "msgID = {1}, errorMsg = {2}")
        @ArgumentsSource(ValidArrays.class)
        public void validDecode(byte[] arr, int msgID, String erMsg){
            NoTiFiError er = (NoTiFiError) NoTiFiMessage.decode(arr);
            assertEquals(2, er.getCode());
            assertEquals(msgID, er.getMsgId());
            assertEquals(erMsg, er.getErrorMessage());
        }
    }

}
