 package controllers;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.namespace.QName;
 
 import models.ModelManager;
 import models.eventstream.EventTopic;
 
 import org.apache.cxf.interceptor.LoggingInInterceptor;
 import org.apache.cxf.interceptor.LoggingOutInterceptor;
 import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
 import org.ontoware.rdf2go.model.Model;
 import org.ow2.play.governance.api.EventGovernance;
 import org.ow2.play.governance.api.GovernanceExeption;
 import org.ow2.play.governance.api.bean.Topic;
 import org.ow2.play.metadata.api.Data;
 import org.ow2.play.metadata.api.Metadata;
 import org.ow2.play.metadata.api.MetadataException;
 import org.ow2.play.metadata.client.MetadataClient;
 import org.petalslink.dsb.notification.client.http.simple.HTTPProducerClient;
 import org.petalslink.dsb.notification.client.http.simple.HTTPSubscriptionManagerClient;
 import org.petalslink.dsb.notification.commons.NotificationException;
 
 import play.Logger;
 import play.mvc.Controller;
 import play.mvc.Router;
 import play.mvc.Util;
 import play.utils.HTML;
 
 import com.ebmwebsourcing.wsstar.basefaults.datatypes.impl.impl.WsrfbfModelFactoryImpl;
 import com.ebmwebsourcing.wsstar.basenotification.datatypes.impl.impl.WsnbModelFactoryImpl;
 import com.ebmwebsourcing.wsstar.resource.datatypes.impl.impl.WsrfrModelFactoryImpl;
 import com.ebmwebsourcing.wsstar.resourcelifetime.datatypes.impl.impl.WsrfrlModelFactoryImpl;
 import com.ebmwebsourcing.wsstar.resourceproperties.datatypes.impl.impl.WsrfrpModelFactoryImpl;
 import com.ebmwebsourcing.wsstar.topics.datatypes.impl.impl.WstopModelFactoryImpl;
 import com.ebmwebsourcing.wsstar.wsnb.services.impl.util.Wsnb4ServUtils;
 
 import eu.play_project.play_commons.constants.Constants;
 import eu.play_project.play_commons.constants.Stream;
 import eu.play_project.play_commons.eventformat.EventFormatHelpers;
 import eu.play_project.play_eventadapter.AbstractReceiver;
 
 /**
  * The WebService controller is in charge of SOAP connection with the DSB.
  * 
  * @author Alexandre Bourdin
  * @author Roland Stühmer
  */
 public class WebService extends Controller {
 
 	public static String DSB_NOTIFICATION_PRODUCER_SERVICE = 
 			Constants.getProperties().getProperty("dsb.subscribe.endpoint");
 	public static String EVENT_GOVERNANCE_SERVICE = 
 			Constants.getProperties().getProperty("governance.eventgovernance.endpoint");
 	public static String METADATA_SERVICE = 
 			Constants.getProperties().getProperty("governance.metadataservice.endpoint");
 	private static AbstractReceiver receiver = new AbstractReceiver() {
 	};
 
 	static {
 		Wsnb4ServUtils.initModelFactories(new WsrfbfModelFactoryImpl(),
 				new WsrfrModelFactoryImpl(), new WsrfrlModelFactoryImpl(),
 				new WsrfrpModelFactoryImpl(), new WstopModelFactoryImpl(),
 				new WsnbModelFactoryImpl());
 	}
 
 	/**
 	 * SOAP endpoint to receive WS-Notifications from the DSB.
 	 * 
 	 * @param topicId
 	 *            : necessary to have a unique endpoint for each topic.
 	 */
 	public static void soapNotifEndPoint(String topicId) {
 
 		String notifyMessage;
 
 		// A trick to read a Stream into to String:
 		try {
 			notifyMessage = new java.util.Scanner(request.body).useDelimiter("\\A")
 					.next();
 		} catch (java.util.NoSuchElementException e) {
 			notifyMessage = "";
 		}
 
 		Model rdf;
 		try {
 			/*
 			 * Deal with RDF events:
 			 */
 			rdf = receiver.parseRdf(notifyMessage);
 
 			ModelManager.get().getTopicById(topicId)
 					.multicast(models.eventstream.Event.eventFromRdf(rdf));
 		} catch (Exception e) {
 			/*
 			 * Deal with non-RDF events:
 			 */
 			String eventTitle = "Event";
 			String eventText = HTML
 					.htmlEscape(
 							EventFormatHelpers
 									.unwrapFromNativeMessageElement(notifyMessage))
 					.replaceAll("\n", "<br />")
 					.replaceAll("\\s{4}", "&nbsp;&nbsp;&nbsp;&nbsp;");
 			ModelManager.get().getTopicById(topicId)
 					.multicast(new models.eventstream.Event(eventTitle, eventText));
 		}
 	}
 
 	/**
 	 * Sends a request to the DSB to get the list of supported topics
 	 */
 	@Util
 	public static ArrayList<EventTopic> getSupportedTopics() {
 		Logger.info("Getting topics from Event Governance at %s",
 				EVENT_GOVERNANCE_SERVICE);
 
 		ArrayList<EventTopic> result = new ArrayList<EventTopic>();
 
 		JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
 		factory.getInInterceptors().add(new LoggingInInterceptor());
 		factory.getOutInterceptors().add(new LoggingOutInterceptor());
 		factory.setServiceClass(EventGovernance.class);
 		factory.setAddress(EVENT_GOVERNANCE_SERVICE);
 		EventGovernance eventGovernance = (EventGovernance) factory.create();
 
 		MetadataClient client = new MetadataClient(METADATA_SERVICE);
 
 		try {
 			List<Topic> topics = eventGovernance.getTopics();
 			Logger.info("Number of topics : " + topics.size());
 			for (Topic t : topics) {
 				try {
 					String icon = "/images/noicon.png";
 					String description = "No description available.";
 					String title = EventTopic.createId(t.getPrefix(),
 							t.getName());
 					Logger.info("name:" + t.getName() + " ns:" + t.getNs()
 							+ " prefix:" + t.getPrefix());
 					List<Metadata> metadataList;
 					metadataList = client
 							.getMetaData(new org.ow2.play.metadata.api.Resource(
 									"stream", t.getNs() + t.getName()));
 					for (Metadata m : metadataList) {
 						for (Data d : m.getData()) {
 							if (m.getName().equals(Stream.STREAM_ICON)) {
 								icon = d.getValue();
 							} else if (m.getName().equals(Stream.STREAM_TITLE)) {
 								title = d.getValue();
 							} else if (m.getName().equals(
 									Stream.STREAM_DESCRIPTION)) {
 								description = d.getValue();
 							}
 						}
 					}
 					result.add(new EventTopic(t.getPrefix(), t.getName(), t
 							.getNs(), title, icon, description, ""));
 				} catch (MetadataException e) {
 					Logger.warn(
 							e,
 							"A problem occurred while fetching topic metadata " +
 							"from the Metadata Service. Skipping topic name:" + t.getName());
 				}
 			}
 		} catch (GovernanceExeption e) {
 			Logger.warn(
 					e,
 					"A problem occurred while fetching the list of available topics " +
 					"from the DSB. Continuing with empty set of topics.");
 		}
 		return result;
 	}
 
 	/**
 	 * Subscription action, forwards the subscription to the DSB
 	 * 
 	 * @param et
 	 */
 	@Util
 	public static int subscribe(EventTopic et) {
 		Logger.info("Subscribing to topic '%s%s' at broker '%s'", et.namespace, et.name,
 				DSB_NOTIFICATION_PRODUCER_SERVICE);
 
 		HTTPProducerClient client = new HTTPProducerClient(
 				DSB_NOTIFICATION_PRODUCER_SERVICE);
 		QName topic = new QName(et.namespace, et.name, et.prefix);
 
 		Logger.info("QNAME: %s, %s, %s", topic.getNamespaceURI(), topic.getLocalPart(), topic.getPrefix());
 		
 		Map params = new HashMap<String, Object>();
 		params.put("topicId", et.getId());
 		String notificationsEndPoint = Router.getFullUrl("WebService.soapNotifEndPoint",
 				params);
 		try {
 			et.subscriptionID = client.subscribe(topic, notificationsEndPoint);
 			et.alreadySubscribedDSB = true;
 		} catch (NotificationException e) {
 			Logger.error(e, "Error subscribing to topic %s.", topic);
 			return 0;
 		}
 
 		return 1;
 	}
 
 	/**
 	 * Unsubscription action, forwards the unsubscription to the DSB
 	 * 
 	 * @param et
 	 */
 	@Util
 	public static int unsubscribe(EventTopic et) {
 		HTTPSubscriptionManagerClient subscriptionManagerClient = new HTTPSubscriptionManagerClient(
 				DSB_NOTIFICATION_PRODUCER_SERVICE);
 		try {
 			subscriptionManagerClient.unsubscribe(et.subscriptionID);
 			et.alreadySubscribedDSB = false;
 		} catch (NotificationException e) {
 			e.printStackTrace();
 			return 0;
 		}
 
 		return 1;
 	}
 }
