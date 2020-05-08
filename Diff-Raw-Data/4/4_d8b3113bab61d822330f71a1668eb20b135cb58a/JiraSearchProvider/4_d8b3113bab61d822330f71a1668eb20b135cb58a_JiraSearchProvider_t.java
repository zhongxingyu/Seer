 package com.atlassian.sal.jira.search;
 
 import java.io.IOException;
 import java.util.*;
 
 import org.apache.log4j.Logger;
 
 import com.atlassian.jira.exception.DataAccessException;
 import com.atlassian.jira.issue.Issue;
 import com.atlassian.jira.issue.IssueManager;
 import com.atlassian.jira.issue.search.SearchContext;
 import com.atlassian.jira.issue.search.SearchContextImpl;
 import com.atlassian.jira.issue.search.SearchException;
 import com.atlassian.jira.issue.search.SearchProvider;
 import com.atlassian.jira.issue.search.SearchRequest;
 import com.atlassian.jira.issue.search.SearchRequestFactory;
 import com.atlassian.jira.issue.search.searchers.impl.QuerySearcher;
 import com.atlassian.jira.issue.transport.FieldValuesHolder;
 import com.atlassian.jira.issue.transport.impl.FieldValuesHolderImpl;
 import com.atlassian.jira.project.Project;
 import com.atlassian.jira.project.ProjectManager;
 import com.atlassian.jira.security.JiraAuthenticationContext;
 import com.atlassian.jira.util.JiraKeyUtils;
 import com.atlassian.jira.util.searchers.ThreadLocalSearcherCache;
 import com.atlassian.jira.web.bean.PagerFilter;
 import com.atlassian.jira.user.util.UserUtil;
 import com.atlassian.sal.api.ApplicationProperties;
 import com.atlassian.sal.api.message.Message;
 import com.atlassian.sal.api.search.SearchMatch;
 import com.atlassian.sal.api.search.SearchResults;
 import com.atlassian.sal.api.search.parameter.SearchParameter;
 import com.atlassian.sal.api.search.query.SearchQuery;
 import com.atlassian.sal.api.search.query.SearchQueryParser;
 import com.atlassian.sal.core.message.DefaultMessage;
 import com.atlassian.sal.core.search.BasicResourceType;
 import com.atlassian.sal.core.search.BasicSearchMatch;
 import com.opensymphony.user.User;
 
 /**
  *
  */
 @SuppressWarnings("unchecked")
 public class JiraSearchProvider implements com.atlassian.sal.api.search.SearchProvider
 {
     private static final Logger log = Logger.getLogger(JiraSearchProvider.class);
     private final SearchProvider searchProvider;
     private final UserUtil userUtil;
     private final ProjectManager projectManager;
     private final IssueManager issueManager;
     private final SearchRequestFactory searchRequestFactory;
     private final JiraAuthenticationContext authenticationContext;
     private final SearchQueryParser searchQueryParser;
     private final ApplicationProperties applicationProperties;
 
     public JiraSearchProvider(com.atlassian.jira.issue.search.SearchProvider searchProvider,
         UserUtil userUtil, ProjectManager projectManager, IssueManager issueManager,
         SearchRequestFactory searchRequestFactory, JiraAuthenticationContext authenticationContext,
         SearchQueryParser searchQueryParser, ApplicationProperties applicationProperties)
     {
         this.searchProvider = searchProvider;
         this.userUtil = userUtil;
         this.projectManager = projectManager;
         this.issueManager = issueManager;
         this.searchRequestFactory = searchRequestFactory;
         this.authenticationContext = authenticationContext;
         this.searchQueryParser = searchQueryParser;
         this.applicationProperties = applicationProperties;
     }
 
     public SearchResults search(String username, String searchString)
     {
         final SearchQuery searchQuery = searchQueryParser.parse(searchString);
         final int maxHits = searchQuery.getParameter(SearchParameter.MAXHITS, Integer.MAX_VALUE);
 
         final User remoteUser = getUser(username);
         final User oldAuthenticationContextUser = getAuthenticationContextUser();
         try
         {
             setAuthenticationContextUser(remoteUser);
             if (remoteUser == null)
             {
                 log.info("User '" + username + "' not found. Running anonymous search...");
             }
             // See if the search String contains a JIRA issue
             final Collection<Issue> issues = getIssuesFromQuery(searchQuery.getSearchString());
             final SearchRequest searchRequest = createSearchRequest(searchQuery, remoteUser);
             return performSearch(maxHits, searchRequest, issues, remoteUser);
         }
         finally
         {
             // restore original user (who is hopefully null)
             setAuthenticationContextUser(oldAuthenticationContextUser);
         }
     }
 
     private SearchResults performSearch(int maxHits, SearchRequest searchRequest, Collection<Issue> issues,
         User remoteUser)
     {
         try
         {
             final long startTime = System.currentTimeMillis();
             final PagerFilter pagerFilter = new PagerFilter();
 
             pagerFilter.setMax(maxHits);
             final com.atlassian.jira.issue.search.SearchResults searchResults =
                 searchProvider.search(searchRequest, remoteUser, pagerFilter);
             issues.addAll(searchResults.getIssues());
             final int numResults = searchResults.getTotal() - searchResults.getIssues().size() + issues.size();
 
             List<Issue> trimedResults = new ArrayList<Issue>(issues);
             if (trimedResults.size() > maxHits)
                 trimedResults = trimedResults.subList(0, maxHits);
 
             return new SearchResults(transformResults(trimedResults), numResults,
                 System.currentTimeMillis() - startTime);
         }
         catch (final SearchException e)
         {
             log.error("Error executing search", e);
             final ArrayList<Message> errors = new ArrayList<Message>();
             errors.add(new DefaultMessage(e.getMessage()));
             return new SearchResults(errors);
         }
         finally
         {
             try
             {
                 ThreadLocalSearcherCache.resetSearchers();
             }
             catch (final IOException e)
             {
                 log.error("Error closing searchers", e);
             }
         }
     }
 
     private List<SearchMatch> transformResults(final Collection<Issue> issues)
     {
         final List<SearchMatch> matches = new ArrayList<SearchMatch>();
         for (Issue issue : issues)
         {
             matches.add(new BasicSearchMatch(applicationProperties.getBaseUrl() + "/browse/" + issue.getKey(),
                 "[" + issue.getKey() + "] " + issue.getSummary(), issue.getDescription(),
                 new BasicResourceType(applicationProperties, issue.getIssueTypeObject().getId())));
         }
         return matches;
     }
 
     private SearchRequest createSearchRequest(SearchQuery query, User remoteUser)
     {
         FieldValuesHolder holder = new FieldValuesHolderImpl();
         holder.put(QuerySearcher.ID, makeAllTermsCompulsary(query.getSearchString()));
         for (String field : (Iterable<String>) QuerySearcher.QUERY_FIELDS_LIST)
         {
             holder.put(field, Boolean.toString(true));
         }
 
         final String projectKey = query.getParameter(SearchParameter.PROJECT);
         if (projectKey != null)
         {
             final Project project = projectManager.getProjectObjByKey(projectKey);
             if (project != null)
             {
                holder.put("pid", Arrays.asList(project.getId().toString()));
            } 
         }
 
         return searchRequestFactory.create(null, remoteUser, holder, getSearchContext());
     }
 
     private String makeAllTermsCompulsary(String searchString)
     {
         // Split... I don't know if we should do anything like honour quotes
         String[] terms = searchString.trim().split("\\s");
         if (terms.length > 1)
         {
             StringBuilder sb = new StringBuilder();
             String sep = "";
             for (String term : terms)
             {
                 sb.append(sep).append("+").append(term);
                 sep = " ";
             }
             return sb.toString();
         }
         return searchString;
     }
 
     Collection<String> getIssueKeysFromQuery(String query)
     {
         return JiraKeyUtils.getIssueKeysFromString(query);
     }
 
     private Collection<Issue> getIssuesFromQuery(String query)
     {
         final Collection<String> issueKeys = getIssueKeysFromQuery(query);
         // Need to ensure issue order is maintained, while also ensuring uniqueness, hence LinkedHashSet
         final Collection<Issue> issues = new LinkedHashSet<Issue>();
         for (final String issueKey : issueKeys)
         {
             final Issue issue = getIssueByKey(issueKey);
             if (issue != null)
             {
                 issues.add(issue);
             }
         }
         return issues;
     }
 
     private Issue getIssueByKey(String issueKey)
     {
         try
         {
             return issueManager.getIssueObject(issueKey);
         }
         catch (final DataAccessException dae)
         {
             // Not found
             return null;
         }
     }
 
     SearchContext getSearchContext()
     {
         return new SearchContextImpl();
     }
 
     private User getUser(String username)
     {
         return userUtil.getUser(username);
     }
 
     private void setAuthenticationContextUser(final User remoteUser)
     {
         authenticationContext.setUser(remoteUser);
     }
 
     private User getAuthenticationContextUser()
     {
         return authenticationContext.getUser();
     }
 
 }
