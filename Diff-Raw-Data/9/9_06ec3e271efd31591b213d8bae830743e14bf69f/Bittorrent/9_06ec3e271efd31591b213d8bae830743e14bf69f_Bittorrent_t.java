 package bt.Model;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.URL;
 import java.net.UnknownHostException;
 import java.security.NoSuchAlgorithmException;
 import java.util.Arrays;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.concurrent.PriorityBlockingQueue;
 import bt.Exceptions.DuplicatePeerException;
 import bt.Exceptions.UnknownBittorrentException;
 import bt.Utils.Bencoder2;
 import bt.Utils.TorrentInfo;
 import bt.Utils.Utilities;
 import bt.View.ClientGUI;
 import bt.View.UserInterface;
 
 /**
  * Bittorrent is the main class for the client. It will initialize the
  * Tracker data fields in its constructor for then establishing a connection
  * with it.
  * 
  * @author Isaac Yochelson, Robert Schomburg and Fernando Geraci
  *
  */
 
 public class Bittorrent {
 	
 	private static final String TEMP_FILE = "rsc"+File.separator+"cs352.tmp";
 
 	/**
 	 * Will communicate with the tracker every N seconds.
 	 */
 	TrackerRefresher tr;
 	
 	/**
 	 * Whenever this countdown reaches 0, which will occur every three minutes, we will add all pieces
 	 * that have not been verified to our weighted request queue.
 	 */
 	private int countdownToRequeue = 0;
 	
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
 	private String rscFileFolder = "rsc"+File.separator;
 	
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
 	 * Responsible for spooling, choking and unchoking peers (sound terrible)
 	 */
 	private PeerSpooler peerSpooler;
 
 	/**
 	 * Priority queue of requests to be made
 	 */
 	private PriorityBlockingQueue<WeightedRequest> weightedRequestQueue = null;
 	
 	private ClientGUI cGUI;
 	
 	private boolean isPaused = false;
 	
 	/**
 	 * The constructor will initialize all the fields given by the .torrent file.
 	 */
 	private Bittorrent(String torrentFile, String saveFile) throws Exception	{	
 		// open the file
 		File file = new File(torrentFile);
 		// get file info
 		this.peerList = new ArrayList<Peer>();
 		this.clientID = Utilities.generateID();
 		// make sure clientID is not illegal  
 	    while (this.clientID.substring(0,4).equals("RUBT")) {  
 	    	this.clientID = Utilities.generateID();
 	    }
 		this.torrentInfo = new TorrentInfo(Utilities.getBytesFromFile(file));
 /*		this.interval = 0; */
 		this.printTorrentInfoFields();
 		this.initClientState();
 		this.fileName = saveFile;
 		this.properties.load(new FileInputStream(this.rscFileFolder+"prop.properties"));
 		// request the tracker for peers
 		this.sendRequestToTracker();
 		this.tr = TrackerRefresher.getInstance(
 				this.torrentInfo, this.peers, this.peerList/*, this.interval*/);
 		// UserInterface ui = UserInterface.getInstance();
 		this.cGUI = ClientGUI.getInstance();
 		cGUI.publishEvent("Connection Successful, welcome");
 	}
 	
 	/**
 	 * Updates the downloaded int value.
 	 * @param int downloaded
 	 */
 	public void updateDownloaded(int downloaded) {
 		this.downloaded += downloaded;
 		this.left = this.torrentInfo.file_length - this.downloaded;
 	}
 	
 	/**
 	 * Returns the name of the torrent file.
 	 * @return
 	 */
 	public String getFileName() {
 		return this.torrentInfo.file_name;
 	}
 	
 	/**
 	 * This method will be called and execute specific instructions from different projects.
 	 * For example, for Project 1, it will execute automatic connections to specific peers.
 	 */
 	public void startExecuting() throws Exception {
 		File torrentFile = new File(this.torrentInfo.file_name);
 		File altTorrentFile = new File(this.fileName);
 		// if the file exists, just load it into memory for serving.
 		this.cGUI.updateProgressBar(this.downloaded);
 		if(torrentFile.exists() || altTorrentFile.exists()) {
 			Utilities.initializeFileHeap(this.fileName);
 			int i = 0;
 			for(boolean b : this.completedPieces) {
 				this.completedPieces[i] = true;
 				++i;
 			}
 			this.cGUI.publishEvent("File successfully loaded in client's heap for uploading.");
 			this.cGUI.updateProgressBar(this.torrentInfo.file_length);
 			this.setEvent("completed");
 		} else {
			// The idea is the following:
			// We initially connect to 4 random peers.  We will allow up to 6 simultaneously unchoked connections,
			// which allows for 2 incoming unchoked connections.  Every 30 seconds we will choke all but the
			// top 3 peers, then unchoke a random peer.
			// we will leave the other connections open, sending keep alives accordingly for potentially unchoking them
 			// on the next cycle.
 			this.cGUI.publishEvent("-- Establishing connections to Peers, please wait... ");
 			this.connectToAllPeers(6); // up to 6 connections open.
 			/*
 			this.connectToPeer("128.6.171.8:6927");
 			this.connectToPeer("128.6.171.7:6888");
 			this.connectToPeer("128.6.171.6:6928");
 			this.connectToPeer("128.6.171.5:6972");
 			this.connectToPeer("128.6.171.4:6988");
 			*/
 			// this.connectToPeer("76.117.89.150:6881");
 
 			//this.connectToPeer("76.117.89.150:6881");
 
 			this.peerSpooler = new PeerSpooler(this, 30*1000); // spool peers every 30 seconds.
 			this.downloadAlgorithm();
 		}
 	}
 	
 	private void connectToAllPeers(int limit) {
 		for(int i = 0; (i < this.peers.length) && (i < limit); ++i) {
 			String peer = this.peers[i];
 			try {
 				this.connectToPeer(peer);
 			} catch (Exception e) { this.cGUI.publishEvent("ERROR: Connecting to peer: "+peer+" failed."); }
 		}
 	}
 	
 	/**
 	 * @deprecated
 	 */
 	public void updateGUIState() {
 		this.cGUI.updateTableModel();
 	}
 	
 	/**
 	 * Returns the entire list of peers from the server.
 	 * @return
 	 */
 	public String[] getPeersArray() {
 			return this.peers.clone();
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
 	 * Update the current list of peers from the tracker.
 	 * @param updatedList
 	 */
 	public void setPeersArray(String[] updatedList) {
 		this.peers = updatedList;
 		this.cGUI.updatePeerModel();
 	}
 	
 	/**
 	 * Initializes the state of the client from the properties file.
 	 * @throws IOException thrown if we cannot open the torrent file to read
 	 * @throws FileNotFoundException thrown if we cannot find the torrent file
 	 */
 	private void initClientState() throws FileNotFoundException, IOException {
 		double blocks = ((double)(this.torrentInfo.file_length)) / this.torrentInfo.piece_length;
 		this.pieces = (int)(Math.ceil(blocks));
 		this.pieceLength = this.torrentInfo.piece_length;
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
 		File temp = new File(TEMP_FILE);
 		if(temp.exists()){
 			try {
 				int[] intArray = new int[3];
 				try {
 					Utilities.loadState(intArray, collection, 
 							this.pieceLength, 
 							this.pieces, 
 							temp, 
 							this.completedPieces, 
 							this.torrentInfo, 
 							this.verificationArray);
 					this.deleteTmpFile();
 				} catch (NoSuchAlgorithmException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (UnknownBittorrentException e) {
 					System.out.println("FATAL: I am having and identity crisis.");
 					e.printStackTrace();
 					System.exit(-1);
 				}
 				this.downloaded = intArray[0];
 				this.uploaded = intArray[1];
 				this.left = intArray[2];
 			} catch (IOException e) {
 				System.err.println("Unable to load file pieces from previous session.");
 				for (int i = 0; i < completedPieces.length; ++i) {
 				}
 				this.downloaded = 0;
 				this.uploaded = 0;
 				this.left = torrentInfo.file_length;
 			}
 		} else {
 			for (int i = 0; i < completedPieces.length; ++i) {
 				completedPieces[i] = false;
 			}
 			this.downloaded = 0;
 			this.uploaded = 0;
 			this.left = torrentInfo.file_length;
 		}
 	}
 	
 	/**
 	 * Returns the length of each piece.
 	 * @return int
 	 */
 	public int getPieceLength() {
 		return this.torrentInfo.piece_length;
 	}
 	
 	/**
 	 * Returns a reference to the client's file's heap.
 	 * @return byte[][]
 	 */
 	public byte[][] getFileHeap() {
 		return this.collection;
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
 	public static Bittorrent getInstance(String torrentFile, String saveFile) throws Exception {
 		if(Bittorrent.instance == null) {
 			Bittorrent.instance = new Bittorrent(torrentFile, saveFile);
 		}
 		return Bittorrent.instance;
 	}
 	
 	/**
 	 * Checks if all the peer connections have been unchoked.
 	 * @return boolean True if any peers are choked, false otherwise. 
 	 */
 	public boolean peersChoked() {
 		synchronized(peerList) {
 			for(Peer p : this.peerList) {
 				if(p.isChoked()) return true;
 			}
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
 	 * @throws UnknownBittorrentException thrown if the client was never initialized.
 	 */
 	public static Bittorrent getInstance() throws UnknownBittorrentException  {
 		if(Bittorrent.instance == null) return null;
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
 	 * @return boolean value as to whether client has downloaded no pieces at all
 	 */
 	public boolean noPieces() {
 		if (this.downloaded == 0) return true;
 		else return false;
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
 			Map trackerResponse = (Map)Bencoder2.decode(responseInBytes);
 			this.peers = Utilities.decodeCompressedPeers(trackerResponse);
 	/*		this.interval = Utilities.decodeInterval(trackerResponse); */
 			ClientGUI.getInstance().publishEvent("Peers List:");
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
 		
 		ClientGUI.getInstance().publishEvent("TRACKER INFO (Successfully Decoded):");
 		ClientGUI.getInstance().publishEvent("-----------------------------------");
 		ClientGUI.getInstance().publishEvent("Announce: "+this.torrentInfo.announce_url);
 		ClientGUI.getInstance().publishEvent("Info Hash: "+this.info_hash);
 		ClientGUI.getInstance().publishEvent("Info Hash URL Encoded: "+Utilities.encodeInfoHashToURL(this.info_hash));
 		ClientGUI.getInstance().publishEvent("File Name: "+this.torrentInfo.file_name);
 		ClientGUI.getInstance().publishEvent("File Length: "+this.torrentInfo.file_length);
 		ClientGUI.getInstance().publishEvent("Piece Size: "+this.torrentInfo.piece_length);
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
 	public int getFileLength() {
 		return this.torrentInfo.file_length;
 	}
 	
 	/**
 	 * Returns the verification array for the SHA.
 	 * @return byte[][] verification array.
 	 */
 	public byte[][] getVerifyHash() {
 		return this.verificationArray;
 	}
 	
 	/**
 	* Returns the first available port given the range or -1 if non if available.
 	* @param from left bound
 	* @param to right bound
 	* @return port The port of the server just initialized.
 	*/
 	private int initServer(int from, int to) {
 		int port = from;
 		while(port > 0) {
 			try {
 				ss = new ServerSocket(port);
 				// ss.close();
 				break;
 			} catch (Exception e) {
 				++port;
 				if(port > to) {
 					port = -1;
 				}
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
 	 * @throws UnknownBittorrentException 
 	 * @throws IOException 
 	 */
 	public void stopServer() throws IOException, UnknownBittorrentException  {
 		TrackerRefresher.getInstance().notifyClose();
 		this.server.terminateServer();
 	}
 	
 	/**
 	 * Print a list of available peers as per last refresh from tracker.
 	 */
 	public void printPeerList() {
 		int number = 1;
 		ClientGUI.getInstance().publishEvent("-----------------------------------");
 		synchronized (this.peers) {
 			for(String s: this.peers) {
 				ClientGUI.getInstance().publishEvent(number+". "+s);
 				++number;
 			}
 		}
 		ClientGUI.getInstance().publishEvent("-----------------------------------");
 	}
 	
 	/**
 	 * Connect to the selected peer. Rustic implementation, but it's just a starter.
 	 * @param peer peer number
 	 * @throws IOException thrown if we cannot open the torrent file for reading
 	 * @throws UnknownHostException thrown if we cannot resolve the tracker's hostname
 	 * @throws DuplicatePeerException thrown if the peer is already in the peer list
 	 */
 	public void connectToPeer(int peer) throws UnknownHostException, IOException, DuplicatePeerException {
 		// create peer, attempt connection, feed arguments.
 		try {
 			if(peer < 0 || peer >= this.peers.length) {
 				throw new IllegalArgumentException("Invalid peer number, out of range.");
 			} else if(this.connections[peer]) {
 				throw new DuplicatePeerException("Connection already established with peer: "+this.peers[peer]);
 			} else {
 				// get info
 				String ip = Utilities.getIPFromString(this.peers[peer]);
 				int port = Utilities.getPortFromString(this.peers[peer]);
 				// attempt peer
 				Peer p = new Peer(	ip,
 									port,
 									Utilities.getHashBytes(this.torrentInfo.info_hash),
 									this.clientID.getBytes(), 
 									this.collection,
 									this.verificationArray,
 									this.completedPieces,
 									this);
 				// add the peer to the peers list
 				synchronized(peerList) {
 					synchronized(connections) {
 						this.peerList.add(p);
 						ClientGUI.getInstance().updatePeerInTable(p, ClientGUI.ADDPEER_UPDATE);
 						// mark the connection as boolean connected in this.connectios
 						this.connections[peer] = true;
 					}
 				}
 			}
 		}catch (Exception e) {
 			System.out.println("Connection to peer failed");
 		}
 	}
 	
 	/**
 	* Creates a new peer connection to an incoming peer from the server.
 	* @param String IPAddress
 	* @param int port
 	* @param Socket socket
 	 * @throws IOException thrown if we cannot open the torrent file for reading
 	 * @throws UnknownHostException thrown if we cannot resolve the tracker's hostname
 	*/
 	public void connectToIncomingPeer(String IPAddress, int port, Socket socket) throws UnknownHostException, IOException {
 		Peer p;
 		p = new Peer(	IPAddress,
 						port,
 						socket,
 						Utilities.getHashBytes(this.torrentInfo.info_hash),
 						this.clientID.getBytes(), 
 						this.collection,
 						this.verificationArray,
 						this.completedPieces,
 						this);
 		// add the peer to the peers list
 		synchronized(peerList) {
 			synchronized(connections) {
 				this.peerList.add(p);
 				ClientGUI.getInstance().updatePeerInTable(p, ClientGUI.ADDPEER_UPDATE);
 			}
 		}
 	}
 	
 	/**
 	 * For the sake of Project 0, this will connect to the selected peer.
 	 * Please do not pay attention to the i+1, that's to compensate an i - 1
 	 * on the called method.
 	 * @param peer ipAndPort
 	 * @throws DuplicatePeerException 
 	 * @throws IOException 
 	 * @throws UnknownHostException 
 	 */
 	public void connectToPeer(String peer)throws IllegalArgumentException, UnknownHostException, IOException, DuplicatePeerException {
 		boolean connected = false;
 		synchronized(peers) {
 			String[] peers = this.getPeersArray();
 			for(int i = 0; i < peers.length; ++i) {
 				if(peer.equals(peers[i])) {
 					connected = true;
 					this.connectToPeer(i);
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
 				while ((begin * Utilities.MAX_PIECE_LENGTH) + Utilities.MAX_PIECE_LENGTH < this.getFileLength() - (piece * this.pieceLength)) {
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
 							this.getFileLength() - ((this.pieceLength * piece) + (begin * Utilities.MAX_PIECE_LENGTH))));
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
 	 * For each peer ask 3 pieces and re-queue every N milliseconds. Each peer is responsible for
 	 * keeping state on number of served and pending requests.
 	 */
 	public void downloadAlgorithm() {
 		while(!isFileCompleted()) {
 			if (this.countdownToRequeue <= 0) {
 				this.populateWeightedRequestQueue();
 				this.countdownToRequeue = 15;
 			}
 			--this.countdownToRequeue;
 			updateWeights();
 			boolean requested = false;
 			PriorityBlockingQueue<WeightedRequest> nextQueue = new PriorityBlockingQueue<WeightedRequest>();
 			synchronized(weightedRequestQueue) {
 				synchronized (peerList) {
 					while (!weightedRequestQueue.isEmpty() && !this.isPaused) {
 						WeightedRequest req = weightedRequestQueue.poll();
 						requested = false; // get the first piece we need
 						for (Peer peer: peerList) { // for each peer we are connected to
 							if (!requested) { // always run at least once per peer
 								int pendingReqs = peer.getPendingRequests();
 								int pieceNumber = req.getIndex();
 								boolean isChoked = peer.isChoked();
 								boolean hasPiece = peer.peerHasPiece(pieceNumber);
 								if (pendingReqs < 3 && hasPiece && !isChoked) {
 									boolean sent = false;
 									requested = true;
 									while (!sent) {
 										try {
 											peer.requestIndex(req.getRequest());
 											sent = true;
 										} catch (IOException e) {
 											try {
 												Thread.sleep(50);
 											} catch (InterruptedException e1) {
 												continue;
 											}
 										}
 									}
 								}
 							}
 						}
 						if (!requested) {
 							nextQueue.offer(req);
 						}
 					}
 				}
 				weightedRequestQueue = nextQueue;
 			}
 			try {
 				Thread.sleep(2000);
 			} catch (InterruptedException e) {
 				continue;
 			}
 		}
 	}
 	
 	/**
 	 * Updates the requests' weights in the queue.
 	 */
 	private void updateWeights() {
 		int[] weights = new int[pieces];
 		synchronized(peerList) {
 			for (Peer peer: peerList) {
 				for (int index = 0; index < this.pieces; index++) {
 					if (peer.peerHasPiece(index)) {
 						++weights[index];
 					}
 				}
 			}
 		}
 		synchronized(weightedRequestQueue) {
 			for (WeightedRequest req: weightedRequestQueue) {
 				req.update(weights[req.getIndex()]);
 			}
 		}
 	}
 
 	/**
 	 * Closes and saves the file after the client successfully downloaded it.
 	 * @throws IOException
 	 */
 	public void saveFile () throws IOException {
 		this.deleteTmpFile();
 		FileOutputStream fileOut = new FileOutputStream(fileName);
 		byte[] fileArray = new byte[torrentInfo.file_length];
 		synchronized(collection) {
 			for(int i = 0; i < this.getFileLength(); ++i ) {
 				fileArray[i] = this.collection[i/this.pieceLength][i%this.pieceLength];
 			}
 		}
 		fileOut.write(fileArray);
 		fileOut.close();
 	}
 	
 	/**
 	 * It will check if the file is completed for then closing it and save it.
 	 */
 	boolean isFileCompleted() {
 		synchronized(completedPieces) {
 			for(int i = 0; i < this.completedPieces.length; ++i) {
 				if(!this.completedPieces[i]) return false;
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
 		this.setEvent("completed");
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
 			this.cGUI.publishEvent("-- Tracker notified of download completion.");
 			// close streams
 			fromServer.close();
 		} catch (Exception e) {
 			System.err.println(e.getMessage());
 		}
 	}
 	
 	/**
 	 * Changes the event status from the client.
 	 * @param String event
 	 */
 	public void setEvent(String event) {
 		switch(event) {
 		case "started": case "stopped": case "completed":
 			this.event = event;
 			this.cGUI.updateClientEvent();
 		}
 	}
 	
 	/**
 	 * It terminates gracefully a connection to a peer.
 	 * @param String peer
 	 */
 	public void terminatePeer(String peer) {
 		for(Peer p : this.peerList) {
 			if(peer.equals(p)) {
 				p.dispose();
 				// I will complete these eventually.
 				this.peerList.remove((Peer)p);
 				this.cGUI.updatePeerInTable(p, ClientGUI.DELETEROW_UPDATE);
 				/*
 				for(int i = 0; i < this.peers.length; ++i) {
 					if(p.toString().equals(this.peers[i])) {
 						
 					}
 				}*/
 				// remove from lists
 				// set connected to false;
 			}
 		}
 	}
 
 	/**
 	 * Returns a clone of our PeerList
 	 * @return a clone of our PeerList
 	 */
 	public LinkedList<Peer> getPeerList() {
 		List<Peer> linkedList = new LinkedList<Peer>();
 		for(Peer p: this.peerList) {
 			linkedList.add(p);
 		}
 		return (LinkedList<Peer>)linkedList;
 	}
 	 
 	/**
 	 * Convert this.Bittorrent.completedPieces boolean array to a comma-separated string,
 	 * for saving file-download-state to prop.properties file 
 	 * @return converted string object
 	 */
 	public String saveCompletedPieces (){
 		 String btCompletedStr = "";
 		 for (int i = 0; i < this.completedPieces.length; i++) 
 			 btCompletedStr += (Boolean.toString(this.completedPieces[i]) + ",");
 		 return btCompletedStr;
 	 }
 	 
 	/**
 	 * Load string of comma-separated booleans into
 	 * this.Bittorrent.completedPieces boolean array, in order 
 	 * to restore the state of a torrent download after an interrupt.
 	 * @param btCompletedStr
 	 */
 	public void loadCompletedPieces (String btCompletedStr) {
 		String boolString = "true,false,true,false,true,";
 		//for (int i = 0; i < this.torrentInfo.file_length; i++)
 		int i = 0;
 		while (!boolString.isEmpty()) {
 			String bool = boolString.substring(0, boolString.indexOf(','));
 			boolString = boolString.substring(boolString.indexOf(',')+1, boolString.length());
 			this.completedPieces[i] = Boolean.valueOf(bool);
 			i++;
 		}
 		//System.out.println(Arrays.toString(this.completedPieces));
 	 }
 	
 	/**
 	 * Small routine to verify state of this.completedPieces boolean array
 	 */
 	public void completedString() {
 		System.out.println(Arrays.toString(this.completedPieces));
 	}
 
 	/**
 	 * Saves the current file heap to a tmp file for later reuse.
 	 */
 	public void saveHeap() {
 		if (!this.isFileCompleted()) {
 			File temp = null;
 			try {
 				temp = new File(TEMP_FILE);
 				Utilities.saveState(downloaded, uploaded, left, collection, temp);
 			} catch (IOException e) {
 				temp.delete();
 				System.err.println("unable to open cs352.tmp for writing.");
 			}
 		} else {
 			this.deleteTmpFile();
 		}
 	}
 	
 	/**
 	 * Wipes the tmp file if available.
 	 */
 	private void deleteTmpFile() {
 		try {
 			File temp = new File (TEMP_FILE);
 			if (temp.exists()) {
 				temp.delete();
 			}
 		} catch (Exception e) {
 			System.out.println("Temp file couldn't be deleted.");
 		}
 	}
 }
