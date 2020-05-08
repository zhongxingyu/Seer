 package de.dakror.liturfaliarcest.game;
 
 import java.awt.Graphics2D;
 import java.util.HashMap;
 
 import de.dakror.gamesetup.GameFrame;
 import de.dakror.gamesetup.util.Helper;
 import de.dakror.liturfaliarcest.game.entity.creature.Player;
 import de.dakror.liturfaliarcest.game.entity.object.ObjectType;
 import de.dakror.liturfaliarcest.game.world.World;
 
 public class Game extends GameFrame
 {
 	public static Game currentGame;
 	public static World world;
 	public static Player player;
 	
 	public static HashMap<String, World> worlds;
 	
 	public String actionOnFade;
 	
 	public Game()
 	{
 		currentGame = this;
 	}
 	
 	@Override
 	public void initGame()
 	{
 		ObjectType.init();
 		
 		worlds = new HashMap<>();
		world = new World("Zu Hause");
		player = new Player(90, 400);
 		world.addEntity(player);
 		addLayer(world);
 	}
 	
 	public void setWorld(String map)
 	{
 		int index = layers.indexOf(world);
 		world = worlds.containsKey(map) ? worlds.get(map) : new World(map);
 		world.components.clear();
 		world.init();
 		layers.set(index, world);
 		world.addEntity(player);
 	}
 	
 	@Override
 	public void draw(Graphics2D g)
 	{
 		drawLayers(g);
 		
 		Helper.drawString("FPS: " + getFPS(), 10, 26, g, 18);
 		Helper.drawString("UPS: " + getUPS(), 10, 52, g, 18);
 	}
 }
