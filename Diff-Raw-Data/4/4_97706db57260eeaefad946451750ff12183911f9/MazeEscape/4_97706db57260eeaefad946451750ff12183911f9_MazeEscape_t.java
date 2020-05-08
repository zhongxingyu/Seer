 package mazeEscapeApp;
 
 import java.awt.Color;
 import java.util.ArrayList;
 
 import javax.swing.JMenuBar;
 import javax.swing.JToolBar;
 
 import mazeEscapeUtils.*;
 
 import org.jhotdraw.application.DrawApplication;
 import org.jhotdraw.framework.FigureAttributeConstant;
 import org.jhotdraw.framework.Tool;
 
 /*
  * Class represents an instance of a mazeEscape game. No GUI involved here.
  */
 public class MazeEscape extends DrawApplication {
 	private static final long serialVersionUID = 5843503278232195529L;
 	// The row, column arrays storing the clickable areas and respective
 	// GridCell frames
 	private GCellArea[][] gCellClickableArea;
 	private GridCell[][] gridCells;
 
 	// For an n * n maze, length = width = n
 	private int lengthMaze;
 	private int gCellPixelLength = 35;
 	private String difficulty;
 
 	// Currently selected grid cell (used for game; user grid selection)
 	private GCellArea currentlySelected;
 	private GCellArea startCell, endCell;
 
 	// Used in handleClick. Checks to see if first selected cell
 	private boolean isFirstClick = true;
 	private boolean reachedEndCell = false;
 	private boolean reset = false;
 	private boolean start = true;
 
 	// Used for calculation of score
 	private boolean on = true;
 	private int levelPoints = 0;
 	private int timePassed = 0;
 	private int timeScore = 0;
 	private int minSteps = 0;
 	private int stepsTaken = 0;
 
 	// Used for displaying time and steps
 	private MazeText stepCount;
 	private MazeText timeCount;
 	private String time;
 	private String count;
 
 	// Used for the toolbar code
 	private GUIDrawer guiDrawer;
 	private ForfeitButton fb;
 	private ResetButton rb;
 	private static final String IMAGE = "/resources/";
 
 	private MazeSaveLoad msl;
 
 	public MazeEscape() {
 		super("MazeEscape");
 		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
 		ReplayScreen screen = new ReplayScreen(this);
 		screen.selectDifficulty();
 		setDifficultyMode(difficulty);
 
 		gridCells = new GridCell[lengthMaze][lengthMaze];
 		gCellClickableArea = new GCellArea[lengthMaze][lengthMaze];
 	}
 
 	/**
 	 * Calculates the score based on amount of steps taken combined with time
 	 * taken to reach the end
 	 */
 	@SuppressWarnings("static-access")
 	public void calculateScore() throws InterruptedException {
 		while (on == true) {
 			Thread thread = new Thread();
 			thread.start();
 			// Displays the time and steps taken by user
 			displayTimeAndSteps();
 			// Displays legend to show formula on score calculation
 			displayLegend();
 			if (timePassed > timeScore) {
 				timeCount.setAttribute(FigureAttributeConstant.TEXT_COLOR,
 						Color.BLACK);
 				timeCount.setAttribute(FigureAttributeConstant.FILL_COLOR,
 						Color.RED);
 			}
 			timePassed++;
 			// Checks if end of the maze has been reached
 			if (isReachedEndCell() == true) {
 				// Calculates maze points as well as completion accuracy
 				if (timePassed <= timeScore) {
 					double score = getStepsTaken();
 					int points = timeScore - timePassed + levelPoints;
 					double accuracy = minSteps / score * 100;
 					WinnerScreen win = new WinnerScreen(this);
 					win.writeOutput(accuracy, points);
 					// System.out.println("Your score is: " + points + "!");
 					// System.out.println("Maze completion accuracy is: " +
 					// accuracy +"%!");
 					on = false;
 				} else {
 					double score = getStepsTaken();
 					double accuracy = minSteps / score * 100;
 					WinnerScreen win = new WinnerScreen(this);
 					win.writeOutput(accuracy, levelPoints);
 					// System.out.println("Your score is: " + levelPoints +
 					// "!");
 					// System.out.println("Maze completion accuracy: " +
 					// accuracy +"%!");
 					on = false;
 				}
 			}
 			thread.sleep(1000);
 		}
 	}
 	
 
 	/**
 	 * Displays the time passed and steps taken on the maze
 	 */
 	public void displayTimeAndSteps() {
 		if (timeCount != null || stepCount != null) {
 			this.view().remove(timeCount);
 			this.view().remove(stepCount);
 		}
 		timeCount = new MazeText();
 		timeCount.setAttribute(FigureAttributeConstant.FILL_COLOR, Color.BLUE);
 		timeCount.setAttribute(FigureAttributeConstant.TEXT_COLOR, Color.GREEN);
 		time = Integer.toString(timePassed);
 		timeCount.setText("Time: " + time + " sec");
 
 		stepCount = new MazeText(20, 20);
 		stepCount.setAttribute(FigureAttributeConstant.FILL_COLOR, Color.BLUE);
 		stepCount.setAttribute(FigureAttributeConstant.TEXT_COLOR, Color.GREEN);
 		count = Integer.toString(stepsTaken);
 		stepCount.setText("Steps taken: " + count);
 		this.view().add(timeCount);
 		this.view().add(stepCount);
 	}
 	
 	/**
 	 * Method to display the legend with score formula and color keys
 	 */
 	public void displayLegend() {
 		MazeText legend = new MazeText(150, 0);
 		MazeText score = new MazeText(250, 15);
 		MazeText accuracy = new MazeText(250, 30);
 		MazeText start = new MazeText(150, 15);
 		MazeText end = new MazeText(150, 30);
 		legend.setText("Legend:");
 		score.setText("Maze Score Calculation: time constraint - time passed + bonus level points");
 		accuracy.setText("Maze Accuracy Calculation: 100 x (minimum steps taken / your steps taken)");
 		start.setText("Start Cell Color");
 		end.setText("End Cell Color");
 		legend.setAttribute(FigureAttributeConstant.FILL_COLOR, Color.BLACK);
 		legend.setAttribute(FigureAttributeConstant.TEXT_COLOR, Color.RED);
 		score.setAttribute(FigureAttributeConstant.FILL_COLOR, Color.YELLOW);
 		score.setAttribute(FigureAttributeConstant.TEXT_COLOR, Color.BLUE);
 		accuracy.setAttribute(FigureAttributeConstant.FILL_COLOR, Color.YELLOW);
 		accuracy.setAttribute(FigureAttributeConstant.TEXT_COLOR, Color.BLUE);
 		start.setAttribute(FigureAttributeConstant.FILL_COLOR, Color.BLUE);
 		start.setAttribute(FigureAttributeConstant.TEXT_COLOR, Color.GREEN);
 		end.setAttribute(FigureAttributeConstant.FILL_COLOR, Color.WHITE);
 		end.setAttribute(FigureAttributeConstant.TEXT_COLOR, Color.GREEN);
 		this.view().add(legend);
 		this.view().add(score);
 		this.view().add(accuracy);
 		this.view().add(start);
 		this.view().add(end);
 	}
 
 	public String getDifficulty() {
 		return difficulty;
 	}
 
 	public void setDifficulty(String d) {
 		this.difficulty = d;
 	}
 
 	public void setDifficultyMode(String difficulty) {
 		this.difficulty = difficulty;
 		if (difficulty.equals("Easy")) {
 			lengthMaze = 10;
 			timeScore = 15;
 			levelPoints = 50;
 		} else if (difficulty.equals("Medium")) {
 			lengthMaze = 15;
 			timeScore = 20;
 			levelPoints = 100;
 		} else if (difficulty.equals("Hard")) {
 			lengthMaze = 20;
 			timeScore = 30;
 			levelPoints = 200;
 		}
 	}
 
 	protected void createTools(JToolBar tBar) {
 		// Instantiate the tools
 		setDefaultTool(createDefaultTool());
 		fb = new ForfeitButton(this, guiDrawer, this);
 		rb = new ResetButton(this, this);
 		LoadButton lb = new LoadButton(this, this);
 		SaveButton sb = new SaveButton(this, this);
 		QuitButton qb = new QuitButton(this);
 
 		// Put the relevant tools on the toolbar
 		tBar.add(createToolButton(IMAGE + "RESET", "Reset Game", rb));
		tBar.add(createToolButton(IMAGE + "LOAD", "Load Game", lb));
		tBar.add(createToolButton(IMAGE + "SAVE", "Save Game", sb));
 		tBar.add(createToolButton(IMAGE + "FORFEIT", "Forfeit + See Solution",
 				fb));
 		tBar.add(createToolButton(IMAGE + "QUIT", "Quit Game", qb));
 	}
 
 	protected Tool createDefaultTool() {
 		return new MazeNavigateTool(this);
 	}
 
 	@Override
 	protected void createMenus(JMenuBar mb) {
 
 	}
 
 	/*
 	 * Getters and setters below here
 	 */
 
 	public void setGUIDrawer(GUIDrawer gd) {
 		guiDrawer = gd;
 		fb.setGUIDrawer(guiDrawer);
 	}
 
 	public GCellArea[][] getgCellClickableArea() {
 		return gCellClickableArea;
 	}
 
 	public void setgCellClickableArea(GCellArea[][] gCellClickableArea) {
 		this.gCellClickableArea = gCellClickableArea;
 	}
 
 	public GridCell[][] getGridCells() {
 		return gridCells;
 	}
 
 	public void setGridCells(GridCell[][] gridCells) {
 		this.gridCells = gridCells;
 	}
 
 	public int getLengthMaze() {
 		return lengthMaze;
 	}
 
 	public void setLengthMaze(int lengthMaze) {
 		this.lengthMaze = lengthMaze;
 	}
 
 	public int getgCellPixelLength() {
 		return gCellPixelLength;
 	}
 
 	public void setgCellPixelLength(int gCellPixelLength) {
 		this.gCellPixelLength = gCellPixelLength;
 	}
 
 	public GCellArea getCurrentlySelected() {
 		return currentlySelected;
 	}
 
 	public void setCurrentlySelected(GCellArea currentlySelected) {
 		this.currentlySelected = currentlySelected;
 	}
 
 	public GCellArea getStartCell() {
 		return startCell;
 	}
 
 	public void setStartCell(GCellArea startCell) {
 		this.startCell = startCell;
 		this.startCell.setAttribute(FigureAttributeConstant.FILL_COLOR, Color.BLUE);
 	}
 
 	public GCellArea getEndCell() {
 		return endCell;
 	}
 
 	public void setEndCell(GCellArea endCell) {
 		this.endCell = endCell;
 	}
 
 	public boolean isFirstClick() {
 		return isFirstClick;
 	}
 
 	public void setFirstClick(boolean isFirstClick) {
 		this.isFirstClick = isFirstClick;
 	}
 
 	public boolean isReachedEndCell() {
 		return reachedEndCell;
 	}
 
 	public void setReachedEndCell(boolean reachedEndCell) {
 		// msl.saveGame();
 		//
 		this.reachedEndCell = reachedEndCell;
 	}
 
 	public void setSteps(int mSteps) {
 		this.minSteps = mSteps;
 	}
 
 	public boolean isOn() {
 		return on;
 	}
 
 	public void setOn(boolean on) {
 		this.on = on;
 	}
 
 	public int getStepsTaken() {
 		return stepsTaken;
 	}
 
 	public void setStepsTaken(int stepsTaken) {
 		this.stepsTaken = stepsTaken;
 		if (this.stepsTaken > 0) {
 			rb.setEnabled(true);
 		}
 		count = Integer.toString(stepsTaken);
 		stepCount.setText("Steps taken: " + count);
 	}
 
 	public void setTimePassed(int tPassed) {
 		this.timePassed = tPassed;
 		time = Integer.toString(timePassed);
 		timeCount.setText("Time: " + time + " sec");
 	}
 
 	public void setReset(boolean reset) {
 		this.reset = reset;
 		if (this.reset == true) {
 			this.view().remove(GCellArea.larry);
 			guiDrawer.resetSolution();
 			ArrayList<GCellCoordinate> list = msl.getCellArray();
 			for (GCellCoordinate c:list) {
 				c.getGCellArea().setAttribute(FigureAttributeConstant.FILL_COLOR, new Color(112, 219, 147));
 			}
 			this.startCell.setAttribute(FigureAttributeConstant.FILL_COLOR, Color.BLUE);
 			this.endCell.setAttribute(FigureAttributeConstant.FILL_COLOR, Color.WHITE);
 		}
 	}
 	
 	public void saveGame()
 	{
 		msl.saveGame(this);
 	}
 
 	public boolean getReset() {
 		return reset;
 	}
 
 	public void setMSL(MazeSaveLoad m) {
 		this.msl = m;
 	}
 
 	public MazeSaveLoad getMSL() {
 		return msl;
 	}
 	
 	public void handleFirstClickAutomatically(){
 		gCellClickableArea[1][1].callHandleClick();
 	}
 
 	public int getLevelPoints() {
 		return levelPoints;
 	}
 
 	public void setLevelPoints(int levelPoints) {
 		this.levelPoints = levelPoints;
 	}
 
 	public int getTimeScore() {
 		return timeScore;
 	}
 
 	public void setTimeScore(int timeScore) {
 		this.timeScore = timeScore;
 	}
 
 	public int getMinSteps() {
 		return minSteps;
 	}
 
 	public void setMinSteps(int minSteps) {
 		this.minSteps = minSteps;
 	}
 
 	public MazeText getStepCount() {
 		return stepCount;
 	}
 
 	public void setStepCount(MazeText stepCount) {
 		this.stepCount = stepCount;
 	}
 
 	public MazeText getTimeCount() {
 		return timeCount;
 	}
 
 	public void setTimeCount(MazeText timeCount) {
 		this.timeCount = timeCount;
 	}
 
 	public String getTime() {
 		return time;
 	}
 
 	public void setTime(String time) {
 		this.time = time;
 	}
 
 	public String getCount() {
 		return count;
 	}
 
 	public void setCount(String count) {
 		this.count = count;
 	}
 
 	public GUIDrawer getGuiDrawer() {
 		return guiDrawer;
 	}
 
 	public void setGuiDrawer(GUIDrawer guiDrawer) {
 		this.guiDrawer = guiDrawer;
 	}
 
 	public ForfeitButton getFb() {
 		return fb;
 	}
 
 	public void setFb(ForfeitButton fb) {
 		this.fb = fb;
 	}
 
 	public MazeSaveLoad getMsl() {
 		return msl;
 	}
 
 	public void setMsl(MazeSaveLoad msl) {
 		this.msl = msl;
 	}
 
 	public int getTimePassed() {
 		return timePassed;
 	}
 	
 	public void setStart(boolean start) {
 		this.start = start;
 	}
 	
 	public boolean getStart() {
 		return start;
 	}
 	
 	
 
 }
