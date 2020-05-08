 package org.hackystat.projectbrowser.page.projects;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 import java.util.logging.Logger;
 import javax.xml.datatype.DatatypeConstants;
 import javax.xml.datatype.XMLGregorianCalendar;
 import org.apache.wicket.markup.html.form.Button;
 import org.apache.wicket.markup.html.list.ListView;
 import org.apache.wicket.util.tester.FormTester;
 import org.apache.wicket.util.tester.WicketTester;
 import org.hackystat.projectbrowser.ProjectBrowserApplication;
 import org.hackystat.projectbrowser.authentication.SigninPage;
 import org.hackystat.projectbrowser.page.sensordata.SensorDataPage;
 import org.hackystat.projectbrowser.test.ProjectBrowserTestHelper;
 import org.hackystat.sensorbase.client.SensorBaseClient;
 import org.hackystat.sensorbase.client.SensorBaseClientException;
 import org.hackystat.sensorbase.resource.projects.jaxb.Project;
 import org.hackystat.sensorbase.resource.projects.jaxb.ProjectRef;
 import org.hackystat.utilities.tstamp.Tstamp;
 import org.junit.After;
 import org.junit.Test;
 import org.junit.Assert;
 
 /**
  * Tests the SensorDataPage.
  * 
  * @author Philip Johnson
  * @author Randy Cox
  */
 public class TestProjectsPage extends ProjectBrowserTestHelper {  
 
   private static String TEST_USER = "TestUser@hackystat.org";
   private static String TEST_INVITEE = "TestInvitee@hackystat.org";
   private static String DEFAULT_NAME = "Default";
 
   private static String TEST_NEW_NAME = "TestProjectName";
   private static String TEST_NEW_STARTDATE = "2008-01-01";
   private static String TEST_NEW_ENDDATE = "2008-12-31";
   private static String TEST_NEW_DESC = "Test description.";
   private static String TEST_NEW_INVITEE = TEST_INVITEE;
  private static String TEST_NEW_SPECTATOR = "TestSpectator@hackystat.org";
 
   private static String TEST_EDIT_STARTDATE = "2008-01-01";
   private static String TEST_EDIT_ENDDATE = "2008-12-31";
   private static String TEST_EDIT_DESC = "Test description.";
   private static String TEST_EDIT_INVITEE = TEST_INVITEE;
  private static String TEST_EDIT_SPECTATOR = "TestSpectator@hackystat.org";
 
   private static String TEST_RENAME_NAME = "TestRenameProject";
 
   private static String PREFIX_LIST = "projectsForm:projListPanel:projListForm:projectTable:";
   private static String DATE_PATTERN = "yyyy-MM-dd";
   
   private String PROJECTS_LINK = "ProjectsPageLink";
 //  private String LOGOUT_LINK = "LogoutLink";
   private String PROJECTS_FORM = "projectsForm";
   private String SIGNIN_FORM = "signinForm";
   private String LIST_PANEL = "projectsForm:projListPanel";
   private String EDIT_PANEL = "projectsForm:projEditPanel";
   private String RENAME_PANEL = "projectsForm:projRenamePanel";
   private String DELETE_PANEL = "projectsForm:projDeletePanel";
   private String LEAVE_PANEL = "projectsForm:projLeavePanel";
   private String REPLY_PANEL = "projectsForm:projReplyPanel";
   private String PROJECT_TABLE = "projectsForm:projListPanel:projListForm:projectTable";
   private String EDIT_BUTTON = ":editButton";
   private String RENAME_BUTTON = ":renameButton";
   private String DELETE_BUTTON = ":deleteButton";
   private String LEAVE_BUTTON = ":leaveButton";
   private String REPLY_BUTTON = ":replyButton";
 
   private String USER = "user";
   private String PASSWORD = "password";
   private String SIGNIN = "Signin";
   private String NOT_FOUND = " project not found in table list.";
   
 
   /**
    * Clean up data.
    * 
    * @throws Exception when communication error occurs.
    */
   @After
   public void cleanup() throws Exception { 
     SensorBaseClient client = new SensorBaseClient(getSensorBaseHostName(), TEST_USER, TEST_USER);
     for (ProjectRef ref : client.getProjectIndex(TEST_USER).getProjectRef()) {
       Project project = client.getProject(ref);
       if (TEST_USER.equals(project.getOwner()) && !DEFAULT_NAME.equals(project.getName())) {
         client.deleteProject(TEST_USER, project.getName());
       }
     }
   }
 
   /**
    * Logs in and navigates to the projects page.
    * 
    * @param tester Wicket tester
    */
   public void login(WicketTester tester) { 
 
     // Login and check for correct start page
     tester.startPage(SigninPage.class);
     FormTester signinForm = tester.newFormTester(SIGNIN_FORM);  
     signinForm.setValue(USER, TEST_USER);
     signinForm.setValue(PASSWORD, TEST_USER);
     signinForm.submit(SIGNIN);
     tester.assertRenderedPage(SensorDataPage.class);
 
     // Navigate to projects page and check that page is setup correctly
     tester.clickLink(PROJECTS_LINK);
     tester.assertRenderedPage(ProjectsPage.class);
     tester.assertComponent(PROJECTS_FORM, ProjectsForm.class);
     tester.assertVisible(LIST_PANEL);
     tester.assertInvisible(EDIT_PANEL);
     tester.assertInvisible(RENAME_PANEL);
     tester.assertInvisible(LEAVE_PANEL);
     tester.assertInvisible(DELETE_PANEL);
     tester.assertInvisible(REPLY_PANEL);
   }
 
   /**
    * Dump project info to logger.
    * 
    * @param project to log.
    */
   public void logProject(Project project) {
     Logger logger = ((ProjectBrowserApplication) ProjectBrowserApplication.get()).getLogger();
     logger.info("  Name = " + project.getName());
     logger.info("  Owner = " + project.getOwner());
     logger.info("  StartTime = " + project.getStartTime());
     logger.info("  EndTime = " + project.getEndTime());
     logger.info("  Description = " + project.getDescription());
     logger.info("  Members = " + project.getMembers().getMember().toString());
     logger.info("  Invitations = " + project.getInvitations().getInvitation().toString());
     logger.info("  Spectators = " + project.getSpectators().getSpectator().toString());
     Integer propertyCount = project.getProperties().getProperty().size();
     logger.info("  Properties row count= " + propertyCount.toString());
     if (propertyCount > 0) {
       logger.info("    1st projectPropertyKey = "
           + project.getProperties().getProperty().get(0).getKey());
       logger.info("    1st projectPropertyValue = "
           + project.getProperties().getProperty().get(0).getValue());
     }
   }
 
   /**
    * Loop through project list in List panel and find index of project
    * 
    * @param tester application
    * @param name to look for in list
    * @return index of project in list, -1 = not found
    */
   private int findProjectIndexInList(WicketTester tester, String name) {
     ListView listView = (ListView) tester
         .getComponentFromLastRenderedPage(PROJECT_TABLE);
     Integer index = -1;
     for (int i = 0; i < listView.size(); i++) {
       Project project = (Project) listView.getList().get(i);
       if (name.equals(project.getName())) {
         index = i;
         break;
       }
     }
     return index;
   }
 
   /**
    * Loop through project list in List panel and find index of project
    * 
    * @param tester application
    * @param name to look for in list
    * @return index of project in list, -1 = not found
    */
   private Project findProjectInList(WicketTester tester, String name) {
     ListView listView = (ListView) tester
         .getComponentFromLastRenderedPage(PROJECT_TABLE);
     Project project = null;
     for (int i = 0; i < listView.size(); i++) {
       if (name.equals(((Project) listView.getList().get(i)).getName())) {
         project = (Project) listView.getList().get(i);
         break;
       }
     }
     return project;
   }
 
   /**
    * Dump component tree to log for debug.
    * 
    * @throws SensorBaseClientException when communication error occurs.
    */
   public void dumpComponentTree() throws SensorBaseClientException { // NOPMD WicketTester has
                                                                           // its own assert classes.
     WicketTester tester = new WicketTester(new ProjectBrowserApplication(getTestProperties()));
     login(tester);
     tester.debugComponentTrees();
   }
 
   /**
    * Test navigation to projects page and rendering of project list panel.
    * 
    * @throws SensorBaseClientException when communication error occurs.
    */
   @Test
   public void testProjectsPage() throws SensorBaseClientException { // NOPMD WicketTester has its
                                                                     // own assert classes.
     WicketTester tester = new WicketTester(new ProjectBrowserApplication(getTestProperties()));
 
     // Login
     login(tester);
 
     // Loop through table and make sure that test project is present
     ListView listView = (ListView) tester
         .getComponentFromLastRenderedPage(PROJECT_TABLE);
     Integer index = 0;
     Boolean found = false;
     for (Object object : listView.getList()) {
 
       // Log project
       Project project = (Project) object;
 
       // Check for default project and make sure all buttons are invisible
       if (TestProjectsPage.DEFAULT_NAME.equals(project.getName())) {
         found = true;
         // Verify default project and state of buttons
         tester.assertInvisible(PREFIX_LIST + index.toString() + EDIT_BUTTON);
         tester.assertInvisible(PREFIX_LIST + index.toString() + DELETE_BUTTON);
         tester.assertInvisible(PREFIX_LIST + index.toString() + RENAME_BUTTON);
         tester.assertInvisible(PREFIX_LIST + index.toString() + LEAVE_BUTTON);
         tester.assertInvisible(PREFIX_LIST + index.toString() + REPLY_BUTTON);
       }
       else {
         index++;
       }
     }
     Assert.assertTrue(DEFAULT_NAME + NOT_FOUND, found);
   }
 
   /**
    * Create a new project, used by multiple tests.
    * 
    * @param tester Wicket tester
    * @throws Exception when communication error occurs.
    */
   private void createNewProject(WicketTester tester) throws Exception {
 
     // Register intended invitees and spectators
     SensorBaseClient.registerUser(getSensorBaseHostName(), TEST_NEW_INVITEE);
     SensorBaseClient.registerUser(getSensorBaseHostName(), TEST_NEW_SPECTATOR);
 
     // Store project count for later testing
     ListView listView = (ListView) tester
         .getComponentFromLastRenderedPage(PROJECT_TABLE);
     Integer previousListCount = listView.getList().size();
 
     // Push the new button
     Button newButton = (Button) tester
         .getComponentFromLastRenderedPage("projectsForm:projListPanel:projListForm:newButton");
     newButton.onSubmit();
 
     // Assert correct panels are visible
     tester.assertComponent(EDIT_PANEL, ProjEditPanel.class);
     tester.assertVisible(EDIT_PANEL);
     tester.assertInvisible(LIST_PANEL);
     tester.assertInvisible(RENAME_PANEL);
     tester.assertInvisible(LEAVE_PANEL);
     tester.assertInvisible(DELETE_PANEL);
     tester.assertInvisible(REPLY_PANEL);
 
     // Enter test data into edit form
     FormTester editForm = tester.newFormTester("projectsForm:projEditPanel:projEditForm");
     editForm.setValue("projectName", TEST_NEW_NAME);
     editForm.setValue("projectStartDate", TEST_NEW_STARTDATE);
     editForm.setValue("projectEndDate", TEST_NEW_ENDDATE);
     editForm.setValue("projectDesc", TEST_NEW_DESC);
     editForm.setValue("projectInvitations", TEST_NEW_INVITEE);
     editForm.setValue("projectSpectators", TEST_NEW_SPECTATOR);
     editForm.submit();
 
     // Push save button
     Button saveButton = (Button) tester
         .getComponentFromLastRenderedPage("projectsForm:projEditPanel:projEditForm:saveButton");
     saveButton.onSubmit();
     tester.newFormTester(PROJECTS_FORM).submit();
 
     // Check that project list panel is active and visIble
     tester.assertVisible(LIST_PANEL);
     tester.assertInvisible(EDIT_PANEL);
     tester.assertInvisible(RENAME_PANEL);
     tester.assertInvisible(LEAVE_PANEL);
     tester.assertInvisible(DELETE_PANEL);
     tester.assertInvisible(REPLY_PANEL);
 
     // Check project count, should be one more
     ListView listViewAfter = (ListView) tester
         .getComponentFromLastRenderedPage(PROJECT_TABLE);
     Integer projectListCount = listViewAfter.getList().size();
     Assert.assertEquals("An addtional project should be added to project list.",
         (int) previousListCount + 1, (int) projectListCount);
 
     // Loop through table and make sure that test project is present
     Integer index = 0;
     Boolean found = false;
     for (Object object : listViewAfter.getList()) {
       Project project = (Project) object;
 
       // Check data within model backing the test project
       if (TestProjectsPage.TEST_NEW_NAME.equals(project.getName())) {
         found = true;
 
         // Check buttons
         tester.assertVisible(PREFIX_LIST + index.toString() + EDIT_BUTTON);
         tester.assertVisible(PREFIX_LIST + index.toString() + DELETE_BUTTON);
         tester.assertVisible(PREFIX_LIST + index.toString() + RENAME_BUTTON);
         tester.assertInvisible(PREFIX_LIST + index.toString() + LEAVE_BUTTON);
         tester.assertInvisible(PREFIX_LIST + index.toString() + REPLY_BUTTON);
 
         // Check start date.
         SimpleDateFormat format = new SimpleDateFormat(DATE_PATTERN, Locale.ENGLISH);
         Date startDate = format.parse(TestProjectsPage.TEST_NEW_STARTDATE);
         XMLGregorianCalendar startTime = Tstamp.makeTimestamp(startDate.getTime());
         Assert.assertEquals("Start date should be " + TEST_NEW_STARTDATE, DatatypeConstants.EQUAL,
             project.getStartTime().compare(startTime));
 
         // Check start date.
         Date endDate = format.parse(TestProjectsPage.TEST_NEW_ENDDATE);
         XMLGregorianCalendar endTime = Tstamp.makeTimestamp(endDate.getTime());
         Assert.assertEquals("End date should be " + TEST_NEW_ENDDATE, DatatypeConstants.EQUAL,
             project.getEndTime().compare(endTime));
 
         // Test project description.
         Assert.assertEquals("Project description", TestProjectsPage.TEST_NEW_DESC, project
             .getDescription());
 
         // Test project members.
         Assert.assertTrue(project.getMembers().getMember().isEmpty());
         Assert.assertTrue(project.getInvitations().getInvitation().contains(TEST_NEW_INVITEE));
         Assert.assertTrue(project.getSpectators().getSpectator().contains(TEST_NEW_SPECTATOR));
       }
       else {
         index++;
       }
     }
     Assert.assertTrue(TEST_NEW_NAME + NOT_FOUND, found);
   }
 
   /**
    * Test creating a new project.
    * 
    * @throws Exception when communication error occurs.
    */
   @Test
   public void testProjectsNewPage() throws Exception { // NOPMD WicketTester has its own assert
                                                         // classes.
     WicketTester tester = new WicketTester(new ProjectBrowserApplication(getTestProperties()));
 
     // Login
     login(tester);
 
     // Create new project and test it
     createNewProject(tester);
   }
 
   /**
    * Test edit of existing project.
    * 
    * @throws Exception when communication error occurs.
    */
   @Test
   public void testProjectsEditPage() throws Exception { // NOPMD WicketTester has its own assert
                                                         // classes.
     WicketTester tester = new WicketTester(new ProjectBrowserApplication(getTestProperties()));
 
     login(tester);
     createNewProject(tester);
 
     ListView listView = (ListView) tester
         .getComponentFromLastRenderedPage(PROJECT_TABLE);
     Integer previousListCount = listView.getList().size();
 
     // Push the edit button
     Integer index = findProjectIndexInList(tester, TEST_NEW_NAME);
     Button editButton = (Button) tester
         .getComponentFromLastRenderedPage(PREFIX_LIST
             + index.toString() + EDIT_BUTTON);
     editButton.onSubmit();
 
     // Assert correct panels are visible
     tester.assertComponent(EDIT_PANEL, ProjEditPanel.class);
     tester.assertVisible(EDIT_PANEL);
     tester.assertInvisible(LIST_PANEL);
     tester.assertInvisible(RENAME_PANEL);
     tester.assertInvisible(LEAVE_PANEL);
     tester.assertInvisible(DELETE_PANEL);
     tester.assertInvisible(REPLY_PANEL);
 
     // Enter test data into edit form
     FormTester form = tester.newFormTester("projectsForm:projEditPanel:projEditForm");
     form.setValue("projectStartDate", TEST_EDIT_STARTDATE);
     form.setValue("projectEndDate", TEST_EDIT_ENDDATE);
     form.setValue("projectDesc", TEST_EDIT_DESC);
     form.setValue("projectInvitations", TEST_EDIT_INVITEE);
     form.setValue("projectSpectators", TEST_EDIT_SPECTATOR);
     form.submit();
 
     // Push save button
     Button button = (Button) tester
         .getComponentFromLastRenderedPage("projectsForm:projEditPanel:projEditForm:saveButton");
     button.onSubmit();
 
     // Check project count, should be one more
     listView = (ListView) tester
         .getComponentFromLastRenderedPage(PROJECT_TABLE);
     Integer projectListCount = listView.getList().size();
     Assert.assertEquals("No additional project should be added to project list.",
         (int) previousListCount, (int) projectListCount);
 
     // Loop through table and make sure that test rename project is present
     index = 0;
     Boolean found = false;
     for (Object object : listView.getList()) {
       Project project = (Project) object;
 
       // Check data within model backing the test project
       if (TestProjectsPage.TEST_NEW_NAME.equals(project.getName())) {
         found = true;
 
         // Check start date.
         SimpleDateFormat format = new SimpleDateFormat(DATE_PATTERN, Locale.ENGLISH);
         Date startDate = format.parse(TEST_EDIT_STARTDATE);
         XMLGregorianCalendar startTime = Tstamp.makeTimestamp(startDate.getTime());
         Assert.assertEquals("Start date should be " + TEST_EDIT_STARTDATE, DatatypeConstants.EQUAL,
             project.getStartTime().compare(startTime));
 
         // Check start date.
         Date endDate = format.parse(TEST_EDIT_ENDDATE);
         XMLGregorianCalendar endTime = Tstamp.makeTimestamp(endDate.getTime());
         Assert.assertEquals("End date should be " + TEST_EDIT_ENDDATE, DatatypeConstants.EQUAL,
             project.getEndTime().compare(endTime));
 
         // Test project description.
         Assert.assertEquals("Project description", TEST_EDIT_DESC, project.getDescription());
 
         // Test project members.
         Assert.assertTrue(project.getMembers().getMember().isEmpty());
         Assert.assertTrue(project.getInvitations().getInvitation().contains(TEST_EDIT_INVITEE));
         Assert.assertTrue(project.getSpectators().getSpectator().contains(TEST_EDIT_SPECTATOR));
       }
       else {
         index++;
       }
     }
     Assert.assertTrue(TEST_NEW_NAME + NOT_FOUND, found);
   }
 
   /**
    * Test renaming of project.
    * 
    * @throws Exception when communication error occurs.
    */
   @Test
   public void testProjectsRenamePage() throws Exception { // NOPMD WicketTester has its own assert
                                                           // classes.
 
     WicketTester tester = new WicketTester(new ProjectBrowserApplication(getTestProperties()));
 
     login(tester);
     createNewProject(tester);
 
     ListView listView = (ListView) tester
         .getComponentFromLastRenderedPage(PROJECT_TABLE);
     Integer previousListCount = listView.getList().size();
 
     // Push the edit button
     Integer index = findProjectIndexInList(tester, TEST_NEW_NAME);
     Button renameButton = (Button) tester
         .getComponentFromLastRenderedPage(PREFIX_LIST
             + index.toString() + RENAME_BUTTON);
     renameButton.onSubmit();
 
     // Assert correct panels are visible
     tester.assertComponent(RENAME_PANEL, ProjRenamePanel.class);
     tester.assertInvisible(EDIT_PANEL);
     tester.assertInvisible(LIST_PANEL);
     tester.assertVisible(RENAME_PANEL);
     tester.assertInvisible(LEAVE_PANEL);
     tester.assertInvisible(DELETE_PANEL);
     tester.assertInvisible(REPLY_PANEL);
 
     // Enter test data into edit form
     FormTester renameForm = tester.newFormTester("projectsForm:projRenamePanel:projRenameForm");
     renameForm.setValue("projectRename", TEST_RENAME_NAME);
     renameForm.submit();
 
     // Push save button
     Button renameSaveButton = (Button) tester.getComponentFromLastRenderedPage(
       "projectsForm:projRenamePanel:projRenameForm:renameButton");
     renameSaveButton.onSubmit();
 
     // Check project count, should be one more
     listView = (ListView) tester
         .getComponentFromLastRenderedPage(PROJECT_TABLE);
     Integer projectListCount = listView.getList().size();
     Assert.assertEquals("No additional project should be added to project list.",
         (int) previousListCount, (int) projectListCount);
 
     // Loop through table and make sure that test rename project is present
     index = 0;
     Boolean found = false;
     for (Object object : listView.getList()) {
       Project project = (Project) object;
 
       // Check data within model backing the test project
       if (TestProjectsPage.TEST_RENAME_NAME.equals(project.getName())) {
         found = true;
 
         // Check start date.
         SimpleDateFormat format = new SimpleDateFormat(DATE_PATTERN, Locale.ENGLISH);
         Date startDate = format.parse(TestProjectsPage.TEST_NEW_STARTDATE);
         XMLGregorianCalendar startTime = Tstamp.makeTimestamp(startDate.getTime());
         Assert.assertEquals("Start date should be " + TEST_NEW_STARTDATE, DatatypeConstants.EQUAL,
             project.getStartTime().compare(startTime));
 
         // Check start date.
         Date endDate = format.parse(TestProjectsPage.TEST_NEW_ENDDATE);
         XMLGregorianCalendar endTime = Tstamp.makeTimestamp(endDate.getTime());
         Assert.assertEquals("End date should be " + TEST_NEW_ENDDATE, DatatypeConstants.EQUAL,
             project.getEndTime().compare(endTime));
 
         // Test project data
         Assert.assertEquals("Project description", TestProjectsPage.TEST_NEW_DESC, project
             .getDescription());
         Assert.assertTrue(project.getMembers().getMember().isEmpty());
         Assert.assertTrue(project.getInvitations().getInvitation().contains(TEST_NEW_INVITEE));
         Assert.assertTrue(project.getSpectators().getSpectator().contains(TEST_NEW_SPECTATOR));
       }
       else {
         index++;
       }
     }
     Assert.assertTrue(TEST_RENAME_NAME + NOT_FOUND, found);
   }
 
   /**
    * Test the deletion of a project.
    * 
    * @throws Exception when communication error occurs.
    */
   @Test
   public void testProjectsDeletePage() throws Exception { // NOPMD WicketTester has its own assert
 
     WicketTester tester = new WicketTester(new ProjectBrowserApplication(getTestProperties()));
 
     login(tester);
     createNewProject(tester);
 
     ListView listView = (ListView) tester
         .getComponentFromLastRenderedPage(PROJECT_TABLE);
     Integer previousListCount = listView.getList().size();
 
     // Push the edit button
     Integer index = findProjectIndexInList(tester, TEST_NEW_NAME);
     Button deleteButton = (Button) tester
         .getComponentFromLastRenderedPage(PREFIX_LIST
             + index.toString() + DELETE_BUTTON);
     deleteButton.onSubmit();
 
     // Assert correct panels are visible
     tester.assertComponent(DELETE_PANEL, ProjDeletePanel.class);
     tester.assertInvisible(EDIT_PANEL);
     tester.assertInvisible(LIST_PANEL);
     tester.assertInvisible(RENAME_PANEL);
     tester.assertInvisible(LEAVE_PANEL);
     tester.assertVisible(DELETE_PANEL);
     tester.assertInvisible(REPLY_PANEL);
 
     // Push cancel button
     Button cancelDeleteButton = (Button) tester.getComponentFromLastRenderedPage(
       "projectsForm:projDeletePanel:projDeleteForm:cancelButton");
     cancelDeleteButton.onSubmit();
     tester.newFormTester(PROJECTS_FORM).submit();
 
     // Assert correct panels are visible
     tester.assertComponent(LIST_PANEL, ProjListPanel.class);
     tester.assertInvisible(EDIT_PANEL);
     tester.assertVisible(LIST_PANEL);
     tester.assertInvisible(RENAME_PANEL);
     tester.assertInvisible(LEAVE_PANEL);
     tester.assertInvisible(DELETE_PANEL);
     tester.assertInvisible(REPLY_PANEL);
 
     // Push the delete button again
     index = findProjectIndexInList(tester, TEST_NEW_NAME);
     deleteButton = (Button) tester
         .getComponentFromLastRenderedPage(PREFIX_LIST
             + index.toString() + DELETE_BUTTON);
     deleteButton.onSubmit();
 
     // Push delete button
     Button deleteDeleteButton = (Button) tester.getComponentFromLastRenderedPage(
       "projectsForm:projDeletePanel:projDeleteForm:deleteButton");
     deleteDeleteButton.onSubmit();
 
     // Check project count, should be one more
     listView = (ListView) tester
         .getComponentFromLastRenderedPage(PROJECT_TABLE);
     Integer projectListCount = listView.getList().size();
     Assert.assertEquals("One project should be reomoved to project list.",
         (int) previousListCount - 1, (int) projectListCount);
 
     // Loop through table and make sure that test rename project is present
     Boolean found = false;
     for (Object object : listView.getList()) {
       Project project = (Project) object;
 
       // Check data within model backing the test project
       if (TestProjectsPage.TEST_NEW_NAME.equals(project.getName())) {
         found = true;
       }
     }
     Assert.assertFalse(TEST_NEW_NAME + " project should have been deleted.", found);
   }
 
   /**
    * Test the acceptance of an invitation to join a group.
    * 
    * @throws Exception when communication error occurs.
    */
   @Test
   public void testProjectsReplyAcceptPage() throws Exception { // NOPMD WicketTester has its own
                                                                 // assert classes.
 
     WicketTester tester = new WicketTester(new ProjectBrowserApplication(getTestProperties()));
 
     // Login and create new record with a test invitee
     login(tester);
     createNewProject(tester);
 
     // Login as test invitee
     tester.startPage(SigninPage.class);
     FormTester signinForm = tester.newFormTester(SIGNIN_FORM);
     signinForm.setValue(USER, TEST_INVITEE);
     signinForm.setValue(PASSWORD, TEST_INVITEE);
     signinForm.submit(SIGNIN);
     tester.assertRenderedPage(SensorDataPage.class);
 
     // Navigate to projects page and check that page is setup correctly
     tester.clickLink(PROJECTS_LINK);
     tester.assertRenderedPage(ProjectsPage.class);
     tester.assertComponent(PROJECTS_FORM, ProjectsForm.class);
     tester.assertVisible(LIST_PANEL);
     tester.assertInvisible(EDIT_PANEL);
     tester.assertInvisible(RENAME_PANEL);
     tester.assertInvisible(LEAVE_PANEL);
     tester.assertInvisible(DELETE_PANEL);
     tester.assertInvisible(REPLY_PANEL);
 
     // Store project count for later compare
     ListView listView = (ListView) tester
         .getComponentFromLastRenderedPage(PROJECT_TABLE);
     Integer previousListCount = listView.getList().size();
 
     // Check that all button invisible except reply button
     Integer index = findProjectIndexInList(tester, TEST_NEW_NAME);
     tester.assertInvisible(PREFIX_LIST + index.toString() + EDIT_BUTTON);
     tester.assertInvisible(PREFIX_LIST + index.toString() + DELETE_BUTTON);
     tester.assertInvisible(PREFIX_LIST + index.toString() + RENAME_BUTTON);
     tester.assertInvisible(PREFIX_LIST + index.toString() + LEAVE_BUTTON);
     tester.assertVisible(PREFIX_LIST + index.toString() + REPLY_BUTTON);
 
     // Push the edit button
     Button replyButton = (Button) tester
         .getComponentFromLastRenderedPage(PREFIX_LIST
             + index.toString() + REPLY_BUTTON);
     replyButton.onSubmit();
 
     // Assert correct panels are visible
     tester.assertComponent(REPLY_PANEL, ProjReplyPanel.class);
     tester.assertInvisible(EDIT_PANEL);
     tester.assertInvisible(LIST_PANEL);
     tester.assertInvisible(RENAME_PANEL);
     tester.assertInvisible(LEAVE_PANEL);
     tester.assertInvisible(DELETE_PANEL);
     tester.assertVisible(REPLY_PANEL);
 
     // Push cancel button
     Button cancelReplyButton = (Button) tester
         .getComponentFromLastRenderedPage("projectsForm:projReplyPanel:projReplyForm:cancelButton");
     cancelReplyButton.onSubmit();
     tester.newFormTester(PROJECTS_FORM).submit();
 
     // Assert correct panels are visible
     tester.assertComponent(LIST_PANEL, ProjListPanel.class);
     tester.assertInvisible(EDIT_PANEL);
     tester.assertVisible(LIST_PANEL);
     tester.assertInvisible(RENAME_PANEL);
     tester.assertInvisible(LEAVE_PANEL);
     tester.assertInvisible(DELETE_PANEL);
     tester.assertInvisible(REPLY_PANEL);
 
     // Push the delete button again
     index = findProjectIndexInList(tester, TEST_NEW_NAME);
     replyButton = (Button) tester
         .getComponentFromLastRenderedPage(PREFIX_LIST
             + index.toString() + REPLY_BUTTON);
     replyButton.onSubmit();
 
     // Push save button
     Button acceptButton = (Button) tester
         .getComponentFromLastRenderedPage("projectsForm:projReplyPanel:projReplyForm:acceptButton");
     acceptButton.onSubmit();
     tester.newFormTester(PROJECTS_FORM).submit();
 
     // Check project count
     listView = (ListView) tester
         .getComponentFromLastRenderedPage(PROJECT_TABLE);
     Integer projectListCount = listView.getList().size();
     Assert.assertEquals("Should have same amount of projects.", (int) previousListCount,
         (int) projectListCount);
     Project project = findProjectInList(tester, TEST_NEW_NAME);
 
     // Check if project found and check buttons
     index = findProjectIndexInList(tester, TEST_NEW_NAME);
     Assert.assertTrue(TEST_NEW_NAME + " project not found.", (index >= 0));
     tester.assertInvisible(PREFIX_LIST + index.toString() + EDIT_BUTTON);
     tester.assertInvisible(PREFIX_LIST + index.toString() + DELETE_BUTTON);
     tester.assertInvisible(PREFIX_LIST + index.toString() + RENAME_BUTTON);
     tester.assertVisible(PREFIX_LIST + index.toString() + LEAVE_BUTTON);
     tester.assertInvisible(PREFIX_LIST + index.toString() + REPLY_BUTTON);
 
     // Login as test user again
 
     tester.startPage(SigninPage.class);
     signinForm = tester.newFormTester(SIGNIN_FORM);
     signinForm.setValue(USER, TEST_USER);
     signinForm.setValue(PASSWORD, TEST_USER);
     signinForm.submit(SIGNIN);
     tester.assertRenderedPage(SensorDataPage.class);
 
     // Check if invitee is a member and not an invitee
     tester.clickLink(PROJECTS_LINK);
     project = findProjectInList(tester, TEST_NEW_NAME);
     Assert.assertEquals("Invitee should now be a member.", TEST_INVITEE, project.getMembers()
         .getMember().get(0));
     Assert.assertEquals("Invitee should not be an invitee.", 0, project.getInvitations()
         .getInvitation().size());
 
   }
 
   /**
    * Test the declining of an invitation to join a group.
    * 
    * @throws Exception when communication error occurs.
    */
   @Test
   public void testProjectsReplyDeclinePage() throws Exception { // NOPMD WicketTester has its own
                                                                 // assert classes.
 
     WicketTester tester = new WicketTester(new ProjectBrowserApplication(getTestProperties()));
 
     // Login and create new record with a test invitee
     login(tester);
     createNewProject(tester);
 
     // Login as test invitee
 
     tester.startPage(SigninPage.class);
     FormTester signinForm = tester.newFormTester(SIGNIN_FORM);
     signinForm.setValue(USER, TEST_INVITEE);
     signinForm.setValue(PASSWORD, TEST_INVITEE);
     signinForm.submit(SIGNIN);
     tester.assertRenderedPage(SensorDataPage.class);
 
     // Navigate to projects page and check that page is setup correctly
     tester.clickLink(PROJECTS_LINK);
     tester.assertRenderedPage(ProjectsPage.class);
     tester.assertComponent(PROJECTS_FORM, ProjectsForm.class);
     tester.assertVisible(LIST_PANEL);
     tester.assertInvisible(EDIT_PANEL);
     tester.assertInvisible(RENAME_PANEL);
     tester.assertInvisible(LEAVE_PANEL);
     tester.assertInvisible(DELETE_PANEL);
     tester.assertInvisible(REPLY_PANEL);
 
     // Store project count for later compare
     ListView listView = (ListView) tester
         .getComponentFromLastRenderedPage(PROJECT_TABLE);
     Integer previousListCount = listView.getList().size();
 
     // Check that all button invisible except reply button
     Integer index = findProjectIndexInList(tester, TEST_NEW_NAME);
     tester.assertInvisible(PREFIX_LIST + index.toString() + EDIT_BUTTON);
     tester.assertInvisible(PREFIX_LIST + index.toString() + DELETE_BUTTON);
     tester.assertInvisible(PREFIX_LIST + index.toString() + RENAME_BUTTON);
     tester.assertInvisible(PREFIX_LIST + index.toString() + LEAVE_BUTTON);
     tester.assertVisible(PREFIX_LIST + index.toString() + REPLY_BUTTON);
 
     // Push the edit button
     Button replyButton = (Button) tester
         .getComponentFromLastRenderedPage(PREFIX_LIST
             + index.toString() + REPLY_BUTTON);
     replyButton.onSubmit();
 
     // Assert correct panels are visible
     tester.assertComponent(REPLY_PANEL, ProjReplyPanel.class);
     tester.assertInvisible(EDIT_PANEL);
     tester.assertInvisible(LIST_PANEL);
     tester.assertInvisible(RENAME_PANEL);
     tester.assertInvisible(LEAVE_PANEL);
     tester.assertInvisible(DELETE_PANEL);
     tester.assertVisible(REPLY_PANEL);
 
     // Push save button
     Button declineButton = (Button) tester.getComponentFromLastRenderedPage(
       "projectsForm:projReplyPanel:projReplyForm:declineButton");
     declineButton.onSubmit();
     tester.newFormTester(PROJECTS_FORM).submit();
 
     // Check project count
     listView = (ListView) tester
         .getComponentFromLastRenderedPage(PROJECT_TABLE);
     Integer projectListCount = listView.getList().size();
     Assert.assertEquals("Should have one less project.", (int) previousListCount - 1,
         (int) projectListCount);
 
     // Check to see if project is gone.
     Project project = findProjectInList(tester, TEST_NEW_NAME);
     Assert.assertNull("Project should be gone, after decline.", project);
 
     // Login as test user again
 
     tester.startPage(SigninPage.class);
     signinForm = tester.newFormTester(SIGNIN_FORM);
     signinForm.setValue(USER, TEST_USER);
     signinForm.setValue(PASSWORD, TEST_USER);
     signinForm.submit(SIGNIN);
     tester.assertRenderedPage(SensorDataPage.class);
 
     // Check if invitee is a member and not an invitee
     tester.clickLink(PROJECTS_LINK);
     project = findProjectInList(tester, TEST_NEW_NAME);
     Assert.assertEquals("Invitee should not be a member.", 0, project.getMembers().getMember()
         .size());
     Assert.assertEquals("Invitee should not be an invitee.", 0, project.getInvitations()
         .getInvitation().size());
 
   }
 
   /**
    * Test leaving a project.
    * 
    * @throws Exception when communication error occurs.
    */
   @Test
   public void testProjectsLeavePage() throws Exception { // NOPMD WicketTester has its own assert
                                                           // classes.
 
     WicketTester tester = new WicketTester(new ProjectBrowserApplication(getTestProperties()));
 
     // Login and create new record with a test invitee
     login(tester);
     createNewProject(tester);
 
     // Login as test invitee
     tester.startPage(SigninPage.class);
     FormTester signinForm = tester.newFormTester(SIGNIN_FORM);
     signinForm.setValue(USER, TEST_INVITEE);
     signinForm.setValue(PASSWORD, TEST_INVITEE);
     signinForm.submit(SIGNIN);
     tester.assertRenderedPage(SensorDataPage.class);
 
     // Navigate to projects page and check that page is setup correctly
     tester.clickLink(PROJECTS_LINK);
     tester.assertRenderedPage(ProjectsPage.class);
     tester.assertComponent(PROJECTS_FORM, ProjectsForm.class);
     tester.assertVisible(LIST_PANEL);
     tester.assertInvisible(EDIT_PANEL);
     tester.assertInvisible(RENAME_PANEL);
     tester.assertInvisible(LEAVE_PANEL);
     tester.assertInvisible(DELETE_PANEL);
     tester.assertInvisible(REPLY_PANEL);
 
     // Store project count for later compare
     ListView listView = (ListView) tester
         .getComponentFromLastRenderedPage(PROJECT_TABLE);
     Integer previousListCount = listView.getList().size();
 
     // Check that all button invisible except reply button
     Integer index = findProjectIndexInList(tester, TEST_NEW_NAME);
     tester.assertInvisible(PREFIX_LIST + index.toString() + EDIT_BUTTON);
     tester.assertInvisible(PREFIX_LIST + index.toString() + DELETE_BUTTON);
     tester.assertInvisible(PREFIX_LIST + index.toString() + RENAME_BUTTON);
     tester.assertInvisible(PREFIX_LIST + index.toString() + LEAVE_BUTTON);
     tester.assertVisible(PREFIX_LIST + index.toString() + REPLY_BUTTON);
 
     // Push the edit button
     Button replyButton = (Button) tester
         .getComponentFromLastRenderedPage(PREFIX_LIST
             + index.toString() + REPLY_BUTTON);
     replyButton.onSubmit();
 
     // Assert correct panels are visible
     tester.assertComponent(REPLY_PANEL, ProjReplyPanel.class);
     tester.assertInvisible(EDIT_PANEL);
     tester.assertInvisible(LIST_PANEL);
     tester.assertInvisible(RENAME_PANEL);
     tester.assertInvisible(LEAVE_PANEL);
     tester.assertInvisible(DELETE_PANEL);
     tester.assertVisible(REPLY_PANEL);
 
     // Push save button
     Button acceptButton = (Button) tester
         .getComponentFromLastRenderedPage("projectsForm:projReplyPanel:projReplyForm:acceptButton");
     acceptButton.onSubmit();
     tester.newFormTester(PROJECTS_FORM).submit();
 
     // Check project count
     listView = (ListView) tester
         .getComponentFromLastRenderedPage(PROJECT_TABLE);
     Integer projectListCount = listView.getList().size();
     Assert.assertEquals("Should have same amount of projects.", (int) previousListCount,
         (int) projectListCount);
 
     Project project = findProjectInList(tester, TEST_NEW_NAME);
 
     // Check if project found and check buttons
     index = findProjectIndexInList(tester, TEST_NEW_NAME);
     Assert.assertTrue(TEST_NEW_NAME + " project not found.", (index >= 0));
     tester.assertInvisible(PREFIX_LIST + index.toString() + EDIT_BUTTON);
     tester.assertInvisible(PREFIX_LIST + index.toString() + DELETE_BUTTON);
     tester.assertInvisible(PREFIX_LIST + index.toString() + RENAME_BUTTON);
     tester.assertVisible(PREFIX_LIST + index.toString() + LEAVE_BUTTON);
     tester.assertInvisible(PREFIX_LIST + index.toString() + REPLY_BUTTON);
 
     // Push the leave button
     Button leaveButton = (Button) tester
         .getComponentFromLastRenderedPage(PREFIX_LIST
             + index.toString() + LEAVE_BUTTON);
     leaveButton.onSubmit();
 
     // Assert correct panels are visible
     tester.assertComponent(LEAVE_PANEL, ProjLeavePanel.class);
     tester.assertInvisible(EDIT_PANEL);
     tester.assertInvisible(LIST_PANEL);
     tester.assertInvisible(RENAME_PANEL);
     tester.assertVisible(LEAVE_PANEL);
     tester.assertInvisible(DELETE_PANEL);
     tester.assertInvisible(REPLY_PANEL);
 
     // Push cancel button
     Button cancelLeaveButton = (Button) tester
         .getComponentFromLastRenderedPage("projectsForm:projLeavePanel:projLeaveForm:cancelButton");
     cancelLeaveButton.onSubmit();
     tester.newFormTester(PROJECTS_FORM).submit();
 
     // Push the delete button again
     index = findProjectIndexInList(tester, TEST_NEW_NAME);
     leaveButton = (Button) tester
         .getComponentFromLastRenderedPage(PREFIX_LIST
             + index.toString() + LEAVE_BUTTON);
     leaveButton.onSubmit();
 
     // Push save button
     Button leaveLeaveButton = (Button) tester
         .getComponentFromLastRenderedPage("projectsForm:projLeavePanel:projLeaveForm:leaveButton");
     leaveLeaveButton.onSubmit();
     tester.newFormTester(PROJECTS_FORM).submit();
 
     // Assert correct panels are visible
     tester.assertComponent(LIST_PANEL, ProjListPanel.class);
     tester.assertInvisible(EDIT_PANEL);
     tester.assertVisible(LIST_PANEL);
     tester.assertInvisible(RENAME_PANEL);
     tester.assertInvisible(LEAVE_PANEL);
     tester.assertInvisible(DELETE_PANEL);
     tester.assertInvisible(REPLY_PANEL);
 
     // Check to see if project is gone.
     project = findProjectInList(tester, TEST_NEW_NAME);
     Assert.assertNull("Project should be gone, after decline.", project);
 
     // Login as test user again
     tester.startPage(SigninPage.class);
     signinForm = tester.newFormTester(SIGNIN_FORM);
     signinForm.setValue(USER, TEST_USER);
     signinForm.setValue(PASSWORD, TEST_USER);
     signinForm.submit(SIGNIN);
     tester.assertRenderedPage(SensorDataPage.class);
 
     // Check if invitee is a member and not an invitee
     tester.clickLink(PROJECTS_LINK);
     project = findProjectInList(tester, TEST_NEW_NAME);
     Assert.assertEquals("Invitee should not be a member.", 0, project.getMembers().getMember()
         .size());
     Assert.assertEquals("Invitee should not be an invitee.", 0, project.getInvitations()
         .getInvitation().size());
   }
 }
