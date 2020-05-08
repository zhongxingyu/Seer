 /*
  * This file is part of PortListener.
  *
  * PortListener is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * PortListener is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with PortListener.  If not, see <http://www.gnu.org/licenses/>.
  */
 package net.crimsoncraft.portlistener.net;
 
 import com.google.common.base.Joiner;
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.Socket;
 import java.util.logging.Level;
 import net.crimsoncraft.portlistener.PortListener;
 import net.crimsoncraft.portlistener.api.PortReceiveEvent;
 import org.bukkit.Bukkit;
 
 /**
  * Thread for handling requests.
  */
 public class RequestHandler extends Thread {
     private final PortListener plugin;
 
     private Socket socket;
 
     public RequestHandler(PortListener plugin, Socket socket) {
         this.plugin = plugin;
         this.socket = socket;
     }
 
     @Override
     public void run() {
         try {
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
 
             // Read the request and handle it.
             handleRequest(socket, reader.readLine());
 
             // Finally close the socket.
             socket.close();
         } catch (IOException ignored) {
         } catch (Exception ex) {
             plugin.getLogger().log(Level.SEVERE, "Uh oh! Something unexpected happened.", ex);
         }
     }
 
     private void handleRequest(Socket socket, String message) throws IOException {
         DataOutputStream out = new DataOutputStream(socket.getOutputStream());
         if (parseRequest(message)) {
             out.writeBytes("GOOD");
         } else {
             out.writeBytes("BAD");
         }
     }
 
     private boolean parseRequest(String request) {
         String[] parts = request.split(";");
         if (parts.length < 3) {
             return false;
         }
 
         String referer = parts[0];
         String key = parts[1];
        String content = parts[2];
 
         if (referer.isEmpty()) {
             return false;
         }
 
         if (!key.equals(plugin.getRequestKey(referer))) {
             return false;
         }
 
         PortReceiveEvent event = new PortReceiveEvent(referer, key, content);
         Bukkit.getPluginManager().callEvent(event);
 
         return true;
     }
 
 }
