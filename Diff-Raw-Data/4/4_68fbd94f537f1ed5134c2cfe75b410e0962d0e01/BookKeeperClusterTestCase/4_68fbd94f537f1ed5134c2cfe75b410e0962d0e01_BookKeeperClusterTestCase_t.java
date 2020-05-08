 /**
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.apache.bookkeeper.test;
 
 import org.apache.bookkeeper.client.MockBookKeeper;
 import org.apache.bookkeeper.conf.ClientConfiguration;
import org.apache.bookkeeper.util.ZkUtils;
 import org.apache.zookeeper.MockZooKeeper;
 import org.apache.zookeeper.ZooKeeper;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.testng.annotations.AfterMethod;
 import org.testng.annotations.BeforeMethod;
 
 /**
  * A class runs several bookie servers for testing.
  */
 public abstract class BookKeeperClusterTestCase {
 
     static final Logger LOG = LoggerFactory.getLogger(BookKeeperClusterTestCase.class);
 
     // ZooKeeper related variables
     protected MockZooKeeper zkc;
 
     // BookKeeper related variables
     protected MockBookKeeper bkc;
     protected int numBookies;
 
     protected ClientConfiguration baseClientConf = new ClientConfiguration();
 
     public BookKeeperClusterTestCase() {
         // By default start a 3 bookies cluster
         this(3);
     }
 
     public BookKeeperClusterTestCase(int numBookies) {
         this.numBookies = numBookies;
     }
 
     @BeforeMethod
     public void setUp() throws Exception {
         try {
             // start bookkeeper service
             startBookKeeper();
         } catch (Exception e) {
             LOG.error("Error setting up", e);
             throw e;
         }
     }
 
     @AfterMethod
     public void tearDown() throws Exception {
         LOG.info("TearDown");
         stopBookKeeper();
         stopZooKeeper();
     }
 
     /**
      * Start cluster
      * 
      * @throws Exception
      */
     protected void startBookKeeper() throws Exception {
         zkc = MockZooKeeper.newInstance();
         for (int i = 0; i < numBookies; i++) {
            ZkUtils.createFullPathOptimistic(zkc, "/ledgers/available/192.168.1.1:" + (5000 + i), "".getBytes(), null, null);
         }
 
         zkc.create("/ledgers/LAYOUT", "1\nflat:1".getBytes(), null, null);
 
         bkc = new MockBookKeeper(baseClientConf, zkc);
     }
 
     protected void stopBookKeeper() throws Exception {
         bkc.shutdown();
     }
 
     protected void stopZooKeeper() throws Exception {
         zkc.shutdown();
     }
 
 }
