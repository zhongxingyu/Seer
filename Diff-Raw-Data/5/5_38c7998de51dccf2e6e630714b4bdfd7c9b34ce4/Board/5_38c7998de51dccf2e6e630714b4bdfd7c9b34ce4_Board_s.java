 package team01;
 
 import java.util.*;
 
 public class Board {
 
 	static final int ROW_SIZE = 5;
 	static final int COL_SIZE = 9;
 	static final int BOARD_SIZE = ROW_SIZE * COL_SIZE;
 	
 	// board states
 	static final int EMPTY = 0;
 	static final int WHITE = 1;
 	static final int BLACK = 2;
 	
 	private List <Integer> board;
 	private int nWhite;
 	private int nBlack;
 	
 	// new game
 	public Board() {
 		board = new ArrayList <Integer>(BOARD_SIZE);
 		nWhite = nBlack = 22;
 		
 		// first 2 rows
		for (int i = 0; i < 2*ROW_SIZE; i++) {
 			this.setPosition(i, BLACK);
 		}
 		
 		// middle row
 		this.setPosition(18,BLACK);
 		this.setPosition(19,WHITE);
 		this.setPosition(20,BLACK);
 		this.setPosition(21,WHITE);
 		this.setPosition(22,EMPTY);
 		this.setPosition(23,BLACK);
 		this.setPosition(24,WHITE);
 		this.setPosition(25,BLACK);
 		this.setPosition(26,WHITE);
 		
 		// last 2 rows
		for (int i = 3*ROW_SIZE; i < BOARD_SIZE; i++) {
 			this.setPosition(i, WHITE);
 		}
 	}
 	
 	int numWhite() {
 		return nWhite;
 	}
 	
 	int numBlack() {
 		return nBlack;
 	}
 	
 	int numEmpty() {
 		return BOARD_SIZE - nWhite - nBlack;
 	}
 	
 	boolean isEmpty(int pos) {
 		return board.get(pos) == EMPTY;
 	}
 	
 	boolean isWhite(int pos) {
 		return board.get(pos) == WHITE;
 	}
 	
 	boolean isBlack(int pos) {
 		return board.get(pos) == BLACK;
 	}
 	
 	void setPosition(int pos, int state) {
 		board.set(pos, state);
 	}
 }
