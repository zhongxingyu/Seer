 package gui.common;
 
 import java.awt.event.*;
 
 import javax.swing.*;
 
 import gui.main.*;
 
 /**
  * Used to display modal dialog boxes.
  */
 @SuppressWarnings("serial")
 public class DialogBox extends JDialog {
 
 	/**
 	 * Owning GUI object.
 	 */
 	private GUI _parent;
 	
 	/**
 	 * Constructor.
 	 * 
 	 * @param parent Owning GUI object.
 	 * @param title Title of dialog box window.
 	 * 
 	 * {@pre parent != null, title != null}
 	 * 
 	 * {@post DialogBox has been initialized with the specified
 	 * parent and title.}
 	 */
 	public DialogBox(GUI parent, String title) {
 		super(parent, title, true);
 		
 		_parent = parent;
 	}
 	
 	/**
 	 * Displays the specified DialogView in a dialog box.
 	 * 
 	 * @param content The view defining the content of the dialog box.
 	 * @param resizable Flag indicating whether or not the dialog box
 	 * 	should be resizable.
 	 * 
 	 * {@pre content != null}
 	 * 
 	 * {@post The specified DialogView has been displayed in a
 	 * modal dialog box on the screen.  The dialog box is resizable
 	 * iff resizable is true.}
 	 */
 	public void display(DialogView content, boolean resizable) {		
 		setResizable(resizable);
 		addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent evt) {
 				dispose();
 			}		
 		});
 		setContentPane(content);		
 		pack();
 		setLocationRelativeTo(_parent);
		setVisible(true);		
 	}
 
 }
 
