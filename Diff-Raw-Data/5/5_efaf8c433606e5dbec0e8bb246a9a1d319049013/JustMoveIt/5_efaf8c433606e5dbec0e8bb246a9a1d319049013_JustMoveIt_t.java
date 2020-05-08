 package com.chandu;
 
 import java.awt.AWTException;
 import java.awt.CardLayout;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.Robot;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.KeyEvent;
 import java.util.Arrays;
 import java.util.Random;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 /**
 * @version 0.0.2
  * @author Chandrasekhar Thotakura
  */
 public enum JustMoveIt {
 
     ONE;
 
     private void run() {
         ControlWindow.ONE.open();
     }
 
     public static void main(final String[] tcs) {
         JustMoveIt.ONE.run();
     }
 }
 
 enum MouseMover {
 
     ONE;
     private Random rnd;
     private Robot robo;
     private int width;
     private int height;
 
     private MouseMover() {
         final Dimension screenSize =
                 Toolkit.getDefaultToolkit().getScreenSize();
         width = (int) screenSize.getWidth();
         height = (int) screenSize.getHeight();
         rnd = new Random();
         try {
             robo = new Robot();
         } catch (AWTException ex) {
             Logger.getLogger(
                     MouseMover.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     public void move() {
         final int posX = rnd.nextInt(width);
         final int posY = rnd.nextInt(height);
         robo.mouseMove(posX, posY);
     }
 }
 
 enum ControlWindow implements ActionListener, ItemListener {
 
     ONE;
     private static final String EMPTY = "";
     private static final int WINDOW_WIDTH = 300;
     private static final int WINDOW_HEIGHT = 200;
     private static final int MILLIS_PER_SECOND = 1000;
     private static final int SECS_PER_MINUTE = 60;
     private static final int MINS_PER_HOUR = 60;
     private JFrame frame;
     private JPanel pane, inputPanel, outputPanel;
     private JButton startButton, stopButton, iExitButton, oExitButton;
     private JCheckBox fixedTimeCheckBox;
     private JComboBox hoursComboBox, minutesComboBox, secondsComboBox;
     private JLabel intervalLabel, elapsedLabel, remainingLabel;
     private CardLayout cardLayout;
     private DurationUpdater task;
     private Timer timer;
     private int intervalSeconds, elapsedSeconds, scheduledSeconds;
     private boolean isFixedTime;
 
     private ControlWindow() {
         prepareInputPanel();
         prepareOutputPanel();
 
         cardLayout = new CardLayout();
         pane = new JPanel(cardLayout);
         pane.add(inputPanel, "input");
         pane.add(outputPanel, "output");
         frame = new JFrame("JustMoveIt - Chandu");
         frame.setContentPane(pane);
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.setResizable(false);
         frame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
         //frame.pack();
         frame.setLocationRelativeTo(null);
 
         timer = new Timer();
     }
 
     private void prepareInputPanel() {
         fixedTimeCheckBox = new JCheckBox("Fixed Time?");
         fixedTimeCheckBox.setMnemonic(KeyEvent.VK_F);
         fixedTimeCheckBox.setSelected(false);
         fixedTimeCheckBox.addItemListener(this);
 
         final JPanel panel1 = new JPanel(new FlowLayout());
         panel1.add(fixedTimeCheckBox);
 
         hoursComboBox = new JComboBox(getNumbers(23));
         hoursComboBox.setSelectedIndex(1);
         hoursComboBox.setEnabled(false);
         minutesComboBox = new JComboBox(getNumbers(59));
         minutesComboBox.setEnabled(false);
 
         final JPanel panel2 = new JPanel(new FlowLayout());
         panel2.add(new JLabel("Hours:"));
         panel2.add(hoursComboBox);
         panel2.add(new JLabel("Minutes:"));
         panel2.add(minutesComboBox);
 
         secondsComboBox = new JComboBox(
                 Arrays.copyOfRange(getNumbers(120), 1, 121));
         secondsComboBox.setSelectedIndex(5);
 
         final JPanel panel3 = new JPanel(new FlowLayout());
         panel3.add(new JLabel("Time interval (seconds):"));
         panel3.add(secondsComboBox);
 
         startButton = new JButton("START");
         startButton.addActionListener(this);
         startButton.setMnemonic(KeyEvent.VK_S);
         iExitButton = new JButton("  EXIT  ");
         iExitButton.addActionListener(this);
         iExitButton.setMnemonic(KeyEvent.VK_X);
 
         final JPanel panel4 = new JPanel(new FlowLayout());
         panel4.add(startButton);
         panel4.add(iExitButton);
 
         inputPanel = new JPanel(new GridLayout(4, 1));
         inputPanel.add(panel1);
         inputPanel.add(panel2);
         inputPanel.add(panel3);
         inputPanel.add(panel4);
     }
 
     private void prepareOutputPanel() {
         intervalLabel = new JLabel(EMPTY);
         final JPanel panel1 = new JPanel(new FlowLayout());
         panel1.add(new JLabel("Time interval:"));
         panel1.add(intervalLabel);
 
         elapsedLabel = new JLabel(EMPTY);
         final JPanel panel2 = new JPanel(new FlowLayout());
         panel2.add(new JLabel("Elapsed time:"));
         panel2.add(elapsedLabel);
 
         remainingLabel = new JLabel(EMPTY);
         final JPanel panel3 = new JPanel(new FlowLayout());
         panel3.add(new JLabel("Remaining time:"));
         panel3.add(remainingLabel);
 
         stopButton = new JButton("STOP");
         stopButton.addActionListener(this);
         stopButton.setMnemonic(KeyEvent.VK_O);
         oExitButton = new JButton(" EXIT ");
         oExitButton.addActionListener(this);
         oExitButton.setMnemonic(KeyEvent.VK_X);
 
         final JPanel panel4 = new JPanel(new FlowLayout());
         panel4.add(stopButton);
         panel4.add(oExitButton);
 
         outputPanel = new JPanel(new GridLayout(4, 1));
         outputPanel.add(panel1);
         outputPanel.add(panel2);
         outputPanel.add(panel3);
         outputPanel.add(panel4);
     }
 
     private void updateLabels() {
         final String remainingText = isFixedTime
                 ? getFormattedTime(scheduledSeconds - elapsedSeconds)
                 : "UKNOWKN";
         remainingLabel.setText(remainingText);
         elapsedLabel.setText(getFormattedTime(elapsedSeconds));
     }
 
     public void open() {
         frame.setVisible(true);
     }
 
     private String[] getNumbers(final int num) {
         String[] returnArr = new String[num + 1];
         for (int i = 0; i <= num; i += 1) {
             returnArr[i] = EMPTY + i;
         }
         return returnArr;
     }
 
     private int getValueFromComboBox(final JComboBox comboBox) {
         return Integer.parseInt(comboBox.getSelectedItem().toString());
     }
 
     private String getFormattedTime(final int seconds) {
         // String.format("%d:%02d:%02d", s/3600, (s%3600)/60, (s%60));
         return String.format("   %02d : %02d : %02d",
                 seconds / (MINS_PER_HOUR * SECS_PER_MINUTE),
                 (seconds % (MINS_PER_HOUR * SECS_PER_MINUTE)) / MINS_PER_HOUR,
                 seconds % SECS_PER_MINUTE);
     }
 
     @Override
     public void actionPerformed(final ActionEvent event) {
         final Object source = event.getSource();
         if (source == startButton) {
             intervalSeconds = getValueFromComboBox(secondsComboBox);
             final int minutes = getValueFromComboBox(minutesComboBox);
             final int hours = getValueFromComboBox(hoursComboBox);
             scheduledSeconds = (MINS_PER_HOUR * hours + minutes) * SECS_PER_MINUTE;
             intervalLabel.setText(EMPTY + intervalSeconds);
             elapsedSeconds = 0;
             updateLabels();
             task = new DurationUpdater();
             timer.scheduleAtFixedRate(task, MILLIS_PER_SECOND, MILLIS_PER_SECOND);
             cardLayout.last(pane);
         } else if (source == stopButton) {
             task.cancel();
             timer.purge();
             cardLayout.first(pane);
         } else if (source == iExitButton) {
             System.exit(0);
         } else if (source == oExitButton) {
             task.cancel();
             timer.cancel();
             System.exit(0);
         }
     }
 
     @Override
     public void itemStateChanged(final ItemEvent event) {
         if (event.getSource() == fixedTimeCheckBox) {
             final boolean isSelected =
                     (event.getStateChange() == ItemEvent.SELECTED);
             hoursComboBox.setEnabled(isSelected);
             minutesComboBox.setEnabled(isSelected);
             isFixedTime = isSelected;
         }
     }
 
     private class DurationUpdater extends TimerTask {
 
         @Override
         public void run() {
             elapsedSeconds += 1;
             updateLabels();
            if ((isFixedTime && elapsedSeconds >= scheduledSeconds)
                     || elapsedSeconds == Integer.MAX_VALUE) {
                 task.cancel();
                 timer.cancel();
                 System.exit(0);
             }
             if (elapsedSeconds % intervalSeconds == 0) {
                 MouseMover.ONE.move();
             }
         }
     }
 }
