 package ccasola.man2oh.view;
 
 import java.awt.BorderLayout;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSlider;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.ScrollPaneConstants;
 
 import ccasola.man2oh.model.ManParser;
 import ccasola.man2oh.model.ManParser.HELP_LEVEL;
 import ccasola.man2oh.model.UnknownEntryException;
 
 /**
  * Panel to display the lookup form, and then the man page text
  */
 @SuppressWarnings("serial")
 public class ManPage extends JPanel implements ActionListener, ISliderUpdated {
 
 	/** The title of the man entry */
 	String entryTitle = "";
 	
 	/** The text field for looking up help items */
 	final JTextField txtLookup;
 	
 	/** The find button */
 	final JButton btnFind;
 	
 	/** The text area for displaying the help text */
 	final JTextArea helpContents;
 	
 	/** A scroll pane for the helpContents text area */
 	final JScrollPane helpContentsScrollPane;
 	
 	/** The current help level being displayed */
 	HELP_LEVEL helpLevel;
 	
 	/** The current help text being displayed */
 	String helpText = "";
 	
 	/** The main application window */
 	final ManFrame view;
 	
 	/**
 	 * Constructs a ManPage
 	 * @param mainPanel the main application window
 	 */
 	public ManPage(ManFrame mainPanel) {
 		this.view = mainPanel;
 		
 		// Construct and configure the help content text area
 		this.helpContents = new JTextArea();
 		this.helpContents.setWrapStyleWord(true);
 		this.helpContents.setEditable(false);
 		this.helpContentsScrollPane = new JScrollPane(helpContents);
 		this.helpContentsScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 		
 		// Construct the lookup text field
 		this.txtLookup = new JTextField(20);
 		txtLookup.addActionListener(this);
 		
 		// Construct the find button
 		this.btnFind = new JButton("Find");
 		btnFind.addActionListener(this);
 		
 		// Set the layout manager of the panel
 		this.setLayout(new BorderLayout());
 		
 		// Add lookup controls to the panel
 		JPanel lookupPanel = new JPanel();
 		lookupPanel.setLayout(new FlowLayout());
 		lookupPanel.add(new JLabel("Enter item name: "));
 		lookupPanel.add(txtLookup);
 		lookupPanel.add(btnFind);
 		this.add(lookupPanel, BorderLayout.PAGE_START);
 		
 		// The default help level is Topic
 		this.helpLevel = HELP_LEVEL.TOPIC;
 	}
 
 	/**
 	 * This method is called when the find button is pressed or when
 	 * the lookup text field has focus and the enter key is pressed.
 	 * 
 	 * This method is required by the ActionListener interface
 	 */
 	@Override
 	public void actionPerformed(ActionEvent ae) {
 		if (txtLookup.getText().length() < 1) { // the user did not enter anything in the lookup field
 			JOptionPane.showMessageDialog(this, "You must enter the name of an item to lookup", "Invalid Item Name", JOptionPane.WARNING_MESSAGE);
 			txtLookup.requestFocus();
 		}
 		else {
 			try {
 				// Get the new help text
 				helpText = ManParser.getInstance().getEntry(txtLookup.getText(), HELP_LEVEL.TOPIC);
 				
 				// Update the man page viewer
 				this.entryTitle = txtLookup.getText();
 				this.helpContents.setText(helpText);
 				this.removeAll();
 				this.add(helpContentsScrollPane, BorderLayout.CENTER);
 				this.validate();
 				this.repaint();
 				
 				// Update the name of the tab
 				((TabComponent)view.getTabPanel().getTabComponentAt(view.getTabPanel().getSelectedIndex())).setTitle(entryTitle);
 			}
 			catch (UnknownEntryException e) { // the man entry was not found
 				JOptionPane.showMessageDialog(this, "The item you entered was not found. Please try again.", "Item Not Found", JOptionPane.INFORMATION_MESSAGE);
 				txtLookup.setText("");
 				txtLookup.requestFocus();
 			}
 		}
 	}
 
 	/**
 	 * This method is called to notify this panel that the value of the
 	 * help level slider has changed.
 	 * 
 	 * This method is required by the ISliderUpdated interface
 	 */
 	@Override
 	public void sliderChanged(JSlider slider) {
 		// Update the help level of this panel to reflect the value of the slider
 		switch(slider.getValue()) {
 		case 1:
 			helpLevel = HELP_LEVEL.TOPIC;
 			break;
 		case 2:
 			helpLevel = HELP_LEVEL.SUMMARY;
 			break;
 		case 3:
 			helpLevel = HELP_LEVEL.DETAIL;
 			break;
 		}
 		
 		// Get the new help text for the current help level
 		try {
 			helpText = ManParser.getInstance().getEntry(entryTitle, helpLevel);
 			helpContents.setText(helpText);
 			this.validate();
 			this.repaint();
 		} catch (UnknownEntryException e) {
			// if this exception is thrown something bad happened,
			// since we already know this man item exists
			System.err.println("A fatal error occurred when switching the help level.");
			System.exit(1);
 		}
 	}
 	
 	/**
 	 * Returns the lookup field
 	 * @return the lookup field
 	 */
 	public JTextField getLookupField() {
 		return txtLookup;
 	}
 	
 	/**
 	 * Returns the current help level of this man page
 	 * @return the current help level of this man page
 	 */
 	public HELP_LEVEL getHelpLevel() {
 		return helpLevel;
 	}
 }
