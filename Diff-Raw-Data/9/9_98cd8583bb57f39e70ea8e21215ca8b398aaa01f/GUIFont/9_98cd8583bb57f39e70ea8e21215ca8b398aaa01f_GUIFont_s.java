 package cx.it.hyperbadger.spacezombies.gui;
 
 import java.awt.Font;
 import java.awt.geom.Point2D;
 
 import org.lwjgl.opengl.Display;
 import org.newdawn.slick.UnicodeFont;
 
 public class GUIFont extends UnicodeFont{
 	public GUIFont(Font font) {
 		super(font);
 	}
 	public void drawString(Point2D topLeft, Point2D bottomRight, String text){
 		int dW = Display.getWidth();
 		int dH = Display.getHeight();
 		int left = (int) ((topLeft.getX()*dW)/100);
 		int top = (int) ((topLeft.getY()*dH)/100);
 		//int bottom = (int) ((bottomRight.getY()*dH)/100);
 		int right = (int) ((bottomRight.getX()*dW)/100);
 		String out = text;
 		while(this.getWidth(out)>(right-left)){
 			out = out.substring(0, out.length()-1);
 		}
		this.drawString(left, top, text);
 	}
 
 }
