 package vooga.towerdefense.gameElements;
 
 import java.awt.Dimension;
 import java.util.List;
 
 import vooga.towerdefense.util.Location;
 import vooga.towerdefense.util.Pixmap;
 import vooga.towerdefense.util.Sprite;
 
 
 /**
  * Blank tower that holds its attributes and actions that define it
  * @author Matthew Roy
  *
  */
 public class Tower extends Sprite {
     
     Attributes myAttributes;
     List<AbstractAction> myActions;
     
     public Tower (Pixmap image, Location center, Dimension size, Attributes attributes, List<AbstractAction> actions) {
         super(image, center, size);
         myAttributes = attributes;
         myActions = actions;
     }
     
     
     public void update(double elapsedTime) {
         for (AbstractAction a : myActions) {
             a.execute(elapsedTime);
         }
     }
    
    public Attributes getAttributes() {
        return myAttributes;
    }
 
 }
