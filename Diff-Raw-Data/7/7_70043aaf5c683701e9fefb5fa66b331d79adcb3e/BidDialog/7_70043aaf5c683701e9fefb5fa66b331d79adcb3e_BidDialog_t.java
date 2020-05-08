 package org.joedog.pinochle.view;
 
 import java.awt.Container;
 import java.awt.Component;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Rectangle;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.event.ComponentListener;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentAdapter;
 import java.beans.PropertyChangeEvent;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JRootPane;
 import javax.swing.SwingUtilities;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import org.joedog.pinochle.control.*;
 import org.joedog.pinochle.game.*;
 
 public class BidDialog extends JFrame implements View {
   private int       x, y;
   private int       value;
   private int       bid;
   private JComboBox bidList;
   private JButton   okay;
   private JButton   pass;
   private Container dialog;
   private JPanel    buttons;
   private JLabel    header;
   private JLabel    suit;
   public  boolean   paused;
   private GameController controller;
 
   /** 
    * Default constructor
    * This is a custome dialog which captures and stores
    * its position coordinates; the value returned from the
    * dialog must be called from instance.getValue();
    * <p>
    * @param  GameController  reference to controller for storage
    * @param  int             the current bid
    * @return BidDialog
    */
   public BidDialog(GameController controller, int bid) {
     this.bid        = bid;
     this.value      = -1;
     this.controller = controller;
     this.paused     = true;
     this.controller.addView(this);
     SwingUtilities.invokeLater(new Runnable() {
       public void run() {
         createAndShowGui();
       }
     });
   }
 
   /**
    * Returns a bid which was selected by the user
    * <p>
    * @param  none
    * @return int    the selected bid 
    */
   public int getValue() {
     while (true) {
       if (this.paused) {
         try {
           Thread.sleep(1000); 
         } catch (Exception e) {}
       } else {
         this.setVisible(false);
         this.controller.removeView(this);
         return this.value;
       }
     }
   }
 
   public void modelPropertyChange(PropertyChangeEvent e) {
     if (e.getNewValue() == null) return;
     if (e.getPropertyName().equals(controller.RESET)) {
       this.paused = false;
       this.value  = -1;
       this.controller.removeView(this);
       this.setVisible(false);
       this.dispose();
     }
   }
 
   private void createAndShowGui() {
     this.dialog = this.getContentPane();
     this.dialog.setLayout(null);
     this.dialog.add(this.getSuitLabel(), null);
     this.header = new JLabel();
     this.header.setBounds(new Rectangle(70, 10, 120, 20));
     this.header.setText("Select a bid:");
     this.dialog.add(this.header, null);
     this.dialog.add(getComboBox(bid), null);
     this.buttons = new JPanel();
     this.buttons.setBounds(new Rectangle(70, 60, 190, 35));
     this.buttons.setLayout(new FlowLayout());
     this.buttons.add(this.getOkayButton());
     this.buttons.add(this.getPassButton());
     this.dialog.add(buttons, null);
    this.setPreferredSize(new Dimension(268,146));
     JRootPane root = this.getRootPane();
     root.setDefaultButton(okay);
     this.addWindowFocusListener(new WindowAdapter() {
       @Override
       public void windowLostFocus(WindowEvent e) {
         toFront();
       }
       @Override
       public void windowGainedFocus(WindowEvent e) {
         okay.requestFocusInWindow();
       }
     });
     this.addComponentListener(new ComponentAdapter() {
       public void componentMoved(ComponentEvent e) {
        x  = getX() + 10;  
        y  = getY() + 10;  
       }
     });
     int xpos = controller.getIntProperty("DialogX");
     int ypos = controller.getIntProperty("DialogY"); 
     this.pack();
     this.setVisible(true);
     this.setLocation(xpos, ypos);
   }
 
   private int myX() {
     return this.x;
   }
 
   private int myY() {
     return this.y;
   }
 
   private JLabel getSuitLabel() {
     if (suit == null) {
       suit = new JLabel();
     }
     suit.setIcon(new TrumpIcon(Pinochle.SPADES));
     suit.setBounds(new Rectangle(15,5,50,50));
     return suit;
   }
 
   private JButton getOkayButton() {
     if (okay == null) {
       okay = new JButton();
       okay.setText("Okay");
       okay.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent e) {
           controller.setProperty("DialogX",  Integer.toString(myX()));
           controller.setProperty("DialogY",  Integer.toString(myY()));
           value  = (Integer)bidList.getSelectedItem();
           paused = false;
         }
       });
     }
     return okay;
   }
 
   private JButton getPassButton() {
     if (pass == null) {
       pass = new JButton();
       pass.setText("Pass");
       pass.addActionListener(new java.awt.event.ActionListener() {
         public void actionPerformed(java.awt.event.ActionEvent e) {
           controller.setProperty("DialogX",  Integer.toString(myX()));
           controller.setProperty("DialogY",  Integer.toString(myY()));
           value  = -1;
           paused = false;
         }
       });
     }
     return pass;
   }
 
   private JComboBox getComboBox(int bid) {
     Integer bids[] = new Integer[16];
 
     for (int i = 0; i < bids.length; i++) {
       bids[i] = (bid+i+1);
     }
     if (bidList == null) {
       bidList = new JComboBox(bids);
     }
     bidList.setSelectedIndex(0);
     bidList.setPreferredSize(new Dimension(60,20));
     bidList.setBounds(new Rectangle(70, 35, 160, 20));
     bidList.addItemListener(new ItemListener(){
       public void itemStateChanged(ItemEvent ie){
         value = (Integer)bidList.getSelectedItem();
       }
     }); 
     return bidList;
   }
 }
