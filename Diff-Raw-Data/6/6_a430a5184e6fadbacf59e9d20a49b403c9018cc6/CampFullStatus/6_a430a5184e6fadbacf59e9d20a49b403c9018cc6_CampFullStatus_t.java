 package package1;
 
 import javax.swing.*;
 
 import java.awt.BorderLayout;
import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.event.*;
 public class CampFullStatus extends JDialog implements ActionListener{
 
 	/**Default serial UID */
 	private static final long serialVersionUID = 1L;
 		
 	/** table for data */
 	private JTable outputTable;
 	
 	/** scroll panel for table */
 	private JScrollPane scrollPane;
 	
 	/** label to display date given */
 	private JLabel displayDate;
 	
 	/** table model */
 	private StatusModel tableModel;
 	
 	/** OK Button */
 	private JButton OKButton;
 	
 	public void checkStatus(BetterGregorianCalendar d) {
 		// Instantiates the table model with the passed in date
 		tableModel = new StatusModel(d);
 		// loads the text file that was saved off in the StatusModel
 		tableModel.loadFromText("CampStatus");
 		// sets the JTable to the StatusModel
 		outputTable = new JTable(tableModel);
 		// instantiates a new JScollPane
 		scrollPane = new JScrollPane(outputTable);
 		// Displays the date of checked status
 		displayDate = new JLabel("Camp status on: " + d.toString());
 		// JButton for OK
 		OKButton = new JButton("OK");
 		// adds action listener to OK
 		OKButton.addActionListener(this);
 		
 		//Creates a new panel
 		JPanel panel = new JPanel();
 		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
 		panel.add(displayDate);
 		panel.add(scrollPane);
 		buttonPanel.add(OKButton);
 		panel.add(buttonPanel);
 		add(panel);
 		
 		
 		
 		setSize(700,300);
 		setVisible(true);
 		
 	}
 
 	public void actionPerformed(ActionEvent event) {
 		if(event.getSource() == OKButton){
 			dispose();
 		}
 	}
 	
 	
 
 }
