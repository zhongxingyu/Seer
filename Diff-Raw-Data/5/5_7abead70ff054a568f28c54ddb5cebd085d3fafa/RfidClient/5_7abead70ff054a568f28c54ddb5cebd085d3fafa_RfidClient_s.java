 /**
  * Copyright (c) 2011, SOCIETIES Consortium (WATERFORD INSTITUTE OF TECHNOLOGY (TSSG), HERIOT-WATT UNIVERSITY (HWU), SOLUTA.NET 
  * (SN), GERMAN AEROSPACE CENTRE (Deutsches Zentrum fuer Luft- und Raumfahrt e.V.) (DLR), Zavod za varnostne tehnologije
  * informacijske družbe in elektronsko poslovanje (SETCCE), INSTITUTE OF COMMUNICATION AND COMPUTER SYSTEMS (ICCS), LAKE
  * COMMUNICATIONS (LAKE), INTEL PERFORMANCE LEARNING SOLUTIONS LTD (INTEL), PORTUGAL TELECOM INOVAÇÃO, SA (PTIN), IBM Corp., 
  * INSTITUT TELECOM (ITSUD), AMITEC DIACHYTI EFYIA PLIROFORIKI KAI EPIKINONIES ETERIA PERIORISMENIS EFTHINIS (AMITEC), TELECOM 
  * ITALIA S.p.a.(TI),  TRIALOG (TRIALOG), Stiftelsen SINTEF (SINTEF), NEC EUROPE LTD (NEC))
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
  * conditions are met:
  *
  * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
  *
  * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
  *    disclaimer in the documentation and/or other materials provided with the distribution.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
  * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT 
  * SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package ac.hw.rfid.client;
 
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.societies.api.comm.xmpp.interfaces.ICommManager;
 import org.societies.api.context.broker.ICtxBroker;
 import org.societies.api.context.model.CtxAttributeTypes;
 import org.societies.api.context.source.ICtxSourceMgr;
 import org.societies.api.identity.IIdentity;
 import org.societies.api.identity.IIdentityManager;
 import org.societies.api.identity.RequestorService;
 import org.societies.api.osgi.event.CSSEvent;
 import org.societies.api.osgi.event.CSSEventConstants;
 import org.societies.api.osgi.event.EventListener;
 import org.societies.api.osgi.event.EventTypes;
 import org.societies.api.osgi.event.IEventMgr;
 import org.societies.api.osgi.event.InternalEvent;
 import org.societies.api.schema.servicelifecycle.model.ServiceResourceIdentifier;
 import org.societies.api.services.IServices;
 import org.societies.api.services.ServiceMgmtEvent;
 import org.societies.api.services.ServiceMgmtEventType;
 
 import ac.hw.rfid.client.api.IRfidClient;
 import ac.hw.rfid.server.api.remote.IRfidServer;
 
 
 
 
 /**
  * @author  Eliza Papadopoulou
  * @created December, 2010
  */
 
 public class RfidClient extends EventListener implements IRfidClient {
 
 	private ICommManager commManager;
 	private Logger logging = LoggerFactory.getLogger(this.getClass());
 	private ICtxSourceMgr ctxSourceMgr;
 	private IIdentityManager idm;
 
 	/* 
 	 * serviceID of RfidClient
 	 */
 	private ServiceResourceIdentifier	clientID;
 
 
 
 	/*
 	 * my LOCAL DPI
 	 */
 	private IIdentity userIdentity;
 
 	private ClientGUIFrame clientGUI;
 
 	private IIdentity serverIdentity;
 	private IRfidServer rfidServerRemote;
 	private String myCtxSourceId;
 	private ICtxBroker ctxBroker;
 	private IEventMgr evMgr;
 	private IServices services;
 	private RequestorService requestor;
 
 
 
 	public RfidClient() {
 		
 	}
 
 
 
 
 	public void close() {
 		this.clientGUI.setVisible(false);
 		this.clientGUI.dispose();
 	}
 
 
 	public void initialiseRFIDClient() {
 		this.registerForSLMEvents();
 	}
 
 	private void registerForSLMEvents() {
 		String eventFilter = "(&" + 
 				"(" + CSSEventConstants.EVENT_NAME + "="+ServiceMgmtEventType.NEW_SERVICE+")" +
 				"(" + CSSEventConstants.EVENT_SOURCE + "=org/societies/servicelifecycle)" +
 				")";
 		this.getEvMgr().subscribeInternalEvent(this, new String[]{EventTypes.SERVICE_LIFECYCLE_EVENT}, eventFilter);
 		this.logging.debug("Subscribed to "+EventTypes.SERVICE_LIFECYCLE_EVENT+" events");
 
 	}
 
 	private void unRegisterFromSLMEvents()
 	{
 		String eventFilter = "(&" + 
 				"(" + CSSEventConstants.EVENT_NAME + "="+ServiceMgmtEventType.NEW_SERVICE+")" +
 				"(" + CSSEventConstants.EVENT_SOURCE + "=org/societies/servicelifecycle)" +
 				")";
 
 		this.evMgr.unSubscribeInternalEvent(this, new String[]{EventTypes.SERVICE_LIFECYCLE_EVENT}, eventFilter);
 		//this.evMgr.subscribeInternalEvent(this, new String[]{EventTypes.SERVICE_LIFECYCLE_EVENT}, eventFilter);
 		this.logging.debug("Unsubscribed from "+EventTypes.SERVICE_LIFECYCLE_EVENT+" events");
 	}
 
 	private boolean register(){
 		try {
 			Future<String> fID = this.ctxSourceMgr.register("RFID", CtxAttributeTypes.LOCATION_SYMBOLIC);
 			myCtxSourceId = fID.get();
 			return true;
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return false;
 		} catch (ExecutionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	@Override
 	public void sendUpdate(String symLoc, String tagNumber) {
 		
 		this.clientGUI.sendSymLocUpdate(tagNumber, symLoc);
 		this.clientGUI.tfTagNumber.setText(tagNumber);
 
 		if (this.myCtxSourceId==null){
 			boolean registered = this.register();
 			if (registered){
 				this.ctxSourceMgr.sendUpdate(this.myCtxSourceId, symLoc);
 				this.logging.debug("Sent new RFID information");
 			}else{
 				this.logging.debug("Received symloc update: "+symLoc+" but unable to register as a context source with the ICtxSourceMgr.");
 			}
 		}else{
 			//this.ctxSourceMgr.sendUpdate(this.myCtxSourceId, symLoc);		
 			
 			this.ctxSourceMgr.sendUpdate(myCtxSourceId, symLoc, null, false, 1.0, 1d/60);
 			this.logging.debug("Sent new RFID information");
 		}
 
 	}
 
 	@Override
 	public void acknowledgeRegistration(Integer rStatus) {
 		this.clientGUI.acknowledgeRegistration(rStatus);
 
 	}
 
 	/**
 	 * @return the commManager
 	 */
 	public ICommManager getCommManager() {
 		return commManager;
 	}
 
 	/**
 	 * @param commManager the commManager to set
 	 */
 	public void setCommManager(ICommManager commManager) {
 		this.commManager = commManager;
 		this.idm = this.commManager.getIdManager();
 		this.userIdentity = this.idm.getThisNetworkNode();
 	}
 
 
 
 
 	/**
 	 * @return the ctxSourceMgr
 	 */
 	public ICtxSourceMgr getCtxSourceMgr() {
 		return ctxSourceMgr;
 	}
 
 
 
 
 	/**
 	 * @param ctxSourceMgr the ctxSourceMgr to set
 	 */
 	public void setCtxSourceMgr(ICtxSourceMgr ctxSourceMgr) {
 		this.ctxSourceMgr = ctxSourceMgr;
 	}
 
 
 
 
 	/**
 	 * @return the rfidServer
 	 */
 	public IRfidServer getRfidServerRemote() {
 		return rfidServerRemote;
 	}
 
 
 
 
 	/**
 	 * @param rfidServer the rfidServer to set
 	 */
 	public void setRfidServerRemote(IRfidServer rfidServer) {
 		this.rfidServerRemote = rfidServer;
 	}
 
 
 
 
 	/**
 	 * @return the ctxBroker
 	 */
 	public ICtxBroker getCtxBroker() {
 		return ctxBroker;
 	}
 
 
 
 
 	/**
 	 * @param ctxBroker the ctxBroker to set
 	 */
 	public void setCtxBroker(ICtxBroker ctxBroker) {
 		this.ctxBroker = ctxBroker;
 	}
 
 
 
 
 	@Override
 	public void handleInternalEvent(InternalEvent event) {
 		ServiceMgmtEvent slmEvent = (ServiceMgmtEvent) event.geteventInfo();
 
 		if (slmEvent.getBundleSymbolName().equalsIgnoreCase("ac.hw.rfid.RFIDClientApp")){
 			this.logging.debug("Received SLM event for my bundle");
 
 			if (slmEvent.getEventType().equals(ServiceMgmtEventType.NEW_SERVICE)){
 				ServiceResourceIdentifier myClientServiceID = slmEvent.getServiceId();
 				this.serverIdentity = getServices().getServer(myClientServiceID);
 				this.logging.debug("Retrieved my server's identity: "+this.serverIdentity.getJid());
 				//this.requestServerIdentityFromUser();
 				//ServiceResourceIdentifier serviceId = this.portalServerRemote.getServerServiceId(serverIdentity);
 
 				//UIManager.put("ClassLoader", ClassLoader.getSystemClassLoader());
 
 				ServiceResourceIdentifier serviceId = this.getServices().getServerServiceIdentifier(myClientServiceID);
 				this.logging.debug("Retrieved my server's serviceID: "+serviceId.getIdentifier().toASCIIString());
 				this.requestor = new RequestorService(serverIdentity, serviceId);
 
 				boolean registered = this.register();
 				if (registered){

 					clientGUI = new ClientGUIFrame(this.rfidServerRemote, this.getCtxBroker(), this.userIdentity, this.serverIdentity, clientID);
 				}else{
 					this.logging.debug("unable to register as a context source with the ICtxSourceMgr");
 				}
 
 				this.unRegisterFromSLMEvents();
 			}
 		}else{
 			this.logging.debug("Received SLM event but it wasn't related to my bundle");
 		}
 
 	}
 
 
 
 
 	@Override
 	public void handleExternalEvent(CSSEvent event) {
 		// TODO Auto-generated method stub
 
 	}
 
 
 
 
 	public IEventMgr getEvMgr() {
 		return evMgr;
 	}
 
 
 
 
 	public void setEvMgr(IEventMgr evMgr) {
 		this.evMgr = evMgr;
 	}
 
 
 
 
 	public IServices getServices() {
 		return services;
 	}
 
 
 
 
 	public void setServices(IServices services) {
 		this.services = services;
 	}
 
 
 
 
 
 
 } 
 
 
 
