 package uk.org.sappho.code.change.management.engine;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 
 import com.google.inject.AbstractModule;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 
 import uk.org.sappho.code.change.management.data.IssueData;
 import uk.org.sappho.code.change.management.data.RawData;
 import uk.org.sappho.code.change.management.data.RevisionData;
 import uk.org.sappho.code.change.management.data.WarningList;
 import uk.org.sappho.code.change.management.data.persistence.ConfigurationRawDataPersistence;
 import uk.org.sappho.code.change.management.issues.IssueManagement;
 import uk.org.sappho.code.change.management.scm.SCM;
 import uk.org.sappho.code.change.management.scm.SCMException;
 import uk.org.sappho.configuration.Configuration;
 import uk.org.sappho.configuration.ConfigurationException;
 import uk.org.sappho.string.mapping.Mapper;
 
 public class EngineModule extends AbstractModule {
 
     private Configuration config;
     private Injector injector;
     private RawData rawData = new RawData();
     private static final Logger log = Logger.getLogger(EngineModule.class);
 
     public void init(Configuration config) {
 
         this.config = config;
         injector = Guice.createInjector(this);
     }
 
     protected Configuration getConfiguration() {
 
         return config;
     }
 
     protected Injector getInjector() {
 
         return injector;
     }
 
     protected SCM getSCMPlugin() {
 
         return injector.getInstance(SCM.class);
     }
 
     protected IssueManagement getIssueManagementPlugin() {
 
         return injector.getInstance(IssueManagement.class);
     }
 
     protected RawDataProcessing getRawDataProcessingPlugin() {
 
         return injector.getInstance(RawDataProcessing.class);
     }
 
     @Override
     protected void configure() {
 
         try {
             bind(Configuration.class).toInstance(config);
             bind(SCM.class)
                     .to(config.<SCM> getPlugin("scm.plugin", "uk.org.sappho.code.change.management.scm"));
             bind(IssueManagement.class)
                     .to(config.<IssueManagement> getPlugin("issues.plugin",
                             "uk.org.sappho.code.change.management.issues"));
             bind(RawDataProcessing.class).to(
                     config.<RawDataProcessing> getPlugin("raw.data.processing.plugin",
                             "uk.org.sappho.code.heatmap.engine"));
         } catch (ConfigurationException e) {
             throw new RuntimeException(e.getMessage(), e);
         }
     }
 
     public void loadRawData() throws IOException, ConfigurationException {
 
         rawData = new ConfigurationRawDataPersistence(config).load();
     }
 
     public void saveRawData() throws IOException, ConfigurationException {
 
         new ConfigurationRawDataPersistence(config).save(rawData);
     }
 
     public void scanSCM() throws SCMException, ConfigurationException {
 
         getSCMPlugin().scan(rawData);
         refreshRawData();
     }
 
     public void processRawData() throws RawDataProcessingException {
 
         getRawDataProcessingPlugin().run(rawData);
     }
 
     public void refreshRawData() throws ConfigurationException {
 
         log.info("Refreshing issue management data mapping to revisions");
         IssueManagement issueManagement = getIssueManagementPlugin();
         Mapper commitCommentToIssueKeyMapper = (Mapper) config
                 .getGroovyScriptObject("mapper.commit.comment.to.issue.key");
         Mapper releaseMapper = (Mapper) config.getGroovyScriptObject("mapper.raw.release.to.release");
         Mapper issueTypeMapper = (Mapper) config.getGroovyScriptObject("mapper.issue.type");
         // clear out issue data from a previous scan or refresh
         rawData.clearIssueData();
         WarningList warningList = rawData.getWarnings();
         // run through all the stored revisions to pick up fresh linked issue data
         for (String revisionKey : rawData.getRevisionKeys()) {
             RevisionData revisionData = rawData.getRevisionData(revisionKey);
             String commitComment = revisionData.getCommitComment().split("\n")[0];
             String issueKey = commitCommentToIssueKeyMapper.map(commitComment);
             if (issueKey != null) {
                 IssueData issueData = issueManagement.getIssueData(issueKey);
                 if (issueData != null) {
                     rawData.putIssueData(issueData);
                 }
             } else {
                 warningList.add("Issue not found", "Unable to find an issue to match revision " + revisionKey + " \""
                         + commitComment + "\"");
             }
         }
         // re-wire the newly acquired issue data into the revisions
         rawData.reWire(commitCommentToIssueKeyMapper);
         // set up a map of raw release names to meaningful cooked ones
         Map<String, String> releaseMappings = new HashMap<String, String>();
         for (String rawRelease : issueManagement.getRawReleases()) {
             String cookedRelease = releaseMapper.map(rawRelease);
             if (cookedRelease != null) {
                 warningList.add("Release mapping", "Raw release \"" + rawRelease + "\" mapped to \"" + cookedRelease
                         + "\"");
                 releaseMappings.put(rawRelease, cookedRelease);
             } else {
                 warningList.add("Release ignored", "Raw release \"" + rawRelease + "\" will be ignored");
             }
         }
        // run through all the retrieved issues settings mapped types and releases
         for (String issueKey : rawData.getIssueKeys()) {
             IssueData issueData = rawData.getIssueData(issueKey);
             String rawType = issueData.getType();
             String cookedType = issueTypeMapper.map(rawType);
             warningList
                     .add("Issue type mapping", "Raw issue type \"" + rawType + "\" mapped to \"" + cookedType + "\"");
             issueData.setType(cookedType);
             List<String> rawReleases = issueData.getReleases();
             List<String> cookedReleases = new ArrayList<String>();
             String rawReleasesStr = "";
             String cookedReleasesStr = "";
             for (String rawRelease : rawReleases) {
                 rawReleasesStr += " \"" + rawRelease + "\"";
                 String cookedRelease = releaseMappings.get(rawRelease);
                 if (cookedRelease != null) {
                     if (!cookedReleases.contains(cookedRelease)) {
                         cookedReleasesStr += " \"" + cookedRelease + "\"";
                         cookedReleases.add(cookedRelease);
                     }
                 }
             }
             issueData.setReleases(cookedReleases);
             if (rawReleases.size() == 0) {
                 warningList.add("Issue with no releases", "Issue " + issueKey + " has no association to a release");
             } else if (rawReleases.size() != 1) {
                 warningList.add("Issue with multiple releases", "Issue " + issueKey
                         + " is associated with more than one raw release"
                         + rawReleasesStr + " mapping to" + cookedReleasesStr);
             }
         }
     }
 }
