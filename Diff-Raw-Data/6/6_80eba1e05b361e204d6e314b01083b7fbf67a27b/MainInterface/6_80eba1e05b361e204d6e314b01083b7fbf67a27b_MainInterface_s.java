 package GUI;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.*;
 import java.applet.Applet;
 import utilities.Encryption;
 import datatstructures.EmployeeRecord;
 
 public class MainInterface extends JFrame implements ActionListener {
 
     JFrame f;
     Container contentPane;
     JPanel pnlLogos;
     JPanel pnlLogin;
     ImageIcon imgLogo;
     JLabel lblImg;
     JLabel lblEmployee;
     JLabel lblPass;
     JTextField txtEmployee;
     JTextField txtPass;
     JRadioButton rbtnManager;
     JRadioButton rbtnCashier;
     JButton btnAbout;
     JButton btnLogin;
     Font fntLabels;
     public static EmployeeRecord test[];
 
     public MainInterface(String title) {
         super(title);
         setLayout(null);
         init();
     }
 
     public static void main(String args[]) {
         MainInterface mainMenu = new MainInterface("Main Menu");
         mainMenu.setSize(1000, 800);
         mainMenu.setVisible(true);
         test = new EmployeeRecord[1];
         test[1].setFirstName("Chris");
         test[1].setLastName("Niesel");
         test[1].setEmployeeNumber(10001);
         test[1].setPassword("A94A8FE5CCB19BA61C4C0873D391E987982FBBD3");
 
     }
 
     public void init() {
 
         fntLabels = new Font("Britannic Bold", Font.BOLD, 20);
         initializeLogos();
         initializeLogin();
     }
 
     public void initializeLogos() {
         pnlLogos = new JPanel();
         pnlLogos.setLayout(null);
 
         imgLogo = new ImageIcon("Logo2.jpg");
         lblImg = new JLabel();
         lblImg.setIcon(imgLogo);
         lblImg.setBounds(0, 0, 1000, 210);
 
 
         pnlLogos.add(lblImg);
 
         pnlLogos.setBounds(0, 0, 1000, 210);
         add(pnlLogos);
 
     }
 
     public void initializeLogin() {
         pnlLogin = new JPanel();
         pnlLogin.setLayout(null);
         //Color cooBlue = new Color (163, 199, 253);
         Color cooBlue = new Color(190, 219, 246);
 
         /*Radio Buttons*/
         rbtnManager = new JRadioButton("Manager");
         rbtnManager.setBounds(310, 80, 160, 30);
         rbtnManager.setFont(fntLabels);
         rbtnManager.setBackground(cooBlue);
         rbtnCashier = new JRadioButton("Cashier");
         rbtnCashier.setBounds(550, 80, 160, 30);
         rbtnCashier.setFont(fntLabels);
         rbtnCashier.setBackground(cooBlue);
 
 
         /*Labels*/
         lblEmployee = new JLabel("Employee #");
         lblEmployee.setBounds(270, 230, 160, 30);
         lblEmployee.setFont(fntLabels);
         lblPass = new JLabel("Password");
         lblPass.setBounds(270, 310, 160, 30);
         lblPass.setFont(fntLabels);
 
         /*Text Fields*/
         txtEmployee = new JTextField(20);
         txtEmployee.setBounds(510, 225, 250, 40);
         txtEmployee.setFont(fntLabels);
         txtPass = new JTextField(20);
         txtPass.setBounds(510, 300, 250, 40);
         txtPass.setFont(fntLabels);
 
         /*Buttons*/
         btnAbout = new JButton("About");
         btnAbout.setBounds(850, 500, 100, 50);
         btnAbout.setFont(fntLabels);
         btnLogin = new JButton("Login");
         btnLogin.setBounds(450, 400, 100, 50);
         btnLogin.setFont(fntLabels);
         btnLogin.addActionListener(this);
 
 
         pnlLogin.add(rbtnManager);
         pnlLogin.add(rbtnCashier);
         pnlLogin.add(lblEmployee);
         pnlLogin.add(txtEmployee);
         pnlLogin.add(lblPass);
         pnlLogin.add(txtPass);
         pnlLogin.add(btnLogin);
         pnlLogin.add(btnAbout);
 
 
 
         pnlLogin.setBackground(cooBlue);
         pnlLogin.setBounds(0, 210, 1000, 600);
         add(pnlLogin);
 
 
     }
 
     public void checkLogin() throws Exception {
         int tempNumber = Integer.parseInt(txtEmployee.getText());
         String tempHash = Encryption.makeHash(txtPass.getText());
         System.out.println(tempHash);
         if (test[0].getPassword().equals(tempHash) && tempNumber == test[0].getEmployeeNumber()) {
             System.out.println("Password is correct");
         } else {
             txtEmployee.setText("");
             txtPass.setText("");
             System.out.println("User or Password incorrect");
         }
     }
 
     public void actionPerformed(ActionEvent e) {
         String arg = e.getActionCommand();
         Object source = e.getSource();
         this.setVisible(false);
         if (source == btnLogin) {
 
             System.out.println("Login Pressed");
             try {
                 checkLogin();
             } catch (Exception ex) {
                 Logger.getLogger(MainInterface.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
         // ManagerInterface manager = new ManagerInterface("Manager");
         // CashierInterface cashier = new CashierInterface("Cashier");
 
     }
 }
