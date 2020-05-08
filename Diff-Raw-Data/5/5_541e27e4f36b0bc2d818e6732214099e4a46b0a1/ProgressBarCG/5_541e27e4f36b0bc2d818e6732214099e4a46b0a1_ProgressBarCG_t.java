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
 
 
 import org.wings.*;
 import org.wings.io.Device;
 import org.wings.plaf.CGManager;
 import org.wings.session.SessionManager;
 
 import java.io.IOException;
 
 public class ProgressBarCG extends AbstractComponentCG implements
         org.wings.plaf.ProgressBarCG {
 
 // --- byte array converted template snippets.
 
     public void installCG(final SComponent comp) {
         super.installCG(comp);
         final SProgressBar component = (SProgressBar) comp;
         final CGManager manager = component.getSession().getCGManager();
         Object value;
         Object previous;
         value = manager.getObject("SProgressBar.borderColor", java.awt.Color.class);
         if (value != null) {
             component.setBorderColor((java.awt.Color) value);
         }
         value = manager.getObject("SProgressBar.filledColor", java.awt.Color.class);
         if (value != null) {
             component.setFilledColor((java.awt.Color) value);
         }
         value = manager.getObject("SProgressBar.foreground", java.awt.Color.class);
         if (value != null) {
             component.setForeground((java.awt.Color) value);
         }
         value = manager.getObject("SProgressBar.preferredSize", SDimension.class);
         if (value != null) {
             component.setPreferredSize((SDimension) value);
         }
         value = manager.getObject("SProgressBar.unfilledColor", java.awt.Color.class);
         if (value != null) {
             component.setUnfilledColor((java.awt.Color) value);
         }
     }
 
     public void uninstallCG(final SComponent comp) {
     }
 
 //--- code from common area in template.
     private static final SIcon BLIND_ICON = (SIcon) SessionManager.getSession()
     .getCGManager().getObject("ProgressBarCG.blindIcon", SIcon.class);
 
 //--- end code from common area in template.
 
 
     public void writeContent(final Device device,
                              final SComponent _c)
             throws IOException {
         final SProgressBar component = (SProgressBar) _c;
 
 //--- code from write-template.
         String style = component.getStyle();
 
         /* FIXME: The problem here is that the component size is used as the
          * size for the progressbar. If text is rendered below
          * (isStringPainted), then that text is out of the component box. So
          * either create a distinct ProgressBar size or subtract some height.
          * OL: created distinct height. other solution is removing string 
          * completely.
          */
         
         SDimension size = component.getProgressBarDimension();
         int width = size != null ? size.getIntWidth() : 200;
         int height = size != null ? size.getIntHeight() : 5;
 
         if (component.isBorderPainted()) {
             device.print("<div style=\"width: 100%;height:100%;border: 1px solid ");
             Utils.write(device, component.getBorderColor());
             device.print(";\">");
             width -= 2; //compensate for border
             height -= 2;
         }
 
        device.print("<table class=\"SLayout\"><tr><td class=\"SLayout\"");
         if (component.getFilledColor() != null) {
             device.print(" style=\"background-color: ");
             Utils.write(device, component.getFilledColor());
             device.print(";\"");
         }
         device.print(">");
         device.print("<img");
         Utils.optAttribute(device, "src", BLIND_ICON.getURL());
         device.print(" width=\"");
         device.print(String.valueOf(Math.round(width * component.getPercentComplete())));
         device.print("\"");
         device.print(" height=\"");
         device.print(String.valueOf(height));
         device.print("\"></td>");
        device.print("<td class=\"SLayout\"");
         if (component.getUnfilledColor() != null) {
             device.print(" style=\"background-color: ");
             Utils.write(device, component.getUnfilledColor());
            device.print(";\"");
         }
         device.print(">");
         device.print("<img");
         Utils.optAttribute(device, "src", BLIND_ICON.getURL());
         device.print(" width=\"");
         device.print(String.valueOf(Math.round(width * (1 - component.getPercentComplete()))));
         device.print("\" height=\"");
         device.print(String.valueOf(height));
         device.print("\"></td></tr></table>");
         if (component.isBorderPainted()) {
             device.print("</div>");
         }
 
         if (component.isStringPainted()) {
             device.print("<div style=\"width: 100%; text-align: center;\"");
             if (style != null) {
                 device.print(" class=\"");
                 Utils.write(device, style);
                 device.print("_string\"");
             }
             device.print(">");
             Utils.write(device, component.getString());
             device.print("</div>");
         }
 //--- end code from write-template.
     }
 }
