 /*******************************************************************************
  * Copyright (c) 2013 Juuso Vilmunen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  * 
  * Contributors:
  *     Juuso Vilmunen - initial API and implementation
  ******************************************************************************/
 package waazdoh.cp2p.impl;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 import waazdoh.client.Binary;
 import waazdoh.client.MStringID;
 import waazdoh.cp2p.impl.handlers.ByteArraySource;
 import waazdoh.cp2p.impl.handlers.PingHandler;
 import waazdoh.cp2p.impl.handlers.StreamHandler;
 import waazdoh.cp2p.impl.handlers.WhoHasHandler;
 import waazdoh.cutils.MLogger;
 import waazdoh.cutils.MPreferences;
 import waazdoh.cutils.xml.JBean;
 import waazdoh.service.ReportingService;
 
 public final class P2PServer implements MMessager, MMessageFactory,
 		MNodeConnection {
 	static final int MESSAGESENDLOOP_COUNT = 10;
 	private static final int MAX_SENTCOUNT = 2;
 	private static final long REBOOT_DELAY = 120000;
 	//
 	public static final String PREFERENCES_PORT = "p2pserver.port";
 	//
 	private MLogger log = MLogger.getLogger(this);
 	private final Map<MStringID, Download> downloads = new HashMap<MStringID, Download>();
 	MStringID networkid;
 	Map<String, MMessageHandler> handlers = new HashMap<String, MMessageHandler>();
 	//
 	List<Node> nodes = new LinkedList<Node>();
 	Set<SourceListener> sourcelisteners = new HashSet<SourceListener>();
 	private Map<MessageID, MessageResponseListener> responselisteners = new HashMap<MessageID, MessageResponseListener>();
 	//
 	boolean dobind;
 	private boolean closed = false;
 	private MPreferences p;
 	private ByteArraySource bytesource;
 	private long lastmessagereceived;
 	private TCPListener tcplistener;
 
 	private long bytecountstart = System.currentTimeMillis();
 	private int inputbytecount;
 	private long outputbytecount;
 
 	private ThreadGroup tg = new ThreadGroup("p2p");
 	private ReportingService reporting;
 
 	public P2PServer(MPreferences p, boolean bind2, ByteArraySource nbytesource) {
 		this.p = p;
 		this.dobind = bind2;
 		this.bytesource = nbytesource;
 		initHandlers();
 		//
 		runChecker();
 	}
 
 	public void setReportingService(ReportingService reporting) {
 		this.reporting = reporting;
 	}
 
 	@Override
 	public void reportDownload(MStringID id, boolean success) {
 		if (reporting != null) {
 			reporting.reportDownload(id, success);
 		}
 	}
 
 	public String getInfoText() {
 		String s = "nodes:" + nodes.size() + " downloads:" + downloads.size();
 		s += " " + tcplistener + " messagereceived:"
 				+ (System.currentTimeMillis() - lastmessagereceived)
 				+ "ms ago ";
 		long dtime = System.currentTimeMillis() - bytecountstart;
		if (dtime > 0) {
			s += " I:" + (inputbytecount * 1000 / dtime);
			s += " O:" + (outputbytecount * 1000 / dtime);
		}
		//
 		if (dtime > 10 * 1000) {
 			bytecountstart = System.currentTimeMillis();
 			inputbytecount = 0;
 			outputbytecount = 0;
 		}
 		return s;
 	}
 
 	public String getMemoryUserInfo() {
 		String info = "";
 
 		info += "downloads:" + downloads.size();
 		info += "[";
 		for (Download download : downloads.values()) {
 			info += download.getMemoryUsageInfo();
 		}
 		info += "]";
 
 		for (Node node : nodes) {
 			info += " node:" + node.getMemoryUsageInfo();
 		}
 		return info;
 	}
 
 	private void initHandlers() {
 		handlers.put("ping", new PingHandler());
 		handlers.put("whohas", new WhoHasHandler(bytesource, this));
 		handlers.put("stream", new StreamHandler(this));
 		handlers.put("connectednode", new ConnectedNodeHandler(this));
 		handlers.put("newnode", new NewNodeHandler(this));
 		//
 		for (MMessageHandler handler : handlers.values()) {
 			handler.setFactory(this);
 		}
 	}
 
 	private void sleep(long time) {
 		synchronized (this) {
 			try {
 				if (time < 10) {
 					time = 10;
 				}
 				this.wait(time);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 				log.error(e);
 			}
 		}
 	}
 
 	public void setDownloadEverything(boolean b) {
 		if (b) {
 			WhoHasHandler handler = (WhoHasHandler) handlers.get("whohas");
 			handler.downloadEveryThing(true);
 		}
 	}
 
 	private void runChecker() {
 		final Thread t = new Thread(tg, new Runnable() {
 			@Override
 			public void run() {
 				lastmessagereceived = System.currentTimeMillis();
 				while (isRunning()) {
 					long dt = System.currentTimeMillis() - lastmessagereceived;
 					if (dt > REBOOT_DELAY) {
 						reboot();
 					} else {
 						sleep(dt - REBOOT_DELAY);
 					}
 				}
 			}
 		}, "P2PServer checker");
 		t.start();
 	}
 
 	private void reboot() {
 		try {
 			log.info("rebootin server "
 					+ (REBOOT_DELAY - System.currentTimeMillis()));
 			close();
 			startNetwork();
 		} catch (Exception e) {
 			log.error(e);
 		}
 	}
 
 	public void startNetwork() {
 		lastmessagereceived = System.currentTimeMillis();
 		closed = false;
 		if (networkid == null) {
 			networkid = new MStringID();
 		}
 		//
 		if (dobind) {
 			tcplistener = new TCPListener(tg, this, p);
 			tcplistener.start();
 		}
 
 		addDefaultNodes();
 		//
 		startMessageSendLoops();
 	}
 
 	public Node addNode(MHost string, int i) {
 		log.info("addnode " + string + " " + i);
 		Node n = new Node(null, string, i, this);
 		addNode(this, n);
 		return n;
 	}
 
 	Node addNode(MNodeID nodeid, MHost host, int port) {
 		Node n = new Node(nodeid, host, port, this);
 		addNode(this, n);
 		return n;
 	}
 
 	public void addNode(P2PServer p2pServer, Node n) {
 		log.info("adding node " + n);
 		synchronized (nodes) {
 			nodes.add(n);
 		}
 		//
 		Set<SourceListener> listeners = sourcelisteners;
 		for (SourceListener sourceListener : listeners) {
 			sourceListener.nodeAdded(n);
 		}
 	}
 
 	void startMessageSendLoops() {
 		int loopcount = MESSAGESENDLOOP_COUNT;
 		for (int i = 0; i < loopcount; i++) {
 			Thread t = new Thread(tg, new Runnable() {
 				@Override
 				public void run() {
 					messageSendLoop();
 				}
 			}, "MessageSendLooper");
 			t.start();
 		}
 	}
 
 	private void messageSendLoop() {
 		while (isRunning()) {
 			Node node = null;
 			Iterator<Node> iterator;
 			iterator = getNodesIterator();
 			//
 			while (iterator.hasNext()) {
 				node = iterator.next();
 				if (node.checkPing()) {
 					sendPing(node);
 				}
 				//
 				if (node.getID() != null && node.getID().equals(networkid)) {
 					log.info("Having myself as remote node. Removing.");
 					synchronized (nodes) {
 						node.close();
 						nodes.remove(node);
 					}
 				} else if (node.shouldDie()) {
 					log.info("Killing node " + node);
 					synchronized (nodes) {
 						node.close();
 						nodes.remove(node);
 					}
 				} else {
 					node.check();
 				}
 			}
 			synchronized (nodes) {
 				if (nodes.size() == 0) {
 					addDefaultNodes();
 				}
 				//
 				try {
 					nodes.wait(100 + (int) (Math.random() * 100 * nodes.size() * MESSAGESENDLOOP_COUNT));
 				} catch (InterruptedException e) {
 					log.error(e);
 				}
 			}
 		}
 		log.info("server thread shutting down");
 	}
 
 	private void sendPing(Node node) {
 		node.addMessage(getMessage("ping"), new MessageResponseListener() {
 			private long sent = System.currentTimeMillis();
 			private boolean done = false;
 
 			@Override
 			public void messageReceived(Node n, MMessage message) {
 				log.info("PING response in "
 						+ (System.currentTimeMillis() - sent) + " ms");
 				done = true;
 			}
 
 			@Override
 			public boolean isDone() {
 				if ((System.currentTimeMillis() - sent) > 10000) {
 					log.info("PING giving up");
 					return true;
 				} else {
 					return done;
 				}
 			}
 		});
 	}
 
 	private Iterator<Node> getNodesIterator() {
 		Iterator<Node> iterator;
 		synchronized (nodes) {
 			if (nodes.size() == 0) {
 				addDefaultNodes();
 			}
 			iterator = new LinkedList<Node>(this.nodes).iterator();
 		}
 		return iterator;
 	}
 
 	private void addDefaultNodes() {
 		if (tcplistener != null && dobind) {
 			tcplistener.addDefaultNodes();
 		}
 		String slist = p.get(MPreferences.SERVERLIST, null);
 		log.info("got server list " + slist);
 		if (slist != null) {
 			StringTokenizer st = new StringTokenizer(slist, ",");
 			while (st.hasMoreTokens()) {
 				String server = st.nextToken();
 				for (int i = 0; i < 6; i++) {
 					addNode(new MHost(server), TCPListener.DEFAULT_PORT - 5 + i);
 				}
 			}
 		}
 	}
 
 	public void notifyNewMessages() {
 		synchronized (nodes) {
 			nodes.notifyAll();
 		}
 	}
 
 	public void broadcastMessage(MMessage notification) {
 		broadcastMessage(notification, null);
 	}
 
 	@Override
 	public void broadcastMessage(MMessage notification,
 			MessageResponseListener messageResponseListener) {
 		broadcastMessage(notification, messageResponseListener, null);
 	}
 
 	@Override
 	public void addSourceListener(SourceListener nodeListener) {
 		sourcelisteners.add(nodeListener);
 	}
 
 	public void broadcastMessage(MMessage notification,
 			MessageResponseListener messageResponseListener,
 			Set<MNodeID> exceptions) {
 		if (notification.getSentCount() <= MAX_SENTCOUNT) {
 			notification.addAttribute("sentcount",
 					notification.getSentCount() + 1);
 			synchronized (nodes) {
 				for (Node node : nodes) {
 					if (exceptions == null
 							|| !exceptions.contains(node.getID())) {
 						node.addMessage(notification, messageResponseListener);
 					}
 				}
 				//
 				nodes.notify();
 			}
 		} else {
 			log.info("not sending m" + "essage " + notification
 					+ " due sentcount " + notification.getSentCount());
 		}
 	}
 
 	public boolean isRunning() {
 		return !closed;
 	}
 
 	public void close() {
 		log.info("closing server");
 		if (getID() != null) {
 			broadcastMessage(new MMessage("close", getID()));
 			//
 		}
 		closed = true;
 
 		LinkedList<Node> ns = new LinkedList<Node>(nodes);
 		for (Node n : ns) {
 			n.close();
 		}
 
 		if (tcplistener != null) {
 			tcplistener.close();
 		}
 		synchronized (nodes) {
 			nodes.notifyAll();
 		}
 		log.info("closing nodes");
 		synchronized (nodes) {
 			for (Node node : nodes) {
 				node.close();
 			}
 		}
 		log.info("closing done");
 	}
 
 	public List<MMessage> handle(List<MMessage> messages) {
 		lastmessagereceived = System.currentTimeMillis();
 		//
 		Node sentbynode = null; // should be same node for every message
 		Node lasthandler = null;
 		for (MMessage message : messages) {
 			MNodeID lasthandlerid = message.getLastHandler();
 			lasthandler = getNode(lasthandlerid);
 
 			MNodeID sentby = message.getSentBy();
 			//
 			sentbynode = getNode(sentby);
 			if (sentbynode == null) {
 				sentbynode = new Node(sentby, this);
 				addNode(this, sentbynode);
 			}
 			//
 			sentbynode.touch();
 			//
 			log.info("handling message: " + message + " from " + sentbynode);
 			message.setLastHandler(networkid);
 			//
 			if (!networkid.equals(message.getSentBy())) {
 				handle(message, lasthandler != null ? lasthandler : sentbynode);
 			} else {
 				log.info("not handling message because networkid is equal with sentby "
 						+ message);
 			}
 		}
 
 		List<MMessage> ret;
 		if (lasthandler != null) {
 			ret = lasthandler.getMessages();
 		} else if (sentbynode != null) {
 			ret = sentbynode.getMessages();
 		} else {
 			ret = new LinkedList<MMessage>();
 		}
 		log.info("" + messages.size() + " handled and returning " + ret);
 		return ret;
 	}
 
 	public void handle(MMessage message, Node node) {
 		try {
 			inputbytecount += message.getByteCount();
 			log.info(this.toString() + " " + message);
 			MMessageHandler handler = getHandler(message.getName());
 			//
 			JBean nodeinfo = message.get("nodeinfo");
 			if (nodeinfo != null) {
 				List<JBean> nodeinfos = nodeinfo.getChildren();
 				for (JBean inode : nodeinfos) {
 					MNodeID nodeinfoid = new MNodeID(inode.getName());
 					if (getNode(nodeinfoid) == null) {
 						MHost host = new MHost(inode.getValue("host"));
 						int port = inode.getIntValue("port");
 						addNode(nodeinfoid, host, port);
 					}
 				}
 			}
 			//
 			MessageID responseto = message.getResponseTo();
 			MNodeID to = message.getTo("to");
 			if (to == null || to.equals(getID())) {
 				if (handler != null) {
 					handler.handle(message, node);
 				} else if (responseto == null) {
 					log.info("unknown message " + message);
 				}
 			} else {
 				Node tonode = getNode(to);
 				log.info("redirecting message to " + tonode);
 				if (tonode != null) {
 					node.addInfoTo(message);
 					tonode.addMessage(message);
 				} else {
 					log.info("message recieved in wrong node " + message);
 				}
 			}
 			//
 			if (responseto != null) {
 				MessageResponseListener responselistener = getResponseListener(responseto);
 				if (responselistener != null) {
 					responselistener.messageReceived(node, message);
 					removeResponseListener(responseto);
 				}
 			}
 		} catch (Exception e) {
 			log.error(e);
 			node.warning();
 		}
 	}
 
 	private MMessageHandler getHandler(String name) {
 		return handlers.get(name);
 	}
 
 	@Override
 	public MMessage newResponseMessage(MMessage childb, String string) {
 		MMessage m = getMessage(string);
 		m.addAttribute("responseto", childb.getAttribute("messageid"));
 		m.addIDAttribute("to", childb.getSentBy());
 		return m;
 	}
 
 	@Override
 	public MMessage getMessage(String string) {
 		MMessage ret = new MMessage(string, networkid);
 		ret.addAttribute("date", "" + new Date());
 		return ret;
 	}
 
 	public Node getNode(MNodeID sentby) {
 		if (sentby != null) {
 			for (Node node : nodes) {
 				if (sentby.equals(node.getID())) {
 					return node;
 				}
 			}
 		}
 		return null;
 	}
 
 	private void removeResponseListener(MessageID id) {
 		synchronized (responselisteners) {
 			responselisteners.remove(id);
 		}
 
 	}
 
 	public void addResponseListener(MessageID id,
 			MessageResponseListener messageResponseListener) {
 		synchronized (responselisteners) {
 			if (messageResponseListener != null) {
 				responselisteners.put(id, messageResponseListener);
 			}
 		}
 	}
 
 	private MessageResponseListener getResponseListener(MessageID id) {
 		synchronized (responselisteners) {
 			return responselisteners.get(id);
 		}
 	}
 
 	public void nodeConnected(Node node) {
 		MMessage m = new MMessage("connectednode", getID());
 		Set<MNodeID> exceptions = new HashSet<MNodeID>();
 		exceptions.add(node.getID());
 		broadcastMessage(m, null, exceptions);
 	}
 
 	public MStringID getID() {
 		return networkid;
 	}
 
 	@Override
 	public Download getDownload(MStringID streamid) {
 		return downloads.get(streamid);
 	}
 
 	@Override
 	public void removeDownload(MStringID did) {
 		synchronized (downloads) {
 			log.info("removing download " + did);
 			if (getDownload(did) != null) {
 				getDownload(did).stop();
 				downloads.remove(did);
 			}
 		}
 	}
 
 	public void addDownload(Binary bs) {
 		synchronized (downloads) {
 			if (downloads.get(bs.getID()) == null) {
 				log.info("adding download " + bs);
 				downloads.put(bs.getID(), new Download(bs, this));
 			}
 		}
 	}
 
 	public void start() {
 		startNetwork();
 	}
 
 	public boolean canDownload() {
 		return downloads.size() < p.getInteger(
 				MPreferences.NETWORK_MAX_DOWNLOADS, 8);
 	}
 
 	public void clearMemory(int suggestedmemorytreshold) {
 		synchronized (responselisteners) {
 			HashMap<MessageID, MessageResponseListener> nresponselisteners = new HashMap<MessageID, MessageResponseListener>(
 					responselisteners);
 			for (MessageID id : nresponselisteners.keySet()) {
 				MessageResponseListener l = getResponseListener(id);
 				if (l.isDone()) {
 					removeResponseListener(id);
 				}
 			}
 			//
 			synchronized (sourcelisteners) {
 				List<SourceListener> nsourcelisteners = new LinkedList<SourceListener>(
 						sourcelisteners);
 				for (SourceListener l : nsourcelisteners) {
 					if (l.isDone()) {
 						sourcelisteners.remove(l);
 					}
 				}
 			}
 
 			synchronized (downloads) {
 				Map<MStringID, Download> ndownloads = new HashMap<MStringID, Download>(
 						downloads);
 				for (MStringID did : ndownloads.keySet()) {
 					Download d = getDownload(did);
 					if (d.isDone()) {
 						removeDownload(did);
 					}
 				}
 
 			}
 		}
 		//
 	}
 
 	public boolean waitForDownloadSlot(int i) {
 		try {
 			while (!canDownload() && i > 0) {
 				synchronized (this) {
 					this.wait(100);
 					i -= 100;
 				}
 			}
 		} catch (InterruptedException e) {
 			log.error(e);
 		}
 
 		return canDownload();
 
 	}
 }
