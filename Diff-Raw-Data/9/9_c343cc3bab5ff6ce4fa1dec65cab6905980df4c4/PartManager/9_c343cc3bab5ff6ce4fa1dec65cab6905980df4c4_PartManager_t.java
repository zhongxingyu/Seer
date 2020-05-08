 import java.awt.*;
 import java.awt.event.*;
 import java.util.*;
 import javax.swing.*;
 
 @SuppressWarnings("serial")
 public class PartManager extends JPanel {
 	/** PartsClient variable which will sent Msg classes to the server when a button is pressed */
 	private PartsClient myClient;
 	/** print text in a label "part name:" */
 	private JLabel pName;
 	/** print text in a label "part number:" */
 	private JLabel pNumber;
 	/** print text in a label "part info:" */
 	private JLabel pInfo;
 	/** print text in a label "part image:" */
 	private JLabel pImage;
 	/** print text in a label "Number of part to be changed/deleted" */
 	private JLabel pEdit;
 	/** print text in a label "Part will be changed to new part above" */
 	private JLabel pEdit2;
 	/** textfield for prompting part name */
 	private JTextField tName;
 	/** textfield for prompting part number */
 	private JTextField tNumber;
 	/** textfield for prompting part description */
 	private JTextField tInfo;
 	/** textfield for prompting the number of part he wants to change */
 	private JTextField tEdit;
 	/** create button to create a part */
 	private JButton create;
 	/** change button to change a part */
 	private JButton change;
 	/** delete button to delete a part */
 	private JButton delete;
 	/** scroll pane that stored parts */
 	private JScrollPane scroll;
 	/** panel that contains all of the available parts */
 	private JPanel parts;
 	/** print error message */
 	private JLabel msg;
 	/** JComboBox for selecting images */
 	private JComboBox imageCB;
 	/** Array to link indices in the parts combobox to their ImageEnum */
 	private ArrayList<Painter.ImageEnum> imgEnumList;
 
 	
 	/** initialization*/
 	public PartManager( PartsClient pc ){
 		myClient = pc;
 		Painter.loadImages();
 		pName = new JLabel("Part Name: ");
 		pNumber = new JLabel("Part Number: ");
 		pInfo = new JLabel("Part Info: ");
 		pImage = new JLabel("Part Image: ");
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
 
 		imageCB = new JComboBox();
 		imgEnumList = new ArrayList<Painter.ImageEnum>();
 		
 		for (Painter.ImageEnum en : Painter.ImageEnum.values())
 		{
			ImageIcon img = new ImageIcon( ( ( Painter.getImageIcon(en).getImage() ).getScaledInstance( 25, 25, java.awt.Image.SCALE_SMOOTH ) ) );
 			//add images to JComboBox
 			imageCB.addItem(img);
 			imgEnumList.add(en);
 		}
 		
 		//JScrollPane for list of parts
 		parts = new JPanel();
 		parts.setLayout( new GridBagLayout() );
 		scroll = new JScrollPane(parts);
 		
 		//layout GUI
 		setLayout( new GridBagLayout() );
 		GridBagConstraints c = new GridBagConstraints();
 		
 		//parts scroll pane
 		c.fill = GridBagConstraints.BOTH;
 		c.gridx = 0;
 		c.gridy = 0;
 		c.weightx = 1;
 		c.weighty = 0;
 		c.gridwidth = 2;
 		c.gridheight = 10;
 		add( scroll, c );
 		
 		
 		//adding parts
 		c.fill = GridBagConstraints.HORIZONTAL;
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
 		
 		c.gridx = 2;
 		c.gridy = 3;
 		add( pImage, c );
 		
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
 		
 		c.gridx = 3;
 		c.gridy = 3;
 		add( imageCB, c );
 		
 		//changing/deleting parts
 		c.gridx = 2;
 		c.gridy = 5;
 		add( pEdit, c );
 		
 		c.gridx = 3;
 		c.gridy = 4;
 		add( pEdit2, c );
 		
 		c.gridx = 3;
 		c.gridy = 5;
 		add( tEdit, c );
 		
 		c.gridheight = 1;
 		c.gridx = 4;
 		c.gridy = 4;
 		add( change, c );
 		
 		c.gridx = 4;
 		c.gridy = 5;
 		add( delete, c );
 		
 		//messages
 		c.gridx = 2;
 		c.gridy = 6;
 		c.gridwidth = 3;
 		add( msg, c );
 		
 		//action listeners for buttons
 		create.addActionListener( new ActionListener() {
 			public void actionPerformed( ActionEvent e ){
 				if( !tName.getText().equals("") && !tInfo.getText().equals("") && !tNumber.getText().equals("") ) {
 					try{
 						//add part to server
 						myClient.getCom().write( new NewPartMsg( new Part( tName.getText(), tInfo.getText(), (int)Integer.parseInt( tNumber.getText() ), imgEnumList.get(imageCB.getSelectedIndex()) ) ) );
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
 						myClient.getCom().write( new ChangePartMsg( (int)Integer.parseInt( tEdit.getText() ), new Part( tName.getText(), tInfo.getText(), (int)Integer.parseInt( tNumber.getText() ), imgEnumList.get(imageCB.getSelectedIndex()) ) ) );
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
 	/** regenerate parts label in parts panel */
 	public void displayParts(){
 		//remove current list from the panel
 		parts.removeAll();
 				
 		//add new list to panel
 		ArrayList<Part> temp = myClient.getParts();
 		
 		//constraints
 		GridBagConstraints c = new GridBagConstraints();
 		c.gridx = 0;
 		c.gridy = 0;
 		
 		for( Part p : temp ){
 			parts.add( new JLabel( Painter.getImageIcon(p.getImage())), c );
 			c.gridx++;
 			
 			parts.add( new JLabel( p.getNumber() + " - " + p.getName() + " - " + p.getDescription() ), c );
 			c.gridx--;
 			c.gridy++;
 		}
 				
 		validate();
 		repaint();
 	}
 	
 	public void setMsg( String s ){
 		msg.setText(s);
 	}
 	
 }
