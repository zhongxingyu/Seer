 /*
  * Copyright (C) 2012 Martin Leopold <m@martinleopold.com>
  *
  * This program is free software; you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation; either version 2 of the License, or (at your option) any later
  * version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along with
  * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
  * Place - Suite 330, Boston, MA 02111-1307, USA.
  */
 package com.martinleopold.mode.debug;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.event.TreeExpansionEvent;
 import javax.swing.event.TreeWillExpandListener;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.ExpandVetoException;
 import javax.swing.tree.MutableTreeNode;
 import javax.swing.tree.TreePath;
 import org.netbeans.swing.outline.DefaultOutlineModel;
 import org.netbeans.swing.outline.OutlineModel;
 import org.netbeans.swing.outline.RenderDataProvider;
 import org.netbeans.swing.outline.RowModel;
 
 /**
  * Variable Inspector window.
  *
  * @author Martin Leopold <m@martinleopold.com>
  */
 public class VariableInspector extends javax.swing.JFrame implements TreeWillExpandListener {
 
     protected DefaultMutableTreeNode rootNode;
     protected DefaultTreeModel treeModel;
 //    protected DefaultMutableTreeNode callStackNode;
 //    protected DefaultMutableTreeNode localsNode;
 //    protected DefaultMutableTreeNode thisNode;
 //    protected DefaultMutableTreeNode nonInheritedThisNode;
     protected List<DefaultMutableTreeNode> callStack;
     protected List<VariableNode> locals;
     protected List<VariableNode> thisFields;
     protected List<VariableNode> declaredThisFields;
     protected DebugEditor editor;
     protected Debugger dbg;
 
     /**
      * Creates new form NewJFrame
      */
     public VariableInspector(DebugEditor editor) {
         this.editor = editor;
         this.dbg = editor.dbg();
 
         initComponents();
 
         // setup Outline
         rootNode = new DefaultMutableTreeNode();
         treeModel = (new DefaultTreeModel(rootNode));
         OutlineModel model = DefaultOutlineModel.createOutlineModel(treeModel, new VariableRowModel(), true, "Name");
         model.getTreePathSupport().addTreeWillExpandListener(this);
         tree.setModel(model);
         tree.setRootVisible(false);
         tree.setRenderDataProvider(new OutlineRenderer());
         tree.setColumnHidingAllowed(false); // disable visible columns button (shows by default when right scroll bar is visible)
 
         callStack = new ArrayList();
         locals = new ArrayList();
         thisFields = new ArrayList();
         declaredThisFields = new ArrayList();
 
         this.setTitle("Variable Inspector");
 
 //        for (Entry<Object, Object> entry : UIManager.getDefaults().entrySet()) {
 //            System.out.println(entry.getKey());
 //        }
     }
 
     protected class VariableRowModel implements RowModel {
 
         protected String[] columnNames = {"Value", "Type"};
 
         @Override
         public int getColumnCount() {
             if (p5mode) {
                 return 1; // only show value in p5 mode
             } else {
                 return 2;
             }
         }
 
         @Override
         public Object getValueFor(Object o, int i) {
             if (o instanceof VariableNode) {
                 VariableNode var = (VariableNode) o;
                 switch (i) {
                     case 0:
                         return var.getStringValue();
                     case 1:
                         return var.getTypeName();
                     default:
                         return "";
                 }
             } else {
                 return "";
             }
         }
 
         @Override
         public Class getColumnClass(int i) {
             return String.class;
         }
 
         @Override
         public boolean isCellEditable(Object o, int i) {
             return false;
         }
 
         @Override
         public void setValueFor(Object o, int i, Object o1) {
             // do nothing
         }
 
         @Override
         public String getColumnName(int i) {
             return columnNames[i];
         }
     }
 
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         scrollPane = new javax.swing.JScrollPane();
         tree = new org.netbeans.swing.outline.Outline();
 
         scrollPane.setViewportView(tree);
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 400, Short.MAX_VALUE)
             .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE))
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGap(0, 300, Short.MAX_VALUE)
             .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                 .addComponent(scrollPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 300, Short.MAX_VALUE))
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
 //    /**
 //     * @param args the command line arguments
 //     */
 //    public static void main(String args[]) {
 //        /*
 //         * Set the Nimbus look and feel
 //         */
 //        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
 //        /*
 //         * If Nimbus (introduced in Java SE 6) is not available, stay with the
 //         * default look and feel. For details see
 //         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
 //         */
 //        try {
 //            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
 //        } catch (ClassNotFoundException ex) {
 //            java.util.logging.Logger.getLogger(VariableInspector.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
 //        } catch (InstantiationException ex) {
 //            java.util.logging.Logger.getLogger(VariableInspector.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
 //        } catch (IllegalAccessException ex) {
 //            java.util.logging.Logger.getLogger(VariableInspector.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
 //        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
 //            java.util.logging.Logger.getLogger(VariableInspector.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
 //        }
 //        //</editor-fold>
 //
 //        /*
 //         * Create and display the form
 //         */
 //        run(new VariableInspector());
 //    }
     protected static void run(final VariableInspector vi) {
         /*
          * Create and display the form
          */
         java.awt.EventQueue.invokeLater(new Runnable() {
             @Override
             public void run() {
                 vi.setVisible(true);
             }
         });
     }
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JScrollPane scrollPane;
     protected org.netbeans.swing.outline.Outline tree;
     // End of variables declaration//GEN-END:variables
 
     /**
      * Access the root node of the JTree.
      *
      * @return the root node
      */
     public DefaultMutableTreeNode getRootNode() {
         return rootNode;
     }
 
     // rebuild after this to avoid these ... dots
     public void unlock() {
         tree.setEnabled(true);
     }
 
     public void lock() {
         tree.setEnabled(false);
     }
 
     public void clear() {
         rootNode.removeAllChildren();
         // clear local data for good measure (in case someone rebuilds)
         callStack.clear();
         locals.clear();
         thisFields.clear();
         declaredThisFields.clear();
         // update
         treeModel.nodeStructureChanged(rootNode);
     }
 
     @Override
     public void treeWillExpand(TreeExpansionEvent tee) throws ExpandVetoException {
         //System.out.println("tree expansion: " + tee.getPath());
         Object last = tee.getPath().getLastPathComponent();
         if (!(last instanceof VariableNode)) {
             return;
         }
         VariableNode var = (VariableNode) last;
         // load children
         if (!dbg.isPaused()) {
             throw new ExpandVetoException(tee, "Debugger busy");
         } else {
             var.removeAllChildren(); // TODO: should we only load it once?
             // TODO: don't filter in advanced mode
             //System.out.println("loading children for: " + var);
             // true means include inherited
             var.addChildren(filterNodes(dbg.getFields(var.getValue(), 0, true), new ThisFilter()));
         }
     }
 
     @Override
     public void treeWillCollapse(TreeExpansionEvent tee) throws ExpandVetoException {
         //throw new UnsupportedOperationException("Not supported yet.");
     }
 
     protected class OutlineRenderer implements RenderDataProvider {
 
         protected Icon[][] icons;
 
         public OutlineRenderer() {
             // load icons
             icons = loadIcons("theme/var-icons.gif");
         }
 
         /**
          * Returns an ImageIcon, or null if the path was invalid.
          */
         protected ImageIcon[][] loadIcons(String fileName) {
             DebugMode mode = editor.mode();
             File file = mode.getContentFile(fileName);
             if (!file.exists()) {
                 Logger.getLogger(OutlineRenderer.class.getName()).log(Level.SEVERE, "icon file not found: {0}", file.getAbsolutePath());
                 return null;
             }
             Image allIcons = mode.loadImage(fileName);
             int cols = allIcons.getWidth(null) / ICON_SIZE;
             int rows = allIcons.getHeight(null) / ICON_SIZE;
             ImageIcon[][] iconImages = new ImageIcon[cols][rows];
 
             for (int i = 0; i < cols; i++) {
                 for (int j = 0; j < rows; j++) {
                     //Image image = createImage(ICON_SIZE, ICON_SIZE);
                     Image image = new BufferedImage(ICON_SIZE, ICON_SIZE, BufferedImage.TYPE_INT_ARGB);
                     Graphics g = image.getGraphics();
                     g.drawImage(allIcons, -i * ICON_SIZE, -j * ICON_SIZE, null);
                     iconImages[i][j] = new ImageIcon(image);
                 }
             }
             return iconImages;
         }
 
         protected Icon getIcon(int type, int state) {
             if (type < 0 || type > icons.length - 1) {
                 return null;
             }
             return icons[type][state];
         }
 
         protected VariableNode toVariableNode(Object o) {
             if (o instanceof VariableNode) {
                 return (VariableNode) o;
             } else {
                 return null;
             }
         }
 
         @Override
         public String getDisplayName(Object o) {
             return o.toString(); // VariableNode.toString() returns name; (for sorting)
 //            VariableNode var = toVariableNode(o);
 //            if (var != null) {
 //                return var.getName();
 //            } else {
 //                return o.toString();
 //            }
         }
 
         @Override
         public boolean isHtmlDisplayName(Object o) {
             return false;
         }
 
         @Override
         public Color getBackground(Object o) {
             return null;
         }
 
         @Override
         public Color getForeground(Object o) {
             if (tree.isEnabled()) {
                 return null; // default
             } else {
                 return Color.GRAY;
             }
         }
 
         @Override
         public String getTooltipText(Object o) {
             VariableNode var = toVariableNode(o);
             if (var != null) {
                 return var.description();
             } else {
                 return "";
             }
         }
 
         @Override
         public Icon getIcon(Object o) {
             VariableNode var = toVariableNode(o);
             if (var != null) {
                 if (tree.isEnabled()) {
                     return getIcon(var.getType(), 0);
                 } else {
                     return getIcon(var.getType(), 1);
                 }
             } else {
                 return null;
                 //UIManager.getIcon(o);
             }
         }
     }
     protected static final int ICON_SIZE = 16;
     protected boolean p5mode = true;
 
     public void setAdvancedMode() {
         p5mode = false;
     }
 
     public void setP5Mode() {
         p5mode = true;
     }
 
     public void toggleMode() {
         if (p5mode) {
             setAdvancedMode();
         } else {
             setP5Mode();
         }
     }
 
     public void updateCallStack(List<DefaultMutableTreeNode> nodes, String title) {
         callStack = nodes;
     }
 
     public void updateLocals(List<VariableNode> nodes, String title) {
         locals = nodes;
     }
 
     public void updateThisFields(List<VariableNode> nodes, String title) {
         thisFields = nodes;
     }
 
     public void updateDeclaredThisFields(List<VariableNode> nodes, String title) {
         declaredThisFields = nodes;
     }
 
     public void rebuild() {
         rootNode.removeAllChildren();
         if (p5mode) {
             // add all locals to root
             addAllNodes(rootNode, locals);
 
             // add non-inherited this fields
             addAllNodes(rootNode, filterNodes(declaredThisFields, new LocalHidesThisFilter(locals, LocalHidesThisFilter.MODE_PREFIX)));
 
             // add p5 builtins in a new folder
             DefaultMutableTreeNode builtins = new DefaultMutableTreeNode("Processing");
             addAllNodes(builtins, filterNodes(thisFields, new P5BuiltinsFilter()));
             rootNode.add(builtins);
 
            tree.expandPath(new TreePath(new Object[]{rootNode, builtins}));

             // notify tree (using model) changed a node and its children
            //http://stackoverflow.com/questions/2730851/how-to-update-jtree-elements
             treeModel.nodeStructureChanged(rootNode);
 
             //System.out.println("shown fields: " + rootNode.getChildCount());
 
         } else {
             // TODO: implement advanced mode here
 //            rootNode.add(callStackNode);
 //            rootNode.add(localsNode);
 //            rootNode.add(thisNode);
             // expand top level nodes
             // needs to happen after nodeStructureChanged
 //            tree.expandPath(new TreePath(new Object[]{rootNode, callStackNode}));
 //            tree.expandPath(new TreePath(new Object[]{rootNode, localsNode}));
 //            tree.expandPath(new TreePath(new Object[]{rootNode, thisNode}));
         }
     }
 
     protected List<VariableNode> filterNodes(List<VariableNode> nodes, VariableNodeFilter filter) {
         List<VariableNode> filtered = new ArrayList();
         for (VariableNode node : nodes) {
             if (filter.accept(node)) {
                 filtered.add(node);
             }
         }
         return filtered;
     }
 
     protected void addAllNodes(DefaultMutableTreeNode root, List<? extends MutableTreeNode> nodes) {
         for (MutableTreeNode node : nodes) {
             root.add(node);
         }
     }
 
     public interface VariableNodeFilter {
 
         public boolean accept(VariableNode var);
     }
 
     public class P5BuiltinsFilter implements VariableNodeFilter {
 
         protected String[] p5Builtins = {
             "focused",
             "frameCount",
             "frameRate",
             "height",
             "online",
             "screen",
             "width",
             "mouseX",
             "mouseY",
             "pmouseX",
             "pmouseY",
             "key",
             "keyCode",
             "keyPressed"
         };
 
         @Override
         public boolean accept(VariableNode var) {
             return Arrays.asList(p5Builtins).contains(var.getName());
         }
     }
 
     // filter implicit this reference
     public class ThisFilter implements VariableNodeFilter {
 
         @Override
         public boolean accept(VariableNode var) {
             return !var.getName().startsWith("this$");
         }
     }
 
     public class LocalHidesThisFilter implements VariableNodeFilter {
 
         public static final int MODE_HIDE = 0; // don't show hidden this fields
         public static final int MODE_PREFIX = 1; // prefix hidden this fields with "this."
         protected List<VariableNode> locals;
         protected int mode;
 
         public LocalHidesThisFilter(List<VariableNode> locals, int mode) {
             this.locals = locals;
             this.mode = mode;
         }
 
         @Override
         public boolean accept(VariableNode var) {
             // check if the same name appears in the list of locals i.e. the local hides the field
             for (VariableNode local : locals) {
                 if (var.getName().equals(local.getName())) {
                     switch (mode) {
                         case MODE_PREFIX:
                             var.setName("this." + var.getName());
                             return true;
                         case MODE_HIDE:
                             return false;
                     }
                 }
             }
             return true;
         }
     }
 }
