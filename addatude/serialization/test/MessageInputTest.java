/*
 * Sheldon Smith
 * Course: CSI 4321
 *
 * Brainstorm Buddy: Malik Mohamedali
 */
import addatude.serialization.MessageInput;
import addatude.serialization.ValidationException;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

//The class to test the message input functions
public class MessageInputTest {

    //Nested tests for the constructor
    @DisplayName("Constructor Tests")
    @Nested
    class Constructor{
        @Test
        @DisplayName("Valid Constructor Test")
        public void constructorValid(){ //Function to test out valid creation
            MessageInput input = new MessageInput(new ByteArrayInputStream("".getBytes()));
            assertNotNull(input);
        }

        @Test
        @DisplayName("Null Constructor Test")
        public void constructorNull(){ //Function to test out invalid creation
            assertThrows(NullPointerException.class, () -> {new MessageInput(null);});
        }
    }

    @DisplayName("EOS check Tests")
    @Nested
    class eosTests{
        @DisplayName("Invalid EoS Read")
        @Test
        public void invalidEOS(){
            MessageInput in = new MessageInput(new ByteArrayInputStream("\re".getBytes(StandardCharsets.UTF_8)));
            assertThrows(ValidationException.class, in::checkForEOS);
        }

        @DisplayName("No EOS Check")
        @Test
        public void invalidEOS2(){
            MessageInput in = new MessageInput(new ByteArrayInputStream("rwer".getBytes(StandardCharsets.UTF_8)));
            assertThrows(ValidationException.class, in::checkForEOS);
        }
    }

    @DisplayName("Check Valid Read Tests")
    @Nested
    class validReadTests{
        @DisplayName("Invalid Check")
        @Test
        public void invalidCheck(){
            MessageInput in = new MessageInput(new ByteArrayInputStream("".getBytes()));
            assertThrows(IOException.class, () -> in.checkValidRead(-1));
        }

        @DisplayName("Invalid Check")
        @ParameterizedTest(name = "int = {0}")
        @ValueSource(ints = {10, 13})
        public void invalidCheck2(int i){
            MessageInput in = new MessageInput(new ByteArrayInputStream("".getBytes()));
            assertThrows(ValidationException.class, () -> in.checkValidRead(i));
        }
    }

    @DisplayName("Read Until Space Test")
    @ParameterizedTest(name = "str = {0}")
    @CsvSource({"test ing, test", "this is a test, this", "t t, t"})
    public void readUntilSpaceTest(String all, String correct) throws ValidationException, IOException {
        MessageInput in = new MessageInput(new ByteArrayInputStream(all.getBytes()));
        String str = in.readUntilSpace();
        assertEquals(correct, str);
    }
}
