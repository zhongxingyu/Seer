 package com.robonobo.mina.instance;
 
 import java.util.*;
 import java.util.concurrent.ScheduledThreadPoolExecutor;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.doomdark.uuid.UUID;
 import org.doomdark.uuid.UUIDGenerator;
 
 import com.robonobo.common.concurrent.SafetyNet;
 import com.robonobo.common.exceptions.SeekInnerCalmException;
 import com.robonobo.common.util.ExceptionEvent;
 import com.robonobo.common.util.ExceptionListener;
 import com.robonobo.core.api.*;
 import com.robonobo.core.api.proto.CoreApi.EndPoint;
 import com.robonobo.core.api.proto.CoreApi.Node;
 import com.robonobo.mina.agoric.BuyMgr;
 import com.robonobo.mina.agoric.SellMgr;
 import com.robonobo.mina.bidstrategy.BidStrategy;
 import com.robonobo.mina.escrow.EscrowMgr;
 import com.robonobo.mina.escrow.EscrowProvider;
 import com.robonobo.mina.external.*;
 import com.robonobo.mina.external.buffer.PageBuffer;
 import com.robonobo.mina.external.buffer.PageBufferProvider;
 import com.robonobo.mina.message.proto.MinaProtocol.Agorics;
 import com.robonobo.mina.network.*;
 import com.robonobo.mina.util.BadNodeList;
 import com.robonobo.mina.util.Tagline;
 
 /**
  * Main per-instance class - does little except hold references to mgr classes
  * @author macavity
  *
  */
 public class MinaInstance implements MinaControl {
 	public static final int MINA_PROTOCOL_VERSION = 1;
 
 	private String myNodeId;
 	private Log log;
 	private MinaConfig config;
 	private CCMgr ccm;
 	private MessageMgr messageMgr;
 	private NetworkMgr netMgr;
 	private BidStrategy bidStrategy;
 	private	StreamMgr streamMgr;
 	private StreamConnsMgr scm;
 	private PageRequestMgr prm;
 	private FlowRateMgr flowRateMgr;
 	private SupernodeMgr supernodeMgr;
 	private SellMgr sellMgr;
 	private BuyMgr buyMgr;
 	private SourceMgr sourceMgr;
 	private EscrowMgr escrowMgr;
 	private EscrowProvider escrowProvider;
 	private ScheduledThreadPoolExecutor executor;
 	private BadNodeList badNodes;
 	private EventMgr eventMgr;
 	private StreamAdvertiser streamAdvertiser;
 	private Application implementingApplication;
 	private boolean started = false;
 	private Agorics myAgorics;
 	private CurrencyClient curClient;
 	private PageBufferProvider pageBufProvider;
 
 	public MinaInstance(MinaConfig config, Application application, ScheduledThreadPoolExecutor executor) {
 		// Make sure we know about exceptions
 		SafetyNet.addListener(new MinaExceptionListener());
 		log = getLogger(getClass());
 		this.config = config;
 		this.executor = executor;
 		if (config.getNodeId() != null)
 			myNodeId = config.getNodeId();
 		else
 			myNodeId = generateNodeId();
 		log.fatal("Mina running on udp port " + config.getListenUdpPort() + ", node id: " + myNodeId);
 		messageMgr = new MessageMgr(this);
 		ccm = new CCMgr(this);
 		netMgr = new NetworkMgr(this);
 		try {
 			bidStrategy = (BidStrategy) Class.forName(config.getBidStrategyClass()).newInstance();
			bidStrategy.setMinaInstance(this);
 		} catch (Exception e) {
 			throw new SeekInnerCalmException(e);
 		}
 		scm = new StreamConnsMgr(this);
 		streamMgr = new StreamMgr(this);
 		prm = new PageRequestMgr(this);
 		flowRateMgr = new FlowRateMgr(this);
 		if (config.isSupernode())
 			supernodeMgr = new SupernodeMgr(this);
 		if (config.isAgoric()) {
 			sellMgr = new SellMgr(this);
 			buyMgr = new BuyMgr(this);
 			escrowMgr = new EscrowMgr(this);
 		}
 		if (config.getRunEscrowProvider()) {
 			escrowProvider = new EscrowProvider(this);
 		}
 		badNodes = new BadNodeList(this);
 		eventMgr = new EventMgr(this);
 		sourceMgr = new SourceMgr(this);
 		streamAdvertiser = new StreamAdvertiser(this);
 		implementingApplication = application;
 	}
 
 	private String generateNodeId() {
 		UUID uuid = UUIDGenerator.getInstance().generateRandomBasedUUID();
 		return uuid.toString().replace("-", "");
 	}
 
 	/**
 	 * @syncpriority 90
 	 */
 	public void abort() {
 		log.fatal("Mina instance ABORTING!");
 		ccm.prepareForShutdown();
 		scm.abort();
 		ccm.abort();
 		netMgr.stop();
 		started = false;
 		log.fatal("Mina instance stopped");
 	}
 
 	public void addMinaListener(MinaListener listener) {
 		eventMgr.addMinaListener(listener);
 	}
 
 	public void addNodeLocator(NodeLocator locator) {
 		getNetMgr().addNodeLocator(locator);
 	}
 
 	@Override
 	public void startBroadcasts(Collection<String> sids) {
 		streamMgr.startBroadcasts(sids);
 	}
 	
 	public void startBroadcast(String sid) {
 		streamMgr.startBroadcast(sid);
 	}
 
 	public void stopBroadcast(String sid) {
 		streamMgr.stopBroadcast(sid);
 	}
 
 	public void startReception(String sid, StreamVelocity sv) {
 		bidStrategy.setStreamVelocity(sid, sv);
 		streamMgr.startReception(sid);
 	}
 
 	public void stopReception(String sid) {
 		streamMgr.stopReception(sid);
 	}
 
 	public BadNodeList getBadNodeList() {
 		return badNodes;
 	}
 
 	public CCMgr getCCM() {
 		return ccm;
 	}
 
 	public MinaConfig getConfig() {
 		return config;
 	}
 
 	public List<ConnectedNode> getConnectedNodes() {
 		return ccm.getConnectedNodes();
 	}
 
 	public Set<String> getSources(String sid) {
 		return streamMgr.getSourceNodeIds(sid);
 	}
 
 	public int numSources(String sid) {
 		return streamMgr.numSources(sid);
 	}
 
 	public List<String> getConnectedSources(String sid) {
 		LCPair[] arrr = scm.getListenConns(sid);
 		List<String> result = new ArrayList<String>();
 		for (LCPair lcp : arrr) {
 			result.add(lcp.getCC().getNodeId());
 		}
 		return result;
 	}
 
 	public boolean isConnectedToSupernode() {
 		return ccm.haveSupernode();
 	}
 
 	public List<String> getMyEndPointUrls() {
 		List<String> result = new ArrayList<String>();
 		for (EndPointMgr epMgr : getNetMgr().getEndPointMgrs()) {
 			EndPoint localEp = epMgr.getLocalEndPoint();
 			if (localEp != null)
 				result.add(localEp.getUrl());
 			EndPoint publicEp = epMgr.getPublicEndPoint();
 			if (publicEp != null && ((localEp == null) || !publicEp.getUrl().equals(localEp.getUrl())))
 				result.add(publicEp.getUrl());
 		}
 		return result;
 	}
 
 	public MessageMgr getMessageMgr() {
 		return messageMgr;
 	}
 
 	public EventMgr getEventMgr() {
 		return eventMgr;
 	}
 
 	public ScheduledThreadPoolExecutor getExecutor() {
 		return executor;
 	}
 
 	public Application getImplementingApplication() {
 		return implementingApplication;
 	}
 
 	public Log getLogger(Class callerClass) {
 		return LogFactory.getLog(callerClass.getName() + ".INSTANCE." + myNodeId);
 	}
 
 	public String getMyNodeId() {
 		return myNodeId;
 	}
 
 	public NetworkMgr getNetMgr() {
 		return netMgr;
 	}
 
 	public SupernodeMgr getSupernodeMgr() {
 		return supernodeMgr;
 	}
 
 	public boolean isStarted() {
 		return started;
 	}
 
 	public void removeNodeLocator(NodeLocator locator) {
 		getNetMgr().removeNodeLocator(locator);
 	}
 
 	public void start() throws MinaException {
 		try {
 			log.info(Tagline.getTagLine());
 			netMgr.start();
 			started = true;
 		} catch (MinaException e) {
 			SafetyNet.notifyException(e, this);
 			log.fatal("MinaException caught on startup, stopping Mina");
 			stop();
 			throw e;
 		}
 	}
 
 	/**
 	 * @syncpriority 200
 	 */
 	public void stop() throws MinaException {
 		log.fatal("Mina instance stopping");
 		ccm.prepareForShutdown();
 		scm.closeAllStreamConns();
 		streamMgr.stop();
 		sourceMgr.stop();
 		streamAdvertiser.cancel();
 		ccm.stop();
 		netMgr.stop();
 		badNodes.clear();
 		started = false;
 		log.fatal("Mina instance stopped");
 	}
 
 	@Override
 	public String toString() {
 		return "[MinaInstance,id=" + myNodeId + "]";
 	}
 
 	public void addFoundSourceListener(String sid, FoundSourceListener listener) {
 		streamMgr.addFoundSourceListener(sid, listener);
 	}
 
 	public void removeFoundSourceListener(String sid, FoundSourceListener listener) {
 		streamMgr.removeFoundSourceListener(sid, listener);
 	}
 
 	public Set<Node> getKnownSources(String sid) {
 		return streamMgr.getKnownSources(sid);
 	}
 
 	private class MinaExceptionListener implements ExceptionListener {
 		public void onException(ExceptionEvent e) {
 			log.fatal(e.getSource().getClass().getName() + " caught Exception: ", e.getException());
 		}
 	}
 
 	public Map<String, TransferSpeed> getTransferSpeeds() {
 		Map<String, TransferSpeed> result = new HashMap<String, TransferSpeed>();
 		for (String sid : streamMgr.getLiveStreamIds()) {
 			int upload = flowRateMgr.getBroadcastingFlowRate(sid);
 			int download = flowRateMgr.getListeningFlowRate(sid);
 			if (upload > 0 || download > 0)
 				result.put(sid, new TransferSpeed(sid, download, upload));
 		}
 		return result;
 	}
 
 	public StreamAdvertiser getStreamAdvertiser() {
 		return streamAdvertiser;
 	}
 
 	public void clearStreamPriorities() {
 		streamMgr.clearStreamPriorities();
 	}
 
 	/**
 	 * The stream priority dictates the importance of streams relative to each other (higher is more important)
 	 */
 	public void setStreamPriority(String streamId, int priority) {
 		streamMgr.setPriority(streamId, priority);
 	}
 
 	/**
 	 * The streamvelocity dictates how fast we want this stream (slower = cheaper)
 	 */
 	public void setStreamVelocity(String sid, StreamVelocity sv) {
 		bidStrategy.setStreamVelocity(sid, sv);
 		// After the velocity is changed, we might want to inc/dec our bid to our sources
 		for (LCPair lcp : scm.getListenConns(sid)) {
 			buyMgr.possiblyRebid(lcp.getCC().getNodeId());
 		}
 	}
 
 	public void setAllStreamVelocitiesExcept(String streamId, StreamVelocity sv) {
 		String[] receivingSids = streamMgr.getReceivingStreamIds();
 		// After the velocity is changed, we might want to inc/dec our bid to our sources
 		Set<String> rebidNodeIds = new HashSet<String>();
 		for (String sid : receivingSids) {
 			if(!sid.equals(streamId)) {
 				bidStrategy.setStreamVelocity(sid, sv);
 				for (LCPair lcp : scm.getListenConns(sid)) {
 					rebidNodeIds.add(lcp.getCC().getNodeId());
 				}
 			}
 		}
 		for (String nodeId : rebidNodeIds) {
 			buyMgr.possiblyRebid(nodeId);
 		}
 	}
 
 	@Override
 	public void addNodeFilter(NodeFilter nf) {
 		netMgr.addNodeFilter(nf);
 	}
 
 	@Override
 	public void removeNodeFilter(NodeFilter nf) {
 		netMgr.removeNodeFilter(nf);
 	}
 
 	public void setCurrencyClient(CurrencyClient client) {
 		curClient = client;
 		Agorics.Builder ab = Agorics.newBuilder();
 		ab.setCurrencyUrl(client.currencyUrl());
 		ab.setAcceptPaymentMethods(client.getAcceptPaymentMethods());
 		ab.setMinBid(client.getMinBid());
 		ab.setIncrement(client.getBidIncrement());
 		ab.setMinTopRate(client.getMinTopRate());
 		myAgorics = ab.build();
 		if (escrowProvider != null)
 			escrowProvider.setCurrencyClient(client);
 	}
 
 	@Override
 	public void setPageBufferProvider(PageBufferProvider provider) {
 		pageBufProvider = provider;
 	}
 	
 	public PageBufferProvider getPageBufProvider() {
 		return pageBufProvider;
 	}
 	
 	public void configUpdated() {
 		netMgr.configUpdated();
 	}
 
 	@Override
 	public void setHandoverHandler(HandoverHandler handler) {
 		netMgr.setHandoverHandler(handler);
 	}
 
 	public CurrencyClient getCurrencyClient() {
 		return curClient;
 	}
 
 	public BidStrategy getBidStrategy() {
 		return bidStrategy;
 	}
 	
 	public StreamMgr getStreamMgr() {
 		return streamMgr;
 	}
 	
 	public StreamConnsMgr getSCM() {
 		return scm;
 	}
 	
 	public PageRequestMgr getPRM() {
 		return prm;
 	}
 	
 	public FlowRateMgr getFlowRateMgr() {
 		return flowRateMgr;
 	}
 	
 	public SellMgr getSellMgr() {
 		return sellMgr;
 	}
 
 	public BuyMgr getBuyMgr() {
 		return buyMgr;
 	}
 
 	public SourceMgr getSourceMgr() {
 		return sourceMgr;
 	}
 
 	public Agorics getMyAgorics() {
 		return myAgorics;
 	}
 
 	public EscrowMgr getEscrowMgr() {
 		return escrowMgr;
 	}
 
 	public EscrowProvider getEscrowProvider() {
 		return escrowProvider;
 	}
 }
