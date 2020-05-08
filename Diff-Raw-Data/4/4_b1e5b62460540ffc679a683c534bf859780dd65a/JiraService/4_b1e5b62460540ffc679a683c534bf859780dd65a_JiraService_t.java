 package uk.org.sappho.code.heatmap.issues.jira;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.log4j.Logger;
 
 import com.atlassian.jira.rpc.soap.client.RemoteIssue;
 import com.atlassian.jira.rpc.soap.client.RemoteVersion;
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 
 import uk.org.sappho.code.heatmap.config.Configuration;
 import uk.org.sappho.code.heatmap.config.ConfigurationException;
 import uk.org.sappho.code.heatmap.issues.IssueManagement;
 import uk.org.sappho.code.heatmap.issues.IssueManagementException;
 import uk.org.sappho.code.heatmap.issues.IssueWrapper;
 import uk.org.sappho.code.heatmap.issues.Releases;
 import uk.org.sappho.code.heatmap.warnings.Warnings;
 import uk.org.sappho.jira4j.soap.JiraSoapService;
 
 @Singleton
 public class JiraService implements IssueManagement {
 
     protected JiraSoapService jiraSoapService = null;
     protected Map<String, IssueWrapper> allowedIssues = new HashMap<String, IssueWrapper>();
     protected Map<String, String> releases = new HashMap<String, String>();
     protected Map<String, String> versionWarnings = new HashMap<String, String>();
     protected Map<String, String> issueTypes = new HashMap<String, String>();
     protected Map<String, Integer> issueTypeWeightMultipliers = new HashMap<String, Integer>();
     protected Warnings warnings;
     protected Configuration config;
     protected static final Pattern SIMPLE_JIRA_REGEX = Pattern.compile("^([a-zA-Z]{2,}-\\d+):.*$");
     private static final Logger LOG = Logger.getLogger(JiraService.class);
     protected static final String ISSUE_FIELDS = "Issue fields";
 
     @Inject
     public JiraService(Warnings warnings, Configuration config) throws IssueManagementException {
 
         LOG.info("Using Jira issue management plugin");
         this.warnings = warnings;
         this.config = config;
         connect();
         getAllowedIssues();
     }
 
     protected void connect() throws IssueManagementException {
 
         String url = config.getProperty("jira.url", "http://example.com");
         String username = config.getProperty("jira.username", "nobody");
         String password = config.getProperty("jira.password", "nopassword");
         LOG.info("Connecting to " + url + " as " + username);
         try {
             jiraSoapService = new JiraSoapService(url, username, password);
         } catch (Throwable t) {
             throw new IssueManagementException("Unable to log in to Jira at " + url + " as user " + username, t);
         }
     }
 
     protected void getAllowedIssues() throws IssueManagementException {
 
         /**
          * note: this is a bit rubbish but because jira's soap interface doesn't have a getParent function it's the only way to fake it
          * making this better will require an installed plugin
          * **/
         LOG.info("Getting list of allowed issues");
         try {
             // get all tasks we're prepared to deal with
             String jql = config.getProperty("jira.filter.issues.allowed");
             LOG.info("Running Jira query: " + jql);
             RemoteIssue[] remoteIssues = jiraSoapService.getService().getIssuesFromJqlSearch(
                     jiraSoapService.getToken(), jql, 5000);
             LOG.info("Processing " + remoteIssues.length + " issues returned by query");
             // map all subtasks back to their parents
             Map<String, RemoteIssue> mappedRemoteIssues = new HashMap<String, RemoteIssue>();
             Map<String, String> subTaskParents = new HashMap<String, String>();
             for (RemoteIssue remoteIssue : remoteIssues) {
                 String issueKey = remoteIssue.getKey();
                 if (mappedRemoteIssues.get(issueKey) == null) {
                     mappedRemoteIssues.put(issueKey, remoteIssue);
                 }
                 RemoteIssue[] subTasks = jiraSoapService.getService().getIssuesFromJqlSearch(
                         jiraSoapService.getToken(), "parent = " + issueKey, 200);
                 for (RemoteIssue subTask : subTasks) {
                     String subTaskKey = subTask.getKey();
                     warnings.add("Issue subtask mapping", subTaskKey + " --> " + issueKey);
                     if (mappedRemoteIssues.get(subTaskKey) == null) {
                         mappedRemoteIssues.put(subTaskKey, subTask);
                     }
                     subTaskParents.put(subTaskKey, issueKey);
                 }
             }
             // create issue wrappers for all allowed root (non-subtask) issues
             for (String issueKey : mappedRemoteIssues.keySet()) {
                 String parentKey = subTaskParents.get(issueKey);
                 IssueWrapper issueWrapper = parentKey != null ?
                         createIssueWrapper(mappedRemoteIssues.get(parentKey), issueKey) :
                         createIssueWrapper(mappedRemoteIssues.get(issueKey), null);
                 allowedIssues.put(issueKey, issueWrapper);
             }
             LOG.info("Processed " + mappedRemoteIssues.size()
                     + " issues - added subtasks might have inflated this figure");
         } catch (Throwable t) {
             throw new IssueManagementException("Unable to get list of allowed issues", t);
         }
     }
 
     protected IssueWrapper createIssueWrapper(RemoteIssue issue, String subTaskKey) throws IssueManagementException {
 
         Releases issueReleases = new Releases();
         Map<String, String> issueReleaseMap = new HashMap<String, String>();
         for (RemoteVersion remoteVersion : issue.getFixVersions()) {
             String remoteVersionName = remoteVersion.getName();
             String versionWarning = versionWarnings.get(remoteVersionName);
             if (versionWarning == null) {
                 versionWarning = config.getProperty("jira.version.status." + remoteVersionName, "");
                 versionWarnings.put(remoteVersionName, versionWarning);
                 if (versionWarning.length() > 0) {
                     warnings.add("Issue version", remoteVersionName + " " + versionWarning);
                 }
             }
             if (versionWarning.length() > 0) {
                 issueReleases.addWarning(remoteVersionName + " " + versionWarning);
             }
             String release = releases.get(remoteVersionName);
             if (release == null) {
                 try {
                     release = config.getProperty("jira.version.map.name." + remoteVersionName);
                 } catch (ConfigurationException e) {
                     release = remoteVersionName;
                 }
                 warnings.add("Version mapping", remoteVersionName + " --> " + release);
                 releases.put(remoteVersionName, release);
             }
             issueReleaseMap.put(release, release);
         }
         for (String release : issueReleaseMap.keySet()) {
             issueReleases.addRelease(release);
         }
         if (issueReleases.getReleases().size() == 0) {
             warnings.add(ISSUE_FIELDS, issue.getKey() + " has no fix version");
         } else if (issueReleases.getReleases().size() > 1) {
             warnings.add(ISSUE_FIELDS, issue.getKey() + " has more than one fix version");
         }
         String typeId = issue.getType();
         String typeName = issueTypes.get(typeId);
         if (typeName == null) {
             typeName = config.getProperty("jira.type.map.id." + typeId, "housekeeping");
             warnings.add("Issue type mapping", typeId + " --> " + typeName);
             issueTypes.put(typeId, typeName);
         }
         Integer weight = issueTypeWeightMultipliers.get(typeName);
         if (weight == null) {
             String typeNameKey = "jira.type.multiplier." + typeName;
             try {
                 weight = Integer.parseInt(config.getProperty(typeNameKey, "0"));
             } catch (Throwable t) {
                 throw new IssueManagementException(
                             "Issue type weight configuration \"" + typeNameKey + "\" is invalid", t);
             }
             warnings.add("Issue type weight", typeName + " = " + weight);
             issueTypeWeightMultipliers.put(typeName, weight);
         }
         return new JiraIssueWrapper(issue, subTaskKey, issueReleases, weight);
     }
 
     protected String getIssueKeyFromCommitComment(String commitComment) {
 
         String key = null;
         Matcher matcher = SIMPLE_JIRA_REGEX.matcher(commitComment.split("\n")[0]);
         if (matcher.matches()) {
             key = matcher.group(1);
         } else {
             warnings.add(ISSUE_FIELDS, "No Jira issue key found in commit comment: " + commitComment);
         }
         return key;
     }
 
     public IssueWrapper getIssue(String commitComment) {
 
         IssueWrapper issue = null;
         String key = getIssueKeyFromCommitComment(commitComment);
         if (key != null) {
             issue = allowedIssues.get(key);
            if (issue == null) {
                warnings.add(ISSUE_FIELDS, "No Jira issue found for " + key
                        + " - query specified in jira.filter.issues.allowed configuration too restrictive?");
            }
         }
         return issue;
     }
 }
