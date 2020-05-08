 package ui.swing.mainScreen.tasks;
 
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusAdapter;
 import java.awt.event.FocusEvent;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.util.concurrent.Callable;
 
 import javax.swing.BorderFactory;
 import javax.swing.JCheckBox;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.SwingUtilities;
 
 import jira.Jira;
 import jira.JiraIssue;
 import jira.JiraUtil;
 import jira.exception.JiraIssueNotFoundException;
 import jira.exception.JiraOptionsNotSetException;
 import lang.Maybe;
 
 import org.apache.commons.lang.StringUtils;
 
 import tasks.TaskView;
 import tasks.home.TaskData;
 import ui.swing.presenter.ActionPane;
 import ui.swing.presenter.Presenter;
 import ui.swing.presenter.UIAction;
 import ui.swing.presenter.ValidationException;
 import ui.swing.tasks.SelectedTaskSource;
 import ui.swing.utils.SwingUtils;
 import basic.Formatter;
 import basic.NonEmptyString;
 
 import com.jgoodies.forms.builder.DefaultFormBuilder;
 import com.jgoodies.forms.layout.FormLayout;
 
 public class TaskScreenController {
 
 	public static final String TITLE = "Task edition";
 
 	private final Formatter formatter;
 	private final TaskScreenModel model;
 	private final Presenter presenter;
 	private final Jira jira;
 	private final SelectedTaskSource selectedTaskSource;
 	
 	public TaskScreenController(final Formatter formatter,
 			final TaskScreenModel model, final Presenter presenter,
 			final Jira jira, SelectedTaskSource selectedTaskSource) {
 		this.model = model;
 		this.formatter = formatter;
 		this.presenter = presenter;
 		this.jira = jira;
 		this.selectedTaskSource = selectedTaskSource;
 	}
 
 	public void createTaskStarted(final long time) {
 		internalShow(null, Maybe.wrap(Long.valueOf(time)));
 	}
 
 	public void editSelectedTask() {
 		final TaskView selectedTask = model.selectedTask();
 		if (selectedTask == null) {
 			internalShow(((Maybe<TaskView>) null), null);
 		} else {
 			internalShow(Maybe.wrap(selectedTask), null);
 		}
 	}
 
 	public void createTask() {
 		internalShow(null, null);
 	}
 
 	private void internalShow(final Maybe<TaskView> task,
 			final Maybe<Long> start) {
 		SwingUtilities.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				showOkCancelDialog(task, start);
 			}
 		});
 	}
 
 	void showOkCancelDialog(final Maybe<TaskView> task, final Maybe<Long> start) {
 		presenter.showOkCancelDialog(new TaskScreenDialog(task, start),
 				TITLE);
 	}
 
 	class TaskScreenDialog implements ActionPane {
 		private JTextField taskNameTextBox;
 		private JCheckBox budgetCheckBox;
 		private JTextField budgetHours;
 		private TaskView taskView;
 		private JTextField jiraIssueKeyTextField;
 		private Maybe<JiraIssue> jiraIssue;
 
 		private final Maybe<TaskView> maybeTaskView;
 
 		private final Maybe<Long> time;
 		private String errorMessage;
 
 		public TaskScreenDialog(final Maybe<TaskView> maybeTaskView,
 				final Maybe<Long> time) {
 			this.maybeTaskView = maybeTaskView;
 			this.time = time;
 			if (maybeTaskView != null)
 				this.jiraIssue = maybeTaskView.unbox().getJiraIssue();
 		}
 
 		@Override
 		public JPanel getPanel() {
 			final JPanel dialog = createDialog();
 		
 			taskNameTextBox.requestFocus();
 		
 			if (maybeTaskView == null) {
 				return dialog;
 			}
 		
 			taskView = maybeTaskView.unbox();
 			if (taskView.getJiraIssue() != null) {
 				jiraIssueKeyTextField.setText(taskView.getJiraIssue().unbox()
 						.getKey());
 			}
 			taskNameTextBox.setText(taskView.name());
 			if (taskView.budgetInHours() != null) {
 				budgetCheckBox.setSelected(true);
 				budgetHours.setText(formatter.formatNumber(taskView
 						.budgetInHours()));
 			} else {
 				budgetCheckBox.setSelected(false);
 				budgetHours.setText("");
 			}
 		
 			taskNameTextBox.selectAll();
 		
 			return dialog;
 		}
 
 		@Override
 		public UIAction action() {
 			return new UIAction() {
 		
 				@Override
 				public void run() throws ValidationException {
 					runUIActionTaskScreenDialog();
 				}
 			};
 		}
 
 		private JPanel createDialog() {
 
 			taskNameTextBox = new JTextField(30);
 			budgetCheckBox = new JCheckBox("Budget");
 			budgetHours = new JTextField(5);
 			jiraIssueKeyTextField = new JTextField(30);
 			addIssueKeyTextFieldListeners();
 
 			final FormLayout layout = new FormLayout("pref, 3dlu, left:pref");
 			final DefaultFormBuilder builder = new DefaultFormBuilder(layout);
 
 			builder.append("Issue key", jiraIssueKeyTextField);
 			builder.append("Task name", taskNameTextBox);
 			builder.nextLine();
 			builder.append(budgetCheckBox);
 			builder.append(budgetHours);
 
 			final JPanel fieldsPanel1 = builder.getPanel();
 			fieldsPanel1.setBorder(BorderFactory.createEmptyBorder(20, 15, 15,
 					15));
 			return fieldsPanel1;
 
 		}
 
 		private void addIssueKeyTextFieldListeners() {
 			jiraIssueKeyTextField.addActionListener(new ActionListener() {
 				@Override
 				public void actionPerformed(final ActionEvent e) {
 					loadIssue();
 				}
 			});
 
 			jiraIssueKeyTextField.addKeyListener(new KeyAdapter() {
 				@Override
 				public void keyTyped(final KeyEvent e) {
 					if ('\n' != e.getKeyChar()) setBackgroundWhiteJiraIssueKeyTextField();
 				}
 			});
 
 			jiraIssueKeyTextField.addFocusListener(new FocusAdapter() {
 
 				@Override
 				public void focusLost(FocusEvent e) {
 					onFocusLostJiraIssueKeyTextField();
 				}
 			});
 		}
 
 		void loadIssue() {
 			final String key = jiraIssueKeyTextField.getText();
 			if (key.isEmpty()) {
 				jiraIssue = null;
 				return;
 			}
 
 			try {
 				jiraIssue = Maybe.wrap(jira.getIssueByKey(key));
				if (taskNameTextBox.getText().equals("")) {
 					taskNameTextBox.setText(JiraUtil.getIssueDescription(jiraIssue
 										.unbox()));
 				}
 				jiraIssueKeyTextField.setBackground(new Color(0xAAFFAA));
 
 			} catch (final JiraIssueNotFoundException ex) {
 				jiraIssue = null;
 				JOptionPane.showMessageDialog(taskNameTextBox,
 						"Issue with key '" + key + "' not found");
 				if (maybeTaskView == null) {
 					taskNameTextBox.setText("");
 				}
 				jiraIssueKeyTextField.setBackground(new Color(0xFFAAAA));
 			} catch (final JiraOptionsNotSetException ex) {
 				JOptionPane.showMessageDialog(taskNameTextBox,
 						"Jira options not set");
 			}
 		}
 
 		private Double getBudget() {
 			final String budgetHoursText = budgetHours.getText();
 			if (StringUtils.isNumeric(budgetHoursText)
 					&& !budgetHoursText.isEmpty()) {
 				return Double.valueOf(budgetHoursText);
 			}
 			return null;
 		}
 		
 		private Maybe<TaskData> getDataInSwingThread(){
 			return SwingUtils.getOrCry(new Callable<Maybe<TaskData>>() {
 				@Override
 				public Maybe<TaskData> call() throws Exception {
 					return internalGetTaskData();
 				}
 			});
 			
 		}
 
 		Maybe<TaskData> internalGetTaskData(){
 			final String taskName = taskNameTextBox.getText();
 			if (taskName.isEmpty()){
 				errorMessage = 
 						"Task name must not be blank";
 				return null;
 			}
 			
 			TaskData data = new TaskData(new NonEmptyString(taskName));
 			data.setBudget(getBudget());
 			
 			if (jiraIssue == null || !(jiraIssue.unbox().getKey().equals(jiraIssueKeyTextField.getText()))){						
 				loadIssue();
 			}
 			
 			if(jiraIssue != null)
 				data.setJiraIssue(jiraIssue.unbox());
 			
 			return Maybe.wrap(data);
 		}
 
 		void runUIActionTaskScreenDialog() throws ValidationException {
 			Maybe<TaskData> maybeData = getDataInSwingThread();
 			if (maybeData == null)
 				throw new ValidationException(errorMessage);
 			
 			TaskData data = maybeData.unbox();
 
 			if (taskView != null) {
 				model.editTask(taskView, data);
 			} else if (time == null) {
 				model.createTask(data);
 			} else {
 				model.createTaskAndStart(data, time.unbox());
 			}
 			selectedTaskSource.supply(taskView);
 		}
 
 		void onFocusLostJiraIssueKeyTextField() {
 			if (jiraIssue == null)
 				loadIssue();
 			else if (!jiraIssue.unbox().getKey().equals(
 					jiraIssueKeyTextField.getText()))
 				loadIssue();
 		}
 
 		void setBackgroundWhiteJiraIssueKeyTextField() {
 			jiraIssueKeyTextField.setBackground(Color.WHITE);
 		}
 	}
 
 }
