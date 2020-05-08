 import java.awt.BorderLayout;
 import java.awt.EventQueue;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 import java.awt.GridBagLayout;
 import javax.swing.JLabel;
 import java.awt.Font;
 import java.awt.GridLayout;
 
 import javax.swing.JComponent;
 import javax.swing.JTextField;
 import javax.swing.JTextPane;
 import javax.swing.DropMode;
 import javax.swing.JButton;
 import java.awt.Color;
 import javax.swing.SwingConstants;
 import javax.swing.border.BevelBorder;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import javax.swing.JPasswordField;
 import javax.swing.JTextArea;
 import javax.swing.UIManager;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.factories.FormFactory;
 import com.jgoodies.forms.layout.RowSpec;
 
 public class LoginFrame extends JFrame {
 
 	private JPanel contentPane;
 	private JPasswordField passwordTextField;
 	private NewUserFrame newUserFrame;
 
 	/**
 	 * Create the frame.
 	 */
 	public LoginFrame() {
 		setTitle("Välkommen!");
 		setResizable(false);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setBounds(100, 100, 571, 314);
 		contentPane = new JPanel();
 		contentPane.setBackground(UIManager.getColor("Panel.background"));
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		contentPane.setLayout(new FormLayout(new ColumnSpec[] {
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("322px"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("211px"),},
 			new RowSpec[] {
 				FormFactory.UNRELATED_GAP_ROWSPEC,
 				RowSpec.decode("15px"),
 				FormFactory.UNRELATED_GAP_ROWSPEC,
 				RowSpec.decode("178px"),
 				FormFactory.UNRELATED_GAP_ROWSPEC,
 				RowSpec.decode("37px"),}));
 		
 		JLabel titleLabel = new JLabel("iMat");
 		titleLabel.setForeground(Color.DARK_GRAY);
 		titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
 		contentPane.add(titleLabel, "2, 2, left, fill");
 		
 		JPanel loginPanel = new JPanel();
 		loginPanel.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
 		loginPanel.setBackground(UIManager.getColor("Panel.background"));
 		contentPane.add(loginPanel, "2, 4, fill, fill");
 		loginPanel.setLayout(new FormLayout(new ColumnSpec[] {
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("70px"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("219px"),},
 			new RowSpec[] {
 				RowSpec.decode("27px"),
 				RowSpec.decode("21px"),
 				RowSpec.decode("25px"),
 				RowSpec.decode("19px"),
 				RowSpec.decode("49px"),
 				RowSpec.decode("25px"),}));
 		
 		JLabel emailLabel = new JLabel("E-post");
 		emailLabel.setForeground(Color.DARK_GRAY);
 		loginPanel.add(emailLabel, "2, 2, fill, bottom");
 		
 		final JTextField emailTextField = new JTextField();
 		emailTextField.setFont(new Font("Dialog", Font.PLAIN, 12));
 		emailTextField.setBorder(null);
 		loginPanel.add(emailTextField, "4, 2, fill, fill");
 		
 		JLabel passwordLabel = new JLabel("Lösenord");
 		passwordLabel.setForeground(Color.DARK_GRAY);
 		loginPanel.add(passwordLabel, "2, 4, fill, center");
 		
 		final JButton loginButton = new JButton("Logga in");
 		loginButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				/*
				 * Skicka email och l��sen vidare
 				 */
 				execute_login();
 			}
 		});
 		loginPanel.add(loginButton, "4, 6, right, top");
 		
 		passwordTextField = new JPasswordField();
 		passwordTextField.setEchoChar('*');
 		passwordTextField.setBorder(null); //Ingen standard-border
 		loginPanel.add(passwordTextField, "4, 4, fill, fill");
 		
 		JPanel newUserPanel = new JPanel();
 		newUserPanel.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
 		newUserPanel.setBackground(Color.GRAY);
 		contentPane.add(newUserPanel, "4, 4, fill, fill");
 		newUserPanel.setLayout(new BorderLayout(0, 0));
 		
 		JButton createAccountButton = new JButton("Skapa konto");
 		createAccountButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				/*
				 * ��ppna kontoinfof��nster
 				 */
 				newUserFrame = new NewUserFrame(LoginFrame.this);
 			}
 		});
 		newUserPanel.add(createAccountButton, BorderLayout.SOUTH);
 		
 		JTextArea newUserTextArea = new JTextArea();
 		newUserTextArea.setForeground(Color.DARK_GRAY);
 		newUserTextArea.setFont(new Font("SansSerif", Font.PLAIN, 12));
 		newUserTextArea.setText("\n Fördelar med att skapa konto\n    - Enkel betalning\n    - Spara favoriter\n    - Se dina tidigare köp\n    - Spara listor");
 		newUserTextArea.setEditable(false);
 		newUserTextArea.setBackground(UIManager.getColor("Panel.background"));
 		newUserPanel.add(newUserTextArea, BorderLayout.CENTER);
 		
 		JPanel noLoginPanel = new JPanel();
 		noLoginPanel.setBorder(new BevelBorder(BevelBorder.RAISED, null, null, null, null));
 		noLoginPanel.setBackground(UIManager.getColor("Panel.background"));
 		contentPane.add(noLoginPanel, "2, 6, 3, 1, fill, fill");
 		noLoginPanel.setLayout(new BorderLayout(0, 0));
 		
 		JLabel noLoginLabel = new JLabel("Använd iMat utan att logga in -->");
 		noLoginLabel.setBackground(UIManager.getColor("Panel.background"));
 		noLoginLabel.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				/*
				 * G�� vidare till huvudf��nstret
 				 */
 				execute_noUser();
 			}
 		});
 		noLoginLabel.setForeground(Color.DARK_GRAY);
 		noLoginLabel.setHorizontalAlignment(SwingConstants.CENTER);
 		noLoginPanel.add(noLoginLabel, BorderLayout.CENTER);
 		
 		// Ordna så att tab och enter gör det de ska
 		KeyAdapter loginKeyPresses = new KeyAdapter() {
 			@Override
 			public void keyPressed(KeyEvent e) {
 				if (e.getKeyCode() == KeyEvent.VK_ENTER)
 					loginButton.doClick();
 			}
 		};
 		passwordTextField.addKeyListener(loginKeyPresses);
 		emailTextField.addKeyListener(loginKeyPresses);
 		
 	}
 	
 	public void createAccount() {
 		System.out.println("Creating account...");
 	}
 	
 	public void execute_noUser() {
 		System.out.println("Running without logging in...");
 	}
 	
 	public void execute_login() {
 		System.out.println("Running with user logged in...");
 	}
 	
 	public enum PaymentMethod {
 		CARD,
 		INTERNET,
 		INOVICE,
 		PAYPAL
 	}
 }
