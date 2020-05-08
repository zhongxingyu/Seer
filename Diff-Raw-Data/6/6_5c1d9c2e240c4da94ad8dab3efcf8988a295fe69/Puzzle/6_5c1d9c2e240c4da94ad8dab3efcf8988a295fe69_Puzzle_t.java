 import java.io.BufferedWriter;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.util.NoSuchElementException;
 import java.util.Scanner;
 
 public class Puzzle {
 
 	private Position[][] sudokuGrid;
 	private final int gridSize;
 	public final int DUMMY;
 	private String puzzleIdentifier;
 	private String puzzleID;
 	
 	public Puzzle() 
 	{
 		gridSize = 9;
 		DUMMY = 0;
 		sudokuGrid = new Position[gridSize][gridSize];
 		
 		Position dummyPosition = null;
 		
 		int row = 0;
 		int column = 0;
 		
 		while (column != gridSize) 
 		{
 			while (row != gridSize) 
 			{
 				dummyPosition = new Position (row, column, DUMMY, true);
 				sudokuGrid[row][column] = dummyPosition;
 				row++;
 			}
 			row = 0;
 			column++;
 		}
 	}
 	
 	//Adds a position
 	public void addPosition(int x, int y, int val)
 	{
 		if (isLegal(x) && isLegal(y) && isLegalVal(val)) 
 		{
 			Position temp = sudokuGrid[x][y];
 			
 			if (temp.canChange()) 
 			{
 				Position p = new Position (x, y, val, true);
 				sudokuGrid[x][y] = p;
 			}
 		} 
 		else 
 		{
 			System.out.println("Invalid coordinates or value");
 		}
 		
 	}
 	
 	//Makes all position that aren't dummies set
 	public void makePositionsSet()
 	{
 		int row = 0;
 		int column = 0;
 		
 		while (column != gridSize)
 		{
 			
 			while (row != gridSize) 
 			{
 				Position temp = sudokuGrid[row][column];
 				if (temp.getValue() != DUMMY) 
 				{
 					changeSetToFalse(row, column);
 					//Position setPosition = new Position(row, column, temp.getValue(), false);
 					//sudokuGrid[row][column] = setPosition;
 				}
 				row++;
 			}
 			
 			row = 0;
 			column++;
 		}
 	}
 	
 	//Clears a position
 	public void clearPosition (int x, int y) 
 	{
 		if (isLegal(x) && isLegal(y)) 
 		{
 			
 			Position temp = sudokuGrid[x][y];
 			
 			if (temp.canChange()) 
 			{
 				Position dummy = new Position (x, y, DUMMY, true);
 				sudokuGrid[x][y] = dummy;
 			}
 		} 
 		else 
 		{
 			System.out.println("Invalid coordinates");
 		}
 		
 	}
 	
 	//Clears the grid
 	public void reset()
 	{
 		int row = 0;
 		int column = 0;
 		
 		while (column != gridSize)
 		{
 			
 			while (row != gridSize) 
 			{
 				Position dummy = new Position (row, column, DUMMY, true);
 				sudokuGrid[row][column] = dummy;
 				row++;
 			}
 			
 			row = 0;
 			column++;
 		}
 	}
 	
 	//Gives the value at a position
 	public int getValueAtPosition (int x, int y) 
 	{
 		int value = 0;
 		
 		if (isLegal(x) && isLegal(y))
 		{
 			value = sudokuGrid[x][y].getValue();
 		} 
 		else 
 		{
 			System.out.println("Invalid coordinates");
 		}
 		
 		return value;
 	}
 	
 	//Clears all positions that haven't been set
 	public void clearUnsetPositions()
 	{
 		int row = 0;
 		int column = 0;
 		
 		while (column != gridSize)
 		{
 			
 			while (row != gridSize) 
 			{
 				
 				Position temp = null;
 				
 				temp = sudokuGrid[row][column];
 				
 				if (!temp.canChange()) 
 				{
 					//do nothing
 				} 
 				else 
 				{
 					Position dummy = new Position (row, column, DUMMY, true);
 					sudokuGrid[row][column] = dummy;
 				}
 				row++;
 			}
 			
 			row = 0;
 			column++;
 		}
 	}
 	
 	//Sets the puzzle name
 	public void setPuzzleName(String name) 
 	{
 		puzzleIdentifier = name;
 	}
 	
 	//Sets the ID
 	public void setID(String x) 
 	{
 		puzzleID = x;
 	}
 	
 	//Changes the value at that position
 	public void changeValueAtPosition (int x, int y, int val) 
 	{
 		Position temp = sudokuGrid[x][y];
 		
 		if (isLegal(x) && isLegal(y) && isLegalVal(val) && temp.canChange()) 
 		{
 			temp.changeVal(val);
 			
 			sudokuGrid[x][y] = temp;
 		}
 		
 	}
 	
 	//Returns ID
 	public String getID() 
 	{
 		return puzzleID;
 	}
 	
 	//Returns the position
 	public Position getPosition(int x, int y) 
 	{
 		return sudokuGrid[x][y];
 	}
 	
 	//Not sure how rondo will implement this
 	public Position giveHint() 
 	{
 		Position temp = null;
 		
 		return temp;
 	}
 	
 	//Saves the puzzle
 	public void savePuzzle() 
 	{
 		try 
 		{
 			File savedPuzzle = new File("sudoku/userPuzzles/" + puzzleIdentifier + ".txt");
 		    FileOutputStream fo = new FileOutputStream(savedPuzzle);
 		    OutputStreamWriter osw = new OutputStreamWriter(fo);    
 		    BufferedWriter w = new BufferedWriter(osw);
 		    
 		    int column = 0;
 		    int row = 0;
 		    
 		    w.write(puzzleID);
 		    w.newLine();
 		    while (column != gridSize) 
 		    {
 		    	while (row != gridSize) 
 		    	{
 		    		int val = getValueAtPosition(row, column); 
 		    		w.write(val + " ");
 		    		row++;
 		    	}
 		    	row = 0;
 		    	w.newLine();
 		    	column++;
 		    }
 		    w.close();
 		} 
 		catch (IOException e) 
 		{
 		    System.out.println("Problem has occured");
 		}
 		
 		savePuzzleSetPositions();
 	}
 	
 	//Saves if the position can be changed
 	private void savePuzzleSetPositions() 
 	{
 		
 		try 
 		{
 			File savedPuzzle = new File("sudoku/userPuzzles/" + puzzleIdentifier + "Position.txt");
 		    FileOutputStream fo = new FileOutputStream(savedPuzzle);
 		    OutputStreamWriter osw = new OutputStreamWriter(fo);    
 		    BufferedWriter w = new BufferedWriter(osw);
 		    
 		    int column = 0;
 		    int row = 0;
 		    
 		    w.write(puzzleID);
 		    w.newLine();
 		    while (column != gridSize) 
 		    {
 		    	while (row != gridSize) 
 		    	{
 		    		boolean canSet = canBeChanged(row, column);
 		    		int val = 0;
 		    		if (canSet) 
 		    		{
 		    			val = 1;
 		    		}
 		    		w.write(val + " ");
 		    		row++;
 		    	}
 		    	row = 0;
 		    	w.newLine();
 		    	column++;
 		    }
 		    w.close();
 		} catch (IOException e) {
 		    System.out.println("Problem has occured");
 		}
 	}
 	
 	//Loads the puzzle
 	public void loadPuzzle(String filename) 
 	{
 		String s = null;
 		boolean savedPositions = false;
 		
 		if (puzzleIdentifier == null) 
 		{
 			s = "sudoku/";
 		} 
 		else 
 		{
 			s = "sudoku/userPuzzles/";
 			savedPositions = true;
 		}
 		
 		try
 		{
 			//System.out.println(filename);
 			Scanner sc = new Scanner(new FileReader(s + filename + ".txt")); 
 			//System.out.println("hi");
 			readPuzzle(sc);
 			
 			sc.close();
 			
 			if (savedPositions) 
 			{
 				sc = new Scanner(new FileReader(s + filename + "Position.txt")); 
 				readPuzzlePosition(sc);
 				sc.close();
 			}
 			
 		}
 		
 		catch (NoSuchElementException n) 
 		{
 			
 		}
 		
 		catch (FileNotFoundException e) 
 		{
 			System.out.println("File not found!");
 		}
 		
 	}
 
 	//Reads the puzzle for the values
 	private void readPuzzle(Scanner sc) 
 	{
 		int row = 0;
 		int column = 0;
 		
 		String id = sc.next();
 		//System.out.println("id is " + id);
 		puzzleID = id;
 		while (column != gridSize) 
 		{
 			while (row != gridSize) 
 			{
 				//System.out.println("hi");
 				//String stemp = sc.next();
 				int val = sc.nextInt();
 				//System.out.println("hi");
 				if (val != DUMMY) 
 				{
 					this.addPosition(row, column, val);
 				}
 				
 				row++;
 			}
 			row = 0;
 			column++;
 		}
 
 		//System.out.println("completed");
 	}
 	
 	//Read if positions can be changed
 	private void readPuzzlePosition(Scanner sc) 
 	{
 		int row = 0;
 		int column = 0;
 		
 		String id = sc.next();
 		puzzleID = id;
 		while (column != gridSize) 
 		{
 			while (row != gridSize) 
 			{
 				//System.out.println("hi");
 				//String stemp = sc.next();
 				int val = sc.nextInt();
 				
 				//val = 0 means false
 				if (val == 0) 
 				{
 					changeSetToFalse(row, column);
 				}
 
 				row++;
 			}
 			row = 0;
 			column++;
 		}
 
 		//System.out.println("completed");
 	}
 	
 	//Checks if puzzle is solved
 	public boolean checkPuzzle() 
 	{
 		Solver check = new Solver();
 		boolean answer = false;
 		
 		answer = check.solved(this);
 		
 		return answer;
 	}
 
 	//solves puzzle
 	public Puzzle solvePuzzle() 
 	{
 		DSolve solve = new DSolve(this);
 		Puzzle solution = null;
 		
 		if (solve.didNotSolve() == true)
 		{
 			String ld = "sudoku/userPuzzles/";
			//puzzleIdentifier = "testingSol";
 			String filename = puzzleIdentifier;
 			
 			this.savePuzzleSetPositions();
 			
 			Scanner sc = null;
 			
 			try 
 			{
 				sc = new Scanner(new FileReader(ld + filename + "Position.txt"));
 				
 				char s = this.getID().charAt(0);
 				int x = s - 'A' + 1;
 				
 				solution = new Puzzle();
				//solution.setPuzzleName(name);
 				solution.loadPuzzle("solution" + x);
 				
				solution.readPuzzlePosition(sc);
 				sc.close();
 			} 
 			catch (FileNotFoundException e) 
 			{
 				
 			} 
 			
 		}
 		else
 		{
 			solution = this;
 		}
 		
 		return solution;
 	}
 	
 	//Makes the value to can't be changed
 	private void changeSetToFalse(int x, int y)
 	{
 		if (isLegal(x) && isLegal(y)) 
 		{
 			Position temp = sudokuGrid[x][y];
 			temp.changeSet(false);
 			sudokuGrid[x][y] = temp;
 		}
 	}
 	
 	//Checks if a position can be changed or not
 	public boolean canBeChanged(int x, int y)
 	{
 		boolean answer = false;
 		Position temp = sudokuGrid[x][y];
 		
 		if (isLegal(x) && isLegal(y) && temp.canChange()) 
 		{
 			answer = true;
 		}
 		
 		return answer;
 	}
 	
 	//Checks if coordinate is legal
 	private boolean isLegal(int x) 
 	{
 		boolean answer = false;
 		
 		if (x < gridSize && x >= 0) 
 		{
 			answer = true;
 		}
 		
 		return answer;
 	}
 	
 	//Checks if the value is legal
 	private boolean isLegalVal(int x) 
 	{
 		boolean answer = false;
 		
 		if (x > 0 && x < 10) {
 			answer = true;
 		}
 		
 		return answer;
 	}
 }
