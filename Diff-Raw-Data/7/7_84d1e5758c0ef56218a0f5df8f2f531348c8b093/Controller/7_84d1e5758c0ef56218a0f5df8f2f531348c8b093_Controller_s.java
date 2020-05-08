 import org.newdawn.slick.*;
 
 
 public class Controller{
 	Model model;
 	Input input;
 	CommandBox cur_command_box;
 	
 	
 	public Controller(Model model) {
 		
 		this.model = model;
 		
 		
 	}
 	
 	// get input from user and update model
 	public void update(Input input) {
 		this.input = input;
 		
 		
 	}
 
 	public void mouseClicked(int arg0, int arg1, int arg2, int arg3) {
 		// TODO Auto-generated method stub
 		
 		// check for run
 		System.out.println("clicked");
 		
 		
 	}
 
 
 	public void mouseDragged(int arg0, int arg1, int new_x, int new_y) {
 		// TODO Auto-generated method stub
 		
 		if (cur_command_box != null){
			model.modifyCBPos(cur_command_box, new_x, new_y );
 		}
 		// modify command box
 		
 	}
 
 	public void mouseMoved(int arg0, int arg1, int arg2, int arg3) {
 		// TODO Auto-generated method stub
 		
 		
 		
 	}
 
 
 	public void mousePressed(int arg0, int x, int y) {
 		// TODO Auto-generated method stub
 		
 		// get command box (put into current command box)
 		System.out.println("pressed");
 		cur_command_box = getCurBox(x, y);
 		if (cur_command_box != null) {
			model.changeCBColor(Color.blue, cur_command_box);
 		}
 
 		// modify command box 
 		
 	}
 
 	
 	public void mouseReleased(int arg0, int x, int y) {
 		// TODO Auto-generated method stub
 		
 		// check to modify stack
 		if (cur_command_box != null) {
 			model.modifyCBPos(cur_command_box, cur_command_box.x, cur_command_box.y);
 		}
 		
 	}
 
 	public CommandBox getCurBox(int x, int y){
 		
 		if (model.cur_screen == Model.Screen.LEVEL1) {
 			
 			for (int i = 0; i< model.level_one_boxes.size(); i++){
 				CommandBox temp = model.level_one_boxes.get(i);
 				if (x > temp.cur_x && x < temp.cur_x+CommandBox.width &&
 					y > temp.cur_y && y < temp.cur_y + CommandBox.height) {
 					
 					return temp;
 				}
 				
 			}
 			return null;
 			
 		}
 		return null;
 	}
 
 
 
 	                 
 }
