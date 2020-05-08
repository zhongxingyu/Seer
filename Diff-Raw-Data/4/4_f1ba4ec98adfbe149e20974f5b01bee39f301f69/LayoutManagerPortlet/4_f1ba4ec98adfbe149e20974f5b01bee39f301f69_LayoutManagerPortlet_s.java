 /*
  * @author <a href="mailto:michael.russell@aei.mpg.de">Michael Russell</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.portlets.core.layout;
 
 import org.gridlab.gridsphere.portlet.*;
 import org.gridlab.gridsphere.portlet.service.PortletServiceException;
 import org.gridlab.gridsphere.provider.event.FormEvent;
 import org.gridlab.gridsphere.provider.portlet.ActionPortlet;
 import org.gridlab.gridsphere.provider.portletui.beans.*;
 import org.gridlab.gridsphere.provider.portletui.model.DefaultTableModel;
 import org.gridlab.gridsphere.services.core.layout.LayoutManagerService;
 import org.gridlab.gridsphere.layout.*;
 
 import javax.servlet.UnavailableException;
 import java.util.*;
 
 public class LayoutManagerPortlet extends ActionPortlet {
 
     // JSP pages used by this portlet
     public static final String VIEW_JSP = "layout/view.jsp";
     public static final String EDIT_JSP = "layout/edit.jsp";
 
     public static final String CONFIGURE_JSP = "layout/configure.jsp";
 
     // Portlet services
     private LayoutManagerService layoutMgr = null;
 
     public void init(PortletConfig config) throws UnavailableException {
         super.init(config);
         this.log.debug("Entering initServices()");
         try {
             this.layoutMgr = (LayoutManagerService)config.getContext().getService(LayoutManagerService.class);
         } catch (PortletServiceException e) {
             log.error("Unable to initialize services!", e);
         }
         this.log.debug("Exiting initServices()");
         //portletMgr = PortletManager.getInstance();
 
         DEFAULT_VIEW_PAGE = "doShowLayout";
 
     }
 
     public void initConcrete(PortletSettings settings) throws UnavailableException {
         super.initConcrete(settings);
     }
 
     public void refreshLayout(FormEvent event) {
         PortletRequest req = event.getPortletRequest();
         layoutMgr.refreshPage(req);
     }
 
     public void doShowLayout(FormEvent event) {
         PortletRequest req = event.getPortletRequest();
         PortletResponse res = event.getPortletResponse();
         ListBoxBean themeLB = event.getListBoxBean("themeLB");
         themeLB.clear();
         String themes = getPortletSettings().getAttribute("supported-themes");
         StringTokenizer st = new StringTokenizer(themes, ",");
         while (st.hasMoreTokens()) {
             ListBoxItemBean lb = new ListBoxItemBean();
             String val = (String)st.nextElement();
             lb.setValue(val.trim());
             themeLB.addBean(lb);
         }
 
         // put tab configuration in request
         String[] tabNames = layoutMgr.getTabNames(req);
         String tabName = event.getHiddenFieldBean("tabHF").getValue();
         if (tabName == null) {
             tabName = tabNames[0];
             System.err.println("Set tab: " + tabName);
             event.getHiddenFieldBean("tabHF").setValue(tabName);
         } else {
             System.err.println("found a tab: " + tabName);
         }
         ListBoxBean seltabsLB = event.getListBoxBean("seltabsLB");
         seltabsLB.clear();
         TableCellBean tabsTC = event.getTableCellBean("tabsTC");
         for (int i = 0; i < tabNames.length; i++) {
             ListBoxItemBean item = new ListBoxItemBean();
             item.setValue(tabNames[i]);
             item.setName(String.valueOf(i));
             seltabsLB.addBean(item);
             TextFieldBean tf = new TextFieldBean();
             tf.setBeanId("tab" + i);
             tf.setValue(tabNames[i]);
             tabsTC.addBean(tf);
             if (tabNames[i].equals(tabName)) item.setSelected(true);
         }
 
         ListBoxBean deltabsLB = event.getListBoxBean("deltabsLB");
         deltabsLB.clear();
         for (int i = 0; i < tabNames.length; i++) {
             ListBoxItemBean item = new ListBoxItemBean();
             item.setValue(tabNames[i]);
             item.setName(String.valueOf(i));
             deltabsLB.addBean(item);
         }
         //req.setAttribute("tabNames", tabNames);
 
         // put sub tab configuration in request
         String[] subtabNames = layoutMgr.getSubTabNames(req, tabName);
         String subtabName = event.getHiddenFieldBean("subtabHF").getValue();
         if (subtabName == null) {
             subtabName = subtabNames[0];
             event.getHiddenFieldBean("subtabHF").setValue(subtabName);
         }
         ListBoxBean selsubtabsLB = event.getListBoxBean("selsubtabsLB");
         selsubtabsLB.clear();
         TableCellBean subtabsTC = event.getTableCellBean("subtabsTC");
         for (int i = 0; i < subtabNames.length; i++) {
             ListBoxItemBean item = new ListBoxItemBean();
             item.setValue(subtabNames[i]);
             //item.setName(String.valueOf(i));
             selsubtabsLB.addBean(item);
             TextFieldBean tf = new TextFieldBean();
             tf.setBeanId("subtab" + i);
             tf.setValue(subtabNames[i]);
             subtabsTC.addBean(tf);
             if (subtabNames[i].equals(subtabName)) item.setSelected(true);
         }
 
         ListBoxBean delsubtabsLB = event.getListBoxBean("delsubtabsLB");
         delsubtabsLB.clear();
         for (int i = 0; i < subtabNames.length; i++) {
             ListBoxItemBean item = new ListBoxItemBean();
             item.setValue(subtabNames[i]);
             item.setName(String.valueOf(i));
             delsubtabsLB.addBean(item);
         }
 
 
         //req.setAttribute("subtabNames", subtabNames);
 
         // create portlet frame layout
         FrameBean frame = event.getFrameBean("portletLayout");
         DefaultTableModel model = new DefaultTableModel();
 
 
 
         System.err.println(subtabName);
         PortletTableLayout table = layoutMgr.getPortletLayout(req, subtabName);
         PortletComponent c = null;
         int j = 0;
 
         // this needs fixing with the portlets available to the user and not the ones that are
         // already in their layout
         List pnames = layoutMgr.getSubscribedPortlets(req);
 
         if (table != null) {
             List rowlayouts = table.getPortletComponents();
             Iterator it = rowlayouts.iterator();
             int k = 0;
             // iterate over <row-layout>
             while (it.hasNext()) {
                 c = (PortletComponent)it.next();
                 if (c instanceof PortletRowLayout) {
                     TableRowBean tr = new TableRowBean();
 
                     PortletRowLayout row = (PortletRowLayout)c;
                     List collayouts = row.getPortletComponents();
                     Iterator colit = collayouts.iterator();
                     // iterate over <column-layout>
                     while (colit.hasNext()) {
                         c = (PortletComponent)colit.next();
                         if (c instanceof PortletColumnLayout) {
                             PortletColumnLayout col = (PortletColumnLayout)c;
                             List pframes = col.getPortletComponents();
                             if (pframes.isEmpty()) {
                                 TableCellBean tc = new TableCellBean();
                                 ListBoxBean lb = new ListBoxBean();
                                 lb.setBeanId("portletframeLB" + j);
 
                                 Iterator pnit = pnames.iterator();
                                 // Create a listbox with all portlets available
                                 while (pnit.hasNext()) {
                                     String concID = (String)pnit.next();
                                     ListBoxItemBean item = new ListBoxItemBean();
                                     item.setName(concID);
                                     int li = concID.lastIndexOf(".");
                                     String display = concID.substring(0, li);
                                     li = display.lastIndexOf(".");
                                     display = display.substring(li+1);
                                     item.setValue(display);
                                     lb.addBean(item);
                                 }
                                 tc.addBean(lb);
                                 tr.addBean(tc);
 
 
                             } else {
                                 Iterator pit = pframes.iterator();
                                 // iterate over <portlet-frame> -- in principle should be one
                                 while (pit.hasNext()) {
                                     TableCellBean tc = new TableCellBean();
                                     c = (PortletComponent)pit.next();
                                     if (c instanceof PortletFrame) {
                                         PortletFrame pframe = (PortletFrame)c;
                                         ListBoxBean lb = new ListBoxBean();
                                         lb.setBeanId("portletframeLB" + j);
 
                                         Iterator pnit = pnames.iterator();
                                         // Create a listbox with all portlets available
                                         while (pnit.hasNext()) {
                                             String concID = (String)pnit.next();
                                             ListBoxItemBean item = new ListBoxItemBean();
                                             item.setName(concID);
                                             int li = concID.lastIndexOf(".");
                                             String display = concID.substring(0, li);
                                             li = display.lastIndexOf(".");
                                             display = display.substring(li+1);
                                             item.setValue(display);
                                             if (pframe.getPortletClass().equals(concID)) item.setSelected(true);
                                             lb.addBean(item);
                                         }
 
                                         tc.addBean(lb);
                                         tr.addBean(tc);
 
 
                                         j++;
                                     } else {
                                         log.error("Unable to find a <portlet-frame> beneath the <portlet-column> in descriptor");
                                     }
                                 }
                             }
 
                         }
                     }
 
                     TableCellBean tc = new TableCellBean();
                     ActionSubmitBean delrow = new ActionSubmitBean();
 
                     delrow.setValue(this.getLocalizedText(req, "LAYOUT_DELROW"));
                     DefaultPortletAction portletAction = new DefaultPortletAction("delTableRow");
                     portletAction.addParameter("idx", String.valueOf(k));
 
                     PortletURI uri = res.createURI();
                     uri.addAction(portletAction);
                     delrow.setName(uri.toString());
                     delrow.setAction(portletAction.toString());
                     ActionSubmitBean newcell = new ActionSubmitBean();
                     portletAction = new DefaultPortletAction("addNewPortlet");
                     portletAction.addParameter("idx", String.valueOf(k));
                     uri = res.createURI();
                     uri.addAction(portletAction);
                     newcell.setName(uri.toString());
                     newcell.setAction(portletAction.toString());
                     newcell.setValue(this.getLocalizedText(req, "LAYOUT_ADDPORTLET"));
                     tc.addBean(delrow);
                     tc.addBean(newcell);
                     tr.addBean(tc);
                     model.addTableRowBean(tr);
                     k++;
                 }
 
 
             }
 
             frame.setTableModel(model);
         } else {
             log.error("Unable to find a <table-layout> beneath the <portlet-tab title=" + subtabNames[0] + " in descriptor");
         }
 
         setNextState(req, VIEW_JSP);
     }
 
     public void saveTheme(FormEvent event) {
         PortletRequest req = event.getPortletRequest();
         ListBoxBean themeLB = event.getListBoxBean("themeLB");
         String theme = themeLB.getSelectedValue();
         layoutMgr.setTheme(req, theme);
         layoutMgr.reloadPage(req);
 
     }
 
     public void saveTabs(FormEvent event) {
         PortletRequest req = event.getPortletRequest();
 
 
         String[] tabNames = layoutMgr.getTabNames(req);
         for (int i = 0; i < tabNames.length; i++) {
             TextFieldBean tf = event.getTextFieldBean("tab" + i);
             String newtitle = tf.getValue();
             if (newtitle == null) newtitle = "Untitled " + i + 1;
             tabNames[i] = newtitle;
             System.err.println("settng " + tabNames[i]);
         }
 
         layoutMgr.setTabNames(req, tabNames);
         layoutMgr.reloadPage(req);
 
     }
 
     public void deleteTab(FormEvent event) {
         PortletRequest req = event.getPortletRequest();
         String[] tabNames = null;
         ListBoxBean tabsLB = event.getListBoxBean("deltabsLB");
         String tabnum = "";
         if (tabsLB.getSelectedValue() != null) {
             tabnum = tabsLB.getSelectedValue();
             System.err.println("retrieve tab num:" + tabnum);
             tabNames = layoutMgr.getTabNames(req);
             //System.err.println("retrieved tab names:" + tabnum);
 
         if (tabNames != null) {
             int tabNum = Integer.parseInt(tabnum);
             String tabName = tabNames[tabNum];
             System.err.println("removing tab:" + tabName);
             layoutMgr.removeTab(req, tabName);
             layoutMgr.reloadPage(req);
         }
         }
 
     }
 
     public void deleteSubTab(FormEvent event) {
         PortletRequest req = event.getPortletRequest();
         ListBoxBean tabsLB = event.getListBoxBean("subtabsLB");
         HiddenFieldBean hf = event.getHiddenFieldBean("tabHF");
         String tabNameHF = hf.getValue();
         String[] tabNames = null;
         String tabnum = "";
         if (tabsLB.getSelectedValue() != null) {
             tabnum = tabsLB.getSelectedValue();
             System.err.println("retrieve tab num:" + tabnum);
             tabNames = layoutMgr.getSubTabNames(req, tabNameHF);
             if (tabNames != null) {
                 int tabNum = Integer.parseInt(tabnum);
                 String tabName = tabNames[tabNum];
                 System.err.println("removing tab:" + tabName);
                 layoutMgr.removeTab(req, tabName);
                 layoutMgr.reloadPage(req);
             }
         }
     }
 
     public void createTab(FormEvent event) {
         PortletRequest req = event.getPortletRequest();
         PortletPage page = layoutMgr.getPortletPage(req);
         PortletTabbedPane pane = page.getPortletTabbedPane();
         PortletTab toptab = new PortletTab();
         String tabName = event.getTextFieldBean("newTab").getValue();
         toptab.setTitle(tabName);
 
         PortletTab tab = new PortletTab();
         String title = this.getLocalizedText(req, "LAYOUT_UNTITLED_TAB");
         tab.setTitle(title);
         PortletTabbedPane newpane = new PortletTabbedPane();
         newpane.setStyle("sub-menu");
         toptab.setPortletComponent(newpane);
         PortletTableLayout table = new PortletTableLayout();
         PortletRowLayout row = new PortletRowLayout();
         PortletColumnLayout col = new PortletColumnLayout();
         row.addPortletComponent(col);
         table.addPortletComponent(row);
         tab.setPortletComponent(table);
         newpane.addTab(tab);
         pane.addTab(toptab);
         layoutMgr.reloadPage(req);
     }
 
     public void createSubTab(FormEvent event) {
         PortletRequest req = event.getPortletRequest();
         PortletPage page = layoutMgr.getPortletPage(req);
         PortletTabbedPane pane = page.getPortletTabbedPane();
 
         HiddenFieldBean tabHF = event.getHiddenFieldBean("tabHF");
         String tabname = tabHF.getValue();
 
         System.err.println("getting tab " + tabname);
 
         PortletTab toptab = pane.getPortletTab(tabname);
         PortletTabbedPane subpane = (PortletTabbedPane)toptab.getPortletComponent();
 
         PortletTab tab = new PortletTab();
         String subtabName = event.getTextFieldBean("newSubTab").getValue();
         tab.setTitle(subtabName);
 
         PortletTableLayout table = new PortletTableLayout();
         PortletRowLayout row = new PortletRowLayout();
         PortletColumnLayout col = new PortletColumnLayout();
         row.addPortletComponent(col);
         table.addPortletComponent(row);
         tab.setPortletComponent(table);
         subpane.addTab(tab);
         layoutMgr.reloadPage(req);
     }
 
     public void selectTab(FormEvent event) {
         PortletRequest req = event.getPortletRequest();
         ListBoxBean tabsLB = event.getListBoxBean("seltabsLB");
         HiddenFieldBean tabsHF = event.getHiddenFieldBean("tabHF");
 
         //event.getHiddenFieldBean("tabHF").setValue("howdy ho");
 
         String selTab = tabsLB.getSelectedValue();
 
         String[] tabNames = layoutMgr.getTabNames(req);
         String tabname = tabNames[Integer.parseInt(selTab)];
         tabsHF.setValue(tabname);
 
         HiddenFieldBean subtabHF = event.getHiddenFieldBean("subtabHF");
         String[] subtabNames = layoutMgr.getSubTabNames(req, tabname);
         selTab = subtabNames[0];
 
         subtabHF.setValue(selTab);
 
     }
 
     public void selectSubTab(FormEvent event) {
         PortletRequest req = event.getPortletRequest();
         ListBoxBean tabsLB = event.getListBoxBean("selsubtabsLB");
         HiddenFieldBean subtabHF = event.getHiddenFieldBean("subtabHF");
 
         //event.getHiddenFieldBean("subtabHF").setValue("howdy ho");
 
         String selTab = tabsLB.getSelectedValue();
 
         System.err.println("selected tab!!" + selTab);
 
         subtabHF.setValue(selTab);
 
     }
 
 
     public void saveSubTabs(FormEvent event) {
         PortletRequest req = event.getPortletRequest();
 
         String tabName = event.getHiddenFieldBean("tabHF").getValue();
 
         String[] tabNames = layoutMgr.getSubTabNames(req, tabName);
         for (int i = 0; i < tabNames.length; i++) {
             TextFieldBean tf = event.getTextFieldBean("subtab" + i);
             String newtitle = tf.getValue();
             String title = this.getLocalizedText(req, "LAYOUT_UNTITLED_TAB");
             if (newtitle == null) newtitle = title + " " + i + 1;
             tabNames[i] = newtitle;
             System.err.println("setting " + tabNames[i]);
         }
 
         layoutMgr.setSubTabNames(req, tabName, tabNames);
         layoutMgr.reloadPage(req);
     }
 
     public void saveTabName(FormEvent event) {
         PortletRequest req = event.getPortletRequest();
         TextFieldBean tabTF = event.getTextFieldBean("tabTF");
         String tabName = tabTF.getValue();
         HiddenFieldBean hf = event.getHiddenFieldBean("tabNumHF");
         String[] tabs = layoutMgr.getTabNames(req);
         int tabNum = Integer.parseInt(hf.getValue());
         tabs[tabNum] = tabName;
         layoutMgr.setTabNames(req, tabs);
         applyChanges(event);
         setNextState(req, "editTab");
     }
 
     public void addTableRow(FormEvent event) {
         PortletRequest req = event.getPortletRequest();
         String subtabName = event.getHiddenFieldBean("subtabHF").getValue();
         PortletTableLayout table = layoutMgr.getPortletLayout(req, subtabName);
         PortletColumnLayout col = new PortletColumnLayout();
         PortletRowLayout row = new PortletRowLayout();
         row.addPortletComponent(col);
         table.addPortletComponent(row);
         //List comps = table.getPortletComponents();
 
     }
 
     public void delTableRow(FormEvent event) {
         PortletRequest req = event.getPortletRequest();
         String subtabName = event.getHiddenFieldBean("subtabHF").getValue();
         PortletTableLayout table = layoutMgr.getPortletLayout(req, subtabName);
         List comps = table.getPortletComponents();
 
         String idx = event.getAction().getParameter("idx");
         try {
             comps.remove(Integer.parseInt(idx));
         } catch (Exception e) {
             //
         }
 
     }
 
     public void addNewPortlet(FormEvent event) {
         PortletRequest req = event.getPortletRequest();
         String subtabName = event.getHiddenFieldBean("subtabHF").getValue();
         PortletTableLayout table = layoutMgr.getPortletLayout(req, subtabName);
         List comps = table.getPortletComponents();
 
         String idx = event.getAction().getParameter("idx");
 
         try {
             PortletComponent c = (PortletComponent)comps.get(Integer.parseInt(idx));
             if (c instanceof PortletRowLayout) {
                 PortletRowLayout row = (PortletRowLayout)c;
                 PortletColumnLayout col = new PortletColumnLayout();
                 //PortletFrame frame = new PortletFrame();
                 //col.addPortletComponent(frame);
                 row.addPortletComponent(col);
 
             }
         } catch (Exception e) {
             // can't parse string to int then forget it
         }
 
     }
 
     public void savePortletFrames(FormEvent event) {
 
         PortletRequest req = event.getPortletRequest();
         String subtabName = event.getHiddenFieldBean("subtabHF").getValue();
         PortletTableLayout table = layoutMgr.getPortletLayout(req, subtabName);
         List comps = table.getPortletComponents();
 
         //String idx = event.getAction().getParameter("idx");
 
         try {
             Iterator rowit = comps.iterator();
             int j = 0;
             while (rowit.hasNext()) {
                 PortletComponent c = (PortletComponent)rowit.next();
                 if (c instanceof PortletRowLayout) {
                     PortletRowLayout row = (PortletRowLayout)c;
 
                     List rowcomps = row.getPortletComponents();
                     Iterator colit = rowcomps.iterator();
                     while (colit.hasNext()) {
                         c = (PortletComponent)colit.next();
                         if (c instanceof PortletColumnLayout) {
 
                             PortletColumnLayout col = (PortletColumnLayout)c;
                             List frames = col.getPortletComponents();
                             ListBoxBean lb = event.getListBoxBean("portletframeLB" + j);
                             String value = lb.getSelectedValue();
 
                             //System.err.println("portlet value " + value);
                             if (value != null) {
                                 if (frames.isEmpty()) {
                                     PortletFrame f = new PortletFrame();
                                     f.setPortletClass(value);
                                     col.addPortletComponent(f);
                                 } else {
                                     PortletFrame f = (PortletFrame)frames.get(0);
                                     f.setPortletClass(value);
                                 }
                             }
                         }
                         j++;
 
                     }
                 }
 
             }
         } catch (Exception e) {
             // can't parse string to int then forget it
         }
         layoutMgr.reloadPage(req);
     }
 
     public void saveSubTabName(FormEvent event) {
         PortletRequest req = event.getPortletRequest();
         TextFieldBean tabTF = event.getTextFieldBean("subtabTF");
         String subtabName = tabTF.getValue();
         HiddenFieldBean hf = event.getHiddenFieldBean("tabNumHF");
         String[] tabs = layoutMgr.getTabNames(req);
         int tabNum = Integer.parseInt(hf.getValue());
         String tabName = tabs[tabNum];
         HiddenFieldBean subhf = event.getHiddenFieldBean("subtabNumHF");
         String[] subtabs = layoutMgr.getSubTabNames(req, tabName);
         int subtabNum = Integer.parseInt(subhf.getValue());
         subtabs[subtabNum] = subtabName;
         layoutMgr.setSubTabNames(req, tabName, subtabs);
         applyChanges(event);
         setNextState(req, "editSubTab");
     }
 
     public void applyChanges(FormEvent event) {
         PortletRequest req = event.getPortletRequest();
 
         layoutMgr.reloadPage(req);
     }
 
 
     public void doConfigureLayout(FormEvent event) throws PortletException {
         PortletRequest req = event.getPortletRequest();
 
         String themes = getPortletSettings().getAttribute("supported-themes");
         TextFieldBean themesTF = event.getTextFieldBean("themesTF");
         themesTF.setValue(themes);
 
         setNextState(req, CONFIGURE_JSP);
     }
 
 
 }
