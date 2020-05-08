 package com.zachnickell.platform.level;
 
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Polygon;
 import java.awt.Rectangle;
 import java.util.ArrayList;
 import java.util.Random;
 
 import org.lwjgl.opengl.GL11;
 import org.lwjgl.util.glu.Project;
 
 import com.zachnickell.platform.Input;
 import com.zachnickell.platform.Platform;
 import com.zachnickell.platform.entity.*;
 import com.zachnickell.platform.entity.tile.*;
 import com.zachnickell.platform.item.weapon.Bullet;
 import com.zachnickell.platform.level.creator.LevelCreator;
 
 public class Level {
 	// screen size is 20 x 15 @(16x16)size tiles
 	// 50 x 50 map is highest efficient running size so far 3/24/13
 	// 256 x 256 map at 56% cpu and ~30 fps 3/25/13
 	// 175 x 175 map running smoothly at 4% cpu and ~60 fps 3/25/13 2:35PM
 
 	Random r = new Random();
 	int width;// = 25;
 	int height;// = 25;
 	int spawnX;// = width / 2;
 	int spawnY;// = height / 2;
 	public static Player player;
 	static PlayerGui pg;
 	int[][] tileGrid;// = new int[width][height];
 	Portal portal;
 	ArrayList<Entity> entities;
 	ArrayList<Tile> tiles;
 
 		
 	public Level(LevelCreator levelcreator) {
 		player = levelcreator.getPlayer();
 		pg = new PlayerGui(player);	
 		
 		tiles = levelcreator.getTiles();
 		entities = levelcreator.getEntities();
 			
 	}
 
 	public void render(){
 		GL11.glPushMatrix();
 		GL11.glTranslated(-player.x + Platform.WIDTH / 2 - 10, -player.y + Platform.HEIGHT / 2 - 10, 0);
 		for (int t = 0; t < tiles.size(); t++) {
 			tiles.get(t).render();
 		}
 		//portal.render();
 		//drenderMonsters();
 		player.render();
 		GL11.glPopMatrix();
 		pg.render();
 		//GL11.glTranslated(0, 0, 0);
 	}
 	
 	public void renderMonsters(){
 		ArrayList<Monster> mons = portal.getMonsters();
 		for(int m = 0; m < mons.size(); m++){
 			Monster monster = mons.get(m);
 			monster.render();
 		}
 	}
 
 	public void update(int delta) {
 		player.update(delta);
 		for (int e = 0; e < entities.size(); e++ ){
 			entities.get(e).update(delta);
 		}
 		//newColision();
 		
 	}
 	
 	public void updateMonsters(int delta){
 		ArrayList<Monster> mons = portal.getMonsters();
 		for (int m = 0; m < mons.size(); m ++){
 			Monster monster = mons.get(m);
 			monster.update(delta);
 		}
 	}
 	/*
 	public void createLevel() {
 		for (int x = 0; x < width; x++) {
 			for (int y = 0; y < height; y++) {
 				if (tiles[x][y].isOnScreen) {
 					if (tileGrid[x][y] == Tile.DEFAULT) {
 						tiles[x][y] = new Tile(x, y, width, height);
 					} else if (tileGrid[x][y] == Tile.SNOW) {
 						tiles[x][y] = new Snow(x, y, width, height);
 					} else if (tileGrid[x][y] == Tile.DIRT) {
 						tiles[x][y] = new Wall(x, y, width, height);
 					} else
 						tiles[x][y] = new Tile(x, y, width, height);
 				}
 			}
 		}
 	}*/
 
 	public Rectangle renderZone() {
 		Rectangle r1 = new Rectangle(
 				(int) player.x + player.w/2 - (Platform.WIDTH) / 2 - 2, (int) player.y + player.h/2
 						- (Platform.HEIGHT) / 2 + 14, Platform.WIDTH,
 				Platform.HEIGHT - 16);
 		return r1;
 		//Rectangle r2;
 		/*for (int x = 0; x < width; x++) {
 			for (int y = 0; y < height; y++) {
 				r2 = tiles[x][y].getBounds();
 				if (r1.intersects(r2)) {
 					tiles[x][y].shouldRender();
 				} else
 					tiles[x][y].shouldNotRender();
 			}
 		}
 		for (int m = 0; m < monsterNumber; m++) {
 			r2 = monsters[m].getBounds();
 			if (r1.intersects(r2)) {
 				monsters[m].shouldRender();
 			} else
 				monsters[m].shouldNotRender();
 		}*/
 
 	}
 	
 	public void newColision(){
 		ArrayList<Bullet> bullets = player.p.getBullets();
 		ArrayList<Monster> monsters = portal.getMonsters();
 		for (int b = 0; b < bullets.size(); b++){
 			Bullet bullet = (Bullet) bullets.get(b);
 			
 			Polygon p1 = bullet.getBounds();
 			
 			/*Rectangle r = portal.getBounds();
 			if (p1.intersects(r)){
 				portal.doesDamage(bullet.damage);
 				bullets.remove(b);
 				break;
 			}
 			*/
 			for (int m = 0; m < entities.size(); m++){
 				Entity monster = (Monster) entities.get(m);
 				Rectangle r1 = monster.getBounds();
 				
 				if (p1.intersects(r1)){
 					monster.doesDamage(bullet.damage);
 					bullets.remove(b);
 					break;
 				}
 			}
 			
 		}
 	}
 
 	/*public void CollisionDetect(Entity e, int delta) {
 		Rectangle r1;
 		Rectangle r2;
 		Polygon polygon;
 		r1 = player.getBounds();
 		/*
 		 * r2 = rock.getBounds(); if (r1.intersects(r2)){ rock.collision(player,
 		 * delta); } else player.fixMovement();
 		 
 		for (int x = 0; x < width; x++) {
 			for (int y = 0; y < height; y++) {
 				r2 = tiles[x][y].getBounds();
 				if (r1.intersects(r2)) {
 					tiles[x][y].collision(e, delta);
 				}
 			}
 		}
 		for (int m = 0; m < monsterNumber; m++) {
 			r2 = monsters[m].getBounds();
 			if (r1.intersects(r2)) {
 				monsters[m].collision(player, delta);
 			}
 		}/*
 		
 		/*for (int b = 0; b < player.p.bullet.size(); b++){
 			for (int m = 0; m < monsterNumber; m++){
 				polygon = player.p.bullet.get(b).getFiringBounds();
 				r2 = monsters[m].getBounds();
 				if (polygon.intersects(r2)){
 					player.p.collision(monsters[m], delta);
 				}
 			}
 		}*/
 		
 		/*for (int m = 0; m < monsterNumber; m++) {
 			r2 = monsters[m].getBounds();
 			if (p.intersects(r2)) {
 				player.p.collision(monsters[m], delta);
 			}
 		}
 	}*/
 	
	public boolean isFree(Entity e, int x, int y){
 		Tile tile;
		Rectangle r1, r2;
 		r1 = e.getBounds();
 		for (int t = 0; t < tiles.size(); t ++){
 			tile = tiles.get(t);
 			if (tile.isSolid){
 				r2 = tile.getBounds();
 				if (r1.intersects(r2)){
 					return false;
 				}
 			}
 		}
 			return true;
 	}
 	
 	public boolean isInteracting(int x, int y){
 		
 		return false;
 	}
 	
 	public void respawn(Entity e){
 		int x = r.nextInt(width);
 		int y = r.nextInt(height);
 		if (x == spawnX && y == spawnY){
 			respawn(e);
 		}
 		e.respawn(x, y);
 	}
 	
 }
