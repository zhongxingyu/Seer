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
 package org.societies.privacytrust.privacyprotection.privacynegotiation.negotiation.client;
 
 import java.io.IOException;
 import java.io.Serializable;
 import java.security.Key;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 
 import javax.swing.JOptionPane;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.societies.api.context.CtxException;
 import org.societies.api.context.model.CtxAttribute;
 import org.societies.api.context.model.CtxEntity;
 import org.societies.api.context.model.CtxEntityTypes;
 import org.societies.api.context.model.CtxIdentifier;
 import org.societies.api.context.model.CtxModelType;
 import org.societies.api.context.model.IndividualCtxEntity;
 import org.societies.api.context.model.util.SerialisationHelper;
 import org.societies.api.identity.IIdentity;
 import org.societies.api.identity.IIdentityManager;
 import org.societies.api.identity.InvalidFormatException;
 import org.societies.api.identity.Requestor;
 import org.societies.api.identity.RequestorCis;
 import org.societies.api.identity.RequestorService;
 import org.societies.api.internal.context.broker.ICtxBroker;
 import org.societies.api.internal.privacytrust.privacyprotection.INegotiationAgent;
 import org.societies.api.internal.privacytrust.privacyprotection.INegotiationClient;
 import org.societies.api.internal.privacytrust.privacyprotection.model.PrivacyException;
 import org.societies.api.internal.privacytrust.privacyprotection.model.privacypolicy.Action;
 import org.societies.api.internal.privacytrust.privacyprotection.model.privacypolicy.AgreementEnvelope;
 import org.societies.api.internal.privacytrust.privacyprotection.model.privacypolicy.IAgreement;
 import org.societies.api.internal.privacytrust.privacyprotection.model.privacypolicy.IAgreementEnvelope;
 import org.societies.api.internal.privacytrust.privacyprotection.model.privacypolicy.NegotiationAgreement;
 import org.societies.api.internal.privacytrust.privacyprotection.model.privacypolicy.NegotiationStatus;
 import org.societies.api.internal.privacytrust.privacyprotection.model.privacypolicy.PPNegotiationEvent;
 import org.societies.api.internal.privacytrust.privacyprotection.model.privacypolicy.RequestItem;
 import org.societies.api.internal.privacytrust.privacyprotection.model.privacypolicy.RequestPolicy;
 import org.societies.api.internal.privacytrust.privacyprotection.model.privacypolicy.ResponseItem;
 import org.societies.api.internal.privacytrust.privacyprotection.model.privacypolicy.ResponsePolicy;
 import org.societies.api.internal.privacytrust.privacyprotection.model.privacypolicy.constants.ActionConstants;
 import org.societies.api.osgi.event.EMSException;
 import org.societies.api.osgi.event.EventTypes;
 import org.societies.api.osgi.event.IEventMgr;
 import org.societies.api.osgi.event.InternalEvent;
 import org.societies.api.schema.servicelifecycle.model.ServiceResourceIdentifier;
 import org.societies.privacytrust.privacyprotection.api.IPrivacyAgreementManagerInternal;
 import org.societies.privacytrust.privacyprotection.api.IPrivacyDataManagerInternal;
 import org.societies.privacytrust.privacyprotection.api.IPrivacyPreferenceManager;
 import org.societies.privacytrust.privacyprotection.api.identity.IIdentityOption;
 import org.societies.privacytrust.privacyprotection.api.identity.IIdentitySelection;
 import org.societies.privacytrust.privacyprotection.api.model.privacypreference.FailedNegotiationEvent;
 import org.societies.privacytrust.privacyprotection.privacynegotiation.PrivacyPolicyNegotiationManager;
 import org.societies.privacytrust.privacyprotection.privacynegotiation.policyGeneration.client.ClientResponseChecker;
 import org.societies.privacytrust.privacyprotection.privacynegotiation.policyGeneration.client.ClientResponsePolicyGenerator;
 import org.societies.privacytrust.privacyprotection.privacynegotiation.policyGeneration.client.TimedNotificationGUI;
 
 
 
 
 /**
  * Describe your class here...
  *
  * @author Eliza
  *
  */
 public class NegotiationClient implements INegotiationClient {
 
 	private Logger logging = LoggerFactory.getLogger(this.getClass());
 	private final INegotiationAgent negotiationAgent;
 	private RequestPolicy requestPolicy;
 	private ICtxBroker ctxBroker;
 	private IEventMgr eventMgr;
 	private PrivacyPolicyNegotiationManager policyMgr;
 	private Hashtable<Requestor,ResponsePolicy> myPolicies;
 	private Hashtable<Requestor, IAgreement> agreements;
 	private IPrivacyAgreementManagerInternal policyAgreementMgr;
 	private IPrivacyDataManagerInternal privacyDataManager;
 	private IIdentitySelection idS;
 	private IPrivacyPreferenceManager privPrefMgr;
 	private IIdentityManager idm;
	private IIdentity userIdentity;
 
 	public NegotiationClient(INegotiationAgent negotiationAgent, PrivacyPolicyNegotiationManager policyMgr){
 		this.negotiationAgent = negotiationAgent;
 		this.policyMgr = policyMgr;
 		this.ctxBroker = policyMgr.getCtxBroker();
 		this.eventMgr = policyMgr.getEventMgr();
 		this.policyAgreementMgr = policyMgr.getPrivacyAgreementManagerInternal();
 		this.privacyDataManager = policyMgr.getPrivacyDataManagerInternal();
 		this.idS = policyMgr.getIdentitySelection();
 		this.privPrefMgr = policyMgr.getPrivacyPreferenceManager();
 		this.idm = policyMgr.getIdm();
 		this.myPolicies = new Hashtable<Requestor, ResponsePolicy>();
 		this.agreements = new Hashtable<Requestor, IAgreement>();
		this.userIdentity = idm.getThisNetworkNode();
 		
 		
 	}
 	
 	private void log(String message){
 		this.logging.info(this.getClass().getName()+" : "+message);
 	}
 	
 
 	@Override
 	public void receiveProviderPolicy(RequestPolicy policy) {
 		log("Received Provider RequestPolicy!");
 		log(policy.toString());
 		this.requestPolicy = policy;
 		List<String> notFoundTypes = this.contextTypesExist(policy);
 		String str = "";
 		for (String s : notFoundTypes){
 			str = str.concat(s+"\n");
 		}
 		if (notFoundTypes.size()>0){
 			log("Service requires these contextTypes\n"+str+"which don't exist");
 			JOptionPane.showMessageDialog(null, "Service requires these contextTypes\n"+str+"which don't exist", "Error Starting service", JOptionPane.ERROR_MESSAGE);
 			InternalEvent evt = this.createFailedNegotiationEvent(policy.getRequestor());
 			try {
 				this.eventMgr.publishInternalEvent(evt);
 			} catch (EMSException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			return;
 		}
 		ClientResponsePolicyGenerator policyGen = new ClientResponsePolicyGenerator(this.policyMgr);
 		ResponsePolicy response = policyGen.generatePolicy(policy); 
 
 		Future<ResponsePolicy> responsePolicy = this.negotiationAgent.negotiate(policy.getRequestor(), response);
 		this.myPolicies.put(response.getRequestor(), response);
 		try {
 			
 			this.receiveNegotiationResponse(responsePolicy.get());
 			
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 
 		
 	}
 
 	@Override
 	public void receiveNegotiationResponse(ResponsePolicy policy) {
 		log("Received response policy from Provider!");
 		log(policy.toString());
 		if (policy.getStatus().equals(NegotiationStatus.FAILED)){
 			JOptionPane.showMessageDialog(null, "Negotiation Status: "+NegotiationStatus.FAILED+"\n"
 					+"Provider did not accept my terms & conditions");
 			InternalEvent evt = this.createFailedNegotiationEvent(policy.getRequestor());
 			try {
 				this.eventMgr.publishInternalEvent(evt);
 			} catch (EMSException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		else{
 			ResponsePolicy myResponsePolicy = this.findMyResponsePolicy(policy);
 			if (myResponsePolicy == null){
 				new TimedNotificationGUI().showGUI("Ignoring an invalid response policy", "message from: Privacy Policy Negotiation Client");
 				return;
 			}
 			ClientResponseChecker checker = new ClientResponseChecker();
 			if (checker.checkResponse(myResponsePolicy, policy)){
 				IAgreement agreement = new NegotiationAgreement(policy);
				agreement.setUserPublicIdentity(this.userIdentity);
				
				
				
 				
 				this.agreements.put(policy.getRequestor(), agreement);
 				//TODO: select an identity and call setFinalIdentity(..);
 				List<IIdentityOption> idOptions  = this.idS.processIdentityContext(agreement);
 				IIdentity selectedIdentity = this.selectIdentity(idOptions, agreement);
 				//this.privacyCallback.setInitialAgreement(agreement);
 				this.setFinalIdentity(selectedIdentity, policy.getRequestor());
 
 			}else{
 				new TimedNotificationGUI().showGUI("Response policy received from Provider\n"
 						+"did not match user response policy.\n"
 						+"Aborted negotiation","message from: Privacy Policy Negotiation Client");
 				InternalEvent evt = this.createFailedNegotiationEvent(policy.getRequestor());
 				try {
 					this.eventMgr.publishInternalEvent(evt);
 				} catch (EMSException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				return;
 			}
 		}
 		
 	}
 	private IIdentity selectIdentity(List<IIdentityOption> idOptions, IAgreement agreement) {
 		List<IIdentity> identities = new ArrayList<IIdentity>();
 		List<String> strIds = new ArrayList<String>();
 		for (IIdentityOption idOption : idOptions){
 			identities.add(idOption.getReferenceIdentity());
 			strIds.add(idOption.getReferenceIdentity().getJid());
 		}
 		strIds.add("Create new identity");
 		IIdentity selectedIdentity = this.privPrefMgr.evaluateIDSPreferences(agreement, identities);
 		if (selectedIdentity == null){
 			selectedIdentity = idm.newTransientIdentity();
 		}else{
 			String s = (String) JOptionPane.showInputDialog(null, this.getMessage(agreement.getRequestor()), "", JOptionPane.PLAIN_MESSAGE, null, strIds.toArray(), identities.get(0));
 			if (s.equalsIgnoreCase("Create new identity")){
 				this.log("Requesting creation of new transient identity");
 				return this.idm.newTransientIdentity();
 			}else{
 				try {
 					return this.idm.fromJid(s);
 				} catch (InvalidFormatException e) {
 					e.printStackTrace();
 					return null;
 				}
 			}
 		}
 		return selectedIdentity;
 			
 	}
 
 	private String getMessage(Requestor requestor){
 		if (requestor instanceof RequestorCis){
 			return "Please select one of the following identities to join CIS: "+((RequestorCis) requestor).getCisRequestorId();
 		}else if (requestor instanceof RequestorService){
 			return "Please select one of the following identities to use service: "+((RequestorService) requestor).getRequestorServiceId();
 		}else{
 			return "Please select one of the following identities to interact with: "+requestor.getRequestorId();
 		}
 	}
 	public void setFinalIdentity( IIdentity userId, Requestor requestor){
 		try {
 			if (this.agreements.containsKey(requestor)){
 				IAgreement agreement = this.agreements.get(requestor);
 				agreement.setUserIdentity(userId);
 				//agreement.setUserPublicDPI(this.IDM.getPublicDigitalPersonalIdentifier());
 				AgreementFinaliser finaliser = new AgreementFinaliser();
 				byte[] signature = finaliser.signAgreement(agreement);
 				Key publicKey = finaliser.getPublicKey();
 				AgreementEnvelope envelope = new AgreementEnvelope(agreement, SerialisationHelper.serialise(publicKey), signature);
 				
 				Future<Boolean> ack = this.negotiationAgent.acknowledgeAgreement(envelope);
 				if (null==ack){
 					JOptionPane.showMessageDialog(null, "ack is null");
 				}
 				this.acknowledgeAgreement(requestor, envelope, ack.get());
 			}else{
 				log("Agreement not found for requestor: "+requestor.toString()+"\nIs this negotiation process obsolete? Ignoring call to setFinalIdentity()");
 				
 			}
 		}
 		catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	@Override
 	public void acknowledgeAgreement(Requestor requestor, AgreementEnvelope envelope, boolean b) {
 		if (b){
 			log("Acknowledged Agreement - creating access control objects");			
 			try {
 				//TODO: uncomment lines
 				if (requestor instanceof RequestorCis){
 
 					this.policyAgreementMgr.updateAgreement((envelope.getAgreement().getRequestor()), envelope);
 
 				}else{
 					this.policyAgreementMgr.updateAgreement(envelope.getAgreement().getRequestor(), envelope);
 				}
 
 				List<ResponseItem> requests = envelope.getAgreement().getRequestedItems();
 				for (ResponseItem responseItem : requests){
 					privacyDataManager.updatePermission(requestor, envelope.getAgreement().getUserIdentity(), responseItem);
 				}
 				
 				InternalEvent event = this.createSuccessfulNegotiationEvent(envelope);
 				try {
 					this.eventMgr.publishInternalEvent(event);
 				} catch (EMSException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 					this.log("Unable to post "+event.geteventType()+" event");
 				}
 			} catch (PrivacyException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}else{
 			log("DID NOT Acknowledge Agreement");
 			InternalEvent event = this.createFailedNegotiationEvent(requestor);
 			try {
 				this.eventMgr.publishInternalEvent(event);
 			} catch (EMSException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				this.log("Unable to post "+event.geteventType()+" event");
 			}
 		}
 
 	}
 	
 	
 
 	/**
 	 * No need for this step. This should be removed and the negotiation step should commence in the startPrivacyPolicyNegotiation(RequestPolicy,S
 	 * @param requestor
 	 */
 	public void startNegotiation(Requestor requestor){
 		
 		log("Starting negotiation with: "+requestor.toString());
 		try {
 			this.receiveProviderPolicy(this.negotiationAgent.getPolicy(requestor).get());
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ExecutionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 	
 	
 	/**
 	 * 
 	 * 
 	 * 
 	 * HELPER METHODS BELOW
 	 */
 
 	
 	private List<String> contextTypesExist(RequestPolicy policy){
 		List<String> notFoundContextTypes = new ArrayList<String>();
 		List<RequestItem> items = policy.getRequests();
 		for (RequestItem item : items){
 			if (!item.isOptional()){
 				String contextType = item.getResource().getContextType();
 				try {
 					Future<IndividualCtxEntity> futurePerson = ctxBroker.retrieveCssOperator();
 					CtxEntity person = futurePerson.get();
 					Set<CtxAttribute> resultSet = person.getAttributes(contextType);
 					if (resultSet.isEmpty()){
 						boolean containsCreate = false;
 						for (Action a : item.getActions()){
 							if (a.getActionType().equals(ActionConstants.CREATE)){
 								containsCreate = true;
 							}
 						}
 						if (!containsCreate){
 							notFoundContextTypes.add(contextType);
 						}
 					}
 				} catch (CtxException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (ExecutionException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}
 
 
 		return notFoundContextTypes;
 	}
 	
 	
 	private InternalEvent createFailedNegotiationEvent(Requestor requestor){
 		FailedNegotiationEvent event = new FailedNegotiationEvent(requestor);
 		InternalEvent iEvent = new InternalEvent(EventTypes.FAILED_NEGOTIATION_EVENT, "", INegotiationClient.class.getName(), event);
 		return iEvent;
 	}
 
 	private InternalEvent createSuccessfulNegotiationEvent(AgreementEnvelope envelope) {
 		PPNegotiationEvent event = new PPNegotiationEvent(envelope.getAgreement(), NegotiationStatus.SUCCESSFUL);
 		InternalEvent iEvent = new InternalEvent(EventTypes.PRIVACY_POLICY_NEGOTIATION_EVENT, "", INegotiationClient.class.getName(), event);
 		return iEvent;
 	}
 	private ResponsePolicy findMyResponsePolicy(ResponsePolicy providerPolicy){
 		Requestor requestor = providerPolicy.getRequestor();
 		if (this.myPolicies.containsKey(requestor)){
 			return this.myPolicies.get(requestor);
 		}
 
 		Enumeration<Requestor> en = this.myPolicies.keys();
 
 		while (en.hasMoreElements()){
 			Requestor provider = en.nextElement();
 			if ((provider instanceof RequestorService) && (requestor instanceof RequestorService)){
 				if (provider.getRequestorId().equals(requestor.getRequestorId())){
 					ServiceResourceIdentifier serviceID = ((RequestorService) provider).getRequestorServiceId();
 					if (serviceID!=null){
 						if (serviceID.equals(((RequestorService) requestor).getRequestorServiceId())){
 							return this.myPolicies.get(provider);
 						}
 					}
 				}
 			}else if ((provider instanceof RequestorCis) && (requestor instanceof RequestorCis)){
 				if (provider.getRequestorId().equals(requestor.getRequestorId())){
 					IIdentity cisId = ((RequestorCis) provider).getCisRequestorId();
 					if (cisId!=null){
 						if (cisId.equals(((RequestorCis) requestor).getCisRequestorId())){
 							return this.myPolicies.get(provider);
 						}
 					}
 				}
 			}
 		}
 
 		return null;
 	}
 
 
 	@Override
 	public void startPrivacyPolicyNegotiation(Requestor requestor, RequestPolicy policy) {
 		// TODO Auto-generated method stub
 		
 	}
 
 
 }
