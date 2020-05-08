 //Ben Mayeux and Stephanie Reagle
 //CS 200
 
 package factory.managers;
 
 import java.awt.BorderLayout;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import factory.graphics.*;
 import factory.*;
 import factory.swing.LaneManPanel;
 import factory.client.Client;
 
 public class LaneManager extends Client {
 
 	private static final long serialVersionUID = 6767006307991802656L;
 	
 	LaneManPanel buttons;
 	LanePanel animation;
 	ArrayList<Integer> laneSpeeds; // stores speeds of each lane
 	ArrayList<Integer> laneAmplitudes; // stores amplitudes of each lane
 	
 	public LaneManager() {
 		super(Client.Type.lm, null, null); 
 		
 		buttons = new LaneManPanel(this);
 		animation = new LanePanel(this);
 		
 		laneSpeeds = new ArrayList<Integer>();
 		laneAmplitudes = new ArrayList<Integer>(); 
 		for (int i = 0; i < 8; i++){    // presets lane speeds and amplitudes
			laneSpeeds.set(i, 2);  
			laneAmplitudes.set(i,2);
 		}
 		
 		setInterface();
 	}
 	
 	public static void main(String[] args){
 		LaneManager l = new LaneManager(); 
 	}
 	
 	
 	public void setInterface() {
 		graphics = animation;
 		UI = buttons;
 
 		add(graphics, BorderLayout.CENTER);
 		
 		add(UI, BorderLayout.LINE_END);  
 		
 		pack();
 		setVisible(true);
 	}
 
 	public void doCommand(ArrayList<String> pCmd) {
 		int size = pCmd.size();
 		//parameters lay between i = 2 and i = size - 2
 		System.out.println(Arrays.toString(pCmd.toArray()));
 		String action = pCmd.get(0);
 		String identifier = pCmd.get(1);
 		if(action.equals("cmd")){
 			
 			// Commands from FeederAgent
 			if (identifier.equals("startfeeding"))
 			{
 				int feederSlot = Integer.valueOf(pCmd.get(2));
 				((LanePanel) graphics).feedFeeder(feederSlot);
 			}
 			else if (identifier.equals("stopfeeding"))
 			{
 				int feederSlot = Integer.valueOf(pCmd.get(2));
 				((LanePanel) graphics).turnFeederOff(feederSlot);
 			} 
 			else if (identifier.equals("purgefeeder"))
 			{
 				int feederSlot = Integer.valueOf(pCmd.get(2));
 				((LanePanel) graphics).purgeFeeder(feederSlot);
 			}
 			else if (identifier.equals("switchlane"))
 			{
 				int feederSlot = Integer.valueOf(pCmd.get(2));
 				((LanePanel) graphics).switchFeederLane(feederSlot);
 			}
 			else if (identifier.equals("purgetoplane"))
 			{
 				int feederSlot = Integer.valueOf(pCmd.get(2));
 				((LanePanel) graphics).purgeTopLane(feederSlot);
 			}
 			else if (identifier.equals("purgebottomlane"))
 			{
 				int feederSlot = Integer.valueOf(pCmd.get(2));
 				((LanePanel) graphics).purgeBottomLane(feederSlot);
 			}
 			
 			//Commands from VisionAgent
 			else if (identifier.equals("takepictureofnest")) {
 				int nestIndex = Integer.valueOf(pCmd.get(2));
 				((LanePanel) graphics).cameraFlash(nestIndex);
 			}
 
 		}
 		else if(action.equals("req")){
 
 		}
 		else if(action.equals("get")){
 
 		}
 		else if(action.equals("set")){
 			if (identifier.equals("loadpartatfeeder")) {
 				int feederSlot = Integer.valueOf(pCmd.get(2));
 				GraphicBin bin = new GraphicBin(new Part(pCmd.get(3)));
 				((LanePanel) graphics).setFeederBin(feederSlot, bin);
 			}
 			else if (identifier.equals("nestitemtaken")) {
 				int nestIndex = Integer.valueOf(pCmd.get(2));
 				int itemIndex = Integer.valueOf(pCmd.get(3));
 				((LanePanel) graphics).popNestItem(nestIndex, itemIndex);
 			}
 
 		}
 		else if(action.equals("cnf")){
 
 		}
 		else if(action.equals("mcs")){
 			   try {
 					this.server.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 				   System.exit(0);
 			   }
 		else 
 	   		  System.out.println("Stuff is FU with the server...\n(string does not contain a command type)");
 	}
 	
 	public void setLaneSpeed(int laneNumber, int speed){
 		laneSpeeds.set(laneNumber, speed);
 	}
 	
 	public void setLaneAmplitude(int laneNumber, int amplitude){
 		laneAmplitudes.set(laneNumber, amplitude);
 	}
 	
 	public int getLaneSpeed(int laneNumber){
 		return laneSpeeds.get(laneNumber-1);
 	}
 	
 	public int getLaneAmplitude(int laneNumber){
 		return laneAmplitudes.get(laneNumber-1);
 	}
 }
