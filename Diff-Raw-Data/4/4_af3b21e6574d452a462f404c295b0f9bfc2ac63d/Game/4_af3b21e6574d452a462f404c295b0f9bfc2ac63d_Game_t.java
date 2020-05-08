 import javax.swing.*;
 import java.awt.*;
 import java.io.*;
 import java.awt.event.*;
 
 /** Main class for Condi
  * 
  * @author Charles Zinn
  */
public class Game extends JFrame implements KeyListener {
   public static int FPS = 60;
   
   TextPanel p;
   
   String message;
   CharCol messageCol;
   int messageTime;
   
   int gs;
   int sgs;
   static int GS_MAIN_MENU = 0;
   static int GS_GAME = 1;
   
   Menu menuMain;
   
   Map test;
   
   public static void main(String[] args) {
     Game g = new Game();
   }
   
   Game() {
     super("Roguelike");
     p = new TextPanel(50, 80);
     this.add(p);
     this.pack();
     this.addKeyListener(this);
     this.setVisible(true);
     
     //Init game varibles
     message = "";
     messageTime = 0;
     messageCol = new CharCol();
     
     gs = GS_MAIN_MENU;
     sgs = 0;
     
     //Init menus
     menuMain = new Menu(new String[]{"Start", "Stop", "test", "four", "wut"});
     
     //Init the test map
     test = new Map(100, 100);
     
     //Start the game
     run();
   }
   
   /** Main game loop */
   public void run() {
     boolean doLoop = true;
     long curTime;
     while(doLoop) {
       //Start the frame timer
       curTime = System.currentTimeMillis();
       
       //Clear drawing surface
       p.clear();
       
       //Draw a border, with a spot at the bottom for messages
       p.drawBox(' ', new CharCol(Color.GRAY, Color.GRAY), 0, 0, 48, 80);
       p.drawBox(' ', new CharCol(Color.GRAY, Color.GRAY), 47, 0, 3, 80);
 
       
       //Draw the message if there is one, and tick down message timer
       if(messageTime > 0) {
         p.drawString(message, messageCol, 48, 3);
         messageTime--;
       }      
       
       if(gs == GS_MAIN_MENU) {
         //Draw the menu
         menuMain.draw(p, new CharCol(), 0, 0, 48, 80);
       } else if(gs == GS_GAME) {
         //Draw the map
         test.draw(p, 1, 1, 0, 0, 46, 78);
       }
       
       
       //Flip buffer and repaint
       p.flip();
       this.repaint();
       
       //End of loop
       //Do a terrible stall-timer loop thing to maintain FPS
       while(System.currentTimeMillis() - curTime < 1000 / FPS) {
         //Do nothing
         
       }
     }
   }
   
   /** Posts a message in the given colour to the message box at bottom of screen */
   public void postMessage(String m, CharCol c) {
     message = m;
     messageCol = c;
     messageTime = 5 * FPS;
   }
   
   /** Handle the key typed event */
   public void keyTyped(KeyEvent e) {
     //Do nothing
   }
   
   /** Handle the key-pressed event */
   public void keyPressed(KeyEvent e) {
     int k = e.getKeyCode();
     postMessage("Pressed " + k, new CharCol());
     
     if(gs == GS_MAIN_MENU) {
       if(k == 38) { //UP
         menuMain.selectUp();
       }
       if(k == 40) { //DOWN
         menuMain.selectDown();
       }
       if(k == 10) { //ENTER (select)
         int sel = menuMain.getSelect();
         if(sel == 0) { //"Start"
           gs = GS_GAME;
         }
       }
     }
   }
   
   /** Handle the key-released event */
   public void keyReleased(KeyEvent e) {
     
   }
   
   /** Generates a random integer value between 'a' and 'b' - 1 */
   public static int rand(int a, int b) {
     return (int)Math.floor(Math.random() * (b - a)) + a;
   }
 }
