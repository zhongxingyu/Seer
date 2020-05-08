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
 package org.wings.plaf.css.msie;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.wings.*;
 import org.wings.border.STitledBorder;
 import org.wings.dnd.DragSource;
 import org.wings.io.Device;
 import org.wings.plaf.css.AbstractLayoutCG;
 import org.wings.plaf.css.InputMapScriptListener;
 import org.wings.plaf.css.Utils;
 import org.wings.plaf.css.VersionedInputMap;
 
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
         SDimension prefSize = component.getPreferredSize();
         final int align = component.getHorizontalAlignment();
 
         // For centering or right-alignment we need we surrounding helper table to be stretched to full width.
         if (prefSize == null && (align == SConstants.CENTER || align == SConstants.RIGHT))
             prefSize = new SDimension("100%", null);
 
         StringBuffer inlineStyles = Utils.generateCSSInlinePreferredSize(prefSize);
         Utils.appendCSSComponentInlineColorStyle(inlineStyles, component);
 
         Utils.printDebugNewline(device, component);
         Utils.printDebug(device, "<!-- ").print(component.getName()).print(" -->");
         
         device.print("<table id=\"").print(component.getName()).print("\"");
         // Special handling: Mark Titled Borders for styling
         if (component.getBorder() instanceof STitledBorder) {
             Utils.optAttribute(device, "class", component.getStyle() + " STitledBorder SContainer");
         } else {
             Utils.optAttribute(device, "class", component.getStyle() + " SContainer");
         }
        // TODO these two are clashing...
        if (component instanceof DragSource) {
            device.print(" style=\"position:relative;\"");
        }
         Utils.optAttribute(device, "style", inlineStyles.toString());
 
         if (component instanceof LowLevelEventListener) {
             LowLevelEventListener lowLevelEventListener = (LowLevelEventListener) component;
             device.print(" eid=\"")
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
             //ComponentCG menuCG = menu.getCG();
             String componentId = menu.getName();
             String popupId = componentId + "_pop";
             //String hookId = component.getName();
             device.print(" onContextMenu=\"javascript:return wpm_menuPopup(event, '");
             device.print(popupId);
             device.print("');\" onMouseDown=\"javascript:return wpm_menuPopup(event, '");
             device.print(popupId);
             device.print("');\"");
         }
 
         device.print("><tr>"); // table
         AbstractLayoutCG.openLayouterCell(device, false, 0, 0, 0, component);
         device.print(">");
 
         // Special handling: Render title of STitledBorder
         // TODO Attention - This may break CSS selectors as a new element is introduced inbetween.
         // Maybe move out of DIV with the componentn ID.
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
         AbstractLayoutCG.closeLayouterCell(device, false);
         device.print("</tr></table>");
         Utils.printDebug(device, "<!-- /").print(component.getName()).print(" -->");
     }
 
 }
