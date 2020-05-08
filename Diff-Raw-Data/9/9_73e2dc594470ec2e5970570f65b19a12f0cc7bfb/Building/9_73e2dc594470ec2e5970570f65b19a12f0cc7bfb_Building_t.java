 import java.util.ArrayList;
 import java.awt.Graphics;
 import java.awt.Rectangle;
 import java.awt.Image;
 import javax.swing.ImageIcon;
 
 public class Building extends Item {
 
     private ArrayList obstacles;
     private ArrayList powerups;
 	private ArrayList birds;
 	private String graphic = "building.png";
 	private Image image;
 	
 
 	public Building(int width, int height, int x, int y) {
 		if(width == 0)
 		{
 			width = Board.getRandom(300,1000);
         	width += 50 - (width % 50);
         	x = Board.getInstance().getWidth()+Board.getRandom(50,250);
         	y = Board.getRandom(290,420);
 		}
		height = 600;
 
 		ImageIcon ii = new ImageIcon(this.getClass().getResource(graphic));
         image = ii.getImage();
 		setWidth(width);
 		setHeight(height);
 		setX(x);
 		setY(y);
 		setOrientation(1);
		System.out.println(Board.getRandom(0,2));
 		if(Board.getRandom(0,2) == 1) {
         	this.setX(getX());
        	this.setY(Board.getRandom(100,200)-getHeight());
        	System.out.println(getY());
         	this.setOrientation(-1);
         }
 		obstacles = new ArrayList();
 		powerups = new ArrayList();
 		birds = new ArrayList();
 		spawnObstacles();
 		spawnPowerups();
 		spawnBirds();
 	}
 
 	public Building() {
 		this(0,0,0,0);
 	}
 
 	public void move() {
 		setX(getX() - Board.getInstance().getSpeed());
 		moveObstacles();
 		movePowerups();
 		moveBirds();
 		flyBirds();
 	}
 
 	public void paintComplete(Graphics g) {
 		this.paint(g);
 		this.paintObstacles(g);
 		this.paintPowerups(g);
 		this.paintBirds(g);
 	}
 
 	public void paint(Graphics g) {
 		int graphicX = (int)getX();
 		for(int i = 0; i < getWidth() / 50; ++i)
 		{
 			g.drawImage(image, graphicX, (int)getY(), Board.getInstance());
 			graphicX += 50;
 		}
 	}
 
 	public ArrayList getObstacles() {
 		return obstacles;
 	}
 
 	public ArrayList getPowerups() {
 		return powerups;
 	}
 
 	public ArrayList getBirds() {
 		return birds;
 	}
 
 	public Rectangle getHitbox() {
 		return new Rectangle((int)getX(), (int)getY() - 200, getWidth(), getHeight()+ 200);
 	}
 
 	public void spawnObstacles() {
 
 		if(Board.getRandom(0,2) != 1) return;
 		int lastX = 0;
 		for(int i = 0; i <= Board.getRandom(0,2); ++i)
 		{
 			lastX += Board.getRandom(180,300);
 			if(lastX + 120 > getWidth()) break;
 			int currentX = lastX + (int) getX();
 			if(getOrientation() == -1) {
 				Obstacle o = new Obstacle(20,20,currentX,(int) getY()+getHeight()+1);
 				this.obstacles.add(o);
 			} else {
 				Obstacle o = new Obstacle(20,20,currentX,(int) getY()-20);
 				this.obstacles.add(o);
 			}
 			
 		}
 	}
 
 	public void moveObstacles() {
 		if(this.obstacles.size() == 0) return;
 		for(int i = 0; i < this.obstacles.size(); ++i) {
 			Obstacle o = (Obstacle) this.obstacles.get(i);
 			o.move();
 		}
 	}
 
 	public void paintObstacles(Graphics g) {
 		
 		if(this.obstacles.size() == 0) return;
 		for(int i = 0; i < this.obstacles.size(); ++i) {
 			Obstacle o = (Obstacle) this.obstacles.get(i);
 			if(o.isVisible())
 				o.paint(g);
 		}
 	}
 
 	public void spawnPowerups() {
 		if(Board.getRandom(0,2) != 1) return;
 		int lastX = 0;
 		for(int i = 0; i <= Board.getRandom(0,1); ++i)
 		{
 			lastX += Board.getRandom(100,280);
 			if(lastX + 30 > getWidth()) break;
 			int currentX = lastX + (int) getX();
 			Powerup p = new Powerup(30,30,currentX,(int) getY()-30);
 			this.powerups.add(p);
 
 		}
 	}
 
 	public void movePowerups() {
 		if(this.powerups.size() == 0) return;
 		for(int i = 0; i < this.powerups.size(); ++i) {
 			Powerup p = (Powerup) this.powerups.get(i);	
 			p.move();
 		}
 	}
 
 	public void paintPowerups(Graphics g) {
 		if(this.powerups.size() == 0) return;
 		for(int i = 0; i < this.powerups.size(); ++i) {
 			Powerup p = (Powerup) this.powerups.get(i);	
 			p.paint(g);
 		}
 	}
 	
 	public void spawnBirds() {
 		int lastX	 = (int)getX();
 		if(Board.getRandom(0,2) == 1) return;
 		
 		
 		for(int i = 0;; ++i) {
 				
 			lastX += Board.getRandom(10,30);
 			
 			if(lastX > getWidth() + getX() - 10) {
 				break;
 			}
 			
 			if(getOrientation() == -1) {
 				Bird b = new Bird(lastX, (int)getY() +getHeight());
 				birds.add(b);
 			} else {
 				Bird b = new Bird(lastX, (int)getY() -10);
 				birds.add(b);
 			}
 			
 				
 		
 		
 		}
 	}
 	
 	public void moveBirds() {
 		for(int i=0; i< birds.size();++i) {
 			Bird b = (Bird) birds.get(i);
 			b.move();
 		}	
 	
 	}
 
 	public void flyBirds() {
 		if(birds.size() == 0) return;
 
 		for(int i=0;i<birds.size();++i) {
 			Bird b = (Bird) birds.get(i);
 			if(b.getProperties() == Bird.FLYING) b.fly();
 		}
 	}	
 	
 	public void paintBirds(Graphics g) {
 		
 		for(int i=0; i< birds.size();++i) {
 			Bird b = (Bird) birds.get(i);
 			b.paint(g);
 		}	
 	
 	
 	}
 }
