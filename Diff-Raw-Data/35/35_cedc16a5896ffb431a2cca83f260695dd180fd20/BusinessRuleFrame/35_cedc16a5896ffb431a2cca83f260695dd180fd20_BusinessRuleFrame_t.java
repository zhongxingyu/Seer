 package gui.view;
 
 import generator.Generator;
 import gui.view.brf.BRList;
 import gui.view.brf.LanguageList;
 import gui.view.brf.SelectBusinessRules;
 
 import java.awt.BorderLayout;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.util.ArrayList;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.KeyStroke;
 
 import model.BusinessRule;
 import model.BusinessRuleType;
 import connections.SourceDAO;
 import connections.SourceDatabaseJDBC_Oracle;
 import controls.Register;
 
 @SuppressWarnings("serial")
 public class BusinessRuleFrame extends JFrame
 {
 	private Register mc = Register.getInstance();
 	
 	private BRList BRList;
 	
 	private JFrame myframe;
 
 	private JPanel centerPanel;
 	private JPanel bottomPanel;
 
 	private SelectBusinessRules generateList;
 	private SelectBusinessRules selectList;
 	private JComboBox selectLanguage;
 
 	private ArrayList<String> ruleTypes;
 	private JButton closeB;
 	private JButton genererenB;
 	private JButton selectedToGenerateB;
 	private JButton generateToSelectedB;
 	private JButton allToGenerateB;
 	private JButton allToSelectedB;
 	private JLabel pleaseWaitLabel = new JLabel("Receiving Business Rules");
 	
 	private JMenuBar menuBar;
 	private JMenu file_menu, database_menu, about_menu;
 	private JMenuItem menuItem_close, menuItem_setDatabase, menuItem_info;
 	
 	public BusinessRuleFrame()
 	{
 		new JFrame();
 		setResizable(false);
 		myframe = this;
 		myframe.setResizable(false);
 		closeB = new JButton("Close");
 		genererenB = new JButton("Generate");
 		selectedToGenerateB = new JButton(">  ");
 		generateToSelectedB = new JButton("<  ");
 		allToGenerateB = new JButton(">> ");
 		allToSelectedB = new JButton("<< ");
 		startingUP();
 	}
 	
 	public void startingUP(){
 		Toolkit tk = Toolkit.getDefaultToolkit();
 		Dimension screenSize = tk.getScreenSize();
 		setSize(165, 75);
 		int screenHeight = screenSize.height;
 		int screenWidth = screenSize.width;
 		setLocation((screenWidth / 2) - (getWidth() / 2), (screenHeight / 2) - (getHeight() / 2));
 		add(pleaseWaitLabel);
 		setVisible(true);
 
 		SourceDAO mc = Register.getSourceConnection();
 		mc.loadObjects();
 		
 		this.init();	
 	}
 	
 	public void init()
 	{
 		BRList = new BRList();
 		
 		myframe.remove(pleaseWaitLabel);
 
 		setSize(700,600);
 		
 		Toolkit tk = Toolkit.getDefaultToolkit();
 		Dimension screenSize = tk.getScreenSize();
 		int screenHeight = screenSize.height;
 		int screenWidth = screenSize.width;
 		setLocation((screenWidth / 2) - (getWidth() / 2), (screenHeight / 2) - (getHeight() / 2));
 
 		
 		
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		//pack();
 		setTitle("Business Rule Generator");		
 		
 		
 		//MenuBar config...
 		menuBar = new JMenuBar();
 		
 		file_menu = new JMenu("File");
 		file_menu.setMnemonic(KeyEvent.VK_F);
 		file_menu.getAccessibleContext().setAccessibleDescription("Menu for doing basic window operations");
 		
 		menuItem_close = new JMenuItem("Close", KeyEvent.VK_C);
 		menuItem_close.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, ActionEvent.ALT_MASK));
 		file_menu.add(menuItem_close);
 
 		menuItem_close.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				System.exit(0);
 			}
 		});
 		
 		menuBar.add(file_menu);
 		
 		database_menu = new JMenu("Database");
 		database_menu.setMnemonic(KeyEvent.VK_D);
 		database_menu.getAccessibleContext().setAccessibleDescription("Menu for setting database connection");
 		
 		menuItem_setDatabase = new JMenuItem("Set Target Database", KeyEvent.VK_D);
 		menuItem_setDatabase.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.ALT_MASK));
 		database_menu.add(menuItem_setDatabase);
 		
 		menuBar.add(database_menu);
 		
 		about_menu = new JMenu("About");
 		about_menu.setMnemonic(KeyEvent.VK_A);
 		about_menu.getAccessibleContext().setAccessibleDescription("About this software");
 		
 		menuItem_info = new JMenuItem("Info", KeyEvent.VK_I);
 		menuItem_info.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.ALT_MASK));
 		about_menu.add(menuItem_info);
 		
 		menuBar.add(about_menu);
 		
 		myframe.setJMenuBar(menuBar);
 		
 		
 		//MenuBar config end...
 		
 		setLayout(new BorderLayout());
 		
 		ruleTypes = new ArrayList<String>();
 		ruleTypes.add("All Business Rules");
 		for(BusinessRuleType t : mc.getBrt_list()){
 			ruleTypes.add(t.getName());
 		}
 				
 		
 		selectList = new SelectBusinessRules("SELECTING", BRList, ruleTypes.toArray());
 		generateList = new SelectBusinessRules("GENERATING", BRList, ruleTypes.toArray());
 		
 		addPanelNorth();
 		myframe.getContentPane().add(selectList, BorderLayout.WEST);
 		myframe.getContentPane().add(generateList, BorderLayout.EAST);
 		addPanelCenter();
 		addPanelSouth();
 		
 		setVisible(true);
 	}
 	
 	private void addPanelNorth(){
 		
 		myframe.getContentPane().add(new JLabel("Business Rule Generator"), BorderLayout.NORTH);
 	}
 	
 	private void addPanelCenter(){
 		centerPanel = new JPanel();
 		myframe.getContentPane().add(centerPanel, BorderLayout.CENTER);
 		centerPanel.setSize(new Dimension(100, 500));
 		
 		JPanel knoppenPanel = new JPanel();
 		knoppenPanel.setSize(new Dimension(50,500));
 		knoppenPanel.setLayout(new BoxLayout(knoppenPanel, BoxLayout.Y_AXIS));
 		
 		knoppenPanel.add(selectedToGenerateB);
 		knoppenPanel.add(new JPanel());
 		knoppenPanel.add(generateToSelectedB);
 		knoppenPanel.add(new JPanel());
 		knoppenPanel.add(allToGenerateB);
 		knoppenPanel.add(new JPanel());
 		knoppenPanel.add(allToSelectedB);
 		
 		selectedToGenerateB.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				BRList.SetToGenerate(selectList.getGeselecteerdeSelected());
 				generateList.update(null, null);
 				selectList.update(null, null);
 			}
 		});
 		
 		generateToSelectedB.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				BRList.RemoveFromGenerate(generateList.getGeselecteerdeSelected());
 				generateList.update(null, null);
 				selectList.update(null, null);
 			}
 		});
 
 		allToGenerateB.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				BRList.allToGenerateB();
 				generateList.update(null, null);
 				selectList.update(null, null);
 			}
 		});
 		
 		allToSelectedB.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				BRList.allToSelectedB();
 				generateList.update(null, null);
 				selectList.update(null, null);
 			}
 		});
 		
 		
 		
 		centerPanel.add(knoppenPanel);
 		selectedToGenerateB.setAlignmentX(CENTER_ALIGNMENT);
 		generateToSelectedB.setAlignmentX(CENTER_ALIGNMENT);
 		allToGenerateB.setAlignmentX(CENTER_ALIGNMENT);
 		allToSelectedB.setAlignmentX(CENTER_ALIGNMENT);
 		allToSelectedB.setAlignmentY(Container.BOTTOM_ALIGNMENT);
 	}
 	
 	private void addPanelSouth(){
 		bottomPanel = new JPanel();
 				
 		selectLanguage = new JComboBox(LanguageList.getAllLanguages().toArray());
 		bottomPanel.add(selectLanguage);
 		
 		
 		bottomPanel.add(genererenB);
 		bottomPanel.add(closeB);
 		
 		closeB.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				System.exit(0);
 			}
 		});
 		
 
 		genererenB.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				Generator gen = LanguageList.getGenerator(getGenerateLanguage());
 				String s = "";
 				for(BusinessRule br : BRList.getBusinessToGenerate()) {
					s += gen.generateBR(br) + "\n/\n\n";
 				}
				s = s.substring(0, s.length()-4);
 				
 				GeneratedCode gc = new GeneratedCode(s);
 				gc.setVisible(true);
 			}
 		});
 		
 		myframe.getContentPane().add(bottomPanel, BorderLayout.SOUTH);
 	}
 	
 	public ArrayList<BusinessRule> getGeselecteerdeSelected(){
 		return selectList.getGeselecteerdeSelected();
 	}
 	
 	public ArrayList<BusinessRule> getGeselecteerdeGenerate(){
 		return generateList.getGeselecteerdeSelected();
 	}
 
 	public String getGenerateLanguage(){
 		return selectLanguage.getSelectedItem().toString();
 	}	
 }
