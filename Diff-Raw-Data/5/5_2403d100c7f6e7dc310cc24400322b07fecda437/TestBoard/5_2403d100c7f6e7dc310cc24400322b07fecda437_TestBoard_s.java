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
 	public TestBoard()
 	{
 		super();
 	}
     
 	public static TestBoard getConsecutiveBoard()
 	{
 		TestBoard b = new TestBoard();
 		for(int i = 0; i < 7; i++)
 		{
 			b.pieceAt(6, i).setValue(i + 1);
 		}
 		return b;
 	}
 	
 	public static TestBoard getEmptyBoard()
 	{
 		TestBoard b = new TestBoard();
 		for(int i = 0; i < 7; i++)
 		{
 			b.pieceAt(6, i).setType(Piece.Type.EMPTY);
 		}
 		return b;
 	}
 	
 	public static TestBoard getTest1()
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
 	
 	public static TestBoard getTest2()
 	{
 		TestBoard b = new TestBoard();
 		for (int i = 0; i< 7; i++)
 		{
 			b.pieceAt(6,i).setValue(2);
 		}
 		return b;
 	}
 	
 	public void testCheck(Piece p, boolean debugMode)
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
 	
 	public static void m1()
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
 	
 	public static void m2()
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
 	
 	
 	public static void main(String[] args)
 	{
 		Scanner input = new Scanner(System.in);
		TestBoard b = TestBoard.getTest2();
 		System.out.println(b);
 		
 		while(true)
 		{
 			input.nextLine();
			b.insert(1, 2);
 			System.out.println("Inserted completed...");
 			ArrayList<Piece> test = b.getAllRemove();
 			System.out.println("The size of the list of removed is " +  test.size());
 			System.out.println(b);
 		}
 	}
 }
