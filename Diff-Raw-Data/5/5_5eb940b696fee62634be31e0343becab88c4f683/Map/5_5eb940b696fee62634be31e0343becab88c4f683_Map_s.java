 package game;
 
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.util.ArrayList;
 
 class Map {
     MapObject[][] grid;
     ArrayList<MapObject> objects = new ArrayList<>();
 
     Dimension size;
     public boolean addObject(MapObject object) {
 	grid[object.coordinates.y][object.coordinates.x] = object;
 	return objects.add(object);
     }
     
     public boolean objectMove(int oldX, int oldY, int x, int y) {
	if (grid[y][x] != null)
		return false;
 	try {
 	    grid[y][x] = grid[oldY][oldX];
 	    grid[oldY][oldX] = null;
 	} catch (ArrayIndexOutOfBoundsException e) {
 	    return false;
 	}
 	return true;
     }
 
     public Map(int width, int height) {
 	size = new Dimension(width, height);
 	grid = new MapObject[height/50][width/50];
     }
 
     public void draw(Graphics g) {
 	for(MapObject object : objects) {
 	    g.drawImage(object.sprite, object.coordinates.x*50, object.coordinates.y*50, null);
 	}
     }
 }
