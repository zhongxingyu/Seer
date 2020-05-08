 package cu.cs.cpsc215.crazy_mail.ui;
 
 import cu.cs.cpsc215.crazy_mail.MainDriver;
 import cu.cs.cpsc215.crazy_mail.data.DataStore;
 import cu.cs.cpsc215.crazy_mail.ui.contacts.ViewContactsState;
 import cu.cs.cpsc215.crazy_mail.ui.messages.InboxState;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 import java.util.HashMap;
 
 import javax.imageio.ImageIO;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.ListSelectionModel;
 import javax.swing.border.LineBorder;
 
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 /**
  * 
  * @author Kevin Jett
  * @author Emmanuel John
  * 
  * The Parent frame of the mail client
  * 
  */
 public class MainFrame extends JFrame{
 	
 	/**
 	 * Primary frame class for the program. 
 	 * This sets up the window and global layout, along with providing a means to access the global elements.
 	 * Will eventually contain methods to handle state changing for the program.
 	 */
 	private static final long serialVersionUID = 2113187488340018117L;	
 	
 	//Global panels
 	JPanel mainPanel;
 	JPanel leftPanel;
 	JMenuBar mainMenu;
 	JPanel footerPanel;
 	
 	//Singleton pattern
 	static MainFrame inst;
     private JLabel statuslabel;
     private HashMap<String,FrameState> stateMap;
     private FrameState currentState = null;
     private BufferedImage iconImage;
     
 	public static void init(){
 		if(inst != null)
 			return;
 					
 		inst = new MainFrame();
 		inst.setVisible(true);
 		inst.sizeGlobalElements();
 		
 		//Show a message if the user hasn't made any accounts
         if(DataStore.get().getAccounts().size() == 0)
         {
         	//new ConfigurationDlg(null, null);
         }
 	}
 	public static MainFrame getInst(){
 		return inst;
 	}
 	
 	//Initial constructor - sets up the window
 	private MainFrame(){
             
             Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
             stateMap = new HashMap<String,FrameState>();
             
             setTitle("Crazy Mail");
             setSize(d.width-300,d.height-300);
             setLocationRelativeTo(null);
             setDefaultCloseOperation(EXIT_ON_CLOSE);
             setIcon();
             makeGlobalElements();
             
             switchState(ViewContactsState.get());
             
             final MainFrame t = this;
             this.addWindowListener(new WindowAdapter() {
                 @Override
                 public void windowClosing(WindowEvent e) {
                     t.dispose();
                     MainDriver.shutdown();
                 }
 
             });
             
             
 	}
 	
 	//Makes the global elements for the layout
 	private void makeGlobalElements(){
 		mainPanel = new JPanel();
 		mainPanel.setLayout(new BorderLayout());
         leftPanel = createSidePane();
        leftPanel.setSize(200,200);
 		footerPanel = createStatusPane();
 		mainPanel.setBorder(new LineBorder(Color.DARK_GRAY));
                 makeMenu();
 		
 		//Positioning
 		add(leftPanel, "West");
		
 		add(footerPanel,BorderLayout.SOUTH);
 		add(mainPanel,BorderLayout.CENTER);
 		add(mainMenu,BorderLayout.NORTH);
 
 		leftPanel.setPreferredSize(new Dimension((int)(this.getWidth()*0.25),200));
 
 	}
 	public JPanel createStatusPane(){
 	    JPanel panel = new JPanel();
 	    panel.setLayout(new FlowLayout(FlowLayout.LEADING));
             panel.setPreferredSize(new Dimension(this.getWidth(), 55));
 	    statuslabel = new JLabel("Ready");
 
 	    statuslabel.setFont(new Font("Sans Serif",Font.PLAIN,14));
 	    panel.add(statuslabel);
 	    
 	    return panel;
 	}
         
         public JPanel createSidePane(){
             
             JPanel main_panel = new JPanel();
             main_panel.setLayout(new BorderLayout());
             
             JPanel n_panel = new JPanel();
             n_panel.setLayout(new FlowLayout(FlowLayout.LEADING));
             JButton composebtn = new JButton("Compose Message");
             
             composebtn.addActionListener(new ActionListener(){
                 @Override
                 public void actionPerformed(ActionEvent ae){
                     new EmailTransmissionDlg(MainFrame.this);
                 }
             
             });
                         
             n_panel.add(composebtn);
             
             JPanel c_panel = new JPanel();
             c_panel.setLayout(new BorderLayout());
             final JList list = new JList(new String[]{"Contacts","Inbox",
                 "Sent"});
             list.setBackground(null);
             list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
             list.setFixedCellHeight(50);
             list.setSelectedIndex(0);
             
             list.addListSelectionListener(new ListSelectionListener() {
 
                 @Override
                 public void valueChanged(ListSelectionEvent lse) {
                     if(list.getSelectedIndex()==0){
                         switchState(ViewContactsState.get());
                     }
                     
                     if(list.getSelectedIndex() == 1){
                         switchState(InboxState.get());
                     }
                     
                     if(list.getSelectedIndex() == 2){
                         JOptionPane.showMessageDialog(MainFrame.this, "This feature is not yet available.");
                     }
                     
                 }
             });
             
             c_panel.add(new JScrollPane(list));
             
             main_panel.add(n_panel, "North");
             main_panel.add(c_panel);
             
             return main_panel;
         }
 	
 	//Makes the main menu
 	private void makeMenu(){
 		final MainFrame t = this;
 		mainMenu = new JMenuBar();
 		
 		JMenu fileMenu = new JMenu("File");
 		JMenu configMenu = new JMenu("Configuration");
 		JMenu helpMenu = new JMenu("Help");
 		
 		JMenuItem about = new JMenuItem("About Crazy Mail");
 		JMenuItem exit = new JMenuItem("Exit");
 		JMenuItem viewAccounts = new JMenuItem("Configurations");
 		JMenuItem addAccount = new JMenuItem("Add Configuration");
 		
 		fileMenu.add(exit);
 		helpMenu.add(about);
 		configMenu.add(addAccount);
 		configMenu.add(viewAccounts);
 		mainMenu.add(fileMenu);
 		mainMenu.add(configMenu);
 		mainMenu.add(helpMenu);
 		
 		//Menu listeners
 		addAccount.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				new ConfigurationDlg(null,null);
 			}
 			
 		});
 		
 		about.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0){
 				new SystemInformationDlg(t);
 			}
 		});
 		
 		viewAccounts.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0){
 				new ViewConfigurationsDlg(MainFrame.getInst());
 			}
 		});
 		exit.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {
 				MainFrame.getInst().dispose();
 				MainDriver.shutdown();
 			}
 		});
 	}
 	
 	/*Does any sizing on the global elements. Currently used to set the bounds for the left panel.
 	 * 	This is done outside of the constructor because panels will not return their proper dimensions inside of it.
 	 *  (Possibly because swing needs time to set up the window) */
 	public void sizeGlobalElements(){
 		int innerHeight = this.rootPane.getHeight();
 		int menuH = mainMenu.getHeight();
 		leftPanel.setBounds(0, menuH, 150, innerHeight - ( menuH + footerPanel.getHeight() ));
 	}
 	
 	//Sets the icon.
 	public void setIcon(){
 		iconImage = null;
 	       try {
 	           iconImage = ImageIO.read(new File("icon.png"));
 	           this.setIconImage(iconImage);
 	       } catch (IOException e){
 	       }
 	}
 	
 	public BufferedImage getIconImage()
 	{
 		return iconImage;
 	}
 	
 	//Getters for all the global elements. Will probably replace with more specific functions later.
 	public JPanel getMainPanel(){
 		return mainPanel;
 	}
 	
 	public JPanel getLeftPanel(){
 		return leftPanel;
 	}
 	
 	public JMenuBar getMainMenu(){
 		return mainMenu;
 	}
         
     public void setStatus(String s){
         statuslabel.setText(s);
     }
     
     public String getStatus(){
         return statuslabel.getText();
     }
     
     public void switchState(FrameState state){
     	
     	//If state is unchanged, do nothing
     	if(state == currentState)
     	{
     		return;
     	}
     	
     	//If there is a current state, remove and hide it
     	if(currentState != null)
     	{       
     		mainPanel.removeAll();
     		currentState.onHide();
                 
                 mainPanel.validate();
                 mainPanel.repaint();
     	}
     	
     	//Try to retrieve state from existing map
     	if(stateMap.containsKey(state.getName()))
     	{
     		currentState = stateMap.get(state.getName());
     	}
     	else //If it's not there, put it in the map
     	{
     		stateMap.put(state.getName(),state);
     		currentState = state;
     	}
     	
     	//Add to main panel
                
 		mainPanel.add(currentState.getPanel());
 		currentState.onShow();
                 mainPanel.setBackground(new Color(240,255,240));
                 
                 mainPanel.validate();
                 mainPanel.repaint();
     		
     }
     
     
 
 }
