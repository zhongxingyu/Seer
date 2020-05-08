 package by.muna.mt;
 
 import by.muna.mt.by.muna.mt.keys.MTAuthKey;
 import by.muna.mt.crypto.Encryption;
 import by.muna.mt.crypto.Hashes;
 import by.muna.mt.logging.IMTClientLogger;
 import by.muna.mt.messages.IMTMessageStatusListener;
 import by.muna.mt.storage.ISeqNoPoller;
 import by.muna.mt.storage.IMTStorage;
 import by.muna.mt.tl.*;
 import by.muna.tl.*;
 import by.muna.util.BufferUtil;
 import by.muna.util.BytesUtil;
 import by.muna.util.ChecksumUtil;
 import by.muna.util.StringUtil;
 import by.muna.yasly.*;
 
 import java.net.InetSocketAddress;
 import java.nio.ByteBuffer;
 import java.security.SecureRandom;
 import java.util.*;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 public class MTClient {
     public static class AuthKeySessionPair {
         private long authKeyId;
         private long sessionId;
 
         public AuthKeySessionPair(long authKeyId, long sessionId) {
             this.authKeyId = authKeyId;
             this.sessionId = sessionId;
         }
 
         public long getAuthKeyId() {
             return this.authKeyId;
         }
 
         public long getSessionId() {
             return this.sessionId;
         }
     }
     private static class UndeliveredMessage {
         private TLValue toSend;
         private boolean meaningful;
         private long authKeyId, sessionId;
         private long messageId;
 
         private IMTMessageStatusListener statusListener;
 
         public UndeliveredMessage(
             long authKeyId, long sessionId,
             long messageId, TLValue toSend, boolean meaningful,
             IMTMessageStatusListener statusListener)
         {
             this.authKeyId = authKeyId;
             this.sessionId = sessionId;
             this.messageId = messageId;
             this.toSend = toSend;
             this.meaningful = meaningful;
 
             this.statusListener = statusListener;
         }
 
         public void constructed(long messageId) {
             this.messageId = messageId;
 
             this.statusListener.onConstructed(this.messageId);
         }
 
         public void sent() {
             this.statusListener.onSent(this.messageId);
         }
 
         public void connectError() {
             this.statusListener.onConnectionError(this.messageId);
         }
 
         public void delivered() {
             this.statusListener.onDelivered(this.messageId);
         }
 
         public TLValue getToSend() {
             return this.toSend;
         }
 
         public boolean isMeaningful() {
             return this.meaningful;
         }
 
         public long getAuthKeyId() {
             return this.authKeyId;
         }
 
         public long getSessionId() {
             return this.sessionId;
         }
     }
 
     public final ConstructorProviders schema = new ConstructorProviders();
     {
         this.schema.addProvider(MTTL.SCHEMA);
     }
 
     private SocketThread socketThread;
     private SocketController socketController;
 
     private Timer timer = new Timer();
     private TimerTask ackTimerTask;
 
     private InetSocketAddress address;
 
     private IMTStorage storage;
     private IMTClientLogger logger;
 
     private IMTConnectionListener connectListener;
     private IMTDataListener dataListener;
 
     private boolean readingSize = true;
     private ByteBuffer sizeBuffer = BufferUtil.allocateLE(4);
     private ByteBuffer bodyBuffer;
 
     private Map<Long, MTAuthKey> authKeys = new HashMap<Long, MTAuthKey>(1);
     private MTAuthKeyGenerators authKeyGenerators = new MTAuthKeyGenerators();
 
     private Map<Long, Map<Long, Queue<MTProtoMessage>>> sendQueues =
         new HashMap<Long, Map<Long, Queue<MTProtoMessage>>>(1);
     private Queue<MTProtoMessage> unencryptedSendQueue = new ConcurrentLinkedQueue<MTProtoMessage>();
     private Queue<AuthKeySessionPair> sendQueue = new ConcurrentLinkedQueue<AuthKeySessionPair>();
 
     private Map<Long, Map<Long, Queue<Long>>> acks = new HashMap<Long, Map<Long, Queue<Long>>>();
 
     private Map<Long, UndeliveredMessage> undeliveredMessages = new HashMap<Long, UndeliveredMessage>();
     private Map<Long, Long> containerDeliverDependencies = new HashMap<Long, Long>();
 
     private long lastMessageId = 0;
     private int packetNo = 0;
     private int serverPacketNo = 0;
 
     public MTClient(SocketThread socketThread) {
         this.socketThread = socketThread;
     }
 
     public void setStorage(IMTStorage storage) {
         this.storage = storage;
     }
     public IMTStorage getStorage() {
         return this.storage;
     }
 
     public void setLogger(IMTClientLogger logger) {
         this.logger = logger;
     }
 
     public void addSchema(IConstructorProvider schema) {
         this.schema.addProvider(schema);
     }
 
     public long addAuthKey(MTAuthKey key) {
         long authKeyId = key.getAuthKeyId();
 
         if (this.authKeys.containsKey(authKeyId)) return authKeyId;
 
         this.authKeys.put(authKeyId, key);
 
         this.sendQueues.put(authKeyId, new HashMap<Long, Queue<MTProtoMessage>>());
         this.acks.put(authKeyId, new HashMap<Long, Queue<Long>>());
 
         return authKeyId;
     }
 
     private long pollMessageId() {
         long time = System.currentTimeMillis() + this.storage.getTimeDiff() * 1000;
 
         long id = (
             ((time / 1000) << 32) + // 32 bits
                 (((time % 1000) * 256 / 1000) << 24) + // 8 bits
                 MTClient.getRandom().nextInt(16777216) // from 0 to 2^(6*4), rest bits
         ) & 0xfffffffffffffffcL; // nullify last 2 bits
 
         if (id <= this.lastMessageId) {
             id = this.lastMessageId + 4;
         }
 
         this.lastMessageId = id;
 
         return id;
     }
 
     public void setConnectionListener(IMTConnectionListener connectListener) {
         this.connectListener = connectListener;
     }
 
     public void setOnData(IMTDataListener dataListener) {
         this.dataListener = dataListener;
     }
 
     public void send(long authKeyId, long sessionId, ITypedData data, IMTMessageStatusListener messageStatusListener) {
         this.send(authKeyId, sessionId, data, true, messageStatusListener);
     }
     public void send(
         long authKeyId, long sessionId,
         ITypedData data, boolean meaningful, IMTMessageStatusListener messageStatusListener)
     {
         MTProtoMessage protoMessage = new MTProtoMessage(data, meaningful, messageStatusListener);
 
         if (authKeyId != 0) {
             Map<Long, Queue<MTProtoMessage>> sessionsMap = this.sendQueues.get(authKeyId);
             Queue<MTProtoMessage> sendQueue = sessionsMap.get(sessionId);
 
             if (sendQueue == null) {
                 sendQueue = new ConcurrentLinkedQueue<MTProtoMessage>();
                 sessionsMap.put(sessionId, sendQueue);
             }
 
             sendQueue.add(protoMessage);
 
             this.sendQueue.add(new AuthKeySessionPair(authKeyId, sessionId));
         } else {
             this.unencryptedSendQueue.add(protoMessage);
             this.sendQueue.add(new AuthKeySessionPair(0, 0));
         }
 
         final Queue<MTProtoMessage> messagesToSend = new LinkedList<MTProtoMessage>();
 
         this.socketController.send(new IBufferProvider() {
             @Override
             public ByteBuffer getBuffer() {
                 int count = 0;
                 AuthKeySessionPair pair = null;
 
                 while (!MTClient.this.sendQueue.isEmpty()) {
                     pair = MTClient.this.sendQueue.poll();
 
                     Queue<MTProtoMessage> candidate;
 
                     if (pair.getAuthKeyId() != 0) {
                         candidate = MTClient.this.sendQueues.get(pair.getAuthKeyId()).get(pair.getSessionId());
                     } else {
                         candidate = MTClient.this.unencryptedSendQueue;
                     }
 
                     if (!candidate.isEmpty()) {
                         while (!candidate.isEmpty()) {
                             messagesToSend.add(candidate.poll());
                             count++;
                         }
 
                         break;
                     }
                 }
 
                 if (count > 0) {
                     long authKeyId = pair.getAuthKeyId();
                     long sessionId = pair.getSessionId();
 
                     ByteBuffer buffer;
 
                     if (authKeyId != 0) {
                         TLValue toSend;
                         boolean meaningful;
 
                         ISeqNoPoller seqNoPoller = MTClient.this.storage.getSeqNoPoller(authKeyId, sessionId);
                         final List<Long> containerMeaningfulMessages = new LinkedList<Long>();
 
                         boolean haveMeaningfulForContainer = false;
 
                         for (MTProtoMessage message : messagesToSend) {
                             if (message.isMeaningful() || MTClient.isMeaningfulForContainer(message.getData())) {
                                 haveMeaningfulForContainer = true;
                                 break;
                             }
                         }
 
                         if (count > 1 || !haveMeaningfulForContainer) {
                             ITypedData[] messages = new ITypedData[count + (haveMeaningfulForContainer ? 0 : 1)];
 
                             int i = 0;
                             for (MTProtoMessage message : messagesToSend) {
                                 ITypedData messageData = message.getData();
 
                                 long messageId = MTClient.this.pollMessageId();
                                 int seqNo = seqNoPoller.pollSeqNo(message.isMeaningful());
 
                                 message.setMessageId(messageId);
                                 message.getStatusListener().onConstructed(messageId);
 
                                 if (message.isMeaningful()) {
                                     containerMeaningfulMessages.add(messageId);
                                 }
 
                                 ITypedData messageTl = new TypedData(MTMessage.CONSTRUCTOR)
                                     .setTypedData(MTMessage.msgId, messageId)
                                     .setTypedData(MTMessage.seqno, seqNo)
                                     .setTypedData(MTMessage.bytes,
                                         new TL(
                                             messageData.getConstructor().getType(),
                                             messageData
                                         ).calcSize())
                                     .setTypedData(MTMessage.body, messageData);
 
                                 messages[i++] = messageTl;
                             }
 
                             if (!haveMeaningfulForContainer) {
                                 long messageId = MTClient.this.pollMessageId();
 
                                 containerMeaningfulMessages.add(messageId);
 
                                 ITypedData pingBody = new TypedData(MTPing.CONSTRUCTOR)
                                     .setTypedData(MTPing.pingId, 0L);
 
                                 messages[count] = new TypedData(MTMessage.CONSTRUCTOR)
                                     .setTypedData(MTMessage.msgId, messageId)
                                     .setTypedData(MTMessage.seqno, seqNoPoller.pollSeqNo(false))
                                     .setTypedData(MTMessage.bytes,
                                         new TL(
                                             MTPing.TYPE,
                                             pingBody
                                         ).calcSize())
                                     .setTypedData(MTMessage.body, pingBody);
                             }
 
                             toSend = new TL(MTMsgContainer.TYPE, new TypedData(MTMsgContainer.CONSTRUCTOR)
                                 .setTypedData(MTMsgContainer.messages, new TypedData(MTVector.CONSTRUCTOR_MESSAGE)
                                     .setTypedData(0, messages))
                             );
 
                             meaningful = false;
                         } else {
                             MTProtoMessage message = messagesToSend.peek();
 
                             ITypedData messageData = message.getData();
                             toSend = new TL(messageData.getConstructor().getType(), messageData);
 
                             meaningful = message.isMeaningful();
                         }
 
                         MTAuthKey authKey = MTClient.this.authKeys.get(authKeyId);
 
                         long serverSalt = MTClient.this.storage.getServerSalt(authKeyId, sessionId);
                         long messageId = MTClient.this.pollMessageId();
                         int seqNo = seqNoPoller.pollSeqNo(meaningful);
 
                         if (count == 1 && haveMeaningfulForContainer) {
                             MTProtoMessage message = messagesToSend.peek();
 
                             message.setMessageId(messageId);
                             message.getStatusListener().onConstructed(messageId);
                         }
 
                         int toSendSize = toSend.calcSize();
 
                         int innerDataLength = 8 + 8 + 8 + 4 + 4 + toSendSize;
                         int padding = (16 - innerDataLength % 16) % 16;
 
                         byte[] serialized = new byte[innerDataLength + padding];
 
                         ByteBuffer innerBuffer = BufferUtil.wrapLE(serialized);
                         innerBuffer.putLong(serverSalt);
                         innerBuffer.putLong(sessionId);
                         innerBuffer.putLong(messageId);
                         innerBuffer.putInt(seqNo);
                         innerBuffer.putInt(toSendSize);
 
                         MTClient.this.logger.onSend(MTClient.this, authKeyId, sessionId, messageId, seqNo, toSend);
 
                         toSend.serialize(innerBuffer);
 
                         // random
                         /*int i = innerDataLength;
                         for (byte b : MTClient.getRandomBytes(padding)) {
                             serialized[i++] = b;
                         }*/
 
                         byte[] messageKey = Encryption.calcMessageKey(serialized, innerDataLength);
 
                         byte[] encrypted = Encryption.encrypt(authKey, messageKey, serialized);
 
                         int totalLength = 4 + 4 + 8 + 16 + encrypted.length + 4;
 
                         buffer = BufferUtil.allocateLE(totalLength);
 
                         buffer.putInt(totalLength);
                         buffer.putInt(MTClient.this.packetNo++);
 
                         buffer.putLong(authKeyId);
                         buffer.put(messageKey);
                         buffer.put(encrypted);
 
                         int crc32 = ChecksumUtil.CRC32(buffer.array(), buffer.arrayOffset(), totalLength - 4);
                         buffer.putInt(crc32);
 
                         for (long msgId : containerMeaningfulMessages) {
                             // map container messages to container messageId
                             MTClient.this.containerDeliverDependencies.put(msgId, messageId);
                         }
 
                         MTClient.this.undeliveredMessages.put(messageId, new UndeliveredMessage(
                             authKeyId, sessionId, messageId, toSend, meaningful,
                             new IMTMessageStatusListener() {
                                 @Override
                                 public void onConstructed(long messageId) {
                                     for (MTProtoMessage message : messagesToSend) {
                                         message.getStatusListener().onConstructed(messageId);
                                     }
                                 }
 
                                 @Override
                                 public void onSent(long messageId) {
                                     for (MTProtoMessage message : messagesToSend) {
                                         message.getStatusListener().onSent(messageId);
                                     }
                                 }
 
                                 @Override
                                 public void onDelivered(long messageId) {
                                     for (MTProtoMessage message : messagesToSend) {
                                         message.getStatusListener().onDelivered(messageId);
                                     }
                                 }
 
                                 @Override
                                 public void onConnectionError(long messageId) {
                                     for (MTProtoMessage message : messagesToSend) {
                                         message.getStatusListener().onConnectionError(messageId);
                                     }
                                 }
                             }
                         ));
                     } else {
                         // not encrypted messages
                         // tcp transport wrapper. length, packet number, crc32
                         int totalLength = 0;
 
                         int i = 0;
                         // FIXME: temporary solution
                         MTProtoMessage[] messagesArray = new MTProtoMessage[count];
 
                         TLValue[] boxedValues = new TLValue[count];
                         int[] packetSizes = new int[count];
                         int[] valuesSizes = new int[count];
                         for (MTProtoMessage message : messagesToSend) {
                             ITypedData messageData = message.getData();
 
                             messagesArray[i] = message;
 
                             boxedValues[i] = new TL(messageData.getConstructor().getType(), messageData);
                             valuesSizes[i] = boxedValues[i].calcSize();
                             packetSizes[i] = 4 + 4 + // packet length, packet no
                                 // authKeyId, messageId, length, data
                                 8 + 8 + 4 + valuesSizes[i] +
                                 4; // crc32
                             totalLength += packetSizes[i];
 
                             i++;
                         }
 
                         buffer = BufferUtil.allocateLE(totalLength);
 
                         for (i = 0; i < boxedValues.length; i++) {
                             int preWritePosition = buffer.position();
 
                             long messageId = MTClient.this.pollMessageId();
 
                             messagesArray[i].setMessageId(messageId);
                             messagesArray[i].getStatusListener().onConstructed(messageId);
 
                             buffer.putInt(packetSizes[i]);
                             buffer.putInt(MTClient.this.packetNo++);
 
                             buffer.putLong(0L); // auth_key
                             buffer.putLong(messageId);
                             buffer.putInt(valuesSizes[i]);
 
                             MTClient.this.logger.onSend(MTClient.this, 0, 0, messageId, 0, boxedValues[i]);
 
                             boxedValues[i].serialize(buffer);
 
                             int crc32 = ChecksumUtil.CRC32(
                                 buffer.array(),
                                 buffer.arrayOffset() + preWritePosition,
                                 packetSizes[i] - 4
                             );
 
                             buffer.putInt(crc32);
                         }
                     }
 
                     buffer.position(0);
 
                     return buffer;
                 } else {
                     return null;
                 }
             }
         }, new ISendStatusListener() {
             @Override
             public void onSent() {
                 for (MTProtoMessage message : messagesToSend) {
                     message.getStatusListener().onSent(message.getMessageId());
                 }
             }
 
             @Override
             public void onConnectError(boolean gracefully) {
                 for (MTProtoMessage message : messagesToSend) {
                     message.getStatusListener().onConnectionError(message.getMessageId());
                 }
             }
         });
     }
 
     public static boolean isMeaningfulForContainer(ITypedData data) {
         // if server will reply for this message type,
         // it will mean, that container will be confirmed,
         // and we can notify all messages about successful delivery.
 
         switch (data.getId()) {
         case MTPing.CONSTRUCTOR_ID:
             return true;
         }
 
         return false;
     }
 
     public void connect(InetSocketAddress address) {
         this.address = address;
 
         this.socketController = this.socketThread.connect(this.address);
 
         this.socketController.setConnectionListener(new IConnectionListener() {
             @Override public void onConnected(SocketController controller) {
                 MTClient.this.connectListener.onConnected(MTClient.this);
             }
 
             @Override
             public void onConnectError(SocketController controller, boolean graceful) {
                 MTClient.this.connectListener.onConnectError(MTClient.this, graceful);
             }
         });
 
         this.socketController.setOnData(new IDataListener() {
             @Override
             public void onData(SocketController socketController, ISocketReadable readable) {
                 MTClient.this.onSocketData(readable);
             }
         });
     }
     public void disconnect(boolean graceful) {
         this.socketController.disconnect(graceful);
     }
 
     public void generateAuthKey(IMTAuthKeyListener listener) {
         MTAuthKeyGenerator generator = new MTAuthKeyGenerator(this, listener);
 
         this.authKeyGenerators.add(generator);
 
         generator.start();
     }
     void forgotAuthKeyGenerator(MTAuthKeyGenerator generator) {
         this.authKeyGenerators.remove(generator);
     }
 
     public void confirmMessage(long authKeyId, long sessionId, long messageId) {
         Map<Long, Queue<Long>> sessionAcksMap = this.acks.get(authKeyId);
         Queue<Long> acksQueue = sessionAcksMap.get(sessionId);
 
         acksQueue.add(messageId);
 
         this.createAckTimer(authKeyId, sessionId);
     }
 
     private void onData(long authKeyId, long sessionId, long messageId, int seqNo, ITypedData data) {
         Map<Long, Queue<Long>> sessionAcksMap = this.acks.get(authKeyId);
         Queue<Long> acksQueue = sessionAcksMap.get(sessionId);
 
         if (acksQueue == null) {
             acksQueue = new LinkedList<Long>();
             sessionAcksMap.put(sessionId, acksQueue);
         }
 
         boolean sendAck = (seqNo % 2 == 1);
 
         boolean consumed = true;
 
         switch (data.getId()) {
         case MTRpcResult.CONSTRUCTOR_ID:
             consumed = false;
             sendAck = false; // because acks for rpc-answers only by demand
             break;
         case MTMsgContainer.CONSTRUCTOR_ID:
             Object[] containerMessages = data.<ITypedData>getTypedData(MTMsgContainer.messages)
                 .getTypedData(0);
 
             for (Object messageObject : containerMessages) {
                 ITypedData message = (ITypedData) messageObject;
 
                 long msgId = message.getTypedData(MTMessage.msgId);
                 int msgSeqNo = message.getTypedData(MTMessage.seqno);
                 ITypedData body = message.getTypedData(MTMessage.body);
 
                 this.onData(authKeyId, sessionId, msgId, msgSeqNo, body);
             }
 
             break;
         case MTMsgsAck.CONSTRUCTOR_ID:
             Long[] ackMsgIds = data.<ITypedData>getTypedData(MTMsgsAck.msgIds).getTypedData(0);
 
             for (long msgId : ackMsgIds) {
                 this.confirmMessage(msgId);
             }
 
             break;
         case MTBadServerSalt.CONSTRUCTOR_ID:
             int badSaltErrorCode = data.getTypedData(MTBadServerSalt.errorCode);
             long newServerSalt = data.getTypedData(MTBadServerSalt.newServerSalt);
             long badMsgId = data.getTypedData(MTBadServerSalt.badMsgId);
 
             switch (badSaltErrorCode) {
             case 48: // bad salt
                 this.storage.serverSalt(authKeyId, sessionId, newServerSalt);
                 this.resendMessage(badMsgId);
                 break;
             default:
                 throw new RuntimeException("unknown bad_server_salt error_code: " + badSaltErrorCode);
             }
             break;
         case MTBadMsgNotification.CONSTRUCTOR_ID:
             int badMsgErrorCode = data.getTypedData(MTBadMsgNotification.errorCode);
 
             this.logger.undefinedBehavior(
                 this,
                 "bad_msg_notification unsupported error code",
                 badMsgErrorCode
             );
 
             throw new RuntimeException("bad_msg_notification: " + badMsgErrorCode);
             //break;
         case MTPong.CONSTRUCTOR_ID:
             if (data.<Long>getTypedData(MTPong.pingId) != 0) {
                 consumed = false;
             }
 
             this.confirmMessage(data.<Long>getTypedData(MTPong.msgId));
 
             break;
         case MTNewSessionCreated.CONSTRUCTOR_ID:
             this.storage.serverSalt(authKeyId, sessionId, data.<Long>getTypedData(MTNewSessionCreated.serverSalt));
             break;
         default:
             this.logger.undefinedBehavior(
                 this,
                 "unsupported packet type",
                 data.getId()
             );
 
             throw new RuntimeException("Unsupported packet type: " + data.getConstructor().getRootName());
         }
 
         if (sendAck) {
             acksQueue.add(messageId);
         }
 
         if (!consumed) {
             this.dataListener.onData(this, authKeyId, sessionId, messageId, data);
         }
     }
 
     private void createAckTimer(final long authKeyId, final long sessionId) {
         final Queue<Long> acksQueue = this.acks.get(authKeyId).get(sessionId);
 
         if (!acksQueue.isEmpty()) {
             if (this.ackTimerTask != null) this.ackTimerTask.cancel();
 
             this.ackTimerTask = new TimerTask() {
                 @Override
                 public void run() {
                     synchronized (acksQueue) {
                         if (acksQueue.isEmpty()) return;
 
                         Long[] acksArray = new Long[acksQueue.size()];
 
                         int i = 0;
                         for (long msgId : acksQueue) {
                             acksArray[i++] = msgId;
                         }
 
                         acksQueue.clear();
 
                         ITypedData ackMessage = new TypedData(MTMsgsAck.CONSTRUCTOR)
                             .setTypedData(MTMsgsAck.msgIds, new TypedData(MTVector.CONSTRUCTOR_LONG)
                                 .setTypedData(0, acksArray));
 
                         MTClient.this.send(authKeyId, sessionId, ackMessage, false, new IMTMessageStatusListener() {
                             @Override public void onConstructed(long messageId) {}
                             @Override public void onSent(long messageId) {}
                             @Override public void onDelivered(long messageId) {}
                             @Override public void onConnectionError(long messageId) {}
                         });
                     }
                 }
             };
             this.timer.schedule(this.ackTimerTask, 5000);
         }
     }
 
     private void confirmMessage(long messageId) {
         UndeliveredMessage message = this.undeliveredMessages.get(messageId);
 
         if (message == null) {
             Long containerMessageId = this.containerDeliverDependencies.get(messageId);
 
             if (containerMessageId != null) {
                 messageId = containerMessageId;
                 message = this.undeliveredMessages.get(messageId);
 
                 this.containerDeliverDependencies.remove(messageId);
             }
 
             if (message == null) return;
         }
 
         message.delivered();
 
         this.undeliveredMessages.remove(messageId);
     }
 
     private void resendMessage(final long messageId) {
         final UndeliveredMessage message = this.undeliveredMessages.get(messageId);
 
         ITypedData data = message.getToSend().getData();
 
         this.send(message.getAuthKeyId(), message.getSessionId(),
             data, message.isMeaningful(),
             new IMTMessageStatusListener() {
                 @Override
                 public void onConstructed(long messageId) {
                     MTClient.this.undeliveredMessages.remove(messageId);
                     MTClient.this.undeliveredMessages.put(messageId, message);
 
                     message.constructed(messageId);
                 }
 
                 @Override public void onSent(long messageId) {
                     message.sent();
                 }
 
                 @Override public void onDelivered(long messageId) {
                     message.delivered();
                 }
 
                 @Override
                 public void onConnectionError(long messageId) {
                     message.connectError();
                 }
             }
         );
     }
 
     private void onSocketData(ISocketReadable readable) {
         while (true) {
             if (this.readingSize) {
                 readable.read(this.sizeBuffer);
 
                 if (this.sizeBuffer.hasRemaining()) return;
 
                 int length = this.sizeBuffer.getInt(0);
                 this.bodyBuffer = BufferUtil.allocateLE(length);
 
                 this.bodyBuffer.putInt(length);
 
                 this.sizeBuffer.position(0);
                 this.readingSize = false;
             }
 
             readable.read(this.bodyBuffer);
 
             if (this.bodyBuffer.hasRemaining()) return;
 
             this.readingSize = true;
 
             int packetNo = this.bodyBuffer.getInt(4);
             if (this.serverPacketNo != packetNo) {
                 System.err.println("Wrong packetNo. Got: " + packetNo + ". Expected: " + this.serverPacketNo);
 
                 // wrong packetNo
                 this.socketController.disconnect(false);
 
                 return;
             }
 
             this.serverPacketNo++;
 
             // buffer received
             int actualCrc32 = ChecksumUtil.CRC32(
                 this.bodyBuffer.array(),
                 this.bodyBuffer.arrayOffset(),
                 this.bodyBuffer.limit() - 4
             );
 
             int packetCrc32 = this.bodyBuffer.getInt(this.bodyBuffer.limit() - 4);
 
             if (actualCrc32 != packetCrc32) {
                 this.socketController.disconnect(false);
 
                 return;
             }
 
             this.handleReceivedBuffer(this.bodyBuffer);
         }
     }
 
     private void handleReceivedBuffer(ByteBuffer buffer) {
         buffer.position(8); // skipping size and packetNo
 
         long authKeyId = buffer.getLong();
         long messageId;
 
         if (authKeyId != 0) {
             MTAuthKey authKey = this.authKeys.get(authKeyId);
 
             if (authKey == null) {
                 System.err.println("Unknown authKeyId: " + StringUtil.toHex(authKeyId));
                 return;
             }
 
             byte[] messageKey = new byte[16];
 
             buffer.get(messageKey);
 
             byte[] encrypted = new byte[buffer.limit() - 4 - 4 - 8 - 16 - 4];
 
             buffer.get(encrypted);
 
             byte[] decrypted = Encryption.decrypt(authKey, messageKey, encrypted);
 
             ByteBuffer decryptedBuffer = BufferUtil.wrapLE(decrypted);
 
             long serverSalt = decryptedBuffer.getLong();
             long sessionId = decryptedBuffer.getLong();
             messageId = decryptedBuffer.getLong();
             int seqNo = decryptedBuffer.getInt();
 
             // message length
             int messageLength = decryptedBuffer.getInt();
 
             byte[] actualMessageKey = BytesUtil.slice(Hashes.SHA1(
                 decrypted,
                 0,
                 8 + 8 + 8 + 4 + 4 + messageLength
             ), 4, 16);
 
             if (!BytesUtil.equals(messageKey, actualMessageKey)) {
                 this.logger.undefinedBehavior(
                     this, "Wrong message key",
                     // variables containing trash, but log it
                     authKeyId, sessionId, messageId, seqNo
                 );
                 return;
             }
 
             this.storage.serverSalt(authKeyId, sessionId, serverSalt);
 
             ITypedData data = TL.parse(this.schema, decryptedBuffer);
 
             this.logger.onReceived(this, authKeyId, sessionId, messageId, seqNo, data);
 
             this.onData(authKeyId, sessionId, messageId, seqNo, data);
             this.createAckTimer(authKeyId, sessionId);
         } else {
             // unecnrypted message
 
             messageId = buffer.getLong();
             buffer.getInt(); // message length
 
             ITypedData data = (ITypedData) TL.deserialize(MTTL.SCHEMA, MTTypeObject.TYPE, buffer);
 
             this.logger.onReceived(this, 0, 0, messageId, 0, data);
 
             // if not consumed by key generators
             if (!this.authKeyGenerators.onData(data)) {
                 this.dataListener.onData(
                     this,
                     0, 0, messageId,
                     data
                 );
             }
         }
 
         // TODO
         //this.storage.syncTime((int) (messageId >>> 32));
     }
 
     public InetSocketAddress getAddress() {
         return this.address;
     }
 
     public static int calcPadding(int length, int padding) {
         return (padding - (length % padding)) % padding;
     }
 
     private static Random random = new Random();
     private static SecureRandom secureRandom = new SecureRandom();
     public static Random getRandom() {
         return MTClient.random;
     }
     public static SecureRandom getSecureRandom() {
         return  MTClient.secureRandom;
     }
 
     public static byte[] getRandomBytes(int count) {
         byte[] result = new byte[count];
 
         MTClient.random.nextBytes(result);
 
         return result;
     }
 
     public static byte[] getSecureRandomBytes(int count) {
         byte[] result = new byte[count];
 
         MTClient.secureRandom.nextBytes(result);
 
         return result;
     }
 }
