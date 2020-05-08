 package client;
 
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.table.DefaultTableModel;
 
 import main.Packet;
 import main.Connection.Command;
 
 
 public class RoomTextListener implements KeyListener {
     private JTable roomTable;
     private JTextField roomText;
     private ClientConnection conn;
     private JLabel roomLabel;
     public RoomTextListener(JTable roomTable, JTextField roomText, JLabel roomLabel, ClientConnection conn) {
         this.roomTable = roomTable;
         this.roomText = roomText;
         this.conn = conn;
         this.roomLabel = roomLabel;
     }
 
     @Override
     public void keyPressed(KeyEvent e) {
         if(e.getKeyCode() == KeyEvent.VK_ENTER) { 
             String room = roomText.getText();
             if (room.contains(" ")) {
                 JOptionPane.showMessageDialog(null, "Please type in a name without spaces.");
             }
             else {
                 if (!conn.roomExists(room)) {
                     List<String> messages = new ArrayList<String>();
                     conn.client.roomMessages.put(roomText.getText(), messages);
                     
                    DefaultTableModel model = (DefaultTableModel) roomTable.getModel();
                     model.addRow(new Object[]{"x", ">", room});
                    /*this.roomLabel.setText(room);
                     conn.join(room);
                     Packet message = new Packet(Command.LIST_USERS, room, Calendar.getInstance(), "", conn.getUsername());
                     conn.sendMessage(message);*/
                     
                 }
             }
             roomText.setText("");
             
         }
     }
 
     @Override
     public void keyReleased(KeyEvent arg0) {
         // TODO Auto-generated method stub
         
     }
 
     @Override
     public void keyTyped(KeyEvent arg0) {
         // TODO Auto-generated method stub
         
     }
 
 }
