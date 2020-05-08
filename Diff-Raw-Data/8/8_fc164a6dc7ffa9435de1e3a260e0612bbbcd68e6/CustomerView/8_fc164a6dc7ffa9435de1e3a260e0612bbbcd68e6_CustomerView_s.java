 package com.cs174.starrus.view;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 
 
 import javax.swing.*;
 import javax.swing.border.LineBorder;
 import com.cs174.starrus.controller.DepositController;
 import com.cs174.starrus.controller.LoginController;
 import com.cs174.starrus.controller.LogoutController;
 import com.cs174.starrus.controller.MTransactionController;
 import com.cs174.starrus.controller.STransactionController;
 import com.cs174.starrus.controller.WithdrawController;
 import com.cs174.starrus.controller.BuyStockController;
 import com.cs174.starrus.controller.SellStockController;
 import com.cs174.starrus.controller.TopMovieController;
 import com.cs174.starrus.model.Customer;
 import java.awt.ComponentOrientation;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Vector;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 
 public class CustomerView extends JPanel implements IView{
 	
 	/**
 	 * 
 	 */
 	private boolean DEBUG = true;
 	private static final long serialVersionUID = 1L;
 	//-------------main window components--------------
 	private JPanel left;
 	private JPanel right;
 	private JLabel lblWelcom;
 	private JLabel lblTime;
 	private JLabel lblCurrentTime;
 	private JPanel mainDisp;
 	private JLabel lblMarketAccount;
 	private JLabel lblStockAccount;
 	private JLabel lblAccount;
 	private JLabel lblAccountBalance;
 	private JPanel Maccount;
 	private JPanel Saccount;
 	private JLabel lblAccountS;
 	private JButton btnDeposit;
 	private JButton btnWithdraw;
 	private JButton btnSellStocks;
 	private JButton btnBuyStocks;
 	private JButton btnViewTransactions;
 	private JButton btnViewTransactionsS;
 	private JLabel lblUsername;
 	private final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
 	private JPanel stockPanel;
 	private JPanel moviePanel;
 	private JLabel lblUsername_1;
 	private JLabel lblUsfield;
 	private JLabel lblMyAccountInfo;
 	private JLabel lblAge;
 	private JLabel lblAgefield;
 	private JLabel lblPassword;
 	private static CustomerView cView = null;	
 	private JLabel lblPhone;
 	private JLabel lblState;
 	private JLabel lblTaxId;
 	private JLabel lblEmail;
 	private JLabel lblPsdfield;
 	private JLabel lblPhonefield;
 	private JLabel lblStatefield;
 	private JLabel lblTaxfield;
 	private JLabel lblEmailfield;
 	private JLabel lblUserLevel;
 	private JLabel lblLevelfield;
 	private JLabel lblMAccountId;
 	private JLabel balancefield;
 	private JLabel lblSAccountId;
 	private JButton btnLogout;
 	Vector<Vector<String>> row_listStock = new Vector<Vector<String>>();
 	Vector<Vector<String>> row_myStock = new Vector<Vector<String>>();
 	Vector<Vector<String>> row_listMovie = new Vector<Vector<String>>();
 	private JScrollPane scrollPane_myStock;
 	private JScrollPane scrollPane_listStock;
 	private JScrollPane scrollPane_listMovie;
 	private JPanel panel_myStock;
 	private JTable table_myStock;
 	private JTable table_listStock;
 	private JTable table_listMovie;
 	private JLabel lblFrom;
 	private JTextField txtFromfield;
 	private JLabel lblTo;
 	private JTextField txtTofield;
 	private JButton btnTopMovies;
 	private JPanel panel;
 		
 	private CustomerView(){
 		this.setSize(new Dimension(800, 600));
 		this.setPreferredSize(new Dimension(800, 600));
 		setLayout(null);
 		//setView(new Customer());
 	}
 	
 	public static CustomerView getView(){
 		if(cView == null)
 			cView = new CustomerView();
 		return cView;
 	}
 	
 	public void setView(Customer c){
 		this.removeAll();
 		//------------------------------left panel-------------------------------
 		this.left = new JPanel();
 		this.left.setBounds(0, 0, 200, 600);
 		this.left.setBackground(new Color(204, 153, 153));
 		this.left.setBorder(new LineBorder(new Color(0, 0, 0)));
 		this.left.setPreferredSize(new Dimension(200, 600));
 		this.add(left);
 		this.left.setLayout(null);
 		
 		this.lblWelcom = new JLabel("Welcome");
 		this.lblWelcom.setFont(new Font("Lucida Grande", Font.BOLD, 13));
 		this.lblWelcom.setBounds(6, 23, 81, 16);
 		this.left.add(this.lblWelcom);
 		
 		this.lblUsername = new JLabel("name");
 		this.lblUsername.setBounds(87, 23, 107, 16);
 		this.lblUsername.setText(c.getCname());
 		this.left.add(this.lblUsername);
 		this.left.setPreferredSize(new Dimension(400, 600));
 		
 		this.lblUsername_1 = new JLabel("Username: ");
 		this.lblUsername_1.setBounds(6, 79, 84, 16);
 		this.left.add(this.lblUsername_1);
 		
 		this.lblUsfield = new JLabel("pengwang");
 		this.lblUsfield.setText(c.getUsername());
 		this.lblUsfield.setBounds(87, 79, 107, 16);
 		this.left.add(this.lblUsfield);
 		
 		this.lblMyAccountInfo = new JLabel("My Account Infomation : ");
 		this.lblMyAccountInfo.setFont(new Font("Lucida Grande", Font.BOLD | Font.ITALIC, 13));
 		this.lblMyAccountInfo.setBounds(6, 51, 178, 16);
 		this.left.add(this.lblMyAccountInfo);
 		
 		this.lblAge = new JLabel("Age:");
 		this.lblAge.setBounds(6, 107, 84, 16);
 		this.left.add(this.lblAge);
 		
 		this.lblAgefield = new JLabel("100");
		this.lblAgefield.setText("");
 		this.lblAgefield.setBounds(87, 107, 107, 16);
 		this.left.add(this.lblAgefield);
 		
 		this.lblPassword = new JLabel("Password:");
 		this.lblPassword.setBounds(6, 135, 84, 16);
 		this.left.add(this.lblPassword);
 		
 		this.lblPhone = new JLabel("Phone #:");
 		this.lblPhone.setBounds(6, 163, 84, 16);
 		this.left.add(this.lblPhone);
 		
 		this.lblState = new JLabel("State:");
 		this.lblState.setBounds(6, 191, 84, 16);
 		this.left.add(this.lblState);
 		
 		this.lblTaxId = new JLabel("Tax ID:");
 		this.lblTaxId.setBounds(6, 219, 84, 16);
 		this.left.add(this.lblTaxId);
 		
 		this.lblEmail = new JLabel("Email:");
 		this.lblEmail.setBounds(6, 247, 84, 16);
 		this.left.add(this.lblEmail);
 		
 		this.lblPsdfield = new JLabel("psdfield");
 		this.lblPsdfield.setText(c.getPsd());
 		this.lblPsdfield.setBounds(87, 135, 107, 16);
 		this.left.add(this.lblPsdfield);
 		
 		this.lblPhonefield = new JLabel("phonefield");
 		this.lblPhonefield.setText(c.getPhone_num());
 		this.lblPhonefield.setBounds(87, 163, 107, 16);
 		this.left.add(this.lblPhonefield);
 		
 		this.lblStatefield = new JLabel("statefield");
 		this.lblStatefield.setText(c.getState());
 		this.lblStatefield.setBounds(87, 191, 107, 16);
 		this.left.add(this.lblStatefield);
 		
 		this.lblTaxfield = new JLabel("taxfield");
		this.lblTaxfield.setText("");
 		this.lblTaxfield.setBounds(87, 219, 107, 16);
 		this.left.add(this.lblTaxfield);
 		
 		this.lblEmailfield = new JLabel("emailfield");
 		this.lblEmailfield.setText(c.getEmail());
 		this.lblEmailfield.setBounds(16, 275, 178, 16);
 		this.left.add(this.lblEmailfield);
 		
 		this.lblUserLevel = new JLabel("User Level:");
 		this.lblUserLevel.setBounds(6, 303, 81, 16);
 		this.left.add(this.lblUserLevel);
 		
 		this.lblLevelfield = new JLabel("levelfield");
		this.lblLevelfield.setText("");
 		this.lblLevelfield.setBounds(87, 303, 107, 16);
 		this.left.add(this.lblLevelfield);
 		
 		this.btnLogout = new JButton("Logout");
 		listeners.associate(this.btnLogout, new LogoutController());
 		this.btnLogout.setBounds(16, 545, 168, 29);
 		this.left.add(this.btnLogout);
 		//------------------------------- end of left panel --------------------------------
 		
 		//------------------------------ tabbed pane -------------------------------------
 		this.tabbedPane.removeAll();
 		this.tabbedPane.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
 		this.tabbedPane.setMinimumSize(new Dimension(600, 600));
 		this.tabbedPane.setPreferredSize(new Dimension(600, 600));
 		this.tabbedPane.setBounds(200, 0, 600, 600);
 		this.add(tabbedPane);
 		
 		//------------------------- right panel in the tabbed pane ----------------------
 		
 		this.right = new JPanel();
 		tabbedPane.addTab("My Account", null, this.right, "View Account Information");
 		this.right.setBorder(null);
 		this.right.setLayout(null);
 		
 		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//dd/MM/yyyy
 	    Date now = new Date();
 	    String strDate = sdfDate.format(now);
 		this.lblTime = new JLabel();
 		this.lblTime.setText(strDate);
 		this.lblTime.setBounds(408, 6, 165, 16);
 		this.right.add(this.lblTime);
 		
 		this.lblCurrentTime = new JLabel("Current Time:");
 		this.lblCurrentTime.setBounds(282, 6, 99, 16);
 		this.right.add(this.lblCurrentTime);
 		
 		this.mainDisp = new JPanel();
 		this.mainDisp.setBorder(null);
 		this.mainDisp.setBounds(0, 31, 588, 517);
 		this.right.add(this.mainDisp);
 		this.mainDisp.setLayout(null);
 		
 		this.lblMarketAccount = new JLabel("Market Account Summary");
 		this.lblMarketAccount.setFont(new Font("Lucida Grande", Font.BOLD, 13));
 		this.lblMarketAccount.setBounds(6, 6, 213, 16);
 		this.mainDisp.add(this.lblMarketAccount);
 		
 		this.lblStockAccount = new JLabel("Stock Account Summary");
 		this.lblStockAccount.setFont(new Font("Lucida Grande", Font.BOLD, 13));
 		this.lblStockAccount.setBounds(6, 114, 168, 16);
 		this.mainDisp.add(this.lblStockAccount);
 		
 		this.Maccount = new JPanel();
 		this.Maccount.setBackground(new Color(204, 153, 153));
 		this.Maccount.setBounds(16, 32, 555, 70);
 		this.mainDisp.add(this.Maccount);
 		this.Maccount.setLayout(null);
 		
 		this.lblAccount = new JLabel("Account #:");
 		this.lblAccount.setBounds(20, 6, 87, 16);
 		this.Maccount.add(this.lblAccount);
 		
 		this.lblAccountBalance = new JLabel("Account Balance:  $");
 		this.lblAccountBalance.setBounds(248, 6, 132, 16);
 		this.Maccount.add(this.lblAccountBalance);
 		
 		this.btnDeposit = new JButton("Deposit");
 		this.btnDeposit.setBounds(20, 34, 112, 29);
 		listeners.associate(this.btnDeposit, new DepositController());
 		this.Maccount.add(this.btnDeposit);
 		
 		this.btnWithdraw = new JButton("Withdraw");
 		listeners.associate(this.btnWithdraw, new WithdrawController());
 		this.btnWithdraw.setBounds(144, 34, 112, 29);
 		this.Maccount.add(this.btnWithdraw);
 		
 		this.btnViewTransactions = new JButton("View Transactions");
 		listeners.associate(this.btnViewTransactions, new MTransactionController());
 		this.btnViewTransactions.setBounds(268, 34, 155, 29);
 		this.Maccount.add(this.btnViewTransactions);
 		
 		this.lblMAccountId = new JLabel();
 		this.lblMAccountId.setText(Integer.toString(c.getM_account_id()));
 		//System.out.println(c.getM_account_id());
 		this.lblMAccountId.setBounds(119, 6, 117, 16);
 		this.Maccount.add(this.lblMAccountId);
 		
 		this.balancefield = new JLabel("New label");
 		this.balancefield.setText(Float.toString(c.getBalance()));
 		System.out.println(c.getBalance());
 		this.balancefield.setBounds(392, 6, 132, 16);
 		this.Maccount.add(this.balancefield);
 		
 		this.Saccount = new JPanel();
 		this.Saccount.setLayout(null);
 		this.Saccount.setBackground(new Color(204, 153, 153));
 		this.Saccount.setBounds(16, 142, 555, 390);
 		this.mainDisp.add(this.Saccount);
 		
 		this.lblAccountS = new JLabel("Account #:");
 		this.lblAccountS.setBounds(20, 6, 87, 16);
 		this.Saccount.add(this.lblAccountS);
 		
 		this.btnSellStocks = new JButton("Sell Stocks");
 		this.btnSellStocks.setBounds(20, 334, 112, 29);
 		listeners.associate(this.btnSellStocks, new SellStockController());
 		this.Saccount.add(this.btnSellStocks);
 		
 		this.btnBuyStocks = new JButton("Buy Stocks");
 		this.btnBuyStocks.setBounds(144, 334, 112, 29);
 		listeners.associate(this.btnBuyStocks, new BuyStockController());
 		this.Saccount.add(this.btnBuyStocks);
 		
 		this.btnViewTransactionsS = new JButton("View Transactions");
 		listeners.associate(this.btnViewTransactionsS, new STransactionController());
 		this.btnViewTransactionsS.setBounds(268, 334, 155, 29);
 		this.Saccount.add(this.btnViewTransactionsS);
 		
 		
 		//------------------------my stock list------------------------------------
 		this.panel_myStock = new JPanel();
 		this.panel_myStock.setBounds(30, 34, 505, 288);
 		this.panel_myStock.setLayout(null);
 		this.scrollPane_myStock = new JScrollPane();
 		this.scrollPane_myStock.setBounds(6, 5, 493, 277);
 		this.panel_myStock.add(this.scrollPane_myStock);
 		
 		//making table col and row
 		Vector<String> col_myStock = new Vector<String>();
 	    col_myStock.add("Symbol");
 	    col_myStock.add("Total Shares");
 	    
 		this.table_myStock = new JTable(row_myStock, col_myStock){
 			public boolean isCellEditable(int rowIndex, int colIndex){
 				return false;
 			}
 		};
 		this.scrollPane_myStock.setViewportView(this.table_myStock);
 		this.Saccount.add(this.panel_myStock);
 		
 		this.lblSAccountId = new JLabel();
 		this.lblSAccountId.setText(Integer.toString(c.getS_account_id()));
 		this.lblSAccountId.setBounds(119, 6, 137, 16);
 		this.Saccount.add(this.lblSAccountId);
 		//------------------------------end my stocks list ----------------------------
 		
 		//----------------------stock list (tab) -----------------------------------
 		this.stockPanel = new JPanel();
 		tabbedPane.addTab("View Stocks", null, this.stockPanel, "View All Available Stocks");
 		this.stockPanel.setLayout(null);
 		
 		this.scrollPane_listStock = new JScrollPane();
 		this.scrollPane_listStock.setBounds(6, 6, 567, 542);
 		this.stockPanel.add(this.scrollPane_listStock);
 		
 		//making table col and row
 		Vector<String> col_listStock = new Vector<String>();
 	    col_listStock.add("Symbol");
 	    col_listStock.add("Current Price");
 
 		this.table_listStock = new JTable(row_listStock, col_listStock){
             public boolean isCellEditable(int row, int col){
 				return false;
 			}
 		};
 		this.scrollPane_listStock.setViewportView(this.table_listStock);
 		//-----------------------------end of stock list  (tab) ---------------------------
 		
 		this.moviePanel = new JPanel();
 		tabbedPane.addTab("View Movies", null, this.moviePanel, "View All Available Movies");
 		this.stockPanel.setLayout(null);
 		this.moviePanel.setLayout(null);
 		
 		this.scrollPane_listMovie = new JScrollPane();
 		this.scrollPane_listMovie.setBounds(6, 5, 567, 404);
 		this.moviePanel.add(this.scrollPane_listMovie);
 		
 		//making table col and row
 		Vector<String> col_listMovie = new Vector<String>();
 	    col_listMovie.add("Movie");
 	    col_listMovie.add("Production");
 	    col_listMovie.add("Organization");
 
 		this.table_listMovie = new JTable(row_listMovie, col_listMovie){
             public boolean isCellEditable(int row, int col){
 				return false;
 			}
 		};
 		this.table_listMovie.addMouseListener(
 			new java.awt.event.MouseAdapter(){
 				public void mouseClicked(java.awt.event.MouseEvent e){
 					int row = table_listMovie.rowAtPoint(e.getPoint());
 					String movie = table_listMovie.getValueAt(row,0).toString();
 					LoginController.showReviews(movie);
 			}
 		});
 
 		this.scrollPane_listMovie.setViewportView(this.table_listMovie);
 		
 		this.panel = new JPanel();
 		this.panel.setBorder(new LineBorder(new Color(0, 0, 0)));
 		this.panel.setBounds(6, 421, 567, 111);
 		this.moviePanel.add(this.panel);
 		this.panel.setLayout(null);
 		
 		this.txtFromfield = new JTextField();
 		this.txtFromfield.setBounds(118, 17, 134, 28);
 		this.panel.add(this.txtFromfield);
 		this.txtFromfield.setColumns(10);
 		
 		this.lblFrom = new JLabel("From:");
 		this.lblFrom.setBounds(42, 23, 64, 16);
 		this.panel.add(this.lblFrom);
 		
 		this.lblTo = new JLabel("To: ");
 		this.lblTo.setBounds(42, 62, 61, 16);
 		this.panel.add(this.lblTo);
 		
 		this.txtTofield = new JTextField();
 		this.txtTofield.setBounds(118, 56, 134, 28);
 		this.panel.add(this.txtTofield);
 		this.txtTofield.setColumns(10);
 		
 		this.btnTopMovies = new JButton("Top Movies");
 		this.btnTopMovies.setBounds(340, 18, 165, 66);
 		this.panel.add(this.btnTopMovies);
 
 	}
 
 	@Override
 	public void present(String model) {
 		// TODO Auto-generated method stub
 	}
 
 	public JLabel getLblAgefield() {
 		return lblAgefield;
 	}
 
 	public void setLblAgefield(JLabel lblAgefield) {
 		this.lblAgefield = lblAgefield;
 	}
 
 	public JLabel getLblPsdfield() {
 		return lblPsdfield;
 	}
 
 	public void setLblPsdfield(JLabel lblPsdfield) {
 		this.lblPsdfield = lblPsdfield;
 	}
 
 	public JLabel getLblPhonefield() {
 		return lblPhonefield;
 	}
 
 	public void setLblPhonefield(JLabel lblPhonefield) {
 		this.lblPhonefield = lblPhonefield;
 	}
 
 	public JLabel getLblStatefield() {
 		return lblStatefield;
 	}
 
 	public void setLblStatefield(JLabel lblStatefield) {
 		this.lblStatefield = lblStatefield;
 	}
 
 	public JLabel getLblEmailfield() {
 		return lblEmailfield;
 	}
 
 	public void setLblEmailfield(JLabel lblEmailfield) {
 		this.lblEmailfield = lblEmailfield;
 	}
 
 	public JLabel getLblLevelfield() {
 		return lblLevelfield;
 	}
 
 	public void setLblLevelfield(JLabel lblLevelfield) {
 		this.lblLevelfield = lblLevelfield;
 	}
 
 	public JLabel getBalancefield() {
 		return balancefield;
 	}
 
 	public void setBalancefield(String text) {
 		balancefield.setText(text);
 	}
 
 	public Vector<Vector<String>> getRow_listStock() {
 		return row_listStock;
 	}
 
 	public void setRow_listStock(Vector<Vector<String>> row_listStock) {
 		this.row_listStock = row_listStock;
 	}
 
 	public Vector<Vector<String>> getRow_myStock() {
 		return row_myStock;
 	}
 
 	public void setRow_myStock(Vector<Vector<String>> row_myStock) {
 		this.row_myStock = row_myStock;
 	}
 
 	public Vector<Vector<String>> getRow_listMovie() {
 		return row_listMovie;
 	}
 
 	public void setRow_listMovie(Vector<Vector<String>> row_listMovie) {
 		this.row_listMovie = row_listMovie;
 	}
 	
 	public void updateView(Customer c){
 
 		this.setView(c);
 	}
 }
