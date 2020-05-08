 package org.doublelong.jastroblast.entity;
 
 import java.util.Random;
 
 import org.doublelong.jastroblast.renderer.AsteroidRenderer;
 
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 
 public class Asteroid
 {
 
 	private final Space space;
 
 	// TODO fix Height/Width, should be associated with the sprite
 	private static final float WIDTH = 136f;
 	private static final float HEIGHT = 111f;
 
 
 	private final Vector2 position;
 	public Vector2 getPosition() { return this.position; }
 
 	private final Rectangle bounds;
 	public Rectangle getBounds() { return this.bounds; }
 
 	private final Vector2 velocity;
 
 	private float angle;
 	public float getAngle() { return this.angle; }
 	public void setAngle(float angle) { this.angle = angle; }
 
 	private final float spin;
 	private final int direction;
 
 	public AsteroidRenderer renderer;
 
 	public Asteroid(Space space, Vector2 position)
 	{
 		this.space = space;
 		this.position = position;
 		this.velocity = new Vector2();
 		this.bounds = new Rectangle(this.position.x, this.position.y, Asteroid.WIDTH, Asteroid.HEIGHT);
 
 		this.spin = (float) Math.random() * 100;
 		this.direction = new Random().nextBoolean() ? 1 : -1;
 
 		this.renderer = new AsteroidRenderer(this);
 	}
 
 	public void render(SpriteBatch batch, OrthographicCamera cam)
 	{
 		this.renderer.render(batch, cam);
 	}
 
 	public void update(float delta)
 	{
 		//System.out.println(this.angle);
 		this.angle += this.direction * this.spin * delta;
 		//this.setAngle(this.direction * this.spin * delta);
 
 		float scale_x = this.direction * (delta * 10f);
 		float scale_y = this.direction * (delta * 10f);
 
 		this.position.add(new Vector2(scale_x, scale_y));
 	}
 
 	public void dispose()
 	{
 		this.renderer.dispose();
 	}
 }
