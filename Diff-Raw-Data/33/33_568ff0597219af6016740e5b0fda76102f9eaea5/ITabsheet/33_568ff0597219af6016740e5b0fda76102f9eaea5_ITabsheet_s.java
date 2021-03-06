 package com.itmill.toolkit.terminal.gwt.client.ui;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.DeferredCommand;
 import com.google.gwt.user.client.Element;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.SourcesTabEvents;
 import com.google.gwt.user.client.ui.TabBar;
 import com.google.gwt.user.client.ui.TabListener;
 import com.google.gwt.user.client.ui.Widget;
 import com.itmill.toolkit.terminal.gwt.client.ApplicationConnection;
 import com.itmill.toolkit.terminal.gwt.client.ContainerResizedListener;
 import com.itmill.toolkit.terminal.gwt.client.Paintable;
 import com.itmill.toolkit.terminal.gwt.client.UIDL;
 import com.itmill.toolkit.terminal.gwt.client.Util;
 
 public class ITabsheet extends FlowPanel implements Paintable,
         ContainerResizedListener {
 
     public static final String CLASSNAME = "i-tabsheet";
 
     String id;
     ApplicationConnection client;
 
     private ArrayList tabKeys = new ArrayList();
     private ArrayList captions = new ArrayList();
     int activeTabIndex = 0;
     private final TabBar tb;
     private final ITabsheetPanel tp;
     private final Element contentNode, deco;
     private boolean disabled;
 
     private final TabListener tl = new TabListener() {
 
         public void onTabSelected(SourcesTabEvents sender, final int tabIndex) {
             if (ITabsheet.this.client != null
                     && ITabsheet.this.activeTabIndex != tabIndex) {
                 addStyleDependentName("loading");
                 // run updating variables in deferred command to bypass some FF
                 // optimization issues
                 DeferredCommand.addCommand(new Command() {
                     public void execute() {
                         ITabsheet.this.client.updateVariable(ITabsheet.this.id,
                                 "selected", ""
                                         + ITabsheet.this.tabKeys.get(tabIndex),
                                 true);
                     }
                 });
             }
         }
 
         public boolean onBeforeTabSelected(SourcesTabEvents sender, int tabIndex) {
             if (disabled) {
                 return false;
             }
             return true;
         }
 
     };
 
     private String height;
 
     public ITabsheet() {
         setStyleName(CLASSNAME);
 
         tb = new TabBar();
         tp = new ITabsheetPanel();
         contentNode = DOM.createDiv();
         deco = DOM.createDiv();
 
         addStyleDependentName("loading"); // Indicate initial progress
         tb.setStyleName(CLASSNAME + "-tabs");
         DOM
                 .setElementProperty(contentNode, "className", CLASSNAME
                         + "-content");
         DOM.setElementProperty(deco, "className", CLASSNAME + "-deco");
 
         add(tb);
         DOM.appendChild(getElement(), contentNode);
         insert(tp, contentNode, 0, true);
         DOM.appendChild(getElement(), this.deco);
 
         this.tb.addTabListener(this.tl);
 
         clearTabs();
 
         // TODO Use for Safari only. Fix annoying 1px first cell in TabBar.
         DOM.setStyleAttribute(DOM.getFirstChild(DOM.getFirstChild(DOM
                 .getFirstChild(tb.getElement()))), "display", "none");
     }
 
     public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
         this.client = client;
         this.id = uidl.getId();
 
         if (client.updateComponent(this, uidl, false)) {
             return;
         }
 
         disabled = uidl.hasAttribute("disabled");
 
         // Add proper stylenames for all elements
         if (uidl.hasAttribute("style")) {
             String[] styles = uidl.getStringAttribute("style").split(" ");
             String contentBaseClass = "CLASSNAME" + "-content";
             String contentClass = contentBaseClass;
             String decoBaseClass = CLASSNAME + "-deco";
             String decoClass = decoBaseClass;
             for (int i = 0; i < styles.length; i++) {
                 tb.addStyleDependentName(styles[i]);
                 contentClass += " " + contentBaseClass + "-" + styles[i];
                 decoClass += " " + decoBaseClass + "-" + styles[i];
             }
             DOM.setElementProperty(contentNode, "className", contentClass);
             DOM.setElementProperty(deco, "className", decoClass);
         } else {
             tb.setStyleName(CLASSNAME + "-tabs");
             DOM.setElementProperty(contentNode, "className", CLASSNAME
                     + "-content");
             DOM.setElementProperty(deco, "className", CLASSNAME + "-deco");
         }
 
         // Adjust width and height
         if (uidl.hasAttribute("height")) {
             setHeight(uidl.getStringAttribute("height"));
         } else {
             setHeight("");
         }
         if (uidl.hasAttribute("width")) {
             setWidth(uidl.getStringAttribute("width"));
         } else {
             setWidth("");
         }
 
         // Render content
         UIDL tabs = uidl.getChildUIDL(0);
         boolean keepCurrentTabs = tabKeys.size() == tabs.getNumberOfChildren();
         for (int i = 0; keepCurrentTabs && i < tabKeys.size(); i++) {
             keepCurrentTabs = tabKeys.get(i).equals(
                     tabs.getChildUIDL(i).getStringAttribute("key"))
                     && captions.get(i).equals(
                             tabs.getChildUIDL(i).getStringAttribute("caption"));
         }
         if (keepCurrentTabs) {
             int index = 0;
             for (Iterator it = tabs.getChildIterator(); it.hasNext();) {
                 UIDL tab = (UIDL) it.next();
                 if (tab.getBooleanAttribute("selected")) {
                     activeTabIndex = index;
                     renderContent(tab.getChildUIDL(0));
                 }
                 index++;
             }
         } else {
             tabKeys.clear();
             captions.clear();
             clearTabs();
 
             int index = 0;
             for (Iterator it = tabs.getChildIterator(); it.hasNext();) {
                 UIDL tab = (UIDL) it.next();
                 String key = tab.getStringAttribute("key");
                 String caption = tab.getStringAttribute("caption");
                 if (caption == null) {
                     caption = "&nbsp;";
                 }
 
                 captions.add(caption);
                 tabKeys.add(key);
 
                 // Add new tab (additional SPAN-element for loading indication)
                 tb.insertTab("<span>" + caption + "</span>", true, tb
                         .getTabCount());
 
                 // Add placeholder content
                 tp.add(new ILabel(""));
 
                 if (tab.getBooleanAttribute("selected")) {
                     activeTabIndex = index;
                     renderContent(tab.getChildUIDL(0));
                 }
                 index++;
             }
         }
 
         // Open selected tab, if there's something to show
         if (tabKeys.size() > 0)
             tb.selectTab(activeTabIndex);
 
     }
 
     private void renderContent(final UIDL contentUIDL) {
         DeferredCommand.addCommand(new Command() {
             public void execute() {
                 Widget content = ITabsheet.this.client.getWidget(contentUIDL);
                 ITabsheet.this.tp.remove(ITabsheet.this.activeTabIndex);
                 ITabsheet.this.tp
                         .insert(content, ITabsheet.this.activeTabIndex);
                 ITabsheet.this.tp.showWidget(ITabsheet.this.activeTabIndex);
                 ((Paintable) content).updateFromUIDL(contentUIDL,
                         ITabsheet.this.client);
                 ITabsheet.this.removeStyleDependentName("loading");
                 ITabsheet.this.iLayout();
             }
         });
 
     }
 
     private void clearTabs() {
         int i = tb.getTabCount();
         while (i > 0) {
             tb.removeTab(--i);
         }
         tp.clear();
 
         // Get rid of unnecessary 100% cell heights in TabBar (really ugly hack)
         Element tr = DOM.getChild(DOM.getChild(tb.getElement(), 0), 0);
         Element rest = DOM.getChild(
                 DOM.getChild(tr, DOM.getChildCount(tr) - 1), 0);
         DOM.removeElementAttribute(rest, "style");
     }
 
     public void setHeight(String height) {
         this.height = height;
         iLayout();
     }
 
     public void setWidth(String width) {
         if ("100%".equals(width)) {
             // Allow browser to calculate width
             super.setWidth("");
         } else {
             super.setWidth(width);
         }
     }
 
     public void iLayout() {
         if (height != null && height != "") {
             // Take content out of flow for a while
             String originalPositioning = DOM.getStyleAttribute(tp.getElement(),
                     "position");
             DOM.setStyleAttribute(tp.getElement(), "position", "absolute");
             DOM.setStyleAttribute(contentNode, "overflow", "hidden");
 
             // Calculate target height
            int targetHeight = 0;
            /*
             * if (height.indexOf("%") > 0) { // Percentage heights are handled
             * separately int parentHeight = DOM.getElementPropertyInt(DOM
             * .getParent(getElement()), "offsetHeight"); targetHeight =
             * parentHeight Integer.parseInt(height.substring(0, height.length() -
             * 1)) / 100; } else {
             */
             super.setHeight(height);
            targetHeight = getOffsetHeight();
            // }
 
             // Calculate used height
             super.setHeight("");
            int usedHeight = getOffsetHeight();
 
             // Calculate content area height (don't allow negative values)
             int h = targetHeight - usedHeight;
             if (h < 0) {
                 h = 0;
             }
 
             // Set proper values for content element
             tp.setHeight(h + "px");
             DOM.setStyleAttribute(tp.getElement(), "position",
                     originalPositioning);
             DOM.setStyleAttribute(contentNode, "overflow", "auto");
         } else {
             tp.setHeight("");
         }
         Util.runDescendentsLayout(this);
     }
 }
