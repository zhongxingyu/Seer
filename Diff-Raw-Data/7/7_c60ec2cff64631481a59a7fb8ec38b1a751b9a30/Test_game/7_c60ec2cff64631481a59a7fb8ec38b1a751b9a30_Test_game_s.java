 package HUD.test;
 import hudDisplay.GraphicItem;
 import hudDisplay.HeadsUpDisplay;
 import hudDisplay.NumberStat;
import hudDisplay.Stat;
 import hudDisplay.TextItem;
 import input.InputManager;

 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.event.KeyEvent;
 import java.util.Map;

 import sprites.Chris_TestSprite;
import sprites.GeneralSprite;
 import States.State;
 
 import com.golden.gamedev.Game;
 import com.golden.gamedev.object.CollisionManager;
 import com.golden.gamedev.object.GameFont;
 import com.golden.gamedev.object.PlayField;
 import com.golden.gamedev.object.Sprite;
 import com.golden.gamedev.object.SpriteGroup;
 import com.golden.gamedev.object.background.ColorBackground;
 import com.golden.gamedev.object.collision.BasicCollisionGroup;
 
 import core.EventManager;
 
 public class Test_game extends Game {
 	Chris_TestSprite s1;
 	Map<String, State> stateMap;
 	PlayField playfield;
 	CollisionManager collisionTypeWall;
 	HeadsUpDisplay HUD;
 	GameFont scoreFont;
 	
 	
 
 
 	
 	public void initResources() {
 		playfield = new PlayField();
 		playfield.setBackground(new ColorBackground(Color.LIGHT_GRAY, 1200, 900));
 		
 		HUD = new HeadsUpDisplay(getImage("images/EmptyHUD.png"),0,0);
 		
 		 s1 = new Chris_TestSprite();
 	        //BufferedImage[] images = new BufferedImage[1];
 	        //	images[0] = ;
 	        s1.setImage(getImage("images/mario1.png"));
 	        s1.setLocation(300, 200);
 	        
 		s1.createStat("health", new NumberStat(300));
 		s1.createStat("mana", new NumberStat(300));
 		s1.createStat("score", new NumberStat(0));
 		
 		SpriteGroup character = new SpriteGroup("character");
         character.add(s1);
         
 		GraphicItem healthbar = new GraphicItem(getImage("images/healthBar.png", false),10,10, s1.getStat("health"));
 		HUD.addGraphicItem(healthbar);
 		
 		GraphicItem manabar = new GraphicItem(getImage("images/healthBar2.png", false),200,10, s1.getStat("mana"));
 		manabar.Follow(s1);
 		HUD.addGraphicItem(manabar);
 		
 	
 
 		scoreFont = fontManager.getFont(getImages("images/Score_Font.png", 8,12));
 		TextItem Score = new TextItem(scoreFont, 400, 10,s1.getStat("score"));
 		HUD.addTextItem(Score);
 
 		
 		
 
 
 		Sprite wall1 = new Sprite(getImage("images/block.png"));
 		wall1.setLocation(350,400);
 		Sprite wall2 = new Sprite(getImage("images/block.png"));
 		wall2.setLocation(300,400);
 		Sprite wall3 = new Sprite(getImage("images/block.png"));
 		wall3.setLocation(200,400);
 		Sprite wall4 = new Sprite(getImage("images/block.png"));
 		wall4.setLocation(250,400);
 
 
 		SpriteGroup walls = new SpriteGroup("walls");
 		walls.add(wall1);
 		walls.add(wall2);
 		walls.add(wall3);
 		walls.add(wall4);
 		
 		
 		collisionTypeWall = new WallCollision();
 		collisionTypeWall.setCollisionGroup(character, walls);
 		
 		playfield.addGroup(character);
 		playfield.addGroup(walls);
 		
 
 
 		
 		
 		
 	}
 	
 	public void render(Graphics2D arg0) {
 	playfield.render(arg0);
 	collisionTypeWall.checkCollision();
 	HUD.render(arg0);
 	}
 	
 	protected void initEngine() {
 		super.initEngine();
 		this.bsInput = new InputManager(this.bsGraphics.getComponent());
 	}
 	
 	public void update(long elapsedTime) {
 		
 		//Cutscene Code
 //		if(cutTimer.action(elapsedTime)) {
 //			trigger.triggerCutscene();
 //			cutTimer.setActive(false);
 //		}
 //		cutscene.update(elapsedTime);
 		
 		EventManager.getEventManager().update(elapsedTime);
 		playfield.update(elapsedTime);
 		HUD.update(elapsedTime);		
 		
 		if (keyDown(KeyEvent.VK_LEFT))
 		{
 			EventManager.getEventManager().sendEvent("Left");
 		}
 		if (keyDown(KeyEvent.VK_RIGHT))
 		{
 			EventManager.getEventManager().sendEvent("Right");
 		}
 		if (keyDown(KeyEvent.VK_UP))
 		{
 			EventManager.getEventManager().sendEvent("Up");	
 		}
 		if (keyDown(KeyEvent.VK_DOWN))
 		{
 			EventManager.getEventManager().sendEvent("Down");	
 		}
 	}
 	
 	class WallCollision extends BasicCollisionGroup {
 
 	    public WallCollision() {
 	    	pixelPerfectCollision = true;
 	    }
 
 	    public void collided(Sprite s1, Sprite s2) {
 	    	EventManager.getEventManager().sendEvent("floor collide");
 	    	EventManager.getEventManager().sendEvent("got hit");
 //	    	EventManager.getEventManager().sendEvent("switchstates");
 	    	
 	    }
 
 	}
 }
