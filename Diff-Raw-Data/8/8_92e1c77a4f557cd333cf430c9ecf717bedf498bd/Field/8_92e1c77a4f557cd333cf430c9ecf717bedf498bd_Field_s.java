 package com.udev.domain;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.*;
 
 /**
  * Created with IntelliJ IDEA.
  *
  * @author taipan
  *         Date: 04.05.13
  *         Time: 17:47
  */
 public class Field {
 
     /**
      * Left coordinate of the figure when it is added on the field.
      */
     public static final int CREATE_FIGURE_LEFT_COORDINATE = 4;
 
     /**
      * Height of the field.
      */
     public static final int HEIGHT = 10;
 
     /**
      * Width of the field.
      */
     public static final int WIDTH = 20;
 
     /**
      * The value of the field is 1 (busy).
      */
     public static final byte ONE = 1;
 
     /**
      * Initial value of the field is 0 (empty).
      */
     public static final byte ZERO = 0;
 
     /**
      * Logger. *
      */
     private static final Logger logger = LoggerFactory.getLogger(Field.class);
 
     /**
      * Is set to {@code true} when there is enough space to create a new figure.
      */
     private boolean notFull = true;
 
     /**
      * Is set to {@code true} when it is possible to move the figure.
      * When it is {@code false}, the new figure will be created.
      */
     private boolean possibleMoveFigure = false;
 
     /**
      * Scores.
      */
     private int scores = 0;
 
     /**
      * Data of the field.
      */
     private Cell[][] cells = new Cell[WIDTH][HEIGHT];
 
 
     /**
      * Constructor.
      */
     public Field() {
         init();
     }
 
     /**
      * @return Data of the field.
      */
     public Cell[][] getCells() {
         return cells;
     }
 
     /**
      * Sets data of the field.
      *
      * @param cells Data of the field.
      */
     public void setCells(Cell[][] cells) {
         this.cells = cells;
     }
 
     /**
      * @return {@code true} when it is possible to move the figure.
      */
     public boolean isPossibleMoveFigure() {
         return possibleMoveFigure;
     }
 
     /**
      * Sets possibleMoveFigure
      *
      * @param possibleMoveFigure Is {@code true} when it is possible to move the figure.
      */
     public void setPossibleMoveFigure(boolean possibleMoveFigure) {
         this.possibleMoveFigure = possibleMoveFigure;
     }
 
     /**
      * Indicates if it is possible to create a new figure.
      * When the field is full, the game is over.
      *
      * @return <code>true</code> if there is enough space to create a new figure.
      */
     public boolean isNotFull() {
         return notFull;
     }
 
     /**
      * Sets notFull field.
      *
      * @param notFull Indicates if it is possible to create a new figure.
      * When the field is full, the game is over.
      */
     public void setNotFull(boolean notFull) {
         this.notFull = notFull;
     }
 
     /**
      *
      * @return Scores.
      */
     public int getScores() {
         return scores;
     }
 
     /**
      * Clears the com.udev.field
      */
     public void clear() {
         for (int i = 0; i < WIDTH; i++) {
             for (int j = 0; j < HEIGHT; j++) {
                 Cell cell = cells[i][j];
                 cell.setI(i);
                 cell.setJ(j);
                 cell.setData(ZERO);
             }
         }
     }
 
     public void checkScores() {
         Queue<Integer> linesToRemove = new ArrayDeque<>();
         for (int i = 0; i < 20; i++) {
             boolean lineIsPopulated = true;
             for (int j = 0; j < 10; j++) {
                 Cell cell = cells[i][j];
                 if (cell.getData() == Field.ZERO) {
                     lineIsPopulated = false;
                     break;
                 }
             }
             if (lineIsPopulated) {
                 linesToRemove.add(i);
             }
         }
        rearrangeData(linesToRemove);
         scores = scores + linesToRemove.size() * 10;
     }
 
     /**
      * Logs the current state of the data
      */
     public void showData() {
         logger.debug("\n\n The data:");
         StringBuilder sb = new StringBuilder();
         for (int i = 0; i < WIDTH; i++) {
             for (int j = 0; j < HEIGHT; j++) {
                 sb.append(cells[i][j].getData() + " ");
             }
             sb.append("\n");
         }
         logger.debug("\n" + sb.toString());
     }
 
     /**
      * Initialize the cells
      */
     private void init() {
         for (int i = 0; i < WIDTH; i++) {
             for (int j = 0; j < HEIGHT; j++) {
                 Cell cell = new Cell();
                 cell.setI(i);
                 cell.setJ(j);
                 cell.setData(ZERO);
                 cells[i][j] = cell;
             }
         }
     }
 
     private void rearrangeData(Queue<Integer> linesToRemove) {
         while (!linesToRemove.isEmpty()) {
             int line = linesToRemove.poll();
             for (int i = line; i > 0; i--) {
                 for (int j = 0; j < 10; j++) {
                     cells[i][j] = cells[i - 1][j];
                     cells[i][j].setI(i);
                 }
             }
             for (int j = 0; j < 10; j++) {
                cells[0][j].setData(ZERO);
                cells[0][j].setI(0);
             }
             showData();
         }
     }
 }
