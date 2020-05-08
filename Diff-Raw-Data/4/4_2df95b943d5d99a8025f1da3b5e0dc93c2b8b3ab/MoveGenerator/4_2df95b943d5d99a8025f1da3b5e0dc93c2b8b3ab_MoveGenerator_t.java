 package utils;
 
 import java.util.ArrayList;
 
 import constants.LOAConstants;
 
 import model.Move;
 import model.Piece;
 import interfaces.IMoveGenerator;
 
 public class MoveGenerator implements IMoveGenerator {
 
 	public ArrayList<Integer> horizontalMoves = new ArrayList<Integer>();
 	public ArrayList<Integer> verticalMoves = new ArrayList<Integer>();
 	public ArrayList<Integer> diagonal135Moves = new ArrayList<Integer>();
 	public ArrayList<Integer> diagonal45Moves = new ArrayList<Integer>();
 
 	private ArrayList<Integer> possibleDirections =  new ArrayList<Integer>();
 //	private ArrayList<Move> possibleCaptures = new ArrayList<Move>();
 
 	public MoveGenerator(){
 		possibleDirections.add(LOAConstants.RIGHT);
 		possibleDirections.add(LOAConstants.LEFT);		
 		possibleDirections.add(LOAConstants.DOWN);
 		possibleDirections.add(LOAConstants.UP);		
 		possibleDirections.add(LOAConstants.UPPER_RIGHT);
 		possibleDirections.add(LOAConstants.BOTTOM_LEFT);	
 		possibleDirections.add(LOAConstants.BOTTOM_RIGHT);
 		possibleDirections.add(LOAConstants.UPPER_LEFT);				
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see interfaces.IMoveGenerator#isValid(model.Move)
 	 */
 	public boolean isMoveValid(Move m, int color, Piece[][] board) {		
 		if (m != null){
 			
 			int oppositeColor = 0;
 			if(color == LOAConstants.PIECE_TYPE_BLACK)
 				oppositeColor = LOAConstants.PIECE_TYPE_WHITE;
 			else
 				oppositeColor = LOAConstants.PIECE_TYPE_BLACK;
 			
 			int originRow = Helpers.convertRowChartoInt(m.getOrigin().charAt(1));
 			int originColumn = Helpers.convertColunmCharToInt(m.getOrigin().charAt(0));
 			
 			int destinationRow = Helpers.convertRowChartoInt(m.getDestination().charAt(1));
 			int destinationColumn = Helpers.convertColunmCharToInt(m.getDestination().charAt(0));
 			
 			boolean isValid = true;
 			
 			// verifies if row doesn't exceed the board
 			if( destinationRow <= LOAConstants.BOARD_UPPER_AND_BOTTOM_SIDE || destinationRow > LOAConstants.BOARD_RIGHT_AND_LEFT_SIDE)
 				isValid = false;
 			
 			// verifies column doesn't exceed the board
 			if( destinationColumn < LOAConstants.BOARD_UPPER_AND_BOTTOM_SIDE || destinationColumn >= LOAConstants.BOARD_RIGHT_AND_LEFT_SIDE  )
 				isValid = false;
 			
 			// verifies if destination doesn't contain a piece of the same color
 			if(board[8-destinationRow][destinationColumn].getType() == color)
 				isValid = false;
 			
 			//verifies if destination isn't the origin point
 			 if(m.getOrigin() == m.getDestination())
 				isValid = false;
 			 
 			// START VERIFY //
 			// that there isn't any opposite color pieces in between the origin and the destination 
 			if(isValid)
 			{
 				if(originRow != destinationRow && originColumn != destinationColumn) // move diagonale
 				{
 					int columnStart = 0;
 					int columnEnd = 0;
 					int rowStart = 0;
 					int rowEnd = 0;
 					if(originRow < destinationRow && originColumn > destinationColumn) // top left
 					{
 						columnStart = destinationColumn;
 						columnEnd = originColumn;
 						rowStart = 8 - destinationRow;
 						rowEnd = 8 - originRow;
 					}
 					else
 					if(originRow < destinationRow && originColumn < destinationColumn) // top right
 					{
 						columnStart = originColumn;
 						columnEnd = destinationColumn;
 						rowStart = 8 - destinationRow;
 						rowEnd = 8 - originRow;
 					}
 					else
 					if(originRow > destinationRow && originColumn > destinationColumn) // bottom left
 					{
 						columnStart = destinationColumn;
 						columnEnd = originColumn;
 						rowStart = 8 - originRow;
 						rowEnd = 8 - destinationRow;
 					}
 					else
 					if(originRow > destinationRow && originColumn < destinationColumn) // bottom right
 					{
 						columnStart = originColumn;
 						columnEnd = destinationColumn;
 						rowStart = 8 - originRow;
 						rowEnd = 8 - destinationRow;
 					}
 					
 					int j = columnStart + 1;
 					for(int i = rowStart + 1; i < rowEnd; i++)
 					{
 						if(board[i][j].getType() == oppositeColor)
 							isValid = false;
 						j++;
 					}
 				}
 				else
 				if(originRow == destinationRow && originColumn != destinationColumn) // move horizontale
 				{		
 					int start = 0;
 					int end = 0;
 					if(destinationColumn > originColumn) // move vers la droite
 					{
 						start = originColumn;
 						end = destinationColumn;
 					}
 					else 
 					if(originColumn > destinationColumn) // move vers la gauche
 					{
 						start = destinationColumn;
 						end = originColumn;
 					}
 					
 					for(int i = start + 1; i < end; i++)
 					{
 						if(board[8-destinationRow][i].getType() == oppositeColor)
 							isValid = false;
 					}
 				}
 				else
 				if(originRow != destinationRow && originColumn == destinationColumn) // move verticale
 				{
 					int start = 0;
 					int end = 0;
 					if(destinationRow > originRow) // move vers le haut
 					{
 						start = 8 - destinationRow;
 						end = 8 - originRow;
 					}
 					else 
 					if(originRow > destinationRow) // move vers le bas
 					{
 						start = 8 - originRow;
 						end = 8 - destinationRow;
 					}
 					
 					for(int i = start + 1; i < end; i++)
 					{
 						if(board[i][destinationColumn].getType() == oppositeColor)
 							isValid = false;
 					}
 				}
 			}
 			// END VERIFY
 			
 			/*if (( i > LOAConstants.BOARD_UPPER_AND_BOTTOM_SIDE || i <= LOAConstants.BOARD_RIGHT_AND_LEFT_SIDE) || 
 					( j > LOAConstants.BOARD_UPPER_AND_BOTTOM_SIDE || j <= LOAConstants.BOARD_RIGHT_AND_LEFT_SIDE  ) &&
 					board[i][j].getType() != color && m.getOrigin() != (m.getDestination())){
 				return true;
 			}*/
 			//if(isValid)
 				//System.out.println("Move" + m.getOrigin() + m.getDestination() + " is " + isValid);
 			return isValid;
 		}
 		return false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see interfaces.IMoveGenerator#makeMove(model.Piece[][], java.lang.String, java.lang.String, int)
 	 */
 	public Piece[][] makeMove(Piece board[][], Move move, int type) {
 
 		//System.out.println(move.getOrigin().charAt(1) + move.getOrigin().charAt(0));
 		int rowStart = Helpers.convertRowChartoInt(move.getOrigin().charAt(1));
 		int colunmStart = Helpers.convertColunmCharToInt(move.getOrigin().charAt(0));
 
 		int rowEnd = Helpers.convertRowChartoInt(move.getDestination().charAt(1));
 		int colunmEnd = Helpers.convertColunmCharToInt(move.getDestination().charAt(0));
 
 		board[8-rowEnd][colunmEnd].setType( type );
 
 		board[8-rowStart][colunmStart].setType( LOAConstants.PIECE_TYPE_NULL );
 
 		return board;
 	}
 
 	@Override
 	public ArrayList<Move> generatePossibleMoves(Piece[][] board, int type) {
 		// TODO Auto-generated method stub
 		ArrayList<Move> moves = new ArrayList<Move>();
 
 		for (int i = 0; i <= board.length - 1; i++)
 			for(int j = 0; j <= board.length - 1; j++){	
 				if(board[i][j].getType() == type){
 					for( int direction : possibleDirections ){
 						Move move = createMoveByDirection( board, i, j, direction, getNumberOfPieceByActionLine( i, j, direction, board ), type );
 						//if(move != null)
 							//System.out.println("Move " + move.getOrigin() + move.getDestination() + " is " + isMoveValid( move, type, board ));
 						if ( isMoveValid( move, type, board ) )
 							moves.add( move );
 					}
 				}					
 			}
 		return moves;
 	}
 
 	@Override
 	public ArrayList<Move> generatePossibleCaptures(Piece[][] board, int type) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public Move createMoveByDirection( Piece [][] board, int i, int j, int direction, int distance, int playerColor ){
 
 		
 		int currentColunm = j;
 		int currentRow = i;
 		
 		switch(direction){
 
 		case LOAConstants.RIGHT: // means that we decrease the colunm and check if there any piece		
 			j += distance ;						
 			break;									
 
 		case LOAConstants.LEFT: // means that we increase the colunm and check if there is any piece
 			j -= distance;			
 			break;						
 
 		case LOAConstants.DOWN:// means that we decrease the row and check if there is any piece			
 			i -= distance;
 			break;
 
 		case LOAConstants.UP:// means that we increase the row and check if there is any piece			
 			i += distance;
 			break;
 			
 		case LOAConstants.UPPER_RIGHT:
 			j += distance;
 			i += distance;
 			break;
 
 		case LOAConstants.UPPER_LEFT:
 			j -= distance;
 			i += distance;
 			break;
 
 		case LOAConstants.BOTTOM_LEFT:
 			j -= distance;
 			i -= distance;
 			break;
 
 		case LOAConstants.BOTTOM_RIGHT:
 			j += distance;
 			i -= distance;
 			break;
 		}
 
 		if (  (i >= LOAConstants.BOARD_UPPER_AND_BOTTOM_SIDE && i <= LOAConstants.BOARD_RIGHT_AND_LEFT_SIDE - 1)  &&
 		   (  j >= LOAConstants.BOARD_UPPER_AND_BOTTOM_SIDE && j <= LOAConstants.BOARD_RIGHT_AND_LEFT_SIDE - 1 )  ){			
 			
 			int weight = BoardEvaluator.centralisation(i, j);
 
 			return new Move ( Helpers.convertColunmIntToLetter( currentColunm ) + String.valueOf( 8 - currentRow ), 
 					Helpers.convertColunmIntToLetter( j ) + String.valueOf( 8 - i ), 
 					distance, weight );
 		}
 		else if ( ( i > LOAConstants.BOARD_UPPER_AND_BOTTOM_SIDE || i < LOAConstants.BOARD_RIGHT_AND_LEFT_SIDE - 1 ) ||
 				 ( j > LOAConstants.BOARD_UPPER_AND_BOTTOM_SIDE || j < LOAConstants.BOARD_RIGHT_AND_LEFT_SIDE - 1 ) &&
 				board[i][j].getType() == Helpers.getOtherPlayerType(playerColor))
 			return null;
 
 		return null;
 	}
 	
 
 	/**
 	 * This function will get the number of pieces in the specific
 	 * direction gave by the the current piece
 	 */
 	public int getNumberOfPieceByActionLine(int row, int colunm, int direction, Piece[][]board) {
 
 		int counter=0;	
 
 		//Get the position of the current piece
 		int currentColunm = colunm;
 		int currentRow = row ;
 		
 		//Variable for the diagonal
 		int startX = 0, startY = 0;
 
 		if (board[currentRow][currentColunm].getType() != LOAConstants.PIECE_TYPE_NULL){
 
 			switch( direction ){
 
 			case LOAConstants.RIGHT: // means that we decrease the colunm and check if there any piece
 			case LOAConstants.LEFT: // means that we increase the colunm and check if there is any piece
 				while (startY <= LOAConstants.BOARD_RIGHT_AND_LEFT_SIDE - 1 ){
 					if( board[currentRow][startY++].getType() != LOAConstants.PIECE_TYPE_NULL )
 						counter++;				
 				}			
 				break;			
 
 			case LOAConstants.DOWN:// means that we decrease the row and check if there is any piece
 			case LOAConstants.UP:// means that we increase the row and check if there is any piece
 				while (startX <= LOAConstants.BOARD_RIGHT_AND_LEFT_SIDE - 1){
 					if( board[startX++][currentColunm].getType() != LOAConstants.PIECE_TYPE_NULL ) 
 						counter++;
 				}
 				break;			
 
 			case LOAConstants.UPPER_RIGHT:
 			case LOAConstants.BOTTOM_LEFT:
 				startY = currentColunm - (Math.min(currentColunm, currentRow));
 				startX = currentRow - (Math.min(currentColunm, currentRow));
 				
				while( startY <= LOAConstants.BOARD_RIGHT_AND_LEFT_SIDE   &&
						startX <= LOAConstants.BOARD_RIGHT_AND_LEFT_SIDE   &&
 						startY >= LOAConstants.BOARD_UPPER_AND_BOTTOM_SIDE &&
 					   startX >= LOAConstants.BOARD_UPPER_AND_BOTTOM_SIDE){
 					if(board[startX++][startY++].getType() != LOAConstants.PIECE_TYPE_NULL )
 						counter++;
 				
 				}
 				break;
 
 			case LOAConstants.BOTTOM_RIGHT:
 			case LOAConstants.UPPER_LEFT:				
 				startY = currentColunm - (Math.min(currentColunm, currentRow));
 				startX = currentRow - (Math.min(currentColunm, currentRow));				
 				
 				while( startY <= LOAConstants.BOARD_RIGHT_AND_LEFT_SIDE - 1 && 
 					   startX <= LOAConstants.BOARD_RIGHT_AND_LEFT_SIDE - 1){
 
 					if(board[startX++][startY++].getType() != LOAConstants.PIECE_TYPE_NULL )							
 						counter++;
 				 }				
 				break;
 				
 			}			
 		}
 		return counter;
 	}
 }
