 /*
  * Select the naming preference for graphclasses.
  *
  * $Header: /home/ux/CVSROOT/teo/teo/isgci/gui/SearchDialog.java,v 2.0 2011/09/25 12:37:13 ux Exp $
  *
  * This file is part of the Information System on Graph Classes and their
  * Inclusions (ISGCI) at http://www.graphclasses.org.
  * Email: isgci@graphclasses.org
  */
 
 package teo.isgci.gui;
 
 import java.awt.Container;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Set;
 
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JDialog;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.ListSelectionModel;
 import javax.swing.SwingUtilities;
 
 import teo.isgci.db.Algo;
 import teo.isgci.drawing.DrawingLibraryInterface;
 import teo.isgci.drawing.GraphManipulationInterface;
 import teo.isgci.gc.GraphClass;
 import teo.isgci.util.LessLatex;
 
 public class SearchDialog extends JDialog implements ActionListener {
     protected ISGCIMainFrame parent;
     protected ButtonGroup group;
     protected JCheckBox basicBox, derivedBox, forbiddenBox;
     protected JButton searchButton, cancelButton;
     protected NodeList classesList;
 
     public SearchDialog(ISGCIMainFrame parent) {
         super(parent, "Search for a graphclass", true);
         this.parent = parent;
         group = new ButtonGroup();
 
         Algo.NamePref mode = parent.getTabbedPane().getNamingPref();
 
         Container content = getContentPane();
 
         GridBagLayout gridbag = new GridBagLayout();
         GridBagConstraints c = new GridBagConstraints();
         content.setLayout(gridbag);
 
         c.gridwidth = GridBagConstraints.REMAINDER;
         c.insets = new Insets(5, 5, 5, 5);
         c.weighty = 1.0;
         c.weightx = 1.0;
         c.fill = GridBagConstraints.BOTH;
         classesList = new NodeList();
         classesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         JScrollPane scroller = new JScrollPane(classesList);
         gridbag.setConstraints(scroller, c);
         content.add(scroller);
 
         c.insets = new Insets(0, 5, 0, 0);
         c.weightx = 0.0;
         c.weighty = 0.0;
         c.fill = GridBagConstraints.NONE;
         c.anchor = GridBagConstraints.WEST;
         c.fill = GridBagConstraints.BOTH;
 
         JPanel p = new JPanel();
         searchButton = new JButton("Search");
         searchButton.setToolTipText("Search in database; "
                 + "needs connection to internet");
         cancelButton = new JButton("Cancel");
         cancelButton.setToolTipText("Close this dialogue");
         p.add(searchButton);
         p.add(cancelButton);
         c.insets = new Insets(5, 0, 5, 0);
         gridbag.setConstraints(p, c);
         content.add(p);
 
         searchButton.addActionListener(this);
         cancelButton.addActionListener(this);
         setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
 
         if (parent.getTabbedPane()
                 .getActiveDrawingLibraryInterface() != null) {
             List<Set<GraphClass>> setNames = parent.getTabbedPane()
                     .getActiveDrawingLibraryInterface().getVisibleNodes();
 
             List<GraphClass> listNames = new ArrayList<GraphClass>();
             for (Set<GraphClass> set : setNames) {
                 for (GraphClass graphClass : set) {
                     listNames.add(graphClass);
                 }
             }
             if (!listNames.isEmpty()) {
                 Collections.sort(listNames, new LessLatex());
                 classesList.setListData(listNames);
             }
         }
 
         pack();
         setSize(300, 350);
 
     }
 
     protected void closeDialog() {
         setVisible(false);
         dispose();
     }
 
     public void actionPerformed(ActionEvent event) {
         Object source = event.getSource();
         if (source == cancelButton) {
             closeDialog();
         } else if (source == searchButton
                 && classesList.getSelectedValue() != null) {
             Set<GraphClass> node = null;
             Set setNames = parent.getTabbedPane()
                     .getActiveDrawingLibraryInterface().getGraph().vertexSet();
             List<GraphClass> listNames = new ArrayList<GraphClass>();
             for (Object object : setNames) {
                 Set names = (Set) object;
                 if (names.contains(classesList.getSelectedValue())) {
                     node = (Set<GraphClass>) object;
                 }
             }
 
             DrawingLibraryInterface drawLib
                   = parent.getTabbedPane().getActiveDrawingLibraryInterface();
 
             if (drawLib == null) {
                 return;
             }
 
             final double searchZoomLevel = 2;
 
             final GraphManipulationInterface manipulationInterface
                     = drawLib.getGraphManipulationInterface();
 
             manipulationInterface.beginNotUndoable();
 
             try {
                 if (searchZoomLevel > manipulationInterface.getZoomLevel()) {
                     manipulationInterface.zoomTo(searchZoomLevel);
                 }
 
                 List selectedNodes = new ArrayList();
                 selectedNodes.add(node);
                 drawLib.setSelectedNodes(selectedNodes);
                
 
             } finally {
                 manipulationInterface.endNotUndoable();
             }
 
             // Invoke later to prevent centering bug
             final Set<GraphClass> runnableNode = node;
             SwingUtilities.invokeLater(new Runnable() {
                 @Override
                 public void run() {
                     manipulationInterface.centerNode(runnableNode);
                 }
             });
 
 
             closeDialog();
         }
     }
 }
 
 /* EOF */
