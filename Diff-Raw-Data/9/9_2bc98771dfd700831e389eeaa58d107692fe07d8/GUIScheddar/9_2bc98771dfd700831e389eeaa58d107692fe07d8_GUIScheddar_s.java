 package edu.brown.cs32.scheddar.gui;
 
 import java.awt.Dimension;
 import java.awt.GraphicsEnvironment;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 import edu.brown.cs32.scheddar.Main;
 import edu.brown.cs32.scheddar.Scheddar;
 import edu.brown.cs32.scheddar.XMLtoScheddar;
 
 
 /**
  * @author sdemane
  * 
  * This is the primary runnable class of the Scheddar application.
  * It initializes an application window waiting to be filled with
  * a ScheddarPanel which is project-specific. The ScheddarPanel is
  * initialized and added when a project is opened from a file or 
  * a new project is created.
  *
  */
 public class GUIScheddar extends JFrame {
 	
 	public static final boolean DEBUG = true;
 	
 	private static final long serialVersionUID = 1L;
 	private Dimension _screenSize;
 	ScheddarPane _scheddarPane;
 	JMenuBar _mb;
 	String group;
 	JFrame startFrame;
 	AbstractForm form;
 	
 	private boolean _funMode = false;
 	private JCheckBox checkbox;
 	
 	
 	public GUIScheddar() {
 		super();
 		_scheddarPane = null;
 		
 		
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setTitle("Scheddar");
 		
 		
 		setMaximizedBounds(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds());
 		//setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH); 
 		setSize(getMaximizedBounds().getSize());
 		initMenuBar();
 		setJMenuBar(_mb);
 		_screenSize = this.getContentPane().getSize();
 		
 		
 	}
 	
 	public Dimension getScreenSize() {
 		return this.getContentPane().getSize();
 	}
 	
 	private void renderScheddar(ScheddarPane pane) {
 		_scheddarPane = pane;
 		add(pane);
 		setVisible(true);
 		//setResizable(false);
 		_screenSize = this.getContentPane().getSize();
 		setGroup(_scheddarPane.getRootGroupName());
 	}
 	
 	public boolean isFun() {
 		return _funMode;
 	}
 	
 	
 	/**
 	 * @return Menu bar for the primary Scheddar app, complete with listeners
 	 */
 	private JMenuBar initMenuBar() {
 		
 		_mb = new JMenuBar();
 		
 		// creating file menu
 		JMenu fileMenu = new JMenu("File");
 		JMenuItem newScheddar = new JMenuItem("New");
 		JMenuItem open = new JMenuItem("Open");
 		JMenuItem save = new JMenuItem("Save");
 		JMenuItem options = new JMenuItem("Options");
 		JMenuItem exit = new JMenuItem("Exit");
 		
 		// adding listeners for file menu
 		newScheddar.addActionListener(new NewScheddarListener());
 		
 		open.addActionListener(new OpenScheddarListener());
 		
 		save.addActionListener(new SaveScheddarListener());
 		
 		options.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				new OptionsForm(_scheddarPane);
 			}
 		});
 		
 		exit.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// TODO:
 				boolean save = PopUps.popUpConfirmationBox();
 				System.exit(0);
 			}
 		});
 		
 		fileMenu.add(newScheddar);
 		fileMenu.add(open);
 		fileMenu.add(save);
 		fileMenu.add(exit);
 		fileMenu.add(options);
 		
 		// creating create menu
 		JMenu createMenu = new JMenu("Create");
 		JMenuItem person = new JMenuItem("New person");
 		JMenuItem group = new JMenuItem("New group");
 		JMenuItem meeting = new JMenuItem("New meeting");
 		
 		
 		// adding listeners for create menu
 		person.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				new PersonForm(_scheddarPane);
 			}
 		});
 		
 		group.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				new GroupForm(_scheddarPane);
 			}
 		});
 		
 		meeting.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				_scheddarPane.initializeMeeting();
 			}
 		});
 		
 		createMenu.add(person);
 		createMenu.add(group);
 		createMenu.add(meeting);
 		
 		// creating edit menu
 		JMenu editMenu = new JMenu("Edit");
 		JMenuItem editPerson = new JMenuItem("Edit person");
 		JMenuItem editGroup = new JMenuItem("Edit group");	
 		JMenuItem deletePerson = new JMenuItem("Delete person");
 		JMenuItem deleteGroup = new JMenuItem("Delete group");
 		
 		// adding listeners for delete menu
 		editPerson.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				new DeletePersonForm(_scheddarPane, false);
 			}
 		});
 		
 		editGroup.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				new DeleteGroupForm(_scheddarPane, false);
 			}
 		});		
 
 		deletePerson.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				new DeletePersonForm(_scheddarPane, true);
 			}
 		});
 		
 		deleteGroup.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				new DeleteGroupForm(_scheddarPane, true);
 			}
 		});		
 
 		editMenu.add(editPerson);
 		editMenu.add(editGroup);
 		editMenu.add(deletePerson);
 		editMenu.add(deleteGroup);
 		
 		// creating email menu
 		JMenu emailMenu = new JMenu("Email");
 		JMenuItem organizationEmail = new JMenuItem("Organization");
 		JMenuItem personEmail = new JMenuItem("Individual");
 		JMenuItem groupEmail = new JMenuItem("Group");
 		JMenuItem meetingEmail = new JMenuItem("Meeting");
 
 		// adding listeners for email menu
 		organizationEmail.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				new EmailForm(_scheddarPane);
 			}
 		});
 		
 		personEmail.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				new EmailForm(_scheddarPane);
 			}
 		});
 		
 		groupEmail.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				new EmailForm(_scheddarPane);
 			}
 		});
 		
 		meetingEmail.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				new EmailForm(_scheddarPane);
 			}
 		});
 		
 		emailMenu.add(organizationEmail);
 		emailMenu.add(personEmail);
 		emailMenu.add(groupEmail);
 		emailMenu.add(meetingEmail);
 		
 		
 		// creating view menu
 		JMenu viewMenu = new JMenu("View");
 		JMenuItem weekView = new JMenuItem("Week View");
 		JMenuItem monthView = new JMenuItem("Month View");
 		JMenuItem meetingView = new JMenuItem("Meeting");
 		
 		weekView.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				_scheddarPane._calendar.switchToWeek(_scheddarPane._calendar.getTime());
 			}
 		});
 		
 		monthView.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				_scheddarPane._calendar.switchToMonth(_scheddarPane._calendar.getTime());
 			}
 		});
 		
 		meetingView.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				_scheddarPane.initializeMeeting();
 			}
 		});
 		
 		viewMenu.add(weekView);
 		viewMenu.add(monthView);
 		viewMenu.add(meetingView);
 		
 		_mb.add(fileMenu);
 		_mb.add(createMenu);
 		_mb.add(editMenu);
 		//_mb.add(emailMenu);
 		_mb.add(viewMenu);
 		return _mb;
 	}
 	
 	public JMenuBar getMenu() {
 		return _mb;
 	}
 	
 	public void setGroup(String g) {
 		group = g;
 		if(g!=null && g.length()>0) {
 			setTitle("Scheddar - "+g);
 		}
 	}
 	
 	
 	public void showStartFrame() {
 		startFrame = new JFrame();
 		JLabel l1 = new JLabel("Welcome to Scheddar, the cheesy scheduling app!");
 		JLabel l2 = new JLabel("Would you like to start a new Scheddar schedule, or open an existing one?");
 		JButton newButton = new JButton("New Schedule");
 		JButton openButton = new JButton("Open Schedule");
 		
 		checkbox = new JCheckBox();
 		checkbox.setSelected(false);
 		checkbox.addItemListener(new ItemListener() {
 			
 			@Override
 			public void itemStateChanged(ItemEvent e) {
 				if (e.getSource() == checkbox) {
 					_funMode = e.getStateChange() == ItemEvent.SELECTED;
 				}
 			}
 		});
 		
 		
 		JPanel panel = new JPanel();
 		panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS));
 		l1.setAlignmentX(CENTER_ALIGNMENT);
 		l2.setAlignmentX(CENTER_ALIGNMENT);
 		newButton.setAlignmentX(CENTER_ALIGNMENT);
 		openButton.setAlignmentX(CENTER_ALIGNMENT);
 		checkbox.setAlignmentX(CENTER_ALIGNMENT);
 		
 		
 		
 		
 		newButton.addActionListener(new NewScheddarListener());
 		
 		openButton.addActionListener(new OpenScheddarListener());
 		
 		panel.add(Box.createVerticalStrut(20));
 		panel.add(l1);
 		panel.add(Box.createVerticalStrut(10));
 		panel.add(l2);
 		panel.add(Box.createVerticalStrut(15));
 		panel.add(newButton);
 		panel.add(Box.createVerticalStrut(6));
 		panel.add(openButton);
 		panel.add(Box.createVerticalStrut(10));
 		panel.add(checkbox);
 		panel.add(Box.createVerticalStrut(20));
 		
 		startFrame.add(panel);
 		
 		// adding horizontal border space
 		JPanel otherPanel = new JPanel();
 		otherPanel.setLayout(new BoxLayout(otherPanel,BoxLayout.X_AXIS));
 		otherPanel.add(Box.createHorizontalStrut(30));
 		otherPanel.add(panel);
 		otherPanel.add(Box.createHorizontalStrut(30));
 		
 		
 		startFrame.add(otherPanel);
 		startFrame.pack();
 		startFrame.setTitle("Scheddar");
 		startFrame.setLocationRelativeTo(null);
 		startFrame.setDefaultCloseOperation(EXIT_ON_CLOSE);
 		startFrame.setVisible(true);
 	}
 	
 	public class NewScheddarListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			form = new ScheddarForm(null,GUIScheddar.this);
 			form.addComponentListener(new ScheddarFormCloseListener());
 			
 		}
 	}
 	
 	public class ScheddarFormCloseListener implements ComponentListener {
 		@Override
 		public void componentShown(ComponentEvent arg0) {}
 		
 		@Override
 		public void componentResized(ComponentEvent arg0) {}
 		
 		@Override
 		public void componentMoved(ComponentEvent arg0) {}
 		
 		@Override
 		public void componentHidden(ComponentEvent arg0) {
 			ScheddarPane sp = form._scheddarPane;
 			if (sp == null) {
 				form.dispose();
 			} else {
 				renderScheddar(sp);
 				startFrame.dispose();
 				form.dispose();
 			}
 		}
 	}
 	
 	public class OpenScheddarListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			JFileChooser fc = new JFileChooser();
 			fc.setDialogTitle("Open Scheddar...");
 			fc.setFileFilter(new FileNameExtensionFilter("Scheddar files","xml"));
 			int retval = fc.showOpenDialog(GUIScheddar.this);
 			
 			if (retval == JFileChooser.APPROVE_OPTION) {
 				File scheddarFile = fc.getSelectedFile();
 				XMLtoScheddar x = new XMLtoScheddar(scheddarFile);
 				Scheddar s = x.parseScheddar();
 				ScheddarPane sp = new ScheddarPane(GUIScheddar.this, s);
 				renderScheddar(sp);
 				startFrame.dispose();
 			}
 		}
 	}
 	
 	public class SaveScheddarListener implements ActionListener {
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			JFileChooser fc = new JFileChooser();
 			fc.setDialogTitle("Save Scheddar...");
 			fc.setFileFilter(new FileNameExtensionFilter("Scheddar files","xml"));
 			fc.setSelectedFile(_scheddarPane._scheddar.getSaveFile());
 			int retval = fc.showSaveDialog(GUIScheddar.this);
 			
 			if (retval == JFileChooser.APPROVE_OPTION) {
 				File scheddarFile = fc.getSelectedFile();
 				_scheddarPane._scheddar.save(scheddarFile);
 			}
 		}
 	}
 	
 	
 	/**
 	 *                                                                              _ _                                                                               
 	 *                                                                 ___   ___   (/(/  ___                                                                          
 	 *                                                           ____ (___) (___)       (___)   _                                                                     
 	 *                                                          (____)                         (_)                                                                    
 	 *                                                      _ _                                       _ _                                                             
 	 *                                               ___   (/(/                                      (/(/  ___                                                        
 	 *                                          _   (___)                                                 (___)   _                                                   
 	 *                                         (_)                                                               (_)                                                  
 	 *                                    _     _ _   _ _                                                                _                                            
 	 *                                   ( \   (/(/  (/(/  ___   ___                                                    (/   ___                                      
 	 *                                   / /              (___) (___)   _     _                                             (___)   _                                 
 	 *                                   \_)                           (_)   (_)                                                   (_)                                
 	 *                                    _                                         _ _   _ _                                              _                          
 	 *                                   ( \                                       (/(/  (/(/  ___   ___                                  (/   ___                    
 	 *                                   / /                                                  (___) (___)   _     _                           (___)   _               
 	 *                                   \_)                                                               (_)   (_)                                 (_)              
 	 *                                    _                                         _ _                                 _ _   _ _                           _ _       
 	 *                                   ( \         ___                     ___   (/(/                                (/(/  (/(/  ___   ___               (/(/       
 	 *                                   / /    _   (___)   _           _   (___)         _                                       (___) (___)   _     _           _   
 	 *                                   \_)   (_)         (_)         (_)               (_)                                                   (_)   (_)         (_)  
 	 *                                    _      _           _                             _                                                                      _   
 	 *                                   ( \    (/          / )              ___          (/                     ___                                             ( \  
 	 *                                   / /          _    / /              (___)  ____                     _   (___)   _                                        / /  
 	 *                                   \_)         (_)  (_/                     (____)                   (_)         (_)                                       \_)  
 	 *                                    _                                                                  _                 _                                   _  
 	 *                                   ( \                                             ___                (/         ___    (/                           ___    (/  
 	 *                                   / /                                        _   (___)   _                 _   (___)                           _   (___)       
 	 *                                   \_)                                       (_)         (_)               (_)                                 (_)              
 	 *                                     _                                         _           _                                                                    
 	 *                                    (/   ___   ___                            (/          (/                                 ___               ___              
 	 *                                        (___) (___)   _     _                       _                                   _   (___)             (___)   _         
 	 *                                                     (_)   (_)                     (_)                                 (_)                           (_)        
 	 *                                                                  _ _   _ _                                              _           _                      _   
 	 *                                                                 (/(/  (/(/  ___   ___                                  (/          (/                     (_)  
 	 *                                                                            (___) (___)   _     _                            ____                           _   
 	 *                                                                                         (_)   (_)                          (____)                         (_)  
 	 *                                                                                                      _ _   _ _                                             _   
 	 *                                                                                                     (/(/  (/(/  ___   ___                                 ( \  
 	 *                                                                                                                (___) (___)   _     _                      / /  
 	 *                                                                                                                             (_)   (_)                     \_)  
 	 *                                                                                                                                          _ _   _ _          _  
 	 *                                                                                                                                         (/(/  (/(/  ___    (/  
 	 *                                                                                                                                                    (___)       
 	 *                                                                                                                                                                
 	 */
 	public static void main(String[] args) {
 		
 		try {
 			UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");
 		} catch (Exception e) {}
 		GUIScheddar gui = new GUIScheddar();
 		
 		gui.showStartFrame();
 		
 //		Scheddar scheddar = Main.getTestData();
 //		ScheddarPane sp = new ScheddarPane(gui,scheddar);
 //		gui.renderScheddar(sp);
 		
 		
 		
 	}
 }
