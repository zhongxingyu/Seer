 package tfdhs.core.ui;
 
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Window;
 import java.net.Authenticator;
 import java.net.PasswordAuthentication;
 
 import javax.swing.JComponent;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPasswordField;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 
 import tfdhs.api.AuthenticatorResolver;
 
 /**
  * Display a dialog where the user can enter username/password.
  * 
  * @author frode
  * 
  */
 public class DialogAuthenticatorResolver extends Authenticator implements
 	AuthenticatorResolver {
 
     private final Window parent;
    private String INFO_AUTH_FORMAT = "To access this page, you must log in to \"%s\" on \"%s\".";
 
     /**
      * Create a new Authenticator resolver.
      * 
      * @param parent
      *            used to determine which fame this dialog is places in.
      */
     public DialogAuthenticatorResolver(Window parent) {
 	this.parent = parent;
 	initComponents();
     }
 
     private void initComponents() {
 	informationTextArea = new JTextArea();
 	informationTextArea.setName("informationTextArea");
 	informationTextArea.setLineWrap(true);
 	informationTextArea.setOpaque(false);
	informationTextArea.setEditable(false);
 
 	usernameField = new JTextField(20);
 	usernameField.setName("usernameField");
 
 	passwordField = new JPasswordField(20);
 	passwordField.setName("passwordField");
 
 	inputPane = new JOptionPane(new Object[] {
 		informationTextArea,
 		layout(2, new JLabel("Username:"), usernameField, new JLabel(
 			"Password:"), passwordField) },
 		JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
 
 	dialog = inputPane.createDialog(parent, "Authentication");
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public PasswordAuthentication authenticate() {
 	informationTextArea.setText(createAuthInformation());
 	dialog.pack();
 	dialog.setLocationRelativeTo(parent);
 	getDialog().setVisible(true);
 	getDialog().dispose();
 
 	if (userClickedOk()) {
 	    return new PasswordAuthentication(usernameField.getText(),
 		    passwordField.getPassword());
 	}
 
 	return null;
     }
 
     protected String createAuthInformation() {
 	return String.format(INFO_AUTH_FORMAT, getRequestingPrompt(),
 		getRequestingSite());
     }
 
     protected static JComponent layout(int cols, JComponent... components) {
 	JPanel panel = new JPanel();
 	panel.setLayout(new GridBagLayout());
 	GridBagConstraints c = new GridBagConstraints();
 	int x = 0;
 	int y = 0;
 	for (JComponent component : components) {
 	    c.gridx = x;
 	    c.gridy = y;
 	    x++;
 	    if (x == cols) {
 		x = 0;
 		y++;
 	    }
 	    panel.add(component, c);
 	}
 	return panel;
     }
 
     /**
      * @return credentials entered by user.
      */
     protected PasswordAuthentication getPasswordAuthentication() {
 	return authenticate();
     }
 
     protected boolean userClickedOk() {
 	return getInputPane().getValue() != JOptionPane.UNINITIALIZED_VALUE
 		&& ((Integer) getInputPane().getValue()).intValue() == JOptionPane.OK_OPTION;
 
     }
 
     protected JOptionPane getInputPane() {
 	return inputPane;
     }
 
     protected JDialog getDialog() {
 	return dialog;
     }
 
     protected void setUsername(String username) {
 	usernameField.setText(username);
     }
 
     protected void setPassword(String password) {
 	passwordField.setText(password);
     }
 
     private JTextArea informationTextArea;
     private JTextField usernameField;
     private JPasswordField passwordField;
     private JOptionPane inputPane;
     private JDialog dialog;
 
 }
