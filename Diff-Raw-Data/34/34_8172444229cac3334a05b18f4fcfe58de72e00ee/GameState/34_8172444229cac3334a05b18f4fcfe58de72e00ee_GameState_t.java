 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 
 public class GameState extends BasicGameState
 
 {
 	private World<GameObject> world;
 	private Engine engine;
 	private StateBasedGame parent;
 	
 	public GameState()
 	{
 		// TODO Auto-generated constructor stub
 	}
 
 	public void init (GameContainer gc, StateBasedGame parent)
 			throws SlickException
 	{
 		Player p;
 		world = new LimitedWorld<GameObject>(600, 800, 0.85);
		world.addToWorld(new Magnet(Pole.PARA, 20, 20, 80, 20, 150));
 		world.addToWorld(new Magnet(Pole.PARA, 400, 300, 80, 20, 150));
 		//world.addToWorld(new Magnet(Pole.DIA, 20, 580, 20, 80, 1500));
 		//world.addToWorld(new Magnet(Pole.DIA, 520, 20, 20, 80, 1500));
 		p = new Player(200, 300, 10, 10);
		p.setXSpeed(-130);
		p.setYSpeed(50);
 		p.addSelfToWorld(world);
 		//p2.addSelfToWorld(world);
 		//p3.addSelfToWorld(world);
 		engine = new Engine(world);
 		this.parent = parent;
 	}
 
 	public void update (GameContainer gc, StateBasedGame parent, int delta)
 			throws SlickException
 	{
		engine.recalculateMovement(2*delta);
		engine.handleMovement(2*delta);
 	}
 
 	public void keyPressed (int key, char c)
 	{
 		if (key == 1)
 			parent.enterState (1);
 			
 	}
 
 
 	public void render (GameContainer gc, StateBasedGame parent, Graphics g)
 			throws SlickException
 	{
 		engine.renderImages(g);
 	}
 
 	@Override
 	public int getID()
 	{
 		return 3;
 	}
 }
