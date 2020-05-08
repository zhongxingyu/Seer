 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @author <a href="mailto:wehrens@aei.mpg.de">Oliver Wehrens</a>
  * @version $Id$
  */
 
 package org.gridlab.gridsphere.layout;
 
 import org.gridlab.gridsphere.portletcontainer.GridSphereEvent;
 import org.gridlab.gridsphere.portletcontainer.GridSphereProperties;
 import org.gridlab.gridsphere.portlet.PortletURI;
 import org.gridlab.gridsphere.portlet.PortletLog;
 import org.gridlab.gridsphere.portlet.impl.SportletRequest;
 import org.gridlab.gridsphere.portlet.impl.SportletResponse;
 import org.gridlab.gridsphere.portlet.impl.SportletLog;
 
 import java.util.List;
 import java.util.ArrayList;
 import java.io.IOException;
 import java.io.PrintWriter;
 
 public class PortletTabbedPane extends BasePortletComponent implements PortletTabListener {
 
     protected transient static PortletLog log = SportletLog.getInstance(PortletTabbedPane.class);
 
 
     private List tabs = new ArrayList();
     private int selectedIndex = 0;
     private String style = "menu";
 
     public PortletTabbedPane() {}
 
     public void setStyle(String style) {
         log.debug("style set: "+style);
         this.style = style;
     }
 
     public String getStyle() {
         return style;
     }
 
     public void addTab(String title, PortletComponent comp) {
         PortletTab tab = new PortletTab(title, comp);
         tabs.add(tab);
     }
 
     public void setSelectedIndex(int index) {
         this.selectedIndex = index;
     }
 
     public int getSelectedIndex() {
         return selectedIndex;
     }
 
     public PortletTab getSelectedTab() {
         return (PortletTab)tabs.get(selectedIndex);
     }
 
     public PortletTab getPortletTabAt(int index) {
         return (PortletTab)tabs.get(index);
     }
 
     public int getTabCount() {
         return tabs.size();
     }
 
     public String getTitleAt(int index) {
         PortletTab tab = (PortletTab)tabs.get(index);
         return tab.getTitle();
     }
 
     public int indexOfTab(String title) {
         boolean found = false;
         int i;
         for (i = 0; i < tabs.size(); i++) {
             PortletTab tab = (PortletTab)tabs.get(i);
             if (tab.getTitle().equals(title)) {
                 found = true;
                 break;
             }
         }
         if (found)
             return i;
         return -1;
     }
 
     public void insertTab(PortletTab tab, int index) {
         tabs.add(index, tab);
     }
 
     public void removeTab(PortletTab tab) {
         if (tabs.contains(tab)) tabs.remove(tab);
     }
 
     public void removeTabAt(int index) {
         tabs.remove(index);
     }
 
     public void removeAll() {
         for (int i = 0; i < tabs.size(); i++) {
             tabs.remove(i);
         }
     }
 
     public void setTitleAt(int index, String title) {
         PortletTab tab = (PortletTab)tabs.get(index);
         tab.setTitle(title);
     }
 
     public void setPortletTabs(ArrayList tabs) {
         this.tabs = tabs;
 
     }
 
     public void setSelectedPortletTab(PortletTab tab) {
         PortletTab portletTab;
 
         for (int i = 0; i < tabs.size(); i++) {
             portletTab = (PortletTab)tabs.get(i);
             if (portletTab.getComponentID() == tab.getComponentID()) {
                 selectedIndex = i;
                 portletTab.setSelected(true);
             } else {
                 portletTab.setSelected(false);
             }
         }
     }
 
     public List getPortletTabs() {
         return tabs;
     }
 
     public List init(List list) {
         if (selectedIndex < 0) selectedIndex = 0;
         PortletTab tab = null;
         for (int i = 0; i < getTabCount(); i++) {
             tab = getPortletTabAt(i);
             if (selectedIndex==i) tab.setSelected(true);
             tab.addPortletTabListener(this);
             list = tab.init(list);
         }
         return list;
     }
 
     public void handlePortletTabEvent(PortletTabEvent event) {
         if (event.getAction() == PortletTabEvent.Action.TAB_SELECTED) {
             PortletTab selectedTab = event.getPortletTab();
             setSelectedPortletTab(selectedTab);
         }
     }
 
     public String[] makeTabLinks(GridSphereEvent event) throws IOException {
         SportletRequest req = event.getSportletRequest();
 
         // Make tab links
         String[] tabLinks = new String[tabs.size()];
 
         PortletURI sportletURI;
         for (int i = 0; i < tabs.size(); i++) {
             PortletTab tab = (PortletTab)tabs.get(i);
            sportletURI = event.createNewAction(tab.getComponentID(), null);
 
             sportletURI.addParameter(GridSphereProperties.PORTLETTAB, tab.getTitle());
             tabLinks[i] = sportletURI.toString();
         }
         req.setAttribute(LayoutProperties.TABLINKS, tabLinks);
         return tabLinks;
     }
 
     public void doRenderMenu(GridSphereEvent event, String[] links) throws PortletLayoutException, IOException {
         SportletResponse res = event.getSportletResponse();
         PrintWriter out = res.getWriter();
 
         // Render tabs titles
         out.println("<table class=\"tab-pane\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
         out.println("<tr>");
 
         PortletTab tab;
         for (int i = 0; i < tabs.size(); i++) {
             String title = getTitleAt(i);
             tab = (PortletTab)tabs.get(i);
             if (tab.isSelected()) {
                 out.println("<td><img src=\"themes/"+theme+"/images/tab-active-left.gif\"/></td>");
                 out.println("<td class=\"tab-active\">" + title + "</td>");
                 out.println("<td><img src=\"themes/"+theme+"/images/tab-active-right.gif\"/></td>");
             } else {
                 out.println("<td><img src=\"themes/"+theme+"/images/tab-inactive-left.gif\"/></td>");
                 out.println("<td class=\"tab-inactive\"><a class=\"tab-menu\" href=\"" + links[i] + "\" >" +  title + "</a>");
                 out.println("<td><img src=\"themes/"+theme+"/images/tab-inactive-right.gif\"/></td>");
             }
             out.println("<td class=\"tab-empty\">&nbsp;</td>");
         }
         out.println("</tr></table>");
 
         PortletTab selectedTab = (PortletTab)tabs.get(selectedIndex);
         if (selectedTab != null)
             selectedTab.doRender(event);
 
     }
 
     public void doRenderSubMenu(GridSphereEvent event, String[] links) throws PortletLayoutException, IOException {
         SportletResponse res = event.getSportletResponse();
         PrintWriter out = res.getWriter();
 
         // Render tabs titles
         //out.println("<div class=\"tab-sub-pane\">");
         //out.println("<div class=\"tab-sub-menu\">");
         out.println("<table border=\"0\" class=\"tab-sub-pane\" width=\"100%\"><tr><td>");
         out.println("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\"><tr>");
 
 
         PortletTab tab;
         for (int i = 0; i < tabs.size(); i++) {
             String title = getTitleAt(i);
             tab = (PortletTab)tabs.get(i);
             if (tab.isSelected()) {
                 //out.println("<span class=\"tab-sub-active\">" + title + "</span>");
                 out.println("<td> <span class=\"tab-sub-active\">"+title+"</span></td>");
 
             } else {
                 out.println("<td> <span class=\"tab-sub-inactive\">");
                 out.println("<a class=\"tab-sub-menu\" href=\"" + links[i] + "\" >" +  title + "</a>");
                 out.println("</span></td>");
 
                // out.println("<span class=\"tab-sub-inactive\"><a class=\"tab-sub-menu\" href=\"" + links[i] + "\" >" +  title + "</a></span>");
             }
         }
 
         out.println("</tr></table>");
 
         out.println("</td></tr></table>");
 
         ///out.println("</div></div><div class=\"tab-bar\"></div>");
         PortletTab selectedTab = (PortletTab)tabs.get(selectedIndex);
         if (selectedTab != null)
             selectedTab.doRender(event);
     }
 
     public void doRender(GridSphereEvent event) throws PortletLayoutException, IOException {
         super.doRender(event);
 
         String[] links = makeTabLinks(event);
 
         log.debug("in tabbed pane: style=" + style);
         if (style.equals("sub-menu")) {
             doRenderSubMenu(event, links);
         } else {
             doRenderMenu(event, links);
         }
 
     }
 
 }
