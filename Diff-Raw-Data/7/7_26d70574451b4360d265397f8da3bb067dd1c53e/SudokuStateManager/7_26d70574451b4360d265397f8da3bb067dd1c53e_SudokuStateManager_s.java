 package problemSolver.statemanager;
 
 import java.util.ArrayList;
 
 import problemSolver.algorithms.SimulatedAnnealing;
 
 public class SudokuStateManager extends StateManager {
 
 	public int size;
 	public int bulkSize;
 	public int[] conflicts;
 	public int sumConflicts;
 
 	public SudokuStateManager(int[][] puzzle) {
 		size = puzzle.length;
 		bulkSize = (int) Math.sqrt(puzzle.length);
 		setValueConstrains(1, size);
 		setValuesSize(size * size);
 
 		this.constrainedIndexes = new boolean[values.length];
 
 		for (int i = 0; i < size; i++) {
 			for (int j = 0; j < size; j++) {
 				values[i * size + j] = puzzle[i][j];
 				if (puzzle[i][j] != 0)
 					constrainedIndexes[i * size + j] = true;
 			}
 		}
 
 		// Initiate fields
 		conflicts = new int[values.length];
 		sumConflicts = 0;
 	}
 
 	public void convertToList(int[][] puzzel) {
 
 	}
 
 	public void swap() {
 
 	}
 
 	public double getStateValue() {
 		calculateConflicts();
		//System.out.println(sumConflicts > size * size * size);
		return 1.0 - (double) (sumConflicts / size * size * size);
 	}
 
 	public void printState() {
 		System.out.println("Map");
 		for (int i = 0; i < size; i++) {
 			for (int j = 0; j < size; j++) {
 				System.out.print(values[i * size + j] + " ");
 			}
 			System.out.println();
 		}
 		System.out.println();
 		
 		System.out.println("Score:"+getStateValue());
 	}
 
 	public void printFixed() {
 		System.out.println("Fixed values");
 		for (int i = 0; i < size; i++) {
 			for (int j = 0; j < size; j++) {
 				if (constrainedIndexes[i * size + j])
 					System.out.print("1 ");
 				else
 					System.out.print("0 ");
 			}
 			System.out.println();
 		}
 		System.out.println();
 	}
 
 	public void printConflicts() {
 		System.out.println("Conflicts");
 		for (int i = 0; i < size; i++) {
 			for (int j = 0; j < size; j++) {
 				System.out.print(conflicts[i * size + j] + " ");
 			}
 			System.out.println();
 		}
 		System.out.println();
 	}
 
 	public void initState() {
 		for (int i = 0; i < values.length; i++) {
 			if (!constrainedIndexes[i]) values[i] = getRandomConstrained();
 		}
 	}
 
 	private void calculateConflicts() {
 		sumConflicts = 0;
 		for (int i = 0; i < values.length; i++) {
 			conflicts[i] = 0;
 			int radIndex = (int) Math.floor(i / size);
 			int colIndex = i % size;
 
 			for (int k = radIndex * size, to = radIndex * size + size; k < to; k++) {
 				if (k != i && values[i] == values[k])
 					conflicts[i]++;
 			}
 
 			for (int j = colIndex; j < values.length; j += size) {
 				if (j != i && values[i] == values[j])
 					conflicts[i]++;
 			}
 
 			int startIndex = i - (radIndex % bulkSize) * size - colIndex
 					% bulkSize, bulkLength = startIndex + size * (bulkSize - 1)
 					+ bulkSize;
 			for (int k = startIndex; k < bulkLength; k += size) {
 				for (int k2 = 0; k2 < bulkSize; k2++) {
 					int index = k + k2;
 					if (i != index && values[i] == values[index])
 						conflicts[i]++;
 				}
 			}
 			
 			sumConflicts += conflicts[i];
 		}
 	}
 
 	public static void main(String[] args) {
 
 		int[][] puzzle1 = { { 4, 3, 0, 7, 0, 0, 0, 9, 0 },
 				{ 8, 0, 1, 0, 0, 0, 0, 0, 4 }, { 0, 0, 5, 2, 4, 8, 3, 0, 0 },
 				{ 6, 0, 0, 5, 0, 0, 9, 7, 0 }, { 0, 8, 4, 0, 0, 0, 6, 3, 0 },
 				{ 0, 9, 7, 0, 0, 3, 0, 0, 5 }, { 0, 0, 8, 6, 2, 9, 1, 0, 0 },
 				{ 1, 0, 0, 0, 0, 0, 5, 0, 9 }, { 0, 2, 0, 0, 0, 1, 0, 6, 8 } };
 		
 		SudokuStateManager sudoku = new SudokuStateManager(puzzle1);
 		sudoku.initState();
 		sudoku.printState();
 		sudoku.printFixed();
 		sudoku.calculateConflicts();
 		sudoku.printConflicts();
		SimulatedAnnealing sa = new SimulatedAnnealing(sudoku, 10000, 0.1, 1.0, 50);
 		sa.run();
 		sudoku.calculateConflicts();
 	}
 
 }
