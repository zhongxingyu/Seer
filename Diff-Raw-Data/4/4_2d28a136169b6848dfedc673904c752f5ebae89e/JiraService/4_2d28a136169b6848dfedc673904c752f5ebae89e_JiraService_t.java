 package uk.org.sappho.code.heatmap.issues.jira;
 
 import java.net.URL;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.log4j.Logger;
 
 import com.atlassian.jira.rpc.soap.client.JiraSoapService;
 import com.atlassian.jira.rpc.soap.client.JiraSoapServiceServiceLocator;
 import com.atlassian.jira.rpc.soap.client.RemoteIssue;
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 
 import uk.org.sappho.code.heatmap.config.Configuration;
 import uk.org.sappho.code.heatmap.issues.IssueManagement;
 import uk.org.sappho.code.heatmap.issues.IssueManagementException;
 import uk.org.sappho.code.heatmap.issues.IssueWrapper;
 
 @Singleton
 public class JiraService implements IssueManagement {
 
     protected JiraSoapService jiraSoapService = null;
     protected String JiraSoapServiceToken = null;
     protected Map<String, IssueWrapper> allowedIssues = new HashMap<String, IssueWrapper>();
     protected Map<RemoteIssue, RemoteIssue> subTaskParents = new HashMap<RemoteIssue, RemoteIssue>();
     protected Map<String, Integer> issueTypeWeightMultipliers = new HashMap<String, Integer>();
     protected Configuration config;
     protected static final Pattern SIMPLE_JIRA_REGEX = Pattern.compile("^([A-Z]+-[0-9]+):.*$");
     private static final Logger LOG = Logger.getLogger(JiraService.class);
 
     @Inject
     public JiraService(Configuration config) throws IssueManagementException {
 
         LOG.info("Using Jira issue management plugin");
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
             jiraSoapService = new JiraSoapServiceServiceLocator().getJirasoapserviceV2(new URL(url
                     + "/rpc/soap/jirasoapservice-v2"));
             JiraSoapServiceToken = jiraSoapService.login(username, password);
         } catch (Throwable t) {
             throw new IssueManagementException("Unable to log in to Jira at " + url + " as user " + username, t);
         }
     }
 
     protected void getAllowedIssues() throws IssueManagementException {
 
         LOG.info("Getting list of allowed issues");
         RemoteIssue[] issues;
         try {
            issues = jiraSoapService.getIssuesFromJqlSearch(JiraSoapServiceToken, config
                    .getProperty("jira.filter.issues.allowed"), 1000);
             // map all subtasks back to their parents
             for (RemoteIssue issue : issues) {
                 RemoteIssue[] subTasks = jiraSoapService.getIssuesFromJqlSearch(JiraSoapServiceToken, "parent = "
                         + issue.getKey(),
                         200);
                 for (RemoteIssue subTask : subTasks) {
                     LOG.info("Mapping " + subTask.getKey() + " to parent issue " + issue.getKey());
                     subTaskParents.put(subTask, issue);
                 }
             }
             // create issue wrappers for all allowed root (non-subtask) issues
             for (RemoteIssue issue : issues) {
                 RemoteIssue parent = subTaskParents.get(issue);
                 if (parent == null) {
                     String issueId = issue.getKey();
                     IssueWrapper issueWrapper = createIssueWrapper(issue);
                     allowedIssues.put(issueId, issueWrapper);
                 }
             }
             // map subtask issues to parents
             for (RemoteIssue issue : issues) {
                 RemoteIssue parent = subTaskParents.get(issue);
                 if (parent != null) {
                     String issueId = issue.getKey();
                     String parentId = parent.getKey();
                     IssueWrapper issueWrapper = allowedIssues.get(parentId);
                     if (issueWrapper != null) {
                         allowedIssues.put(issueId, issueWrapper);
                     } else {
                         LOG.info("Mapping " + issueId + " to parent issue " + parentId
                                 + " not actually possible because parent is not allowed by filter");
                     }
                 }
             }
         } catch (Throwable t) {
             throw new IssueManagementException("Unable to get list of allowed issues", t);
         }
     }
 
     protected IssueWrapper createIssueWrapper(RemoteIssue issue) throws IssueManagementException {
 
         IssueWrapper issueWrapper = null;
         String typeId = issue.getType();
         String typeName = config.getProperty("jira.type.map.id." + typeId, "housekeeping");
         Integer weight = issueTypeWeightMultipliers.get(typeName);
         if (weight == null) {
             String typeNameKey = "jira.type.multiplier." + typeName;
             try {
                 weight = Integer.parseInt(config.getProperty(typeNameKey, "0"));
                 LOG.info("Weight of issue type " + typeName + " is " + weight);
             } catch (Throwable t) {
                 throw new IssueManagementException(
                             "Issue type weight configuration \"" + typeNameKey + "\" is invalid", t);
             }
             issueTypeWeightMultipliers.put(typeName, weight);
         }
         issueWrapper = new JiraIssueWrapper(issue, weight);
         return issueWrapper;
     }
 
     protected String getIssueIdFromCommitComment(String commitComment) {
 
         String id = null;
         Matcher matcher = SIMPLE_JIRA_REGEX.matcher(commitComment.split("\n")[0]);
         if (matcher.matches()) {
             id = matcher.group(1);
         } else {
             LOG.debug("No issue ID found in commit comment: " + commitComment);
         }
         return id;
     }
 
     public IssueWrapper getIssue(String commitComment) {
 
         IssueWrapper issue = null;
         String id = getIssueIdFromCommitComment(commitComment);
         if (id != null) {
             issue = allowedIssues.get(id);
         }
         return issue;
     }
 }
