 //Ben Mayeux
 
 package factory.managers;
 
 import java.awt.BorderLayout;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.util.ArrayList;
 
 import javax.swing.JPanel;
 
 import factory.client.Client;
 import factory.swing.KitManPanel;
 
 public class KitManager extends Client {
 	private static final long serialVersionUID = -3161852324870948654L;
 
 	public KitManager(JPanel buttons, JPanel animation) {
		super(Client.Type.KM, buttons, animation);
 		setInterface();
 	}
 	public static void main(String[] args){
 	    KitManPanel buttons = new KitManPanel();
 	    JPanel animation = new JPanel();
 		KitManager k = new KitManager(buttons, animation);
 		buttons.setManager(k);
 	}
 
 	public void sendMessage(String kitname, int setting, String message){
 		String cmd = new String("");
 		if (message == "power"){
 			cmd =  "km kma set kitcontent #kitname #itemnumber #itemname"; //hi
 			output.println(cmd);
 		}
 		
 	}
 	
 	public void setInterface() {
 		add(graphics, BorderLayout.CENTER);
 		
 		add(UI, BorderLayout.LINE_END);
 		pack();
 		setVisible(true);
 	}
 	
 	
 	public void doCommand(ArrayList<String> pCmd) {
 		// TODO Auto-generated method stub
 		
 	}
 }
