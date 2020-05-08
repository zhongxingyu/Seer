 /*
  * Displays information about a given graphclass.
  *
  * $Header: /home/ux/CVSROOT/teo/teo/isgci/gui/GraphClassInformationDialog.java,v 2.1 2011/09/29 08:38:57 ux Exp $
  *
  * This file is part of the Information System on Graph Classes and their
  * Inclusions (ISGCI) at http://www.graphclasses.org.
  * Email: isgci@graphclasses.org
  */
 
 package teo.isgci.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Vector;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.AbstractTableModel;
 
 import teo.isgci.db.Algo;
 import teo.isgci.db.DataSet;
 import teo.isgci.gc.GraphClass;
 import teo.isgci.problem.Problem;
 import teo.isgci.util.LessLatex;
 
 
 /**
  * Display a list of graphclasses and for the selected class also its super-,
  * sub- and equivalent classes.
  */
 public class GraphClassInformationDialog extends JDialog
         implements ActionListener, ListSelectionListener {
 
     protected ISGCIMainFrame parent;
     protected NodeList classesList;
     protected NodeList subClassesList, supClassesList, equClassesList;
     protected ListGroup lists;
     protected JLabel complexity;
     protected JTable problems;
     protected JButton okButton, classButton, inclButton, drawButton;
     protected WebSearch search;
     protected MouseAdapter mouseAdapter;
 
     public GraphClassInformationDialog(ISGCIMainFrame parent) {
         this(parent, null);
     }
 
     public GraphClassInformationDialog(ISGCIMainFrame parent,
             GraphClass target) {
         super(parent, "Graph Class Information", false);
         this.parent = parent;
         setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 
         Container contents = getContentPane();
         Dimension listdim = new Dimension(150, 150);
         JPanel p;
         JLabel label;
 
         GridBagLayout gridbag = new GridBagLayout();
         GridBagConstraints c = new GridBagConstraints();
         contents.setLayout(gridbag);
         c.weightx = 0.0;
         c.weighty = 0.0;
         c.fill = GridBagConstraints.BOTH;
 
         //---- Graph class ----
         c.weightx = 0.0;
         c.weighty = 0.0;
         c.fill = GridBagConstraints.NONE;
         c.gridwidth = 1;
         c.anchor = GridBagConstraints.WEST;
         c.insets = new Insets(5, 5, 0, 0);
         label = new JLabel("Graph Class:", JLabel.LEFT);
         gridbag.setConstraints(label, c);
         contents.add(label);
 
         //---- Filter ----
         label = new JLabel("Filter: ", JLabel.RIGHT);
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
 
         //---- Graph Class list ----
         c.insets = new Insets(5, 5, 5, 5);
         c.weightx = 1.0;
         c.weighty = 1.0;
         c.fill = GridBagConstraints.BOTH;
         c.gridwidth = GridBagConstraints.REMAINDER;
         classesList = new NodeList();
         classesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         JScrollPane scroller = new JScrollPane(classesList);
         scroller.setPreferredSize(listdim);
         scroller.setMinimumSize(listdim);
         gridbag.setConstraints(scroller, c);
         contents.add(scroller);
 
         //---- Complexity ----
         p = new JPanel(new BorderLayout());
         problems = new JTable(new ProblemsModel());
         p.add(problems.getTableHeader(), BorderLayout.NORTH);
         p.add(problems, BorderLayout.SOUTH);
         problems.setShowVerticalLines(false);
         problems.getTableHeader().setFont(label.getFont());
         problems.setBorder(
                 BorderFactory.createMatteBorder(0,1,0,1,Color.black));
         c.gridwidth = 4;
         c.weighty = 0.0;
         c.insets = new Insets(5, 5, 10, 5);
         gridbag.setConstraints(p, c);
         contents.add(p);
         
         // Fold line
         p = new JPanel();
         c.gridwidth = GridBagConstraints.REMAINDER;
         c.insets = new Insets(0, 0, 0, 0);
         gridbag.setConstraints(p, c);
         contents.add(p);
 
         //---- Sub/super/equ classes ----
         c.gridwidth = 2;
         c.insets = new Insets(0, 5, 0, 0);
         c.weighty = 0.0;
         JLabel superLabel = new JLabel("Superclasses:", JLabel.LEFT);
         gridbag.setConstraints(superLabel, c);
         contents.add(superLabel);
 
         JLabel equLabel = new JLabel("Equivalent Classes:", JLabel.LEFT);
         gridbag.setConstraints(equLabel, c);
         contents.add(equLabel);
 
         c.gridwidth = GridBagConstraints.REMAINDER;
         JLabel subLabel = new JLabel("Subclasses:", JLabel.LEFT);
         gridbag.setConstraints(subLabel, c);
         contents.add(subLabel);
 
         c.insets = new Insets(0, 5, 5, 5);
         c.gridwidth = 2;
         c.weighty = 1.0;
         supClassesList = new NodeList();
         scroller = new JScrollPane(supClassesList);
         scroller.setPreferredSize(listdim);
         scroller.setMinimumSize(listdim);
         gridbag.setConstraints(scroller, c);
         contents.add(scroller);
 
         equClassesList = new NodeList();
         scroller = new JScrollPane(equClassesList);
         scroller.setPreferredSize(listdim);
         scroller.setMinimumSize(listdim);
         gridbag.setConstraints(scroller, c);
         contents.add(scroller);
 
         c.gridwidth = GridBagConstraints.REMAINDER;
         subClassesList = new NodeList();
         scroller = new JScrollPane(subClassesList);
         scroller.setPreferredSize(listdim);
         scroller.setMinimumSize(listdim);
         gridbag.setConstraints(scroller, c);
         contents.add(scroller);
 
         lists = new ListGroup(3);
         lists.add(subClassesList);
         lists.add(supClassesList);
         lists.add(equClassesList);
 
         //---- Buttons ----
         JPanel okPanel = new JPanel();
         classButton = new JButton("Class details");
         classButton.setToolTipText("Show more information about this " 
                      + "graphclass");
         okPanel.add(classButton);
         
         inclButton = new JButton("Inclusion info");
         inclButton.setToolTipText("Show inclusions of selected "
          + "graphclasses; needs a class from the lower listbox "
          + "to be selected!");
         inclButton.setEnabled(false);
         okPanel.add(inclButton);
         
         drawButton = new JButton("Draw");
         drawButton.setToolTipText("Draw this graphclass and its "
     + "subclasses/superclasses; opens a dialogue");
         okPanel.add(drawButton);
         
         okButton = new JButton("Close");
         okButton.setToolTipText("Close this dialogue");
         okPanel.add(okButton);
         
         c.weighty = 0.0;
         c.insets = new Insets(5, 0, 0, 0);
         c.gridwidth = GridBagConstraints.REMAINDER;
         gridbag.setConstraints(okPanel, c);
         contents.add(okPanel);
 
         classesList.setListData(DataSet.getClasses());
 
         mouseAdapter = new MouseAdapter() {
             public void mouseClicked(MouseEvent e) {
                 if (e.getClickCount() == 2) {
                     NodeList list = (NodeList) e.getSource();
                     showNode(list.getSelectedNode());
                 }
             }
         };
         classButton.addActionListener(this);
         inclButton.addActionListener(this);
         okButton.addActionListener(this);
         drawButton.addActionListener(this);
         classesList.addListSelectionListener(this);
         supClassesList.addListSelectionListener(this);
         subClassesList.addListSelectionListener(this);
         equClassesList.addListSelectionListener(this);
         supClassesList.addMouseListener(mouseAdapter);
         equClassesList.addMouseListener(mouseAdapter);
         subClassesList.addMouseListener(mouseAdapter);
 
         if (target != null) {
             showNode(target);
         } else {
             showNode();
         }
     }
 
 
     /**
      * Show the information about the given class.
      */
     private void showNode(GraphClass target) {
        if (target == null) {
            return;
        }
        
         classesList.setSelectedValue(target, true);
         updateLists(target);
         updateComplexity(target);
     }
 
     /**
      * Show the information abot the first class in the list.
      */
     private void showNode() {
         GraphClass node = null;
         if (classesList.getElementCount() > 0) {
             node = (GraphClass) classesList.getModel().getElementAt(0);
         }
         showNode(node);
         repaint();
     }
 
 
     /**
      * Update the information in the complexity label.
      */
     private void updateComplexity(GraphClass target) {
         ((ProblemsModel) problems.getModel()).setNode(target);
     }
 
 
     /**
      * Update the information displayed in the super/sub/equiv lists.
      * @param target graph classe of which to display the information
      */
     private synchronized void updateLists(GraphClass target) {
         if (target == null) {
             Vector<?> empty = new Vector<Object>();
             subClassesList.setListData(empty);
             supClassesList.setListData(empty);
             equClassesList.setListData(empty);
             return;
         }
 
         ArrayList<GraphClass> sup = Algo.superNodes(target);
         ArrayList<GraphClass> sub = Algo.subNodes(target);
         ArrayList<GraphClass> equ = Algo.equNodes(target);
 
         sup.removeAll(equ);
         sub.removeAll(equ);
 
         supClassesList.setListData(sup);
         subClassesList.setListData(sub);
 
         Collections.sort(equ, new LessLatex());
         equClassesList.setListData(equ);
     }
 
 
     protected void closeDialog() {
         setVisible(false);
         dispose();
     }
 
     @Override
     public void valueChanged(ListSelectionEvent e) {
         if (e.getValueIsAdjusting()) {
             return;
         }
          // subClassesList, supClassesList, equClassesList;
         if (e.getSource() == subClassesList) {
             inclButton.setEnabled(true);
         } else if (e.getSource() == supClassesList) {
             inclButton.setEnabled(true);
         } else if (e.getSource() == equClassesList) {
             inclButton.setEnabled(true);
         } else if (e.getSource() == classesList
                 && classesList.getSelectedNode() != null) {
             showNode(classesList.getSelectedNode());
             inclButton.setEnabled(false);
         }
     }
 
 
     @Override
     public void actionPerformed(ActionEvent event) {
         Object source = event.getSource();
         
         if (source == okButton) {
             closeDialog();
         } else if (source == classButton) {
             parent.loader.showDocument("classes/"
                 + classesList.getSelectedNode().getID() + ".html");
         } else if (source == inclButton) {
             GraphClass c1 = classesList.getSelectedNode();
             GraphClass c2 = lists.getSelectedNode();
             if (c1 != null && c2 != null) {
                 JDialog dia = InclusionResultDialog
                         .newInstance(parent, c1, c2);
                 dia.setVisible(true);
             }
         } else if (source == drawButton) {
             GraphClassSelectionDialog draw = 
                 new GraphClassSelectionDialog(parent);
             draw.select(classesList.getSelectedNode());
             draw.setVisible(true);
             closeDialog();
         } else if (source == search) {
             search.setListData(parent, classesList);
             showNode();
         }
     }
 
 }
 
 /**
  * The model for the problem - complexity table in the dialogue.
  */
 class ProblemsModel extends AbstractTableModel {
     private static String[] colNames = {"Problem", "Complexity"};
     private GraphClass gc;
 
     public ProblemsModel() {
         super();
         gc = null;
     }
 
 
     public int getColumnCount() {
         return 2;
     }
 
     public int getRowCount() {
         return DataSet.problems.size();
     }
 
     public String getColumnName(int col) {
         return colNames[col];
     }
 
     public Object getValueAt(int row, int col) {
         if (row < 0  ||  row >= DataSet.problems.size()  ||
                 col < 0  ||  col > 1 ||
                 gc == null) {
             return "???";
         }
 
         Problem p = DataSet.problems.elementAt(row);
         if (col == 0) {
             return p.getName();
         } else if (col == 1) {
             return p.getComplexityString(p.getComplexity(gc));
         }
 
         return "???";
     }
 
     void setNode(GraphClass n) {
         gc = n;
         fireTableDataChanged();
     }
 }
 
 /* EOF */
