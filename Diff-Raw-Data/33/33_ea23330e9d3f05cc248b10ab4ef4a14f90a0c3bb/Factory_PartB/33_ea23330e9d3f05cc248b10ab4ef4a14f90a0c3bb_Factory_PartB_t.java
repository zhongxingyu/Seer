 package state;
 
 import agents.*;
 import agents.interfaces.*;
 import gui.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.geom.*;
 import java.util.ArrayList;
 import java.util.Map;
 import java.util.Collections;
 import java.util.HashMap;
 
 import javax.swing.*;
 
 public class Factory_PartB extends JFrame implements ActionListener {
 	static final int WIDTH = 800;
 	static final int HEIGHT = 600;
 	
 	// GUI Elements
	//CardLayout cards;
 	final Rectangle2D.Double bkgrnd = new Rectangle2D.Double(0, 0, WIDTH, HEIGHT);
 
 	// Classes that do stuff
 	public GUI_PartRobot guiPartRobot;
 	public PartRobotAgent partRobot;
 	public java.util.List<GUI_Part> guiPartList = Collections.synchronizedList(new ArrayList<GUI_Part>());
 	public ArrayList<GUI_Nest> guiNestList;
 	public ArrayList<NestAgent> nestAgents;
 	public ArrayList<GUI_Component> compList;
 	public GUI_KitStand guiKitStand;
 	public KitStand kitStand;
 	public PartVisionAgent vision;
 
 	public Factory_PartB() {
 		//Setup member variables
 		guiNestList = new ArrayList<GUI_Nest>(8);
 		nestAgents = new ArrayList<NestAgent>(8);
 
 		compList = new ArrayList<GUI_Component>();
 		
 		vision = new PartVisionAgent();
 
 		guiKitStand = new GUI_KitStand(100, 100);
 		kitStand = new KitStand();
 		
 		guiPartRobot = new GUI_PartRobot(this,partRobot,0,0);
 		partRobot = new PartRobotAgent(nestAgents, kitStand, this);
 		
 		for(int i=0; i<4; i++) {
 			GUI_NestPair p = new GUI_NestPair(2*i, 2*i+1, (i/2) * GUI_Nest.WIDTH + 400, (i%2) * 2 * GUI_Nest.HEIGHT + 75, this);
 			
 			//Nest 1
 			nestAgents.add(new NestAgent("aefaiefaeifaenntotoeaiaeof", partRobot, p.getNest1(), vision));
 			guiNestList.add(p.getNest1());
 			
 			//Nest 2
 			nestAgents.add(new NestAgent("aefaiefaeifaenntotoeaiaeof", partRobot, p.getNest2(), vision));
 			guiNestList.add(p.getNest2());
 		}
 
 		//Add GUI_Components in the correct draw order
 		compList.add(guiKitStand);
 		for(GUI_Nest n : guiNestList)
 			compList.add(n);
 		compList.add(guiPartRobot);
 		
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
 		this.setTitle("Nemo Factory v0 Part B");
 		this.setVisible(true);
 
 
 
 		// starting threads
 		vision.startThread();
 
 		partRobot.startThread();
 
 
 		// test simulation for v0
 		Map<String,Integer> kitConfig = new HashMap<String,Integer>();
 		kitConfig.put("clown", 2);
 		kitConfig.put("angler", 3);
 		kitConfig.put("puffer",3);
 		Kit sampleKit = new Kit(kitConfig, 0);
 		kitStand.insertEmptyKit(sampleKit);
 
    		partRobot.msgMakeKits();
 
    		// lane 0 becomes clown
    		// lane 1 becomes angler
    		// lane 2 becomes puffer
 
 		for(NestAgent a : nestAgents) a.startThread();
 
 		for(int i=0; i<8; i++) nestAgents.get(0).msgHereIsPart(new Part("clown"));
 		for(int i=0; i<10; i++) nestAgents.get(1).msgHereIsPart(new Part("angler"));
 		//for(int i=0; i<15; i++) nestAgents.get(2).msgHereIsPart(new Part("puffer"));
 
 
 
 
 
 	}
 
 	//*** DoXXX API ***
 	public void doPickUpParts(Map<Part, Integer> parts) {
                 
 		ArrayList<GUI_Part> partsToGet = new ArrayList<GUI_Part>();
 		ArrayList<Integer> nestIndices = new ArrayList<Integer>();
 		synchronized(guiPartList) {
 			for (GUI_Part p : guiPartList) {
 				if (parts.containsKey(p.agentPart)) {
 	                                System.out.println("$$$"+p.getName());
 					partsToGet.add(p);
 					nestIndices.add(parts.get(p.agentPart));
 				}
 
 			}
 		}
 		guiPartRobot.doTransferParts(partsToGet, nestIndices);
 	}       
         
 	//Timer callback
 	public void actionPerformed(ActionEvent ae) {
 		// This will be called by the Timer
 		// CALLS ALL updateGraphics methods of components to be displayed
                 for(GUI_Component c : compList)
                     c.updateGraphics();
 		
 		this.repaint();
 	}
 	
 	public static void main(String[] args) {
 		Factory_PartB app = new Factory_PartB();
 		new Timer(15, app).start();
 
 	}
 }
