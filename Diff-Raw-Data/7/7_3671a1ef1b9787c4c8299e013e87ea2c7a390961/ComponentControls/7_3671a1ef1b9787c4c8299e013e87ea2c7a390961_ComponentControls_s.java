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
 import org.wings.border.*;
 import org.wings.style.CSSProperty;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.*;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * @author hengels
  * @version $Revision$
  */
 public class ComponentControls
     extends SPanel
 {
     protected static final Object[] BORDERS = new Object[] {
         new Object[] { "none",    null },
         new Object[] { "raised",  new SBevelBorder(SBevelBorder.RAISED, new Insets(5, 5, 5, 5)) },
         new Object[] { "lowered", new SBevelBorder(SBevelBorder.LOWERED, new Insets(5, 5, 5, 5)) },
         new Object[] { "line",    new SLineBorder(2, new Insets(5, 5, 5, 5)) },
         new Object[] { "grooved", new SEtchedBorder(SEtchedBorder.LOWERED, new Insets(5, 5, 5, 5)) },
         new Object[] { "ridged",  new SEtchedBorder(SEtchedBorder.RAISED, new Insets(5, 5, 5, 5)) },
         new Object[] { "titled",  new STitledBorder(new SEtchedBorder(SEtchedBorder.LOWERED, new Insets(5, 5, 5, 5)), "Border Title") },
     };
 
     protected static final Object[] COLORS = new Object[] {
         new Object[] { "translucent", null },
         new Object[] { "yellow",      Color.YELLOW },
         new Object[] { "red",         Color.RED },
         new Object[] { "green",       Color.GREEN },
         new Object[] { "blue",        Color.BLUE },
     };
 
     protected final List components = new LinkedList();
 
     protected SToolBar globalControls = new SToolBar();
     protected SToolBar localControls = new SToolBar();
     protected final STextField widthTextField = new STextField();
     protected final STextField heightTextField = new STextField();
     protected final STextField insetsTextField = new STextField();
 
     protected final SComboBox borderComboBox = new SComboBox(BORDERS);
     protected final STextField borderThicknessTextField = new STextField();
 
     protected final SComboBox backgroundComboBox = new SComboBox(COLORS);
     protected final SButton applyButton;
 
     public ComponentControls() {
         super(new SGridBagLayout());
        setAttribute(CSSProperty.BORDER_BOTTOM, "1px solid #cccccc");
 
         applyButton = new SButton("Apply");
         applyButton.setName("apply");
         applyButton.setActionCommand("apply");
         applyButton.setHorizontalAlignment(SConstants.CENTER_ALIGN);
         applyButton.setVerticalAlignment(SConstants.CENTER_ALIGN);
 
        SBorder border = new SLineBorder(Color.LIGHT_GRAY, 0);
         border.setThickness(1, SConstants.LEFT);
         globalControls.setBorder(border);
         border = new SLineBorder(Color.LIGHT_GRAY, 0);
         border.setThickness(1, SConstants.LEFT);
         border.setThickness(1, SConstants.TOP);
         localControls.setBorder(border);
         localControls.setVisible(false);
 
         GridBagConstraints c = new GridBagConstraints();
         c.gridwidth = GridBagConstraints.RELATIVE;
         c.gridheight = 2;
         add(applyButton, c);
         c.gridwidth = GridBagConstraints.REMAINDER;
         c.gridheight = 1;
         add(globalControls, c);
         add(localControls, c);
 
         widthTextField.setColumns(5);
         widthTextField.setToolTipText("length with unit (200px)");
         heightTextField.setColumns(5);
         heightTextField.setToolTipText("length with unit (200px)");
         insetsTextField.setColumns(2);
         insetsTextField.setToolTipText("length only");
         borderThicknessTextField.setColumns(2);
         borderThicknessTextField.setToolTipText("length only");
         borderComboBox.setRenderer(new ObjectPairCellRenderer());
         backgroundComboBox.setRenderer(new ObjectPairCellRenderer());
 
         globalControls.add(new SLabel("width "));
         globalControls.add(widthTextField);
         globalControls.add(new SLabel("   height "));
         globalControls.add(heightTextField);
         globalControls.add(new SLabel("    insets "));
         globalControls.add(insetsTextField);
         globalControls.add(new SLabel("   border "));
         globalControls.add(borderComboBox);
         globalControls.add(borderThicknessTextField);
         globalControls.add(new SLabel("   background "));
         globalControls.add(backgroundComboBox);
 
         addActionListener(new wingset.SerializableActionListener() {
             public void actionPerformed(ActionEvent event) {
                 SDimension preferredSize = new SDimension();
                 preferredSize.setWidth(widthTextField.getText());
                 preferredSize.setHeight(heightTextField.getText());
 
                 int insets = 0;
                 try {
                     insets = Integer.parseInt(insetsTextField.getText());
                 }
                 catch (NumberFormatException e) {}
 
                 int borderThickness = 1;
                 try {
                     borderThickness = Integer.parseInt(borderThicknessTextField.getText());
                 }
                 catch (NumberFormatException e) {}
 
                 SBorder border = (SBorder)getSelectedObject(borderComboBox);
                 if (border != null) {
                     border.setInsets(new Insets(insets, insets, insets, insets));
                     border.setThickness(borderThickness);
                 }
 
                 Color background = (Color)getSelectedObject(backgroundComboBox);
 
                 for (Iterator iterator = components.iterator(); iterator.hasNext();) {
                     SComponent component = (SComponent) iterator.next();
                     component.setPreferredSize(preferredSize);
                     component.setBorder(border);
                     component.setBackground(background);
                 }
             }
         });
     }
 
     protected Object getSelectedObject(SComboBox combo) {
         return combo.getSelectedIndex() != -1 ? ((Object[])combo.getSelectedItem())[1] : null;
     }
 
     public void addControl(SComponent component) {
         localControls.add(component);
         localControls.setVisible(true);
     }
 
     public void addSizable(SComponent component) {
         components.add(component);
     }
 
     protected void addActionListener(ActionListener actionListener) {
         applyButton.addActionListener(actionListener);
     }
 
     protected static class ObjectPairCellRenderer extends SDefaultListCellRenderer {
         public SComponent getListCellRendererComponent(SComponent list, Object value, boolean selected, int row) {
             Object[] objects = (Object[])value;
             value = objects[0];
             return super.getListCellRendererComponent(list, value, selected, row);
         }
     }
 }
