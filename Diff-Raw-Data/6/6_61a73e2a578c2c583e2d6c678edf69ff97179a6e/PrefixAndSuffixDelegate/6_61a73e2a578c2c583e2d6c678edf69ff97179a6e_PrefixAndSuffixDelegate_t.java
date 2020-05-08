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
  */package org.wings.plaf.css;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.wings.LowLevelEventListener;
 import org.wings.SComponent;
 import org.wings.SDimension;
 import org.wings.SPopupMenu;
 import org.wings.border.STitledBorder;
 import org.wings.io.Device;
 
 import javax.swing.*;
 import java.io.IOException;
 
 /**
  * @author ole
  */
 public class PrefixAndSuffixDelegate implements org.wings.plaf.PrefixAndSuffixDelegate {
     private final static transient Log log = LogFactory.getLog(PrefixAndSuffixDelegate.class);
 
     public PrefixAndSuffixDelegate() {}
     
     public void writePrefix(Device device, SComponent component) throws IOException {
         SDimension prefSize = component.getPreferredSize();
         Utils.printDebugNewline(device, component);
         Utils.printDebug(device, "<!-- ").print(component.getName()).print(" -->");
         device.print("<div");
         if (component.getStyle() != null && component.getStyle().length() > 0) {
             device.print(" class=\"");
             device.print(component.getStyle());
             device.print("_Box\"");
         }
 
         // if sizes are spec'd in percentages, we need the outer box to have full size...
         boolean isHeightPercentage = prefSize != null && prefSize.height != null && prefSize.height.indexOf("%") != -1;
         boolean isWidthPercentage = prefSize != null && prefSize.width != null && prefSize.width.indexOf("%") != -1;
        // special case of special case: if the component with relative size is vertically aligned, we must avoid 100% heigth
        boolean isVAligned = (component.getVerticalAlignment() == SComponent.CENTER || component.getVerticalAlignment() == SComponent.BOTTOM );

         if ( isHeightPercentage || isWidthPercentage ) {
             device.print(" style=\"");
            if (isHeightPercentage && isVAligned == false) {
                 device.print("height:100%;");
             }
             if (isWidthPercentage) {
                 device.print("width:100%;");
             }
             device.print("\"");
         }
         
         device.print(">");
         device.print("<div id=\"").print(component.getName()).print("\"");
         Utils.optAttribute(device, "class", component.getStyle());
         Utils.printCSSInlinePreferredSize(device, prefSize);
 
         if (component instanceof LowLevelEventListener) {
             LowLevelEventListener lowLevelEventListener = (LowLevelEventListener) component;
             device.print(" event=\"")
                     .print(lowLevelEventListener.getEncodedLowLevelEventId()).print("\"");
         }
 
         String toolTip = component.getToolTipText();
         if (toolTip != null)
             device.print(" onmouseover=\"return makeTrue(domTT_activate(this, event, 'content', '")
                     .print(toolTip)
                     .print("', 'predefined', 'default'));\"");
 
         InputMap inputMap = component.getInputMap();
         if (inputMap != null && !(inputMap instanceof VersionedInputMap)) {
             log.debug("inputMap = " + inputMap);
             inputMap = new VersionedInputMap(inputMap);
             component.setInputMap(inputMap);
         }
 
         if (inputMap != null) {
             VersionedInputMap versionedInputMap = (VersionedInputMap) inputMap;
             Integer inputMapVersion = (Integer) component.getClientProperty("inputMapVersion");
             if (inputMapVersion == null || versionedInputMap.getVersion() != inputMapVersion.intValue()) {
                 log.debug("inputMapVersion = " + inputMapVersion);
                 InputMapScriptListener.install(component);
                 component.putClientProperty("inputMapVersion", new Integer(versionedInputMap.getVersion()));
             }
         }
 
         SPopupMenu menu = component.getComponentPopupMenu();
         if (menu != null) {
             String componentId = menu.getName();
             String popupId = componentId + "_pop";
             String hookId = component.getName();
 
             device.print(" onclick=\"Menu.prototype.toggle(null,'");
             Utils.write(device, hookId);
             device.print("','");
             Utils.write(device, popupId);
             device.print("')\"");
         }
 
         device.print(">"); // div
 
         // Special handling: Render title of STitledBorder
         if (component.getBorder() instanceof STitledBorder) {
             STitledBorder titledBorder = (STitledBorder) component.getBorder();
             device.print("<div class=\"STitledBorderLegend\" style=\"");
             titledBorder.getTitleAttributes().write(device);
             device.print("\">");
             device.print(titledBorder.getTitle());
             device.print("</div>");
         }
 
         component.fireRenderEvent(SComponent.START_RENDERING);
     }
 
     public void writeSuffix(Device device, SComponent component) throws IOException {
         component.fireRenderEvent(SComponent.DONE_RENDERING);
 
         boolean backup = component.getInheritsPopupMenu();
         component.setInheritsPopupMenu(false);
 
         if (component.getComponentPopupMenu() != null)
             component.getComponentPopupMenu().write(device);
 
         component.setInheritsPopupMenu(backup);
 
         device.print("</div>");
         device.print("</div>");
         Utils.printDebug(device, "<!-- /").print(component.getName()).print(" -->");
     }
 
 }
