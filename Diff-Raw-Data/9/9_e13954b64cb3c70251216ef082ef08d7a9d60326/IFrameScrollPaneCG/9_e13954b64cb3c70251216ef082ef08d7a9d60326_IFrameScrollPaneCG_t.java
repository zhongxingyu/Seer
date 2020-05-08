 /*
  * $Id$
  * (c) Copyright 2000 wingS development team.
  *
  * This file is part of wingS (http://wings.mercatis.de).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 
 package org.wingx.plaf;
 
 import java.awt.Rectangle;
 import java.io.IOException;
 
 import org.wings.*;
 import org.wings.externalizer.*;
 import org.wings.io.*;
 import org.wings.plaf.*;
 import org.wings.style.*;
 
 public final class IFrameScrollPaneCG
     implements org.wings.plaf.ScrollPaneCG
 {
     public void installCG(SComponent component) {}
     public void uninstallCG(SComponent component) {}
 
     public void write(Device d, SComponent c)
         throws IOException
     {
         SScrollPane scrollPane = (SScrollPane)c;
 
 	// write the contents to another device
 	SComponent contents = (SComponent)scrollPane.getScrollable();
         Scrollable scrollable = (Scrollable)contents;
         Rectangle rect = scrollable.getViewportSize();
         rect.setSize(scrollable.getScrollableViewportSize());
 
 	if (contents != null) {
 	    Device document = new StringBufferDevice();
             SFrame frame = c.getParentFrame();
 	    writeDocumentPrefix(document, frame);
 	    contents.write(document);
 	    writeDocumentPostfix(document);
 
 	    ExternalizeManager ext = c.getExternalizeManager();
 	    if (ext != null) {
 		try {
                     String url = frame.getServerAddress().getAbsoluteAddress();
                     url = url.substring(0, url.lastIndexOf('/') + 1);
 		    url += ext.externalize(document);
		    d.append("<iframe frameborder=\"0\" src=\"");
                     d.append(url);
                     d.append("\" width=\"");
                     d.append(scrollPane.getHorizontalExtent());
                     d.append("\" height=\"");
                     d.append(scrollPane.getVerticalExtent());
                     d.append("\"></iframe>");
 		}
 		catch (java.io.IOException e) {
                     d.append("sorry: something went wrong");
 		    // dann eben nicht !!
 		    e.printStackTrace(System.err);
 		}
 	    }
 	}
     }
 
     protected void writeDocumentPrefix(Device d, SFrame frame) {
         String language = "en"; // TODO: ???
 
         d.append("<?xml version=\"1.0\" encoding=\"");
         d.append(frame.getSession().getCharSet());
         d.append("\"?>\n");
         d.append("<!DOCTYPE html\n");
 	d.append("   PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\"\n");
         d.append("   \"DTD/xhtml1-transitional.dtd\">\n");
         d.append("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"");
         d.append(language);
         d.append("\" lang=\"");
         d.append(language);
         d.append("\">\n");
         d.append("<head>");
         d.append("<meta http-equiv=\"Content-type\" content='text/html; charset=\"");
         d.append(frame.getSession().getCharSet());
         d.append("\"' />\n");
         d.append("<meta http-equiv=\"expires\" content=\"0\" />\n");
         d.append("<meta http-equiv=\"pragma\" content=\"no-cache\" />\n");
         d.append("<base target=\"_top\">\n");
         d.append("</head>\n");
 	
         StyleSheet styleSheet = frame.getStyleSheet();
         if (styleSheet != null) {
             ExternalizeManager ext = frame.getExternalizeManager();
             String link = null;
 
             if (ext != null) {
                 try {
                     link = ext.externalize(styleSheet);
                 }
                 catch (java.io.IOException e) {
                     // dann eben nicht !!
                     e.printStackTrace(System.err);
                 }
             }
             if (link != null) {
                 d.append("<link rel=\"stylesheet\" type=\"text/css\" href=\"");
                 d.append(link);
                 d.append("\" />");
             }
         }
         else {
             System.err.println("Frame.styleSheet == null!");
         }
     }
 
     protected void writeDocumentPostfix(Device d) {
 	d.append("</body></html>");
     }
 }
 
 /*
  * Local variables:
  * c-basic-offset: 4
  * indent-tabs-mode: nil
  * End:
  */
