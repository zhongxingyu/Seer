 package com.omgrentbbq.client;
 
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.http.client.URL;
 import com.google.gwt.user.client.Window;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.*;
 import com.omgrentbbq.client.resources.MainBundle;
 import com.omgrentbbq.shared.model.Pair;
 import com.omgrentbbq.shared.model.User;
 import com.omgrentbbq.shared.model.UserSession;
 
 import java.io.Serializable;
 
 
 /**
  * Entry point classes define <code>onModuleLoad()</code>.
  */
 public class OmgRentBbq implements EntryPoint {
     /**
      * The message displayed to the session when the server cannot be reached or
      * returns an error.
      */
     @SuppressWarnings("unused")
     private static final String SERVER_ERROR = "An error occurred while "
             + "attempting to contact the server. Please check your network "
             + "connection and try again.";
     private DockPanel panel;
     private VerticalPanel authPanel;
 
 
     static String GDATA_API_KEY;
     private User user;
     private Anchor authAnchor;
 
     public void onModuleLoad() {
         final String host = Window.Location.getHost();
         final String proto = Window.Location.getProtocol();
         if (proto.startsWith("https"))
             GDATA_API_KEY = host.startsWith("127.0.0.1") ? "ABQIAAAAWpB08GH6KmKITXI7rtGRpBREGtQZq9OFJfHndXhPP8gxXzlLARRs1Zat3MllIUzN5hpmsbfnyEF7wA" :
                     "ABQIAAAAWpB08GH6KmKITXI7rtGRpBQP9W7Y7I5qr-k1KpACLx2-LL8VZRSAmDzEx8058dg-LbfPzLfgD1bPqQ";
         else
             GDATA_API_KEY = host.startsWith("127.0.0.1") ? "ABQIAAAAWpB08GH6KmKITXI7rtGRpBREGtQZq9OFJfHndXhPP8gxXzlLARRs1Zat3MllIUzN5hpmsbfnyEF7wA" :
                     "ABQIAAAAWpB08GH6KmKITXI7rtGRpBSZ0_RId71_G7aCA6qntwd15T_WaBRjfmPbE7W4RF2InR8N8OZxXPGNTQ";
         panel = new DockPanel();
         panel.add(new Image(MainBundle.INSTANCE.logo()), DockPanel.WEST);
         RootPanel.get().add(panel);
         final LoginAsync lm = GWT.create(Login.class);
 
 
         lm.getUserSession(Window.Location.getHref(), new AsyncCallback<Pair<UserSession, String>>() {
             @Override
             public void onFailure(Throwable throwable) {
 
             }
 
             @Override
             public void onSuccess(Pair<UserSession, String> userSessionURLPair) {
 
                 final UserSession userSession = userSessionURLPair.getFirst();
                 panel.add(new Label(userSession.toString()),DockPanel.CENTER);
                 authAnchor = new Anchor();
                 panel.add(authAnchor,DockPanel.EAST);
                 final String url = userSessionURLPair.getSecond();
                 authAnchor.setHref(url);
                 User user= (User) userSession.properties.get("user");
                authAnchor.setText(
                        (!url.contains("/_ah/logout?continue=" ))
 
                  ?"Sign in using your google account now":"not "+user.properties.get("nickname")+"? Sign Out"
 
                 );
             }
         });
     }
 }
 
