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
 import com.google.common.base.Preconditions;
 import com.google.common.collect.ImmutableList;
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 import com.google.inject.Singleton;
 import com.google.inject.name.Named;
 import com.google.protobuf.ByteString;
 
import org.waveprotocol.wave.examples.fedone.crypto.WaveSigner;
 import org.waveprotocol.wave.examples.fedone.waveserver.CertificateManager.SignatureResultListener;
import org.waveprotocol.wave.protocol.common.ProtocolSignedDelta;
import org.waveprotocol.wave.protocol.common.ProtocolWaveletDelta;
 
 import java.util.concurrent.ScheduledExecutorService;
 
 @Singleton
 public class DeltaSignerProvider  implements Provider<DeltaSigner> {
 
   private int maximumDeltaBundleSize;
   private int bundlingAccumulationDelayMs;
   private final WaveSigner waveSigner;
   private final ScheduledExecutorService executor;
 
   @Inject
   public DeltaSignerProvider(ScheduledExecutorService executor,
     @Named("maximum_delta_bundle_size") int maximumDeltaBundleSize,
     @Named("delta_bundling_accumulation_delay_ms") int bundlingAccumulationDelayMs,
     WaveSigner waveSigner) {
 
     Preconditions.checkArgument(maximumDeltaBundleSize > 0, "Max delta bundle size must be >= 1.");
     Preconditions.checkArgument(bundlingAccumulationDelayMs >= 0, "Delta bundling accumulation " +
     		"delay must be >= 0");
     this.executor = executor;
     this.waveSigner = waveSigner;
     this.maximumDeltaBundleSize = maximumDeltaBundleSize;
     this.bundlingAccumulationDelayMs = bundlingAccumulationDelayMs;
   }
 
   @Override
   public DeltaSigner get() {
     Preconditions.checkArgument(maximumDeltaBundleSize > 0);
     if (maximumDeltaBundleSize == 1) {
       return getSimpleDeltaSigner(waveSigner);
     } else {
       return new BundlingDeltaSigner(
           executor,
           waveSigner, maximumDeltaBundleSize,
           bundlingAccumulationDelayMs);
     }
   }
 
   /**
    * Simple signer, signs the delta and immediately calls the resultListener.
    * @return the signed delta with a non-bundle signature.
    */
   @VisibleForTesting
   static DeltaSigner getSimpleDeltaSigner(final WaveSigner waveSigner) {
     return new DeltaSigner() {
 
       @Override
       public void sign(ByteStringMessage<ProtocolWaveletDelta> delta,
           SignatureResultListener resultListener) {
         ProtocolSignedDelta.Builder signedDelta = ProtocolSignedDelta.newBuilder();
         ByteString deltaBytes =  delta.getByteString();
         signedDelta.setDelta(deltaBytes);
         signedDelta.addAllSignature(ImmutableList.of(waveSigner.sign(deltaBytes.toByteArray())));
         resultListener.signatureResult(signedDelta.build());
       }
 
     };
   }
 }
