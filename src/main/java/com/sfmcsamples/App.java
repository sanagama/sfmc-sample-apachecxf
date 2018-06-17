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
        String toEmailAddress = "sanagama2@gmail.com";
        String fromEmailAddress = "sanagama2@gmail.com";
        String customerKey = "Apache-CXF-Test";

        // Create TriggeredSendDefinition and initialize the TriggeredSend
        LOGGER.info("Creating TriggeredSendDefinition...");
        TriggeredSendDefinition tsd = new TriggeredSendDefinition();
    	tsd.setCustomerKey( customerKey );
    	TriggeredSend triggeredSend = new TriggeredSend();
    	triggeredSend.setTriggeredSendDefinition(tsd);

    	// Create a Subscriber
        LOGGER.info("Creating Subscriber...");
    	Subscriber subscriber = new Subscriber();
    	subscriber.setEmailAddress(toEmailAddress);
        subscriber.setSubscriberKey(customerKey);

/*        
    	Owner ownerSubscriber = new Owner();
    	ownerSubscriber.setFromAddress( validFromAddress );
    	ownerSubscriber.setFromName( validFromName );
    	subscriber.setOwner( ownerSubscriber );
*/
    	//Populate array of Subscribers
        testArray[0] = subscriber;
        testArray[1] = subscriberInvalid;
        java.util.List<Subscriber> list = Arrays.asList( testArray );        
        triggeredSend.getSubscribers().addAll( list );

        // Create an e-mail
        LOGGER.info("Creating a new Email...");
        Email newEmail = new Email();
        newEmail.setName(customerKey);
        newEmail.setSubject(customerKey);
        newEmail.setHTMLBody("<html><body>this is a test email from the Java Apache CXF sample</body></html>");
        String newObjectIdEmail = createObjectHelper(soapClient, newEmail);

        // Create a Send object
        LOGGER.info("Creating a new Send...");
        EmailSendDefinition sendDefinition = new EmailSendDefinition();
        sendDefinition.setCustomerKey(customerKey);
        //sendDefinition
        Send newSend = new Send();
        newSend.setEmail(newEmail);
        newSend.setCustomerKey(customerKey);
        newSend.setFromAddress("sanagama2@gmail.com");
        newSend.setFromName("Sanjay Nagamangalam");
        newSend.setEmailSendDefinition(sendDefinition);
        String newObjectIdSend = createObjectHelper(soapClient, newSend);

        PerformRequestMsg performRequestMsg = new PerformRequestMsg();
        performRequestMsg.setOptions(new PerformOptions());
        performRequestMsg.setAction("start");
        //performRequestMsg.setDefinitions( new EmailSendDefinition[]{definition});
        //This sends email using User-Initiated-Email-definition.
        PerformResponseMsg responseMsg = soapClient.perform(performRequestMsg);
        System.out.println(responseMsg.getOverallStatus());

    	// Create a new Subscriber
    	Subscriber newSubscriber = new Subscriber();
        newSubscriber.setEmailAddress(fromEmailAddress);
        String newSubscriberKey = createObjectHelper(soapClient, newSubscriber);
        
        TriggeredSendDefinition triggeredSendDefinition = new TriggeredSendDefinition();
    	triggeredSendDefinition.setCustomerKey( customerKey );
    	TriggeredSend triggeredSend = new TriggeredSend();
        triggeredSend.setTriggeredSendDefinition(triggeredSendDefinition);
    }

    private String createObjectHelper(Soap soapClient, APIObject object)
    {
        String newObjectId = "";
        try
        {
            CreateRequest createRequest = new CreateRequest();
            createRequest.getObjects().addAll(Arrays.asList( new APIObject[] {object} ));
            createRequest.setOptions(new CreateOptions());

            CreateResponse response = soapClient.create(createRequest);
            String overallStatus = response.getOverallStatus();
            CreateResult result = response.getResults().get(0); // get the first and only result from the list of results
            newObjectId = result.getNewObjectID();

            LOGGER.info("overall status = " + overallStatus);
            if(overallStatus != "OK")
            {
                LOGGER.info("status code = " + result.getStatusCode() + 
                            ", status message = " + result.getStatusMessage() +
                            ", NewID = " + result.getNewID() +
                            ", NewObjectID = " + result.getNewObjectID());
            }
        }
        catch(Exception ex)
        {
            LOGGER.severe(ex.getMessage());
        }
        return newObjectId; 
    }
}
