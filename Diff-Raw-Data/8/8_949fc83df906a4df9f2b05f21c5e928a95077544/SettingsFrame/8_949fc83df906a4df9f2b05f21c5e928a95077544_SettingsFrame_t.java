 package gui;
 import javax.swing.*;
 import javax.swing.event.InternalFrameAdapter;
 import javax.swing.event.InternalFrameEvent;
 import javax.swing.event.InternalFrameListener;
 
 import bll.ViewController;
 
 import java.awt.event.*;
 import java.awt.*;
 import java.util.HashSet;
 import java.util.Set;
 
 
 
 /**
  * 
  * @author Andreas Johnstad
  */
 @SuppressWarnings("serial")
 public class SettingsFrame extends JInternalFrame {
 
 	private static final Dimension PREFERRED_SIzE= new Dimension(
 			800,600
 	);
 
 	// Variables 
 	private ViewController viewCtrl;
 	private DisplaySettingsPanel dispset;
 	private HashtagSettingsPanel hashpan;
 	private TableSettingsPanel tablepanel;
 	private JLabel backgroundImageLabel;
 	private JButton saveButton;
 	
 	/**
 	 * Creates new form SettingsFrame
 	 */
 	public SettingsFrame(ViewController viewCtrl) {
 		super("Settings" , false, // resizable
 				true, // closable
 				false, // maximizable
 				false);// iconifiable'
 		
 		this.viewCtrl = viewCtrl;
 
 		setJMenuBar(null);
 		getJMenuBar();
 		initComponents();
 		initFrame();
 		
 
 		// Testing add hashtags to list
 		hashpan.setHashtagList(viewCtrl.getHashtags());initFrame();
 	}
 	public SettingsFrame() {
 		super("Settings" , false, // resizable
 				true, // closable
 				false, // maximizable
 				false);// iconifiable'
 		setJMenuBar(null);
 		getJMenuBar();
 		initComponents();
 		initFrame();
 
 	}
 
 
 	private void SendHastagUpdate(){
 
 		
 		
 	}
 	
 	/**
 
 	 */
 	private void initFrame(){
 		getContentPane().setLayout(null);
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
 		//setResizable(true);
 		setPreferredSize(PREFERRED_SIzE);
 		this.addInternalFrameListener(new closeAndSave());
 
 		setDoubleBuffered(true);
 		getContentPane().add(dispset);
 		getContentPane().add(tablepanel);
 		getContentPane().add(hashpan);
 		getContentPane().add(saveButton);
 		getContentPane().add(backgroundImageLabel);
 		
 		//setFrameIcon(new ImageIcon(getClass().getResource("/resource/img/settings.gif")));
 		setFrameIcon(null);
 		//pack();
 		setVisible(true);
 		
 	}
 	private void initComponents() {
 		// TableSettingsPanel declaration
 		tablepanel = new TableSettingsPanel();
 		tablepanel.setBounds(300, 10, 510, 510);
 		
 		// HashSettingsPanel declaration
 		hashpan = new HashtagSettingsPanel();
 		hashpan.setBounds(50, -12, 180, 320);
 		
 		// DisplaySettingsPanel
 		dispset = new DisplaySettingsPanel();
 		dispset.setBounds(60, 360, 185, 70);
 
 		// Save settings button declaration
 		saveButton = new JButton("Save Settings");
 		saveButton.setBounds(660, 530, 105, 28);
 		
 		
 		// Background Image declaration
 		backgroundImageLabel = new JLabel();
 		backgroundImageLabel.setIcon(new ImageIcon(getClass().getResource(
 		"/resource/img/backgr.jpg")));
 		backgroundImageLabel.setIconTextGap(0);
 		backgroundImageLabel.setPreferredSize(new Dimension(2560, 1600));
 		backgroundImageLabel.setBounds(0, 0, 933, 810);
 	}
 
    // Will be called when the frame is in the process of closing
    // Will call the necessary methods to update settings
 	class closeAndSave extends InternalFrameAdapter {
 
 		  public void internalFrameClosing(InternalFrameEvent internalFrameEvent) {
 		 System.out.println("internalFrameClosing");
 			  /*
 		  *  update,set displaysettings here
 		  *  
 		  *   Call:  updateDisplaySettings()
 		  */
			//  updateDisplaySettings();
 	    /*
 	     *  update,set hashtags here 
 	     *  
 	     *  Call: updateHashtags
 			  
 	     */ 
			 //updateHashtags();
 	    /*
 	     *  update thumnails   
 	     *  
 	     *  
 	     */
 		  }
 		}
 
 
 /// send updated display settings 
   
     // get updated displaysettings  and set them
     public void updateDisplaySettings(){
     	
     	viewCtrl.setRandom(dispset.getViewMode());
 		viewCtrl.setDisplayTime(dispset.getViewDelay());
     	
     }
     //  send updated hashtags to ViewCtrl
 	public void updateHashtags() {
 		Set<String>  hashtagList = hashpan.getHashtagList();
 		viewCtrl.updateHashtags(hashtagList);
 	}
 	//Get hashtags from DB to show on start up.
 	public void getHashTags(){
 		
 		
 	}
 }
