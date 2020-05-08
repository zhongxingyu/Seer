 package edu.cmu.cs.diamond.pathfind;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Shape;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.*;
 import javax.swing.border.TitledBorder;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import edu.cmu.cs.diamond.opendiamond.Search;
 
 public class SearchPanel extends JPanel implements ListSelectionListener {
     final protected JList list;
     
     protected Search theSearch;
 
     final private PathFind pathFind;
 
     public SearchPanel(PathFind pf) {
     	/* XXX NEW */
         pathFind = pf;

        setLayout(new BorderLayout());
         
         setBorder(BorderFactory.createTitledBorder("Search Results"));
         
         list = new JList();
         list.setCellRenderer(new SearchPanelCellRenderer());
         list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
         list.setVisibleRowCount(1);
         
        setPreferredSize(new Dimension(100, 280));

         add(new JScrollPane(list,
                 JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                 JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
     }
 
     @Override
     public void setVisible(boolean visible) {
         boolean oldVisible = isVisible();
 
         super.setVisible(visible);
         if (oldVisible != visible && !visible) {
             if (theSearch != null) {
                 theSearch.stop();
             }
 
             // deregister listeners
             deregisterListener();
         }
     }
 
     private void deregisterListener() {
         ListModel oldModel = list.getModel();
         if (oldModel instanceof SearchModel) {
             SearchModel m = (SearchModel) oldModel;
             m.removeSearchListener();
         }
     }
 
     void beginSearch(Search s) {
         if (theSearch != null) {
             theSearch.stop();
         }
 
         theSearch = s;
         
         deregisterListener();
 
         list.setModel(new SearchModel(theSearch, Integer.MAX_VALUE));
 
         theSearch.start();
 
         setVisible(true);
     }
     
     public void valueChanged(ListSelectionEvent e) {
         /* Shape selection = (Shape) list.getSelectedValue();
 
         if (selection == null) {
             setRightSlide(null, null);
         } else {
             setRightSlide(slides[0].getWholeslide(), "Search Result");
             slides[1].setSelection(selection);
         }*/
     }
 /*
     public void valueChanged(ListSelectionEvent e) {
         if (!e.getValueIsAdjusting()) {
             MassResult r = (MassResult) list.getSelectedValue();
             pathFind.setSelectedResult(r);
         }
     } */
 }
