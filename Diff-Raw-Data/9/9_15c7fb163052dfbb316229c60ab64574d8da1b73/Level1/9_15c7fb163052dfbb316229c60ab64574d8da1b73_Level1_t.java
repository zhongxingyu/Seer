 import java.util.ArrayList;
 import org.newdawn.slick.gui.TextField;
 import org.newdawn.slick.*;
 import java.awt.Font;
 import org.newdawn.slick.font.*;
 
 public class Level1{
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
 		ArrayList<TextField> tf_list;
 		
 		Font font1;
 		TrueTypeFont font2;
 		
 		public Level1(Model m, GameContainer gc){
 			this.model = m;
 			
 			commandbox_1 = new CommandBox(40, 200, "clean counter");
 			commandbox_2 = new CommandBox(40, 260, "clean red plate");
 			commandbox_3 = new CommandBox(180, 200, "clean black plate");
 			commandbox_4 = new CommandBox(180, 260, "clean fork");
 			commandbox_5 = new CommandBox(40, 320, "clean knife");
 			commandbox_6 = new CommandBox(40, 380, "clean refridgerator");
 			commandbox_7 = new CommandBox(40, 440, "walk to living room");
 			commandbox_8 = new CommandBox(180, 320, "turn on TV");
 			commandbox_9 = new CommandBox(180, 380, "plug sink");
 			commandbox_10 = new CommandBox(180, 440, "run hot water");
 			commandbox_11 = new CommandBox(40, 500, "add soap to water");
 			
 			
 			model.boxes.add(0, commandbox_1);
 			model.boxes.add(1, commandbox_2);
 			model.boxes.add(2, commandbox_3);
 			model.boxes.add(3,commandbox_4);
 			model.boxes.add(4, commandbox_5);
 			model.boxes.add(5,commandbox_6);
 			model.boxes.add(6, commandbox_7);
 			model.boxes.add(7, commandbox_8);
 			model.boxes.add(8, commandbox_9);
 			model.boxes.add(9, commandbox_10);
 			model.boxes.add(10, commandbox_11);
 			model.boxes.add(11, commandbox_11);
 			String eol = System.getProperty("line.separator");
 			description = "Welcome to Kitchen Disaster!\n"+ eol+" You are tasked with giving instructions to a robot to" +eol+
 					" make peanut butter sandwiches for your family.  But alas, your children have left the " +
 					"kitchen a complete mess!  Clean it up!";
 			
 			font1 = new Font("Times New Roman", Font.PLAIN, 15);
 			font2 = new TrueTypeFont(font1, false);
 			
 			tf1 = new TextField(gc,font2, 100, 40, 400, 20);
 			tf2 = new TextField(gc,font2, 100, 60, 400, 20);
 			tf3 = new TextField(gc,font2, 100, 80, 400, 20);
 			tf4 = new TextField(gc,font2, 100, 100, 400, 20);
 			tf5 = new TextField(gc, font2, 100, 120, 400, 20);
 			tf1.setText("Welcome to Kitchen Disaster!");
 			tf2.setText("You are tasked with giving instructions to a robot to");
			tf3.setText("make peanut butter sandwiches for your family.");
 			tf4.setText("But alas, your children have left the kitchen a complete mess! " );
 			tf5.setText("Drag commands to the stack!");
 			tf1.setConsumeEvents(false);
 			tf2.setConsumeEvents(false);
 			tf3.setConsumeEvents(false);
 			tf4.setConsumeEvents(false);
 			tf5.setConsumeEvents(false);
 			tf_list = new ArrayList<TextField>();
 			tf_list.add(tf1);
 			tf_list.add(tf2);
 			tf_list.add(tf3);
 			tf_list.add(tf4);
 			tf_list.add(tf5);
 			
 		}
 		
 		public void run(){
 			boolean containsCommandBox1 = false;
 			boolean containsCommandBox2 = false;
 			boolean contains3 = false;
 			boolean contains4 = false;
 			boolean contains5 = false;
 			boolean contains_plug = false;
 			boolean ran_water = false;
 			boolean added_soap = false;
 			boolean wrong = false;
 			for (int i = 0 ; i< model.stack.num_boxes; i++) {
 				CommandBox temp = model.stack.box_stack[i];
 				if (temp == null ){
 					continue;
 				}
 				if (temp.str.equals(commandbox_9.str)){
 					contains_plug = true;
 				}
 				if (temp.str.equals(commandbox_10.str) && contains_plug) {
 					ran_water = true;
 				}
 				if (temp.str.equals(commandbox_11.str) && ran_water) {
 					added_soap = true;
 				}
 				if (temp.str.equals(commandbox_1.str)){
 					containsCommandBox1 = true;
 				} else if (temp.str.equals(commandbox_2.str) && added_soap){
 					containsCommandBox2 = true;
 				} else if (temp.str.equals(commandbox_3.str) && added_soap){
 					contains3 = true;
 				} else if (temp.str.equals(commandbox_4.str) && added_soap){
 					contains4 = true;
 				} else if (temp.str.equals(commandbox_5.str) && added_soap){
 					contains5 = true;
 				} else if (temp.str.equals(commandbox_6.str) || 
 						   temp.str.equals(commandbox_7.str) ||
 						   temp.str.equals(commandbox_8.str) ) {
 					wrong = true;
 				}
 				
 			}
 			if (! added_soap) {
 				model.cur_error = model.error_2;
 				model.cur_prog = Model.Progress.ERROR;
 			} else if ( wrong) {
 				model.cur_error = model.error_3;
 				model.cur_prog = Model.Progress.ERROR;
 			}
 			else if (! (containsCommandBox1 && containsCommandBox2 &&
 					contains3 && contains4 && contains5)){
 				model.cur_error = model.error_1;
 				model.cur_prog = Model.Progress.ERROR;
 			} else {
 				model.cur_error = "Done!";
 				model.cur_prog = Model.Progress.SUCCESS;
 			}
 			
 		}
 
 }
