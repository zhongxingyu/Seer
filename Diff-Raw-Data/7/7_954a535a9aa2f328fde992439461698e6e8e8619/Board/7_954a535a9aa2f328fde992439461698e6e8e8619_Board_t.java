 package org.doublelong.catchr.entity;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.doublelong.catchr.Catchr;
 
import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.assets.AssetManager;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.ParticleEffect;
 import com.badlogic.gdx.graphics.g2d.ParticleEmitter;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Contact;
 import com.badlogic.gdx.physics.box2d.World;
 import com.badlogic.gdx.utils.Array;
 
 public class Board
 {
 	public AssetManager manager;
 
 	private static final float SPAWN_WAIT_TIME = 2f;
 	private static final Vector2 GRAVITY = new Vector2(0, -50);
 	public static float UNIT_WIDTH = Catchr.WINDOW_WIDTH / 160;
 	public static float UNIT_HEIGHT = Catchr.WINDOW_HEIGHT / 160;
 
 	private float time = 0;
 	private final OrthographicCamera cam;
 
 	private final World world;
 	public World getWorld() {return this.world; }
 
 	private final Paddle player;
 	public Paddle getPlayer() { return this.player; }
 
 	private final Wall[] walls;
 
 	private int ballCount = 0;
 	private final int ballLimit = 10;
 	private final List<Ball> balls;
 
 	private final boolean debug;
 
 	private SpriteBatch batch;
 	private BitmapFont font;
 
 	private final ParticleEffect effect;
 	private Array<ParticleEmitter> emitters;
 
 	public Board(Catchr game, OrthographicCamera cam, boolean debug)
 	{
 		this.manager = new AssetManager();
 		this.debug = debug;
 
 		this.world = new World(GRAVITY, false);
 
 		this.cam = cam;
 		this.cam.setToOrtho(false, game.WINDOW_WIDTH, game.WINDOW_HEIGHT);
 
 		this.player = new Paddle(this.world, new Vector2(this.cam.viewportWidth / 2, 100f));
 
 		this.balls = this.generateBalls(1);
 		this.walls = this.generateWalls(2);
 
 		this.batch = new SpriteBatch();
 		this.font = new BitmapFont();
 
 		this.effect = new ParticleEffect();
		this.effect.load(Gdx.files.internal("assets/particles/squirt2.p"), Gdx.files.internal("assets/images"));
 	}
 
 	public void dispose()
 	{
 		this.world.dispose();
 		this.player.dispose();
 	}
 
 	public void tick(float delta)
 	{
 		this.time += delta;
 		if (this.time >= SPAWN_WAIT_TIME)
 		{
 			this.spwanBall();
 			this.time -= SPAWN_WAIT_TIME;
 		}
 	}
 
 	public void update(float delta)
 	{
 		this.cam.update();
 
 		this.player.controller.processControls();
 		this.batch.begin();
 		this.font.draw(this.batch, "Points: " + String.valueOf(this.player.getPoints()), 30f, 580f);
 
 		this.player.renderer.render(this.batch, this.cam);
 		for (int i = 0; i < this.balls.size(); i++)
 		{
 			Ball b = this.balls.get(i);
 			b.renderer.renderer(this.batch, this.cam);
 			if (b.getBody().getPosition().y < 0)
 			{
 				this.balls.remove(b);
 				this.world.destroyBody(b.getBody());
 			}
 
 		}
 		this.testCollisions(delta, this.batch);
 		this.effect.draw(this.batch, delta);
 
 		this.batch.end();
 		this.tick(delta);
 	}
 
 	private void spwanBall()
 	{
 		if (this.ballCount <= this.ballLimit)
 		{
 			this.balls.add(new Ball(this));
 			this.ballCount++;
 		}
 	}
 
 	private List<Ball> generateBalls(int n)
 	{
 		List<Ball> balls = new ArrayList<Ball>();
 		for(int i = 0; i < n; i++)
 		{
 			balls.add(new Ball(this));
 		}
 		return balls;
 	}
 
 	private Wall[] generateWalls(int n)
 	{
 		// there are only two walls
 		Wall[] walls = new Wall[n];
 		// left wall starts at: (0,0), bounds: (10f, viewportHeight)
 		walls[0] = new Wall(this.world, new Vector2(11, 1), new Vector2(10f, this.cam.viewportHeight));
 		// right wall starts at (viewportWidth - wall width,0), bounds: (10f, viewportHeight)
 		walls[1] = new Wall(this.world, new Vector2(this.cam.viewportWidth - 10, 1), new Vector2(10f, this.cam.viewportHeight));
 		// bottom wall: starts at (0, 0)
 		// walls[2] = new Wall(this.world, new Vector2(10f, 1f), new Vector2(this.cam.viewportWidth, 10f));
 		return walls;
 	}
 
 	private void testCollisions(float delta, SpriteBatch batch)
 	{
 		List<Contact> contactList = this.world.getContactList();
 		List<Ball> killList = new ArrayList<Ball>();
 
 		for(int i = 0; i < contactList.size(); i++)
 		{
 			Contact contact = contactList.get(i);
 			// test the contacts
 			if (contact.isTouching() && (contact.getFixtureA() == this.player.getSensorFixture() || contact.getFixtureB() == this.player.getSensorFixture()))
 			{
 				for (int j = 0; j < this.balls.size(); j++)
 				{
 					Ball b =  this.balls.get(j);
 					if (contact.getFixtureA() == b.getFixture() || contact.getFixtureB() == b.getFixture())
 					{
 						float p = b.getPoints();
 						this.player.addPoints(p);
 						b.setPoints(p + p);
 						b.setBounceCount(b.getBounceCount() + 1);
 						if (b.getBounceCount() > Ball.MAX_BOUNCE)
 						{
 							killList.add(b);
 							this.balls.remove(j);
 						}
 					}
 				}
 			}
 		}
 
 		if (killList.size() > 0)
 		{
 			for(int i = 0; i < killList.size(); i++)
 			{
 				Ball b = killList.get(i);
				b.explode(this.effect.getEmitters().get(0));
 				b.showPoints(batch);
 				killList.remove(b);
 
 				this.world.destroyBody(b.getBody());
 			}
 		}
 	}
 }
