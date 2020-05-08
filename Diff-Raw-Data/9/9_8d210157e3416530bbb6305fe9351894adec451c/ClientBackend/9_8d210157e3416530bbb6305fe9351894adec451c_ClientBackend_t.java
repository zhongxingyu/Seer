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
 
 package org.waveprotocol.wave.examples.fedone.waveclient.common;
 
 import com.google.common.base.Preconditions;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.protobuf.RpcCallback;
 import com.google.protobuf.RpcController;
 
 import org.waveprotocol.wave.examples.fedone.common.CommonConstants;
 import org.waveprotocol.wave.examples.fedone.common.DocumentConstants;
 import org.waveprotocol.wave.examples.fedone.common.HashedVersion;
 import org.waveprotocol.wave.examples.fedone.common.WaveletOperationSerializer;
 import org.waveprotocol.wave.examples.fedone.model.util.HashedVersionZeroFactoryImpl;
 import org.waveprotocol.wave.examples.fedone.rpc.ClientRpcChannel;
 import org.waveprotocol.wave.examples.fedone.rpc.WebSocketClientRpcChannel;
 import org.waveprotocol.wave.examples.fedone.util.BlockingSuccessFailCallback;
 import org.waveprotocol.wave.examples.fedone.util.Log;
 import org.waveprotocol.wave.examples.fedone.util.SuccessFailCallback;
 import org.waveprotocol.wave.examples.fedone.util.URLEncoderDecoderBasedPercentEncoderDecoder;
 import org.waveprotocol.wave.examples.fedone.waveserver.WaveClientRpc.ProtocolOpenRequest;
 import org.waveprotocol.wave.examples.fedone.waveserver.WaveClientRpc.ProtocolSubmitRequest;
 import org.waveprotocol.wave.examples.fedone.waveserver.WaveClientRpc.ProtocolSubmitResponse;
 import org.waveprotocol.wave.examples.fedone.waveserver.WaveClientRpc.ProtocolWaveClientRpc;
 import org.waveprotocol.wave.examples.fedone.waveserver.WaveClientRpc.ProtocolWaveletUpdate;
 import org.waveprotocol.wave.federation.Proto.ProtocolWaveletDelta;
 import org.waveprotocol.wave.model.document.operation.Attributes;
 import org.waveprotocol.wave.model.document.operation.impl.DocOpBuilder;
 import org.waveprotocol.wave.model.id.IdURIEncoderDecoder;
 import org.waveprotocol.wave.model.id.URIEncoderDecoder.EncodingException;
 import org.waveprotocol.wave.model.id.WaveId;
 import org.waveprotocol.wave.model.id.WaveletName;
 import org.waveprotocol.wave.model.operation.OperationException;
 import org.waveprotocol.wave.model.operation.wave.AddParticipant;
 import org.waveprotocol.wave.model.operation.wave.NoOp;
 import org.waveprotocol.wave.model.operation.wave.RemoveParticipant;
 import org.waveprotocol.wave.model.operation.wave.WaveletDelta;
 import org.waveprotocol.wave.model.operation.wave.WaveletDocumentOperation;
 import org.waveprotocol.wave.model.operation.wave.WaveletOperation;
 import org.waveprotocol.wave.model.util.Pair;
 import org.waveprotocol.wave.model.wave.ParticipantId;
 import org.waveprotocol.wave.model.wave.data.WaveletData;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.TimeUnit;
 import java.util.logging.Handler;
 import java.util.logging.SimpleFormatter;
 import java.util.logging.StreamHandler;
 
 /**
  * The "backend" of a basic wave client, designed for user interfaces to interact with.
  *
  *
  */
 public class ClientBackend {
 
   /**
    * Container for data to {@code WaveletOperationListener}s on events.
    */
   private static class WaveletEventData {
 
     private final String author;
     private final WaveletData waveletData;
     private final WaveletOperation waveletOperation;
     // Hack to mark whether this is actually the "end of a delta sequence", in which case the
     // above data is irrelevant.
     private final boolean isDeltaSequenceEnd;
 
     /**
      * Standard constructor.
      */
     WaveletEventData(String author, WaveletData waveletData, WaveletOperation waveletOperation) {
       this.author = author;
       this.waveletData = waveletData;
       this.waveletOperation = waveletOperation;
       this.isDeltaSequenceEnd = false;
     }
 
     /**
      * Constructor to set this to a delta sequence end marker.
      */
     WaveletEventData(WaveletData waveletData) {
       this.author = null;
       this.waveletData = waveletData;
       this.waveletOperation = null;
       this.isDeltaSequenceEnd = true;
     }
 
     String getAuthor() {
       return author;
     }
 
     WaveletData getWaveletData() {
       return waveletData;
     }
 
     WaveletOperation getWaveletOperation() {
       return waveletOperation;
     }
 
     boolean isDeltaSequenceEnd() {
       return isDeltaSequenceEnd;
     }
   }
 
   private static final Log LOG = Log.get(ClientBackend.class);
   private static final ByteArrayOutputStream logOutput = new ByteArrayOutputStream();
   static {
     LOG.getLogger().addHandler(new StreamHandler(logOutput, new SimpleFormatter()));
     LOG.getLogger().setUseParentHandlers(false);
   }
 
   /** User id of the user of the backend (encapsulating both user and server). */
   private final ParticipantId userId;
 
   /** Waves this backend is aware of. */
   private final Map<WaveId, ClientWaveView> waves = Maps.newHashMap();
 
   /** RPC controllers for the open wave connections. */
   private final Map<WaveId, RpcController> waveControllers = Maps.newHashMap();
 
   /** Listeners waiting on wave updates. */
   private final List<WaveletOperationListener> waveletOperationListeners = Lists.newArrayList();
 
   /** Id generator used for this (server, user) pair. */
   private final ClientIdGenerator idGenerator;
 
   /** Id URI encoder and decoder. */
   private final IdURIEncoderDecoder uriCodec;
 
   /** RPC stub for communicating with server. */
   private final ProtocolWaveClientRpc.Stub rpcServer;
 
   /** RPC channel for communicating with server. */
   private final ClientRpcChannel rpcChannel;
 
   /** Producer/consumer event queue. */
   private final BlockingQueue<WaveletEventData> eventQueue =
       new LinkedBlockingQueue<WaveletEventData>();
 
   /**
    * Create new client backend tied permanently to a given server and user, open that client's
    * index, and begin managing waves it has access to.
    *
    * @param userAtDomain the user and their domain (for example, foo@bar.org)
    * @param server the server to connect to (for example, acmewave.com)
    * @param port port to connect to server with
    * @throws IOException if we can't connect to the server
    */
   public ClientBackend(String userAtDomain, String server, int port) throws IOException {
     if (userAtDomain.split("@").length != 2) {
       throw new IllegalArgumentException("userAtName must be in form user@domain");
     }
 
     this.userId = new ParticipantId(userAtDomain);
     this.idGenerator = new RandomIdGenerator(userId.getDomain());
     this.uriCodec = new IdURIEncoderDecoder(new URLEncoderDecoderBasedPercentEncoderDecoder());
    this.rpcChannel = new ClientRpcChannel(new InetSocketAddress(server, port));
     this.rpcServer = ProtocolWaveClientRpc.newStub(rpcChannel);
 
     // Opening the index wave will kickstart the process of receiving waves
     openWave(CommonConstants.INDEX_WAVE_ID, "");
 
     // Start pushing events to listeners in a separate thread.
     new Thread() {
       @Override
       public void run() {
         for (;;) {
           try {
             WaveletEventData nextEvent = eventQueue.take();
             if (nextEvent.isDeltaSequenceEnd()) {
               for (WaveletOperationListener listener : waveletOperationListeners) {
                 listener.onDeltaSequenceEnd(nextEvent.getWaveletData());
               }
             } else {
               notifyWaveletOperationListeners(nextEvent.getAuthor(), nextEvent.getWaveletData(),
                   nextEvent.getWaveletOperation());
             }
           } catch (InterruptedException e) {
             // TODO: stop?
           }
         }
       }
     }.start();
   }
 
   /**
    * @return the client backend log.
    */
   public static String getLog() {
     for (Handler handler : LOG.getLogger().getHandlers()) {
       handler.flush();
     }
     return logOutput.toString();
   }
 
   /**
    * Clear the log.
    */
   public static void clearLog() {
     logOutput.reset();
   }
 
   /**
    * Gracefully shut down the backend, closing all connections to the server and clearing the
    * collection of waves (since they will no longer be valid).  This renders the backend relatively
    * useless since no more updates from the index wave will be received, but it is still valid.
    */
   public void shutdown() {
     for (RpcController rpcController : waveControllers.values()) {
       LOG.info("Cancelling RpcController " + rpcController);
       rpcController.startCancel();
     }
 
     waves.clear();
     waveControllers.clear();
   }
 
   /**
    * Open a wave.  This method will return immediately and updates will be delivered internally
    * from the RPC interface, and externally to {@link WaveletOperationListener}s.
    *
    * @param waveId of wave to open
    * @param waveletIdPrefix filter such that the server will send wavelet updates for ids that
    * match any of this prefix
    */
   private void openWave(WaveId waveId, String waveletIdPrefix) {
     if (waveControllers.containsKey(waveId)) {
       throw new IllegalArgumentException(waveId + " is already open");
     } else {
       // May already be there if created with createNewWave
       if (!waves.containsKey(waveId)) {
         createWave(waveId);
       }
     }
 
     ProtocolOpenRequest.Builder openRequest = ProtocolOpenRequest.newBuilder();
 
     openRequest.setParticipantId(getUserId().getAddress());
     openRequest.setWaveId(waveId.serialise());
     openRequest.addWaveletIdPrefix(waveletIdPrefix);
 
     final RpcController rpcController = rpcChannel.newRpcController();
     waveControllers.put(waveId, rpcController);
 
     LOG.info("Opening wave " + waveId + " for prefix \"" + waveletIdPrefix + '"');
     rpcServer.open(
         rpcController,
         openRequest.build(),
         new RpcCallback<ProtocolWaveletUpdate>() {
           @Override public void run(ProtocolWaveletUpdate update) {
             if (update == null) {
               LOG.warning("RPC failed: " + rpcController.errorText());
             } else {
               receiveWaveletUpdate(update);
             }
           }
         }
     );
   }
 
   /**
    * Create a new conversation wave and tell the server about it, by adding ourselves as a
    * participant on the conversation root.
    *
    * @param callback callback invoked when the server rpc is complete
    * @return the {@link ClientWaveView} created
    */
   public ClientWaveView createConversationWave(
       SuccessFailCallback<ProtocolSubmitResponse, String> callback) {
     return createConversationWave(getIdGenerator().newWaveId(), callback);
   }
 
   /**
    * Create a new conversation wave with a given wave id and tell the server about it, by adding
    * ourselves as a participant on the conversation root.
    *
    * @param newWaveId the id to give the new wave
    * @param callback callback invoked when the server rpc is complete
    * @return the {@link ClientWaveView} created
    */
   private ClientWaveView createConversationWave(WaveId newWaveId,
       SuccessFailCallback<ProtocolSubmitResponse, String> callback) {
     ClientWaveView waveView = createWave(newWaveId);
     WaveletData convRoot = waveView.createWavelet(getIdGenerator().newConversationRootWaveletId());
 
     // Add ourselves in the first operation
     AddParticipant addUserOp = new AddParticipant(getUserId());
 
     // Create a document manifest in the second operation
     WaveletDocumentOperation addManifestOp = new WaveletDocumentOperation(
         DocumentConstants.MANIFEST_DOCUMENT_ID,
         new DocOpBuilder()
             .elementStart(DocumentConstants.CONVERSATION, Attributes.EMPTY_MAP)
             .elementEnd().build());
 
     sendWaveletDelta(convRoot.getWaveletName(),
         new WaveletDelta(getUserId(), ImmutableList.of(addUserOp, addManifestOp)), callback);
 
     return waveView;
   }
 
   /**
    * @param id of wave to get
    * @return wave with the given id, or null if not found
    */
   public ClientWaveView getWave(WaveId id) {
     return waves.get(id);
   }
 
   /**
    * @return the special wave containing the index data
    */
   public ClientWaveView getIndexWave() {
     return getWave(CommonConstants.INDEX_WAVE_ID);
   }
 
   /**
    * Send a single wavelet operation over the wire.
    *
    * @param waveletName of the wavelet to apply the operation to
    * @param op to send
    * @param callback callback invoked when the server rpc is complete
    */
   public void sendWaveletOperation(WaveletName waveletName, WaveletOperation op,
       SuccessFailCallback<ProtocolSubmitResponse, String> callback) {
     sendWaveletDelta(waveletName, new WaveletDelta(getUserId(), ImmutableList.of(op)), callback);
   }
 
   /**
    * Send a single wavelet operation over the wire and wait for the roundtrip success: the submit
    * callback from our local server, and the wavelet to be updated to the version given in the
    * response.
    *
    * @param waveletName of the wavelet to apply the operation to
    * @param op to send
    * @param timeout used twice, so real timeout may be up to twice that specified
    * @param unit of timeout
    * @return true if the roundtrip trip was successful, false otherwise
    */
   public boolean sendAndAwaitWaveletOperation(WaveletName waveletName, WaveletOperation op,
       long timeout, TimeUnit unit) {
     return sendAndAwaitWaveletDelta(waveletName, new WaveletDelta(getUserId(),
         ImmutableList.of(op)), timeout, unit);
   }
 
   /**
    * Send a wavelet delta over the wire.
    *
    * @param waveletName of the wavelet that the delta applies to
    * @param delta to send
    * @param callback callback invoked when the server rpc is complete
    */
   public void sendWaveletDelta(WaveletName waveletName, WaveletDelta delta,
       final SuccessFailCallback<ProtocolSubmitResponse, String> callback) {
     // Build the submit request
     ProtocolSubmitRequest.Builder submitRequest = ProtocolSubmitRequest.newBuilder();
 
     try {
       submitRequest.setWaveletName(uriCodec.waveletNameToURI(waveletName));
     } catch (EncodingException e) {
       throw new IllegalArgumentException(e);
     }
 
     ClientWaveView wave = waves.get(waveletName.waveId);
     submitRequest.setDelta(WaveletOperationSerializer.serialize(delta,
         wave.getWaveletVersion(waveletName.waveletId)));
 
     final RpcController rpcController = rpcChannel.newRpcController();
 
     LOG.info("Sending delta " + delta + " for " + waveletName);
     rpcServer.submit(rpcController, submitRequest.build(),
         new RpcCallback<ProtocolSubmitResponse>() {
           @Override
           public void run(ProtocolSubmitResponse response) {
             if (response == null) {
               callback.onFailure("null response");
             } else if (rpcController.failed()) {
               callback.onFailure(rpcController.errorText());
             } else {
               callback.onSuccess(response);
             }
           }
         });
   }
 
   /**
    * Send a wavelet delta over the wire and wait for the roundtrip success: the submit callback
    * from our local server, and the wavelet to be updated to the version given in the response.
    *
    * @param waveletName of the wavelet that the delta applies to
    * @param delta to send
    * @param timeout used twice, so real timeout may be up to twice that specified
    * @param unit of timeout
    * @return true if the roundtrip trip was successful, false otherwise
    */
   public boolean sendAndAwaitWaveletDelta(WaveletName waveletName, WaveletDelta delta,
       long timeout, TimeUnit unit) {
     BlockingSuccessFailCallback<ProtocolSubmitResponse, String> callback =
         BlockingSuccessFailCallback.create();
     sendWaveletDelta(waveletName, delta, callback);
     Pair<ProtocolSubmitResponse, String> result = callback.await(timeout, unit);
     if (result == null) {
       LOG.warning("Error: submit result pair for " + waveletName + " was null, timed out?");
       return false;
     } else if (result.getFirst() == null) {
       LOG.warning("Error: submit response to " + waveletName + " was null: " + result.getSecond());
       return false;
     } else {
       return getWave(waveletName.waveId).awaitWaveletVersion(waveletName.waveletId,
           result.getFirst().getHashedVersionAfterApplication().getVersion(), timeout, unit);
     }
   }
 
   /**
    * Receive a protocol wavelet update from the wave server.
    *
    * @param waveletUpdate the wavelet update
    */
   public void receiveWaveletUpdate(ProtocolWaveletUpdate waveletUpdate) {
     LOG.info("Received update " + waveletUpdate);
 
     WaveletName waveletName;
     try {
       waveletName = uriCodec.uriToWaveletName(waveletUpdate.getWaveletName());
     } catch (EncodingException e) {
       throw new IllegalArgumentException(e);
     }
 
     ClientWaveView wave = waves.get(waveletName.waveId);
     if (wave == null) {
       // The wave view should always be present, since openWave adds them immediately.
       throw new AssertionError("Received update on absent waveId " + waveletName.waveId);
     }
 
     WaveletData wavelet = wave.getWavelet(waveletName.waveletId);
     if (wavelet == null) {
       wavelet = wave.createWavelet(waveletName.waveletId);
     }
 
     if (waveletUpdate.hasCommitNotice()) {
       Preconditions.checkArgument(waveletUpdate.getAppliedDeltaList().isEmpty());
       Preconditions.checkArgument(!waveletUpdate.hasResultingVersion());
 
       for (WaveletOperationListener listener : waveletOperationListeners) {
         listener.onCommitNotice(wavelet, WaveletOperationSerializer
                 .deserialize(waveletUpdate.getCommitNotice()));
       }
     } else {
       Preconditions.checkArgument(waveletUpdate.hasResultingVersion());
       Preconditions.checkArgument(!waveletUpdate.getAppliedDeltaList().isEmpty());
 
       // Apply operations to the wavelet.
       List<Pair<String, WaveletOperation>> successfulOps = Lists.newArrayList();
 
       for (ProtocolWaveletDelta protobufDelta : waveletUpdate.getAppliedDeltaList()) {
         Pair<WaveletDelta, HashedVersion> deltaAndVersion =
           WaveletOperationSerializer.deserialize(protobufDelta);
         List<WaveletOperation> ops = deltaAndVersion.first.getOperations();
 
         for (WaveletOperation op : ops) {
           try {
             op.apply(wavelet);
             successfulOps.add(Pair.of(protobufDelta.getAuthor(), op));
           } catch (OperationException e) {
             // It should be okay (if cheeky) for the client to just ignore failed ops.  In any case,
             // this should never happen if our server is behaving correctly.
             LOG.severe("OperationException when applying " + op + " to " + wavelet);
           }
         }
       }
 
       wave.setWaveletVersion(waveletName.waveletId, WaveletOperationSerializer
           .deserialize(waveletUpdate.getResultingVersion()));
 
       // If we have been removed from this wavelet then remove the data too, since if we're re-added
       // then the deltas will come from version 0, not the latest version we've seen.
       if (!wavelet.getParticipants().contains(getUserId())) {
         wave.removeWavelet(waveletName.waveletId);
       }
 
       // If it was an update to the index wave, might need to open/close some more waves.
       if (wave.getWaveId().equals(CommonConstants.INDEX_WAVE_ID)) {
         syncWithIndexWave(wave);
       }
 
       // Push the events to the event queue.
       for (Pair<String, WaveletOperation> authorAndOp : successfulOps) {
         eventQueue.offer(new WaveletEventData(authorAndOp.first, wavelet, authorAndOp.second));
       }
       eventQueue.offer(new WaveletEventData(wavelet));
     }
   }
 
   /**
    * Creates a new, empty wave view and stores it in {@code waves}.
    * @param waveId the new wave id
    * @return the new wave's {@code ClientWaveView}
    */
   private ClientWaveView createWave(WaveId waveId) {
     ClientWaveView wave = new ClientWaveView(new HashedVersionZeroFactoryImpl(), waveId);
     waves.put(waveId, wave);
     return wave;
   }
 
   /**
    * Synchronise with the index wave by opening any waves that appear in the index but that we
    * don't have an RPC open request to.
    *
    * @param indexWave to synchronise with
    */
   private void syncWithIndexWave(ClientWaveView indexWave) {
     List<IndexEntry> indexEntries = ClientUtils.getIndexEntries(indexWave);
 
     for (IndexEntry indexEntry : indexEntries) {
       if (!waveControllers.containsKey(indexEntry.getWaveId())) {
         WaveId waveId = indexEntry.getWaveId();
         openWave(waveId, ClientUtils.getConversationRootId(waveId).serialise());
       }
     }
   }
 
   /**
    * Notify all wavelet operation listeners of a wavelet operation.
    *
    * @param author the author who caused the operation
    * @param wavelet operated on
    * @param op the operation
    */
   private void notifyWaveletOperationListeners(String author, WaveletData wavelet, WaveletOperation op) {
     for (WaveletOperationListener listener : waveletOperationListeners) {
       try {
         if (op instanceof WaveletDocumentOperation) {
           listener.waveletDocumentUpdated(author, wavelet, (WaveletDocumentOperation) op);
         } else if (op instanceof AddParticipant) {
           listener.participantAdded(author, wavelet, ((AddParticipant) op).getParticipantId());
         } else if (op instanceof RemoveParticipant) {
           listener.participantRemoved(author, wavelet, ((RemoveParticipant) op).getParticipantId());
         } else if (op instanceof NoOp) {
           listener.noOp(author, wavelet);
         }
       } catch (RuntimeException e) {
         LOG.severe("RuntimeException for listener " + listener, e);
       }
     }
   }
 
   /**
    * @return the {@link ParticipantId} id of the user
    */
   public ParticipantId getUserId() {
     return userId;
   }
 
   /**
    * @return the id generator which generates wave, wavelet, and document ids
    */
   public ClientIdGenerator getIdGenerator() {
     return idGenerator;
   }
 
   /**
    * Add a {@link WaveletOperationListener} to be notified whenever a wave is updated.
    *
    * @param listener new listener
    */
   public void addWaveletOperationListener(WaveletOperationListener listener) {
     waveletOperationListeners.add(listener);
   }
 
   /**
    * @param listener listener to be removed
    */
   public void removeWaveletOperationListener(WaveletOperationListener listener) {
     waveletOperationListeners.remove(listener);
   }
 }
