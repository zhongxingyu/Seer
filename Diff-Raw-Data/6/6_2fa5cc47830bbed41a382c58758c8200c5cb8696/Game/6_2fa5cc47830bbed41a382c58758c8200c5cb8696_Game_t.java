 package bomberman.game;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import bomberman.game.character.BomberHuman;
 import bomberman.game.objects.Bomb;
 import bomberman.game.objects.Exit;
 import bomberman.gui.GameGui;
 
 /**
  * Game Object After constructing a Game object, it requires to be started via
  * {@link #start()} separately. Once started, it should not terminate until
  * either you win or lose. Only one Game at a time should be active, so don't
  * use it in threads, else the GUI will probably get messed up.
  * 
  * @author philipp
  * 
  */
 public class Game {
 
 	// it's not in use for now...
 	// private final Board board;
 
 	protected final List<BomberHuman> bman = new ArrayList<BomberHuman>();
 	protected final Exit exit;
 	protected final List<Bomb> bombs = new ArrayList<Bomb>();
 	// let's see if we can use this interface for something productive
 	private final List<Character> enemies = new ArrayList<Character>();
 	protected final GameGui gui;
 	protected final Controls controls;
 	private final ExplosionAreaCalculator eac;
 
 	protected boolean alive = true;
 	protected boolean won = false;
 
 	protected long lastTickAt = System.currentTimeMillis();
 
 	/**
 	 * Associates a Game with controls, an Exit, an EAC and a GUI. It will also
 	 * create a BomberHuman that is controlled by the player.
 	 * 
 	 * @param controls
 	 * @param exit
 	 * @param eac
 	 * @param gui
 	 */
 	public Game(final Controls controls, final Exit exit,
 			final ExplosionAreaCalculator eac, final GameGui gui) {
 		// this.board = board;
 		bman.add(new BomberHuman(true, 25, 25));
 		this.exit = exit;
 		this.eac = eac;
 		this.gui = gui;
 		this.controls = controls;
 	}
 
 	/**
 	 * Starts the Game loop.
 	 */
 	public void start() {
 		gui.initialize();
 		// DEBUG mode
		bman.get(0).boostSpeed(2);
 
 		loop();
 	}
 
 	protected void loop() {
 		while (alive && !won) {
 			final long diff = System.currentTimeMillis() - lastTickAt;
 			if (diff < 5) {
 				try {
 					Thread.sleep(5 - diff);
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 			checkWin();
 			controls.doSomethingWithInput(bman.get(0), bombs);
 			manageBombs();
 			gui.draw(bombs, bman, exit);
 			lastTickAt = System.currentTimeMillis();
 		}
 	}
 
 	protected void manageBombs() {
 		final List<Integer> exploded = new ArrayList<Integer>();
 
 		int count = 0;
 
 		for (Bomb b : bombs) {
 			b.tick();
 			final boolean exists = b.isStillThere();
 
 			if (!exists) {
 				exploded.add(count);
 			}
 			count++;
 		}
 
 		count = 0;
 
 		for (Integer integer : exploded) {
 			final Bomb b = bombs.get(integer - count);
 			if (b.getTimer() == -1) {
 				tryToKillStuff(b);
 				eac.affectedWalls(b);
 			}
 
 			if (!b.isCurrentlyExploding()) {
 				bombs.remove(integer - count);
				count++;
 			}
 		}
 	}
 
 	private void tryToKillStuff(final Bomb b) {
 		// right now, there's only bomberman. as soon as enemies are
 		// implemented, we should add a list of Characters or something like
 		// that. we will probably need that interface at this point.
 		for (Bomb bomb : bombs)
 			if (bomb != b && !bomb.isCurrentlyExploding())
 				if (eac.isInExplosionArea(b, bomb)) {
 					bomb.goBomf();
 				}
 
 		if (eac.isInExplosionArea(b, bman.get(0))) {
 			alive = false;
 			gui.lost();
 		}
 	}
 
 	protected void checkWin() {
 		if (!enemies.isEmpty())
 			return;
 
 		if (bman.get(0).getPosX() < exit.getPosX() + 20
 				&& bman.get(0).getPosX() > exit.getPosX() - 20
 				&& bman.get(0).getPosY() < exit.getPosY() + 20
 				&& bman.get(0).getPosY() > exit.getPosY() - 20) {
 			gui.won();
 			won = true;
 		}
 	}
 
 }
