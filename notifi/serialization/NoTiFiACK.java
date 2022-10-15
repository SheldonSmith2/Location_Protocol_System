/************************************************
 *
 * Author: Sheldon
 * Assignment: Program 4
 * Class: CSI 4321
 *
 ************************************************/
package notifi.serialization;

/**
 * NoTiFi ACK
 *
 * @author Sheldon Smith
 * @version 1.0
 */
public class NoTiFiACK extends NoTiFiMessage {
    /** The specific code for the ACK */
    public static final int CODE = 3;
    /**
     * Constructs NoTiFi ACK from values
     *
     * @param msgId message ID
     * @throws IllegalArgumentException if invalid message ID
     */
    public NoTiFiACK(int msgId) throws IllegalArgumentException{
        super(msgId);
    }

    public byte[] encode(){
        //Convert the header to bytes and return it
        return encodeHeader(CODE);
    }

    /**
     * Returns a String representation<br><br>
     * ACK:_msgid=&lt;msgid&gt;<br><br>
     * Underscore indicates a space. You <strong>must</strong> follow the spacing, etc. precisely<br><br>
     * For example<br><br>
     * ACK: msgid=501
     *
     * @return string representation
     */
    @Override
    public String toString(){
        return "ACK: msgid=" + msgID;
    }

    @Override
    public int getCode() {
        return CODE;
    }

}
