 /**
  * Copyright 2010 Google Inc.
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
 
 package org.waveprotocol.wave.examples.fedone.util;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.Lists;
 
 import org.waveprotocol.wave.model.document.operation.impl.DocInitializationBuffer;
 import org.waveprotocol.wave.model.id.WaveletName;
 import org.waveprotocol.wave.model.operation.OperationException;
 import org.waveprotocol.wave.model.operation.core.CoreWaveletDelta;
 import org.waveprotocol.wave.model.operation.wave.ConversionUtil;
 import org.waveprotocol.wave.model.operation.wave.WaveletOperation;
 import org.waveprotocol.wave.model.testing.BasicFactories;
 import org.waveprotocol.wave.model.version.DistinctVersion;
 import org.waveprotocol.wave.model.wave.ParticipantId;
 import org.waveprotocol.wave.model.wave.data.BlipData;
 import org.waveprotocol.wave.model.wave.data.DocumentFactory;
 import org.waveprotocol.wave.model.wave.data.ObservableWaveletData;
 import org.waveprotocol.wave.model.wave.data.WaveletData;
 import org.waveprotocol.wave.model.wave.data.impl.EmptyWaveletSnapshot;
 import org.waveprotocol.wave.model.wave.data.impl.WaveletDataImpl;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Utility methods for {@link WaveletData}.
  *
  * @author ljvderijk@google.com (Lennard de Rijk)
  */
 public final class WaveletDataUtil {
 
   private static final DocumentFactory<?> DOCUMENT_FACTORY = BasicFactories.muteDocumentFactory();
   private WaveletDataUtil() {
   }
 
   /**
    * Returns the {@link WaveletName} for the given wavelet.
    *
    * @param wavelet the wavelet to get the name for
    */
   public static WaveletName waveletNameOf(WaveletData wavelet) {
     return WaveletName.of(wavelet.getWaveId(), wavelet.getWaveletId());
   }
 
   /**
    * Apply a delta to the given wavelet. Rolls back the operation if it fails.
    *
    * @param delta delta to apply.
    * @param wavelet the wavelet to apply the operations to.
    * @param endVersion the {@link DistinctVersion} after applying the deltas.
    * @param applicationTimestamp timestamp of operation application.
    *
    * @throws OperationException if the operations fail to apply (and are
    *         successfully rolled back).
    * @throws IllegalStateException if the operations have failed and can not be
    *         rolled back.
    */
   public static void applyWaveletDelta(CoreWaveletDelta delta, WaveletData wavelet,
       DistinctVersion endVersion, long applicationTimestamp) throws OperationException {
     Preconditions.checkState(wavelet != null, "wavelet may not be null");
     long expectedEndVersion = wavelet.getVersion() + delta.getOperations().size();
     Preconditions.checkState(expectedEndVersion == endVersion.getVersion(),
         "Expected end version (" + expectedEndVersion + ") does not match the given end version ("
             + endVersion.getVersion() + ")");
 
     // Create WaveletOperations from the CoreWaveletOperations in the delta
     List<WaveletOperation> ops =
         ConversionUtil.fromCoreWaveletDelta(delta, applicationTimestamp, endVersion);
 
     List<WaveletOperation> reverseOps = new ArrayList<WaveletOperation>();
     WaveletOperation lastOp = null;
     int opsApplied = 0;
     try {
       for (WaveletOperation op : ops) {
         lastOp = op;
         List<? extends WaveletOperation> reverseOp = op.applyAndReturnReverse(wavelet);
         reverseOps.addAll(reverseOp);
         opsApplied++;
       }
     } catch (OperationException e) {
       // Deltas are atomic, so roll back all operations that were successful
       rollbackWaveletOperations(wavelet, reverseOps);
       throw new OperationException(
           "Only applied " + opsApplied + " of " + ops.size() + " operations at version "
               + wavelet.getVersion() + ", rolling back, failed op was " + lastOp, e);
     }
   }
 
   /**
    * Like applyWaveletOperations, but throws an {@link IllegalStateException}
    * when ops fail to apply. Is used for rolling back operations.
    *
    * @param ops to apply for rollback
    */
   private static void rollbackWaveletOperations(WaveletData wavelet, List<WaveletOperation> ops) {
     for (int i = ops.size() - 1; i >= 0; i--) {
       try {
         ops.get(i).apply(wavelet);
       } catch (OperationException e) {
         throw new IllegalStateException(
             "Failed to roll back " + ops.get(i) + " with inverse " + ops.get(i), e);
       }
     }
   }
 
   /**
    * Creates an empty wavelet.
    *
    * @param waveletName the name of the wavelet.
    * @param author the author of the wavelet.
   * @param applicationTimeStamp the time at which the wavelet is created.
    */
   public static ObservableWaveletData createEmptyWavelet(
      WaveletName waveletName, ParticipantId author, long applicationTimeStamp) {
     return WaveletDataImpl.Factory.create(DOCUMENT_FACTORY).create(new EmptyWaveletSnapshot(
        waveletName.waveId, waveletName.waveletId, author, applicationTimeStamp));
   }
 
   /**
    * Adds an empty blip to the given wavelet.
    *
    * @param wavelet the wavelet to add the blip to.
    * @param blipId the id of the blip to add.
    * @param author the author of this blip (will also be the only participant).
    * @param time the time to set in the blip as creation/lastmodified time.
    */
   public static BlipData addEmptyBlip(
       WaveletData wavelet, String blipId, ParticipantId author, long time) {
     return wavelet.createBlip(blipId,
         author,
         Lists.newArrayList(author),
         new DocInitializationBuffer().finish(),
         time,
         time);
   }
 }
