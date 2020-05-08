 /**********************************************************************/
 /* Copyright 2013 KRV                                                 */
 /*                                                                    */
 /* Licensed under the Apache License, Version 2.0 (the "License");    */
 /* you may not use this file except in compliance with the License.   */
 /* You may obtain a copy of the License at                            */
 /*                                                                    */
 /*  http://www.apache.org/licenses/LICENSE-2.0                        */
 /*                                                                    */
 /* Unless required by applicable law or agreed to in writing,         */
 /* software distributed under the License is distributed on an        */
 /* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,       */
 /* either express or implied.                                         */
 /* See the License for the specific language governing permissions    */
 /* and limitations under the License.                                 */
 /**********************************************************************/
 package ui.graphical;
 
 // Default Libraries
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.WeakHashMap;
 
 // Graphical Libraries (AWT)
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.TexturePaint;
 import java.awt.image.BufferedImage;
 
 // Graphical Libraries (Swing)
 import javax.swing.*;
 import javax.swing.border.*;
 import javax.imageio.ImageIO;
 import javax.swing.SwingUtilities;
 
 // Libraries
 import arena.Map;
 import parameters.*;
 import arena.Robot;
 import players.Player;
 import scenario.Scenario;
 import stackable.Direction;
 import stackable.item.Item;
 
 // Import links
 import static parameters.Game.*;
 
 /**
  * <b>Graphical - Panel</b><br>
  * Creates the main panel to exhibit 
  * the map.
  * @see Graphical
  * @see arena.Map
  * @see arena.World
  * 
  * @author Renato Cordeiro Ferreira
  * @author Vinicius Silva
  */
 class Panel extends JLayeredPane
 {
     // Map made with cells
     private Cell[][] cell = new Cell[MAP_SIZE][MAP_SIZE];
     
     // Local variables
     private Map       map;
     private Player    player;
     private Graphical gui;
     
     // Paint utils
     private int[] update  = new int[2];
     private Direction dir;
     private WeakHashMap<Robot,JRobot> robots 
         = new WeakHashMap<Robot,JRobot>();
     
     // Paint auxiliars
     private Phase phase;
     private int   nTS;
     private int   nPlayers;
     private int   nRobots;
     
     // Show or not scenarios/items
     boolean hide = false;
     
     /**
      * Create a Panel with dimensions width x height,
      * containing MAP_SIZE² hexagons (built from a map).
      * @see Cell
      * @see Graphical
      *
      * @param gui    Graphical set in which the 
      *               Panel is set
      * @param map    Map over which the panel will
      *               create the GUI hexagons
      * @param player Player who is visualizing the
      *               panel (for his specific view)
      * @param R      Hexagon radius
      * @param y0     Desired vertical shift
      * @param width  Desired width of the screen
      * @param height Desired height of the screen
      */
     Panel(Graphical gui, Map map, Player player, 
           int R, int x0, int y0, int width, int height, boolean hide)
     {
         // Store game attributes
         this.gui      = gui;
         this.map      = map;
         this.player   = player;
         
         // Initializes auxiliar variable
         try { this.dir = new Direction(""); }
         catch(Exception e)
         {
             System.err.println("[PANEL] This sould never happen...");
             e.printStackTrace();
         }
         
         // Preferences
         this.setOpaque        (true);
         this.setLayout        (null);
         this.setBackground    (Color.black);
         this.setPreferredSize (new Dimension(width, height));
         
         // Initial phase: active
         this.phase = Phase.ACTIVE;
         
         // Put images in the screen
         int Dx = (int) ( 2*R * Math.sin(Math.PI/3) ); 
         int Dy = 3 * R/2 +1;
        
         // Build a grid of cells
         int Δ  = 0;
         for (int i = 0; i < MAP_SIZE; i++)
             for (int j = 0; j < MAP_SIZE; j++) 
             {
                 cell[i][j] = new Cell(
                     this.player,       // Player
                     x0 + Δ + R + i*Dx, // Horizontal position
                     y0 + R + j*Dy,     // Vertical position
                     R,                 // Hexagon radius
                     map.map[j][i]      // Terrain in map[j,i]
                 ); 
                 Δ = (Δ == 0) ? Dx/2 : 0;
                 
                 // Add statical events
                 if(!hide) this.add(cell[i][j].invs, Level.FOG.get());
                 if(!hide) this.add(cell[i][j].hit,  Level.BATTLE.get());
             }
         
         // Visibility around player's bases
         int X = this.player.getBase().getPosX(this.player);
         int Y = this.player.getBase().getPosY(this.player);
         this.setVisible(X,Y,3);
     }
     
     /**
      * Paint hexagons on the screen.<br>
      * At each step, repaint the cells
      * as they need changes.
      * @param g Graphic object with all context 
      *          needed to render the image
      */
     protected void paintComponent(Graphics g) 
     {
         super.paintComponent(g);
         
         // First, update player's visibility
         this.blinkVisibility();
         
         // Then, draw all the background without Fog War
         this.paintMap(g);
         
         // Remove all GUI elements, mainly the robots power/HP bars
         for(java.util.Map.Entry<Robot, JRobot> entry : robots.entrySet())
             entry.getValue().remove(); 
         
         // Finally, print accordingly the state
         switch(this.phase)
         {
             case LOOSER: if(!hide) looser(g);    break;
             case WINNER: if(!hide) winner(g);    break;
             case ACTIVE: if(!hide) paintLife(g); break;
         }
     }
     
     /**
      * Set if the scenarios and items
      * should or not be showed.
      * @param show True if show invisible mask and 
      *             scenarios, false otherwise
      */
     void hide(boolean show)
     {
         hide = show;
     }
     
     /**
      * Load info about the game phase.<br>
      * @param phase    Game phase
      * @param nTS      Number of time steps since 
      *                 the beggining of the game
      * @param nPlayers Number of players
      * @param nRobots  Number of robots created by 
      *                 all players along the game
      */
     void setGamePhase(
         Phase phase, int nTS, int nPlayers, int nRobots)
     {
         this.nTS      = nTS;
         this.phase    = phase;
         this.nRobots  = nRobots;
         this.nPlayers = nPlayers;
     }
     
     /**
      * Auxiliar function for painting the 
      * hexagonal grid arena of the game.
      * @param g Game graphical context
      */
     private void paintMap(Graphics g)
     {
         for (int i = 0; i < MAP_SIZE; i++) 
             for (int j = 0; j < MAP_SIZE; j++)
                 cell[i][j].draw(g); 
     }
     
     /**
      * Auxiliar function for painting all
      * the elements over the arena.
      * @param g Game graphical context
      */
     private void paintLife(Graphics g)
     {
         for (int i = 0; i < MAP_SIZE; i++) 
             for (int j = 0; j < MAP_SIZE; j++)
             {
                 // Get fog war (and print properly)
                 boolean fog = cell[i][j].terrain.getFogWar(this.player);
                 cell[i][j].hit.setVisible(false);
                 
                 // Print items and scenarios if there is no fog
                 if(fog) continue;
                 
                 item(i, j); // Items (crystals, stones...)
                 scen(i, j); // Scenarios (robots, trees, rocks...)
                 
                 // Invisibility
                 boolean vis = cell[i][j].terrain.getVisibility(this.player);
                 if(!vis) cell[i][j].invs.setVisible(true);
                 else cell[i][j].invs.setVisible(false);
             }
     }
     
     /**
      * Make all terrains invisible and, then, set the visibility
      * for this new step (around player's stuff).
      */
     private void blinkVisibility()
     {
         // Hide all the map
         for(int i = 0; i < MAP_SIZE; i++)
             for(int j = 0; j < MAP_SIZE; j++)
                 cell[i][j].terrain.setInvisible(this.player);
         
         // But let the base's around be visible
         int X = this.player.getBase().getPosX(this.player);
         int Y = this.player.getBase().getPosY(this.player);
        this.setVisible(Y,X,7);
         
         // And the player's robots
         for(Robot r: this.player.armies)
             this.setVisible(r.getPosX(), r.getPosY(), r.getSight() * 2);
     }
     
     /**
      * Auxiliar method for making an area visible
      * (which takes out Fog War and shows all scenarios).
      * @param X Horizontal position
      * @param Y Vertical position
      * @param S Sight (radius) of the area to be set visible
      */
     private void setVisible(int X, int Y, int S)
     {
         if(X < 0 || X >= MAP_SIZE) return;
         if(Y < 0 || Y >= MAP_SIZE) return;
         
         cell[X][Y].terrain.setVisible(this.player);
         
         if(S == 0) return;
             
         try {
             for(int k = 1; k <= 6; k++)
             {
                 dir.set(k); update = dir.get(Y);
                 if(X + update[1] < 0 || X + update[1] >= MAP_SIZE) continue;
                 if(Y + update[0] < 0 || Y + update[0] >= MAP_SIZE) continue;
                 setVisible(X + update[1], Y + update[0], S-1);
             }
             
         } catch(Exception e) {
             System.err.println("[PANEL] (setVisible) This sould never happen...");
             e.printStackTrace();
         }
     }
     
     /**
      * Auxiliar function for painting a 
      * scenario over a terrain in the game.
      * @param i Vertical position of the scenario
      * @param j Horizontal position of the scenario
      */
     private void scen(int i, int j)
     {
         Cell hex = cell[i][j];
         int x = hex.x, y = hex.y;
         boolean vis = hex.terrain.getVisibility(this.player);
         
         if(!vis) return;
         
         Scenario s = hex.terrain.getScenario();
         if(s != null)
         {
             // Special treatment for robots
             if(s instanceof Robot)
             {
                 // Get the robot and its animation phase
                 Robot r = (Robot) s;
                 
                 // Add new JRobots for new Robots
                 if(!this.robots.containsKey(r))
                     this.robots.put(r, new JRobot(r));
                 
                 // Add JRobot in the Panel
                 robots.get(r).add(x, y);
             }
             else if(hex.scen == null) // && s is not robot
             {
                 hex.insertScenario();
                 this.add(hex.scen, Level.NATURE.get());
                 this.moveToFront(hex.scen);
             }
             
             // When something is being hit, show it!
             if(s.sufferedDamage()) hex.hit.setVisible(true);
             
         } // s != null
         else if(hex.scen != null) // && s == null
         {
             this.remove(hex.scen);
             hex.scen = null;
         }
     }
     
     /**
      * Auxiliar function for painting an 
      * item over a terrain in the game.
      * @param g Game graphical context
      * @param i Vertical position of the item
      * @param j Horizontal position of the item
      */
     private void item(int i, int j)
     {
         Cell hex = cell[i][j];
         
         // Print items
         if(hex.item != null && hex.terrain.getItem() == null)
         {
             this.remove(hex.item);
             hex.item = null;
         }
         if(hex.terrain.getItem() != null && hex.item == null)
         {
             hex.insertItem();
             this.add(hex.item, Level.ITEM.get());
         }
     }
     
     /**
      * Paints message to the looser user.
      * @param g Game graphical context
      */
     private void looser(Graphics g)
     {
         // Useful variables
         int H = this.getHeight(), W = this.getWidth();
         int hH = H/2, hW = W/2; // Half Height/Width
         
         // Paint background
         this.paintMap(g);
         
         // Painting strip
         g.setColor(Color.WHITE);
         g.fillRect(65, hH-60, W-130, 190);
         
         // Painting label
         g.setColor(Color.RED);
         g.setFont(new Font("Arial Black", Font.BOLD, 50));
         g.drawString(this.player + ", YOU LOOSE!", hW-300, hH + 35);
         
         // Paint all the label
         paintEdge(g);
     }
     
     /**
      * Paints message to the winner user.
      * @param g Game graphical context
      */
     private void winner(Graphics g)
     {
         // Useful variables
         int H = this.getHeight(), W = this.getWidth();
         int hH = H/2, hW = W/2; // Half Height/Width
         
         // Paint background
         this.paintMap(g);
         
         // Painting the strip
         g.setColor(Color.WHITE);
         g.fillRect(65, hH-60, W-130, 190);
         
         // Painting the label
         g.setColor(Color.BLUE);
         g.setFont(new Font("Arial Black", Font.BOLD, 50));
         g.drawString(this.player + ", YOU WIN!", hW-280, hH-15);
         
         // Painting game's info
         g.setColor(Color.GREEN);
         g.setFont(new Font("Arial Black", Font.BOLD, 30));
         g.drawString("Number of Steps: "   + this.nTS,      hW-280, hH+35);
         g.drawString("Number of Players: " + this.nPlayers, hW-280, hH+66);
         g.drawString("Number of Robots: "  + this.nRobots,  hW-280, hH+97);
         
         // Paint all the label
         paintEdge(g);
     }
     
     /**
      * Auxiliar method for printing a border around 
      * both winner and looser player messages.
      * @param g Game graphical context
      */
     private void paintEdge(Graphics g)
     {
         // Useful variables
         int H = this.getHeight(), W = this.getWidth();
         int hH = H/2, hW = W/2; // Half Height/Width
         
         // Painting the border
         Graphics2D g2d = (Graphics2D) g;
         
         // Draw red robots
         Image red = Images.RED_ROBOT.img().getSubimage(0, 32, 32, 32);
         
         g2d.drawImage(red,   28,     hH - 65,  null);
         g2d.drawImage(red,   28,     hH + 5 ,  null);
         g2d.drawImage(red,   28,     hH + 75,  null);
         
         g2d.drawImage(red,   W - 60, hH - 65,  null);
         g2d.drawImage(red,   W - 60, hH + 5 ,  null);
         g2d.drawImage(red,   W - 60, hH + 75,  null);
         g2d.drawImage(red,   W - 60, hH + 145, null);
             
         // Draw black robots
         Image black = Images.BLACK_ROBOT.img().getSubimage(0, 64, 32, 32);
             
         g2d.drawImage(black, 28,     hH - 30,  null);
         g2d.drawImage(black, 28,     hH + 40,  null);
         g2d.drawImage(black, 28,     hH + 110, null);
         
         g2d.drawImage(black, W - 60, hH - 100, null);
         g2d.drawImage(black, W - 60, hH - 30,  null);
         g2d.drawImage(black, W - 60, hH + 40,  null);
         g2d.drawImage(black, W - 60, hH + 110, null);
         
         // Draw both robots
         for(int i = 28; i < (W-93); i += 35)
         {
             g2d.drawImage(black, i, hH - 100, null);
             g2d.drawImage(red  , i, hH + 145, null);  
         }
     }
     
     /**
      * <b>Panel - Level</b>
      * Auxiliar enum for the levels
      * in which the elements will be 
      * displayed inside the Panel.
      */
     enum Level
     {
         FOG    (7),
         LABEL  (6),
         BATTLE (5),
         NATURE (4),
         TECH   (3),
         ITEM   (2),
         BAR    (1);
         
         // Integer representing the level
         private Integer level;
         
         /**
          * Default contructor.<br>
          * @param l Level
          */
         private Level(int l) { this.level = new Integer(l); }
         
         /**
          * Getter for the level.
          * @return Integer with the level of the element
          */
         Integer get() { return this.level; }
     }
     
     /**
      * <b>Panel - JRobot</b><br>
      * Print a robot exhibiting a 
      * status bar and other useful 
      * info for the player.
      */
     private class JRobot
     {
         // Configurations
         private Dimension size = new Dimension(30,5); // Bar size
         
         // Parameters
         private Robot robot;
         
         // Graphical context
         private Images scen;
         
         // Position info
         private int x, y, x0, y0;
         
         // Additional info for settings
         private int maxHP;
         private int maxPower;
         
         // Graphical Components
         private JLabel name;
         private JLabel jrobot;
         private JProgressBar hp;
         private JProgressBar power;
         
         private boolean exhibitName = false;
         
         /**
          * Default Constructor.
          * @param robot Robot to be stored
          */
         JRobot(Robot robot)
         {
             // Stores parameters in attributes
             this.robot = robot;
             this.scen  = Images.valueOf(
                 this.robot.name(), this.robot.getTeam()
             );
             
             // Creates and sets its image
             this.jrobot = new JLabel();
             
             // Creates label to the name
             this.name = new JLabel(this.robot.toString());
             this.name.setOpaque(true);
             this.name.setForeground(Color.WHITE);
             this.name.setBackground(Color.BLACK);
             
             this.jrobot.addMouseListener(new MouseAdapter() 
             {
                 @Override
                 public void mouseEntered(MouseEvent e) {
                     JRobot.this.exhibitName = true;
                 }
                 @Override
                 public void mouseExited(MouseEvent e) {
                     JRobot.this.exhibitName = false;
                 }
                 @Override
                 public void mouseClicked(MouseEvent e){
                     if(e.getClickCount() >= 2)
                     {
                         String path = JRobot.this.robot.getPathToProg();
                         Panel.this.gui.editorFrame.loadFile   (JRobot.this.robot, path);
                         Panel.this.gui.editorFrame.setVisible (true);
                     }
                 }
             });
             
             // Creates and sets HP bar
             this.maxHP = robot.getMaxHP       ();
             this.hp = new JProgressBar        ();
             this.hp.setMaximum                (this.maxHP    );
             this.hp.setPreferredSize          (this.size     );
             this.hp.setForeground             (Color.BLUE    );
             
             // Creates and sets Power bar
             this.maxPower = robot.getMaxPower ();
             this.power = new JProgressBar     ();
             this.power.setMaximum             (this.maxPower );
             this.power.setPreferredSize       (this.size     );
             this.hp.setForeground             (Color.GREEN   );
         }
         
         /**
          * Add bars in the Panel.<br>
          */
         protected void add(int x, int y)
         {
             // Original positions
             this.x = x; this.y = y;
             
             // Updated positions
             this.x0 = x-this.scen.dx(); 
             this.y0 = y-this.scen.dy();
             
             // Painting
             this.paintRobot();
             this.paintBars();
             this.paintName();
         }
         
         /**
          * Remove bars from the Panel.<br>
          */
         protected void remove()
         {
             Panel.this.remove(this.hp);
             Panel.this.remove(this.name);
             Panel.this.remove(this.power);
             Panel.this.remove(this.jrobot);
         }
         
         /**
          * Paint label with robot name 
          * in its new position.
          */
         private void paintName()
         {
             if(!this.exhibitName) return;
             Dimension d = this.name.getPreferredSize();
             int W = (int) d.getWidth(), H = (int) d.getHeight();
             // TODO: Take out hardcoded constants
             this.name.setBounds   (x-W/2, y+20, W, H);
             this.name.setLabelFor (this.jrobot);
             Panel.this.add        (this.name, Level.LABEL.get());
         }
             
         /**
          * Paint robot in its new position.
          */
         private void paintRobot()
         {
             // Set new JRobot info
             int[] phase = this.robot.getPhase();
             
             BufferedImage img = this.scen.img().getSubimage(phase[0], phase[1], 32, 32);
             ImageIcon ico = new ImageIcon(img);
             this.jrobot.setIcon   (ico);
             this.jrobot.setBounds (x0, y0, 32, 32);
             Panel.this.add        (jrobot, Level.TECH.get());
             Panel.this.moveToFront(jrobot);
             
             // Update robot's position
             this.robot.setPhase(32, phase[1]);
         }
         
         /**
          * Update bars with infos exhibited by a robot.
          */
         private void paintBars()
         {
             int hp    = this.robot.getHP    ();
             int power = this.robot.getPower ();
             
             // Update Color Scheme
             this.updateColorScheme(this.hp, this.maxHP);
             
             try  {
                 // Configure and paint hp bar
                 this.hp.setBounds    (x0, y0-15, size.width, size.height);
                 this.hp.setValue     (hp);
                 
                 // Configure and paint power bar
                 this.power.setBounds (x0, y0-10, size.width, size.height);
                 this.power.setValue  (power);
             
             } catch(java.lang.IllegalArgumentException e) {
                 // TODO: Nothing to do - the program still works
             }
             
             // Add HP and Power bars
             Panel.this.add(this.hp,    Level.BAR.get());
             Panel.this.add(this.power, Level.BAR.get());
         }
         
         /**
          * Updates the color of a given bar.<br>
          * If greater than 2/3, paint in GREEN. If lower
          * than 1/3, paint RED. Otherwise, use YELLOW.
          * @param pb  JProgressBar
          * @param max Maximum value to be used as parameter
          *            to the tresholds of each color region
          */
         private void updateColorScheme(JProgressBar pb, int max)
         {
             double per = max * pb.getPercentComplete();
             Color c = Color.YELLOW;
             if(per > 2.0/3 * max) c = Color.GREEN;
             if(per < 1.0/3 * max) c = Color.RED;
             pb.setForeground(c);
         }
     }
 }
