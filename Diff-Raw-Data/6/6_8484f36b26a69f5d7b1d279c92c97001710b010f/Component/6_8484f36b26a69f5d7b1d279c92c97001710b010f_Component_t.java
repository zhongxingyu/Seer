 package com.mjm.Touchwire;
 
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 
 import javax.xml.bind.util.ValidationEventCollector;
import java.util.ArrayList;
import java.util.List;
 
 //The component class is the overarching class of objects to be placed on the board to be wired up
 public class Component
 {
     public Rectangle Bounds; //Rectangle bounds represent size and location of components
     public Texture texture = Main.defaultTexture;
 
     //The positive and negative terminals
     public Terminal posTerminal;
     public Terminal negTerminal;
 
     //Constructor, needs a position as an argument
     public Component(Vector2 pos)
     {
         //Sets bounds rectangle position to the position provided, centered based of texture size, size is set to texture
         Bounds = new Rectangle(pos.x-texture.getWidth()/2,pos.y-texture.getWidth()/2,texture.getWidth(),texture.getHeight());
 
         //setups up the terminals
         posTerminal = new Terminal(this, true);
         negTerminal = new Terminal(this, false);
     }
 
     public void Draw()
     {
         //Draws the texture to position of the Bounds
         Main.spriteBatch.draw(texture, Bounds.x, Bounds.y);
 
         //Draws the terminals
         posTerminal.Draw();
         negTerminal.Draw();
     }
 
     //Special position setting function so terminals follow it around
     public void SetPosition(int x, int y)
     {
         Bounds.x = x;
         Bounds.y = y;
 
         posTerminal.updatePos();
         negTerminal.updatePos();
     }
 
     //Deletion function makes sure everything gets deleted, wires and all
     public void Delete()
     {
         if (posTerminal.wire!=null)
             Main.board.wires.remove(posTerminal.wire);
 
         if (negTerminal.wire!=null)
             Main.board.wires.remove(negTerminal.wire);
 
         Main.board.components.remove(this);
     }
 
 }
 
 //Battery subclass of Component
 class Battery extends Component
 {
    public static List<Component> connectedComponents = new ArrayList<Component>(); //list of components connected to this battery
 
     //constructor inherits from parent, but sets texture
     public Battery(Vector2 pos)
     {
         super(pos);
         texture = Main.batteryTexture;
     }
 }
 
 //Light subclass of Component
 class Light extends Component
 {
     //constructor inherits from parent, but sets texture
     public Light(Vector2 pos)
     {
         super(pos);
         texture = Main.lightTexture;
     }
 
 
 }
