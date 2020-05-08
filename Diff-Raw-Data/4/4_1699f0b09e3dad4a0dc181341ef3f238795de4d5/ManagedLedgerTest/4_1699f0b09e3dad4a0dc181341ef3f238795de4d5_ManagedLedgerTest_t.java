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
 import static org.testng.Assert.assertNotNull;
 import static org.testng.Assert.assertNull;
 import static org.testng.Assert.assertTrue;
 import static org.testng.Assert.fail;
 
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.RejectedExecutionException;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import org.apache.bookkeeper.mledger.AsyncCallbacks.AddEntryCallback;
 import org.apache.bookkeeper.mledger.AsyncCallbacks.CloseCallback;
 import org.apache.bookkeeper.mledger.AsyncCallbacks.DeleteCursorCallback;
 import org.apache.bookkeeper.mledger.AsyncCallbacks.DeleteLedgerCallback;
 import org.apache.bookkeeper.mledger.AsyncCallbacks.MarkDeleteCallback;
 import org.apache.bookkeeper.mledger.AsyncCallbacks.OpenCursorCallback;
 import org.apache.bookkeeper.mledger.AsyncCallbacks.OpenLedgerCallback;
 import org.apache.bookkeeper.mledger.AsyncCallbacks.ReadEntriesCallback;
 import org.apache.bookkeeper.mledger.Entry;
 import org.apache.bookkeeper.mledger.ManagedCursor;
 import org.apache.bookkeeper.mledger.ManagedLedger;
 import org.apache.bookkeeper.mledger.ManagedLedgerConfig;
 import org.apache.bookkeeper.mledger.ManagedLedgerException;
 import org.apache.bookkeeper.mledger.ManagedLedgerException.ManagedLedgerFencedException;
 import org.apache.bookkeeper.mledger.ManagedLedgerFactory;
 import org.apache.bookkeeper.mledger.Position;
 import org.apache.bookkeeper.mledger.util.Pair;
 import org.apache.bookkeeper.test.BookKeeperClusterTestCase;
 import org.apache.zookeeper.CreateMode;
 import org.apache.zookeeper.ZooDefs;
 import org.apache.zookeeper.ZooKeeper;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.testng.annotations.Test;
 
 import com.google.common.base.Charsets;
 import com.google.common.collect.Sets;
 
 public class ManagedLedgerTest extends BookKeeperClusterTestCase {
 
     private static final Logger log = LoggerFactory.getLogger(ManagedLedgerTest.class);
 
     private static final Charset Encoding = Charsets.UTF_8;
 
     @Test
     public void managedLedgerApi() throws Exception {
         ManagedLedgerFactory factory = new ManagedLedgerFactoryImpl(bkc, bkc.getZkHandle());
 
         ManagedLedger ledger = factory.open("my_test_ledger");
 
         ManagedCursor cursor = ledger.openCursor("c1");
 
         for (int i = 0; i < 100; i++) {
             String content = "entry-" + i;
             ledger.addEntry(content.getBytes());
         }
 
         // Reads all the entries in batches of 20
         while (cursor.hasMoreEntries()) {
 
             List<Entry> entries = cursor.readEntries(20);
             log.debug("Read {} entries", entries.size());
 
             for (Entry entry : entries) {
                 log.info("Read entry. Position={} Content='{}'", entry.getPosition(), new String(entry.getData()));
             }
 
             // Acknowledge only on last entry
             Entry lastEntry = entries.get(entries.size() - 1);
             cursor.markDelete(lastEntry.getPosition());
 
             log.info("-----------------------");
         }
 
         log.info("Finished reading entries");
 
         ledger.close();
     }
 
     @Test(timeOut = 20000)
     public void simple() throws Exception {
         ManagedLedgerFactory factory = new ManagedLedgerFactoryImpl(bkc, zkc);
 
         ManagedLedger ledger = factory.open("my_test_ledger");
 
         assertEquals(ledger.getNumberOfEntries(), 0);
         assertEquals(ledger.getTotalSize(), 0);
 
         ledger.addEntry("dummy-entry-1".getBytes(Encoding));
 
         assertEquals(ledger.getNumberOfEntries(), 1);
         assertEquals(ledger.getTotalSize(), "dummy-entry-1".getBytes(Encoding).length);
 
         ManagedCursor cursor = ledger.openCursor("c1");
 
         assertEquals(cursor.hasMoreEntries(), false);
         assertEquals(cursor.readEntries(100), new ArrayList<Entry>());
 
         ledger.addEntry("dummy-entry-2".getBytes(Encoding));
 
         assertEquals(cursor.hasMoreEntries(), true);
 
         List<Entry> entries = cursor.readEntries(100);
         assertEquals(entries.size(), 1);
 
         entries = cursor.readEntries(100);
         assertEquals(entries.size(), 0);
 
         ledger.close();
         factory.shutdown();
     }
 
     @Test(timeOut = 20000)
     public void closeAndReopen() throws Exception {
         ManagedLedgerFactory factory = new ManagedLedgerFactoryImpl(bkc, zkc);
 
         ManagedLedger ledger = factory.open("my_test_ledger");
 
         ledger.addEntry("dummy-entry-1".getBytes(Encoding));
 
         ManagedCursor cursor = ledger.openCursor("c1");
 
         ledger.addEntry("dummy-entry-2".getBytes(Encoding));
 
         ledger.close();
 
         log.info("Closing ledger and reopening");
 
         // / Reopen the same managed-ledger
         factory = new ManagedLedgerFactoryImpl(bkc, bkc.getZkHandle());
         ledger = factory.open("my_test_ledger");
 
         cursor = ledger.openCursor("c1");
 
         assertEquals(ledger.getNumberOfEntries(), 2);
         assertEquals(ledger.getTotalSize(), "dummy-entry-1".getBytes(Encoding).length * 2);
 
         List<Entry> entries = cursor.readEntries(100);
         assertEquals(entries.size(), 1);
 
         ledger.close();
     }
 
     @Test(timeOut = 20000)
     public void acknowledge1() throws Exception {
         ManagedLedgerFactory factory = new ManagedLedgerFactoryImpl(bkc, bkc.getZkHandle());
 
         ManagedLedger ledger = factory.open("my_test_ledger");
 
         ManagedCursor cursor = ledger.openCursor("c1");
 
         ledger.addEntry("dummy-entry-1".getBytes(Encoding));
         ledger.addEntry("dummy-entry-2".getBytes(Encoding));
 
         assertEquals(cursor.hasMoreEntries(), true);
 
         List<Entry> entries = cursor.readEntries(2);
         assertEquals(entries.size(), 2);
         assertEquals(cursor.hasMoreEntries(), false);
 
         cursor.markDelete(entries.get(0).getPosition());
 
         ledger.close();
 
         // / Reopen the same managed-ledger
 
         ledger = factory.open("my_test_ledger");
         cursor = ledger.openCursor("c1");
 
         assertEquals(ledger.getNumberOfEntries(), 2);
         assertEquals(ledger.getTotalSize(), "dummy-entry-1".getBytes(Encoding).length * 2);
 
         assertEquals(cursor.hasMoreEntries(), true);
 
         entries = cursor.readEntries(100);
         assertEquals(entries.size(), 1);
 
         ledger.close();
     }
 
     @Test(timeOut = 20000)
     public void asyncAPI() throws Throwable {
         ManagedLedgerFactory factory = new ManagedLedgerFactoryImpl(bkc, bkc.getZkHandle());
 
         final CountDownLatch counter = new CountDownLatch(1);
 
         factory.asyncOpen("my_test_ledger", new ManagedLedgerConfig(), new OpenLedgerCallback() {
             public void openLedgerComplete(ManagedLedger ledger, Object ctx) {
                 ledger.asyncOpenCursor("test-cursor", new OpenCursorCallback() {
                     public void openCursorComplete(ManagedCursor cursor, Object ctx) {
                         ManagedLedger ledger = (ManagedLedger) ctx;
 
                         ledger.asyncAddEntry("test".getBytes(Encoding), new AddEntryCallback() {
                             public void addComplete(Position position, Object ctx) {
                                 @SuppressWarnings("unchecked")
                                 Pair<ManagedLedger, ManagedCursor> pair = (Pair<ManagedLedger, ManagedCursor>) ctx;
                                 ManagedLedger ledger = pair.first;
                                 ManagedCursor cursor = pair.second;
 
                                 assertEquals(ledger.getNumberOfEntries(), 1);
                                 assertEquals(ledger.getTotalSize(), "test".getBytes(Encoding).length);
 
                                 cursor.asyncReadEntries(2, new ReadEntriesCallback() {
                                     public void readEntriesComplete(List<Entry> entries, Object ctx) {
                                         ManagedCursor cursor = (ManagedCursor) ctx;
 
                                         assertEquals(entries.size(), 1);
                                         Entry entry = entries.get(0);
                                         assertEquals(new String(entry.getData(), Encoding), "test");
 
                                         log.debug("Mark-Deleting to position {}", entry.getPosition());
                                         cursor.asyncMarkDelete(entry.getPosition(), new MarkDeleteCallback() {
                                             public void markDeleteComplete(Object ctx) {
                                                 log.debug("Mark delete complete");
                                                 ManagedCursor cursor = (ManagedCursor) ctx;
                                                 assertEquals(cursor.hasMoreEntries(), false);
 
                                                 counter.countDown();
                                             }
 
                                             @Override
                                             public void markDeleteFailed(ManagedLedgerException exception, Object ctx) {
                                                 fail(exception.getMessage());
                                             }
 
                                         }, cursor);
                                     }
 
                                     public void readEntriesFailed(ManagedLedgerException exception, Object ctx) {
                                         fail(exception.getMessage());
                                     }
                                 }, cursor);
                             }
 
                             @Override
                             public void addFailed(ManagedLedgerException exception, Object ctx) {
                                 fail(exception.getMessage());
                             }
                         }, new Pair<ManagedLedger, ManagedCursor>(ledger, cursor));
                     }
 
                     public void openCursorFailed(ManagedLedgerException exception, Object ctx) {
                         fail(exception.getMessage());
                     }
 
                 }, ledger);
             }
 
             public void openLedgerFailed(ManagedLedgerException exception, Object ctx) {
                 fail(exception.getMessage());
             }
         }, null);
 
         counter.await();
 
         log.info("Test completed");
     }
 
     @Test(timeOut = 20000)
     public void spanningMultipleLedgers() throws Exception {
         ManagedLedgerFactory factory = new ManagedLedgerFactoryImpl(bkc, bkc.getZkHandle());
         ManagedLedgerConfig config = new ManagedLedgerConfig().setMaxEntriesPerLedger(10);
         ManagedLedger ledger = factory.open("my_test_ledger", config);
 
         assertEquals(ledger.getNumberOfEntries(), 0);
         assertEquals(ledger.getTotalSize(), 0);
 
         ManagedCursor cursor = ledger.openCursor("c1");
 
         for (int i = 0; i < 11; i++)
             ledger.addEntry(("dummy-entry-" + i).getBytes(Encoding));
 
         List<Entry> entries = cursor.readEntries(100);
         assertEquals(entries.size(), 10);
         assertEquals(cursor.hasMoreEntries(), true);
 
         PositionImpl first = (PositionImpl) entries.get(0).getPosition();
 
         // Read again, from next ledger id
         entries = cursor.readEntries(100);
         assertEquals(entries.size(), 1);
         assertEquals(cursor.hasMoreEntries(), false);
 
         PositionImpl last = (PositionImpl) entries.get(0).getPosition();
 
         log.info("First={} Last={}", first, last);
         assertTrue(first.getLedgerId() < last.getLedgerId());
         assertEquals(first.getEntryId(), 0);
         assertEquals(last.getEntryId(), 0);
         ledger.close();
     }
 
     @Test(timeOut = 20000)
     public void spanningMultipleLedgersWithSize() throws Exception {
         ManagedLedgerFactory factory = new ManagedLedgerFactoryImpl(bkc, zkc);
         ManagedLedgerConfig config = new ManagedLedgerConfig().setMaxEntriesPerLedger(1000000);
         config.setMaxSizePerLedgerMb(1);
         config.setEnsembleSize(1);
         config.setWriteQuorumSize(1).setAckQuorumSize(1);
         config.setMetadataWriteQuorumSize(1).setMetadataAckQuorumSize(1);
         ManagedLedger ledger = factory.open("my_test_ledger", config);
 
         assertEquals(ledger.getNumberOfEntries(), 0);
         assertEquals(ledger.getTotalSize(), 0);
 
         ManagedCursor cursor = ledger.openCursor("c1");
 
         byte[] content = new byte[1023 * 1024];
 
         for (int i = 0; i < 3; i++)
             ledger.addEntry(content);
 
         List<Entry> entries = cursor.readEntries(100);
         assertEquals(entries.size(), 2);
         assertEquals(cursor.hasMoreEntries(), true);
 
         PositionImpl first = (PositionImpl) entries.get(0).getPosition();
 
         // Read again, from next ledger id
         entries = cursor.readEntries(100);
         assertEquals(entries.size(), 1);
         assertEquals(cursor.hasMoreEntries(), false);
 
         PositionImpl last = (PositionImpl) entries.get(0).getPosition();
 
         log.info("First={} Last={}", first, last);
         assertTrue(first.getLedgerId() < last.getLedgerId());
         assertEquals(first.getEntryId(), 0);
         assertEquals(last.getEntryId(), 0);
         ledger.close();
     }
 
     @Test(expectedExceptions = IllegalArgumentException.class)
     public void invalidReadEntriesArg1() throws Exception {
         ManagedLedgerFactory factory = new ManagedLedgerFactoryImpl(bkc, bkc.getZkHandle());
         ManagedLedger ledger = factory.open("my_test_ledger");
         ManagedCursor cursor = ledger.openCursor("c1");
 
         ledger.addEntry("entry".getBytes());
         cursor.readEntries(-1);
 
         fail("Should have thrown an exception in the above line");
     }
 
     @Test(expectedExceptions = IllegalArgumentException.class)
     public void invalidReadEntriesArg2() throws Exception {
         ManagedLedgerFactory factory = new ManagedLedgerFactoryImpl(bkc, bkc.getZkHandle());
         ManagedLedger ledger = factory.open("my_test_ledger");
         ManagedCursor cursor = ledger.openCursor("c1");
 
         ledger.addEntry("entry".getBytes());
         cursor.readEntries(0);
 
         fail("Should have thrown an exception in the above line");
     }
 
     @Test(timeOut = 20000)
     public void deleteAndReopen() throws Exception {
         ManagedLedgerFactory factory = new ManagedLedgerFactoryImpl(bkc, bkc.getZkHandle());
 
         ManagedLedger ledger = factory.open("my_test_ledger");
 
         ledger.addEntry("dummy-entry-1".getBytes(Encoding));
         assertEquals(ledger.getNumberOfEntries(), 1);
         ledger.close();
 
         // Reopen
         ledger = factory.open("my_test_ledger");
         assertEquals(ledger.getNumberOfEntries(), 1);
         ledger.close();
 
         // Delete and reopen
         factory.delete("my_test_ledger");
         ledger = factory.open("my_test_ledger");
         assertEquals(ledger.getNumberOfEntries(), 0);
         ledger.close();
     }
 
     @Test(timeOut = 20000)
     public void deleteAndReopenWithCursors() throws Exception {
         ManagedLedgerFactory factory = new ManagedLedgerFactoryImpl(bkc, bkc.getZkHandle());
 
         ManagedLedger ledger = factory.open("my_test_ledger");
         ledger.openCursor("test-cursor");
 
         ledger.addEntry("dummy-entry-1".getBytes(Encoding));
         assertEquals(ledger.getNumberOfEntries(), 1);
         ledger.close();
 
         // Reopen
         ledger = factory.open("my_test_ledger");
         assertEquals(ledger.getNumberOfEntries(), 1);
         ledger.close();
 
         // Delete and reopen
         factory.delete("my_test_ledger");
         ledger = factory.open("my_test_ledger");
         assertEquals(ledger.getNumberOfEntries(), 0);
         ManagedCursor cursor = ledger.openCursor("test-cursor");
         assertEquals(cursor.hasMoreEntries(), false);
         ledger.close();
     }
 
     @Test(timeOut = 20000)
     public void asyncDeleteWithError() throws Exception {
         ManagedLedgerFactory factory = new ManagedLedgerFactoryImpl(bkc, bkc.getZkHandle());
 
         ManagedLedger ledger = factory.open("my_test_ledger");
         ledger.openCursor("test-cursor");
 
         ledger.addEntry("dummy-entry-1".getBytes(Encoding));
         assertEquals(ledger.getNumberOfEntries(), 1);
         ledger.close();
 
         // Reopen
         ledger = factory.open("my_test_ledger");
         assertEquals(ledger.getNumberOfEntries(), 1);
         ledger.close();
 
         final CountDownLatch counter = new CountDownLatch(1);
         stopBookKeeper();
         stopZooKeeper();
 
         // Delete and reopen
         factory.asyncDelete("my_test_ledger", new DeleteLedgerCallback() {
 
             public void deleteLedgerComplete(Object ctx) {
                 assertNull(ctx);
                 fail("The async-call should have failed");
             }
 
             public void deleteLedgerFailed(ManagedLedgerException exception, Object ctx) {
                 counter.countDown();
             }
 
         }, null);
 
         counter.await();
     }
 
     @Test(timeOut = 20000)
     public void asyncAddEntryWithoutError() throws Exception {
         ManagedLedgerFactory factory = new ManagedLedgerFactoryImpl(bkc, bkc.getZkHandle());
 
         ManagedLedger ledger = factory.open("my_test_ledger");
         ledger.openCursor("test-cursor");
 
         final CountDownLatch counter = new CountDownLatch(1);
 
         ledger.asyncAddEntry("dummy-entry-1".getBytes(Encoding), new AddEntryCallback() {
             public void addComplete(Position position, Object ctx) {
                 assertNull(ctx);
 
                 counter.countDown();
             }
 
             public void addFailed(ManagedLedgerException exception, Object ctx) {
                 fail(exception.getMessage());
             }
 
         }, null);
 
         counter.await();
         assertEquals(ledger.getNumberOfEntries(), 1);
         assertEquals(ledger.getTotalSize(), "dummy-entry-1".getBytes(Encoding).length);
     }
 
     @Test(timeOut = 20000)
     public void doubleAsyncAddEntryWithoutError() throws Exception {
         ManagedLedgerFactory factory = new ManagedLedgerFactoryImpl(bkc, bkc.getZkHandle());
 
         ManagedLedger ledger = factory.open("my_test_ledger");
         ledger.openCursor("test-cursor");
 
         final CountDownLatch done = new CountDownLatch(10);
 
         for (int i = 0; i < 10; i++) {
             final String content = "dummy-entry-" + i;
             ledger.asyncAddEntry(content.getBytes(Encoding), new AddEntryCallback() {
                 public void addComplete(Position position, Object ctx) {
                     assertNotNull(ctx);
 
                     log.info("Successfully added {}", content);
                     done.countDown();
                 }
 
                 public void addFailed(ManagedLedgerException exception, Object ctx) {
                     fail(exception.getMessage());
                 }
 
             }, this);
         }
 
         done.await();
         assertEquals(ledger.getNumberOfEntries(), 10);
     }
 
     @Test(timeOut = 20000)
     public void asyncAddEntryWithError() throws Exception {
         ManagedLedgerFactory factory = new ManagedLedgerFactoryImpl(bkc, bkc.getZkHandle());
 
         ManagedLedger ledger = factory.open("my_test_ledger");
         ledger.openCursor("test-cursor");
 
         final CountDownLatch counter = new CountDownLatch(1);
         stopBookKeeper();
         stopZooKeeper();
 
         ledger.asyncAddEntry("dummy-entry-1".getBytes(Encoding), new AddEntryCallback() {
             public void addComplete(Position position, Object ctx) {
                 fail("Should have failed");
             }
 
             public void addFailed(ManagedLedgerException exception, Object ctx) {
                 counter.countDown();
             }
 
         }, null);
 
         counter.await();
     }
 
     @Test(timeOut = 20000)
     public void asyncCloseWithoutError() throws Exception {
         ManagedLedgerFactory factory = new ManagedLedgerFactoryImpl(bkc, bkc.getZkHandle());
 
         ManagedLedger ledger = factory.open("my_test_ledger");
         ledger.openCursor("test-cursor");
         ledger.addEntry("dummy-entry-1".getBytes(Encoding));
 
         final CyclicBarrier barrier = new CyclicBarrier(2);
 
         ledger.asyncClose(new CloseCallback() {
             public void closeComplete(Object ctx) {
                 assertNull(ctx);
 
                 try {
                     barrier.await();
                 } catch (Exception e) {
                     fail("Received exception ", e);
                 }
             }
 
             public void closeFailed(ManagedLedgerException exception, Object ctx) {
                 fail(exception.getMessage());
             }
 
         }, null);
 
         barrier.await();
     }
 
     @Test(timeOut = 20000)
     public void asyncOpenCursorWithoutError() throws Exception {
         ManagedLedgerFactory factory = new ManagedLedgerFactoryImpl(bkc, bkc.getZkHandle());
 
         ManagedLedger ledger = factory.open("my_test_ledger");
 
         final CountDownLatch counter = new CountDownLatch(1);
 
         ledger.asyncOpenCursor("test-cursor", new OpenCursorCallback() {
             public void openCursorComplete(ManagedCursor cursor, Object ctx) {
                 assertNull(ctx);
                 assertNotNull(cursor);
 
                 counter.countDown();
             }
 
             @Override
             public void openCursorFailed(ManagedLedgerException exception, Object ctx) {
                 fail(exception.getMessage());
             }
 
         }, null);
 
         counter.await();
     }
 
     @Test(timeOut = 20000)
     public void asyncOpenCursorWithError() throws Exception {
         ManagedLedgerFactory factory = new ManagedLedgerFactoryImpl(bkc, bkc.getZkHandle());
 
         ManagedLedger ledger = factory.open("my_test_ledger");
 
         final CountDownLatch counter = new CountDownLatch(1);
 
         stopBookKeeper();
         stopZooKeeper();
 
         ledger.asyncOpenCursor("test-cursor", new OpenCursorCallback() {
             public void openCursorComplete(ManagedCursor cursor, Object ctx) {
                 fail("The async-call should have failed");
             }
 
             public void openCursorFailed(ManagedLedgerException exception, Object ctx) {
                 counter.countDown();
             }
         }, null);
 
         counter.await();
     }
 
     @Test(timeOut = 20000)
     public void readFromOlderLedger() throws Exception {
         ManagedLedgerFactory factory = new ManagedLedgerFactoryImpl(bkc, bkc.getZkHandle());
         ManagedLedgerConfig config = new ManagedLedgerConfig().setMaxEntriesPerLedger(1);
         ManagedLedger ledger = factory.open("my_test_ledger", config);
         ManagedCursor cursor = ledger.openCursor("test");
 
         ledger.addEntry("entry-1".getBytes(Encoding));
         ledger.addEntry("entry-2".getBytes(Encoding));
 
         assertEquals(cursor.hasMoreEntries(), true);
     }
 
     @Test(timeOut = 20000)
     public void readFromOlderLedgers() throws Exception {
         ManagedLedgerFactory factory = new ManagedLedgerFactoryImpl(bkc, bkc.getZkHandle());
         ManagedLedgerConfig config = new ManagedLedgerConfig().setMaxEntriesPerLedger(1);
         ManagedLedger ledger = factory.open("my_test_ledger", config);
         ManagedCursor cursor = ledger.openCursor("test");
 
         ledger.addEntry("entry-1".getBytes(Encoding));
         ledger.addEntry("entry-2".getBytes(Encoding));
         ledger.addEntry("entry-3".getBytes(Encoding));
 
         assertEquals(cursor.hasMoreEntries(), true);
         cursor.readEntries(1);
         assertEquals(cursor.hasMoreEntries(), true);
         cursor.readEntries(1);
         assertEquals(cursor.hasMoreEntries(), true);
         cursor.readEntries(1);
         assertEquals(cursor.hasMoreEntries(), false);
     }
 
     @Test(timeOut = 20000)
     public void triggerLedgerDeletion() throws Exception {
         ManagedLedgerFactory factory = new ManagedLedgerFactoryImpl(bkc, bkc.getZkHandle());
         ManagedLedgerConfig config = new ManagedLedgerConfig().setMaxEntriesPerLedger(1);
         ManagedLedger ledger = factory.open("my_test_ledger", config);
         ManagedCursor cursor = ledger.openCursor("test");
 
         ledger.addEntry("entry-1".getBytes(Encoding));
         ledger.addEntry("entry-2".getBytes(Encoding));
         ledger.addEntry("entry-3".getBytes(Encoding));
 
         assertEquals(cursor.hasMoreEntries(), true);
         List<Entry> entries = cursor.readEntries(1);
         assertEquals(entries.size(), 1);
         assertEquals(ledger.getNumberOfEntries(), 3);
 
         assertEquals(cursor.hasMoreEntries(), true);
         entries = cursor.readEntries(1);
         assertEquals(cursor.hasMoreEntries(), true);
 
         cursor.markDelete(entries.get(0).getPosition());
     }
 
     @Test(timeOut = 20000)
     public void testEmptyManagedLedgerContent() throws Exception {
         ZooKeeper zk = bkc.getZkHandle();
         zk.create("/managed-ledger", new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
         zk.create("/managed-ledger/my_test_ledger", " ".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
 
         ManagedLedgerFactory factory = new ManagedLedgerFactoryImpl(bkc, bkc.getZkHandle());
         ManagedLedger ledger = factory.open("my_test_ledger");
         ledger.openCursor("test");
 
         ledger.addEntry("entry-1".getBytes(Encoding));
         assertEquals(ledger.getNumberOfEntries(), 1);
     }
 
     @Test(timeOut = 20000)
     public void testProducerAndNoConsumer() throws Exception {
         ManagedLedgerFactory factory = new ManagedLedgerFactoryImpl(bkc, bkc.getZkHandle());
         ManagedLedgerConfig config = new ManagedLedgerConfig().setMaxEntriesPerLedger(1);
         ManagedLedger ledger = factory.open("my_test_ledger", config);
 
         assertEquals(ledger.getNumberOfEntries(), 0);
 
         ledger.addEntry("entry-1".getBytes(Encoding));
         assertEquals(ledger.getNumberOfEntries(), 1);
 
         // Since there are no consumers, older ledger will be deleted
         // in a short time (in a background thread)
         ledger.addEntry("entry-2".getBytes(Encoding));
         while (ledger.getNumberOfEntries() > 1) {
             log.debug("entries={}", ledger.getNumberOfEntries());
             Thread.sleep(100);
         }
 
         ledger.addEntry("entry-3".getBytes(Encoding));
         while (ledger.getNumberOfEntries() > 1) {
             log.debug("entries={}", ledger.getNumberOfEntries());
             Thread.sleep(100);
         }
     }
 
     @Test(timeOut = 20000)
     public void testTrimmer() throws Exception {
         ManagedLedgerFactory factory = new ManagedLedgerFactoryImpl(bkc, bkc.getZkHandle());
         ManagedLedgerConfig config = new ManagedLedgerConfig().setMaxEntriesPerLedger(1);
         ManagedLedger ledger = factory.open("my_test_ledger", config);
         ManagedCursor cursor = ledger.openCursor("c1");
 
         assertEquals(ledger.getNumberOfEntries(), 0);
 
         ledger.addEntry("entry-1".getBytes(Encoding));
         ledger.addEntry("entry-2".getBytes(Encoding));
         ledger.addEntry("entry-3".getBytes(Encoding));
         ledger.addEntry("entry-4".getBytes(Encoding));
         assertEquals(ledger.getNumberOfEntries(), 4);
 
         cursor.readEntries(1);
         cursor.readEntries(1);
         Position lastPosition = cursor.readEntries(1).get(0).getPosition();
 
         assertEquals(ledger.getNumberOfEntries(), 4);
 
         cursor.markDelete(lastPosition);
 
         while (ledger.getNumberOfEntries() != 2) {
             Thread.sleep(10);
         }
     }
 
     @Test(timeOut = 20000)
     public void testAsyncAddEntryAndSyncClose() throws Exception {
         ManagedLedgerFactory factory = new ManagedLedgerFactoryImpl(bkc, bkc.getZkHandle());
         ManagedLedgerConfig config = new ManagedLedgerConfig().setMaxEntriesPerLedger(10);
         ManagedLedger ledger = factory.open("my_test_ledger", config);
         ledger.openCursor("c1");
 
         assertEquals(ledger.getNumberOfEntries(), 0);
 
         final CountDownLatch counter = new CountDownLatch(100);
 
         for (int i = 0; i < 100; i++) {
             String content = "entry-" + i;
             ledger.asyncAddEntry(content.getBytes(Encoding), new AddEntryCallback() {
                 public void addComplete(Position position, Object ctx) {
                     counter.countDown();
                 }
 
                 public void addFailed(ManagedLedgerException exception, Object ctx) {
                     fail(exception.getMessage());
                 }
 
             }, null);
         }
 
         counter.await();
 
         assertEquals(ledger.getNumberOfEntries(), 100);
     }
 
     @Test(timeOut = 20000)
     public void moveCursorToNextLedger() throws Exception {
         ManagedLedgerFactory factory = new ManagedLedgerFactoryImpl(bkc, bkc.getZkHandle());
         ManagedLedgerConfig config = new ManagedLedgerConfig().setMaxEntriesPerLedger(1);
         ManagedLedger ledger = factory.open("my_test_ledger", config);
         ManagedCursor cursor = ledger.openCursor("test");
 
         ledger.addEntry("entry-1".getBytes(Encoding));
         log.debug("Added 1st message");
         List<Entry> entries = cursor.readEntries(1);
         log.debug("read message ok");
         assertEquals(entries.size(), 1);
 
         ledger.addEntry("entry-2".getBytes(Encoding));
         log.debug("Added 2nd message");
         ledger.addEntry("entry-3".getBytes(Encoding));
         log.debug("Added 3nd message");
 
         assertEquals(cursor.hasMoreEntries(), true);
         assertEquals(cursor.getNumberOfEntries(), 2);
 
         entries = cursor.readEntries(2);
         assertEquals(entries.size(), 0);
 
         entries = cursor.readEntries(2);
         assertEquals(entries.size(), 1);
 
         entries = cursor.readEntries(2);
         assertEquals(entries.size(), 1);
 
         entries = cursor.readEntries(2);
         assertEquals(entries.size(), 0);
     }
 
     @Test(timeOut = 20000)
     public void differentSessions() throws Exception {
         ManagedLedgerFactory factory = new ManagedLedgerFactoryImpl(bkc, zkc);
 
         ManagedLedger ledger = factory.open("my_test_ledger");
 
         assertEquals(ledger.getNumberOfEntries(), 0);
         assertEquals(ledger.getTotalSize(), 0);
 
         ManagedCursor cursor = ledger.openCursor("c1");
 
         ledger.addEntry("dummy-entry-1".getBytes(Encoding));
 
         assertEquals(ledger.getNumberOfEntries(), 1);
         assertEquals(ledger.getTotalSize(), "dummy-entry-1".getBytes(Encoding).length);
 
         assertEquals(cursor.hasMoreEntries(), true);
         assertEquals(cursor.getNumberOfEntries(), 1);
 
         ledger.close();
 
         // Create a new factory and re-open the same managed ledger
         factory = new ManagedLedgerFactoryImpl(bkc, zkc);
 
         ledger = factory.open("my_test_ledger");
 
         assertEquals(ledger.getNumberOfEntries(), 1);
         assertEquals(ledger.getTotalSize(), "dummy-entry-1".getBytes(Encoding).length);
 
         cursor = ledger.openCursor("c1");
 
         assertEquals(cursor.hasMoreEntries(), true);
         assertEquals(cursor.getNumberOfEntries(), 1);
 
         ledger.addEntry("dummy-entry-2".getBytes(Encoding));
 
         assertEquals(ledger.getNumberOfEntries(), 2);
         assertEquals(ledger.getTotalSize(), "dummy-entry-1".getBytes(Encoding).length * 2);
 
         assertEquals(cursor.hasMoreEntries(), true);
         assertEquals(cursor.getNumberOfEntries(), 2);
 
         ledger.close();
     }
 
     @Test
     public void fenceManagedLedger() throws Exception {
         ManagedLedgerFactory factory1 = new ManagedLedgerFactoryImpl(bkc, bkc.getZkHandle());
         ManagedLedger ledger1 = factory1.open("my_test_ledger");
         ManagedCursor cursor1 = ledger1.openCursor("c1");
         ledger1.addEntry("entry-1".getBytes(Encoding));
 
         ManagedLedgerFactory factory2 = new ManagedLedgerFactoryImpl(bkc, bkc.getZkHandle());
         ManagedLedger ledger2 = factory2.open("my_test_ledger");
         ManagedCursor cursor2 = ledger2.openCursor("c1");
 
         // At this point ledger1 must have been fenced
         try {
             ledger1.addEntry("entry-1".getBytes(Encoding));
             fail("Expecting exception");
         } catch (ManagedLedgerFencedException e) {
         }
 
         try {
             ledger1.addEntry("entry-2".getBytes(Encoding));
             fail("Expecting exception");
         } catch (ManagedLedgerFencedException e) {
         }
 
         try {
             cursor1.readEntries(10);
             fail("Expecting exception");
         } catch (ManagedLedgerFencedException e) {
         }
 
         try {
             ledger1.openCursor("new cursor");
             fail("Expecting exception");
         } catch (ManagedLedgerFencedException e) {
         }
 
         ledger2.addEntry("entry-2".getBytes(Encoding));
 
         assertEquals(cursor2.getNumberOfEntries(), 2);
     }
 
     @Test
     public void forceCloseLedgers() throws Exception {
         ManagedLedgerFactory factory1 = new ManagedLedgerFactoryImpl(bkc, bkc.getZkHandle());
         ManagedLedger ledger1 = factory1.open("my_test_ledger", new ManagedLedgerConfig().setMaxEntriesPerLedger(1));
         ledger1.openCursor("c1");
         ManagedCursor c2 = ledger1.openCursor("c2");
         ledger1.addEntry("entry-1".getBytes(Encoding));
         ledger1.addEntry("entry-2".getBytes(Encoding));
         ledger1.addEntry("entry-3".getBytes(Encoding));
 
         c2.readEntries(1);
         c2.readEntries(1);
         c2.readEntries(1);
 
         ledger1.close();
 
         try {
             ledger1.addEntry("entry-3".getBytes(Encoding));
             fail("should not have reached this point");
         } catch (ManagedLedgerException e) {
             // ok
         }
 
         try {
             ledger1.openCursor("new-cursor");
             fail("should not have reached this point");
         } catch (ManagedLedgerException e) {
             // ok
         }
     }
 
     @Test
     public void closeLedgerWithError() throws Exception {
         ManagedLedgerFactory factory = new ManagedLedgerFactoryImpl(bkc, bkc.getZkHandle());
         ManagedLedger ledger = factory.open("my_test_ledger");
         ledger.addEntry("entry-1".getBytes(Encoding));
 
         stopZooKeeper();
         stopBookKeeper();
 
         try {
             ledger.close();
             // fail("should have thrown exception");
         } catch (ManagedLedgerException e) {
             // Ok
         }
     }
 
     @Test(timeOut = 20000)
     public void deleteWithErrors1() throws Exception {
         ManagedLedgerFactory factory = new ManagedLedgerFactoryImpl(bkc, bkc.getZkHandle());
 
         ManagedLedger ledger = factory.open("my_test_ledger");
 
         PositionImpl position = (PositionImpl) ledger.addEntry("dummy-entry-1".getBytes(Encoding));
         assertEquals(ledger.getNumberOfEntries(), 1);
         ledger.close();
 
         // Force delete a ledger and test that deleting the ML still happens
         // without errors
         bkc.deleteLedger(position.getLedgerId());
         factory.delete("my_test_ledger");
     }
 
     @Test(timeOut = 20000)
     public void deleteWithErrors2() throws Exception {
         ManagedLedgerFactory factory = new ManagedLedgerFactoryImpl(bkc, bkc.getZkHandle());
 
         ManagedLedger ledger = factory.open("my_test_ledger");
         ledger.addEntry("dummy-entry-1".getBytes(Encoding));
         ledger.close();
 
         stopZooKeeper();
 
         try {
             factory.delete("my_test_ledger");
             fail("should have failed");
         } catch (ManagedLedgerException e) {
             // ok
        } catch (RejectedExecutionException e) {
            // ok
         }
     }
 
     @Test(timeOut = 20000)
     public void readWithErrors1() throws Exception {
         ManagedLedgerFactory factory = new ManagedLedgerFactoryImpl(bkc, bkc.getZkHandle());
 
         ManagedLedger ledger = factory.open("my_test_ledger", new ManagedLedgerConfig().setMaxEntriesPerLedger(1));
         ManagedCursor cursor = ledger.openCursor("c1");
         ledger.addEntry("dummy-entry-1".getBytes(Encoding));
         ledger.addEntry("dummy-entry-2".getBytes(Encoding));
 
         stopZooKeeper();
         stopBookKeeper();
 
         try {
             cursor.readEntries(10);
             fail("should have failed");
         } catch (ManagedLedgerException e) {
             // ok
         }
 
         try {
             ledger.addEntry("dummy-entry-3".getBytes(Encoding));
             fail("should have failed");
         } catch (ManagedLedgerException e) {
             // ok
         }
     }
 
     @Test(timeOut = 20000, enabled = false)
     void concurrentAsyncOpen() throws Exception {
         ManagedLedgerFactory factory = new ManagedLedgerFactoryImpl(bkc, bkc.getZkHandle());
 
         final CountDownLatch counter = new CountDownLatch(2);
 
         class Result {
             ManagedLedger instance1 = null;
             ManagedLedger instance2 = null;
         }
 
         final Result result = new Result();
         factory.asyncOpen("my-test-ledger", new OpenLedgerCallback() {
 
             public void openLedgerComplete(ManagedLedger ledger, Object ctx) {
                 result.instance1 = ledger;
                 counter.countDown();
             }
 
             public void openLedgerFailed(ManagedLedgerException exception, Object ctx) {
             }
         }, null);
 
         factory.asyncOpen("my-test-ledger", new OpenLedgerCallback() {
 
             public void openLedgerComplete(ManagedLedger ledger, Object ctx) {
                 result.instance2 = ledger;
                 counter.countDown();
             }
 
             public void openLedgerFailed(ManagedLedgerException exception, Object ctx) {
             }
         }, null);
 
         counter.await();
         assertEquals(result.instance1, result.instance2);
         assertNotNull(result.instance1);
     }
 
     @Test
     public void getCursors() throws Exception {
         ManagedLedgerFactoryImpl factory = new ManagedLedgerFactoryImpl(bkc, bkc.getZkHandle());
         ManagedLedger ledger = factory.open("my_test_ledger");
         ManagedCursor c1 = ledger.openCursor("c1");
         ManagedCursor c2 = ledger.openCursor("c2");
 
         assertEquals(Sets.newHashSet(ledger.getCursors()), Sets.newHashSet(c1, c2));
 
         c1.close();
         ledger.deleteCursor("c1");
         assertEquals(Sets.newHashSet(ledger.getCursors()), Sets.newHashSet(c2));
 
         c2.close();
         ledger.deleteCursor("c2");
         assertEquals(Sets.newHashSet(ledger.getCursors()), Sets.newHashSet());
     }
 
     @Test
     public void openReadOnly() throws Exception {
         ManagedLedgerFactoryImpl factory = new ManagedLedgerFactoryImpl(bkc, bkc.getZkHandle());
         ManagedLedger ledger = factory.open("my_test_ledger");
         ledger.openCursor("c1");
 
         ledger.addEntry("data".getBytes(Encoding));
 
         ManagedLedger readOnlyLedger = factory.openReadOnly("my_test_ledger");
         assertEquals(readOnlyLedger.getNumberOfEntries(), 1);
 
         // Try to publish again to prove the previous handle is still valid
         // ledger.addEntry("data".getBytes(Encoding));
         // assertEquals(readOnlyLedger.getNumberOfEntries(), 2);
 
         try {
             readOnlyLedger.addEntry("data".getBytes(Encoding));
             fail("should have failed");
         } catch (ManagedLedgerException e) {
             // Ok
         }
 
         final AtomicBoolean operationFailed = new AtomicBoolean(false);
 
         readOnlyLedger.asyncAddEntry("data".getBytes(Encoding), new AddEntryCallback() {
             public void addFailed(ManagedLedgerException exception, Object ctx) {
                 operationFailed.set(true);
             }
 
             public void addComplete(Position position, Object ctx) {
                 operationFailed.set(false);
             }
         }, null);
 
         assertEquals(operationFailed.get(), true);
 
         readOnlyLedger.asyncOpenCursor("c2", new OpenCursorCallback() {
             public void openCursorFailed(ManagedLedgerException exception, Object ctx) {
                 operationFailed.set(true);
             }
 
             public void openCursorComplete(ManagedCursor cursor, Object ctx) {
                 operationFailed.set(false);
             }
         }, null);
 
         assertEquals(operationFailed.get(), true);
 
         readOnlyLedger.asyncDeleteCursor("c2", new DeleteCursorCallback() {
             public void deleteCursorFailed(ManagedLedgerException exception, Object ctx) {
                 operationFailed.set(true);
             }
 
             public void deleteCursorComplete(Object ctx) {
                 operationFailed.set(false);
             }
         }, null);
 
         assertEquals(operationFailed.get(), true);
 
         try {
             readOnlyLedger.openCursor("c2");
             fail("should have failed");
         } catch (ManagedLedgerException e) {
             // Ok
         }
 
         try {
             readOnlyLedger.deleteCursor("c2");
             fail("should have failed");
         } catch (ManagedLedgerException e) {
             // Ok
         }
 
         readOnlyLedger.close();
         ledger.close();
     }
 
     @Test
     public void ledgersList() throws Exception {
         ManagedLedgerFactoryImpl factory = new ManagedLedgerFactoryImpl(bkc, bkc.getZkHandle());
         MetaStore store = factory.getMetaStore();
 
         assertEquals(Sets.newHashSet(store.getManagedLedgers()), Sets.newHashSet());
         factory.open("ledger1");
         assertEquals(Sets.newHashSet(store.getManagedLedgers()), Sets.newHashSet("ledger1"));
         factory.open("ledger2");
         assertEquals(Sets.newHashSet(store.getManagedLedgers()), Sets.newHashSet("ledger1", "ledger2"));
         factory.delete("ledger1");
         assertEquals(Sets.newHashSet(store.getManagedLedgers()), Sets.newHashSet("ledger2"));
         factory.delete("ledger2");
         assertEquals(Sets.newHashSet(store.getManagedLedgers()), Sets.newHashSet());
     }
 }
