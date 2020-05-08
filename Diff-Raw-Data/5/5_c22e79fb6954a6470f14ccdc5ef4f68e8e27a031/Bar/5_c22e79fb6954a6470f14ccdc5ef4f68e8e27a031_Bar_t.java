 package org.gridsphere.layout.view.brush;
 
 import org.gridsphere.layout.PortletComponent;
 import org.gridsphere.layout.view.BaseRender;
 import org.gridsphere.layout.view.Render;
 import org.gridsphere.portletcontainer.GridSphereEvent;
 
 /**
  * @author <a href="mailto:novotny@gridsphere.org">Jason Novotny</a>
  * @version $Id$
  */
 public class Bar extends BaseRender implements Render {
 
 
     public StringBuffer doStart(GridSphereEvent event, PortletComponent comp) {
         StringBuffer buffer = new StringBuffer();
         buffer.append("<!-- START BAR -->")
                .append("<div id=\"gridsphere-layout-navigation\">");
 
         return buffer;
     }
 
     public StringBuffer doEndBorder(GridSphereEvent event, PortletComponent comp) {
         StringBuffer buffer = new StringBuffer();
         buffer.append("<div id=\"gridsphere-menu-bottom-line\">&nbsp;</div>");
        buffer.append("</div> <!-- end layout navigation -->");
         buffer.append("<div id=\"gridsphere-layout-body\"> <!-- start the main portlets -->\n");
         return buffer;
     }
 
     public StringBuffer doEnd(GridSphereEvent event, PortletComponent comp) {
         StringBuffer buffer = new StringBuffer();
         buffer.append("\n</div> <!-- END gridsphere-layout-body -->\n");
         return buffer;
     }
 }
 
 
