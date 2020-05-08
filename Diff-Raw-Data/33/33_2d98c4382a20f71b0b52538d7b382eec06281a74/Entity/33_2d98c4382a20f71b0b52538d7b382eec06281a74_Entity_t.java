 package com.zachnickell.platform.entity;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Rectangle;
 import java.awt.image.BufferedImage;
 
 import org.newdawn.slick.opengl.Texture;
 import org.newdawn.slick.particles.Particle;
 import org.newdawn.slick.util.*;
 
 import com.zachnickell.platform.level.Level;
 
 public class Entity {
 	public double angle = 0;
 	public double x, y;
 	public int w, h;
 	public double speed;
 	public double maxSpeed;
 	public int maxHealth = 1;
 	public int health = maxHealth;
 	public int kills;
 	public int xp;
 	public int maxXP;
 	public int damage;
 	public int stamina;
 	public int maxStamina;
 	public boolean invincable = false;
 	public Color c;
 	public boolean isSolid = false;
 	public boolean isMovable = false;
 	public boolean alive = true;
 	public int direction = 0;
 	public static final int IDLE = 0;
 	public static final int UP = 1;
 	public static final int DOWN = 2;
 	public static final int LEFT = 3;
 	public static final int RIGHT = 4;
 	public boolean canMoveUp = true;
 	public boolean canMoveDown = true;
 	public boolean canMoveLeft = true;
 	public boolean canMoveRight = true;
 	public int level;
 	public int giveXP;
 	public long time;
 	public long lastStaminaCheck;
 	public boolean isOnScreen = true;
 	public boolean reloadStamina = false;
 	//public BufferedImage sprite;
 	public Texture sprite;
 	public static Level lvl;
 
 	long lastTime;
 
 	public void init(Level level) {
 		//System.out.println("ENTITY INITIALIZED!");
 		this.lvl = level;
 		//System.out.println(lvl.toString());
 	}
 
 	public void render(){//Graphics g) {
 		if (shouldRender()) {
 			//g.setColor(c);
 			//g.fillRect((int) x, (int) y, w, h);
 		}
 	}
 
 	public boolean shouldRender() {
 		//Rectangle r = new Rectangle((int)x,(int)y,w,h);
 		if (getBounds().intersects(lvl.renderZone())){
 			return true;
 		} else
 			return false;
 	}
 
 
 	public void update(int delta) {
 
 	}
 
 	public boolean isAlive() {
 		if (health <= 0)
 			alive = false;
 		if (health > 0)
 			alive = true;
 		return alive;
 	}
 
 	/*
 	 * public void collision(Entity e, int delta) { if (isOnScreen) { if
 	 * (isSolid && e.isSolid) { if (e.direction == UP) { // e.canMoveUp = false;
 	 * e.y = y + h; if (isMovable) { y -= e.speed * delta; } } else if
 	 * (e.direction == DOWN) { // e.canMoveDown = false; e.y = y - e.h; if
 	 * (isMovable) { y += e.speed * delta; } } else if (e.direction == LEFT) {
 	 * // e.canMoveLeft = false; e.x = x + w; if (isMovable) { x -= e.speed *
 	 * delta; } } else if (e.direction == RIGHT) { // e.canMoveRight = false;
 	 * e.x = x - e.w; if (isMovable) { x += e.speed * delta; } } } } }
 	 */
 
 	public void collision(Entity e, int delta) {
 		if (isSolid && e.isSolid) {
 
 		}
 	}
 
 	public double getAngle() {
 		return angle;
 	}
 
 	public void decreaseStamina(int penalty) {
 		if (System.currentTimeMillis() - lastStaminaCheck > 250) {
 			if (stamina != 0) {
 				lastStaminaCheck = System.currentTimeMillis();
 				stamina -= penalty;
 			}
 		}
 	}
 
 	public void increaseStamina(int advantage) {
 		if (System.currentTimeMillis() - lastStaminaCheck > 250) {
 			if (stamina < maxStamina) {
 				lastStaminaCheck = System.currentTimeMillis();
 				stamina += advantage;
 			}
 		}
 	}
 
 	public void doesDamage(int damage) {
 		health -= damage;
 	}
 
 	public Rectangle getBounds() {
 		return new Rectangle((int) x, (int) y, w, h);
 	}
 
 	public void moveCheck(double dx, double dy){
		if(lvl.isFree(this, (int)dx, 0)){
 			x += dx;
 		} else 
 			x += 0;
		if(lvl.isFree(this, 0, (int)dy)){
 			y += dy;
 		} else 
 			y += 0;
 	}
 	
 	public boolean isHurt(){
 		if (maxHealth > health){
 			return true;
 		}
 		else return false;
 	}
 	
 	public void respawn(int x, int y) {
 		health = maxHealth;
 		this.x = x * 16;
 		this.y = y * 16;
 	}
 
 }
