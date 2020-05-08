 package org.hackystat.sensorbase.resource.users;
 
 import java.io.File;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.Marshaller;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import org.hackystat.sensorbase.db.DbManager;
 import org.hackystat.utilities.stacktrace.StackTrace;
 import org.hackystat.utilities.tstamp.Tstamp;
 import org.hackystat.sensorbase.resource.projects.ProjectManager;
 import org.hackystat.sensorbase.resource.users.jaxb.Properties;
 import org.hackystat.sensorbase.resource.users.jaxb.Property;
 import org.hackystat.sensorbase.resource.users.jaxb.User;
 import org.hackystat.sensorbase.resource.users.jaxb.UserIndex;
 import org.hackystat.sensorbase.resource.users.jaxb.UserRef;
 import org.hackystat.sensorbase.resource.users.jaxb.Users;
 import org.hackystat.sensorbase.server.Server;
 import static org.hackystat.sensorbase.server.ServerProperties.XML_DIR_KEY;
 import static org.hackystat.sensorbase.server.ServerProperties.TEST_DOMAIN_KEY;
 import static org.hackystat.sensorbase.server.ServerProperties.ADMIN_EMAIL_KEY;
 import static org.hackystat.sensorbase.server.ServerProperties.ADMIN_PASSWORD_KEY;
 import org.w3c.dom.Document;
 
 /**
  * Manages access to the User resources. 
  * Loads default definitions if available. 
  * 
  * Thread Safety Note: This class must NOT invoke any methods from ProjectManager or 
  * SensorDataManager in order to avoid potential deadlock. (ProjectManager and SensorDataManager
  * both invoke methods from UserManager, so if UserManager were to invoke a method from
  * either of these two classes, then we would have multiple locks not being acquired in the
  * same order, which produces the potential for deadlock.)
  *   
  * @author Philip Johnson
  */
 public class UserManager {
   
   /** Holds the class-wide JAXBContext, which is thread-safe. */
   private JAXBContext jaxbContext;
   
  /** The Server associated with this UserManager. */
   Server server; 
   
   /** The DbManager associated with this server. */
   DbManager dbManager;
   
   /** The UserIndex open tag. */
   public static final String userIndexOpenTag = "<UserIndex>";
   
   /** The UserIndex close tag. */
   public static final String userIndexCloseTag = "</UserIndex>";
   
   /** The initial size for Collection instances that hold the Users. */
   private static final int userSetSize = 127;
   
   /** The in-memory repository of Users, keyed by Email. */
   private Map<String, User> email2user = new HashMap<String, User>(userSetSize);
   
   /** The in-memory repository of User XML strings, keyed by User. */
   private Map<User, String> user2xml = new HashMap<User, String>(userSetSize);
   
   /** The in-memory repository of UserRef XML strings, keyed by User. */
   private Map<User, String> user2ref = new HashMap<User, String>(userSetSize);
   
   /** 
    * The constructor for UserManagers. 
    * @param server The Server instance associated with this UserManager. 
    */
   public UserManager(Server server) {
     this.server = server;
     this.dbManager = (DbManager)this.server.getContext().getAttributes().get("DbManager");
     try {
       this.jaxbContext = 
         JAXBContext.newInstance(
             org.hackystat.sensorbase.resource.users.jaxb.ObjectFactory.class);
       loadDefaultUsers(); //NOPMD it's throwing a false warning. 
       initializeCache();  //NOPMD 
       initializeAdminUser(); //NOPMD
     }
     catch (Exception e) {
       String msg = "Exception during UserManager initialization processing";
       server.getLogger().warning(msg + "\n" + StackTrace.toString(e));
       throw new RuntimeException(msg, e);
     }
   }
 
   /**
    * Loads the default Users from the defaults file and adds them to the database. 
    * @throws Exception If problems occur. 
    */
   private final void loadDefaultUsers() throws Exception {
     // Get the default User definitions from the XML defaults file. 
     File defaultsFile = findDefaultsFile();
     // Add these users to the database if we've found a default file. 
     if (defaultsFile.exists()) {
       server.getLogger().info("Loading User defaults from " + defaultsFile.getPath()); 
       Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
       Users users = (Users) unmarshaller.unmarshal(defaultsFile);
       for (User user : users.getUser()) {
         user.setLastMod(Tstamp.makeTimestamp());
         this.dbManager.storeUser(user, this.makeUser(user), this.makeUserRefString(user));
       }
     }
   }
   
   /** Read in all Users from the database and initialize the in-memory cache. */
   private final void initializeCache() {
     try {
       UserIndex index = makeUserIndex(this.dbManager.getUserIndex());
       for (UserRef ref : index.getUserRef()) {
         String email = ref.getEmail();
         String userString = this.dbManager.getUser(email);
         User user = makeUser(userString);
         this.updateCache(user);
       }
     }
     catch (Exception e) {
       server.getLogger().warning("Failed to initialize users " + StackTrace.toString(e));
     }
   }
   
   
   /**
    * Ensures a User exists with the admin role given the data in the sensorbase.properties file. 
    * The admin password will be reset to what was in the sensorbase.properties file. 
    * Note that the "admin" role is managed non-persistently: it is read into the cache from
    * the sensorbase.properties at startup, and any persistently stored values for it are 
    * ignored. This, of course, will eventually cause confusion. 
    * @throws Exception if problems creating the XML string representations of the admin user.  
    */
   private final void initializeAdminUser() throws Exception {
     String adminEmail = server.getServerProperties().get(ADMIN_EMAIL_KEY);
     String adminPassword = server.getServerProperties().get(ADMIN_PASSWORD_KEY);
     // First, clear any existing Admin role property.
     for (User user : this.email2user.values()) {
       user.setRole("basic");
     }
     // Now define the admin user with the admin property.
     if (this.email2user.containsKey(adminEmail)) {
       User user = this.email2user.get(adminEmail);
       user.setPassword(adminPassword);
       user.setRole("admin");
     }
     else {
       User admin = new User();
       admin.setEmail(adminEmail);
       admin.setPassword(adminPassword);
       admin.setRole("admin");
       this.updateCache(admin);
     }
   }
 
   /**
    * Updates the in-memory cache with information about this User. 
    * @param user The user to be added to the cache.
    * @throws Exception If problems occur updating the cache. 
    */
   private final void updateCache(User user) throws Exception {
     if (user.getLastMod() == null) {
       user.setLastMod(Tstamp.makeTimestamp());
     }
     updateCache(user, this.makeUser(user), this.makeUserRefString(user));
   }
   
   /**
    * Updates the cache given all the User representations.
    * @param user The User.
    * @param userXml The User as an XML string. 
    * @param userRef The User as an XML reference. 
    */
   private void updateCache(User user, String userXml, String userRef) {
     this.email2user.put(user.getEmail(), user);
     this.user2xml.put(user, userXml);
     this.user2ref.put(user, userRef);
   }
   
   /**
    * Checks ServerProperties for the XML_DIR property.
   * If this property is null, returns the File for ./xml/defaults/users.defaults.xml.
    * @return The File instance (which might not point to an existing file.)
    */
   private File findDefaultsFile() {
     String defaultsPath = "/defaults/users.defaults.xml";
     String xmlDir = server.getServerProperties().get(XML_DIR_KEY);
     return (xmlDir == null) ?
         new File (System.getProperty("user.dir") + "/xml" + defaultsPath) :
           new File (xmlDir + defaultsPath);
   }
 
   /**
    * Returns the XML string containing the UserIndex with all defined Users.
    * Uses the in-memory cache of UserRef strings.  
    * @return The XML string providing an index to all current Users.
    */
   public synchronized String getUserIndex() {
     StringBuilder builder = new StringBuilder(512);
     builder.append(userIndexOpenTag);
     for (String ref : this.user2ref.values()) {
       builder.append(ref);
     }
     builder.append(userIndexCloseTag);
     return builder.toString();
   }
   
   /**
    * Updates the Manager with this User. Any old definition is overwritten.
    * @param user The User.
    */
   public synchronized void putUser(User user) {
     try {
       user.setLastMod(Tstamp.makeTimestamp());
       String xmlUser =  this.makeUser(user);
       String xmlRef =  this.makeUserRefString(user);
       this.updateCache(user, xmlUser, xmlRef);
       this.dbManager.storeUser(user, xmlUser, xmlRef);
     }
     catch (Exception e) {
       server.getLogger().warning("Failed to put User" + StackTrace.toString(e));
     }
   }
   
 
   /**
    * Ensures that the passed User is no longer present in this Manager, and 
    * deletes all Projects associated with this user. 
    * @param email The email address of the User to remove if currently present.
    */
   public synchronized void deleteUser(String email) {
     User user = this.email2user.get(email);
     // First, delete all the projects owned by this user.
     ProjectManager projectManager =  
       (ProjectManager)this.server.getContext().getAttributes().get("ProjectManager");
     projectManager.deleteProjects(user);
     // Now delete the user
     if (user != null) {
       this.email2user.remove(email);
       this.user2xml.remove(user);
       this.user2ref.remove(user);
     }
     this.dbManager.deleteUser(email);
   }
   
 
   /**
    * Returns the User associated with this email address if they are currently registered, or null
    * if not found.
    * @param email The email address
    * @return The User, or null if not found.
    */
   public synchronized User getUser(String email) {
     return (email == null) ? null : email2user.get(email);
   }
   
   /**
    * Returns the User Xml String associated with this email address if they are registered, or 
    * null if user not found.
    * @param email The email address
    * @return The User XML string, or null if not found.
    */
   public synchronized String getUserString(String email) {
     User user = email2user.get(email);
     return (user == null) ? null : user2xml.get(user);
   }
   
   /**
    * Updates the given User with the passed Properties. 
    * @param user The User whose properties are to be updated.
    * @param properties The Properties. 
    */
   public synchronized void updateProperties(User user, Properties properties) {
     for (Property property : properties.getProperty()) {
       user.getProperties().getProperty().add(property);
     }
     this.putUser(user);
   }
   
   /**
    * Returns a set containing the current User instances. 
    * For thread safety, a fresh Set of Users is built each time this is called. 
    * @return A Set containing the current Users. 
    */
   public synchronized Set<User> getUsers() {
     Set<User> userSet = new HashSet<User>(userSetSize); 
     userSet.addAll(this.email2user.values());
     return userSet;
   }
   
   /**
    * Returns true if the User as identified by their email address is known to this Manager.
    * @param email The email address of the User of interest.
    * @return True if found in this Manager.
    */
   public synchronized boolean isUser(String email) {
     return (email != null) && email2user.containsKey(email);
   }
   
   /**
    * Returns true if the User as identified by their email address and password
    * is known to this Manager.
    * @param email The email address of the User of interest.
    * @param password The password of this user.
    * @return True if found in this Manager.
    */
   public synchronized boolean isUser(String email, String password) {
     User user = this.email2user.get(email);
     return (user != null) && (password != null) && (password.equals(user.getPassword()));
   }
   
   /**
    * Returns true if email is a defined User with Admin privileges. 
    * @param email An email address. 
    * @return True if email is a User with Admin privileges. 
    */
   public synchronized boolean isAdmin(String email) {
     return (email != null) &&
            email2user.containsKey(email) && 
            email.equals(server.getServerProperties().get(ADMIN_EMAIL_KEY));
   }
   
   /**
    * Returns true if the passed user is a test user.
    * This is defined as a User whose email address uses the TEST_DOMAIN.  
    * @param user The user. 
    * @return True if the user is a test user. 
    */
   public synchronized boolean isTestUser(User user) {
     return user.getEmail().endsWith(server.getServerProperties().get(TEST_DOMAIN_KEY));
   }
   
   /** 
    * Registers a User, given their email address.
    * If a User with the passed email address exists, then return the previously registered User.
    * Otherwise create a new User and return it.
    * If the email address ends with the test domain, then the password will be the email.
    * Otherwise, a unique, randomly generated 12 character key is generated as the password. 
    * Defines the Default Project for each new user. 
    * @param email The email address for the user. 
    * @return The retrieved or newly created User.
    */
   public synchronized User registerUser(String email) {
     // registering happens rarely, so we'll just iterate through the userMap.
     for (User user : this.email2user.values()) {
       if (user.getEmail().equals(email)) {
         return user;
       }
     }
     // if we got here, we need to create a new User.
     User user = new User();
     user.setEmail(email);
     user.setProperties(new Properties());
     // Password is either their Email in the case of a test user, or the randomly generated string.
     String password = 
       email.endsWith(server.getServerProperties().get(TEST_DOMAIN_KEY)) ? 
           email : PasswordGenerator.make();
     user.setPassword(password);
     this.putUser(user);
     return user;
   } 
   
   /**
    * Takes a String encoding of a Properties in XML format and converts it to an instance. 
    * 
    * @param xmlString The XML string representing a Properties.
    * @return The corresponding Properties instance. 
    * @throws Exception If problems occur during unmarshalling.
    */
   public final synchronized Properties makeProperties(String xmlString) throws Exception {
     Unmarshaller unmarshaller = this.jaxbContext.createUnmarshaller();
     return (Properties)unmarshaller.unmarshal(new StringReader(xmlString));
   }
   
   /**
    * Takes a String encoding of a User in XML format and converts it to an instance. 
    * 
    * @param xmlString The XML string representing a User
    * @return The corresponding User instance. 
    * @throws Exception If problems occur during unmarshalling.
    */
   public final synchronized User makeUser(String xmlString) throws Exception {
     Unmarshaller unmarshaller = this.jaxbContext.createUnmarshaller();
     return (User)unmarshaller.unmarshal(new StringReader(xmlString));
   }
   
   /**
    * Takes a String encoding of a UserIndex in XML format and converts it to an instance. 
    * 
    * @param xmlString The XML string representing a UserIndex.
    * @return The corresponding UserIndex instance. 
    * @throws Exception If problems occur during unmarshalling.
    */
   public final synchronized UserIndex makeUserIndex(String xmlString) 
   throws Exception {
     Unmarshaller unmarshaller = this.jaxbContext.createUnmarshaller();
     return (UserIndex)unmarshaller.unmarshal(new StringReader(xmlString));
   }
   
   /**
    * Returns the passed User instance as a String encoding of its XML representation.
    * Final because it's called in constructor.
    * @param user The User instance. 
    * @return The XML String representation.
    * @throws Exception If problems occur during translation. 
    */
   public final synchronized String makeUser (User user) throws Exception {
     Marshaller marshaller = jaxbContext.createMarshaller(); 
     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
     dbf.setNamespaceAware(true);
     DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
     Document doc = documentBuilder.newDocument();
     marshaller.marshal(user, doc);
     DOMSource domSource = new DOMSource(doc);
     StringWriter writer = new StringWriter();
     StreamResult result = new StreamResult(writer);
     TransformerFactory tf = TransformerFactory.newInstance();
     Transformer transformer = tf.newTransformer();
     transformer.transform(domSource, result);
     String xmlString = writer.toString();
     // Now remove the processing instruction.  This approach seems like a total hack.
     xmlString = xmlString.substring(xmlString.indexOf('>') + 1);
     return xmlString;
   }
   
   /**
    * Returns the passed Properties instance as a String encoding of its XML representation.
    * @param properties The Properties instance. 
    * @return The XML String representation.
    * @throws Exception If problems occur during translation. 
    */
   public synchronized String makeProperties (Properties properties) throws Exception {
     Marshaller marshaller = jaxbContext.createMarshaller(); 
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
     // Now remove the processing instruction.  This approach seems like a total hack.
     xmlString = xmlString.substring(xmlString.indexOf('>') + 1);
     return xmlString;
   }
 
   /**
    * Returns the passed User instance as a String encoding of its XML representation 
    * as a UserRef object.
    * Final because it's called in constructor.
    * @param user The User instance. 
    * @return The XML String representation of it as a UserRef
    * @throws Exception If problems occur during translation. 
    */
   public final synchronized String makeUserRefString (User user) 
   throws Exception {
     UserRef ref = makeUserRef(user);
     Marshaller marshaller = jaxbContext.createMarshaller(); 
     DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
     dbf.setNamespaceAware(true);
     DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
     Document doc = documentBuilder.newDocument();
     marshaller.marshal(ref, doc);
     DOMSource domSource = new DOMSource(doc);
     StringWriter writer = new StringWriter();
     StreamResult result = new StreamResult(writer);
     TransformerFactory tf = TransformerFactory.newInstance();
     Transformer transformer = tf.newTransformer();
     transformer.transform(domSource, result);
     String xmlString = writer.toString();
     // Now remove the processing instruction.  This approach seems like a total hack.
     xmlString = xmlString.substring(xmlString.indexOf('>') + 1);
     return xmlString;
   }
   
   /**
    * Returns a UserRef instance constructed from a User instance.
    * @param user The User instance. 
    * @return A UserRef instance. 
    */
   public synchronized UserRef makeUserRef(User user) {
     UserRef ref = new UserRef();
     ref.setEmail(user.getEmail());
     ref.setHref(this.server.getHostName() + "users/" + user.getEmail()); 
     return ref;
   }
   
 }
 
