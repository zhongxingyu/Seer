 package bt.Model;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.nio.ByteBuffer;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayDeque;
 import java.util.BitSet;
 import java.util.Date;
 import java.util.Queue;
 
 import bt.Utils.Utilities;
 import bt.View.ClientGUI;
 
 /**
  * Creates a connection with a peer to download a file.
  * @author Isaac Yochelson, Robert Schomburg and Fernando Geraci
  *
  */
 
 public class Peer implements Runnable {
 	
 	/**
 	 * This boolean determines whether is a requested by client or incoming connection.
 	 */
 	private boolean incoming = false;
 	
 	/**
 	 * Actual bytes of torrent files by pieces.
 	 */
 	private byte[][] fileHeap = null;
 	
 	/**
 	 * Verification values per piece.
 	 */
 	private byte[][] verifyHash = null;
 	
 	/**
 	 * Array of true/false as per each piece completion.
 	 */
 	private boolean[] completed = null;
 	
 	/**
 	 * Received bitfield from client.
 	 */
 	private boolean[] bitField = null;
 	
 	/**
 	 * Peer's PeerListener instance.
 	 */
 	private PeerListener listener = null;
 	
 	private int downloadRate = 0;
 
 	private boolean connectionEstablished = false;
 	private Bittorrent parent = null;
 	private int pendingRequests = 0;
 	private boolean choked = true;
 	private boolean haveChoked = false;
 	private boolean interested = false;
 	private Socket dataSocket = null;
 	private InputStream in = null;
 	private OutputStream out = null;
 	private boolean running = true;
 	private byte[] handShakeResponse;
 	public boolean peerAccepted = false;
 	private String IP;
 	private int port;
 	private Date timeout;
 	MessageDigest sha;
 	private int downloaded = 0;
 	private int lastDownloaded = 0;
 	private int uploaded = 0;
 	private int lastUploaded = 0;
 	private long startTime = 0;
 	private long lastUpdate = 0;
 
 	
 	/**
 	 * This field, hash, holds the 20 byte info_hash of the .Torrent file being used by the client which
 	 * instantiated this object.
 	 */
 	private byte[] hash;
 	
 	/**
 	 * This field, clientID, holds the 20 byte peer id of the client which instantiated this object.
 	 */
 	private byte[] clientID;
 	
 	/**
 	 * interestedQueue is a maintained list of the piece requests a particular peer has made to this client.
 	 * When there is space on the outgoing TCP queue, and the connection is not choked, the oldest value in
 	 * this queue will be sent to the peer.
 	 */
 	private Queue <Request> interestedQueue;
 
 	private int uploadRate;
 	
 	/**
 	 * This is a constructor for a Peer taking the address and port as parameters.  The address and port of a
 	 * peer object are immutable during running, and therefore can only be set with this constructor.
 	 * @param address Address of the peer this object represents.
 	 * @param port Port on which to contact the peer which this object represents.
 	 * @param hashIn This field will hold 20 byte hash of the .Torrent file being used by the client which
 	 * instantiated this object.
 	 * @param peerID This field will hold the 20 byte peer id of the client which instantiated this object.
 	 * @param heapReference A reference to the section of the heap where the file is stored during download.
 	 * @param verifyReference A reference to a byte array storing the correct SHA-1 hashes of the pieces of
 	 * the file.
 	 * @param completedReference A reference to a boolean array storing the completeness status of each piece
 	 * of the file.
 	 * @throws UnknownHostException If the address cannot be resolved to a host, this exception will be thrown.
 	 * @throws IOException If a connection cannot be opened to this host, this exception will be thrown.
 	 */
 	
 	public Peer(final String address, final int port, final byte[] hashIn, final byte[] peerID,
 			byte[][] heapReference, byte[][] verifyReference, boolean[] completedReference,
 			Bittorrent creator)
 			throws UnknownHostException, IOException {
 		this.IP = address;
 		this.port = port;
 		this.parent = creator;
 		interestedQueue = new ArrayDeque <Request> ();
 		//dataSocket = new Socket(address, port);
 		//in = dataSocket.getInputStream();
 		//listener = new PeerListener(this, in);
 		//out = dataSocket.getOutputStream();
 		hash = hashIn;
 		clientID = peerID;
 		fileHeap = heapReference;
 		verifyHash = verifyReference;
 		completed = completedReference;	// points to bittorrent.completedPieces
 		bitField = new boolean[fileHeap.length];
 		startTime = System.currentTimeMillis();
 		// sha = MessageDigest.getInstance("SHA-1");
 		synchronized(bitField) {
 			for (int i = 0; i < bitField.length; ++i) {
 				bitField[i] = false;
 			}
 		}
 		// added to start a new thread on the instantiation of a peer.
 		Thread peerThread = new Thread(this);
 		peerThread.start();
 		updateTimeout();
 	}
 	
 	public boolean connectionEstablished() {
 		return this.connectionEstablished;
 	}
 	
 	/**
 	 * Default constructor used for testing from CommandParser interface
 	 */
 	public Peer() {};
 	
 	/**
 	 * Overloaded constructor for accepted peers from the server.
 	 * @param address
 	 * @param port
 	 * @param socket
 	 * @param hashIn
 	 * @param peerID
 	 * @param heapReference
 	 * @param verifyReference
 	 * @param completedReference
 	 * @throws UnknownHostException
 	 * @throws IOException
 	 */
 	public Peer(final String address, final int port, Socket socket, final byte[] hashIn, final byte[] peerID,
 			byte[][] heapReference, byte[][] verifyReference, boolean[] completedReference, Bittorrent creator)
 			throws UnknownHostException, IOException {
 		this.parent = creator;
 		this.IP = address;
 		this.port = port;
 		interestedQueue = new ArrayDeque <Request> ();
 		dataSocket = socket;
 		in = dataSocket.getInputStream();
 		listener = new PeerListener(this, in);
 		out = dataSocket.getOutputStream();
 		hash = hashIn;
 		clientID = peerID;
 		fileHeap = heapReference;
 		verifyHash = verifyReference;
 		completed = completedReference;	// points to bittorrent.completedPieces
 		bitField = new boolean[fileHeap.length];
 		startTime = System.currentTimeMillis();
 		synchronized(bitField) {
 			for (int i = 0; i < bitField.length; ++i) {
 				bitField[i] = false;
 			}
 		}
 		this.incoming = true; // tells this is going to initiate the handshake.
 		// added to start a new thread on the instantiation of a peer.
 		Thread peerThread = new Thread(this);
 		peerThread.start();
 		updateTimeout();
 	}
 	
 	/**
 	 * Updates the downloadRate to bytes per second.
 	 */
 	void updateDownloadRate() {
 		if (this.lastUpdate == 0) {
 			this.lastUpdate = this.startTime;
 		}
 		this.downloadRate = (int)(this.lastDownloaded) / (int)((System.currentTimeMillis()-this.lastUpdate)/1000);
 		this.uploadRate = (int)(this.lastUploaded) / (int)((System.currentTimeMillis()-this.lastUpdate)/1000);
 		this.lastUpdate = System.currentTimeMillis();
 		this.lastDownloaded = 0;
 		this.lastUploaded = 0;
 	}
 	
 	/**
 	 * Returns the current peer's download rate in bps.
 	 * @return current peer's rate in bps
 	 */
 	public synchronized int getDownloadRate() {
 		if(this.downloaded != 0 ) {
 			return this.downloadRate;
 		} else return 0;
 	}
 	
 	/**
 	 * Returns the current peer's upload rate in bps.
 	 * @return current peer's rate in bps
 	 */
 	public synchronized int getUploadRate() {
 		if(this.uploaded != 0 ) {
 			return this.uploadRate;
 		} else return 0;
 	}
 	
 	/**
 	 * Sets lastDownloaded to 0 after getting chocked by the client.
 	 */
 	public void resetDownloaded() {
 		this.lastDownloaded = 0;
 	}
 	
 	/**
 	 * If the peer is incoming is True, false otherwise.
 	 * @return boolean True or False
 	 */
 	public boolean isIncoming() {
 		return this.incoming;
 	}
 		
 	/**
 	 * It sets the handshake response from the peer.
 	 * @param hsr
 	 */
 	public void setHandShakeResponse(byte[] hsr) {
 		if(this.handShakeResponse == null) {
 			this.handShakeResponse = hsr;
 		}
 	}
 	
 	/**
 	 * This method spins off a listener thread to receive file pieces from the peer this object
 	 * represents, calls for a handshake with that peer, then enters a loop in which it serves
 	 * requested file pieces to that peer.
 	 */
 	public void run() {
 		try {
 			dataSocket = new Socket(this.IP, this.port);
 			this.connectionEstablished = true;
 			in = dataSocket.getInputStream();
 			listener = new PeerListener(this, in);
 			out = dataSocket.getOutputStream();
 			Thread.sleep(500);
 		} catch(Exception e) { ClientGUI.getInstance().publishEvent("Connection to peer: "+this+" timed out.");}
 		if(!this.incoming) {
 			handShake();
 		} else {
 			this.listener.receiveHandshake();
 		}
 		
 		Thread listenerThread = new Thread(listener);
 		listenerThread.start(); // changed, it was run(), which won't start a new thread.
 		// This will block until the handshake was done and we can start downloading.
 		
 		while(running) {	// This is the file sending loop.
 			if (this.timeout.getTime() - new Date().getTime() > Utilities.MAX_TIMEOUT) {
 				try {
 					this.parent.terminatePeer(this.toString());
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			} else {
 				if (interestedQueue.isEmpty()) {
 					try {
 						Thread.sleep(50);
 					} catch (InterruptedException e) {
 						continue;
 					}
 				} else {
 					if (!choked && this.interested) {
 						try {
 							Thread.sleep(1000);
 						} catch (Exception e) {}
 						synchronized (interestedQueue) {
 							Request toSend = interestedQueue.poll();
 							if (completed[toSend.getIndex()]) {
 								send(toSend);
 								// this.listener.updateInactive();
 							} else {
 								interestedQueue.offer(toSend);
 							}
 						}
 					}
 				}
 			}					// This is the end of the file sending loop.
 		}
 	}
 	
 	/**
 	 * When called, this method replaces timeout with a new Date object, which by default is set to
 	 * the current date and time.
 	 */
 	void updateTimeout () {
 		timeout = new Date();
 	}
 	
 	/**
 	 * Sends a keep alive signal to the peer this object represents.
 	 * @throws IOException If the system fails to send the TCP message, this exception will be thrown.
 	 */
 	void keepalive () throws IOException {
 		byte[] b = {(byte) 0};
 		out.write(b);
 		out.flush();
 	}
 	
 	/**
 	 * Sends a message to the peer this object represents that it has been choked.
 	 * @throws IOException If the system fails to send the TCP message, this exception will be thrown.
 	 */
 	void choke () throws IOException {
 		byte[] b = new byte[5];
 		b[0] = 0;
 		b[1] = 0;
 		b[2] = 0;
 		b[3] = (byte) 1;
 		b[4] = (byte) 0;
 		out.write(b);
 		out.flush();
 		this.haveChoked = true;
 		ClientGUI.getInstance().publishEvent(">>> I just choked peer "+this);
 		ClientGUI.getInstance().updatePeerInTable(this, ClientGUI.STATUS_UPDATE);
 	}
 	
 	/**
 	 * Sends a message to the peer this object represents that it has been unchoked.
 	 * @throws IOException If the system fails to send the TCP message, this exception will be thrown.
 	 */
 	void unChoke () throws IOException {
 		byte[] b = new byte[5];
 		b[0] = 0;
 		b[1] = 0;
 		b[2] = 0;
 		b[3] = (byte) 1;
 		b[4] = (byte) 1;
 		out.write(b);
 		out.flush();
 		this.haveChoked = false;
 		ClientGUI.getInstance().publishEvent(">>> I just unchoked peer "+this);
 		ClientGUI.getInstance().updatePeerInTable(this, ClientGUI.STATUS_UPDATE);
 	}
 	
 	/**
 	 * Sends a message to the peer this object represents we are interested in data it holds.
 	 * @throws IOException If the system fails to send the TCP message, this exception will be thrown.
 	 */
 	void showInterested() throws IOException {
 		byte[] b = new byte[5];
 		b[0] = 0;
 		b[1] = 0;
 		b[2] = 0;
 		b[3] = (byte) 1;
 		b[4] = (byte) 2;
 		out.write(b);
 		out.flush();
 	}
 	
 	/**
 	 * Sends a message to the peer this object represents that we are not interested in the data it holds.
 	 * @throws IOException If the system fails to send the TCP message, this exception will be thrown.
 	 */
 	void showNotInterested() throws IOException {
 		byte[] b = new byte[5];
 		b[0] = 0;
 		b[1] = 0;
 		b[2] = 0;
 		b[3] = (byte) 1;
 		b[4] = (byte) 3;
 		out.write(b);
 		out.flush();
 	}
 	
 	/**
 	 * This method will notify this client that we have successfully completed the transfer of a piece of
 	 * the file from some peer.  The peer this object represents will therefore be able to remove this
 	 * piece from the queue of interested pieces it is maintaining for this client. 
 	 * @param piece The piece of the file which has been completed.
 	 * @throws IOException 
 	 */
 	void showFinished (int piece) throws IOException {
 		byte[] message = new byte[9];
 		ByteBuffer messageBuffer = ByteBuffer.allocate(9);
 		messageBuffer.putInt(5).put((byte)4).putInt(piece);
 		messageBuffer.rewind();
 		messageBuffer.get(message);
 		out.flush();
 		out.write(message);
 		out.flush();
 	}
 	
 	/**
 	 * Sends a request to a peer.
 	 * @param Request request
 	 */
 	private void send (Request request) {
 		byte[] payload = new byte[request.getLength()];
 		synchronized (fileHeap[request.getIndex()]){
 			for (int i = 0; i < request.getLength(); i++) {
 				payload[i] = fileHeap[request.getIndex()][request.getBegin() + i];
 			}
 		}
 		boolean sent = false;
 		while (!sent) {	// Try to send this message until it succeeds.
 			try {
 				sendPiece (request.getIndex(), request.getBegin(), request.getLength(), payload);
 				sent = true;
 			} catch (IOException e) {
 				continue;
 			}
 		}
 	}
 	
 	/**
 	 * This message sends a piece of the file to the peer this object represents.
 	 * @param index Index of this piece of the file
 	 * @param begin byte offset in the piece where the payload of this message begins.
 	 * @param payloadSize size of the payload in bytes.
 	 * @param payload byte array of the payload.
 	 * @throws IOException will be thrown if the system is unable to dispatch the message.
 	 */
 	void  sendPiece (int index, int begin, int payloadSize, byte[] payload) throws IOException {
 		int length = payloadSize + 9;
 		byte[] message = null;
 		ByteBuffer messageBuffer = ByteBuffer.allocate(length + 4);
 		messageBuffer.putInt(length).put((byte)7).putInt(index).putInt(begin).put(payload);
 		message = messageBuffer.array();
 		out.write(message);
 		out.flush();
 		ClientGUI.getInstance().publishEvent("Piece with index ("+ index +"), begin ("+ begin +
 				"), size ("+ payloadSize +") was sent to "+ this.toString() +".");		
 	}
 	
 	/**
 	 * This method is called by the PeerListener child of this object when a piece of the file is received
 	 * from the peer this object represents.  If this piece is already marked completed, we will assume that
 	 * this peer's 'have' message has been lost, and will resend it.  If it is not marked completed, we will
 	 * add this data to the fileHeap and attempt to verify that the piece is complete.
 	 * @param index The index of this piece of the file.
 	 * @param begin The base zero offset from the beginning of this piece where the payload begins.
 	 * @param payload A byte array of the incoming data.
 	 */
 	void getPiece (int index, int begin, byte[] payload) {
 		updateTimeout();
 		this.pendingRequests--;
 		if (completed[index]) {
 			boolean sent = false;
 			// This is a bit complicated looking, but this block attempts to send a have message every
 			// 50 Milliseconds until it succeeds.
 			while (!sent) {
 				try {
 					showFinished(index);
 					sent = true;
 				} catch (IOException e) {
 					try {
 						Thread.sleep(50);
 					} catch (InterruptedException e1) {
 						continue;
 					}
 				}
 			}
 		} else {
 		// This loops over the bytes in payload and writes them into the file heap.
 			int offset;
 			synchronized(fileHeap[index]) {
 				for (offset = 0; offset < payload.length; ++offset) {
 					fileHeap[index][begin + offset] = payload[offset];
 				}
 			}
 			try {
 				this.parent.addBytesToPiece(index, offset);
 			} catch (Exception e) {ClientGUI.getInstance().publishEvent(e.getMessage());}
 			try {
 				verifySHA(index);
 			} catch (Exception e) {
 				System.err.println(e.getMessage());
 			}
 		}
 	}
 
 	/**
 	 * This method sends a request message to the peer this object represents.
 	 * @param request The index, offset, and length of the request encapsulated in a Request object.
 	 * @throws IOException will be thrown if the system is unable to dispatch the message.
 	 */
 	void requestIndex(Request request) throws IOException {
 		byte[] message = new byte[17];
 		ByteBuffer messageBuffer = ByteBuffer.allocate(17);
 		int index = request.getIndex();
 		int begin = request.getBegin();
 		int length = request.getLength();
 		messageBuffer.putInt(13).put((byte) 6).putInt(index).putInt(begin).putInt(length);
 		// necessary to reset iterator.
 		messageBuffer.rewind();
 		messageBuffer.get(message);
 		out.write(message);
 		out.flush();
 		this.pendingRequests++;
 		ClientGUI.getInstance().publishEvent("-- Piece: "+request.getIndex()+" From: "+request.getBegin()+" Bytes:  "+request.getLength()+" requested from " + this);
 	}
 	
 	/**
 	 * This method is called by the PeerListener child of this object when a request is received from
 	 * the peer this object represents.
 	 * @param index The index of the piece that the peer has requested.
 	 */
 	void requestReceived (int index, int begin, int length) {
 		updateTimeout();
 		if (length > Utilities.MAX_PIECE_LENGTH) {
 			// may drop connection
 			ClientGUI.getInstance().publishEvent("Dropping connection, because requested length ("+ length +
 					") is greater than maximum-piece-length"+ Utilities.MAX_PIECE_LENGTH + ".");
 			this.parent.terminatePeer(this.toString());
 		} 
		if ((index+1) > this.completed.length) {
			System.err.println("Peer = "+ this.toString() +" has requested a piece with index = "+
				index +", out-of-range for piece count = "+ this.completed.length +
				", for torrent = "+ this.parent.getFileName() +".");
		}
 		synchronized(interestedQueue) {
 			interestedQueue.add(new Request(index, begin, length));
 		}
 	}
 	
 	/**
 	 * String methods overridden.
 	 * @return String IP:PORT
 	 */
 	public String toString() {
 		return this.IP+":"+this.port;
 	}
 	
 	/**
 	 * Getter for choked status.
 	 * @return boolean True if choked, false otherwise.
 	 */
 	public boolean isChoked() {
 		return (this.choked || this.haveChoked);
 	}
 	
 	/**
 	 * Returns bytes downloaded so far from this peer.
 	 * @return
 	 */
 	public int getDownloaded() {
 		return this.downloaded;
 	}
 	
 	/**
 	 * Updates value.
 	 * @param bytes
 	 */
 	public void updateDownloaded(int bytes) {
 		this.downloaded += bytes;
 		this.lastDownloaded += bytes;
 		// this should trigger a table refresh.
 	}
 	
 	/**
 	 * Returns bytes downloaded so far from this peer.
 	 * @return
 	 */
 	public int getUploaded() {
 		return this.uploaded;
 	}
 	
 	/**
 	 * Updates value.
 	 * @param bytes
 	 */
 	public void updateUploaded(int bytes) {
 		this.uploaded += bytes;
 		this.lastUploaded += bytes;
 		// this should trigger a table refresh.
 	}
 	
 	/**
 	 * This method is called by the PeerListener child of this object when a have is received from
 	 * the peer this object represents.
 	 * @param index The index of the piece that the peer has acknowledged complete..
 	 */
 	void haveReceived (int index) {
 		if ((index >= bitField.length) || (index < 0)) {
 			ClientGUI.getInstance().publishEvent("haveReceived index ("+ index +") is out-of-range.");
 			return;
 		}
 		updateTimeout();
 		synchronized (interestedQueue) {
 			synchronized (bitField) {
 				bitField[index] = true;
 				interestedQueue.remove(index);
 			}
 		}
 	}
 	
 	/**
 	 * This method sends a cancel message to the peer this object represents.
 	 * @param index piece of the file to be requested.
 	 * @param begin byte offset
 	 * @param length byte offset
 	 * @throws IOException will be thrown if the system is unable to dispatch the message.
 	 */
 	void cancelIndex(int index, int begin, int length) throws IOException {
 		Byte b = (byte) 8;
 		byte[] message = new byte[17];
 		ByteBuffer messageBuffer = ByteBuffer.allocate(13);
 		messageBuffer.put(b).putInt(index).putInt(begin).putInt(length);
 		messageBuffer.get(message);
 		out.write(message);
 		out.flush();
 	}
 	
 	/**
 	 * Received a peer's bitfield.
 	 * @param bitfield
 	 */
 	void receiveBitfield(byte[] bitfield) {
 		updateTimeout();
 		byte[] pieces = new byte[bitfield.length-1]; // substract the length of the bitfield's bytes.
 		for(int i = 0; i < pieces.length; ++i) {
 			pieces[i] = bitfield[i+1]; 
 		}
 		BitSet bs = BitSet.valueOf(pieces);
 		// Apparently we need to deal with some endianess problem... need to discuss this with you guys.
 		// FOR EACH BYTE
 		//	FOR EIGHT BITS
 		int index = 0;
 		mainLoop : for (int i = 0; i < pieces.length; ++i) {
 			int bit = 7;
 			int base = (bit*i);
 			synchronized (bitField) {
 				for(bit = bit + base; bit >= base; --bit) {
 					if(index < this.bitField.length) {
 						this.bitField[index] = bs.get(bit);
 						index++;
 					} else break mainLoop;
 				}
 			}
 		}	
 	}
 	
 	/**
 	 * This method is used to determine if a peer has a piece of the file being downloaded.
 	 * @param index the piece of the file being queried
 	 * @return true if the peer this object represents has that piece of the file, false otherwise.
 	 */
 	boolean peerHasPiece(int index) {
 		synchronized (bitField) {
 			return bitField[index];
 		}
 	}
 	
 	/**
 	 * This method can be used to send a bitfield to the peer this object represents.  This should only
 	 * be done as the first message to this peer.  A bitfield is a byte[] with each index that the downloader,
 	 * this client, has received set to one and the rest set to zero. Downloaders which don't have anything yet
 	 * may skip the 'bitfield' message. The first byte of the bitfield corresponds to indices 0 - 7 from high
 	 * bit to low bit, respectively. The next one 8-15, etc. Spare bits at the end are set to zero.
 	 * @throws IOException if the system fails to send the TCP packet properly, this exception will be thrown.
 	 */
 	void sendBitfield() throws IOException {
 		BitSet bs = new BitSet();
 		synchronized(completed) {
 			for (int i = 0; i < completed.length; ++i) {
 				if (completed[i]) {
 					bs.set(i, true);
 				} else {
 					bs.set(i, false);
 				}
 			}
 			// get byte representation of bitfield in little endian
 			byte[] bytesInBitField = bs.toByteArray();
 			// calculate number of 0s to shift left
 			int shiftLeft = (8*bytesInBitField.length) - bs.cardinality();
 			// get the value to be adjusted from the array
 			int last = bytesInBitField[bytesInBitField.length-1];
 			// adjust it - working ok.
 			bytesInBitField[bytesInBitField.length-1] =  (byte)(last << shiftLeft);
 			// instantiate the actual array to be sent.
 			byte[] toSend = new byte[bytesInBitField.length+2];
 			int length = bytesInBitField.length+1;
 			ByteBuffer bf = ByteBuffer.allocate((5+bytesInBitField.length));
 			bf.rewind();
 			bf.putInt(length);
 			bf.put((byte)5);
 			for(byte b : bytesInBitField) {
 				bf.put(b);
 			}
 			bf.rewind();
 			byte[] sendThis = bf.array();
 			bf.get(sendThis);
 			out.write(sendThis);
 			out.flush();
 		}
 	}
 	
 	
 	/**
 	 * Sets the interested bit flag on this peer's connection.  When a peer sets not interested we
 	 * clear the interestedQueue.
 	 * @param value Value for interested flag.
 	 */
 	void setInterested (boolean value) {
 		updateTimeout();
 		interested = value;
 		if (!value) {
 			synchronized(interestedQueue) {
 				interestedQueue.clear();
 			}
 		}
 	}
 	
 	/**
 	 * Sets the choked bit flag on this peer's connection.  We clear the interestedQueue when we
 	 * are choked.
 	 * @param value Value for the choked flag.
 	 */
 	void setChoke (boolean value) {
 		updateTimeout();
 		this.choked = value;
 		ClientGUI.getInstance().updatePeerInTable(this, ClientGUI.STATUS_UPDATE);
 		if (value) {
 			synchronized(interestedQueue) {
 				interestedQueue.clear();
 			}
 		}
 	}
 	
 	/**
 	 * Performs the handshake to open connection with peer.
 	 */
 	public void handShake() {
  		byte[] handShakeBA = new byte[68];		
 		ByteBuffer handShakeBB = ByteBuffer.allocate(68);
 		String btProtocol = "BitTorrent protocol";
 		byte[] b1 = new byte[1];
 		b1[0] = (byte) 19;
 		byte[] b2 = new byte[8];
 		for (int i = 0; i < 8; i++) {
 			b2[i] = (byte) 0;
 		}
 		handShakeBB
 			.put(b1)
 			.put(btProtocol.getBytes())
 			.put(b2)
 			.put(this.hash)
 			.put(clientID);
 		// added rewind and change the array initialization.
 		handShakeBB.rewind();
 		handShakeBB.get(handShakeBA);
 		try {
 			this.out.write(handShakeBA);
 			this.out.flush();
 		} catch (Exception e) { 
 			ClientGUI.getInstance().publishEvent("Error in handshake");
 			/* hope this never happens */ 
 		}		
 	}
 	
 	/**
 	 * This method can be called before removing the last reference to this object to clear system
 	 * resources and heap memory.
 	 */
 	void dispose () {
 		synchronized(interestedQueue) {
 			synchronized(fileHeap) {
 				listener.dispose();
 				fileHeap = null;
 				verifyHash = null;
 				completed = null;
 				listener = null;
 				choked = true;
 				interested = false;
 				running = false;
 				boolean closed = false;
 				// This loop attempts to close dataSocket once every 50 Milliseconds until it succeeds.
 				while (!closed) {
 					try {
 						dataSocket.close();
 						closed = true;
 					} catch (IOException e) {
 						try {
 							Thread.sleep(50);
 							closed = true;
 						} catch (InterruptedException e1) {
 							continue;
 						}
 					}
 				}
 				in = null;
 				out = null;
 				dataSocket = null;
 				hash = null;
 				clientID = null;
 				interestedQueue.clear();
 				interestedQueue = null;	
 			}
 		}
 	}
 	
 	/**
 	 * This method verifies that the piece of the file with the given index is complete and valid.  If the
 	 * file is complete and valid, It will be marked complete in the completed array, and the peer will be
 	 * sent a have message.
 	 * @param index The piece of the file being verified
 	 * @throws IOException This exception is thrown if we fail to open the file for saving.
 	 */
 	private void verifySHA(int index) throws IOException {
 		try {
 		MessageDigest sha = MessageDigest.getInstance("SHA-1");
 			byte[] toDigest = null;
 			if (index < fileHeap.length - 1) {
 				toDigest = new byte[this.parent.pieceLength];
 				synchronized(fileHeap) {
 					// load full-sized piece to be hashed
 					for(int i = 0; i < toDigest.length; ++i) {
 						toDigest[i] = fileHeap[index][i];
 					}
 				}
 			} else {
 				toDigest = new byte[this.parent.getFileLength() - ((fileHeap.length-1)*this.parent.pieceLength)];
 				synchronized(fileHeap) {
 					// load possibly partial-sized piece to be hashed
 					for(int i = 0; i < toDigest.length; ++i) {
 						toDigest[i] = fileHeap[index][i];
 					}
 				}
 			}
 			// hash this piece 
 			byte[] test = sha.digest(toDigest);
 			// test the piece
 			if (Utilities.sameArray(verifyHash[index], test)) { 
 				// piece hash is correct
 				ClientGUI.getInstance().publishEvent("We have completed piece: " + index);
 				ClientGUI.getInstance().updateProgressBar(toDigest.length);
 				// update downloaded
 				try {
 					Bittorrent.getInstance().updateDownloaded(toDigest.length);
 				} catch (Exception e) { /* shouldnt happen */ }
 				boolean sent = false;
 				// This is a bit complicated looking, but this block attempts to send a have message every
 				// 50 Milliseconds until it succeeds.
 				while (!sent) {
 					try {
 						// Notify this peer that client now has piece
 						this.showFinished(index);
 						sent = true;
 					} catch (IOException e) {
 						try {
 							Thread.sleep(50);
 						} catch (InterruptedException e1) {
 							continue;
 						}
 					}
 				}
 				synchronized(completed) {
 					if(!this.parent.isFileCompleted()){
 						completed[index] = true;
 						if(this.parent.isFileCompleted()) {
 							this.parent.notifyFullyDownloaded(); // notifies tracker
 							this.parent.saveFile(); // create the downloaded file
 							ClientGUI.getInstance().publishEvent("\n-- FILE SUCCESSFULLY DOWNLOADED --");
 						}
 					}
 				}
 			} else { // piece hash is incorrect
 				// System.out.println("Index # " + index + " failed was not verified.");
 			}
 		} catch (NoSuchAlgorithmException e) {
 			ClientGUI.getInstance().publishEvent(e.getMessage());
 		}
 	}
 	
 	
 	public int getPendingRequests() {
 		return this.pendingRequests;
 	}
 	
 	/**
 	 * It does a byte match of the info_hashes.
 	 * @param response Response to our handshake from peer.
 	 * @return boolean True if match, false otherwise.
 	 */
 	public boolean validateInfoHash(byte[] response) {
 		if(!this.peerAccepted) {
 			// match info_hashes
 			if(Utilities.matchBytes(
 					Utilities.getInfoHashFromHandShakeResponse(response), 
 					this.hash)) {
 				this.peerAccepted = true;
 				return this.peerAccepted;
 			} else {
 				ClientGUI.getInstance().publishEvent("ERROR: info_hash doesn't match, connection terminated.");
 				parent.terminatePeer(this.toString());
 			}
 		}
 		return false;
 	}
 }
