 import java.awt.BorderLayout; 
 import java.awt.Color; 
 import java.awt.Dimension; 
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent; 
 import java.awt.event.ActionListener; 
 import java.awt.event.WindowEvent; 
 import java.awt.event.WindowFocusListener; 
 import java.io.File;
 import java.util.Vector; 
 
 import javax.swing.DefaultListModel; 
 import javax.swing.ImageIcon;
 import javax.swing.JButton; 
 import javax.swing.JFrame; 
 import javax.swing.JLabel; 
 import javax.swing.JList; 
 import javax.swing.JPanel; 
 import javax.swing.JScrollPane; 
 import javax.swing.JTextArea; 
 import javax.swing.ListSelectionModel; 
 import javax.swing.SwingConstants;
 import javax.swing.event.ListSelectionEvent; 
 import javax.swing.event.ListSelectionListener; 
 
 /** 
  * This class is the frame for the inventory that will show up. 
  * 
  * I would like to expand it to show more and look cool like have a picture of a 
  * space ship and status towards fixing the drive. 
  * 
  * @author Ryan 
  * 
  */ 
 public class InventoryFrame extends JFrame implements WindowFocusListener 
 { 
 
 	private Player thePlayer; 
 	private JButton use; 
 	private JButton drop; 
 	private JButton back; 
 	private JList inventory; 
 	private JLabel bank; 
 	private JTextArea descriptionArea; 
 	private DefaultListModel inventoryList; 
 	private JScrollPane scroll; 
 	private JPanel buttons; 
 	private JPanel itemPanel; 
 	private JLabel message; 
 	private Vector<Integer> usableItems; 
 	private SpacePanel thepanel; 
 
 	public InventoryFrame(Player player,SpacePanel sp) 
 	{  
 		super("Inventory");
 		thepanel=sp;
 		addWindowFocusListener(this); 
 
 		thePlayer = player; 
 		thepanel=sp; 
 		use = new JButton();
 		use.setIcon(new ImageIcon("Art" + File.separator + "UseButton.png"));
 		use.setSelectedIcon(new ImageIcon("Art" + File.separator + "UseButtonPressed.png"));
 		use.setOpaque(false);
 		use.setContentAreaFilled(false);
 		use.setBorderPainted(false);
 		
 		drop = new JButton(); 
 		drop.setIcon(new ImageIcon("Art" + File.separator + "DropButton.png"));
 		drop.setSelectedIcon(new ImageIcon("Art" + File.separator + "DropButtonPressed.png"));
 		drop.setOpaque(false);
 		drop.setContentAreaFilled(false);
 		drop.setBorderPainted(false);
 		
 		back = new JButton();
 		back.setIcon(new ImageIcon("Art" + File.separator + "BackIButton.png"));
 		back.setSelectedIcon(new ImageIcon("Art" + File.separator + "BackIButtonPressed.png"));
 		back.setOpaque(false);
 		back.setContentAreaFilled(false);
 		back.setBorderPainted(false);
 		
 		buttons = new JPanel(); 
 		buttons.setLayout(new GridLayout(2,1));
 		buttons.setBackground(Color.DARK_GRAY); 
 		
 		JPanel bankPanel = new JPanel();
 		bankPanel.setLayout(new GridLayout(1,1));
 		bankPanel.setBackground(Color.DARK_GRAY);
 		
 		JPanel buttonPanel = new JPanel();
 		buttonPanel.setLayout(new GridLayout(1,3));
 		buttonPanel.setBackground(Color.DARK_GRAY);
 
 		bank = new JLabel("Money: $" + thePlayer.getMoney() + " Fuel: " 
 				+ thePlayer.getFuelLevel()); 
 		bank.setForeground(Color.GREEN); 
 		bank.setHorizontalAlignment(SwingConstants.CENTER);
 
 		ButtonListener handler = new ButtonListener(); 
 		use.addActionListener(handler); 
 		drop.addActionListener(handler); 
 		back.addActionListener(handler); 
 
 		bankPanel.add(bank);
 		buttonPanel.add(use); 
 		buttonPanel.add(drop); 
 		buttonPanel.add(back);  
 		buttons.add(bankPanel);
 		buttons.add(buttonPanel);
 
 		itemPanel = new JPanel(); 
 		itemPanel.setBackground(Color.DARK_GRAY); 
 
 		descriptionArea = new JTextArea(); 
 		descriptionArea.setPreferredSize(new Dimension(200, 200)); 
 		descriptionArea.setBackground(Color.BLACK); 
 		descriptionArea.setForeground(Color.GREEN); 
 		descriptionArea.setWrapStyleWord(true); 
 		descriptionArea.setLineWrap(true); 
 		descriptionArea.setEditable(false); 
 		descriptionArea.setWrapStyleWord(true); 
 
 		usableItems = new Vector<Integer>(); 
 		fillUsuable(); 
 
 		message = new JLabel(); 
 		message.setText(" ");
		message.setOpaque(true);
		message.setBackground(Color.DARK_GRAY);
		message.setForeground(Color.GREEN);
 
 
 		makeInventory(); 
 		inventory = new JList(inventoryList); 
 		inventory.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); 
 		inventory.setBackground(Color.BLACK); 
 		inventory.setForeground(Color.WHITE); 
 		inventory.setSelectionBackground(Color.DARK_GRAY); 
 		inventory.setSelectionForeground(Color.GREEN); 
 
 		inventory.addListSelectionListener(new ListSelectionListener() 
 		{ 
 
 			@Override 
 			public void valueChanged(ListSelectionEvent arg0) 
 			{ 
 				try 
 				{ 
 					descriptionArea.setText(((Item) inventory.getSelectedValue()) 
 							.getDescription()); 
 				} catch (NullPointerException e) 
 				{ 
 					descriptionArea.setText(""); 
 				} 
 			} 
 
 		}); 
 
 		scroll = new JScrollPane(inventory); 
 		scroll.setPreferredSize(new Dimension(200,200)); 
 		scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 		scroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
 
 		itemPanel.add(scroll); 
 		itemPanel.add(descriptionArea); 
 		itemPanel.setBackground(Color.DARK_GRAY); 
 
 		this.setLayout(new BorderLayout());
 		this.add(itemPanel, BorderLayout.CENTER); 
 		this.add(buttons, BorderLayout.SOUTH); 
 		this.add(message, BorderLayout.NORTH); 
 	} 
 
 
 	/** 
 	 * Use this method to fill the array of Items that can be used. If the name of 
 	 * the item is not added to this array it won't be able to be used.!!!! Use 
 	 * the number that is assigned to each Item 
 	 * 
 	 */ 
 	private void fillUsuable() 
 	{ 
 		usableItems.add(4); 
 		usableItems.add(28);
 		usableItems.add(32); 
 		usableItems.add(31); 
 		usableItems.add(30); 
 
 	} 
 
 	private void makeInventory() 
 	{ 
 		inventoryList = new DefaultListModel(); 
 		Vector<Item> v = thePlayer.getInventory(); 
 		for (Item item : v) 
 		{ 
 			inventoryList.addElement(item); 
 		} 
 	} 
 
 	public void windowGainedFocus(WindowEvent arg0) 
 	{ 
 	} 
 
 	public void windowLostFocus(WindowEvent arg0) 
 	{ 
 		dispose(); 
 	} 
 
 	private class ButtonListener implements ActionListener 
 	{ 
 
 		@Override 
 		public void actionPerformed(ActionEvent event) 
 		{ 
 			if (!event.getSource().equals(back)) 
 			{ 
 
 			
 
 				try 
 				{ 
 				  
 				  Item item = (Item) inventory.getSelectedValue(); 
 	        int indexNumber = inventory.getSelectedIndex(); 
 	        int itemNumber = item.getIDNumber(); 
 	        
 					if (event.getSource().equals(use)) 
 					{ 
 						if (usableItems.contains(itemNumber) && thePlayer.canUseThisPart(item)) 
 						{ 
 							message.setText(thePlayer.use(item)); 
 							if (thepanel!=null) 
 							{ 
 								thepanel.playerStatusChanged(); 
 							} 
 							bank.setText("Money: $" + thePlayer.getMoney() + " Fuel: " 
 									+ thePlayer.getFuelLevel()); 
 							inventoryList.removeElement(item); 
 							inventory.setSelectedIndex(indexNumber); 
 
 						} else 
 						{ 
 							message.setText("You can't use this item");
 						} 
 					} else if (event.getSource().equals(drop)) 
 					{ 
 
 						thePlayer.drop(item); 
 						inventoryList.removeElement(item); 
 						inventory.setSelectedIndex(indexNumber); 
 					} 
					repaint();
 				} catch (Exception e) 
 				{ 
 
 				} 
 			} else 
 			{ 
 				dispose(); 
 			} 
 
 		} 
 	} 
 } 
