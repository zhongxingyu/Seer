 package com.atlassian.sal.jira.search;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import junit.framework.TestCase;
 
 import org.easymock.EasyMock;
 import org.easymock.MockControl;
 import org.easymock.classextension.MockClassControl;
 
 import com.atlassian.jira.issue.Issue;
 import com.atlassian.jira.issue.issuetype.IssueType;
 import com.atlassian.jira.issue.search.*;
 import com.atlassian.jira.issue.search.util.QueryCreator;
 import com.atlassian.jira.issue.transport.FieldValuesHolder;
 import com.atlassian.jira.issue.transport.impl.FieldValuesHolderImpl;
 import com.atlassian.jira.issue.transport.impl.IssueNavigatorActionParams;
 import com.atlassian.jira.security.JiraAuthenticationContext;
 import com.atlassian.jira.util.ErrorCollection;
 import com.atlassian.jira.web.bean.PagerFilter;
 import com.atlassian.sal.api.ApplicationProperties;
 import com.atlassian.sal.api.search.SearchMatch;
 import com.atlassian.sal.api.user.UserManager;
 import com.atlassian.sal.core.search.query.DefaultSearchQueryParser;
 import com.atlassian.sal.core.component.MockComponentLocator;
 import com.opensymphony.user.User;
 
 /**
  *
  */
 public class TestJiraSearchProvider extends TestCase
 {
 	@Override
 	protected void setUp() throws Exception
 	{
 		super.setUp();
 		MockComponentLocator.create(new DefaultSearchQueryParser(), EasyMock.createNiceMock(UserManager.class));
 	}
     public void testNoResults() throws SearchException
     {
         MockControl mockSearchRequestControl = MockClassControl.createControl(SearchRequest.class);
         SearchRequest mockSearchRequest = (SearchRequest) mockSearchRequestControl.getMock();
         mockSearchRequestControl.replay();
 
         FieldValuesHolder fieldValuesHolder = new FieldValuesHolderImpl();
         fieldValuesHolder.put("query", "query");
 
         MockControl mockJiraAuthenticationContextControl = MockControl.createControl(JiraAuthenticationContext.class);
         JiraAuthenticationContext mockJiraAuthenticationContext = (JiraAuthenticationContext) mockJiraAuthenticationContextControl.getMock();
         mockJiraAuthenticationContext.getUser();
         mockJiraAuthenticationContextControl.setDefaultReturnValue(null);
         mockJiraAuthenticationContextControl.replay();
 
         MockControl mockSearchProviderControl = MockControl.createControl(com.atlassian.jira.issue.search.SearchProvider.class);
         com.atlassian.jira.issue.search.SearchProvider mockSearchProvider = (com.atlassian.jira.issue.search.SearchProvider) mockSearchProviderControl.getMock();
         mockSearchProvider.search(mockSearchRequest, null, PagerFilter.getUnlimitedFilter());
         mockSearchProviderControl.setDefaultReturnValue(new SearchResults(Collections.EMPTY_LIST, 0, PagerFilter.getUnlimitedFilter()));
         mockSearchProviderControl.replay();
 
         MockControl mockQueryCreatorControl = MockClassControl.createControl(QueryCreator.class);
         QueryCreator mockQueryCreator = (QueryCreator) mockQueryCreatorControl.getMock();
         mockQueryCreator.createQuery("query");
         mockQueryCreatorControl.setDefaultReturnValue("?query=query&summary=true");
         mockQueryCreatorControl.replay();
 
         MockControl mockSearchRequestFactoryControl = MockControl.createControl(SearchRequestFactory.class);
         SearchRequestFactory mockSearchRequestFactory = (SearchRequestFactory) mockSearchRequestFactoryControl.getMock();
         mockSearchRequestFactory.create(null, null, fieldValuesHolder, null);
         mockSearchRequestFactoryControl.setDefaultReturnValue(mockSearchRequest);
         mockSearchRequestFactoryControl.replay();
 
         MockControl mockSearchRequestManagerControl = MockControl.createControl(SearchRequestManager.class);
         SearchRequestManager mockSearchRequestManager = (SearchRequestManager) mockSearchRequestManagerControl.getMock();
         mockSearchRequestManager.create(mockSearchRequest);
         mockSearchRequestManagerControl.setDefaultReturnValue(mockSearchRequest);
         mockSearchRequestManagerControl.replay();
 
        JiraSearchProvider searchProvider = new JiraSearchProvider(null, mockQueryCreator, mockSearchRequestManager, mockSearchProvider, null, null, null, mockSearchRequestFactory)
         {
             void populateAndValidate(IssueNavigatorActionParams actionParams, FieldValuesHolder fieldValuesHolder, ErrorCollection errors, User remoteUser)
             {
                 fieldValuesHolder.put("query", "query");
             }
 
             SearchContext getSearchContext()
             {
                 return null;
             }
 
             ApplicationProperties getWebProperties()
             {
                 return null;
             }
 
             User getUser(String username)
             {
                 return null;
             }
         };
 
         com.atlassian.sal.api.search.SearchResults results = searchProvider.search(null, "query");
         assertNotNull(results);
         assertEquals(0, results.getErrors().size());
         assertEquals(0, results.getMatches().size());
 
         mockJiraAuthenticationContextControl.verify();
         mockQueryCreatorControl.verify();
         mockSearchProviderControl.verify();
         mockSearchRequestControl.verify();
         mockSearchRequestManagerControl.verify();
     }
 
     public void testErrors() throws SearchException
     {
         MockControl mockJiraAuthenticationContextControl = MockControl.createControl(JiraAuthenticationContext.class);
         JiraAuthenticationContext mockJiraAuthenticationContext = (JiraAuthenticationContext) mockJiraAuthenticationContextControl.getMock();
         mockJiraAuthenticationContext.getUser();
         mockJiraAuthenticationContextControl.setDefaultReturnValue(null);
         mockJiraAuthenticationContextControl.replay();
 
         MockControl mockQueryCreatorControl = MockClassControl.createControl(QueryCreator.class);
         QueryCreator mockQueryCreator = (QueryCreator) mockQueryCreatorControl.getMock();
         mockQueryCreator.createQuery("query");
         mockQueryCreatorControl.setDefaultReturnValue("?query=query&summary=true");
         mockQueryCreatorControl.replay();
 
        JiraSearchProvider searchProvider = new JiraSearchProvider(null, mockQueryCreator, null, null, null, null, null, null)
         {
             void populateAndValidate(IssueNavigatorActionParams actionParams, FieldValuesHolder fieldValuesHolder, ErrorCollection errors, User user)
             {
                 errors.addError("query", "invalid query string provided");
             }
 
             SearchContext getSearchContext()
             {
                 return null;
             }
 
             ApplicationProperties getWebProperties()
             {
                 return null;
             }
 
 
             User getUser(String username)
             {
                 return null;
             }
         };
 
         com.atlassian.sal.api.search.SearchResults results = searchProvider.search(null, "badquery");
         assertNotNull(results);
         assertEquals(1, results.getErrors().size());
         assertEquals("invalid query string provided", results.getErrors().get(0).getKey());
         assertEquals(0, results.getMatches().size());
 
         mockJiraAuthenticationContextControl.verify();
         mockQueryCreatorControl.verify();
     }
 
     public void testResults() throws SearchException
     {
         MockControl mockSearchRequestControl = MockClassControl.createControl(SearchRequest.class);
         SearchRequest mockSearchRequest = (SearchRequest) mockSearchRequestControl.getMock();
         mockSearchRequestControl.replay();
 
         FieldValuesHolder fieldValuesHolder = new FieldValuesHolderImpl();
         fieldValuesHolder.put("query", "query");
 
         MockControl mockJiraAuthenticationContextControl = MockControl.createControl(JiraAuthenticationContext.class);
         JiraAuthenticationContext mockJiraAuthenticationContext = (JiraAuthenticationContext) mockJiraAuthenticationContextControl.getMock();
         mockJiraAuthenticationContext.getUser();
         mockJiraAuthenticationContextControl.setDefaultReturnValue(null);
         mockJiraAuthenticationContextControl.replay();
 
         MockControl mockIssueTypeControl = MockControl.createControl(IssueType.class);
         IssueType mockIssueType = (IssueType) mockIssueTypeControl.getMock();
         mockIssueType.getId();
         mockIssueTypeControl.setReturnValue("1");
         mockIssueTypeControl.replay();
 
         MockControl mockIssueControl = MockControl.createControl(Issue.class);
         Issue mockIssue = (Issue) mockIssueControl.getMock();
         mockIssue.getKey();
         mockIssueControl.setDefaultReturnValue("JST-234");
         mockIssue.getSummary();
         mockIssueControl.setReturnValue("Sample Summary");
         mockIssue.getDescription();
         mockIssueControl.setReturnValue("Sample description for the query issue.");
         mockIssue.getIssueTypeObject();
         mockIssueControl.setReturnValue(mockIssueType);
         mockIssueControl.replay();
 
         List issues = new ArrayList();
         issues.add(mockIssue);
 
         MockControl mockSearchProviderControl = MockControl.createControl(com.atlassian.jira.issue.search.SearchProvider.class);
         com.atlassian.jira.issue.search.SearchProvider mockSearchProvider = (com.atlassian.jira.issue.search.SearchProvider) mockSearchProviderControl.getMock();
         mockSearchProvider.search(mockSearchRequest, null, PagerFilter.getUnlimitedFilter());
         mockSearchProviderControl.setDefaultReturnValue(new SearchResults(issues, 1, PagerFilter.getUnlimitedFilter()));
         mockSearchProviderControl.replay();
 
         MockControl mockQueryCreatorControl = MockClassControl.createControl(QueryCreator.class);
         QueryCreator mockQueryCreator = (QueryCreator) mockQueryCreatorControl.getMock();
         mockQueryCreator.createQuery("query");
         mockQueryCreatorControl.setDefaultReturnValue("?query=query&summary=true");
         mockQueryCreatorControl.replay();
 
         MockControl mockSearchRequestFactoryControl = MockControl.createControl(SearchRequestFactory.class);
         SearchRequestFactory mockSearchRequestFactory = (SearchRequestFactory) mockSearchRequestFactoryControl.getMock();
         mockSearchRequestFactory.create(null, null, fieldValuesHolder, null);
         mockSearchRequestFactoryControl.setDefaultReturnValue(mockSearchRequest);
         mockSearchRequestFactoryControl.replay();
 
         MockControl mockSearchRequestManagerControl = MockControl.createControl(SearchRequestManager.class);
         SearchRequestManager mockSearchRequestManager = (SearchRequestManager) mockSearchRequestManagerControl.getMock();
         mockSearchRequestManager.create(mockSearchRequest);
         mockSearchRequestManagerControl.setDefaultReturnValue(mockSearchRequest);
         mockSearchRequestManagerControl.replay();
 
        JiraSearchProvider searchProvider = new JiraSearchProvider(null, mockQueryCreator, mockSearchRequestManager, mockSearchProvider, null, null, null, mockSearchRequestFactory)
         {
             void populateAndValidate(IssueNavigatorActionParams actionParams, FieldValuesHolder fieldValuesHolder, ErrorCollection errors, User user)
             {
                 fieldValuesHolder.put("query", "query");
             }
 
             SearchContext getSearchContext()
             {
                 return null;
             }
 
             ApplicationProperties getWebProperties()
             {
                 MockControl mockWebPropertiesControl = MockControl.createControl(ApplicationProperties.class);
                 ApplicationProperties mockApplicationProperties = (ApplicationProperties) mockWebPropertiesControl.getMock();
                 mockApplicationProperties.getBaseUrl();
                 mockWebPropertiesControl.setDefaultReturnValue("http://jira.atlassian.com");
                 mockApplicationProperties.getApplicationName();
                 mockWebPropertiesControl.setDefaultReturnValue("JIRA");
                 mockWebPropertiesControl.replay();
 
                 return mockApplicationProperties;
             }
 
             User getUser(String username)
             {
                 return null;
             }
         };
 
         com.atlassian.sal.api.search.SearchResults results = searchProvider.search(null, "query");
         assertNotNull(results);
         assertEquals(0, results.getErrors().size());
         assertEquals(1, results.getMatches().size());
         //assert stuff abou the one match
         SearchMatch searchMatch = results.getMatches().get(0);
         assertEquals("http://jira.atlassian.com/browse/JST-234", searchMatch.getUrl());
         assertEquals("[JST-234] Sample Summary", searchMatch.getTitle());
         assertEquals("Sample description for the query issue.", searchMatch.getExcerpt());
         assertEquals("1", searchMatch.getResourceType().getType());
         assertEquals("JIRA", searchMatch.getResourceType().getName());
         assertEquals("http://jira.atlassian.com", searchMatch.getResourceType().getUrl());
 
         mockJiraAuthenticationContextControl.verify();
         mockQueryCreatorControl.verify();
         mockSearchProviderControl.verify();
         mockSearchRequestControl.verify();
         mockSearchRequestManagerControl.verify();
     }
 }
