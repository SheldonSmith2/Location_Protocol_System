//package addatude.app.server.test;

import static addatude.serialization.Message.decode;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import addatude.serialization.Error;
import addatude.serialization.LocationRecord;
import addatude.serialization.LocationRequest;
import addatude.serialization.LocationResponse;
import addatude.serialization.MessageInput;
import addatude.serialization.MessageOutput;
import addatude.serialization.NewLocation;
import addatude.serialization.ValidationException;

/**
 * P3 Server test
 *
 * @version 1.2
 */
@TestMethodOrder(OrderAnnotation.class)
public class ServerTest {
    private static final String SERVER = "127.0.0.1";
    private static final int PORT = 4444;
    private static final Charset ENC = StandardCharsets.UTF_8;
    private static final int DELAY = 10;
    private static final long MAPID = 345;
    private static final String MAPNAME = "Class Map";

    private Socket clientSocket;
    private MessageInput in;
    private MessageOutput out;

    @BeforeEach
    protected void before() throws IOException {
        clientSocket = new Socket(SERVER, PORT);
        in = new MessageInput(clientSocket.getInputStream());
        out = new MessageOutput(getSlowOutput(clientSocket.getOutputStream(), DELAY));
    }

    @AfterEach
    protected void after() throws IOException {
        clientSocket.close();
    }

    @DisplayName("Empty (3)")
    @Test
    @Order(1)
    protected void testEmpty() throws IOException, ValidationException {
        // Request all
        new LocationRequest(MAPID).encode(out);
        var r = (LocationResponse) decode(in);
        testMatch(r, MAPID, MAPNAME, List.of());
    }

    @DisplayName("New Location (3)")
    @Test
    @Order(2)
    protected void testNewLocation() throws IOException, ValidationException {
        // New location 1
        LocationRecord lr = new LocationRecord(56, "-97.12", "31.55", "FortWorth", "Down town");
        new NewLocation(MAPID, lr).encode(out);
        LocationResponse r = (LocationResponse) decode(in);
        testMatch(r, MAPID, MAPNAME,
                List.of(new LocationRecord(56, "-97.12", "31.55", "Bob Smith: FortWorth", "Down town")));
    }

    @DisplayName("Replace Location (3)")
    @Test
    @Order(3)
    protected void testReplaceLocation() throws IOException, ValidationException {
        // Replace location 1
        var lr = new LocationRecord(56, "-97.3", "32.72", "Fort Worth", "Downtown");
        new NewLocation(MAPID, lr).encode(out);
        var r = (LocationResponse) decode(in);
        testMatch(r, MAPID, MAPNAME,
                List.of(new LocationRecord(56, "-97.3", "32.72", "Bob Smith: Fort Worth", "Downtown")));
    }

    @DisplayName("Second Location (3)")
    @Test
    @Order(4)
    protected void testSecondLocation() throws IOException, ValidationException {
        // New location 2
        var lr2 = new LocationRecord(0, "-80.0", "30.0", "Waco", "Baylor!");
        new NewLocation(MAPID, lr2).encode(out);
        var r = (LocationResponse) decode(in);
        testMatch(r, MAPID, MAPNAME,
                List.of(new LocationRecord(56, "-97.3", "32.72", "Bob Smith: Fort Worth", "Downtown"),
                        new LocationRecord(0, "-80.0", "30.0", "Earl: Waco", "Baylor!")));
    }

    @DisplayName("All Location (3)")
    @Test
    @Order(5)
    protected void testAllLocation() throws IOException, ValidationException {
        // Request all
        new LocationRequest(MAPID).encode(out);
        var r = (LocationResponse) decode(in);
        testMatch(r, MAPID, MAPNAME,
                List.of(new LocationRecord(56, "-97.3", "32.72", "Bob Smith: Fort Worth", "Downtown"),
                        new LocationRecord(0, "-80.0", "30.0", "Earl: Waco", "Baylor!")));
    }

    @DisplayName("Bad user (3)")
    @Test
    @Order(6)
    protected void testBadUser() throws IOException, ValidationException {
        // New location 1
        LocationRecord lr = new LocationRecord(57, "-97.12", "31.55", "FortWorth", "Down town");
        new NewLocation(MAPID, lr).encode(out);
        Error r = (Error) decode(in);
        assertAll(() -> assertEquals(MAPID, r.getMapId()),
                () -> assertTrue(r.getErrorMessage().contains("No such user")),
                () -> assertTrue(r.getErrorMessage().contains("57")));
    }

    @DisplayName("Bad map id (3)")
    @Test
    @Order(7)
    protected void testBadMapID() throws IOException, ValidationException {
        // New location 1
        LocationRecord lr = new LocationRecord(56, "-97.12", "31.55", "FortWorth", "Down town");
        new NewLocation(348, lr).encode(out);
        Error r = (Error) decode(in);
        assertAll(() -> assertEquals(348, r.getMapId()), () -> assertTrue(r.getErrorMessage().contains("No such map")),
                () -> assertTrue(r.getErrorMessage().contains("348")));
    }

    @DisplayName("Unexpected type (2)")
    @Test
    @Order(8)
    protected void testUnexpected() throws IOException, ValidationException {
        new Error(MAPID, "Boo").encode(out);
        Error r = (Error) decode(in);
        assertAll(() -> assertEquals(MAPID, r.getMapId()),
                () -> assertTrue(r.getErrorMessage().contains("Unexpected message type")));
    }

    @DisplayName("Invalid message (2)")
    @Test
    @Order(9)
    protected void testInvalid() throws IOException, NullPointerException, ValidationException {
        clientSocket.getOutputStream().write("ADDATUDEv1 345 ALLL \r\n".getBytes(ENC));
        Error r = (Error) decode(in);
        assertAll(() -> assertEquals(0, r.getMapId()),
                () -> assertTrue(r.getErrorMessage().contains("Invalid message")));
    }

    @DisplayName("Race Condition (3)")
    @Test
    @Order(10)
    protected void testRaceCondition()
            throws NullPointerException, InterruptedException, IOException, ValidationException {
        System.err.println("Restart server and hit return");
        System.in.read();
        final int BND = 1000;
        Runnable r = () -> {
            try (var sock = new Socket(SERVER, PORT)) {
                var rin = new MessageInput(sock.getInputStream());
                var rout = new MessageOutput(sock.getOutputStream());
                for (int i = 0; i <= BND; i++) {
                    String dg = (i % 90) + ".0";
                    String s = "N" + i;
                    LocationRecord lr;
                    lr = new LocationRecord(0, dg, dg, s, s);
                    new NewLocation(MAPID, lr).encode(rout);
                    var nl = (LocationResponse) decode(rin);
//                    testMatch(nl, MAPID, MAPNAME,
//                            List.of(new LocationRecord(0, dg, dg, "Earl:" + s, s)));
                }
            } catch (ValidationException | IOException e) {
                throw new RuntimeException("bad stuff", e);
            }
        };
        var t1 = new Thread(r);
        var t2 = new Thread(r);
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        try (var sock = new Socket(SERVER, PORT)) {
            var rin = new MessageInput(sock.getInputStream());
            var rout = new MessageOutput(sock.getOutputStream());
            new LocationRequest(MAPID).encode(rout);
            var rsp = (LocationResponse) decode(rin);
            String dg = (BND % 90) + ".0";
            String s = "N" + BND;
            testMatch(rsp, MAPID, MAPNAME, List.of(new LocationRecord(0, dg, dg, "Earl:" + s, s)));
        }
    }

    private static void testMatch(LocationResponse r, long mapid, String mapname, List<LocationRecord> explrs) {
        Comparator<LocationRecord> lrc = Comparator.comparing(LocationRecord::getLocationName);
        assertAll(() -> assertEquals(mapid, r.getMapId()), () -> assertEquals(mapname, r.getMapName()),
                () -> assertIterableEquals(explrs.stream().map(ServerTest::transform).sorted(lrc).toList(),
                        r.getLocationRecordList().stream().map(ServerTest::transform).sorted(lrc).toList()));
    }

    private static LocationRecord transform(LocationRecord r) {
        try {
            return r.setLocationName(r.getLocationName().replaceAll("\\s+", ""));
        } catch (ValidationException e) {
            return r;
        }
    }

    private static OutputStream getSlowOutput(OutputStream out, long milliseconds) {
        return new OutputStream() {
            @Override
            public synchronized void write(final int b) throws IOException {
                out.write(b);
                out.flush();
                try {
                    wait(milliseconds);
                } catch (InterruptedException e) {
                }
            }

            @Override
            public synchronized void write(final byte[] b, final int off, final int len) throws IOException {
                for (int i = off; i < len; i++) {
                    write(b[i]);
                }
            }

            @Override
            public synchronized void write(final byte[] b) throws IOException {
                write(b, 0, b.length);
            }
        };
    }
}
