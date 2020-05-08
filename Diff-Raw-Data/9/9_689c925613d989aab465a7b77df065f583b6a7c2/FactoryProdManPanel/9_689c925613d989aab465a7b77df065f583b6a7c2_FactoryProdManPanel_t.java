 /*
 
 author: Joey Huang
 Last edited: 11/8/12 7:30pm
 */
 package factory.swing;
 import java.util.*;
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 import factory.managers.*;
 
 
 public class FactoryProdManPanel extends JPanel implements ActionListener {
 	JComboBox kitNameBox; // contain String names of saved kit configurations
 	JSpinner spinner; // quantity of kits to produce
 	JButton submitButton;
 	JTextArea messageBox; // submission confirmations
 	FactoryProductionManager factoryProductionManager;
 	
 	public FactoryProdManPanel() { 
 		kitNameBox = new JComboBox();
 		kitNameBox.setPreferredSize(new Dimension(225,25));
 		
 		setLayout(new GridBagLayout());
 		GridBagConstraints c = new GridBagConstraints();
 		c.gridy = 1;
 		c.gridx = 1;
 		c.gridwidth = 4;
 		c.gridheight = 1;
 		c.insets = new Insets(0,0,25,0); 
 		add(new JLabel("Factory Production Manager"),c);
 		
 		
 		c.insets = new Insets(10,0,10,0);
 		c.gridy = 2;
 		c.anchor = GridBagConstraints.LINE_START;
 		add(new JLabel("Submit New Batch Order:"),c);
 		
 		c.gridy = 3;
 		c.anchor = GridBagConstraints.CENTER;
 		add(kitNameBox,c);
 
 		c.anchor = GridBagConstraints.LINE_END;
 		c.gridy = 4;
 		c.gridx = 2;
 		c.gridwidth = 1;
 		add(new JLabel("Quantity"),c);
 		
 		c.gridx = 4;
 		SpinnerNumberModel qntyModel = new SpinnerNumberModel(1,1,500,1);
 		spinner = new JSpinner(qntyModel);
 		add(spinner,c);
 		
 		c.gridy = 5;
 		c.gridx = 4;
 		submitButton = new JButton("Submit");
 		submitButton.addActionListener(this);
 		add(submitButton,c);
 		
 		c.gridy = 6;
 		c.gridx = 1;
 		c.gridwidth = 4;
 		messageBox = new JTextArea("System Messages\n",10,20);
 		add(new JScrollPane(messageBox),c);
 				
 	}
 		
 	public void actionPerformed(ActionEvent ae) {
 		
 		if (ae.getSource().equals(submitButton)) {		// print messages to be displayed in messageBox
 			if (kitNameBox.getSelectedItem() == null)
 				messageBox.append("No kit selected.\n");
 			else {
				 factoryProductionManager.sendOrder((String)kitNameBox.getSelectedItem(),(String)spinner.getValue());
 					
 			messageBox.append("Order Submitted.\n     Details: " + spinner.getValue() + " units of " + (String)kitNameBox.getSelectedItem() + "\n" );
 			}
 		}
 	}
 	
 	public void addKit(String kitName) {	//add new kit name (String) to Jcombobox list - received from kit manager
 		kitNameBox.addItem(kitName);	
 		((JComboBox) kitNameBox.getItemAt(kitNameBox.getItemCount()-1)).addActionListener(this);
 		kitNameBox.setSelectedIndex(0);
 	}
 	
 	public void removeKit(String kitName) { // remove kit from list - received from kit manager
 		kitNameBox.removeItem(kitName);	// should have been validated elsewhere
 	}
 
 	public void setManager(FactoryProductionManager fpm) {
 		factoryProductionManager = fpm;
 	}
 
 }
