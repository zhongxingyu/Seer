 package com.trendmicro.mist.mfr;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.jms.ConnectionFactory;
 import javax.jms.JMSException;
 
 import org.apache.activemq.ActiveMQConnectionFactory;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.google.protobuf.TextFormat;
 import com.trendmicro.codi.CODIException;
 import com.trendmicro.codi.DataListener;
 import com.trendmicro.codi.DataObserver;
 import com.trendmicro.codi.ZNode;
 import com.trendmicro.mist.Daemon;
 import com.trendmicro.mist.proto.ZooKeeperInfo;
 import com.trendmicro.mist.util.ConnectionList;
 import com.trendmicro.spn.common.util.Utils;
 
 public class BrokerFarm implements DataListener {
     private static Log logger = LogFactory.getLog(BrokerFarm.class);
     private static BrokerFarm m_theSingleton = null;
 
     private HashMap<String, ZooKeeperInfo.Broker> allBrokers = new HashMap<String, ZooKeeperInfo.Broker>();
     private HashMap<String, ZooKeeperInfo.Loading> allBrokerLoadings = new HashMap<String, ZooKeeperInfo.Loading>();
     private DataObserver obs = null;
     private long lastUpdateTs = 0;
 
     private void initTmeNodes() {
         String node_prefix = "zookeeper.node.";
         for(Entry<Object, Object> ent : Daemon.propMIST.entrySet()) {
             String keyname = (String) ent.getKey();
             if(keyname.startsWith(node_prefix)) {
                 keyname = keyname.substring(node_prefix.length());
                 ZNode node = new ZNode(keyname);
                 try {
                     if(!node.exists())
                         node.create(false, new String((String) ent.getValue()).getBytes());
                 }
                 catch(CODIException.NodeExist e) {
                 }
                 catch(Exception e) {
                     logger.error(Utils.convertStackTrace(e));
                 }
             }
         }
     }
 
     public static javax.jms.Connection prepareJMSConnection(String broker_type, ConnectionList connList, String username, String password) throws JMSException {
         if(username == null)
             username = "";
         if(password == null)
             password = "";
 
         javax.jms.Connection theConn = null;
         try {            
             if(broker_type.equals("activemq")) {
                 String broker_uri = "tcp://" + connList.get(0).toString();                 
                 for(int i = 1; i < connList.size(); i++)
                     broker_uri += (",tcp://" + connList.get(i).toString());
                 if(connList.size() > 1)
                     broker_uri = String.format("failover:(%s)", broker_uri);
                 theConn = new ActiveMQConnectionFactory(broker_uri).createConnection(username, password);
             }
             else if(broker_type.equals("openmq")) {
                 ConnectionFactory conn_fact = new com.sun.messaging.ConnectionFactory();
                 if(connList.size() == 1) {
                     ((com.sun.messaging.ConnectionFactory) conn_fact).setProperty(com.sun.messaging.ConnectionConfiguration.imqBrokerHostName, connList.get(0).getHost());
                     ((com.sun.messaging.ConnectionFactory) conn_fact).setProperty(com.sun.messaging.ConnectionConfiguration.imqBrokerHostPort, connList.get(0).getPort());
                 }
                 else if(connList.size() > 1) {
                     ((com.sun.messaging.ConnectionFactory) conn_fact).setProperty(com.sun.messaging.ConnectionConfiguration.imqAddressList, connList.toString());
                     ((com.sun.messaging.ConnectionFactory) conn_fact).setProperty(com.sun.messaging.ConnectionConfiguration.imqAddressListIterations, "-1");
                     ((com.sun.messaging.ConnectionFactory) conn_fact).setProperty(com.sun.messaging.ConnectionConfiguration.imqReconnectEnabled, "true");
                     ((com.sun.messaging.ConnectionFactory) conn_fact).setProperty(com.sun.messaging.ConnectionConfiguration.imqReconnectAttempts, "1");
                 }
                 ((com.sun.messaging.ConnectionFactory) conn_fact).setProperty(com.sun.messaging.ConnectionConfiguration.imqDefaultUsername, username);
                 ((com.sun.messaging.ConnectionFactory) conn_fact).setProperty(com.sun.messaging.ConnectionConfiguration.imqDefaultPassword, password);
                 theConn = conn_fact.createConnection();
             }
         }
         catch(JMSException e) {
             throw e;
         }
         return theConn;
     }
 
     // ////////////////////////////////////////////////////////////////////////////
     public static final String BRK_NODE = "/tme2/broker";
 
     public BrokerFarm() {
         initTmeNodes();
         obs = new DataObserver(BRK_NODE, this, true, 0);
         obs.start();
     }
 
     public static BrokerFarm getInstance() {
         if(null == m_theSingleton)
             m_theSingleton = new BrokerFarm();
         return m_theSingleton;
     }
 
     public long getLastUpdateTs() {
         return lastUpdateTs;
     }
 
     public ZooKeeperInfo.Broker getBrokerByHost(String hostname) {
         return allBrokers.get(hostname);
     }
 
     public int getBrokerCount() {
         return allBrokers.size();
     }
 
     public Map<String, ZooKeeperInfo.Loading> getAllLoading() {
         return allBrokerLoadings;
     }
 
     public Map<String, ZooKeeperInfo.Broker> getAllBrokers() {
         return allBrokers;
     }
 
     public static boolean checkConnectable(ZooKeeperInfo.Broker broker) {
         if(broker.getStatus() != ZooKeeperInfo.Broker.Status.ONLINE)
             return false;
 
         boolean connectable = false;
         Socket sock = null;
         try {
             sock = new Socket();
             sock.setReuseAddress(true);
             sock.setTcpNoDelay(true);
             sock.connect(new InetSocketAddress(broker.getHost(), Integer.parseInt(broker.getPort())));
             BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
             for(int wait_cnt = 0; wait_cnt < 20 && !in.ready(); wait_cnt++)
                 Utils.justSleep(500);
 
             if(in.ready()) {
                 if(broker.getBrokerType().equals("openmq")) {
                     String line = in.readLine();
                     if(line.startsWith("101 "))
                         connectable = true;
                     else
                         logger.error("checkConnectable(): get " + line);
                 }
                 else if(broker.getBrokerType().equals("activemq")) {
                     connectable = true;
                 }
             }
         }
         catch(IOException e) {
             logger.warn("checkConnectable() " + e.getMessage());
         }
         finally {
             try {
                 sock.getInputStream().close();
                 sock.close();
             }
             catch(IOException e) {
                 logger.error(e.getMessage());
             }
         }
         return connectable;
     }
 
     public static boolean authenticateBroker(String broker_type, ConnectionList connList, String username, String password) {
         if(!Utils.checkSocketConnectable(connList.toString()))
             return false;
 
         try {
             javax.jms.Connection theConn = prepareJMSConnection(broker_type, connList, username, password);
             theConn.start();
             javax.jms.Session theSess = theConn.createSession(false, javax.jms.Session.AUTO_ACKNOWLEDGE);
             theSess.createQueue("tme.monitor.bounce");
             theSess.close();
             theConn.close();
             return true;
         }
         catch(Exception e) {
             logger.error(e.getMessage());
         }
         return false;
     }
 
     @Override
     public void onDataChanged(String parentPath, Map<String, byte[]> changeMap) {
         for(Entry<String, byte[]> ent : changeMap.entrySet()) {
             if(ent.getKey().length() == 0)
                 continue;
            else if(ent.getKey().endsWith(".lock"))
                continue;
             
             String host = ent.getKey();
             boolean isLoading = ent.getKey().endsWith("loading");
             if(isLoading)
                 host = host.substring(0, host.lastIndexOf('/'));
             if(ent.getValue() == null) {
                 if(isLoading)
                     allBrokerLoadings.remove(host);
                 else
                     allBrokers.remove(host);
             }
             else {
                 try {
                     if(isLoading) {
                         ZooKeeperInfo.Loading.Builder builder = ZooKeeperInfo.Loading.newBuilder();
                         TextFormat.merge(new String(ent.getValue()), builder);
                         allBrokerLoadings.put(host, builder.build());
                     }
                     else {
                         ZooKeeperInfo.Broker.Builder builder = ZooKeeperInfo.Broker.newBuilder();
                         TextFormat.merge(new String(ent.getValue()), builder);
                         allBrokers.put(host, builder.build());
                     }
                 }
                 catch(Exception e) {
                     logger.error(Utils.convertStackTrace(e));
                 }
             }
         }
         lastUpdateTs = new Date().getTime();
     }
 }
