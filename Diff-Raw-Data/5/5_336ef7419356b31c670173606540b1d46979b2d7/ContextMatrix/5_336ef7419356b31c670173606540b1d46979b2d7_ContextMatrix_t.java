 package fcatools.conexpng.gui;
 
 import javax.swing.*;
 import javax.swing.event.TableModelEvent;
 import javax.swing.table.*;
 import java.awt.*;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.HashMap;
 import java.util.Map;
 
 import static fcatools.conexpng.gui.Util.clamp;
 import static javax.swing.KeyStroke.getKeyStroke;
 
 /**
  * ContextMatrix is simply a customisation of JTable in order to make it look & behave more
  * like a spreadsheet editor resp. ConExp's context editor. The code is intricate, a bit
  * ugly and uses quite a few snippets from various sources from the internet (see below).
  * That is just because of the way JTable is designed - it is not meant to be too flexible.
  *
  * PRECONDITION: Expects a model that extends 'AbstractTableModel' and implements 'Reordarable'! TODO: Overwrite setTableModel to assert for that
  *
  * Resources:
  * http://explodingpixels.wordpress.com/2009/05/18/creating-a-better-jtable/
  * http://stackoverflow.com/questions/14416188/jtable-how-to-get-selected-cells
  * http://stackoverflow.com/questions/5044222/how-can-i-determine-which-cell-in-a-jtable-was-selected?rq=1
  * http://tonyobryan.com/index.php?article=57
  * http://www.jroller.com/santhosh/entry/make_jtable_resiable_better_than
  */
 class ContextMatrix extends JTable {
 
     private static final long serialVersionUID = -7474568014425724962L;
 
     private static final Color HEADER_COLOR = new Color(245, 245, 250);
     private static final Color EVEN_ROW_COLOR = new Color(252, 252, 252);
     private static final Color ODD_ROW_COLOR = new Color(255, 255, 255);
     private static final Color TABLE_GRID_COLOR = new Color(0xd9d9d9);
 
     public ContextMatrix(TableModel dm, Map<Integer, Integer> columnWidths) {
         super(dm);
         this.columnWidths = columnWidths;
         setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
         setTableHeader(null);
         setOpaque(false);
         setGridColor(TABLE_GRID_COLOR);
         setIntercellSpacing(new Dimension(0, 0));
         setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
         setCellSelectionEnabled(true);
         setShowGrid(false);
         clearKeyBindings();
         createResizingInteractions();
         createDraggingInteractions();
     }
 
     // Create our custom viewport into which our custom JTable will be inserted
     public JScrollPane createStripedJScrollPane(Color bg) {
         JScrollPane scrollPane =  new JScrollPane(this);
         scrollPane.setViewport(new StripedViewport(this, bg));
         scrollPane.getViewport().setView(this);
         scrollPane.setBorder(BorderFactory.createEmptyBorder());
         return scrollPane;
     }
 
     // For correct rendering of table after data changes
     @Override
     public void tableChanged(TableModelEvent e) {
         super.tableChanged(e);
         alignCells();
         restoreColumnWidths();
         makeHeaderCellsEditable();
     }
 
     // For removing standard JTable keybindings that would not fit this customised JTable
     private void clearKeyBindings() {
         // After testings thoroughly it seems to be impossible to simply clear all keybindings
         // even if Swing's API suggests that it should be possible. So we need to rely on a hack
         // for removing keybindings
         int[] is = {JComponent.WHEN_IN_FOCUSED_WINDOW, JComponent.WHEN_FOCUSED, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT};
         InputMap im = getInputMap();
         for (int i = 0; i <= is.length; i++) {
             im.put(getKeyStroke(KeyEvent.VK_ENTER, 0), "none");
             im.put(getKeyStroke(KeyEvent.VK_UP, 0), "none");
             im.put(getKeyStroke(KeyEvent.VK_LEFT, 0), "none");
             im.put(getKeyStroke(KeyEvent.VK_RIGHT, 0), "none");
             im.put(getKeyStroke(KeyEvent.VK_DOWN, 0), "none");
             im.put(getKeyStroke(KeyEvent.VK_UP, KeyEvent.SHIFT_MASK), "none");
             im.put(getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.SHIFT_MASK), "none");
             im.put(getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.SHIFT_MASK), "none");
             im.put(getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.SHIFT_MASK), "none");
             if (i == is.length) break;
             //noinspection MagicConstant
             im = getInputMap(is[i]);
         }
     }
 
     // For centering text inside cells
     private void alignCells() {
         DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
         centerRenderer.setHorizontalAlignment(JLabel.CENTER);
         for (int i = 0; i < getColumnCount(); i++) {
             getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
         }
     }
 
 
     ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     // Selecting
     ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 
     // For preventing a selection to disappear after an operation like "invert"
     private int lastSelectedRowsStartIndex;
     private int lastSelectedRowsEndIndex;
     private int lastSelectedColumnsStartIndex;
     public int lastSelectedColumnsEndIndex;
 
     /* For allowing a programmatical cell selection (i.e. not only through mouse/keyboard events) */
     public void selectCell(int row, int column) {
         setRowSelectionInterval(row, row);
         setColumnSelectionInterval(column, column);
     }
 
     /* Programmatically select a row */
     public void selectRow(int row) {
         setRowSelectionInterval(row, row);
         setColumnSelectionInterval(1, this.getColumnCount() - 1);
     }
 
     /* Programmatically select a column */
     public void selectColumn(int column) {
         setColumnSelectionInterval(column, column);
         setRowSelectionInterval(1, this.getRowCount() - 1);
     }
 
     /* For preventing a selection to disappear after an operation like "invert" */
     public void saveSelectedInterval() {
         lastSelectedRowsStartIndex = getSelectedRow();
         lastSelectedRowsEndIndex = getSelectedRowCount()-1 + lastSelectedRowsStartIndex;
         lastSelectedColumnsStartIndex = getSelectedColumn();
         lastSelectedColumnsEndIndex = getSelectedColumnCount()-1 + lastSelectedColumnsStartIndex;
     }
 
     /* For preventing a selection to disappear after an operation like "invert" */
     public void restoreSelectedInterval() {
         if (getRowCount() <= 1 || getColumnCount() <= 1) return;
         if (  (lastSelectedColumnsEndIndex <= 0 && lastSelectedColumnsStartIndex <= 0)
                 || (lastSelectedRowsEndIndex    <= 0 && lastSelectedRowsStartIndex    <= 0)) return;
         lastSelectedRowsStartIndex = clamp(lastSelectedRowsStartIndex, 1, getRowCount()-1);
         lastSelectedRowsEndIndex = clamp(lastSelectedRowsEndIndex, 1, getRowCount()-1);
         lastSelectedColumnsStartIndex = clamp(lastSelectedColumnsStartIndex, 1, getColumnCount() - 1);
         lastSelectedColumnsEndIndex = clamp(lastSelectedColumnsEndIndex, 1, getColumnCount()-1);
         setRowSelectionInterval(lastSelectedRowsStartIndex, lastSelectedRowsEndIndex);
         setColumnSelectionInterval(lastSelectedColumnsStartIndex, lastSelectedColumnsEndIndex);
     }
 
     public boolean wasColumnSelected(int j) {
         return (lastSelectedColumnsEndIndex == j && lastSelectedColumnsStartIndex == j);
     }
 
     public boolean wasRowSelected(int i) {
         return (lastSelectedRowsEndIndex == i && lastSelectedRowsStartIndex == i);
     }
 
     /* Overridden as header cells should *not* be selected when selecting all cells */
     @Override
     public void selectAll() {
         setRowSelectionInterval(1, getRowCount()-1);
         setColumnSelectionInterval(1, getColumnCount()-1);
     }
 
     /* Overridden as header cells should *not* be selectable through mouse clicks / keyboard events */
     @Override
     public boolean isCellSelected(int i, int j) {
         return i != 0 && j != 0 && super.isCellSelected(i, j);
     }
 
     /* For correct painting of table when selecting something */
     @Override
     public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
         Component component = super.prepareRenderer(renderer, row, column);
         if (component instanceof JComponent) {
             ((JComponent)component).setOpaque(isCellSelected(row, column));
         }
         return component;
     }
 
     public int getLastSelectedRowsStartIndex() {
         return lastSelectedRowsStartIndex;
     }
 
     public int getLastSelectedRowsEndIndex() {
         return lastSelectedRowsEndIndex;
     }
 
     public int getLastSelectedColumnsStartIndex() {
         return lastSelectedColumnsStartIndex;
     }
 
     public int getLastSelectedColumnsEndIndex() {
         return lastSelectedColumnsEndIndex;
     }
 
 
     ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     // Renaming
     ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 
     // For being able to rename headers
     private void makeHeaderCellsEditable() {
         for (int i = 0; i < getColumnCount(); i++) {
             getColumnModel().getColumn(i).setCellEditor(editor);
         }
     }
 
     public void renameColumnHeader(int i) {
         editCellAt(0, i);
         requestFocus();
         ContextCellEditor ed = (ContextCellEditor) editor;
         ed.getTextField().requestFocus();
         ed.getTextField().selectAll();
     }
 
     public void renameRowHeader(int i) {
         editCellAt(i, 0);
         requestFocus();
         ContextCellEditor ed = (ContextCellEditor) editor;
         ed.getTextField().requestFocus();
         ed.getTextField().selectAll();
     }
 
     // For enabling renaming of headers
     TableCellEditor editor = new ContextCellEditor(new JTextField());
 
     // Custom cell editor. Needed for renaming of objects/attributes
     @SuppressWarnings("serial")
     public static class ContextCellEditor extends DefaultCellEditor {
 
         int lastRow = 0;
         int lastColumn = 0;
         String lastName;
         ContextMatrixModel model = null;
         JTextField textField = null;
 
         public ContextCellEditor(JTextField textField) {
             super(textField);
             this.textField = textField;
             // Prevent double clicking from initiating edit operation
             this.clickCountToStart = Integer.MAX_VALUE;
         }
 
         @Override
         public Component getTableCellEditorComponent(JTable table,
                                                      Object value, boolean isSelected, int row, int column) {
             JTextField f = (JTextField) super.getTableCellEditorComponent(table, value, isSelected, row, column);
             model = (ContextMatrixModel) table.getModel();
             String text;
             if (column == 0) {
                 text = model.getObjectNameAt(row-1);
             } else {
                 text = model.getAttributeNameAt(column-1);
             }
             f.setText(text);
             lastName = text;
             lastColumn = column;
             lastRow = row;
             this.textField = f;
             return f;
         }
 
         @Override
         public Object getCellEditorValue() {
             String newName = super.getCellEditorValue().toString();
             if (lastColumn == 0) {
                 boolean success = model.renameObject(lastName, newName);
                 if (!success) {
                     // TODO: Show dialog that says name already taken
                 }
             } else {
                 boolean success = model.renameAttribute(lastName, newName);
                 if (!success) {
                     // TODO: Show dialog that says name already taken
                 }
             }
             return super.getCellEditorValue();
         }
 
         public JTextField getTextField() {
             return textField;
         }
 
     }
 
 
 
     ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     // Dragging
     ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 
     // For dragging logic
     boolean isDraggingRow = false;
     boolean isDraggingColumn = false;
     boolean didReorderOccur = false;
     int lastDraggedRowIndex;
     int lastDraggedColumnIndex;
 
     private void createDraggingInteractions() {
         MouseAdapter mouseAdapter = new MouseAdapter() {
 
             public void mousePressed(MouseEvent e) {
                 int i = rowAtPoint(e.getPoint());
                 int j = columnAtPoint(e.getPoint());
                 lastDraggedRowIndex = i;
                 lastDraggedColumnIndex = j;
                 // A hacky way to check if the user is currently resizing a column
                 if (getCursor() != ContextMatrix.resizeCursor) {
                     if (SwingUtilities.isLeftMouseButton(e) && j == 0 && i > 0) {
                         isDraggingRow = true;
                         setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                     }
                     if (SwingUtilities.isLeftMouseButton(e) && i == 0 && j > 0) {
                         isDraggingColumn = true;
                         setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                     }
                 }
             }
 
             public void mouseDragged(MouseEvent e) {
                 Reorderable model = (Reorderable) getModel();
                 int i = rowAtPoint(e.getPoint());
                 int j = columnAtPoint(e.getPoint());
                 if (i < 0 || j < 0) return;
                 if (isDraggingRow || isDraggingColumn) clearSelection();
                 // A reorder of rows occured
                 if (isDraggingRow && i != lastDraggedRowIndex && i != 0) {
                     model.reorderRows(lastDraggedRowIndex, i);
                     ((AbstractTableModel)getModel()).fireTableDataChanged();
                     lastDraggedRowIndex = i;
                     didReorderOccur = true;
                 }
                 // A reorder of columns occured
                 if (isDraggingColumn && j != lastDraggedColumnIndex && j != 0) {
                     // to prevent a bug when reordering columns of different widths
                     Rectangle selected = getCellRect(0, lastDraggedColumnIndex, false);
                     Rectangle r = getCellRect(0, j, false);
                     Point p = r.getLocation();
                     if ((j <= lastDraggedColumnIndex || e.getX() > p.x + r.width - selected.width) &&
                             (j >  lastDraggedColumnIndex || e.getX() < p.x + selected.width)) {
                         model.reorderColumns(lastDraggedColumnIndex, j);
                         switchColumnWidths(lastDraggedColumnIndex, j);
                         ((AbstractTableModel) model).fireTableDataChanged();
                         lastDraggedColumnIndex = j;
                         didReorderOccur = true;
                     }
                 }
             }
 
             public void mouseReleased(MouseEvent e) {
                 // For selecting entire row/column when clicking on a header
                 int i = rowAtPoint(e.getPoint());
                 int j = columnAtPoint(e.getPoint());
                 if (getCursor() != ContextMatrix.resizeCursor && !didReorderOccur) {
                     if (SwingUtilities.isLeftMouseButton(e) && j == 0 && i > 0) {
                         if (!wasRowSelected(i)) selectRow(i);
                         else clearSelection();
                     }
                     if (SwingUtilities.isLeftMouseButton(e) && i == 0 && j > 0) {
                         if (!wasColumnSelected(j)) selectColumn(j);
                         else clearSelection();
                     }
                 }
 
                 isDraggingRow = false;
                 isDraggingColumn = false;
                 didReorderOccur = false;
                 if (getCursor() != ContextMatrix.resizeCursor) {
                     setCursor(Cursor.getDefaultCursor());
                 }
                 saveSelectedInterval();
                 invalidate();
                 repaint();
             }
         };
         addMouseListener(mouseAdapter);
         addMouseMotionListener(mouseAdapter);
     }
 
 
     ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     // Resizing
     ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 
     // Beware! Unelegant code ahead!
     public static final int DEFAULT_COLUMN_WIDTH = 80;
     public static final int COMPACTED_COLUMN_WIDTH = 15;
     public static Cursor resizeCursor = Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR);
     public Map<Integer,Integer> columnWidths;
     public Map<Integer,Integer> compactedColumnWidths = new HashMap<>();
     private int mouseXOffset;
     private Cursor otherCursor = resizeCursor;
     private TableColumn resizingColumn;
     private boolean isCompacted = false;
 
     public void compact() {
         isCompacted = true;
         for (int i = 1; i < getColumnModel().getColumnCount(); i++) {
             getColumnModel().getColumn(i).setPreferredWidth(COMPACTED_COLUMN_WIDTH);
         }
         compactedColumnWidths.clear();
     }
 
     public void uncompact() {
         isCompacted = false;
         restoreColumnWidths();
     }
 
     public void restoreColumnWidths() {
         if (columnWidths == null) return;
         if (!isCompacted) {
             for (int i = 0; i < getColumnCount(); i++) {
                 Integer w = columnWidths.get(i);
                 TableColumn t = getColumnModel().getColumn(i);
                 if (w == null) {
                     t.setPreferredWidth(DEFAULT_COLUMN_WIDTH);
                 } else {
                     t.setPreferredWidth(w);
                 }
             }
         } else {
             for (int i = 1; i < getColumnCount(); i++) {
                 Integer w = compactedColumnWidths.get(i);
                 TableColumn t = getColumnModel().getColumn(i);
                 if (w == null) {
                     t.setPreferredWidth(COMPACTED_COLUMN_WIDTH);
                 } else {
                     t.setPreferredWidth(w);
                 }
             }
         }
     }
 
     public void updateColumnWidths(int removedIndex) {
         if (!isCompacted) {
             columnWidths.remove(removedIndex);
            for (int i = removedIndex + 1; i < getColumnCount() + 20; i++) {
                 Integer oldWidth = columnWidths.remove(i);
                 if (oldWidth == null) continue;
                 if (i == 1) continue;
                 columnWidths.put(i-1, oldWidth);
             }
         } else {
             compactedColumnWidths.remove(removedIndex);
            for (int i = removedIndex + 1; i < getColumnCount() + 20; i++) {
                 Integer oldWidth = compactedColumnWidths.remove(i);
                 if (oldWidth == null) continue;
                 if (i == 1) continue;
                 compactedColumnWidths.put(i-1, oldWidth);
             }
         }
     }
 
     public void switchColumnWidths(int from, int to) {
         Integer fromVal, toVal;
 
         fromVal = columnWidths.get(from);
         toVal = columnWidths.get(to);
         if (fromVal == null) fromVal = ContextMatrix.DEFAULT_COLUMN_WIDTH;
         if (toVal == null) toVal = ContextMatrix.DEFAULT_COLUMN_WIDTH;
         columnWidths.put(to, fromVal);
         columnWidths.put(from, toVal);
 
         fromVal = compactedColumnWidths.get(from);
         toVal = compactedColumnWidths.get(to);
         if (fromVal == null) fromVal = ContextMatrix.COMPACTED_COLUMN_WIDTH;
         if (toVal == null) toVal = ContextMatrix.COMPACTED_COLUMN_WIDTH;
         compactedColumnWidths.put(to, fromVal);
         compactedColumnWidths.put(from, toVal);
     }
 
     private void createResizingInteractions() {
         MouseAdapter columnResizeMouseAdapter = new MouseAdapter() {
 
             public void mousePressed(MouseEvent e){
                 ContextMatrix matrix = ContextMatrix.this;
                 Point p = e.getPoint();
                 // First find which header cell was hit
                 int index = matrix.columnAtPoint(p);
                 if(index == -1) return;
                 // The last 3 pixels + 3 pixels of next column are for resizing
                 TableColumn resizingColumn = getResizingColumn(p, index);
                 if(resizingColumn == null) return;
                 matrix.resizingColumn = resizingColumn;
                 mouseXOffset = p.x - resizingColumn.getWidth();
                 matrix.restoreSelectedInterval();
             }
 
             public void mouseMoved(MouseEvent e){
                 ContextMatrix matrix = ContextMatrix.this;
                 if((getResizingColumn(e.getPoint()) == null) == (matrix.getCursor() == resizeCursor)){
                     swapCursor();
                 }
             }
 
             public void mouseDragged(MouseEvent e){
                 ContextMatrix matrix = ContextMatrix.this;
                 int mouseX = e.getX();
                 TableColumn resizingColumn = matrix.resizingColumn;
                 if(resizingColumn != null){
                     matrix.restoreSelectedInterval();
                     int oldWidth = resizingColumn.getWidth();
                     int newWidth = Math.max(mouseX - mouseXOffset, 20);
                     resizingColumn.setWidth(newWidth);
                     resizingColumn.setPreferredWidth(newWidth);
                     if (!isCompacted) {
                         columnWidths.put(resizingColumn.getModelIndex(), newWidth);
                     } else {
                         compactedColumnWidths.put(resizingColumn.getModelIndex(), newWidth);
                     }
 
                     Container container;
                     if((matrix.getParent() == null)
                             || ((container = matrix.getParent().getParent()) == null)
                             || !(container instanceof JScrollPane)){
                         return;
                     }
 
                     JViewport viewport = ((JScrollPane)container).getViewport();
                     int viewportWidth = viewport.getWidth();
                     int diff = newWidth - oldWidth;
                     int newHeaderWidth = matrix.getWidth() + diff;
 
                     // Resize a table
                     Dimension tableSize = matrix.getSize();
                     tableSize.width += diff;
                     matrix.setSize(tableSize);
 
                     // If this table is in AUTO_RESIZE_OFF mode and has a horizontal
                     // scrollbar, we need to update a view's position.
                     if((newHeaderWidth >= viewportWidth)
                             && (matrix.getAutoResizeMode() == JTable.AUTO_RESIZE_OFF)){
                         Point p = viewport.getViewPosition();
                         p.x =
                                 Math.max(0, Math.min(newHeaderWidth - viewportWidth, p.x + diff));
                         viewport.setViewPosition(p);
 
                         // Update the original X offset value.
                         mouseXOffset += diff;
                     }
                 }
             }
 
             public void mouseReleased(MouseEvent e){
                 ContextMatrix matrix = ContextMatrix.this;
                 matrix.resizingColumn = null;
 //                matrix.saveSelectedInterval();
             }
 
             private void swapCursor(){
                 Cursor tmp = getCursor();
                 setCursor(otherCursor);
                 otherCursor = tmp;
             }
 
             private TableColumn getResizingColumn(Point p) {
                 return getResizingColumn(p, columnAtPoint(p));
             }
 
             private TableColumn getResizingColumn(Point p, int column) {
                 if (column == -1) return null;
                 int row = rowAtPoint(p);
                 if (row != 0) return null;
                 Rectangle r = getCellRect(row, column, true);
                 r.grow(-3, 0);
                 if (r.contains(p)) return null;
                 int midPoint = r.x + r.width / 2;
                 int columnIndex = (p.x < midPoint) ? column - 1 : column;
                 if(columnIndex == -1) return null;
                 return getColumnModel().getColumn(columnIndex);
             }
         };
         addMouseListener(columnResizeMouseAdapter);
         addMouseMotionListener(columnResizeMouseAdapter);
     }
 
 
     ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
     // Drawing
     ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 
     private class StripedViewport extends JViewport {
 
         private static final long serialVersionUID = 171992496170114834L;
 
         // Needed as otherwise there is a weird white area below the editor
         // We just paint the editor background in the background color of the containing element
         private final Color BACKGROUND_COLOR;
         private final JTable fTable;
 
         public StripedViewport(JTable table, Color bg) {
             BACKGROUND_COLOR = bg;
             fTable = table;
             setBackground(BACKGROUND_COLOR);
             setOpaque(false);
             initListeners();
         }
 
         private void initListeners() {
             PropertyChangeListener listener = createTableColumnWidthListener();
             for (int i=0; i<fTable.getColumnModel().getColumnCount(); i++) {
                 fTable.getColumnModel().getColumn(i).addPropertyChangeListener(listener);
             }
         }
 
         private PropertyChangeListener createTableColumnWidthListener() {
             return new PropertyChangeListener() {
                 public void propertyChange(PropertyChangeEvent evt) {
                     repaint();
                 }
             };
         }
 
         @Override
         public void setViewPosition(Point p) {
             super.setViewPosition(p);
             repaint();
         }
 
         @Override
         protected void paintComponent(Graphics g) {
             paintBackground(g);
             paintStripedBackground(g);
             paintVerticalHeaderBackground(g);
             paintHorizontalHeaderBackground(g);
             paintGridLines(g);
             super.paintComponent(g);
         }
 
         private void paintBackground(Graphics g) {
             g.setColor(BACKGROUND_COLOR);
             g.fillRect(g.getClipBounds().x, g.getClipBounds().y,
                     g.getClipBounds().width, g.getClipBounds().height);
         }
 
         private void paintStripedBackground(Graphics g) {
             int rowHeight = fTable.getRowHeight();
             int tableWidth = fTable.getWidth();
             int offsetX = getViewPosition().x;
             int offsetY = getViewPosition().y;
             int x = -offsetX;
             int y = -offsetY;
             for (int j = 0; j < fTable.getRowCount(); j++) {
                 g.setColor(j % 2 == 0 ? EVEN_ROW_COLOR : ODD_ROW_COLOR);
                 g.fillRect(x, y + j * rowHeight, tableWidth, rowHeight);
             }
         }
 
         private void paintVerticalHeaderBackground(Graphics g) {
             int tableHeight = fTable.getHeight();
             int firstColumnWidth = fTable.getColumnModel().getColumn(0).getWidth();
             int rowHeight = fTable.getRowHeight();
             int offsetX = getViewPosition().x;
             int offsetY = getViewPosition().y;
             int x = -offsetX;
             int y = -offsetY;
             g.setColor(HEADER_COLOR);
             g.fillRect(x, y, firstColumnWidth, tableHeight);
 
             if (isDraggingRow) {
                 g.setColor(new Color(230,230,230));
                 g.fillRect(x, y + lastDraggedRowIndex * rowHeight + 1, firstColumnWidth, rowHeight - 1);
             }
 
             g.setColor(new Color(255,255,255));
             g.drawLine(x + firstColumnWidth - 2, y + rowHeight, x + firstColumnWidth - 2, y + tableHeight);
             g.setColor(new Color(235,235,235));
             g.drawLine(x + firstColumnWidth, y + rowHeight, x + firstColumnWidth, y + tableHeight);
             g.setColor(new Color(255, 255, 255));
             for (int j = 0; j < fTable.getRowCount() + 1; j++) {
                 g.drawLine(x, y + j * rowHeight - 1, x + firstColumnWidth - 1, y + j * rowHeight - 1);
             }
         }
 
         private void paintHorizontalHeaderBackground(Graphics g) {
             int tableWidth = fTable.getWidth();
             int firstRowHeight = fTable.getRowHeight();
             int firstColumnWidth = fTable.getColumnModel().getColumn(0).getWidth();
             int offsetX = getViewPosition().x;
             int offsetY = getViewPosition().y;
             int x = -offsetX;
             int y = -offsetY;
             g.setColor(HEADER_COLOR);
             g.fillRect(x, y, tableWidth, firstRowHeight);
 
             if (isDraggingColumn) {
                 int columnWidth0 = 0;
                 int columnWidth1 = fTable.getColumnModel().getColumn(lastDraggedColumnIndex).getWidth();
                 for (int j = 1; j < lastDraggedColumnIndex + 1; j++) {
                     columnWidth0 += fTable.getColumnModel().getColumn(j-1).getWidth();
                 }
                 g.setColor(new Color(230,230,230));
                 g.fillRect(x + columnWidth0 - 2, y, columnWidth1, firstRowHeight - 1);
             }
 
             g.setColor(new Color(255, 255, 255));
             g.drawLine(x + firstColumnWidth, y + firstRowHeight - 1, x + tableWidth, y + firstRowHeight - 1);
             g.setColor(new Color(235, 235, 235));
             g.drawLine(x + firstColumnWidth, y + firstRowHeight + 1, x + tableWidth, y + firstRowHeight + 1);
             g.setColor(new Color(255, 255, 255));
             int columnWidth = 0;
             for (int j = 1; j < fTable.getColumnCount() + 1; j++) {
                 columnWidth += fTable.getColumnModel().getColumn(j-1).getWidth();
                 g.drawLine(x + columnWidth - 2, y, x + columnWidth - 2, y + firstRowHeight - 1);
             }
         }
 
         private void paintGridLines(Graphics g) {
             int tableHeight = fTable.getHeight();
             int rowHeight = fTable.getRowHeight();
             int offsetX = getViewPosition().x;
             int offsetY = getViewPosition().y;
             int x = -offsetX;
             int y = -offsetY;
             g.setColor(TABLE_GRID_COLOR);
             for (int i = 0; i < fTable.getColumnCount(); i++) {
                 TableColumn column = fTable.getColumnModel().getColumn(i);
                 x += column.getWidth();
                 g.drawLine(x - 1, y, x - 1, y + tableHeight);
             }
             for (int j = 1; j < fTable.getRowCount() + 1; j++) {
                 g.drawLine(-offsetX, y + j * rowHeight, x - 1, y + j * rowHeight);
             }
         }
 
     }
 
 }
