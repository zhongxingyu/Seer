 package org.jboss.workspace.client.widgets;
 
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.dom.client.Style;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.Event;
 import com.google.gwt.user.client.EventPreview;
 import com.google.gwt.user.client.ui.*;
 import static com.google.gwt.user.client.ui.RootPanel.getBodyElement;
 
 import java.util.ArrayList;
 
 public class WSGrid extends Composite {
     private FocusPanel fPanel;
     private VerticalPanel panel;
     private WSAbstractGrid titleBar;
     private WSAbstractGrid dataGrid;
 
     private ArrayList<Integer> columnWidths;
 
     private int cols;
 
     private WSCell currentFocus;
     private boolean currentFocusRowColSpan;
     private boolean _leftGrow = false;
 
     private boolean _resizeArmed = false;
     private boolean _resizing = false;
 
     private WSGrid wsGrid = this;
     private PopupPanel resizeLine = new PopupPanel() {
         @Override
         public void onBrowserEvent(Event event) {
             wsGrid.onBrowserEvent(event);
         }
     };
 
     private ArrayList<Integer> colSizes = new ArrayList<Integer>();
 
     public WSGrid() {
         this(true);
     }
 
     public WSGrid(boolean scrollable) {
         panel = new VerticalPanel();
         fPanel = new FocusPanel(panel);
 
         initWidget(fPanel);
 
         titleBar = new WSAbstractGrid(false, GridType.TITLEBAR);
 
         panel.add(titleBar);
 
         titleBar.setStylePrimaryName("WSGrid-header");
         dataGrid = new WSAbstractGrid(scrollable, GridType.EDITABLE_GRID);
 
         panel.add(dataGrid);
         panel.setCellVerticalAlignment(dataGrid, HasVerticalAlignment.ALIGN_TOP);
 
         dataGrid.setStylePrimaryName("WSGrid-datagrid");
 
         columnWidths = new ArrayList<Integer>();
 
         resizeLine.setWidth("5px");
         resizeLine.setHeight("800px");
         resizeLine.setStyleName("WSGrid-resize-line");
         resizeLine.sinkEvents(Event.MOUSEEVENTS);
 
         fPanel.addMouseListener(new MouseListener() {
             private int _fpanel_offset = -1;
             private int _startpos = 0;
 
             public void onMouseDown(Widget sender, int x, int y) {
                 if (currentFocus != null && currentFocus.isEdit()) {
                    System.out.println("edit!");
                     return;
                 }
 
                 if (_fpanel_offset == -1) {
                     _fpanel_offset = DOM.getAbsoluteLeft(sender.getElement());
                 }
 
                 if (_resizeArmed) {
                     if (!_resizing) {
                         _resizing = true;
 
                         disableTextSelection(getBodyElement(), true);
 
                         _startpos = _fpanel_offset + x;
 
                         resizeLine.show();
                         resizeLine.setPopupPosition(x + _fpanel_offset, 0);
                     }
                 }
             }
 
             public void onMouseEnter(Widget sender) {
             }
 
             public void onMouseLeave(Widget sender) {
             }
 
             public void onMouseMove(Widget sender, int x, int y) {
                 if (_resizing) {
                     if (_fpanel_offset == -1) {
                         _fpanel_offset = DOM.getAbsoluteLeft(sender.getElement());
                     }
 
                     DOM.setStyleAttribute(resizeLine.getElement(), "left", (x + _fpanel_offset) + "px");
                 }
             }
 
             public void onMouseUp(Widget sender, int x, int y) {
                 if (_resizing) {
                     cancelMove();
 
                     int selCol = (_leftGrow ? currentFocus.col - 1 : currentFocus.col);
                     if (selCol == -1) return;
 
                     int width = colSizes.get(selCol);
                     int offset = x + _fpanel_offset;
 
                     /**
                      * If we didn't move at all, don't calculate a new size.
                      */
                     if (offset - _startpos == 0) return;
 
                     width -= (_startpos -= x + _fpanel_offset);
 
                     colSizes.set(selCol, width);
 
                     titleBar.getTable().getColumnFormatter().setWidth(selCol, width + "px");
                     dataGrid.getTable().getColumnFormatter().setWidth(selCol, width + "px");
 
                     titleBar.tableIndex.get(0).get(selCol).setWidth(width + "px");
 
                     for (int cX = 0; cX < dataGrid.tableIndex.size(); cX++) {
                         dataGrid.tableIndex.get(cX).get(selCol).setWidth(width + "px");
                     }
                 }
             }
         });
 
         fPanel.addKeyboardListener(new KeyboardListener() {
             public void onKeyDown(Widget sender, char keyCode, int modifiers) {
                 if (currentFocus == null || currentFocus.edit) {
                     return;
                 }
 
                 switch (keyCode) {
                     case KeyboardListener.KEY_TAB:
                         if ((modifiers & KeyboardListener.MODIFIER_SHIFT) != 0) {
                             if (currentFocus.getCol() == 0 && currentFocus.getRow() > 0) {
                                 dataGrid.tableIndex.get(currentFocus.getRow() - 1).get(cols - 1).focus();
                             }
                             else {
                                 dataGrid.tableIndex.get(currentFocus.getRow()).get(currentFocus.getCol() - 1).focus();
                             }
                         }
                         else {
                             if (currentFocus.getCol() == cols - 1 && currentFocus.getRow() < dataGrid.tableIndex.size()) {
                                 dataGrid.tableIndex.get(currentFocus.getRow() + 1).get(0).focus();
                             }
                             else {
                                 dataGrid.tableIndex.get(currentFocus.getRow()).get(currentFocus.getCol() + 1).focus();
                             }
                         }
                         break;
                     case 63232:
                     case KeyboardListener.KEY_UP:
                         if (currentFocus.getRow() > 0)
                             dataGrid.tableIndex.get(currentFocus.getRow() - 1).get(currentFocus.getCol()).focus();
                         break;
                     case 63235:
                     case KeyboardListener.KEY_RIGHT:
                         if (currentFocus.getCol() < cols - 1) {
                             if (currentFocusRowColSpan) {
                                 titleBar.tableIndex.get(currentFocus.getRow()).get(currentFocus.getCol() + 1).focus();
                             }
                             else {
                                 dataGrid.tableIndex.get(currentFocus.getRow()).get(currentFocus.getCol() + 1).focus();
                             }
                         }
                         break;
                     case 63233:
                     case KeyboardListener.KEY_ENTER:
                     case KeyboardListener.KEY_DOWN:
                         if (currentFocus.getRow() < dataGrid.tableIndex.size())
                             dataGrid.tableIndex.get(currentFocus.getRow() + 1).get(currentFocus.getCol()).focus();
                         break;
                     case 63234:
                     case KeyboardListener.KEY_LEFT:
                         if (currentFocus.getCol() > 0) {
                             if (currentFocusRowColSpan) {
                                 titleBar.tableIndex.get(currentFocus.getRow()).get(currentFocus.getCol() - 1).focus();
                             }
                             else {
                                 dataGrid.tableIndex.get(currentFocus.getRow()).get(currentFocus.getCol() - 1).focus();
                             }
                         }
                         break;
 
                     case 63272:
                     case KeyboardListener.KEY_DELETE:
                         currentFocus.getWrappedWidget().setHTML("");
                         break;
 
                     case 32: // spacebar
                         currentFocus.edit();
                         break;
                 }
 
             }
 
             public void onKeyPress(Widget sender, char keyCode, int modifiers) {
             }
 
             public void onKeyUp(Widget sender, char keyCode, int modifiers) {
             }
         });
 
         DOM.addEventPreview(new EventPreview() {
             public boolean onEventPreview(Event event) {
                 if (currentFocus != null && currentFocus.isEdit()) {
                     return true;
                 }
 
                 switch (event.getTypeInt()) {
                     case Event.ONKEYDOWN:
                         switch (event.getKeyCode()) {
                             case KeyboardListener.KEY_TAB:
                                 if (currentFocus != null && currentFocus.isEdit()) {
                                     currentFocus.stopedit();
                                 }
                             case 63232:
                             case KeyboardListener.KEY_UP:
                             case 63235:
                             case KeyboardListener.KEY_RIGHT:
                             case 63233:
                             case KeyboardListener.KEY_ENTER:
                             case KeyboardListener.KEY_DOWN:
                             case 63234:
                             case KeyboardListener.KEY_LEFT:
                             case 63272:
                                 event.preventDefault();
                         }
                 }
                 return true;
             }
         });
 
     }
 
     public void setColumnHeader(int row, int column, String html) {
         cols = titleBar.ensureRowsAndCols(row + 1, column + 1);
         titleBar.getTableIndex().get(row).get(column).getWrappedWidget().setHTML(html);
     }
 
     public void setCell(int row, int column, String html) {
         cols = dataGrid.ensureRowsAndCols(row + 1, column + 1);
         dataGrid.getTableIndex().get(row).get(column).getWrappedWidget().setHTML(html);
     }
 
     public void setCols(int cols) {
         this.cols = cols;
     }
 
     public int getCols() {
         return cols;
     }
 
     private int checkWidth(int column) {
         if (columnWidths.size() - 1 < column) {
             for (int i = 0; i <= column; i++) {
                 columnWidths.add(150);
             }
         }
 
         return columnWidths.get(column);
     }
 
     public void updateWidth(int column, int width) {
         HTMLTable.ColumnFormatter colFormatter = titleBar.getTable().getColumnFormatter();
         colFormatter.setWidth(column, width + "px");
 
         colFormatter = dataGrid.getTable().getColumnFormatter();
         colFormatter.setWidth(column, width + "px");
 
         checkWidth(column);
         columnWidths.set(column, width);
     }
 
     public WSCell getCell(int row, int col) {
         return dataGrid.getCell(row, col);
     }
 
     private void selectColumn(int col) {
         for (ArrayList<WSCell> row : titleBar.getTableIndex()) {
             row.get(col).addStyleDependentName("hcolselect");
         }
 
         for (ArrayList<WSCell> row : dataGrid.getTableIndex()) {
             row.get(col).addStyleDependentName("colselect");
         }
     }
 
     private void blurColumn(int col) {
         for (ArrayList<WSCell> row : titleBar.getTableIndex()) {
             row.get(col).removeStyleDependentName("hcolselect");
         }
 
         for (ArrayList<WSCell> row : dataGrid.getTableIndex()) {
             row.get(col).removeStyleDependentName("colselect");
         }
     }
 
 
     public class WSAbstractGrid extends Composite {
         private ScrollPanel scrollPanel;
         private FlexTable table;
         private ArrayList<ArrayList<WSCell>> tableIndex;
 
         private GridType type;
 
         public WSAbstractGrid() {
             this(false, GridType.EDITABLE_GRID);
         }
 
         public WSAbstractGrid(GridType type) {
             this(false, type);
         }
 
         public WSAbstractGrid(boolean scrollable, GridType type) {
             this.type = type;
             table = new FlexTable();
             table.setStylePrimaryName("WSGrid");
             table.insertRow(0);
 
             if (scrollable) {
                 scrollPanel = new ScrollPanel();
                 initWidget(scrollPanel);
                 scrollPanel.setAlwaysShowScrollBars(true);
                 scrollPanel.add(table);
             }
             else {
                 initWidget(table);
             }
 
             tableIndex = new ArrayList<ArrayList<WSCell>>();
             tableIndex.add(new ArrayList<WSCell>());
         }
 
         public void addCell(int row, HTML w) {
             int currentColSize = table.getCellCount(row);
             table.addCell(row);
             table.setWidget(row, currentColSize, new WSCell(this, w, row, currentColSize));
         }
 
 
         public void addRow() {
             table.insertRow(table.getRowCount());
             for (int i = 0; i < cols; i++) {
                 addCell(table.getRowCount() - 1, new HTML());
             }
         }
 
         public int ensureRowsAndCols(int rows, int cols) {
             if (colSizes.size() < cols) {
                 for (int i = 0; i < cols; i++) {
                     colSizes.add(200);
                 }
             }
 
             if (table.getRowCount() == 0) {
                 addRow();
             }
 
             while (table.getRowCount() < rows) {
                 addRow();
             }
 
             for (int r = 0; r < table.getRowCount(); r++) {
                 if (table.getCellCount(r) < cols) {
                     int growthDelta = cols - table.getCellCount(r);
 
                     for (int c = 0; c < growthDelta; c++) {
                         table.getColumnFormatter().setWidth(c, colSizes.get(c) + "px");
 
                         addCell(r, new HTML());
                     }
 
                     assert table.getCellCount(r) == cols : "New size is wrong: " + table.getCellCount(r);
                 }
             }
 
             return cols == 0 ? 1 : cols;
         }
 
         public FlexTable getTable() {
             return table;
         }
 
         public void setHeight(String height) {
             if (scrollPanel != null) scrollPanel.setHeight(height);
         }
 
         public void setWidth(String width) {
             if (scrollPanel != null) scrollPanel.setWidth(width);
         }
 
         public int getOffsetHeight() {
             if (scrollPanel != null) return scrollPanel.getOffsetHeight();
             else return table.getOffsetHeight();
         }
 
         public int getOffsetWidth() {
             if (scrollPanel != null) return scrollPanel.getOffsetWidth();
             else return table.getOffsetWidth();
         }
 
         @Override
         protected void onAttach() {
             super.onAttach();
         }
 
         public ArrayList<ArrayList<WSCell>> getTableIndex() {
             return tableIndex;
         }
 
         public WSCell getCell(int row, int col) {
             return tableIndex.get(row).get(col);
         }
     }
 
     public class WSCell extends Composite {
         private FlowPanel panel;
 
         private HTML wrappedWidget;
         private boolean edit;
         private TextBox textBox;
 
         private int row;
         private int col;
 
         private WSAbstractGrid grid;
 
         public WSCell(WSAbstractGrid grid, HTML widget, int row, int col) {
             this.grid = grid;
             panel = new FlowPanel();
             panel.setStyleName("WSCell-panel");
 
             textBox = new TextBox();
             textBox.setStylePrimaryName("WSCell-editbox");
             textBox.setVisible(false);
 
             RootPanel.get().add(textBox);
 

             textBox.addKeyboardListener(new KeyboardListener() {
                 public void onKeyDown(Widget sender, char keyCode, int modifiers) {
                 }
 
                 public void onKeyPress(Widget sender, char keyCode, int modifiers) {
                     switch (keyCode) {
                         case KeyboardListener.KEY_TAB:
                             stopedit();
                             break;
                         case KeyboardListener.KEY_ENTER:
                             stopedit();
                             break;
                     }
                 }
 
                 public void onKeyUp(Widget sender, char keyCode, int modifiers) {
                 }
             });
 
             if (grid.tableIndex.size() - 1 < row) {
                 while (grid.tableIndex.size() - 1 < row) {
                     grid.tableIndex.add(new ArrayList<WSCell>());
                 }
             }
             ArrayList<WSCell> cols = grid.tableIndex.get(row);
 
             if (cols.size() == 0 || cols.size() - 1 < col) {
                 cols.add(this);
             }
             else {
                 cols.set(col, this);
             }
 
             this.wrappedWidget = widget;
             panel.add(wrappedWidget);
 
             this.row = row;
             this.col = col;
 
             initWidget(panel);
             setWidth(colSizes.get(col) + "px");
             setStyleName("WSCell");
 
             sinkEvents(Event.MOUSEEVENTS | Event.FOCUSEVENTS | Event.ONCLICK | Event.ONDBLCLICK);
         }
 
         /**
          * Calling this method will place the cell into edit mode.
          */
         public void edit() {
          //  panel.remove(wrappedWidget);
            textBox.setWidth(getOffsetWidth() + "px");
             textBox.setText(wrappedWidget.getHTML());
            textBox.setReadOnly(false);

         //   panel.add(textBox);
 
             textBox.setVisible(true);
             textBox.getElement().getStyle().setProperty("left", getAbsoluteLeft() + "px");
             textBox.getElement().getStyle().setProperty("top", getAbsoluteTop() + "px");
 
             edit = true;
 
             textBox.setFocus(true);

            // for IE
            textBox.setCursorPos(textBox.getText().length());
         }
 
         public void stopedit() {
             if (edit) {
                 wrappedWidget.setHTML(textBox.getText());
                 textBox.setVisible(false);
            //    RootPanel.get().remove(textBox);
             //   panel.remove(textBox);
             //   panel.add(wrappedWidget);
                edit = false;
 
                 fPanel.setFocus(true);
             }
         }
 
         public void blur() {
             stopedit();
             removeStyleDependentName("selected");
 
             if (currentFocusRowColSpan) {
                 blurColumn(col);
                 currentFocusRowColSpan = false;
             }
         }
 
         public void focus() {
             if (currentFocus != null && currentFocus != this) {
                 currentFocus.blur();
             }
             currentFocus = this;
 
             if (grid.type == GridType.TITLEBAR) {
                 currentFocusRowColSpan = true;
                 selectColumn(col);
             }
 
             addStyleDependentName("selected");
         }
 
         public int getRow() {
             return row;
         }
 
         public int getCol() {
             return col;
         }
 
         public boolean isEdit() {
             return edit;
         }
 
         public HTML getWrappedWidget() {
             return wrappedWidget;
         }
 
         public Style getStyle() {
             return wrappedWidget.getElement().getStyle();
         }
 
         @Override
         public void onBrowserEvent(Event event) {
             int leftG = getAbsoluteLeft() + 10;
             int rightG = getAbsoluteLeft() + colSizes.get(col) - 10;
 
             switch (event.getTypeInt()) {
                 case Event.ONMOUSEOVER:
                     if (!_resizing) addStyleDependentName("hover");
                     break;
                 case Event.ONMOUSEOUT:
                     if (!_resizing) removeStyleDependentName("hover");
                     break;
 
                 case Event.ONMOUSEMOVE:
                     if (!_resizing) {
                         if (event.getClientX() < leftG) {
                             addStyleDependentName("resize-left");
                             _resizeArmed = true;
                             _leftGrow = true;
                         }
                         else if (event.getClientX() > rightG) {
                             addStyleDependentName("resize-right");
                             _resizeArmed = true;
                             _leftGrow = false;
                         }
                         else {
                             removeStyleDependentName("resize-left");
                             removeStyleDependentName("resize-right");
                             _resizeArmed = false;
                         }
                     }
                     break;
                 case Event.ONMOUSEDOWN:
                     if (edit) {
                         return;
                     }
 
                     focus();
                     break;
 
                 case Event.ONFOCUS:
                     break;
 
                 case Event.ONCLICK:
                     if (edit) {
                        textBox.setFocus(true);
                        textBox.onBrowserEvent(event);
                     }
                     break;
 
                 case Event.ONDBLCLICK:
                     switch (grid.type) {
                         case EDITABLE_GRID:
                             edit();
                             break;
                         case TITLEBAR:
                             break;
                     }
                     break;
             }
         }
     }
 
     private void cancelMove() {
         resizeLine.hide();
         _resizing = false;
         _resizeArmed = false;
     }
 
     public void setHeight(String height) {
         panel.setHeight(height);
     }
 
     public void setWidth(String width) {
         panel.setWidth(width);
     }
 
     @Override
     protected void onAttach() {
         super.onAttach();
         panel.setCellHeight(titleBar, titleBar.getOffsetHeight() + "px");
         dataGrid.setHeight("100%");
     }
 
     public static void disableTextSelection(Element elem, boolean
             disable) {
         // setStyleName(elem, "my-no-selection", disable);
          disableTextSelectInternal(elem, disable);
     }
 
     private native static void disableTextSelectInternal(Element e, boolean disable)/*-{
       if (disable) {
         e.ondrag = function () { return false; };
         e.onselectstart = function () { return false; };
       } else {
         e.ondrag = null;
         e.onselectstart = null;
       }
     }-*/;
 
     private static final int EDITABLE = 1;
     private static final int TITLEGRID = 1 << 1;
 
     public enum GridType {
         EDITABLE_GRID(EDITABLE),
         TITLEBAR(TITLEGRID);
 
         private int options;
 
         GridType(int options) {
             this.options = options;
         }
 
         public boolean isEditable() {
             return (EDITABLE & options) != 0;
         }
 
         public boolean isTitleGrid() {
             return (TITLEGRID & options) != 0;
         }
     }
 }
