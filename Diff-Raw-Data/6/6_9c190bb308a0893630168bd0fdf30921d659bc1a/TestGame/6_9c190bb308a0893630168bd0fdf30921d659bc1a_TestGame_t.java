 package cutscenes.test;
 
 import input.InputManager;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.event.KeyEvent;
 import java.io.FileNotFoundException;
 import java.util.Map;
 
 import sprites.Chris_TestSprite;
 import sprites.Mike_TestSprite;
 import sprites.WalkingBadGuy;
 import States.State;
 
 import com.golden.gamedev.Game;
 import com.golden.gamedev.object.CollisionManager;
 import com.golden.gamedev.object.PlayField;
 import com.golden.gamedev.object.Sprite;
 import com.golden.gamedev.object.SpriteGroup;
 import com.golden.gamedev.object.Timer;
 import com.golden.gamedev.object.background.ColorBackground;
 import com.golden.gamedev.object.collision.AdvanceCollisionGroup;
 import com.golden.gamedev.object.collision.BasicCollisionGroup;
 
 import core.Condition;
 import core.EventManager;
 import core.conditions.DelayedCondition;
 import core.conditions.TimedCutsceneCondition;
 import cutscenes.BadFileFormatException;
 import cutscenes.Cutscene;
 import cutscenes.CutsceneAutomation;
 import cutscenes.EventAutomation;
 import sprites.Character;
 import sprites.GeneralSprite;
 
 
 public class TestGame extends Game{
     Sprite s1;
     Map<String, State> stateMap;
     PlayField playfield;
     CollisionManager collisionTypeWall;
     CollisionManager collisionTypeBlocker;
     CollisionManager cutsceneTrigger;
     Cutscene myCutscene;
     Timer cutTimer;
     Timer endTimer;
 
 
     protected void initEngine() {
 		super.initEngine();
 		this.bsInput = new InputManager(this.bsGraphics.getComponent());
 	}
 
     public void initResources() {
         //		stateMap = new HashMap<String, State>();
         playfield = new PlayField();
         playfield.setBackground(new ColorBackground(Color.LIGHT_GRAY, 1200, 900));
 
         s1 = new Mike_TestSprite();
         //BufferedImage[] images = new BufferedImage[1];
         //	images[0] = ;
         s1.setImage(getImage("images/mario1.png"));
         s1.setLocation(300, 200);
         SpriteGroup character = new SpriteGroup("character");
         character.add(s1);
 
         //added by Ben
         Sprite enemy1 = new WalkingBadGuy ();
         enemy1.setImage(getImage("images/thebadguy.png"));
         enemy1.setLocation(350,200);
         enemy1.setMovement(0.025, 270);
 
 
         Sprite blocker1 = new Sprite(getImage("images/block.png"));
         Sprite blocker2 = new Sprite(getImage("images/block.png"));
         Sprite blocker3 = new Sprite(getImage("images/block.png"));
         Sprite blocker4 = new Sprite(getImage("images/block.png"));
 
         blocker1.setLocation(200, 200);
         blocker2.setLocation(500,200);
         blocker3.setLocation(490,100);
         blocker4.setLocation(210,100);
         //
 
 
         Sprite wall1 = new Sprite(getImage("images/block.png"));
         wall1.setLocation(350,400);
         Sprite wall2 = new Sprite(getImage("images/block.png"));
         wall2.setLocation(300,400);
         Sprite wall3 = new Sprite(getImage("images/block.png"));
         wall3.setLocation(200,400);
         Sprite wall4 = new Sprite(getImage("images/block.png"));
         wall4.setLocation(250,400);
 
         //added by Ben
         SpriteGroup enemies = new SpriteGroup("enemies");
         SpriteGroup blockers = new SpriteGroup("blockers");
         enemies.add(enemy1);
         blockers.add(blocker1);
         blockers.add(blocker2);
         blockers.add(blocker3);
         blockers.add(blocker4);
         //
 
         SpriteGroup walls = new SpriteGroup("walls");
         walls.add(wall1);
         walls.add(wall2);
         walls.add(wall3);
         walls.add(wall4);
 
         //added by Ben
         collisionTypeBlocker = new CantGoFurtherCollision();
         collisionTypeBlocker.setCollisionGroup(enemies, blockers);
         //
         collisionTypeWall = new WallCollision();
         collisionTypeWall.setCollisionGroup(character, walls);
         
         cutsceneTrigger = new CutsceneTriggerCollision();
         cutsceneTrigger.setCollisionGroup(character, blockers);
         
 
         playfield.addGroup(character);
         playfield.addGroup(walls);
 
         //added by Ben
         playfield.addGroup(enemies);
         playfield.addGroup(blockers);
         //
         
         
         
         //Cutscene Code - from Mike
         EventAutomation automation = null;
         EventAutomation automation2 = null;
         try {
 			automation = new CutsceneAutomation("src/cutscenes/test/testCutsceneScript.script");
 			automation2 = new CutsceneAutomation("src/cutscenes/test/testCutsceneScript2.script");
		} catch (FileNotFoundException  e) {
 			e.printStackTrace();
 		}
        catch(BadFileFormatException e){
        	e.printStackTrace();
        }
         
         automation.addTransition(new DelayedCondition(2000), automation2);
         myCutscene = new Cutscene(automation, "start-cutscene","end-cutscene");
         cutTimer = new Timer(10);
         endTimer = new Timer(5000);
         endTimer.setActive(false);
     }
 
     public void render(Graphics2D arg0) {
         playfield.render(arg0);
         collisionTypeWall.checkCollision();
         //added by Ben
         collisionTypeBlocker.checkCollision();
         cutsceneTrigger.checkCollision();
         //	HUD.render(arg0);
     }
 
     public void update(long elapsedTime) {
 //        Cutscene Code
 //		if(cutTimer.action(elapsedTime)) {
 //			EventManager.getEventManager().sendEvent("start-cutscene");
 //			cutTimer.setActive(false);
 //		}
 		if(endTimer.action(elapsedTime)) {
 			EventManager.getEventManager().sendEvent("end-cutscene");
 			endTimer.setActive(false);
 		}
 		myCutscene.update(elapsedTime);
 
         EventManager.getEventManager().update(elapsedTime);
         playfield.update(elapsedTime);
         //		HUD.update(elapsedTime);
 
 
 
 //        if (keyDown(KeyEvent.VK_LEFT))
 //        {
 //            EventManager.getEventManager().sendEvent("Left");
 //        }
 //        if (keyDown(KeyEvent.VK_RIGHT))
 //        {
 //            EventManager.getEventManager().sendEvent("Right");
 //        }
 //        if (keyDown(KeyEvent.VK_UP))
 //        {
 //            EventManager.getEventManager().sendEvent("Up");	
 //        }
 //        if (keyDown(KeyEvent.VK_DOWN))
 //        {
 //            EventManager.getEventManager().sendEvent("Down");
 //        }
     }
     
     
     class CutsceneTriggerCollision extends BasicCollisionGroup {
     	
     	public CutsceneTriggerCollision() {
     		pixelPerfectCollision = true;
     	}
     	
 		@Override
 		public void collided(Sprite arg0, Sprite arg1) {
 			EventManager.getEventManager().sendEvent("start-cutscene");
 			endTimer = new Timer(5000);
 		}
     	
     }
     
     class WallCollision extends BasicCollisionGroup {
 
         public WallCollision() {
             pixelPerfectCollision = true;
         }
 
         public void collided(Sprite s1, Sprite s2) {
             EventManager.getEventManager().sendEvent("floor collide");
             EventManager.getEventManager().sendEvent("switchstates");
 
 
         }
 
     }
 
     class CantGoFurtherCollision extends AdvanceCollisionGroup{
         public CantGoFurtherCollision(){
             pixelPerfectCollision = true;
         }
 
         @Override
         public void collided(Sprite s1, Sprite s2) {
             if(getCollisionSide()==1){
                 EventManager.getEventManager().sendEvent("walk right");
             }
             else if( getCollisionSide()==2){
                 EventManager.getEventManager().sendEvent("walk up");
             }
             else if ( getCollisionSide()== 4){
                 EventManager.getEventManager().sendEvent("walk left");
             }
             else EventManager.getEventManager().sendEvent("walk down");
 
 
 
         }
     }
 
 
 
 }
