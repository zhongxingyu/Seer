 package com.cozycoding.crea1.tetris;
 
 import com.cozycoding.crea1.tetris.blocks.Cell;
 import com.cozycoding.crea1.tetris.blocks.TetrisBlock;
 
 /**
  * @author Marius Kristensen
  */
 public class GameRules {
     // |0|1|2|3|4|5|6|7|8|9
     public static final int noOfColumns = 10;
     public static final int noOfRows = 18;
 
     private Cell[][] gameBoard = new Cell[noOfRows][];
     private TetrisBlock activeBlock;
 
     public GameRules() {
         fillGameBoardWithEmptyRows(gameBoard);
     }
 
     public void fillGameBoardWithEmptyRows(Cell[][] gameBoard) {
         Cell[][] board = new Cell[gameBoard.length][];
         for (int row = 0; row < gameBoard.length; row++) {
             board[row] = fillCellRowWithEmptyCells(new Cell[noOfColumns], row);
         }
         this.gameBoard = board;
     }
 
     public boolean isRowFilled(Cell[] cells) {
         for (Cell cell : cells) {
             if (!cell.isFilled()) {
                 return false;
             }
         }
         return true;
     }
 
     public Cell[] fillCellRowWithEmptyCells(Cell[] cells, int row) {
         for (int i = 0; i < cells.length; i++) {
             Cell cell = new Cell(false, i, row);
             cells[i] = cell;
         }
         return cells;
     }
 
     public void placeBlockOnGameBoard(TetrisBlock tetrisBlock) {
         this.activeBlock = tetrisBlock;
         }
 
     public synchronized void moveActiveBlockDown() {
         if (!hasCrashedWithOtherCells()) {
             activeBlock.moveDown();
         }
     }
 
     public void moveActiveBlockLeft() {
         //todo also check if there are blocks to the left
         if (!isActiveBlockAtLeftWall() && !cellToTheLeftIsFilled()) {
             activeBlock.moveLeft();
         }
     }
 
     private boolean cellToTheLeftIsFilled() {
         for (Cell cell : activeBlock.getShape()) {
             if (gameBoard[cell.getY()][cell.getX() - 1].isFilled()) {
                 return true;
             }
         }
         return false;
     }
 
     public void moveActiveBlockRight() {
         if (!isActiveBlockAtRightWall() && !cellToTheRightIsFilled()) {
             activeBlock.moveRight();
         }
     }
 
     private boolean cellToTheRightIsFilled() {
         for (Cell cell : activeBlock.getShape()) {
            if (gameBoard[cell.getY()][cell.getX() + 1].isFilled()) {
                 return true;
             }
         }
         return false;
     }
 
     public boolean isActiveBlockAtBottom() {
         for (Cell cell : activeBlock.getShape()) {
             if (cell.isFilled() && cell.getY() == noOfRows-1) {
                 return true;
             }
         }
         return false;
     }
 
     public boolean isActiveBlockAtLeftWall() {
         for (Cell cell : activeBlock.getShape()) {
             if (cell.isFilled() && cell.getX() == 0) {
                 return true;
             }
         }
         return false;
     }
 
     public boolean isActiveBlockAtRightWall() {
         for (Cell cell : activeBlock.getShape()) {
             if (cell.isFilled() && cell.getX() == noOfColumns-1) {
                 return true;
             }
         }
         return false;
     }
 
 
 
     public synchronized Cell[][] getGameBoard() {
         return gameBoard;
     }
 
     public synchronized TetrisBlock getActiveBlock() {
         return activeBlock;
     }
 
     public void stopActiveBlockAndMergeItWithBoard() {
         for (Cell cell : activeBlock.getShape()) {
             gameBoard[cell.getY()][cell.getX()].setFilled(true);
             gameBoard[cell.getY()][cell.getX()].setColor(cell.getColor());
 
         }
         activeBlock = null;
     }
 
     public boolean hasCrashedWithOtherCells() {
         if (isActiveBlockAtBottom()) {
             return true;
         }
         for (Cell cell : activeBlock.getShape()) {
             if (gameBoard[cell.getY() + 1][cell.getX()].isFilled()) {
                 return true;
             }
         }
         return false;
     }
 
     public void rotateActiveBlock() {
         // TODO Check for crash when rotatin
         activeBlock.rotateShapeCW();
     }
 }
