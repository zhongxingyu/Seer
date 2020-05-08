 package com.dat255_group3.utils;
 
 import com.badlogic.gdx.maps.tiled.TiledMap;
 import com.badlogic.gdx.maps.tiled.TiledMapTile;
 import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
 import com.badlogic.gdx.math.Vector2;
 import com.dat255_group3.model.MapList;
 
 
 /**
  * A class containing methods to detect positions from the tmx map.
  * @author group 3
  *
  */
 public class WorldUtil {
 	private Vector2 startPos;
 	private TiledMap map;
 	private MapList groundList;
 	private float finishLineX;
 	private MapList obstacleList;
 	private Vector2 tileSize;
 
 
 	/**
 	 * Class constructor specifying the map and creating lists.
 	 * @param tiledMap
 	 * 				The tiled map
 	 */
 	public WorldUtil(TiledMap tiledMap) {
 		this.map = tiledMap;
 		groundList = new MapList();
 		obstacleList = new MapList();
 		findTileSize();
 		addToLists();
 
 	}
 
 
 	/**
 	 * A method to find the size of the tiles
 	 */
 	public void findTileSize() {
 		TiledMapTileLayer layer = (TiledMapTileLayer)map.getLayers().get(0);
 		tileSize = new Vector2(layer.getTileHeight(), layer.getTileWidth());
 	}
 
 	/**
 	 * A method to get the size of the tiles
 	 * @return 
 	 * 		The size of the tile
 	 */
 	public Vector2 getTileSize() {
 		return tileSize;
 	}
 
 	/**
 	 * A method to get the list containing the positions of the obstacles
 	 * @return
 	 * 		The list with the obstacles positions
 	 */
 	public MapList getObstacleList() {
 		return obstacleList;
 	}
 
 
 	/**
 	 * A method to get the start position on the map for the character 
 	 * @return
 	 * 		The start position for the character	
 	 */
 	public Vector2 getStartPos() {
 		return startPos;
 	}
 
 	/**
 	 * A method to get a list containing all the positions of ground tiles in the map.
 	 * @return
 	 * 		A list of positions of the ground tiles
 	 */
 	public MapList getGroundList() {
 		return groundList;
 	}
 
 	/**
 	 * A method to get the list of all the positions for the finish line in the map
 	 * @return
 	 * 		A list of all positions of the finish line
 	 */
 	public float finishLineX() {
 		return finishLineX;
 	}
 
 
 	/**
 	 * A method to loop through the map layers and create lists of different kinds of positions
 	 */
 	private void addToLists() {
 		for(int i=0; i<map.getLayers().getCount(); i++) {
 			TiledMapTileLayer currentLayer = (TiledMapTileLayer)map.getLayers().get(i);
 			if(currentLayer.getName().equalsIgnoreCase("Solids")) {
 				for(int x=0; x<currentLayer.getWidth(); x++) {
 					for(int y=0; y<currentLayer.getHeight(); y++) {
 						if(currentLayer.getCell(x, y) != null) {
 							TiledMapTile tile = currentLayer.getCell(x, y).getTile();
 							if(tile.getProperties().containsKey("Ground")) {
								groundList.getMapList().add(new Vector2((x*tileSize.x)/2 + tileSize.x/2,y*tileSize.y));
 							}else if(tile.getProperties().containsKey("Obstacle")) {
								obstacleList.getMapList().add(new Vector2((x*tileSize.x)/2 + tileSize.x/2, y*tileSize.y));
 							}
 						}
 					}
 
 				}
 			}else if(currentLayer.getName().equalsIgnoreCase("Positions")) {
 				for(int x=0; x<currentLayer.getWidth(); x++) {
 					for(int y=0; y<currentLayer.getHeight(); y++) {
 						if(currentLayer.getCell(x, y) != null) {
 							TiledMapTile tile = currentLayer.getCell(x, y).getTile();
 							if(tile.getProperties().containsKey("FinishLine")) {
 								finishLineX = x*tileSize.x/2;
 							}else if(tile.getProperties().containsKey("StartPosition")) {
								startPos = new Vector2((x*tileSize.x)/2 + tileSize.x/2,y*tileSize.y);
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 }
 
