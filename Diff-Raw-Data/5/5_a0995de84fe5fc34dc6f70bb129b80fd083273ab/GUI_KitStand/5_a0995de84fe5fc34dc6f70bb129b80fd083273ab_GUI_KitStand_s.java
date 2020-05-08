 package gui;
 
 import java.util.List;
 
 import agents.Kit;
 import agents.KitRobotAgent;
 import agents.Part;
 
 public class GUI_KitStand extends GUI_Component {
 
 	public GUI_InspectionCamera camera;
 	
 	GUI_Stand[] stands;
 	KitRobotAgent kitRobot;
 
 	// This is the constructor. It creates the array( size 3) of stands and sets
 	// each stands. Then it sets stands[0] and stand[1] to normals stand objs
 	// and stands[2] to inspection area obj.
 	public GUI_KitStand(int x, int y) {
 		stands = new GUI_Stand[3];
 		camera = new GUI_InspectionCamera(x, y);
 
 		for (int i = 0; i < 3; i++)
 			stands[i] = new GUI_Stand(x, i*150 + y);
 		
 		myDrawing = new Drawing(x, y, "NOPIC");
 	}
 
 	public void DoAddKit(Kit k, KitRobotAgent r) {
 		kitRobot = r;
 		for (int s = 2; s>=0; s--){
 			if (stands[s].kit == null){
 				stands[s].kit = new GUI_Kit(k,getX(s)+25,getY(s)+65);
 				stands[s].temp = stands[s].kit;
 				break;
 			}
 		}
 		kitRobot.kitOnStandRelease();
 	}
 	
 	public void DoRemoveKit(Kit k) {
 		for (int s = 0; s<=2; s++){
 			if (stands[s].kit.kit==k)
 			{
 				stands[s].removeKit();
 				break;
 			}
 		}
 	}
 
 	public void paintComponent() {
 		myDrawing.subDrawings.clear();
 		
 		for(GUI_Stand s : stands) {
 			s.paintComponent();
 			myDrawing.subDrawings.add(s.myDrawing);
 		}
 		
 		camera.paintComponent();
 		myDrawing.subDrawings.add(camera.myDrawing);
 	}
 	public void updateGraphics() {
 		for (GUI_Stand s : stands)
 			s.updateGraphics();
 		
 		camera.updateGraphics();
 	}
 
 	public int getX(int i) {
 		return stands[i].x;
 	}
 
 	public int getY(int i) {
 		return stands[i].y;
 	}
 
 	public void addPart(int number, GUI_Part p) {
 		
 		stands[number].kit.addPart(p);
 	}
 
 	public boolean positionOpen(int i) {
 		if (stands[i].kit == null){
 			return true;
 		}
 		else{
 			return false;
 		}
 	}
 	
 	public void addkit(GUI_Kit k, int number){
 		stands[number].kit = k;
 		stands[number].kit.setX(stands[number].x);
 		stands[number].kit.setY(stands[number].y);
 		stands[number].temp = stands[number].kit;
 	}
 	
 	public GUI_Kit checkKit(int number){
 		return stands[number].temp;
 	}
 	
 	public GUI_Kit guiRemoveKit(int number){
 		GUI_Kit passKit = stands[number].temp;
 		stands[number].temp = null;
 		stands[number].kit = null;
 		return passKit;
 	}
 	
 	public Kit getTempKit(){
 		int i = 2;
 		for(int j = 0; j<2; j++){
 			if(stands[j].kit!= null){
 				i = j;
 				break;
 			}
 		}
 		return stands[i].kit.kit;
 	}
 	
 	public void DoBreakInspectionKit(List<Part> p)
 	{
		stands[2].kit.parts.clear();
 		for(Part part: p)
 		{
 			GUI_Part newpart = new GUI_Part(part);
			stands[2].kit.parts.add(newpart);
 		}
 	}
 }
