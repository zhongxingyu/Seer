 package client.drawing.strategy;
 
 import client.drawing.NwbDrawingInfo;
 
 import java.awt.*;
 import java.io.Serializable;
 
 /**
  * Created with IntelliJ IDEA.
  * User: hanmoi
  * Date: 15/09/12
  * Time: 6:04 PM
  * To change this template use File | Settings | File Templates.
  */
 public abstract class NwbDrawingStrategy implements Serializable {
 
     /**
 	 * 
 	 */
 	private static final long serialVersionUID = -7278829595202134257L;
     protected NwbDrawingInfo drawingInfo;
     protected Color currentFgColor;
     protected Color currentBgColor;
     protected Stroke currentStroke;
 
     public abstract void drawShape(Graphics2D g2D);
 
     protected void beforeDrawing(Graphics2D g2D){
         currentFgColor = g2D.getColor();
         currentBgColor = g2D.getBackground();
         currentStroke = g2D.getStroke();
 
         g2D.setColor(drawingInfo.getFgColor());
         g2D.setBackground(drawingInfo.getBgColor());
         g2D.setStroke(new BasicStroke(drawingInfo.getStrokeSize()));
     }
 
     protected void afterDrawing(Graphics2D g2D){
         g2D.setColor(currentFgColor);
         g2D.setBackground(currentBgColor);
         g2D.setStroke(currentStroke);
        
        currentFgColor = null;
        currentBgColor = null;
        currentStroke = null;

     }
 
     protected void switchColorBtwBgNFg(Graphics2D g2D) {
         Color bgColor = g2D.getBackground();
         Color fgColor = g2D.getColor();
         g2D.setBackground(fgColor);
         g2D.setColor(bgColor);
     }
 
     public void setDrawingInfo(NwbDrawingInfo info) {
         this.drawingInfo = info;
     }
 }
