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
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.ArrayListMultimap;
 import com.google.common.collect.Multimap;
 import com.google.protobuf.ByteString;
 import com.google.protobuf.InvalidProtocolBufferException;
 
 import org.waveprotocol.wave.examples.fedone.common.HashedVersion;
 import org.waveprotocol.wave.examples.fedone.common.WaveletOperationSerializer;
 import static org.waveprotocol.wave.examples.fedone.common.WaveletOperationSerializer.serialize;
import org.waveprotocol.wave.examples.fedone.util.Log;
 import org.waveprotocol.wave.federation.Proto.ProtocolAppliedWaveletDelta;
 import org.waveprotocol.wave.federation.Proto.ProtocolHashedVersion;
 import org.waveprotocol.wave.federation.Proto.ProtocolSignature;
 import org.waveprotocol.wave.federation.Proto.ProtocolSignedDelta;
 import org.waveprotocol.wave.federation.Proto.ProtocolWaveletDelta;
 import org.waveprotocol.wave.model.id.WaveletName;
 import org.waveprotocol.wave.model.operation.OperationException;
 import org.waveprotocol.wave.model.operation.wave.WaveletDelta;
 import org.waveprotocol.wave.model.operation.wave.WaveletOperation;
 import org.waveprotocol.wave.model.util.Pair;
 
 import java.util.List;
 
 /**
  * A local wavelet may be updated by submits. The local wavelet will perform
  * operational transformation on the submitted delta and assign it the latest
  * version of the wavelet.
  */
 class LocalWaveletContainerImpl extends WaveletContainerImpl
     implements LocalWaveletContainer {
 
  private static final Log LOG = Log.get(LocalWaveletContainerImpl.class);

   /**
    * Associates a delta (represented by the hashed version of the wavelet state after its
    * application) with its signers (represented by their signer id)
    */
   private final Multimap<ProtocolHashedVersion, ByteString> deltaSigners =
       ArrayListMultimap.create();
 
   public LocalWaveletContainerImpl(WaveletName waveletName) {
     super(waveletName);
   }
 
   @Override
   public DeltaApplicationResult submitRequest(WaveletName waveletName,
       ProtocolSignedDelta signedDelta) throws OperationException,
       InvalidProtocolBufferException, InvalidHashException, EmptyDeltaException {
 
     acquireWriteLock();
     try {
       return transformAndApplyLocalDelta(signedDelta);
     } finally {
       releaseWriteLock();
     }
   }
 
   /**
    * Apply a signed delta to a local wavelet. This assumes the caller has
    * validated that the delta is at the correct version and can be applied to
    * the wavelet. Must be called with writelock held.
    *
    * @param signedDelta the delta that is to be applied to wavelet.
    * @return transformed operations are applied to this delta.
    * @throws OperationException if an error occurs during transformation or
    *         application
    * @throws InvalidProtocolBufferException if the signed delta did not contain a valid delta
    * @throws InvalidHashException if delta hash sanity checks fail
    *
    * @throws OperationException
    */
   private DeltaApplicationResult transformAndApplyLocalDelta(ProtocolSignedDelta signedDelta)
       throws OperationException, InvalidProtocolBufferException, EmptyDeltaException,
       InvalidHashException {
 
     ByteStringMessage<ProtocolWaveletDelta> protocolDelta = ByteStringMessage.from(
         ProtocolWaveletDelta.getDefaultInstance(), signedDelta.getDelta());
     Pair<WaveletDelta, HashedVersion> deltaAndVersion =
       WaveletOperationSerializer.deserialize(protocolDelta.getMessage());
 
    if (deltaAndVersion.first.getOperations().isEmpty()) {
      LOG.warning("No operations to apply at version " + currentVersion);
      throw new EmptyDeltaException();
    }
    
     VersionedWaveletDelta transformed =
         maybeTransformSubmittedDelta(deltaAndVersion.first, deltaAndVersion.second);
 
     if (transformed.delta.getOperations().isEmpty()) {
       Preconditions.checkState(transformed.version.getVersion() <= currentVersion.getVersion());
       // The delta was transformed away. That's OK but we don't call either
       // applyWaveletOperations(), because that will throw EmptyDeltaException, or
       // commitAppliedDelta(), because empty deltas cannot be part of the delta history.
       return new DeltaApplicationResult(buildAppliedDelta(signedDelta, transformed),
           WaveletOperationSerializer.serialize(transformed.delta, transformed.version),
           WaveletOperationSerializer.serialize(transformed.version));
     }
 
     if (!transformed.version.equals(currentVersion)) {
       Preconditions.checkState(transformed.version.getVersion() < currentVersion.getVersion());
       // The delta was a duplicate of an existing server delta.
       // We duplicate-eliminate it (don't apply it to the wavelet state and don't store it in
       // the delta history) and return the server delta which it was a duplicate of
       // (so delta submission becomes idem-potent).
       ByteStringMessage<ProtocolAppliedWaveletDelta> appliedDelta =
           lookupAppliedDelta(transformed.version);
       // TODO: look this up (in appliedDeltas or currentVersion) rather than compute it?
       HashedVersion hashedVersionAfterApplication = HASHED_HISTORY_VERSION_FACTORY.create(
         appliedDelta.getByteArray(), transformed.version, transformed.delta.getOperations().size());
       return new DeltaApplicationResult(appliedDelta,
           WaveletOperationSerializer.serialize(transformed.delta, transformed.version),
           WaveletOperationSerializer.serialize(hashedVersionAfterApplication));
     }
 
     // If any of the operations fail to apply, the wavelet data will be returned to its original
     // state and an OperationException thrown
     applyWaveletOperations(transformed.delta.getOperations());
 
     // Build the applied delta to commit
     ByteStringMessage<ProtocolAppliedWaveletDelta> appliedDelta =
         buildAppliedDelta(signedDelta, transformed);
 
     DeltaApplicationResult applicationResult = commitAppliedDelta(appliedDelta,
         transformed.delta);
 
     // Associate this hashed version with its signers.
     for (ProtocolSignature signature : signedDelta.getSignatureList()) {
       deltaSigners.put(serialize(HashedVersion.getHashedVersionAfter(appliedDelta)),
           signature.getSignerId());
     }
 
     return applicationResult;
   }
 
   private static ByteStringMessage<ProtocolAppliedWaveletDelta> buildAppliedDelta(
       ProtocolSignedDelta signedDelta, VersionedWaveletDelta transformed) {
     ProtocolAppliedWaveletDelta.Builder appliedDeltaBuilder =
         ProtocolAppliedWaveletDelta.newBuilder()
             .setSignedOriginalDelta(signedDelta)
             .setOperationsApplied(transformed.delta.getOperations().size())
             .setApplicationTimestamp(System.currentTimeMillis());
 
     // TODO: re-enable this condition for version 0.3 of the spec
     if (/*opsWereTransformed*/ true) {
       // This is set to indicate the head version of the wavelet was different to the intended
       // version of the wavelet (so the hash will have changed)
       appliedDeltaBuilder.setHashedVersionAppliedAt(
           WaveletOperationSerializer.serialize(transformed.version));
     }
 
     return ByteStringMessage.fromMessage(appliedDeltaBuilder.build());
   }
 
   @Override
   public boolean isDeltaSigner(ProtocolHashedVersion version, ByteString signerId) {
     return deltaSigners.get(version).contains(signerId);
   }
 }
