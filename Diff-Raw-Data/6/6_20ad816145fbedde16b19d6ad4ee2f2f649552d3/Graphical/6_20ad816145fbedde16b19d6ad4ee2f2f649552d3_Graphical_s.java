 package gui.graphical;
 
 // Graphical Libraries (AWT)
 import java.awt.Dimension;
 
 // Graphical Libraries (Swing)
 import javax.swing.JFrame;
 import javax.swing.SwingUtilities;
 
 // Libraries
 import gui.*;
 import arena.Map;
 import parameters.*;
 
 /**
  * <b>GUI - Graphical Mode</b><br>
  * Makes an implementation of the interface
  * GUI for exhibiting the game, using Java's
  * default graphic libraries (AWT and Swing).
  * 
  * @author Karina Suemi
  * @author Renato Cordeiro Ferreira
  * @author Vinicius Silva
  */
 public class Graphical extends JFrame implements GUI,Game
 {
     /* Auxiliar variables for keeping interface GUI */
     private boolean firstTime = true;
     private Map map;
     
     /** 
      * Default constructor.<br>
      * @param map Object of the class map
      *            from package arena.
      */
     public Graphical(Map map)
     {
         this.map = map;
         
         /* TODO: Take out hardcoded strings */
         this.setTitle("Robot's Battle");
 		this.setSize(718, 635);
         this.setLocationRelativeTo(null);
         this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         
        Panel screen = new Panel(25, 600, 600, map);
        add(screen);
     }
     
     /**
      * Paint 1 frame of the game.<br>
      * Exhibits a new frame in the main
      * window created in the game.
      */
     public void paint()
     {
         try { Thread.sleep(SPEED); } 
         catch (Exception e) { }
         
         this.printMap();
     }
     
     /**
      * Shows Map.<br>
      * Creates a Map with more details of each element
      * of the scenarios and items in the map.
      */
     public void printMap()
     {
         if(this.firstTime)
         {
             SwingUtilities.invokeLater(new Runnable() {
                 @Override
                 public void run() { setVisible(true); }
             });
             this.firstTime = false;
         }
         screen.repaint();
     }
     
     /** 
      * Shows Mini Map.<br>
      * Creates a Mini Map with dimensions MAP_SIZE
      * x MAP_SIZE, with a one-character representation
      * of each scenario/item in the map.
      */
     public void printMiniMap() { }
 }
