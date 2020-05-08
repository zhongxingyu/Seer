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
 import org.wings.io.Device;
 import org.wings.session.ScriptManager;
 import org.wings.plaf.css.script.*;
 
 
 public class ContainerCG extends AbstractComponentCG implements org.wings.plaf.PanelCG {
     private static final long serialVersionUID = 1L;
 
     public void writeInternal(final Device device, final SComponent component) throws java.io.IOException {
         final SContainer container = (SContainer) component;
         final SLayoutManager layout = container.getLayout();
 
         SDimension preferredSize = container.getPreferredSize();
         String height = preferredSize != null ? preferredSize.getHeight() : null;
         boolean clientLayout = height != null && Utils.isMSIE(container) && !"auto".equals(height)
             && (layout instanceof SBorderLayout || layout instanceof SGridBagLayout || layout instanceof SCardLayout);
 
         device.print("<table");
 
         if (clientLayout) {
             Utils.optAttribute(device, "layoutHeight", height);
            if (!"px".equals(preferredSize.getHeightUnit()))
                Utils.setPreferredSize(component, preferredSize.getWidth(), null);
         }
 
         Utils.writeAllAttributes(device, component);
         Utils.writeEvents(device, component, null);
 
         if (clientLayout) {
            if (!"px".equals(preferredSize.getHeightUnit()))
                Utils.setPreferredSize(component, preferredSize.getWidth(), height);
             ScriptManager.getInstance().addScriptListener(new LayoutFillScript(component.getName()));
         }
 
         device.print(">");
 
         // special case templateLayout and card layout. We open a TABLE cell for them.
         final boolean writeTableData = layout instanceof STemplateLayout;
         if (writeTableData) {
             device.print("<tr><td");
             Utils.printTableCellAlignment(device, component, SConstants.LEFT_ALIGN, SConstants.TOP_ALIGN);
             device.print(">");
         }
 
         Utils.renderContainer(device, container);
 
         if (writeTableData) {
             device.print("</td></tr>");
         }
 
         device.print("</table>");
     }
 }
