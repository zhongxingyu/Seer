 /*
  * RTS
  * 
  */
 package sts;
 
 import java.awt.Color;
 import java.util.HashSet;
 import javax.swing.JOptionPane;
 import sts.game.Game;
 import sts.game.Player;
 import sts.gui.ImageHandler;
 
 /**
  *
  * @author Phillip Cohen
  */
 public class Main
 {
     /**
      * Small counters that are incremented each time <code>warning</code> or <code>fatalError</code> is called.
      */
     private static int errorCount = 0,  warningCount = 0;
 
     /**
      * @param args the command line arguments
      */
     public static void main( String[] args )
     {
         Player dummy = new Player( Color.red, "Bob" );
         ImageHandler.init();
 
         HashSet<Player> players = new HashSet<Player>();
         players.add( dummy );
         players.add( new Player( Color.blue, "Fantasmal" ) );
         players.add( new Player( Color.GREEN, "Third" ) );
        players.add( new Player( Color.orange, "Forth" ) );
         players.add( new Player( Color.cyan, "Fifety" ) );
         
 
 
         Local.setUp( new Game( players ), dummy );
     }
 
     public static void quit()
     {
         System.exit( 0 );
     }
 
     /**
      * Logs a message to <code>println</code> and eventually the game window.
      */
     public static void log( String message )
     {
         System.out.println( message );
     }
 
     /**
      * Logs a message to <code>println</code> and eventually the game window.
      */
     public static void log( String message, int priority )
     {
         System.out.println( message );
     }
 
     /**
      * Logs a warning to the console and bumps the warningCount.
      */
     public static void warning( String message )
     {
         log( "WARNING: " + message, 3 );
         warningCount++;
     }
 
     /**
      * Logs a warning and exception to the console and bumps the warningCount.
      */
     public static void warning( String message, Throwable t )
     {
         log( "WARNING: " + message, 3 );
         t.printStackTrace();
         warningCount++;
     }
 
     /**
      * Shows a JOptionPane error dialog with the message text, logs it, and quits.
      */
     public static void fatalError( String message )
     {
         JOptionPane.showMessageDialog( null, message, "Disasteroids: Very Fatal Error", JOptionPane.ERROR_MESSAGE );
         System.out.println( "FATAL ERROR: " + message );
         errorCount++;
         quit();
     }
 
     /**
      * Shows a JOptionPane error dialog with the message text, logs it and the exception's stack trace, and quits.
      */
     public static void fatalError( String message, Exception e )
     {
         e.printStackTrace();
         fatalError( message + "\n\nWith exception: " + e.getLocalizedMessage() );
     }
 }
