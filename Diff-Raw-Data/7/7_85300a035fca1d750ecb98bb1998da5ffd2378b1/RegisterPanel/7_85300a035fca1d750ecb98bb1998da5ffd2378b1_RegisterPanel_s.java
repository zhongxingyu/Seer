 package rentalcar;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 public class RegisterPanel extends JPanel implements ActionListener{
     JLabel userN, passw, confPwd, type;
     JTextField userName, password, confirmP;
     JButton register, cancel;
     JComboBox userType;
         
     public RegisterPanel() {
         userN = new JLabel("Username: ");
         userName = new JTextField(20);
     
         passw = new JLabel("Password: ");
         password = new JTextField(20);
         
         confPwd = new JLabel("Confirm Password: ");
         confirmP = new JTextField(20);
         
         type = new JLabel("Type of User: ");
         
         cancel = new JButton("Cancel and Exit");
         cancel.addActionListener(this);
         register = new JButton("Register");
         register.addActionListener(this);
         
         
         this.add(userN);
         this.add(userName);
         this.add(passw);
         this.add(password);
         this.add(confPwd);
         this.add(confirmP);
         this.add(type);
         this.add(register);
     }
 
    @Override
     public void actionPerformed(ActionEvent e) {
         if(e.getSource() == register) {
             //TODO
         }
         else {
             //TODO
         }
     }
 }
