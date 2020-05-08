 package net.sipty.tictactoe;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.math.MathUtils;
 
 public class Mech {
 		
 	// declarations
 	private static Texture ice, fire;
 	
 	private static int side, ai=2, winnerIs, rnd, isBoardFull=0/*it is at isBoardFull==9*/;
 	
	private static Boolean noWin=true, goEasyOnMe=true;
 
 	public static int[][] box, boxCoords;
 	
 	
 	// constructor
 	public Mech() {
 		ice = new Texture(Gdx.files.internal("ice.png"));
 		fire = new Texture(Gdx.files.internal("fire.png"));
 		box = new int[10][4];	// c.0 is the box number, c.1 is the side, c.2 x coord, c.3 y coord
 							// NOTE: boxes start counting up from 1, thus making the array size ([10][4]) important!!!
 		boxCoords = new int[10][2];	//c.0 = x, c.1 = y
 		// fil the box's coords
 		boxCoords[1][0]= GameScreen.x1;	boxCoords[1][1]= GameScreen.y1;
 		boxCoords[2][0]= GameScreen.x2;	boxCoords[2][1]= GameScreen.y1;
 		boxCoords[3][0]= GameScreen.x3;	boxCoords[3][1]= GameScreen.y1;
 		boxCoords[4][0]= GameScreen.x1;	boxCoords[4][1]= GameScreen.y2;
 		boxCoords[5][0]= GameScreen.x2;	boxCoords[5][1]= GameScreen.y2;
 		boxCoords[6][0]= GameScreen.x3;	boxCoords[6][1]= GameScreen.y2;
 		boxCoords[7][0]= GameScreen.x1;	boxCoords[7][1]= GameScreen.y3;
 		boxCoords[8][0]= GameScreen.x2;	boxCoords[8][1]= GameScreen.y3;
 		boxCoords[9][0]= GameScreen.x3;	boxCoords[9][1]= GameScreen.y3;
 		
 		// other version - note that there is something wrong with it
 		/*for(int i=0; i<7; i+=3) {
 			boxCoords[i][0]=GameScreen.x1;
 			boxCoords[i+1][0]=GameScreen.x2;
 			boxCoords[i+2][0]=GameScreen.x3;
 		}
 		for(int i=0; i<3; i++) {
 			boxCoords[i][1]=GameScreen.x1;
 			boxCoords[i+3][1]=GameScreen.x2;
 			boxCoords[i+6][1]=GameScreen.x3;
 		}*/
 	}
 	
 	
 	
 	// draw the tics/tacs
 	public static void tick() {
 		// go through each box on the board
 		for(int i=0; i<10; i++) {
 			//if box has been drawn on
 			if(box[i][0] == 1) {
 				// ice
 				if(box[i][1] == 1) {
 					GameScreen.game.batch.draw(ice, box[i][2], box[i][3]);
 ;				}
 				// fire
 				else {
 					GameScreen.game.batch.draw(fire, box[i][2], box[i][3]);
 				}
 			}
 		}
 	}
 	
 	// check win
 	public static void checkWin() {	// check if winning conditions have been met
 			
 		if(noWin) {	// if the game is over, no need to loop through the winning conditions
 			// check for draw
 			if(isBoardFull==9) {
 				noWin = false;
 				winnerIs = 3; 
 			}
 			// check winning condition in the following format (b1 is an element && b1=b2 && b1==b3)
 			for(int i=1; i<10; i++) {
 				// NOTE: winning condition check is accessed only if the first floor is ticked, in the attempt to save some computation power
 		
 				// columns
 				if((box[i][0] !=0) && i<4) {		
 					if((box[i][1] == box[i+3][1]) && (box[i][1] == box[i+6][1])) {
 						noWin = false;
 						winnerIs = box[i][1]; 
 					}
 				}
 				// rows
 				if((box[i][0] !=0) && (i==1 || i==4 || i==7)) {		
 					if((box[i][1] == box[i+1][1]) && (box[i][1] == box[i+2][1])) {
 						noWin = false;
 						winnerIs = box[i][1]; 
 					}
 				}
 				// diagonal 1->9
 				if((box[i][0] !=0) && i==1) {		
 					if((box[i][1] == box[i+4][1]) && (box[i][1] == box[i+8][1])) {
 						noWin = false;
 						winnerIs = box[i][1]; 
 					}
 				}
 				// diagonal 3->7
 				if((box[i][0] !=0) && i==3) {		
 					if((box[i][1] == box[i+2][1]) && (box[i][1] == box[i+4][1])) {
 						noWin = false;
 						winnerIs = box[i][1]; 
 					}
 				}
 			}
 		}
 	}
 	// AI
 	private static void easyAI() {
 		// now moving to the ai
 		do {	// pick random box
 			rnd = MathUtils.random(1,9);
 		}while(box[rnd][0] == 1);	// until an empty box has been found
 		isBoardFull++;
 		aiBoxFill(rnd);
 		checkWin();
 	}
 	private static void aiBoxFill(int i) {
 		// fill the box with:
 		box[i][0] = 1;	// a 'box has been turned on' sign
 		if(side==1)		// the opposite side mark
 			box[i][1] = 2;
 		else
 			box[i][1] = 1;								
 		box[i][2] = boxCoords[i][0]; 	// the box's x coordinates
 		box[i][3] = boxCoords[i][1]; 	// the box's y coordinates
 	}	
 	
 	// box[i][0] - is box turned on
 	// box[i][1] - what side has the box
 	// box[i][2/3] - coords
 	
 		// i=number of box, plus1/2 = the original box + how much to get a line, check1/2/3 = what box numbers are needed to start the calculation, 
 		// isDiagon is a different method for diagonals							/* eg for a row, we would start from either 1, 4 or 7 */
 	private static Boolean aiTripleBoxCheck(int i, int plus1, int plus2, int check1, int check2, int check3, Boolean isDiagon) {
 		// with the three OR statements we check if this row/column/diagonal
 		// can be used for a win next turn and if so:
 		// -indicate that we've seized another value from the box isBoardFull++
 		// -do the box filling ritual via aiBoxFill(i)
 		// -check if we've won!
 		if(((i==check1) || (i==check2) || (i==check3))) {			// the starting positions  
 			if(check2+plus2<10 && check3+plus1<10 && check3+plus2<10) {	// out of bounds security check
 				if((box[i][0]!=0) && (box[i][1] == box[i+plus1][1]) && (box[i+plus2][0]==0)) {
 					isBoardFull++;
 					aiBoxFill(i+plus2);
 					checkWin();
 					return true;
 				}
 			}	
 			if(check2+plus2<10 && check3+plus1<10 && check3+plus2<10) {	// out of bounds security check
 				if((box[i][0]!=0) && (box[i][1] == box[i+plus2][1]) && (box[i+plus1][0]==0)) {
 					isBoardFull++;
 					aiBoxFill(i+plus1);
 					checkWin();
 					return true;
 				}
 			}
 			if(check2+plus2<10 && check3+plus1<10 && check3+plus2<10) {	// out of bounds security check
 				if((box[i+plus1][0]!=0) && (box[i+plus1][1] == box[i+plus2][1]) && (box[i][0]==0)) {
 					isBoardFull++;
 					aiBoxFill(i);
 					checkWin();	
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 	private static void ai(int b, int x, int y) {
 		switch(ai) {
 		// local ai
 			case 0:
 				if(side==2)
 					side = 1;
 				else
 					side = 2;
 				
 				box[b][2] = x;	// x coord of ticked box
 				box[b][3] = y;	// y cood of ticked box
 				isBoardFull++;
 				checkWin();
 				break;
 		// easy ai	
 			case 1:	// it marks random boxes				
 				// player's tick
 				box[b][2] = x;	// x coord of ticked box
 				box[b][3] = y;	// y cood of ticked box
 				isBoardFull++;
 				checkWin();
 				if(noWin) {					
 					easyAI();
 					break;
 				}
 		// medium ai
 			case 2:	// blocks the player				
 				// player's tick
 				box[b][2] = x;	// x coord of ticked box
 				box[b][3] = y;	// y cood of ticked box
 				isBoardFull++;
 				checkWin();
 				if(noWin) {
 					// check for winning lines
 					for(int i=1; i<10; i++) {	// go through the starting positions and decide if expansion is necessary
 						// columns
 						if(aiTripleBoxCheck(i, 3, 6, 1, 2, 3, false)) {	
 							goEasyOnMe = false;
 							break;
 						}
 						// rows
 						if(aiTripleBoxCheck(i, 1, 2, 1, 4, 7, false)) {	
 							goEasyOnMe = false;
 							break;
 						}
 						//diagonal 1->9
 						if(aiTripleBoxCheck(i, 4, 8, 1, 5, 9, true))	{	
 							goEasyOnMe = false;
 							break;
 						}
 						// diagonal 3->7
 						if(aiTripleBoxCheck(i, 2, 4, 3, 5, 7, true))	{	
 							goEasyOnMe = false;
 							break;
 						}
 					}
 					// if we don't find any winning lines fill a random empty box
 					if(goEasyOnMe) {
 						easyAI();
 						break;
 					}
 				}
 		}
 	}
 	
 	// setters & getters:
 	// box array
 	public static void setBox(int b, int x, int y) {	// provides all info needed regarding the board
 		if(noWin) {	// don't allow changes to the board, if there is a winner
 			box[b][0] = 1;	// box has been turned on
 			if(box[b][1] == 0) {	// makes sure that sides can not be changed mid game
 				box[b][1] = side;	// ticked by (side)		1-ice, 2-fire
 				ai(b, x, y);
 			}
 		}
 	}
 	
 	public static int getWinnerIs() {
 		return winnerIs;
 	}
 	public static Boolean getNoWin() {
 		return noWin;
 	}
 	public static void setAi(int ai) {
 		Mech.ai = ai;
 	}
 	public static int getAi() {
 		return ai;
 	}
 	// side int
 	public static void setSide(int side) {	// used by the player to choose his side
 		Mech.side = side;
 	}
 	
 	
 }
