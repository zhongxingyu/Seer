 package com.cs408.supersweeper;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Properties;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.SwingConstants;
 
 public class GameFrame implements ActionListener {
 
     private JFrame frame;
     private JMenuItem mntmRestartLevel;
     private JMenuItem mntmLevelSelect;
     private JMenuItem mntmExit;
     private JMenuItem mntmHelp;
     private JLabel lblScore;
     private String propFile;
     private int userScore = 0;
     private LevelSelectPanel lsp;
     private GamePanel gp;
     
     private static final String helpMessage = "Powerups are a powerful tool for winning SuperSweeper.  Their point value will deduct from your points.\n\n" +
             "Extra Lives will save you if you click on a mine.\nThe Metal Detector will allow you to see a small part of the board for a second.\n"
             + "The Missile will destroy a part of the board, safely detonating all mines.";
 
 
 
     /**
      * Launch the application.
      */
     public static void main(String[] args) {
         new GameFrame();
 
     }
 
     /**
      * Create the application.
      * 
      * @throws IOException
      */
     public GameFrame() {
         initialize();
         frame.setVisible(true);
     }
 
     /**
      * Initialize the contents of the frame.
      * 
      * @throws IOException
      */
     private void initialize() {
         frame = new JFrame();
         frame.setTitle("SuperSweeper!");
         frame.setIconImage(GridUnit.images.get("mine"));
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         
         frame.getContentPane().setLayout(new GridBagLayout());
         GridBagConstraints c = new GridBagConstraints();
         c.gridy = 0;
         
         lsp = new LevelSelectPanel(this);
         frame.getContentPane().add(lsp, c);
         
         JMenuBar menuBar = new JMenuBar();
         frame.setJMenuBar(menuBar);
 
         menuBar.setLayout(new BorderLayout());
         
         JMenu mnOptions = new JMenu("Options");
         menuBar.add(mnOptions, BorderLayout.WEST);
         
         mntmLevelSelect = new JMenuItem("Level Select");
         mnOptions.add(mntmLevelSelect);
         mntmLevelSelect.addActionListener(this);
 
         mntmRestartLevel = new JMenuItem("Restart Level");
         mnOptions.add(mntmRestartLevel);
         mntmRestartLevel.addActionListener(this);
         
         mntmHelp = new JMenuItem("Help");
         mnOptions.add(mntmHelp);
         mntmHelp.addActionListener(this);
         
 //        mntmResetScore = new JMenuItem("Reset Score");
 //        mnOptions.add(mntmResetScore);
 //        mntmResetScore.addActionListener(this);
 
         mntmExit = new JMenuItem("Exit");
         mnOptions.add(mntmExit);
         
         lblScore = new JLabel();
         //userScore = loadSavedScore();
         userScore = 0;
         lblScore.setText("Score: " + userScore + "   ");
         lblScore.setHorizontalAlignment(SwingConstants.RIGHT);
         menuBar.add(lblScore, BorderLayout.EAST);
         mntmExit.addActionListener(this);
 
         //Resize
         resizeFrame();
         
         //Move to center of screen
         Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
         frame.setLocation(dim.width/2-frame.getSize().width/2, dim.height/2-frame.getSize().height/2);
     }
 
     public void actionPerformed(ActionEvent e) {
         Object action = e.getSource();
 
         if (action == mntmRestartLevel) {
             if(gp != null) {
                 gp.restartLevel();
                 gp.getGameState().setScore(0);
             }
 
         }  else if (action == mntmLevelSelect) {
             gotoLevelSelect();
            gp.getGameState().setScore(0);
         } else if (action == mntmExit) {
             if (gp != null){
                 getScore();
             }
             frame.dispose();
             System.exit(0);
         } else if (action == mntmHelp) {
             JOptionPane.showMessageDialog(null, helpMessage, "Help", JOptionPane.PLAIN_MESSAGE);
         }
     }
     
 //    public int loadSavedScore() {
 //        try {
 //            _prop.load(this.getClass().getResourceAsStream("/userProgress.properties"));
 //        } catch (Exception e) {
 //            System.err.println("Could not locate Properties File: userProgress score");
 //            System.exit(1);
 //        }  
 //        return Integer.parseInt(_prop.getProperty("score"));
 //    }
 //    
 //    public void saveScore() {
 //        //TODO: How to save the score when someone exits program another way other than hitting exit option
 //        _prop.setProperty("score", Integer.toString(userScore));
 //        try {
 //            _prop.store(new FileOutputStream("/" + this.propFile), null);
 //        } catch (Exception e) {
 //            e.printStackTrace();
 //        } 
 //    }
     
     public void getScore() {
         userScore = gp.getGameState().getScore();
     }
 
     public void startLevel(String propertiesFile) {  
         GridBagConstraints c = new GridBagConstraints();
         c.gridy = 0;
         
         this.propFile = propertiesFile;
         
         //Remove levelSelectPanel
         frame.remove(lsp);
         
         int level;
         if(propertiesFile.contains("bonus"))
             level = 9;
         else
             level = Integer.parseInt(propertiesFile.charAt(2) + "");
         System.out.println(level);
         //Add appropriate GamePanel
         gp = new GamePanel(level, propertiesFile, lblScore);
         frame.getContentPane().add(gp, c);
         
         resizeFrame();
     }
     
     public void gotoLevelSelect(){
         if(gp == null) {
             return;
         }
         
         getScore();
         
         GridBagConstraints c = new GridBagConstraints();
         c.gridy = 0;
         
         //Remove gamePanel
         frame.remove(gp);
         
         //Add new LevelSelectPanel
         frame.getContentPane().add(lsp, c); 
 
         resizeFrame();
     }
     
     /** Internal Methods */
     private void resizeFrame() {
         //Tried using setMax/Min Size but SetMaximumSize is broken (known Bug)
         //So i just made it not resizeable >.>
         frame.pack();
         frame.setResizable(false);
         frame.setVisible(true);
     }
 }
