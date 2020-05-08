 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.layout.view.modern;
 
 import org.gridlab.gridsphere.layout.PortletComponent;
 import org.gridlab.gridsphere.layout.PortletPage;
 import org.gridlab.gridsphere.layout.view.BaseRender;
 import org.gridlab.gridsphere.layout.view.Render;
 import org.gridlab.gridsphere.portlet.PortletRequest;
 import org.gridlab.gridsphere.portlet.impl.SportletProperties;
 import org.gridlab.gridsphere.portletcontainer.GridSphereEvent;
 
 import java.awt.*;
 import java.awt.List;
 import java.util.*;
 
 public class Page extends BaseRender implements Render {
 
     /**
      * Constructs an instance of PortletPage
      */
     public Page() {
     }
 
     public StringBuffer doStart(GridSphereEvent event, PortletComponent component) {
 
         PortletRequest req = event.getPortletRequest();
 
         StringBuffer page = new StringBuffer();
 
         PortletPage portletPage = (PortletPage)component;
         // page header
         Locale locale = req.getLocale();
         ComponentOrientation orientation = ComponentOrientation.getOrientation(locale);
         page.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>");
         page.append("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" " );
         page.append("\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">");
         if (orientation.isLeftToRight()) {
             page.append("<html");
         } else {
             page.append("<html dir=\"rtl\"");
         }
         page.append(" xmlns=\"http://www.w3.org/1999/xhtml\">");
 
         page.append("\n\t<head>");
         page.append("\n\t<title>" + portletPage.getTitle() + "</title>");
         ///page.append("\n\t<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/>");
         page.append("\n\t<meta name='keywords' content='" + portletPage.getKeywords() + "' />");
         page.append("\n\t<meta http-equiv=\"Pragma\" content=\"no-cache\" />");
         if (portletPage.getRefresh() > 0) page.append("\n\t<meta http-equiv=\"refresh\" content=\"" + portletPage.getRefresh() + "\"/>");
         page.append("\n\t<link type=\"text/css\" href=\"" +
                 req.getContextPath() + "/themes/" + portletPage.getTheme() + "/css" +
                     "/default.css\" rel=\"stylesheet\"/>");
 
         // Add portlet defined stylesheet if defined
         Map props = (Map)req.getAttribute(SportletProperties.PORTAL_PROPERTIES);
         if (props != null) {
             Object cssHrefObj = props.get("CSS_HREF");
             if (cssHrefObj instanceof java.util.List) {
                 java.util.List cssHref = (java.util.List)cssHrefObj;
                 Iterator it = cssHref.iterator();
                 while (it.hasNext()) {
                     String cssLink = (String)it.next();
                     if (cssLink != null) {
                        page.append("\n\t<link type=\"text/css\" href=\"" + cssLink + "\" rel=\"stylesheet\"/>");
                     }
                 }
             }
         }
         page.append("\n\t<link rel=\"icon\" href=\"" + portletPage.getIcon() + "\" type=\"imge/x-icon\"/>");
         page.append("\n\t<link rel=\"shortcut icon\" href=\"" + req.getContextPath() + "/" + portletPage.getIcon() + "\" type=\"image/x-icon\"/>");
         page.append("\n\t<script type=\"text/javascript\" src=\"" + req.getContextPath() + "/javascript/gridsphere.js\"></script>");
         page.append("\n\t</head>\n\t<body>\n");
         return page;
     }
 
     public StringBuffer doEnd(GridSphereEvent event, PortletComponent comp) {
         return new StringBuffer("\n</body></html>");
     }
 }
 
