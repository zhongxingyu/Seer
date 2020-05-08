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
 
 package org.waveprotocol.box.server.waveserver;
 
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Lists;
 import com.google.protobuf.ByteString;
 import com.google.protobuf.InvalidProtocolBufferException;
 
 import junit.framework.TestCase;
 
 import org.waveprotocol.box.server.common.CoreWaveletOperationSerializer;
 import org.waveprotocol.box.server.common.HashedVersionFactoryImpl;
 import org.waveprotocol.box.server.util.URLEncoderDecoderBasedPercentEncoderDecoder;
 import org.waveprotocol.wave.federation.Proto.ProtocolAppliedWaveletDelta;
 import org.waveprotocol.wave.federation.Proto.ProtocolHashedVersion;
 import org.waveprotocol.wave.federation.Proto.ProtocolSignature;
 import org.waveprotocol.wave.federation.Proto.ProtocolSignedDelta;
 import org.waveprotocol.wave.federation.Proto.ProtocolWaveletDelta;
 import org.waveprotocol.wave.model.document.operation.BufferedDocOp;
 import org.waveprotocol.wave.model.document.operation.impl.DocOpBuilder;
 import org.waveprotocol.wave.model.id.IdURIEncoderDecoder;
 import org.waveprotocol.wave.model.id.WaveId;
 import org.waveprotocol.wave.model.id.WaveletId;
 import org.waveprotocol.wave.model.id.WaveletName;
 import org.waveprotocol.wave.model.operation.OperationException;
 import org.waveprotocol.wave.model.operation.wave.AddParticipant;
 import org.waveprotocol.wave.model.operation.wave.BlipContentOperation;
 import org.waveprotocol.wave.model.operation.wave.RemoveParticipant;
 import org.waveprotocol.wave.model.operation.wave.TransformedWaveletDelta;
 import org.waveprotocol.wave.model.operation.wave.WaveletBlipOperation;
 import org.waveprotocol.wave.model.operation.wave.WaveletDelta;
 import org.waveprotocol.wave.model.operation.wave.WaveletOperation;
 import org.waveprotocol.wave.model.operation.wave.WaveletOperationContext;
 import org.waveprotocol.wave.model.version.HashedVersion;
 import org.waveprotocol.wave.model.version.HashedVersionFactory;
 import org.waveprotocol.wave.model.wave.ParticipantId;
 
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 import java.util.Set;
 
 /**
  * Tests for local and remote wavelet containers.
  *
  *
  */
 public class WaveletContainerTest extends TestCase {
 
   private static final IdURIEncoderDecoder URI_CODEC =
       new IdURIEncoderDecoder(new URLEncoderDecoderBasedPercentEncoderDecoder());
   private static final HashedVersionFactory HASH_FACTORY = new HashedVersionFactoryImpl(URI_CODEC);
 
   private static final String domain = "wave.google.com";
   private static final WaveletName waveletName = WaveletName.of(
       new WaveId(domain, "waveid"), new WaveletId(domain, "waveletid"));
   private static final ParticipantId author = new ParticipantId("admin@" + domain);
   private static final Set<ParticipantId> participants = ImmutableSet.of(
       new ParticipantId("foo@" + domain), new ParticipantId("bar@example.com"));
   private static final HashedVersion version0 = HASH_FACTORY.createVersionZero(waveletName);
   private static final ByteString fakeSigner1 = ByteString.EMPTY;
   private static final ByteString fakeSigner2 = ByteString.copyFrom(new byte[] {1});
   private static final ProtocolSignature fakeSignature1 = ProtocolSignature.newBuilder()
       .setSignatureBytes(ByteString.EMPTY)
       .setSignerId(fakeSigner1)
       .setSignatureAlgorithm(ProtocolSignature.SignatureAlgorithm.SHA1_RSA)
       .build();
   private static final ProtocolSignature fakeSignature2 = ProtocolSignature.newBuilder()
       .setSignatureBytes(ByteString.copyFrom(new byte[] {1}))
       .setSignerId(fakeSigner2)
       .setSignatureAlgorithm(ProtocolSignature.SignatureAlgorithm.SHA1_RSA)
       .build();
   private static final WaveletOperationContext CONTEXT = new WaveletOperationContext(author,
       0, 1);
 
   private static final List<WaveletOperation> addParticipantOps = Lists.newArrayList();
   private static final List<WaveletOperation> removeParticipantOps = Lists.newArrayList();
   private static final List<WaveletOperation> doubleRemoveParticipantOps;
 
   static {
     for (ParticipantId p : participants) {
       addParticipantOps.add(new AddParticipant(CONTEXT, p));
       removeParticipantOps.add(new RemoveParticipant(CONTEXT, p));
     }
 
     Collections.reverse(removeParticipantOps);
     doubleRemoveParticipantOps = Lists.newArrayList(removeParticipantOps);
     doubleRemoveParticipantOps.addAll(removeParticipantOps);
   }
 
   private LocalWaveletContainerImpl localWavelet;
   private RemoteWaveletContainerImpl remoteWavelet;
 
   @Override
   public void setUp() throws Exception {
     super.setUp();
     localWavelet = new LocalWaveletContainerImpl(waveletName);
     remoteWavelet = new RemoteWaveletContainerImpl(waveletName);
   }
 
   // Tests
 
   public void testLocalApplyWaveletOperation() throws Exception {
     assertSuccessfulApplyWaveletOperations(localWavelet);
   }
 
   public void testRemoteApplyWaveletOperation() throws Exception {
     assertSuccessfulApplyWaveletOperations(remoteWavelet);
   }
 
   public void testLocalFailedWaveletOperations() throws Exception {
     assertFailedWaveletOperations(localWavelet);
   }
 
   public void testRemoteFailedWaveletOperations() throws Exception {
     assertFailedWaveletOperations(localWavelet);
   }
 
   public void testSuccessfulLocalRequest() throws Exception {
     ProtocolSignedDelta addDelta = ProtocolSignedDelta.newBuilder()
         .addSignature(fakeSignature1)
         .setDelta(addParticipantProtoDelta(localWavelet).toByteString())
         .build();
     localWavelet.submitRequest(waveletName, addDelta);
     assertEquals(localWavelet.getCurrentVersion().getVersion(), 2);
     assertTrue(localWavelet.isDeltaSigner(
         localWavelet.getCurrentVersion(), fakeSigner1));
     assertFalse(localWavelet.isDeltaSigner(
         localWavelet.getCurrentVersion(), fakeSigner2));
 
     HashedVersion oldVersion = localWavelet.getCurrentVersion();
     ProtocolSignedDelta removeDelta = ProtocolSignedDelta.newBuilder()
         .addSignature(fakeSignature2)
         .setDelta(ProtocolWaveletDelta.newBuilder(removeParticipantProtoDelta(localWavelet))
             .setHashedVersion(serialize(localWavelet.getCurrentVersion())).build().toByteString())
         .build();
     localWavelet.submitRequest(waveletName, removeDelta);
     assertEquals(localWavelet.getCurrentVersion().getVersion(), 4);
     assertTrue(localWavelet.isDeltaSigner(oldVersion, fakeSigner1));
     assertFalse(localWavelet.isDeltaSigner(oldVersion, fakeSigner2));
     assertTrue(localWavelet.isDeltaSigner(
         localWavelet.getCurrentVersion(), fakeSigner2));
     assertFalse(localWavelet.isDeltaSigner(
         localWavelet.getCurrentVersion(), fakeSigner1));
   }
 
   public void testFailedLocalWaveletRequest() throws Exception {
     ProtocolSignedDelta removeDelta = ProtocolSignedDelta.newBuilder()
         .addSignature(fakeSignature1)
         .setDelta(removeParticipantProtoDelta(localWavelet).toByteString())
         .build();
     try {
       localWavelet.submitRequest(waveletName, removeDelta);
       fail("Should fail");
     } catch (OperationException e) {
       // Correct
     }
     assertEquals(localWavelet.getCurrentVersion(), version0);
 
     ProtocolSignedDelta addDelta = ProtocolSignedDelta.newBuilder()
         .addSignature(fakeSignature1)
         .setDelta(addParticipantProtoDelta(localWavelet).toByteString())
         .build();
 
     localWavelet.submitRequest(waveletName, addDelta);
     try {
       ProtocolSignedDelta addAgainDelta = ProtocolSignedDelta.newBuilder()
           .addSignature(fakeSignature2)
           .setDelta(ProtocolWaveletDelta.newBuilder(addParticipantProtoDelta(localWavelet))
               .setHashedVersion(serialize(localWavelet.getCurrentVersion()))
               .build().toByteString())
           .build();
       localWavelet.submitRequest(waveletName, addAgainDelta);
       fail("Should fail");
     } catch (OperationException e) {
       // Correct
     }
     assertEquals(localWavelet.getCurrentVersion().getVersion(), 2);
     assertTrue(localWavelet.isDeltaSigner(
         localWavelet.getCurrentVersion(), fakeSigner1));
     assertFalse(localWavelet.isDeltaSigner(
         localWavelet.getCurrentVersion(), fakeSigner2));
 
     HashedVersion oldVersion = localWavelet.getCurrentVersion();
     ProtocolSignedDelta rollbackDelta = ProtocolSignedDelta.newBuilder()
         .addSignature(fakeSignature1)
         .setDelta(ProtocolWaveletDelta.newBuilder(doubleRemoveParticipantProtoDelta(localWavelet))
             .setHashedVersion(serialize(localWavelet.getCurrentVersion()))
             .build().toByteString())
         .build();
     try {
       localWavelet.submitRequest(waveletName, rollbackDelta);
       fail("Should fail");
     } catch (OperationException e) {
       // Correct
     }
     assertEquals(localWavelet.getCurrentVersion(), oldVersion);
   }
 
   public void testLocalEmptyDelta() throws Exception {
     ProtocolSignedDelta emptyDelta = ProtocolSignedDelta.newBuilder()
         .addSignature(fakeSignature1)
         .setDelta(ProtocolWaveletDelta.newBuilder()
             .setAuthor(author.toString())
             .setHashedVersion(serialize(version0))
             .build().toByteString())
         .build();
     try {
       localWavelet.submitRequest(waveletName, emptyDelta);
       fail("Should fail");
     } catch (IllegalArgumentException e) {
       // Correct
     }
   }
 
   public void testOperationsOfDifferentSizes() throws Exception {
     String docId = "b+somedoc";
     BufferedDocOp docOp1 = new DocOpBuilder().characters("hi").build();
     WaveletDelta delta1 = createDelta(docId, docOp1, version0);
 
     applyDeltaToWavelet(localWavelet, delta1);
     try {
      BufferedDocOp docOp2 = new DocOpBuilder().characters("bye").build();
      WaveletDelta delta2 = createDelta(docId, docOp2, localWavelet.getCurrentVersion());

       applyDeltaToWavelet(localWavelet, delta2);
       fail("Composition of \"hi\" and \"bye\" did not throw OperationException");
     } catch (OperationException expected) {
       // Correct
     }
   }
 
   // Utilities
 
   /**
    * Returns a {@link WaveletDelta} for the list of operations performed by
    * the author set in the constants.
    */
   private WaveletDelta createDelta(String docId, BufferedDocOp docOp, HashedVersion version) {
     return new WaveletDelta(author, version, Arrays.asList(new WaveletBlipOperation(
         docId, new BlipContentOperation(CONTEXT, docOp))));
   }
 
   /**
    * Check that a container succeeds when adding non-existent participants and removing existing
    * participants.
    */
   private void assertSuccessfulApplyWaveletOperations(WaveletContainerImpl with) throws Exception {
     applyDeltaToWavelet(with, addParticipantDelta(with));
     assertEquals(with.getParticipants(), participants);
 
     applyDeltaToWavelet(with, removeParticipantDelta(with));
     assertEquals(with.getParticipants(), Collections.emptySet());
   }
 
   /**
    * Check that a container fails when removing non-existent participants and adding duplicate
    * participants, and that the partipant list is preserved correctly.
    */
   private void assertFailedWaveletOperations(WaveletContainerImpl with) throws Exception {
     try {
       applyDeltaToWavelet(with, removeParticipantDelta(with));
       fail("Should fail");
     } catch (OperationException e) {
       // Correct
     }
     assertEquals(localWavelet.getParticipants(), Collections.emptySet());
 
     applyDeltaToWavelet(with, addParticipantDelta(with));
     try {
       applyDeltaToWavelet(with, addParticipantDelta(with));
       fail("Should fail");
     } catch (OperationException e) {
       // Correct
     }
     assertEquals(with.getParticipants(), participants);
 
     try {
       applyDeltaToWavelet(with, doubleRemoveParticipantDelta(with));
       fail("Should fail");
     } catch (OperationException e) {
       // Correct
     }
     assertEquals(with.getParticipants(), participants);
   }
 
   /**
    * Applies and commits a delta to a wavelet container.
    */
   private static void applyDeltaToWavelet(WaveletContainerImpl wavelet, WaveletDelta delta)
       throws InvalidProtocolBufferException, OperationException {
     ProtocolWaveletDelta protoDelta = serialize(delta);
     ByteStringMessage<ProtocolWaveletDelta> deltaBytes =
         ByteStringMessage.serializeMessage(protoDelta);
     ProtocolSignedDelta signedDelta =
         ProtocolSignedDelta.newBuilder().setDelta(deltaBytes.getByteString()).build();
     ByteStringMessage<ProtocolAppliedWaveletDelta> appliedDelta =
         LocalWaveletContainerImpl.buildAppliedDelta(
             signedDelta, delta.getTargetVersion(), delta.size(), 0L);
     HashedVersion resultingVersion = HASH_FACTORY.create(
         appliedDelta.getByteArray(), delta.getTargetVersion(), delta.size());
     TransformedWaveletDelta transformedDelta =
         new TransformedWaveletDelta(delta.getAuthor(), resultingVersion, 0L, delta);
     WaveletDeltaRecord applicationResult = new WaveletDeltaRecord(appliedDelta, transformedDelta);
 
     // Apply the delta to the local wavelet state.
     wavelet.applyDelta(applicationResult);
   }
 
   private static WaveletDelta addParticipantDelta(WaveletContainer target) {
     return new WaveletDelta(author, target.getCurrentVersion(), addParticipantOps);
   }
 
   private static ProtocolWaveletDelta addParticipantProtoDelta(WaveletContainer target) {
     return serialize(addParticipantDelta(target));
   }
 
   private static WaveletDelta removeParticipantDelta(WaveletContainer target) {
     return new WaveletDelta(author, target.getCurrentVersion(), removeParticipantOps);
   }
 
   private static ProtocolWaveletDelta removeParticipantProtoDelta(WaveletContainer target) {
     return serialize(removeParticipantDelta(target));
   }
 
   private static WaveletDelta doubleRemoveParticipantDelta(WaveletContainer target) {
     return new WaveletDelta(author, target.getCurrentVersion(),
         doubleRemoveParticipantOps);
   }
 
   private static ProtocolWaveletDelta doubleRemoveParticipantProtoDelta(WaveletContainer target) {
     return serialize(doubleRemoveParticipantDelta(target));
   }
 
   private static ProtocolHashedVersion serialize(HashedVersion v) {
     return CoreWaveletOperationSerializer.serialize(v);
   }
 
   private static ProtocolWaveletDelta serialize(WaveletDelta d) {
     return CoreWaveletOperationSerializer.serialize(d);
   }
 }
