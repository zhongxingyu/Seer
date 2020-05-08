 import java.awt.Font;
 import java.util.ArrayList;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.TrueTypeFont;
 import org.newdawn.slick.gui.TextField;
 
 
 public class Level4 extends Level {
 	//CommandBoxes
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
 			
 			String description;
 			
 			Model model;
 			
 			TextField tf1;
 			TextField tf2;
 			TextField tf3;
 			TextField tf4;
 			TextField tf5;
 			TextField tf6;
 			TextField tf7;
 			ArrayList<TextField> tf_list;
 			
 			Font font1;
 			TrueTypeFont font2;
 	    ArrayList<CommandBox> boxes;
 	    Level prev_level;
 	    Level next_level;
 	    
 	    
 
 		  Stack stack = new Stack(600, 40, 10);
 			
 			public Level4(Model m, GameContainer gc){
 				this.model = m;
 				
 				commandbox_1 = new CommandBox(40, 200, "end resursive case");
 				commandbox_2 = new CommandBox(40, 260, "pick up all sandwiches");
				commandbox_3 = new CommandBox(210, 200, "no sandwiches");
 				commandbox_4 = new CommandBox(210, 260, "start base case");
 				commandbox_5 = new CommandBox(40, 320, "put sandwich on plate");
 				commandbox_6 = new CommandBox(40, 380, "end base case");
 				commandbox_7 = new CommandBox(40, 440, "start recursive case");
 				commandbox_8 = new CommandBox(210, 320, "pick up top sandwich");
 				commandbox_9 = new CommandBox(210, 380, "all sandwiches");
				commandbox_10 = new CommandBox(210, 440, "put sandwich in the trash");
 				commandbox_11 = new CommandBox(40, 500, "get plate");
 				
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
 				
 				font1 = new Font("Times New Roman", Font.PLAIN, 15);
 				font2 = new TrueTypeFont(font1, false);
 				
 				tf1 = new TextField(gc,font2, 100, 40, 400, 20);
 				tf2 = new TextField(gc,font2, 100, 60, 400, 20);
 				tf3 = new TextField(gc,font2, 100, 80, 400, 20);
 				tf4 = new TextField(gc,font2, 100, 100, 400, 20);
 				tf5 = new TextField(gc, font2, 100, 120, 400, 20);
 				tf6 = new TextField(gc, font2, 100, 140, 400, 20);
 				tf7 = new TextField(gc, font2, 100, 160, 400, 20);
 				tf1.setText("The sandwiches are made! They are in a stack on");
 				tf2.setText("the counter but they need to go on plates. Unfortunately,");
 				tf3.setText("you forgot how many sandwiches you made! In order to not");
 				tf4.setText("waste time counting how many sandwiches you've made, use" );
 				tf5.setText("recursion to quickly put each of the sandwiches on a plate.");
 				tf6.setText("Hint: base case is when you should stop and recursive case is");
 				tf7.setText("the action you want to be repeated.");
 				tf_list = new ArrayList<TextField>();
 				tf_list.add(tf1);
 				tf_list.add(tf2);
 				tf_list.add(tf3);
 				tf_list.add(tf4);
 				tf_list.add(tf5);
 				tf_list.add(tf6);
 				tf_list.add(tf7);
 				
 			}
 			
 			public void run(){
 				boolean end_recursive = false;
 				boolean no_sandwiches = false;
 				boolean start_base = false;
 				boolean on_plate = false;
 				boolean end_base = false;
 				boolean start_recursive = false;
 				boolean pick_up_top = false;
 				boolean get_plate = false;
 				boolean trash = false;
 				boolean wrong = false;
 				boolean base_block = false;
 				boolean recurse_block = false;
 				
 				for (int i = 0 ; i< stack.num_boxes; i++) {
 					CommandBox temp = stack.box_stack[i];
 					
 					if (temp == null ){
 						continue;
 					} else if (temp.str.equals(commandbox_1.str) && on_plate && 
 							!(base_block)){
 						end_recursive = true;
 						recurse_block = false;
 					}else if (temp.str.equals(commandbox_3.str) && start_base ){
 						no_sandwiches = true;
 					} else if (temp.str.equals(commandbox_4.str) && !(recurse_block)){
 						start_base = true;
 						base_block = true;
 					} else if (temp.str.equals(commandbox_5.str) && pick_up_top && get_plate){
 						on_plate = true;
 					} else if (temp.str.equals(commandbox_6.str) && no_sandwiches && !(recurse_block)){
 						end_base = true;
 						base_block = false;
 					} else if (temp.str.equals(commandbox_7.str) && !(base_block)) {
 						start_recursive = true;
 						recurse_block = true;
 					} else if (temp.str.equals(commandbox_8.str) && start_recursive){
 						pick_up_top = true;
 					} else if (temp.str.equals(commandbox_10.str)){
 						trash = true;
 					} else if (temp.str.equals(commandbox_11.str) && start_recursive) {
 						get_plate = true;
 					}  else {
 						wrong = true;
 					}
 					
 				}
 				
 				wrong = wrong || !(end_recursive && no_sandwiches && start_base && on_plate 
 						&& end_base && start_recursive && pick_up_top && get_plate);
 			
 				if (!(start_base && end_base && start_recursive && end_recursive)){
 					model.cur_error = "Complete the cases";
 					model.cur_prog = Model.Progress.ERROR;
 				}
 				else if (trash){
 					model.cur_error = "Oh no! You are losing sandwiches";
 				}
 				else if (wrong) {
 					model.cur_error = "Wrong!";
 					model.cur_prog = Model.Progress.ERROR;
 				} 
 				else {
 					model.cur_error = "Done!";
 					model.cur_prog = Model.Progress.SUCCESS;
 				}
 				
 			}
 
 			@Override
 			public void render() {
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
