 /**
  * Jin - a chess client for internet chess servers.
  * More information is available at http://www.jinchess.com/.
  * Copyright (C) 2003 Alexander Maryanovsky.
  * All rights reserved.
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 
 package free.jin.ui;
 
 import free.jin.*;
 import free.util.BrowserControl;
 import free.util.StringEncoder;
 import free.util.Utilities;
 import free.workarounds.FixedJComboBox;
 import free.workarounds.FixedJPasswordField;
 import free.workarounds.FixedJTextField;
 
 import javax.swing.*;
 import javax.swing.border.TitledBorder;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.ResourceBundle;
 import java.util.StringTokenizer;
 
 
 
 
 
 
 
 /**
  * The login panel.
  */
 
 public class LoginPanel extends DialogPanel{
 
     private JDialog dialog;
     /**
      *  The resource bundle - translation effort.
      */
     
     private ResourceBundle translation = ResourceBundle.getBundle("free.jin.ui.loginpanel");
     /**
      * The list of servers, in the order we put them in the serverBox.
      */
     
     private Server [] servers;
     
     
     
     /**
      * The list of users, in the order we put them in the userBox.
      */
     
     private User [] users;
     
     
     
     /**
      * The JComboBox displaying the current server.
      */
     
     private final JComboBox serverBox;
     
     
     
     /**
      * The JComboBox displaying the current account (User).
      */
     
     private final JComboBox userBox;
     
     
     
     /**
      * The username field.
      */
     
     private final JTextField usernameField;
     
     
     
     /**
      * The password field.
      */
     
     private final JPasswordField passwordField;
     
     
     
     /**
      * The save password checkbox.
      */
     
     private final JCheckBox savePasswordCheckBox;
     
     
     
     /**
      * The retrieve password button.
      */
     
     private final JButton retrievePasswordButton;
     
     
     
     /**
      * The hostname box.
      */
     
     private final JComboBox hostnameBox;
     
     
     
     /**
      * The ports field.
      */
     
     private final JTextField portsField;
     
     
     
     /**
      * The "connect" button.
      */
     
     private final JButton connectButton;
     
     
     
     /**
      * The "register" button.
      */
     
     private final JButton registerButton;
     private String ErrorPort = LoginPanel.this.translation.getString("Bad_Ports");
     private String ErrorLine1 = LoginPanel.this.translation.getString("The_ports_text_field_must_contain_a");
     private String ErrorLine2 = LoginPanel.this.translation.getString("nonempty,_space_separated,_list_of_ports.");
     private String ErrorLine3 = LoginPanel.this.translation.getString("A_valid_port_is_a_number_between_0_and_65535.");
     private String ErrorLine21 = LoginPanel.this.translation.getString("nonempty_space_separated_list_of_ports.");
     private String ErrorLine22 = LoginPanel.this.translation.getString("A_valid_port_is_between_0_and_65535.");
 
 
     /**
      * Creates a new <code>LoginPanel</code> with the specified initial connection
      * details.
      */
 
     public LoginPanel(ConnectionDetails connDetails){
         this(connDetails.getServer(), connDetails);
     }
     
     
     
     /**
      * Creates a new <code>LoginPanel</code> with the specified initial server.
      */
     
     public LoginPanel(Server server){
         this(server, null);
     }
     
     
     
     /**
      * Creates a new <code>LoginPanel</code> with the specified server and
      * connection details.
      */
     
     private LoginPanel(Server server, ConnectionDetails connDetails){
 
 //        this.translation = ResourceBundle.getBundle("free.jin.ui.loginpanel");
         this.serverBox = new FixedJComboBox();
         this.userBox = new FixedJComboBox();
         this.usernameField = new FixedJTextField();
         this.passwordField = new FixedJPasswordField();
         this.savePasswordCheckBox = new JCheckBox(translation.getString("Save_password"));
         this.retrievePasswordButton = new JButton(translation.getString("Retrieve_Password"));
         this.hostnameBox = new FixedJComboBox();
         this.portsField = new FixedJTextField(7);
         this.connectButton = new JButton(translation.getString("Connect"));
         this.registerButton = new JButton(translation.getString("Register"));
         
         createUI();
         
         this.servers = Jin.getInstance().getServers();
         
         serverBox.setModel(createServerBoxModel());
         
         setData(server, connDetails);
 
     }
     
     
     
     /**
      * Sets the data currently displayed by the dialog. If the connection details
      * are <code>null</code>, it means that the user is creating a new account
      * from scratch (only the server is known).
      */
     
     private void setData(Server server, ConnectionDetails connDetails){
         serverBox.setSelectedIndex(Utilities.indexOf(servers, server));
         
         users = getServerUsers(server);
         userBox.setModel(createUserBoxModel());
         
         User user = connDetails == null ? null :
             Jin.getInstance().getUser(server, connDetails.getUsername());
         if (user == null)
             userBox.setSelectedIndex(0);
         else
             userBox.setSelectedIndex(Utilities.indexOf(users, user) + 1);
         
         if ((connDetails == null) || connDetails.isGuest()){
             usernameField.setText("");
             passwordField.setText("");
             savePasswordCheckBox.setSelected(false);
         } else{
             usernameField.setText(connDetails.getUsername());
             passwordField.setText(connDetails.getPassword());
             savePasswordCheckBox.setSelected(connDetails.isSavePassword());
         }
         
         hostnameBox.setModel(new DefaultComboBoxModel(server.getHosts()));
         hostnameBox.setSelectedItem(connDetails == null ? server.getDefaultHost() : connDetails.getHost());
         
         int [] ports = connDetails == null ? server.getPorts() : connDetails.getPorts();
         portsField.setText(StringEncoder.encodeIntList(ports));
         
         retrievePasswordButton.setVisible(server.getPasswordRetrievalPage() != null);
         
         registerButton.setVisible(server.getRegistrationPage() != null);
         
         if (!isShowing()){
             // Set default focused component
             if (usernameField.getText().length() == 0)
                 defaultFocusedComponent = usernameField;
             else if (passwordField.getPassword().length == 0)
                 defaultFocusedComponent = passwordField;
             else
                 defaultFocusedComponent = connectButton;
         }
     }
     
     
     
     /**
      * Creates the ComboBoxModel for the servers combo box.
      */
     
     private ComboBoxModel createServerBoxModel(){
         DefaultComboBoxModel model = new DefaultComboBoxModel();
         for (int i = 0; i < servers.length; i++)
             model.addElement(servers[i].getLongName());
         
         return model;
     }
     
     
     
     /**
      * Returns a list of known users on the specified server.
      */
     
     private User [] getServerUsers(Server server){
         ArrayList usersVec = new ArrayList();
         ListModel usersList = Jin.getInstance().getUsers();
         
         for (int i = 0; i < usersList.getSize(); i++){
             User user = (User)usersList.getElementAt(i);
             if (user.getServer() == server)
                 usersVec.add(user);
         }
         
         User [] usersArr = new User[usersVec.size()];
         usersArr = (User[]) usersVec.toArray(usersArr);
         return usersArr;
     }
     
     
     
     /**
      * Creates the ComboBoxModel for the users combo box.
      */
     
     private ComboBoxModel createUserBoxModel(){
         DefaultComboBoxModel model = new DefaultComboBoxModel();
         model.addElement(translation.getString("<New_account>"));
         
         for (int i = 0; i < users.length; i++)
             model.addElement(users[i].getUsername());
         
         return model;
     }
     
     
     
     /**
      * Displays this panel and returns the connection details specified by the
      * user. Returns <code>null</code> if the user closes the panel or otherwise
      * cancels the operation.
      */
     
     public ConnectionDetails askConnectionDetails(){
         return (ConnectionDetails)super.askResult();
     }
     
     
     
     /**
      * Returns the title for this panel.
      */
     
     protected String getTitle(){
         return translation.getString("Select_Login_Details");
     }
     
     
     
     /**
      * Displays an error panel with the specified title and message. Helper
      * function.
      */
     
     private void showError(String title, String message){
         OptionPanel panel = new OptionPanel(this, OptionPanel.ERROR, title,
                 new Object[]{OptionPanel.OK}, OptionPanel.OK, message);
         dialog = new JDialog();
         panel.show(this.dialog, this);
     }
     
     
     
     /**
      * Returns the currently selected server. Helper function.
      */
     
     private Server getServer(){
         return servers[serverBox.getSelectedIndex()];
     }
     
     
     
     /**
      * Creates and adds the user interface of this panel.
      */
     
     private void createUI(){
         serverBox.setEditable(false);
         serverBox.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent evt){
                 Server server = servers[serverBox.getSelectedIndex()];
                 User [] users = getServerUsers(server);
                 
                 // If there's only one known account, that's probably what the user wants to use
                 if (users.length == 1)
                     setData(server, users[0].getPreferredConnDetails());
                 else
                     setData(server, null);
             }
         });
         
         userBox.setEditable(false);
         userBox.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent evt){
                 if (userBox.getSelectedIndex() == 0)
                     setData(getServer(), null);
                 else
                     setData(getServer(), users[userBox.getSelectedIndex() - 1].getPreferredConnDetails());
             }
         });
         hostnameBox.setEditable(true);
         
         savePasswordCheckBox.setMnemonic('S');
         
         connectButton.setDefaultCapable(true);
         setDefaultButton(connectButton);
         connectButton.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent evt){
                 Server server = getServer();
                 String username = usernameField.getText();
                 UsernamePolicy policy = server.getUsernamePolicy();
                 String invalidityReason = policy.invalidityReason(username);
                 /*if (invalidityReason != null){
                     showError(translation.getString("Bad_Username"), invalidityReason);
                     return;
                 }*/
                 
                 int [] ports = parsePorts(portsField.getText());
                 if (ports == null){
                     System.err.println("LoginPanel.this.ErrorPort");
 
                     showError("Hey!" + ErrorPort, ErrorLine1 +
                             ErrorLine2 +
                             ErrorLine3);
                     return;
                 }
                 
                 String password = new String(passwordField.getPassword());
                 if ("".equals(password)) // An empty string indicates there is no password
                     password = null;       // but we want to let the user input it himself
                 
                 boolean savePassword = (savePasswordCheckBox != null) && savePasswordCheckBox.isSelected();
                 
                 User user = Jin.getInstance().getUser(server, username);
                 if (user == null)
                     user = new User(server, username);
                 
                 ConnectionDetails result = policy.isSame(username, policy.getGuestUsername()) ?
                     ConnectionDetails.createGuest(server, username, (String)hostnameBox.getSelectedItem(),
                         ports) :
                     ConnectionDetails.create(server, user, username, password, savePassword,
                         (String)hostnameBox.getSelectedItem(), ports);
                 
                 close(result);
             }
         });
         
         retrievePasswordButton.setMnemonic('t');
         retrievePasswordButton.setDefaultCapable(false);
         retrievePasswordButton.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent evt){
                 String url = getServer().getPasswordRetrievalPage();
                 if (!BrowserControl.displayURL(url))
                     BrowserControl.showDisplayBrowserFailedDialog(url, LoginPanel.this, true);
             }
         });
         
         JButton loginAsGuestButton = new JButton(translation.getString("Login_as_Guest"));
         loginAsGuestButton.setMnemonic('G');
         loginAsGuestButton.setDefaultCapable(false);
         loginAsGuestButton.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent evt){
                 int [] ports = parsePorts(portsField.getText());
                 if (ports == null){
                     showError(ErrorPort, ErrorLine1 +
                             ErrorLine21 +
                             ErrorLine22);
                     return;
                 }
                 
                 String username = getServer().getUsernamePolicy().getGuestUsername();
                 
                 close(ConnectionDetails.createGuest(getServer(), username,
                         (String)hostnameBox.getSelectedItem(), ports));
             }
         });
         
         registerButton.setMnemonic('R');
         registerButton.setDefaultCapable(false);
         registerButton.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent evt){
                 String url = getServer().getRegistrationPage();
                 if (!BrowserControl.displayURL(url))
                     BrowserControl.showDisplayBrowserFailedDialog(url, LoginPanel.this, true);
             }
         });
         
         JButton cancelButton = new JButton(LoginPanel.this.translation.getString("Cancel"));
         cancelButton.setDefaultCapable(false);
         cancelButton.addActionListener(new ActionListener(){
             public void actionPerformed(ActionEvent evt){
                 close(null);
             }
         });
         
         if (Jin.getInstance().getPasswordSaveWarning() != null){
             savePasswordCheckBox.addActionListener(new ActionListener(){
                 public void actionPerformed(ActionEvent evt){
                     if (savePasswordCheckBox.isSelected()){
                         OptionPanel optionPanel =
                                 new OptionPanel(LoginPanel.this,
                                 OptionPanel.WARNING, LoginPanel.this.translation.getString("Save_password?"),
                                 new Object[]{OptionPanel.YES, OptionPanel.NO},
                                 OptionPanel.YES, Jin.getInstance().getPasswordSaveWarning());
                         
                         if (optionPanel.display() != OptionPanel.YES)
                             savePasswordCheckBox.setSelected(false);
                     }
                 }
             });
         }
         
         
         
         // Create the main panel parts
         
         Component serverUserPanel = createServerUserPanel(serverBox, userBox);
         Component guestPanel = createGuestPanel(loginAsGuestButton, registerButton);
         Component advancedPanel = createAdvancedPanel(hostnameBox, portsField);
         Component membersPanel = createMembersPanel(usernameField, passwordField,
                 savePasswordCheckBox, retrievePasswordButton, connectButton);
         Component cancelPanel = createCancelPanel(cancelButton);
         
         // Add the main panels and layout
         setLayout(new BorderLayout(20, 20));
         
         Box upperPanel = new Box(BoxLayout.X_AXIS);
         Box lowerPanel = new Box(BoxLayout.X_AXIS);
         
         upperPanel.add(guestPanel);
         upperPanel.add(Box.createHorizontalStrut(10));
         upperPanel.add(advancedPanel);
         
         lowerPanel.add(membersPanel);
         lowerPanel.add(Box.createHorizontalStrut(10));
         lowerPanel.add(cancelPanel);
         
         Box vpanel = new Box(BoxLayout.Y_AXIS);
         vpanel.add(upperPanel);
         vpanel.add(Box.createVerticalStrut(10));
         vpanel.add(lowerPanel);
         
         add(serverUserPanel, BorderLayout.NORTH);
         add(vpanel, BorderLayout.CENTER);
         setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
     }
     
     
     
     /**
      * Creates the panel which lets the user choose the current server and user.
      */
     
     private Component createServerUserPanel(JComboBox serverBox, JComboBox userBox){
         JLabel serverLabel = new JLabel(translation.getString("Server:"));
         serverLabel.setLabelFor(serverBox);
         serverLabel.setDisplayedMnemonic('e');
         
         JLabel userLabel = new JLabel(translation.getString("Account:"));
         userLabel.setLabelFor(userBox);
         userLabel.setDisplayedMnemonic('A');
         
         Box inPanel = Box.createHorizontalBox();
         inPanel.add(Box.createHorizontalStrut(10));
         inPanel.add(serverLabel);
         inPanel.add(Box.createHorizontalStrut(5));
         inPanel.add(serverBox);
         inPanel.add(Box.createHorizontalStrut(20));
         inPanel.add(userLabel);
         inPanel.add(Box.createHorizontalStrut(5));
         inPanel.add(userBox);
         inPanel.add(Box.createHorizontalStrut(10));
         
         JPanel panel = new JPanel(new BorderLayout(10, 10));
         panel.add(BorderLayout.CENTER, inPanel);
         panel.add(BorderLayout.SOUTH, new JSeparator());
         
         return panel;
     }
     
     
     
     /**
      * Creates the guest panel.
      */
     
     private Component createGuestPanel(JButton loginAsGuestButton, JButton registerButton){
         Box vpanel = new Box(BoxLayout.Y_AXIS);
         vpanel.add(Box.createVerticalStrut(10));
         vpanel.add(loginAsGuestButton);
         vpanel.add(Box.createVerticalStrut(10));
         vpanel.add(registerButton);
         vpanel.add(Box.createVerticalStrut(10));
         
         Box hpanel = new Box(BoxLayout.X_AXIS);
         hpanel.add(Box.createHorizontalStrut(10));
         hpanel.add(vpanel);
         hpanel.add(Box.createHorizontalStrut(10));
         
         JPanel outerPanel = new JPanel(new BorderLayout());
         outerPanel.add(hpanel,BorderLayout.CENTER);
         outerPanel.setBorder(new TitledBorder(translation.getString("_Guests_")));
         
         return outerPanel;
     }
     
     
     
     /**
      * Creates the advanced options panel.
      */
     
     private Component createAdvancedPanel(JComboBox hostnameBox, JTextField portsField){
         JPanel hostnameLabelPanel = new JPanel(new GridLayout(3,1));
         hostnameLabelPanel.add(new JLabel(translation.getString("Server")));
         hostnameLabelPanel.add(new JLabel(translation.getString("Hostname_or")));
         JLabel hostnameLabel = new JLabel(translation.getString("IP_Number"));
         hostnameLabel.setDisplayedMnemonic('I');
         hostnameLabelPanel.add(hostnameLabel);
         
         Box hostnamePanel = new Box(BoxLayout.X_AXIS);
         
         hostnameBox.setFont(UIManager.getFont("TextField.font"));
         hostnameBox.setEditable(true);
         hostnameLabel.setLabelFor(hostnameBox);
         
         hostnamePanel.add(hostnameBox);
         hostnamePanel.add(Box.createHorizontalStrut(10));
         hostnamePanel.add(hostnameLabelPanel);
         
         
         Box portPanel = new Box(BoxLayout.X_AXIS);
         
         portsField.setMaximumSize(portsField.getPreferredSize());
         
         portPanel.add(portsField);
         portPanel.add(Box.createHorizontalStrut(10));
         JLabel portLabel = new JLabel(translation.getString("Ports"));
         portLabel.setDisplayedMnemonic('o');
         portLabel.setLabelFor(portsField);
         portPanel.add(portLabel);
         portPanel.add(Box.createHorizontalGlue());
         
         
         Box vpanel = new Box(BoxLayout.Y_AXIS);
         vpanel.add(hostnamePanel);
         vpanel.add(Box.createVerticalStrut(10));
         vpanel.add(portPanel);
         vpanel.add(Box.createVerticalStrut(10));
         
         Box hpanel = new Box(BoxLayout.X_AXIS);
         hpanel.add(Box.createHorizontalStrut(10));
         hpanel.add(vpanel);
         hpanel.add(Box.createHorizontalStrut(10));
         
         JPanel outerPanel = new JPanel(new BorderLayout());
         outerPanel.setBorder(new TitledBorder(translation.getString("_Advanced_Options_")));
         outerPanel.add(hpanel, BorderLayout.CENTER);
         
         return outerPanel;
     }
     
     
     
     /**
      * Creates the members panel.
      */
     
     private Component createMembersPanel(JTextField usernameField, JPasswordField passwordField,
             JCheckBox savePasswordCheckBox, JButton retrievePasswordButton, JButton connectButton){
         
         Box usernamePanel = new Box(BoxLayout.X_AXIS);
         
        usernameField.setPreferredSize(new Dimension(130, 20));
         usernamePanel.add(usernameField);
         usernamePanel.add(Box.createHorizontalStrut(10));
         JLabel handleLabel = new JLabel(translation.getString("Handle_(your_login_name)"));
         handleLabel.setDisplayedMnemonic('H');
         handleLabel.setLabelFor(usernameField);
         usernamePanel.add(handleLabel);
         usernamePanel.add(Box.createHorizontalGlue());
         usernamePanel.add(Box.createHorizontalStrut(10));
         
         
         Box passwordInputPanel = new Box(BoxLayout.X_AXIS);
         
        passwordField.setPreferredSize(new Dimension(130, 20));
         passwordInputPanel.add(passwordField);
         passwordInputPanel.add(Box.createHorizontalStrut(10));
         JLabel passwordLabel = new JLabel(translation.getString("Password"));
         passwordLabel.setDisplayedMnemonic('P');
         passwordLabel.setLabelFor(passwordField);
         passwordInputPanel.add(passwordLabel);
         passwordInputPanel.add(Box.createHorizontalGlue());
         passwordInputPanel.add(Box.createHorizontalStrut(10));
         
         int fieldsWidth = Math.max(usernameField.getPreferredSize().width, passwordField.getPreferredSize().width);
         usernameField.setMaximumSize(new Dimension(fieldsWidth, usernameField.getPreferredSize().height));
         passwordField.setMaximumSize(new Dimension(fieldsWidth, passwordField.getPreferredSize().height));
         usernameField.setPreferredSize(new Dimension(fieldsWidth, usernameField.getPreferredSize().height));
         passwordField.setPreferredSize(new Dimension(fieldsWidth, passwordField.getPreferredSize().height));
         
         usernameField.setColumns(0); // Otherwise setPreferredSize is ignored and it will still
         passwordField.setColumns(0); // use the amount of columns to calculate preferred size.
         
         Box passwordOptionsPanel = new Box(BoxLayout.X_AXIS);
         
         boolean addSavePasswordCB = savePasswordCheckBox != null;
         boolean addRetrievePassButton = retrievePasswordButton != null;
         
         if (addSavePasswordCB)
             passwordOptionsPanel.add(savePasswordCheckBox);
         
         if (addRetrievePassButton){
             if (addSavePasswordCB)
                 passwordOptionsPanel.add(Box.createHorizontalStrut(10));
             passwordOptionsPanel.add(retrievePasswordButton);
         }
         passwordOptionsPanel.add(Box.createHorizontalGlue());
         
         
         Box decisionPanel = new Box(BoxLayout.X_AXIS);
         
         decisionPanel.add(connectButton);
         decisionPanel.add(Box.createHorizontalGlue());
         
         Box vpanel = new Box(BoxLayout.Y_AXIS);
         vpanel.add(Box.createVerticalStrut(10));
         vpanel.add(usernamePanel);
         vpanel.add(Box.createVerticalStrut(10));
         vpanel.add(passwordInputPanel);
         vpanel.add(Box.createVerticalStrut(10));
         vpanel.add(passwordOptionsPanel);
         vpanel.add(Box.createVerticalStrut(10));
         vpanel.add(decisionPanel);
         vpanel.add(Box.createVerticalStrut(10));
         
         Box hpanel = new Box(BoxLayout.X_AXIS);
         hpanel.add(Box.createHorizontalStrut(10));
         hpanel.add(vpanel);
         hpanel.add(Box.createHorizontalGlue());
         
         
         JPanel outerPanel = new JPanel(new BorderLayout());
         outerPanel.setBorder(new TitledBorder(translation.getString("_Members_")));
         outerPanel.add(hpanel,BorderLayout.CENTER);
         
         return outerPanel;
     }
     
     
     
     /**
      * Creates the panel for the cancel button.
      */
     private Component createCancelPanel(JButton cancelButton){
         Box panel = new Box(BoxLayout.Y_AXIS);
         panel.add(Box.createVerticalGlue());
         panel.add(cancelButton);
         
         return panel;
     }
     
     
     
     /**
      * Parses the specified string as a list of space delimited ports. Returns
      * <code>null</code> if the string is invalid.
      */
     
     private static int [] parsePorts(String s){
         StringTokenizer tokenizer = new StringTokenizer(s, " ");
         int [] ports = new int[tokenizer.countTokens()];
         if (ports.length == 0)
             return null;
         
         try{
             for (int i = 0; i < ports.length; i++){
                 int port = Integer.parseInt(tokenizer.nextToken());
                 if ((port < 0) || (port > Short.MAX_VALUE))
                     return null;
                 ports[i] = port;
             }
         } catch (NumberFormatException e){
             return null;
         }
         
         return ports;
     }
     
     
     
     /**
      * The component focused by default.
      */
     
     private JComponent defaultFocusedComponent = null;
     
     
     
     // Hack to set the focus where we want it.
     public void paint(Graphics g){
         
         if (defaultFocusedComponent != null){
             defaultFocusedComponent.requestFocus();
             defaultFocusedComponent = null;
         }
         super.paint(g);
     }
     
     
     
 }
