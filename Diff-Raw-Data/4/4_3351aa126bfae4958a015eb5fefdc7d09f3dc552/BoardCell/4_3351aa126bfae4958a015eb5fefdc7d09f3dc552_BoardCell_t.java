 /**
  * Class to store and access values associated with an individual
  *  cell in the grid
  * @author Hayden, Laura, Jerome, Steven
  * @version 0.1
  * 
  */
 public class BoardCell {
 
 	/**
 	 * 
 	 * @param boardSize
 	 */
 	public BoardCell(int boardSize)
 	{		
 		this.boardSize = boardSize;
		this.tempNumbers = new boolean[boardSize];
 		for (int i = 0; i < boardSize; i++)
 		{
			this.tempNumbers[i] = false;
 		}
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public int getFinalValue()
 	{
 		return valueFinal;
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public int getInputValue()
 	{
 		return valueInput;
 	}
 		
 	/**
 	 * 
 	 * @param number
 	 */
 	public void setFinalValue(int number)
 	{
 		valueFinal = number;
 	}
 	
 	/**
 	 * 
 	 * @param number
 	 */
 	public void setInputValue(int number)
 	{
 		valueInput = number;
 	}
 	
 	/**
 	 * 
 	 */
 	public void removeInputValue()
 	{
 		this.valueInput = -1;
 	}
 	
 	/**
 	 * 
 	 */
 	public void removeFinalValue()
 	{
 		this.valueFinal = -1;
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public boolean isCurrentlyVisible()
 	{
 		return this.isCurrentlyVisible;
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public boolean isInitiallyVisible()
 	{
 		return this.isInitiallyVisible;
 	}
 
 	/**
 	 * 
 	 * @param isVisible
 	 */
 	public void setCurrentlyVisible(boolean isVisible)
 	{
 		this.isCurrentlyVisible = isVisible;
 	}
 	
 	/**
 	 * 
 	 * @param isVisible
 	 */
 	public void setInitiallyVisible(boolean isVisible)
 	{
 		this.isInitiallyVisible = isVisible;
 	}
 	
 	/**
 	 * 
 	 * @param number
 	 * @return
 	 */
 	public boolean issetTemp(int number)
 	{
 		return tempNumbers[number - 1];
 	}
 
 	/**
 	 * 
 	 * @param number
 	 * @param isSet
 	 */
 	public void setTemp(int number, boolean isSet)
 	{
 		tempNumbers[number - 1] = isSet;
 	}
 	
 	/**
 	 * 
 	 */
 	public void reset()
 	{
 		this.valueFinal = -1;
 		this.valueInput = -1;
 		this.isCurrentlyVisible = false;
 		this.isInitiallyVisible = false;
 		for (int i = 0; i < boardSize; i++)
 		{
 			tempNumbers[i] = false;
 		}
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public boolean isCorrect()
 	{
 		if (valueInput == valueFinal)
 		{
 			return true;
 		}
 		return false;
 	}
 	
 	private int boardSize;
 	private int valueFinal;
 	private int valueInput;
 	private boolean isInitiallyVisible;
 	private boolean isCurrentlyVisible;
 	private boolean tempNumbers[];
 	
 }
