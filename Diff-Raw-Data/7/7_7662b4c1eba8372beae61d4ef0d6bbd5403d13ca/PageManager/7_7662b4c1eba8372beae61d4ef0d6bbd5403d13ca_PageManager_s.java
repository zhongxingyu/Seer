 package org.mule.galaxy.web.client;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.mule.galaxy.web.client.ui.panel.ErrorPanel;
 import org.mule.galaxy.web.client.ui.panel.Showable;
 
 import com.extjs.gxt.ui.client.event.ComponentEvent;
 import com.extjs.gxt.ui.client.event.Events;
 import com.extjs.gxt.ui.client.event.SelectionListener;
 import com.extjs.gxt.ui.client.event.TabPanelEvent;
 import com.extjs.gxt.ui.client.widget.ReloadableTabPanel;
 import com.extjs.gxt.ui.client.widget.TabItem;
 import com.extjs.gxt.ui.client.widget.TabPanel;
 import com.extjs.gxt.ui.client.widget.layout.FlowLayout;
 import com.google.gwt.event.logical.shared.ValueChangeEvent;
 import com.google.gwt.event.logical.shared.ValueChangeHandler;
 import com.google.gwt.user.client.History;
 import com.google.gwt.user.client.ui.Widget;
 
 public class PageManager implements ValueChangeHandler<String>{
 
     public static final String WILDCARD = "*";
     private static final String DEFAULT_PAGE = "browse";
     private PageInfo curInfo;
     private String currentToken;
     private final TabPanel tabPanel;
     private boolean suppressTabHistory;
     private Map<String, PageInfo> history = new HashMap<String, PageInfo>();
     private List<String> tabNames = new ArrayList<String>();
 
     public PageManager() {
         super();
         tabPanel = new ReloadableTabPanel() {
             @Override
             protected void onLastSelectedItemClick(TabItem item, ComponentEvent ce) {
                PageManager.this.onHistoryChanged(getToken(getCurrentPage()));
             }
         };
         tabPanel.setBorderStyle(false);
         tabPanel.setAutoHeight(true);
         tabPanel.setAutoWidth(true);
     }
     
     public void initialize() {
         History.addValueChangeHandler(this);
         
         tabPanel.addListener(Events.Select, new SelectionListener<TabPanelEvent>() {
 
             @Override
             public void componentSelected(TabPanelEvent ce) {
                 if (!suppressTabHistory) {
                     TabItem item = ce.getItem();
                     int newTab = tabPanel.getItems().indexOf(item);
                     History.newItem(tabNames.get(newTab));
                 }
             }
 
         });
     }
 
    private String getToken(PageInfo page) {
        return tabNames.get(page.getTabIndex());
    }

     public PageInfo getCurrentPage() {
         return curInfo;
     }
 
     public TabPanel getTabPanel() {
         return tabPanel;
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
 
     public PageInfo createPageInfo(String token,
                                    final Widget composite,
                                    int tab) {
         PageInfo page = new PageInfo(token, tab) {
             public Widget createInstance() {
                 return composite;
             }
         };
         addPage(page);
         return page;
     }
 
     public PageInfo getPageInfo(String token) {
         PageInfo page = history.get(token);
 
         if (page == null) {
 
             // hack to match "foo/*" style tokens
             int slashIdx = token.indexOf("/");
             if (slashIdx == -1) {
                 slashIdx = token.length();
             }
             
             if (slashIdx != -1) {
                 page = history.get(token.substring(0, slashIdx) + "/" + WILDCARD);
             }
 
             if (page == null) {
                 page = history.get(token.substring(0, slashIdx));
             }
         }
 
         if (page == null) {
             throw new IllegalStateException("Could not find page: " + token);
         }
         
         return page;
     }
 
     public void setMessageAndGoto(String token, String message) {
         PageInfo pi = getPageInfo(token);
 
         ErrorPanel ep = (ErrorPanel) pi.getInstance();
 
         History.newItem(token);
 
         ep.setMessage(message);
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
 
         if (tabPanel.getSelectedItem() == null || !tabPanel.getSelectedItem().equals(p)) {
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
         curInfo = page;
     }
 
     public int createTab(String name, String token, String toolTip) {
         int index = tabPanel.getItemCount();
         createTab(index, name, token, toolTip);
         return index;
     }
 
     public void createTab(int index, String name, String token, String toolTip) {
         tabPanel.insert(createEmptyTab(name, toolTip), index);
         tabNames.add(index, token);
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
 
 }
