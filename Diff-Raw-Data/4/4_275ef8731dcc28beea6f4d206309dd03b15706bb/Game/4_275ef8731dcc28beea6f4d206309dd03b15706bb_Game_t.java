 
 import org.newdawn.slick.*;
 
 import LinkedQueue.*;
 
 public class Game extends BasicGame {
 
 	private final int HEIGHT = 600;
 	private final int WIDTH = 300;
 
 	private int rowHeight;
 	private int columnWidth;
 	private GameGrid tetrisGrid;
 	private Shape current;
 	private Shape ghost;
 	private Shape hold;
 	private int level;
 	private int linesCleared;
 	private int elapsedTime;
 	private int max;
 	private Shape[] upcoming;
 	private LinkedQueue<Shape> generated;
 	private int score;
 	private int freeFallIterations;
 	private boolean heldThisTurn;
 	private boolean paused;
 	private boolean lineClearedDelay;
 	public Game(String title) {
 		super(title);
 	}
 
 	public int getRowHeight() {
 		return rowHeight;
 	}
 
 
 
 	public void setRowHeight(int rowHeight) {
 		this.rowHeight = rowHeight;
 	}
 
 
 
 	public int getColumnWidth() {
 		return columnWidth;
 	}
 
 
 
 	public void setColumnWidth(int columnWidth) {
 		this.columnWidth = columnWidth;
 	}
 
 
 
 	public GameGrid getTetrisGrid() {
 		return tetrisGrid;
 	}
 
 
 
 	public void setTetrisGrid(GameGrid tetrisGrid) {
 		this.tetrisGrid = tetrisGrid;
 	}
 
 
 
 	public Shape getCurrent() {
 		return current;
 	}
 
 
 
 	public void setCurrent(Shape current) {
 		this.current = current;
 	}
 
 
 
 	@Override
 	public void init(GameContainer container) throws SlickException {
 		// TODO Auto-generated method stub
 		restart();
 		rowHeight = HEIGHT / (tetrisGrid.ROWS - 2);
 		columnWidth = WIDTH / tetrisGrid.COLUMNS;
 	}
 
 	@Override
 	public void update(GameContainer container, int delta) throws SlickException {
 		// TODO Auto-generated method stub
 		elapsedTime += delta;
 		if (!tetrisGrid.isLose() && !paused) {
 			if (container.isPaused())
 				container.resume();
 			if (generated.isEmpty())
 				generated = Shape.generate();
 			if (lineClearedDelay) {
 				if (elapsedTime > 367 + (33 * (11 - level))) {
 					elapsedTime = 0;
 					lineClearedDelay = false;
 				}
 			}
 			else if (current == null) {
 				try {
 					current = upcoming[0];
 					upcoming[0] = upcoming[1];
 					upcoming[1] = upcoming[2];
 					upcoming[2] = generated.remove();
 				} catch (QueueUnderflowException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				while (current.getTop() < 2 && shapeCanFall(tetrisGrid, current)) {
 					++current.y;
 				}
 				ghost = new GhostShape(current);
 				hardDrop(ghost);
 			}
 			else {
 				if (elapsedTime > max) {
 					elapsedTime = 0;
 					ghost = new GhostShape(current);
 					hardDrop(ghost);
 					if (shapeCanFall(tetrisGrid, current)) {
 						++current.y;
 						++freeFallIterations;
 					}
 					else {
 						tetrisGrid.embedShape(current);
 						current = null;
 						ghost = null;
 						updateLevel(container, tetrisGrid.clearLines(tetrisGrid.linesToClear()));
 						freeFallIterations = 0;
 						heldThisTurn = false;
 					}
 				}
 			}
 		}
 		else if (paused) {
 			if (!container.isPaused())
 				container.pause();
 		}
 
 	}
 
 	@Override
 	public void render(GameContainer container, Graphics g) throws SlickException {
 		// TODO Auto-generated method stub
 		tetrisGrid.draw();
 		if (ghost != null) {
 			ghost.draw();
 		}
 		if (current != null) {
 			current.draw();
 		}
 		if (tetrisGrid.isLose()) {
 			g.drawString("You lose (esc to restart)", 50, 300);
 
 		}
 		if (paused) {
 			g.drawString("Paused (esc to resume)", 50, 300);
 
 		}
 		g.drawString("Level: " + level, 300, 0);
 		g.drawString("Rows:  " + linesCleared, 300, 20);
 		g.drawString("Score: " + score, 300, 40);
 		//draw held
 		drawHeld(g);
 		drawUpcoming(g);
 		drawControls(g);
 	}
 
 	@Override
 	public void keyPressed (int kc, char v) {
 
 		if (paused) {
 			if (kc == Input.KEY_ESCAPE) {
 				paused = false;
 			}	
 		}
 		else if (!tetrisGrid.isLose() && current != null) {
 			//move left
 			if (kc == Input.KEY_LEFT) {
 				if (shapeCanMoveLeft(tetrisGrid, current)) {
 					--current.x;
 					ghost = new GhostShape(current);
 					hardDrop(ghost);
 				}
 			}
 			//move right
 			if (kc == Input.KEY_RIGHT) {
 				if (shapeCanMoveRight(tetrisGrid, current)) {
 					++current.x;
 					ghost = new GhostShape(current);
 					hardDrop(ghost);
 				}
 			}
 			//soft drop
 			if (kc == Input.KEY_DOWN) {
 				if (shapeCanFall(tetrisGrid, current)) {
 					elapsedTime = 0;
 					++current.y;
 					ghost = new GhostShape(current);
 					hardDrop(ghost);
 				}
 			}
 			//hard drop
 			if (kc == Input.KEY_SPACE) {
 				hardDrop(current);
 			}
 			//rotate clockwise
 			if (kc == Input.KEY_Z || kc == Input.KEY_UP) {
 				wallKickRotateCW();
 				ghost = new GhostShape(current);
 				hardDrop(ghost);
 			}
 			//rotate counterclockwise
 			if (kc == Input.KEY_X) {
 				wallKickRotateCCW();
 				ghost = new GhostShape(current);
 				hardDrop(ghost);
 			}
 			//hold
 			if (kc == Input.KEY_C) {
 				swapHold();
 			}
 			//pause
 			if (kc == Input.KEY_ESCAPE) {
 				paused = true;
 			}
 		}
		else if (tetrisGrid.isLose()) {
 			if (kc == Input.KEY_ESCAPE) {
 				restart();
 			}
		}
 	}
 
 	public boolean shapeCanFall(GameGrid grid, Shape shape) {
 		int startY = shape.getY();
 		int startX = shape.getX();
 
 
 		for (int y = 0; y < 4; ++y) {
 			for (int x = 0; x < 4; ++x) {
 				if (shape.getBlocks()[y][x] != null) {
 					if (y + startY >= grid.ROWS - 1) {
 						return false;
 					}
 					else if (!grid.getGrid()[y + startY + 1][x + startX].isEmpty()) {
 						return false;
 					}
 				}
 			}
 		}
 		return true;
 	}
 
 	public boolean shapeCanMoveLeft(GameGrid grid, Shape shape) {
 		int startY = shape.getY();
 		int startX = shape.getX();
 
 		if (shape.getLeft() <= 0) {
 			return false;
 		}
 		else {
 			for (int y = 0; y < 4; ++y) {
 				for (int x = 0; x < 4; ++x) {
 					if (shape.getBlocks()[y][x] != null) {
 						if (!grid.getGrid()[y + startY][x + startX - 1].isEmpty()) {
 							return false;
 						}
 					}
 				}
 			}
 			return true;
 		}
 	}
 
 	public boolean shapeCanMoveRight(GameGrid grid, Shape shape) {
 		int startY = shape.getY();
 		int startX = shape.getX();
 
 		if (shape.getRight() >= grid.COLUMNS - 1) {
 			return false;
 		}
 		else {
 			for (int y = 0; y < 4; ++y) {
 				for (int x = 0; x < 4; ++x) {
 					if (shape.getBlocks()[y][x] != null) {
 						if (!grid.getGrid()[y + startY][x + startX + 1].isEmpty()) {
 							return false;
 						}
 					}
 				}
 			}
 			return true;
 		}
 	}
 
 	public boolean shapeCanRotateCW(GameGrid grid, Shape shape) {
 		shape.rotateCW();
 		int startY = shape.getY();
 		int startX = shape.getX();
 
 		if (shape.getRight() > grid.COLUMNS - 1 || shape.getLeft() < 0 
 				|| shape.getBottom() > grid.ROWS - 1 || shape.getTop() < 0) {
 			shape.rotateCCW();
 			return false;
 		}
 		else {
 			for (int y = 0; y < 4; ++y) {
 				for (int x = 0; x < 4; ++x) {
 					if (shape.getBlocks()[y][x] != null) {
 						if (!grid.getGrid()[y + startY][x + startX].isEmpty()) {
 							shape.rotateCCW();
 							return false;
 						}
 					}
 				}
 			}
 			shape.rotateCCW();
 			return true;
 		}
 	}
 
 	public boolean shapeCanRotateCCW(GameGrid grid, Shape shape) {
 		shape.rotateCCW();
 		int startY = shape.getY();
 		int startX = shape.getX();
 
 		if (shape.getRight() > grid.COLUMNS - 1 || shape.getLeft() < 0 
 				|| shape.getBottom() > grid.ROWS - 1 || shape.getTop() < 0) {
 			shape.rotateCW();
 			return false;
 		}
 		else {
 			for (int y = 0; y < 4; ++y) {
 				for (int x = 0; x < 4; ++x) {
 					if (shape.getBlocks()[y][x] != null) {
 						if (!grid.getGrid()[y + startY][x + startX].isEmpty()) {
 							shape.rotateCW();
 							return false;
 						}
 					}
 				}
 			}
 			shape.rotateCW();
 			return true;
 		}
 	}
 
 	public void hardDrop(Shape shape) {
 		while (shapeCanFall(tetrisGrid, shape)) {
 			if (shape == current)
 				elapsedTime = max;
 			++shape.y;
 		}
 	}
 
 	public void addScore() {
 		score += ((21 + (3 * level)) - freeFallIterations);
 	}
 
 	public void updateLevel(GameContainer container, int lines) {
 		addScore();
 		linesCleared += lines;
 		if (linesCleared <= 0)
 		{
 			level = 1;
 		}
 		else if ((linesCleared >= 1) && (linesCleared <= 90))
 		{
 			level = 1 + ((linesCleared - 1) / 10);
 		}
 		else if (linesCleared >= 91)
 		{
 			level = 10;
 		}
 		max = (50 * (11 - level));
 		//wait for the time of one block drop if a line is cleared
 		if (lines > 0)
 			lineClearedDelay = true;
 	}
 
 	public void restart() {
 		tetrisGrid = new GameGrid(this);
 		Shape.setGame(this);
 		upcoming = new Shape[3];
 		generated = Shape.generate();
 		//initialize shape
 		try {
 			current = generated.remove();
 		} catch (QueueUnderflowException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		//initialize upcoming
 		for (int i = 0; i < 3; ++i) {
 			try {
 				upcoming[i] = generated.remove();
 			} catch (QueueUnderflowException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		hold = null;
 		score = 0;
 		level = 1;
 		linesCleared = 0;
 		freeFallIterations = 0;
 		max = (50 * (11 - level));
 		heldThisTurn = false;
 		elapsedTime = 0;
 		while (current.getTop() < 2 && shapeCanFall(tetrisGrid, current)) {
 			++current.y;
 		}
 		ghost = new GhostShape(current);
 		hardDrop(ghost);
 	}
 
 	public void wallKickRotateCW() {
 		if (shapeCanRotateCW(tetrisGrid, current)){
 			current.rotateCW();
 		}
 		else {
 			current.x++;
 			if (shapeCanRotateCW(tetrisGrid, current)){
 				current.rotateCW();
 			}
 			else {
 				current.x -= 2;
 				if (shapeCanRotateCW(tetrisGrid, current)){
 					current.rotateCW();
 				}
 				else
 					current.x++;
 			}
 		}
 	}
 
 	public void wallKickRotateCCW() {
 		if (shapeCanRotateCCW(tetrisGrid, current)){
 			current.rotateCCW();
 		}
 		else {
 			current.x++;
 			if (shapeCanRotateCCW(tetrisGrid, current)){
 				current.rotateCCW();
 			}
 			else {
 				current.x -= 2;
 				if (shapeCanRotateCCW(tetrisGrid, current)){
 					current.rotateCCW();
 				}
 				else
 					current.x++;
 			}
 		}
 	}
 
 	public void swapHold() {
 		if (!heldThisTurn) {
 			current.reset();
 			if (hold == null) {
 				hold = current;
 				current = upcoming[0];
 			}
 			else {
 				Shape temp = current;
 				current = hold;
 				hold = temp;
 			}
 			//push piece down
 			while (current.getTop() < 2 && shapeCanFall(tetrisGrid, current)) {
 				++current.y;
 			}
 			elapsedTime = 0;
 			heldThisTurn = true;
 		}
 	}
 
 	public void drawHeld(Graphics g) throws SlickException {
 		g.drawString("HOLD", 332, 60);
 		//draw box
 		g.drawLine(324, 77, 376, 77);
 		g.drawLine(324, 129, 376, 129);
 		g.drawLine(324, 77, 324, 129);
 		g.drawLine(376, 77, 376, 129);
 		if (hold == null)
 			g.drawString("NONE", 332, 94);
 		else if (hold instanceof IShape)
 			new Image("res/Ipreview.png").draw(325, 78);
 		else if (hold instanceof JShape)
 			new Image("res/Jpreview.png").draw(325, 78);
 		else if (hold instanceof LShape)
 			new Image("res/Lpreview.png").draw(325, 78);
 		else if (hold instanceof OShape)
 			new Image("res/Opreview.png").draw(325, 78);
 		else if (hold instanceof SShape)
 			new Image("res/Spreview.png").draw(325, 78);
 		else if (hold instanceof TShape)
 			new Image("res/Tpreview.png").draw(325, 78);
 		else if (hold instanceof ZShape)
 			new Image("res/Zpreview.png").draw(325, 78);
 	}
 	
 	public void drawUpcoming(Graphics g) throws SlickException {
 		g.drawString("NEXT", 332, 130);
 		
 		g.drawLine(324, 299, 376, 299);	//bottom-most line
 		for (int i = 0; i < 3; ++i) {
 			//draw box
 			g.drawLine(324, i * 50 + 147, 376, i * 50 + 147);	//top
 			g.drawLine(324, i * 50 + 147, 324, i * 50 + 197);	//left
 			g.drawLine(376, i * 50 + 147, 376, i * 50 + 199);	//right
 			//draw preview
 			if (upcoming[i] instanceof IShape)
 				new Image("res/Ipreview.png").draw(325, i * 50 + 148);
 			else if (upcoming[i] instanceof JShape)
 				new Image("res/Jpreview.png").draw(325, i * 50 + 148);
 			else if (upcoming[i] instanceof LShape)
 				new Image("res/Lpreview.png").draw(325, i * 50 + 148);
 			else if (upcoming[i] instanceof OShape)
 				new Image("res/Opreview.png").draw(325, i * 50 + 148);
 			else if (upcoming[i] instanceof SShape)
 				new Image("res/Spreview.png").draw(325, i * 50 + 148);
 			else if (upcoming[i] instanceof TShape)
 				new Image("res/Tpreview.png").draw(325, i * 50 + 148);
 			else if (upcoming[i] instanceof ZShape)
 				new Image("res/Zpreview.png").draw(325, i * 50 + 148);
 		}
 	}
 	
 	public void drawControls(Graphics g) throws SlickException {
 		g.drawString("CONTROLS", 315, 300);
 		g.drawString("Shift Left:", 300, 320);
 		g.drawString("  LEFT", 300, 340);
 		g.drawString("Shift Right:", 300, 360);
 		g.drawString("  RIGHT", 300, 380);
 		g.drawString("Rotate CW:", 300, 400);
 		g.drawString("  UP, Z", 300, 420);
 		g.drawString("Rotate CCW:", 300, 440);
 		g.drawString("  X", 300, 460);
 		g.drawString("Soft Drop:", 300, 480);
 		g.drawString("  DOWN", 300, 500);
 		g.drawString("Hard Drop:", 300, 520);
 		g.drawString("  SPACE", 300, 540);
 		g.drawString("Pause:", 300, 560);
 		g.drawString("  ESC", 300, 580);
 	}
 		
 }
