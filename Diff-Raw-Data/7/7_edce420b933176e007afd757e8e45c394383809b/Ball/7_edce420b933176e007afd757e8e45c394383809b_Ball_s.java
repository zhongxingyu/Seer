 package org.doublelong.entities;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.utils.Pool;
 
 public class Ball
 {
 	private final Board board;
 
 	public enum States { ACTIVE }
 	public static Map<States, Boolean> states = new HashMap<Ball.States, Boolean>();
 	static {states.put(States.ACTIVE, false);}
 	public Boolean isActive() { return states.get(States.ACTIVE);}
 	public void setActive(Boolean b) {states.put(States.ACTIVE, b);}
 
 	public static final float SIZE = .25f;
 	private static final float SPEED = 2f;
 	private static final float MAX_VEL = 5f;
 	private final float ACCELERATION = 40f;
 
 	private final Vector2 velocity = new Vector2();
 	public Vector2 getVelocity() { return this.velocity; }
 
 	private final Vector2 acceleration = new Vector2();
 	public Vector2 getAcceleration() { return this.acceleration; }
 
 	private final Vector2 position;
 	public Vector2 getPosition() { return this.position; }
 
 	private final Rectangle bounds;
 	public Rectangle getBounds() { return this.bounds; }
 
 	private final float ballSpeed;
 	public float getBallSpeed() { return this.ballSpeed; }
 
 	private int direction_y = 1;
 	private int direction_x = 1;
 
 	private final ShapeRenderer renderer;
 
 	private final Pool<Rectangle> rectPool = new Pool<Rectangle>() {
 		@Override
 		protected Rectangle newObject()
 		{
 			return new Rectangle();
 		}
 	};
 
 	public Ball(Board board)
 	{
 		this.board = board;
 		this.ballSpeed = SPEED;
 
 		this.setActive(false);
 		this.position = this.board.paddle.getBallPosition();
 		this.bounds = new Rectangle(this.position.x, this.position.y, SIZE, SIZE);
 		this.renderer = new ShapeRenderer();
 	}
 
 	public void render(SpriteBatch batch, OrthographicCamera cam)
 	{
 		this.renderer.setProjectionMatrix(cam.combined);
 		this.renderer.begin(ShapeType.FilledRectangle);
 
 		this.renderer.setColor(Color.GREEN);
 		this.renderer.filledRect(this.position.x, this.position.y, this.bounds.height, this.bounds.width);
 
 		this.renderer.end();
 	}
 
 	public void update(float delta)
 	{
 		// if ball is active, move it around
 		if(this.isActive())
 		{
 			// check for collisions!
 			this.acceleration.mul(delta);
 			this.collides(delta);
 
 			this.velocity.add(this.acceleration.x, this.acceleration.y);
 			this.capVelocity();
 			if (this.position.y < 0 || this.position.y > Board.BOARD_HEIGHT)
 			{
 				this.reset();
 			}
 		}
 		else // it's not active, it should "attached" to the paddle
 		{
 			this.position.x = this.board.paddle.getBallPosition().x;
 			this.position.y = this.board.paddle.getBallPosition().y;
 			this.bounds.setX(this.position.x);
 			this.bounds.setY(this.position.y);
 		}
 	}
 
 	public void reset()
 	{
 		this.setActive(false);
 
 		this.getPosition().x = this.board.paddle.getBallPosition().x;
 		this.getPosition().y = this.board.paddle.getBallPosition().y;
 		this.bounds.x = this.position.x;
 		this.bounds.y = this.position.y;
 		this.velocity.x = 0f;
 		this.velocity.y = 0f;
 		this.acceleration.x = 0f;
 		this.acceleration.y = 0f;
 
 
 	}
 
 	private void collides(float delta)
 	{
 		this.velocity.mul(delta);
 		Rectangle r = rectPool.obtain();
 
 		r.set(this.bounds);
 
 		r.x += this.velocity.x;
 
 		// bouce off walls
 		for (Wall wall : this.board.walls.getBlocks())
 		{
 			if (r.overlaps(wall.getBounds()))
 			{
 				// walls have names, bricks don't
 				if (wall.getName().equals("left") || wall.getName().equals("right"))
 				{
 					this.direction_x *= -1;
 					this.velocity.x = -this.velocity.x;
 					this.acceleration.x = this.direction_x * 1f;
 				}
 				if (wall.getName().equals("top"))
 				{
 					this.direction_y *= -1;
 					this.velocity.y = -this.velocity.y;
 					this.acceleration.y = this.direction_y * 1f;
 				}
 				else
 				{
 					//this.acceleration.y -= ACCELERATION;
 				}
 				break;
 			}
 			else
 			{
 				this.acceleration.x = this.direction_x * 1f;
 				this.acceleration.y = this.direction_y * 1f;
 			}
 		}
 
 		// detected brick collisions
 		for (Block brick : this.board.bricks.getBlocks())
 		{
 			if (r.overlaps(brick.getBounds()) && !brick.isDestroyed())
 			{
 				brick.setDestroyed(true);
 				this.board.destroyedBricks = this.board.destroyedBricks + 1;
 				this.direction_x *= 1;
 				//this.velocity.x = this.velocity.x;
 				this.acceleration.x = this.direction_x * this.velocity.x;
 
 				this.direction_y *= -1;
 				this.velocity.y = -this.velocity.y;
 				this.acceleration.y += this.direction_y;
 
 
 			}
 			else
 			{
 				this.acceleration.x = this.direction_x * 1f;
 				this.acceleration.y = this.direction_y * 1f;
 			}
 		}
 
 		if (r.overlaps(this.board.paddle.getBounds()))
 		{
 
 			this.direction_y *= -1f;
 			this.velocity.y = -this.velocity.y;
 			this.acceleration.y = this.direction_y * 1f;
 
 			if (this.board.destroyedBricks == this.board.bricks.getBlocks().length)
 			{
 				this.reset();
 			}
 		}
 
 		r.x = this.position.x;
 		r.y = this.position.y;
 		this.position.add(this.velocity);
 		this.bounds.setX(this.position.x);
 		this.bounds.setY(this.position.y);
 
 		this.velocity.mul(1 / delta);
 	}
 
 	private void capVelocity()
 	{
 		if(this.velocity.y > MAX_VEL)
 		{
 			this.velocity.y = MAX_VEL;
 		}
 		if(this.velocity.y < -MAX_VEL)
 		{
 			this.velocity.y = -MAX_VEL;
 		}
 		if(this.velocity.x > MAX_VEL)
 		{
 			this.velocity.x = MAX_VEL;
 		}
 		if(this.velocity.x < -MAX_VEL)
 		{
 			this.velocity.x = -MAX_VEL;
 		}
 	}
 }
