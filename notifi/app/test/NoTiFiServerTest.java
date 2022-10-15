
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Random;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.Timeout;

import addatude.serialization.LocationRecord;
import addatude.serialization.MessageOutput;
import addatude.serialization.NewLocation;
import addatude.serialization.ValidationException;
import notifi.serialization.NoTiFiACK;
import notifi.serialization.NoTiFiError;
import notifi.serialization.NoTiFiLocationAddition;
import notifi.serialization.NoTiFiMessage;
import notifi.serialization.NoTiFiRegister;

/**
 * Testing for NoTiFi server
 *
 * @version 1.0
 *
 * Instructions:
 * . Start NoTiFi server on earth
 * . Set SERVERNAME/PORT below to correct values
 * . Start this test on wind
 * . When prompted, start your NoTiFi client on fire
 *   to register with your NoTiFi server and then
 *   hit return to continue
 *
 * This version of the test may be updated.  Only the
 * latest version of the test should be used on your
 * solution.  Check back on Canvas for updates.  You
 * are responsible for using the latest version for
 * testing.  If you have questions, ask; don't assume.
 */
@TestMethodOrder(OrderAnnotation.class)
public class NoTiFiServerTest {

    private static final InetAddress serverAddr;
    private static final String SERVERNAME = "localhost";
    private static final int SERVERPORT = 5678;
    private static final int SOCKTIMEOUT = 2000;
    private static final int MAXMSGID = 255;
    private static final int PKTMAX = 65507;
    private static final long USERID = 0;
    private static final long MAPID = 345;

    static {
        try {
            serverAddr = InetAddress.getByName(SERVERNAME);
        } catch (UnknownHostException e) {
            throw new RuntimeException("Cannot resolve name");
        }
    }
    private static DatagramSocket notifiSocket;
    private Random rnd = new Random();
    private Socket addatudeSocket;
    private MessageOutput out;

    @BeforeAll
    protected static void notifiSetup() throws IOException {
        notifiSocket = new DatagramSocket();
        notifiSocket.setSoTimeout(SOCKTIMEOUT);
        notifiSocket.connect(serverAddr, SERVERPORT);
    }

    @AfterAll
    protected static void notifiTeardown() {
        notifiSocket.close();
    }

    @BeforeEach
    protected void addatudeSetup() throws IOException {
        addatudeSocket = new Socket(SERVERNAME, SERVERPORT);
        addatudeSocket.setSoTimeout(SOCKTIMEOUT);
        out = new MessageOutput(addatudeSocket.getOutputStream());
    }

    @AfterEach
    protected void addatudeTeardown() throws IOException {
        addatudeSocket.shutdownInput();
        addatudeSocket.close();
    }

    private int nid() {
        return rnd.nextInt(MAXMSGID);
    }

    @DisplayName("Unexpected message type")
    @Test
    @Timeout(1)
    @Order(1)
    void testUnexpectedMessage() throws IOException {
        int nxt = nid();
        sendMessage(notifiSocket, new NoTiFiError(nxt, "surprise"));
        testMatch((NoTiFiError) receivePacket(notifiSocket), nxt, "unexpected message type");
    }

    @DisplayName("Invalid message")
    @Test
    @Timeout(1)
    @Order(2)
    void testValidation() throws IOException {
        byte[] pkt = new NoTiFiError(5, "surprise").encode();
        pkt[0] |= 4;
        sendPacket(notifiSocket, pkt);
        testMatch((NoTiFiError) receivePacket(notifiSocket), 0, "unable to parse");
    }

    @DisplayName("Register Multicast")
    @Test
    @Timeout(1)
    @Order(3)
    void testRegisterMulticast() throws IOException {
        int nxt = nid();
        sendMessage(notifiSocket, new NoTiFiRegister(nxt, makeAddress("224.1.2.3"), notifiSocket.getLocalPort()));
        testMatch((NoTiFiError) receivePacket(notifiSocket), nxt, "bad address");
    }

    @DisplayName("Register Incorrect Port")
    @Test
    @Timeout(1)
    @Order(4)
    void testRegisterIncorrectPort() throws IOException {
        int nxt = nid();
        sendMessage(notifiSocket, new NoTiFiRegister(nxt, (Inet4Address) notifiSocket.getLocalAddress(),
                notifiSocket.getLocalPort() + 1));
        testMatch((NoTiFiError) receivePacket(notifiSocket), nxt, "incorrect port");
    }

    @DisplayName("Register")
    @Test
    @Timeout(1)
    @Order(5)
    public void testRegister() throws IOException {
        int nxt = nid();
        sendMessage(notifiSocket,
                new NoTiFiRegister(nxt, (Inet4Address) notifiSocket.getLocalAddress(), notifiSocket.getLocalPort()));
        testMatch((NoTiFiACK) receivePacket(notifiSocket), nxt);
    }

    @DisplayName("Register Repeat")
    @Test
    @Timeout(1)
    @Order(6)
    void testRegisterRepeat() throws IOException {
        int nxt = nid();
        sendMessage(notifiSocket,
                new NoTiFiRegister(nxt, (Inet4Address) notifiSocket.getLocalAddress(), notifiSocket.getLocalPort()));
        testMatch((NoTiFiError) receivePacket(notifiSocket), nxt, "already registered");
    }

    @DisplayName("New Location")
    @Test
    @Timeout(1)
    @Order(7)
    public void testNewLocation() throws IOException, ValidationException {
        new NewLocation(MAPID, new LocationRecord(USERID, "3.4", "4.5", "ZY", "ABC")).encode(out);
        testMatch((NoTiFiLocationAddition) receivePacket(notifiSocket), USERID, 3.4, 4.5, "Earl: ZY", "ABC");
    }

    // Second register and third register
    private static DatagramSocket notifiSocket2;

    @DisplayName("Register 2")
    @Test
    @Order(8)
    public void testRegister2() throws IOException {
        notifiSocket2 = new DatagramSocket();
        notifiSocket2.connect(serverAddr, SERVERPORT);
        int nxt = nid();
        sendMessage(notifiSocket2,
                new NoTiFiRegister(nxt, (Inet4Address) notifiSocket2.getLocalAddress(), notifiSocket2.getLocalPort()));
        testMatch((NoTiFiACK) receivePacket(notifiSocket2), nxt);
        System.out.println("Start NoTiFiClient and hit return");
        System.in.read();
    }

    // New Location 2
    @DisplayName("New Location 2")
    @Test
    @Timeout(1)
    @Order(9)
    public void testNewLocation2() throws IOException, ValidationException {
        new NewLocation(MAPID, new LocationRecord(USERID, "-4.5", "8.6", "Y Z", "C A")).encode(out);
        testMatch((NoTiFiLocationAddition) receivePacket(notifiSocket), USERID, -4.5, 8.6, "Earl: Y Z", "C A");
        testMatch((NoTiFiLocationAddition) receivePacket(notifiSocket2), USERID, -4.5, 8.6, "Earl: Y Z", "C A");
        System.out.println(
                "Verify NoTiFiClient receives userid=%d, (%s,%s) at %s-%s".formatted(USERID, -4.5, 8.6, "Y Z", "C A"));
    }

    /*
     * Test name too long
     */
    @DisplayName("Name too long")
    @Test
    @Timeout(1)
    @Order(10)
    public void testNameTooLong() throws IOException, ValidationException {
        // Long name is 251 because it will be prefixed with "Earl: "
        new NewLocation(MAPID, new LocationRecord(USERID, "-6.0", "-7.0", "A".repeat(251), "XYZ")).encode(out);
        testMatch((NoTiFiLocationAddition) receivePacket(notifiSocket), USERID, -6.0, -7.0, "Earl: " + "A".repeat(249),
                "XYZ");
        testMatch((NoTiFiLocationAddition) receivePacket(notifiSocket2), USERID, -6.0, -7.0, "Earl: " + "A".repeat(249),
                "XYZ");
    }

    /*
     * Test description with bad characters
     */
    @DisplayName("Description bad chars")
    @Test
    @Timeout(1)
    @Order(11)
    public void testDescriptionBadChars() throws IOException, ValidationException {
        // Long name is 251 because it will be prefixed with "Earl: "
        new NewLocation(MAPID, new LocationRecord(USERID, "-6.0", "-7.0", "ABC", "1?3")).encode(out);
        testMatch((NoTiFiLocationAddition) receivePacket(notifiSocket), USERID, -6.0, -7.0, "Earl: ABC", "1?3");
        testMatch((NoTiFiLocationAddition) receivePacket(notifiSocket2), USERID, -6.0, -7.0, "Earl: ABC", "1?3");
    }


    /*
     * Test new addatude client and location addition
     */
    @DisplayName("New AddATude client")
    @Test
    @Timeout(1)
    @Order(12)
    public void testNewAddATudeClient() throws IOException, ValidationException {
        addatudeSocket.close();
        addatudeSocket = new Socket(SERVERNAME, SERVERPORT);
        addatudeSocket.setSoTimeout(SOCKTIMEOUT);
        out = new MessageOutput(addatudeSocket.getOutputStream());
        new NewLocation(MAPID, new LocationRecord(USERID, "1.2", "3.4", "K", "Z")).encode(out);
        testMatch((NoTiFiLocationAddition) receivePacket(notifiSocket), USERID, 1.2, 3.4, "Earl: K", "Z");
        testMatch((NoTiFiLocationAddition) receivePacket(notifiSocket2), USERID, 1.2, 3.4, "Earl: K", "Z");
    }

    /*
     * Test functions
     */
    private static void testMatch(NoTiFiACK r, int msgid) {
        assertAll("(1)", () -> assertEquals(msgid, r.getMsgId(), "msgid"));
    }

    private static void testMatch(NoTiFiError r, int msgid, String msg) {
        assertAll("(2)", () -> assertEquals(msgid, r.getMsgId(), "msgid"),
                () -> assertTrue(msg.toLowerCase().contains(msg.toLowerCase())));
    }

    private static void testMatch(NoTiFiLocationAddition r, long userid, double longitude, double latitude,
                                  String locName, String locDesc) {
        assertAll("(5)", () -> assertEquals(userid, r.getUserId(), "userid"),
                () -> assertEquals(longitude, r.getLongitude(), "long"),
                () -> assertEquals(latitude, r.getLatitude(), "lat"),
                () -> assertEquals(locName, r.getLocationName(), "name"),
                () -> assertEquals(locDesc, r.getLocationDescription(), "desc"));
    }

    private static Inet4Address makeAddress(String address) throws UnknownHostException {
        return (Inet4Address) InetAddress.getByName(address);
    }

    /*
     * Utility functions
     */
    protected static void sendMessage(DatagramSocket s, NoTiFiMessage pkt) throws IOException {
        sendPacket(s, pkt.encode());
    }

    protected static void sendPacket(DatagramSocket s, byte[] sndBuffer) throws IOException {
        DatagramPacket sndDatagram = new DatagramPacket(sndBuffer, sndBuffer.length);
        s.send(sndDatagram);
    }

    protected static NoTiFiMessage receivePacket(DatagramSocket s) throws IOException {
        DatagramPacket rcvDatagram = new DatagramPacket(new byte[PKTMAX], PKTMAX);
        s.receive(rcvDatagram); // Receive packet from client

        // Copy valid subset of buffer bytes and decode
        byte[] rcvBuffer = Arrays.copyOf(rcvDatagram.getData(), rcvDatagram.getLength());

        return NoTiFiMessage.decode(rcvBuffer);
    }
}
