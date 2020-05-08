 package com.dlepla.db9000;
 
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 
 // Login Panel class which sets up a JPanel with several Boxes to display the initial login screen.
 
 import javax.swing.*;
 
 import com.dlepla.db9000.lib.Reference;
 
 class LoginPanel extends JPanel
 {
     /**
      * 
      */
     private static final long serialVersionUID = -7284737752069591276L;
     
     JFrame mainWindow;
     JTextField username;
     JPasswordField password;
     JButton loginButton;
     JLabel headerTitle;
     JLabel overTitle;
 
     //Login Panel constructor which initializes and builds the layout of the LoginPanel window.
     
     public LoginPanel(JFrame main)
     {
     
     mainWindow = main;    
     this.setLayout(new GridBagLayout());
     this.setBackground(Reference.CENTER_BACKGROUND_COLOR);
     this.setOpaque(true);
     
     Box htextBox = Box.createHorizontalBox();
     headerTitle = new JLabel("Debt Blaster 9000");
     headerTitle.setFont(new Font("Elephant", Font.PLAIN, 31));
     headerTitle.setForeground(Reference.HEADER_TEXT_COLOR);
     
     htextBox.add(Box.createHorizontalStrut(198));
     htextBox.add(Box.createHorizontalGlue());
     htextBox.add(headerTitle);
     htextBox.add(Box.createHorizontalGlue());
     htextBox.add(Box.createHorizontalStrut(198));
     
     Box hbBox = Box.createHorizontalBox();
     hbBox.setOpaque(true);
     hbBox.setBackground(Reference.HEADER_BORDER_COLOR);
     hbBox.add(Box.createVerticalStrut(6));
     
     Box headerBox = Box.createVerticalBox();
     headerBox.setOpaque(true);
     headerBox.setBackground(Reference.HEADER_BACKGROUD_COLOR);
     headerBox.add(Box.createVerticalStrut(16));
     headerBox.add(htextBox);
     headerBox.add(Box.createVerticalStrut(16));
     headerBox.add(hbBox);
     Reference.addItem(this, headerBox, 0, 0, 1, 1, GridBagConstraints.CENTER,
             GridBagConstraints.BOTH);
     
     Box upLabelBox = Box.createVerticalBox();
     upLabelBox.add(new JLabel("Username: "));
     upLabelBox.add(Box.createRigidArea(new Dimension(6, 8)));
     upLabelBox.add(new JLabel("Password: "));
     upLabelBox.add(Box.createRigidArea(new Dimension(75, 7)));
     
     Box plBox = Box.createVerticalBox();
     username = new JTextField(22);
     username.setMaximumSize(username.getPreferredSize());
     password = new JPasswordField(22);
     password.setMaximumSize(password.getPreferredSize());
     JLabel plLabel = new JLabel("Please Login");
     plLabel.setFont(new Font("Elephant", Font.PLAIN, 16));
     loginButton = new JButton("Login");
     ButtonListener bll = new ButtonListener();
     loginButton.addActionListener(bll);
     plBox.add(Box.createVerticalStrut(75));
     plBox.add(plLabel);
     plBox.add(Box.createRigidArea(new Dimension(0, 5)));
     plBox.add(username);
     plBox.add(Box.createRigidArea(new Dimension(0, 5)));
     plBox.add(password);
     plBox.add(Box.createRigidArea(new Dimension(0, 10)));
     plBox.add(loginButton);
     plBox.add(Box.createVerticalStrut(75));
     
     Box centerBox = Box.createHorizontalBox();
     centerBox.add(Box.createHorizontalStrut(173));
     centerBox.add(Box.createHorizontalGlue());
     centerBox.add(upLabelBox);
     centerBox.add(Box.createRigidArea(new Dimension(5, 7)));
     centerBox.add(plBox);
     centerBox.add(Box.createHorizontalGlue());
     centerBox.add(Box.createHorizontalStrut(173));
     Reference.addItem(this, centerBox, 0, 1, 1, 1, GridBagConstraints.CENTER,
             GridBagConstraints.HORIZONTAL);
     
     Box footerBox = Box.createHorizontalBox();
     footerBox.setOpaque(true);
     footerBox.setBackground(Reference.FOOTER_BACKGROUND_COLOR);
     footerBox.add(Box.createVerticalStrut(110));
     Reference.addItem(this, footerBox, 0, 2, 1, 1, GridBagConstraints.SOUTH,
             GridBagConstraints.BOTH);
   
     }
     
     
     private class ButtonListener implements ActionListener
     {
         @Override
         public void actionPerformed(ActionEvent e)
         {
 
             if (e.getSource() == loginButton)
             {
                 String name = username.getText();
                 char[] pass = password.getPassword();
                 boolean loginAuthorized = true;
                 loginAuthorized = Reference.authLogin(name, pass);
                 System.out.print("loginAuthorized = " + loginAuthorized);
                 if (name.length() <= 0)
                 {
                     JOptionPane
                             .showMessageDialog(
                                     getRootPane(),
                                     "Username is blank. You must enter a username and password.",
                                     "Invalid Login",
                                     JOptionPane.INFORMATION_MESSAGE);
                 } else if (pass.length == 0)
                 {
                     JOptionPane
                             .showMessageDialog(
                                     getRootPane(),
                                     "Password is blank. You must enter a username and password.",
                                     "Invalid Login",
                                     JOptionPane.INFORMATION_MESSAGE);
                 } else if (loginAuthorized == true)
                 {
                     JOptionPane
                             .showMessageDialog(getRootPane(),
                                     "Username and password correct, opening Overview window!");
                     
                     
                     
                     OverPanel overPanel = new OverPanel();
                     
                     mainWindow.getContentPane().removeAll();
                     mainWindow.getContentPane().add(overPanel);
                     mainWindow.getContentPane().doLayout();
                    update(mainWindow.getGraphics());
                     mainWindow.pack();
                    //mainWindow.setVisible(true);
                     
                 } else
                 {
                     JOptionPane
                             .showMessageDialog(
                                     getRootPane(),
                                     "Username or password is not found, please try again or contact administrator",
                                     "Invalid Login",
                                     JOptionPane.INFORMATION_MESSAGE);
                     
                 }
             }
         }
     }
 }
