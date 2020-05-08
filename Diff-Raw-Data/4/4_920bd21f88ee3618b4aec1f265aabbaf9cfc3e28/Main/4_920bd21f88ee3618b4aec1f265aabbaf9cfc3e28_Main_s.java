 import java.util.Scanner;
 import java.util.Date;
 
 class Main{
 	public static void main(String args[]){
 
 		System.out.println("\nWelcome to Team35's Fanorona game");
 		System.out.println("X=black  0=white, N='null'(no piece)\n");
 
 		Fanorona game = new Fanorona(9,5);
 		
 		//GameWindow gw = new GameWindow(5,5);
 		
 		
 		/* TODO change the rows and columns to start at 1 instead of 0 */
 
 		game.prettyprint();
 
 		int turn = 0;
 		int numberOfBlackMovesThisTurn = 0;
 		while(true){
 			
 			System.out.println(game.numberOfMoves());
 
 			long time1 = new Date().getTime();
 			long time2 = -1;
 
 			game.printScore();
 			String move = "";
 			if(game.capturingMoveAvailable()) System.out.println("Capturing move required");
 
 
 			int pieceInt1 = -1;
 			int pieceInt2 = -1;
 
 			String pieceStr1 = "";
 			String pieceStr2 = "";
 
 			Scanner scan2;
 			Scanner scan;
 
 			String playerInput;
 
 			boolean moveturn = true; //indicates that a move as been entered
 
 
 			if(game.activePlayer() == Piece.Type.BLACK){
 				//random
 				
 				System.out.println("evaluation: "+game.evaluate());
 				
 				MiniMaxTree mmt = new MiniMaxTree(game.copyGame());
 				mmt.processToDepth(2);
 				
 				String bestMove = "";
 				int minimum = 100000;
 				for(int i = 0; i < mmt.root.children.size();i++){
 					if(mmt.root.children.get(i).getUtilityValue() < minimum){
 						bestMove = mmt.root.children.get(i).getState().getMove(false);
 						minimum = mmt.root.children.get(i).getUtilityValue();
 					}
 				}
 				
 				System.out.println("best move: "+bestMove);
 				
 				for(int k = 0; k <= numberOfBlackMovesThisTurn; k++){
 					int temp = bestMove.indexOf('+');
 					if(temp == -1){
 						move = bestMove;
 						break;
 					}
 					move = bestMove.substring(0,temp);
 					bestMove = bestMove.substring(temp+2,bestMove.length());
 				}
 				
 				numberOfBlackMovesThisTurn++;
 				//move = game.getRandomMove();
 				System.out.println("Black move: "+move);
 				
 				
 				time2 = new Date().getTime();
 				
 			}
 			else{
 				System.out.print("Enter a move: ");
 
 				playerInput = "";
 				scan2 = new Scanner(System.in);
 				playerInput = scan2.nextLine();
 				scan = new Scanner(playerInput);
 
 				time2 = new Date().getTime();
 
 				if(playerInput.equals("quit")){
 					System.out.println("\nExiting\n\n");
 					scan.close();
 					scan2.close();
 					System.exit(0);
 				}
 				else if(playerInput.equals("moves")){
					//System.out.println("\nwhite: "+game.board.whiteMovesFull);
					//System.out.println("\nblack: "+game.board.blackMovesFull);
 					moveturn = false;
 					turn--;
 				}
 				else if(playerInput.equals("reset")){
 					moveturn = false;
 					game = new Fanorona(5,9);
 					System.out.println("Starting a new game");
 					turn = 0;
 				}
 				else{
 
 					try {
 //			        	pieceInt1 = scan.nextInt();
 //			        	pieceInt2 = scan.nextInt();
 //
 //			        	pieceStr1 = Integer.toString(pieceInt1);
 //			        	pieceStr2 = Integer.toString(pieceInt2);
 //
 //			        	if(pieceInt1 < 10){
 //			        		pieceStr1 = "0"+pieceStr1;
 //			        	}
 //			        	if(pieceStr1.length() != 2){
 //			        		pieceStr1 = pieceStr1+="0";
 //			        	}
 //
 //			        	if(pieceInt2 < 10){
 //			        		pieceStr2 = "0"+pieceStr2;
 //			        	}
 //			        	if(pieceStr2.length() != 2){
 //			        		pieceStr2 = pieceStr2+="0";
 //			        	}
 //
 //			        	playerInput = scan.nextLine();
 //
 //
 //				      	move = pieceStr1 + " " + pieceStr2 + playerInput;
 						move = scan.nextLine();
 				    }
 				    catch(Exception e){
 	        			System.out.println("Error: " + e.getMessage());
 	      			}
 	      		}
 	      	}
 			boolean valid = true;
       		if(moveturn){
 
 //		      	int row1 = move.charAt(0)-48;
 //		      	int col1 = move.charAt(1)-48;
 //		      	int row2 = move.charAt(3)-48;
 //		      	int col2 = move.charAt(4)-48;
 //		      	char moveType = move.charAt(6);
       			
       			int row1;
       			int row2;
       			int col1;
       			int col2;
       			char moveType;
       			
       			if(game.activePlayer() == Piece.Type.WHITE){
 	      			
 	      			row1 = game.board.rows-Fanorona.getFirstRowCMD(move);
 	      			col1 = Fanorona.getFirstColumnCMD(move)-1;
 	      			row2 = game.board.rows-Fanorona.getSecondRowCMD(move);
 	      			col2 = Fanorona.getSecondColumnCMD(move)-1;
 	      			moveType = Fanorona.getMoveType(move);
 	      			
 	      			move = moveType + " " + row1 + " " + col1 + " "+row2 + " " +col2;
       			}
       			else{
       				
       				row1 = Fanorona.getFirstRow(move);
 	      			col1 = Fanorona.getFirstColumn(move);
 	      			row2 = Fanorona.getSecondRow(move);
 	      			col2 = Fanorona.getSecondColumn(move);
 	      			moveType = Fanorona.getMoveType(move);
       				
       			}
       			
 		      	
 
 		      	valid = game.validMoveSystax(move);
 
 		      	if(!valid){
 		      		System.out.println("not valid");
 		      	}
 		      	else{
 
 		      		if(game.activePlayer() == Piece.Type.WHITE){
 		      			game.player1Time += (time2-time1);
 		      		}
 		      		else{
 		      			game.player2Time += (time2-time1);
 		      		}
 
 		      		if(game.capturingMoveAvailable()){
 			      		if(game.isPossibleCapturingMove(row1, col1, row2, col2, moveType)){
 			      			Piece.Type previous = game.activePlayer();
 			      			boolean successiveMove = game.move(row1, col1, row2, col2, moveType);
 			      			if(previous == Piece.Type.BLACK && game.activePlayer() == Piece.Type.WHITE){
 			      				numberOfBlackMovesThisTurn = 0;
 			      			}
 			      			if(successiveMove){
 
 			      			}
 			      		}
 			      		else{
 			      			System.out.println("A capturing move must be entered\n");
 			      			valid = false;
 			      		}
 			      	}
 			      	else{
 			      		game.move(row1,col1,row2,col2,moveType);
 			      	}
 		      		if(game.board.numberRemaining(Piece.Type.WHITE) == 0){
 		      			System.out.println("Black victory");
 		      			break;
 		      		}
 		      		else if(game.board.numberRemaining(Piece.Type.BLACK) == 0){
 		      			System.out.println("White victory");
 		      			break;
 		      		}
 		      	}
 		    }		    
 		  	game.prettyprint();
 		  	game.printTime();
 		  	if(valid && moveturn){
 		  		turn++;
 		  	}
 		  	if (turn == 50) {
 		  		System.out.println("Maximum turns reached");
 		  		break;
 		  	}
 		}
 	}
 }
