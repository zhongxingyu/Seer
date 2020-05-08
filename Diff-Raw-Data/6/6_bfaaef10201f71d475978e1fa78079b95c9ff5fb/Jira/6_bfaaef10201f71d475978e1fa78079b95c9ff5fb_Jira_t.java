 package uk.org.sappho.code.change.management.issues;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.sappho.jira.rpc.soap.client.SapphoJiraRpcSoapServiceWrapper;
 
 import com.atlassian.jira.rpc.soap.client.AbstractRemoteConstant;
 import com.atlassian.jira.rpc.soap.client.RemoteComponent;
 import com.atlassian.jira.rpc.soap.client.RemoteIssue;
 import com.atlassian.jira.rpc.soap.client.RemoteVersion;
 import com.google.inject.Inject;
 
 import uk.org.sappho.code.change.management.data.IssueData;
 import uk.org.sappho.code.change.management.data.WarningList;
 import uk.org.sappho.configuration.Configuration;
 import uk.org.sappho.configuration.ConfigurationException;
 import uk.org.sappho.jira4j.soap.JiraSoapService;
 
 public class Jira implements IssueManagement {
 
     private static enum Field {
         type,
         priority,
         status,
         resolution
     };
 
     private final Configuration config;
     private WarningList warnings = null;
     private String jiraURL = null;
     private JiraSoapService jiraSoapService = null;
     private SapphoJiraRpcSoapServiceWrapper sapphoJiraRpcSoapServiceWrapper = null;
     private final Map<Field, Map<String, String>> idNameMappings = new HashMap<Field, Map<String, String>>();
     private final Map<String, IssueData> issueMappings = new HashMap<String, IssueData>();
     private final Map<String, String> movedIssueMappings = new HashMap<String, String>();
     private final Map<String, String> parentIssueMappings = new HashMap<String, String>();
     private final List<String> allRawReleases = new ArrayList<String>();
     private static final Logger log = Logger.getLogger(Jira.class);
 
     @Inject
     public Jira(Configuration config) {
 
         log.info("Using Jira issue management plugin");
         this.config = config;
     }
 
     public void init(WarningList warnings) throws IssueManagementException, ConfigurationException {
 
         this.warnings = warnings;
         jiraURL = config.getProperty("jira.url");
         String username = config.getProperty("jira.username");
         String password = config.getProperty("jira.password");
         log.info("Connecting to " + jiraURL + " as " + username);
         try {
             jiraSoapService = new JiraSoapService(jiraURL, username, password);
             sapphoJiraRpcSoapServiceWrapper = new SapphoJiraRpcSoapServiceWrapper(jiraURL, username, password);
             initIdNameMappings(Field.type, jiraSoapService.getService()
                     .getIssueTypes(jiraSoapService.getToken()));
             initIdNameMappings(Field.priority, jiraSoapService.getService()
                     .getPriorities(jiraSoapService.getToken()));
             initIdNameMappings(Field.status, jiraSoapService.getService()
                     .getStatuses(jiraSoapService.getToken()));
             initIdNameMappings(Field.resolution, jiraSoapService.getService()
                     .getResolutions(jiraSoapService.getToken()));
         } catch (Throwable t) {
             throw new IssueManagementException("Unable to connect to " + jiraURL + " as user " + username
                     + " - is Sappho SOAP service installed?", t);
         }
     }
 
     private void initIdNameMappings(Field field, AbstractRemoteConstant[] constants) {
 
         Map<String, String> mappings = new HashMap<String, String>();
         idNameMappings.put(field, mappings);
         for (AbstractRemoteConstant constant : constants)
             mappings.put(constant.getId(), constant.getName());
     }
 
     private String getRealIssueKey(String issueKey) {
 
         String newIssueKey = movedIssueMappings.get(issueKey);
         if (newIssueKey == null) {
             try {
                 newIssueKey = sapphoJiraRpcSoapServiceWrapper.getMovedIssueKey(issueKey);
                 if (newIssueKey != null)
                     warnings.add("Moved issue", "Issue " + issueKey + " has moved to " + newIssueKey);
             } catch (Exception e) {
                 // if this doesn't work it'll be caught further downstream so can be ignored here
             }
             if (newIssueKey == null)
                 newIssueKey = issueKey;
             movedIssueMappings.put(issueKey, newIssueKey);
         }
         return newIssueKey;
     }
 
     private String getParentIssueKey(String issueKey) {
 
         String newIssueKey = parentIssueMappings.get(issueKey);
         if (newIssueKey == null) {
             try {
                 newIssueKey = sapphoJiraRpcSoapServiceWrapper.getParent(issueKey);
                 if (newIssueKey != null)
                     warnings.add("Subtask", "Issue " + issueKey + " is a subtask of " + newIssueKey);
             } catch (Exception e) {
                 // if this doesn't work it'll be caught further downstream so can be ignored here
             }
             if (newIssueKey == null)
                 newIssueKey = issueKey;
             parentIssueMappings.put(issueKey, newIssueKey);
         }
         return newIssueKey;
     }
 
     public IssueData getIssueData(String issueKey) {
 
         issueKey = getRealIssueKey(issueKey);
         issueKey = getParentIssueKey(issueKey);
         IssueData issueData = issueMappings.get(issueKey);
         if (issueData == null) {
             RemoteIssue remoteIssue = null;
             try {
                 remoteIssue = jiraSoapService.getService().getIssue(jiraSoapService.getToken(), issueKey);
             } catch (Exception e) {
             }
             if (remoteIssue != null) {
                 List<String> issueRawReleases = new ArrayList<String>();
                 RemoteVersion[] fixVersions = remoteIssue.getFixVersions();
                 for (RemoteVersion remoteVersion : fixVersions) {
                     String remoteVersionName = remoteVersion.getName();
                     if (!issueRawReleases.contains(remoteVersionName)) {
                         issueRawReleases.add(remoteVersionName);
                     }
                     if (!allRawReleases.contains(remoteVersionName)) {
                         allRawReleases.add(remoteVersionName);
                     }
                 }
                 String type = idNameMappings.get(Field.type).get(remoteIssue.getType());
                String summary = remoteIssue.getSummary();
                if (summary == null)
                    summary = "";
                 RemoteComponent[] remoteComponents = remoteIssue.getComponents();
                 List<String> components = new ArrayList<String>();
                 for (RemoteComponent remoteComponent : remoteComponents) {
                     components.add(remoteComponent.getName());
                 }
                 String assignee = remoteIssue.getAssignee();
                 String project = remoteIssue.getProject();
                 String priority = idNameMappings.get(Field.priority).get(remoteIssue.getPriority());
                 String resolution = idNameMappings.get(Field.resolution).get(remoteIssue.getResolution());
                 String status = idNameMappings.get(Field.status).get(remoteIssue.getStatus());
                 Date createdOn = remoteIssue.getCreated().getTime();
                 Date updatedOn = remoteIssue.getUpdated().getTime();
                issueData = new IssueData(issueKey, type, summary, createdOn, updatedOn,
                         assignee, project, priority, resolution, status, components, issueRawReleases);
                 issueMappings.put(issueKey, issueData);
             }
         }
         return issueData;
     }
 
     public List<String> getRawReleases() {
 
         return allRawReleases;
     }
 }
