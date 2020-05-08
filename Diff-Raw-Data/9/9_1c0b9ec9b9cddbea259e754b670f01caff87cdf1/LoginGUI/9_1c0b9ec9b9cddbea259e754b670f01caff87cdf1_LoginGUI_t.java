 package de.fhkoeln.gm.serientracker.client.gui;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPasswordField;
 import javax.swing.JTextField;
 import javax.swing.UIManager;
 
 import net.miginfocom.swing.MigLayout;
 import de.fhkoeln.gm.serientracker.client.TrackerClient;
 import de.fhkoeln.gm.serientracker.client.utils.LoginHandler;
 import de.fhkoeln.gm.serientracker.xmpp.XMPPConfig;
 
 /**
  * Provides the login GUI.
  *
  * @author Dominik Schilling
  */
 public class LoginGUI extends JFrame implements ActionListener {
 
 	private static final long serialVersionUID = 1L;
 
 	// GUI components
 	private JTextField inputUsername;
 	private JPasswordField inputPassword;
 	private JTextField inputHostname;
 	private JTextField inputPort;
 	private JButton buttonLogin;
 	private JButton buttonRegister;
 
 	/**
 	 * Constructor.
 	 * Sets the UI look and inits components.
 	 */
 	public LoginGUI() {
 		try {
 			UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
 		} catch ( Exception e ) {
 		}
 
 		initComponents();
 	}
 
 	/**
 	 * Sets up the GUI components.
 	 */
 	public void initComponents() {
 		// Close when exit
 		setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
 
 		// Set frame title
 		setTitle( "SERIENTRACKER | LOGIN" );
 
 		// Set minimum frame size (x, y, width, height)
 		setBounds( 0, 0, 300, 220 );
 
 		// Center frame on screen
 		setLocationRelativeTo( null );
 
 		// Disable resizing
 		setResizable( false );
 
 		// Content Panel
 		JPanel panel = new JPanel( new MigLayout( "", "[100][grow]", "[][][][][grow]" ) );
 		add( panel );
 
 		// Label for username
 		JLabel labelUsername = new JLabel( "Username:" );
 
 		// Label for password
 		JLabel labelPassword = new JLabel( "Password:" );
 
 		// Label for hostname
 		JLabel lableHostname = new JLabel( "Hostname:" );
 
 		// Label for port
 		JLabel labelPort = new JLabel( "Port:" );
 
 		// Input field for username
 		inputUsername = new JTextField();
 		inputUsername.setText( "test" ); // TODO
 		inputUsername.setActionCommand( "RETURN" );
 		inputUsername.addActionListener( this );
 
 		// Input field for password
 		inputPassword = new JPasswordField();
 		inputPassword.setText( "test" ); // TODO
 		inputPassword.setActionCommand( "RETURN" );
 		inputPassword.addActionListener( this );
 
 		// Input field for hostname
 		inputHostname = new JTextField();
 		inputHostname.setText( XMPPConfig.hostname );
 		inputHostname.setActionCommand( "RETURN" );
 		inputHostname.addActionListener( this );
 
 		// Input field for port
 		inputPort = new JTextField();
 		inputPort.setText( String.valueOf( XMPPConfig.port ) );
 		inputPort.setActionCommand( "RETURN" );
 		inputPort.addActionListener( this );
 
 		// Login button
 		buttonLogin = new JButton( "Login" );
 		buttonLogin.addActionListener( this );
 
 		// Register button
 		buttonRegister = new JButton( "Register" );
 		buttonRegister.addActionListener( this );
 
 		// Add items to panel
 		panel.add( labelUsername, "cell 0 0" );
 		panel.add( labelPassword, "cell 0 1"  );
 		panel.add( lableHostname, "cell 0 2" );
 		panel.add( labelPort, "cell 0 3" );
 
 		panel.add( inputUsername, "cell 1 0, grow" );
 		panel.add( inputPassword, "cell 1 1, grow" );
 		panel.add( inputHostname, "cell 1 2, grow" );
 		panel.add( inputPort, "cell 1 3, grow" );
 
 		panel.add( buttonRegister, "cell 0 4" );
 		panel.add( buttonLogin, "cell 1 4, right" );
 
 	}
 
 	/**
 	 * Check user input and try to connect/login to the XMPP server.
 	 */
 	public void loginActionPerformed() {
 		// Check username
 		String username = inputUsername.getText().trim();
 		if ( username.length() <= 0 ) {
 			errorDialog( "Name must not be empty." );
 			inputUsername.requestFocusInWindow();
 			return;
 		}
 
 		// Check password
 		char[] password = inputPassword.getPassword();
 		if ( password.length == 0 ) {
 			errorDialog( "Password must not be empty." );
 			inputPassword.requestFocusInWindow();
 			return;
 		}
 
 		// Check hostname
 		String hostname = inputHostname.getText().trim();
 		if ( hostname.length() <= 0 ) {
 			errorDialog( "Hostname must not be empty." );
 			inputHostname.requestFocusInWindow();
 			return;
 		}
 
 		// Check port
 		int port = -1;
 		try {
 			port = Integer.parseInt( inputPort.getText() );
 		} catch ( NumberFormatException e1 ) {}
 
 		if ( port < 0 || port > 65535 ) {
 			errorDialog( "Port must not be empty and between 0 and 65535." );
 			inputPort.requestFocusInWindow();
 			return;
 		}
 
 		// Call login handler and execute the login
 		LoginHandler loginHandler = new LoginHandler( username, password, hostname, port );
 		loginHandler.execute();
 
 		if ( loginHandler.hasError() ) {
 			this.errorDialog( loginHandler.getErrorMessage() );
 		} else {
 			TrackerClient.showMain();
 		}
 	};
 
 	/**
 	 * Helper method to show an error dialog.
 	 *
 	 * @param String message
 	 */
 	private void errorDialog( String message ) {
 		JOptionPane.showMessageDialog( null, message, "Error", JOptionPane.ERROR_MESSAGE );
 	}
 
 	/**
 	 * Event listener for button actions.
 	 *
 	 * @param ActionEvent e
 	 */
 	@Override
 	public void actionPerformed( ActionEvent e ) {
 		// Login action
 		if ( e.getSource() == buttonLogin || e.getActionCommand().equals( "RETURN" ) ) {
 			this.loginActionPerformed();
 		}
 		if ( e.getSource() == buttonRegister ) {
 			this.registerActionPerformed();
 		}
 	}
 
 	private void registerActionPerformed() {
		TrackerClient.showRegister();
 	}
 
 }
