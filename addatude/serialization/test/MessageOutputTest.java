/*
 * Sheldon Smith
 * Course: CSI 4321
 *
 * Brainstorm Buddy: Malik Mohamedali
 */
import addatude.serialization.MessageOutput;
import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class MessageOutputTest {
    //Nested tests for the constructor
    @DisplayName("Constructor Tests")
    @Nested
    class Constructor{
        @Test
        @DisplayName("Valid Constructor Test")
        public void constructorValid(){ //Function to test out valid creation
            MessageOutput output = new MessageOutput(new ByteArrayOutputStream());
            assertNotNull(output);
        }

        @Test
        @DisplayName("Null Constructor Test")
        public void constructorNull(){ //Function to test out invalid creation
            assertThrows(NullPointerException.class, () -> {new MessageOutput(null);});
        }
    }

    //Test that the write function works correctly
    @DisplayName("write Test")
    @Test
    public void writeTest() throws IOException {
        //Create stream for testing
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        MessageOutput out = new MessageOutput(stream);

        //Write to the MessageOutput
        byte[] arr = "100".getBytes(StandardCharsets.UTF_8);
        out.write(arr);

        //Check that the bytes match
        assertArrayEquals("100".getBytes(StandardCharsets.UTF_8), stream.toByteArray());
    }
}
