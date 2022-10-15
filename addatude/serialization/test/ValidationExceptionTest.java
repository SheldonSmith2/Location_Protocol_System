/*
 * Sheldon Smith
 * Course: CSI 4321
 */
import addatude.serialization.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ValidationExceptionTest {
    @DisplayName("Constructor with Throwable Test")
    @ParameterizedTest(name = "token = {0}, msg = {1}")
    @CsvSource({"token, error message", "test, bad thing occured", "error, "})
    public void constructorTest(String token, String msg){
        try{
            throw new ValidationException(token, msg, new IOException());
        } catch (ValidationException e){
            assertEquals(token, e.getInvalidToken());
            assertEquals(msg, e.getMessage());
        }
    }

    @DisplayName("Constructor without Throwable Test")
    @ParameterizedTest(name = "token = {0}, msg = {1}")
    @CsvSource({"token, error message", "test, bad thing occured", "error, "})
    public void constructorTest2(String token, String msg){
        try{
            throw new ValidationException(token, msg);
        } catch (ValidationException e){
            assertEquals(token, e.getInvalidToken());
            assertEquals(msg, e.getMessage());
            assertEquals(null, e.getCause());
        }
    }
}
