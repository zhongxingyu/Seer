 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Toolkit;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import javax.imageio.ImageIO;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 
 
 public class Game extends JComponent implements Runnable, MouseListener, MouseMotionListener
 {
     Thread animThread;
     int mouseX, mouseY;
     int attack = 25;
     boolean enabled = true;
     boolean fleet = false;
     BufferedImage bufferedImage;
     int draw = 0;
 
     private GamePiece selected;         // `null` if no GamePiece has been selected
     private GamePiece hoverOn;      // `null` if the mouse is hovering over no GamePieces
 
     private Color selectedColor = Color.BLUE;
     private Color hoverOnColor = Color.RED;
 
     private Universe universe;
 
 
     /**
      * Run a game with 2 players
      */
     public Game()
     {
         this(2);
     }
 
     public Game(int numberOfPlayers)
     {
         Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 
         setOpaque(true);
         setPreferredSize(screenSize);
         addMouseListener(this);
         addMouseMotionListener(this);
 
         // create a universe with 12 planets and a minimum separation of 50 units
         this.universe = new Universe(screenSize.width, screenSize.height, 12, 50);
 
         // add a human player, then computer players.
         this.universe.addPlayer(new Player(1));
         for (int i=1; i < numberOfPlayers; i++) {
             this.universe.addPlayer(new ComputerPlayer(i+1));
         }
 
         // get the Universe to set up the players with planets, etc.
         this.universe.setUpPlayers();
     }
 
 
 
     public void paintComponent(Graphics g)
     {
         Graphics2D g2 = (Graphics2D) g.create();
 
         // draw the universe background
         g2.drawImage(this.universe.getBackground(), 0, 0, null);
 
         // load a fleet image, which is reused
         // TODO: remove this, as fleets provide their own images
         BufferedImage b;
         try {
             File f = new File("resources/images/fleets/fleet1.png");
             b = ImageIO.read(f);
             bufferedImage=GamePiece.resize(b, 10);
         } catch (IOException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
 
         // set the Swing component to be opaque
         setOpaque(true);
 
         // TODO: write separate methods to draw all planets/fleets
 
         // draw planets
         Planet[] planets = this.universe.getPlanets();
         for (int i=0; i < planets.length; i++) {
             Planet p = planets[i];
             g2.drawImage(p.getImage(), p.getX(), p.getY(), null);
 
             g2.setColor(Color.YELLOW);
             // draw the number of ships on the planet for the controlling player
             g2.drawString("ships: "+ p.getPlayerShips(p.getControllingPlayer()), p.getX(), p.getY());
             // draw the number of Resource Units the planet has 
             g2.drawString("RUs: "+ p.getResourceValue(), p.getX() + 2*p.getRadius(), p.getY());
         }
 
         // draw Fleets
         Fleet[] fleets = this.universe.getFleets();
         for (int i=0; i < fleets.length; i++) {
             Fleet f = fleets[i];
             g2.drawImage(f.getImage(), f.getX(), f.getY(), null);
 
             g2.setColor(Color.YELLOW);
             // draw the number of ships in the fleet
             g2.drawString("ships: "+ f.getShips(), f.getX(), f.getY());
         }
 
         // NOTE: what does this do?
         if(draw == 1) g2.drawOval(200,200,400,400);
 
         // TODO: selection and hovering ought to use GamePiece.getColour() 
         // draw around the selected GamePiece
         if (this.selected != null) {
             g2.setColor(this.selectedColor);
             g2.setStroke(new BasicStroke(5F));
             g2.drawOval(this.selected.getX(), this.selected.getY(), this.selected.getRadius()*2, this.selected.getRadius()*2);
         }
 
         // draw around the hoveredOver GamePiece
         if (this.hoverOn != null) {
             g2.setColor(this.hoverOnColor);
             g2.setStroke(new BasicStroke(5F));
             g2.drawOval(this.hoverOn.getX(), this.hoverOn.getY(), this.hoverOn.getRadius()*2, this.hoverOn.getRadius()*2);
             // TODO: GamePiece.getSelectionRadius() method - public int
         }
 
         // draw lines between the selected GamePiece and hoveredOver GamePiece
         if ((this.selected != null) && (this.hoverOn != null)) {
             g2.setColor(Color.red);
             g2.setStroke(new BasicStroke(3F));
             g2.drawLine(this.selected.getXCenter(), this.selected.getYCenter(), this.hoverOn.getXCenter(), this.hoverOn.getYCenter());
         }
 
         g2.dispose();
     }
 
 
     /*
      * Code to be executed by a thread.
      * 
      * This loop should update the state of everything, then sleep for 10ms.
      */
     public void run()
     {
 
         // TODO: update all the planets
         for (Planet p : this.universe.getPlanets()) {
             p.update();
         }
 
         // TODO: update all the fleets
         for (Fleet f : this.universe.getFleets()) {
             f.update();
         }
 
         // TODO: update all the players
         for (Player p : this.universe.getPlayers()) {
             p.update();
         }
 
 
         // set the process to sleep for 10ms
         try {
             Thread.sleep(10);
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
         // then repaint the window
         repaint();
     }
 
 
     // ******************************************************************
     // METHODS BELOW HERE ARE APPLIED WHEN THE APPROPRIATE ACTIONS OCCUR
     // ******************************************************************
 
     @Override
     public void mouseClicked(MouseEvent e) {
         // TODO Auto-generated method stub
 
     }
 
     /* mousePressed is checking coordinates of mouse whenever it was pressed
      * in this case whether the cursor is inside of either planet, calls appropriate action
      *  
      * Matej
      */
     @Override
     public void mousePressed(MouseEvent e) {
         int clickX = e.getX();
         int clickY = e.getY();
 
         // TODO: if the mouse has clicked on empty space, set selected to `null`
         // ...
 
         // if the mouse has been clicked inside a GamePiece without any other selected, set this GamePiece to be selected
         if (this.selected == null) {
             for (GamePiece g : this.universe.getGamePieces()) {
                 if (g.isClickInside(clickX, clickY)) {
                     this.selected = g;
                     break;
                 }
             }
         }
 
         // if another GamePiece has already been selected, perform the appropriate action
         if (this.selected != null) {
 
             // TODO: if a planet has been selected, and another planet clicked on, show UI to send a fleet
 
             // TODO: figure out what needs to be done for the other cases (Planet->Fleet, Fleet->Whatever)
         }
 
         repaint();
     }
 
     @Override
     public void mouseReleased(MouseEvent e) {
 
 
     }
 
     @Override
     public void mouseEntered(MouseEvent e) {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void mouseExited(MouseEvent e) {
         // TODO Auto-generated method stub
 
     }
 
 
 
     @Override
     public void mouseDragged(MouseEvent e) {
         // TODO Auto-generated method stub
 
     }
 
 
 
     /*
      * If the mouseMoved inside of a GamePiece's clickRadius, set this.hoverOn to be that GamePiece.
      * Otherwise set this.hoverOn to be `null`.
      */
     @Override
     public void mouseMoved(MouseEvent e) {
         mouseX = e.getX();
         mouseY = e.getY();
 
         // TODO: if the mouse is inside a GamePiece, set the GamePiece to be the hoveredOn GamePiece
         for (GamePiece g : this.universe.getGamePieces()) {
             if (g.isClickInside(mouseX, mouseY)) {
                 this.hoverOn = g;
                 repaint();
                 break;
             } else {
                 this.hoverOn = null;
             }
         }
     }
}
