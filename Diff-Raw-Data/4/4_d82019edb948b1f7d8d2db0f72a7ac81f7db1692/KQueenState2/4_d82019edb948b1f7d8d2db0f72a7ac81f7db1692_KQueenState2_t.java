 public class KQueenState2 implements State {
 
 	public int k;
 	public int[][] state;
 	public int[] queen;
 	public State[] neighbours;
 
 	// Construct blank state
 	public KQueenState2(int k) {
 		this.k = k;
 	}
 
 	// Construct state
 	public KQueenState2(int k, int[][] state, int[] queen) {
 		this.k = k;
 		this.state = state;
 		this.queen = queen;
 	}
 
 	// Initiate state with random placement of Q on each row
 	public void initState() {
 		// Initiate arrays
 		state = new int[k][k];
 		queen = new int[k];
 
 		// Insert queen in random position on each row
 		for (int i = 0; i < k; i++) {
 			int random = (int) (Math.random() * k);
 			state[i][random] = 1;
 			queen[i] = random;
 		}
 	}
 
 	// Display state
 	public void printState() {
 		// Displays the grid
 		if (k <= 50) {
 			for (int i = 0; i < k; i++) {
 				for (int j = 0; j < k; j++) {
 					System.out.print(state[i][j] + " ");
 				}
 				System.out.println("Queen index : " + queen[i]);
 			}
 		}
 		// Displays the value
 		System.out.println("Value: " + getStateValue());
 		System.out.println("Conflicts: " + getConflicts());
 		System.out.println();
 	}
 
 	// Returns number of conflicts
 	public int getConflicts() {
 		int conflicts = 0;
 		int rowCount;
 
 		// Detects conflicts on each column
 		for (int i = 0; i < k; i++) {
 			rowCount = 0;
 			for (int j = 0; j < k; j++) {
 				if (queen[j] == i) {
 					rowCount++;
 				}
 			}
 			conflicts += Math.max(0, (rowCount - 1));
 		}
 		// Iterates through each left diagonal
 		int row = k - 2;
 		int column = 0;
 
 		while (row >= 0 && column <= k - 2) {
 
 			conflicts += getDiagonalConflictsLeft(row, column);
 
 			if (column == 0)
 				row--;
 			if (row == 0)
 				column++;
 		}
 		conflicts += getDiagonalConflictsLeft(0, 0);
 
 		// Iterates through each right diagonal
 		row = 0;
 		column = 1;
 
 		while (row <= k - 2 && column <= k - 1) {
 
 			conflicts += getDiagonalConflictsRight(row, column);
 
 			if (row == 0)
 				column++;
 			if (column == k - 1)
 				row++;
 		}
 		// If(k>2) bug
 		conflicts += getDiagonalConflictsRight(0, k - 1);
 
 		return conflicts;
 	}
 
 	// Detects conflicts on left diagonal
 	private int getDiagonalConflictsLeft(int row, int column) {
 		int count = 0;
 
 		while (row <= (k - 1) && column <= (k - 1)) {
 
 			if (state[row][column] == 1)
 				count++;
 
 			row++;
 			column++;
 		}
 		return Math.max((count - 1), 0);
 	}
 
 	// Detects conflicts on right diagonal
 	public int getDiagonalConflictsRight(int row, int column) {
 		int count = 0;
 
 		while (row <= (k - 1) && column >= 0) {
 			if (state[row][column] == 1)
 				count++;
 			row++;
 			column--;
 		}
 
 		return Math.max((count - 1), 0);
 	}
 
 	// Returns a value for the entire state
 	public double getStateValue() {
 		// Calculates a value for the state
 		double value = 1 - (double) getConflicts() / (2 * k - 3);
 		return Math.max(0, value);
 	}
 
 	// Generates n neighbours
 	public void generateNeighbours(int n) {
		
		/*
		 * BUG: overkjrer this.state
		 * */
 		// Initiate array
 		neighbours = new State[n];
 		
 		// Temporary arrays
 		int[][] newState;
 		int[] newQueen;
 		
 		// Generates n number of neighbours
 		for (int i = 0; i < n; i++) {
 
 			// Initiate values for new States
 			newState = this.state;
 			newQueen = this.queen;
 
 			// Collect random row
 			int rowNumber = (int) (Math.random() * k);
 
 			// Find queen index
 			int indexOfQ = newQueen[rowNumber];
 			System.out.println("row: " + rowNumber + " index of q: " + indexOfQ);
 
 			// Move Queen +1 to a random direction
 			if (indexOfQ == (k - 1)) {
 				// Must go left
 				newQueen[rowNumber] = newQueen[rowNumber] - 1;
 				newState[rowNumber][indexOfQ] = 0;
 				newState[rowNumber][indexOfQ - 1] = 1;
 
 			} else if (indexOfQ == 0) {
 				// Must go right
 				newQueen[rowNumber] = newQueen[rowNumber] + 1;
 				newState[rowNumber][indexOfQ] = 0;
 				newState[rowNumber][indexOfQ + 1] = 1;
 
 			} else if ((int) (Math.random() * 2) == 0) {
 				// Random left
 				newQueen[rowNumber] = newQueen[rowNumber] - 1;
 				newState[rowNumber][indexOfQ] = 0;
 				newState[rowNumber][indexOfQ - 1] = 1;
 
 			} else {
 				// Random right
 				newQueen[rowNumber] = newQueen[rowNumber] + 1;
 				newState[rowNumber][indexOfQ] = 0;
 				newState[rowNumber][indexOfQ + 1] = 1;
 			}
 
 			// Create neighbour
 			neighbours[i] = new KQueenState2(k, newState, newQueen);
 			neighbours[i].printState();
 			System.out.println("Initial state");
 			printState();
 		}
 	}
 
 	// Returns the best neighbour
 	public State getBestNeighbour() {
 		double bestValue = 0;
 		int bestValueIndex = 0;
 
 		for (int i = 0; i < neighbours.length; i++) {
 			double value = neighbours[i].getStateValue();
 			if (value > bestValue) {
 				bestValueIndex = i;
 				bestValue = value;
 			}
 		}
 		return neighbours[bestValueIndex];
 	}
 
 	// Returns the best neighbour
 	public State getRandomNeighbour() {
 		int index = (int) (Math.random() * neighbours.length);
 		return neighbours[index];
 	}
 }
