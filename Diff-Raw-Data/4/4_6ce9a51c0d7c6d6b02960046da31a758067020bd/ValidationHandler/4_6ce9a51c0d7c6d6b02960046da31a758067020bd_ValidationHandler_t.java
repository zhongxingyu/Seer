 package blue.hotel.gui;
 
 import javax.swing.JOptionPane;
 
 public class ValidationHandler {
 	public static boolean validate(Editor<?> editor) {
 		if (!editor.validateInput()) { 
			JOptionPane.showMessageDialog(null,
 					"Cannot save because of the following errors:\n\n" +
 			        editor.inputErrors(), 
 			        "Error",
 			        JOptionPane.ERROR_MESSAGE);
 			return false;
 		}
 		
 		return true;
 	}
 }
