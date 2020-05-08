 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 
 /**
  * The Planet class is constructed with the x and y coordinates, as well as a radius.
  */
 public abstract class GamePiece implements Position, Drawable
 {
     protected int x,y;
     protected Player owner;
     protected BufferedImage image;
     protected int radius;
     public int clickRadius;  // the maximum distance outside of the centre coordinates that a click can be
 
 
 
     public static BufferedImage resize(BufferedImage originalImage, int r)
     {
         //Assumes that the image is a square. Also r is half the size we want.
         BufferedImage scaledBI = new BufferedImage(r*2, r*2, BufferedImage.TYPE_INT_ARGB);
         Graphics2D g = scaledBI.createGraphics();
         g.drawImage(originalImage, 0, 0, r*2, r*2, null); 
         g.dispose();
         return scaledBI;
     }
 
     /**
      * @return the `x` co-ordinate of the planet.
      */
     public int getX()
     {
         return x;
     }
 
     /**
      * @return the `y` co-ordinate of the planet.
      */
     public int getY()
     {
         return y;
     }
 
     public void setX (int x)
     {
         this.x=x;
     }
 
     public void setY (int y)
     {
         this.y=y;
     }
 
     /**
      * @return the x-coordinate at the centre of the GamePiece
      */
     public int getXCenter()
     {
         return this.getX()+radius;
     }
 
     /**
      * @return the y-coordinate at the centre of the GamePiece
      */
     public int getYCenter()
     {
         return this.getY()+radius;
     }
 
     /**
      * @return a boolean stating whether or not the given click co-ordinate is 'inside' the GamePiece.
      * Used for testing mouse-click-selection.
      */
     public boolean isClickInside(int clickX, int clickY){
         //Is the given coordinate's distance from the centre coordinate less than (or equal) the clickRadius?
         if ((this.getXCenter()-clickX) * (this.getXCenter()-clickX) + (this.getYCenter()-clickY) * (this.getYCenter()-clickY) <= this.getClickRadius()*this.getClickRadius()) {
             return true;
         }
         return false;
     }
 
     public int getClickRadius() {
         return this.clickRadius;
     }
 
     /**
      * @return the Planet radius, or the selection circle radius for Fleets
      */
     public int getRadius() {
         return this.radius;
     }
 
     public double distanceFrom(Position p) 
     {
 
        int dx = this.getX() - p.getX();
        int dy = this.getY() - p.getY();
 
         return Math.sqrt( (dx * dx) + (dy * dy) );
     }
 
 
     public BufferedImage getImage()
     {
         return image;
     }
 
     public Player getPlayer()
     {
         return owner;
     }
 
     public void setPlayer(Player p)
     {
         owner=p;
     }
 
     public boolean belongsTo(Player p)
     {
         return this.getOwner() == p;
     }
 
     public abstract Player getOwner();
 }
