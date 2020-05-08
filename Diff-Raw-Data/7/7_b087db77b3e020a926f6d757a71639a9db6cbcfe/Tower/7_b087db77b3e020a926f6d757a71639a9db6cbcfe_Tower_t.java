 package com.jeffreyregalia;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.util.ArrayList;
 
 public class Tower implements Entity{
 	int size;
 	int radius;
 	int x;
 	int y;
 	Enemy target;
 	double targetDistance;
 	Point targetPoint;
 	int timeSinceAttack = 0;
 	int attackTime = 250;
 	int attackFrames = 0;
 	boolean attacked = false;
 	int power = 2;
 	
 	Tower(int size, int radius, int x, int y){
 		this.size = size;
 		this.radius = radius;
 		this.x = x;
 		this.y = y;
 	}
 	
 	public void update(int time){
 		this.timeSinceAttack += time;
 		if(this.timeSinceAttack > this.attackTime)
 			this.timeSinceAttack = this.attackTime;
 		if(this.target == null){
 			this.targetDistance = 0.0;
 			this.targetPoint = null;
 		}else{
 			if(this.timeSinceAttack == this.attackTime){
 				this.target.attack(this.power);
 				this.timeSinceAttack = 0;
 				this.attacked = true;
 				if(this.target.getHP() < 1){
 					this.target = null;
 				}
 			}
 		}
 		// fire?
 		// rotate gun?
 	}
 	
 	public void render(Graphics g){
 		//draw tower
 		if(!attacked && this.timeSinceAttack == this.attackTime)
 			g.setColor( new Color (150, 150, 0));
 		else
 			g.setColor( new Color(0,0,0));
 		g.fillRect(x-size/2, y-size/2, size, size);
 		//draw radius
 		if(target == null){
 			g.setColor( new Color(0,255,0));
 		}else {
 			g.setColor( new Color(255,0,0));
 		}
 		g.drawOval(x-radius, y-radius, radius*2, radius*2);
 		if(targetPoint != null){
 			//if(attacked){
 				g.setColor( new Color(0,0,0));
 				g.drawLine(x,y,(int) targetPoint.getX(),(int) targetPoint.getY());
 				attackFrames++;
 				if(attackFrames > 5){
 					attackFrames = 0;
 					this.attacked = false;
 				}
 			//}
 //			g.drawString("TX: " + targetPoint.getX() + " TY: " + targetPoint.getY(), 50, 90);
 		} else {
 			this.attacked = false;
 		}
 //		g.drawString("Distance: "+this.targetDistance,50,50);
 //		g.drawString("X: " + x + " Y: " + y, x, y);
 	}
 	
 	public void findTarget(ArrayList<Enemy> enemyList){
 		double tempDistance = 0.0;
 		if(target==null){
 			for(Enemy enemy: enemyList){
 				for(Point point: enemy.getHitBox()){
 					double distance = Math.sqrt((x-point.getX())*(x-point.getX())+(y-point.getY())*(y-point.getY()));
					if(distance < radius)
 						if(tempDistance == 0.0 || distance < tempDistance){
 							tempDistance = distance;
 							this.targetDistance = distance;
 							this.targetPoint = point;
							this.target = enemy;
 						}
 				}
 			}
 			if(tempDistance == 0.0){
 				this.targetPoint = null;
 				this.targetDistance = 0.0;
 				this.target = null;
 			}
 		}else{
 			boolean stillValid = false;
 			for(Point point: this.target.getHitBox()){
 				double distance = Math.sqrt((x-point.getX())*(x-point.getX())+(y-point.getY())*(y-point.getY()));
 				if(distance < radius &&(tempDistance == 0.0 || distance < tempDistance)){
 					tempDistance = distance;
 					this.targetDistance = distance;
 					this.targetPoint = point;
 					stillValid = true;
 				}
 			}
 			if(!stillValid){
 				this.target = null;
 				this.targetDistance = 0.0;
 				this.targetPoint = null;
 			}
 		}
 	}
 
 	
 	public void setTarget(){
 		
 	}
 
 	public Entity getTarget(){
 		return this.target;
 	}
 	
 	public boolean isAlive(){
 		return true;
 	}
 }
