 /* 
 * Copyright (C) allesklar.com AG
 * All rights reserved.
 *
 * Author: juergi
 * Date: 30.05.12 
 *
 */
 
 
 package com.jmelzer.service.batch;
 
 import com.jmelzer.data.dao.*;
 import com.jmelzer.data.model.*;
 import com.jmelzer.data.model.ui.UiField;
 import com.jmelzer.data.model.ui.View;
 import com.jmelzer.data.model.ui.ViewTab;
 import com.jmelzer.data.uimodel.Field;
 import com.jmelzer.data.util.StreamUtils;
 import com.jmelzer.service.IssueManager;
 import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
 
 public class Setup extends AbstractBatch {
 
     public static void main(String[] args) {
 
         Setup batch = new Setup();
         batch.run();
     }
 
     @Override
     void doIt() throws Exception {
         Md5PasswordEncoder encoder = new Md5PasswordEncoder();
         UserDao userDao = (UserDao) context.getBean("userDao");
         UserRoleDao userRoleDao = (UserRoleDao) context.getBean("userRoleDao");
         ProjectDao projectDao = (ProjectDao) context.getBean("projectDao");
         StatusDao statusDao = (StatusDao) context.getBean("statusDao");
         PriorityDao priorityDao = (PriorityDao) context.getBean("priorityDao");
         IssueTypeDao issueTypeDao = (IssueTypeDao) context.getBean("issueTypeDao");
         IssueManager issueManager = (IssueManager) context.getBean("issueManager");
         ViewDao viewDao = (ViewDao) context.getBean("viewDao");
         ActivationCodeDao activationCodeDao = (ActivationCodeDao) context.getBean("activationCodeDao");
 
         UserRole userRoleDev = new UserRole(UserRole.Roles.ROLE_DEVELOPER.toString());
         userRoleDao.save(userRoleDev);
         UserRole userRoleAdmin = new UserRole(UserRole.Roles.ROLE_ADMIN.toString());
         userRoleDao.save(userRoleAdmin);
         UserRole userRoleUser = new UserRole(UserRole.Roles.ROLE_USER.toString());
         userRoleDao.save(userRoleUser);
 
 
         User dev;
         User jm;
         {
             User user = userDao.findByUserNameNonLocked("admin");
             if (user == null) {
                 user = new User();
             }
 
             user.setName("admin");
             user.setPassword(encoder.encodePassword("42", "admin"));
             user.setEmail("admin@wreckcontrol.net");
             user.setLoginName("admin");
             user.setLocked(false);
             user.setAvatar(StreamUtils.toByteArray(getClass().getResourceAsStream("admin.png")));
             user.addRole(userRoleAdmin);
             userDao.save(user);
         }
         {
             jm = userDao.findByUserNameNonLocked("jm");
             if (jm == null) {
                 jm = new User();
             }
             jm.setName("jm");
             jm.setPassword(encoder.encodePassword("42", "jm"));
             jm.setEmail("jm@wreckcontrol.net");
             jm.setLoginName("jm");
             jm.setLocked(false);
             jm.setAvatar(StreamUtils.toByteArray(getClass().getResourceAsStream("user.png")));
             jm.addRole(userRoleUser);
             userDao.save(jm);
         }
         {
             dev = userDao.findByUserNameNonLocked("developer");
             if (dev == null) {
                 dev = new User();
             }
             dev.setName("developer");
             dev.setPassword(encoder.encodePassword("42", "developer"));
             dev.setEmail("developer@wreckcontrol.net");
             dev.setLoginName("developer");
             dev.setLocked(false);
             dev.setAvatar(StreamUtils.toByteArray(getClass().getResourceAsStream("user.png")));
             dev.addRole(userRoleDev);
             userDao.save(dev);
         }
         Status status;
         {
             status = new Status("Open", 1);
             statusDao.save(status);
             statusDao.save(new Status("In Progress", 2));
             statusDao.save(new Status("Resolved", 3));
             statusDao.save(new Status("Reopened", 4));
             statusDao.save(new Status("Closed", 5));
         }
         Project project;
         {
             //dummy project for testing
             project = new Project();
             project.setName("Test");
             project.setShortName("TST");
             Project projectDb = projectDao.findOneByExample(project);
             if (projectDb != null) {
                 project = projectDb;
             }
             project.setLead(userDao.findByUserName("jm"));
             projectDao.save(project);
 
             {
                 Component component = new Component("service");
                 project.addComponent(component);
             }
             {
                 Component component = new Component("frontend");
                 project.addComponent(component);
             }
             {
                 ProjectVersion projectVersion = new ProjectVersion();
                 projectVersion.setVersionNumber("0.9");
                 project.addVersion(projectVersion);
             }
             {
                 ProjectVersion projectVersion = new ProjectVersion();
                 projectVersion.setVersionNumber("1.0");
                 project.addVersion(projectVersion);
             }
             projectDao.save(project);
         }
         IssueType issueType;
         {
             issueTypeDao.save(new IssueType("Bug", "images/bug.gif"));
             issueType = new IssueType("Feature", "images/newfeature.gif");
             issueTypeDao.save(issueType);
             issueTypeDao.save(new IssueType("Task", "images/task.gif"));
         }
         Priority prio = createPriorities(priorityDao);
         createViews(viewDao);
         createSampleIssue(issueManager, project.getId(),
                           status,
                           issueType.getId(), prio.getId(), "service",
                           dev, jm.getUsername());
     }
 
     private void createSampleIssue(IssueManager issueManager,
                                    Long projectId,
                                    Status status,
                                    Long issueTypeId,
                                    Long prioId,
                                    String componentName,
                                    User assignee,
                                    String reporter) throws InterruptedException {
         Issue issue = new Issue();
         issue.setAssignee(assignee);
         issue.setSummary("this is an example test issue.");
        issue.setDescription("Um <bold>Suchwort</bold>-Analysen erstellen zu k\u00F6nnen ben\u00F6tigen wir die M\u00F6glichkeit, einen Blick in den Index (in die f\u00FCr eine Kampagne erstellten Suchw\u00F6rter) zu werfen." +
                              "<br>" +
                              "Dabei haben wir im wesentlich zwei Anforderungen:.");
         issue.setStatus(status);
         issueManager.create(issue,
                             projectId,
                             issueTypeId,
                             prioId,
                             componentName,
                             reporter);
 
         issueManager.addComment(issue.getPublicId(), "Das ist doch alles nix hier", "developer");
         Thread.sleep(1000L);
         issueManager.addComment(issue.getPublicId(), "Doch doch dat funktioniert doch alles", "admin");
         Thread.sleep(1000L);
         issueManager.addComment(issue.getPublicId(), "Doch doch dat funktioniert doch alles", "admin");
         Thread.sleep(1000L);
        issueManager.addComment(issue.getPublicId(), " <H2>Demonstrating a few HTML features</H2>\\n" +
                                                      "\n" +
                                                      "</CENTER>\n" +
                                                      "\n" +
                                                      "HTML is really a very simple language. It consists of ordinary text, with commands that are enclosed by \"<\" and \">\" characters, or bewteen an \"&\" and a \";\". <P>\n" +
                                                      " ", "developer");
     }
 
     private Priority createPriorities(PriorityDao priorityDao) {
         priorityDao.save(new Priority("Blocker", 1));
         priorityDao.save(new Priority("Critical", 2));
         Priority prio = new Priority("Major", 3);
         priorityDao.save(prio);
         priorityDao.save(new Priority("Minor", 4));
         priorityDao.save(new Priority("Trivial", 5));
         return prio;
     }
 
     private void createViews(ViewDao viewDao) {
         View view = new View();
         view.setName("createissue");
         view.setDecription("Standard view for creating issues");
         ViewTab viewTab = new ViewTab();
         viewTab.setName("Tab1");
         view.addTab(viewTab);
         {
             UiField field = new UiField();
             field.setFieldId(Field.ISSUETYPE_ID);
             field.setPosition(2);
             viewTab.addField(field);
         }
         {
             UiField field = new UiField();
             field.setFieldId(Field.PROJECT_ID);
             field.setPosition(1);
             viewTab.addField(field);
         }
         {
             UiField field = new UiField();
             field.setFieldId(Field.SUMMARY_ID);
             field.setPosition(3);
             viewTab.addField(field);
         }
         {
             UiField field = new UiField();
             field.setFieldId(Field.PRIORITY_ID);
             field.setPosition(4);
             viewTab.addField(field);
         }
         {
             UiField field = new UiField();
             field.setFieldId(Field.DUEDATE_ID);
             field.setPosition(5);
             viewTab.addField(field);
         }
         {
             UiField field = new UiField();
             field.setFieldId(Field.COMPONENT_ID);
             field.setPosition(6);
             viewTab.addField(field);
         }
         {
             UiField field = new UiField();
             field.setFieldId(Field.FIXVERSION_ID);
             field.setPosition(7);
             viewTab.addField(field);
         }
         {
             UiField field = new UiField();
             field.setFieldId(Field.AFFECTED_VERSION_ID);
             field.setPosition(8);
             viewTab.addField(field);
         }
         {
             UiField field = new UiField();
             field.setFieldId(Field.ASSIGNEE_ID);
             field.setPosition(9);
             viewTab.addField(field);
         }
         {
             UiField field = new UiField();
             field.setFieldId(Field.DESCRIPTION_ID);
             field.setPosition(10);
             viewTab.addField(field);
         }
         {
             UiField field = new UiField();
             field.setFieldId(Field.REMAININGESTIMATE_ID);
             field.setPosition(11);
             viewTab.addField(field);
         }
         {
             UiField field = new UiField();
             field.setFieldId(Field.ORGESTIMATE_ID);
             field.setPosition(12);
             viewTab.addField(field);
         }
         viewDao.save(view);
     }
 }
