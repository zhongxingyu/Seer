 /*
  * Copyright (C) 2011 Near Infinity Corporation
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.nearinfinity.blur.manager.indexserver;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import org.apache.lucene.analysis.standard.StandardAnalyzer;
 import org.apache.lucene.index.CorruptIndexException;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.IndexWriterConfig;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.LockObtainFailedException;
 import org.apache.lucene.store.RAMDirectory;
 import org.apache.lucene.util.Version;
 import org.junit.After;
 import org.junit.Test;
 
 import com.nearinfinity.blur.manager.indexserver.ZkTest.ZkInMemory;
 import com.nearinfinity.blur.manager.writer.BlurIndex;
 import com.nearinfinity.blur.manager.writer.BlurIndexReader;
 
 public class DistributedIndexServerTest {
     
     private static final String TEST = "test";
     private static final List<String> NODE_LIST = Arrays.asList("node1","node2","node3","node4");
     private static final List<String> SHARD_LIST = Arrays.asList("a","b","c","d","e","f","g","h","i");
     
     @After
     public void tearDown() {
         
     }
 
     @Test
     public void testDistributedIndexServer() throws IOException {
         List<String> shardBeingServed = new ArrayList<String>();
         for (String node : NODE_LIST) {
             DistributedManager dm = new ZkInMemory();
            dm.createPath("blur","clusters","default","tables",TEST,"enabled");
             DistributedIndexServer indexServer = new MockDistributedIndexServer(NODE_LIST,SHARD_LIST);
             indexServer.setNodeName(node);
             indexServer.setDistributedManager(dm);
             indexServer.init();
             Set<String> keySet = indexServer.getIndexes(TEST).keySet();
             shardBeingServed.addAll(keySet);
             indexServer.close();
         }
         Collections.sort(shardBeingServed);
         assertEquals(SHARD_LIST,shardBeingServed);
     }
     
 //    @Test
     public void testReaderCloserDaemon() throws IOException, InterruptedException {
         List<String> nodes = new ArrayList<String>(NODE_LIST);
         final List<String> toBeClosed = new ArrayList<String>();
         toBeClosed.add("g");
         toBeClosed.add("h");
         DistributedManager dm = new ZkInMemory();
        dm.createPath("blur","clusters","default","tables",TEST,"enabled");
         DistributedIndexServer indexServer = new MockDistributedIndexServer(nodes, SHARD_LIST) {
             @Override
             protected void beforeClose(String shard, BlurIndex index) {
                 assertTrue(toBeClosed.contains(shard));
                 toBeClosed.remove(shard);
             }
         };
         indexServer.setDistributedManager(dm);
         indexServer.setNodeName("node2");
         indexServer.setDelay(5000);
         indexServer.init();
         assertEquals(new TreeSet<String>(Arrays.asList("g","h")), new TreeSet<String>(indexServer.getIndexes(TEST).keySet()));
         nodes.remove(3);
         assertEquals(new TreeSet<String>(Arrays.asList("a","b","c")), new TreeSet<String>(indexServer.getIndexes(TEST).keySet()));
         Thread.sleep(10000);
         assertTrue(toBeClosed.isEmpty());
         indexServer.close();
     }
     
     @Test
     public void testDistributedIndexServerTestingLayout() throws IOException {
         List<String> shardBeingServed = new ArrayList<String>();
         List<String> bigNodeList = getBigNodeList();
         List<String> bigShardList = getBigShardList();
         List<String> offlineNodes = new ArrayList<String>();
         
         Map<String,Set<String>> nodesToShards1 = new HashMap<String,Set<String>>();
         Map<String,DistributedIndexServer> servers = new TreeMap<String,DistributedIndexServer>();
         for (String node : bigNodeList) {
             DistributedManager dm = new ZkInMemory();
            dm.createPath("blur","clusters","default","tables",TEST,"enabled");
             DistributedIndexServer indexServer = new MockDistributedIndexServer(bigNodeList,bigShardList,offlineNodes);
             indexServer.setNodeName(node);
             indexServer.setDistributedManager(dm);
             indexServer.init();
             servers.put(node,indexServer);
             Set<String> keySet = indexServer.getIndexes(TEST).keySet();
             nodesToShards1.put(node, new TreeSet<String>(keySet));
             shardBeingServed.addAll(keySet);
         }
         
         Collections.sort(shardBeingServed);
         Collections.sort(bigShardList);
         assertEquals(bigShardList,shardBeingServed);
         
         shardBeingServed.clear();
         
         offlineNodes.add("node-7");
         for (DistributedIndexServer indexServer : servers.values()) {
             indexServer.shardServerStateChange();
         }
         
         Map<String,Set<String>> nodesToShards2 = new HashMap<String,Set<String>>();
         for (String node : bigNodeList) {
             DistributedIndexServer indexServer = servers.get(node);
             Set<String> keySet = indexServer.getIndexes(TEST).keySet();
             nodesToShards2.put(node, new TreeSet<String>(keySet));
             shardBeingServed.addAll(keySet);
         }
         
         Collections.sort(shardBeingServed);
         Collections.sort(bigShardList);
         assertEquals(bigShardList,shardBeingServed);
         
         for (String node : new TreeSet<String>(nodesToShards1.keySet())) {
             Set<String> set1 = nodesToShards1.get(node);
             Set<String> set2 = nodesToShards2.get(node);
             System.out.println("node [" + node +
             		"] removed [" + removed(set1,set2) +
             		"] added [" + added(set1,set2) +
             		"]");
         }
         
         for (DistributedIndexServer indexServer : servers.values()) {
             indexServer.close();
         }
     }
     
     private Set<String> added(Set<String> before, Set<String> after) {
         if (after == null) {
             after = new TreeSet<String>();
         }
         Set<String> results = new TreeSet<String>();
         for (String s : after) {
             if (!before.contains(s)) {
                 results.add(s);
             }
         }
         return results;
     }
 
     private Set<String> removed(Set<String> before, Set<String> after) {
         if (after == null) {
             after = new TreeSet<String>();
         }
         Set<String> results = new TreeSet<String>();
         for (String s : before) {
             if (!after.contains(s)) {
                 results.add(s);
             }
         }
         return results;
     }
 
     private List<String> getBigShardList() {
         List<String> shards = new ArrayList<String>();
         for (int i = 0; i < 512; i++) {
             shards.add("shard-"+i);
         }
         return shards;
     }
 
     private List<String> getBigNodeList() {
         List<String> nodes = new ArrayList<String>();
         for (int i = 0; i < 32; i++) {
             nodes.add("node-"+i);
         }
         return nodes;
     }
 
     public static class MockDistributedIndexServer extends DistributedIndexServer {
         
         private List<String> shards;
         private List<String> nodes;
         private List<String> offlineNodes;
         
         public MockDistributedIndexServer(List<String> nodes, List<String> shards) {
             this(nodes,shards,new ArrayList<String>());
             this.statusMap.get().put(TEST, TABLE_STATUS.ENABLED);
         }
 
         public MockDistributedIndexServer(List<String> nodes, List<String> shards, List<String> offlineNodes) {
             this.shards = shards;
             this.nodes = nodes;
             this.offlineNodes = offlineNodes;
         }
 
         @Override
         protected BlurIndex openShard(String table, String shard) {
             return getEmptyIndexReader();
         }
         
         private BlurIndex getEmptyIndexReader() {
             try {
                 return new BlurIndexReader(IndexReader.open(getEmptyDir()));
             } catch (IOException e) {
                 throw new RuntimeException(e);
             }
         }
 
         private Directory getEmptyDir() throws CorruptIndexException, LockObtainFailedException, IOException {
             RAMDirectory directory = new RAMDirectory();
             new IndexWriter(directory, new IndexWriterConfig(Version.LUCENE_33, new StandardAnalyzer(Version.LUCENE_33))).close();
             return directory;
         }
 
         @Override
         public List<String> getShardList(String table) {
             return shards;
         }
 
         @Override
         public List<String> getShardServerList() {
             return nodes;
         }
         
         @Override
         public void close() {
             
         }
 
 
         @Override
         public List<String> getControllerServerList() {
             throw new RuntimeException("not implement");
         }
 
         @Override
         protected void beforeClose(String shard, BlurIndex index) {
             throw new RuntimeException("not implement");            
         }
 
         @Override
         public List<String> getOfflineShardServers() {
             return offlineNodes;
         }
 
         @Override
         public List<String> getOnlineShardServers() {
             List<String> list = new ArrayList<String>(getShardServerList());
             list.removeAll(getOfflineShardServers());
             return list;
         }
 
 		@Override
 		public long getTableSize(String table) throws IOException {
 			throw new RuntimeException("no impl");
 		}
         
         
     }
 }
