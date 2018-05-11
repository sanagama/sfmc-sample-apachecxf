package com.sfmcsamples;

import java.util.*;
import java.util.logging.Logger;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.cxf.message.Message;
import org.apache.wss4j.dom.WSConstants;
import org.apache.wss4j.dom.handler.WSHandlerConstants;

import com.exacttarget.wsdl.partnerapi.*;


/**
 * Salesforce Marteting Cloud SOAP API sample with Apache CXF
 *
 */
public class App 
{
    // NOTE:
    // Set the following environment variables before running this app:
    //
    // SFMC_SOAP_API_USERNAME=<SOAP API username for your SFMC account>
    // SFMC_SOAP_API_PASSWORD=<SOAP API password for your SFMC account>
    //
    // More details: https://developer.salesforce.com/docs/atlas.en-us.noversion.mc-apis.meta/mc-apis/authenticate-soap-api.htm
    //

    private static final Logger LOGGER = Logger.getLogger( App.class.getName() );
    
    public static void main( String[] args )
    {
        LOGGER.info("Sample Java app that uses Marketing Cloud SOAP APIs with Apache CXF");
        App app = new App();
        app.run();
        LOGGER.info("All done!");
    }

    public void run()
    {
        try
        {
            // Initialize SOAP client with UsernameToken
            PartnerAPI partnerApi = new PartnerAPI();
            Soap soapClient = createSoapClient(partnerApi);            

            // Run samples
            SendEmail(soapClient);

            //TriggeredSendSample(soapClient);
        }
        catch( Exception e )
        {
        	e.printStackTrace();
        }
    }

    private Soap createSoapClient(PartnerAPI partnerApi)
    {
        Soap soapClient = partnerApi.getSoap();
        Client client = ClientProxy.getClient(soapClient);
        Endpoint cxfEndpoint = client.getEndpoint();

        // Initialize interceptor for UsernameToken and callback for password
        Map<String,Object> outProps = new HashMap<String,Object>();
        outProps.put(WSHandlerConstants.ACTION, WSHandlerConstants.USERNAME_TOKEN);
        outProps.put(WSHandlerConstants.USER, System.getenv("SFMC_SOAP_API_USERNAME"));
        outProps.put(WSHandlerConstants.PASSWORD_TYPE, WSConstants.PW_TEXT);
        outProps.put(WSHandlerConstants.PW_CALLBACK_CLASS, ClientPasswordCallback.class.getName() );

        WSS4JOutInterceptor wssOut = new WSS4JOutInterceptor(outProps);
        cxfEndpoint.getOutInterceptors().add(wssOut);

        client.getRequestContext().put(Message.ENCODING, "UTF-8");
        return soapClient;
    }
  
    private void SendEmail(Soap soapClient)
    {
        // Create an e-mail
        Email email = new Email();
        email.setName("Test from Apache CXF");
        email.setStatus("Test from Apache CXF");
        email.setSubject("Test from Apache CXF");
        email.setCharacterSet("UTF-8");
        email.setHTMLBody("<html><body>this is a test email from the Java Apache CXF sample</body></html>");

        CreateRequest createRequest = new CreateRequest();
        createRequest.getObjects().addAll(Arrays.asList( new APIObject[] {email} ));
        createRequest.setOptions(new CreateOptions());
        CreateResponse response = soapClient.create(createRequest);
        LOGGER.info("overall status = " + response.getOverallStatus());
        for (CreateResult result : response.getResults())
        {
            LOGGER.info("status code = " + result.getStatusCode() + ", status message = " + result.getStatusMessage());
        }
    }
}
