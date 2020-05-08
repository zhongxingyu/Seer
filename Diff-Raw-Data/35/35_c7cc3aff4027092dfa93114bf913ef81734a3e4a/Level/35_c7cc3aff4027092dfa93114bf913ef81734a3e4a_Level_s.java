 package level;
 
 
 import gameCharacter.GameCharacter;
 
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.util.HashMap;
 import java.util.StringTokenizer;
 
 import player.Player;
 
 import utils.JsonUtil;
 import utils.Location;
 import utils.JsonUtil.JSONPlayer;
 
 import com.golden.gamedev.engine.BaseIO;
 import com.golden.gamedev.engine.BaseLoader;
 import com.golden.gamedev.engine.timer.SystemTimer;
 import com.golden.gamedev.object.PlayField;
 import com.golden.gamedev.object.background.abstraction.AbstractTileBackground;
 import com.golden.gamedev.util.FileUtil;
 import com.golden.gamedev.util.ImageUtil;
 import com.google.gson.Gson;
 
 //import enemy.Enemy;
 //import enemy.IEnemy;
 //import inventory.ItemSub;
 //import inventory.MakeItems;
 //
 //import scenery.Scenery;
 //import npc.NPC;
 import app.RPGame;
 
 public class Level extends AbstractTileBackground{
 	
 	public static final int TILE_WIDTH = 32, TILE_HEIGHT = 32;
 	
 	Chipset		chipsetE;
 	Chipset		chipsetF;
 	Chipset[] 	chipset;
 
 	int[][] layer1;			// the lower tiles
 	int[][]	layer2;			// the fringe tiles
 	
     //protected MakeItems MI;
 	protected RPGame game;
 	//protected HashMap<String, NPC> npcs = new HashMap<String, NPC>();
 	//protected HashMap<String, Enemy> enemies = new HashMap<String, Enemy>();
 	//protected HashMap<String, Scenery> scenery = new HashMap<String, Scenery>();
 	//protected HashMap<String, ItemSub> items = new HashMap<String, ItemSub>();
 	protected SystemTimer levelTimer = new SystemTimer();
 	protected long levelStartTime;
 	protected String nextLevelName;
 	protected String startText;
 	protected PlayField field;
 	
 	public Level(BaseLoader bsLoader, BaseIO bsIO, RPGame game, String levelFileName) {
 		super(0, 0, TILE_WIDTH, TILE_HEIGHT);
 		
 		this.game = game;
 		//MI = new MakeItems(game);
 		//addNPCs(); addItems(); addEnemies();
 		
 		layer1 = new int[40][25];
 		layer2 = new int[40][25];
 		
     	field = game.getField();
     	
 		//parse JSON files and set up player and npcs
     	Gson gson = new Gson();
    	JsonUtil.JSONLevel jsonLevel = gson.fromJson(levelFileName, JsonUtil.JSONLevel.class);
     	JSONPlayer jPlayer = jsonLevel.player;
     	Location playerLoc = new Location(jPlayer.location);
     	Player player = new Player(new GameCharacter(game, playerLoc, jPlayer.directionsURL), jPlayer.actionsURL);
     	field.add(player);
     	
 		
 		String[] lowerTile = FileUtil.fileRead(bsIO.getStream(jsonLevel.lowerFilename));
 		String[] upperTile = FileUtil.fileRead(bsIO.getStream(jsonLevel.upperFilename));
 		for (int j=0;j < layer1[0].length;j++) {
 			StringTokenizer lowerToken = new StringTokenizer(lowerTile[j]);
 			StringTokenizer upperToken = new StringTokenizer(upperTile[j]);
 			for (int i=0;i < layer1.length;i++) {
 				layer1[i][j] = Integer.parseInt(lowerToken.nextToken());
 				layer2[i][j] = Integer.parseInt(upperToken.nextToken());
 			}
 		}
 
 		// set the actual map size based on the read file
 		setSize(layer1.length, layer1[0].length);
 
 		chipsetE = new Chipset(bsLoader.getImages("rsc/level/ChipSet2.png", 6, 24, false));
 		chipsetF = new Chipset(bsLoader.getImages("rsc/level/ChipSet3.png", 6, 24));
 
 		chipset = new Chipset[16];
 		BufferedImage[] image = bsLoader.getImages("rsc/level/ChipSet1.png", 4, 4, false);
 		int[] chipnum = new int[] { 0,1,4,5,8,9,11,12,2,3,6,7,10,11,14,15 };
 		for (int i=0;i < chipset.length;i++) {
 			int num = chipnum[i];
 			BufferedImage[] chips = ImageUtil.splitImages(image[num], 3, 4);
 			chipset[i] = new Chipset(chips);
 		}
 		
 		levelTimer.setFPS(100);
 		levelTimer.startTimer();
 		levelStartTime = levelTimer.getTime();
 	}
 	
 //	public void addSceneryObject(Scenery scenery) {
 //		int[] loc = new int[]{
 //			game.getBG().getWidth()/2,
 //			game.getBG().getHeight()/2
 //		};
 //		scenery.add(loc, 0);
 //		scenery.generate();
 //	}
 	
 //	protected void addNPCs();
 //	protected void addItems();
 //	protected void addEnemies();
 	
 //	public NPC getNPC(String name) {
 //		return npcs.get(name);
 //	}
 //	
 //	public IEnemy getEnemy(String name) {
 //		return enemies.get(name);
 //	}
 //	
 //	public ItemSub getItem(String name) {
 //	    
 //		return items.get(name);
 //	}
 //	
 //	public void generate() {
 //		for (String name : npcs.keySet())
 //			npcs.get(name).generate();
 //		for (String name : scenery.keySet())
 //			scenery.get(name).generate();
 //		for (String name : items.keySet())
 //			items.get(name).generate();
 //		for (String name : enemies.keySet())
 //			enemies.get(name).generate();
 //	}
 //	
 //	public void update(long elapsed) {
 //		for (String name : enemies.keySet())
 //			enemies.get(name).act(elapsed);
 //	}
 	
 	public long getLevelTime() {
 		return levelTimer.getTime() - levelStartTime;
 	}
 	
 	
 //	public void endLevel(String nextLevel) {
 //		for (String name : npcs.keySet())
 //			npcs.get(name).getGroup().setActive(false);
 //		for (String name : scenery.keySet())
 //			scenery.get(name).getGroup().setActive(false);
 //		for (String name : items.keySet())
 //			items.get(name).getGroup().setActive(false);
 //		nextLevel(nextLevel);
 //	}
 	
 
 	public void nextLevel(String next){
 		
 	}
 
 	public void setStartText(String text){
 		startText = text;
 	}
 	
 	public void setNextLevel(String nextLevel){
 		nextLevelName = nextLevel;
 	}
 
 	public void render(Graphics2D g){
 		
 	}
 	
 	public void renderTile(Graphics2D g,
 							int tileX, int tileY,
 							int x, int y) {
 		// render layer 1
 		int tilenum = layer1[tileX][tileY];
 		if (tilenum < chipsetE.image.length) {
 			g.drawImage(chipsetE.image[tilenum], x, y, null);
 
 		} else if (tilenum >= chipsetE.image.length) {
 			BufferedImage image = chipset[tilenum-chipsetE.image.length].image[2];
 			g.drawImage(image, x, y, null);
 		}
 
 		// render layer 2
 		int tilenum2 = layer2[tileX][tileY];
 		if (tilenum2 != -1) {
 			g.drawImage(chipsetF.image[tilenum2], x, y, null);
 		}
 
 		// layer 3 is rendered by sprite group
 	}
 	
 	public boolean isOccupied(int tileX, int tileY) {
 		try {
 		    return (layer2[tileX][tileY] != -1);
 		} catch (Exception e) {
 			// out of bounds
 			return true;
 		} }
 	
 	// chipset is only a pack of images
 	class Chipset {
 		BufferedImage[] image;
 
 		public Chipset(BufferedImage[] image) {
 			this.image = image;
 		}
 
 	}
 }
