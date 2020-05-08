 package gui.manager.forms;
 
 import gui.Card;
 
 import java.awt.Container;
 
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import java.awt.*;
 import java.awt.event.*;
 
 import javax.swing.*;
 
 import com.jgoodies.forms.factories.*;
 import com.jgoodies.forms.layout.*;
 
 import system.SystemBox;
 import system.Store;
 import system.Item;
 import system.Delivery;
 
 import java.util.LinkedList;
 
 public class RestockCard2 
 {
 	private JSplitPane restockpane;
 	private Container con;
 	private JTextField textField;
 	private JTextField textField_1;
 	private JTextField textField_2;
 	private JTextField textField_9;
 	private JPanel panel_1;
 	
 	private LinkedList<JTextField> transactionDetails;
 	private int transactionDrawingPosition;
 	
 	private Store store;
 	
 	public void setStore(Store store)
 	{
 		this.store = store;
 	}
 	
 	public JSplitPane getCard(Container con)
 	{
 		if(restockpane==null)
 		{
 			restockpane = new JSplitPane();
 			this.con = con;
 			init();
 		}
 		return restockpane;
 	}
 	
 	/**
 	 * @wbp.parser.entryPoint
 	 */
 	public void init()
 	{
 		JScrollPane scrollPane = new JScrollPane();
 		restockpane.setRightComponent(scrollPane);
 		
 		panel_1 = new JPanel();
 		scrollPane.setViewportView(panel_1);
 		panel_1.setLayout(new FormLayout(
 		new ColumnSpec[] 
 		{
 			FormFactory.RELATED_GAP_COLSPEC,
 			FormFactory.MIN_COLSPEC,
 			FormFactory.RELATED_GAP_COLSPEC,
 			ColumnSpec.decode("default:grow"),
 			FormFactory.RELATED_GAP_COLSPEC,
 			ColumnSpec.decode("max(39dlu;default):grow"),
 		},
 		new RowSpec[] 
 		{
 			FormFactory.RELATED_GAP_ROWSPEC,
 			FormFactory.DEFAULT_ROWSPEC,
 			FormFactory.RELATED_GAP_ROWSPEC,
 			FormFactory.DEFAULT_ROWSPEC,
 			FormFactory.RELATED_GAP_ROWSPEC,
 			FormFactory.DEFAULT_ROWSPEC,
 			FormFactory.RELATED_GAP_ROWSPEC,
 			FormFactory.DEFAULT_ROWSPEC,
 			FormFactory.RELATED_GAP_ROWSPEC,
 			FormFactory.DEFAULT_ROWSPEC,
 			FormFactory.RELATED_GAP_ROWSPEC,
 			FormFactory.DEFAULT_ROWSPEC,
 			FormFactory.RELATED_GAP_ROWSPEC,
 			FormFactory.DEFAULT_ROWSPEC,
 			FormFactory.RELATED_GAP_ROWSPEC,
 			FormFactory.DEFAULT_ROWSPEC,
 			FormFactory.RELATED_GAP_ROWSPEC,
 			FormFactory.DEFAULT_ROWSPEC,
 			FormFactory.RELATED_GAP_ROWSPEC,
 			FormFactory.DEFAULT_ROWSPEC,
 			FormFactory.RELATED_GAP_ROWSPEC,
 			FormFactory.DEFAULT_ROWSPEC,
 			FormFactory.RELATED_GAP_ROWSPEC,
 			FormFactory.DEFAULT_ROWSPEC,
 			FormFactory.RELATED_GAP_ROWSPEC,
 			FormFactory.DEFAULT_ROWSPEC,
 		}));
 		
 		JLabel lblQuantity_1 = new JLabel("Quantity");
 		panel_1.add(lblQuantity_1, "2, 2");
 		
 		JLabel lblItem = new JLabel("Item");
 		panel_1.add(lblItem, "4, 2");
 		
 		JLabel lblPrice = new JLabel("Total Price");
 		panel_1.add(lblPrice, "6, 2");
 		
 		transactionDetails = new LinkedList<JTextField>();
 		transactionDrawingPosition = 4;
 		
 		JPanel panel = new JPanel();
 		restockpane.setLeftComponent(panel);
 		panel.setLayout(new FormLayout(
 		new ColumnSpec[] 
 		{
 			FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
 			ColumnSpec.decode("85px:grow"),
 			FormFactory.RELATED_GAP_COLSPEC,
 			ColumnSpec.decode("max(55dlu;default):grow"),
 		},
 		new RowSpec[] 
 		{
 			FormFactory.LINE_GAP_ROWSPEC,
 			RowSpec.decode("23px"),
 			FormFactory.RELATED_GAP_ROWSPEC,
 			FormFactory.DEFAULT_ROWSPEC,
 			FormFactory.RELATED_GAP_ROWSPEC,
 			FormFactory.DEFAULT_ROWSPEC,
 			FormFactory.RELATED_GAP_ROWSPEC,
 			FormFactory.DEFAULT_ROWSPEC,
 			FormFactory.RELATED_GAP_ROWSPEC,
 			FormFactory.DEFAULT_ROWSPEC,
 			FormFactory.RELATED_GAP_ROWSPEC,
 			FormFactory.DEFAULT_ROWSPEC,
 			FormFactory.RELATED_GAP_ROWSPEC,
 			FormFactory.DEFAULT_ROWSPEC,
 			FormFactory.RELATED_GAP_ROWSPEC,
 			FormFactory.DEFAULT_ROWSPEC,
 			FormFactory.RELATED_GAP_ROWSPEC,
 			FormFactory.DEFAULT_ROWSPEC,
 		}));
 		
 		JLabel lblItemId = new JLabel("Item Code:");
 		panel.add(lblItemId, "2, 2, right, default");
 		
 		textField = new JTextField();
 		panel.add(textField, "4, 2, fill, default");
 		textField.setColumns(10);
 		
 		JLabel lblQuantity = new JLabel("Quantity:");
 		panel.add(lblQuantity, "2, 4, right, default");
 		
 		textField_1 = new JTextField();
 		panel.add(textField_1, "4, 4, fill, default");
 		textField_1.setColumns(10);
 		
 		JButton btnEnd = new JButton("End");
 		btnEnd.addMouseListener(new MouseAdapter() 
 		{
 			@Override
 			public void mousePressed(MouseEvent e) 
 			{
 				Delivery d = store.endDeliveryBatch();
 				SystemBox.getSystem().addDelivery(d);
 				JOptionPane.showMessageDialog(restockpane, "Delivery ended. The total amount due is " + d.getTotalPrice() + ".");
 				resetFields();
 				CardLayout cl = (CardLayout) con.getLayout();
 				cl.show(con, Card.MANAGER.getLabel());
 			}
 		});
 		
 		JButton btnAdd = new JButton("Add");
 		btnAdd.addMouseListener(new MouseAdapter() 
 		{
 			@Override
 			public void mousePressed(MouseEvent e) 
 			{
 				if(textField.getText().equals(""))
 				{
 					JOptionPane.showMessageDialog(restockpane, "No Item Code specified.");
 					return;
 				}
 				if(!SystemBox.getSystem().containsItem(textField.getText()))
 				{
 					JOptionPane.showMessageDialog(restockpane, "Item not found.");
 					return;
 				}
 				int quantity = 0;
 				try
 				{
 					quantity = Integer.parseInt(textField_1.getText());
 				}
 				catch(NumberFormatException nfe)
 				{
 					JOptionPane.showMessageDialog(restockpane, "Specified Quantity is in an improper format.");
 					return;
 				}
 				double price = 0;
 				try
 				{
 					price = Double.parseDouble(textField_9.getText());
 				}
 				catch(NumberFormatException nfe)
 				{
 					JOptionPane.showMessageDialog(restockpane, "Specified Price is in an improper format.");
 					return;
 				}
 				Item accepted = SystemBox.getSystem().getItem(textField.getText());
 				double added = store.acceptDeliveryItem(accepted, quantity, price);
 				
 				textField.setText("");
 				textField_1.setText("");
 				textField_9.setText("");
				//textField_2.setText(Double.parseDouble(textField_2.getText()) + added + "");
 				
 				JTextField tempQuantity = new JTextField();
 				JTextField tempItem = new JTextField();
 				JTextField tempTotalPrice = new JTextField();
 				
 				tempQuantity.setEditable(false);
 				tempItem.setEditable(false);
 				tempTotalPrice.setEditable(false);
 				
 				tempQuantity.setText(quantity + "");
 				tempItem.setText(accepted.getItemName() + " at " + price + " each");
 				tempTotalPrice.setText(quantity * price + "");
 				
 				panel_1.add(tempQuantity, "2, " + transactionDrawingPosition + ", fill, default");
 				panel_1.add(tempItem, "4, " + transactionDrawingPosition + ", fill, default");
 				panel_1.add(tempTotalPrice, "6, " + transactionDrawingPosition + ", fill, default");
 				
 				tempQuantity.setColumns(10);
 				tempItem.setColumns(10);
 				tempTotalPrice.setColumns(10);
 				
 				panel_1.revalidate();
 		
 				transactionDetails.add(tempQuantity);
 				transactionDetails.add(tempItem);
 				transactionDetails.add(tempTotalPrice);
 				
 				transactionDrawingPosition += 2;
 			}
 		});
 		
 		JLabel lblWholesalePrice = new JLabel("Wholesale Price:");
 		panel.add(lblWholesalePrice, "2, 6, right, default");
 		
 		textField_9 = new JTextField();
 		panel.add(textField_9, "4, 6, fill, default");
 		textField_9.setColumns(10);
 		panel.add(btnAdd, "4, 10, fill, top");
 		panel.add(btnEnd, "4, 12");
 		
 		JLabel lblAmountDue = new JLabel("Amount Due:");
 		panel.add(lblAmountDue, "2, 14");
 		
 		textField_2 = new JTextField();
 		textField_2.setHorizontalAlignment(SwingConstants.RIGHT);
 		textField_2.setEditable(false);
 		textField_2.setText("0.0");
 		panel.add(textField_2, "2, 16, fill, default");
 		textField_2.setColumns(10);
 		
 		JButton btnCancel = new JButton("Cancel");
 		btnCancel.addMouseListener(new MouseAdapter() 
 		{
 			@Override
 			public void mousePressed(MouseEvent arg0) 
 			{
 				resetFields();
 				CardLayout cl = (CardLayout) con.getLayout();
 				cl.show(con, Card.MANAGER.getLabel());
 			}
 		});
 		panel.add(btnCancel, "2, 18");
 	}
 	
 	public void resetFields()
 	{
 		textField.setText("");
 		textField_1.setText("");
		textField_2.setText("");
 		textField_9.setText("");
 		
 		for(JTextField j : transactionDetails)
 		{
 			panel_1.remove(j);
 		}
 		
 		transactionDetails = new LinkedList<JTextField>();
 		transactionDrawingPosition = 4;
 	}
 }
