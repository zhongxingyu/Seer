 package com.bics.jira.mail.handler;
 
 import com.atlassian.crowd.embedded.api.User;
 import com.atlassian.jira.config.ConstantsManager;
 import com.atlassian.jira.issue.CustomFieldManager;
 import com.atlassian.jira.issue.Issue;
 import com.atlassian.jira.issue.IssueFactory;
 import com.atlassian.jira.issue.IssueFieldConstants;
 import com.atlassian.jira.issue.MutableIssue;
 import com.atlassian.jira.issue.fields.CustomField;
 import com.atlassian.jira.issue.priority.Priority;
 import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
 import com.atlassian.jira.project.Project;
 import com.atlassian.jira.service.util.handler.MessageHandlerErrorCollector;
 import com.atlassian.jira.web.FieldVisibilityManager;
 import com.bics.jira.mail.CommentExtractor;
 import com.bics.jira.mail.IssueBuilder;
 import com.bics.jira.mail.model.HandlerModel;
 import com.bics.jira.mail.model.MessageAdapter;
 
 import javax.mail.MessagingException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * JavaDoc here
  *
  * @author Victor Polischuk
  * @since 04.02.13 21:38
  */
 public class IssueBuilderImpl implements IssueBuilder {
     private final IssueFactory issueFactory;
     private final IssueSecurityLevelManager issueSecurityLevelManager;
     private final CustomFieldManager customFieldManager;
     private final ConstantsManager constantsManager;
     private final CommentExtractor commentExtractor;
     private final FieldVisibilityManager fieldVisibilityManager;
 
     public IssueBuilderImpl(FieldVisibilityManager fieldVisibilityManager, CommentExtractor commentExtractor, ConstantsManager constantsManager, CustomFieldManager customFieldManager, IssueSecurityLevelManager issueSecurityLevelManager, IssueFactory issueFactory) {
         this.fieldVisibilityManager = fieldVisibilityManager;
         this.commentExtractor = commentExtractor;
         this.constantsManager = constantsManager;
         this.customFieldManager = customFieldManager;
         this.issueSecurityLevelManager = issueSecurityLevelManager;
         this.issueFactory = issueFactory;
     }
 
     @Override
     public MutableIssue build(HandlerModel model, MessageAdapter message, MessageHandlerErrorCollector monitor) throws MessagingException {
         Project project = model.getProject();
         String issueTypeId = model.getIssueType().getId();
         User defaultReporter = model.getReporterUser();
 
         Long levelId = issueSecurityLevelManager.getDefaultSecurityLevel(project);
         MutableIssue issue = issueFactory.getIssue();
 
         issue.setProjectObject(project);
         issue.setSummary(message.getSubject());
         issue.setIssueTypeId(issueTypeId);
         issue.setReporter(defaultReporter);
 
         if (!fieldVisibilityManager.isFieldHiddenInAllSchemes(project.getId(), IssueFieldConstants.PRIORITY, Collections.singletonList(issueTypeId))) {
             setPriority(issue, message);
         }
 
         if (!fieldVisibilityManager.isFieldHiddenInAllSchemes(project.getId(), IssueFieldConstants.DESCRIPTION, Collections.singletonList(issueTypeId))) {
             String body = commentExtractor.extractBody(model, message);
 
             issue.setDescription(body);
         }
 
         if (levelId != null) {
             issue.setSecurityLevelId(levelId);
         }
 
 
         for (CustomField customField : customFieldManager.getCustomFieldObjects(issue)) {
             issue.setCustomFieldValue(customField, customField.getDefaultValue(issue));
         }
 
         return issue;
     }
 
     private void setPriority(MutableIssue issue, MessageAdapter message) {
         List<Priority> priorities = new ArrayList<Priority>(constantsManager.getPriorityObjects());
 
        int index = (int) Math.ceil((double) message.getPriority() * priorities.size() / 5D);
 
         if (index >= priorities.size()) {
             index = priorities.size() - 1;
         }
 
         issue.setPriorityObject(priorities.get(index));
     }
 }
