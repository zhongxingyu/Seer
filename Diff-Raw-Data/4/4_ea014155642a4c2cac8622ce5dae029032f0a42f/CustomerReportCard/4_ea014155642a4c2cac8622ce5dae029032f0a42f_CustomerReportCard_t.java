 package gui.manager.forms;
 
 import gui.Card;
 import java.awt.CardLayout;
 import java.awt.Container;
 
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.sql.Timestamp;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 
 import javax.swing.*;
 
 import org.hibernate.Transaction;
 
 import system.Customer;
 import system.Store;
 import system.SystemBox;
 import system.TransactionItem;
 
 import com.jgoodies.forms.factories.*;
 import com.jgoodies.forms.layout.*;
 
 public class CustomerReportCard {
 	private JSplitPane reportpane;
 	private Container con;
 	private JTextField textField;
 	private JComboBox comboBox;
 	
 	private HashMap<String, system.Transaction> activeTransactions;
 	private LinkedList<JTextField> transactionDetails;
 	
 	public JSplitPane getCard(Container con){
 		if(reportpane==null){
 			reportpane = new JSplitPane();
 			this.con = con;
 			init();
 		}
 		return reportpane;
 	}
 	
 	final JPanel panel_1 = new JPanel();
 	final JLabel paymentDetails = new JLabel();
 	
 	/**
 	 * @wbp.parser.entryPoint
 	 */
 	public void init(){
 		activeTransactions = new HashMap<String, system.Transaction>();
 		transactionDetails = new LinkedList<JTextField>();
 		JScrollPane scrollPane = new JScrollPane();
 		reportpane.setRightComponent(scrollPane);
 		
 		
 		scrollPane.setViewportView(panel_1);
 		panel_1.setLayout(new FormLayout(new ColumnSpec[] {
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.MIN_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("default:grow"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("max(39dlu;default):grow"),},
 			new RowSpec[] {
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
 				FormFactory.DEFAULT_ROWSPEC,}));
 		
 		JLabel lblQuantity_1 = new JLabel("Quantity");
 		panel_1.add(lblQuantity_1, "2, 2");
 		
 		JLabel lblItem = new JLabel("Item");
 		panel_1.add(lblItem, "4, 2");
 		
 		JLabel lblPrice = new JLabel("Price");
 		panel_1.add(lblPrice, "6, 2");
 		
 		JPanel panel = new JPanel();
 		reportpane.setLeftComponent(panel);
 		panel.setLayout(new FormLayout(new ColumnSpec[] {
 				FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
 				FormFactory.MIN_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("max(55dlu;default):grow"),},
 			new RowSpec[] {
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
 				FormFactory.DEFAULT_ROWSPEC,}));
 		
 		JLabel lblItemId = new JLabel("Customer ID:");
 		panel.add(lblItemId, "2, 2, right, default");
 		
 		textField = new JTextField();
 		panel.add(textField, "4, 2, fill, default");
 		textField.setColumns(10);
 		
 		JButton btnInquire = new JButton("Inquire");
 		btnInquire.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mousePressed(MouseEvent arg0) {
 				String customerIdString = textField.getText();
 				int customerId = 0;
 				try
 				{
 					customerId = Integer.parseInt(customerIdString);
 				}
 				catch(NumberFormatException nfe)
 				{
 					JOptionPane.showMessageDialog(reportpane, "Specified Customer ID is in an improper format.");
 					return;
 				}
 				Customer c = null;
 				try
 				{
 					c = SystemBox.getSystem().getCustomer(customerId);
 				}
 				catch(IndexOutOfBoundsException ioobe)
 				{
 					JOptionPane.showMessageDialog(reportpane, "Customer not found.");
 					return;
 				}
 				activeTransactions.clear();
 				comboBox.removeAllItems();
 				Iterator<Store> stores = SystemBox.getSystem().storeIterator();
 				while(stores.hasNext())
 				{
 					Store s = stores.next();
 					Iterator<system.Transaction> transactions = s.transactionIterator();
 					while(transactions.hasNext())
 					{
 						system.Transaction t = transactions.next();
 						if(t.getCustomer().equals(c))
 						{
 							comboBox.addItem(t.getDateTime().toString());
 							activeTransactions.put(t.getDateTime().toString(), t);
 						}
 					}
 				}
 				comboBox.revalidate();
 				changeTransactionDetails();
 			}
 		});
 		panel.add(btnInquire, "2, 4");
 		
 		JLabel lblSelectTransaction = new JLabel("Select Transaction:");
 		panel.add(lblSelectTransaction, "2, 6, right, default");
 		
 		comboBox = new JComboBox();
 		comboBox.addItemListener(new ItemListener() {
 			public void itemStateChanged(ItemEvent arg0) {
 				
 				changeTransactionDetails();
 				
 			}
 
 			
 		});
 		panel.add(comboBox, "4, 6, fill, default");
 		
 		JButton btnCancel = new JButton("Cancel");
 		btnCancel.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mousePressed(MouseEvent arg0) {
 				resetFields();
 				CardLayout cl = (CardLayout) con.getLayout();
 				cl.show(con, Card.MANAGER.getLabel());
 			}
 		});
 		panel.add(btnCancel, "2, 10");
 		
 	}
 	
 	public void resetFields()
 	{
 		textField.setText("");
 		activeTransactions.clear();
 		comboBox.removeAllItems();
 		paymentDetails.setText("");
 		
 		for(JTextField j : transactionDetails)
 		{
 			panel_1.remove(j);
 		}
 		
 		transactionDetails = new LinkedList<JTextField>();
 	}
 	
 	private void changeTransactionDetails() {
 		system.Transaction t;
 		for(JTextField j : transactionDetails)
 		{
 			panel_1.remove(j);
 		}
 		paymentDetails.setText("");
 		
 		transactionDetails = new LinkedList<JTextField>();
 		try
 		{
 			t = activeTransactions.get(comboBox.getSelectedItem().toString());
 			int transactionDrawingPosition = 4;
 			Iterator<TransactionItem> itemsSold = t.itemsSoldIterator();
 			while(itemsSold.hasNext())
 			{
 				TransactionItem ti = itemsSold.next();
 				
 				JTextField quantity = new JTextField();
 				JTextField item = new JTextField();
 				JTextField price = new JTextField();
 				
 				quantity.setEditable(false);
 				item.setEditable(false);
 				price.setEditable(false);
 				
 				quantity.setText(ti.getQuantity() + "");
				item.setText(ti.getItem().getItemCode() + ": " + ti.getItem().getItemName() + " at " + ti.getPrice() + " per " + ti.getItem().getUnitName());
				price.setText(ti.getPrice() * ti.getQuantity() + "");
 				
 				panel_1.add(quantity, "2, " + transactionDrawingPosition + ", fill, default");
 				panel_1.add(item, "4, " + transactionDrawingPosition + ", fill, default");
 				panel_1.add(price, "6, " + transactionDrawingPosition + ", fill, default");
 				
 				transactionDetails.add(quantity);
 				transactionDetails.add(item);
 				transactionDetails.add(price);
 				transactionDrawingPosition += 2;
 			}
 			
 			
 			paymentDetails.setText("The total amount due was " + (t.getRevenue() + t.getPointsUsed()) + ", and " + t.getPointsUsed() + " points were used, generating a total revenue of " + t.getRevenue() + ".");
 			panel_1.add(paymentDetails, "4, " + transactionDrawingPosition + ", fill, default");
 
 			panel_1.revalidate();
 		}
 		catch(NullPointerException npe)
 		{
 			panel_1.revalidate();
 		}
 	}
 }
