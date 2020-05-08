 package com.salvadorjacobi.tetris;
 
 import java.awt.Point;
 import java.util.Collections;
 import java.util.Observable;
 import java.util.Stack;
 
 public class GameModel extends Observable {
 	public static final int TICK_INTERVAL = 1000 / 60;
 	public static final int LOCK_DELAY = 30;
 	public static final int SOFTDROP_SPEEDUP = 20;
 
 	public static final int SKY_HEIGHT = 2;
 	public static final int SOFTDROP_MULTIPLIER = 1;
 	public static final int HARDDROP_MULTIPLIER = 2;
 
 	public final int width;
 	public final int height;
 	public final int scale;
 
 	private Block[][] matrix;
 	private int score;
 	private int actionPoints;
 	private Tetrimino fallingTetrimino;
 	private Tetrimino heldTetrimino;
 	private boolean swapped;
 	private boolean gameOver;
 	private boolean paused;
 	private final Stack<Tetrimino> bag = new Stack<Tetrimino>();
 	private int comboCounter;
 	private boolean difficultClear;
 	private boolean previousDifficultClear;
 	private int linesCleared;
 	private int linesClearedPerLevel;
 	private int level;
 	private boolean tSpinKick;
 	private boolean lastMoveRotation;
 	private int hangTime;
 	private int surfaceTime;
 	private boolean softDrop;
 
 	public GameModel(int width, int height, int scale) {
 		this.width = width;
 		this.height = height + SKY_HEIGHT;
 		this.scale = scale;
 
 		reset();
 	}
 
 	public Block getBlock(int x, int y) {
 		return matrix[x][y];
 	}
 
 	public int getScore() {
 		return score;
 	}
 
 	public Tetrimino getFallingTetrimino() {
 		return fallingTetrimino;
 	}
 
 	public Tetrimino getNextTetrimino() {
 		return bag.peek();
 	}
 
 	public Tetrimino getHeldTetrimino() {
 		return heldTetrimino;
 	}
 
 	public int getLevel() {
 		return level;
 	}
 
 	public int getFallDelay() {
 		return (int) (60 * Math.exp(-0.2 * (level - 1)));
 	}
 
 	public void tick() {
 		if (gameOver || paused) return;
 
 		hangTime += (softDrop ? SOFTDROP_SPEEDUP : 1);
 
 		Point down = new Point(0, 1);
 		Point originalPosition = fallingTetrimino.getPosition();
 
 		boolean shouldFall = false;
 
 		if (hangTime >= getFallDelay()) {
 			hangTime = 0;
 			shouldFall = true;
 		}
 
 		if (translate(down)) {
 			if (shouldFall) {
				if (softDrop) {
					score += SOFTDROP_MULTIPLIER;
				}

 				setChanged();
 			} else {
 				fallingTetrimino.setPosition(originalPosition);
 			}
 		} else {
 			surfaceTime++;
 
 			if (surfaceTime >= LOCK_DELAY) {
 				surfaceTime = 0;
 
 				next();
 
 				setChanged();
 			}
 		}
 	}
 
 	public void populateBag() {
 		while (!bag.empty()) {
 			bag.pop();
 		}
 
 		for (Tetrimino.Shape shape : Tetrimino.Shape.VALUES) {
 			bag.push(new Tetrimino(shape, new Point(0,0), 0));
 		}
 
 		Collections.shuffle(bag);
 	}
 
 	public void next() {
 		if (gameOver) return;
 
 		if (fallingTetrimino != null) {
 			embed(fallingTetrimino);
 
 			boolean threeCorners = (fallingTetrimino.getShape() == Tetrimino.Shape.T && cornerSum(fallingTetrimino) >= 3);
 
 			if (clear()) {
 				comboCounter++;
 				score += (comboCounter - 1) * 50 * level;
 
 				// Back-to-back difficult clear
 				if (previousDifficultClear && difficultClear) {
 					score += actionPoints / 2 * level;
 				}
 
 				previousDifficultClear = difficultClear;
 				difficultClear = false;
 
 				Constants.sounds.get("clear").play();
 			} else {
 				comboCounter = 0;
 			}
 
 			if (threeCorners && lastMoveRotation) {
 				switch (linesCleared) {
 					case 0:
 						score += (tSpinKick ? 100 : 0) * level;
 						break;
 					case 1:
 						score += (tSpinKick ? 200 : 100) * level;
 						break;
 					case 2:
 						score += (tSpinKick ? 1200 : 300) * level;
 						break;
 					case 3:
 						score += (tSpinKick ? 1600 : 500) * level;
 						break;
 				}
 			}
 		}
 
 		spawn(bag.pop());
 
 		swapped = false;
 		tSpinKick = false;
 
 		if (bag.empty()) {
 			populateBag();
 		}
 
 		setChanged();
 	}
 
 	public boolean translate(Point delta) {
 		Point originalPosition = fallingTetrimino.getPosition();
 
 		fallingTetrimino.translate(delta);
 
 		if (isOutOfBounds(fallingTetrimino, false) || isOverlapping(fallingTetrimino)) {
 			fallingTetrimino.setPosition(originalPosition);
 			return false;
 		}
 
 		setChanged();
 
 		return true;
 	}
 
 	public boolean rotate(boolean direction) {
 		Tetrimino.Shape shape = fallingTetrimino.getShape();
 		int rotation = fallingTetrimino.getRotation();
 		Point originalPosition = fallingTetrimino.getPosition();
 		int nextRotation;
 		int[][] kickTranslations;
 
 		fallingTetrimino.rotate(direction);
 
 		nextRotation = fallingTetrimino.getRotation();
 
 		int[][][] offsetData = Constants.offsetData.get(shape);
 		int offsetCount = offsetData[rotation].length;
 
 		kickTranslations = new int[offsetCount][];
 
 		for (int i = 0; i < offsetCount; i++) {
 			int[] offset = offsetData[rotation][i];
 			int[] nextOffset = offsetData[nextRotation][i];
 
 			kickTranslations[i] = new int[] {offset[0] - nextOffset[0], offset[1] - nextOffset[1]};
 		}
 
 		for (int i = 0; i < kickTranslations.length; i++) {
 			int dx = kickTranslations[i][0];
 			int dy = kickTranslations[i][1];
 
 			fallingTetrimino.translate(new Point(dx, dy));
 
 			if (isOutOfBounds(fallingTetrimino, false) || isOverlapping(fallingTetrimino)) {
 				fallingTetrimino.setPosition(originalPosition);
 
 				continue;
 			}
 
 			// Detect kick
 			if (shape == Tetrimino.Shape.T) {
 				tSpinKick = (i != 0);
 			}
 
 			surfaceTime = 0;
 			lastMoveRotation = true;
 
 			setChanged();
 
 			return true;
 		}
 
 		fallingTetrimino.rotate(!direction);
 
 		return false;
 	}
 
 	public void softDrop(boolean enable) {
 		softDrop = enable;
 	}
 
 	public void hardDrop() {
 		int lines = drop(fallingTetrimino);
 
 		score += lines * HARDDROP_MULTIPLIER;
 
 		if (lines > 0) {
 			lastMoveRotation = false;
 		}
 
 		next();
 	}
 
 	public boolean shift(boolean left) {
 		Point offset = new Point((left ? -1 : 1), 0);
 
 		surfaceTime = 0;
 		lastMoveRotation = false;
 
 		return translate(offset);
 	}
 
 	public void swap() {
 		if (swapped) return;
 
 		if (heldTetrimino != null) {
 			Tetrimino temporary = heldTetrimino;
 			heldTetrimino = fallingTetrimino;
 
 			spawn(temporary);
 		} else {
 			heldTetrimino = fallingTetrimino;
 
 			spawn(bag.pop());
 		}
 
 		swapped = true;
 
 		if (bag.empty()) {
 			populateBag();
 		}
 
 		setChanged();
 	}
 
 	public boolean clear() {
 		int lines = 0;
 
 		for (int j = 0; j < height; j++) {
 			boolean cleared = true;
 
 			for (int i = 0; i < width; i++) {
 				if (getBlock(i, j) == null) {
 					cleared = false;
 					break;
 				}
 			}
 
 			if (!cleared) {
 				continue;
 			}
 
 			lines++;
 
 			// Move blocks down
 			for (int k = j; k > 0; k--) {
 				for (int i = 0; i < width; i++) {
 					matrix[i][k] = matrix[i][k - 1];
 				}
 			}
 
 			// Clear top row
 			for (int i = 0; i < width; i++) {
 				matrix[i][0] = null;
 			}
 		}
 
 		linesCleared = lines;
 		linesClearedPerLevel += lines;
 
 		switch (lines) {
 			case 0:
 				return false;
 			case 1:
 				actionPoints = 100 * level;
 				break;
 			case 2:
 				actionPoints = 300 * level;
 				break;
 			case 3:
 				actionPoints = 500 * level;
 				break;
 			case 4:
 				actionPoints = 800 * level;
 				difficultClear = true;
 				break;
 		}
 
 		score += actionPoints;
 
 		int threshold = levelThreshold();
 
 		while (linesClearedPerLevel >= threshold) {
 			level++;
 			linesClearedPerLevel -= threshold;
 		}
 
 		setChanged();
 		notifyObservers();
 
 		return true;
 	}
 
 	public int levelThreshold() {
 		return (int) (10 + 5 * Math.log(level));
 	}
 
 	public void spawn(Tetrimino tetrimino) {
 		Point startPosition = new Point(width / 2 - 1, 1);
 
 		tetrimino.setPosition(startPosition);
 		tetrimino.setRotation(0);
 
 		fallingTetrimino = tetrimino;
 
 		if (isOverlapping(fallingTetrimino)) {
 			gameOver = true;
 		}
 	}
 
 	public int drop(Tetrimino tetrimino) {
 		int lines = 0;
 
 		Point down = new Point(0, 1);
 		Point up = new Point(0, -1);
 
 		while (!isOutOfBounds(tetrimino, false) && !isOverlapping(tetrimino)) {
 			tetrimino.translate(down);
 
 			lines++;
 		}
 
 		tetrimino.translate(up);
 
 		return lines - 1;
 	}
 
 	public void embed(Tetrimino tetrimino) {
 		Tetrimino.Shape shape = tetrimino.getShape();
 		Point position = tetrimino.getPosition();
 		int rotation = tetrimino.getRotation();
 		int[][] pattern = Constants.trueRotation.get(shape)[rotation];
 
 		int size = pattern.length;
 		int radius = (size - 1) / 2;
 
 		boolean potentialGameOver = true;
 
 		for (int i = 0; i < size; i++) {
 			for (int j = 0; j < size; j++) {
 				if (pattern[j][i] == 1) {
 					int x = position.x - radius + i;
 					int y = position.y - radius + j;
 
 					matrix[x][y] = new Block(shape);
 
 					if (y >= GameModel.SKY_HEIGHT) {
 						potentialGameOver = false;
 					}
 				}
 			}
 		}
 
 		if (potentialGameOver) {
 			gameOver = true;
 		}
 	}
 
 	public int cornerSum(Tetrimino tetrimino) {
 		int sum = 0;
 		Point position = tetrimino.getPosition();
 
 		for (int i = -1; i <= 1; i += 2) {
 			for (int j = -1; j <= 1; j += 2) {
 				int x = position.x + i;
 				int y = position.y + j;
 
 				if (x < 0 || x >= width || y < 0 || y >= height) {
 					sum++;
 				} else if (getBlock(x, y) != null) {
 					sum++;
 				}
 			}
 		}
 
 		return sum;
 	}
 
 	public boolean isOverlapping(Tetrimino tetrimino) {
 		Tetrimino.Shape shape = tetrimino.getShape();
 		Point position = tetrimino.getPosition();
 		int rotation = tetrimino.getRotation();
 		int[][] pattern = Constants.trueRotation.get(shape)[rotation];
 
 		int size = pattern.length;
 		int radius = (size - 1) / 2;
 
 		for (int i = 0; i < size; i++) {
 			for (int j = 0; j < size; j++) {
 				if (pattern[j][i] == 1) {
 					int x = position.x - radius + i;
 					int y = position.y - radius + j;
 
 					if (matrix[x][y] != null)
 						return true;
 				}
 			}
 		}
 
 		return false;
 	}
 
 	public boolean isOutOfBounds(Tetrimino tetrimino, boolean cutAtSkyline) {
 		Tetrimino.Shape shape = tetrimino.getShape();
 		Point position = tetrimino.getPosition();
 		int rotation = tetrimino.getRotation();
 		int[][] pattern = Constants.trueRotation.get(shape)[rotation];
 
 		int size = pattern.length;
 		int radius = (size - 1) / 2;
 
 		for (int i = 0; i < size; i++) {
 			for (int j = 0; j < size; j++) {
 				if (pattern[j][i] == 1) {
 					int x = i + position.x - radius;
 					int y = j + position.y - radius;
 
 					if (pattern[j][i] == 1) {
 						if (x < 0 || x >= width || y < (cutAtSkyline ? SKY_HEIGHT : 0) || y >= height)
 							return true;
 					}
 				}
 			}
 		}
 
 		return false;
 	}
 
 	public boolean isGameOver() {
 		return gameOver;
 	}
 
 	public boolean isPaused() {
 		return paused;
 	}
 
 	public void pause() {
 		paused = true;
 	}
 
 	public void resume() {
 		paused = false;
 	}
 
 	public void reset() {
 		matrix = new Block[width][height];
 		score = 0;
 		heldTetrimino = null;
 		fallingTetrimino = null;
 		swapped = false;
 		gameOver = false;
 		comboCounter = 0;
 		difficultClear = false;
 		previousDifficultClear = false;
 		linesClearedPerLevel = 0;
 		level = 1;
 		tSpinKick = false;
 		lastMoveRotation = false;
 
 		populateBag();
 		next();
 
 		setChanged();
 	}
 }
