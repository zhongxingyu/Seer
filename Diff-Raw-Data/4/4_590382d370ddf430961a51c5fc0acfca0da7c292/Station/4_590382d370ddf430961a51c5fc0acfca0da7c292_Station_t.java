 /*
  * DISASTEROIDS
  * Station.java
  */
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 /**
  * A satellite that shoots missiles at passing ships.
 * @author Phillip Cohen, Andy Kooiman
  * @since January 6, 2008
  */
 public class Station extends GameObject implements ShootingObject
 {
     /**
      * When 0 or less, we can shoot. Otherwise we're reloading.
      * @since January 6, 2008
      */
     private int shootTimer = 0;
 
     /**
      * The angle we're facing.
      * @since January 6, 2008
      */
     private double angle;
 
     /**
      * Our firing manager.
      * @since January 6, 2008
      */
     private MissileManager manager;
 
     /**
      * Width/height of the station.
      * @since January 6, 2008
      */
     int size = 35;
 
     /**
      * Creates the station at the given position and random floating speed.
      * 
      * @param x     x coordinate in game
      * @param y     y coordinate in game
      * @since January 6, 2008
      */
     public Station( double x, double y )
     {
         setLocation( x, y );
         setDx( Math.random() * 2 - 1 );
         setDy( Math.random() * 2 - 1 );
         angle = 0;
         manager = new MissileManager();
         manager.setPopQuantity( 0 );
        manager.setLife( 50 );
         shootTimer = 0;
     }
 
     /**
      * Moves, acquires a target, and shoots.
      * 
      * @since January 6, 2008
      */
     public void act()
     {
         move();
 
         // Find players within our range.
         int range = 300;
         Ship closestShip = null;
         for ( Ship s : Game.getInstance().players )
         {
             if ( getProximity( s ) < range )
             {
                 if ( closestShip == null || getProximity( s ) > getProximity( closestShip ) )
                     closestShip = s;
             }
         }
 
         // Aim towards closest ship.
         if ( closestShip != null )
         {
             angle = Math.atan( ( closestShip.getY() - getY() ) / (double) ( closestShip.getX() - getX() ) );
             if ( closestShip.getX() - getX() < 0 )
                 angle += Math.PI;
 
             // Fire!
             if ( canShoot() )
             {
                 manager.add( (int) ( centerX() + 25 * Math.cos( 0 - angle ) ), (int) ( centerY() - 25 * Math.sin( 0 - angle ) ), 0 - angle, 0d, 0d, Color.white );
                 shootTimer = 10;
             }
         }
         else
             angle += 0.01;
 
         // Reload.
         if ( !canShoot() )
             shootTimer--;
 
         manager.act();
     }
 
     /**
      * Returns the distance to a given ship using pythagoras.
      * 
      * @param s     the ship
      * @return      the distance to it
      * @since January 6, 2008
      */
     private double getProximity( Ship s )
     {
         return Math.sqrt( Math.pow( getX() - s.getX(), 2 ) + Math.pow( getY() - s.getY(), 2 ) );
     }
 
     /**
      * Draws this and our bullets to the given context. Uses RelativeGraphics.
      * 
      * @param g the context
      * @since January 6, 2008
      */
     public void draw( Graphics g )
     {
         int rX = RelativeGraphics.translateX( getX() );
         int rY = RelativeGraphics.translateY( getY() );
 
         int cX = RelativeGraphics.translateX( centerX() );
         int cY = RelativeGraphics.translateY( centerY() );
 
         // Draw the base.
         g.setColor( Color.darkGray );
         g.fillRect( rX, rY, size, size );
         g.setColor( new Color( 20, 20, 20 ) );
         g.drawRect( rX, rY, size, size );
 
         // Draw the corners.
         g.setColor( new Color( 20, 20, 20 ) );
         g.fillRect( rX - 2, rY - 2, 10, 10 );
         g.fillRect( rX + 27, rY, 10, 10 );
         g.fillRect( rX + 27, rY + 27, 10, 10 );
         g.fillRect( rX, rY + 27, 10, 10 );
 
         // Draw the turret.
         g.setColor( Color.white );
         int eX = (int) ( cX + 15 * Math.cos( angle ) );
         int eY = (int) ( cY + 15 * Math.sin( angle ) );
         g.drawLine( cX, cY, eX, eY );
         g.drawLine( cX, cY + 1, eX, eY + 1 );
         g.drawLine( cX + 1, cY, eX + 1, eY );
 
         manager.draw( g );
     }
 
     /**
      * Returns the center position of the station.
      * 
      * @return      the x coordinate of the center
      * @since January 6, 2008
      */
     int centerX()
     {
         return (int) ( getX() + size / 2 );
     }
 
     /**
      * Returns the center position of the station.
      * 
      * @return      the y coordinate of the center
      * @since January 6, 2008
      */
     int centerY()
     {
         return (int) ( getY() + size / 2 );
     }
 
     /**
      * Returns a linked queue containing our one weapon manager. Used for ShootingObject.
      * 
      * @return  thread-safe queue containg our <code>MissileManager</code>
      * @since January 6, 2008
      */
     public ConcurrentLinkedQueue<WeaponManager> getManagers()
     {
         ConcurrentLinkedQueue<WeaponManager> c = new ConcurrentLinkedQueue<WeaponManager>();
         c.add( manager );
         return c;
     }
 
     /**
      * Returns if we're done reloading.
      * 
      * @return  if we're done reloading
      * @since January 6, 2008
      */
     public boolean canShoot()
     {
         return ( shootTimer == 0 );
     }
 }
