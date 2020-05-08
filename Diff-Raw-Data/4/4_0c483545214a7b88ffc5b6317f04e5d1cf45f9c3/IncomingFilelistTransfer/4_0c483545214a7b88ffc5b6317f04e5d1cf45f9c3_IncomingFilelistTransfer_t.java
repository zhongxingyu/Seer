 package de.tr0llhoehle.buschtrommel.network;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.logging.Handler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import de.tr0llhoehle.buschtrommel.models.GetFilelistMessage;
 import de.tr0llhoehle.buschtrommel.models.Host;
 import de.tr0llhoehle.buschtrommel.models.Message;
 
 /**
  * This object starts a filelist tranfer from anoter host to this host.
  * After a GET FILELIST message has been sent, the answer stream will be cut into single file announcement messages. These will be sent via the MessageMonitor interfaces.
  * Make sure to call registerObserver befor calling start();
  * 
  * @author Tobias Sturm
  *
  */
 public class IncomingFilelistTransfer extends Transfer {
 	
 	public IncomingFilelistTransfer(Host host) {
 		super(new InetSocketAddress(host.getAddress(), host.getPort()));
 		assert host != null;
 		logger = java.util.logging.Logger.getLogger("incoming filelist from " + host.toString());
 		offset = 0;
 		hash = "";
 	}
 	
 	
 	@Override
 	public void cancel() {
 		keepTransferAlive = false;
 	}
 
 	@Override
 	public void reset() {
 		throw new UnsupportedOperationException("incoming filelist-transfers can't be reset");
 		
 	}
 
 	@Override
 	public void resumeTransfer() {
 		throw new UnsupportedOperationException("incoming filelist-transfers can't be resumed");
 		
 	}
 
 	@Override
 	public void start() {
 		(new Thread(new Runnable() {
 			
 			@Override
 			public void run() {
 				Socket s;
 				keepTransferAlive = true;
 				try {
 					transferState = TransferStatus.Connecting;
 					s = new Socket(partner.getAddress(), partner.getPort());
 					transferState = TransferStatus.Transfering;
 					s.getOutputStream().write(new GetFilelistMessage().Serialize().getBytes(Message.ENCODING));
 					processResponseFilestream(s.getInputStream());
 					s.close();
 				} catch (IOException e) {
					logger.log(Level.SEVERE, "Can't get filelist from " + partner.toString() + ": " + e.getMessage());
 				}
 			}
 		})).start();
 	}
 	
 	private void processResponseFilestream(InputStream in) throws IOException {
 		int next = 0;
 		int received = 0;
 		char[] buffer = new char[512];
 		while((next = in.read()) != -1 && keepTransferAlive) {
 			if(next != Message.MESSAGE_SPERATOR) {
 				buffer[received++] = (char) next;
 				continue;
 			} else {
 				String raw = String.valueOf(buffer, 0, received) + Message.MESSAGE_SPERATOR;
 				Message result = MessageDeserializer.Deserialize(raw);
 				if(result != null) {
					result.setSource(partner);
 					sendMessageToObservers(result);
 				} else {
 					logger.log(Level.SEVERE, "could not deserialize message: " + raw);
 				}
 				received = 0;
 			}
 		}
 		if(!keepTransferAlive)
 			transferState = TransferStatus.Canceled;
 		else
 			transferState = TransferStatus.Finished;
 		keepTransferAlive = false;
 	}
 
 
 
 	@Override
 	public String getTargetFile() {
 		return "filelist";
 	}
 }
