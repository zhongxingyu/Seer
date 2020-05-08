 import javax.sound.sampled.AudioSystem;
 import javax.sound.sampled.Clip;
 import javax.sound.sampled.LineUnavailableException;
 import javax.sound.sampled.UnsupportedAudioFileException;
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.MouseMotionListener;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.ConcurrentModificationException;
 import java.util.Random;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 /**
  * User: FeliciousX
  * Date: 5/21/13
  * Time: 3:58 PM
  *
  * MissilePanel is where the GUI for the game is shown.
  * The largest part of the game is also handled here.
  */
 public class MissilePanel extends JPanel implements MouseMotionListener, MouseListener, Runnable {
     public static final int MAX_WIDTH = 600;
     public static final int MAX_HEIGHT = 500;
 
     private double angle;
     private Image cannon, background;
     private CopyOnWriteArrayList<Target> targetArray;
     private CopyOnWriteArrayList<Missile> missileArray;
     private boolean gameOver;
     private Player player;
     private StatusPanel statusPanel;
     private HighScore highScore;
 
     public MissilePanel(Player player, StatusPanel statusPanel, HighScore highScore) {
         super();
         this.player = player;
         this.statusPanel = statusPanel;
         this.highScore = highScore;
 
         this.setBackground(new Color(255, 255, 255));
         this.setPreferredSize(new Dimension(MAX_WIDTH, MAX_HEIGHT));
 
         this.init();
     }
 
     private void init()
     {
         background = Toolkit.getDefaultToolkit().getImage("img/bg.jpg");
         cannon = Toolkit.getDefaultToolkit().getImage("img/cannon.png");
         angle = Math.PI * 2;
         this.targetArray = new CopyOnWriteArrayList<Target>();
         this.missileArray = new CopyOnWriteArrayList<Missile>();
         this.addMouseMotionListener(this);
         this.addMouseListener(this);
 
         this.setVisible(false);
     }
 
     public void startGame() {
         this.gameOver = false;
         this.targetArray.add(new Target(player.getLevel()));
 
         Thread run = new Thread(this);
         run.start();
     }
 
     @Override
     public void mouseMoved(MouseEvent e) {
         // Calculate the angle based on the coordinate of mouse and origin of cannon uses inverse tangent
         // tan(titah) = opposite / adjacent
         // - PI / 2 because of the way java is coordinated
         angle = Math.atan2(MAX_HEIGHT - e.getY(), (MAX_WIDTH / 2) - e.getX()) - Math.PI / 2;
         repaint();
     }
 
     @Override
     public void paintComponent(Graphics g) {
         super.paintComponent(g);
         g.drawImage(background, 0, 0, MAX_WIDTH, MAX_HEIGHT, this);
         Graphics2D g2d = (Graphics2D) g;
         updateMissiles(g2d);
         updateBombs(g2d);
         updateCannon(g2d);
     }
 
     private synchronized void updateMissiles(Graphics2D g2d) {
 
         for(Missile m: missileArray) {
            Color randomColor = new Color((float)Math.random(), (float)Math.random(), (float)Math.random());
            g2d.setColor(randomColor);
            g2d.setStroke(new BasicStroke(3));
            g2d.drawLine(m.getPrevX(), m.getPrevY(), m.getxPos(), m.getyPos());
         }
 
         g2d.setColor(new Color(255, 23, 28));
     }
 
     private synchronized void updateBombs(Graphics2D g2d) {
         for (Target t: targetArray) {
             g2d.fillOval(t.getxPos(), t.getyPos(), Target.getDiameter(), Target.getDiameter());
         }
     }
 
     private synchronized void updateCannon(Graphics2D g2d) {
         double img_width = cannon.getWidth(this);
         double img_height = cannon.getHeight(this);
         double centerX = img_width / 2;
         double centerY = img_height / 2;
 
         g2d.translate(MAX_WIDTH / 2 - centerX, MAX_HEIGHT - centerY);
         g2d.rotate(angle, centerX, centerY);
         g2d.drawImage(cannon, 0, 0, this);
     }
 
     @Override
     public void run() {
         while(!gameOver) { // Runs thread until game over
 
             ArrayList<Target> toDeleteTarget = new ArrayList<Target>();
             ArrayList<Missile> toDeleteMissile = new ArrayList<Missile>();
 
 
             for (Target t: targetArray) {
                 t.move();  // move every target down depending on their speed
 
                 detectCollision(t);
                 // if target exceed screen, damage player and delete target from arrayList
                 if (t.getyPos() > MAX_HEIGHT ) {
                     toDeleteTarget.add(t);
                     player.damage();
                 }
 
                 // if target health is 0 or less, delete target from arrayList
                 if (t.getHealth() <= 0) {
                     toDeleteTarget.add(t);
                     player.addScore(1);
                 }
             }
 
             if (! toDeleteTarget.isEmpty()) {
                 targetArray.removeAll(toDeleteTarget);
             }
 
             for (Missile m: missileArray) {
                 m.move();  // move missile depending on angle by 5 pixels up Y-axis (Default)
 
                 // Check if missile go out of screen
                 // Or if missile has collided with target
                 // delete missile from arrayList
                 if (m.getxPos() < 0 || m.getxPos() > MAX_WIDTH || m.getyPos() < 0
                         || m.isCollided()) {
                     toDeleteMissile.add(m);
                 }
             }
 
             if (! toDeleteMissile.isEmpty()) {
                 missileArray.removeAll(toDeleteMissile);
             }
 
             Random ran = new Random();
             int i = ran.nextInt(100);
             if (i == 50) {
                 // Generates a new bomb in 1/100 chance
                 targetArray.add(new Target(player.getLevel()));
             }
 
             repaint();
             try {
                 Thread.sleep(17);  // 17 makes it 60 fps
             } catch (InterruptedException e) {
                 e.printStackTrace();
             }
 
             statusPanel.update();
 
             // increase difficulty every 20 levels (Default)
             if (player.getScore() / Assign2.getLevelInterval() == player.getLevel()) {
                 player.setLevel(player.getLevel() + 1);
             }
 
             // checks player health. 0 or less means game over.
             if (player.getHealth() <= 0) {
                 player.setWin(false);
                 gameOver = true;
 
                 Thread die = new Thread() {
                     @Override
                     public void run(){
                         try {
                             Clip c = AudioSystem.getClip();
                            c.open(AudioSystem.getAudioInputStream(new File("src/sound/game_over.wav")));
                             c.start();
                         }
                         catch (LineUnavailableException e) {
                             // No Sound then
                             e.printStackTrace();
                         }
                         catch (UnsupportedAudioFileException e)
                         {
                             // This sound format is not supported
                             e.printStackTrace();
                         }
                         catch (IOException e) {
                             // No sound again
                             e.printStackTrace();
                         }
                     }
                 };
                 die.start();
             }
 
             // if player's score reaches the winning score, Game over!
             if (player.getScore() >= Assign2.getWinningScore()) {
                 player.setWin(true);
                 gameOver = true;
             }
         }
 
         if (player.getScore() > highScore.getLowestHighScore()) {
             JOptionPane.showMessageDialog(getParent(), "You have made it in the top 10 list!", "Congratulations!", JOptionPane.INFORMATION_MESSAGE);
             highScore.update(player);
         }
         else if (highScore.getNumberOfPlayers() < HighScore.getMaxPlayers()) {
             highScore.update(player);
         }
 
         highScore.displayScores();
 
         this.showGameOverDialog();
     }
 
     // Detects collision between target and missile.
     // If coordinate of missile is inside the area of target t, collision is true
     private void detectCollision(Target t) {
         int originX, originY;
 
         originX = t.getxPos();
         originY = t.getyPos();
 
         for (Missile m : missileArray ) {
             int mX = m.getxPos();
             int mY = m.getyPos();
             int mCx = m.getPrevX();
             int mCy = m.getPrevY();
 
             // it checks if any missile is in the area of the target. If true, it will damage the target and clear missile
             if ( ((mX <= originX + Target.getDiameter()) && (mX >= originX)) && ((mY >= originY) && (mY <= originY + Target.getDiameter()))
                     || ((mCx <= originX + Target.getDiameter()) && (mCx >= originX)) && ((mCy >= originY) && (mCy <= originY + Target.getDiameter())) ) {
 
                 t.damage(m.getPower());
                 m.setCollided(true);
 
                 Thread pak = new Thread() {
                     @Override
                     public void run(){
                         try {
                             Clip c = AudioSystem.getClip();
                            c.open(AudioSystem.getAudioInputStream(new File("src/sound/pak.wav")));
                             c.start();
                         }
                         catch (LineUnavailableException e) {
                             // No Sound then
                             e.printStackTrace();
                         }
                         catch (UnsupportedAudioFileException e)
                         {
                             // This sound format is not supported
                             e.printStackTrace();
                         }
                         catch (IOException e) {
                             // No sound again
                             e.printStackTrace();
                         }
                     }
                 };
                 pak.start();
             }
         }
     }
 
     private void showGameOverDialog() {
         DialogGameOver dialogGameOver = new DialogGameOver(player);
 
         final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
         final int x = (screenSize.width - dialogGameOver.getWidth()) / 2;
         final int y = (screenSize.height - dialogGameOver.getHeight()) / 2;
 
         dialogGameOver.setLocation(x, y);
         dialogGameOver.setVisible(true);
     }
 
     // Adds a new missile each time mousePressed is invoked
     @Override
     public void mousePressed(MouseEvent e) {
         this.missileArray.add(new Missile(e.getX(), e.getY()));
         Thread pew = new Thread() {
             @Override
             public void run(){
                 try {
                     Clip c = AudioSystem.getClip();
                    c.open(AudioSystem.getAudioInputStream(new File("src/sound/pew.wav")));
                     c.start();
                 }
                 catch (LineUnavailableException e) {
                     // No Sound then
                     e.printStackTrace();
                 }
                 catch (UnsupportedAudioFileException e)
                 {
                     // This sound format is not supported
                     e.printStackTrace();
                 }
                 catch (IOException e) {
                     // No sound again
                     e.printStackTrace();
                 }
             }
         };
         pew.start();
         repaint();
     }
 
     @Override
     public void mouseClicked(MouseEvent e) {
         // Nothing
     }
 
     @Override
     public void mouseReleased(MouseEvent e) {
         // Nothing
     }
 
     @Override
     public void mouseEntered(MouseEvent e) {
         // Nothing
     }
 
     @Override
     public void mouseExited(MouseEvent e) {
         // Nothing
     }
 
     @Override
     public void mouseDragged(MouseEvent e) {
         // Nothing
     }
 }
