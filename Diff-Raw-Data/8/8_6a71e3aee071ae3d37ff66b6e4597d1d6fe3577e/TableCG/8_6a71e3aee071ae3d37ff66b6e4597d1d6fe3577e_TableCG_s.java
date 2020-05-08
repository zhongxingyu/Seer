 /*
  * $Id$
  * Copyright 2000,2005 wingS development team.
  *
  * This file is part of wingS (http://www.j-wings.org).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 package org.wings.plaf.css;
 
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.wings.SCellRendererPane;
 import org.wings.SComponent;
 import org.wings.SConstants;
 import org.wings.SDimension;
 import org.wings.SIcon;
 import org.wings.SLabel;
 import org.wings.SListSelectionModel;
 import org.wings.STable;
 import org.wings.io.CachingDevice;
 import org.wings.io.Device;
 import org.wings.io.StringBuilderDevice;
 import org.wings.plaf.CGManager;
 import org.wings.session.SessionManager;
 import org.wings.table.SDefaultTableCellRenderer;
 import org.wings.table.STableCellRenderer;
 import org.wings.table.STableColumn;
 import org.wings.table.STableColumnModel;
 import org.wings.util.SStringBuilder;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.KeyEvent;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 public class TableCG extends AbstractComponentCG implements org.wings.plaf.TableCG {
     private static final long serialVersionUID = 1L;
     /**
      * Apache jakarta commons logger
      */
     private final static Log log = LogFactory.getLog(TableCG.class);
     protected String fixedTableBorderWidth;
     protected SIcon editIcon;
     protected String selectionColumnWidth = "22px";
 
     /**
      * Initialize properties from config
      */
     public TableCG() {
         final CGManager manager = SessionManager.getSession().getCGManager();
         setFixedTableBorderWidth((String) manager.getObject("TableCG.fixedTableBorderWidth", String.class));
         setEditIcon(manager.getIcon("TableCG.editIcon"));
         selectionColumnWidth = (String) manager.getObject("TableCG.selectionColumnWidth", String.class);
     }
 
     /**
      * Tweak property. Declares a deprecated BORDER=xxx attribute on the HTML TABLE element.
      */
     public String getFixedTableBorderWidth() {
         return fixedTableBorderWidth;
     }
 
     /**
      * Tweak property. Declares a deprecated BORDER=xxx attribute on the HTML TABLE element.
      */
     public void setFixedTableBorderWidth(String fixedTableBorderWidth) {
         this.fixedTableBorderWidth = fixedTableBorderWidth;
     }
 
 
     /**
      * Sets the icon used to indicated an editable cell (if content is not direct clickable).
      */
     public void setEditIcon(SIcon editIcon) {
         this.editIcon = editIcon;
     }
 
     /**
      * @return Returns the icon used to indicated an editable cell (if content is not direct clickable).
      */
     public SIcon getEditIcon() {
         return editIcon;
     }
 
     /**
      * @return The width of the (optional) row selection column in px
      */
     public String getSelectionColumnWidth() {
         return selectionColumnWidth;
     }
 
     /**
      * The width of the (optional) row selection column in px
      *
      * @param selectionColumnWidth The width of the (optional) row selection column with unit
      */
     public void setSelectionColumnWidth(String selectionColumnWidth) {
         this.selectionColumnWidth = selectionColumnWidth;
     }
 
 
     public void installCG(final SComponent comp) {
         super.installCG(comp);
 
         final STable table = (STable) comp;
         final CGManager manager = table.getSession().getCGManager();
         Object value;
 
         value = manager.getObject("STable.defaultRenderer", STableCellRenderer.class);
         if (value != null) {
             table.setDefaultRenderer((STableCellRenderer) value);
             if (value instanceof SDefaultTableCellRenderer) {
                 SDefaultTableCellRenderer cellRenderer = (SDefaultTableCellRenderer) value;
                 cellRenderer.setEditIcon(editIcon);
             }
         }
 
         value = manager.getObject("STable.headerRenderer", STableCellRenderer.class);
         if (value != null) {
             table.setHeaderRenderer((STableCellRenderer) value);
         }
 
         value = manager.getObject("STable.rowSelectionRenderer", org.wings.table.STableCellRenderer.class);
         if (value != null) {
             table.setRowSelectionRenderer((org.wings.table.STableCellRenderer) value);
         }
 
         InputMap inputMap = new InputMap();
         inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, KeyEvent.ALT_DOWN_MASK, false), "left");
         inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, KeyEvent.ALT_DOWN_MASK, false), "right");
         inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.ALT_DOWN_MASK, false), "up");
         inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.ALT_DOWN_MASK, false), "down");
         table.setInputMap(SComponent.WHEN_IN_FOCUSED_FRAME, inputMap);
 
         Action action = new AbstractAction() {
             public void actionPerformed(ActionEvent e) {
                 if (!table.isEditing())
                     return;
                 if (table.getEditingRow() > 0 && "up".equals(e.getActionCommand()))
                     table.setEditingRow(table.getEditingRow() - 1);
                 if (table.getEditingRow() < table.getRowCount() -1 && "down".equals(e.getActionCommand()))
                     table.setEditingRow(table.getEditingRow() + 1);
                 if (table.getEditingColumn() > 0 && "left".equals(e.getActionCommand()))
                     table.setEditingColumn(table.getEditingColumn() - 1);
                 if (table.getEditingColumn() < table.getColumnCount() -1 && "right".equals(e.getActionCommand()))
                     table.setEditingColumn(table.getEditingColumn() + 1);
                 table.requestFocus();
             }
         };
         ActionMap actionMap = new ActionMap();
         actionMap.put("up", action);
         actionMap.put("down", action);
         actionMap.put("left", action);
         actionMap.put("right", action);
         table.setActionMap(actionMap);
     }
 
     public void uninstallCG(SComponent component) {
         super.uninstallCG(component);
         final STable table = (STable) component;
         table.setHeaderRenderer(null);
         table.setDefaultRenderer(null);
         table.setRowSelectionRenderer(null);
         table.setActionMap(null);
         table.setInputMap(null);
     }
 
     /**
      * write a specific cell to the device
      */
     protected void renderCellContent(final Device device, final STable table, final SCellRendererPane rendererPane,
                                      final int row, final int col)
             throws IOException {
         SComponent component = null;
         final boolean isEditingCell = table.isEditing() && row == table.getEditingRow() && col == table.getEditingColumn();
         final boolean editableCell = table.isCellEditable(row, col);
         final boolean selectableCell = table.getSelectionMode() != SListSelectionModel.NO_SELECTION && !table.isEditable();
 
         if (isEditingCell) {
             component = table.getEditorComponent();
         } else {
             component = table.prepareRenderer(table.getCellRenderer(row, col), row, col);
         }
 
         final boolean contentContainsClickables = !(component instanceof SLabel);
 
         device.print("<td");
         Utils.optAttribute(device, "col", col);
 
         if (component == null) {
             device.print("></td>");
             return;
         }
         Utils.printTableCellAlignment(device, component, SConstants.LEFT, SConstants.TOP);
         device.print(">");
 
         // Collect inline dynamic styles of cell renderer component:
         // Cell renderer components are not visible to the DynamicStyleResource as they are not
         // reachable via the component hierarchy. They'd also not be reachable for the event
         // dispatcher if not added to the CellRendererPane. Adding to the cellrenderer pane
         // does the job of registering those items as low level event listener.
         // the following code does the job of rendering their styles inline.
         // TODO: Maybe "compress" repeated styles here as well as in the stylesheet writer.
         try {
             final StringBuilderDevice stringBuilderDevice = new StringBuilderDevice();
             final String styleString = stringBuilderDevice.toString();
             if (styleString.length() > 0) {
                 device.print("<style>").print(styleString).print("</style>");
             }
         } catch (Exception e) {
             log.info("Unexpected Exception durign collection of cell renderer styles", e);
         }
 
         String parameter = null;
         if (table.isEditable() && !isEditingCell && editableCell)
             parameter = table.getEditParameter(row, col);
         else if (selectableCell)
             parameter = table.getToggleSelectionParameter(row, col);
 
         if (parameter != null && !isEditingCell && (selectableCell || editableCell) && !contentContainsClickables) {
             Utils.printButtonStart(device, table, parameter, true, table.getShowAsFormComponent());
             device.print(">");
         } else
             device.print("<span>");
 
         rendererPane.writeComponent(device, component, table);
 
         if (parameter != null && !isEditingCell && selectableCell && !contentContainsClickables) {
             Utils.printButtonEnd(device, table, parameter, true);
         } else
             device.print("</span>");
 
         device.print("</td>");
         Utils.printNewline(device, component);
     }
 
     protected void writeHeaderCell(final Device device, final STable table,
                                    final SCellRendererPane rendererPane,
                                    final int c)
             throws IOException {
 
         final SComponent comp = table.prepareHeaderRenderer(c);
 
         device.print("<th");
         Utils.printTableCellAlignment(device, comp, SConstants.CENTER, SConstants.CENTER);
         device.print(">");
         rendererPane.writeComponent(device, comp, table);
         device.print("</th>");
         Utils.printNewline(device, comp);
     }
 
 
     public final void writeInternal(final Device _device, final SComponent _c) throws IOException {
         final STable table = (STable) _c;
         final SDimension intercellPadding = table.getIntercellPadding();
         final SDimension intercellSpacing = table.getIntercellSpacing();
         final SListSelectionModel selectionModel = table.getSelectionModel();
         final SCellRendererPane rendererPane = table.getCellRendererPane();
         final boolean needsSelectionRow = selectionModel.getSelectionMode() != SListSelectionModel.NO_SELECTION && table.isEditable();
         final boolean showAsFormComponent = table.getShowAsFormComponent();
         final SDimension tableWidthByColumnModel = determineTableWidthByColumnModel(table, needsSelectionRow);
 
         /**
          * Description: This is a FIREFOX bug workaround. Currently we render all components surrounded by a DIV.
          * During heavy load and incremental delivery of a page this leads to disorted tables as the firefox seems
          * to have an bug. Refer to http://jira.j-wings.org/browse/WGS-139
          *
          * THis workaround tries to deliver the HTML code of a table at once. This should resolve this issue to 99%.
          */
        final boolean innerTable = _device instanceof CachingDevice;
        final CachingDevice device = innerTable ? (CachingDevice) _device : new CachingDevice(_device);
 
         try {
             device.print("<table");
         if (tableWidthByColumnModel != null)
             Utils.optAttribute(device,"style",tableWidthByColumnModel.toString()); // apply table dimension if set
         else
             Utils.printCSSInlineFullSize(device, table.getPreferredSize()); // stretch if outer dimension has been set
 
         // TODO: border="" should be obsolete
         // TODO: cellspacing and cellpadding may be in conflict with border-collapse
         /* Tweaking: CG configured to have a fixed border="xy" width */
         Utils.optAttribute(device, "class", "STable");
         Utils.optAttribute(device, "border", fixedTableBorderWidth);
         Utils.optAttribute(device, "cellspacing", ((intercellSpacing != null) ? ""+intercellSpacing.getWidthInt() : null));
         Utils.optAttribute(device, "cellpadding", ((intercellPadding != null) ? ""+intercellPadding.getHeightInt() : null));
         device.print(">");
         Utils.printNewline(device, table);
 
         STableColumnModel columnModel = table.getColumnModel();
         if (columnModel != null && atLeastOneColumnWidthIsNotNull(columnModel)) {
             if (needsSelectionRow)
                 writeCol(device, selectionColumnWidth);
 
             int columnCount = columnModel.getColumnCount();
             for (int i=0; i < columnCount; i++) {
                 STableColumn column = columnModel.getColumn(i);
                 if (!column.isHidden())
                     writeCol(device, column.getWidth());
             }
         }
 
         /*
         * get viewable area
         */
         int startRow = 0;
         int startCol = 0;
         int endRow = table.getRowCount();
         int endCol = table.getColumnCount();
         final Rectangle viewport = table.getViewportSize();
         if (viewport != null) {
             startRow = viewport.y;
             startCol = viewport.x;
             endRow = Math.min(startRow + viewport.height, endRow);
             endCol = Math.min(startCol + viewport.width, endCol);
         }
 
         /*
         * render the header
         */
         if (table.isHeaderVisible()) {
             SStringBuilder headerArea = Utils.inlineStyles(table.getDynamicStyle(STable.SELECTOR_HEADER));
             device.print("<thead><tr class=\"header\"");
             Utils.optAttribute(device, "style", headerArea);
             device.print(">");
             Utils.printNewline(device, table);
 
             if (needsSelectionRow)
                 device.print("<th width=\"").print(selectionColumnWidth).print("\"></th>");
 
             for (int c = startCol; c < endCol; c++) {
                 writeHeaderCell(device, table, rendererPane, table.convertColumnIndexToModel(c));
             }
 
             device.print("</tr></thead>\n");
         }
 
         SStringBuilder selectedArea = Utils.inlineStyles(table.getDynamicStyle(STable.SELECTOR_SELECTED));
         SStringBuilder evenArea = Utils.inlineStyles(table.getDynamicStyle(STable.SELECTOR_EVEN_ROWS));
         SStringBuilder oddArea = Utils.inlineStyles(table.getDynamicStyle(STable.SELECTOR_ODD_ROWS));
 
         device.print("<tbody>\n");
         for (int r = startRow; r < endRow; r++) {
             SStringBuilder rowClass = new SStringBuilder(table.getRowStyle(r) != null ? table.getRowStyle(r)+" " : "");
             device.print("<tr");
             if (selectionModel.isSelectedIndex(r)){
                 Utils.optAttribute(device, "style", selectedArea);
                     rowClass.append("selected ");
             }
             else if (r % 2 != 0)
                 Utils.optAttribute(device, "style", oddArea);
             else
                 Utils.optAttribute(device, "style", evenArea);
 
                 rowClass.append(r % 2 != 0 ? "odd" : "even");
                 Utils.optAttribute(device, "class", rowClass);
                 device.print(">");
 
                 if (needsSelectionRow) {
                     renderSelectionColumn(device, table, rendererPane, r, showAsFormComponent);
                 }
 
                 for (int c = startCol; c < endCol; c++) {
                     renderCellContent(device, table, rendererPane, r, table.convertColumnIndexToModel(c));
                 }
 
                 device.print("</tr>");
                 Utils.printNewline(device, table);
             }
             device.print("</tbody>");
             device.print("</table>");
         } finally {
             /* Refer to description above. */
            device.flush();
             //device = null;
         }
     }
 
     private boolean atLeastOneColumnWidthIsNotNull(STableColumnModel columnModel) {
         int columnCount = columnModel.getColumnCount();
         for (int i=0; i < columnCount; i++)
             if (columnModel.getColumn(i).getWidth() != null)
                 return true;
         return false;
     }
 
     private void writeCol(Device device, String width) throws IOException {
         device.print("<col");
         Utils.optAttribute(device, "width", width);
         device.print(">");
     }
 
     /**
      * Renders a COLGROUP html element to format the column widths
      */
     protected List determineColumnWidths(final STable table,
                                          final boolean needsSelectionRow, final int startcol, final int endcol) throws IOException {
         final STableColumnModel columnModel = table.getColumnModel();
         final String totalWidth = columnModel.getTotalColumnWidth();
         ArrayList widthStrings = null;
 
         if (totalWidth != null) {
             widthStrings = new ArrayList();
 
             for (int i = startcol; i < endcol; i++) {
                 final STableColumn column = columnModel.getColumn(i);
                 if (column != null && !column.isHidden()) {
                     widthStrings.add(column.getWidth());
                 }
             }
         }
 
         return widthStrings;
     }
 
     /**
      * Renders the row sometimes needed to allow row selection.
      */
     protected void renderSelectionColumn(final Device device, final STable table, final SCellRendererPane rendererPane,
                                          final int row, final boolean showAsFormComponent)
             throws IOException {
         final STableCellRenderer rowSelectionRenderer = table.getRowSelectionRenderer();
         final String columnStyle = Utils.joinStyles((SComponent) rowSelectionRenderer, "numbering");
 
         device.print("<td");
         Utils.optAttribute(device, "class", columnStyle);
         Utils.optAttribute(device, "width", selectionColumnWidth);
         device.print(">");
 
         String value = table.getToggleSelectionParameter(row, -1);
         Utils.printButtonStart(device, table, value, true, table.getShowAsFormComponent());
         device.print(">");
         renderSelectionColumnContent(device, row, table, rendererPane);
         Utils.printButtonEnd(device, table, value, true);
         device.print("</td>");
     }
 
     /**
      * Renders the <b>content</b> of the row selection row.
      */
     private void renderSelectionColumnContent(final Device device, int row, final STable table, final SCellRendererPane rendererPane)
             throws IOException {
         final STableCellRenderer rowSelectionRenderer = table.getRowSelectionRenderer();
         if (rowSelectionRenderer == null) {
             // simple case: just row number
             device.print(row);
         } else {
             // default case: use row selection renderer component
             final SComponent comp = rowSelectionRenderer.getTableCellRendererComponent(table,
                     table.getToggleSelectionParameter(row, -1),
                     table.isRowSelected(row),
                     row, -1);
             rendererPane.writeComponent(device, comp, table);
         }
     }
 
     /**
      * @return The total width for this table or <code>null</code> if none.
      */
     protected SDimension determineTableWidthByColumnModel(final STable table, final boolean needsSelectionRow) {
         if (table.getColumnModel() == null) {
             return null;
         } else {
             String totalWidth = table.getColumnModel().getTotalColumnWidth();
             return totalWidth != null ? new SDimension(totalWidth, null) : null;
         }
     }
 }
