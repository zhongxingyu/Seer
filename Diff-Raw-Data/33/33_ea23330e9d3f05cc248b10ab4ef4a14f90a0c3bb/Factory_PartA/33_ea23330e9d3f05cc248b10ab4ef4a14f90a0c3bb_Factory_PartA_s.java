 package state;
 
 import agents.*;
 import agents.interfaces.*;
 import gui.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.geom.*;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.HashMap;
 
 import javax.swing.*;
 
 public class Factory_PartA extends JFrame implements ActionListener, MouseListener {
 	static final int WIDTH = 800;
 	static final int HEIGHT = 600;
 	
 	// GUI Elements
	CardLayout cards;
 	final Rectangle2D.Double bkgrnd = new Rectangle2D.Double(0, 0, WIDTH, HEIGHT);
 
	// Classes that do stuff
 	public FCSAgent fcs;
     public ConveyorAgent conveyor;
     public KitRobotAgent kitRobot;
     public KitVision camera;
     //public PartRobot partRobot;
     public KitStand stand;
 	public ArrayList<GUI_Component> compList;
 	public GUI_Conveyor guiconveyor;
 	public GUI_KitStand guiKitStand;
 	public GUI_KitRobot guiKitRobot;
 	public GUI_Camera guicamera;
 
 	public Factory_PartA() {
 		//Setup member variables
 		compList = new ArrayList<GUI_Component>();
 		stand = new KitStand();
 		fcs = new FCSAgent();
 		conveyor = new ConveyorAgent();
 		fcs.setConveyor(conveyor);
 		kitRobot = new KitRobotAgent(conveyor, stand);
		guiKitStand = new GUI_KitStand(300,300);
 		guiconveyor = new GUI_Conveyor();
 		guiKitRobot = new GUI_KitRobot(guiconveyor, guiKitStand);
		
		
		
		
		this.addMouseListener(this);
		
 		
 		//Add GUI_Components in the correct draw order
 		compList.add(guiKitStand);
 		compList.add(guiconveyor);
 		compList.add(guiKitRobot);
		compList.add(guicamera);
 		
		//Setup this (Factory_PartB)
 		this.setContentPane(new JPanel() {
 			public void paint(Graphics g) { // called upon app.repaint() in Game class
 				Graphics2D g2D = (Graphics2D) g;
 				g2D.setColor(new Color(230, 230, 230));
 				g2D.fill(bkgrnd);
 
 				// CALLS ALL paintComponent methods of components to be displayed
 				for(GUI_Component c : compList)
 					c.paintComponent(this, g2D);				
 			}
 		});
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		this.setSize(WIDTH, HEIGHT);
 		this.setResizable(false);
 		this.setTitle("Nemo Factory v0 Part A");
 		this.setVisible(true);
 
 
 
 		// starting threads
 		fcs.startThread();
 		conveyor.startThread();
 		kitRobot.startThread();
 		//partRobot.startThread();
 
 
 
 		// test simulation for v0
 		/*Map<String,Integer> kitConfig = new HashMap<String,Integer>();
 		Kit sampleKit = new Kit(kitConfig, 0);
 		conveyor.msgHeresKit(sampleKit);
 		conveyor.msgHeresKit(sampleKit);*/
 
    	
 
 	}
 
 	//*** DoXXX API ***
 	public void doAddKits() {
                 System.out.println("doAddKits() called.");
                 Map<String,Integer> kitConfig = new HashMap<String,Integer>();
         		Kit sampleKit = new Kit(kitConfig, 0);
         		conveyor.msgHeresKit(sampleKit);
         		conveyor.msgHeresKit(sampleKit);
 
 		
 		//guiKitRobot.
 	}
    
         
 	//Timer callback
 	public void actionPerformed(ActionEvent ae) {
 		// This will be called by the Timer
 		// CALLS ALL updateGraphics methods of components to be displayed
                 for(GUI_Component c : compList)
                     c.updateGraphics();
 		
 		this.repaint();
 	}
 	
 	//*** TEMPORARY: mouse listener methods ***
 	public void mousePressed(MouseEvent me) {
 		guicamera.takePicture();
 	}
 	
 	public void mouseClicked(MouseEvent arg0) {}
 	public void mouseEntered(MouseEvent arg0) {}
 	public void mouseExited(MouseEvent arg0) {}
 	public void mouseReleased(MouseEvent arg0) {}
 	
 	public static void main(String[] args) {
 		Factory_PartA app = new Factory_PartA();
 		new Timer(15, app).start();
 
 	}
 }
