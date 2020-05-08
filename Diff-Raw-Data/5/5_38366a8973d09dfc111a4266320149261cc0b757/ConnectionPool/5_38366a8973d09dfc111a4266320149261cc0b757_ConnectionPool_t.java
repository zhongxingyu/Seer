 package org.apache.cassandra.casserole;
 
 import com.google.common.collect.Iterables;
 import org.apache.cassandra.dht.Token;
 import org.apache.cassandra.casserole.model.RingData;
 import org.apache.cassandra.service.StorageServiceMBean;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.lang.reflect.UndeclaredThrowableException;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 public class ConnectionPool {
     private static final Logger logger = LoggerFactory.getLogger(ConnectionPool.class);
     
     private String primaryHost;
     private Map<String, Connection> connections;
     private SchemaRetriever schemaRetriever = new SchemaRetriever();
     
     public ConnectionPool(Connection init) throws RemoteException
     {
         if (!init.isConnected())
             init.connect();
         primaryHost = init.getHost();
         connections = new HashMap<String, Connection>();
         Iterable<String> allHosts = Iterables.concat(
                 init.getStorageService().getLiveNodes(),
                 init.getStorageService().getUnreachableNodes(),
                 init.getStorageService().getJoiningNodes(),
                 init.getStorageService().getLeavingNodes()
         );
         for (String host : allHosts) {
             Connection con = new Connection();
             con.setHost(host);
            if (System.getProperty("futzjmx") != null)
                con.setJmxPort(JmxPortResolver.getPort(host, init.getJmxPort()));
            else
                con.setJmxPort(init.getJmxPort());
             con.setThriftPort(init.getThriftPort());
             try {
                 con.connect();
             } catch (RemoteException ex) {
                 // host might be down.
                 logger.warn("init pool. " + ex.getMessage());
             }
             connections.put(host, con);
         }
     }
     
     public Connection getPrimary() {
         return getConnection(primaryHost);
     }
     
     public Connection getConnection(String host) {
         // boil it down to an ip.
         InetAddress addr = null;
         try {
             addr = InetAddress.getByName(host);
         } catch (UnknownHostException ex) {
             // we should be beyond this crap by now.
             throw new RuntimeException(ex);
         }
         Connection con = connections.get(addr.getHostAddress());
         if (!con.isConnected()) {
             // this is a reconnect.
             try {
                 con.connect();
             } catch (RemoteException ex) {
                 logger.trace("Reconnect failed " + host);
             }
         }
         return con;
     }
     
     public SortedMap<Token, RingData> getRingData() {
         StorageServiceMBean bean = null;
         try {
             bean = getPrimary().getStorageService();
             // try something innocuous to see if the connection is ok.
             try {
                 bean.getOperationMode();
             } catch (UndeclaredThrowableException ex) {
                 logger.debug(ex.getMessage(), ex);
                 getPrimary().disconnect();
             }
         } catch (RemoteException ex) {
             logger.error(ex.getMessage(), ex);
             getPrimary().disconnect();
         }
         Map<Token, String> tokenMap = bean.getTokenToEndpointMap();
         List<Token> sortedTokens = new ArrayList<Token>(tokenMap.keySet());
         Collections.sort(sortedTokens);
         Map<String, String> loadMap = bean.getLoadMap();
         
         List<String> live = bean.getLiveNodes();
         List<String> unreachable = bean.getUnreachableNodes();
         List<String> joining = bean.getJoiningNodes();
         List<String> leaving = bean.getLeavingNodes();
         
         schemaRetriever.maybePullSchema(this);
         
         SortedMap<Token, RingData> map = new TreeMap<Token, RingData>();
         for (Token token : sortedTokens) {
             String host = tokenMap.get(token);
             String schema = schemaRetriever.getSchema(host);
             String status = "Unknown";
             if (live.contains(host))
                 status = "Up";
             if (unreachable.contains(host))
                 status = "Unreachable";
             if (joining.contains(host))
                 status = "Joining";
             if (leaving.contains(host))
                 status = "Leaving";
             String load = loadMap.get(host);
             if (load == null) load = "Unknown";
             String mode = "Unknown";
             try {
                 mode = getConnection(host).getStorageService().getOperationMode();
             } catch (RemoteException ex) {
                 mode = "Error";
             } catch (UndeclaredThrowableException ex) {
                 logger.debug(ex.getMessage(), ex);
                 mode = "Error";
                 getConnection(host).disconnect();
             }
             if (mode == null) {
                 logger.debug("Mode was null");
                 mode = "Not Null";
             }
             map.put(token, new RingData(token, host, status, load, mode, schema));
         } 
         return map;
     }
     
     // Since retrieving the schema for the entire cluster can be an expensive endeavor, only do it every five minutes.
     private static class SchemaRetriever {
         private Map<String, String> schemaVersions = new HashMap<String, String>();
         private long lastSchemaUpdate = 0;
         
         void maybePullSchema(ConnectionPool pool) {
             if (System.currentTimeMillis() - lastSchemaUpdate < 300000)
                 return;
             lastSchemaUpdate = System.currentTimeMillis();
             try {
                 for (Map.Entry<String, List<String>> entry : pool.getPrimary().getSchema().entrySet())
                     for (String ip : entry.getValue())
                         schemaVersions.put(ip, entry.getKey());
             } catch (RemoteException ex) {
                 logger.error(ex.getMessage(), ex);
             }    
         }
         
         String getSchema(String host) {
             if (schemaVersions.containsKey(host))
                 return schemaVersions.get(host);
             else
                 return "Unknown";
         }
     }
 }
