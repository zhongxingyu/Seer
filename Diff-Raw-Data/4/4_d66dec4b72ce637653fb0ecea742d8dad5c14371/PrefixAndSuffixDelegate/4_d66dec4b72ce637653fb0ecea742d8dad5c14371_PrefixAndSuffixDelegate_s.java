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
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.wings.LowLevelEventListener;
 import org.wings.SComponent;
 import org.wings.SConstants;
 import org.wings.SDimension;
 import org.wings.SPopupMenu;
 import org.wings.border.STitledBorder;
 import org.wings.dnd.DragSource;
 import org.wings.io.Device;
 
 import javax.swing.*;
 import java.io.IOException;
 
 /**
  * @author ole
  */
 public class PrefixAndSuffixDelegate implements org.wings.plaf.PrefixAndSuffixDelegate {
     private final static transient Log log = LogFactory.getLog(PrefixAndSuffixDelegate.class);
 
     public PrefixAndSuffixDelegate() {
     }
 
     public void writePrefix(Device device, SComponent component) throws IOException {
         final SDimension prefSize = component.getPreferredSize();
         final StringBuffer cssInlineStyle = new StringBuffer();
         final boolean isTitleBorder = component.getBorder() instanceof STitledBorder;
 
         Utils.printDebugNewline(device, component);
         Utils.printDebug(device, "<!-- ").print(component.getName()).print(" -->");
 
         //------------------------ OUTER DIV
 
         // This is the outer DIV element of a component
         // it is responsible for Postioning (i.e. it take up all free space around to i.e. center
         // the inner div inside this free space
         device.print("<div");
         Utils.optAttribute(device, "class", Utils.appendSuffixesToWords(component.getStyle(), "_Box"));
         Utils.optAttribute(device, "id", component.getName());
         if (component instanceof DragSource) {
             cssInlineStyle.append("position:relative;");
         }
 
         // if sizes are spec'd in percentages, we need the outer box to have full size...
         final boolean isHeightPercentage = prefSize != null && prefSize.getHeightUnit() != null && prefSize.getHeightUnit().indexOf("%") != -1;
         final boolean isWidthPercentage = prefSize != null && prefSize.getWidthUnit() != null && prefSize.getWidthUnit().indexOf("%") != -1;
         // special case of special case: if the component with relative size is vertically aligned, we must avoid 100% heigth
         final boolean isVAligned = (component.getVerticalAlignment() == SConstants.CENTER
                 || component.getVerticalAlignment() == SConstants.BOTTOM);
         if (isHeightPercentage && isVAligned == false) {
             cssInlineStyle.append("height:100%;");
         }
         if (isWidthPercentage) {
             cssInlineStyle.append("width:100%;");
         }
 
         // Output collected inline CSS style
         Utils.optAttribute(device, "style", cssInlineStyle);
         device.print(">"); // div
 
         //------------------------ INNER DIV
 
         // This is the inner DIV around each component.
         // It is responsible for component size, and other styles.
         device.print("<div");
         Utils.optAttribute(device, "id", component.getName() + "_i");
         Utils.optAttribute(device, "class", isTitleBorder ? component.getStyle() + " STitledBorder" : component.getStyle());         // Special handling: Mark Titled Borders for styling
         Utils.optAttribute(device, "style", Utils.generateCSSInlinePreferredSize(prefSize).toString());
 
         if (component instanceof LowLevelEventListener) {
             Utils.optAttribute(device, "eid", ((LowLevelEventListener) component).getEncodedLowLevelEventId());
         }
 
         // Tooltip handling
         writeTooltipMouseOver(device, component);
 
         // Key bindings
         handleKeyBindings(component);
 
         // Component popup menu
         writeContextMenu(device, component);
 
         device.print(">"); // div
 
         // Special handling: Render title of STitledBorder
         if (isTitleBorder) {
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
         device.print("</div></div>");
         Utils.printDebug(device, "<!-- /").print(component.getName()).print(" -->");
     }
 
     /**
      * Write JS code for context menus. Common implementaton for MSIE and gecko.
      */
     protected static void writeContextMenu(Device device, SComponent component) throws IOException {
         final SPopupMenu menu = component.getComponentPopupMenu();
         if (menu != null) {
             final String componentId = menu.getName();
             final String popupId = componentId + "_pop";
             device.print(" onContextMenu=\"return wpm_menuPopup(event, '");
             device.print(popupId);
             device.print("');\" onMouseDown=\"return wpm_menuPopup(event, '");
             device.print(popupId);
             device.print("');\"");
         }
     }
 
     /**
      * Handle key binding attached to component. Common handler for MSIE and Gecko PLAF.
      */
     protected static void handleKeyBindings(SComponent component) {
         InputMap inputMap = component.getInputMap();
         if (inputMap != null && inputMap.size() > 0) {
             if (!(inputMap instanceof VersionedInputMap)) {
                 log.debug("inputMap = " + inputMap);
                 inputMap = new VersionedInputMap(inputMap);
                 component.setInputMap(inputMap);
             }
 
             final VersionedInputMap versionedInputMap = (VersionedInputMap) inputMap;
             final Integer inputMapVersion = (Integer) component.getClientProperty("inputMapVersion");
             if (inputMapVersion == null || versionedInputMap.getVersion() != inputMapVersion.intValue()) {
                 log.debug("inputMapVersion = " + inputMapVersion);
                 InputMapScriptListener.install(component);
                 component.putClientProperty("inputMapVersion", new Integer(versionedInputMap.getVersion()));
             }
         }
     }
 
     /**
      * Write DomTT Tooltip code. Common handler for MSIE and Gecko PLAF.
      */
     protected static void writeTooltipMouseOver(Device device, SComponent component) throws IOException {
         final String toolTipText = component.getToolTipText();
         if (toolTipText != null) {
             device.print(" onmouseover=\"return makeTrue(domTT_activate(this, event, 'content', '");
            Utils.quote(device, toolTipText, true, false, true);
             device.print("', 'predefined', 'default'));\"");
         }
     }
 
 }
