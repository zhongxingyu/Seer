 package blue.hotel.gui;
 
 import javax.swing.JOptionPane;
 
 public class ValidationHandler {
 	public static boolean validate(Editor<?> editor) {
 		if (!editor.validateInput()) { 
			JOptionPane.showConfirmDialog(null,
 					"Cannot save because of the following errors:\n\n" +
 			        editor.inputErrors(), 
 			        "Error",
			        JOptionPane.OK_OPTION,
 			        JOptionPane.ERROR_MESSAGE);
 			return false;
 		}
 		
 		return true;
 	}
 }
