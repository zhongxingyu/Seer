 package indaprojekt;
 
 import java.awt.geom.Rectangle2D;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Queue;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Animation;
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.BasicGame;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 
 /**
  * Describes the actual game.
  */
 public class Game extends BasicGame
 {
 	private List<Player> players;
 	private List<Obstacle> obstacles;
 	private List<Entity> entities;
 	private UserInterface ui;
 	private Input input;
 	private Image background;
 	
     public Game()
     {
         super("Awesome Game");
     }
     
     private void setupWalls(GameContainer container) throws SlickException
     {
 		Image obstacleImage = new Image("res//images//isbit.png");
 		Obstacle obstacle = new Obstacle(100, 100, new Rectangle2D.Float(0, 0, obstacleImage.getWidth(), obstacleImage.getHeight()), 
 		new Animation(new Image[]{obstacleImage}, 1));
 		obstacles.add(obstacle);
 		entities.add(obstacle);
 		
 		int w = container.getWidth();
 		int h = container.getHeight();
 				
 		Image iceCube = new Image("res//images//isbit.png");
 		int cubeW = iceCube.getWidth();
 		int cubeH = iceCube.getHeight();
 		for (int i = 0; i < w; i += cubeW) {
 			Obstacle cube = new Obstacle(i, 0, new Rectangle2D.Float(0, 0, cubeW, cubeH), 
 											   new Animation(new Image[]{obstacleImage}, 1));
 			obstacles.add(cube);
 			entities.add(cube);
 		}
 		for (int i = 0; i < h; i += cubeH) {
 			Obstacle cube = new Obstacle(0, i, new Rectangle2D.Float(0, 0, cubeW, cubeH), 
 											   new Animation(new Image[]{obstacleImage}, 1));
 			obstacles.add(cube);
 			entities.add(cube);
 		}
 		for (int i = 0; i < w; i += cubeW) {
 			Obstacle cube = new Obstacle(i, h - cubeH, new Rectangle2D.Float(0, 0, cubeW, cubeH), 
 													   new Animation(new Image[]{obstacleImage}, 1));
 			obstacles.add(cube);
 			entities.add(cube);
 		}
 		for (int i = 0; i < h; i += cubeW) {
 			Obstacle cube = new Obstacle(w - cubeW, i, new Rectangle2D.Float(0, 0, cubeW, cubeH), 
 													   new Animation(new Image[]{obstacleImage}, 1));
 			obstacles.add(cube);
 			entities.add(cube);
 		}
     }
     
     /**
      * Sets up and adds all the entities of the map to the game
      */
     private void setupEntities(GameContainer container) throws SlickException
     {	
 		setupWalls(container);
     	
     	Player player1 = new PlayerOne(50, 50);
 		players.add(player1);
 		entities.add(player1);
 		
 		Player player2 = new PlayerTwo(150, 150);
 		players.add(player2);
 		entities.add(player2);
 		
 		background = new Image("res//images//bakgrund.png");
 		
 		Image itemImage = new Image("res//images//bomb2.png");
 		Item item = new Item(itemImage, 250, 250, new Rectangle2D.Float(0, 0, 25, 25));
 		entities.add(item);
 		
 		Image spdUpImage = new Image("res//images//Speed.png");
 		SpeedUp spdUp = new SpeedUp(spdUpImage, 300, 300, new Rectangle2D.Float(0, 0, 25, 25), 0.3f, 4000);
 		entities.add(spdUp);
     }
  
     @Override
     public void init(GameContainer gc) throws SlickException 
     {
     	players = new ArrayList<Player>(2);
     	obstacles = new ArrayList<Obstacle>();
     	entities = new LinkedList<Entity>();
 
     	ui = new DefaultUserInterface(gc.getWidth(), gc.getHeight());
     	
     	setupEntities(gc);
     	
     	input = gc.getInput();
     }
  
     @Override
     public void update(GameContainer gc, int delta) throws SlickException     
     {
     	for (Entity entity : entities) {
     		entity.doLogic(input, delta);
     	}
     	
     	{
 	    	Iterator<Player> iterator = players.iterator();
 	    	while (iterator.hasNext()) {
 	    		Player player = iterator.next();
 	    		if (player.isDead()) {
 	    			iterator.remove();
 	    			entities.remove(player);
 	    		}
 	    	}
     	}
     	
     	int length = (int)Math.ceil(entities.size()/2);
     	int i = 0;
     	for (Entity entity : entities) {
     		i++;
//    		if (i > length) {
//    			break;
//    		}
     		for (Entity entity2 : entities) {
     			if (entity != entity2) {
     				if (entity.isCollision(entity2)) {
     					entity.handleCollision(entity2);
    			//		entity2.handleCollision(entity);
     				}
     			}
     		}
     	}
     	
     	{
     		List<Entity> toAdd = new LinkedList<Entity>();
     		Entity spawn = null;
 	    	Iterator<Entity> iterator = entities.iterator();
 	    	while (iterator.hasNext()) {
 	    		Entity entity = iterator.next();
 	    		spawn = entity.spawnEntity();
 	    		if (spawn != null) {
 	    			toAdd.add(spawn);
 	    		}
 	    		if (entity.shouldBeRemoved()) {
 	    			iterator.remove();
 	    		}
 	    	}
 	    	for (Entity e : toAdd) {
 	    		entities.add(e);
 	    	}
     	}
 
     	ui.setPlayer1Lives(players.get(0).getLives());
     	
     	if (players.size() >= 2) {
     		ui.setPlayer2Lives(players.get(1).getLives());
     	}
     }
  
     @Override
     public void render(GameContainer gc, Graphics g) throws SlickException 
     {
     	background.draw();
     	
     	for (Entity entity : entities) {
     		entity.draw();
     	}
     	
     	ui.draw();
     }
  
     public static void main(String[] args) throws SlickException
     {	
          AppGameContainer app = new AppGameContainer(new Game());
          app.setDisplayMode(1000, 600, false);
          app.setVSync(true);
          app.setFullscreen(false);
          app.start();
     }	
 }
