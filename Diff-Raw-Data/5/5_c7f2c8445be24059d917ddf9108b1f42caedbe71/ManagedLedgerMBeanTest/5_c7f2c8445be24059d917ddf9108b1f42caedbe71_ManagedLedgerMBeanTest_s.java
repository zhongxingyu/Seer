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
 package org.apache.bookkeeper.mledger.impl;
 
 import static org.testng.Assert.assertEquals;
 import static org.testng.Assert.assertTrue;
 
 import java.util.Arrays;
 import java.util.List;
import java.util.Random;

import junit.framework.Assert;
 
 import org.apache.bookkeeper.mledger.Entry;
 import org.apache.bookkeeper.mledger.ManagedCursor;
 import org.apache.bookkeeper.mledger.ManagedLedgerFactory;
 import org.apache.bookkeeper.test.BookKeeperClusterTestCase;
import org.apache.commons.lang.math.RandomUtils;
 import org.testng.annotations.Test;
 
 public class ManagedLedgerMBeanTest extends BookKeeperClusterTestCase {
     
     private void waitForRefresh(ManagedLedgerMBeanImpl mbean) {
         try {
             Thread.sleep(100);
         } catch (Exception e) {
             // do nothing
         }
     }
     
     @Test
     public void simple() throws Exception {
         ManagedLedgerFactory factory = new ManagedLedgerFactoryImpl(bkc, zkc);
         ManagedLedgerImpl ledger = (ManagedLedgerImpl) factory.open("my_test_ledger");
         ManagedCursor cursor = ledger.openCursor("c1");
         ManagedLedgerMBeanImpl mbean = ledger.mbean;
 
         assertEquals(mbean.getName(), "my_test_ledger");
 
         assertEquals(mbean.getStoredMessagesSize(), 0);
         assertEquals(mbean.getNumberOfMessagesInBacklog(), 0);
 
         waitForRefresh(mbean);
         
         mbean.addAddEntryLatencySample(1.0);
         mbean.addAddEntryLatencySample(10.0);
         mbean.addAddEntryLatencySample(1000.0);
 
         mbean.refreshStats();
 
         assertEquals(mbean.getAddEntryBytesRate(), 0.0);
         assertEquals(mbean.getAddEntryMessagesRate(), 0.0);
         assertEquals(mbean.getAddEntrySucceed(), 0);
         assertEquals(mbean.getAddEntryErrors(), 0);
         assertEquals(mbean.getReadEntriesBytesRate(), 0.0);
         assertEquals(mbean.getReadEntriesRate(), 0.0);
         assertEquals(mbean.getReadEntriesSucceeded(), 0);
         assertEquals(mbean.getReadEntriesErrors(), 0);
 
         assertEquals(mbean.getAddEntryLatencyBuckets(), new long[] { 0, 1, 0, 1, 0, 0, 0, 0, 1 });
         assertEquals(mbean.getAddEntryLatencyMin(), 1.0);
         assertEquals(mbean.getAddEntryLatencyMax(), 1000.0);
         assertEquals(mbean.getEntrySizeBuckets(), new long[] { 0, 0, 0, 0, 0, 0, 0, 0, 0 });
 
         ledger.addEntry(new byte[200]);
         ledger.addEntry(new byte[600]);
 
         mbean.refreshStats();
 
         assertTrue(mbean.getAddEntryBytesRate() > 0.0);
         assertTrue(mbean.getAddEntryMessagesRate() > 0.0);
         assertEquals(mbean.getAddEntrySucceed(), 2);
         assertEquals(mbean.getAddEntryErrors(), 0);
         assertEquals(mbean.getReadEntriesBytesRate(), 0.0);
         assertEquals(mbean.getReadEntriesRate(), 0.0);
         assertEquals(mbean.getReadEntriesSucceeded(), 0);
         assertEquals(mbean.getReadEntriesErrors(), 0);
 
         System.out.println(Arrays.toString(mbean.getEntrySizeBuckets()));
         assertEquals(mbean.getEntrySizeBuckets(), new long[] { 0, 1, 1, 0, 0, 0, 0, 0, 0 });
 
         assertEquals(mbean.getEntrySizeMin(), 200.0);
         assertEquals(mbean.getEntrySizeMax(), 600.0);
 
         mbean.recordAddEntryError();
         mbean.recordReadEntriesError();
 
         mbean.refreshStats();
 
         assertEquals(mbean.getAddEntryErrors(), 1);
         assertEquals(mbean.getReadEntriesErrors(), 1);
 
         List<Entry> entries = cursor.readEntries(100);
         assertEquals(entries.size(), 2);
 
         mbean.refreshStats();
 
         assertTrue(mbean.getReadEntriesBytesRate() > 0.0);
         assertTrue(mbean.getReadEntriesRate() > 0.0);
         assertEquals(mbean.getReadEntriesSucceeded(), 1);
         assertEquals(mbean.getReadEntriesErrors(), 0);
         assertEquals(mbean.getNumberOfMessagesInBacklog(), 0);
     }
 
 }
