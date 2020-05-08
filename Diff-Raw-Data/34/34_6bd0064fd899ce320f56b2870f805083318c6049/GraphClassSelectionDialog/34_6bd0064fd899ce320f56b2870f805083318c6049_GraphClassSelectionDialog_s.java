 /*
  * Allows the user to select graphclasses for drawing.
  *
  * $Header: /home/ux/CVSROOT/teo/teo/isgci/gui/GraphClassSelectionDialog.java,
  * v 2.0 2011/09/25 12:37:13 ux Exp $
  *
  * This file is part of the Information System on Graph Classes and their
  * Inclusions (ISGCI) at http://www.graphclasses.org.
  * Email: isgci@graphclasses.org
  */
 
 package teo.isgci.gui;
 
import java.awt.BorderLayout;
 import java.awt.Container;
import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.SwingUtilities;
 
 import org.jgrapht.Graphs;
 import org.jgrapht.graph.DefaultEdge;
 import org.jgrapht.graph.SimpleDirectedGraph;
 
 import teo.isgci.db.Algo;
 import teo.isgci.db.DataSet;
 import teo.isgci.gc.GraphClass;
 import teo.isgci.grapht.BFSWalker;
 import teo.isgci.grapht.GAlg;
 import teo.isgci.grapht.GraphWalker;
 import teo.isgci.grapht.Inclusion;
 import teo.isgci.grapht.RevBFSWalker;
 
 /**
  * Display a list of graphclasses and change the drawing according to the
  * selection.
  */
 public class GraphClassSelectionDialog extends JDialog implements
         ActionListener {
 
     protected ISGCIMainFrame parent;
     protected NodeList classesList;
     protected JCheckBox superCheck, subCheck;
     protected JButton addButton, removeButton, newButton, cancelButton,
             newTabButton;
     protected WebSearch search;
  
 
     public GraphClassSelectionDialog(ISGCIMainFrame parent) {
         super(parent, "Select Graph Classes", false);
         this.parent = parent;
 
         Container contents = getContentPane();
         GridBagLayout gridbag = new GridBagLayout();
         GridBagConstraints c = new GridBagConstraints();
         contents.setLayout(gridbag);
 
         c.insets = new Insets(5, 5, 0, 0);
         c.weightx = 0.0;
         c.gridwidth = 1;
         c.anchor = GridBagConstraints.WEST;
         c.fill = GridBagConstraints.BOTH;
         JLabel label = new JLabel("Draw:      ", JLabel.LEFT);
         gridbag.setConstraints(label, c);
         contents.add(label);
 
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
         c.insets = new Insets(5, 5, 0, 5);
         gridbag.setConstraints(search, c);
         contents.add(search);
 
         c.insets = new Insets(5, 5, 5, 5);
         c.weightx = 1.0;
         c.weighty = 1.0;
         c.fill = GridBagConstraints.BOTH;
         classesList = new NodeList();
         JScrollPane scroller = new JScrollPane(classesList);
         gridbag.setConstraints(scroller, c);
         contents.add(scroller);
 
         c.insets = new Insets(0, 5, 0, 0);
         c.weighty = 0.0;
         label = new JLabel("and their", JLabel.LEFT);
         gridbag.setConstraints(label, c);
         contents.add(label);
 
         c.insets = new Insets(0, 5, 0, 0);
         c.fill = GridBagConstraints.NONE;
         c.anchor = GridBagConstraints.WEST;
         superCheck = new JCheckBox("superclasses");
         gridbag.setConstraints(superCheck, c);
         contents.add(superCheck);
 
         subCheck = new JCheckBox("subclasses");
         gridbag.setConstraints(subCheck, c);
         contents.add(subCheck);
 
         JPanel buttonPanel = new JPanel();
         newButton = new JButton("New drawing");
         newButton.setToolTipText("Draw a new hierarchy in this tab; "
                        + "opens a dialogue");
         newTabButton = new JButton("Draw in new Tab");
         newTabButton.setToolTipText("Draw an new hierarchy in a new tab; "
            + "opens a dialogue");
         addButton = new JButton("Add to drawing");
         addButton.setToolTipText("Add the selected item to the "
                + "current drawing");
         removeButton = new JButton("Remove from drawing");
         removeButton.setToolTipText("Remove the selected item "
                   + "from the current drawing");
         cancelButton = new JButton("Cancel");
         cancelButton.setToolTipText("Close this dialogue");
 
         buttonPanel.add(newButton);
         buttonPanel.add(newTabButton);
         // buttonPanel.add(addButton);
         // buttonPanel.add(removeButton);
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
         addButton.addActionListener(this);
         removeButton.addActionListener(this);
         cancelButton.addActionListener(this);
         newTabButton.addActionListener(this);
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
 
     @Override
     public void actionPerformed(ActionEvent event) {
         Object source = event.getSource();
         
         if (source == cancelButton) {
             closeDialog();
         } else if (source == newButton) {
             
             // Disable all buttons
             newButton.setEnabled(false);
             newTabButton.setEnabled(false);
             cancelButton.setEnabled(false);
             
             // Inform user
             newButton.setText("Please wait");
             
             // Create runnable to execute later, so swing repaints the ui first
             Runnable drawGraph = new Runnable() {
 
                 @Override
                public void run() {
                     SimpleDirectedGraph<Set<GraphClass>, DefaultEdge> graph 
                     = getGraph();
                parent.getTabbedPane().drawInActiveTab(graph,
                        classesList.getSelectedValue().toString());
                closeDialog();
                 }
 
             };
             
             SwingUtilities.invokeLater(drawGraph);
             
 
         } else if (source == search) {
             search.setListData(parent, classesList);
         } else if (source == newTabButton) {
             
             // Disable all buttons
             newButton.setEnabled(false);
             newTabButton.setEnabled(false);
             cancelButton.setEnabled(false);
             
             // Inform user
             newTabButton.setText("Please wait");
             
             // Create runnable to execute later, so swing repaints the ui first
             Runnable drawGraph = new Runnable() {
 
                 @Override
                 public void run() {
                     SimpleDirectedGraph<Set<GraphClass>, DefaultEdge> graph 
                    = getGraph();
                parent.getTabbedPane().drawInNewTab(graph,
                        classesList.getSelectedValue().toString());
                closeDialog();
                 }
 
             };
             
             SwingUtilities.invokeLater(drawGraph);
 
         }
     }
 
     /**
      * Generates and returns a SimpleDirectedGraph of GraphClasses.
      * Generation is dependant on what was chosen by the user in 
      * the {@link #classesList} and whether {@link #subCheck} and
      * {@link #superCheck} were checked.
      * 
      * @return
      *          The graph that represents the GraphClass and their sub-
      *          and superclasses.
      */
     private SimpleDirectedGraph<Set<GraphClass>, DefaultEdge> getGraph() {
         Collection<GraphClass> nodes = getNodes();
         
         SimpleDirectedGraph<Set<GraphClass>, DefaultEdge> graph =
                 Algo.createHierarchySubgraph(nodes);
 
             List<SimpleDirectedGraph<Set<GraphClass>, DefaultEdge>> list =
                     GAlg.split(graph, DefaultEdge.class);
 
             SimpleDirectedGraph<Set<GraphClass>, DefaultEdge> superGraph
                 = new SimpleDirectedGraph<Set<GraphClass>, DefaultEdge>(
                         DefaultEdge.class);
             
             for (SimpleDirectedGraph<Set<GraphClass>, DefaultEdge> subGraph 
                     : list) {
                 Graphs.addGraph(superGraph, subGraph);
             }
             
             return superGraph;
     }
     
     /**
      * Returns a Collection with the classes (in DataSet.inclGraph) that are
      * selected by the current settings.
      * 
      * @return
      *          The Collection of GraphClasses based on userselection
      */
     protected final Collection<GraphClass> getNodes() {
         final HashSet<GraphClass> result = new HashSet<GraphClass>();
         boolean doSuper = superCheck.isSelected(), doSub = subCheck
                 .isSelected();
 
         for (Object o : classesList.getSelectedValues()) {
             GraphClass gc = (GraphClass) o;
             result.add(gc);
             if (doSuper) {
                 new RevBFSWalker<GraphClass, Inclusion>(DataSet.inclGraph, gc,
                         null, GraphWalker.InitCode.DYNAMIC) {
                     public void visit(final GraphClass v) {
                         result.add(v);
                         super.visit(v);
                     }
                 } .run();
             }
             if (doSub) {
                 new BFSWalker<GraphClass, Inclusion>(DataSet.inclGraph, gc,
                         null, GraphWalker.InitCode.DYNAMIC) {
                     public void visit(final GraphClass v) {
                         result.add(v);
                         super.visit(v);
                     }
                 } .run();
             }
         }
 
         return result;
     }
 }
 
 /* EOF */
 
