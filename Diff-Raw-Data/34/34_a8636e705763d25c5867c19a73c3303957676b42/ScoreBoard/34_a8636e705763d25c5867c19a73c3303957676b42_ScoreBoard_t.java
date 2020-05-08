 import java.awt.RenderingHints;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.GraphicsDevice;
 import java.awt.GraphicsEnvironment;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.image.BufferStrategy;
 import java.awt.event.KeyListener;
 import javax.swing.event.MouseInputListener;
 import java.awt.Polygon;
 import java.awt.Rectangle;
 import java.awt.Shape;
 import java.awt.geom.Ellipse2D;
 import javax.swing.JFrame;
 import java.awt.BasicStroke;
 import java.awt.Stroke;
 import java.util.*;
 import java.awt.Color;
 import java.util.Date;
 
 final class ScoreBoard implements GameListener {
  
     public enum ScoreBoardState {
         DEFAULT,
         SELECT_PLAYER_TO_EDIT,
         SELECT_PLAYER_TO_DELETE,
         SELECT_PLAYER_TO_ADD,
         EDIT_PLAYER
     }
     private String helpMessage;
     private JFrame frame;
     private Game game;
     private ScoreBoardHelper helper = new ScoreBoardHelper();
     private KeyListener controller;
     private MouseInputListener mouseController;
     private boolean showHelp = false;
     private boolean showColorHelp = true;
     private boolean showMouseControls = false;
     private boolean fullScreen = false;
     private boolean isAnimating = true;
     private Player winner;
     private List<GUIObject> guiObjects;
     private List<HotZone> hotZones;
     private PlayerPainterFactory factory;
  
    
 
     
     // might as well cache these;
     private Font bigFont = new Font( "Helvectica", 0, 64 );
     private Font littleFont = new Font( "Courier", 0, 22 );
 
     public ScoreBoard(Game game) {
         guiObjects = Collections.synchronizedList(new LinkedList<GUIObject>());
         this.factory = new PlayerPainterFactory();
 
         this.game = game;
         for (Player player : game.getLeaderBoard()) {
             guiObjects.add(factory.getPainter(player, game));
         }
         game.addGameListener(this);
         this.helpMessage = helper.getHelpMessage(ScoreBoardState.DEFAULT);
         this.controller = new ScoreBoardKeyListener(game, this);
 
         this.hotZones = Collections.synchronizedList(new LinkedList<HotZone>()); 
         this.mouseController = new ScoreBoardMouseListener(this,game,hotZones);
 
 
 
         toggleFullScreen();
 
 
    }
 
     public void setShowHelp(boolean showHelp) {
         this.showHelp = showHelp;
     }
 
     public boolean getShowHelp() {
         return showHelp;
     }
     
     public void setShowColorHelp(boolean showHelp) {
         this.showColorHelp = showHelp;
     }
 
     public boolean getShowColorHelp() {
         return showColorHelp;
     }
 
     public void setShowMouseControls(boolean showMouseControls) {
         synchronized(guiObjects) {
             for (GUIObject o : guiObjects) {
                 o.setShowMouseAffordances(showMouseControls);
             }
         }
     }
 
     public boolean getShowMouseControls() {
         return showMouseControls;
     }
 
     public void setState(ScoreBoardState state) {
         this.helpMessage = helper.getHelpMessage(state);
         finishEditing();
     }
 
     public void setStateAchievement(Achievement achievement) {
         this.helpMessage = "Enter Color of player who has the " + achievement.toString() + ".";
         finishEditing();
     }
 
     public void setStateEditing(PlayerColor playerColor) {
         this.helpMessage = "Editing " + playerColor.toString() + ".";
 
         PlayerPainter painter = null;
         synchronized(guiObjects) {
             for (GUIObject o : guiObjects) {
                 if (o instanceof PlayerPainter && ((PlayerPainter)o).player.getPlayerColor() ==  playerColor) {
                     painter = (PlayerPainter)o;
                 }
             }
         }
         painter.setEditing(true);
     }
 
     private void finishEditing() {
         synchronized(guiObjects) {
             for (GUIObject o : guiObjects) {
                 if (o instanceof PlayerPainter) {
                     ((PlayerPainter)o).setEditing(false);
                 }
             }
         }
     }
 
     public void renderLoop() {
         while (true) {
             paint();
             // limit framerate if there's no animation currently happening
             try {
             if (!isAnimating)
                 Thread.sleep(100);
             }
             catch (Exception ex) {
                 // Don't really care if I get woken.
             }
         }
     }
     public void tickLoop() {
         while (true) {
             
             int width = frame.getContentPane().getWidth();
             int height = frame.getContentPane().getHeight();
 
 
             long time = (new Date()).getTime();
             synchronized(guiObjects) { 
 
                 List<GUIObject> toremove = new ArrayList<GUIObject>();
                 for (GUIObject p : guiObjects) {
                     if ((p.getY(time) > height || p.getX(time) < 0 || p.getX(time) > width) && p instanceof Particle) {
                         toremove.add(p);
                     }
                 }
                 guiObjects.removeAll(toremove);
             }
 
 
             if (guiObjects.size() < 20  && winner != null) {
                 int startx = (int)(Math.floor(Math.random() * width));
                 int starty = (int)(Math.floor(Math.random() * height));
                 for (int i = 0; i < 30; i++) {
                     guiObjects.add(new Particle(startx,starty,helper.getGraphicsColor(winner.getPlayerColor()),time));
                 }
             }
 
             try {
                 Thread.sleep(10);
             }
             catch (InterruptedException ex) {
                 // If we've been interrupted, do the update;
             }
         }
     }
 
     public void run() throws InterruptedException {
         Thread graphics = new Thread() {
             public void run() {
                 tickLoop();
             }
         };
 
         Thread ticker = new Thread() {
             public void run() {
                 renderLoop();
             }
         };
 
         graphics.start();
         ticker.start();
         graphics.join();
         ticker.join();
     }
 
     public synchronized void toggleFullScreen() {
 
         // To be honest, this is all a bit messy.
         GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
         GraphicsDevice gs = ge.getDefaultScreenDevice();
 
         // If we can't do full screen and there is no window to display,
         //  then there is nothing to do here.
         if (frame != null && !gs.isFullScreenSupported())
             return;
 
         if (frame != null) {
             frame.setVisible(false);
             gs.setFullScreenWindow(null);
             frame.dispose();
         }
 
         frame = new JFrame(gs.getDefaultConfiguration());
         frame.addKeyListener(controller);

         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.validate();
 
         if (gs.isFullScreenSupported() && !fullScreen) {
            frame.addMouseListener(mouseController);
            frame.addMouseMotionListener(mouseController);
             frame.setUndecorated(true);
             frame.setResizable(false);
             frame.setIgnoreRepaint(true);
             gs.setFullScreenWindow(frame);
 
             fullScreen = true;
         }
         else {
            frame.getContentPane().addMouseListener(mouseController);
            frame.getContentPane().addMouseMotionListener(mouseController);
             frame.setSize(800,600);
             frame.setResizable(true);
             frame.setIgnoreRepaint(false);
             frame.setUndecorated(false);
             frame.setVisible(true);
 

             fullScreen = false;
         }
         frame.createBufferStrategy(2);
     }
 
 
     private synchronized void paint () {
         BufferStrategy bf = frame.getBufferStrategy();
         Graphics graphics = bf.getDrawGraphics();
 
         // background
         graphics.setColor( java.awt.Color.BLACK );
         graphics.fillRect( 0, 0, frame.getWidth(),frame.getHeight() );
 
         // FIXME a bit iffy if not fullscreen
         if (!fullScreen) {
             graphics.translate(0, frame.getHeight() - frame.getContentPane().getHeight());
         }
 
         // antialiasing
         ((Graphics2D)graphics).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                          RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
         ((Graphics2D)graphics).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                          RenderingHints.VALUE_ANTIALIAS_ON);
 
         graphics.setFont(bigFont);
 
         long now = (new Date()).getTime();
 
         synchronized(guiObjects) { 
             isAnimating = false;
             synchronized(hotZones) { // otherwise sometimes a click will happen when hotZones is empty
                 hotZones.clear();
                 for (GUIObject p : guiObjects) {
                     isAnimating |= p.paint(graphics, now, frame.getContentPane().getWidth(), frame.getContentPane().getHeight(), hotZones);
                 }
             }
         }
         graphics.dispose();
         bf.show();
     }
 
     public void winnerChanged(GameEvent wce) {
         winner = wce.getPlayer();
     }
 
     public void playerRemoved(GameEvent e) {
         PlayerPainter toRemove = null;
         synchronized(guiObjects) {
             for (GUIObject o : guiObjects) {
                 if (o instanceof PlayerPainter && ((PlayerPainter)o).player.equals(e.getPlayer())) {
                     toRemove = (PlayerPainter)o;
                 }
                 else {
                     // if it's the painter we're looking for it'll still need to redraw itself
                     o.invalidate();
                 }
             }
         }
         if (toRemove != null) {
             guiObjects.remove(toRemove);
         }
     }
 
     public void playerAdded(GameEvent e) {
         guiObjects.add(factory.getPainter(e.getPlayer(), game));
         synchronized(guiObjects) {
             for (GUIObject o : guiObjects) {
                 o.invalidate();
             }
         }
     }
        
 }
