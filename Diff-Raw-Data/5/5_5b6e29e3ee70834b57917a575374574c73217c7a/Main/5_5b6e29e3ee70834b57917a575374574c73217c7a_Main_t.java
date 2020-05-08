 
 package Version1;
 import java.util.*;
 import java.util.Random;
 import java.awt.Font;
 import java.io.IOException;
 import static org.lwjgl.opengl.GL11.*;
 import static org.lwjgl.util.glu.GLU.*;
 import org.lwjgl.LWJGLException;
 import org.lwjgl.Sys;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.DisplayMode;
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 import org.newdawn.slick.TrueTypeFont;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.util.ResourceLoader;
 import org.lwjgl.openal.AL;
 import org.newdawn.slick.openal.Audio;
 import org.newdawn.slick.openal.AudioLoader;
 import org.newdawn.slick.openal.SoundStore;
 
 public class Main
 {
     float square2x1 = 50;
     float square2y1 = 50;
     float square2x2 = 100;
     float square2y2  = 100;
     float clickedXPos, clickedYPos =0;
     boolean shoot = false;
     boolean menu = true;
     boolean gameOver = false;
     boolean reloadNeeded = false;
     boolean pressed = false;
     int score =0;
     TrueTypeFont font;
     private ArrayList<Object> wObjs = new ArrayList<Object>();
     private Player player;
     private Object menuObj;
     private Object bkgd;
     private Object GUIObj;
     private Object goverObj;
     private Object mapScreen;
     
     private long lastPressed = 0;
     private long lastSwitch = 0;
     private long reloadTime = 0;
     
     private Random rng = new Random();
     
     private int SCREEN_WIDTH = 1024;
     private int SCREEN_HEIGHT = 600;
     
     private Audio menuMusic;
     private Audio fireSound;
     private Audio roarSound;
     private Audio hitSound;
     private Audio reloadSound;
     private Audio gameOverSound;
     private Audio pickupSound;
     private Audio inGameMusic;
     
     private long lastFrame;
     private int fps;
     private long lastFPS;
     
     public static void main(String[] args)
     {
         Main main = null;
         try
         {
           main = new Main();
           main.create();
           main.run();
         }
         catch(Exception ex)
         {
             //LOGGER.log(Level.SEVERE,ex.toString(),ex);
         }
     }
     
     
     public void create() throws LWJGLException, IOException
     {
         initGL(SCREEN_WIDTH, SCREEN_HEIGHT);
         
         getDelta();
         lastFPS = getTime();
         
         menuObj = new Object("resource/start.png", 0.0f, 0.0f, 1024f, 600f, 0.0f, 0.0f, 1024f, 600f);
         
         player = new Player(10, 10);
         wObjs.add(player);
         
         bkgd = new Object("resource/street2.png", 0.0f, 0.0f, SCREEN_WIDTH, SCREEN_HEIGHT, 220.0f, 0.0f, 420.0f, 280.0f);
         
         GUIObj = new Object("resource/headerBar2.png", 0.0f, 0.0f, SCREEN_WIDTH, 40, 0.0f, 0.0f, 512.0f, 20.0f);
         
         //mapScreen = new Object("resource/map.png", 0.0f, 0.0f, SCREEN_WIDTH, SCREEN_HEIGHT, 0.0f, 0.0f, 1024.0f, 500.0f);
         
         goverObj = new Object("resource/killScreen.png", 0.0f, 0.0f, SCREEN_WIDTH, SCREEN_HEIGHT, 0.0f, 0.0f, 1024.0f, 600.0f);
         
         Font awtFont = new Font("Times New Roman", Font.BOLD, 24);
         font = new TrueTypeFont(awtFont, false);
                     
         menuMusic = AudioLoader.getAudio("WAV", ResourceLoader.getResourceAsStream("resource/menu3.wav"));
         inGameMusic = AudioLoader.getAudio("WAV", ResourceLoader.getResourceAsStream("resource/ingame1.wav"));
         fireSound = AudioLoader.getAudio("WAV", ResourceLoader.getResourceAsStream("resource/shoot.wav"));
         roarSound = AudioLoader.getAudio("WAV", ResourceLoader.getResourceAsStream("resource/roar.wav"));
         hitSound = AudioLoader.getAudio("WAV", ResourceLoader.getResourceAsStream("resource/hit.wav"));
         reloadSound = AudioLoader.getAudio("WAV", ResourceLoader.getResourceAsStream("resource/reload.wav"));
         gameOverSound = AudioLoader.getAudio("WAV", ResourceLoader.getResourceAsStream("resource/gameover.wav"));
         pickupSound = AudioLoader.getAudio("WAV", ResourceLoader.getResourceAsStream("resource/pickup.wav"));
         
         Mouse.setGrabbed(false);
         Mouse.create();        
     }
     
      
     public void destroy()
     {
         AL.destroy();
         Mouse.destroy();
         Display.destroy();
     }
     
     public void initGL(int width, int height)
     {
         try
         {
             Display.setDisplayMode(new DisplayMode(width,height));
             Display.create();
             Display.setTitle("Dinohibition");
             //Display.setVSyncEnabled(true);
 	}
         catch (LWJGLException e)
         {
             e.printStackTrace();
             System.exit(0);
 	}
  
 	glEnable(GL11.GL_TEXTURE_2D);
         glClearColor(0.0f,0.0f,0.0f,0.0f);
         
         glEnable(GL11.GL_BLEND);
         glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
         
         glViewport(0,0,width,height);
 	glMatrixMode(GL_MODELVIEW);
         
         glMatrixMode(GL_PROJECTION);
         glLoadIdentity();
         glOrtho(0, width, height, 0, 1, -1);
         //gluOrtho2D(0.0f,width,0.0f,height);
 	glMatrixMode(GL_MODELVIEW);
         
         glDisable(GL_LINE_SMOOTH);
         glDisable(GL_DEPTH_TEST);
         glDisable(GL_LIGHTING);
     }
     
     public void render()
     {
         glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
         glLoadIdentity();
         
         if(menu == true)
         {
             menuObj.render();
         }
         else if (gameOver == true)
         {
             goverObj.render();
         }
         else
         {
             bkgd.render();
             GUIObj.render();
             for(int i = 0; i < wObjs.size(); i++)
             {
                 if(wObjs.get(i).type.equals("Police"))
                 {
                     if(((Police)wObjs.get(i)).flagDamage)
                     {
                         wObjs.get(i).changeSprite(0, 64);
                         ((Police)wObjs.get(i)).flagDamage = false;
                     }
                     else
                     {
                         //wObjs.get(i).changeSprite(0, 0);
                     }
                 }
                 wObjs.get(i).render();
             }
         
             displayScore();
         }
     }
     
     public void processMouse()
     {
         clickedXPos = Mouse.getX();
         clickedYPos = Mouse.getY();
              
     }
     
     public void menuKeyboard()
     {
         if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE))
         {
            destroy();
             System.exit(0);
         }
         if(Keyboard.isKeyDown(Keyboard.KEY_RETURN))
         {
             menu = false;
         }
     }
     
     public void gameOverKeyboard() throws LWJGLException, IOException
     {
         if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE))
         {
            destroy();
             System.exit(0);
         }
         if(Keyboard.isKeyDown(Keyboard.KEY_RETURN))
         {
             inGameMusic.stop();
             Display.destroy();
             create();
             shoot = false;
             menu = true;
             gameOver = false;
             reloadNeeded = false;
             wObjs.clear();
             score =0;
             player = new Player(10, 10);
             wObjs.add(player);
             run();
         }
     }
     
     public void processKeyboard()
     {
         if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE))
         {
            destroy();
             System.exit(0);
         }
         if(Keyboard.isKeyDown(Keyboard.KEY_D))
         {
             player.setXVel(4);
             long check = getTime();
             check = check - lastPressed;
             
             if(check < 17)
             {            
                 if(player.getSpriteX() == 0)
                 {
                     player.changeSprite(32, player.getSpriteY());
                 }
                 else if(player.getSpriteX() == 32)
                 {
                     player.changeSprite(64, player.getSpriteY());
                 }
                 else
                 {
                     player.changeSprite(0, player.getSpriteY());
                 }         
             }
             lastPressed = getTime();
         }
         else if(Keyboard.isKeyDown(Keyboard.KEY_A))
         {
             player.setXVel(-4);
             long check2 = getTime();
             check2 = check2 - lastPressed;
             
             if(check2 <17)
             {
                 if(player.getSpriteX() == 64)
                 {
                     player.changeSprite(32, player.getSpriteY());
                 }
                 else if(player.getSpriteX() == 32)
                 {
                     player.changeSprite(0, player.getSpriteY());
                 }
                 else
                 {
                     player.changeSprite(64, player.getSpriteY());
                 }
             }
             lastPressed = getTime();
         }
         else
         {
             player.setXVel(0);
         }
         
         if(Keyboard.isKeyDown(Keyboard.KEY_W))
         {
             player.setYVel(-3);
         }
         else if(Keyboard.isKeyDown(Keyboard.KEY_S))
         {
             player.setYVel(3);
         }
         else
         {
             player.setYVel(0);
         }
         
         if(Keyboard.isKeyDown(Keyboard.KEY_SPACE))
         {
             if(player.getBurstShots() < player.getBurstRate())
             {
                 if(reloadNeeded == false)
                 {
                      ((Player)player).setShooting(true);
                 }
             }
             else
             {
                 ((Player)player).setShooting(false);
             }
         }
         else
         {
             ((Player)player).setShooting(false);
             player.setBurstShots(0);
         }
         
         if(Keyboard.isKeyDown(Keyboard.KEY_R))
         {
             if(reloadNeeded && player.getTotalAmmo() > 0)
             {
                 reloadSound.playAsSoundEffect(1.0f, 1.0f, false);
                 player.setShotsFired(0);
                 player.addTotalAmmo(-12);
                 reloadNeeded = false;
                 reloadTime = getTime();
             }
             else if(player.getTotalAmmo() > 0)
             {
                 if(getTime() > (reloadTime +3000))
                 {
                    if(pressed == false)
                    {
                         reloadSound.playAsSoundEffect(1.0f, 1.0f, false);
                         pressed = true;
                    }
                     player.addTotalAmmo(-(player.getShotsFired()));
                     player.setShotsFired(0);
                     reloadTime = getTime();
                 }
             }
         }
         else
         {
             pressed = false;
         }
     }
     
     public void run() throws LWJGLException, IOException            
     {
         boolean inMenu = false;
       while(!Display.isCloseRequested())
       {
           if(Display.isVisible())
           {
               if(menu == true)
               {
                   if(inMenu == false)
                   {
                     inGameMusic.stop();
                     menuMusic.playAsSoundEffect(1.0f, 1.0f, true);
                     inMenu = true;
                   }
                   processMouse();
                   menuKeyboard();
                   startClicked();
                   render();
                   SoundStore.get().poll(0);
               }
               else if(gameOver == true)
               {
                   processMouse();
                   gameOverKeyboard();
                   retryClicked();
                   render();
                   gameOverScore();
               }
               else
               {
                   if(inMenu == true)
                   {
                     menuMusic.stop();
                     inGameMusic.playAsMusic(1.0f, 1.0f, true);
                     inMenu = false;
                   }
                   int delta = getDelta();
                   processKeyboard();
                   processMouse();
                   update(delta);
                   render();
                   SoundStore.get().poll(0);
               }
           }
           Display.update();
           Display.sync(60);
       }
       Display.destroy();
       AL.destroy();
     }
     
     public void update(int delta)
     {
         if(player.isShooting() && player.getLastFired() + 50 < getTime())
         {
             fireSound.playAsSoundEffect(1.0f, 1.0f, false);
             wObjs.add(new Bullet(player.xPos +60 , player.yPos +50, true, "Player"));
             if(player.getShotsFired() < 12)
             {
               player.incBurstShots(1);
               player.incShotsFired(1);
             }
             else
             {
                 player.setShooting(false);
             }
             player.setLastFired(getTime());
         }
         if(player.getShotsFired() == player.shotLimit)
         {
             reloadNeeded = true;
         }
         
         if(gameOver == false)
         {
             constrainPlayer();
             addPoPo();
             updateLocations();
             policeMove();
             bulletCol();
             playDeath();
             poShootYou();
             playerHealthCheck();
         }
     }
     
     public void updateLocations()
     {
         ListIterator wObjIter = wObjs.listIterator();
         while(wObjIter.hasNext())
         {
             Object obj = (Object) wObjIter.next();
             obj.moveUpdate();
         }
     }
     
     public void startClicked()
     {
         if((Mouse.isButtonDown(0))&&(clickedXPos > 270)&&(clickedXPos < 765)&&(clickedYPos < 225)&&(clickedYPos > 150))
         {
             menu = false;
         }
         else if(Mouse.isButtonDown(0))
         {
            // System.out.println(clickedYPos);
         }
     }
     
     public void retryClicked() throws LWJGLException, IOException
     {
         if((Mouse.isButtonDown(0))&&(clickedXPos > 340)&&(clickedXPos < 680)&&(clickedYPos < 210)&&(clickedYPos > 160))
         {
             inGameMusic.stop();
             Display.destroy();
             create();
             shoot = false;
             menu = true;
             gameOver = false;
             reloadNeeded = false;
             wObjs.clear();
             score =0;
             player = new Player(10, 10);
             wObjs.add(player);
             run();
         }        
     }
     
     public void poShootYou()
     {
         for(int i=0; i < wObjs.size(); i++)
         {
             if(wObjs.get(i).type.equals("Police"))
             {
                 if((getTime() > ((Police)wObjs.get(i)).shotRate + 2000) && ((Police)wObjs.get(i)).shotLimit < 3)
                 {
                     wObjs.add(new Bullet(wObjs.get(i).xPos -15 , wObjs.get(i).yPos +50, false, "Police"));
                     ((Police)wObjs.get(i)).shotLimit++;
                     ((Police)wObjs.get(i)).shotRate = getTime();
                 }
             }
         }
     }
     
     public void playerHealthCheck()
     {
         if(player.health < 5)
         {
             inGameMusic.stop();
             gameOverSound.playAsSoundEffect(1.0f, 1.0f, false);
             displayKillScreen();
         }
     }
     
     public void displayKillScreen()
     {
         gameOver = true;
     }
     
     public void policeMove()
     {
         for(int i=0; i < wObjs.size(); i++)
         {
             if(wObjs.get(i).type.equals("Police"))
             {
                 if(((Police)wObjs.get(i)).health > 10)
                 {
                     if(wObjs.get(i).xPos - player.xPos > 0)    
                     {
                         wObjs.get(i).xPos -= 1;
                                               
                         if(wObjs.get(i).getLastSpriteUpdate() + 50 < getTime())
                         {
                             if(wObjs.get(i).getSpriteX() == 0)
                             {
                                 wObjs.get(i).changeSprite(32, 0);
                             }
                             else if(wObjs.get(i).getSpriteX() == 32)
                             {
                                 wObjs.get(i).changeSprite(64, 0);
                             }
                             else
                             {
                                 wObjs.get(i).changeSprite(0, 0);
                             }
                             wObjs.get(i).setLastSpriteUpdate(getTime());
                         }
                     }
                     else
                     {
                         wObjs.get(i).xPos += 1;
                     }
                     
                     if(wObjs.get(i).yPos - player.yPos > 0)
                     {
                         wObjs.get(i).yPos -=1;
                     }
                     else
                     {
                         wObjs.get(i).yPos +=1;
                     }
                 }
                 lastSwitch = getTime();
             }
             else if(wObjs.get(i).type.equals("Ammo"))
             {
                 if(wObjs.get(i).getLastSpriteUpdate() + 80 < getTime())
                 {
                     wObjs.get(i).changeSprite(wObjs.get(i).getSpriteX() + 32, wObjs.get(i).getSpriteY());
                     wObjs.get(i).setLastSpriteUpdate(getTime());
                 }
             }
         }
     }
     
     public void constrainPlayer()
     {
         if(player.xPos < 5)
         {
             player.xPos = 5;
         }
         if(player.xPos > 960)
         {
             player.xPos = 960;
         }
         if(player.yPos > 500)
         {
             player.yPos = 500;
         }
         if(player.yPos < 400)
         {
             player.yPos = 400;
         }
     }
     
     public void playDeath()
     {
         Random randomGenerator = new Random();
         for(int i=0;i<wObjs.size(); i++)
         {
             if(wObjs.get(i).type.equals("Police"))
             {
                 if(((Police)wObjs.get(i)).health < 10)
                 {
                     if(((Police)wObjs.get(i)).deathTimer == 0)
                     {
                         roarSound.playAsSoundEffect(1.0f, 1.0f, false);
                         ((Police)wObjs.get(i)).yPos += 20;
                         if(rng.nextInt(10) < 5)
                         {
                             wObjs.add(new Loot(wObjs.get(i).xPos - 12 + rng.nextInt(32), wObjs.get(i).yPos - 12 + rng.nextInt(32), "Ammo"));
                         }
                         if(rng.nextInt(10) > 5)
                         {
                            wObjs.add(new Loot(wObjs.get(i).xPos - 12 + rng.nextInt(32), wObjs.get(i).yPos - 12 + rng.nextInt(32), "Beer"));
                         }
                     }
                     ((Police)wObjs.get(i)).width =  64;
                     ((Police)wObjs.get(i)).height = 64;
                     ((Police)wObjs.get(i)).changeSprite(32, 64);
                     ((Police)wObjs.get(i)).deathTimer += 10;
                 }
             }
         }
     }
     
     public void addPoPo()
     {
         Random randomGenerator = new Random();
         int chance = randomGenerator.nextInt(150);
                 
         if(chance == 75)
         {
             int randY = randomGenerator.nextInt(120);
             wObjs.add(new Police(1000, 350+randY));
         }
     }
     
     public void bulletCol()
     {
         ListIterator firstIter = wObjs.listIterator();
         while(firstIter.hasNext())
         {
             Object o1 = (Object) firstIter.next();
             
             ListIterator secondIter = wObjs.listIterator();
             
             while(secondIter.hasNext())
             {
                 Object o2 = (Object) secondIter.next();
                 if(o1 != o2)
                 {
                     if((o1.xPos > o2.xPos)&&(o1.xPos < o2.xPos + o2.width)&&(o1.yPos > o2.yPos) &&(o1.yPos < o2.yPos + o2.height))
                     {
                         if((o1.type.equals("Bullet") ^ o2.type.equals("Bullet")) && (o1 != player && o2 != player))
                         {
                             if(o1.type.equals("Police") && ((Bullet)o2).getSource().equals("Player"))
                             {
                                 if(((Police)o1).health >10)
                                 {
                                     hitSound.playAsSoundEffect(1.0f, 1.0f, false);
                                     ((Police)o1).takeDamage();
                                     ((Police)o1).flagDamage = true;                                
                                 }
                             }
                             else
                             {
                                 if(o2.type.equals("Police") && ((Bullet)o1).getSource().equals("Player"))
                                 {
                                     if(((Police)o2).health >10)
                                     {
                                         o1.setToRemove(true);
                                     }
                                 }
                             }
                             
                             if(o2.type.equals("Police") && ((Bullet)o1).getSource().equals("Player"))
                             {
                                 if(((Police)o2).health >10)
                                 {
                                     hitSound.playAsSoundEffect(1.0f, 1.0f, false);
                                     ((Police)o2).takeDamage();
                                     ((Police)o2).flagDamage =true;
                                 }
                             }
                             else
                             {
                                 if(o1.type.equals("Police") && ((Bullet)o2).getSource().equals("Player"))
                                 {
                                     if(((Police)o1).health >10)
                                     {
                                          o2.setToRemove(true);
                                     }
                                 }
                             }
                     
                             break;
                         }
                         
                         if((o1.type.equals("Bullet") ^ o2.type.equals("Bullet")) && (!o1.type.equals("Police") && !o2.type.equals("Police")))
                         {
                             if(o1 == player)
                             {
                                 if(player.health >4)
                                 {
                                     hitSound.playAsSoundEffect(1.0f, 1.0f, false);
                                     player.takeDamage();
                                     //((Police)o1).flagDamage = true;                                
                                 }
                             }
                             else
                             {
                                 if(o2 == player)
                                 {
                                     if(player.health >4)
                                     {
                                         o1.setToRemove(true);
                                     }
                                 }
                             }   
                             if(o2 == player)
                             {
                                 if(player.health >4)
                                 {
                                     hitSound.playAsSoundEffect(1.0f, 1.0f, false);
                                     player.takeDamage();
                                     //((Police)o1).flagDamage = true;                                
                                 }
                             }
                             else
                             {
                                 if(o1 == player)
                                 {
                                     if(player.health >5)
                                     {
                                         o2.setToRemove(true);
                                     }
                                 }
                             }
                         }
                         
                         if(o1.type.equals("Player") && o2.type.equals("Ammo"))
                         {
                             pickupSound.playAsSoundEffect(1.0f, 1.0f, false);
                             player.addTotalAmmo(rng.nextInt(3) * 12);
                             o2.setToRemove(true);
                         }
                         
                         if(o2.type.equals("Player") && o1.type.equals("Ammo"))
                         {
                             pickupSound.playAsSoundEffect(1.0f, 1.0f, false);
                             player.addTotalAmmo(rng.nextInt(3) * 12);
                             o1.setToRemove(true);
                         }
                     }
                 }
             }
         }
         
         checkIfDead();
         
         ListIterator wObjsIter = wObjs.listIterator();
         while(wObjsIter.hasNext())
         {
             Object obj = (Object) wObjsIter.next();
             if(obj.getToRemove() || (obj.type.equals("Bullet") && obj.xPos > SCREEN_WIDTH + 20) || (obj.type.equals("Bullet") && obj.xPos < -20))
             {
                 wObjsIter.remove();
             }
         }
     }
     
     public void checkIfDead()
     {
         ListIterator policeIter = wObjs.listIterator();
         while(policeIter.hasNext())
         {
             Object obj = (Object) policeIter.next();
             if(obj.type.equals("Police"))
             {
                 if(((Police)obj).getHealth() < 10)
                 {
                     if(((Police)obj).deathTimer > 1000)
                     {
                         obj.setToRemove(true);
                         score += 10;
                     }
                 }
             }
         }
     }
     
     public void displayScore()
     {
         font.drawString(20, 5, "Score: "+score, Color.green);
         font.drawString(280, 5, "Health: "+player.health, Color.green);
         font.drawString(540, 5, "Weapon: Tommy", Color.green);
         font.drawString(800, 5, "Ammo: "+(player.shotLimit - player.getShotsFired())+"/"+player.getTotalAmmo(), Color.green);
     }
     
     public void gameOverScore()
     {
         //font.
         font.drawString(400, 320, "Your Final Score: "+score, Color.white); 
     }
     
     public int getDelta()
     {
         long time = getTime();
         int delta = (int) (time - lastFrame);
         lastFrame = time;
         
         return delta;
     }
     
     public long getTime()
     {
         return (Sys.getTime() * 1000) / Sys.getTimerResolution();
     }
 }
