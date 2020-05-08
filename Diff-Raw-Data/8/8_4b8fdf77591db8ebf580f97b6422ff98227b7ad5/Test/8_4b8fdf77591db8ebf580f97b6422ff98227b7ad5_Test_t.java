 package gui;
 
 import java.awt.EventQueue;
 import javax.swing.text.Element;
 import javax.swing.JFrame;
 
 import casa.CASAProcess;
 import casa.Status;
 
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.layout.RowSpec;
 import com.jgoodies.forms.factories.FormFactory;
 
 import javax.swing.JPanel;
 import java.awt.CardLayout;
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JFileChooser;
 import javax.swing.JOptionPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JTextArea;
 import javax.swing.ScrollPaneConstants;
 
 import java.awt.Panel;
 import java.awt.BorderLayout;
 
 import java.awt.FlowLayout;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import javax.swing.JCheckBox;
 import javax.swing.JScrollPane;
 import javax.swing.JTextPane;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.TextArea;
 import java.awt.ScrollPane;
 import java.awt.Label;
 import java.awt.Scrollbar;
 import java.awt.Toolkit;
 
 
 public class Test{
 
 	private JFrame frmRoboteach;

 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					Test window = new Test();
 					window.frmRoboteach.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 		
 	}
 
 	/**
 	 * Create the application.
 	 */
 	public Test() {
 		initialize();
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	public void initialize() {
 /*****************************************************************************************************************************************
 * Main Page initialization and components
 *****************************************************************************************************************************************/
 		frmRoboteach = new JFrame();
 		frmRoboteach.setTitle("Robo-Teach");
 		frmRoboteach.setBounds(100, 100, 800, 600);
 		frmRoboteach.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frmRoboteach.getContentPane().setLayout(new CardLayout(0, 0));
 		
 		//get size of the screen
 		Toolkit toolkit = Toolkit.getDefaultToolkit();  
 		Dimension screenSize = toolkit.getScreenSize(); 
 		
 		//TO SHOW THE WINDOW IN THE CENTER OF THE SCREEN
 		int x = (screenSize.width - frmRoboteach.getWidth()) / 2;  
 		int y = (screenSize.height - frmRoboteach.getHeight()) / 2; 
 		frmRoboteach.setLocation(x,y);
 		
 		//USER LOGIN
 		new Login(frmRoboteach);
 		
 		//Creating the base panel
 		final JPanel BasePanel = new JPanel();
 		frmRoboteach.getContentPane().add(BasePanel, "name_20408249708069");
 		BasePanel.setLayout(new CardLayout(0, 0));
 		//Data needed for resizing
 		JPanel TitlePage = new JPanel();
 		BasePanel.add(TitlePage, "TitlePage");
 		TitlePage.setLayout(new FormLayout(new ColumnSpec[] {
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("default:grow"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("default:grow"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,},
 			new RowSpec[] {
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("bottom:default:grow"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("default:grow"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,}));
 		
 /*****************************************************************************************************************************************
 * TitlePage initialization and components
 *****************************************************************************************************************************************/
 		
 		//Title screen picture, add picture to folder and change file name here
 		String imgStr = "Pictures/TitlePicture.png";
 		ImageIcon TitlePicture = new ImageIcon(imgStr);
 		//Creating the picture panel
 		Panel TitleLabelPanel = new Panel();
 		TitlePage.add(TitleLabelPanel, "2, 2, 25, 9, fill, fill");
 		TitleLabelPanel.setLayout(new BorderLayout(0, 0));
 		JLabel TitleLabel = new JLabel(" ", TitlePicture, JLabel.CENTER);
 		//Creating the button panel
 		JPanel TitleButtonPanel = new JPanel();
 		TitlePage.add(TitleButtonPanel, "2, 12, 25, 3, fill, fill");
 		TitleButtonPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
 		//Start Button Activity
 		JButton StartButton = new JButton("Start");
 		StartButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				((CardLayout) BasePanel.getLayout()).show(BasePanel, "TabPage");
 			}
 		});
 		//User Manual Button Activity
 		JButton UserManualButton = new JButton("User Manual");
 		UserManualButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 			}
 		});
 		
 		//Put everything onto the title panel
 		TitleButtonPanel.add(StartButton);
 		TitleLabelPanel.add(TitleLabel);
 		TitleButtonPanel.add(UserManualButton);
 		
 /*****************************************************************************************************************************************
 * TitlePage initialization and components
 *****************************************************************************************************************************************/	
 		JTabbedPane TabPage = new JTabbedPane(JTabbedPane.TOP);
 		BasePanel.add(TabPage, "TabPage");
 		
 		JPanel WelcomeTab = new JPanel();
 		TabPage.addTab("Welcome", null, WelcomeTab, null);
 		WelcomeTab.setLayout(new FormLayout(new ColumnSpec[] {
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("default:grow"),},
 			new RowSpec[] {
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("default:grow"),}));
 		
 		String imgStr2 = "Pictures/WelcomePicture.png";
 		final ImageIcon WelcomePicture = new ImageIcon(imgStr2);
 		String imgStr3 = "Pictures/GettingStartedPicture.png";
 		final ImageIcon GettingStartedPicture = new ImageIcon(imgStr3);
 		
 		JScrollPane scrollPane = new JScrollPane();
 		WelcomeTab.add(scrollPane, "4, 2, 1, 31, fill, fill");
 		
 		final JLabel WelcomeLabel = new JLabel(" ", WelcomePicture, JLabel.CENTER);
 		scrollPane.setViewportView(WelcomeLabel);
 		
 		JButton WelcomeButton = new JButton("Welcome");
 		WelcomeButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				WelcomeLabel.setIcon(WelcomePicture);
 			}
 		});
 		WelcomeTab.add(WelcomeButton, "2, 2");
 		
 		JButton GettingStartedButton = new JButton("Getting Started");
 		GettingStartedButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				WelcomeLabel.setIcon(GettingStartedPicture);
 			}
 		});
 		WelcomeTab.add(GettingStartedButton, "2, 4");
 		
 		JButton BackToTitleButton = new JButton("Back to Title");
 		BackToTitleButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				((CardLayout) BasePanel.getLayout()).show(BasePanel, "TitlePage");
 			}
 		});
 		WelcomeTab.add(BackToTitleButton, "2, 6");
 
 /*****************************************************************************************************************************************
 * LessonsTab initialization and components
 ******************************************************************************************************************************************/	
 		JPanel LessonsTab1;
 		LessonsTab newLessonsTab = new LessonsTab();
 		LessonsTab1 = newLessonsTab.initialize();
 		TabPage.addTab("Lessons", null, LessonsTab1, null);
 
 /*****************************************************************************************************************************************
 * ProgramTab initialization and components
 ******************************************************************************************************************************************/	
 		
 		JPanel ProgramTab1;
 		ProgramTab newTab = new ProgramTab();
 		ProgramTab1 = newTab.initialize();
 		TabPage.addTab("Program", null, ProgramTab1, null);
 		
 		
 /*****************************************************************************************************************************************
 * ChallengeTab initialization and components
 ******************************************************************************************************************************************/			
 		JPanel ChallengeTab1;
 		ChallengesTab newChallengeTab = new ChallengesTab();
 		ChallengeTab1 = newChallengeTab.initialize();
 		TabPage.addTab("Challenges", null, ChallengeTab1, null);
 	}
 }
