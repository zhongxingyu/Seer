 package tutoring.ui;
 
 import java.awt.event.KeyEvent;
 import tutoring.helper.Login;
 
 public class LoginView extends javax.swing.JFrame
 {
 
     public LoginView()
     {
         initComponents();
        this.setExtendedState(this.getExtendedState() | this.MAXIMIZED_BOTH);
 
     }
 
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents()
     {
         java.awt.GridBagConstraints gridBagConstraints;
 
         jPanel1 = new javax.swing.JPanel();
         errorLabel = new javax.swing.JLabel();
         jPanel7 = new javax.swing.JPanel();
         jPanel3 = new javax.swing.JPanel();
         jPanel5 = new javax.swing.JPanel();
         jPanel2 = new javax.swing.JPanel();
         usernameLabel = new javax.swing.JLabel();
         usernameField = new javax.swing.JTextField();
         passwordLabel = new javax.swing.JLabel();
         passwordField = new javax.swing.JPasswordField();
         loginButton = new javax.swing.JButton();
         logoIcon = new javax.swing.JLabel();
         jLabel1 = new javax.swing.JLabel();
         jLabel2 = new javax.swing.JLabel();
         jPanel6 = new javax.swing.JPanel();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         setMinimumSize(new java.awt.Dimension(577, 476));
 
         jPanel1.setBackground(new java.awt.Color(255, 255, 255));
         jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
         jPanel1.setLayout(new java.awt.BorderLayout());
 
         errorLabel.setBackground(new java.awt.Color(255, 255, 255));
         errorLabel.setFont(new java.awt.Font("Lucida Grande", 2, 12)); // NOI18N
         errorLabel.setForeground(java.awt.Color.red);
         errorLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         jPanel1.add(errorLabel, java.awt.BorderLayout.PAGE_START);
 
         jPanel7.setBackground(new java.awt.Color(255, 255, 255));
 
         org.jdesktop.layout.GroupLayout jPanel7Layout = new org.jdesktop.layout.GroupLayout(jPanel7);
         jPanel7.setLayout(jPanel7Layout);
         jPanel7Layout.setHorizontalGroup(
             jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 799, Short.MAX_VALUE)
         );
         jPanel7Layout.setVerticalGroup(
             jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 0, Short.MAX_VALUE)
         );
 
         jPanel1.add(jPanel7, java.awt.BorderLayout.PAGE_END);
 
         jPanel3.setBackground(new java.awt.Color(255, 255, 255));
 
         org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
         jPanel3.setLayout(jPanel3Layout);
         jPanel3Layout.setHorizontalGroup(
             jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 100, Short.MAX_VALUE)
         );
         jPanel3Layout.setVerticalGroup(
             jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 438, Short.MAX_VALUE)
         );
 
         jPanel1.add(jPanel3, java.awt.BorderLayout.LINE_END);
 
         jPanel5.setBackground(new java.awt.Color(255, 255, 255));
         jPanel5.setLayout(new java.awt.GridBagLayout());
 
         jPanel2.setBackground(new java.awt.Color(255, 255, 255));
 
         usernameLabel.setBackground(new java.awt.Color(255, 255, 255));
         usernameLabel.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
         usernameLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
         usernameLabel.setText("Username:");
 
         usernameField.addKeyListener(new java.awt.event.KeyAdapter()
         {
             public void keyPressed(java.awt.event.KeyEvent evt)
             {
                 usernameFieldKeyPressed(evt);
             }
         });
 
         passwordLabel.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
         passwordLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
         passwordLabel.setText("Password:");
 
         passwordField.addKeyListener(new java.awt.event.KeyAdapter()
         {
             public void keyPressed(java.awt.event.KeyEvent evt)
             {
                 passwordFieldKeyPressed(evt);
             }
         });
 
         loginButton.setFont(new java.awt.Font("Lucida Grande", 1, 13)); // NOI18N
         loginButton.setForeground(new java.awt.Color(0, 153, 0));
         loginButton.setText("Login");
         loginButton.addActionListener(new java.awt.event.ActionListener()
         {
             public void actionPerformed(java.awt.event.ActionEvent evt)
             {
                 loginButtonActionPerformed(evt);
             }
         });
 
         logoIcon.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
         logoIcon.setIcon(new javax.swing.ImageIcon(getClass().getResource("/tutoring/images/pmslogo.PNG"))); // NOI18N
         logoIcon.addMouseListener(new java.awt.event.MouseAdapter()
         {
             public void mouseClicked(java.awt.event.MouseEvent evt)
             {
                 logoIconMouseClicked(evt);
             }
         });
 
         jLabel1.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
         jLabel1.setText("username = hpu_rocks");
 
         jLabel2.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
         jLabel2.setText("password = password");
 
         org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
         jPanel2.setLayout(jPanel2Layout);
         jPanel2Layout.setHorizontalGroup(
             jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(logoIcon, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 404, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
             .add(jPanel2Layout.createSequentialGroup()
                 .add(71, 71, 71)
                 .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(jPanel2Layout.createSequentialGroup()
                         .add(usernameLabel)
                         .add(6, 6, 6)
                         .add(usernameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 205, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                     .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                         .add(jPanel2Layout.createSequentialGroup()
                             .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                                 .add(jLabel1)
                                 .add(jLabel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 109, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                             .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                             .add(loginButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 106, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                         .add(jPanel2Layout.createSequentialGroup()
                             .add(passwordLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 72, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                             .add(6, 6, 6)
                             .add(passwordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 205, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))))
         );
         jPanel2Layout.setVerticalGroup(
             jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel2Layout.createSequentialGroup()
                 .add(logoIcon, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 233, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .add(0, 0, 0)
                 .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(usernameLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 28, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(usernameField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .add(6, 6, 6)
                 .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(passwordLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 28, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(passwordField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                     .add(jPanel2Layout.createSequentialGroup()
                         .add(jLabel1)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .add(jLabel2))
                     .add(loginButton))
                 .add(8, 8, 8))
         );
 
         gridBagConstraints = new java.awt.GridBagConstraints();
         gridBagConstraints.gridx = 0;
         gridBagConstraints.gridy = 0;
         gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
         gridBagConstraints.insets = new java.awt.Insets(0, 0, 19, 0);
         jPanel5.add(jPanel2, gridBagConstraints);
 
         jPanel1.add(jPanel5, java.awt.BorderLayout.CENTER);
 
         jPanel6.setBackground(new java.awt.Color(255, 255, 255));
         jPanel6.setPreferredSize(new java.awt.Dimension(103, 332));
 
         org.jdesktop.layout.GroupLayout jPanel6Layout = new org.jdesktop.layout.GroupLayout(jPanel6);
         jPanel6.setLayout(jPanel6Layout);
         jPanel6Layout.setHorizontalGroup(
             jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 103, Short.MAX_VALUE)
         );
         jPanel6Layout.setVerticalGroup(
             jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 438, Short.MAX_VALUE)
         );
 
         jPanel1.add(jPanel6, java.awt.BorderLayout.LINE_START);
 
         org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                 .addContainerGap()
                 .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(layout.createSequentialGroup()
                 .addContainerGap()
                 .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void loginButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loginButtonActionPerformed
 
        loginButton.setEnabled(false);
        
         login = new Login(usernameField.getText(), passwordField.getText());
         errorLabel.setText(login.loginFeedback());
 
         if (login.isLoggedIn())
         {
             if (login.getRole().getType().equalsIgnoreCase("SIA"))
             {
                 SIAView sia = new SIAView();
                sia.setVisible(true);
                 this.setVisible(false);
 
             } else if (login.getRole().getType().equalsIgnoreCase("ADMIN"))
             {
                 AdminView admin = new AdminView();
                admin.setVisible(true);
                 this.setVisible(false);
             }
         }
        ///loginButton.setEnabled(true);
     }//GEN-LAST:event_loginButtonActionPerformed
 
     private void usernameFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_usernameFieldKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER && loginButton.isEnabled())
         {
             loginButtonActionPerformed((java.awt.event.ActionEvent) loginButton.getAction());
         }
     }//GEN-LAST:event_usernameFieldKeyPressed
 
     private void passwordFieldKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_passwordFieldKeyPressed
        if (evt.getKeyCode() == KeyEvent.VK_ENTER && loginButton.isEnabled())
         {
             loginButtonActionPerformed((java.awt.event.ActionEvent) loginButton.getAction());
         }
     }//GEN-LAST:event_passwordFieldKeyPressed
 
     private void logoIconMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_logoIconMouseClicked
         if (count == 3)
         {
             count = 0;
             //go to SignInSignOutView
             SignInSignOut siso = new SignInSignOut();
             siso.show();
             this.setVisible(false);
 
         } else
         {
             count++;
         }
     }//GEN-LAST:event_logoIconMouseClicked
 
     public static void main(String args[])
     {
         /*
          * Set the Nimbus look and feel
          */
         //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
         /*
          * If Nimbus (introduced in Java SE 6) is not available, stay with the
          * default look and feel. For details see
          * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
          */
         try
         {
             for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels())
             {
                 if ("Nimbus".equals(info.getName()))
                 {
                     javax.swing.UIManager.setLookAndFeel(info.getClassName());
 
                     break;
                 }
             }
         } catch (ClassNotFoundException ex)
         {
             java.util.logging.Logger.getLogger(LoginView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (InstantiationException ex)
         {
             java.util.logging.Logger.getLogger(LoginView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (IllegalAccessException ex)
         {
             java.util.logging.Logger.getLogger(LoginView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         } catch (javax.swing.UnsupportedLookAndFeelException ex)
         {
             java.util.logging.Logger.getLogger(LoginView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
         }
         //</editor-fold>
 
         java.awt.EventQueue.invokeLater(new Runnable()
         {
             @Override
             public void run()
             {
                 new LoginView().setVisible(true);
             }
         });
     }
     static int count = 0;
     private Login login = new Login();
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JLabel errorLabel;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JPanel jPanel2;
     private javax.swing.JPanel jPanel3;
     private javax.swing.JPanel jPanel5;
     private javax.swing.JPanel jPanel6;
     private javax.swing.JPanel jPanel7;
     private javax.swing.JButton loginButton;
     private javax.swing.JLabel logoIcon;
     private javax.swing.JPasswordField passwordField;
     private javax.swing.JLabel passwordLabel;
     private javax.swing.JTextField usernameField;
     private javax.swing.JLabel usernameLabel;
     // End of variables declaration//GEN-END:variables
 }
