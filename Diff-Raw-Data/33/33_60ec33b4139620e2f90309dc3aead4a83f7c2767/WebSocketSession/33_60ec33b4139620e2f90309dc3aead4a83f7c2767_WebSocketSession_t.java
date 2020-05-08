 /* Copyright (c) 2011 Danish Maritime Authority
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this library.  If not, see <http://www.gnu.org/licenses/>.
  */
 package dk.dma.ais.virtualnet.common.websocket;
 
 import java.io.IOException;
 import java.util.concurrent.CountDownLatch;
 
 import org.eclipse.jetty.websocket.api.RemoteEndpoint;
 import org.eclipse.jetty.websocket.api.Session;
 import org.eclipse.jetty.websocket.api.WebSocketListener;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.gson.Gson;
 
 import dk.dma.ais.packet.AisPacket;
 import dk.dma.ais.virtualnet.common.message.WsMessage;
 
 public abstract class WebSocketSession implements WebSocketListener {
     
     private static final Logger LOG = LoggerFactory.getLogger(WebSocketSession.class);
     
     private final Gson gson = new Gson();
 
     private final CountDownLatch connected = new CountDownLatch(1);
 
     private Session session;
 
     public WebSocketSession() {
         
     }
     
     /**
      * Method to handle incoming messages
      */
     protected abstract void handleMessage(WsMessage wsMessage);
     
     @Override
     public void onWebSocketConnect(Session session) {
         LOG.info("Client connected: " + session.getRemoteAddress());
         this.session = session;
         getConnected().countDown();
     }
     
     @Override
     public void onWebSocketClose(int statusCode, String reason) {
         LOG.info("Client connection closed: " + session.getRemoteAddress());
         session = null;
     }
 
     @Override
     public void onWebSocketBinary(byte[] payload, int offset, int len) {
         LOG.error("Received binary data");
         Session s = session;
         try {
             s.close(1, "Expected text only");
         } catch (IOException e) {
             LOG.error("Failed to close web sokcet", e);
         }
     }
 
     @Override
     public void onWebSocketError(Throwable t) {
         
     }
 
     @Override
     public void onWebSocketText(String message) {
         // Try to deserialize into message
         WsMessage msg = gson.fromJson(message, WsMessage.class);
         // TODO handle exception
         handleMessage(msg);        
     }
     
     public final void close() {
         Session s = session;
         try {
             if (s != null) {
                 s.close();
             }
         } catch (IOException e) {
             LOG.error("Failed to close web socket: " + e.getMessage());
         }
     }
     
     public void sendPacket(AisPacket packet) {
         sendMessage(new WsMessage(packet));
     }
     
     public void sendPacket(String packet) {
         WsMessage wsMessage = new WsMessage();
         wsMessage.setPacket(packet);
         sendMessage(wsMessage);
     }
     
     protected final void sendMessage(WsMessage wsMessage) {
         sendText(gson.toJson(wsMessage));
     }
     
     private void sendText(String text) {
         Session s = session;
         RemoteEndpoint r = s == null ? null : s.getRemote();
         if (r != null) {
             try {
                 r.sendString(text);
             } catch (IOException e) {
                 // ignore
             }
         } else {
             LOG.error("Could not send to web socket");
         }
     }
 
     public CountDownLatch getConnected() {
         return connected;
     }
 
 
 }
