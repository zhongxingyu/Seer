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
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 /**
  * @author <a href="mailto:haaf@mercatis.de">Armin Haaf</a>
  * @version $Revision$
  */
 public class LabelExample extends WingSetPane {
     private static final SIcon WAIT_ICON = new SResourceIcon("org/wings/icons/Wait.gif");
     private static final String directions[] = {"nw", "n", "ne", "w", "e", "sw", "s", "se"};
     private static final SIcon onIcons[];
     private static final SIcon offIcons[];
     private ComponentControls controls;
 
     static {
         onIcons = new SIcon[directions.length];
         offIcons = new SIcon[directions.length];
         for (int i = 0; i < directions.length; ++i) {
             String d = directions[i];
             onIcons[i] = new SURLIcon("../icons/Label" + d + "On.gif");
             offIcons[i] = new SURLIcon("../icons/Label" + d + "Off.gif");
         }
     }
 
     public SComponent createExample() {
         controls = new ComponentControls();
 
        final SLabel testLabel = new SLabel("A yellow, bold text");
         testLabel.setHorizontalAlignment(LEFT);
         testLabel.setIcon(WAIT_ICON);
        testLabel.setForeground(Color.YELLOW);
        testLabel.setFont(new SFont("Arial Black",SFont.ITALIC,SFont.FONT+1));
         controls.addSizable(testLabel);
 
         SPanel p = new SPanel(new SGridLayout(3));
         p.add(new SLabel("Control the label's text position"));
         p.add(new SSpacer(100,1));
         p.add(new SLabel("Result"));
         p.add(createRoundRadio(testLabel));
         p.add(new SLabel());
         p.add(testLabel);
 
         SForm form = new SForm(new SBorderLayout());
         form.add(controls, SBorderLayout.NORTH);
         form.add(p, SBorderLayout.CENTER);
         return form;
     }
 
     private SRadioButton createRadio(SPanel p, String constraint,
                                      SButtonGroup buttonGroup,
                                      String toolTip, int icon) {
         SRadioButton button = new SRadioButton();
         button.setIcon(offIcons[icon]);
         button.setSelectedIcon(onIcons[icon]);
         button.setToolTipText(toolTip);
         p.add(button, constraint);
         buttonGroup.add(button);
         return button;
     }
 
     SContainer createRoundRadio(final SLabel label) {
         SPanel b = new SPanel(new SBoxLayout(null, SBoxLayout.Y_AXIS));
 
         SPanel sp = new SPanel(new SBoxLayout(b, SBoxLayout.X_AXIS));
         SButtonGroup g = new SButtonGroup();
         final SRadioButton nw = createRadio(sp, "p=nw", g, "North West", 0);
         final SRadioButton n = createRadio(sp, "p=n", g, "North", 1);
         final SRadioButton ne = createRadio(sp, "p=ne", g, "North East", 2);
         b.add(sp);
 
         SPanel sp1 = new SPanel(new SBoxLayout(b, SBoxLayout.X_AXIS));
 
         final SRadioButton w = createRadio(sp1, "p=w", g, "West", 3);
         SLabel sl = new SLabel();
         sl.setIcon(new SURLIcon("../icons/cowSmall.gif"));
         sp1.add(sl);
         final SRadioButton e = createRadio(sp1, "p=e", g, "East", 4);
         b.add(sp1);
 
         SPanel sp2 = new SPanel(new SBoxLayout(b, SBoxLayout.X_AXIS));
         final SRadioButton sw = createRadio(sp2, "p=sw", g, "South West", 5);
         final SRadioButton s = createRadio(sp2, "p=s", g, "South", 6);
         final SRadioButton se = createRadio(sp2, "p=se", g, "South East", 7);
 
         nw.setShowAsFormComponent(false);
         n.setShowAsFormComponent(false);
         ne.setShowAsFormComponent(false);
         w.setShowAsFormComponent(false);
         e.setShowAsFormComponent(false);
         sw.setShowAsFormComponent(false);
         s.setShowAsFormComponent(false);
         se.setShowAsFormComponent(false);
 
         b.add(sp2);
 
         g.addActionListener(new ActionListener() {
             public void actionPerformed(ActionEvent ev) {
                 Object button = ((SButtonGroup) ev.getSource())
                         .getSelection();
                 if (button == n) {
                     label.setVerticalTextPosition(TOP);
                     label.setHorizontalTextPosition(CENTER);
                 } else if (button == nw) {
                     label.setVerticalTextPosition(TOP);
                     label.setHorizontalTextPosition(LEFT);
                 } else if (button == ne) {
                     label.setVerticalTextPosition(TOP);
                     label.setHorizontalTextPosition(RIGHT);
                 } else if (button == w) {
                     label.setVerticalTextPosition(CENTER);
                     label.setHorizontalTextPosition(LEFT);
                 } else if (button == e) {
                     label.setVerticalTextPosition(CENTER);
                     label.setHorizontalTextPosition(RIGHT);
                 } else if (button == sw) {
                     label.setVerticalTextPosition(BOTTOM);
                     label.setHorizontalTextPosition(LEFT);
                 } else if (button == se) {
                     label.setVerticalTextPosition(BOTTOM);
                     label.setHorizontalTextPosition(RIGHT);
                 } else if (button == s) {
                     label.setVerticalTextPosition(BOTTOM);
                     label.setHorizontalTextPosition(CENTER);
                 }
                 /*
                 else if (button == cc) {
                     label.setVerticalTextPosition(CENTER);
                     label.setHorizontalTextPosition(CENTER);
                 }
                 */
 
             }
         });
 
         n.setSelected(true);
 
         return b;
     }
 
 }
 
 
