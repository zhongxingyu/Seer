 /**
  * 
  */
 package edu.wpi.cs.wpisuitetng.modules.RequirementManager.view.Requirements;
 
 import java.awt.Color;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.text.Format;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.ListIterator;
 
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 import javax.swing.SpringLayout;
 import javax.swing.border.Border;
 
 import edu.wpi.cs.wpisuitetng.modules.RequirementManager.models.characteristics.Note;
 import edu.wpi.cs.wpisuitetng.modules.RequirementManager.models.characteristics.NoteList;
 
 /**
  * Class to display a note inside the EditRequirementPanel
  * @author Brian Froehlich
  *
  */
 public class NotePanel extends JPanel {
 	
 	/**
 	 * Basic constructor for a NotePanel
 	 * @param note Note to be displayed
 	 */
 	public NotePanel(Note note)
 	{
 		this.setBorder(BorderFactory.createLineBorder(Color.black)); //Set note border
 		
 		// Create a text area containing the note's message
 		JTextArea message = new JTextArea(note.getMessage());
 		message.setEditable(false); // Do not allow to be edited
 		
 		// Give the message a black border with 2px padding inside
 		Border b = BorderFactory.createCompoundBorder(
 				BorderFactory.createLineBorder(Color.black), 
 	            BorderFactory.createEmptyBorder(2, 2, 2, 2));
 		message.setBorder(b);
 
 		// Get user name of user who created note
 		String user = note.getUser();
 		
 		// Get the date the note was added, then format it to display correctly
 		Date date = new Date(note.getTimestamp());
 		Format format = new SimpleDateFormat("MMMMM d, yyyy 'at' hh:mm:ss aaa");
 		String noteDate = format.format(date).toString();
 		JLabel noteInfo = new JLabel(user + " on " + noteDate);
 		
 		// Create a layout manager for this note panel
 		this.setLayout(new GridBagLayout());
 		GridBagConstraints noteConstraints = new GridBagConstraints();
 		
 		noteConstraints.fill = GridBagConstraints.HORIZONTAL; // Fill elements horizontally
 		noteConstraints.gridy = 0; //Row 0
 		noteConstraints.weightx = 1; //Fill the width
 		noteConstraints.insets = new Insets(2,2,2,2); //2px margin
 		this.add(message, noteConstraints); // Add message to notePanel
 		
 		noteConstraints.anchor = GridBagConstraints.SOUTHEAST; // Display info in btm-right
 		noteConstraints.fill = GridBagConstraints.NONE; // Don't fill elements
 		noteConstraints.gridy = 1; //Row 1
 		this.add(noteInfo, noteConstraints); // Add info to notePanel
 	}
 	
 	public static JPanel createList(NoteList list)
 	{
 		// Create a panel to hold all of the notes
 		JPanel panel = new JPanel();
 		panel.setBackground(Color.WHITE); // Background color is white
 		panel.setLayout(new GridBagLayout());
 		GridBagConstraints c = new GridBagConstraints();
 		c.gridy = GridBagConstraints.RELATIVE;
 		c.anchor = GridBagConstraints.NORTH;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.weightx = 1;//Fill horizontally
 		c.gridy = 0; //Row 0
 		c.insets = new Insets(5,5,5,5);
 		
 		ListIterator<Note> itt = list.getIterator(0);
 		while(itt.hasNext())
 		{
 			//Create a new NotePanel for each Note and add it to the panel
 			panel.add(new NotePanel(itt.next()),c);
 			c.gridy++; //Next Row
 		}
 		
 		//Create a dummy panel to take up space at the bottom
 		c.weighty = 1;
 		JPanel dummy = new JPanel();
 		dummy.setBackground(Color.WHITE);
 		panel.add(dummy,c);
 		
 		return panel;
 	}
 }
