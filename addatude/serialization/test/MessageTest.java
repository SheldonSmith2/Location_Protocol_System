import addatude.serialization.Message;
import addatude.serialization.MessageInput;
import addatude.serialization.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MessageTest {
    @DisplayName("Decode Exception Tests")
    @ParameterizedTest(name = "str = {0}")
    @ValueSource(strings = {"ADDA", "ADDATUDEv1 501 ERROR g test"})
    public void decodeTest(String str){
        MessageInput in = new MessageInput(new ByteArrayInputStream(str.getBytes()));
        assertThrows(ValidationException.class, () -> Message.decode(in));
    }

    @DisplayName("Decode Exception Tests 2")
    @ParameterizedTest(name = "str = {0}")
    @ValueSource(strings = {"ADDATUDEv1 34 Test 4 Test"})
    public void decodeTest2(String str){
        MessageInput in = new MessageInput(new ByteArrayInputStream(str.getBytes()));
        ValidationException ex = assertThrows(ValidationException.class, () -> Message.decode(in));
        assertEquals("Invalid format", ex.getMessage());
    }

}
