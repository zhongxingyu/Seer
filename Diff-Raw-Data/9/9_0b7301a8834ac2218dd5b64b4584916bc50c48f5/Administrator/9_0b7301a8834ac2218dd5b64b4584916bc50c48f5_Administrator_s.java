 package view.administrator;
 
 import connectivity.QueryManager;
 import javax.swing.JDialog;
 import java.awt.Frame;
 import java.awt.Component;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.List;
 import java.util.ResourceBundle;
 import javax.swing.JOptionPane;
 import javax.swing.table.DefaultTableModel;
 import main.*;
 import model.User;
 
 /**
  *
  * @author IS101
  */
 public class Administrator extends javax.swing.JFrame {
 
     private final User user = new User();
     private final QueryManager query = new QueryManager();
 
     private List<User> users;
     private DefaultTableModel model;
 
     private Component succes, deleteUserPopup, createUserPopup, ErrorPopUp;
 
     public static String nameTypeToChange, accountToChange;
 
 //    private Locale locale = new Locale("nl", "NL");
     private final ResourceBundle BUNDLE = ResourceBundle.getBundle("languages.ResourceBundle"); //, locale
 
     public Administrator() {
         initComponents();
         model = (DefaultTableModel) this.userTable.getModel();
         searchUserTable(9999, "");
     }
 
     /**
      * Searches through the user database and loads the results into the first
      * user table. This method calls upon the searchUserList method to produce
      * the intended result.
      *
      * @param dbField specifies the field to search in. Setting this parameter
      * to 0 searches all fields.
      * @param searchArg argument used to search the database.
      */
     public void searchUserTable(int dbField, String searchArg) {
         model.setRowCount(0); //nodig voor 
         users = query.searchUserList(dbField, searchArg);
         for (User user : users) {
             model.addRow(new Object[]{user.getUsername(),
                 user.getFirstName(),
                 user.getLastName(),
                 (user.getPermissionId() == 1) ? BUNDLE.getString("employee")
                 : (user.getPermissionId() == 2) ? BUNDLE.getString("manager")
                 : BUNDLE.getString("administrator"),
                 (user.getIncorrectLogin() >= user.MAX_INCORRECT_LOGINS)
                 ? BUNDLE.getString("locked") : BUNDLE.getString("active")});
         }
     }
 
     /**
      * Creates a new User with the info supplied by the textFields and comboBox.
      */
     private void doCreateUser() {
 
         String newUsername = tfUsername.getText().trim();
         String newFirstName = tfFirstName.getText().trim();
         String newLastName = tfLastName.getText().trim();
         String newPassword = tfPassword.getText().trim();
         int newGroup = permissionSelector.getSelectedIndex() + 1;
         newUsername = newUsername.toLowerCase();
         query.createUser(newUsername, newFirstName, newLastName, newPassword, newGroup);
 
         // maakt alle textakken leeg
         clearFields();
         searchUserTable(9999, "");
     }
 
     /**
      * Method to display a yes-or no pop-up that confirms the user's choice.
      *
      * @param message The message you get for clicking the create button and
      * will be displayed a pop-up.
      * @return The confirmation for creating.
      */
     private boolean createPopUp(String message) {
         boolean createConfirm = false;
         final JOptionPane createUserPopPane = new JOptionPane(message,
                 JOptionPane.WARNING_MESSAGE,
                 JOptionPane.YES_NO_OPTION);
         final JDialog dialog = new JDialog((Frame) createUserPopup, BUNDLE.getString("attention"), true);
         dialog.setContentPane(createUserPopPane);
         createUserPopPane.addPropertyChangeListener(
                 new PropertyChangeListener() {
                     public void propertyChange(PropertyChangeEvent e) {
                         String prop = e.getPropertyName();
 
                         if (dialog.isVisible()
                         && (e.getSource() == createUserPopPane)
                         && (prop.equals(JOptionPane.VALUE_PROPERTY))) {
                             dialog.setVisible(false);
                         }
                     }
                 });
         dialog.pack();
         dialog.setVisible(true);
 
         int value = ((Integer) createUserPopPane.getValue()).intValue();
         if (value == JOptionPane.YES_OPTION) {
             createConfirm = true;
         }
         return createConfirm;
     }
 
     /**
      * Method used to clear all the text fields.
      */
     private void clearFields() {
         tfUsername.setText("");
         tfFirstName.setText("");
         tfLastName.setText("");
         tfPassword.setText("");
     }
 
     /**
      * Method used to create and display an error message and is used for error-
      * handling.
      *
      * @param errorMessage The message that will be displayed as a pop-up.
      */
     private void errorPopUp(String errorMessage) {
         JOptionPane.showMessageDialog(ErrorPopUp, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
     }
 
     /**
      * Method for checking input errors, will return a boolean if all fields are
      * filled in correctly.
      *
      * @return The correct input the user has filled in.
      */
     private boolean errorCheckCreateUser() {
         boolean isError[] = new boolean[4];
        boolean totalCorrectInput = false;
        
         String userName = tfUsername.getText();
         String passWord = tfPassword.getText();
         String firstName = tfFirstName.getText();
         String lastName = tfLastName.getText();
         String empty = "";
 
         if (userName.equals(empty)) {
             isError[0] = true;
             errorPopUp(BUNDLE.getString("enterUserName"));
         } else {
             isError[0] = false;
         }
 
         if (passWord.equals(empty)) {
             isError[1] = true;
             errorPopUp(BUNDLE.getString("enterPassword"));
         } else {
             isError[1] = false;
         }
 
         if (firstName.equals(empty)) {
             isError[2] = true;
             errorPopUp(BUNDLE.getString("enterFirstName"));
         } else {
             isError[2] = false;
         }
 
         if (lastName.equals(empty)) {
             isError[3] = true;
             errorPopUp(BUNDLE.getString("enterLastName"));
         } else {
             isError[3] = false;
         }
 
         for (int x = 0; x < isError.length; x++) {
             if (isError[x] == true) {
                 totalCorrectInput = false;
             }
         }
 
         return totalCorrectInput;
 
     }
 
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         jLayeredPane1 = new javax.swing.JLayeredPane();
         userOptionsPanel = new javax.swing.JPanel();
         unlockAccount = new javax.swing.JButton();
         resetPassword = new javax.swing.JButton();
         lockAccount = new javax.swing.JButton();
         deleteUser = new javax.swing.JButton();
         btEditUser = new javax.swing.JButton();
         userCreationPanel = new javax.swing.JPanel();
         tfFirstName = new javax.swing.JTextField();
         tfLastName = new javax.swing.JTextField();
         tfUsername = new javax.swing.JTextField();
         firstNameLabel = new javax.swing.JLabel();
         lastNameLabel = new javax.swing.JLabel();
         usernameLabel = new javax.swing.JLabel();
         passwordLabel = new javax.swing.JLabel();
         permissionLabel = new javax.swing.JLabel();
         tfPassword = new javax.swing.JPasswordField();
         permissionSelector = new javax.swing.JComboBox();
         clearFields = new javax.swing.JButton();
         createUser = new javax.swing.JButton();
         userTablePanel = new javax.swing.JPanel();
         refreshButton1 = new javax.swing.JButton();
         jScrollPane1 = new javax.swing.JScrollPane();
         userTable = new javax.swing.JTable();
         tfSearch = new javax.swing.JTextField();
         searchButton = new javax.swing.JButton();
         searchComboBox = new javax.swing.JComboBox();
         menuBar = new javax.swing.JMenuBar();
         userMenu = new javax.swing.JMenu();
         changePassword = new javax.swing.JMenuItem();
         logout = new javax.swing.JMenuItem();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         setTitle(BUNDLE.getString("administrator") + " - " + Session.storedFirstName + " " + Session.storedLastName);
         setIconImage(getToolkit().getImage(getClass().getResource("/img/corendon.png")));
         setMinimumSize(new java.awt.Dimension(720, 534));
 
         userOptionsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(BUNDLE.getString("userOptions")));
         userOptionsPanel.setPreferredSize(new java.awt.Dimension(173, 215));
 
         unlockAccount.setText(BUNDLE.getString("unlockAccount"));
         unlockAccount.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 unlockAccountActionPerformed(evt);
             }
         });
 
         resetPassword.setText(BUNDLE.getString("resetPassword"));
         resetPassword.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 resetPasswordActionPerformed(evt);
             }
         });
 
         lockAccount.setText(BUNDLE.getString("lockAccount"));
         lockAccount.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 lockAccountActionPerformed(evt);
             }
         });
 
         deleteUser.setText(BUNDLE.getString("deleteUser"));
         deleteUser.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 deleteUserActionPerformed(evt);
             }
         });
 
         btEditUser.setText(BUNDLE.getString("editInfo"));
         btEditUser.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btEditUserActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout userOptionsPanelLayout = new javax.swing.GroupLayout(userOptionsPanel);
         userOptionsPanel.setLayout(userOptionsPanelLayout);
         userOptionsPanelLayout.setHorizontalGroup(
             userOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(userOptionsPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(userOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(unlockAccount, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(lockAccount, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(resetPassword, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(deleteUser, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(btEditUser, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap())
         );
         userOptionsPanelLayout.setVerticalGroup(
             userOptionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(userOptionsPanelLayout.createSequentialGroup()
                 .addComponent(unlockAccount)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(lockAccount)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(resetPassword)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(deleteUser)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(btEditUser)
                 .addGap(0, 27, Short.MAX_VALUE))
         );
 
         userCreationPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(BUNDLE.getString("createUser")));
 
         tfFirstName.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 tfFirstNameActionPerformed(evt);
             }
         });
         tfFirstName.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 tfFirstNameKeyPressed(evt);
             }
         });
 
         tfLastName.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 tfLastNameActionPerformed(evt);
             }
         });
         tfLastName.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 tfLastNameKeyPressed(evt);
             }
         });
 
         tfUsername.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 tfUsernameActionPerformed(evt);
             }
         });
         tfUsername.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 tfUsernameKeyPressed(evt);
             }
         });
 
         firstNameLabel.setText(BUNDLE.getString("firstName"));
 
         lastNameLabel.setText(BUNDLE.getString("lastName"));
 
         usernameLabel.setText(BUNDLE.getString("username"));
 
         passwordLabel.setText(BUNDLE.getString("password"));
 
         permissionLabel.setText(BUNDLE.getString("userGroup"));
 
         tfPassword.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 tfPasswordActionPerformed(evt);
             }
         });
         tfPassword.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 tfPasswordKeyPressed(evt);
             }
         });
 
         permissionSelector.setModel(new javax.swing.DefaultComboBoxModel(BUNDLE.getStringArray("userTypes")));
         permissionSelector.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 permissionSelectorActionPerformed(evt);
             }
         });
         permissionSelector.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 permissionSelectorKeyPressed(evt);
             }
         });
 
         clearFields.setText(BUNDLE.getString("reset"));
         clearFields.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 clearFieldsActionPerformed(evt);
             }
         });
 
         createUser.setText(BUNDLE.getString("create"));
         createUser.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 createUserActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout userCreationPanelLayout = new javax.swing.GroupLayout(userCreationPanel);
         userCreationPanel.setLayout(userCreationPanelLayout);
         userCreationPanelLayout.setHorizontalGroup(
             userCreationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(userCreationPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(userCreationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(tfFirstName)
                     .addComponent(tfLastName)
                     .addComponent(tfUsername)
                     .addComponent(tfPassword)
                     .addGroup(userCreationPanelLayout.createSequentialGroup()
                         .addGap(0, 19, Short.MAX_VALUE)
                         .addComponent(clearFields)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(createUser))
                     .addGroup(userCreationPanelLayout.createSequentialGroup()
                         .addGroup(userCreationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addComponent(firstNameLabel)
                             .addComponent(lastNameLabel)
                             .addComponent(usernameLabel)
                             .addComponent(passwordLabel)
                             .addComponent(permissionLabel)
                             .addComponent(permissionSelector, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addGap(0, 0, Short.MAX_VALUE)))
                 .addContainerGap())
         );
         userCreationPanelLayout.setVerticalGroup(
             userCreationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(userCreationPanelLayout.createSequentialGroup()
                 .addComponent(firstNameLabel)
                 .addGap(1, 1, 1)
                 .addComponent(tfFirstName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(lastNameLabel)
                 .addGap(1, 1, 1)
                 .addComponent(tfLastName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(usernameLabel)
                 .addGap(1, 1, 1)
                 .addComponent(tfUsername, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(passwordLabel)
                 .addGap(1, 1, 1)
                 .addComponent(tfPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addComponent(permissionLabel)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(permissionSelector, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 32, Short.MAX_VALUE)
                 .addGroup(userCreationPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(clearFields)
                     .addComponent(createUser))
                 .addContainerGap())
         );
 
         userTablePanel.setBorder(javax.swing.BorderFactory.createTitledBorder(BUNDLE.getString("userOverview")));
 
         refreshButton1.setText(BUNDLE.getString("resetOverview"));
         refreshButton1.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 refreshButton1ActionPerformed(evt);
             }
         });
 
         userTable.setAutoCreateRowSorter(true);
         userTable.setModel(new javax.swing.table.DefaultTableModel(
             new Object [][] {
 
             },
             BUNDLE.getStringArray("userTableFields")
         ) {
             Class[] types = new Class [] {
                 java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class
             };
             boolean[] canEdit = new boolean [] {
                 false, false, false, false, false
             };
 
             public Class getColumnClass(int columnIndex) {
                 return types [columnIndex];
             }
 
             public boolean isCellEditable(int rowIndex, int columnIndex) {
                 return canEdit [columnIndex];
             }
         });
         userTable.setVerifyInputWhenFocusTarget(false);
         jScrollPane1.setViewportView(userTable);
 
         tfSearch.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 tfSearchActionPerformed(evt);
             }
         });
         tfSearch.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 tfSearchKeyPressed(evt);
             }
         });
 
         searchButton.setText(BUNDLE.getString("search"));
         searchButton.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 searchButtonActionPerformed(evt);
             }
         });
         searchButton.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 searchButtonKeyPressed(evt);
             }
         });
 
         searchComboBox.setModel(new javax.swing.DefaultComboBoxModel(BUNDLE.getStringArray("userSearchFields")));
         searchComboBox.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 searchComboBoxActionPerformed(evt);
             }
         });
 
         javax.swing.GroupLayout userTablePanelLayout = new javax.swing.GroupLayout(userTablePanel);
         userTablePanel.setLayout(userTablePanelLayout);
         userTablePanelLayout.setHorizontalGroup(
             userTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(userTablePanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(userTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(jScrollPane1)
                     .addGroup(userTablePanelLayout.createSequentialGroup()
                         .addComponent(tfSearch, javax.swing.GroupLayout.DEFAULT_SIZE, 144, Short.MAX_VALUE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(searchButton)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(searchComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(181, 181, 181)
                         .addComponent(refreshButton1)))
                 .addContainerGap())
         );
         userTablePanelLayout.setVerticalGroup(
             userTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(userTablePanelLayout.createSequentialGroup()
                 .addGap(6, 6, 6)
                 .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 583, Short.MAX_VALUE)
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(userTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                     .addComponent(refreshButton1)
                     .addComponent(tfSearch, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                     .addComponent(searchButton)
                     .addComponent(searchComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap())
         );
 
         userMenu.setText(BUNDLE.getString("options"));
 
         changePassword.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/changePassword.png"))); // NOI18N
         changePassword.setText(BUNDLE.getString("changePassword"));
         changePassword.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 changePasswordActionPerformed(evt);
             }
         });
         userMenu.add(changePassword);
 
         logout.setIcon(new javax.swing.ImageIcon(getClass().getResource("/img/logout.png"))); // NOI18N
         logout.setText(BUNDLE.getString("logout"));
         logout.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 logoutActionPerformed(evt);
             }
         });
         userMenu.add(logout);
 
         menuBar.add(userMenu);
 
         setJMenuBar(menuBar);
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addComponent(userCreationPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addComponent(userOptionsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 242, Short.MAX_VALUE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                 .addComponent(userTablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addComponent(userTablePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(userOptionsPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 172, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(userCreationPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(0, 0, Short.MAX_VALUE)))
                 .addContainerGap())
         );
 
         pack();
         setLocationRelativeTo(null);
     }// </editor-fold>//GEN-END:initComponents
 
     private void tfFirstNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfFirstNameActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_tfFirstNameActionPerformed
 
     private void tfLastNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfLastNameActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_tfLastNameActionPerformed
 
     private void tfUsernameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfUsernameActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_tfUsernameActionPerformed
 
     private void tfPasswordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfPasswordActionPerformed
         // TODO hier moet een refresh methode komen
     }//GEN-LAST:event_tfPasswordActionPerformed
 
     private void permissionSelectorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_permissionSelectorActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_permissionSelectorActionPerformed
 
     @SuppressWarnings("empty-statement")
     private void createUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createUserActionPerformed
         boolean totalCorrectInput = errorCheckCreateUser();
         if (totalCorrectInput == true) {
             doCreateUser();
         }
 
     }//GEN-LAST:event_createUserActionPerformed
 
     private void clearFieldsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearFieldsActionPerformed
         // maakt alle textakken leeg
         clearFields();
     }//GEN-LAST:event_clearFieldsActionPerformed
 
     private void refreshButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshButton1ActionPerformed
         searchUserTable(9999, "");
         tfSearch.setText("");
         searchComboBox.setSelectedIndex(0);
     }//GEN-LAST:event_refreshButton1ActionPerformed
 
     private void resetPasswordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetPasswordActionPerformed
         nameTypeToChange = "wachtwoord";
         boolean isError = false;
         try {
             accountToChange = userTable.getValueAt(userTable.getSelectedRow(), 0).toString();
         } catch (IndexOutOfBoundsException e) {
             errorPopUp(BUNDLE.getString("selectItemInTable"));
             isError = true;
         }
         System.out.println(accountToChange);
         System.out.println(Session.storedUsername);
         if (accountToChange.equals(Session.storedUsername)) {
             JOptionPane.showMessageDialog(succes, BUNDLE.getString("userLogged"));
         } else {
             Main.displayResetPassword();
         }
     }//GEN-LAST:event_resetPasswordActionPerformed
 
     private void lockAccountActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lockAccountActionPerformed
         boolean isError = false;
         try {
             query.lockUserAccount(userTable.getValueAt(userTable.getSelectedRow(),
                     0).toString(), user.MAX_INCORRECT_LOGINS);
         } catch (IndexOutOfBoundsException e) {
             errorPopUp(BUNDLE.getString("selectItemInTable"));
             isError = true;
         }
         if (isError == false) {
             searchUserTable(9999, "");
             JOptionPane.showMessageDialog(succes, BUNDLE.getString("accountIsLocked"));
         }
     }//GEN-LAST:event_lockAccountActionPerformed
 
     private void tfPasswordKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfPasswordKeyPressed
         if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
             boolean totalCorrectInput = errorCheckCreateUser();
             boolean isConfirm = false;
             if (totalCorrectInput) {
                 isConfirm = createPopUp(BUNDLE.getString("createUserPrompt"));
                 if (isConfirm) {
                     doCreateUser();
                 }
             }
         }
     }//GEN-LAST:event_tfPasswordKeyPressed
 
     private void tfUsernameKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfUsernameKeyPressed
         if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
             boolean totalCorrectInput = errorCheckCreateUser();
             boolean isConfirm = false;
             if (totalCorrectInput) {
                 isConfirm = createPopUp(BUNDLE.getString("createUserPrompt"));
                 if (isConfirm) {
                     doCreateUser();
                 }
             }
         }
 
     }//GEN-LAST:event_tfUsernameKeyPressed
 
     private void tfLastNameKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfLastNameKeyPressed
         if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
             boolean totalCorrectInput = errorCheckCreateUser();
             boolean isConfirm = false;
             if (totalCorrectInput) {
                 isConfirm = createPopUp(BUNDLE.getString("createUserPrompt"));
                 if (isConfirm) {
                     doCreateUser();
                 }
             }
         }
     }//GEN-LAST:event_tfLastNameKeyPressed
 
     private void tfFirstNameKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfFirstNameKeyPressed
         if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
             boolean totalCorrectInput = errorCheckCreateUser();
             boolean isConfirm = false;
             if (totalCorrectInput) {
                 isConfirm = createPopUp(BUNDLE.getString("createUserPrompt"));
                 if (isConfirm) {
                     doCreateUser();
                 }
             }
         }
 
     }//GEN-LAST:event_tfFirstNameKeyPressed
 
     private void permissionSelectorKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_permissionSelectorKeyPressed
         if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
             boolean totalCorrectInput = errorCheckCreateUser();
             boolean isConfirm = false;
             if (totalCorrectInput) {
                 isConfirm = createPopUp(BUNDLE.getString("createUserPrompt"));
                 if (isConfirm) {
                     doCreateUser();
                 }
             }
         }
     }//GEN-LAST:event_permissionSelectorKeyPressed
 
     private void tfSearchActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tfSearchActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_tfSearchActionPerformed
 
     private void unlockAccountActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_unlockAccountActionPerformed
         boolean isError = false;
         try {
             query.unlockUser(userTable.getValueAt(userTable.getSelectedRow(),
                     0).toString(), "0");
         } catch (IndexOutOfBoundsException e) {
             errorPopUp(BUNDLE.getString("selectItemInTable"));
             isError = true;
         }
         if (isError == false) {
             searchUserTable(9999, "");
             JOptionPane.showMessageDialog(succes, BUNDLE.getString("accountIsUnlocked"));
         }
     }//GEN-LAST:event_unlockAccountActionPerformed
 
     private void deleteUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteUserActionPerformed
         boolean isError = false;
         try {
             String check = userTable.getValueAt(userTable.getSelectedRow(), 0).toString();
         } catch (IndexOutOfBoundsException e) {
             errorPopUp(BUNDLE.getString("selectItemInTable"));
             isError = true;
         }
         if (isError == false) {
             createPopUp(BUNDLE.getString("deleteUserPrompt"));
             query.deleteUser(userTable.getValueAt(userTable.getSelectedRow(),
                     0).toString());
             searchUserTable(9999, "");
 
         }
     }//GEN-LAST:event_deleteUserActionPerformed
 
     private void searchComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchComboBoxActionPerformed
         // TODO add your handling code here:
     }//GEN-LAST:event_searchComboBoxActionPerformed
 
     private void searchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
         int searchField = searchComboBox.getSelectedIndex();
         searchUserTable(searchField, tfSearch.getText().trim());
     }//GEN-LAST:event_searchButtonActionPerformed
 
     private void searchButtonKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_searchButtonKeyPressed
         if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
             int searchField = searchComboBox.getSelectedIndex();
             searchUserTable(searchField, tfSearch.getText().trim());
         }
     }//GEN-LAST:event_searchButtonKeyPressed
 
     private void tfSearchKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_tfSearchKeyPressed
         if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
             int searchField = searchComboBox.getSelectedIndex();
             searchUserTable(searchField, tfSearch.getText().trim());
         }
     }//GEN-LAST:event_tfSearchKeyPressed
 
     private void btEditUserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btEditUserActionPerformed
         Session.tempUsername = userTable.getValueAt(userTable.getSelectedRow(), 0).toString();
         Main.displayChangeUser();
     }//GEN-LAST:event_btEditUserActionPerformed
 
     private void changePasswordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changePasswordActionPerformed
         Main.displayChangePassword();
     }//GEN-LAST:event_changePasswordActionPerformed
 
     private void logoutActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logoutActionPerformed
         dispose();
         Main.displayLogin();
     }//GEN-LAST:event_logoutActionPerformed
 
     /**
      * @param args the command line arguments
      */
 //    public static void main(String args[]) {
 //        /* Set the Nimbus look and feel */
 //        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
 //        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
 //         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
 //         */
 //        try {
 //            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
 //                if ("Nimbus".equals(info.getName())) {
 //                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
 //                    break;
 //                }
 //            }
 //        } catch (ClassNotFoundException ex) {
 //            java.util.logging.Logger.getLogger(Administrator.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
 //        } catch (InstantiationException ex) {
 //            java.util.logging.Logger.getLogger(Administrator.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
 //        } catch (IllegalAccessException ex) {
 //            java.util.logging.Logger.getLogger(Administrator.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
 //        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
 //            java.util.logging.Logger.getLogger(Administrator.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
 //        }
 //        //</editor-fold>
 //
 //        /* Create and display the form */
 //        java.awt.EventQueue.invokeLater(new Runnable() {
 //            public void run() {
 //                new Administrator().setVisible(true);
 //            }
 //        });
 //    }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btEditUser;
     private javax.swing.JMenuItem changePassword;
     private javax.swing.JButton clearFields;
     private javax.swing.JButton createUser;
     private javax.swing.JButton deleteUser;
     private javax.swing.JLabel firstNameLabel;
     private javax.swing.JLayeredPane jLayeredPane1;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JLabel lastNameLabel;
     private javax.swing.JButton lockAccount;
     private javax.swing.JMenuItem logout;
     private javax.swing.JMenuBar menuBar;
     private javax.swing.JLabel passwordLabel;
     private javax.swing.JLabel permissionLabel;
     private javax.swing.JComboBox permissionSelector;
     private javax.swing.JButton refreshButton1;
     private javax.swing.JButton resetPassword;
     private javax.swing.JButton searchButton;
     private javax.swing.JComboBox searchComboBox;
     private javax.swing.JTextField tfFirstName;
     private javax.swing.JTextField tfLastName;
     private javax.swing.JPasswordField tfPassword;
     private javax.swing.JTextField tfSearch;
     private javax.swing.JTextField tfUsername;
     private javax.swing.JButton unlockAccount;
     private javax.swing.JPanel userCreationPanel;
     private javax.swing.JMenu userMenu;
     private javax.swing.JPanel userOptionsPanel;
     private javax.swing.JTable userTable;
     private javax.swing.JPanel userTablePanel;
     private javax.swing.JLabel usernameLabel;
     // End of variables declaration//GEN-END:variables
 }
