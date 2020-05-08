 package edu.calpoly.csc.pulseman.gameobject;
 
 import java.util.List;
 
 import org.newdawn.slick.Animation;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.Input;
 import org.newdawn.slick.geom.Rectangle;
 
 import edu.calpoly.csc.pulseman.Main;
 import edu.calpoly.csc.pulseman.World;
 import edu.calpoly.csc.pulseman.Main.GameState;
 
 public class Player extends Entity
 {
 	public static final int STAND = 0;
 	public static final int WALKR = 1;
 	public static final int WALKL = 2;
 	public static final int JUMPR = 3;
 	public static final int JUMPL = 4;
 
 	Direction dir;
 	int state;
 	private static Animation anim;
 	private static Image image[];
 	public final static float PLAYER_SPEED = 0.25f, JUMP_VELOCIYY = 0.4f;
 
 	public static void init(Animation walkAnim, Image standImg, Image jumpImg)
 	{
 		Player.anim = walkAnim;
 		Player.image = new Image[2];
 		Player.image[0] = standImg;
 		Player.image[1] = jumpImg;
 	}
 
 	public Player(int x, int y)
 	{
 		super(new Rectangle(x, y, anim.getWidth(), anim.getHeight()), false);
 		dir = Direction.LEFT;
 	}
 
 	@Override
 	public void render(GameContainer gc, Graphics g)
 	{
 		switch(state)
 		{
 		case WALKR:
 			g.drawImage(anim.getCurrentFrame(), position.x, position.y);
 			break;
 		case WALKL:
 			g.drawImage(anim.getCurrentFrame(), position.x, position.y, anim.getWidth(), 0, 0, anim.getHeight());
 			break;
 		case JUMPR:
 			g.drawImage(image[1], position.x, position.y);
 			break;
 		case JUMPL:
 			g.drawImage(image[1], position.x, position.y, anim.getWidth(), 0, 0, anim.getHeight());
 			break;
 		default:
 			g.drawImage(image[0], position.x, position.y);
 			break;
 		}
 
 	}
 
 	@Override
 	public void update(GameContainer gc, int delta)
 	{
 		Input input = gc.getInput();
 		anim.update(delta);
 		state = STAND;
 
 		if(bounds.getMaxY() > World.getWorld().getLevelHeight())
 		{
 			Main.setState(GameState.GAMEOVER);
 		}
 
 		if(input.isKeyDown(Input.KEY_A) || input.isKeyDown(Input.KEY_LEFT))
 		{
 			state = (isOnGround() == true) ? WALKL : JUMPL;
 			dir = Direction.LEFT;
 
 			if(isOnGround() || velocity.x >= -PLAYER_SPEED)
 			{
 				velocity.x = -PLAYER_SPEED;
 			}
 		}
 		if(input.isKeyDown(Input.KEY_D) || input.isKeyDown(Input.KEY_RIGHT))
 		{
 			state = (isOnGround() == true) ? WALKR : JUMPR;
 			dir = Direction.RIGHT;
 
 			if(isOnGround() || velocity.x <= PLAYER_SPEED)
 			{
 				velocity.x = PLAYER_SPEED;
 			}
 		}
 
 		if(input.isKeyDown(Input.KEY_SPACE) && floor != null)
 		{
 			velocity.y -= JUMP_VELOCIYY;
 		}
 
 		super.update(gc, delta);
 	}
 
 	@Override
 	protected void handleCollision(Collidable collidable, Rectangle oldBounds)
 	{
 		if(collidable instanceof Murderer)
 		{
 			Main.setState(GameState.GAMEOVER);
 		}
 		else if(collidable instanceof Goal)
 		{
 			World.getWorld().nextLevel();
 		}
 		else
 		{
 			super.handleCollision(collidable, oldBounds);
 
 			List<Enemy> enemies = World.getWorld().getEnemies();
 			for(int i = 0; i < enemies.size(); ++i)
 			{
 				if(bounds.intersects(enemies.get(i).bounds))
 				{
					super.handleCollision(enemies.get(i), oldBounds);
 				}
 			}
 		}
 	}
 
 	@Override
 	public boolean isAffectedByPulse()
 	{
 		return false;
 	}
 }
