 package thesaurus.gui.canvas;
 
 import javafx.scene.canvas.GraphicsContext;
 import javafx.scene.paint.Color;
 import javafx.scene.text.Font;
 
 public class MainNode extends Node {
 	
 	/**
 	 * Constructor Method
 	 * 
 	 * Instantiates new instance of MainNode
 	 * 
 	 * @param value
 	 * @param gc
 	 * @param x
 	 * @param y
 	 */
 	protected MainNode(String value, GraphicsContext gc, int x, int y){
 		super(value,gc,x,y);
 	}
 	
 	/**
 	 * void draw()
 	 * 
 	 * Draws MainNode to screen
 	 */
 	protected void draw(){
 		getGc().setStroke(Color.BLACK);
 		getGc().setFill(Color.WHITE);
		getGc().setLineWidth(3);
 		getGc().strokeOval((getX()-50),(getY()-25), 100, 50);
 		getGc().fillOval((getX()-49),(getY()-24),98,48);
 		getGc().setFill(Color.BLACK);
 		getGc().setFont(new Font(20));
 		getGc().fillText(getValue(), (getX()-34), (getY()+5));
 	}
 }
