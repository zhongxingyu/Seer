 import java.awt.Point;
 import java.util.*;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 public class AI {
     AI() {}
     
     static private final int BLACKPIECE = -1;
     static private final int WHITEPIECE = 1;
     static private final int EMPTYSPOT = 0;
     static public int MAX_GRID_WIDTH_INDEX;
     static public int MAX_GRID_HEIGHT_INDEX;
 	
     static Boolean gameOver(int[][] gameState){
     	
     	int blackPieces = 0;
     	int whitePieces = 0;
     	for (int x = 0; x < MAX_GRID_WIDTH_INDEX+1; x++){ 
 			for (int y = 0; y < MAX_GRID_HEIGHT_INDEX+1; y++){ 
 				if(gameState[x][y] == 1) whitePieces++;
 				if(gameState[x][y] == -1) blackPieces++;
 			}
 		}
     
     	if( (whitePieces == 0) || (blackPieces == 0) ) return true;
     	
     	return false;
 	
     }
     
     static void setBounds(int rowSize, int columnSize){
     	
     	MAX_GRID_WIDTH_INDEX = rowSize;
     	MAX_GRID_HEIGHT_INDEX = columnSize;
     	
     }
     
     static int evaluateBoard(int[][] gameState){
 		int value = 0;
 		
 		for (int x = 0; x < MAX_GRID_WIDTH_INDEX+1; x++){ 
 			for (int y = 0; y < MAX_GRID_HEIGHT_INDEX+1; y++){ 
 				if(gameState[x][y] == 1) value++;
 				if(gameState[x][y] == -1) value--;
 			}
 		}
 		return value;
 	}	
 	
 	static public Boolean aiIsOnGrid(Point move) {
 		
         if(move.x < 1 || move.x > MAX_GRID_WIDTH_INDEX+1) return false;
         
         if(move.y < 1 || move.y > MAX_GRID_HEIGHT_INDEX+1) return false; 
         
         return true;
     }
 	
 	static ArrayList<Move> getValidMoves(int[][] gameState) {//{{{
     	
     	int movingColor = 0;
     	int fwdColor = 0;
     	int bkwdColor = 0;
     	ArrayList<Point> blackPawnLocations = new ArrayList<Point>();
     	ArrayList<Move> validMoves = new ArrayList<Move>();  	
 
     	
     	//For our current board, we want to find all of the black pawns on the board.
     	
     	for (int x = 0; x < MAX_GRID_WIDTH_INDEX+1; x++){ 
 			for (int y = 0; y < MAX_GRID_HEIGHT_INDEX+1; y++){ 
 				
 				if(gameState[x][y] == BLACKPIECE){
 					
 					blackPawnLocations.add(new Point(x + 1,y + 1));
 					
 				}
 			}
 		}    	
     	
     	System.out.println("Number of black pawns: " + blackPawnLocations.size());
     	
 	    for (Point startLocation: blackPawnLocations){
 	    	
 	    	List<Point> adjacentMoves = Grid.getAdjacentPoints(startLocation);
 	    	
 	    	for(Point adjacentMove: adjacentMoves){
 	    		
 	    		if(aiIsOnGrid(adjacentMove) && (gameState[adjacentMove.x-1][adjacentMove.y-1] == EMPTYSPOT) ){	    			
 	    			
 		    		Point fwdMove = Vector.subtract(adjacentMove,startLocation);
 	    	        Point fwdTarPt = Vector.add(adjacentMove,fwdMove);
 	    	        Point bkwdMove = Vector.subtract(startLocation,adjacentMove);
 	    	        Point bkwdTarPt = Vector.add(startLocation,bkwdMove);	    	        
 	    	        movingColor = gameState[startLocation.x-1][startLocation.y-1];
 	    	        
 	    	        
 	    	        if(aiIsOnGrid(fwdTarPt)){
 	    	        	
 	    	        	fwdColor = gameState[fwdTarPt.x-1][fwdTarPt.y-1];
 	    	        	
 	    	        	if( (movingColor != fwdColor) && (fwdColor != EMPTYSPOT) )			
 	    	        		validMoves.add(new Move(startLocation.x, startLocation.y, adjacentMove.x, adjacentMove.y) );	    	        	
 	    	        }	    	        
 	    	        else if(aiIsOnGrid(bkwdTarPt)){		    			
 			    		bkwdColor = gameState[bkwdTarPt.x-1][bkwdTarPt.y-1];
 			    		
 			    		if( (movingColor != bkwdColor) && (bkwdColor != EMPTYSPOT) )			    			
 			    			validMoves.add(new Move(startLocation.x, startLocation.y, adjacentMove.x, adjacentMove.y) );			        	
 		    		}
 		    	}
 	    			
 	    			
 	    	}
 	    		
 	    }
 	        
 	    return validMoves;    
     }
     
 	static ArrayList<Move> getValidPaikaMoves(int[][] gameState) {//{{{
     	
     	ArrayList<Point> blackPawnLocations = new ArrayList<Point>();
     	ArrayList<Move> validMoves = new ArrayList<Move>();  	
     	
     	for (int x = 0; x < MAX_GRID_WIDTH_INDEX+1; x++){ 
 			for (int y = 0; y < MAX_GRID_HEIGHT_INDEX+1; y++){ 
 				
 				if(gameState[x][y] == BLACKPIECE){
 					
 					blackPawnLocations.add(new Point(x + 1,y + 1));
 					
 				}
 			}
 		}
     	
 	    for (Point startLocation: blackPawnLocations){
 	    	
 	    	List<Point> adjacentMoves = Grid.getAdjacentPoints(startLocation);
 	    	
 	    	for(Point adjacentMove: adjacentMoves){
 	    		
 	    		if(aiIsOnGrid(adjacentMove) && (gameState[adjacentMove.x-1][adjacentMove.y-1] == EMPTYSPOT) )  			
 	    			validMoves.add(new Move(startLocation.x, startLocation.y, adjacentMove.x, adjacentMove.y) );	     	
 		    		
 	    	}	    		
 	    }
 	        
 	    return validMoves;    
     }
     
     
     
     static public Move minimax (int[][] gameBoard){
         
         
         int[][] nextBoard;
         int value, maxValue = Integer.MIN_VALUE;
         Move bestMove = new Move();
         
         ArrayList<Move> possibleMoveList = getValidMoves(gameBoard);
         
         if(possibleMoveList.size() != 0) bestMove = new Move(possibleMoveList.get(0));
         
         for (Move possibleMove: possibleMoveList){
         	
           nextBoard = (int[][]) gameBoard.clone();
           
           nextBoard = possibleMove.makeMove(gameBoard);
           value = minMove(nextBoard, 5, maxValue, Integer.MAX_VALUE);
 
           if (value > maxValue) {
         	System.out.println ("Max value : " + value + " at depth : 0");
             maxValue = value;
             bestMove = new Move(possibleMove);
           }
         }
 
         System.out.println ("Move value selected : " + maxValue + " at depth : 0");
 
         return bestMove;
       }
     
       static public int maxMove (int[][] gameBoard, int depth, int alpha, int beta){
 
     	if ( gameOver(gameBoard) || depth <= 0 ) return evaluateBoard(gameBoard);
     	
         int[][] nextBoard;
         int value;        
         ArrayList<Move> possibleMoveList = getValidMoves(gameBoard);
 
         System.out.println ("Max node at depth : " + depth + " with alpha : " + alpha + 
                             " beta : " + beta);
         
         for (Move possibleMove: possibleMoveList){
         	
           nextBoard = (int[][]) gameBoard.clone();
           nextBoard = possibleMove.makeMove(gameBoard);
           
           value = minMove (nextBoard, depth - 1, alpha, beta);
 
           if (value > alpha) {
             alpha = value;
             System.out.println ("Max value : " + value + " at depth : " + depth);
           }
 
           if (alpha > beta) {
        	System.out.println ("Max value with prunning : " + beta + " at depth : " + depth);
             return beta;
           }
       
         }
 
         System.out.println ("Max value selected : " + alpha + " at depth : " + depth);
         return alpha;
       }
       
 	static public int minMove (int[][] gameBoard, int depth, int alpha, int beta){
 	   
 		if ( gameOver(gameBoard) || depth <= 0 ) return evaluateBoard(gameBoard);
 		
 		int[][] nextBoard;
 		int value;          
 		ArrayList<Move> possibleMoveList = getValidMoves(gameBoard);
 		
 		System.out.println ("Min node at depth : " + depth + " with alpha : " + alpha + 
 		                    " beta : " + beta);
 		
 		for (Move possibleMove: possibleMoveList){
         	
 	      nextBoard = (int[][]) gameBoard.clone();
 	      nextBoard = possibleMove.makeMove(gameBoard);
 		  value = maxMove (nextBoard, depth - 1, alpha, beta);
 		
 		  if (value < beta) {
 		    beta = value;
 		    System.out.println ("Min value : " + value + " at depth : " + depth);
 		  }
 		
 		  if (beta < alpha) {
		    System.out.println ("Min value with prunning : " + alpha + " at depth : " + depth);
 		    return alpha;
 		  }
 		}
 		
 		System.out.println ("Min value selected : " + beta + " at depth : " + depth);
 		return beta;
 	}    
 
     static public Move getMove(int[][] gridState) {
         //returns a Move which is 2 pts: start & end
     	Random rand = new Random();
     	int min = 0;
     	int max = 0;    	
     	int randomNum = 0;
     	ArrayList<Move> captureMoves = getValidMoves(gridState);
     	if( captureMoves.size() != 0){
     		
       		return minimax(gridState);
     	}
     	else{
     	
     		ArrayList<Move> paikaMoves = getValidPaikaMoves(gridState);
     		max = paikaMoves.size() - 1;
     		randomNum = rand.nextInt(max - min + 1) + min;
     		Move bestMove = paikaMoves.get(randomNum);
     		
     		return bestMove;
     	
     	}    	
         
     }
 
     static public Move getDoubleMove(int[][] gridState, Point selectedPiece, List<Point> validEndPts) {
     	
     	Random rand = new Random();
     	int min = 0;
     	int max = validEndPts.size() - 1;    	
     	int randomNum = rand.nextInt(max - min + 1) + min;
     	
         Point endPoint = validEndPts.get(randomNum);
         Move bestMove = new Move(selectedPiece.x, selectedPiece.y, endPoint.x, endPoint.y);
         return bestMove;
     }
 }
