 /**
  * Copyright 2009 Google Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 
 package org.waveprotocol.wave.examples.fedone.waveserver;
 
 import com.google.common.annotations.VisibleForTesting;
 import com.google.common.collect.Sets;
 import com.google.protobuf.InvalidProtocolBufferException;
 
 import org.waveprotocol.wave.examples.fedone.common.HashedVersion;
 import org.waveprotocol.wave.examples.fedone.common.HashedVersionFactory;
 import org.waveprotocol.wave.examples.fedone.common.WaveletOperationSerializer;
 import org.waveprotocol.wave.examples.fedone.model.util.HashedVersionFactoryImpl;
 import org.waveprotocol.wave.examples.fedone.util.Log;
 import org.waveprotocol.wave.federation.Proto.ProtocolAppliedWaveletDelta;
 import org.waveprotocol.wave.federation.Proto.ProtocolHashedVersion;
 import org.waveprotocol.wave.federation.Proto.ProtocolSignedDelta;
 import org.waveprotocol.wave.federation.Proto.ProtocolWaveletDelta;
 import org.waveprotocol.wave.model.id.WaveletName;
 import org.waveprotocol.wave.model.operation.OperationException;
 import org.waveprotocol.wave.model.operation.OperationPair;
 import org.waveprotocol.wave.model.operation.Transform;
 import org.waveprotocol.wave.model.operation.TransformException;
 import org.waveprotocol.wave.model.operation.wave.WaveletDelta;
 import org.waveprotocol.wave.model.operation.wave.WaveletOperation;
 import org.waveprotocol.wave.model.wave.ParticipantId;
 import org.waveprotocol.wave.model.wave.data.WaveletData;
 import org.waveprotocol.wave.model.wave.data.impl.WaveletDataImpl;
 
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.List;
 import java.util.NavigableSet;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReadWriteLock;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 /**
  * Contains the history of a wavelet - applied and transformed deltas plus the content
  * of the wavelet.
  */
 abstract class WaveletContainerImpl implements WaveletContainer {
 
   /**
    * A wavelet delta with a target hashed version.
    */
   protected static final class VersionedWaveletDelta {
     public final WaveletDelta delta;
     public final HashedVersion version;
 
     public VersionedWaveletDelta(WaveletDelta delta, HashedVersion version) {
       this.delta = delta;
       this.version = version;
     }
   }
 
   private static final Log LOG = Log.get(WaveletContainerImpl.class);
 
   protected static final HashedVersionFactory HASHED_HISTORY_VERSION_FACTORY =
       new HashedVersionFactoryImpl();
 
   protected final NavigableSet<ByteStringMessage<ProtocolAppliedWaveletDelta>> appliedDeltas;
   protected final NavigableSet<ProtocolWaveletDelta> transformedDeltas;
   protected final NavigableSet<VersionedWaveletDelta> deserializedTransformedDeltas;
   private final Lock readLock;
   private final Lock writeLock;
   protected WaveletName waveletName;
   protected WaveletData waveletData;
   protected HashedVersion currentVersion;
   protected ProtocolHashedVersion lastCommittedVersion;
   protected State state;
 
   /** Constructor. */
   public WaveletContainerImpl(WaveletName waveletName) {
     this.waveletName = waveletName;
     waveletData = new WaveletDataImpl(waveletName.waveId, waveletName.waveletId);
     currentVersion = HASHED_HISTORY_VERSION_FACTORY.createVersionZero(waveletName);
     lastCommittedVersion = null;
 
     appliedDeltas = Sets.newTreeSet(appliedDeltaComparator);
     transformedDeltas = Sets.newTreeSet(transformedDeltaComparator);
     deserializedTransformedDeltas = Sets.newTreeSet(deserializedDeltaComparator);
 
     // Configure the locks used by this Wavelet.
     final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
     readLock = readWriteLock.readLock();
     writeLock = readWriteLock.writeLock();
     state = State.OK;
   }
 
   protected void acquireReadLock() {
     readLock.lock();
   }
 
   protected void releaseReadLock() {
     readLock.unlock();
   }
 
   protected void acquireWriteLock() {
     writeLock.lock();
   }
 
   protected void releaseWriteLock() {
     writeLock.unlock();
   }
 
   /** A comparator to be used in a TreeSet for applied deltas. */
   protected static final Comparator<ByteStringMessage<ProtocolAppliedWaveletDelta>> appliedDeltaComparator =
       new Comparator<ByteStringMessage<ProtocolAppliedWaveletDelta>>() {
         @Override
         public int compare(ByteStringMessage<ProtocolAppliedWaveletDelta> first,
             ByteStringMessage<ProtocolAppliedWaveletDelta> second) {
           if (first == null && second != null) { return -1; }
           if (first != null && second == null) { return 1; }
           if (first == null && second == null) { return 0; }
           try {
             return Long.valueOf(getVersionAppliedAt(first.getMessage()).getVersion()).compareTo(
                 getVersionAppliedAt(second.getMessage()).getVersion());
           } catch (InvalidProtocolBufferException e) {
             throw new IllegalStateException("Invalid applied delta added to history", e);
           }
         }
       };
 
   /**
    * Return a dummy ProtocolWaveletDelta instance used as a range
    * boundary for use in searches within a NavigableSet of deltas.
    *
    * @param version the version to return the delta applied at
    * @return the generated dummy delta
    */
   private static ProtocolWaveletDelta emptyDeltaAtVersion(final long version) {
     return ProtocolWaveletDelta.newBuilder()
         .setAuthor("dummy")
         .setHashedVersion(WaveletOperationSerializer.serialize(HashedVersion.unsigned(version)))
         .build();
   }
 
   /**
    * Return a dummy ProtocolAppliedWaveleetDelta instance used as a range
    * boundary.
    */
   private static ByteStringMessage<ProtocolAppliedWaveletDelta> emptyAppliedDeltaAtVersion(
       final long version) {
     ProtocolAppliedWaveletDelta delta = ProtocolAppliedWaveletDelta.newBuilder()
         .setApplicationTimestamp(0)
         .setOperationsApplied(0)
         .setSignedOriginalDelta(ProtocolSignedDelta.newBuilder()
              .setDelta(emptyDeltaAtVersion(version).toByteString())
         ).build();
     return ByteStringMessage.fromMessage(delta);
   }
 
   /**
    * Returns the version that the passed delta was actually applied at. Simply
    * resolves to the hashed version stored on the applied delta if it exists, or
    * defaults to the hashed version stored on the original immutable delta.
    *
    * @param appliedDelta the delta to examine
    * @return the hashed version applied at
    */
   protected static ProtocolHashedVersion getVersionAppliedAt(
       final ProtocolAppliedWaveletDelta appliedDelta) throws InvalidProtocolBufferException {
     if (appliedDelta.hasHashedVersionAppliedAt()) {
       return appliedDelta.getHashedVersionAppliedAt();
     } else {
       ProtocolSignedDelta signedDelta = appliedDelta.getSignedOriginalDelta();
       ProtocolWaveletDelta delta = ProtocolWaveletDelta.parseFrom(signedDelta.getDelta());
       return delta.getHashedVersion();
     }
   }
 
   /**
    * Return a dummy VersionedWaveletDelta instance used as a range boundary for
    * use in searches within a NavigableSet of deltas.
    *
    * @param version the version with which to return the versioned delta
    * @return a dummy versioned delta with a null delta
    */
   private static VersionedWaveletDelta emptyDeserializedDeltaAtVersion(final long version) {
     return new VersionedWaveletDelta(null, HashedVersion.unsigned(version));
   }
 
   /** A comparator to be used in a TreeSet for deserialized deltas. */
   private static final Comparator<VersionedWaveletDelta> deserializedDeltaComparator =
       new Comparator<VersionedWaveletDelta>() {
         @Override
         public int compare(VersionedWaveletDelta first, VersionedWaveletDelta second) {
           if (first == null && second != null) { return -1; }
           if (first != null && second == null) { return 1; }
           if (first == null && second == null) { return 0; }
           return Long.valueOf(first.version.getVersion()).compareTo(second.version.getVersion());
         }
       };
 
   /** A comparator to be used in a TreeSet for transformed deltas. */
   @VisibleForTesting
   static final Comparator<ProtocolWaveletDelta> transformedDeltaComparator =
       new Comparator<ProtocolWaveletDelta>() {
         @Override
         public int compare(ProtocolWaveletDelta first, ProtocolWaveletDelta second) {
           if (first == null && second != null) { return -1; }
           if (first != null && second == null) { return 1; }
           if (first == null && second == null) { return 0; }
           return Long.valueOf(first.getHashedVersion().getVersion()).compareTo(
               second.getHashedVersion().getVersion());
         }
       };
 
   protected void assertStateOk() throws WaveletStateException {
     if (state != State.OK) {
       throw new WaveletStateException(state, "The wavelet is not in a usable state. ");
     }
   }
 
   @Override
   public State getState() {
     acquireReadLock();
     try {
       return state;
     } finally {
       releaseReadLock();
     }
   }
 
 
   @Override
   public void setState(State state) {
     acquireWriteLock();
     try {
       this.state = state;
     } finally {
       releaseWriteLock();
     }
   }
 
   @Override
   public boolean checkAccessPermission(ParticipantId participantId) throws WaveletStateException {
     acquireReadLock();
     try {
       assertStateOk();
       return waveletData.getParticipants().contains(participantId);
     } finally {
       releaseReadLock();
     }
   }
 
   @Override
   public ProtocolHashedVersion getLastCommittedVersion() throws WaveletStateException {
     acquireReadLock();
     try {
       assertStateOk();
       return lastCommittedVersion;
     } finally {
       releaseReadLock();
     }
   }
 
   @Override
   public WaveletData getWaveletData() {
     return waveletData;
   }
 
   /**
    * Transform a wavelet delta if it has been submitted against a different head (currentVersion).
    * Must be called with write lock held.
    *
    * @param delta to possibly transform
    * @param appliedVersion that the delta is applied to
    * @return the transformed delta and the version it was applied at
    *   (the version is the current version of the wavelet, unless the delta is
    *   a duplicate in which case it is the version at which it was originally
    *   applied)
    * @throws InvalidHashException if submitting against same version but different hash
    * @throws OperationException if transformation fails
    */
   protected VersionedWaveletDelta maybeTransformSubmittedDelta(WaveletDelta delta,
       HashedVersion appliedVersion) throws InvalidHashException, OperationException {
     if (appliedVersion.equals(currentVersion)) {
       // Applied version is the same, we're submitting against head, don't need to do OT
       return new VersionedWaveletDelta(delta, appliedVersion);
     } else {
       // Not submitting against head, we need to do OT, but check the versions really are different
       if (appliedVersion.getVersion() == currentVersion.getVersion()) {
         LOG.warning("Same version (" + currentVersion.getVersion() + ") but different hashes (" +
             appliedVersion + "/" + currentVersion + ")");
         throw new InvalidHashException("Different hash, same version: "
             + currentVersion.getVersion());
       } else {
         return transformSubmittedDelta(delta, appliedVersion);
       }
     }
   }
 
   /**
    * Apply a list of operations from a single delta to the wavelet container.
    *
    * @param ops to apply
    */
   protected void applyWaveletOperations(List<WaveletOperation> ops) throws OperationException,
       EmptyDeltaException {
     if (ops.isEmpty()) {
       LOG.warning("No operations to apply at version " + currentVersion);
       throw new EmptyDeltaException();
     }
 
     WaveletOperation lastOp = null;
     int opsApplied = 0;
 
     try {
       for (WaveletOperation op : ops) {
         lastOp = op;
         op.apply(waveletData);
         opsApplied++;
       }
     } catch (OperationException e) {
       LOG.warning("Only applied " + opsApplied + " of " + ops.size() + " operations at version "
           + currentVersion + ", rolling back, failed op was " + lastOp, e);
       // Deltas are atomic, so roll back all operations that were successful
       rollbackWaveletOperations(ops.subList(0, opsApplied));
       throw new OperationException("Failed to apply all operations, none were applied", e);
     }
   }
 
   /**
    * Like applyWaveletOperations, but applies the inverse of the given operations (in reverse), and
    * no operations are permitted to fail.
    *
    * @param ops to roll back
    */
   private void rollbackWaveletOperations(List<WaveletOperation> ops) {
     for (int i = ops.size() - 1; i >= 0; i--) {
       try {
         ops.get(i).getInverse().apply(waveletData);
       } catch (OperationException e) {
         throw new IllegalArgumentException("Failed to roll back " + ops.get(i) + " with inverse "
             + ops.get(i).getInverse(), e);
       }
     }
   }
 
   /**
    * Finds range of server deltas needed to transform against, then transforms all client
    * ops against the server ops.
    */
   private VersionedWaveletDelta transformSubmittedDelta(WaveletDelta submittedDelta,
       HashedVersion appliedVersion) throws OperationException, InvalidHashException {
 
     NavigableSet<VersionedWaveletDelta> serverDeltas = deserializedTransformedDeltas.tailSet(
         deserializedTransformedDeltas.floor(emptyDeserializedDeltaAtVersion(
             appliedVersion.getVersion())),true);
 
     if (serverDeltas.size() == 0) {
       LOG.warning("Got empty server set, but not sumbitting to head! " + submittedDelta);
       // Not strictly an invalid hash, but it's a related issue
       throw new InvalidHashException("Cannot submit to head");
     }
 
     // Confirm that the target version/hash of this delta is valid.
     if (!serverDeltas.first().version.equals(appliedVersion)) {
       LOG.warning("Mismatched hashes: expected: " + serverDeltas.first().version +
           " got: " + appliedVersion);
       // Don't leak the hash to the client in the error message.
       throw new InvalidHashException("Mismatched hashes at version " + appliedVersion.getVersion());
     }
 
     ParticipantId clientAuthor = submittedDelta.getAuthor();
     List<WaveletOperation> clientOps = submittedDelta.getOperations();
     for (VersionedWaveletDelta d : serverDeltas) {
       // If the client delta transforms to nothing before we've traversed all the server
       // deltas, return the version at which the delta was obliterated (rather than the
       // current version) to ensure that delta submission is idempotent.
       if (clientOps.isEmpty()) {
System.err.println("empty\nc=" + clientAuthor + "\ns=" + d.delta.getAuthor() + ":" + d.delta.getOperations().size() + d.delta.getOperations().get(0) + "\nv=" + d.version);
         return new VersionedWaveletDelta(new WaveletDelta(clientAuthor, clientOps), d.version);
       }
       ParticipantId serverAuthor = d.delta.getAuthor();
       List<WaveletOperation> serverOps = d.delta.getOperations();
System.err.println("c=" + clientAuthor + ":" + clientOps.size() + clientOps.get(0) + "\ns=" + serverAuthor + ":" + serverOps.size() + serverOps.get(0) + "\nv=" + d.version + "\nequal=" + clientOps.equals(serverOps) + "," + clientOps.get(0).equals(serverOps.get(0)));
       if (clientAuthor.equals(serverAuthor) && clientOps.equals(serverOps)) {
System.err.println("dup\nc=" + clientAuthor + ":" + clientOps.size() + clientOps.get(0) + "\ns=" + serverAuthor + ":" + serverOps.size() + serverOps.get(0) + "\nv=" + d.version + "\nequal=" + clientOps.equals(serverOps) + "," + clientOps.get(0).equals(serverOps.get(0)));
         return d;
       }
       clientOps = transformOps(clientOps, serverOps);
     }
System.err.println("non-dup\nc=" + clientAuthor + ":" + clientOps.size() + clientOps.get(0) + "\ncurrentVersion=" + currentVersion);
     return new VersionedWaveletDelta(new WaveletDelta(clientAuthor, clientOps), currentVersion);
   }
 
   /**
    * Transforms the specified client operations against the specified server operations,
    * returning the transformed client operations in a new list.
    *
    * @param clientOps may be unmodifiable
    * @param serverOps may be unmodifiable
    * @return The transformed client ops
    */
   private List<WaveletOperation> transformOps(List<WaveletOperation> clientOps,
       List<WaveletOperation> serverOps) throws OperationException {
     List<WaveletOperation> transformedClientOps = new ArrayList<WaveletOperation>();
 
     for (WaveletOperation c : clientOps) {
      for (int i = 0; i < serverOps.size(); i++) {
        WaveletOperation s = serverOps.get(i);
         OperationPair<WaveletOperation> pair;
         try {
           pair = Transform.transform(c, s);
        } catch(TransformException e) {
           throw new OperationException(e);
         }
         c = pair.clientOp();
       }
       transformedClientOps.add(c);
     }
     return transformedClientOps;
   }
 
   /**
    * Commit an applied delta to this wavelet container.
    *
    * @param appliedDelta to commit
    * @param transformedDelta of the applied delta
    * @return result of the application
    */
   protected DeltaApplicationResult commitAppliedDelta(
       ByteStringMessage<ProtocolAppliedWaveletDelta> appliedDelta,
       WaveletDelta transformedDelta) {
 
     ProtocolWaveletDelta transformedProtocolDelta =
       WaveletOperationSerializer.serialize(transformedDelta, currentVersion);
     transformedDeltas.add(transformedProtocolDelta);
     deserializedTransformedDeltas.add(new VersionedWaveletDelta(transformedDelta, currentVersion));
     appliedDeltas.add(appliedDelta);
 
     HashedVersion newVersion = HASHED_HISTORY_VERSION_FACTORY.create(
         appliedDelta.getByteArray(), currentVersion, transformedDelta.getOperations().size());
     currentVersion = newVersion;
 
     return new DeltaApplicationResult(appliedDelta, transformedProtocolDelta,
         WaveletOperationSerializer.serialize(newVersion));
   }
 
   /**
   * @param version
    * @return the applied delta applied at the specified hashed version
    */
   protected ByteStringMessage<ProtocolAppliedWaveletDelta> lookupAppliedDelta(
       HashedVersion version) {
     return appliedDeltas.floor(emptyAppliedDeltaAtVersion(version.getVersion()));
   }
 
   @Override
   public NavigableSet<ByteStringMessage<ProtocolAppliedWaveletDelta>> requestHistory(
       ProtocolHashedVersion versionStart, ProtocolHashedVersion versionEnd)
       throws WaveletStateException {
     acquireReadLock();
     try {
       assertStateOk();
       // TODO: ### validate requested range.
       // TODO: #### make immutable.
 
       NavigableSet<ByteStringMessage<ProtocolAppliedWaveletDelta>> set =
           appliedDeltas.subSet(appliedDeltas.floor(
               emptyAppliedDeltaAtVersion(versionStart.getVersion())),
               true,
               emptyAppliedDeltaAtVersion(versionEnd.getVersion()),
               false);
       LOG.info("### HR " + versionStart.getVersion() + " - " + versionEnd.getVersion() + " set - " +
           set.size() + " = " + set);
       return set;
     } finally {
       releaseReadLock();
     }
   }
 
   @Override
   public NavigableSet<ProtocolWaveletDelta> requestTransformedHistory(ProtocolHashedVersion versionStart,
       ProtocolHashedVersion versionEnd) throws WaveletStateException {
     acquireReadLock();
     try {
       assertStateOk();
       // TODO: ### validate requested range.
       // TODO: #### make immutable.
       return transformedDeltas.subSet(transformedDeltas.floor(
           emptyDeltaAtVersion(versionStart.getVersion())),
           true, emptyDeltaAtVersion(versionEnd.getVersion()), false);
     } finally {
       releaseReadLock();
     }
   }
 
   @Override
   public List<ParticipantId> getParticipants() {
     acquireReadLock();
     try {
       return (waveletData != null ? waveletData.getParticipants() : null);
     } finally {
       releaseReadLock();
     }
   }
 
   @Override
   public HashedVersion getCurrentVersion() {
     return currentVersion;
   }
 }
