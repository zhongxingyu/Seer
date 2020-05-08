 package gui;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import javax.swing.ImageIcon;
 
 import agents.ConveyorAgent;
 import agents.Kit;
 
 public class GUI_Conveyor extends GUI_Component {
 	ImageIcon ConImage;//image of conveyor
 	int x,y, state, numberOfKitsOnStands;//coordinates
 	List<GUI_Kit> kits;
 	GUI_Kit shownkit;
 	ConveyorAgent conagent;
 	
 	//Constructor
 	public GUI_Conveyor(){
 		kits = Collections.synchronizedList (new ArrayList<GUI_Kit> () );
 		ConImage = new ImageIcon("gfx/Conveyor.png");
 		x =0;
 		y=0;
 		state = 0;
 		numberOfKitsOnStands = 0; 
 		
 		myDrawing = new Drawing(x, y, "Conveyor.png");
 
 	}
 
 	public void DoAddKitToConveyor(ConveyorAgent con){
 
 		System.out.println("#ofKits: " + kits.size());
 		System.out.println("#ofKitsOnStand: " + numberOfKitsOnStands);
 		shownkit = kits.get(numberOfKitsOnStands);
 		state = 1;
 		conagent = con;
 	}
 	
 	synchronized public void addKit(Kit k) {
 		GUI_Kit addkit = new GUI_Kit(k,130 ,0);
 		kits.add(addkit);
 		//System.out.println("guiConveyor added kit. kits.size(): " + kits.size());
 	}
 	
 	
 	public GUI_Kit robotRemoveKit(){
 		state = 3;
 		GUI_Kit passkit = shownkit;
 		shownkit= null;
 		numberOfKitsOnStands++;
 		return passkit;
 	}
 	
 	public GUI_Kit checkKit(){
 		return shownkit;
 	}
 	
 	public void DoGetKitOut(Kit k){
 		shownkit = new GUI_Kit(k, 70,30);
 		state = 2;
 		numberOfKitsOnStands --;
 	}
 	
 	
 	
 	//the move function will have the requirements for actually moving the conveyor belt and the pallets
 	public void state1Move(){
 		if(shownkit!=null){//if there are kits to move, move them along the conveyor
 			if (shownkit.posY<= 30){
 				shownkit.posY += 2;
 			}
 			else{
 				if(shownkit.posX>70){
 				shownkit.posX -=2;
 				}
 				/*else if (numberOfKitsOnStands <2 & kits.size()>1){
 					DoAddKitToConveyor(conagent);
 					//System.out.println("guiConveyor calling DoAddKitToConveyor");
 				}*/
 				else
 				{					
 					state = 0;
 					conagent.lockRelease();
 				}
 			}
 		}
 	}
 	
 	public void state2Move(){
 		if(shownkit!=null){//if there are kits to move, move them along the conveyor
 			if(shownkit.posX>-10){
 				shownkit.posX -=2;
 				}
 				else
 				{
 					state = 0;
 					kits.remove(0);
 					shownkit = null;
 				}
 		}
 	}
 	
 	public void paintComponent(){
 		myDrawing.posX = x;
 		myDrawing.posY =y;
 		myDrawing.subDrawings.clear();
 		
		for(GUI_Kit k: kits){
 			k.paintComponent();
 			myDrawing.subDrawings.add(k.myDrawing);
 		}
 	}
 	
     public void updateGraphics(){
     	if(state==1){
     		state1Move();
     	}
     	else if (state ==2){
     		state2Move();
     	}
     	else if (state ==3){
     		
     	}    	
     	
     }
 	
     public int getX(){
     	return shownkit.posX;
     }
     
     public int getY(){
     	return shownkit.posY;
     }
 	
     public List<GUI_Kit> getKits(){
     	return kits;
     }
 	
 }
