 package gui;
 
 import java.awt.CardLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyListener;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import game.GameState;
 import game.Logic;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.GridBagLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JLabel;
 import javax.swing.UIManager;
 import org.apache.log4j.Logger;
 
 /**
  *
  * @author Haisin Yip
  * @author Michael Smith
  */
 
 public class MenuGUI implements Runnable
 {
     
     private static final int DEFAULT_WIDTH = 800;
     private static final int DEFAULT_HEIGHT = 600;
     
     public static int WIDTH = DEFAULT_WIDTH;
     public static int HEIGHT = DEFAULT_HEIGHT;
     
    
     //main window, associated elements
     private JFrame frame;
     private CardLayout cardLayout;
     private JPanel card = new JPanel();
     
     // menu buttons
     public JButton singlePbutton = new JButton("SINGLE-PLAYER MODE");
     public JButton twoPbutton = new JButton("TWO-PLAYER MODE");
     public JButton leaderBoardButton = new JButton("LEADERBOARD");
     public JButton tutorialButton = new JButton("TUTORIAL");
     public JButton quitButton = new JButton("QUIT");
     
     //back button for both leaderboard and tutorial
     public JButton backButton = new JButton("BACK");
     
     private ActionListener buttonClick;
     private KeyListener keyboard;
     
     //whether or not we are in single player mode
     private boolean singleP = false;
     
     //Panels for single and 2 player modes
     private SinglePgamePanel onePPanel;
     private TwoPgamePanel twoPPanel;
     
     private GameOverPanel gameOverPanel;
     
     private final static Logger log = Logger.getLogger(MenuGUI.class.getName());
     
     /**
      * Starts the GUI Menu.
      * @param AL
      * @param keyb 
      */
     public MenuGUI(ActionListener AL, KeyListener keyb)
     {
         try {
             //use system theme: windows/mac/linux: looks better
             UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
         } catch (Exception e) {
         }
         
         log.setLevel(Logic.LOG_LEVEL);
         
         // create and initialize frame
         frame = new JFrame("AMBroSIA");
         cardLayout = new CardLayout();
         card.setLayout(cardLayout);
         frame.getContentPane().add(card);
         
         //allow global access to actionlistener,keyboard listener
         buttonClick = AL;
         keyboard = keyb;
         
         //set important parameters
         frame.setVisible(true);
         frame.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
         frame.setLocation(100, 100);
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         //frame.setMinimumSize(new Dimension(DEFAULT_WIDTH,DEFAULT_HEIGHT));
         frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
         
         //set up (but do not yet allow) keyboard input
         frame.addKeyListener(keyboard);
         frame.setFocusable(false);
     }
  
     public void showMenu() 
     {
         //disable keyboard input
         frame.setFocusable(false);
         // create menu page panel, set it up, show it
         JPanel cardMenu = new JPanel(new GridLayout(2, 1));
         cardMenu.add(new MenuPanel());
         
  
         // initialize the 5 buttons that shall be in the menu page
         JPanel buttonPanelMenu = new JPanel(new GridLayout(5, 1));
         
         buttonPanelMenu.add(singlePbutton);
         buttonPanelMenu.add(twoPbutton);
         buttonPanelMenu.add(leaderBoardButton);
         buttonPanelMenu.add(tutorialButton);
         buttonPanelMenu.add(quitButton);
         
         singlePbutton.addActionListener(buttonClick);
         twoPbutton.addActionListener(buttonClick);
         leaderBoardButton.addActionListener(buttonClick);
         tutorialButton.addActionListener(buttonClick);
         quitButton.addActionListener(buttonClick);
         
         cardMenu.add(buttonPanelMenu);
         card.add("Menu", cardMenu);
         cardLayout.show(card, "Menu");
         
         frame.setResizable(true);
     }
     
     public void displaySingleP(GameState gs) 
     {
         //allow keyboard input
         frame.setFocusable(true);
        //create panel, show it
         onePPanel = new SinglePgamePanel(gs, new ImageIcon("./src/images/spaceBackground.jpg").getImage());
         JPanel cardGame1P = new JPanel();
         cardGame1P.add(onePPanel);
         card.add("Single-Player Mode", cardGame1P);
         cardLayout.show(card, "Single-Player Mode");
         //let other methods know we are in single P mode
         singleP = true;
         
         //do not allow resizing at this point, as it can disrupt gameplay
         frame.setResizable(false);
     }
 
     public void displayTwoP(GameState gs) 
     {
         frame.setFocusable(true);
         twoPPanel = new TwoPgamePanel(gs, new ImageIcon("./src/images/spaceBackground.jpg").getImage());
         JPanel cardGame2P = new JPanel();
         //cardGame.setLayout(new GridLayout(2,1)); not sure how to set layout for actual gameplay
         cardGame2P.add(twoPPanel);
         card.add("Two-Player Mode", cardGame2P);
         cardLayout.show(card, "Two-Player Mode");
         frame.setResizable(false);
     }
        
     public void displayLeaderBoard() 
     {
         // create leaderboard page
         JPanel cardLeaderBoard = new JPanel();
         cardLeaderBoard.setLayout(new BorderLayout());
         JPanel leaderBoardPanel = new LeaderBoardPanel(WIDTH, HEIGHT);
         cardLeaderBoard.add(leaderBoardPanel, BorderLayout.NORTH);
         //initialize a back button
         JPanel buttonPanelLeaderBoard = new JPanel();
         buttonPanelLeaderBoard.add(backButton);
         backButton.addActionListener(buttonClick);
         cardLeaderBoard.add(buttonPanelLeaderBoard, BorderLayout.SOUTH);
         card.add("LeaderBoard", cardLeaderBoard);
         cardLayout.show(card, "LeaderBoard");
     }
 
     public void displayTutorial() 
     {
         JPanel cardTutorial = new JPanel();
         cardTutorial.setLayout(new BorderLayout());
         JPanel tutorialPanel = new TutorialPanel(new ImageIcon("./src/images/keyboard.jpg").getImage());
         cardTutorial.add(tutorialPanel, BorderLayout.NORTH);
         //initialize a back button
         JPanel buttonPanelTutorial = new JPanel();
         buttonPanelTutorial.add(backButton);
         buttonPanelTutorial.setBackground(Color.white);
         backButton.addActionListener(buttonClick);
         cardTutorial.add(buttonPanelTutorial, BorderLayout.SOUTH);
         cardTutorial.setBackground(Color.white);
         card.add("Tutorial", cardTutorial);
         cardLayout.show(card, "Tutorial");
     }
     
     public void displayGameOver(GameState gs)
     {
         gameOverPanel = new GameOverPanel(new ImageIcon("./src/images/spaceBackground.jpg").getImage(), gs);
         JPanel cardGameOver = new JPanel();
         cardGameOver.setLayout(new BorderLayout());
         cardGameOver.add(gameOverPanel);
         
         //initialize a back button
         JPanel buttonPanelGameOver = new JPanel();
         buttonPanelGameOver.add(backButton);
         buttonPanelGameOver.setBackground(Color.black);
         backButton.addActionListener(buttonClick);
         cardGameOver.add(buttonPanelGameOver, BorderLayout.SOUTH);
         cardGameOver.setBackground(Color.white);
         card.add("GameOver", cardGameOver);
         cardLayout.show(card, "GameOver");
     }
         
     public void goBack() { cardLayout.show(card, "Menu");}
 
     public void updateDraw()
     {
         if (singleP)
         {
            onePPanel.updatePanel();
         }
         else
         {
             twoPPanel.updatePanel();
         }
     }
 
     @Override
     public void run() 
     {
         log.info("GUI updating");
         updateSize();
         updateDraw();
     }
     
     private void updateSize() 
     {
         MenuGUI.WIDTH = frame.getWidth();
         MenuGUI.HEIGHT = frame.getHeight();
     }
 }
