 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package egyptians;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Random;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.newdawn.slick.Animation;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Vector2f;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 /**
  *
  * @author rikki
  */
 public class GameState extends BasicGameState
 {
     
     Image stage = null;
     Image moses = null;
 
     private List<Cow> cows = new LinkedList<>();
     private List<Dude> dudes = new LinkedList<>();
 
     Image[] boxes = new Image[4];
     Image thunder1 = null;
     Image thunder2 = null;
     Animation thunder;
     Vector2f thunderPos;
     
     final static int POWER_COW = 0;
     final static int POWER_LIGHTNING = 1;
     final static int POWER_HAILSTORM = 2;
     final static int POWER_DEATH = 3;
     int[] power_cooldown_time = { 1000, 3000, 5000, 25000 };
     int[] power_cooldown_time_remainaing = { 0, 0, 0, 0 };
     
     static Image[][] boximages;
     
     static {
         try {
             Image[][] nboximages = {
                 {new Image("box1.png"), new Image("box1click.png")},
                 {new Image("box2.png"), new Image("box2click.png")},
                 {new Image("box3.png"), new Image("box3click.png")},
                 {new Image("box4.png"), new Image("box4click.png")}
             };
             boximages = nboximages;
         } catch (SlickException ex) {
             Logger.getLogger(GameState.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     
     private boolean can(int power) { return power_cooldown_time_remainaing[power] <= 0; }
     private void activated(int power) { power_cooldown_time_remainaing[power] = power_cooldown_time[power]; }
     
     private enum STATES 
     {
         NORMAL_STATE, PLACE_COW_STATE, PLACE_THUNDER_STATE;
     }
     
     private STATES state = STATES.NORMAL_STATE;
     
     static final int HAILTIME = 2000;
     static public int hailTimeLeft = 0;   // time in ms remaining of hail
     
     static final int LIGHTNINGTIME = 500;
     static public int lightningTimeLeft = 0;
     
     private int stateid = -1;
     public GameState(int sid)
     {
         stateid = sid;
     }
     
     @Override public int getID()
     {
         return stateid;
     }
     
     @Override public void init(GameContainer gc, StateBasedGame sbg) throws SlickException
     { 
         gc.setMinimumLogicUpdateInterval(1000/60);
         this.stage = new Image("stage.png");
         this.moses = new Image("moses.png");
         this.boxes[0] = boximages[0][0];
         this.boxes[1] = boximages[1][0];
         this.boxes[2] = boximages[2][0];
         this.boxes[3] = boximages[3][0];
         this.thunder1 = new Image("thunder1.png");
         this.thunder2 = new Image("thunder2.png");
         Image[] thunders = { thunder1, thunder2 };
         this.thunder = new Animation(thunders, 100, true);
     }
   
     Random randomGenerator = new Random();
     @Override public void update(GameContainer gc, StateBasedGame sbg, int delta) throws SlickException
     { 
         if(hailTimeLeft > 0) 
         {
             hailTimeLeft -= delta;
             if(hailTimeLeft <= 0)
             {
                 Dude.dudeSpeed = Dude.DUDE_SPEED_NORMAL;
             }
         }
         
         if(lightningTimeLeft > 0)
             lightningTimeLeft -= delta;
         
         for(int i = POWER_COW; i <= POWER_DEATH; ++i)
             power_cooldown_time_remainaing[i] -= delta;
         
         double DUDE_PROBABILITY_KINDA_THING = 0.5;
         // shall we create a Dude? lets ask probability!
         if(randomGenerator.nextDouble() * delta < DUDE_PROBABILITY_KINDA_THING)
             createDude();
         
         //let all the entities think
         Iterator<Dude> iterd = dudes.iterator();
         while(iterd.hasNext())
         {
             Dude d = iterd.next();
             // check if it's gone offscreen
             if(d.pos.x > Egyptians.WINDOW_SIZE.x)
             {
                 iterd.remove();
             }
             else d.think(delta);
         }
         
         Iterator<Cow> iterc = cows.iterator();
         while(iterc.hasNext())
         {
             Cow c = iterc.next();
             // check if it's gone offscreen
             if(c.pos.y > Egyptians.WINDOW_SIZE.y)
             {
                 iterc.remove();
             }
             else 
             {
                 c.think(delta);
                 
                 Iterator<Dude> dudeIter = dudes.iterator();
                 while(dudeIter.hasNext())
                 {
                     Dude d = dudeIter.next();
 
                     //collision test for the cow/dude
                     if((c.pos.y + c.size.y > d.pos.y) &&
                       (c.pos.x + c.size.x > d.pos.x) &&
                       (c.pos.x < d.pos.x + d.size.x) &&
                       (d.pos.y + d.size.y > c.pos.y))
                     {
                     //if we get here, it's a collission
                     cowCollide(c, d);
                     dudeIter.remove();
                     killed();
                     }
                 }
             }    
         }
         
         // TODO place here all the logic which calls the event methods below
             
     }
     
     @Override public void keyPressed(int key, char c)
     {
         try {
         if (key == Input.KEY_1)
         {
             this.boxes[0] = boximages[0][1];
             doCow();
         }
           
         if (key == Input.KEY_2)
         {
             this.boxes[1] = boximages[1][1];
             doThunder();
         }
                     
         if (key == Input.KEY_3)
         {
             this.boxes[2] = boximages[2][1];
             doHailstorm();
         }
                     
         if (key == Input.KEY_4)
         {
             this.boxes[3] = boximages[3][1];
             doAngelOfDeath();
         }
         } catch (SlickException ex) {
             Logger.getLogger(GameState.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
           
     
     @Override public void keyReleased(int key, char c)
     {
         
         if (key == Input.KEY_1)
             this.boxes[0] = boximages[0][0];
            
         else if (key == Input.KEY_2)
             this.boxes[1] = boximages[1][0];
           
         else if (key == Input.KEY_3)
             this.boxes[2] = boximages[2][0];
           
         else if (key == Input.KEY_4)
             this.boxes[3] = boximages[3][0];
     }
             
     @Override public void render(GameContainer gc, StateBasedGame sbg, Graphics g) throws SlickException
     {
         stage.draw(0, 0);
         moses.draw(880, 200);
         for (int i=0;i<4;i++)
             boxes[i].draw(30+i*130, 30);
         
         g.drawString("CowCount: " + cows.size(), 190, 220);
         g.drawString("DudeCount: " + dudes.size(), 190, 235);
         
         for(int i = POWER_COW; i <= POWER_DEATH; ++i)
         {
             if(!can(i))
             {
                 float progress = (float)power_cooldown_time_remainaing[i] / (float)(power_cooldown_time[i]);
                 g.fillRoundRect(30+i*130, 135, progress * 100, 5, 3);
             }
         }
 
         //render each entity
         Iterator<? extends Entity> iter = dudes.iterator();
         while(iter.hasNext())
             iter.next().render(g);
         
         iter = cows.iterator();
         while(iter.hasNext())
             iter.next().render(g);
         
         if(hailTimeLeft > 0)
         {
             
         }
         
         if(lightningTimeLeft > 0)
         {
             thunder.draw(thunderPos.x, thunderPos.y);
         }
     }
     
     private void createDude() throws SlickException
     {
         final Vector2f DUDE_START_POS = new Vector2f(-100, 250 + randomGenerator.nextInt(250));
         Dude thisguy = new Dude(DUDE_START_POS);
         dudes.add(thisguy);
     }
     
     @Override public void mousePressed(int button, int x, int y)
     {
         try {
             if(state == STATES.PLACE_COW_STATE)
                 placeCow(x);
             else if(state == STATES.PLACE_THUNDER_STATE)
                 shootThunder(x, y);
             else if (y > 30 && y < 130)
             {
                 if (x > 30 && x < 130) {
                     doCow();
                     this.boxes[0] = boximages[0][1];
                } else if (x > 130 && x < 230) {
                     doThunder();
                     this.boxes[1] = boximages[1][1];
                } else if (x > 230 && x < 330) {
                     doHailstorm();
                     this.boxes[2] = boximages[2][1];
                } else if (x > 330 && x < 430){ 
                     doAngelOfDeath();
                     this.boxes[3] = boximages[3][1];
                 }
             }       
         } catch (SlickException ex) {
             Logger.getLogger(GameState.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     
     @Override public void mouseReleased(int button, int x, int y)
     {
         this.boxes[0] = boximages[0][0];
         this.boxes[1] = boximages[1][0];
         this.boxes[2] = boximages[2][0];
         this.boxes[3] = boximages[3][0];
     }
         
     ////// event methods //////
     // functions called when the user presses one of the power buttons
     private void doCow() throws SlickException
     {
         if(can(POWER_COW))
             state = STATES.PLACE_COW_STATE;
         else
             state = STATES.NORMAL_STATE;
     }
     
     private void doThunder()
     {
         if(can(POWER_LIGHTNING))
             state = STATES.PLACE_THUNDER_STATE;
         else
             state = STATES.NORMAL_STATE;
     }
     
     private void doHailstorm()
     {
         state = STATES.NORMAL_STATE;
         if(can(POWER_HAILSTORM))
         {
             activateHail();
             activated(POWER_HAILSTORM);
         }
     }
     
     private void doAngelOfDeath()
     {
         state = STATES.NORMAL_STATE;
     }
     
     //this is called when the user chooses where to place the cow (i.e mouse click)
     private void placeCow(int xpos) throws SlickException
     {
         Cow moocow = new Cow(xpos); //cows go moo
         cows.add(moocow); //and now it gets to go join the herd
         state = STATES.NORMAL_STATE;
         activated(POWER_COW);
     }
     
     //this is called when they choose where to shoot thunder
     private void shootThunder(float xpos, float ypos)
     {
         state = STATES.NORMAL_STATE;
         activated(POWER_LIGHTNING);
         
         final float LIGHTNING_RADIUS = 120f;
         Vector2f pos = new Vector2f(xpos, ypos);
         Vector2f dpos = new Vector2f();
         thunderPos = pos.copy();
         thunderPos.add(new Vector2f(-75, -750+52));
         lightningTimeLeft = LIGHTNINGTIME;
                 
         for(Iterator<Dude> iter = dudes.iterator(); iter.hasNext(); )
         {
             Dude d = iter.next();
             dpos.set(d.size);
             dpos.scale(0.5f);
             dpos.add(d.pos);
             if(pos.distance(dpos) < LIGHTNING_RADIUS)
             {
                 iter.remove();
                 killed();
             }
         }
     }
     
     private void killed()
     {
         //a dude has been killed (oh noes!)
     }
     
     private void activateHail()
     {
         hailTimeLeft = HAILTIME;
         Dude.dudeSpeed = Dude.DUDE_SPEED_SLOW;
     }
     
     //when a cow collides with a dude
     private void cowCollide(Cow cow, Dude dude)
     {
         //cowcount or something? this is perhaps useless
     }    
 }
