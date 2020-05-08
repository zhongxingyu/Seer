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
 /**
  * 
  */
 package org.apache.bookkeeper.mledger.impl;
 
 import static com.google.common.base.Preconditions.checkArgument;
 import static com.google.common.base.Preconditions.checkNotNull;
 import static org.apache.bookkeeper.mledger.util.VarArgs.va;
 
 import java.util.Enumeration;
 import java.util.List;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.atomic.AtomicReference;
 
 import javax.annotation.concurrent.ThreadSafe;
 
 import org.apache.bookkeeper.client.AsyncCallback.AddCallback;
 import org.apache.bookkeeper.client.AsyncCallback.CloseCallback;
 import org.apache.bookkeeper.client.AsyncCallback.CreateCallback;
 import org.apache.bookkeeper.client.AsyncCallback.DeleteCallback;
 import org.apache.bookkeeper.client.AsyncCallback.OpenCallback;
 import org.apache.bookkeeper.client.AsyncCallback.ReadCallback;
 import org.apache.bookkeeper.client.BKException;
 import org.apache.bookkeeper.client.BookKeeper;
 import org.apache.bookkeeper.client.LedgerEntry;
 import org.apache.bookkeeper.client.LedgerHandle;
 import org.apache.bookkeeper.mledger.AsyncCallbacks.MarkDeleteCallback;
 import org.apache.bookkeeper.mledger.AsyncCallbacks.ReadEntriesCallback;
 import org.apache.bookkeeper.mledger.Entry;
 import org.apache.bookkeeper.mledger.ManagedCursor;
 import org.apache.bookkeeper.mledger.ManagedLedgerConfig;
 import org.apache.bookkeeper.mledger.ManagedLedgerException;
 import org.apache.bookkeeper.mledger.ManagedLedgerException.MetaStoreException;
 import org.apache.bookkeeper.mledger.Position;
 import org.apache.bookkeeper.mledger.impl.MetaStore.MetaStoreCallback;
 import org.apache.bookkeeper.mledger.impl.MetaStore.Version;
 import org.apache.bookkeeper.mledger.proto.MLDataFormats.ManagedCursorInfo;
 import org.apache.bookkeeper.mledger.proto.MLDataFormats.PositionInfo;
 import org.apache.bookkeeper.mledger.util.CallbackMutex;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Objects;
 import com.google.protobuf.InvalidProtocolBufferException;
 
 @ThreadSafe
 class ManagedCursorImpl implements ManagedCursor {
 
     protected final BookKeeper bookkeeper;
     protected final ManagedLedgerConfig config;
     private final ManagedLedgerImpl ledger;
     private final String name;
 
     private final AtomicReference<PositionImpl> acknowledgedPosition = new AtomicReference<PositionImpl>();
     private final AtomicReference<PositionImpl> readPosition = new AtomicReference<PositionImpl>();
 
     // Cursor ledger reference will always point to an opened ledger
     private AtomicReference<LedgerHandle> cursorLedger = new AtomicReference<LedgerHandle>();
     private AtomicReference<Version> cursorLedgerVersion = new AtomicReference<Version>();
 
     // This mutex is used to prevent mark-delete being run while we are
     // switching to a new ledger for cursor position
     private final CallbackMutex ledgerMutex = new CallbackMutex();
 
     public interface VoidCallback {
         public void operationComplete();
 
         public void operationFailed(ManagedLedgerException exception);
     }
 
     ManagedCursorImpl(BookKeeper bookkeeper, ManagedLedgerConfig config, ManagedLedgerImpl ledger, String cursorName) {
         this.bookkeeper = bookkeeper;
         this.config = config;
         this.ledger = ledger;
         this.name = cursorName;
     }
 
     /**
      * Performs the initial recovery, reading the mark-deleted position from the ledger and then calling initialize to
      * have a new opened ledger
      */
     void recover(final VoidCallback callback) {
         // Read the meta-data ledgerId from the store
         log.debug("[{}] Recovering from bookkeeper ledger", ledger.getName(), name);
         ledger.getStore().asyncGetConsumerLedgerId(ledger.getName(), name, new MetaStoreCallback<ManagedCursorInfo>() {
             public void operationComplete(ManagedCursorInfo info, Version version) {
                 log.debug("[{}] Consumer {} meta-data recover from ledger {}",
                         va(ledger.getName(), name, info.getCursorsLedgerId()));
                 cursorLedgerVersion.set(version);
                 recoverFromLedger(info.getCursorsLedgerId(), callback);
             }
 
             public void operationFailed(MetaStoreException e) {
                 callback.operationFailed(e);
             }
         });
     }
 
     protected OpenCallback getOpenCallback(final long ledgerId, final VoidCallback callback, final boolean isReadOnly) {
         return new OpenCallback() {
             public void openComplete(int rc, LedgerHandle lh, Object ctx) {

                 if (rc != BKException.Code.OK) {
                     log.warn("[{}] Error opening metadata ledger {} for consumer {}: {}",
                             va(ledger.getName(), ledgerId, name, BKException.create(rc)));
                     callback.operationFailed(new ManagedLedgerException(BKException.create(rc)));
                     return;
                 }
 
                 // Read the last entry in the ledger
                 final long entryId = lh.getLastAddConfirmed();
                 lh.asyncReadEntries(entryId, entryId, new ReadCallback() {
                     public void readComplete(int rc, LedgerHandle lh, Enumeration<LedgerEntry> seq, Object ctx) {
                         log.debug("readComplete rc={} entryId={}", rc, entryId);
                         if (rc != BKException.Code.OK) {
                             log.warn("[{}] Error reading from metadata ledger {} for consumer {}: {}",
                                     va(ledger.getName(), ledgerId, name, BKException.create(rc)));
                             callback.operationFailed(new ManagedLedgerException(BKException.create(rc)));
                             return;
                         }
 
                         LedgerEntry entry = seq.nextElement();
                         PositionInfo positionInfo;
                         try {
                             positionInfo = PositionInfo.parseFrom(entry.getEntry());
                         } catch (InvalidProtocolBufferException e) {
                             callback.operationFailed(new ManagedLedgerException(e));
                             return;
                         }
 
                         PositionImpl position = new PositionImpl(positionInfo);
                         log.debug("[{}] Consumer {} recovered to position {}", va(ledger.getName(), name, position));
                         if (isReadOnly) {
                             setAcknowledgedPosition(position);
                         } else {
                             initialize(position, callback);
                             lh.asyncClose(new CloseCallback() {
                                 public void closeComplete(int rc, LedgerHandle lh, Object ctx) {
                                 }
                             }, null);
                         }
                     }
                 }, null);
             }
         };
     }
 
     protected void recoverFromLedger(final long ledgerId, final VoidCallback callback) {
         // Read the acknowledged position from the metadata ledger, then create
         // a new ledger and write the position into it
         bookkeeper.asyncOpenLedger(ledgerId, config.getDigestType(), config.getPassword(),
                 getOpenCallback(ledgerId, callback, false), null);
     }
 
     void initialize(final PositionImpl position, final VoidCallback callback) {
         setAcknowledgedPosition(position);
         createNewMetadataLedger(callback);
     }
 
     @Override
     public List<Entry> readEntries(int numberOfEntriesToRead) throws InterruptedException, ManagedLedgerException {
         checkArgument(numberOfEntriesToRead > 0);
 
         final CountDownLatch counter = new CountDownLatch(1);
         class Result {
             ManagedLedgerException exception = null;
             List<Entry> entries = null;
         }
 
         final Result result = new Result();
 
         asyncReadEntries(numberOfEntriesToRead, new ReadEntriesCallback() {
             public void readEntriesComplete(List<Entry> entries, Object ctx) {
                 result.entries = entries;
                 counter.countDown();
             }
 
             public void readEntriesFailed(ManagedLedgerException exception, Object ctx) {
                 result.exception = exception;
                 counter.countDown();
             }
 
         }, null);
 
         counter.await();
 
         if (result.exception != null)
             throw result.exception;
 
         return result.entries;
     }
 
     @Override
     public void asyncReadEntries(final int numberOfEntriesToRead, final ReadEntriesCallback callback, final Object ctx) {
         checkArgument(numberOfEntriesToRead > 0);
 
         OpReadEntry op = new OpReadEntry(this, readPosition.get(), numberOfEntriesToRead, callback, ctx);
         ledger.asyncReadEntries(op);
     }
 
     @Override
     public boolean hasMoreEntries() {
         return ledger.hasMoreEntries(readPosition.get());
     }
 
     @Override
     public long getNumberOfEntries() {
         return ledger.getNumberOfEntries(readPosition.get());
     }
 
     @Override
     public void markDelete(Position position) throws InterruptedException, ManagedLedgerException {
         checkNotNull(position);
         checkArgument(position instanceof PositionImpl);
 
         class Result {
             ManagedLedgerException exception = null;
         }
 
         final Result result = new Result();
         final CountDownLatch counter = new CountDownLatch(1);
 
         asyncMarkDelete(position, new MarkDeleteCallback() {
             public void markDeleteComplete(Object ctx) {
                 counter.countDown();
             }
 
             public void markDeleteFailed(ManagedLedgerException exception, Object ctx) {
                 result.exception = exception;
                 counter.countDown();
             }
         }, null);
 
         counter.await();
         if (result.exception != null) {
             throw result.exception;
         }
     }
 
     /**
      * 
      * @param newPosition
      *            the new acknowledged position
      * @return the previous acknowledged position
      */
     PositionImpl setAcknowledgedPosition(PositionImpl newPosition) {
         PositionImpl oldPosition = acknowledgedPosition.getAndSet(newPosition);
 
         PositionImpl currentRead = readPosition.get();
         if (currentRead == null || newPosition.compareTo(currentRead) >= 0) {
             // If the position that is markdeleted is past the read position, it
             // means that the client has skipped some entries. We need to move
             // read position forward
             readPosition.compareAndSet(currentRead,
                     new PositionImpl(newPosition.getLedgerId(), newPosition.getEntryId() + 1));
         }
 
         return oldPosition;
     }
 
     @Override
     public void asyncMarkDelete(final Position position, final MarkDeleteCallback callback, final Object ctx) {
         checkNotNull(position);
         checkArgument(position instanceof PositionImpl);
         log.debug("Acquiring ledger mutex");
         ledgerMutex.lock();
 
         log.debug("[{}] Mark delete cursor {} up to position: {}", va(ledger.getName(), name, position));
         final PositionImpl newPosition = (PositionImpl) position;
         final PositionImpl oldPosition = setAcknowledgedPosition(newPosition);
         persistPosition(cursorLedger.get(), newPosition, new VoidCallback() {
             public void operationComplete() {
                 log.debug("[{}] Mark delete to position {} succeeded", ledger.getName(), position);
                 ledger.updateCursor(ManagedCursorImpl.this, oldPosition, (PositionImpl) position);
                 callback.markDeleteComplete(ctx);
             }
 
             public void operationFailed(ManagedLedgerException exception) {
                 log.warn("[{}] Failed to mark delete position for cursor={} ledger={} position={}",
                         va(ledger.getName(), ManagedCursorImpl.this, position));
                 callback.markDeleteFailed(exception, ctx);
             }
         });
 
         ledgerMutex.unlock();
     }
 
     @Override
     public synchronized String toString() {
         return Objects.toStringHelper(this).add("ledger", ledger.getName()).add("name", name)
                 .add("ackPos", acknowledgedPosition).add("readPos", readPosition).toString();
     }
 
     @Override
     public String getName() {
         return name;
     }
 
     @Override
     public Position getReadPosition() {
         return readPosition.get();
     }
 
     @Override
     public Position getMarkDeletedPosition() {
         return acknowledgedPosition.get();
     }
 
     @Override
     public void skip(int entries) throws ManagedLedgerException {
         checkArgument(entries > 0);
         readPosition.set(ledger.skipEntries(readPosition.get(), entries));
     }
 
     @Override
     public void seek(Position newReadPositionInt) throws ManagedLedgerException {
         checkArgument(newReadPositionInt instanceof PositionImpl);
         PositionImpl newReadPosition = (PositionImpl) newReadPositionInt;
         checkArgument(newReadPosition.compareTo(acknowledgedPosition.get()) > 0,
                 "new read position must be greater than or equal to the mark deleted position for this cursor");
 
         checkArgument(ledger.isValidPosition(newReadPosition), "new read position is not valid for this managed ledger");
         readPosition.set(newReadPosition);
     }
 
     @Override
     public void close() throws InterruptedException, ManagedLedgerException {
         // Nothing to release in this implementation
     }
 
     /**
      * Internal version of seek that doesn't do the validation check
      * 
      * @param newReadPosition
      */
     void setReadPosition(Position newReadPositionInt) {
         checkArgument(newReadPositionInt instanceof PositionImpl);
         PositionImpl newReadPosition = (PositionImpl) newReadPositionInt;
         readPosition.set(newReadPosition);
     }
 
     // //////////////////////////////////////////////////
     void createNewMetadataLedger(final VoidCallback callback) {
         bookkeeper.asyncCreateLedger(config.getMetadataEnsemblesize(), config.getMetadataWriteQuorumSize(),
                 config.getMetadataAckQuorumSize(), config.getDigestType(), config.getPassword(), new CreateCallback() {
                     public void createComplete(int rc, final LedgerHandle lh, Object ctx) {
                         if (rc == BKException.Code.OK) {
                             // Created the ledger, now write the last position
                             // content
                             ledgerMutex.lock();
                             persistPosition(lh, acknowledgedPosition.get(), new VoidCallback() {
                                 public void operationComplete() {
                                     switchToNewLedger(lh, callback);
                                 }
 
                                 public void operationFailed(ManagedLedgerException exception) {
                                     bookkeeper.asyncDeleteLedger(lh.getId(), new DeleteCallback() {
                                         public void deleteComplete(int rc, Object ctx) {
                                         }
                                     }, null);
                                     callback.operationFailed(exception);
                                     ledgerMutex.unlock();
                                 }
                             });
                         } else {
                             log.warn("[{}] Error creating ledger for cursor {}: {}",
                                     va(ledger.getName(), name, BKException.getMessage(rc)));
                             callback.operationFailed(new ManagedLedgerException(BKException.create(rc)));
                         }
                     }
                 }, null);
     }
 
     void persistPosition(final LedgerHandle lh, final PositionImpl position, final VoidCallback callback) {
         PositionInfo pi = position.getPositionInfo();
         try {
             lh.asyncAddEntry(pi.toByteArray(), new AddCallback() {
                 public void addComplete(int rc, LedgerHandle lh, long entryId, Object ctx) {
                     if (rc == BKException.Code.OK) {
                         log.debug("[{}] Updated cursor position {} in meta-ledger {}",
                                 va(ledger.getName(), position, lh.getId()));
                         callback.operationComplete();
 
                         if (lh.getLastAddConfirmed() == config.getMetadataMaxEntriesPerLedger()) {
                             // Force to create a new ledger
                             createNewMetadataLedger(new VoidCallback() {
                                 public void operationComplete() {
                                     log.info("[{}] Created new metadata ledger for consumer {}", ledger.getName(), name);
                                 }
 
                                 public void operationFailed(ManagedLedgerException exception) {
                                     log.warn("[{}] Failed to create new metadata ledger for consumer {}: {}",
                                             va(ledger.getName(), name, exception));
                                 }
                             });
                         }
                     } else {
                         log.warn("[{}] Error updating cursor position {} in meta-ledger {}: ",
                                 va(ledger.getName(), position, lh.getId(), BKException.create(rc)));
                         callback.operationFailed(new ManagedLedgerException(BKException.create(rc)));
                     }
                 }
             }, null);
         } catch (RuntimeException e) {
             callback.operationFailed(new ManagedLedgerException(e));
         }
     }
 
     void switchToNewLedger(final LedgerHandle lh, final VoidCallback callback) {
         // Now we have an opened ledger that already has the acknowledged
         // position written into. At this point we can start using this new
         // ledger and delete the old one.
         ManagedCursorInfo info = ManagedCursorInfo.newBuilder().setCursorsLedgerId(lh.getId()).build();
         ledger.getStore().asyncUpdateConsumer(ledger.getName(), name, info, cursorLedgerVersion.get(),
                 new MetaStoreCallback<Void>() {
                     public void operationComplete(Void result, Version version) {
                         log.debug("[{}] Updated consumer {} with ledger id {} md-position={} rd-position={}",
                                 va(ledger.getName(), name, lh.getId(), acknowledgedPosition.get(), readPosition.get()));
                         LedgerHandle oldLedger = cursorLedger.getAndSet(lh);
                         cursorLedgerVersion.set(version);
                         closeAndDeleteLedger(oldLedger);
                         callback.operationComplete();
                         ledgerMutex.unlock();
                     }
 
                     public void operationFailed(MetaStoreException e) {
                         log.warn("[{}] Failed to update consumer {}: {}", va(ledger.getName(), name, e));
                         callback.operationFailed(e);
                         ledgerMutex.unlock();
                     }
                 });
     }
 
     void closeAndDeleteLedger(final LedgerHandle lh) {
         if (lh == null) {
             return;
         }
 
         ledger.getExecutor().execute(new Runnable() {
             public void run() {
                 try {
                     lh.close();
                     bookkeeper.deleteLedger(lh.getId());
                 } catch (Exception e) {
                     log.warn("[{}] Failed to close&delete ledger {} in backgrond", ledger.getName(), lh.getId());
                 }
             }
         });
     }
 
     private static final Logger log = LoggerFactory.getLogger(ManagedCursorImpl.class);
 }
