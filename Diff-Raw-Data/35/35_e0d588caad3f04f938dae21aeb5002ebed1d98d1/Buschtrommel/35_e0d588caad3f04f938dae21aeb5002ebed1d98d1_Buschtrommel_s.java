 package de.tr0llhoehle.buschtrommel;
 
 import java.net.InetAddress;
 import java.util.Hashtable;
 
 import de.tr0llhoehle.buschtrommel.models.File;
 import de.tr0llhoehle.buschtrommel.models.Host;
 import de.tr0llhoehle.buschtrommel.network.FileTransferAdapter;
 import de.tr0llhoehle.buschtrommel.network.UDPAdapter;
 
 public class Buschtrommel {
 
 	private IGUICallbacks gui;
 	private FileTransferAdapter fileTransferAdapter;
 	private UDPAdapter udpAdapter;
 
 	public Buschtrommel(IGUICallbacks gui) {
		if(!HashFuncWrapper.check())
 			//cancel bootstrap: Hashfunction is not available!
 	}
 	
 	public void start() {
 		
 	}
 	
 	public void stop() {
 		
 	}
 	
 	public ITransferProgress DownloadFile(String hash, Host host) {
 		return null;
 	}
 	
 	public void AddFileToShare(String path, String dspName, String meta) {
 		
 	}
 	
 	public void RemoveFileFromShare(File file) {
 		
 	}
 	
 	public Hashtable<String, ITransferProgress> getIncomingTransfers() {
 		return null;
 	}
 	
 	public Hashtable<InetAddress, ITransferProgress> getOutgoingTransfers() {
 		return null;
 	}
 	
 	public Hashtable<String, File> getShares() {
 		return null;
 	}
 	
 	public Hashtable<InetAddress, Host> getHosts() {
 		return null;
 	}
 	
 	public void CancelFileTransfer(ITransferProgress transferProgress) {
 		
 	}
 }
