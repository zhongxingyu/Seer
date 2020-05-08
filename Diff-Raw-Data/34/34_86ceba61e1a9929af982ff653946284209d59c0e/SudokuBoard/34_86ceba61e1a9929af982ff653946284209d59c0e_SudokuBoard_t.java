 import java.util.ArrayList;
 import java.util.Random;
 
 /**
 * Class that allows creation, modification and access to
 *  a Sudoku board
 * @author Hayden Smith, Laura Hodges, Jerome Bird, Steven Falconieri
 * @version 2.2
 * */
 public class SudokuBoard implements Board {
 
 	/**
 	 * 
 	 * @param size
 	 * @param difficulty
 	 */
 	public SudokuBoard(int size)
 	{	
 		this.boardSize = size;
 		board = new ArrayList<ArrayList<BoardCell>>();
 		
 		for (int i = 0; i < boardSize; i++)
 		{
 			board.add(new ArrayList<BoardCell>());
 			ArrayList<BoardCell> row = board.get(i);
 			for (int j = 0; j < boardSize; j++)
 			{ 
 				row.add(new BoardCell(boardSize));
 			}
 		}
 	}
 	
 	/**
 	 * Generate a new board
 	 * @param difficulty The difficulty level the board
 	 *  should be generated with
 	 */
 	public void generate(int difficulty)
 	{
 		clear();
 		
 		this.currentlyGenerating = true;
 		this.difficulty = difficulty;
 		
 		/*
 		// Debugging
 		Random n = new Random();
 		for (int i = 1; i <= 9; i++)
 		{
 			for (int j = 1; j <= 9; j++)
 			{
 				setCellValue(i, j, n.nextInt(9) + 1);
 				int l = n.nextInt(2);
 				System.out.println(l);
 				if (l == 1) setIfInitiallySet(i, j, true);
 				if (l == 0) setIfInitiallySet(i, j, false);
 			}
 		}*/
 		
 		BoardFiller filler = new BoardFiller(this);
 		Removal r = new Removal(this);
 		filler.fillBoard();
 		
 		r.removeValues(new SimpleRemover(this));
 		r.removeValues(new HardRemover(this));
		r.removeValues(new ExhaustiveRemover(this));
 
 
 		this.currentlyGenerating = false;
 	}
 	
 	/**
 	 * Remove the value stored in a cell
 	 * @param row Row cell is in
 	 * @param col Column cell is in
 	 */
 	public void removeCellValue(int row, int col)
 	{
 		if (currentlyGenerating)
 		{
 			getCell(row, col).removeCorrectValue();
 		}
 		else
 		{
 			getCell(row, col).removeInputValue();
 			for (int i = 1; i <= boardSize; i++)
 			{
 				setCellDraftVisibility(row, col, i, false);
 			}
 		}
 	}
 	
 	/**
 	 * Get the value that is currently held within a particular cell
 	 * @param row Row cell is in
 	 * @param col Column cell is in
 	 * @return Value currently held in the cell
 	 */
 	public int getCellValue(int row, int col)
 	{
 		if (currentlyGenerating || isEmptyCell(row, col))
 		{
 			return getCell(row, col).getCorrectValue();
 		}
 		else
 		{
 			return getCell(row, col).getInputValue();
 		}
 	}
 	
 	/**
 	 * Set the value that is currently held within a particular cell
 	 * @param row Row cell is in 
 	 * @param col Column cell is in
 	 * @param number Value currently held in the cell
 	 */
 	public void setCellValue(int row, int col, int number)
 	{
 		if (currentlyGenerating)
 		{
 			getCell(row, col).setCorrectValue(number);
 		}
 		else
 		{
 			getCell(row, col).setInputValue(number);
 			for (int i = 1; i <= boardSize; i++)
 			{
 				setCellDraftVisibility(row, col, i, false);
 			}
 			
 		}
 	}
 	
 	/**
 	 * Determine whether a cell is currently visible for the current 
 	 *  state of the board
 	 * @param row Row cell is in
 	 * @param col Column cell is in
 	 * @return Whether the cell is currently visible at the current
 	 *  particular state of the board
 	 */
 	public boolean hasInput(int row, int col)
 	
 	{
 		//return getCell(row, col).isCurrentlyVisible();
 		return getCell(row, col).getInputValue()!=-1;
 	}
 	
 	public boolean hasDrafts(int row, int col)
 	{
 		for (int i = 1; i <= boardSize; i++)
 		{
 			if (isVisibleCellDraft(row, col, i))
 			{
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * Determine whether a cell is visible at the initial state 
 	 *  of the board
 	 * @param row Row cell is in
 	 * @param col Column cell is in
 	 * @return Whether the cell is visible at the initial state
 	 *  of the board
 	 */
 	public boolean isInitiallySet(int row, int col)
 	{
 		return getCell(row, col).isInitiallyVisible();
 	}
 	/**
 	 * Set the visiblity of a cell on the board
 	 * @param row Row cell is in
 	 * @param col Column cell is in
 	 * @param visiblity Whether the particular cell is visible or not
 	 */
 	
 	public void setIfInitiallySet(int row, int col, boolean visibility)
 	{
 		if (currentlyGenerating)
 		{
 			getCell(row, col).setInitiallyVisible(visibility);
 		}
 		getCell(row, col).setCurrentlyVisible(visibility);
 	}
 	
 	/**
 	 * Check if a given cell (for row, column) is empty
 	 * @param row Row cell is in
 	 * @param col Column cell is in
 	 * @return Whether a given cell (for row, column) is empty
 	 */
 	public boolean isEmptyCell(int row, int col)
 	{
 		if (currentlyGenerating)
 		{
 			return getCell(row, col).isEmptyCorrect();
 		}
 		else
 		{
 			return getCell(row, col).isEmptyInput();
 		}
 	}
 			
 	/**
 	 * Check is a particular cell value's temporary value for a 
 	 *  particular number is visible
 	 * @param row Row cell is in
 	 * @param col Column Cell is in
 	 * @param number Number of the temporary value that needs to
 	 *  be checked if is visible
 	 * @return Whether a particular cell value's temporary value for
 	 *  a particular number is visible
 	 */
 	public boolean isVisibleCellDraft(int row, int col, int number)
 	{
 		return getCell(row, col).issetDraft(number);
 	}
 	
 	/**
 	 * Make a particular cell value's temporary value for a 
 	 *  particular number either visible or not visible
 	 * @param row Row cell is in
 	 * @param col Column Cell is in
 	 * @param number Number of the temporary value that needs to
 	 *  have it's visiblity changed
 	 * @param isSet Value to denote whether to have the cell visible
 	 *  or not visible
 	 */
 	public void setCellDraftVisibility(int row, int col, int number, boolean isSet)
 	{
 		getCell(row, col).setDraft(number, isSet);
 		if (isSet)
 		{
 			removeCellValue(row, col);
 		}
 	}
 			
 	/**
 	 * Return whether the board has been correctly filled out
 	 * @return Whether the board has been correctly filled out
 	 */
 	public boolean isCorrectBoard()
 	{
 		boolean allCorrect = true;
 		for (int i = 1; i <= boardSize; i++)
 		{
 			for (int j = 1; i <= boardSize; j++)
 			{
 				if (!isCorrectCell(i, j))
 				{
 					allCorrect = false;
 				}
 			}
 		}
 		return allCorrect;
 	}
 	
 	/**
 	 * Return whether a particular cell in the board has been
 	 *  correctly filled out 
 	 * @param row Row cell is in
 	 * @param col Column cell is in
 	 * @return Whether a particular cell in the board has been correctly
 	 *  filled out
 	 */
 	public boolean isCorrectCell(int row, int col)
 	{
 		return getCell(row, col).isCorrect();
 	}
 	
 	/**
 	 * Return the difficulty constant for "Easy"
 	 * @return Difficulty constant for "Easy"
 	 */
 	public boolean isDifficultyEasy()
 	{
 		if (this.difficulty == DIFFICULTY_EASY)
 		{
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Return the difficulty constant for "Medium"
 	 * @return Difficulty constant for "Medium"
 	 */
 	public boolean isDifficultyMedium()
 	{
 		if (this.difficulty == DIFFICULTY_MEDIUM)
 		{
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Return the difficulty constant for "Hard"
 	 * @return Difficulty constant for "Hard"
 	 */
 	public boolean isDifficultyHard()
 	{
 		if (this.difficulty == DIFFICULTY_HARD)
 		{
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Get the size of the board
 	 * @return Size of the board
 	 */
 	public int getBoardSize()
 	{
 		return this.boardSize;
 	}
 	
 	/**
 	 * Reset all values in the board
 	 */
 	public void clear()
 	{
 		for (int i = 1; i <= getBoardSize(); i++)
 		{
 			for (int j = 1; j <= getBoardSize(); j++)
 			{
 				clearCell(i, j);				
 			}
 		}
 	}
 	
 	/**
 	 * Reset the board to its original state
 	 *  after generation
 	 */
 	public void restart()
 	{
 		for (int i = 1; i <= getBoardSize(); i++)
 		{
 			for (int j = 1; j <= getBoardSize(); j++)
 			{
 				getCell(i, j).restart();				
 			}
 		}
 	}
 	
 
 	/**
 	 * For a given row determine if a value exists in it
 	 * @param row Row to check if number exists in
 	 * @param value Value to check if exists in row
 	 * @return Whether a value exists in a particular row
 	 */
 	public boolean rowHas(int row, int value)
 	{
 		boolean result = false;
 		for (int i = 1; i <= this.boardSize; i++)
 		{
 			if (getCellValue(row, i) == value)
 			{
 				if(isInitiallySet(row, i)){
 					result = true;
 				}
 			}
 		}
 		return result;
 	}
 	
 	/**
 	 * For a given column determine if a value exists in it
 	 * @param row Column to check if number exists in
 	 * @param value Value to check if exists in column
 	 * @return Whether a value exists in a particular column
 	 */
 	public boolean columnHas(int col, int value)
 	{
 		boolean result = false;
 		for (int i = 1; i <= this.boardSize; i++)
 		{
 			if (getCellValue(i, col) == value)
 			{
 				if(isInitiallySet(i, col)){
 					result = true;
 				}
 			}
 		}
 		return result;
 	}
 	
 	/**
 	 * Determine whether a square contains a particular value
 	 * @param sqr As defined below for a 9x9 board
 	 * 
 	 * 1 1 1 | 2 2 2 | 3 3 3
 	 * 1 1 1 | 2 2 2 | 3 3 3
 	 * 1 1 1 | 2 2 2 | 3 3 3
 	 * ---------------------
 	 * 4 4 4 | 5 5 5 | 6 6 6
 	 * 4 4 4 | 5 5 5 | 6 6 6
 	 * 4 4 4 | 5 5 5 | 6 6 6
 	 * ---------------------
 	 * 7 7 7 | 8 8 8 | 9 9 9
 	 * 7 7 7 | 8 8 8 | 9 9 9
 	 * 7 7 7 | 8 8 8 | 9 9 9
 	 * 
 	 * @param value Value to check if exists in a square
 	 * @return Whether a square contains a particular value
 	 */
 	public boolean squareHas(int sqrId, int value)
 	{
 		boolean result = false;
 		for (int i = 1; i <= this.boardSize; i++)
 		{
 			for (int j = 1; j <= this.boardSize; j++)
 			{
 				if (squareSectionId(i, j) == sqrId)
 				{
 					if (getCellValue(i, j) == value)
 					{
 						if(isInitiallySet(i, j)){
 							result = true;
 						}
 					}
 				}
 			}
 		}
 		return result;
 	}
 	
 	/**
 	 * Return the difficulty of the current board that has
 	 *  been generated
 	 * @return Difficulty of current board that has been generated
 	 */
 	public int getDifficulty()
 	{
 		return this.difficulty;
 	}
 	
 	public void undoLast()
 	{
 		for (int i = 1; i <= 9; i++)
 		{
 			for (int j = 1; j <= 9; j++)
 			{
 				this.setCellValue(i, j, 2);
 			}
 		}
 	}
 	
 	
 	private int squareSectionId(int row, int col)
 	{
 		int sectionId = 0;
 		if (col >= 1 && col <= 3)
 		{
 			sectionId += 1;
 		}
 		else if (col >= 4 && col <= 6)
 		{
 			sectionId += 2;
 		}
 		else if (col >= 7 && col <= 9)
 		{
 			sectionId += 3;
 		}
 		
 		if (row > 3)
 		{
 			sectionId += 3;
 		}
 		if (row > 6)
 		{
 			sectionId += 3;
 		}
 		return sectionId;
 	}
 	
 	/**
 	 * TODO: Fill
 	 * @param row
 	 * @param col
 	 */
 	private void clearCell(int row, int col)
 	{
 		getCell(row, col).clear();
 	}
 	
 	/**
 	 * TODO: Fill
 	 * @param row
 	 * @param col
 	 * @return
 	 */
 	private BoardCell getCell(int row, int col)
 	{
 		return board.get(row - 1).get(col - 1);
 	}	
 	
 	public boolean isCorrectInputForCell(int row, int col, int value)
 	{
 		if (value == getCell(row, col).getCorrectValue())
 		{
 			return true;
 		}
 		return false;
 	}
 
 	public boolean isFilledBoard()
 	{
 		for (int i = 1; i <= boardSize; i++)
 		{
 			for (int j = 1; j <= boardSize; j++)
 			{
 				if (!getCell(i, j).isInitiallyVisible())
 				{
 					if (!isEmptyCell(i, j))
 					{
 						return false;
 					}
 				}
 			}
 		}
 		return true;
 	}
 	
 	private ArrayList<ArrayList<BoardCell>> board;
 	private int boardSize;
 	private int difficulty;
 	public boolean currentlyGenerating;
 	private static final int DIFFICULTY_EASY = 0;
 	private static final int DIFFICULTY_MEDIUM = 1;
 	private static final int DIFFICULTY_HARD = 2;
 	
 	
 }
