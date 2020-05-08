 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.codejive.websocket.wstestserver;
 
 import java.io.IOException;
 import javax.servlet.ServletConfig;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.codejive.rws.RwsContext;
 import org.codejive.rws.RwsException;
 
 import org.codejive.rws.RwsObject;
 import org.codejive.rws.RwsSession;
 import org.codejive.rws.converters.RwsBeanConverter;
 import org.codejive.rws.utils.RwsContextWebFactory;
 import org.eclipse.jetty.websocket.WebSocket;
 import org.eclipse.jetty.websocket.WebSocketServlet;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class DangerZoneWebSocketServlet extends WebSocketServlet {
 
     private static final RwsContext context = new RwsContext();
     private static final DataStore dataStore = new DataStore();
     
     Logger logger = LoggerFactory.getLogger(DangerZoneWebSocketServlet.class);
 
     @Override
     public void init(ServletConfig config) throws ServletException {
         super.init(config);
         try {
 
             RwsBeanConverter conv = new RwsBeanConverter(context.getRegistry());
             // TODO Make this configurable!
             RwsObject srv = new RwsObject(DangerZoneWebSocketServlet.class, "Server", conv);
             // METHODS new String[]{"echo"}, true
             // INSTANCE srv.setTargetObject(null, this); // Scope.global,
             context.getRegistry().register(srv, context, "server", this);
 
             RwsObject ctx = new RwsObject(RwsContext.class, "Context", conv);
             // METHODS new String[]{"listClients", "subscribeConnect", "unsubscribeConnect", "subscribeDisconnect", "unsubscribeDisconnect", "subscribeChange", "unsubscribeChange"}, true
             // INSTANCE Scope.global
             context.getRegistry().register(ctx, context, "context", context);
 
             RwsObject clt = new RwsObject(RwsSession.class, "Session", conv);
             // INSTANCE Scope.connection
            context.getRegistry().register(clt);
 
             RwsObject store = new RwsObject(DataStore.class, "DataStore", conv);
             // INSTANCE store.setTargetObject(null, dataStore); // Scope.global,
             context.getRegistry().register(store, context, "dataStore", dataStore);
 
             RwsObject pkg = new RwsObject(PackageInfo.class, "PackageInfo", conv);
             // INSTANCE pkg.setTargetObject(null, new Package(config)); // , Scope.global
             context.getRegistry().register(pkg, context, "packageInfo", new PackageInfo(config));
             
 //            String[] cltProps = {"id", "name"};
 //            context.getRegistry().register(new RwsBeanConverter(cltProps, true), Clients.ClientInfo.class.getName());
 //            context.getRegistry().register(new RwsBeanConverter(), Clients.ClientEvent.class.getName());
//            context.getRegistry().register(new RwsBeanConverter(), RwsSession.Subscription.class.getName());
 
             RwsContextWebFactory.getInstance(config.getServletContext()).setContext(context);
         } catch (RwsException ex) {
             throw new ServletException("Could not initialize context.getRegistry()", ex);
         }
     }
 
     @Override
     protected void doGet(HttpServletRequest request, HttpServletResponse response)
             throws javax.servlet.ServletException, IOException {
         getServletContext().getNamedDispatcher("default").forward(request, response);
     }
 
     @Override
     protected WebSocket doWebSocketConnect(HttpServletRequest request, String protocol) {
         return new DangerZoneWebSocket();
     }
 
     public Object echo(Object value) {
         return value;
     }
     
     /* ------------------------------------------------------------ */
     /* ------------------------------------------------------------ */
     class DangerZoneWebSocket implements WebSocket {
         private JettyWebSocketAdapter adapter;
         
         @Override
         public void onConnect(Outbound outbound) {
             logger.info(this + " onConnect - creating JettyWebSocketAdapter");
             adapter = new JettyWebSocketAdapter(context, outbound);
             adapter.onConnect();
         }
 
         @Override
         public void onMessage(byte frame, byte[] data, int offset, int length) {
             // Log.info(this+" onMessage: "+TypeUtil.toHexString(data,offset,length));
         }
 
         @Override
         public void onMessage(byte frame, String msg) {
             adapter.onMessage(msg);
         }
 
         @Override
         public void onDisconnect() {
             adapter.onDisconnect();
             adapter = null;
         }
     }
 }
