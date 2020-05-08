 package gui;
 
 import java.awt.Component;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.util.ArrayList;
 
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 import agents.Kit;
 
 public class GUI_Conveyor implements GUI_Component {
 	ImageIcon ConImage;//image of conveyor
	double x,y;//coordinates
 	ArrayList<GUI_Kit> kits;
 	
 	//Constructor
 	GUI_Conveyor(){
 		kits = new ArrayList<GUI_Kit> ();
 		ConImage = new ImageIcon("gfx/Conveyor.png");
 	}
 
 	void DoAddKit(Kit k) {
 		
 		kits.add(new GUI_Kit(k));
 
 	}
 	//the addGUIKit function will add a GUIKit to the stand
	void addGUIKit(GUI_Kit k){
		
	}
 	
 	void DoRemoveKit(Kit k) {
 		for(GUI_Kit gk : kits)
 		{
 			if(gk.kit == k)
 			{
				removeKit(gk);
 			}
 		}
 	}
	//the removeKit function will set the GUIKit of the stand to null
	void removeKit(GUI_Kit k){
		if(kits.remove(k)){
			System.out.print("GUI_Kit removed successfully");
		}
		else{
			System.out.print("GUI_Kit could not be removed!!!");
			System.exit(0);
		}
	}
 	//the move function will have the requirements for actually moving the conveyor belt and the pallets
 	void moveKits(){
 		if(kits.size()!=0){//if there are kits to move, move them along the conveyor
 			for(GUI_Kit gk: kits){
 				//GUI_Kit.x
 			}
 		}
 	}
 	
 	public void paintComponent(JPanel j, Graphics2D g){
		ConImage.paintIcon((Component) j,(Graphics) g, (int) x, (int) y);
 		
 	}
 	public void paintComponent(Component j, Graphics2D g){
		ConImage.paintIcon((Component) j,(Graphics) g, (int) x, (int) y);
 		
 	}
     public void updateGraphics(){
     	
     }
 	
     public static void main(String[] args) {		
 		final GUI_Conveyor Con = new GUI_Conveyor();
 				
 		JFrame f = new JFrame() {
 			public void paint(Graphics g) {
 				Con.paintComponent((Component)this, (Graphics2D) g);
 			}
 		};
 		
 		f.pack();
 		f.setVisible(true);
 	}
 	
 	
 }
