 /*
  * $Id$
  * --------------------------------------------------------------------------------------
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 package org.mule.galaxy.web.client;
 
 import static org.mule.galaxy.web.client.WidgetHelper.newSpacerPipe;
 import com.extjs.gxt.ui.client.GXT;
 import com.extjs.gxt.ui.client.Style.LayoutRegion;
 import com.extjs.gxt.ui.client.Style.Scroll;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.event.TabPanelEvent;
 import com.extjs.gxt.ui.client.util.Margins;
 import com.extjs.gxt.ui.client.widget.ContentPanel;
 import com.extjs.gxt.ui.client.widget.TabItem;
 import com.extjs.gxt.ui.client.widget.TabPanel;
 import com.extjs.gxt.ui.client.widget.Viewport;
 import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
 import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
 import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.rpc.ServiceDefTarget;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.RootPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.mule.galaxy.web.client.admin.AdministrationPanel;
 import org.mule.galaxy.web.client.item.RepositoryMenuPanel;
 import org.mule.galaxy.web.client.property.PropertyInterfaceManager;
 import org.mule.galaxy.web.client.registry.SearchPanel;
 import org.mule.galaxy.web.client.ui.BaseConstants;
 import org.mule.galaxy.web.client.ui.BaseMessages;
 import org.mule.galaxy.web.client.util.ExternalHyperlink;
 import org.mule.galaxy.web.client.util.InlineFlowPanel;
 import org.mule.galaxy.web.rpc.AbstractCallback;
 import org.mule.galaxy.web.rpc.AdminService;
 import org.mule.galaxy.web.rpc.AdminServiceAsync;
 import org.mule.galaxy.web.rpc.HeartbeatService;
 import org.mule.galaxy.web.rpc.HeartbeatServiceAsync;
 import org.mule.galaxy.web.rpc.RegistryService;
 import org.mule.galaxy.web.rpc.RegistryServiceAsync;
 import org.mule.galaxy.web.rpc.SecurityService;
 import org.mule.galaxy.web.rpc.SecurityServiceAsync;
 import org.mule.galaxy.web.rpc.WExtensionInfo;
 import org.mule.galaxy.web.rpc.WUser;
 
 
 /**
  * Entry point classes define <code>onModuleLoad()</code>.
  */
 public class Galaxy implements EntryPoint, ValueChangeHandler<String> {
 
     public static final String WILDCARD = "*";
     private static final String DEFAULT_PAGE = "browse";
     private RegistryServiceAsync registryService;
     private SecurityServiceAsync securityService;
     private HeartbeatServiceAsync heartbeatService;
     private AdminServiceAsync adminService;
     private InlineFlowPanel rightHeaderPanel;
     private PageInfo curInfo;
     private Map<String, PageInfo> history = new HashMap<String, PageInfo>();
     protected TabPanel tabPanel;
     protected WUser user;
     protected int oldTab;
     private boolean suppressTabHistory;
     private Map<String, AbstractShowable> historyListeners = new HashMap<String, AbstractShowable>();
     protected int adminTabIndex;
     protected Viewport base;
     protected PropertyInterfaceManager propertyInterfaceManager = new PropertyInterfaceManager();
     protected List extensions;
     private String currentToken;
     protected Label product;
     protected InlineFlowPanel footerPanel;
 
     protected List<String> tabNames = new ArrayList<String>();
     protected int repositoryTabIndex;
     private BaseConstants baseConstants;
     private BaseMessages baseMessages;
     private RepositoryMenuPanel repositoryPanel;
 
     /**
      * This is the entry point method.
      */
     public void onModuleLoad() {
 
 
         //GXT.setDefaultTheme(Theme.GRAY, true);
         GXT.BLANK_IMAGE_URL = "extjsresources/images/default/s.gif";
         final String LOGO = "images/galaxy_logo_main_trans.gif";
 
         // prefetch the image, so that e.g. SessionKilled dialog can be properly displayed for the first time
         // when the server is already down and cannot serve it.
         Image.prefetch("images/lightbox.png");
 
         History.addValueChangeHandler(this);
 
         this.registryService = (RegistryServiceAsync) GWT.create(RegistryService.class);
 
         ServiceDefTarget target = (ServiceDefTarget) registryService;
         String baseUrl = GWT.getModuleBaseURL();
         target.setServiceEntryPoint(baseUrl + "../handler/registry.rpc");
 
         this.securityService = (SecurityServiceAsync) GWT.create(SecurityService.class);
 
         target = (ServiceDefTarget) securityService;
         target.setServiceEntryPoint(baseUrl + "../handler/securityService.rpc");
 
         this.heartbeatService = (HeartbeatServiceAsync) GWT.create(HeartbeatService.class);
         target = (ServiceDefTarget) heartbeatService;
         target.setServiceEntryPoint(baseUrl + "../handler/heartbeat.rpc");
 
         this.adminService = (AdminServiceAsync) GWT.create(AdminService.class);
         target = (ServiceDefTarget) adminService;
         target.setServiceEntryPoint(baseUrl + "../handler/admin.rpc");
 
         this.baseConstants = (BaseConstants) GWT.create(BaseConstants.class);
         this.baseMessages = (BaseMessages) GWT.create(BaseMessages.class);
 
 
         base = new Viewport();
         base.setLayout(new BorderLayout());
 
         createHeader(new Image(LOGO));
 
         tabPanel = new TabPanel();
         tabPanel.setAutoHeight(true);
 
         tabNames.add("browse");
         tabNames.add("search");
 
         tabPanel.addListener(Events.Select, new SelectionListener<TabPanelEvent>() {
 
             @Override
             public void componentSelected(TabPanelEvent ce) {
                 TabItem item = ce.getItem();
                 int newTab = tabPanel.getItems().indexOf(item);
                 if (!suppressTabHistory) {
                     History.newItem(tabNames.get(newTab));
                 }
                 oldTab = newTab;
             }
 
         });
 
         createBody();
 
         // prefetch extensions
         registryService.getExtensions(new AbstractCallback(repositoryPanel) {
             @SuppressWarnings("unchecked")
             public void onSuccess(Object o) {
                 extensions = (List) o;
                 Collections.sort(extensions);
             }
         });
 
         final Galaxy galaxy = this;
         registryService.getUserInfo(new AbstractCallback(repositoryPanel) {
             public void onSuccess(Object o) {
                 user = (WUser) o;
 
                 // always the left most item
                 rightHeaderPanel.insert(new Label("Welcome, " + user.getName()), 0);
 
                 suppressTabHistory = true;
                 loadTabs(galaxy);
                 suppressTabHistory = false;
                 showFirstPage();
             }
         });
 
         createFooter();
 
         RootPanel.get().add(base);
         base.layout(true);
 
         new HeartbeatTimer(Galaxy.this);
     }
 
 
     private void createFooter() {
         ContentPanel southPanel = new ContentPanel();
         southPanel.setBorders(false);
         southPanel.setHeaderVisible(false);
 
         BorderLayoutData data = new BorderLayoutData(LayoutRegion.SOUTH, 33);
         data.setMargins(new Margins());
 
         footerPanel = new InlineFlowPanel();
         footerPanel.setStyleName("footer");
 
         prependFooterConent();
 
         southPanel.add(footerPanel);
         base.add(southPanel, data);
     }
 
 
     /**
      * adds to the left of the  copyright info
      */
     protected void prependFooterConent() {
         product = new Label("About " + getProductName());
         product.setStyleName("footer-link");
         product.addClickHandler(new ClickHandler() {
             public void onClick(ClickEvent arg0) {
                 new AboutPanel();
             }
         });
         footerPanel.add(product);
         footerPanel.add(WidgetHelper.newSpacerPipe());
 
         Label copyright = new Label(getFooterText());
         footerPanel.add(copyright);
     }
 
 
     protected void createHeader(Image logo) {
         ContentPanel northPanel = new ContentPanel();
         northPanel.setBorders(false);
         northPanel.setHeaderVisible(false);
 
         BorderLayoutData data = new BorderLayoutData(LayoutRegion.NORTH, 33);
         data.setMargins(new Margins());
         data.setSize(50);
 
         rightHeaderPanel = new InlineFlowPanel();
         rightHeaderPanel.setStyleName("header-right");
         rightHeaderPanel.add(createHeaderOptions());
 
         // custom logo
         FlowPanel header = new FlowPanel();
         header.setStyleName("header");
         header.add(rightHeaderPanel);
         logo.setTitle("Home");
         logo.addStyleName("gwt-Hyperlink");
         logo.addClickHandler(new ClickHandler() {
             public void onClick(ClickEvent clickEvent) {
                 History.newItem("browse");
             }
         });
         header.add(logo);
 
         northPanel.add(header);
         base.add(northPanel, data);
     }
 
     protected InlineFlowPanel createHeaderOptions() {
 
         InlineFlowPanel options = new InlineFlowPanel();
         options.setStyleName("header-right-options");
 
         ExternalHyperlink logout = new ExternalHyperlink("Log Out", GWT.getHostPageBaseURL() + "j_logout");
         options.add(newSpacerPipe());
         options.add(logout);
 
         return options;
     }
 
     protected void createBody() {
         ContentPanel centerPanel = new ContentPanel();
         centerPanel.setBorders(false);
         centerPanel.setHeaderVisible(false);
         centerPanel.setScrollMode(Scroll.NONE);
         centerPanel.setLayout(new FlowLayout());
         centerPanel.add(tabPanel);
 
         BorderLayoutData data = new BorderLayoutData(LayoutRegion.CENTER);
         data.setMargins(new Margins());
 
         base.add(centerPanel, data);
     }
 
     protected void createRepositoryPanels() {
         repositoryPanel = new RepositoryMenuPanel(this);
     }
 
     public int getRepositoryTab() {
         return repositoryTabIndex;
     }
 
     protected String getFooterText() {
         return "Copyright 2009 MuleSource, Inc. All rights reserved";
     }
 
     protected String getProductName() {
         return "Mule Galaxy";
     }
 
     public PageInfo createPageInfo(String token,
                                    final AbstractShowable composite,
                                    int tab) {
         PageInfo page = new PageInfo(token, tab) {
             public AbstractShowable createInstance() {
                 return composite;
             }
         };
         addPage(page);
         return page;
     }
 
     protected void loadTabs(final Galaxy galaxy) {
         tabPanel.add(createEmptyTab("Registry"));
         createRepositoryPanels();
 
         int searchIdx = tabPanel.getItemCount();
         createPageInfo("search", new SearchPanel(this), searchIdx);
         tabPanel.add(createEmptyTab("Search"));
 
         if (showAdminTab(user)) {
             adminTabIndex = tabPanel.getItemCount();
             tabNames.add(adminTabIndex, "admin");
             createPageInfo("admin", createAdministrationPanel(), adminTabIndex);
             tabPanel.add(createEmptyTab("Administration"));
         }
     }
 
     protected TabItem createEmptyTab(String name, String toolTip) {
         TabItem tab = new TabItem();
         TabItem.HeaderItem header = tab.getHeader();
         header.setText(name);
 
         if (toolTip != null) {
             header.setToolTip(toolTip);
         }
         tab.setLayout(new FlowLayout());
         return tab;
     }
 
     protected TabItem createEmptyTab(String name) {
         return createEmptyTab(name, null);
     }
 
     protected AdministrationPanel createAdministrationPanel() {
         return new AdministrationPanel(this);
     }
 
     protected boolean showAdminTab(WUser user) {
         for (Iterator<String> itr = user.getPermissions().iterator(); itr.hasNext();) {
             String s = itr.next();
 
             if (s.startsWith("MANAGE_") || "EXECUTE_ADMIN_SCRIPTS".equals(s)) {
                 return true;
             }
         }
         return false;
     }
 
     protected void showFirstPage() {
         // Show the initial screen.
         String initToken = History.getToken();
         if (initToken.length() > 0) {
             onHistoryChanged(initToken);
         } else {
             show("browse");
         }
     }
 
     public void addPage(PageInfo info) {
         history.put(info.getName(), info);
     }
 
     /**
      * Shows a page, but does not trigger a history event.
      *
      * @param token
      */
     public void show(String token) {
         show(getPageInfo(token), getParams(token));
     }
 
     protected void show(PageInfo page, List<String> params) {
         suppressTabHistory = true;
         TabItem p = (TabItem) tabPanel.getWidget(page.getTabIndex());
 
         if (!tabPanel.getSelectedItem().equals(p)) {
             tabPanel.setSelection(p);
         }
 
         p.removeAll();
         p.layout();
         
         Widget instance = page.getInstance();
         p.add(instance);
         p.layout();
 
         if (instance instanceof Showable) {
             ((Showable) instance).showPage(params);
         }
        suppressTabHistory = false;
     }
 
     public void onValueChange(ValueChangeEvent<String> event) {
         onHistoryChanged(event.getValue());
     }
 
     public void onHistoryChanged(String token) {
         currentToken = token;
         if ("".equals(token)) {
             token = DEFAULT_PAGE;
         }
 
         if ("nohistory".equals(token) && curInfo != null) {
             suppressTabHistory = false;
             return;
         }
 
         PageInfo page = getPageInfo(token);
         List<String> params = getParams(token);
 
         // hide the previous page
         if (curInfo != null) {
             Widget instance = curInfo.getInstance();
             if (instance instanceof Showable) {
                 ((Showable) instance).hidePage();
             }
         }
 
         if (page == null) {
             // went to a page which isn't in our history anymore. go to the first page
             if (curInfo == null) {
                 onHistoryChanged(DEFAULT_PAGE);
             }
         } else {
             curInfo = page;
 
             show(page, params);
         }
     }
 
 
     private List<String> getParams(String token) {
         List<String> params = new ArrayList<String>();
         String[] split = token.split("/");
 
         if (split.length > 1) {
             for (int i = 1; i < split.length; i++) {
                 params.add(split[i]);
             }
         }
         return params;
     }
 
     public String getCurrentToken() {
         return currentToken;
     }
 
     public PageInfo getPageInfo(String token) {
         PageInfo page = history.get(token);
 
         if (page == null) {
 
             // hack to match "foo/*" style tokens
             int slashIdx = token.indexOf("/");
             if (slashIdx != -1) {
                 page = history.get(token.substring(0, slashIdx) + "/" + WILDCARD);
             }
 
             if (page == null) {
                 page = history.get(token.substring(0, slashIdx));
             }
         }
 
         return page;
     }
 
     public void setMessageAndGoto(String token, String message) {
         PageInfo pi = getPageInfo(token);
 
         ErrorPanel ep = (ErrorPanel) pi.getInstance();
 
         History.newItem(token);
 
         ep.setMessage(message);
     }
 
     public PropertyInterfaceManager getPropertyInterfaceManager() {
         return propertyInterfaceManager;
     }
 
     public List getExtensions() {
         return extensions;
     }
 
     public RegistryServiceAsync getRegistryService() {
         return registryService;
     }
 
     public SecurityServiceAsync getSecurityService() {
         return securityService;
     }
 
     public HeartbeatServiceAsync getHeartbeatService() {
         return this.heartbeatService;
     }
 
     public AdminServiceAsync getAdminService() {
         return adminService;
     }
 
     public TabPanel getTabPanel() {
         return tabPanel;
     }
 
     public BaseConstants getBaseConstants() {
         return baseConstants;
     }
 
     public BaseMessages getBaseMessages() {
         return baseMessages;
     }
 
     public boolean hasPermission(String perm) {
         for (Iterator<String> itr = user.getPermissions().iterator(); itr.hasNext();) {
             String s = itr.next();
 
             if (s.startsWith(perm)) return true;
         }
         return false;
     }
 
     public int getAdminTab() {
         return adminTabIndex;
     }
 
     public void addHistoryListener(String token, AbstractShowable composite) {
         historyListeners.put(token, composite);
     }
 
     public WExtensionInfo getExtension(String id) {
         for (Iterator itr = extensions.iterator(); itr.hasNext();) {
             WExtensionInfo ei = (WExtensionInfo) itr.next();
 
             if (id.equals(ei.getId())) {
                 return ei;
             }
         }
         return null;
     }
 
 }
