 /*
  * This file (FlexiGrid.java) is part of the Echolot Project (hereinafter "Echolot").
  * Copyright (C) 2008-2010 eXXcellent Solutions GmbH.
  *
  * Version: MPL 1.1/GPL 2.0/LGPL 2.1
  *
  * The contents of this file are subject to the Mozilla Public License Version
  * 1.1 (the "License"); you may not use this file except in compliance with
  * the License. You may obtain a copy of the License at
  * http://www.mozilla.org/MPL/
  *
  * Software distributed under the License is distributed on an "AS IS" basis,
  * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
  * for the specific language governing rights and limitations under the
  * License.
  *
  * Alternatively, the contents of this file may be used under the terms of
  * either the GNU General Public License Version 2 or later (the "GPL"), or
  * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
  * in which case the provisions of the GPL or the LGPL are applicable instead
  * of those above. If you wish to allow use of your version of this file only
  * under the terms of either the GPL or the LGPL, and not to allow others to
  * use your version of this file under the terms of the MPL, indicate your
  * decision by deleting the provisions above and replace them with the notice
  * and other provisions required by the GPL or the LGPL. If you do not delete
  * the provisions above, a recipient may use your version of this file under
  * the terms of any one of the MPL, the GPL or the LGPL.
  */
 package de.exxcellent.echolot.app;
 
 import de.exxcellent.echolot.event.flexi.FlexiActivePageChangedEvent;
 import de.exxcellent.echolot.layout.FlexiCellLayoutData;
 
 import de.exxcellent.echolot.model.flexi.FlexiCell;
 import de.exxcellent.echolot.model.flexi.FlexiSortingModel;
 import de.exxcellent.echolot.model.flexi.FlexiRowSelection;
 import de.exxcellent.echolot.model.flexi.ResultsPerPageOption;
 import de.exxcellent.echolot.model.flexi.FlexiColumn;
 import de.exxcellent.echolot.model.flexi.FlexiColumnModel;
 import de.exxcellent.echolot.model.flexi.FlexiTableModel;
 import de.exxcellent.echolot.model.flexi.FlexiPage;
 import de.exxcellent.echolot.model.flexi.FlexiRow;
 import de.exxcellent.echolot.model.flexi.FlexiColumnVisibility;
 
 import de.exxcellent.echolot.event.flexi.FlexiTableModelEvent;
 import de.exxcellent.echolot.event.flexi.FlexiRPPOEvent;
 import de.exxcellent.echolot.event.flexi.FlexiColumnToggleEvent;
 import de.exxcellent.echolot.event.flexi.FlexiRowSelectionEvent;
 import de.exxcellent.echolot.event.flexi.FlexiSortingChangeEvent;
 
 import de.exxcellent.echolot.listener.flexi.FlexiActivePageChangeListener;
 import de.exxcellent.echolot.listener.flexi.FlexiTableModelListener;
 import de.exxcellent.echolot.listener.flexi.FlexiRPPOListener;
 import de.exxcellent.echolot.listener.flexi.FlexiColumnToggleListener;
 import de.exxcellent.echolot.listener.flexi.FlexiRowSelectionListener;
 import de.exxcellent.echolot.listener.flexi.FlexiSortingChangeListener;
 
 import de.exxcellent.echolot.model.flexi.FlexiColumn.FlexiColumnProperty;
 import de.exxcellent.echolot.model.flexi.FlexiColumnsUpdate;
 import de.exxcellent.echolot.util.ArrayList;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.Serializable;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Iterator;
 import nextapp.echo.app.*;
 
 import java.util.EventListener;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.TreeSet;
 
 /**
  * The {@link FlexiGrid} is a component using the <a href="http://www.flexigrid.info/">flexigrid jquery
  * plugin</a> to visualize a data grid. The flexigrid has several features such as:
  * <ul>
  * <li>Resizable columns</li>
  * <li>Resizable height and width</li>
  * <li>Sortable column headers (drag and drop)</li>
  * <li>Paging</li>
  * <li>Show/hide columns</li>
  * </ul>
  * <p/>
  * <pre>
  * +--------------------------+-+
  * | Title                    |+|
  * +-------+------------------+-+
  * | Head  | Head               |
  * +-------+--------------------+
  * | Cell  | Cell               |
  * +-------+--------------------+
  * | Cell  | Cell               |
  * +-------+--------------------+
  * | Footer                     |
  * +----------------------------+
  * </pre>
  * <p/>
  * Use the {@link FlexiGrid} like this:
  * <p/>
  * <pre>
  * final FlexiGrid FlexiGrid = new FlexiGrid();
  * final Column[] columns = new Column[]{
  *       		new Column(0, "First name", 50, true, "left"),
  *       		new Column(1, "Name", 50, false, "right"),
  *       		new Column(2, "Email", 10, true, "center")};
  * final Row[] rows = new Row[]{
  * 				new Row(0, new String[]{"Bob", "Doe","bob.doe@email.com"}),
  * 				new Row(1, new String[]{"Lisa", "Minelli", "lisa.minelli@email.com"}),
  * 				new Row(2, new String[]{"Ronald","McDonald","ronald.mcdonald@email.com"})
  * };
  * final Page page = new Page(pageIdx, 3, rows);
  * final TableModel model = new TableModel(columns, page);
  *
  * FlexiGrid.setTableModel(model);
  * </pre>
  *
  * @author Oliver Pehnke <o.pehnke@exxcellent.de>
  * @see <a href="http://www.flexigrid.info/">Flexigrid Home</a>
  * @see <a href="http://codeigniter.com/forums/viewthread/75326">CodeIgniter Flexigrid Forum</a>
  */
 public final class FlexiGrid extends Component implements Pane {
 
     private static final long serialVersionUID = 7873962246421609162L;
     private static final String CSS_REFERENCE = ResourceHelper.getFileAsString("js/flexigrid/css/flexigrid/flexigrid-template.css");
     /**
      * the sorting model to be displayed in the grid and used as parameter if the sorting changes
      */
     public static final String PROPERTY_SORTINGMODEL = "sortingModel";
     /**
      * the column model to be displayed in the grid
      */
     public static final String PROPERTY_COLUMNMODEL = "columnModel";
     /**
      * the title at the top of the grid
      */
     public static final String PROPERTY_TITLE = "title";
     /**
      * the width of the grid itself
      */
     public static final String PROPERTY_WIDTH = "width";
     /**
      * the height of the grid itself
      */
     public static final String PROPERTY_HEIGHT = "height";
     /**
      * the heightOffset is used to determine the correct maximum height if height is 'auto'.
      */
     public static final String PROPERTY_HEIGHT_OFFSET = "heightOffset";
     /**
      * if <code>true</code>, the grid will have a button to hide and show the grid at the top right
      */
     public static final String PROPERTY_SHOW_TABLE_TOGGLE_BUTTON = "showTableToggle";
     /**
      * the css as string injected in the html-head, since echo3 doesn't support any css styling by default
      */
     public static final String PROPERTY_CSS = "css";
     /**
      * if <code>true</code> the grid is resizable horizontally and vertically
      */
     public static final String PROPERTY_RESIZABLE = "resizable";
     /**
      * if <code>true</code> the client side sorting algorithm is enabled.
      */
     public static final String PROPERTY_CLIENT_SORTING = "clientSorting";
     /**
      * if the client side sorting algorithm is enabled you need to specify this delimiter value to sort numbers,e.g.
      * '1000,00' regarding to you locale.
      */
     public static final String PROPERTY_DIGITGROUP_DELIMITER = "digitGroupDelimiter";
     /**
      * To Specify a delimiter for Decimal-Values: e.g. 17,345 or 258845,66
      */
     public static final String PROPERTY_DECIMAL_DELIMITER = "decimalDelimiter";
     /**
      * <code>true</code> if the pager is shown
      */
     public static final String PROPERTY_SHOW_PAGER = "showPager";
     /**
      * <code>true</code> if the page statistics are shown in the footer
      */
     public static final String PROPERTY_SHOW_PAGE_STAT = "showPageStatistics";
     /**
      * <code>true</code> if the results per page are shown
      */
     public static final String PROPERTY_SHOW_RESULTS_PPAGE = "showResultsPerPage";        
     /**
      * the state if the even and odd rows have different colors, i.e. "striped".
      */
     public static final String PROPERTY_STRIPED = "striped";
     /**
      * the minimal width of the grid if the user resizes
      */
     public static final String PROPERTY_COLUMN_MIN_WIDTH = "minColumnWidth";
     /**
      * the minimal height of the grid if the user resizes
      */
     public static final String PROPERTY_MIN_COLUMN_HEIGHT = "minColumnHeight";
     /**
      * the minimal height of the grid if the user resizes
      */
     public static final String PROPERTY_HEADER_VISIBLE = "headerVisible";
     /**
      * <code>true</code> if no wrap is enabled
      */
     public static final String PROPERTY_NO_WRAP = "noWrap";
     
     public static final int DISABLE_SELECTION_MODE = 0;
     public static final int SINGLE_SELECTION_MODE = 1;
     public static final int MULTI_SELECTION_MODE = 2;
     public static final String PROPERTY_SELECTION_MODE = "selectionMode";
     
     // ** Active Page Properties */
     public static final String PROPERTY_ACTIVE_PAGE = "activePage";
     public static final String INPUT_ACTIVE_PAGE_CHANGED = "activePageChanged";
     public static final String TABLE_ACTIVE_PAGE_LISTENERS_CHANGED_PROPERTY = "activePageChangedListeners";
     // ** Table Row Selection Properties */
     public static final String PROPERTY_TABLE_ROW_SELECTION = "tableRowSelection";
     public static final String INPUT_TABLE_ROW_SELECTION_CHANGED = "tableRowSelectionChanged";
     public static final String TABLE_ROW_SELECTION_LISTENERS_CHANGED_PROPERTY = "tableRowSelectionListeners";
     // ** Results Per Page Option Properties */
     public static final String PROPERTY_RESULTS_PER_PAGE_OPTION = "resultsPerPageOption";
     public static final String INPUT_PROPERTY_RESULTS_PER_PAGE_OPTION_CHANGED = "resultsPerPageOptionChanged";
     public static final String RESULTS_PER_PAGE_OPTION_LISTENERS_CHANGED_PROPERTY = "resultsPerPageOptionListeners";
     /**
      * The constant used to track changes to the action listener list.
      */
     public static final String TABLE_COLUMNTOGGLE_LISTENERS_CHANGED_PROPERTY = "tableColumnToggleListeners";
     /**
      * The constant used to track changes to the action listener list.
      */
     public static final String TABLE_SORTCHANGE_LISTENERS_CHANGED_PROPERTY = "tableSortingChangeListeners";
     /**
      * The name of the action event registered in the peer when action listeners are added or removed.
      */
     public static final String INPUT_TABLE_COLUMN_TOGGLE = "tableColumnToggle";
     /**
      * The name of the action event registered in the peer when action listeners are added or removed.
      */
     public static final String INPUT_TABLE_SORTING_CHANGE = "tableSortingChange";
     public static final String PROPERTY_FLEXICOLUMNS_UPDATE = "columnsUpdate";
     public static final String PROPERTY_LINE_IMG = "LINE_IMG";
     public static final String PROPERTY_HL_IMG = "HL_IMG";
     public static final String PROPERTY_HL_COLOR = "HL_COLOR";
     public static final String PROPERTY_FHBG_IMG = "FHBG_IMG";
     public static final String PROPERTY_DDN_IMG = "DDN_IMG";
     public static final String PROPERTY_WBG_IMG = "WBG_IMG";
     public static final String PROPERTY_UUP_IMG = "UUP_IMG";
     public static final String PROPERTY_BGROUND_IMG = "BGROUND_IMG";
     public static final String PROPERTY_DOWN_IMG = "DOWN_IMG";
     public static final String PROPERTY_UP_IMG = "UP_IMG";
     public static final String PROPERTY_PREV_IMG = "PREV_IMG";
     public static final String PROPERTY_MAGNIFIER_IMG = "MAGNIFIER_IMG";
     public static final String PROPERTY_FIRST_IMG = "FIRST_IMG";
     public static final String PROPERTY_NEXT_IMG = "NEXT_IMG";
     public static final String PROPERTY_LAST_IMG = "LAST_IMG";
     public static final String PROPERTY_LOAD_IMG = "LOAD_IMG";
     public static final String PROPERTY_LOAD_BTN_IMG = "LOAD_BTN_IMG";
     private static final ImageReference LINE_IMG = new ResourceImageReference("js/flexigrid/css/flexigrid/images/line.gif");
     
     private static final ImageReference HL_IMG = new ResourceImageReference("js/flexigrid/css/flexigrid/images/hl.png");
     private static final Color HL_COLOR = new Color(252, 210, 126);
     
     private static final ImageReference FHBG_IMG =
             new ResourceImageReference("js/flexigrid/css/flexigrid/images/fhbg.gif");
     private static final ImageReference DDN_IMG =
             new ResourceImageReference("js/flexigrid/css/flexigrid/images/ddn.png");
     private static final ImageReference WBG_IMG =
             new ResourceImageReference("js/flexigrid/css/flexigrid/images/wbg.gif");
     private static final ImageReference UUP_IMG =
             new ResourceImageReference("js/flexigrid/css/flexigrid/images/uup.png");
     private static final ImageReference BGROUND_IMG =
             new ResourceImageReference("js/flexigrid/css/flexigrid/images/bg.gif");
     private static final ImageReference DOWN_IMG =
             new ResourceImageReference("js/flexigrid/css/flexigrid/images/dn.png");
     private static final ImageReference UP_IMG = new ResourceImageReference("js/flexigrid/css/flexigrid/images/up.png");
     private static final ImageReference PREV_IMG =
             new ResourceImageReference("js/flexigrid/css/flexigrid/images/prev.gif");
     private static final ImageReference MAGNIFIER_IMG =
             new ResourceImageReference("js/flexigrid/css/flexigrid/images/magnifier.png");
     private static final ImageReference FIRST_IMG =
             new ResourceImageReference("js/flexigrid/css/flexigrid/images/first.gif");
     private static final ImageReference NEXT_IMG =
             new ResourceImageReference("js/flexigrid/css/flexigrid/images/next.gif");
     private static final ImageReference LAST_IMG =
             new ResourceImageReference("js/flexigrid/css/flexigrid/images/last.gif");
     private static final ImageReference LOAD_IMG =
             new ResourceImageReference("js/flexigrid/css/flexigrid/images/load.png");
     private static final ImageReference LOAD_BTN_IMG =
             new ResourceImageReference("js/flexigrid/css/flexigrid/images/load.gif");
     
     
     /**
      * the message displayed if no items were found, e.g. "no items found"
      */
     public static final String PROPERTY_NO_ITEMS_MSG = "messageNoItems";
     /**
      * the message displayed while processing the data
      */
     public static final String PROPERTY_PROCESS_MSG = "messageProcessing";
     /**
      * the message displayed as tooltip on a column
      */
     public static final String PROPERTY_HIDE_COLUMN_MSG = "messageColumnHiding";
     /**
      * the message displayed on the button to hide the table
      */
     public static final String PROPERTY_MIN_TABLE_MSG = "messageTableHiding";
     /**
      * the message displayed as page statistics
      */
     public static final String PROPERTY_PAGE_STATISTICS_MSG = "messagePageStatistics";
 
     public static final String PROPERTY_PAGE_WORD = "wordPage";
     public static final String PROPERTY_OF_WORD = "wordOf";
     
     
     // * Bulgarian translate:
     public static final String LANG_BG = "BG";
     private static final String MSG_PAGE_STATISTICS_BG = "Показани {from} до {to} от {total} елемента.";
     private static final String MSG_PROCESSING_BG = "Обработва се, моля изчакайте ...";
     private static final String MSG_COLUMN_HIDING_BG = "Показване/Скриване на колони";
     private static final String MSG_TABLE_HIDING_BG = "Свиване/Разширяване на таблицата";
     private static final String MSG_NO_ITEMS_BG = "Няма елементи.";
     private static final String WORD_PAGE_BG = "Страница";
     private static final String WORD_OF_BG = "от";
     
     // * English translate:
     public static final String LANG_EN = "EN";
     private static final String MSG_PAGE_STATISTICS_EN = "Displaying {from} to {to} of {total} items";
     private static final String MSG_PROCESSING_EN = "Processing, please wait ...";
     private static final String MSG_COLUMN_HIDING_EN = "Hide/Show Columns";
     private static final String MSG_TABLE_HIDING_EN = "Minimize/Maximize Table";
     private static final String MSG_NO_ITEMS_EN = "No Items";
     private static final String WORD_PAGE_EN = "Page";
     private static final String WORD_OF_EN = "of";
     
     
     
     private static long nextID = 0;
         
     private final FlexiActivePageChangeListener TABLE_AP_CHANGE_LISTENER = new FlexiActivePageChangeListener() {
       
         @Override
         public void activePageChanged(FlexiActivePageChangedEvent e) {
             tableModel.onActivePageChange(e.getNewPageNo());
         }
     };    
     
     private final FlexiSortingChangeListener TABLE_SORTING_CHANGE_LISTENER = new FlexiSortingChangeListener() {
 
         @Override
         public void sortingChange(FlexiSortingChangeEvent e) {
             FlexiGrid.this.set(FlexiGrid.PROPERTY_SORTINGMODEL, e.getSortingModel(), false);
             if (FlexiGrid.this.getShowPager()) {
                 tableModel.onSort(e.getSortingModel());
                 // tableModel.onActivePageChange(activePageIdx);
             }
         }
     };
     
     private final FlexiRowSelectionListener TABLE_RS_CHANGE_LISTENER = new FlexiRowSelectionListener() {
 
         @Override
         public void rowSelection(FlexiRowSelectionEvent e) {
             FlexiGrid.this.set(FlexiGrid.PROPERTY_TABLE_ROW_SELECTION, e.getRowSelection().getAllSelectedRowsIds(), false);
         }
     };
     
     private final FlexiRPPOListener TABLE_RPPO_CHANGE_LISTENER = new FlexiRPPOListener() {
 
         @Override
         public void resultsPerPageChange(FlexiRPPOEvent e) {
             ResultsPerPageOption currentRPPO = getResultsPerPageOption();
             setResultsPerPageOption(new ResultsPerPageOption(e.getNewIntialOption(), currentRPPO.getPageOption()));
             tableModel.onActivePageChange(1);
         }
     };
     
     private final String renderId;
     private FlexiTableModel tableModel;
     private int activePageIdx = -1;
     private final FlexiColumnPropertyChangeListener FLEXICOLUMN_PROPERTY_CHANGE_LISTENER = new FlexiColumnPropertyChangeListener();
     private final FlexiColumnsUpdate columnsUpdate = new FlexiColumnsUpdate();
 
     /**
      * Default constructor for a {@link FlexiGrid}. Sets the several default values.
      */
     public FlexiGrid() {
         this(LANG_EN);
     }
 
     public FlexiGrid(String lang) {
         super();
 
         this.renderId = "FG" + nextID++;
         setRenderId(renderId);
 
         setCSS(CSS_REFERENCE);
         setHeight(null);
         setWidth(null);
         setTitle("");
         setShowTableToggleButton(Boolean.TRUE);
         setShowPager(Boolean.TRUE);
         setShowResultsPerPage(Boolean.TRUE);
         
         String psm = MSG_PAGE_STATISTICS_EN;
         String pm  = MSG_PROCESSING_EN;
         String chm = MSG_COLUMN_HIDING_EN;
         String thm = MSG_TABLE_HIDING_EN;
         String nim = MSG_NO_ITEMS_EN;
         String pw  = WORD_PAGE_EN;
         String ow  = WORD_OF_EN;
         
         if (lang.equalsIgnoreCase(LANG_BG)) {
             psm = MSG_PAGE_STATISTICS_BG;
             pm  = MSG_PROCESSING_BG;
             chm = MSG_COLUMN_HIDING_BG;
             thm = MSG_TABLE_HIDING_BG;
             nim = MSG_NO_ITEMS_BG;
             pw  = WORD_PAGE_BG;
             ow  = WORD_OF_BG;
         }
         
         setMessagePageStatistics(psm);
         setMessageProcessing(pm);
         setMessageColumnHiding(chm);
         setMessageTableHiding(thm);
         setMessageNoItems(nim);
         set(PROPERTY_PAGE_WORD, pw);
         set(PROPERTY_OF_WORD, ow);
         
         setStriped(Boolean.TRUE);
         setMinimalColumnWidth(30);
         setMinimalColumnHeight(80);
         setNoWrap(Boolean.TRUE);
         setSelectionMode(FlexiGrid.MULTI_SELECTION_MODE);
         set(PROPERTY_FLEXICOLUMNS_UPDATE, new FlexiColumnsUpdate());
         setHeaderVisible(true);
         set(PROPERTY_TABLE_ROW_SELECTION, new Integer[0], false);
 
         /* images */
         set(PROPERTY_LINE_IMG, LINE_IMG);
         set(PROPERTY_HL_IMG, HL_IMG);
         set(PROPERTY_HL_COLOR, HL_COLOR);
         set(PROPERTY_FHBG_IMG, FHBG_IMG);
         set(PROPERTY_DDN_IMG, DDN_IMG);
         set(PROPERTY_WBG_IMG, WBG_IMG);
         set(PROPERTY_UUP_IMG, UUP_IMG);
         set(PROPERTY_BGROUND_IMG, BGROUND_IMG);
         set(PROPERTY_DOWN_IMG, DOWN_IMG);
         set(PROPERTY_UP_IMG, UP_IMG);
         set(PROPERTY_PREV_IMG, PREV_IMG);
         set(PROPERTY_MAGNIFIER_IMG, MAGNIFIER_IMG);
         set(PROPERTY_FIRST_IMG, FIRST_IMG);
         set(PROPERTY_NEXT_IMG, NEXT_IMG);
         set(PROPERTY_LAST_IMG, LAST_IMG);
         set(PROPERTY_LOAD_IMG, LOAD_IMG);
         set(PROPERTY_LOAD_BTN_IMG, LOAD_BTN_IMG);
 
         addActivePageChangeListener(TABLE_AP_CHANGE_LISTENER);
         addTableSortingChangeListener(TABLE_SORTING_CHANGE_LISTENER);
         addTableRowSelectionListener(TABLE_RS_CHANGE_LISTENER);
         addResultsPerPageOptionChangeListener(TABLE_RPPO_CHANGE_LISTENER);
     }
 
     /**
      * Sets a generic property of the <code>Component</code>.
      * The value will be stored in this <code>Component</code>'s local style.
      * 
      * @param propertyName the name of the property
      * @param newValue the value of the property
      * @firePropertyChangeEvent flag for firePropertyChangeEvent
      * @see #get(java.lang.String)
      */
     private void set(String propertyName, Object newValue, boolean firePropertyChangeEvent) {
         MutableStyle ms = (MutableStyle) getLocalStyle();
         Object oldValue = ms.get(propertyName);
         ms.set(propertyName, newValue);
         if (firePropertyChangeEvent) {
             firePropertyChange(propertyName, oldValue, newValue);
         }
     }
 
     /**
      * Set current selected rows' ids.
      * 
      * @param rowsIds
      */
     public void selectRows(Integer... rowsIds) {
         FlexiPage currentPage = (FlexiPage) get(PROPERTY_ACTIVE_PAGE);
         ArrayList<Integer> currentRowsIds = new ArrayList<Integer>(Arrays.asList(currentPage.getRowsIds()));
 
         ArrayList<Integer> validRowsIds = new ArrayList<Integer>();
         for (Integer id : rowsIds) {
             if (currentRowsIds.contains(id)) {
                 validRowsIds.add(id);
             }
         }
 
         set(PROPERTY_TABLE_ROW_SELECTION, validRowsIds.toArray(new Integer[validRowsIds.size()]), true);
     }
 
     /**
      * Unselect rows from current selection.
      * 
      * @param rowsIds 
      */
     public void unselectRows(Integer... rowsIds) {
         HashSet<Integer> selected = new HashSet(Arrays.asList(getSelectedRowsIds()));
         for (Integer id : rowsIds) {
             selected.remove(id);
         }
         selectRows(selected.toArray(new Integer[selected.size()]));
     }
 
     /**
      * Set the tableModel
      *
      * @param tableModel the FlexiTableModel for FlexiGrid
      */
     public void setFlexiTableModel(FlexiTableModel tableModel) {
         this.tableModel = tableModel;
 
         // wenn das flexTableModel null ist, dann wars das hier auch schon
         if (tableModel == null) {
             setColumnModel(new FlexiColumnModel(new FlexiColumn[0]));
             return;
         }
 
         tableModel.addFlexTableModelListener(FLEX_TABLE_MODEL_LISTENER);
 
         // setzen der RowsPerPage Option
         // -----------------------------
         if (getResultsPerPageOption() == null) {
             ResultsPerPageOption rppo = new ResultsPerPageOption();
             if (tableModel.getDefaultResultsPerPage() == FlexiTableModel.SHOW_ALL_ROWS_ON_ONE_PAGE) {                
                 rppo.setInitialOption(-1);
                 rppo.setPageOption(new int[]{ tableModel.getRowCount() });
             } else {
                 rppo.setInitialOption(tableModel.getDefaultResultsPerPage());
                 rppo.setPageOption(tableModel.getDefaultResultsPerPageOption());
             }
             setResultsPerPageOption(rppo);
         }
         
         // data in model is ready adn set active page
         // ------------------------------------------
         tableModel.onActivePageChange(1);
     }
 
     /**
      * Returns the FlexTableModel
      *
      * @return the flexTableModel
      */
     public FlexiTableModel getFlexiTableModel() {
         return tableModel;
     }
 
     public int getTotalPageCount() {
         int result = 1;
         final int rowsPerPageCount = this.getRowsPerPageCount();
         if (rowsPerPageCount == FlexiTableModel.SHOW_ALL_ROWS_ON_ONE_PAGE) {
             return result;
         }
 
         final int totalRowCount = tableModel.getRowCount();
         if ((totalRowCount % rowsPerPageCount) == 0) {
             result = totalRowCount / rowsPerPageCount;
         } else {
             result = totalRowCount / rowsPerPageCount + 1;
         }
 
         return result == 0 ? 1 : result;
     }
 
     public void setLastActivePage() {
         setActivePage(getTotalPageCount());
     }
 
     public void reloadCurrentPage() {
         setActivePage(activePageIdx);
     }
 
     /**
      * Set the current activePage of the TableModel
      * The needed Rows for this page will be extracted and transported to client to be visible in flexigrid.
      *
      * @param page
      */
     public void setActivePage(int page) {
         if (page == -1) {
             return;
         }
         activePageIdx = page;
         FlexiPage requestedPage = null;
         if (tableModel == null) {
             requestedPage = new FlexiPage(1, 0, new FlexiRow[0]);
         } else {
             requestedPage = makePage(page);
         }
         setActivePage(requestedPage);
     }
 
     /**
      * Returns the options of number of shown results per page.
      *
      * @return the options of number of shown results per page
      */
     public ResultsPerPageOption getResultsPerPageOption() {
         return (ResultsPerPageOption) get(PROPERTY_RESULTS_PER_PAGE_OPTION);
     }
 
     public int getRowsPerPageCount() {
         return getResultsPerPageOption().getInitialOption();
     }
 
     /**
      * Sets the options of number of shown results per page, e.g. "[10,15,20,25]".
      * Don't set this manually if you don't know what you are doing... - will be done by flexigrid for you
      *
      * @param newValue the initial number shown of results per page
      */
     public void setResultsPerPageOption(ResultsPerPageOption newValue) {
         set(PROPERTY_RESULTS_PER_PAGE_OPTION, newValue);
     }
 
     public void setResultsPerPage(int count) {
         ResultsPerPageOption rppo = getResultsPerPageOption();
         if (rppo != null) {
             setResultsPerPageOption(new ResultsPerPageOption(count, rppo.getPageOption()));
         }
     }
     
     /**
      * Returns <code>true</code> if the pager is shown.
      *
      * @return <code>true</code> if the pager is shown
      */
     public boolean getShowPager() {
         return (Boolean) get(PROPERTY_SHOW_PAGER);
     }
 
     /**
      * Sets the visibility of the pager.
      *
      * @param newValue <code>true</code> the pager is visible
      */
     public void setShowPager(boolean newValue) {
         set(PROPERTY_SHOW_PAGER, newValue);
     }
     
     public boolean isHeaderVisible() {
         return (Boolean) get(PROPERTY_HEADER_VISIBLE); 
     }
     
     public void setHeaderVisible(boolean newValue) {
         set(PROPERTY_HEADER_VISIBLE, newValue);
     }
 
     /**
      * Clear current selected rows' ids.
      * <br />
      * Called when active page is changed!
      */
     public void clearSelection() {
         set(PROPERTY_TABLE_ROW_SELECTION, new Integer[0], true);
     }
 
     /**
      * Returns <code>true</code> if the results per page are shown.
      *
      * @return <code>true</code> if results per page are shown
      */
     public Boolean getShowResultsPerPage() {
         return (Boolean) get(PROPERTY_SHOW_RESULTS_PPAGE);
     }
 
     /**
      * Sets the visibility of the results per page.
      *
      * @param newValue <code>true</code> the results per page are visible
      */
     public void setShowResultsPerPage(Boolean newValue) {
         set(PROPERTY_SHOW_RESULTS_PPAGE, newValue);
     }
 
     /**
      * Returns <code>true</code> if the page statistics are shown.
      *
      * @return <code>true</code> if the page statistics are shown
      */
     public boolean getShowPageStatistics() {
         return Boolean.getBoolean((String) get(PROPERTY_SHOW_PAGE_STAT));
     }
 
     /**
      * Sets the visibility of the page statistics.
      *
      * @param newValue <code>true</code> the page statistics are visible
      */
     public void setShowPageStatistics(boolean newValue) {
         set(PROPERTY_SHOW_PAGE_STAT, newValue);
     }
 
     /**
      * Returns the cascading style sheet for this component.
      *
      * @return the cascading style sheet
      */
     public String getCSS() {
         return (String) get(PROPERTY_CSS);
     }
 
     /**
      * Sets the cascading style sheet for this component.
      *
      * @param newValue the new css
      */
     public void setCSS(String newValue) {
         set(PROPERTY_CSS, newValue);
     }
 
     public int getSelectionMode() {
         return (Integer) get(PROPERTY_SELECTION_MODE);
     }
 
     public void setSelectionMode(int selectionMode) {
         if (selectionMode < DISABLE_SELECTION_MODE || selectionMode > MULTI_SELECTION_MODE) {
             throw new Error("FlexiGrid Error: Try to set invalid selection mode !!!");
         }
         set(PROPERTY_SELECTION_MODE, selectionMode);
     }
     
     public void setSelectionBackground(Color color) {
         set(PROPERTY_HL_COLOR, color);
     }
     
     public Color getSelectionBackground() {
       return (Color) get(PROPERTY_HL_COLOR);
     }
 
     public void setSelectionBackgroundImage(ResourceImageReference imageReference) {
         set(PROPERTY_HL_IMG, imageReference);
     }
     
     public ResourceImageReference getSelectionBackgroundImage() {
         return (ResourceImageReference) get(PROPERTY_HL_IMG);
     }
     
     /**
      * Returns <code>true</code> if no wrap is enabled.
      *
      * @return <code>true</code> if no wrap is enabled
      */
     public Boolean getNoWrap() {
         return (Boolean) get(PROPERTY_NO_WRAP);
     }
 
     /**
      * Sets the state if no wrap is enabled.
      *
      * @param newValue the state if no wrap is enabled
      */
     public void setNoWrap(Boolean newValue) {
         set(PROPERTY_NO_WRAP, newValue);
     }
 
     /**
      * Returns the minimal of the columns in the grid if the user resizes.
      *
      * @return the amount of minimal height used by a column if the user resizes
      */
     public int getMinimalColumnHeight() {
         return Integer.parseInt((String) get(PROPERTY_MIN_COLUMN_HEIGHT));
     }
 
     /**
      * Sets the minimal height of the columns in the grid if the user resizes.
      *
      * @param newValue the minimal height of the grid if the user resizes
      */
     public void setMinimalColumnHeight(int newValue) {
         set(PROPERTY_MIN_COLUMN_HEIGHT, newValue);
     }
 
     /**
      * Returns the minimal width of the columns in the grid if the user resizes.
      *
      * @return the amount of minimal width used by each column if the user resizes
      */
     public int getMinimalColumnWidth() {
         return Integer.parseInt((String) get(PROPERTY_COLUMN_MIN_WIDTH));
     }
 
     /**
      * Sets the minimal width of the columns in the grid if the user resizes.
      *
      * @param newValue the minimal width of the columns in the grid if the user resizes
      */
     public void setMinimalColumnWidth(int newValue) {
         set(PROPERTY_COLUMN_MIN_WIDTH, newValue);
     }
 
     /**
      * Returns the state if the even and odd rows have different colors, i.e. "striped".
      *
      * @return <code>true</code> if the even and odd rows have different colors
      */
     public Boolean getStriped() {
         return (Boolean) get(PROPERTY_STRIPED);
     }
 
     /**
      * Sets the state if the even and odd rows have different colors.
      *
      * @param newValue the state if the even and odd rows have different colors
      */
     public void setStriped(Boolean newValue) {
         set(PROPERTY_STRIPED, newValue);
     }
 
     /**
      * Returns the message displayed as page statistics.
      *
      * @return the message displayed as page statistics
      */
     public String getMessagePageStatistics() {
         return (String) get(PROPERTY_PAGE_STATISTICS_MSG);
     }
 
     /**
      * Sets the message displayed as page statistics. Use may use tokens to be replaced, such as
      * "Displaying {from} to {to} of {total} items".
      *
      * @param newValue the message displayed as page statistics
      */
     public void setMessagePageStatistics(String newValue) {
         set(PROPERTY_PAGE_STATISTICS_MSG, newValue);
     }
 
     /**
      * Returns the message displayed while processing the data.
      *
      * @return the message while processing the data
      */
     public String getMessageProcessing() {
         return (String) get(PROPERTY_PROCESS_MSG);
     }
 
     /**
      * Sets the message displayed while processing the data.
      *
      * @param newValue the message while processing the data
      */
     public void setMessageProcessing(String newValue) {
         set(PROPERTY_PROCESS_MSG, newValue);
     }
 
     /**
      * Returns the message displayed as tooltip on the hide button on the table headline.
      *
      * @return the tooltip on the hide button on the table headline
      */
     public String getMessageTableHiding() {
         return (String) get(PROPERTY_MIN_TABLE_MSG);
     }
 
     /**
      * Sets the message displayed as tooltip on the hide button on the table headline.
      *
      * @param newValue the message as tooltip on the hide button on the table headline
      */
     public void setMessageTableHiding(String newValue) {
         set(PROPERTY_MIN_TABLE_MSG, newValue);
     }
 
     /**
      * Returns the message displayed as tooltip on a column.
      *
      * @return the tooltip on a column
      */
     public String getMessageColumnHiding() {
         return (String) get(PROPERTY_HIDE_COLUMN_MSG);
     }
 
     /**
      * Sets the message displayed as tooltip on a column.
      *
      * @param newValue the message displayed as tooltip on a column
      */
     public void setMessageColumnHiding(String newValue) {
         set(PROPERTY_HIDE_COLUMN_MSG, newValue);
     }
 
     /**
      * Returns the message displayed if no items were found.
      *
      * @return the message displayed if no items were found
      */
     public String getMessageNoItems() {
         return (String) get(PROPERTY_NO_ITEMS_MSG);
     }
         
     /**
      * Sets the message displayed if no items were found, e.g. "no items found".
      *
      * @param newValue the message displayed if no items were found
      */
     public void setMessageNoItems(String newValue) {
         set(PROPERTY_NO_ITEMS_MSG, newValue);
     }
 
     /**
      * Returns current selected rows' ids.
      *
      * @return array of selected rows' ids
      */
     public Integer[] getSelectedRowsIds() {
         return (Integer[]) get(PROPERTY_TABLE_ROW_SELECTION);
     }
 
     /**
      * Set the current active Page
      * !! --------------------------------------------- !!
      * !! Be careful by calling this manually
      * !! It's better to use {@link #setActivePage(int)}
      * !! --------------------------------------------- !!
      *
      * @param newPage the active page to be set to current
      */
     public void setActivePage(final FlexiPage newPage) {
         clearSelection();
         set(PROPERTY_ACTIVE_PAGE, newPage);
     }
 
     /**
      * Return the current active page
      *
      * @return the current active page
      */
     public FlexiPage getActivePage() {
         return (FlexiPage) get(PROPERTY_ACTIVE_PAGE);
     }
 
     /**
      * Returns the current activePage Index
      *
      * @return the index of the current active page
      */
     public int getActivePageIdx() {
         return activePageIdx;
     }
 
     /**
      * Return the table model.
      *
      * @return The table model object or {@code null} if no such exists.
      */
     public FlexiColumnModel getColumnModel() {
         return (FlexiColumnModel) get(PROPERTY_COLUMNMODEL);
     }
 
     /**
      * Set the value of the {@link #PROPERTY_COLUMNMODEL} property.
      *
      * @param newTableModel The table model to be represented in this component.
      */
     public void setColumnModel(final FlexiColumnModel newColumnModel) {
         set(PROPERTY_COLUMNMODEL, newColumnModel);
     }
 
     /**
      * Set the value of the {@link #PROPERTY_COLUMNMODEL} property.
      *
      * @param columnModel The table model to be represented in this component.
      */
     public void setFlexiColumnModel(final FlexiColumnModel columnModel) {
         this.columnModel = columnModel;
         set(PROPERTY_COLUMNMODEL, columnModel);
     }
 
     /**
      * Set the value of the {@link #PROPERTY_SORTINGMODEL} property.
      *
      * @param newSortingModel The sorting model to be represented in this component.
      */
     public void setSortingModel(final FlexiSortingModel newSortingModel) {
         set(PROPERTY_SORTINGMODEL, newSortingModel);
     }
 
     /**
      * Return the sorting model of the grid.
      *
      * @return The sorting model object or {@code null} if no such exists.
      */
     public FlexiSortingModel getSortingModel() {
         return (FlexiSortingModel) get(PROPERTY_SORTINGMODEL);
     }
 
     /**
      * Return the table title above the table.
      *
      * @return The title is the name above the table.
      */
     public String getTitle() {
         return (String) get(PROPERTY_TITLE);
     }
 
     /**
      * Set the title above the table, see {@link #PROPERTY_TITLE} property.
      *
      * @param title The table title to be represented in this component.
      */
     public void setTitle(final String title) {
         set(PROPERTY_TITLE, title);
     }
 
     /**
      * Return the table height of the table.
      *
      * @return The height of the table.
      */
     public int getHeight() {
         return Integer.parseInt((String) get(PROPERTY_HEIGHT));
     }
 
     /**
      * Set the height of the table, see {@link #PROPERTY_HEIGHT} property. If the value is -1 its set to "auto" height.
      *
      * @param height The table height of this component.
      */
     public void setHeight(final Extent height) {
         set(PROPERTY_HEIGHT, height);
     }
 
     /**
      * Return the heightOffset is used to determine the correct maximum height if height is 'auto'.
      *
      * @return The height of the table.
      */
     public int getHeightOffset() {
         return Integer.parseInt((String) get(PROPERTY_HEIGHT_OFFSET));
     }
 
     /**
      * Set the heightOffset is used to determine the correct maximum height if height is 'auto'.
      *
      * @param height the heightOffset is used to determine the correct maximum height if height is 'auto'.
      */
     public void setHeightOffset(final Extent height) {
         set(PROPERTY_HEIGHT_OFFSET, height);
     }
 
     /**
      * Return the table width of the table.
      *
      * @return The width of the table.
      */
     public int getWidth() {
         return Integer.parseInt((String) get(PROPERTY_WIDTH));
     }
 
     /**
      * Set the width of the table, see {@link #PROPERTY_WIDTH} property. Can be a value like "400" interpreted as 400px
      * or "auto". If the value is -1 its set to "auto" height.
      *
      * @param width The table width of this component.
      */
     public void setWidth(final Extent width) {
         set(PROPERTY_WIDTH, width);
     }
 
     /**
      * Shows the button to hide and show the the table grid.
      *
      * @param showTableToggleButton <code>true</code> the button will be shown
      */
     public void setShowTableToggleButton(boolean showTableToggleButton) {
         set(PROPERTY_SHOW_TABLE_TOGGLE_BUTTON, showTableToggleButton);
     }
 
     /**
      * Return <code>true</code> if the button to hide and show the the table grid is visible.
      *
      * @return <code>true</code> if the button to hide and show the the table grid is visible.
      */
     public boolean getShowTableToggleButton() {
         return (Boolean) get(PROPERTY_SHOW_TABLE_TOGGLE_BUTTON);
     }
 
     /**
      * If <code>true</code> the whole grid is resizable vertically and horizontally.
      *
      * @param resizable <code>true</code> the grid is resizable
      */
     public void setResizable(boolean resizable) {
         set(PROPERTY_RESIZABLE, resizable);
     }
 
     /**
      * Return <code>true</code> if the grid is resizable vertically and horizontally.
      *
      * @return <code>true</code> if the grid is resizable.
      */
     public boolean getResizable() {
         return (Boolean) get(PROPERTY_RESIZABLE);
     }
 
     /**
      * !!!!!
      * Does no longer work with lazy loading
      * ! Feel free to fix this !
      * !!!!!
      * <p/>
      * If <code>true</code> the client side sorting algorithm is enabled. The client side sorting reduces the bandwidth
      * and supports multicolumn sorting for alpha numeric values. You can also use your own sorting method server side
      * by implementing an {@link FlexiSortingChangeListener} to sort and setting the updated {@link TableModel}
      * containing all sorted data. However this clientSorting value should then be set to <code>false</code> to avoid
      * double sorting.
      *
      * @param clientSorting <code>true</code> activates the client side sorting.
      * @deprecated
      */
     public void setClientSorting(boolean clientSorting) {
         set(PROPERTY_CLIENT_SORTING, clientSorting);
     }
 
     /**
      * Return <code>true</code> if the client side sorting algorithm is enabled.
      *
      * @return <code>true</code> if the debug mode is enabled.
      * @deprecated
      */
     public boolean getClientSorting() {
         return (Boolean) get(PROPERTY_CLIENT_SORTING);
     }
 
     /**
      * If the client side sorting algorithm is enabled you need to specify this delimiter value to sort numbers,e.g.
      * '1000,00' regarding to you locale.
      *
      * @param digitGroupDelimiter the delimiter, such as '.' or ',' or whatever.
      */
     public void setDigitGroupDelimiter(String digitGroupDelimiter) {
         set(PROPERTY_DIGITGROUP_DELIMITER, digitGroupDelimiter);
     }
 
     /**
      * Return the delimiter used for sorting numbers.
      *
      * @return the delimiter used for sorting numbers.
      */
     public String getDigitGroupDelimiter() {
         return (String) get(PROPERTY_DIGITGROUP_DELIMITER);
     }
 
     /**
      * If the client side sorting algorithm is enabled you need to specify this delimiter value to sort numbers with decimals,e.g.
      * '1000,345' or '23,66235' regarding to you locale.
      *
      * @param decimalDelimiter the delimiter, such as '.' or ',' or whatever.
      */
     public void setDecimalDelimiter(String decimalDelimiter) {
         set(PROPERTY_DECIMAL_DELIMITER, decimalDelimiter);
     }
 
     /**
      * Return the decimal-delimiter used for sorting numbers.
      *
      * @return the decimal-delimiter used for sorting numbers.
      */
     public String getDecimalDelimiter() {
         return (String) get(PROPERTY_DECIMAL_DELIMITER);
     }
 
     /**
      * Adds a {@link FlexiRowSelectionListener}.
      *
      * @param l will be informed if a row is selected
      */
     public void addTableRowSelectionListener(FlexiRowSelectionListener l) {
         getEventListenerList().addListener(FlexiRowSelectionListener.class, l);
         firePropertyChange(TABLE_ROW_SELECTION_LISTENERS_CHANGED_PROPERTY, null, l);
     }
 
     /**
      * Removes a {@link FlexiRowSelectionListener}
      *
      * @param l will be removed from listener list.
      */
     public void removeTableRowSelectionListener(FlexiRowSelectionListener l) {
         getEventListenerList().removeListener(FlexiRowSelectionListener.class, l);
         firePropertyChange(TABLE_ROW_SELECTION_LISTENERS_CHANGED_PROPERTY, l, null);
     }
 
     /**
      * Adds a {@link FlexiColumnToggleListener}.
      *
      * @param l will be informed if the state of a column changes from visible to invisible
      */
     public void addTableColumnToggleListener(FlexiColumnToggleListener l) {
         getEventListenerList().addListener(FlexiColumnToggleListener.class, l);
         firePropertyChange(TABLE_COLUMNTOGGLE_LISTENERS_CHANGED_PROPERTY, null, l);
     }
 
     /**
      * Removes a {@link FlexiColumnToggleListener}
      *
      * @param l will be removed from listener list.
      */
     public void removeTableColumnToggleListener(FlexiColumnToggleListener l) {
         getEventListenerList().removeListener(FlexiColumnToggleListener.class, l);
         firePropertyChange(TABLE_COLUMNTOGGLE_LISTENERS_CHANGED_PROPERTY, l, null);
     }
 
     /**
      * Adds a {@link FlexiSortingChangeListener}.
      *
      * @param l will be informed if the sorting changes for columns
      */
     public void addTableSortingChangeListener(FlexiSortingChangeListener l) {
         getEventListenerList().addListener(FlexiSortingChangeListener.class, l);
         firePropertyChange(TABLE_SORTCHANGE_LISTENERS_CHANGED_PROPERTY, null, l);
     }
 
     /**
      * Removes a {@link FlexiSortingChangeListener}
      *
      * @param l will be removed from listener list.
      */
     public void removeTableSortingChangeListener(FlexiSortingChangeListener l) {
         getEventListenerList().removeListener(FlexiSortingChangeListener.class, l);
         firePropertyChange(TABLE_SORTCHANGE_LISTENERS_CHANGED_PROPERTY, l, null);
     }
 
     /**
      * Adds a {@link FlexiRPPOListener}.
      *
      * @param l will be informed if the results per page options change
      */
     public void addResultsPerPageOptionChangeListener(FlexiRPPOListener l) {
         getEventListenerList().addListener(FlexiRPPOListener.class, l);
         firePropertyChange(RESULTS_PER_PAGE_OPTION_LISTENERS_CHANGED_PROPERTY, null, l);
     }
 
     /**
      * Removes a {@link FlexiRPPOListener}
      *
      * @param l will be removed from listener list.
      */
     public void removeResultsPerPageOptionChangeListener(FlexiRPPOListener l) {
         getEventListenerList().removeListener(FlexiRPPOListener.class, l);
         firePropertyChange(RESULTS_PER_PAGE_OPTION_LISTENERS_CHANGED_PROPERTY, l, null);
     }
     
     /**
      * Adds a {@link FlexiActivePageChangeListener}.
      *
      * @param l will be informed if active page changed
      */
     public void addActivePageChangeListener(FlexiActivePageChangeListener l) {
         getEventListenerList().addListener(FlexiActivePageChangeListener.class, l);
         firePropertyChange(TABLE_ACTIVE_PAGE_LISTENERS_CHANGED_PROPERTY, null, l);
     }
 
     /**
      * Removes a {@link FlexiActivePageChangeListener}
      *
      * @param l will be removed from listener list.
      */
     public void removeActivePageChangeListener(FlexiActivePageChangeListener l) {
         getEventListenerList().removeListener(FlexiActivePageChangeListener.class, l);
         firePropertyChange(TABLE_ACTIVE_PAGE_LISTENERS_CHANGED_PROPERTY, l, null);
     }
 
     /**
      * Processes a user request to select a row with the given parameter.
      *
      * @param rowSelection the object containing information about the selection
      */
     public void userTableRowSelection(FlexiRowSelection rowSelection) {
         fireTableRowSelection(rowSelection);
     }
 
     /**
      * Processes a user request to toggle the visibility state of a column.
      *
      * @param columnVisibility the object containing information about the toggle
      */
     public void userTableColumnToggle(FlexiColumnVisibility columnVisibility) {
         fireTableColumnToggle(columnVisibility);
     }
 
     /**
      * Processes a user request to change the sorting of columns.
      *
      * @param sortingModel the object containing information about the sorting
      */
     public void userTableSortingChange(FlexiSortingModel sortingModel) {
         fireTableSortingChange(sortingModel);
     }
 
     /**
      * Processes a user request to change the results per page.
      *
      * @param pageNo the new rows per page count
      */
     public void userResultsPerPageOptionChange(Integer initialOption) {
         fireResultsPerPageOptionChange(initialOption);
     }
     
     /**
      * Processes a user request to change the current active page.
      *
      * @param pageNo the new active page no
      */
     public void userActivePageChange(Integer pageNo) {
         fireActivePageChange(pageNo);
     }
 
     /**
      * Notifies <code>FlexiRowSelectionListener</code>s that the user has selected a row.
      */
     protected void fireTableRowSelection(FlexiRowSelection rowSelection) {
         if (!hasEventListenerList()) {
             return;
         }
         EventListener[] listeners = getEventListenerList().getListeners(FlexiRowSelectionListener.class);
         if (listeners.length == 0) {
             return;
         }
         FlexiRowSelectionEvent e = new FlexiRowSelectionEvent(this, rowSelection);
         for (int i = 0; i < listeners.length; ++i) {
             ((FlexiRowSelectionListener) listeners[i]).rowSelection(e);
         }
     }
 
     /**
      * Notifies <code>FlexiRowSelectionListener</code>s that the user has selected a row.
      */
     protected void fireTableColumnToggle(FlexiColumnVisibility columnVisibility) {
         if (!hasEventListenerList()) {
             return;
         }
         EventListener[] listeners = getEventListenerList().getListeners(FlexiColumnToggleListener.class);
         if (listeners.length == 0) {
             return;
         }
         FlexiColumnToggleEvent e = new FlexiColumnToggleEvent(this, columnVisibility);
         for (int i = 0; i < listeners.length; ++i) {
             ((FlexiColumnToggleListener) listeners[i]).columnToggle(e);
         }
     }
 
     /**
      * Notifies <code>FlexiSortingChangeListener</code>s that the user has changed the sorting.
      */
     protected void fireTableSortingChange(FlexiSortingModel sortingModel) {
         if (!hasEventListenerList()) {
             return;
         }
         EventListener[] listeners = getEventListenerList().getListeners(FlexiSortingChangeListener.class);
         if (listeners.length == 0) {
             return;
         }
         FlexiSortingChangeEvent e = new FlexiSortingChangeEvent(this, sortingModel);
         for (int i = 0; i < listeners.length; ++i) {
             ((FlexiSortingChangeListener) listeners[i]).sortingChange(e);
         }
     }
 
     /**
      * Notifies <code>FlexiRPPOListener</code>s that the user has changed the results per page option.
      */
     protected void fireResultsPerPageOptionChange(Integer initialOption) {
         if (!hasEventListenerList()) {
             return;
         }
         EventListener[] listeners = getEventListenerList().getListeners(FlexiRPPOListener.class);
         if (listeners.length == 0) {
             return;
         }
         FlexiRPPOEvent e = new FlexiRPPOEvent(this, initialOption);
         for (int i = 0; i < listeners.length; ++i) {
             ((FlexiRPPOListener) listeners[i]).resultsPerPageChange(e);
         }
     }
     
     /**
      * Notifies <code>FlexiActivePageChangeListener</code>s that the user has changed the current active page.
      */
     protected void fireActivePageChange(Integer pageNo) {
         if (!hasEventListenerList()) {
             return;
         }
         EventListener[] listeners = getEventListenerList().getListeners(FlexiActivePageChangeListener.class);
         if (listeners.length == 0) {
             return;
         }
         FlexiActivePageChangedEvent e = new FlexiActivePageChangedEvent(this, pageNo);
         for (int i = 0; i < listeners.length; ++i) {
             ((FlexiActivePageChangeListener) listeners[i]).activePageChanged(e);
         }
     }
 
     /**
      * Determines the any <code>FlexiRowSelectionListener</code>s are registered.
      *
      * @return true if any <code>FlexiRowSelectionListener</code>s are registered
      */
     public boolean hasTableRowSelectionListeners() {
         if (!hasEventListenerList()) {
             return false;
         }
         return getEventListenerList().getListenerCount(FlexiRowSelectionListener.class) > 0;
     }
 
     /**
      * Determines the any <code>FlexiColumnToggleListener</code>s are registered.
      *
      * @return true if any <code>FlexiColumnToggleListener</code>s are registered
      */
     public boolean hasTableColumnToggleListeners() {
         if (!hasEventListenerList()) {
             return false;
         }
         return getEventListenerList().getListenerCount(FlexiColumnToggleListener.class) > 0;
     }
 
     /**
      * Determines the any <code>FlexiSortingChangeListener</code>s are registered.
      *
      * @return true if any <code>FlexiSortingChangeListener</code>s are registered
      */
     public boolean hasTableSortingChangeListeners() {
         if (!hasEventListenerList()) {
             return false;
         }
         return getEventListenerList().getListenerCount(FlexiSortingChangeListener.class) > 0;
     }
 
     /**
      * Determines the any <code>FlexiRPPOListener</code>s are registered.
      *
      * @return true if any <code>FlexiRPPOListener</code>s are registered
      */
     public boolean hasResultPerPageOptionChangeListeners() {
         if (!hasEventListenerList()) {
             return false;
         }
         return getEventListenerList().getListenerCount(FlexiRPPOListener.class) > 0;
     }
     
     /**
      * Determines the any <code>FlexiRPPOListener</code>s are registered.
      *
      * @return true if any <code>FlexiRPPOListener</code>s are registered
      */
     public boolean hasActivePageChangedListeners() {
         if (!hasEventListenerList()) {
             return false;
         }
         return getEventListenerList().getListenerCount(FlexiActivePageChangeListener.class) > 0;
     }
         
     /**
      * The content of each rendered row.
      * <ul>
      * <li><b>Key:</b> FlexiRow's ID</li>
      * <li><b>Value:</b> All FlexiCells in FlexiRow</li>
      * </ul>
      */
     private final HashMap<Integer, ArrayList<FlexiCell>> row2cells = new HashMap<Integer, ArrayList<FlexiCell>>();
     /**
      * Contains current rendered components.
      * <ul>
      * <li><b>Key:</b>Echo.Component</li>
      * <li><b>Value:</b>Echo.Component's owner (FlexiCell)</li>
      * </ul>
      */
     private final HashMap<Component, FlexiCell> component2cell = new HashMap<Component, FlexiCell>();
     /**
      * The position of each column.
      * <br />
      * [4, 2, 1, 3] - FlexiColumns IDs
      */
     private final ArrayList<Integer> columnPositions = new ArrayList<Integer>();
     /**
      * Contains the positions of components (unusable components) that can be overwritten.
      */
     private final TreeSet<Integer> markedForReplace = new TreeSet<Integer>();
     /**
      * Contains width for each column.
      * <ul>
      * <li><b>Key:</b>FlexiColumn's ID</li>
      * <li><b>Value:</b>Width (Extent)</li>
      * </ul>
      */
     private final HashMap<Integer, Extent> maxW = new HashMap<Integer, Extent>();
     /**
      * Contains height for each row.
      * <ul>
      * <li><b>Key:</b>FlexiRow's ID</li>
      * <li><b>Value:</b>Height (Extent)</li>
      * </ul>
      */
     private final HashMap<Integer, Extent> maxH = new HashMap<Integer, Extent>();
     private int currentPageFirstRow;
     private int currentPageLastRow;
     private FlexiColumnModel columnModel;
     private FlexiColumn counterColumn = null;
     private boolean invalidColumnModel = true;
     private final FCLayoutDataChangeListener FC_LAYOUTDATA_CHANGE_LISTENER = new FCLayoutDataChangeListener();
     private final FCComponentChangeListener FC_COMPONENT_CHANGE_LISTENER = new FCComponentChangeListener();
 
     private FlexiPage makePage(int page) {
         int maxCompIndex = -1;
 
         // if column model is marked as invalid
         // ------------------------------------
         if (invalidColumnModel) {
             validateColumnModel();
         }
 
         // cerrent cells
         // -------------
         ArrayList<FlexiCell> currentCells = new ArrayList<FlexiCell>();
         for (ArrayList<FlexiCell> cells : row2cells.values()) {
             currentCells.addAll(cells);
         }
 
         // new cells ... add header's cells
         // --------------------------------
         ArrayList<FlexiCell> newCells = new ArrayList<FlexiCell>();
         for (FlexiColumn c : columnModel.getColumns()) {
             FlexiCell cc = c.getCell();
             int ci = Integer.parseInt(cc.getVisibleComponent().getId());
             maxCompIndex = ci > maxCompIndex ? ci : maxCompIndex;
             newCells.add(cc);
         }
         
 //        // if sorting model exists then ask tablemodel for sorting ...
 //        // -----------------------------------------------------------
 //        FlexiSortingModel sortingModel = getSortingModel();
 //        if (sortingModel != null) {
 //            tableModel.onSort(sortingModel);
 //        }
         
         final int totalRows = tableModel.getRowCount();
         final boolean showPager = getShowPager();
         if (!showPager) {
             ResultsPerPageOption rppo = new ResultsPerPageOption(-1, new int[] { totalRows });
             setResultsPerPageOption(rppo);
         }
 
         // if all Rows should be displayed on one page ...
         // -----------------------------------------------
         final int rowsPerPageCount = this.getRowsPerPageCount();
         if (rowsPerPageCount == FlexiTableModel.SHOW_ALL_ROWS_ON_ONE_PAGE) {
             // ... we set rowStart to zero and rowEnd to maximum
             // -------------------------------------------------
             currentPageFirstRow = 0;
             currentPageLastRow = totalRows;
         } else {
             // ... otherwise if there is some paging active we have to calculate the range of rows to display
             // ----------------------------------------------------------------------------------------------
             currentPageFirstRow = (page - 1) * rowsPerPageCount;
             currentPageLastRow = currentPageFirstRow + rowsPerPageCount;            
             if (currentPageLastRow > totalRows) {
                 currentPageLastRow = totalRows;
             }
         }
 
         // The number of rows for this page
         // --------------------------------
         final int amountOfRows = currentPageLastRow - currentPageFirstRow;
         final int columnsOffset = counterColumn == null ? 0 : 1;
         final int amountOfColumns = columnModel.getColumns().length;
         FlexiRow[] rows = new FlexiRow[amountOfRows];
 
         int rowCounter = 0;
         for (int currentRow = currentPageFirstRow; currentRow < currentPageLastRow; currentRow++) {
             FlexiCell[] rowCells = new FlexiCell[amountOfColumns];
 
             int rowID = tableModel.getRowAt(currentRow).getId();
             Extent rowMaxHeight = new Extent(0);
             for (int currentColumn = 0; currentColumn < amountOfColumns; currentColumn++) {
 
                 // process FlexiColumn
                 // -------------------
                 FlexiColumn column = columnModel.getColumnAt(currentColumn);
                 int colID = column.getId();
                 FlexiCell columnCell = column.getCell();
 
                 // process FlexiCell
                 // -----------------
                 FlexiCell cell = null;
                 if (colID == -1) {
                     cell = new FlexiCell(rowID, colID, Integer.toString(currentRow + 1));
                 } else {
                     cell = tableModel.getCellAt(currentRow, currentColumn - columnsOffset);
                 }
 
                 cell.equalizeLayoutDataTo(columnCell);
 
                 // check for max width
                 // -------------------
                 Extent currentMaxWidth = maxW.get(colID);
                 Extent cellWidth = cell.getWidth();
                 if (cellWidth.compareTo(currentMaxWidth) >= 1) {
                     maxW.put(colID, cellWidth);
                 }
 
                 // check for max height (current row)
                 // ... may have rows with different heights
                 // ----------------------------------------
                 Extent cellHeight = cell.getHeight();
                 if (cellHeight.compareTo(rowMaxHeight) >= 1) {
                     rowMaxHeight = cellHeight;
                 }
 
                 // add current cell to FlexiGrid
                 // -----------------------------------------
                 newCells.add(cell);
                 rowCells[currentColumn] = cell;
             }
 
             // set the maximum height for each cell of current row
             // ---------------------------------------------------
             maxH.put(rowID, rowMaxHeight);
             for (FlexiCell cell : rowCells) {
                 if (cell.setHeight(rowMaxHeight)) {
                     break;
                 }
             }
 
             FlexiRow row = new FlexiRow(rowID, rowCells);
             rows[rowCounter] = row;
             rowCounter++;
         }
 
         // set the maximum width for each column cell
         // ------------------------------------------
         for (int c = 0; c < amountOfColumns; c++) {
             FlexiColumn column = columnModel.getColumnAt(c);
             Extent maxWidth = maxW.get(column.getId());
             if (column.getCell().setWidth(maxWidth)) {
                 continue;
             }
 
             for (int r = 0; r < rows.length; r++) {
                 if (rows[r].getCellAt(c).setWidth(maxWidth)) {
                     continue;
                 }
             }
         }
 
         Iterator<FlexiCell> it = null;
 
         // mark unuseble cells for replace
         // -------------------------------
         ArrayList<FlexiCell> toBeMarked = (ArrayList<FlexiCell>) currentCells.clone();
         toBeMarked.removeAll(newCells);
         it = toBeMarked.iterator();
         while (it.hasNext()) {
             unbindCell(it.next());
         }
 
         // mark new cells for add
         // ----------------------
         ArrayList<FlexiCell> toBeAdded = (ArrayList<FlexiCell>) newCells.clone();
         toBeAdded.removeAll(currentCells);
         attemptToRevive(toBeAdded);
         it = toBeAdded.iterator();
         while (it.hasNext()) {
             addCell(it.next());
         }
 
         // get max component index ...
         // ... remove all components greater than the index.
         for (int r = 0; r < rows.length; r++) {
             FlexiCell[] cells = rows[r].getCells();
             for (int c = 0; c < cells.length; c++) {
                 int ci = Integer.parseInt(cells[c].getVisibleComponent().getId());
                 maxCompIndex = ci > maxCompIndex ? ci : maxCompIndex;
             }
         }
 
         removeUnusedComponents(maxCompIndex);
 
         // return new page
         // ---------------
         return new FlexiPage(page, tableModel.getRowCount(), rows);
     }
     
     private void attemptToRevive(ArrayList<FlexiCell> newCells) {
         HashSet<Component> marked = new HashSet<Component>();
         for (Integer index : markedForReplace) {
             marked.add(getComponent(index));
         }
         
         for (int f = 0; f < newCells.size(); f++) {
             FlexiCell fc = newCells.get(f);
             Component c = fc.getVisibleComponent();
             if (marked.contains(c)) {
                 Integer ID = Integer.valueOf(c.getId());
                 markedForReplace.remove(ID);
                 newCells.remove(fc);
                 bindCell(fc);
                 f--;
             }
         }
     }
 
     private void addCell(FlexiCell cell) { 
         Component component = cell.getVisibleComponent();
         Integer replaceIndex = markedForReplace.pollFirst();
         int index = replaceIndex != null ? replaceIndex : -1;
         internalAdd(cell, component, index);
         bindCell(cell);
     }
 
     private void bindCell(FlexiCell cell) {
         cell.addLayoutDataChangeListener(FC_LAYOUTDATA_CHANGE_LISTENER);
         cell.addComponentChangeListener(FC_COMPONENT_CHANGE_LISTENER);
 
         int rowID = cell.getRowId();
         int colID = cell.getColId();
         ArrayList<FlexiCell> rowCells = row2cells.get(rowID);
         if (rowCells == null) {
             rowCells = new ArrayList<FlexiCell>();
             row2cells.put(rowID, rowCells);
         }
 
         int index = columnPositions.indexOf(colID);        
         if (index >= rowCells.size()) {
             rowCells.setSize(index + 1);
         }
         if (rowCells.get(index) == null) { rowCells.set(index, cell); }
         else { rowCells.add(index, cell); }            
     }
 
     private void unbindCell(FlexiCell cell) {
        markedForReplace.add(Integer.valueOf(cell.getVisibleComponent().getId()));
        
         cell.removeComponentChangeListener(FC_COMPONENT_CHANGE_LISTENER);
         cell.removeLayoutDataChangeListener(FC_LAYOUTDATA_CHANGE_LISTENER);
 
         final int rowID = cell.getRowId();
         ArrayList<FlexiCell> rowCells = row2cells.get(rowID);
         if (rowCells != null && !rowCells.isEmpty()) {
             rowCells.remove(cell);
             if (rowCells.isEmpty()) {
                 row2cells.remove(rowID);
             }
         } else {
             row2cells.remove(rowID);
         }
     }
 
     private void unbindColumn(FlexiColumn column) {
         column.removeComponentChangeListener(FLEXICOLUMN_PROPERTY_CHANGE_LISTENER);
         unbindCell(column.getCell());
     }
 
     private void removeUnusedComponents(int maxIndex) {
         TreeSet<Integer> unusedIndeces = (TreeSet<Integer>) markedForReplace.tailSet(maxIndex, false);
         while (!unusedIndeces.isEmpty()) {
             Integer idx = unusedIndeces.pollLast();
             internalRemove(idx);
         }
     }
 
     private String genRenderId(int componentIndex) {
         return renderId + "FC" + componentIndex;
     }
 
     /**
      * Create column models by tableModel
      */
     private void validateColumnModel() {
         // header max height
         Extent maxHeight = new Extent(0);
 
         // clear column positions
         columnPositions.clear();
 
         // columns for new model
         ArrayList<FlexiColumn> newColumns = new ArrayList<FlexiColumn>();
 
         // current columns
         HashSet<FlexiColumn> currentColumns = columnModel == null ? new HashSet<FlexiColumn>() : new HashSet<FlexiColumn>(Arrays.asList(columnModel.getColumns()));
 
         if (counterColumn != null) {
             FlexiCell c = counterColumn.getCell();
             int colID = counterColumn.getId();
             columnPositions.add(colID);
 
             if (!currentColumns.contains(counterColumn)) {
                 addCell(c);
                 counterColumn.addPropertyChangeListener(FLEXICOLUMN_PROPERTY_CHANGE_LISTENER);
             }
             newColumns.add(counterColumn);
 
             maxHeight = c.getHeight();
             maxW.put(colID, c.getWidth());
         }
 
         int columnCount = tableModel.getColumnCount();
         for (int c = 0; c < columnCount; c++) {
             FlexiColumn currentColumn = tableModel.getColumnAt(c);
             FlexiCell columnCell = currentColumn.getCell();
 
             int colID = currentColumn.getId();
             columnPositions.add(colID);
 
             if (!currentColumns.contains(currentColumn)) {
                 // add column cell to FlexiGrid
                 // ---------------------------- 
                 addCell(columnCell);
 
                 // add listener for component change event
                 // ---------------------------------------
                 currentColumn.addPropertyChangeListener(FLEXICOLUMN_PROPERTY_CHANGE_LISTENER);
             }
             newColumns.add(currentColumn);
 
             // check for max height
             // --------------------
             Extent columnHeight = columnCell.getHeight();
             if (columnHeight.compareTo(maxHeight) > 0) {
                 maxHeight = columnHeight;
             }
 
             // set default columns widths
             // --------------------------
             maxW.put(currentColumn.getId(), columnCell.getWidth());
         }
 
         // set max height for each column from model
         // -----------------------------------------
         maxH.put(-1, maxHeight);
         for (FlexiColumn col : newColumns) {
             if (col.getCell().setHeight(maxHeight)) {
                 break;
             }
         }
 
         // set new column model
         // --------------------
         setFlexiColumnModel(new FlexiColumnModel(newColumns.toArray(new FlexiColumn[newColumns.size()])));
 
         // mark column model as valid
         // --------------------------
         invalidColumnModel = false;
     }
 
     public void setCounter(Label label) {
         if (label != null && counterColumn == null) {
             // create default column ...
             // --------------------------
             counterColumn = new FlexiColumn(-1, label, null, false, false, true);
             counterColumn.getCell().setWidth(new Extent(45));
         } else if (label == null && counterColumn != null) {
             unbindColumn(counterColumn);
             counterColumn = null;
         } else {
             return;
         }
 
         // mark column model as invalid
         // ----------------------------
         invalidColumnModel = true;
         setActivePage(activePageIdx);
     }
 
     public FlexiColumn getCounterColumn() {
         return counterColumn;
     }
 
     private final class FCLayoutDataChangeListener implements PropertyChangeListener, Serializable {
 
         private boolean inProcess = false;
 
         @Override
         public void propertyChange(PropertyChangeEvent pce) {          
             if (this.inProcess || activePageIdx == -1) {
                 return;
             } else {
                 this.inProcess = true;
 
                 FlexiCell source = (FlexiCell) pce.getSource();
                 final int rowID = source.getRowId();
                 final int colID = source.getColId();                
                 
                 // prev layoutData ...
                 FlexiCellLayoutData oldLayoutData = (FlexiCellLayoutData) pce.getOldValue();
                 
                 Extent oldWidth = oldLayoutData.getWidth();
                 if (oldWidth == null) oldWidth = new Extent(0);
                 Extent oldHeight = oldLayoutData.getHeight();
                 if (oldHeight == null) oldHeight = new Extent(0);
 
                 // new layoutData ...
                 FlexiCellLayoutData newLayoutData = (FlexiCellLayoutData) pce.getNewValue();
                 Extent newWidth = newLayoutData.getWidth();
                 if (newWidth == null) {
                     newWidth = maxW.get(colID);
                     oldWidth = newWidth;
                     source.setWidth(newWidth);
                 }
                 Extent newHeight = newLayoutData.getHeight();
                 if (newHeight == null) {
                     newHeight = maxH.get(rowID);
                     oldHeight = newHeight;
                     source.setHeight(newHeight);
                 }
 
                 // check for changes in height or width
                 // ... ignore all the other changes ...
                 // ------------------------------------
                 int wResult = newWidth.compareTo(oldWidth);
                 int hResult = newHeight.compareTo(oldHeight);
                 if (wResult == 0 && hResult == 0) {
                     this.inProcess = false;
                     return;
                 }
 
                 ArrayList<FlexiCell> sourceRowCells = row2cells.get(rowID);
 
                 if (hResult != 0) {
                     for (FlexiCell cell : sourceRowCells) {
                         cell.setHeight(newHeight);
                     }
                     maxH.put(rowID, newHeight);
                 }
 
                 if (wResult != 0) {
                     int position = sourceRowCells.indexOf(source);
                     Collection<ArrayList<FlexiCell>> rowsCells = row2cells.values();
                     for (ArrayList<FlexiCell> rowCells : rowsCells) {
                         rowCells.get(position).setWidth(newWidth);
                     }
                     maxW.put(colID, newWidth);
                 }
 
                 // finishing ...
                 this.inProcess = false;
             }
         }
     }
 
     private final class FCComponentChangeListener implements PropertyChangeListener, Serializable {
 
         @Override
         public void propertyChange(PropertyChangeEvent pce) {
             Component oldComponent = (Component) pce.getOldValue();            
             Integer componentIdx = Integer.valueOf(oldComponent.getId());
             FlexiGrid.this.internalAdd((FlexiCell) pce.getSource(), (Component) pce.getNewValue(), componentIdx);
         }
     }
 
     private final class FlexiColumnPropertyChangeListener implements PropertyChangeListener, Serializable {
 
         @Override
         public void propertyChange(PropertyChangeEvent pce) {
             FlexiColumn fc = (FlexiColumn) pce.getSource();
             FlexiColumnProperty newProp = (FlexiColumnProperty) pce.getNewValue();
 
             columnsUpdate.add(fc.getId(), newProp.getKey(), newProp.getValue());
             set(PROPERTY_FLEXICOLUMNS_UPDATE, columnsUpdate);
             firePropertyChange(PROPERTY_FLEXICOLUMNS_UPDATE, null, columnsUpdate);
         }
     }
     private final FlexiTableModelListener FLEX_TABLE_MODEL_LISTENER = new FlexiTableModelListener() {
 
         @Override
         public void flexTableChanged(FlexiTableModelEvent event) {
             final int type = event.getType();
             switch (type) {
                 // table has new rows
                 // ------------------
                 case FlexiTableModelEvent.INSERT_ROWS:
                     tableModel.onActivePageChange(getTotalPageCount());
                     break;
                 // table has deleted rows
                 // ----------------------
                 case FlexiTableModelEvent.DELETE_ROWS:
                     final int lastPageIdx = FlexiGrid.this.getTotalPageCount();
                     int currentPageIdx = activePageIdx;
                     if (currentPageIdx > lastPageIdx) {
                         currentPageIdx = lastPageIdx;
                     }
                     tableModel.onActivePageChange(currentPageIdx);
                     break;
                 case FlexiTableModelEvent.INSERT_COLUMNS:
                     // nothing at now ...
                     break;
                 case FlexiTableModelEvent.DELETE_COLUMNS:
                     // nothing at now ...
                     break;
                 default:
                     throw new Error("Unsupported FlexTableModelEvent type!");
             }
         }
     };
 
     @Override
     public void add(Component cmpnt, int i) throws IllegalChildException {
         externalAdd(cmpnt, i);
     }
 
     private void internalAdd(FlexiCell cell, Component component, int index) {
         Component parent = component.getParent();        
         if (parent != null) {
             parent.remove(component);
         }
         
         String ID = null;
         String rID = null;
         if (index == -1) {
             int cc = getComponentCount();
             ID = Integer.toString(cc);
             rID = genRenderId(cc);
         } else {
             internalRemove(index);
             ID = Integer.toString(index);
             rID = genRenderId(index);
         }
         
         component.setId(ID);
         component.setRenderId(rID);
         super.add(component, index);
         component2cell.put(component, cell);
     }
     
     private void externalAdd(Component cmpnt, int i) {
         throw new Error("FlexiGrid Error: External add components is not allowed !!!");
     }
     
     @Override
     public void remove(Component cmpnt) {
         externalRemove(cmpnt);
     }
 
     private void internalRemove(int idx) {
         Component c = getComponent(idx);
         component2cell.remove(c);
         super.remove(c);
     }
 
     private void externalRemove(Component component) {
         FlexiCell cell = component2cell.get(component);
         if (cell != null) {
             cell.invalidate();
             component.setRenderId(null);
             component.setId(null);
         } else {
             throw new Error("FlexiGrid Error: External remove components is not allowed !!!");
         }
     }
 }
