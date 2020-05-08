 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 
 public class Player {
 	private String name;
 	private float x, y;
 	private Image image;
 	private int health;
	private float radius;
 	
 	public Player(String s){
 		name = s;
 		health = 100;
 		try {
 			image = new Image("data/player.png");
 		} catch (SlickException e) {}
		radius = image.getTextureHeight()/2;
 	}
 	
 	/**
 	 * returns the player's image
 	 */
 	public Image getImage(){
 		return image;
 	}
 	
 	/**
 	 * sets the player's image
 	 * 
 	 * @param Path to image
 	 */
 	public void setImage(String s){
 		try {
 			image = new Image(s);
 		} catch (SlickException e) {}
 	}
 	
 	/**
 	 * returns the player's health
 	 */
 	public int getHealth(){
 		return health;
 	}
 	
 	/**
 	 * returns the player's name
 	 */
 	public String getName(){
 		return name;
 	}
 	
 	/**
 	 * returns the player's x coordinate
 	 */
 	public float getX(){
 		return x;
 	}
 	
 	/**
 	 * sets the player's x coordinate
 	 * 
 	 * @param Coordinate
 	 */
 	public void setX(float i){
 		x=i;
 	}
 	
 	/**
 	 * returns the player's y coordinate
 	 */
 	public float getY(){
 		return y;
 	}
 	
 	/**
 	 * sets the player's y coordinate
 	 * 
 	 * @param Coordinate
 	 */
 	public void setY(float i){
 		y=i;
 	}
 	
	/**
	 * Checks if the 2 coordinates is colliding
	 * with the player.
	 * @return true if coordinates is colliding
	 */
	public boolean checkCollision(float x, float y){
		if(x < this.x + radius && x > this.x - radius && 
				y < this.y + radius && y > this.y - radius){
			return true;
		}
		return false;
	}
 	
 }
