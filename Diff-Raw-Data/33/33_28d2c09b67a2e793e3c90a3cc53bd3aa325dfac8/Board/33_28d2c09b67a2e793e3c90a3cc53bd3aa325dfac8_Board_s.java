 package quoridor;
 public class Board implements BoardInterface {
 
 	final static int numRows = 9;
 	final static int numCols = 9;
 	
 	private Cell board[][] = new Cell [numRows][numCols];
 	public Board(Player one, Player two) {
 		for (int i=0; i < numRows; i++) {
 		     for (int j=0; j < numCols; j++) {
 		    	 board[i][j] = new Cell();
 		     }
 		 }
 		
 		board[one.getX()][one.getY()].playerNum = one.getPlayer();
 		board[two.getX()][two.getY()].playerNum = two.getPlayer();
 		
 		for (int i = 0; i < numRows; i++) {
 			board[i][0].v = true;
 			board[0][i].h = true;
 		}
 	}
 
 	@Override
 	public boolean isLegalMove(Player p, int x, int y) {
 		boolean result = false;
 		if ((board[x][y].playerNum != 0)) {
 		// checks if they are trying to move up and stops if there is a wall
 		} else if ((p.getX()-x) == 1 && (p.getY() == y)) {
 			if (board[p.getX()][y].h == false){
 				result = true;	
 			}
 		//checks if they want to move down and stops if there is a wall
 		} else if ((p.getX()-x) == -1 && (p.getY() == y)) {
 			if (board[x][y].h == false) {
 				result = true;
 			}
 		} else if ((p.getY()-y) == -1 && (p.getX() == x)) {
 			if (board[x][y].v == false) {
 				result = true;
 			}
 		} else if ((p.getY()-y) == 1 && (p.getX() == x)) {
 			if (board[x][p.getY()].v == false) {
 				result = true;
 			}
 		} else if (isLegalJumpMove(p,x,y)== true || isLegalDiagonalMove(p,x,y) == true ) {
 			result = true;
 		}
 		if (result == false) {
 			System.out.println("invalid move");
 		}
 		return result;
 	}
 
 	@Override
 	public boolean placeWall(Player p, int x, int y,char d) {
 		boolean r = false;
 		if (isLegalWall(x,y,d) == true) {
 			if (d == 'h') {
 				if (board[x][y].h == false) {
 					board[x][y].h = true;
 					board[x][y+1].h = true;
 					p.useWall();
 					r = true;
 				}
 			} else if (d == 'v') {
 				if (board[x][y].v == false) {
 					board[x][y].v = true;
 					board[x+1][y].v = true;
 					p.useWall();
 					r = true;
 				}
 			}
 		}
 		return r;
 	}
 
 	@Override
 	public boolean movePlayer(Player p, int x, int y) {
 		boolean r = false;
 		if (isLegalMove(p,x,y) == true) {
 			int startx = p.getX();
 			int starty = p.getY();
 			board[startx][starty].playerNum = 0;
 			board[x][y].playerNum = p.getPlayer();
 			p.setX(x);
 			p.setY(y);
 			//return true;
 			r = true;
 		}
 		return r;
 	}
 
 	@Override
 	
 	// i is the rows(y)
 	//j is the coloum(x)
 	public void displayBoard() {
 		for (int i=0; i<numRows; i++) {
 			System.out.print(" ");
 		     for (int j=0; j<numCols; j++) {
 		    	 if (board[i][j].h == false) {
 		    		 System.out.print("  ");
 		    	 } else {
 		    		 System.out.print(" _");
 		    	 }
 		    	 System.out.print(" ");
 		        
 		     }
 		     System.out.println("");
 		     for (int j=0; j<numCols; j++) {
 		    	 if (board[i][j].v == false) {
 		    		 System.out.print(" ");
 		    	 } else {
 		    		 System.out.print("|");
 			    	 if (j == 0) {
 			    		 System.out.print(" ");
 			    	 }
 		    	 }
 		    	 if (board[i][j].playerNum == 0) {
 		    		 System.out.print("#");
 		    	 } else {
 		    		 System.out.print(board[i][j].playerNum);
 		    	 }
 		    	 System.out.print(" ");
 		    		 
 		        
 		     }	
 			 
 	         System.out.println("|"+i);
 		 }
 		System.out.println( "  _  _  _  _  _  _  _  _  _");
 		System.out.println( "  a  b  c  d  e  f  g  h  i");
 	}
 
 	@Override
 	public Player checkWinner(Player one, Player two) {
 		if (one.getX() == one.getGoal()) {
 			return one;
 		} else if (two.getX() == two.getGoal()) {
 			return two;
 		}
 		
 		return null;
 	}
 	
 	//may need to be changed to pass both players, for checking blocked paths
 	//checks that the wall is going to be placed within the borders of the game
 	// and wont collide with other walls
 	private boolean isLegalWall(int x, int y, char d) {
 		boolean r = false;
 		if (d == 'h') {
 			if ( y < numCols-1 &&(board[x][y].h == false) && (board[x][y].h == false)) {
 				r = true;
 			}
 		}
 		if (d == 'v') {
			if (x<numRows-1 && board[x][y].v == false && board[x+1][y].v == false) {
 				r = true;
 			}
 		}
 		if (r == false) {
 			System.out.println("illegal wall placement");
 		}
 		
 		
 		return r;
 	}
 	//checks if its legal to jump over a player
 	private boolean isLegalJumpMove(Player p, int x, int y) {
 		boolean r = false;
 		//if the player moves to the left
 		if (p.getX()-x==2 && p.getY() == y) {
 			// if there is a player one to the left
 			if (board[p.getX()-1][y].playerNum != 0) {
 				//return true (is a legal move)
 				r = true;
 			}
 		} else if (p.getX()-x==-2 && p.getY() == y) {
 			// if there is a player one to the left
 			if (board[p.getX()+1][y].playerNum != 0) {
 				//return true (is a legal move)
 				r = true;
 			}
 		// else if the player is moving two up	
 		} else if (p.getY()-y==2 && p.getX() == x) {
 			if (board[x][p.getY()-1].playerNum != 0) {
 				r = true;
 			}
 		} else if (p.getY()-y==-2 && p.getX() == x) {
 			if (board[x][p.getY()+1].playerNum != 0) {
 				r = true;
 			}
 		}
 		return r;
 		
 	}
 	
 	private boolean isLegalDiagonalMove (Player p, int x, int y) {
 		boolean r = false;
		//if moving left
		if (p.getX()-x == 1) {
			//if moving up
			if (p.getY()-y == 1) {
				if (board[x][y-1].playerNum != 0 && board[x][y-1].h == true) {
					r = true;
				} else if (board[x-1][y].playerNum != 0 && board[x+1][y].v == true) {
 					r = true;
 				}
 			}
 		}
 		if (r == true) {
 			System.out.println("legal diagonal");
 		}
 		return r;
 	}
 	
 }
