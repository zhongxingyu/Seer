 package view;
 
 import java.awt.Dimension;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 
 import javax.swing.JPanel;
 import utility.*;
 import model.TaskModel;
 import net.miginfocom.swing.MigLayout;
 
 import javax.swing.JFrame;
 import javax.swing.JTextField;
 import javax.swing.JLabel;
 import javax.swing.JButton;
 import controller.*;
 import javax.swing.JTextArea;
 import javax.swing.JEditorPane;
 import javax.swing.border.SoftBevelBorder;
 import javax.swing.border.BevelBorder;
 import javax.swing.UIManager;
 import calendar.*;
 
 /**
  * A class to represent the view where you can edit a task.
  * 
  * @author Hanna
  *
  */
 public class EditTaskPanel extends JPanel {
 	
 	private JTextField titleTextField;
 	private JEditorPane descriptionTextField;
 	private TaskModel taskModel;
 	private JButton cancelButton;
 	private JButton saveButton;
 	private DisplayTaskPanel displayTaskPanel;
 	private JTextField deadlineInput;
 	private JDateTextField currentDeadlineLabel= new JDateTextField();
 	private JLabel deadlineLabel;
 	private JButton calendarButton;
 	private JCalendar calendar;
 	/**
 	 * Create the panel.
 	 */
 	public EditTaskPanel(TaskModel taskModel) {
 		
 		this.setMinimumSize(new Dimension (300, 580));
 		this.setMaximumSize(new Dimension (300, 580));
 		this.setPreferredSize(new Dimension (300, 580));
 		this.taskModel = taskModel;
 		this.setBackground(GraphicConstants.BACKGROUND);
 		setLayout(new MigLayout("", "[][283.00,grow]", "[][][][][][][][][][][]"));
 		
 		JLabel changeTitleLabel = new JLabel("Change title:");
 		changeTitleLabel.setFont(GraphicConstants.REGULARFONT);
 		add(changeTitleLabel, "cell 0 1,alignx right");
 		
 		titleTextField = new JTextField(taskModel.getTitle());
 		titleTextField.setFont(GraphicConstants.REGULARFONT);
 		add(titleTextField, "cell 1 1,alignx left");
 		titleTextField.setColumns(10);
 		
 		JLabel changeDescriptionLabel = new JLabel("Change description:");
 		changeDescriptionLabel.setFont(GraphicConstants.REGULARFONT);
 		add(changeDescriptionLabel, "cell 0 3,alignx right,aligny top");
 		
 		descriptionTextField = new JEditorPane();
 		descriptionTextField.setText(taskModel.getDescription());
 		descriptionTextField.setBorder(UIManager.getBorder("TextField.border"));
 		descriptionTextField.setPreferredSize(new Dimension(150, 90));
 		descriptionTextField.setFont(GraphicConstants.REGULARFONT);
 		add(descriptionTextField, "cell 1 3,alignx left");
 		
 		JLabel changePriorityLabel = new JLabel("Change priority:");
 		changePriorityLabel.setFont(GraphicConstants.REGULARFONT);
 		add(changePriorityLabel, "cell 0 5,alignx right");
 		
 		deadlineLabel = new JLabel("Current Deadline:");
 		deadlineLabel.setFont(GraphicConstants.REGULARFONT);
 		add(deadlineLabel, "cell 0 7,alignx right");
 		
 		setCurrentDeadlineLabel(currentDeadlineLabel);
 		currentDeadlineLabel.setFont(GraphicConstants.REGULARFONT);
 		add(currentDeadlineLabel, "cell 1 7,alignx center");
 		
 		JLabel changeDeadlineLabel = new JLabel("Change deadline:");
 		changeDeadlineLabel.setFont(GraphicConstants.REGULARFONT);
 		add(changeDeadlineLabel, "cell 0 8,alignx trailing");
 		
 		calendar = new JCalendar(null, "Calendar", true, calendar.RIGHT_SPINNER);
 		calendarButton = new JButton("Set date");
 		calendarButton.setActionCommand("calendar");
 		add(calendarButton, "cell 1 8,growx");
 		//deadlineInput.setColumns(10);
 		
 		cancelButton = new JButton("cancel");
 		cancelButton.setFont(GraphicConstants.REGULARFONT);
 		cancelButton.setActionCommand("cancel");
 		add(cancelButton, "flowx,cell 1 10,alignx right");
 		
 		saveButton = new JButton("save");
 		saveButton.setFont(GraphicConstants.REGULARFONT);
 		saveButton.setActionCommand("save");
 		add(saveButton, "cell 1 10,alignx right");
 		
 	
 
 	}
 	public JCalendar getCalendar(){
 		return calendar;
 	}
 
 	public void setCurrentDeadlineLabel(JDateTextField currentDeadline){
		//String date = new SimpleDateFormat("yyyyMMdd").format(calendar);
 		//currentDeadline = new JLabel (date);
		currentDeadline.setFormat(currentDeadlineLabel.US_SHORT_DATE);
 		currentDeadlineLabel=currentDeadline;
 	}
 	public JDateTextField getCurrentDeadlineLabel(){
 		return currentDeadlineLabel;
 	}
 	
 	public DisplayTaskPanel getDisplayTaskPanel(){
 		return displayTaskPanel;
 	}
 
 	public String getTitleTextField(){
 		return titleTextField.getText();
 	}
	
 	public String getDescriptionTextField(){
 		return descriptionTextField.getText();
 	}
 	
 	public TaskModel getTaskModel(){
 		return taskModel;
 	}
 	public String deadline(){
 		return deadlineInput.toString();
 	}
 	public void setController(EditController controller){
 		saveButton.addActionListener(controller);
 		cancelButton.addActionListener(controller);
 		calendarButton.addActionListener(controller);
 	}
 
 }
