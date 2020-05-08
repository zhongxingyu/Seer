 package nitrogene.core;
 
 import java.util.ArrayList;
 
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 
 public class Planet extends CircleMesh{
 	private int maxhp;
 	private int hp;
 	private Image im;
 
	public Planet(float centerx, float centery, Image theimage, int maxhp) {
		super(centerx, centery, theimage);
 		this.maxhp = maxhp;
 		hp = maxhp;
 		im = theimage;
 	}
 	
 	public void damage(int damage, ArrayList<Planet> planet, ArrayList<CircleMesh> circle, int e) throws SlickException{
 		hp -= damage;
 		if(hp <= 0){
 			hp = 0;
 			//explode();
 			this.destroy(planet, circle, e);
 		}
 	}
 	public void destroy(ArrayList<Planet> planet, ArrayList<CircleMesh> circle, int e) throws SlickException {
 		im.destroy();
 		planet.remove(e);
 		
 	}
 	public int getHp(){
 		return hp;
 	}
 	
 	public int getMaxHp(){
 		return maxhp;
 	}
 
 }
