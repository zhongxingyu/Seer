 package org.gridsphere.portlets.core.admin.layout;
 
 import org.gridsphere.layout.*;
 import org.gridsphere.portlet.impl.SportletProperties;
 import org.gridsphere.portletcontainer.ApplicationPortlet;
 import org.gridsphere.portletcontainer.DefaultPortletAction;
 import org.gridsphere.portletcontainer.GridSphereEvent;
 import org.gridsphere.portletcontainer.impl.GridSphereEventImpl;
 import org.gridsphere.provider.event.jsr.ActionFormEvent;
 import org.gridsphere.provider.event.jsr.FormEvent;
 import org.gridsphere.provider.event.jsr.RenderFormEvent;
 import org.gridsphere.provider.portlet.jsr.ActionPortlet;
 import org.gridsphere.provider.portletui.beans.*;
 import org.gridsphere.services.core.content.ContentFile;
 import org.gridsphere.services.core.content.ContentManagerService;
 import org.gridsphere.services.core.portal.PortalConfigService;
 import org.gridsphere.services.core.registry.PortletRegistryService;
 import org.gridsphere.services.core.security.role.PortletRole;
 import org.gridsphere.services.core.security.role.RoleManagerService;
 
 import javax.portlet.*;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.File;
 import java.io.IOException;
 import java.util.*;
 
 
 public class LayoutManagerPortlet extends ActionPortlet {
 
 
     public static final String VIEW_JSP = "admin/layout/view.jsp";
 
     private static Map<String, PortletPage> pages = new HashMap<String, PortletPage>();
     private static RoleManagerService roleManagerService;
     private static ContentManagerService contentManagerService;
     private static PortalConfigService portalConfigService = null;
     private PortletRegistryService portletRegistryService = null;
     private PortletPageFactory pageFactory = null;
 
     private static final String SELECTED_LAYOUT = LayoutManagerPortlet.class.getName() + ".SELECTED_LAYOUT";
 
     public void init(PortletConfig config) throws PortletException {
         super.init(config);
         roleManagerService = (RoleManagerService) createPortletService(RoleManagerService.class);
         contentManagerService = (ContentManagerService) createPortletService(ContentManagerService.class);
         portletRegistryService = (PortletRegistryService) createPortletService(PortletRegistryService.class);
         portalConfigService = (PortalConfigService) createPortletService(PortalConfigService.class);
         pageFactory = PortletPageFactory.getInstance();
         DEFAULT_VIEW_PAGE = "doShowLayout";
     }
 
     public void savePageDetails(ActionFormEvent event) {
         PortletRequest req = event.getActionRequest();
         String sessionId = req.getPortletSession().getId();
         PortletPage page = (PortletPage) pages.get(sessionId);
         String title = event.getTextFieldBean("titleTF").getValue();
         page.setTitle(title);
         String keywords = event.getTextFieldBean("keywordsTF").getValue();
         page.setKeywords(keywords);
         pageFactory.savePortletPageMaster(page);
     }
 
     public void editComponent(RenderFormEvent event) throws PortletException, IOException {
         PortletRequest req = event.getRenderRequest();
         PortletResponse res = event.getRenderResponse();
         PortletContext context = getPortletConfig().getPortletContext();
         GridSphereEvent gsevent = new GridSphereEventImpl(context, (HttpServletRequest) req, (HttpServletResponse) res);
         String sessionId = req.getPortletSession().getId();
         PortletPage page = (PortletPage) pages.get(sessionId);
         page.actionPerformed(gsevent);
         pages.put(sessionId, page);
 
         setNextState(req, DEFAULT_VIEW_PAGE);
 
     }
 
     public PortletPage createLayout(FormEvent event, PortletRequest req) throws PortletException, IOException {
 
         PortletSession session = req.getPortletSession();
         String layoutId = (String) session.getAttribute(SELECTED_LAYOUT);
 
         PortletPage page = pageFactory.createPortletPageCopy(layoutId);
 
         pageFactory.setPageTheme(page, req);
 
         page.init(req, new ArrayList<ComponentIdentifier>());
 
         return page;
     }
 
     /*
     public void doFinish(FormEvent event) throws PortletException, IOException {
         PortletSession session = event.getPortletRequest().getPortletSession();
         session.setAttribute(SportletProperties.LAYOUT_PAGE, PortletPageFactory.USER_PAGE);
         session.removeAttribute(SELECTED_LAYOUT);
         pages.remove(session.getId());
     }
     */
 
     public void doSaveFrame(ActionFormEvent event) throws PortletException, IOException {
         PortletRequest req = event.getActionRequest();
 
         String sessionId = req.getPortletSession().getId();
         PortletPage page = (PortletPage) pages.get(sessionId);
 
         HiddenFieldBean compHF = event.getHiddenFieldBean("compHF");
         String activeComp = compHF.getValue();
         String reqRole = event.getListBoxBean("rolesLB").getSelectedName();
         String portletClass = event.getListBoxBean("portletsLB").getSelectedName();
         String label = event.getTextFieldBean("labelTF").getValue();
         PortletComponent comp = page.getActiveComponent(activeComp);
 
         if (comp instanceof PortletFrame) {
             PortletFrame frame = (PortletFrame) comp;
             if (reqRole.toUpperCase().equals("NONE")) reqRole = "";
             frame.setRequiredRole(reqRole);
             frame.setLabel(label);
             frame.setPortletClass(portletClass);
             log.debug("setting frame class to " + portletClass);
             RadioButtonBean istitleRB = event.getRadioButtonBean("istitleRB");
             if (!istitleRB.getSelectedValue().equalsIgnoreCase("yes")) {
                 frame.setTransparent(true);
             }
             page.init(req, new ArrayList<ComponentIdentifier>());
         }
 
         pageFactory.savePortletPageMaster(page);
 
     }
 
     public void doSaveBar(ActionFormEvent event) throws PortletException, IOException {
         PortletRequest req = event.getActionRequest();
 
         String sessionId = req.getPortletSession().getId();
         PortletPage page = (PortletPage) pages.get(sessionId);
 
 
         HiddenFieldBean compHF = event.getHiddenFieldBean("compHF");
         String activeComp = compHF.getValue();
 
         PortletComponent comp = page.getActiveComponent(activeComp);
 
         if (comp instanceof PortletBar) {
             PortletBar bar = (PortletBar) comp;
             ListBoxBean colsLB = event.getListBoxBean("colsLB");
             String colTemplateNum = colsLB.getSelectedName();
             if (colTemplateNum != null) {
                 PortletComponent c = createLayoutStrategy(colTemplateNum, bar.getPortletComponent());
                 bar.setPortletComponent(c);
             }
         }
 
         pageFactory.savePortletPageMaster(page);
     }
 
     public void doSaveTab(ActionFormEvent event) throws PortletException, IOException {
         PortletRequest req = event.getActionRequest();
 
         String sessionId = req.getPortletSession().getId();
         PortletPage page = (PortletPage) pages.get(sessionId);
         HiddenFieldBean compHF = event.getHiddenFieldBean("compHF");
         String activeComp = compHF.getValue();
         String reqRole = event.getListBoxBean("rolesLB").getSelectedName();
         String name = event.getTextFieldBean("nameTF").getValue();
         String label = event.getTextFieldBean("labelTF").getValue();
         PortletComponent comp = page.getActiveComponent(activeComp);
 
         if (comp instanceof PortletTab) {
             PortletTab tab = (PortletTab) comp;
             if (reqRole.equalsIgnoreCase("NONE")) reqRole = "";
             tab.setRequiredRole(reqRole);
             tab.setLabel(label);
             tab.setTitle(req.getLocale().getLanguage(), name);
             ListBoxBean colsLB = event.getListBoxBean("colsLB");
             String colTemplateNum = colsLB.getSelectedName();
             if (colTemplateNum != null) {
                 PortletComponent table = createLayoutStrategy(colTemplateNum, tab.getPortletComponent());
                 tab.setPortletComponent(table);
                 PortletNavMenu parent = (PortletNavMenu) tab.getParentComponent();
                 parent.setSelectedPortletTab(tab);
             }
         }
 
         pageFactory.savePortletPageMaster(page);
     }
 
     public void doSaveNewTab(ActionFormEvent event) throws PortletException, IOException {
         PortletRequest req = event.getActionRequest();
 
         String sessionId = req.getPortletSession().getId();
         PortletPage page = (PortletPage) pages.get(sessionId);
 
         HiddenFieldBean compHF = event.getHiddenFieldBean("compHF");
         String activeComp = compHF.getValue();
         String reqRole = event.getListBoxBean("rolesLB").getSelectedName();
         String name = event.getTextFieldBean("nameTF").getValue();
         String label = event.getTextFieldBean("labelTF").getValue();
         PortletComponent comp = page.getActiveComponent(activeComp);
         //PortletComponent comp = active.getParentComponent();
 
         log.debug("compHF=" + activeComp);
         log.debug("comp class= " + comp.getClass());
         log.debug("active comp = " + comp.getComponentID());
         if (comp instanceof PortletTabbedPane) {
             PortletTabbedPane pane = (PortletTabbedPane) comp;
             //System.err.println("tab name " + thistab.getTitle("en"));
             //PortletTabbedPane pane = (PortletTabbedPane)thistab.getParentComponent();
             PortletTab tab = new PortletTab();
             tab.setTitle(req.getLocale().getLanguage(), name);
             if (reqRole.equalsIgnoreCase("NONE")) reqRole = "";
             tab.setRequiredRole(reqRole);
             tab.setLabel(label);
             if (pane.getStyle().equals("menu")) {
                 //RadioButtonBean addSubTab = event.getRadioButtonBean("subcompRB");
                 //if (addSubTab.getSelectedValue().equals("double")) {
                 PortletTabbedPane newpane = new PortletTabbedPane();
                 newpane.setStyle("sub-menu");
                 tab.setPortletComponent(newpane);
                 /*} else {
                     PortletRowLayout row = new PortletRowLayout();
                     PortletColumnLayout col = new PortletColumnLayout();
                     col.setWidth("100%");
                     row.addPortletComponent(col);
                     PortletTableLayout table = new PortletTableLayout();
                     table.addPortletComponent(row);
                     tab.setPortletComponent(table);
                 }*/
             } else if (pane.getStyle().equals("sub-menu")) {
                 ListBoxBean colsLB = event.getListBoxBean("colsLB");
                 String colTemplateNum = colsLB.getSelectedName();
                 PortletComponent c = createLayoutStrategy(colTemplateNum, tab.getPortletComponent());
                 tab.setPortletComponent(c);
             }
             pane.addTab(tab);
             pane.setSelectedPortletTab(tab);
 
             pageFactory.savePortletPageMaster(page);
 
             page.init(req, new ArrayList<ComponentIdentifier>());
             pages.put(sessionId, page);
         } else if (comp instanceof PortletMenu) {
             PortletMenu menu = (PortletMenu) comp;
             //System.err.println("tab name " + thistab.getTitle("en"));
             //PortletTabbedPane pane = (PortletTabbedPane)thistab.getParentComponent();
             log.debug("creating new menu tab!");
             PortletTab tab = new PortletTab();
             tab.setTitle(req.getLocale().getLanguage(), name);
             if (reqRole.equalsIgnoreCase("NONE")) reqRole = "";
             tab.setRequiredRole(reqRole);
             tab.setLabel(label);
             ListBoxBean colsLB = event.getListBoxBean("colsLB");
             String colTemplateNum = colsLB.getSelectedName();
             PortletComponent c = createLayoutStrategy(colTemplateNum, tab.getPortletComponent());
             tab.setPortletComponent(c);
             menu.addTab(tab);
             menu.setSelectedPortletTab(tab);
 
             pageFactory.savePortletPageMaster(page);
 
 
             page.init(req, new ArrayList<ComponentIdentifier>());
 
             pages.put(sessionId, page);
         }
 
 
     }
 
     public void doDeleteTab(ActionFormEvent event) throws PortletException, IOException {
         PortletRequest req = event.getActionRequest();
         String sessionId = req.getPortletSession().getId();
         PortletPage page = (PortletPage) pages.get(sessionId);
 
         HiddenFieldBean compHF = event.getHiddenFieldBean("compHF");
         String activeComp = compHF.getValue();
 
         PortletComponent comp = page.getActiveComponent(activeComp);
 
         if (comp instanceof PortletTab) {
             PortletTab tab = (PortletTab) comp;
             PortletNavMenu menu = (PortletNavMenu) tab.getParentComponent();
 
             int index = menu.getIndexOfTab(tab);
 
             if (index < (menu.getTabCount() - 1)) {
                 menu.setSelectedPortletTabIndex(index + 1);
             } else if (index > 0) {
                 menu.setSelectedPortletTabIndex(index - 1);
             }
             menu.removeTab(tab);
 
         }
 
 
         pageFactory.savePortletPageMaster(page);
     }
 
 
     public void doMoveTabLeft(ActionFormEvent event) throws PortletException, IOException {
         PortletRequest req = event.getActionRequest();
         String sessionId = req.getPortletSession().getId();
         PortletPage page = (PortletPage) pages.get(sessionId);
 
         HiddenFieldBean compHF = event.getHiddenFieldBean("compHF");
         String activeComp = compHF.getValue();
 
         PortletComponent comp = page.getActiveComponent(activeComp);
 
         if (comp instanceof PortletTab) {
             PortletTab tab = (PortletTab) comp;
             PortletNavMenu pane = (PortletNavMenu) tab.getParentComponent();
             int index = pane.getIndexOfTab(tab);
             pane.removeTab(tab);
             pane.insertTab(tab, index - 1);
             pane.setSelectedPortletTabIndex(index - 1);
         }
 
 
         pageFactory.savePortletPageMaster(page);
 
     }
 
     public void doMoveTabRight(ActionFormEvent event) throws PortletException, IOException {
         PortletRequest req = event.getActionRequest();
         String sessionId = req.getPortletSession().getId();
         PortletPage page = (PortletPage) pages.get(sessionId);
 
         HiddenFieldBean compHF = event.getHiddenFieldBean("compHF");
         String activeComp = compHF.getValue();
 
         PortletComponent comp = page.getActiveComponent(activeComp);
 
         if (comp instanceof PortletTab) {
             PortletTab tab = (PortletTab) comp;
             PortletNavMenu pane = (PortletNavMenu) tab.getParentComponent();
             int index = pane.getIndexOfTab(tab);
             pane.removeTab(tab);
             pane.insertTab(tab, index + 1);
             pane.setSelectedPortletTabIndex(index + 1);
         }
 
 
         pageFactory.savePortletPageMaster(page);
 
     }
 
     public void doCancel(ActionFormEvent event) throws PortletException, IOException {
         // do nothing
     }
 
     public void doShowLayout(ActionFormEvent event) throws PortletException, IOException {
         doShowLayout(event.getActionRequest(), event.getActionResponse(), event);
     }
 
     public void doShowLayout(RenderFormEvent event) throws PortletException, IOException {
         RenderRequest req = event.getRenderRequest();
         RenderResponse res = event.getRenderResponse();
         doShowLayout(req, res, event);
         setNextState(event.getRenderRequest(), VIEW_JSP);
     }
 
     public void doShowLayout(PortletRequest req, PortletResponse res, FormEvent event) throws PortletException, IOException {
 
         PortletSession session = req.getPortletSession();
 
         Set<String> layoutIds = pageFactory.getEditableLayoutIds();
 
         // set guest page as the selected page
         if (session.getAttribute(SELECTED_LAYOUT) == null) {
             session.setAttribute(SELECTED_LAYOUT, PortletPageFactory.GUEST_PAGE);
         }
 
         String selectedLayout = (String) session.getAttribute(SELECTED_LAYOUT);
         req.setAttribute("pageName", selectedLayout);
 
         ListBoxBean layoutsLB = event.getListBoxBean("layoutsLB");
         layoutsLB.clear();
         for (String layoutId : layoutIds) {
             ListBoxItemBean item = new ListBoxItemBean();
             item.setName(layoutId);
             item.setValue(layoutId);
             if (layoutId.equalsIgnoreCase(selectedLayout)) item.setSelected(true);
             layoutsLB.addBean(item);
         }
 
        String theme = (String) req.getPortletSession().getAttribute(SportletProperties.LAYOUT_THEME, PortletSession.APPLICATION_SCOPE);
        //String theme = portalConfigService.getProperty(PortalConfigService.DEFAULT_THEME);
         String renderkit = (String) req.getPortletSession().getAttribute(SportletProperties.LAYOUT_RENDERKIT, PortletSession.APPLICATION_SCOPE);
 
 
         ListBoxBean themesLB = event.getListBoxBean("themesLB");
 
         themesLB.clear();
 
         String themesPath = getPortletConfig().getPortletContext().getRealPath("/themes");
         /// retrieve the current renderkit
         themesPath += "/" + renderkit;
 
         String[] themes = null;
         File f = new File(themesPath);
         if (f.isDirectory()) {
             themes = f.list();
         }
 
         for (int i = 0; i < themes.length; i++) {
             ListBoxItemBean lb = new ListBoxItemBean();
             lb.setValue(themes[i].trim());
             if (themes[i].trim().equalsIgnoreCase(theme)) lb.setSelected(true);
             themesLB.addBean(lb);
         }
 
         PortletContext context = getPortletConfig().getPortletContext();
 
         // theme has to be set before it is inited
         req.setAttribute(SportletProperties.LAYOUT_EDIT_MODE, "true");
         String cid = (String) req.getAttribute(SportletProperties.COMPONENT_ID);
 
         String sessionId = session.getId();
         String extraURI = "&" + SportletProperties.COMPONENT_ID + "=" + cid +
                 "&" + SportletProperties.DEFAULT_PORTLET_ACTION + "=doShowLayout";
 
         log.debug("extraURI= " + extraURI);
         req.setAttribute(SportletProperties.EXTRA_QUERY_INFO, extraURI);
 
         PortletPage page = (PortletPage) pages.get(sessionId);
         if (page == null) page = createLayout(event, req);
 
         // set page details
         event.getTextFieldBean("titleTF").setValue(page.getTitle());
         event.getTextFieldBean("keywordsTF").setValue(page.getKeywords());
 
         GridSphereEventImpl gsevent = new GridSphereEventImpl(context, (HttpServletRequest) req, (HttpServletResponse) res);
         req.setAttribute(SportletProperties.IGNORE_PARSING, "true");
 
         String controlUI = "";
         String compid = req.getParameter(SportletProperties.COMPONENT_ID_2);
         // put new cid in before render is called
         if (compid != null) {
             gsevent.setComponentID(compid);
             System.err.println("\n\n\nfound compid2 = " + compid);
         }
         String action = req.getParameter(SportletProperties.DEFAULT_PORTLET_ACTION_2);
         if (action != null) {
             gsevent.setAction(new DefaultPortletAction(action));
             System.err.println("found action2 = " + action);
         }
 
         if (req.getParameter("usertable") != null) {
             page.init(req, new ArrayList<ComponentIdentifier>());
             page.actionPerformed(gsevent);
             pageFactory.savePortletPageMaster(page);
             page.init(req, new ArrayList<ComponentIdentifier>());
         }
 
 
         if (compid != null) {
 
             PortletComponent comp = page.getActiveComponent(compid);
 
             if (comp instanceof PortletFrame) {
                 log.debug("it's a frame!");
                 PortletFrame frame = (PortletFrame) comp;
                 // don't perform action on portlet frame
                 if (action.equals(PortletFrame.DELETE_PORTLET)) {
                     PortletComponent parent = frame.getParentComponent();
                     parent.remove(frame);
                     pageFactory.savePortletPageMaster(page);
                 } else {
                     controlUI = "frame";
                 }
                 if (!frame.getTransparent()) {
                     req.setAttribute("isTitle", "true");
                 }
 
                 ListBoxBean portletsLB = event.getListBoxBean("portletsLB");
                 Collection<ApplicationPortlet> appColl = portletRegistryService.getAllApplicationPortlets();
                 Locale loc = req.getLocale();
 
                 for (ApplicationPortlet app : appColl) {
 
                     String concID = app.getConcretePortletID();
                     // we don't want to list PortletServlet loader!
                     // if (concID.startsWith(PortletServlet.class.getName())) continue;
 
                     String dispName = app.getDisplayName(loc);
                     String descName = app.getDescription(loc);
                     ListBoxItemBean item = new ListBoxItemBean();
                     item.setValue(dispName + " - " + descName);
                     item.setName(concID);
                     if (concID.equalsIgnoreCase(frame.getPortletClass())) item.setSelected(true);
                     portletsLB.addBean(item);
 
                 }
             } else if (comp instanceof PortletContent) {
                 if ((action != null) && (!action.equals(""))) {
                     PortletContent content = (PortletContent) comp;
                     PortletComponent parent = content.getParentComponent();
                     parent.remove(content);
                     pageFactory.savePortletPageMaster(page);
                 } else {
                     controlUI = "content";
                     ListBoxBean contentLB = event.getListBoxBean("contentLB");
                     List contentFiles = contentManagerService.getAllContent();
                     for (int i = 0; i < contentFiles.size(); i++) {
                         ContentFile contentFile = (ContentFile) contentFiles.get(i);
                         ListBoxItemBean item = new ListBoxItemBean();
                         item.setName(contentFile.getFile().getName());
                         item.setValue(contentFile.getFile().getName());
                         contentLB.addBean(item);
                     }
                 }
             } else if (comp instanceof PortletTab) {
                 PortletTab tab = (PortletTab) comp;
                 PortletNavMenu pane = (PortletNavMenu) tab.getParentComponent();
                 if (pane.getStyle().equals("menu")) {
                     log.debug("it's a tab!");
                     controlUI = "tab";
                     createColsListBox(event, req, tab.getPortletComponent());
                 } else {
                     log.debug("it's a subtab");
                     controlUI = "subtab";
                     createColsListBox(event, req, tab.getPortletComponent());
                 }
                 log.debug("tab name=" + tab.getTitle(req.getLocale().getLanguage()));
 
                 // if selected tab is first tab disable 'move left' button
                 if ((pane.getIndexOfTab(tab) == 0)) {
                     ActionSubmitBean moveLeftButton = event.getActionSubmitBean("moveLeftButton");
                     moveLeftButton.setDisabled(true);
                 }
                 // if selected tab is last tab disable 'move right' buttom
                 if ((pane.getIndexOfTab(tab) == (pane.getTabCount() - 1))) {
                     ActionSubmitBean moveRightButton = event.getActionSubmitBean("moveRightButton");
                     moveRightButton.setDisabled(true);
                 }
                 log.debug("invoking action on tab/subtab");
 
                 page.actionPerformed(gsevent);
             } else if (comp instanceof PortletBar) {
                 PortletBar bar = (PortletBar) comp;
                 controlUI = "bar";
                 createColsListBox(event, req, bar.getPortletComponent());
 
             }
             boolean itsanewtab = false;
             if (req.getParameter("newtab") != null) {
                 controlUI = "tab";
                 TextFieldBean nameTF = event.getTextFieldBean("nameTF");
                 nameTF.setValue(this.getLocalizedText(req, "LAYOUT_NEW_TAB2"));
                 itsanewtab = true;
                 comp = new PortletTab();
             } else if (req.getParameter("newsubtab") != null) {
                 controlUI = "subtab";
                 TextFieldBean nameTF = event.getTextFieldBean("nameTF");
                 nameTF.setValue(this.getLocalizedText(req, "LAYOUT_NEW_SUBTAB2"));
                 itsanewtab = true;
                 PortletTab tab = new PortletTab();
                 createColsListBox(event, req, tab.getPortletComponent());
                 comp = tab;
             } else if (req.getParameter("newmenutab") != null) {
                 controlUI = "menu";
                 TextFieldBean nameTF = event.getTextFieldBean("nameTF");
                 nameTF.setValue(this.getLocalizedText(req, "LAYOUT_NEW_MENUTAB"));
                 itsanewtab = true;
                 PortletTab tab = new PortletTab();
                 createColsListBox(event, req, tab.getPortletComponent());
                 comp = tab;
             }
             if (itsanewtab) {
                 TextFieldBean labelTF = event.getTextFieldBean("labelTF");
                 labelTF.setValue("");
                 ActionSubmitBean moveLeftButton = event.getActionSubmitBean("moveLeftButton");
                 moveLeftButton.setDisabled(true);
                 ActionSubmitBean moveRightButton = event.getActionSubmitBean("moveRightButton");
                 moveRightButton.setDisabled(true);
                 req.setAttribute("isnewtab", "true");
             }
 
             ListBoxBean rolesLB = event.getListBoxBean("rolesLB");
             rolesLB.clear();
             ListBoxItemBean item = new ListBoxItemBean();
 
             item.setValue(this.getLocalizedText(req, "LAYOUT_ROLE_NONE_REQUIRED"));
             item.setName("NONE");
 
             rolesLB.addBean(item);
             List<PortletRole> roles = roleManagerService.getRoles();
             for (PortletRole role : roles) {
                 item = new ListBoxItemBean();
                 item.setValue(role.getName());
                 item.setName(role.getName());
                 if ((comp != null) && (comp.getRequiredRole() != null)
                         && (comp.getRequiredRole().equalsIgnoreCase(role.getName()))) item.setSelected(true);
                 rolesLB.addBean(item);
             }
 
             HiddenFieldBean compHF = event.getHiddenFieldBean("compHF");
             compHF.setName("compid");
             compHF.setValue(compid);
 
             req.setAttribute("portletComp", comp);
             req.setAttribute("controlUI", controlUI);
         }
 
         StringBuffer pageBuffer = new StringBuffer();
         PortletComponent comp = page.getPortletComponent();
 
         log.debug("rendering the component");
 
 
         comp.doRender(gsevent);
 
         pageBuffer = comp.getBufferedOutput(req);
 
         //System.err.println(pageBuffer);
 
         ListBoxBean navigationLB = event.getListBoxBean("navigationLB");
         navigationLB.clear();
         ListBoxItemBean item = new ListBoxItemBean();
         item.setName("bar");
         item.setValue(this.getLocalizedText(req, "LAYOUT_SINGLE_DIVIDER"));
         if (comp instanceof PortletBar) {
             item.setSelected(true);
         }
         navigationLB.addBean(item);
         item = new ListBoxItemBean();
         item.setName("menu");
         item.setValue(this.getLocalizedText(req, "LAYOUT_MENUBAR"));
         if (comp instanceof PortletMenu) {
             item.setSelected(true);
         }
         navigationLB.addBean(item);
         item = new ListBoxItemBean();
         item.setName("pane");
         item.setValue(this.getLocalizedText(req, "LAYOUT_TABBEDPANE"));
         if (comp instanceof PortletTabbedPane) {
             item.setSelected(true);
         }
         navigationLB.addBean(item);
 
 
         req.setAttribute("pane", pageBuffer.toString());
         pages.put(sessionId, page);
 
         // put old cid back so beans/tags work!!
         req.setAttribute(SportletProperties.COMPONENT_ID, cid);
         // remove special layout attributes so the rest of "real" layout after this portlet renders properly
         req.removeAttribute(SportletProperties.EXTRA_QUERY_INFO);
         req.removeAttribute(SportletProperties.LAYOUT_EDIT_MODE);
 
         //setNextState(req, VIEW_JSP);
     }
 
     public void selectLayout(ActionFormEvent event) throws PortletException, IOException {
         ListBoxBean layoutsLB = event.getListBoxBean("layoutsLB");
         PortletSession session = event.getActionRequest().getPortletSession();
         session.setAttribute(SELECTED_LAYOUT, layoutsLB.getSelectedValue());
         pages.remove(session.getId());
     }
 
     public void selectTheme(ActionFormEvent event) throws PortletException, IOException {
         ListBoxBean themesLB = event.getListBoxBean("themesLB");
         String selectedTheme = themesLB.getSelectedValue();
         portalConfigService.setProperty(PortalConfigService.DEFAULT_THEME, selectedTheme);
         MessageBoxBean msg = event.getMessageBoxBean("msg");
         String value = this.getLocalizedText(event.getActionRequest(), "LAYOUT_SAVETHEMEMSG") + " " + selectedTheme;
         msg.setValue(value);
     }
 
     public void doSaveNav(ActionFormEvent event) {
         PortletRequest req = event.getActionRequest();
 
         String sessionId = req.getPortletSession().getId();
         PortletPage page = (PortletPage) pages.get(sessionId);
 
         PortletComponent navComp = page.getPortletComponent();
         ListBoxBean navLB = event.getListBoxBean("navigationLB");
         String name = navLB.getSelectedName();
 
         if (name.equals("bar")) {
             // the actual component matches the selected one, do nothing
             if (navComp instanceof PortletBar) {
                 return;
             }
             PortletBar bar = new PortletBar();
             // set the first menu tab component to be the bar component
             if (navComp instanceof PortletMenu) {
                 PortletMenu menu = (PortletMenu) navComp;
                 List<PortletTab> tabs = menu.getPortletTabs();
                 PortletTab tab = tabs.get(0);
                 bar.setPortletComponent(tab.getPortletComponent());
             }
             // set the component of the first subtab to be the bar component
             if (navComp instanceof PortletTabbedPane) {
                 PortletTabbedPane pane = (PortletTabbedPane) navComp;
                 List<PortletTab> tabs = pane.getPortletTabs();
                 PortletTab tab = tabs.get(0);
                 PortletTabbedPane subpane = (PortletTabbedPane) tab.getPortletComponent();
                 PortletTab subtab = subpane.getPortletTabAt(0);
                 bar.setPortletComponent(subtab.getPortletComponent());
             }
             page.setPortletComponent(bar);
 
         } else if (name.equals("menu")) {
             if (navComp instanceof PortletMenu) {
                 return;
             }
             PortletMenu menu = new PortletMenu();
             if (navComp instanceof PortletBar) {
                 PortletBar bar = (PortletBar) navComp;
                 PortletTab tab = new PortletTab();
                 tab.setTitle(req.getLocale().getLanguage(), this.getLocalizedText(req, "LAYOUT_DEFAULT_TAB_NAME"));
                 tab.setPortletComponent(bar.getPortletComponent());
                 menu.addTab(tab);
             }
 
             if (navComp instanceof PortletTabbedPane) {
                 PortletTabbedPane pane = (PortletTabbedPane) navComp;
                 List<PortletTab> tabs = pane.getPortletTabs();
                 for (PortletTab atab : tabs) {
                     PortletTabbedPane subpane = (PortletTabbedPane) atab.getPortletComponent();
                     List<PortletTab> subtabs = subpane.getPortletTabs();
                     menu.addTab(subtabs.get(0));
 
                 }
             }
             page.setPortletComponent(menu);
         } else if (name.equals("pane")) {
             if (navComp instanceof PortletTabbedPane) {
                 return;
             }
             PortletTabbedPane pane = new PortletTabbedPane();
             pane.setStyle("menu");
             if (navComp instanceof PortletBar) {
                 PortletTab newtab = new PortletTab();
                 newtab.setTitle(req.getLocale().getLanguage(), this.getLocalizedText(req, "LAYOUT_DEFAULT_TAB_NAME"));
                 pane.addTab(newtab);
                 PortletTabbedPane subpane = new PortletTabbedPane();
                 subpane.setStyle("sub-menu");
                 newtab.setPortletComponent(subpane);
                 PortletTab subtab = new PortletTab();
                 subtab.setTitle(req.getLocale().getLanguage(), this.getLocalizedText(req, "LAYOUT_DEFAULT_TAB_NAME"));
 
                 PortletTableLayout table = new PortletTableLayout();
                 PortletRowLayout row = new PortletRowLayout();
                 PortletColumnLayout col = new PortletColumnLayout();
                 col.setWidth("100%");
                 row.addPortletComponent(col);
                 table.addPortletComponent(row);
                 subtab.setPortletComponent(table);
             } else if (navComp instanceof PortletMenu) {
                 PortletMenu menu = (PortletMenu) navComp;
                 List<PortletTab> tabs = menu.getPortletTabs();
                 for (PortletTab atab : tabs) {
                     PortletTab newtab = new PortletTab();
                     newtab.setTitle(req.getLocale().getLanguage(), atab.getTitle(req.getLocale().getLanguage()));
                     pane.addTab(newtab);
                     PortletTabbedPane subpane = new PortletTabbedPane();
                     subpane.setStyle("sub-menu");
                     newtab.setPortletComponent(subpane);
                     subpane.addTab(atab);
                 }
 
             }
             page.setPortletComponent(pane);
         }
         pageFactory.savePortletPageMaster(page);
         page.init(req, new ArrayList<ComponentIdentifier>());
         pages.put(sessionId, page);
     }
 
     /**
      * Modifies a portlet tab to provide the desired column layout strategy given a tab
      * The numbering is as follows:
      * "one"  - 1 column
      * "two" - 2 col, 33%, 66%
      * "three" - 2 col, 50%, 50%
      * "four" - 2 col, 66%, 33%
      * "five" - 3 col, 33%, 33%, 33%
      * "six" - 3 col 25%, 50%, 25%
      *
      * @param strategyNum the string as one of the above
      * @param comp        a portlet component
      * @return the updated table layout
      */
     private PortletComponent createLayoutStrategy(String strategyNum, PortletComponent comp) {
         log.debug("col strategy: " + strategyNum);
         if ((comp != null) && (comp instanceof PortletTableLayout)) {
             PortletTableLayout table = (PortletTableLayout) comp;
             List rows = table.getPortletComponents();
             if ((rows != null) && (!rows.isEmpty())) {
                 PortletComponent c = (PortletComponent) rows.get(0);
                 if (c instanceof PortletRowLayout) {
                     PortletRowLayout row = (PortletRowLayout) c;
                     List cols = row.getPortletComponents();
                     System.err.println("cols size= " + cols.size());
                     if (cols.size() == 1) {
                         if (strategyNum.equals("one")) {
                             return table;
                         }
                         if (strategyNum.equals("two")) {
                             // deal with case where column layout needs to be extended
                             PortletColumnLayout oldcol = (PortletColumnLayout) cols.get(0);
                             PortletColumnLayout col = new PortletColumnLayout();
                             oldcol.setWidth("33%");
                             col.setWidth("66%");
                             row.addPortletComponent(col);
                         }
                         if (strategyNum.equals("three")) {
                             PortletColumnLayout oldcol = (PortletColumnLayout) cols.get(0);
                             PortletColumnLayout col = new PortletColumnLayout();
                             oldcol.setWidth("50%");
                             col.setWidth("50%");
                             row.addPortletComponent(col);
                         }
                         if (strategyNum.equals("four")) {
                             PortletColumnLayout oldcol = (PortletColumnLayout) cols.get(0);
                             PortletColumnLayout col = new PortletColumnLayout();
                             oldcol.setWidth("66%");
                             col.setWidth("33%");
                             row.addPortletComponent(col);
                         }
                         if (strategyNum.equals("five")) {
                             PortletColumnLayout oldcol = (PortletColumnLayout) cols.get(0);
                             PortletColumnLayout col = new PortletColumnLayout();
                             PortletColumnLayout newcol = new PortletColumnLayout();
                             oldcol.setWidth("33%");
                             col.setWidth("33%");
                             newcol.setWidth("33%");
                             row.addPortletComponent(col);
                             row.addPortletComponent(newcol);
                         }
                         if (strategyNum.equals("six")) {
                             PortletColumnLayout oldcol = (PortletColumnLayout) cols.get(0);
                             PortletColumnLayout col = new PortletColumnLayout();
                             PortletColumnLayout newcol = new PortletColumnLayout();
                             oldcol.setWidth("25%");
                             col.setWidth("50%");
                             newcol.setWidth("25%");
                             row.addPortletComponent(col);
                             row.addPortletComponent(newcol);
                         }
                     }
 
                     if (cols.size() == 2) {
                         if (strategyNum.equals("one")) {
                             // deal with case where column layout needs to be reduced
                             PortletColumnLayout col = (PortletColumnLayout) cols.get(0);
                             PortletColumnLayout oldcol = (PortletColumnLayout) cols.get(1);
                             col.setWidth("100%");
                             row.removePortletComponent(oldcol);
                         }
                         if (strategyNum.equals("two")) {
                             PortletColumnLayout oldcol = (PortletColumnLayout) cols.get(0);
                             PortletColumnLayout col = (PortletColumnLayout) cols.get(1);
                             oldcol.setWidth("33%");
                             col.setWidth("66%");
                         }
                         if (strategyNum.equals("three")) {
                             PortletColumnLayout oldcol = (PortletColumnLayout) cols.get(0);
                             PortletColumnLayout col = (PortletColumnLayout) cols.get(1);
                             oldcol.setWidth("50%");
                             col.setWidth("50%");
                         }
                         if (strategyNum.equals("four")) {
                             PortletColumnLayout oldcol = (PortletColumnLayout) cols.get(0);
                             PortletColumnLayout col = (PortletColumnLayout) cols.get(1);
                             oldcol.setWidth("66%");
                             col.setWidth("33%");
                         }
                         if (strategyNum.equals("five")) {
                             PortletColumnLayout oldcol = (PortletColumnLayout) cols.get(0);
                             PortletColumnLayout col = (PortletColumnLayout) cols.get(1);
                             PortletColumnLayout newcol = new PortletColumnLayout();
                             oldcol.setWidth("33%");
                             col.setWidth("33%");
                             newcol.setWidth("33%");
                             row.addPortletComponent(newcol);
                         }
                         if (strategyNum.equals("six")) {
                             PortletColumnLayout oldcol = (PortletColumnLayout) cols.get(0);
                             PortletColumnLayout col = (PortletColumnLayout) cols.get(1);
                             PortletColumnLayout newcol = new PortletColumnLayout();
                             oldcol.setWidth("25%");
                             col.setWidth("50%");
                             newcol.setWidth("25%");
                             row.addPortletComponent(newcol);
                         }
                     }
 
                     if (cols.size() == 3) {
                         if (strategyNum.equals("one")) {
                             // deal with case where column layout needs to be reduced
                             PortletColumnLayout col = (PortletColumnLayout) cols.get(0);
                             PortletColumnLayout newcol = (PortletColumnLayout) cols.get(2);
                             PortletColumnLayout oldcol = (PortletColumnLayout) cols.get(1);
                             col.setWidth("100%");
                             row.removePortletComponent(oldcol);
                             row.removePortletComponent(newcol);
                         }
 
                         if (strategyNum.equals("two")) {
                             PortletColumnLayout col1 = (PortletColumnLayout) cols.get(0);
                             PortletColumnLayout col2 = (PortletColumnLayout) cols.get(1);
                             PortletColumnLayout col3 = (PortletColumnLayout) cols.get(2);
                             col1.setWidth("33%");
                             col2.setWidth("66%");
                             row.removePortletComponent(col3);
 
                         }
                         if (strategyNum.equals("three")) {
                             PortletColumnLayout col1 = (PortletColumnLayout) cols.get(0);
                             PortletColumnLayout col2 = (PortletColumnLayout) cols.get(1);
                             PortletColumnLayout col3 = (PortletColumnLayout) cols.get(2);
                             col1.setWidth("50%");
                             col2.setWidth("50%");
                             row.removePortletComponent(col3);
                         }
                         if (strategyNum.equals("four")) {
                             PortletColumnLayout col1 = (PortletColumnLayout) cols.get(0);
                             PortletColumnLayout col2 = (PortletColumnLayout) cols.get(1);
                             PortletColumnLayout col3 = (PortletColumnLayout) cols.get(2);
                             col1.setWidth("66%");
                             col2.setWidth("33%");
                             row.removePortletComponent(col3);
                         }
 
                         if (strategyNum.equals("five")) {
                             PortletColumnLayout col1 = (PortletColumnLayout) cols.get(0);
                             PortletColumnLayout col2 = (PortletColumnLayout) cols.get(1);
                             PortletColumnLayout col3 = (PortletColumnLayout) cols.get(2);
                             col1.setWidth("33%");
                             col2.setWidth("33%");
                             col3.setWidth("33%");
                         }
                         if (strategyNum.equals("six")) {
                             PortletColumnLayout col1 = (PortletColumnLayout) cols.get(0);
                             PortletColumnLayout col2 = (PortletColumnLayout) cols.get(1);
                             PortletColumnLayout col3 = (PortletColumnLayout) cols.get(2);
                             col1.setWidth("25%");
                             col2.setWidth("50%");
                             col3.setWidth("25%");
                         }
 
                     }
 
 
                 }
             }
             System.err.println("return comp " + comp.getClass().getName());
             return table;
         } else {
 
             System.err.println("creating a new table");
             PortletTableLayout table = new PortletTableLayout();
             PortletRowLayout row = new PortletRowLayout();
 
             if (strategyNum.equals("one")) {
                 PortletColumnLayout col = new PortletColumnLayout();
                 col.setWidth("100%");
                 row.addPortletComponent(col);
             }
 
             if (strategyNum.equals("two")) {
                 PortletColumnLayout col = new PortletColumnLayout();
                 PortletColumnLayout newcol = new PortletColumnLayout();
                 col.setWidth("33%");
                 row.addPortletComponent(col);
                 col.setWidth("66%");
                 row.addPortletComponent(newcol);
             }
 
             if (strategyNum.equals("three")) {
                 PortletColumnLayout col = new PortletColumnLayout();
                 PortletColumnLayout newcol = new PortletColumnLayout();
                 col.setWidth("50%");
                 row.addPortletComponent(col);
                 col.setWidth("50%");
                 row.addPortletComponent(newcol);
             }
 
             if (strategyNum.equals("four")) {
                 PortletColumnLayout col = new PortletColumnLayout();
                 PortletColumnLayout newcol = new PortletColumnLayout();
                 col.setWidth("66%");
                 row.addPortletComponent(col);
                 col.setWidth("33%");
                 row.addPortletComponent(newcol);
             }
 
             if (strategyNum.equals("five")) {
                 PortletColumnLayout col1 = new PortletColumnLayout();
                 PortletColumnLayout col2 = new PortletColumnLayout();
                 PortletColumnLayout col3 = new PortletColumnLayout();
                 col1.setWidth("33%");
                 row.addPortletComponent(col1);
                 col2.setWidth("33%");
                 row.addPortletComponent(col2);
                 col2.setWidth("33%");
                 row.addPortletComponent(col3);
             }
             if (strategyNum.equals("six")) {
                 PortletColumnLayout col1 = new PortletColumnLayout();
                 PortletColumnLayout col2 = new PortletColumnLayout();
                 PortletColumnLayout col3 = new PortletColumnLayout();
                 col1.setWidth("25%");
                 row.addPortletComponent(col1);
                 col2.setWidth("50%");
                 row.addPortletComponent(col2);
                 col2.setWidth("25%");
                 row.addPortletComponent(col3);
             }
             table.addPortletComponent(row);
             System.err.println("return table");
             return table;
 
         }
 
     }
 
 
     private void createColsListBox(FormEvent event, PortletRequest req, PortletComponent comp) {
         // TODO  deal with column layouts
         String colType = "one";
 
         if ((comp != null) && (comp instanceof PortletTableLayout)) {
             PortletTableLayout tableLayout = (PortletTableLayout) comp;
             List rows = tableLayout.getPortletComponents();
             if ((rows != null) && (!rows.isEmpty())) {
                 PortletComponent row = (PortletComponent) rows.get(0);
                 if (row instanceof PortletRowLayout) {
                     PortletRowLayout r = (PortletRowLayout) row;
                     List cols = r.getPortletComponents();
                     if (cols.size() == 2) {
                         PortletColumnLayout col = (PortletColumnLayout) cols.get(0);
                         if (col.getWidth().equals("33%")) {
                             colType = "two";
                         }
                         if (col.getWidth().equals("50%")) {
                             colType = "three";
                         }
                         if (col.getWidth().equals("66%")) {
                             colType = "four";
                         }
                     }
                     if (cols.size() == 3) {
                         PortletColumnLayout col = (PortletColumnLayout) cols.get(0);
                         if (col.getWidth().equals("33%")) {
                             colType = "five";
                         }
                         if (col.getWidth().equals("25%")) {
                             colType = "six";
                         }
                     }
 
                 }
             }
         }
 
         ListBoxBean colsLB = event.getListBoxBean("colsLB");
         colsLB.clear();
         ListBoxItemBean one = new ListBoxItemBean();
         ListBoxItemBean two = new ListBoxItemBean();
         ListBoxItemBean three = new ListBoxItemBean();
         ListBoxItemBean four = new ListBoxItemBean();
         ListBoxItemBean five = new ListBoxItemBean();
         ListBoxItemBean six = new ListBoxItemBean();
         one.setValue(this.getLocalizedText(req, "LAYOUT_ONECOL"));
         one.setName("one");
         if (colType.equals("one")) one.setSelected(true);
         two.setValue(this.getLocalizedText(req, "LAYOUT_TWOCOL1"));
         two.setName("two");
         if (colType.equals("two")) two.setSelected(true);
         three.setValue(this.getLocalizedText(req, "LAYOUT_TWOCOL2"));
 
         three.setName("three");
         if (colType.equals("three")) three.setSelected(true);
         four.setValue(this.getLocalizedText(req, "LAYOUT_TWOCOL3"));
         four.setName("four");
         if (colType.equals("four")) four.setSelected(true);
         five.setValue(this.getLocalizedText(req, "LAYOUT_THREECOL1"));
         five.setName("five");
         if (colType.equals("five")) five.setSelected(true);
         six.setValue(this.getLocalizedText(req, "LAYOUT_THREECOL2"));
         six.setName("six");
         if (colType.equals("six")) six.setSelected(true);
         colsLB.addBean(one);
         colsLB.addBean(two);
         colsLB.addBean(three);
         colsLB.addBean(four);
         colsLB.addBean(five);
         colsLB.addBean(six);
     }
 
 
 }
