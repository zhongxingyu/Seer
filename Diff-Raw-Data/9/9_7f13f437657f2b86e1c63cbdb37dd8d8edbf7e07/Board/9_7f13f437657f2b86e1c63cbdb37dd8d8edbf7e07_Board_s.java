 
 
 package ChiniMess;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.util.ArrayList;
 
 import Figures.*;
 import TTable.ZobristTable;
 
 
 public class Board implements Comparable<Board>{
     
 
     private final int WIDTH = 5, HEIGHT = 6;
     private final int MAXMOVES = 40; 
     public static final boolean WHITE = true;
     public static final boolean BLACK = false;
     private long hash; // hashmember for TTable
     
     private int moveNumber; 
     private boolean onMove; 
     
     ArrayList<Figure> board = new ArrayList<Figure>();
 
     /**
      * @brief init board with default values and positions
      */
     public Board() {
     	moveNumber = 1; 			 // first move is one
     	onMove = true;  			 //white gets first 		
     	initEmptyBoard(); 			 //set empty board
     	initDefaultFigurePosition(); //set start positions
     	
     }
     
     public Board(char inputTurn) {
     	this();
     	if (inputTurn == 'W')
     		onMove = true; 
     	else
     		onMove = false;
     }
     
     /**
      * @brief Copy constructor
      */
     public Board(Board inputBoard) {    
     	this.onMove 	= inputBoard.getPlayerOnTurn();
     	this.moveNumber = inputBoard.getMoveNumber();
     	this.board 		= inputBoard.getBoard();
      }
     
     /**
      * @brief set board from String-Input; set default if Input is not valid
      * @param boardInput
      */
 	public Board(String boardInput) {
 		initEmptyBoard(); 	
     	if (checkAndSetBoardFromInput(boardInput) == false) { //check and set input
     		moveNumber = 1; 								  //white gets first
         	onMove = true; 									  // first move is one
     		initEmptyBoard(); 	
     		initDefaultFigurePosition(); 					  //set start positions if input is rubbish!
     		 								  
     	}
 	}
 	
 	
 	/**
      * @brief set board from InputStream; set default if Input is not valid
      * @param boardInput
      */
 	public Board(InputStream inputStream) throws IOException { // don't forget ExceptionHandling!
 		
 		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
 		String strLine; // buffer for current line
 		String input = "";
 		
 		initEmptyBoard(); 	
 		
 		while ((strLine = br.readLine()) != null)   { //Read Line By Line
 			input += strLine;
 		}
 		if (checkAndSetBoardFromInput(input) == false) { //check and set input
 			initEmptyBoard(); 
 			initDefaultFigurePosition(); 					   //set start positions if input is rubbish!
     		moveNumber = 1; 							       // first move is one
         	onMove = true;  								   //white gets first 
     	}
 	}
 	
 	/**
 	 * @brief init empty board
 	 */
 	private void initEmptyBoard() {
 		for (int i = 0; i < WIDTH * HEIGHT; i++) {
 				this.board.add(null); //put an empty figure in each cell
 		}
 		
 	}
 	
 	
 	/**
 	 * @brief get String from current Board parameters
 	 * @return String of the Board
 	 */
 	public String toString() {
 		String outputString = "";
 			
 		outputString += "" + this.moveNumber + " "; //print number of moves first
 		
 		//set header
 		if (this.onMove == true) //white 
 				outputString += "W"; //white-players turn
 		else
 				outputString += "B"; //black-players turn
 
 		outputString += "\n"; //new line for board
 		
 		//set board
 		for (int i = 0; i < board.size(); i++) {
 			if (i % WIDTH == 0 && i != 0) 
 				outputString += '\n'; //end of one line
 			
 			if (board.get(i) != null) {
 				outputString += board.get(i); //print the toString() of the figure
 			}
 			else {
 				outputString += "."; //for empty positions
 			}
 			
 		}
 		return outputString; //return the result
 	}
 	
 	
 	/**
 	 * @brief checks and set the current turn parameter from inputString
 	 * @param inputMove
 	 * @return move could be set
 	 */
 	public boolean checkAndSetCurrentTurn(String inputTurn) {
 		//check moves
 		try {
 			int turn = Integer.valueOf(inputTurn);
 			if (turn < MAXMOVES  && turn > 0 ) {	//check if move is in range	
 				this.moveNumber = turn; 				//set attribute to current turn_value
 				return true;							//return success
 			}
 			return false;								//BAM!
 			
 					
 		}
 		//set default if no luck
 		catch(NumberFormatException e_ref) {
 			return false;								//BAM!
 		}
 		
 	}
 	
 	/**
 	 * @brief check if color is black or white from input string
 	 * @param input
 	 * @return color is valid
 	 */
 	public boolean checkAndSetColor(String input) {
 		
 		
 		//check colors
 		if (input.contains("W") && input.contains("B")) {;
 			return false; 					// current move cannot be black and white!
 		}
 		else if (input.contains("W")) {
 			this.onMove = true; 			//set white player
 		}
 		else if (input.contains("B"))
 			this.onMove = false; 			//set black player
 		else
 			return false;					//BAM! 
 		
 		return true;						//YEAH!
 		
 	}
 	
 	/**
 	 * @brief check string for valid board parameters; set values if possible
 	 * @param input
 	 * @return
 	 */
 	public boolean checkAndSetBoardFromInput(String input) {
 	
 		//init StringTokenizer -> remove spaces
 		String tokens [] = input.split("[ \t\n]+"); //split by regular expression
 		if (tokens[0] != null && this.checkAndSetCurrentTurn(tokens[0]) == false)	//first part of the String has to be the move_value
 				return false;	
 		
 		if (tokens[1] != null && this.checkAndSetColor(tokens[1]) == false) 
 				return false;
 		
 		if (tokens.length == HEIGHT + 2) { //test if String_HEIGHT is valid
 			for (int i = 2; i < tokens.length;i++) {
 				char currentLine [] = tokens[i].toCharArray();
 				
 				if (currentLine.length < WIDTH) //test String_WIDTH is valid
 					return false;
 				
 				for (int j = 0; j < currentLine.length;j++) {
 					Square figureSquare = new Square(j,(HEIGHT - 1) - (i - 2));
 					if (validateAndSetCurrentFigure(currentLine[j],figureSquare) == false) { //check and set current Figure_Object
 						return false;
 					}
 				}
 			}
 			return true;
 		}
 		else 
 			return false;
 		
 	}
 	
 	/**
 	 * @brief init figure with char c at position index on the board
 	 * @param c
 	 * @param index
 	 * @return true, if figure on index with char could be set
 	 */
 	public boolean validateAndSetCurrentFigure(char c, Square inputSquare) {
 		boolean isWhite = false; 
 		boolean possible = false; // return_value
 		
 		if (c > 'A' && c < 'Z') //white is upper_Case
 			isWhite = true; 
 		
 		c = Character.toLowerCase(c); //we do not need to check upper and lower-cases
 		
 		try { //set the objects
 			switch (c) {
 				
 				case 'k':
 					possible = setFigureToField(inputSquare, new King(isWhite));
 					break;
 				case 'q':
 					possible = setFigureToField(inputSquare, new Queen(isWhite));
 					break;
 				case 'b':
 					possible = setFigureToField(inputSquare, new Bishop(isWhite));
 					break;
 				case 'n':
 					possible = setFigureToField(inputSquare, new Knight(isWhite));
 					break;
 				case 'r':
 					possible = setFigureToField(inputSquare, new Rook(isWhite));
 					break;
 				case 'p':
 					possible = setFigureToField(inputSquare, new Pawn(isWhite));
 					break;
 				case '.':
 					possible = setFigureToField(inputSquare, null);
 					break;
 				default: 
 					possible = false;
 			}
 		}
 		//if index is out of range
 		catch (IndexOutOfBoundsException e_ref) {
 			possible = false;
 		}
 		//return success
 		return possible;
 		
 	}
 	
 	
 	/**
 	 * @brief  Executes a move.
 	 *         checks if the move is valid. 
 	 *         changes current player and the perhaps number of moves.
 	 *                  
 	 * @param m
 	 * @return
 	 */
 	public boolean executeMove(Move m)  {
 		
 		Square square_from = new Square(m.getFrom().getCol(), m.getFrom().getRow());
 		Square square_to   = new Square(m.getTo().getCol(),   m.getTo().getRow());
 		
 		if(m.isValid()) {
 		    Figure fromFigure = this.getFigureFromField(square_from);
 		    Figure toFigure = this.getFigureFromField(square_to);
 			if(fromFigure != null 
 			&& ((fromFigure.canExecuteMove(m) && toFigure == null) 
 			 || (fromFigure.canExecuteCapture(m) && toFigure != null && toFigure.getColor() != fromFigure.getColor()))
 			//toFigure.getColor() != fromFigure.getColor()
 			&& (fromFigure.canJump() || m.pathIsFree(this))) {
 			    
 			    // if a Pawn gets to the other side -> make it a Queen
 			    if ( (fromFigure instanceof Pawn && fromFigure.getColor() == this.WHITE && square_to.getRow() == this.HEIGHT-1)
 			      || (fromFigure instanceof Pawn && fromFigure.getColor() == this.BLACK && square_to.getRow() == 0)){
 			        //System.out.println("Pawn -> Queen");
 			        fromFigure = new Queen(fromFigure.getColor());
 			    }
 			    
 			    this.setFigureToField(square_to, fromFigure);
 			    this.setFigureToField(square_from, null);
 			    
 			    this.onMove = !this.onMove;  // change current player
 			    if (this.onMove == this.WHITE)             // white players turn again
 			        this.moveNumber++;
 			    return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	
 	/**
 	 * @brief get current figure from inputSquare
 	 * @param inputSquare
 	 * @return figure at square-position; null if empty
 	 */
 	public Figure getFigureFromField(Square inputSquare) {
 		
 		if (inputSquare.isValid() == false) {
 			
 			throw new IllegalArgumentException("wrong square!");
 		}
 		else {
 			return board.get(WIDTH * (HEIGHT - inputSquare.getRow() - 1) + inputSquare.getCol()); //get Figure from Square
 		}
 	}
 	
 	public boolean setFigureToField(Square inputSquare, Figure inputFigure) {
 		if (inputSquare.isValid()) {
 			int column = inputSquare.getCol();
 			int line  = HEIGHT - inputSquare.getRow() - 1;
 			board.set(line * WIDTH + column, inputFigure);
 			return true;
 		}
 		else
 			return false;
 	}
 	
 	/**
 	 * @brief write output values with current board parameters
 	 * @param outputStream
 	 * @throws IOException
 	 */
 	public void print(OutputStream outputStream) throws IOException { //BAM-> ExceptionHandling outside the class!
 		
 		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
 		String output [] = this.toString().split("\n"); //BufferedWriter ignores '\n'
 		for (String line: output) {
 			bw.write(line); //write board to file
 			bw.newLine();	//and another line...
 		}
 		bw.close(); //close stream
 	}
 	
 	/**
 	 * @brief set starting positions
 	 */
 	private void  initDefaultFigurePosition() {
 		
 		int offset = 0;
 		
 		//set figures for black 
 		board.set(0, new King(false)); 
 		board.set(1, new Queen(false));
 		board.set(2, new Bishop(false));
 		board.set(3, new Knight(false));
 		board.set(4, new Rook(false));
 
 		
 		//set pawns for black
 		for (int i = WIDTH; i < 2 * WIDTH; i++) {
 			board.set(i, new Pawn(false));
 		}
 
 		//set figures for white
 		offset = WIDTH * HEIGHT - 1;
 		board.set(offset - 0, new King(true)); 
 		board.set(offset - 1, new Queen(true));
 		board.set(offset - 2, new Bishop(true));
 		board.set(offset - 3, new Knight(true));
 		board.set(offset - 4, new Rook(true));
 		
 		offset = WIDTH * HEIGHT;
 		//set pawn for white
 		for (int i = offset - 2 * WIDTH; i < offset - WIDTH; i++) {
 			board.set(i, new Pawn(true));
 		}
 			
 	}
 	
 	/**
 	 * @brief get possible moves for all valid figures
 	 * @return possible moves for all figures of the current player
 	 */
 	public ArrayList<Move> genMoves() {
 			
 		ArrayList<Move> moves = new ArrayList<Move>();
 		for(int row = 0; row < HEIGHT; row++) {
 			for (int col = 0; col < WIDTH; col++) {
 				
 				Square fromSquare = new Square(col, row);
 				Figure tmpFigure = getFigureFromField(fromSquare);
 				
 				if (tmpFigure != null && tmpFigure.getColor() == this.onMove) { //test for empty field and turn		
 					moves.addAll(simulateMoves(fromSquare));
 				}
 			}
 		}
 		return moves;
 			
 	}
 	
 	/**
 	 * @brief get possible moves for a valid figure
 	 * @param fromSquare
 	 * @return possible moves for figure on "fromSquare" field
 	 */
 	public ArrayList<Move> simulateMoves(Square fromSquare) {
 		
 		ArrayList<Move> possibleMoves = new ArrayList<Move>();
 		
 		for(int currentRow = 0; currentRow < HEIGHT; currentRow++) {
 			for (int currentCol = 0; currentCol < WIDTH; currentCol++) {
 				
 				Square toSquare = new Square(currentCol, currentRow);
 				Figure toFigure   = getFigureFromField(toSquare);       // figure on destination square
 				Figure fromFigure = getFigureFromField(fromSquare);     // figure on start square
 				Move m = new Move(fromSquare, toSquare);
 				if (toFigure == null  
					|| (toFigure.getColor() != fromFigure.getColor() && fromFigure.canExecuteCapture(m))){   // free square or figure of other team?
				   
				    if (fromFigure.canExecuteMove(m)  && m.pathIsFree(this) ) {       // can figure execute move?
 				        possibleMoves.add(new Move(fromSquare, toSquare));
				    }
 				}
 			}
 		}
 		return possibleMoves;
 	}
 	
 	
 	
 	public int calculateScore(){
         int white_score = 0, black_score = 0;
         boolean white_king_alive = false, black_king_alive = false;
         
         if (this.gameOver() == GameStatus.GAME_DRAW)
             return 0;
         
         for (int r = 0; r < this.HEIGHT; r++)
             for (int c = 0; c < this.WIDTH; c++){
                 Square s = new Square(c, r);
                 Figure f = this.getFigureFromField(s);
                 
                 if (f != null){
                     
                     if (f instanceof King){
                         if (f.getColor() == this.BLACK)
                             black_king_alive = true;
                         else
                             white_king_alive = true;
                     }
                     
                     if (f.getColor() == this.BLACK)
                         black_score += f.getScore();
                     else
                         white_score += f.getScore();
                 }
             }
         if (this.onMove == this.WHITE){
             if (!black_king_alive)
                 return 10000;
             if (!white_king_alive)
                 return -10000;
             return white_score - black_score;
         }
         else{
             if (!white_king_alive)
                 return 10000;
             if (!black_king_alive)
                 return -10000;
             return black_score - white_score;
         }
     }
     
     public GameStatus gameOver(){
         boolean white_lives = false, black_lives = false;
         if (this.moveNumber >= this.MAXMOVES)
             return GameStatus.GAME_DRAW;
         for (int r = 0; r < 6; r++)
             for (int c = 0; c < 5; c++){
                 Square s = new Square(c, r);
                 if (!s.isValid())
                     throw new IllegalArgumentException("wrong square!");
                 Figure f = this.getFigureFromField(s);
                 if (f != null){
                     if (f.toString().equals("k"))
                         black_lives = true;
                     else if (f.toString().equals("K"))
                         white_lives = true;
                 }
             }
         
         if (black_lives && white_lives)
             return GameStatus.GAME_RUNNING;
         else if (black_lives)
             return GameStatus.GAME_BLACKWINS;
         else if (white_lives)
             return GameStatus.GAME_WHITEWINS;
         else
             return GameStatus.GAME_DRAW;     // shouldn't be possible!
     }
 	
 
 	public boolean getPlayerOnTurn(){
 	    return this.onMove;
 	}
 
 	public int getMoveNumber(){
 	    return this.moveNumber;
 	}
 	
 	public void setMoveNumber(int number){
         if (number > 0 && number < 40)
             this.moveNumber = number;
     }
 
 	public ArrayList<Figure> getBoard() {
 		return new ArrayList<Figure>(this.board);
 	}@Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
 
         for (int row=0; row < this.HEIGHT; row++)
             for (int col=0; col < this.WIDTH; col++)
                 result += ZobristTable.array[col + row*this.WIDTH];
         result = prime * result + (int) (hash ^ (hash >>> 32));
         return result;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (this == obj)
             return true;
         if (obj == null)
             return false;
         if (getClass() != obj.getClass())
             return false;
         Board other = (Board) obj;
         if (hash != other.hash)
             return false;
         return true;
     }
     
     public void setTurn(boolean turn) {
     	this.onMove = turn;
     }
 
     @Override
     public int compareTo(Board arg0) {
         int this_score = this.calculateScore();
         int other_score = arg0.calculateScore();
         if (this_score == other_score) 
             return 0;
         else if (this_score > other_score)
             return 1;
         else
             return -1;
     }}
