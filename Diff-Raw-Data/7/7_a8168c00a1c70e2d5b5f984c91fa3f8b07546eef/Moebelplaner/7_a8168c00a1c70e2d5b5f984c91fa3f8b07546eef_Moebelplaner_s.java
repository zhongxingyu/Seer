 package io.github.christiangaertner.moebelplaner;
 
import io.github.christiangaertner.moebelplaner.graphics.IRenderable;
 import io.github.christiangaertner.moebelplaner.graphics.Renderer;
 import io.github.christiangaertner.moebelplaner.grid.Grid;
 import io.github.christiangaertner.moebelplaner.input.Mouse;
import io.github.christiangaertner.moebelplaner.moebel.Schrank;
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
 import java.awt.image.BufferStrategy;
 import java.awt.image.BufferedImage;
 import java.awt.image.DataBufferInt;
 import javax.swing.JFrame;
 
 /**
  *
  * @author Christian
  */
 public final class Moebelplaner extends Canvas implements Runnable {
     
     /**
      * Zeigt an ob debug info angezeigt werden soll
      */
     private final boolean DEBUG;
     
     /**
      * Der Title (für den JFrame)
      */
     public final static String TITLE = "Moebelplaner";
     
     /**
      * Für Threading...
      */
     private static final long serialVersionUID = 1L;
     
     /**
      * Der Renderer, den den int[] für den Canvas enthäkt
      */
     private Renderer renderer;
     
     /**
      * Zeigt an, ob das Programm gerade läuft
      */
     private boolean running;
     
     /**
      * Der Haupt-Thread
      */
     private Thread thread;
     
     /**
      * Die JFrame instanze
      */
     private JFrame frame;
     
     /**
      * Der MouseListener
      */
     private Mouse mouse;
     
     /**
      * Fenster Breite
      */
     public static final int WIDTH = 1280,
             
             /**
              * Fenster Höhe
              * Durch das "/ 19 * 9" wird das
              * Fenster 16:9
              */
             HEIGHT = WIDTH / 16 * 9;
     
     /**
      * BufferdImage für den Canvas
      */
     private BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
     
     /**
      * Die pixel für das BufferdImage
      */
     private int[] pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
     
     /**
      * Die Haupt Grid
      */
     private Grid grid;
     
     
     /**
      * Neuer Planer nicht im Debug-Mode
      */
     public Moebelplaner() {
         this(false);
     }
     
     /**
      * Neuer Planer.
      * @param debug Ob Debug-info angezeigt werden soll
      */
     public Moebelplaner(boolean debug) {
         DEBUG = debug;
         
         // Für den Canvas
         setPreferredSize(new Dimension(WIDTH, HEIGHT));
         
         renderer = new Renderer(WIDTH, HEIGHT);
         frame = new JFrame();
         mouse = new Mouse();
         
         grid = new Grid(mouse);
         
         
         addMouseListener(mouse);
         addMouseMotionListener(mouse);
     }
     
     
     /**
      * Started den neuen Thread "Display"
      */
     public synchronized void start() {
         running = true;
         thread = new Thread(this, "Display");
         thread.start();
     }
     
     /**
      * Versucht den Thread zu stoppen 
      */
     public synchronized void stop() {
         running = false;
         try {
             thread.join();
         } catch (InterruptedException ex) {
             // Ignorieren
         }
 
     }
     
     /**
      * Started den render-loop
      */
     @Override
     public void run() {
 
         // Die jetzige Zeit
         long lastTime = System.nanoTime();
         
         // Die jetzige Zeit
         long timer = System.currentTimeMillis();
 
         // Das ist nur zur Berechnung des delta von nöten (1 sec)
         final double ns = 1000000000.0 / 60.0;
         
         // Dieser Wert wird gleich genutzt,
         // um zu gucken, wann wir wieder updaten können
         // Wichtig, damit alles immer gleich schnell ist
         // egal wie stark der Computer ist
         double delta = 0;
 
         // Zählt bei wie vielen frames wir gerade sind
         int frames = 0;
         
         // Zählt bei wie vielen updates wir gerade sind
         int updates = 0;
 
         // Sonst muss man erstmal einmal klicken...
         requestFocus();
         
         // Nur die Deklaration (muss ja nicht jeden frame neu passieren)
         long now;
         
         // Der Haupt-Render-Loop
         while (running) {
             // Erstmal "jetzt" speichern
             now = System.nanoTime();
             
             // Dieser Wert wird immer etwas größer.
             // Umso höher die Frequenz von diesem Loop,
             // desto geringer der Anstieg. Dies
             // werden wir nutzen um einen einheitliche update-Rythmus
             // zu erlangen
             delta += (now - lastTime) / ns;
             
             // Wird bei der nächsten irretation gebraucht (s.o.)
             lastTime = now;
 
             // update 60 times per second
             // Hier wird ein while-Loop genutzt, falls delta weit größer
             // als 1 ist, dies kann bei SEHR langsamen Rechner passieren
             while (delta >= 1) {
                 // Updaten wir alles...
                 update();
                 // Notieren den update
                 updates++;
                 // und ziehen 1 von delta ab, damit es wieder
                 // von neuem los gehen kann.
                 delta--;
             }
             
             // Rendern können wir immer,
             // da das ja nichts am Verhalten ändert.
             render();
             // Den Render notieren
             frames++;
             
             // Jede Sekunde den frame titel updaten
             // Praktisch fürs debuggen
             // Dies ist nicht wichtig, wenn wir nicht debuggen.
             if (System.currentTimeMillis() - timer >= 1000 && DEBUG) {
                 timer += 1000;
                 // System.out.println(updates + " ups, " + frames + " fps");
                 frame.setTitle(TITLE + "  |  " + updates + " ups, " + frames + " fps");
                 updates = 0;
                 frames = 0;
             }
 
         }
         stop();
 
     }
     
     /**
      * Updated alles im Programm
      */
     public void update() {
         grid.update();
     }
     
     /**
      * Rendered den Canvas
      */
     public void render() {
         // Buffered einfach die Bilder
         BufferStrategy bs = getBufferStrategy();
         
         // wenn wir das noch nicht haben,
         // dann erstellen wir es
         if (bs == null) {
             createBufferStrategy(3);
             return;
         }
         
         // Wir müssen erstmal wieder alles zurücksetzten,
         // sonst könnten "echos" auftreten
         renderer.clear();
         
         // Einfach das Grpahics object bekommen
         Graphics g = bs.getDrawGraphics();
         
         // Wir renderen unserer Grid
         renderer.render(grid);
         
         
         // Wir nehmen jetzt den int[] den der Renderer
         // verwaltet hat und kopieren ihn in das BufferdImage
         System.arraycopy(renderer.pixels, 0, pixels, 0, pixels.length);
         
         // Wir setzten die Hintergrundfarbe
         // schwarz ist immer ein guter Anfang
         g.setColor(Color.BLACK);
         
         // und füllen jetzt den HG
         g.fillRect(0, 0, getWidth(), getHeight());
         
         // Jetzt füllen wir unserern canvas mit dem BufferedImage
         g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
         
         // Das ist jetzt nur zum Testen
         // etwas text
         g.setColor(Color.RED);
         g.setFont(new Font("Verdana", 0, 50));
         
         if (DEBUG) {
             g.drawString("DEBUG MODE", 50, 50);
             
             String display;
             
             switch(mouse.clicked()) {
                 case 1:
                     display = "LEFT CLICK";
                     break;
                 case 2:
                     display = "MIDDLE CLICK";
                     break;
                 case 3:
                     display = "RIGHT CLICK";
                     break;
                 default:
                     display = "OTHER BUTTON. ID: " + mouse.clicked();
             }
             
             if (mouse.clicked() != -1) {
                 g.drawString(display, 50, 100);
             }
         }
         
         
         // Jetzt das GraphicsObject "packen"
         g.dispose();
         // Und das Bild zeigen.
         bs.show();
     }
     
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         
         Moebelplaner planer = new Moebelplaner(true);
         
         // Sonst wird es schwieriger mit den Graphiken
         planer.frame.setResizable(false);
         
         // Den Haupt-Titel (oben) setzten
         planer.frame.setTitle(Moebelplaner.TITLE);
         
         // Den canvas in den JFrame "packen"
         planer.frame.add(planer);
         
         // Den JFrame vorbereiten
         planer.frame.pack();
         
         // Sonst würde das Programm schwerer zu beenden sein
         planer.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         
         // Einfach in der Mitter erstellen
         planer.frame.setLocationRelativeTo(null);
         
         // Sonst würd' man nichts sehen
         planer.frame.setVisible(true);
         
         // und jetzt starten wir das rendern & updaten
         planer.start();
         
     }
 }
