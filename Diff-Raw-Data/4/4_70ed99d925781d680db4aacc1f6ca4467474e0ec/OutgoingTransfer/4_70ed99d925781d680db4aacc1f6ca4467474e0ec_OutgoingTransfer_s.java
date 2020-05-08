 package de.tr0llhoehle.buschtrommel.network;
 
 import java.io.ByteArrayInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.InetSocketAddress;
 import java.net.SocketException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 
 import de.tr0llhoehle.buschtrommel.LocalShareCache;
 import de.tr0llhoehle.buschtrommel.models.GetFileMessage;
 import de.tr0llhoehle.buschtrommel.models.FileRequestResponseMessage;
 import de.tr0llhoehle.buschtrommel.models.GetFilelistMessage;
 import de.tr0llhoehle.buschtrommel.models.Message;
 import de.tr0llhoehle.buschtrommel.models.FileRequestResponseMessage.ResponseCode;
 import de.tr0llhoehle.buschtrommel.network.ITransferProgress.TransferStatus;
 
 /**
  * Implements any outgoing transfer from this host to other hosts.
  * 
  * The transfer can be a file or a filelist. The hash of a filelist is
  * "filelist". Depending on the requested data this class will send a
  * FileRequestResponse-header or not.
  * 
  * @author Tobias Sturm
  * 
  */
 public class OutgoingTransfer extends Transfer {
 	private OutputStream networkOutputStream; // network stream to the requester
 	private java.io.InputStream ressourceInputStream; // stream from filelist /
 														// file
 	private LocalShareCache myShares; // all my shares
 
 	private long numAvailableData; // max possible number of bytes to send
 	private boolean sendHeaderInRsp;
 	private String localeFilename;
 	private Message requestMessage; // the message that contains the request
 	private int bufferSize;
 	Thread transferThread;
 
 	/**
 	 * Creates an uploading file transfer of a file
 	 * 
 	 * @param m
 	 *            the request message. Has to be either a GetFile or GetFileList
 	 *            message
 	 * @param out
 	 *            the network stream to write to
 	 * @param myShares
 	 *            all known, local shares
 	 * @param partner
 	 *            the communication partner, that receives the file
 	 */
 	public OutgoingTransfer(Message m, OutputStream out, LocalShareCache myShares, InetSocketAddress partner) {
 		super(partner);
 		assert m instanceof GetFilelistMessage || m instanceof GetFileMessage;
 		assert partner != null;
 		requestMessage = m;
 		this.networkOutputStream = out;
 		this.myShares = myShares;
 		this.transferType = TransferType.Outgoing;
 		bufferSize = -1;
 
 		logger = java.util.logging.Logger.getLogger("outgoing " + partner.toString());
 		keepTransferAlive = true;
 		transferState = TransferStatus.Initialized;
 		totalTransferedVolume = 0;
 
 		if (m instanceof GetFileMessage) {
 			sendHeaderInRsp = true;
 			hash = ((GetFileMessage) m).getHash();
 		} else if (m instanceof GetFilelistMessage) {
 			sendHeaderInRsp = false;
 			hash = "filelist";
 			localeFilename = "filelist";
 		}
 	}
 
 	/**
 	 * Creates an uploading file transfer of a file
 	 * 
 	 * @param m
 	 *            the request message. Has to be either a GetFile or GetFileList
 	 *            message
 	 * @param out
 	 *            the network stream to write to
 	 * @param myShares
 	 *            all known, local shares
 	 * @param partner
 	 *            the communication partner, that receives the file
 	 * @param bufferSize
 	 *            the size of the sending buffer ( > 0). Default is 1
 	 */
 	public OutgoingTransfer(Message m, OutputStream out, LocalShareCache myShares, InetSocketAddress partner,
 			int bufferSize) {
 		this(m, out, myShares, partner);
 		assert bufferSize > 0;
 		this.bufferSize = bufferSize;
 	}
 
 	/**
 	 * Creates a stream that holds the data to send (filecontent or filelist) or
 	 * null, if the requested ressource is not available
 	 * 
 	 * @param m
 	 *            the request message
 	 * @throws UnsupportedEncodingException
 	 */
 	private void openInputStream(Message m) throws UnsupportedEncodingException {
 		if (m instanceof GetFilelistMessage) {
 			byte[] fileList = myShares.getAllShares().getBytes(Message.ENCODING);
 			numAvailableData = fileList.length;
 			ressourceInputStream = new ByteArrayInputStream(fileList);
 		} else if (m instanceof GetFileMessage) {
 			// do I know the file?
 			if (!myShares.has(((GetFileMessage) m).getHash())) {
 				logger.log(Level.INFO, "Requested file is not in share cache");
 				ressourceInputStream = null; // file not available
 				return;
 			}
 
 			// does the file exist?
 			java.io.File file = new java.io.File(myShares.get(((GetFileMessage) m).getHash()).getPath());
 			localeFilename = file.getName();
 			if (!file.exists()) {
 				logger.log(Level.INFO, "Requested file is not found");
 				ressourceInputStream = null;
 				return;
 			}
 			numAvailableData = file.length();
 
 			// open file for read
 			logger.log(Level.INFO, "Open file for outgoing file transfer");
 			try {
 				ressourceInputStream = new java.io.FileInputStream(file);
 			} catch (FileNotFoundException e) {
 				logger.log(Level.INFO, "Requested file could not be opend");
 				ressourceInputStream = null;
 			}
 			return;
 		}
 	}
 
 	/**
 	 * Handles the requested ranges of data
 	 * 
 	 * @param inStream
 	 * @param request
 	 */
 	private void handleRequestedRanges(Message request) {
 		if (request instanceof GetFilelistMessage) {
 			offset = 0;
 			expectedTransferVolume = numAvailableData;
 		} else if (request instanceof GetFileMessage) {
 			offset = ((GetFileMessage) request).getOffset();
 			expectedTransferVolume = ((GetFileMessage) request).getLength();
 		}
 	}
 
 	private void doTransfer() throws IOException {
 		if (ressourceInputStream == null) {
 			if (sendHeaderInRsp) {
 				String header = new FileRequestResponseMessage(ResponseCode.NEVER_TRY_AGAIN, 0).Serialize();
 				logger.info("send header '" + header + "'");
 				networkOutputStream.write(header.getBytes());
 			}
 
 			networkOutputStream.close();
 			return;
 		} else {
 			if (offset > numAvailableData) { // offset not in file
 				logger.log(Level.INFO, "Requested offset is not valid: requested " + offset + ", length of file: "
 						+ numAvailableData);
 
 				if (sendHeaderInRsp)
 					networkOutputStream
 							.write(new FileRequestResponseMessage(ResponseCode.OK, 0).Serialize().getBytes());
 
 				networkOutputStream.close();
 				transferState = TransferStatus.Finished;
 				return;
 			}
 
 			if (offset + expectedTransferVolume > numAvailableData) { // requested
 																		// Length
 																		// too
 																		// large?
 																		// Shorten
 																		// it!
 				logger.log(Level.INFO, "Requested length of " + expectedTransferVolume
 						+ " was too large, shortened  it to " + expectedTransferVolume);
 				expectedTransferVolume = numAvailableData - offset;
 			}
 
 			// send the header
 			transferState = TransferStatus.Transfering;
 			if (sendHeaderInRsp)
 				networkOutputStream.write((new FileRequestResponseMessage(ResponseCode.OK, expectedTransferVolume)
 						.Serialize()).getBytes(Message.ENCODING));
 
 			// adjust buffer size
 			if (bufferSize == -1) {
 				bufferSize = FALLBACK_BUFFER_SIZE;
 				logger.log(Level.INFO, "Using fallback buffersize " + bufferSize);
 			}
 
 			// send the file
 			logger.info("sending data");
 			ressourceInputStream.skip(offset);
 			int bytesRead = 0;
 			int bytesToRead = bufferSize;
 			byte[] buffer = new byte[bufferSize];
 			while (bytesToRead > 0 && keepTransferAlive && totalTransferedVolume < expectedTransferVolume
 					&& (bytesRead = ressourceInputStream.read(buffer, 0, bytesToRead)) != -1) {
 				networkOutputStream.write(buffer, 0, bytesRead);
 				totalTransferedVolume += bytesRead;
 				if (totalTransferedVolume + bytesToRead > expectedTransferVolume) {
 					bytesToRead = (int) (expectedTransferVolume - totalTransferedVolume); // it's
 																							// ok,
 																							// because
 																							// buffer
 																							// is
 																							// an
 																							// int,
 																							// too
 				}
 			}
 			networkOutputStream.flush();
 			networkOutputStream.close();
 			ressourceInputStream.close();
 			logger.info("finished sending data");
 			
 			if (totalTransferedVolume == expectedTransferVolume)
 				transferState = TransferStatus.Finished;
 			else {
 				if (!keepTransferAlive)
 					transferState = TransferStatus.Canceled;
 				else
 					transferState = TransferStatus.LostConnection;
 			}
 		}
 	}
 
 	@Override
 	public void cancel() {
 		keepTransferAlive = false;
 		transferState = TransferStatus.Canceled;
 		try {
 			networkOutputStream.close();
 		} catch (IOException e) {
 			// ignore
 		}
 	}
 
 	@Override
 	public void reset() {
 		throw new UnsupportedOperationException("Outgoing transfers can't be reset");
 	}
 
 	@Override
 	public void resumeTransfer() {
 		throw new UnsupportedOperationException("Outgoing transfers can't be resumed");
 	}
 
 	@Override
 	public String getTargetFile() {
 		return localeFilename;
 	}
 
 	@Override
 	public void start() {
 		logger.info("start outgoing transfer");
 		assert transferThread == null;
 		transferThread = new Thread(new Runnable() {
 
 			@Override
 			public void run() {
 				try {
 					openInputStream(requestMessage);
 				} catch (UnsupportedEncodingException e1) {
 					logger.log(Level.SEVERE, "Unsupported encoding");
 					ressourceInputStream = null;
 				}
 				handleRequestedRanges(requestMessage);
 				try {
 					doTransfer();
 					keepTransferAlive = false;
 				} catch (IOException e) { // catch *all* errors and do nothing,
 											// because we don't give a shit if
 											// someone doesn't get his candy
 					logger.log(Level.SEVERE, "Could not handle outgoing file transfer: " + e.getMessage());
 					cancel();
 				}
 			}
 		});
 		transferThread.start();
 	}
 
 	@Override
 	public void cleanup() {
 		if (transferState == TransferStatus.Cleaned) {
 			logger.log(Level.WARNING, "transfer is already cleaned!");
 			return;
 		}
 		super.cleanup();
 
 		// remove references
 		myShares = null;
 		requestMessage = null;
 
 		try {
 			if (networkOutputStream != null)
 				networkOutputStream.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		try {
 			if (ressourceInputStream != null)
 				ressourceInputStream.close();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		if (transferThread != null)
 			transferThread.interrupt();
 	}
 }
