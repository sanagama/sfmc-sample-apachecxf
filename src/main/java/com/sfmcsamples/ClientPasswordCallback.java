
package com.sfmcsamples;

import java.io.IOException;
import java.util.logging.Logger;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import org.apache.wss4j.common.ext.WSPasswordCallback;

public class ClientPasswordCallback implements CallbackHandler
{
    private static final Logger LOGGER = Logger.getLogger( App.class.getName() );
    
    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException
    {
        // Read username and password from environment variables
        String username = System.getenv("SFMC_SOAP_API_USERNAME");
        String password = System.getenv("SFMC_SOAP_API_PASSWORD");

        WSPasswordCallback pc = (WSPasswordCallback) callbacks[0];
        String cbUsername = pc.getIdentifier();
        LOGGER.info("called for username: " + cbUsername);
        if(username.equals(cbUsername))
        {
            pc.setPassword(password);
            LOGGER.info("sent password for username: " + cbUsername);
        }
        else
        {
            LOGGER.warning("no password found for username: " + cbUsername);
        }
    }
}