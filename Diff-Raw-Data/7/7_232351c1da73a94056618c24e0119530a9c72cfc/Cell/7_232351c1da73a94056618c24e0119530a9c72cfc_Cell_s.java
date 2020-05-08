 package map;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.PriorityQueue;
 import java.util.Set;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.StateBasedGame;
 import org.newdawn.slick.tiled.GroupObject;
 import org.newdawn.slick.tiled.Layer;
 import org.newdawn.slick.tiled.ObjectGroup;
 import org.newdawn.slick.tiled.TiledMap;
 
 import utils.LayerRenderable;
 import entities.Entity;
 import entities.enemies.Enemy;
 import entities.objects.Door;
 import entities.objects.DoorTrigger;
 import entities.objects.JumpPlatform;
 import entities.objects.LeafTest;
 import entities.players.Player;
 import game.config.Config;
 
 
 public class Cell extends TiledMap{
 
 	private final Tile[][] properties = new Tile[getHeight()][getWidth()];
 	private final Set<Entity> defaultEntities = new HashSet<Entity>();
 	private final Set<Entity> entities = new HashSet<Entity>();
 	private final Set<Entity> entitiesToRemove = new HashSet<Entity>();
 	private final Set<Entity> entitiesToAdd = new HashSet<Entity>();
     private Player player;
 			
 	public Cell(String location) throws SlickException {
 		super(location);
 		loadProperties();
 		loadDefaultEntities();
 	}
 	
 	public void loadDefaultEntities(){
 		entities.clear();
 		if(defaultEntities.isEmpty()){
 			Map<String,Door> doors = new HashMap<String,Door>();
 			Map<String,DoorTrigger> triggers = new HashMap<String,DoorTrigger>();
 			
 			for(ObjectGroup og : super.objectGroups){
 				for(GroupObject go : og.objects){
 					int x = go.x / Config.getTileSize();
 					int y = go.y / Config.getTileSize();
 					if(go.type.equalsIgnoreCase("enemy")){
 						defaultEntities.add(Enemy.getNewEnemy(this, go.name, x,y));
 					}else if(go.type.equalsIgnoreCase("door")){
 						if(triggers.containsKey(go.name)){
 							defaultEntities.add(new Door(this,triggers.remove(go.name),x,y));
 						}else{
 							Door d = new Door(this,null,x,y);
 							doors.put(go.name,d);
 							defaultEntities.add(d);
 						}
 					}else if(go.type.equalsIgnoreCase("doorTrigger")){
 						if(doors.containsKey(go.name)){
 							defaultEntities.add(new DoorTrigger(doors.remove(go.name),x,y));
 						}else{
 							DoorTrigger dt = new DoorTrigger(null, x, y);
 							triggers.put(go.name, dt);
 							defaultEntities.add(dt);
 						}
 					}else if(go.type.equalsIgnoreCase("leafTest")){
 						defaultEntities.add(new LeafTest(x,y));
 					} else if(go.type.equalsIgnoreCase("jumpPlatform")){
 						defaultEntities.add(new JumpPlatform(x,y));
 					}
 				}
 			}
 		}
 		for(Entity e : defaultEntities){
 			addEntity(e.clone());
 		}
 	}
 	
	
 	public Tile getTile(int x, int y) {
 		return properties[y][x];
 	}
 	
 	
 	/**
 	 * Extracts the properties of each cell in the map to a
 	 * Tile.
 	 * (This method only pulls out "blocked" and "enemy" properties
 	 *  for the moment. Add more as needed.)
 	 * PRE: 0 <= x < width / 0 <= y < height
 	 * @return 
 	 */
 	private void loadProperties(){
 		//go through all tiles in map
 		for (int xAxis = 0; xAxis < width; xAxis++) { 
 			for (int yAxis = 0; yAxis < height; yAxis++) {
 				properties[yAxis][xAxis] = new Tile(getTileId(xAxis, yAxis, 0));
 				properties[yAxis][xAxis].parseTileProperties(this);
 			}
 		}
 	}
 	
 	
 	public void addEntity(Entity newEntity) {
 		entitiesToAdd.add(newEntity);
 	}
 	
 	public void render(GameContainer gc, StateBasedGame sbg, Graphics g) {
 		//super.render(-Config.getTileSize(),-Config.getTileSize());
 		PriorityQueue<LayerRenderable> orderedLayers = new PriorityQueue<LayerRenderable>();
 		orderedLayers.addAll(entities);
 		for(Layer l : layers){
 			try{
 				orderedLayers.add(new LayeredLayer(l));
 			}catch(NoSuchFieldException e){
 				System.out.println(e.getMessage());
 				for(int i=0;i<l.height-2;i++){
 					l.render(0, 0, 1, 1, l.width-2, i, false, Config.getTileSize(), Config.getTileSize());
 				}
 			}
 		}
 		while(!orderedLayers.isEmpty()){
 			orderedLayers.poll().render(gc, sbg, g);
 		}
 		
 	}
 	
 	public void clearEntities() {
 		entitiesToRemove.addAll(entities);
 	}
 	
 	public Set<Entity> getEntities() {
 	    return entities;
 	}
 	
 	public void updateEntities(GameContainer gc, StateBasedGame sbg, int delta){
 		entitiesToRemove.clear();
 		entities.addAll(entitiesToAdd);
 		entitiesToAdd.clear();
 		for(Entity e : entities){
 			e.update(gc, sbg, delta);
 			for (Entity e2 : entities){
				if (e.intersects(e2) && !e.equals(e2)){
 					e.collide(e2);
 				}
 			}
 		}
 		entities.removeAll(entitiesToRemove);
 	}
     
     public void removeEntity(Entity e) {
         entitiesToRemove.add(e);
     }
     
     public void setPlayer(Player player) {
         this.player = player;
     }
     
     public Player getPlayer() {
         return player;
     }
 
 }
