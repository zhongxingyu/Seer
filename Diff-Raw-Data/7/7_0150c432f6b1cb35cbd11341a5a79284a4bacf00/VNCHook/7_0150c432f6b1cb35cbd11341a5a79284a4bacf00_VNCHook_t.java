 /*
  * Made by Wannes 'W' De Smet
  * (c) 2011 Wannes De Smet
  * All rights reserved.
  * 
  */
 package net.wgr.xenmaster.web;
 
 import com.google.gson.Gson;
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Map.Entry;
 import java.util.UUID;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.TimeUnit;
 import net.wgr.server.web.handling.WebCommandHandler;
 import net.wgr.utility.GlobalExecutorService;
 import net.wgr.wcp.Commander;
 import net.wgr.wcp.Scope;
 import net.wgr.wcp.command.Command;
 import net.wgr.wcp.command.CommandException;
 import net.wgr.xenmaster.api.Console;
 import net.wgr.xenmaster.api.VM;
 import net.wgr.xenmaster.connectivity.ConnectionMultiplexer;
 import net.wgr.xenmaster.controller.Controller;
 import org.apache.commons.codec.binary.Base64;
 import org.apache.log4j.Logger;
 
 /**
  * 
  * @created Dec 15, 2011
  * @author double-u
  */
 public class VNCHook extends WebCommandHandler {
 
     protected static ConnectionMultiplexer cm;
     protected static ConcurrentHashMap<String, Connection> connections;
     protected ConnectionMultiplexer.ActivityListener al;
     protected static int connectionCounter;
 
     public VNCHook() {
         super("vnc");
 
         if (cm == null) {
             setupInfrastructure();
         }
     }
 
     protected static String buildHttpConnect(URI uri) {
         StringBuilder sb = new StringBuilder();
         sb.append("CONNECT ");
         sb.append(uri.getPath()).append('?').append(uri.getQuery());
         sb.append(" HTTP/1.1").append("\r\n");
         sb.append("Cookie: session_id=").append(Controller.getSession().getReference());
         sb.append("\r\n\r\n");
         return sb.toString();
     }
 
     @Override
     public Object execute(Command cmd) {
         try {
             Gson gson = new Gson();
 
             switch (cmd.getName()) {
                 case "openConnection":
                     if (!cmd.getData().isJsonObject() || !cmd.getData().getAsJsonObject().has("ref")) {
                         throw new IllegalArgumentException("No VM reference parameter given");
                     }
                     Connection conn = new Connection(cmd.getConnection().getId());
                     VM vm = new VM(cmd.getData().getAsJsonObject().get("ref").getAsString(), false);
                     for (Console c : vm.getConsoles()) {
                         if (c.getProtocol() == Console.Protocol.RFB) {
                             try {
                                 URI uri = new URI(c.getLocation());
                                 conn.uri = uri;
                                 InetSocketAddress isa = new InetSocketAddress(uri.getHost(), 80);
                                 conn.waitForAddress = isa;
                                 cm.addConnection(isa);
                             } catch (URISyntaxException ex) {
                                 Logger.getLogger(getClass()).error("Failed to parse URI", ex);
                             }
                         }
                     }
 
                     conn.lastWriteTime = System.currentTimeMillis();
                     connections.put(conn.getReference(), conn);
                     return conn.getReference();
                 case "write":
                     Arguments data = gson.fromJson(cmd.getData(), Arguments.class);
                     if (!connections.containsKey(data.ref)) {
                         return new CommandException("Tried to write to unexisting connection", data.ref);
                     }
                     Connection c = connections.get(data.ref);
                     c.lastWriteTime = System.currentTimeMillis();
                     byte[] bytes = Base64.decodeBase64(data.data);
                     cm.write(c.connection, ByteBuffer.wrap(bytes));
                     break;
                 case "closeConnection":
                     Arguments close = gson.fromJson(cmd.getData(), Arguments.class);
                     cm.close(connections.get(close.ref).connection);
                     break;
             }
         } catch (IOException ex) {
             Logger.getLogger(getClass()).error("Command failed : " + cmd.getName(), ex);
         }
 
         return null;
     }
 
     protected static class Connection {
 
         public UUID clientId;
         public int connection;
         protected String reference;
         public InetSocketAddress waitForAddress;
         public long lastWriteTime;
         public URI uri;
         public boolean dismissedHttpOK;
 
         public Connection(UUID client) {
             connectionCounter++;
             this.reference = "ConnectionRef:" + connectionCounter;
             this.clientId = client;
         }
 
         public String getReference() {
             return reference;
         }
     }
 
     protected static class Arguments {
 
         public String data;
         public String ref;
 
         public Arguments() {
         }
 
         public Arguments(String data, String ref) {
             this.data = data;
             this.ref = ref;
         }
     }
 
     protected static class AL implements ConnectionMultiplexer.ActivityListener {
 
         @Override
         public void dataReceived(ByteBuffer buffer, int connection, ConnectionMultiplexer cm) {
             Connection conn = null;
             for (Entry<String, Connection> entry : connections.entrySet()) {
                 if (entry.getValue().connection == connection) {
                     conn = entry.getValue();
                     break;
                 }
             }
 
             if (conn == null) {
                Logger.getLogger(getClass()).warn("Received data on inactive connection " + connection + ". Closing ...");
                try {
                    cm.close(connection);
                } catch (IOException ex) {
                    Logger.getLogger(getClass()).error("Failed to close connection " + connection, ex);
                }
                 return;
             }
 
             byte[] data = buffer.array();
 
             if (!conn.dismissedHttpOK) {
                 String content = new String(data);
                 // RFB xxx.xxx denotes start of VNC handshake
                 if (content.contains("RFB")) {
                     conn.dismissedHttpOK = true;
                     data = content.substring(content.indexOf("RFB")).getBytes();
                 } else {
                     return;
                 }
             }
 
             if (data.length < 1) {
                 return;
             }
 
             Arguments vncData = new Arguments();
             vncData.ref = conn.getReference();
             vncData.data = Base64.encodeBase64String(data).replace("\r\n", "");
             Command cmd = new Command("vnc", "updateScreen", vncData);
             ArrayList<UUID> ids = new ArrayList<>();
             ids.add(conn.clientId);
             Scope scope = new Scope(ids);
             Commander.getInstance().commandeer(cmd, scope);
         }
 
         @Override
         public void connectionClosed(int connection) {
             for (Iterator<Entry<String, Connection>> it = connections.entrySet().iterator(); it.hasNext();) {
                 Entry<String, Connection> entry = it.next();
                 if (entry.getValue().connection == connection) {
                     Command cmd = new Command("vnc", "connectionClosed", new Arguments("", entry.getKey()));
                     Commander.getInstance().commandeer(cmd, new Scope(Scope.Target.ALL));
                     it.remove();
                     break;
                 }
             }
         }
 
         @Override
         public void connectionEstablished(int connection, Socket socket) {
             Connection conn = null;
             for (Entry<String, Connection> entry : connections.entrySet()) {
                 if (entry.getValue().waitForAddress.equals(socket.getRemoteSocketAddress())) {
                     conn = entry.getValue();
                     break;
                 }
             }
 
             if (conn == null) {
                 Logger.getLogger(getClass()).warn("Unknown connection established to " + ((InetSocketAddress) socket.getRemoteSocketAddress()).getHostString());
                 return;
             }
 
             conn.connection = connection;
             cm.write(conn.connection, ByteBuffer.wrap(buildHttpConnect(conn.uri).getBytes()));
 
             Command cmd = new Command("vnc", "connectionEstablished", new Arguments("", conn.getReference()));
             ArrayList<UUID> ids = new ArrayList<>();
             ids.add(conn.clientId);
             Scope scope = new Scope(ids);
             Commander.getInstance().commandeer(cmd, scope);
         }
     }
 
     protected static void setupInfrastructure() {
         cm = new ConnectionMultiplexer();
         cm.addActivityListener(new AL());
         cm.start();
         connections = new ConcurrentHashMap<>();
         GlobalExecutorService.get().scheduleAtFixedRate(new Reaper(), 0, 5, TimeUnit.MINUTES);
     }
 
     protected static class Reaper implements Runnable {
 
         @Override
         public void run() {
             for (Entry<String, Connection> entry : connections.entrySet()) {
                 if (System.currentTimeMillis() - entry.getValue().lastWriteTime > 1000 * 60) {
                     try {
                         cm.close(entry.getValue().connection);
                     } catch (IOException ex) {
                         Logger.getLogger(getClass()).error("Failed to close connection", ex);
                     }
                 }
             }
         }
     }
 }
