 package ch.ilikechickenwings.karpfengame.Entity;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.awt.event.KeyEvent;
import java.io.IOException;
 
import javax.imageio.ImageIO;

import ch.ilikechickenwings.karpfengame.KarpfenGame;
 import ch.ilikechickenwings.karpfengame.Handler.InputHandler;
 import ch.ilikechickenwings.karpfengame.Entity.Entity;
 
 public class Player extends Entity{
 
 	private boolean falling=true;
 	private boolean jumping=false;
 	private int gravity=10;
 	private int jumped=0;
 	
 	// coffee:
 	private double coffee;
	private int coffeeHeigth=10; // height of the coffee-bar.
 	private double cReduceWalk=0.2; // the amount of coffee that is lost per jump
 	private double cReduceJump=2.0; // the amount of coffee that is lost per update whilst one is walking.
 	
 	public Player(int x, int y, int lifes, int vel) {
 		
 		setX_Point(x);
 		setY_Point(y);
 		setLifes(lifes);
 		setWidth(30);
 		setHeight(70);
 		setVelocity(vel);
 		setCoffee(100);
 		//super(x, y, lifes);
 		// TODO Auto-generated constructor stub
 	}
 
 	public int getCoffee() {
 		return (int)coffee;
 	}
 
 	public void setCoffee(int coffee) {
 		this.coffee = (double)coffee;
 	}
 
 	public boolean isFalling() {
 		return falling;
 	}
 
 	public void setFalling(boolean falling) {
 		this.falling = falling;
 	}
 
 	public boolean isJumping() {
 		return jumping;
 	}
 
 	public void setJumping(boolean jumping) {
 		this.jumping = jumping;
 	}
 
 	public void update(InputHandler inHandler) {
 		
 		
 			if ((inHandler.getKeys()[KeyEvent.VK_W]||inHandler.getKeys()[KeyEvent.VK_UP]||inHandler.getKeys()[KeyEvent.VK_SPACE])&&!jumping&&!falling) { // 
 				jumping=true;
 				falling=false;
				coffee-=cReduceJump;
 			}
 			
 			jump();
 
 			if (inHandler.getKeys()[KeyEvent.VK_A]||inHandler.getKeys()[KeyEvent.VK_LEFT]) {
				setX_Point(getX_Point()-getVelocity());
				coffee-=cReduceWalk;
 			}
 			if (inHandler.getKeys()[KeyEvent.VK_D]||inHandler.getKeys()[KeyEvent.VK_RIGHT]) {
				setX_Point(getX_Point()+getVelocity());
				coffee-=cReduceWalk;
 			}
 
 		/* moved to Level.java - SC
 		for (int w = 0; w < KarpfenGame.walls.size(); w++) { // TODO: This collision is checked in Level.java
 			Wall wall = (Wall) KarpfenGame.walls.get(w);
 		if(getX_Point()<wall.getX_Point()+wall.getWidth()&&getX_Point()+30>wall.getX_Point()){
 			if(wall.getY_Point()<getY_Point()+getHeight() && !(wall.getY_Point()+10<getY_Point()+70)){
 				x=1;
 			}else{
 				x=0;}
 				
 			}
 		}
 		
 		if(x==1){
 			falling=false;
 			jumping=false;
 			x=0;
 		}else if (!jumping){
 			falling=true;
 		}
 		x=0;
 		*/ 
 			
 		if(falling){
 			setY_Point(getY_Point()+gravity);
 		}
 		
 		
 	}
 
 	private void jump() {
 		if(jumping==true){
 		jumped++;
 		if(jumped>9){
 			falling=true;
 			jumping=false;
 			jumped=0;
 		}
 		setY_Point(getY_Point()-10);
 		}
 	}
 	
 	public void draw(Graphics2D g2,int xOffset){
 		g2.setColor(Color.green);
 		g2.fillRect(getX_Point()-xOffset, getY_Point(), getWidth(), getHeight());
 		g2.setColor(Color.black);
 		//coffe shouldn't be above the player
 		
 		
 		Image imge1 = null;
 			imge1 = Toolkit.getDefaultToolkit().getImage(
 					getClass().getResource("/res/loadingbar.png"));
 
 		g2.drawImage(imge1,5, 5, 150, 30,null);
 		g2.fillRect(5+(int)(150*coffee/100), 5,150,30);
 		
 	}
 
 }
