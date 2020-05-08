 package rogueshadow.SpaceRPG;
 
 import java.util.ArrayList;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.geom.Vector2f;
 
 import rogueshadow.SpaceRPG.entities.Bullet;
 import rogueshadow.SpaceRPG.entities.Planet;
 import rogueshadow.SpaceRPG.entities.PlayerShip;
 import rogueshadow.SpaceRPG.entities.Ship;
 import rogueshadow.SpaceRPG.entities.Star;
 import rogueshadow.SpaceRPG.entities.WorldObject;
 
 public class Minimap {
 	float zoom = 1f;
 	float x = 0;
 	float y = 0;
 	float width = 0;
 	float height = 0;
 	Vector2f pos = new Vector2f(0,0);
 	
 	public void setTracking(Vector2f pos){
 		this.pos = pos;
 	}
 	
 	public Minimap(float x,float y, float width, float height){
 		super();
 		this.x = x;
 		this.y = y;
 		this.width = width;
 		this.height = height;
 	}
 	
 	public void render(Graphics g){
 		float x = 0;
 		float y = 0;
 		float w = getWidth()/2f;
 		float h = getHeight()/2f;
 		g.pushTransform();
 		g.translate(getX(), getY());
 		g.setColor(Color.black);
 		g.fillRect(0,0, getWidth(), getHeight());	
 		g.setColor(Color.cyan);
 		g.drawRect(0,0, getWidth(), getHeight());
 		
 		ArrayList<WorldObject> maplist;
		maplist = Engine.getWorld().dataStructure.get(Engine.getWorld().getCamera().getBB().copy().grow(4300));
 		
 		for (WorldObject e: maplist){
 			if (e instanceof PlayerShip){
 				g.setColor(Color.green);
 			}else
 			if (e instanceof Planet){
 				g.setColor(Color.blue);
 			}else
 			if (e instanceof Star){
 				g.setColor(Color.yellow);
 			}else
 			if (e instanceof Bullet){
 				g.setColor(Color.red);
 			}else
 			if (e instanceof Ship){
 				g.setColor(Color.orange);
 			}else{
 				g.setColor(Color.gray);
 			}
 			x = (((e.getX())*0.01f) - pos.x*0.01f)*getZoom() + w;
 			y = (((e.getY())*0.01f) - pos.y*0.01f)*getZoom() + h;
 			if (x < 0 || x > getWidth() || y < 0 || y > getHeight())continue;
 				g.drawRect(x, y, 1, 1);
 		}
 
 		g.popTransform();
 
 	}
 
 	public float getWidth() {
 		return width;
 	}
 
 	public void setWidth(float width) {
 		this.width = width;
 	}
 
 	public float getHeight() {
 		return height;
 	}
 
 	public void setHeight(float height) {
 		this.height = height;
 	}
 
 	public float getZoom() {
 		return zoom;
 	}
 
 	public void setZoom(float zoom) {
 		this.zoom = zoom;
 	}
 
 	public float getX() {
 		return x;
 	}
 
 	public void setX(float x) {
 		this.x = x;
 	}
 
 	public float getY() {
 		return y;
 	}
 
 	public void setY(float y) {
 		this.y = y;
 	}
 
 }
