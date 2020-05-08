 package org.hackystat.sensor.ant.issue;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 import javax.xml.datatype.XMLGregorianCalendar;
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Task;
 import org.hackystat.sensorbase.client.SensorBaseClient;
 import org.hackystat.sensorbase.client.SensorBaseClientException;
 import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorData;
 import org.hackystat.sensorbase.resource.sensordata.jaxb.SensorDataRef;
 import org.hackystat.sensorshell.SensorShell;
 import org.hackystat.sensorshell.SensorShellException;
 import org.hackystat.sensorshell.SensorShellProperties;
 import org.hackystat.sensorshell.usermap.SensorShellMap;
 import org.hackystat.sensorshell.usermap.SensorShellMapException;
 import org.hackystat.utilities.tstamp.Tstamp;
 import au.com.bytecode.opencsv.CSVReader;
 
 /**
  * Ant task to retieve the issue changes and send those information to Hackystat
  * server.
  * 
  * @author Shaoxuan Zhang
  *
  */
 public class IssueSensor extends Task {
 
   /** DateFormat for google csv table. */
   public static final DateFormat googleDateFormat = 
     new SimpleDateFormat("MMM dd, yyyy kk:mm:ss", Locale.US);
   
   /** SensorDataType of Issue data. */
   public static final String ISSUE_SENSOR_DATA_TYPE = "Issue";
   private String tool = "GoogleProjectHosting";
   private String projectName;
   private String dataOwnerHackystatAccount = "";
   private String dataOwnerHackystatPassword = "";
   private String hackystatSensorbase = "";
   private boolean isVerbose = false;
   private XMLGregorianCalendar runTimestamp = Tstamp.makeTimestamp();
   
   List<IssueEntry> updatedIssues = new ArrayList<IssueEntry>();
   Map<String, IssueEntry> issues = new HashMap<String, IssueEntry>();
   SensorShellMap shellMap;
   
   /**
    * Prepare sensor shell map for this sensor.
    * @throws SensorShellMapException if error when loading sensor shell map
    */
   public IssueSensor() throws SensorShellMapException {
     //Construct SensorShellMap.
     shellMap = new SensorShellMap(this.tool);
     if (this.isVerbose) {
       System.out.println("Checking for user maps at: " + shellMap.getUserMapFile());
       System.out.println(this.tool + " accounts found: " + shellMap.getToolAccounts(this.tool));
     }
     try {
       shellMap.validateHackystatInfo(this.tool);
     }
     catch (Exception e) {
       System.out.println("Warning: UserMap validation failed: " + e.getMessage());
     }
   }
   
   /**
    * Extracts issue information from feeds, and sends them to the
    * Hackystat server.
    * 
    * @throws BuildException If the task fails.
    */
   @Override
   public void execute() throws BuildException {
     this.validateProperties(); // sanity check.
     if (this.isVerbose) {
       System.out.println("Processing issue updates for project " + this.projectName);
     }  
     
 
     List<String[]> issueTableContents = new ArrayList<String[]>();
     //[1] Extract data from Issue CSV table. 
     if (this.isVerbose) {
       System.out.println("Retrieving issue csv table from " + this.getAllCsvUrl());
     }
     try {
       URL url = new URL(this.getAllCsvUrl());
       URLConnection urlConnection = url.openConnection();
       urlConnection.connect();
       Reader reader = new InputStreamReader(url.openStream());
       CSVReader csvReader = new CSVReader(reader);
       
       //skip the first line, the header.
       String[] line = csvReader.readNext();
       while ((line = csvReader.readNext()) != null && line.length > 1) {
         issueTableContents.add(line);
       }
     }
     catch (MalformedURLException e) {
       throw new BuildException("Source URL malformed.", e);
     }
     catch (IOException e) {
       throw new BuildException("Internet connection error.", e);
     }
 
     this.processGoogleIssueCsvData(issueTableContents);
     
   }
   
   /**
    * Process the issues information from csv table contents.
    * @param issueTableContents the parsed table content, in forms of List of String[].
    */
   protected void processGoogleIssueCsvData(List<String[]> issueTableContents) {
     try {
       runTimestamp = Tstamp.makeTimestamp();
       
       updatedIssues.clear();
 
       SensorShellProperties props = new SensorShellProperties(this.hackystatSensorbase,
           this.dataOwnerHackystatAccount, this.dataOwnerHackystatPassword);
       SensorShell shell = new SensorShell(props, false, this.tool);
       SensorBaseClient sensorBaseClient = new SensorBaseClient(this.hackystatSensorbase,
           this.dataOwnerHackystatAccount, this.dataOwnerHackystatPassword);
       
       // Retrieve sensor data.
       if (this.isVerbose) {
         System.out.println("Retrieve sensordata from " + this.hackystatSensorbase);
       }
       for (SensorDataRef ref : sensorBaseClient.getSensorDataIndex(
           this.dataOwnerHackystatAccount, ISSUE_SENSOR_DATA_TYPE).getSensorDataRef()) {
         SensorData sensorData = sensorBaseClient.getSensorData(ref);
         if (tool.equals(sensorData.getTool())) {
           IssueEntry issue = new IssueEntry(sensorData);
           issues.put(getResourceFromId(String.valueOf(issue.getIssueId())), issue);
         }
       }
 
       if (this.isVerbose) {
         System.out.println(issues.size() + " sensordata found on sensorbase.");
       }
       
       // Put information into sensordata.
       for (String[] line : issueTableContents) {
         try {
           // Retrieve SensorData according to issue id.
           int id = Integer.valueOf(line[0]);
           line[5] = mapToHackystatAccount(line[5]);
           IssueEntry issue = issues.get(getResourceFromId(String.valueOf(id)));
           if (issue == null) {
             issue = new IssueEntry(createSensorData(line));
             if (this.isVerbose) {
               System.out.println("New issue #" + issue.getIssueId() + " found. " + 
                   printStrings(line));
             }
             issue.upToDate(line, runTimestamp, false);
             issues.put(getResourceFromId(String.valueOf(id)), issue);
             updatedIssues.add(issue);
           }
           else {
             if (issue.upToDate(line, runTimestamp, isVerbose)) {
               if (this.isVerbose) {
                 System.out.println("Issue #" + issue.getIssueId() + " updated. ");
               }
               updatedIssues.add(issue);
             }
           }
         }
         catch (Exception e) {
           e.printStackTrace();
         }
       }
       if (this.isVerbose) {
         System.out.println("Found " + issues.size() + " issues, " + 
             updatedIssues.size() + " updated.");
       }
       
       //Put send updated data.
       for (IssueEntry issueEntry : updatedIssues) {
         shell.add(issueEntry.getSensorData());
       }
       shell.send();
       shell.quit();
     }
     catch (SensorBaseClientException e) {
       throw new BuildException("SensorBaseClient error.", e);
     }
     catch (SensorShellMapException e) {
       throw new BuildException("SensorShellMap error.", e);
     }
     catch (SensorShellException e) {
       throw new BuildException("SensorShellException error.", e);
     }
     catch (Exception e) {
       throw new BuildException("Error when constructing issue data.", e);
     }
   }
 
   /**
    * Print the array of String, separated by comma.
    * @param line the array of String
    * @return the string.
    */
   private String printStrings(String[] line) {
     StringBuffer buffer = new StringBuffer();
     for (String string : line) {
       buffer.append(string);
       buffer.append(", ");
     }
     return buffer.toString();
   }
   
   /**
    * add the udpate information to the issue entry's sensordata.
    * @param issue the issue entry.
    * @param updates the IssueUpdates.
    *//*
   private void addIssueUpdates(IssueEntry issue, List<IssueUpdate> updates) {
     boolean modified = false;
     SensorData issueData = issue.getSensorData();
     for (IssueUpdate update : updates) {
       String propertyValue = update.getUpdateValueWithTime();
       if (update.getTimestamp().compare(issueData.getLastMod()) == DatatypeConstants.GREATER) {
         issueData.addProperty(update.getUpdateKey(), propertyValue);
         modified = true;
       }
       else {
         boolean foundDuplicate = false;
         for (Property property : issueData.getProperties().getProperty()) {
           if (property.getKey().equals(update.getUpdateKey()) && property.getValue()
           .equals(propertyValue)) {
             foundDuplicate = true;
             break;
           }
         }
         if (!foundDuplicate) {
           issueData.addProperty(update.getUpdateKey(), propertyValue);
           modified = true;
         }
       }
     }
     if (modified && runTimestamp.compare(issueData.getLastMod()) == DatatypeConstants.GREATER) {
       issueData.setLastMod(runTimestamp);
     }
   }
   */
   /**
    * Create a SensorData according to the given issue table column.
    * @param line The given issue table column contents.
    * @return A SensorData
    * @throws Exception if error occurs.
    */
   private synchronized SensorData createSensorData(String[] line) throws Exception {
     SensorData sensorData = new SensorData();
     sensorData.setOwner(dataOwnerHackystatAccount);
     sensorData.setTool(tool);
     sensorData.setRuntime(runTimestamp);
     sensorData.setTimestamp(Tstamp.makeTimestamp(googleDateFormat.parse(line[6]).getTime()));
     sensorData.setSensorDataType(ISSUE_SENSOR_DATA_TYPE);
     sensorData.setLastMod(runTimestamp);
     sensorData.setResource(this.getResourceFromId(line[0]));
 
     sensorData.addProperty(IssueEntry.ID_PROPERTY_KEY, line[0]);
     
     return sensorData;
   }
 
 
   /**
    * Map the given issue account to hackystat account. 
    * Return the issue account if no mapping found.
    * @param issueAccount the issue account.
    * @return the mapped account.
    * @throws SensorShellMapException if error when mapping.
    */
   private String mapToHackystatAccount(String issueAccount) 
     throws SensorShellMapException {
 
     String hackystatAccount;
     if (shellMap != null && shellMap.hasUserShell(issueAccount)) {
       hackystatAccount = 
         shellMap.getUserShell(issueAccount).getProperties().getSensorBaseUser();
     }
     else {
       hackystatAccount = issueAccount;
     }
     return hackystatAccount;
   }
   
 
   /**
    * Checks and make sure all properties are set up correctly.
    * 
    * @throws BuildException If any error is detected in the property setting.
    */
   protected void validateProperties() throws BuildException {
     if (this.projectName == null || this.projectName.length() == 0) {
       throw new BuildException("Attribute 'projectName' must be set.");
     }
     if (this.dataOwnerHackystatAccount == null || this.dataOwnerHackystatAccount.length() == 0) {
       throw new BuildException("Attribute 'dataOwnerHackystatAccount' must be set.");
     }
     if (this.dataOwnerHackystatPassword == null || this.dataOwnerHackystatPassword.length() == 0) {
       throw new BuildException("Attribute 'dataOwnerHackystatPassword' must be set.");
     }
     if (this.hackystatSensorbase == null || this.hackystatSensorbase.length() == 0) {
       throw new BuildException("Attribute 'hackystatSensorbase' must be set.");
     }
   }
 
   /**
    * Returns the shell associated with the specified author, or null if not found. 
    * The shellCache is
    * used to store SensorShell instances associated with the specified user. The
    * SensorShellMap contains the SensorShell instances built from the
    * UserMap.xml file. This method should be used to retrieve the SensorShell
    * instances to avoid the unnecessary creation of SensorShell instances when
    * sending data for each commit entry. Rather than using a brand new
    * SensorShell instance, this method finds the correct shell in the map,
    * cache, or creates a brand new shell to use.
    * @param shellCache the mapping of author to SensorShell.
    * @param shellMap the mapping of author to SensorShell created by a usermap
    * entry.
    * @param author the author used to retrieve the shell instance.
    * @return the shell instance associated with the author name.
    * @throws SensorShellMapException thrown if there is a problem retrieving the
    * shell instance.
    * @throws SensorShellException thrown if there is a problem retrieving
    * the Hackystat host from the v8.sensor.properties file.
    */
   /*
   private SensorShell getShell(Map<String, SensorShell> shellCache, SensorShellMap shellMap,
       String author) throws SensorShellMapException, SensorShellException {
     if (shellCache.containsKey(author)) {
       return shellCache.get(author); // Returns a cached shell instance.
     }
     else {
       // If the shell user mapping has a shell, add it to the shell cache.
       if (shellMap.hasUserShell(author)) {
         SensorShell shell = shellMap.getUserShell(author);
         shellCache.put(author, shell);
         return shell;
       }
       else { // Create a new shell and add it to the cache.
         if ("".equals(this.defaultHackystatAccount)
             || "".equals(this.defaultHackystatPassword)
             || "".equals(this.defaultHackystatSensorbase)) {
           System.out.println("Warning: A user mapping for the user, " + author
               + " was not found and no default Hackystat account login, password, "
               + "or server was provided. Data ignored.");
           return null;
         }
         SensorShellProperties props = new SensorShellProperties(this.defaultHackystatSensorbase,
             this.defaultHackystatAccount, this.defaultHackystatPassword);
 
         SensorShell shell = new SensorShell(props, false, this.tool);
         shellCache.put(author, shell);
         return shell;
       }
     }
   }*/
 
  /**
   * return resource according to the id.
   * @param id the id.
   * @return the resource.
   */
   private String getResourceFromId(String id) {
     return "http://code.google.com/p/" + projectName + "/issues/detail?id=" + id;
   }
 
   /**
    * @return the csvUrl
    */
   public String getAllCsvUrl() {
     return "http://code.google.com/p/" + projectName + "/issues/csv?can=1&q=&" +
     "colspec=ID%20Type%20Status%20Priority%20Milestone%20Owner%20Opened%20Closed%20Modified";
   }
 
   /**
    * @return the csvUrl
    */
   public String getOpenCsvUrl() {
     return "http://code.google.com/p/" + projectName + "/issues/csv";
   }
   
   /**
    * Sets if verbose mode has been enabled.
    * @param isVerbose true if verbose mode is enabled, false if not.
    */
   public void setVerbose(boolean isVerbose) {
     this.isVerbose = isVerbose;
   }
 
   /**
    * Sets the default Hackystat sensorbase server.
    * @param defaultHackystatSensorbase the default sensorbase server.
    */
   public void setDefaultHackystatSensorbase(String defaultHackystatSensorbase) {
     this.hackystatSensorbase = defaultHackystatSensorbase;
   }
   
   /**
    * @param projectName the projectName to set
    */
   public void setProjectName(String projectName) {
     this.projectName = projectName;
   }
 
   /**
    * @param dataOwner the dataOwner account to set
    */
   public void setDataOwnerHackystatAccount(String dataOwner) {
     this.dataOwnerHackystatAccount = dataOwner;
   }
 
   /**
    * @param password the password to set
    */
   public void setDataOwnerHackystatPassword(String password) {
     this.dataOwnerHackystatPassword = password;
   }
 
 }
