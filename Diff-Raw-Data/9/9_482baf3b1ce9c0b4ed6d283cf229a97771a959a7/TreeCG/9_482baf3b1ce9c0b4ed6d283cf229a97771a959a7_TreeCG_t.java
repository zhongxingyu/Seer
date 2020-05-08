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
 
 
 import org.wings.*;
 import org.wings.io.Device;
 import org.wings.plaf.CGManager;
 import org.wings.session.SessionManager;
 import org.wings.tree.SDefaultTreeSelectionModel;
 import org.wings.tree.STreeCellRenderer;
 
 import javax.swing.tree.TreeModel;
 import javax.swing.tree.TreePath;
 import java.io.IOException;
 
 public class TreeCG extends AbstractComponentCG implements
         org.wings.plaf.TreeCG {
     private SIcon collapseControlIcon;
     private SIcon emptyFillIcon;
     private SIcon expandControlIcon;
     private SIcon hashMark;
     private SIcon leafControlIcon;
     
     /**
      * Initialize properties from config
      */
     public TreeCG() {
         final CGManager manager = SessionManager.getSession().getCGManager();
 
         setCollapseControlIcon((SIcon) manager.getObject("TreeCG.collapseControlIcon", SIcon.class));
         setEmptyFillIcon((SIcon) manager.getObject("TreeCG.emptyFillIcon", SIcon.class));
         setExpandControlIcon((SIcon) manager.getObject("TreeCG.expandControlIcon", SIcon.class));
         setHashMark((SIcon) manager.getObject("TreeCG.hashMark", SIcon.class));
         setLeafControlIcon((SIcon) manager.getObject("TreeCG.leafControlIcon", SIcon.class));
     }
 
 
     public void installCG(final SComponent comp) {
         super.installCG(comp);
         final STree component = (STree) comp;
         final CGManager manager = component.getSession().getCGManager();
         Object value;
         value = manager.getObject("STree.cellRenderer", STreeCellRenderer.class);
         if (value != null) {
             component.setCellRenderer((STreeCellRenderer) value);
         }
         value = manager.getObject("STree.nodeIndentDepth", Integer.class);
         if (value != null) {
             component.setNodeIndentDepth(((Integer) value).intValue());
         }
     }
 
 //--- code from common area in template.
     private final boolean isLastChild(TreeModel model, TreePath path, int i) {
         if (i == 0)
             return true;
         Object node = path.getPathComponent(i);
         Object parent = path.getPathComponent(i - 1);
         return node.equals(model.getChild(parent, model.getChildCount(parent) - 1));
     }
 
     private void writeIcon(Device device, SIcon icon, int width, int height) throws IOException {
 
         device.print("<img");
         Utils.optAttribute(device, "src", icon.getURL());
         Utils.optAttribute(device, "width", icon.getIconWidth());
         Utils.optAttribute(device, "height", icon.getIconHeight());
         device.print(" alt=\"");
         device.print(icon.getIconTitle());
         device.print("\"/>");
     }
 
     private void writeIcon(Device device, SIcon icon, boolean nullBorder) throws IOException {
         if (icon == null) return;
 
         device.print("<img");
         Utils.optAttribute(device, "src", icon.getURL());
         Utils.optAttribute(device, "width", icon.getIconWidth());
         Utils.optAttribute(device, "height", icon.getIconHeight());
         device.print(" alt=\"");
         device.print(icon.getIconTitle());
         device.print("\"/>");
     }
 
     private void writeTreeNode(STree component, Device device, int row, int depth)
             throws IOException {
         boolean childSelectorWorkaround = !component.getSession().getUserAgent().supportsCssChildSelector();
         final TreePath path = component.getPathForRow(row);
 
         final Object node = path.getLastPathComponent();
         final STreeCellRenderer cellRenderer = component.getCellRenderer();
 
         final boolean isLeaf = component.getModel().isLeaf(node);
         final boolean isExpanded = component.isExpanded(path);
         final boolean isSelected = component.isPathSelected(path);
 
         final boolean isSelectable = (component.getSelectionModel() != SDefaultTreeSelectionModel.NO_SELECTION_MODEL);
 
         SComponent cellComp = cellRenderer.getTreeCellRendererComponent(component, node,
                 isSelected,
                 isExpanded,
                 isLeaf, row,
                 false);
 
         /*
          * now, write the component.
          */
         device.print("<li");
 
         if (isSelected) {
             device.print(" selected=\"true\"");
             if (childSelectorWorkaround)
                 Utils.optAttribute(device, "class", "selected");
         }
         device.print(">");
 
         /*
         * in most applications, the is no need to render a control icon for a
         * leaf. So in that case, we can avoid writing the sourrounding 
         * table, that will speed up things in browsers.
         */
         final boolean renderControlIcon = !(isLeaf && leafControlIcon == null);
 
         if (renderControlIcon) {
             /*
              * This table has to be here so that block level elements can be
              * nodes. I just can't think around it. So it won...
              */
             device.print("<table border=\"0\" class=\"SLayout\"><tr><td class=\"SLayout\">");
 
             if (isLeaf) {
                // render a disabled button around this. firefox position bugfix (ol)
                if (component.getShowAsFormComponent()) {
                    writeButtonStart(component, device, component.getExpansionParameter(row, false));
                    device.print(" disabled=\"disabled\">");
                }
                 writeIcon(device, leafControlIcon, false);
                if (component.getShowAsFormComponent()) {
                    device.print("</button>");
                }
             } else {
                 if (component.getShowAsFormComponent()) {
                     writeButtonStart(component, device, component.getExpansionParameter(row, false));
                     device.print(" type=\"submit\" name=\"");
                     Utils.write(device, Utils.event(component));
                     device.print("\" value=\"");
                     Utils.write(device, component.getExpansionParameter(row, false));
                     device.print("\"");
                 } else {
                     RequestURL selectionAddr = component.getRequestURL();
                     selectionAddr.addParameter(Utils.event(component),
                             component.getExpansionParameter(row, false));
 
                     device.print("<a href=\"");
                     Utils.write(device, selectionAddr.toString());
                     device.print("\"");
                 }
                 device.print(">");
 
                 if (isExpanded) {
                     if (collapseControlIcon == null) {
                         device.print("-");
                     } else {
                         writeIcon(device, collapseControlIcon, true);
                     }
                 } else {
                     if (expandControlIcon == null) {
                         device.print("+");
                     } else {
                         writeIcon(device, expandControlIcon, true);
                     }
                 }
                 if (component.getShowAsFormComponent())
                     device.print("</button>");
                 else
                     device.print("</a>");
             }
             /*
              * closing layout td
              */
             device.print("</td><td class=\"SLayout\">");
 
         }
 
         SCellRendererPane rendererPane = component.getCellRendererPane();
         if (isSelectable) {
             if (component.getShowAsFormComponent()) {
                 writeButtonStart(component, device, component.getSelectionParameter(row, false));
                 device.print(" type=\"submit\" name=\"");
                 Utils.write(device, Utils.event(component));
                 device.print("\" value=\"");
                 Utils.write(device, component.getSelectionParameter(row, false));
                 device.print("\"");
             } else {
                 RequestURL selectionAddr = component.getRequestURL();
                 selectionAddr.addParameter(Utils.event(component),
                         component.getSelectionParameter(row, false));
 
                 writeLinkStart(device, selectionAddr);
             }
 
             Utils.optAttribute(device, "tabindex", component.getFocusTraversalIndex());
             Utils.writeEvents(device, component);
             device.print(">");
 
             rendererPane.writeComponent(device, cellComp, component);
 
             if (component.getShowAsFormComponent())
                 device.print("</button>");
             else
                 device.print("</a>");
         } else {
             rendererPane.writeComponent(device, cellComp, component);
         }
         
         if (renderControlIcon) {
             /*
              * we have to close the table
              */
             device.print("</td></tr></table>\n");
         }
         
 
         //handle the depth level of the tree
         int nextPathCount = 1;
         int pathCount = path.getPathCount();
         TreePath nextPath = component.getPathForRow(row + 1);
         // is there a next element? else use initialized level.
         if (nextPath != null) {
             nextPathCount = nextPath.getPathCount();
         }
         if ( pathCount < nextPathCount ) {
             indentOnce(device, component, path);
         } else if ( pathCount > nextPathCount ) {
             device.print("</li>\n");
             for (int i = nextPathCount; i < pathCount; i++) {
                 device.print("</ul></div></li>\n");
             }
         } else if ( path.getPathCount() == nextPathCount ) {
             device.print("</li>");
         }
     }
 
 
     /**
      * @param device
      * @param selectionAddr
      * @throws IOException
      */
     protected void writeLinkStart(Device device, RequestURL selectionAddr) throws IOException {
         device.print("<a href=\"");
         Utils.write(device, selectionAddr.toString());
         device.print("\"");
     }
 
 
     protected void writeButtonStart(STree component, Device device, String value) throws IOException {
         device.print("<button");
     }
 
 
     public void writeContent(final Device device, final SComponent _c)
             throws IOException {
         final STree component = (STree) _c;
 
         int start = 0;
         int count = component.getRowCount();
 
         java.awt.Rectangle viewport = component.getViewportSize();
         if (viewport != null) {
             start = viewport.y;
             count = viewport.height;
         }
 
         final int depth = component.getMaximumExpandedDepth();
 
         device.print("<ul class=\"STree\"");
         Utils.printCSSInlineFullSize(device, _c.getPreferredSize());
         device.print(">");
         if (start != 0) {
             TreePath path = component.getPathForRow(start);
             indentRecursive(device, component, path.getParentPath());
         }
 
         for (int i = start; i < start + count; ++i) {
             writeTreeNode(component, device, i, depth);
         }
         device.print("</ul>");
     }
 
     /**
      * Recursively indents the Tree in case it isn't displayed from the root
      * node. reversely traverses the {@link TreePath} and renders afterwards. 
      * @param device
      * @param component
      * @param path
      * @throws IOException
      */
     private void indentRecursive(Device device, STree component, TreePath path) throws IOException {
         if (path == null) return;
         indentRecursive(device, component, path.getParentPath());
         device.print("<li>");
         indentOnce(device, component, path);
     }
 
 
     /**
      * Helper method for code reuse
      */
     private void indentOnce(Device device, STree component, TreePath path) throws IOException {
         device.print("<div");
         if (!isLastChild(component.getModel(), path, path.getPathCount()-1)) {
             device.print(" class=\"SSubTree\"");
         }
         device.print("><ul class=\"STree\"");
         device.print(" style=\"margin-left:");
         device.print(component.getNodeIndentDepth());
         device.print("px;\">\n");
     }
 
 
     //--- setters and getters for the properties.
 
     public SIcon getCollapseControlIcon() {
         return collapseControlIcon;
     }
 
     public void setCollapseControlIcon(SIcon collapseControlIcon) {
         this.collapseControlIcon = collapseControlIcon;
     }
 
     public SIcon getEmptyFillIcon() {
         return emptyFillIcon;
     }
 
     public void setEmptyFillIcon(SIcon emptyFillIcon) {
         this.emptyFillIcon = emptyFillIcon;
     }
 
     public SIcon getExpandControlIcon() {
         return expandControlIcon;
     }
 
     public void setExpandControlIcon(SIcon expandControlIcon) {
         this.expandControlIcon = expandControlIcon;
     }
 
     public SIcon getHashMark() {
         return hashMark;
     }
 
     public void setHashMark(SIcon hashMark) {
         this.hashMark = hashMark;
     }
 
     public SIcon getLeafControlIcon() {
         return leafControlIcon;
     }
 
     public void setLeafControlIcon(SIcon leafControlIcon) {
         this.leafControlIcon = leafControlIcon;
     }
 
 }
