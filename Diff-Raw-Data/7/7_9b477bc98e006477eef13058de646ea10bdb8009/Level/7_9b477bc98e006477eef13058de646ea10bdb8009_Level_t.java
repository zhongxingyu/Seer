 package nl.tdegroot.games.nemesis.level;
 
 import nl.tdegroot.games.nemesis.Camera;
 import nl.tdegroot.games.nemesis.entity.Entity;
 import org.lwjgl.opengl.Display;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.tiled.TiledMap;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class Level {
 
 	private TiledMap map;
 	private final int tileSize;
 
 	private List<Entity> entities = new ArrayList<Entity>();
 
 	public Level(String path) throws SlickException {
 		map = new TiledMap(path);
 		tileSize = map.getTileWidth() & map.getTileHeight();
 		if (tileSize == 0) {
 			throw new SlickException("Tilewidth and Tileheight are not equal!");
 		}
 	}
 
 	public void update() {
 		for (int i = 0; i < entities.size(); i++) {
 			entities.get(i).update();
 		}
 	}
 
 	public void render(Graphics g, Camera camera) {
 		int x = (int) - (camera.getX() % tileSize) - tileSize;
 		int y = (int) - (camera.getY() % tileSize) - tileSize;
		int sx = (int) (camera.getX() / tileSize) -1;
		int sy = (int) (camera.getY() / tileSize) -1;
 		int sectionWidth = (Display.getWidth() / tileSize) + 3;
		int sectionHeight = (Display.getHeight() / tileSize) + 4;
 
 		map.render(x, y, sx, sy, sectionWidth, sectionHeight);
 
 		for (int i = 0; i < entities.size(); i++) {
 			entities.get(i).render(g, camera);
 		}
 	}
 
 	public void addEntity(Entity e) {
 		entities.add(e);
 	}
 
 	public TiledMap getMap() {
 		return map;
 	}
 
 	public float getPixelWidth() {
 		return map.getWidth() * map.getTileWidth();
 	}
 
 	public float getPixelHeight() {
 		return map.getHeight() * map.getTileHeight();
 	}
 
 }
