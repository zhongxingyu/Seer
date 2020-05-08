 package heraclite.gui.listeners;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JTextField;
 
 import network.ConnectionInformations;
 
 public class ConnectButtonListener implements ActionListener {
 
   JTextField ip;
   
   public ConnectButtonListener(JTextField ip) {
     this.ip = ip;
   }
   
   @Override
   public void actionPerformed(ActionEvent arg0) {
     try {
       ConnectionInformations ci = new ConnectionInformations("Zan", ip.getText());
      ci.connect();
     } catch (Exception e) {
       e.printStackTrace();
     }
   }
 
 }
