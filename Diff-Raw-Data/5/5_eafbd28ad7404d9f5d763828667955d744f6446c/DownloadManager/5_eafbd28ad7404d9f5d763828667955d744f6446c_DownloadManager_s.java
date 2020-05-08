 
 import java.awt.event.ActionEvent;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.BitSet;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.PriorityQueue;
 import java.io.ByteArrayOutputStream;
 import java.util.Queue;
 import java.util.Vector;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * STEPS: 1) Perform handshake 2) Send interested message 3) Wait for unchoke 4)
  * Unchoke, start sequentially requesting blocks of data 5) When a piece is
  * complete, verify it sends a "have message" and writes to "destination" File
  *
  */
 public class DownloadManager extends Thread {
 
     private Tracker tracker;
     private TorrentInfo torrent;
 	private ArrayList<Peer> peers = new ArrayList<Peer>();
     private RUBTClient client;
     private boolean stillRunning;
 
     public DownloadManager(RUBTClient r, Tracker t) {
         client = r;
         torrent = r.getTorrentInfo();
         tracker = t;
         stillRunning = true;
     }
 
     /**
      * METHOD: Running the program
      */
     public void run() {
 		String delims = "[:]";
 
         for(String peerFullIP : tracker.getpeerIPList())
 		{
 			String[] ipParts = peerFullIP.split(delims);
 			try
 			{
 				String newIP = ipParts[0];
 				System.out.println("PeerIP: " + newIP);
 				
 				int newPort = Integer.parseInt(ipParts[1]);
 				System.out.println("PeerPort: " + newPort);
 
 				peers.add(new Peer(newIP, newPort, null));
 				System.out.println("Peer has been added to List. ");
 			}
 			catch(Exception e)
 			{
 				System.err.println("ERROR: Could not create peer. ");
 			}
 		}
     }
 
     /* +++++++++++++++++++++++++++++++ GET METHODS +++++++++++++++++++++++++++++++++++ */
     ArrayList<Peer> getPeerList() {
         return peers;
     }
 
 	/**  METHOD: Take a piece of the file and save it into the clients pieces array at the given index.
 	  *
 	  *  @param piece A ByteArrayOutputStream containing the bytes of the piece.
 	  *  @param index The index of the piece.
 	  */
 	public static void savePiece(ByteArrayOutputStream piece, int index, Peer hasLock)
 	{
 		synchronized(hasLock)
 		{
 			RUBTClient.piecesDL[index] = piece;
 			RUBTClient.intBitField[index] = 1;
			//RUBTClient.Bitfield.set(index);
 		}
 		if (RUBTClient.Bitfield.cardinality() == RUBTClient.numPieces)
 		{
 			// this.closePeers();
 			RUBTClient.writeFile();
 		}
 	}
 
 	/** METHOD: Determine whether or not the piece of the file specified by index
 	  *			has been downloaded or not
 	  *
 	  *  @param index The index number of the piece.
 	  *  @return True if the piece has been downloaded false if not.
 	  */
 	public static synchronized boolean hasPiece(int index, Peer hasLock)
 	{
 		synchronized(hasLock)
		{//RUBTClient.Bitfield.get(index) &&
 			boolean returnThis =  RUBTClient.intBitField[index] == 0;
 			if (returnThis)
 				RUBTClient.intBitField[index] = 1;
 			return returnThis;
 		}
 	}
 }
