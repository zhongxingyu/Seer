 package org.hackystat.sensorbase.client;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.Marshaller;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.datatype.XMLGregorianCalendar;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.hackystat.sensorbase.resource.projects.jaxb.Invitations;
 import org.hackystat.sensorbase.resource.projects.jaxb.Project;
 import org.hackystat.sensorbase.resource.projects.jaxb.ProjectIndex;
 import org.hackystat.sensorbase.resource.projects.jaxb.ProjectRef;
 import org.hackystat.sensorbase.resource.projects.jaxb.ProjectSummary;
 import org.hackystat.sensorbase.resource.sensorbase.SensorBaseResource;
 import org.hackystat.sensorbase.resource.sensordata.jaxb.Property;
 import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorData;
 import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorDataIndex;
 import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorDataRef;
 import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorDatas;
 import org.hackystat.sensorbase.resource.sensordatatypes.jaxb.SensorDataType;
 import org.hackystat.sensorbase.resource.sensordatatypes.jaxb.SensorDataTypeIndex;
 import org.hackystat.sensorbase.resource.sensordatatypes.jaxb.SensorDataTypeRef;
 import org.hackystat.sensorbase.resource.users.jaxb.Properties;
 import org.hackystat.sensorbase.resource.users.jaxb.User;
 import org.hackystat.sensorbase.resource.users.jaxb.UserIndex;
 import org.hackystat.sensorbase.resource.users.jaxb.UserRef;
 import org.hackystat.utilities.tstamp.Tstamp;
 import org.hackystat.utilities.uricache.NewUriCache;
 import org.hackystat.utilities.uricache.UriCacheException;
 import org.restlet.Client;
 import org.restlet.data.ChallengeResponse;
 import org.restlet.data.ChallengeScheme;
 import org.restlet.data.Form;
 import org.restlet.data.MediaType;
 import org.restlet.data.Method;
 import org.restlet.data.Preference;
 import org.restlet.data.Protocol;
 import org.restlet.data.Reference;
 import org.restlet.data.Request;
 import org.restlet.data.Response;
 import org.restlet.data.Status;
 import org.restlet.resource.Representation;
 import org.w3c.dom.Document;
 
 /**
  * Provides a high-level interface for Clients wishing to communicate with a SensorBase.
  * 
  * @author Philip Johnson
  * 
  */
 public class SensorBaseClient {
   
   /** The possible responses to a Project invitation. */
   public enum InvitationReply { ACCEPT, DECLINE };
 
   /** Holds the userEmail to be associated with this client. */
   private String userEmail;
   /** Holds the password to be associated with this client. */
   private String password;
   /** The SensorBase host, such as "http://localhost:9876/sensorbase". */
   private String sensorBaseHost;
   /** The Restlet Client instance used to communicate with the server. */
   private Client client;
   /** SDT JAXBContext */
   private static final JAXBContext sdtJAXB;
   /** Users JAXBContext */
   private static final JAXBContext userJAXB;
   /** SDT JAXBContext */
   private static final JAXBContext sensordataJAXB;
   /** SDT JAXBContext */
   private static final JAXBContext projectJAXB;
   /** The http authentication approach. */
   private ChallengeScheme scheme = ChallengeScheme.HTTP_BASIC;
   /** The preferred representation type. */
   private Preference<MediaType> xmlMedia = new Preference<MediaType>(MediaType.TEXT_XML);
   /** For PMD */
   private String sensordataUri = "sensordata/";
   /** For PMD */
   private String projectsUri = "projects/";
   /** For PMD */
   private String andEndTime = "&endTime=";
   
   /** To facilitate debugging of problems using this system. */
   private boolean isTraceEnabled = false;
 
   /** An associated UriCache to improve responsiveness. */
   private NewUriCache uriCache;
   
   /** Indicates whether or not cache is enabled. */
   private boolean isCacheEnabled = false;
   
   /** Timestamp of last time we tried to contact a server and failed, since this is expensive. */
   private static Map<String, Long> lastHostNotAvailable = new HashMap<String, Long>();
   
   /** The System property key used to retrieve the default timeout value in milliseconds. */
   public static final String SENSORBASECLIENT_TIMEOUT_KEY = "sensorbaseclient.timeout";
 
   // JAXBContexts are thread safe, so we can share them across all instances and threads.
   // https://jaxb.dev.java.net/guide/Performance_and_thread_safety.html
   static {
     try {
       sdtJAXB = JAXBContext
           .newInstance(org.hackystat.sensorbase.resource.sensordatatypes.jaxb.ObjectFactory.class);
       userJAXB = JAXBContext
           .newInstance(org.hackystat.sensorbase.resource.users.jaxb.ObjectFactory.class);
       sensordataJAXB = JAXBContext
           .newInstance(org.hackystat.sensorbase.resource.sensordata.jaxb.ObjectFactory.class);
       projectJAXB = JAXBContext
           .newInstance(org.hackystat.sensorbase.resource.projects.jaxb.ObjectFactory.class);
     }
     catch (Exception e) {
       throw new RuntimeException("Couldn't create JAXB context instances.", e);
     }
   }
 
   /**
    * Initializes a new SensorBaseClient, given the host, userEmail, and password.
    * 
    * @param host The host, such as 'http://localhost:9876/sensorbase'.
    * @param email The user's email that we will use for authentication.
    * @param password The password we will use for authentication.
    */
   public SensorBaseClient(String host, String email, String password) {
     validateArg(host);
     validateArg(email);
     validateArg(password);
     this.userEmail = email;
     this.password = password;
     this.sensorBaseHost = host;
     if (!this.sensorBaseHost.endsWith("/")) {
       this.sensorBaseHost = this.sensorBaseHost + "/";
     }
     if (this.isTraceEnabled) {
       System.out.println("SensorBaseClient Tracing: INITIALIZE " + "host='" + host + "', email='"
           + email + "', password='" + password + "'");
     }
     this.client = new Client(Protocol.HTTP);
     setTimeout(getDefaultTimeout());
   }
   
   /**
    * Attempts to provide a timeout value for this SensorBaseClient.  
    * @param milliseconds The number of milliseconds to wait before timing out. 
    */
   public final synchronized void setTimeout(int milliseconds) {
     setClientTimeout(this.client, milliseconds);
   }
   
   /**
    * Returns the default timeout in milliseconds. 
    * The default timeout is set to 2000 ms, but clients can change this by creating a 
    * System property called sensorbaseclient.timeout and set it to a String indicating
    * the number of milliseconds.  
    * @return The default timeout.
    */
   private static int getDefaultTimeout() {
     String systemTimeout = System.getProperty(SENSORBASECLIENT_TIMEOUT_KEY, "2000");
     int timeout = 2000;
     try {
       timeout = Integer.parseInt(systemTimeout);
     }
     catch (Exception e) {
       timeout = 2000;
     }
     return timeout;
   }
 
   /**
    * When passed true, future HTTP calls using this client instance will print out information on
    * the request and response.
    * 
    * @param enable If true, trace output will be generated.
    */
   public synchronized void enableHttpTracing(boolean enable) {
     this.isTraceEnabled = enable;
   }
 
   /**
    * Authenticates this user and password with the server.
    * 
    * @return This SensorBaseClient instance.
    * @throws SensorBaseClientException If authentication is not successful.
    */
   public synchronized SensorBaseClient authenticate() throws SensorBaseClientException {
     String uri = "ping?user=" + this.userEmail + "&password=" + this.password;
     Response response = makeRequest(Method.GET, uri, null);
     if (!response.getStatus().isSuccess()) {
       throw new SensorBaseClientException(response.getStatus());
     }
     String responseString;
     try {
       responseString = response.getEntity().getText();
     }
     catch (Exception e) {
       throw new SensorBaseClientException("Bad response", e);
     }
     if (!"SensorBase authenticated".equals(responseString)) {
       throw new SensorBaseClientException("Authentication failed");
     }
     return this;
   }
 
   /**
    * Provides an easy way to construct SensorData instances. The keyValMap is processed and the
    * key-value pairs are processed in the following way.
    * <ul>
    * <li> ["Owner", email] (if not supplied, defaults to this client's email)
    * <li> ["Timestamp", timestamp string] (if not supplied, defaults to the current time.)
    * <li> ["Runtime", timestamp string] (if not supplied, defaults to Timestamp.)
    * <li> ["Resource", resource string] (if not supplied, defaults to "")
    * <li> ["SensorDataType", sdt string] (if not supplied, defaults to "")
    * <li> ["Tool", tool string] (if not supplied, defaults to "")
    * <li> Any other key-val pairs are extracted and put into the SensorData properties.
    * </ul>
    * Throws an exception if Timestamp or Runtime are supplied but can't be parsed into an
    * XMLGregorianCalendar instance.
    * 
    * @param keyValMap The map of key-value pairs corresponding to SensorData fields and properties.
    * @return A SensorData instance.
    * @throws SensorBaseClientException If errors occur parsing the contents of the keyValMap.
    */
   public synchronized SensorData makeSensorData(Map<String, String> keyValMap)
       throws SensorBaseClientException {
     // Begin by creating the sensor data instance.
     SensorData data = new SensorData();
     XMLGregorianCalendar defaultTstamp = Tstamp.makeTimestamp();
     XMLGregorianCalendar tstamp = null;
     try {
       tstamp = Tstamp.makeTimestamp(takeFromMap(keyValMap, "Timestamp", defaultTstamp.toString()));
     }
     catch (Exception e) {
       throw new SensorBaseClientException("Error parsing tstamp", e);
     }
     XMLGregorianCalendar runtime = null;
     try {
       runtime = Tstamp.makeTimestamp(takeFromMap(keyValMap, "Runtime", defaultTstamp.toString()));
     }
     catch (Exception e) {
       throw new SensorBaseClientException("Error parsing runtime", e);
     }
     data.setOwner(takeFromMap(keyValMap, "Owner", userEmail));
     data.setResource(takeFromMap(keyValMap, "Resource", ""));
     data.setRuntime(runtime);
     data.setSensorDataType(takeFromMap(keyValMap, "SensorDataType", ""));
     data.setTimestamp(tstamp);
     data.setTool(takeFromMap(keyValMap, "Tool", "unknown"));
     // Add all remaining key-val pairs to the property list.
     data.setProperties(new org.hackystat.sensorbase.resource.sensordata.jaxb.Properties());
     for (Map.Entry<String, String> entry : keyValMap.entrySet()) {
       Property property = new Property();
       property.setKey(entry.getKey());
       property.setValue(entry.getValue());
       data.getProperties().getProperty().add(property);
     }
     return data;
   }
 
   /**
    * Returns the value associated with key in keyValMap, or the default, and also removes the
    * mapping associated with key from the keyValMap.
    * 
    * @param keyValMap The map
    * @param key The key
    * @param defaultValue The value to return if the key has no mapping.
    * @return The value to be used.
    */
   private String takeFromMap(Map<String, String> keyValMap, String key, String defaultValue) {
     String value = (keyValMap.get(key) == null) ? defaultValue : keyValMap.get(key);
     keyValMap.remove(key);
     return value;
   }
 
   /**
    * Returns the index of SensorDataTypes from this server.
    * 
    * @return The SensorDataTypeIndex instance.
    * @throws SensorBaseClientException If the server does not return the Index or returns an index
    *         that cannot be marshalled into Java SensorDataTypeIndex instance.
    */
   public synchronized SensorDataTypeIndex getSensorDataTypeIndex() 
                                                       throws SensorBaseClientException {
     Response response = makeRequest(Method.GET, "sensordatatypes", null);
     SensorDataTypeIndex index;
     if (!response.getStatus().isSuccess()) {
       throw new SensorBaseClientException(response.getStatus());
     }
     try {
       String xmlData = response.getEntity().getText();
       index = makeSensorDataTypeIndex(xmlData);
     }
     catch (Exception e) {
       throw new SensorBaseClientException(response.getStatus(), e);
     }
     return index;
   }
 
   /**
    * Returns the named SensorDataType from this server.
    * 
    * @param sdtName The SDT name.
    * @return The SensorDataType instance.
    * @throws SensorBaseClientException If the server does not return the SDT or returns something
    *         that cannot be marshalled into Java SensorDataType instance.
    */
   public synchronized SensorDataType getSensorDataType(String sdtName)
       throws SensorBaseClientException {
     Response response = makeRequest(Method.GET, "sensordatatypes/" + sdtName, null);
     SensorDataType sdt;
     if (!response.getStatus().isSuccess()) {
       throw new SensorBaseClientException(response.getStatus());
     }
     try {
       String xmlData = response.getEntity().getText();
       sdt = makeSensorDataType(xmlData);
     }
     catch (Exception e) {
       throw new SensorBaseClientException(response.getStatus(), e);
     }
     return sdt;
   }
 
   /**
    * Returns the named SensorDataType associated with the SensorDataTypeRef.
    * 
    * @param ref The SensorDataTypeRef instance
    * @return The SensorDataType instance.
    * @throws SensorBaseClientException If the server does not return the SDT or returns something
    *         that cannot be marshalled into Java SensorDataType instance.
    */
   public synchronized SensorDataType getSensorDataType(SensorDataTypeRef ref)
       throws SensorBaseClientException {
     Response response = getUri(ref.getHref());
     SensorDataType sdt;
     if (!response.getStatus().isSuccess()) {
       throw new SensorBaseClientException(response.getStatus());
     }
     try {
       String xmlData = response.getEntity().getText();
       sdt = makeSensorDataType(xmlData);
     }
     catch (Exception e) {
       throw new SensorBaseClientException(response.getStatus(), e);
     }
     return sdt;
   }
 
   /**
    * Creates the passed SDT on the server. This is an admin-only operation.
    * 
    * @param sdt The SDT to create.
    * @throws SensorBaseClientException If the user is not the admin or if there is some problem with
    *         the SDT instance.
    */
   public synchronized void putSensorDataType(SensorDataType sdt) throws SensorBaseClientException {
     try {
       String xmlData = makeSensorDataType(sdt);
       Representation representation = SensorBaseResource.getStringRepresentation(xmlData);
       String uri = "sensordatatypes/" + sdt.getName();
       Response response = makeRequest(Method.PUT, uri, representation);
       if (!response.getStatus().isSuccess()) {
         throw new SensorBaseClientException(response.getStatus());
       }
     }
     // Allow SensorBaseClientExceptions to be thrown out of this method.
     catch (SensorBaseClientException f) {
       throw f;
     }
     // All other exceptions are caught and rethrown.
     catch (Exception e) {
       throw new SensorBaseClientException("Error marshalling SDT", e);
     }
   }
 
   /**
    * Deletes the SDT given its name.
    * 
    * @param sdtName The name of the SDT to delete.
    * @throws SensorBaseClientException If the server does not indicate success.
    */
   public synchronized void deleteSensorDataType(String sdtName) throws SensorBaseClientException {
     Response response = makeRequest(Method.DELETE, "sensordatatypes/" + sdtName, null);
     if (!response.getStatus().isSuccess()) {
       throw new SensorBaseClientException(response.getStatus());
     }
   }
 
   /**
    * Returns the index of Users from this server. This is an admin-only operation.
    * 
    * @return The UserIndex instance.
    * @throws SensorBaseClientException If the server does not return the Index or returns an index
    *         that cannot be marshalled into Java UserIndex instance.
    */
   public synchronized UserIndex getUserIndex() throws SensorBaseClientException {
     Response response = makeRequest(Method.GET, "users", null);
     UserIndex index;
     if (!response.getStatus().isSuccess()) {
       throw new SensorBaseClientException(response.getStatus());
     }
     try {
       String xmlData = response.getEntity().getText();
       index = makeUserIndex(xmlData);
     }
     catch (Exception e) {
       throw new SensorBaseClientException(response.getStatus(), e);
     }
     return index;
   }
 
   /**
    * Returns the named User from this server.
    * 
    * @param email The user email.
    * @return The User.
    * @throws SensorBaseClientException If the server does not return the SDT or returns something
    *         that cannot be marshalled into Java User instance.
    */
   public synchronized User getUser(String email) throws SensorBaseClientException {
     Response response = makeRequest(Method.GET, "users/" + userEmail, null);
     User user;
     if (!response.getStatus().isSuccess()) {
       throw new SensorBaseClientException(response.getStatus());
     }
     try {
       String xmlData = response.getEntity().getText();
       user = makeUser(xmlData);
     }
     catch (Exception e) {
       throw new SensorBaseClientException(response.getStatus(), e);
     }
     return user;
   }
 
   /**
    * Returns the named User associated with the UserRef.
    * 
    * @param ref The UserRef instance
    * @return The User instance.
    * @throws SensorBaseClientException If the server does not return the user or returns something
    *         that cannot be marshalled into Java User instance.
    */
   public synchronized User getUser(UserRef ref) throws SensorBaseClientException {
     Response response = getUri(ref.getHref());
     User user;
     if (!response.getStatus().isSuccess()) {
       throw new SensorBaseClientException(response.getStatus());
     }
     try {
       String xmlData = response.getEntity().getText();
       user = makeUser(xmlData);
     }
     catch (Exception e) {
       throw new SensorBaseClientException(response.getStatus(), e);
     }
     return user;
   }
 
   /**
    * Deletes the User given their email.
    * 
    * @param email The email of the User to delete.
    * @throws SensorBaseClientException If the server does not indicate success.
    */
   public synchronized void deleteUser(String email) throws SensorBaseClientException {
     Response response = makeRequest(Method.DELETE, "users/" + email, null);
     if (!response.getStatus().isSuccess()) {
       throw new SensorBaseClientException(response.getStatus());
     }
   }
 
   /**
    * Updates the specified user's properties.
    * 
    * @param email The email of the User whose properties are to be deleted.
    * @param properties The properties to post.
    * @throws SensorBaseClientException If the server does not indicate success.
    */
   public synchronized void updateUserProperties(String email, Properties properties)
       throws SensorBaseClientException {
     String xmlData;
     try {
       xmlData = makeProperties(properties);
     }
     catch (Exception e) {
       throw new SensorBaseClientException("Failed to marshall Properties instance.", e);
     }
     Representation representation = SensorBaseResource.getStringRepresentation(xmlData);
     Response response = makeRequest(Method.POST, "users/" + email, representation);
     if (!response.getStatus().isSuccess()) {
       throw new SensorBaseClientException(response.getStatus());
     }
   }
 
   /**
    * GETs the URI string and returns the Restlet Response if the server indicates success.
    * 
    * @param uriString The URI String, such as "http://localhost:9876/sensorbase/sensordatatypes".
    * @return The response instance if the GET request succeeded.
    * @throws SensorBaseClientException If the server indicates that a problem occurred.
    */
   public synchronized Response getUri(String uriString) throws SensorBaseClientException {
     Reference reference = new Reference(uriString);
     Request request = new Request(Method.GET, reference);
     request.getClientInfo().getAcceptedMediaTypes().add(xmlMedia);
     ChallengeResponse authentication = new ChallengeResponse(scheme, this.userEmail, this.password);
     request.setChallengeResponse(authentication);
     if (this.isTraceEnabled) {
       System.out.println("SensorBaseClient Tracing: GET " + reference);
     }
     Response response = this.client.handle(request);
     if (this.isTraceEnabled) {
       Status status = response.getStatus();
       System.out.println("  => " + status.getCode() + " " + status.getDescription());
     }
     if (!response.getStatus().isSuccess()) {
       throw new SensorBaseClientException(response.getStatus());
     }
     return response;
   }
 
   /**
    * Returns the index of SensorData from this server. This is an admin-only operation.
    * 
    * @return The SensorDataIndex instance.
    * @throws SensorBaseClientException If the server does not return the Index or returns an index
    *         that cannot be marshalled into Java SensorDataIndex instance.
    */
   public synchronized SensorDataIndex getSensorDataIndex() throws SensorBaseClientException {
     Response response = makeRequest(Method.GET, "sensordata", null);
     SensorDataIndex index;
     if (!response.getStatus().isSuccess()) {
       throw new SensorBaseClientException(response.getStatus());
     }
     try {
       String xmlData = response.getEntity().getText();
       index = makeSensorDataIndex(xmlData);
     }
     catch (Exception e) {
       throw new SensorBaseClientException(response.getStatus(), e);
     }
     setSensorDataIndexLastMod(index);
     return index;
   }
 
   /**
    * Computes the lastMod value for this index. Iterates through the individual refs to find their
    * lastMod values, and stores the most recent lastMod value as the result. If the index is empty,
    * then a default lastMod of 1000-01-01 is returned. We hope that indexes are cached so that this
    * is not done a lot.
    * 
    * @param index The index.
    * @throws SensorBaseClientException If problems occur parsing the lastMod fields.
    */
   private void setSensorDataIndexLastMod(SensorDataIndex index) throws SensorBaseClientException {
     try {
       index.setLastMod(Tstamp.makeTimestamp("1000-01-01"));
       for (SensorDataRef ref : index.getSensorDataRef()) {
         XMLGregorianCalendar lastMod = ref.getLastMod();
         if ((!(lastMod == null)) && Tstamp.greaterThan(lastMod, index.getLastMod())) {
           index.setLastMod(lastMod);
         }
       }
     }
     catch (Exception e) {
       throw new SensorBaseClientException("Error setting LastMod for index: " + index, e);
     }
   }
 
   /**
    * Returns the index of SensorData for this user from this server.
    * 
    * @param email The user email.
    * @return The SensorDataIndex instance.
    * @throws SensorBaseClientException If the server does not return the Index or returns an index
    *         that cannot be marshalled into Java SensorDataIndex instance.
    */
   public synchronized SensorDataIndex getSensorDataIndex(String email)
       throws SensorBaseClientException {
     Response response = makeRequest(Method.GET, sensordataUri + email, null);
     SensorDataIndex index;
     if (!response.getStatus().isSuccess()) {
       throw new SensorBaseClientException(response.getStatus());
     }
     try {
       String xmlData = response.getEntity().getText();
       index = makeSensorDataIndex(xmlData);
     }
     catch (Exception e) {
       throw new SensorBaseClientException(response.getStatus(), e);
     }
     setSensorDataIndexLastMod(index);
     return index;
   }
 
   /**
    * Returns the index of SensorData for this user from this server with the specified SDT.
    * 
    * @param email The user email.
    * @param sdtName The name of the SDT whose SensorData is to be returned.
    * @return The SensorDataIndex instance.
    * @throws SensorBaseClientException If the server does not return the Index or returns an index
    *         that cannot be marshalled into Java SensorDataIndex instance.
    */
   public synchronized SensorDataIndex getSensorDataIndex(String email, String sdtName)
       throws SensorBaseClientException {
     Response response = makeRequest(Method.GET, sensordataUri + email + "?sdt=" + sdtName, null);
     SensorDataIndex index;
     if (!response.getStatus().isSuccess()) {
       throw new SensorBaseClientException(response.getStatus());
     }
     try {
       String xmlData = response.getEntity().getText();
       index = makeSensorDataIndex(xmlData);
     }
     catch (Exception e) {
       throw new SensorBaseClientException(response.getStatus(), e);
     }
     return index;
   }
 
   /**
    * Returns an index to SensorData for the specified user of all sensor data that have arrived at
    * the server since the specified start and end times. Uses the LastMod field to determine what
    * data will be retrieved.
    * <p>
    * Note that data could be sent recently with a Timestamp (as opposed to LastMod field) from far
    * back in the past, and the index will include references to such data. This method thus differs
    * from all other SensorDataIndex-returning methods, because the others compare passed timestamp
    * values to the Timestamp associated with the moment at which a sensor data instance is created,
    * not the moment it ends up being received by the server.
    * <p>
    * This method is intended for use by user interface facilities such as the SensorDataViewer that
    * wish to monitor the arrival of data at the SensorBase.
    * 
    * @param email The user email.
    * @param lastModStartTime A timestamp used to determine the start time of data to get.
    * @param lastModEndTime A timestamp used to determine the end time of data to get.
    * @return The SensorDataIndex instance.
    * @throws SensorBaseClientException If the server does not return the Index or returns an index
    *         that cannot be marshalled into Java SensorDataIndex instance.
    */
   public synchronized SensorDataIndex getSensorDataIndexLastMod(String email,
       XMLGregorianCalendar lastModStartTime, XMLGregorianCalendar lastModEndTime)
       throws SensorBaseClientException {
     Response response = makeRequest(Method.GET, sensordataUri + email + "?lastModStartTime="
         + lastModStartTime + "&lastModEndTime=" + lastModEndTime, null);
     SensorDataIndex index;
     if (!response.getStatus().isSuccess()) {
       throw new SensorBaseClientException(response.getStatus());
     }
     try {
       String xmlData = response.getEntity().getText();
       index = makeSensorDataIndex(xmlData);
     }
     catch (Exception e) {
       throw new SensorBaseClientException(response.getStatus(), e);
     }
     return index;
   }
 
   /**
    * Returns the SensorData for this user from this server with the specified timestamp.
    * Uses the cache if enabled.
    * 
    * @param email The user email.
    * @param timestamp The timestamp.
    * @return The SensorData instance.
    * @throws SensorBaseClientException If the server does not return the success code or returns a
    *         String that cannot be marshalled into Java SensorData instance.
    */
   public synchronized SensorData getSensorData(String email, XMLGregorianCalendar timestamp)
       throws SensorBaseClientException {
     SensorData data;
     String uri = sensordataUri + email + "/" + timestamp;
     // Check the cache, and return the sensor data instance from it if available. 
     if (this.isCacheEnabled) {
       data = (SensorData)this.uriCache.get(uri);
       if (data != null) {
         return data;
       }
     }
     // If not in the cache, request it from the sensorbase service.
     Response response = makeRequest(Method.GET, uri, null);
     if (!response.getStatus().isSuccess()) {
       throw new SensorBaseClientException(response.getStatus());
     }
     try {
       String xmlData = response.getEntity().getText();
       data = makeSensorData(xmlData);
       // Add it to the cache if we're using one.
       if (this.isCacheEnabled) {
         this.uriCache.put(uri, data);
       }
     }
     catch (Exception e) {
       throw new SensorBaseClientException(response.getStatus(), e);
     }
     return data;
   }
 
   /**
    * Returns the SensorData for this user from this server given the passed uriString.
    * 
    * @param uriString A URL that should return a SensorData instance in XML format.
    * @return The SensorData instance.
    * @throws SensorBaseClientException If the server does not return the success code or returns a
    *         String that cannot be marshalled into Java SensorData instance.
    */
   public synchronized SensorData getSensorData(String uriString) throws SensorBaseClientException {
     SensorData data;
     // Check the cache, and return the sensor data instance from it if available. 
     if (this.isCacheEnabled) {
       data = (SensorData)this.uriCache.get(uriString);
       if (data != null) {
         return data;
       }
     }
     // Otherwise get it from the sensorbase.
     Response response = makeRequest(Method.GET, uriString, null);
     if (!response.getStatus().isSuccess()) {
       throw new SensorBaseClientException(response.getStatus());
     }
     try {
       String xmlData = response.getEntity().getText();
       data = makeSensorData(xmlData);
       // Add it to the cache if we're using one.
       if (this.isCacheEnabled) {
         this.uriCache.put(uriString, data);
       }
     }
     catch (Exception e) {
       throw new SensorBaseClientException(response.getStatus(), e);
     }
     return data;
   }
 
   /**
    * Returns the named SensorData associated with the SensorDataRef.
    * 
    * @param ref The SensorDataRef instance
    * @return The SensorData instance.
    * @throws SensorBaseClientException If the server does not return the data or returns something
    *         that cannot be marshalled into Java SensorData instance.
    */
   public synchronized SensorData getSensorData(SensorDataRef ref) throws SensorBaseClientException {
     SensorData data;
     String uri = ref.getHref();
     // Check the cache, and return the sensor data instance from it if available. 
     if (this.isCacheEnabled) {
       data = (SensorData)this.uriCache.get(uri);
       if (data != null) {
         return data;
       }
     }
     Response response = getUri(uri);
     if (!response.getStatus().isSuccess()) {
       throw new SensorBaseClientException(response.getStatus());
     }
     try {
       String xmlData = response.getEntity().getText();
       data = makeSensorData(xmlData);
       // Add it to the cache if we're using one.
       if (this.isCacheEnabled) {
         this.uriCache.put(uri, data);
       }
     }
     catch (Exception e) {
       throw new SensorBaseClientException(response.getStatus(), e);
     }
     return data;
   }
 
 
   /**
    * Creates the passed SensorData on the server.
    * 
    * @param data The sensor data to create.
    * @throws SensorBaseClientException If problems occur posting this data.
    */
   public synchronized void putSensorData(SensorData data) throws SensorBaseClientException {
     try {
       String xmlData = makeSensorData(data);
       Representation representation = SensorBaseResource.getStringRepresentation(xmlData);
       String uri = sensordataUri + data.getOwner() + "/" + data.getTimestamp();
       Response response = makeRequest(Method.PUT, uri, representation);
       if (!response.getStatus().isSuccess()) {
         throw new SensorBaseClientException(response.getStatus());
       }
     }
     // Allow SensorBaseClientExceptions to be thrown out of this method.
     catch (SensorBaseClientException f) {
       throw f;
     }
     // All other exceptions are caught and rethrown.
     catch (Exception e) {
       throw new SensorBaseClientException("Error marshalling sensor data", e);
     }
   }
 
   /**
    * Creates the passed batch of SensorData on the server. Assumes that all of them have the same
    * owner field, and that batch is non-empty.
    * 
    * @param data The sensor data batch to create, represented as a SensorDatas instance.
    * @throws SensorBaseClientException If problems occur posting this data.
    */
   public synchronized void putSensorDataBatch(SensorDatas data) throws SensorBaseClientException {
     try {
       String xmlData = makeSensorDatas(data);
       String owner = data.getSensorData().get(0).getOwner();
       Representation representation = SensorBaseResource.getStringRepresentation(xmlData);
       String uri = sensordataUri + owner + "/batch";
       Response response = makeRequest(Method.PUT, uri, representation);
       if (!response.getStatus().isSuccess()) {
         throw new SensorBaseClientException(response.getStatus());
       }
     }
     // Allow SensorBaseClientExceptions to be thrown out of this method.
     catch (SensorBaseClientException f) {
       throw f;
     }
     // All other exceptions are caught and rethrown.
     catch (Exception e) {
       throw new SensorBaseClientException("Error marshalling batch sensor data", e);
     }
   }
 
   /**
    * Ensures that the SensorData instance with the specified user and tstamp is not on the server.
    * Returns success even if the SensorData instance did not exist on the server.
    * 
    * @param email The email of the User.
    * @param timestamp The timestamp of the sensor data.
    * @throws SensorBaseClientException If the server does not indicate success.
    */
   public synchronized void deleteSensorData(String email, XMLGregorianCalendar timestamp)
       throws SensorBaseClientException {
     Response response = makeRequest(Method.DELETE, sensordataUri + email + "/" + timestamp, null);
     if (!response.getStatus().isSuccess()) {
       throw new SensorBaseClientException(response.getStatus());
     }
   }
 
   /**
    * Deletes all sensor data associated with the specified user. 
    * Note that the user must be a test user.
    * Returns success even if the user had no sensor data.  
    * 
    * @param email The email of the User.
    * @throws SensorBaseClientException If the server does not indicate success.
    */
   public synchronized void deleteSensorData(String email) throws SensorBaseClientException {
     Response response = makeRequest(Method.DELETE, sensordataUri + email, null);
     if (!response.getStatus().isSuccess()) {
       throw new SensorBaseClientException(response.getStatus());
     }
   }
 
   /**
    * Returns the index of all Projects from this server. This is an admin-only operation.
    * 
    * @return The ProjectIndex instance.
    * @throws SensorBaseClientException If the server does not return the Index or returns an index
    *         that cannot be marshalled into Java ProjectIndex instance.
    */
   public synchronized ProjectIndex getProjectIndex() throws SensorBaseClientException {
     Response response = makeRequest(Method.GET, projectsUri, null);
     ProjectIndex index;
     if (!response.getStatus().isSuccess()) {
       throw new SensorBaseClientException(response.getStatus());
     }
     try {
       String xmlData = response.getEntity().getText();
       index = makeProjectIndex(xmlData);
     }
     catch (Exception e) {
       throw new SensorBaseClientException(response.getStatus(), e);
     }
     return index;
   }
 
   /**
    * Returns the index of all Projects from this server associated with this user.
    * This includes the projects that this user owns, that this user is a member of,
    * and that this user has been invited to participate in as a member (but has not
    * yet accepted or declined.)
    * 
    * @param email The user email.
    * @return The ProjectIndex instance.
    * @throws SensorBaseClientException If the server does not return the Index or returns an index
    *         that cannot be marshalled into Java ProjectIndex instance.
    */
   public synchronized ProjectIndex getProjectIndex(String email)
       throws SensorBaseClientException {
     Response response = makeRequest(Method.GET, projectsUri + email, null);
     ProjectIndex index;
     if (!response.getStatus().isSuccess()) {
       throw new SensorBaseClientException(response.getStatus());
     }
     try {
       String xmlData = response.getEntity().getText();
       index = makeProjectIndex(xmlData);
     }
     catch (Exception e) {
       throw new SensorBaseClientException(response.getStatus(), e);
     }
     return index;
   }
   
 
   /**
    * Returns the Project from this server.
    * 
    * @param email The user email.
    * @param projectName The project name.
    * @return The Project
    * @throws SensorBaseClientException If the server does not return success or returns something
    *         that cannot be marshalled into Java Project instance.
    */
   public synchronized Project getProject(String email, String projectName)
       throws SensorBaseClientException {
     Response response = makeRequest(Method.GET, projectsUri + email + "/" + projectName, null);
     Project project;
     if (!response.getStatus().isSuccess()) {
       throw new SensorBaseClientException(response.getStatus());
     }
     try {
       String xmlData = response.getEntity().getText();
       project = makeProject(xmlData);
     }
     catch (Exception e) {
       throw new SensorBaseClientException(response.getStatus(), e);
     }
     return project;
   }
   
   /**
    * Invites the user indicated via their email address to the named project owned by this user.
    * Has no effect if the user is already an invited member.
    * Returns the updated project representation. 
    * 
    * @param email The user to be invited to this project.  
    * @param projectName The project name.
    * @return The project representation as a result of the invitation.
    * @throws SensorBaseClientException If the server does not return success.
    */
   public synchronized Project invite(String email, String projectName) 
   throws SensorBaseClientException {
     
     // First, get the project representation.
     Project project = this.getProject(this.userEmail, projectName);
     // Make sure that the Invitations instance is not null.
     if (project.getInvitations() == null) {
       project.setInvitations(new Invitations());
     }
     // If user hasn't already been invited, then invite them.
     if (!project.getInvitations().getInvitation().contains(email)) {
       project.getInvitations().getInvitation().add(email);
       this.putProject(project);
     }
     // Return the updated project representation from the server. 
     return this.getProject(this.userEmail, projectName);
   }
   
   /**
    * Accepts the invitation to be a member of the project owned by owner.
    * 
    * @param owner The owner of the project that this user has been invited into
    * @param projectName the name of the project.
    * @param reply The reply, either ACCEPT or DECLINE.
    * @throws SensorBaseClientException If the server returns an error from this acceptance, for
    * example if the user has not actually been invited.
    */
   public synchronized void reply(String owner, String projectName, InvitationReply reply) 
   throws SensorBaseClientException {
     Response response = makeRequest(Method.POST, 
         projectsUri + owner + "/" + projectName + "/invitation/" + 
         reply.toString().toLowerCase(), null);
     if (!response.getStatus().isSuccess()) {
       throw new SensorBaseClientException(response.getStatus());
     }
   }
   
   
 
   /**
    * Returns the named Project associated with the ProjectRef.
    * 
    * @param ref The ProjectRef instance
    * @return The Project instance.
    * @throws SensorBaseClientException If the server does not return the user or returns something
    *         that cannot be marshalled into Java Project instance.
    */
   public synchronized Project getProject(ProjectRef ref) throws SensorBaseClientException {
     Response response = getUri(ref.getHref());
     Project project;
     if (!response.getStatus().isSuccess()) {
       throw new SensorBaseClientException(response.getStatus());
     }
     try {
       String xmlData = response.getEntity().getText();
       project = makeProject(xmlData);
     }
     catch (Exception e) {
       throw new SensorBaseClientException(response.getStatus(), e);
     }
     return project;
   }
 
   /**
    * Returns a SensorDataIndex representing all the SensorData for this Project.
    * 
    * @param owner The project owner's email.
    * @param projectName The project name.
    * @return A SensorDataIndex.
    * @throws SensorBaseClientException If the server does not return success or returns something
    *         that cannot be marshalled into Java SensorDataIndex instance.
    */
   public synchronized SensorDataIndex getProjectSensorData(String owner, String projectName)
       throws SensorBaseClientException {
     Response response = makeRequest(Method.GET, projectsUri + owner + "/" + projectName
         + "/sensordata", null);
     SensorDataIndex index;
     if (!response.getStatus().isSuccess()) {
       throw new SensorBaseClientException(response.getStatus());
     }
     try {
       String xmlData = response.getEntity().getText();
       index = makeSensorDataIndex(xmlData);
     }
     catch (Exception e) {
       throw new SensorBaseClientException(response.getStatus(), e);
     }
     return index;
   }
   
  
 
   /**
    * Returns a SensorDataIndex representing the SensorData for the Project during the time interval.
    * 
    * @param owner The project owner's email.
    * @param projectName The project name.
    * @param startTime The start time.
    * @param endTime The end time.
    * @return A SensorDataIndex.
    * @throws SensorBaseClientException If the server does not return success or returns something
    *         that cannot be marshalled into Java SensorDataIndex instance.
    */
   public synchronized SensorDataIndex getProjectSensorData(String owner, String projectName,
       XMLGregorianCalendar startTime, XMLGregorianCalendar endTime)
       throws SensorBaseClientException {
     Response response = makeRequest(Method.GET, projectsUri + owner + "/" + projectName
         + "/sensordata?startTime=" + startTime + andEndTime + endTime, null);
     SensorDataIndex index;
     if (!response.getStatus().isSuccess()) {
       throw new SensorBaseClientException(response.getStatus());
     }
     try {
       String xmlData = response.getEntity().getText();
       index = makeSensorDataIndex(xmlData);
     }
     catch (Exception e) {
       throw new SensorBaseClientException(response.getStatus(), e);
     }
     return index;
   }
   
   /**
    * Returns a SensorDataIndex representing the SensorData with the given SDT for the Project 
    * during the time interval.
    * 
    * @param owner The project owner's email.
    * @param projectName The project name.
    * @param startTime The start time.
    * @param endTime The end time.
    * @param sdt The SensorDataType.
    * @return A SensorDataIndex.
    * @throws SensorBaseClientException If the server does not return success or returns something
    *         that cannot be marshalled into Java SensorDataIndex instance.
    */
   public synchronized SensorDataIndex getProjectSensorData(String owner, String projectName,
       XMLGregorianCalendar startTime, XMLGregorianCalendar endTime, String sdt)
       throws SensorBaseClientException {
     Response response = makeRequest(Method.GET, projectsUri + owner + "/" + projectName
         + "/sensordata?sdt=" + sdt + "&startTime=" + startTime + andEndTime + endTime, null);
     SensorDataIndex index;
     if (!response.getStatus().isSuccess()) {
       throw new SensorBaseClientException(response.getStatus());
     }
     try {
       String xmlData = response.getEntity().getText();
       index = makeSensorDataIndex(xmlData);
     }
     catch (Exception e) {
       throw new SensorBaseClientException(response.getStatus(), e);
     }
     return index;
   }
   
   /**
    * Returns a SensorDataIndex representing the SensorData with the given SDT for the Project 
    * during the time interval.
    * 
    * @param owner The project owner's email.
    * @param projectName The project name.
    * @param startTime The start time.
    * @param endTime The end time.
    * @param sdt The SensorDataType.
    * @param tool The tool that generated this sensor data of the given type.
    * @return A SensorDataIndex.
    * @throws SensorBaseClientException If the server does not return success or returns something
    *         that cannot be marshalled into Java SensorDataIndex instance.
    */
   public synchronized SensorDataIndex getProjectSensorData(String owner, String projectName,
       XMLGregorianCalendar startTime, XMLGregorianCalendar endTime, String sdt, String tool)
       throws SensorBaseClientException {
     Response response = makeRequest(Method.GET, projectsUri + owner + "/" + projectName
         + "/sensordata?sdt=" + sdt + "&startTime=" + startTime + andEndTime + endTime +
         "&tool=" + tool, null);
     SensorDataIndex index;
     if (!response.getStatus().isSuccess()) {
       throw new SensorBaseClientException(response.getStatus());
     }
     try {
       String xmlData = response.getEntity().getText();
       index = makeSensorDataIndex(xmlData);
     }
     catch (Exception e) {
       throw new SensorBaseClientException(response.getStatus(), e);
     }
     return index;
   }
   
   /**
    * Returns a SensorDataIndex representing the SensorData with the startIndex and 
    * maxInstances for the Project during the time interval.
    * The startIndex must be non-negative, and is zero-based.  The maxInstances must be non-negative.
    * If startIndex is greater than the number of instances in the time interval, then an 
    * empty SensorDataIndex is returned.  
    * 
    * @param owner The project owner's email.
    * @param projectName The project name.
    * @param startTime The start time.
    * @param endTime The end time.
    * @param startIndex The zero-based index to the first Sensor Data instance to be returned in 
    * the time interval, when the instances are all ordered by timestamp.
    * @param maxInstances The maximum number of instances to return.
    * @return A SensorDataIndex.
    * @throws SensorBaseClientException If the server does not return success or returns something
    *         that cannot be marshalled into Java SensorDataIndex instance.
    */
   public synchronized SensorDataIndex getProjectSensorData(String owner, String projectName,
       XMLGregorianCalendar startTime, XMLGregorianCalendar endTime, int startIndex, 
       int maxInstances) throws SensorBaseClientException {
     Response response = makeRequest(Method.GET, projectsUri + owner + "/" + projectName
         + "/sensordata?startTime=" + startTime + andEndTime + endTime + "&startIndex="
         + startIndex + "&maxInstances=" + maxInstances, null);
     SensorDataIndex index;
     if (!response.getStatus().isSuccess()) {
       throw new SensorBaseClientException(response.getStatus());
     }
     try {
       String xmlData = response.getEntity().getText();
       index = makeSensorDataIndex(xmlData);
     }
     catch (Exception e) {
       throw new SensorBaseClientException(response.getStatus(), e);
     }
     return index;
   }
   
   /**
    * Returns a SensorDataIndex containing a snapshot of the sensor data for the given project and
    * sdt during the specified time interval.  A "snapshot" is the set of sensor data with the most
    * recent runtime value during that time interval.
    * @param owner The owner of the project.
    * @param projectName The project name.
    * @param startTime The start time.
    * @param endTime The end time.
    * @param sdt The sdt of interest for the sensor data.
    * @return The SensorDataIndex containing the "snapshot".
    * @throws SensorBaseClientException If problems occur.
    */
   public synchronized SensorDataIndex getProjectSensorDataSnapshot(String owner, String projectName,
       XMLGregorianCalendar startTime, XMLGregorianCalendar endTime, String sdt) 
   throws SensorBaseClientException {
     Response response = makeRequest(Method.GET, projectsUri + owner + "/" + projectName
         + "/snapshot?startTime=" + startTime + andEndTime + endTime + "&sdt=" + sdt, null);
     SensorDataIndex index;
     if (!response.getStatus().isSuccess()) {
       throw new SensorBaseClientException(response.getStatus());
     }
     try {
       String xmlData = response.getEntity().getText();
       index = makeSensorDataIndex(xmlData);
     }
     catch (Exception e) {
       throw new SensorBaseClientException(response.getStatus(), e);
     }
     return index;
   }
   
   /**
    * Returns a SensorDataIndex containing a snapshot of the sensor data for the given project, sdt
    * and tool during the specified time interval.  A "snapshot" is the set of sensor data with the 
    * most recent runtime value during that time interval.
    * @param owner The owner of the project.
    * @param projectName The project name.
    * @param startTime The start time.
    * @param endTime The end time.
    * @param sdt The sdt of interest for the sensor data.
    * @param tool The tool of interest for the sensor data. 
    * @return The SensorDataIndex containing the "snapshot".
    * @throws SensorBaseClientException If problems occur.
    */
   public synchronized SensorDataIndex getProjectSensorDataSnapshot(String owner, String projectName,
       XMLGregorianCalendar startTime, XMLGregorianCalendar endTime, String sdt, String tool) 
   throws SensorBaseClientException {
     Response response = makeRequest(Method.GET, projectsUri + owner + "/" + projectName
         + "/snapshot?startTime=" + startTime + andEndTime + endTime + "&sdt=" + sdt +
         "&tool=" + tool, null);
     SensorDataIndex index;
     if (!response.getStatus().isSuccess()) {
       throw new SensorBaseClientException(response.getStatus());
     }
     try {
       String xmlData = response.getEntity().getText();
       index = makeSensorDataIndex(xmlData);
     }
     catch (Exception e) {
       throw new SensorBaseClientException(response.getStatus(), e);
     }
     return index;
   }
   
   /**
    * Returns a ProjectSummary representing a summary of the number of sensor data instances of
    * each type for the given interval.
    * @param owner The project owner.
    * @param projectName The project name.
    * @param startTime The start time. 
    * @param endTime The end time.
    * @return A ProjectSummary.
    * @throws SensorBaseClientException If problems occur.
    */
   public synchronized ProjectSummary getProjectSummary(String owner, String projectName, 
       XMLGregorianCalendar startTime, XMLGregorianCalendar endTime) 
   throws  SensorBaseClientException {
     Response response = makeRequest(Method.GET, projectsUri + owner + "/" + projectName 
         + "/summary?startTime=" + startTime + andEndTime + endTime, null);
     
     ProjectSummary summary;
     if (!response.getStatus().isSuccess()) {
       throw new SensorBaseClientException(response.getStatus());
     }
     try {
       String xmlData = response.getEntity().getText();
       summary = makeProjectSummary(xmlData);
     }
     catch (Exception e) {
       throw new SensorBaseClientException(response.getStatus(), e);
     }
     return summary;
   }
 
   /**
    * Creates the passed Project on the server.
    * 
    * @param project The project to create.
    * @throws SensorBaseClientException If problems occur posting this data.
    */
   public synchronized void putProject(Project project) throws SensorBaseClientException {
     try {
       String xmlData = makeProject(project);
       Representation representation = SensorBaseResource.getStringRepresentation(xmlData);
       String uri = projectsUri + project.getOwner() + "/" + project.getName();
       Response response = makeRequest(Method.PUT, uri, representation);
       if (!response.getStatus().isSuccess()) {
         throw new SensorBaseClientException(response.getStatus());
       }
     }
     // Allow SensorBaseClientExceptions to be thrown out of this method.
     catch (SensorBaseClientException f) {
       throw f;
     }
     // All other exceptions are caught and rethrown.
     catch (Exception e) {
       throw new SensorBaseClientException("Error marshalling sensor data", e);
     }
   }
 
   /**
    * Deletes the Project given its owner and projectName.
    * 
    * @param email The email of the project owner.
    * @param projectName The project name.
    * @throws SensorBaseClientException If the server does not indicate success.
    */
   public synchronized void deleteProject(String email, String projectName)
       throws SensorBaseClientException {
     Response response = makeRequest(Method.DELETE, projectsUri + email + "/" + projectName, null);
     if (!response.getStatus().isSuccess()) {
       throw new SensorBaseClientException(response.getStatus());
     }
   }
 
   /**
    * Registers the given user email with the given SensorBase.
    * Timeout is set to 5 seconds. 
    * 
    * @param host The host name, such as "http://localhost:9876/sensorbase".
    * @param email The user email.
    * @throws SensorBaseClientException If problems occur during registration.
    */
   public static void registerUser(String host, String email) throws SensorBaseClientException {
     String registerUri = host.endsWith("/") ? host + "register" : host + "/register";
     Request request = new Request();
     request.setResourceRef(registerUri);
     request.setMethod(Method.POST);
     Form form = new Form();
     form.add("email", email);
     request.setEntity(form.getWebRepresentation());
     Client client = new Client(Protocol.HTTP);
     setClientTimeout(client, 10000);
     Response response = client.handle(request);
     if (!response.getStatus().isSuccess()) {
       throw new SensorBaseClientException(response.getStatus());
     }
   }
   
   /**
    * Sets the lastHostNotAvailable timestamp for the passed host.
    * @param host The host that was determined to be not available.
    */
   private static void setLastHostNotAvailable(String host) {
     lastHostNotAvailable.put(host, (new Date()).getTime());
   }
   
   /**
    * Gets the lastHostNotAvailable timestamp associated with host.
    * Returns 0 if there is no lastHostNotAvailable timestamp.
    * @param host The host whose lastNotAvailable timestamp is to be retrieved.
    * @return The timestamp.
    */
   private static long getLastHostNotAvailable(String host) {
     Long time = lastHostNotAvailable.get(host);
     return (time == null) ? 0 : time;
   }
 
   /**
    * Returns true if the passed host is a SensorBase host.
    * The timeout is set at the default timeout value. 
    * Since checking isHost() when the host is not available is expensive, we cache the timestamp
    * whenever we find the host to be unavailable and if there is another call to isHost() within
    * two seconds, we will immediately return false.  This makes startup of clients like 
    * SensorShell go much faster, since they call isHost() several times during startup. 
    * 
    * @param host The URL of a sensorbase host, such as "http://localhost:9876/sensorbase".
    * @return True if this URL responds as a SensorBase host.
    */
   public static boolean isHost(String host) {
     // We return false immediately if we failed to contact the host within the last two seconds. 
     long currTime = (new Date()).getTime();
     if ((currTime - getLastHostNotAvailable(host)) < 2 * 1000) {
       return false;
     }
 
     // All sensorbase hosts use the HTTP protocol.
     if (!host.startsWith("http://")) {
       setLastHostNotAvailable(host);
       return false;
     }
     // Create the host/register URL.
     try {
       String registerUri = host.endsWith("/") ? host + "ping" : host + "/ping";
       Request request = new Request();
       request.setResourceRef(registerUri);
       request.setMethod(Method.GET);
       Client client = new Client(Protocol.HTTP);
       setClientTimeout(client, getDefaultTimeout());
       Response response = client.handle(request);
       String pingText = response.getEntity().getText();
       boolean isAvailable = (response.getStatus().isSuccess() && "SensorBase".equals(pingText)); 
       if (!isAvailable) {
         setLastHostNotAvailable(host);
       }
       return isAvailable;
     }
     catch (Exception e) {
       setLastHostNotAvailable(host);
       return false;
     }
   }
 
   /**
    * Returns true if the user and password is registered as a user with this host.
    * 
    * @param host The URL of a sensorbase host, such as "http://localhost:9876/sensorbase".
    * @param email The user email.
    * @param password The user password.
    * @return True if this user is registered with this host.
    */
   public static boolean isRegistered(String host, String email, String password) {
     // Make sure the host is OK, which captures bogus hosts like "foo".
     if (!isHost(host)) {
       return false;
     }
     // Now try to authenticate.
     try {
       SensorBaseClient client = new SensorBaseClient(host, email, password);
       client.authenticate();
       return true;
     }
     catch (Exception e) {
       return false;
     }
   }
   
   
 
   /**
    * Throws an unchecked illegal argument exception if the arg is null or empty.
    * 
    * @param arg The String that must be non-null and non-empty.
    */
   private void validateArg(String arg) {
     if ((arg == null) || ("".equals(arg))) {
       throw new IllegalArgumentException(arg + " cannot be null or the empty string.");
     }
   }
 
   /**
    * Does the housekeeping for making HTTP requests to the SensorBase by a test or admin user.
    * 
    * @param method The type of Method.
    * @param requestString A string, such as "users". No preceding slash.
    * @param entity The representation to be sent with the request, or null if not needed.
    * @return The Response instance returned from the server.
    */
   private Response makeRequest(Method method, String requestString, Representation entity) {
     Reference reference = new Reference(this.sensorBaseHost + requestString);
     Request request = (entity == null) ? new Request(method, reference) : new Request(method,
         reference, entity);
     request.getClientInfo().getAcceptedMediaTypes().add(xmlMedia);
     ChallengeResponse authentication = new ChallengeResponse(scheme, this.userEmail, this.password);
     request.setChallengeResponse(authentication);
     if (this.isTraceEnabled) {
       System.out.println("SensorBaseClient Tracing: " + method + " " + reference);
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
    * Takes a String encoding of a SensorDataType in XML format and converts it to an instance.
    * 
    * @param xmlString The XML string representing a SensorDataType
    * @return The corresponding SensorDataType instance.
    * @throws Exception If problems occur during unmarshalling.
    */
   private SensorDataType makeSensorDataType(String xmlString) throws Exception {
     Unmarshaller unmarshaller = sdtJAXB.createUnmarshaller();
     return (SensorDataType) unmarshaller.unmarshal(new StringReader(xmlString));
   }
 
   /**
    * Takes a String encoding of a SensorDataTypeIndex in XML format and converts it to an instance.
    * 
    * @param xmlString The XML string representing a SensorDataTypeIndex.
    * @return The corresponding SensorDataTypeIndex instance.
    * @throws Exception If problems occur during unmarshalling.
    */
   private SensorDataTypeIndex makeSensorDataTypeIndex(String xmlString) throws Exception {
     Unmarshaller unmarshaller = sdtJAXB.createUnmarshaller();
     return (SensorDataTypeIndex) unmarshaller.unmarshal(new StringReader(xmlString));
   }
 
   /**
    * Returns the passed SensorDataType instance as a String encoding of its XML representation.
    * 
    * @param sdt The SensorDataType instance.
    * @return The XML String representation.
    * @throws Exception If problems occur during translation.
    */
   private String makeSensorDataType(SensorDataType sdt) throws Exception {
     Marshaller marshaller = sdtJAXB.createMarshaller();
     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
     dbf.setNamespaceAware(true);
     DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
     Document doc = documentBuilder.newDocument();
     marshaller.marshal(sdt, doc);
     DOMSource domSource = new DOMSource(doc);
     StringWriter writer = new StringWriter();
     StreamResult result = new StreamResult(writer);
     TransformerFactory tf = TransformerFactory.newInstance();
     Transformer transformer = tf.newTransformer();
     transformer.transform(domSource, result);
     String xmlString = writer.toString();
     // Now remove the processing instruction. This approach seems like a total hack.
     xmlString = xmlString.substring(xmlString.indexOf('>') + 1);
     return xmlString;
   }
 
   /**
    * Takes a String encoding of a User in XML format and converts it to an instance.
    * 
    * @param xmlString The XML string representing a User
    * @return The corresponding User instance.
    * @throws Exception If problems occur during unmarshalling.
    */
   private User makeUser(String xmlString) throws Exception {
     Unmarshaller unmarshaller = userJAXB.createUnmarshaller();
     return (User) unmarshaller.unmarshal(new StringReader(xmlString));
   }
 
   /**
    * Takes a String encoding of a UserIndex in XML format and converts it to an instance.
    * 
    * @param xmlString The XML string representing a UserIndex.
    * @return The corresponding UserIndex instance.
    * @throws Exception If problems occur during unmarshalling.
    */
   private UserIndex makeUserIndex(String xmlString) throws Exception {
     Unmarshaller unmarshaller = userJAXB.createUnmarshaller();
     return (UserIndex) unmarshaller.unmarshal(new StringReader(xmlString));
   }
 
   /**
    * Returns the passed Properties instance as a String encoding of its XML representation.
    * 
    * @param properties The Properties instance.
    * @return The XML String representation.
    * @throws Exception If problems occur during translation.
    */
   private String makeProperties(Properties properties) throws Exception {
     Marshaller marshaller = userJAXB.createMarshaller();
     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
     dbf.setNamespaceAware(true);
     DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
     Document doc = documentBuilder.newDocument();
     marshaller.marshal(properties, doc);
     DOMSource domSource = new DOMSource(doc);
     StringWriter writer = new StringWriter();
     StreamResult result = new StreamResult(writer);
     TransformerFactory tf = TransformerFactory.newInstance();
     Transformer transformer = tf.newTransformer();
     transformer.transform(domSource, result);
     String xmlString = writer.toString();
     // Now remove the processing instruction. This approach seems like a total hack.
     xmlString = xmlString.substring(xmlString.indexOf('>') + 1);
     return xmlString;
   }
 
   /**
    * Takes an XML Document representing a SensorDataIndex and converts it to an instance.
    * 
    * @param xmlString The XML string representing a SensorDataIndex.
    * @return The corresponding SensorDataIndex instance.
    * @throws Exception If problems occur during unmarshalling.
    */
   private SensorDataIndex makeSensorDataIndex(String xmlString) throws Exception {
     Unmarshaller unmarshaller = sensordataJAXB.createUnmarshaller();
     return (SensorDataIndex) unmarshaller.unmarshal(new StringReader(xmlString));
   }
   
   /**
    * Takes an XML Document representing a ProjectSummary and converts it to an instance.
    * 
    * @param xmlString The XML string representing a ProjectSummary.
    * @return The corresponding ProjectSummary instance.
    * @throws Exception If problems occur during unmarshalling.
    */
   private ProjectSummary makeProjectSummary(String xmlString) throws Exception {
     Unmarshaller unmarshaller = projectJAXB.createUnmarshaller();
     return (ProjectSummary) unmarshaller.unmarshal(new StringReader(xmlString));
   }
 
   /**
    * Takes a String encoding of a SensorData in XML format and converts it to an instance.
    * 
    * @param xmlString The XML string representing a SensorData.
    * @return The corresponding SensorData instance.
    * @throws Exception If problems occur during unmarshalling.
    */
   private SensorData makeSensorData(String xmlString) throws Exception {
     Unmarshaller unmarshaller = sensordataJAXB.createUnmarshaller();
     return (SensorData) unmarshaller.unmarshal(new StringReader(xmlString));
   }
 
   /**
    * Returns the passed SensorData instance as a String encoding of its XML representation. Final
    * because it's called in constructor.
    * 
    * @param data The SensorData instance.
    * @return The XML String representation.
    * @throws Exception If problems occur during translation.
    */
   private final String makeSensorData(SensorData data) throws Exception {
     Marshaller marshaller = sensordataJAXB.createMarshaller();
     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
     dbf.setNamespaceAware(true);
     DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
     Document doc = documentBuilder.newDocument();
     marshaller.marshal(data, doc);
     DOMSource domSource = new DOMSource(doc);
     StringWriter writer = new StringWriter();
     StreamResult result = new StreamResult(writer);
     TransformerFactory tf = TransformerFactory.newInstance();
     Transformer transformer = tf.newTransformer();
     transformer.transform(domSource, result);
     String xmlString = writer.toString();
     // Now remove the processing instruction. This approach seems like a total hack.
     xmlString = xmlString.substring(xmlString.indexOf('>') + 1);
     return xmlString;
   }
 
   /**
    * Returns the passed SensorDatas instance as a String encoding of its XML representation.
    * 
    * @param data The SensorDatas instance.
    * @return The XML String representation.
    * @throws Exception If problems occur during translation.
    */
   private String makeSensorDatas(SensorDatas data) throws Exception {
     Marshaller marshaller = sensordataJAXB.createMarshaller();
     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
     dbf.setNamespaceAware(true);
     DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
     Document doc = documentBuilder.newDocument();
     marshaller.marshal(data, doc);
     DOMSource domSource = new DOMSource(doc);
     StringWriter writer = new StringWriter();
     StreamResult result = new StreamResult(writer);
     TransformerFactory tf = TransformerFactory.newInstance();
     Transformer transformer = tf.newTransformer();
     transformer.transform(domSource, result);
     String xmlString = writer.toString();
     // Now remove the processing instruction. This approach seems like a total hack.
     xmlString = xmlString.substring(xmlString.indexOf('>') + 1);
     return xmlString;
   }
 
   /**
    * Takes a String encoding of a Project in XML format and converts it to an instance.
    * 
    * @param xmlString The XML string representing a Project
    * @return The corresponding Project instance.
    * @throws Exception If problems occur during unmarshalling.
    */
   private Project makeProject(String xmlString) throws Exception {
     Unmarshaller unmarshaller = projectJAXB.createUnmarshaller();
     return (Project) unmarshaller.unmarshal(new StringReader(xmlString));
   }
 
   /**
    * Takes a String encoding of a ProjectIndex in XML format and converts it to an instance.
    * 
    * @param xmlString The XML string representing a ProjectIndex.
    * @return The corresponding ProjectIndex instance.
    * @throws Exception If problems occur during unmarshalling.
    */
   private ProjectIndex makeProjectIndex(String xmlString) throws Exception {
     Unmarshaller unmarshaller = projectJAXB.createUnmarshaller();
     return (ProjectIndex) unmarshaller.unmarshal(new StringReader(xmlString));
   }
 
   /**
    * Returns the passed Project instance as a String encoding of its XML representation.
    * 
    * @param project The Project instance.
    * @return The XML String representation.
    * @throws Exception If problems occur during translation.
    */
   private String makeProject(Project project) throws Exception {
     Marshaller marshaller = projectJAXB.createMarshaller();
     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
     dbf.setNamespaceAware(true);
     DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
     Document doc = documentBuilder.newDocument();
     marshaller.marshal(project, doc);
     DOMSource domSource = new DOMSource(doc);
     StringWriter writer = new StringWriter();
     StreamResult result = new StreamResult(writer);
     TransformerFactory tf = TransformerFactory.newInstance();
     Transformer transformer = tf.newTransformer();
     transformer.transform(domSource, result);
     String xmlString = writer.toString();
     // Now remove the processing instruction. This approach seems like a total hack.
     xmlString = xmlString.substring(xmlString.indexOf('>') + 1);
     return xmlString;
   }
   
   /**
    * Attempts to set timeout values for the passed client. 
    * @param client The client .
    * @param milliseconds The timeout value. 
    */
   private static void setClientTimeout(Client client, int milliseconds) {
    client.getContext().getParameters().removeAll("connectTimeout");
     client.getContext().getParameters().add("connectTimeout", String.valueOf(milliseconds));
     // For the Apache Commons client.
    client.getContext().getParameters().removeAll("readTimeout");
     client.getContext().getParameters().add("readTimeout", String.valueOf(milliseconds));
    client.getContext().getParameters().removeAll("connectionManagerTimeout");
     client.getContext().getParameters().add("connectionManagerTimeout", 
         String.valueOf(milliseconds));
   }
 
 
   /**
    * Don't use this method.  To be deleted. 
    * @throws UriCacheException If problems.
    * @throws IOException If problems.
    */
   public synchronized void enableCaching() throws UriCacheException, IOException {
     //throw new Exception("This method no longer supported.");
     //this.uriCache = UriCacheManager.getCache(null, this.sensorBaseHost, this.userEmail);
     //this.isCacheEnabled = true;
   }
   
   /**
    * Enables caching in this client.  
    * @param cacheName The name of the cache.
    * @param subDir The subdirectory in which the cache backend store is saved.
    * @param maxLife The default expiration time for objects, in days.
    * @param capacity The maximum number of instances to be held in-memory.
    */
   public synchronized void enableCaching(String cacheName, String subDir, Double maxLife, 
       Long capacity) {
     this.uriCache = new NewUriCache(cacheName, subDir, maxLife, capacity);
     this.isCacheEnabled = true;
   }
  
   /**
    * Delete all entries from this cache. 
    */
   public synchronized void clearCache() {
     this.uriCache.clear();
   }
 
 }
