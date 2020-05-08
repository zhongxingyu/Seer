 /*
  * Author: David Rivera
  * Course: CNT 4714 – Fall 2012
  * Assignment title: Program 1 – Event-driven Programming
  * Date: September 9, 2012
  */
 package ucf.cdstore;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.SpringLayout;
 
 /**
  * Create the main interface a customer interacts with.
  */
 
 public class StoreWindow{
 	
 	private static CD sCD;
 	private static int sCurrentOrderNumber;
 	private static JFrame sStoreWindow;
 	private static JPanel sPanel;
 	private static SpringLayout sLayout;
 	private static StoreAction sStoreAction;
     
 	private static JTextField sNumItemsTF;
 	private static JTextField sIDTF;
 	private static JTextField sQuantityTF;
 	private static JTextField sItemInfoTF;
 	private static JTextField sSubtotalTF;
 	
 	private static JLabel sNumItemsL;
 	private static JLabel sIDL;
 	private static JLabel sQuantityL;
 	private static JLabel sItemInfoL;
 	private static JLabel sSubtotalL;
 	
 	private static JButton sProcessItem;
 	private static JButton sConfirmItem;
 	private static JButton sViewOrder;
 	private static JButton sFinishOrder;
 	private static JButton sNewOrder;
 	private static JButton sExit;
 	
 	/**
 	 * Starts the initialization of the JFrame, and initializes the StoreAction object.
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		new StoreWindow().StartThread();
 		sCurrentOrderNumber = 1;
         InitWindow();
         
 	}
    
 	/**
 	 * Sets many of the base attributes of the frame/panel and calls other methods to populate it.
 	 */
 	private static void InitWindow(){
     	sStoreWindow = new JFrame("CD Store");
     	sPanel  = new JPanel();
 
     	sPanel.setSize(700, 200);
        sStoreWindow.setSize(760, 200);
         
         InitButtons();
         
         InitLabels();
 
         InitText();
 
         InitPlacement();
         
         sStoreWindow.add(sPanel);
         sStoreWindow.setVisible(true);
     }
     
 	/**
 	 * Initializes the JLabels and adds them to the panel.
 	 */
 	private static void InitLabels(){
     	sNumItemsL = new JLabel("Enter the number of items for this order:");
     	sIDL = new JLabel("Enter CD ID for item #"+String.valueOf(sCurrentOrderNumber)+":");
     	sQuantityL = new JLabel("Enter quantity for item #"+String.valueOf(sCurrentOrderNumber)+":");
     	sItemInfoL = new JLabel("Item #"+String.valueOf(sCurrentOrderNumber)+" info:");
     	sSubtotalL = new JLabel("Order subtotal for "+String.valueOf(sCurrentOrderNumber-1)+" item(s):");
     	
         sPanel.add(sNumItemsL);
         sPanel.add(sIDL);
         sPanel.add(sQuantityL);
         sPanel.add(sItemInfoL);
         sPanel.add(sSubtotalL);
     }
     
 	/**
 	 * Initializes the JTextFields and adds listeners to them.
 	 */
 	private static void InitText(){
     	sNumItemsTF = new JTextField(13);
     	sIDTF = new JTextField(13);
     	sQuantityTF = new JTextField(13);
     	sItemInfoTF = new JTextField(40);
     	sSubtotalTF = new JTextField(13);
     	
     	sIDTF.addKeyListener(new KeyListener(){
 
 			@Override
 			public void keyPressed(KeyEvent arg0) {
 				
 			}
 
 			@Override
 			public void keyReleased(KeyEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void keyTyped(KeyEvent arg0) {
 				if((arg0.getKeyCode() == KeyEvent.VK_ENTER) && !sIDTF.getText().isEmpty()){
 					sCD = sStoreAction.FindCD(Integer.valueOf(sIDTF.getText()));
 					if(!sQuantityTF.getText().isEmpty())
 						sStoreAction.SetCDInfo(sCD, Integer.valueOf(sQuantityTF.getText()));
 					else
 						sStoreAction.SetCDInfo(sCD, 1);
 					AutoComplete(sCD);
 				}
 				
 			}});
     	sIDTF.addFocusListener(new FocusListener(){
 
 			@Override
 			public void focusGained(FocusEvent e) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void focusLost(FocusEvent e) {
 				if(!sIDTF.getText().isEmpty()){
 					sCD = sStoreAction.FindCD(Integer.valueOf(sIDTF.getText()));
 					
 					if(!sQuantityTF.getText().isEmpty())
 						sStoreAction.SetCDInfo(sCD, Integer.valueOf(sQuantityTF.getText()));
 					else
 						sStoreAction.SetCDInfo(sCD, 1);
 					
 					AutoComplete(sCD);
 				}
 			}
     		
     	});
     	sQuantityTF.addFocusListener(new FocusListener(){
 
 			@Override
 			public void focusGained(FocusEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void focusLost(FocusEvent arg0) {
 				if(!sQuantityTF.getText().isEmpty() && !sIDTF.getText().isEmpty()){
 //					sCD = sStoreAction.FindCD(Integer.valueOf(sIDTF.getText()),Integer.valueOf(sQuantityTF.getText()));
 					sCD = sStoreAction.FindCD(Integer.valueOf(sIDTF.getText()));
 
 					sStoreAction.SetCDInfo(sCD, Integer.valueOf(sQuantityTF.getText()));
 					
 					AutoComplete(sCD);
 				}
 				
 			}});
     	sQuantityTF.addKeyListener(new KeyListener(){
 
 			@Override
 			public void keyPressed(KeyEvent arg0) {
 				
 			}
 
 			@Override
 			public void keyReleased(KeyEvent arg0) {
 				if(Character.isDigit(arg0.getKeyChar())){
 //					sCD = sStoreAction.FindCD(Integer.valueOf(sIDTF.getText()),Integer.valueOf(sQuantityTF.getText()));
 					sCD = sStoreAction.FindCD(Integer.valueOf(sIDTF.getText()));
 					sStoreAction.SetCDInfo(sCD, Integer.valueOf(sQuantityTF.getText()));
 					
 					AutoComplete(sCD);
 				}
 			}
 
 			@Override
 			public void keyTyped(KeyEvent arg0) {
 			}});
     	
     	sItemInfoTF.setEnabled(false);
     	sSubtotalTF.setEditable(false);
     	
         sPanel.add(sNumItemsTF);
         sPanel.add(sIDTF);
         sPanel.add(sQuantityTF);
         sPanel.add(sItemInfoTF);
         sPanel.add(sSubtotalTF);
     }
     
 	/**
 	 * Initializes the JButtons and adds listeners to them.
 	 */
 	private static void InitButtons(){
     	sProcessItem = new JButton("Process Item #"+String.valueOf(sCurrentOrderNumber)+"");
     	sConfirmItem = new JButton("Confirm Item #"+String.valueOf(sCurrentOrderNumber)+"");
     	sViewOrder = new JButton("View Order");
     	sFinishOrder = new JButton("Finish Order");
     	sNewOrder = new JButton("New Order");
     	sExit = new JButton("Exit");
     	
     	sProcessItem.setEnabled(false);
     	
     	sProcessItem.addActionListener(new ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				sStoreAction.ProcessOrder(sCD, Integer.valueOf(sQuantityTF.getText()), Integer.valueOf(sNumItemsTF.getText()));
				sNumItemsTF.setEditable(false);
				sNumItemsTF.setEditable(false);
				sConfirmItem.setEnabled(true);
				sProcessItem.setEnabled(false);
				
				sIDTF.setText("");
				sQuantityTF.setText("");
				sCurrentOrderNumber++;
				RedrawLabels();
 			}});
     	sConfirmItem.addActionListener(new ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				JOptionPane.showMessageDialog(sPanel,"Item #"+String.valueOf(sCurrentOrderNumber)+" accepted");
 				sNumItemsTF.setEditable(false);
 				sProcessItem.setEnabled(true);
 				sConfirmItem.setEnabled(false);
 			}});
     	sViewOrder.addActionListener(new ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				JOptionPane.showMessageDialog(sPanel, sStoreAction.getDisplayViewOrder());
 			}});
     	sFinishOrder.addActionListener(new ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				JOptionPane.showMessageDialog(sPanel, sStoreAction.DisplayInvoice());
 				sStoreAction.WriteInvoice();
 			}});
     	sNewOrder.addActionListener(new ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				NewOrderClick();
 			}});
     	sExit.addActionListener(new ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				sStoreWindow.dispose();
 			}});
     	
     	sPanel.add(sProcessItem);
         sPanel.add(sConfirmItem);
         sPanel.add(sViewOrder);
         sPanel.add(sFinishOrder);
         sPanel.add(sNewOrder);
         sPanel.add(sExit);
     }
 	
 	/**
 	 * Positions all of the objects in the JPanel using SpringLayout.
 	 */
 	private static void InitPlacement(){
     	sLayout = new SpringLayout();
     	sLayout.putConstraint(SpringLayout.NORTH,sNumItemsL, 5, SpringLayout.NORTH,sPanel);
     	sLayout.putConstraint(SpringLayout.NORTH,sNumItemsTF, 5, SpringLayout.NORTH,sPanel);
     	sLayout.putConstraint(SpringLayout.WEST, sNumItemsL, 5, SpringLayout.WEST, sPanel);
     	sLayout.putConstraint(SpringLayout.WEST, sNumItemsTF, 5, SpringLayout.EAST, sNumItemsL);
     	
     	sLayout.putConstraint(SpringLayout.NORTH,sIDL, 7, SpringLayout.SOUTH,sNumItemsL);
     	sLayout.putConstraint(SpringLayout.WEST, sIDL, 5, SpringLayout.WEST, sPanel);
     	sLayout.putConstraint(SpringLayout.NORTH, sIDTF, 5, SpringLayout.SOUTH, sNumItemsTF);
     	sLayout.putConstraint(SpringLayout.WEST, sIDTF, 5, SpringLayout.EAST, sIDL);
     	
     	sLayout.putConstraint(SpringLayout.NORTH,sQuantityL, 10, SpringLayout.SOUTH,sIDL);
     	sLayout.putConstraint(SpringLayout.WEST, sQuantityL, 5, SpringLayout.WEST, sPanel);
     	sLayout.putConstraint(SpringLayout.NORTH, sQuantityTF, 5, SpringLayout.SOUTH, sIDTF);
     	sLayout.putConstraint(SpringLayout.WEST, sQuantityTF, 5, SpringLayout.EAST, sQuantityL);
     	
     	sLayout.putConstraint(SpringLayout.NORTH,sItemInfoL, 10, SpringLayout.SOUTH,sQuantityL);
     	sLayout.putConstraint(SpringLayout.WEST, sItemInfoL, 5, SpringLayout.WEST, sPanel);
     	sLayout.putConstraint(SpringLayout.NORTH, sItemInfoTF, 5, SpringLayout.SOUTH, sQuantityTF);
     	sLayout.putConstraint(SpringLayout.WEST, sItemInfoTF, 5, SpringLayout.EAST, sItemInfoL);
     	
     	sLayout.putConstraint(SpringLayout.NORTH,sSubtotalL, 10, SpringLayout.SOUTH,sItemInfoL);
     	sLayout.putConstraint(SpringLayout.WEST, sSubtotalL, 5, SpringLayout.WEST, sPanel);
     	sLayout.putConstraint(SpringLayout.NORTH, sSubtotalTF, 5, SpringLayout.SOUTH, sItemInfoTF);
     	sLayout.putConstraint(SpringLayout.WEST, sSubtotalTF, 5, SpringLayout.EAST, sSubtotalL);
     	
     	sLayout.putConstraint(SpringLayout.NORTH, sProcessItem, 10, SpringLayout.SOUTH, sSubtotalL);
     	sLayout.putConstraint(SpringLayout.NORTH, sConfirmItem, 10, SpringLayout.SOUTH, sSubtotalL);
     	sLayout.putConstraint(SpringLayout.NORTH, sViewOrder, 10, SpringLayout.SOUTH, sSubtotalL);
     	sLayout.putConstraint(SpringLayout.NORTH, sFinishOrder, 10, SpringLayout.SOUTH, sSubtotalL);
     	sLayout.putConstraint(SpringLayout.NORTH, sNewOrder, 10, SpringLayout.SOUTH, sSubtotalL);
     	sLayout.putConstraint(SpringLayout.NORTH, sExit, 10, SpringLayout.SOUTH, sSubtotalL);
     	
     	sLayout.putConstraint(SpringLayout.WEST, sProcessItem, 10, SpringLayout.WEST, sPanel);
     	sLayout.putConstraint(SpringLayout.WEST, sConfirmItem, 10, SpringLayout.EAST, sProcessItem);
     	sLayout.putConstraint(SpringLayout.WEST, sViewOrder, 10, SpringLayout.EAST, sConfirmItem);
     	sLayout.putConstraint(SpringLayout.WEST, sFinishOrder, 10, SpringLayout.EAST, sViewOrder);
     	sLayout.putConstraint(SpringLayout.WEST, sNewOrder, 10, SpringLayout.EAST, sFinishOrder);
     	sLayout.putConstraint(SpringLayout.WEST, sExit, 10, SpringLayout.EAST, sNewOrder);
     	
     	sPanel.setLayout(sLayout);
     }
 	
 	/**
 	 * Auto fills the Info, Subtotal, and if necessary the Quantity text boxes.
 	 * @param mCD The current CD object.
 	 */
 	private static void AutoComplete(CD mCD){
     	sItemInfoTF.setText(mCD.getInfo());
     	if(!sQuantityTF.getText().isEmpty())
     		sSubtotalTF.setText(String.valueOf(sStoreAction.getDisplaySubtotal(mCD, Integer.valueOf(sQuantityTF.getText()))));
     	else{
     		sQuantityTF.setText("1");
     		sSubtotalTF.setText(String.valueOf(sStoreAction.getDisplaySubtotal(mCD,1)));
     	}
     }
     
 	/**
 	 * Updates the items number of many of the labels based on the sCurrentOrderNumber.
 	 */
 	private static void RedrawLabels(){
     	sProcessItem.setText("Process Item #"+String.valueOf(sCurrentOrderNumber)+"");
     	sConfirmItem.setText("Confirm Item #"+String.valueOf(sCurrentOrderNumber)+"");
     	sIDL.setText("Enter CD ID for item #"+String.valueOf(sCurrentOrderNumber)+":");
     	sQuantityL.setText("Enter quantity for item #"+String.valueOf(sCurrentOrderNumber)+":");
     	sItemInfoL.setText("Item #"+String.valueOf(sCurrentOrderNumber)+" info:");
     	sSubtotalL.setText("Order subtotal for "+String.valueOf(sCurrentOrderNumber-1)+" item(s):");
     }
     
 	/**
 	 * Clears the text fields, reinitializes the StoreAction object, and redraws the labels.
 	 */
 	private static void NewOrderClick(){
 		new StoreWindow().StartThread();
     	sCurrentOrderNumber = 1;
 		
     	sIDTF.setText("");
 		sQuantityTF.setText("");
 		sItemInfoTF.setText("");
     	sSubtotalTF.setText("");
 		sNumItemsTF.setText("");
 		
 		RedrawLabels();
 		
 		sNumItemsTF.setEditable(true);
 		sConfirmItem.setEnabled(true);
 		sProcessItem.setEnabled(false);
     }
 
 	
 	/**
 	 * Defines a new class to execute the initialization of Store Action on its own thread.
 	 * @author david
 	 */
 	class NewThread extends Thread{
 		@Override
 		public void run() {
 			sStoreAction = new StoreAction();
 		}
 	}
 	
 	/**
 	 * Starts the thread.
 	 */
 	private void StartThread(){
 		new NewThread().start();
 	}
 }
 
 
