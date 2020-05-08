 /*
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
 import org.wings.plaf.css.script.LayoutFillScript;
 
 import java.io.IOException;
 
 public class FormCG extends AbstractComponentCG implements org.wings.plaf.FormCG {
 
     private static final long serialVersionUID = 1L;
 
     public void writeInternal(final Device device, final SComponent component) throws IOException {
         final SForm form = (SForm) component;
         SLayoutManager layout = form.getLayout();
 
         // Prevent nesting of forms
         boolean formTagRequired = !form.getResidesInForm();
 
         if (formTagRequired) {
             device.print("<form method=\"");
             if (form.isPostMethod()) {
                 device.print("post");
             } else {
                 device.print("get");
             }
             device.print("\"");
             writeAllAttributes(device, form);
             Utils.optAttribute(device, "name", form.getName());
             Utils.optAttribute(device, "enctype", form.getEncodingType());
             Utils.optAttribute(device, "action", form.getRequestURL());
             Utils.writeEvents(device, form, null);
 
             device.print(" onSubmit=\"wingS.request.sendEvent(event, true, " +
                     !component.isReloadForced() + "); return false;\">");
             writeCapture(device, form);
         }
 
        // Default button handling
        device.print("<input type=\"hidden\" name=\"");
        Utils.write(device, Utils.event(form));
        device.print("\" value=\"");
        Utils.write(device, form.getName());
        device.print(SConstants.UID_DIVIDER);
        device.print("\" />");

         SDimension preferredSize = form.getPreferredSize();
         String height = preferredSize != null ? preferredSize.getHeight() : null;
         boolean clientLayout = isMSIE(form) && height != null && !"auto".equals(height)
             && (layout instanceof SBorderLayout || layout instanceof SGridBagLayout);
 
         String tableName = form.getName() + "_table";
         device.print("<table id=\"");
         device.print(tableName);
         device.print("\"");
 
         if (clientLayout) {
             device.print(" style=\"width:100%\"");
             Utils.optAttribute(device, "layoutHeight", height);
             form.getSession().getScriptManager().addScriptListener(new LayoutFillScript(tableName));
         }
         else
             Utils.printCSSInlineFullSize(device, form.getPreferredSize());
 
         device.print(">");
 
         // Render the container itself
         Utils.renderContainer(device, form);
 
         device.print("</table>");
 
         if (formTagRequired) {
             writeCapture(device, form);
             device.print("</form>");
         }
     }
 
     /*
      * we render two icons into the page that captures pressing simple 'return'
      * in the page. Why ? Depending on the Browser, the Browser sends the
      * first or the last submit-button it finds in the page as 'default'-Submit
      * when we simply press 'return' somewhere.
      *
      * However, we don't want to have this arbitrary behaviour in wingS.
      * So we add these two (invisible image-) submit-Buttons, either of it
      * gets triggered on simple 'return'. No real wingS-Button will then be
      * triggered but only the ActionListener added to the SForm. So we have
      * a way to distinguish between Forms that have been sent as default and
      * pressed buttons.
      *
      * Watchout: the style of these images once had been changed to display:none;
      * to prevent taking some pixel renderspace. However, display:none; made
      * the Internet Explorer not accept this as an input getting the default-focus,
      * so it fell back to the old behaviour. So changed that style to no-padding,
      * no-margin, no-whatever (HZ).
      */
     private void writeCapture(Device device, SForm form) throws IOException {
         final String defaultButtonName = form.getDefaultButton() != null ? Utils.event(form.getDefaultButton()) : "capture_enter";
         device.print("<input type=\"image\" name=\"").print(defaultButtonName).print("\" border=\"0\" ");
         Utils.optAttribute(device, "src", getBlindIcon().getURL());
         device.print(" width=\"0\" height=\"0\" tabindex=\"\" style=\"border:none;padding:0px;margin:0px;position:absolute\"/>");
     }
 }
