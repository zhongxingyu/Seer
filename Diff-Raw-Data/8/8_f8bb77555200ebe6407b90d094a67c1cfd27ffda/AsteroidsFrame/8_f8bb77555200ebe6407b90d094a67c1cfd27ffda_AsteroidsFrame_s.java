 /*
  * DISASTEROIDS
  * AsteroidsFrame.java
  */
 
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Font;
 import java.awt.Frame;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.GraphicsDevice;
 import java.awt.GraphicsEnvironment;
 import java.awt.Image;
 import java.awt.Point;
 import java.awt.Polygon;
 import java.awt.RenderingHints;
 import java.awt.Toolkit;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.text.DecimalFormat;
 import java.util.Random;
 import javax.swing.JOptionPane;
 
 /**
  * The main, central class/frame.
  * @author Andy Kooiman, Phillip Cohen
  * @since Classic
  */
 public class AsteroidsFrame extends Frame implements KeyListener
 {
     /**
      * Horizontal dimension of the window.
      * @since November 15 2007
      */
     public static final int WINDOW_WIDTH = 800;
 
     /**
      * Vertical dimension of the window.
      * @since November 15 2007
      */
     public static final int WINDOW_HEIGHT = 800;
 
     /**
      * Default player colors. Inspired from AOE2.
      * @since December 14 2007
      */
     private static final Color[] PLAYER_COLORS = { Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW, Color.MAGENTA, Color.CYAN, Color.ORANGE, Color.PINK };
 
     /**
      * The record high score.
      * @since Classic
      */
     private static double highScore;
 
     /**
      * The record high-scorer's name.
      * @since Classic
      */
     private static String highScoreName;
 
     /**
      * The <code>Image</code> used for double buffering.
      * @since Classic
      */
     private static Image virtualMem;
 
     /**
      * The <code>Image</code> storing the star background.
      * @since Classic
      */
     private static Image background;
 
     /**
      * The current level of the game.
      * @since Classic
      */
     private static int level;
 
     /**
      * The <code>Graphics</code> context of the screen.
      * @since Classic
      */
     private Graphics g;
 
     /**
      * The <code>Graphics</code> context of virtualMem; used for double buffering.
      * @since Classic
      */
     private static Graphics gBuff;
 
     /**
      * Stores whether the game is currently paused or not.
      * @since Classic
      */
     private boolean paused = false;
 
     /**
      * The current game time.
      * @since Classic
      */
     public static long timeStep = 0;
 
     /**
      * The current game time of the other player.
      * @since Classic
      */
     public static long otherPlayerTimeStep = 0;
 
     /**
      * Stores the current <code>Asteroid</code> field.
      * @since Classic
      */
     private AsteroidManager asteroidManager;
 
     /**
      * Stores the current pending <code>Action</code>s.
      * @since Classic
      */
     private ActionManager actionManager;
 
     /**
      * Array of players.
      * @since December 14 2007
      */
     public static Ship[] players;
 
     /**
      * Index of the player that's at this computer.
      * @see <code>players</code>
      * @since December 14 2007
      */
     private static int localPlayer;
 
     /**
      * Whether the user is pressing the scoreboard key.
      * @since December 15 2007
      */
     private boolean drawScoreboard;
 
     /**
      * Constructs the game frame and game elements.
      * 
      * @param playerCount   number of players that will be playing
      * @param localPlayer   index of the player at this computer
      * @since December 14, 2007
      */
     public AsteroidsFrame( int playerCount, int localPlayer )
     {
         Running.setEnvironment( this );
 
         // Close when the exit key is pressed.
         addWindowListener( new CloseAdapter() );
 
         // Set our size - fullscreen or windowed.
         updateFullscreen();
 
         highScore = 1000000;
         highScoreName = "Phillip and Andy";
         
         // Set up the connection/game settings.
         players = new Ship[playerCount];
         AsteroidsFrame.localPlayer = localPlayer;
 
         // Set up the game.
         resetEntireGame();
     }
 
     /**
      * Resets all gameplay elements. Ships, asteroids, timesteps, missiles and actions are all reset.
      * Connection elements like the number of players or the index of <code>localPlayer</code> are not reset.
      * 
      * @since December 16, 2007
      */
     private void resetEntireGame()
     {
         timeStep = 0;
         otherPlayerTimeStep = 0;
         
         // Create the ships.
         actionManager = new ActionManager();
         for ( int i = 0; i < players.length; i++ )
             players[i] = new Ship( getWidth() / 2 - ( i * 100 ), getHeight() / 2, PLAYER_COLORS[i], 0, "Player " + ( i + 1 ) );
 
         // Create the asteroids.
         level = 1;
         asteroidManager = new AsteroidManager();
         asteroidManager.setUpAsteroidField( level );
         
         // Reset the background.
         drawBackground();
     }
 
     /**
      * Sets up double buffering, the <code>background</code>, and the <code>KeyListener</code>.
      * 
      * @param g the unbuffered <code>Graphics</code> context
      * @since December 25, 2007
      */
     private void initBuffering( Graphics g )
     {
         this.g = g;
 
         // Create the buffer.
         virtualMem = createImage( getWidth(), getHeight() );
         gBuff = virtualMem.getGraphics();
 
         // Anti-alias, if the user wants it.
         updateAntiAliasing();
 
         // Receive key events.
         this.addKeyListener( this );
 
        // Create the background.
        background = createImage( getWidth(), getHeight() );
         drawBackground();
     }
 
     /**
      * Steps the game through one timestep and paints all components onto the screen.
      * 
      * @param g the <code>Graphics</code> context of the screen
      * @since Classic
      */
     @Override
     public void paint( Graphics g )
     {
         if ( paused )
             return;
 
         if ( gBuff == null )
             initBuffering( g );
 
         AsteroidsServer.send( "t" + String.valueOf( timeStep ) );
 
         // Are we are too far ahead?
         if ( ( timeStep - otherPlayerTimeStep > 2 ) && ( players.length > 1 ) )
         {
             safeSleep( 20 );
             AsteroidsServer.send( "t" + String.valueOf( timeStep ) );
             AsteroidsServer.flush();
             repaint();
             return;
         }
 
         // Scroll the stars up and draw the background.
         updateBackground();
         gBuff.drawImage( background, 0, 0, this );
 
         // Execute game actions.
         timeStep++;
         try
         {
             actionManager.act( timeStep );
         }
         catch ( UnsynchronizedException e )
         {
             System.out.println( "Action missed! " + e + "\nOur timestep: " + timeStep + ", their timestep: " + otherPlayerTimeStep + "." );
             Running.quit();
         }
 
         // Draw stuff in order of importance, from least to most.
         ParticleManager.drawParticles();
         asteroidManager.act();
 
         // Update the ships.
         for ( int i = 0; i < players.length; i++ )
         {
             players[i].act();
 
             // Game over?
             if ( players[i].livesLeft() < 0 )
             {
                 endGame( g );
                 continue;
             }
         }
 
         // Draw the on-screen HUD.
         drawHud( gBuff );
 
         // Draw the entire scoreboard.
         if ( drawScoreboard )
             drawScoreboard( gBuff );
 
         // Flip the buffer to the screen.
         g.drawImage( virtualMem, 0, 0, this );
 
         // Advance to the next level if all asteroids are destroyed (except -999, which is a sandbox).
         if ( asteroidManager.size() == 0 && level != -999 )
             nextLevel();
 
         // This causes 100% cpu usage, but it's safe to say the game always has to be updated.
         repaint();
     }
 
     /**
      * Draws the star field over itself to make stars move.
      *      
      * @since Classic
      */
     public void updateBackground()
     {
         Graphics gBack = background.getGraphics();
         gBack.drawImage( background, 0, -2, this );
         Random rand = RandNumGen.getStarInstance();
         for ( int y = getHeight() - 3; y < getHeight(); y++ )
             for ( int x = 0; x < getWidth(); x++ )
             {
                 if ( rand.nextInt( 1000 ) < 1 )
                     gBack.setColor( Color.white );
                 else
                     gBack.setColor( Color.black );
                 gBack.fillRect( x, y, 1, 1 );
             }
     }
 
     /**
      * Shows a dialog to warps to a particular level.
      * @author Phillip Cohen
      * @since November 15 2007
      */
     public void warpDialog()
     {
         warp( Integer.parseInt( JOptionPane.showInputDialog( "Enter the level number to warp to.", level ) ) );
     }
 
     /**
      * Advances to the next level.
      * @author Phillip Cohen
      * @since November 15 2007
      */
     public void nextLevel()
     {
         warp( level + 1 );
     }
 
     /**
      * Advances the game to a new level.
      * @param newLevel The level to warp to.
      * @author Phillip Cohen
      * @since November 15 2007
      */
     public void warp( int newLevel )
     {
         paused = true;
         level = newLevel;
 
         // All players get bonuses.
         for ( Ship s : players )
         {
             s.addLife();
             s.setInvincibilityCount( 100 );
             s.increaseScore( 2500 );
             s.getMissileManager().clear();
             s.setNumAsteroidsKilled( 0 );
             s.setNumShipsKilled( 0 );
         }
 
         asteroidManager.clear();
         actionManager.clear();
 
         drawBackground();
         restoreBonusValues();
         System.out.println( "Welcome to level " + newLevel + ". Random asteroid numbers: " +
                             RandNumGen.getAsteroidInstance().nextInt( 9 ) + " " +
                             RandNumGen.getAsteroidInstance().nextInt( 9 ) + " " +
                             RandNumGen.getAsteroidInstance().nextInt( 9 ) + " (Seed: " + RandNumGen.seed + ")" );
         asteroidManager.setUpAsteroidField( level );
         paused = false;
     }
 
     /**
      * Advances to the next level from a static context.
      * @deprecated Use <code>Running.environment()</code> to access frame methods.
      * @since Classic
      */
     @Deprecated
     public static void staticNextLevel()
     {
         Running.environment().nextLevel();
     }
 
     /**
      * Draws the on-screen information for the local player.
      * @param g The <code>Graphics</code> context of the screen.
      * @since December 15, 2007
      * @author Phillip Cohen
      */
     private void drawHud( Graphics g )
     {
         Graphics2D g2d = (Graphics2D) g;
         String text = "";
         int x = 0, y = 0;
 
         // Draw the "lives" string.
         g2d.setColor( players[localPlayer].getColor() );
         g2d.setFont( new Font( "Tahoma", Font.ITALIC, 12 ) );
         if ( players[localPlayer].livesLeft() == 1 )
             text = "life";
         else
             text = "lives";
         x = getWidth() - 40;
         y = getHeight() - 15;
         g2d.drawString( text, x, y );
         x -= 10;
 
         // Draw the lives counter.
         g2d.setFont( new Font( "Tahoma", Font.BOLD, 16 ) );
         text = "" + players[localPlayer].livesLeft();
         x -= (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getWidth();
         g2d.drawString( text, x, y );
         x -= 15;
 
         // Draw the level counter.
         g2d.setColor( Color.lightGray );
         g2d.setFont( new Font( "Tahoma", Font.BOLD, 16 ) );
         text = "" + level;
         x -= (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getWidth();
         g2d.drawString( text, x, y );
 
         // Draw the "level" string.
         g2d.setFont( new Font( "Tahoma", Font.ITALIC, 12 ) );
         text = "level";
         x -= (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getWidth() + 8;
         g2d.drawString( text, x, y );
 
         // Draw the score counter.
         g2d.setColor( Color.darkGray );
         g2d.setFont( new Font( "Tahoma", Font.BOLD, 16 ) );
         text = insertThousandCommas( players[localPlayer].getScore() );
         y = ( isFullscreen() ? 0 : 30 ) + 20; // Offset for the titlebar (yuck)!
         x = getWidth() - (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getWidth() - 12;
         g2d.drawString( text, x, y );
 
         // Draw the "score" string.
         g2d.setFont( new Font( "Tahoma", Font.ITALIC, 12 ) );
         text = "score";
         x -= (int) g2d.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getWidth() + 8;
         g2d.drawString( text, x, y );
     }
 
     /**
      * Draws the full scoreboard of all players.
      * @author Phillip Cohen
      * @since December 15 2007
      */
     private void drawScoreboard( Graphics g )
     {
         Graphics2D g2d = (Graphics2D) gBuff;
         String text = "";
         int x = 0, y = 0;
 
         // Draw the "scoreboard" string.
         g.setColor( Color.white );
         g.setFont( new Font( "Tahoma", Font.BOLD, 24 ) );
         text = "Scoreboard";
         x = getWidth() / 2 - (int) g.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getWidth() / 2;
         y = getHeight() / 4;
         g.drawString( text, x, y );
         y += (int) g.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getHeight() + 10;
 
         // Draw the asteroid count.
         g.setColor( Color.lightGray );
         g.setFont( new Font( "Tahoma", Font.PLAIN, 16 ) );
         text = insertThousandCommas( asteroidManager.size() ) + " asteroid" + ( asteroidManager.size() == 1 ? " remains" : "s remain" );
         x = getWidth() / 2 - (int) g.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getWidth() / 2;
         g.drawString( text, x, y );
         y += (int) g.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getHeight() + 10;
 
         // Create the columns.
         ScoreboardColumn[] columns = new ScoreboardColumn[4];
         columns[0] = new ScoreboardColumn( getWidth() * 2 / 7, "Name" );
         columns[1] = new ScoreboardColumn( getWidth() * 1 / 2, "Score" );
         columns[2] = new ScoreboardColumn( getWidth() * 3 / 5, "Lives" );
         columns[3] = new ScoreboardColumn( getWidth() * 2 / 3, "# Destroyed" );
 
         // Draw the column headers.
         y += 15;
         g.setColor( Color.darkGray );
         g.setFont( new Font( "Tahoma", Font.PLAIN, 14 ) );
         for ( ScoreboardColumn c : columns )
             g.drawString( c.getTitle(), c.getX(), y );
 
         g.drawLine( columns[0].getX(), y + 5, columns[columns.length - 1].getX() + (int) g.getFont().getStringBounds( columns[columns.length - 1].getTitle(), g2d.getFontRenderContext() ).getWidth(), y + 5 );
         y += (int) g.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getHeight() + 10;
 
         // Draw the entries.
         for ( Ship s : players )
         {
             g.setColor( s.getColor() );
             for ( int i = 0; i < columns.length; i++ )
             {
                 switch ( i )
                 {
                     case 0:
                         text = s.getName();
                         break;
                     case 1:
                         text = insertThousandCommas( s.score() );
                         break;
                     case 2:
                         text = "" + s.livesLeft();
                         break;
                     case 3:
                         text = "" + s.getNumAsteroidsKilled();
                         break;
                     default:
                         text = "";
                 }
                 g.drawString( text, columns[i].getX(), y );
             }
             y += (int) g.getFont().getStringBounds( text, g2d.getFontRenderContext() ).getHeight() + 3;
         }
     }
 
     /**
      * Draws the star background completely from scratch.
      * @author Andy Kooiman
      * @since Classic
      */
     private void drawBackground()
     {
         Graphics gBack = background.getGraphics();
         gBack.setColor( Color.black );
         gBack.fillRect( 0, 0, getWidth(), getHeight() );
         gBack.setColor( Color.white );
         Random rand = RandNumGen.getStarInstance();
         for ( int star = 0; star < getWidth() * getHeight() / 1000; star++ )
             gBack.fillRect( rand.nextInt( getWidth() ), rand.nextInt( getHeight() ), 1, 1 );
     }
 
     /**
      * Starts a new game.
      * @author Andy Kooiman
      * @deprecated Doesn't work.
      * @since Classic
      */
     @Deprecated
     public void newGame()
     {
         resetEntireGame();
         if ( ( players.length > 1 ) && ( localPlayer == 0 ) )
             AsteroidsServer.send( "ng" );
     }
 
     /**
      * Displays the end game messages.
      * @param g The <code>Graphics</code> context of the screen.
      * @author Andy Kooiman, Phillip Cohen
      * @since Classic
      */
     public void endGame( Graphics g )
     {
         paused = true;
         g.setFont( new Font( "Tahoma", Font.BOLD, 32 ) );
         if ( Settings.soundOn )
             Sound.wheeeargh();
         for ( float sat = 0; sat <= 1; sat += .00005 )
         {
             g.setColor( Color.getHSBColor( sat, sat, sat ) );
             g.drawString( "Game Over", 250, 400 );
         }
         this.setIgnoreRepaint( true );
 
         // Find the player with the highest score.
         Ship highestScorer = players[0];
         for ( Ship s : players )
         {
             if ( s.getScore() > highestScorer.getScore() )
                 highestScorer = s;
         }
 
         String victoryMessage = ( players.length > 1 ) ? highestScorer.getName() + " wins" : "You died";
         victoryMessage += " with a score of " + highestScorer.getScore() + "!";
 
         // Is this a new high score?
         if ( highestScorer.getScore() > highScore )
         {
             victoryMessage += "\nThis is a new high score!";
             highScoreName = highestScorer.getName();
             highScore = highestScorer.getScore();
             if ( ( players.length > 1 ) && ( highestScorer == players[localPlayer] ) )
                 AsteroidsServer.send( "HS" + highScoreName );
         }
 
         // Show the message box declaring victory!
         JOptionPane.showMessageDialog( this, victoryMessage );
 
         /* Easter egg!
         if ( oldHighScore > 10000000 )
         {
         JOptionPane.showMessageDialog( null, "HOLY CRAP!!!! YOUR SCORE IS HIGH!!!\nI NEED HELP TO COMPUTE IT" );
         try
         {
         Runtime.getRuntime().exec( "C:/Windows/System32/calc" );
         }
         catch ( Exception e )
         {
         }
         }
          */
 
         newGame();
         this.setIgnoreRepaint( false );
         paused = false;
         repaint();
     }
 
     /**
      * Called automatically by key listener, and used only for arrow keys.
      * @param e The <code>KeyEvent</code> generated by the key listener.
      * @author Andy Kooiman
      * @since Classic
      */
     public synchronized void keyReleased( KeyEvent e )
     {
         drawScoreboard = false;
 
         if ( !paused || e.getKeyCode() == 80 )
         {
             if ( e.getKeyCode() >= 37 && e.getKeyCode() <= 40 )
                 // Get the raw code from the keyboard
                 //performAction(e.getKeyCode(), ship);
                 actionManager.add( new Action( players[localPlayer], 0 - e.getKeyCode(), timeStep + 2 ) );
             AsteroidsServer.send( "k" + String.valueOf( 0 - e.getKeyCode() ) + "," + String.valueOf( timeStep + 2 ) );
         // [AK] moved to a new method to also be called by another class, receiving data from other computer
         //repaint();
         }
         repaint();
     }
 
     /**
      * Dummy method to satisfy <code>KeyListener</code> interface.
      * @param e The <code>KeyEvent</code> generated by the key listener.
      * @author Andy Kooiman
      * @since Classic
      */
     public void keyTyped( KeyEvent e )
     {
     }
 
     /**
      * Called automatically by key listener for all keys pressed, and creates an <code>Action</code> to store the relevent data.
      * @param e The <code>KeyEvent</code> generated by the key listener.
      * @author Andy Kooiman
      * @since Classic
      */
     public synchronized void keyPressed( KeyEvent e )
     {
         if ( !paused || e.getKeyCode() == 80 )
         {
             // Get the raw code from the keyboard
             //performAction(e.getKeyCode(), ship);
             actionManager.add( new Action( players[localPlayer], e.getKeyCode(), timeStep + 2 ) );
             AsteroidsServer.send( "k" + String.valueOf( e.getKeyCode() ) + "," + String.valueOf( timeStep + 2 ) );
         // [AK] moved to a new method to also be called by another class, receiving data from other computer
         //repaint();
         }
         repaint();
     }
 
     /**
      * Performs the action specified by the action as applied to the actor.
      * @param action The key code for the action.
      * @param actor The <code>Ship</code> to execute the action.
      * @author Andy Kooiman
      * @since Classic
      */
     public synchronized void performAction( int action, Ship actor )
     {
         /* Decide what key was pressed
          *==========================*/
         switch ( action )
         {
             case KeyEvent.VK_ESCAPE:
                 Running.quit();
             case KeyEvent.VK_SPACE:
                 if ( actor.canShoot() )
                     actor.shoot( Settings.soundOn );
                 break;
             case KeyEvent.VK_LEFT:
                 actor.left();
                 break;
             case KeyEvent.VK_RIGHT:
                 actor.right();
                 break;
             case KeyEvent.VK_UP:
                 actor.forward();
                 break;
             case KeyEvent.VK_DOWN:
                 actor.backwards();
                 break;
 
             // Releasing keys.
             case -KeyEvent.VK_LEFT:
                 actor.unleft();
                 break;
             case -KeyEvent.VK_RIGHT:
                 actor.unright();
                 break;
             case -KeyEvent.VK_UP:
                 actor.unforward();
                 break;
             case -KeyEvent.VK_DOWN:
                 actor.unbackwards();
                 break;
 
             // Special keys.
             case KeyEvent.VK_PAGE_UP:
                 actor.fullUp();
                 break;
             case KeyEvent.VK_PAGE_DOWN:
                 actor.fullDown();
                 break;
             case KeyEvent.VK_INSERT:
                 actor.fullLeft();
                 break;
             case KeyEvent.VK_DELETE:
                 actor.fullRight();
                 break;
             case KeyEvent.VK_END:
                 actor.allStop();
                 break;
             case 192:	// ~ activates berserk!
                 actor.berserk();
                 break;
             case KeyEvent.VK_HOME:
                 actor.getMissileManager().explodeAll();
                 break;
             case KeyEvent.VK_P:
                 // [PC] Not unpausable!
                 // paused = !paused;
                 break;
             case KeyEvent.VK_M:
                 toggleMusic();
                 break;
             case KeyEvent.VK_S:
                 toggleSound();
                 break;
             case KeyEvent.VK_F4:
                 toggleFullscreen();
                 break;
             case KeyEvent.VK_W:
                 warpDialog();
                 break;
             case KeyEvent.VK_A:
                 toggleAntiAliasing();
                 break;
             case KeyEvent.VK_BACK_SLASH:
                 drawScoreboard = true;
                 break;
             default:
                 drawScoreboard = false;
                 break;
         }
         repaint();
     }
 
     /**
      * Toggles music on/off, and writes the setting to the background.
      * @author Phillip Cohen
      * @since November 15 2007
      */
     public void toggleMusic()
     {
         writeOnBackground( "Music " + ( Sound.toggleMusic() ? " on." : "off." ), getWidth() / 2 - 10, getHeight() / 2, Color.green );
     }
 
     /**
      * Toggles sound on/off, and writes the setting to the background.
      * @author Phillip Cohen
      * @since November 15 2007
      */
     public void toggleSound()
     {
         writeOnBackground( "Sound " + ( Sound.toggleSound() ? " on." : "off." ), getWidth() / 2 - 10, getHeight() / 2, Color.green );
     }
 
     /**
      * Toggles fullscreen on/off. This is rather problematic.
      * @author Phillip Cohen
      * @since December 11 2007
      */
     public void toggleFullscreen()
     {
         Settings.useFullscreen = !Settings.useFullscreen;
         updateFullscreen();
     }
 
     /**
      * Toggles antialiasing on/off, and writes the setting to the background.
      * @author Phillip Cohen
      * @since December 15 2007
      */
     public void toggleAntiAliasing()
     {
         Settings.antiAlias = !Settings.antiAlias;
         updateAntiAliasing();
         writeOnBackground( "Antialiasing " + ( Settings.antiAlias ? " on." : "off." ), getWidth() / 2 - 10, getHeight() / 2, Color.green );
     }
 
     /**
      * Sets the anti-aliasing mode based on the setting.
      * @author Phillip Cohen
      * @since December 15 2007
      */
     public void updateAntiAliasing()
     {
         // Update the rendering settings.
         if ( Settings.antiAlias )
             ( (Graphics2D) getGBuff() ).setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
         else
             ( (Graphics2D) getGBuff() ).setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF );
     }
 
     /**
      * Sets the fullscreen/window mode based on the setting.
      * @author Phillip Cohen
      * @since December 11 2007
      */
     public void updateFullscreen()
     {
         GraphicsDevice graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
 
         // Set fullscreen mode.
         if ( Settings.useFullscreen )
         {
             // Don't change anything if we're already in fullscreen mode.
             if ( graphicsDevice.getFullScreenWindow() != this )
             {
                 setVisible( false );
                 dispose();
                 setUndecorated( true );
                 setSize( graphicsDevice.getDisplayMode().getWidth(), graphicsDevice.getDisplayMode().getHeight() );
                 graphicsDevice.setFullScreenWindow( this );
 
                 // Hide the cursor.
                 Image cursorImage = Toolkit.getDefaultToolkit().getImage( "xparent.gif" );
                 Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor( cursorImage, new Point( 0, 0 ), "" );
                 setCursor( blankCursor );
 
                 // Re-create the background.
                 if ( background != null )
                     drawBackground();
             }
         }
         // Set windowed mode.
         else
         {
             // Don't change anything if we're already in windowed mode.
             if ( ( getSize().width != WINDOW_WIDTH ) || ( getSize().height != WINDOW_HEIGHT ) )
             {
                 setVisible( false );
                 dispose();
                 setUndecorated( false );
                 setSize( WINDOW_WIDTH, WINDOW_HEIGHT );
                 graphicsDevice.setFullScreenWindow( null );
 
                 // Show the cursor.
                 setCursor( new Cursor( Cursor.DEFAULT_CURSOR ) );
 
                 // Re-create the background.
                 if ( background != null )
                     drawBackground();
             }
         }
         setVisible( true );
     }
 
     /**
      * Used to check if we're in fullscreen (regardless of the setting).
      * @return Whether the frame is in fullscreen mode.
      * @author Phillip Cohen
      * @since December 15 2007
      */
     public boolean isFullscreen()
     {
         return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getFullScreenWindow() == this;
     }
 
     /**
      * Sets the current time of the other player.
      * @param step The new value for the other player's current time.
      * @author Andy Kooiman
      * @since Classic
      */
     public void setOtherPlayerTimeStep( int step )
     {
         otherPlayerTimeStep = step;
     }
 
     /**
      * Included to prevent the clearing of the screen between repaints.
      * @param g The <code>Graphics</code> context of the screen.
      * @author Andy Kooiman
      * @since Classic
      */
     @Override
     public void update( Graphics g )
     {
         paint( g );
     }
 
     /**
      * Undoes all changes that <code>BonusAsteroid</code>s may have caused.
      * @author Andy Kooiman, Phillip Cohen
      * @since Classic
      */
     private void restoreBonusValues()
     {
         for ( Ship ship : players )
         {
             ship.getMissileManager().setHugeBlastProb( 5 );
             ship.getMissileManager().setHugeBlastSize( 50 );
             ship.getMissileManager().setProbPop( 2000 );
             ship.getMissileManager().setPopQuantity( 5 );
             ship.getMissileManager().setSpeed( 10 );
             ship.setMaxShots( 10 );
             ship.getMissileManager().setIntervalShoot( 15 );
         }
     }
 
     /**
      * Returns the current level.
      * @return The current level.
      * @author Andy Kooiman
      * @since Classic
      */
     public static int getLevel()
     {
         return level;
     }
 
     /**
      * Sleeps the game for the specified time.
      * @param milis How many milliseconds to sleep for.
      * @author Andy Kooiman
      * @since Classic
      */
     public static void safeSleep( int milis )
     {
         try
         {
             Thread.sleep( milis );
         }
         catch ( InterruptedException e )
         {
         }
     }
 
     /**
      * Writes a message onto the background.
      * @param message The message to be written.
      * @param x The x coordinate where the message should be drawn.
      * @param y The y coordinate where the message should be drawn.
      * @param col The <code>Color</code> in which the message should be drawn.
      * @author Andy Kooiman
      * @since Classic
      */
     public void writeOnBackground( String message, int x, int y, Color col )
     {
         Graphics gBack = background.getGraphics();
         gBack.setColor( col );
         gBack.setFont( new Font( "Tahoma", Font.BOLD, 9 ) );
         gBack.drawString( message, x, y );
     }
 
     /**
      * Draws a circle with center at coordinates with given radius in given color
      * @param col The <code>Color</code> in which the circle will be drawn
      * @param x The x coordinate of the center
      * @param y The y coordinate of the center
      * @param radius The radius of the circle
      * @since December 15, 2007
      */
     public void drawCircle( Color col, int x, int y, int radius )
     {
         gBuff.setColor( col );
         gBuff.drawOval( x - radius / 2 - 1, y - radius / 2 - 1, radius * 2, radius * 2 );
     }
 
     /**
      * Draws a line from one coordinate to another in a given color
      * @param col The <code>Color</code> in wihch the circle will be drawn
      * @param x1 The first x coordinate
      * @param y1 The first y coordinate
      * @param x2 The second x coordinate
      * @param y2 The second y coordinate
      * @since December 15, 2007
      */
     public void drawLine( Color col, int x1, int y1, int x2, int y2 )
     {
         gBuff.setColor( col );
         gBuff.drawLine( x1, y1, x2, y2 );
     }
 
     /**
      * Draws a circle with center at coordinates with given radius in given color
      * @param col The <code>Color</code> in which the circle will be drawn
      * @param x The x coordinate of the center
      * @param y The y coordinate of the center
      * @param radius The radius of the circle
      * @since December 15, 2007
      */
     public void fillCircle( Color col, int x, int y, int radius )
     {
         gBuff.setColor( col );
         gBuff.fillOval( x - radius / 2, y - radius / 2, radius * 2, radius * 2 );
     }
 
     /**
      * Draws a polygon in one color with a background of another color
      * @param p The <code>Polygon</code> to be drawn
      * @param fill The <code>Color</code> in which the <code>Polygon</code> will be drawn
      * @param outline The <code>Color</code> of the outline
      * @since December 15, 2007
      */
     public void drawPolygon( Color fill, Color outline, Polygon p )
     {
         gBuff.setColor( fill );
         gBuff.fillPolygon( p );
         gBuff.setColor( outline );
         gBuff.drawPolygon( p );
     }
 
     /**
      * Draws a circle with center at given point with given radius in one color, with an outline of another color
      * @param fill The <code>Color</code> in which the circle will be drawn
      * @param outline The <code>Color</code> of the outline
      * @param x The x coordinate of the center
      * @param y The y coordinate of the center
      * @param radius The radius
      * @since December 15, 2007
      */
     public void drawOutlinedCircle( Color fill, Color outline, int x, int y, int radius )
     {
         fillCircle( fill, x - radius / 2, y - radius / 2, radius );
         drawCircle( outline, x - radius / 2, y - radius / 2, radius );
     }
 
     /**
      * Returns the game's <code>ActionManager</code>.
      * @return The <code>ActionManager</code> of <code>this</code>.
      * @author Andy Kooiman
      * @since Classic
      */
     public ActionManager actionManager()
     {
         return actionManager;
     }
 
     /**
      * Returns the <code>Graphics</code> Context of the offscreen <code>Image</code> used for double buffering
      * @return The <code>Graphics</code> Context of the offscreen <code>Image</code> used for double buffering
      * @since classic
      */
     public static Graphics getGBuff()
     {
         return gBuff;
     }
 
     /**
      * Returns whether this computer is the first player.
      * This occurs in singleplayer or when hosting.
      * @return Whether the local computer is player #1.
      * @author Andy Kooiman, Phillip Cohen
      * @since Classic
      */
     public static boolean isPlayerOne()
     {
         return localPlayer == 0;
     }
 
     /**
      * Sets a new high score name.
      * @param name The name of the player who has the high score.
      * @author Andy Kooiman
      * @since Classic
      */
     public void setHighScore( String name )
     {
         highScoreName = name;
     }
 
     /**
      * Inserts comma seperators at each grouping, up to 10^34.
      * @param number The number to be formatted.
      * @author Phillip Cohen
      * @return The formatted string.
      * @since December 15 2007
      */
     public static String insertThousandCommas( int number )
     {
         return new DecimalFormat( "#,###,###,###,###,###,###,###,###,###,###,###" ).format( number );
     }
 
     /**
      * A simple handler for the frame's window buttons.
      * @author Phillip Cohen, Andy Kooiman
      * @since December 15 2007
      */
     private static class CloseAdapter extends WindowAdapter
     {
         /**
          * Invoked when a window has been closed.
          * @param e See <code>WindowListener</code>.
          */
         @Override
         public void windowClosing( WindowEvent e )
         {
             try
             {
                 // Tell the other players that we're leaving.
                 AsteroidsServer.send( "exit" );
                 AsteroidsServer.dispose();
             }
             catch ( NullPointerException ex )
             {
             }
             Running.quit();
         }
     }
 
     private static class ScoreboardColumn
     {
         private int x;
 
         private String title;
 
         public String getTitle()
         {
             return title;
         }
 
         public int getX()
         {
             return x;
         }
 
         public ScoreboardColumn( int x, String title )
         {
             this.x = x;
             this.title = title;
         }
     }
 }
