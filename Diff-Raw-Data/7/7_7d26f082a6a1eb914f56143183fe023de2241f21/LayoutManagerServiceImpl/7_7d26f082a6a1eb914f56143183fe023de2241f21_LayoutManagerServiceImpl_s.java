 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.services.core.layout.impl;
 
 import org.gridlab.gridsphere.layout.*;
 import org.gridlab.gridsphere.portlet.PortletLog;
 import org.gridlab.gridsphere.portlet.User;
 import org.gridlab.gridsphere.portlet.impl.SportletLog;
 import org.gridlab.gridsphere.portlet.service.PortletServiceUnavailableException;
 import org.gridlab.gridsphere.portlet.service.spi.PortletServiceConfig;
 import org.gridlab.gridsphere.portlet.service.spi.PortletServiceProvider;
 import org.gridlab.gridsphere.services.core.layout.LayoutManagerService;
 
 import java.io.IOException;
 import java.util.*;
 
 
 /**
  * The LayoutManagerService provies customization support for user layouts
  */
 public class LayoutManagerServiceImpl implements LayoutManagerService, PortletServiceProvider {
 
     private static PortletLog log = SportletLog.getInstance(LayoutManagerServiceImpl.class);
     private PortletLayoutEngine layoutEngine = null;
 
     /**
      * The init method is responsible for parsing portlet.xml and creating ConcretePortlet objects based on the
      * entries. The RegisteredPortlets are managed by the PortletRegistryService.
      */
     public void init(PortletServiceConfig config) throws PortletServiceUnavailableException {
         log.info("in init()");
         // Need a portlet registry service
         layoutEngine = PortletLayoutEngine.getInstance();
     }
 
     public void destroy() {
         log.info("in destroy()");
     }
 
     public PortletContainer getPortletContainer(User user) {
         return layoutEngine.getPortletContainer(user);
     }
 
     public void setPortletContainer(PortletContainer container, User user) {
 
     }
 
     public void reloadPortletContainer(PortletContainer container) {
 
     }
 
     public Map getTabbedMaps(User user) {
         Map tabMap = new HashMap();
         PortletTabbedPane tabbedPane = findTabbedPane(user);
         Iterator it = tabbedPane.getPortletTabs().iterator();
         PortletTab tab = null;
         while (it.hasNext()) {
             tab = (PortletTab)it.next();
             tabMap = createTabbedMap(tabMap, tab);
         }
         return tabMap;
     }
 
     protected Map createTabbedMap(Map map, PortletTab tab) {
         PortletComponent comp = tab.getPortletComponent();
         if (comp instanceof PortletTabbedPane) {
             Map tabMap = new HashMap();
             PortletTabbedPane tabPane = (PortletTabbedPane)comp;
             Iterator it = tabPane.getPortletTabs().iterator();
             PortletTab ptab = null;
             while (it.hasNext()) {
                 ptab = (PortletTab)it.next();
                 map = createTabbedMap(tabMap, ptab);
             }
         } else {
             map.put(tab.getTitle(), new ArrayList());
         }
         return map;
     }
 
     protected List getComponentIdentifierList(User user) {
        return layoutEngine.getUserLayout(user).getComponentIdentifierList();
     }
 
 /*
     public void addFrameToTabPage(User user, String concretePortletID, String tabName) throws PortletLayoutException {
         PortletTabbedPane pane = findTabbedPane(user);
         int tabindex = pane.indexOfTab(tabName);
         PortletTab tab = pane.getPortletTabAt(tabindex);
         PortletLayout layout = tab.getPortletComponent()panel.getLayoutManager();
         PortletFrame newFrame = new PortletFrame();
         PortletContainer pc = layoutEngine.getPortletContainer(user);
         List newlist = newFrame.init(pc.getComponentIdentifierList());
         pc.setComponentIdentifierList(newlist);
         newFrame.setPortletClass(concretePortletID);
         layout.addPortletComponent(newFrame);
     }
 
     public void removeFrameFromTab(User user, String concretePortletID, String tabName) {
         PortletTabbedPane pane = findTabbedPane(user);
         int tabindex = pane.indexOfTab(tabName);
         PortletPanel panel = pane.getPortletPanelAt(tabindex);
         PortletLayout layout = panel.getLayoutManager();
         List comps = layout.getPortletComponents();
         Iterator it = comps.iterator();
         while (it.hasNext()) {
             PortletComponent c = (PortletComponent)it.next();
             if (c instanceof PortletFrame) {
                 PortletFrame f = (PortletFrame)c;
                 if (f.getPortletClass().equals(concretePortletID)) {
                     layout.removePortletComponent(c);
                     PortletContainer pc = layoutEngine.getPortletContainer(user);
                     List compIdList = pc.getComponentIdentifierList();
                     compIdList.remove(c);
                     pc.setComponentIdentifierList(compIdList);
                     break;
                 }
             }
         }
 
     }
     */
 
     public void addPortletTab(User user, String tabName) {
         PortletPanel panel = new PortletPanel();
         PortletGridLayout gridLayout = new PortletGridLayout();
         panel.setLayout(gridLayout);
         PortletTabbedPane tabbedPane = findTabbedPane(user);
         if (tabbedPane != null) {
            tabbedPane.addTab(tabName, panel);
         }
     }
 
     public void removePortletTab(User user, String tabName) {
         PortletTabbedPane tabbedPane = findTabbedPane(user);
         if (tabbedPane != null) {
             List tabs = tabbedPane.getPortletTabs();
             for (int i = 0; i < tabs.size(); i++) {
                 PortletTab tab = (PortletTab)tabs.get(i);
                 if (tab.getTitle().equals(tabName)) {
                     tabbedPane.removeTabAt(i);
                 }
             }
         }
     }
 
     /**
      * Returns the list of portlet tabs for a user
      */
     public List getPortletTabs(User user) {
         PortletTabbedPane tabbedPane = findTabbedPane(user);
         if (tabbedPane != null) {
             return tabbedPane.getPortletTabs();
         } else {
             return new ArrayList();
         }
     }
 
     /**
      * Locate the first tabbed pane in a users layout.xml
      *
      * @parameter user the user object
      * @return a portlet tabbed pane or null if none exists
      */
     protected PortletTabbedPane findTabbedPane(User user) {
         List compList = getComponentIdentifierList(user);
         Iterator it = compList.iterator();
         PortletTabbedPane tabbedPane = null;
         while (it.hasNext()) {
             ComponentIdentifier id = (ComponentIdentifier)it.next();
             if (id.getClassName().equals("org.gridlab.gridsphere.layout.PortletTabbedPane")) {
                 tabbedPane = (PortletTabbedPane)id.getPortletComponent();
             }
         }
         return tabbedPane;
     }
 
 }
