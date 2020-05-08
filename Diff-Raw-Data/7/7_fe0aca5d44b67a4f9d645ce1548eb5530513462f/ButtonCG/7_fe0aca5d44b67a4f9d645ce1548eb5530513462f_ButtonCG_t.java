 /*
  * Copyright 2000,2005 wingS development team.
  *
  * This file is part of wingS (http://wingsframework.org).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 package org.wings.plaf.css;
 
 
 import org.wings.*;
 import org.wings.plaf.Update;
 import org.wings.util.SStringBuilder;
 import org.wings.io.Device;
 
 import java.io.IOException;
 
 public class ButtonCG extends AbstractLabelCG implements org.wings.plaf.ButtonCG {
 
     /**
      * a serializable class is supposed to have this ID.
      */
     private static final long serialVersionUID = -1794530181411426283L;
 
     public void writeInternal(final Device device, final SComponent component)
             throws IOException {
         final SAbstractButton button = (SAbstractButton) component;
 
         String text = button.getText();
         final SIcon icon = getIcon(button);
 
         if (icon == null && text == null)
             text = "";
         final String ftext = text;
 
         if (icon == null) {
             device.print("<table");
             tableAttributes(device, button);
             device.print("><tr><td");
             PaddingVoodoo.doSimpleTablePaddingWorkaround(device, button);
             device.print(">");
            writeText(device, text, button.isWordWrap());
             device.print("</td></tr></table>");
         }
         else if (text == null) {
             device.print("<table");
             tableAttributes(device, button);
             device.print("><tr><td");
             PaddingVoodoo.doSimpleTablePaddingWorkaround(device, button);
             device.print(">");
             writeIcon(device, icon, Utils.isMSIE(component));
             device.print("</td></tr></table>");
         }
         else {
             new IconTextCompound() {
                 protected void text(Device d) throws IOException {
                    writeText(d, ftext, button.isWordWrap());
                 }
 
                 protected void icon(Device d) throws IOException {
                     writeIcon(d, icon, Utils.isMSIE(component));
                 }
 
                 protected void tableAttributes(Device d) throws IOException {
                     ButtonCG.this.tableAttributes(d, button);
                 }
             }.writeCompound(device, component, button.getHorizontalTextPosition(), button.getVerticalTextPosition(), false);
         }
     }
 
     protected void tableAttributes(Device device, SAbstractButton button) throws IOException {
         Utils.printClickability(device, button, button.getToggleSelectionParameter(), button.isEnabled(), button.getShowAsFormComponent());
 
         String style = button.getStyle();
         SStringBuilder className = new SStringBuilder(style != null ? style : "SButton");
         if (button.getShowAsFormComponent())
             className.append("_form");
         if (!button.isEnabled())
             className.append("_disabled");
         if (button.isSelected())
             className.append("_selected");
 
         button.setStyle(className.toString());
         Utils.writeAllAttributes(device, button);
         button.setStyle(style);
 
         if (button.isFocusOwner())
             Utils.optAttribute(device, "foc", button.getName());
 
         Utils.optAttribute(device, "tabindex", button.getFocusTraversalIndex());
         Utils.optAttribute(device, "accesskey", button.getMnemonic());
     }
 
     /* Retrieve according icon for a button. */
     public static SIcon getIcon(SAbstractButton abstractButton) {
         if (abstractButton.isSelected()) {
             return abstractButton.isEnabled()
                     ? abstractButton.getSelectedIcon()
                     : abstractButton.getDisabledSelectedIcon();
         } else {
             return abstractButton.isEnabled()
                     ? abstractButton.getIcon()
                     : abstractButton.getDisabledIcon();
         }
     }
 
     public Update getTextUpdate(SButton button, String text) {
         return text == null ? null : new TextUpdate(button, text);
     }
 
     public Update getIconUpdate(SButton button, SIcon icon) {
         return icon == null ? null : new IconUpdate(button, icon);
     }
 
 }
