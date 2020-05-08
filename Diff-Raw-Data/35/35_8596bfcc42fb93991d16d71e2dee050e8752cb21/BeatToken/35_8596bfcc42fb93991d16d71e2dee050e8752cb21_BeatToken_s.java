 package beatbots.simulation;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.geom.Vector2f;
 
 public strictfp final class BeatToken implements Collider {
 	
 	private static final float FALL_SPEED = 0.04f;
 	
 	private static final float BUFFER = 64f;
 	
 	private boolean isActive;
 	
 	private Beat beat;
 	
 	private Vector2f position;
 	
 	private Image image;
 	
 	private Color color;
 	
 	public boolean isActive() {
 		
 		return this.isActive;
 	}
 	
 	@Override
 	public Vector2f getPosition() {
 		
 		return this.position;
 	}
 
 	@Override
 	public float getRadius() {
 		
 		return this.image.getWidth() / 2;
 	}
 	
 	public Beat getBeat() {
 		
 		return this.beat;
 	}
 	
 	public BeatToken() {
 		
 		super();
 		
 		this.isActive = false;
 	}
 	
 	public void update(GameContainer gameContainer, int delta) {
 		
 		this.position.y += FALL_SPEED * delta;
 		
 		if (this.position.y > 480f + BUFFER) {
 			
			this.isActive = false;
 		}
 	}
 	
 	public void render(GameContainer gameContainer, Graphics graphics) {
 		
 		graphics.drawImage(
 				this.image, 
 				this.position.getX() - this.image.getWidth() / 2, 
 				this.position.getY() - this.image.getHeight() / 2, 
 				this.color);
 	}
 	
 	public void drop(Image image, Vector2f position, Beat beat) {
 		
 		this.isActive = true;
 		
 		this.image = image;
 		
 		this.position = position;
 		
 		this.beat = beat;
 		
 		this.color = Utils.getBeatColor(this.beat);
 	}
 	
 	public void destroy() {
 		
 		this.isActive = false;
 	}
 }
