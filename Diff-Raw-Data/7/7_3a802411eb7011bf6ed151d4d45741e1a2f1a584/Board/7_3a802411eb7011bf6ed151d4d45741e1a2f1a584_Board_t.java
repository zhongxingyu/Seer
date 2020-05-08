 package twelve.team;
 
 import java.awt.Point;
 
 public class Board
 {
 	private Piece[][] board;
 	private int rows = 5;
 	private int cols = 9;
 	
 	public Board()
 	{
 		board = new Piece[rows][cols];
 		resetBoard();
 	}
 
 	public Board(int r, int c)
 	{
 		rows = r;
 		cols = c;
 		board = new Piece[rows][cols];
 		resetBoard();
 	}
 
 	public void resetBoard()
 	{
		for(int i = 0; i < rows/2; i++) //color bottom whites
 		{
 			for(int j = 0; j < cols; j++)
 			{
 				board[i][j] = new Piece(Piece.Team.WHITE);
 			}
 		}
		for(int i = (rows/2)+1; i < rows; i++)//color top black
 		{
 			for(int j = 0; j < cols; j++)
 			{
 				board[i][j] = new Piece(Piece.Team.BLACK);
 			}
 		}
 		boolean color = true;
 		for(int i = 0; i < cols; i++)
 		{
 			if(i == cols/2)
 			{
 				board[rows/2][i] = null;
 				continue;
 			}
 			if(color)
 				board[rows/2][i] = new Piece(Piece.Team.BLACK);
 			else
 				board[rows/2][i] = new Piece(Piece.Team.WHITE);
 			color = !color;
 		}
 	}
 	
 	public enum Direction {up,down,left,right,topleft,topright,botleft,botright,none};
 	
 	//returns a Direction from a given starting point and an ending point
 	public static Direction getDirection(Point start, Point end)
 	{
 		if( start.x - end.x > 0) //left
 		{
 			if (start.y - end.y < 0) //up
 				return Board.Direction.topleft;
 			else if (start.y - end.y > 0) //down
 				return Board.Direction.botleft;
 			else //same .y
 				return Board.Direction.left;
 		}
 		else if ( start.x - end.x < 0) //right
 		{
 			if (start.y - end.y < 0) //up
 				return Board.Direction.topright;
 			else if (start.y - end.y > 0) //down
 				return Board.Direction.botright;
 			else //same .y
 				return Board.Direction.right;
 		}
 		else // no x movement (only up or down)
 		{
 			if(start.y - end.y < 0)
 				return Board.Direction.up;
 			if(start.y - end.y > 0)
 				return Board.Direction.down;
 			else
 				return Board.Direction.none; //no movement
 		}
 	}
 	
 	//gets the opposite direction from a given direction
 	public Direction getOppositeDirection(Direction d)
 	{
 		switch(d)
 		{
 		case up:
 			return Direction.down;
 		case down:
 			return Direction.up;
 		case left:
 			return Direction.right;
 		case right:
 			return Direction.left;
 		case topleft:
 			return Direction.botright;
 		case topright:
 			return Direction.botleft;
 		case botleft:
 			return Direction.topright;
 		case botright:
 			return Direction.topleft;
 		default:
 			return Direction.none;
 		}
 	}
 	
 	//Gets the point with a given starting point and a direction (for lack of a better name)
 	public Point getPoint(Point start, Direction d)
 	{
 		Point end = new Point();
 		switch(d)
 		{
 		case up:
 			end.y = start.y + 1; //up
 			end.x = start.x; //none
 			break;
 		case down:
 			end.y = start.y - 1; //down
 			end.x = start.x; //none
 			break;
 		case left:
 			end.y = start.y; //none
 			end.x = start.x - 1; //left
 			break;
 		case right:
 			end.y = start.y; //none
 			end.x = start.x + 1; //right
 			break;
 		case topleft:
 			end.y = start.y + 1; //up
 			end.x = start.x - 1; //left
 			break;
 		case botleft:
 			end.y = start.y - 1; //down
 			end.x = start.x - 1; //left
 			break;
 		case topright:
 			end.y = start.y + 1; //up
 			end.x = start.x + 1; //right
 			break;
 		case botright:
 			end.y = start.y - 1; //down
 			end.x = start.x + 1; //right
 			break;
 		case none:
 			end.y = start.y; //none
 			end.x = start.x; //none
 		}
 		//might want to check if the point contains negative values
 		return end;
 	}
 	
 	//checks if a move is valid
 	public boolean isValid(Point start, Point end)
 	{
 		Direction direction  = getDirection(start,end);
 		
 		if(board[end.y][end.x] != null) //you can only move a piece to an empty spot
 			return false;
 		if(direction == Direction.none) //no move was made
 			return true;
 		
 		//no need to check for borders since visual board should not let you move a piece outside the borders
 		
 		if(Math.abs(start.x-end.x) <= 1 && Math.abs(start.y-end.y) <= 1) //only moved max 1 position in any direction
 		{
 			if(start.y + start.x % 2 == 0) //diagonals are possible
 			{	
 				//check for piece
 				return true;
 			}
 			else //only up, down, left, right
 			{
 				if(		direction == Direction.topleft  ||
 						direction == Direction.topright ||
 						direction == Direction.botright || 
 						direction == Direction.botleft )
 					return false;
 				else	
 					return true;
 			}
 		}
 		else
 			return false; //moved either x or y more than one position
 	}
 	
 	//return 0 for no capture, 
 	public boolean canCapture(Piece piece, Point start)
 	{
 		//Piece.Team team = piece.getTeam();
 		Piece.Team opposite = piece.getOppositeTeam();
 		
 		boolean ahead = false;
 		boolean behind = false;
 		int captureAhead = 0;
 		int captureBehind = 0;
 		
 		for(Direction d : Direction.values())
 		{
 			Point land = getPoint(start,d); //get point in all directions
 			
 			if( land == start) //when direction is none
 				continue;
 			
 			if(board[land.y][land.x] == null) //you will land in an empty spot
 			{
 				Point target = getPoint(land,d);  //look for targets in the same direction you traveled
 				Point targetBehind = getPoint(start,getOppositeDirection(d)); //from your starting position get the target behind 
 				if(board[target.y][target.x].getTeam() == opposite) //check position ahead in the same direction you advanced for opposite team
 				{
 					ahead = true;
 					++captureAhead;
 				}
 				if(board[targetBehind.y][targetBehind.x].getTeam() == opposite)
 				{
 					behind = true;
 					++captureBehind;
 				}
 			}
 		}
 		if (captureAhead+captureBehind > 0)
 			return true;
 		else 
 			return false;
 	}
 	
 	public Piece[][] getBoard(){
 		return board;
 	}

 }
