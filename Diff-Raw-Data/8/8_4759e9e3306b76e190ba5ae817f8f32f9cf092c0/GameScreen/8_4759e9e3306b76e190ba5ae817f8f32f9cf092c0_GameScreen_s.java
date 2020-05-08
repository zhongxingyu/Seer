 package edu.calpoly.csc.pulseman;
 
 import edu.calpoly.csc.pulseman.util.AtomicFloat;
 
 import java.util.LinkedList;
 
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 import org.newdawn.slick.KeyListener;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.Input;
 
 import edu.calpoly.csc.pulseman.gameobject.Enemy;
 import edu.calpoly.csc.pulseman.gameobject.KillingObstacle;
 import edu.calpoly.csc.pulseman.gameobject.Player;
 import edu.calpoly.csc.pulseman.gameobject.Tile;
 
 public class GameScreen implements GameInterface, KeyListener
 {
 	public static final float DECAY_VALUE = .99f;
 	public static final float MAX_MULT = 20.0f;
 	public static final float MAX_SPEEDUP = 10.0f;
 
 	private static final boolean PULSE_ENABLED = false;
 
 	private Camera cam = new Camera();
 	private AtomicFloat timeMult;
 
 	public GameScreen()
 	{
 		timeMult = new AtomicFloat(0.0f);
 	}
 
 	@Override
 	public void render(GameContainer gc, Graphics g)
 	{
 		cam.render(gc, g, World.getWorld().getPlayer());
 
 		World.getWorld().render(gc, g);
 		LinkedList<String> messageQueue = Main.getMessageQueue();
 		synchronized(messageQueue)
 		{
 			for(int i = 0; i < messageQueue.size(); ++i)
 			{
 				g.drawString(messageQueue.get(i), 100, 150 + (i * 50));
 			}
 		}
 	}
 
 	@Override
 	public void init(GameContainer gc) throws SlickException
 	{
 		Player.init(new Image("res/monk.png"));
		Enemy.init(new Image("res/e.png"));
 
 		KillingObstacle.init("res/spike.png");
 		Tile.init(new Image("res/brick.png"));
 		World.getWorld().loadLevel("res/level001.png");
 		gc.getInput().addKeyListener(this);
 	}
 
 	@Override
 	public void update(GameContainer gc, int dt)
 	{
 		timeMult.set(timeMult.get() * DECAY_VALUE);
 
 		if(PULSE_ENABLED)
 		{
 			dt = (int)((float)dt * MAX_SPEEDUP * Math.min(timeMult.get(), MAX_MULT) / MAX_MULT);
 		}
 
 		World.getWorld().update(gc, dt);
 	}
 
 	@Override
 	public void inputEnded()
 	{
 	}
 
 	@Override
 	public void inputStarted()
 	{
 	}
 
 	@Override
 	public boolean isAcceptingInput()
 	{
 		return true;
 	}
 
 	@Override
 	public void setInput(Input arg0)
 	{
 	}
 
 	@Override
 	public void keyPressed(int key, char c)
 	{
 		if(key == Input.KEY_1)
 		{
 			playerTwoTap();
 		}
 		else if(key == Input.KEY_0)
 		{
 			Main.setState(Main.GameState.GAMEOVER);
 		}
 	}
 
 	public void playerTwoTap()
 	{
 		timeMult.set(timeMult.get() + 1.0f);
 	}
 
 	@Override
 	public void keyReleased(int arg0, char arg1)
 	{
 	}
 
 }
