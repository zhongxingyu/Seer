 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.mrfornal.lts;
 
 import net.mrfornal.entity.Entity;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Vector2f;
 
 /**
  *
  * @author stone205446
  */
 public class Car extends Entity
 {
     private Image image;
     private Vector2f velocity;
     private Vector2f acceleration;
     
     public Car(Vector2f position, float rotation, int layer) throws SlickException
     {
         super();
         image = new Image("resource/image/ctsv.png");
         this.position = position;
         this.rotation = rotation;
         this.layer = layer;
         velocity = new Vector2f(0,0);
         acceleration = new Vector2f(.0009f,0);
         
     }
     
     public void setVelocity(Vector2f velocity)
     {
         this.velocity = velocity;
     }
     
     public void setAcceleration(Vector2f acceleration)
     {
         this.acceleration = acceleration;
     }
         
     public Vector2f getVelocity()
     {
         return velocity;
     }
     
     public Vector2f getAcceleration()
     {
         return acceleration;
     }
     
     @Override
     public void init(GameContainer container) throws SlickException
     {
         position.x = container.getWidth() / 2;
         position.y = container.getHeight() / 2;     
     }
     
     @Override
     public void update(GameContainer container, int delta) throws SlickException
     {
         Input i = container.getInput();
         
         acceleration.setTheta(rotation);
         
         if (i.isKeyDown(Input.KEY_LEFT))
         {
             rotation -= 0.1f * delta;
            velocity.scale(0.997f);
            
         }
         if (i.isKeyDown(Input.KEY_RIGHT))
         { 
             rotation += 0.1f * delta;
             velocity.scale(0.997f);
         }
         if (i.isKeyDown(Input.KEY_UP))
         {
             velocity.add(acceleration);
            System.out.println(acceleration);
            System.out.println(velocity);
         }
         if(i.isKeyDown(Input.KEY_DOWN))
         {
             velocity.scale(0.995f);
            System.out.println(acceleration);
            System.out.println(velocity);
         }
         
         if(!i.isKeyDown(Input.KEY_LEFT) && !i.isKeyDown(Input.KEY_RIGHT) 
                 && !i.isKeyDown(Input.KEY_UP) && !i.isKeyDown(Input.KEY_DOWN))
         {
             velocity.scale(0.999f);
         }
        
         position.add(velocity);
 
         if (position.x > container.getWidth())
         {
             position.x = Math.abs(container.getWidth() - position.x);
         }
         if (position.y > container.getHeight()) 
         {
             position.y = Math.abs(container.getHeight() - position.y);
         }
         if (position.x < 0) 
         {
             position.x = container.getWidth() - Math.abs(position.x);
         }
         if (position.y < 0)
         {
             position.y = container.getHeight() - Math.abs(position.y);
         }
         
        
     }
     
     @Override
     public void render(GameContainer container, Graphics g) throws SlickException
     {
         image.setRotation(rotation);
         float x = position.x - image.getWidth();
         float y = position.y - image.getHeight();
         image.draw(x, y, 1);
        g.drawString(velocity.toString(), position.x , position.y + image.getHeight() / 2 + 10);
     }   
 }
