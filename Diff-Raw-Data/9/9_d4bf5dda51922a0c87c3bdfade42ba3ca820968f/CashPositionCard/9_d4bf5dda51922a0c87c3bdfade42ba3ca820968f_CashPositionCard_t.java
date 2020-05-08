 package gui.manager.forms;
 
 import gui.Card;
 import java.awt.CardLayout;
 import java.awt.Container;
 
 import javax.swing.JPanel;
 
 import java.awt.*;
 import java.awt.event.*;
 
 import javax.swing.*;
 
 import com.jgoodies.forms.factories.*;
 import com.jgoodies.forms.layout.*;
 
 import system.SystemBox;
 import system.Store;
 import system.Cashier;
 
 import java.util.*;
 
 public class CashPositionCard 
 {
 	private JSplitPane reportpane;
 	private Container con;
 	private JTextField textField;
 	private JTextField textField_1;
 	private JTextField textField_2;
 	private JPanel panel_1;
 	
 	private LinkedList<JTextField> cashierDetails;
 	
 	public JSplitPane getCard(Container con)
 	{
 		if(reportpane==null)
 		{
 			reportpane = new JSplitPane();
 			this.con = con;
 			init();
 		}
 		return reportpane;
 	}
 	
 	
 	public void init()
 	{
 		JScrollPane scrollPane = new JScrollPane();
 		reportpane.setRightComponent(scrollPane);
 		
 		panel_1 = new JPanel();
 		scrollPane.setViewportView(panel_1);
 		panel_1.setLayout(new FormLayout(
 		new ColumnSpec[] 
 		{
 			FormFactory.RELATED_GAP_COLSPEC,
 			FormFactory.MIN_COLSPEC,
 			FormFactory.RELATED_GAP_COLSPEC,
 			ColumnSpec.decode("min:grow"),
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
 		
 		JLabel lblItem = new JLabel("Cashier Index");
 		panel_1.add(lblItem, "2, 2");
 		
 		JLabel lblPrice = new JLabel("Cash");
 		panel_1.add(lblPrice, "4, 2");
 		
 		cashierDetails = new LinkedList<JTextField>();
 		
 		JPanel panel = new JPanel();
 		reportpane.setLeftComponent(panel);
 		panel.setLayout(new FormLayout(
 		new ColumnSpec[] 
 		{
 			FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
 			FormFactory.MIN_COLSPEC,
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
 		
 		JLabel lblItemId = new JLabel("Store ID:");
 		panel.add(lblItemId, "2, 2, right, default");
 		
 		textField = new JTextField();
 		panel.add(textField, "4, 2, fill, default");
 		textField.setColumns(10);
 		
 		JButton btnInquire = new JButton("Inquire");
 		btnInquire.addMouseListener(new MouseAdapter() 
 		{
 			@Override
 			public void mousePressed(MouseEvent arg0) 
 			{
 				int storeId = 0;
 				try
 				{
 					storeId = Integer.parseInt(textField.getText());
 				}
 				catch(NumberFormatException nfe)
 				{
 					JOptionPane.showMessageDialog(reportpane, "Specified Store ID is in an improper format.");
 					return;
 				}
 				Store s = null;
 				try
 				{
 					s = SystemBox.getSystem().getStore(storeId);
 				}
 				catch(IndexOutOfBoundsException ioobe)
 				{
 					JOptionPane.showMessageDialog(reportpane, "Store not found.");
 					return;
 				}
 				double cash = s.getTotalCash();
 				textField_1.setText(cash + "");
				
				for(JTextField j : cashierDetails)
				{
					j.setVisible(false);
					panel_1.remove(j);
				}
				
 				Iterator<Cashier> i = s.cashierIterator();
 				int y = 4;
 				int index = 0;
 				while(i.hasNext())
 				{
 					JTextField tempCashier = new JTextField();
 					tempCashier.setText(index++ + "");
 					tempCashier.setEditable(false);
 					panel_1.add(tempCashier, "2, " + y + ", fill, default");
 					tempCashier.setColumns(10);
 					cashierDetails.add(tempCashier);
 					
 					JTextField tempCash = new JTextField();
 					double currCash = i.next().getCash();
 					cash += currCash;
 					tempCash.setText(currCash + "");
 					tempCash.setEditable(false);
 					panel_1.add(tempCash, "4, " + y + ", fill, default");
 					tempCash.setColumns(10);
 					cashierDetails.add(tempCash);
 					
 					
 					y+=2;
 				}
 				
 				panel_1.revalidate();
 				textField_2.setText(cash + "");
 			}
 		});
 		panel.add(btnInquire, "2, 4");
 		
 		JLabel lblCashOnStore = new JLabel("Cash on Store:");
 		panel.add(lblCashOnStore, "2, 6, right, default");
 		
 		textField_1 = new JTextField();
 		textField_1.setEditable(false);
 		panel.add(textField_1, "4, 6, fill, default");
 		textField_1.setColumns(10);
 		
 		JLabel lblTotalCash = new JLabel("Total Cash:");
 		panel.add(lblTotalCash, "2, 8, right, default");
 		
 		textField_2 = new JTextField();
 		textField_2.setEditable(false);
 		panel.add(textField_2, "4, 8, fill, default");
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
 		panel.add(btnCancel, "2, 12");
 	}
 	
 	public void resetFields()
 	{
 		textField.setText("");
 		textField_1.setText("");
 		textField_2.setText("");
 		
 		for(JTextField j : cashierDetails)
 		{
 			panel_1.remove(j);
 		}
 	}
 }
