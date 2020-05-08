 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.geom.Vector2f;
 
 public class Pad {
 	public boolean visible;
 	private Rectangle rec;
 	private Color filter = new Color ( 1.0f , 1.0f, 1.0f );
 	private float minVelocity = 3.0f;
 	private float velocity  =  3.0f;
 	private float aDown = 0.3f;
 	private float aStopping = 0.1f;
 	private int oldMouseX;
 	private float dX = 0.0f;
	private final float enCoef = 1.60f;
 	
 	public Pad ( Vector2f pos ,Vector2f size ) {
 		rec = new Rectangle( pos.x, pos.y, size.x, size.y );
 		oldMouseX = (int) rec.getCenterX();
 	}
 	public void update(GameContainer gc, int delta) throws SlickException {
 		dX = 0.0f;
 		if ( gc.getInput().isKeyDown( Input.KEY_LEFT )) {
 			dX -= velocity;
 			rec.setX(rec.getX() - velocity);
 			velocity += aDown;
 		}
 		if ( rec.getX() < 0 ) {
 			rec.setX(0);
 			dX -= rec.getX();
 			velocity = minVelocity;
 		}
 		if ( gc.getInput().isKeyDown( Input.KEY_RIGHT ) ) {
 			dX += velocity;
 			rec.setX(rec.getX() + velocity);
 			velocity += aDown; 
 		}
 		if ( rec.getMaxX() > gc.getWidth() ) {
 			rec.setX( gc.getWidth() - rec.getWidth() );
 			dX -= rec.getMaxX()-gc.getWidth();
 			velocity = minVelocity;
 		}
 		velocity -= aStopping;
 		if ( velocity < minVelocity )
 			velocity = minVelocity;
 		float sX = gc.getInput().getMouseX() - oldMouseX;
 		float newX = rec.getX()+ sX;
 		oldMouseX = gc.getInput().getMouseX();
 		if ( newX > 0 && newX + rec.getWidth() < gc.getWidth())
 		{
 			rec.setX(newX);
 			dX += sX;
 		}
 	}
 	
 	public float getDisplacement() {
 		return dX;
 	}
 	
 	public Rectangle getRectangle() {
 		return new Rectangle( rec.getX(), rec.getY(), rec.getWidth(), rec.getHeight());
 	}
 	
 	public void render(GameContainer gc, Graphics g) throws SlickException {
 		g.setColor(filter);
 		g.fill(rec);
 	}
 	
 	public void setColor (Color c) {
 		filter = c;
 	}
 	
 	public void enlarge() {
 		float cent = rec.getCenterX();
 		rec.setWidth(rec.getWidth()*enCoef );
 		rec.setCenterX(cent);
 	}
 	
 	public void shorten() {
 		float cent = rec.getCenterX();
 		rec.setWidth(rec.getWidth()/enCoef );
 		rec.setCenterX(cent);
 	}
 }
