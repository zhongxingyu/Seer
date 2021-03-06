 /* 
 @ITMillApache2LicenseForJavaFiles@
  */
 
 package com.itmill.toolkit.terminal.gwt.client;
 
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.Element;
 import com.google.gwt.user.client.Event;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.Window.Location;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.ClickListener;
 import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.HTML;
 import com.google.gwt.user.client.ui.HorizontalPanel;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.Panel;
 import com.google.gwt.user.client.ui.Widget;
 import com.itmill.toolkit.terminal.gwt.client.ui.IToolkitOverlay;
 
 public final class IDebugConsole extends IToolkitOverlay implements Console {
 
     /**
      * Builds number. For example 0-custom_tag in 5.0.0-custom_tag.
      */
     public static final String VERSION;
 
     /* Initialize version numbers from string replaced by build-script. */
     static {
         if ("@VERSION@".equals("@" + "VERSION" + "@")) {
             VERSION = "5.9.9-INTERNAL-NONVERSIONED-DEBUG-BUILD";
         } else {
             VERSION = "@VERSION@";
         }
     }
 
     Element caption = DOM.createDiv();
 
     private final Panel panel;
 
     private Button clear = new Button("Clear console");
     private Button restart = new Button("Restart app");
     private HorizontalPanel actions;
     private boolean collapsed = false;
 
     private boolean resizing;
     private int startX;
     private int startY;
     private int initialW;
     private int initialH;
 
     private boolean moving = false;
 
     private int origTop;
 
     private int origLeft;
 
     private static final String help = "Drag=move, shift-drag=resize, doubleclick=min/max."
             + "Use debug=quiet to log only to browser console.";
 
     public IDebugConsole(ApplicationConnection client,
             ApplicationConfiguration cnf, boolean showWindow) {
         super(false, false);
 
         panel = new FlowPanel();
         if (showWindow) {
             DOM.appendChild(getContainerElement(), caption);
             setWidget(panel);
             caption.setClassName("i-debug-console-caption");
             setStyleName("i-debug-console");
             DOM.setStyleAttribute(getElement(), "zIndex", 20000 + "");
             DOM.setStyleAttribute(getElement(), "overflow", "hidden");
 
             sinkEvents(Event.ONDBLCLICK);
 
             sinkEvents(Event.MOUSEEVENTS);
 
             panel.setStyleName("i-debug-console-content");
 
             caption.setInnerHTML("Debug window");
             caption.setTitle(help);
 
             setWidget(panel);
             show();
             minimize();
 
             actions = new HorizontalPanel();
             actions.add(clear);
             actions.add(restart);
 
             panel.add(actions);
 
             panel.add(new HTML("<i>" + help + "</i>"));
 
             clear.addClickListener(new ClickListener() {
                 public void onClick(Widget sender) {
                     panel.clear();
                     panel.add(actions);
                 }
             });
 
             restart.addClickListener(new ClickListener() {
                 public void onClick(Widget sender) {
 
                     String queryString = Window.Location.getQueryString();
                     if (queryString != null
                             && queryString.contains("restartApplications")) {
                         Window.Location.reload();
                     } else {
                         String url = Location.getHref();
                        if (!url.contains("?")) {
                            url += "?";
                        } else {
                            url += "&";
                         }
                         if (!url.contains("restartApplication")) {
                             url += "restartApplication";
                         }
                         if (!"".equals(Location.getHash())) {
                             String hash = Location.getHash();
                             url = url.replace(hash, "") + hash;
                         }
                         Window.Location.replace(url);
                     }
 
                 }
             });
         }
 
         log("Toolkit application servlet version: " + cnf.getSerletVersion());
         log("Widget set is built on version: " + VERSION);
         log("Application version: " + cnf.getApplicationVersion());
 
         if (!cnf.getSerletVersion().equals(VERSION)) {
             error("Warning: your widget set seems to be built with different "
                     + "version than the one used on server. Unexpected "
                     + "behavior may occur.");
         }
     }
 
     public void onBrowserEvent(Event event) {
         super.onBrowserEvent(event);
         switch (DOM.eventGetType(event)) {
         case Event.ONMOUSEDOWN:
             if (DOM.eventGetShiftKey(event)) {
                 resizing = true;
                 DOM.setCapture(getElement());
                 startX = DOM.eventGetScreenX(event);
                 startY = DOM.eventGetScreenY(event);
                 initialW = IDebugConsole.this.getOffsetWidth();
                 initialH = IDebugConsole.this.getOffsetHeight();
                 DOM.eventCancelBubble(event, true);
                 DOM.eventPreventDefault(event);
             } else if (DOM.eventGetTarget(event) == caption) {
                 moving = true;
                 startX = DOM.eventGetScreenX(event);
                 startY = DOM.eventGetScreenY(event);
                 origTop = getAbsoluteTop();
                 origLeft = getAbsoluteLeft();
                 DOM.eventCancelBubble(event, true);
                 DOM.eventPreventDefault(event);
             }
 
             break;
         case Event.ONMOUSEMOVE:
             if (resizing) {
                 int deltaX = startX - DOM.eventGetScreenX(event);
                 int detalY = startY - DOM.eventGetScreenY(event);
                 int w = initialW - deltaX;
                 if (w < 30) {
                     w = 30;
                 }
                 int h = initialH - detalY;
                 if (h < 40) {
                     h = 40;
                 }
                 IDebugConsole.this.setPixelSize(w, h);
                 DOM.eventCancelBubble(event, true);
                 DOM.eventPreventDefault(event);
             } else if (moving) {
                 int deltaX = startX - DOM.eventGetScreenX(event);
                 int detalY = startY - DOM.eventGetScreenY(event);
                 int left = origLeft - deltaX;
                 if (left < 0) {
                     left = 0;
                 }
                 int top = origTop - detalY;
                 if (top < 0) {
                     top = 0;
                 }
                 IDebugConsole.this.setPopupPosition(left, top);
                 DOM.eventCancelBubble(event, true);
                 DOM.eventPreventDefault(event);
             }
             break;
         case Event.ONLOSECAPTURE:
         case Event.ONMOUSEUP:
             if (resizing) {
                 DOM.releaseCapture(getElement());
                 resizing = false;
             } else if (moving) {
                 DOM.releaseCapture(getElement());
                 moving = false;
             }
             break;
         case Event.ONDBLCLICK:
             if (DOM.eventGetTarget(event) == caption) {
                 if (collapsed) {
                     panel.setVisible(true);
                     setPixelSize(220, 300);
                 } else {
                     panel.setVisible(false);
                     setPixelSize(120, 20);
                 }
                 collapsed = !collapsed;
             }
             break;
         default:
             break;
         }
 
     }
 
     private void minimize() {
         setPixelSize(200, 100);
         setPopupPosition(Window.getClientWidth() - 210, 0);
     }
 
     public void setPixelSize(int width, int height) {
         panel.setHeight((height - 20) + "px");
         panel.setWidth((width - 2) + "px");
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.itmill.toolkit.terminal.gwt.client.Console#log(java.lang.String)
      */
     public void log(String msg) {
         panel.add(new HTML(msg));
         System.out.println(msg);
         consoleLog(msg);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.itmill.toolkit.terminal.gwt.client.Console#error(java.lang.String)
      */
     public void error(String msg) {
         panel.add((new HTML(msg)));
         System.err.println(msg);
         consoleErr(msg);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.itmill.toolkit.terminal.gwt.client.Console#printObject(java.lang.
      * Object)
      */
     public void printObject(Object msg) {
         panel.add((new Label(msg.toString())));
         consoleLog(msg.toString());
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.itmill.toolkit.terminal.gwt.client.Console#dirUIDL(com.itmill.toolkit
      * .terminal.gwt.client.UIDL)
      */
     public void dirUIDL(UIDL u) {
         panel.add(u.print_r());
         consoleLog(u.getChildrenAsXML());
     }
 
     private static native void consoleLog(String msg)
     /*-{
          if($wnd.console && $wnd.console.log) {
              $wnd.console.log(msg);
          }
      }-*/;
 
     private static native void consoleErr(String msg)
     /*-{
          if($wnd.console) {
              if ($wnd.console.error)
                  $wnd.console.error(msg);
              else if ($wnd.console.log)
                  $wnd.console.log(msg);
          }
      }-*/;
 
 }
