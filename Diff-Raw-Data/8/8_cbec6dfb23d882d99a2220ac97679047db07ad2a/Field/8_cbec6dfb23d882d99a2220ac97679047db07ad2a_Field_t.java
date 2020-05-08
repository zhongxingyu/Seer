 /**
  * Copyright (c) 2011-2012 Henning Funke.
  * 
  * This file is part of Battlepath.
  *
  * Battlepath is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
 
  * Battlepath is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package engine;
 
 import java.awt.Point;
 import java.util.ArrayList;
 
 import util.Line2D;
 import util.Util;
 import util.Vector2D;
 
 /**
  * A field stores the level information. It is separated in square Tiles.
  * Tile with index (0,0) is bottom left, (tilesX-1,tilesY-1) is top right.
  * At this point a Tile with value 0 is empty and a tile with value 1 an obstacle.
  * Vector2D is used for world coordinates.
  */
 public class Field {
 	private Tile tiles[][];
 	private int tilesX=0;
 	private int tilesY=0;
 	private ArrayList<Line2D> boundingFrame = new ArrayList<Line2D>();
 	private String name;
 	
 	/**
 	 * 
 	 * @param numX number of X tiles
 	 * @param numY number of Y tiles
 	 */
 	public Field(int numX, int numY, String title) {
 		tiles = new Tile[numX][numY];
 		tilesX = numX;
 		tilesY = numY;
 		name = title;
 		
 		for(int x=0;x<tilesX;x++) {
 			for(int y=0;y<tilesY;y++) {
 				tiles[x][y] = new Tile(new Point(x,y),0);
 			}
 		}
 		calcBoundingFrame();
 	}
 	
 	/**
 	 * Creates a circle of value 1 tiles in a field
 	 * @param pos world position of the circle
 	 * @param r radius of the circle
 	 */
 	public void createCircle(Vector2D pos, double r) {
		Point topleft = tileIndexAt(new Vector2D(pos.x-r, pos.y-r));
		Point bottomright = tileIndexAt(new Vector2D(pos.x+r, pos.y+r));
		clamp(topleft);
		clamp(bottomright);
		for(int x=topleft.x; x<bottomright.x; x++) {
			for(int y=topleft.y; y<bottomright.y; y++) {
 				//System.out.println(pos.distance(getWorldPos(new Point(x,y))));
 				if(pos.distance(getWorldPos(new Point(x,y))) < r)
 					tiles[x][y].setValue(1);
 			}
 		}
 	}
 	
 	/**
 	 * Clamps given Point to field size
 	 * @param p Point to modify
 	 */
 	public void clamp(Point p) {
 		if(p.x >= tilesX) p.x = tilesX-1;
 		if(p.x < 0) p.x = 0;
 		if(p.y >= tilesY) p.y = tilesY-1;
 		if(p.y < 0) p.y = 0;
 	}
 	
 	/**
 	 * Recalculate bounding frame, order is clockwise to fit collision detection
 	 */
 	private void calcBoundingFrame() {
 		boundingFrame.clear();
 		double sx = tilesX;
 		double sy = tilesY;
 		boundingFrame.add(new Line2D(new Vector2D(0,sy), new Vector2D(0,0)));
 		boundingFrame.add(new Line2D(new Vector2D(0,0), new Vector2D(sx,0)));
 		boundingFrame.add(new Line2D(new Vector2D(sx,0), new Vector2D(sx,sy)));
 		boundingFrame.add(new Line2D(new Vector2D(sx,sy), new Vector2D(0,sy)));
 	}
 
 	/**
 	 * Returns bounding frame, order is clockwise to fit collision detection
 	 * @return set of lines representing the frame
 	 */
 	public ArrayList<Line2D> getBoundingFrame() {
 		return boundingFrame;
 	}
 	
 	/**
 	 * Gives the world position of the center of a Tile
 	 * @param pos index of Tile
 	 * @return 
 	 */
 	public Vector2D getWorldPos(Point pos) {
 		return new Vector2D(
 				(double)pos.x+0.5,
 				(double)pos.y+0.5);
 	}
 	
 	/**
 	 * Sets type of the tile. 0 is free and 1 is an obstacle.
 	 * @param pos world position of tile to set
 	 * @param value tile value (see above)
 	 */
 	public void setTile(Vector2D pos, int value) {
 		Point p = tileIndexAt(pos);
 		tiles[p.x][p.y].setValue(value);
 	}
 	
 	/**
 	 * Gives the index of a tile at the given world position
 	 * @param pos given position
 	 * @return tile index
 	 */
 	public Point tileIndexAt(Vector2D pos) {
 		return new Point(
 				(int)(pos.x()),
 				(int)(pos.y()));
 	}
 	
 	/**
 	 * Returns Tile object of the tile at the given world position
 	 * @param pos given position
 	 * @return Tile object
 	 */
 	public Tile tileAt(Vector2D pos) {
 		Point p = tileIndexAt(pos);
 		return tileAt(p);
 	}
 	
 	/**
 	 * Returns Tile object of the tile at the given index
 	 * @param index given index in Field
 	 * @return Tile object
 	 */
 	public Tile tileAt(Point index) {
 		if(Util.isValueInBounds(0, index.x, tilesX-1) &&
 			Util.isValueInBounds(0, index.y, tilesY-1)) {
 			return tiles[index.x][index.y];
 		}
 		else return null;
 	}
 	
 	/**
 	 * Wrapper for dumbasses
 	 */
 	public Tile tileAt(int x,int y) {
 		return tileAt(new Point(x,y));
 	}
 	
 	/**
 	 * Gives the value of a tile at the given world position
 	 * @param pos given position
 	 * @return tile value
 	 */
 	public int tileValueAt(Vector2D pos) {
 		Point tile = tileIndexAt(pos);
 		if(tile.x>=tilesX || tile.x < 0
 		 ||tile.y>=tilesY || tile.y < 0)
 			return 1;
 		return tiles[tile.x][tile.y].getValue();
 	}
 	
 	/**
 	 * Returns a list of the neighbours of the tile specified by world position
 	 * @param pos world position
 	 * @return list of neighbours of that tile
 	 */
 	public ArrayList<Vector2D> neighbours(Vector2D pos) {
 		ArrayList<Vector2D> result = new ArrayList<Vector2D>();
 		
 		Point tile = tileIndexAt(pos);
 		
 		Vector2D left,right,top,bottom;
 		left = getWorldPos(new Point(tile.x-1,tile.y));
 		right = getWorldPos(new Point(tile.x+1,tile.y));
 		top = getWorldPos(new Point(tile.x,tile.y-1));
 		bottom = getWorldPos(new Point(tile.x,tile.y+1));
 		
 		if(tileValueAt(left) != 1 && tileValueAt(top) != 1)
 			result.add(getWorldPos(new Point(tile.x-1,tile.y-1)));
 		if(tileValueAt(right) != 1 && tileValueAt(top) != 1)
 			result.add(getWorldPos(new Point(tile.x+1,tile.y-1)));
 		if(tileValueAt(left) != 1 && tileValueAt(bottom) != 1)
 			result.add(getWorldPos(new Point(tile.x-1,tile.y+1)));
 		if(tileValueAt(right) != 1 && tileValueAt(bottom) != 1)
 			result.add(getWorldPos(new Point(tile.x+1,tile.y+1)));
 		
 		result.add(left);
 		result.add(right);
 		result.add(top);
 		result.add(bottom);
 		
 		
 		result = removeInvalidTiles(result);
 		
 		
 		return result;
 		
 	}
 	
 	/**
 	 * Sets a value for a bunch of tiles.
 	 * @param tiles bunch of tiles, specified with world position
 	 * @param value value for all of them
 	 */
 	public void setTilesTo(ArrayList<Vector2D> tiles, int value) {
 		for(Vector2D t : tiles) {
 			setTile(t, value);
 		}
 	}
 	
 	/**
 	 * Removes invalid tiles from a set of tiles specified with world position. Invalid are tiles out of the bounds of the Field.
 	 * @param tiles bunch of tiles, in world coordinates
 	 * @return cleaned list
 	 */
 	public ArrayList<Vector2D> removeInvalidTiles(ArrayList<Vector2D> tiles) {
 		for(int i=0; i<tiles.size();i++) {
 			Vector2D p = tiles.get(i);
 			if(p.x() < 0 || p.x() > tilesX ||
 			   p.y() < 0 || p.y() > tilesY) {
 				tiles.remove(i);
 				i--;
 			}
 		}
 		return tiles;
 	}
 	
 	/**
 	 * Removes all non-obstacle tiles from a set of tiles
 	 * @param tiles bunch of tiles, in world coordinates
 	 * @return list with obstacle tiles
 	 */
 	public ArrayList<Vector2D> removeNotOnes(ArrayList<Vector2D> tiles) {
 		for(int i=0; i<tiles.size();i++) {
 			Vector2D p = tiles.get(i);
 			if(this.tileValueAt(p) == 1) {
 				tiles.remove(i);
 				i--;
 			}
 				
 		}
 		return tiles;
 	}
 	
 	/**
 	 * Gives a list of non-obstacle neighbours of a tile
 	 * @param pos tile, in world coordinates
 	 * @return list of free neighbours
 	 */
 	public ArrayList<Vector2D> freeneighbours(Vector2D pos) {
 		ArrayList<Vector2D> result = neighbours(pos);
 		
 		result = removeNotOnes(result);
 		
 		return result;
 	}
 	
 	/**
 	 * Checks whether the positions are on the same tile
 	 * @param a world position one
 	 * @param b world position two
 	 * @return FIXME
 	 */
 	public boolean sameTile(Vector2D a, Vector2D b) {
 		return this.tileIndexAt(a).equals(this.tileIndexAt(b));
 	}
 	
 	/**
 	 * Returns all Tile objects of the field
 	 * @return Array of Tile objects
 	 */
 	public Tile[][] getTiles() {
 		return tiles;
 	}
 	
 	public int getTilesX() {
 		return tilesX;
 	}
 	
 	public int getTilesY() {
 		return tilesY;
 	}
 	
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	/**
 	 * Prints the whole Field to the console. Debug output.
 	 */
 	public void printToConsole() {
 		for(int y=0; y<tilesY; y++) {
 			for(int x=0;x<tilesX; x++) {
 				System.out.print(tiles[x][y]);
 			}
 			System.out.println();
 		}
 		
 	}
 	
 	
 	
 }
