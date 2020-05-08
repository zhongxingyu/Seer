 package com.cozycoding.crea1.tetris;
 
 import com.cozycoding.crea1.tetris.blocks.Cell;
 import com.cozycoding.crea1.tetris.blocks.TetrisBlock;
 
 import javax.swing.JPanel;
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.geom.RoundRectangle2D;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author Marius Kristensen
  */
 public class GamePanel extends JPanel {
 
     private final Double height;
     private final Double width;
     private final Double cellSize;
     private List<Cell> cells = new ArrayList<>();
     private List<Cell> board = new ArrayList<>();
 
     public GamePanel(Dimension dimension) {
         setPreferredSize(dimension);
         this.width = dimension.getWidth();
         this.height = dimension.getHeight();
         this.cellSize = dimension.getWidth() / 10;
         setFocusable(true);
         setVisible(true);
     }
 
     @Override
     public void paintComponent(Graphics g) {
         clear(g);
         Graphics2D graphics2D = (Graphics2D) g;
 
         for (Cell cell : board) {
             graphics2D.setColor(cell.getColor());
             graphics2D.fill(createRectangle(cell.getX(), cell.getY()));
             graphics2D.setStroke(new BasicStroke(1));
             graphics2D.setColor(new Color(0xBBBBBB));
             graphics2D.draw(createRectangle(cell.getX(), cell.getY()));
         }
 
         for (Cell cell : cells) {
             graphics2D.setColor(cell.getColor());
             graphics2D.fill(createRectangle(cell.getX(), cell.getY()));
         }
     }
 
     public void paintGameBoard(Cell[][] gameBoard, TetrisBlock activeBlock) {
        board.removeAll(board);
        cells.removeAll(cells);
         for (int i = 0; i < gameBoard.length; i++) {
             for (int j = 0; j < gameBoard[i].length; j++) {
                 Cell cell = gameBoard[i][j];
                 board.add(cell);
             }
         }
         cells.addAll(activeBlock.getShape());
         repaint();
     }
 
     private void clear(Graphics g) {
         super.paintComponent(g);
     }
 
     private RoundRectangle2D.Double createRectangle(double x, double y) {
         return new RoundRectangle2D.Double(x * cellSize, y * cellSize, cellSize, cellSize, 0, 0);
     }
 }
