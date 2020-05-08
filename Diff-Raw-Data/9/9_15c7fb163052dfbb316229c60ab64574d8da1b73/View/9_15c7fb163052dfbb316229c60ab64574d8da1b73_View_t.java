 import org.newdawn.slick.*;
 import org.newdawn.slick.geom.ShapeRenderer;
 import org.newdawn.slick.gui.TextField;
 
 
 import java.awt.Font;
 
 
 public class View {
 	
 	Model model;
 	Font times;
 	TrueTypeFont times2;
 	
 	public View(Model model) {
 		
 		this.model = model;
 		times = new Font("Times New Roman",Font.BOLD, 15);
 		times2 = new TrueTypeFont(times, false);
 		
 	}
 	// albhalbab
 
 	// get values from model and draw
 	public void render(GameContainer gc, Graphics g) {
 		g.setFont( times2);
 		
 		//start screen
 		if (model.cur_screen == Model.Screen.START){
 			model.start_screen.render(g);
 		}
 		//draw command boxes
 		else if (model.cur_screen == Model.Screen.LEVEL1) {
 			g.setBackground(Color.black);
 			for (int x = 0; x < model.boxes.size(); x++) {
 				
 				CommandBox temp_box = model.boxes.get(x);
 				g.setColor(temp_box.rect_color);
 				temp_box.render();
 				g.setColor(temp_box.text_color);
 				
 				g.drawString(temp_box.str, temp_box.cur_x, temp_box.cur_y);
 			}
 			g.setColor(model.button_color);
 			ShapeRenderer.fill(model.run);
 			g.setColor(Color.black);
 			g.drawString("RUN", model.run.getCenterX()-15, model.run.getCenterY() - 5);
 			model.stack.render(g);
 			
 			if (model.cur_prog == Model.Progress.ERROR){
 				g.setColor(Color.red);
 				g.drawString(model.cur_error, 100, 140);
 			} else if (model.cur_prog == Model.Progress.SUCCESS) {
 				g.setColor(Color.green);
 				g.drawString(model.cur_error, 100, 140);
			} 
 			for (int x = 0; x<model.level1.tf_list.size(); x++){
 				TextField tf = model.level1.tf_list.get(x);
 				tf.setBorderColor(Color.black);
 				tf.setTextColor(Color.white);
 				g.setColor(Color.white);
 				tf.render(gc, g);
 			}
 
 			
 		}
 		
 		// draw stack
 		
 		
 	}
 
 }
