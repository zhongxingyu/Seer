 package player;
 
 import java.awt.Point;
 
 import java.util.List;
 import java.util.Scanner;
 import java.util.Set;
 
 import util.Graph;
 
 /**
  * This class holds a state of the game Quoridor.  This holds a copy of all the variables in the game.
  * The AI should be able to make a decision by comparing different states.
  *
  */
 public class GameState {
     /**
      * Default search algorithm to use when searching for paths in the graph.
      */
    private static final String DEFAULT_SEARCH = "breadth-first";
 
 	//TODO: Consider adding a cool diagram to make things clearer.
 	/**
 	 * Represents the location of the walls as an 8x8 grid.  Whenever a wall is placed, the center of the wall
 	 * will be one of 64 spots depending on the location of the wall.  Each spot in the array represents one of 
 	 * these spots.  
 	 * 
 	 * If the value is zero, there is no wall passing through the spot.  If it's one, there's a vertical wall going through
 	 * it.  If it's two, there's a horizontal wall.
 	 * 
 	 * Also, the first index represents the row, and the second represents the column.
 	 */
 	private int[][] walls;
 	/**
 	 * An array of Player objects, there's one for each Player in the game.
 	 */
 	private Player players[];
 	/**
 	 * This number tells us which Player's turn it is.
 	 */
 	private int turn;
 	/**
 	 * Holds the information about the walls in a Graph structure.  It has a Node representing each space on
 	 * the board, and there is initially an Edge between each adjacent Node in the Graph.  As walls are added, 
 	 * corresponding edges are removed from the Graph.
 	 */
 	private Graph<Point> graph;
 	
 	/**
 	 * Constructs a new State with parameters that match what is passed in.
 	 * @param w
 	 * 		This should be an array representing the locations of the walls in the same way walls does.
 	 * @param pls
 	 * 		An array of Player objects.
 	 * @param turn
 	 * 		Represents the current turn.
 	 * @param graph
 	 * 		A copy of a graph.
 	 */
 	public GameState(int[][] w, Player pls[], int turn, Graph<Point> graph) {
 		walls = new int[w.length][w.length];
 		for (int i = 0; i < w.length; i++) {
 			for (int j = 0; j < w.length; j++) {
 				walls[i][j] = w[i][j];
 			}
 		}
 		players = new Player[pls.length];
 		for (int i = 0; i < pls.length; i++) {
 			players[i] = new Player(i, pls[i].getWalls(), pls[i].getColor(), pls[i].getPlayerType());
 			players[i].setLocation(pls[i].getLocation());
 		}
 		this.turn = turn;
 		this.graph = graph.clone();
 	}
 	
 	//TODO: Make either this class use the isStringLegal method in Board, or make Board use the ont in this class.
 	/**
 	 * Copy/pasted from Board.  Takes a String representing a move in the GUIString format, then 
 	 * returns true if the move is legal, or false is the move is not.
 	 * 
 	 * @param input
 	 * 		Takes a GUIString representing a move.
 	 * 		
 	 * 		A GUI String looks like this:
 	 * 		<op> rowNumber columnNumber
 	 * 		
 	 * 		If a Player is moving their piece op should be M.
 	 * 		If a Player is placing a wall, op should be V for a vertical wall, or H for a horizontal wall.
 	 * 		The row and column numbers should be between 0 and 8 for a move or 0 and 7 for a wall.
 	 * @return
 	 * 		true if the move is legal, false if the move is illegal.
 	 */
 	public boolean isStringLegal(String input) {
 		Point xy = new Point();
 		Scanner sc = new Scanner(input);
 		String firstCh = sc.next();
 		if (firstCh.equals("M")) {
 			xy.x = sc.nextInt();
 			xy.y = sc.nextInt();
 			return isMoveLegal(turn, xy);
 		} else if (firstCh.equals("H")) {
 			xy.x = sc.nextInt();
 			xy.y = sc.nextInt();
 			return isHoriWallLegal(turn, xy);
 		} else if (firstCh.equals("V")) {
 			xy.x = sc.nextInt();
 			xy.y = sc.nextInt();
 			return isVertWallLegal(turn, xy);
 		}
 		return false;
 	}
 	
 	/**
 	 * Called by isStringLegal(String input) when a horizontal wall is being tested for legality.
 	 * 
 	 * @param player
 	 * 		This is the Player that wants to place a wall.
 	 * @param loc
 	 * 		The spot where the wall is being placed.
 	 * @return
 	 * 		true if the wall is legal, false is it's illegal.
 	 */
 	private boolean isHoriWallLegal(int player, Point loc) {
 		//Tests to see if the Player has walls remaining and whether or not the wall is on the board.
 		if (players[player].getWalls() > 0 && loc.x < 8 && loc.x > -1 && loc.y > -1 && loc.y < 8) {
 			//Checks to see if a wall is already going through the same space.
 			if (walls[loc.x][loc.y] > 0)
 				return false;
 			if (loc.x < 7)
 				if (walls[loc.x+1][loc.y] == 2)
 					return false;
 			if (loc.x > 0)
 				if (walls[loc.x-1][loc.y] == 2)
 					return false;
 			
 			boolean legal = true;	//if any player would be blocked by the wall, this will be set to false
 			
 			graph.removeEdge(new Point(loc.x,loc.y), new Point(loc.x,loc.y+1));
 			graph.removeEdge(new Point(loc.x+1,loc.y), new Point(loc.x+1,loc.y+1));
             List<Point> path;
 			for (int i = 0; i < players.length; i++) {
 				path = graph.findPath(DEFAULT_SEARCH, players[i].getLocation(), players[i].goalSet);
                 if (path.isEmpty()) {
 					legal = false;
 				}
 			}
 			graph.addEdge(new Point(loc.x,loc.y), new Point(loc.x,loc.y+1));
 			graph.addEdge(new Point(loc.x+1,loc.y), new Point(loc.x+1,loc.y+1));
 			return legal;
 		}
 
 		return false;
 	}
 	
 	/**
 	 * Called by isStringLegal(String input) when a vertical wall is being tested for legality.
 	 * 
 	 * @param player
 	 * 		This is the Player that wants to place a wall.
 	 * @param loc
 	 * 		The spot where the wall is being placed.
 	 * @return
 	 * 		true if the wall is legal, false is it's illegal.
 	 */
 	private boolean isVertWallLegal(int player, Point loc) {
 		//Tests to see if the Player has walls remaining and whether or not the wall is on the board.
 		if (players[player].getWalls() > 0 && loc.x < 8 && loc.x > -1 && loc.y > -1 && loc.y < 8) {
 			//Checks to see if a wall is already going through the same space.
 			if (walls[loc.x][loc.y] > 0)
 				return false;
 			if (loc.y < 7)
 				if (walls[loc.x][loc.y+1] == 1)
 					return false;
 			if (loc.y > 0)
 				if (walls[loc.x][loc.y-1] == 1)
 					return false;
 			
 			boolean legal = true;	//if any player would be blocked by the wall, this will be set to false
 			
 			graph.removeEdge(new Point(loc.x,loc.y), new Point(loc.x+1,loc.y));
 			graph.removeEdge(new Point(loc.x,loc.y+1), new Point(loc.x+1,loc.y+1));
             List<Point> path;
 			for (int i = 0; i < players.length; i++) {
 				path = graph.findPath(DEFAULT_SEARCH, players[i].getLocation(), players[i].goalSet);
                 if (path.isEmpty()) {
 					legal = false;
 				}
 			}
 			graph.addEdge(new Point(loc.x,loc.y), new Point(loc.x+1,loc.y));
 			graph.addEdge(new Point(loc.x,loc.y+1), new Point(loc.x+1,loc.y+1));
 
 			return legal;
 		}
 		return false;
 	}
 	
 	/**
 	 * Called by isStringLegal to test whether a Player can move to a particular spot.
 	 * @param player
 	 * 		This number represents the ID of the Player trying to move.
 	 * @param move
 	 * 		This is the location the Player is attempting to move to.
 	 * @return
 	 * 		true if the Player can move there, false if the Player can't.
 	 */
 	private boolean isMoveLegal(int player, Point move) {
 		return isMoveLegal(player, move, 0);
 	}
 	
 	/**
 	 * Called by isMoveLegal(int player, Point move), finds all the possible spots a Player could move to and compares
 	 * them to the location the Player wants to move to.
 	 * @param player
 	 * 		The ID of the Player that's trying to move.
 	 * @param move
 	 * 		The location the Player is trying to move to.
 	 * @param rec
 	 * 		This counts the number of times the method has been called.  If this number is greater than the number
 	 * 		of players, it returns false.
 	 * @return
 	 * 		true if the move is legal, or false if it is illegal.
 	 */
 	private boolean isMoveLegal(int player, Point move, int rec) {
 		if (rec >= players.length)
 			return false;
 		
 		Player currentPlayer = players[player];
 		Point[] adjacentSpaces = new Point[4];
 		adjacentSpaces[0] = currentPlayer.up();
 		adjacentSpaces[1] = currentPlayer.down();
 		adjacentSpaces[2] = currentPlayer.left();
 		adjacentSpaces[3] = currentPlayer.right();
 		
 		for (int i = 0; i < adjacentSpaces.length; i++) {
 			if (adjacentSpaces[i] != null) {
 				if (!isBlocked(currentPlayer.getLocation(), adjacentSpaces[i])) {
 					int PID = PlayerOnSpace(adjacentSpaces[i]);
 					if (PID >= 0) {
 						if (isMoveLegal(PID, move, rec+1)) {
 							return true;
 						}
 					}
 					else if (adjacentSpaces[i].equals(move)) {
 						return true;	
 					}
 				}
 			}
 		}
 		
 		return false;
 	}
 	
 	//TODO: Take care of the case where the two Points aren't right next to each other.
 	/**
 	 * Determines whether or not a wall is between two locations.  Assumes the two locations are right next to 
 	 * each other.
 	 * @param p1
 	 * 		The first location.
 	 * @param p2
 	 * 		The second location.
 	 * @return
 	 * 		true if there is a wall between them, false if there isn't.
 	 * 		May return anything if the spaces are not directly next to each other.
 	 */
 	public boolean isBlocked(Point p1, Point p2) {
 		int smaller = -1; // 
 		// if the two spaces are in the same column
 		if (p1.x == p2.x) {
 			smaller = Math.min(p1.y,p2.y); //finds the point that's higher up
 			if (p1.x < 8)
 				if (walls[p1.x][smaller] == 2)
 					return true;
 			if (p1.x > 0)
 				if (walls[p1.x-1][smaller] == 2)
 					return true;
 		}
 		// if the two spaces are in the same row
 		else if (p1.y == p2.y) {
 			smaller = Math.min(p1.x,p2.x); //finds the space to the left
 			if (p1.y < 8)
 				if (walls[smaller][p1.y] == 1)
 					return true;
 			if (p1.y > 0)
 				if (walls[smaller][p1.y-1] == 1)
 					return true;
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * This method determines whether a Player is currently on a particular space.
 	 * @param p
 	 * 		p is the Space we are checking.
 	 * @return
 	 * 		the ID of the Player if there is a Player on p.  Returns -1 if there is no Player on the space.
 	 */
 	private int PlayerOnSpace(Point p) {
 		for (int i = 0; i < players.length; i++) {
 			if (p.getLocation().equals(players[i].getLocation())) {
 				return i;
 			}
 		}
 		return -1;
 	}
 	
 	//TODO: Lots of refactoring to make Board use a State Object to check the legality of moves and also make moves.
 	
 	/**
 	 * Returns a new State which shows what the Board should be after the move has been made.
 	 * Do not call this method without calling isStringLegal first and making sure the move is valid.
 	 * @param move
 	 * 		This is a GUIString which represents the move that is being made.
 	 * @return
 	 * 		Returns the new State of the game after the move has been made.
 	 */
 	public GameState move(String move) {
 		GameState newState = clone();
 		newState.readStringFromGUI(move);
 		return newState;
 	}
 	
 	/**
 	 * Called by the move method to make the necessary changes to newState before returning it.
 	 * @param input
 	 * 		The GUIString representing the move being made.
 	 */
 	private void readStringFromGUI(String input) {
 			Point xy = new Point();
 			Scanner sc = new Scanner(input);
 			String firstCh = sc.next();
 			if (firstCh.equals("M")) {
 				xy.x = sc.nextInt();
 				xy.y = sc.nextInt();
 				move(xy);
 			} else {
 				//addAllJumps(false);
 				if (firstCh.equals("H")) {
 					xy.x = sc.nextInt();
 					xy.y = sc.nextInt();
 					placeHoriWall(xy);
 				} else if (firstCh.equals("V")) {
 					xy.x = sc.nextInt();
 					xy.y = sc.nextInt();
 					placeVertWall(xy);
 				}
 			}
 			//addAllJumps(true);
 		}
 		
 		/**
 		 * Called by readStringFromGUI(String input) when a horizontal wall needs to be placed.
 		 * This method places the horizontal wall.
 		 * @param xy
 		 * 		The location the where wall is being placed.
 		 */
 		private void placeHoriWall(Point xy) {
 			if (players[turn].getWalls() > 0) {
 				walls[xy.x][xy.y] = 2;
                 graph.removeEdge(new Point(xy.x,xy.y), new Point(xy.x,xy.y+1));
                 graph.removeEdge(new Point(xy.x+1,xy.y), new Point(xy.x+1,xy.y+1));
 				players[turn].decrementWall();
 				nextTurn();
 			}
 		}
 		
 		/**
 		 * Called by readStringFromGUI(String input) when a vertical wall needs to be placed.
 		 * This method places the vertical wall.
 		 * @param xy
 		 * 		The location the where wall is being placed.
 		 */
 		private void placeVertWall(Point xy) {
 			if (players[turn].getWalls() > 0) {
 				walls[xy.x][xy.y] = 1;
                 graph.removeEdge(new Point(xy.x,xy.y), new Point(xy.x+1,xy.y));
                 graph.removeEdge(new Point(xy.x,xy.y+1), new Point(xy.x+1,xy.y+1));
 				players[turn].decrementWall();
 				nextTurn();
 			}
 		}
 
 		/**
 		 * Called by readStringFromGUI(String input) when Player needs to be moved.  Moves the current Player
 		 * to a new location.
 		 * @param p
 		 * 		The location where the Player is being moved.
 		 */
 		private void move(Point p) {
 			players[turn].setLocation(p);
 			nextTurn();
 		}
 
 		/**
 		 * Returns an int representing the next Player.  If a player has been removed 
 		 * from the game, an int representing the Player after them is returned instead.
 		 * @return
 		 * 		an int representing the next Player
 		 */
 		public int getNextPlayerNum() {
 			int num = turn;
 			do {
 				num = (num + players.length + 1) % players.length;
 			}while(players[num].getPlayerType() == Player.REMOVED);
 			return num;
 		}
 		
 		/**
 		 * Returns a reference to the Player Object which is moving next.  If that Player has
 		 * been removed, the Player after them is returned instead.
 		 * @return
 		 * 		a reference to the next Player object.
 		 */
 		public Player getNextPlayer() {
 			int num = turn;
 			do {
 				num = (num + players.length + 1) % players.length;
 			}while(players[num].getPlayerType() == Player.REMOVED);
 			return players[num];
 		}
 		
 		/**
 		 * Returns an int representing the previous Player.  If a player has been removed 
 		 * from the game, an int representing the Player before them is returned instead.
 		 * @return
 		 * 		an int representing the previous Player
 		 */
 		public int getPrevPlayerNum() {
 			int num = turn;
 			do {
 				num = (num + players.length - 1) % players.length;
 			}while(players[num].getPlayerType() == Player.REMOVED);
 			return num;
 		}
 		
 		/**
 		 * Returns a reference to the Player Object which is moved previously.  If that Player has
 		 * been removed, the Player before them is returned instead.
 		 * @return
 		 * 		a reference to the previous Player object.
 		 */
 		public Player getPrevPlayer() {
 			int num = turn;
 			do {
 				num = (num + players.length - 1) % players.length;
 			}while(players[num].getPlayerType() == Player.REMOVED);
 			return players[num];
 		}
 		
 		/**
 		 * Called after any move is made to increment turn;
 		 */
 		private void nextTurn() {
 			turn = getNextPlayerNum();
 		}
 		
 		//TODO: Make it return a copy of the array
 		/**
 		 * Returns an array representing the locations of the walls.
 		 * @return
 		 * 		an 8x8 int array representing the locations of the walls on the board.
 		 */
 		public int[][] getWalls() {
 			return walls;
 		}
 		
 		//TODO: Make this return a copy of the Players array instead of the actual thing
 		/**
 		 * Returns a reference to the array containing the information about each Player.
 		 * @return
 		 * 		an array containing a Player object for each Player.
 		 */
 		public Player[] getPlayerArray() {
 			return players;
 		}
 		
 		public Player getPlayer(int num) {
 			return players[num];
 		}
 		
 		public int getCurrentPlayerNum() {
 			return turn;
 		}
 		
 		public Player getCurrentPlayer() {
 			return players[turn];
 		}
 		
 		public Point getCurrentPlayerLocation() {
 			return players[turn].getLocation();
 		}
 		
 		public Point getPlayerLocation(int num) {
 			return players[num].getLocation();
 		}
 		
 		public Set<Point> getCurrentPlayerGoalSet() {
 			return players[turn].goalSet;
 		}
 		
 		public Set<Point> getPlayerGoalSet(int num) {
 			return players[num].goalSet;
 		}
 		
 		/**
 		 * Returns the turn number.
 		 * @return
 		 * 		returns the turn number.
 		 */
 		public int getTurn() {
 			return turn;
 		}
 		/**
 		 * Returns the graph that knows where walls are located.
 		 * @return
 		 * 		a graph representing all the possible moves a Player could make.
 		 */
 		public Graph<Point> getGraph() {
 			return graph;
 		}
 		
 		/**
 		 * Returns true if the specified Player has any walls left.  Otherwise, it returns false.
 		 * @param player
 		 * 		An int representing which Player we're interested in.
 		 * @return
 		 * 		true if the Player has walls left, false otherwise.
 		 */
 		public boolean hasWalls(int player) {
 			return players[player].hasWalls();
 		}
 		
 		/**
 		 * Returns the number of walls the specified Player has left.
 		 * @param player
 		 * 		This is the number of the Player we're interested in.
 		 * @return
 		 * 		the number of walls the Player has left.
 		 */
 		public int numberOfWalls(int player){
 			return players[player].getWalls();
 		}
 		
 		/**
 		 * Returns an int representing the current Player's type.
 		 * @return
 		 * 		an int representing a Player type.
 		 */
 		public int getCurrentPlayerType() {
 			return players[turn].getPlayerType();
 		}
 		
 		public GameState clone() {
 			return new GameState(walls, players, turn, graph);
 		}
 		
 		/**
 		 * This method removes a specified Player from the game.
 		 * @param pl
 		 * 		This is an int representing the Player.
 		 */
 		public void removePlayer(int pl) {
 			players[pl].remove();
 		}
 		
 		/**
 		 * Adds a set of edges to the graph that allows pawns
 		 * to jump over each other. Checks additional spaces
 		 * for double or triple jumps recursively with the
 		 * assJumpEdges method. Returns a copy of the graph
 		 * before these changes are made, so that we can revert 
 		 * to that board after the move is made by the AI
 		 * 
 		 * @param point
 		 * 		the point on the board whose neighboring spaces
 		 * 		we will mark as potential movement destinations
 		 * 
 		 * @return
 		 * 		a copy of the initial state of the board before
 		 * 		any changes are made
 		 */
 	    /*public Graph<Point> addJumpEdgesToGraph(Point point){
 			Graph<Point> originalGraph = graph.clone();
 			addJumpEdges(point);
 			return originalGraph;		
 	    }*/
 
 	    public Graph<Point> getGraphWithJumpEdges(Point point){
 	    	Graph<Point> editedGraph = graph.clone();
 	    	
 	    	Set<Point> neighbors = editedGraph.neighbors(point);
 	    	//here, neighbors is the set of nodes directly adjacent to
 	    	//the player's location
 	    
 	    	addJumpEdges(point, neighbors, editedGraph, 0);
 	    	
 	    	for(Point p: neighbors){
 	    		//Look at spaces directly adjacent to the point.
 	    		//If there is a player there, remove that edge so
 	    		//that jumping over it will be the shortest path.
 	    		if(PlayerOnSpace(p) != -1){
 	    			editedGraph.removeEdge(p, point);
 	    		}
 	    	}
 	    	return editedGraph;
 	    }
 	    
 	    private void addJumpEdges(Point player, Set<Point> points, Graph<Point> gp, int rec){
 	    	if (rec >= players.length)
 	    		return;
 	    	Set<Point> neighbors;
 	    	for(Point p: points){
 	    		if(PlayerOnSpace(p) != -1){
 	    			neighbors = gp.neighbors(p);
 	    			addJumpEdges(player, neighbors, gp, rec+1);
 	    		}
 	    		gp.addEdge(p, player);
 	    	}
 	    	
 	    }
 	    
 	    public void setGraph(Graph<Point> graph){
 	    	this.graph = graph;
 	    }
 	    
 }
