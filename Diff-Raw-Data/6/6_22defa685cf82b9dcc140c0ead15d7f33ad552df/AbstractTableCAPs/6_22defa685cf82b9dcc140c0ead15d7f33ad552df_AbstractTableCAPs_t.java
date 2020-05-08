 /*******************************************************************************
  * Copyright (c) 2012 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation 
  *******************************************************************************/
 package org.eclipse.jubula.rc.common.caps;
 
 import java.awt.Rectangle;
 import org.eclipse.jubula.rc.common.CompSystemConstants;
 import org.eclipse.jubula.rc.common.driver.ClickOptions;
 import org.eclipse.jubula.rc.common.driver.DragAndDropHelper;
 import org.eclipse.jubula.rc.common.exception.StepExecutionException;
 import org.eclipse.jubula.rc.common.implclasses.IndexConverter;
 import org.eclipse.jubula.rc.common.implclasses.MatchUtil;
 import org.eclipse.jubula.rc.common.implclasses.Verifier;
 import org.eclipse.jubula.rc.common.implclasses.table.Cell;
 import org.eclipse.jubula.rc.common.logger.AutServerLogger;
 import org.eclipse.jubula.rc.common.uiadapter.interfaces.ITableAdapter;
 import org.eclipse.jubula.tools.constants.InputConstants;
 import org.eclipse.jubula.tools.objects.event.EventFactory;
 import org.eclipse.jubula.tools.objects.event.TestErrorEvent;
 
 /**
  * General implementation for tables.
  * 
  * @author BREDEX GmbH
  */
 public abstract class AbstractTableCAPs extends AbstractTextInputSupport {
     
     /** the logger */
     private static AutServerLogger log = new AutServerLogger(
             AbstractMenuCAPs.class);
 
     /**
      * @return the log
      */
     public static AutServerLogger getLog() {
         return log;
     }
     
     /**
      * This method is mostly needed for clicks
      * @return the real table as object
      */
     private Object getRealTable() {
         return getComponent().getRealComponent();
     }
     /**
      * 
      * @return the ITableAdapter of this table
      */
     private ITableAdapter getTableAdapter() {
         return (ITableAdapter) getComponent();
     }
     
     /**
      * Verifies the rendered text inside the currently selected cell.
      *
      * @param text The cell text to verify.
      * @throws StepExecutionException
      *      If there is no selected cell, or if the rendered text cannot
      *      be extracted.
      */
     public void gdVerifyText(String text)
         throws StepExecutionException {
 
         gdVerifyText(text, MatchUtil.DEFAULT_OPERATOR);
     }
     
     /**
      * Verifies the rendered text inside the currently selected cell.
      * @param text The cell text to verify.
      * @param operator The operation used to verify
      * @throws StepExecutionException If there is no selected cell, or if the rendered text cannot be extracted.
      */
     public void gdVerifyText(String text, String operator)
         throws StepExecutionException {
         ITableAdapter adapter = getTableAdapter();
         Cell cell = adapter.getSelectedCell();
         final int implRow = cell.getRow();
         final int implCol = cell.getCol();
         checkRowColBounds(implRow, implCol);
 
         adapter.scrollCellToVisible(implRow, implCol);
         final String current = getCellText(implRow, implCol);
         Verifier.match(current, text, operator);
     }
     
     /**
      * Verifies the rendered text inside the passed cell.
      * @param row The row of the cell.
      * @param rowOperator The row header operator
      * @param col The column of the cell.
      * @param colOperator The column header operator
      * @param text The cell text to verify.
      * @param operator The operation used to verify
      * @throws StepExecutionException If the row or the column is invalid, or if the rendered text cannot be extracted.
      */
     public void gdVerifyText(String text, String operator, final String row,
             final String rowOperator, final String col,
             final String colOperator) throws StepExecutionException {
         ITableAdapter adapter = getTableAdapter();
         final int implRow = adapter.getRowFromString(row, rowOperator);
         final int implCol = adapter.getColumnFromString(col, colOperator);
         String current;
         //if row is header and column is existing
         if (implRow == -1 && implCol > -1) {
             current = adapter.getColumnName(implCol);        
         } else {
             checkRowColBounds(implRow, implCol);
             adapter.scrollCellToVisible(implRow, implCol);
             current = getCellText(implRow, implCol);
         }
         
         
 
         
         Verifier.match(current, text, operator);
     }
     
     /**
      * Selects the cell of the Table.<br>
      * With the xPos, yPos, xunits and yUnits the click position inside the cell can be defined.
      * @param row The row of the cell.
      * @param rowOperator The row header operator
      * @param col The column of the cell.
      * @param colOperator The column header operator
      * @param clickCount The number of clicks with the right mouse button
      * @param xPos what x position
      * @param xUnits should x position be pixel or percent values
      * @param yPos what y position
      * @param yUnits should y position be pixel or percent values
      * @param extendSelection Should this selection be part of a multiple selection
      * @param button what mouse button should be used
      * @throws StepExecutionException If the row or the column is invalid
      */
     public void gdSelectCell(final String row, final String rowOperator,
         final String col, final String colOperator,
         final int clickCount, final int xPos, final String xUnits,
         final int yPos, final String yUnits, final String extendSelection,
         int button) 
         throws StepExecutionException {
         ITableAdapter adapter = getTableAdapter();
         final int implRow = adapter.getRowFromString(row, rowOperator);
         final int implCol = adapter.getColumnFromString(col, colOperator);
         final boolean isExtendSelection = extendSelection.equals(
                 CompSystemConstants.EXTEND_SELECTION_YES); 
         if (log.isDebugEnabled()) {
             log.debug("Selecting row, col: " + row + ", " + col); //$NON-NLS-1$//$NON-NLS-2$
         }
         
         Rectangle cellBounds;
         //if row is header and col is existing
         if (implRow == -1 && implCol > -1) {
             cellBounds = adapter.getHeaderBounds(implCol);
         } else {
             cellBounds = adapter.scrollCellToVisible(implRow, implCol);
         }        
         Object o = getSpecificRectangle(cellBounds);
         ClickOptions clickOptions = ClickOptions.create();
         clickOptions.setClickCount(clickCount).setScrollToVisible(false);
         clickOptions.setMouseButton(button);
         try {
             if (isExtendSelection) {
                 getRobot().keyPress(getRealTable(),
                         getExtendSelectionModifier());
             }
             getRobot().click(getRealTable(), o, clickOptions, 
                     xPos, xUnits.equalsIgnoreCase(POS_UNIT_PIXEL), 
                     yPos, yUnits.equalsIgnoreCase(POS_UNIT_PIXEL));
         } finally {
             if (isExtendSelection) {
                 getRobot().keyRelease(getRealTable(),
                         getExtendSelectionModifier());
             }
         }
     }
     /**
      * This is a workaround because the toolkit specific
      * Robot implementation are using different rectangle types.
      * @param rectangle the java.awt.rectangle which needs to 
      *          casted
      * @return the rectangle in the type for the specific robot
      */
     protected Object getSpecificRectangle(Rectangle rectangle) {
         //FIXME changing the Robots might be useful here
         return rectangle;
     }
     /**
      * Verifies, if value exists in column.
      *
      * @param col The column of the cell.
      * @param colOperator the column header operator
      * @param value The cell text to verify.
      * @param operator The operation used to verify
      * @param searchType Determines where the search begins ("relative" or "absolute")
      * @param exists true if value exists, false otherwise
      * @throws StepExecutionException
      *             If the row or the column is invalid, or if the rendered text
      *             cannot be extracted.
      */
     public void gdVerifyValueInColumn(final String col,
             final String colOperator, final String value,
             final String operator, final String searchType, boolean exists)
         throws StepExecutionException {
         ITableAdapter adapter = getTableAdapter();
         final int implCol = adapter.getColumnFromString(col, colOperator);
         
         
         boolean valueExists = isValueExisting(adapter, implCol,
                 value, operator, searchType);
 
         Verifier.equals(exists, valueExists);
     }
     /**
      * Looks if value exists in the Column.
      * 
      * @param adapter the teble adapter working on.
      * @param implCol the implementation column of the cell.
      * @param value the cellt text to verify.
      * @param operator The operation used to verify.
      * @param searchType searchType Determines where the search begins ("relative" or "absolute")
      * @return <code>true</code> it the value exists in the column
      */
     private boolean isValueExisting(ITableAdapter adapter, int implCol, 
             String value, String operator, final String searchType) {
         final int rowCount = adapter.getRowCount();
         for (int i = getStartingRowIndex(searchType);
                 i < rowCount; ++i) {
             if (MatchUtil.getInstance().match(getCellText(i,
                             implCol), value, operator)) {
                 return true;
             }
         }
         if (adapter.isHeaderVisible()) {
             String header = adapter.getColumnName(implCol);
             if (MatchUtil.getInstance().match(header, value,
                                 operator)) {
                 return true;
             }
         }
         return false;
     }
     
     
     
     /**
      * Verifies, if value exists in row..
      *
      * @param row The row of the cell.
      * @param rowOperator the row header operator
      * @param value The cell text to verify.
      * @param operator The operation used to verify
      * @param searchType Determines where the search begins ("relative" or "absolute")
      * @param exists true if value exists, false otherwise
      * @throws StepExecutionException
      *             If the row or the column is invalid, or if the rendered text
      *             cannot be extracted.
      */
     public void gdVerifyValueInRow(final String row, final String rowOperator,
             final String value, final String operator, final String searchType,
             boolean exists)
         throws StepExecutionException {
         final ITableAdapter adapter = getTableAdapter();
         final int implRow = adapter.getRowFromString(row, rowOperator);
         boolean valueIsExisting = false;
         //if row is header
         if (implRow == -1) {
             
             for (int k = getStartingColIndex(searchType); 
                                     k < adapter.getColumnCount(); ++k) {
                 if (MatchUtil.getInstance().match(
                         adapter.getColumnName(k),
                         value, operator)) {
                     valueIsExisting = true;
                     break;
                 }
             }
                             
                         
         } else {
             
             final int columnCount = adapter.getColumnCount();
             if (columnCount > 0) {
                 for (int i = getStartingColIndex(searchType); 
                         i < columnCount; ++i) {
                     if (MatchUtil.getInstance().match(
                             getCellText(implRow, i), value, operator)) {
                         valueIsExisting = true;
                         break;
                     }
                 }
             } else {
                 // No columns found. This table is used to present a
                 // list-like component.
                 if (MatchUtil.getInstance().match(
                         adapter.getRowName(implRow),
                             value, operator)) {
                     valueIsExisting = true;
                     
                 }
             }             
   
         }
         Verifier.equals(exists, valueIsExisting);
     }
     
     /**
      * Verifies the editable property of the given indices.
      *
      * @param editable
      *            The editable property to verify.
      * @param row the row to select
      * @param rowOperator the row header operator
      * @param col the column to select
      * @param colOperator the column header operator
      */
     public void gdVerifyEditable(boolean editable, String row,
             String rowOperator, String col, String colOperator) {
         //if row is header row
         
         if (getTableAdapter().getRowFromString(row, rowOperator) == -1) {
             throw new StepExecutionException("Unsupported Header Action", //$NON-NLS-1$
                     EventFactory.createActionError(
                             TestErrorEvent.UNSUPPORTED_HEADER_ACTION));
         }        
         gdSelectCell(row, rowOperator, col, colOperator, ClickOptions.create(),
                 CompSystemConstants.EXTEND_SELECTION_NO);
         gdVerifyEditable(editable);
     }
     
     
     /**
      * Selects a table cell in the given row and column via click in the midle of the cell.
      * @param row The row of the cell.
      * @param rowOperator The row header operator
      * @param col The column of the cell.
      * @param colOperator The column header operator
      * @param co the click options to use
      * @param extendSelection Should this selection be part of a multiple selection
      */
     private void gdSelectCell(final String row, final String rowOperator,
             final String col, final String colOperator,
             final ClickOptions co, final String extendSelection) {
             
         gdSelectCell(row, rowOperator, col, colOperator, co.getClickCount(),
                     50, POS_UNI_PERCENT, 50, POS_UNI_PERCENT, extendSelection, 
                     co.getMouseButton());
     }
     
     
     /**
      * Verifies the rendered text inside cell at the mouse position on screen.
      *
      * @param text The cell text to verify.
      * @param operator The operation used to verify
      * @throws StepExecutionException If there is no selected cell, or if the
      *                              rendered text cannot be extracted.
      */
     public void gdVerifyTextAtMousePosition(String text, String operator)
         throws StepExecutionException {        
         if (isMouseOnHeader()) {
             throw new StepExecutionException("Unsupported Header Action", //$NON-NLS-1$
                     EventFactory.createActionError(
                             TestErrorEvent.UNSUPPORTED_HEADER_ACTION));
         }        
         Cell cell = getCellAtMousePosition();
         gdVerifyText(text, operator, 
                 Integer.toString(IndexConverter.toUserIndex(cell.getRow())),
                 MatchUtil.EQUALS, 
                 Integer.toString(IndexConverter.toUserIndex(cell.getCol())),
                 MatchUtil.EQUALS);
     }
     
     /**
      * Verifies the editable property of the selected cell.
      *
      * @param editable the editable property to verify.
      */
     public void gdVerifyEditableSelected(boolean editable) {
         gdVerifyEditable(editable);
     }
     
     /**
      * Verifies the editable property of the current selected cell.
      *
      * @param editable The editable property to verify.
      */
     public void gdVerifyEditable(boolean editable) {
         Cell cell = getTableAdapter().getSelectedCell();
         
         Verifier.equals(editable, getTableAdapter()
                 .isCellEditable(cell.getRow(), cell.getCol()));
     }
     
     /**
      * Verifies the editable property of the cell under current mouse position.
      *
      * @param editable the editable property to verify.
      */
     public void gdVerifyEditableMousePosition(boolean editable) {
         //if row is header row
         if (isMouseOnHeader()) {
             throw new StepExecutionException("Unsupported Header Action", //$NON-NLS-1$
                     EventFactory.createActionError(
                             TestErrorEvent.UNSUPPORTED_HEADER_ACTION));
         }
         Cell cell = getCellAtMousePosition();
         boolean isEditable = getTableAdapter().isCellEditable(
                 cell.getRow(), cell.getCol());
         Verifier.equals(editable, isEditable);
     }
     /**
      * Finds the first row which contains the value <code>value</code>
      * in column <code>col</code> and selects this row.
      * @param col the column
      * @param colOperator the column header operator
      * @param value the value
      * @param clickCount the number of clicks.
      * @param regexOp the regex operator
      * @param extendSelection Should this selection be part of a multiple selection
      * @param searchType Determines where the search begins ("relative" or "absolute")
      * @param button what mouse button should be used
      */
     public void gdSelectRowByValue(String col, String colOperator,
             final String value, final String regexOp, int clickCount,
             final String extendSelection, final String searchType, int button) {
         gdSelectRowByValue(col, colOperator, value, regexOp, extendSelection,
                 searchType, ClickOptions.create()
                         .setClickCount(clickCount)
                         .setMouseButton(button));
     }
     
     /**
      * Finds the first row which contains the value <code>value</code>
      * in column <code>col</code> and selects this row.
      *
      * @param col the column
      * @param colOperator the column header operator
      * @param value the value
      * @param regexOp the regex operator
      * @param extendSelection Should this selection be part of a multiple selection
      * @param searchType Determines where the search begins ("relative" or "absolute")
      * @param co the clickOptions to use
      */
     protected void gdSelectRowByValue(String col, String colOperator,
         final String value, final String regexOp, final String extendSelection,
         final String searchType, ClickOptions co) {
         ITableAdapter adapter = getTableAdapter();
         final int implCol = adapter.getColumnFromString(col, colOperator);
         Integer implRow = null;
         final int rowCount = adapter.getRowCount();
         
         for (int i = getStartingRowIndex(searchType); i < rowCount; ++i) {
             if (MatchUtil.getInstance().match(getCellText(i, implCol), 
                     value, regexOp)) {
 
                 implRow = new Integer(i);
                 break;
             }
         }
         if (implRow == null) {
             String header = adapter.getColumnName(implCol);
             if (MatchUtil.getInstance().match(header, value, regexOp)) {
                 implRow = new Integer(-1);
             }
         }
 
         if (implRow == null) {
             throw new StepExecutionException("no such row found", //$NON-NLS-1$
                     EventFactory.createActionError(TestErrorEvent.NOT_FOUND));
         }
         
         String  userIdxRow = new Integer(IndexConverter.toUserIndex(
                 implRow.intValue())).toString();
         String  userIdxCol = new Integer(IndexConverter.toUserIndex(
                 implCol)).toString();            
         
         gdSelectCell(userIdxRow, MatchUtil.EQUALS, userIdxCol, colOperator, co,
                 extendSelection);
     }
     
     /**
      * Finds the first column which contains the value <code>value</code>
      * in the given row and selects the cell.
      *
      * @param row the row to select
      * @param rowOperator the row header operator
      * @param value the value
      * @param clickCount the number of clicks
      * @param regex search using regex
      * @param extendSelection Should this selection be part of a multiple selection
      * @param searchType Determines where the search begins ("relative" or "absolute")
      * @param button what mouse button should be used
      */
     public void gdSelectCellByColValue(String row, String rowOperator,
         final String value, final String regex, int clickCount,
         final String extendSelection, final String searchType, int button) {
         gdSelectCellByColValue(row, rowOperator, value, regex, extendSelection,
                 searchType, ClickOptions.create()
                     .setClickCount(clickCount)
                     .setMouseButton(button));
     }
     
     /**
      * Finds the first column which contains the value <code>value</code>
      * in the given row and selects the cell.
      *
      * @param row the row
      * @param rowOperator the row header operator
      * @param value the value
      * @param regex search using regex
      * @param extendSelection Should this selection be part of a multiple selection
      * @param searchType Determines where the search begins ("relative" or "absolute")
      * @param co the click options to use
      */
     protected void gdSelectCellByColValue(String row, String rowOperator,
         final String value, final String regex, final String extendSelection,
         final String searchType, ClickOptions co) { 
         ITableAdapter adapter = getTableAdapter();
         final int implRow = adapter.getRowFromString(row, rowOperator);
         int colCount = adapter.getColumnCount();
         Integer implCol = null;
         if (implRow == -1) {
             
             for (int i = getStartingColIndex(searchType); i < colCount; ++i) {
                 if (MatchUtil.getInstance().match(adapter.getColumnName(i), 
                         value, regex)) {
                     implCol = new Integer(i);
                     break;
                 }
             }           
         } else {
             for (int i = getStartingColIndex(searchType); i < colCount; ++i) {
                 if (MatchUtil.getInstance().match(getCellText(implRow, i), 
                         value, regex)) {
 
                     implCol = new Integer(i);
                     break;
                 }
             } 
         }
         if (implCol == null) {
             throw new StepExecutionException("no such cell found", EventFactory //$NON-NLS-1$
                     .createActionError(TestErrorEvent.NOT_FOUND));
         }
         
         String usrIdxRowStr = new Integer(IndexConverter.toUserIndex(
                 implRow)).toString();
         String usrIdxColStr = new Integer(IndexConverter.toUserIndex(
                 implCol.intValue())).toString();
         
         gdSelectCell(usrIdxRowStr, rowOperator, usrIdxColStr, MatchUtil.EQUALS,
                 co, extendSelection);
         
     }
 //FIXME    
 //    /**
 //     * Action to read the value of the current selected cell of the JTable 
 //     * to store it in a variable in the Client
 //     * @param variable the name of the variable
 //     * @return the text value.
 //     */
 //    public String gdReadValue(String variable) {
 //        final Cell selectedCell = getTableAdapter().getSelectedCell();
 //        return getCellText(selectedCell.getRow(), selectedCell.getCol());
 //    }
     
     /**
      * Action to read the value of the passed cell of the JTable
      * to store it in a variable in the Client
      * @param variable the name of the variable
      * @param row the row to select
      * @param rowOperator the row header operator
      * @param col the column to select
      * @param colOperator the column header operator
      * @return the text value.
      */
     public String gdReadValue(String variable, String row, String rowOperator,
             String col, String colOperator) {
         ITableAdapter adapter = getTableAdapter();
         final int implRow = adapter.getRowFromString(row, rowOperator);
         final int implCol = adapter.getColumnFromString(col, colOperator);
         
         //if row is header and column is existing
         if (implRow == -1 && implCol > -1) {
             return adapter.getColumnName(implCol); 
         }
         
         checkRowColBounds(implRow, implCol);
 
         adapter.scrollCellToVisible(implRow, implCol);
         return getCellText(implRow, implCol);
     }
     
     /**
      * {@inheritDoc}
      */
     public String gdReadValueAtMousePosition(String variable) {
         Cell cellAtMousePosition = getTableAdapter().getSelectedCell();
         return getCellText(cellAtMousePosition.getRow(), 
                 cellAtMousePosition.getCol());
     }
     
     /**
      * Tries to click in the cell under the mouse position. If the mouse is not
      * over a cell, the current selected cell will be clicked on. If there is no
      * selected cell, the middle of the table is used to click on.
      * @param count Number of clicks
      * @param button The mouse button
      */
     public void gdClick(int count, int button) {
         ITableAdapter adapter = getTableAdapter();
         Cell cell = null;
         if (isMouseOverCell()) {
             cell = getCellAtMousePosition();
         } else if (adapter.hasCellSelection()) {
             cell = adapter.getSelectedCell();
         } 
         if (cell != null) {
             Rectangle cellRect = 
                     adapter.scrollCellToVisible(cell.getRow(), cell.getCol());
            Object robotSpecifcRectangle = getSpecificRectangle(cellRect);
            getRobot().click(getRealTable(), robotSpecifcRectangle,
                    ClickOptions.create().setClickCount(count)
                    .setMouseButton(button));
         } else {
             super.gdClick(count, button);
         }
     }
    
     /**
      * Selects a cell relative to the cell at the current mouse position.
      * If the mouse is not at any cell, the current selected cell is used.
      * @param direction the direction to move.
      * @param cellCount the amount of cells to move
      * @param clickCount the click count to select the new cell.
      * @param xPos what x position
      * @param xUnits should x position be pixel or percent values
      * @param yPos what y position
      * @param yUnits should y position be pixel or percent values
      * @param extendSelection Should this selection be part of a multiple selection
      * @throws StepExecutionException if any error occurs
      */
     public void gdMove(String direction, int cellCount, int clickCount,
         final int xPos, final String xUnits,
         final int yPos, final String yUnits, final String extendSelection)
         throws StepExecutionException {
         if (isMouseOnHeader()) {
             throw new StepExecutionException("Unsupported Header Action", //$NON-NLS-1$
                     EventFactory.createActionError(
                             TestErrorEvent.UNSUPPORTED_HEADER_ACTION));
         }
         Cell currCell = null;
         try {
             currCell = getCellAtMousePosition();
         } catch (StepExecutionException e) {
             currCell = getTableAdapter().getSelectedCell();
         }
         int newCol = currCell.getCol();
         int newRow = currCell.getRow();
         if (CompSystemConstants.TABLE_MOVE_UP
                 .equalsIgnoreCase(direction)) {
             newRow -= cellCount;
         } else if (CompSystemConstants.TABLE_MOVE_DOWN
                 .equalsIgnoreCase(direction)) {
             newRow += cellCount;
         } else if (CompSystemConstants.TABLE_MOVE_LEFT
                 .equalsIgnoreCase(direction)) {
             newCol -= cellCount;
         } else if (CompSystemConstants.TABLE_MOVE_RIGHT
                 .equalsIgnoreCase(direction)) {
             newCol += cellCount;
         }
         newRow = IndexConverter.toUserIndex(newRow);
         newCol = IndexConverter.toUserIndex(newCol);
         String row = Integer.toString(newRow);
         String col = Integer.toString(newCol);
         gdSelectCell(row, MatchUtil.DEFAULT_OPERATOR , col, 
                 MatchUtil.DEFAULT_OPERATOR, clickCount, xPos,
                 xUnits, yPos, yUnits, extendSelection, 
                 InputConstants.MOUSE_BUTTON_LEFT);
 //        gdSelectCell(newRow, newCol, clickCount,
 //                     xPos, xUnits, yPos, yUnits, extendSelection);
     }
     
     /**
      * Writes the passed text into the currently selected cell.
      * 
      * @param text
      *            The text.
      * @throws StepExecutionException
      *             If there is no selected cell, or if the cell is not editable,
      *             or if the table cell editor permits the text to be written.
      */
     public void gdInputText(final String text) throws StepExecutionException {
         inputText(text, false);
     }
     
     /**
      * Types the text in the specified cell.
      * @param text The text
      * @param row The row of the cell.
      * @param rowOperator The row operator
      * @param col The column of the cell.
      * @param colOperator The column operator
      * @throws StepExecutionException If the text input fails
      */
     public void gdInputText(String text, String row, String rowOperator,
             String col, String colOperator)
         throws StepExecutionException {
         //if row is header row
         if (getTableAdapter().getRowFromString(row, rowOperator) == -1) {
             throw new StepExecutionException("Unsupported Header Action", //$NON-NLS-1$
                     EventFactory.createActionError(
                             TestErrorEvent.UNSUPPORTED_HEADER_ACTION));
         }
         gdSelectCell(row, rowOperator, col, colOperator, 
                 ClickOptions.create().setClickCount(1), 
                 CompSystemConstants.EXTEND_SELECTION_NO);
         gdInputText(text);
     }
     
     /**
      * Types <code>text</code> into the component. This replaces the shown
      * content in the current selected cell.
      * 
      * @param text the text to type in
      * @throws StepExecutionException
      *  If there is no selected cell, or if the cell is not editable,
      *  or if the table cell editor permits the text to be written.
      */
     public void gdReplaceText(String text) throws StepExecutionException {
         inputText(text, true);
     }
     
     /**
      * Replaces the given text in the given cell coordinates
      * @param text the text to replace
      * @param row The row of the cell.
      * @param rowOperator The row operator
      * @param col The column of the cell.
      * @param colOperator The column operator
      */
     public void gdReplaceText(String text, String row, String rowOperator,
             String col, String colOperator) {
         //if row is header row
         if (getTableAdapter().getRowFromString(row, rowOperator) == -1) {
             throw new StepExecutionException("Unsupported Header Action", //$NON-NLS-1$
                     EventFactory.createActionError(
                             TestErrorEvent.UNSUPPORTED_HEADER_ACTION));
         }
         gdSelectCell(row, rowOperator, col, colOperator, 
                 ClickOptions.create().setClickCount(1), 
                 CompSystemConstants.EXTEND_SELECTION_NO);
         inputText(text, true);
     }
     
     /**
      * Drags the cell of the Table.<br>
      * With the xPos, yPos, xunits and yUnits the click position inside the
      * cell can be defined.
      *
      * @param mouseButton the mouseButton.
      * @param modifier the modifier.
      * @param row the row to select
      * @param rowOperator the row header operator
      * @param col the column to select
      * @param colOperator the column header operator
      * @param xPos what x position
      * @param xUnits should x position be pixel or percent values
      * @param yPos what y position
      * @param yUnits should y position be pixel or percent values
      * @throws StepExecutionException
      *             If the row or the column is invalid
      */
     public void gdDragCell(final int mouseButton, final String modifier,
             final String row, final String rowOperator,
             final String col, final String colOperator, final int xPos,
             final String xUnits, final int yPos, final String yUnits)
         throws StepExecutionException {
 
         final DragAndDropHelper dndHelper = DragAndDropHelper.getInstance();
         dndHelper.setModifier(modifier);
         dndHelper.setMouseButton(mouseButton);
         dndHelper.setDragComponent(null);
         gdSelectCell(row, rowOperator, col, colOperator, 0, xPos, xUnits, yPos,
                 yUnits, CompSystemConstants.EXTEND_SELECTION_NO, 1);
         pressOrReleaseModifiers(modifier, true);
         getRobot().mousePress(null, null, mouseButton);
     }
     
     /**
      * Drops on the cell of the JTable.<br>
      * With the xPos, yPos, xunits and yUnits the click position inside the
      * cell can be defined.
      *
      * @param row the row to select
      * @param rowOperator the row header operator
      * @param col the column to select
      * @param colOperator the column header operator
      * @param xPos what x position
      * @param xUnits should x position be pixel or percent values
      * @param yPos what y position
      * @param yUnits should y position be pixel or percent values
      * @param delayBeforeDrop the amount of time (in milliseconds) to wait
      *                        between moving the mouse to the drop point and
      *                        releasing the mouse button
      * @throws StepExecutionException
      *             If the row or the column is invalid
      */
     public void gdDropCell(final String row, final String rowOperator,
             final String col, final String colOperator, final int xPos,
             final String xUnits, final int yPos, final String yUnits,
             int delayBeforeDrop) throws StepExecutionException {
 
         final DragAndDropHelper dndHelper = DragAndDropHelper.getInstance();
         try {
             gdSelectCell(row, rowOperator, col, colOperator, 0, xPos, xUnits,
                     yPos, yUnits, CompSystemConstants.EXTEND_SELECTION_NO, 1);
             waitBeforeDrop(delayBeforeDrop);
         } finally {
             getRobot().mouseRelease(null, null, dndHelper.getMouseButton());
             pressOrReleaseModifiers(dndHelper.getModifier(), false);
         }
     }
     
     /**
      * Finds the first row which contains the value <code>value</code>
      * in column <code>col</code> and drags this row.
      *
      * @param mouseButton the mouse button
      * @param modifier the modifier
      * @param col the column
      * @param colOperator the column header operator
      * @param value the value
      * @param regexOp the regex operator
      * @param searchType Determines where the search begins ("relative" or "absolute")
      */
     public void gdDragRowByValue(int mouseButton, String modifier, String col,
             String colOperator, final String value, final String regexOp,
             final String searchType) {
 
         final DragAndDropHelper dndHelper = DragAndDropHelper.getInstance();
         dndHelper.setModifier(modifier);
         dndHelper.setMouseButton(mouseButton);
         gdSelectRowByValue(col, colOperator, value, regexOp, 1,
                 CompSystemConstants.EXTEND_SELECTION_NO, searchType, 1);
         pressOrReleaseModifiers(modifier, true);
         getRobot().mousePress(null, null, mouseButton);
     }
     
     /**
      * Finds the first row which contains the value <code>value</code>
      * in column <code>col</code> and drops on this row.
      *
      * @param col the column to select
      * @param colOperator the column header operator
      * @param value the value
      * @param regexOp the regex operator
      * @param searchType Determines where the search begins ("relative" or "absolute")
      * @param delayBeforeDrop the amount of time (in milliseconds) to wait
      *                        between moving the mouse to the drop point and
      *                        releasing the mouse button
      */
     public void gdDropRowByValue(String col, String colOperator,
             final String value, final String regexOp, final String searchType,
             int delayBeforeDrop) {
         
         final DragAndDropHelper dndHelper = DragAndDropHelper.getInstance();
         try {
             gdSelectRowByValue(col, colOperator, value, regexOp,
                     CompSystemConstants.EXTEND_SELECTION_NO, 
                     searchType, ClickOptions
                     .create().setClickCount(0));
             waitBeforeDrop(delayBeforeDrop);
         } finally {
             getRobot().mouseRelease(null, null, dndHelper.getMouseButton());
             pressOrReleaseModifiers(dndHelper.getModifier(), false);
         }
     }
     
     /**
      * Finds the first column which contains the value <code>value</code>
      * in the given row and drags the cell.
      *
      * @param mouseButton the mouse button
      * @param modifier the modifiers
      * @param row the row to select
      * @param rowOperator the row header operator
      * @param value the value
      * @param regex search using regex
      * @param searchType Determines where the search begins ("relative" or "absolute")
      */
     public void gdDragCellByColValue(int mouseButton, String modifier,
             String row, String rowOperator, final String value,
             final String regex, final String searchType) {
 
         final DragAndDropHelper dndHelper = DragAndDropHelper.getInstance();
         dndHelper.setModifier(modifier);
         dndHelper.setMouseButton(mouseButton);
 
         gdSelectCellByColValue(row, rowOperator, value, regex,
                 CompSystemConstants.EXTEND_SELECTION_NO, searchType, 
                 ClickOptions.create().setClickCount(0));
         pressOrReleaseModifiers(modifier, true);
         getRobot().mousePress(null, null, mouseButton);
     }
     
     /**
      * Finds the first column which contains the value <code>value</code>
      * in the given row and drops on the cell.
      *
      * @param row the row to select
      * @param rowOperator the row header operator
      * @param value the value
      * @param regex search using regex
      * @param searchType Determines where the search begins ("relative" or "absolute")
      * @param delayBeforeDrop the amount of time (in milliseconds) to wait
      *                        between moving the mouse to the drop point and
      *                        releasing the mouse button
      */
     public void gdDropCellByColValue(String row, String rowOperator,
             final String value, final String regex, final String searchType,
             int delayBeforeDrop) {
 
         final DragAndDropHelper dndHelper = DragAndDropHelper.getInstance();
         try {
             gdSelectCellByColValue(row, rowOperator, value, regex, 
                     CompSystemConstants.EXTEND_SELECTION_NO, searchType, 
                     ClickOptions.create().setClickCount(0));
             waitBeforeDrop(delayBeforeDrop);
         } finally {
             getRobot().mouseRelease(null, null, dndHelper.getMouseButton());
             pressOrReleaseModifiers(dndHelper.getModifier(), false);
         }
     }
     
 
     /**
      * Gets the text from the specific cell which is given
      * by the row and the column.
      * @param row the zero based index of the row
      * @param column the zero based index of the column
      * @return the text of the cell of the given coordinates
      */
     private String getCellText(final int row, final int column) {
         return getTableAdapter().getCellText(row, column);
 
     }
         
     /**
      * Checks whether <code>0 <= value < count</code>. 
      * @param value The value to check.
      * @param count The upper bound.
      */
     private void checkBounds(int value, int count) {
         if (value < 0 || value >= count) {
             throw new StepExecutionException("Invalid row/column: " + value, //$NON-NLS-1$
                 EventFactory.createActionError(
                         TestErrorEvent.INVALID_INDEX_OR_HEADER));
         }
     }
     
     /**
      * Checks if the passed row and column are inside the bounds of the Table. 
      * @param row The row
      * @param column The column
      * @throws StepExecutionException If the row or the column is outside of the Table's bounds.
      */
     protected void checkRowColBounds(int row, int column)
         throws StepExecutionException {
         ITableAdapter adapter = getTableAdapter();
         checkBounds(row, adapter.getRowCount());
         
         // Corner case: Only check the bounds if the table is not being
         //              used as a list or anything other than the first column
         //              is being checked.
         int colCount = adapter.getColumnCount();
         if (colCount > 0 || column > 0) {
             checkBounds(column, colCount);
         }
     }
     
     /**
      * @param searchType Determines column where the search begins ("relative" or "absolute")
      * @return The index from which to begin a search, based on the search type
      *         and (if appropriate) the currently selected cell.
      */
     private int getStartingColIndex(String searchType) {
         int startingIndex = 0;
         if (searchType.equalsIgnoreCase(
                 CompSystemConstants.SEARCH_TYPE_RELATIVE)) {
             startingIndex = getTableAdapter().getSelectedCell().getCol() + 1;
         }
         return startingIndex;
     }
 
     /**
      * @param searchType Determines the row where the search begins ("relative" or "absolute")
      * @return The index from which to begin a search, based on the search type
      *         and (if appropriate) the currently selected cell.
      */
     private int getStartingRowIndex(String searchType) {
         int startingIndex = 0;
         if (searchType.equalsIgnoreCase(
                 CompSystemConstants.SEARCH_TYPE_RELATIVE)) {
             startingIndex = getTableAdapter().getSelectedCell().getRow() + 1;
         }
         return startingIndex;
     }
     
     /**
      * inputs/replaces the given text
      * @param text the text to input
      * @param replace wheter to replace or not
      * @throws StepExecutionException If there is no selected cell,
      * or if the cell is not editable, or if the table cell editor permits
      * the text to be written.
      */
     private void inputText(final String text, boolean replace) 
         throws StepExecutionException {
         ITableAdapter adapter = getTableAdapter();
         
         final Cell cell = adapter.getSelectedCell();
         // Ensure that the cell is visible.
         Rectangle rectangle = 
                 adapter.scrollCellToVisible(cell.getRow(), cell.getCol());
         
         Object editor = activateEditor(cell, rectangle);
         editor = setEditorToReplaceMode(editor, replace);
         
         getRobot().type(editor, text);
     }
     
     /**
      * @return true if the mouse pointer is over any cell, false otherwise.
      */
     private boolean isMouseOverCell() {
         try {
             getCellAtMousePosition();
         } catch (StepExecutionException se) {
             return false;
         }
         return true;
     }
     
 
     
     /**
      * Sets the specific editor to an replace mode. Means that the next key
      * input will override the complete text of the editor.
      * @param editor 
      * @param replace if <code>true</code> than the editor has to override
      *              the complete text with the next key input. Else the next
      *              key input will append to the end.
      * @return the editor if it changed
      */
     protected abstract Object setEditorToReplaceMode(Object editor,
             boolean replace);
    
     /**
      * Activates the editor of the specific cell.
      * @param cell 
      * @param rectangle 
      * @return the editor of the cell
      */
     protected abstract Object activateEditor(Cell cell, Rectangle rectangle);
     
 
     /**
      * Gets The modifier for an extended selection (more than one item)
      * @return the modifier
      */
     protected abstract int getExtendSelectionModifier();
     
     /**
      * @return the cell under the current mouse position.
      * @throws StepExecutionException If no cell is found.
      */
     protected abstract Cell getCellAtMousePosition() 
         throws StepExecutionException;
    
     /**
      * Verifies if mouse is on header.
      * @return true if mouse is on header
      */
     protected abstract boolean isMouseOnHeader();
 
 
 }
