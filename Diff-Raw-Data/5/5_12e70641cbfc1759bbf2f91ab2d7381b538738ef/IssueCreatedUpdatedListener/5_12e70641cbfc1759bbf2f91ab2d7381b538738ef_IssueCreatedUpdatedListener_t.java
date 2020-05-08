 package com.ideotechnologies.jira.plugin.event.listener;
 
 import com.atlassian.event.api.EventListener;
 import com.atlassian.event.api.EventPublisher;
 import com.atlassian.jira.ComponentManager;
 import com.atlassian.jira.event.issue.IssueEvent;
 import com.atlassian.jira.event.type.EventType;
 import com.atlassian.jira.issue.Issue;
 import com.atlassian.jira.issue.index.IndexException;
 import com.atlassian.jira.issue.index.IssueIndexManager;
 import com.atlassian.jira.project.Project;
 import com.atlassian.jira.project.version.Version;
 import com.atlassian.jira.util.ImportUtils;
 import org.ofbiz.core.entity.GenericEntityException;
 import org.springframework.beans.factory.DisposableBean;
 import org.springframework.beans.factory.InitializingBean;
 import webwork.util.ClassLoaderUtils;
 
 import java.io.*;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Properties;
 
 public class IssueCreatedUpdatedListener implements InitializingBean, DisposableBean {
 	 private final EventPublisher eventPublisher;
 	 private static final String PROPERTIES_FILE = "projects_map.properties";
 	 /**
 	  * Constructor.
 	  * @param eventPublisher injected {@code EventPublisher} implementation.
 	  */
 	 public IssueCreatedUpdatedListener(EventPublisher eventPublisher){
 		 this.eventPublisher = eventPublisher;
 
 	 }
 
 	 @EventListener
 	 public void onIssueEvent(IssueEvent issueEvent) {
 		 Long eventTypeId = issueEvent.getEventTypeId();
 		 Issue issue = issueEvent.getIssue();
 		 Properties prop;
 		 prop = getProjectKeyProperties();
 		  if (eventTypeId.equals(EventType.ISSUE_CREATED_ID) || eventTypeId.equals(EventType.ISSUE_UPDATED_ID)){
 	 if(issue.getIssueTypeObject().getName().equals("Cut")){
 		 String summary = issue.getSummary();
 		 String projectVersionKey = issue.getProjectObject().getKey();
 		prop = getProjectKeyProperties();
 		String propValue = prop.getProperty(projectVersionKey);
 		//Traitement de la chaine de caractere et conversion en String[] (split)
 		String[] arrayPropValue = propValue.split(",");
 		List<String> listPropValue =Arrays.asList(arrayPropValue);
 		List<Long> issueIds = null ;
 		List<Version> fixVersions;
 		ComponentManager componentManager = ComponentManager.getInstance();
 		for (String keyProjectValue : listPropValue) {
 		Project project = componentManager.getProjectManager().getProjectObjByKey(keyProjectValue.trim());
 		try {
 			issueIds = (List<Long>) componentManager.getIssueManager().getIssueIdsForProject(project.getId());
 		} catch (GenericEntityException e) {
 			e.printStackTrace();
 		}
 		for (Long issueId : issueIds) {
 			Issue issueSelected = componentManager.getIssueManager().getIssueObject(issueId);
 			 fixVersions = (List<Version>) issueSelected.getFixVersions();
 			if(!fixVersions.isEmpty() && fixVersions.get(0).getName().equals(summary)){
 				reIndexIssue(issueSelected);
 			}
 		}
 		}
 		// For each value of the table, retrieve the related projects and reindex the issues
 		// related to the fix version and the issues children.
 		 
 		
 	 }
 	 }
 	
 	 }
 	 
 	 /**
	  * Called when the plugins is being disabled or removed.
 	  * @throws Exception
 	  */
 	 @Override
 	 public void destroy() throws Exception {
 		 // unregister ourselves with the EventPublisher
 		 eventPublisher.unregister(this);
 	 }
 
 	 /**
	  * Called when the plugins has been enabled.
 	  * @throws Exception
 	  */
 	 @Override
 	 public void afterPropertiesSet() throws Exception {
 		 // register ourselves with the EventPublisher
 		 eventPublisher.register(this);
 
 	 }
 	 public Properties getProjectKeyProperties(){
 		 Properties properties = new Properties();
 		 InputStream stream = ClassLoaderUtils.getResourceAsStream(PROPERTIES_FILE, IssueCreatedUpdatedListener.class);
 		 if( stream == null )
 			{
 				URL resource = ClassLoaderUtils.getResource( PROPERTIES_FILE, IssueCreatedUpdatedListener.class );
 				File propFile = new File( resource.getFile() );
 				try {
 					stream = new FileInputStream( propFile );
 				} catch (FileNotFoundException e) {
 					e.printStackTrace();
 				}
 			}
 		 if (stream != null)
 			{
 				try {
 					properties.load( stream );
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 	 }
 		 try {
 			stream.close(); // check if necessary
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	return properties;	 
 }
 	 public void reIndexIssue(final Issue issueObject){
 		 IssueIndexManager issueIndexManager = ComponentManager.getInstance().getIndexManager();
 		 if (ImportUtils.isIndexIssues()) {
              try {
 				issueIndexManager.reIndex(issueObject);
 			} catch (IndexException e) {
 				e.printStackTrace();
 			}
          } else {
              ImportUtils.setIndexIssues(true);
              try {
 				issueIndexManager.reIndex(issueObject);
 			} catch (IndexException e) {
 				e.printStackTrace();
 			}
              finally {
                  // Ensure we disable indexes again.
                  ImportUtils.setIndexIssues(false);
              }}
 	 }
 }
