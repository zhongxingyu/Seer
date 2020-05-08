 package org.apache.bookkeeper.mledger.impl;
 
 import java.util.concurrent.ScheduledExecutorService;
 
 import org.apache.bookkeeper.client.BookKeeper;
 import org.apache.bookkeeper.mledger.AsyncCallbacks.AddEntryCallback;
import org.apache.bookkeeper.mledger.AsyncCallbacks.DeleteCursorCallback;
 import org.apache.bookkeeper.mledger.AsyncCallbacks.OpenCursorCallback;
 import org.apache.bookkeeper.mledger.ManagedCursor;
 import org.apache.bookkeeper.mledger.ManagedLedgerConfig;
 import org.apache.bookkeeper.mledger.ManagedLedgerException;
 import org.apache.bookkeeper.mledger.Position;
 
 /**
  * Read only implementation of ManagedLedger
  */
 public class ManagedLedgerAdminOnlyImpl extends ManagedLedgerImpl {
     public ManagedLedgerAdminOnlyImpl(ManagedLedgerFactoryImpl factory, BookKeeper bookKeeper, MetaStore store,
             ManagedLedgerConfig config, ScheduledExecutorService executor, String name) {
         super(factory, bookKeeper, store, config, executor, name);
     }
 
     private ManagedLedgerException exception() {
         return new ManagedLedgerException("Managed ledger is open only to administrative functions");
     }
 
     @Override
     public Position addEntry(byte[] data) throws InterruptedException, ManagedLedgerException {
         throw exception();
     }
 
     @Override
     public synchronized void asyncAddEntry(byte[] data, AddEntryCallback callback, Object ctx) {
         callback.addFailed(exception(), ctx);
     }
 
     @Override
     public synchronized ManagedCursor openCursor(String cursorName) throws InterruptedException, ManagedLedgerException {
         throw exception();
     }
 
     @Override
     public synchronized void asyncOpenCursor(String cursorName, OpenCursorCallback callback, Object ctx) {
         callback.openCursorFailed(exception(), ctx);
     }
 
    @Override
    public void asyncDeleteCursor(String consumerName, DeleteCursorCallback callback, Object ctx) {
        callback.deleteCursorFailed(exception(), ctx);
    }

    @Override
    public void deleteCursor(String name) throws InterruptedException, ManagedLedgerException {
        throw exception();
    }

 }
