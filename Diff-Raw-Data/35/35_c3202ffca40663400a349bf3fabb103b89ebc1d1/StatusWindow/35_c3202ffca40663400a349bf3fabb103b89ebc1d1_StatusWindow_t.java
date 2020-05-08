 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cu.cs.cpsc215.crazy_mail.ui;
 
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JWindow;
 import net.miginfocom.swing.MigLayout;
 
 /**
  * @Author Kevin Jett
  * @author Emmanuel
  * 
  * This class is a user interface that is displayed when a message is delivered
  * 
  */
 public class StatusWindow extends JWindow {
 
 	private static final long serialVersionUID = -3922283714539226067L;
 
 	public StatusWindow(JFrame parent, String message){
         
         JPanel b_pane = new JPanel();
         b_pane.setLayout(new FlowLayout(FlowLayout.TRAILING));
         b_pane.setBackground(Color.white);
         
         JButton close = new JButton(new ImageIcon("close_small.png"));
         close.setBorderPainted(false);
         close.setContentAreaFilled(false);
         //close.setPreferredSize(new Dimension(15,15));
         close.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
         close.addActionListener(new ActionListener() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 StatusWindow.this.dispose();
             }
         });
         
         b_pane.add(close);
         
         
         JPanel contentPane = new JPanel();
         contentPane.setLayout(new MigLayout("wrap 1"));
         
         JLabel status = new JLabel(message);
         //status.setForeground(Color.GREEN);
         status.setFont(new Font(Font.SANS_SERIF,Font.BOLD, 14));
        
         contentPane.add(status,"gapleft 60");
             
         contentPane.setBackground(Color.white);
         
         add(b_pane,"North");
         add(contentPane);
         setSize(300,100);
         setLocationRelativeTo(parent);
         
         Point position =new Point();
         position.x = parent.getX()+parent.getWidth() - this.getWidth() - 15;
         position.y = parent.getY()+parent.getHeight() - this.getHeight() - 15;
         
         setLocation(position);
         setVisible(true);
         
     }

 }
