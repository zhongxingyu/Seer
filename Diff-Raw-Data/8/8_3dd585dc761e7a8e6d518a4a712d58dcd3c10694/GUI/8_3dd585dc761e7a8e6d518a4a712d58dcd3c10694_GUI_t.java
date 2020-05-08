 
 import player.*;
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 import java.awt.image.*;
 import enemies.*;
 import java.util.ArrayList;
 import util.*;
 
 public class GUI extends JPanel implements ActionListener, EnemyDeletable
 {
     private Dimension d; 
       
     private JPanel menu;
     private JButton easy, hard, back;
     private JLabel highscoreL;
     private JRadioButton onePlayerRB, twoPlayerRB, mouseRB, keyboardRB;
     private JCheckBox musicCB;
     private JSlider keyboardSpeedS;
     
     private int ballN, monsterN, counterN, randomN, rainN, bombN, monsterMultiplier;
     private int multiplier,highscore, score, level;
     private int invSpeed, defaultDistance, width, height, timeDifficulty1, distanceLimit;
     private int[] borders;
     
     private long startTime, timeElapse, timeLast, timeCircle, timeCircleSwitch, programLoopCounter;
     
     private boolean countdownF, spawnCircleB, spawnMonsterB, spawnRandomersB, spawnRainB;
     private boolean circular, spawnIncrease, onePlayerAlive;
     
     private float distance, programSpeedAdjust;
     
     private Set enemies;
     private java.util.List players;
     private Thread t, r;   
     private scbClass scbInstance = new scbClass();
     private MP3 mp3;
     
     public GUI(Dimension a) throws Exception
     {                   
         d = a;
         
         this.setBackground(Color.darkGray);
         this.setLayout(null);
         this.setBounds(0, 0, d.width, d.height); 
         this.setFocusable(true);
         this.setVisible(true);
         
         highscore = 0;          
         width = this.getWidth();
         height = this.getHeight();
         
         borders = new int[4];
         borders[0] = 15;
         borders[1] = 15;
         borders[2] = width-20;
         borders[3] = height-20;
         
         makeMenuScreen();
         this.add(menu);
     }
 
     private void playerSetup() {
         if (players != null) {
             Iterator i = players.iterator();
             while (i.hasNext()) {
                 Player p = (Player) i.next();
                 if (p instanceof MouseControlledPlayer) {
                     MouseControlledPlayer m = (MouseControlledPlayer) p;
                     removeMouseListener(m);
                     removeMouseMotionListener(m);
                 } else if (p instanceof KeyboardControlledPlayer) {
                     KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher((KeyboardControlledPlayer) p);
                 }
             }
         }
         
         if(!twoPlayerRB.isSelected()) {        
             players = new ArrayList(1);
             if(mouseRB.isSelected()) {
                 MouseControlledPlayer p1 = new MouseControlledPlayer(width / 2, height / 2, 3, true, 10, this, 3);
                 addMouseMotionListener(p1);
                 addMouseListener(p1);
                 players.add(p1);
             } else {
             KeyboardControlledPlayer p1 = 
                 new KeyboardControlledPlayer(width/2, height/2, 3, true, 10, this, 3, KeyEvent.VK_W, KeyEvent.VK_A, KeyEvent.VK_S, KeyEvent.VK_D, keyboardSpeedS.getValue(), KeyEvent.VK_SPACE);
             KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(p1);
             players.add(p1);
             }
         } else {
             players = new ArrayList(2);
             MouseControlledPlayer p1 = new MouseControlledPlayer(width / 2, height / 2, 3, true, 10, this, 3);
             addMouseMotionListener(p1);
             addMouseListener(p1);
             players.add(p1);
             KeyboardControlledPlayer p2 =
                     new KeyboardControlledPlayer(width / 2, height / 2, 3, true, 10, this, 3, KeyEvent.VK_W, KeyEvent.VK_A, KeyEvent.VK_S, KeyEvent.VK_D, keyboardSpeedS.getValue(), KeyEvent.VK_SPACE);
             KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(p2);
             players.add(p2);
         }
     }
         
     private void makeMenuScreen()
     {     
         menu = new JPanel();
         menu.setBackground(Color.darkGray);
         menu.setLayout(null);
         menu.setBounds(0, 0, width, height);   
         
         JLabel specialTitle = new JLabel("I                  M");
         specialTitle.setFont(new Font("sansserif", 3, 32));
         specialTitle.setBounds((width/2)-140, (height/2)-250, 600, 100);
         specialTitle.setForeground(Color.RED.brighter());
         
         JLabel title = new JLabel("  NSANE        OUSE");
         title.setFont(new Font("sansserif", 3, 32));
         title.setBounds((width/2)-140, (height/2)-250, 600, 100);
         title.setForeground(Color.white);
         
         JLabel author = new JLabel("By SJ and HH");
         author.setBackground(Color.darkGray);
         author.setBounds((width/2)-50, (height/2)-200, 500, 100);
         author.setForeground(Color.white);
         
         highscoreL = new JLabel("Highscore:   " + String.valueOf(highscore));
         highscoreL.setBackground(Color.darkGray);
         highscoreL.setBounds((width/2)-113, (height/2)+130, 500, 100);
         highscoreL.setForeground(Color.white);
         
         easy = new JButton("Easy");
         hard = new JButton("Hard");
         
         easy.addActionListener(this);
         hard.addActionListener(this);
         
         easy.setBounds((width/2)-50, (height/2)-50, 100, 20);
         hard.setBounds((width/2)-50, height/2, 100, 20);
         
         onePlayerRB = new JRadioButton("One Player");
         twoPlayerRB = new JRadioButton("Two Player");
         mouseRB = new JRadioButton("Mouse (Player 1)");
         keyboardRB = new JRadioButton("Keyboard (Player 1)");
         keyboardSpeedS = new JSlider(JSlider.HORIZONTAL, 10, 150, 50);
         musicCB = new JCheckBox("Music");
         
         onePlayerRB.setBackground(Color.darkGray);
         twoPlayerRB.setBackground(Color.darkGray);
         mouseRB.setBackground(Color.darkGray);
         keyboardRB.setBackground(Color.darkGray);
         keyboardSpeedS.setBackground(Color.darkGray);              
         musicCB.setBackground(Color.darkGray);
         
         onePlayerRB.setForeground(Color.WHITE);
         twoPlayerRB.setForeground(Color.WHITE);
         mouseRB.setForeground(Color.WHITE);
         keyboardRB.setForeground(Color.WHITE);
         keyboardSpeedS.setForeground(Color.WHITE);
         musicCB.setForeground(Color.WHITE);
         
         ButtonGroup playerChoice = new ButtonGroup();
         playerChoice.add(onePlayerRB);
         playerChoice.add(twoPlayerRB);
         onePlayerRB.setSelected(true);
         
         ButtonGroup peripheralChoice = new ButtonGroup();
         peripheralChoice.add(mouseRB);
         peripheralChoice.add(keyboardRB);
         mouseRB.setSelected(true);
         
         onePlayerRB.setBounds((width/2)+100, (height/2)-50, 100, 20);
         twoPlayerRB.setBounds((width/2)+100, (height/2)-30, 100, 20);
         mouseRB.setBounds((width/2)+100, (height/2), 200, 20);
         keyboardRB.setBounds((width/2)+100, (height/2)+20, 200, 20);
         keyboardSpeedS.setBounds(width/2-120, height/2+100, 200, 50);
         musicCB.setBounds((width/2)+100, (height/2)+50, 100, 20);
  
         JLabel keyboardSpeedL = new JLabel("Keyboard Speed");
         keyboardSpeedL.setForeground(Color.WHITE);
         keyboardSpeedL.setBounds(width/2-113, height/2+70, 200, 50);
         
         menu.add(title);
         menu.add(specialTitle);
         menu.add(author);
         menu.add(easy);
         menu.add(hard);
         menu.add(highscoreL);
         menu.add(onePlayerRB);
         menu.add(twoPlayerRB);
         menu.add(mouseRB);
         menu.add(keyboardRB);
         menu.add(keyboardSpeedL);
         menu.add(keyboardSpeedS);
         menu.add(musicCB);    
         
         back = new JButton("Back");
         back.setBounds(width/2-40, height/2+20, 100, 20);
         back.addActionListener(this);
         back.setVisible(false);
         this.add(back);
     }
     
     private void audioSetup() {
         try {
            mp3 = new MP3();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(127);
        }
         mp3.play();
     }
     
     private void setup()
     {
         playerSetup();
         
         menu.setVisible(false);
         this.revalidate();
         
         audioSetup();
         
         Iterator i = players.iterator();
         Player p;
         while(i.hasNext()) {
             p = (Player)i.next();
             p.resetLives(3);
             p.setActive(true);
         }
         
         BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
         Cursor blank = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0,0), "BLANK");
         this.setCursor(blank);
        
         reset();
         
         ballN = 1; 
         level = 1;
         timeLast = 0;        
         score = 0;        
         counterN = 10;
         timeCircle = 0;
         timeCircleSwitch = 0;
         programLoopCounter = 1;
         programSpeedAdjust = 1;
         
         onePlayerAlive = true;                      
         countdownF = true;
         circular = true;
         spawnIncrease = true;
         spawnCircleB = false;  
         spawnMonsterB = false;
         spawnRandomersB = false;
                         
         levelSetup();
         countdown();
         animate();    
     }
     
     private void reset()
     {
         if(enemies != null)
         {
             enemies.clear();
         }
         else 
         {
             enemies = new HashSet();
         }
     }
     
     private void levelSetup()
     {
         reset();
         
         switch(level)
         {
             case 1:                              
                 distance = 600;
                 monsterN = 0;
                 randomN = 0;
                 rainN = 40;
                 defaultDistance = 600;
                 timeLast = 20000;
                 spawnCircleB = true;    
                 break;
             case 2:
                 distance = 600;
                 monsterN = 0;
                 randomN = 40;
                 rainN = 10;
                 defaultDistance = 600;
                 timeLast += 20000;
                 spawnCircleB = true;
                 break;
             case 3:
                 distance = 600;
                 monsterN = 40;
                 randomN = 0;
                 rainN = 0;
                 defaultDistance = 600;
                 timeLast += 20000;
                 spawnCircleB = true;
                 break;
             case 4:
                 distance = 400;
                 monsterN = 10;
                 randomN = 10;
                 rainN = 0;
                 defaultDistance = 400;
                 spawnCircleB = true;
                 timeLast += 20000;
                 break;
             case 5:
                 distance = 400;
                 monsterN = 15;
                 randomN = 15;
                 rainN = 20;
                 defaultDistance = 400;
                 spawnCircleB = true;
                 timeLast += 50000000;
                 level = 1;
                 break;
         }    
           
         monsterN *= monsterMultiplier;
         randomN *= monsterMultiplier;
         spawnMonsterB = true;    
         spawnRandomersB = true;
         spawnIncrease = true;
         spawnRainB = true;
     }
                   
     private void drawLayout(Graphics g)
     {
         g.setColor(Color.blue);
         g.drawRect(10, 10, width-20, height-20);
 
         g.setColor(Color.red.brighter());
         Iterator f = players.iterator();
         int lifeDisplayPosition = 0;
         while(f.hasNext()) 
         {
             Player h = (Player)f.next();
             if(h.isActive())
             {
                 lifeDisplayPosition++;
                 String lifeInformation = "Player " + lifeDisplayPosition + ": ";
                 for(int i = 0; i < h.getLives(); i++)
                 {
                     lifeInformation += "\u2606";
                 }
                 g.drawString(lifeInformation, 20, lifeDisplayPosition*40);
             }
         }
         
         borders[2] = width-20;
         borders[3] = height-20;
         
         Iterator i = players.iterator();
         Player p;
         while(i.hasNext()) {
             p = (Player)i.next();
             if(p.isActive() && !countdownF &&
                     (p.getX() < borders[0] || p.getY() < borders[1] || p.getX() > borders[2] || p.getY() > borders[3])
                     &&(!p.getImmunity())){
                 p.decLives(width / 2, height / 2);    
                 p.setImmunity(true);
             }                        
             if(p.getLives() <= 0) {
                 g.setColor(Color.red.darker());
                 g.drawString("GAME OVER", width/2-20, height/2);
                 g.drawLine(p.getX()-15, p.getY()-15, p.getX()+15, p.getY()+15);
                 g.drawLine(p.getX()+15, p.getY()-15, p.getX()-15, p.getY()+15);
             }
         }
     }
        
     private void countdown() 
     {       
         t = new Thread() 
         {
             public void run()
             {   
                 long time = System.currentTimeMillis();
                 long timeNow = System.currentTimeMillis();
                 
                 while((time + 1000) > timeNow)
                 {
                     timeNow = System.currentTimeMillis();
                     counterN = (int)(1000-(timeNow-time))/100;
                 } 
                             
                 countdownF = false;
                 startTime = System.currentTimeMillis();                    
             }
         };
         if(countdownF)
         {
             t.start();
         }
         else
         {
             t = null;
         }
     }
     
     private void spawnCircles()
     {
         if(circular) 
         {
             for(int i = 0; i < ballN; i++) 
             {
                 double degree = Math.random()*2*Math.PI;
                 float x = ((Player)players.get(0)).getX() + distance * (float) Math.sin(degree * i);
                 float y = ((Player)players.get(0)).getY() + distance * (float) Math.cos(degree * i);
                 enemies.add(new EnemyTypes.Circle(x, y, invSpeed));
             }
         } 
         else 
         {
             for(int i = 1; i < (ballN/2); i++) 
             {
                 float x = (i * 2 * width) / (ballN);
                 float y = 0;
                 enemies.add(new EnemyTypes.Circle(x, y, invSpeed));
             }
 
             for(int i = (ballN/2) + 1; i < ballN; i++) 
             {
                 float x = ((i-ballN/2)*2*width)/ballN;
                 float y = height;
                 enemies.add(new EnemyTypes.Circle(x, y, invSpeed));
             }
         }      
         spawnIncrease = false;
     }
     
     private void spawnMonsters()
     {
         for( int i = 0; i < monsterN; i++)
         {
             float x = (float)Math.random()*width;
             float y = (float)Math.random()*height;          
             float r = (float)Math.sqrt(Math.pow(((Player)players.get(0)).getX() - x, 2) 
                     + Math.pow(((Player)players.get(0)).getY() - y, 2));
             
             while(r < distanceLimit)
             {
             	x = (float)Math.random()*width;
             	y = (float)Math.random()*height;
             	r = (float)Math.sqrt(Math.pow(((Player)players.get(0)).getX() - x, 2) 
                         + Math.pow(((Player)players.get(0)).getY() - y, 2));
             }
             
             enemies.add(new EnemyTypes.Monster(x, y, .8f));
         }
 
         spawnMonsterB = false;
     }
     
     private void spawnRandomers() 
     {
         for (int i = 0; i < randomN; i++) 
         {
             float x = (float) Math.random() * width;
             float y = (float) Math.random() * height;
             float r = (float) Math.sqrt(Math.pow(((Player)players.get(0)).getX() - x, 2) 
                     + Math.pow(((Player)players.get(0)).getY() - x, 2));
 
             while (r < distanceLimit) 
             {
                 x = (float) Math.random() * width;
                 y = (float) Math.random() * height;
                 r = (float) Math.sqrt(Math.pow(((Player)players.get(0)).getX() - x, 2) 
                         + Math.pow(((Player)players.get(0)).getY() - y, 2));
             }
             
             enemies.add(new EnemyTypes.Random(x, y, 0.5f, borders));
         }
 
         spawnRandomersB = false;
     }
 
     private void spawnRain()
     {   
         for(int i = 0; i < rainN; i++) 
         {
             float x = (i*2*width)/rainN;
             float y = 0;
             enemies.add(new EnemyTypes.Rain(x, y, 4, height));
         }        
         spawnRainB = false; 
     }
            
     private class scbClass implements util.SetCallback 
     {
         public void add(Object o) 
         {
             enemies.add(o);
         }
     }
     
     private boolean spawnBombB()
     {
         boolean bombPartsExist = false;
         try
         {
             Iterator i = enemies.iterator();
             while (i.hasNext()) 
             {
                 Enemy e = (Enemy) i.next();
                 if(e.getClass().equals(EnemyTypes.Bomb.class)||(e.getClass().equals(EnemyTypes.Shrapnel.class)))
                 {
                     bombPartsExist = true;
                     break;
                 }
             }
         }catch(Exception e)
         {}
         return !bombPartsExist;
     }
     
     private void spawnBomb()                           
     {
         float x,y;
         for(int i = 0; i < bombN; i++)
         {
             if(Math.random() > .5) { //top/bottom
                 y = (Math.random()>.5)? borders[1]: borders[3];
                 x = (float)Math.random()*borders[2]-borders[0]; //width
             }
             else {
                 x = (Math.random()>.5)? borders[0]: borders[2];
                 y = (float)Math.random()*borders[3]-borders[1]; //height
             } 
             enemies.add(new EnemyTypes.Bomb(x, y, 1f, borders, scbInstance));
         }
     }
     
     int iter = 0;
     private void animate()
     {
         r = new Thread() 
         {
             public void run()
             {
                 while(true)
                 {                                  
                     if(!countdownF)
                     {
                         countdown();
                         
                         if(!onePlayerAlive)
                         {                       
                             if(highscore < score)
                             {
                                 highscore = score;
                                 highscoreL.setText(String.valueOf(highscore));
                             }
                             mp3.stop();
                             break;
                         } 
                         
                         timeElapse = 1+System.currentTimeMillis()-startTime;  
                         programLoopCounter++;
                         if(programLoopCounter % 100 == 0)
                         {
                             float loopPerSec = (float)programLoopCounter/timeElapse;
                             programSpeedAdjust = (0.3f)/loopPerSec;
                         }
                                                
                         if((timeElapse > timeCircle)&&(ballN < 20))
                         {
                             timeCircle = timeElapse + timeDifficulty1;
                             ballN++;
                             distance++;
                             spawnIncrease = true;
                         }
                         
                         score = (int)timeElapse*multiplier;
                         
                         if(timeElapse > timeLast)
                         {
                             level++;
                             levelSetup();
                         }                       
                         
                         if(timeElapse > timeCircleSwitch)
                         {
                             timeCircleSwitch = timeElapse + 10000;
                             deleteIf(new EnemyPredicate() {
                                 public boolean satisfiedBy(Enemy e) {
                                     return e.getClass().equals(EnemyTypes.Circle.class);
                                 }
                             });
                             if(iter++%2==0)
                             {
                             	spawnMonsters();
                             }
                             ballN = 1;
                             distance = defaultDistance;
                             circular = !circular;                           
                         }
                                               
                         deleteIf(new EnemyPredicate() {
                             public boolean satisfiedBy(Enemy e) {
                                 return e.getClass().equals(EnemyTypes.Shrapnel.class)
                                     &&(((EnemyTypes.Shrapnel)e).getBounces() > 1);
                             }
                         });
                         
                         deleteIf(new EnemyPredicate() {
                             public boolean satisfiedBy(Enemy e) {
                                 return e.getClass().equals(EnemyTypes.Bomb.class)
                                     &&(((EnemyTypes.Bomb)e).isMortal());
                             }
                         });
                         
                         movePlayers();
                     }
 
                     try
                     {
                         SwingUtilities.invokeAndWait(new Runnable() 
                         {
                             public void run()
                             {
                                 repaint();
                             }
                         });
                     }catch(Exception e)
                     {
                     }     
                 }
                 back.setVisible(true);
                 setCursor(Cursor.getDefaultCursor());
             }
         };
         r.start();
     }
     
     public void deleteIf(EnemyPredicate p) {
         try
         {
             Set newEnemies = new HashSet(enemies.size());
             Iterator i = enemies.iterator();
             while (i.hasNext()) {
                 Enemy e = (Enemy) i.next();
                 if (!p.satisfiedBy(e)) {
                     newEnemies.add(e);
                 }
             }
             enemies = newEnemies;
         }catch(Exception e)
         {}
     }
     
     private void movePlayers()
     {
         onePlayerAlive = false;
         Iterator i = players.iterator();
         while(i.hasNext()) {
             Player p = (Player)i.next();
             if(p.getLives() == 0)
             {
                 p.setActive(false);
             }
             if(p.isActive())
             {
                 onePlayerAlive = true;
                 p.move();
             }
         }
     }
     
     private void drawPlayers(Graphics g) 
     {           
         Iterator i = players.iterator();
         Player p;
         while(i.hasNext()) 
         {
             p = (Player)i.next();
 
             if(p.isActive())
             {
                 if(p.getImmunity())
                 {
                     g.setColor(Color.red.brighter());
                     g.drawOval(p.getX()-30, p.getY()-30, 60, 60);
                 }
                 g.setColor(Color.WHITE);
                 g.fillOval(p.getX()-5, p.getY()-5, 10, 10);
             }
 
             p.paint(g);
         }
     }
    
     public void paintComponent(Graphics g)
     {
         super.paintComponent(g);
        	
         height = this.getHeight();
         width = this.getWidth();
         
         drawPlayers(g);
         g.drawString("Score", width - 60, 25);
         g.drawString(String.valueOf(score), width-60, 40);
         
         if(countdownF)
         {
             g.drawString(String.valueOf(counterN), width/2-10, height/2);
         }
         else
         {                  
             if((spawnCircleB)&&(spawnIncrease))
             {
                 spawnCircles();
             }
             if(spawnMonsterB)
             {
                 spawnMonsters();
             }
             if(spawnRandomersB)
             {
                 spawnRandomers();
             }
             if(spawnRainB)
             {
                 spawnRain();
             }
             if(spawnBombB())
             {
                 spawnBomb();
             }
             
             try
             {
                 Iterator i = enemies.iterator();
                 while(i.hasNext()) 
                 {
                     Enemy e = (Enemy)i.next();
                     Iterator j = players.iterator();
                     while(j.hasNext()) {
                         Player p = (Player)j.next();
                         e.move(p.getX(), p.getY(), programSpeedAdjust/*/players.size()*/);
                         if((e.collidesWith(p.getX(), p.getY()))&&(!p.getImmunity())) {
                             p.decLives(p.getX(), p.getY());
                             p.setImmunity(true);
                         }
                     }
                     e.paint(g);
                 }
             }catch(Exception e)
             {               
             }
             
         }     
         drawLayout(g);       
     }
        
     public void actionPerformed(ActionEvent e)
     {
         if(e.getSource() == easy)
         {          
             invSpeed = 50000;
             bombN = 1;
             timeDifficulty1 = 1000;
             distanceLimit = 400;
             monsterMultiplier = 1;
             multiplier = 1;
             setup();
         }
         else if(e.getSource() == hard)
         {
             invSpeed = 30000;
             bombN = 4;
             timeDifficulty1 = 500;
             distanceLimit = 200;
             monsterMultiplier = 2;
             multiplier = 2;       
             setup();
         }
         else if(e.getSource() == back)
         {
             r = null;
             menu.setVisible(true);      	
             back.setVisible(false);
             this.revalidate();        
             repaint();            
         }
     }
 }
