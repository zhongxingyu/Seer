 package infinitedog.frisky.gui;
 
 import infinitedog.frisky.entities.Sprite;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.geom.Vector2f;
 
 public class Button extends Sprite {
     private static int id = 0;  
     private String label;
     //Position relative to button
     private Vector2f labelPosition;
     
    public Button(String textureName, String name, String group, String label, Vector2f labelPosition) {
         this.setTexture(textureName);
         this.label = label;
         this.labelPosition = labelPosition;
        this.name = name;
        this.group = group;
     }
     
     @Override
     public void render(GameContainer gc, Graphics g) {
         super.render(gc, g);
         g.setColor(Color.black);
         g.drawString(label, getX() + labelPosition.x, getY() + labelPosition.y);
     }
 
     public void setAutoRendered(boolean b) {
         autoRender = b;
     }
 }
