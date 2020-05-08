 package GUIComponents;
 
 import Background.Background;
 import Controls.Controls;
 import Game.Game;
 import Game.Network.GameClient;
 import Game.Network.GameServer;
 import Game.Network.GameState;
 import Game.PlayerDeathException;
 import Path.StraightPath;
 import Projectile.ComplexProjectile;
 import Projectile.Projectile;
 import Spawn.Spawn;
 import Unit.*;
 import Unit.Player;
 import Weapon.LaserWeapon;
 import Weapon.ProtonWeapon;
 import java.awt.Graphics;
 import java.awt.Color;
 import java.awt.Cursor;
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.RenderingHints;
 import java.awt.Shape;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.geom.Ellipse2D;
 import java.awt.geom.Rectangle2D;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import javax.swing.ImageIcon;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.Timer;
 
 /**
  *
  * @author Jere
  */
 public class GamePanel extends JPanel {
 
     /*
      *  The panel needs to have somewhere to get data from, and logically this is
      * the game that is currently taking place. Perhaps have this as a constructor
      * parameter, or have it passed in using another method.
      */
     Game gameLogic;
     Color bgColor = Color.BLACK;
     Player one;
     Spawn sp;
     LaserWeapon laser;
     ProtonWeapon proton;
     int player1_x = 500;
     int player1_y = 500;
     Controls a;
     Boolean mouse; //if mouse is being used or not
     int width;
     int height;
     Timer timer;
     ArrayList<Unit> spawns;
     int counter;
     Background background;
     int damage;
     int speed;
     Shape shape;
     Color color;
     boolean running;
     boolean switchPanel = false;
     boolean playerDeath = false;
     boolean paused = false;
     GameState currentState;
     GameServer gameServer;
     GameClient gameClient;
     boolean networked = false;
     public Map imageMap;
     boolean playerAdded = false;
     int thisPlayerRef;
 
     public GamePanel(int width, int height) {
         this.width = width;
         this.height = height;
     }
 
     public GamePanel(int width, int height, int port, int maxConnections) {
         this(width, height);
         gameServer = new GameServer(port, maxConnections);
         gameClient = null;
         networked = true;
     }
 
     public GamePanel(int width, int height, String host, int port) {
         this(width, height);
         gameClient = new GameClient(host, port);
         gameClient.connectToServer();
         gameServer = null;
         networked = true;
     }
 
     /**
      * Draws all objects that need to be drawn.
      * @param g
      */
     @Override
     protected void paintComponent(Graphics g) {
         counter++;
         counter = counter % 400;
         Graphics2D g2 = (Graphics2D) g;
         g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                 RenderingHints.VALUE_ANTIALIAS_ON);
         g2.setColor(Color.blue);
 //        g2.draw(new Line2D.Double(30, 30, 500, 500));
 //        try{
 //        shootGame.getProjectileArray().get(shootGame.getProjectileArray().size()-1).draw(g2);
 //        }catch(Exception e){
 //        }
         render(g2);
     }
 
     /**
      * Tries to get this panel to regain focus.
      */
     public void regainFocus() {
         this.setFocusable(true);
         this.requestFocusInWindow();
     }
 
     public void initImageMap() {
 //        try {
 //
         imageMap = new HashMap();
 
 //            Image i = Toolkit.getDefaultToolkit().getImage(getClass().getResource(".//src//Unit//player.png"));
 //            
         imageMap.put("player", Toolkit.getDefaultToolkit().getImage(getClass().getResource("/Unit/player.png")));
         imageMap.put("enemy", Toolkit.getDefaultToolkit().getImage(getClass().getResource("/Unit/enemy.png")));
         imageMap.put("proton", Toolkit.getDefaultToolkit().getImage(getClass().getResource("/Weapon/proton.png")));
         imageMap.put("laser", Toolkit.getDefaultToolkit().getImage(getClass().getResource("/Weapon/laser.png")));
 
 //            imageMap.put("player", ImageIO.read(new File(".//src//Unit//player.png")));
 //            imageMap.put("enemy", ImageIO.read(new File(".//src//Unit//enemy.png")));
 //            imageMap.put("proton", ImageIO.read(new File(".//src//Weapon//proton.png")));
 //            imageMap.put("laser", ImageIO.read(new File(".//src//Weapon//laser.png")));
 //        } catch (IOException ex) {
 //            System.out.println("Error while reading in image data.");
 //            ex.printStackTrace();
 //        }
     }
 
     // Initialization
     public void initialize() {
         sp = new Spawn();
         spawns = new ArrayList();
         laser = new LaserWeapon();
         proton = new ProtonWeapon();
         gameLogic = new Game();
         background = new Background(40);
         one = new Player(300, 200, laser, 200, player1_x, player1_y, Color.WHITE);
         laser.setPlayerRef(one.getObjectReference());
         thisPlayerRef = one.getObjectReference();
         a = new Controls();
         initImageMap();
         setBackground(bgColor);
         gameLogic.pruneArrays(new Dimension(width, height));
         // height = this.getSize().height;
         // width = this.getSize().width;
         one.setLocation(new Point(player1_x, player1_y));
         mouse = a.isMouse();
         gameLogic.addPlayer(one);
 //        /*
 //         * TODO: Implement automatic calling of spawn classes
 //         * Below code generates 5 enemies at random position.
 //         */
 //        int type = 1;
 //        assert (type >= 0 && type <= 2);
 //        spawns = sp.spawnN(5, type);
 //        for (int i = 0; i < spawns.size(); i++) {
 //            shootGame.addUnitToArray(spawns.get(i));
 //        }
         setFocusable(true);
         //requestFocus();
         addMouseListener(a);
         addKeyListener(a);
         addMouseMotionListener(a);
         hideMouse();
         running = true;
         timer = new Timer(10, new ActionListener() { //60 fps
 
             public void actionPerformed(ActionEvent e) {
                 checkUserMovement();
                 if (gameServer != null && gameServer.getNumConnections() != 0) {
 
                     currentState = gameServer.processGameStates();
                     System.out.println(currentState);
                     if (currentState != null) {
 //                        System.out.println("Setting the current game state to the amalgamated state.");
                         gameLogic.setGameState(currentState);
                         if (!playerAdded) {
                             System.out.println("adding player");
                             gameLogic.addPlayer(one);
                             playerAdded = true;
                         }
 //                        System.out.println("***********CURRENT SERVER STATE*************");
 //                        System.out.println(currentState);
 //                        System.out.println("**********************************");
                     }
                     logic();
 //                    System.out.println("(((((((((((((((logic performed)))))))))))))))))))");
 //                    System.out.println(gameLogic.getGameState(playerDeath, paused, running));
 //                    System.out.println("(((((((((((((((((((()))))))))))))))))))))))");
                     gameServer.broadcastGameState(gameLogic.getGameState(playerDeath, paused, running));
 //                    System.out.println("Game state sent to clients.");
                     repaint();
                 } else if (gameClient != null && gameClient.isConnected()) {
 //                    System.out.println("&&&&&&&&&&&&&&&current state&&&&&&&&&&&&&&");
 //                    System.out.println(currentState);
 //                    System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&&");
 //                    System.out.println("Updating state to server state ");
 //                    System.out.println("objref client" + one.objectReference);
                     if (gameClient.getPlayerNumber() == 0) {
                         gameClient.setPlayerNumber(one.objectReference);
                     }
                     currentState = gameClient.getCurrentServerState();
 
                     if (currentState != null) {
                         GameState temp = currentState;
                         gameLogic.setGameState(temp);
 //                        System.out.println("~~~~~~~~~~~Server updated state~~~~~~~~~~");
 //                        System.out.println(currentState);
 //                        System.out.println("~~~~~~~~~~~~~~~~~~~~~~");
                         gameLogic.addPlayer(one);
                     }
                     logic();
                     repaint();
                     gameClient.sendGameState(currentState);
                 } else {
 //                    System.out.println("normal");
                     logic();
                     repaint();
                 }
             }
         });
         timer.start();
     }
 
     // Logic methods
     private void logic() {
         // FIXME would be nice to put this somewhere else, but don't know where.
 
         if (running) {
             mouse = a.isMouse();
             gameLogic.pruneArrays(new Dimension(this.getSize()));
             gameLogic.moveEnemies();
             gameLogic.moveProjectiles();
             try {
                 gameLogic.doNaiveCollisionDetection();
             } catch (PlayerDeathException ex) {
                 playerDeath = true;
                 setRun(false);
                 JOptionPane.showMessageDialog(this, "You died...");
             }
             background.tick();
         }
     }
 
     // Rendering methods
     private void render(Graphics2D g2) {
 
         if (playerDeath) {
             super.paintComponent(g2);
             g2.setColor(Color.BLACK);
             g2.fill(new Rectangle2D.Double(0, 0, 800, 800));
             g2.setColor(Color.WHITE);
             g2.drawString("GAME OVER!", 350, 275);
         } else {
             if (running) {
                 super.paintComponent(g2);
 //        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, // Anti-aliasing
 //                RenderingHints.VALUE_ANTIALIAS_ON);
 //        for (Projectile p : shootGame.getProjectileArray()) {
 //            p.draw(g2);
 ////            System.out.println("x= " + p.getX());
 ////            System.out.println("y= " + p.getY());
 //            p.doMove();
 //
 //        }
                 background.draw(g2);
                 drawHealthBar(g2);
                 drawScore(g2);
                 drawShips(g2);
                 drawProjectiles(g2);
             }
         }
     }
 
    public void drawScore(Graphics2D g2) {
         g2.setColor(Color.PINK);
         g2.drawString(String.format("Score: %d", one.getScore()), 600, 550);
         g2.setColor(Color.black);
     }
 
     private void drawShips(Graphics2D g2) {
         one.setLocation(new Point(player1_x, player1_y));
         ArrayList<Enemy> enemies = gameLogic.getEnemyArray();
         ArrayList<Player> players = gameLogic.getPlayerArray();
         for (Player player : players) {
             player.draw(g2, imageMap);
             g2.setColor(Color.red);
             g2.draw(gameLogic.getCenteredBox(player.getLocation()));
             g2.setColor(Color.black);
         }
         for (Enemy enemy : enemies) {
             enemy.draw(g2, imageMap);
             g2.setColor(Color.red);
             g2.draw(gameLogic.getCenteredBox(enemy.getLocation()));
             g2.setColor(Color.black);
         }
 
 //        System.out.println(counter);
         if (counter == 100 | counter == 200 || counter == 300 || counter == 399) { // will spawn a wave as soon as the game starts
                     /*
              * TODO: Implement automatic calling of spawn classes
              * Below code generates 5 enemies at random position.
              */
 
             spawns = sp.spawnRandom(6);
             for (int i = 0; i < spawns.size(); i++) {
                 // FIXME this needs to be done better, although you shouldn't be spawning players in these spawns.
                 gameLogic.addEnemy((Enemy) spawns.get(i));
             }
             //shootGame.getUnitArray().get(shootGame.getUnitArrayLength() - 1).draw(g2);
         }
     }
 
     private void drawProjectiles(Graphics2D g2) {
 
         for (Enemy enemy : gameLogic.getEnemyArray()) {
             if ((counter % enemy.getWeapon().getFireRate()) == 0) {
                 gameLogic.addManyProjectiles(enemy.getWeapon().fire(enemy.getX(), enemy.getY()));
             }
         }
 //            for (int i = 1; i < gameLogic.getEnemyArray().size() - 1; i++) {
 //                shape = new Ellipse2D.Double(gameLogic.getEnemyArray().get(i).getX(), gameLogic.getEnemyArray().get(i).getY() - 15, 5, 5);
 //                gameLogic.addProjectileToArray(new ComplexProjectile(gameLogic.getEnemyArray().get(i).getX(),
 //                        gameLogic.getEnemyArray().get(i).getY() - 15,
 //                        5, speed, true, shape, color, new StraightPath(StraightPath.Direction.UP), gameLogic.getEnemyArray().get(i).getWeapon().getProjectile().getProjectileType()));
 //            }
         g2.setColor(Color.BLUE);
         ArrayList<Projectile> projectiles = gameLogic.getProjectileArray();
         for (Projectile projectile : projectiles) {
             g2.setColor(Color.blue);
             g2.draw(gameLogic.getCenteredBoxProjectile(projectile.getLocation()));
             g2.setColor(Color.black);
             projectile.draw(g2, imageMap);
         }
 
 //        try{
 //        shootGame.getProjectileArray().get(shootGame.getProjectileArray().size()-1).draw(g2);
 //        }catch(Exception e){
 //
 //        }
 //        for (Projectile p : shootGame.getProjectileArray()) {
 //            p.draw(g2);
 //            p.doMove();
 //
 //        }
     }
 
     /**
      * used to control the movement
      */
     private void checkUserMovement() {
        if (!this.isFocusOwner()) {
            this.regainFocus();
        }
         if (!paused && running) {
             if (mouse) {
                 player1_x = a.getMouseX();
                 player1_y = a.getMouseY();
             } else {
                 if (a.isUp() && player1_y > 0) {
                     player1_y -= 5;
                 }
                 if (a.isDown() && player1_y < height - 42) { // -32 so can still see some of unit
                     player1_y += 5;
                 }
                 if (a.isLeft() && player1_x > 0) {
                     player1_x -= 5;
                 }
                 if (a.isRight() && player1_x < width - 36) { // -16 so can still see some of unit
                     player1_x += 5;
                 }
             }
             if (a.isSpace()) {
                 //testPro = new BasicProjectile(one.getX(), one.getY());
                 //shape = new Ellipse2D.Double(one.getX(), one.getY(), 5, 5);
 //                shape = new Rectangle2D.Double(one.getX(), one.getY(), 5, 5);
                 // shape = new Line2D.Double(one.getX(), one.getY(), one.getX(), one.getY()+10);
 //                gameLogic.addProjectileToArray(new ComplexProjectile(one.getX(), one.getY() - 15,
 //                        100, speed, false, shape, color, new StraightPath(StraightPath.Direction.DOWN),
 //                        one.getWeapon().getProjectile().getProjectileType()));
                 gameLogic.addManyProjectiles(one.getWeapon().fire(player1_x, player1_y));
             }
             if (a.isEsc()) {
                 System.out.println("escape");
                 switchPanel = true;
                 a.setEscFalse();
             }
             if (a.isPause()) {
                 setRun(false);
                 paused = true;
             }
         } else {
             if (!a.isPause()) {
                 paused = false;
                 setRun(true);
             }
         }
     }
 
     private void hideMouse() {
         ImageIcon invisi = new ImageIcon(new byte[0]);
         Cursor invisible = getToolkit().createCustomCursor(
                 invisi.getImage(), new Point(0, 0), "Hiding");
         this.setCursor(invisible);
     }
 
     private void drawHealthBar(Graphics2D g2) {
         Rectangle2D healthBar = new Rectangle2D.Double(35, 525, one.getHealth(), 20);
         g2.setColor(Color.GREEN);
         g2.fill(healthBar);
     }
 
     /**
      * Method to change the variable run that controls rendering.
      * @param run
      */
     public void setRun(boolean run) {
 //        if (run) {
 //            if (!timer.isRunning()) {
 //                timer.start();
 //            }
 //        } else {
 //            if (timer.isRunning()) {
 //                timer.stop();
 //            }
 //        }
         this.running = run;
     }
 
     /**
      * Checks if the switchpanel variable is true. this indicates that the user
      * has pressed escape and so the frame should switch to the menu. Also stops the
      * game from running.
      * @return
      */
     public boolean getPanelSwitchRequest() {
         boolean req = switchPanel;
         if (req == true) {
             setRun(false);
             switchPanel = false;
             return true;
         }
         return false;
     }
 
     public boolean isPaused() {
         return paused;
     }
 }
