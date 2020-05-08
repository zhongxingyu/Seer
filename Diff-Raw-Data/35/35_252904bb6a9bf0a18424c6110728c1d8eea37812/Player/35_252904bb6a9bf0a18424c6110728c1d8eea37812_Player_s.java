 package combat;
 /** 
  * @author DevC
  * @version $Id: Player.java,v 1.17 2012/04/08 03:50:18 DevA Exp $
  *
  * This class defines a player object; it holds the visual representation of 
  *  the object, as well as the logic for how this object interacts with 
  *  other objects
  *
  * Revision History:
  *   $Log: Player.java,v $
  *   Revision 1.17  2012/04/08 03:50:18  DevA
  *   Cleaned up the code to run with Java 1.6: removed unused imports,
  *   fixed some UI focus issues (introduced by new focus "features" in Java since
  *   our original implementation), and made the CommandInterpreter not a Singleton
  *
  *   Revision 1.16  2003/05/30 19:30:21  DevB
  *   Reformatted.
  *
  *   Revision 1.15  2000/05/11 22:29:40  DevC
  *   comments & cleaning up the code
  *
  *   Revision 1.14  2000/05/11 07:12:04  DevA
  *   Got rid of code that was originally supposed to help cleanup
  *   after a round, but, instead, was just causing a phantom
  *   player to stick around.  (the "last" boolean was useless)
  *
  *   Revision 1.13  2000/05/11 06:29:12  DevA
  *   Removed most debugs from the system and now everything works
  *   great on a New Game or New Round...at least as far as the user
  *   can tell.  I think that whichever Player won a round is left
  *   hanging a bit under the hood.  It never redraws, but it
  *   never really goes away.  Seems like a waste of processor time
  *   so I'm stil trying to figure this out.
  *
  *   Revision 1.12  2000/05/10 22:05:03  DevA
  *   Upped player travel distance.
  *
  *   Revision 1.11  2000/05/10 04:58:33  DevA
  *   Made the amount of travel per tick a class variable so it's easier to change
  *
  *   Revision 1.10  2000/05/09 16:35:27  DevA
  *   Becomes inactive when hit by a bullet.
  *
  *   Revision 1.9  2000/05/09 15:47:39  DevA
  *   Added getDirection
  *
  *   Revision 1.8  2000/05/09 15:26:35  DevA
  *   Fixed moves so that they are based on the correct
  *   origin.
  *
  *   Revision 1.7  2000/05/09 14:57:56  DevA
  *   Registering all moves now and has a stay() method
  *
  *   Revision 1.6  2000/05/09 08:07:46  DevA
  *   Handles a null enemy.
  *
  *   Revision 1.5  2000/05/09 06:00:20  DevC
  *   added some missing breaks
  *
  *   Revision 1.4  2000/05/09 04:58:04  DevA
  *   Upped move distance.
  *
  *   Revision 1.3  2000/05/08 21:21:44  DevC
  *   did conflict resolution, and implemented
  *   movement methods.
  *
  *   Revision 1.2  2000/05/08 04:28:55  DevC
  *   added some code for the conflict resolution;
  *   changed the signature of the move() method
  *
  *   Revision 1.1  2000/05/05 21:26:44  DevC
  *   Initial revision
  *
  */
 
 
 public class Player extends Sprite
 {
     /**
      * The amount of travel per tick
      */
     int step = 6;
     
     /**
      * The constructor.
      * @param   image       The directional image file.
      * @param   newBoard    The board to use.
      */
     public Player( DirectionalImage image, Board newBoard )
     {
         super( image, newBoard );
     }
 
     /**
      * Instruct the player to handle the given conflict.  
      * @param   enemy   The sprite that is in conflict with this one.
      */
     public void conflict( Sprite enemy )
     {
         //determine what the object is that I am conflicting with and 
         // take the appropriate action
         
         if( enemy == null )
         {
             // If the conflict is moving off the board, move back
             moveBackward();
         }
         
         if( enemy instanceof Barrier )
         {
             //On a barrier, I bounce back
             moveBackward();
         }
         
         //on another player, I bounce back
         else if( enemy instanceof Player )
         {
             moveBackward();
         }
         
         //on a bullet, I die; so i rotate around for awhile
         else if( enemy instanceof Bullet )
         {
             onBoard = false;
         }
         
         //here for completeness
         else{}
     }
 
    /**
     * Foreward movement of the Player
     */
     public void moveForward()
     {
         //as long as I'm on the board
         if( onBoard )
         {
             //get the direction of the sprite to find what is foreward;
             int dir = image.getDirection();
             
             //now check to see which way to move
             switch( dir )
             {
                 case( 1 ):
                     image.translate( 0, -step );
                     break;
                 case( 2 ):
                     image.translate( step, -step );
                     break;
                 case( 3 ):
                     image.translate( step, 0 );
                     break;
                 case( 4 ):
                     image.translate( step, step );
                     break;
                 case( 5 ):
                     image.translate( 0, step );
                     break;
                 case( 6 ):
                     image.translate( -step, step );
                     break;
                 case( 7 ):
                     image.translate( -step, 0 );
                     break;
                 case( 8 ):
                     image.translate( -step, -step );
                     break;
                 default:
             }
             
             //register my move with the board
             board.registerMove( image, this );
         }
     }
     
    /**
     * Backward movement of the Player
     */
     public void moveBackward()
     {
         //as long as I'm on the board and visible...
         if( onBoard )
         {
             //get the direction of the sprite to find what is foreward;
             int dir = image.getDirection();
             
             //now check to see which way to move
             switch( dir )
             {
                 case( 1 ):
                     image.translate( 0, step );
                     break;
                 case( 2 ):
                     image.translate( -step, step );
                     break;
                 case( 3 ):
                     image.translate( -step, 0 );
                     break;
                 case( 4 ):
                     image.translate( -step, -step );
                     break;
                 case( 5 ):
                     image.translate( 0, -step );
                     break;
                 case( 6 ):
                     image.translate( step, -step );
                     break;
                 case( 7 ):
                     image.translate( step, 0 );
                     break;
                 case( 8 ):
                     image.translate( step, step );
                     break;
                 default:
             }
             
             //register my move with the board
             board.registerMove( image, this );
         }
     }
     
    /**
     * Right turning of the Player.
     */
     public void turnRight()
     {
         //we perform a turn if the command is issued and we're on the baord
         if( onBoard )
         {
             image.rotate( 1 );
             board.registerMove( image, this );
         }
     }
 
    /**
     * Left turning of the Player.
     */
     public void turnLeft()
     {
         //we perform a turn if the command is issued and we're on the baord
         if( onBoard )
         {
             image.rotate( -1 );
             board.registerMove( image, this );
         }
     }
 
    /**
     * No move, but re-register.  ensures this object gets painted
     */
     public void stay()
     {
       if( onBoard ) board.registerMove( image, this );
     }
 
     /**
      * Gets the direction of the player
      * @return    The direction of the player
      */
     public int getDirection()
     {
         return image.getDirection();
     }
     
    /**
     * The player makes and returns its next move
     * this will never get called, since Players aren't ticked - 
     * their managers are.  Here for consistancy
     */
     protected void move(){}
 
    /**
     * This is a no-op
     */
     protected void end()
     {
         onBoard = false;
     }
 
 }
