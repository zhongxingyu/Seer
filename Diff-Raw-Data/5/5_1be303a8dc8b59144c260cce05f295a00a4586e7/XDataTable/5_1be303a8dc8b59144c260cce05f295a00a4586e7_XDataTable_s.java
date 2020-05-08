 /*
  * XDataTable.java
  *
  * Created on January 31, 2011, 10:51 AM
  * @author jaycverg
  */
 
 package com.rameses.rcp.control;
 
 import com.rameses.common.PropertyResolver;
 import com.rameses.rcp.common.AbstractListModel;
 import com.rameses.rcp.common.ListItem;
 import com.rameses.rcp.common.MsgBox;
 import com.rameses.rcp.control.table.DataTableComponent;
 import com.rameses.rcp.control.table.ListScrollBar;
 import com.rameses.rcp.control.table.RowHeader;
 import com.rameses.rcp.control.table.TableListener;
 import com.rameses.rcp.control.table.TableUtil;
 import com.rameses.rcp.framework.Binding;
 import com.rameses.rcp.framework.ClientContext;
 import com.rameses.rcp.ui.*;
 import com.rameses.rcp.util.*;
 import com.rameses.util.ValueUtil;
 import java.awt.*;
 import java.awt.event.*;
 import java.beans.*;
 import javax.swing.*;
 import javax.swing.border.AbstractBorder;
 import javax.swing.plaf.metal.MetalLookAndFeel;
 
 
 
 public class XDataTable extends JPanel implements UIInput, TableListener, Validatable, FocusListener {
     
     private DataTableComponent table;
     private ListScrollBar scrollBar;
     private RowHeaderView rowHeaderView;
     private JScrollPane scrollPane;
     
     private AbstractListModel listModel;
     private String[] depends;
     private Binding binding;
     private int index;
     private String handler;
     private ActionMessage actionMessage = new ActionMessage();
     private boolean dynamic;
     private boolean showRowHeader;
     private String caption;
     
     private ListItem currentItem;
     
     
     
     public XDataTable() {
         init();
     }
     
     //-- channel events to TableComponent
     public void addMouseListener(MouseListener l) {
         table.addMouseListener(l);
     }
     
     public void removeMouseListener(MouseListener l) {
         table.removeMouseListener(l);
     }
     
     public void addKeyListener(KeyListener l) {
         table.addKeyListener(l);
     }
     
     public void removeKeyListener(KeyListener l) {
         table.removeKeyListener(l);
     }
     
     //<editor-fold defaultstate="collapsed" desc="  initialize table  ">
     private void init() {
         table = new DataTableComponent();
         scrollBar = new ListScrollBar();
         
         //--create and decorate scrollpane for the JTable
         scrollPane = new JScrollPane(table);
         scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
         scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
         scrollPane.setBorder(BorderFactory.createEmptyBorder());
         scrollPane.addMouseWheelListener(new MouseWheelListener() {
             public void mouseWheelMoved(MouseWheelEvent e) {
                 int rotation = e.getWheelRotation();
                 if ( rotation == 0 ) return;
                 
                 if ( rotation < 0 )
                     listModel.moveBackRecord();
                 else
                     listModel.moveNextRecord();
             }
         });
         
         super.setLayout(new BorderLayout());
         add(scrollPane, BorderLayout.CENTER);
         add(new ScrollBarPanel(scrollBar), BorderLayout.EAST);
         setBorder(new TableBorder());
         
         //--default table properties
         setGridColor(new Color(217,216,216));
         setShowRowHeader(false);
         setShowHorizontalLines(false);
         setRowMargin(0);
         setShowVerticalLines(true);
         setAutoResize(true);
         
         if ( table.getEvenBackground() == null ) {
             Color bg = (Color) UIManager.get("Table.evenBackground");
             if ( bg == null ) bg = table.getBackground();
             table.setEvenBackground(bg);
         }
         
         if ( table.getEvenForeground() == null ) {
             Color fg = (Color) UIManager.get("Table.evenForeground");
             if ( fg != null ) table.setEvenForeground(fg);
         }
         
         if ( table.getOddBackground() == null ) {
             Color bg = (Color) UIManager.get("Table.oddBackground");
             if ( bg == null ) bg = new Color(225, 232, 246);
             table.setOddBackground(bg);
         }
         
         if ( table.getOddForeground() == null ) {
             Color fg = (Color) UIManager.get("Table.oddForeground");
             if ( fg != null ) table.setOddForeground(fg);
         }
         
         //--design time display
         if ( Beans.isDesignTime() ) {
             if ( rowHeaderView != null )
                 rowHeaderView.setRowCount(1);
             
             setPreferredSize(new Dimension(200,80));
             table.setModel(new javax.swing.table.DefaultTableModel(
                     new Object [][] { {null, null} },
                     new String [] { "Title 1", "Title 2" }
             ));
         }
     }
     //</editor-fold>
     
     //<editor-fold defaultstate="collapsed" desc="  UIInput properties  ">
     public String[] getDepends() {
         return depends;
     }
     
     public void setDepends(String[] depends) {
         this.depends = depends;
     }
     
     public int getIndex() {
         return index;
     }
     
     public void setIndex(int index) {
         this.index = index;
     }
     
     public void setBinding(Binding binding) {
         this.binding = binding;
     }
     
     public Binding getBinding() {
         return binding;
     }
     
     public void refresh() {
         if ( listModel != null ) {
             if ( dynamic )
                 listModel.load();
             else
                 listModel.refresh();
         }
     }
     
     public void load() {
         if ( handler != null ) {
             Object obj = UIControlUtil.getBeanValue(this, handler);
             if ( obj instanceof AbstractListModel ) {
                 listModel = (AbstractListModel) obj;
                 table.setListModel(listModel);
                 table.setListener(this);
                 table.setBinding(binding);
                 scrollBar.setListModel(listModel);
                 
                 if ( rowHeaderView != null )
                     rowHeaderView.setRowCount( listModel.getRows() );
                 
                 if( !dynamic )
                     listModel.load();
             }
             
         }
     }
     
     public Object getValue() {
         if ( Beans.isDesignTime() ) return null;
         
         return listModel.getSelectedItem();
     }
     
     public void setValue(Object value) {}
     
     public boolean isNullWhenEmpty() {
         return true;
     }
     
     public int compareTo(Object o) {
         return UIControlUtil.compare(this, o);
     }
     //</editor-fold>
     
     //<editor-fold defaultstate="collapsed" desc="  table listener methods  ">
     public void refreshList() {
         scrollBar.adjustValues();
         rowChanged();
     }
     
     public void openItem() {
         ListItem selectedItem = listModel.getSelectedItem();
         if( selectedItem!=null && selectedItem.getItem()!=null) {
             try {
                 Object outcome = listModel.openSelectedItem();
                 if ( outcome == null ) return;
                 
                 ControlSupport.fireNavigation(this, outcome);
                 
             } catch(Exception ex){
                 MsgBox.err(new IllegalStateException(ex));
             }
         }
     }
     
     public void rowChanged() {
         String name = getName();
         PropertyResolver resolver = ClientContext.getCurrentContext().getPropertyResolver();
         ListItem item = listModel.getSelectedItem();
         
         if( !ValueUtil.isEqual(currentItem, item) ) {
             if ( !ValueUtil.isEmpty(name) ) {
                 Object value = null;
                 if( item != null ) value = item.getItem();
                 
                 resolver.setProperty(binding.getBean(), name, value);
                 binding.notifyDepends(this);
             }
             
             String varStatus = table.getVarStatus();
             if ( !ValueUtil.isEmpty(varStatus) ) {
                 try {
                     resolver.setProperty(binding.getBean(), varStatus, item);
                 } catch(Exception e){}
             }
         }
         
         //keep the actual state at this time
        currentItem = item.clone();
         
         if ( rowHeaderView != null )
             rowHeaderView.clearEditing();
     }
     
     public void editCellAt(int rowIndex, int colIndex) {
         if ( rowHeaderView != null )
             rowHeaderView.editRow(rowIndex);
     }
     
     public void cancelRowEdit() {
         if ( rowHeaderView != null )
             rowHeaderView.clearEditing();
     }
     //</editor-fold>
     
     //<editor-fold defaultstate="collapsed" desc="  Getters/Setters  ">
     public boolean isRequired() {
         return table.isRequired();
     }
     
     public void setRequired(boolean required) {}
     
     public void validateInput() {
         String errmsg = listModel.getErrorMessages();
         actionMessage.clearMessages();
         if ( errmsg != null ) {
             StringBuffer buffer = new StringBuffer(errmsg);
             if( !ValueUtil.isEmpty(caption) ) {
                 buffer.insert(0, caption + " (\n")
                 .append("\n)");
             }
             actionMessage.addMessage(null, buffer.toString(), null);
         }
     }
     
     public ActionMessage getActionMessage() {
         return actionMessage;
     }
     
     public void setReadonly(boolean readonly) {
         table.setReadonly(readonly);
     }
     
     public boolean isReadonly() {
         return table.isReadonly();
     }
     
     public String getCaption() {
         return this.caption;
     }
     
     public void setCaption(String caption) {
         this.caption = caption;
     }
     
     public void setName(String name) {
         super.setName(name);
         if ( table != null ) table.setName(name);
     }
     
     public void setLayout(LayoutManager mgr) {;}
     
     public String getHandler()             { return handler; }
     public void setHandler(String handler) { this.handler = handler; }
     
     public boolean isDynamic()              { return dynamic; }
     public void setDynamic(boolean dynamic) { this.dynamic = dynamic; }
     
     public void setShowHorizontalLines(boolean show) { table.setShowHorizontalLines(show); }
     public boolean isShowHorizontalLines()           { return table.getShowHorizontalLines(); }
     
     public void setShowVerticalLines(boolean show) { table.setShowVerticalLines(show); }
     public boolean isShowVerticalLines()           { return table.getShowVerticalLines(); }
     
     public void setAutoResize(boolean autoResize) { table.setAutoResize(autoResize); }
     public boolean isAutoResize()                 { return table.isAutoResize(); }
     
     public void setRequestFocus(boolean focus) {
         if ( focus ) table.requestFocus();
     }
     
     public void requestFocus() { table.requestFocus(); }
     
     public void focusGained(FocusEvent e) { table.grabFocus(); }
     public void focusLost(FocusEvent e)   {}
     
     public Color getEvenBackground()                    { return table.getEvenBackground(); }
     public void setEvenBackground(Color evenBackground) { table.setEvenBackground( evenBackground ); }
     
     public Color getOddBackground()                   { return table.getOddBackground(); }
     public void setOddBackground(Color oddBackground) { table.setOddBackground( oddBackground ); }
     
     public Color getErrorBackground()                     { return table.getErrorBackground(); }
     public void setErrorBackground(Color errorBackground) { table.setErrorBackground( errorBackground ); }
     
     public Color getEvenForeground()                    { return table.getEvenForeground(); }
     public void setEvenForeground(Color evenForeground) { table.setEvenForeground( evenForeground ); }
     
     public Color getOddForeground()                   { return table.getOddForeground(); }
     public void setOddForeground(Color oddForeground) { table.setOddForeground( oddForeground ); }
     
     public Color getErrorForeground()                     { return table.getErrorForeground(); }
     public void setErrorForeground(Color errorForeground) { table.setErrorForeground( errorForeground ); }
     
     public boolean isImmediate() { return true; }
     
     public boolean isShowRowHeader() { return showRowHeader; }
     public void setShowRowHeader(boolean showRowHeader) {
         this.showRowHeader = showRowHeader;
         
         if ( showRowHeader ) {
             Color gridColor = getGridColor();
             scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, TableUtil.getTableCornerComponent(gridColor));
             scrollPane.setRowHeaderView( (rowHeaderView = new RowHeaderView()) );
             rowHeaderView.setRowCount( table.getRowCount() );
         } else {
             scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, null);
             scrollPane.setRowHeaderView( (rowHeaderView = null) );
         }
     }
     
     public int getColumnMargin()            { return table.getColumnModel().getColumnMargin(); }
     public void setColumnMargin(int margin) { table.getColumnModel().setColumnMargin(margin); }
     
     public int getRowMargin()            { return table.getRowMargin(); }
     public void setRowMargin(int margin) { table.setRowMargin(margin); }
     
     public Color getGridColor()           { return table.getGridColor(); }
     public void setGridColor(Color color) { table.setGridColor(color); }
     
     public boolean isEnabled()        { return table.isEnabled(); }
     public void setEnabled(boolean e) { table.setEnabled(e); }
     
     public int getRowHeight()       { return table.getRowHeight(); }
     public void setRowHeight(int h) { table.setRowHeight(h); }
     
     public String getVarStatus()               { return table.getVarStatus(); }
     public void setVarStatus(String varStatus) { table.setVarStatus(varStatus); }
     
     //</editor-fold>
     
     
     //--- inner classess
     
     //<editor-fold defaultstate="collapsed" desc="  TableBorder (class)  ">
     private static class TableBorder extends AbstractBorder {
         
         private static final Insets insets = new Insets(1, 1, 2, 2);
         
         public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
             
             g.translate( x, y);
             
             g.setColor( MetalLookAndFeel.getControlDarkShadow() );
             g.drawRect( 0, 0, w-2, h-2 );
             g.setColor( MetalLookAndFeel.getControlHighlight() );
             
             g.drawLine( w-1, 1, w-1, h-1);
             g.drawLine( 1, h-1, w-1, h-1);
             
             g.setColor( MetalLookAndFeel.getControl() );
             g.drawLine( w-2, 2, w-2, 2 );
             g.drawLine( 1, h-2, 1, h-2 );
             
             g.translate( -x, -y);
             
         }
         
         public Insets getBorderInsets(Component c)       {
             return insets;
         }
     }
     //</editor-fold>
     
     //<editor-fold defaultstate="collapsed" desc="  ScrollBarPanel (class)  ">
     private class ScrollBarPanel extends JPanel {
         
         ScrollBarPanel(JScrollBar scrollBar) {
             Dimension ps = scrollBar.getPreferredSize();
             setPreferredSize(ps);
             setLayout(new BorderLayout());
             
             scrollBar.addPropertyChangeListener(new PropertyChangeListener() {
                 public void propertyChange(PropertyChangeEvent evt) {
                     String propName = evt.getPropertyName();
                     if ( "visible".equals(propName) ) {
                         Boolean visible = (Boolean) evt.getNewValue();;
                         setVisible(visible.booleanValue());
                     }
                 }
             });
             
             setVisible( scrollBar.isVisible() );
             add(TableUtil.getTableCornerComponent(getGridColor()), BorderLayout.NORTH);
             add(scrollBar, BorderLayout.CENTER);
         }
         
     }
     //</editor-fold>
     
     //<editor-fold defaultstate="collapsed" desc="  RowHeaderView (class)  ">
     private class RowHeaderView extends JPanel {
         
         private int rowCount;
         private int currentRow = -1;
         
         RowHeaderView() {
             setLayout(new RowHeaderLayout());
         }
         
         public void setRowCount(int rowCount) {
             if ( this.rowCount == rowCount ) return;
             this.rowCount = rowCount;
             
             removeAll();
             JComponent label = null;
             for (int i = 0; i < rowCount; ++i) {
                 add(new RowHeader(table.getGridColor()));
             }
             SwingUtilities.updateComponentTreeUI(this);
         }
         
         public void clearEditing() {
             if ( currentRow != -1 ) {
                 RowHeader rh = (RowHeader) getComponent(currentRow);
                 rh.edit(false);
             }
             currentRow = -1;
         }
         
         public void editRow(int row) {
             if ( currentRow != row ) {
                 clearEditing();
             }
             RowHeader rh = (RowHeader) getComponent(row);
             rh.edit(true);
             currentRow = row;
         }
     }
     //</editor-fold>
     
     //<editor-fold defaultstate="collapsed" desc="  RowHeaderLayout (Class) ">
     private class RowHeaderLayout implements LayoutManager {
         
         public void addLayoutComponent(String name, Component comp) {;}
         public void removeLayoutComponent(Component comp) {;}
         
         public Dimension preferredLayoutSize(Container parent) {
             return getLayoutSize(parent);
         }
         
         public Dimension minimumLayoutSize(Container parent) {
             return getLayoutSize(parent);
         }
         
         public void layoutContainer(Container parent) {
             synchronized (parent.getTreeLock()) {
                 Insets margin = parent.getInsets();
                 int x = margin.left;
                 int y = margin.top;
                 int w = parent.getWidth() - (margin.left + margin.right);
                 int h = parent.getHeight() - (margin.top + margin.bottom);
                 
                 Component[] comps = parent.getComponents();
                 for (int i=0; i<comps.length; i++) {
                     if (!(comps[i] instanceof RowHeader)) continue;
                     
                     int rh = XDataTable.this.table.getRowHeight(i);
                     comps[i].setBounds(x, y, w, rh);
                     y += rh;
                 }
             }
         }
         
         private Dimension getLayoutSize(Container parent) {
             synchronized (parent.getTreeLock()) {
                 int w = 0;
                 int h = 0;
                 Component[] comps = parent.getComponents();
                 for (int i=0; i<comps.length; i++) {
                     if (!(comps[i] instanceof RowHeader)) continue;
                     
                     Dimension dim = comps[i].getPreferredSize();
                     w = Math.max(w, dim.width);
                     h += XDataTable.this.table.getRowHeight(i);
                 }
                 
                 Insets margin = parent.getInsets();
                 w += (margin.left + margin.right);
                 h += (margin.top + margin.bottom);
                 return new Dimension(w,h);
             }
         }
     }
     //</editor-fold>
     
 }
