 /**
  * Copyright (c) 2009 EBM Websourcing, http://www.ebmwebsourcing.com/
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * -------------------------------------------------------------------------
  * $Id$
  * -------------------------------------------------------------------------
  */
 package com.ebmwebsourcing.wsstar.wsnb.services.impl.engines;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.xml.bind.JAXBElement;
 import javax.xml.datatype.Duration;
 import javax.xml.namespace.QName;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 
 import com.ebmwebsourcing.wsaddressing10.api.type.EndpointReferenceType;
 import com.ebmwebsourcing.wsstar.basefaults.datatypes.api.utils.WsrfbfException;
 import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.WsnbConstants;
 import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.FilterType;
 import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.Renew;
 import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.RenewResponse;
 import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.Subscribe;
 import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.SubscriptionManagerRP;
 import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.SubscriptionPolicyType;
 import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.TopicExpressionType;
 import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.Unsubscribe;
 import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.UnsubscribeResponse;
 import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.refinedabstraction.RefinedWsnbFactory;
 import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.utils.WsnbException;
 import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.utils.WsnbFaultMessageContentConstants;
 import com.ebmwebsourcing.wsstar.resource.datatypes.api.utils.WsrfrException;
 import com.ebmwebsourcing.wsstar.resourcelifetime.datatypes.api.abstraction.SetTerminationTime;
 import com.ebmwebsourcing.wsstar.resourcelifetime.datatypes.api.abstraction.SetTerminationTimeResponse;
 import com.ebmwebsourcing.wsstar.resourcelifetime.datatypes.api.refinedabstraction.RefinedWsrfrlFactory;
 import com.ebmwebsourcing.wsstar.resourcelifetime.datatypes.api.utils.WsrfrlException;
 import com.ebmwebsourcing.wsstar.topics.datatypes.api.WstopConstants;
 import com.ebmwebsourcing.wsstar.wsnb.services.ISubscriptionManager;
 import com.ebmwebsourcing.wsstar.wsnb.services.faults.UnableToDestroySubscriptionFault;
 import com.ebmwebsourcing.wsstar.wsnb.services.impl.persistence.WsnbPersistence;
 import com.ebmwebsourcing.wsstar.wsnb.services.impl.topic.TopicsManagerEngine;
 import com.ebmwebsourcing.wsstar.wsnb.services.impl.wsresources.WsnSubscription;
 import com.ebmwebsourcing.wsstar.wsnb.services.transport.ITransporterForWsnbPublisher;
 import com.ebmwebsourcing.wsstar.wsrfbf.services.faults.AbsWSStarFault;
 import com.ebmwebsourcing.wsstar.wsrfr.services.faults.ResourceUnknownFault;
 
 import easybox.org.w3._2005._08.addressing.EJaxbEndpointReferenceType;
 
 public class SubscriptionManagerEngine implements ISubscriptionManager {	
 
 	protected Logger logger;
 	protected Map<String, WsnSubscription> subscriptions;
 	protected Map<QName, List<String>> uuidsPerTopics; 
 
 	protected WsnbPersistence persistenceMgr;
 
 	protected String subscriptionsManagerEdp = "";//"http://www.ebmwebsourcing.com/subscriptionManager/default";
 	protected QName subscriptionsManagerService = null;//new QName("http://www.ebmwebsourcing.com/default","SubscriptionManagerService");
 	protected QName subscriptionsManagerInterface = null;//new QName("http://www.ebmwebsourcing.com/default","SubscriptionManager");	
 
 	// FIXME : This is really bad since it is not synchronized at all, this field is unuseful and MUST be removed!
 	protected String targetSubscriptionResourceUuid = "";
 
 	protected ITransporterForWsnbPublisher notificationSender = null;
 
 	public SubscriptionManagerEngine(Logger logger, WsnbPersistence persistenceMgr, ITransporterForWsnbPublisher transporter) {
 		super();
 		this.logger = logger;
 		this.subscriptions = new ConcurrentHashMap<String, WsnSubscription>();		
 		this.uuidsPerTopics = new ConcurrentHashMap<QName, List<String>>();
 
 		this.persistenceMgr = persistenceMgr ;
 
 		this.notificationSender = transporter;
 
 	}
 
 	public SubscriptionManagerEngine(Logger logger) {
 		super();
 		this.logger = logger;
 
 		this.subscriptions = new ConcurrentHashMap<String, WsnSubscription>();
 		this.uuidsPerTopics = new ConcurrentHashMap<QName, List<String>>();
 
 		this.persistenceMgr = null;
 
 	}
 	// ##################################################
 	//  		----- {Getter,Setter} methods ------
 	// ##################################################
 
 	/**
 	 * 
 	 * @return
 	 */
 	public String getSubscriptionsManagerEdp() {
 		return subscriptionsManagerEdp;
 	}
 
 	public void setSubscriptionsManagerEdp(String subscriptionsMgrEdp) {
 		this.subscriptionsManagerEdp = subscriptionsMgrEdp;
 	}
 
 	public QName getSubscriptionsManagerService() {
 		return subscriptionsManagerService;
 	}
 
 	public void setSubscriptionsManagerService(QName subscriptionsMgrService) {
 		this.subscriptionsManagerService = subscriptionsMgrService;
 	}
 
 	public QName getSubscriptionsManagerInterface() {
 		return (this.subscriptionsManagerInterface!=null)?
 				this.subscriptionsManagerInterface : WsnbConstants.SUBSCRIPTION_MANAGER_INTERFACE;
 	}
 
 	public void setSubscriptionsManagerInterface(QName subscriptionsMgrInterface) {
 		this.subscriptionsManagerInterface = subscriptionsMgrInterface;
 	}
 
 	public List<String> getStoredSubscriptionUuids(){
 		return new CopyOnWriteArrayList<String>(this.subscriptions.keySet());
 	}
 
 	public void setTargetSubscriptionResourceUuid(String subscriptionUuid) {
 		this.targetSubscriptionResourceUuid = subscriptionUuid;
 	}
 
 	public ITransporterForWsnbPublisher getNotificationSender() {
 		return notificationSender;
 	}
 
 	public void setNotificationSender(
 			ITransporterForWsnbPublisher notificationSender) {
 		this.notificationSender = notificationSender;
 	}
 
 	// #####################################################################################
 	// 	----- Methods' implementation of WS-Notification SubscriptionManager Interface ----
 	// #####################################################################################
 
 	private void throwSubscriptionUuidNotSetException(String methodName) throws WsnbException, ResourceUnknownFault {
 		this.logger.log(Level.WARNING, "The target Subscription Uuid value is not set.\n" +
 				"You must first call \"SubscriptionManagerEngine.setTargetSubscriptionResourceUuid(String uuid)\" method before \""+ methodName +
 				"(...)\" method !!");
 		try {
 			throw new ResourceUnknownFault(WsnbFaultMessageContentConstants.FAULT_DESCRIPTION_LANGUAGE,
 					WsnbFaultMessageContentConstants.WsnbUnsubscribeFaultDescriptions.RESOURCE_UNKNOWN_FAULT_DESC);
 		} catch (WsrfrException e) {
 			throw new WsnbException(e);
 		} catch (WsrfbfException e) {
 			throw new WsnbException(e);
 		}		
 	}
 
 
 	/*
 	 * (non-Javadoc)
 	 * @see com.ebmwebsourcing.wsstar.wsnb.services.ISubscriptionManager#unsubscribe(com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.Unsubscribe)
 	 */
 	public UnsubscribeResponse unsubscribe(Unsubscribe payload)
 			throws WsnbException, AbsWSStarFault {
 		logger.log(Level.FINE, "performs a \"Unsubscribe\" request ...");
 
 		try {
 			if (this.targetSubscriptionResourceUuid == null || this.targetSubscriptionResourceUuid.equals("")){
 				if(payload.getAny().size() > 0) {
 					if(payload.getAny().get(0) instanceof JAXBElement) {
 						JAXBElement<?> eprElmt = (JAXBElement<?>) payload.getAny().get(0);
 						if(eprElmt.getValue() instanceof EJaxbEndpointReferenceType) {
 							EJaxbEndpointReferenceType epr = (EJaxbEndpointReferenceType) eprElmt.getValue();
 							Element subscriptionResourceUuidElmt = (Element) epr.getReferenceParameters().getAny().get(0);
 							String subscriptionResourceUuid = subscriptionResourceUuidElmt.getFirstChild().getNodeValue();
 							this.targetSubscriptionResourceUuid = subscriptionResourceUuid;
 						}
 					} else {
 						if (payload.getAny().get(0) instanceof Element) {
 							Element element = (Element) payload.getAny().get(0);
 				            if (element.getLocalName().equals(WsnbConstants.SUBSCRIPTION_ID_QNAME_TAG.getLocalPart()) &&
 				            		element.getNamespaceURI().equals(WsnbConstants.SUBSCRIPTION_ID_QNAME_TAG.getNamespaceURI())){
 				            	this.targetSubscriptionResourceUuid = element.getTextContent();
 				            }
 				        }		
 					}
 				}
 			}
 
 			if (this.targetSubscriptionResourceUuid == null || this.targetSubscriptionResourceUuid.equals("")){
 				this.throwSubscriptionUuidNotSetException("unsubscribe");
 			}
 
 			WsnSubscription wsnSubscription = null;
 
 			// ---- perform a subscription destruction ---
 			if  (subscriptions.containsKey(this.targetSubscriptionResourceUuid)){
 				wsnSubscription = subscriptions.remove(this.targetSubscriptionResourceUuid);
 				if (subscriptions.containsKey(this.targetSubscriptionResourceUuid)){
 					try {
 						throw new UnableToDestroySubscriptionFault(WsnbFaultMessageContentConstants.FAULT_DESCRIPTION_LANGUAGE,
 								WsnbFaultMessageContentConstants.WsnbUnsubscribeFaultDescriptions.UNABLE_TO_DESTROY_SUBSCRIPTION_FAULT_DESC);
 					} catch (WsrfbfException e) {
 						throw new WsnbException(e);
 					}
 				} else {							
 					this.removeUuidFromUuidsPerTopic(this.targetSubscriptionResourceUuid);
 				}
 				if (this.persistenceMgr != null){
 					this.persistenceMgr.removeSubscription(this.targetSubscriptionResourceUuid);
 				}
 
 			} else {
 				try {
 					throw new ResourceUnknownFault(WsnbFaultMessageContentConstants.FAULT_DESCRIPTION_LANGUAGE,
 							WsnbFaultMessageContentConstants.WsnbUnsubscribeFaultDescriptions.RESOURCE_UNKNOWN_FAULT_DESC);
 				} catch (WsrfrException e) {
 					throw new WsnbException(e);
 				} catch (WsrfbfException e) {
 					throw new WsnbException(e);
 				}
 			}
 
 			// call resource "destroy" operation 
 
 			wsnSubscription.destroy(RefinedWsrfrlFactory.getInstance().createDestroy());
 		} catch (WsrfrlException e) {
 			throw new WsnbException(e);
 		} 
 		//		catch (ParserConfigurationException e) {
 		//			throw new WsnbException(e);
 		//		} catch (XmlObjectReadException e) {
 		//			throw new WsnbException(e);
 		//		}
 
 		// ---- /!\ IMPORTANT STEP : reset "targetSubscriptionResourceUuid" attribut's value
 		this.targetSubscriptionResourceUuid = "";
 
 		// ---- build and return a default "UnsubscribeResponse" object		
 
 		return RefinedWsnbFactory.getInstance().createUnsubscribeResponse();
 	}
 
 	/**
 	 * remove Subscription uuid from uuidsPerTopics map 
 	 * 
 	 * @param subscriptionUuid subscription uuid to remove 
 	 */
 	private void removeUuidFromUuidsPerTopic(String subscriptionUuid) {
 		List<String> currentUuids = null;
 		for (Iterator<List<String>> iterator = (this.uuidsPerTopics.values()).iterator(); iterator.hasNext();) {
 			currentUuids = iterator.next();
 			currentUuids.remove(subscriptionUuid);			
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * @see com.ebmwebsourcing.wsstar.wsnb.services.ISubscriptionManager#renew(com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.Renew)
 	 */
 	public RenewResponse renew(Renew payload) throws WsnbException,
 	AbsWSStarFault {	
 		logger.log(Level.FINE, "performs a \"Renew\" request ...");
 
 		if (this.targetSubscriptionResourceUuid == null || this.targetSubscriptionResourceUuid.equals("")){
 			this.throwSubscriptionUuidNotSetException("renew");
 		}
 
 		RenewResponse response = null;
 
 		WsnSubscription subscription = this.subscriptions.get(this.targetSubscriptionResourceUuid);
 
 		if (subscription == null){
 			try {
 				throw new ResourceUnknownFault(WsnbFaultMessageContentConstants.FAULT_DESCRIPTION_LANGUAGE,
 						WsnbFaultMessageContentConstants.WsnbRenewFaultDescriptions.RESOURCE_UNKNOWN_FAULT_DESC);
 			} catch (WsrfrException e) {
 				throw new WsnbException(e);
 			} catch (WsrfbfException e) {
 				throw new WsnbException(e);
 			}
 		}			
 
 		Object requestedTerminationTime = payload.getTerminationTime();
 
 		SetTerminationTime payloadForResource = null;
 		try {
 			if (requestedTerminationTime instanceof Duration){
 				payloadForResource = RefinedWsrfrlFactory.getInstance().createSetTerminationTime((Duration)requestedTerminationTime);
 			} else { 
 				payloadForResource = RefinedWsrfrlFactory.getInstance().createSetTerminationTime((Date)requestedTerminationTime);
 			}
 		} catch (WsrfrlException e) {
 			throw new WsnbException(e);
 		}
 
 		SetTerminationTimeResponse responseFromResource;
 		try {
 			responseFromResource = subscription.setTerminationTime(payloadForResource);
 		} catch (WsrfrlException e) {
 			throw new WsnbException(e);
 		}
 
 		response = RefinedWsnbFactory.getInstance().createRenewResponse(responseFromResource.getNewTerminationTime());
 		response.setCurrentTime(responseFromResource.getCurrentTime());
 
 		/*
 		if (this.persistenceMgr != null)			
 			this.persistenceMgr.persist(subscription, this.targetSubscriptionResourceUuid);
 		 */
 
 		// ---- /!\ IMPORTANT STEP : reset "targetSubscriptionResourceUuid" attribut's value
 		this.targetSubscriptionResourceUuid = "";
 
 		return response;
 	}
 
 	// ###############################################
 	// 			---- Others methods ----
 	// ###############################################
 
 	public EndpointReferenceType createAndStoreSubscriptionResource(String subscriptionId, List<TopicExpressionType> topics,Subscribe payload) 
 			throws WsnbException,AbsWSStarFault {
 
 		// -- Create Subscription resource to manage 
 		WsnSubscription subscription = null;
 
 		try {
 			URI address = new URI(this.subscriptionsManagerService.getNamespaceURI()+"::"+
 					this.subscriptionsManagerService.getLocalPart()+"@"+ this.subscriptionsManagerEdp);
 			subscription = new WsnSubscription(this.logger, address, subscriptionId, payload, this.notificationSender);
 
 			// --- store related topics as list of "Concrete" TopicExpressions
 			// --- used when "NotificationProducer" "topicSet" resource properties is modified
 			subscription.setAssociatedTopicExprs(topics);
 
 		} catch (URISyntaxException e) {
 			throw new WsnbException(e);
 		} 
 
 		this.subscriptions.put(subscriptionId, subscription);
 
 		if (this.persistenceMgr != null){			
 			this.persistenceMgr.persist(subscription.getSubscriptionResource(), subscriptionId);			
 		}
 
 		// ---- store uuid into "uuidsPerTopics" map ----
 		this.storeUuidInUuidsPerTopicsList(subscriptionId, topics);
 		// ----------------------------------------------
 
 		return subscription.getSubscriptionReference();
 	}
 
 	/**
 	 *  store uuid in uuidsPerTopics map -----------
 	 *	It will be used to get subscriptions -------
 	 *	related to a given topic (concreteTopicExpression)
 	 *
 	 * @param subscriptionId
 	 * @param topics
 	 */
 	private void storeUuidInUuidsPerTopicsList(String subscriptionId,
 			List<TopicExpressionType> topics) {
 
 		QName currentTopic = null;
 		List<String> currentUuids = null;
 
 		for (TopicExpressionType topicItem : topics) {
 			if(topicItem.getTopicNamespaces().size() > 0) {
 				currentTopic = new QName(topicItem.getTopicNamespaces().get(0).getNamespaceURI(),
 						topicItem.getContent());
 			} else {
 				currentTopic = new QName(topicItem.getContent());
 			}
 
 			if (this.uuidsPerTopics.containsKey(currentTopic)){
 				currentUuids =  this.uuidsPerTopics.get(currentTopic);
 				if (!currentUuids.contains(subscriptionId)){
 					currentUuids.add(subscriptionId);
 				}
 			} else {			
 				List<String> newUuidsList = new CopyOnWriteArrayList<String>();
 				newUuidsList.add(subscriptionId);
 				this.uuidsPerTopics.put(currentTopic,newUuidsList);
 			}					
 		}		
 	}
 
 	/**
 	 * 
 	 * @return
 	 * @throws WsnbException
 	 */
 	public List<String> removeAllSubscription() throws WsnbException {
 
 		List<String> result = new ArrayList<String>();	
 
 		result.addAll(subscriptions.keySet());
 
 		for (String subscriptionUuidItem : subscriptions.keySet()) {		
 			if (this.persistenceMgr != null){
 				this.persistenceMgr.removeSubscription(subscriptionUuidItem);
 			}
 		} 
 
 		this.subscriptions.clear();
 		this.uuidsPerTopics.clear();
 
 		return result;
 	}
 
 	public EndpointReferenceType getConsumerEdpRefOfSubscription(String subscriptionId) throws WsnbException{
 
 		EndpointReferenceType consumerEdp = null;
 		WsnSubscription subsResource = this.subscriptions.get(subscriptionId);
 		if (subsResource != null)        
 			consumerEdp = subsResource.getConsumerEdpRef();
 		return consumerEdp;
 	}
 
 	public EndpointReferenceType getSubscriptionRef(String subscriptionId) throws WsnbException{
 
 		EndpointReferenceType subsRef = null;
 		WsnSubscription subsResource = this.subscriptions.get(subscriptionId);
 		if (subsResource != null)        
 			subsRef = subsResource.getSubscriptionReference();
 		return subsRef;
 	}
 
 	public List<TopicExpressionType> getTopicExpressionOfSubscription(String subscriptionId) throws WsnbException{
 
 		List<TopicExpressionType> topExprList = null;
 		WsnSubscription subsResource = this.subscriptions.get(subscriptionId);
 		if (subsResource != null)
 			topExprList = subsResource.getTopicExpressionOfSubscription();
 		return topExprList;
 	}
 
 	public FilterType getFilterOfSubscription(String subscriptionId) throws WsnbException{
 
 		FilterType filter = null;
 
 		WsnSubscription subsResource = this.subscriptions.get(subscriptionId);
 		if (subsResource != null)
 			filter = subsResource.getFilterOfSubscription();
 		return filter;
 	}
 
 	public SubscriptionPolicyType getPolicyOfSubscription(String subscriptionId) throws WsnbException {
 
 		SubscriptionPolicyType policy = null;
 		WsnSubscription subsResource = this.subscriptions.get(subscriptionId);
 		if (subsResource != null)
 			policy = subsResource.getPolicyOfSubscription();
 		return policy;		
 	}
 
 	public Date getTerminationTimeOfSubscription(String subscriptionId) throws WsnbException{	
 
 		Date terminationTime = null;
 		WsnSubscription subsResource = this.subscriptions.get(subscriptionId);
 		if (subsResource != null && subsResource.getTerminationTime()!= null)
 			terminationTime = subsResource.getTerminationTime().getValue();
 
 		return terminationTime;
 	}
 
 	public void removeExpiredSubscription(String subscriptionId) throws WsnbException{
 
 		if  (subscriptions.containsKey(subscriptionId)){			
 			subscriptions.remove(subscriptionId);							
 			this.removeUuidFromUuidsPerTopic(subscriptionId);			
 			if (this.persistenceMgr != null){
 				this.persistenceMgr.removeSubscription(subscriptionId);
 			}
 		} 
 	}
 
 	public void restorePersistedSubscriptions(TopicsManagerEngine topicsMgr, Document supportedTopicsAsDOM) throws WsnbException, AbsWSStarFault{		
 
 		if (this.persistenceMgr != null){
 			Map<String, SubscriptionManagerRP> subsRPToRestore = this.persistenceMgr.getSubscriptionsToRestore();
 
 			Set<String> rIds = subsRPToRestore.keySet();
 
 			SubscriptionManagerRP currentSubsRP = null;
 			URI wsaAddress = null;
 			List<TopicExpressionType> topExprList = null;
 			WsnSubscription currentWSSubs = null;
 			List<TopicExpressionType> currentConcreteTopicExprs = null;
 
 			for (String idItem : rIds) {		
 
 				try {
 					wsaAddress = new URI(this.subscriptionsManagerService.getNamespaceURI()+"::"+
 							this.subscriptionsManagerService.getLocalPart()+"@"+ this.subscriptionsManagerEdp);
 				} catch (URISyntaxException e) {
 					throw new WsnbException(e);
 				}
 
 				currentSubsRP = subsRPToRestore.get(idItem);
 				currentWSSubs = new WsnSubscription(this.logger,wsaAddress,idItem,currentSubsRP);
 
 				topExprList = currentSubsRP.getFilter().getTopicExpressions();
 
 				currentConcreteTopicExprs = new CopyOnWriteArrayList<TopicExpressionType>();
 				for (TopicExpressionType topExprItem : topExprList) {				
 					currentConcreteTopicExprs.addAll(topicsMgr.getTopicsAsConcreteTopExpr(topExprItem,supportedTopicsAsDOM));					
 				}	
 				currentWSSubs.setAssociatedTopicExprs(currentConcreteTopicExprs);
 
 				this.subscriptions.put(idItem, currentWSSubs);
 
 				this.storeUuidInUuidsPerTopicsList(idItem, currentConcreteTopicExprs);
 
 			}						
 		}		
 	}
 
 	//	/**
 	//	 * Method which can be used to extract the SubscriptionId
 	//	 * from the WS-Addressing EndpointReference that identify
 	//	 * the subscription resource (seen as a WS-Resource) 
 	//	 * 
 	//	 * @param subscriptionRef
 	//	 * @return
 	//	 * @throws WsaException 
 	//	 */
 	//	private String getSubscriptionUuid(EndpointReferenceType subscriptionRef) throws WsaException{
 	//		String uuidAsString = null;
 	//		
 	//		ReferenceParametersType referenceParams = subscriptionRef.getReferenceParameters();
 	//		if (referenceParams != null){
 	//			uuidAsString = Wsnb4ServUtils.getSubscriptionIdFromReferenceParams(referenceParams);
 	//		}		
 	//		return uuidAsString;
 	//	}
 
 	/**
 	 * look for subscription which topics are no longer supported    
 	 * 
 	 * @return list of related subscription uuids
 	 * @throws WsnbException 
 	 */
 	public List<String> lookForSubscriptionToTerminate(Document topicSetAsDOM,TopicsManagerEngine wstopEngine) throws WsnbException{
 		List<String> uuids = new CopyOnWriteArrayList<String>();
 
 		TopicExpressionType currentConcreteTopExpr = null;
 		int currentPrefixIndex = -1;
 		String currentLocalPart = null;
 
 		WsnSubscription currentSubscriptionToUpdate = null;
 		// --- for each topic - "concrete" topic expression - with
 		// --- some active subscriptions, check if it is still supported 		
 		for (QName keyItem : this.uuidsPerTopics.keySet()) {
 			currentLocalPart = keyItem.getLocalPart();
 			currentPrefixIndex = currentLocalPart.indexOf(":");
 
 			currentConcreteTopExpr = 
 					RefinedWsnbFactory.getInstance().createTopicExpressionType(WstopConstants.CONCRETE_TOPIC_EXPRESSION_DIALECT_URI);
 
 			try {
 				if(currentPrefixIndex > 0) {
 					currentConcreteTopExpr.addTopicNamespace(currentLocalPart.substring(0, currentPrefixIndex),
 							new URI(keyItem.getNamespaceURI()));
 				}
 			} catch (URISyntaxException e) {
 				throw new WsnbException(e);
 			}
 			currentConcreteTopExpr.setContent(currentLocalPart);
 
 			// --- if topic no longer supported then
 
 			if (!wstopEngine.isSupportedTopic(currentConcreteTopExpr, topicSetAsDOM)){
 				List<String> subscriptionUuidsToCheck =  this.uuidsPerTopics.get(keyItem);				
 				for (String uuidItem : subscriptionUuidsToCheck){
 
 					currentSubscriptionToUpdate = this.subscriptions.get(uuidItem);
 					// --- remove it from each resource's related topic list
 					// --- and tag the resource "to be removed" as soon as
 					// --- the list becomes empty
					if (!(currentSubscriptionToUpdate.removeAssociatedTopicExpr(currentConcreteTopExpr)>0) &&
 							!uuids.contains(uuidItem)){
 						uuids.add(uuidItem);
 					}
 				}								
 			}			
 		}
 		return uuids;
 	}
 
 	public void terminateSubscription(String subscriptionUuid) throws WsnbException, AbsWSStarFault {
 
 		WsnSubscription subscription = this.subscriptions.remove(subscriptionUuid);
 
 		// --- remove subscriptionUuid from "uuidsPerTopics" Map
 		List<TopicExpressionType> concreteTopic = subscription.getAssociatedTopicExprs();
 		List<String> currentUuids = null;
 		for (TopicExpressionType concreteTopicItem : concreteTopic) {
 			currentUuids = this.uuidsPerTopics.get(concreteTopicItem);
 			if (currentUuids != null){
 				currentUuids.remove(subscriptionUuid);
 			}			
 		}		
 
 		// --- invoke "Destroy" method of the resource (WS-ResourceLifetime)
 		// --- Note : a Termination notification will be sent to the subscriber !!
 		try {
 			subscription.destroy(RefinedWsrfrlFactory.getInstance().createDestroy());
 		} catch (WsrfrlException e) {
 			throw new WsnbException(e);
 		}
 
 		// --- release the reference of the "WsnSubscription" object
 		subscription = null;
 
 	}	
 	
 	/**
 	 * Delete all the resources for a givent topic. This is a fix since
 	 * subscriptions are not removed when topics are updated...
 	 * CHA
 	 * 
 	 * @param topic to delete
 	 */
 	public void deleteAllForTopic(QName topic) {
 		// get all the subscriptions for the topic
 		List<String> ids = this.uuidsPerTopics.get(topic);
 
 		for (String subscriptionUuidItem : ids) {
 			try {
 				this.removeExpiredSubscription(subscriptionUuidItem);
 			} catch (WsnbException e) {
 				e.printStackTrace();
 			}
 		}
 
 		// delete the map entry
 		this.uuidsPerTopics.remove(topic);
 	}
 }
 
 
