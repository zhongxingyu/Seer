 package nitrogene.core;
 
 import nitrogene.collision.Circle;
 import nitrogene.collision.Vector;
 
 import org.newdawn.slick.Image;
 
 public class CircleMesh {
 	public Circle boundbox;
 	public Image meshImage = null;
	private int scalefactor;
 	
	public CircleMesh(float centerx, float centery, Image theimage, int scalefactor){
 		this.meshImage = theimage;
		boundbox = new Circle(meshImage.getWidth()/2*scalefactor);
 		Update(centerx, centery);
		this.scalefactor = scalefactor;
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
	public int getScaleFactor(){
		return scalefactor;
	}
 	public float getX(){
 		return boundbox.center.x - boundbox.radius;
 	}
 	public float getY(){
 		return boundbox.center.y - boundbox.radius;
 	}
 }
