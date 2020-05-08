 package game;
 
 import java.util.*;
 
 import game.entity.PlayerEntity;
 
 import game.entity.MoveableEntity;
 import game.net.NetworkingClient;
 import initial3d.*;
 import initial3d.engine.*;
 import game.net.*;
 import game.net.packets.MovementPacket;
 import game.states.*;
 
 /***
  * Main game class
  * @author Ben
  *
  */
 public class Game implements Runnable {
 	private final int gameHz = 30;
 	private final long optimalTime = 1000000000 / gameHz;
 	
 	private GameState currentGameState;
 	private Thread gameThread = null;
 	private boolean gameRunning = false;
 	private RenderWindow gameWindow = null;
 	private SceneManager sceneManager = null;
 	private int updatesPerSecond = 0;
 	private NetworkingClient network = null;
 	private NetworkingHost nhost = null;
 	private int playerIndex = -1;
 	
 	public PlayerEntity player = null;
 	public MoveableEntity[] players = new MoveableEntity[4];
 	
 	public Game()
 	{
 		this.changeState(new PreloadGameState(this));
 	}
 	
 	/***
 	 * Starts the main game including the game loop
 	 */
 	public void start()
 	{
 		this.gameRunning = true;
 		this.gameThread = new Thread(this);
 		this.gameThread.start();
 	}
 	
 	public void stop()
 	{
 		this.gameRunning = false;
 		try {
 			this.gameThread.join();
 		} catch (InterruptedException e) {
 			System.out.println("Error: Unable to stop the main game thread");
 		}
 	}
 	
 	/***
 	 * The main game loop thread - contains a much better implementation of ben a's update loop
 	 */
 	public void run()
 	{
 		long lastUpdateTime = System.nanoTime();
 		long lastUpsTime = 0;
 		int ups = 0;
 		
 		while(gameRunning)
 		{
 			long now = System.nanoTime();
 			long updateLength = now - lastUpdateTime;
 			lastUpdateTime = now;
 			double delta = updateLength / 1000000000d;
 			
 			lastUpsTime += updateLength;
 			ups++;
 			
 			if(lastUpsTime >= 1000000000)
 			{
 				this.updatesPerSecond = ups;
 				lastUpsTime = 0;
 				ups = 0;
 			}
 			this.getState().update(delta);
 			
 			try
 			{
 				long sleepTime = (lastUpdateTime - System.nanoTime() + optimalTime) / 1000000;
 				if(sleepTime > 0)
 					Thread.sleep(sleepTime);
 				
 			}
 			catch(Exception e)
 			{
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/***
 	 * Changes the main game state to the state supplied
 	 * @param gs The state to be used as the main game state from now on
 	 */
 	public void changeState(GameState gs)
 	{		
 		Profiler p = new Profiler();
 		p.setResetOutput(System.out);
 		
 		p.startSection("ChangeState-DestroyOldGameState()");
 		if(this.currentGameState != null)
 			this.currentGameState.destroy();
 		p.endSection("ChangeState-DestroyOldGameState()");
 		
 		this.currentGameState = gs;
 		
 		p.startSection("ChangeState-InitaliseNewState()");
 		this.currentGameState.initalise();
 		p.endSection("ChangeState-InitaliseNewState()");
 		
 		p.startSection("ChangeState-AttachToScene()");
 		if(this.sceneManager != null)
 			this.sceneManager.attachToScene(this.currentGameState.scene);
 		p.endSection("ChangeState-AttachToScene()");
 		
 		// This is used to figure out how long stuff is going.. disabled most of the time!
 		// p.reset();
 	}
 	
 	/***
 	 * Returns the game state currently being run by the game
 	 * @return The current GameState
 	 */
 	public GameState getState()
 	{
 		return this.currentGameState;
 	}
 	
 	public int getUps()
 	{
 		return this.updatesPerSecond;
 	}
 
 	/***
 	 *  Ensures the game has a valid game window
 	 */
 	public void createWindow()
 	{
 		this.gameWindow = RenderWindow.create(800, 600);
 		this.gameWindow.setVisible(true);
 		this.sceneManager = new SceneManager(800, 600);
 		RenderWindow rwin = this.gameWindow;
 		this.sceneManager.setDisplayTarget(rwin);
 		rwin.addKeyListener(sceneManager);
 		rwin.addCanvasMouseListener(sceneManager);
 		rwin.addCanvasMouseMotionListener(sceneManager);
 		rwin.addMouseWheelListener(sceneManager);
 		
 		this.sceneManager.getProfiler().setResetOutput(null);
 	}
 	
 	public RenderWindow getWindow()
 	{
 		return this.gameWindow;
 	}	
 	
 	public NetworkingClient getNetwork()
 	{
 		return this.network;
 	}
 	
 	public void setNetwork(NetworkingClient nm)
 	{
 		if(this.network != null)
 			this.network.destroy();
 		
 		this.network = nm;
 		
 		this.network.start(this);
 	}
 
 	public void setHost(NetworkingHost networkingHost) {
 		if(this.nhost != null)
 			this.nhost.destroy();
 		
 		this.nhost = networkingHost;
 		
 		this.nhost.start(this);
 	}
 
 	public void setPlayerIndex(int pIndex) 
 	{
 		System.out.printf("Player index set to %d\n", pIndex);
 		this.playerIndex = pIndex;
		this.player = new PlayerEntity(1, Vec3.create(pIndex+2, 0.25, pIndex+2), 0.1);
 		
 		// This needs to add the main player
 		addPlayer(pIndex, player);
 		
 		// These can add the rest of the players
 		for(int i = 0; i < 4; i++)
 		{
 			if(i == pIndex) continue;
 			
			PlayerEntity p = new PlayerEntity(1, Vec3.create(i+2, 0.25, i+2), 0.1);
 			addPlayer(i, p);
 		}
 	}
 
 	public int getPlayerIndex() {
 		return this.playerIndex;
 	}
 	
 	public void addPlayer(int index, MoveableEntity e)
 	{
 		System.out.printf("Added player to players (%d)\n", index);
 		this.players[index] = e;
 	}
 
 	public void movePlayer(int playerIndex, Vec3 position, Vec3 velocity)
 	{
 		System.out.printf("playerIndex=%d\n", playerIndex);
 		MoveableEntity me = this.players[playerIndex];
 		if(me == null)
 			return;
 		
 		System.out.println("here");
 		
 		//TODO needs tp be more than 2 variables, and needs a syncronized timestamp thingymobob
 		me.updateMotion(position, velocity, Quat.one, Vec3.zero, System.currentTimeMillis());
 		
 		System.out.printf("Moving player %d to %s\n", playerIndex, position.toString());
 	}
 
 	public void transmitPlayerPosition() {
 		MovementPacket mp = new MovementPacket(this.getPlayerIndex(), this.player.getPosition(), this.player.getLinVelocity());
 		this.getNetwork().send(mp.toData());
 	}
 }
