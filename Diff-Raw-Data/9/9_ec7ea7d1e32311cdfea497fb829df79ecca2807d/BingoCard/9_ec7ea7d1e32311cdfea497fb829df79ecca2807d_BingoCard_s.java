 package net.ahjota.praxis.bingo;
 
 import java.util.ArrayList;
 import java.util.BitSet;
 import java.util.Collections;
 import java.util.List;
 import java.util.Random;
 
 import static net.ahjota.praxis.bingo.Bingo.*;
 
 /**
  * Represents a bingo card in play.
  */
 public class BingoCard {
 
 	static final int FREE_SPACE = 0;
 
 	final WinningBingo[] bingos = new WinningBingo[] {
 			new WinningBingo("1111100000000000000000000"),// column 1
 			new WinningBingo("0000011111000000000000000"),// column 2
 			new WinningBingo("0000000000111110000000000"),// column 3
 			new WinningBingo("0000000000000001111100000"),// column 4
 			new WinningBingo("0000000000000000000011111"),// column 5
 			new WinningBingo("1000010000100001000010000"),// row 1
 			new WinningBingo("0100001000010000100001000"),// row 2
 			new WinningBingo("0010000100001000010000100"),// row 3
 			new WinningBingo("0001000010000100001000010"),// row 4
 			new WinningBingo("0000100001000010000100001"),// row 5
 			new WinningBingo("1000001000001000001000001"),// diagonal down
 			new WinningBingo("0000100010001000100010000"),// diagonal up
 	};
 
 	private Random rng = new Random();
 
 	/**
 	 * current state of the board. index represents the board position. value
 	 * represents whether that board position has been marked.
 	 */
 	private BitSet board = new BitSet(boardSize);
 	
 	/**
 	 * numbers on the bingo card. List index is the position of the number.
 	 */
 	private ArrayList<Integer> boardNumbers = new ArrayList<Integer>(boardSize);
 
 	public BingoCard() {
 		this.generateBoard();
		resetCard();
 	}
 
 	private void generateBoard() {
 		/*
 		 * A random permutation of the numbers from 1 to 75, used to generate
 		 * the board.
 		 */
 		ArrayList<Integer> numberSet = new ArrayList<Integer>(numberSetSize - 1);
 		for (int i = 1; i < numberSetSize; ++i) {
 			numberSet.add(i);
 		}
 
 		// prepare the set of numbers for populating the board
 		int colMinimum = 1;
 		List<Integer> columnSubset;
 		for (int col = 0; col < colSize; ++col) {
 			// shuffle the subset of numbers for this column
 			columnSubset = numberSet.subList(colMinimum - 1, colMinimum + columnRange - 1);
 			Collections.shuffle(columnSubset, rng);
 			for (int row = 0; row < colSize; ++row) {
 				// then add the first 5 numbers from the shuffled subset to the board
 				boardNumbers.add(columnSubset.get(row));
 			}
 
 			// prepare for next column
 			colMinimum += columnRange;
 		}
 		// reset free space
 		boardNumbers.set(12, FREE_SPACE);
 
		// mark free space
		this.mark(FREE_SPACE);

		// this.printBoard();
 	}
 
 	public void resetCard() {
 		board.clear();
 	}
 
 	public void mark(int numberCalled) {
 		int index = boardNumbers.indexOf(numberCalled);
 		if (index > -1) {
 			// System.out.println("Marking " + numberCalled + " at position " + index);
 			board.set(index);
 		}
 	}
 
 	// TODO print the transverse
 	public void printBoard() {
 		for (int i = 0; i < boardNumbers.size(); ++i) {
 			System.out.print(boardNumbers.get(i) + "\t");
 			if ((i + 1) % colSize == 0) {
 				System.out.println();
 			}
 		}
 	}
 
 	// TODO print the transverse
 	public void printBoardWithMarks() {
 		for (int i = 0; i < board.size(); ++i) {
 			System.out.print(board.get(i) + "\t");
 			if ((i + 1) % colSize == 0) {
 				System.out.println();
 			}
 		}
 	}
 
 	public boolean checkForBingo() {
 		for (WinningBingo bingo : bingos) {
 			// bingo.printBingo();
 			// System.out.println();
 
 			BitSet cardBits = (BitSet) board.clone();// don't want to modify the actual board with bit operations
 			// System.out.println("CARDIS " + cardBits);
 
 			cardBits.andNot(bingo.mask);// masks the non-bingo positions...
 			// System.out.println("MASKED " + cardBits);
 
 			cardBits.xor(bingo.mask);// ... before setting them back to true
 			// System.out.println("X-ORED " + cardBits);
 
 			if (cardBits.cardinality() == boardSize) {
 				// BINGO iff bingo positions are also true
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	/**
 	 * Represents a winning bingo.
 	 */
 	class WinningBingo {
 
 		/**
 		 * bitmask used for checking whether a bingo card has a bingo.
 		 */
 		protected BitSet mask;
 
 		/**
 		 * @param bingo
 		 *            bitstring representing a particular way to bingo, where
 		 *            '1' is a marked card.
 		 */
 		public WinningBingo(String bingo) {
 			mask = new BitSet(boardSize);
 			for (int i = 0; i < bingo.length(); ++i) {
 				char ch = bingo.charAt(i);
 
 				if ('0' == ch) {
 					mask.set(i);
 				}
 			}
 		}
 
 		public void printMask() {
 			System.out.println("BINGO  " + mask);
 		}
 
 		public void printBingo() {
 			for (int i = 0; i < boardSize; ++i) {
 				System.out.print((true == mask.get(i) ? '0' : '1') + "\t");
 				if ((i + 1) % colSize == 0) {
 					System.out.println();
 				}
 			}
 		}
 	}
 
 }
