 package platformer.core.model.gamestate.impl;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import platformer.core.model.GameObject;
 import platformer.core.model.GameState;
 import platformer.core.model.Level;
 import platformer.core.model.systems.Cullable;
 import platformer.core.model.systems.Simulatable;
 import platformer.core.renderer.Renderable;
 
 import com.badlogic.gdx.Gdx;
 
 public class GameStateImpl implements GameState {
 
 	/**
 	 * Keep track of all game objects.
 	 */
 	private final List<GameObject> gameObjects = new LinkedList<GameObject>();
 
 	/**
 	 * Keep track of all {@link Renderable} objects.
 	 */
 	private final List<Renderable> renderableObjects = new LinkedList<Renderable>();
 
 	private final List<GameObject> toRemove = new LinkedList<GameObject>();
 
 	private final Map<String, GameObject> objectsById = new TreeMap<String, GameObject>();
 
 	private Collection<GameObject> simulatableObjects = new LinkedList<GameObject>();
 
 	private Collection<GameObject> cullableObjects = new LinkedList<GameObject>();
 
 	public void addGameObject(GameObject gameObject) {
 		gameObjects.add(gameObject);
 
 		if (gameObject.getId() != null) {
 			Gdx.app.log("gamestate", "adding new object");
 			objectsById.put(gameObject.getId(), gameObject);
 		}
 
 		if (gameObject instanceof Renderable) {
 			final Renderable renderable = (Renderable) gameObject;
 			renderableObjects.add(renderable);
 		}
 
 		if (gameObject instanceof Simulatable) {
 			simulatableObjects.add(gameObject);
 		}
 
 		if (gameObject instanceof Cullable) {
 			cullableObjects.add(gameObject);
 		}
 	}
 
 	public void cleanUp() {
 
 		for (GameObject go : toRemove) {
 			go.dispose();
 			gameObjects.remove(go);
 			renderableObjects.remove(go);
			cullableObjects.remove(go);
 		}
 
 		toRemove.clear();
 	}
 
 	public Collection<Renderable> getRenderableObjects() {
 		return renderableObjects;
 	}
 
 	@Override
 	public Iterator<GameObject> iterator() {
 		return gameObjects.iterator();
 	}
 
 	public void update() {
 		for (GameObject go : gameObjects) {
 			if (go.canBeRemoved()) {
 				toRemove.add(go);
 			}
 		}
 	}
 
 	@Override
 	public void initialize(Level level) {
 
 		for (GameObject go : level.getGameObjects()) {
 			addGameObject(go);
 		}
 
 	}
 
 	@Override
 	public GameObject findGameObjectById(String id) {
 		return objectsById.get(id);
 	}
 
 	@Override
 	public Collection<GameObject> getSimulatableObjects() {
 		return simulatableObjects;
 	}
 
 	@Override
 	public Collection<GameObject> getCullableObjects() {
		return cullableObjects;
 	}
 
 }
