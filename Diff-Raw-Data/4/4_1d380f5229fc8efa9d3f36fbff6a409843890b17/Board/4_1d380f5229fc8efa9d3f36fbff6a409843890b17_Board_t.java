 package com.github.inside;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import javax.swing.JPanel;
 import java.awt.Toolkit;
 import com.github.inside.Config;
 import java.lang.Math;
 import com.github.inside.Ball;
 import com.github.inside.Helper;
 import com.github.inside.WeightedValue;
 import com.github.inside.Paddle;
 import com.github.inside.PaddleSpeedPower;
 import java.awt.RenderingHints;
 import java.util.List;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 public class Board extends JPanel implements Runnable
 {
     private Thread thread;
     private boolean started = false;
     private boolean paused = false;
     public boolean isRunning = false;
     private long period = 0;
     public long startTime = 0;
     public long fireProjectileTime = 0;
     public long time = 0;
     public List<Projectile> projectiles;
     public Paddle leftPaddle;
     public Paddle rightPaddle;
     public Player leftPlayer;
     public Player rightPlayer;
     public WeightedValue[] weightedValues;
 
     public Board()
     {
         this.setLayout(null);
         this.setBounds(0, 0, Config.BOARD_WIDTH, Config.BOARD_HEIGHT);
         this.period = Math.round((long) 1000 / Config.FRAME_RATE);
         this.projectiles = new CopyOnWriteArrayList<Projectile>();
         this.projectiles.add(this.createNewProjectile("ball"));
         this.leftPaddle = new Paddle("left", this);
         this.rightPaddle = new Paddle("right", this);
         this.weightedValues = new WeightedValue[2];
         this.weightedValues[0] = new WeightedValue("ball",                         1);
         this.weightedValues[1] = new WeightedValue("paddle-speed-power",           99);
//        this.weightedValues[1] = new WeightedValue("paddle-slowness-power",          99);
 //        this.weightedValues[2] = new WeightedValue("paddle-height-power",          10);
 //        this.weightedValues[3] = new WeightedValue("opponents-paddle-speed-power", 10);
         this.setFocusable(true);
         this.setDoubleBuffered(true);
         this.setBackground(Color.decode("#ffffff"));
         this.addKeyListener(new Keyboard(this));
     }
 
     public void paintComponent(Graphics g)
     {
         super.paintComponent(g);
 
         // This fixes flickering on OpenJDK
         Graphics2D g2D = (Graphics2D) g;
         g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                              RenderingHints.VALUE_ANTIALIAS_ON);
 
         for (Projectile projectile : this.projectiles)
         {
             projectile.draw(g2D);
         }
 
         this.leftPaddle.draw(g2D);
         this.rightPaddle.draw(g2D);
     }
 
     public void updateForNewFrame()
     {
         this.time = System.currentTimeMillis() - this.startTime;
         this.startTime = System.currentTimeMillis();
 
         // Paddles
         this.leftPaddle.updateForNewFrame();
         this.rightPaddle.updateForNewFrame();
 
         // Projectiles
         if (this.needsNewProjectile())
         {
             this.initFireProjectileTime();
             this.projectiles.add(this.createNewProjectile("random"));
         }
 
         int i = 0;
 
         for (Projectile projectile : this.projectiles)
         {
             if (projectile.isLiving(this.startTime))
             {
                 projectile.updateForNewFrame();
             }
             else
             {
                 this.projectiles.remove(i);
             }
 
             i++;
         }
 
         // Power timer
         PowerTimer.handlePowerTimer();
 
         // Score
         if (this.leftPlayer.hasReachedScore())
         {
             System.out.println("Left player won with "
                                + this.leftPlayer.score
                                + " points. Right player lost with "
                                + this.rightPlayer.score);
             this.stop();
         }
         else if (this.rightPlayer.hasReachedScore())
         {
             System.out.println("Right player won with "
                                + this.rightPlayer.score
                                + " points. Left player lost with "
                                + this.leftPlayer.score);
             this.stop();
         }
     }
 
     public void run()
     {
         while (true)
         {
             this.updateForNewFrame();
             this.repaint();
 
             try
             {
                 Thread.sleep(this.getNextFrameDelay());
             }
             catch (InterruptedException e)
             {
                 System.out.println("InterruptedException catched");
                 break;
             }
         }
     }
 
     public void start()
     {
         if (this.started)
         {
             return;
         }
 
         this.stop();
         this.leftPlayer.resetScore();
         this.rightPlayer.resetScore();
         this.pursue();
         System.out.println("start called");
     }
 
     public void stop()
     {
         this.started = false;
         this.paused  = true;
         this.stopThread();
         this.projectiles = new CopyOnWriteArrayList<Projectile>();
         this.projectiles.add(this.createNewProjectile("ball"));
         this.leftPaddle.resetPosition();
         this.rightPaddle.resetPosition();
         System.out.println("stop called");
     }
 
     public void pause()
     {
         this.started = true;
         this.paused  = true;
         this.stopThread();
     }
 
     public void pursue()
     {
         this.started = true;
         this.paused = false;
         this.startTime = System.currentTimeMillis();
         this.initFireProjectileTime();
         this.startThread();
         System.out.println("pursue called");
     }
 
     public void togglePause()
     {
         if (!this.started)
         {
             return;
         }
         if (this.paused)
         {
             this.pursue();
         }
         else
         {
             this.pause();
         }
         System.out.println("togglePause called");
     }
 
     private void startThread()
     {
         this.thread = new Thread(this);
         this.thread.setPriority(Thread.MIN_PRIORITY);
         this.thread.start();
     }
 
     private void stopThread()
     {
         if (this.thread != null)
         {
             this.thread.interrupt();
         }
 
         this.thread = null;
     }
 
     private long getNextFrameDelay()
     {
         long processingTime = System.currentTimeMillis() - this.startTime;
 
         if (this.period < processingTime)
         {
             return 0;
         }
 
         return this.period - processingTime;
     }
 
     public Projectile createNewProjectile(String name)
     {
         Projectile projectile = null;
 
         if (name.equals("random"))
         {
             name = Helper.getWeightedRandomValue(this.weightedValues);
         }
 
         if (name.equals("ball"))
         {
             projectile = new Ball(this);
         }
         else if (name.equals("paddle-speed-power"))
         {
             projectile = new PaddleSpeedPower(this);
         }
 //        else if (name.equals("opponents-paddle-speed-power"))
 //        {
 //            projectile = new OpponentsPaddleSpeedPower(this);
 //        }
 //        else if (name.equals("paddle-height-power"))
 //        {
 //            projectile = new PaddleHeightPower(this);
//        }
         else
         {
             System.out.println("try to add an unknown projectile");
         }
 
         return projectile;
     }
 
     public boolean needsNewProjectile()
     {
         return this.fireProjectileTime <= this.startTime && this.projectiles.size() < Config.MAX_PROJECTILES;
     }
 
     public void initFireProjectileTime()
     {
         this.fireProjectileTime = Helper.getRandomFromRange(1000, 2000) + this.startTime;
     }
 }
