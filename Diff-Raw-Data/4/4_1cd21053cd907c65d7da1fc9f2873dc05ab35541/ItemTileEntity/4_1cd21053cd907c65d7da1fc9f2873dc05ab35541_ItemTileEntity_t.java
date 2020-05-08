 package ca.couchware.wezzle2d.tile;
 
 import ca.couchware.wezzle2d.manager.BoardManager;
 import ca.couchware.wezzle2d.graphics.ISprite;
 import ca.couchware.wezzle2d.*;
 import ca.couchware.wezzle2d.graphics.GraphicEntity;
 
 /**
  * An abstract class for making item tiles like bombs and rockets.
  * 
  * @author cdmckay
  */
 
 public abstract class ItemTileEntity extends TileEntity
 {
     /**
      * The sprite representing the bomb graphic.
      */
     final protected ISprite itemGraphic;
     
     /**
      * The rotation of the item.
      */
     protected double itemTheta;
     
     /**
      * The constructor.
      * @param boardMan
      * @param color
      * @param x
      * @param y
      */    
     public ItemTileEntity(final String path, 
             final BoardManager boardMan, final TileColor color, 
             final int x, final int y)
     {
         // Invoke super.
         super(boardMan, color, x, y);
         
         // Load bomb sprite.
         itemGraphic = ResourceFactory.get().getSprite(path);
         
         // Initialize the item theta.
         itemTheta = 0;
     }
     
     
     /**
      * Override that draw muthafucka.
      */
     @Override
     public boolean draw()
     {
         if (isVisible() == false)
             return false;
         
         // Invoke super draw.
         super.draw();        
         
         // Draw bomb on top of it.
         //itemSprite.draw((int) x2, (int) y2, width, height, itemTheta, opacity);
        itemGraphic.draw(x, y).width(width).height(height)
                .theta(itemTheta, width / 2, height /2)
                .opacity(opacity).end();
         
         return true;
     }
 
     public double getItemTheta()
     {
         return itemTheta;
     }
 
     public void setItemTheta(double itemTheta)
     {
         this.itemTheta = itemTheta;
     }
         
 }
