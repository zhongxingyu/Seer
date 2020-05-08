 /**
  * @author Team O
  * @title Graphical Thesaurus
  * @date 3/12/12
  */
 
 package thesaurus.gui.canvas;
 
 import java.awt.geom.Point2D;
 import javafx.scene.canvas.GraphicsContext;
 import javafx.scene.paint.Color;
 import javafx.scene.text.Font;
 
 
 class SynonymNode extends SubNode {
 
 	/**
 	 * Constructor Method
 	 * 
 	 * Instantiates new instance of SynonymNode
 	 * 
 	 * @param value
 	 * @param gc
 	 * @param x
 	 * @param y
 	 */
 	protected SynonymNode(String value, GraphicsContext gc, int x, int y) {
 		super(value, gc, x, y);
 	}
 	
 	protected void drawConnector(Point2D point2d){
 		getGc().setStroke(Color.GREEN);
 		getGc().strokeLine(point2d.getX(), point2d.getY(), getX(), getY());
 	}
 
 	/**
 	 * void drawConnector()
 	 * 
 	 * Draws a connecting line between synonym and a MainNode taken as parameter.
 	 */
 	protected void drawConnector(MainNode main) {
 			getGc().setStroke(Color.GREEN);
 			getGc().strokeLine(main.getX(), main.getY(), getX(), getY());
 	}
 
 	/**
 	 * void draw()
 	 * 
 	 * Draws Synonym on screen
 	 */
 	protected void draw() {
 		getGc().setStroke(Color.GREEN);
 		getGc().setFill(Color.WHITE);
 		getGc().strokeOval((getX()-37),(getY()-13), 74, 36);
 		getGc().fillOval((getX()-36),(getY()-12),72,34);	//Draw white oval overlapping to hide connector
 		getGc().setFill(Color.BLACK);
 		getGc().setFont(new Font(14));
 		getGc().fillText(getValue(), (getX()-25), (getY()+10));
 	}
 
 }
