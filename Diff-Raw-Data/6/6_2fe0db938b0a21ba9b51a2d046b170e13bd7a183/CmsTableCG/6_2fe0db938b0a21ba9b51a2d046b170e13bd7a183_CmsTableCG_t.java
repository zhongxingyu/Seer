 /*
  * Copyright 2000,2005 wingS development team.
  *
  * This file is part of wingS (http://wingsframework.org).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 package org.wings.plaf;
 
 import org.wings.plaf.css.Utils;
 import org.wings.*;
 import org.wings.table.STableCellRenderer;
 import org.wings.io.Device;
 import org.wings.macro.MacroContext;
 import org.wings.macro.MacroContainer;
 import org.wings.macro.MacroTag;
 
 import java.io.IOException;
import java.util.List;
 import java.awt.*;
 
 /**
  * <code>CmsTableCG<code>.
  * <p/>
  * User: raedler
  * Date: 10.08.2007
  * Time: 13:23:45
  *
  * @author raedler
  * @version $Id
  */
 public class CmsTableCG implements TableCG, CmsCG {
 
     private MacroContainer macros;
 
     /**
      * {@inheritDoc}
      */
     public void setMacros(MacroContainer macros) {
         this.macros = macros;
     }
 
     /**
      * {@inheritDoc}
      */
     public Update getTableCellUpdate(STable table, int row, int col) {
         // can be ignored at the moment
         return null;
     }
 
     /**
      * {@inheritDoc}
      */
     public void installCG(SComponent c) {
         // ignore
     }
 
     /**
      * {@inheritDoc}
      */
     public void uninstallCG(SComponent c) {
         // ignore
     }
 
     /**
      * {@inheritDoc}
      */
     public void componentChanged(SComponent c) {
         // can be ignored at the moment
     }
 
     /**
      * {@inheritDoc}
      */
     public void write(Device device, SComponent component) throws IOException {
         macros.execute();
     }
 
     public void writeTableEvent(Device device, SComponent component) throws IOException {
         System.out.println("CmsTableCG.writeTableEvent");
         
         Utils.writeEvents(device, component, null);
     }
 
     public void writeCellEvent(Device device, SComponent _c, Integer row, Integer col) throws IOException {
         System.out.println("CmsTableCG.writeCellEvent");
 
         STable table = (STable) _c;
 
         final boolean isEditingCell =
                 table.isEditing() && row == table.getEditingRow() && col == table.getEditingColumn();
         final boolean editableCell = table.isCellEditable(row, col);
         final boolean selectableCell = table.getSelectionMode() != SListSelectionModel.NO_SELECTION &&
                 !table.isEditable() && table.isSelectable();
 
         final SComponent component;
         if (isEditingCell) {
             component = table.getEditorComponent();
         } else {
             STableCellRenderer renderer = table.getCellRenderer(row.intValue(), col.intValue());
             if (renderer != null) {
                 component = table.prepareRenderer(renderer, row.intValue(), col.intValue());
             }
             else {
                 device.print(table.getValueAt(row.intValue(), col.intValue()));
                 return;
             }
         }
 
         final boolean isClickable = component instanceof SClickable;
 
         String parameter = null;
         if (table.isEditable() && !isEditingCell && editableCell)
             parameter = table.getEditParameter(row.intValue(), col.intValue());
         else if (selectableCell)
             parameter = table.getToggleSelectionParameter(row.intValue(), col.intValue());
 
         if (parameter != null && !isEditingCell && (selectableCell || editableCell) && !isClickable) {
             Utils.printClickability(device, table, parameter, true, table.getShowAsFormComponent());
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public Update getComponentUpdate(SComponent component) {
         // can be ignored at the moment
         return null;
     }
 
     public Update getTableScrollUpdate(STable table, Rectangle newViewport, Rectangle oldViewport) {
         return null;
     }
 
    public Update getSelectionUpdate(STable table, List deselectedIndices, List selectedIndices) {
        return null;
    }

     public Update getEditCellUpdate(STable sTable, int row, int column) {
         return null;
     }
 
     public Update getRenderCellUpdate(STable sTable, int row, int column) {
         return null;
     }
 
     @MacroTag
     public void id(MacroContext context) throws IOException {
         context.getDevice().print("id=\"" + context.getComponent().getName() + "\"");
     }
 
     @MacroTag
     public void toggleSelection(MacroContext context, int row) throws IOException {
         context.getDevice().print("wingS.request.sendEvent(event,true,true,'c','t" + row + ":0;shiftKey='+event.shiftKey+';ctrlKey='+event.ctrlKey+''); return false;");
     }
 
     @MacroTag
     public void cell(MacroContext context, int row, int col) throws IOException {
         throw new UnsupportedOperationException("Currently not supported.");
         /*
         Device device = context.getDevice();
         SComponent component = context.getComponent();
 
         RenderHelper.getInstance(component).forbidCaching();
 
         STable table = (STable) component;
         STableCellRenderer renderer = table.getCellRenderer(row, col);
 
         if (renderer != null) {
             SComponent cellComponent = table.prepareRenderer(renderer, row, col);
             SCellRendererPane rendererPane = table.getCellRendererPane();
             rendererPane.writeComponent(device, cellComponent, table);
         } else {
             device.print(table.getValueAt(row, col));
         }
         RenderHelper.getInstance(component).allowCaching();
         */
     }
 }
