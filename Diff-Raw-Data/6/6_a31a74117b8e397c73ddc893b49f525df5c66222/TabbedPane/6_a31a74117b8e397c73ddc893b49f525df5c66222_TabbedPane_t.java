 /*
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @author <a href="mailto:wehrens@aei.mpg.de">Oliver Wehrens</a>
 * @version $Id$
 */
 
 package org.gridlab.gridsphere.layout.view.css;
 
 import org.gridlab.gridsphere.layout.PortletComponent;
 import org.gridlab.gridsphere.layout.PortletTab;
 import org.gridlab.gridsphere.layout.PortletTabbedPane;
 import org.gridlab.gridsphere.layout.view.BaseRender;
 import org.gridlab.gridsphere.layout.view.TabbedPaneView;
 import org.gridlab.gridsphere.portletcontainer.GridSphereEvent;
 
 import java.util.List;
 import java.util.StringTokenizer;
 
 public class TabbedPane extends BaseRender implements TabbedPaneView {
 
     protected static final StringBuffer TAB_START =
             new StringBuffer("\n<!-- START MODERN TABBED PANE --><div class=\"tab-pane\" ><ul>");
 
     protected static final StringBuffer TAB_END =
             // unuseful fillup DIV removed
             new StringBuffer("</ul></div><!-- END MODERN TABBED PANE -->\n");    
     /**
      * Constructs an instance of PortletTabbedPane
      */
     public TabbedPane() {
     }
 
 
     /**
      * Creates the portlet tab link URIs that are used to send events to
      * the portlet tabs.
      *
      * @param event the gridsphere event
      */
     protected static String[] createTabLinks(GridSphereEvent event, PortletTabbedPane pane) {
         // Make tab links
         String[] tabLinks = new String[pane.getPortletTabs().size()];
         List tabs = pane.getPortletTabs();
         for (int i = 0; i < tabs.size(); i++) {
             PortletTab tab = (PortletTab) tabs.get(i);
             tabLinks[i] = tab.createTabTitleLink(event);
         }
         return tabLinks;
     }
 
     /**
      * Replace blank spaces in title with '&nbsp;'
      *
      * @param title the tab title
      * @return a title without blank spaces
      */
     private static String replaceBlanks(String title) {
         String result = "&nbsp;";
         StringTokenizer st = new StringTokenizer(title);
         while (st.hasMoreTokens()) {
             result += st.nextToken() + "&nbsp;";
         }
         return result;
     }
 
     public StringBuffer doStart(GridSphereEvent event, PortletComponent comp) {
         PortletTabbedPane pane = (PortletTabbedPane)comp;
         if (pane.getStyle().equalsIgnoreCase("sub-menu")) {
            StringBuffer sb = new StringBuffer();
             sb.append("<div class=\"tab-sub-pane" /*+ pane.getTheme()*/ + "\" >"); ///
             sb.append("<ul>\n");
             return sb;
         } else {
             return TAB_START;
         }
     }
 
     public StringBuffer doRenderTab(GridSphereEvent event, PortletTabbedPane tabPane, PortletTab tab) {
         // this really creates the individual tabs
         StringBuffer pane = new StringBuffer();
        String path = event.getPortletRequest().getContextPath() + "/themes/" + tabPane.getRenderKit() + "/" + tabPane.getTheme() + "/images/"; /// Removed File.separator(s)
         String link = tab.createTabTitleLink(event);
         String lang = event.getPortletRequest().getLocale().getLanguage();
         String title = tab.getTitle(lang);
         if (tabPane.getStyle().equals("sub-menu")) {
            pane.append("\n<li>");
             if (tab.isSelected()) {
                 pane.append("<a class=\"tab-sub-active\" href=\"" + link + "\"" + " onclick=\"this.href='" + link + "&amp;JavaScript=enabled'\">");                
                 pane.append("<span class=\"tab-sub-menu-active\">" + replaceBlanks(title) + "</span></a>");            
             } else {
                 pane.append("<a class=\"tab-sub-inactive\" href=\"" + link + "\"" + " onclick=\"this.href='" + link + "&amp;JavaScript=enabled'\">");
                 pane.append("<span class=\"tab-sub-menu\">" + replaceBlanks(title) + "</span></a>");
             }
             pane.append("</li>\n");
         } else {
             if (tab.isSelected()) {
                 pane.append("\n<li>");
                 pane.append("<div class=\"tab-active\"><span>" + replaceBlanks(title) + "</span></div>");
                 pane.append("</li>");
             } else {
                 pane.append("\n<li>");
                 pane.append("<a href=\"" + link + "\"" + " onclick=\"this.href='" + link + "&amp;JavaScript=enabled'\">");
                 pane.append("<span class=\"tab-menu\">" + replaceBlanks(title) + "</span></a>");
                 pane.append("</li>");
             }
         }
         return pane;
     }
 
     public StringBuffer doEndBorder(GridSphereEvent event, PortletComponent component) {
         // this ends the indivual tabs
         PortletTabbedPane tabPane = (PortletTabbedPane)component;
         StringBuffer pane = new StringBuffer();
         if (tabPane.getStyle().equals("sub-menu")) {
            String path = event.getPortletRequest().getContextPath() + "/themes/" + tabPane.getRenderKit() + "/" + tabPane.getTheme() + "/images/"; /// Removed File.separator(s)
             pane.append("</ul>");
             // unuseful empty DIV commented out
             //pane.append("<div class=\"tab-empty\">&nbsp;</div>");            
             pane.append("</div><!-- END SUB MENU TABBED PANE -->\n");
             return pane;
         } else {
             return TAB_END;
         }
     }
 
 }
