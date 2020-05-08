 package simple;
 
 import ie.ul.csis.cs4135.pcshop.factory.ComponentInterface;
 import ie.ul.csis.cs4135.pcshop.factory.Computer.ComputerComposite;
 import ie.ul.csis.cs4135.pcshop.factory.Computer.Desktop.Cpu;
 import ie.ul.csis.cs4135.pcshop.factory.Computer.Desktop.Monitor;
 import ie.ul.csis.cs4135.pcshop.factory.Computer.Desktop.Ram;
 import ie.ul.csis.cs4135.pcshop.OrderManager;
 import ie.ul.csis.cs4135.pcshop.ProductsEnum;
 import ie.ul.csis.cs4135.pcshop.componentDecorator.ComputerModificator;
 import ie.ul.csis.cs4135.pcshop.computerComponentInterfaces.RamInterface;
 import ie.ul.csis.cs4135.pcshop.factory.ComponentComposite;
 import ie.ul.csis.cs4135.pcshop.factory.ComponentInterface;
 import ie.ul.csis.cs4135.pcshop.factory.Computer.ComputerComposite;
 import ie.ul.csis.cs4135.pcshop.factory.Computer.Desktop.*;
 import ie.ul.csis.cs4135.pcshop.taxRegion.AbstractTaxState;
 import ie.ul.csis.cs4135.pcshop.taxRegion.TaxRegionEnum;
 import ie.ul.csis.cs4135.pcshop.taxRegion.TaxStateFactory;
 
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.*;
 import javax.swing.table.DefaultTableModel;
 
 public class SimpleGUI1 extends javax.swing.JPanel {
 
 	private DefaultTableModel model; 
 	private OrderManager orderManager;
 	private ProductsEnum[] allProducts;
 	private Object[] allRegions;
 	public SimpleGUI1 referenceToSelf;
 	public boolean useOnlineAPIcalls;
 	
 
     public SimpleGUI1() throws Exception {
     	//used as means of callbacks for child window
     	referenceToSelf = this;
     	useOnlineAPIcalls = false;
     	
     	//populate Enum lists
     	allProducts = ProductsEnum.values();
 
     	TaxRegionEnum[] localRegions  = TaxRegionEnum.values();
     	List<TaxRegionEnum> temp1= new ArrayList<TaxRegionEnum>();
     	
     	// also try populating it with a null object
     	temp1.add(0, null);
     	for (TaxRegionEnum enumEle : localRegions)
     		temp1.add(enumEle);
     	
     	allRegions = temp1.toArray();
     	
     	orderManager = new OrderManager(TaxRegionEnum.IRELAND);
 //    	orderManager.addProduct(ProductsEnum.COMPUTER_DESKTOP_GAMING);
 //    	orderManager.addProduct(ProductsEnum.COMPUTER_LAPTOP_OFFICE);
         initComponents();
         updateProductTabel();
         
         //make table custom sized and not editable
         productTable.getColumnModel().getColumn(0).setResizable(false);
         productTable.getColumnModel().getColumn(1).setResizable(false);
         productTable.getColumnModel().getColumn(0).setPreferredWidth(500);
         productTable.getColumnModel().getColumn(1).setPreferredWidth(0);
     }
 
     /*
     public void updateTaxRegion(){
     	try{
 	    	TaxRegionEnum taxRegion = (TaxRegionEnum) vatRegionCombobox.getSelectedItem();
 	    	orderManager.setTaxRegion(taxRegion);
 	    	updateTotals();
 	    	System.out.println("updateTaxRegion()");
     	}catch (Exception e) {
 			// TODO: handle exception
 		}
     }
     */
     public void calculateCurrency(){
     	try{
 	    	TaxStateFactory factory = new TaxStateFactory();
 	    	AbstractTaxState taxState;
 	    	String finalText = "", curCode, amount;
 	    	
 	    	TaxRegionEnum region = (TaxRegionEnum) vatRegionCombobox.getSelectedItem();
 	    	
 	    	if(region != null){	
 	    		
 	    		taxState = factory.getCalculator(region);
 		    	curCode = taxState.getCurrencyCode();
 	    		
 	    		if(useOnlineAPIcalls){
 			    	amount = orderManager.getPriceInOtherCurrency(region).toString();
 			    	finalText = curCode + " " + amount;  	
 			    //offline substitute for currency converting
 		    	}else{
 		    		Float ukRate= 1.2F, usaRate = 0.5F;
 		    		Float price = orderManager.getTotalPrice();
 		    		
 		    		switch (region) {
 					case UNITED_KINGDOM:	price = price * ukRate;		break;
 					case USA:				price = price * usaRate;	break;
 					default:				price = price;				break;
 					}
 	
 					finalText = curCode + " " + (price.toString());
 		    	}
 	    	}
 	    	currencyPriceValueLable.setText(finalText);	    	
     	}catch (Exception e) {
     		e.printStackTrace();
 		}	
     }
         
     public void updateTotals(){
     	setSubtotalLabel(orderManager.getSubTotalPrice());
     	setVatLabel(orderManager.getTaxes());
     	setTotalLabel(orderManager.getTotalPrice());
     }
     
     public void setSubtotalLabel(Float ammount){
     	subtotalValueLable.setText(ammount.toString());
     }
     
     public void setVatLabel(Float ammount){
     	VATValueLable.setText(ammount.toString());
     }
 
 	public void setTotalLabel(Float ammount){
 		totalValueLable.setText(ammount.toString());
 	}
     public void updateProductTabel(){
     	 List<ComponentInterface> listOfProducts = orderManager.getOrder();
     	 
     	 clearProductTable();
     	 
     	 for(ComponentInterface singleProduct : listOfProducts){
     		 addRowToProductTable(
     				 new Object[]{singleProduct.getBrandName(), singleProduct.getPrice()});
     	 }
     	 //update prices at bottom corner
     	 updateTotals();
     }
     
     public void addProductToOrderList(ComponentInterface product, JFrame panelRef){
     	orderManager.addExternalProduct(product);
     	closeAddProductWindow( panelRef);
     	updateProductTabel();
     	updateTotals();
     }
     
     private void closeAddProductWindow(JFrame panelRef) {
     	panelRef.setVisible(false);
         // If the frame is no longer needed, call dispose
     	panelRef.dispose(); 
 		
 	}
 
 	public void addRowToProductTable(Object[] array){
     	model.addRow(array);
     }
     
     public void clearProductTable(){
     	model.setRowCount(0);
     	//for(int i=0; i < model.getRowCount(); i++)
     	//	model.removeRow(i);
     }
     
     public static void createAndShowGUI() {
  /*       //Create and set up the window.
     	JFrame frame = new JFrame("ListDemo");
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
         //Create and set up the content pane.
         JComponent newContentPane = new SimpleGUI1();
         newContentPane.setOpaque(true); //content panes must be opaque
         frame.setContentPane(newContentPane);
 
         //Display the window.
         frame.pack();
         frame.setVisible(true);
 //*/
     }
 
     public void createAddProductWindow(){
     	System.out.println("Start sec Window");
 
         javax.swing.SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
				JFrame frame2 = new JFrame("ListDemo 2");
 				frame2.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
 				//Create and set up the content pane.
 				JComponent newContentPane;
 				try {
 						newContentPane = new SimpleGUI2(referenceToSelf);
 						newContentPane.setOpaque(true); //content panes must be opaque
 						frame2.setContentPane(newContentPane);
 						Dimension x = new Dimension(400, 550);
 						frame2.setPreferredSize(x);
 						//Display the window.
 						frame2.pack();
 						frame2.setVisible(true);
     
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
         });     
     }
 
     //@SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">
     private void initComponents() {
 
         jMenuBar1 = new javax.swing.JMenuBar();
         jMenu1 = new javax.swing.JMenu();
         jMenu2 = new javax.swing.JMenu();
         jMenuBar2 = new javax.swing.JMenuBar();
         jMenu3 = new javax.swing.JMenu();
         jMenu4 = new javax.swing.JMenu();
         jMenuBar3 = new javax.swing.JMenuBar();
         jMenu5 = new javax.swing.JMenu();
         jMenu6 = new javax.swing.JMenu();
         computerCorpLabel = new javax.swing.JLabel();
         customerDetailLabel = new javax.swing.JLabel();
         jScrollPane1 = new javax.swing.JScrollPane();
         customerDetailsTextbox = new javax.swing.JTextArea();
         orderMgrLabel = new javax.swing.JLabel();
         jSeparator1 = new javax.swing.JSeparator();
         jScrollPane2 = new javax.swing.JScrollPane();
         productTable = new javax.swing.JTable();
         jSeparator2 = new javax.swing.JSeparator();
         subtotalLable = new javax.swing.JLabel();
         VATLable = new javax.swing.JLabel();
         totalLable = new javax.swing.JLabel();
         subtotalValueLable = new javax.swing.JLabel();
         VATValueLable = new javax.swing.JLabel();
         totalValueLable = new javax.swing.JLabel();
         jSeparator3 = new javax.swing.JSeparator();
         addProductButton = new javax.swing.JButton();
         submitOrderButton = new javax.swing.JButton();
         vatRegionCombobox = new javax.swing.JComboBox();
         currencyPriceLable = new javax.swing.JLabel();
         currencyPriceValueLable =  new javax.swing.JLabel();
         curencyConverterLabel =  new javax.swing.JLabel();
         
         model = new DefaultTableModel(
 					new Object [][] {},	new String [] {	"Product", "Price"}
         );
         
         jMenu1.setText("File");
         jMenuBar1.add(jMenu1);
 
         jMenu2.setText("Edit");
         jMenuBar1.add(jMenu2);
 
         jMenu3.setText("File");
         jMenuBar2.add(jMenu3);
 
         jMenu4.setText("Edit");
         jMenuBar2.add(jMenu4);
 
         jMenu5.setText("File");
         jMenuBar3.add(jMenu5);
 
         jMenu6.setText("Edit");
         jMenuBar3.add(jMenu6);
 
         setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
         setForeground(new java.awt.Color(0, 204, 204));
 
         computerCorpLabel.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
         computerCorpLabel.setText("Computer Corp");
 
         customerDetailLabel.setText("CustomerDetails");
 
         customerDetailsTextbox.setColumns(20);
         customerDetailsTextbox.setRows(5);
         jScrollPane1.setViewportView(customerDetailsTextbox);
 
         orderMgrLabel.setText("Order Manager");
 
         
         
         productTable.setModel(model);
         
         jScrollPane2.setViewportView(productTable);
 
         subtotalLable.setText("Subtotal:");
 
         VATLable.setText("VAT:");
 
         totalLable.setText("Total:");
 
         subtotalValueLable.setText("_");
 
         VATValueLable.setText("_");
 
         totalValueLable.setText("_");
         
         currencyPriceLable.setText("Curency Converter:");
         currencyPriceValueLable.setText("");
         curencyConverterLabel.setText("Currency Converter");
         
         addProductButton.setText("Add a Product");
         addProductButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 addProductButtonActionPerformed(evt);
             }
         });
 
         submitOrderButton.setText("Submit an Order");
         submitOrderButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 submitOrderButtonActionPerformed(evt);
             }
         });
 
         vatRegionCombobox.setModel(new javax.swing.DefaultComboBoxModel(allRegions));
         
         vatRegionCombobox.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 vatRegionComboboxActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
         this.setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createSequentialGroup()
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(computerCorpLabel)
                             .addGroup(layout.createSequentialGroup()
                                 .addGap(10, 10, 10)
                                 .addComponent(orderMgrLabel)))
                         .addGap(185, 185, 185)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(layout.createSequentialGroup()
                                 .addComponent(customerDetailLabel)
                                 .addGap(238, 238, 238))
                             .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 326, Short.MAX_VALUE)))
                     .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                             .addGroup(layout.createSequentialGroup()
                                 .addComponent(vatRegionCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 402, Short.MAX_VALUE)
                                 .addComponent(VATLable))
                             .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                     .addGroup(layout.createSequentialGroup()
                                         .addComponent(currencyPriceLable)
                                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 43, Short.MAX_VALUE)
                                         .addComponent(currencyPriceValueLable)
                                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 418, Short.MAX_VALUE))
                                     .addGroup(layout.createSequentialGroup()
                                         .addComponent(curencyConverterLabel)
                                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                     .addComponent(subtotalLable, javax.swing.GroupLayout.Alignment.TRAILING)
                                     .addComponent(totalLable, javax.swing.GroupLayout.Alignment.TRAILING))))
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                             .addComponent(subtotalValueLable)
                             .addComponent(VATValueLable)
                             .addComponent(totalValueLable)))
                     .addComponent(jSeparator2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 619, Short.MAX_VALUE)
                     .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 619, Short.MAX_VALUE)
                     .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 619, Short.MAX_VALUE)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(addProductButton, javax.swing.GroupLayout.DEFAULT_SIZE, 148, Short.MAX_VALUE)
                         .addGap(323, 323, 323)
                         .addComponent(submitOrderButton, javax.swing.GroupLayout.DEFAULT_SIZE, 148, Short.MAX_VALUE))
                     .addComponent(jSeparator3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(computerCorpLabel)
                     .addComponent(customerDetailLabel))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(orderMgrLabel)
                     .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addGap(18, 18, 18)
                 .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(23, 23, 23)
                 .addComponent(jSeparator2, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(27, 27, 27)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(subtotalLable)
                     .addComponent(subtotalValueLable)
                     .addComponent(curencyConverterLabel))
                 .addGap(18, 18, 18)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(VATLable)
                     .addComponent(VATValueLable)
                     .addComponent(vatRegionCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(jSeparator3, javax.swing.GroupLayout.PREFERRED_SIZE, 10, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addGap(8, 8, 8)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(totalLable)
                     .addComponent(totalValueLable)
                     .addComponent(currencyPriceLable)
                     .addComponent(currencyPriceValueLable))
                 .addGap(73, 73, 73)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(addProductButton)
                     .addComponent(submitOrderButton))
                 .addContainerGap())
         );
     }// </editor-fold>
 
     private void addProductButtonActionPerformed(java.awt.event.ActionEvent evt) {
     	createAddProductWindow();
     }
 
     private void submitOrderButtonActionPerformed(java.awt.event.ActionEvent evt) {
     	updateTotals();
     }
 
     private void vatRegionComboboxActionPerformed(java.awt.event.ActionEvent evt) {
     	calculateCurrency();
     }
 
 
     // Variables declaration - do not modify
     private javax.swing.JLabel VATLable;
     private javax.swing.JLabel VATValueLable;
     private javax.swing.JButton addProductButton;
     private javax.swing.JLabel computerCorpLabel;
     private javax.swing.JLabel customerDetailLabel;
     private javax.swing.JTextArea customerDetailsTextbox;
     private javax.swing.JMenu jMenu1;
     private javax.swing.JMenu jMenu2;
     private javax.swing.JMenu jMenu3;
     private javax.swing.JMenu jMenu4;
     private javax.swing.JMenu jMenu5;
     private javax.swing.JMenu jMenu6;
     private javax.swing.JMenuBar jMenuBar1;
     private javax.swing.JMenuBar jMenuBar2;
     private javax.swing.JMenuBar jMenuBar3;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JScrollPane jScrollPane2;
     private javax.swing.JSeparator jSeparator1;
     private javax.swing.JSeparator jSeparator2;
     private javax.swing.JSeparator jSeparator3;
     private javax.swing.JLabel orderMgrLabel;
     private javax.swing.JTable productTable;
     private javax.swing.JButton submitOrderButton;
     private javax.swing.JLabel subtotalLable;
     private javax.swing.JLabel subtotalValueLable;
     private javax.swing.JLabel totalLable;
     private javax.swing.JLabel totalValueLable;
     private javax.swing.JComboBox vatRegionCombobox;
     private javax.swing.JLabel currencyPriceLable;
     private javax.swing.JLabel currencyPriceValueLable;
     private javax.swing.JLabel curencyConverterLabel;
     // End of variables declaration
 
 }
