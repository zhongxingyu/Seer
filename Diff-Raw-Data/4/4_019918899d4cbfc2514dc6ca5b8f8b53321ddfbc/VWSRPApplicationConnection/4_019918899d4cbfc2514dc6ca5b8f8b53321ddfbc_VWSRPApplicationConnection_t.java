 package com.vaadin.addon.wsrp.gwt.client;
 
 import com.google.gwt.http.client.RequestBuilder;
 import com.google.gwt.http.client.RequestCallback;
 import com.google.gwt.http.client.RequestException;
 import com.google.gwt.http.client.URL;
 import com.vaadin.terminal.gwt.client.ApplicationConfiguration;
 import com.vaadin.terminal.gwt.client.ApplicationConnection;
 import com.vaadin.terminal.gwt.client.WidgetSet;
 
 public class VWSRPApplicationConnection extends ApplicationConnection {
 
     public static final String PARAMETER_PAYLOAD = "pl";
     private boolean WSRPMode = false;
 
     public VWSRPApplicationConnection() {
     }
 
     @Override
     public void init(WidgetSet widgetSet, ApplicationConfiguration cnf) {
         WSRPMode = getWSRPModeFromDOM(cnf.getRootPanelId());
         super.init(widgetSet, cnf);
     }
 
     private native boolean getWSRPModeFromDOM(String id)
     /*-{
     if($wnd.vaadin.vaadinConfigurations && $wnd.vaadin.vaadinConfigurations[id]) {
         var jsobj = $wnd.vaadin.vaadinConfigurations[id];
        if('WSRPMode' in jsobj && typeof(jsobj.WSRPMode) == "boolean") {
            return jsobj.WSRPMode;
         }
     }
     
     return false;
     }-*/;
 
     @Override
     protected void doUidlRequest(String uri, String payload, boolean synchronous) {
         if (synchronous) {
             // TODO, Handle this manually
             super.doUidlRequest(uri, payload, synchronous);
         } else {
             super.doUidlRequest(uri, payload, synchronous);
         }
     }
 
     /*
      * Overridden to change content type and encode data as a form post.
      * 
      * (non-Javadoc)
      * 
      * @see
      * com.vaadin.terminal.gwt.client.ApplicationConnection#doAsyncUIDLRequest
      * (java.lang.String, java.lang.String,
      * com.google.gwt.http.client.RequestCallback)
      */
     @Override
     protected void doAsyncUIDLRequest(String uri, String payload,
             RequestCallback requestCallback) throws RequestException {
         if (!isWSRPMode()) {
             // Use normal communication mechanism for non-WSRP portlets
             super.doAsyncUIDLRequest(uri, payload, requestCallback);
             return;
         }
 
         // Double encode content to work correctly with all WSRP
         // implementations
         payload = PARAMETER_PAYLOAD + "=" + URL.encode(URL.encode(payload));
 
         RequestBuilder rb = new RequestBuilder(RequestBuilder.POST, uri);
         // TODO enable timeout
         // rb.setTimeoutMillis(timeoutMillis);
         rb.setHeader("Content-Type", "application/x-www-form-urlencoded");
         rb.setRequestData(payload);
         rb.setCallback(requestCallback);
 
         rb.send();
     }
 
     public boolean isWSRPMode() {
         return WSRPMode;
     }
 
 }
