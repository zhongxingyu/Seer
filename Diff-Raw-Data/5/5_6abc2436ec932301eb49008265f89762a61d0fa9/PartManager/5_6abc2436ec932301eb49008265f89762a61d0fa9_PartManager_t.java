 import java.awt.*;
 import java.awt.event.*;
 import java.util.*;
 import javax.swing.*;
 
 public class PartManager extends JPanel {
 	private PartsClient myClient;
 	private JLabel pName;
 	private JLabel pNumber;
 	private JLabel pInfo;
 	private JLabel pEdit;
 	private JLabel pEdit2;
 	private JTextField tName;
 	private JTextField tNumber;
 	private JTextField tInfo;
 	private JTextField tEdit;
 	private JButton create;
 	private JButton change;
 	private JButton delete;
 	private JScrollPane scroll;
 	private JPanel parts;
 	private JLabel msg;
 	
 	public PartManager( PartsClient pc ){
 		myClient = pc;
 		
 		pName = new JLabel("Part Name: ");
 		pNumber = new JLabel("Part Number: ");
 		pInfo = new JLabel("Part Info: ");
 		pEdit = new JLabel("Number of part to be changed/deleted: ");
 		pEdit2 = new JLabel("Part will be changed to new part above");
 		tName = new JTextField(10);
 		tNumber = new JTextField(10);
 		tInfo = new JTextField(10);
 		tEdit = new JTextField(10);
 		create = new JButton("Create");
 		change = new JButton("Change");
 		delete = new JButton("Delete");
 		msg = new JLabel("");
 		
 		//jscrollpane for list of parts
 		parts = new JPanel();
 		parts.setLayout( new BoxLayout( parts, BoxLayout.Y_AXIS ) );
 		scroll = new JScrollPane(parts);
 		
 		//layout GUI
 		setLayout( new GridBagLayout() );
 		GridBagConstraints c = new GridBagConstraints();
 		
 		//parts scroll pane
 		c.fill = c.BOTH;
 		c.gridx = 0;
 		c.gridy = 0;
 		c.weightx = 1;
 		c.weighty = 0;
 		c.gridwidth = 2;
 		c.gridheight = 10;
 		add( scroll, c );
 		
 		
 		//adding parts
 		c.fill = c.HORIZONTAL;
 		c.insets = new Insets(20,10,0,0);
 		c.gridx = 2;
 		c.gridy = 0;
 		c.weightx = 0;
 		c.gridwidth = 1;
 		c.gridheight = 1;
 		add( pName, c );
 		
 		c.gridx = 2;
 		c.gridy = 1;
 		add( pNumber, c );
 		
 		c.gridx = 2;
 		c.gridy = 2;
 		add( pInfo, c );
 		
 		c.gridx = 3;
 		c.gridy = 0;
 		add( tName, c );
 		
 		c.gridx = 3;
 		c.gridy = 1;
 		add( tNumber, c );
 		
 		c.gridx = 3;
 		c.gridy = 2;
 		add( tInfo, c );
 		
 		c.gridx = 4;
 		c.gridy = 1;
 		add( create, c );
 		
 		
 		//changing/deleting parts
 		c.gridx = 2;
 		c.gridy = 4;
 		add( pEdit, c );
 		
 		c.gridx = 3;
 		c.gridy = 3;
 		add( pEdit2, c );
 		
 		c.gridx = 3;
 		c.gridy = 4;
 		add( tEdit, c );
 		
 		c.gridheight = 1;
 		c.gridx = 4;
 		c.gridy = 3;
 		add( change, c );
 		
 		c.gridx = 4;
 		c.gridy = 4;
 		add( delete, c );
 		
 		//messages
 		c.gridx = 2;
 		c.gridy = 5;
 		c.gridwidth = 3;
 		add( msg, c );
 		
 		//action listeners for buttons
 		create.addActionListener( new ActionListener() {
 			public void actionPerformed( ActionEvent e ){
 				if( !tName.getText().equals("") && !tInfo.getText().equals("") && !tNumber.getText().equals("") ) {
 					try{
 						//add part to server
 						myClient.getCom().write( new NewPartMsg( new Part( tName.getText(), tInfo.getText(), (int)Integer.parseInt( tNumber.getText() ) ) ) );
 						
 						//display parts list
 						requestParts();
 					} catch (NumberFormatException nfe) {
 						msg.setText( "Please enter a number for Part Number" );
 					}
 				}
 				else {
 					msg.setText( "Please enter all part information" );
 				}
 			}
 		});
 		
 		change.addActionListener( new ActionListener() {
 			public void actionPerformed( ActionEvent e ){
 				if( !tName.getText().equals("") && !tInfo.getText().equals("") && !tNumber.getText().equals("") && !tEdit.getText().equals("") ) {
 					try{
 						//replace part number X with new part
						myClient.getCom().write( new ChangePartMsg( (int)Integer.parseInt( tEdit.getText() ), new Part( tName.getText(), tInfo.getText(), (int)Integer.parseInt( tNumber.getText() ) ) ) );
 
 						//display parts list
 						requestParts();
 					} catch (NumberFormatException nfe) {
 						msg.setText( "Please enter a number for part to be changed" );
 					}
 				}
 				else if( tEdit.getText().equals("") ) {
 					msg.setText( "Please enter part number of part to be changed." );
 				}
 				else {
 					msg.setText( "Please enter all part information" );
 				}
 			}
 		});
 		
 		delete.addActionListener( new ActionListener() {
 			public void actionPerformed( ActionEvent e ){
 				if( !tEdit.getText().equals("") ) {
 					try {
 						//delete the part on the server
						myClient.getCom().write( new DeletePartMsg( (int)Integer.parseInt( tEdit.getText() ) ) );
 	
 						//display parts list
 						requestParts();
 					} catch (NumberFormatException nfe) {
 						msg.setText( "Please enter a number for part to be deleted" );
 					}
 				}
 				else {
 					msg.setText( "Please enter part number of part to be deleted." );
 				}
 			}
 		});
 	}
 	
 	public void requestParts(){
 		//get updated parts list
 		myClient.getCom().write( new PartListMsg() );
 	}
 
 	public void displayParts(){
 		//remove current list from the panel
 		parts.removeAll();
 				
 		//add new list to panel
 		ArrayList<Part> temp = myClient.getParts();
 				
 		for( Part p : temp ){ //maybe use string builder in future?
 			parts.add( new JLabel( p.getNumber() + " - " + p.getName() + " - " + p.getDescription() ) );
 		}
 				
 		validate();
 		repaint();
 	}
 	
 	public void setMsg( String s ){
 		msg.setText(s);
 	}
 	
 }
