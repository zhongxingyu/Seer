 package server;
 
 import java.util.Random;
 
 public class TileMap {
 	private Tile[][] tileMap;
 	private float tileHeight = 30;
 	private float tileWidth = 30;
 	//random starting pos atm.
 	//private static int[] START_TYLE = {1,1};
 	
 	public TileMap(int[][] mapArray){
 		this.tileMap = Util.buildMap(this, mapArray);
 	}
 
 	public float getTileHeight() {
 		return tileHeight;
 	}
 
 	public void setTileHeight(float tileHeight) {
 		this.tileHeight = tileHeight;
 	}
 
 	public float getTileWidth() {
 		return tileWidth;
 	}
 
 	public void setTileWidth(float tileWidth) {
 		this.tileWidth = tileWidth;
 	}
 	
 	public Tile getTileXY(float x, float y){
 		Tile tile;
 		try{
 			tile = tileMap[(int) (y/tileHeight)][(int) (x/tileWidth)];
 		}catch(IndexOutOfBoundsException e){
 			tile = new Tile(this, (int) (x/tileWidth), (int) (y/tileHeight), false);
 		}
 		return tile;
 	}
 	
 	public Tile getTile(int x, int y){
 		Tile tile;
 		try{
 			tile = tileMap[y][x];
 		}catch(IndexOutOfBoundsException e){
 			tile = new Tile(this, x, y, false);
 		}
 		return tile;
 	}
 	
 	public Tile getRandomWalkableTile(){
 		Tile tile = null;
 		while(tile == null){
 			Random rnd = new Random();
 			int y = rnd.nextInt(this.getHeight());
 			int x = rnd.nextInt(this.getWidth());
 			if(getTile(x, y).isWalkable())
 				tile = tileMap[y][x];
 		}
 		return tile;
 	}
 	
 	public int getHeight(){
 		return tileMap.length;
 	}
 	
 	public int getWidth(){
 		return tileMap[0].length;
 	}
 
 	public float[] getStartXY() {
 		//float[] xy = {START_TYLE[1]*tileHeight, START_TYLE[1]*tileWidth};
 		Tile randomWalk = this.getRandomWalkableTile();
		float[] xy = {randomWalk.getY()*tileHeight, randomWalk.getX()*tileWidth};
 		return xy;
 	}
 }
