 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Mazes;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  *
  * @author ovoloshchuk
  */
 public class Maze {
 
     private static class Node {
 
         private boolean bWallDown = true;
         private boolean bWallRight = true;
 
         public Node() {
         }
 
         public boolean hasWallDown() {
             return bWallDown;
         }
 
         public boolean hasWallRight() {
             return bWallRight;
         }
 
         public void setWallDown(boolean bWallDown) {
             this.bWallDown = bWallDown;
         }
 
         public void setWallRight(boolean bWallRight) {
             this.bWallRight = bWallRight;
         }
     }
     private int width = 0;
     private int height = 0;
     private Node[][] nodes = null;
     private int MIN_PATH_LENGTH = 0;
     
     int nExitPointRow = 0;
 
     private enum Direction {
 
         LEFT,
         RIGHT,
         UP,
         DOWN;
         private static final Direction[] dirs = {LEFT, RIGHT, UP, DOWN};
 
         private Direction opposite() {
             switch (this) {
                 case RIGHT:
                     return LEFT;
                 case LEFT:
                     return RIGHT;
                 case UP:
                     return DOWN;
                 case DOWN:
                     return UP;
             }
             return this;
         }
 
         public Direction next() {
             Direction result = this;
             do {
                 result = dirs[(int) (Math.random() * 4)];
             } while (result == this.opposite());
             return result;
         }
     }
 
     public Maze(int width, int height) {
         this.width = width;
         this.height = height;
 
         MIN_PATH_LENGTH = width * height - 1;
 
         nodes = new Node[height][width];
         for (int row = 0; row < height; row++) {
             for (int col = 0; col < width; col++) {
                 nodes[row][col] = new Node();
             }
         }
 
         randomize();
     }
 
     private void randomize() {
         boolean[][] arrUsedCells = new boolean[height][width];
         for (int row = 0; row < height; row++) {
             for (int col = 0; col < width; col++) {
                 arrUsedCells[row][col] = false;
             }
         }
 
         int row = (int) (Math.random() * height);
         int col = 0;
 
         nodes[row][col].setWallDown(false);
         arrUsedCells[row][col] = true;
 
         ArrayList<Point> lsPoints = new ArrayList<Point>();
 
         while (!pathFrom(row, col, arrUsedCells, lsPoints)) {
             Point pt = lsPoints.get((int) (Math.random() * lsPoints.size()));
             row = pt.y;
             col = pt.x;
         }
         nExitPointRow = (int) (Math.random()*height);
     }
 
     private boolean pathFrom(int rowStart, int colStart, boolean[][] arrUsedCells, List<Point> lsPoints) {
         int nPathLength = 0;
         int row = rowStart, col = colStart;
         lsPoints.add(new Point(col, row));
         while (row >= 0 && col >= 0 && row < height && col < width) {
             Direction dir = getDir(row, col, arrUsedCells, nPathLength);
             if (dir == null) {
                 Point pt = lsPoints.get((int) (Math.random() * lsPoints.size()));
                 row = pt.y;
                 col = pt.x;
                 continue;
             }
 
             nPathLength++;
             System.out.println(nPathLength);
             switch (dir) {
                 case LEFT:
                    nodes[row][col].setWallDown(false);
                     col--;
                     break;
                 case RIGHT:
                     if (col < width - 1) {
                         nodes[row][col + 1].setWallDown(false);
                     }
                     col++;
                     break;
                 case UP:
                    nodes[row][col].setWallRight(false);
                     row--;
                     break;
                 case DOWN:
                     if (row < height - 1) {
                         nodes[row + 1][col].setWallRight(false);
                     } 
                     row++;
                     break;
             }
             lsPoints.add(new Point(col, row));
             if (row >= 0 && col >= 0 && row < height & col < width) {
                 arrUsedCells[row][col] = true;
             } else {
                 return true;
             }
         }
         return true;
     }
 
     private Direction getDir(int row, int col, boolean[][] arrUsedCells, int nPathLength) {
         ArrayList<Direction> dirs = new ArrayList<Direction>();
         dirs.add(Direction.UP);
         dirs.add(Direction.DOWN);
         dirs.add(Direction.LEFT);
         dirs.add(Direction.RIGHT);
 
         if (row > 0 && arrUsedCells[row - 1][col]) {
             dirs.remove(Direction.UP);
         }
         if (row < height - 1 && arrUsedCells[row + 1][col]) {
             dirs.remove(Direction.DOWN);
         }
         if (col > 0 && arrUsedCells[row][col - 1]) {
             dirs.remove(Direction.LEFT);
         }
         if (col < width - 1 && arrUsedCells[row][col + 1]) {
             dirs.remove(Direction.RIGHT);
         }
 
         if (nPathLength < MIN_PATH_LENGTH) {
             if (row == 0) {
                 dirs.remove(Direction.UP);
             }
             if (row == height - 1) {
                 dirs.remove(Direction.DOWN);
             }
             if (col == 0) {
                 dirs.remove(Direction.LEFT);
             }
             if (col == width - 1) {
                 dirs.remove(Direction.RIGHT);
             }
         }
 
         if (dirs.isEmpty()) {
             return null;
         }
         return dirs.get((int) (Math.random() * dirs.size()));
     }
 
     public int getHeight() {
         return height;
     }
 
     public int getWidth() {
         return width;
     }
 
     public boolean hasWallRight(int row, int col) {
         return nodes[row][col].hasWallRight();
     }
 
     public boolean hasWallDown(int row, int col) {
         return nodes[row][col].hasWallDown();
     }
 
     public static void paint(Maze maze, Graphics2D g) {
         Color clrBackground = new Color(240, 240, 240);
         clrBackground = Color.WHITE;
         Color clrLine = Color.BLACK;
 
         Rectangle bounds = g.getDeviceConfiguration().getBounds();
         Rectangle walls = (Rectangle) bounds.clone();
         walls.height--;
         walls.width--;
 
         g.setColor(clrBackground);
         g.fill(bounds);
 
         g.setColor(clrLine);
         g.draw(walls);
 
         int stepX, stepY;
         stepX = (int) bounds.getWidth() / (maze.getWidth() - 1);
         stepY = (int) bounds.getHeight() / (maze.getHeight() - 1);
 
         g.setColor(clrLine);
         for (int row = 0; row < maze.getHeight(); row++) {
             for (int col = 0; col < maze.getWidth(); col++) {
                 boolean bDown = maze.hasWallDown(row, col);
                 boolean bRight = maze.hasWallRight(row, col);
 
                 int x0 = col * (int) stepX,
                         y0 = row * (int) stepY;
                 if (bDown) {
                     g.setColor(clrLine);
                     g.drawLine(x0, y0, x0, y0 + stepX);
                 } else {
                     g.setColor(clrBackground);
                     g.drawLine(x0, y0 + 2, x0, y0 + stepY - 2);
                 }
 
                 if (bRight) {
                     g.setColor(clrLine);
                     g.drawLine(x0, y0, x0 + stepX, y0);
                 } else {
                     g.setColor(clrBackground);
                     g.drawLine(x0 + 2, y0, x0 + stepX - 2, y0);
                 }
             }
         }
         g.setColor(clrBackground);
         g.drawLine(stepX*(maze.getWidth())-1, maze.nExitPointRow*stepY, stepX*(maze.getWidth())-1, (maze.nExitPointRow+1)*stepY);
     }
 }
