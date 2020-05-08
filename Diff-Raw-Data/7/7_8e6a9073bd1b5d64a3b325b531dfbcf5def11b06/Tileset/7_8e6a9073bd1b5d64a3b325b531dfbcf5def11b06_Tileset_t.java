 /**
  * Copyright 2012 www.tilepacker.org
  */
 package org.tilepacker.core;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.imageout.ImageOut;
 
 /**
  * Stores a tileset
  * 
  * @author Thomas Cashman
  */
 public class Tileset {
 	public static int MAX_WIDTH;
 	public static int MAX_HEIGHT;
 	private List<Tile> tiles;
 
 	/**
 	 * Constructor
 	 */
 	public Tileset() {
 		tiles = new ArrayList<Tile>();
 	}
 
 	/**
 	 * Adds a tile to the tileset
 	 * @param tile The {@link Tile} to be added
 	 * @return True on success, false if the tileset is full
 	 */
 	public boolean add(Tile tile) {
 		if (tiles.size() + 1 > (getMaximumWidthInTiles() * getMaxiumumHeightInTiles()))
 			return false;
 		tiles.add(tile);
 		return true;
 	}
 
 	/**
 	 * Returns the maximum width in tiles
 	 * @return
 	 */
 	public int getMaximumWidthInTiles() {
 		return MAX_WIDTH / Tile.WIDTH;
 	}
 
 	/**
 	 * Returns the maximum height in tiles
 	 * @return
 	 */
 	public int getMaxiumumHeightInTiles() {
 		return MAX_HEIGHT / Tile.HEIGHT;
 	}
 
 	/**
 	 * Saves the tileset to an image
 	 * @param destinationFile The destination filepath
 	 * @param format The file format
 	 * @throws SlickException
 	 */
 	public void save(String destinationFile, String format)
 			throws SlickException {
 		Image tilesetImage = new Image(MAX_WIDTH, MAX_HEIGHT);
 		Graphics g = tilesetImage.getGraphics();
 		g.setColor(Color.magenta);
 		g.fillRect(0, 0, MAX_WIDTH, MAX_HEIGHT);
		g.setAntiAlias(true);
 		for (int i = 0; i < tiles.size(); i++) {
 			Tile tile = tiles.get(i);
 
 			if (tile != null) {
 				int y = i / (MAX_WIDTH / Tile.WIDTH);
 				int x = i - (y * (MAX_WIDTH / Tile.WIDTH));
 
 				g.drawImage(tile.getTileImage(), x * Tile.WIDTH, y
 						* Tile.HEIGHT);
 			}
 		}
 		g.flush();
 		ImageOut.write(tilesetImage, format, destinationFile);
 	}
 }
