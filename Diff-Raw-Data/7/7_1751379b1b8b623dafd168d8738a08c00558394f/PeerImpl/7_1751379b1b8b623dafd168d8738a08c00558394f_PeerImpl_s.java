 package edu.ualr.bittorrent.impl.core;
 
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.UUID;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.apache.log4j.Logger;
 import org.joda.time.Instant;
 
 import com.google.common.base.Objects;
 import com.google.common.base.Preconditions;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.sun.tools.javac.util.Pair;
 
 import edu.ualr.bittorrent.PeerMessage;
 import edu.ualr.bittorrent.impl.core.messages.HandshakeImpl;
 import edu.ualr.bittorrent.impl.core.messages.MessagesModule;
 import edu.ualr.bittorrent.interfaces.Message;
 import edu.ualr.bittorrent.interfaces.Metainfo;
 import edu.ualr.bittorrent.interfaces.Peer;
 import edu.ualr.bittorrent.interfaces.PeerState;
 import edu.ualr.bittorrent.interfaces.Tracker;
 import edu.ualr.bittorrent.interfaces.TrackerResponse;
 import edu.ualr.bittorrent.interfaces.PeerState.ChokeStatus;
 import edu.ualr.bittorrent.interfaces.PeerState.InterestLevel;
 import edu.ualr.bittorrent.interfaces.PeerState.PieceDeclaration;
 import edu.ualr.bittorrent.interfaces.PeerState.PieceDownload;
 import edu.ualr.bittorrent.interfaces.PeerState.PieceRequest;
 import edu.ualr.bittorrent.interfaces.PeerState.PieceUpload;
 import edu.ualr.bittorrent.interfaces.messages.BitField;
 import edu.ualr.bittorrent.interfaces.messages.Cancel;
 import edu.ualr.bittorrent.interfaces.messages.CancelFactory;
 import edu.ualr.bittorrent.interfaces.messages.Choke;
 import edu.ualr.bittorrent.interfaces.messages.ChokeFactory;
 import edu.ualr.bittorrent.interfaces.messages.Handshake;
 import edu.ualr.bittorrent.interfaces.messages.HandshakeFactory;
 import edu.ualr.bittorrent.interfaces.messages.Have;
 import edu.ualr.bittorrent.interfaces.messages.HaveFactory;
 import edu.ualr.bittorrent.interfaces.messages.Interested;
 import edu.ualr.bittorrent.interfaces.messages.InterestedFactory;
 import edu.ualr.bittorrent.interfaces.messages.KeepAlive;
 import edu.ualr.bittorrent.interfaces.messages.KeepAliveFactory;
 import edu.ualr.bittorrent.interfaces.messages.NotInterested;
 import edu.ualr.bittorrent.interfaces.messages.NotInterestedFactory;
 import edu.ualr.bittorrent.interfaces.messages.Piece;
 import edu.ualr.bittorrent.interfaces.messages.PieceFactory;
 import edu.ualr.bittorrent.interfaces.messages.Port;
 import edu.ualr.bittorrent.interfaces.messages.Request;
 import edu.ualr.bittorrent.interfaces.messages.RequestFactory;
 import edu.ualr.bittorrent.interfaces.messages.Unchoke;
 import edu.ualr.bittorrent.interfaces.messages.UnchokeFactory;
 
 /**
  * Default peer implementation.
  */
 public class PeerImpl implements Peer {
   /*
    * ##########################################################################
    * D E C L A R A T I O N S
    * ##########################################################################
    */
 
   /**
    * Tracker that we will be using to learn about other peers and that we will
    * be reporting our status to
    */
   private Tracker tracker;
 
   /**
    * Unique ID of this peer
    */
   private final byte[] id;
 
   /**
    * Metainfo that we are using to navigate this swarm
    */
   private Metainfo metainfo;
 
   /**
    * Messages that have been sent to this peer that are pending processing
    */
   private final List<Message> inboundMessageQueue = Lists.newArrayList();
 
   /**
    * Number of bytes that this peer has downloaded
    */
   private final AtomicInteger bytesDownloaded = new AtomicInteger();
 
   /**
    * Number of bytes that this peer has uploaded
    */
   private final AtomicInteger bytesUploaded = new AtomicInteger();
 
   /**
    * Number of bytes that this peer still needs to download
    */
   private final AtomicInteger bytesRemaining = new AtomicInteger();
 
   /**
    * Map of active peers and the state of those peers
    */
   private final Map<Peer, PeerState> activePeers = new ConcurrentHashMap<Peer, PeerState>();
 
   /**
    * The data that is being downloaded
    */
   private final Map<Integer, byte[]> data;
 
   /**
    * Injector used to create messages
    */
   private final Injector injector;
 
   /**
    * Arbitrarily chosen limit on the number of peers that will be unchoked at
    * any gieven time
    */
   private static final Integer UNCHOKED_PEER_LIMIT = 100;
 
   /**
    * The next time that this peer can communicate with the tracker
    */
   private Instant nextCommunicationWithTracker;
 
   /**
    * Time to wait on a peer to send a handshake before sending that peer another
    * handshake
    */
   private static final Integer REHANDSHAKE_WAIT_MILLISECONDS = 10000;
 
   /**
    * Time to wait on a peer to send a handshake before removing that peer from
    * the active peers list
    */
   private static final Integer UNRESPONSIVE_HANDSHAKE_MILLISECONDS = 100000;
 
   /**
    * Logger
    */
   private static final Logger logger = Logger.getLogger(PeerImpl.class);
 
   /**
    * Since timing could be an issue, the remote peer might send a handshake more
    * than once before it gets a reply. Every handshake shouldn't be replied to
    * if one has already been sent, so instead of replying to every handshake,
    * the peer will only reply to a handshake message from a peer that it has
    * already responded to if three handshakes in a row occur.
    */
   private static final Integer CONSECUTIVE_HANDSHAKES_UNTIL_RESEND = 3;
 
   /**
    * If a message isn't sent to a peer in this amount of time, send a keep alive
    * message.
    */
   private static final Integer MAX_MILLISECONDS_BETWEEN_MESSAGES = 20000;
 
   /**
    * Just in case our peer doesn't get the message that it is choked or
    * unchoked, remind it on occasion.
    */
   private static final Integer MILLISECONDS_BETWEEN_CHOKE_STATUS_REMINDERS = 10000;
 
   /**
    * If our interest in a peer hasn't changed, we don't technically have to send
    * them another interest message; however, it is nice to remind the peer of
    * our interest, just not annoyingly.
    */
   private static final Integer MILLISECONDS_BETWEEN_REPEAT_INTEREST_MESSAGES = 12345;
 
   /**
    * Hard-limit on the number of peers that can be unchoked at any given time.
    */
   private static final Integer MAX_UNCHOKED_PEERS = 50;
 
   /*
    * ##########################################################################
    * C O N S T R U C T O R S
    * ##########################################################################
    */
 
   /**
    * Create a new PeerImpl object, providing a unique ID and some initial data
    * related to the torrent.
    *
    * @param id
    *
    * @param initialData
    */
   public PeerImpl(byte[] id, Map<Integer, byte[]> initialData) {
     this.id = Preconditions.checkNotNull(id);
     this.injector = Guice.createInjector(new MessagesModule());
 
     if (initialData == null) {
       this.data = Maps.newHashMap();
     } else {
       this.data = initialData;
     }
   }
 
   /**
    * Create a new PeerImpl object, providing some initial data related to the
    * torrent.
    *
    * @param brains
    * @param initialData
    */
   public PeerImpl(Map<Integer, byte[]> initialData) {
     this(UUID.randomUUID().toString().getBytes(), initialData);
   }
 
   /**
    * Create a new leeching PeerImpl object accepting the default
    * {@link PeerBrainsImpl} for peer decision making.
    */
   public PeerImpl() {
     this(UUID.randomUUID().toString().getBytes(), null);
   }
 
   /*
    * ##########################################################################
    * I N T E R F A C E - I M P L E M E N T A T I O NS
    * ##########################################################################
    */
 
   /**
    * {@inheritDoc}
    */
   public void setMetainfo(Metainfo metainfo) {
     this.metainfo = Preconditions.checkNotNull(metainfo);
     this.tracker = Preconditions.checkNotNull(metainfo.getTrackers().get(0));
   }
 
   /**
    * {@inheritDoc}
    */
   public byte[] getId() {
     return id;
   }
 
   /**
    * {@inheritDoc}
    */
   public void message(Message message) {
     synchronized (inboundMessageQueue) {
       inboundMessageQueue.add(message);
     }
   }
 
   /**
    * {@inheritDoc}
    */
   public void run() {
     peer();
   }
 
   /*
    * ##########################################################################
    * C O R E - J A V A - O V E R R I D E S
    * ##########################################################################
    */
 
   /**
    * {@inheritDoc}
    */
   @Override
   public boolean equals(Object object) {
     if (!(object instanceof PeerImpl)) {
       return false;
     }
     PeerImpl peer = (PeerImpl) object;
     return Objects.equal(id, peer.id) && Objects.equal(metainfo, peer.metainfo);
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public int hashCode() {
     return Objects.hashCode(this.id, this.metainfo);
   }
 
   /**
    * {@inheritDoc}
    */
   @Override
   public String toString() {
     return new String(id);
   }
 
   /*
    * ##########################################################################
    * P R I M A R Y - T H R E A D - D R I V E R
    * ##########################################################################
    */
 
   /**
    * Primary driver for the {@link Peer}. This thread runs continually,
    * announcing to the {@link Tracker} at regular intervals, and sending and
    * receiving {@link Message}s with other {@link Peer}s in the swarm.
    */
   public void peer() {
     Preconditions.checkNotNull(tracker);
     Preconditions.checkNotNull(id);
     Preconditions.checkNotNull(metainfo);
 
     /* initial announcement to tracker */
     announce();
 
     while (true) {
       if (new Instant().isAfter(nextCommunicationWithTracker)) {
         announce();
       }
       initiateCommunication();
       processMessages();
     }
   }
 
   /*
    * ##########################################################################
    * T R A C K E R - I N T E R A C T I O N
    * ##########################################################################
    */
 
   /**
    * Announce to the tracker, receiving a list of peers to communicate with.
    */
   private void announce() {
     synchronized (bytesRemaining) {
       bytesRemaining.set(howMuchIsLeftToDownload());
     }
 
     TrackerResponse response = tracker.get(new TrackerRequestImpl(this,
         metainfo.getInfoHash(), bytesDownloaded.get(), bytesUploaded.get(),
         bytesRemaining.get()));
     for (Peer peer : response.getPeers()) {
       if (!activePeers.containsKey(peer) && !this.equals(peer)) {
         synchronized (activePeers) {
           activePeers.put(peer, new PeerStateImpl());
         }
         sendHandshakeMessage(injector.getInstance(HandshakeFactory.class)
             .create(this, peer, HandshakeImpl.DEFAULT_PROTOCOL_IDENTIFIER,
                 metainfo.getInfoHash(), HandshakeImpl.DEFAULT_RESERVED_BYTES));
       }
     }
 
     nextCommunicationWithTracker = new Instant()
         .plus(response.getInterval() * 1000); // seconds to millis
   }
 
   /*
    * ##########################################################################
    * P E E R - I N T E R A C T I O N
    * ##########################################################################
    */
 
   public List<Pair<Peer, Message>> initiateCommunication() {
     Preconditions.checkNotNull(activePeers);
     Preconditions.checkNotNull(metainfo);
     Preconditions.checkNotNull(data);
 
     rehandshake();
 
     remindPeersOfChokeStatus();
 
     tellPeersAboutPiecesLocalHas();
 
     expressInterestOrDisinterest();
 
     unchokePeers();
 
     // TODO: bitfield
     // TODO: cancel
     // TODO: choke
     // TODO: interested
     // TODO: notinterested
     // TODO: piece
     // TODO: request
     // TODO: unchoke
 
     keepAlive();
 
     /*
      * Choke Any peer that I have shaken hands with, but have not choked will be
      * choked.
      *
      * Periodically, I will choke peers that I feel are currently unworthy.
      *
      * Have All peers should get an updated have list from me.
      *
      * Cancel After I receive a piece from a peer, I will tell other peers that
      * are sending that piece that I no longer need it.
      *
      * Interested If a peer sends an interested message and it is choked, I will
      * consider unchoking them.
      *
      * If I am choked by a peer, but would like to get data from them, I will
      * express interest.
      *
      * Not Interested If I am choked by a peer, I will let that peer know that I
      * don't care if I'm unchoked by expressing a lack of interest.
      *
      * If a peer expresses disinterest in me, I will choke that peer if they are
      * unchoked.
      *
      * Piece If an unchoked peer makes a request to me, I will do my best to
      * honor that request.
      *
      * Request If I am unchoked and a peer has data that I need, I will request
      * it from that peer. If the peer does not respond in a reasonable amount of
      * time, I will request the data from another peer.
      *
      * Unchoke If a peer is choked and has expressed interest and if I have a
      * slot open, I will unchoke the peer for a limited period of time to give
      * them a chance to request data from me.
      *
      * KeepAlive If I have no other message to send a peer, I will send it a
      * keep alive. Set<Peer> peers;
      *
      * synchronized (activePeers) { peers = activePeers.keySet(); }
      *
      * for (Peer p : peers) { messages.addAll(makePeerLevelDecisions());
      * PeerState state = returnStateIfAvailable(p); if (state == null) {
      * continue; }
      *
      * if (handleCommunicationInitalization(p, state, messages)) { continue; }
      *
      * Let the remote peer know about any new pieces that we might have if
      * (letPeerKnowAboutNewPieces(p, state, messages)) { continue; }
      *
      * if (sendMeaningfuleMessageToPeer(p, state, messages)) { continue; }
      *
      * If no other messages are necessary, just send a keep alive
      * sendKeepAlive(p, state, messages); }
      *
      * return messages;
      */
 
     return null;
   }
 
   private void processMessages() {
     Message message = null;
     synchronized (inboundMessageQueue) {
       if (inboundMessageQueue.size() > 0) {
         message = inboundMessageQueue.remove(0);
       }
     }
     if (message != null) {
       processMessage(message);
     }
   }
 
   /**
    * A handshake is sent to a new peer as soon as it is encountered, so we can
    * assume that we have sent at least one handshake. Any peer who has not
    * shaken back over the last x period of time will receive another handshake.
    * After n attempts, the peer will be ignored.
    */
   private void rehandshake() {
     Set<Peer> peers;
     synchronized (activePeers) {
       peers = activePeers.keySet();
     }
 
     for (Peer peer : peers) {
       PeerState peerState = getStateForPeer(peer);
 
       Instant remoteSentHandshakeAt = null;
       Instant localSentHandshakeAt = null;
       synchronized (peerState) {
         remoteSentHandshakeAt = peerState.whenDidRemoteSendHandshake();
         localSentHandshakeAt = peerState.whenDidLocalSendHandshake();
       }
 
       if (remoteSentHandshakeAt == null) {
         Instant now = new Instant();
 
         if (now.isAfter(localSentHandshakeAt
             .plus(UNRESPONSIVE_HANDSHAKE_MILLISECONDS))) {
           synchronized (activePeers) {
             activePeers.remove(peer);
           }
         } else if (now.isAfter(localSentHandshakeAt
             .plus(REHANDSHAKE_WAIT_MILLISECONDS))) {
           sendHandshakeMessage(injector.getInstance(HandshakeFactory.class)
               .create(this, peer, HandshakeImpl.DEFAULT_PROTOCOL_IDENTIFIER,
                   metainfo.getInfoHash(), HandshakeImpl.DEFAULT_RESERVED_BYTES));
         }
       }
     }
   }
 
   private void remindPeersOfChokeStatus() {
     Set<Peer> peers;
     synchronized (activePeers) {
       peers = activePeers.keySet();
     }
 
     for (Peer peer : peers) {
       PeerState peerState = getStateForPeer(peer);
 
       Pair<ChokeStatus, Instant> remoteChokeStatus = null;
       synchronized (peerState) {
         remoteChokeStatus = peerState.isRemoteChoked();
       }
 
       if (remoteChokeStatus != null) {
         Instant now = new Instant();
 
         if (now.isAfter(remoteChokeStatus.snd
             .plus(MILLISECONDS_BETWEEN_CHOKE_STATUS_REMINDERS))) {
           if (remoteChokeStatus.fst.equals(ChokeStatus.CHOKED)) {
             sendChokeMessage(injector.getInstance(ChokeFactory.class).create(
                 this, peer));
           } else {
             sendUnchokeMessage(injector.getInstance(UnchokeFactory.class)
                 .create(this, peer));
           }
         }
       }
     }
   }
 
   private void tellPeersAboutPiecesLocalHas() {
     Set<Integer> downloadedPieces = null;
 
     synchronized (data) {
       downloadedPieces = data.keySet();
     }
 
     if (downloadedPieces == null) {
       return;
     }
 
     Set<Peer> peers;
     synchronized (activePeers) {
       peers = activePeers.keySet();
     }
 
     for (Peer peer : peers) {
       PeerState peerState = getStateForPeer(peer);
 
       List<PieceDeclaration> declaredPieces = null;
       synchronized (peerState) {
         declaredPieces = peerState.localHasPieces();
       }
 
       for (Integer pieceIndex : downloadedPieces) {
         boolean declared = false;
         for (PieceDeclaration piece : declaredPieces) {
           if (pieceIndex.equals(piece.getPieceIndex())) {
             declared = true;
             break;
           }
         }
         if (!declared) {
           sendHaveMessage(injector.getInstance(HaveFactory.class).create(this,
               peer, pieceIndex));
         }
       }
     }
   }
 
   private void expressInterestOrDisinterest() {
     // TODO: seeders should automatically express disinterest
     Set<Integer> downloadedPieces = null;
 
     synchronized (data) {
       downloadedPieces = data.keySet();
     }
 
     Set<Peer> peers;
     synchronized (activePeers) {
       peers = activePeers.keySet();
     }
 
     List<PieceDeclaration> remotePieces = null;
     Pair<ChokeStatus, Instant> choked = null;
     Pair<InterestLevel, Instant> interest = null;
     for (Peer peer : peers) {
       PeerState peerState = getStateForPeer(peer);
 
       synchronized (peerState) {
         choked = peerState.isLocalChoked();
         remotePieces = peerState.remoteHasPieces();
         interest = peerState.getLocalInterestLevelInRemote();
       }
 
       if (choked != null && ChokeStatus.UNCHOKED.equals(choked.fst)) {
         continue; // if we are already unchoked, there is no reason to express
         // interest
       }
 
       expressInterestOrDisinterest(downloadedPieces, interest, remotePieces,
           peer);
 
     }
   }
 
   private void expressInterestOrDisinterest(Set<Integer> downloadedPieces,
       Pair<InterestLevel, Instant> interest,
       List<PieceDeclaration> remotePieces, Peer peer) {
     Instant now = new Instant();
 
     // if there is nothing to express interest in, express disinterest
     if (remotePieces == null) {
       if (interest != null
           && interest.fst.equals(InterestLevel.NOT_INTERESTED)
           && now.isBefore(interest.snd
               .plus(MILLISECONDS_BETWEEN_REPEAT_INTEREST_MESSAGES))) {
         return;
       }
       sendNotInterestedMessage(injector.getInstance(NotInterestedFactory.class)
           .create(this, peer));
       return;
     }
 
     for (PieceDeclaration pieceDeclaration : remotePieces) {
       if (!downloadedPieces.contains(pieceDeclaration.getPieceIndex())) {
         // if we sent the same message recently, don't send it again
         if (interest != null
             && interest.fst.equals(InterestLevel.INTERESTED)
             && now.isBefore(interest.snd
                 .plus(MILLISECONDS_BETWEEN_REPEAT_INTEREST_MESSAGES))) {
           return;
         }
 
         sendInterestedMessage(injector.getInstance(InterestedFactory.class)
             .create(this, peer));
 
         return; // only express interest once in this loop
       }
     }
 
     // if we sent the same message recently, don't send it again
     if (interest != null
         && interest.fst.equals(InterestLevel.NOT_INTERESTED)
         && now.isBefore(interest.snd
             .plus(MILLISECONDS_BETWEEN_REPEAT_INTEREST_MESSAGES))) {
       return;
     }
 
     sendNotInterestedMessage(injector.getInstance(NotInterestedFactory.class)
         .create(this, peer));
   }
 
   /*
    * New peers will need to be optimistically unchoked
    */
   private void unchokePeers() {
     Set<Peer> peers;
     synchronized (activePeers) {
       peers = activePeers.keySet();
     }
 
    if (MAX_UNCHOKED_PEERS > ) {
       return;
     }
 
     for (Peer peer : peers) {
       PeerState state;
       synchronized (activePeers) {
         state = activePeers.get(peer);
       }
 
       if (state == null) {
         continue;
       }
 
       if (ChokeStatus.CHOKED.equals(state.isRemoteChoked()) &&
           InterestLevel.INTERESTED.equals(state.getRemoteInterestLevelInLocal())) {
        if ()
        interestedAndChokedPeers.add(peer);
       }
     }
   }
 
   private void keepAlive() {
     Set<Peer> peers;
     synchronized (activePeers) {
       peers = activePeers.keySet();
     }
 
     for (Peer peer : peers) {
       PeerState peerState = getStateForPeer(peer);
 
       Instant lastMessageSentByLocalAt = null;
       synchronized (peerState) {
         lastMessageSentByLocalAt = peerState.whenDidLocalSendLastMessage();
       }
 
       Instant now = new Instant();
       if (now.isAfter(lastMessageSentByLocalAt
           .plus(MAX_MILLISECONDS_BETWEEN_MESSAGES))) {
         sendKeepAliveMessage(injector.getInstance(KeepAliveFactory.class)
             .create(this, peer));
       }
     }
   }
 
   /*
    * ##########################################################################
    * M E S S A G E - P R O C C E S S I N G
    * ##########################################################################
    */
 
   /**
    * Messages are received as somewhat generic {@link PeerMessage} objects. This
    * method determines the type of {@link PeerMessage} and dispatches the
    * message to the appropriate handler.
    *
    * @param message
    */
   private void processMessage(Message message) {
     if (message instanceof BitField) {
       processBitFieldMessage((BitField) message);
     } else if (message instanceof Cancel) {
       processCancelMessage((Cancel) message);
     } else if (message instanceof Choke) {
       processChokeMessage((Choke) message);
     } else if (message instanceof Port) {
       processPortMessage((Port) message);
     } else if (message instanceof Request) {
       processRequestMessage((Request) message);
     } else if (message instanceof Handshake) {
       processHandshakeMessage((Handshake) message);
     } else if (message instanceof Have) {
       processHaveMessage((Have) message);
     } else if (message instanceof Interested) {
       processInterestedMessage((Interested) message);
     } else if (message instanceof KeepAlive) {
       processKeepAliveMessage((KeepAlive) message);
     } else if (message instanceof NotInterested) {
       processNotInterestedMessage((NotInterested) message);
     } else if (message instanceof Piece) {
       processPieceMessage((Piece) message);
     } else if (message instanceof Unchoke) {
       processUnchokeMessage((Unchoke) message);
     } else {
       throw new IllegalArgumentException(String.format(
           "Peer %s sent an unsupported message of type %s", new String(message
               .getSendingPeer().getId()), message.getType()));
     }
   }
 
   /*
    * ==========================================================================
    * B I T F I E L D
    * ==========================================================================
    */
 
   /**
    * Package and send a bitfield message.
    *
    * @param remotePeer
    * @param bitfield
    */
   private void sendBitFieldMessage(Peer remotePeer, BitField bitfield) {
     PeerState state = getStateForPeer(remotePeer);
 
     Instant now = new Instant();
     for (int i = 0; i < bitfield.getBitField().length; i++) {
       if (bitfield.getBitField()[i] != 0x0) {
         PeerState.PieceDeclaration declaration = new PeerStateImpl.PieceDeclarationImpl();
         declaration.setDeclarationTime(now);
         declaration.setPieceIndex(i);
         synchronized (state) {
           state.setRemoteHasPiece(declaration);
         }
       }
     }
 
     remotePeer.message(bitfield);
   }
 
   /**
    * A {@link BitField} message is really just the remote peer letting the local
    * peer know all of the pieces that it has via a single message instead of via
    * multiple {@link Have} messages. This method parses the {@link BitField} and
    * stores a {@link PieceDeclaration} for each piece that the remote claims to
    * possess.
    *
    * @param bitfield
    */
   private void processBitFieldMessage(BitField bitfield) {
     PeerState state = activePeers.get(bitfield.getSendingPeer());
     Instant now = new Instant();
     for (int i = 0; i < bitfield.getBitField().length; i++) {
       if (bitfield.getBitField()[i] != 0x0) {
         PeerState.PieceDeclaration declaration = new PeerStateImpl.PieceDeclarationImpl();
         declaration.setDeclarationTime(now);
         declaration.setPieceIndex(i);
         synchronized (state) {
           state.setRemoteHasPiece(declaration);
         }
       }
     }
   }
 
   /*
    * ==========================================================================
    * C A N C E L
    * ==========================================================================
    */
 
   /**
    * Package and send a cancel message.
    *
    * @param remotePeer
    * @param cancel
    */
   private void sendCancelMessage(Peer remotePeer, Cancel cancel) {
     PeerState state = getStateForPeer(remotePeer);
 
     PieceRequest pieceRequest = new PeerStateImpl.PieceRequestImpl();
     pieceRequest.setPieceIndex(cancel.getPieceIndex());
     pieceRequest.setBlockOffset(cancel.getBeginningOffset());
     pieceRequest.setBlockSize(cancel.getBlockLength());
 
     synchronized (state) {
       state.cancelLocalRequestedPiece(pieceRequest);
     }
     remotePeer.message(cancel);
   }
 
   /**
    * After a remote peer requests a piece, it can later cancel that request by
    * sending a cancel message. This method processes the given cancel message
    * and notes the cancel in the peer state.
    *
    * @param cancel
    */
   private void processCancelMessage(Cancel cancel) {
     PeerState state = activePeers.get(cancel.getSendingPeer());
 
     PieceRequest request = new PeerStateImpl.PieceRequestImpl();
     request.setPieceIndex(cancel.getPieceIndex());
     request.setBlockOffset(cancel.getBeginningOffset());
     request.setBlockSize(cancel.getBlockLength());
     request.setRequestTime(new Instant());
 
     synchronized (state) {
       state.cancelRemoteRequestedPiece(request);
     }
   }
 
   /*
    * ==========================================================================
    * C H O K E
    * ==========================================================================
    */
 
   /**
    * Package and send a choke message.
    *
    * @param remotePeer
    * @param choke
    */
   private void sendChokeMessage(Choke choke) {
     PeerState state = getStateForPeer(choke.getReceivingPeer());
 
     synchronized (state) {
       state.setRemoteIsChoked(ChokeStatus.CHOKED, new Instant());
     }
     choke.getReceivingPeer().message(choke);
   }
 
   /**
    * When choked by a remote peer, the local client should send any requests.
    * This method notes the choke in shared state.
    *
    * @param choke
    */
   private void processChokeMessage(Choke choke) {
     PeerState state = getStateForPeer(choke.getSendingPeer());
 
     synchronized (state) {
       state.setLocalIsChoked(ChokeStatus.CHOKED, new Instant());
     }
   }
 
   /*
    * ==========================================================================
    * P O R T
    * ==========================================================================
    */
 
   /**
    * Package and send a port message.
    *
    * @param remotePeer
    * @param port
    */
   private void sendPortMessage(Peer remotePeer, Port port) {
     PeerState state = getStateForPeer(remotePeer);
 
     synchronized (state) {
       state.setLocalRequestedPort(port.getPort());
     }
     remotePeer.message(port);
   }
 
   /**
    * In the real-world of BitTorrent, a remote peer might request communication
    * on a specific port. This method takes note of the port request; however,
    * since this simulator doesn't make actual network calls, the port operation
    * does little more than update state.
    *
    * @param port
    */
   private void processPortMessage(Port port) {
     PeerState state = getStateForPeer(port.getSendingPeer());
 
     synchronized (state) {
       state.setRemoteRequestedPort(port.getPort());
     }
   }
 
   /*
    * ==========================================================================
    * R E Q U E S T
    * ==========================================================================
    */
 
   /**
    * Package and send a request message.
    *
    * @param remotePeer
    * @param request
    */
   private void sendRequestMessage(Peer remotePeer, Request request) {
     PeerState state = getStateForPeer(remotePeer);
 
     PieceRequest pieceRequest = new PeerStateImpl.PieceRequestImpl();
     pieceRequest.setPieceIndex(request.getPieceIndex());
     pieceRequest.setBlockOffset(request.getBeginningOffset());
     pieceRequest.setBlockSize(request.getBlockLength());
     pieceRequest.setRequestTime(new Instant());
 
     synchronized (state) {
       state.setLocalRequestedPiece(pieceRequest);
     }
     remotePeer.message(request);
   }
 
   /**
    * A request message is sent by the remote peer to request a specific piece
    * from the local client. This method takes note of the specific request.
    *
    * @param request
    */
   private void processRequestMessage(Request request) {
     PeerState state = activePeers.get(request.getSendingPeer());
 
     PieceRequest pieceRequest = new PeerStateImpl.PieceRequestImpl();
     pieceRequest.setPieceIndex(request.getPieceIndex());
     pieceRequest.setBlockOffset(request.getBeginningOffset());
     pieceRequest.setBlockSize(request.getBlockLength());
     pieceRequest.setRequestTime(new Instant());
 
     synchronized (state) {
       state.setRemoteRequestedPiece(pieceRequest);
     }
   }
 
   /*
    * ==========================================================================
    * H A N D S H A K E
    * ==========================================================================
    */
 
   /**
    * Package and send a handshake message.
    *
    * @param remotePeer
    * @param handshake
    */
   private void sendHandshakeMessage(Handshake handshake) {
     PeerState state = getStateForPeer(handshake.getReceivingPeer());
 
     synchronized (state) {
       state.setLocalSentHandshakeAt(new Instant());
     }
     handshake.getReceivingPeer().message(handshake);
 
     sendChokeMessage(injector.getInstance(ChokeFactory.class).create(this,
         handshake.getReceivingPeer()));
   }
 
   /**
    * A handshake message is sent between peers to initiate communication. This
    * method notes that the remote peer sent a handshake. Also, if the local peer
    * has not sent a handshake, one is sent back to the remote.
    *
    * @param handshake
    */
   private void processHandshakeMessage(Handshake handshake) {
     PeerState peerState = getStateForPeer(handshake.getSendingPeer());
     Instant localSentHandshakeAt;
 
     synchronized (peerState) {
       peerState.setRemoteSentHandshakeAt(new Instant());
       localSentHandshakeAt = peerState.whenDidLocalSendHandshake();
     }
 
     if (localSentHandshakeAt == null) {
       sendHandshakeMessage(injector.getInstance(HandshakeFactory.class).create(
           this, handshake.getSendingPeer(),
           HandshakeImpl.DEFAULT_PROTOCOL_IDENTIFIER, metainfo.getInfoHash(),
           HandshakeImpl.DEFAULT_RESERVED_BYTES));
     } else {
       Integer remoteHandshakeCount;
       synchronized (peerState) {
         remoteHandshakeCount = peerState.howManyHandshakesHasTheRemoteSent();
       }
       if ((remoteHandshakeCount % CONSECUTIVE_HANDSHAKES_UNTIL_RESEND) == 0) {
         sendHandshakeMessage(injector.getInstance(HandshakeFactory.class)
             .create(this, handshake.getSendingPeer(),
                 HandshakeImpl.DEFAULT_PROTOCOL_IDENTIFIER,
                 metainfo.getInfoHash(), HandshakeImpl.DEFAULT_RESERVED_BYTES));
       }
     }
   }
 
   /*
    * ==========================================================================
    * H A V E
    * ==========================================================================
    */
 
   /**
    * Package and send a have message.
    *
    * @param remotePeer
    * @param have
    */
   private void sendHaveMessage(Have have) {
     PeerState state = getStateForPeer(have.getReceivingPeer());
 
     PeerState.PieceDeclaration declaration = new PeerStateImpl.PieceDeclarationImpl();
     declaration.setPieceIndex(have.getPieceIndex());
     declaration.setDeclarationTime(new Instant());
 
     synchronized (state) {
       state.setLocalHasPiece(declaration);
     }
 
     have.getReceivingPeer().message(have);
   }
 
   /**
    * As a peer collects pieces, it announces the newly collected pieces to its
    * peers. This method processes a piece announcement from a remote peer.
    *
    * @param have
    */
   private void processHaveMessage(Have have) {
     PeerState state = getStateForPeer(have.getSendingPeer());
 
     PieceDeclaration declaration = new PeerStateImpl.PieceDeclarationImpl();
     declaration.setPieceIndex(have.getPieceIndex());
     declaration.setDeclarationTime(new Instant());
 
     synchronized (state) {
       state.setRemoteHasPiece(declaration);
     }
   }
 
   /*
    * ==========================================================================
    * I N T E R E S T E D
    * ==========================================================================
    */
 
   /**
    * Package and send an interested message.
    *
    * @param remotePeer
    * @param interested
    */
   private void sendInterestedMessage(Interested interested) {
     PeerState state = getStateForPeer(interested.getReceivingPeer());
 
     synchronized (state) {
       state.setLocalInterestLevelInRemote(InterestLevel.INTERESTED,
           new Instant());
     }
     interested.getReceivingPeer().message(interested);
   }
 
   /**
    * When a remote peer, probably a choked one, is interested in a piece that
    * the local client has, it sends an {@link Interested} message to let the
    * local client know that the remote is interested in what the local client
    * has to offer. This method notes that interest.
    *
    * @param interested
    */
   private void processInterestedMessage(Interested interested) {
     PeerState state = getStateForPeer(interested.getSendingPeer());
 
     synchronized (state) {
       state.setRemoteInterestLevelInLocal(InterestLevel.INTERESTED,
           new Instant());
     }
   }
 
   /*
    * ==========================================================================
    * K E E P A L I V E
    * ==========================================================================
    */
 
   /**
    * Package and send a keepalive message.
    *
    * @param remotePeer
    * @param keepAlive
    */
   private void sendKeepAliveMessage(KeepAlive keepAlive) {
     PeerState state = getStateForPeer(keepAlive.getReceivingPeer());
 
     synchronized (state) {
       state.setLocalSentKeepAliveAt(new Instant());
     }
     keepAlive.getReceivingPeer().message(keepAlive);
   }
 
   /**
    * Periodically, clients should send a {@link KeepAlive} message to peers in
    * which they are connected, just to let the peers know that the client is
    * here and communicating... it just might not have had anything interesting
    * to say for a while.
    *
    * @param keepAlive
    */
   private void processKeepAliveMessage(KeepAlive keepAlive) {
     PeerState state = getStateForPeer(keepAlive.getSendingPeer());
 
     synchronized (state) {
       state.setRemoteSentKeepAliveAt(new Instant());
     }
   }
 
   /*
    * ==========================================================================
    * N O T I N T E R E S T E D
    * ==========================================================================
    */
 
   /**
    * Package and sent a not-interested message.
    *
    * @param remotePeer
    * @param notInterested
    */
   private void sendNotInterestedMessage(NotInterested notInterested) {
     PeerState state = getStateForPeer(notInterested.getReceivingPeer());
 
     synchronized (state) {
       state.setLocalInterestLevelInRemote(InterestLevel.NOT_INTERESTED,
           new Instant());
     }
     notInterested.getReceivingPeer().message(notInterested);
   }
 
   /**
    * When a peer isn't interested in anything that the local client has to
    * offer, it can tell them so using the {@link NotInterested} message. This
    * lets the local client know that there really isn't any reason to unchoke
    * the remote peer.
    *
    * @param notInterested
    */
   private void processNotInterestedMessage(NotInterested notInterested) {
     PeerState state = getStateForPeer(notInterested.getSendingPeer());
 
     synchronized (state) {
       state.setRemoteInterestLevelInLocal(InterestLevel.NOT_INTERESTED,
           new Instant());
     }
   }
 
   /*
    * ==========================================================================
    * P I E C E
    * ==========================================================================
    */
 
   /**
    * Package and send a piece message.
    *
    * @param remotePeer
    * @param piece
    */
   private void sendPieceMessage(Peer remotePeer, Piece piece) {
     PeerState state = getStateForPeer(remotePeer);
 
     PieceUpload pieceUpload = new PeerStateImpl.PieceUploadImpl();
     pieceUpload.setPieceIndex(piece.getPieceIndex());
     pieceUpload.setBlockOffset(piece.getBeginningOffset());
     pieceUpload.setBlockSize(piece.getBlock().length);
     pieceUpload.setStartTime(new Instant());
 
     synchronized (state) {
       state.setLocalSentPiece(pieceUpload);
     }
 
     synchronized (bytesUploaded) {
       bytesUploaded.addAndGet(piece.getBlock().length);
     }
 
     remotePeer.message(piece);
   }
 
   /**
    * When a remote peer sends a piece of data to the local client, it does so
    * via a {@link Piece} message. This method collects that piece.
    *
    * @param piece
    */
   private void processPieceMessage(Piece piece) {
     PeerState state = activePeers.get(piece.getSendingPeer());
 
     PieceDownload download = new PeerStateImpl.PieceDownloadImpl();
     download.setPieceIndex(piece.getPieceIndex());
     download.setBlockOffset(piece.getBeginningOffset());
     download.setBlockSize(piece.getBlock().length);
     download.setStartTime(piece.getSentTime());
     download.setCompletionTime(new Instant());
 
     synchronized (state) {
       state.setRemoteSentPiece(download);
     }
 
     synchronized (data) {
       data.put(piece.getPieceIndex(), piece.getBlock());
     }
 
     synchronized (bytesRemaining) {
       bytesRemaining.set(howMuchIsLeftToDownload());
     }
 
     synchronized (bytesDownloaded) {
       bytesDownloaded.addAndGet(piece.getBlock().length);
     }
   }
 
   /*
    * ==========================================================================
    * U N C H O K E
    * ==========================================================================
    */
 
   /**
    * Package and send an unchoke message.
    *
    * @param remotePeer
    * @param unchoke
    */
   private void sendUnchokeMessage(Unchoke unchoke) {
     PeerState state = getStateForPeer(unchoke.getReceivingPeer());
 
     synchronized (state) {
       state.setRemoteIsChoked(ChokeStatus.UNCHOKED, new Instant());
     }
 
     unchoke.getReceivingPeer().message(unchoke);
   }
 
   /**
    * A remote peer sends an {@link Unchoke} message to the local client to let
    * the local client know that it is okay to start sending requests for data.
    * This method notes the unchoked state.
    *
    * @param unchoke
    */
   private void processUnchokeMessage(Unchoke unchoke) {
     PeerState state = getStateForPeer(unchoke.getSendingPeer());
 
     synchronized (state) {
       state.setLocalIsChoked(ChokeStatus.UNCHOKED, new Instant());
     }
   }
 
   private int howMuchIsLeftToDownload() {
     int downloadedPieceCount = 0;
     boolean downloadedLastPiece = false;
 
     synchronized (data) {
       downloadedPieceCount = data.size();
       downloadedLastPiece = data.containsKey(metainfo.getLastPieceIndex());
     }
 
     int downloadedAmount = downloadedPieceCount * metainfo.getPieceLength();
 
     if (downloadedLastPiece) {
       downloadedAmount -= (metainfo.getPieceLength() - metainfo
           .getLastPieceSize());
     }
 
     return metainfo.getTotalDownloadSize() - downloadedAmount;
   }
 
   /*
    * ##########################################################################
    * U T I L I T I E S
    * ##########################################################################
    */
 
   /**
    * Package and send a state message.
    *
    * @param peer
    * @return
    */
   private PeerState getStateForPeer(Peer peer) {
     PeerState state = null;
     synchronized (activePeers) {
       if (activePeers.containsKey(peer)) {
         state = activePeers.get(peer);
       } else {
         state = new PeerStateImpl();
         activePeers.put(peer, state);
       }
     }
     return state;
   }
 
   /**
    * Debugging utility method.
    *
    * @param objects
    */
   private void debug(Object... objects) {
     String formatString = (String) objects[0];
     System.arraycopy(objects, 1, objects, 0, objects.length - 1);
     logger.debug(String.format(formatString, objects));
   }
 
   /************************************************************************************************/
   /************************************************************************************************/
   /************************************************************************************************/
   /************************************************************************************************/
   /************************************************************************************************/
   /************************************************************************************************/
 
   private boolean handleCommunicationInitalization(Peer p, PeerState state,
       List<Pair<Peer, Message>> messages) {
 
     /* If the remote peer hasn't sent a handshake yet, send them another */
     // if (!remoteHasSentHandshake(p, state, messages)) {
     // return true;
     // }
 
     /*
      * If we haven't started communicating with this peer yet, then the choke
      * status will be null. If this is the case, go ahead and send a choke
      * message to make the initial choke state official.
      */
     if (sendInitialChoke(p, state, messages)) {
       return true;
     }
 
     return false;
   }
 
   private boolean askForSomethingFromPeer(Peer p, PeerState state,
       List<Pair<Peer, Message>> messages) {
     /*
      * Let peers that are choking and that have pieces that we want know that we
      * are interested
      */
     // if (expressInterest(p, state, messages)) {
     // return true;
     // } else if (makeRequests(p, state, messages)) {
     // return true;
     // }
 
     return false;
   }
 
   private boolean respondToPeerRequests(Peer p, PeerState state,
       List<Pair<Peer, Message>> messages) {
     /* Unchoke some peers if there are any that seem worthy */
     if (unchoke(p, state, messages)) {
       return true;
     } else if (sendPieces(p, state, messages)
         || cancelPieceRequests(p, state, messages)) {
       return true;
     }
 
     return false;
   }
 
   /**
    * After communication between peers is initialized, they can actually start
    * sending meaningful messages back-and-fourth. This method sees if any
    * messages need to be sent, and if so, returns true. Otherwise, it returns
    * false.
    *
    * @param p
    * @param state
    * @param messages
    * @return
    */
   private boolean sendMeaningfuleMessageToPeer(Peer p, PeerState state,
       List<Pair<Peer, Message>> messages) {
     boolean messageSent = false;
     messageSent = askForSomethingFromPeer(p, state, messages);
     messageSent &= respondToPeerRequests(p, state, messages);
     return messageSent;
   }
 
   /**
    * Initially the remote should be choked according to the specification.
    *
    * @param remotePeer
    * @param state
    * @param messages
    * @return
    */
   private boolean sendInitialChoke(Peer remotePeer, PeerState state,
       List<Pair<Peer, Message>> messages) {
     Pair<ChokeStatus, Instant> choked = null;
 
     synchronized (state) {
       choked = state.isRemoteChoked();
     }
 
     if (choked == null) {
       messages.add(new Pair<Peer, Message>(remotePeer, injector.getInstance(
           ChokeFactory.class).create(this, remotePeer)));
 
       return true;
     }
     return false;
   }
 
   /**
    * Unchoke peers that are interested.
    *
    * @param remotePeer
    * @param state
    * @param messages
    * @return
    */
   private boolean unchoke(Peer remotePeer, PeerState state,
       List<Pair<Peer, Message>> messages) {
 
     Pair<ChokeStatus, Instant> choked = null;
     Pair<InterestLevel, Instant> interest = null;
 
     synchronized (state) {
       choked = state.isRemoteChoked();
       interest = state.getRemoteInterestLevelInLocal();
     }
 
     if (interest == null || InterestLevel.NOT_INTERESTED.equals(interest.fst)) {
       return false; // the peer has no desire to be unchoked
     }
 
     if (choked != null && ChokeStatus.UNCHOKED.equals(choked.fst)) {
       return false; // already unchoked
     }
 
     Set<Peer> peers = null;
 
     synchronized (activePeers) {
       peers = activePeers.keySet();
     }
 
     int unchokedPeerCount = 0;
     for (Peer peer : peers) {
       PeerState peerState = null;
       synchronized (activePeers) {
         peerState = activePeers.get(peer);
       }
       // TODO: add some element of optimistic unchoking here... now this is too
       // optimistic
       if (peerState.isRemoteChoked() != null
           && ChokeStatus.UNCHOKED.equals(peerState.isRemoteChoked().fst)) {
         unchokedPeerCount++;
       }
     }
 
     if (unchokedPeerCount < UNCHOKED_PEER_LIMIT) {
       messages.add(new Pair<Peer, Message>(remotePeer, injector.getInstance(
           UnchokeFactory.class).create(this, remotePeer)));
     }
 
     return true;
   }
 
   /**
    * Make data requests to peers.
    *
    * @param remotePeer
    * @param state
    * @param messages
    * @return
    */
   private boolean makeRequests(Peer remotePeer, PeerState state,
       List<Pair<Peer, Message>> messages) {
     // TODO: be more intelligent about requests (by intelligent, i really be
     // true to spec)
 
     Pair<ChokeStatus, Instant> choked = null;
     List<PieceDeclaration> remotePieces = null;
 
     synchronized (state) {
       choked = state.isLocalChoked();
       remotePieces = state.remoteHasPieces();
     }
 
     if (choked == null || ChokeStatus.CHOKED.equals(choked.fst)) {
       return false; // bummer, choked
     }
 
     Set<Integer> downloadedPieces = null;
 
     synchronized (data) {
       downloadedPieces = data.keySet();
     }
 
     ImmutableList<PieceRequest> alreadyRequestedPieces = null;
     synchronized (state) {
       alreadyRequestedPieces = state.getLocalRequestedPieces();
     }
 
     for (PieceDeclaration piece : remotePieces) {
       if (!downloadedPieces.contains(piece.getPieceIndex())) {
         boolean okayToRequest = true;
         if (alreadyRequestedPieces != null) {
           for (PieceRequest request : alreadyRequestedPieces) {
             if (request.getPieceIndex().equals(piece.getPieceIndex())
                 && request.getRequestTime().isAfter(
                     new Instant().minus(100000L))) {
               okayToRequest = false;
               break;
             }
           }
         }
 
         if (okayToRequest) {
           messages.add(new Pair<Peer, Message>(remotePeer, injector
               .getInstance(RequestFactory.class).create(this, remotePeer,
                   piece.getPieceIndex(), 0, metainfo.getPieceLength())));
         }
 
         return true;
       }
     }
 
     return false;
   }
 
   /**
    * Send pieces to peers.
    *
    * @param remotePeer
    * @param state
    * @param messages
    * @return
    */
   private boolean sendPieces(Peer remotePeer, PeerState state,
       List<Pair<Peer, Message>> messages) {
 
     Pair<ChokeStatus, Instant> choked = null;
     List<PieceRequest> requestedPieces = null;
 
     synchronized (state) {
       choked = state.isRemoteChoked();
       requestedPieces = state.getRemoteRequestedPieces();
     }
 
     if (requestedPieces == null || requestedPieces.size() == 0) {
       return false;
     }
 
     if (choked == null || ChokeStatus.CHOKED.equals(choked.fst)) {
       return false; // remote is choked
     }
 
     byte[] bytes = null;
     int requestedIndex = requestedPieces.get(0).getPieceIndex();
     int requestedOffset = requestedPieces.get(0).getBlockOffset();
     int requestedSize = requestedPieces.get(0).getBlockSize();
 
     synchronized (data) {
       if (data.containsKey(requestedIndex)) {
         bytes = data.get(requestedIndex);
       }
     }
 
     if (bytes == null) {
       return false;
     }
 
     if (requestedOffset > 0) {
       if (requestedOffset + requestedSize <= bytes.length) {
         byte[] newBytes = new byte[requestedSize];
         System.arraycopy(bytes, requestedOffset, newBytes, 0, requestedSize);
         bytes = newBytes;
       } else {
         return false;
       }
     }
 
     messages.add(new Pair<Peer, Message>(remotePeer, injector.getInstance(
         PieceFactory.class).create(this, remotePeer, requestedIndex,
         requestedOffset, bytes)));
 
     return true;
   }
 
   /**
    * Cancel any requests that have been made, but fulfilled by other peers.
    *
    * @param remotePeer
    * @param state
    * @param messages
    * @return
    */
   private boolean cancelPieceRequests(Peer remotePeer, PeerState state,
       List<Pair<Peer, Message>> messages) {
 
     Set<Integer> downloadedPieces;
     ImmutableList<PieceRequest> requestedPieces;
 
     synchronized (data) {
       downloadedPieces = data.keySet();
     }
 
     synchronized (state) {
       requestedPieces = state.getLocalRequestedPieces();
     }
 
     for (PieceRequest request : requestedPieces) {
       if (downloadedPieces.contains(request.getPieceIndex())) {
         messages.add(new Pair<Peer, Message>(remotePeer, injector.getInstance(
             CancelFactory.class).create(this, remotePeer,
             request.getPieceIndex(), request.getBlockOffset(),
             request.getBlockSize())));
       }
     }
 
     return true;
   }
 }
