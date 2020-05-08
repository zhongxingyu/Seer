 package de.tr0llhoehle.buschtrommel.network;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.Hashtable;
 import java.util.List;
 
 import de.tr0llhoehle.buschtrommel.LoggerWrapper;
 import de.tr0llhoehle.buschtrommel.LocalShareCache;
 import de.tr0llhoehle.buschtrommel.models.GetFileMessage;
 import de.tr0llhoehle.buschtrommel.models.GetFilelistMessage;
 import de.tr0llhoehle.buschtrommel.models.Host;
 import de.tr0llhoehle.buschtrommel.models.Message;
 
 public class FileTransferAdapter extends MessageMonitor {
 	private LocalShareCache myShares;
 	private int port = -1;
 	private ServerSocket listeningSocket;
 	private Thread receiveThread;
 	private boolean keepAlive;
 	private Hashtable<java.net.InetAddress, ITransferProgress> outgoingTransfers;
 	private Hashtable<String, ITransferProgress> incomingTransfers;
 
 	/**
 	 * Creates an instance of FileTransferAdapter and opens a listening TCP Port
 	 * on the given port.
 	 * 
 	 * @param s
 	 *            the manager for all own shares to serve GET FILE and GET FILE
 	 *            MESSAGE
 	 * @param port
 	 *            the tcp port to use.
 	 * @throws IOException
 	 */
 	public FileTransferAdapter(LocalShareCache s, int port) throws IOException {
 		this.port = port;
 		myShares = s;
 		incomingTransfers = new Hashtable<>();
 		outgoingTransfers = new Hashtable<>();
 		startListening();
 	}
 
 	public FileTransferAdapter(LocalShareCache s) throws IOException {
 		myShares = s;
 		startListening();
 	}
 
 	private void startListening() throws IOException {
 		keepAlive = true;
 		if (port == -1) {
 			listeningSocket = new ServerSocket();
 			port = listeningSocket.getLocalPort();
 		} else {
 			listeningSocket = new ServerSocket(port);
 		}
 		listeningSocket.setReuseAddress(true);
 		receiveThread = new Thread(new Runnable() {
 
 			@Override
 			public void run() {
 				LoggerWrapper.logInfo("Start Listening thread");
 				handleIncomingConnections();
 				LoggerWrapper.logInfo("Stop Listening thread");
 			}
 		});
 		receiveThread.start();
 	}
 
 	/**
 	 * Called by receiveThread
 	 * 
 	 * @throws IOException
 	 */
 	private void handleIncomingConnections() {
 		while (keepAlive) {
 			Socket s;
 			try {
 				s = listeningSocket.accept();
 				byte[] raw_message = new byte[512];
 				final Message m = MessageDeserializer.Deserialize(new String(raw_message).trim());
 				if(m != null)
 					sendMessageToObservers(m);
 				m.setSource(s.getInetAddress());
 				final OutputStream out = s.getOutputStream();
 				ITransferProgress p = null;
 				if (m instanceof GetFileMessage) {
 					OutgoingTransfer transfer = new OutgoingTransfer((GetFileMessage) m, out, myShares, new InetSocketAddress(s.getInetAddress(), s.getPort()));
 					transfer.start();
 					p = transfer;
 				} else if (m instanceof GetFilelistMessage) {
 					OutgoingTransfer transfer = new OutgoingTransfer((GetFilelistMessage) m, out, myShares, new InetSocketAddress(s.getInetAddress(), s.getPort()));
 					transfer.start();
 					p = transfer;
 				}
 				
 				if(p != null)
 					outgoingTransfers.put(m.getSource(), p);
 				
 			} catch (IOException e) {
 				LoggerWrapper.logError(e.getMessage());
 			}
 		}
 	}
 	
 	public ITransferProgress DownloadFile(String hash, Host host, long length, java.io.File target) {
 		return new IncomingDownload(new GetFileMessage(hash, 0, length), host, target);
 	}
 	
 	/**
 	 * Starts a multisource download
 	 * @param hash hash of requested file
 	 * @param hosts hosts that are offering the file
 	 * @param length expected length of download
 	 * @return one ITransferProgress that may contain multiple children.
 	 */
 	public ITransferProgress DownloadFile(String hash, List<Host> hosts, long length, java.io.File target) {
 		assert hosts.size() > 0;
 		return DownloadFile(hash, hosts.get(0), length, target); //TODO implement multisource
 	}
 
 	/**
 	 * Returns all outgoing Transfers that have been made.
 	 * This is a clone of the internal data structure.
 	 * @return all outgoing transfers
 	 */
 	@SuppressWarnings("unchecked")
 	public Hashtable<java.net.InetAddress, ITransferProgress> getOutgoingTransfers() {
 		return (Hashtable<InetAddress, ITransferProgress>) outgoingTransfers.clone();
 	}
 	
 	/**
 	 * Returns all incoming Transfers that have been made.
 	 * This is a copy of the internal data structure
 	 * @return all incoming transfers
 	 */
 	@SuppressWarnings("unchecked")
 	public Hashtable<String, ITransferProgress> getIncomingTransfers() {
 		return (Hashtable<String, ITransferProgress>) incomingTransfers.clone();
 	}
 
 	/**
 	 * Returns the TCP port that this adapter listens on
 	 * 
 	 * @return the port
 	 */
 	public int getPort() {
 		return port;
 	}
 
 	/**
 	 * Stops the listening thread and all running transfers.
 	 */
 	public void close() {
 		keepAlive = false;
 		receiveThread.interrupt();
 		for(InetAddress k : outgoingTransfers.keySet())
 			outgoingTransfers.get(k).cancel();
 		for(String k : incomingTransfers.keySet())
 			incomingTransfers.get(k).cancel();
 	}
 
 	public ITransferProgress downloadFilelist(Host host) {
		IncomingFilelistTransfer result = new IncomingFilelistTransfer(host);
		
		for(IMessageObserver observer : observers)
			result.registerObserver(observer);
 		result.start();
 		return result;
 	}
 }
