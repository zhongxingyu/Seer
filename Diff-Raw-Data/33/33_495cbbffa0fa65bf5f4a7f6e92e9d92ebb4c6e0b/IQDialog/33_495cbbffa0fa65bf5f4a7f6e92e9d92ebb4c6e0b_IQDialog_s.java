 /*
  * Allows the user to select graphclasses for drawing using an IQ expression.
  *
  * $Header: /home/ux/CVSROOT/teo/teo/isgci/gui/GraphClassSelectionDialog.java,v 2.0 2011/09/25 12:37:13 ux Exp $
  *
  * This file is part of the Information System on Graph Classes and their
  * Inclusions (ISGCI) at http://www.graphclasses.org.
  * Email: isgci@graphclasses.org
  */
 
 
 package teo.isgci.gui;
 
 import teo.isgci.gc.GraphClass;
 import teo.isgci.db.DataSet;
 import teo.isgci.grapht.*;
 import teo.isgci.util.LatexGlyph;
 import java.io.IOException;
 import java.awt.Cursor;
 import java.awt.GridBagLayout;
 import java.awt.GridBagConstraints;
 import java.awt.Insets;
 import java.awt.Container;
 import java.awt.event.*;
 import javax.swing.*;
 import java.util.HashSet;
 import java.util.Collection;
 
 
 /**
  * Display a list of graphclasses and change the drawing according to the
  * selection.
  */
 public class IQDialog extends JDialog
         implements ActionListener {
     
     protected ISGCIMainFrame parent;
     protected NodeList classesList;
     protected JButton newButton, cancelButton;
     protected JButton leButton, ltButton, eqButton, gtButton, geButton;
     protected WebSearch search;
 
     public IQDialog(ISGCIMainFrame parent) {
         super(parent, "Select Graph Classes to draw", false);
         this.parent = parent;
 
         JLabel label;
         Container contents = getContentPane();
         GridBagLayout gridbag = new GridBagLayout();
         GridBagConstraints c = new GridBagConstraints();
         contents.setLayout(gridbag);
 
         //---- and/or ----
         c.insets = new Insets(5, 5, 0, 0);
         c.weightx = 0.0;
         c.anchor = GridBagConstraints.WEST;
         c.fill = GridBagConstraints.BOTH;
         c.gridwidth = GridBagConstraints.REMAINDER;
         label = new JLabel("Connect to previous phrase with", JLabel.LEFT);
         gridbag.setConstraints(label, c);
         contents.add(label);
 
         //---- List of graph classes
         label = new JLabel("     Filter:", JLabel.RIGHT);
         c.anchor = GridBagConstraints.EAST;
         c.gridwidth = 1;
         gridbag.setConstraints(label, c);
         contents.add(label);
 
         search = new WebSearch();
         search.addActionListener(this);
         c.weightx = 1.0;
         c.fill = GridBagConstraints.BOTH;
         c.gridwidth = GridBagConstraints.REMAINDER;
         c.anchor = GridBagConstraints.CENTER;
         c.insets = new Insets(5,5,0,5);
         gridbag.setConstraints(search, c);
         contents.add(search);
 
         c.insets = new Insets(5, 5, 5, 5);
         c.weightx = 1.0;
         c.weighty = 1.0;
         c.fill = GridBagConstraints.BOTH;
         classesList = new NodeList(parent.latex);
         JScrollPane scroller = new JScrollPane(classesList);
         gridbag.setConstraints(scroller, c);
         contents.add(scroller);
 
         //---- Relation buttons
         JPanel panel = new JPanel();
         ltButton = new JButton(LatexGlyph.getGlyph("subset").getUnicode());
         leButton = new JButton(LatexGlyph.getGlyph("subseteq").getUnicode());
         eqButton = new JButton(LatexGlyph.getGlyph("equiv").getUnicode());
         gtButton = new JButton(LatexGlyph.getGlyph("supset").getUnicode());
         geButton = new JButton(LatexGlyph.getGlyph("supseteq").getUnicode());
         panel.add(ltButton);
         panel.add(leButton);
         panel.add(eqButton);
         panel.add(gtButton);
         panel.add(geButton);
 
         c.insets = new Insets(5, 0, 5, 0);
         c.weighty = 0.0;
         gridbag.setConstraints(panel, c);
         contents.add(panel);
 
         /*c.insets = new Insets(0, 5, 0, 0);
         c.fill = GridBagConstraints.NONE;
         c.anchor = GridBagConstraints.WEST;
         superCheck = new JCheckBox("superclasses");
         gridbag.setConstraints(superCheck, c);
         contents.add(superCheck);
 
         subCheck = new JCheckBox("subclasses");
         gridbag.setConstraints(subCheck, c);
         contents.add(subCheck);*/
 
         JPanel buttonPanel = new JPanel();
         newButton = new JButton("New drawing");
         cancelButton = new JButton("Cancel");
         buttonPanel.add(newButton);
         buttonPanel.add(cancelButton);
         c.insets = new Insets(5, 0, 5, 0);
         c.fill = GridBagConstraints.BOTH;
         gridbag.setConstraints(buttonPanel, c);
         contents.add(buttonPanel);
         addListeners();
         setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
 
         classesList.setListData(DataSet.getClasses());
         pack();
         setSize(500, 400);
     }
 
 
     protected void addListeners() {
         newButton.addActionListener(this);
         cancelButton.addActionListener(this);
     }
 
     protected void closeDialog() {
         setVisible(false);
         dispose();
     }
 
     /**
      * Select the given node.
      */
     public void select(GraphClass node) {
         classesList.setSelectedValue(node, true);
     }
 
     public void actionPerformed(ActionEvent event) {
         Object source = event.getSource();
         if (source == cancelButton) {
             closeDialog();
         } else if (source == newButton) {
          // TODO jannis
//            Cursor oldcursor = parent.getCursor();
//            setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
//            parent.getActiveCanvas().drawHierarchy(getNodes());
//            
 //            for (Object o : classesList.getSelectedValues()) {
 //                GraphClass gc = (GraphClass) o;
 //                NodeView v = parent.getActiveCanvas().findNode(gc);
 //                if (v != null)
 //                    v.setNameAndLabel(gc.toString());
 //            }
//            parent.getActiveCanvas().updateBounds();
//            
//            setCursor(oldcursor);
             closeDialog();
         } else if (source == search) {
             search.setListData(parent, classesList);
         }
 
     }
     
     
     /**
      * Returns a Collection with the classes (in DataSet.inclGraph) that are
      * selected by the current settings.
      */
     protected Collection<GraphClass> getNodes() {
         final HashSet<GraphClass> result = new HashSet<GraphClass>();
         /*boolean doSuper = superCheck.isSelected(),
                 doSub = subCheck.isSelected();
        
         for (Object o : classesList.getSelectedValues()) {
             GraphClass gc = (GraphClass) o;
             result.add(gc);
             if (doSuper) {
                 new RevBFSWalker<GraphClass,Inclusion>( DataSet.inclGraph,
                         gc, null, GraphWalker.InitCode.DYNAMIC) {
                     public void visit(GraphClass v) {
                         result.add(v);
                         super.visit(v);
                     }
                 }.run();
             }
             if (doSub) {
                 new BFSWalker<GraphClass,Inclusion>(DataSet.inclGraph,
                         gc, null, GraphWalker.InitCode.DYNAMIC) {
                     public void visit(GraphClass v) {
                         result.add(v);
                         super.visit(v);
                     }
                 }.run();
             }
         }*/
 
         return result;
     }    
 }
 
 
 /* EOF */
 
