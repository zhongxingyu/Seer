 package beatbots.simulation;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Stack;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Vector2f;
 import org.newdawn.slick.tiled.TiledMap;
 
 public strictfp final class BeatBotManager {
 	
 	private BeatMachine beatMachine;
 	private BulletManager bulletManager;
 	private BeatTokenManager beatTokenManager;
 	
 	private String mapFilePath;
 	
 	private List<BeatBot> beatBots;
 	private Stack<BeatBot> beatBotsToRemove;
 	
 	public BeatBotManager(BeatMachine beatMachine, BulletManager bulletManager, BeatTokenManager beatTokenManager, String mapFilePath) {
 		
 		super();
 		
 		this.beatMachine = beatMachine;
 		this.bulletManager = bulletManager;
 		this.beatTokenManager = beatTokenManager;
 		
 		this.mapFilePath = mapFilePath;
 		
 		this.beatBots = new ArrayList<BeatBot>();
 		this.beatBotsToRemove = new Stack<BeatBot>();
 	}
 	
 	public void init(GameContainer gameContainer) throws SlickException {
 		
 		TiledMap tiledMap = new TiledMap(this.mapFilePath);
 		
 		for (int l = 0; l < tiledMap.getLayerCount(); l++) {
 			
 			for (int x = 0; x < tiledMap.getWidth(); x++) {
 				
 				for (int y = 0; y < tiledMap.getHeight(); y++) {
 					
 					int tileId = tiledMap.getTileId(x, y, l);
 					
 					int beatId = Integer.parseInt(tiledMap.getTileProperty(tileId, "Beat", "-1"));
 					
 					switch (beatId) {
 					
 					case 0: 
 						
 						this.spawn(gameContainer, Beat.None, new Vector2f((x + 0.5f) * tiledMap.getTileWidth(), (y - tiledMap.getHeight() + 15.5f) * 32f));
 						
 						break;
 						
 					case 1: 
 						
 						this.spawn(gameContainer, Beat.Red, new Vector2f((x + 0.5f) * tiledMap.getTileWidth(), (y - tiledMap.getHeight() + 15.5f) * 32f));
						
 						break;
 					}
 				}
 			}
 		}
 	}
 	
 	public void update(GameContainer gameContainer, int delta) {
 		
 		for (BeatBot i : this.beatBots) {
 			
 			if (i.isActive()) {
 				
 				i.update(gameContainer, delta);
 			}
 			else {
 				
 				this.beatBotsToRemove.add(i);
 			}
 		}
 		
 		while (!this.beatBotsToRemove.isEmpty()) {
 			
 			this.beatBots.remove(this.beatBotsToRemove.pop());
 		}
 	}
 	
 	public void render(GameContainer gameContainer, Graphics graphics) {
 		
 		for (BeatBot i : this.beatBots) {
 			
 			if (i.isActive()) {
 				
 				i.render(gameContainer, graphics);
 			}
 		}
 	}
 	
 	public void spawn(GameContainer gameContainer, Beat beatBotType, Vector2f startPosition) throws SlickException {
 		
 		switch (beatBotType) {
 		
 		case None:
 			
 			BeatBotNone beatBotNone = new BeatBotNone(this.beatMachine, this.bulletManager, startPosition);
 			
 			this.beatBots.add(beatBotNone);
 			
 			beatBotNone.init(gameContainer);
 			
 			break;
 			
 		case Red: 
 			
 			BeatBotRed beatBotRed = new BeatBotRed(this.beatMachine, this.bulletManager, startPosition, this.beatTokenManager);
 			
 			this.beatBots.add(beatBotRed);
 			
 			beatBotRed.init(gameContainer);
 			
 			break;
 		}
 	}
 }
