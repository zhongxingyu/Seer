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
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Maps;
 import com.google.protobuf.ByteString;
 
 import org.waveprotocol.wave.crypto.WaveSigner;
 import org.waveprotocol.wave.examples.fedone.waveserver.CertificateManager.SignatureResultListener;
 import org.waveprotocol.wave.federation.Proto.ProtocolSignedDelta;
 import org.waveprotocol.wave.federation.Proto.ProtocolWaveletDelta;
 
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 
 /**
  * Signs deltas passed to it in addDelta.
  * Obtain an instance of a Delta Signer using DeltaSignerFactory.
  *
  * Stores deltas in bundles. We will queue bundles that are in the process of being signed
  *
  * After a certain timeout, or after the bundle has reached
  * a maximum size, sign all deltas and call the listeners with the signed deltas.
  * A bundle is created on demand i.e. when a delta needs is to be added the first time,
  * or when a previous one has already been signed.
  *
  * TODO(balfanz): replace/augment the deltasToSign map to use proper streamauth bundles.
  *
  * @author jochen@google.com (Jochen Bekmann)
  */
 class BundlingDeltaSigner implements DeltaSigner {
 
   private final int bundlingAccumulationDelayMs;
   private final WaveSigner signer;
   private final ScheduledExecutorService executorService;
   private DeltaBundle currentBundle;
   private final int maximumDeltaBundleSize;
 
   /** Stores deltas in a bundle. Start a timer when the first delta is added. */
   private class DeltaBundle {
 
     AtomicBoolean acceptMoreDeltas;
 
     Map<ByteStringMessage<ProtocolWaveletDelta>,
         CertificateManager.SignatureResultListener> deltasToSign = Maps.newHashMap();
 
     private ScheduledFuture<?> scheduledFuture;
     private final Runnable signingTask;
 
     DeltaBundle() {
       scheduledFuture = null;  // Will be null until a signingTask is scheduled.
       acceptMoreDeltas = new AtomicBoolean(true);
       signingTask = new Runnable() {
         @Override public void run() {
           signBundle();
         }
       };
     }
 
     boolean canAcceptMoreDeltas() {
       return acceptMoreDeltas.get();
     }
 
     /** Sign bundle, notify every listener with a signedDelta result. */
     synchronized private void signBundle() {
       for (Entry<ByteStringMessage<ProtocolWaveletDelta>, SignatureResultListener> entry :
         deltasToSign.entrySet()) {
         ProtocolSignedDelta.Builder signedDelta = ProtocolSignedDelta.newBuilder();
         ByteString deltaBytes =  entry.getKey().getByteString();
         signedDelta.setDelta(deltaBytes);
        signedDelta.addAllSignature(ImmutableList.of(signer.sign(deltaBytes.toByteArray()).build()));
         entry.getValue().signatureResult(signedDelta.build());
       }
     }
 
     /**
      * Add the delta to the bundle. This may trigger signing now or later. The
      * resultListener will be called once the delta has been signed. May only
      * be called when canAcceptMoreDeltas is true.
      */
     synchronized void addDelta(ByteStringMessage<ProtocolWaveletDelta> delta,
         CertificateManager.SignatureResultListener resultListener) {
       Preconditions.checkState(acceptMoreDeltas.get());
 
       deltasToSign.put(delta, resultListener);
       if (deltasToSign.size() >= maximumDeltaBundleSize) {
         acceptMoreDeltas.set(false);
         // If there is a task scheduled, attempt to cancel, but do not interrupt if it's
         // already executing. If cancel() returns false the task has already been run.
         if (scheduledFuture == null || scheduledFuture.cancel(false)) {
           executorService.execute(signingTask);
         }
       } else if (scheduledFuture == null){
         scheduledFuture = executorService.schedule(signingTask, bundlingAccumulationDelayMs,
             TimeUnit.MILLISECONDS);
       }
     }
   }
 
   /**
    * Constructor.
    * @param executorService a ScheduledExecutorService, may be null if maximumDeltaBundleSize = 1
    * @param signer wave signer
    * @param bundlingAccumulationDelayMs hold deltas at most for this long
    * @param maximumDeltaBundleSize largest size for a bundle
    */
   BundlingDeltaSigner(ScheduledExecutorService executorService,
       WaveSigner signer,
       int maximumDeltaBundleSize,
       int bundlingAccumulationDelayMs) {
     this.executorService = executorService;
     this.signer = signer;
     this.currentBundle = null;
     Preconditions.checkArgument(maximumDeltaBundleSize > 1);
     this.maximumDeltaBundleSize = maximumDeltaBundleSize;
     Preconditions.checkArgument(bundlingAccumulationDelayMs > 0);
     this.bundlingAccumulationDelayMs = bundlingAccumulationDelayMs;
   }
 
   @Override
   synchronized public void sign(ByteStringMessage<ProtocolWaveletDelta> delta,
       CertificateManager.SignatureResultListener resultListener) {
     if (currentBundle == null || !currentBundle.canAcceptMoreDeltas()) {
       // If the old bundle has a pending task it will only be garbage collected once it's
       // signed all deltas because the scheduler has a handle to it.
       currentBundle = new DeltaBundle();
     }
     currentBundle.addDelta(delta, resultListener);
   }
 }
