 package com.ion.ld27.entities;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.image.BufferedImage;
 
 import com.ion.ld27.Game;
 import com.ion.ld27.entities.mobs.Bear;
 import com.ion.ld27.entities.mobs.Rat;
 import com.ion.ld27.entities.mobs.Spider;
 import com.ion.ld27.entities.mobs.Tiger;
 import com.ion.ld27.sfx.Sounds;
 
 public class Entity {
 
 	public static double baseHealth = 1;
 	public static double baseAttack = 1;
 	public int attack;
 	public int speed = 4;
 	public int xpos;
 	public int ypos;
 	public int health;
 	public int healthRange;
 	public String type;
 	public BufferedImage[] imgs;
 	public int currentImage;
 	public int timer = 0;
 	public int tick = 0;
 	public boolean newPath = false;
 	public int punch = 0;
 	public int canPunch = 0;
 	
 	public boolean r = true;
 	public boolean l = true;
 	public boolean u = true;
 	public boolean d = true;
 	
 	public void draw(Graphics g){
 		if(ypos - Game.entities.get(0).ypos + Game.height/2 - 32 > -64 && ypos - Game.entities.get(0).ypos + Game.height/2 - 32 < Game.height){
 			if(xpos - Game.entities.get(0).xpos + Game.width/2 - 32 >  -64 && xpos - Game.entities.get(0).xpos + Game.width/2 - 32 < Game.width){
 				if(getClass() == Bear.class || getClass() == Rat.class || getClass() == Spider.class || getClass() == Tiger.class){
 					int WIDTH = imgs[0].getWidth();
					int gn = (WIDTH / healthRange) * health;
 					int h = 3;
 					g.setColor(new Color(0x00FF00));
 					g.fillRect(xpos - Game.entities.get(0).xpos + Game.width/2 - 32, ypos - Game.entities.get(0).ypos + Game.height/2 - 32 - 7, gn, h);
 					g.setColor(new Color(0xFF0000));
 					g.fillRect(xpos - Game.entities.get(0).xpos + Game.width/2 - 32+gn, ypos - Game.entities.get(0).ypos + Game.height/2 - 32 - 7, WIDTH - gn, h);
 				}
 				g.drawImage(imgs[currentImage], xpos - Game.entities.get(0).xpos + Game.width/2 - 32, ypos - Game.entities.get(0).ypos + Game.height/2 - 32, null);
 			}
 		}
 		if(punch > 0){
 			punch--;
 		}
 	}
 	
 	public void update(){
 		timer++;
 		if(timer > 10){
 			timer = 0;
 			if(tick > 0){
 				tick = 0;
 			}
 			else{
 				tick++;
 			}
 		}
 		else if(timer >= 20){
 			timer = 0;
 		}
 		ai();
 		if(health <= 0){
 			destroy();
 		}
 	}
 	
 	public void ai(){
 	}
 	
 	public void destroy(){
 		int e = Game.entities.indexOf(this);
 		if(e >= 0){
 			Game.entities.remove(e);
 		}
 	}
 	public void collideWith(){
 	}
 	
 	public void newMode(int mode, int oldMode){
 		if(getClass() == Bear.class || getClass() == Rat.class || getClass() == Spider.class || getClass() == Tiger.class){
 			if(oldMode == 1){
 				healthRange--;
 				health--;
 				attack-= 2;
 			}
 			else if(oldMode == 2){
 				healthRange-= 6;
 				health-= 6;	
 			}
 			else if(oldMode == 3){
 				attack-= 3;
 			}
 			else if(oldMode == 4){
 				health++;
 				healthRange++;
 				attack++;
 			}
 			else if(oldMode == 5){
 				health++;
 				healthRange++;
 				attack-= 4;
 			}
 			if(mode == 1){
 				healthRange++;
 				health++;
 				attack+= 2;
 			}
 			else if(mode == 2){
 				healthRange+= 6;
 				health+= 6;	
 			}
 			else if(mode == 3){
 				attack+= 3;
 			}
 			else if(mode == 4){
 				health--;
 				healthRange--;
 				attack--;
 			}
 			else if(mode == 5){
 				health--;
 				healthRange--;
 				attack+= 4;
 			}
 		}
 	}
 	
 	public void check(Entity ent, boolean isPlayer){
 		boolean hit = false;
 		boolean nd = true;
 		boolean nu = true;
 		boolean nr = true;
 		boolean nl = true;
 		boolean inx = false;
 		boolean iny = false;
 		if(isPlayer && (ent.getClass() == Rat.class || ent.getClass() == Bear.class || ent.getClass() == Tiger.class || ent.getClass() == Spider.class)){
 			if(Math.abs(xpos - ent.xpos) + Math.abs(ypos - ent.ypos) <= 512){
 				ent.newPath = true;
 			}
 			if(ent.canPunch == 0){
 				if(Math.abs(xpos - ent.xpos) + Math.abs(ypos - ent.ypos) <= 128){
 					ent.punch();
 				}
 			}
 		}
 		if(Math.abs(xpos - ent.xpos) < 64){
 			inx = true;
 		}
 		if(Math.abs(ypos - ent.ypos) < 64){
 			iny = true;
 		}
 		if(inx){
 			if(ypos - ent.ypos + 1 > -64 && ypos - ent.ypos + 1 < 0){
 				nd = false;
 				hit = true;
 			}
 			if(ypos - ent.ypos - 1 < 64 && ypos - ent.ypos - 1 > 0){
 				nu = false;
 				hit = true;
 			}
 		}
 		if(iny){
 			if(xpos - ent.xpos + 1 > -64 && xpos - ent.xpos + 1 < 0){
 				nr = false;
 				hit = true;
 			}
 			if(xpos - ent.xpos - 1 < 64 && xpos - ent.xpos - 1 > 0){
 				nl = false;
 				hit = true;
 			}
 		}
 		if(hit && punch == 1 && (ent.getClass() == Rat.class || ent.getClass() == Player.class || ent.getClass() == Bear.class || ent.getClass() == Spider.class || ent.getClass() == Tiger.class)){
 			if(!nd && (currentImage == 0 || currentImage == 1)){
 				ent.hurt(attack);
 			}
 			else if(!nu && (currentImage == 6 || currentImage == 7)){
 				ent.hurt(attack);
 			}
 			else if(!nl && (currentImage == 2 || currentImage == 3)){
 				ent.hurt(attack);
 			}
 			else if(!nr && (currentImage == 4 || currentImage == 5)){
 				ent.hurt(attack);
 			}
 		}
 		if(!nd){
 			d = false;
 		}
 		if(!nu){
 			u = false;
 		}
 		if(!nl){
 			l = false;
 		}
 		if(!nr){
 			r = false;
 		}
 		if(isPlayer){
 			if(hit){
 				ent.collideWith();
 			}
 		}
 	}
 	public void punch(){
 		punch = 5;
 		canPunch = 25;
 	}
 	public void hurt(int strength){
 		if(getClass() == Player.class && Sounds.endGame == true){
 			//do nothing
 		}
 		else{
 			health-= strength;
 		}
 	}
 }
