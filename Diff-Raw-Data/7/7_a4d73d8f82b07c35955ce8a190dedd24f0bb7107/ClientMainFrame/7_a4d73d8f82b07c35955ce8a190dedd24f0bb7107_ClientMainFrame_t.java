 /**
  * @(#)ClientMainFrame.java
 
  * @function: A Client View class for displaying GUI information and interaction
  *
  * @author: Hao Shen
  * @version 1.00 2010/11/8, Hao Shen
  * @modify 1.01 2010/11/16, Hao Shen
  * @modify 1.02 2010/11/18, Hao Shen, add edit&save buttons, add vertical scroll pane
  * @modify 1.03 2010/11/19, Hao Shen, change onlineusers from JList to JTextArea type, the eneditable for Jlists
  */
  
  /* Java UI packages & Events*/
 import java.awt.EventQueue;
 import java.awt.Frame;
 import javax.swing.JFrame;
 import java.awt.Canvas;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import java.awt.Color;
 import javax.swing.JLabel;
 import javax.swing.ImageIcon;
 import java.awt.Font;
 import javax.swing.JTable;
 import java.awt.Image;
 import java.awt.SystemColor;
 import java.awt.BorderLayout;
 import javax.swing.JList;
 import javax.swing.JTextField;
 import javax.swing.JButton;
 import javax.swing.table.DefaultTableModel;
 import java.applet.Applet;
 import java.awt.GraphicsConfiguration;
 import java.awt.event.KeyEvent;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.Vector;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.JList;
 import javax.swing.DefaultListModel;
 import javax.swing.ListSelectionModel;
 import javax.swing.JScrollBar;
 import javax.swing.JTextArea;
 import javax.swing.ScrollPaneConstants;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener; ; 
 
 /* Java3d packages */
 import com.sun.j3d.utils.applet.MainFrame;
 import com.sun.j3d.utils.behaviors.mouse.MouseBehavior;
 import com.sun.j3d.utils.behaviors.mouse.MouseRotate;
 import com.sun.j3d.utils.behaviors.mouse.MouseTranslate;
 import com.sun.j3d.utils.behaviors.mouse.MouseWheelZoom;
 import com.sun.j3d.utils.image.TextureLoader;
 import com.sun.j3d.utils.geometry.*;
 import com.sun.j3d.utils.universe.*;
 import javax.media.j3d.*;
 import javax.vecmath.*; 
  //others
 import java.lang.Math;
 
 import java.util.*;
 
 import javax.imageio.ImageIO;  
 import java.io.File;  
 import java.awt.*;  
 import java.awt.image.BufferedImage;  
 import java.io.*;
 
 public class ClientMainFrame extends JFrame{
 
 	private ClientModel clientModel;
 	private JTextField textField_Message;
 	private JButton btnSubmit;
 	private DefaultTableModel  onlineUsersTableModel;
 	private JTextArea textArea;
 	private JPanel panel_3DView;
 	private JTextField textField_Name;
 	private JButton btnEditName;
 	private JButton btnSave;
 	private JTextArea textArea_OnlineUses;
 	private JLabel label_2DCampus;
 	
 	/*3D things here*/
 	private SimpleUniverse simpleU;
 	private BranchGroup scene;
 	
 	public ClientMainFrame(ClientModel model) {
 		clientModel = model;
 		onlineUsersTableModel = new DefaultTableModel();
 		
 		initialize();
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		
 		/***************************************
 		 ************* 2D View ***************** 
 		 ***************************************/
 		//init the main frame
 		setResizable(false);
 		setTitle("Virtual Campus Tour System");
 		setBounds(100, 100, 900, 700);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		getContentPane().setLayout(null);
 		
		
 		//add the user info panel
 		JPanel panel_Info = new JPanel();
 		panel_Info.setBounds(621, 0, 253, 105);
 		getContentPane().add(panel_Info);
 		panel_Info.setLayout(null);
 		
		
 		//add a vatar 
 		JLabel label_Avatar = new JLabel("");
 		label_Avatar.setIcon(new ImageIcon("resources/icons/cc.jpg"));
 		label_Avatar.setBounds(5, 0, 89, 105);
 		panel_Info.add(label_Avatar);
 	
 		//add name textfield		
 		textField_Name = new JTextField();
 		textField_Name.setEditable(false);
 		textField_Name.setBounds(79, 25, 164, 27);
 		panel_Info.add(textField_Name);
 		textField_Name.setColumns(10);
 		
 		//add edit name button
 		btnEditName = new JButton("Edit Name");
 		btnEditName.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		btnEditName.setBounds(79, 63, 82, 27);
 		panel_Info.add(btnEditName);
 		
 		//add save button
 		btnSave = new JButton("Save");
 		btnSave.setFont(new Font("Tahoma", Font.PLAIN, 11));
 		btnSave.setBounds(178, 63, 61, 27);
 		panel_Info.add(btnSave);
 		
 		//add the 3D view panel
 		panel_3DView = new JPanel();
 		panel_3DView.setToolTipText("");
 		panel_3DView.setBackground(SystemColor.activeCaption);
 		panel_3DView.setBounds(0, 0, 611, 410);
 		getContentPane().add(panel_3DView);
 		
 		//add the online users panel
 		JPanel panel_Onlineusers = new JPanel();
 		panel_Onlineusers.setBounds(621, 105, 253, 243);
 		getContentPane().add(panel_Onlineusers);
 		panel_Onlineusers.setLayout(null);
 		
 		//add online usrs label
 		JLabel label_Onlineusers = new JLabel("Online users:");
 		label_Onlineusers.setBounds(0, 0, 175, 18);
 		panel_Onlineusers.add(label_Onlineusers);
 		
 		JScrollPane onlineUsersScrollPane = new JScrollPane((Component) null);
 		onlineUsersScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 		onlineUsersScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 		onlineUsersScrollPane.setBounds(0, 25, 253, 226);
 		panel_Onlineusers.add(onlineUsersScrollPane);
 		
 		textArea_OnlineUses = new JTextArea();
 		textArea_OnlineUses.setWrapStyleWord(true);
 		textArea_OnlineUses.setLineWrap(true);
 		textArea_OnlineUses.setEditable(false);
 		onlineUsersScrollPane.setViewportView(textArea_OnlineUses);
 		
 		//add 2D campus label and let it fixs the label size
 		label_2DCampus = new JLabel("");
 		label_2DCampus.setBounds(0, 407, 611, 255);
 		ImageIcon campus = new ImageIcon("resources/imgs/campus.jpg");
 		campus.setImage(campus.getImage().getScaledInstance(label_2DCampus.getWidth(), label_2DCampus.getHeight(), Image.SCALE_DEFAULT));
 		label_2DCampus.setIcon(campus);
 		getContentPane().add(label_2DCampus);
 		
 		//add message panel
 		JPanel panel_Message = new JPanel();
 		panel_Message.setBounds(620, 390, 264, 272);
 		getContentPane().add(panel_Message);
 		panel_Message.setLayout(null);
 		
 		//add the message label
 		JLabel lblAllMessage = new JLabel("All messages:");
 		lblAllMessage.setBounds(3, 0, 103, 14);
 		panel_Message.add(lblAllMessage);
 		
 		//add a text field for typing message
 		textField_Message = new JTextField();
 		textField_Message.setBounds(5, 245, 176, 25);
 		panel_Message.add(textField_Message);
 		textField_Message.setColumns(10);
 		
 		//add a submit button
 		btnSubmit = new JButton("Submit");
 		btnSubmit.setFont(new Font("Tahoma", Font.PLAIN, 13));
 		btnSubmit.setBounds(179, 245, 79, 25);
 		panel_Message.add(btnSubmit);
 		
 		//add message area
 		textArea = new JTextArea();
 		textArea.setBounds(1, 1, 257, 224);
 		textArea.setWrapStyleWord(true);
 		textArea.setLineWrap(true);
 		textArea.setEditable(false);
 		panel_Message.add(textArea);
 		
 		//add scroll bar to the text area
 		JScrollPane scrollPane = new JScrollPane(textArea);
 		scrollPane.setBounds(5, 20, 253, 226);
 		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 		panel_Message.add(scrollPane);
 		
 		/***************************************
 		 ************* 3D View ***************** 
 		 ***************************************/
 		//3D View 
 		panel_3DView.setLayout(new BorderLayout());
 		GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();
 		Canvas3D canvas3D = new Canvas3D(config);
 		panel_3DView.add("Center", canvas3D);
 		
 		// Create a 3d universe
 		simpleU = new SimpleUniverse(canvas3D);
 		TransformGroup vtg = simpleU.getViewingPlatform().getViewPlatformTransform();
 		Transform3D moveInside = new Transform3D();
	//	moveInside.setTranslation(new Vector3f(0.0f, -0.15f, 0.3f));
		moveInside.setTranslation(new Vector3f(0.0f, 0.0f, 0.6f));
 		vtg.setTransform(moveInside);
 			
 	}
 	
 	
 	
 	//return the root of 3D scene
 	public BranchGroup createSceneGraph(Location currentLocation) {
 	
 		ArrayList<byte[]> currentImages = new ArrayList<byte[]>();
 		currentImages = currentLocation.getLocationImages();
 		
 		// Create the root of the branch graph
 		BranchGroup objRoot = new BranchGroup();
 		
 		//enable this Scene to be removed from its parent which is SimpleU
 		objRoot.setCapability( BranchGroup.ALLOW_DETACH );
 	
 		// Create a transformation node for cube transformation
 		TransformGroup trans = new TransformGroup();
 		trans.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
 		trans.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
 	
 		/*create 6 surfaces of this cube*/
 		//!!!!!!!!Set the back surface of each face as the front surface!!!!!!!
 		PolygonAttributes polyAppear = new PolygonAttributes();
  		polyAppear.setCullFace(PolygonAttributes.CULL_NONE);
  			
  		// Define 6 Appearance objects
 		Appearance[] apperarance = new Appearance[6];
  		for(int i = 0; i < 6; i++)
  		{ 
  			// Load image from file
 			try
 			{
 				InputStream in = new ByteArrayInputStream(currentImages.get(i));
 				TextureLoader myloader = new TextureLoader(ImageIO.read(in), panel_3DView);
 				// Create texture
 				Texture2D mytext=(Texture2D) myloader.getTexture();
 				
 				// Bound the texture to the appearance
 				apperarance[i] = new Appearance();
 				
 				// Make the inside surface visible
 				apperarance[i].setPolygonAttributes(polyAppear);
 				apperarance[i].setTexture(mytext);
 				
 				TextureAttributes myTexAtt = new TextureAttributes();
 				myTexAtt.setTextureMode(TextureAttributes.MODULATE);
 				apperarance[i].setTextureAttributes(myTexAtt);
 				
 				Material mat = new Material();
 				apperarance[i].setMaterial(mat);
 			}
 			catch(Exception e)
 			{
 				e.printStackTrace();
 			}
  		}
  		
 		
 		// Create a cube with texture on each surface inside the cube
 		Box box = new Box(0.4f, 0.4f, 0.4f, Box.GENERATE_TEXTURE_COORDS, new Appearance()) ;
 		
 		/* Apply appearance to each surface */
 		box.getShape(Box.BACK).setAppearance(apperarance[0]);
 		box.getShape(Box.FRONT).setAppearance(apperarance[1]);
 		box.getShape(Box.TOP).setAppearance(apperarance[2]);
 		box.getShape(Box.BOTTOM).setAppearance(apperarance[3]);
 		box.getShape(Box.LEFT).setAppearance(apperarance[4]);
 		box.getShape(Box.RIGHT).setAppearance(apperarance[5]);
 		
 		// Add trans node to the sence root node
 		objRoot.addChild(trans);
 		
 		// Add the cube to trans	
 		trans.addChild(box);
 
 		/****mouse events*****/		
 		/* Cube rotation */
 		MouseRotate myMouseRotate = new MouseRotate();
 		myMouseRotate.setTransformGroup(trans);
 		myMouseRotate.setSchedulingBounds(new BoundingSphere());
 		
 		// Rotate by x axis
 		double yFactor = myMouseRotate.getYFactor();
 		myMouseRotate.setFactor(0, yFactor);
 		
 		// Add x axis rotation matrix to root
 		objRoot.addChild(myMouseRotate);
 		
 		// Create a keyboard event for rotating by y axis
 		SimpleBehavior myRotationBehavior = new SimpleBehavior(trans);
     	myRotationBehavior.setSchedulingBounds(new BoundingSphere());
     	
     	// Add y axis rotation matrix to root
     	objRoot.addChild(myRotationBehavior);
 		
 		/* Disable the translation and scalation*/
 		/*
 		// Cube translation
 		MouseTranslate translate=new MouseTranslate();
 		translate.setTransformGroup(trans);
 		translate.setSchedulingBounds(new BoundingSphere());
 		objRoot.addChild(translate);
 		
 		
 		// Cube scalation
 		MouseWheelZoom zoom=new MouseWheelZoom();
 		zoom.setTransformGroup(trans);
 		zoom.setSchedulingBounds(new BoundingSphere());
 		objRoot.addChild(zoom);
 		
 		*/
 		
 		// Have Java 3D perform optimizations on this scene graph.
 		objRoot.compile();
 		
 		// Return the root of sence
 		return objRoot;
 	}
 	
 	public void enableNameDiting(boolean status)
 	{
 		textField_Name.setEditable(status);
 	}
 	
 	public String getNewName()
 	{
 		return textField_Name.getText();
 	}
 	
 	public void addSubmitListener(ActionListener cal) {
         btnSubmit.addActionListener(cal);
     }
 	
 	public void addEditButtonListener(ActionListener cal) {
         btnEditName.addActionListener(cal);
     }
 	
 	public void addSaveButtonListener(ActionListener cal) {
         btnSave.addActionListener(cal);
     }
 	
 	
 	public void addWindowClosingListener(WindowAdapter cal)
 	{
 		this.addWindowListener(cal);
 	}
 	
 	public void addTextFieldKeyListener(ActionListener cal)
 	{
 		textField_Message.addActionListener(cal);
 	}
 	
 	public void addWindowMouseListener(MouseListener cal)
 	{
 		label_2DCampus.addMouseListener(cal);
 	}
 	
 	
 	public String getMessageInput()
 	{
 		return textField_Message.getText();
 	}
 	
 	public void updateNameTextField(String str)
 	{
 		textField_Name.setText(str);
 	}
 	
 	//udpate message table
 	public void udpateMessageTable(Date loginDateTime, ArrayList<Message> messages)
 	{
 		String displayMessage = "";
 		for(int i = 0; i < messages.size(); i++)
 		{
 			if(!messages.get(i).getDateTime().before(loginDateTime))
 			{
 				displayMessage += messages.get(i).getMessageSenderName() + " says: " + messages.get(i).getMessageContent() + "\n";
 			}
 		}
 		textArea.setText(displayMessage);
 		textField_Message.setText("");
 	}
 	
 	
 	//update onlineusers table
 	public void updateOnlineUsers(ArrayList<User> users)
 	{
 		String userNames = "";
 		for(int i = 0; i < users.size(); i++)
 		{
 			userNames += users.get(i).getUserName() + "\n";
 		}
 		
 		textArea_OnlineUses.setText(userNames);				
 	}
 
 	//clean the scene for repainting later 
 	public void cleanScene()
 	{
 		scene.detach();	
 	}
 		
 	
 	public void update3DView(Location currentLocation)
 	{
 		// Create a simple scene and attach it to the virtual universe
 		scene = createSceneGraph(currentLocation);
 
 		// Add the scene to the 
 		simpleU.addBranchGraph(scene);
 	}
 }
 
 //define a simple keyboard event behavior which is extended from Behavior class
 class SimpleBehavior extends Behavior {
 
    	private TransformGroup targetTG;
    	private Transform3D rotation = new Transform3D();
    	private double angle = 0.0;
 
 	// Constructor
     SimpleBehavior(TransformGroup targetTG) {
      	this.targetTG = targetTG;
     }
 
     // initialize the Behavior
     // set initial wakeup condition
     // called when behavior beacomes live
     public void initialize() {
      	// set initial wakeup condition
      	this.wakeupOn(new WakeupOnAWTEvent(KeyEvent.KEY_PRESSED));
    	}
 
     // behavior
     // called by Java 3D when appropriate stimulus occures
     public void processStimulus(Enumeration criteria) {
       	angle += 0.03;
       	rotation.rotY(angle);
       	targetTG.setTransform(rotation);
       	this.wakeupOn(new WakeupOnAWTEvent(KeyEvent.KEY_PRESSED));
     }
 }
