/************************************************
 *
 * Author: Sheldon
 * Assignment: Program 0
 * Class: CSI 4321
 *
 ************************************************/
package addatude.serialization;

import java.io.Serializable;

/**
 *
 * Validation exception containing the token failing validation
 *
 * @author Sheldon Smith
 * @version 1.1
 */
public class ValidationException extends Exception implements Serializable {
    /**The variable to hold the invalid token*/
    private String invalidToken;
    private static final long serialVersionUID = 1L;


    /**
     * Constructs validation exception
     *
     * @param invalidToken  token that failed validation
     * @param message exception message
     * @param cause exception cause
     */
    public ValidationException(String invalidToken, String message, Throwable cause) {
        //Set the values
        super(message, cause);
        this.invalidToken = invalidToken;
    }

    /**
     * Constructs validation exception with null cause
     *
     * @param  invalidToken  token that failed validation
     * @param message exception message
     */
    public ValidationException(String invalidToken, String message){
        //Set the values
        super(message);
        this.invalidToken = invalidToken;
    }

    /**
     * Get token that failed validation
     *
     * @return token that failed validation
     */
    public String getInvalidToken(){
        return this.invalidToken;
    }

    @Override
    public String getMessage(){
        return super.getMessage();
    }

    @Override
    public Throwable getCause(){
        return super.getCause();
    }
}
