 /**
  * DISASTEROIDS
  * TutorialMode.java
  */
 package disasteroids;
 
 import disasteroids.gui.AsteroidsFrame;
 import disasteroids.gui.Local;
 import disasteroids.weapons.BulletManager;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import javax.swing.JOptionPane;
 
 /**
  * A short tutorial for players.
  * @author Phillip Cohen
  */
 public class TutorialMode implements GameMode
 {
     int stage = 0, counter = 0;
 
     double playerStartX = 0, playerStartY = 0;
 
     public void act()
     {
         if ( Local.isStuffNull() )
             return;
 
         ++counter;
 
         // Welcome!
         if ( stage == 0 && counter > 200 )
             nextStage();
 
         // You are the ship.
         if ( stage == 1 && counter > 100 )
             nextStage();
 
         // Use the arrow keys.
         if ( stage == 2 && counter > 200 )
         {
             nextStage();
            Game.getInstance().getObjectManager().addObject( new Asteroid( Local.getLocalPlayer().getX(), Local.getLocalPlayer().getY() - 150, 0, -0.5, 150, 5 ) );
         }
 
         // Ram it!
         if ( stage == 3 && counter > 50 && Game.getInstance().getObjectManager().getAsteroids().size() != 1 )
             nextStage();
 
         // Good!
         if ( ( stage == 4 || stage == 6 || stage == 10 || stage == 16 ) && counter > 135 )
             nextStage();
 
         // Shoot!
         if ( stage == 5 && counter > 250 && Game.getInstance().getObjectManager().getAsteroids().size() == 0 )
             nextStage();
 
         // You have two cows - ah, weapons.
         if ( stage == 7 && counter > 200 )
             nextStage();
 
         // Use whichever.
         if ( stage == 8 && Local.getLocalPlayer().getWeaponManager() instanceof BulletManager )
         {
             nextStage();
             for ( int i = 0; i < 8; i++ )
                 Game.getInstance().getObjectManager().addObject( new Asteroid( Local.getLocalPlayer().getX() + Util.getRandomGenerator().nextInt( 900 ) - 450,
                                                                                Local.getLocalPlayer().getY() - 700 + Util.getRandomGenerator().nextInt( 80 ) - 40,
                                                                               Util.getRandomGenerator().nextMidpointDouble( 2 ), Util.getRandomGenerator().nextDouble() * 2, Util.getRandomGenerator().nextInt( 50 ) + 60, 50 ) );
         }
 
         // Take it out!
         if ( stage == 9 && counter > 250 && Game.getInstance().getObjectManager().getAsteroids().size() == 0 )
             nextStage();
 
         // Boring.
         if ( stage == 11 && counter > 200 )
             nextStage();
 
         // Aliens!
         if ( stage == 12 && counter > 200 )
             nextStage();
 
         // Manuevering.
         if ( stage == 13 && counter > 200 )
             nextStage();
 
         // Strafing.
         if ( stage == 14 && counter > 700 )
         {
             nextStage();
             for ( int i = 0; i < 4; i++ )
                 Game.getInstance().getObjectManager().addObject( new Alien( Local.getLocalPlayer().getX() + Util.getRandomGenerator().nextInt( 900 ) - 450,
                                                                             Local.getLocalPlayer().getY() - 700 + Util.getRandomGenerator().nextInt( 80 ) - 40,
                                                                            Util.getRandomGenerator().nextMidpointDouble( 2 ), Util.getRandomGenerator().nextDouble() * 2 ) );
         }
 
         // Here they come!
         if ( stage == 15 && Game.getInstance().getObjectManager().getBaddies().size() == 0 )
             nextStage();
 
         // Berserk.
         if ( stage == 17 && counter > 350 )
             nextStage();
 
         // Press ~.
         if ( stage == 18 && counter > 150 && !Local.getLocalPlayer().getWeapons()[0].canBerserk() )
             nextStage();
 
         if ( stage == 19 && counter > 250 )
             nextStage();
     }
 
     private void nextStage()
     {
         counter = 0;
         ++stage;
     }
 
     public void draw( Graphics g )
     {
         Graphics2D g2d = (Graphics2D) g;
         Font title = new Font( "Tahoma", Font.BOLD, 24 );
         Font textFont = new Font( "Tahoma", Font.BOLD, 12 );
         g.setFont( textFont );
         int x = 0,  y = AsteroidsFrame.frame().getPanel().getHeight() / 4;
         String text = "";
         g.setColor( Local.getLocalPlayer().getColor() );
         switch ( stage )
         {
             case 0:
                 g.setFont( title );
                 y = Math.min( counter * 4, AsteroidsFrame.frame().getPanel().getHeight() / 4 );
                 text = "Welcome to DISASTEROIDS!";
                 break;
             case 1:
                 text = "You're the player in the center of the screen.";
                 break;
             case 2:
                 text = "To move, use the arrow keys.";
                 break;
             case 3:
                 text = "Try it - ram that asteroid!";
                 break;
             case 4:
             case 6:
             case 10:
             case 16:
                 text = "Good!";
                 break;
             case 5:
                 text = "Now try shooting. Press SPACE to shoot.";
                 break;
             case 7:
                 text = "By default, you have two guns.";
                 break;
             case 8:
                 text = "You've seen the missile launcher. Press Q to cycle to the MACHINE GUN!";
                 break;
             case 9:
                 text = "Use whichever gun you like to take out this next wave.";
                 break;
             case 11:
                 text = "Asteroids are pretty boring.";
                 break;
             case 12:
                 text = "This is why god made aliens.";
                 break;
             case 13:
                 text = "You'll need some better manuevering skills, because they fire back.";
                 break;
             case 14:
                 text = "Use CTRL and NUMPAD0 to strafe left and right.";
                 break;
             case 15:
                 text = "Here they come!";
                 break;
             case 17:
                 text = "Our last tidbit of advice is how to BERSERK!";
                 break;
             case 18:
                 text = "Press ~ to release a powerful shelling of shrapnel!";
                 break;
             case 19:
                 text = "Would've been helpful earlier, no?";
                 break;
             case 20:
                 text = "Those're the basics! Enjoy playing the game.";
                 break;
 
         }
         x = (int) ( AsteroidsFrame.frame().getPanel().getWidth() / 2 - g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getWidth() / 2 );
         g.drawString( text, x, y );
     }
 
     public void flatten( DataOutputStream stream ) throws IOException
     {
         throw new UnsupportedOperationException( "Tutorials can't be used in net games." );
     }
 
     public void optionsKey()
     {
         try
         {
             stage = Integer.parseInt( JOptionPane.showInputDialog( null, "Enter the section to skip to.", stage ) );
             counter = Integer.MAX_VALUE / 2;
         }
         catch ( NumberFormatException e )
         {
             // Do nothing with incorrect or cancelled input.
             Running.log( "Invalid section command.", 800 );
         }
     }
 
     public int id()
     {
         throw new UnsupportedOperationException( "Tutorials can't be used in net games." );
     }
 }
