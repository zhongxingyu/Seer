 package com.zachnickell.platform.level.creator;
 
 import java.awt.Graphics;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.imageio.ImageIO;
 
 import com.zachnickell.platform.entity.Entity;
 import com.zachnickell.platform.entity.Player;
 import com.zachnickell.platform.entity.Portal;
 import com.zachnickell.platform.entity.tile.Tile;
 import com.zachnickell.platform.entity.tile.Wall;
 
 public class LevelCreator {
 
 	Graphics g;
 
 	ArrayList<Entity> entities;
 	ArrayList<Tile> tiles;
 	Player player;
 
 	public static BufferedImage map;
 
 	public LevelCreator() {
 		entities = new ArrayList<Entity>();
 		tiles = new ArrayList<Tile>();
 		try {
 			map = ImageIO.read(LevelCreator.class.getResource("/map.png"));
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		createLevel();
 	}
 
 	public void createLevel() {
 		int color;
 		for (int x = 0; x < map.getWidth(); x++) {
 			for (int y = 0; y < map.getHeight(); y++) {
 
 				color = map.getRGB(x, y) & 0xffffff;
 
 				//tiles
 				// top left wall
 				if (color == 0x1e1e1e)
 					addTile(new Wall(x, y, Wall.TOPLEFT));
 				// top wall
 				if (color == 0x000000)
 					addTile(new Wall(x, y, Wall.TOP));
 				// top right wall
 				if (color == 0x212121)
 					addTile(new Wall(x, y, Wall.TOPRIGHT));
 				// bottom left wall
 				if (color == 0x1f1f1f)
 					addTile(new Wall(x, y, Wall.BOTTOMLEFT));
				// bottom wallk
 				if (color == 0x5a5a5a) 
 					addTile(new Wall(x, y, Wall.BOTTOM));
 				// bottom right wall
 				if (color == 0x202020)
 					addTile(new Wall(x, y, Wall.BOTTOMRIGHT));
 				//left wall
 				if (color == 0x2d2d2d)
 					addTile(new Wall(x, y, Wall.LEFT));
 				//right wall
 				if (color == 0x878787)
 					addTile(new Wall(x, y, Wall.RIGHT));
 				// default floor
 				if (color == 0xffffff) 
 					addTile(new Tile(x, y));
 				//entities
 				if (color == 0x00ff21){
 					player = (new Player(x, y));
 					addTile(new Tile(x, y));
 				}
 				if (color == 0xb200ff){
 					addEntity(new Portal(x, y));
 					addTile(new Tile(x, y));
 				}
 			}
 		}
 	}
 
 	public Player getPlayer(){
 		return player;
 	}
 	
 	public ArrayList<Entity> getEntities(){
 		return entities;
 	}
 	public ArrayList<Tile> getTiles(){
 		return tiles;
 	}
 	
 	public void addEntity(Entity e) {
 		entities.add(e);
 	}
 
 	public void addTile(Tile t) {
 		tiles.add(t);
 	}
 
 	/*
 	 * public void getHexCode(int pixel) { //String hex; pixel = pixel &
 	 * 0xffffff; int alpha = (pixel >> 24) & 0xff; int red = (pixel >> 16) &
 	 * 0xff; int green = (pixel >> 8) & 0xff; int blue = pixel & 0xff;
 	 * 
 	 * System.out.println(alpha + red + blue + green);
 	 * 
 	 * //return hex; }
 	 */
 }
