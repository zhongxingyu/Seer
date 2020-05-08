 package bt.Model;
 
 import java.io.BufferedWriter;
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.URL;
 import java.net.UnknownHostException;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import bt.Utils.Bencoder2;
 import bt.Utils.TorrentInfo;
 import bt.Utils.Utilities;
 
 /**
  * Bittorrent is the main class for the client. It will initialize the
  * Tracker data fields in its constructor for then establishing a connection
  * with it.
  * 
  * @author Ike, Robert and Fernando
  *
  */
 
 public class Bittorrent {
 	
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
 	 * The constructor will initialize all the fields given by the .torrent file.
 	 */
 	private Bittorrent(String torrentFile, String saveFile)
 	{	
 		// open the file
 		File file = new File((this.rscFileFolder+torrentFile));
 		try {
 			// get file info
 			this.peerList = new ArrayList<Peer>();
 			this.clientID = Utilities.generateID();
 			this.torrentInfo = new TorrentInfo(Utilities.getBytesFromFile(file));
 			this.printTorrentInfoFields();
 			this.initClientState();
 			this.properties.load(new FileInputStream(this.rscFileFolder+"prop.properties"));
 			// request the tracker for peers
 			this.initPiecesColletion();
 			this.sendRequestToTracker();
 		} catch (Exception e) {
 			System.err.println(e.getMessage());
 		}
 	}
 	
 	
 	/**
 	 * Updates the value in left bytes for the file.
 	 * @param bytes bytes
 	 */
 	public void updateLeft(int bytes) {
 		this.left += bytes;
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
				verificationArray[i][j] = this.torrentInfo.piece_hashes[(20*i)+j].get();
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
 		this.pieces = (int)(Math.ceil(this.torrentInfo.file_length / this.torrentInfo.piece_length));
 		int pieceSize = this.torrentInfo.piece_length;
 		this.fileName = this.torrentInfo.file_name;
 		this.properties = new Properties();
 		this.properties.load(new FileInputStream(this.rscFileFolder+"prop.properties"));
 		this.event = this.properties.getProperty("event");
 		this.uploaded = Integer.parseInt(this.properties.getProperty("uploaded"));
 		this.downloaded = Integer.parseInt(this.properties.getProperty("downloaded"));
 		this.left = Integer.parseInt(this.properties.getProperty("left"));
 		this.collection = new byte[pieces][pieceSize];
 		this.verificationArray = new byte[pieces][20];
 		this.completedPieces = new boolean[this.collection.length];
		
		// loadVerificationArray();
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
 	 * Returns the singleton instance of the client.
 	 * @return Bittorrent instance
 	 * @throws Exception
 	 */
 	public static Bittorrent getInstance() throws Exception {
 		if(Bittorrent.instance == null) throw new Exception("Client was never initialized");
 		return Bittorrent.instance;
 	}
 	
 	/**
 	 * It will issue and HTTP GET request to obtain bencoded information 
 	 * about peers and seeds in the server.
 	 * @throws IOException 
 	 */
 	public String sendRequestToTracker() {
 		// initializes the server and returns its port
 		this.server = Server.getInstance();
 		int port = this.server.getPort();
 		
 		String response = null;
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
 	 * Connect to peer - this is just trial code to instantiate and test the peer.
 	 */
 	private void initPeers() throws Exception {
 		ArrayList<byte[]> torrentPeerList = new ArrayList<byte[]>();
 		int peerIndex = 0;
 		for (byte[] peerID : torrentPeerList) {
 			try {
 				peerList.add(new Peer(	Utilities.getIPFromString(this.peers[0]), // peer[0] selected
 										Utilities.getPortFromString(this.peers[0]), // peer[0] selected
 										this.info_hash.getBytes(),
 										this.clientID.getBytes(),
 										collection, // to be completed
 										this.verificationArray, // to be completed
 										completedPieces));
 			} catch (UnknownHostException e) {
 				System.out.println("Host Unknown: " + peers[0]);
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				System.err.println("Failed to contact: " + peers[0]);
 			}
 			Thread peerThread = new Thread(peerList.get(peerIndex));
 			peerThread.start();
 			peerIndex++;
 		}
 	}
 	
 	/**
 	* Returns the first available port given the range or -1 if non if available.
 	* @param from left bound
 	* @param to right bound
 	* @return int port
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
 	 * Initiates the bytes collections. 
 	 */
 	private void initPiecesColletion() {
 		int segments = (int)(Math.ceil(this.torrentInfo.file_length / this.torrentInfo.piece_length));
 		int segmentLength = this.torrentInfo.file_length / segments;
 		this.collection = new byte[segments][segmentLength];
 	}
 	
 	/**
 	 * Print a list of available peers as per last refresh from tracker.
 	 */
 	public void printPeerList() {
 		int number = 1;
 		System.out.println("-----------------------------------");
 		for(String s: this.peers) {
 			System.out.println(number+". "+s);
 			++number;
 		}
 		System.out.println("-----------------------------------");
 	}
 	
 	/**
 	 * Connect to the selected peer. Rustic implementation, but its just a starter.
 	 * @param peer peer number
 	 */
 	public void connectToPeer(int peer) throws Exception {
 		peer = peer - 1;
 		// create peer, attempt connection, feed arguments.
 		if(peer < 0 || peer >= this.peers.length) {
 			throw new IllegalArgumentException("Invalid peer number, out of range.");
 		} else if(this.connections[peer]) {
 			throw new Exception("Connection already stablished with peer: "+this.peers[peer]);
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
 			this.peerList.add(p);
 			// mark the connection as boolean connected in this.connectios
 			this.connections[peer] = true;
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
 		for(int i = 0; i < this.peers.length; ++i) {
 			if(peer.equals(this.peers[i])) {
 				connected = true;
 				this.connectToPeer(i+1);
 			}
 		}
 		if(!connected)
 			throw new IllegalArgumentException("Peer does not exist.");
 	}
 	
 	/**
 	 * It will terminate all the connection with the peers.
 	 */
 	public void disposePeers() {
 		for(Peer p:this.peerList) {
 			p.dispose();
 		}
 	}
 
 	/**
 	 * This method is a simple algorithm for sending requests for a file.
 	 */
 	public void simpleDownloadAlgorithm() {
 		// This is a temporary algorithm for Project 0.  It will be replaced with a more robust one
 		// when we are doing more than downloading a file from a known see.
 		Peer peer = peerList.get(0);
 		for (int i = 0; i < collection.length; ++i) {
 			boolean sent = false;
 			// Attempt to request the piece until it succeeds.
 			while (!sent) {
 				try {
 					peer.requestIndex(i, 0, 16384);
 					peer.requestIndex(i, 16384, 16384);
 					System.out.println("-- Piece: "+i+" requested");
 					sent = true;
 				} catch (IOException e) {
 					System.err.println(e.getMessage());
 				}
 			}
 		}
 	}
 	/**
 	 * This method is our algorithm for rending requests for the file we are downloading.
 	 */
 	public void downloadAlgorithm() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void saveFile () throws IOException {
 		FileOutputStream fileOut = new FileOutputStream(fileName);
 		byte[] fileArray = new byte[torrentInfo.file_length];
 		for (int outer = 0; outer < collection.length; ++outer) {
 			for (int inner = 0; inner < collection[outer].length; ++inner) {
 				fileArray[(outer * collection[0].length) + inner] = collection[outer][inner];
 			}
 		}
 		fileOut.write(fileArray);
 		fileOut.close();
 	}
 }
