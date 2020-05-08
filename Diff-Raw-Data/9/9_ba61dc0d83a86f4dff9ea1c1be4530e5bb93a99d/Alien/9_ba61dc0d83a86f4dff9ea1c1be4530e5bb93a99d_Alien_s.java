 import java.awt.*;
 
 // Thoughts on making this astract/an 
 // interface to build more detailed aliens??? 
 public class Alien extends GameEntity{
 
 	// Split into own classes
     final static int NORMAL = 0;
 	final static int BOSS = 1;
 
 	int id;
 
 	public Alien(int x,int y,int id,String image) {
 	    super(x,y,0,image);
 		this.id = id;
 		if (id == NORMAL) {
 		   fric = 0.7;
 		   setHealth(60);  
 		   bounds.setSize(2*getWidth(),2*getHeight());
 		} else if (id == BOSS) {
 		   setHealth(30);
 		   bounds.setSize(getWidth(),getHeight());
 		} else {
 		   id = NORMAL;
 		   fric = 0.7;
 		   setHealth(60);
 		   bounds.setSize(2*getWidth(),2*getHeight());
 		}
 		
 		
 	}
 	
 	public void update(Space space) {
 	   if (y_pos < space.HEIGHT) {
 	      y_vel += accel;
 		  if(y_pos < space.player.getY() && x_pos < space.player.getX()) {
 		    x_vel += accel;
 		  } else if(y_pos < space.player.getY() && x_pos > space.player.getX()) {
 		    x_vel -= accel;
 		  } else {
 		    x_vel = 0;
 		  }
 	   } else {
 	      space.aliens.remove(this);
 	   }
 	   
 	   y_pos += y_vel;
 	   x_pos += x_vel;
 	   y_vel *= fric;
 	   x_vel *= fric;
 	   
 	   bounds.setLocation(getX(),getY());
 	   
 	   for(Bullet bullet : space.bullets) {
 	     if (bullet.y_pos > y_pos){
 		    if(x_pos >= bullet.x_pos-200 && x_pos <= bullet.x_pos+200 && x_pos<bullet.x_pos) {
 			    x_pos -= accel;
 			}else if(x_pos >= bullet.x_pos-200 && x_pos <= bullet.x_pos+200 && x_pos>bullet.x_pos) {
 			    x_pos += accel;
 			}
 		 }
 	   
 	      if (bullet.bounds.intersects(bounds)) {
 			  space.bullets.remove(bullet);
 		    if(health <= 0){
 			  space.aliens.remove(this); 
 			  space.score += 10;
 			  space.remEnemies--;
 			  space.canCreateBonus = true;
 			  space.explosionHappened = true;
 			  space.explX = getX() + getWidth()/2;
 			  space.explY = getY() + getHeight()/2;
 			  space.bonusX = getX();
 			  space.bonusY = getY();
 			} else {
 			  health -= 30;
 			}
 		  }
 	   }
 	}
 	
 	// Abstract draw. But if we do keep one class. Easier to manage with switch
 	public void draw(Graphics2D g) {
 	  switch(id){
 	    case NORMAL:
     	  g.drawImage(img,getX(),getY(),2*getWidth(),2*getHeight(),null);
	      Graphics2D graphics = (Graphics2D)g.create();
 	      graphics.setColor(Color.red);
 	      graphics.fill3DRect(getX()+10,getY()-10,health,10,true);
 	      graphics.dispose();
 	      break;
 	    case BOSS:
 	      g.drawImage(img,getX(),getY(),getWidth(),getHeight(),null);
	      Graphics2D graphics = (Graphics2D)g.create();
 	      graphics.setColor(Color.red);
 	      graphics.fill3DRect(getX()+10,getY()-10,health,10,true);
 	      graphics.dispose();
 		  break;
 	  }
 	}
 
 }
