 import java.util.List;
 import java.util.ArrayList;
 import javax.swing.JOptionPane;
 /**
 * Graphical user interface for decision support application.
 * 
 * @author Josh Gillham
 * @version 10-29-12
 */
 public class GraphicalUserInterface extends UserInterfaceBase {
     /**
      * Creates a yes or no dialog displaying the message.
      * 
      * @param message is the text to display.
      * 
      * @return is an enum showing the result OR null if the user clicks the X
      * 
      * @throws NullPointerException if message is null.
      */
     public YNAnswer inputYNQuestion( String message ) { 
        if( message == null )
            throw new NullPointerException( "Message was null." );
         int result = JOptionPane.showConfirmDialog( 
             null, message, "", JOptionPane.YES_NO_OPTION
         );
         if ( result == 1 )
             return YNAnswer.No;
         else if ( result == 0 )
             return YNAnswer.Yes;
         return null;
     }
     
     /**
      * Creates an input dialog displaying the message.
      * 
      * @param message is the text to display.
      * 
      * @return the user input OR null if the user clicked cancel or the X.
      * 
      * @throws NullPointerException if message is null.
      */
     public String inputQuestion( String message ) {
        if( message == null )
            throw new NullPointerException( "Message was null." );
         return JOptionPane.showInputDialog( message );
     }
     
     /**
      * Displays a dialog with the message.
      * 
      * @param message is the text to display.
      * 
      * @throws NullPointerException if message is null.
      */
     public void showMessage( String message ) { 
        if( message == null )
            throw new NullPointerException( "Message was null." );
         JOptionPane.showMessageDialog( null, message );
     }
 }
