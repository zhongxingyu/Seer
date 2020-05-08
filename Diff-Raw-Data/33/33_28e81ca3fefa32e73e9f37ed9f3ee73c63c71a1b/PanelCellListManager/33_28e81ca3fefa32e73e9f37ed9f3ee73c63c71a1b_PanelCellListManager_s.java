 /**
  * Created On: 12-Dec-06 12:01:28 AM
  * $Id$
  */
 package com.thinkparity.ophelia.browser.application.browser.display.renderer.tab.panel;
 
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.util.List;
 
 import javax.swing.KeyStroke;
 
 import com.thinkparity.codebase.assertion.Assert;
 
 import com.thinkparity.ophelia.browser.util.localization.Localization;
 
 /**
  * @author rob_masako@shaw.ca
  * @version $Revision$
  */
 public class PanelCellListManager {
 
     /** The complete list of <code>? extends Cell</code>. */
     private List<? extends Cell> cells;
 
     /** The count JLabel */
     private final javax.swing.JLabel countJLabel;
 
     /** The current page */
     private int currentPage;
 
     /** The first JLabel */
     private final javax.swing.JLabel firstJLabel;
 
     /** Flag indicating if row 0 stays at the top, & is in the cells list */
     private final Boolean fixedFirstRow;
 
     /** The last JLabel */
     private final javax.swing.JLabel lastJLabel;
 
     /** The list model. */
     private final PanelCellListModel listModel;
 
     /** The panel localization. */
     private final Localization localization;
 
     /** The next JLabel */
     private final javax.swing.JLabel nextJLabel;
 
     /** The number of pages */
     private int numberPages;
 
     /** Number of rows per page */
     private final int perPage;
 
     /** The previous JLabel */
     private final javax.swing.JLabel previousJLabel;
 
     /** The number of visible rows. */
     private final int visibleRows;
 
     /**
      * Create a PanelCellListManager.
      * This handles the paging up and down through a Cell list
      * by clicking on next / previous style buttons.
      * 
      * @param listModel
      *            A <code>PanelCellListModel</code>.
      * @param localization
      *            A <code>Localization</code>.          
      * @param visibleRows
      *            The number of visible rows.
      * @param firstJLabel
      *            The first <code>JLabel</code>.
      * @param previousJLabel
      *            The previous <code>JLabel</code>.           
      * @param countJLabel
      *            The count <code>JLabel</code>.                             
      * @param nextJLabel
      *            The next <code>JLabel</code>.                              
      * @param lastJLabel
      *            The last <code>JLabel</code>.
      * @param fixedFirstRow
      *            Flag indicating that the first row is fixed, & in the cells list.                                                   
      */
     public PanelCellListManager(
             final PanelCellListModel listModel,
             final Localization localization,
             final int visibleRows,
             final javax.swing.JLabel firstJLabel,
             final javax.swing.JLabel previousJLabel,
             final javax.swing.JLabel countJLabel,
             final javax.swing.JLabel nextJLabel,
             final javax.swing.JLabel lastJLabel,
             final Boolean fixedFirstRow) {
         super();
         this.localization = localization;
         this.listModel = listModel;
         this.visibleRows = visibleRows;
         this.firstJLabel = firstJLabel;
         this.previousJLabel = previousJLabel;
         this.countJLabel = countJLabel;
         this.nextJLabel = nextJLabel;
         this.lastJLabel = lastJLabel;
         this.fixedFirstRow = fixedFirstRow;
         perPage = fixedFirstRow ? visibleRows-1 : visibleRows;
         initializeMouseListeners();
     }
 
     /**
      * Get the list of cells.
      * 
      * @return The <code>List</code> of <code>? extends Cell</code> cells.
      */
     public List<? extends Cell> getCells() {
         return cells;
     }
 
     /**
      * Get the page that has this cell.
      * 
      * @param cell
      *            A <code>Cell</code>.
      * @return The <code>int</code> page index with this cell, or -1 if not found.
      */
     public int getPageContainingCell(final Cell cell) {
         final int index = cells.indexOf(cell);
         if (index < 0) {
             return -1;
         } else if (fixedFirstRow) {
             return (index-1) / perPage;
         } else {
             return index / perPage;
         }
     }
 
     /**
      * Initialize with the cells list.
      * Called after the cells list is populated or changed.
      * 
      * @param cells
      *            The complete list of <code>? extends Cell</code>.
      */
     public void initialize(final List<? extends Cell> cells) {
         this.cells = cells;
         if (null == cells) {
             numberPages = 0;
         } else {                     
             final int firstItemOffset = fixedFirstRow ? 1 : 0;
             numberPages = 1 + (cells.size() - firstItemOffset - 1) / perPage;
         }
         currentPage = 0;
         updateModel();
         reloadControls();
     }
 
     /**
      * Process a key stroke.
      * 
      * @param keyStroke
      *            A <code>KeyStroke</code>. 
      */
     public void processKeyStroke(final KeyStroke keyStroke) {
         switch(keyStroke.getKeyCode()) {
         case KeyEvent.VK_PAGE_DOWN:
             if (isNextAvailable()) {
                 goNext();
                 selectTopRow();
             }
             break;
         case KeyEvent.VK_PAGE_UP:
             if (isPreviousAvailable()) {
                 goPrevious();
                 selectTopRow();
             }
             break;
         case KeyEvent.VK_HOME:
             goFirst();
             selectTopRow();
             break;
         case KeyEvent.VK_END:
             goLast();
             selectBottomRow();
             break;
         case KeyEvent.VK_DOWN:
             cursorDown();
             break;
         case KeyEvent.VK_UP:
             cursorUp();
             break;
         default:
             break;
         }
     }
 
     /**
      * Show the specified page.
      * 
      * @param page
      *            A page index <code>int</code>. 
      */
     public void showPage(final int page) {
         Assert.assertTrue("Invalid page index", (page>=0 && page<numberPages));
         if (page != currentPage) {
             currentPage = page;
             updateModel();
             reloadControls();
         }
     }
 
     /**
      * Determine the number of rows on this page.
      */
     private int countRowsThisPage() {
         if (null == cells) {
             return 0;
         } else if (currentPage+1 < numberPages) {
             return visibleRows;
         } else {
             return cells.size() - (numberPages-1)*perPage;
         }
     }
 
     /**
      * Cursor down.
      */
     private void cursorDown() {
         if (listModel.getSelectedIndex() < countRowsThisPage() - 1) {
             listModel.setSelectedIndex(listModel.getSelectedIndex() + 1);
         } else if (isNextAvailable()) {
             goNext();
             selectTopRow();
         }
     }
 
     /**
      * Cursor down.
      */
     private void cursorUp() {
         if (listModel.getSelectedIndex() > (fixedFirstRow ? 1 : 0)) {
             listModel.setSelectedIndex(listModel.getSelectedIndex() - 1);
         } else if (isPreviousAvailable()) {
             goPrevious();
             selectBottomRow();
         }
     }
 
     /**
      * Go to the first page.
      */
     private void goFirst() {
         currentPage = 0;
         updateModel();
         reloadControls();
     }
 
     /**
      * Go to the last page.
      */
     private void goLast() {
         currentPage = numberPages - 1;
         updateModel();
         reloadControls();
     }
 
     /**
      * Go to the next page.
      */
     private void goNext() {
         currentPage++;
         updateModel();
         reloadControls();
     }
 
     /**
      * Go to the previous page.
      */
     private void goPrevious() {
         currentPage--;
         updateModel();
         reloadControls();
     }
 
     /**
      * Handle a mouse press.
      * 
      * @param e
      *            A <code>MouseEvent</code>.
      */
     private void iconJLabelMousePressed(final java.awt.event.MouseEvent e) {
         if (e.getSource().equals(firstJLabel)) {
             goFirst();
         } else if (e.getSource().equals(previousJLabel)) {
             goPrevious();
             selectTopRow();
         } else if (e.getSource().equals(nextJLabel)) {
             goNext();
             selectTopRow();
         } else if (e.getSource().equals(lastJLabel)) {
             goLast();
         }
     }
 
     /**
      * Initialize mouse listeners.
      */
     private void initializeMouseListeners() {
         MouseAdapter mouseAdapter = new java.awt.event.MouseAdapter() {
             public void mousePressed(final java.awt.event.MouseEvent e) {
                 iconJLabelMousePressed(e);
             }
         };
         this.firstJLabel.addMouseListener(mouseAdapter);
         this.previousJLabel.addMouseListener(mouseAdapter);
         this.nextJLabel.addMouseListener(mouseAdapter);
         this.lastJLabel.addMouseListener(mouseAdapter);
     }
 
     /**
      * Determine if "first" is available.
      * 
      * @return true if "first" is available.
      */
     private boolean isFirstAvailable() {
         return currentPage > 1;
     }
 
     /**
      * Determine if "last" is available.
      * 
      * @return true if "last" is available.
      */
     private boolean isLastAvailable() {
         return currentPage+2 < numberPages;
     }
 
     /**
      * Determine if "next" is available.
      * 
      * @return true if "next" is available.
      */
     private boolean isNextAvailable() {
         return currentPage+1 < numberPages;
     }
 
     /**
      * Determine if "previous" is available.
      * 
      * @return true if "previous" is available.
      */
     private boolean isPreviousAvailable() {
         return currentPage > 0;
     }
 
     /**
      * Reload controls.
      */
     private void reloadControls() {
         reloadFirst();
         reloadPrevious();
         reloadCount();
         reloadNext();
         reloadLast();
     }
 
     /**
      * Reload the count.
      */
     private void reloadCount() {
         if (numberPages > 1) {
             countJLabel.setText(localization.getString("countJLabel", new Object[] {currentPage+1, numberPages}));
             countJLabel.setVisible(true);
         } else {
             countJLabel.setVisible(false);
         }
     }
 
     /**
      * Reload the "first" link.
      */
     private void reloadFirst() {
         firstJLabel.setVisible(isFirstAvailable());
     }
 
     /**
      * Reload the "last" link.
      */
     private void reloadLast() {
         lastJLabel.setVisible(isLastAvailable());
     }
 
     /**
      * Reload the "next" link.
      */
     private void reloadNext() {
         nextJLabel.setVisible(isNextAvailable());
     }
 
     /**
      * Reload the "previous" link.
      */
     private void reloadPrevious() {
         previousJLabel.setVisible(isPreviousAvailable());
     }
 
     /**
      * Select the bottom row.
      */
     private void selectBottomRow() {
         listModel.setSelectedIndex(countRowsThisPage() - 1);
     }
 
     /**
      * Select the top row.
      */
     private void selectTopRow() {
         listModel.setSelectedIndex(fixedFirstRow ? 1 : 0);
     }
 
     /**
      * Update the model.
      */
     private void updateModel() {
         final int rowsThisPage = countRowsThisPage();
         final int offset = currentPage * perPage;
 
         // Repopulate the list model
         listModel.getListModel().clear();
         if (rowsThisPage > 0) {
             if (fixedFirstRow) {
                 listModel.getListModel().addElement(cells.get(0));
                 for (int index = 1; index < rowsThisPage; index++) {
                     listModel.getListModel().addElement(cells.get(offset+index));
                 }
             } else {
                 for (int index = 0; index < rowsThisPage; index++) {
                     listModel.getListModel().addElement(cells.get(offset+index));
                 }
             }
         }
     }
 }
