 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 import java.awt.*;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.IOException;
 import java.net.URL;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import javax.imageio.ImageIO;
 import javax.swing.JOptionPane;
 import org.powerbot.core.event.events.MessageEvent;
 import org.powerbot.core.event.listeners.MessageListener;
 import org.powerbot.core.event.listeners.PaintListener;
 import org.powerbot.core.script.ActiveScript;
 import org.powerbot.core.script.job.state.Node;
 import org.powerbot.core.script.job.state.Tree;
 import org.powerbot.game.api.Manifest;
 import org.powerbot.game.api.methods.Game;
 import org.powerbot.game.api.methods.input.Mouse;
 import org.powerbot.game.api.methods.tab.Skills;
 import org.powerbot.game.api.methods.widget.Camera;
 import org.powerbot.game.api.util.Random;
 import org.powerbot.game.api.util.Timer;
 
 /**
  *
  * @author PimGame
  */
 @Manifest(authors = {"yPadScripting"}, name = "ySmelter", description = "Smelts all ")
 public class Smelter extends ActiveScript implements MouseListener, PaintListener, MessageListener {
 
     private Tree container = null;
     private List<Node> jobs = new ArrayList<Node>();
     private final LinkedList<MousePathPoint> mousePath = new LinkedList<MousePathPoint>();
     private boolean hide = false;
     private boolean guiWait = true;
     String currentStatus = "";
     Image background = getImage("http://i46.tinypic.com/vxi4cg.jpg");
     Image hidebuttonenabled = getImage("http://i50.tinypic.com/a08p4x.jpg");
     Image hidebuttondisabled = getImage("http://i50.tinypic.com/16i6wz4.jpg");
     Rectangle hidebutton = new Rectangle(497, 395, 20, 20);
     Font font1 = new Font("Verdana", 0, 20);
     Timer runTime = new Timer(0);
     public static double bars;
     public static String msg = "";
     public static String onDepletion = "";
     private double xpgained;
     private double startingxp;
     public static boolean ring = false;
     public static boolean rest = false;
     public static int restpercentage = 0;
     DecimalFormat df = new DecimalFormat("#,###,###.##");
     SmeltGUI1 g = new SmeltGUI1();
 
     @Override
     public void onStart() {
         System.out.println("Welcome to ySmelter!");
         startingxp = Skills.getExperience(Skills.SMITHING);
         g.setVisible(true);
         Camera.setPitch(90);
     }
 
     @Override
     public void onStop() {
         System.out.println("Bars made: " + df.format(bars));
         System.out.println("Experience gained: " + df.format(xpgained));
         System.out.println("Thank you for using ySmelter!");
         
         g.dispose();
     }
 
     @Override
     public int loop() {
 
 
 
         while (guiWait) {
             sleep(500);
         }
 
         if (container != null) {
             final Node job = container.state();
 
             if (job != null) {
                 currentStatus = job.toString();
                 container.set(job);
                 getContainer().submit(job);
                 job.join();
             }
         } else {
             jobs.add(new Banking());
             jobs.add(new WalkToFurnace());
             jobs.add(new Smelting());
             jobs.add(new WalkToBank());
             container = new Tree(jobs.toArray(new Node[jobs.size()]));
         }
         return Random.nextInt(150, 250);
     }
 
     @Override
     public void mouseClicked(MouseEvent e) {
         Point m = e.getPoint();
         if (hidebutton.contains(m)) {
             hide = !hide;
         }
     }
 
     @Override
     public void mousePressed(MouseEvent e) {
     }
 
     @Override
     public void mouseReleased(MouseEvent e) {
     }
 
     @Override
     public void mouseEntered(MouseEvent e) {
     }
 
     @Override
     public void mouseExited(MouseEvent e) {
     }
 
     @Override
     public void onRepaint(Graphics g1) {
 
         Graphics2D g = (Graphics2D) g1;
 
         if (Game.getClientState() == 11) {
             if (!hide) {                
                 xpgained = Skills.getExperience(Skills.SMITHING) - startingxp;
                 double time = (double) runTime.getElapsed();
                 DecimalFormat barsmade = new DecimalFormat("#,###,###");
 
                 g.drawImage(background, 7, 395, null);
                 g.setColor(Color.BLACK);
                 g.setFont(font1);
                 g.drawString("Status: " + "\n" + currentStatus, 7, 365); // Status
                 g.drawString(runTime.toElapsedString(), 350, 425); //Runtime
                 g.drawString(df.format((3600000 / time) * bars), 350, 450); //Ores per Hour
                 g.drawString(barsmade.format(bars), 350, 477); //Bars made
                 g.drawString(df.format(xpgained), 350, 505); //Exp Gained
                 g.drawImage(hidebuttondisabled, 497, 395, null);
             }
             if (hide) {
                 g.drawImage(hidebuttonenabled, 497, 395, null);
             }
         }
 
         //Mouse cursor
         g.setColor(Color.YELLOW);
         g.drawLine(Mouse.getX() - 5, Mouse.getY() - 5, Mouse.getX() + 5, Mouse.getY() + 5);
         g.drawLine(Mouse.getX() - 5, Mouse.getY() + 5, Mouse.getX() + 5, Mouse.getY() - 5);
 
         //Mouse trail
         while (!mousePath.isEmpty() && mousePath.peek().isUp()) {
             mousePath.remove();
         }
         Point clientCursor = Mouse.getLocation();
         MousePathPoint mpp = new MousePathPoint(clientCursor.x, clientCursor.y,
                 200); //Lasting time/MS
         if (mousePath.isEmpty() || !mousePath.getLast().equals(mpp)) {
             mousePath.add(mpp);
         }
         MousePathPoint lastPoint = null;
         for (MousePathPoint a : mousePath) {
             if (lastPoint != null) {
                 g.setColor(Color.YELLOW);//Trail color
                 g.drawLine(a.x, a.y, lastPoint.x, lastPoint.y);
             }
             lastPoint = a;
         }
     }
 
     @Override
     public void messageReceived(MessageEvent me) {
         msg = me.getMessage();
         if (msg.startsWith("You retrieve a bar")) {
             bars++;
         }
     }
 
     private Image getImage(String url) {
         try {
             return ImageIO.read(new URL(url));
         } catch (IOException e) {
             return null;
         }
     }
 
     @SuppressWarnings("serial")
     private class MousePathPoint extends Point { // All credits to Enfilade
 
         private long finishTime;
         private double lastingTime;
 
         public MousePathPoint(int x, int y, int lastingTime) {
             super(x, y);
             this.lastingTime = lastingTime;
             finishTime = System.currentTimeMillis() + lastingTime;
         }
 
         public boolean isUp() {
             return System.currentTimeMillis() > finishTime;
         }
     }
 
     private class SmeltGUI1 extends javax.swing.JFrame {
 
         /**
          * Creates new form SmeltGUI
          */
         public SmeltGUI1() {
             initComponents();
             ringBox.setEnabled(false);
         }
 
         /**
          * This method is called from within the constructor to initialize the
          * form. WARNING: Do NOT modify this code. The content of this method is
          * always regenerated by the Form Editor.
          */
         @SuppressWarnings("unchecked")
         // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
         private void initComponents() {
 
             jLabel1 = new javax.swing.JLabel();
             oreToSmelt = new javax.swing.JComboBox();
             jLabel2 = new javax.swing.JLabel();
             amountToSmelt = new javax.swing.JTextField();
             jLabel3 = new javax.swing.JLabel();
             startButton = new javax.swing.JButton();
             jScrollPane1 = new javax.swing.JScrollPane();
             jTextArea1 = new javax.swing.JTextArea();
             ringBox = new javax.swing.JCheckBox();
             restBox = new javax.swing.JCheckBox();
             restPercentage = new javax.swing.JTextField();
             jLabel4 = new javax.swing.JLabel();
             ringDepletionBox = new javax.swing.JComboBox();
             jLabel5 = new javax.swing.JLabel();
 
             setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
 
             jLabel1.setFont(new java.awt.Font("Tahoma", 0, 24)); // NOI18N
             jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
             jLabel1.setText("Welcome to ySmelt by yPad!");
 
             oreToSmelt.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"Bronze", "Iron", "Steel", "Mithril", "Adamantite", "Runite", "Gold", "Silver", " "}));
             oreToSmelt.addItemListener(new java.awt.event.ItemListener() {
 
                 public void itemStateChanged(java.awt.event.ItemEvent evt) {
                     oreToSmeltItemStateChanged(evt);
                 }
             });
 
             jLabel2.setText("Bars to make:");
 
             jLabel3.setText("Amount to smelt (0 = max):");
 
             startButton.setText("Start");
             startButton.addActionListener(new java.awt.event.ActionListener() {
 
                 public void actionPerformed(java.awt.event.ActionEvent evt) {
                     startButtonActionPerformed(evt);
                 }
             });
 
             jScrollPane1.setBorder(null);
             jScrollPane1.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
 
             jTextArea1.setColumns(20);
             jTextArea1.setEditable(false);
             jTextArea1.setFont(new java.awt.Font("Malgun Gothic", 0, 13)); // NOI18N
             jTextArea1.setLineWrap(true);
             jTextArea1.setRows(5);
             jTextArea1.setText("Start the script in either the Al-Kharid bank  or at the Al-Kharid furnace to start. Fill in the type of ore you wish to smelt and the amount of ores you wish to smelt.");
             jTextArea1.setWrapStyleWord(true);
             jTextArea1.setAutoscrolls(false);
             jTextArea1.setBorder(null);
             jTextArea1.setOpaque(false);
             jScrollPane1.setViewportView(jTextArea1);
 
             ringBox.setText("Ring of Forging");
             ringBox.setEnabled(false);
 
             restBox.setText("Rest");
 
             restPercentage.setText("0");
 
             jLabel4.setText("Percentage:");
 
             ringDepletionBox.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"Continue smelting", "Walk to bank"}));
 
             jLabel5.setText("On Ring depletion:");
 
             javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
             getContentPane().setLayout(layout);
             layout.setHorizontalGroup(
                     layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGap(40, 40, 40).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(restBox).addComponent(jLabel1).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false).addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING).addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(oreToSmelt, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(jLabel2).addComponent(ringBox).addComponent(restPercentage, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(jLabel4)).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGap(56, 56, 56).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING).addComponent(amountToSmelt, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(jLabel3).addComponent(startButton, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE))).addGroup(layout.createSequentialGroup().addGap(18, 18, 18).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jLabel5).addComponent(ringDepletionBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))))).addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
             layout.setVerticalGroup(
                     layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGap(21, 21, 21).addComponent(jLabel1).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 81, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jLabel2).addComponent(jLabel3)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(oreToSmelt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(amountToSmelt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(ringBox).addComponent(jLabel5)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED).addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(restBox).addComponent(ringDepletionBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(jLabel4).addGap(4, 4, 4).addComponent(restPercentage, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addGap(0, 8, Short.MAX_VALUE)).addGroup(layout.createSequentialGroup().addGap(0, 0, Short.MAX_VALUE).addComponent(startButton, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE))).addContainerGap()));
 
             pack();
         }// </editor-fold>                        
 
         private void oreToSmeltItemStateChanged(java.awt.event.ItemEvent evt) {
             if (oreToSmelt.getSelectedItem().toString().equals("Iron")) {
                 ringBox.setEnabled(true);
             } else {
                 ringBox.setEnabled(false);
             }
         }
 
         private void startButtonActionPerformed(java.awt.event.ActionEvent evt) {
             if (oreToSmelt.getSelectedItem().toString().equals("Bronze")) {
                 Variables.SELECTED_ORE = 436;
                 Variables.bar = "Bronze";
             }
             if (oreToSmelt.getSelectedItem().toString().equals("Iron")) {
                 Variables.SELECTED_ORE = 440;
                 Variables.bar = "Iron";
             }
             if (oreToSmelt.getSelectedItem().toString().equals("Steel")) {
                 Variables.SELECTED_ORE = 440;
                 Variables.bar = "Steel";
             }
             if (oreToSmelt.getSelectedItem().toString().equals("Mithril")) {
                 Variables.SELECTED_ORE = 447;
                 Variables.bar = "Mithril";
             }
             if (oreToSmelt.getSelectedItem().toString().equals("Adamantite")) {
                 Variables.SELECTED_ORE = 449;
                 Variables.bar = "Adamant";
             }
             if (oreToSmelt.getSelectedItem().toString().equals("Runite")) {
                 Variables.SELECTED_ORE = 451;
                 Variables.bar = "Rune";
             }
             if (oreToSmelt.getSelectedItem().toString().equals("Gold")) {
                 Variables.SELECTED_ORE = 444;
                 Variables.bar = "Gold";
             }
             if (oreToSmelt.getSelectedItem().toString().equals("Silver")) {
                 Variables.SELECTED_ORE = 442;
                 Variables.bar = "Silver";
             }
 
             if (amountToSmelt.getText().matches("[0-9]+")) {
                 Variables.oresAmount = Integer.parseInt(amountToSmelt.getText());
                 guiWait = false;
                 this.dispose();
             } else {
                 int exit = JOptionPane.showConfirmDialog(null, "You have entered an invalid amount of ores to smelt. \n Would you like me to smelt all ores available (Y), or do you want to alter your value (N)?", "Invalid amount", JOptionPane.YES_NO_OPTION);
                 if (exit == JOptionPane.YES_OPTION) {
                     Variables.oresAmount = 0;
                     guiWait = false;
                     this.dispose();
                 } else {
                 }
             }
 
             onDepletion = ringDepletionBox.getSelectedItem().toString();
 
             if (ringBox.isSelected()) {
                 ring = true;
             }
 
             if (restBox.isSelected()) {
                 rest = true;
                 if (restPercentage.getText().matches("[0-9]+")) {
                     int percentage = Integer.parseInt(restPercentage.getText());
                     if (percentage > 50) {
                         percentage = 50;
                     }
                     if (percentage < 0) {
                         percentage = 0;
                     }
                     restpercentage = percentage;
                 }
             }
         }
 
         /**
          * @param args the command line arguments
          */
         public void main(String args[]) {
             /*
              * Set the Nimbus look and feel
              */
             //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
         /*
              * If Nimbus (introduced in Java SE 6) is not available, stay with
              * the default look and feel. For details see
              * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
              */
             try {
                 for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                     if ("Nimbus".equals(info.getName())) {
                         javax.swing.UIManager.setLookAndFeel(info.getClassName());
                         break;
                     }
                 }
             } catch (ClassNotFoundException ex) {
                 java.util.logging.Logger.getLogger(SmeltGUI1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
             } catch (InstantiationException ex) {
                 java.util.logging.Logger.getLogger(SmeltGUI1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
             } catch (IllegalAccessException ex) {
                 java.util.logging.Logger.getLogger(SmeltGUI1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
             } catch (javax.swing.UnsupportedLookAndFeelException ex) {
                 java.util.logging.Logger.getLogger(SmeltGUI1.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
             }
             //</editor-fold>
 
             /*
              * Create and display the form
              */
             java.awt.EventQueue.invokeLater(new Runnable() {
 
                 public void run() {
                     new SmeltGUI1().setVisible(true);
                 }
             });
         }
         // Variables declaration - do not modify                     
         private javax.swing.JTextField amountToSmelt;
         private javax.swing.JLabel jLabel1;
         private javax.swing.JLabel jLabel2;
         private javax.swing.JLabel jLabel3;
         private javax.swing.JLabel jLabel4;
         private javax.swing.JLabel jLabel5;
         private javax.swing.JScrollPane jScrollPane1;
         private javax.swing.JTextArea jTextArea1;
         private javax.swing.JComboBox oreToSmelt;
         private javax.swing.JCheckBox restBox;
         private javax.swing.JTextField restPercentage;
         private javax.swing.JCheckBox ringBox;
         private javax.swing.JComboBox ringDepletionBox;
         private javax.swing.JButton startButton;
         // End of variables declaration                   
     }
 }
