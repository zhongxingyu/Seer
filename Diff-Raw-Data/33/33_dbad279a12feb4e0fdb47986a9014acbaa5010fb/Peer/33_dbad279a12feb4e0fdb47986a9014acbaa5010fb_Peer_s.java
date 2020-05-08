 package bt.Model;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.nio.ByteBuffer;
 import java.util.ArrayDeque;
 import java.util.Queue;
 
 
 /**
  * Creates a connection with a peer to download a file.
  * @author Ike, Robert and Fernando
  *
  */
 
 public class Peer implements Runnable {
 
 	private boolean choked = true;
 	private Bittorrent bittorrent;
 	private boolean interested = false;
 	private Socket dataSocket = null;
 	private InputStream in = null;
 	private OutputStream out = null;
 	
 	/**
 	 * This field, hash, holds the 20 byte hash of the .Torrent file being used by the client which
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
 	private Queue <Integer> interestedQueue;
 	
 	/**
 	 * This is a constructor for a Peer taking the address and port as parameters.  The address and port of a
 	 * peer object are immutable during running, and therefore can only be set with this constructor.
 	 * @param address Address of the peer this object represents.
 	 * @param port Port on which to contact the peer which this object represents.
 	 * @param hashIn This field will hold 20 byte hash of the .Torrent file being used by the client which
 	 * instantiated this object.
 	 * @param peerID This field will hold the 20 byte peer id of the client which instantiated this object.
 	 * @throws UnknownHostException If the address cannot be resolved to a host, this exception will be thrown.
 	 * @throws IOException If a connection cannot be opened to this host, this exception will be thrown.
 	 */
 	public Peer(final String address, final int port, final byte[] hashIn, final byte[] peerID)
 			throws UnknownHostException, IOException, Exception {
 		bittorrent = Bittorrent.getInstance();
 		interestedQueue = new ArrayDeque <Integer> ();
 		dataSocket = new Socket(address, port);
 		in = dataSocket.getInputStream();
 		out = dataSocket.getOutputStream();
 		hash = hashIn;
 		clientID = peerID;
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
 	 */
 	void showFinished (int piece) {
 		//stub
 	}
 	
 	/**
 	 * This method sends a request message to the peer this object represents.
 	 * @param index piece of the file to be requested.
 	 * @param begin byte offset
 	 * @param length byte offset
 	 * @throws IOException will be thrown if the system is unable to dispatch the message.
 	 */
 	void requestIndex(int index, int begin, int length) throws IOException {
 		Byte b = (byte) 6;
 		byte[] message = new byte[17];
 		ByteBuffer messageBuffer = ByteBuffer.allocate(13);
 		messageBuffer.put(b).putInt(index).putInt(begin).putInt(length);
 		messageBuffer.get(message);
 		out.write(message);
 		out.flush();
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
 	 * This method can be used to send a bitfield to the peer this object represents.  This should only
 	 * be done as the first message to this peer.  A bitfield is a byte[] with each index that the downloader,
 	 * this client, has sent set to one and the rest set to zero. Downloaders which don't have anything yet
 	 * may skip the 'bitfield' message. The first byte of the bitfield corresponds to indices 0 - 7 from high
 	 * bit to low bit, respectively. The next one 8-15, etc. Spare bits at the end are set to zero.
 	 * @param bitfield a byte[] bitfield to form the message
 	 * @throws IOException if the system fails to send the TCP packet properly, this exception will be thrown.
 	 */
 	void sendBitfield(byte[] bitfield) throws IOException {
 		out.write(bitfield);
 		out.flush();
 	}
 	
 	// setInterested and setChoke are both placeholders for the single peer client.  When there are more than one
 	// peer, choke and interested will be set by those peers by TCP messages.
 	/**
 	 * Sets the interested bit flag on this peer's connection.
 	 * @param value Value for interested flag.
 	 */
 	void setInterested (boolean value) {
 		interested = value;
 	}
 	
 	/**
 	 * Sets the choked bit flag on this peer's connection.
 	 * @param value Value for the choked flag.
 	 */
 	void setChoke (boolean value) {
 		choked = value;
 	}
 	
 	public void run() {
 		handShake();
 	}
 	
 	private void handShake() {
 		byte[] b1 = new byte[1];
 		b1[0] = (byte) 19;
 		byte[] b2 = new byte[8];
 		for (int i = 0; i < 8; i++)
 			b2[i] = (byte) 0;		
		String handShakeStr = "" + b1[0] + "BitTorrent protocol" + b2
			+ bt.Utils.Utilities.encodeInfoHashToURL(bittorrent.getInfoHash()) 
			+ bittorrent.getPeerId();
 	}
 	
 }
