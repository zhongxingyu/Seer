 package com.tacoid.puyopuyo.logic;
 
 import java.util.ArrayList;
 
 import com.tacoid.puyopuyo.SoundPlayer;
 import com.tacoid.puyopuyo.SoundPlayer.SoundType;
 
 public class GameLogic {
 	
 	public enum MoveType {
 		LEFT,
 		RIGHT,
 		UP,
 		DOWN,
 		ROT_RIGHT,
 		ROT_LEFT
 	}
 
 	public final int LINES = 12;
 	public final int COLUMNS = 6;
 	public final int GARBAGE = 6;
 
 	private State state = State.MOVE;
 
 	int grid[][] = new int[LINES * 2][COLUMNS];
 	private Coord[] piece = new Coord[2];
 	private Coord[] nextPiece = new Coord[2];
 	private int rot = 0;
 	private int nextRot = 0;
 
 	public boolean gridFF[][];
 	private float sum = 0f;
 	private int score = 0;
 	private ArrayList<Falling> fallings;
 	private float speed = 0.4f;
 
 	private boolean first = true;
 	private ArrayList<Explosion> removes;
 	private int combo = 1;
 	private boolean isDown = false;
 	private GameLogic opponent = null;
 	private int points = 0;
 	private int garbage = 0;
 	private float leftoverNuisance = 0f;
 	
 	protected boolean paused = false;
 
 	private boolean isIA;
 	private int n_colors = 4;
 	private boolean cheatMode = false;
	private float initialSpeed = 0.4f;
 
 	public GameLogic() {
 		init();
 	}
 	
 	public void init() {
 		fallings = null;
 		score = 0;
 		sum = 0;
 		rot = 0;
 		nextRot = 0;
		speed = initialSpeed;
 		state = State.MOVE;
 		points = 0;
 		garbage = 0;
 		leftoverNuisance = 0f;
 	
 		for (int l = 0; l < LINES * 2; l++) {
 			for (int c = 0; c < COLUMNS; c++) {
 				grid[l][c] = 0;
 			}
 		}
 		generate(); // generate current
 		generate(); // generate next
 	}
 
 	public GameLogic(GameLogic logic, boolean isIA) {
 		// copy grid.
 		for (int l = 0; l < LINES; l++) {
 			for (int c = 0; c < COLUMNS; c++) {
 				grid[l][c] = logic.grid[l][c];
 			}
 		}
 		
 		this.isIA = isIA;
 
 		// piece + next piece :
 		piece[0] = new Coord(logic.piece[0].l, logic.piece[0].c,
 				logic.piece[0].coul);
 		piece[1] = new Coord(logic.piece[1].l, logic.piece[1].c,
 				logic.piece[1].coul);
 		nextPiece[0] = new Coord(logic.nextPiece[0].l, logic.nextPiece[0].c,
 				logic.nextPiece[0].coul);
 		nextPiece[1] = new Coord(logic.nextPiece[1].l, logic.nextPiece[1].c,
 				logic.nextPiece[1].coul);
 		rot = logic.rot;
 		nextRot = logic.nextRot;
 
 		state = logic.state;
 		combo = 1;
 	}
 
 	private int cheatColor() {
 		int[] counts = new int[GARBAGE];
 		for (int c = 0; c < COLUMNS; c++) {
 			int l = LINES;
 			while (l >= 0 && grid[l][c] == 0) l--;
 			if (l >= 0 && grid[l][c] > 0 && grid[l][c] != GARBAGE) {
 				counts[grid[l][c]]++;
 			}
 		}
 		
 		int max = 1;
 		for (int i = 1; i < GARBAGE; i++) {
 			if (counts[max] < counts[i]) {
 				max = i;
 			}
 		}
 		return max;
 	}
 	
 	private boolean generate() {
 		// Sens de la piece :
 		// [x] [x][ ] [ ] [ ][x]
 		// [ ] [x]
 		piece[0] = nextPiece[0];
 		piece[1] = nextPiece[1];
 		rot = nextRot;
 
 		int coul1 = 1 + (int) (Math.random() * n_colors);
 		int coul2;
 		
 		if (cheatMode) {
 			if (Math.random() < 0.5) {
 				coul2 = coul1;
 			} else {
 				coul2 = cheatColor();
 			}
 		} else {
 			coul2 = 1 + (int) (Math.random() * n_colors);
 		}
 		nextRot = (int) (Math.random() * 4);
 
 		switch (nextRot) {
 		case 0:
 			nextPiece[0] = new Coord(LINES - 1, COLUMNS / 2, coul1);
 			nextPiece[1] = new Coord(LINES, COLUMNS / 2, coul2);
 			break;
 		case 1:
 			nextPiece[0] = new Coord(LINES, COLUMNS / 2, coul1);
 			nextPiece[1] = new Coord(LINES, COLUMNS / 2 + 1, coul2);
 			break;
 		case 2:
 			nextPiece[0] = new Coord(LINES, COLUMNS / 2, coul1);
 			nextPiece[1] = new Coord(LINES - 1, COLUMNS / 2, coul2);
 			break;
 		case 3:
 			nextPiece[0] = new Coord(LINES, COLUMNS / 2 + 1, coul1);
 			nextPiece[1] = new Coord(LINES, COLUMNS / 2, coul2);
 			break;
 		}
 
 		return (piece[0] == null) || (grid[piece[0].l][piece[0].c] == 0 && grid[piece[1].l][piece[1].c] == 0);
 	}
 
 	public void rotateRight() {
 		if (state == State.MOVE) {
 			if(!isIA) {
 				SoundPlayer.getInstance().playSound(SoundType.ROTATE, 0.8f, true);
 			}
 			Coord newPiece[] = new Coord[2];
 			newPiece[0] = new Coord(piece[0].l, piece[0].c, piece[0].coul);
 			newPiece[1] = new Coord(piece[1].l, piece[1].c, piece[1].coul);
 
 			int decL = 0;
 			int decC = 0;
 			switch (rot) {
 			case 0:
 				decL = -1;
 				decC = 1;
 				break;
 			case 1:
 				decL = -1;
 				decC = -1;
 				break;
 			case 2:
 				decL = 1;
 				decC = -1;
 				break;
 			case 3:
 				decL = 1;
 				decC = 1;
 				break;
 			}
 			newPiece[1].c += decC;
 			newPiece[1].l += decL;
 
 			if (newPiece[1].c < 0) {
 				newPiece[1].c++;
 				newPiece[0].c++;
 			}
 
 			if (newPiece[1].c >= COLUMNS) {
 				newPiece[1].c--;
 				newPiece[0].c--;
 			}
 
 			if (newPiece[1].l < 0) {
 				newPiece[1].l++;
 				newPiece[0].l++;
 			}
 
 			if (grid[newPiece[0].l][newPiece[0].c] == 0
 					&& grid[newPiece[1].l][newPiece[1].c] == 0) {
 				piece[0] = newPiece[0];
 				piece[1] = newPiece[1];
 				rot = (rot + 1) % 4;
 			}
 		}
 	}
 
 	public void rotateLeft() {
 		if (state == State.MOVE) {
 			if(!isIA) {
 				SoundPlayer.getInstance().playSound(SoundType.ROTATE, 0.8f, true);
 			}
 			Coord newPiece[] = new Coord[2];
 			newPiece[0] = new Coord(piece[0].l, piece[0].c, piece[0].coul);
 			newPiece[1] = new Coord(piece[1].l, piece[1].c, piece[1].coul);
 
 			int decL = 0;
 			int decC = 0;
 			switch (rot) {
 			case 0:
 				decL = -1;
 				decC = -1;
 				break;
 			case 1:
 				decL = 1;
 				decC = -1;
 				break;
 			case 2:
 				decL = 1;
 				decC = 1;
 				break;
 			case 3:
 				decL = -1;
 				decC = 1;
 				break;
 			}
 			newPiece[1].c += decC;
 			newPiece[1].l += decL;
 
 			if (newPiece[1].c < 0) {
 				newPiece[1].c++;
 				newPiece[0].c++;
 			}
 
 			if (newPiece[1].c >= COLUMNS) {
 				newPiece[1].c--;
 				newPiece[0].c--;
 			}
 
 			if (newPiece[1].l < 0) {
 				newPiece[1].l++;
 				newPiece[0].l++;
 			}
 
 			if (grid[newPiece[0].l][newPiece[0].c] == 0
 					&& grid[newPiece[1].l][newPiece[1].c] == 0) {
 				piece[0] = newPiece[0];
 				piece[1] = newPiece[1];
 				rot = (rot + 3) % 4;
 			}
 		}
 	}
 
 	public void moveDown() {
 		if (state == State.MOVE) {
 			descendre();
 			if(!isIA) {
 				SoundPlayer.getInstance().playSound(SoundType.MOVE, 0.5f, true);
 			}
 		}
 	}
 
 	public void moveLeft() {
 		if (state == State.MOVE) {
 			boolean ok = true;
 			for (int i = 0; i < 2; i++) {
 				if (piece[i].c <= 0 || grid[piece[i].l][piece[i].c - 1] > 0) {
 					ok = false;
 				}
 			}
 			if (ok) {
 				if(!isIA) {
 					SoundPlayer.getInstance().playSound(SoundType.MOVE, 0.5f, true);
 				}
 				for (int i = 0; i < 2; i++) {
 					piece[i].c--;
 				}
 			}
 		}
 	}
 
 	public void moveRight() {
 		if (state == State.MOVE) {
 			boolean ok = true;
 			for (int i = 0; i < 2; i++) {
 				if (piece[i].c >= COLUMNS - 1
 						|| grid[piece[i].l][piece[i].c + 1] > 0) {
 					ok = false;
 				}
 			}
 			if (ok) {
 				if(!isIA) {
 					SoundPlayer.getInstance().playSound(SoundType.MOVE, 0.5f, true);
 				}
 				for (int i = 0; i < 2; i++) {
 					piece[i].c++;
 				}
 			}
 		}
 	}
 	
 	public void dropPiece() {
 		if (state == State.MOVE) {
 			state = State.POSE;
 		}
 	}
 
 	private boolean descendre() {
 		boolean result = true;
 		for (Coord p : piece) {
 			if (p.l <= 0 || grid[p.l - 1][p.c] > 0) {
 				result = false;
 			}
 		}
 		if (result) {
 			for (Coord p : piece) {
 				p.l--;
 			}
 		}
 		return result;
 	}
 
 	private ArrayList<Falling> gravity() {
 		ArrayList<Falling> boules = new ArrayList<Falling>();
 		int[] sums = new int[COLUMNS];
 		for (int l = 0; l < LINES * 2; l++) {
 			for (int c = 0; c < COLUMNS; c++) {
 				if (grid[l][c] == 0) {
 					sums[c]++;
 				} else if (sums[c] > 0) {
 					boules.add(new Falling(new Coord(l, c, grid[l][c]), sums[c]));
 					grid[l][c] = 0;
 				}
 			}
 		}
 		return boules;
 	}
 
 	public int floodfill(int l, int c, int coul, ArrayList<Coord> list) {
 		if (l < 0 || c < 0 || l >= LINES || c >= COLUMNS || grid[l][c] != coul
 				|| gridFF[l][c]) {
 			return 0;
 		}
 		gridFF[l][c] = true;
 		if (list != null) {
 			list.add(new Coord(l, c, coul));
 		}
 		return 1 + floodfill(l + 1, c, coul, list)
 				+ floodfill(l - 1, c, coul, list)
 				+ floodfill(l, c + 1, coul, list)
 				+ floodfill(l, c - 1, coul, list);
 	}
 
 	public ArrayList<Explosion> resolve() {
 		ArrayList<Explosion> remove = new ArrayList<Explosion>();
 		gridFF = new boolean[LINES][COLUMNS];
 		for (int l = 0; l < LINES; l++) {
 			for (int c = 0; c < COLUMNS; c++) {
 				if (grid[l][c] > 0 && grid[l][c] != GARBAGE) {
 					ArrayList<Coord> list = new ArrayList<Coord>();
 					if (floodfill(l, c, grid[l][c], list) >= 4) {
 						remove.add(new Explosion(this, list));
 					}
 				}
 			}
 		}
 		if(!isIA && !remove.isEmpty()) {
 			SoundPlayer.getInstance().playSound(SoundType.EXPLODE, 1.0f, true);
 		}
 		return remove;
 	}
 
 	private void pose() {
 		for (Coord p : piece) {
 			grid[p.l][p.c] = p.coul;
 			p.coul = 0;
 		}
 	}
 
 	public void poseEtGravity() {
 		for (Coord c : piece) {
 			while (c.l > 0 && grid[c.l - 1][c.c] == 0) {
 				c.l--;
 			}
 			grid[c.l][c.c] = c.coul;
 		}
 	}
 
 	public int descendreEtPose() {
 		pose();
 		points = 0;
 		combo = 1;
 		do {
 			fallings = gravity();
 			for (Falling f : fallings) {
 				grid[f.getEnd().l][f.getEnd().c] = f.getEnd().coul;
 			}
 
 			removes = resolve();
 			for (Explosion r : removes) {
 				points += r.getNbPuyos() * 10 * (r.getNbPuyos() - 3) * combo;
 			}
 			combo *= 2;
 		} while (removes.size() > 0);
 		return points;
 	}
 
 	public void update(float delta) {
 		if(!paused) {
 			sum += delta;
 			switch (state) {
 			case GARBAGE:
 				if (garbage > 0) {
 					generateGarbage();
 					state = State.GRAVITY;
 				} else {
 					state = State.MOVE;
 				}
 				first = true;
 				break;
 			case MOVE:
 				if (first) {
 					if (!generate()) {
 						state = State.LOST;
 					}
 					first = false;
 				}
 				if (sum > speed || (isDown && sum > 0.1f)) {
 					if (!descendre()) {
 						state = State.POSE;
 					}
 					sum = 0f;
 				}
 				break;
 			case POSE:
 				if (sum > speed) {
 					pose();
 					state = State.GRAVITY;
 					sum = 0f;
 					first = true;
 				}
 				break;
 			case GRAVITY:
 				if (first) {
 					fallings = gravity();
 					first = false;
 				}
 				for (Falling f : fallings) {
 					f.update(delta / speed);
 				}
 				if (sum > 0.5) {
 					for (Falling f : fallings) {
 						grid[f.getEnd().l][f.getEnd().c] = f.getEnd().coul;
 					}
 					fallings = null;
 					first = true;
 					state = State.RESOLVE;
 					sum = 0f;
 				}
 				break;
 			case RESOLVE:
 				if (first) {
 					removes = resolve();
 					for (Explosion r : removes) {
 						int p = r.getNbPuyos() * 10 * (r.getNbPuyos() - 3) * combo;
 						if (opponent != null) {
 							float nuisance = p / 70.0f + leftoverNuisance;
 							leftoverNuisance = nuisance - (int) nuisance;
 							opponent.sendGarbage((int) nuisance);
 						}
 						points += p;
 					}
 					first = false;
 				}
 				if (sum > 0.5) {
 					if (removes.size() > 0) {
 						state = State.GRAVITY;
 						first = true;
 						combo *= 2;
 					} else {
 						state = State.GARBAGE;
 						score += points;
 						points = 0;
 						combo = 1;
 					}
 					sum = 0f;
 				}
 				break;
 			}
 		}
 	}
 
 	private void generateGarbage() {
 		int l = LINES;
 		while (garbage >= COLUMNS) {
 			for (int c = 0; c < COLUMNS; c++) {
 				grid[l][c] = GARBAGE;
 			}
 			garbage -= COLUMNS;
 			l++;
 		}
 
 		ArrayList<Integer> cols = new ArrayList<Integer>();
 		for (int c = 0; c < COLUMNS; c++) {
 			cols.add(c);
 		}
 		
 		for (int c = 0; c < garbage; c++) {
 			int i = (int)(Math.random() * cols.size());
 			grid[l][cols.get(i)] = GARBAGE;
 			cols.remove(i);
 		}
 		garbage = 0;
 	}
 
 	public int[][] getGrid() {
 		return grid;
 	}
 
 	public Coord[] getPiece() {
 		return piece;
 	}
 
 	public Coord[] getNextPiece() {
 		return nextPiece;
 	}
 
 	public ArrayList<Falling> getFallings() {
 		return fallings;
 	}
 
 	public State getState() {
 		return state;
 	}
 
 	public int getScore() {
 		return score;
 	}
 
 	public void down() {
 		isDown = true;
 	}
 
 	public void up() {
 		isDown = false;
 	}
 
 	public void setOpponent(GameLogic opponent) {
 		this.opponent = opponent;
 	}
 
 	public void sendGarbage(int garbage) {
 		this.garbage += garbage;
 		if (this.garbage > LINES * COLUMNS) {
 			garbage = LINES * COLUMNS;
 		}
 	}
 	
 	public void pause() {
 		paused = true;
 	}
 	
 	public void resume() {
 		paused = false;
 	}
 	
 	public boolean isPaused() {
 		return paused;
 	}
 	
	public void setInitialSpeed(float speed) {
		this.initialSpeed = speed;
		this.speed = speed;
	}
	
 	public void setSpeed(float speed) {
 		this.speed = speed;
 	}
 	
 	public void setNColors(int n) {
 		this.n_colors = n;
 	}
 
 	public void setCheatMode(boolean cheatMode) {
 		this.cheatMode = cheatMode;
 	}
 }
