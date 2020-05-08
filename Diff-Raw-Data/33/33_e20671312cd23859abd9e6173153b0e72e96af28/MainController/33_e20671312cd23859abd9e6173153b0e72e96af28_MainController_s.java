 /**
  * 
  */
 package de.findus.cydonia.main;
 
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 import com.jme3.app.Application;
 import com.jme3.bullet.BulletAppState;
 import com.jme3.bullet.BulletAppState.ThreadingType;
 import com.jme3.bullet.collision.PhysicsCollisionListener;
 import com.jme3.math.FastMath;
 import com.jme3.math.Matrix3f;
 import com.jme3.math.Quaternion;
 import com.jme3.math.Transform;
 
 import de.findus.cydonia.events.Event;
 import de.findus.cydonia.events.EventListener;
 import de.findus.cydonia.events.EventMachine;
 import de.findus.cydonia.level.WorldController;
 import de.findus.cydonia.player.Player;
 import de.findus.cydonia.player.PlayerController;
 
 /**
  * @author Findus
  *
  */
 public abstract class MainController extends Application implements PhysicsCollisionListener, EventListener {
 		
 	    public static float PLAYER_SPEED = 5f;
 	    public static float PHYSICS_ACCURACY = (1f / 192);
 	    
 	    public static Transform ROTATE90LEFT = new Transform(new Quaternion().fromRotationMatrix(new Matrix3f(1, 0, FastMath.HALF_PI, 0, 1, 0, -FastMath.HALF_PI, 0, 1)));
 	    
 	    private GameState gamestate;
 	    
 	    private GameConfig gameConfig;
 	    
 	    private WorldController worldController;
 	    
 	    private PlayerController playerController;
 	    
 	    private BulletAppState bulletAppState;
 	    
 	    private EventMachine eventMachine;
 	    
 	    private ConcurrentLinkedQueue<Event> eventQueue;
 	    
 
 	    @Override
 	    public void initialize() {
 	        super.initialize();
 	        
 	        this.gamestate = GameState.LOADING;
 	        
 	        gameConfig = new GameConfig(true);
 	        eventMachine = new EventMachine();
 	        eventQueue = new ConcurrentLinkedQueue<Event>();
 	        
 	    	bulletAppState = new BulletAppState();
 	    	bulletAppState.setEnabled(false);
 	        bulletAppState.setThreadingType(ThreadingType.PARALLEL);
 	        stateManager.attach(bulletAppState);
 	        bulletAppState.getPhysicsSpace().setMaxSubSteps(16);
 	        bulletAppState.getPhysicsSpace().setAccuracy(PHYSICS_ACCURACY);
 	        bulletAppState.getPhysicsSpace().addCollisionListener(this);
 	        
 	        worldController = new WorldController(assetManager, bulletAppState.getPhysicsSpace());
 	        eventMachine.registerListener(this);
 			
			playerController = new PlayerController(assetManager, worldController, eventMachine);
 	    }
 	    
 	    protected void cleanup() {
 	    	bulletAppState.setEnabled(false);
 	    	eventMachine.stop();
 	    }
 	    
 	    @Override
 	    public void update() {
 	        super.update(); // makes sure to execute AppTasks
 	        handleEvents();
 	    }
 
 	    protected abstract void handleEvent(Event e);
 
 		private void handleEvents() {
 			Event e = null;
 			while ((e = eventQueue.poll()) != null) {
 				this.handleEvent(e);
 			}
 		}
 
 		@Override
 		public void newEvent(Event e) {
 			eventQueue.offer(e);
 		}
 		
 		protected void killPlayer(Player p) {
 			if(p == null) return;
 			worldController.detachPlayer(p);
 			p.setAlive(false);
 		}
 		
 		protected void joinPlayer(int playerid, String playername) {
 			Player p = playerController.createNew(playerid);
 			p.setName(playername);
 		}
 		
 		protected void quitPlayer(Player p) {
 			if(p == null) return;
 			worldController.detachPlayer(p);
 			playerController.removePlayer(p.getId());
 		}
 		
 		protected void respawn(final Player p) {
 			if(p == null) return;
 			p.setHealthpoints(100);
 			p.setAlive(true);
 
 			p.getControl().setPhysicsLocation(worldController.getSpawnPoint(p.getTeam()).getPosition());
 			worldController.attachPlayer(p);
 		}
 		
 		protected void chooseTeam(Player p, int team) {
 			if(p == null) return;
 			playerController.setTeam(p, team);
 		}
 		
 		public GameState getGamestate() {
 			return gamestate;
 		}
 
 	    /**
 		 * @param gamestate the gamestate to set
 		 */
 		public void setGamestate(GameState gamestate) {
 			this.gamestate = gamestate;
 		}
 
 		/**
 		 * @return the gameConfig
 		 */
 		public GameConfig getGameConfig() {
 			return gameConfig;
 		}
 
 		/**
 		 * @return the bulletAppState
 		 */
 		public BulletAppState getBulletAppState() {
 			return bulletAppState;
 		}
 
 		/**
 		 * @return the worldController
 		 */
 		public WorldController getWorldController() {
 			return worldController;
 		}
 
 		/**
 		 * @return the playerController
 		 */
 		public PlayerController getPlayerController() {
 			return playerController;
 		}
 
 		/**
 		 * @return the eventMachine
 		 */
 		public EventMachine getEventMachine() {
 			return eventMachine;
 		}
 }
