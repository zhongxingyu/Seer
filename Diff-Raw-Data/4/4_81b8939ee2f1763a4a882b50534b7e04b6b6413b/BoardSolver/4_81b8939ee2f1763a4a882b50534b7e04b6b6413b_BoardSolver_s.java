 /*
  * Fling! Solver
  * Copyright (C) 2013 - Jos Expsito <jose.exposito89@gmail.com>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>
  */
 package com.eggsoftware.flingsolver.solver;
 
 import java.util.ArrayList;
 import android.util.Log;
 
 public class BoardSolver {
 
 	private static final String TAG = "BoardSolver";
 	
 	/**
 	 * Dimensions of the board.
 	 */
 	public static final int BOARD_NUM_COLUMNS = 7;
 	public static final int BOARD_NUM_ROWS    = 8;
 	
 	/**
 	 * Directions in which can move a Fling!
 	 */
 	public enum Direction { UP, DOWN, LEFT, RIGHT }
 
 	/**
 	 * Solves the specified board.
 	 * @return The solution step by step if it is possible to solve the board, or null otherwise.
 	 */
 	public static ArrayList<SolutionStep> solveBoard(boolean[][] board) {
 		// Check the size of the board
 		if (board.length != BOARD_NUM_ROWS) {
 			Log.d(TAG, "Invalid board size: The height must be BOARD_HEIGHT");
 			return null;
 		}
 		
 		for (int row=0; row<BOARD_NUM_ROWS; row++) {
 			if (board[row].length != BOARD_NUM_COLUMNS) {
 				Log.d(TAG, "Invalid board size: The width must be BOARD_WIDTH");
 				return null;
 			}
 		}
 
 		// Make a copy of the board to not modify them
 		boolean[][] boardCopy = BoardSolver.copyBoard(board);
 		
 		// Solve the board
 		int numberOfFlings = BoardSolver.getNumberOfFlingsInBoard(boardCopy);
		if (numberOfFlings == 0) {
			Log.d(TAG, "The boar  haven't got flings");
 			return null;
 		}
 		
 		ArrayList<SolutionStep> solution = new ArrayList<SolutionStep>();
 		boolean boardHasSolution = BoardSolver.solveBoardAux(boardCopy, numberOfFlings, solution);
 		
 		if (!boardHasSolution)
 			return null;
 		
 		// Set the boards to the solution
 		solution.get(0).setBoard(board);
 		for (int n=1; n<solution.size(); n++) {
 			SolutionStep previousStep = solution.get(n-1);
 			solution.get(n).setBoard(applyTransformation(previousStep.getRow(), previousStep.getCol(),
 					previousStep.getDirection(), previousStep.getBoard()));
 		}
 
 		return solution;
 	}
 	
 	/**
 	 * Auxiliary recursive method used by solveBoard(). 
 	 * @see solveBoard
 	 */
 	private static boolean solveBoardAux(boolean[][] board, int numberOfFlings, ArrayList<SolutionStep> solutionSteps) {
 		if (numberOfFlings == 1) {
 			return true;
 		} else {			
 			for (int row=0; row<BOARD_NUM_ROWS; row++) {
 				for (int col=0; col<BOARD_NUM_COLUMNS; col++) {
 					if (board[row][col]) {
 						for (Direction direction : Direction.values()) {
 							if (BoardSolver.canMove(row, col, direction, board)) {
 								solutionSteps.add(new SolutionStep(row, col, direction));
 								if (BoardSolver.solveBoardAux(BoardSolver.applyTransformation(row, col, direction, board), numberOfFlings-1, solutionSteps))
 									return true;
 								solutionSteps.remove(solutionSteps.size()-1);
 							}
 						}
 					}
 				}
 			}
 			return false;
 		}
 	}
 	
 	/**
 	 * @return A copy of the specified board.
 	 */
 	private static boolean[][]copyBoard(boolean[][] board) {
 		boolean[][] boardCopy = new boolean[BOARD_NUM_ROWS][BOARD_NUM_COLUMNS];
 		for (int row=0; row<BOARD_NUM_ROWS; row++)
 			boardCopy[row] = board[row].clone();
 		return boardCopy;
 	}
 	
 	/**
 	 * Specifies if it is possible to move a Fling!
 	 * @param  flingToMoveRow The row of the Fling! to move.
 	 * @param  flingToMoveCol The column of the Fling! to move.
 	 * @param  direction UP, DOWN, LEFT or RIGHT.
 	 * @return true if it is possible to move the Fling!, false otherwise.
 	 */
 	private static boolean canMove(int flingToMoveRow, int flingToMoveCol, Direction direction, boolean[][] board) {
 		// Validate the parameters
 		if (direction == null)
 			return false;
 		
 		if (flingToMoveRow >= BOARD_NUM_ROWS || flingToMoveCol >= BOARD_NUM_COLUMNS || flingToMoveRow < 0 || flingToMoveCol < 0)
 			return false;
 		
 		if (!board[flingToMoveRow][flingToMoveCol])
 			return false;
 		
 		// Switch depending of the direction
 		if (direction == Direction.UP) {
 			if (flingToMoveRow == 0)
 				return false;
 			if (board[flingToMoveRow-1][flingToMoveCol])
 				return false;
 			
 			for (int row=flingToMoveRow-2; row>=0; row--) {
 				if (board[row][flingToMoveCol])
 					return true;
 			}
 			
 		} else if (direction == Direction.DOWN) {
 			if (flingToMoveRow == BOARD_NUM_ROWS-1)
 				return false;
 			if (board[flingToMoveRow+1][flingToMoveCol])
 				return false;
 			
 			for (int row=flingToMoveRow+2; row<BOARD_NUM_ROWS; row++) {
 				if (board[row][flingToMoveCol])
 					return true;
 			}
 			
 		} else if (direction == Direction.LEFT) {
 			if (flingToMoveCol == 0)
 				return false;
 			if (board[flingToMoveRow][flingToMoveCol-1])
 				return false;
 			
 			for (int col=flingToMoveCol-2; col>=0; col--) {
 				if (board[flingToMoveRow][col])
 					return true;
 			}
 			
 		} else if (direction == Direction.RIGHT) {
 			if (flingToMoveCol == BOARD_NUM_COLUMNS-1)
 				return false;
 			if (board[flingToMoveRow][flingToMoveCol+1])
 				return false;
 			
 			for (int col=flingToMoveCol+2; col<BOARD_NUM_COLUMNS; col++) {
 				if (board[flingToMoveRow][col])
 					return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * Moves the specified Fling! following the Fling! game rules.
 	 * Call canMove() before call BoardSolver methods to ensure that the movement it's possible.
 	 * @param  flingToMoveRow The row of the Fling! to move.
 	 * @param  flingToMoveCol The column of the Fling! to move.
 	 * @param  direction UP, DOWN, LEFT or RIGHT.
 	 * @return The new board.
 	 */
 	private static boolean[][] applyTransformation(int flingToMoveRow, int flingToMoveCol, Direction direction, boolean[][] board) {
 		boolean[][] resultBoard = BoardSolver.copyBoard(board);
 		boolean removeCurrentFling = true;
 		
 		if (direction == Direction.UP) {
 			for (int row=flingToMoveRow-1; row>=0; row--) {
 				if (resultBoard[row][flingToMoveCol]) {
 					resultBoard[row+1][flingToMoveCol] = true;
 					removeCurrentFling = (row != flingToMoveRow-1);
 					resultBoard = BoardSolver.applyTransformation(row, flingToMoveCol, direction, resultBoard);
 					break;
 				}
 			}
 			
 		} else if (direction == Direction.DOWN) {
 			for (int row=flingToMoveRow+1; row<BOARD_NUM_ROWS; row++) {
 				if (resultBoard[row][flingToMoveCol]) {
 					resultBoard[row-1][flingToMoveCol] = true;
 					removeCurrentFling = (row != flingToMoveRow+1);
 					resultBoard = BoardSolver.applyTransformation(row, flingToMoveCol, direction, resultBoard);
 					break;
 				}
 			}
 			
 		} else if (direction == Direction.LEFT) {
 			for (int col=flingToMoveCol-1; col>=0; col--) {
 				if (resultBoard[flingToMoveRow][col]) {
 					resultBoard[flingToMoveRow][col+1] = true;
 					removeCurrentFling = (col != flingToMoveCol-1);
 					resultBoard = BoardSolver.applyTransformation(flingToMoveRow, col, direction, resultBoard);
 					break;
 				}
 			}
 			
 		} else if (direction == Direction.RIGHT) {
 			for (int col=flingToMoveCol+1; col<BOARD_NUM_COLUMNS; col++) {
 				if (resultBoard[flingToMoveRow][col]) {
 					resultBoard[flingToMoveRow][col-1] = true;
 					removeCurrentFling = (col != flingToMoveCol+1);
 					resultBoard = BoardSolver.applyTransformation(flingToMoveRow, col, direction, resultBoard);
 					break;
 				}
 			}
 		}
 		
 		if (removeCurrentFling)
 			resultBoard[flingToMoveRow][flingToMoveCol] = false;
 		return resultBoard;
 	}
 	
 	
 	/**
 	 * Returns how many Flings! the board have.
 	 */
 	private static int getNumberOfFlingsInBoard(boolean[][] board) {
 		int numFlings = 0;
 		
 		for (int row=0; row<BOARD_NUM_ROWS; row++) {
 			for (int col=0; col<BOARD_NUM_COLUMNS; col++) {
 				if (board[row][col])
 					numFlings++;
 			}
 		}
 		
 		return numFlings;
 	}
 	
 }
