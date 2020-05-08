 package view;
 
 import java.awt.BorderLayout;
 import java.awt.CardLayout;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.JTextPane;
 import javax.swing.SwingUtilities;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import model.Schedule;
 import model.Task;
 import model.scheduleralgorithms.SupportedSchedulers;
 import model.scheduleralgorithms.SupportedSchedulers.SUPPORTED_SCHEDULING_ALGORITHMS;
 import output.OutputIpe;
 
 /**
  * ScheduleToIpe is a GUI program that allows a user to create
  * a taskset and then have it scheduled by different algorithms.
  * The output is an IPE file.
  * 
  * @author Thom Castermans
  */
 public class ScheduleToIpe extends JFrame {
 
 	/**
 	 * Serial version UID.
 	 */
 	private static final long serialVersionUID = -4434864814684611817L;
 	/** Text to display in list when no tasks are available yet. */
 	private static final String NO_TASKS_TEXT = "No tasks created yet.";
 	/** Text to display in info pane when no tasks are available yet. */
 	private static final String NO_TASKS_INFO_TEXT = "Here, information about created tasks will appear. " +
 			"You should first create a task and select it on the left.\n\n" +
 			"When you have selected a task, you will be able to change its properties here.";
 	
 	// For CardLayout
 	private static final String CARD_NO_TASK_PANEL = "NoTasksCard";
 	private static final String CARD_TASK_INFO_PANEL = "TaskInfoCard";
 	
 	/** Panel that contains both a text about no tasks being created and a panel with information. */
 	private JPanel rightPanel;
 	/** List with created tasks. */
 	private JList<String> taskList;
 	/** Model for list with created tasks. */
 	private DefaultListModel<String> taskListModel;
 	/** Listener for list with created tasks. */
 	private ListSelectionListener taskListSelectionListener = new ListSelectionListener() {
 		@Override
 		public void valueChanged(ListSelectionEvent e) {
 			if (createdTasks.size() > 0 && taskListModel.size() > 0 && taskList.getSelectedIndex() >= 0) {
 				showInfoAbout(taskListModel.getElementAt(taskList.getSelectedIndex()));
 				removeTaskButton.setEnabled(true);
 			} else {
 				((CardLayout) rightPanel.getLayout()).show(rightPanel, CARD_NO_TASK_PANEL);
 			}
 		}
 	};
 	/** "Add task" button. */
 	JButton addTaskButton;
 	/** Listener for "Add task" button. */
 	ActionListener addTaskButtonListener = new ActionListener() {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			// Ask for task name
 			String name = JOptionPane.showInputDialog(ScheduleToIpe.this, "Enter a unique name for the task to be created.");
 			// Check name
 			if (name.length() == 0) {
 				JOptionPane.showMessageDialog(ScheduleToIpe.this, "A task should have a name of at least one character.",
 						"ScheduleToIpe - Error!", JOptionPane.ERROR_MESSAGE);
 				return;
 			}
 			// Check if we do not have a task with that name already
 			for (Task t : createdTasks) {
 				if (t.getName().equals(name)) {
 					JOptionPane.showMessageDialog(ScheduleToIpe.this, "A task with the name \"" + name + "\" already exists.",
 							"ScheduleToIpe - Error!", JOptionPane.ERROR_MESSAGE);
 					return;
 				}
 			}
 			// Remove "NO_TASKS_TEXT" from list
 			if (createdTasks.size() == 0)  taskListModel.removeAllElements();
 			// Add task
 			taskListModel.addElement(name);
 			createdTasks.add(new Task(name, 5, 5, 2));
 			exportScheduleButton.setEnabled(true);
 			// Update list
 			taskList.setSelectedIndex(taskListModel.getSize() - 1);
 			taskList.repaint();
 		}
 	};
 	/** "Remove task" button. */
 	JButton removeTaskButton;
 	/** Listener for "Remove task" button. */
 	ActionListener removeTaskButtonListener = new ActionListener() {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			// Check if there is actually a task selected... should be the case :)
 			int prevSelected = taskList.getSelectedIndex();
 			if (prevSelected == -1)  return;
 			// Remove tasks from internal set
 			for (Task t : createdTasks) {
 				if (t.getName().equals(taskListModel.getElementAt(taskList.getSelectedIndex()))) {
 					createdTasks.remove(t);
 					break;
 				}
 			}
 			// Remove task from model, add special text if no tasks left
 			taskListModel.remove(taskList.getSelectedIndex());
 			if (taskListModel.size() == 0) {
 				taskListModel.addElement(NO_TASKS_TEXT);
 				exportScheduleButton.setEnabled(false);
 			}
 			// Select next task in list, or disable remove task button as no task can be removed
 			if (createdTasks.size() > 0) {
 				if (taskListModel.size() > prevSelected) {
 					taskList.setSelectedIndex(prevSelected);
 				} else {
 					taskList.setSelectedIndex(0);
 				}
 			} else {
 				removeTaskButton.setEnabled(false);
 			}
 		}
 	};
 	/** "Export schedule" button. */
 	private JButton exportScheduleButton;
 	/** Listener for "Export schedule" button. */
 	ActionListener exportScheduleButtonListener = new ActionListener() {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			Schedule schedule = SupportedSchedulers.createSchedule(
 					createdTasks,
 					SUPPORTED_SCHEDULING_ALGORITHMS.valueOf(
 							inputTaskSchedulingAlgorithm.getItemAt(
 									inputTaskSchedulingAlgorithm.getSelectedIndex()
 								)
 						)
 				);
 			outputIpe.outputIpeFile(schedule);
 			//System.out.println(schedule);
 		}
 	};
 	/** Input with name of task that is currenlty edited. */
 	private JTextField inputTaskName;
 	/** Input with period of task that is currenlty edited. */
 	private JTextField inputTaskPeriod;
 	/** Input with deadline of task that is currenlty edited. */
 	private JTextField inputTaskDeadline;
 	/** Input with execution time of task that is currenlty edited. */
 	private JTextField inputTaskExecutionTime;
 	/** Dropdown select with available algorithms. */
 	private JComboBox<String> inputTaskSchedulingAlgorithm;
 	
 	/** Tasks created by the user. */
 	Set<Task> createdTasks;
 	/** Object used to output an Ipe-readable file. */
 	OutputIpe outputIpe;
 
 	/**
 	 * Start the GUI program.
 	 * 
 	 * @param args Command-line arguments.
 	 */
 	public static void main(String[] args) {
 		SwingUtilities.invokeLater(new Runnable() {
 			
 			@Override
 			public void run() {
 				ScheduleToIpe sti = new ScheduleToIpe();
 				sti.setVisible(true);
 			}
 		});
 	}
 	
 	/**
 	 * Create a new ScheduleToIpe GUI window.
 	 */
 	public ScheduleToIpe() {
 		createdTasks = new HashSet<Task>();
 		try {
 			outputIpe = new OutputIpe(new File(System.getProperty("user.home") + "/out.ipe"));
 		} catch (FileNotFoundException e) {
 			outputIpe = new OutputIpe();
 		}
 		
 		setTitle("ScheduleToIpe");
 		setSize(new Dimension(800, 600));
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setLayout(new GridLayout());
 		
 		// Leftpanel
 		JPanel leftPanel = new JPanel();
 		leftPanel.setLayout(new BorderLayout());
 		// list of created tasks
 		taskListModel = new DefaultListModel<String>();
 		taskListModel.addElement(NO_TASKS_TEXT);
 		taskList = new JList<String>(taskListModel);
 		taskList.addListSelectionListener(taskListSelectionListener);
 		leftPanel.add(new JScrollPane(taskList), BorderLayout.CENTER);
 		// buttons to create and delete tasks
 		JPanel controlPanel = new JPanel();
 		controlPanel.setLayout(new GridLayout(3, 1));
 		addTaskButton = new JButton("Add task");
 		addTaskButton.addActionListener(addTaskButtonListener);
 		controlPanel.add(addTaskButton);
 		removeTaskButton = new JButton("Remove selected task");
 		removeTaskButton.addActionListener(removeTaskButtonListener);
 		removeTaskButton.setEnabled(false);
 		controlPanel.add(removeTaskButton);
 		JPanel exportPanel = new JPanel(new BorderLayout());
 		inputTaskSchedulingAlgorithm = new JComboBox<String>();
 		for (SUPPORTED_SCHEDULING_ALGORITHMS algorithm : SUPPORTED_SCHEDULING_ALGORITHMS.values()) {
 			inputTaskSchedulingAlgorithm.addItem(algorithm.toString());
 		}
 		exportPanel.add(inputTaskSchedulingAlgorithm, BorderLayout.WEST);
 		exportScheduleButton = new JButton("Export schedule to Ipe");
 		exportScheduleButton.addActionListener(exportScheduleButtonListener);
 		exportScheduleButton.setEnabled(false);
 		exportPanel.add(exportScheduleButton, BorderLayout.CENTER);
 		controlPanel.add(exportPanel);
 		leftPanel.add(controlPanel, BorderLayout.PAGE_END);
 		add(leftPanel);
 		
 		// Rightpanel
 		rightPanel = new JPanel(new CardLayout());
 		JPanel noTasksPanel = new JPanel(new BorderLayout());
 		JTextPane noTasksPane = new JTextPane();
 		noTasksPane.setEditable(false);
 		noTasksPane.setText(NO_TASKS_INFO_TEXT);
 		noTasksPanel.add(noTasksPane, BorderLayout.NORTH);
 		rightPanel.add(noTasksPanel, CARD_NO_TASK_PANEL);
 		JPanel taskInfoPanel = new JPanel(new BorderLayout());
 		JPanel formPanel = new JPanel(new GridLayout(9, 1));
 		formPanel.add(new JLabel("The name of the task:"));
 		inputTaskName = new JTextField();
 		formPanel.add(inputTaskName);
 		formPanel.add(new JLabel("The period (T) of the task:"));
 		inputTaskPeriod = new JTextField();
 		formPanel.add(inputTaskPeriod);
 		formPanel.add(new JLabel("The deadline (D) of the task:"));
 		inputTaskDeadline = new JTextField();
 		formPanel.add(inputTaskDeadline);
 		formPanel.add(new JLabel("The execution time (C) of the task:"));
 		inputTaskExecutionTime = new JTextField();
 		formPanel.add(inputTaskExecutionTime);
 		JPanel saveButtonPanel = new JPanel(new BorderLayout());
 		JButton saveButton = new JButton("Save");
 		saveButton.addActionListener(new ActionListener() {
 			@SuppressWarnings("boxing")
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// Find "current" task
 				Task t = null;
 				boolean canChangeName = true;
 				for (Task tt : createdTasks) {
 					if (tt.getName().equals(taskListModel.elementAt(taskList.getSelectedIndex()))) {
 						t = tt;
 					} else if (tt.getName().equals(inputTaskName.getText())) {
 						canChangeName = false;
 					}
 				}
 				if (t == null)  return;
 				if (canChangeName) {
					String oldName = t.getName();
 					t.setName(inputTaskName.getText());
					taskListModel.setElementAt(t.getName(), taskListModel.indexOf(oldName));
					taskList.repaint();
 				} else {
 					inputTaskName.setText(t.getName());
 				}
 				t.setPeriod(Integer.valueOf(inputTaskPeriod.getText()));
 				t.setDeadline(Integer.valueOf(inputTaskDeadline.getText()));
 				t.setExecutionTime(Double.valueOf(inputTaskExecutionTime.getText()));
 			}
 		});
 		saveButtonPanel.add(saveButton, BorderLayout.EAST);
 		formPanel.add(saveButtonPanel);
 		taskInfoPanel.add(formPanel, BorderLayout.NORTH);
 		rightPanel.add(taskInfoPanel, CARD_TASK_INFO_PANEL);
 		((CardLayout) rightPanel.getLayout()).show(rightPanel, CARD_NO_TASK_PANEL);
 		add(rightPanel);
 	}
 	
 	/**
 	 * Show information about a given task.
 	 * 
 	 * @param taskName Name of the task.
 	 */
 	private void showInfoAbout(String taskName) {
 		((CardLayout) rightPanel.getLayout()).show(rightPanel, CARD_TASK_INFO_PANEL);
 		Task t = null;
 		for (Task tt : createdTasks) {
 			if (tt.getName().equals(taskName)) {
 				t = tt;
 				break;
 			}
 		}
 		if (t == null)  return;
 		// Set values in inputs
 		inputTaskName.setText(t.getName());
 		inputTaskPeriod.setText(t.getPeriod() + "");
 		inputTaskDeadline.setText(t.getDeadline() + "");
 		inputTaskExecutionTime.setText(t.getExecutionTime() + "");
 		rightPanel.repaint();
 	}
 }
