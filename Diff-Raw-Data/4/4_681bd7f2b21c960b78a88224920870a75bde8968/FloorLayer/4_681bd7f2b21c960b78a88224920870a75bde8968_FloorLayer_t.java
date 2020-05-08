 package nl.vaya.mobilegame.layer;
 
 import nl.vaya.mobilegame.tiles.TileObject;
 import nl.vaya.mobilegame.tiles.Water;
 
 import org.cocos2d.layers.CCLayer;
 import org.cocos2d.nodes.CCDirector;
 import org.cocos2d.nodes.CCSprite;
 import org.cocos2d.types.CGSize;
 
 public class FloorLayer extends CCLayer {
 	static final String logTag = "log_tag";
 	
 	protected int[] _tilePositionArray;
 	protected TileObject[] _tileArray;
 	
	protected float _numTilesWidth = 12;
	protected float _numTilesHeight = 8;
 	
 	protected CGSize winSize;
 	
 	public FloorLayer(){
 		winSize = CCDirector.sharedDirector().displaySize();
 		_tileArray = new TileObject[15];
 		_tilePositionArray = new int[] {
 				1,1,1,1
 		};
 		
 		_tileArray[1] = new Water(CCSprite.sprite("sand.png"));						//Tile object 1
 		
 		for(int i = 0; i< 2; i++){
 		//for(int i = 0; i< _tilePositionArray.length; i++){
 			float y = (int)(i/40);
 			float x = (int)i-(y*40);
 			
 			TileObject tile = _tileArray[1];
 			tile.setPosition(x,y);
 			this.addChild(tile.getTile());
 
 		}
 
 	}
 	
 	public void update(float dt){
 		//Log.i(logTag, "Update FloorLayer");
 	}
 }
