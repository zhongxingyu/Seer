 package cz.csob.smtp.mailer.gui;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
import java.awt.Rectangle;
 
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextPane;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.border.EtchedBorder;
 import javax.swing.border.TitledBorder;
 import java.awt.Component;
 
 public class MonitorPanel extends JPanel {
 
     public MonitorPanel() {
         super();
         create();
     }
 
     /**
      * 
      */
     private static final long serialVersionUID = -2702024507197138938L;
     private JLabel lblCurrentState;
 
     public JLabel getLblCurrentState() {
         return lblCurrentState;
     }
 
     public void setLblCurrentState(JLabel lblCurrentState) {
         this.lblCurrentState = lblCurrentState;
     }
 
     public synchronized void appendToConsole(String text) {
        Rectangle rect = textPane.getVisibleRect();
         //textPaneConsole.setText(textPaneConsole.getText() + text + "\n");
        textPane.setText(textPane.getText() + "aaa" + "\n");
         //scrollPane.doLayout();
         //textPaneConsole.doLayout();
         this.repaint();
        scrollPane.scrollRectToVisible(rect);
        textPane.repaint();
        scrollPane.repaint();
     }
 
    JTextPane textPane = new JTextPane();
     JTabbedPane tabbedPaneMonitor = new JTabbedPane(JTabbedPane.TOP);
    JScrollPane scrollPane = new JScrollPane();
 
     private void create() {
 
         GridBagLayout gbl_panelMonitor = new GridBagLayout();
         gbl_panelMonitor.columnWidths = new int[] { 170, 0 };
         gbl_panelMonitor.rowHeights = new int[] { 169, 0 };
         gbl_panelMonitor.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
         gbl_panelMonitor.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
         setLayout(gbl_panelMonitor);
 
         tabbedPaneMonitor = new JTabbedPane(JTabbedPane.TOP);
         GridBagConstraints gbc_tabbedPaneMonitor = new GridBagConstraints();
         gbc_tabbedPaneMonitor.fill = GridBagConstraints.BOTH;
         gbc_tabbedPaneMonitor.gridx = 0;
         gbc_tabbedPaneMonitor.gridy = 0;
         this.add(tabbedPaneMonitor, gbc_tabbedPaneMonitor);
         tabbedPaneMonitor.setBorder(new TitledBorder(null, "Monitor",
                 TitledBorder.LEADING, TitledBorder.TOP, null, null));
 
         JPanel panelInfo = new JPanel();
         panelInfo.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
         tabbedPaneMonitor.addTab("Info", null, panelInfo, null);
         GridBagLayout gbl_panelInfo = new GridBagLayout();
         gbl_panelInfo.columnWidths = new int[] { 200, 200, 0 };
         gbl_panelInfo.rowHeights = new int[] { 14, 14, 0, 0 };
         gbl_panelInfo.columnWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
         gbl_panelInfo.rowWeights = new double[] { 0.0, 0.0, 1.0,
                 Double.MIN_VALUE };
         panelInfo.setLayout(gbl_panelInfo);
 
         JLabel lblState = new JLabel("State:");
         GridBagConstraints gbc_lblState = new GridBagConstraints();
         gbc_lblState.fill = GridBagConstraints.BOTH;
         gbc_lblState.insets = new Insets(0, 0, 5, 5);
         gbc_lblState.gridx = 0;
         gbc_lblState.gridy = 0;
         panelInfo.add(lblState, gbc_lblState);
 
         lblCurrentState = new JLabel("stopped");
         GridBagConstraints gbc_lblCurrentState = new GridBagConstraints();
         gbc_lblCurrentState.insets = new Insets(0, 0, 5, 0);
         gbc_lblCurrentState.fill = GridBagConstraints.BOTH;
         gbc_lblCurrentState.gridx = 1;
         gbc_lblCurrentState.gridy = 0;
         panelInfo.add(lblCurrentState, gbc_lblCurrentState);
         lblCurrentState.setForeground(Color.RED);
 
         JLabel lblWorkingThreads = new JLabel("Working threads:");
         GridBagConstraints gbc_lblWorkingThreads = new GridBagConstraints();
         gbc_lblWorkingThreads.anchor = GridBagConstraints.WEST;
         gbc_lblWorkingThreads.insets = new Insets(0, 0, 5, 5);
         gbc_lblWorkingThreads.gridx = 0;
         gbc_lblWorkingThreads.gridy = 1;
         panelInfo.add(lblWorkingThreads, gbc_lblWorkingThreads);
 
         JLabel lblCurrentWorkingThreads = new JLabel("0");
         GridBagConstraints gbc_lblCurrentWorkingThreads = new GridBagConstraints();
         gbc_lblCurrentWorkingThreads.insets = new Insets(0, 0, 5, 0);
         gbc_lblCurrentWorkingThreads.anchor = GridBagConstraints.WEST;
         gbc_lblCurrentWorkingThreads.gridx = 1;
         gbc_lblCurrentWorkingThreads.gridy = 1;
         panelInfo.add(lblCurrentWorkingThreads, gbc_lblCurrentWorkingThreads);
 
         JPanel panelConsole = new JPanel();
         tabbedPaneMonitor.addTab("Console", null, panelConsole, null);
         GridBagLayout gbl_panelConsole = new GridBagLayout();
         gbl_panelConsole.columnWidths = new int[] { 364, 0 };
         gbl_panelConsole.rowHeights = new int[] { 50, 0 };
         gbl_panelConsole.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
         gbl_panelConsole.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
         panelConsole.setLayout(gbl_panelConsole);
        
        
         GridBagConstraints gbc_scrollPane = new GridBagConstraints();
         gbc_scrollPane.fill = GridBagConstraints.BOTH;
         gbc_scrollPane.gridx = 0;
         gbc_scrollPane.gridy = 0;
         panelConsole.add(scrollPane, gbc_scrollPane);
        scrollPane.setViewportView(textPane);
        
        
        textPane.setAlignmentX(Component.RIGHT_ALIGNMENT);
 
         Thread consoleWriter = new Thread(new ConsoleWriter(this));
         consoleWriter.start();
 
     }
 }
 
 class ConsoleWriter implements Runnable {
 
     private MonitorPanel panelMonitor;
 
     ConsoleWriter(MonitorPanel panelMonitor) {
         this.panelMonitor = panelMonitor;
     }
 
     @Override
     public void run() {
         try {
             while (true) {
                 if (AppenderLog4j.APPENDER != null) {
                     synchronized (AppenderLog4j.APPENDER) {
                         StringBuffer buffer = AppenderLog4j.APPENDER
                                 .getBuffer();
                         if (buffer.length() > 0) {
                             panelMonitor.appendToConsole(buffer.toString());
                             buffer.setLength(0);
                         }
 
                     }
                 }
                 Thread.sleep(1000);
             }
         } catch (InterruptedException e) {
         }
 
     }
 }
