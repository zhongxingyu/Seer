 package pl.edu.icm.oozierunner;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Properties;
 import java.util.regex.Pattern;
 import org.apache.oozie.client.OozieClient;
 import org.apache.oozie.client.OozieClientException;
 import org.apache.oozie.client.WorkflowJob;
 import org.apache.oozie.client.WorkflowJob.Status;
 import org.springframework.util.PropertyPlaceholderHelper;
 
 public class OozieRunner {
 
     protected String oozieServiceURI;
     protected Properties wfProperties;
     protected String envIT;
     protected OozieClient oozie;
 
     public OozieRunner() throws IOException {
         this(OozieRunnerConstants.IT_WF_PROPERTIES_LOCATION);
     }
 
     public OozieRunner(String... propertiesFileLocations) throws IOException {
 
         Properties localProperties;
 
         envIT = System.getProperty("IT.env", "local");
 
         String itEnvPropertiesLocation = OozieRunnerConstants.IT_ENV_PROPERTIES_LOCATION.replaceAll(
                 Pattern.quote(OozieRunnerConstants.IT_ENV_PLACEHOLDER),
                 envIT);
 
         localProperties = loadProperties(propertiesFileLocations);
         addPropertiesFromFile(localProperties, itEnvPropertiesLocation);
         resolvePlaceholders(localProperties);
 
         String appPath = localProperties.getProperty(OozieRunnerConstants.HDFS_WORKING_DIR_URI) + "/" +
                 localProperties.getProperty(OozieRunnerConstants.WORKFLOW_DIR);
         localProperties.setProperty(OozieClient.APP_PATH, appPath);
 
        oozieServiceURI = localProperties.getProperty(OozieRunnerConstants.OOZIE_SERVICE_URI);
        if (oozieServiceURI == null || oozieServiceURI.isEmpty()) {
            throw new OozieRunnerException(OozieRunnerConstants.OOZIE_SERVICE_URI + " cannot be empty");
        }
        oozie = new OozieClient(oozieServiceURI);
         wfProperties = oozie.createConfiguration();
         wfProperties.putAll(localProperties);
     }
 
     public File run() throws IOException {
         OozieRunnerHDFSHelper hdfsHelper = new OozieRunnerHDFSHelper(wfProperties);
         hdfsHelper.copyInputFiles();
         for (String propertyKey : wfProperties.stringPropertyNames()) {
             System.out.println(propertyKey + "="
                     + wfProperties.getProperty(propertyKey));
         }
 
         Status status;
 
         try {
             String jobId = oozie.run(wfProperties);
 
             // wait until the workflow job finishes printing the status every 10 seconds
             while (oozie.getJobInfo(jobId).getStatus() == WorkflowJob.Status.RUNNING) {
                 Thread.sleep(10 * 1000);
             }
 
             // print the final status o the workflow job
             //logger.info("Workflow job completed ...");
             //logger.info(wc.getJobInfo(jobId).toString());
 
             status = oozie.getJobInfo(jobId).getStatus();
 
         } catch (OozieClientException ex) {
             throw new OozieRunnerException(ex);
         } catch (InterruptedException ex) {
             throw new OozieRunnerException(ex);
         }
 
         if (status != WorkflowJob.Status.SUCCEEDED) {
             throw new OozieRunnerException("Workflow finished with status " + status);
         }
 
         return hdfsHelper.getOutputFiles();
     }
 
     protected Properties loadProperties(String... propertiesFileLocations)
             throws IOException {
         Properties properties = new Properties();
         for (String propertiesFileLocation : propertiesFileLocations) {
             addPropertiesFromFile(properties, propertiesFileLocation);
         }
         return properties;
     }
 
     protected void addPropertiesFromFile(Properties properties,
             String propertiesFileLocation) throws IOException {
         Properties propertiesTmp = new Properties();
         propertiesTmp.load(this.getClass().getClassLoader().getResourceAsStream(propertiesFileLocation));
         for (String key : propertiesTmp.stringPropertyNames()) {
             properties.setProperty(key, propertiesTmp.getProperty(key));
         }
     }
 
     protected void resolvePlaceholders(Properties properties) {
         String placeholderPrefix = properties.getProperty(
                 OozieRunnerConstants.PLACEHOLDER_PREFIX_NAME,
                 OozieRunnerConstants.PLACEHOLDER_PREFIX_DEFAULT);
         String placeholderSuffix = properties.getProperty(
                 OozieRunnerConstants.PLACEHOLDER_SUFFIX_NAME,
                 OozieRunnerConstants.PLACEHOLDER_SUFFIX_DEFAULT);
         String placeholderValuesSeparator = properties.getProperty(
                 OozieRunnerConstants.PLACEHOLDER_VALUE_SEPARATOR_NAME,
                 OozieRunnerConstants.PLACEHOLDER_VALUE_SEPARATOR_DEFAULT);
 
         boolean ignoreUnresolvablePlaceholders = Boolean.parseBoolean(properties.getProperty(
                 OozieRunnerConstants.PLACEHOLDER_IGNORE_UNRESOLVABLE_PLACEHOLDERS_NAME,
                 OozieRunnerConstants.PLACEHOLDER_IGNORE_UNRESOLVABLE_PLACEHOLDERS_DEFAULT));
 
         PropertyPlaceholderHelper pph = new PropertyPlaceholderHelper(
                 placeholderPrefix, placeholderSuffix,
                 placeholderValuesSeparator, ignoreUnresolvablePlaceholders);
 
         for (String propertyName : properties.stringPropertyNames()) {
             properties.setProperty(
                     propertyName,
                     pph.replacePlaceholders(
                     properties.getProperty(propertyName), properties));
         }
     }
 }
