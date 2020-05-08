 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.dstvo.lobber.view;
 
 import com.dstvo.lobber.model.GridPosition;
 import com.dstvo.lobber.LobberConstants;
 import com.dstvo.lobber.model.CellContent;
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Label;
 
 /**
  *
  * @author user
  */
 public class LobberGrid extends Container
 {
     LobberCell[][] cells;
 
     // TODO: Use layout instead of hard-coding bounds
     void initializeCells()
     {
         cells = new LobberCell[LobberConstants.ROW_COUNT][LobberConstants.COLUMN_COUNT];
        int cellWidth = getWidth() / LobberConstants.ROW_COUNT;
        int cellHeight = getHeight() / LobberConstants.COLUMN_COUNT;
         int cellY_Pos, cellX_Pos;
         for (int row = 0; row < cells.length; row++) //Each row
         {
             cellY_Pos = cellHeight * row;
             for (int column = 0; column < cells[row].length; column++)
             {
                 cellX_Pos = cellWidth * column;
                 LobberCell cell = cells[row][column] = new LobberCell();
                 cell.setBounds(cellX_Pos, cellY_Pos, cellWidth - 1, cellHeight - 1);
                 cell.setBackground(new Color(row * 15, column * 15, 100));
                 cell.setAlignment(Label.CENTER);
                 add(cell);
                 cell.setVisible(true);
             }
         }
     }
 
     // TODO: Modify logic to update only the changed cells
 //    public void updateCells(byte[][] cellValues, GridPosition currentOpponentPosition,
 //            GridPosition lastPlayerPosition, GridPosition lastOpponentPosition)
 //    {
 //        for (int row = 0; row < cellValues.length; row++)
 //        {
 //            for (int column = 0; column < cellValues[row].length; column++)
 //            {
 //                byte b = cellValues[row][column];
 //                System.out.println("b = " + b);
 //                if (b == CellContent.PLAYER_CELL)
 //                {
 //                    cells[row][column].setText("P");
 //                } else if (b == CellContent.OPPONENT_CELL)
 //                {
 //                    cells[row][column].setText("O");
 //                }
 //                if (currentOpponentPosition.getRow() == row && currentOpponentPosition.getColumn() == column)
 //                {
 //                    cells[row][column].setBackground(Color.white);
 //                }
 //                else
 //                {
 //                    cells[row][column].setBackground(new Color(row * 15, column * 15, 100));
 //                }
 //            }
 //        }
 //    }
 
     LobberCell lastPlayerSelectedCell;
     LobberCell lastOpponentSelectedCell;
     LobberCell lastFocusCell;
 
     void shiftFocusToCell(GridPosition currentPos)
     {
        LobberCell currentFocusCell = cells[currentPos.getRow()][currentPos.getColumn()];
        currentFocusCell.setHighlighted(true);
        if (null != lastFocusCell)
        {
            lastFocusCell.setHighlighted(false);
        }
        lastFocusCell = currentFocusCell;
     }
 
     void selectCell(GridPosition currentPos, byte cellValue)
     {
         LobberCell currentSelectedCell = cells[currentPos.getRow()][currentPos.getColumn()];
         currentSelectedCell.setCellValue(cellValue);
         currentSelectedCell.setSelected(true);
         switch (cellValue)
         {
             case CellContent.OPPONENT_CELL:
                 if (null != lastOpponentSelectedCell)
                 {
                     lastOpponentSelectedCell.setSelected(false);
                 }
                 lastOpponentSelectedCell = currentSelectedCell;
                 break;
             case CellContent.PLAYER_CELL:
                 if (null != lastPlayerSelectedCell)
                 {
                     lastPlayerSelectedCell.setSelected(false);
                 }
                 lastPlayerSelectedCell = currentSelectedCell;
                 break;
         }
     }
 }
