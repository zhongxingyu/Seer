 package org.game.cs.support.websocket;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.game.cs.support.log.Observer;
 import org.game.cs.support.log.event.LogEvent;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.vertx.java.core.Handler;
 import org.vertx.java.core.http.ServerWebSocket;
 
 public class WebSocketHandler implements Handler<ServerWebSocket>, Observer {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketHandler.class);
     private Map<String, List<ServerWebSocket>> listeners = new ConcurrentHashMap<>();
 
     @Override
     public void handle(ServerWebSocket event) {
         if (isItGoodPath(event)) {
             registerListener(getServerAddress(event.path), event);
         } else {
             rejectConnection(event);
         }
     }
 
     private void rejectConnection(ServerWebSocket event) {
         event.reject();
         LOGGER.info("connection rejected");
     }
 
     private boolean isItGoodPath(ServerWebSocket event) {
         return event.path.startsWith("/websocket");
     }
 
     private void registerListener(String address, ServerWebSocket event) {
         if (!isServerRegistered(address)) {
             registerServerWithWebSocket(address, event);
         } else {
             addServerToList(address, event);
         }
     }
 
     private void addServerToList(String address, ServerWebSocket event) {
         getWebSocketList(address).add(event);
         LOGGER.info("websocket added to an already registered server");
     }
 
     private void registerServerWithWebSocket(String address, ServerWebSocket event) {
         listeners.put(address, new ArrayList<ServerWebSocket>(Arrays.asList(event)));
         LOGGER.info(address + " server registered");
     }
 
     private boolean isServerRegistered(String address) {
         return listeners.containsKey(address);
     }
 
     private String getServerAddress(String path) {
         return path.split("/")[2];
     }
 
     @Override
     public void update(LogEvent event) {
         LOGGER.info("event received: " + event.getMessage());
        for (ServerWebSocket webSocket : getWebSocketList(event.getSender())) {
            webSocket.writeTextFrame(event.getMessage());
         }
     }
 
     private List<ServerWebSocket> getWebSocketList(String address) {
         return listeners.get(address);
     }
 
 }
