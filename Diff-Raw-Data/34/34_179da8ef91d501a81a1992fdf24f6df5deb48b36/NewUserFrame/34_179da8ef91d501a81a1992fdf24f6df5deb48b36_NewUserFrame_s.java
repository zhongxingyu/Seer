package v1;

 import java.awt.BorderLayout;
 import java.awt.EventQueue;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 import java.awt.Color;
 import javax.swing.JButton;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 
 import javax.swing.UIManager;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.layout.RowSpec;
 
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JPasswordField;
 import javax.swing.SwingConstants;
 import javax.swing.JTextField;
 import javax.swing.JRadioButton;
 import com.jgoodies.forms.factories.FormFactory;
 import javax.swing.ButtonGroup;
 import java.awt.CardLayout;
 import javax.swing.JTabbedPane;
 import java.awt.GridBagLayout;
 import java.awt.GridBagConstraints;
 import java.awt.Insets;
 import javax.swing.JComboBox;
 import javax.swing.DefaultComboBoxModel;
 
 
 public class NewUserFrame extends JFrame {
 	
 	private JPanel contentPane;
 	
 	private LoginFrame loginFrame;
 	private JTextField emailTextField;
 	private JPasswordField passwordTextField;
 	private JPasswordField repeatPasswordTextField;
 	private JTextField adressTextField;
 	private JTextField postalCodeTextField;
 	private JTextField cityTextField;
 	private final ButtonGroup buttonGroup = new ButtonGroup();
 	private JTextField cardNumberTextField;
 	private JTextField cvcTextField;
 	private JTextField nameTextField;
 
 	/**
 	 * Create the frame.
 	 */
 	public NewUserFrame(final LoginFrame loginFrame) {
 		setResizable(false);
 		setTitle("Skapa konto");
 		this.loginFrame = loginFrame;
 		
 		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		setBounds(100, 100, 391, 340);
 		contentPane = new JPanel();
 		contentPane.setBackground(UIManager.getColor("Panel.background"));
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		contentPane.setLayout(null);
 		
 		final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
 		tabbedPane.setEnabled(false);
 		tabbedPane.setBounds(12, 0, 365, 292);
 		contentPane.add(tabbedPane);
 		
 		JPanel accountAndDeliveryPanel = new JPanel();
 		tabbedPane.addTab("Konto och leverans", null, accountAndDeliveryPanel, null);
 		accountAndDeliveryPanel.setLayout(null);
 		
 		JLabel accountInfoLabel = new JLabel("<html><span style=\"text-decoration: underline;\">Kontoinformation</span>");
 		accountInfoLabel.setBounds(12, 12, 151, 15);
 		accountAndDeliveryPanel.add(accountInfoLabel);
 		
 		JLabel emailLabel = new JLabel("Epost:");
 		emailLabel.setBounds(106, 41, 45, 15);
 		accountAndDeliveryPanel.add(emailLabel);
 		emailLabel.setHorizontalAlignment(SwingConstants.TRAILING);
 		
 		emailTextField = new JTextField();
 		emailTextField.setBounds(156, 39, 171, 19);
 		accountAndDeliveryPanel.add(emailTextField);
 		emailTextField.setColumns(10);
 		
		JLabel passwordLabel = new JLabel("Lösenord:");
 		passwordLabel.setBounds(79, 68, 72, 15);
 		accountAndDeliveryPanel.add(passwordLabel);
 		passwordLabel.setHorizontalAlignment(SwingConstants.TRAILING);
 		
 		passwordTextField = new JPasswordField();
 		passwordTextField.setBounds(156, 66, 171, 19);
 		accountAndDeliveryPanel.add(passwordTextField);
 		passwordTextField.setEchoChar('*');
 		passwordTextField.setColumns(10);
 		
 		JLabel repeatPasswordLabel = new JLabel("Lösenord igen:");
 		repeatPasswordLabel.setBounds(44, 95, 107, 15);
 		accountAndDeliveryPanel.add(repeatPasswordLabel);
 		repeatPasswordLabel.setHorizontalAlignment(SwingConstants.TRAILING);
 		
 		repeatPasswordTextField = new JPasswordField();
 		repeatPasswordTextField.setBounds(156, 93, 171, 19);
 		accountAndDeliveryPanel.add(repeatPasswordTextField);
 		repeatPasswordTextField.setEchoChar('*');
 		repeatPasswordTextField.setColumns(10);
 		
 		JLabel deliveryInfoLabel = new JLabel("<html><span style=\"text-decoration: underline;\">Leveransinformation</span>");
 		deliveryInfoLabel.setBounds(12, 122, 147, 15);
 		accountAndDeliveryPanel.add(deliveryInfoLabel);
 		
 		JLabel adressLabel = new JLabel("Adress:");
 		adressLabel.setBounds(97, 149, 54, 15);
 		accountAndDeliveryPanel.add(adressLabel);
 		adressLabel.setHorizontalAlignment(SwingConstants.TRAILING);
 		
 		JLabel postalCodeLabel = new JLabel("Postnummer:");
 		postalCodeLabel.setBounds(57, 176, 94, 15);
 		accountAndDeliveryPanel.add(postalCodeLabel);
 		postalCodeLabel.setHorizontalAlignment(SwingConstants.TRAILING);
 		
 		JLabel cityLabel = new JLabel("Stad:");
 		cityLabel.setBounds(113, 203, 38, 15);
 		accountAndDeliveryPanel.add(cityLabel);
 		cityLabel.setHorizontalAlignment(SwingConstants.TRAILING);
 		
 		adressTextField = new JTextField();
 		adressTextField.setBounds(156, 147, 171, 19);
 		accountAndDeliveryPanel.add(adressTextField);
 		adressTextField.setColumns(10);
 		
 		postalCodeTextField = new JTextField();
 		postalCodeTextField.setBounds(156, 174, 171, 19);
 		accountAndDeliveryPanel.add(postalCodeTextField);
 		postalCodeTextField.setColumns(10);
 		
 		cityTextField = new JTextField();
 		cityTextField.setBounds(156, 201, 171, 19);
 		accountAndDeliveryPanel.add(cityTextField);
 		cityTextField.setColumns(10);
 		
 		final JButton nextButton = new JButton("Nästa");
 		nextButton.setBounds(210, 233, 117, 25);
 		accountAndDeliveryPanel.add(nextButton);
 		
 		final JButton firstCancelButton = new JButton("Avbryt");
 		firstCancelButton.setBounds(12, 233, 117, 25);
 		accountAndDeliveryPanel.add(firstCancelButton);
 		
 		final JLabel emailErrorLabel = new JLabel("*");
 		emailErrorLabel.setForeground(Color.RED);
 		emailErrorLabel.setVisible(false);
 		emailErrorLabel.setBounds(333, 41, 15, 15);
 		accountAndDeliveryPanel.add(emailErrorLabel);
 		
 		final JLabel passwordErrorLabel = new JLabel("*");
 		passwordErrorLabel.setForeground(Color.RED);
 		passwordErrorLabel.setVisible(false);
 		passwordErrorLabel.setBounds(333, 68, 15, 15);
 		accountAndDeliveryPanel.add(passwordErrorLabel);
 		
 		final JLabel repeatPasswordErrorLabel = new JLabel("*");
 		repeatPasswordErrorLabel.setForeground(Color.RED);
 		repeatPasswordErrorLabel.setVisible(false);
 		repeatPasswordErrorLabel.setBounds(333, 95, 15, 15);
 		accountAndDeliveryPanel.add(repeatPasswordErrorLabel);
 		
 		final JLabel adressErrorLabel = new JLabel("*");
 		adressErrorLabel.setForeground(Color.RED);
 		adressErrorLabel.setVisible(false);
 		adressErrorLabel.setBounds(333, 149, 15, 15);
 		accountAndDeliveryPanel.add(adressErrorLabel);
 		
 		final JLabel postalCodeErrorLabel = new JLabel("*");
 		postalCodeErrorLabel.setForeground(Color.RED);
 		postalCodeErrorLabel.setVisible(false);
 		postalCodeErrorLabel.setBounds(333, 176, 15, 15);
 		accountAndDeliveryPanel.add(postalCodeErrorLabel);
 		
 		final JLabel cityErrorLabel = new JLabel("*");
 		cityErrorLabel.setForeground(Color.RED);
 		cityErrorLabel.setVisible(false);
 		cityErrorLabel.setBounds(333, 203, 15, 15);
 		accountAndDeliveryPanel.add(cityErrorLabel);
 		
 		JPanel paymentPanel = new JPanel();
 		tabbedPane.addTab("Betalning", null, paymentPanel, null);
 		paymentPanel.setLayout(null);
 		
 		JLabel paymentInfoLabel = new JLabel("<html><span style=\"text-decoration: underline;\">Betalningsinformation</span>");
 		paymentInfoLabel.setBounds(12, 12, 158, 15);
 		paymentPanel.add(paymentInfoLabel);
 		
 		JLabel paymentOptionLabel = new JLabel("Välj betalsätt:");
 		paymentOptionLabel.setHorizontalAlignment(SwingConstants.TRAILING);
 		paymentOptionLabel.setBounds(69, 39, 101, 15);
 		paymentPanel.add(paymentOptionLabel);
 		
 		final JRadioButton cardRadioButton = new JRadioButton("Kortbetalning");
 		buttonGroup.add(cardRadioButton);
 		cardRadioButton.setBounds(19, 62, 123, 23);
 		paymentPanel.add(cardRadioButton);
 		
 		JRadioButton internetBankRadioButton = new JRadioButton("Internetbank");
 		buttonGroup.add(internetBankRadioButton);
 		internetBankRadioButton.setBounds(19, 89, 123, 23);
 		paymentPanel.add(internetBankRadioButton);
 		
 		JRadioButton inoviceRadioButton = new JRadioButton("Faktura");
 		buttonGroup.add(inoviceRadioButton);
 		inoviceRadioButton.setBounds(19, 116, 123, 23);
 		paymentPanel.add(inoviceRadioButton);
 		
 		JRadioButton paypalRadioButton = new JRadioButton("PayPal");
 		buttonGroup.add(paypalRadioButton);
 		paypalRadioButton.setBounds(19, 143, 123, 23);
 		paymentPanel.add(paypalRadioButton);
 		
 		JButton previousButton = new JButton("Föregående");
 		previousButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				tabbedPane.setSelectedIndex(0);
 			}
 		});
 		previousButton.setBounds(12, 228, 130, 25);
 		paymentPanel.add(previousButton);
 		
 		final JButton doneButton = new JButton("Klar");
 		doneButton.setBounds(210, 228, 117, 25);
 		paymentPanel.add(doneButton);
 		
 		JButton secondCancelButton = new JButton("Avbryt");
 		secondCancelButton.setBounds(12, 191, 130, 25);
 		paymentPanel.add(secondCancelButton);
 		
 		final JPanel cardPaymentPanel = new JPanel();
 		cardPaymentPanel.setVisible(false);
 		cardPaymentPanel.setBounds(154, 66, 194, 150);
 		paymentPanel.add(cardPaymentPanel);
 		cardPaymentPanel.setLayout(null);
 		
 		JLabel cardNumberLabel = new JLabel("Kortnummer");
 		cardNumberLabel.setBounds(12, 0, 161, 15);
 		cardPaymentPanel.add(cardNumberLabel);
 		
 		cardNumberTextField = new JTextField();
 		cardNumberTextField.setBounds(12, 22, 170, 19);
 		cardPaymentPanel.add(cardNumberTextField);
 		cardNumberTextField.setColumns(10);
 		
 		JLabel expireLabel = new JLabel("Utgångsdatum");
 		expireLabel.setBounds(10, 53, 115, 15);
 		cardPaymentPanel.add(expireLabel);
 		
 		JLabel cvcLabel = new JLabel("CVC");
 		cvcLabel.setHorizontalAlignment(SwingConstants.TRAILING);
 		cvcLabel.setBounds(148, 53, 34, 15);
 		cardPaymentPanel.add(cvcLabel);
 		
 		final JComboBox monthComboBox = new JComboBox();
 		monthComboBox.setModel(new DefaultComboBoxModel(new String[] {"m", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"}));
 		monthComboBox.setSelectedIndex(0);
 		monthComboBox.setBounds(10, 80, 43, 24);
 		cardPaymentPanel.add(monthComboBox);
 		
 		JLabel dividerLabel = new JLabel("/");
 		dividerLabel.setBounds(55, 85, 11, 15);
 		cardPaymentPanel.add(dividerLabel);
 		
 		final JComboBox yearComboBox = new JComboBox();
 		yearComboBox.setModel(new DefaultComboBoxModel(new String[] {"y", "13", "14", "15", "16", "17", "18"}));
 		yearComboBox.setSelectedIndex(0);
 		yearComboBox.setBounds(65, 80, 43, 24);
 		cardPaymentPanel.add(yearComboBox);
 		
 		cvcTextField = new JTextField();
 		cvcTextField.setBounds(139, 80, 43, 19);
 		cardPaymentPanel.add(cvcTextField);
 		cvcTextField.setColumns(10);
 		
 		JLabel nameLabel = new JLabel("Namn");
 		nameLabel.setBounds(12, 116, 58, 15);
 		cardPaymentPanel.add(nameLabel);
 		
 		nameTextField = new JTextField();
 		nameTextField.setBounds(12, 131, 170, 19);
 		cardPaymentPanel.add(nameTextField);
 		nameTextField.setColumns(10);
 		
 		final JLabel cardErrorLabel = new JLabel("*");
 		cardErrorLabel.setForeground(Color.RED);
 		cardErrorLabel.setVisible(false);
 		cardErrorLabel.setBounds(12, 66, 11, 15);
 		paymentPanel.add(cardErrorLabel);
 		
 		final JLabel internetBankErrorLabel = new JLabel("*");
 		internetBankErrorLabel.setForeground(Color.RED);
 		internetBankErrorLabel.setVisible(false);
 		internetBankErrorLabel.setBounds(12, 93, 11, 15);
 		paymentPanel.add(internetBankErrorLabel);
 		
 		final JLabel inoviceErrorLabel = new JLabel("*");
 		inoviceErrorLabel.setForeground(Color.RED);
 		inoviceErrorLabel.setVisible(false);
 		inoviceErrorLabel.setBounds(12, 120, 11, 15);
 		paymentPanel.add(inoviceErrorLabel);
 		
 		final JLabel paypalErrorLabel = new JLabel("*");
 		paypalErrorLabel.setForeground(Color.RED);
 		paypalErrorLabel.setVisible(false);
 		paypalErrorLabel.setBounds(12, 147, 11, 15);
 		paymentPanel.add(paypalErrorLabel);
 		
 		final JTextField[] accountDeliveryInputs = { 	emailTextField,
 														passwordTextField,
 														repeatPasswordTextField,
 														adressTextField,
 														postalCodeTextField,
 														cityTextField };
 		final JRadioButton[] paymentInputs = {	cardRadioButton,
 												internetBankRadioButton,
 												inoviceRadioButton,
 												paypalRadioButton };
 		
 		ActionListener radioButtonListener = new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				JRadioButton radioButton = (JRadioButton) e.getSource();
 				if (radioButton == cardRadioButton)
 					cardPaymentPanel.setVisible(true);
 				else
 					cardPaymentPanel.setVisible(false);
 			}
 			
 		};
 		cardRadioButton.addActionListener(radioButtonListener);
 		internetBankRadioButton.addActionListener(radioButtonListener);
 		inoviceRadioButton.addActionListener(radioButtonListener);
 		paypalRadioButton.addActionListener(radioButtonListener);
 		
 		ActionListener cancelButtonListener = new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				NewUserFrame.this.dispose();
 			}
 			
 		};
 		firstCancelButton.addActionListener(cancelButtonListener);
 		secondCancelButton.addActionListener(cancelButtonListener);
 		
 		nextButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				boolean invalidInputFlag = false;
 				
 				if (emailTextField.getText().equals("") )
 					emailErrorLabel.setVisible(true);
 				else
 					emailErrorLabel.setVisible(false);
 				if ( passwordTextField.getText().equals("") || 
 						!passwordTextField.getText().equals(repeatPasswordTextField.getText())) {
 					passwordErrorLabel.setVisible(true);
 					invalidInputFlag = true;
 				} else
 					passwordErrorLabel.setVisible(false);
 				if ( repeatPasswordTextField.getText().equals("") || 
 						!passwordTextField.getText().equals(repeatPasswordTextField.getText())) {
 					repeatPasswordErrorLabel.setVisible(true);
 					invalidInputFlag = true;
 				} else
 					repeatPasswordErrorLabel.setVisible(false);
 				if ( adressTextField.getText().equals("") )
 					adressErrorLabel.setVisible(true);
 				else
 					adressErrorLabel.setVisible(false);
 				if ( postalCodeTextField.getText().equals("") )
 					postalCodeErrorLabel.setVisible(true);
 				else
 					postalCodeErrorLabel.setVisible(false);
 				if ( cityTextField.getText().equals("") )
 					cityErrorLabel.setVisible(true);
 				else
 					cityErrorLabel.setVisible(false);
 				
 				for(JTextField input : accountDeliveryInputs) {
 					if (input.getText().equals(""))
 						invalidInputFlag = true;
 				}
 				if (!invalidInputFlag)
 					tabbedPane.setSelectedIndex(1);
 			}
 		});
 		
 		doneButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				boolean invalidInputFlag = false;
 				
 				if ( buttonGroup.getSelection() == null ) {
 					invalidInputFlag = true;
 					cardErrorLabel.setVisible(true);
 					internetBankErrorLabel.setVisible(true);
 					inoviceErrorLabel.setVisible(true);
 					paypalErrorLabel.setVisible(true);
 				} else if ( 	cardRadioButton.isSelected() && (
 							cardNumberTextField.getText().equals("") ||
 							monthComboBox.getSelectedIndex() == 0 ||
 							yearComboBox.getSelectedIndex() == 0 ||
 							cvcTextField.getText().equals("") ||
 							nameTextField.getText().equals(""))) {
 					cardErrorLabel.setVisible(true);
 					internetBankErrorLabel.setVisible(false);
 					inoviceErrorLabel.setVisible(false);
 					paypalErrorLabel.setVisible(false);
 					invalidInputFlag = true;
 				} else
 					cardErrorLabel.setVisible(false);
 				
 				if ( !invalidInputFlag ) {
 					NewUserFrame.this.dispose();
 					if ( cardRadioButton.isSelected() )
 						loginFrame.createAccount();
 					else
 						loginFrame.createAccount();
 				}
 			}
 		});
 		
 		KeyAdapter actionKeyListener = new KeyAdapter() {
 			public void keyPressed(KeyEvent e) {
 				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
 					switch (tabbedPane.getSelectedIndex()) {
 					case 0:
 						nextButton.doClick();
 						break;
 					case 1:
 						doneButton.doClick();
 						break;
 					}
 				}
 				else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
 					firstCancelButton.doClick();
 				}
 			}
 		};
 		for (JTextField input : accountDeliveryInputs) {
 			input.addKeyListener(actionKeyListener);
 		}
 		for (JRadioButton input : paymentInputs) {
 			input.addKeyListener(actionKeyListener);
 		}
 		cardNumberTextField.addKeyListener(actionKeyListener);
 		monthComboBox.addKeyListener(actionKeyListener);
 		yearComboBox.addKeyListener(actionKeyListener);
 		cvcTextField.addKeyListener(actionKeyListener);
 		nameTextField.addKeyListener(actionKeyListener);
 		
 		
 		setVisible(true);
 	}
 }
