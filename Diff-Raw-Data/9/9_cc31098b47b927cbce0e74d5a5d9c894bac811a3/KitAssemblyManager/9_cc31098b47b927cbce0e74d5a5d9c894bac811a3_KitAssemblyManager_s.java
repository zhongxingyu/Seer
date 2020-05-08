 //Ben Mayeux, Stephanie Reagle, Marc Mendiola
 //CS 200
 
 package factory.managers;
 
 import java.awt.*;
 import java.util.ArrayList;
 
 import javax.swing.BoxLayout;
 
 import factory.client.Client;
 import factory.graphics.KitAssemblyPanel;
 import factory.swing.KitAssManPanel;
 
 public class KitAssemblyManager extends Client {
 	private static final long serialVersionUID = -4230607892468748490L;
 
 		public KitAssemblyManager() {
			super(Client.Type.KAM, null, null);
 			graphics = new KitAssemblyPanel(this);
 			UI = new KitAssManPanel();
 			((KitAssManPanel)UI).setManager(this);
 			this.setInterface();
 		}
 		public static void main(String[] args){
 			KitAssemblyManager k = new KitAssemblyManager();
 		}
 
 		public void setInterface() {
 			setSize(1780, 720);
 			setLayout(new BorderLayout());
 			
 
 			add(graphics, BorderLayout.CENTER);
 			
 			//add(UI, BorderLayout.LINE_END); //to be implemented in V.2
 			setVisible(true);
 
 		}
 		
 		public void sendMessage (String kitname, String quantity, String message) { // sends message out from swing
 			String set = new String("");
 			if (message == "incorrectKits"){
 				//TODO get the format of the command from server //for V.2
 			}
 			else if (message == "kitRobotFreeze"){
 				//TODO get the format of the command from server //for V.2
 			}
 			else{
 				set = "bad command";
 			}
 			sendCommand(set);
 		}
 
 		public void doCommand(ArrayList<String> pCmd) {
 			// TODO receive commands
 			
 		}
 }
