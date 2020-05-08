 package com.vesalaakso.rbb.view;
 
 import org.newdawn.slick.tiled.TiledMap;
 
 import com.vesalaakso.rbb.model.Camera;
 import com.vesalaakso.rbb.model.TileMap;
 
 /**
  * Handles the drawing of the tile map.
  */
 public abstract class TileMapPainter implements Painter {
 
 	/** The {@link TileMap} we want to draw. */
 	private TileMap map;
 
 	/**
 	 * An empty constructor with no access modifier to prevent other than
 	 * the classes in this package instantiating this class.
 	 */
 	TileMapPainter() {
 	}
 
 	/**
 	 * Sets the {@link TileMap} that will be drawn when {@link #paint} gets
 	 * called.
 	 * 
 	 * @param map
 	 *            the new {@link TileMap} that is to be drawn
 	 */
 	public void setMap(TileMap map) {
 		this.map = map;
 	}
 
 	/**
	 * Gets the current {@link TileMap} that is to be drawn.
 	 * 
	 * @return current {@link TileMap} that is to be drawn.
 	 */
 	protected TileMap getMap() {
 		return map;
 	}
 
 	/**
 	 * Draws the given layer of the map.
 	 * 
 	 * @param layer
 	 *            which layer should be drawn
 	 * @param cam
 	 *            a Camera that can be consulted to get information about the
 	 *            location one will draw stuff to
 	 */
 	protected void drawLayer(int layer, Camera cam) {
 		TiledMap tmap = map.getTiledMap();
 
 		// Calculate the top left tile that will be drawn first
 		int camTileX = (int) cam.getX() / TileMap.TILE_SIZE;
 		int camTileY = (int) cam.getY() / TileMap.TILE_SIZE;
 
 		// How many pixels will the camera be offset
 		int camOffsetX = (int) (camTileX * TileMap.TILE_SIZE - cam.getX());
 		int camOffsetY = (int) (camTileY * TileMap.TILE_SIZE - cam.getY());
 
 		// Drawing it now.
 		tmap.render(camOffsetX, camOffsetY, camTileX, camTileY,
 				map.getWidthInTiles() + 3, map.getHeightInTiles() + 3,
 				layer, false);
 	}
 
 	/**
 	 * @see Painter#isDrawnToWorldCoordinates
 	 * 
 	 * @return <code>false</code>, tile maps are always drawn precisely.
 	 */
 	@Override
 	public boolean isDrawnToWorldCoordinates() {
 		return false;
 	}
 
 }
