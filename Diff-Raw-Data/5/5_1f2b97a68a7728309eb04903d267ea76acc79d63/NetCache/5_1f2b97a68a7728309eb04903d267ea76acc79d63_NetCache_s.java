 package de.tr0llhoehle.buschtrommel.network;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.util.Hashtable;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import de.tr0llhoehle.buschtrommel.Config;
 import de.tr0llhoehle.buschtrommel.IGUICallbacks;
 import de.tr0llhoehle.buschtrommel.LoggerWrapper;
 import de.tr0llhoehle.buschtrommel.models.ByeMessage;
 import de.tr0llhoehle.buschtrommel.models.PeerDiscoveryMessage.DiscoveryMessageType;
 import de.tr0llhoehle.buschtrommel.models.RemoteShare;
 import de.tr0llhoehle.buschtrommel.models.FileAnnouncementMessage;
 import de.tr0llhoehle.buschtrommel.models.Host;
 import de.tr0llhoehle.buschtrommel.models.Message;
 import de.tr0llhoehle.buschtrommel.models.PeerDiscoveryMessage;
 import de.tr0llhoehle.buschtrommel.models.ShareAvailability;
 
 /**
  * This class manages the state of the network. All known hosts and shares of other hosts are stored here. As soon as a new host is discovered, this object will start a filelist transfer and digest the results via the loose observer-interface.
  * @author Moritz Winter
  * 
  */
 public class NetCache implements IMessageObserver {
 
 	private static int TTL_REFRESH_RATE = 5;
 
 	protected Hashtable<InetAddress, Host> knownHosts;
 	protected Hashtable<String, RemoteShare> knownShares;
 
 	protected IGUICallbacks guiCallbacks;
 	protected UDPAdapter udpAdapter;
 	protected FileTransferAdapter fileTransferAdapter;
 	protected Timer ttlChecker;
 
 	public NetCache(UDPAdapter udpAdapter, FileTransferAdapter fileAdapter, IGUICallbacks guiCallbacks) {
 		this.udpAdapter = udpAdapter;
 		this.guiCallbacks = guiCallbacks;
 		
 		this.knownHosts = new Hashtable<InetAddress, Host>();
 		this.knownShares = new Hashtable<String, RemoteShare>();
 		
 		this.fileTransferAdapter = fileAdapter;
 		
 		this.ttlChecker = new Timer();
 		TimerTask task = new TimerTask() {
 			public void run() {
 				checkTTL();
 			}
 		};
 		ttlChecker.scheduleAtFixedRate(task, TTL_REFRESH_RATE * 1000, TTL_REFRESH_RATE * 1000);
 	}
 
 	/**
 	 * Returns the remoteShare with the given hash or null if the share is
 	 * unknown
 	 * 
 	 * @param hash
 	 *            of the share
 	 * @return null or the share
 	 */
 	public RemoteShare getShare(String hash) {
 		return knownShares.get(hash);
 	}
 
 	@Override
 	public void receiveMessage(Message message) {
 		if (message instanceof FileAnnouncementMessage) {
 			this.fileAnnouncmentHandler((FileAnnouncementMessage) message);
 		} else if (message instanceof PeerDiscoveryMessage) {
 			this.peerDiscoveryHandler((PeerDiscoveryMessage) message);
 		} else if (message instanceof ByeMessage) {
 			this.byeHandler((ByeMessage) message);
 		}
 
 	}
 
 	private void fileAnnouncmentHandler(FileAnnouncementMessage message) {
 		Host host = new Host(message.getSource().getAddress(), message.getSource().getHostName(), 0);
 		int ttl = message.getFile().getTTL();
 		String hash = message.getFile().getHash();
 
 		boolean foundHost = this.hostExists(host);
 
 		if (this.knownShares.containsKey(hash)) {
 			RemoteShare tempShare = this.knownShares.get(hash);
 
 			// case: host and share already cached
 			if (foundHost) {
 
 				// if the share is already associated to the host, just update
 				// ttl
 				if (host.getSharedFiles().containsKey(hash)) {
 					host.getSharedFiles().get(hash).setTTL(ttl);
 					if (this.guiCallbacks != null) {
 						this.guiCallbacks.updatedTTL(host.getSharedFiles().get(hash));
 					}
 				}
 
 				// if the share share isn't already associated, associate it to
 				// the host
 				else {
 					host.addFileToSharedFiles(tempShare.addFileSource(host, ttl, message.getFile().getDisplayName(),
 							message.getFile().getMeta()));
 					if (this.guiCallbacks != null) {
 						this.guiCallbacks.newShareAvailable(host.getSharedFiles().get(hash));
 					}
 				}
 
 				// case: share cached but new host
 			} else {
 				this.knownHosts.put(host.getAddress(), host);
 				host.addFileToSharedFiles(tempShare.addFileSource(host, ttl, message.getFile().getDisplayName(),
 						message.getFile().getMeta()));
 				if (this.guiCallbacks != null) {
 					this.guiCallbacks.newHostDiscovered(host);
 					this.guiCallbacks.newShareAvailable(host.getSharedFiles().get(hash));
 				}
 			}
 
 		} else {
 			// case: share not cached, but host already cached
 			if (foundHost) {
 				RemoteShare tmp = new RemoteShare(hash, message.getFile().getLength());
 				this.knownShares.put(hash, tmp);
 				host.addFileToSharedFiles(tmp.addFileSource(host, ttl, message.getFile().getDisplayName(), message
 						.getFile().getMeta()));
 				if (this.guiCallbacks != null) {
 					this.guiCallbacks.newShareAvailable(host.getSharedFiles().get(hash));
 				}
 			}
 
 			// case: neither host nor share cached
 			else {
 				this.knownHosts.put(host.getAddress(), host);
 				RemoteShare tmp = new RemoteShare(hash, message.getFile().getLength());
 				this.knownShares.put(hash, tmp);
 				host.addFileToSharedFiles(tmp.addFileSource(host, ttl, message.getFile().getDisplayName(), message
 						.getFile().getMeta()));
 				if (this.guiCallbacks != null) {
 					this.guiCallbacks.newHostDiscovered(host);
 					this.guiCallbacks.newShareAvailable(host.getSharedFiles().get(hash));
 				}
 			}
 		}
 	}
 
 	private void peerDiscoveryHandler(PeerDiscoveryMessage message) {
 		Host host = new Host(message.getSource().getAddress(), message.getSource().getHostName(), 0);
 		if (this.hostExists(host)) {
 			// update values
 			host.setDisplayName(message.getAlias());
 			host.setPort(message.getPort());
 		} else {
 			// add new host
 			this.knownHosts.put(host.getAddress(), host);
 			if (this.guiCallbacks != null) {
 				this.guiCallbacks.newHostDiscovered(host);
 			}
 		}
 		switch (message.getType()) {
 		case PeerDiscoveryMessage.TYPE_FIELD_HI:
 			try {
				if (this.udpAdapter != null) {
 					Thread.sleep((int) (Math.random() * Config.maximumYoResponseTime) + 5000);
 					fileTransferAdapter.downloadFilelist(host);
 				} else {
					LoggerWrapper.logError("Could not find UDP Adapter");
 				}
 			} catch (InterruptedException e) {
 				LoggerWrapper.logError(e.getMessage());
 			}
 			break;
 		case PeerDiscoveryMessage.TYPE_FIELD_YO: // nothing to do
 			break;
 		default:
 		}
 	}
 
 	private void byeHandler(ByeMessage message) {
 		Host host = new Host(message.getSource().getAddress(), message.getSource().getHostName(), 0);
 		if (this.hostExists(host)) {
 			RemoteShare tmp;
 			if (this.guiCallbacks != null) {
 				this.guiCallbacks.hostWentOffline(host);
 			}
 			for (String hash : host.getSharedFiles().keySet()) {
 				tmp = this.knownShares.get(hash);
 				tmp.removeFileSource(host.getSharedFiles().get(hash));
 				if (tmp.noSourcesAvailable()) {
 					this.knownShares.remove(hash);
 				}
 				this.knownHosts.remove(host.getAddress());
 			}
 		}
 	}
 
 	public Hashtable<String, RemoteShare> getShares() {
 		return this.knownShares;
 	}
 
 	public Hashtable<InetAddress, Host> getHosts() {
 		return this.knownHosts;
 	}
 
 	/**
 	 * Checks if the specified host already exists. Updates the last seen value
 	 * of the host.
 	 * 
 	 * Changes the host to the host found in cache!
 	 * 
 	 * @param host
 	 *            the specified host
 	 * @return true if the specified host was found
 	 */
 	public boolean hostExists(Host host) {
 		for (InetAddress address : this.knownHosts.keySet()) {
 			if (host.equals(this.knownHosts.get(address))) {
 				host = this.knownHosts.get(address);
 				host.Seen();
 				return true;
 			}
 		}
 		return false;
 	}
 
 	
 	public void removeHost(Host host) {
 		this.knownHosts.remove(host.getAddress());
 	}
 	
 	public boolean shareExists(String hash) {
 		return knownShares.get(hash) != null;
 	}
 
 	private void checkTTL() {
 		Host host;
 		ShareAvailability shareAvailability;
 		int tmpTTL = 0;
 		for (InetAddress address : knownHosts.keySet()) {
 			host = knownHosts.get(address);
 			for (String hash : host.getSharedFiles().keySet()) {
 				shareAvailability = host.getSharedFiles().get(hash);
 				tmpTTL = shareAvailability.getTtl();
 				if (tmpTTL <= TTL_REFRESH_RATE) {
 					host.removeFileFromSharedFiles(hash);
 					shareAvailability.getFile().removeFileSource(shareAvailability);
 					if (shareAvailability.getFile().noSourcesAvailable()) {
 						knownShares.remove(hash);
 					}
 					if (guiCallbacks != null) {
 						guiCallbacks.removeShare(shareAvailability);
 					}
 				} else {
 					shareAvailability.setTTL(tmpTTL - TTL_REFRESH_RATE);
 				}
 			}
 		}
 
 	}
 
 }
