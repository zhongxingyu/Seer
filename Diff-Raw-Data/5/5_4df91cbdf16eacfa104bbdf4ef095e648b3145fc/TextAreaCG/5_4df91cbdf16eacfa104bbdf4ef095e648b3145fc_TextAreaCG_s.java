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
 package org.wings.plaf.css;
 
 
 import java.io.IOException;
 
 import org.wings.SComponent;
 import org.wings.STextArea;
 import org.wings.io.Device;
 
 public class TextAreaCG extends AbstractComponentCG implements
         org.wings.plaf.TextAreaCG {
 
     public void writeContent(final Device device,
                              final SComponent _c)
             throws IOException {
         final STextArea component = (STextArea) _c;
 
         if (!component.isEditable() && (component.getLineWrap() == STextArea.NO_WRAP) && (component.getColumns() == 0) && (component.getRows() == 0)) {
                /* A second way could be to calculate rows and columns and generate a textarea, but this will be 
                 * very time consuming at large texts. But if this way makes to much trouble, the other will be quite equal */
             String text = component.getText();       
             if (text != null) {
                 device.print("<nobr>");               /* Should we really ignore everything ? */
                 Utils.writeQuoted(device,text,true);    /* Write new text */
                 device.print("</nobr>");
             }
 
         } else {
             device.print("<textarea");
             Utils.optAttribute(device, "tabindex", component.getFocusTraversalIndex());
             
             Utils.optAttribute(device, "cols", component.getColumns());
             Utils.optAttribute(device, "rows", component.getRows());
 
             switch (component.getLineWrap()) {
                 case STextArea.VIRTUAL_WRAP:
                     device.print(" wrap=\"virtual\"");
                     break;
                 case STextArea.PHYSICAL_WRAP:
                     device.print(" wrap=\"physical\"");
                     break;
             }
 
             Utils.printCSSInlineFullSize(device, component.getPreferredSize());
 
             if (!component.isEditable() || !component.isEnabled()) {
                 device.print(" readonly=\"true\"");
             }
             if (component.isEnabled()) {
                 device.print(" name=\"");
                 Utils.write(device, Utils.event(component));
                 device.print("\"");
             } else {
                 device.print(" disabled=\"true\"");
             }
 
             if (component.isFocusOwner())
                 Utils.optAttribute(device, "focus", component.getName());
 
             Utils.writeEvents(device, component);
             device.print(">");
            Utils.writeRaw(device, component.getText());
             device.print("</textarea>\n");
         }
     }
 }
