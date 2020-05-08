 package nitrogene.core;
 
 import nitrogene.collision.Circle;
 import nitrogene.collision.Vector;
 
 import org.newdawn.slick.Image;
 
 public class CircleMesh {
 	public Circle boundbox;
 	public Image meshImage = null;
 	
	public CircleMesh(float centerx, float centery, Image theimage){
 		this.meshImage = theimage;
		boundbox = new Circle(meshImage.getWidth()/2*4);
 		Update(centerx, centery);
 	}
 	
 	public void Update(float x, float y){
 		Vector vec = new Vector();
 		vec.x = x;
 		vec.y = y;
 		boundbox.update(vec);
 	}
 	
 	public Image getImage(){
 		return meshImage;
 	}
	
 	public float getX(){
 		return boundbox.center.x - boundbox.radius;
 	}
 	public float getY(){
 		return boundbox.center.y - boundbox.radius;
 	}
 }
