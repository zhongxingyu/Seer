 package ui.swing.mainScreen.tasks;
 
 import jira.JiraIssue;
 import jira.JiraIssueData;
 
 import org.junit.After;
 import org.junit.Test;
 import org.netbeans.jemmy.operators.JDialogOperator;
 
 import tasks.adapters.ui.operators.TaskScreenOperator;
 import ui.swing.mainScreen.tasks.mock.MockJira;
 import ui.swing.mainScreen.tasks.mock.MockTask;
 import ui.swing.mainScreen.tasks.mock.MockTaskScreenModel;
 import ui.swing.presenter.PresenterImpl;
 import ui.swing.utils.UIEventsExecutorImpl;
 import basic.FormatterImpl;
 
 public class TaskScreenTest {
 
 	MockJira mockJira = new MockJira();
 	MockTaskScreenModel mockModel = new MockTaskScreenModel();
 	PresenterImpl presenter = new PresenterImpl(new UIEventsExecutorImpl(null));
 	TaskScreenController controller = new TaskScreenController(new FormatterImpl(), mockModel, presenter, mockJira, null);
 
 	@Test
 	public void testTaskNameAutoCompleteFromJira() {
 
 		controller.createTask();
 
 		TaskScreenOperator operator = new TaskScreenOperator();
 		operator.setJiraKey("jira-issue-key");
		operator.waitName("[jira-issue-key] jira-issue-summary");
 
 	}
 
 	@Test
 	public void testTaskNameAutoCompleteFromJiraOnlyIfNameFieldIsEmpty() {
 
 		controller.createTask();
 
 		TaskScreenOperator operator = new TaskScreenOperator();
 		operator.setTaskName("foobar");
 		operator.setJiraKey("test");
		operator.waitName("foobar");
 
 	}
 
 	@Test
 	public void testCreateTaskWithJiraIssue() {
 
 		controller.createTask();
 
 		TaskScreenOperator operator = new TaskScreenOperator();
 		operator.setJiraKey("test");
 		operator.setTaskName("name");
 		operator.clickOk();
 
 		mockModel.waitCreatedTaskWithJiraId("test");
 
 	}
 
 	@Test
 	public void testEditTaskWithJiraIssue() {
 
 		MockTask mockTask = createMockTaskWithJiraKey("foobar");
 		mockModel.setSelectedTask(mockTask);
 		controller.editSelectedTask();
 
 		TaskScreenOperator operator = new TaskScreenOperator();
 		operator.waitJiraKey("foobar");
 
 		operator.setJiraKey("foobarbaz");
 		operator.clickOk();
 		mockModel.waitCreatedTaskWithJiraId("foobarbaz");
 
 	}
 	
 	@Test
	public void addJiraIssueToExistingTask_ShouldUpdateNameIfEmpty() {

		MockTask mockTask = createMockTaskWithJiraKey("foobar");
		mockModel.setSelectedTask(mockTask);
		controller.editSelectedTask();

		TaskScreenOperator operator = new TaskScreenOperator();
		operator.setTaskName("");
		operator.setJiraKey("foobarbaz");
		operator.waitName("[foobarbaz] jira-issue-summary");

	}
	
	@Test
 	public void onEmptyTaskname_ShouldNotSaveTask(){
 		controller.createTask();
 		TaskScreenOperator operator = new TaskScreenOperator();
 		operator.clickOk();
 		JDialogOperator dialog = new JDialogOperator("Message");
 		dialog.close();
 	}
 
 	private static MockTask createMockTaskWithJiraKey(String jiraKey) {
 		MockTask mockTask = new MockTask();
 		JiraIssueData data = new JiraIssueData();
 		data.setKey(jiraKey);
 		mockTask.setJiraIssue(new JiraIssue(data));
 		return mockTask;
 	}
 
 	@After
 	public void tearDown() {
 		presenter.stop();
 	}
 
 }
