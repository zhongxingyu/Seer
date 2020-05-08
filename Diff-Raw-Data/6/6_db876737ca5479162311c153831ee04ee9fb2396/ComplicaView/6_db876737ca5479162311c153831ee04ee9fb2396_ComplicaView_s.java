 /*
  * ComplicaView.java
  */
 
 package complica;
 
 import java.awt.Color;
 import org.jdesktop.application.Action;
 import org.jdesktop.application.ResourceMap;
 import org.jdesktop.application.SingleFrameApplication;
 import org.jdesktop.application.FrameView;
 import org.jdesktop.application.TaskMonitor;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import javax.swing.Timer;
 import javax.swing.Icon;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 
 /**
  * The application's main frame.
  */
 public class ComplicaView extends FrameView {
 
      int numMoves = 0;
      boolean winner = false;
      Board gameBoard= new Board();
      Player p1 = new Player("Player 1");
      Player p2 = new Player("Player 2");
 
      
     public ComplicaView(SingleFrameApplication app) {
         super(app);
 
         initComponents();
 
         // status bar initialization - message timeout, idle icon and busy animation, etc
         ResourceMap resourceMap = getResourceMap();
         int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
         messageTimer = new Timer(messageTimeout, new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 statusMessageLabel.setText("");
             }
         });
         messageTimer.setRepeats(false);
         int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
         for (int i = 0; i < busyIcons.length; i++) {
             busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
         }
         busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                 statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
             }
         });
         idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
         statusAnimationLabel.setIcon(idleIcon);
         progressBar.setVisible(false);
 
         // connecting action tasks to status bar via TaskMonitor
         TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
         taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
             public void propertyChange(java.beans.PropertyChangeEvent evt) {
                 String propertyName = evt.getPropertyName();
                 if ("started".equals(propertyName)) {
                     if (!busyIconTimer.isRunning()) {
                         statusAnimationLabel.setIcon(busyIcons[0]);
                         busyIconIndex = 0;
                         busyIconTimer.start();
                     }
                     progressBar.setVisible(true);
                     progressBar.setIndeterminate(true);
                 } else if ("done".equals(propertyName)) {
                     busyIconTimer.stop();
                     statusAnimationLabel.setIcon(idleIcon);
                     progressBar.setVisible(false);
                     progressBar.setValue(0);
                 } else if ("message".equals(propertyName)) {
                     String text = (String)(evt.getNewValue());
                     statusMessageLabel.setText((text == null) ? "" : text);
                     messageTimer.restart();
                 } else if ("progress".equals(propertyName)) {
                     int value = (Integer)(evt.getNewValue());
                     progressBar.setVisible(true);
                     progressBar.setIndeterminate(false);
                     progressBar.setValue(value);
                 }
             }
         });
         if(numMoves % 2 == 0){
             lblPlayerTurn.setText(p1.getName() + "'s Move");
             lblPlayerTurn.setForeground(Color.RED);
         }
         else {
             lblPlayerTurn.setText(p2.getName() + "'s Move");
             lblPlayerTurn.setForeground(Color.BLUE);
         }
     }
 
     public void updateBoard(int x, int y, int player){
         Color color;
         if(winner == false){
             if(player == 1){
                 color = Color.RED;
                 lblPlayerTurn.setText(p2.getName() + "'s Move");
                 lblPlayerTurn.setForeground(Color.BLUE);
             }
             else{
                 color = Color.BLUE;
                 lblPlayerTurn.setText(p1.getName() + "'s Move");
                 lblPlayerTurn.setForeground(Color.RED);
             }
             switch (y){
                 case -1:
                     switch (x){
                         case 0:
                             jPanel11.setBackground(jPanel10.getBackground());
                             jPanel10.setBackground(jPanel9.getBackground());
                             jPanel9.setBackground(jPanel8.getBackground());
                             jPanel8.setBackground(jPanel7.getBackground());
                             jPanel7.setBackground(jPanel6.getBackground());
                             jPanel6.setBackground(jPanel5.getBackground());
                             jPanel5.setBackground(color);
                             break;
                         case 1:
                             jPanel13.setBackground(jPanel18.getBackground());
                             jPanel18.setBackground(jPanel17.getBackground());
                             jPanel17.setBackground(jPanel16.getBackground());
                             jPanel16.setBackground(jPanel15.getBackground());
                             jPanel15.setBackground(jPanel14.getBackground());
                             jPanel14.setBackground(jPanel12.getBackground());
                             jPanel12.setBackground(color);
                             break;
                         case 2:
                             jPanel25.setBackground(jPanel24.getBackground());
                             jPanel24.setBackground(jPanel23.getBackground());
                             jPanel23.setBackground(jPanel22.getBackground());
                             jPanel22.setBackground(jPanel21.getBackground());
                             jPanel21.setBackground(jPanel20.getBackground());
                             jPanel20.setBackground(jPanel19.getBackground());
                             jPanel19.setBackground(color);
                             break;
                         case 3:
                             jPanel32.setBackground(jPanel31.getBackground());
                             jPanel31.setBackground(jPanel30.getBackground());
                             jPanel30.setBackground(jPanel29.getBackground());
                             jPanel29.setBackground(jPanel28.getBackground());
                             jPanel28.setBackground(jPanel27.getBackground());
                             jPanel27.setBackground(jPanel26.getBackground());
                             jPanel26.setBackground(color);
                             break;
                     } break;
                 case 0:
                     switch (x){
                         case 0: jPanel5.setBackground(color); break;
                         case 1: jPanel12.setBackground(color); break;
                         case 2: jPanel19.setBackground(color); break;
                         case 3: jPanel26.setBackground(color); break;
                     } break;
                 case 1:
                     switch (x){
                         case 0: jPanel6.setBackground(color); break;
                         case 1: jPanel14.setBackground(color); break;
                         case 2: jPanel20.setBackground(color); break;
                         case 3: jPanel27.setBackground(color); break;
                     } break;
                 case 2:
                     switch (x){
                         case 0: jPanel7.setBackground(color); break;
                         case 1: jPanel15.setBackground(color); break;
                         case 2: jPanel21.setBackground(color); break;
                         case 3: jPanel28.setBackground(color); break;
                     } break;
                 case 3:
                     switch (x){
                         case 0: jPanel8.setBackground(color); break;
                         case 1: jPanel16.setBackground(color); break;
                         case 2: jPanel22.setBackground(color); break;
                         case 3: jPanel29.setBackground(color); break;
                     } break;
                 case 4:
                     switch (x){
                         case 0: jPanel9.setBackground(color); break;
                         case 1: jPanel17.setBackground(color); break;
                         case 2: jPanel23.setBackground(color); break;
                         case 3: jPanel30.setBackground(color); break;
                     } break;
                 case 5:
                     switch (x){
                         case 0: jPanel10.setBackground(color); break;
                         case 1: jPanel18.setBackground(color); break;
                         case 2: jPanel24.setBackground(color); break;
                         case 3: jPanel31.setBackground(color); break;
                     } break;
                 case 6:
                     switch (x){
                         case 0: jPanel11.setBackground(color); break;
                         case 1: jPanel13.setBackground(color); break;
                         case 2: jPanel25.setBackground(color); break;
                         case 3: jPanel32.setBackground(color); break;
                     } break;
             }
         }
 
     }
 
     public void resetBoard(){
         Color color = new Color(240, 240, 240);
         jPanel5.setBackground(color);
         jPanel6.setBackground(color);
         jPanel7.setBackground(color);
         jPanel8.setBackground(color);
         jPanel9.setBackground(color);
         jPanel10.setBackground(color);
         jPanel11.setBackground(color);
         jPanel12.setBackground(color);
         jPanel13.setBackground(color);
         jPanel14.setBackground(color);
         jPanel15.setBackground(color);
         jPanel16.setBackground(color);
         jPanel17.setBackground(color);
         jPanel18.setBackground(color);
         jPanel19.setBackground(color);
         jPanel20.setBackground(color);
         jPanel21.setBackground(color);
         jPanel22.setBackground(color);
         jPanel23.setBackground(color);
         jPanel24.setBackground(color);
         jPanel25.setBackground(color);
         jPanel26.setBackground(color);
         jPanel27.setBackground(color);
         jPanel28.setBackground(color);
         jPanel29.setBackground(color);
         jPanel30.setBackground(color);
         jPanel31.setBackground(color);
         jPanel32.setBackground(color);
         gameBoard.resetBoardArray();
         winner = false;
        numMoves = 0;
         if(p1.getLosses() > p1.getWins()){
             lblPlayerTurn.setText(p1.getName() + "'s Move");
             lblPlayerTurn.setForeground(Color.RED);
         }
         else{
             lblPlayerTurn.setText(p2.getName() + "'s Move");
             lblPlayerTurn.setForeground(Color.BLUE);
         }
     }
 
     @Action
     public void showAboutBox() {
         if (aboutBox == null) {
             JFrame mainFrame = ComplicaApp.getApplication().getMainFrame();
             aboutBox = new ComplicaAboutBox(mainFrame);
             aboutBox.setLocationRelativeTo(mainFrame);
         }
         ComplicaApp.getApplication().show(aboutBox);
     }
 
     /** This method is called from within the constructor to
      * initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is
      * always regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         mainPanel = new javax.swing.JPanel();
         jPanel1 = new javax.swing.JPanel();
         jPanel5 = new javax.swing.JPanel();
         jPanel6 = new javax.swing.JPanel();
         jPanel7 = new javax.swing.JPanel();
         jPanel8 = new javax.swing.JPanel();
         jPanel9 = new javax.swing.JPanel();
         jPanel10 = new javax.swing.JPanel();
         jPanel11 = new javax.swing.JPanel();
         jPanel2 = new javax.swing.JPanel();
         jPanel17 = new javax.swing.JPanel();
         jPanel18 = new javax.swing.JPanel();
         jPanel12 = new javax.swing.JPanel();
         jPanel14 = new javax.swing.JPanel();
         jPanel13 = new javax.swing.JPanel();
         jPanel16 = new javax.swing.JPanel();
         jPanel15 = new javax.swing.JPanel();
         jPanel3 = new javax.swing.JPanel();
         jPanel19 = new javax.swing.JPanel();
         jPanel20 = new javax.swing.JPanel();
         jPanel21 = new javax.swing.JPanel();
         jPanel22 = new javax.swing.JPanel();
         jPanel23 = new javax.swing.JPanel();
         jPanel24 = new javax.swing.JPanel();
         jPanel25 = new javax.swing.JPanel();
         jPanel4 = new javax.swing.JPanel();
         jPanel30 = new javax.swing.JPanel();
         jPanel31 = new javax.swing.JPanel();
         jPanel28 = new javax.swing.JPanel();
         jPanel32 = new javax.swing.JPanel();
         jPanel26 = new javax.swing.JPanel();
         jPanel29 = new javax.swing.JPanel();
         jPanel27 = new javax.swing.JPanel();
         lblPlayerTurn = new javax.swing.JLabel();
         jPanel33 = new javax.swing.JPanel();
         jTextField1 = new javax.swing.JTextField();
         jLabel1 = new javax.swing.JLabel();
         lblP1W = new javax.swing.JLabel();
         jLabel2 = new javax.swing.JLabel();
         lblP1L = new javax.swing.JLabel();
         jPanel34 = new javax.swing.JPanel();
         jTextField2 = new javax.swing.JTextField();
         jLabel3 = new javax.swing.JLabel();
         lblP1W1 = new javax.swing.JLabel();
         lblP1L1 = new javax.swing.JLabel();
         jLabel4 = new javax.swing.JLabel();
         jButton1 = new javax.swing.JButton();
         menuBar = new javax.swing.JMenuBar();
         javax.swing.JMenu fileMenu = new javax.swing.JMenu();
         javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
         javax.swing.JMenu helpMenu = new javax.swing.JMenu();
         javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
         statusPanel = new javax.swing.JPanel();
         javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
         statusMessageLabel = new javax.swing.JLabel();
         statusAnimationLabel = new javax.swing.JLabel();
         progressBar = new javax.swing.JProgressBar();
 
         mainPanel.setName("mainPanel"); // NOI18N
 
         org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(complica.ComplicaApp.class).getContext().getResourceMap(ComplicaView.class);
         jPanel1.setBackground(resourceMap.getColor("jPanel1.background")); // NOI18N
         jPanel1.setName("jPanel1"); // NOI18N
         jPanel1.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jPanel1MouseClicked(evt);
             }
         });
 
         jPanel5.setName("jPanel5"); // NOI18N
 
         org.jdesktop.layout.GroupLayout jPanel5Layout = new org.jdesktop.layout.GroupLayout(jPanel5);
         jPanel5.setLayout(jPanel5Layout);
         jPanel5Layout.setHorizontalGroup(
             jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 80, Short.MAX_VALUE)
         );
         jPanel5Layout.setVerticalGroup(
             jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 78, Short.MAX_VALUE)
         );
 
         jPanel6.setName("jPanel6"); // NOI18N
 
         org.jdesktop.layout.GroupLayout jPanel6Layout = new org.jdesktop.layout.GroupLayout(jPanel6);
         jPanel6.setLayout(jPanel6Layout);
         jPanel6Layout.setHorizontalGroup(
             jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 80, Short.MAX_VALUE)
         );
         jPanel6Layout.setVerticalGroup(
             jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 78, Short.MAX_VALUE)
         );
 
         jPanel7.setName("jPanel7"); // NOI18N
 
         jPanel8.setName("jPanel8"); // NOI18N
 
         org.jdesktop.layout.GroupLayout jPanel8Layout = new org.jdesktop.layout.GroupLayout(jPanel8);
         jPanel8.setLayout(jPanel8Layout);
         jPanel8Layout.setHorizontalGroup(
             jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 80, Short.MAX_VALUE)
         );
         jPanel8Layout.setVerticalGroup(
             jPanel8Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 78, Short.MAX_VALUE)
         );
 
         jPanel9.setName("jPanel9"); // NOI18N
 
         org.jdesktop.layout.GroupLayout jPanel9Layout = new org.jdesktop.layout.GroupLayout(jPanel9);
         jPanel9.setLayout(jPanel9Layout);
         jPanel9Layout.setHorizontalGroup(
             jPanel9Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 80, Short.MAX_VALUE)
         );
         jPanel9Layout.setVerticalGroup(
             jPanel9Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 78, Short.MAX_VALUE)
         );
 
         jPanel10.setName("jPanel10"); // NOI18N
 
         org.jdesktop.layout.GroupLayout jPanel10Layout = new org.jdesktop.layout.GroupLayout(jPanel10);
         jPanel10.setLayout(jPanel10Layout);
         jPanel10Layout.setHorizontalGroup(
             jPanel10Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 80, Short.MAX_VALUE)
         );
         jPanel10Layout.setVerticalGroup(
             jPanel10Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 78, Short.MAX_VALUE)
         );
 
         jPanel11.setName("jPanel11"); // NOI18N
 
         org.jdesktop.layout.GroupLayout jPanel11Layout = new org.jdesktop.layout.GroupLayout(jPanel11);
         jPanel11.setLayout(jPanel11Layout);
         jPanel11Layout.setHorizontalGroup(
             jPanel11Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 88, Short.MAX_VALUE)
         );
         jPanel11Layout.setVerticalGroup(
             jPanel11Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 78, Short.MAX_VALUE)
         );
 
         org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
         jPanel1.setLayout(jPanel1Layout);
         jPanel1Layout.setHorizontalGroup(
             jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel1Layout.createSequentialGroup()
                 .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(jPanel1Layout.createSequentialGroup()
                         .add(20, 20, 20)
                         .add(jPanel5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                     .add(jPanel1Layout.createSequentialGroup()
                         .addContainerGap()
                         .add(jPanel8, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                     .add(jPanel1Layout.createSequentialGroup()
                         .addContainerGap()
                         .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                             .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel7, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                     .add(jPanel1Layout.createSequentialGroup()
                         .addContainerGap()
                         .add(jPanel9, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                     .add(jPanel1Layout.createSequentialGroup()
                         .addContainerGap()
                         .add(jPanel10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                     .add(jPanel1Layout.createSequentialGroup()
                         .addContainerGap()
                         .add(jPanel11, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                 .addContainerGap())
         );
         jPanel1Layout.setVerticalGroup(
             jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel1Layout.createSequentialGroup()
                 .addContainerGap()
                 .add(jPanel5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                 .add(jPanel6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .add(14, 14, 14)
                 .add(jPanel7, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 76, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .add(18, 18, 18)
                 .add(jPanel8, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                 .add(jPanel9, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                 .add(jPanel10, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .add(11, 11, 11)
                 .add(jPanel11, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .addContainerGap())
         );
 
         jPanel2.setBackground(resourceMap.getColor("jPanel1.background")); // NOI18N
         jPanel2.setName("jPanel2"); // NOI18N
         jPanel2.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jPanel2MouseClicked(evt);
             }
         });
 
         jPanel17.setName("jPanel17"); // NOI18N
 
         org.jdesktop.layout.GroupLayout jPanel17Layout = new org.jdesktop.layout.GroupLayout(jPanel17);
         jPanel17.setLayout(jPanel17Layout);
         jPanel17Layout.setHorizontalGroup(
             jPanel17Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 80, Short.MAX_VALUE)
         );
         jPanel17Layout.setVerticalGroup(
             jPanel17Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 78, Short.MAX_VALUE)
         );
 
         jPanel18.setName("jPanel18"); // NOI18N
 
         org.jdesktop.layout.GroupLayout jPanel18Layout = new org.jdesktop.layout.GroupLayout(jPanel18);
         jPanel18.setLayout(jPanel18Layout);
         jPanel18Layout.setHorizontalGroup(
             jPanel18Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 80, Short.MAX_VALUE)
         );
         jPanel18Layout.setVerticalGroup(
             jPanel18Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 78, Short.MAX_VALUE)
         );
 
         jPanel12.setName("jPanel12"); // NOI18N
 
         org.jdesktop.layout.GroupLayout jPanel12Layout = new org.jdesktop.layout.GroupLayout(jPanel12);
         jPanel12.setLayout(jPanel12Layout);
         jPanel12Layout.setHorizontalGroup(
             jPanel12Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 80, Short.MAX_VALUE)
         );
         jPanel12Layout.setVerticalGroup(
             jPanel12Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 78, Short.MAX_VALUE)
         );
 
         jPanel14.setName("jPanel14"); // NOI18N
 
         org.jdesktop.layout.GroupLayout jPanel14Layout = new org.jdesktop.layout.GroupLayout(jPanel14);
         jPanel14.setLayout(jPanel14Layout);
         jPanel14Layout.setHorizontalGroup(
             jPanel14Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 80, Short.MAX_VALUE)
         );
         jPanel14Layout.setVerticalGroup(
             jPanel14Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 78, Short.MAX_VALUE)
         );
 
         jPanel13.setName("jPanel13"); // NOI18N
 
         org.jdesktop.layout.GroupLayout jPanel13Layout = new org.jdesktop.layout.GroupLayout(jPanel13);
         jPanel13.setLayout(jPanel13Layout);
         jPanel13Layout.setHorizontalGroup(
             jPanel13Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 80, Short.MAX_VALUE)
         );
         jPanel13Layout.setVerticalGroup(
             jPanel13Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 75, Short.MAX_VALUE)
         );
 
         jPanel16.setName("jPanel16"); // NOI18N
 
         org.jdesktop.layout.GroupLayout jPanel16Layout = new org.jdesktop.layout.GroupLayout(jPanel16);
         jPanel16.setLayout(jPanel16Layout);
         jPanel16Layout.setHorizontalGroup(
             jPanel16Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 80, Short.MAX_VALUE)
         );
         jPanel16Layout.setVerticalGroup(
             jPanel16Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 78, Short.MAX_VALUE)
         );
 
         jPanel15.setName("jPanel15"); // NOI18N
 
         org.jdesktop.layout.GroupLayout jPanel15Layout = new org.jdesktop.layout.GroupLayout(jPanel15);
         jPanel15.setLayout(jPanel15Layout);
         jPanel15Layout.setHorizontalGroup(
             jPanel15Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 80, Short.MAX_VALUE)
         );
         jPanel15Layout.setVerticalGroup(
             jPanel15Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 78, Short.MAX_VALUE)
         );
 
         org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
         jPanel2.setLayout(jPanel2Layout);
         jPanel2Layout.setHorizontalGroup(
             jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel2Layout.createSequentialGroup()
                 .addContainerGap()
                 .add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                     .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel13, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel12, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel14, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel15, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel16, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel17, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                     .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel18, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                 .addContainerGap())
         );
         jPanel2Layout.setVerticalGroup(
             jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel2Layout.createSequentialGroup()
                 .add(22, 22, 22)
                 .add(jPanel12, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                 .add(jPanel14, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                 .add(jPanel15, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                 .add(jPanel16, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                 .add(jPanel17, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                 .add(jPanel18, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                 .add(jPanel13, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(26, Short.MAX_VALUE))
         );
 
         jPanel3.setBackground(resourceMap.getColor("jPanel1.background")); // NOI18N
         jPanel3.setName("jPanel3"); // NOI18N
         jPanel3.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jPanel3MouseClicked(evt);
             }
         });
 
         jPanel19.setName("jPanel19"); // NOI18N
 
         org.jdesktop.layout.GroupLayout jPanel19Layout = new org.jdesktop.layout.GroupLayout(jPanel19);
         jPanel19.setLayout(jPanel19Layout);
         jPanel19Layout.setHorizontalGroup(
             jPanel19Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 80, Short.MAX_VALUE)
         );
         jPanel19Layout.setVerticalGroup(
             jPanel19Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 78, Short.MAX_VALUE)
         );
 
         jPanel20.setName("jPanel20"); // NOI18N
 
         org.jdesktop.layout.GroupLayout jPanel20Layout = new org.jdesktop.layout.GroupLayout(jPanel20);
         jPanel20.setLayout(jPanel20Layout);
         jPanel20Layout.setHorizontalGroup(
             jPanel20Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 80, Short.MAX_VALUE)
         );
         jPanel20Layout.setVerticalGroup(
             jPanel20Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 78, Short.MAX_VALUE)
         );
 
         jPanel21.setName("jPanel21"); // NOI18N
 
         org.jdesktop.layout.GroupLayout jPanel21Layout = new org.jdesktop.layout.GroupLayout(jPanel21);
         jPanel21.setLayout(jPanel21Layout);
         jPanel21Layout.setHorizontalGroup(
             jPanel21Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 80, Short.MAX_VALUE)
         );
         jPanel21Layout.setVerticalGroup(
             jPanel21Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 78, Short.MAX_VALUE)
         );
 
         jPanel22.setName("jPanel22"); // NOI18N
 
         org.jdesktop.layout.GroupLayout jPanel22Layout = new org.jdesktop.layout.GroupLayout(jPanel22);
         jPanel22.setLayout(jPanel22Layout);
         jPanel22Layout.setHorizontalGroup(
             jPanel22Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 80, Short.MAX_VALUE)
         );
         jPanel22Layout.setVerticalGroup(
             jPanel22Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 78, Short.MAX_VALUE)
         );
 
         jPanel23.setName("jPanel23"); // NOI18N
 
         org.jdesktop.layout.GroupLayout jPanel23Layout = new org.jdesktop.layout.GroupLayout(jPanel23);
         jPanel23.setLayout(jPanel23Layout);
         jPanel23Layout.setHorizontalGroup(
             jPanel23Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 80, Short.MAX_VALUE)
         );
         jPanel23Layout.setVerticalGroup(
             jPanel23Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 78, Short.MAX_VALUE)
         );
 
         jPanel24.setName("jPanel24"); // NOI18N
 
         org.jdesktop.layout.GroupLayout jPanel24Layout = new org.jdesktop.layout.GroupLayout(jPanel24);
         jPanel24.setLayout(jPanel24Layout);
         jPanel24Layout.setHorizontalGroup(
             jPanel24Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 80, Short.MAX_VALUE)
         );
         jPanel24Layout.setVerticalGroup(
             jPanel24Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 78, Short.MAX_VALUE)
         );
 
         jPanel25.setName("jPanel25"); // NOI18N
 
         org.jdesktop.layout.GroupLayout jPanel25Layout = new org.jdesktop.layout.GroupLayout(jPanel25);
         jPanel25.setLayout(jPanel25Layout);
         jPanel25Layout.setHorizontalGroup(
             jPanel25Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 80, Short.MAX_VALUE)
         );
         jPanel25Layout.setVerticalGroup(
             jPanel25Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 78, Short.MAX_VALUE)
         );
 
         org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
         jPanel3.setLayout(jPanel3Layout);
         jPanel3Layout.setHorizontalGroup(
             jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel3Layout.createSequentialGroup()
                 .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .add(jPanel19, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
             .add(jPanel3Layout.createSequentialGroup()
                 .addContainerGap()
                 .add(jPanel20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
             .add(jPanel3Layout.createSequentialGroup()
                 .addContainerGap()
                 .add(jPanel21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
             .add(jPanel3Layout.createSequentialGroup()
                 .addContainerGap()
                 .add(jPanel22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
             .add(jPanel3Layout.createSequentialGroup()
                 .addContainerGap()
                 .add(jPanel23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
             .add(jPanel3Layout.createSequentialGroup()
                 .addContainerGap()
                 .add(jPanel24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
             .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel3Layout.createSequentialGroup()
                 .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .add(jPanel25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap())
         );
         jPanel3Layout.setVerticalGroup(
             jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel3Layout.createSequentialGroup()
                 .add(23, 23, 23)
                 .add(jPanel19, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                 .add(jPanel20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                 .add(jPanel21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                 .add(jPanel22, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                 .add(jPanel23, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                 .add(jPanel24, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                 .add(jPanel25, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(22, Short.MAX_VALUE))
         );
 
         jPanel4.setBackground(resourceMap.getColor("jPanel1.background")); // NOI18N
         jPanel4.setName("jPanel4"); // NOI18N
         jPanel4.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jPanel4MouseClicked(evt);
             }
         });
 
         jPanel30.setName("jPanel30"); // NOI18N
 
         org.jdesktop.layout.GroupLayout jPanel30Layout = new org.jdesktop.layout.GroupLayout(jPanel30);
         jPanel30.setLayout(jPanel30Layout);
         jPanel30Layout.setHorizontalGroup(
             jPanel30Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 80, Short.MAX_VALUE)
         );
         jPanel30Layout.setVerticalGroup(
             jPanel30Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 78, Short.MAX_VALUE)
         );
 
         jPanel31.setName("jPanel31"); // NOI18N
 
         org.jdesktop.layout.GroupLayout jPanel31Layout = new org.jdesktop.layout.GroupLayout(jPanel31);
         jPanel31.setLayout(jPanel31Layout);
         jPanel31Layout.setHorizontalGroup(
             jPanel31Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 80, Short.MAX_VALUE)
         );
         jPanel31Layout.setVerticalGroup(
             jPanel31Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 78, Short.MAX_VALUE)
         );
 
         jPanel28.setName("jPanel28"); // NOI18N
 
         org.jdesktop.layout.GroupLayout jPanel28Layout = new org.jdesktop.layout.GroupLayout(jPanel28);
         jPanel28.setLayout(jPanel28Layout);
         jPanel28Layout.setHorizontalGroup(
             jPanel28Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 80, Short.MAX_VALUE)
         );
         jPanel28Layout.setVerticalGroup(
             jPanel28Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 78, Short.MAX_VALUE)
         );
 
         jPanel32.setName("jPanel32"); // NOI18N
 
         org.jdesktop.layout.GroupLayout jPanel32Layout = new org.jdesktop.layout.GroupLayout(jPanel32);
         jPanel32.setLayout(jPanel32Layout);
         jPanel32Layout.setHorizontalGroup(
             jPanel32Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 80, Short.MAX_VALUE)
         );
         jPanel32Layout.setVerticalGroup(
             jPanel32Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 78, Short.MAX_VALUE)
         );
 
         jPanel26.setName("jPanel26"); // NOI18N
 
         org.jdesktop.layout.GroupLayout jPanel26Layout = new org.jdesktop.layout.GroupLayout(jPanel26);
         jPanel26.setLayout(jPanel26Layout);
         jPanel26Layout.setHorizontalGroup(
             jPanel26Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 80, Short.MAX_VALUE)
         );
         jPanel26Layout.setVerticalGroup(
             jPanel26Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 78, Short.MAX_VALUE)
         );
 
         jPanel29.setName("jPanel29"); // NOI18N
 
         org.jdesktop.layout.GroupLayout jPanel29Layout = new org.jdesktop.layout.GroupLayout(jPanel29);
         jPanel29.setLayout(jPanel29Layout);
         jPanel29Layout.setHorizontalGroup(
             jPanel29Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 80, Short.MAX_VALUE)
         );
         jPanel29Layout.setVerticalGroup(
             jPanel29Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 78, Short.MAX_VALUE)
         );
 
         jPanel27.setName("jPanel27"); // NOI18N
 
         org.jdesktop.layout.GroupLayout jPanel27Layout = new org.jdesktop.layout.GroupLayout(jPanel27);
         jPanel27.setLayout(jPanel27Layout);
         jPanel27Layout.setHorizontalGroup(
             jPanel27Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 80, Short.MAX_VALUE)
         );
         jPanel27Layout.setVerticalGroup(
             jPanel27Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(0, 78, Short.MAX_VALUE)
         );
 
         org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
         jPanel4.setLayout(jPanel4Layout);
         jPanel4Layout.setHorizontalGroup(
             jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel4Layout.createSequentialGroup()
                 .addContainerGap()
                 .add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(jPanel26, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(jPanel27, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(jPanel28, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(jPanel29, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(jPanel30, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(jPanel31, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                     .add(jPanel32, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
         jPanel4Layout.setVerticalGroup(
             jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel4Layout.createSequentialGroup()
                 .addContainerGap()
                 .add(jPanel26, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                 .add(jPanel27, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                 .add(jPanel28, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                 .add(jPanel29, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                 .add(jPanel30, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                 .add(jPanel31, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                 .add(jPanel32, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addContainerGap(37, Short.MAX_VALUE))
         );
 
         lblPlayerTurn.setFont(resourceMap.getFont("lblPlayerTurn.font")); // NOI18N
         lblPlayerTurn.setText(resourceMap.getString("lblPlayerTurn.text")); // NOI18N
         lblPlayerTurn.setName("lblPlayerTurn"); // NOI18N
 
         jPanel33.setBackground(resourceMap.getColor("jPanel33.background")); // NOI18N
         jPanel33.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
         jPanel33.setName("jPanel33"); // NOI18N
 
         jTextField1.setText(resourceMap.getString("jTextField1.text")); // NOI18N
         jTextField1.setName("jTextField1"); // NOI18N
         jTextField1.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusLost(java.awt.event.FocusEvent evt) {
                 jTextField1FocusLost(evt);
             }
         });
         jTextField1.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 jTextField1KeyReleased(evt);
             }
         });
 
         jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
         jLabel1.setName("jLabel1"); // NOI18N
 
         lblP1W.setText(resourceMap.getString("lblP1W.text")); // NOI18N
         lblP1W.setName("lblP1W"); // NOI18N
 
         jLabel2.setText(resourceMap.getString("jLabel2.text")); // NOI18N
         jLabel2.setName("jLabel2"); // NOI18N
 
         lblP1L.setText(resourceMap.getString("lblP1L.text")); // NOI18N
         lblP1L.setName("lblP1L"); // NOI18N
 
         org.jdesktop.layout.GroupLayout jPanel33Layout = new org.jdesktop.layout.GroupLayout(jPanel33);
         jPanel33.setLayout(jPanel33Layout);
         jPanel33Layout.setHorizontalGroup(
             jPanel33Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jTextField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 108, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
             .add(jPanel33Layout.createSequentialGroup()
                 .addContainerGap()
                 .add(jLabel1)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(lblP1W)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jLabel2)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(lblP1L))
         );
         jPanel33Layout.setVerticalGroup(
             jPanel33Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel33Layout.createSequentialGroup()
                 .add(jTextField1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jPanel33Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(jLabel1)
                     .add(lblP1W)
                     .add(jLabel2)
                     .add(lblP1L))
                 .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         jPanel34.setBackground(resourceMap.getColor("jPanel34.background")); // NOI18N
         jPanel34.setBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
         jPanel34.setName("jPanel34"); // NOI18N
 
         jTextField2.setText(resourceMap.getString("jTextField2.text")); // NOI18N
         jTextField2.setName("jTextField2"); // NOI18N
         jTextField2.addFocusListener(new java.awt.event.FocusAdapter() {
             public void focusLost(java.awt.event.FocusEvent evt) {
                 jTextField2FocusLost(evt);
             }
         });
         jTextField2.addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 jTextField2KeyReleased(evt);
             }
         });
 
         jLabel3.setText(resourceMap.getString("jLabel3.text")); // NOI18N
         jLabel3.setName("jLabel3"); // NOI18N
 
         lblP1W1.setText(resourceMap.getString("lblP1W1.text")); // NOI18N
         lblP1W1.setName("lblP1W1"); // NOI18N
 
         lblP1L1.setText(resourceMap.getString("lblP1L1.text")); // NOI18N
         lblP1L1.setName("lblP1L1"); // NOI18N
 
         jLabel4.setText(resourceMap.getString("jLabel4.text")); // NOI18N
         jLabel4.setName("jLabel4"); // NOI18N
 
         org.jdesktop.layout.GroupLayout jPanel34Layout = new org.jdesktop.layout.GroupLayout(jPanel34);
         jPanel34.setLayout(jPanel34Layout);
         jPanel34Layout.setHorizontalGroup(
             jPanel34Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jTextField2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 108, Short.MAX_VALUE)
             .add(jPanel34Layout.createSequentialGroup()
                 .addContainerGap()
                 .add(jLabel3)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(lblP1W1)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jLabel4)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(lblP1L1)
                 .addContainerGap(33, Short.MAX_VALUE))
         );
         jPanel34Layout.setVerticalGroup(
             jPanel34Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(jPanel34Layout.createSequentialGroup()
                 .add(jTextField2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(jPanel34Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(jLabel3)
                     .add(lblP1W1)
                     .add(jLabel4)
                     .add(lblP1L1))
                 .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         jButton1.setText(resourceMap.getString("jButton1.text")); // NOI18N
         jButton1.setName("jButton1"); // NOI18N
         jButton1.addMouseListener(new java.awt.event.MouseAdapter() {
             public void mouseClicked(java.awt.event.MouseEvent evt) {
                 jButton1MouseClicked(evt);
             }
         });
 
         org.jdesktop.layout.GroupLayout mainPanelLayout = new org.jdesktop.layout.GroupLayout(mainPanel);
         mainPanel.setLayout(mainPanelLayout);
         mainPanelLayout.setHorizontalGroup(
             mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(mainPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                 .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                 .add(jPanel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                 .add(jPanel4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(mainPanelLayout.createSequentialGroup()
                         .add(26, 26, 26)
                         .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                             .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel34, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel33, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .add(jButton1)))
                     .add(mainPanelLayout.createSequentialGroup()
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                         .add(lblPlayerTurn, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 138, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                 .add(92, 92, 92))
         );
         mainPanelLayout.setVerticalGroup(
             mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(mainPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                     .add(mainPanelLayout.createSequentialGroup()
                         .add(lblPlayerTurn, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 45, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                         .add(jPanel33, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                         .add(jPanel34, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                         .add(52, 52, 52)
                         .add(jButton1)
                         .addContainerGap())
                     .add(mainPanelLayout.createSequentialGroup()
                         .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                             .add(jPanel4, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                             .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                                 .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel3, 0, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                 .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                 .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                         .add(129, 129, 129))))
         );
 
         menuBar.setName("menuBar"); // NOI18N
 
         fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
         fileMenu.setName("fileMenu"); // NOI18N
 
         javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(complica.ComplicaApp.class).getContext().getActionMap(ComplicaView.class, this);
         exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
         exitMenuItem.setName("exitMenuItem"); // NOI18N
         fileMenu.add(exitMenuItem);
 
         menuBar.add(fileMenu);
 
         helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
         helpMenu.setName("helpMenu"); // NOI18N
 
         aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
         aboutMenuItem.setName("aboutMenuItem"); // NOI18N
         helpMenu.add(aboutMenuItem);
 
         menuBar.add(helpMenu);
 
         statusPanel.setName("statusPanel"); // NOI18N
 
         statusMessageLabel.setName("statusMessageLabel"); // NOI18N
 
         statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
         statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N
 
         progressBar.setName("progressBar"); // NOI18N
 
         org.jdesktop.layout.GroupLayout statusPanelLayout = new org.jdesktop.layout.GroupLayout(statusPanel);
         statusPanel.setLayout(statusPanelLayout);
         statusPanelLayout.setHorizontalGroup(
             statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(statusPanelLayout.createSequentialGroup()
                 .addContainerGap()
                 .add(statusMessageLabel)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 690, Short.MAX_VALUE)
                 .add(statusAnimationLabel)
                 .addContainerGap())
             .add(org.jdesktop.layout.GroupLayout.TRAILING, statusPanelSeparator, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 714, Short.MAX_VALUE)
             .add(org.jdesktop.layout.GroupLayout.TRAILING, statusPanelLayout.createSequentialGroup()
                 .addContainerGap(560, Short.MAX_VALUE)
                 .add(progressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .add(8, 8, 8))
         );
         statusPanelLayout.setVerticalGroup(
             statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
             .add(statusPanelLayout.createSequentialGroup()
                 .add(statusPanelSeparator, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                 .add(progressBar, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                 .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                 .add(statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                     .add(statusMessageLabel)
                     .add(statusAnimationLabel))
                 .add(3, 3, 3))
         );
 
         setComponent(mainPanel);
         setMenuBar(menuBar);
         setStatusBar(statusPanel);
     }// </editor-fold>//GEN-END:initComponents
 
     private void jPanel1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel1MouseClicked
         if(numMoves % 2 == 0){
             updateBoard(0, gameBoard.addPiece(1, 0), 1);
         }
         else {
             updateBoard(0, gameBoard.addPiece(2,0), 2);
         }
         if(gameBoard.checkWinner()==1 && winner == false){
             p1.win();
             p2.lose();
             lblP1W.setText(Integer.toString(p1.getWins()));
             lblP1L.setText(Integer.toString(p1.getLosses()));
             lblP1W1.setText(Integer.toString(p2.getWins()));
             lblP1L1.setText(Integer.toString(p2.getLosses()));
             System.out.println("Player one wins!");
             lblPlayerTurn.setText(p1.getName() + " Wins!");
             lblPlayerTurn.setForeground(Color.RED);
             winner  = true;
         }
         else if(gameBoard.checkWinner()==2 && winner == false){
             p2.win();
             p1.lose();
             lblP1W.setText(Integer.toString(p1.getWins()));
             lblP1L.setText(Integer.toString(p1.getLosses()));
             lblP1W1.setText(Integer.toString(p2.getWins()));
             lblP1L1.setText(Integer.toString(p2.getLosses()));
             System.out.println("Player two wins!");
             lblPlayerTurn.setText(p2.getName() + " Wins!");
             lblPlayerTurn.setForeground(Color.BLUE);
             winner  = true;
         }
         numMoves++;
     }//GEN-LAST:event_jPanel1MouseClicked
 
     private void jPanel2MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel2MouseClicked
         if(numMoves % 2 == 0){
             updateBoard(1, gameBoard.addPiece(1, 1), 1);
         }
         else {
             updateBoard(1, gameBoard.addPiece(2,1), 2);
         }
         if(gameBoard.checkWinner()==1 && winner == false){
             p1.win();
             p2.lose();
             lblP1W.setText(Integer.toString(p1.getWins()));
             lblP1L.setText(Integer.toString(p1.getLosses()));
             lblP1W1.setText(Integer.toString(p2.getWins()));
             lblP1L1.setText(Integer.toString(p2.getLosses()));
             System.out.println("Player one wins!");
             lblPlayerTurn.setText(p1.getName() + " Wins!");
             lblPlayerTurn.setForeground(Color.RED);
             winner  = true;
         }
         else if(gameBoard.checkWinner()==2 && winner == false){
             p2.win();
             p1.lose();
             lblP1W.setText(Integer.toString(p1.getWins()));
             lblP1L.setText(Integer.toString(p1.getLosses()));
             lblP1W1.setText(Integer.toString(p2.getWins()));
             lblP1L1.setText(Integer.toString(p2.getLosses()));
             System.out.println("Player two wins!");
             lblPlayerTurn.setText(p2.getName() + " Wins!");
             lblPlayerTurn.setForeground(Color.BLUE);
             winner  = true;
         }
             numMoves++;
     }//GEN-LAST:event_jPanel2MouseClicked
 
     private void jPanel3MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel3MouseClicked
         if(numMoves % 2 == 0){
             updateBoard(2, gameBoard.addPiece(1, 2), 1);
         }
         else {
             updateBoard(2, gameBoard.addPiece(2,2), 2);
         }
         if(gameBoard.checkWinner()==1 && winner == false){
             p1.win();
             p2.lose();
             lblP1W.setText(Integer.toString(p1.getWins()));
             lblP1L.setText(Integer.toString(p1.getLosses()));
             lblP1W1.setText(Integer.toString(p2.getWins()));
             lblP1L1.setText(Integer.toString(p2.getLosses()));
             System.out.println("Player one wins!");
             lblPlayerTurn.setText(p1.getName() + " Wins!");
             lblPlayerTurn.setForeground(Color.RED);
             winner  = true;
         }
         else if(gameBoard.checkWinner()==2 && winner == false){
             p2.win();
             p1.lose();
             lblP1W.setText(Integer.toString(p1.getWins()));
             lblP1L.setText(Integer.toString(p1.getLosses()));
             lblP1W1.setText(Integer.toString(p2.getWins()));
             lblP1L1.setText(Integer.toString(p2.getLosses()));
             System.out.println("Player two wins!");
             lblPlayerTurn.setText(p2.getName() + " Wins!");
             lblPlayerTurn.setForeground(Color.BLUE);
             winner  = true;
        }
             numMoves++;
     }//GEN-LAST:event_jPanel3MouseClicked
 
     private void jPanel4MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jPanel4MouseClicked
         if(numMoves % 2 == 0){
             updateBoard(3, gameBoard.addPiece(1, 3), 1);
         }
         else {
             updateBoard(3, gameBoard.addPiece(2,3), 2);
         }
         if(gameBoard.checkWinner()==1 && winner == false){
             p1.win();
             p2.lose();
             lblP1W.setText(Integer.toString(p1.getWins()));
             lblP1L.setText(Integer.toString(p1.getLosses()));
             lblP1W1.setText(Integer.toString(p2.getWins()));
             lblP1L1.setText(Integer.toString(p2.getLosses()));
             System.out.println("Player one wins!");
             lblPlayerTurn.setText(p1.getName() + " Wins!");
             lblPlayerTurn.setForeground(Color.RED);
             winner  = true;
         }
         else if(gameBoard.checkWinner()==2 && winner == false){
             System.out.println("Player two wins!");
             p2.win();
             p1.lose();
             lblP1W.setText(Integer.toString(p1.getWins()));
             lblP1L.setText(Integer.toString(p1.getLosses()));
             lblP1W1.setText(Integer.toString(p2.getWins()));
             lblP1L1.setText(Integer.toString(p2.getLosses()));
             lblPlayerTurn.setText(p2.getName() + " Wins!");
             lblPlayerTurn.setForeground(Color.BLUE);
             winner  = true;
        }      
             numMoves++;
     }//GEN-LAST:event_jPanel4MouseClicked
 
     private void jTextField1FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField1FocusLost
         p1.setName(jTextField1.getText());
     }//GEN-LAST:event_jTextField1FocusLost
 
     private void jTextField2FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTextField2FocusLost
         p2.setName(jTextField2.getText());
     }//GEN-LAST:event_jTextField2FocusLost
 
     private void jTextField1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField1KeyReleased
         p1.setName(jTextField1.getText());
     }//GEN-LAST:event_jTextField1KeyReleased
 
     private void jTextField2KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField2KeyReleased
         p2.setName(jTextField2.getText());
     }//GEN-LAST:event_jTextField2KeyReleased
 
     private void jButton1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButton1MouseClicked
         resetBoard();
     }//GEN-LAST:event_jButton1MouseClicked
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton jButton1;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JLabel jLabel3;
     private javax.swing.JLabel jLabel4;
     private javax.swing.JPanel jPanel1;
     private javax.swing.JPanel jPanel10;
     private javax.swing.JPanel jPanel11;
     private javax.swing.JPanel jPanel12;
     private javax.swing.JPanel jPanel13;
     private javax.swing.JPanel jPanel14;
     private javax.swing.JPanel jPanel15;
     private javax.swing.JPanel jPanel16;
     private javax.swing.JPanel jPanel17;
     private javax.swing.JPanel jPanel18;
     private javax.swing.JPanel jPanel19;
     private javax.swing.JPanel jPanel2;
     private javax.swing.JPanel jPanel20;
     private javax.swing.JPanel jPanel21;
     private javax.swing.JPanel jPanel22;
     private javax.swing.JPanel jPanel23;
     private javax.swing.JPanel jPanel24;
     private javax.swing.JPanel jPanel25;
     private javax.swing.JPanel jPanel26;
     private javax.swing.JPanel jPanel27;
     private javax.swing.JPanel jPanel28;
     private javax.swing.JPanel jPanel29;
     private javax.swing.JPanel jPanel3;
     private javax.swing.JPanel jPanel30;
     private javax.swing.JPanel jPanel31;
     private javax.swing.JPanel jPanel32;
     private javax.swing.JPanel jPanel33;
     private javax.swing.JPanel jPanel34;
     private javax.swing.JPanel jPanel4;
     private javax.swing.JPanel jPanel5;
     private javax.swing.JPanel jPanel6;
     private javax.swing.JPanel jPanel7;
     private javax.swing.JPanel jPanel8;
     private javax.swing.JPanel jPanel9;
     private javax.swing.JTextField jTextField1;
     private javax.swing.JTextField jTextField2;
     private javax.swing.JLabel lblP1L;
     private javax.swing.JLabel lblP1L1;
     private javax.swing.JLabel lblP1W;
     private javax.swing.JLabel lblP1W1;
     private javax.swing.JLabel lblPlayerTurn;
     private javax.swing.JPanel mainPanel;
     private javax.swing.JMenuBar menuBar;
     private javax.swing.JProgressBar progressBar;
     private javax.swing.JLabel statusAnimationLabel;
     private javax.swing.JLabel statusMessageLabel;
     private javax.swing.JPanel statusPanel;
     // End of variables declaration//GEN-END:variables
 
     private final Timer messageTimer;
     private final Timer busyIconTimer;
     private final Icon idleIcon;
     private final Icon[] busyIcons = new Icon[15];
     private int busyIconIndex = 0;
 
     private JDialog aboutBox;
 }
