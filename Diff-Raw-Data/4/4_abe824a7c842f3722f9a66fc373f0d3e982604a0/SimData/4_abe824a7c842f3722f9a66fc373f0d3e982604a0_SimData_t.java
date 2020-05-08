 package org.hackystat.simdata;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import javax.xml.datatype.XMLGregorianCalendar;
 
 import org.hackystat.sensorbase.client.SensorBaseClient;
 import org.hackystat.sensorbase.client.SensorBaseClientException;
 import org.hackystat.sensorbase.client.SensorBaseClient.InvitationReply;
 import org.hackystat.sensorbase.resource.projects.jaxb.Project;
 import org.hackystat.sensorbase.resource.projects.jaxb.UriPatterns;
 import org.hackystat.sensorbase.resource.sensordata.jaxb.Properties;
 import org.hackystat.sensorbase.resource.sensordata.jaxb.Property;
 import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorData;
 import org.hackystat.sensorshell.SensorShell;
import org.hackystat.sensorshell.SensorShellException;
 import org.hackystat.sensorshell.SensorShellProperties;
 import org.hackystat.simdata.simpletelemetry.SimpleTelemetry;
 import org.hackystat.utilities.logger.HackystatLogger;
 import org.hackystat.utilities.tstamp.Tstamp;
 
 /**
  * Provides utility functions for all SimData scenarios. 
  * Main program takes a host and populates it with all scenarios.
  * @author Philip Johnson
  */
 public class SimData {
   
   /** The host to received this simulated data. */
   private String host;
   
   /** The logger for this simdata run. */
   private Logger logger;
   
   /** The test domain for all users in this simulation. */
   private static final String testdomain = "@hackystat.org";
   
   /** Maps user names to their associated SensorBaseClients. */
   private Map<String, SensorBaseClient> clients = new HashMap<String, SensorBaseClient>();
 
   /** Maps user names to their associated SensorShells. */
   private Map<String, SensorShell> shells = new HashMap<String, SensorShell>();
 
   /** A millisecond offset to guarantee sensor data uniqueness. */
   private int milliseconds = 0;
   
   /**
    * Creates a SimData instance for the given host.
    * @param host The SensorBase host. 
    * @throws Exception If the host cannot be contacted.
    */
   public SimData(String host) throws Exception {
     this.host = host;
     if (!SensorBaseClient.isHost(this.host)) {
       throw new Exception("Could not contact host: " + host);
     }
     this.logger = HackystatLogger.getLogger("org.hackystat.simdata", "simdata");
     
   }
   
   /**
    * Returns the logger for indicating progress of the simulated data generation.
    * @return The logger.
    */
   public Logger getLogger() {
     return this.logger;
   }
   
   /**
    * Returns the test domain used for these users.
    * @return The test domain.
    */
   public static String getTestDomain() {
     return SimData.testdomain;
   }
   
   /**
    * Registers the user with the test domain suffix at the host.
    * Deletes any pre-existing data they might have. 
    * @param userName The user name, with the domain.
    * @throws Exception If problems occur. 
    */
   public void makeUser(String userName) throws Exception {
     String email = userName + testdomain;
     SensorBaseClient.registerUser(this.host, email);
     SensorBaseClient client = new SensorBaseClient(host, email, email);
     client.authenticate();
     client.setTimeout(100000);
     client.deleteSensorData(email); 
     clients.put(userName, client);
     shells.put(userName, makeShell(email));
   }
   
   /**
    * Creates a returns a SensorShell instance for this user. 
    * @param email The user whose shell is to be created. 
    * @return The initialized sensorshell.
    * @throws Exception If problems occur.
    */
   private SensorShell makeShell(String email) throws Exception {
     // The testing configuration of SensorShell is good for SimData.
     SensorShellProperties testProps = SensorShellProperties.getTestInstance(host, email, email);
     //SensorShellProperties shellProps = new SensorShellProperties(host, email, email);
     java.util.Properties props = new java.util.Properties();
     props.setProperty(SensorShellProperties.SENSORSHELL_MULTISHELL_ENABLED_KEY, "true");
     props.setProperty(SensorShellProperties.SENSORSHELL_MULTISHELL_NUMSHELLS_KEY, "2");
     SensorShellProperties shellProps = new SensorShellProperties(testProps, props);
     return new SensorShell(shellProps, false, "SimData");
   }
   
   /**
    * Invokes quit() on all created shells, thus ensuring that any remaining buffered
    * data is sent to the host. 
    */
  public void quitShells() throws SensorShellException {
     for (SensorShell shell : shells.values()) {
       shell.quit();
     }
   }
   
   /**
    * Create a project that will encapsulate this simulation data. 
    * @param projectName The project name.
    * @param user The owner. 
    * @param start The start day for the project.
    * @param end The end day for the project. 
    * @param uriPattern The UriPattern for this project.
    * @throws Exception If problems occur. 
    */
   public void makeProject(String projectName, String user, XMLGregorianCalendar start, 
       XMLGregorianCalendar end, String uriPattern) 
   throws Exception {
     String email = user + testdomain;
     Project project = new Project();
     project.setName(projectName);
     project.setOwner(email);
     project.setDescription("SimData project");
     project.setStartTime(start);
     project.setEndTime(end);
     UriPatterns uriPatterns = new UriPatterns();
     uriPatterns.getUriPattern().add(uriPattern);
     project.setUriPatterns(uriPatterns);
     this.clients.get(user).putProject(project);
   }
   
   /**
    * Adds newMember to projectName owned by owner. 
    * The newMember is invited by the owner, and then newMember accepts. 
    * @param projectName The name of the project.
    * @param owner The owner of the project (without domain name).
    * @param newMember The member to be added (without domain name). 
    * @throws SensorBaseClientException If problems occur during adding. 
    */
   public void addMember(String projectName, String owner, String newMember) 
   throws SensorBaseClientException {
     String ownerEmail = owner + testdomain;
     String newMemberEmail = newMember + testdomain;
     Project project = clients.get(owner).getProject(ownerEmail, projectName);
     project.getInvitations().getInvitation().add(newMemberEmail);
     clients.get(owner).putProject(project);
     clients.get(newMember).reply(ownerEmail, projectName, InvitationReply.ACCEPT);
   }
   
  
   /**
    * Creates and returns a SensorData instance, initialized appropriately.
    * Tstamp is incremented by the internal millisecond counter in order to guarantee
    * uniqueness, and its unincremented form is used for the runtime.  
    * @param user The owner (without the domain.)
    * @param sdt The sensor data type.
    * @param tool The tool name.
    * @param resource The resource.
    * @param tstamp The timestamp.
    * @return The newly created SensorData instance.
    */
   private SensorData makeSensorData(String user, String sdt, String tool, String resource, 
       XMLGregorianCalendar tstamp) {
     return makeSensorData(user, sdt, tool, resource, tstamp, tstamp);
   }
   
   /**
    * Creates and returns a SensorData instance, initialized appropriately.
    * All timestamps are incremented by the internal millisecond counter in order to guarantee
    * uniqueness. 
    * @param user The owner (without the domain.)
    * @param sdt The sensor data type.
    * @param tool The tool name.
    * @param resource The resource.
    * @param tstamp The timestamp.
    * @param runtime The runtime. 
    * @return The newly created SensorData instance.
    */
   private SensorData makeSensorData(String user, String sdt, String tool, String resource, 
       XMLGregorianCalendar tstamp, XMLGregorianCalendar runtime) {
     XMLGregorianCalendar newTstamp = Tstamp.incrementMilliseconds(tstamp, this.milliseconds++);
     String userEmail = user + testdomain;
     SensorData data = new SensorData();
     data.setOwner(userEmail);
     data.setResource(resource);
     data.setRuntime(runtime);
     data.setSensorDataType(sdt);
     data.setTimestamp(newTstamp);
     data.setTool(tool);
     data.setProperties(new Properties());
     return data;
   }
   
   /**
    * Adds the passed key-value pair to the SensorData instance. 
    * @param data The sensor data instance. 
    * @param key The key.
    * @param value The value.
    */
   private void addProperty(SensorData data, String key, String value) {
     Property property = new Property();
     property.setKey(key);
     property.setValue(value);
     data.getProperties().getProperty().add(property);
   }
   
   /**
    * Sends a set of DevEvents to the SensorBase host.
    * @param user The user who will own these DevEvents.
    * @param tstamp The starting timestamp.
    * @param numDevEvents The total number of DevEvents to generate.  Each are five minutes apart.
    * @param file The file to be used as the resource.
    * @throws Exception If problems occur.
    */
   public void addDevEvents(String user, XMLGregorianCalendar tstamp, int numDevEvents, String file) 
   throws Exception {
     for (int i = 0; i < numDevEvents; i++) {
       XMLGregorianCalendar timestamp = Tstamp.incrementMinutes(tstamp, i * 5);
       SensorData data = makeSensorData(user, "DevEvent", "Eclipse", file, timestamp);
       shells.get(user).add(data);
     }
   }
   
   /**
    * Sends a single CodeIssue instance with the specified number of CodeIssues to the SensorBase.
    * @param user The user who will own these CodeIssues.
    * @param tstamp The timestamp.
    * @param file The file to be used as the resource.
    * @param numIssues The total number of CodeIssues in this instance.
    * @throws Exception If problems occur.
    */
   public void addCodeIssues(String user, XMLGregorianCalendar tstamp, String file, int numIssues) 
   throws Exception {
     SensorData data = makeSensorData(user, "CodeIssue", "FindBugs", file, tstamp);
     addProperty(data, "Type_NPE", String.valueOf(numIssues));
     shells.get(user).add(data);
   }
   
   /**
    * Adds a single FileMetric sensor data instance using the SCLC tool.
    * @param user The user who owns this FileMetric.
    * @param tstamp The tstamp (and runtime) for this FileMetric.
    * @param file The resource.
    * @param totalLines The total lines of code. 
    * @param runtime The runtime timestamp, so that multiple FileMetrics will be bundled together in
    * analyses.
    * @throws Exception If problems occur. 
    */
   public void addFileMetric(String user, XMLGregorianCalendar tstamp, String file, int totalLines, 
       XMLGregorianCalendar runtime)
   throws Exception {
     SensorData data = makeSensorData(user, "FileMetric", "SCLC", file, tstamp, runtime);
     addProperty(data, "TotalLines", String.valueOf(totalLines));
     shells.get(user).add(data);
   }
   
   /**
    * Adds a single FileMetric sensor data instance representing Cyclomatic Complexity using the 
    * JavaNCSS tool.
    * @param user The user who owns this FileMetric.
    * @param tstamp The tstamp (and runtime) for this FileMetric.
    * @param file The resource.
    * @param totalLines The total lines of code. 
    * @param runtime The runtime timestamp, so that multiple FileMetrics will be bundled together in
    * analyses.
    * @param complexity A single complexity value for this file. 
    * @throws Exception If problems occur. 
    */
   public void addComplexity(String user, XMLGregorianCalendar tstamp, String file, 
       int totalLines, XMLGregorianCalendar runtime, int complexity)
   throws Exception {
     SensorData data = makeSensorData(user, "FileMetric", "JavaNCSS", file, tstamp, runtime);
     addProperty(data, "TotalLines", String.valueOf(totalLines));
     addProperty(data, "CyclomaticComplexityList", String.valueOf(complexity));
     shells.get(user).add(data);
   }
   
   /**
    * Adds a single Commit sensor data instance with an associated churn value.
    * Pass the target churn as an int, which will be divided between lines added and deleted.
    * @param user The user who owns this Commit.
    * @param tstamp The tstamp (and runtime) for this Commit.
    * @param file The resource.
    * @param churn The total churn (lines added plus deleted).
    * @throws Exception If problems occur. 
    */
   public void addCommit(String user, XMLGregorianCalendar tstamp, String file, int churn) 
   throws Exception {
     int linesAdded = (int)(churn * 0.50);
     int linesDeleted = churn - linesAdded;
     SensorData data = makeSensorData(user, "Commit", "Subversion", file, tstamp);
     addProperty(data, "linesAdded", String.valueOf(linesAdded));
     addProperty(data, "linesDeleted", String.valueOf(linesDeleted));
     shells.get(user).add(data);
   }
   
   /**
    * Adds multiple Commit sensor data instances. The churn is divided among them.
    * Pass the target churn as an int, which will be divided between lines added and deleted.
    * Churn must be more than twice the number of commits.
    * @param user The user who owns this Commit.
    * @param tstamp The tstamp (and runtime) for this Commit.
    * @param file The resource.
    * @param churn The total churn (lines added plus deleted).
    * @param numCommits The number of commit instances to create.
    * @throws Exception If problems occur. 
    */
   public void addCommits(String user, XMLGregorianCalendar tstamp, String file, int churn, 
       int numCommits) throws Exception {
     if (churn <= (numCommits * 2)) {
       throw new Exception("Churn must be more than twice the number of commits.");
     }
     // Churn / Commits is the churn per commit value. Divide that equally among the added/deleted.
     int linesAddedOrDeleted = (int)((churn / (double)numCommits) / 2.0);
     for (int i = 0; i < numCommits; i++) {
       SensorData data = makeSensorData(user, "Commit", "Subversion", file, tstamp);
       addProperty(data, "linesAdded", String.valueOf(linesAddedOrDeleted));
       addProperty(data, "linesDeleted", String.valueOf(linesAddedOrDeleted));
       shells.get(user).add(data);
     }
   }
   
   /**
    * Adds a single Build sensor data instance. 
    * @param user The user who owns this FileMetric.
    * @param tstamp The tstamp (and runtime) for this FileMetric.
    * @param file The resource.
    * @param result The string Success or Failure.
    * @param numBuilds The number of Build instances to create. 
    * @throws Exception If problems occur. 
    */
   public void addBuilds(String user, XMLGregorianCalendar tstamp, String file, String result, 
       int numBuilds) throws Exception {
     for (int i = 0; i < numBuilds; i++) {
       SensorData data = makeSensorData(user, "Build", "Ant", file, tstamp);
       addProperty(data, "Result", result);
       shells.get(user).add(data);
     }
   }
   
   /**
    * Adds a single UnitTest sensor data instance. 
    * @param user The user who owns this UnitTest.
    * @param tstamp The tstamp (and runtime) for this UnitTest.
    * @param file The resource.
    * @param result The string "pass" or "fail"
    * @param numTests The number of test instances to create.
    * @throws Exception If problems occur. 
    */
   public void addUnitTests(String user, XMLGregorianCalendar tstamp, String file, String result, 
       int numTests) throws Exception {
     for (int i = 0; i < numTests; i++) {
       SensorData data = makeSensorData(user, "UnitTest", "JUnit", file, tstamp);
       addProperty(data, "Result", result);
       shells.get(user).add(data);
     }
   }
   
   /**
    * Adds a single Coverage sensor data instance with line-level coverage only.
    * Pass the target percentage coverage and the total lines of code, and this method will
    * determine the number of covered and uncovered lines that provide the desired 
    * line-level coverage (or as close as possible).
    * @param user The user who owns this FileMetric.
    * @param tstamp The tstamp (and runtime) for this FileMetric.
    * @param file The resource.
    * @param percent The target coverage percentage, as a number between 0 and 100.
    * @param loc The total lines of code.  
    * @param runtime The runtime. 
    * @throws Exception If problems occur, or if percent is not between 0 and 100.
    */
   public void addCoverage(String user, XMLGregorianCalendar tstamp, String file, int percent, 
       int loc, XMLGregorianCalendar runtime) throws Exception {
     if ((percent < 0) || (percent > 100)) {
       throw new Exception("Percent must be between 0 and 100");
     }
     int covered = (int)(loc * (percent / 100.0));
     int uncovered = loc - covered;
     SensorData data = makeSensorData(user, "Coverage", "Emma", file, tstamp, runtime);
     addProperty(data, "line_Covered", String.valueOf(covered));
     addProperty(data, "line_Uncovered", String.valueOf(uncovered));
     shells.get(user).add(data);
   }
   
   /**
    * Takes one argument, the SensorBase host, such as "http://localhost:9876/sensorbase".
    * @param args One argument, the SensorBase host.
    * @throws Exception if problems occur.
    */
   public static void main(String[] args) throws Exception {
     if (args.length == 0) {
       System.out.println("SimData takes one argument, the SensorBase host URL.");
       return;
     }
     // Get the host.
     String host = args[0];
     // Create the simple telemetry scenario.
     new SimpleTelemetry(host);
   }
 
 }
