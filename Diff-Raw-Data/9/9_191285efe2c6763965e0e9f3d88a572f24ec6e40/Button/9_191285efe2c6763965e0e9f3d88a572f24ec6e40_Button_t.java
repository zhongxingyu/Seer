 package vooga.scroller.level_management.splash_page;
 
 import java.awt.Dimension;
 import util.Location;
 import vooga.scroller.util.ISpriteView;
 import vooga.scroller.util.Pixmap;
 import vooga.scroller.util.Sprite;
 
 public class Button extends Sprite {
 
     
     private static final Location DEFAULT_CENTER = new Location(400,150);
     private static final Dimension MOUSE_DIMENSIONS = new Dimension(100,100);
     private static final int DEFAULT_HEALTH = 9000;
     private static final int DEFAULT_DAMAGE = 9000;
    private static final ISpriteView DEFAULT_IMAGE = new Pixmap("/vooga/scroller/images/start_button.png");
     
     public Button () {
         super(DEFAULT_IMAGE, DEFAULT_CENTER, MOUSE_DIMENSIONS, DEFAULT_HEALTH, DEFAULT_DAMAGE);
         // TODO Auto-generated constructor stub
     }
 
 }
