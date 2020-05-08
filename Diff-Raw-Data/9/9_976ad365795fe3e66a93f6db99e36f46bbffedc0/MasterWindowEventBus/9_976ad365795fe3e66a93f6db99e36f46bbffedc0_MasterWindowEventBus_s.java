 package de.atns.common.gwt.client.window;
 
 import com.google.gwt.core.client.JavaScriptObject;
 import com.google.gwt.event.logical.shared.CloseEvent;
 import com.google.gwt.event.logical.shared.CloseHandler;
 import com.google.gwt.json.client.JSONArray;
 import com.google.gwt.user.client.Window;
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 import com.google.web.bindery.event.shared.Event;
 import com.google.web.bindery.event.shared.EventBus;
 import com.google.web.bindery.event.shared.HandlerRegistration;
 import com.google.web.bindery.event.shared.SimpleEventBus;
 
 import java.util.HashMap;
 
 import static java.lang.System.currentTimeMillis;
 
 /**
  * @author tbaum
  * @since 03.02.11
  */
 
 @Singleton public class MasterWindowEventBus extends EventBus implements WindowEventBus {
 
     private final EventBus simpleEventBus = new SimpleEventBus();
     private final EventSerializer serializer;
     private HashMap<String, JavaScriptObject> windows = new HashMap<String, JavaScriptObject>();
 
     public static String currentLocation() {
         String location = location();
         int i = location.indexOf("#");
         if (i > -1) {
             location = location.substring(0, i);
         }
         return location;
     }
 
     private static native String location() /*-{
         return $wnd.location.href
     }-*/;
 
     @Inject public MasterWindowEventBus(EventSerializer eventSerializer) {
         this.serializer = eventSerializer;
         _registerEventBus(this);
 
         Window.addCloseHandler(new CloseHandler<Window>() {
             @Override public void onClose(CloseEvent<Window> windowCloseEvent) {
                 for (JavaScriptObject window : windows.values()) {
                     closeWindow(window);
                 }
             }
         });
     }
 
     private native void _registerEventBus(MasterWindowEventBus windowEventbus) /*-{
         $wnd._event_from_popup = function (source, event) {
             windowEventbus.@de.atns.common.gwt.client.window.MasterWindowEventBus::fire(Lcom/google/gwt/core/client/JavaScriptObject;Lcom/google/gwt/core/client/JavaScriptObject;)(source,
                     event);
         };
         $wnd._popup_closed = function (source) {
             windowEventbus.@de.atns.common.gwt.client.window.MasterWindowEventBus::removeWindow(Ljava/lang/String;)(source);
         };
         $wnd._open_popup = function (url, title, para) {
             windowEventbus.@de.atns.common.gwt.client.window.MasterWindowEventBus::openWindow(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(url,
                     title, para);
         };
     }-*/;
 
     private native void closeWindow(JavaScriptObject window) /*-{
         window.close();
     }-*/;
 
     @Override public void openWindow(String url, String name, String args) {
         final String id = String.valueOf(currentTimeMillis());
        JavaScriptObject wnd = open(this, currentLocation() + url, (name + "_" + id).replaceAll(":", "_"), id, args);
         windows.put(id, wnd);
     }
 
     @Override public <H> HandlerRegistration addHandler(Event.Type<H> type, H handler) {
         // TODO unregister
         return simpleEventBus.addHandler(type, handler);
     }
 
     @Override public <H> HandlerRegistration addHandlerToSource(Event.Type<H> type, Object source, H handler) {
         return simpleEventBus.addHandlerToSource(type, source, handler);
     }
 
     @SuppressWarnings({"UnusedDeclaration"})
     private void fire(JavaScriptObject src, JavaScriptObject o) {
         final TransportAware transportAware = serializer.deserialize(new JSONArray(o));
         simpleEventBus.fireEvent((Event<?>) transportAware);
 
         for (JavaScriptObject window : windows.values()) {
             _event_fireEvent(src, window, o);
         }
     }
 
     private native void _event_fireEvent(JavaScriptObject source, JavaScriptObject popupWindow,
                                          JavaScriptObject event) /*-{
         if (source != popupWindow) {
             popupWindow._event_from_master(event);
         }
     }-*/;
 
     @Override public void fireEvent(Event event) {
         simpleEventBus.fireEvent(event);
 
         if (event instanceof TransportAware) {
             TransportAware transportAware = (TransportAware) event;
             for (JavaScriptObject window : windows.values()) {
                 _event_fireEvent(JavaScriptObject.createObject(), window, serializer.serialize(transportAware));
             }
         }
     }
 
     @Override public void fireEventFromSource(Event event, Object source) {
         simpleEventBus.fireEventFromSource(event, source);
         //TODO?
         //        for (JavaScriptObject window : windows) {
         //            _event_fireEventFromSource(window, event, source);
         //        }
     }
 
    private native JavaScriptObject open(MasterWindowEventBus eventbus, String url, String name, String myid,
                                         String args) /*-{
        var newWindow = $wnd.open(url, name, args)
         newWindow._myid = myid;
 
         try {
 //            netscape.security.PrivilegeManager.enablePrivilege('UniversalBrowserWrite');
             newWindow.menubar.visible = false;
             newWindow.toolbar.visible = false;
             newWindow.locationbar.visible = false;
             newWindow.statusbar.visible = false;
             newWindow.linkbar.visible = false;
         } catch (ignored) {
             if ($wnd.Console && $wnd.Console.log) $wnd.Console.log(ignored);
         }
         return newWindow;
     }-*/;
 
     private void removeWindow(String wnd) {
         windows.remove(wnd);
     }
 }
