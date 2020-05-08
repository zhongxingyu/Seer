 package com.echoeight.oa;
 
 import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
 import static org.lwjgl.opengl.GL11.glClear;
 
 import java.awt.Font;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 
 import org.lwjgl.Sys;
 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 import org.lwjgl.opengl.Display;
 import org.lwjgl.opengl.GL11;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.UnicodeFont;
 import org.newdawn.slick.font.effects.ColorEffect;
 import org.newdawn.slick.opengl.Texture;
 import org.newdawn.slick.opengl.TextureLoader;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.util.ResourceLoader;
 
 import com.echoeight.oa.entities.Dude;
 import com.echoeight.oa.entities.Floor;
 import com.echoeight.oa.entities.Ladder;
 import com.echoeight.oa.entities.Mine;
 import com.echoeight.oa.entities.MoveableEntity;
 import com.echoeight.oa.entities.RedBullet;
 import com.echoeight.oa.entities.RedGrenade;
 import com.echoeight.oa.entities.Tank;
 import com.echoeight.oa.entities.TankGrenade;
 import com.echoeight.oa.entities.MoveableEntity.Gun;
 import com.echoeight.oa.images.LoadTextures;
 import com.echoeight.oa.util.Fullscreen;
 
 public class LevelOneState extends BasicGameState {
 	
 	private long lastFrame;
     
 	//UnicodeFont font;
 	
 	Image background = null;
 	
 	UnicodeFont font;
 	
 	Texture bg;
 	
 	public ArrayList<RedBullet> rbullets = new ArrayList<RedBullet>();
 	public ArrayList<RedGrenade> rgrenades = new ArrayList<RedGrenade>();
 	
 	public ArrayList<Gun> availguns = new ArrayList<Gun>();
 
 	public ArrayList<Ladder> ladders = new ArrayList<Ladder>();
 	
 	public ArrayList<Tank> tanks = new ArrayList<Tank>();
 	public ArrayList<Tank> tanksrem = new ArrayList<Tank>();
 	
 	private int smgammo;
 	
 	public ArrayList<Mine> mines = new ArrayList<Mine>();
 	public ArrayList<Mine> minesrem = new ArrayList<Mine>();
 	
 	public static ArrayList<Floor> floors = new ArrayList<Floor>();
 
 	public ArrayList<RedGrenade> rgrenadesrem = new ArrayList<RedGrenade>();
 	public ArrayList<RedBullet> rbulletsrem = new ArrayList<RedBullet>();
 	
 	public ArrayList<TankGrenade> tbullet = new ArrayList<TankGrenade>();
 	public ArrayList<TankGrenade> tbulletrem = new ArrayList<TankGrenade>();
 	
 	ColorEffect colorEffect;
 	
     private boolean jumping = false;
     private boolean onLadder = false;
     private boolean isDead = false;
     private boolean isFalling = false; 
     
     private double jumptop;
     private double jumpinitial;
     
     public String worldname = "level1";
 
     
     public int lives;
 
     public MoveableEntity man;
     
     private long getTime() {
             return (Sys.getTime() * 1000) / Sys.getTimerResolution();
     }
 
     private int getDelta() {
             long currentTime = getTime();
             int delta = (int) (currentTime - lastFrame);
             lastFrame = getTime();
             return delta;
     }
           
             
     static int WIDTH = 640;
     static int HEIGHT = 480;
 	
     public boolean isRunning = true;
 
  	private int smgbulletint = 0;
 
 	@Override
 	public void init(GameContainer arg0, StateBasedGame arg1) throws SlickException {
 		initGL(WIDTH, HEIGHT);  			 
 		LoadTextures.LoadAll();
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public void render(GameContainer arg0, StateBasedGame arg1, Graphics arg2)
 			throws SlickException {
 		 if(worldname.equals("level1")){
 			 availguns.clear();
 			 LevelOne();
 			 availguns.add(Gun.PISTOL);			 
 		 }
 	        man = new Dude(-100, HEIGHT-162, 25, 49);
 	        
 				Font awtFont = new Font("Arial", Font.BOLD, 24);
 //				try {
 //					InputStream inputStream	= ResourceLoader.getResourceAsStream("res/font.ttf");	 
 //					awtFont = Font.createFont(Font.TRUETYPE_FONT, inputStream);
 //					font = new UnicodeFont(awtFont, 24, false, false);
 //					colorEffect = new ColorEffect();
 //					   font.getEffects().add(colorEffect);
 //					   font.addAsciiGlyphs();
 //					   font.loadGlyphs();
 //				} catch (Exception e) {
 //					e.printStackTrace();
 //				}
 				
 		try {
 			InputStream inputStream	= ResourceLoader.getResourceAsStream("res/font.ttf");	 
 			awtFont = Font.createFont(Font.TRUETYPE_FONT, inputStream);
 			font = new UnicodeFont(awtFont, 24, false, false);
 			   colorEffect = new ColorEffect();
 			   font.getEffects().add(colorEffect);
 			   font.addAsciiGlyphs();
 			   font.loadGlyphs();
 		} catch (Exception e) {
 			   e.printStackTrace();
 		}
 		
 	}
 
 	@Override
 	public void update(GameContainer arg0, StateBasedGame arg1, int arg2)
 			throws SlickException {
 		 while (!Display.isCloseRequested()) {	
 			 Color.white.bind();
 			 //tankLogic(man);TODO: finish tanks!
 			 handleInput();
 			 
 			 if(isFalling){
 				 isFalling = false;
 			 }
 		   	 for(Ladder lad : ladders){
 				 if(man.intersects(lad)){
 					 onLadder = true;
 				 }
 			 }
 		   	 
 			 if(isDead){
 				 reset();
 				 man.setX(-100);
 				 man.setY(HEIGHT-162);
 				 isDead = false;
 				 for(Mine min : mines){
 					 if(min.isExploded()){
 						 min.explode(false);
 					 }
 				 }
 			 }
 			 
 			 intersects(man);
 			 hideMouse();
 			
              if(isFalling && !(jumping) && !(onLadder)){
             	 man.setY(man.getY() + 8);
              }
              if(onLadder){
             	 onLadder = false;
              }
              //TODO: tweak grenades
              for(RedGrenade gre : rgrenades){
             	 if(gre.getY() <= gre.getInitialY() - 50){
             		 if(Dude.manflip){
             			 gre.setDX(-0.5);
             		 }else{
             			 gre.setDX(0.5);
             		 }
             		 gre.setDY(0.6);
             	 }
              }         
              
              //jump code
              if(jumping){
 	             if(man.getY() < jumptop){
 	            	 man.setDY(0.25);
 	             }
 	             if(man.getY() > jumpinitial){
 	            	 man.setDY(0);
 	            	 man.setY(jumpinitial);
 	            	 jumping = false;
 	             }
              }
             
              glClear(GL_COLOR_BUFFER_BIT);
          	 for(RedBullet bul : rbullets){
          		 if(bul.getX() < -120 || bul.getX() > 530){
          			 bul.setDX(0);
          			 bul.setDY(0);
          			 rbulletsrem.add(bul);
          		 }
          	 }
          	 
          	 
          	 for(RedGrenade gre : rgrenades){
          		 if(gre.getY() > HEIGHT-115){
          			 gre.setDX(0);
          			 gre.setDY(0);
          			 rgrenadesrem.add(gre);
          		 }
          	 }
          	
          	 for(RedGrenade gre : rgrenadesrem){
          		rgrenades.remove(gre);
          	 }
              
              for(Mine min : mines){
             	 if(min.isExploded()){
             		 minesrem.add(min);
             	 }
             	 if(man.intersects(min)){
             		 min.explode(true);
             		 isDead = true;
             	 }
             	  for(RedGrenade bul : rgrenades){
                  	 if(bul.intersects(min)){
                  		 min.explode(true);
                  	 }
                   }
             	  for(RedBullet bul : rbullets){
                   	 if(bul.intersects(min)){
                   		 min.explode(true);
                   	 }
                    }
              }
              int distance = (int) (HEIGHT-115 - man.getY());
              if(distance <= 46){
                 man.setY(HEIGHT-162);
              }
                           
              //draw
              
              cleanUpEntities();
              
              for(Tank tank : tanks){
             	 tank.draw(isTankFlipped(tank));
              }
              
              for(Floor flor : floors){
             	 flor.draw(false);
              }
           	 
              for(Ladder lad : ladders){
               	lad.draw(false);
              }
              
              man.draw(Dude.manflip);
 
              for(RedBullet bul : rbullets){
             	 bul.draw(false);
              }
              for(RedGrenade bul : rgrenades){
             	 bul.draw(false);
              }      	 
              for(Mine min : mines){
             	 min.draw(false);
              }       	
              
              //update
              int delta = getDelta();
              
              for(RedBullet bul : rbullets){
             	 bul.update(delta);
              }
              for(RedGrenade bul : rgrenades){
             	 bul.update(delta);
              }          
              for(Mine min : mines){
             	 min.update(delta);
              }         
              for(Ladder lad : ladders){
              	lad.update(delta);
              }
              for(Tank tank : tanks){
             	 tank.update(delta);
              }            
              man.update(delta);
              for(Floor flor : floors){
             	 flor.update(getDelta());
              }
             // drawBackground();
              drawAmmo();
              Display.update();
              Display.sync(60);
 		}
 		 destroyDisplay();
 	}
 
     private void destroyDisplay() {
 		Display.destroy();
 		System.exit(0);		
 	}
 
 
 	int stateID = 1;
     
     LevelOneState( int stateID ) 
     {
        this.stateID = stateID;
     }
  
     @Override
     public int getID() {
         return stateID;
     }
 
 	private void LevelOne() {
 			
 			smgammo = 100;
 			
 			floors.add(new Floor(-100, HEIGHT-115, WIDTH, 8));
 	        floors.add(new Floor(-100, HEIGHT-240, 292, 8));
 	        floors.add(new Floor(230, HEIGHT-150, 350, 8));
 	        
 	        ladders.add(new Ladder(200, HEIGHT-144, 22, 30));
 	        ladders.add(new Ladder(200, HEIGHT-172, 22, 30));
 	        ladders.add(new Ladder(200, HEIGHT-200, 22, 30));
 	        ladders.add(new Ladder(200, HEIGHT-228, 22, 30));
 	        ladders.add(new Ladder(200, HEIGHT-256, 22, 30));
 	        
 	        mines.add(new Mine(10, HEIGHT-137, 24, 23));
 	        
 	        //tanks.add(new Tank(100, HEIGHT-266, 70, 28));
 	}
 
 	private void initGL(int width, int height) {
 
 		GL11.glEnable(GL11.GL_TEXTURE_2D);
 		GL11.glShadeModel(GL11.GL_SMOOTH);        
 		GL11.glDisable(GL11.GL_DEPTH_TEST);
 		GL11.glDisable(GL11.GL_LIGHTING);                    
  
 		GL11.glClearColor(255f, 255f, 255f, 0f);                
         GL11.glClearDepth(1);                                       
  
         GL11.glEnable(GL11.GL_BLEND);
         GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
  
         GL11.glViewport(0,0,width,height);
 		GL11.glMatrixMode(GL11.GL_MODELVIEW);
  
 		GL11.glMatrixMode(GL11.GL_PROJECTION);
 		GL11.glLoadIdentity();
 		GL11.glOrtho(0, width, height, 0, 1, -1);
 		GL11.glMatrixMode(GL11.GL_MODELVIEW);
 		
 		
 		
 		
 	}
 
 	private void drawAmmo() {
 		Color.white.bind();
 		font.drawString(100, 100, "Ammo: " + smgammo, Color.black);
 	}
 
 	private void reset(){
 		 removeEntities();
 		 availguns.clear();
 		 LevelOne();
 		 availguns.add(Gun.PISTOL);
 	}
 
 	private void intersects(MoveableEntity man) {
 		for(Tank tank : tanks){
 			if(man.intersects(tank)){
 				isDead = true;
 				tanksrem.add(tank);
 				return;
 			}
 			if(isTankFlipped(tank)){
 				tank.setX(tank.getX() - 1.2);
 			}else{
 				tank.setX(tank.getX() + 1.2);
 			}
 		}
 		if(man.getX() < -100){
 			man.setX(-100);
 		}else if(man.getX() > 516){
 			man.setX(516);
 		}
 		isFalling = true;
 		 for(Floor flor : floors){
 			if(man.onFloor(man,flor)){
 				isFalling = false;
 			}
 			 if(man.intersects(flor) && !man.onFloor(man,flor)){
 				 if(Dude.manflip){
 					 man.setX(man.getX() + 4);
 				 }else{
 					 man.setX(man.getX() - 4);
 				 }
 			 }
 			 for(RedBullet bul : rbullets){
 				 if(bul.intersects(flor)){
 					 rbulletsrem.add(bul);
 				 }
 			 }
 			 for(RedGrenade gre : rgrenades){
 				 if(gre.intersects(flor)){
 					 rgrenadesrem.add(gre);
 				 }
 			 }
 		 }
 	}
 
 	private void hideMouse() {
 		if(Mouse.isInsideWindow()){
             Mouse.setGrabbed(true);
        }else{
             Mouse.setGrabbed(false);
        }		
 	}
 
 	public boolean isOnLadder(MoveableEntity man) {
 
 		for (Ladder lad: ladders) {
 
 			if (man.intersects(lad)) {
 
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public void cleanUpEntities(){
    	 for(RedBullet bul : rbulletsrem){
    		 rbullets.remove(bul);
    	 }
    	 rbulletsrem.clear();
     	 for(Mine min : minesrem){
     		 mines.remove(min);
     	 }
     	 minesrem.clear();
     	 for(RedGrenade gre : rgrenadesrem){
     		 rgrenades.remove(gre);
     	 }
     	 rgrenadesrem.clear();
     	 for(Tank tank : tanksrem){
     		 tanks.remove(tank);
     	 }
     	 tanksrem.clear();
 	}
 
 	public boolean isTankFlipped(Tank tank){
 		boolean tankOnFloor = false;
 		for(Floor flor : floors){
 			if(tank.onFloor(tank,  flor)){
 				tankOnFloor = true;
 			}
 			if(tank.intersects(flor) && !tank.onFloor(tank, flor)){
 				if(tank.getY() > tank.getBaseX()){
 					tank.setLast(true);
 					return true;
 				}else{
 					tank.setLast(false);
 					return false;
 				}
 			}
 			if(tank.onFloor(tank, flor) && (tank.getX()+(tank.getWidth())) > (flor.getX()+(flor.getWidth()))){
 				tank.setLast(true);
 				return true;
 			}else if(tank.onFloor(tank, flor) && (tank.getX()-(tank.getWidth())) < (flor.getX()-(flor.getWidth()))){
 				tank.setLast(false);
 				return false;
 			}
 		}
 		if(!tankOnFloor){
 			if(tank.getY() > tank.getBaseX()){
 				tank.setLast(true);
 				return true;
 			}else{
 				tank.setLast(false);
 				return false;
 			}
 		}
     	if(tank.getX() < -100){
     		tank.setLast(false);
     		return false;
     	}else if(tank.getX() > 505){
     		tank.setLast(true);
     		return true;
     	}
     	return tank.getLast();
 	}
 
 	private void removeEntities(){
 		 for(RedBullet bul : rbullets){
 			rbulletsrem.add(bul);
 		 }
    	 for(RedBullet bul : rbulletsrem){
    		 rbullets.remove(bul);
    	 }
    	 rbulletsrem.clear();
     	 for(RedGrenade gre : rgrenades){
     		 rgrenadesrem.add(gre);
     	 }
     	 for(Mine min : mines){
     		 minesrem.add(min);
     	 }
     	 for(Mine min : minesrem){
     		 mines.remove(min);
     	 }
     	 minesrem.clear();
     	 for(RedGrenade gre : rgrenadesrem){
     		 rgrenades.remove(gre);
     	 }
     	 rgrenadesrem.clear();
     	 for(Tank tank : tanks){
     		 tanksrem.add(tank);
     	 }
     	 for(Tank tank : tanksrem){
     		 tanks.remove(tank);
     	 }
     	 tanksrem.clear();
     	 ArrayList<Ladder> laddersrem = new ArrayList<Ladder>();
     	 for(Ladder lad : ladders){
     		 laddersrem.add(lad);
     	 }
     	 for(Ladder lad : laddersrem){
     		 ladders.remove(lad);
     	 }
     	 laddersrem.clear();
     	 ArrayList<Floor> floorsrem = new ArrayList<Floor>();
     	 for(Floor flor : floors){
     		 floorsrem.add(flor);
     	 }
     	 for(Floor flor : floorsrem){
     		 floors.remove(flor);
     	 }
     	 floorsrem.clear();
 	}
 	
 
 	/*	
 	private void tankLogic(MoveableEntity man) {
         for(TankGrenade gre : tbullet){
         	System.out.println("test");
 	       	if(gre.isFlipped()){
 	       		gre.setDX(-0.5);
 	       	}else{
 	       		gre.setDX(0.5);
 	       	}
         }
 		for(Tank tank : tanks){
 			for(int i=0;i<30;i++){
 				if((tank.getY()+i == man.getY() && isTankFlipped(tank)) || (tank.getY()-i == man.getY() && !isTankFlipped(tank))){
 					i=30;
 						if(isTankFlipped(tank)){
 							System.out.println("FIRE1!");
 							TankGrenade bul = new TankGrenade(true, 0, 0, 8, 8);
 		    	        	bul.setX(tank.getX());
 							bul.draw(false);
 							bul.update(getDelta());
 						}else{
 							System.out.println("FIRE2!");
 		    	        	TankGrenade bul = new TankGrenade(false, 0, 0, 8, 8);
 		    	        	bul.setX(tank.getX());
 							bul.draw(false);
 							bul.update(getDelta());
 		    	        	//bul.setDX(0.1);
 						}
 //					if(isTankFlipped(tank)){
 //						for(int x=0;x<100;x++){
 //							if(tank.getX()-x == man.getX()){
 //								System.out.println("shooting 1");
 //								return;
 //							}
 //						}
 //					}else{
 //						for(int x=0;x<100;x++){
 //							if(tank.getX()+x == man.getX()){
 //								System.out.println("shooting 2");
 //								return;
 //							}
 //						}
 //					}
 				}
 			}
 		}
 	}
 */
 	
 	public void handleInput() {
 		 if(Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)){
 				destroyDisplay();
 		 }
 	     if(Keyboard.isKeyDown(Keyboard.KEY_UP)){
      	 for(Ladder lad : ladders){
      		 if(man.intersects(lad)){
      			 man.setDY(0);
      			 man.setY(man.getY() - 1);
      			 if(jumping){
  	            	 man.setDY(0);
  	            	 jumping = false;
      			 }
      		 }
      	 }
 	     }
 	     if(Keyboard.isKeyDown(Keyboard.KEY_DOWN)){
      	 for(Ladder lad : ladders){
      		 if(man.intersects(lad)){
      			 man.setDY(0);
      			 man.setY(man.getY() + 2);
      			 if(jumping){
  	            	 man.setDY(0);
  	            	 jumping = false;
      			 }
      		 }
      	 }
 	     }
       if(Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
      	 if(!jumping){
      		 man.setX(man.getX() + 4);
      	 }else{
      		 man.setX(man.getX() + 8);
      	 }
           Dude.manflip = false;
       }
       if(Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
      	 if(!jumping){
      		 man.setX(man.getX() - 4);
      	 }else{
      		 man.setX(man.getX() - 8);
      	 }
           Dude.manflip = true;
       }
       if(Dude.currentGun == Gun.SMG){
           if(Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
          	if (!isOnLadder(man)) {
          		if(Dude.hasgun && smgammo > 0){
         			if (smgbulletint == 0) {
      					smgammo = smgammo - 1;
         				smgbulletint = 10;
          				RedBullet bul = new RedBullet(0, 0, 8, 8);
          				if(Dude.manflip){
          					bul.setX(man.getX());
          				}else{
          					bul.setX(man.getX()+30);
          				}
          				bul.setY(man.getY() + 18);
          				if(Dude.manflip){
          					bul.setDX(-0.4);
          				}else{
          					bul.setDX(0.4);
          				}
          				rbullets.add(bul);
          			}
          			else {
         				smgbulletint = smgbulletint-1;
          			}
 	     	    }
  	        }
           }  
       }             
       while (Keyboard.next()) {
      	 if (Keyboard.getEventKey() == Keyboard.KEY_F11) {
          	    if (Keyboard.getEventKeyState()) {
          	    	if(Fullscreen.fullscreen){
          	    		Fullscreen.fullscreen = false;
          	    		Fullscreen.setDisplayMode(640, 480, false);
          	    	}else{
          	    		Fullscreen.fullscreen = true;
          	    		Fullscreen.setDisplayMode(640, 480, true);
          	    	}
          	    }
      	 }  
           if (Keyboard.getEventKey() == Keyboard.KEY_G) {
          	    if (Keyboard.getEventKeyState()) {
          	        if(Dude.hasgun){
          	        	if(man.getGun() == Gun.PISTOL && availguns.contains(Gun.SMG)){
          	        		man.setGun(Gun.SMG);
          	        	}else if(man.getGun() == Gun.SMG && availguns.contains(Gun.GRENADE)){
          	        		man.setGun(Gun.GRENADE);
          	        	}else{
          	        		man.setGun(Gun.NONE);
          	        		Dude.hasgun = false;
          	        	}
          	        	Dude.currentGun = man.getGun();
          	        }else{            	        	
          	        	if(man.getGun() == Gun.NONE && availguns.contains(Gun.PISTOL)){
          	        		Dude.hasgun = true;
          	        		man.setGun(Gun.SMG);//TODO chnage guns here
          	        		Dude.currentGun = man.getGun();
          	        	}
          	        }
          	    }
           }
           if(Dude.currentGun == Gun.PISTOL || Dude.currentGun == Gun.GRENADE){
 	             if (Keyboard.getEventKey() == Keyboard.KEY_SPACE) {
 	            	    if (Keyboard.getEventKeyState()) {
      	        		if (!isOnLadder(man)) {
      	        			if(Dude.hasgun){
      	        				if(Dude.currentGun == Gun.PISTOL){
      	        					RedBullet bul = new RedBullet(0, 0, 8, 8);
 	            	        			if(Dude.manflip){
 	            	        				bul.setX(man.getX());
 	            	        			}else{
 	            	        				bul.setX(man.getX()+30);
 	            	        			}
 	            	        			bul.setY(man.getY() + 18);
 	            	        			if(Dude.manflip){
 	            	        				bul.setDX(-0.4);
 	            	        			}else{
 	            	        				bul.setDX(0.4);
 	            	        			}
 	            	        			rbullets.add(bul);
      	        				}else if(Dude.currentGun == Gun.GRENADE){
      	        					if (rgrenades.size() < 1) {
      	        						RedGrenade gre = new RedGrenade(man.getY() + 13, 0, 0, 8, 8);
      	        						if(Dude.manflip){
      	        							gre.setX(man.getX());
      	        						}else{
      	        							gre.setX(man.getX()+30);
      	        						}
      	        						gre.setY(man.getY() + 13);
      	        						if(Dude.manflip){
      	        							gre.setDX(-0.2);
      	        						}else{
      	        							gre.setDX(0.2);
      	        						}
      	        						gre.setDY(-0.3);
      	        						rgrenades.add(gre);	
      	        					}
      	        				}
 	            	        	}
 	            	        }
 	            	    }
 	             }
           }	             
           if (Keyboard.getEventKey() == Keyboard.KEY_UP) {
          	 if(!(jumping) && !(onLadder) && !(isFalling)){
          	    if (Keyboard.getEventKeyState()) {
 	            	 	jumping = true;
 	            	    jumptop =  man.getY()-60;
 	            	    jumpinitial = man.getY();
          	    	man.setDY(-0.4);
          	    }
          	 }
           }
       }
 	}
 	
 	public void drawBackground(){
     	try {
     		if(bg != null)
     		GL11.glDeleteTextures(bg.getTextureID());
     		bg = TextureLoader.getTexture("PNG", ResourceLoader.getResourceAsStream("res/menu.png"));
     	} catch (IOException e) {
     		e.printStackTrace();
     	}
         bg.bind();
         GL11.glLoadIdentity();
         GL11.glTranslated(-100, -100, 0);
     	GL11.glBegin(GL11.GL_QUADS);
 			GL11.glTexCoord2f(0,0);
 			GL11.glVertex2f(100,100);
 			GL11.glTexCoord2f(1,0);
 			GL11.glVertex2d(640+100,100);
 			GL11.glTexCoord2f(1,1);
 			GL11.glVertex2d(640+100,100+480);
 			GL11.glTexCoord2f(0,1);
 			GL11.glVertex2f(100,100+480);
 		GL11.glEnd();
 		 GL11.glLoadIdentity();
 	}
 	
 }
