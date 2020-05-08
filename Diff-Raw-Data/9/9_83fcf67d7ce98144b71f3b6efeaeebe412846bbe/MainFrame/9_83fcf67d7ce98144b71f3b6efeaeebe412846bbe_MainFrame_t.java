 //GREAT JORB! (this wasn't including in commit)
 
 import java.awt.EventQueue;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 import javax.swing.JLabel;
 import javax.swing.SwingConstants;
 import java.awt.Font;
 import javax.swing.JComboBox;
 //import net.miginfocom.swing.MigLayout;
 import javax.swing.JFormattedTextField;
 
 import java.awt.GridBagLayout;
 import java.awt.GridBagConstraints;
 import java.awt.Insets;
 import java.awt.CardLayout;
 import javax.swing.JButton;
 import javax.swing.JTextPane;
 import javax.swing.JSeparator;
 import javax.swing.JPasswordField;
 import javax.swing.JTabbedPane;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JTextField;
 //import net.miginfocom.swing.MigLayout;
 import javax.swing.JTable;
 import javax.swing.JScrollPane;
 import javax.swing.border.EtchedBorder;
 import javax.swing.border.SoftBevelBorder;
 import javax.swing.border.BevelBorder;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 
 import java.util.ArrayList;
 import java.util.Date;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.JTextArea;
 import javax.swing.border.LineBorder;
 import java.awt.Color;
 import javax.swing.DropMode;
 
 public class MainFrame extends JFrame {
 	private AMSOracleConnection con = null;
 	private JPanel contentPane;
 	private JPasswordField passwordField;
 	private JTextField inpq;
 	private JTextField instorecardexpd;
 	private JTextField returnReceiptId;
 	private JTextField returnUPC;
 	private JTextField custUName;
 	private JTextField custPW;
 	private JTextField custRegId;
 	private JTextField custRegPW;
 	private JTextField custRegName;
 	private JTextField custRegAddr;
 	private JTextField custPNum;
 	private JTextField inpupc;
 	private JTextField ccno;
 	private JTextField opqty;
 	private JTextField opls;
 	private JTextField opcardno;
 	private static ArrayList<String> savedInstoreItems;
 	private static JTextArea purchaseItems;
 	private static String dailySalesReport="";
 	private static Integer numberOfItems = 0;
 	private static ArrayList<String> instorePurchaseItems;
 	private JTextField dailySalesDate;
 	private static JTextArea dailySalesReportTextArea;
 	private JTextField topSellDate;
 	private JTextField topSellNum;
 	private String customerId = "";
 	private static ArrayList<String> onlineSearchItems;
 	private static ArrayList<String> onlinePurchaseItems;
 	private static Integer onlineItemCount=0;
 	private JTextField textField_2;
 	private JTextField opcardxd;
 	private JTextField opcat;
 	private static JTextArea onlineSearchTextArea;
 	private static JTextArea shoppingBasketTextArea;
 	private static JTextArea topSellTextArea;
 	private static Integer searchResultItemCount = 0;
 	private JTextField optitle;
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					
 					MainFrame frame = new MainFrame();
 					frame.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the frame.
 	 */
 	public MainFrame() {
 		final ArrayList<String> stockUpdate  = new ArrayList<String>();
 		instorePurchaseItems = new ArrayList<String>();
 		savedInstoreItems = new ArrayList<String>();
 		onlineSearchItems = new ArrayList<String>();
 		onlinePurchaseItems = new ArrayList<String>();
 		con = AMSOracleConnection.getInstance();
 		setTitle("AMS Database");
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setBounds(100, 100, 450, 300);
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(1, 1, 1, 1));
 		setContentPane(contentPane);
 		
 		JPanel login = new JPanel();
 		login.setBorder(new EmptyBorder(5, 5, 5, 5));
 		GridBagLayout gbl_login = new GridBagLayout();
 		gbl_login.columnWidths = new int[]{0, 414, 0};
 		gbl_login.rowHeights = new int[]{24, 24, 24, 24, 24, 24, 24, 0, 24, 24, 0};
 		gbl_login.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
 		gbl_login.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
 		login.setLayout(gbl_login);
 		
 		JLabel lblNewLabel = new JLabel("Username:");
 		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
 		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
 		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
 		gbc_lblNewLabel.fill = GridBagConstraints.BOTH;
 		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 0);
 		gbc_lblNewLabel.gridx = 1;
 		gbc_lblNewLabel.gridy = 1;
 		login.add(lblNewLabel, gbc_lblNewLabel);
 		
 		final JTextField userName = new JTextField();
 		GridBagConstraints gbc_userName = new GridBagConstraints();
 		gbc_userName.fill = GridBagConstraints.BOTH;
 		gbc_userName.insets = new Insets(0, 0, 5, 0);
 		gbc_userName.gridx = 1;
 		gbc_userName.gridy = 2;
 		login.add(userName, gbc_userName);
 		
 		JLabel lblPassword = new JLabel("Password:");
 		lblPassword.setFont(new Font("Tahoma", Font.BOLD, 11));
 		lblPassword.setHorizontalAlignment(SwingConstants.CENTER);
 		GridBagConstraints gbc_lblPassword = new GridBagConstraints();
 		gbc_lblPassword.fill = GridBagConstraints.BOTH;
 		gbc_lblPassword.insets = new Insets(0, 0, 5, 0);
 		gbc_lblPassword.gridx = 1;
 		gbc_lblPassword.gridy = 3;
 		login.add(lblPassword, gbc_lblPassword);
 		
 		passwordField = new JPasswordField();
 		GridBagConstraints gbc_passwordField = new GridBagConstraints();
 		gbc_passwordField.fill = GridBagConstraints.BOTH;
 		gbc_passwordField.insets = new Insets(0, 0, 5, 0);
 		gbc_passwordField.gridx = 1;
 		gbc_passwordField.gridy = 4;
 		login.add(passwordField, gbc_passwordField);
 		
 		JLabel lblNewLabel_1 = new JLabel("Specify Your Access Level:");
 		lblNewLabel_1.setFont(new Font("Tahoma", Font.BOLD, 11));
 		lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
 		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
 		gbc_lblNewLabel_1.fill = GridBagConstraints.BOTH;
 		gbc_lblNewLabel_1.insets = new Insets(0, 0, 5, 0);
 		gbc_lblNewLabel_1.gridx = 1;
 		gbc_lblNewLabel_1.gridy = 5;
 		login.add(lblNewLabel_1, gbc_lblNewLabel_1);
 		
 		final JComboBox AccessLevel = new JComboBox();
 		AccessLevel.setModel(new DefaultComboBoxModel(new String[] {"Customer", "Clerk", "Manager"}));
 		GridBagConstraints gbc_AccessLevel = new GridBagConstraints();
 		gbc_AccessLevel.fill = GridBagConstraints.BOTH;
 		gbc_AccessLevel.insets = new Insets(0, 0, 5, 0);
 		gbc_AccessLevel.gridx = 1;
 		gbc_AccessLevel.gridy = 6;
 		login.add(AccessLevel, gbc_AccessLevel);
 		
 		JButton btnSubmit = new JButton("Submit");
 		btnSubmit.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				if (con.connect(userName.getText(), 
 					     String.valueOf(passwordField.getPassword()))){
 					String access = (String) AccessLevel.getSelectedItem();
 					switchToNextCard(access);
 					// select next card based on accesslevel
 					
 					
 				}
 			}
 		});
 		GridBagConstraints gbc_btnSubmit = new GridBagConstraints();
 		gbc_btnSubmit.insets = new Insets(0, 0, 5, 0);
 		gbc_btnSubmit.gridx = 1;
 		gbc_btnSubmit.gridy = 7;
 		login.add(btnSubmit, gbc_btnSubmit);
 		
 		JSeparator separator_3 = new JSeparator();
 		GridBagConstraints gbc_separator_3 = new GridBagConstraints();
 		gbc_separator_3.insets = new Insets(0, 0, 0, 5);
 		gbc_separator_3.gridx = 0;
 		gbc_separator_3.gridy = 9;
 		login.add(separator_3, gbc_separator_3);
 		contentPane.setLayout(new CardLayout(0, 0));
 		contentPane.add(login, "name_1441150054636723");
 		
 		JTabbedPane clerkOperations = new JTabbedPane(JTabbedPane.TOP);
 		contentPane.add(clerkOperations, "name_1442560847488307");
 		
 		JPanel processPurchase = new JPanel();
 		processPurchase.setBorder(new EmptyBorder(10, 10, 10, 10));
 		clerkOperations.addTab("Purchase", null, processPurchase, null);
 		GridBagLayout gbl_processPurchase = new GridBagLayout();
 		gbl_processPurchase.columnWidths = new int[]{32, 82, 0, 0, 105, 0, 0};
 		gbl_processPurchase.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0};
 		gbl_processPurchase.columnWeights = new double[]{1.0, 1.0, 1.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
 		gbl_processPurchase.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
 		processPurchase.setLayout(gbl_processPurchase);
 		
 		JLabel label = new JLabel("UPC:");
 		GridBagConstraints gbc_label = new GridBagConstraints();
 		gbc_label.anchor = GridBagConstraints.EAST;
 		gbc_label.insets = new Insets(0, 0, 5, 5);
 		gbc_label.gridx = 0;
 		gbc_label.gridy = 0;
 		processPurchase.add(label, gbc_label);
 		
 		inpupc = new JTextField();
 		GridBagConstraints gbc_inpupc = new GridBagConstraints();
 		gbc_inpupc.gridwidth = 2;
 		gbc_inpupc.insets = new Insets(0, 0, 5, 5);
 		gbc_inpupc.fill = GridBagConstraints.HORIZONTAL;
 		gbc_inpupc.gridx = 1;
 		gbc_inpupc.gridy = 0;
 		processPurchase.add(inpupc, gbc_inpupc);
 		
 		JButton addpurchitem = new JButton("Add Item");
 		addpurchitem.addActionListener(new ActionListener() {
 			//TODO: Instore Purchase Additem Button
 			public void actionPerformed(ActionEvent arg0) {
 				String upc = inpupc.getText().trim();
 				Integer q = Integer.parseInt(inpq.getText().trim());
 				PurchaseOperations p = new PurchaseOperations();
 				if(p.isInStock(upc,q)){
 					System.out.println("Item is in stock!");
 					instorePurchaseItems.add(upc);
 					instorePurchaseItems.add(q.toString());
 					numberOfItems++;
 					//ensure items are added to table
 				}
 				
 			}
 		});
 		GridBagConstraints gbc_addpurchitem = new GridBagConstraints();
 		gbc_addpurchitem.insets = new Insets(0, 0, 5, 5);
 		gbc_addpurchitem.gridx = 4;
 		gbc_addpurchitem.gridy = 0;
 		processPurchase.add(addpurchitem, gbc_addpurchitem);
 		
 		JLabel lblQuantity_5 = new JLabel("Quantity:");
 		GridBagConstraints gbc_lblQuantity_5 = new GridBagConstraints();
 		gbc_lblQuantity_5.anchor = GridBagConstraints.EAST;
 		gbc_lblQuantity_5.insets = new Insets(0, 0, 5, 5);
 		gbc_lblQuantity_5.gridx = 0;
 		gbc_lblQuantity_5.gridy = 1;
 		processPurchase.add(lblQuantity_5, gbc_lblQuantity_5);
 		
 		inpq = new JTextField();
 		GridBagConstraints gbc_inpq = new GridBagConstraints();
 		gbc_inpq.gridwidth = 2;
 		gbc_inpq.insets = new Insets(0, 0, 5, 5);
 		gbc_inpq.fill = GridBagConstraints.HORIZONTAL;
 		gbc_inpq.gridx = 1;
 		gbc_inpq.gridy = 1;
 		processPurchase.add(inpq, gbc_inpq);
 		inpq.setColumns(10);
 		
 		JScrollPane scrollPane_4 = new JScrollPane();
 		GridBagConstraints gbc_scrollPane_4 = new GridBagConstraints();
 		gbc_scrollPane_4.gridheight = 2;
 		gbc_scrollPane_4.fill = GridBagConstraints.BOTH;
 		gbc_scrollPane_4.gridwidth = 6;
 		gbc_scrollPane_4.insets = new Insets(0, 0, 5, 0);
 		gbc_scrollPane_4.gridx = 0;
 		gbc_scrollPane_4.gridy = 2;
 		processPurchase.add(scrollPane_4, gbc_scrollPane_4);
 		
 		this.purchaseItems = new JTextArea();
 		scrollPane_4.setViewportView(purchaseItems);
 		purchaseItems.append("                    UPC               Price               Quantity\n=============================================================\n");
 		
 		JLabel lblCreditCardNumber = new JLabel("Credit Card Number:");
 		GridBagConstraints gbc_lblCreditCardNumber = new GridBagConstraints();
 		gbc_lblCreditCardNumber.gridwidth = 3;
 		gbc_lblCreditCardNumber.anchor = GridBagConstraints.EAST;
 		gbc_lblCreditCardNumber.fill = GridBagConstraints.VERTICAL;
 		gbc_lblCreditCardNumber.insets = new Insets(0, 0, 5, 5);
 		gbc_lblCreditCardNumber.gridx = 1;
 		gbc_lblCreditCardNumber.gridy = 4;
 		processPurchase.add(lblCreditCardNumber, gbc_lblCreditCardNumber);
 		
 		ccno = new JTextField();
 		ccno.setColumns(10);
 		GridBagConstraints gbc_ccno = new GridBagConstraints();
 		gbc_ccno.insets = new Insets(0, 0, 5, 5);
 		gbc_ccno.fill = GridBagConstraints.HORIZONTAL;
 		gbc_ccno.gridx = 4;
 		gbc_ccno.gridy = 4;
 		processPurchase.add(ccno, gbc_ccno);
 		
 		JLabel lblCardExpiryDate = new JLabel("Card Expiry Date:");
 		GridBagConstraints gbc_lblCardExpiryDate = new GridBagConstraints();
 		gbc_lblCardExpiryDate.gridwidth = 3;
 		gbc_lblCardExpiryDate.anchor = GridBagConstraints.EAST;
 		gbc_lblCardExpiryDate.fill = GridBagConstraints.VERTICAL;
 		gbc_lblCardExpiryDate.insets = new Insets(0, 0, 0, 5);
 		gbc_lblCardExpiryDate.gridx = 1;
 		gbc_lblCardExpiryDate.gridy = 5;
 		processPurchase.add(lblCardExpiryDate, gbc_lblCardExpiryDate);
 		
 		instorecardexpd = new JTextField();
 		instorecardexpd.setColumns(10);
 		GridBagConstraints gbc_instorecardexpd = new GridBagConstraints();
 		gbc_instorecardexpd.insets = new Insets(0, 0, 0, 5);
 		gbc_instorecardexpd.fill = GridBagConstraints.HORIZONTAL;
 		gbc_instorecardexpd.gridx = 4;
 		gbc_instorecardexpd.gridy = 5;
 		processPurchase.add(instorecardexpd, gbc_instorecardexpd);
 		
 		//TODO: INSTORE PURCHASE COMPLETE BUTTON
 		JButton btnCompletePurchase = new JButton("Complete Purchase");
 		btnCompletePurchase.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				PurchaseOperations p = new PurchaseOperations();
 				ItemOperations i = new ItemOperations();
 			
 				String expd = instorecardexpd.getText().trim();
 				String cno = ccno.getText().trim();
 				
 				if (p.completePurchase(cno, expd)){
 					System.out.println("Items Purchased!");
 				clearPurchaseList();
 				}
 				if (i.reduceStockForPurchase(instorePurchaseItems)){
 					System.out.println("Stock has been adjusted to reflect purchase");
 				}
 			}
 		});
 		GridBagConstraints gbc_btnCompletePurchase = new GridBagConstraints();
 		gbc_btnCompletePurchase.gridx = 5;
 		gbc_btnCompletePurchase.gridy = 5;
 		processPurchase.add(btnCompletePurchase, gbc_btnCompletePurchase);
 		
 		JPanel processReturn = new JPanel();
 		clerkOperations.addTab("Return", null, processReturn, null);
 		GridBagLayout gbl_processReturn = new GridBagLayout();
 		gbl_processReturn.columnWidths = new int[]{0, 0, 0, 0, 0};
 		gbl_processReturn.rowHeights = new int[]{35, 0, 0, 0, 0, 0, 0};
 		gbl_processReturn.columnWeights = new double[]{0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
 		gbl_processReturn.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
 		processReturn.setLayout(gbl_processReturn);
 		
 		JLabel lblNewLabel_8 = new JLabel("Return Item");
 		lblNewLabel_8.setFont(new Font("Tahoma", Font.BOLD, 11));
 		GridBagConstraints gbc_lblNewLabel_8 = new GridBagConstraints();
 		gbc_lblNewLabel_8.insets = new Insets(0, 0, 5, 5);
 		gbc_lblNewLabel_8.gridx = 1;
 		gbc_lblNewLabel_8.gridy = 0;
 		processReturn.add(lblNewLabel_8, gbc_lblNewLabel_8);
 		
 		JLabel lblReceiptid = new JLabel("ReceiptID:");
 		GridBagConstraints gbc_lblReceiptid = new GridBagConstraints();
 		gbc_lblReceiptid.insets = new Insets(0, 0, 5, 5);
 		gbc_lblReceiptid.anchor = GridBagConstraints.EAST;
 		gbc_lblReceiptid.gridx = 1;
 		gbc_lblReceiptid.gridy = 2;
 		processReturn.add(lblReceiptid, gbc_lblReceiptid);
 		
 		returnReceiptId = new JTextField();
 		GridBagConstraints gbc_returnReceiptId = new GridBagConstraints();
 		gbc_returnReceiptId.insets = new Insets(0, 0, 5, 5);
 		gbc_returnReceiptId.fill = GridBagConstraints.HORIZONTAL;
 		gbc_returnReceiptId.gridx = 2;
 		gbc_returnReceiptId.gridy = 2;
 		processReturn.add(returnReceiptId, gbc_returnReceiptId);
 		returnReceiptId.setColumns(10);
 		
 		JLabel lblUpc_2 = new JLabel("UPC:");
 		GridBagConstraints gbc_lblUpc_2 = new GridBagConstraints();
 		gbc_lblUpc_2.anchor = GridBagConstraints.EAST;
 		gbc_lblUpc_2.insets = new Insets(0, 0, 5, 5);
 		gbc_lblUpc_2.gridx = 1;
 		gbc_lblUpc_2.gridy = 3;
 		processReturn.add(lblUpc_2, gbc_lblUpc_2);
 		
 		returnUPC = new JTextField();
 		returnUPC.setColumns(10);
 		GridBagConstraints gbc_returnUPC = new GridBagConstraints();
 		gbc_returnUPC.insets = new Insets(0, 0, 5, 5);
 		gbc_returnUPC.fill = GridBagConstraints.HORIZONTAL;
 		gbc_returnUPC.gridx = 2;
 		gbc_returnUPC.gridy = 3;
 		processReturn.add(returnUPC, gbc_returnUPC);
 		
 		JButton btnProcessReturn = new JButton("Process Return");
 		btnProcessReturn.addActionListener(new ActionListener() {
 			//TODO: RETURN BUTTON
 			public void actionPerformed(ActionEvent arg0) {
 				String receiptid = returnReceiptId.getText().trim();
 				String retupc = returnUPC.getText().trim();
 				
 				ReturnOperations r = new ReturnOperations();
 				r.returnItem(receiptid, retupc);
 				//ReturnItemOperations ri = new ReturnItemOperations();
 				//Date rdate = new Date();
 				//java.sql.Date sqlDate = new java.sql.Date(rdate.getTime());
 //				int paymenttype = 0;
 //				int qty = 0;
 //				if(r.validateReturn(retupc,receiptid,paymenttype,qty)){
 //					switch(paymenttype){
 //					case (0):System.out.println("Cash Return");
 //					case (1):System.out.println("Credit Return");
 //					}
 //					
 //					if(r.insert(receiptid,sqlDate)){
 //						if(ri.insert(retupc, paymenttype)){
 ////							System.out.println("Return Successful");
 //						}
 //					}
 				}
 			});
 		
 			GridBagConstraints gbc_btnProcessReturn = new GridBagConstraints();
 			gbc_btnProcessReturn.insets = new Insets(0, 0, 5, 0);
 			gbc_btnProcessReturn.gridx = 3;
 			gbc_btnProcessReturn.gridy = 3;
 			processReturn.add(btnProcessReturn, gbc_btnProcessReturn);
 		
 		JTabbedPane managerOperations = new JTabbedPane(JTabbedPane.TOP);
 		contentPane.add(managerOperations, "name_1449473464875310");
 		
 		JPanel addItem = new JPanel();
 		addItem.setBorder(new EmptyBorder(15, 15, 15, 15));
 		managerOperations.addTab("Add Item to Store", null, addItem, null);
 		GridBagLayout gbl_addItem = new GridBagLayout();
 		gbl_addItem.columnWidths = new int[] {0, 10};
 		gbl_addItem.rowHeights = new int[] {0, 10, 0, 0, 0};
 		gbl_addItem.columnWeights = new double[]{0.0, 1.0};
 		gbl_addItem.rowWeights = new double[]{1.0, 1.0, 1.0, 0.0, 0.0};
 		addItem.setLayout(gbl_addItem);
 		
 		JLabel lblUpc_1 = new JLabel("UPC:");
 		GridBagConstraints gbc_lblUpc_1 = new GridBagConstraints();
 		gbc_lblUpc_1.insets = new Insets(0, 0, 5, 5);
 		gbc_lblUpc_1.gridx = 0;
 		gbc_lblUpc_1.gridy = 0;
 		addItem.add(lblUpc_1, gbc_lblUpc_1);
 		
 		final JTextField additemupc = new JTextField();
 		GridBagConstraints gbc_additemupc = new GridBagConstraints();
 		gbc_additemupc.insets = new Insets(0, 0, 5, 0);
 		gbc_additemupc.fill = GridBagConstraints.BOTH;
 		gbc_additemupc.gridx = 1;
 		gbc_additemupc.gridy = 0;
 		addItem.add(additemupc, gbc_additemupc);
 		
 		JLabel lblQuantity_1 = new JLabel("Quantity:");
 		GridBagConstraints gbc_lblQuantity_1 = new GridBagConstraints();
 		gbc_lblQuantity_1.insets = new Insets(0, 0, 5, 5);
 		gbc_lblQuantity_1.gridx = 0;
 		gbc_lblQuantity_1.gridy = 1;
 		addItem.add(lblQuantity_1, gbc_lblQuantity_1);
 		
 		final JTextField additemqty = new JTextField();
 		GridBagConstraints gbc_additemqty = new GridBagConstraints();
 		gbc_additemqty.insets = new Insets(0, 0, 5, 0);
 		gbc_additemqty.fill = GridBagConstraints.BOTH;
 		gbc_additemqty.gridx = 1;
 		gbc_additemqty.gridy = 1;
 		addItem.add(additemqty, gbc_additemqty);
 		
 		JLabel lblPrice = new JLabel("Price (Optional):");
 		GridBagConstraints gbc_lblPrice = new GridBagConstraints();
 		gbc_lblPrice.insets = new Insets(0, 0, 5, 5);
 		gbc_lblPrice.gridx = 0;
 		gbc_lblPrice.gridy = 2;
 		addItem.add(lblPrice, gbc_lblPrice);
 		
 		final JTextField additemprice = new JTextField();
 		GridBagConstraints gbc_additemprice = new GridBagConstraints();
 		gbc_additemprice.insets = new Insets(0, 0, 5, 0);
 		gbc_additemprice.fill = GridBagConstraints.BOTH;
 		gbc_additemprice.gridx = 1;
 		gbc_additemprice.gridy = 2;
 		addItem.add(additemprice, gbc_additemprice);
 		
 		JButton btnAddItem = new JButton("Add Item");
 		//TODO: Manager AddItem button
 		btnAddItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
				Double priceDouble;
 				ItemOperations i = new ItemOperations();
 				String upcString = additemupc.getText();
 				Integer qtyString = Integer.parseInt(additemqty.getText());
				if (additemprice.getText() == null || additemprice.getText().isEmpty()) {
					priceDouble = 0.0;
				}
				else {
					priceDouble = Double.parseDouble(additemprice.getText());
				}
 				i.managerAddItem(upcString, qtyString, priceDouble);
 			}
 		});
 		GridBagConstraints gbc_btnAddItem = new GridBagConstraints();
 		gbc_btnAddItem.insets = new Insets(0, 0, 5, 0);
 		gbc_btnAddItem.gridx = 1;
 		gbc_btnAddItem.gridy = 3;
 		addItem.add(btnAddItem, gbc_btnAddItem);
 		
 		JSeparator separator_2 = new JSeparator();
 		GridBagConstraints gbc_separator_2 = new GridBagConstraints();
 		gbc_separator_2.gridx = 1;
 		gbc_separator_2.gridy = 4;
 		addItem.add(separator_2, gbc_separator_2);
 		
 		JPanel processDelivery = new JPanel();
 		processDelivery.setBorder(new EmptyBorder(15, 15, 15, 15));
 		managerOperations.addTab("Process Delivery", null, processDelivery, null);
 
 		GridBagLayout gbl_processDelivery = new GridBagLayout();
 		gbl_processDelivery.columnWidths = new int[]{1, 0, 0, 0, 0, 0};
 		gbl_processDelivery.rowHeights = new int[]{1, 0, 0, 0, 0};
 		gbl_processDelivery.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
 		gbl_processDelivery.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
 		processDelivery.setLayout(gbl_processDelivery);
 		
 		JLabel lblNewLabel_2 = new JLabel("Enter Order ReceiptID:");
 		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
 		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
 		gbc_lblNewLabel_2.fill = GridBagConstraints.BOTH;
 		gbc_lblNewLabel_2.gridx = 0;
 		gbc_lblNewLabel_2.gridy = 0;
 		processDelivery.add(lblNewLabel_2, gbc_lblNewLabel_2);
 		
 		final JTextField delivReceiptId = new JTextField();
 		GridBagConstraints gbc_delivReceiptId = new GridBagConstraints();
 		gbc_delivReceiptId.insets = new Insets(0, 0, 5, 5);
 		gbc_delivReceiptId.fill = GridBagConstraints.BOTH;
 		gbc_delivReceiptId.gridx = 1;
 		gbc_delivReceiptId.gridy = 0;
 		processDelivery.add(delivReceiptId, gbc_delivReceiptId);
 		
 		JLabel lblEnterDeliveryDate = new JLabel("Enter Delivery Date:");
 		GridBagConstraints gbc_lblEnterDeliveryDate = new GridBagConstraints();
 		gbc_lblEnterDeliveryDate.insets = new Insets(0, 0, 5, 5);
 		gbc_lblEnterDeliveryDate.fill = GridBagConstraints.BOTH;
 		gbc_lblEnterDeliveryDate.gridx = 0;
 		gbc_lblEnterDeliveryDate.gridy = 1;
 		processDelivery.add(lblEnterDeliveryDate, gbc_lblEnterDeliveryDate);
 		
 		final JTextField delivDate = new JTextField();
 		GridBagConstraints gbc_delivDate = new GridBagConstraints();
 		gbc_delivDate.insets = new Insets(0, 0, 5, 5);
 		gbc_delivDate.fill = GridBagConstraints.BOTH;
 		gbc_delivDate.gridx = 1;
 		gbc_delivDate.gridy = 1;
 		processDelivery.add(delivDate, gbc_delivDate);
 		
 		JButton setdelivdate = new JButton("Set Delivery Date");
 		setdelivdate.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				PurchaseOperations p = new PurchaseOperations();
 				String stringdate = delivDate.getText().trim();
 				String ridstring = delivReceiptId.getText();
 				int ridint = Integer.parseInt(ridstring);
 				p.updateDeliveryDate(ridint, stringdate);
 			}
 		});
 		
 		JLabel lblUseFormatDdmmyy = new JLabel("Use Format dd/MM/yy");
 		GridBagConstraints gbc_lblUseFormatDdmmyy = new GridBagConstraints();
 		gbc_lblUseFormatDdmmyy.insets = new Insets(0, 0, 5, 5);
 		gbc_lblUseFormatDdmmyy.gridx = 2;
 		gbc_lblUseFormatDdmmyy.gridy = 1;
 		processDelivery.add(lblUseFormatDdmmyy, gbc_lblUseFormatDdmmyy);
 		GridBagConstraints gbc_setdelivdate = new GridBagConstraints();
 		gbc_setdelivdate.insets = new Insets(0, 0, 0, 5);
 		gbc_setdelivdate.fill = GridBagConstraints.BOTH;
 		gbc_setdelivdate.gridx = 1;
 		gbc_setdelivdate.gridy = 3;
 		processDelivery.add(setdelivdate, gbc_setdelivdate);
 		
 		JPanel dailySalesReportTab = new JPanel();
 		managerOperations.addTab("Daily Sales Report", null, dailySalesReportTab, null);
 		GridBagLayout gbl_dailySalesReportTab = new GridBagLayout();
 		gbl_dailySalesReportTab.columnWidths = new int[]{0, 232, 0, 0, 0};
 		gbl_dailySalesReportTab.rowHeights = new int[]{32, 0, 0, 0, 0};
 		gbl_dailySalesReportTab.columnWeights = new double[]{0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
 		gbl_dailySalesReportTab.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
 		dailySalesReportTab.setLayout(gbl_dailySalesReportTab);
 		
 		JLabel lblDailySalesReport = new JLabel("Daily Sales Report");
 		lblDailySalesReport.setFont(new Font("Tahoma", Font.BOLD, 11));
 		GridBagConstraints gbc_lblDailySalesReport = new GridBagConstraints();
 		gbc_lblDailySalesReport.anchor = GridBagConstraints.WEST;
 		gbc_lblDailySalesReport.insets = new Insets(0, 0, 5, 5);
 		gbc_lblDailySalesReport.gridx = 1;
 		gbc_lblDailySalesReport.gridy = 0;
 		dailySalesReportTab.add(lblDailySalesReport, gbc_lblDailySalesReport);
 		
 		JLabel lblDdmmyy = new JLabel("dd-mm-yy");
 		GridBagConstraints gbc_lblDdmmyy = new GridBagConstraints();
 		gbc_lblDdmmyy.insets = new Insets(0, 0, 5, 5);
 		gbc_lblDdmmyy.gridx = 2;
 		gbc_lblDdmmyy.gridy = 0;
 		dailySalesReportTab.add(lblDdmmyy, gbc_lblDdmmyy);
 		
 		JLabel lblSalesDate = new JLabel("Sales Date:");
 		GridBagConstraints gbc_lblSalesDate = new GridBagConstraints();
 		gbc_lblSalesDate.insets = new Insets(0, 0, 5, 5);
 		gbc_lblSalesDate.anchor = GridBagConstraints.EAST;
 		gbc_lblSalesDate.gridx = 1;
 		gbc_lblSalesDate.gridy = 1;
 		dailySalesReportTab.add(lblSalesDate, gbc_lblSalesDate);
 		
 		dailySalesDate = new JTextField();
 		GridBagConstraints gbc_dailySalesDate = new GridBagConstraints();
 		gbc_dailySalesDate.insets = new Insets(0, 0, 5, 5);
 		gbc_dailySalesDate.fill = GridBagConstraints.HORIZONTAL;
 		gbc_dailySalesDate.gridx = 2;
 		gbc_dailySalesDate.gridy = 1;
 		dailySalesReportTab.add(dailySalesDate, gbc_dailySalesDate);
 		dailySalesDate.setColumns(10);
 		
 		JButton btnDisplayReport = new JButton("Display Report");
 		//TODO: Daily Sales Report Button
 		btnDisplayReport.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				dailySalesReportTextArea.setText("");
 				String s = dailySalesDate.getText().trim();
 				ItemOperations i = new ItemOperations();
 				if (i.dailySalesReportGUI(s)){
 					showDailySalesReport(dailySalesReport);
 					System.out.println("Daily Sales Report Should Be Displayed in GUI");
 				}
 			}
 		});
 		GridBagConstraints gbc_btnDisplayReport = new GridBagConstraints();
 		gbc_btnDisplayReport.insets = new Insets(0, 0, 5, 5);
 		gbc_btnDisplayReport.gridx = 2;
 		gbc_btnDisplayReport.gridy = 2;
 		dailySalesReportTab.add(btnDisplayReport, gbc_btnDisplayReport);
 		
 		JScrollPane scrollPane_3 = new JScrollPane();
 		GridBagConstraints gbc_scrollPane_3 = new GridBagConstraints();
 		gbc_scrollPane_3.fill = GridBagConstraints.BOTH;
 		gbc_scrollPane_3.gridwidth = 4;
 		gbc_scrollPane_3.gridx = 0;
 		gbc_scrollPane_3.gridy = 3;
 		dailySalesReportTab.add(scrollPane_3, gbc_scrollPane_3);
 		
 		dailySalesReportTextArea = new JTextArea();
 		scrollPane_3.setViewportView(dailySalesReportTextArea);
 		
 		JPanel topSellingItemsTab = new JPanel();
 		topSellingItemsTab.setBorder(new EmptyBorder(10, 10, 10, 10));
 		managerOperations.addTab("Top Selling Items", null, topSellingItemsTab, null);
 		GridBagLayout gbl_topSellingItemsTab = new GridBagLayout();
 		gbl_topSellingItemsTab.columnWidths = new int[]{156, 0, 74, 46, 0};
 		gbl_topSellingItemsTab.rowHeights = new int[]{14, 0, 0, 0};
 		gbl_topSellingItemsTab.columnWeights = new double[]{1.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
 		gbl_topSellingItemsTab.rowWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
 		topSellingItemsTab.setLayout(gbl_topSellingItemsTab);
 		
 		JLabel lblNewLabel_3 = new JLabel("Top Selling Items Report");
 		lblNewLabel_3.setFont(new Font("Tahoma", Font.BOLD, 11));
 		GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
 		gbc_lblNewLabel_3.insets = new Insets(0, 0, 5, 5);
 		gbc_lblNewLabel_3.anchor = GridBagConstraints.NORTHWEST;
 		gbc_lblNewLabel_3.gridx = 0;
 		gbc_lblNewLabel_3.gridy = 0;
 		topSellingItemsTab.add(lblNewLabel_3, gbc_lblNewLabel_3);
 		
 		JLabel lblDate = new JLabel("Date:");
 		GridBagConstraints gbc_lblDate = new GridBagConstraints();
 		gbc_lblDate.anchor = GridBagConstraints.EAST;
 		gbc_lblDate.insets = new Insets(0, 0, 5, 5);
 		gbc_lblDate.gridx = 1;
 		gbc_lblDate.gridy = 0;
 		topSellingItemsTab.add(lblDate, gbc_lblDate);
 		
 		topSellDate = new JTextField();
 		GridBagConstraints gbc_topSellDate = new GridBagConstraints();
 		gbc_topSellDate.insets = new Insets(0, 0, 5, 5);
 		gbc_topSellDate.fill = GridBagConstraints.HORIZONTAL;
 		gbc_topSellDate.gridx = 2;
 		gbc_topSellDate.gridy = 0;
 		topSellingItemsTab.add(topSellDate, gbc_topSellDate);
 		topSellDate.setColumns(10);
 		
 		JLabel lblDdmmyy_1 = new JLabel("dd-MM-yy");
 		GridBagConstraints gbc_lblDdmmyy_1 = new GridBagConstraints();
 		gbc_lblDdmmyy_1.insets = new Insets(0, 0, 5, 0);
 		gbc_lblDdmmyy_1.gridx = 3;
 		gbc_lblDdmmyy_1.gridy = 0;
 		topSellingItemsTab.add(lblDdmmyy_1, gbc_lblDdmmyy_1);
 		
 		JLabel lblNumberOfItems = new JLabel("Number of Items:");
 		GridBagConstraints gbc_lblNumberOfItems = new GridBagConstraints();
 		gbc_lblNumberOfItems.anchor = GridBagConstraints.WEST;
 		gbc_lblNumberOfItems.insets = new Insets(0, 0, 5, 5);
 		gbc_lblNumberOfItems.gridx = 1;
 		gbc_lblNumberOfItems.gridy = 1;
 		topSellingItemsTab.add(lblNumberOfItems, gbc_lblNumberOfItems);
 		
 		topSellNum = new JTextField();
 		GridBagConstraints gbc_topSellNum = new GridBagConstraints();
 		gbc_topSellNum.insets = new Insets(0, 0, 5, 5);
 		gbc_topSellNum.fill = GridBagConstraints.HORIZONTAL;
 		gbc_topSellNum.gridx = 2;
 		gbc_topSellNum.gridy = 1;
 		topSellingItemsTab.add(topSellNum, gbc_topSellNum);
 		topSellNum.setColumns(10);
 		
 		JButton topSellingButton = new JButton("Submit");
 		topSellingButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 			topSellTextArea.setText("");
 			ItemOperations i = new ItemOperations();
 			String sd = topSellDate.getText().trim();
 			Integer n = Integer.parseInt(topSellNum.getText());
 			if(i.topSellingItemsReportGUI(sd, n)){
 				System.out.println("Report should be displayed in GUI");
 			}
 			}
 		});
 		GridBagConstraints gbc_topSellingButton = new GridBagConstraints();
 		gbc_topSellingButton.insets = new Insets(0, 0, 5, 0);
 		gbc_topSellingButton.gridx = 3;
 		gbc_topSellingButton.gridy = 1;
 		topSellingItemsTab.add(topSellingButton, gbc_topSellingButton);
 		
 		JScrollPane scrollPane_1 = new JScrollPane();
 		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
 		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
 		gbc_scrollPane_1.gridwidth = 4;
 		gbc_scrollPane_1.gridx = 0;
 		gbc_scrollPane_1.gridy = 2;
 		topSellingItemsTab.add(scrollPane_1, gbc_scrollPane_1);
 		
 		topSellTextArea = new JTextArea();
 		scrollPane_1.setViewportView(topSellTextArea);
 		
 		final JPanel customerOperations = new JPanel();
 		contentPane.add(customerOperations, "name_1452058092586835");
 		customerOperations.setLayout(new CardLayout(0, 0));
 		
 		JPanel customerLogin = new JPanel();
 		customerLogin.setBorder(new LineBorder(new Color(0, 0, 0)));
 		customerOperations.add(customerLogin, "name_1546903163253674");
 		GridBagLayout gbl_customerLogin = new GridBagLayout();
 		gbl_customerLogin.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0};
 		gbl_customerLogin.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
 		gbl_customerLogin.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, Double.MIN_VALUE};
 		gbl_customerLogin.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
 		customerLogin.setLayout(gbl_customerLogin);
 		
 		JLabel lblCustomerLogin = new JLabel("Customer Login");
 		lblCustomerLogin.setFont(new Font("Tahoma", Font.BOLD, 11));
 		GridBagConstraints gbc_lblCustomerLogin = new GridBagConstraints();
 		gbc_lblCustomerLogin.insets = new Insets(0, 0, 5, 5);
 		gbc_lblCustomerLogin.gridx = 1;
 		gbc_lblCustomerLogin.gridy = 1;
 		customerLogin.add(lblCustomerLogin, gbc_lblCustomerLogin);
 		
 		JLabel lblUsername = new JLabel("Username:");
 		GridBagConstraints gbc_lblUsername = new GridBagConstraints();
 		gbc_lblUsername.anchor = GridBagConstraints.EAST;
 		gbc_lblUsername.insets = new Insets(0, 0, 5, 5);
 		gbc_lblUsername.gridx = 2;
 		gbc_lblUsername.gridy = 4;
 		customerLogin.add(lblUsername, gbc_lblUsername);
 		
 		custUName = new JTextField();
 		GridBagConstraints gbc_custUName = new GridBagConstraints();
 		gbc_custUName.gridwidth = 2;
 		gbc_custUName.insets = new Insets(0, 0, 5, 5);
 		gbc_custUName.fill = GridBagConstraints.HORIZONTAL;
 		gbc_custUName.gridx = 3;
 		gbc_custUName.gridy = 4;
 		customerLogin.add(custUName, gbc_custUName);
 		custUName.setColumns(10);
 		
 		JLabel lblPassword_1 = new JLabel("Password:");
 		GridBagConstraints gbc_lblPassword_1 = new GridBagConstraints();
 		gbc_lblPassword_1.anchor = GridBagConstraints.EAST;
 		gbc_lblPassword_1.insets = new Insets(0, 0, 5, 5);
 		gbc_lblPassword_1.gridx = 2;
 		gbc_lblPassword_1.gridy = 5;
 		customerLogin.add(lblPassword_1, gbc_lblPassword_1);
 		
 		custPW = new JTextField();
 		GridBagConstraints gbc_custPW = new GridBagConstraints();
 		gbc_custPW.gridwidth = 2;
 		gbc_custPW.insets = new Insets(0, 0, 5, 5);
 		gbc_custPW.fill = GridBagConstraints.HORIZONTAL;
 		gbc_custPW.gridx = 3;
 		gbc_custPW.gridy = 5;
 		customerLogin.add(custPW, gbc_custPW);
 		custPW.setColumns(10);
 		
 		JButton btnSubmit_1 = new JButton("Submit");
 		
 			//TODO: submit operations to login customer
 			btnSubmit_1.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					System.out.println("Submit was pressed")	;	
 					CustomerOperations c = new CustomerOperations();
 					if (c.login(custUName.getText().trim(), custPW.getText().trim())){
 						setCustomerId(custUName.getText().trim());
 						CardLayout cl = (CardLayout) customerOperations.getLayout();
 						 cl.show(customerOperations, "name_1452390541607518");
 					}
 				}
 			});
 			GridBagConstraints gbc_btnSubmit_1 = new GridBagConstraints();
 			gbc_btnSubmit_1.insets = new Insets(0, 0, 5, 5);
 			gbc_btnSubmit_1.gridx = 3;
 			gbc_btnSubmit_1.gridy = 6;
 			customerLogin.add(btnSubmit_1, gbc_btnSubmit_1);
 		
 		JButton btnRegisterNow = new JButton("Register Now");
 		btnRegisterNow.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				CardLayout cl = (CardLayout) customerOperations.getLayout();
 				cl.show(customerOperations,"name_1452387567243092");
 			}
 		});
 		
 		JLabel lblNotRegistered = new JLabel("Not Registered?");
 		GridBagConstraints gbc_lblNotRegistered = new GridBagConstraints();
 		gbc_lblNotRegistered.insets = new Insets(0, 0, 5, 5);
 		gbc_lblNotRegistered.gridx = 6;
 		gbc_lblNotRegistered.gridy = 7;
 		customerLogin.add(lblNotRegistered, gbc_lblNotRegistered);
 		GridBagConstraints gbc_btnRegisterNow = new GridBagConstraints();
 		gbc_btnRegisterNow.insets = new Insets(0, 0, 5, 5);
 		gbc_btnRegisterNow.gridx = 6;
 		gbc_btnRegisterNow.gridy = 8;
 		customerLogin.add(btnRegisterNow, gbc_btnRegisterNow);
 		
 		JPanel registerCustomer = new JPanel();
 		registerCustomer.setBorder(new SoftBevelBorder(BevelBorder.RAISED, null, null, null, null));
 		customerOperations.add(registerCustomer, "name_1452387567243092");
 		GridBagLayout gbl_registerCustomer = new GridBagLayout();
 		gbl_registerCustomer.columnWidths = new int[]{0, 0, 0, 0, 0, 0, 0};
 		gbl_registerCustomer.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
 		gbl_registerCustomer.columnWeights = new double[]{0.0, 1.0, 1.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
 		gbl_registerCustomer.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
 		registerCustomer.setLayout(gbl_registerCustomer);
 		
 		JLabel lblCustomerRegistration = new JLabel("Customer Registration");
 		lblCustomerRegistration.setFont(new Font("Tahoma", Font.BOLD, 11));
 		GridBagConstraints gbc_lblCustomerRegistration = new GridBagConstraints();
 		gbc_lblCustomerRegistration.insets = new Insets(0, 0, 5, 5);
 		gbc_lblCustomerRegistration.gridx = 2;
 		gbc_lblCustomerRegistration.gridy = 0;
 		registerCustomer.add(lblCustomerRegistration, gbc_lblCustomerRegistration);
 		
 		JLabel lblNewLabel_6 = new JLabel("Customer ID:");
 		GridBagConstraints gbc_lblNewLabel_6 = new GridBagConstraints();
 		gbc_lblNewLabel_6.insets = new Insets(0, 0, 5, 5);
 		gbc_lblNewLabel_6.gridx = 1;
 		gbc_lblNewLabel_6.gridy = 2;
 		registerCustomer.add(lblNewLabel_6, gbc_lblNewLabel_6);
 		
 		custRegId = new JTextField();
 		GridBagConstraints gbc_custRegId = new GridBagConstraints();
 		gbc_custRegId.insets = new Insets(0, 0, 5, 5);
 		gbc_custRegId.fill = GridBagConstraints.HORIZONTAL;
 		gbc_custRegId.gridx = 2;
 		gbc_custRegId.gridy = 2;
 		registerCustomer.add(custRegId, gbc_custRegId);
 		custRegId.setColumns(10);
 		
 		JLabel lblPassword_2 = new JLabel("Password:");
 		GridBagConstraints gbc_lblPassword_2 = new GridBagConstraints();
 		gbc_lblPassword_2.anchor = GridBagConstraints.EAST;
 		gbc_lblPassword_2.insets = new Insets(0, 0, 5, 5);
 		gbc_lblPassword_2.gridx = 1;
 		gbc_lblPassword_2.gridy = 3;
 		registerCustomer.add(lblPassword_2, gbc_lblPassword_2);
 		
 		custRegPW = new JTextField();
 		GridBagConstraints gbc_custRegPW = new GridBagConstraints();
 		gbc_custRegPW.insets = new Insets(0, 0, 5, 5);
 		gbc_custRegPW.fill = GridBagConstraints.HORIZONTAL;
 		gbc_custRegPW.gridx = 2;
 		gbc_custRegPW.gridy = 3;
 		registerCustomer.add(custRegPW, gbc_custRegPW);
 		custRegPW.setColumns(10);
 		
 		JLabel lblNewLabel_4 = new JLabel("Name:");
 		GridBagConstraints gbc_lblNewLabel_4 = new GridBagConstraints();
 		gbc_lblNewLabel_4.anchor = GridBagConstraints.EAST;
 		gbc_lblNewLabel_4.insets = new Insets(0, 0, 5, 5);
 		gbc_lblNewLabel_4.gridx = 1;
 		gbc_lblNewLabel_4.gridy = 4;
 		registerCustomer.add(lblNewLabel_4, gbc_lblNewLabel_4);
 		
 		custRegName = new JTextField();
 		GridBagConstraints gbc_custRegName = new GridBagConstraints();
 		gbc_custRegName.insets = new Insets(0, 0, 5, 5);
 		gbc_custRegName.fill = GridBagConstraints.HORIZONTAL;
 		gbc_custRegName.gridx = 2;
 		gbc_custRegName.gridy = 4;
 		registerCustomer.add(custRegName, gbc_custRegName);
 		custRegName.setColumns(10);
 		
 		JLabel lblAddress = new JLabel("Address:");
 		GridBagConstraints gbc_lblAddress = new GridBagConstraints();
 		gbc_lblAddress.anchor = GridBagConstraints.EAST;
 		gbc_lblAddress.insets = new Insets(0, 0, 5, 5);
 		gbc_lblAddress.gridx = 1;
 		gbc_lblAddress.gridy = 5;
 		registerCustomer.add(lblAddress, gbc_lblAddress);
 		
 		custRegAddr = new JTextField();
 		GridBagConstraints gbc_custRegAddr = new GridBagConstraints();
 		gbc_custRegAddr.insets = new Insets(0, 0, 5, 5);
 		gbc_custRegAddr.fill = GridBagConstraints.HORIZONTAL;
 		gbc_custRegAddr.gridx = 2;
 		gbc_custRegAddr.gridy = 5;
 		registerCustomer.add(custRegAddr, gbc_custRegAddr);
 		custRegAddr.setColumns(10);
 		
 		JLabel lblNewLabel_5 = new JLabel("Phone Number:");
 		GridBagConstraints gbc_lblNewLabel_5 = new GridBagConstraints();
 		gbc_lblNewLabel_5.anchor = GridBagConstraints.EAST;
 		gbc_lblNewLabel_5.insets = new Insets(0, 0, 5, 5);
 		gbc_lblNewLabel_5.gridx = 1;
 		gbc_lblNewLabel_5.gridy = 6;
 		registerCustomer.add(lblNewLabel_5, gbc_lblNewLabel_5);
 		
 		custPNum = new JTextField();
 		GridBagConstraints gbc_custPNum = new GridBagConstraints();
 		gbc_custPNum.insets = new Insets(0, 0, 5, 5);
 		gbc_custPNum.fill = GridBagConstraints.HORIZONTAL;
 		gbc_custPNum.gridx = 2;
 		gbc_custPNum.gridy = 6;
 		registerCustomer.add(custPNum, gbc_custPNum);
 		custPNum.setColumns(10);
 		
 		JButton btnRegisterNow_1 = new JButton("Register Now");
 		btnRegisterNow_1.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				CustomerOperations c = new CustomerOperations();
 				if (c.insert(custRegId.getText(), custRegPW.getText(), custRegName.getText(), custRegAddr.getText(), custPNum.getText())){
 					System.out.println("Insert attempted");
 					goToOnlinePurchase();
 					System.out.println("Customer Insertion Successful");
 					setCustomerId(custRegId.getText().trim());
 				}
 				else System.out.println("Customer Insertion Error");
 			}
 
 			private void goToOnlinePurchase() {
 				
 				CardLayout cl = (CardLayout) customerOperations.getLayout();
 				 cl.show(customerOperations, "name_1452390541607518");
 			}
 		});
 		GridBagConstraints gbc_btnRegisterNow_1 = new GridBagConstraints();
 		gbc_btnRegisterNow_1.insets = new Insets(0, 0, 0, 5);
 		gbc_btnRegisterNow_1.gridx = 2;
 		gbc_btnRegisterNow_1.gridy = 8;
 		registerCustomer.add(btnRegisterNow_1, gbc_btnRegisterNow_1);
 		
 		JPanel onlinePurchase = new JPanel();
 		onlinePurchase.setBorder(new EmptyBorder(10, 10, 10, 10));
 		customerOperations.add(onlinePurchase, "name_1452390541607518");
 		GridBagLayout gbl_onlinePurchase = new GridBagLayout();
 		gbl_onlinePurchase.columnWidths = new int[] {15, 73, 85, 58, 0};
 		gbl_onlinePurchase.rowHeights = new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 20};
 		gbl_onlinePurchase.columnWeights = new double[]{0.0, 1.0, 1.0, 1.0, 1.0};
 		gbl_onlinePurchase.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
 		onlinePurchase.setLayout(gbl_onlinePurchase);
 		
 		JLabel label_1 = new JLabel("Item Search");
 		label_1.setFont(new Font("Tahoma", Font.BOLD, 12));
 		GridBagConstraints gbc_label_1 = new GridBagConstraints();
 		gbc_label_1.insets = new Insets(0, 0, 5, 5);
 		gbc_label_1.gridx = 0;
 		gbc_label_1.gridy = 0;
 		onlinePurchase.add(label_1, gbc_label_1);
 		
 		JLabel lblQuantity = new JLabel("Quantity");
 		GridBagConstraints gbc_lblQuantity = new GridBagConstraints();
 		gbc_lblQuantity.anchor = GridBagConstraints.EAST;
 		gbc_lblQuantity.insets = new Insets(0, 0, 5, 5);
 		gbc_lblQuantity.gridx = 1;
 		gbc_lblQuantity.gridy = 0;
 		onlinePurchase.add(lblQuantity, gbc_lblQuantity);
 		
 		opqty = new JTextField();
 		GridBagConstraints gbc_opqty = new GridBagConstraints();
 		gbc_opqty.insets = new Insets(0, 0, 5, 5);
 		gbc_opqty.fill = GridBagConstraints.HORIZONTAL;
 		gbc_opqty.gridx = 2;
 		gbc_opqty.gridy = 0;
 		onlinePurchase.add(opqty, gbc_opqty);
 		opqty.setColumns(10);
 		
 		JLabel opcatlbl = new JLabel("Category");
 		GridBagConstraints gbc_opcatlbl = new GridBagConstraints();
 		gbc_opcatlbl.anchor = GridBagConstraints.EAST;
 		gbc_opcatlbl.insets = new Insets(0, 0, 5, 5);
 		gbc_opcatlbl.gridx = 3;
 		gbc_opcatlbl.gridy = 0;
 		onlinePurchase.add(opcatlbl, gbc_opcatlbl);
 		
 		opcat = new JTextField();
 		GridBagConstraints gbc_opcat = new GridBagConstraints();
 		gbc_opcat.insets = new Insets(0, 0, 5, 5);
 		gbc_opcat.fill = GridBagConstraints.HORIZONTAL;
 		gbc_opcat.gridx = 4;
 		gbc_opcat.gridy = 0;
 		onlinePurchase.add(opcat, gbc_opcat);
 		opcat.setColumns(10);
 		
 		JLabel lblLeadingSingers = new JLabel("Leading Singers:");
 		GridBagConstraints gbc_lblLeadingSingers = new GridBagConstraints();
 		gbc_lblLeadingSingers.gridwidth = 2;
 		gbc_lblLeadingSingers.anchor = GridBagConstraints.EAST;
 		gbc_lblLeadingSingers.insets = new Insets(0, 0, 5, 5);
 		gbc_lblLeadingSingers.gridx = 0;
 		gbc_lblLeadingSingers.gridy = 1;
 		onlinePurchase.add(lblLeadingSingers, gbc_lblLeadingSingers);
 		
 		//TODO Online Search Add Item
 		JButton btnSearchItems = new JButton("Search Items");
 		btnSearchItems.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				onlineSearchTextArea.setText("");
 				Integer qty = Integer.parseInt(opqty.getText().trim());
 				String title = optitle.getText().trim();
 				String category = opcat.getText().trim();
 				String ls = opls.getText().trim();
 				PurchaseOperations p = new PurchaseOperations();
 				
 				if (p.onlinePurchaseItemSearch(title,category,ls,qty)){
 					System.out.println("Query was successful. Item should be displayed in search results");
 				}
 			}
 		});
 		
 		opls = new JTextField();
 		GridBagConstraints gbc_opls = new GridBagConstraints();
 		gbc_opls.insets = new Insets(0, 0, 5, 5);
 		gbc_opls.fill = GridBagConstraints.HORIZONTAL;
 		gbc_opls.gridx = 2;
 		gbc_opls.gridy = 1;
 		onlinePurchase.add(opls, gbc_opls);
 		opls.setColumns(10);
 		
 		JLabel lblNewLabel_7 = new JLabel("Title");
 		GridBagConstraints gbc_lblNewLabel_7 = new GridBagConstraints();
 		gbc_lblNewLabel_7.anchor = GridBagConstraints.EAST;
 		gbc_lblNewLabel_7.insets = new Insets(0, 0, 5, 5);
 		gbc_lblNewLabel_7.gridx = 3;
 		gbc_lblNewLabel_7.gridy = 1;
 		onlinePurchase.add(lblNewLabel_7, gbc_lblNewLabel_7);
 		
 		optitle = new JTextField();
 		optitle.setColumns(10);
 		GridBagConstraints gbc_optitle = new GridBagConstraints();
 		gbc_optitle.insets = new Insets(0, 0, 5, 5);
 		gbc_optitle.fill = GridBagConstraints.HORIZONTAL;
 		gbc_optitle.gridx = 4;
 		gbc_optitle.gridy = 1;
 		onlinePurchase.add(optitle, gbc_optitle);
 		GridBagConstraints gbc_btnSearchItems = new GridBagConstraints();
 		gbc_btnSearchItems.insets = new Insets(0, 0, 5, 0);
 		gbc_btnSearchItems.gridx = 5;
 		gbc_btnSearchItems.gridy = 1;
 		onlinePurchase.add(btnSearchItems, gbc_btnSearchItems);
 		
 		JLabel lblSearchItems = new JLabel("Search Items");
 		lblSearchItems.setFont(new Font("Tahoma", Font.BOLD, 11));
 		GridBagConstraints gbc_lblSearchItems = new GridBagConstraints();
 		gbc_lblSearchItems.gridwidth = 2;
 		gbc_lblSearchItems.anchor = GridBagConstraints.NORTHWEST;
 		gbc_lblSearchItems.insets = new Insets(0, 0, 5, 5);
 		gbc_lblSearchItems.gridx = 1;
 		gbc_lblSearchItems.gridy = 2;
 		onlinePurchase.add(lblSearchItems, gbc_lblSearchItems);
 		
 		JLabel lblShoppingBasket = new JLabel("Shopping Basket");
 		lblShoppingBasket.setFont(new Font("Tahoma", Font.BOLD, 11));
 		GridBagConstraints gbc_lblShoppingBasket = new GridBagConstraints();
 		gbc_lblShoppingBasket.gridwidth = 2;
 		gbc_lblShoppingBasket.insets = new Insets(0, 0, 5, 0);
 		gbc_lblShoppingBasket.gridx = 4;
 		gbc_lblShoppingBasket.gridy = 2;
 		onlinePurchase.add(lblShoppingBasket, gbc_lblShoppingBasket);
 		JScrollPane scrollPane_2 = new JScrollPane();
 		GridBagConstraints gbc_scrollPane_2 = new GridBagConstraints();
 		gbc_scrollPane_2.gridheight = 4;
 		gbc_scrollPane_2.gridwidth = 4;
 		gbc_scrollPane_2.insets = new Insets(0, 0, 5, 5);
 		gbc_scrollPane_2.fill = GridBagConstraints.BOTH;
 		gbc_scrollPane_2.gridx = 0;
 		gbc_scrollPane_2.gridy = 3;
 		onlinePurchase.add(scrollPane_2, gbc_scrollPane_2);
 		
 		onlineSearchTextArea = new JTextArea();
 		scrollPane_2.setViewportView(onlineSearchTextArea);
 		
 		JScrollPane scrollPane = new JScrollPane();
 		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
 		gbc_scrollPane.gridheight = 3;
 		gbc_scrollPane.gridwidth = 2;
 		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
 		gbc_scrollPane.fill = GridBagConstraints.BOTH;
 		gbc_scrollPane.gridx = 4;
 		gbc_scrollPane.gridy = 3;
 		onlinePurchase.add(scrollPane, gbc_scrollPane);
 		
 		shoppingBasketTextArea = new JTextArea();
 		scrollPane.setViewportView(shoppingBasketTextArea);
 		shoppingBasketTextArea.append(" UPC     Price     Quantity\n ----------------------------------");
 		
 		JLabel lblCardNumber = new JLabel("Card Number");
 		GridBagConstraints gbc_lblCardNumber = new GridBagConstraints();
 		gbc_lblCardNumber.insets = new Insets(0, 0, 5, 5);
 		gbc_lblCardNumber.anchor = GridBagConstraints.EAST;
 		gbc_lblCardNumber.gridx = 4;
 		gbc_lblCardNumber.gridy = 6;
 		onlinePurchase.add(lblCardNumber, gbc_lblCardNumber);
 		
 		opcardno = new JTextField();
 		GridBagConstraints gbc_opcardno = new GridBagConstraints();
 		gbc_opcardno.insets = new Insets(0, 0, 5, 0);
 		gbc_opcardno.fill = GridBagConstraints.HORIZONTAL;
 		gbc_opcardno.gridx = 5;
 		gbc_opcardno.gridy = 6;
 		onlinePurchase.add(opcardno, gbc_opcardno);
 		opcardno.setColumns(10);
 		
 		JButton btnSubmitOrder = new JButton("Submit Order");
 		
 		//TODO: Submit Online Purchase Button
 		btnSubmitOrder.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				PurchaseOperations po = new PurchaseOperations();
 				String cnumber = opcardno.getText();
 				String cexpdate = opcardxd.getText();
 				String cardHolder = customerId;
 				if (po.completeOnlinePurchase(cnumber, cexpdate, cardHolder)) {
 					System.out.println("Online purchase made");
 				}
 				
 			}
 		});
 		
 		JLabel label_2 = new JLabel("UPC:");
 		GridBagConstraints gbc_label_2 = new GridBagConstraints();
 		gbc_label_2.anchor = GridBagConstraints.EAST;
 		gbc_label_2.insets = new Insets(0, 0, 5, 5);
 		gbc_label_2.gridx = 0;
 		gbc_label_2.gridy = 7;
 		onlinePurchase.add(label_2, gbc_label_2);
 		
 		textField_2 = new JTextField();
 		GridBagConstraints gbc_textField_2 = new GridBagConstraints();
 		gbc_textField_2.insets = new Insets(0, 0, 5, 5);
 		gbc_textField_2.fill = GridBagConstraints.HORIZONTAL;
 		gbc_textField_2.gridx = 1;
 		gbc_textField_2.gridy = 7;
 		onlinePurchase.add(textField_2, gbc_textField_2);
 		textField_2.setColumns(10);
 		
 		JLabel lblExpiryDate = new JLabel("Expiry Date");
 		GridBagConstraints gbc_lblExpiryDate = new GridBagConstraints();
 		gbc_lblExpiryDate.insets = new Insets(0, 0, 5, 5);
 		gbc_lblExpiryDate.anchor = GridBagConstraints.EAST;
 		gbc_lblExpiryDate.gridx = 4;
 		gbc_lblExpiryDate.gridy = 7;
 		onlinePurchase.add(lblExpiryDate, gbc_lblExpiryDate);
 		
 		opcardxd = new JTextField();
 		GridBagConstraints gbc_opcardxd = new GridBagConstraints();
 		gbc_opcardxd.insets = new Insets(0, 0, 5, 0);
 		gbc_opcardxd.fill = GridBagConstraints.HORIZONTAL;
 		gbc_opcardxd.gridx = 5;
 		gbc_opcardxd.gridy = 7;
 		onlinePurchase.add(opcardxd, gbc_opcardxd);
 		opcardxd.setColumns(10);
 		
 		JButton btnAddItem_2 = new JButton("Add Item");
 		btnAddItem_2.addActionListener(new ActionListener() {
 			//TODO: ONLINE PURCHASE ADD ITEM BUTTON
 			public void actionPerformed(ActionEvent arg0) {
 				onlineSearchTextArea.setText("");
 				String upc = textField_2.getText().trim(); 
 				PurchaseOperations p = new PurchaseOperations();
 				if(p.addItemToShoppingCart(upc)){
 					System.out.println();
 				}
 			}
 		});
 		GridBagConstraints gbc_btnAddItem_2 = new GridBagConstraints();
 		gbc_btnAddItem_2.anchor = GridBagConstraints.WEST;
 		gbc_btnAddItem_2.gridwidth = 2;
 		gbc_btnAddItem_2.insets = new Insets(0, 0, 0, 5);
 		gbc_btnAddItem_2.gridx = 1;
 		gbc_btnAddItem_2.gridy = 8;
 		onlinePurchase.add(btnAddItem_2, gbc_btnAddItem_2);
 		GridBagConstraints gbc_btnSubmitOrder = new GridBagConstraints();
 		gbc_btnSubmitOrder.gridx = 5;
 		gbc_btnSubmitOrder.gridy = 8;
 		onlinePurchase.add(btnSubmitOrder, gbc_btnSubmitOrder);
 	}
 	public void switchToNextCard(String accesslevel){
 		CardLayout cl = (CardLayout) (contentPane.getLayout());
 		switch (accesslevel){
 		case ("Clerk"):
 			cl.show(contentPane, "name_1442560847488307");
 			break;
 			
 		case ("Customer" ):
 			cl.show(contentPane, "name_1452058092586835");
 			break;
 		
 		case ("Manager"):
 			cl.show(contentPane, "name_1449473464875310");
 			break;
 			
 				
 		}
 	}
 	
 	public static void showDailySalesReport(String s){
 		dailySalesReportTextArea.append(s);
 	}
 	public static void showSearchResults(String s){
 		onlineSearchTextArea.append(s);
 	}
 public static void addInstoreItemToPurchase(String item){
 	purchaseItems.append(item+"\n");
 }
 public static void savePurchaseItem(String arg){
 	savedInstoreItems.add(arg);
 }
 public static void clearPurchaseList(){
 	savedInstoreItems.clear();
 }
 public static ArrayList<String> getPurchaseItems(){
 	return savedInstoreItems;
 }
 public static Integer getNumberInstorePurchaseItems(){
 	return numberOfItems;
 }
 public static void setDailySalesReport(String s){
 	dailySalesReport = s;
 }
 
 public String getCustomerId() {
 	return customerId;
 }
 
 public void setCustomerId(String customerId) {
 	this.customerId = customerId;
 }
 public static ArrayList<String> getOnlineSearchItems(){
 	return onlineSearchItems;
 }
 public static ArrayList<String> getOnlinePurchaseItems(){
 	return onlinePurchaseItems;
 }
 public static void addSearchItem(String s){
 	onlineSearchItems.add(s);
 }
 public static void clearSearchResults(){
 	onlineSearchItems.clear();
 }
 public static void saveOnlinePurchaseItem(String s){
 	onlinePurchaseItems.add(s);
 }
 public static int getOnlinePurchaseCount(){
 	return onlineItemCount;
 }
 public static void incOnlinePurchaseCount(){
 	onlineItemCount++;
 }
 
 public static Integer getSearchResultItemCount() {
 	return searchResultItemCount;
 }
 public static void incSearchResultCount(){
 	searchResultItemCount++;
 }
 public static void appendShoppingBasketTextArea(String s){
 	shoppingBasketTextArea.append(s);
 }
 public static void appendTopSellingItemsReport(String s){
 	topSellTextArea.append(s);
 }
 public static void appendSearchText(String s){
 onlineSearchTextArea.append(s);
 }
 }
