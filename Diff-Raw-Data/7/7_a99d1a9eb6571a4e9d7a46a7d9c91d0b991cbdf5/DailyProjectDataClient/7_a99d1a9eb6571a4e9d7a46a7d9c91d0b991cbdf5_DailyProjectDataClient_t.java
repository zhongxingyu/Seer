 package org.hackystat.dailyprojectdata.client;
 
 import java.io.StringReader;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.datatype.XMLGregorianCalendar;
 
 import org.hackystat.dailyprojectdata.resource.devtime.jaxb.DevTimeDailyProjectData;
 import org.restlet.Client;
 import org.restlet.data.ChallengeResponse;
 import org.restlet.data.ChallengeScheme;
 import org.restlet.data.MediaType;
 import org.restlet.data.Method;
 import org.restlet.data.Preference;
 import org.restlet.data.Protocol;
 import org.restlet.data.Reference;
 import org.restlet.data.Request;
 import org.restlet.data.Response;
 import org.restlet.data.Status;
 import org.restlet.resource.Representation;
 
 /**
  * Provides a client to support access to the DailyProjectData service. 
  * @author Philip Johnson
  */
 public class DailyProjectDataClient {
   
   /** Holds the userEmail to be associated with this client. */
   private String userEmail;
   /** Holds the password to be associated with this client. */
   private String password;
   /** The DailyProjectData host, such as "http://localhost:9877/dailyprojectdata". */
   private String dailyProjectDataHost;
   /** The Restlet Client instance used to communicate with the server. */
   private Client client;
   /** DevTime JAXBContext */
   private JAXBContext devTimeJAXB;
   /** The http authentication approach. */
   private ChallengeScheme scheme = ChallengeScheme.HTTP_BASIC;
   /** The preferred representation type. */
   private Preference<MediaType> xmlMedia = new Preference<MediaType>(MediaType.TEXT_XML);
   /** To facilitate debugging of problems using this system. */
   private boolean isTraceEnabled = false;
   
   /**
    * Initializes a new DailyProjectDataClient, given the host, userEmail, and password. 
    * Note that the userEmail and password refer to the underlying SensorBase client
    * associated with this DailyProjectData service.  This service does not keep its own
    * independent set of userEmails and passwords.  Authentication is not actually performed
    * in this constructor. Use the authenticate() method to explicitly check the authentication
    * credentials. 
    * @param host The host, such as 'http://localhost:9877/dailyprojectdata'.
    * @param email The user's email used for authentication. 
    * @param password The password used for authentication.
    */
   public DailyProjectDataClient(String host, String email, String password) {
     validateArg(host);
     validateArg(email);
     validateArg(password);
     this.userEmail = email;
     this.password = password;
     this.dailyProjectDataHost = host;
     if (!this.dailyProjectDataHost.endsWith("/")) {
       this.dailyProjectDataHost = this.dailyProjectDataHost + "/";
     }
     if (this.isTraceEnabled) {
       System.out.println("DailyProjectDataClient Tracing: INITIALIZE " + 
           "host='" + host + "', email='" + email + "', password='" + password + "'");
     }
     this.client = new Client(Protocol.HTTP);
     try {
       this.devTimeJAXB = 
         JAXBContext.newInstance(
             org.hackystat.dailyprojectdata.resource.devtime.jaxb.ObjectFactory.class);
     }
     catch (Exception e) {
       throw new RuntimeException("Couldn't create JAXB context instances.", e);
     }
   }
   
   /**
    * Throws an unchecked illegal argument exception if the arg is null or empty. 
    * @param arg The String that must be non-null and non-empty. 
    */
   private void validateArg(String arg) {
     if ((arg == null) || ("".equals(arg))) {
       throw new IllegalArgumentException(arg + " cannot be null or the empty string.");
     }
   }
   
   /**
    * Does the housekeeping for making HTTP requests to the SensorBase by a test or admin user. 
    * @param method The type of Method.
    * @param requestString A string, such as "users". No preceding slash. 
    * @param entity The representation to be sent with the request, or null if not needed.  
    * @return The Response instance returned from the server.
    */
   private Response makeRequest(Method method, String requestString, Representation entity) {
     Reference reference = new Reference(this.dailyProjectDataHost + requestString);
     Request request = (entity == null) ? 
         new Request(method, reference) :
           new Request(method, reference, entity);
     request.getClientInfo().getAcceptedMediaTypes().add(xmlMedia); 
     ChallengeResponse authentication = new ChallengeResponse(scheme, this.userEmail, this.password);
     request.setChallengeResponse(authentication);
     if (this.isTraceEnabled) {
       System.out.println("DailyProjectDataClient Tracing: " + method + " " + reference);
       if (entity != null) {
         try {
           System.out.println(entity.getText());
         }
         catch (Exception e) {
           System.out.println("  Problems with getText() on entity.");
         }
       }
     }
     Response response = this.client.handle(request);
     if (this.isTraceEnabled) {
       Status status = response.getStatus();
       System.out.println("  => " + status.getCode() + " " + status.getDescription());
     }
     return response;
   }
   
   /**
    * Takes a String encoding of a DevTimeDailyProjectData in XML format and converts it. 
    * @param xmlString The XML string representing a DevTimeDailyProjectData.
    * @return The corresponding DevTimeDailyProjectData instance. 
    * @throws Exception If problems occur during unmarshalling.
    */
   private DevTimeDailyProjectData makeDevTimeDailyProjectData(String xmlString) throws Exception {
     Unmarshaller unmarshaller = this.devTimeJAXB.createUnmarshaller();
     return (DevTimeDailyProjectData)unmarshaller.unmarshal(new StringReader(xmlString));
   }
   
   /**
    * Authenticates this user and password with this DailyProjectData service, throwing a
    * DailyProjectDataException if the user and password associated with this instance
    * are not valid credentials. 
    * Note that authentication is performed by checking these credentials with the 
    * SensorBase; this service does not keep its own independent set of usernames and passwords.
    * @return This DailyProjectDataClient instance. 
    * @throws DailyProjectDataClientException If authentication is not successful. 
    */
   public synchronized DailyProjectDataClient authenticate() throws DailyProjectDataClientException {
     // Performs authentication by invoking ping with user and password as form params.
     String uri = "ping?user=" + this.userEmail + "&password=" + this.password;
     Response response = makeRequest(Method.GET, uri, null); 
     if (!response.getStatus().isSuccess()) {
       throw new DailyProjectDataClientException(response.getStatus());
     }
     String responseString;
     try {
       responseString = response.getEntity().getText();
     }
     catch (Exception e) {
       throw new DailyProjectDataClientException("Bad response", e);
     }
     if (!"DailyProjectData authenticated".equals(responseString)) {
       throw new DailyProjectDataClientException("Authentication failed");
     }
     return this;
   }
   
   /**
    * Returns a DevTimeDailyProjectData instance from this server, or throws a
    * DailyProjectData exception if problems occurred.  
   * @param user The user that owns the project. 
   * @param project The project owned by user. 
    * @param timestamp The Timestamp indicating the start of the 24 hour period of DevTime.
   * @return A DevTimeDailyProjectData instance. 
    * @throws DailyProjectDataClientException If the credentials associated with this instance
    * are not valid, or if the underlying SensorBase service cannot be reached, or if one or more
    * of the supplied user, password, or timestamp is not valid.
    */
   public synchronized DevTimeDailyProjectData getDevTime(String user, String project, 
       XMLGregorianCalendar timestamp) throws DailyProjectDataClientException {
     Response response = makeRequest(Method.GET, "devtime/" + user + "/" + project + "/" + timestamp,
         null);
     DevTimeDailyProjectData devTime;
     if (!response.getStatus().isSuccess()) {
       throw new DailyProjectDataClientException(response.getStatus());
     }
     try {
       String xmlData = response.getEntity().getText();
       devTime = makeDevTimeDailyProjectData(xmlData);
     }
     catch (Exception e) {
       throw new DailyProjectDataClientException(response.getStatus(), e);
     }
     return devTime;
   } 
   
   /**
    * Returns true if the passed host is a DailyProjectData host. 
    * @param host The URL of a DailyProjectData host, "http://localhost:9876/dailyprojectdata".
    * @return True if this URL responds as a DailyProjectData host. 
    */
   public static boolean isHost(String host) {
     // All sensorbase hosts use the HTTP protocol.
     if (!host.startsWith("http://")) {
       return false;
     }
     // Create the host/register URL.
     try {
       String registerUri = host.endsWith("/") ? host + "ping" : host + "/ping"; 
       Request request = new Request();
       request.setResourceRef(registerUri);
       request.setMethod(Method.GET);
       Client client = new Client(Protocol.HTTP);
       Response response = client.handle(request);
       String pingText = response.getEntity().getText();
       return (response.getStatus().isSuccess() && "DailyProjectData".equals(pingText));
     }
     catch (Exception e) {
       return false;
     }
   }
   
 }
