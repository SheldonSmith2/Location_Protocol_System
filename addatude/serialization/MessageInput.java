/************************************************
 *
 * Author: Sheldon
 * Assignment: Program 1
 * Class: CSI 4321
 *
 ************************************************/
package addatude.serialization;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 *
 * Deserialization input source for messages.<br><br>
 *
 * Note: In its simplest form, this is just a wrapper for the InputStream. You can (and should) add (public) methods to
 * suit your needs (as this just defines the minimum interface). Do not add protocol-specific deserialization here;
 * those belong in the specific message classes. You must test all public methods.
 *
 * @author Sheldon Smith
 * @version 1.1
 */
public class MessageInput {
    /**The variable to hold the input stream of the message input*/
    private InputStream inputStream;
    /**The variable to hold the input stream reader of the message input*/
    private InputStreamReader reader;

    /**Constant to hold the carriage return integer*/
    private final int CARRIAGE_RETURN = 13;
    /**Constant to hold the line feed integer*/
    private final int LINE_FEED = 10;

    /**
     * Constructs a new input source from an InputStream
     *
     * @param  in byte input source
     * @throws NullPointerException if in is null
     */
    public MessageInput(InputStream in){
        //Check that the InputStream is not null
        if (Objects.isNull(in)){
            throw new NullPointerException("in cannot be null");
        }

        //Set the values for later use
        inputStream = in;
        reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
    }

    /**
     * read from the InputStreamReader
     *
     * @return number of bytes successfully read
     * @throws IOException if read fails
     */
    public int read() throws IOException {
        return reader.read();
    }

    /**
     * Check whether there is an EoS in the stream
     *
     * @throws IOException if read fails
     * @throws ValidationException if incorrect EoS is found
     */
    public void checkForEOS() throws IOException, ValidationException {
        int b = reader.read();
        if (b == CARRIAGE_RETURN){
            int t = reader.read();
            if (t != LINE_FEED){
                throw new ValidationException("Error Reading", "Incorrect EoS");
            }
        } else {
            throw new ValidationException("Error Reading", "Bad next character after string");
        }
    }

    /**
     * Check whether the read byte is valid
     *
     * @param currByte the current byte to be checked
     * @throws IOException if the end of the stream if hit
     * @throws ValidationException if part of the EoS is read
     */
    public void checkValidRead(int currByte) throws IOException, ValidationException {
        if (currByte == -1){
            throw new IOException();
        } else if (currByte == CARRIAGE_RETURN || currByte == LINE_FEED) {
            throw new ValidationException("Error Reading", "Premature EOS");
        }
    }

    /**
     * Reads until a space is found
     *
     * @return the string before the next space
     * @throws ValidationException if validation fails
     * @throws IOException if read fails
     */
    public String readUntilSpace() throws ValidationException, IOException {
        int currByte;
        StringBuilder currStr = new StringBuilder();
        while (((char)(currByte = reader.read())) != ' '){
            //Check that the read was successful
            checkValidRead(currByte);
            currStr.append((char)currByte);
        }
        return currStr.toString();
    }

}
