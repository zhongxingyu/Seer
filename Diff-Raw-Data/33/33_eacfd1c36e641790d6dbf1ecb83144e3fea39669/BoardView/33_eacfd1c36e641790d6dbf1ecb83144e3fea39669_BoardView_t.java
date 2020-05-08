 package se.chalmers.dryleafsoftware.androidrally.libgdx;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import se.chalmers.dryleafsoftware.androidrally.libgdx.actions.GameAction;
 import se.chalmers.dryleafsoftware.androidrally.libgdx.gameboard.AnimatedElement;
 import se.chalmers.dryleafsoftware.androidrally.libgdx.gameboard.CheckPointView;
 import se.chalmers.dryleafsoftware.androidrally.libgdx.gameboard.CollisionMatrix;
 import se.chalmers.dryleafsoftware.androidrally.libgdx.gameboard.ConveyorBeltView;
 import se.chalmers.dryleafsoftware.androidrally.libgdx.gameboard.GearsView;
 import se.chalmers.dryleafsoftware.androidrally.libgdx.gameboard.LaserView;
 import se.chalmers.dryleafsoftware.androidrally.libgdx.gameboard.MapBuilder;
 import se.chalmers.dryleafsoftware.androidrally.libgdx.gameboard.RobotView;
 import se.chalmers.dryleafsoftware.androidrally.libgdx.gameboard.DockView;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.Texture.TextureFilter;
 import com.badlogic.gdx.graphics.Texture.TextureWrap;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.scenes.scene2d.Group;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
 import com.badlogic.gdx.scenes.scene2d.ui.Table;
 import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
 
 /**
  * This view class displays everything on the game area.
  * 
  * @author
  *
  */
 public class BoardView extends Stage {
 
 	private List<RobotView> robots = new ArrayList<RobotView>();
 	private final List<AnimatedElement> animated = new ArrayList<AnimatedElement>();
 	private final Vector2[] docks = new Vector2[8];
 	private final Group container; 
 	private final Table scrollContainer;
 	private final ScrollPane pane;
 	private CollisionMatrix collisionMatrix;
 	private final List<CheckPointView> checkPoints = new ArrayList<CheckPointView>();
 	private MapBuilder mapBuilder;
 			
 	/**
 	 * Creates a new instance of BoardView.
 	 */
 	public BoardView() {
 		super();
 		// Create a group of all the objects on the map.
 		container = new Group();
 		container.setPosition(0, 0);
 //		container.setSize(480, 480);		
 		
 		// Create a inner container for the the ScrollPane.
 		scrollContainer = new Table();
 		scrollContainer.setFillParent(true);
 		scrollContainer.setLayoutEnabled(false);
 		scrollContainer.add(container);
 		
 		// Create the ScrollPane.
         ScrollPaneStyle scrollStyle = new ScrollPaneStyle();
         pane = new ScrollPane(scrollContainer, scrollStyle);
         pane.setForceOverscroll(true, true);
         pane.setFillParent(true);
         
         // Create a container to hold the ScrollPane.
         Table scrollParent = new Table();
         scrollParent.add(pane);
 //        scrollParent.debug();
         scrollParent.setPosition(0, 320);
         scrollParent.setSize(480, 480);
         addActor(scrollParent);
         
         // Create a default camera.
         OrthographicCamera boardCamera = new OrthographicCamera(480, 800);
 		boardCamera.position.set(240, 400, 0f);
 		boardCamera.update();
 		setCamera(boardCamera);
 	}
 	
 	public void restore(String data, Texture texture) {
 		String[] chunks = data.split("b");		
 		createBoard(texture, chunks[1]);
 		
 		String[] robots = chunks[0].split("a");
 		for(int i = 0; i < robots.length; i++) {
 			String[] r = robots[i].split(":");
 			float x = Float.parseFloat(r[0]);
 			float y = Float.parseFloat(r[1]);
 			float rot = Float.parseFloat(r[2]);
 			int lives = Integer.parseInt(r[3]);
 			int damage = Integer.parseInt(r[4]);
 			RobotView robot = getRobots().get(i);
 			robot.setX(x);
 			robot.setY(y);
 			robot.setRotation(rot);
 			robot.setLives(lives);
 			robot.setDamage(damage);
 			container.removeActor(robot);
 			container.addActor(robot);
 		}
 	}
 	
 	// [robot]b[robot]b[robot]ba[map]
 	public String getSaveData() {
 		StringBuilder sb = new StringBuilder();
 		
 		// [x]:[y]:[dir]:[lives]:[damage]
 		for(RobotView r : getRobots()) {
 			sb.append(r.getX() + ":");
 			sb.append(r.getY() + ":");
 			sb.append(r.getRotation() + ":");
 			sb.append(r.getLives() + ":");
 			sb.append(r.getDamage());
 			sb.append("a");
 		}
 		sb.append("b");
 		sb.append(mapBuilder.getMap());
 		return sb.toString();
 	}
 	
 	/**
 	 * Builds the board using the specified texture and map data.
 	 * @param texture The texture to use.
 	 * @param map An array of strings describing the map's layout.
 	 * NOTE: The bottom four rows of the map array will always be created as the dock area.
 	 */
 	public void createBoard(final Texture texture, String map) {
 		final Texture conveyerTexture = new Texture(Gdx.files.internal("textures/special/conveyor.png"));
 		conveyerTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
 		conveyerTexture.setWrap(TextureWrap.ClampToEdge, TextureWrap.Repeat);
 
 		// To be added on top of all the other objects
 		final List<Image> overlay = new ArrayList<Image>();
		final List<AnimatedElement> lasers = new ArrayList<AnimatedElement>();
 
 		this.mapBuilder = new MapBuilder(map) {
 			@Override
 			public void buildFactoryFloor(int x, int y) {
 				Image floor = new Image(new TextureRegion(texture, 0, 0, 64, 64));
 				floor.setSize(40, 40);
 				floor.setPosition(40 * x, getHeight() * 40 - 40 * (y+1));
 				container.addActor(floor);
 			}
 			@Override
 			public void buildDockFloor(int x, int y) {
 				Image floor = new Image(new TextureRegion(texture, 64, 0, 64, 64));
 				floor.setSize(40, 40);
 				floor.setPosition(40 * x, getHeight() * 40 - 40 * (y+1));
 				container.addActor(floor);
 			}
 			@Override
 			public void buildHole(int x, int y) {
 				container.addActor(setCommonValues(
 						new Image(new TextureRegion(texture, 128, 0, 64, 64)), x, y));
 			}
 			@Override
 			public void buildGear(int x, int y, boolean cw) {
 				animated.add((AnimatedElement)setCommonValues(
 						new GearsView(new TextureRegion(texture, 0, 256, 64, 64), cw), x, y));
 			}
 			@Override
 			public void buildConveyerBelt(int x, int y, int speed, int dir) {
 				animated.add((AnimatedElement)setCommonValues(
 						new ConveyorBeltView(new TextureRegion(
 								conveyerTexture, 64 * (speed - 1), 0, 64, 64), 
 								dir * 90, speed), x, y));
 			}
 			@Override
 			public void buildCheckPoint(int x, int y, int nbr) {
 				CheckPointView cv = new CheckPointView(nbr);
 				container.addActor(setCommonValues(cv, x, y));
 				checkPoints.add(cv);
 			}
 			@Override
 			public void buildRepair(int x, int y) {
 				container.addActor(setCommonValues(
 						new Image(new TextureRegion(texture, 256, 0, 64, 64)), x, y));
 						
 			}
 			@Override
 			public void buildStartDock(int x, int y, int nbr) {
 				container.addActor(setCommonValues(
 						new DockView(new TextureRegion(
 								texture, 320, 0, 64, 64), nbr), x, y));
 				docks[nbr - 1] = new Vector2(40 * x, getHeight() * 40 - 40 * (y+1));
 			}
 			@Override
 			public void buildWall(int x, int y, int dir) {
 				overlay.add(setCommonOverlayValues(
 						new Image(new TextureRegion(texture, 384, 0, 64, 64)), x, y, dir));
 				collisionMatrix.setWall(x, y, dir);
 			}
 			@Override
 			public void buildLaser(int x, int y, int dir) {
 				overlay.add(setCommonOverlayValues(
 						new Image(new TextureRegion(texture, 448, 0, 64, 64)), x, y, dir));
				lasers.add((AnimatedElement)setCommonValues(
 						new LaserView(new TextureRegion(texture, 128, 320, 64, 64), (dir + 2)%4), x, y));
 			}
 			private Image setCommonOverlayValues(Image overlayImage, int x, int y, int dir) {
 				overlayImage.setSize(40, 40);
 				Vector2 pos = convertToMapY(x, y);
 				overlayImage.setPosition(pos.x, pos.y + 20);
 				overlayImage.setOrigin(overlayImage.getWidth()/2 , 
 						overlayImage.getHeight()/2 - 20);
 				overlayImage.rotate(-(dir) * 90);
 				return overlayImage;	
 			}
 			private Image setCommonValues(Image i, int x, int y) {
 				i.setSize(40, 40);
 				Vector2 pos = convertToMapY(x, y);
 				i.setPosition(pos.x, pos.y);
 				i.setOrigin(20, 20);
 				return i;
 			}
 			@Override
 			public Vector2 convertToMapY(int x, int y) {
 				return new Vector2(40 * x, getHeight() * 40 - 40 * (y+1));
 			}
 		};
 		collisionMatrix = new CollisionMatrix(mapBuilder.getWidth(), mapBuilder.getHeight());
 		mapBuilder.build();
			
		animated.addAll(lasers);
 		for(AnimatedElement i : animated) {
 			container.addActor(i);
		}		
 		//Add walls (added last to be on top of everything else)
 		for(Image i : overlay) {
 			container.addActor(i);
 		}
 		
 		container.setSize(mapBuilder.getWidth() * 40,
 				mapBuilder.getHeight() * 40);	
 		scrollContainer.setSize(480, 480);
 		pane.setScrollingDisabled(mapBuilder.getWidth() < 12, mapBuilder.getHeight() < 12);
 		pane.layout();
 		pane.setScrollPercentY(100);
 	}
 	
 	public MapBuilder getMapBuilder() {
 		return this.mapBuilder; 
 	}
 	
 	public List<CheckPointView> getCheckPoints() {
 		return this.checkPoints;
 	}
 
 	/**
 	 * Starts the animations of the items connected with the specified phase.
 	 * @param phase The phase to animate.
 	 */
 	public void setAnimate(int phase, int speed) {
 		if(phase == GameAction.PHASE_LASER) {
 			collisionMatrix.clearDynamic();
 			for(RobotView robot : robots) {
 				if(robot.isDead()) {
 					animated.remove(robot.getLaser());
 					container.removeActor(robot.getLaser());
 				}else{
 					animated.add(robot.getLaser());
 					container.addActor(robot.getLaser());
 					collisionMatrix.setDynamic((int)(robot.getX()/40), 15 - (int)(robot.getY()/40));
 				}
 			}
 		}
 		for(AnimatedElement a : animated) {
 			a.enable(phase, speed);
 			if(phase == GameAction.PHASE_LASER && a instanceof LaserView) {
 				((LaserView)a).setCollisionMatrix(collisionMatrix);
 			}
 		}
 	}
 
 	/**
 	 * Stops all animations.
 	 */
 	public void stopAnimations() {
 		for(AnimatedElement a : animated) {
 			a.disable();
 		}
 	}
 	
 	/**
 	 * Gives an array of the eight docks' positions stored as vectors where (0,0) is the top
 	 * left corner and (40, 40) is the tile to the right and down.
 	 * @return An array of the eight docks.
 	 */
 	public Vector2[] getDocksPositions() {
 		return this.docks;
 	}
 	
 	/**
 	 * Adds the specified player to the board.
 	 * @param player The player to add.
 	 */
 	public void addRobot(RobotView player) {
 		robots.add(player);
 		container.addActor(player);
 	}
 	
 	/**
 	 * Gives a list of all the robots on the board.
 	 * @return A list of all the robots on the board.
 	 */
 	public List<RobotView> getRobots() {
 		return this.robots;
 	}
 	
 	public void setRobots(List<RobotView> robots) {
 		for(RobotView r : this.robots) {
 			container.removeActor(r);
 		}
 		this.robots = robots;
 		for(RobotView r : this.robots) {
 			container.addActor(r);
 		}
 	}
 }
