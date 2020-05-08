 
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 import java.util.*;
 enum MOVE {FOWARD, BACK, LEFT, RIGHT, DF_RIGHT, DF_LEFT, DB_RIGHT, DB_LEFT};
 public class Board extends JPanel {
 	public Board(){
 		/* initialize game state here; create pieces,
 		 * populate board, make players, get everything setup
 		 */
 		 
 		 //set up the white pieces need to figure out how to set all the x and y positions
 		int xPos = 0;
 		int yPos = 0;
 		int ROWS = 5;
 		int COLS = 9;
 		pieces = new GamePiece[21];
 		 for(int i=0;i < 11; i++){
 		 GamePiece tempPiece = new GamePiece(xPos, yPos, PieceColor.WHITE);
 		 pieces[i] = tempPiece;
 		 }
 		 //set up the black pieces need to figure out how to set all the x and y positions
 		 for(int k=11; k < 21; k++){
 		 GamePiece tempPiece = new GamePiece(xPos, yPos, PieceColor.BLACK);
 		 pieces[k] = tempPiece;
 		 }
 		 
 		pieceBoardThere = new boolean[ROWS][COLS]; 
 		for(int i = 0; i < pieceBoardThere.length - 1; i++)
 			for(int j = 0; i < pieceBoardThere.length - 1; i++)
				pieceBoardThere[3][5] = false;
 		
 		pieceBoardColor = new PieceColor[][]{{PieceColor.BLACK,PieceColor.BLACK,PieceColor.BLACK,PieceColor.BLACK,PieceColor.BLACK,PieceColor.BLACK,PieceColor.BLACK,PieceColor.BLACK,PieceColor.BLACK},{PieceColor.BLACK,PieceColor.BLACK,PieceColor.BLACK,PieceColor.BLACK,PieceColor.BLACK,PieceColor.BLACK,PieceColor.BLACK,PieceColor.BLACK,PieceColor.BLACK}
 		,{PieceColor.BLACK,PieceColor.WHITE,PieceColor.BLACK,PieceColor.WHITE,PieceColor.NULL,PieceColor.BLACK,PieceColor.WHITE,PieceColor.BLACK,PieceColor.WHITE},{PieceColor.WHITE,PieceColor.WHITE,PieceColor.WHITE,PieceColor.WHITE,PieceColor.WHITE,PieceColor.WHITE,PieceColor.WHITE,PieceColor.WHITE,PieceColor.WHITE},{PieceColor.WHITE,PieceColor.WHITE,PieceColor.WHITE,PieceColor.WHITE,PieceColor.WHITE,PieceColor.WHITE,PieceColor.WHITE,PieceColor.WHITE,PieceColor.WHITE}};
 	
 	}
 	// still not done has working checking blocked in and if opposite color is within 2 squaures
 	private boolean isMoveValid(GamePiece piece , MOVE move){
 		//the valid move checker
 		int positionX, positionY;
 		positionX = piece.getXPosition();
 		positionY = piece.getYPosition();
 		boolean valid = false;
 		//int[][] gridBoard = new int[][];
 		boolean[][] attackGridThere = new boolean[5][5];
 		PieceColor[][] attackGridColor = new PieceColor[5][5];
 		/*(bool,Color)[][] pieceBoard = new (bool,Color)[][] {{(true,BLACK),(true,BLACK),(true,BLACK),(true,BLACK),(true,BLACK),(true,BLACK),(true,BLACK),(true,BLACK),(true,BLACK)}
 		,{(true,BLACK),(true,BLACK),(true,BLACK),(true,BLACK),(true,BLACK),(true,BLACK),(true,BLACK),(true,BLACK),(true,BLACK)},{(true,BLACK),(true,WHITE),(true,BLACK),(true,WHITE),(true,NULL),(true,BLACK),(true,WHITE),(true,BLACK),(true,WHITE)}
 		,{(true,WHITE),(true,WHITE),(true,WHITE),(true,WHITE),(true,WHITE),(true,WHITE),(true,WHITE),(true,WHITE),(true,WHITE)},{(true,WHITE),(true,WHITE),(true,WHITE),(true,WHITE),(true,WHITE),(true,WHITE),(true,WHITE),(true,WHITE),(true,WHITE)}};
 		*/
 		/*
 		boolean[][] pieceBoard = new boolean[ROWS][COLS]; 
 		Arrays.fill(pieceBoard, true);
 		pieceBoard[3][5] = false;
 		*/
 		/*
 		
 		for(int j=0;j<attackGrid.length + 1; j++){
 			for(int i=0;i<attackGrid[0].length + 1;i++){
 				attackGrid[j][i] = pieceBoard[positionX-2][positionY-2];
 			}
 		}
 		attackGrid[0][0] = pieceBoard[positionX-2][positionY-2]; attackGrid[0][2] = pieceBoard[positionX-2][positionY]; attackGrid[0][4] = pieceBoard[positionX-2][positionY+2];
 		attackGrid[1][1] = pieceBoard[positionX-1][positionY-2]; attackGrid[1][2] = pieceBoard[positionX-1][positionY]; attackGrid[1][3] = pieceBoard[positionX-1][positionY+1];
 		attackGrid[2][0] = pieceBoard[positionX][positionY-2]; attackGrid[2][1] = pieceBoard[positionX][positionY-2]; attackGrid[2][2] = pieceBoard[positionX][positionY]; attackGrid[2][3] = pieceBoard[positionX][positionY+1]; attackGrid[2][4] = pieceBoard[positionX][positionY+2];
 		attackGrid[3][1] = pieceBoard[positionX+1][positionY-2]; attackGrid[3][2] = pieceBoard[positionX+1][positionY]; attackGrid[3][3] = pieceBoard[positionX+1][positionY+1];
 		attackGrid[4][0] = pieceBoard[positionX+2][positionY-2]; attackGrid[4][2] = pieceBoard[positionX+2][positionY]; attackGrid[4][4] = pieceBoard[positionX+2][positionY+2];
 		*/
 		if(positionX == 0){
 			//fill 11-23
 			attackGridThere[1][1] = pieceBoardThere[positionX-1][positionY-2]; attackGridThere[1][2] = pieceBoardThere[positionX-1][positionY]; attackGridThere[1][3] = pieceBoardThere[positionX-1][positionY+1];
 			attackGridThere[2][1] = pieceBoardThere[positionX][positionY-2]; attackGridThere[2][2] = pieceBoardThere[positionX][positionY]; attackGridThere[2][3] = pieceBoardThere[positionX][positionY+1]; 
 		}
 		else if(positionX == 4){
 			//fill 21-33
 			attackGridThere[2][1] = pieceBoardThere[positionX][positionY-2]; attackGridThere[2][2] = pieceBoardThere[positionX][positionY]; attackGridThere[2][3] = pieceBoardThere[positionX][positionY+1];
 			attackGridThere[3][1] = pieceBoardThere[positionX+1][positionY-2]; attackGridThere[3][2] = pieceBoardThere[positionX+1][positionY]; attackGridThere[3][3] = pieceBoardThere[positionX+1][positionY+1];			 
 		}
 		else if(positionY == 0){
 			//fill 12-33
 			attackGridThere[1][2] = pieceBoardThere[positionX-1][positionY]; attackGridThere[1][3] = pieceBoardThere[positionX-1][positionY+1];
 			attackGridThere[2][2] = pieceBoardThere[positionX][positionY]; attackGridThere[2][3] = pieceBoardThere[positionX][positionY+1];
 			attackGridThere[3][2] = pieceBoardThere[positionX+1][positionY]; attackGridThere[3][3] = pieceBoardThere[positionX+1][positionY+1];
 		}
 		else if(positionY == 8){
 			//fill 11-32
 			attackGridThere[1][1] = pieceBoardThere[positionX-1][positionY-2]; attackGridThere[1][2] = pieceBoardThere[positionX-1][positionY];
 			attackGridThere[2][1] = pieceBoardThere[positionX][positionY-2]; attackGridThere[2][2] = pieceBoardThere[positionX][positionY];
 			attackGridThere[3][1] = pieceBoardThere[positionX+1][positionY-2]; attackGridThere[3][2] = pieceBoardThere[positionX+1][positionY]; 
 		}
 		else{
 			//fill all
 			attackGridThere[1][1] = pieceBoardThere[positionX-1][positionY-2]; attackGridThere[1][2] = pieceBoardThere[positionX-1][positionY]; attackGridThere[1][3] = pieceBoardThere[positionX-1][positionY+1];
 			attackGridThere[2][1] = pieceBoardThere[positionX][positionY-2]; attackGridThere[2][2] = pieceBoardThere[positionX][positionY]; attackGridThere[2][3] = pieceBoardThere[positionX][positionY+1];
 			attackGridThere[3][1] = pieceBoardThere[positionX+1][positionY-2]; attackGridThere[3][2] = pieceBoardThere[positionX+1][positionY]; attackGridThere[3][3] = pieceBoardThere[positionX+1][positionY+1];
 		}
 		
 		for(int j=1;j<attackGridThere.length - 1; j++){
 			for(int i=1;i<attackGridThere[0].length - 1;i++){
 				if(pieceBoardThere[i][j] == true)
 				valid = false;
 				else
 				valid = true;
 			}
 		}
 		
 		if(valid == true){
 			//fill 00,02,04,20,24,40,42,44
 			for(int i=0;i<5;i=i+2){
 				for(int j=0;j<5;j=j+2){
 					attackGridThere[i][j] = pieceBoardThere[positionX+(i-2)][positionY+(j-2)];
 					attackGridColor[i][j] = pieceBoardColor[positionX+(i-2)][positionY+(j-2)];
 				}
 			}
 			
 			for(int i=0;i<5;i=i+2){
 				for(int j=0;j<5;j=j+2){
 					if(attackGridColor[i][j] ==  piece.getColor()){
 						attacksAvailable = false;
 					}
 					else
 						attacksAvailable = true;
 				}
 			}
 			// check if any of those are oppostie color and a clear path
 		}
 		
 		if(valid == false){
 			
 		}
 		/*
 		if(piece.getColor() == BLACK)
 					return false;
 				}
 				else{
 					return true;
 					
 				}
 			*/
 			
 			// know locked in pieces
 			// if not locked in does emimy piece within 2 places
 			// if around emepny pieces does have a clear move
 			// create a 2D array of bool that checks can attack chekcing a 5by5 grid around each piece
 			
 		
 		
 		return valid;
 	}
 	
 	public void movePiece(/*need to figure out what goes here*/){
 		if(/*isMoveValid(move stuff)*/true){
 			//do move
 		}
 	}
 	
 	public void paintComponent(Graphics g){
 		super.paintComponent(g);
 		int w = getWidth();
         int h = getHeight();
 		
         // center pt: (325, 175)
         
 		// draw horizontal lines
 		for(int y=50; y<=375; y+=75) {
 			Graphics2D g2 = (Graphics2D) g;
 			g2.setStroke(new BasicStroke(3));
 			g.drawLine(50,y,650,y);
 		}
 		
		// draw vertical lines
 		for(int x=50; x<=650; x+=75) {
 			Graphics2D g2 = (Graphics2D) g;
 			g2.setStroke(new BasicStroke(3));
 			g.drawLine(x,50,x,350);
 		}
 		
 		// draw diagonal lines
 		Graphics2D g2 = (Graphics2D) g;
 		g2.setStroke(new BasicStroke(3));
 		
 		// left half of board
 		for(int x=0; x<=275; x+=75) {
 			g.drawLine(50,125+x,125+x,50);
 		}
 		for(int x=0; x<=200; x+=75) {
 			g.drawLine(350,275-x,275-x,350);
 		}
 		for(int x=0; x<=275; x+=75) {
 			g.drawLine(275-x,50,350,125+x);
 		}
 		for(int x=0; x<=200; x+=75) {
 			g.drawLine(50,275-x,125+x,350);
 		}
 		
 		// right half of board
 		for(int x=0; x<=275; x+=75){
 			g.drawLine(575-x,50,650,125+x);
 		}
 		for(int x=0; x<=200; x+=75){
 			g.drawLine(350,275-x,425+x,350);
 		}
 		for(int x=0; x<=275; x+=75){
 			g.drawLine(425+x,50,350,125+x);
 		}
 		for(int x=0; x<=200; x+=75){
 			g.drawLine(650,275-x,575-x,350);
 		}
 		
 		
 		// draw top pieces
 		g.setColor(Color.white);
 		for(int x=50; x<=1350; x+=150){
 			g.drawOval(x/2,25,50,50);
 			g.fillOval(x/2,25,50,50);
 		}
 		
 		// draw bottom pieces
 		g.setColor(Color.black);
 		for(int x=50; x<=1350; x+=150){
 			g.drawOval(x/2,325,50,50);
 			g.fillOval(x/2,325,50,50);
 		}
 		
 	}
 	
 	private GamePiece[] pieces;
 	private boolean[][] pieceBoardThere;
 	private PieceColor[][] pieceBoardColor;
 	private boolean attacksAvailable;
 	private Player player1;
 	private Player player2;
 
 }
