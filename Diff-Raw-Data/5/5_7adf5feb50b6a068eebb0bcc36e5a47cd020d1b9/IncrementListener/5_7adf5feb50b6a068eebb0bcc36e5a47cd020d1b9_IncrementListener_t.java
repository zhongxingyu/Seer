 package App.listener;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.regex.Pattern;
 
 import javax.swing.JFormattedTextField;
 
 /**
  * This class validates the text in the fields on the new player info screen,
  * and allows them to be incremented or decremented. Regex is used for safely
  * checking the content of the string before attempting to increment. Buttons
  * that use this cannot decrement past zero, and will reset to zero if the
  * text contains invalid characters. Also, fields can not be incremented past 16.
  * @author Andrew Wilder
  */
 
 public class IncrementListener implements ActionListener {
 	
 	private JFormattedTextField theField;
 	private boolean IncType;
 	
 	public static boolean INC = true, DEC = false;
 	
 	public IncrementListener(JFormattedTextField theField, boolean IncType) {
 		this.theField = theField;
 		this.IncType = IncType;
 	}
 
 	public void actionPerformed(ActionEvent e) {
         if(Pattern.matches("[0-9]+", theField.getText())) {
 			int n = Integer.parseInt(theField.getText());
            theField.setText(n + (IncType ? (n < 16 ? 1 : 0) : (n > 0 ? -1 : 0)) + "");
 		} else {
 			theField.setText("0");
 		}
 	}
 }
