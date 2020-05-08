 package be.artesis.timelog.gui;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.SystemColor;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusAdapter;
 import java.awt.event.FocusEvent;
 import java.io.IOException;
 import java.text.ParseException;
 import java.util.Date;
 import java.util.Iterator;
 
 import javax.swing.DefaultListModel;
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.LayoutStyle.ComponentPlacement;
 import javax.swing.border.BevelBorder;
 
 import org.json.JSONException;
 
 import be.artesis.timelog.clock.Clock;
 import be.artesis.timelog.model.Validator;
 import be.artesis.timelog.model.WebserviceException;
 import be.artesis.timelog.view.DataInputException;
 import be.artesis.timelog.view.Opdrachtgever;
 import be.artesis.timelog.view.Project;
 import be.artesis.timelog.view.Taak;
 import be.artesis.timelog.view.Tijdspanne;
 
 import com.jgoodies.forms.factories.FormFactory;
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.RowSpec;
 import com.toedter.calendar.JDateChooser;
 
 /**
  * @author Gilliam
  */
 @SuppressWarnings({ "unchecked", "rawtypes", "serial" })
 public class GUIForm extends javax.swing.JFrame {
 
 	// ================================================================================
 	// Properties
 	// ================================================================================
 
 	LoginDialog login;
 	Validator validator;
 	final String NEWCLIENTITEM = "< New client >";
 	final String NEWTASKITEM = "< New task >";
 	final String NEWPROJECTITEM = "< New project >";
 
 	public GUIForm() {
 		validator = Validator.getInstance();
 		login = new LoginDialog(this, true, validator);
 		login.setVisible(true);
 		initComponents();
 	}
 
 	/* Begin gegenereerde code */
 	// Initialiseer GUI componenten
 	private void initComponents() {
 		JTabbedPane = new javax.swing.JTabbedPane();
 		homeJPanel = new javax.swing.JPanel();
 		homeJLabel = new javax.swing.JLabel();
 		workJButton = new javax.swing.JButton();
 		jScrollPane7 = new javax.swing.JScrollPane();
 		homeProjectsJList = new javax.swing.JList();
 		HomeProjectsJLabel = new javax.swing.JLabel();
 		jScrollPane8 = new javax.swing.JScrollPane();
 		homeTasksJList = new javax.swing.JList();
 		TasksJLabel = new javax.swing.JLabel();
 		projectsJPanel = new javax.swing.JPanel();
 		projectsJLabel = new javax.swing.JLabel();
 		projectsJLabel.setBounds(10, 11, 204, 16);
 		jScrollPane1 = new javax.swing.JScrollPane();
 		jScrollPane1.setBounds(10, 33, 204, 336);
 		setCurrentProjectJButton = new javax.swing.JButton();
 		setCurrentProjectJButton.setBounds(10, 404, 204, 36);
 		filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 32767));
 		filler2.setBounds(220, 152, 0, 0);
 		removeProjectJButton = new javax.swing.JButton();
 		removeProjectJButton.setBounds(10, 375, 204, 23);
 		tasksJPanel = new javax.swing.JPanel();
 		tasksJLabel = new javax.swing.JLabel();
		tasksJLabel.setBounds(10, 14, 204, 16);
 		jScrollPane3 = new javax.swing.JScrollPane();
 		jScrollPane3.setBounds(10, 40, 204, 366);
 		removeTaskJButton = new javax.swing.JButton();
 		removeTaskJButton.setBounds(10, 417, 204, 23);
 		scheduleJPanel = new javax.swing.JPanel();
 		scheduleJLabel = new javax.swing.JLabel();
 		scheduleJLabel.setBounds(10, 11, 66, 19);
 		optionsJPanel = new javax.swing.JPanel();
 		settingsJLabel = new javax.swing.JLabel();
 		headerJPanel = new javax.swing.JPanel();
 		titleLabel = new javax.swing.JLabel();
 		ingelogdJLabel = new javax.swing.JLabel();
 		currentProjectJLabel = new javax.swing.JLabel();
 		clockJLabel = new javax.swing.JLabel();
 
 		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
 		setTitle("Time Management System");
 		setBackground(new java.awt.Color(51, 51, 51));
 		this.getContentPane().setBackground(Color.darkGray);
 		setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
 		setForeground(new java.awt.Color(51, 51, 51));
 		setIconImages(null);
 		setMaximumSize(new java.awt.Dimension(2000, 2000));
 		setName("guiFrame");
 		setResizable(false);
 		addWindowListener(new java.awt.event.WindowAdapter() {
 			public void windowOpened(java.awt.event.WindowEvent evt) {
 				guiOpened(evt);
 			}
 		});
 
 		JTabbedPane.setBackground(new java.awt.Color(51, 51, 51));
 		JTabbedPane.setTabPlacement(javax.swing.JTabbedPane.LEFT);
 		JTabbedPane.setName("tabContainer");
 
 		homeJPanel.setBackground(new java.awt.Color(153, 153, 153));
 		homeJPanel.setForeground(new java.awt.Color(65, 152, 134));
 
 		homeJLabel.setFont(new java.awt.Font("Tw Cen MT", 1, 14));
 		homeJLabel.setForeground(new java.awt.Color(255, 255, 255));
 		homeJLabel.setText("Home");
 
 		workJButton.setBackground(new java.awt.Color(14, 196, 188));
 		workJButton.setFont(new java.awt.Font("Tahoma", 1, 18));
 		workJButton.setForeground(new java.awt.Color(204, 204, 204));
 		workJButton.setText("Work");
 		workJButton.addMouseListener(new java.awt.event.MouseAdapter() {
 			public void mouseClicked(java.awt.event.MouseEvent evt) {
 				workClicked(evt);
 			}
 		});
 
 		homeProjectsJList.setToolTipText("Press enter to set the current project");
 		homeProjectsJList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
 			public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
 				homeProjectsJListValueChanged(evt);
 			}
 		});
 		homeProjectsJList.addKeyListener(new java.awt.event.KeyAdapter() {
 			public void keyReleased(java.awt.event.KeyEvent evt) {
 				homeProjectsJListKeyReleased(evt);
 			}
 		});
 		jScrollPane7.setViewportView(homeProjectsJList);
 
 		HomeProjectsJLabel.setFont(new java.awt.Font("Tw Cen MT", 1, 14));
 		HomeProjectsJLabel.setForeground(new java.awt.Color(0, 153, 153));
 		HomeProjectsJLabel.setText("Projects");
 
 		jScrollPane8.setViewportView(homeTasksJList);
 
 		TasksJLabel.setFont(new java.awt.Font("Tw Cen MT", 1, 14));
 		TasksJLabel.setForeground(new java.awt.Color(0, 153, 153));
 		TasksJLabel.setText("Tasks");
 
 		javax.swing.GroupLayout homeJPanelLayout = new javax.swing.GroupLayout(homeJPanel);
 		homeJPanel.setLayout(homeJPanelLayout);
 		homeJPanelLayout.setHorizontalGroup(homeJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(homeJPanelLayout.createSequentialGroup().addContainerGap().addGroup(homeJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(workJButton, javax.swing.GroupLayout.DEFAULT_SIZE, 662, Short.MAX_VALUE).addGroup(homeJPanelLayout.createSequentialGroup().addGroup(homeJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(homeJLabel).addGroup(homeJPanelLayout.createSequentialGroup().addGroup(homeJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jScrollPane7, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(HomeProjectsJLabel)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addGroup(homeJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(TasksJLabel).addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 160, javax.swing.GroupLayout.PREFERRED_SIZE)))).addGap(0, 0, Short.MAX_VALUE))).addContainerGap()));
 		homeJPanelLayout.setVerticalGroup(homeJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(homeJPanelLayout.createSequentialGroup().addContainerGap().addComponent(homeJLabel).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(workJButton, javax.swing.GroupLayout.PREFERRED_SIZE, 53, javax.swing.GroupLayout.PREFERRED_SIZE).addGap(28, 28, 28).addGroup(homeJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(HomeProjectsJLabel).addComponent(TasksJLabel)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(homeJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jScrollPane8, javax.swing.GroupLayout.DEFAULT_SIZE, 249, Short.MAX_VALUE).addComponent(jScrollPane7)).addContainerGap()));
 
 		JTabbedPane.addTab("", new javax.swing.ImageIcon(getClass().getResource("/be/artesis/timelog/gui/icons/HomeNeonIcon.png")), homeJPanel, "Home"); // NOI18N
 
 		projectsJPanel.setBackground(new java.awt.Color(153, 153, 153));
 
 		projectsJLabel.setFont(new java.awt.Font("Tw Cen MT", 1, 14));
 		projectsJLabel.setForeground(new java.awt.Color(255, 255, 255));
 		projectsJLabel.setText("Projects");
 
 		setCurrentProjectJButton.setText("Set as current project");
 		setCurrentProjectJButton.setEnabled(false);
 		setCurrentProjectJButton.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				setCurrentProjectJButtonActionPerformed(evt);
 			}
 		});
 
 		removeProjectJButton.setText("Remove project");
 		removeProjectJButton.setEnabled(false);
 		removeProjectJButton.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				removeProject();
 			}
 		});
 
 		projectFieldsJPanel = new JPanel();
 		projectFieldsJPanel.setBounds(340, 33, 334, 336);
 		projectFieldsJPanel.setBackground(Color.DARK_GRAY);
 		projectFieldsJPanel.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
 		projectsJList = new javax.swing.JList();
 		jScrollPane1.setViewportView(projectsJList);
 
 		projectsJList.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
 		projectsJList.setModel(new javax.swing.AbstractListModel() {
 			String[] strings = {};
 
 			public int getSize() {
 				return strings.length;
 			}
 
 			public Object getElementAt(int i) {
 				return strings[i];
 			}
 		});
 		projectsJList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
 			public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
 				projectsJListValueChanged(evt);
 			}
 		});
 		projectFieldsJPanel.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.UNRELATED_GAP_COLSPEC, ColumnSpec.decode("65px"), FormFactory.LABEL_COMPONENT_GAP_COLSPEC, ColumnSpec.decode("max(53dlu;default)"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("123px"), FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, }, new RowSpec[] { FormFactory.UNRELATED_GAP_ROWSPEC, RowSpec.decode("20px"), FormFactory.LINE_GAP_ROWSPEC, FormFactory.PREF_ROWSPEC, FormFactory.LINE_GAP_ROWSPEC, FormFactory.PREF_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("96px"), FormFactory.UNRELATED_GAP_ROWSPEC, RowSpec.decode("20px"), FormFactory.LINE_GAP_ROWSPEC, RowSpec.decode("21px"), RowSpec.decode("60px"), RowSpec.decode("23px"), }));
 		namecompJLabel = new javax.swing.JLabel();
 		projectFieldsJPanel.add(namecompJLabel, "2, 2, left, center");
 
 		namecompJLabel.setForeground(new java.awt.Color(255, 255, 255));
 		namecompJLabel.setText("Naam");
 		nameJTextField = new javax.swing.JTextField();
 		projectFieldsJPanel.add(nameJTextField, "4, 2, 3, 1, fill, top");
 		startdatecompJLabel = new javax.swing.JLabel();
 		projectFieldsJPanel.add(startdatecompJLabel, "2, 4, 2, 1, left, center");
 
 		startdatecompJLabel.setForeground(new java.awt.Color(255, 255, 255));
 		startdatecompJLabel.setText("Start date");
 
 		projectStartDateChooser = new JDateChooser();
 		projectStartDateChooser.setDateFormatString("dd/MM/yyyy");
 		projectFieldsJPanel.add(projectStartDateChooser, "4, 4, fill, fill");
 		enddatecompJLabel1 = new javax.swing.JLabel();
 		projectFieldsJPanel.add(enddatecompJLabel1, "2, 6, 2, 1, left, center");
 
 		enddatecompJLabel1.setForeground(new java.awt.Color(255, 255, 255));
 		enddatecompJLabel1.setText("End date");
 
 		projectEndDateChooser = new JDateChooser();
 		projectEndDateChooser.setDateFormatString("dd/MM/yyyy");
 		projectFieldsJPanel.add(projectEndDateChooser, "4, 6, fill, fill");
 
 		projectTasksJList = new JList();
 		projectTasksJList.setEnabled(false);
 		projectTasksJList.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
 		projectTasksJList.setBackground(Color.LIGHT_GRAY);
 		projectFieldsJPanel.add(projectTasksJList, "4, 8, 3, 1, fill, fill");
 		clientcompJLabel1 = new javax.swing.JLabel();
 		projectFieldsJPanel.add(clientcompJLabel1, "2, 10, left, top");
 
 		clientcompJLabel1.setForeground(new java.awt.Color(255, 255, 255));
 		clientcompJLabel1.setText("Client");
 		clientJLabel = new javax.swing.JLabel();
 		projectFieldsJPanel.add(clientJLabel, "4, 10, 3, 1, fill, top");
 
 		clientJLabel.setBackground(new java.awt.Color(204, 255, 255));
 		clientJLabel.setForeground(new java.awt.Color(255, 255, 255));
 		clientJLabel.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
 		clientJLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
 		clientJLabel.setMaximumSize(null);
 		clientJLabel.setMinimumSize(new java.awt.Dimension(34, 20));
 		clientJLabel.setName("");
 		clientJLabel.setPreferredSize(new java.awt.Dimension(34, 20));
 		taskscompJLabel = new javax.swing.JLabel();
 		projectFieldsJPanel.add(taskscompJLabel, "2, 8, left, top");
 
 		taskscompJLabel.setForeground(new java.awt.Color(255, 255, 255));
 		taskscompJLabel.setText("Tasks");
 		percentageCompleteJProgressBar = new javax.swing.JProgressBar();
 		projectFieldsJPanel.add(percentageCompleteJProgressBar, "4, 12, 3, 1, fill, fill");
 		percentageCompletecompJLabel = new javax.swing.JLabel();
 		projectFieldsJPanel.add(percentageCompletecompJLabel, "2, 12, fill, fill");
 
 		percentageCompletecompJLabel.setForeground(new java.awt.Color(255, 255, 255));
 		percentageCompletecompJLabel.setText("Complete");
 		saveProjectJButton = new javax.swing.JButton();
 		projectFieldsJPanel.add(saveProjectJButton, "2, 14, 5, 1, fill, top");
 
 		saveProjectJButton.setText("Save");
 		saveProjectJButton.setEnabled(false);
 		saveProjectJButton.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				saveProject();
 			}
 		});
 
 		nameJTextField.addFocusListener(new java.awt.event.FocusAdapter() {
 			public void focusGained(java.awt.event.FocusEvent evt) {
 				editFieldsFocused(evt);
 			}
 		});
 
 		JTabbedPane.addTab("", new javax.swing.ImageIcon(getClass().getResource("/be/artesis/timelog/gui/icons/ProjectsNeonIcon.png")), projectsJPanel, "Projects");
 		projectsJPanel.setLayout(null);
 		projectsJPanel.add(removeProjectJButton);
 		projectsJPanel.add(setCurrentProjectJButton);
 		projectsJPanel.add(jScrollPane1);
 		projectsJPanel.add(filler2);
 		projectsJPanel.add(projectFieldsJPanel);
 		projectsJPanel.add(projectsJLabel);
 
 		tasksJPanel.setBackground(new java.awt.Color(153, 153, 153));
 
 		tasksJLabel.setFont(new java.awt.Font("Tw Cen MT", 1, 14));
 		tasksJLabel.setForeground(new java.awt.Color(255, 255, 255));
 		tasksJLabel.setText("Tasks");
 
 		removeTaskJButton.setText("Remove task");
 		removeTaskJButton.setEnabled(false);
 		removeTaskJButton.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				removeTask();
 			}
 		});
 
 		JTabbedPane.addTab("", new javax.swing.ImageIcon(getClass().getResource("/be/artesis/timelog/gui/icons/TasksNeonIcon.png")), tasksJPanel, "Tasks");
 		tasksJPanel.setLayout(null);
 		tasksJPanel.add(tasksJLabel);
 		tasksJPanel.add(jScrollPane3);
 
 		tasksJList_1 = new JList();
 		tasksJList_1.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
 		jScrollPane3.setViewportView(tasksJList_1);
 		tasksJPanel.add(removeTaskJButton);
 
 		taskFieldsJPanel = new JPanel();
 		taskFieldsJPanel.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
 		taskFieldsJPanel.setBackground(Color.DARK_GRAY);
 		taskFieldsJPanel.setBounds(273, 40, 399, 400);
 		tasksJPanel.add(taskFieldsJPanel);
 		taskFieldsJPanel.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.UNRELATED_GAP_COLSPEC, ColumnSpec.decode("56px"), FormFactory.UNRELATED_GAP_COLSPEC, ColumnSpec.decode("1px"), FormFactory.LABEL_COMPONENT_GAP_COLSPEC, ColumnSpec.decode("110px"), FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("55px"), ColumnSpec.decode("51px"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(27dlu;default)"), FormFactory.RELATED_GAP_COLSPEC, ColumnSpec.decode("max(15dlu;default)"), }, new RowSpec[] { FormFactory.UNRELATED_GAP_ROWSPEC, RowSpec.decode("20px"), FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("20px:grow"), FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("20px:grow"), RowSpec.decode("38px"), RowSpec.decode("51px"), FormFactory.UNRELATED_GAP_ROWSPEC, RowSpec.decode("51px"), FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("37px"), FormFactory.UNRELATED_GAP_ROWSPEC, RowSpec.decode("23px"), FormFactory.UNRELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.UNRELATED_GAP_ROWSPEC, RowSpec.decode("41px"), }));
 
 		label_5 = new JLabel();
 		label_5.setText("Name");
 		label_5.setForeground(Color.WHITE);
 		taskFieldsJPanel.add(label_5, "2, 2, fill, center");
 
 		taskNameJTextField = new JTextField();
 		taskNameJTextField.addFocusListener(new FocusAdapter() {
 			@Override
 			public void focusGained(FocusEvent e) {
 				editFieldsFocused(e);
 			}
 		});
 		taskFieldsJPanel.add(taskNameJTextField, "4, 2, 9, 1, fill, top");
 
 		label_6 = new JLabel();
 		label_6.setText("Start date");
 		label_6.setForeground(Color.WHITE);
 		taskFieldsJPanel.add(label_6, "2, 4, fill, center");
 
 		taskStartDateChooser = new JDateChooser();
 		taskStartDateChooser.setDateFormatString("dd/MM/yyyy");
 		taskFieldsJPanel.add(taskStartDateChooser, "4, 4, 3, 1, fill, center");
 
 		label_7 = new JLabel();
 		label_7.setText("Completed");
 		label_7.setForeground(Color.WHITE);
 		taskFieldsJPanel.add(label_7, "9, 4, 2, 1, center, center");
 
 		taskCompletedJCheckBox = new JCheckBox();
 		taskCompletedJCheckBox.addFocusListener(new FocusAdapter() {
 			@Override
 			public void focusGained(FocusEvent arg0) {
 				editFieldsFocused(arg0);
 			}
 		});
 		taskCompletedJCheckBox.setBackground(Color.DARK_GRAY);
 		taskFieldsJPanel.add(taskCompletedJCheckBox, "12, 4, left, center");
 
 		label_8 = new JLabel();
 		label_8.setText("End date");
 		label_8.setForeground(Color.WHITE);
 		taskFieldsJPanel.add(label_8, "2, 6, fill, center");
 
 		taskEndDateChooser = new JDateChooser();
 		taskEndDateChooser.setDateFormatString("dd/MM/yyyy");
 		taskFieldsJPanel.add(taskEndDateChooser, "4, 6, 3, 1, fill, center");
 		tasksJList = new javax.swing.JList();
 		taskFieldsJPanel.add(tasksJList, "2, 7");
 
 		tasksJList.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
 		tasksJList.setModel(new javax.swing.AbstractListModel() {
 			String[] strings = {};
 
 			public int getSize() {
 				return strings.length;
 			}
 
 			public Object getElementAt(int i) {
 				return strings[i];
 			}
 		});
 		tasksJList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
 			public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
 				tasksJListprojectListValueChanged(evt);
 			}
 		});
 
 		label_9 = new JLabel();
 		label_9.setText("Comment");
 		label_9.setForeground(Color.WHITE);
 		taskFieldsJPanel.add(label_9, "2, 8, fill, top");
 
 		lblWorked = new JLabel();
 		lblWorked.setText("Worked");
 		lblWorked.setForeground(Color.WHITE);
 		taskFieldsJPanel.add(lblWorked, "2, 10, 3, 1, fill, top");
 
 		taskTotalTimeCompJLabel = new JLabel();
 		taskTotalTimeCompJLabel.setToolTipText("");
 		taskTotalTimeCompJLabel.setForeground(SystemColor.menu);
 		taskTotalTimeCompJLabel.setBackground(Color.LIGHT_GRAY);
 		taskFieldsJPanel.add(taskTotalTimeCompJLabel, "6, 14, 8, 1, fill, fill");
 
 		saveTaskJButton = new JButton();
 		saveTaskJButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				saveTask();
 			}
 		});
 
 		taskTotalPauseCompJLabel = new JLabel();
 		taskTotalPauseCompJLabel.setForeground(SystemColor.menu);
 		taskTotalPauseCompJLabel.setBackground(Color.LIGHT_GRAY);
 		taskFieldsJPanel.add(taskTotalPauseCompJLabel, "6, 16, 8, 1, fill, fill");
 		saveTaskJButton.setText("Save");
 		saveTaskJButton.setEnabled(false);
 		taskFieldsJPanel.add(saveTaskJButton, "2, 18, 11, 1, fill, top");
 
 		taskCommentJTextArea = new JTextArea();
 		taskCommentJTextArea.addFocusListener(new FocusAdapter() {
 			@Override
 			public void focusGained(FocusEvent e) {
 				editFieldsFocused(e);
 			}
 		});
 		taskFieldsJPanel.add(taskCommentJTextArea, "4, 8, 9, 1, fill, fill");
 
 		workedTimeJList = new JList();
 		taskFieldsJPanel.add(workedTimeJList, "5, 10, 8, 3, fill, fill");
 		clientsJPanel = new javax.swing.JPanel();
 		clientsJLabel = new javax.swing.JLabel();
 		jScrollPane5 = new javax.swing.JScrollPane();
 		clientsJList = new javax.swing.JList();
 		clientsJList.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
 		removeClientJButton = new javax.swing.JButton();
 
 		clientsJPanel.setBackground(new java.awt.Color(153, 153, 153));
 
 		clientsJLabel.setFont(new java.awt.Font("Tw Cen MT", 1, 14));
 		clientsJLabel.setForeground(new java.awt.Color(255, 255, 255));
 		clientsJLabel.setText("Clients");
 
 		clientsJList.setModel(new javax.swing.AbstractListModel() {
 			String[] strings = {};
 
 			public int getSize() {
 				return strings.length;
 			}
 
 			public Object getElementAt(int i) {
 				return strings[i];
 			}
 		});
 		clientsJList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
 			public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
 				clientsJListValueChanged(evt);
 			}
 		});
 		jScrollPane5.setViewportView(clientsJList);
 
 		removeClientJButton.setText("Remove client");
 		removeClientJButton.setEnabled(false);
 		removeClientJButton.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				removeClient();
 			}
 		});
 
 		clientFieldsJPanel = new JPanel();
 		clientFieldsJPanel.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
 		clientFieldsJPanel.setBackground(Color.DARK_GRAY);
 
 		javax.swing.GroupLayout clientsJPanelLayout = new javax.swing.GroupLayout(clientsJPanel);
 		clientsJPanelLayout.setHorizontalGroup(clientsJPanelLayout.createParallelGroup(Alignment.LEADING).addGroup(clientsJPanelLayout.createSequentialGroup().addContainerGap().addGroup(clientsJPanelLayout.createParallelGroup(Alignment.LEADING).addGroup(clientsJPanelLayout.createSequentialGroup().addGroup(clientsJPanelLayout.createParallelGroup(Alignment.LEADING, false).addComponent(jScrollPane5, GroupLayout.DEFAULT_SIZE, 197, Short.MAX_VALUE).addComponent(removeClientJButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).addPreferredGap(ComponentPlacement.RELATED, 143, Short.MAX_VALUE).addComponent(clientFieldsJPanel, GroupLayout.PREFERRED_SIZE, 324, GroupLayout.PREFERRED_SIZE)).addComponent(clientsJLabel)).addContainerGap()));
 		clientsJPanelLayout.setVerticalGroup(clientsJPanelLayout.createParallelGroup(Alignment.TRAILING).addGroup(clientsJPanelLayout.createSequentialGroup().addGroup(clientsJPanelLayout.createParallelGroup(Alignment.TRAILING).addGroup(Alignment.LEADING, clientsJPanelLayout.createSequentialGroup().addGap(33).addComponent(clientFieldsJPanel, GroupLayout.DEFAULT_SIZE, 378, Short.MAX_VALUE)).addGroup(clientsJPanelLayout.createSequentialGroup().addContainerGap().addComponent(clientsJLabel).addPreferredGap(ComponentPlacement.RELATED).addComponent(jScrollPane5, GroupLayout.DEFAULT_SIZE, 336, Short.MAX_VALUE))).addPreferredGap(ComponentPlacement.RELATED).addComponent(removeClientJButton, GroupLayout.PREFERRED_SIZE, 23, GroupLayout.PREFERRED_SIZE).addContainerGap()));
 		clientFieldsJPanel.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.UNRELATED_GAP_COLSPEC, ColumnSpec.decode("79px"), ColumnSpec.decode("225px"), }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, RowSpec.decode("20px"), FormFactory.LINE_GAP_ROWSPEC, RowSpec.decode("20px"), FormFactory.LINE_GAP_ROWSPEC, RowSpec.decode("20px"), FormFactory.LINE_GAP_ROWSPEC, RowSpec.decode("20px"), FormFactory.LINE_GAP_ROWSPEC, RowSpec.decode("20px"), RowSpec.decode("192px"), RowSpec.decode("17px"), FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));
 
 		JLabel label = new JLabel();
 		label.setText("Name");
 		label.setForeground(Color.WHITE);
 		clientFieldsJPanel.add(label, "2, 2, fill, center");
 
 		clientNameJTextField = new JTextField();
 		clientNameJTextField.addFocusListener(new FocusAdapter() {
 			@Override
 			public void focusGained(FocusEvent e) {
 				editFieldsFocused(e);
 			}
 		});
 		clientFieldsJPanel.add(clientNameJTextField, "3, 2, fill, top");
 
 		JLabel label_1 = new JLabel();
 		label_1.setText("First name");
 		label_1.setForeground(Color.WHITE);
 		clientFieldsJPanel.add(label_1, "2, 4, fill, center");
 
 		clientFirstNameJTextField = new JTextField();
 		clientFirstNameJTextField.addFocusListener(new FocusAdapter() {
 			@Override
 			public void focusGained(FocusEvent e) {
 				editFieldsFocused(e);
 			}
 		});
 		clientFieldsJPanel.add(clientFirstNameJTextField, "3, 4, fill, top");
 
 		JLabel label_2 = new JLabel();
 		label_2.setText("Company");
 		label_2.setForeground(Color.WHITE);
 		clientFieldsJPanel.add(label_2, "2, 6, fill, center");
 
 		clientCompanyJTextField = new JTextField();
 		clientCompanyJTextField.addFocusListener(new FocusAdapter() {
 			@Override
 			public void focusGained(FocusEvent e) {
 				editFieldsFocused(e);
 			}
 		});
 		clientFieldsJPanel.add(clientCompanyJTextField, "3, 6, fill, top");
 
 		JLabel label_3 = new JLabel();
 		label_3.setText("E-mail");
 		label_3.setForeground(Color.WHITE);
 		clientFieldsJPanel.add(label_3, "2, 8, fill, center");
 
 		clientEmailJTextField = new JTextField();
 		clientEmailJTextField.addFocusListener(new FocusAdapter() {
 			@Override
 			public void focusGained(FocusEvent e) {
 				editFieldsFocused(e);
 			}
 		});
 		clientFieldsJPanel.add(clientEmailJTextField, "3, 8, fill, top");
 
 		JLabel label_4 = new JLabel();
 		label_4.setText("Phone number");
 		label_4.setForeground(Color.WHITE);
 		clientFieldsJPanel.add(label_4, "2, 10, fill, center");
 
 		clientPhoneNumberJTextField = new JTextField();
 		clientPhoneNumberJTextField.addFocusListener(new FocusAdapter() {
 			@Override
 			public void focusGained(FocusEvent e) {
 				editFieldsFocused(e);
 			}
 		});
 		clientFieldsJPanel.add(clientPhoneNumberJTextField, "3, 10, fill, top");
 
 		saveClientJButton = new JButton();
 		saveClientJButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				saveClient();
 			}
 		});
 		saveClientJButton.setText("Save");
 		saveClientJButton.setEnabled(false);
 		clientFieldsJPanel.add(saveClientJButton, "2, 14, 2, 1, fill, top");
 		clientsJPanel.setLayout(clientsJPanelLayout);
 
 		JTabbedPane.addTab("", new javax.swing.ImageIcon(getClass().getResource("/be/artesis/timelog/gui/icons/ClientsNeonIcon.png")), clientsJPanel, "Clients");
 
 		scheduleJPanel.setBackground(new java.awt.Color(153, 153, 153));
 
 		scheduleJLabel.setFont(new java.awt.Font("Tw Cen MT", 1, 14));
 		scheduleJLabel.setForeground(new java.awt.Color(255, 255, 255));
 		scheduleJLabel.setText("Schedule");
 
 		JTabbedPane.addTab("", new javax.swing.ImageIcon(getClass().getResource("/be/artesis/timelog/gui/icons/CalendarNeonIcon.png")), scheduleJPanel, "Schedule");
 		scheduleJPanel.setLayout(null);
 		scheduleJPanel.add(scheduleJLabel);
 
 		optionsJPanel.setBackground(new java.awt.Color(153, 153, 153));
 
 		settingsJLabel.setFont(new java.awt.Font("Tw Cen MT", 1, 14));
 		settingsJLabel.setForeground(new java.awt.Color(255, 255, 255));
 		settingsJLabel.setText("Settings");
 
 		javax.swing.GroupLayout optionsJPanelLayout = new javax.swing.GroupLayout(optionsJPanel);
 		optionsJPanel.setLayout(optionsJPanelLayout);
 		optionsJPanelLayout.setHorizontalGroup(optionsJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(optionsJPanelLayout.createSequentialGroup().addContainerGap().addComponent(settingsJLabel).addContainerGap(627, Short.MAX_VALUE)));
 		optionsJPanelLayout.setVerticalGroup(optionsJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(optionsJPanelLayout.createSequentialGroup().addContainerGap().addComponent(settingsJLabel).addContainerGap(369, Short.MAX_VALUE)));
 
 		JTabbedPane.addTab("", new javax.swing.ImageIcon(getClass().getResource("/be/artesis/timelog/gui/icons/SettingsNeonIcon.png")), optionsJPanel, "Settings");
 
 		headerJPanel.setBackground(new java.awt.Color(64, 64, 64));
 
 		titleLabel.setFont(new java.awt.Font("Tempus Sans ITC", 1, 18));
 		titleLabel.setForeground(new java.awt.Color(255, 255, 255));
 		titleLabel.setText("ChronoMatic");
 
 		ingelogdJLabel.setBackground(new java.awt.Color(255, 255, 255));
 		ingelogdJLabel.setFont(new java.awt.Font("Tahoma", 1, 11));
 		ingelogdJLabel.setForeground(new java.awt.Color(255, 0, 0));
 		ingelogdJLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
 		ingelogdJLabel.setText("Not logged in");
 		ingelogdJLabel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
 		ingelogdJLabel.setName("");
 		ingelogdJLabel.setOpaque(true);
 
 		currentProjectJLabel.setBackground(new java.awt.Color(255, 255, 255));
 		currentProjectJLabel.setFont(new java.awt.Font("Tahoma", 1, 11));
 		currentProjectJLabel.setForeground(new java.awt.Color(0, 204, 204));
 		currentProjectJLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
 		currentProjectJLabel.setText("Current project: ...");
 		currentProjectJLabel.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.LOWERED));
 		currentProjectJLabel.setName("");
 		currentProjectJLabel.setOpaque(true);
 
 		clockJLabel.setIcon(new javax.swing.ImageIcon(getClass().getResource("/be/artesis/timelog/gui/icons/ClockNeonIcon.png")));
 
 		javax.swing.GroupLayout headerJPanelLayout = new javax.swing.GroupLayout(headerJPanel);
 		headerJPanel.setLayout(headerJPanelLayout);
 		headerJPanelLayout.setHorizontalGroup(headerJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(javax.swing.GroupLayout.Alignment.TRAILING, headerJPanelLayout.createSequentialGroup().addContainerGap().addGroup(headerJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false).addComponent(currentProjectJLabel, javax.swing.GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE).addComponent(ingelogdJLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(titleLabel).addGap(18, 18, 18).addComponent(clockJLabel).addGap(6, 6, 6)));
 		headerJPanelLayout.setVerticalGroup(headerJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(headerJPanelLayout.createSequentialGroup().addContainerGap().addGroup(headerJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(titleLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(clockJLabel, javax.swing.GroupLayout.Alignment.TRAILING).addGroup(headerJPanelLayout.createSequentialGroup().addComponent(ingelogdJLabel).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addComponent(currentProjectJLabel).addGap(0, 0, Short.MAX_VALUE))).addContainerGap()));
 
 		clockJLabel.getAccessibleContext().setAccessibleName("iconJLabel");
 
 		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
 		getContentPane().setLayout(layout);
 		layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(headerJPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addComponent(JTabbedPane));
 		layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addComponent(headerJPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(JTabbedPane)));
 		pack();
 	}
 
 	/* Einde gegenereerde code */
 
 	// ================================================================================
 	// Save / Edit methods
 	// ================================================================================
 
 	// Save PROJECT
 	private void saveProject() {
 		try {
 			String name = nameJTextField.getText();
 			long startdate = projectStartDateChooser.getDate().getTime();
 			long enddate = projectEndDateChooser.getDate().getTime();
 			int opdrachtgeverID = 0; // FIXME int halen uit selectie uit dropdownbox
 
 			if (projectsJList.getSelectedValue().equals(NEWPROJECTITEM)) {
 				UserInterface.saveNewProject(name, startdate, enddate, opdrachtgeverID);
 				JOptionPane.showMessageDialog(this, "Project added!");
 			} else {
 				UserInterface.saveProject(projectsJList.getSelectedIndex(), name, startdate, enddate, opdrachtgeverID);
 				JOptionPane.showMessageDialog(this, "Project edited!");
 			}
 		} catch (DataInputException | IOException | WebserviceException | JSONException | ParseException ex) {
 			ex.printStackTrace();
 			JOptionPane.showMessageDialog(this, ex.getMessage());
 		} catch (NullPointerException ex) {
 			ex.printStackTrace();
 			JOptionPane.showMessageDialog(this, "Please choose a valid date");
 		} finally {
 			refreshProjectsList(projectsJList, homeProjectsJList);
 			toggleButtonStates();
 		}
 	}
 
 	// Save TASK
 	private void saveTask() {
 		try {
 			String name = taskNameJTextField.getText();
 			long startdate = taskStartDateChooser.getDate().getTime();
 			long enddate = taskEndDateChooser.getDate().getTime();
 			String comment = taskCommentJTextArea.getText();
 			boolean completed = taskCompletedJCheckBox.isSelected();
 
 			if (tasksJList.getSelectedValue().equals(NEWTASKITEM)) {
 				UserInterface.saveNewTask(name, startdate, enddate, comment, completed);
 				JOptionPane.showMessageDialog(this, "Task added!");
 				refreshTasksList(UserInterface.getCurrentProject(), tasksJList);
 			} else {
 				UserInterface.saveTask(tasksJList.getSelectedIndex(), name, startdate, enddate, comment, completed);
 				JOptionPane.showMessageDialog(this, "Task edited!");
 				refreshTasksList(UserInterface.getCurrentProject(), tasksJList);
 			}
 		} catch (GUIException | DataInputException | JSONException | ParseException | IOException | WebserviceException ex) {
 			ex.printStackTrace();
 			JOptionPane.showMessageDialog(this, ex.getMessage());
 		} catch (NullPointerException ex) {
 			ex.printStackTrace();
 			JOptionPane.showMessageDialog(this, "Please choose a valid date");
 		} finally {
 			toggleButtonStates();
 			loadProjectInfo(projectsJList.getSelectedIndex());
 		}
 	}
 
 	// Save CLIENT
 	private void saveClient() {
 		String naam = clientNameJTextField.getText();
 		String voornaam = clientFirstNameJTextField.getText();
 		String bedrijfsnaam = clientCompanyJTextField.getText();
 		String email = clientEmailJTextField.getText();
 		String telefoonnummer = clientPhoneNumberJTextField.getText();
 
 		try {
 			if (clientsJList.getSelectedValue().equals(NEWCLIENTITEM)) {
 				UserInterface.saveNewClient(naam, voornaam, bedrijfsnaam, email, telefoonnummer);
 				JOptionPane.showMessageDialog(this, "Client added!");
 			} else {
 				UserInterface.saveClient(clientsJList.getSelectedIndex(), voornaam, voornaam, bedrijfsnaam, email, telefoonnummer);
 				JOptionPane.showMessageDialog(this, "Client edited!");
 			}
 		} catch (DataInputException | IOException | WebserviceException | JSONException ex) {
 			ex.printStackTrace();
 			JOptionPane.showMessageDialog(this, ex.getMessage());
 		} finally {
 			refreshClientsList();
 			toggleButtonStates();
 		}
 	}
 
 	// ================================================================================
 	// Remove methods
 	// ================================================================================
 
 	// Remove PROJECT
 	private void removeProject() {
 		int result = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this project?", null, JOptionPane.YES_NO_OPTION);
 		if (result == JOptionPane.YES_OPTION) {
 			try {
 				UserInterface.removeProject((Project) projectsJList.getSelectedValue());
 				refreshProjectsList(projectsJList, homeProjectsJList);
 				JOptionPane.showMessageDialog(this, "Project removed!");
 			} catch (IOException | WebserviceException ex) {
 				ex.printStackTrace();
 				JOptionPane.showMessageDialog(this, ex.getMessage());
 			} finally {
 				clearFieldsOnPanel(projectFieldsJPanel);
 				toggleButtonStates();
 			}
 		}
 	}
 
 	// Remove TASK
 	private void removeTask() {
 		int result = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this task?", null, JOptionPane.YES_NO_OPTION);
 		if (result == JOptionPane.YES_OPTION) {
 			try {
 				UserInterface.removeTask((Taak) tasksJList.getSelectedValue());
 				JOptionPane.showMessageDialog(this, "Task removed!");
 			} catch (GUIException | IOException | WebserviceException ex) {
 				ex.printStackTrace();
 				JOptionPane.showMessageDialog(this, ex.getMessage());
 			} finally {
 				clearFieldsOnPanel(taskFieldsJPanel);
 				toggleButtonStates();
 			}
 		}
 	}
 
 	// Remove CLIENT
 	private void removeClient() {
 		int result = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this client?", null, JOptionPane.YES_NO_OPTION);
 		if (result == JOptionPane.YES_OPTION) {
 			try {
 				UserInterface.removeClient((Opdrachtgever) clientsJList.getSelectedValue());
 				refreshClientsList(clientsJList);
 				clearFieldsOnPanel(clientFieldsJPanel);
 				toggleButtonStates();
 				JOptionPane.showMessageDialog(this, "Client removed!");
 			} catch (GUIException | IOException | WebserviceException ex) {
 				ex.printStackTrace();
 				JOptionPane.showMessageDialog(this, ex.getMessage());
 			}
 		}
 	}
 
 	// ================================================================================
 	// Refresh methods
 	// ================================================================================
 
 	// Refresh all PROJECT lists
 	private void refreshProjectsList(JList... lists) {
 		for (JList list : lists) {
 			int selectedIndex = list.getSelectedIndex();
 			DefaultListModel listmodel = new DefaultListModel();
 
 			for (Iterator<Project> it = UserInterface.getProjects().iterator(); it.hasNext();) {
 				Project p = it.next();
 				listmodel.addElement(p);
 			}
 
 			if (list.equals(projectsJList)) {
 				listmodel.addElement(NEWPROJECTITEM);
 				list.setModel(listmodel);
 			} else {
 				list.setModel(listmodel);
 			}
 			list.setCellRenderer(new ProjectCellRenderer());
 			list.setSelectedIndex(selectedIndex);
 		}
 	}
 
 	// Refresh all TASK lists
 	private void refreshTasksList(Project p, JList... lists) {
 		for (JList list : lists) {
 			int selectedIndex = list.getSelectedIndex();
 			DefaultListModel listmodel = new DefaultListModel();
 
 			for (Iterator<Taak> it = p.getTaken().iterator(); it.hasNext();) {
 				Taak t = it.next();
 				listmodel.addElement(t);
 			}
 
 			if (list == tasksJList) {
 				listmodel.addElement(NEWTASKITEM);
 				list.setModel(listmodel);
 			} else {
 				list.setModel(listmodel);
 			}
 			list.setCellRenderer(new TaskCellRenderer());
 			list.setSelectedIndex(selectedIndex);
 		}
 	}
 
 	// Refresh all CLIENT lists
 	private void refreshClientsList(JList... lists) {
 		for (JList list : lists) {
 			int selectedIndex = list.getSelectedIndex();
 			DefaultListModel listmodel = new DefaultListModel();
 
 			for (Iterator<Opdrachtgever> it = UserInterface.getClients().iterator(); it.hasNext();) {
 				Opdrachtgever o = it.next();
 				listmodel.addElement(o);
 			}
 
 			if (list == clientsJList) {
 				listmodel.addElement(NEWCLIENTITEM);
 				list.setModel(listmodel);
 			} else {
 				list.setModel(listmodel);
 			}
 			list.setSelectedIndex(selectedIndex);
 		}
 	}
 
 	// ================================================================================
 	// Loading methods
 	// ================================================================================
 
 	// Load info from PROJECT with index parameter
 	private void loadProjectInfo(int index) {
 		if (index != -1) {
 			Project p = UserInterface.getProjects().get(index);
 			nameJTextField.setText(p.getNaam());
 			projectStartDateChooser.setDate(new Date(p.getBegindatum()*1000));
 			projectEndDateChooser.setDate(new Date(p.getEinddatum()*1000));
 			try {
 				clientJLabel.setText(UserInterface.getClient(p.getOpdrachtgeverId()).toString());
 			} catch (DataInputException ex) {
 				ex.printStackTrace();
 				JOptionPane.showMessageDialog(this, ex.getMessage());
 			}
 			refreshTasksList(p, projectTasksJList);
 			percentageCompleteJProgressBar.setValue((int) (((Project) projectsJList.getSelectedValue()).getPercentageComplete() * 100));
 		}
 	}
 
 	// Load info from TASK with index parameter
 	private void loadTaskInfo(int index) throws GUIException {
 		if (index != -1) {
 			Taak t = (Taak) tasksJList.getSelectedValue();
 			taskNameJTextField.setText(t.getNaam());
 			taskStartDateChooser.setDate(new Date(t.getBegindatum()*1000));
 			taskEndDateChooser.setDate(new Date(t.getGeschatteEinddatum()*1000));
 			taskCommentJTextArea.setText(t.getCommentaar());
 			taskCompletedJCheckBox.setSelected(t.getCompleted());
 			DefaultListModel listmodel = new DefaultListModel();
 
 			for (Iterator<Tijdspanne> it = t.getTotaleTijd().iterator(); it.hasNext();) {
 				Tijdspanne ts = it.next();
 				listmodel.addElement(ts);
 			}
 
 			workedTimeJList.setModel(listmodel);
 			taskTotalTimeCompJLabel.setText("Total worked : " + Clock.longTimeToString(t.getTotaleWerktijd(), false));
 			taskTotalPauseCompJLabel.setText("Total paused : " + Clock.longTimeToString(t.getTotalePauze(), false));
 		}
 	}
 
 	// Load info from CLIENT with index parameter
 	private void loadClientInfo(int index) {
 		if (index != -1) {
 			Opdrachtgever o = (Opdrachtgever) clientsJList.getSelectedValue();
 			clientNameJTextField.setText(o.getNaam());
 			clientFirstNameJTextField.setText(o.getVoornaam());
 			clientCompanyJTextField.setText(o.getBedrijfsnaam());
 			clientEmailJTextField.setText(o.getEmail());
 			clientPhoneNumberJTextField.setText(o.getTelefoonnummer());
 		}
 	}
 
 	// ================================================================================
 	// Other methods (nakijken)
 	// ================================================================================
 
 	private void workClicked(java.awt.event.MouseEvent evt) {
 		try {
 			Project p = UserInterface.getCurrentProject();
 			if (p.getTaken().isEmpty()) {
 				throw new GUIException("Current project contains no tasks");
 			}
 			boolean taskAvailable = false;
 			for (Taak t : p.getTaken()) {
 				if (!t.overTijd()) {
 					taskAvailable = true;
 					break;
 				}
 			}
 			if (!taskAvailable) {
 				throw new GUIException("Current project contains no running tasks");
 			}
 			setVisible(false);
 			WorkDialog work = new WorkDialog(this, true, validator);
 			work.setVisible(true);
 			setVisible(true);
 			loadTaskInfo(tasksJList.getSelectedIndex());
 			toggleButtonStates();
 		} catch (GUIException ex) {
 			ex.printStackTrace();
 			JOptionPane.showMessageDialog(this, ex.getMessage());
 		}
 	}
 
 	// ================================================================================
 	// GUI methods
 	// ================================================================================
 
 	// Clear all fields on the panels in parameter
 	private void clearFieldsOnPanel(JPanel panel) {
 		Component[] clientPanelComps = panel.getComponents();
 		for (Component c : clientPanelComps) {
 			if (c instanceof JTextField) {
 				((JTextField)c).setText(null);
 			} else if(c instanceof JList){
 				((JList)c).setModel(new DefaultListModel());
 			} else if(c instanceof JTextArea){
 				((JTextArea)c).setText(null);
 			} else if(c instanceof JCheckBox){
 				((JCheckBox)c).setSelected(false);
 			} else if(c instanceof JDateChooser){
				((JDateChooser)c).setDate(new Date());
 			}
 		}
 	}
 
 	// Change button states (enabled / disabled) looking at selected indexes
 	private void toggleButtonStates() {
 		boolean taskSelected = tasksJList.getSelectedIndex() != -1;
 		boolean projectSelected = projectsJList.getSelectedIndex() != -1;
 		boolean clientSelected = clientsJList.getSelectedIndex() != -1;
 		saveTaskJButton.setEnabled(taskSelected);
 		removeTaskJButton.setEnabled(taskSelected);
 		setCurrentProjectJButton.setEnabled(projectSelected);
 		saveProjectJButton.setEnabled(projectSelected);
 		removeProjectJButton.setEnabled(projectSelected);
 		saveClientJButton.setEnabled(clientSelected);
 		removeClientJButton.setEnabled(clientSelected);
 	}
 
 	// ================================================================================
 	// Event handlers, FIXME afsplitsen!
 	// ================================================================================
 
 	private void setCurrentProjectJButtonActionPerformed(java.awt.event.ActionEvent evt) {
 		try {
 			UserInterface.setCurrentProjectIndex(projectsJList.getSelectedIndex());
 			currentProjectJLabel.setText("Current project: " + UserInterface.getCurrentProject().getNaam());
 			saveTaskJButton.setText("Save to " + UserInterface.getCurrentProject().getNaam());
 			refreshProjectsList(projectsJList, homeProjectsJList);
 			refreshTasksList(UserInterface.getCurrentProject(), tasksJList);
 			clearFieldsOnPanel(taskFieldsJPanel);
 		} catch (GUIException ex) {
 			ex.printStackTrace();
 			JOptionPane.showMessageDialog(this, ex.getMessage());
 		}
 	}
 
 	private void projectsJListValueChanged(javax.swing.event.ListSelectionEvent evt) {
 		if (projectsJList.getSelectedIndex() != -1) {
 			if (projectsJList.getSelectedValue().equals(NEWPROJECTITEM)) {
 				clearFieldsOnPanel(projectFieldsJPanel);
 				saveProjectJButton.setText("Save [new]");
 			} else {
 				loadProjectInfo(projectsJList.getSelectedIndex());
 				saveProjectJButton.setText("Save");
 			}
 			toggleButtonStates();
 		}
 	}
 
 	private void guiOpened(java.awt.event.WindowEvent evt) {
 		ingelogdJLabel.setText(UserInterface.getUser().getVolledigeNaam());
 		refreshProjectsList(projectsJList, homeProjectsJList);
 		refreshClientsList(clientsJList);
 	}
 
 	// Event handlers for all the edit fields
 	private void editFieldsFocused(java.awt.event.FocusEvent evt) {
 		toggleButtonStates();
 	}
 
 	private void clientsJListValueChanged(javax.swing.event.ListSelectionEvent evt) {
 		if (projectsJList.getSelectedIndex() != -1) {
 			if (clientsJList.getSelectedValue().equals(NEWCLIENTITEM)) {
 				clearFieldsOnPanel(clientFieldsJPanel);
 				saveClientJButton.setText("Save [new]");
 			} else {
 				loadClientInfo(clientsJList.getSelectedIndex());
 				saveClientJButton.setText("Save");
 			}
 			toggleButtonStates();
 		}
 	}
 
 	private void tasksJListprojectListValueChanged(javax.swing.event.ListSelectionEvent evt) {
 		if (projectsJList.getSelectedIndex() != -1) {
 			if (tasksJList.getSelectedValue().equals(NEWTASKITEM)) {
 				clearFieldsOnPanel(taskFieldsJPanel);
 				saveTaskJButton.setText("Save [new]");
 			} else {
 				try {
 					loadTaskInfo(tasksJList.getSelectedIndex());
 				} catch (GUIException ex) {
 					ex.printStackTrace();
 					JOptionPane.showMessageDialog(this, ex.getMessage());
 				} finally {
 					saveTaskJButton.setText("Save");
 				}
 			}
 			toggleButtonStates();
 		}
 	}
 
 	private void homeProjectsJListValueChanged(javax.swing.event.ListSelectionEvent evt) {
 		if (homeProjectsJList.getSelectedIndex() != -1) {
 			refreshTasksList(UserInterface.getProject(homeProjectsJList.getSelectedIndex()), homeTasksJList);
 		} else {
 			try {
 				refreshTasksList(UserInterface.getCurrentProject(), homeTasksJList);
 			} catch (GUIException ex) {
 				ex.printStackTrace();
 			}
 		}
 	}
 
 	private void homeProjectsJListKeyReleased(java.awt.event.KeyEvent evt) {
 		if (evt.getKeyCode() == 10) {
 			UserInterface.setCurrentProjectIndex(homeProjectsJList.getSelectedIndex());
 			refreshProjectsList(homeProjectsJList);
 			try {
 				refreshTasksList(UserInterface.getCurrentProject(), tasksJList);
 			} catch (GUIException ex) {
 				ex.printStackTrace();
 			}
 		}
 	}
 
 	// ================================================================================
 	// Variable declarations
 	// ================================================================================
 	private javax.swing.JLabel HomeProjectsJLabel;
 	private javax.swing.JTabbedPane JTabbedPane;
 	private javax.swing.JLabel TasksJLabel;
 	private javax.swing.JLabel clientJLabel;
 	private javax.swing.JLabel clientcompJLabel1;
 	private javax.swing.JLabel clientsJLabel;
 	private javax.swing.JList clientsJList;
 	private javax.swing.JPanel clientsJPanel;
 	private javax.swing.JLabel clockJLabel;
 	private javax.swing.JLabel currentProjectJLabel;
 	private javax.swing.JLabel enddatecompJLabel1;
 	private javax.swing.Box.Filler filler2;
 	private javax.swing.JPanel headerJPanel;
 	private javax.swing.JLabel homeJLabel;
 	private javax.swing.JPanel homeJPanel;
 	private javax.swing.JList homeProjectsJList;
 	private javax.swing.JList homeTasksJList;
 	private javax.swing.JLabel ingelogdJLabel;
 	private javax.swing.JScrollPane jScrollPane1;
 	private javax.swing.JScrollPane jScrollPane3;
 	private javax.swing.JScrollPane jScrollPane5;
 	private javax.swing.JScrollPane jScrollPane7;
 	private javax.swing.JScrollPane jScrollPane8;
 	private javax.swing.JTextField nameJTextField;
 	private javax.swing.JLabel namecompJLabel;
 	private javax.swing.JPanel optionsJPanel;
 	private javax.swing.JProgressBar percentageCompleteJProgressBar;
 	private javax.swing.JLabel percentageCompletecompJLabel;
 	private javax.swing.JLabel projectsJLabel;
 	private javax.swing.JList projectsJList;
 	private javax.swing.JPanel projectsJPanel;
 	private javax.swing.JButton removeClientJButton;
 	private javax.swing.JButton removeProjectJButton;
 	private javax.swing.JButton removeTaskJButton;
 	private javax.swing.JButton saveProjectJButton;
 	private javax.swing.JLabel scheduleJLabel;
 	private javax.swing.JPanel scheduleJPanel;
 	private javax.swing.JButton setCurrentProjectJButton;
 	private javax.swing.JLabel settingsJLabel;
 	private javax.swing.JLabel startdatecompJLabel;
 	private javax.swing.JLabel tasksJLabel;
 	private javax.swing.JList tasksJList;
 	private javax.swing.JPanel tasksJPanel;
 	private javax.swing.JLabel taskscompJLabel;
 	private javax.swing.JLabel titleLabel;
 	private javax.swing.JButton workJButton;
 	private JTextField clientNameJTextField;
 	private JTextField clientFirstNameJTextField;
 	private JTextField clientCompanyJTextField;
 	private JTextField clientEmailJTextField;
 	private JTextField clientPhoneNumberJTextField;
 	private JPanel clientFieldsJPanel;
 	private JButton saveClientJButton;
 	private JPanel taskFieldsJPanel;
 	private JLabel label_5;
 	private JTextField taskNameJTextField;
 	private JLabel label_6;
 	private JLabel label_7;
 	private JCheckBox taskCompletedJCheckBox;
 	private JLabel label_8;
 	private JLabel label_9;
 	private JLabel lblWorked;
 	private JLabel taskTotalTimeCompJLabel;
 	private JLabel taskTotalPauseCompJLabel;
 	private JButton saveTaskJButton;
 	private JTextArea taskCommentJTextArea;
 	private JList workedTimeJList;
 	private JPanel projectFieldsJPanel;
 	private JList tasksJList_1;
 	private JList projectTasksJList;
 	private JDateChooser taskStartDateChooser;
 	private JDateChooser taskEndDateChooser;
 	private JDateChooser projectStartDateChooser;
 	private JDateChooser projectEndDateChooser;
 }
