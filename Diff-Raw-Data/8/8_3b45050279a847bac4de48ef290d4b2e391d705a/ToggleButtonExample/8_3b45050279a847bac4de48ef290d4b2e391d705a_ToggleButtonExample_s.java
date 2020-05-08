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
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Iterator;
 
 /**
  * @author <a href="mailto:haaf@mercatis.de">Armin Haaf</a>
  * @version $Revision$
  */
 public class ToggleButtonExample
         extends WingSetPane {
     static final ClassLoader cl = WingSet.class.getClassLoader();
     static final SIcon icon = new SURLIcon("../icons/ButtonIcon.gif");
     static final SIcon disabledIcon = new SURLIcon("../icons/ButtonDisabledIcon.gif");
     static final SIcon pressedIcon = new SURLIcon("../icons/ButtonPressedIcon.gif");
     static final SIcon rolloverIcon = new SURLIcon("../icons/ButtonRolloverIcon.gif");
     private ButtonControls controls;
 
     public SComponent createExample() {
         controls = new ButtonControls();
         SContainer p = createButtonExample();
 
         SForm form = new SForm(new SBorderLayout());
         form.add(controls, SBorderLayout.NORTH);
         form.add(p, SBorderLayout.CENTER);
         return form;
     }
 
     SContainer createButtonExample() {
         SButtonGroup group = new SButtonGroup();
         SToggleButton[] buttons = new SToggleButton[9];
 
         for (int i = 0; i < buttons.length; i++) {
            buttons[i] = new SToggleButton("text " + (i + 1));
             buttons[i].setActionCommand("" + (i + 1));
             if (i != 4) {
                 buttons[i].setIcon(icon);
                 buttons[i].setDisabledIcon(disabledIcon);
                 buttons[i].setRolloverIcon(rolloverIcon);
                 buttons[i].setPressedIcon(pressedIcon);
                 buttons[i].setSelectedIcon(pressedIcon);
             }
             buttons[i].setToolTipText("Button " + i);
             buttons[i].setName(buttons[i].getText());
             buttons[i].setShowAsFormComponent(false);
             group.add(buttons[i]);
             controls.addSizable(buttons[i]);
         }
 
         buttons[0].setVerticalTextPosition(SConstants.TOP);
         buttons[0].setHorizontalTextPosition(SConstants.LEFT);
 
         buttons[1].setVerticalTextPosition(SConstants.TOP);
         buttons[1].setHorizontalTextPosition(SConstants.CENTER);
 
         buttons[2].setVerticalTextPosition(SConstants.TOP);
         buttons[2].setHorizontalTextPosition(SConstants.RIGHT);
 
         buttons[3].setVerticalTextPosition(SConstants.CENTER);
         buttons[3].setHorizontalTextPosition(SConstants.LEFT);
 
         buttons[4].setVerticalTextPosition(SConstants.CENTER);
         buttons[4].setHorizontalTextPosition(SConstants.CENTER);
 
         buttons[5].setVerticalTextPosition(SConstants.CENTER);
         buttons[5].setHorizontalTextPosition(SConstants.RIGHT);
 
         buttons[6].setVerticalTextPosition(SConstants.BOTTOM);
         buttons[6].setHorizontalTextPosition(SConstants.LEFT);
 
         buttons[7].setVerticalTextPosition(SConstants.BOTTOM);
         buttons[7].setHorizontalTextPosition(SConstants.CENTER);
 
         buttons[8].setVerticalTextPosition(SConstants.BOTTOM);
         buttons[8].setHorizontalTextPosition(SConstants.RIGHT);
 
         SPanel erg = new SPanel();
 
         SGridLayout grid = new SGridLayout(3, 3);
         grid.setBorder(1);
         SPanel b = new SPanel(grid);
 
         final SLabel pressed = new SLabel("No button pressed");
 
         ActionListener action = new ActionListener() {
             public void actionPerformed(ActionEvent e) {
                 pressed.setText("Button \"" + e.getActionCommand() + "\" pressed");
             }
         };
 
         for (int i = 0; i < buttons.length; i++)
             buttons[i].addActionListener(action);
 
         for (int i = 0; i < buttons.length; i++)
             b.add(buttons[i]);
 
         erg.add(b);
         erg.add(new SLabel("<html><br />"));
         erg.add(pressed);
 
         return erg;
     }
 
     class ButtonControls extends ComponentControls {
         public ButtonControls() {
             final SCheckBox showAsFormComponent = new SCheckBox("Show as Form Component");
             showAsFormComponent.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent e) {
                     for (Iterator iterator = components.iterator(); iterator.hasNext();) {
                         SComponent component = (SComponent) iterator.next();
                         component.setShowAsFormComponent(showAsFormComponent.isSelected());
                     }
                 }
             });
             add(showAsFormComponent);
 
             final SCheckBox useImages = new SCheckBox("Use Icons");
             useImages.setSelected(true);
             useImages.addActionListener(new ActionListener() {
                 public void actionPerformed(ActionEvent e) {
                     boolean use = useImages.isSelected();
 
                     for (Iterator iterator = components.iterator(); iterator.hasNext();) {
                         SAbstractButton component = (SAbstractButton) iterator.next();
                         component.setIcon(use ? icon : null);
                         component.setDisabledIcon(use ? disabledIcon : null);
                         component.setRolloverIcon(use ? rolloverIcon : null);
                         component.setPressedIcon(use ? pressedIcon : null);
                     }
                 }
             });
             add(useImages);
         }
     }
 }
