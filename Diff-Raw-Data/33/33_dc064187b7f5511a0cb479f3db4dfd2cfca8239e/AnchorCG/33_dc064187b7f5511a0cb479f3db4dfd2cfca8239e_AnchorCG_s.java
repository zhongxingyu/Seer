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
 
 import org.wings.SAnchor;
 import org.wings.SComponent;
 import org.wings.io.Device;
 
 public class AnchorCG
         extends AbstractComponentCG
         implements org.wings.plaf.AnchorCG {
     /* (non-Javadoc)
      * @see org.wings.plaf.css.AbstractComponentCG#writeContent(org.wings.io.Device, org.wings.SComponent)
      */
     public void writeContent(final Device device,
                              final SComponent _c)
             throws IOException {
         final SAnchor component = (SAnchor) _c;
 
        writeLinkStart(device, component);
 
         Utils.printCSSInlineFullSize(device, _c.getPreferredSize());
         if (component.isFocusOwner())
             Utils.optAttribute(device, "focus", component.getName());
 
         Utils.optAttribute(device, "target", component.getTarget());
         Utils.optAttribute(device, "name", component.getName());
         Utils.optAttribute(device, "tabindex", component.getFocusTraversalIndex());
         Utils.writeEvents(device, component);
         device.print(">");
         Utils.renderContainer(device, component);
         device.print("</a>");
     }
 
     /**
      * Helper Function to ease choosing between normal and javascript links.
      * Right now this is just for an IE workaround.
      * @param device the device to output to
      * @param component the component to be rendered
      * @throws IOException 
      */
    protected void writeLinkStart(final Device device, final SAnchor component) throws IOException {
         device.print("<a href=\"");
        device.print(component.getURL());
         device.print("\"");
     }
 }
