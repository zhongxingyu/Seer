 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 import java.util.*;
 enum MOVE {FOWARD, BACK, LEFT, RIGHT, DF_RIGHT, DF_LEFT, DB_RIGHT, DB_LEFT};
 enum GameState {Init, P1_Turn, P2_Turn, P1_Won, P2_Won, Tie};
 public class Board extends JPanel {
 	public Board(){
 		/* initialize game state here; create pieces,
 		 * populate board, make players, get everything setup
 		 */
 		addMouseListener(new BoardAdapter());
 		int xPos = 50;
 		int yPos = 50;
 		ROWS = 5;
 		COLS = 9;
 		
 		player1 = new Player(true, PieceColor.WHITE);
 		player2 = new Player(true, PieceColor.BLACK);
 		
 		piecePositions = new Position[COLS][ROWS];	
 		
 		for(int y=0; y<ROWS; y++) {
 			for(int x=0; x<COLS; x++){
 				piecePositions[x][y] = new Position(xPos, yPos);
 				xPos += 75;
 			}
 			xPos = 50;
 			yPos += 75;
 		}
 		
 		/*
 		int count=0;
 		for(int i=0; i<ROWS; i++)
 			for(int j=0; j<COLS; j++){
 				System.out.print(piecePositions[j][i].x + ", ");
 				System.out.print(piecePositions[j][i].y + "\n");
 				count++;
 			}
 		System.out.print(count + "\n");
 		*/
 		
 		pieces = new GamePiece[COLS][ROWS]; // total of 44 pieces & 45 spaces to move
 		
 		// top 2 rows - black
 		for(int y=0; y < ROWS/2; y++){
 			 for(int x=0; x < COLS; x++) {
 				GamePiece tempPiece = new GamePiece(piecePositions[x][y], PieceColor.BLACK);
 			 	pieces[x][y] = tempPiece;
 			 }
 		 }
 		
 		// middle row - black
 		for(int x=0; x < COLS; x+=2) {
 			int y = ROWS/2;
 			GamePiece tempPiece = new GamePiece(piecePositions[x][y], PieceColor.BLACK);
 		 	pieces[x][y] = tempPiece;
 		}
 		for(int x=COLS/2+1; x < COLS; x+=2) {
 			int y = ROWS/2;
 			if(ROWS==3 && x==ROWS-1) {
 				GamePiece tempPiece = new GamePiece(piecePositions[x][y], PieceColor.WHITE);
 			 	pieces[x][y] = tempPiece;
 			}
 			else {
 				GamePiece tempPiece = new GamePiece(piecePositions[x][y], PieceColor.BLACK);
 			 	pieces[x][y] = tempPiece;	
 			}
 		}
 		
 		// middle row - white
 		for(int x=1; x < COLS/2+1; x+=2) {
 			int y = ROWS/2;
 			GamePiece tempPiece = new GamePiece(piecePositions[x][y], PieceColor.WHITE);
 		 	pieces[x][y] = tempPiece;
 		}
 		for(int x=COLS/2+2; x < COLS; x+=2) {
 			int y = ROWS/2;
 			GamePiece tempPiece = new GamePiece(piecePositions[x][y], PieceColor.WHITE);
 		 	pieces[x][y] = tempPiece;
 		}
 
 		// bottom 2 rows
 		for(int y=ROWS/2+1; y < ROWS; y++){
 			for(int x=0; x < COLS; x++){
 				GamePiece tempPiece = new GamePiece(piecePositions[x][y], PieceColor.WHITE);
 			 	pieces[x][y] = tempPiece;
 			 }
 		 }
 		 pieces[COLS/2][ROWS/2] = new GamePiece(piecePositions[COLS/2][ROWS/2],PieceColor.NULL);
 		 
 		pieceBoardThere = new boolean[COLS][ROWS];
 
 		for (int i = 0; i < pieceBoardThere.length; i++)
 			{
 			    Arrays.fill( pieceBoardThere[i], true );
 			}
 		pieceBoardThere[COLS/2][ROWS/2] = false;
 
 		// initialize directions that are available for each position
 		/*
 		piecePositions[x][y].N=true;
 		piecePositions[x][y].NE=true;
 		piecePositions[x][y].E=true;
 		piecePositions[x][y].SE=true;
 		piecePositions[x][y].S=true;
 		piecePositions[x][y].SW=true;
 		piecePositions[x][y].W=true;
 		piecePositions[x][y].NW=true;
 		
 		
 		for(int y=1; y < ROWS; y++) {
 			for(int x=0; x < COLS; x++) {
 				if(COLS % 2==0 && ROWS % 2==0 && COLS != 0 && COLS != 8) {	// even
 					piecePositions[x][y].N=true;
 					piecePositions[x][y].NE=true;
 					piecePositions[x][y].E=true;
 					piecePositions[x][y].SE=true;
 					piecePositions[x][y].S=true;
 					piecePositions[x][y].SW=true;
 					piecePositions[x][y].W=true;
 					piecePositions[x][y].NW=true;
 				}
 				else if(COLS % 2==1 && ROWS % 2==1 && COLS != 0 && COLS != 8) {	// odd
 					piecePositions[x][y].N=true;
 					piecePositions[x][y].NE=true;
 					piecePositions[x][y].E=true;
 					piecePositions[x][y].SE=true;
 					piecePositions[x][y].S=true;
 					piecePositions[x][y].SW=true;
 					piecePositions[x][y].W=true;
 					piecePositions[x][y].NW=true;
 				}
 				else if(COLS == 0) {	// cols 0
 
 				}
 				else {	// cols 8
 
 				}				
 			}
 		}
 		*/
 		boardState = GameState.Init;
		turnCounter = 0;
 		updateState();
 	}
 	
 	public void newGame(int _ROWS, int _COLS, Player _player1, Player _player2){
 		/* initialize game state here; create pieces,
 		 * populate board, make players, get everything setup
 		 */
 		addMouseListener(new BoardAdapter());
 		int xPos = 50;
 		int yPos = 50;
 		ROWS = _ROWS;
 		COLS = _COLS;
 		
 		player1 = new Player(_player1.isUser(), _player1.whichColor());
 		player2 = new Player(_player2.isUser(), _player2.whichColor());
 		System.out.println(player2.isUser());
 		
 		piecePositions = new Position[COLS][ROWS];	
 		
 		for(int y=0; y<ROWS; y++) {
 			for(int x=0; x<COLS; x++){
 				piecePositions[x][y] = new Position(xPos, yPos);
 				xPos += 75;
 			}
 			xPos = 50;
 			yPos += 75;
 		}
 		
 		/*
 		int count=0;
 		for(int i=0; i<ROWS; i++)
 			for(int j=0; j<COLS; j++){
 				System.out.print(piecePositions[j][i].x + ", ");
 				System.out.print(piecePositions[j][i].y + "\n");
 				count++;
 			}
 		System.out.print(count + "\n");
 		*/
 		
 		pieces = new GamePiece[COLS][ROWS]; // total of 44 pieces & 45 spaces to move
 		
 		// top 2 rows - black
 		for(int y=0; y < ROWS/2; y++){
 			 for(int x=0; x < COLS; x++) {
 				GamePiece tempPiece = new GamePiece(piecePositions[x][y], PieceColor.BLACK);
 			 	pieces[x][y] = tempPiece;
 			 }
 		 }
 		
 		// middle row - black
 		for(int x=0; x < COLS; x+=2) {
 			int y = ROWS/2;
 			GamePiece tempPiece = new GamePiece(piecePositions[x][y], PieceColor.BLACK);
 		 	pieces[x][y] = tempPiece;
 		}
 		for(int x=COLS/2+1; x < COLS; x+=2) {
 			int y = ROWS/2;
 			if(ROWS==3 && x==ROWS-1) {
 				GamePiece tempPiece = new GamePiece(piecePositions[x][y], PieceColor.WHITE);
 			 	pieces[x][y] = tempPiece;
 			}
 			else {
 				GamePiece tempPiece = new GamePiece(piecePositions[x][y], PieceColor.BLACK);
 			 	pieces[x][y] = tempPiece;	
 			}
 		}
 		
 		// middle row - white
 		for(int x=1; x < COLS/2+1; x+=2) {
 			int y = ROWS/2;
 			GamePiece tempPiece = new GamePiece(piecePositions[x][y], PieceColor.WHITE);
 		 	pieces[x][y] = tempPiece;
 		}
 		for(int x=COLS/2+2; x < COLS; x+=2) {
 			int y = ROWS/2;
 			GamePiece tempPiece = new GamePiece(piecePositions[x][y], PieceColor.WHITE);
 		 	pieces[x][y] = tempPiece;
 		}
 
 		// bottom 2 rows
 		for(int y=ROWS/2+1; y < ROWS; y++){
 			for(int x=0; x < COLS; x++){
 				GamePiece tempPiece = new GamePiece(piecePositions[x][y], PieceColor.WHITE);
 			 	pieces[x][y] = tempPiece;
 			 }
 		 }
 		 pieces[COLS/2][ROWS/2] = new GamePiece(piecePositions[COLS/2][ROWS/2],PieceColor.NULL);
 		 
 		pieceBoardThere = new boolean[COLS][ROWS];
 
 		for (int i = 0; i < pieceBoardThere.length; i++)
 			{
 			    Arrays.fill( pieceBoardThere[i], true );
 			}
 		pieceBoardThere[COLS/2][ROWS/2] = false;
 
 		// initialize directions that are available for each position
 		/*
 		piecePositions[x][y].N=true;
 		piecePositions[x][y].NE=true;
 		piecePositions[x][y].E=true;
 		piecePositions[x][y].SE=true;
 		piecePositions[x][y].S=true;
 		piecePositions[x][y].SW=true;
 		piecePositions[x][y].W=true;
 		piecePositions[x][y].NW=true;
 		
 		
 		for(int y=1; y < ROWS; y++) {
 			for(int x=0; x < COLS; x++) {
 				if(COLS % 2==0 && ROWS % 2==0 && COLS != 0 && COLS != 8) {	// even
 					piecePositions[x][y].N=true;
 					piecePositions[x][y].NE=true;
 					piecePositions[x][y].E=true;
 					piecePositions[x][y].SE=true;
 					piecePositions[x][y].S=true;
 					piecePositions[x][y].SW=true;
 					piecePositions[x][y].W=true;
 					piecePositions[x][y].NW=true;
 				}
 				else if(COLS % 2==1 && ROWS % 2==1 && COLS != 0 && COLS != 8) {	// odd
 					piecePositions[x][y].N=true;
 					piecePositions[x][y].NE=true;
 					piecePositions[x][y].E=true;
 					piecePositions[x][y].SE=true;
 					piecePositions[x][y].S=true;
 					piecePositions[x][y].SW=true;
 					piecePositions[x][y].W=true;
 					piecePositions[x][y].NW=true;
 				}
 				else if(COLS == 0) {	// cols 0
 
 				}
 				else {	// cols 8
 
 				}				
 			}
 		}
 		*/
 		boardState = GameState.Init;
		turnCounter = 0;
 		updateState();
 	}
 
 	// still not done has working checking blocked in and if opposite color is within 2 squaures
 	private boolean isMoveValid(int column, int row){
 		//the valid move checker
 		boolean valid = false;
 		boolean[][] attackGridThere = new boolean[5][5];
 
 		for (int i = 0; i < 5; i++)
 		{
 		    Arrays.fill( attackGridThere[i], true );
 		}
 		
 		for (int i = 0; i < 3; i++)
 		{
 		    Arrays.fill( clearPath[i], false );
 		}
 		
 		if((row%2==0 && column%2==0) || !(row%2==0 || column%2==0)){
 			if(row == ROWS-1 && column == COLS-1){
 				attackGridThere[1][1] = pieceBoardThere[column-1][row-1]; attackGridThere[2][1] = pieceBoardThere[column][row-1];
 				attackGridThere[1][2] = pieceBoardThere[column-1][row]; attackGridThere[2][2] = pieceBoardThere[column][row];
 			}
 			
 			else if(row == ROWS-1 && column == 0){
 				 attackGridThere[2][1] = pieceBoardThere[column][row-1]; attackGridThere[3][1] = pieceBoardThere[column+1][row-1];
 				 attackGridThere[2][2] = pieceBoardThere[column][row]; attackGridThere[3][2] = pieceBoardThere[column+1][row]; 
 			}
 			
 			else if(row == 0 && column == COLS-1){
 				attackGridThere[1][2] = pieceBoardThere[column-1][row]; attackGridThere[2][2] = pieceBoardThere[column][row];
 				attackGridThere[1][3] = pieceBoardThere[column-1][row+1]; attackGridThere[2][3] = pieceBoardThere[column][row+1];
 			}
 			
 			else if(row == 0 && column == 0){
 				attackGridThere[2][2] = pieceBoardThere[column][row]; attackGridThere[3][2] = pieceBoardThere[column+1][row];
 				attackGridThere[2][3] = pieceBoardThere[column][row+1]; attackGridThere[3][3] = pieceBoardThere[column+1][row+1];
 			}
 			
 			else if(row == ROWS-1){
 				attackGridThere[1][1] = pieceBoardThere[column-1][row-1]; attackGridThere[2][1] = pieceBoardThere[column][row-1]; attackGridThere[3][1] = pieceBoardThere[column+1][row-1];
 				attackGridThere[1][2] = pieceBoardThere[column-1][row]; attackGridThere[2][2] = pieceBoardThere[column][row]; attackGridThere[3][2] = pieceBoardThere[column+1][row]; 
 			}
 			else if(row == 0){
 				attackGridThere[1][2] = pieceBoardThere[column-1][row]; attackGridThere[2][2] = pieceBoardThere[column][row]; attackGridThere[3][2] = pieceBoardThere[column+1][row];
 				attackGridThere[1][3] = pieceBoardThere[column-1][row+1]; attackGridThere[2][3] = pieceBoardThere[column][row+1]; attackGridThere[3][3] = pieceBoardThere[column+1][row+1];			 
 			}
 			else if(column == 0){
 				attackGridThere[2][1] = pieceBoardThere[column][row-1]; attackGridThere[3][1] = pieceBoardThere[column+1][row-1];
 				attackGridThere[2][2] = pieceBoardThere[column][row]; attackGridThere[3][2] = pieceBoardThere[column+1][row];
 				attackGridThere[2][3] = pieceBoardThere[column][row+1]; attackGridThere[3][3] = pieceBoardThere[column+1][row+1];
 			}
 			else if(column == COLS-1){
 				attackGridThere[1][1] = pieceBoardThere[column-1][row-1]; attackGridThere[2][1] = pieceBoardThere[column][row-1];
 				attackGridThere[1][2] = pieceBoardThere[column-1][row]; attackGridThere[2][2] = pieceBoardThere[column][row];
 				attackGridThere[1][3] = pieceBoardThere[column-1][row+1]; attackGridThere[2][3] = pieceBoardThere[column][row+1]; 
 			}
 			else{
 				attackGridThere[1][1] = pieceBoardThere[column-1][row-1];
 				attackGridThere[2][1] = pieceBoardThere[column][row-1]; 
 				attackGridThere[3][1] = pieceBoardThere[column+1][row-1];
 				attackGridThere[1][2] = pieceBoardThere[column-1][row]; 
 				attackGridThere[2][2] = pieceBoardThere[column][row]; 
 				attackGridThere[3][2] = pieceBoardThere[column+1][row];
 				attackGridThere[1][3] = pieceBoardThere[column-1][row+1]; 
 				attackGridThere[2][3] = pieceBoardThere[column][row+1];
 				attackGridThere[3][3] = pieceBoardThere[column+1][row+1];
 			}
 		}
 		else{
 			if(row == ROWS-1){
 				attackGridThere[1][2] = pieceBoardThere[column-1][row]; attackGridThere[2][2] = pieceBoardThere[column][row]; attackGridThere[3][2] = pieceBoardThere[column+1][row]; 
 				attackGridThere[2][1] = pieceBoardThere[column][row-1];
 			}
 			else if(row == 0){
 				attackGridThere[1][2] = pieceBoardThere[column-1][row]; attackGridThere[2][2] = pieceBoardThere[column][row]; attackGridThere[3][2] = pieceBoardThere[column+1][row]; 
 				attackGridThere[2][3] = pieceBoardThere[column][row+1]; 
 			}
 			else if(column == 0){
 				attackGridThere[2][1] = pieceBoardThere[column][row-1];
 				attackGridThere[2][2] = pieceBoardThere[column][row]; attackGridThere[3][2] = pieceBoardThere[column+1][row];
 				attackGridThere[2][3] = pieceBoardThere[column][row+1];
 			}
 			else if(column == COLS-1){
 				attackGridThere[2][1] = pieceBoardThere[column][row-1];
 				attackGridThere[1][2] = pieceBoardThere[column-1][row]; attackGridThere[2][2] = pieceBoardThere[column][row];
 				attackGridThere[2][3] = pieceBoardThere[column][row+1]; 
 			}
 			else{
 				attackGridThere[2][1] = pieceBoardThere[column][row-1];
 				attackGridThere[1][2] = pieceBoardThere[column-1][row]; 
 				attackGridThere[2][2] = pieceBoardThere[column][row];
 				attackGridThere[2][3] = pieceBoardThere[column][row+1];
 				attackGridThere[3][2] = pieceBoardThere[column+1][row];
 			}	
 		}
 		
 		/*
 		//System.out.printf("xMax: %d\nyMax: %d\n",xMax,yMax);
 		System.out.printf("column: %d \n ",column);
 		System.out.printf("row: %d \n ",row);
 		System.out.printf("if row and column even or odd: %b \n ",(row%2==0 && column%2==0) || !(row%2==0 || column%2==0));
 		
 		System.out.printf("AttackGrid 12: %b \n ",attackGridThere[1][2]);
 		System.out.printf("AttackGrid 22: %b \n",attackGridThere[2][2]);
 		System.out.printf("AttackGrid 23: %b \n",attackGridThere[2][3]);
 		System.out.printf("AttackGrid 13: %b \n ",attackGridThere[1][3]);
 		
 		System.out.print("AG:\n ");
 		System.out.printf("|%b | %b | %b |\n ",attackGridThere[1][1],attackGridThere[2][1],attackGridThere[3][1]);
 		System.out.printf("|%b | %b | %b |\n ",attackGridThere[1][2],attackGridThere[2][2],attackGridThere[3][2]);
 		System.out.printf("|%b | %b | %b |\n ",attackGridThere[1][3],attackGridThere[2][3],attackGridThere[3][3]);
 		*/
 		for(int i=1;i<4; i++){
 			for(int j=1;j<4;j++){
 				if(attackGridThere[j][i] == false){
 					clearPath[j-1][i-1] = !(attackGridThere[j][i]);
 				}	
 			}
 		}
 		for(int i=0;i<3; i++){
 			for(int j=0;j<3;j++){
 				if(clearPath[j][i] == true){
 					valid = true;
 				}
 			}
 		}
 		/*
 		System.out.print("clearPath:\n ");
 		System.out.printf("|%b | %b | %b |\n ",clearPath[0][0],clearPath[1][0],clearPath[2][0]);
 		System.out.printf("|%b | %b | %b |\n ",clearPath[0][1],clearPath[1][1],clearPath[2][1]);
 		System.out.printf("|%b | %b | %b |\n ",clearPath[0][2],clearPath[1][2],clearPath[2][2]);
 		*/
 		return valid;
 		
 			
 	}
 	private boolean isAttackValid(int column, int row){
 		boolean[][] attackGridThere = new boolean[5][5];
 		PieceColor[][] attackGridColor = new PieceColor[5][5];
 		
 		for (int i = 0; i < 5; i++)
 		{
 		    Arrays.fill( attackGridColor[i], PieceColor.NULL );
 		}
 		
 			//fill 00,02,04,20,24,40,42,44
 			for(int i=0;i<3;i=i++){
 				for(int j=0;j<3;j=j++){
 					if(clearPath[i][j] == true)
 					attackGridThere[2*i][2*j] = pieceBoardThere[column+((i*2)-2)][row+((j*2)-2)];
 					attackGridColor[2*i][2*j] = pieces[column+((i*2)-2)][row+((j*2)-2)].getColor();
 				}
 			}
 			
 			for(int i=0;i<5;i=i+2){
 				for(int j=0;j<5;j=j+2){
 					if(attackGridColor[i][j] ==  PieceColor.NULL){
 						attacksAvailable = false;
 					}
 					else if(attackGridColor[i][j] ==  PieceColor.BLACK){
 						if(pieces[column][row].getColor() == PieceColor.BLACK ){
 							attacksAvailable = false;
 						}
 						else{
 							return true;
 						}
 					}
 					else{
 						if(pieces[column][row].getColor() == PieceColor.WHITE ){
 							return true;
 						}
 						else{
 							attacksAvailable = false;
 						}
 					}
 				}
 
 		}
 	
 		return attacksAvailable;
 	}
 	
 	public void movePiece(GamePiece piece, int x, int y){
 		
 		if(isMoveValid(pieceSelectedCol,pieceSelectedRow)/* &&  isAttackValid(pieceSelectedCol,pieceSelectedRow)*/){
 			//add move to possible attack moves
 			
 			if(pieces[pieceSelectedCol][pieceSelectedRow].getColor() == PieceColor.BLACK){
 				pieces[pieceSelectedCol][pieceSelectedRow].setColor(PieceColor.NULL);
 				pieces[x][y].setColor(PieceColor.BLACK);
 				pieceBoardThere[pieceSelectedCol][pieceSelectedRow] = false;
 				pieceBoardThere[x][y] = true;
 			}
 			else if(pieces[pieceSelectedCol][pieceSelectedRow].getColor() == PieceColor.WHITE){
 				pieces[pieceSelectedCol][pieceSelectedRow].setColor(PieceColor.NULL);
 				pieces[x][y].setColor(PieceColor.WHITE);
 				pieceBoardThere[pieceSelectedCol][pieceSelectedRow] = false;
 				pieceBoardThere[x][y] = true;
 			}
 			updateState();
 		}
 		else if (isMoveValid(pieceSelectedCol,pieceSelectedRow) && !(isAttackValid(pieceSelectedCol,pieceSelectedRow))){
 			//add move to possible moves
 		}
 		/*
 		if(pieces[pieceSelectedCol][pieceSelectedRow].getColor() == PieceColor.BLACK){
 			pieces[pieceSelectedCol][pieceSelectedRow].setColor(PieceColor.NULL);
 			pieces[x][y].setColor(PieceColor.BLACK);
 		}
 		else if(pieces[pieceSelectedCol][pieceSelectedRow].getColor() == PieceColor.WHITE){
 			pieces[pieceSelectedCol][pieceSelectedRow].setColor(PieceColor.NULL);
 			pieces[x][y].setColor(PieceColor.WHITE);	
 		}
 		*/
 		repaint();
 		
 	}
 	
 	public void paintComponent(Graphics g){
 		super.paintComponent(g);
 		int w = getWidth();
         	int h = getHeight();
 		
       	 	int xMax = pieces[COLS-1][ROWS-1].getPosition().x;
        		int yMax = pieces[COLS-1][ROWS-1].getPosition().y;
         
         	//System.out.printf("xMax: %d\nyMax: %d\n",xMax,yMax);
         
         	// center pt: (325, 175)
         
 		// draw horizontal lines
 		for(int y=50; y<=yMax; y+=75) {
 			Graphics2D g2 = (Graphics2D) g;
 			g2.setStroke(new BasicStroke(3));
 			g.drawLine(50,y,xMax,y);	
 		}
 		
 		// draw vertical lines
 		for(int x=50; x<=xMax; x+=75) {
 			Graphics2D g2 = (Graphics2D) g;
 			g2.setStroke(new BasicStroke(3));
 			g.drawLine(x,50,x,yMax);
 		}
 		
 		// draw diagonal lines
 		Graphics2D g2 = (Graphics2D) g;
 		g2.setStroke(new BasicStroke(3));
 		
 		/*
 		// draw from center piece
 		g.drawLine(pieces[COLS/2][ROWS/2].getPosition().x,pieces[COLS/2][ROWS/2].getPosition().y, pieces[COLS/2][ROWS/2].getPosition().x+75,pieces[COLS/2][ROWS/2].getPosition().y+75 );
 		g.drawLine(pieces[COLS/2][ROWS/2].getPosition().x,pieces[COLS/2][ROWS/2].getPosition().y, pieces[COLS/2][ROWS/2].getPosition().x+75,pieces[COLS/2][ROWS/2].getPosition().y-75 );
 		g.drawLine(pieces[COLS/2][ROWS/2].getPosition().x,pieces[COLS/2][ROWS/2].getPosition().y, pieces[COLS/2][ROWS/2].getPosition().x-75,pieces[COLS/2][ROWS/2].getPosition().y+75 );
 		g.drawLine(pieces[COLS/2][ROWS/2].getPosition().x,pieces[COLS/2][ROWS/2].getPosition().y, pieces[COLS/2][ROWS/2].getPosition().x-75,pieces[COLS/2][ROWS/2].getPosition().y-75 );
 		*/
 		
 		g.drawLine(50,200,200,50);
 		g.drawLine(50,350,350,50);
 		g.drawLine(200,350,500,50);
 		g.drawLine(350,350,650,50);
 		g.drawLine(500,350,650,200);
 		
 		g.drawLine(650,200,500,50);
 		g.drawLine(650,350,350,50);
 		g.drawLine(500,350,200,50);
 		g.drawLine(350,350,50,50);
 		g.drawLine(200,350,50,200);
 		
 		
 		// draw pieces
 		for(int y=0; y<ROWS; y++) {
 			for(int x=0; x<COLS; x++) {
 				if(pieces[x][y].getColor()!=PieceColor.NULL) {
 					if(pieces[x][y].getColor() == PieceColor.BLACK)
 						g.setColor(Color.black);
 					if(pieces[x][y].getColor() == PieceColor.WHITE)
 						g.setColor(Color.white);
 					//g.drawOval(pieces[x][y].getPosition().x-25, pieces[x][y].getPosition().y-25, 50, 50);
 					g.fillOval(pieces[x][y].getPosition().x-25, pieces[x][y].getPosition().y-25, 50, 50);
 				}
 			}
 		}
 		
 		if(pieceSelectedCol != -1 && pieceSelectedRow != -1){
 			g.setColor(Color.red);
 			g.drawOval(pieces[pieceSelectedCol][pieceSelectedRow].getPosition().x-25,
 					pieces[pieceSelectedCol][pieceSelectedRow].getPosition().y-25, 50, 50);
 		}
 	}
 
 	private class BoardAdapter extends MouseAdapter {
 		public void mouseClicked(MouseEvent me){
 			int mouseX = me.getX();
 			int mouseY = me.getY();
 			int mouseCol = -1;
 			int mouseRow = -1;
 			
 			for (int i = 0; i < piecePositions.length; i++){
 				if(mouseX > (piecePositions[i][0].x - 25) && mouseX < (piecePositions[i][0].x + 25)){
 					mouseCol = i;
 				}
 			}
 			for (int i = 0; i < piecePositions[0].length; i++){
 				if(mouseY > (piecePositions[0][i].y - 25) && mouseY < (piecePositions[0][i].y + 25)){
 					mouseRow = i;
 				}
 			}
 			//System.out.printf("Column Clicked: %d\nRow Clicked: %d\n", mouseCol, mouseRow);
 			//Handle piece selection
 			switch(boardState){
 			case P1_Turn:
 				if(mouseCol > -1 && mouseRow > -1 && player1.isUser()){
 					/*//Print to console for debugging purposes
 					if(pieces[mouseCol][mouseRow].getColor() == PieceColor.NULL)
 						System.out.println("Nothing to see here!");
 					if(pieces[mouseCol][mouseRow].getColor() == PieceColor.WHITE)
 						System.out.println("White piece clicked!");
 					if(pieces[mouseCol][mouseRow].getColor() == PieceColor.BLACK)
 						System.out.println("Black piece clicked!");*/
 				//Start cases for selecting a piece
 					//No piece selected, pick the piece that was clicked if it belongs to player
 					if(pieceSelectedCol == -1 && pieceSelectedRow == -1){
 						if(pieces[mouseCol][mouseRow].getColor() == player1.whichColor()){
 							pieceSelectedCol = mouseCol;
 							pieceSelectedRow = mouseRow;
 						}
 					}
 					//Piece selected, but user selects another piece
 					else if(pieces[mouseCol][mouseRow].getColor() == player1.whichColor()){
 						pieceSelectedCol = mouseCol;
 						pieceSelectedRow = mouseRow;
 					}
 					//User clicks other players piece, de-select current selected piece
 					else if(pieces[mouseCol][mouseRow].getColor() == player2.whichColor()){
 						pieceSelectedCol = -1;
 						pieceSelectedRow = -1;
 					}
 					else if(pieces[mouseCol][mouseRow].getColor() == PieceColor.NULL){
 						movePiece(pieces[pieceSelectedCol][pieceSelectedRow],mouseCol, mouseRow);
 						pieceSelectedCol = -1;
 						pieceSelectedRow = -1;
 					}
 				}
 				break;
 			case P2_Turn:
 				if(mouseCol > -1 && mouseRow > -1 && player2.isUser()){
 					//Print to console for debugging purposes
 					/*if(pieces[mouseCol][mouseRow].getColor() == PieceColor.NULL)
 						System.out.println("Nothing to see here!");
 					if(pieces[mouseCol][mouseRow].getColor() == PieceColor.WHITE)
 						System.out.println("White piece clicked!");
 					if(pieces[mouseCol][mouseRow].getColor() == PieceColor.BLACK)
 						System.out.println("Black piece clicked!");*/
 				//Start cases for selecting a piece
 					//No piece selected, pick the piece that was clicked if it belongs to player
 					if(pieceSelectedCol == -1 && pieceSelectedRow == -1){
 						if(pieces[mouseCol][mouseRow].getColor() == player2.whichColor()){
 							pieceSelectedCol = mouseCol;
 							pieceSelectedRow = mouseRow;
 						}
 					}
 					//Piece selected, but user selects another piece
 					else if(pieces[mouseCol][mouseRow].getColor() == player2.whichColor()){
 						pieceSelectedCol = mouseCol;
 						pieceSelectedRow = mouseRow;
 					}
 					//User clicks other player's piece, de-select current selected piece
 					else if(pieces[mouseCol][mouseRow].getColor() == player1.whichColor()){
 						pieceSelectedCol = -1;
 						pieceSelectedRow = -1;
 					}
 					//User clicks empty space, attempt to move selected piece there
 					else if(pieces[mouseCol][mouseRow].getColor() == PieceColor.NULL){
 						movePiece(pieces[pieceSelectedCol][pieceSelectedRow],mouseCol, mouseRow);
 						pieceSelectedCol = -1;
 						pieceSelectedRow = -1;//Deselect space no matter what happens
 					}
 				}
 				break;
 			case Tie:
 				break;
 			}
 			repaint();
 		}
 	}
 	
 	private void doAIMove(Player ai){
 		System.out.println("Doing an AI Move");
 		boolean hasMoved = false;
 		Random rn = new Random();
 		int tries = 0;
 		while(!hasMoved && tries < 1000){
 			int aiPieceCol = rn.nextInt(COLS);
 			int aiPieceRow = rn.nextInt(ROWS);
 			if(pieces[aiPieceCol][aiPieceRow].getColor() != ai.whichColor()){
 				tries++;
 				continue;
 			}
 			if(!isMoveValid(aiPieceCol, aiPieceRow)){
 				tries++;
 				continue;
 			}
 			pieceSelectedCol = aiPieceCol;
 			pieceSelectedRow = aiPieceRow;
 			while(!hasMoved && tries < 1000){
 				int moveToCol = rn.nextInt(COLS);
 				int moveToRow = rn.nextInt(ROWS);
 				if (pieces[moveToCol][moveToRow].getColor() != PieceColor.NULL)
 					continue;
 				movePiece(pieces[pieceSelectedCol][pieceSelectedRow], moveToCol, moveToRow);
 				if(pieces[aiPieceCol][aiPieceRow].getColor() == PieceColor.NULL)
 					hasMoved = true;
 				tries++;
 			}
 		}
		if(tries >= 1000 && !hasMoved) return;
 		pieceSelectedCol = -1;
 		pieceSelectedRow = -1;
 		repaint();
 	}
 	
 	private void updateState(){
 		switch (boardState){
 		case Init:
 			boardState = GameState.P1_Turn;
 			if(!(player1.isUser())) doAIMove(player1);
 			break;
 		case P1_Turn:
 			if(turnCounter >= 5){
 				boardState = GameState.Tie;
 				updateState();
 				break;
 			}
 			boardState = GameState.P2_Turn;
 			System.out.println("Player 2 turn");
 			if(!(player2.isUser())){
 				doAIMove(player2);
 			}
 			break;
 		case P2_Turn:
 			if(turnCounter >= 5){
 				boardState = GameState.Tie;
 				updateState();
 				break;
 			}
 			boardState = GameState.P1_Turn;
 			System.out.println("PLayer 1 turn");
 			if(!(player1.isUser())){
 				doAIMove(player1);
 			}
 			break;
 		case Tie:
 			JOptionPane.showMessageDialog(this, "Game tied, Please Start a new game");
 			break;
 		}
 		turnCounter++;
 	}
 	
 	private GamePiece[][] pieces;
 	private boolean[][] pieceBoardThere;
 	private boolean[][] clearPath = new boolean[3][3];
 	private boolean attacksAvailable;
 	//private boolean hasMoved;
 	private Player player1;
 	private Player player2;
 	private Position[][] piecePositions;
 	private int pieceSelectedCol = -1;
 	private int pieceSelectedRow = -1;
 	private int ROWS;
 	private int COLS;
 	private int turnCounter;
 	private GameState boardState;
 }
