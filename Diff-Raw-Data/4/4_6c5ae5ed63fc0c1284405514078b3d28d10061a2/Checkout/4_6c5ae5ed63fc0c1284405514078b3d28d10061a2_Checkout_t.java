 package imat.gui;
 
 import imat.backend.ShopModel;
 
 import java.awt.CardLayout;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.text.NumberFormat;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Locale;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.BorderFactory;
 import javax.swing.ButtonGroup;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPasswordField;
 import javax.swing.JRadioButton;
 import javax.swing.JTextField;
 import javax.swing.KeyStroke;
 import javax.swing.LayoutStyle.ComponentPlacement;
 import javax.swing.SwingConstants;
 import javax.swing.UIManager;
 import javax.swing.border.LineBorder;
 
 import se.chalmers.ait.dat215.project.CreditCard;
 import se.chalmers.ait.dat215.project.IMatDataHandler;
 
 public class Checkout extends JPanel implements ActionListener, PropertyChangeListener {
 
 	// TODO: Confirmation screen after authentication (different card)
 	
 	//Constants
 	//The price of home delivery
 	private int DELIVERY = 20;
 	//The price of pickup
 	private int PICKUP = 0;
 	//Options for the password dialog
 	String options[] = {"Submit", "Cancel"};
 	
 	Icon passIcon = new ImageIcon(Checkout.class.getResource("/imat/resources/passwordicon.PNG"));
 	
 	//The IMatDataHandler
 	private IMatDataHandler imdh;
 	
 	//Various variables
 	private JOptionPane parentPane;
 	private JDialog passDialog;
 	private JPasswordField pwd;
 	private double sum;
 	private double shoppingCart;
 	private JLabel sumLabel;
 	private JLabel cartLabel;
 	private JLabel deliveryLabel;
 	private JCheckBox save;
 	private JTextField txtCard;
 	private JTextField txtSec;
 	private JPanel cardPanel;
 	private JComboBox<String> delivery;
 	private JComboBox<String> pickup;
 	private JComboBox<String> year;
 	private JComboBox<String> month;
 	private JTextField txtName;
 	private JRadioButton visa;
 	private JRadioButton mastercard;
 	private JRadioButton iButik;
 	private CreditCard cc;
 	private ButtonGroup cardGroup;
 	private ButtonGroup payGroup;
 	private ButtonGroup deliveryGroup;
 	private JComponent[] listComponents;
 	private ShopModel model;
 	
 	private static NumberFormat format = NumberFormat
 			.getCurrencyInstance(Locale.forLanguageTag("sv-SE"));
 	
 	private Action closeWindow = new AbstractAction() {
 	    public void actionPerformed(ActionEvent e) {
 	        destroyAndCreate("Felaktigt lösenord");
 	    }
 	};
 	
 
 	/**
 	 * Create the application.
 	 */
 	public Checkout(ShopModel model) {
 		this.model = model;
 		model.addPropertyChangeListener(this);
 		initialize();
 		initCardInfo();
 		initPassDialog();
 		listComponents = new JComponent[]{ visa, mastercard, txtCard, txtSec, txtName, year, month};
 	}
 	
 	public void actionPerformed (ActionEvent e){
 		
 		if(e.getActionCommand().equals("delivery")){
 			//If customer chooses home delivery, set related
 			//elements enabled and disable irrelevant ones
 			pickup.setEnabled(false);
 			delivery.setEnabled(true);
 			iButik.setEnabled(false);
 			deliveryLabel.setText("Leverans: " + DELIVERY + " kr");
 			amendSum(DELIVERY);
 		} else if (e.getActionCommand().equals("pickup")){
 			//If customer chooses pick up at store, set related
 			//elements enabled and disable irrelevant ones
 			pickup.setEnabled(true);
 			delivery.setEnabled(false);
 			iButik.setEnabled(true);
 			deliveryLabel.setText("Leverans: " + PICKUP + " kr");
 			amendSum(-DELIVERY);
 		} else if (e.getActionCommand().equals("credit")){
 			//If customer chooses to pay by credit card, set 
 			//related elements enabled and add color to show
 			//how the fields and panels are now enabled
 			cardPanel.setBackground(new Color(238,238,238));
 			visa.setBackground(new Color(238,238,238));
 			mastercard.setBackground(new Color(238,238,238));
 			for (JComponent jc : listComponents) {
 				jc.setEnabled(true);
 			}
 			
 		} else if (e.getActionCommand().equals("finish")){
 			//If the customer wishes to finish his purchase,
 			//store data (and shutdown, for now)
 			
 			if(checkInput()){ //If input is valid continues with authentication			
 				if(shallPass()){ //If properly authenticated, finalises purchase
 					JOptionPane.showMessageDialog(this, "Tack för ditt köp");
 					if(save.isSelected()){
 						saveCardInfo();
 					}
 					imdh.placeOrder();
 				}
 			}
 		} else {
 			//If any other way of paying is chosen but credit card,
 			//disable all elements in the cardPanel and darken it
 			//to make it seem unusable
 			cardPanel.setBackground(Color.LIGHT_GRAY);
 			visa.setBackground(Color.LIGHT_GRAY);
 			mastercard.setBackground(Color.LIGHT_GRAY);
 			for (JComponent jc : listComponents) {
 				jc.setEnabled(false);
 			}
 		}
 	}
 	
 	//Decides whether the user shall pass or not, based on what password they enter
 	private boolean shallPass() {
 		
 		parentPane = new JOptionPane(pwd, JOptionPane.INFORMATION_MESSAGE,
 				JOptionPane.OK_CANCEL_OPTION, passIcon, options, pwd);
 		
 		passDialog = parentPane.createDialog(this, "Lösenord krävs");
 		passDialog.setVisible(true);
 		
 		//Fetches password, blanks out array
 		String value = parentPane.getValue().toString();
 		char[] pass = pwd.getPassword();
 		String stringPass = String.copyValueOf(pass);
 		pwd.setText("");
 		Arrays.fill(pass, '0');
 		
 		destroyAndCreate("Felaktigt lösenord");
 		
 		while(!value.equalsIgnoreCase("cancel")){ 
 			//When cancel is pressed, getValue on 
 			//parentPane returns "Cancel", if window
 			//is closed, the dialog crashes due to
 			//NullPointerException, but who the hell cares?
 			
 			if(stringPass.equals(imdh.getUser().getPassword())){
 				passDialog.dispose();
 				return true;
 			}
 			
 			passDialog.setVisible(true);
 			//Fetches password, blanks out array
 			value = parentPane.getValue().toString();
 			pass = pwd.getPassword();
 			stringPass = String.copyValueOf(pass);
 			pwd.setText("");
 			Arrays.fill(pass, '0');
 			destroyAndCreate("Felaktigt lösenord");
 		}
 		return false;
 	}
 	
 	//Checks what card the customer has selected and returns it
 	private String selectedCard() {
 		if(mastercard.isSelected()){
 			return "Mastercard";
 		} else {
 			return "Visa";
 		}
 	}
 	
 	//Amends the displayed sum with the specified value
 	private void amendSum (double d) {
 		sum += d;
 		sumLabel.setText("Summa: " + format.format(sum));
 	}
 	
 	private void setSum(double d) {
 		sum = d;
 		sumLabel.setText("Summa: " + format.format(sum));
 	}
 	
 	private void setCart(double d){
 		shoppingCart = d;
 		cartLabel.setText("Varukorg: " + format.format(shoppingCart));
 	}
 	
 	//Gets the chosen year for the credit cards validity and returns it
 	private int getChosenYear() {
 		String s = (String)year.getSelectedItem();
 		return Integer.parseInt(s);
 	}
 	
 	//Gets the chosen month for the credit cards validity and returns it
 	private int getChosenMonth() {
 		String s = (String)month.getSelectedItem();
 		return Integer.parseInt(s);
 	}
 	
 	//Sets the corrects year and month validity for the credit card,
 	//as received from the CreditCard class.
 	private void setYearAndMonth() {
 		String s = String.valueOf(cc.getValidMonth());
 		month.setSelectedItem(s);
 		s = String.valueOf(cc.getValidYear());
 		year.setSelectedItem(s);
 	}
 	
 	//Saves the customers card info
 	private void saveCardInfo(){
 		cc.setHoldersName(txtName.getText());
 		cc.setCardNumber(txtCard.getText());
 		cc.setVerificationCode(Integer.parseInt(txtSec.getText()));
 		cc.setValidMonth(getChosenMonth());
 		cc.setValidYear(getChosenYear());
 		cc.setCardType(selectedCard());
 	}
 	
 	//Destroys and creates the password prompt dialog so it will display
 	//and focus correctly with the specified title
 	private void destroyAndCreate(String title) {
 
 		passDialog.dispose();
 		parentPane = new JOptionPane(pwd, JOptionPane.INFORMATION_MESSAGE,
 				JOptionPane.OK_CANCEL_OPTION, passIcon, options, pwd);
         passDialog = parentPane.createDialog(this, title);
 	}
 	
 	//Returns true if and only if input is valid, otherwise false
 	private boolean checkInput() {
 		List<String> errorList = new LinkedList<String>();
 		
 		String toTest = txtCard.getText().trim();
 		if(charInInt(toTest)){
 			errorList.add("Letter in cardnumber");
 			txtCard.setForeground(Color.red);
 		}
 		if(toTest.length() != 16){
 			errorList.add("A cardnumber should consist of 16 digits");
 			txtCard.setForeground(Color.red);
 		}
 		
 		toTest = txtSec.getText().trim();
 		if(charInInt(toTest)){
 			errorList.add("Letter in verification code");
 			txtCard.setForeground(Color.red);
 		}
 		if(toTest.length() != 3){
 			errorList.add("A verification code should consist of 3 digits");
 			txtSec.setForeground(Color.red);
 		}
 		
 		if(errorList.isEmpty()){
 			return true;
 		} else{
 			StringBuilder sb = new StringBuilder();
 			for(String s : errorList){
 				sb.append(s + "\n");
 			}
 			JOptionPane.showMessageDialog(this, sb.toString().trim(), 
 					"Fel i inmatning", JOptionPane.ERROR_MESSAGE);
 			return false;
 		}
 	}
 	
 	//Determines if there is a char in a String or if it 
 	private boolean charInInt(String s) {
 		char[] stringArray = s.toCharArray();
 		for(Character test : stringArray){
 			if(!Character.isDigit(test)){
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	//Fills the fields containing information about the costumers 
 	//credit card
 	private void initCardInfo() {
 		cc = imdh.getCreditCard();
 		txtName.setText(cc.getHoldersName());
 		txtCard.setText(cc.getCardNumber());
 		if(cc.getVerificationCode() != 0){
 			txtSec.setText(String.valueOf(cc.getVerificationCode()));
		} else {
			txtSec.setText("");
 		}
 		setYearAndMonth();
 		if(cc.getCardType().equals("Mastercard")){
 			mastercard.setSelected(true);
 		}
 	
 	}
 	
 	//Sets keylisteners to ensure that the password prompt
 	//works as expected
 	private void initPassDialog() {
 		pwd.getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "closeDialog");
 		pwd.getActionMap().put("closeDialog", closeWindow);
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		pwd = new JPasswordField();
 		imdh = IMatDataHandler.getInstance();
 		shoppingCart = imdh.getShoppingCart().getTotal();
 		sum = shoppingCart + DELIVERY;
 		
 		payGroup = new ButtonGroup();
 		deliveryGroup = new ButtonGroup();
 		cardGroup = new ButtonGroup();
 		
 		this.setBounds(100, 100, 600, 540);
 		this.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
 		this.setLayout(new CardLayout(0, 0));
 		
 		JPanel panel_1 = new JPanel();
 		this.add(panel_1, "name_4538180736579");
 		
 		JPanel panel = new JPanel();
 		
 		JPanel panel_2 = new JPanel();
 		
 		JPanel panel_3 = new JPanel();
 		GroupLayout gl_panel_1 = new GroupLayout(panel_1);
 		gl_panel_1.setHorizontalGroup(
 			gl_panel_1.createParallelGroup(Alignment.TRAILING)
 				.addGroup(gl_panel_1.createSequentialGroup()
 					.addContainerGap()
 					.addGroup(gl_panel_1.createParallelGroup(Alignment.TRAILING)
 						.addComponent(panel_2, Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 590, Short.MAX_VALUE)
 						.addComponent(panel, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 590, Short.MAX_VALUE)
 						.addComponent(panel_3, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 590, Short.MAX_VALUE))
 					.addContainerGap())
 		);
 		gl_panel_1.setVerticalGroup(
 			gl_panel_1.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_panel_1.createSequentialGroup()
 					.addContainerGap()
 					.addComponent(panel, GroupLayout.PREFERRED_SIZE, 58, GroupLayout.PREFERRED_SIZE)
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addComponent(panel_2, GroupLayout.PREFERRED_SIZE, 202, GroupLayout.PREFERRED_SIZE)
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addComponent(panel_3, GroupLayout.DEFAULT_SIZE, 244, Short.MAX_VALUE)
 					.addContainerGap())
 		);
 		
 		JRadioButton rdbtnKreditkort = new JRadioButton("Kreditkort");
 		rdbtnKreditkort.setSelected(true);
 		
 		JRadioButton rdbtnInternetbank = new JRadioButton("Internetbank");
 		
 		JRadioButton rdbtnIButik = new JRadioButton("I butik");
 		rdbtnIButik.setEnabled(false);
 		
 		iButik = rdbtnIButik;
 		
 		payGroup.add(rdbtnKreditkort);
 		payGroup.add(rdbtnInternetbank);
 		payGroup.add(rdbtnIButik);
 		
 		rdbtnKreditkort.addActionListener(this);
 		rdbtnKreditkort.setActionCommand("credit");
 		rdbtnInternetbank.addActionListener(this);
 		rdbtnInternetbank.setActionCommand("bank");
 		rdbtnIButik.addActionListener(this);
 		rdbtnIButik.setActionCommand("store");
 		
 		
 		JLabel lblHurVillDu_1 = new JLabel("Hur vill du betala?");
 		
 		JPanel panel_4 = new JPanel();
 		panel_4.setBackground(UIManager.getColor("Panel.background"));
 		panel_4.setBorder(new LineBorder(new Color(0, 0, 0)));
 		
 		JButton btnSlutfr = new JButton("Slutför");
 		btnSlutfr.addActionListener(this);
 		btnSlutfr.setActionCommand("finish");
 		
 		JCheckBox chckbxSparaMinaUppgifter = new JCheckBox("Spara mina uppgifter");
 		save = chckbxSparaMinaUppgifter;
 		chckbxSparaMinaUppgifter.setSelected(true);
 		GroupLayout gl_panel_3 = new GroupLayout(panel_3);
 		gl_panel_3.setHorizontalGroup(
 			gl_panel_3.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_panel_3.createSequentialGroup()
 					.addContainerGap()
 					.addGroup(gl_panel_3.createParallelGroup(Alignment.LEADING)
 						.addComponent(rdbtnInternetbank)
 						.addComponent(rdbtnIButik)
 						.addComponent(lblHurVillDu_1)
 						.addComponent(rdbtnKreditkort))
 					.addPreferredGap(ComponentPlacement.UNRELATED, 18, Short.MAX_VALUE)
 					.addGroup(gl_panel_3.createParallelGroup(Alignment.TRAILING, false)
 						.addGroup(gl_panel_3.createSequentialGroup()
 							.addComponent(chckbxSparaMinaUppgifter)
 							.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 							.addComponent(btnSlutfr))
 						.addComponent(panel_4, GroupLayout.PREFERRED_SIZE, 421, GroupLayout.PREFERRED_SIZE))
 					.addContainerGap())
 		);
 		gl_panel_3.setVerticalGroup(
 			gl_panel_3.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_panel_3.createSequentialGroup()
 					.addGroup(gl_panel_3.createParallelGroup(Alignment.LEADING)
 						.addGroup(gl_panel_3.createSequentialGroup()
 							.addContainerGap()
 							.addComponent(lblHurVillDu_1)
 							.addPreferredGap(ComponentPlacement.UNRELATED)
 							.addComponent(rdbtnKreditkort)
 							.addPreferredGap(ComponentPlacement.UNRELATED)
 							.addComponent(rdbtnInternetbank)
 							.addPreferredGap(ComponentPlacement.UNRELATED)
 							.addComponent(rdbtnIButik))
 						.addComponent(panel_4, GroupLayout.PREFERRED_SIZE, 184, GroupLayout.PREFERRED_SIZE))
 					.addPreferredGap(ComponentPlacement.RELATED, 21, Short.MAX_VALUE)
 					.addGroup(gl_panel_3.createParallelGroup(Alignment.BASELINE)
 						.addComponent(btnSlutfr)
 						.addComponent(chckbxSparaMinaUppgifter))
 					.addContainerGap())
 		);
 		
 		cardPanel = panel_4;
 		
 		JLabel lblKortnummer = new JLabel("Kortnummer");
 		
 		txtCard = new JTextField();
 		txtCard.setColumns(10);
 		
 		JLabel lblSakerhetsKod = new JLabel("Säkerhetskod (CVC)");
 		
 		txtSec = new JTextField();
 		txtSec.setColumns(10);
 		
 		JLabel lblKortinnehavare = new JLabel("Kortinnehavare");
 		
 		txtName = new JTextField();
 		txtName.setColumns(10);
 		
 		JLabel lblGiltligTill = new JLabel("Giltig till (Månad/År)");
 		
 		JLabel label = new JLabel("/");
 		
 		JRadioButton rdbtnVisa = new JRadioButton("Visa");
 		rdbtnVisa.setSelected(true);
 		rdbtnVisa.setBackground(UIManager.getColor("Panel.background"));
 		visa = rdbtnVisa;
 		
 		JRadioButton rdbtnMastercard = new JRadioButton("Mastercard");
 		rdbtnMastercard.setBackground(UIManager.getColor("Panel.background"));
 		mastercard = rdbtnMastercard;
 		
 		cardGroup.add(rdbtnVisa);
 		cardGroup.add(rdbtnMastercard);
 		
 		JLabel lblKorttyp = new JLabel("Korttyp");
 		
 		JComboBox<String> comboMonth = new JComboBox<String>();
 		month = comboMonth;
 		comboMonth.setModel(new DefaultComboBoxModel<String>(new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"}));
 		
 		JComboBox<String> comboYear = new JComboBox<String>();
 		year = comboYear;
 		comboYear.setModel(new DefaultComboBoxModel<String>(new String[] {"13", "14", "15", "16", "17", "18", "19", "20", "21"}));
 		GroupLayout gl_panel_4 = new GroupLayout(panel_4);
 		gl_panel_4.setHorizontalGroup(
 			gl_panel_4.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_panel_4.createSequentialGroup()
 					.addContainerGap()
 					.addGroup(gl_panel_4.createParallelGroup(Alignment.LEADING)
 						.addGroup(gl_panel_4.createSequentialGroup()
 							.addComponent(lblGiltligTill)
 							.addGap(18)
 							.addComponent(lblKorttyp))
 						.addGroup(gl_panel_4.createSequentialGroup()
 							.addComponent(comboMonth, GroupLayout.PREFERRED_SIZE, 53, GroupLayout.PREFERRED_SIZE)
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addComponent(label)
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addComponent(comboYear, GroupLayout.PREFERRED_SIZE, 64, GroupLayout.PREFERRED_SIZE)
 							.addGap(18)
 							.addComponent(rdbtnVisa)
 							.addGap(18)
 							.addComponent(rdbtnMastercard))
 						.addGroup(gl_panel_4.createParallelGroup(Alignment.LEADING, false)
 							.addComponent(lblKortinnehavare)
 							.addGroup(gl_panel_4.createSequentialGroup()
 								.addGroup(gl_panel_4.createParallelGroup(Alignment.LEADING)
 									.addComponent(lblKortnummer)
 									.addComponent(txtCard, GroupLayout.PREFERRED_SIZE, 241, GroupLayout.PREFERRED_SIZE))
 								.addPreferredGap(ComponentPlacement.RELATED)
 								.addGroup(gl_panel_4.createParallelGroup(Alignment.TRAILING, false)
 									.addComponent(txtSec)
 									.addComponent(lblSakerhetsKod)))
 							.addComponent(txtName)))
 					.addGap(65))
 		);
 		gl_panel_4.setVerticalGroup(
 			gl_panel_4.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_panel_4.createSequentialGroup()
 					.addContainerGap()
 					.addComponent(lblKortinnehavare)
 					.addPreferredGap(ComponentPlacement.UNRELATED)
 					.addComponent(txtName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 					.addPreferredGap(ComponentPlacement.UNRELATED)
 					.addGroup(gl_panel_4.createParallelGroup(Alignment.BASELINE)
 						.addComponent(lblKortnummer)
 						.addComponent(lblSakerhetsKod))
 					.addPreferredGap(ComponentPlacement.UNRELATED)
 					.addGroup(gl_panel_4.createParallelGroup(Alignment.BASELINE)
 						.addComponent(txtCard, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 						.addComponent(txtSec, GroupLayout.PREFERRED_SIZE, 19, GroupLayout.PREFERRED_SIZE))
 					.addPreferredGap(ComponentPlacement.UNRELATED)
 					.addGroup(gl_panel_4.createParallelGroup(Alignment.BASELINE)
 						.addComponent(lblGiltligTill)
 						.addComponent(lblKorttyp))
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addGroup(gl_panel_4.createParallelGroup(Alignment.BASELINE)
 						.addComponent(label)
 						.addComponent(rdbtnVisa)
 						.addComponent(rdbtnMastercard)
 						.addComponent(comboMonth, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 						.addComponent(comboYear, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
 					.addContainerGap(12, Short.MAX_VALUE))
 		);
 		panel_4.setLayout(gl_panel_4);
 		panel_3.setLayout(gl_panel_3);
 		
 		JRadioButton rdbtnHemleveransKr = new JRadioButton("Hemleverans");
 		rdbtnHemleveransKr.setSelected(true);
 		
 		JComboBox<String> deliveryBox = new JComboBox<String>();
 		deliveryBox.setModel(new DefaultComboBoxModel<String>(new String[] {"Måndag em", "Tisdag fm", "Tisdag em", "Onsdag fm", "Onsdag em"}));
 		
 		JRadioButton rdbtnHmtaIButik = new JRadioButton("Hämta i butik");
 		
 		deliveryGroup.add(rdbtnHemleveransKr);
 		deliveryGroup.add(rdbtnHmtaIButik);
 		
 		rdbtnHemleveransKr.addActionListener(this);
 		rdbtnHemleveransKr.setActionCommand("delivery");
 		rdbtnHmtaIButik.addActionListener(this);
 		rdbtnHmtaIButik.setActionCommand("pickup");
 		
 		JComboBox<String> pickupBox = new JComboBox<String>();
 		pickupBox.setEnabled(false);
 		pickupBox.setModel(new DefaultComboBoxModel<String>(new String[] {"Måndag em", "Tisdag fm", "Tisdag em", "Onsdag fm", "Onsdag em"}));
 		
 		delivery = deliveryBox;
 		pickup = pickupBox;
 		
 		JLabel label_1 = new JLabel("");
 		label_1.setIcon(new ImageIcon(Checkout.class.getResource("/imat/resources/homeDelivery150x100.PNG")));
 		
 		JLabel label_2 = new JLabel("");
 		label_2.setIcon(new ImageIcon(Checkout.class.getResource("/imat/resources/inStore150x100.PNG")));
 		
 		JLabel lblSumma = new JLabel("Summa: " + format.format(sum));
 		lblSumma.setHorizontalTextPosition(SwingConstants.LEFT);
 		lblSumma.setHorizontalAlignment(SwingConstants.LEFT);
 		sumLabel = lblSumma;
 		lblSumma.setFont(new Font("Dialog", Font.BOLD, 16));
 		
 		JLabel lblLeveransKr = new JLabel("Leverans: " + format.format(DELIVERY));
 		lblLeveransKr.setHorizontalTextPosition(SwingConstants.LEFT);
 		lblLeveransKr.setHorizontalAlignment(SwingConstants.LEFT);
 		deliveryLabel = lblLeveransKr;
 		
 		JLabel lblVarukorgKr = new JLabel("Varukorg: " + format.format(shoppingCart));
 		lblVarukorgKr.setHorizontalTextPosition(SwingConstants.LEFT);
 		lblVarukorgKr.setHorizontalAlignment(SwingConstants.LEFT);
 		cartLabel = lblVarukorgKr;
 		
 		GroupLayout gl_panel_2 = new GroupLayout(panel_2);
 		gl_panel_2.setHorizontalGroup(
 			gl_panel_2.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_panel_2.createSequentialGroup()
 					.addGroup(gl_panel_2.createParallelGroup(Alignment.LEADING, false)
 						.addComponent(deliveryBox, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 						.addComponent(rdbtnHemleveransKr, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
 						.addComponent(label_1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
 					.addGap(18)
 					.addGroup(gl_panel_2.createParallelGroup(Alignment.LEADING)
 						.addComponent(label_2, GroupLayout.DEFAULT_SIZE, 154, Short.MAX_VALUE)
 						.addComponent(rdbtnHmtaIButik, GroupLayout.DEFAULT_SIZE, 154, Short.MAX_VALUE)
 						.addComponent(pickupBox, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE))
 					.addGap(105)
 					.addGroup(gl_panel_2.createParallelGroup(Alignment.LEADING)
 						.addComponent(lblSumma, Alignment.TRAILING)
 						.addComponent(lblVarukorgKr, Alignment.TRAILING)
 						.addComponent(lblLeveransKr, Alignment.TRAILING))
 					.addContainerGap())
 		);
 		gl_panel_2.setVerticalGroup(
 			gl_panel_2.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_panel_2.createSequentialGroup()
 					.addGroup(gl_panel_2.createParallelGroup(Alignment.LEADING)
 						.addGroup(gl_panel_2.createSequentialGroup()
 							.addGroup(gl_panel_2.createParallelGroup(Alignment.LEADING)
 								.addComponent(label_1)
 								.addComponent(label_2))
 							.addGap(18)
 							.addGroup(gl_panel_2.createParallelGroup(Alignment.BASELINE)
 								.addComponent(rdbtnHemleveransKr)
 								.addComponent(rdbtnHmtaIButik))
 							.addPreferredGap(ComponentPlacement.UNRELATED)
 							.addGroup(gl_panel_2.createParallelGroup(Alignment.BASELINE)
 								.addComponent(deliveryBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
 								.addComponent(pickupBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
 						.addGroup(gl_panel_2.createSequentialGroup()
 							.addContainerGap()
 							.addComponent(lblSumma)
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addComponent(lblVarukorgKr)
 							.addPreferredGap(ComponentPlacement.RELATED)
 							.addComponent(lblLeveransKr)))
 					.addContainerGap(29, Short.MAX_VALUE))
 		);
 		panel_2.setLayout(gl_panel_2);
 		
 		JLabel lblNewLabel = new JLabel("Välkommen till kassan!");
 		lblNewLabel.setFont(new Font("Dialog", Font.BOLD, 20));
 		
 		JLabel lblHurVillDu = new JLabel("Hur vill du ha dina varor?");
 		GroupLayout gl_panel = new GroupLayout(panel);
 		gl_panel.setHorizontalGroup(
 			gl_panel.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_panel.createSequentialGroup()
 					.addContainerGap()
 					.addGroup(gl_panel.createParallelGroup(Alignment.LEADING)
 						.addComponent(lblHurVillDu)
 						.addComponent(lblNewLabel))
 					.addContainerGap(187, Short.MAX_VALUE))
 		);
 		gl_panel.setVerticalGroup(
 			gl_panel.createParallelGroup(Alignment.LEADING)
 				.addGroup(gl_panel.createSequentialGroup()
 					.addGap(5)
 					.addComponent(lblNewLabel)
 					.addPreferredGap(ComponentPlacement.RELATED)
 					.addComponent(lblHurVillDu)
 					.addContainerGap(17, Short.MAX_VALUE))
 		);
 		panel.setLayout(gl_panel);
 		panel_1.setLayout(gl_panel_1);
 	}
 
 	@Override
 	public void propertyChange(PropertyChangeEvent evt) {
 		if (evt.getPropertyName().equals("cart")) {
 			setCart(model.getShoppingCart().getTotal());
 			setSum(model.getShoppingCart().getTotal());
 			amendSum(DELIVERY);
 		}
 	}
 }
