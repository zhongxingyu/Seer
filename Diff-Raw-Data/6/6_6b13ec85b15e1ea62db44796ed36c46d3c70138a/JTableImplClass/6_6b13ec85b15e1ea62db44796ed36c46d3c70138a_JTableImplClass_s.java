 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.rc.swing.swing.implclasses;
 
 import java.awt.Component;
 import java.awt.IllegalComponentStateException;
 import java.awt.Point;
 import java.awt.Rectangle;
 
 import javax.swing.JComponent;
 import javax.swing.JPopupMenu;
 import javax.swing.JTable;
 import javax.swing.table.JTableHeader;
 import javax.swing.table.TableCellRenderer;
 import javax.swing.table.TableColumn;
 import javax.swing.table.TableColumnModel;
 
 import org.eclipse.jubula.rc.common.CompSystemConstants;
 import org.eclipse.jubula.rc.common.driver.ClickOptions;
 import org.eclipse.jubula.rc.common.driver.DragAndDropHelper;
 import org.eclipse.jubula.rc.common.driver.IRunnable;
 import org.eclipse.jubula.rc.common.exception.StepExecutionException;
 import org.eclipse.jubula.rc.common.implclasses.IndexConverter;
 import org.eclipse.jubula.rc.common.implclasses.MatchUtil;
 import org.eclipse.jubula.rc.common.implclasses.Verifier;
 import org.eclipse.jubula.rc.common.implclasses.table.Cell;
 import org.eclipse.jubula.rc.common.logger.AutServerLogger;
 import org.eclipse.jubula.rc.swing.swing.interfaces.IJTableImplClass;
 import org.eclipse.jubula.rc.swing.utils.SwingUtils;
 import org.eclipse.jubula.tools.objects.event.EventFactory;
 import org.eclipse.jubula.tools.objects.event.TestErrorEvent;
 
 
 /**
  * This class implements actions on the Swing JTable.
  *
  * @author BREDEX GmbH
  * @created 23.03.2005
  */
 public class JTableImplClass extends AbstractSwingImplClass 
     implements IJTableImplClass {
 
     /**
      * The logger.
      */
     private static AutServerLogger log =
         new AutServerLogger(JTableImplClass.class);
 
     /**
      * The JTable on which the action are performed.
      */
     private JTable m_table;
 
     /**
      * {@inheritDoc}
      */
     public void setComponent(Object graphicsComponent) {
         m_table = (JTable) graphicsComponent;
     }
 
     /**
      * {@inheritDoc}
      */
     public JComponent getComponent() {
         return m_table;
     }
 
     /**
      * Checks wether <code>0 <= value < count</code>.
      *
      * @param value
      *            The value to check.
      * @param count
      *            The upper bound.
      */
     private void checkBounds(int value, int count) {
         if ((value < 0) || (value >= count)) {
             throw new StepExecutionException("Invalid row/column: " + value, //$NON-NLS-1$
                 EventFactory.createActionError(
                         TestErrorEvent.INVALID_INDEX_OR_HEADER));
         }
     }
 
     /**
      * Checks if the passed row and column are inside the bounds of the JTable.
      *
      * @param row
      *            The row
      * @param column
      *            The column
      * @throws StepExecutionException
      *             If the row or the column is outside of the JTable's bounds.
      */
     private void checkRowColBounds(int row, int column)
         throws StepExecutionException {
         checkBounds(row, m_table.getRowCount());
         checkBounds(column, m_table.getColumnCount());
     }
 
 
     /**
      * @return the cell under the current mouse position.
      * @throws StepExecutionException If no cell is found.
      */
     protected Cell getCellAtMousePosition() throws StepExecutionException {
         Point mousePos = getRobot().getCurrentMousePosition();
         Point tablePos = m_table.getLocationOnScreen();
         Point relativePos = new Point(mousePos.x - tablePos.x,
             mousePos.y - tablePos.y);
         final int column = m_table.columnAtPoint(relativePos);
         final int row = m_table.rowAtPoint(relativePos);
         if (log.isDebugEnabled()) {
             log.debug("Selected row, col: " + row + ", " + column); //$NON-NLS-1$ //$NON-NLS-2$
         }
         checkRowColBounds(row, column);
         return new Cell(row, column);
     }
 
 
     /**
      * @return The currently selected cell of the JTable.
      *
      * @throws StepExecutionException
      *             If no cell is selected.
      */
     private Cell getSelectedCell() throws StepExecutionException {
         int row = m_table.getSelectedRow();
         int col = m_table.getSelectedColumn();
         if (log.isDebugEnabled()) {
             log.debug("Selected row, col: " + row + ", " + col); //$NON-NLS-1$ //$NON-NLS-2$
         }
         try {
             checkRowColBounds(row, col);
         } catch (StepExecutionException e) {
             if ((e.getEvent() != null)
                 && (TestErrorEvent.INVALID_INDEX.equals(e.getEvent()
                     .getProps().get(TestErrorEvent.Property
                         .DESCRIPTION_KEY)))) {
                 // set "invalid index" to "no selection" -> better description!
                 throw new StepExecutionException("No selection", //$NON-NLS-1$
                     EventFactory.createActionError(
                         TestErrorEvent.NO_SELECTION));
             }
             throw e;
         }
         return new Cell(row, col);
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
             startingIndex = getSelectedCell().getCol() + 1;
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
             startingIndex = getSelectedCell().getRow() + 1;
         }
         return startingIndex;
     }
 
     /**
      * In addition to {@link #getSelectedCell()}, the operation is performed in
      * the event queue thread.
      *
      * @return The currently selected cell of the JTable.
      *
      * @throws StepExecutionException
      *             If no cell is selected.
      */
     private Cell invokeGetSelectedCell() throws StepExecutionException {
         return (Cell) getEventThreadQueuer().invokeAndWait("getSelectedCell", //$NON-NLS-1$
                 new IRunnable() {
                     public Object run() {
                         return getSelectedCell();
                     }
                 });
     }
 
     /**
      * In addition to {@link #getCellAtMousePosition()}, the operation is
      * performed in the event queue thread.
      *
      * @return The cell of the JTable under the current mouse position
      *
      * @throws StepExecutionException If no cell is found.
      */
     private Cell invokeGetCellAtMousePosition() throws StepExecutionException {
         return (Cell) getEventThreadQueuer().invokeAndWait("getCellAtMousePosition()", //$NON-NLS-1$
             new IRunnable() {
                 public Object run() {
                     return getCellAtMousePosition();
                 }
             });
     }
 
     /**
      * Scrolls the passed cell (as row and column) to visible.
      *
      * @param row
      *            The row.
      * @param col
      *            The column.
      * @return The rectangle of the cell.
      * @throws StepExecutionException
      *             If getting the cell rectangle or the scrolling fails.
      */
     private Rectangle invokeScrollCellToVisible(final int row, final int col)
         throws StepExecutionException {
         Rectangle bounds = (Rectangle) getEventThreadQueuer().invokeAndWait(
                 "getCellRect", //$NON-NLS-1$
                 new IRunnable() {
                     public Object run() {
                         return m_table.getCellRect(row, col, true);
                     }
                 });
 
         getRobot().scrollToVisible(m_table, bounds);
         return bounds;
     }
 
     /**
      * Selects a table cell in the given row and column via click in the midlle
      * of the cell.
      * @param row the row to select
      * @param rowOperator the row header operator
      * @param col the column to select
      * @param colOperator the column header operator
      * @param co the click options to use
      * @param extendSelection Should this selection be part of a multiple selection
      */
     private void gdSelectCell(final String row, final String rowOperator,
             final String col, final String colOperator,
         final ClickOptions co, final String extendSelection) {
 
         gdSelectCell(row, rowOperator, col, colOperator, co.getClickCount(),
                 50, POS_UNI_PERCENT, 50, POS_UNI_PERCENT, extendSelection, co
                         .getMouseButton());
     }
     
     /**
      * Selects a table cell in the given row and column via click in the midlle
      * of the cell.
      * @param row the row to select
      * @param col the column to select
      * @param clickCount the number of mouse clicks
      * @param extendSelection Should this selection be part of a multiple selection
      * @deprecated Will be removed with gdSelectCell with String parameter
      * for Row/Column
      */
     private void gdSelectCell(final int row, final int col,
         final int clickCount, final String extendSelection) {
 
         gdSelectCell(row, col, clickCount, 50, POS_UNI_PERCENT, 50,
             POS_UNI_PERCENT, extendSelection);
     }
 
     /**
      * Selects the cell of the JTable.<br>
      * With the xPos, yPos, xunits and yUnits the click position inside the
      * cell can be defined.
      *
      * @param row the row to select
      * @param rowOperator the row header operator
      * @param col the column to select
      * @param colOperator the column header operator
      * @param clickCount The number of clicks with the right mouse button
      * @param xPos what x position
      * @param xUnits should x position be pixel or percent values
      * @param yPos what y position
      * @param yUnits should y position be pixel or percent values
      * @param extendSelection Should this selection be part of a multiple selection
      * @param button what mouse button should be used
      * @throws StepExecutionException
      *             If the row or the column is invalid
      */
     public void gdSelectCell(final String row, final String rowOperator,
         final String col, final String colOperator,
         final int clickCount, final int xPos, final String xUnits,
         final int yPos, final String yUnits, final String extendSelection, 
         int button)
         throws StepExecutionException {
         final int implRow = getRowFromString(row, rowOperator);
         final int implCol = getColumnFromString(col, colOperator);
         final boolean isExtendSelection = 
             extendSelection.equals(CompSystemConstants.EXTEND_SELECTION_YES);
         if (log.isDebugEnabled()) {
             log.debug("Selecting row, col: " + row + ", " + col); //$NON-NLS-1$//$NON-NLS-2$
         }
         Object source = m_table;
         Rectangle rectangle;
         //if row is header and col is existing
         if (implRow == -1 && implCol > -1) {
             rectangle = m_table.getTableHeader().getHeaderRect(implCol);
             source = m_table.getTableHeader();
         } else {
             rectangle = (Rectangle) getEventThreadQueuer().invokeAndWait(
                     "selectCell", //$NON-NLS-1$
                     new IRunnable() {
                         public Object run() {
                             checkRowColBounds(implRow, implCol);
                             return m_table.getCellRect(implRow, implCol, true);
                         }
                     });
         }
         
         ClickOptions clickOptions = ClickOptions.create();
         clickOptions.setClickCount(clickCount);
         clickOptions.setMouseButton(button);
 
         try {
             if (isExtendSelection) {
                 getRobot().keyPress(m_table,
                         SwingUtils.getSystemDefaultModifier());
             }
             getRobot().click(source, rectangle, clickOptions,
                     xPos, xUnits.equalsIgnoreCase(POS_UNIT_PIXEL),
                     yPos, yUnits.equalsIgnoreCase(POS_UNIT_PIXEL));
         } finally {
             if (isExtendSelection) {
                 getRobot().keyRelease(m_table,
                         SwingUtils.getSystemDefaultModifier());
             }
         }
     }
     
     /**
      * Selects the cell of the JTable.<br>
      * With the xPos, yPos, xunits and yUnits the click position inside the
      * cell can be defined.
      *
      * @param row The row of the cell.
      * @param col The column of the cell.
      * @param clickCount The number of clicks with the right mouse button
      * @param xPos what x position
      * @param xUnits should x position be pixel or percent values
      * @param yPos what y position
      * @param yUnits should y position be pixel or percent values
      * @param extendSelection Should this selection be part of a multiple selection
      * @throws StepExecutionException
      *             If the row or the column is invalid
      * @deprecated Will be removed with gdSelectCell with String parameter
      * for Row/Column
      */
     public void gdSelectCell(final int row, final int col,
         final int clickCount, final int xPos, final String xUnits,
         final int yPos, final String yUnits, final String extendSelection)
         throws StepExecutionException {
 
         final int implRow = IndexConverter.toImplementationIndex(row);
         final int implCol = IndexConverter.toImplementationIndex(col);
         final boolean isExtendSelection = 
             extendSelection.equals(CompSystemConstants.EXTEND_SELECTION_YES);
         if (log.isDebugEnabled()) {
             log.debug("Selecting row, col: " + row + ", " + col); //$NON-NLS-1$//$NON-NLS-2$
         }
         Rectangle rectangle = (Rectangle) getEventThreadQueuer().invokeAndWait(
                 "selectCell", //$NON-NLS-1$
                 new IRunnable() {
                     public Object run() {
                         checkRowColBounds(implRow, implCol);
                         return m_table.getCellRect(implRow, implCol, true);
                     }
                 });
         ClickOptions clickOptions = ClickOptions.create();
         clickOptions.setClickCount(clickCount);
         clickOptions.left();
 
         try {
             if (isExtendSelection) {
                 getRobot().keyPress(m_table,
                         SwingUtils.getSystemDefaultModifier());
             }
             getRobot().click(m_table, rectangle, clickOptions,
                     xPos, xUnits.equalsIgnoreCase(POS_UNIT_PIXEL),
                     yPos, yUnits.equalsIgnoreCase(POS_UNIT_PIXEL));
         } finally {
             if (isExtendSelection) {
                 getRobot().keyRelease(m_table,
                         SwingUtils.getSystemDefaultModifier());
             }
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
             currCell = invokeGetCellAtMousePosition();
         } catch (StepExecutionException e) {
             currCell = invokeGetSelectedCell();
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
         gdSelectCell(newRow, newCol, clickCount,
                      xPos, xUnits, yPos, yUnits, extendSelection);
     }
 
     /**
      * Selects the passed row of the JTable. This is actually a selection of the
      * cell <code>(row, 0)</code>.
      *
      * @param row
      *            The row
      * @param extendSelection Should this selection be part of a multiple selection
      * @throws StepExecutionException
      *             If the row is invalid
      * @deprecated the same as gdSelectCell
      * Will be removed!
      */
     public void gdSelectRow(int row, String extendSelection)
         throws StepExecutionException {
         gdSelectCell(row, IndexConverter.toUserIndex(0), 1, extendSelection);
     }
 
     /**
      * Verifies the rendered text inside the passed cell.
      *
      * @param row the row to select
      * @param rowOperator the row header operator
      * @param col the column to select
      * @param colOperator the column header operator
      * @param text
      *            The cell text to verify.
      * @param operator
      *            The operation used to verify
      * @throws StepExecutionException
      *             If the row or the column is invalid, or if the rendered text
      *             cannot be extracted.
      */
     public void gdVerifyText(String text, String operator, final String row,
             final String rowOperator, final String col,
             final String colOperator) throws StepExecutionException {
         final int implRow = getRowFromString(row, rowOperator);
         final int implCol = getColumnFromString(col, colOperator);
         String current;
         //if row is header and column is existing
         if (implRow == -1 && implCol > -1) {
             current = (String)getEventThreadQueuer().invokeAndWait("gdVerifyText", //$NON-NLS-1$
                     new IRunnable() {
                         public Object run() {
                             return m_table.getColumnName(implCol);
                         }
                     }); 
         } else {
             getEventThreadQueuer().invokeAndWait("checkRowColBounds", //$NON-NLS-1$
                     new IRunnable() {
                         public Object run() {
                             checkRowColBounds(implRow, implCol);
                             return null;
                         }
                     });
             invokeScrollCellToVisible(implRow, implCol);
             current = getCellText(implRow, implCol);
         }
         
         Verifier.match(current, text, operator);
     }
     
     /**
      * Verifies the rendered text inside the passed cell.
      *
      * @param row
      *            The row of the cell.
      * @param col
      *            The column of the cell.
      * @param text
      *            The cell text to verify.
      * @param operator
      *            The operation used to verify
      * @throws StepExecutionException
      *             If the row or the column is invalid, or if the rendered text
      *             cannot be extracted.
      * @deprecated Will be removed with gdVerifyText with String parameter
      * for Row/Column
      */
     public void gdVerifyText(String text, String operator, final int row,
             final int col) throws StepExecutionException {
 
         final int implRow = IndexConverter.toImplementationIndex(row);
         final int implCol = IndexConverter.toImplementationIndex(col);
         getEventThreadQueuer().invokeAndWait("checkRowColBounds", //$NON-NLS-1$
                 new IRunnable() {
                     public Object run() {
                         checkRowColBounds(implRow, implCol);
                         return null;
                     }
                 });
 
         invokeScrollCellToVisible(implRow, implCol);
         final String current = getCellText(implRow, implCol);
         Verifier.match(current, text, operator);
     }
 
     /**
      * @param row the zero based index of the row
      * @param col the zero based index of the column
      * @return the text of the cell of the given coordinates
      */
     protected String getCellText(final int row, final int col) {
         String current = (String)getEventThreadQueuer().invokeAndWait(
                 "getCellText", //$NON-NLS-1$
                 new IRunnable() {
                     public Object run() {
                         Object value = m_table.getValueAt(row, col);
                         boolean selected = m_table.isCellSelected(row,
                                 col);
                         if (log.isDebugEnabled()) {
                             log.debug("Getting cell text:"); //$NON-NLS-1$
                             log.debug("Row, col: " + row + ", " + col); //$NON-NLS-1$ //$NON-NLS-2$
                             log.debug("Value: " + value); //$NON-NLS-1$
                         }
                         TableCellRenderer renderer = m_table.getCellRenderer(
                                 row, col);
                         Component c = renderer.getTableCellRendererComponent(
                                 m_table, value, selected, true, row,
                                 col);
                         return getRenderedText(c, false);
                     }
                 });
         return current;
     }
 
     /**
      * Verifies the rendered text inside the currently selected cell.
      *
      * @param text
      *            The cell text to verify.
      * @param operator
      *            The operation used to verify
      * @throws StepExecutionException
      *             If there is no selected cell, or if the rendered text cannot
      *             be extracted.
      */
     public void gdVerifyText(String text, String operator)
         throws StepExecutionException {
 
         Cell cell = invokeGetSelectedCell();
         gdVerifyText(text, operator, IndexConverter.toUserIndex(cell.getRow()),
                 IndexConverter.toUserIndex(cell.getCol()));
     }
 
 
     /**
      * Verifies the rendered text inside cell at the mouse position on screen.
      *
      * @param text
      *            The cell text to verify.
      * @param operator
      *            The operation used to verify
      * @throws StepExecutionException
      *             If there is no selected cell, or if the rendered text cannot
      *             be extracted.
      */
     public void gdVerifyTextAtMousePosition(String text, String operator)
         throws StepExecutionException {        
         if (isMouseOnHeader()) {
             throw new StepExecutionException("Unsupported Header Action", //$NON-NLS-1$
                     EventFactory.createActionError(
                             TestErrorEvent.UNSUPPORTED_HEADER_ACTION));
         }        
         Cell cell = invokeGetCellAtMousePosition();
         gdVerifyText(text, operator, IndexConverter.toUserIndex(cell.getRow()),
                 IndexConverter.toUserIndex(cell.getCol()));
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
      * Verifies, if value exists in row.
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
             boolean exists) throws StepExecutionException {
         final int implRow = getRowFromString(row, rowOperator);
         Boolean valueExists = Boolean.TRUE;
         //if row is header
         if (implRow == -1) {
             valueExists = (Boolean)getEventThreadQueuer().invokeAndWait(
                     "selectCellByColValue", new IRunnable() { //$NON-NLS-1$
                         public Object run() throws StepExecutionException {
                             for (int k = getStartingColIndex(searchType);
                                     k < m_table.getColumnCount();
                                     ++k) {
                                 String header = m_table.getColumnName(k);
                                 if (MatchUtil.getInstance().match(
                                         header, value, operator)) {
                                     return Boolean.TRUE;
                                 }
                             }
                             return Boolean.FALSE;
                         }
                     });
         } else {
             valueExists = (Boolean)getEventThreadQueuer().invokeAndWait(
                     "selectCellByColValue", new IRunnable() { //$NON-NLS-1$
                         public Object run() throws StepExecutionException {
                             for (int i = getStartingColIndex(searchType);
                                     i < m_table.getColumnCount();
                                     ++i) {
                                 if (MatchUtil.getInstance().match(
                                         getCellText(implRow, i), value,
                                         operator)) {
                                     return Boolean.TRUE;
                                 }
                             }
                             return Boolean.FALSE;
                         }
                     });
         }
         Verifier.equals(exists, valueExists.booleanValue());
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
         final int implCol = getColumnFromString(col, colOperator);
         Boolean valueExists = (Boolean)getEventThreadQueuer().invokeAndWait(
             "verifyValueInColumn", //$NON-NLS-1$
             new IRunnable() {
                 public Object run() throws StepExecutionException {
                     final int rowCount = m_table.getRowCount();
                     for (int i = getStartingRowIndex(searchType);
                             i < rowCount; ++i) {
                         if (MatchUtil.getInstance().match(getCellText(i,
                             implCol), value, operator)) {
                             return Boolean.TRUE;
                         }
                     }
                     String header = m_table.getColumnName(implCol);
                     if (MatchUtil.getInstance().match(header, value,
                             operator)) {
                         return Boolean.TRUE;
                     }
                     return Boolean.FALSE;
                 }
             });
         Verifier.equals(exists, valueExists.booleanValue());
     }
 
     /**
      * @return <code>true</code> if the currently selected cell is editable.
      *
      * @throws StepExecutionException
      *             If there is no selected cell.
      */
     private boolean isCellEditable() throws StepExecutionException {
         Boolean editable = (Boolean) getEventThreadQueuer().invokeAndWait(
                 "isCellEditable", //$NON-NLS-1$
                 new IRunnable() {
                     public Object run() {
                         Cell cell = getSelectedCell();
                         // see findBugs
                         return (m_table.isCellEditable(cell.getRow(),
                                 cell.getCol())) ? Boolean.TRUE : Boolean.FALSE;
                     }
                 });
         return editable.booleanValue();
     }
     
     /**
      * @return <code>true</code> if the cell at mouse position is editable.
      *
      * @throws StepExecutionException
      *             If there is no cell at mouse position.
      */
     private boolean isCellEditableMousePosition()
         throws StepExecutionException {
         Boolean editable = (Boolean) getEventThreadQueuer().invokeAndWait(
                 "isCellEditable", //$NON-NLS-1$
                 new IRunnable() {
                     public Object run() {
                         Cell cell = getCellAtMousePosition();
                         // see findBugs
                         return (m_table.isCellEditable(cell.getRow(),
                                 cell.getCol())) ? Boolean.TRUE : Boolean.FALSE;
                     }
                 });
         return editable.booleanValue();
     }
 
     /**
      * Verifies the editable property of the current selected cell.
      *
      * @param editable
      *            The editable property to verify.
      */
     public void gdVerifyEditable(boolean editable) {
         Verifier.equals(editable, isCellEditable());
     }
     
     /**
      * Verifies the editable property of the cell at mouse position.
      *
      * @param editable
      *            The editable property to verify.
      */
     private void verifyEditableMousePosition(boolean editable) {
         Verifier.equals(editable, isCellEditableMousePosition());
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
         if (getRowFromString(row, rowOperator) == -1) {
             throw new StepExecutionException("Unsupported Header Action", //$NON-NLS-1$
                     EventFactory.createActionError(
                             TestErrorEvent.UNSUPPORTED_HEADER_ACTION));
         }        
         gdSelectCell(row, rowOperator, col, colOperator, ClickOptions.create(),
                 CompSystemConstants.EXTEND_SELECTION_NO);
         gdVerifyEditable(editable);
     }
     
     /**
      * Verifies the editable property of the given indices.
      *
      * @param editable
      *            The editable property to verify.
      * @param row
      *            The table row
      * @param col
      *            The table column
      * @deprecated Will be removed with gdVerifyEditable with String parameter
      * for Row/Column
      */
     public void gdVerifyEditable(boolean editable, int row, int col) {
         gdSelectCell(row, col, 1, CompSystemConstants.EXTEND_SELECTION_NO);
         gdVerifyEditable(editable);
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
         
         verifyEditableMousePosition(editable);
     }
 
     /**
      * Gets the TableCellEditor of the given cell
      * @param cell the cell.
      * @return the TableCellEditor
      */
     private Component getTableCellEditor(final Cell cell) {
         return (Component) getEventThreadQueuer()
             .invokeAndWait("getCellEditor", //$NON-NLS-1$
                 new IRunnable() {
                     public Object run() {
                         Object value = m_table.getValueAt(
                             cell.getRow(), cell.getCol());
                         boolean selected = m_table.isCellSelected(
                             cell.getRow(), cell.getCol());
                         return m_table.getCellEditor(cell.getRow(),
                             cell.getCol()).getTableCellEditorComponent(m_table,
                                 value, selected, cell.getRow(), cell.getCol());
                     }
                 });
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
      * @param row the row to select
      * @param rowOperator the row header operator
      * @param col the column to select
      * @param colOperator the column header operator
      */
     public void gdReplaceText(String text, String row, String rowOperator,
             String col, String colOperator) {
         //if row is header row
         if (getRowFromString(row, rowOperator) == -1) {
             throw new StepExecutionException("Unsupported Header Action", //$NON-NLS-1$
                     EventFactory.createActionError(
                             TestErrorEvent.UNSUPPORTED_HEADER_ACTION));
         }
         gdSelectCell(row, rowOperator, col, colOperator, ClickOptions.create(),
                 CompSystemConstants.EXTEND_SELECTION_NO);        
         inputText(text, true);
     }
     
     /**
      * Replaces the given text in the given cell coordinates
      * @param text the text to replace
      * @param row the row
      * @param col the column
      * @deprecated Will be removed with gdReplaceText with String parameter
      * for Row/Column
      */
     public void gdReplaceText(String text, int row, int col) {
         gdSelectCell(row, col, 1, CompSystemConstants.EXTEND_SELECTION_NO);
         inputText(text, true);
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
      * inputs/replaces the given text
      * @param text the text to input
      * @param replace wheter to replace or not
      * @throws StepExecutionException If there is no selected cell,
      * or if the cell is not editable, or if the table cell editor permits
      * the text to be written.
      */
     private void inputText(final String text, boolean replace)
         throws StepExecutionException {
 
         final Cell cell = invokeGetSelectedCell();
         if (!isCellEditable()) {
             throw new StepExecutionException("Selected table cell " //$NON-NLS-1$
                 + cell + " is not editable", //$NON-NLS-1$
                 EventFactory.createActionError(TestErrorEvent.NOT_EDITABLE));
         }
         // Ensure that the cell is visible.
         Rectangle rectangle = invokeScrollCellToVisible(cell.getRow(), cell
                 .getCol());
         // click the cell once to activate it
         getRobot().click(m_table, rectangle);
         Component editor = getTableCellEditor(cell);
         // sometimes the editor only appears after doubleclick!
         if (editor == null) {
             ClickOptions co = ClickOptions.create().setClickCount(2);
             getRobot().click(m_table, rectangle, co);
             editor = getTableCellEditor(cell);
         }
         // Perform 3 clicks with the editor as target, not the table.
         // Don't scroll to visible here, it's already done.
         if (replace) {
             getRobot().clickAtCurrentPosition(editor, 3, 1);
         } else {
             editor = m_table;
         }
         // Type the text in table NOT in editor (because editor didn't work in JUnitTest)
         getRobot().type(editor, text);
         // FIXME Andreas: do not leave with implizit ENTER. Use UTF-8 (later)
         // Leave the editor by typing "Enter".
 //        getRobot().keyType(editor, KeyEvent.VK_ENTER);
 //
 //        // Check if the editor is still editing. This happens if
 //        // the input text is invalid. Cancel the editing and
 //        // throw an exception.
 //        getEventThreadQueuer().invokeAndWait("isEditing", //$NON-NLS-1$
 //                new IRunnable() {
 //                    public Object run() throws StepExecutionException {
 //                        if (m_table.isEditing()) {
 //                            m_table.getCellEditor(cell.getRow(), cell.getCol())
 //                                    .cancelCellEditing();
 //                            throw new StepExecutionException(
 //                                "Text input into cell " + cell //$NON-NLS-1$
 //                                    + " failed. Is the text '" + text + "' invalid?", //$NON-NLS-1$ //$NON-NLS-2$
 //                                EventFactory.createActionError(
 //                                    TestErrorEvent.INPUT_FAILED));
 //                        }
 //                        return null;
 //                    }
 //                });
 //
 //        // If the underlying table model fires a data change event,
 //        // the table loses it's selection. So, reselect the cell if necessary.
 //        Boolean select = (Boolean) getEventThreadQueuer().invokeAndWait(
 //                "adjustSelection", //$NON-NLS-1$
 //                new IRunnable() {
 //                    public Object run() {
 //                        try {
 //                            Cell cell2 = getSelectedCell();
 //                            return cell.equals(cell2) ? Boolean.FALSE
 //                                    : Boolean.TRUE;
 //                        } catch (StepExecutionException e) {
 //                            return Boolean.TRUE;
 //                        }
 //                    }
 //                });
 //        if (Boolean.TRUE.equals(select)) {
 //            gdSelectCell(IndexConverter.toUserIndex(cell.getRow()),
 //                    IndexConverter.toUserIndex(cell.getCol()), 1);
 //        }
     }
 
 
     /**
      * Types the text in the specified cell.
      *
      * @param text The text
      * @param row the row to select
      * @param rowOperator the row header operator
      * @param col the column to select
      * @param colOperator the column header operator
      * @throws StepExecutionException
      *             If the text input fails
      */
     public void gdInputText(String text, String row, String rowOperator,
             String col, String colOperator)
         throws StepExecutionException {
         //if row is header row
         if (getRowFromString(row, rowOperator) == -1) {
             throw new StepExecutionException("Unsupported Header Action", //$NON-NLS-1$
                     EventFactory.createActionError(
                             TestErrorEvent.UNSUPPORTED_HEADER_ACTION));
         }
         gdSelectCell(row, rowOperator, col, colOperator, ClickOptions.create(),
                 CompSystemConstants.EXTEND_SELECTION_NO);
         gdInputText(text);
     }
     
     /**
      * Types the text in the specified cell.
      *
      * @param text
      *            The text
      * @param row
      *            The table row
      * @param col
      *            The table column
      * @throws StepExecutionException
      *             If the text input fails
      * @deprecated Will be removed with gdInputText with String parameter
      * for Row/Column
      */
     public void gdInputText(String text, int row, int col)
         throws StepExecutionException {
         gdSelectCell(row, col, 1, CompSystemConstants.EXTEND_SELECTION_NO);
         gdInputText(text);
     }
 
     /**
      * Tries to click in the cell under the mouse position. If the mouse is not
      * over a cell, the current selected cell will be clicked on. If there is no
      * selected cell, the middle of the table is used to click on.
      *
      * @param count Number of clicks
      * @param button The mouse button
      */
     public void gdClick(int count, int button) {
         Cell currCell = null;
         if (isMouseOverCell()) {
             currCell = invokeGetCellAtMousePosition();
         } else if (isCellSelection()) {
             currCell = invokeGetSelectedCell();
         }
         if (currCell != null) {
             final Cell cell = currCell;
             final Rectangle rectangle = (Rectangle)getEventThreadQueuer()
                 .invokeAndWait("click", new IRunnable() { //$NON-NLS-1$
                         public Object run() {
                             checkRowColBounds(cell.getRow(), cell.getCol());
                             return m_table.getCellRect(cell.getRow(),
                                 cell.getCol(), true);
                         }
                     });
             getRobot().click(m_table, rectangle, ClickOptions.create()
                 .setClickCount(count).setMouseButton(button));
         } else {
             super.gdClick(count, button);
         }
     }
 
     /**
      * @return true, if there is any cell selection in the table, false otherwise.
      */
     private boolean isCellSelection() {
         try {
             invokeGetSelectedCell();
         } catch (StepExecutionException se) {
             return false;
         }
         return true;
     }
 
     /**
      * @return true if the mouse pointer is over any cell, false otherwise.
      */
     private boolean isMouseOverCell() {
         try {
             invokeGetCellAtMousePosition();
         } catch (StepExecutionException se) {
             return false;
         }
         return true;
     }
     
     /**
      * Verifies if mouse is on header.
      * @return true if mouse is on header
      */
     private boolean isMouseOnHeader() {        
         if (m_table.getTableHeader() == null
                 || !(m_table.getTableHeader().isShowing())) {
             return false;
         }
         
         JTableHeader header = m_table.getTableHeader();
         Point mousePos = getRobot().getCurrentMousePosition();        
         try {
             Point headerPos = header.getLocationOnScreen();
             Point relativePos = new Point(mousePos.x - headerPos.x,
                 mousePos.y - headerPos.y);            
             return header.getBounds().contains(relativePos);
         } catch (IllegalComponentStateException icse) {
             return false;
         }       
     }
 
     /**
      * Finds the first row which contains the value <code>value</code>
      * in column <code>col</code> and selects this row.
      *
      * @param col the column to select
      * @param colOperator the column header operator
      * @param value the value
      * @param regexOp the regex operator
      * @param clickCount the number of clicks
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
      * @param value the value
      * @param clickCount the number of clicks
      * @param regexOp the regex operator
      * @param extendSelection Should this selection be part of a multiple selection
      * @param searchType Determines where the search begins ("relative" or "absolute")
      * @deprecated Will be removed with gdSelectRowByValue with String parameter
      * for Row/Column
      */
     public void gdSelectRowByValue(int col, final String value,
         int clickCount, final String regexOp, final String extendSelection,
         final String searchType) {
 
         gdSelectRowByValue(col, value, regexOp, extendSelection, searchType,
                 clickCount);
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
         
         final int implCol = getColumnFromString(col, colOperator);
         Integer implRow;
         implRow = (Integer)getEventThreadQueuer().invokeAndWait(
             "selectRowByValue", //$NON-NLS-1$
             new IRunnable() {
                 public Object run() throws StepExecutionException {
                     final int rowCount = m_table.getRowCount();
                     for (int i = getStartingRowIndex(searchType);
                             i < rowCount; ++i) {
                         if (MatchUtil.getInstance().match(getCellText(i,
                             implCol), value, regexOp)) {
 
                             return new Integer(i);
                         }
                     }
                     String header = m_table.getColumnName(implCol);
                     if (MatchUtil.getInstance().match(header, value, regexOp)) {
                         return new Integer(-1);
                     }
                     return null;
                 }
             });
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
      * Finds the first row which contains the value <code>value</code>
      * in column <code>col</code> and selects this row.
      *
      * @param col the column
      * @param value the value
      * @param regexOp the regex operator
      * @param extendSelection Should this selection be part of a multiple selection
      * @param searchType Determines where the search begins ("relative" or "absolute")
      * @param clickCount the clickCount
      * @deprecated Will be removed with gdSelectRowByValue with String parameter
      * for Row/Column
      */
     protected void gdSelectRowByValue(int col, final String value,
         final String regexOp, final String extendSelection,
         final String searchType, int clickCount) {
 
         final int implCol = IndexConverter.toImplementationIndex(col);
         Integer implRow;
         implRow = (Integer)getEventThreadQueuer().invokeAndWait(
             "selectRowByValue", //$NON-NLS-1$
             new IRunnable() {
                 public Object run() throws StepExecutionException {
                     final int rowCount = m_table.getRowCount();
                     for (int i = getStartingRowIndex(searchType);
                             i < rowCount; ++i) {
                         if (MatchUtil.getInstance().match(getCellText(i,
                             implCol), value, regexOp)) {
 
                             return new Integer(i);
                         }
                     }
                     return null;
                 }
             });
         if (implRow == null) {
             throw new StepExecutionException("no such row found", //$NON-NLS-1$
                     EventFactory.createActionError(TestErrorEvent.NOT_FOUND));
         }
         gdSelectCell(IndexConverter.toUserIndex(implRow.intValue()),
                 IndexConverter.toUserIndex(implCol), clickCount,
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
      * @param value the value
      * @param clickCount the number of clicks
      * @param regex search using regex
      * @param extendSelection Should this selection be part of a multiple selection
      * @param searchType Determines where the search begins ("relative" or "absolute")
      * @deprecated Will be removed with gdSelectCellByColValue with String parameter
      * for Row/Column
      */
     public void gdSelectCellByColValue(int row, final String value,
         final String regex, int clickCount, final String extendSelection,
         final String searchType) {
 
         gdSelectCellByColValue(row, value, regex, extendSelection, searchType,
                 clickCount);
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
         final int implRow = getRowFromString(row, rowOperator);
         Integer implCol;
         if (implRow == -1) {
             implCol = (Integer)getEventThreadQueuer().invokeAndWait(
                     "selectCellByColValue", new IRunnable() { //$NON-NLS-1$
                         public Object run() throws StepExecutionException {
                             for (int k = getStartingColIndex(searchType);
                                     k < m_table.getColumnCount();
                                     ++k) {
                                 String header = m_table.getColumnName(k);
                                 if (MatchUtil.getInstance().match(
                                         header, value, regex)) {
                                     return new Integer(k);
                                 }
                             }
                             return null;
                         }
                     });
         } else {
             implCol = (Integer)getEventThreadQueuer().invokeAndWait(
                     "selectCellByColValue", new IRunnable() { //$NON-NLS-1$
                         public Object run() throws StepExecutionException {
                             for (int i = getStartingColIndex(searchType);
                                     i < m_table.getColumnCount();
                                     ++i) {
                                 if (MatchUtil.getInstance().match(
                                         getCellText(implRow, i), 
                                         value, regex)) {
                                     return new Integer(i);
                                 }
                             }
                             return null;
                         }
                     });
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
     
     /**
      * Finds the first column which contains the value <code>value</code>
      * in the given row and selects the cell.
      *
      * @param row the row
      * @param value the value
      * @param regex search using regex
      * @param extendSelection Should this selection be part of a multiple selection
      * @param searchType Determines where the search begins ("relative" or "absolute")
      * @param clickCount the number of clicks
      * @deprecated Will be removed with gdSelectCellByColValue with String parameter
      * for Row/Column
      */
     protected void gdSelectCellByColValue(int row, final String value,
         final String regex, final String extendSelection,
         final String searchType, int clickCount) {
 
         final int implRow = IndexConverter.toImplementationIndex(row);
         Integer implCol;
         implCol = (Integer)getEventThreadQueuer().invokeAndWait(
             "selectCellByColValue", new IRunnable() { //$NON-NLS-1$
                 public Object run() throws StepExecutionException {
                     for (int i = getStartingColIndex(searchType);
                             i < m_table.getColumnCount();
                             ++i) {
                         if (MatchUtil.getInstance().match(
                                     getCellText(implRow, i), value, regex)) {
                             return new Integer(i);
                         }
                     }
                     return null;
                 }
             });
 
         if (implCol == null) {
             throw new StepExecutionException("no such cell found", EventFactory //$NON-NLS-1$
                     .createActionError(TestErrorEvent.NOT_FOUND));
         }
         gdSelectCell(row, IndexConverter.toUserIndex(implCol.intValue()),
                 clickCount, extendSelection);
     }
 
     /**
      * Finds the first row which contains the value <code>value</code>
      * in the given column and selects the cell.
      *
      * @param col the column
      * @param value the value
      * @param regex search using regex
      * @deprecated the same as gdSelectRowByValue
      * Will be removed!
      */
     public void gdSelectCellByRowValue(int col, final String value,
         boolean regex) {
         final int implCol = IndexConverter.toImplementationIndex(col);
         Integer implRow;
         if (regex) {
             implRow = (Integer)getEventThreadQueuer().invokeAndWait(
                 "selectCellByRowValue", new IRunnable() { //$NON-NLS-1$
                     public Object run() throws StepExecutionException {
                         for (int i = 0; i < m_table.getRowCount(); ++i) {
                             if (MatchUtil.getInstance().match(
                                     getCellText(i, implCol), value,
                                     MatchUtil.MATCHES_REGEXP)) {
                                 return new Integer(i);
                             }
                         }
                         return null;
                     }
 
                 });
         } else {
             implRow = (Integer)getEventThreadQueuer().invokeAndWait(
                 "selectCellByRowValue", new IRunnable() { //$NON-NLS-1$
                     public Object run() throws StepExecutionException {
                         for (int i = 0; i < m_table.getRowCount(); ++i) {
                             if (MatchUtil.getInstance().match(
                                     getCellText(i, implCol), value,
                                     MatchUtil.MATCHES_REGEXP)) {
                                 return new Integer(i);
                             }
                         }
                         return null;
                     }
 
                 });
         }
         if (implRow == null) {
             throw new StepExecutionException("no such cell found", EventFactory //$NON-NLS-1$
                     .createActionError(TestErrorEvent.NOT_FOUND));
         }
         gdSelectCell(IndexConverter.toUserIndex(implRow.intValue()),
                      col, 1, CompSystemConstants.EXTEND_SELECTION_NO);
     }
     /**
      * Selects a popup menu item at the specified cell
      *
      * @param row row
      * @param col column
      * @param indexPath path of the menu items
      * @deprecated <b>Will be made private. For internal use only!</b>
      */
     public void gdPopupByIndexPathAtCell(int row, int col, String indexPath) {
         final int implRow = IndexConverter.toImplementationIndex(row);
         final int implCol = IndexConverter.toImplementationIndex(col);
 
         if (log.isDebugEnabled()) {
             log.debug("Selecting row, col: " + row + ", " + col); //$NON-NLS-1$//$NON-NLS-2$
         }
         Rectangle rectangle = (Rectangle) getEventThreadQueuer().invokeAndWait(
                 "selectCell", //$NON-NLS-1$
                 new IRunnable() {
                     public Object run() {
                         checkRowColBounds(implRow, implCol);
                         return m_table.getCellRect(implRow, implCol, true);
                     }
                 });
         gdPopupSelectByIndexPath((int)rectangle.getCenterX(),
                 (int)rectangle.getCenterY(), POS_UNIT_PIXEL, indexPath);
     }
 
     /**
      * Selects a popup menu item at the specified cell
      *
      * @param row row
      * @param col column
      * @param textPath path of the menu items
      * @deprecated <b>Will be made private. For internal use only!</b>
      */
     public void gdPopupByTextPathAtCell(int row, int col, String textPath) {
         final int implRow = IndexConverter.toImplementationIndex(row);
         final int implCol = IndexConverter.toImplementationIndex(col);
 
         if (log.isDebugEnabled()) {
             log.debug("Selecting row, col: " + row + ", " + col); //$NON-NLS-1$//$NON-NLS-2$
         }
         Rectangle rectangle = (Rectangle) getEventThreadQueuer().invokeAndWait(
                 "selectCell", //$NON-NLS-1$
                 new IRunnable() {
                     public Object run() {
                         checkRowColBounds(implRow, implCol);
                         return m_table.getCellRect(implRow, implCol, true);
                     }
                 });
         gdPopupSelectByTextPath((int)rectangle.getCenterX(),
                 (int)rectangle.getCenterY(), POS_UNIT_PIXEL, textPath,
                 MatchUtil.EQUALS);
     }
 
     /**
      * Select a popup menu item at the selected cell
      * @param indexPath path of the menu items
      * @deprecated will be removed!
      */
     public void gdPopupByIndexPathAtSelectedCell(String indexPath) {
         Cell cell = getSelectedCell();
         int row = IndexConverter.toUserIndex(cell.getRow());
         int col = IndexConverter.toUserIndex(cell.getCol());
         gdPopupByIndexPathAtCell(row, col, indexPath);
     }
 
     /**
      * Select a popup menu item at the selected cell
      * @param textPath path of the menu items
      * @deprecated will be removed!
      */
     public void gdPopupByTextPathAtSelectedCell(String textPath) {
         Cell cell = getSelectedCell();
         int row = IndexConverter.toUserIndex(cell.getRow());
         int col = IndexConverter.toUserIndex(cell.getCol());
         gdPopupByTextPathAtCell(row, col, textPath);
     }
 
     /**
      * Action to read the value of the current selected cell of the JTable
      * to store it in a variable in the Client
      * @param variable the name of the variable
      * @return the text value.
      */
     public String gdReadValue(String variable) {
         return getText();
     }
 
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
         final int implRow = getRowFromString(row, rowOperator);
         final int implCol = getColumnFromString(col, colOperator);
         
         //if row is header and column is existing
         if (implRow == -1 && implCol > -1) {
             getEventThreadQueuer().invokeAndWait("gdReadValue", //$NON-NLS-1$
                     new IRunnable() {
                         public Object run() {
                             return m_table.getColumnName(implCol);
                         }
                     }); 
         }
         getEventThreadQueuer().invokeAndWait("checkRowColBounds", //$NON-NLS-1$
                 new IRunnable() {
                     public Object run() {
                         checkRowColBounds(implRow, implCol);
                         return null;
                     }
                 });
 
         invokeScrollCellToVisible(implRow, implCol);
         return getCellText(implRow, implCol);
     }
     
     /**
      * Action to read the value of the passed cell of the JTable
      * to store it in a variable in the Client
      * @param variable the name of the variable
      * @param row The row of the cell
      * @param col The column of the cell
      * @return the text value.
      * @deprecated Will be removed with gdReadValue with String parameter
      * for Row/Column
      */
     public String gdReadValue(String variable, int row, int col) {
         final int implRow = IndexConverter.toImplementationIndex(row);
         final int implCol = IndexConverter.toImplementationIndex(col);
         getEventThreadQueuer().invokeAndWait("checkRowColBounds", //$NON-NLS-1$
                 new IRunnable() {
                     public Object run() {
                         checkRowColBounds(implRow, implCol);
                         return null;
                     }
                 });
 
         invokeScrollCellToVisible(implRow, implCol);
         return getCellText(implRow, implCol);
 
     }
     
 
 
     /**
      * Drags the cell of the JTable.<br>
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
         gdSelectCell(row, rowOperator, col, colOperator, 0, xPos, xUnits, yPos,
                 yUnits, CompSystemConstants.EXTEND_SELECTION_NO, 1);
         pressOrReleaseModifiers(modifier, true);
         getRobot().mousePress(null, null, mouseButton);
     }
     
     /**
      * Drags the cell of the JTable.<br>
      * With the xPos, yPos, xunits and yUnits the click position inside the
      * cell can be defined.
      *
      * @param mouseButton the mouseButton.
      * @param modifier the modifier.
      * @param row The row of the cell.
      * @param col The column of the cell.
      * @param xPos what x position
      * @param xUnits should x position be pixel or percent values
      * @param yPos what y position
      * @param yUnits should y position be pixel or percent values
      * @throws StepExecutionException
      *             If the row or the column is invalid
      * @deprecated Will be removed with gdDragCell with String parameter
      * for Row/Column
      */
     public void gdDragCell(final int mouseButton, final String modifier,
             final int row, final int col, final int xPos,
             final String xUnits, final int yPos, final String yUnits)
         throws StepExecutionException {
 
         final DragAndDropHelper dndHelper = DragAndDropHelper.getInstance();
         dndHelper.setModifier(modifier);
         dndHelper.setMouseButton(mouseButton);
         gdSelectCell(row, col, 0, xPos, xUnits, yPos, yUnits, 
                 CompSystemConstants.EXTEND_SELECTION_NO);
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
      * Drops on the cell of the JTable.<br>
      * With the xPos, yPos, xunits and yUnits the click position inside the
      * cell can be defined.
      *
      * @param row The row of the cell.
      * @param col The column of the cell.
      * @param xPos what x position
      * @param xUnits should x position be pixel or percent values
      * @param yPos what y position
      * @param yUnits should y position be pixel or percent values
      * @param delayBeforeDrop the amount of time (in milliseconds) to wait
      *                        between moving the mouse to the drop point and
      *                        releasing the mouse button
      * @throws StepExecutionException
      *             If the row or the column is invalid
      * @deprecated Will be removed with gdDropCell with String parameter
      * for Row/Column
      */
     public void gdDropCell(final int row, final int col, final int xPos,
             final String xUnits, final int yPos, final String yUnits,
             int delayBeforeDrop) throws StepExecutionException {
 
         final DragAndDropHelper dndHelper = DragAndDropHelper.getInstance();
         try {
             gdSelectCell(row, col, 0, xPos, xUnits, yPos, yUnits,
                     CompSystemConstants.EXTEND_SELECTION_NO);
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
      * in column <code>col</code> and drags this row.
      *
      * @param mouseButton the mouse button
      * @param modifier the modifier
      * @param col the column
      * @param value the value
      * @param regexOp the regex operator
      * @param searchType Determines where the search begins ("relative" or "absolute")
      * @deprecated Will be removed with gdDragRowByValue with String parameter
      * for Row/Column
      */
     public void gdDragRowByValue(int mouseButton, String modifier, int col,
             final String value, final String regexOp, final String searchType) {
 
         final DragAndDropHelper dndHelper = DragAndDropHelper.getInstance();
         dndHelper.setModifier(modifier);
         dndHelper.setMouseButton(mouseButton);
         gdSelectRowByValue(col, value, 1, regexOp, 
                 CompSystemConstants.EXTEND_SELECTION_NO, 
                 searchType);
         pressOrReleaseModifiers(modifier, true);
         getRobot().mousePress(null, null, mouseButton);
     }
     
     /**
      * Finds the first row which contains the value <code>value</code>
      * in column <code>col</code> and drops on this row.
      *
      * @param col the column
      * @param value the value
      * @param regexOp the regex operator
      * @param searchType Determines where the search begins ("relative" or "absolute")
      * @param delayBeforeDrop the amount of time (in milliseconds) to wait
      *                        between moving the mouse to the drop point and
      *                        releasing the mouse button
      * @deprecated Will be removed with gdDropRowByValue with String parameter
      * for Row/Column
      */
     public void gdDropRowByValue(int col, final String value,
             final String regexOp, final String searchType,
             int delayBeforeDrop) {
 
         final DragAndDropHelper dndHelper = DragAndDropHelper.getInstance();
         try {
             gdSelectRowByValue(col, value, regexOp, 
                     CompSystemConstants.EXTEND_SELECTION_NO, 
                     searchType, 0);
             waitBeforeDrop(delayBeforeDrop);
         } finally {
             getRobot().mouseRelease(null, null, dndHelper.getMouseButton());
             pressOrReleaseModifiers(dndHelper.getModifier(), false);
         }
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
      * in the given row and drags the cell.
      *
      * @param mouseButton the mouse button
      * @param modifier the modifiers
      * @param row the row
      * @param value the value
      * @param regex search using regex
      * @param searchType Determines where the search begins ("relative" or "absolute")
      * @deprecated Will be removed with gdDragCellByColValue with String parameter
      * for Row/Column
      */
     public void gdDragCellByColValue(int mouseButton, String modifier, int row,
             final String value, final String regex, final String searchType) {
 
         final DragAndDropHelper dndHelper = DragAndDropHelper.getInstance();
         dndHelper.setModifier(modifier);
         dndHelper.setMouseButton(mouseButton);
 
         gdSelectCellByColValue(row, value, regex, 
                 CompSystemConstants.EXTEND_SELECTION_NO, 
                 searchType, 0);
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
      * Finds the first column which contains the value <code>value</code>
      * in the given row and drops on the cell.
      *
      * @param row the row
      * @param value the value
      * @param regex search using regex
      * @param searchType Determines where the search begins ("relative" or "absolute")
      * @param delayBeforeDrop the amount of time (in milliseconds) to wait
      *                        between moving the mouse to the drop point and
      *                        releasing the mouse button
      * @deprecated Will be removed with gdDropCellByColValue with String parameter
      * for Row/Column
      */
     public void gdDropCellByColValue(int row, final String value,
             final String regex, final String searchType,
             int delayBeforeDrop) {
 
         final DragAndDropHelper dndHelper = DragAndDropHelper.getInstance();
         try {
             gdSelectCellByColValue(row, value, regex, 
                     CompSystemConstants.EXTEND_SELECTION_NO, searchType, 0);
             waitBeforeDrop(delayBeforeDrop);
         } finally {
             getRobot().mouseRelease(null, null, dndHelper.getMouseButton());
             pressOrReleaseModifiers(dndHelper.getModifier(), false);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public String[] getTextArrayFromComponent() {
         final String[] componentTextArray;
         TableColumnModel columnModel = m_table.getColumnModel();
         if (columnModel == null) {
             componentTextArray = null;
         } else {
             componentTextArray = new String[columnModel.getColumnCount()];
             for (int i = 0; i < componentTextArray.length; i++) {
                 TableColumn tableColumn = columnModel.getColumn(i);
                 if (tableColumn == null) {
                     componentTextArray[i] = null;
                 } else {
                     Object headerValue = tableColumn.getHeaderValue();
                     if (headerValue == null) {
                         componentTextArray[i] = null;
                     } else {
                         componentTextArray[i] = headerValue.toString();
                     }
                 }
             }
         }
         return componentTextArray;
     }
     
     /**
      * Gets column index from string with header name or index
      *
      * @param col Headername or index of column 
      * @param operator The operation used to verify
      * @return column index 
      */
     private int getColumnFromString (String col, String operator) {
         int column = -2;
         try {
             int usrIdxCol = Integer.parseInt(col);
             if (usrIdxCol == 0) {
                 usrIdxCol = usrIdxCol + 1;
             }
             column = IndexConverter.toImplementationIndex(usrIdxCol);
         } catch (NumberFormatException nfe) {
             try {
                 if (m_table.getTableHeader() == null
                         || !(m_table.getTableHeader().isShowing())) {
                     throw new StepExecutionException("No Header", //$NON-NLS-1$
                             EventFactory.createActionError(
                                 TestErrorEvent.NO_HEADER));
                 }
                 for (int i = 0; i < m_table.getColumnCount(); i++) {
                     String header = m_table.getColumnName(i);
                     if (MatchUtil.getInstance().match(header, col, operator)) {
                         column = i;
                     }
                 }
             } catch (IllegalArgumentException iae) {
                 //do nothing here                
             }
         }
         
         return column;
     }
     
     /**
      * Gets row index from string with index or text of first row
      *
      * @param row index or value in first col
      * @param operator The operation used to verify
      * @return integer of String of row 
      */
     private int getRowFromString (String row, String operator) {
         int rowInt = -2;        
         try {
             rowInt = IndexConverter.toImplementationIndex(
                     Integer.parseInt(row));
             if (rowInt == -1) {
                 if (m_table.getTableHeader() == null
                         || !(m_table.getTableHeader().isShowing())) {
                     throw new StepExecutionException("No Header", //$NON-NLS-1$
                             EventFactory.createActionError(
                                 TestErrorEvent.NO_HEADER));
                 }                
             }
         } catch (NumberFormatException nfe) {
             for (int i = 0; i < m_table.getRowCount(); i++) {
                 String cellTxt = getCellText(i, 0);
                 if (MatchUtil.getInstance().match(cellTxt, row,
                         operator)) {
                     return i;
                 }
             }
         }        
         return rowInt;
     }
 
     /**
      * @see org.eclipse.jubula.rc.swing.swing.implclasses.AbstractSwingImplClass#getText()
      * @return the renderer text of the selected cell
      */
     protected String getText() {
         final Cell selectedCell = invokeGetSelectedCell();
         return getCellText(selectedCell.getRow(), selectedCell.getCol());
     }
 
     /**
      * {@inheritDoc}
      */
     public String gdReadValueAtMousePosition(String variable) {
         Cell cellAtMousePosition = (Cell)getEventThreadQueuer().invokeAndWait(
                 "getCellAtMousePosition", new IRunnable() { //$NON-NLS-1$
                     public Object run() throws StepExecutionException {
                         return getCellAtMousePosition();
                     }
                 });
         return getCellText(cellAtMousePosition.getRow(), 
                 cellAtMousePosition.getCol());
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     protected JPopupMenu showPopup(int button) {
        JTableHeader tableHeader = m_table.getTableHeader();
        if ((getRobot()).isMouseInComponent(tableHeader)) {
             JTableHeaderImplClass headerTester = new JTableHeaderImplClass();
            headerTester.setComponent(tableHeader);
             return headerTester.showPopup(button);
         }
         return super.showPopup(button);
     }
     
 }
