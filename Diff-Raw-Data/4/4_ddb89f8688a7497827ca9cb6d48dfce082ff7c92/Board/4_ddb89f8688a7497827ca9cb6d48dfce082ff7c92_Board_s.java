 package twelve.team;
 
 import java.awt.Point;
 import java.util.Scanner;
 
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
 	private static Direction getDirection(Point start, Point end)
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
 	
 	public void printBoard()
 	{
 		for(int i = rows-1; i >= 0; i--)
 		{
 			for(int j = 0; j < cols; j++)
 			{
 				char t = 0;
 				if(board[i][j] == null)
 				{
 					t = ' ';
 				}
 				else
 				{
 					switch(board[i][j].getTeam())
 					{
 					case WHITE:
 						t = 'w';
 						break;
 					case BLACK:
 						t = 'b';
 						break;
 					default:
 						t = ' ';
 						break;
 					}
 				}
 				System.out.print("["+t+"]");
 			}
 			System.out.println();
 		}
 	}
 	//gets the opposite direction from a given direction
 	private Direction getOppositeDirection(Direction d)
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
 	//returns null if point is out of bounds
 	private Point getPoint(Point start, Direction d)
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
 		if(end.y < 0 || end.y >= rows)
 			return null;
 		if(end.x < 0 || end.x >= cols)
 			return null;
 		//might want to check if the point contains negative values
 		return end;
 	}
 	
 	//checks if a move is valid
 	private boolean isValid(Point start, Point end)
 	{
 		if(start == null || end == null)
 			return false;
 		Direction direction  = getDirection(start,end);
 		
 		if(getPiece(start) == null) //cannot move an empty spot!
 			return false;
 		if(getPiece(end) != null) //you can only move a piece to an empty spot
 			return false;
 		if(direction == Direction.none) //no move was made
 			return true;
 		
 		//no need to check for borders since visual board should not let you move a piece outside the borders
 		
 		if(Math.abs(start.x-end.x) <= 1 && Math.abs(start.y-end.y) <= 1) //only moved max 1 position in any direction
 		{
 			if((start.y + start.x) % 2 == 0) //diagonals are possible
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
 	private boolean canCapture(Point start)
 	{
 		Piece piece = board[start.y][start.x];
 				
 		Piece.Team opposite = piece.getOppositeTeam();
 		
 		int captureAhead = 0;
 		int captureBehind = 0;
 		
 		for(Direction d : Direction.values())
 		{
 			Point land = getPoint(start,d); //get point in all directions
 			
 			if( land == start) //when direction is none
 				continue;
 			
 			//if(board[land.y][land.x] == null) //you will land in an empty spot
 			if(isValid(start,land))// checks that the move was legal
 			{
 				Point target = getPoint(land,d);  //look for targets in the same direction you traveled
 				Point targetBehind = getPoint(start,getOppositeDirection(d)); //from your starting position get the target behind 
 				if(target != null)
 					if(board[target.y][target.x] != null) //make sure target is not null
 						if(board[target.y][target.x].getTeam() == opposite) //check position ahead in the same direction you advanced for opposite team
 						{
 							++captureAhead;
 						}
 				if(targetBehind != null)
 					if(board[targetBehind.y][targetBehind.x] != null) //make sure behind is not null
 						if(board[targetBehind.y][targetBehind.x].getTeam() == opposite)
 						{
 							++captureBehind;
 						}
 			}
 		}
 		if (captureAhead+captureBehind > 0)
 			return true;
 		else 
 			return false;
 	}
 	
 	private void deletePiece(Point p)
 	{
 		board[p.y][p.x] = null;
 	}
 	
 	private Piece getPiece(Point p)
 	{
 		return board[p.y][p.x];
 	}
 	
 	private Point possibleCapture(Point start, Point end, moveType type) throws MoveException
 	{
 		if(getPiece(start) == null)
 			return null;
 		Direction d = getDirection(start,end);
 		
 		Point behind = getPoint(start,getOppositeDirection(d));
 		Point target = getPoint(end,d);
 		Piece.Team opposite = getPiece(start).getOppositeTeam();
 		
 		//check that points target and behind are not null
 		if(type == moveType.NONE)
 		{
 			if(behind != null && target != null)
 				if(board[target.y][target.x] != null && board[behind.y][behind.x] != null)
 					if(board[target.y][target.x].getTeam() == opposite && board[behind.y][behind.x].getTeam() == opposite )
 					{
 						throw new MoveException(target,behind); //throws the points of both pieces that can be taken
 					}
 		}
 		if(type == moveType.NONE || type == moveType.ADVANCE)
 		{
 			if(target != null)
 				if(board[target.y][target.x] != null)
 					if(board[target.y][target.x].getTeam() == opposite )
 					{
 						return target;
 					}
 		}
 		if(type == moveType.NONE || type == moveType.RETREAT)
 		{
 			if(behind != null)
 				if(board[behind.y][behind.x] != null)
 					if(board[behind.y][behind.x].getTeam() == opposite )
 					{
 						return behind;
 					}
 		}
 		return null;
 	}
 	
 	public enum moveType {ADVANCE,RETREAT,NONE};
 	//returns true if another capture can be made by the same piece
 	
 	public boolean move(Point start, Point end) throws Exception,MoveException
 	{
 		return move(start,end,moveType.NONE);
 	}
 	
 	public boolean move(Point start, Point end, moveType type) throws Exception,MoveException
 	{
 		//check if capture moves are available
 		if(isValid(start,end))
 		{
 			Piece p = getPiece(start);
 			Direction d = getDirection(start,end);
 			Piece.Team opposite = getPiece(start).getOppositeTeam();
 			
			board[end.y][end.x] = p; //move the chosen piece to the space that it was moved
 			
 			Point target = possibleCapture(start,end,type);
 			deletePiece(start);
 			if(target != null)
 			{
 				Direction attack = getDirection(start,target);
 				while(target != null && getPiece(target).getTeam() == opposite) //error here
 				{
 					deletePiece(target);
 					target = getPoint(target,attack);
 					if(target != null)
 						if(board[target.y][target.x] == null )
 							break;
 					//error here
 				}
 			}
 				
 			//if you can capture more pieces with the same original piece return true
 			if(canCapture(end))
 				return true;
 			else
 				return false;
 		}
 		else
 			throw new Exception("Invalid Move");
 	}
 	
 	
 	
 	
 	public Piece[][] getBoard(){
 		return board;
 	}
 	
 	public static void main(String args[])
 	{
 		Board board = new Board();
 		Scanner scanner = new Scanner(System.in);
 		
 		int startx;//column
 		int starty;//row
 		int endx;
 		int endy;
 		board.printBoard();
 		while(true)
 		{
 			System.out.println("Enter your move 'col' 'row' 'col' 'row' ");
 			
 			startx = scanner.nextInt();
 			starty = scanner.nextInt();
 			endx = scanner.nextInt();
 			endy = scanner.nextInt();
 			
 			Point start = new Point();
 			start.x = startx;
 			start.y = starty;
 			Point end = new Point();
 			end.x = endx;
 			end.y = endy;
 			try {
 				if(board.move(start,end,moveType.ADVANCE))
 				{
 					board.printBoard();
 					System.out.println("You can move Again");
 				}
 				else 
 				{
 					board.printBoard();
 					System.out.println("Other team's turn");
 				}
 			} catch (MoveException e) {
 				// TODO Auto-generated catch block
 				//e.printStackTrace();
 				System.out.println("Can capture two");
 				
 				break;
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				if(e.getMessage() == "Invalid Move")
 					System.out.println("Invalid Move");
 				else
 					e.printStackTrace();
 					board.printBoard();
 				continue;
 			}
 		}
 		
 		scanner.close();
 		
 	
 	}
 
 }
