 package gameObjects;
 
 import game.PlayerInfo;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 
 import com.golden.gamedev.Game;
 
 import states.FullHealthState;
 import states.LowHealthState;
 import states.State;
 import states.StateFactories;
 import states.StateFactory;
<<<<<<< HEAD
import states.StateLoader;
 import levelLoadSave.ForSave;
=======
 import states.EnemyDataLoader;
>>>>>>> 4e0395a3eeb379cfa46b648ddc6219a8ee4bc8c5
 import movement.BackForthMovement;
 import movement.Movement;
 import movement.MovementFactories;
 import movement.MovementFactory;
 import movement.TargetedMovement;
 
 /**
  * 
  * @author James Pagliuca
  * 
  */
 
 @ForSave
 public class Enemy extends GameObject {
 
 	private ArrayList<State> possibleStates;
 	private State currentState;
 	private EnemyDataLoader loader;
 	private int currentHealth;
 	private static int count=0;
 
 	public Enemy(double x, double y, String imgPath, String filename) {
 		myX = x;
 		myY = y;
 		myImgPath = imgPath;
 		myType = "Enemy";
 		setLocation(myX, myY);
 		FileInputStream f;
 		try {
 			f = new FileInputStream(filename);
 			loader = new EnemyDataLoader(f);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		possibleStates = new ArrayList<State>();
 		parseStates(parseMovementTypes());
 		currentState = possibleStates.get(0);
 		currentHealth = 500;
 	}
 
 	@Override
 	public String getImgPath() {
 		return myImgPath;
 	}
 
 	public void move() {
 		currentState.move();
 		currentHealth--;
 	}
 
 	public void updateEnemy() {
 		move();
 		checkState();
 	}
 
 	public void checkState() {
 		for (State s : possibleStates) {
 			if (s.shouldBeCurrentState()) {
 				currentState = s;
 			}
 		}
 	}
 
 	public int getCurrentHealth() {
 		return currentHealth;
 	}
 
 	public State getCurrentState() {
 		return currentState;
 	}
 
 	public void sustainDamage(int damage) {
 		currentHealth -= damage;
 	}
 
 	private ArrayList<Movement> parseMovementTypes() {
 		MovementFactories factoryInfo = new MovementFactories();
 		ArrayList<MovementFactory> movementFactories = factoryInfo
 				.getAllMovementFactories();
 		ArrayList<String> movementInfo = loader.getMovementInfo();
 		ArrayList<Movement> movementTypes = new ArrayList<Movement>();
 		for (String s : movementInfo) {
 			String[] parameters = s.split(",");
 			String name = parameters[0];
 			System.out.println(name);
 			for (MovementFactory f : movementFactories) {
 				if (f.isMyMovement(name)) {
 					Movement newMovement = f.makeMyMovement(parameters);
 					movementTypes.add(newMovement);
 				}
 			}
 		}
 		return movementTypes;
 	}
 
 	private void parseStates(ArrayList<Movement> movementTypes) {
 		StateFactories factoryInfo = new StateFactories();
 		ArrayList<StateFactory> stateFactories = factoryInfo
 				.getAllStateFactories();
 		ArrayList<String> stateInfo = loader.getStateInfo();
 		for (int i = 0; i < stateInfo.size(); i++) {
 			String[] parameters = stateInfo.get(i).split(",");
 			String name = parameters[0];
 			for (StateFactory f : stateFactories) {
 				if (f.isMyState(name)) {
 					State newState = f.makeMyState(this, parameters,
 							movementTypes, i);
 					possibleStates.add(newState);
 				}
 			}
 		}
 	}
 
 	@Override
 	public GameObject makeGameObject(GameObjectData god) {
 		Double x = god.getX();
 		Double y = god.getY();
 		String imgPath = god.getImgPath();	
 		count++;
 		String filename = "stateInfo"+count+ ".txt";
 		System.out.println(count);
 		return new Enemy(x, y, imgPath, filename);
 
 	}
 
 	/**
 	 * Enemy() and getFactory() must be implemented by each game object; they
 	 * are used for the factory system.
 	 */
 	private Enemy() {
 		myType = "Enemy";
 	}
 
 	public static GameObjectFactory getFactory() {
 		return new GameObjectFactory(new Enemy());
 	}
 
 }
