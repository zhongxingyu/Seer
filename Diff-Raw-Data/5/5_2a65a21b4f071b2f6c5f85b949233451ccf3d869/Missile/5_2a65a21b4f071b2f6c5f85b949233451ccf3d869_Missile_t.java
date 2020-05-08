 /*
  * DISASTEROIDS
  * Missile.java
  */
 package disasteroids;
 
 import disasteroids.gui.AsteroidsFrame;
 import disasteroids.gui.ParticleManager;
 import disasteroids.gui.Particle;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.util.Random;
 
 /**
  * A more elaborate bullet that sets off chain reactions.
  * @author Andy Kooiman
  * @since Classic
  */
 public class Missile extends GameObject implements Weapon, GameElement
 {
     /**
      * The <code>Color</code> to be drawn in.
      * @since Classic
      */
     private Color myColor;
 
     /**
      * The angle the <code>Missile</code> is pointing (not necessarily the angle at which it is moving).
      * @since Classic
      */
     private double angle;
 
     /**
      * How long <code>this</code> has been in existance.
      * @since Classic
      */
     private int age;
 
     /**
      * The current stage of explosion.
      * @since Classic
      */
     private int explodeCount = 0;
 
     /**
      * Whether <code>this</code> is currently exploding.
      * @since Classic
      */
     private boolean isExploding;
 
     /**
      * The current radius of <code>this</code>.
      * @since Classic
      */
     private double radius;
 
     /**
      * Whether <code>this</code> will have a huge blast or not.
      * @since Classic
      */
     private boolean hugeBlast;
 
     /**
      * The <code>MissileManager</code> to which <code>this</code> belongs.
      * @since Classic
      */
     private MissileManager manager;
 
     /**
      * Whether <code>this</code> should be removed.
      * @since Classic
      */
     private boolean needsRemoval = false;
 
     /**
      * Constructs a new instance of <code>Missile</code>.
      * @param m The <code>MissileManager</code> responsible for <code>this</code>.
      * @param x The x coordinate.
      * @param y The y coordinate.
      * @param angle The angle to be pointing.
      * @param dx The x velocity.
      * @param dy The y velocity (up is negative).
      * @param c The <code>Color to be drawn in.
      * @author Andy Kooiman
      * @since Classic
      */
     public Missile( MissileManager m, int x, int y, double angle, double dx, double dy, Color c )
     {
         manager = m;
         setData( x, y, angle, dx, dy, c );
     }
 
     /**
      * A utility method called by the constructor to intialize the object.
      * @param x The x coordinate.
      * @param y The y coordinate.
      * @param angle The angle to be pointing.
      * @param dx The x velocity.
      * @param dy The y velocity (up is negative).
      * @param c The <code>Color to be drawn in.
      * @author Andy Kooiman
      * @since Classic
      */
     private void setData( int x, int y, double angle, double dx, double dy, Color c )
     {
         age = 0;
         setLocation( x, y );
         setSpeed( dx, dy );
         this.angle = angle;
         radius = 3;
         explodeCount = 0;
         isExploding = false;
         myColor = c;
         hugeBlast = ( RandomGenerator.get().nextInt( manager.hugeBlastProb() ) <= 1 );
     }
 
     /**
      * Draws <code>this</code>
      * @param g The <code>Graphics</code> context in which to be drawn
      * @since Classic
      */
     public void draw( Graphics g )
     {
         AsteroidsFrame.frame().drawLine( g, myColor, (int) getX(), (int) getY(), 10, angle + Math.PI );
         AsteroidsFrame.frame().fillCircle( g, myColor, (int) getX(), (int) getY(), (int) radius );
 
         // Draw explosion.
         Color col;
         switch ( explodeCount )
         {
             case 1:
             case 2:
             case 3:
             case 4:
                 if ( explodeCount % 2 == 0 )
                     col = myColor;
                 else
                     col = Color.yellow;
                 AsteroidsFrame.frame().fillCircle( g, col, (int) getX(), (int) getY(), (int) radius );
                 break;
             case 5:
             case 6:
             case 7:
             case 8:
                 if ( explodeCount % 2 == 0 )
                     col = myColor;
                 else
                     col = Color.yellow;
                 radius = 5;
                 AsteroidsFrame.frame().fillCircle( g, col, (int) getX(), (int) getY(), (int) radius );
                 break;
             case 9:
             case 10:
             case 11:
                 if ( hugeBlast )
                 {
                     col = myColor;
                     radius = manager.hugeBlastSize();
                     AsteroidsFrame.frame().fillCircle( g, col, (int) getX(), (int) getY(), (int) radius );
                 }
                 else
                 {
                     radius = 14;
                     col = Color.yellow;
                     AsteroidsFrame.frame().fillCircle( g, col, (int) getX(), (int) getY(), (int) radius );
                     this.explodeCount++;
                 }
                 break;
             }
     }
 
     /**
      * Moves <code>this</code> according to its speed.
      * 
      * @since Classic
      */
     @Override
     public void move()
     {
         super.move();
         setDx( ( getDx() + manager.speed() * Math.cos( angle ) / 50 ) * .98 );
         setDy( ( getDy() - manager.speed() * Math.sin( angle ) / 50 ) * .98 );
     }
 
     /**
      * Steps <code>this</code> through one iteration and draws it.
      * 
      * @author Andy Kooiman
      * @since Classic
      */
     public void act()
     {
         if ( age < 30 )
         {
             Random rand = RandomGenerator.get();
             for ( int i = 0; i < (int) ( 7 - Math.sqrt( getDx() * getDx() + getDy() * getDy() ) ); i++ )
                 ParticleManager.addParticle( new Particle(
                                              getX() + rand.nextInt( 8 ) - 4,
                                              getY() + rand.nextInt( 8 ) - 4,
                                              rand.nextInt( 4 ),
                                              myColor,
                                              rand.nextDouble() * 3,
                                              angle + rand.nextDouble() * .4 - .2 + Math.PI,
                                              30, 10 ) );
         }
         age++;
         move();
         checkLeave();
         explode( explodeCount );
     }
 
     /**
      * Checks the age of <code>this</code> and starts the explosion sequence if too old.
      * @author Andy Kooiman
      * @since Classic
      */
     private void checkLeave()
     {
         if ( age > manager.life() )
             explode();
     }
 
     /**
      * Initiates the explosion sequence.
      * @author Andy Kooiman
      * @since Classic
      */
     public void explode()
     {
         if ( isExploding )
             return;
 
         // Simply pop into several other <code>Missiles</code>.
         if ( RandomGenerator.get().nextInt( manager.probPop() ) <= 101 )
             pop();
 
         explodeCount = 1;
         isExploding = true;
     }
 
     /**
      * Steps <code>this</code> through the explosion sequence and draws.
      * 
      * @param explodeCount The current stage of the explosion.
      * @since Classic
      */
     private void explode( int explodeCount )
     {
         if ( explodeCount <= 0 )
             return;
         this.explodeCount++;
         switch ( explodeCount )
         {
             case 0:
                 return;
             case 1:
             case 2:
             case 3:
             case 4:
                 setDx( getDx() * .8 );
                 setDy( getDy() * .8 );
                 radius = 3;
                 break;
             case 5:
             case 6:
             case 7:
             case 8:
                 setDx( getDx() * .8 );
                 setDy( getDy() * .8 );
                 break;
             case 9:
             case 10:
             case 11:
                 setDx( getDx() * .8 );
                 setDy( getDy() * .8 );
                 break;
             default:
                 needsRemoval = true;
         }
     }
 
     /**
     * Splits <code>this</code> into several new <code>Missile</code>s.
      * @author Andy Kooiman
      * @since Classic
      */
     private void pop()
     {
         if ( needsRemoval )
             return;
         for ( double ang = 0; ang < 2 * Math.PI; ang += 2 * Math.PI / manager.popQuantity() )
            manager.add( new Missile( manager, (int) getX(), (int) getY(), ang, 0, 0, myColor ), false );
         needsRemoval = true;
     }
 
     /**
      * Sets the current stage of explosion
      * @param count The new stage of explosion.
      * @author Andy Kooiman
      * @since Classic
      */
     public void setExplodeCount( int count )
     {
         explodeCount = count;
     }
 
     /**
      * Gets the current stage of explosion.
      * @return The current stage of explosion.
      * @author Andy Kooiman
      * @since Classic
      */
     public int getExplodeCount()
     {
         return explodeCount;
     }
 
     /**
      * Returns whether <code>this</code> need to be removed.
      * @return Whether <code>this</code> should be removed.
      * @author Andy Kooiman
      * @since Classic
      */
     public boolean needsRemoval()
     {
         return needsRemoval;
     }
 
     /**
      * Returns the damage this <code>Weapon</code> will do.
      * 
      * @return The damage done by this <code>Weapon</code>
      */
     public int getDamage()
     {
         return 100;
     }
 
     public double getAngle()
     {
         return angle;
     }
 
     public Color getMyColor()
     {
         return myColor;
     }
 
     public int getAge()
     {
         return age;
     }
 
     public void setAge( int age )
     {
         this.age = age;
     }
 
     public double getRadiusDouble()
     {
         return radius;
     }
 
     public int getRadius()
     {
         return (int) radius;
     }
     
     
 
     public void setRadius( double radius )
     {
         this.radius = radius;
     }
 
     public MissileManager getManager()
     {
         return manager;
     }
     
     
 }
