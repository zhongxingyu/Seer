 package test;
 
 import GameObjects.Board;
 import GameObjects.Piece;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 /**
  *
  * @author RHsu
  */
 public class TestBoard extends Board
 {
 	/**
 	 * Just calls the base constructor
 	 */
 	TestBoard()
 	{
 		super();
 	}
     
 	/**
 	 * Creates a board that looks like this 1 2 3 4 5 6 7
 	 * @return a board of consecutive values
 	 */
 	static TestBoard getConsecutiveBoard()
 	{
 		TestBoard b = new TestBoard();
 		for(int i = 0; i < 7; i++)
 		{
 			b.pieceAt(6, i).setValue(i + 1);
 		}
 		return b;
 	}
 	
 	/**
 	 * Creates a board that is empty
 	 * @return an empty board
 	 */
 	static TestBoard getEmptyBoard()
 	{
 		TestBoard b = new TestBoard();
 		for(int i = 0; i < 7; i++)
 		{
 			b.pieceAt(6, i).setType(Piece.Type.EMPTY);
 		}
 		return b;
 	}
 	
 	/**
 	 * This test was used to create a board but with a couple of values changed
 	 * @return a board
 	 */
 	static TestBoard getTest1()
 	{
 		TestBoard b = new TestBoard();
 		for(int i = 0; i < 7; i++)
 		{
 			b.pieceAt(6, i).setValue(i + 1);
 		}
                 
 		//set 5 - 6 to 3-SET
 		b.pieceAt(5, 6).setValue(3);
             
 		//set 6 - 0 to empty
 		b.pieceAt(6,0).setType(Piece.Type.EMPTY);
                 
                 
 		//set 6 - 2 to empty
 		b.pieceAt(6,2).setType(Piece.Type.EMPTY);
 		return b;
 	}
 	
 	/**
 	 * This test was used to create a board of all 2s
 	 * @return 
 	 */
 	static TestBoard getTest2()
 	{
 		TestBoard b = new TestBoard();
 		for (int i = 0; i< 7; i++)
 		{
 			b.pieceAt(6,i).setValue(2);
 		}
 		return b;
 	}
 
 	/**
 	 * An extender test method for board
 	 * Takes a given piece and performs a check on the row
 	 * Flags all pieces in the row if they should be removed.
 	 * @param p the piece to perform the check on
 	 * @param debugMode whether or not to print
 	 */
 	void testCheck(Piece p, boolean debugMode)
 	{
 		ArrayList<Piece> rows = getAllInRow(p);
 		ArrayList<Piece> columns = getAllInColumn(p);
 		
 		if(debugMode)
 		{	
 			System.out.println("rows is : " + rows);
 			System.out.println("columns is : " + columns);
 		}
 			
 		for(Piece item : rows)
 		{
 			int value = item.getValue();
 			int column = getColumnAdjacent(item);
 			int row = getRowAdjacent(item);
 
 			if((value == column) || (value == row))
 			{
 				item.setRemove(true);
 			}
 			
 			if(debugMode)
 			{
 				System.out.println("This is item: " + item);	
 				System.out.println("The value is: " + value);	
 				System.out.println("The column adjacent is " + column);
 				System.out.println("The row adjacent is: " + row);
 			}
 		}	
 	}
 	
 	/**
 	 * Old main. This was used to test the method checkForRemoval on a piece
 	 */
 	static void m1()
 	{
 		TestBoard b = TestBoard.getTest1();
 		System.out.println(b);
 		
 		Piece p = b.pieceAt(6, 3);
 		b.testCheck(p, false);
 		
 		System.out.println("Remove is: " + b.getAllRemove());
 		
 		b.checkForRemoval(p);
 		System.out.println(b);
 		System.out.println("Calling remove");
 		b.removeMarked();	
 		System.out.println(b);
 	}
 	
 	/**
 	 * Old main was used to test a bug with the insert
 	 */
 	static void m2()
 	{
 		//create a board
 		TestBoard b = TestBoard.getTest1();
 		
 		System.out.println(b);
 		
 		//insert at position 2 a value of 1
 		/*
 		 * Looks like this
 		 * 
 		 * 1
 		 * 2
 		 */
 		
 		b.insert(2, 1);
 		b.removeMarked();
 		
 		System.out.println(b);
 		
 		b.insert(2,1);
 		
 		System.out.println(b);
 		
 		ArrayList<Piece> ListOfRemovedPieces = b.getAllRemove();
 		
 		System.out.println(ListOfRemovedPieces);
 	}
 	
 	/**
 	 * Old main was used to test a bug with the insert
 	 */
 	static void m3()
 	{
 		Scanner input = new Scanner(System.in);
 		TestBoard b = TestBoard.getEmptyBoard();
 		System.out.println(b);
 		
 		while(true)
 		{
 			input.nextLine();
 			b.insert(1, 3);
 			System.out.println("Inserted completed...");
 			ArrayList<Piece> test = b.getAllRemove();
 			System.out.println("The size of the list of removed is " +  test.size());
 			System.out.println(b);
 		}
 	}
 	
 	public static void main(String[] args)
 	{
		System.out.println("Printing empty board");
		TestBoard b = getEmptyBoard();
		System.out.println(b);
 	}
 }
