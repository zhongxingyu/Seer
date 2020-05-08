 package sts.game;
 
 import java.awt.Graphics2D;
 import sts.gui.ImageHandler;
 
 /**
  * A pile of gold.
  * @author Phillip Cohen
  */
 public class GoldPile extends GameObject
 {
    int remainingGold = 500;
 
     public GoldPile( int x, int y, Player nature )
     {
         super( x, y, Integer.MAX_VALUE, nature );
     }
 
     @Override
     public void act()
     {
         
     }
     
     public boolean removeGold()
     {
       
         if(remainingGold==1)//out of Gold
             getOwningPlayer().removeObject(this);
         return --remainingGold>=0;
     }
     
     public int getGold()
     {
         return remainingGold;
     }
 
     @Override
     public void draw( Graphics2D g )
     {
         ImageHandler.drawGold(g, getX(), getY());   
     }
 
     @Override
     public String getName()
     {
         return "Gold pile with "+remainingGold +" gold remaining";
     }
     
     @Override
     public boolean isClickContained( int x, int y )
     {
         return isClickContainedInRectangle( this, x, y, 8, 8 );
     }        
 }
