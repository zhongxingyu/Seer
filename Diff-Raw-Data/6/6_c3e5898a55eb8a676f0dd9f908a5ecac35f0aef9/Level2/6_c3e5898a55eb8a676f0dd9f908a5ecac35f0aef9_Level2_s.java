 import java.awt.Font;
 import java.util.ArrayList;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.TrueTypeFont;
 import org.newdawn.slick.gui.TextField;
 
 
 public class Level2 extends Level {
 
 
 	
 	CommandBox commandbox_1;
 	CommandBox commandbox_2;
 	CommandBox commandbox_3;
 	CommandBox commandbox_4;
 	CommandBox commandbox_5;
 	CommandBox commandbox_6;
 	CommandBox commandbox_7;
 	CommandBox commandbox_8;
 	CommandBox commandbox_9;
 	CommandBox commandbox_10;
 	CommandBox commandbox_11;
 	CommandBox commandbox_12;
 	CommandBox commandbox_13;
 	CommandBox commandbox_14;
 	CommandBox commandbox_15;
 	CommandBox commandbox_16;
 	CommandBox commandbox_17;
 	
 	Model model;
 	
 	TextField tf1;
 	TextField tf2;
 	TextField tf3;
 	TextField tf4;
 	TextField tf5;
 	ArrayList<TextField> tf_list;
 	
 	Font font1;
 	TrueTypeFont font2;
 	
 	ArrayList<CommandBox> boxes;
 	
 	Level prev_level;
 	Level next_level;
 	
 	Stack stack = new Stack(600, 40, 24);
 	
 	public Level2(Model m, GameContainer gc) {
 		this.model = m;
 		
 		commandbox_1 = new CommandBox(40, 180, "IF at fridge");
 		commandbox_2 = new CommandBox(40, 240, "IF at drawer");
 		commandbox_3 = new CommandBox(210, 180, "IF at cabinet");
 		commandbox_4 = new CommandBox(210, 240, "open fridge");
 		commandbox_5 = new CommandBox(40, 300, "open cabinet");
 		commandbox_6 = new CommandBox(40, 360, "open drawer");
 		commandbox_7 = new CommandBox(40, 420, "close fridge");
 		commandbox_8 = new CommandBox(210, 300, "close drawer");
 		commandbox_9 = new CommandBox(210, 360, "close cabinet");
 		commandbox_10 = new CommandBox(210, 420, "get bread");
 		commandbox_11 = new CommandBox(40, 480, "get knife");
 		commandbox_12 = new CommandBox(210, 480, "get spoon");
 		commandbox_13 = new CommandBox(40, 540, "get fork");
 		commandbox_14 = new CommandBox(210, 540, "get bowl");
 		commandbox_15 = new CommandBox(40, 600, "get peanut butter");
 		commandbox_16 = new CommandBox(210, 600, "get jelly");
 		commandbox_17 = new CommandBox(40, 660, "get plate");
 
 		
 		boxes = new ArrayList<CommandBox>();
 		boxes.add( commandbox_1);
 		boxes.add( commandbox_2);
 		boxes.add( commandbox_3);
 		boxes.add(commandbox_4);
 		boxes.add( commandbox_5);
 		boxes.add(commandbox_6);
 		boxes.add( commandbox_7);
 		boxes.add( commandbox_8);
 		boxes.add(commandbox_9);
 		boxes.add(commandbox_10);
 		boxes.add( commandbox_11);
 		boxes.add(commandbox_12);
 		boxes.add(commandbox_13);
 		boxes.add(commandbox_14);
 		boxes.add(commandbox_15);
 		boxes.add(commandbox_16);
 		boxes.add(commandbox_17);
 		
 		tf_list = new ArrayList<TextField>();
 		
 		font1 = new Font("Times New Roman", Font.PLAIN, 15);
 		font2 = new TrueTypeFont(font1, false);
 		
 		tf1 = new TextField(gc,font2, 100, 40, 400, 20);
 		tf2 = new TextField(gc,font2, 100, 60, 400, 20);
 		tf3 = new TextField(gc,font2, 100, 80, 400, 20);
 		tf4 = new TextField(gc,font2, 100, 100, 400, 20);
 		tf5 = new TextField(gc, font2, 100, 120, 400, 20);
 		tf1.setText("Now that the kitchen is cleaned, it's time for the ");
 		tf2.setText("robot to gather all of the materials it needs. The");
 		tf3.setText("robot is moving around the kitchen and will stop at ");
 		tf4.setText("different locations in the kitchen. Make sure he gathers" );
 		tf5.setText("everything it needs to make sandwiches!");
 		tf_list = new ArrayList<TextField>();
 		tf_list.add(tf1);
 		tf_list.add(tf2);
 		tf_list.add(tf3);
 		tf_list.add(tf4);
 		tf_list.add(tf5);
 		
 	}
 	
 	public void run() {
 		boolean at_fridge = false;
 		boolean at_drawer = false;
 		boolean at_cabinet = false;
 		boolean open_fridge = false;
 		boolean open_drawer = false;
 		boolean open_cabinet = false;
 		boolean close_fridge = false;
 		boolean close_drawer = false;
 		boolean close_cabinet = false;
 		boolean bread = false;
 		boolean knife = false;
 		boolean pb = false;
 		boolean j = false;
 		boolean plate = false;
 		boolean too_much = false;
 		boolean fBlock = false;
 		boolean dBlock = false;
 		boolean cBlock = false;
 		boolean food_place = true;
 		boolean wrong = false;
 		
 		for(int i = 0; i<stack.num_boxes; i++) { 
 			CommandBox temp = stack.box_stack[i];
 			
 			if (temp == null) {
 				continue;
 			}
 			if (temp.str.equals(commandbox_1.str) && !dBlock && !cBlock) {
 				 at_fridge = true;
 				 fBlock = true;
 			}
 			else if (temp.str.equals(commandbox_2.str) && !fBlock && !cBlock) {
 				at_drawer = true;
 				dBlock = true;
 			}
 			else if (temp.str.equals(commandbox_3.str) && !fBlock && !dBlock) {
 				at_cabinet = true;
 				cBlock = true;
 			}
 			else if (temp.str.equals(commandbox_4.str) && at_fridge) {
 				open_fridge = true;
 			}
 			else if (temp.str.equals(commandbox_5.str) && at_cabinet) {
 				open_cabinet = true;
 			}
 			else if (temp.str.equals(commandbox_6.str) && at_drawer) {
 				open_drawer = true;
 			}
 			else if (temp.str.equals(commandbox_7.str) && open_fridge) {
 				close_fridge = true;
 				fBlock = false;
 			}
 			else if (temp.str.equals(commandbox_8.str) && open_drawer) {
 				close_drawer = true;
 				dBlock = false;
 			}
 			else if (temp.str.equals(commandbox_9.str) && open_cabinet) {
 				close_cabinet = true;
 				cBlock = false;
 			}
 			else if (temp.str.equals(commandbox_10.str)) {
 				bread = true;
 				
 				if (!fBlock){
 					food_place = false;
 				}
 			}
 			else if (temp.str.equals(commandbox_11.str) && open_drawer) {
 				knife = true;
 			}
 			else if (temp.str.equals(commandbox_12.str) ||
 					temp.str.equals(commandbox_13.str) ||
 					temp.str.equals(commandbox_14.str)) {
 				too_much = true;
 			}
 			else if (temp.str.equals(commandbox_15.str)){
 				pb = true;
 				
 				if (!fBlock){
 					food_place = false;
 				}
 			}
 			else if (temp.str.equals(commandbox_16.str)){
 				j = true;
 				
 				if (!fBlock){
 					food_place = false;
 				}
 			}
 			else if (temp.str.equals(commandbox_17.str) && open_cabinet){
 				plate = true;
 			}
 			else{
 				wrong = true;
 			}
 		}
 		
 		
 		if (!(at_fridge && at_drawer && at_cabinet)){
			model.cur_error = "You're passing a crucial location!";
			model.cur_prog = Model.Progress.ERROR;
		}
		else if (fBlock || cBlock || dBlock){
			model.cur_error = "You can't be at 2 places at once!";
 			model.cur_prog = Model.Progress.ERROR;
 		}
 		else if (!(close_fridge && close_cabinet && close_drawer)){
 			model.cur_error = "You left something open!";
 			model.cur_prog = Model.Progress.ERROR;
 		}
 		else if (!(bread && knife && pb && j && plate)){
 			model.cur_error = "You forgot to get something!";
 			model.cur_prog = Model.Progress.ERROR;
 		}
 		else if (!food_place){
 			model.cur_error = "There's no food in the cabinet or drawer!";
 			model.cur_prog = Model.Progress.ERROR;
 		}
 		else if (too_much) {
 			model.cur_error = "You're carrying too many things!";
 			model.cur_prog = Model.Progress.ERROR;
 		} 
 		else if (wrong) {
 			model.cur_error = "Wrong";
 			model.cur_prog = Model.Progress.ERROR;
 		}
 		else {
 			model.cur_error = "Done!";
 			model.cur_prog = Model.Progress.SUCCESS;
 		}
 		
 
 				
 				
 		
 		
 	}
 
 	@Override
 	void render() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	ArrayList<CommandBox> getBoxes() {
 		// TODO Auto-generated method stub
 		return boxes;
 	}
 
 	@Override
 	Stack getStack() {
 		// TODO Auto-generated method stub
 		return stack;
 	}
 
 	@Override
 	ArrayList<TextField> getTF() {
 		// TODO Auto-generated method stub
 		return tf_list;
 	}
 
 	@Override
 	void setPrevLevel(Level level) {
 		// TODO Auto-generated method stub
 		prev_level = level;
 	}
 
 	@Override
 	Level getPrevLevel() {
 		// TODO Auto-generated method stub
 		return prev_level;
 	}
 
 	@Override
 	void setNextLevel(Level level) {
 		// TODO Auto-generated method stub
 		next_level = level;
 	}
 
 	@Override
 	Level getNextLevel() {
 		// TODO Auto-generated method stub
 		return next_level;
 	}
 	
 
 
 }
