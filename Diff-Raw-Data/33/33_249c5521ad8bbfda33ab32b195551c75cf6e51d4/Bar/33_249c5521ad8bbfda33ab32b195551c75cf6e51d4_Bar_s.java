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
 
    private static StringBuffer START_BAR = new StringBuffer()
            .append("<!-- START BAR -->")
            .append("<div id=\"gridsphere-layout-navigation\">")
            .append("<ul id=\"gridsphere-nav\"></ul>")
            .append("<ul id=\"gridsphere-sub-nav\">")
            .append("<li id=\"gridsphere-sub-nav-border-right\">");

    private static StringBuffer END_BORDER_BAR = new StringBuffer("&nbsp;</li></ul>");

    private static StringBuffer END_BAR = new StringBuffer("</div> <!-- END BAR -->\n");
 
     public StringBuffer doStart(GridSphereEvent event, PortletComponent comp) {
        return START_BAR;
     }
 
     public StringBuffer doEndBorder(GridSphereEvent event, PortletComponent comp) {
        return END_BORDER_BAR;
     }
 
     public StringBuffer doEnd(GridSphereEvent event, PortletComponent comp) {
        return END_BAR;
     }

    

 }
 
 
