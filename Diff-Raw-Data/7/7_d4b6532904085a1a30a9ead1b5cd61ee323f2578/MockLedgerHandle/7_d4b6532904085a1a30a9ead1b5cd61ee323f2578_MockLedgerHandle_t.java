 package org.apache.bookkeeper.client;
 
 import static org.apache.bookkeeper.mledger.util.VarArgs.va;
 
 import java.security.GeneralSecurityException;
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.Queue;
 
 import org.apache.bookkeeper.client.AsyncCallback.AddCallback;
 import org.apache.bookkeeper.client.AsyncCallback.CloseCallback;
 import org.apache.bookkeeper.client.AsyncCallback.ReadCallback;
 import org.apache.bookkeeper.client.BookKeeper.DigestType;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.inject.internal.Lists;
 
 public class MockLedgerHandle extends LedgerHandle {
 
     final ArrayList<LedgerEntry> entries = Lists.newArrayList();
     final MockBookKeeper bk;
     final long id;
     long lastEntry = -1;
     boolean fenced = false;
 
     MockLedgerHandle(MockBookKeeper bk, long id) throws GeneralSecurityException {
        super(bk, id, new LedgerMetadata(3, 3, 2, DigestType.MAC, "".getBytes()), DigestType.MAC, "".getBytes());
         this.bk = bk;
         this.id = id;
     }
 
     @Override
     public void asyncClose(CloseCallback cb, Object ctx) {
         fenced = true;
         cb.closeComplete(0, this, ctx);
     }
 
     @Override
     public void asyncReadEntries(long firstEntry, long lastEntry, ReadCallback cb, Object ctx) {
         if (bk.isStopped()) {
             log.debug("Bookkeeper is closed!");
             cb.readComplete(-1, this, null, ctx);
             return;
         }
 
         log.debug("readEntries: first={} last={} total={}", va(firstEntry, lastEntry, entries.size()));
         final Queue<LedgerEntry> seq = new ArrayDeque<LedgerEntry>();
         long entryId = firstEntry;
         while (entryId <= lastEntry && entryId < entries.size()) {
             seq.add(entries.get((int) entryId++));
         }
 
         log.debug("Entries read: {}", seq);
 
         cb.readComplete(0, this, new Enumeration<LedgerEntry>() {
             public boolean hasMoreElements() {
                 return !seq.isEmpty();
             }
 
             public LedgerEntry nextElement() {
                 return seq.remove();
             }
 
         }, ctx);
     }
 
     @Override
    public long addEntry(byte[] data) throws InterruptedException, BKException {
         if (fenced) {
             throw BKException.create(BKException.Code.LedgerFencedException);
         }
 
         if (bk.isStopped()) {
             throw BKException.create(BKException.Code.NoBookieAvailableException);
         }
 
         lastEntry = entries.size();
         entries.add(new MockLedgerEntry(ledgerId, lastEntry, data));
        return lastEntry;
     }
 
     @Override
     public void asyncAddEntry(byte[] data, AddCallback cb, Object ctx) {
         if (bk.isStopped()) {
             cb.addComplete(-1, this, LedgerHandle.INVALID_ENTRY_ID, ctx);
             return;
         }
 
         if (fenced) {
             cb.addComplete(BKException.Code.LedgerFencedException, this, LedgerHandle.INVALID_ENTRY_ID, ctx);
         } else {
             lastEntry = entries.size();
             LedgerEntry entry = new MockLedgerEntry(ledgerId, lastEntry, data);
             entries.add(entry);
             cb.addComplete(0, this, lastEntry, ctx);
         }
     }
 
     @Override
     public long getId() {
         return ledgerId;
     }
 
     @Override
     public long getLastAddConfirmed() {
         return lastEntry;
     }
 
     @Override
     public long getLength() {
         long length = 0;
         for (LedgerEntry entry : entries) {
             length += entry.getLength();
         }
 
         return length;
     }
 
     private static final Logger log = LoggerFactory.getLogger(MockLedgerHandle.class);
 
 }
