 package org.vergeman.sulfonicavenger;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Random;
 
 import org.lwjgl.Sys;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Input;
import org.newdawn.slick.Music;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.BasicGameState;
 import org.newdawn.slick.state.StateBasedGame;
 
 public class GamePlayState extends BasicGameState {
 
 	private int stateID;
 
 	private static int CENTIPEDE_SIZE = 8;
	private static int MAX_CENTIPEDES = 3;
 	private static int NUM_SHOTS = 10;
	private static int NUM_MOLECULES = 10;
 	private long CentipedeInterval = 10000; // ms
 	private long lastCentipede;
 
 	private enum STATES {
 		START_GAME_STATE, PLAY_GAME_STATE, GAME_OVER_STATE
 	}
 
 	private STATES currentState = null;
 	private int pause_counter = 0;
 
 	String TITLE_MSG = "SULFONIC          AVENGER";
 	float TITLE_SZ = 30f;
 	String SCORE_MSG = "SCORE  ";
 	float SCORE_SZ = 30f;
 	String STATE_MSG;
 	float STATE_SZ = 40f;
 	String HIGH_SCORE_MSG = "HIGH   SCORE  ";
 
 	WindowManager windowManager;
 	AssetManager assetManager;
 	TextDrawManager textDrawManager;
 
 	Color background;
 	Input input;
 
 	Random r;
 
 	PlayerEntity player;
 	ArrayList<MoleculeEntity> molecules;
 	ArrayList<NH3Entity> nh3s;
 	ShotEntity[] shots;
 	Sprite[] sprite_molecules;
 	Sprite sprite_molecule1;
 	Sprite sprite_molecule2;
 	Sprite sprite_molecule3;
 	Sprite sprite_damage;
 	Sprite sprite_centibody;
 	Sprite sprite_centihead;
 	Sprite sprite_shot;
 	Sprite sprite_nh3;
 	ArrayList<Centipede> centipedes;
 	boolean newCentipede = false;
 
 	int score;
 	int high_score = 0;
 	int last_life_score = 0;
 
 	int NH3SpawnInterval = 90000; // ms
 	long lastNH3;
 
 	public GamePlayState(int id) {
 		super();
 		this.stateID = id;
 	}
 
 	@Override
 	public void init(GameContainer container, StateBasedGame game)
 			throws SlickException {
 
 		container.setShowFPS(false);
 		container.getGraphics().setBackground(Color.black);
		
 		windowManager = new WindowManager(container, game);
 		assetManager = new AssetManager();
 		assetManager.init();
 
 		textDrawManager = new TextDrawManager(container.getGraphics(),
 				windowManager, assetManager);
 		textDrawManager.build("state", STATE_SZ);
 		textDrawManager.build("title", TITLE_SZ);
 		textDrawManager.build("score", SCORE_SZ);
 		textDrawManager.build("high_score", SCORE_SZ);
 
 		player = new PlayerEntity(
 				container,
 				assetManager,
 				new Sprite(assetManager.getImage("ship")),
 				(int) windowManager.getCenterX(),
 				(int) (windowManager.getCenterY() + windowManager.getCenterY() / 2));
 
 		score = 0;
 		last_life_score = 0;
 
 		/* // MOLECULES */
 		molecules = new ArrayList<MoleculeEntity>();
 		r = new Random();
 		sprite_molecules = new Sprite[3];
 		sprite_molecule1 = new Sprite(assetManager.getImage("molecule1"));
 		sprite_molecule2 = new Sprite(assetManager.getImage("molecule2"));
 		sprite_molecule3 = new Sprite(assetManager.getImage("molecule3"));
 		sprite_molecules[0] = sprite_molecule1;
 		sprite_molecules[1] = sprite_molecule2;
 		sprite_molecules[2] = sprite_molecule3;
 
 		sprite_damage = new Sprite(assetManager.getImage("damage"));
 
 		int w, h;
 		int s_w = sprite_molecule1.getWidth();
 		int s_h = sprite_molecule1.getHeight();
 		HashMap<String, Boolean> molecule_pos = new HashMap<String, Boolean>();
 
 		// spawn evenly on sprite dimensions
 		while (molecule_pos.size() < NUM_MOLECULES) {
 			w = ((int) (r.nextDouble() * container.getWidth()) / s_w) * s_w;
 
 			h = ((int) (r.nextDouble() * (container.getHeight() - container
 					.getHeight() / 3)) / s_h) * s_h;
 
 			if (!(h == 0 && w < 5 * s_w)) {
 				molecule_pos.put(w + "-" + h, true);
 			}
 		}
 
 		int type;
 		for (String pos : molecule_pos.keySet()) {
 			type = (int) (r.nextDouble() * 3 - .1);
 			molecules.add(new MoleculeEntity(sprite_molecules[type], type + 1,
 					Integer.valueOf(pos.split("-")[0]), Integer.valueOf(pos
 							.split("-")[1])));
 		}
 
 		/* SHOTS */
 		sprite_shot = new Sprite(assetManager.getImage("shot"));
 		shots = new ShotEntity[NUM_SHOTS];
 		for (int s = 0; s < NUM_SHOTS; s++) {
 			shots[s] = new ShotEntity(sprite_shot, -3000, -3000);
 		}
 		player.setShots(shots);
 
 		/* NH3 -SPIDERS */
 		// TODO: adjust to spawn relative to 30% of the screen
 		sprite_nh3 = new Sprite(assetManager.getImage("nh3"));
 		nh3s = new ArrayList<NH3Entity>();
 		nh3s.add(new NH3Entity(container, sprite_nh3, r.nextBoolean() ? 0
 				: windowManager.get_orig_width(), (int) (windowManager
 				.get_orig_height() - 200 + (r.nextDouble() * 200))));
 		lastNH3 = Sys.getTime();
 
 		/* CENTIPEDE */
 		sprite_centibody = new Sprite(assetManager.getImage("centibody"));
 		sprite_centihead = new Sprite(assetManager.getImage("centihead"));
 
 		centipedes = new ArrayList<Centipede>();
 		centipedes.add(new Centipede(container, sprite_centihead,
 				sprite_centibody, CENTIPEDE_SIZE));
 		lastCentipede = Sys.getTime();
 
		Music theme = new Music("data/theme.ogg");
		theme.loop();

 		container.getInput().addKeyListener(player);
 	}
 
 	@Override
 	public void update(GameContainer container, StateBasedGame game, int delta)
 			throws SlickException {
 
 		if (windowManager.isResized()) {
 
 		}
 
 		/* GAME_STATE */
 
 		switch (currentState) {
 		case START_GAME_STATE:
 
 			STATE_MSG = "PRESS     ENTER     TO     PLAY";
 			player.x = -2000;
 			player.y = -2000;
 			input = container.getInput();
 
 			if (input.isKeyPressed(Input.KEY_ENTER)) {
 				init(container, game);
 				// container.reinit();
				player.alive = true;
 				currentState = STATES.PLAY_GAME_STATE;
				//pause_counter = 3000;
 
 			}
 			break;
 
 		case PLAY_GAME_STATE:
 			STATE_MSG = null;
 			container.getInput().addKeyListener(player);
 
 			break;
 
 		case GAME_OVER_STATE:
 			container.getInput().removeKeyListener(player);
 			player.x = -2000;
 			player.y = -2000;
 
 			if (pause_counter == 3000) {
 				STATE_MSG = "GAME     OVER";
 			}
 
 			pause_counter -= delta;
 
 			if (pause_counter < 0 || input.isKeyPressed(Input.KEY_ENTER)) {
 				// game.enterState(SulfonicAvenger.MAINMENUSTATE);
 				currentState = STATES.START_GAME_STATE;
 			}
 
 			break;
 		}
 
 		if (input.isKeyPressed(Input.KEY_ESCAPE)) {
 			System.exit(0);
 		}
 		/* test collision detect */
 
 		/*
 		 * check against barries - i.e. molecules blocking our way, we undo move
 		 * here because the delta will change all the time depending on comp
 		 * speed
 		 */
 		player.move(delta, molecules);
 
 		/** spawn H3 */
 		if (Sys.getTime() - lastNH3 > NH3SpawnInterval) {
 			nh3s.add(new NH3Entity(container, sprite_nh3, r.nextBoolean() ? 0
 					: windowManager.get_orig_width(), (int) (windowManager
 					.get_orig_height() - 200 + (r.nextDouble() * 200))));
 
 			lastNH3 = Sys.getTime();
 		}
 
 		/* move NH3's */
 		for (Iterator<NH3Entity> i = nh3s.iterator(); i.hasNext();) {
 			NH3Entity n = i.next();
 
 			n.move(delta, molecules, centipedes);
 
 			if (n.collidesWith(player)) {
 				n.collidedWith(player);
 				player.collidedWith(n);
 
 				if (player.isGameOver()) {
 					currentState = STATES.GAME_OVER_STATE;
 				}
 
 			}
 
 			// remove
 			if (n.remove_me) {
 				i.remove();
 			}
 
 		}
 
 		// move centiballs & smooth
 		for (Centipede c : centipedes) {
 			c.move(delta, molecules);
 			if (c.checkCollisions(player)) {
 
 				if (player.isGameOver()) {
 					currentState = STATES.GAME_OVER_STATE;
 				}
 			}
 		}
 		/* cheap smoooth */
 		/* collision detection */
 		// move shots
 		for (ShotEntity s : shots) {
 
 			// shots and molecules
 			for (Iterator<MoleculeEntity> i = molecules.iterator(); i.hasNext();) {
 				MoleculeEntity m = i.next();
 				if (s.collidesWith(m)) {
 					s.collidedWith(m);
 					m.collidedWith(s);
 					assetManager.getSound("hit").play();
 				}
 				if (m.remove) {
 					updateScore(m.getScore());
 					i.remove();
 				}
 			}
 
 			// shots and NH3's
 			for (Iterator<NH3Entity> i = nh3s.iterator(); i.hasNext();) {
 				NH3Entity n = i.next();
 				if (n.display && s.collidesWith(n)) {
 					s.collidedWith(n);
 					n.collidedWith(s);
 					updateScore(n.getScore());
 					assetManager.getSound("hit").play();
 
 					i.remove();
 				}
 
 			}
 
 			// shots and centiballs - toggle display
 			for (Iterator<Centipede> i = centipedes.iterator(); i.hasNext();) {
 				Centipede c = i.next();
 
 				updateScore(c.checkCollisions(s, molecules, sprite_molecules));
 				// no centis, toggle spawn
 				if (!c.isAlive) {
 					i.remove();
 					newCentipede = true;
 				}
 
 			}
 
 			// Verify hit and spawn new Centipede - TODO: prolly refactor this
 			ArrayList<Centipede> new_centis = new ArrayList<Centipede>();
 
 			for (int i = 0; i < centipedes.size(); i++) {
 
 				Centipede c = centipedes.get(i);
 				List<CentiBallEntity> CentipedeBallList = ((List<CentiBallEntity>) c.centipede);
 
 				boolean has_new = false;
 				ArrayList<CentiBallEntity> temp = new ArrayList<CentiBallEntity>();
 
 				int q = 0;
 				for (int j = 0; has_new == false
 						&& j < CentipedeBallList.size(); j++) {
 
 					CentiBallEntity cbe = CentipedeBallList.get(j);
 
 					if (!cbe.isDisplay()) {
 
 						List<CentiBallEntity> l = CentipedeBallList.subList(
 								q + 1, CentipedeBallList.size());
 
 						temp = new ArrayList<CentiBallEntity>(l);
 
 						l.clear();
 
 						CentipedeBallList.remove(j);
 
 						has_new = true;
 					}
 					q++;
 
 				}
 
 				if (temp.size() > 0) {
 					new_centis.add(new Centipede(container, sprite_centibody,
 							sprite_centihead, temp));
 				}
 			}
 
 			// clean ball-less centipedes
 			for (Iterator<Centipede> c = centipedes.iterator(); c.hasNext();) {
 				Centipede cs = c.next();
 
 				if (cs.centipede.size() <= 0 || cs.isAlive == false) {
 					c.remove();
 				}
 			}
 			// add new centipedes
 			if (new_centis.size() > 0) {
 				centipedes.addAll(new_centis);
 			}
 
 			// update only displayable shots
 			if (s.isDisplay()) {
 				s.move(delta);
 			}
 
 		}
 		// end shots
 
 		// spawn centipede
		if ((centipedes.size() < MAX_CENTIPEDES && newCentipede) || (Sys.getTime() - lastCentipede > CentipedeInterval)) {
 			centipedes.add(new Centipede(container, sprite_centihead,
 					sprite_centibody, CENTIPEDE_SIZE));
 			newCentipede = false;
 			lastCentipede = Sys.getTime();
 		}
 
 	}
 
 	@Override
 	public void render(GameContainer container, StateBasedGame game, Graphics g)
 			throws SlickException {
 
 		for (Centipede c : centipedes) {
 			c.draw();
 		}
 
 		for (MoleculeEntity molecule : molecules) {
 			molecule.draw();
 			for (String coord : molecule.damages) {
 				sprite_damage.draw(Integer.parseInt(coord.split("-")[0]),
 						Integer.parseInt(coord.split("-")[1]));
 			}
 		}
 
 		for (NH3Entity n : nh3s) {
 			if (n.display) {
 				n.draw();
 			}
 		}
 
 		if (!player.isGameOver()) {
 			player.draw();
 		}
 
 		for (ShotEntity s : shots) {
 			if (s.isDisplay()) {
 				s.draw();
 			}
 		}
 
 		// render bottom for z-index
 		textDrawManager.draw("state", STATE_MSG, Color.white,
 				(container.getWidth() / 2), (int) windowManager.getCenterY(),
 				-0.5, 0, 0, 0);
 		textDrawManager.draw("title", TITLE_MSG, Color.white,
 				(container.getWidth() / 2), container.getHeight(), -0.5, -2, 0,
 				0);
 		textDrawManager.draw("score", SCORE_MSG + score, Color.red, 20,
 				container.getHeight(), 0, -2, 0, 0);
 		textDrawManager.draw("high_score", HIGH_SCORE_MSG + high_score,
 				Color.blue, container.getWidth(), container.getHeight(), -1,
 				-2, -20, 0);
 
 		textDrawManager.draw("score", "LIVES  " + player.lives, Color.red,
 				20 + textDrawManager.getWidth("score"), container.getHeight(),
 				0, -2, 40, 0);
 
 	}
 
 	@Override
 	public void enter(GameContainer container, StateBasedGame game)
 			throws SlickException {
 		super.enter(container, game);
 		currentState = STATES.START_GAME_STATE;
 		score = 0;
 
 	}
 
 	@Override
 	public int getID() {
 		return this.stateID;
 	}
 
 	@Override
 	public void keyPressed(int key, char c) {
 		super.keyPressed(key, c);
 	}
 
 	public void updateScore(int up) {
 
 		if (currentState == STATES.PLAY_GAME_STATE) {
 			score += up;
 
 			if (score - last_life_score >= 1000) {
 				player.lives += 1;
 				last_life_score += 1000;
 				// System.out.println("Lives:" + player.lives);
 			}
 
 		}
 		high_score = Math.max(score, high_score);
 	}
 }
