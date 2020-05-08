 package views.dataentry;
 
 import controllers.TaskCtrl;
 import controllers.TimeSheetCtrl;
 import models.DataEntry;
 import models.Task;
 import models.TimeSheet;
 import models.User;
 import utils.Logging;
 import utils.UserSession;
 import views.dataentry.CreateDataEntryUI;
 import views.shared.DateTimePanel;
 
 import javax.swing.*;
 import javax.swing.border.EmptyBorder;
 import javax.swing.border.LineBorder;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 
 public class CreateDataEntryUI
 {
 
     private static JFrame _frame;
     private static CreateDataEntryUI _instance;
     private JPanel contentPane;
     
 	private JTextPane txtRemark;
 	private JTextField txtTitle;
 	private JTextPane txtDescription;
 	
     private TimeSheetCtrl _tsCtrl;
     private TaskCtrl _taCtrl;
     private static TimeSheet _ts; // timesheet needs to be imported from super, when we open this class.
     
     private JComboBox<String> drpXTask;
     private DateTimePanel startDate;
     private DateTimePanel endDate;
 	private DefaultComboBoxModel<String> _model;
 
     private JPanel task;
 
 	public static JFrame createWindow(TimeSheet timeSheet)
     {
 		if(_instance == null)
 		{
 			_instance = new CreateDataEntryUI();
 			_ts = timeSheet;
 		}
 			
 		return _frame;
     }
 
     private CreateDataEntryUI()
     {
     	createElements();	
     }
 
 	public void createElements() 
 	{
 		_tsCtrl = new TimeSheetCtrl();
 		_taCtrl = new TaskCtrl();
 		
 		_frame = new JFrame();
         _frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         _frame.setTitle("Ny Registrering");
 		_frame.setBounds(100, 100, 320, 327);
		_frame.setAlwaysOnTop(true);
         _frame.setResizable(false);
         _frame.setVisible(true);
         _frame.setLocationRelativeTo(null);
         
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		contentPane.setLayout(null);
 		_frame.setContentPane(contentPane);
 		
 		_frame.setContentPane(contentPane);
 		contentPane.setLayout(null);
 		
 		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
 		tabbedPane.setBounds(10, 10, 295, 230);
 		contentPane.add(tabbedPane);
 		
 		JPanel dataEntry = new JPanel();
 		dataEntry.setFont(new Font("Dialog", Font.PLAIN, 12));
 		tabbedPane.addTab("Registrering", null, dataEntry, null);
 		dataEntry.setLayout(null);
 		
 		JLabel lblTask = new JLabel("Opgave:");
 		lblTask.setFont(new Font("Dialog", Font.PLAIN, 12));
 		lblTask.setBounds(10, 11, 70, 14);
 		dataEntry.add(lblTask);
 		
 		drpXTask = new JComboBox<String>();
 		drpXTask.setFont(new Font("Dialog", Font.PLAIN, 12));
 		drpXTask.setBounds(95, 8, 183, 20);
 		dataEntry.add(drpXTask);
 		_model = new DefaultComboBoxModel<String>(addTasks());
         drpXTask.setModel(_model);
 		
 		JSeparator separator = new JSeparator();
 		separator.setBounds(10, 37, 268, 1);
 		dataEntry.add(separator);
 		
 		JLabel lblStarted = new JLabel("P\u00E5begyndt:");
 		lblStarted.setFont(new Font("Dialog", Font.PLAIN, 12));
 		lblStarted.setBounds(10, 50, 70, 14);
 		dataEntry.add(lblStarted);
 		
 		JLabel lblEnded = new JLabel("Afsluttet:");
 		lblEnded.setFont(new Font("Dialog", Font.PLAIN, 12));
 		lblEnded.setBounds(10, 75, 70, 14);
 		dataEntry.add(lblEnded);
 		
 		JLabel lblRemark = new JLabel("Bem\u00E6rkning:");
 		lblRemark.setFont(new Font("Dialog", Font.PLAIN, 12));
 		lblRemark.setBounds(10, 100, 80, 14);
 		dataEntry.add(lblRemark);
 		
 		txtRemark = new JTextPane();
 		txtRemark.setBounds(95, 100, 190, 100);
 		txtRemark.setBorder(new LineBorder(Color.LIGHT_GRAY));
 		dataEntry.add(txtRemark);
 
         startDate = new DateTimePanel();
         JPanel pnlStartDate = startDate.buildDateTimePanel(new Date());
         pnlStartDate.setBounds(95, 47, 240, 20);
 		dataEntry.add(pnlStartDate);
 
         endDate = new DateTimePanel();
         JPanel pnlEndDate = startDate.buildDateTimePanel(new Date());
         pnlEndDate.setBounds(95, 72, 240, 20);
         dataEntry.add(pnlEndDate);
 		
 		task = new JPanel();
 		task.setFont(new Font("Dialog", Font.PLAIN, 12));
 		tabbedPane.addTab("Opgave", null, task, null);
 		task.setLayout(null);
 		
 		JLabel lblTitle = new JLabel("Title:");
 		lblTitle.setFont(new Font("Dialog", Font.PLAIN, 12));
 		lblTitle.setBounds(10, 11, 57, 14);
 		task.add(lblTitle);
 		
 		JLabel lblDescription = new JLabel("Beskrivelse:");
 		lblDescription.setFont(new Font("Dialog", Font.PLAIN, 12));
 		lblDescription.setBounds(10, 45, 65, 14);
 		task.add(lblDescription);
 		
 		txtTitle = new JTextField();
 		txtTitle.setBounds(77, 11, 201, 20);
 		task.add(txtTitle);
 		txtTitle.setColumns(10);
 		
 		txtDescription = new JTextPane();
 		txtDescription.setBounds(77, 42, 201, 136);
 		txtDescription.setBorder(new LineBorder(Color.LIGHT_GRAY));
 		task.add(txtDescription);
 		
 		JButton btnCreateTask = new JButton("Opret ny Opgave");
         btnCreateTask.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 createTask();
             }
         });
 		btnCreateTask.setBounds(155, 183, 125, 23);
 		task.add(btnCreateTask);
 		
         JButton btnCancel = new JButton("Annuller");
         btnCancel.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 _instance = null;
                 _frame.dispose();
             }
         });
 		btnCancel.setBounds(195, 248, 89, 23);
 		contentPane.add(btnCancel);
 		
         JButton btnCreate = new JButton("Opret");
         btnCreate.addActionListener(new ActionListener()
         {
             public void actionPerformed(ActionEvent e)
             {
                 createDataEntry();
             }
         });
 		btnCreate.setBounds(96, 248, 89, 23);
 		contentPane.add(btnCreate);
 	}
 	
 	public void createDataEntry()
 	{
         try
         {
             System.out.println(startDate.getTimeSpinner().getValue().toString());
 
             Task task = (Task)drpXTask.getSelectedItem();
             User user = UserSession.getLoggedInUser();
             Date chosenStartDate = startDate.getDateChooser().getDate();
             Date chosenEndDate = endDate.getDateChooser().getDate();
             String entryRemark = txtRemark.getText();
             Calendar cal = Calendar.getInstance();
             Date creationDate = cal.getTime();
             Date editedDate =  cal.getTime();
             DataEntry dataEntry = new DataEntry(task, user, chosenStartDate, chosenEndDate, entryRemark, creationDate, editedDate);
 
             _tsCtrl.addDataEntry(_ts, dataEntry);
 
             JOptionPane.showMessageDialog(null, "Registreringen er oprettet", "Information!", JOptionPane.INFORMATION_MESSAGE);
             _instance = null;
             _frame.dispose();
         }
         catch (Exception e)
         {
             e.printStackTrace();
             JOptionPane.showMessageDialog(null, Logging.handleException(e, 1), "Fejl", JOptionPane.WARNING_MESSAGE);
         }
 	}
 	
 	public void createTask()
 	{
         try
         {
             String title = txtTitle.getText();
             String description = txtDescription.getText();
             Task task = new Task(title, description);
 
             _taCtrl.insertTask(task);
 
             JOptionPane.showMessageDialog(null, "Opgavetypen er oprettet", "Information!", JOptionPane.INFORMATION_MESSAGE);
             _instance = null;
             _frame.dispose();
         }
         catch (Exception e)
         {
             e.printStackTrace();
             JOptionPane.showMessageDialog(null, Logging.handleException(e, 1), "Fejl", JOptionPane.WARNING_MESSAGE);
         }
 	}
 	
 	public String[] addTasks()
 	{
 		ArrayList<Task> tasks;
 		try
 		{
 			tasks = _taCtrl.getAllTasks();
 			String[] tasksNames = new String[tasks.size()];
 			for (int index = 0; index < tasks.size(); index++)
 				tasksNames[index] = tasks.get(index).getTitle();
 
 			return tasksNames;
 		}
 		catch (Exception e)
 		{
 			JOptionPane.showMessageDialog(null, Logging.handleException(e, 0), "Fejl", JOptionPane.WARNING_MESSAGE);
 		}
 		
 		return null;
 	}
 }
