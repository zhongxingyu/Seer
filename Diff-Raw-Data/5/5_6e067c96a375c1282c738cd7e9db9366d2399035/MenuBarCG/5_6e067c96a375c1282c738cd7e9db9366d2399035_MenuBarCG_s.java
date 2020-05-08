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
 
 
 import org.wings.SConstants;
 import org.wings.SMenu;
 import org.wings.SMenuBar;
 import org.wings.event.SParentFrameEvent;
 import org.wings.event.SParentFrameListener;
 import org.wings.header.Header;
 import org.wings.header.SessionHeaders;
 import org.wings.io.Device;
 import org.wings.script.JavaScriptEvent;
 import org.wings.script.JavaScriptListener;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Collections;
 
 /**
  * This is the Default XHTML CSS plaf for the SMenuBar Component.
  * @author ole
  */
 public class MenuBarCG extends AbstractComponentCG<SMenuBar>
        implements org.wings.plaf.MenuBarCG, SParentFrameListener {
 
     protected final static List<Header> CALENDAR_MSIE_HEADERS;
     static {
         ArrayList<Header> tmpHeaders = new ArrayList<Header>();
         tmpHeaders.add(Utils.createExternalizedJSHeaderFromProperty(Utils.JS_ETC_MENU));
         CALENDAR_MSIE_HEADERS = Collections.unmodifiableList(tmpHeaders);
     }
 
     public static final JavaScriptListener BODY_ONCLICK_SCRIPT =
         new JavaScriptListener(JavaScriptEvent.ON_CLICK, "wpm_handleBodyClicks(event)");
 
     public MenuBarCG() {
     }
 
     @Override
     public void installCG(final SMenuBar comp) {
         super.installCG(comp);
         comp.addParentFrameListener(this);
     }
 
     public void parentFrameAdded(SParentFrameEvent e) {
         SessionHeaders.getInstance().registerHeaders(CALENDAR_MSIE_HEADERS);
         //e.getParentFrame().addScriptListener(BODY_ONCLICK_SCRIPT);
     }
 
     public void parentFrameRemoved(SParentFrameEvent e) {
         SessionHeaders.getInstance().deregisterHeaders(CALENDAR_MSIE_HEADERS);
         //e.getParentFrame().removeScriptListener(BODY_ONCLICK_SCRIPT);
     }
 
     /* (non-Javadoc)
      * @see org.wings.plaf.css.AbstractComponentCG#writeContent(org.wings.io.Device, org.wings.SComponent)
      */
     @Override
     public void writeInternal(final Device device, final SMenuBar menuBar) throws IOException {
         final int mcount = menuBar.getComponentCount();
         writeTablePrefix(device, menuBar);
 
         printSpacer(device);         /* clear:both to ensuer menubar surrounds all SMenu entries */
 
         // Left-aligned menues must rendered first in natural order
         for (int i = 0; i < mcount; i++) {
             final SMenu menu = menuBar.getMenu(i);
             if (menu != null && menu.isVisible() && menu.getHorizontalAlignment() != SConstants.RIGHT_ALIGN) {
                renderSMenu(device, menu, false);
             }
         }
         // Right-aligned menues must rendered first in revers order due to float:right
         for (int i = mcount-1; i >= 0 ; i--) {
             final SMenu menu = menuBar.getMenu(i);
             if (menu != null && menu.isVisible() && menu.getHorizontalAlignment() == SConstants.RIGHT_ALIGN) {
                renderSMenu(device, menu, true);
             }
         }
 
         printSpacer(device);      /* clear:both to ensuer menubar surrounds all SMenu entries */
 
         writeTableSuffix(device, menuBar);
     }
 
     /* Renders the DIV representing a top SMenu item inside the menu bar. */
     protected void renderSMenu(final Device device, final SMenu menu, boolean rightAligned) throws IOException {
         if (menu.isEnabled()) {
             device.print("<div class=\"SMenu\" onMouseDown=\"wpm_menu(event,'");
             device.print(menu.getName());
            device.print("_pop');\" onMouseOver=\"wpm_changeMenu(event,'");
             device.print(menu.getName());
            device.print("_pop');\"");
         } else {
             device.print("<div class=\"SMenu_Disabled\"");
         }
         if (rightAligned)
             device.print(" style=\"float:right\"");
         device.print(">");
         Utils.write(device, menu.getText());
         device.print("</div>");
     }
 
     /**
      * Prints a spacer if necessary, depending on browser compatibility.
      * Is inserted here for possible overwriting in inheriting plafs for
      * other browsers.
      * @param device the device to print on
      * @throws IOException
      */
     protected void printSpacer(final Device device) throws IOException {
         device.print("<div class=\"spacer\"></div>");
     }
 
 }
