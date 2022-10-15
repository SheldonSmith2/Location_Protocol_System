/*
 * Sheldon Smith
 * Course: CSI 4321
 */
import notifi.serialization.NoTiFiMessage;
import notifi.serialization.NoTiFiRegister;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class NoTiFiRegisterTest {
    @DisplayName("Constructor Tests")
    @Nested
    class constructorTests{
        @DisplayName("Valid Constructor")
        @ParameterizedTest(name = "msgId = {0} port = {1}}")
        @CsvSource({"1, 1", "1, 0", "50, 65535"})
        public void validConstructor(int msgId, int port) {
            Inet4Address addr = (Inet4Address) Inet4Address.getLoopbackAddress();
            NoTiFiRegister reg = new NoTiFiRegister(msgId, addr, port);
            assertEquals(msgId, reg.getMsgId());
            assertEquals(addr, reg.getAddress());
            assertEquals(port, reg.getPort());
            assertEquals(0, reg.getCode());

            assertEquals(new InetSocketAddress(addr, port), reg.getSocketAddress());
        }

        @DisplayName("Invalid Constructor")
        @ParameterizedTest(name = "msgId = {0} port = {1}}")
        @CsvSource({"1, -1", "1, -100", "50, 65536"})
        public void invalidConstructor(int msgId, int port) {
            Inet4Address addr = (Inet4Address) Inet4Address.getLoopbackAddress();
            assertThrows(IllegalArgumentException.class, () -> new NoTiFiRegister(msgId, addr, port));
        }

        @DisplayName("Invalid Null Constructor")
        @ParameterizedTest(name = "msgId = {0} port = {1}}")
        @CsvSource({"1, 1", "1, 100", "50, 65535"})
        public void invalidConstructorNull(int msgId, int port) {
            assertThrows(IllegalArgumentException.class, () -> new NoTiFiRegister(msgId, null, port));
        }
    }

    @DisplayName("toString Tests")
    @ParameterizedTest(name = "msgId = {0} port = {1}}")
    @CsvSource({"1, 1, Register: msgid=1 address=127.0.0.1 port=1", "10, 0, Register: msgid=10 address=127.0.0.1 port=0",
            "1, 65535, Register: msgid=1 address=127.0.0.1 port=65535"})
    public void toStringTest(int msgId, int port, String msg){
        NoTiFiRegister reg = new NoTiFiRegister(msgId, (Inet4Address) Inet4Address.getLoopbackAddress(), port);
        assertEquals(msg, reg.toString());
    }

    static class ValidArrays implements ArgumentsProvider {
        @Override
        public Stream<Arguments> provideArguments(ExtensionContext extensionContext) {
            List<Arguments> list = new ArrayList<>();
            list.add(Arguments.of(new byte[]{0x30, 10, 1, 0, 0, 127, (byte) 0x88, 0x13}, 10, "127.0.0.1", 5000));
            list.add(Arguments.of(new byte[]{0x30, 45, 2, 1, (byte) 168, (byte) 192, 0x04, 0x04}, 45, "192.168.1.2", 1028));
            list.add(Arguments.of(new byte[]{0x30, 124, 4, 3, 2, 1, (byte) 0xd2, 0x04}, 124, "1.2.3.4", 1234));
            return list.stream();
        }
    }

    @DisplayName("Encode Tests")
    @Nested
    class encodeTests{
        @DisplayName("Valid Encode")
        @ParameterizedTest(name = "msgID = {1}, address = {2}, port = {3}")
        @ArgumentsSource(ValidArrays.class)
        public void validEncode(byte[] arr, int msgID, String address, int port) throws UnknownHostException {
            NoTiFiRegister reg = new NoTiFiRegister(msgID, (Inet4Address) Inet4Address.getByName(address), port);

            assertArrayEquals(arr, reg.encode());
        }
    }

    @DisplayName("Decode Tests")
    @Nested
    class decodeTests{
        @DisplayName("Valid Decode")
        @ParameterizedTest(name = "msgID = {1}, address = {2}, port = {3}")
        @ArgumentsSource(ValidArrays.class)
        public void validDecode(byte[] arr, int msgID, String address, int port){
            NoTiFiRegister req = (NoTiFiRegister) NoTiFiMessage.decode(arr);

            assertEquals(0, req.getCode());
            assertEquals(msgID, req.getMsgId());
            assertEquals(address, req.getAddress().getHostAddress());
            assertEquals(port, req.getPort());
        }

    }
}
