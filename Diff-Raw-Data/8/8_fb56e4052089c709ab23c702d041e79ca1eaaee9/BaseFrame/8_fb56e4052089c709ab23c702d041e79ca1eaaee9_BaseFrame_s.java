 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package GUIComponents;
 
 import java.awt.CardLayout;
 import java.awt.Dimension;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 /**
  * Seems like somewhere in here we need to have the keylisteners and
  * mouselisteners, since the frame is what is focused when you're clicking and
  * pressing keys. This means that the mousecontrol and keyboardcontrol classes
  * should be instances of keylistener and mouselistener so that we can just
  * pass those to the addlistener methods. Of couse, this is only guesswork, since
  * i've not done it before. Many problems here. I suggest for the time being we do
  * it the simple way and just make listeners in here to listen for things.
  * But there's another problem! If we're using this frame as the main thing, and
  * we're swapping panels, then listeners have to do different things based on game
  * state - might be solvable if we reference the panel and get the game state.
  * 
  * @author michal
  */
 /**
  * Constructor sets up the frame by adding the listeners required and creates a new controls object
  * @author Dave
  */
 public class BaseFrame extends JFrame {
 
     private static Dimension _windowSize;
     JPanel cardPanel;
     GamePanel gamePanel;
     MenuPanel menuPanel;
 
     enum Panels {
 
         GAME, MENU
     }
     // TODO change this to start from the menu. Will require a change in the init methods as well.
     Panels currentPanel = Panels.GAME;
 
     public BaseFrame(Dimension windowSize) {
         _windowSize = windowSize;
 //        gamePanel = new GamePanel(this.getWidth(), this.getHeight(), "localhost", 2000); // client
 //        this.setTitle("client");
         gamePanel = new GamePanel(this.getWidth(), this.getHeight(), 2000, 4); //server
         this.setTitle("server");
         menuPanel = new MenuPanel();
         initCardLayoutPanel();
         initFrame();
        
     }
 
     private void initFrame() {
        setSize(_windowSize);
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         setEnabled(true);
         add("Center", cardPanel);
         gamePanel.initialize();
         setVisible(true);
         waitForPanelChangeRequest();
     }
 
     private void initCardLayoutPanel() {
         cardPanel = new JPanel(new CardLayout());
         cardPanel.add(gamePanel, "game");
         cardPanel.add(menuPanel, "menu");
     }
 
     /**
      * Waits for a boolean in the game to be switched to a state in which it wants
      * the panel changed.
      */
     private void waitForPanelChangeRequest() {
         while (true) {
             switch (currentPanel) {
                 case GAME:
                     boolean gTemp = gamePanel.getPanelSwitchRequest();
                     if (gTemp == true) {
                         switchPanels();
                     }
                     break;
                 case MENU:
                     boolean mTemp = menuPanel.getPanelSwitchRequest();
                     if (mTemp == true) {
                         switchPanels();
                     }
                     break;
             }
             try {
                 Thread.sleep(500);
             } catch (InterruptedException ex) {
                 System.out.printf("Thread interrupted at %s, method %s\n", this.getClass().toString(), "waitforpanelchangerequest");
                 ex.printStackTrace();
             }
         }
     }
 
     /**
      * Switches the panel to the next panel. The frame starts off in the
      * first panel added to the cardlayout in the initcardlayoutpanel method.
      *
      * !!!!!!!!!This method assumes that there are only two panels!!!!!!!!!!!!!!
      */
     public void switchPanels() {
         CardLayout l = (CardLayout) cardPanel.getLayout();
         switch (currentPanel) {
             case GAME:
                 l.next(cardPanel);
                 currentPanel = Panels.MENU;
                 break;
             case MENU:
                 /* TODO set any variables that need setting or
                  * methods that need executing (in the menu) when the menu is quit here.
                  * !!!!!Also use this to pass things from the menu to the game !!!!
                  */
                 l.next(cardPanel);
                 gamePanel.regainFocus();
                 gamePanel.setRun(true);
                 currentPanel = Panels.GAME;
                 break;
         }
     }
 
     public static Dimension getWindowSize() {
         return _windowSize;
     }
 }
