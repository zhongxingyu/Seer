 package com.robonobo.mina.external;
 
 import java.io.Serializable;
 
 /**
  * A JavaBean representation of the config for Mina.
  * 
  * @author ray
  * 
  */
 public class MinaConfig implements Cloneable, Serializable {
 	String localAddress;
 	String gatewayAddress;
 	String nodeId;
 	/** Comma-sep list of fq classnames of endpoint mgrs */
 	String endPointMgrClasses = "com.robonobo.mina.network.eon.EonEndPointMgr";
 	/** FQ classname of bid strategy */
	String bidStrategyClass = "com.robonobo.mina.stream.bidstrategy.DefaultBidStrategy";
 	int maxCachedBroadcasterAge = 300;
 	int listenUdpPort = 23232;
 	int gatewayUdpPort = 17235;
 	int localLocatorUdpPort = 23232;
 	int messageMaxSize = 16384; // Bytes
 	int pageReadBufferSize = 262144; // Bytes
 	int messageTimeout = 30; // Seconds
 	int initialBadNodeTimeout = 60; // Seconds
 	int locateNodesFreq = 120; // Seconds
 	int maxSources = 16; // Maximum number of sources to receive from - per
 	// stream
 	int maxRunningListeners = 8; // Max number of nodes receiving from us at more than 0bps - global
 	int logRateFreq = 1000; // Millisecs - 0 to disable
 	boolean logFlowRates = false;
 	boolean locateLocalNodes = true;
 	boolean locateRemoteNodes = true;
 	boolean supernode = false;
 	boolean sendPrivateAddrsToLocator = false;
 	/** Are we using the currency system? */
 	boolean agoric = true;
 	int velocityCheckFreq = 5000; // Millisecs
 	int auctionStateCacheTime = 1000; // Millisecs
 	int bidTimeout = 10000; // Millisecs
 	int minTimeBetweenAuctions = 30000; // Millisecs
 	/**
 	 * Make sure we have enough in our accounts with senders to receive for this
 	 * time (secs)
 	 */
 	int balanceBufferTime = 30;
 	/**
 	 * When we get a payment demand and we find that our balance doesn't agree
 	 * with what they say, it might be due to pages in flight - back off for
 	 * this many seconds and try again
 	 */
 	int payUpCatchUpTime = 10;
 	/**
 	 * If a source does not have data useful to us, wait this many secs before
 	 * polling it again (secs)
 	 */
 	int sourceDataFailWaitTime = 60;
 	/**
 	 * If a source is too expensive for us, wait this many secs before polling
 	 * it again (secs)
 	 */
 	int sourceAgoricsFailWaitTime = 60;
 	/**
 	 * If a source dies unexpectedly, query it again after this many secs to see
 	 * if it's returned
 	 */
 	int deadSourceQueryTime = 60;
 	/**
 	 * When we first hear about a source and query it, retry after this many secs
 	 */
 	int initialSourceQueryTime = 60;
 	/**
 	 * If a source doesn't answer a query for status, retry this many times
 	 * before giving up (doubling wait time each time)
 	 */
 	int sourceQueryRetries = 3;
 	/**
 	 * Batch up requests for source information, waiting a max of this many ms
 	 * before sending. Requests for immediate-playback streams are not batched
 	 */
 	int sourceRequestBatchTime = 2000;
 	/**
 	 * When we request pages, ask for this many millisecs' worth based on
 	 * current flow rate
 	 */
 	int pageRequestLookAheadTime = 2000;
 	/**
 	 * Ask for pages up to this many millisecs ahead of our last contiguous page
 	 */
 	int pageWindowTime = 10000;
 	/**
 	 * If a source has no pages that are useful to us for this many secs, we close the conn
 	 */
 	int usefulDataSourceTimeout = 120;
 	/**
 	 * Run escrow services on this node
 	 */
 	boolean runEscrowProvider = false;
 	/**
 	 * Percent fee
 	 */
 	int escrowFee = 1;
 	int maxOutboundBps = -1;
 	
 	public MinaConfig() {
 	}
 
 	public Object clone() {
 		try {
 			return super.clone();
 		} catch (CloneNotSupportedException e) {
 			throw new RuntimeException("BUG: should ALWAYS support Clonable", e);
 		}
 	}
 
 	public int getMessageMaxSize() {
 		return messageMaxSize;
 	}
 
 	public int getMessageTimeout() {
 		return messageTimeout;
 	}
 
 	public String getGatewayAddress() {
 		return gatewayAddress;
 	}
 
 	public int getGatewayUdpPort() {
 		return gatewayUdpPort;
 	}
 
 	public int getInitialBadNodeTimeout() {
 		return initialBadNodeTimeout;
 	}
 
 	public String getLocalAddress() {
 		return localAddress;
 	}
 
 	public int getLocalLocatorUdpPort() {
 		return localLocatorUdpPort;
 	}
 
 	public int getListenUdpPort() {
 		return listenUdpPort;
 	}
 
 	public int getLocateNodesFreq() {
 		return locateNodesFreq;
 	}
 
 	public int getLogRateFreq() {
 		return logRateFreq;
 	}
 
 	public int getMaxSources() {
 		return maxSources;
 	}
 
 	public int getMaxCachedBroadcasterAge() {
 		return maxCachedBroadcasterAge;
 	}
 
 	public String getNodeId() {
 		return nodeId;
 	}
 
 	public boolean getLocateLocalNodes() {
 		return locateLocalNodes;
 	}
 
 	public boolean getLogFlowRates() {
 		return logFlowRates;
 	}
 
 	public void setLogFlowRates(boolean logFlowRates) {
 		this.logFlowRates = logFlowRates;
 	}
 
 	public boolean isSupernode() {
 		return supernode;
 	}
 
 	public void setMessageMaxSize(int commandMaxSize) {
 		this.messageMaxSize = commandMaxSize;
 	}
 
 	public void setMessageTimeout(int commandTimeout) {
 		this.messageTimeout = commandTimeout;
 	}
 
 	public void setGatewayAddress(String gatewayAddress) {
 		this.gatewayAddress = gatewayAddress;
 	}
 
 	public void setGatewayUdpPort(int gatewayUdpPort) {
 		this.gatewayUdpPort = gatewayUdpPort;
 	}
 
 	public void setInitialBadNodeTimeout(int initialBadNodeTimeout) {
 		this.initialBadNodeTimeout = initialBadNodeTimeout;
 	}
 
 	public void setLocalAddress(String localAddress) {
 		this.localAddress = localAddress;
 	}
 
 	public void setLocalLocatorUdpPort(int localLocatorUdpPort) {
 		this.localLocatorUdpPort = localLocatorUdpPort;
 	}
 
 	public void setListenUdpPort(int localUdpPort) {
 		this.listenUdpPort = localUdpPort;
 	}
 
 	public void setLocateLocalNodes(boolean locateLocalNodes) {
 		this.locateLocalNodes = locateLocalNodes;
 	}
 
 	public void setLocateNodesFreq(int locateNodesFreq) {
 		this.locateNodesFreq = locateNodesFreq;
 	}
 
 	public void setLogRateFreq(int logRateFreq) {
 		this.logRateFreq = logRateFreq;
 	}
 
 	public void setMaxSources(int maxBroadcasters) {
 		this.maxSources = maxBroadcasters;
 	}
 
 	public void setMaxCachedBroadcasterAge(int maxCachedBroadcasterAge) {
 		this.maxCachedBroadcasterAge = maxCachedBroadcasterAge;
 	}
 
 	public void setNodeId(String nodeId) {
 		this.nodeId = nodeId;
 	}
 
 	public void setSupernode(boolean supernode) {
 		this.supernode = supernode;
 	}
 
 	public String getEndPointMgrClasses() {
 		return endPointMgrClasses;
 	}
 
 	public void setEndPointMgrClasses(String endPointMgrClasses) {
 		this.endPointMgrClasses = endPointMgrClasses;
 	}
 
 	public boolean getSendPrivateAddrsToLocator() {
 		return sendPrivateAddrsToLocator;
 	}
 
 	public void setSendPrivateAddrsToLocator(boolean sendPrivateAddrsToLocator) {
 		this.sendPrivateAddrsToLocator = sendPrivateAddrsToLocator;
 	}
 
 	public boolean isAgoric() {
 		return agoric;
 	}
 
 	public void setAgoric(boolean agoric) {
 		this.agoric = agoric;
 	}
 
 	public int getVelocityCheckFreq() {
 		return velocityCheckFreq;
 	}
 
 	public void setVelocityCheckFreq(int velocityCheckFreq) {
 		this.velocityCheckFreq = velocityCheckFreq;
 	}
 
 	public int getAuctionStateCacheTime() {
 		return auctionStateCacheTime;
 	}
 
 	public void setAuctionStateCacheTime(int auctionStatusCacheTime) {
 		this.auctionStateCacheTime = auctionStatusCacheTime;
 	}
 
 	public int getBidTimeout() {
 		return bidTimeout;
 	}
 
 	public void setBidTimeout(int bidTimeout) {
 		this.bidTimeout = bidTimeout;
 	}
 
 	public int getMinTimeBetweenAuctions() {
 		return minTimeBetweenAuctions;
 	}
 
 	public void setMinTimeBetweenAuctions(int minTimeBetweenAuctions) {
 		this.minTimeBetweenAuctions = minTimeBetweenAuctions;
 	}
 
 	public int getBalanceBufferTime() {
 		return balanceBufferTime;
 	}
 
 	public void setBalanceBufferTime(int balanceBufferTime) {
 		this.balanceBufferTime = balanceBufferTime;
 	}
 
 	public int getPayUpCatchUpTime() {
 		return payUpCatchUpTime;
 	}
 
 	public void setPayUpCatchUpTime(int payUpCatchUpTime) {
 		this.payUpCatchUpTime = payUpCatchUpTime;
 	}
 
 	public int getSourceDataFailWaitTime() {
 		return sourceDataFailWaitTime;
 	}
 
 	public void setSourceDataFailWaitTime(int sourceDataFailWaitTime) {
 		this.sourceDataFailWaitTime = sourceDataFailWaitTime;
 	}
 
 	public int getSourceAgoricsFailWaitTime() {
 		return sourceAgoricsFailWaitTime;
 	}
 
 	public void setSourceAgoricsFailWaitTime(int sourceAgoricsFailWaitTime) {
 		this.sourceAgoricsFailWaitTime = sourceAgoricsFailWaitTime;
 	}
 
 	public String getBidStrategyClass() {
 		return bidStrategyClass;
 	}
 
 	public void setBidStrategyClass(String bidStrategyClass) {
 		this.bidStrategyClass = bidStrategyClass;
 	}
 
 	public int getPageReadBufferSize() {
 		return pageReadBufferSize;
 	}
 
 	public void setPageReadBufferSize(int pageReadBufferSize) {
 		this.pageReadBufferSize = pageReadBufferSize;
 	}
 
 	public boolean getLocateRemoteNodes() {
 		return locateRemoteNodes;
 	}
 
 	public void setLocateRemoteNodes(boolean locateRemoteNodes) {
 		this.locateRemoteNodes = locateRemoteNodes;
 	}
 
 	public int getSourceRequestBatchTime() {
 		return sourceRequestBatchTime;
 	}
 
 	public void setSourceRequestBatchTime(int sourceRequestBatchTime) {
 		this.sourceRequestBatchTime = sourceRequestBatchTime;
 	}
 
 	public int getDeadSourceQueryTime() {
 		return deadSourceQueryTime;
 	}
 
 	public void setDeadSourceQueryTime(int deadSourceQueryTime) {
 		this.deadSourceQueryTime = deadSourceQueryTime;
 	}
 
 	public int getSourceQueryRetries() {
 		return sourceQueryRetries;
 	}
 
 	public void setSourceQueryRetries(int sourceQueryRetries) {
 		this.sourceQueryRetries = sourceQueryRetries;
 	}
 
 	public int getPageRequestLookAheadTime() {
 		return pageRequestLookAheadTime;
 	}
 
 	public void setPageRequestLookAheadTime(int pageRequestLookAheadTime) {
 		this.pageRequestLookAheadTime = pageRequestLookAheadTime;
 	}
 
 	public int getPageWindowTime() {
 		return pageWindowTime;
 	}
 
 	public void setPageWindowTime(int pageWindowTime) {
 		this.pageWindowTime = pageWindowTime;
 	}
 
 	public int getUsefulDataSourceTimeout() {
 		return usefulDataSourceTimeout;
 	}
 
 	public void setUsefulDataSourceTimeout(int usefulDataSourceTimeout) {
 		this.usefulDataSourceTimeout = usefulDataSourceTimeout;
 	}
 
 	public boolean getRunEscrowProvider() {
 		return runEscrowProvider;
 	}
 
 	public void setRunEscrowProvider(boolean runEscrowProvider) {
 		this.runEscrowProvider = runEscrowProvider;
 	}
 
 	public int getEscrowFee() {
 		return escrowFee;
 	}
 
 	public void setEscrowFee(int escrowFee) {
 		this.escrowFee = escrowFee;
 	}
 
 	public int getMaxOutboundBps() {
 		return maxOutboundBps;
 	}
 
 	public void setMaxOutboundBps(int maxOutboundBps) {
 		this.maxOutboundBps = maxOutboundBps;
 	}
 
 	public int getInitialSourceQueryTime() {
 		return initialSourceQueryTime;
 	}
 
 	public void setInitialSourceQueryTime(int initialSourceQueryTime) {
 		this.initialSourceQueryTime = initialSourceQueryTime;
 	}
 
 	public int getMaxRunningListeners() {
 		return maxRunningListeners;
 	}
 
 	public void setMaxRunningListeners(int maxListeners) {
 		this.maxRunningListeners = maxListeners;
 	}
 }
