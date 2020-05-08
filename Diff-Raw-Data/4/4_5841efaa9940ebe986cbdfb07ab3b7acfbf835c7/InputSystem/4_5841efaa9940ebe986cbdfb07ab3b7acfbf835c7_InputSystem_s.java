 package se.exuvo.planets.systems;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import se.exuvo.planets.EntityFactory;
 import se.exuvo.planets.components.Position;
 import se.exuvo.planets.components.Size;
 import se.exuvo.planets.components.Vector2Component;
 import se.exuvo.planets.components.Velocity;
 import se.exuvo.settings.Settings;
 
 import com.artemis.Aspect;
 import com.artemis.ComponentMapper;
 import com.artemis.Entity;
 import com.artemis.EntitySystem;
 import com.artemis.annotations.Mapper;
 import com.artemis.utils.Bag;
 import com.artemis.utils.ImmutableBag;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
 import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
 import com.badlogic.gdx.math.MathUtils;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.math.Vector3;
 
 /**
  * The system responsible for handling user input (keyboard and mouse).
  */
 public class InputSystem extends EntitySystem implements InputProcessor {
 
 	@Mapper ComponentMapper<Size> sm;
 	@Mapper ComponentMapper<Position> pm;
 	@Mapper ComponentMapper<Velocity> vm;
 
 	/** The gameworld-camera. */
 	private OrthographicCamera camera;
 	private Vector3 cameraVelocity;
 	private float cameraMoveSpeed = 1000f;
 	private float zoomLevel = 1f, zoomSensitivity = Settings.getFloat("zoomSensitivity");
 
 	private Vector2 mouseVector;
 
 	/**
 	 * Used to hold the mouseposition where the user last placed a planet. It is used for when the user rightclick-drags the mouse, creating
 	 * a planet at the rightclick and giving it an velocity in the dragged-to direction.
 	 */
 	private Vector2 mouseStartVector;
 
 	// Buffers due to gui has to be done in the correct thread.
 	private boolean createPlanet, selectPlanet, potentialMove, movePlanet, pushPlanet, releasePlanet, follow;
 	private Bag<Entity> selectedPlanets;
 	private long potentialMoveStart, potentialMoveTimeDelay = Settings.getInt("moveDelay");
 	private float potentialMoveMouseShake = Settings.getFloat("moveMouseSensitivity"), pushForceMultiplier = Settings
 			.getFloat("pushForceMultiplier");
 
 	private ShapeRenderer render;
 	private SpriteBatch renderBatch;
 	private HudRenderSystem hudSys;
 
 	private boolean paused, wasPaused;
 
 	private List<PlanetSelectionChanged> listeners = new ArrayList<PlanetSelectionChanged>();
 	private UISystem uisystem;
 
 	public InputSystem(OrthographicCamera camera) {
 		super(Aspect.getAspectForAll(Position.class, Size.class));
 		this.camera = camera;
 		mouseVector = new Vector2();
 		mouseStartVector = new Vector2();
 		cameraVelocity = new Vector3();
 	}
 
 	// TODO dragging selected planet
 
 	@Override
 	protected void initialize() {
 		render = new ShapeRenderer();
 		renderBatch = new SpriteBatch();
 		hudSys = world.getSystem(HudRenderSystem.class);
 		uisystem = world.getSystem(UISystem.class);
 
 		selectedPlanets = new Bag<Entity>();
 
 		if (zoomSensitivity <= 1) {
 			System.out.println("Warning zoomSensitivity is too low to have any effect: " + zoomSensitivity);
 		}
 	}
 
 	@Override
 	protected void begin() {
 		render.setProjectionMatrix(camera.combined);
 		renderBatch.setProjectionMatrix(camera.combined);
 		renderBatch.begin();
 	}
 
 	@Override
 	protected void processEntities(ImmutableBag<Entity> entities) {
 		// TODO separate the various operations into methods.
 		camera.position.add(cameraVelocity.cpy().mul(camera.zoom * Gdx.graphics.getDeltaTime()));
 		updateMouse();
 
 		if (createPlanet) {
			Entity planet = EntityFactory.createHollowPlanet(world, uisystem.getRadius(), uisystem.getMass(), new Vector2(mouseVector.x,
					mouseVector.y), uisystem.getColor());
 			planet.addToWorld();
 			selectedPlanets.add(planet);
 
 			createPlanet = false;
 		}
 
 		if (selectPlanet) {
 			Entity planet = null;
 
 			// compare each planets position to the mousePos to see if it was clicked.
 			for (int i = 0; i < entities.size(); i++) {
 				Entity e = entities.get(i);
 				Position p = pm.get(e);
 				Size s = sm.get(e);
 				if (mouseVector.dst(p.vec) < s.radius * 2) {
 					planet = e;
 					break;
 				}
 			}
 
 			if (planet == null) {
 				if (!Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
 					selectedPlanets.clear();
 					fireSelectionChangeEvent();
 				}
 			} else {
 				if (selectedPlanets.contains(planet)) {
 					// Allow for moving planets
 //					selectedPlanets.remove(planet);
 //					fireSelectionChangeEvent();
 				} else {
 					if (!Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)) {
 						selectedPlanets.clear();
 					}
 					selectedPlanets.add(planet);
 					fireSelectionChangeEvent();
 
 					if (follow) {
 						potentialMove = false;
 					}
 				}
 			}
 			selectPlanet = false;
 		}
 
 		if (!selectedPlanets.isEmpty()) {
 			render.begin(ShapeType.Triangle);
 			render.setColor(Color.CYAN);
 
 			for (Entity e : selectedPlanets) {
 				Position p = pm.get(e);
 				Size s = sm.get(e);
 
 				// draw a triangle around the planet (showing that it's selected)
 				float r = s.radius * 3f;
 				render.triangle(p.vec.x + r * MathUtils.cosDeg(90), p.vec.y + r * MathUtils.sinDeg(90),
 								p.vec.x + r * MathUtils.cosDeg(210), p.vec.y + r * MathUtils.sinDeg(210),
 								p.vec.x + r * MathUtils.cosDeg(330), p.vec.y + r * MathUtils.sinDeg(330));
 			}
 
 			render.end();
 		}
 
 		if (potentialMove && !selectedPlanets.isEmpty()) {
 			if (mouseDiff().len() > potentialMoveMouseShake * camera.zoom
 					|| System.currentTimeMillis() - potentialMoveStart > potentialMoveTimeDelay) {
 				movePlanet = true;
 				potentialMove = false;
 				checkPause();
 			}
 		}
 
 		if (movePlanet) {
 			Vector2 diff = mouseDiff();
 			render.begin(ShapeType.Circle);
 			render.setColor(Color.WHITE);
 
 			for (Entity e : selectedPlanets) {
 				Vector2 p = pm.get(e).vec.cpy().add(diff);
 				Size s = sm.get(e);
 				render.circle(p.x, p.y, s.radius);
 			}
 			render.end();
 
 			render.begin(ShapeType.Line);
 			render.line(mouseStartVector.x, mouseStartVector.y, mouseVector.x, mouseVector.y);
 			render.end();
 
 			hudSys.font.draw(renderBatch, diff.toString(), mouseVector.x, mouseVector.y);
 		}
 
 		if (pushPlanet) {
 			// from where the planet was created (old mousePos) to the current mousePos
 			float angle = MathUtils.atan2(mouseVector.x - mouseStartVector.x, mouseStartVector.y - mouseVector.y);
 
 			float size = 10 * camera.zoom;
 			float xr = size * MathUtils.cos(angle);
 			float yr = size * MathUtils.sin(angle);
 
 			// draw an arrow-like triangle from startMouse to current mousePos
 			render.begin(ShapeType.FilledTriangle);
 			render.setColor(Color.CYAN);
 			render.filledTriangle(	mouseStartVector.x + xr, mouseStartVector.y + yr, mouseStartVector.x - xr, mouseStartVector.y - yr,
 									mouseVector.x, mouseVector.y);
 			render.end();
 
 			hudSys.font.draw(renderBatch, "" + mouseDiff().len() * pushForceMultiplier, mouseVector.x, mouseVector.y);
 		}
 
 		if (releasePlanet) {
 			Vector2 diff = mouseDiff();
 			if (pushPlanet) {
 				// give the planet a velocity. (with the angle and magnitude the user showed)
 				diff.mul(pushForceMultiplier);
 
 				for (Entity e : selectedPlanets) {
 					vm.get(e).vec.add(diff);
 				}
 
 				pushPlanet = false;
 				restorePause();
 			}
 
 			if (movePlanet) {
 				for (Entity e : selectedPlanets) {
 					pm.get(e).vec.add(diff);
 				}
 
 				movePlanet = false;
 				restorePause();
 			}
 
 			releasePlanet = false;
 		}
 
 		if (follow && !selectedPlanets.isEmpty()) {
 			Vector2 center = Vector2Component.mean(pm, selectedPlanets);
 			camera.position.set(center.x, center.y, 0);
 		}
 	}
 
 	@Override
 	protected void end() {
 		renderBatch.end();
 	}
 
 	private void checkPause() {
 		wasPaused = paused;
 		if (Settings.getBol("pauseWhenCreatingPlanets")) {
 			setPaused(true);
 		}
 	}
 
 	private void restorePause() {
 		if (!wasPaused) {
 			setPaused(false);
 		}
 	}
 
 	private void updateMouse() {
 		Vector3 mouseTmp = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
 
 		// unproject screen coordinates to corresponding world position
 		camera.unproject(mouseTmp);
 		mouseVector.set(mouseTmp.x, mouseTmp.y);
 	}
 
 	private void mouseStart() {
 		mouseStartVector.set(mouseVector);
 	}
 
 	private Vector2 mouseDiff() {
 		return mouseVector.cpy().sub(mouseStartVector);
 	}
 
 	@Override
 	protected boolean checkProcessing() {
 		return true;
 	}
 
 	public boolean isPaused() {
 		return paused;
 	}
 
 	public boolean isFollow() {
 		return follow;
 	}
 
 	private void setPaused(boolean newValue) {
 		paused = newValue;
 	}
 
 	public boolean isSpeedup() {
 		return !isPaused() && Gdx.input.isKeyPressed(Input.Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Input.Keys.CONTROL_RIGHT);
 	}
 
 	public boolean isSSpeedup() {
 		return !isPaused() && Gdx.input.isKeyPressed(Input.Keys.ALT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.ALT_RIGHT);
 	}
 
 	@Override
 	public boolean keyDown(int keycode) {
 		if (keycode == Input.Keys.SPACE) {
 			setPaused(!paused);
 			return true;
 		} else if (keycode == Input.Keys.W || keycode == Input.Keys.UP) {
 			cameraVelocity.y += cameraMoveSpeed;
 			return true;
 		} else if (keycode == Input.Keys.S || keycode == Input.Keys.DOWN) {
 			cameraVelocity.y += -cameraMoveSpeed;
 			return true;
 		} else if (keycode == Input.Keys.D || keycode == Input.Keys.RIGHT) {
 			cameraVelocity.x += cameraMoveSpeed;
 			return true;
 		} else if (keycode == Input.Keys.A || keycode == Input.Keys.LEFT) {
 			cameraVelocity.x += -cameraMoveSpeed;
 			return true;
 		} else if (keycode == Input.Keys.T) {
 			follow = !follow;
 			return true;
 		}
 
 		return false;
 	}
 
 	@Override
 	public boolean keyUp(int keycode) {
 		if (keycode == Input.Keys.W || keycode == Input.Keys.UP) {
 			cameraVelocity.y -= cameraMoveSpeed;
 			return true;
 		} else if (keycode == Input.Keys.S || keycode == Input.Keys.DOWN) {
 			cameraVelocity.y -= -cameraMoveSpeed;
 			return true;
 		} else if (keycode == Input.Keys.D || keycode == Input.Keys.RIGHT) {
 			cameraVelocity.x -= cameraMoveSpeed;
 			return true;
 		} else if (keycode == Input.Keys.A || keycode == Input.Keys.LEFT) {
 			cameraVelocity.x -= -cameraMoveSpeed;
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public boolean keyTyped(char c) {
 		return false;
 	}
 
 	@Override
 	public boolean touchDown(int x, int y, int pointer, int button) {
 		mouseStart();
 
 		if (button == Input.Buttons.LEFT) {
 			selectPlanet = true;
 			potentialMove = true;
 			potentialMoveStart = System.currentTimeMillis();
 			return true;
 		} else if (button == Input.Buttons.RIGHT) {
 			if (selectedPlanets.isEmpty()) {
 				createPlanet = true;
 			} else {
 				pushPlanet = true;
 				checkPause();
 			}
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public boolean touchUp(int x, int y, int pointer, int button) {
 		if (button == Input.Buttons.RIGHT) {
 			releasePlanet = true;
 			return true;
 		} else if (button == Input.Buttons.LEFT) {
 			potentialMove = false;
 			releasePlanet = true;
 		}
 		return false;
 	}
 
 	@Override
 	public boolean touchDragged(int x, int y, int pointer) {
 		return false;
 	}
 
 	@Override
 	public boolean scrolled(int amount) {
 		// -1 for zoom-in. 1 for zoom out
 		float oldZoom = camera.zoom;
 
 		zoomLevel += amount;
 		if (zoomLevel < 0) {
 			zoomLevel = 0;
 		}
 
 		camera.zoom = (float) Math.pow(zoomSensitivity, zoomLevel);
 //		System.out.println("zoom:" + camera.zoom + "  zoomLevel:" + zoomLevel);
 
 //		camera.zoom += amount;
 		if (camera.zoom < 1) {
 			camera.zoom = 1;
 		}
 
 		if (amount < 0) {
 //			Det som var under musen innan scroll ska fortsätta vara där efter zoom
 //			http://stackoverflow.com/questions/932141/zooming-an-object-based-on-mouse-position
 
 			Vector3 diff = camera.position.cpy().sub(new Vector3(mouseVector.x, mouseVector.y, 0));
 			camera.position.sub(diff.sub(diff.cpy().div(oldZoom).mul(camera.zoom)));
 		}
 
 		return true;
 	}
 
 	@Override
 	public boolean mouseMoved(int screenX, int screenY) {
 		return false;
 	}
 
 	@Override
 	protected void removed(Entity e) {
 		selectedPlanets.remove(e);
 		fireSelectionChangeEvent();
 	};
 
 	public void addListener(PlanetSelectionChanged psc) {
 		listeners.add(psc);
 	}
 
 	private void fireSelectionChangeEvent() {
 		for (PlanetSelectionChanged psc : listeners) {
 			psc.planetSelectionChanged(selectedPlanets);
 		}
 	}
 
 	public static interface PlanetSelectionChanged {
 		public void planetSelectionChanged(Bag<Entity> planets);
 	}
 }
