 package rajola.test;
 
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.BasicGame;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 
 import rajola.pipeline.TileMap;
import rajola.pipeline.sprites.SpriteSheet;
 
 public class BasicPlaformerTest extends BasicGame {
 
	protected SpriteSheet spriteSheet;
 	protected TileMap map;
 	
 	public BasicPlaformerTest() {
 		super("Rajola - Basic platformer integration test");
 	}
 	
 	public static void main(String args[]) throws SlickException {
 		AppGameContainer gc = new AppGameContainer(new BasicPlaformerTest());
 		gc.setDisplayMode(800, 600, false);
 		gc.start();
 	}
 
 	@Override
 	public void render(GameContainer gc, Graphics g) throws SlickException {
 		
 	}
 
 	@Override
 	public void update(GameContainer gc, int d) throws SlickException {
 		
 	}
 
 	@Override
 	public void init(GameContainer gc) throws SlickException {
		spriteSheet = new SpriteSheet("res/tileset.jpg", 32);
 		map = new TileMap();
 		
 	}
 	
 }
