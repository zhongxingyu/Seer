 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package org.codejive.websocket.wstestserver;
 
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import org.codejive.rws.RwsException;
 import org.codejive.rws.RwsRegistry;
 import org.codejive.rws.RwsWebSocketAdapter;
 import org.codejive.websocket.wstestserver.Clients.ClientInfo;
 import org.eclipse.jetty.websocket.WebSocket.Outbound;
import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author tako
  */
 public class JettyWebSocketAdapter implements RwsWebSocketAdapter {
     private Outbound outbound;
     private ClientInfo client;
 
     private static final Clients clients = new Clients();
 
     private static final Logger log = LoggerFactory.getLogger(DangerZoneWebSocketServlet.class);
 
     public JettyWebSocketAdapter(Outbound outbound) {
         this.outbound = outbound;
     }
 
     @Override
     public void onConnect() {
         client = clients.addClient(this);
         RwsRegistry.getObject("Client").setTargetObject(client, client);
         RwsRegistry.getObject("Clients").setTargetObject(null, clients);
 
     }
 
     @Override
     public void onMessage(String msg) {
         log.info(this + " onMessage: " + msg);
         try {
             JSONParser parser = new JSONParser();
             JSONObject info = (JSONObject) parser.parse(msg);
             String to = (String) info.get("to");
             String action = (String) info.get("action");
             if (to == null || "sys".equals(to)) {
                 // The message is for the server
                 doCall(info);
             } else if ("all".equals(to)) {
                 // Send the message to all connected sockets
                 clients.sendAll(client.getId(), info, false);
             } else {
                 // Send the message to the indicated socket
                 clients.sendTo(client.getId(), to, info);
             }
         } catch (ParseException ex) {
             log.error("Couldn't parse incoming message", ex);
         } catch (IOException ex) {
             log.error("Could not send message, disconnecting socket", ex);
             clients.removeClient(client);
             if (isConnected()) {
                 disconnect();
             }
         }
     }
 
     @Override
     public void onDisconnect() {
         log.info(this + " onDisconnect");
         clients.removeClient(client);
         client = null;
     }
 
     @Override
     public boolean isConnected() {
         return outbound.isOpen();
     }
 
     @Override
     public void disconnect() {
         outbound.disconnect();
     }
 
     @Override
     public void sendMessage(String msg) throws IOException {
         outbound.sendMessage(msg);
     }
 
     private void doCall(Object data) throws IOException {
         JSONObject info = (JSONObject) data;
         String id = (String) info.get("id"); // If null the caller is not interested in the result!
         String obj = (String) info.get("object");
         String method = (String) info.get("method");
         Object params = (Object) info.get("params");
 
         Object[] args = null;
         // Convert parameter map to array
        if (params != null && params instanceof JSONArray) {
            JSONArray p = (JSONArray) params;
             args = new Object[p.size()];
             for (int i = 0; i < p.size(); i++) {
                args[i] = p.get(i);
             }
         }
 
         try {
             Object result = RwsRegistry.call(client, obj, method, args);
             if (id != null) {
                 client.send("sys", newCallResult(id, result));
             }
         } catch (InvocationTargetException ex) {
             log.error("Remote object returned an error", ex);
             if (id != null) {
                 client.send("sys", newCallException(id, ex));
             }
         } catch (RwsException ex) {
             log.error("Rws Call failed", ex);
             if (id != null) {
                 client.send("sys", newCallException(id, ex));
             }
         }
     }
 
     private JSONObject newCallResult(String id, Object data) {
         JSONObject obj = new JSONObject();
         obj.put("id", id);
         obj.put("result", data);
         return obj;
     }
 
     private JSONObject newCallException(String id, Throwable th) {
         JSONObject obj = new JSONObject();
         obj.put("id", id);
         obj.put("exception", th.toString());
         return obj;
     }
 }
