 package spiralPrintChallenge;
 
 import java.awt.Point;
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.Vector;
 
 
 public class SpiralPrint {
 
 
 	/**
 	 * Reads the file given and prints the elements in it in a spiral order
 	 * 
 	 * @param path - The path of the input file
 	 */
 	private void printSpiralOrder(String path){
 		
 
 		try {
 
 			FileReader fr = new FileReader(path);
 			BufferedReader br = new BufferedReader(fr);
 			String line;
 			Vector<String> spiralOrder = null;
 			
 			while( ( line = br.readLine() ) != null){
 				
 				try{
 					String[][] tableElements = buildTable(line);
 					spiralOrder = printSpiral(tableElements);
 					//Print result
 					for( String spiralElement : spiralOrder )
 						System.out.print(spiralElement + " ");
 					System.out.print("\n");
 				}
 				catch(NumberFormatException e){
 					System.out.println("The row representing the table is not well formed. " +
 											"Doesn't Work " +
 											e.getMessage() );
 				}
 				catch(Exception f){
 					System.out.print( f.getMessage() );
 				}
 				
 				
 			}
 			fr.close();
 			
 		}catch(IOException g){
 			System.out.println("Cannot read input file" + g.getMessage());
 		}
 		catch (Exception h) {
 			System.out.println( h.getMessage());
 		}
 		
 		
 	}
 	
 	/**
 	 * Walks through clockwise spirally the table representation of the input elements.
 	 * Steps right until reaches the end of the array or reaches an already inspected element then turns.
 	 * Steps down still the same criteria, and so on, with the remaining directions.
 	 * When done with the loop, steps right and starts over, if there are remaining elements.
 	 * 
 	 * @param tableElements - 2D String array representing the structure of the input row
 	 * @return - The Vector representation of the spirally ordered elements
 	 */
 	private Vector<String> printSpiral(String[][] tableElements){
 		
 		Vector<String> spiralOrder 	= new Vector<String>();
 		Vector<Point> visited		= new Vector<Point>();
 		
 		Point neighbour;
 		
 		int rowNum = tableElements.length;
 		int colNum = tableElements[0].length;
 		
 		Point currentPoint 	= new Point(0, 0);
 		
 		while(currentPoint != null){
 
 			boolean endOfLine = false;
 			// step right
 			while( ! endOfLine ){
 			
 				if( !visited.contains(currentPoint) ){
 					visited.add(currentPoint);
 					spiralOrder.add(tableElements[currentPoint.y][currentPoint.x]);
 				}
 				
 				if( !visited.contains( neighbour = new Point((int)currentPoint.getX()+1, (int)currentPoint.getY())) && neighbour.x < colNum )
 					currentPoint = neighbour;
 				else
 					endOfLine = true;
 			}
 			// step down
 			endOfLine = false;
 			while( ! endOfLine ){
 			
 				if( !visited.contains(currentPoint) ){
 					visited.add(currentPoint);
 					spiralOrder.add(tableElements[currentPoint.y][currentPoint.x]);
 				}
 
 				if( !visited.contains( neighbour = new Point((int)currentPoint.getX(), (int)currentPoint.getY()+1)) && neighbour.y < rowNum )
 					currentPoint = neighbour;
 				else
 					endOfLine = true;
 			}
 			// step left
 			endOfLine = false;
 			while( ! endOfLine ){	
 			
 				if( !visited.contains(currentPoint) ){
 					visited.add(currentPoint);
 					spiralOrder.add(tableElements[currentPoint.y][currentPoint.x]);
 				}
 
 				if( !visited.contains( neighbour = new Point((int)currentPoint.getX()-1, (int)currentPoint.getY())) && neighbour.x >= 0 )
 					currentPoint = neighbour;
 				else
 					endOfLine = true;
 			}
 			// step up
 			endOfLine = false;
 			while( ! endOfLine ){	
 			
 				if( !visited.contains(currentPoint) ){
 					visited.add(currentPoint);
 					spiralOrder.add(tableElements[currentPoint.y][currentPoint.x]);
 				}
 
 				if( !visited.contains( neighbour = new Point((int)currentPoint.getX(), (int)currentPoint.getY()-1)) && neighbour.y >= 0 )
 					currentPoint = neighbour ;
 				else
 					endOfLine = true;
 			}
 
 			
 			// if right neighbour is not already visited, or out of bound, step right and start over
 			// else set inspected point to null end quit
 			if( !visited.contains( neighbour = new Point((int)currentPoint.getX()+1, (int)currentPoint.getY())) && neighbour.x < colNum ){
 				currentPoint = neighbour;
 			}
 			else
 				currentPoint = null;
 
 			
 			
 		}
 		
 		return spiralOrder;
 		
 	}
 	
 	
 	/**
 	 * Builds the 2D array from the new line of the file. 
 	 * If too much elements comparing to the given dimensions, cuts it off.
 	 * 
 	 * @param line -  Line of the file
 	 * @return 2D array of table
 	 */
 	private String[][] buildTable(String line) throws NumberFormatException, Exception{
 		
 		int rowNum=0;
 		int colNum=0;
 		
 		
 		String[] parts = line.split(";");
 		rowNum = Integer.valueOf(parts[0]);
 		colNum = Integer.valueOf(parts[1]);
 		String[] lineElements = parts[2].split(" ");
 		String[][] tableElements = new String[rowNum][colNum];
 		
 		if ( lineElements.length < rowNum*colNum){
 			System.out.println();
 			throw new Exception("Comparing to the tablesize given, not enough elements in the row to build the table");
 		}
 		else{
 		
 			// Filling up tableElements
 			int i = 0;
 			for( int n=0 ; n<rowNum ; n++ )
 				for( int m=0 ; m<colNum ; m++ ){
 					tableElements[n][m] = lineElements[i++];
 				}
 		}
 		
 		return tableElements;
 	}
 		
 	
 	public static void main(String[] args) {
 		
 		SpiralPrint sp = new SpiralPrint();
		
		try{	
			sp.printSpiralOrder(args[0]);
		}
		catch(Exception e){
			System.out.print( "No argument given. Please give path of input file as arhument. " + e.getMessage() );
		}
 	}
 
 }
