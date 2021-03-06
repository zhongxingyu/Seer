 /* This file is part of VoltDB.
  * Copyright (C) 2008-2012 VoltDB Inc.
  *
  * Permission is hereby granted, free of charge, to any person obtaining
  * a copy of this software and associated documentation files (the
  * "Software"), to deal in the Software without restriction, including
  * without limitation the rights to use, copy, modify, merge, publish,
  * distribute, sublicense, and/or sell copies of the Software, and to
  * permit persons to whom the Software is furnished to do so, subject to
  * the following conditions:
  *
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
  * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
  * IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY CLAIM, DAMAGES OR
  * OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
  * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
  * OTHER DEALINGS IN THE SOFTWARE.
  */
 package org.voltcore.zk;
 
 import static org.junit.Assert.*;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import org.voltcore.agreement.ZKUtil;
 import org.voltcore.messaging.HostMessenger;
 import org.voltcore.utils.Pair;
 import org.apache.zookeeper_voltpatches.*;
 import org.apache.zookeeper_voltpatches.KeeperException.NoNodeException;
 import org.apache.zookeeper_voltpatches.Watcher.Event.EventType;
 import org.apache.zookeeper_voltpatches.ZooDefs.Ids;
 import org.apache.zookeeper_voltpatches.data.Stat;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.net.InetSocketAddress;
 import java.util.concurrent.Semaphore;
 import java.util.concurrent.TimeUnit;
 
 public class TestZK extends ZKTestBase {
 
     private final int NUM_AGREEMENT_SITES = 8;
 
     @Before
     public void setUp() throws Exception {
         setUpZK(NUM_AGREEMENT_SITES);
     }
 
     @After
     public void tearDown() throws Exception {
         tearDownZK();
     }
 
     public void failSite(int site) throws Exception {
         m_messengers.get(site).shutdown();
         m_messengers.set(site, null);
     }
 
     public void recoverSite(int site) throws Exception {
         HostMessenger.Config config = new HostMessenger.Config();
         config.internalPort += site;
         config.zkInterface = "127.0.0.1:" + (2182 + site);
         config.networkThreads = 1;
         config.coordinatorIp = new InetSocketAddress("127.0.0.1", config.internalPort + NUM_AGREEMENT_SITES - 1);
         HostMessenger hm = new HostMessenger(config);
         hm.start();
         m_messengers.set(site, hm);
     }
 
     @Test
     public void testBasic() throws Exception {
         ZooKeeper zk = getClient(0);
         zk.create("/foo", new byte[1], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
 
         ZooKeeper zk2 = getClient(1);
         Stat stat = new Stat();
         assertEquals( 1, zk2.getData("/foo", false, stat).length);
         zk2.setData("/foo", new byte[4], stat.getVersion());
 
         assertEquals( 4, zk.getData("/foo", false, stat).length);
         zk.delete("/foo", -1);
 
         zk2.create("/bar", new byte[6], Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
         zk.create("/bar", new byte[7], Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
 
         ZooKeeper zk3 = getClient(2);
         List<String> children = zk3.getChildren("/", false);
         System.out.println(children);
         assertEquals(4, children.size());
         assertTrue(children.contains("zookeeper"));
        assertTrue(children.contains("bar0000000003"));
        assertTrue(children.contains("bar0000000004"));
         assertTrue(children.contains("core"));
 
         zk.close();
         zk2.close();
         m_clients.clear(); m_clients.add(zk3);
 
         children = zk3.getChildren("/", false);
         System.out.println(children);
         assertEquals(2, children.size());
         assertTrue(children.contains("zookeeper"));
         assertTrue(children.contains("core"));
     }
 
     @Test
     public void testFailure() throws Exception {
         ZooKeeper zk = getClient(1);
         zk.create("/foo", new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
         assertEquals(zk.getData("/foo", false, null).length, 0);
         System.out.println("Created node");
         failSite(0);
         assertEquals(zk.getData("/foo", false, null).length, 0);
     }
 
     @Test
     public void testFailureKillsEphemeral() throws Exception {
         ZooKeeper zk = getClient(0);
         zk.create("/foo", new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
         assertEquals(zk.getData("/foo", false, null).length, 0);
         failSite(0);
         zk = getClient(1);
         try {
             zk.getData("/foo", false, null);
         } catch (NoNodeException e) {
             return;
         }
         fail();
     }
 
     @Test
     public void testRecovery() throws Exception {
         ZooKeeper zk = getClient(0);
         zk.create("/foo", new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
         failSite(0);
         zk.close();
         for (int ii = 1; ii < NUM_AGREEMENT_SITES; ii++) {
             zk = getClient(ii);
             assertEquals(zk.getData("/foo", false, null).length, 0);
         }
         recoverSite(0);
         assertEquals(zk.getData("/foo", false, null).length, 0);
         zk = getClient(0);
         assertEquals(zk.getData("/foo", false, null).length, 0);
     }
 
     @Test
     public void testWatches() throws Exception {
         ZooKeeper zk = getClient(0);
         ZooKeeper zk2 = getClient(1);
         final Semaphore sem = new Semaphore(0);
         zk.exists("/foo", new Watcher() {
             @Override
             public void process(WatchedEvent event) {
                 if (event.getType() == EventType.NodeCreated) {
                     sem.release();
                     System.out.println(event);
                 }
             }
         });
         zk2.create("/foo", new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
         sem.tryAcquire(5, TimeUnit.SECONDS);
 
         zk.create("/foo2", new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
 
         zk2.exists("/foo2", new Watcher() {
             @Override
             public void process(WatchedEvent event) {
                 if (event.getType() == EventType.NodeDeleted) {
                     sem.release();
                     System.out.println(event);
                 }
             }
         });
 
         zk.delete("/foo2", -1);
         sem.acquire();
     }
 
     @Test
     public void testNullVsZeroLengthData() throws Exception {
         ZooKeeper zk = getClient(0);
         zk.create("/foo", new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
         zk.create("/bar", null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
         assertEquals(null, zk.getData("/bar", false, null));
         assertTrue(zk.getData("/foo", false, null).length == 0);
     }
 
     @Test
     public void testSessionExpireAndRecovery() throws Exception {
         ZooKeeper zk = getClient(0);
         zk.create("/foo", null, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
         failSite(1);
         recoverSite(1);
         for (int ii = 2; ii < 8; ii++) {
             failSite(ii);
         }
         failSite(0);
         zk = getClient(1);
         assertNull(zk.exists("/foo", false));
     }
 
     @Test
     public void testSortSequentialNodes() {
         ArrayList<String> nodes = new ArrayList<String>();
         nodes.add("/a/b/node0000012345");
         nodes.add("/a/b/node0000000000");
         nodes.add("/a/b/node0000010234");
         nodes.add("/a/b/node0000000234");
         ZKUtil.sortSequentialNodes(nodes);
 
         Iterator<String> iter = nodes.iterator();
         assertEquals("/a/b/node0000000000", iter.next());
         assertEquals("/a/b/node0000000234", iter.next());
         assertEquals("/a/b/node0000010234", iter.next());
         assertEquals("/a/b/node0000012345", iter.next());
     }
 
     @Test
     public void testLeaderElection() throws Exception {
         ZooKeeper zk = getClient(0);
         ZooKeeper zk2 = getClient(1);
         ZooKeeper zk3 = getClient(2);
 
         zk.create("/election", new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
 
         Pair<String, Boolean> result1 = ZKUtil.createAndElectLeader(zk, "/election", new byte[0], null);
         Pair<String, Boolean> result2 = ZKUtil.createAndElectLeader(zk2, "/election", new byte[0], null);
         Pair<String, Boolean> result3 = ZKUtil.createAndElectLeader(zk3, "/election", new byte[0], null);
 
         assertTrue(result1.getSecond());
         assertFalse(result2.getSecond());
         assertFalse(result3.getSecond());
 
         zk.close();
         zk2.close();
         zk3.close();
     }
 
 //    @Test
 //    public void testMassiveNode() throws Exception {
 //        ZooKeeper zk = getClient(0);
 //        byte bytes[] = new byte[1048400];
 //        zk.create("/bar", null, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
 //        zk.create("/foo", bytes, Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
 //    }
 }
