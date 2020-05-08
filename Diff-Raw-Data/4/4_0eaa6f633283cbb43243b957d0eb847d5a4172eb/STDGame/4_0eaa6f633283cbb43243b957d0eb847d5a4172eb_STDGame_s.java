 package se.chalmers.tda367.std.gui;
 
 import org.newdawn.slick.BasicGame;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.SpriteSheet;
 
 import se.chalmers.tda367.std.core.GameBoard;
 import se.chalmers.tda367.std.core.tiles.BuildableTile;
 import se.chalmers.tda367.std.core.tiles.IBoardTile;
 import se.chalmers.tda367.std.core.tiles.IBuildableTile;
 import se.chalmers.tda367.std.core.tiles.PathTile;
 import se.chalmers.tda367.std.core.tiles.enemies.IEnemy;
 import se.chalmers.tda367.std.core.tiles.towers.BasicAttackTower;
 import se.chalmers.tda367.std.core.tiles.towers.ITower;
 import se.chalmers.tda367.std.utilities.Position;
 import se.chalmers.tda367.std.utilities.Sprite;
 
 /**
  * The main class containing the main method.
  * @author Emil Edholm
  * @date 2012-03-13
  */
 public final class STDGame extends BasicGame {
 	private Image background;
 	private Image pathTile;
 	private Image defaultTile;
 	private Image buildableTile;
 	private Image enemyTile;
 	private Image towerTile;
 	private GameBoard board;
 	private int tileScale = 32;
 	
 	SpriteSheet towerSprite;
 	
 	public STDGame(){
         super("STD - Shroom Tower Defense");
     }
 	
 	private String getResourcePath(String path){
 		return getClass().getResource(path).getPath();
 	}
 	
 	@Override
     public void init(GameContainer container) throws SlickException {
 		background = new Image(getResourcePath("/background.png"));
 		pathTile = new Image(getResourcePath("/path_tile.jpg"));
 		defaultTile = new Image(getResourcePath("/default_tile.jpg"));
 		buildableTile = new Image(getResourcePath("/buildable_tile.png"));
 		enemyTile = new Image(getResourcePath("/enemy.png"));
 		towerTile = new Image(getResourcePath("/tower_tile1.png"));
 		
 		board = new GameBoard(25,20, new Position(0,12), new Position (19,12));
		randomPlaceTile(board);
		placePath(board);
 	}
 
     @Override
     public void update(GameContainer container, int delta)
             throws SlickException {
     	
     }
     
     @Override
     public void mouseClicked(int button,
             int x,
             int y,
             int clickCount){
     	x = x / tileScale;
 		y = y / tileScale;
 		Position p = Position.valueOf(x, y);
 		
 		if(board.getTileAt(p) instanceof IBuildableTile)
 			board.placeTile(new BasicAttackTower(), p);
     }
 
     @Override
     public void render(GameContainer container, Graphics g)
             throws SlickException {
     	//background.draw(0, 0, 640, 480);
         g.drawString("STD - Tower Defense", 100, 10);
         
         int w = board.getWidth();
         int h = board.getHeight();
         for(int y = 0; y < h; y++){
         	for(int x = 0; x < w; x++){
         		IBoardTile tile = board.getTileAt(x, y);
         		int nX = x * tileScale;
         		int nY = y * tileScale;
         		if(tile instanceof PathTile){
         			pathTile.draw(nX, nY, tileScale, tileScale);
         		}
         		else if(tile instanceof BuildableTile){
         			buildableTile.draw(nX, nY, tileScale, tileScale);
         		}
         		else if(tile instanceof ITower){
         			towerTile.draw(nX, nY, tileScale, tileScale);
 
         		}
         		else if(tile instanceof IEnemy){
         			enemyTile.draw(nX, nY, tileScale, tileScale);
         		}
         		else
         			defaultTile.draw(nX, nY, tileScale, tileScale);
 
         	}
         }
     }
     
     
     
     
     
 	private static void placePath(GameBoard board) {
 		IBoardTile pathTile = new PathTile(new Sprite());
 		int y = (board.getHeight()/2)-1;
 		for (int i = 0; i < board.getWidth(); i++) {
 			board.placeTile(pathTile, new Position(i,y));
 			board.placeTile(pathTile, new Position(i,y+1));
 		}
 	}
 
 	private static void randomPlaceTile(GameBoard board) {
 		IBoardTile buildTile = new BuildableTile(new Sprite());
 		for (int y = 4; y < 17; y++) {
 			for (int x = 0; x < 20; x++) {
 				board.placeTile(buildTile, new Position(x,y));
 			}
 			
 		}
 	}
 }
