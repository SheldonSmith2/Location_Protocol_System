/************************************************
 *
 * Author: Sheldon
 * Assignment: Program 0
 * Class: CSI 4321
 *
 ************************************************/
package addatude.serialization;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

/**
 *
 * Serialization output source for messages. In its simplest form, this is just a wrapper for the OutputStream.
 * You may add methods to assist you with general serialization. Do not add protocol-specific serialization here.
 * That belongs in the specific message classes.
 *
 * @author Sheldon Smith
 * @version 1.1
 */
public class MessageOutput {
    OutputStream outputStream;

    /**
     * Constructs a new output sink to an OutputStream
     *
     * @param  out  byte output sink
     * @throws NullPointerException if out is null
     */
    public MessageOutput(OutputStream out){
        //Check if the OutputStream is null
        if (Objects.isNull(out)){
            //Throw exception if OutputStream is null
            throw new NullPointerException("out cannot be null");
        }
        outputStream = out;
    }

    /**
     * Write the given byte array to the MessageOutput
     *
     * @param  arr  the bytes arr to be written
     * @throws IOException if error occurs when writing
     */
    public void write(byte[] arr) throws IOException {
        //Write the given byte array to the outputStream
        outputStream.write(arr);
    }

}
