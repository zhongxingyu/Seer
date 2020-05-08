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
 package wingset;
 
 import org.wings.*;
 import org.wings.border.SBorder;
 import org.wings.border.SLineBorder;
 import org.wings.script.JavaScriptListener;
 import org.wings.style.CSSProperty;
 import java.awt.*;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 
 /**
  * A quickhack example to show the capabilities of the dynamic wings layout managers.
  *
  * @author bschmid
  */
 public class DynamicLayoutExample extends WingSetPane {
     private final SPanel[] demoPanels = {new BorderLayoutDemoPanel(),
                                          new FlowLayoutDemoPanel(),
                                          new GridBagDemoPanel(),
                                          new GridLayoutDemoPanel(),
                                          new BoxLayoutDemoPanel()};
     private final static String[] demoManagerNames = {"SBorderLayout",
                                                       "SFlowLayout/SFlowDownLayout",
                                                       "SGridBagLayout",
                                                       "SGridLayout",
                                                       "SBoxLayout"};
     private final SComboBox selectLayoutManager = new SComboBox(demoManagerNames);
     protected SForm panel;
 
     protected SComponent createExample() {
         SToolBar controls = new SToolBar();
        controls.setAttribute(CSSProperty.BORDER_BOTTOM, "1px solid #cccccc");
 
         selectLayoutManager.addItemListener(new ItemListener() {
             public void itemStateChanged(ItemEvent e) {
                 panel.remove(1);
                 panel.add(demoPanels[selectLayoutManager.getSelectedIndex()]);
             }
         });
         selectLayoutManager.addScriptListener(new JavaScriptListener("onChange", "this.form.submit()"));
 
         controls.add(selectLayoutManager);
 
         panel = new SForm(new SBorderLayout());
         panel.add(controls, SBorderLayout.NORTH);
         panel.add(demoPanels[0], SBorderLayout.CENTER);
 
         return panel;
     }
 
     private static class BorderLayoutDemoPanel extends SPanel {
         private int i = 0;
 
         public BorderLayoutDemoPanel() {
             super(new SFlowDownLayout());
 
             add(createDescriptionLabel("The box layout allows to position a component in various regions of a panel: " +
                     "NORTH, SOUTH, EAST, WEST and CENTER.\n" +
                     "Normally these sections are invisible but we encouloured them here for demonstration the component" +
                     "alignment inside the cells."));
 
             add(new SLabel("\nA default SBorderLayout"));
             SPanel borderDemoPanel1 = new SPanel(new SBorderLayout());
             borderDemoPanel1.add(wrapIntoColouredPane(createDummyLabel(0)), SBorderLayout.NORTH);
             borderDemoPanel1.add(wrapIntoColouredPane(createDummyLabel(1)), SBorderLayout.SOUTH);
             borderDemoPanel1.add(wrapIntoColouredPane(createDummyLabel(2)), SBorderLayout.EAST);
             borderDemoPanel1.add(wrapIntoColouredPane(createDummyLabel(3)), SBorderLayout.WEST);
             borderDemoPanel1.add(wrapIntoColouredPane(createDummyLabel(4)), SBorderLayout.CENTER);
             add(borderDemoPanel1);
             borderDemoPanel1.setPreferredSize(new SDimension(800, 150));
             borderDemoPanel1.setBackground(new Color(210, 210, 210));
         }
 
         private SComponent wrapIntoColouredPane(SComponent c) {
             final Color[] colors = {Color.red, Color.green, Color.pink, Color.magenta, Color.gray, Color.cyan};
             SPanel colouredPanel = new SPanel(new SBoxLayout(SBoxLayout.VERTICAL));
             colouredPanel.add(c);
             colouredPanel.setPreferredSize(SDimension.FULLAREA);
             colouredPanel.setVerticalAlignment(c.getVerticalAlignment());
             colouredPanel.setHorizontalAlignment(c.getHorizontalAlignment());
             colouredPanel.setBackground(colors[i++ % colors.length]);
             return colouredPanel;
         }
     }
 
     private static class BoxLayoutDemoPanel extends SPanel {
         public BoxLayoutDemoPanel() {
             super(new SFlowDownLayout());
 
             add(createDescriptionLabel("The box layout is a simple version of gridlayout. It allows you to arrange " +
                     "components in a simple line - either horizontally or vertically aligned. Look at SFlowLayout " +
                     "if you require a wrapping horizontal alignment."));
 
             add(new SLabel("\nHorizontal box layout with padding & border"));
             SBoxLayout horizontalLayout = new SBoxLayout(SBoxLayout.HORIZONTAL);
             horizontalLayout.setHgap(10);
             horizontalLayout.setVgap(10);
             horizontalLayout.setBorder(1);
             add(createPanel(horizontalLayout, 5));
 
             add(new SLabel("\nVertical vanilla box layout"));
             SBoxLayout verticalLayout = new SBoxLayout(SBoxLayout.VERTICAL);
             add(createPanel(verticalLayout, 5));
         }
     }
 
     private static class FlowLayoutDemoPanel extends SPanel {
         public FlowLayoutDemoPanel() {
             super(new SFlowDownLayout());
             add(createDescriptionLabel("Demonstration of flowing layout managers SFlowLayout and SFlowDownLayout\n" +
                     "Try out to resize the browser window to smaller/larger size. These labels below should 'flow'. " +
                     "This means they should break into the next line if the space is not sufficient to display them" +
                     "in the current line.\nPlease note that some component alignments won't work correctly " +
                     "due to restrictions of the rendering engines."));
 
             // SFlowLayout
             add(new SLabel(" "));
             add(createDescriptionLabel("SFlowLayout"));
             add(new SLabel("SFlowLayout - Layout Alignment: default (SFlowLayout.LEFT) - Container alignment: default"));
             add(createPanel(new SFlowLayout(SFlowLayout.LEFT), 4));
             add(new SLabel("\nSFlowLayout - Alignment: SFlowLayout.CENTER - Container alignment: center"));
             final SPanel panel2 = createPanel(new SFlowLayout(SFlowLayout.CENTER), 4);
             panel2.setHorizontalAlignment(CENTER);
             add(panel2);
             add(new SLabel("\nSFlowLayout - Alignment: SFlowLayout.RIGHT - Container alignment: right"));
             final SPanel panel3 = createPanel(new SFlowLayout(SFlowLayout.RIGHT), 4);
             panel3.setHorizontalAlignment(RIGHT);
             add(panel3);
 
 
             // SFlowDownLayout
             add(new SLabel(" "));
             add(createDescriptionLabel("SFlowDownLayout"));
             add(new SLabel("SFlowDownLayout - Container alignment: default"));
             add(createPanel(new SFlowDownLayout(), 4));
             add(new SLabel("\nSFlowDownLayout - Container alignment: center"));
             final SPanel panel4 = createPanel(new SFlowDownLayout(), 3);
             panel4.setHorizontalAlignment(CENTER);
             add(panel4);
             add(new SLabel("\nSFlowDownLayout - Container alignment: right"));
             final SPanel panel5 = createPanel(new SFlowDownLayout(), 4);
             panel5.setHorizontalAlignment(RIGHT);
             add(panel5);
         }
     }
 
     private static class GridLayoutDemoPanel extends SPanel {
         public GridLayoutDemoPanel() {
             super(new SFlowDownLayout());
             add(createDescriptionLabel("The grid layout is a simple way to arrange your components in a tabular manner.\n" +
                     "The example below adds 9 components in a 3-columned grid layout."));
 
             add(new SLabel("\nGrid Layout panel with 3 colums, border, 10px horizontal gap, 40 vertical gap"));
             SGridLayout layout1 = new SGridLayout(3);
             layout1.setBorder(1);
             layout1.setHgap(10);
             layout1.setVgap(40);
 
             add(createPanel(layout1, 12));
         }
     }
 
     private static class GridBagDemoPanel extends SPanel {
         public GridBagDemoPanel() {
             setLayout(new SFlowDownLayout());
 
             add(createDescriptionLabel("The SGridBagLayout is a powerful layout manager to align components " +
                     "in a very complex manner. "));
 
             addRemainderDemo();
             addPredefinedGridXAndXDemo();
             addRandomAddngWithPreDefinedGridXandY();
             addWeightBasedDemo();
             addGridWidthRelativeDemo();
             addGridHeightRelativeDemo();
         }
 
         private void addGridHeightRelativeDemo() {
             SGridBagLayout layout;
             SPanel p;
             GridBagConstraints c;
             add(new SLabel("\nVertical adding with gridheight=RELATIVE"));
             layout = new SGridBagLayout();
             layout.setBorder(1);
             p = new SPanel(layout);
             add(p);
             c = new GridBagConstraints();
 
             c.gridx = 0;
             p.add(new SLabel("1"), c);
             p.add(new SLabel("2"), c);
             p.add(new SLabel("3"), c);
             p.add(new SLabel("4"), c);
             c.gridheight = GridBagConstraints.RELATIVE;
             p.add(new SLabel("5"), c);
             c.gridheight = 1;
             p.add(new SLabel("end #1"), c);
 
             c.gridx = 1;
             p.add(new SLabel("6"), c);
             p.add(new SLabel("7"), c);
             p.add(new SLabel("8"), c);
             c.gridheight = GridBagConstraints.RELATIVE;
             p.add(new SLabel("9"), c);
             c.gridheight = 1;
             p.add(new SLabel("end #2"), c);
 
             c.gridx = 2;
             p.add(new SLabel("10"), c);
             p.add(new SLabel("11"), c);
             c.gridheight = GridBagConstraints.RELATIVE;
             p.add(new SLabel("12"), c);
             c.gridheight = 1;
             p.add(new SLabel("end #3"), c);
 
             c.gridx = 3;
             p.add(new SLabel("13"), c);
             c.gridheight = GridBagConstraints.RELATIVE;
             p.add(new SLabel("14"), c);
             c.gridheight = 1;
             p.add(new SLabel("end #4"), c);
 
             c.gridx = 4;
             c.gridheight = GridBagConstraints.RELATIVE;
             p.add(new SLabel("15"), c);
             c.gridheight = 1;
             p.add(new SLabel("end #5"), c);
         }
 
         private void addGridWidthRelativeDemo() {
             SGridBagLayout layout;
             SPanel p;
             GridBagConstraints c;
             add(new SLabel("\nAdding with gridwidth=RELATIVE"));
             layout = new SGridBagLayout();
             layout.setBorder(1);
             p = new SPanel(layout);
             add(p);
             c = new GridBagConstraints();
             p.add(new SLabel("1"), c);
             p.add(new SLabel("2"), c);
             p.add(new SLabel("3"), c);
             p.add(new SLabel("4"), c);
             c.gridwidth = GridBagConstraints.RELATIVE;
             p.add(new SLabel("5"), c);
             c.gridwidth = 1;
             p.add(new SLabel("end #1"), c);
 
             p.add(new SLabel("6"), c);
             p.add(new SLabel("7"), c);
             p.add(new SLabel("8"), c);
             c.gridwidth = GridBagConstraints.RELATIVE;
             p.add(new SLabel("9"), c);
             c.gridwidth = 1;
             p.add(new SLabel("end #2"), c);
 
             p.add(new SLabel("10"), c);
             p.add(new SLabel("11"), c);
             c.gridwidth = GridBagConstraints.RELATIVE;
             p.add(new SLabel("12"), c);
             c.gridwidth = 1;
             p.add(new SLabel("end #3"), c);
 
             p.add(new SLabel("13"), c);
             c.gridwidth = GridBagConstraints.RELATIVE;
             p.add(new SLabel("14"), c);
             c.gridwidth = 1;
             p.add(new SLabel("end #4"), c);
 
             c.gridwidth = GridBagConstraints.RELATIVE;
             p.add(new SLabel("15"), c);
             c.gridwidth = 1;
             p.add(new SLabel("end #5"), c);
         }
 
         private void addWeightBasedDemo() {
             SGridBagLayout layout;
             SPanel p;
             GridBagConstraints c;
             add(new SLabel("\nUsing weight"));
             layout = new SGridBagLayout();
             layout.setBorder(1);
             p = new SPanel(layout);
             add(p);
             p.setPreferredSize(new SDimension(500, 500));
 
             c = new GridBagConstraints();
             c.gridx = 0;
             c.gridy = 0;
             c.weightx = 0;
             c.weighty = 0;
             p.add(new SLabel("1"), c);
             c.gridx = 1;
             c.gridy = 0;
             c.weightx = 1;
             c.weighty = 0;
             p.add(new SLabel("2"), c);
             c.gridx = 2;
             c.gridy = 0;
             c.weightx = 2;
             c.weighty = 0;
             p.add(new SLabel("3"), c);
             c.gridx = 3;
             c.gridy = 0;
             c.weightx = 1;
             c.weighty = 0;
             p.add(new SLabel("4"), c);
             c.gridx = 4;
             c.gridy = 0;
             c.weightx = 0;
             c.weighty = 0;
             p.add(new SLabel("5"), c);
 
             c.gridx = 0;
             c.gridy = 1;
             c.weightx = 0;
             c.weighty = 1;
             p.add(new SLabel("6"), c);
             c.gridx = 1;
             c.gridy = 1;
             c.weightx = 1;
             c.weighty = 1;
             p.add(new SLabel("7"), c);
             c.gridx = 2;
             c.gridy = 1;
             c.weightx = 2;
             c.weighty = 1;
             p.add(new SLabel("8"), c);
             c.gridx = 3;
             c.gridy = 1;
             c.weightx = 1;
             c.weighty = 1;
             p.add(new SLabel("9"), c);
             c.gridx = 4;
             c.gridy = 1;
             c.weightx = 0;
             c.weighty = 1;
             p.add(new SLabel("10"), c);
 
             c.gridx = 0;
             c.gridy = 2;
             c.weightx = 0;
             c.weighty = 2;
             p.add(new SLabel("11"), c);
             c.gridx = 1;
             c.gridy = 2;
             c.weightx = 1;
             c.weighty = 2;
             p.add(new SLabel("12"), c);
             c.gridx = 2;
             c.gridy = 2;
             c.weightx = 2;
             c.weighty = 2;
             p.add(new SLabel("13"), c);
             c.gridx = 3;
             c.gridy = 2;
             c.weightx = 1;
             c.weighty = 2;
             p.add(new SLabel("14"), c);
             c.gridx = 4;
             c.gridy = 2;
             c.weightx = 0;
             c.weighty = 2;
             p.add(new SLabel("15"), c);
             c.gridx = 0;
             c.gridy = 3;
             c.weightx = 0;
             c.weighty = 1;
             p.add(new SLabel("16"), c);
             c.gridx = 1;
             c.gridy = 3;
             c.weightx = 1;
             c.weighty = 1;
             p.add(new SLabel("17"), c);
             c.gridx = 2;
             c.gridy = 3;
             c.weightx = 2;
             c.weighty = 1;
             p.add(new SLabel("18"), c);
             c.gridx = 3;
             c.gridy = 3;
             c.weightx = 1;
             c.weighty = 1;
             p.add(new SLabel("19"), c);
             c.gridx = 4;
             c.gridy = 3;
             c.weightx = 0;
             c.weighty = 1;
             p.add(new SLabel("20"), c);
             c.gridx = 0;
             c.gridy = 4;
             c.weightx = 0;
             c.weighty = 0;
             p.add(new SLabel("21"), c);
             c.gridx = 1;
             c.gridy = 4;
             c.weightx = 1;
             c.weighty = 0;
             p.add(new SLabel("22"), c);
             c.gridx = 2;
             c.gridy = 4;
             c.weightx = 2;
             c.weighty = 0;
             p.add(new SLabel("23"), c);
             c.gridx = 3;
             c.gridy = 4;
             c.weightx = 1;
             c.weighty = 0;
             p.add(new SLabel("24"), c);
             c.gridx = 4;
             c.gridy = 4;
             c.weightx = 0;
             c.weighty = 0;
             p.add(new SLabel("25"), c);
         }
 
         private void addRandomAddngWithPreDefinedGridXandY() {
             SGridBagLayout layout;
             SPanel p;
             GridBagConstraints c;
             add(new SLabel("\nRandom adding with pre-defined gridx+gridy"));
             layout = new SGridBagLayout();
             layout.setBorder(1);
             p = new SPanel(layout);
             add(p);
 
             c = new GridBagConstraints();
             c.gridx = 4;
             c.gridy = 0;
             p.add(new SLabel("1"), c);
             c.gridx = 3;
             c.gridy = 1;
             p.add(new SLabel("2"), c);
             c.gridx = 2;
             c.gridy = 2;
             p.add(new SLabel("3"), c);
             c.gridx = 1;
             c.gridy = 3;
             p.add(new SLabel("4"), c);
             c.gridx = 0;
             c.gridy = 4;
             p.add(new SLabel("5"), c);
         }
 
         private void addPredefinedGridXAndXDemo() {
             SGridBagLayout layout;
             SPanel p;
             GridBagConstraints c;
             add(new SLabel("\nVertical adding using pre-defined gridx"));
             layout = new SGridBagLayout();
             layout.setBorder(1);
             p = new SPanel(layout);
             add(p);
 
             c = new GridBagConstraints();
             c.gridx = 0;
             c.gridheight = GridBagConstraints.REMAINDER;
             p.add(new SLabel("1"), c);
             c.gridheight = 1;
 
             c.gridx = 1;
             p.add(new SLabel("2"), c);
             c.gridheight = GridBagConstraints.REMAINDER;
             p.add(new SLabel("3"), c);
             c.gridheight = 1;
 
             c.gridx = 2;
             p.add(new SLabel("4"), c);
             p.add(new SLabel("5"), c);
             c.gridheight = GridBagConstraints.REMAINDER;
             p.add(new SLabel("6"), c);
             c.gridheight = 1;
 
             c.gridx = 3;
             p.add(new SLabel("7"), c);
             p.add(new SLabel("8"), c);
             p.add(new SLabel("9"), c);
             c.gridheight = GridBagConstraints.REMAINDER;
             p.add(new SLabel("10"), c);
         }
 
         private void addRemainderDemo() {
             add(new SLabel("\nHorizontal adding using REMAINDER"));
             SGridBagLayout layout = new SGridBagLayout();
             layout.setBorder(1);
             SPanel p = new SPanel(layout);
             p.setPreferredSize(new SDimension(300, 100));
             p.setBackground(Color.red);
             add(p);
 
             GridBagConstraints c = new GridBagConstraints();
             c.gridwidth = GridBagConstraints.REMAINDER;
             p.add(new SLabel("1"), c);
             c.gridwidth = 1;
 
             p.add(new SLabel("2"), c);
             c.gridwidth = GridBagConstraints.REMAINDER;
             p.add(new SLabel("3"), c);
             c.gridwidth = 1;
 
             p.add(new SLabel("4"), c);
             p.add(new SLabel("5"), c);
             c.gridwidth = GridBagConstraints.REMAINDER;
             p.add(new SLabel("6"), c);
             c.gridwidth = 1;
 
             p.add(new SLabel("7"), c);
             p.add(new SLabel("8"), c);
             p.add(new SLabel("9"), c);
             c.gridwidth = GridBagConstraints.REMAINDER;
             p.add(new SLabel("10"), c);
         }
     }
 
     /** Returns a formatted component describing the current layout exampel. */
     private static SComponent createDescriptionLabel(String description) {
         SLabel multiLineLabel = new SLabel();
         //multiLineLabel.setEditable(false);
         multiLineLabel.setWordWrap(true);
         multiLineLabel.setText(description);
         multiLineLabel.setBorder(new SLineBorder(Color.gray, 1));
         multiLineLabel.setBackground(Color.orange);
         return multiLineLabel;
     }
 
     /**
      * Creates a new panel with desired amount of dummy labels on it.
      */
     private static SPanel createPanel(SLayoutManager layout, int amountOfDummyLabels) {
         final SPanel panel = new SPanel(layout);
         panel.setBackground(new Color(210, 210, 210));
         for (int i = 0; i < amountOfDummyLabels; i++) {
             panel.add(createDummyLabel(i));
         }
         return panel;
     }
 
     /* Create a dummy label with a specific label, color, border and alignment depending on index. */
     private static SLabel createDummyLabel(int i) {
         final String[] texts = {"[%] A very short component (Top/Left)",
                                 "[%] A much longer, unbreakable label for wrapping demo (Default)",
                                 "[%] And again a short one (Right/Bottom)",
                                 "[%] A 2-line\nlabel (Center/Center)"};
         final SBorder greenLineBorder = new SLineBorder();
         greenLineBorder.setColor(Color.red);
 
         final SLabel label = new SLabel(texts[i % 4].replace('%', Integer.toString((i + 1)).charAt(0)));
         label.setBorder(greenLineBorder);
         label.setBackground(new Color(0xEE,0xEE,0xEE));
 
         if (i % texts.length == 0) {
             label.setVerticalAlignment(TOP);
             label.setHorizontalAlignment(LEFT);
         } else if (i % texts.length == 1)
             ;
         else if (i % texts.length == 2) {
             label.setHorizontalAlignment(RIGHT);
             label.setVerticalAlignment(BOTTOM);
         } else if (i % texts.length == 3) {
             label.setHorizontalAlignment(CENTER);
             label.setVerticalAlignment(CENTER);
         }
 
         return label;
     }
 
 }
