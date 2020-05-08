 /**
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.apache.cassandra.hadoop;
 
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Random;
 import java.util.concurrent.TimeoutException;
 
 import org.apache.cassandra.thrift.*;
 import org.apache.cassandra.utils.CircuitBreaker;
 import org.apache.log4j.Logger;
 import org.apache.thrift.TException;
 import org.apache.thrift.transport.*;
 
 /**
  * This wraps the underlying Cassandra thrift client and attempts to handle
  * disconnect, unavailable, timeout errors gracefully.
  * 
  * On disconnect, if it cannot reconnect to the same host then it will use a
  * different host from the ring, which it periodically checks for updates to.
  * 
  * This incorporates the CircuitBreaker pattern so not to overwhelm the network
  * with reconnect attempts.
  * 
  */
 public class CassandraProxyClient implements java.lang.reflect.InvocationHandler
 {
 
     private static final Logger logger  = Logger.getLogger(CassandraProxyClient.class);
 
     private String              host;
     private int                 port;
     private final boolean       framed;
     private final boolean       randomizeConnections;
     private long                lastPoolCheck;
     private List<TokenRange>    ring;
     private Cassandra.Client    client;
     private String              ringKs;
     private CircuitBreaker      breaker = new CircuitBreaker(1, 1);
 
    public static Cassandra.Iface newProxyConnection(String host, int port, boolean framed, boolean randomizeConnections)
             throws IOException
     {
 
        return (Cassandra.Iface) java.lang.reflect.Proxy.newProxyInstance(Cassandra.Client.class.getClassLoader(),
                 Cassandra.Client.class.getInterfaces(), new CassandraProxyClient(host, port, framed,
                         randomizeConnections));
     }
 
     private Cassandra.Client createConnection(String host, Integer port, boolean framed) throws IOException
     {
         TSocket socket = new TSocket(host, port);
         TTransport trans = framed ? new TFramedTransport(socket) : socket;
         try
         {
             trans.open();
         }
         catch (TTransportException e)
         {
             throw new IOException("unable to connect to server", e);
         }
         
         Cassandra.Client client = new Brisk.Client(new TBinaryProtocol(trans));
         
         //connect to last known keyspace
         if(ringKs != null)
         {
             try
             {
                 client.set_keyspace(ringKs);
             }
             catch (Exception e)
             {
                 throw new IOException(e);
             }
         }
         
         return client;
     }
 
     private CassandraProxyClient(String host, int port, boolean framed, boolean randomizeConnections)
             throws IOException
     {
 
         this.host = host;
         this.port = port;
         this.framed = framed;
         this.randomizeConnections = randomizeConnections;
         lastPoolCheck = 0;
 
         initialize();
     }
 
     private void initialize() throws IOException
     {
         attemptReconnect();
 
         try
         {
             List<KsDef> allKs = client.describe_keyspaces();
 
             if (allKs.isEmpty() || (allKs.size() == 1 && allKs.get(0).name.equalsIgnoreCase("system")))
                 allKs.add(createTmpKs());
 
             for(KsDef ks : allKs)
             {   
                 if(!ks.name.equalsIgnoreCase("system"))
                     ringKs = ks.name;
             }            
         }
         catch (Exception e)
         {
             throw new IOException(e);
         }
 
         checkRing();
     }
 
     private KsDef createTmpKs() throws InvalidRequestException, TException, InterruptedException
     {
         KsDef tmpKs = new KsDef("proxy_client_ks", "org.apache.cassandra.locator.SimpleStrategy", 1, Arrays
                 .asList(new CfDef[] {}));
 
         client.system_add_keyspace(tmpKs);
 
         return tmpKs;
     }
 
     private synchronized void checkRing() throws IOException
     {
 
         if (client == null)
         {
             breaker.failure();
             return;
         }
 
         long now = System.currentTimeMillis();
 
         if ((now - lastPoolCheck) > 60 * 1000)
         {
             try
             {
                 if (breaker.allow())
                 {
                     ring = client.describe_ring(ringKs);
                     lastPoolCheck = now;
 
                     breaker.success();
                 }
             }
             catch (TException e)
             {
                 breaker.failure();
                 attemptReconnect();
             }
             catch (InvalidRequestException e)
             {
                 throw new IOException(e);
             }
         }
 
     }
 
     private Cassandra.Iface attemptReconnect()
     {
 
         // first try to connect to the same host as before
         if (!randomizeConnections || ring == null || ring.size() == 0)
         {
 
             try
             {
                 client = createConnection(host, port, framed);
                 breaker.success();
                 if(logger.isDebugEnabled())
                     logger.debug("Connected to cassandra at " + host + ":" + port);
                 return client;
             }
             catch (IOException e)
             {
                 logger.warn("Connection failed to Cassandra node: " + host + ":" + port + " " + e.getMessage());
             }
         }
 
         // this is bad
         if (ring == null || ring.size() == 0)
         {
             logger.warn("No cassandra ring information found, no other nodes to connect to");
             return null;
         }
 
         // pick a different node from the ring
         Random r = new Random();
 
         List<String> endpoints = ring.get(r.nextInt(ring.size())).endpoints;
         String endpoint = endpoints.get(r.nextInt(endpoints.size()));
 
         if (!randomizeConnections)
         {
             // only one node (myself)
             if (endpoint.equals(host) && ring.size() == 1)
             {
                 logger.warn("No other cassandra nodes in this ring to connect to");
                 return null;
             }
 
             // make sure this is a node other than ourselves
             while (endpoint.equals(host))
             {
                 endpoint = endpoints.get(r.nextInt(endpoints.size()));
             }
         }
 
         try
         {
             client = createConnection(endpoint, port, framed);
             breaker.success();
             if(logger.isDebugEnabled())
                 logger.debug("Connected to cassandra at " + endpoint + ":" + port);
         }
         catch (IOException e)
         {
             logger.warn("Failed connecting to a different cassandra node in this ring: " + endpoint + ":" + port);
 
             try
             {
                 client = createConnection(host, port, framed);
                 breaker.success();
                 if(logger.isDebugEnabled())
                     logger.debug("Connected to cassandra at " + host + ":" + port);
             }
             catch (IOException e2)
             {
                 logger.warn("Connection failed to Cassandra node: " + host + ":" + port);
             }
 
             return null;
         }
 
         return client;
     }
 
     public Object invoke(Object proxy, Method m, Object[] args) throws Throwable
     {
         Object result = null;
 
         int tries = 0;
         int maxTries = 10;
 
         //Keep last known keyspace
         if(m.getName().equalsIgnoreCase("set_keyspace") && args.length == 1)
             ringKs = (String)args[0];
         
         // incase this is the first time
         if (ring == null)
             checkRing();
 
         while (result == null && tries++ < maxTries)
         {
 
             // don't even try if client isn't connected
             if (client == null)
             {
                 breaker.failure();
             }
 
             try
             {
 
                 if (breaker.allow())
                 {
                     result = m.invoke(client, args);
                     breaker.success();
                     return result;
                 }
                 else
                 {
 
                     while (!breaker.allow())
                     {
                         Thread.sleep(1050); // sleep and try again
                     }
 
                     attemptReconnect();
                 }
             }
             catch (InvocationTargetException e)
             {
 
                 if (e.getCause() instanceof UnavailableException || e.getCause() instanceof TimeoutException
                         || e.getCause() instanceof TTransportException)
                 {
 
                     breaker.failure();
                     attemptReconnect();
 
                     // rethrow on last try
                     if (tries >= maxTries)
                         throw e.getCause();
                 }
                 else
                 {
                     throw e.getCause();
                 }
             }
             catch (Exception e)
             {
                 logger.error("Error invoking a method via proxy: ", e);
                 throw new RuntimeException(e);
             }
 
         }
 
         throw new UnavailableException();
     }
 
 }
