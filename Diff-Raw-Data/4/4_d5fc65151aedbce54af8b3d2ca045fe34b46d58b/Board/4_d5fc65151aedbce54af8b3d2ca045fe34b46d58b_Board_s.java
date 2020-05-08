 package main;
 /**
  * @author boaz, September 2010
  * @version 1.0
  */
 public class Board {
 
 	/**
 	 * Board game - private array of chars
 	 */
 	private char[] locate;
 	
 	/**
 	 * Constructor for class Board - only initialize locate array
 	 */
 	public Board()
 	{
 		locate = new char[9];
 		locate[0]='1';
 		locate[1]='2';
 		locate[2]='3';
 		locate[3]='4';
 		locate[4]='5';
 		locate[5]='6';
 		locate[6]='7';
 		locate[7]='8';
 		locate[8]='9';
 	}
 	
 	/**
 	 * This function just print the board to standard output.
 	 * 
 	 */
 	public void PrintBoard()
 	{
 		System.out.println(locate[0] + "|" + locate[1] + "|" + locate[2]);
 		System.out.println("-----");
 		System.out.println(locate[3] + "|" + locate[4] + "|" + locate[5]);
 		System.out.println("-----");
 		System.out.println(locate[6] + "|" + locate[7] + "|" + locate[8]);
 	}
 	
 	/**
 	 * 
 	 * @param i - The locate that we want to get.
 	 * @return The value in locate i.
 	 */
 	public char getLocateValue(int i)
 	{
 		return locate[i];
 	}
 	
 	/**
 	 * 
 	 * @param i - The locate that we want to change
 	 * @param value - Value to set to locate i
 	 */
 	public void setLocateValue(int i, char value)
 	{
 		locate[i] = value;
 	}
 	
 	/**
 	 * 
 	 * @return true if have a winner in this board, false otherwise.
 	 */
 	public boolean isWin()
 	{
 		if ( (locate[0] == locate[1] && locate[1] == locate[2]) || //0-1-2 (first row)
 				(locate[3] == locate[4] && locate[4] == locate[5]) || //3-4-5 (second row)
 				(locate[6] == locate[7] && locate[7] == locate[8]) || //6-7-8 (third row)
 				(locate[0] == locate[3] && locate[3] == locate[6]) || //0-3-6 (left column)
 				(locate[1] == locate[4] && locate[4] == locate[7]) || //1-4-7 (center column)
 				(locate[2] == locate[5] && locate[5] == locate[8]) || //2-5-8 (right column)
				(locate[0] == locate[1] && locate[1] == locate[2]) || //0-4-8 (left-up diagonal)
				(locate[0] == locate[1] && locate[1] == locate[2]) ) //2-4-6 (left-down diagonal)
 			return true;
 		else
 			return false;
 			
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public int[] getEmpty() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
