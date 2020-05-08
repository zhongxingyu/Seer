 /*
  * Copyright (C) 2013 Spencer Alderman
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.rogue.connectfour.board;
 
 import com.rogue.connectfour.ConnectFour;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * The {@link Board} class used for managing the {@link Piece} objects of the
  * game and general information
  * 
  * @since 1.0.0
  * @author Spencer Alderman
  * @version 1.0.0
  */
 public class Board {
 
     private ConnectFour project;
     private Node<Piece>[][] grid;
     public final int maxHeight;
     public final int maxWidth;
     private final List<Integer> fullCols = new ArrayList();
     //Winning variables, used on win only.
     private Direction winningDir = Direction.TOP;
     private int winningRow = 0;
     private int winningCol = 0;
 
     /**
      * Constructor for {@link Board}. Initializes the grid and then sets the
      * neighbors for the {@link Node} objects
      *
      * @since 1.0.0
      * @version 1.0.0
      *
      * @param project The {@link ConnectFour} instance
      * @param rows The number of rows
      * @param columns The number of columns
      */
     public Board(ConnectFour project, int rows, int columns) {
         this.project = project;
        this.maxHeight = rows;
        this.maxWidth = columns;
         this.grid = new Node[rows][columns];
         for (int i = 0; i < grid.length; i++) {
             for (int w = 0; w < grid[i].length; w++) {
                 grid[i][w] = new Node<Piece>(Piece.NULL, w);
             }
         }
         for (int i = 0; i < grid.length; i++) {
             for (int w = 0; w < grid[i].length; w++) {
                 grid[i][w].setNeighbors(this, i, w);
             }
         }
     }
 
     /**
      * Plays a piece on the {@link Board}
      *
      * @since 1.0.0
      * @version 1.0.0
      *
      * @param type The type of piece to play
      * @param column The column to play in1
      * 
      * @throws FullColumnException If the provided column is full
      *
      * @return True if play was a game-winning move
      */
     public boolean play(Piece type, int column) throws FullColumnException {
         boolean full = true;
         for (int i = this.grid.length - 1; i >= 0; i--) {
             if (this.grid[i][column].getData().equals(Piece.NULL)) {
                 full = false;
                 this.grid[i][column].setData(type);
                 System.out.println("Player drops an " + type.toString() + " piece into column: " + column);
                 for (Direction d : Direction.values()) {
                     if (this.grid[i][column].search(d) >= 4) {
                         this.winningDir = d;
                         this.winningCol = column;
                         this.winningRow = i;
                         return true;
                     }
                 }
                 break;
             }
         }
         if (full) {
             this.fullCols.add(column);
             throw new FullColumnException();
         }
         return false;
     }
 
     /**
      * Returns the grid. Should be used in a synchronized block for safety.
      *
      * @since 1.0.0
      * @version 1.0.0
      *
      * @return The board grid
      */
     public synchronized Node<Piece>[][] getGrid() {
         return this.grid;
     }
 
     /**
      * Gets a list of open {@link Node} objects on the board based on type
      *
      * @since 1.0.0
      * @version 1.0.0
      *
      * @deprecated Needs to be more efficient
      * 
      * @param type The {@link Piece} type to look for
      *
      * @return List of open nodes on the board
      */
     public List<Node<Piece>> getOpenNodes(Piece type) {
         List<Node<Piece>> nodes = new ArrayList();
         for (int i = 0; i < grid.length; i++) {
             for (int w = 0; w < grid[i].length; w++) {
                 if (this.grid[i][w].getData().equals(type)) {
                     dirLoop:
                     for (Direction d : Direction.values()) {
                         if (this.grid[i][w].getNeighbor(d) != null) {
                             if (this.grid[i][w].getNeighbor(d).getData().equals(Piece.NULL)) {
                                 nodes.add(this.grid[i][w]);
                                 break dirLoop;
                             }
                         }
                     }
                 }
             }
         }
         return nodes;
     }
     
     /**
      * Returns the string format for the winning move.
      * 
      * @since 1.0.0
      * @version 1.0.0
      * 
      * @return The winning move in string form
      */
     public String getWinningMove() {
         if (this.winningDir.equals(Direction.TOP) || this.winningDir.equals(Direction.BOTTOM)) {
             return "in column " + this.winningCol;
         } else if (this.winningDir.equals(Direction.LEFT) || this.winningDir.equals(Direction.RIGHT)) {
             return "in row " + this.winningRow;
         } else {
             return "on a diagonal";
         }
     }
     
     /**
      * Gets a list of the full columns on the board
      * 
      * @since 1.0.0
      * @version 1.0.0
      * 
      * @return List of full columns
      */
     public List<Integer> getFullColumns() {
         return this.fullCols;
     }
 }
