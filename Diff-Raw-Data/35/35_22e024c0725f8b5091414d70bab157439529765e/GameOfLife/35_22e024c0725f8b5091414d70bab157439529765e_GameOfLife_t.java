 package com.mangst.gameoflife;
 
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 
 /**
  * A concurrent implementation of The Game of Life.
  * @author mangst
  */
 public class GameOfLife {
 	/**
 	 * Runs the game from the command line.
 	 * @param args the command line arguments
 	 */
 	public static void main(String args[]) {
 		List<String> argErrors = new ArrayList<String>();
 		Arguments arguments = new Arguments(args);
 
 		//display help message
 		if (arguments.exists("h", "help")) {
 			System.out.println("The Game of Life");
 			System.out.println("by Michael Angstadt");
 			System.out.println();
 			System.out.println("usage:    java -jar GameOfLife.jar <arguments>");
 			System.out.println("example:  java -jar GameOfLife.jar --rows=30 --cols=30");
 			System.out.println();
 			System.out.println("Arguments");
 			System.out.println("-r=N, --rows=N (required)");
 			System.out.println("   The number of rows in the grid.");
 			System.out.println("-c=N, --cols=N (required)");
 			System.out.println("   The number of columns in the grid.");
 			System.out.println("-t=N, --threads=N");
 			System.out.println("   The number of threads the game will use.");
 			System.out.println("   (defaults to the computer's number of cores)");
 			System.out.println("-n=N, --noise=N");
 			System.out.println("   Chooses N cells at random each iteration and toggles their states.");
 			System.out.println("   (defaults to 0)");
 			System.out.println("-s=N, --sleep=N");
 			System.out.println("   The number of milliseconds the program will pause for before iterating to");
 			System.out.println("   the next grid state.");
 			System.out.println("   (defaults to 100)");
 			System.out.println("-i=N, --iterations=N");
 			System.out.println("   The number of iterations to perform.");
 			System.out.println("   (defaults to infinite--the game will never end)");
 			System.out.println("-u, --suppressOutput");
 			System.out.println("   Use this flag to stop the board from being displayed every iteration.");
 			System.out.println("-a=N, --startAlive=N");
 			System.out.println("   The percent chance each cell has of starting in the \"alive\" state.");
 			System.out.println("   (defaults to 0.25, unless -g is specified, in which case it is ignored)");
 			System.out.println("-g=FILE, --grid=FILE");
 			System.out.println("   Specify the starting grid state in a file. Each character in the text file");
 			System.out.println("   represents a cell. Alive cells are 'x', dead cells can be anything else.");
 			System.out.println("   If the specified grid size (-r, -c) is greater than the grid size in the");
 			System.out.println("   file, it will populate the upper-left corner of the grid. If it's less than");
 			System.out.println("   the grid size in the file, it will populate as much as it can.");
 			System.out.println("   Example:");
 			System.out.println("   x..x.");
 			System.out.println("   .xx..");
 			System.out.println("   ..x.x");
 			System.out.println("-h, --help");
 			System.out.println("   Displays this help message.");
 			System.exit(0);
 		}
 
 		//get the number of rows in the grid
 		Integer rows = arguments.valueInt("r", "rows");
 		if (rows == null) {
 			argErrors.add("Number of rows is required (example: \"--rows=50\").");
 		}
 
 		//get the number of columns in the grid
 		Integer cols = arguments.valueInt("c", "cols");
 		if (cols == null) {
 			argErrors.add("Number of columns is required (example: \"--cols=50\").");
 		}

		if (!argErrors.isEmpty()) {
			for (String error : argErrors) {
 				System.err.println(error);
 			}
 			System.exit(1);
 		}
 
 		//get number of threads to spawn
 		Integer threads = arguments.valueInt("t", "threads", Runtime.getRuntime().availableProcessors());
 
 		//get the amount of noise
 		Integer noise = arguments.valueInt("n", "noise", 0);
 
 		//get the number of ms to sleep between iterations
 		Integer sleep = arguments.valueInt("s", "sleep", 100);
 
 		//get the number max iterations
 		Integer iterations = arguments.valueInt("i", "iterations");
 
 		//do not display the board state
 		boolean suppressOutput = arguments.exists("u", "suppressOutput");
 
 		//construct GameOfLife object
 		GameOfLife gameOfLife;
 		String gridFile = arguments.value("g", "grid");
 		if (gridFile != null) {
 			//use the user's specified starting state
 
 			Grid starting = new Grid(rows, cols);
 			Scanner in = null;
 			try {
 				in = new Scanner(new FileReader(gridFile));
 				for (int i = 0; i < rows && in.hasNextLine(); i++) {
 					String line = in.nextLine();
 					for (int j = 0; j < cols && j < line.length(); j++) {
 						boolean alive = line.charAt(j) == 'x';
 						starting.setAlive(i, j, alive);
 					}
 				}
 			} catch (IOException e) {
 				System.err.println("Problem reading the grid input file \"" + gridFile + "\": " + e.getMessage());
 				System.exit(1);
 			} finally {
 				in.close();
 			}
 			gameOfLife = new GameOfLife(starting);
 		} else {
 			//populate all cells randomly
 
 			//get the chance that a cell starts in the alive state
 			Double startAlive = arguments.valueDouble("a", "startAlive", 0.25);
 
 			gameOfLife = new GameOfLife(rows, cols, startAlive);
 		}
 		gameOfLife.setThreads(threads);
 		gameOfLife.setNoise(noise);
 
 		//start the game
 		long start = System.currentTimeMillis();
 		if (!suppressOutput) System.out.println(rows + " " + cols);
 		while (iterations == null || gameOfLife.getIterationCount() < iterations) {
 			//output board
 			if (!suppressOutput) System.out.println(gameOfLife.getGrid());
 
 			//iterate game state
 			gameOfLife.iterate();
 
 			//sleep
 			try {
 				Thread.sleep(sleep);
 			} catch (InterruptedException e) {
 			}
 		}
 		long time = System.currentTimeMillis() - start;
 		System.out.println(time + "ms");
 	}
 
 	/**
 	 * The number of iterations the game has run for.
 	 */
 	private long iterationCount = 0;
 
 	/**
 	 * The number of threads to use.
 	 */
 	private int threads = Runtime.getRuntime().availableProcessors();
 
 	/**
 	 * The threads array.
 	 */
 	private LifeThread[] threadList = new LifeThread[threads];
 
 	/**
 	 * The number of random cells to toggle every iteration.
 	 */
 	private int noise;
 
 	/**
 	 * The current grid state.
 	 */
 	private Grid current;
 
 	/**
 	 * The next grid state.
 	 */
 	private Grid next;
 
 	/**
 	 * Constructs a new Game of Life.
 	 * @param start the starting grid state
 	 */
 	public GameOfLife(Grid start) {
 		current = start;
 		next = new Grid(start.getRows(), start.getCols());
 	}
 
 	/**
 	 * Constructs a new Game of Life.
 	 * @param rows the number of rows in the grid
 	 * @param cols the number of columns in the grid
 	 * @param aliveChance the percent chance that each cell has of starting in
 	 * the "alive" state (0.0 to 1.0)
 	 */
 	public GameOfLife(int rows, int cols, double aliveChance) {
 		current = new Grid(rows, cols);
 		for (int i = 0; i < rows; i++) {
 			for (int j = 0; j < cols; j++) {
 				boolean alive = Math.random() < aliveChance;
 				current.setAlive(i, j, alive);
 			}
 		}
 		next = new Grid(rows, cols);
 	}
 
 	/**
 	 * Constructs a new Game of Life.
 	 * @param rows the number of rows in the grid
 	 * @param cols the number of columns in the grid
 	 */
 	public GameOfLife(int rows, int cols) {
 		this(rows, cols, 0.25);
 	}
 
 	/**
 	 * Gets the number of threads the game will use. Defaults to the computer's
 	 * number of cores.
 	 * @return the number of threads the game will use
 	 */
 	public int getThreads() {
 		return threads;
 	}
 
 	/**
 	 * Sets the number of threads the game will use. Defaults to the computer's
 	 * number of cores.
 	 * @param threads the number of threads the game will use
 	 */
 	public void setThreads(int threads) {
 		this.threads = threads;
 		threadList = new LifeThread[threads];
 	}
 
 	/**
 	 * Gets the number of random cells whose state will be toggled every
 	 * iteration.
 	 * @return the number of random cells whose state will be toggled every
 	 * iteration
 	 */
 	public int getNoise() {
 		return noise;
 	}
 
 	/**
 	 * Sets the number of random cells whose state will be toggled every
 	 * iteration.
 	 * @param noise the number of random cells whose state will be toggled every
 	 * iteration
 	 */
 	public void setNoise(int noise) {
 		this.noise = noise;
 	}
 
 	/**
 	 * Gets the current grid.
 	 * @return the current grid
 	 */
 	public Grid getGrid() {
 		return current;
 	}
 
 	/**
 	 * Gets the number of iterations the game has run for.
 	 * @return the number of iterations the game has run for
 	 */
 	public long getIterationCount() {
 		return iterationCount;
 	}
 
 	/**
 	 * Moves the game forward the given number of steps.
 	 * @param numIterations the number of steps to move the game forward
 	 * @return the resulting grid state
 	 */
 	public Grid iterate(long numIterations) {
 		for (long i = 0; i < numIterations; i++) {
 			iterate();
 		}
 		return current;
 	}
 
 	/**
 	 * Moves the game forward one step.
 	 * @return the resulting grid state
 	 */
 	public Grid iterate() {
 		//decompose task
 		for (int i = 0; i < threadList.length; i++) {
 			threadList[i] = new LifeThread(i);
 		}
 
 		//start threads
 		for (LifeThread t : threadList) {
 			t.start();
 		}
 
 		//wait for them to finish
 		for (LifeThread t : threadList) {
 			try {
 				t.join();
 			} catch (InterruptedException e) {
 			}
 		}
 
 		//add noise (toggle random cells)
		if (noise > 0) {
			int changed[][] = new int[noise][2];
			for (int i = 0; i < noise; i++) {
				//find a random cell (that hasn't already been chosen this iteration)
				boolean repeat;
				int row, col;
				do {
					repeat = false;
					row = (int) (Math.random() * current.getRows());
					col = (int) (Math.random() * current.getCols());
					for (int j = 0; j < i; j++){
						if (changed[j][0] == row && changed[j][1] == col){
							repeat = true;
							break;
						}
					}
				} while (repeat);
				
				boolean alive = next.isAlive(row, col);
				next.setAlive(row, col, !alive);
				changed[i][0] = row;
				changed[i][1] = col;
			}
 		}
 
 		//swap grids
 		Grid temp = current;
 		current = next;
 		next = temp;
 
 		iterationCount++;
 
 		return current;
 	}
 
 	/**
 	 * The thread object that is used to calculate the next game state.
 	 * @author mangst
 	 * 
 	 */
 	private class LifeThread extends Thread {
 		/**
 		 * This thread's number, used for determining which parts of the board
 		 * it must handle.
 		 */
 		private int num;
 
 		/**
 		 * Constructs a new life thread.
 		 * @param num the thread's number
 		 */
 		public LifeThread(int num) {
 			this.num = num;
 		}
 
 		@Override
 		public void run() {
 			int row = num;
 			while (row < current.getRows()) {
 				for (int j = 0; j < current.getCols(); j++) {
 					//determine if the cell should be alive or dead next round
 					boolean alive = current.isAlive(row, j);
 					boolean nextAlive;
 					int surrounding = current.getAliveSurrounding(row, j);
 					if (alive) {
 						nextAlive = surrounding == 2 || surrounding == 3;
 					} else {
 						nextAlive = surrounding == 3;
 					}
 					next.setAlive(row, j, nextAlive);
 				}
 				row += threads;
 			}
 		}
 	}
 }
