 package game;
 
 import ia.IA;
 
 import java.util.concurrent.TimeUnit;
 
 import physics.BMWM3Properties;
 import physics.CarProperties;
 import physics.EnginePhysics;
 import physics.tools.Conversion;
 
 import com.jme3.app.Application;
 import com.jme3.app.state.AppStateManager;
 import com.jme3.bullet.collision.PhysicsCollisionEvent;
 import com.jme3.bullet.collision.shapes.BoxCollisionShape;
 import com.jme3.bullet.control.GhostControl;
 import com.jme3.math.Matrix3f;
 import com.jme3.math.Vector3f;
 import com.jme3.renderer.queue.RenderQueue.ShadowMode;
 import com.jme3.scene.Node;
 
 import de.lessvoid.nifty.elements.render.TextRenderer;
 
 public abstract class GameScreenState extends AbstractGameScreenState {
 	protected Car bot;
 	protected CarProperties botCarProperties;
 	protected EnginePhysics botEnginePhysics;
 	protected IA botIA;
 
 	protected Vector3f botArrivalPoint;
 
 	protected boolean botFinish;
 	protected boolean botStoped;
 
 	protected long timeBot;
 
 	protected GhostControl finishCell;
 	protected Node finishNode;
 	
 
 	public GameScreenState() {
 		super();
 	}
 
 	@Override
 	public void initialize(AppStateManager stateManager, Application a) {
 		/** init the screen */
 		super.initialize(stateManager, a);
 
 		initGame();
 	}
 
 	protected void initGame() {
 		super.initGame();
 
 		buildBot();
 		buildFinishLine();
 
 		playerFinish = false;
 		botFinish = false;
 		botStoped = false;
 	}
 
 	protected void buildFinishLine() {
 		// Init finish cell detection
 		finishCell = new GhostControl(new BoxCollisionShape(new Vector3f(40, 1,
 				1)));
 		finishNode = new Node("finish zone");
 		finishNode.addControl(finishCell);
 		finishNode.move(0, 27, 0);
 
 		rootNode.attachChild(finishNode); 
 		super.getPhysicsSpace().add(finishCell);
 	}
 	
 	private void buildBot() {
 		botCarProperties = new BMWM3Properties();
 		bot = new Car(assetManager, botCarProperties,
 				"Models/FerrariGreen/Car.scene");
 		bot.setPhysicsLocation(new Vector3f(10, 27, 700));
 		bot.getNode().setShadowMode(ShadowMode.CastAndReceive);
 		botEnginePhysics = bot.getEnginePhysics();
 		botIA = bot.getIA();
 		rootNode.attachChild(bot.getNode());
 		super.getPhysicsSpace().add(bot);
 		botArrivalPoint = (new Vector3f(0, 0, 0)).subtract(
 				bot.getForwardVector(null)).mult(5000);
 	}
 
 	@Override
 	public void update(float tpf) {
 		/** any main loop action happens here */
 		if (needReset) {
 			reset();
 			return;
 		}
 		super.update(tpf);
 
 		if (botFinish && !botStoped) {
 			bot.stop(1000);
 			botStoped = true;
 		}
 
 		// Tester si le round est fini
 		if (playerFinish && botFinish && !runFinish) {
 			String text = "";
 
 			if (timePlayer <= timeBot && !player.getBurstEnabled()) {
 				text = "Gagne !\n ";
 				audioMotor.playWin();
 			} else {
 				audioMotor.playLost();
 				text = "Perdu !\n ";
 			}
 			text += String.format("Joueur:  %d : %d\n",
 					TimeUnit.MILLISECONDS.toSeconds(timePlayer),
 					(timePlayer % 1000) / 1);
 			text += String.format("Bot:  %d : %d",
 					TimeUnit.MILLISECONDS.toSeconds(timeBot),
 					(timeBot % 1000) / 1);
 
 			screen.findElementByName("startTimer")
 					.getRenderer(TextRenderer.class).setText(text);
 
 			runFinish = true;
 			runIsOn = false;
 		}
 
 		int botSpeed = (int) Math.abs(bot.getCurrentVehicleSpeedKmHour());
 		if (runIsOn) {
 
 			long timeMili = (System.currentTimeMillis() - startTime);
 
 			String sTimer = String.format("%d : %d",
 					TimeUnit.MILLISECONDS.toSeconds(timeMili),
 					(timeMili % 1000) / 10);
 
 			screen.findElementByName("timer").getRenderer(TextRenderer.class)
 					.setText(sTimer);
 
 			if (!botFinish)	{
 				botEnginePhysics.setSpeed(Math.abs(Conversion.kmToMiles(botSpeed)));
 				bot.accelerate(-(float) botEnginePhysics.getForce() / 5);
 				botIA.act();
 				botIA.target(botArrivalPoint, 0, 0);
 			}
 		}
 	}
 
 	protected void reset() {
 		super.reset();
 
 		bot.accelerate(0);
 		bot.removeExplosion();
 		botEnginePhysics.setSpeed(0);
 		botEnginePhysics.setRpm(1000);
 
 		bot.setPhysicsLocation(new Vector3f(10, 27, 700));
 		bot.setPhysicsRotation(new Matrix3f());
 		bot.setLinearVelocity(Vector3f.ZERO);
 		bot.setAngularVelocity(Vector3f.ZERO);
 		botEnginePhysics.setGear(1);
 		bot.resetSuspension();
 
 		bot.steer(0);
 		
 		bot.setLife(100);
 
 		botFinish = false;
 		runIsOn = false;
 		needReset = false;
 		runFinish = false;
 		playerFinish = false;
 		botFinish = false;
 		botStoped = false;
 
 		startTime = 0;
 		countDown = 0;
 
 		screen.findElementByName("timer").getRenderer(TextRenderer.class).setText("0 : 0");
 		screen.findElementByName("startTimer").getRenderer(TextRenderer.class)
 				.setText("Ready ?");
 	}
 
 	@Override
 	public void collision(PhysicsCollisionEvent event) {
 		super.collision(event);
 		if (finishCell.getOverlappingObjects().contains(player)
 				&& !playerFinish) {
 			timePlayer = (System.currentTimeMillis() - startTime);
 			System.out.println(String.format("player : %d : %d",
 					TimeUnit.MILLISECONDS.toSeconds(timePlayer),
 					(timePlayer % 1000) / 10));
 
 			playerFinish = true;
 		}
 		if (finishCell.getOverlappingObjects().contains(bot) && !botFinish) {
 			timeBot = (System.currentTimeMillis() - startTime);
 			System.out.println(String.format("player : %d : %d",
 					TimeUnit.MILLISECONDS.toSeconds(timeBot),
 					(timeBot % 1000) / 10));
 
 			botFinish = true;
 		}
 
 	}
 }
