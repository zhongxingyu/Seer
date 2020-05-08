 package bt.Model;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.BitSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.concurrent.PriorityBlockingQueue;
 
 import bt.Exceptions.NotifyPromptException;
 import bt.Utils.Bencoder2;
 import bt.Utils.TorrentInfo;
 import bt.Utils.Utilities;
 
 /**
  * Bittorrent is the main class for the client. It will initialize the
  * Tracker data fields in its constructor for then establishing a connection
  * with it.
  * 
  * @author Isaac Yochelson, Robert Schomburg and Fernando Geraci
  *
  */
 
 public class Bittorrent {
 	
 	/**
 	 * Will communicate with the tracker every N seconds.
 	 */
 	TrackerRefresher tr;
 	
 	/**
 	 * For each piece, indicates which peer has a true.
 	 */
 	private boolean[][] bitfieldPieces;
 	
 	/**
 	 * Size of each piece
 	 */
 	public int pieceLength;
 	
 	/**
 	 * There should be one entry for each successful connection mapped to peers[].
 	 */
 	boolean[] connections;
 	
 	/**
 	 * Listening server for incoming connections.
 	 */
 	private Server server;
 	
 	/**
 	 * Listener server of the client
 	 */
 	ServerSocket ss;
 	
 	/**
 	 * Name of the file
 	 */
 	private String fileName;
 	
 	
 	/**
 	 * Single Bittorrent instance.
 	 */
 	private static Bittorrent instance = null;
 	
 	/**
 	 * Hard coded .torrent file path.
 	 */
 	private String rscFileFolder = "rsc"+File.separator;;
 	
 	/**
 	 * Decoded .torrent file information.
 	 */
 	private TorrentInfo torrentInfo;
 	
 	/**
 	 * Decoded info hash from the .torrent file.
 	 */
 	private String info_hash;
 	
 	/**
 	 * Input stream from server.
 	 */
 	private DataInputStream readFromServer;
 	
 	/**
 	 * Output stream to server.
 	 */
 	private DataOutputStream writeToServer;
 	
 	/**
 	 * Client socket to communicate with the tracker server.
 	 */
 	private Socket clientSocket;
 	
 	/**
 	 * Randomly generated user ID.
 	 */
 	private String clientID;
 	
 	/**
 	 * Properties file.
 	 */
 	private Properties properties;
 	
 	/**
 	 * Last event in client's activity.
 	 */
 	private String event;
 	
 	/**
 	 * Bytes uploaded so far.
 	 */
 	private int uploaded;
 	
 	/**
 	 * Bytes downloaded so far.
 	 */
 	private int downloaded;
 	
 	/**
 	 * Keep track of downloaded bytes by piece.
 	 */
 	private int[] downloadedByPiece;
 	
 	/**
 	 * Bytes left to download for current file.
 	 */
 	private int left;
 	
 	/**
 	 * Peers list as per the server's response.
 	 */
 	private String[] peers;
 	
 	/**
 	 * double array pieces collection
 	 */
 	private byte[][] collection;
 	
 	//This is going to have to be filled out properly later.
 	/**
 	 * This array stores the correct SHA-1 hashes for each piece of the file this object was
 	 * instantiated to retrieve.
 	 */
 	private byte[][] verificationArray = null;
 	
 	//This is going to have to be filled out properly later.
 	//We must be sure to allocate this on the heap, so that our reference is valid at each Peer object instance.
 	/**
 	 * This array reads true if the piece at the index has been downloaded and verified, and false
 	 * otherwise.
 	 */
 	private boolean[] completedPieces = null;
 	
 	/**
 	 * Number of pieces in this torrent
 	 */
 	private int pieces;
 	
 	/**
 	 * Current list of peers.
 	 */
 	private List<Peer> peerList = null;
 
 	/**
 	 * Priority queue of requests to be made
 	 */
 	private PriorityBlockingQueue<WeightedRequest> weightedRequestQueue = null;
 	
 	/**
 	 * The constructor will initialize all the fields given by the .torrent file.
 	 */
 	private Bittorrent(String torrentFile, String saveFile)	{	
 		// open the file
 		File file = new File((this.rscFileFolder+torrentFile));
 		try {
 			// get file info
 			this.peerList = new ArrayList<Peer>();
 			this.clientID = Utilities.generateID();
 			// make sure clientID is not illegal  
 		    while (this.clientID.substring(0,4).equals("RUBT")) {  
 		    	this.clientID = Utilities.generateID();
 		    }
 			this.torrentInfo = new TorrentInfo(Utilities.getBytesFromFile(file));
 			this.printTorrentInfoFields();
 			this.initClientState();
 			this.properties.load(new FileInputStream(this.rscFileFolder+"prop.properties"));
 			// request the tracker for peers
 			this.sendRequestToTracker();
 			this.tr = new TrackerRefresher(this.torrentInfo, this.peers, this.peerList);
 		} catch (Exception e) {
 			System.err.println(e.getMessage());
 		}
 	}
 	
 	
 	/**
 	 * Updates the value in left bytes for the file.
 	 * @param bytes bytes
 	 */
 	public void updateLeft(int bytes) {
 		this.left -= bytes;
 	}
 	
 	/**
 	 * public getter for info_hash byte array
 	 * @return Byte[] backing ByteBuffer
 	 */
 	public byte[] getTorrentInfoHash() {
 		return this.torrentInfo.info_hash.array();
 	}
 	
 	/**
 	 * Function to populate byte[][] verificationArray
 	 */
 	private void loadVerificationArray() {
 		for (int i = 0; i < this.pieces; i++) {
 			for (int j = 0; j < 20; j++) {
 				verificationArray[i][j] = this.torrentInfo.piece_hashes[i].get();
 			}
 		}
 	}
 	
 	/**
 	 * Function to return byte [][] verificationArray
 	 */
 	public byte[][] getVerificationArray() {
 		return this.verificationArray;
 	}
 	
 	/**
 	 * Initializes the state of the client from the properties file.
 	 * @throws Exception
 	 */
 	private void initClientState() throws Exception {
 		double blocks = ((double)(this.torrentInfo.file_length)) / this.torrentInfo.piece_length;
 		this.pieces = (int)(Math.ceil(blocks));
 		this.pieceLength = this.torrentInfo.piece_length;
 		this.fileName = this.torrentInfo.file_name;
 		this.properties = new Properties();
 		this.properties.load(new FileInputStream(this.rscFileFolder+"prop.properties"));
 		this.event = this.properties.getProperty("event");
 		this.uploaded = Integer.parseInt(this.properties.getProperty("uploaded"));
 		this.downloaded = Integer.parseInt(this.properties.getProperty("downloaded"));
 		this.left = Integer.parseInt(this.properties.getProperty("left"));
 		this.downloadedByPiece = new int[this.pieces];
 		
 		// there are more than one call to create collection.
 		this.collection = new byte[pieces][this.pieceLength];
 		this.verificationArray = new byte[pieces][20];
 		this.completedPieces = new boolean[this.collection.length];
 		this.loadVerificationArray();
 		this.weightedRequestQueue = new PriorityBlockingQueue<WeightedRequest>();
 		this.populateWeightedRequestQueue();
 	}
 	
 	/**
 	 * Returns the sum of bytes downloaded per block.
 	 * @param index Integer index of file piece
 	 * @return int Bytes Sum
 	 */
 	public int getBytesDownloadedByIndex(int index) {
 		return this.downloadedByPiece[index];
 	}
 	
 	/**
 	 * Returns a singleton instance of the Bittorrent main client.
 	 * @return Bittorrent instance
 	 */
 	public static Bittorrent getInstance(String torrentFile, String saveFile) {
 		if(Bittorrent.instance == null) {
 			Bittorrent.instance = new Bittorrent(torrentFile, saveFile);
 		}
 		return Bittorrent.instance;
 	}
 	
 	/**
 	 * Queues bitfields for selecting pieces from peers.
 	 */
 	public void queueBitFields() {
 		// do something
 		// I'm trying to get bitfields first!
 	}
 	
 	/**
 	 * Checks if all the peer connections have been unchoked.
 	 * @return boolean True if any peers are choked, false otherwise. 
 	 */
 	public boolean peersChoked() {
 		for(Peer p : this.peerList) {
 			if(p.isChoked()) return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Checks if a single peer connection has been unchoked.
 	 * @return boolean True if this peer is choked, false otherwise. 
 	 */
 	public boolean peerChoked(Peer peer) {
 		if(peer.isChoked()) 
 			return true;
 		else
 			return false;
 	}
 	
 	/**
 	 * Returns the singleton instance of the client.
 	 * @return Bittorrent instance
 	 * @throws Exception
 	 */
 	public static Bittorrent getInstance() throws Exception {
 		if(Bittorrent.instance == null) throw new Exception("Client was never initialized");
 		return Bittorrent.instance;
 	}
 	
 	/**
 	 * Returns uploaded bytes.
 	 * @return int bytes uploaded
 	 */
 	public int getUploaded() {
 		return this.uploaded;
 	}
 	
 	/**
 	 * Returns downloaded bytes.
 	 * @return int bytes downloaded
 	 */
 	public int getDownloaded() {
 		return this.downloaded;
 	}
 	
 	/**
 	 * Returns left bytes.
 	 * @return int bytes left
 	 */
 	public int getLeft() {
 		return this.left;
 	}
 	
 	/**string info hash.
 	 * @return String info hash
 	 */
 	public String getInfoHashString() {
 		return this.info_hash;
 	}
 	
 	/**
 	 * Returns event.
 	 * @return String event
 	 */
 	public String getEvent() {
 		return this.event;
 	}
 	
 	/**
 	 * Returns reference to this.peerList
 	 */
 	public List<Peer> getPeerList() {
 		return this.peerList;
 	}
 	
 	/**
 	 * It will issue an HTTP GET request to obtain bencoded information 
 	 * about peers and seeds in the server.
 	 * @throws IOException 
 	 */
 	public String sendRequestToTracker() {
 		String response = null;
 		// initializes the server and returns its port
 		this.server = Server.getInstance();
 		
 		//int port = this.server.getPort();//THIS CALL MIGHT FAIL
 		int port = -1;
		while ((port=this.server.getPort()) == -1) {
 			System.err.println("Waiting for available port...");
 			if (port == -1) {
 				try {
 					Thread.sleep(20);
 				} catch (InterruptedException e) {
 					continue;
 				}
 			}
 		}
 		try {
 			
 			// create the tracker URL for the GET request
 
 			URL tracker = new URL(
 				this.torrentInfo.announce_url+
 				"?info_hash="+Utilities.encodeInfoHashToURL(this.info_hash)+
 				"&peer_id="+this.clientID+
 				"&port="+port+
 				"&uploaded="+ this.uploaded+
 				"&downloaded="+ this.downloaded+
 				"&left="+ this.left+
 				"&event="+ this.event);
 			// open streams
 			InputStream fromServer = tracker.openStream();
 			byte[] responseInBytes = new byte[512];
 			// read all the response from the server
 			int b = -1;
 			int pos = 0;
 			while((b = fromServer.read()) != -1) {
 				responseInBytes[pos] = (byte)b;
 				++pos;
 			}
 			this.peers = Utilities.decodeCompressedPeers((Map)Bencoder2.decode(responseInBytes));
 			System.out.println("Peers List:");
 			this.printPeerList();
 			this.connections = new boolean[this.peers.length];
 			// close streams
 			fromServer.close();
 		} catch (Exception e) {
 			System.err.println(e.getMessage());
 		}
 		return response;
 	}
 	
 	/**
 	 * Prints a list of decoded information from the .torrent file
 	 * after the file was successfully decoded by the Bittorrent ctor.
 	 */
 	private void printTorrentInfoFields() {
 		this.info_hash = Utilities.getStringFromByteBuffer(this.torrentInfo.info_hash);
 		System.out.println("\nTRACKER INFO (Successfully Decoded):");
 		System.out.println("-----------------------------------");
 		System.out.println("Announce: "+this.torrentInfo.announce_url);
 		System.out.println("Info Hash: "+this.info_hash);
 		System.out.println("Info Hash URL Encoded: "+Utilities.encodeInfoHashToURL(this.info_hash));
 		System.out.println("File Name: "+this.torrentInfo.file_name);
 		System.out.println("File Length: "+this.torrentInfo.file_length);
 		System.out.println("Piece Size: "+this.torrentInfo.piece_length);
 	}
 	
 	/**
 	 * Sum bytes to specific block for record keeping.
 	 * @param index integer index of file piece
 	 * @param bytes integer count of bytes downloaded.
 	 */
 	public void addBytesToPiece(int index, int bytes) {
 		synchronized(downloadedByPiece) {
 			this.downloadedByPiece[index] += bytes;
 		}
 	}
 	
 	/**
 	 * Total file length.
 	 * @return length The length of the file.
 	 */
 	int getFileLength() {
 		return this.torrentInfo.file_length;
 	}
 	
 	/**
 	* Returns the first available port given the range or -1 if non if available.
 	* @param from left bound
 	* @param to right bound
 	* @return port The port of the server just initialized.
 	*/
 	private int initServer(int from, int to) {
 		int port = from;
 		while(true) {
 			try {
 				ss = new ServerSocket(port);
 				// ss.close();
 				break;
 			} catch (Exception e) {
 				++port;
 				if(port > to) {
 					port = -1;
 					break;
 				}
 			} finally {
 				try {
 					// if(ss != null) ss.close(); // NOT NECESSARY
 				} catch (Exception e) { System.err.println(e.getMessage()); }
 			}
 		}
 		return port;
 	}
 	
 	/**
 	 * Returns the current info hash.
 	 * @return String info_hash.
 	 */
 	public String getInfoHash() {
 		return this.info_hash;
 	}
 	
 	/**
 	 * Return the current peer id.
 	 * @return the peer id of this object.
 	 */
 	public String getPeerId() {
 		return this.clientID+"";
 	}
 	
 	/**
 	 * Terminates server on close.
 	 * @throws IOException
 	 */
 	public void stopServer() throws IOException, Exception {
 		this.server.terminateServer();
 	}
 	
 	/**
 	 * Print a list of available peers as per last refresh from tracker.
 	 */
 	public void printPeerList() {
 		int number = 1;
 		System.out.println("-----------------------------------");
 		synchronized (this.peers) {
 			for(String s: this.peers) {
 				System.out.println(number+". "+s);
 				++number;
 			}
 		}
 		System.out.println("-----------------------------------");
 	}
 	
 	/**
 	 * Connect to the selected peer. Rustic implementation, but it's just a starter.
 	 * @param peer peer number
 	 */
 	public void connectToPeer(int peer) throws Exception {
 		peer = peer - 1;
 		// create peer, attempt connection, feed arguments.
 		if(peer < 0 || peer >= this.peers.length) {
 			throw new IllegalArgumentException("Invalid peer number, out of range.");
 		} else if(this.connections[peer]) {
 			throw new Exception("Connection already established with peer: "+this.peers[peer]);
 		} else {
 			// get info
 			String ip = Utilities.getIPFromString(this.peers[peer]);
 			int port = Utilities.getPortFromString(this.peers[peer]);
 			System.out.println();
 			// attempt peer
 			Peer p = new Peer(	ip,
 								port,
 								Utilities.getHashBytes(this.torrentInfo.info_hash),
 								this.clientID.getBytes(), 
 								this.collection,
 								this.verificationArray,
 								this.completedPieces);
 			// add the peer to the peers list
 			synchronized(peerList) {
 				synchronized(connections) {
 					this.peerList.add(p);
 					// mark the connection as boolean connected in this.connectios
 					this.connections[peer] = true;
 				}
 			}
 		}
 	}
 	
 	/**
 	* Creates a new peer connection to an incoming peer from the server.
 	* @param String IPAddress
 	* @param int port
 	* @param Socket socket
 	*/
 	public void connectToIncomingPeer(String IPAddress, int port, Socket socket) {
 		try {
 			Peer p;
 			p = new Peer(	IPAddress,
 							port,
 							socket,
 							Utilities.getHashBytes(this.torrentInfo.info_hash),
 							this.clientID.getBytes(), 
 							this.collection,
 							this.verificationArray,
 							this.completedPieces);
 			// add the peer to the peers list
 			synchronized(peerList) {
 				synchronized(connections) {
 					this.peerList.add(p);
 				}
 			}
 		} catch(Exception e) { // let's hope this wont happen.
 			System.out.println("Failed to connect to incoming peer: "+IPAddress+":"+port);
 		}
 	}
 	
 	/**
 	 * For the sake of Project 0, this will connect to the selected peer.
 	 * Please do not pay attention to the i+1, that's to compensate an i - 1
 	 * on the called method.
 	 * @param peer ipAndPort
 	 */
 	public void connectToPeer(String peer)throws Exception {
 		boolean connected = false;
 		synchronized(peers) {
 			for(int i = 0; i < this.peers.length; ++i) {
 				if(peer.equals(this.peers[i])) {
 					connected = true;
 					this.connectToPeer(i+1);
 				}
 			}
 		}
 		if(!connected)
 			throw new IllegalArgumentException("Peer does not exist.");
 	}
 	
 	/**
 	 * It will terminate all the connection with the peers.
 	 */
 	public void disposePeers() {
 		synchronized(peerList) {
 			for(Peer p:this.peerList) {
 				p.dispose();
 			}
 		}
 	}
 
 	/**
 	 * It creates a queue for delivery pieces as per their priority.
 	 */
 	private void populateWeightedRequestQueue() {
 		synchronized (weightedRequestQueue) {
 			for (int piece = 0; piece < collection.length - 1; ++piece) {
 				if (!this.completedPieces[piece]) {
 					for (int begin = 0; begin < collection[0].length / Utilities.MAX_PIECE_LENGTH; ++begin) {
 						weightedRequestQueue.offer(new WeightedRequest(
 								piece,
 								begin * Utilities.MAX_PIECE_LENGTH,
 								Utilities.MAX_PIECE_LENGTH));
 					}
 					if (this.pieceLength - ((collection[0].length / Utilities.MAX_PIECE_LENGTH) * Utilities.MAX_PIECE_LENGTH) > 0) {
 						weightedRequestQueue.offer(new WeightedRequest(
 								piece,
 								((collection[0].length / Utilities.MAX_PIECE_LENGTH) * Utilities.MAX_PIECE_LENGTH),
 								(this.pieceLength - ((collection[0].length / Utilities.MAX_PIECE_LENGTH) * Utilities.MAX_PIECE_LENGTH))));
 					}
 				}
 			}
 			int piece = collection.length - 1;
 			if (!this.completedPieces[piece]) {
 				int begin = 0;
 				while ((begin * Utilities.MAX_PIECE_LENGTH) + Utilities.MAX_PIECE_LENGTH < this.getFileLength()) {
 					weightedRequestQueue.offer(new WeightedRequest(
 							piece,
 							begin * Utilities.MAX_PIECE_LENGTH,
 							Utilities.MAX_PIECE_LENGTH));
 					++begin;
 				}
 				if (this.getFileLength() > (this.pieceLength * piece) + (begin * Utilities.MAX_PIECE_LENGTH)) {
 					weightedRequestQueue.offer(new WeightedRequest(
 							piece,
 							begin * Utilities.MAX_PIECE_LENGTH,
 							this.getFileLength() - (this.pieceLength * piece) + (begin * Utilities.MAX_PIECE_LENGTH)));
 				}
 			}
 		}
 	}
 	
 	/**
 	 * This method is a simple algorithm for sending requests for a file.
 	 */
 	public void simpleDownloadAlgorithm() {
 		// This is a temporary algorithm for Project 0.  It will be replaced with a more robust one
 		// when we are doing more than downloading a file from a known seed.
 		Peer peer = null;
 		synchronized(peerList) {
 			peer = peerList.get(0);
 		}
 		for (int i = 0; i < collection.length - 1; ++i) {
 			boolean sent = false;
 			// Attempt to request the piece until it succeeds.
 			while (!sent) {
 				try {
 					peer.requestIndex(new Request(i, 0, 16384));
 					peer.requestIndex(new Request(i, 16384, 16384));
 					sent = true;
 				} catch (IOException e) {
 					System.err.println(e.getMessage());
 				}
 			}
 			sent = false;
 			
 		}
 		boolean sent = false;
 		while (!sent) {
 			if (torrentInfo.file_length > (4.5 * torrentInfo.piece_length)){
 				try {
 					peer.requestIndex(new Request(collection.length -1, 0, 16384));
 					peer.requestIndex(new Request(collection.length -1, 16384, torrentInfo.file_length - (int) (4.5 * torrentInfo.piece_length)));
 					sent = true;
 				} catch (IOException e) {
 					System.err.println(e.getMessage());
 				}
 			} else {
 				try {
 					peer.requestIndex(new Request(collection.length -1, 0,  torrentInfo.file_length - (4 * torrentInfo.piece_length)));
 					sent = true;
 				} catch (IOException e) {
 					System.err.println(e.getMessage());
 				}
 			}
 		}
 	}
 	/**
 	 * This method is our default algorithm for sending requests for the file we are downloading.
 	 */
 	public void downloadAlgorithm() throws NotifyPromptException {
 		this.bitfieldPieces = new boolean[this.pieces][this.peerList.size()];
 		
 		Peer peer = null;	//HERE WE MUST CHOOSE THE PEER WE WILL REQUEST FROM//
 		
 		// Implement algorithm here.
 		for (int i = 0; i < collection.length - 1; ++i) {
 			boolean sent = false;
 			// Attempt to request the piece until it succeeds.
 			while (!sent) {
 				try {
 					peer.requestIndex(new Request(i, 0, 16384));
 					peer.requestIndex(new Request(i, 16384, 16384));
 					sent = true;
 				} catch (IOException e) {
 					System.err.println(e.getMessage());
 				}
 			}
 			sent = false;
 			
 		}
 		boolean sent = false;
 		while (!sent) {
 			if (torrentInfo.file_length > (4.5 * torrentInfo.piece_length)){
 				try {
 					peer.requestIndex(new Request(collection.length -1, 0, 16384));
 					peer.requestIndex(new Request(collection.length -1, 16384, torrentInfo.file_length - (int) (4.5 * torrentInfo.piece_length)));
 					sent = true;
 				} catch (IOException e) {
 					System.err.println(e.getMessage());
 				}
 			} else {
 				try {
 					peer.requestIndex(new Request(collection.length -1, 0,  torrentInfo.file_length - (4 * torrentInfo.piece_length)));
 					sent = true;
 				} catch (IOException e) {
 					System.err.println(e.getMessage());
 				}
 			}
 		}
 
 		// this.queueBitFields();
 		// throw new NotifyPromptException("Download Algorithm done.");
 		
 	}
 	
 	/**
 	 * This is our TEST method for sending requests for the file we are downloading.
 	 */
 	public void downloadAlgorithm(Peer peer) throws NotifyPromptException {
 		this.bitfieldPieces = new boolean[this.pieces][this.peerList.size()];
 		// Implement algorithm here.
 		for (int i = 0; i < collection.length - 1; ++i) {
 			boolean sent = false;
 			// Attempt to request the piece until it succeeds.
 			while (!sent) {
 				try {
 					peer.requestIndex(new Request(i, 0, 16384));
 					peer.requestIndex(new Request(i, 16384, 16384));
 					sent = true;
 				} catch (IOException e) {
 					System.err.println(e.getMessage());
 				}
 			}
 			sent = false;			
 		}
 		boolean sent = false;
 		while (!sent) {
 			if (torrentInfo.file_length > (4.5 * torrentInfo.piece_length)){
 				try {
 					peer.requestIndex(new Request(collection.length -1, 0, 16384));
 					peer.requestIndex(new Request(collection.length -1, 16384, torrentInfo.file_length - (int) (4.5 * torrentInfo.piece_length)));
 					sent = true;
 				} catch (IOException e) {
 					System.err.println(e.getMessage());
 				}
 			} else {
 				try {
 					peer.requestIndex(new Request(collection.length -1, 0,  torrentInfo.file_length - (4 * torrentInfo.piece_length)));
 					sent = true;
 				} catch (IOException e) {
 					System.err.println(e.getMessage());
 				}
 			}
 		}
 
 		// this.queueBitFields();
 		// throw new NotifyPromptException("Download Algorithm done.");
 		
 	}
 
 	/**
 	 * Closes and saves the file after the client successfully downloaded it.
 	 * @throws IOException
 	 */
 	public void saveFile () throws IOException {
 		System.out.println("-- Saving file...");
 		FileOutputStream fileOut = new FileOutputStream(fileName);
 		byte[] fileArray = new byte[torrentInfo.file_length];
 		synchronized(collection) {
 			for(int i = 0; i < this.getFileLength(); ++i ) {
 				fileArray[i] = this.collection[i/this.pieceLength][i%this.pieceLength];
 			}
 		}
 		System.out.println("-- All file bytes completed");
 		fileOut.write(fileArray);
 		fileOut.close();
 	}
 	
 	/**
 	 * It will check if the file is completed for then closing it and save it.
 	 */
 	boolean isFileCompleted() {
 		synchronized(completedPieces) {
 			for(int i = 0; i < this.completedPieces.length; ++i) {
 				if(!this.completedPieces[i]) return false;;
 			}
 		}
 		return true;
 	}
 	
 	/**
 	 * Notifies the tracker the file was successfully downloaded.
 	 */
 	void notifyFullyDownloaded() {
 		int port = -1;
		while ((port=this.server.getPort()) == -1) {
 			System.err.println("Waiting for available port...");
 			if (port == -1) {
 				try {
 					Thread.sleep(20);
 				} catch (InterruptedException e) {
 					continue;
 				}
 			}
 		}
 		this.event = "completed";
 		String response = null;
 		try {	
 			// create the tracker URL for the GET request
 			URL tracker = new URL(
 				this.torrentInfo.announce_url+
 				"?info_hash="+Utilities.encodeInfoHashToURL(this.info_hash)+
 				"&peer_id="+this.clientID+
 				"&port="+port+
 				"&uploaded="+ this.uploaded+
 				"&downloaded="+ this.torrentInfo.file_length+
 				"&left="+ 0+
 				"&event="+ this.event);
 			// open streams
 			InputStream fromServer = tracker.openStream();
 			byte[] responseInBytes = new byte[512];
 			// read all the response from the server
 			int b = -1;
 			int pos = 0;
 			while((b = fromServer.read()) != -1) {
 				responseInBytes[pos] = (byte)b;
 				++pos;
 			}
 			 System.out.println("responseInBytes: " + responseInBytes);  /* */
 			// close streams
 			fromServer.close();
 		} catch (Exception e) {
 			System.err.println(e.getMessage());
 		}
 	}
 	
 	/**
 	 * We are not really sure what this will do.
 	 */
 	private void refreshPeersList() {
 		// stay put for the implementation in incoming episodes!
 	}
 }
