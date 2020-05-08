 package game;
 import gameObjects.Barrier;
 
 
 import gameObjects.Enemy;
 import gameObjects.GameObjectData;
 import gameObjects.GameObjectFactory;
 import gameObjects.Player;
 
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.image.BufferedImage;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.List;
 
 import levelLoadSave.LevelLoader;
 import levelLoadSave.LoadObserver;
 import levelLoadSave.PlayerLoadObserver;
 import maps.Map;
 import movement.TargetedMovement;
 
 import com.golden.gamedev.Game;
 import com.golden.gamedev.object.Background;
 import com.golden.gamedev.object.PlayField;
 import com.golden.gamedev.object.Sprite;
 import com.golden.gamedev.object.SpriteGroup;
 import com.golden.gamedev.object.background.ImageBackground;
 
 
 public class TopDownDemo extends Game {
 	
 	private Player myPlayer;
 	private Enemy myEnemy;
 	private SpriteGroup myPlayerGroup;
 	private SpriteGroup myBarrierGroup;
 	private SpriteGroup myEnemyGroup;
 	private SpriteGroup myProjectileGroup;
 	private Background myBackground;
 	private PlayField myPlayfield;
 	
 	private BufferedImage myBackImage;
 	private Map myMap; 
 	
 	private List<LoadObserver> myLoadObservers;
 	
 
 	@Override
 	public void initResources() {	    
 	    myBarrierGroup = new SpriteGroup("barrier");
 	    myPlayerGroup = new SpriteGroup("player");
 	    myEnemyGroup = new SpriteGroup("enemy");
 	    myProjectileGroup = new SpriteGroup("projectile");
 	   
 	    //init background using the new Map class
 	    myBackImage = getImage("resources/Back2.png"); 
 	    myMap = new Map(myBackImage, getWidth(), getHeight()); 
 	    myMap.setSpeed(10);
 	    myBackground = myMap.getMyBack(); 
 	    myPlayerGroup.setBackground(myBackground);
 	    myBarrierGroup.setBackground(myBackground);
 	    myEnemyGroup.setBackground(myBackground);
 	    myProjectileGroup.setBackground(myBackground);
 	    
 	    
 	    
 	    
 	    //init playfield
 	    myPlayfield = new PlayField(myBackground);
 	    myPlayfield.addGroup(myPlayerGroup);
 	    myPlayfield.addGroup(myBarrierGroup);
 	    myPlayfield.addGroup(myEnemyGroup);
 	    myPlayfield.addGroup(myProjectileGroup);
 	    myPlayfield.addCollisionGroup(myPlayerGroup, myBarrierGroup, new PlayerBarrierCollision());
 	    
 	    //load level data
 	    myLoadObservers = new ArrayList<LoadObserver>();
 	    myLoadObservers.add(new PlayerLoadObserver(myPlayerGroup, this));
 	    myLoadObservers.add(new SimpleLoadObserver(myEnemyGroup));
 	    myLoadObservers.add(new SimpleLoadObserver(myBarrierGroup));
 	    LevelLoader l = new LevelLoader(myLoadObservers);
 	    l.loadLevelData("serializeTest.ser");
 	    
 //this is for testing enemy movement
 //		Enemy e = new Enemy (100, 2400, "resources/enemy.png");
 //		e.setImage(getImage(e.getImgPath()));
 //		myEnemy = e;
 //		myEnemyGroup.add(myEnemy);
 	}
 
 	@Override
 	public void render(Graphics2D pen) {
 		myPlayfield.render(pen);
 //		this is for testing enemy movement
 		//myEnemy.render(pen);
 	}
 
 	@Override
 	public void update(long elapsedTime) {
 		myMap.moveMap(elapsedTime); 
 		playerMovement();
 		myPlayfield.update(elapsedTime);
 	
 		
 // this is for testing enemy movement
 		//myEnemy.update();
 	}
 	
 	public void playerMovement(){
 		if (myPlayer.getX() > 0 && keyDown(java.awt.event.KeyEvent.VK_A)) {
 			myPlayer.moveX(-3);
 		}
 		if (myPlayer.getX() < getWidth()-myPlayer.getWidth()-3 && keyDown(java.awt.event.KeyEvent.VK_D)){
 			myPlayer.moveX(3);
 		}
 		if (myPlayer.getY() > myMap.getFrameHeight()  && keyDown(java.awt.event.KeyEvent.VK_W)){
 			myPlayer.moveY(-3);
 		}
 		if (myPlayer.getY()  < getHeight()-myPlayer.getHeight()-3 +myMap.getFrameHeight()&& keyDown(java.awt.event.KeyEvent.VK_S)){
 			myPlayer.moveY(3);
 		}
 	}
 	
 	public void setPlayer(Player g) {
 	    myPlayer = g;
 	}
 	
 //	public void loadLevelData() {
 ////	    //THIS IS IDEAL IMPLEMENTATION, BUT CAN'T USE YET BECAUSE MUST SET myPlayer, setImage(), etc.
 ////	    List<GameObjectFactory> allFactories = new ArrayList<GameObjectFactory>();
 ////	    allFactories.add(Player.getFactory());
 ////	    allFactories.add(Barrier.getFactory());
 ////	    allFactories.add(Enemy.getFactory());
 ////	    
 ////        //load level
 ////        LevelLoader l = new LevelLoader();
 ////        try {
 ////            List<GameObjectData> gameObjectDatas = l.load("savedLevel.json");
 ////            for (GameObjectData god : gameObjectDatas) {
 ////                for (GameObjectFactory factory : allFactories) {
 ////                    if (factory.isMyObject(god)) {
 ////                        factory.makeGameObject(god);
 ////                        break;
 ////                    }
 ////                }
 ////            }
 ////            
 ////        }
 ////        catch (FileNotFoundException e) {
 ////            e.printStackTrace();
 ////        }
 //	  
 //	  
 //      //load level
 //      LevelLoader l = new LevelLoader(myLoadObservers);
 //      l.loadLevelData("serializeTest.ser");
 //      
 //      try {
 //          List<GameObjectData> gameObjectDatas = l.jsonLoad("savedLevel.json");
 //          for (GameObjectData god : gameObjectDatas) {
 //              
 //              GameObjectFactory playerFactory = Player.getFactory();
 //              GameObjectFactory barrierFactory = Barrier.getFactory();
 //              GameObjectFactory enemyFactory = Enemy.getFactory();
 //              
 //              if (playerFactory.isMyObject(god)) {
 //                  myPlayer = (Player) playerFactory.makeGameObject(god);
 //                  myPlayer.setImage(getImage(myPlayer.getImgPath()));
 //                  //myPlayer.setSpeed(0, 20); 
 //                  myPlayerGroup.add(myPlayer);
 //               
 //              }
 //              if (barrierFactory.isMyObject(god)) {
 //                  Barrier b = (Barrier) barrierFactory.makeGameObject(god);
 //                  b.setImage(getImage(b.getImgPath()));
 //                  myBarrierGroup.add(b);
 //              }
 //              if (enemyFactory.isMyObject(god)) {
 //                  Enemy e = (Enemy) enemyFactory.makeGameObject(god);
 //                  e.setImage(getImage(e.getImgPath()));
 //                  myEnemyGroup.add(e);
 //              }
 //          }
 //          
 //      }
 //      catch (FileNotFoundException e) {
 //          e.printStackTrace();
 //      }
 //	}
 //	
 
 }
