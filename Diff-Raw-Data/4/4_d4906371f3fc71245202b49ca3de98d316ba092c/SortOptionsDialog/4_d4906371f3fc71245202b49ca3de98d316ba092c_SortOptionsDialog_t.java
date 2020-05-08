 package jfmi.gui;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 
 /** A SortOptionsDialog provides a JDialog wrapper frame for a SortOptionsBox.
   It acts as an ActionListener for a SortOptionsBox, and provides methods for
   clients to display the options to the user, and determine what action the
   user took (e.g. changed an option selection).
   */
 public class SortOptionsDialog extends JDialog implements ActionListener {
 	
 	// PRIVATE Instance Fields
 	SortOptionsBox optionsBox;
 
 
 	//************************************************************
 	// PUBLIC Instance Methods
 	//************************************************************
 
 	/** Constructs a new SortOptionsDialog with the specified parent frame
 	  and sort options box.
 	  @param parent this dialog's parent JFrame
 	  @param opBox the SortOptionsBox this instance wraps
 	  @throws NullPointerException if opBox is null
 	  */
 	public SortOptionsDialog(JFrame parent, SortOptionsBox opBox)
 	{
		super(parent, "Sorting Options", true);
 		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
 		setVisible(false);
 
 		if (opBox == null) {
 			throw new NullPointerException("opBox cannot be null");
 		}
 
 		optionsBox = opBox;
 	}
 
 	/** Gets the value of the currently selected field sorting option.
 	  @return the selected field; null if empty set or none selected
 	  */
 	public String getSelectedField()
 	{
 		return optionsBox.getSelectedField();
 	}
 
 	/** Gets the value of the currently selected order sorting option.
 	  @return the selected order; null if empty set or none selected
 	  */
 	public String getSelectedOrder()
 	{
 		return optionsBox.getSelectedOrder();
 	}
 	
 
 	//************************************************************
 	// IMPLEMENTATION of ActionListener 
 	//************************************************************
 
 	public void actionPerformed(ActionEvent e)
 	{
 
 	}
}
 
