 package uk.ac.york.minesweeper;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.SwingUtilities;
 import javax.swing.Timer;
 
 public class MinesweeperFrame extends JFrame implements ActionListener
 {
     private static final long serialVersionUID = 1L;
 
     // Constants
     private static final String[] DIFFICULTIES = { "Easy", "Medium", "Hard" };
 
     private static final String INCREMENT = "incr";
     private static final String RESET = "reset";
 
     // Interface
     private JPanel mainPanel =  new JPanel(new BorderLayout(10, 10));
     private JComboBox<String> difficultyBox = new JComboBox<String>(DIFFICULTIES);
     private MinefieldPanel minePanel;
 
     // Timer
     private Timer scoreTimer = new Timer(1000, this);
     private JLabel topTimer;
     private int time = 0;
 
     // Button Images
     private JButton topResetBtn;
 
     public MinesweeperFrame()
     {
         // Basic Interface Settings
         this.setDefaultCloseOperation(EXIT_ON_CLOSE);
         this.setLayout(new BorderLayout(0,0));
         this.getContentPane().setBackground(Color.white);
         this.setSize(new Dimension(400, 500));
         this.setMinimumSize(new Dimension(400, 500));
         this.setTitle("Minesweeper");
 
         // Interface Structure
         JPanel topPanel = new JPanel(new GridLayout(1, 3, 10, 10));
         topPanel.setBackground(Color.white);
 
         JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
         centerPanel.setBackground(Color.white);
 
         JPanel centerMidPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
 
         minePanel = new MinefieldPanel(new Minefield(16, 16, 40));
         minePanel.addStateChangeListener(new MinefieldStateChangeListener()
         {
             @Override
             public void stateChanged(MinefieldStateChangeEvent event)
             {
                 Minefield minefield = minePanel.getMinefield();
 
                 if (minefield.isFinished())
                 {
                     // Stop timer and set icon
                     scoreTimer.stop();
 
                     if (minefield.getGameState() == GameState.WON)
                         topResetBtn.setIcon(new ImageIcon(Images.FACE_WON));
                     else
                         topResetBtn.setIcon(new ImageIcon(Images.FACE_LOST));
                 }
                 else
                 {
                     // Set normal face and start timer if we've just started
                     topResetBtn.setIcon(new ImageIcon(Images.FACE_NORMAL));
 
                     if (minefield.getGameState() == GameState.RUNNING)
                         scoreTimer.start();
                 }
 
                 topResetBtn.repaint();
             }
         });
 
         centerMidPanel.add(minePanel);
 
         // Difficulty Chooser
         difficultyBox.setSelectedIndex(1);
 
         // Reset Button
         topResetBtn = new JButton();
         topResetBtn.setPreferredSize(new Dimension(50, 50));
         topResetBtn.setActionCommand(RESET);
         topResetBtn.addActionListener(this);
         centerPanel.add(topResetBtn);
 
         topResetBtn.setIcon(new ImageIcon(Images.FACE_NORMAL));
 
         // Labels
         topTimer = new JLabel(String.valueOf(time) + " Seconds");
         scoreTimer.setActionCommand(INCREMENT);
 
         // Adding Items to Grid
         topPanel.add(difficultyBox);
         topPanel.add(centerPanel);
         topPanel.add(topTimer);
         mainPanel.add(topPanel, BorderLayout.NORTH);
         mainPanel.add(centerMidPanel, BorderLayout.CENTER);
 
         this.getContentPane().add(mainPanel, BorderLayout.NORTH);
         this.pack();
     }
 
     @Override
     public void actionPerformed(ActionEvent event)
     {
         if(event.getActionCommand().equals(INCREMENT))
         {
             time++;
         }
         else if(event.getActionCommand().equals(RESET))
         {
             time = 0;
 
             if (difficultyBox.getSelectedIndex() == 0)
             {
                 minePanel.setMinefield((new Minefield(9, 9, 10)));
             }
             else if (difficultyBox.getSelectedIndex() == 2)
             {
                 minePanel.setMinefield((new Minefield(30, 16, 99)));
             }
             else if (difficultyBox.getSelectedIndex() == 1)
             {
                 minePanel.setMinefield((new Minefield(16, 16, 40)));
             }
 
             pack();
         }
 
         topTimer.setText((time) + " Seconds   ");
     }
 
     public static void main(String[] args)
     {
         SwingUtilities.invokeLater(new Runnable()
         {
             @Override
             public void run()
             {
                 new MinesweeperFrame().setVisible(true);
             }
         });
     }
 }
