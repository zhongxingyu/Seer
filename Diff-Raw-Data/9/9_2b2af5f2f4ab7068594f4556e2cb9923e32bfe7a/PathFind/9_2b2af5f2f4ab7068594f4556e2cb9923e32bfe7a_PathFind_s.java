 /*
  *  PathFind -- a Diamond system for pathology
  *
  *  Copyright (c) 2008 Carnegie Mellon University
  *  All rights reserved.
  *
  *  PathFind is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, version 2.
  *
  *  PathFind is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with PathFind. If not, see <http://www.gnu.org/licenses/>.
  *
  *  Linking PathFind statically or dynamically with other modules is
  *  making a combined work based on PathFind. Thus, the terms and
  *  conditions of the GNU General Public License cover the whole
  *  combination.
  *
  *  In addition, as a special exception, the copyright holders of
  *  PathFind give you permission to combine PathFind with free software
  *  programs or libraries that are released under the GNU LGPL or the
  *  Eclipse Public License 1.0. You may copy and distribute such a system
  *  following the terms of the GNU GPL for PathFind and the licenses of
  *  the other code concerned, provided that you include the source code of
  *  that other code when and as the GNU GPL requires distribution of source
  *  code.
  *
  *  Note that people who make modified versions of PathFind are not
  *  obligated to grant this special exception for their modified versions;
  *  it is their choice whether to do so. The GNU General Public License
  *  gives permission to release a modified version without this exception;
  *  this exception also makes it possible to release a modified version
  *  which carries forward this exception.
  */
 
 package edu.cmu.cs.diamond.pathfind;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.Rectangle;
 import java.awt.Shape;
 import java.awt.event.ActionEvent;
 import java.io.File;
 import java.util.Random;
 
 import javax.swing.*;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import edu.cmu.cs.diamond.wholeslide.Wholeslide;
 import edu.cmu.cs.diamond.wholeslide.gui.WholeslideView;
 
 public class PathFind extends JFrame {
 
     private final JPanel slideViews;
 
     private final JToggleButton linkButton;
 
     private final JPanel searchResults;
 
     private final JList searchResultsList;
 
     private final WholeslideView slides[] = new WholeslideView[2];
 
     private final JPanel selectionPanel;
 
     private final JList savedSelections;
 
     private DefaultListModel ssModel;
 
     private DefaultListModel srModel;
 
     public PathFind(String filename) {
         super("PathFind");
         setSize(1000, 750);
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
         // link button at bottom
         linkButton = new JToggleButton("Link");
         add(linkButton, BorderLayout.SOUTH);
         linkButton.setVisible(false);
         linkButton.addChangeListener(new ChangeListener() {
            @Override
             public void stateChanged(ChangeEvent e) {
                 if (linkButton.isSelected()) {
                     slides[0].linkWithOther(slides[1]);
                 } else {
                     slides[0].unlinkOther();
                 }
             }
         });
 
         // search results at top
         searchResults = new JPanel(new BorderLayout());
         searchResults.setBorder(BorderFactory
                 .createTitledBorder("Search Results"));
         searchResults.setVisible(false);
         searchResultsList = new JList();
         searchResultsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         searchResultsList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
         searchResultsList.setVisibleRowCount(1);
         searchResults.add(new JScrollPane(searchResultsList,
                 JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                 JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED));
         searchResults.setPreferredSize(new Dimension(100, 200));
         srModel = new DefaultListModel();
         searchResultsList.setModel(srModel);
         searchResultsList.addListSelectionListener(new ListSelectionListener() {
            @Override
             public void valueChanged(ListSelectionEvent e) {
                 Shape selection = (Shape) searchResultsList.getSelectedValue();
 
                 if (selection == null) {
                     setRightSlide(null, null);
                 } else {
                     setRightSlide(slides[0].getWholeslide(), "Search Result");
                     slides[1].setSelection(selection);
                 }
             }
         });
         add(searchResults, BorderLayout.NORTH);
 
         // save searches at left
         selectionPanel = new JPanel(new BorderLayout());
         savedSelections = new JList();
         savedSelections.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
         savedSelections.setLayoutOrientation(JList.VERTICAL);
         selectionPanel.add(new JScrollPane(savedSelections,
                 JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                 JScrollPane.HORIZONTAL_SCROLLBAR_NEVER));
         selectionPanel.setBorder(BorderFactory
                 .createTitledBorder("Saved Selections"));
         selectionPanel.setPreferredSize(new Dimension(280, 100));
         savedSelections.addListSelectionListener(new ListSelectionListener() {
            @Override
             public void valueChanged(ListSelectionEvent e) {
                 Shape selection = (Shape) savedSelections.getSelectedValue();
                 slides[0].setSelection(selection);
             }
         });
         add(selectionPanel, BorderLayout.WEST);
 
         // main view in center
         slideViews = new JPanel(new GridLayout(1, 2));
         add(slideViews);
         setLeftSlide(new Wholeslide(new File(filename)), filename);
 
         // fake menus for now
         JMenuBar mb = new JMenuBar();
         setJMenuBar(mb);
 
         JMenu searchMenu = new JMenu("Search");
         JMenuItem searchMenuItem = new JMenuItem("ImageJ Search");
         searchMenuItem.addActionListener(new AbstractAction() {
            @Override
             public void actionPerformed(ActionEvent e) {
                 doImageJSearch();
             }
         });
 
         searchMenu.add(searchMenuItem);
         mb.add(searchMenu);
     }
 
     protected void doImageJSearch() {
         Random rand = new Random();
 
         searchResults.setVisible(true);
 
         for (int i = 0; i < 40; i++) {
             Rectangle r = new Rectangle(rand.nextInt(40000), rand
                     .nextInt(20000), rand.nextInt(1000), rand.nextInt(1000));
             srModel.addElement(r);
         }
     }
 
     private void setLeftSlide(Wholeslide wholeslide, String title) {
         WholeslideView oldSlide = slides[0];
         if (oldSlide != null) {
             oldSlide.unlinkOther();
             oldSlide.getActionMap().put("save selection", null);
         }
         final WholeslideView wv = createNewView(wholeslide, title);
 
         slides[0] = wv;
         slideViews.add(wv, 0);
         wv.getInputMap()
                 .put(KeyStroke.getKeyStroke("INSERT"), "save selection");
         wv.getActionMap().put("save selection", new AbstractAction() {
            @Override
             public void actionPerformed(ActionEvent e) {
                 saveSelection(wv);
             }
         });
         ssModel = new SavedSelectionModel(wv);
         savedSelections.setModel(ssModel);
         savedSelections.setCellRenderer(new SavedSelectionCellRenderer(wv));
         searchResultsList.setCellRenderer(new SavedSelectionCellRenderer(wv));
     }
 
     private void setRightSlide(Wholeslide wholeslide, String title) {
         WholeslideView oldView = slides[1];
         linkButton.setSelected(false);
         if (oldView != null) {
             oldView.unlinkOther();
             slideViews.remove(oldView);
         }
 
         if (wholeslide == null) {
             linkButton.setVisible(false);
         } else {
             slides[1] = createNewView(wholeslide, title);
             slideViews.add(slides[1]);
             linkButton.setVisible(true);
         }
         
         slideViews.revalidate();
     }
 
     protected void saveSelection(WholeslideView wv) {
         Shape s = wv.getSelection();
         if (s != null) {
             ssModel.addElement(s);
         }
     }
 
     private WholeslideView createNewView(Wholeslide wholeslide, String title) {
         WholeslideView wv = new WholeslideView(wholeslide);
         wv.setBorder(BorderFactory.createTitledBorder(title));
         return wv;
     }
 
     public static void main(String[] args) {
         PathFind pf = new PathFind(args[0]);
         pf.setVisible(true);
     }
 }
