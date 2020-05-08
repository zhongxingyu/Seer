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
 package org.eclipse.jubula.rc.swt.implclasses;
 
 import java.awt.Point;
 
 import org.eclipse.jubula.rc.common.CompSystemConstants;
 import org.eclipse.jubula.rc.common.driver.ClickOptions;
 import org.eclipse.jubula.rc.common.driver.IRobot;
 import org.eclipse.jubula.rc.common.driver.IRunnable;
 import org.eclipse.jubula.rc.common.exception.StepExecutionException;
 import org.eclipse.jubula.rc.common.implclasses.IndexConverter;
 import org.eclipse.jubula.rc.common.implclasses.MatchUtil;
 import org.eclipse.jubula.rc.common.implclasses.Verifier;
 import org.eclipse.jubula.rc.common.implclasses.table.Cell;
 import org.eclipse.jubula.rc.common.logger.AutServerLogger;
 import org.eclipse.jubula.rc.swt.driver.DragAndDropHelperSwt;
 import org.eclipse.jubula.rc.swt.interfaces.ITableImplClass;
 import org.eclipse.jubula.rc.swt.listener.TableSelectionTracker;
 import org.eclipse.jubula.rc.swt.utils.SwtUtils;
 import org.eclipse.jubula.tools.constants.InputConstants;
 import org.eclipse.jubula.tools.objects.event.EventFactory;
 import org.eclipse.jubula.tools.objects.event.TestErrorEvent;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.TableCursor;
 import org.eclipse.swt.graphics.FontMetrics;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Item;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 
 
 /**
  * Implementation class for SWT Table.
  *
  * @author BREDEX GmbH
  * @created Oct 30, 2006
  */
 public class TableImplClass extends AbstractControlImplClass 
     implements ITableImplClass {
     /**
      * The logger.
      */
     private static AutServerLogger log = new AutServerLogger(
         TableImplClass.class);
 
     /** the Table from the AUT */
     private Table m_table;
 
     /**
      * {@inheritDoc}
      */
     public void setComponent(Object graphicsComponent) {
         m_table = (Table)graphicsComponent;
     }
     
     /**
      * {@inheritDoc}
      */
     public Control getComponent() {
         return m_table;
     }
     
     /**
      * Checks wether <code>0 <= value < count</code>. 
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
     private void checkRowColBounds(int row, int column)
         throws StepExecutionException {
         
         checkBounds(row, m_table.getItemCount());
         
         // Corner case: Only check the bounds if the table is not being
         //              used as a list or anything other than the first column
         //              is being checked.
         int colCount = m_table.getColumnCount();
         if (colCount > 0 || column > 0) {
             checkBounds(column, colCount);
         }
     }
 
     /**
      * 
      * @param row   The row of the cell
      * @param col   The column of the cell
      * @return The bounding rectangle for the cell, relative to the table's 
      *         location.
      */
     private Rectangle getCellBounds(final int row, final int col) {
         Rectangle cellBounds = (Rectangle)getEventThreadQueuer().invokeAndWait(
                 "evaluateCellBounds", //$NON-NLS-1$
                 new IRunnable() {
                     public Object run() {
                         checkRowColBounds(row, col);
                         TableItem ti = m_table.getItem(row);
                         int column = (m_table.getColumnCount() > 0 || col > 0) 
                             ? col : 0;
                         Rectangle r = ti.getBounds(column);
                         String text = ti.getText(column);
                         Image image = ti.getImage(column);
                         if (text != null && text.length() != 0) {
                             GC gc = new GC(m_table);
                             int charWidth = 0; 
                             try {
                                 FontMetrics fm = gc.getFontMetrics();
                                 charWidth = fm.getAverageCharWidth();
                             } finally {
                                 gc.dispose();
                             }
                             r.width = text.length() * charWidth;
                             if (image != null) {
                                 r.width += image.getBounds().width;
                             }
                         } else if (image != null) {
                             r.width = image.getBounds().width;
                         }
                         if (column > 0) {
                             TableColumn tc = m_table.getColumn(column);
                             int alignment = tc.getAlignment();
                             if (alignment == SWT.CENTER) {
                                r.x += ((double)tc.getWidth() / 2) 
                                        - ((double)r.width / 2);
                             }
                             if (alignment == SWT.RIGHT) {
                                r.x += tc.getWidth() - r.width;
                             }
                         }
                         
                         return new Rectangle(r.x, r.y, r.width, r.height);
                     }
                 });
         return cellBounds;
     }
     
     /**
      * @return the cell under the current mouse position.
      * @throws StepExecutionException If no cell is found.
      */
     protected Cell getCellAtMousePosition() throws StepExecutionException {
         Cell cell = null;
         final Point awtMousePos = getRobot().getCurrentMousePosition();
         org.eclipse.swt.graphics.Point mousePos = 
             new org.eclipse.swt.graphics.Point(awtMousePos.x, awtMousePos.y);
         final int itemCount = m_table.getItemCount();
         for (int rowCount = m_table.getTopIndex(); rowCount < itemCount; 
             rowCount++) {
             if (cell != null) {
                 break;
             }
             final int columnCount = m_table.getColumnCount();
             if (columnCount > 0) {
                 for (int col = 0; col < columnCount; col++) {
                     final Rectangle itemBounds = getCellBounds(rowCount, col);
                     final org.eclipse.swt.graphics.Point absItemBounds = 
                         m_table.toDisplay(itemBounds.x, itemBounds.y);
                     final Rectangle absRect = new Rectangle(
                             absItemBounds.x, absItemBounds.y, 
                             itemBounds.width, itemBounds.height);
                     if (absRect.contains(mousePos)) {
                         cell = new Cell(rowCount, col);
                         break;
                     }
                 }
             } else {
                 final Rectangle itemBounds = getCellBounds(rowCount, 0);
                 final org.eclipse.swt.graphics.Point absItemBounds = 
                     m_table.toDisplay(itemBounds.x, itemBounds.y);
                 final Rectangle absRect = new Rectangle(absItemBounds.x, 
                         absItemBounds.y, itemBounds.width, itemBounds.height);
                 if (absRect.contains(mousePos)) {
                     cell = new Cell(rowCount, 0);
                 }
             }
         }
         if (cell == null) {
             throw new StepExecutionException("No cell under mouse position found!", //$NON-NLS-1$
                 EventFactory.createActionError(TestErrorEvent.NOT_FOUND));
         }
         return cell;
     }
     
     /**
      * @return The currently selected cell of the Table.
      * @throws StepExecutionException If no cell is selected.
      */
     private Cell getSelectedCell() throws StepExecutionException {
         return TableSelectionTracker.getInstance().getSelectedCell(m_table);
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
         Boolean isVisible;
         isVisible = (Boolean)getEventThreadQueuer().invokeAndWait(
                 "isMouseOnHeader", //$NON-NLS-1$
                 new IRunnable() {
                     public Object run() {
                         return new Boolean(m_table.getHeaderVisible());
                     }
                 });
         
         if (!(isVisible.booleanValue())) {
             return false;
         }
         
         Boolean isOnHeader = new Boolean(false);
         isOnHeader = (Boolean)getEventThreadQueuer().invokeAndWait(
                 "isMouseOnHeader", //$NON-NLS-1$
                 new IRunnable() {
                     public Object run() {
                         final Point awtMousePos = getRobot()
                             .getCurrentMousePosition();
                         org.eclipse.swt.graphics.Point mousePos =
                             new org.eclipse.swt.graphics.Point(
                                 awtMousePos.x, awtMousePos.y);
 
                         for (int j = 0; j < m_table.getColumnCount(); j++) {
                             final Rectangle constraints = getHeaderBounds(j);
                             
                             Rectangle bounds = SwtUtils.getWidgetBounds(
                                     m_table);
                             
                             if (constraints != null) {
                                 // Use SWT's mapping function, if possible, as it is more
                                 // multi-platform than simply adding the x and y values.
                                 org.eclipse.swt.graphics.Point
                                 convertedLocation = getConvertedLocation(
                                         constraints);
                                 bounds.x = convertedLocation.x;
                                 bounds.y = convertedLocation.y;
                                 
                                 bounds.height = constraints.height;
                                 bounds.width = constraints.width;
                             }
 
                             if (bounds.contains(mousePos)) {
                                 return new Boolean(true);
                             }
                         }      
                         return new Boolean(false);
                     }
                 });                  
         
         return isOnHeader.booleanValue();
         
     }
     
     
     /**
      * @param constraints Rectangle
      * @return converted Location of table
      */
     private org.eclipse.swt.graphics.Point getConvertedLocation(
             final Rectangle constraints) {
         org.eclipse.swt.graphics.Point convertedLocation =
             (org.eclipse.swt.graphics.Point)getEventThreadQueuer()
                 .invokeAndWait("toDisplay", new IRunnable() { //$NON-NLS-1$
                     public Object run() throws StepExecutionException {
                         return m_table.toDisplay(
                                 constraints.x, constraints.y);
                     }
                 });
         return convertedLocation;
     }
     
     /**
      * @return true, if there is any cell selection in the table, false otherwise.
      */
     private boolean isCellSelection() {
         TableItem[] selItems = (TableItem[])getEventThreadQueuer()
             .invokeAndWait("isCellSelection", //$NON-NLS-1$
                 new IRunnable() {
                     public Object run() {
                         return m_table.getSelection();
                     }
                 });
         return selItems.length > 0;
     }
     
     
     /**
      * In addition to {@link #getSelectedCell()}, the operation is performed in the event queue thread.
      * @return The currently selected cell of the Table.
      * @throws StepExecutionException If no cell is selected.
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
      * In addition to {@link #getCellAtMousePosition()}, the operation is performed in the event queue thread.
      * @return The cell of the Table under the current mouse position
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
      * gets header bounds for column
      * @param col The column.
      * @return The rectangle of the header
      */
     private Rectangle getHeaderBounds(final int col) {
         Rectangle cellBounds;
         cellBounds = (Rectangle)getEventThreadQueuer().invokeAndWait(
             "getHeaderBounds", //$NON-NLS-1$
             new IRunnable() {
                 public Object run() throws StepExecutionException {
                     Rectangle rect = m_table.getItem(0).getBounds(col);
                     rect.y = m_table.getClientArea().y;
                     return rect;
                 }
             });
         return cellBounds;
     }
     
     /**
      * Scrolls the passed cell (as row and column) to visible.
      * @param row The row.
      * @param col The column.
      * @return The rectangle of the cell.
      * @throws StepExecutionException If getting the cell rectangle or the scrolling fails.
      */
     private Rectangle invokeScrollCellToVisible(final int row, final int col)
         throws StepExecutionException {
         
         
         getEventThreadQueuer().invokeAndWait("scrollCellToVisible", //$NON-NLS-1$
             new IRunnable() {
                 public Object run() {
                     if (m_table.getColumnCount() > 0 || col > 0) {
                         m_table.showColumn(m_table.getColumn(col));
                     }
                     m_table.showItem(m_table.getItem(row));
                     return null;
                 }
             });
 
         final Rectangle cellBoundsRelativeToParent = getCellBounds(row, col);
         
         getEventThreadQueuer().invokeAndWait("getCellBoundsRelativeToParent", //$NON-NLS-1$
             new IRunnable() {
                 public Object run() {
                     org.eclipse.swt.graphics.Point cellOriginRelativeToParent = 
                         getComponent().getDisplay().map(
                                 getComponent(), getComponent().getParent(), 
                                 new org.eclipse.swt.graphics.Point(
                                         cellBoundsRelativeToParent.x, 
                                         cellBoundsRelativeToParent.y));
                     cellBoundsRelativeToParent.x = 
                         cellOriginRelativeToParent.x;
                     cellBoundsRelativeToParent.y = 
                         cellOriginRelativeToParent.y;
                     return null;
                 }
             });
 
         Control parent = (Control)getEventThreadQueuer().invokeAndWait("getParent", //$NON-NLS-1$
                 new IRunnable() {
                 public Object run() {
                     getComponent().getParent();
                     return null;
                 }
             });
 
         
         getRobot().scrollToVisible(
                 parent, cellBoundsRelativeToParent);
         
         return getVisibleBounds(getCellBounds(row, col));
     }
 
     /**
      * Computes the visible cellBounds inside the visible bounds of the table.<br>
      * The result is the intersection of the visible bounds of the table and the 
      * bounds of the cell.
      * @param cellBounds the bounds of the cell to click in. These bounds must
      *                  be relative to the table's location.
      * @return the visible cell bounds, relative to the table's location.
      */
     private Rectangle getVisibleBounds(Rectangle cellBounds) {
         org.eclipse.swt.graphics.Rectangle r = 
             (org.eclipse.swt.graphics.Rectangle)
             getEventThreadQueuer().invokeAndWait("getVisibleCellBounds: " + cellBounds,  //$NON-NLS-1$
                     new IRunnable() {
 
                     public Object run() {
                         return m_table.getClientArea();
                     }
                 });
         
         Rectangle visibleTableBounds = new Rectangle(
             r.x, r.y, r.width, r.height);
         Rectangle visibleCellBounds = 
             visibleTableBounds.intersection(cellBounds);
         return visibleCellBounds;
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
      * Selects a table cell in the given row and column via click in the midle of the cell.
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
         final int implRow = getRowFromString(row, rowOperator);
         final int implCol = getColumnFromString(col, colOperator);
         final boolean isExtendSelection = extendSelection.equals(
                 CompSystemConstants.EXTEND_SELECTION_YES); 
         if (log.isDebugEnabled()) {
             log.debug("Selecting row, col: " + row + ", " + col); //$NON-NLS-1$//$NON-NLS-2$
         }
         
         Rectangle cellBounds;
         //if row is header and col is existing
         if (implRow == -1 && implCol > -1) {
             cellBounds = getHeaderBounds(implCol);
         } else {
             cellBounds = invokeScrollCellToVisible(implRow, implCol);
         }        
         
         ClickOptions clickOptions = ClickOptions.create();
         clickOptions.setClickCount(clickCount).setScrollToVisible(false);
         clickOptions.setMouseButton(button);
         try {
             if (isExtendSelection) {
                 getRobot().keyPress(m_table, SWT.MOD1);
             }
             getRobot().click(m_table, cellBounds, clickOptions, 
                     xPos, xUnits.equalsIgnoreCase(POS_UNIT_PIXEL), 
                     yPos, yUnits.equalsIgnoreCase(POS_UNIT_PIXEL));
         } finally {
             if (isExtendSelection) {
                 getRobot().keyRelease(m_table, SWT.MOD1);
             }
         }
     }
     
     /**
      * Selects the cell of the Table.<br>
      * With the xPos, yPos, xunits and yUnits the click position inside the cell can be defined.
      * @param row The row of the cell.
      * @param col The column of the cell.
      * @param clickCount The number of clicks with the right mouse button
      * @param xPos what x position
      * @param xUnits should x position be pixel or percent values
      * @param yPos what y position
      * @param yUnits should y position be pixel or percent values
      * @param extendSelection Should this selection be part of a multiple selection
      * @throws StepExecutionException If the row or the column is invalid
      * @deprecated Will be removed with gdSelectCell with String parameter
      * for Row/Column
      */
     public void gdSelectCell(final int row, final int col, 
         final int clickCount, final int xPos, final String xUnits,
         final int yPos, final String yUnits, final String extendSelection) 
         throws StepExecutionException {
 
         final int implRow = IndexConverter.toImplementationIndex(row);
         final int implCol = IndexConverter.toImplementationIndex(col);
         final boolean isExtendSelection = extendSelection.equals(
                 CompSystemConstants.EXTEND_SELECTION_YES); 
         if (log.isDebugEnabled()) {
             log.debug("Selecting row, col: " + row + ", " + col); //$NON-NLS-1$//$NON-NLS-2$
         }
 
         Rectangle cellBounds = invokeScrollCellToVisible(implRow, implCol);
         ClickOptions clickOptions = ClickOptions.create();
         clickOptions.setClickCount(clickCount).setScrollToVisible(false);
         
         try {
             if (isExtendSelection) {
                 getRobot().keyPress(m_table, SWT.MOD1);
             }
             getRobot().click(m_table, cellBounds, clickOptions.left(), 
                     xPos, xUnits.equalsIgnoreCase(POS_UNIT_PIXEL), 
                     yPos, yUnits.equalsIgnoreCase(POS_UNIT_PIXEL));
         } finally {
             if (isExtendSelection) {
                 getRobot().keyRelease(m_table, SWT.MOD1);
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
         Cell cell = null;
         try {
             cell = invokeGetCellAtMousePosition();
         } catch (StepExecutionException e) {
             cell = invokeGetSelectedCell();
         }
         int newCol = cell.getCol();
         int newRow = cell.getRow();
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
         //String rowStr = (new Integer(newRow)).toString();
         //String colStr = (new Integer(newCol)).toString();
         gdSelectCell(newRow, newCol, clickCount, 
                      xPos, xUnits, yPos, yUnits, extendSelection);
     }
     
     
     /**
      * Selects the passed row of the JTable. This is actually a selection of the cell <code>(row, 0)</code>.
      * @param row The row
      * @param extendSelection Should this selection be part of a multiple selection
      * @throws StepExecutionException If the row is invalid
      * @deprecated the same as gdSelectCell
      * Will be removed!
      */
     public void gdSelectRow(int row, String extendSelection) 
         throws StepExecutionException {
         gdSelectCell(row, IndexConverter.toUserIndex(0), 1, extendSelection);
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
 
         final int implRow = getRowFromString(row, rowOperator);
         final int implCol = getColumnFromString(col, colOperator);
         String current;
         //if row is header and column is existing
         if (implRow == -1 && implCol > -1) {
             current = (String)getEventThreadQueuer().invokeAndWait("gdVerifyText", //$NON-NLS-1$
                     new IRunnable() {
                         public Object run() {
                             return m_table.getColumn(implCol).getText();
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
      * @param row The row of the cell.
      * @param col The column of the cell.
      * @param text The cell text to verify.
      * @param operator The operation used to verify
      * @throws StepExecutionException If the row or the column is invalid, or if the rendered text cannot be extracted.
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
         String current = (String)getEventThreadQueuer().invokeAndWait("getCellText", //$NON-NLS-1$
                 new IRunnable() {
                     public Object run() {
                         String value = m_table.getItem(row).getText(col);
                         if (log.isDebugEnabled()) {
                             log.debug("Getting cell text:"); //$NON-NLS-1$
                             log.debug("Row, col: " + row + ", " + col); //$NON-NLS-1$ //$NON-NLS-2$
                             log.debug("Value: " + value); //$NON-NLS-1$
                         }
                         return value;
                     }
                 });
         return current;
     }
 
     /**
      * Verifies the rendered text inside the currently selected cell.
      * @param text The cell text to verify.
      * @param operator The operation used to verify
      * @throws StepExecutionException If there is no selected cell, or if the rendered text cannot be extracted.
      */
     public void gdVerifyText(String text, String operator)
         throws StepExecutionException {
         
         Cell cell = invokeGetSelectedCell();
         gdVerifyText(text, operator, IndexConverter.toUserIndex(cell.getRow()),
                 IndexConverter.toUserIndex(cell.getCol()));
     }
     
     
     /**
      * Verifies the rendered text inside cell at the mouse position on screen.
      * @param text The cell text to verify.
      * @param operator The operation used to verify
      * @throws StepExecutionException If there is no selected cell, or if the rendered text cannot be extracted.
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
      * @param text The cell text to verify.
      * @throws StepExecutionException If there is no selected cell, or if the rendered text cannot be extracted.
      */
     public void gdVerifyText(String text)
         throws StepExecutionException {
 
         gdVerifyText(text, MatchUtil.DEFAULT_OPERATOR);
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
         final int implRow = getRowFromString(row, rowOperator);
         Boolean valueExists = new Boolean(true);
         //if row is header
         if (implRow == -1) {
             valueExists = (Boolean)getEventThreadQueuer().invokeAndWait(
                     "selectCellByColValue", new IRunnable() { //$NON-NLS-1$
                         public Object run() throws StepExecutionException {
                             for (int k = getStartingColIndex(searchType); 
                                     k < m_table.getColumnCount(); ++k) {
                                 if (MatchUtil.getInstance().match(
                                         m_table.getColumn(k).getText(),
                                         value, operator)) {
                                     return new Boolean(true);
                                 }
                             }                            
                             return new Boolean(false);
                         }
                     });
         } else {
             valueExists = (Boolean)getEventThreadQueuer().invokeAndWait(
                     "selectCellByColValue", new IRunnable() { //$NON-NLS-1$
                         public Object run() throws StepExecutionException {
                             final int columnCount = m_table.getColumnCount();
                             if (columnCount > 0) {
                                 for (int i = getStartingColIndex(searchType); 
                                         i < columnCount; ++i) {
                                     if (MatchUtil.getInstance().match(
                                             m_table.getItem(implRow)
                                                 .getText(i), value, operator)) {
                                         return new Boolean(true);
                                     }
                                 }
                             } else {
                                 // No columns found. This table is used to present a
                                 // list-like component.
                                 if (MatchUtil.getInstance().match(
                                         m_table.getItem(implRow).getText(),
                                             value, operator)) {
                                     return new Boolean(true);
                                 }
                             }
                             return new Boolean(false);
                         }
                     });
         }
         Verifier.equals(exists, valueExists.booleanValue());
     }
     
     /**
      * Verifies, if value exists in column.
      *
      * @param col The column of the cell.
      * @param colOperator The column header operator
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
         Boolean valueExists = new Boolean(true);
         valueExists = (Boolean)getEventThreadQueuer().invokeAndWait(
             "verifyValueInColumn", //$NON-NLS-1$
             new IRunnable() {
                 public Object run() throws StepExecutionException {
                     final int itemCount = m_table.getItemCount();
                     for (int i = getStartingRowIndex(searchType); 
                             i < itemCount; ++i) {
                         if (MatchUtil.getInstance().match(m_table.getItem(i)
                                 .getText(implCol), value, operator)) {
                             return new Boolean(true);
                         }
                     }
                     if (m_table.getHeaderVisible()) {
                         String header = m_table.getColumn(implCol).getText();
                         if (MatchUtil.getInstance().match(header, value,
                                 operator)) {
                             return new Boolean(true);
                         }
                     }
                     return new Boolean(false);
                 }
             });
         Verifier.equals(exists, valueExists.booleanValue());
     }
 
     
     /**
      * @param cellEditor The cell editor to check.
      * @return <code>true</code> if the given cell editor is editable.
      */
     private boolean isEditable(Control cellEditor) {
 
         if (cellEditor == null || cellEditor instanceof TableCursor
             || cellEditor == m_table) {
             // No actual editor found.
             return false;
         }
         
         return (cellEditor.getStyle() & SWT.READ_ONLY) == 0;
     }  
 
     /**
      * Tries to activate the editor for the given cell.
      * 
      * @param cell The cell for which to find the editor.
      * @return The best guess for the editor of the given cell.
      */
     private Control activateEditor(Cell cell) {
 
         Control editor = getTableCellEditor(cell);
         // sometimes the editor only appears after doubleclick!
         if (!invokeIsEditable(editor)) {
             Rectangle cellBounds = 
                 invokeScrollCellToVisible(cell.getRow(), cell.getCol());
             ClickOptions co = ClickOptions.create().setClickCount(2)
                 .setScrollToVisible(false);
             Control clickTarget = editor == null 
                 || editor instanceof TableCursor ? m_table : editor;
             getRobot().click(clickTarget, cellBounds, co);
             editor = getTableCellEditor(cell);
         }
 
         return editor;
     }
 
     /**
      * @param cellEditor The cell editor to check.
      * @return <code>true</code> if the given editor is editable. Otherwise
      *         <code>false</code>.
      */
     private boolean invokeIsEditable(final Control cellEditor) {
 
         boolean isEditable = ((Boolean)getEventThreadQueuer().invokeAndWait(
                 "getSelectedCell", //$NON-NLS-1$
                 new IRunnable() {
                     public Object run() {
                         return isEditable(cellEditor) 
                             ? Boolean.TRUE : Boolean.FALSE;
                     }
                 })).booleanValue();
         return isEditable;
     }
 
     /**
      * Verifies the editable property of the current selected cell.
      * @param editable The editable property to verify.
      */
     public void gdVerifyEditable(boolean editable) {
         Cell cell = invokeGetSelectedCell();
         Control editor = activateEditor(cell);
         Verifier.equals(editable, invokeIsEditable(editor));
     }
     
     /**
      * Verifies the editable property of the cell at mouse position.
      * @param editable The editable property to verify.
      */
     private void verifyEditableMousePosition(boolean editable) {
         Cell cell = invokeGetCellAtMousePosition();
         Control editor = activateEditor(cell);
         Verifier.equals(editable, invokeIsEditable(editor));
     }
 
     /**
      * Verifies the editable property of the given indices.
      * @param editable The editable property to verify.
      * @param row The row of the cell.
      * @param rowOperator The row header operator
      * @param col The column of the cell.
      * @param colOperator The column operator
      */
     public void gdVerifyEditable(boolean editable, String row,
             String rowOperator, String col, String colOperator) {
         //if row is header row
         if (getRowFromString(row, rowOperator) == -1) {
             throw new StepExecutionException("Unsupported Header Action", //$NON-NLS-1$
                     EventFactory.createActionError(
                             TestErrorEvent.UNSUPPORTED_HEADER_ACTION));
         }  
         gdSelectCell(row, rowOperator, col, colOperator, 
                 ClickOptions.create().setClickCount(1), 
                 CompSystemConstants.EXTEND_SELECTION_NO);
         gdVerifyEditable(editable);
     }
     
     /**
      * Verifies the editable property of the given indices.
      * @param editable The editable property to verify.
      * @param row The table row
      * @param col The table column
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
         if (isMouseOnHeader()) {
             throw new StepExecutionException("Unsupported Header Action", //$NON-NLS-1$
                     EventFactory.createActionError(
                             TestErrorEvent.UNSUPPORTED_HEADER_ACTION));
         }
         verifyEditableMousePosition(editable);
     }
     
     /**
      * Gets the TableCellEditor of the given cell.
      * The Cell has to be activated before!
      * @param cell the cell.
      * @return the TableCellEditor
      */
     private Control getTableCellEditor(final Cell cell) {
         getRobot().click(m_table, 
             invokeScrollCellToVisible(cell.getRow(), cell.getCol()),
             ClickOptions.create().setClickCount(0));
 
         
         return SwtUtils.getCursorControl();
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
         if (getRowFromString(row, rowOperator) == -1) {
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
         // Ensure that the cell is visible.
         invokeScrollCellToVisible(cell.getRow(), cell.getCol());
         
         // Get the editor for the cell
         Control editor = activateEditor(cell);
         
         // Verify that the cell is editable
         if (!invokeIsEditable(editor)) {
             throw new StepExecutionException("Selected table cell " //$NON-NLS-1$
                 + cell + " is not editable", //$NON-NLS-1$
                 EventFactory.createActionError(TestErrorEvent.NOT_EDITABLE));
         }
         // Perform 3 clicks with the editor as target, not the table.
         // Don't scroll to visible here, it's already done.
         if (replace) {
             getRobot().clickAtCurrentPosition(editor, 3, 
                     InputConstants.MOUSE_BUTTON_LEFT);
         } else {
             getRobot().clickAtCurrentPosition(editor, 2, 
                     InputConstants.MOUSE_BUTTON_LEFT);
         }
         getRobot().type(editor, text);
         // FIXME Andreas: do not leave with implizit ENTER. Use UTF-8 (later)
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
         if (getRowFromString(row, rowOperator) == -1) {
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
      * Types the text in the specified cell.
      * @param text The text
      * @param row The table row
      * @param col The table column
      * @throws StepExecutionException If the text input fails
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
      * @param count Number of clicks
      * @param button The mouse button
      */
     public void gdClick(int count, int button) {
         Cell cell = null;
         if (isMouseOverCell()) {
             cell = invokeGetCellAtMousePosition();
         } else if (isCellSelection()) {
             cell = invokeGetSelectedCell();
         } 
         if (cell != null) {
             Rectangle cellRect = 
                 invokeScrollCellToVisible(cell.getRow(), cell.getCol());
             getRobot().click(m_table, cellRect, ClickOptions.create()
                 .setClickCount(count).setMouseButton(button));
         } else {
             super.gdClick(count, button);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void gdClickDirect(int count, int button, int xPos, String xUnits, 
         int yPos, String yUnits) throws StepExecutionException {
         
         int correctedYPos = correctYPos(yPos, yUnits);
         super.gdClickDirect(count, button, xPos, xUnits, correctedYPos, yUnits);
     }
 
     /**
      * Corrects the given Y position based on the height of the table's header.
      * This ensures, for example, that test steps don't try to click within the
      * table header (where we receive no confirmation events).
      * 
      * @param pos The Y position to correct.
      * @param units The units used for the Y position.
      * @return The corrected Y position.
      */
     private int correctYPos(int pos, String units) {
         int correctedPos = pos;
         int headerHeight = ((Integer)getEventThreadQueuer().invokeAndWait(
                 "getHeaderHeight", new IRunnable() { //$NON-NLS-1$
 
                     public Object run() throws StepExecutionException {
                         return new Integer(
                             ((Table)getComponent()).getHeaderHeight());
                     }
             
                 })).intValue();
 
         if (POS_UNIT_PIXEL.equalsIgnoreCase(units)) {
             // Pixel units
             correctedPos += headerHeight;
         } else {
             // Percentage units
             int totalHeight = ((Integer)getEventThreadQueuer().invokeAndWait(
                     "getWidgetBounds", new IRunnable() { //$NON-NLS-1$
 
                         public Object run() throws StepExecutionException {
                             return new Integer(
                                 SwtUtils.getWidgetBounds(
                                     getComponent()).height);
                         }
             
                     })).intValue();
             long targetHeight = totalHeight - headerHeight;
             long targetPos = Math.round((double)targetHeight * (double)pos
                 / 100.0);
             targetPos += headerHeight;
             double heightPercentage = 
                 (double)targetPos / (double)totalHeight * 100.0;
             correctedPos = (int)Math.round(heightPercentage);
             if (correctedPos > 100) { // rounding error
                 correctedPos = 100;
             }
         }
         return correctedPos;
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
      * @param col the column
      * @param value the value
      * @param clickCount the number of clicks.
      * @param regexOp the regex operator
      * @param extendSelection Should this selection be part of a multiple selection
      * @param searchType Determines where the search begins ("relative" or "absolute")
      * @deprecated Will be removed with gdSelectRowByValue with String parameter
      * for Row/Column
      */
     public void gdSelectRowByValue(int col, final String value,
         int clickCount, final String regexOp, final String extendSelection, 
         final String searchType) {
         
         gdSelectRowByValue(col, value, regexOp, extendSelection,
                 searchType, clickCount);
     }
     
     
     /**
      * Finds the first row which contains the value <code>value</code>
      * in column <code>col</code> and selects this row.
      * @param col the column
      * @param colOperator The column header operator
      * @param value the value
      * @param regexOp the regex operator
      * @param extendSelection Should this selection be part of a multiple selection
      * @param searchType Determines where the search begins ("relative" or "absolute")
      * @param co the click options to use
      */
     protected void gdSelectRowByValue(String col, final String colOperator,
         final String value, final String regexOp, final String extendSelection, 
         final String searchType, ClickOptions co) {
         
         final int implCol = getColumnFromString(col, colOperator);
         Integer implRow;
         implRow = (Integer)getEventThreadQueuer().invokeAndWait(
             "selectRowByValue", //$NON-NLS-1$
             new IRunnable() {
                 public Object run() throws StepExecutionException {
                     final int itemCount = m_table.getItemCount();
                     for (int i = getStartingRowIndex(searchType); 
                             i < itemCount; ++i) {
                         if (MatchUtil.getInstance().match(m_table.getItem(i)
                                 .getText(implCol), value, regexOp)) {
                             return new Integer(i);
                         }
                     }
                     String header = m_table.getColumn(implCol).getText();
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
         
         gdSelectCell(userIdxRow, MatchUtil.EQUALS, userIdxCol, colOperator,
                 co, extendSelection);
     }
     
     /**
      * Finds the first row which contains the value <code>value</code>
      * in column <code>col</code> and selects this row.
      * @param col the column
      * @param value the value
      * @param regexOp the regex operator
      * @param extendSelection Should this selection be part of a multiple selection
      * @param searchType Determines where the search begins ("relative" or "absolute")
      * @param clickCount the number of clicks.
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
                     final int itemCount = m_table.getItemCount();
                     for (int i = getStartingRowIndex(searchType); 
                             i < itemCount; ++i) {
                         if (MatchUtil.getInstance().match(m_table.getItem(i)
                                 .getText(implCol), value, regexOp)) {
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
      * @param row the row
      * @param rowOperator the row header operator
      * @param value the value
      * @param clickCount the mouse clicks.
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
      * @param clickCount the mouse clicks.
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
         //if row is header
         if (implRow == -1) {
             implCol = (Integer)getEventThreadQueuer().invokeAndWait(
                     "selectCellByColValue", new IRunnable() { //$NON-NLS-1$
                         public Object run() throws StepExecutionException {
                             for (int k = getStartingColIndex(searchType); 
                                     k < m_table.getColumnCount(); ++k) {
                                 if (MatchUtil.getInstance().match(
                                         m_table.getColumn(k).getText(),
                                         value, regex)) {
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
                             final int columnCount = m_table.getColumnCount();
                             if (columnCount > 0) {
                                 for (int i = getStartingColIndex(searchType); 
                                         i < columnCount; ++i) {
                                     if (MatchUtil.getInstance().match(
                                             m_table.getItem(implRow)
                                                 .getText(i), value, regex)) {
                                         return new Integer(i);
                                     }
                                 }
                             } else {
                                 // No columns found. This table is used to present a
                                 // list-like component.
                                 if (MatchUtil.getInstance().match(
                                         m_table.getItem(implRow).getText(),
                                             value, regex)) {
                                     return new Integer(0);
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
      * @param clickCount the mouse clicks.
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
                     final int columnCount = m_table.getColumnCount();
                     if (columnCount > 0) {
                         for (int i = getStartingColIndex(searchType); 
                                 i < columnCount; ++i) {
                             if (MatchUtil.getInstance().match(m_table.getItem(
                                     implRow).getText(i), value, regex)) {
                                 
                                 return new Integer(i);
                             }
                         }
                     } else {
                         // No columns found. This table is used to present a
                         // list-like component.
                         if (MatchUtil.getInstance().match(m_table.getItem(
                                 implRow).getText(), value, regex)) {
                             
                             return new Integer(0);
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
      * Action to read the value of the passed cell and 
      * to store it in a variable in the Client
      * @param row The row of the cell.
      * @param rowOperator The row operator
      * @param col The column of the cell.
      * @param colOperator The column operator
      * @param variable the name of the variable
      * @return the text value.
      */
     public String gdReadValue(String variable, final String row,
             final String rowOperator, final String col,
             final String colOperator) throws StepExecutionException {
 
         final int implRow = getRowFromString(row, rowOperator);
         final int implCol = getColumnFromString(col, colOperator);
         
         //if row is header and column is existing
         if (implRow == -1 && implCol > -1) {
             getEventThreadQueuer().invokeAndWait("gdReadValue", //$NON-NLS-1$
                     new IRunnable() {
                         public Object run() {
                             return m_table.getColumn(implCol).getText();
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
      * Action to read the value of the passed cell and 
      * to store it in a variable in the Client
      * @param row The row of the cell.
      * @param col The column of the cell.
      * @param variable the name of the variable
      * @return the text value.
      * @deprecated Will be removed with gdReadValue with String parameter
      * for Row/Column
      */
     public String gdReadValue(String variable, final int row,
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
         return getCellText(implRow, implCol);
     }
 
     /**
      * Action to read the value of the current selected cell of the JTable 
      * to store it in a variable in the Client
      * @param variable the name of the variable
      * @return the text value.
      */
     public String gdReadValue(String variable) {
         final Cell selectedCell = invokeGetSelectedCell();
         return getCellText(selectedCell.getRow(), selectedCell.getCol());
     }
     
     
     /**
      * Drags the cell of the Table.<br>
      * With the xPos, yPos, xunits and yUnits the click position inside the 
      * cell can be defined.
      * 
      * @param mouseButton the mouseButton.
      * @param modifier the modifier.
      * @param row The row of the cell.
      * @param rowOperator the row header operator
      * @param col The column of the cell.
      * @param colOperator the column header operator
      * @param xPos what x position
      * @param xUnits should x position be pixel or percent values
      * @param yPos what y position
      * @param yUnits should y position be pixel or percent values
      * @throws StepExecutionException
      *             If the row or the column is invalid
      */
     public void gdDragCell(final int mouseButton, final String modifier, 
             final String row, String rowOperator, final String col,
             final String colOperator, final int xPos, final String xUnits,
             final int yPos, final String yUnits) 
         throws StepExecutionException {
         
         final DragAndDropHelperSwt dndHelper = DragAndDropHelperSwt
             .getInstance();
         dndHelper.setMouseButton(mouseButton);
         dndHelper.setModifier(modifier);
         dndHelper.setDragComponent(null);
         
         gdSelectCell(row, rowOperator, col, colOperator, 0, xPos,
                 xUnits, yPos, yUnits, 
                 CompSystemConstants.EXTEND_SELECTION_NO, 1);
     }
     
     /**
      * Drags the cell of the Table.<br>
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
         
         final DragAndDropHelperSwt dndHelper = DragAndDropHelperSwt
             .getInstance();
         dndHelper.setMouseButton(mouseButton);
         dndHelper.setModifier(modifier);
         dndHelper.setDragComponent(null);
         
         gdSelectCell(row, col, 0, xPos, xUnits, yPos, yUnits, 
                 CompSystemConstants.EXTEND_SELECTION_NO);
     }
     
     /**
      * Drops on the cell of the JTable.<br>
      * With the xPos, yPos, xunits and yUnits the click position inside the 
      * cell can be defined.
      * 
      * @param row The row of the cell.
      * @param rowOperator The row operator
      * @param col The column of the cell.
      * @param colOperator The column operator
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
         
         final DragAndDropHelperSwt dndHelper = DragAndDropHelperSwt
             .getInstance();
         final IRobot robot = getRobot();
         
         pressOrReleaseModifiers(dndHelper.getModifier(), true);
         try {
             getEventThreadQueuer().invokeAndWait("gdDropCell", new IRunnable() { //$NON-NLS-1$
 
                 public Object run() throws StepExecutionException {
                     // drag
                     robot.mousePress(dndHelper.getDragComponent(), null, 
                             dndHelper.getMouseButton());
 
                     shakeMouse();
 
                     // drop
                     gdSelectCell(row, rowOperator, col, colOperator, 0, xPos,
                             xUnits, yPos, yUnits, 
                             CompSystemConstants.EXTEND_SELECTION_NO, 1);
                     return null;
                 }            
             });
 
             waitBeforeDrop(delayBeforeDrop);
             
         } finally {
             robot.mouseRelease(dndHelper.getDragComponent(), null, 
                     dndHelper.getMouseButton());
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
         
         final DragAndDropHelperSwt dndHelper = DragAndDropHelperSwt
             .getInstance();
         final IRobot robot = getRobot();
         
         pressOrReleaseModifiers(dndHelper.getModifier(), true);
         try {
             getEventThreadQueuer().invokeAndWait("gdDropCell", new IRunnable() { //$NON-NLS-1$
 
                 public Object run() throws StepExecutionException {
                     // drag
                     robot.mousePress(dndHelper.getDragComponent(), null, 
                             dndHelper.getMouseButton());
                     
                     shakeMouse();
 
                     // drop
                     gdSelectCell(row, col, 0, xPos, xUnits, yPos, yUnits, 
                             CompSystemConstants.EXTEND_SELECTION_NO);
                     return null;
                 }            
             });
 
             waitBeforeDrop(delayBeforeDrop);
             
         } finally {
             robot.mouseRelease(dndHelper.getDragComponent(), null, 
                     dndHelper.getMouseButton());
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
         
         final DragAndDropHelperSwt dndHelper = DragAndDropHelperSwt
             .getInstance();
         dndHelper.setMouseButton(mouseButton);
         dndHelper.setModifier(modifier);
         dndHelper.setDragComponent(null);
         
         gdSelectRowByValue(col, colOperator, value, regexOp, 1, 
                 CompSystemConstants.EXTEND_SELECTION_NO,
                 searchType, 1);
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
         
         final DragAndDropHelperSwt dndHelper = DragAndDropHelperSwt
             .getInstance();
         dndHelper.setMouseButton(mouseButton);
         dndHelper.setModifier(modifier);
         dndHelper.setDragComponent(null);
         
         gdSelectRowByValue(col, value, 1, regexOp, 
                 CompSystemConstants.EXTEND_SELECTION_NO, searchType);
     }
     
     
     /**
      * Finds the first row which contains the value <code>value</code>
      * in column <code>col</code> and drops on this row.
      * 
      * @param col the column
      * @param colOperator the column operator
      * @param value the value
      * @param regexOp the regex operator
      * @param searchType Determines where the search begins ("relative" or "absolute")
      * @param delayBeforeDrop the amount of time (in milliseconds) to wait
      *                        between moving the mouse to the drop point and
      *                        releasing the mouse button                       
      */
     public void gdDropRowByValue(final String col, final String colOperator,
             final String value, final String regexOp, final String searchType, 
             int delayBeforeDrop) {
         
         final DragAndDropHelperSwt dndHelper = DragAndDropHelperSwt
             .getInstance();
         final IRobot robot = getRobot();
         pressOrReleaseModifiers(dndHelper.getModifier(), true);
         
         try {
             getEventThreadQueuer().invokeAndWait("gdDropRowByValue", new IRunnable() { //$NON-NLS-1$
 
                 public Object run() throws StepExecutionException {
                     // drag
                     robot.mousePress(dndHelper.getDragComponent(), null, 
                             dndHelper.getMouseButton());
 
                     shakeMouse();
 
                     // drop
                     gdSelectRowByValue(col, colOperator, value, regexOp,
                             CompSystemConstants.EXTEND_SELECTION_NO, 
                             searchType, 
                             ClickOptions.create().setClickCount(0));
                     return null;
                 }            
             });
             
             waitBeforeDrop(delayBeforeDrop);
             
         } finally {
             robot.mouseRelease(dndHelper.getDragComponent(), null, 
                     dndHelper.getMouseButton());
             pressOrReleaseModifiers(dndHelper.getModifier(), false);
         }
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
     public void gdDropRowByValue(final int col, final String value, 
             final String regexOp, final String searchType, 
             int delayBeforeDrop) {
         
         final DragAndDropHelperSwt dndHelper = DragAndDropHelperSwt
             .getInstance();
         final IRobot robot = getRobot();
         pressOrReleaseModifiers(dndHelper.getModifier(), true);
         
         try {
             getEventThreadQueuer().invokeAndWait("gdDropRowByValue", new IRunnable() { //$NON-NLS-1$
 
                 public Object run() throws StepExecutionException {
                     // drag
                     robot.mousePress(dndHelper.getDragComponent(), null, 
                             dndHelper.getMouseButton());
 
                     shakeMouse();
 
                     // drop
                     gdSelectRowByValue(col, value, regexOp, 
                             CompSystemConstants.EXTEND_SELECTION_NO,
                             searchType, 0);
                     return null;
                 }            
             });
             
             waitBeforeDrop(delayBeforeDrop);
             
         } finally {
             robot.mouseRelease(dndHelper.getDragComponent(), null, 
                     dndHelper.getMouseButton());
             pressOrReleaseModifiers(dndHelper.getModifier(), false);
         }
     }
     
     
     /**
      * Finds the first column which contains the value <code>value</code>
      * in the given row and drags the cell.
      * 
      * @param mouseButton the mouse button
      * @param modifier the modifiers
      * @param row the row
      * @param rowOperator the row header operator
      * @param value the value
      * @param regex search using regex
      * @param searchType Determines where the search begins ("relative" or "absolute")
      */
     public void gdDragCellByColValue(int mouseButton, String modifier,
             String row, String rowOperator, final String value,
             final String regex, final String searchType) {
         
         final DragAndDropHelperSwt dndHelper = DragAndDropHelperSwt
             .getInstance();
         dndHelper.setMouseButton(mouseButton);
         dndHelper.setModifier(modifier);
         dndHelper.setDragComponent(null);
         gdSelectCellByColValue(row, rowOperator, value, regex, 
                 CompSystemConstants.EXTEND_SELECTION_NO,
                 searchType, ClickOptions.create().setClickCount(0));
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
      * @deprecated Will be removed with gdDragCell with String parameter
      * for Row/Column
      */
     public void gdDragCellByColValue(int mouseButton, String modifier, int row, 
             final String value, final String regex, final String searchType) {
         
         final DragAndDropHelperSwt dndHelper = DragAndDropHelperSwt
             .getInstance();
         dndHelper.setMouseButton(mouseButton);
         dndHelper.setModifier(modifier);
         dndHelper.setDragComponent(null);
         gdSelectCellByColValue(row, value, regex, 
                 CompSystemConstants.EXTEND_SELECTION_NO, searchType, 
                 0);
     }
     
     
     /**
      * Finds the first column which contains the value <code>value</code>
      * in the given row and drops on the cell.
      * 
      * @param row the row
      * @param rowOperator the row header operator
      * @param value the value
      * @param regex search using regex
      * @param searchType Determines where the search begins ("relative" or "absolute")
      * @param delayBeforeDrop the amount of time (in milliseconds) to wait
      *                        between moving the mouse to the drop point and
      *                        releasing the mouse button                       
      */
     public void gdDropCellByColValue(final String row, final String rowOperator,
             final String value, final String regex, final String searchType,
             int delayBeforeDrop) {
         
         final DragAndDropHelperSwt dndHelper = DragAndDropHelperSwt
             .getInstance();
         final IRobot robot = getRobot();
         pressOrReleaseModifiers(dndHelper.getModifier(), true);
 
         try {
             getEventThreadQueuer().invokeAndWait("gdDropCellByColValue", new IRunnable() { //$NON-NLS-1$
 
                 public Object run() throws StepExecutionException {
                     // drag
                     robot.mousePress(dndHelper.getDragComponent(), null, 
                             dndHelper.getMouseButton());
 
                     shakeMouse();
 
                     // drop
                     gdSelectCellByColValue(row, rowOperator, value, regex,
                             CompSystemConstants.EXTEND_SELECTION_NO, 
                             searchType, 
                             ClickOptions.create().setClickCount(0));
                     return null;
                 }            
             });
 
             waitBeforeDrop(delayBeforeDrop);
         
         } finally {
             robot.mouseRelease(dndHelper.getDragComponent(), null, 
                     dndHelper.getMouseButton());
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
     public void gdDropCellByColValue(final int row, final String value, 
             final String regex, final String searchType, int delayBeforeDrop) {
         
         final DragAndDropHelperSwt dndHelper = DragAndDropHelperSwt
             .getInstance();
         final IRobot robot = getRobot();
         pressOrReleaseModifiers(dndHelper.getModifier(), true);
 
         try {
             getEventThreadQueuer().invokeAndWait("gdDropCellByColValue", new IRunnable() { //$NON-NLS-1$
 
                 public Object run() throws StepExecutionException {
                     // drag
                     robot.mousePress(dndHelper.getDragComponent(), null, 
                             dndHelper.getMouseButton());
 
                     shakeMouse();
 
                     // drop
                     gdSelectCellByColValue(row, value, regex, 
                             CompSystemConstants.EXTEND_SELECTION_NO, 
                             searchType, 0);
                     return null;
                 }            
             });
 
             waitBeforeDrop(delayBeforeDrop);
         
         } finally {
             robot.mouseRelease(dndHelper.getDragComponent(), null, 
                     dndHelper.getMouseButton());
             pressOrReleaseModifiers(dndHelper.getModifier(), false);
         }
     }
     
     /**
      * Gets column index from string with header name or index
      *
      * @param col Headername or index of column
      * @param operator The operator
      * @return column index 
      */
     private int getColumnFromString (final String col, final String operator) {
         int column = -2;
         try {
             int usrIdxCol = Integer.parseInt(col);
             if (usrIdxCol == 0) {
                 usrIdxCol = usrIdxCol + 1;
             }
             column = IndexConverter.toImplementationIndex(
                     usrIdxCol);
         } catch (NumberFormatException nfe) {
             try {
                 Boolean isVisible;
                 isVisible = (Boolean)getEventThreadQueuer().invokeAndWait(
                         "getColumnFromString", //$NON-NLS-1$
                         new IRunnable() {
                             public Object run() {
                                 return new Boolean(m_table.getHeaderVisible());
                             }
                         });  
                 if (!(isVisible.booleanValue())) {
                     throw new StepExecutionException("No Header", //$NON-NLS-1$
                             EventFactory.createActionError(
                                 TestErrorEvent.NO_HEADER));
                 }
                 
                 Integer implCol;
                 implCol = (Integer)getEventThreadQueuer().invokeAndWait(
                     "getColumnFromString", new IRunnable() { //$NON-NLS-1$
                         public Object run() throws StepExecutionException {
                             for (int i = 0; i < m_table.getColumnCount(); i++) {
                                 TableColumn tblCol = m_table.getColumn(i);
                                 if (MatchUtil.getInstance().match(
                                         tblCol.getText(), col, operator)) {
                                     return new Integer (i);
                                 }
                             }
                             return new Integer (-2);
                         }
                     });                
                 column = implCol.intValue();                
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
      * @param operator The operator used to verify
      * @return integer of String of row 
      */
     private int getRowFromString (final String row, final String operator) {
         int rowInt = -2;        
         try {
             rowInt = IndexConverter.toImplementationIndex(
                     Integer.parseInt(row));                       
             if (rowInt == -1) {
                 Boolean isVisible;
                 isVisible = (Boolean)getEventThreadQueuer().invokeAndWait(
                         "getRowFromString", //$NON-NLS-1$
                         new IRunnable() {
                             public Object run() {
                                 return new Boolean(m_table.getHeaderVisible());
                             }
                         }); 
                 if (!(isVisible.booleanValue())) {
                     throw new StepExecutionException("Header not visible", //$NON-NLS-1$
                             EventFactory.createActionError(
                                 TestErrorEvent.NO_HEADER));
                 }                
             }
         } catch (NumberFormatException nfe) {
             Integer implRow;
             implRow = (Integer)getEventThreadQueuer().invokeAndWait(
                 "getRowFromString", //$NON-NLS-1$
                 new IRunnable() {
                     public Object run() throws StepExecutionException {
                         for (int i = 0; i < m_table.getItemCount(); i++) {
                             String cellTxt = getCellText(i, 0);
                             if (MatchUtil.getInstance().match(
                                     cellTxt, row, operator)) {
                                 return new Integer(i);
                             }
                         }
                         return new Integer(-2);
                     }
                 });
             rowInt = implRow.intValue();
         }        
         return rowInt;
     }
     
     /**
      * {@inheritDoc}
      */
     public String[] getTextArrayFromComponent() {
         final String[] componentTextArray;
         Item[] itemArray = m_table.getColumns();
         componentTextArray = getTextArrayFromItemArray(itemArray);         
         return componentTextArray;
     }
 
     /**
      * {@inheritDoc}
      */
     public void gdPopupByIndexPathAtCell(int row, int column, 
             String indexPath) {
         throwUnsupportedAction();
     }
 
     /**
      * {@inheritDoc}
      */
     public void gdPopupByIndexPathAtSelectedCell(String indexPath) {
         throwUnsupportedAction();
     }
 
     /**
      * {@inheritDoc}
      */
     public void gdPopupByTextPathAtCell(int row, int column, String textPath) {
         throwUnsupportedAction();
     }
 
     /**
      * {@inheritDoc}
      */
     public void gdPopupByTextPathAtSelectedCell(String textPath) {
         throwUnsupportedAction();
     }
 
     /**
      * {@inheritDoc}
      */
     public void gdSelectCellByRowValue(int column, String cellValue,
             boolean useRegularExpression) {
         throwUnsupportedAction();
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
 
 }
