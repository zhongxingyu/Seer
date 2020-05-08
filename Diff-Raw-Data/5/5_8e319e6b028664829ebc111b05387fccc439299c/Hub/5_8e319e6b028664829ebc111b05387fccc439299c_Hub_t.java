 package org.ow2.chameleon.rose.pubsubhubbub.hub;
 
 import static org.osgi.framework.FrameworkUtil.createFilter;
 
 import java.io.IOException;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.Dictionary;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Map;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.felix.ipojo.ConfigurationException;
 import org.apache.felix.ipojo.Factory;
 import org.apache.felix.ipojo.MissingHandlerException;
 import org.apache.felix.ipojo.UnacceptableConfiguration;
 import org.apache.felix.ipojo.annotations.Component;
 import org.apache.felix.ipojo.annotations.Property;
 import org.apache.felix.ipojo.annotations.Requires;
 import org.apache.felix.ipojo.annotations.Validate;
 import org.apache.http.HttpStatus;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.Constants;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceReference;
 import org.osgi.service.http.HttpService;
 import org.osgi.service.http.NamespaceException;
 import org.osgi.service.log.LogService;
 import org.osgi.service.remoteserviceadmin.EndpointDescription;
 import org.osgi.service.remoteserviceadmin.RemoteConstants;
 import org.osgi.util.tracker.ServiceTracker;
 import org.osgi.util.tracker.ServiceTrackerCustomizer;
 import org.ow2.chameleon.json.JSONService;
 import org.ow2.chameleon.syndication.FeedEntry;
 import org.ow2.chameleon.syndication.FeedReader;
 
 @Component(name = "Rose_Pubsubhubbub.hub")
 public class Hub extends HttpServlet {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -1526708334275691196L;
 
 	private static final String FEED_READER_FACTORY_FILTER = "(&("
 			+ Constants.OBJECTCLASS
 			+ "=org.apache.felix.ipojo.Factory)(factory.name=org.ow2.chameleon.syndication.rome.reader))";
 
 	private static final String READER_SERVICE_CLASS = "org.ow2.chameleon.syndication.FeedReader";
 	private static final String READER_FILTER_PROPERTY = "org.ow2.chameleon.syndication.feed.url";
 
 	private static final String ENDPOINT_ADD = "endpoint.add";
 	private static final String ENDPOINT_REMOVE = "endpoint.remove";
 
 	private static final String TOPIC_DELETE = "topic.delete";
 	
 	@Requires
 	private HttpService httpService;
 
 	@Requires
 	private JSONService json;
 
 	@Requires(optional = true)
 	private LogService logger;
 
 	@Property(name="hub.url")
 	private String hubServlet = "/hub";
 	
 	
 	
 	// Http response status code
 	int responseCode;
 	// store instances of RSS reader for different topics
 	private Map<String, FeedReader> readers;
 	private ServiceTracker feedReaderTracker;
 	private ServiceTracker factoryTracker;
 	private BundleContext context;
 	private Dictionary<String, Object> instanceDictionary;
 
 	private static Registrations registrations;
 	private SendSubscription sendSubscription;
 
 
 
 	public enum HubMode {
 		publish, unpublish, update, subscribe, unsubscribe, getAllEndpoints;
 
 		public Object getValue(Map<String, Object> values) {
 			return values.get(this.toString());
 		}
 	}
 
 	public Hub(BundleContext context) {
 		this.context = context;
 	}
 
 	@Validate
 	void start() {
 		try {
 			httpService.registerServlet(hubServlet, this, null, null);
 			registrations = new Registrations();
 			readers = new HashMap<String, FeedReader>();
 
 		} catch (ServletException e) {
 			e.printStackTrace();
 		} catch (NamespaceException e) {
 			e.printStackTrace();
 		}
 	}
 
 	void stop() {
 		httpService.unregister(hubServlet);
 	}
 
 	private boolean createReader(String rssUrl) {
 		try {
 			new FeedReaderTracker(rssUrl);
 			new FactoryTracker(rssUrl);
 			return true;
 		} catch (InvalidSyntaxException e) {
 			logger.log(LogService.LOG_ERROR, "Tracker not stared", e);
 		}
 		return false;
 	}
 
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 
 		String rssUrl;
 		String endpointFilter;
 		String callBackUrl;
 
 		// check the content type, must be application/x-www-form-urlencoded
 		if ((!(req.getHeader("Content-Type")
 				.equals("application/x-www-form-urlencoded")))
 				|| (req.getParameter("hub.mode") == null)) {
 			resp.setStatus(HttpStatus.SC_BAD_REQUEST);
 			return;
 		}
 		rssUrl = req.getParameter("hub.topic");
 		endpointFilter = req.getParameter("hub.endp.filter");
 		callBackUrl = req.getParameter("hub.callback");
 		// check the hub mode
 		switch (HubMode.valueOf(req.getParameter("hub.mode"))) {
 		case publish:
 
 			if ((rssUrl != null) && (createReader(rssUrl))) {
 				// register a topic
 				registrations.addTopic(rssUrl);
 				responseCode = HttpStatus.SC_CREATED;
 			} else {
 				responseCode = HttpStatus.SC_BAD_REQUEST;
 			}
 			break;
 
 		case unpublish:
 
 			if (rssUrl != null) {
 				// remove a topic
 				sendSubscription = new SendSubscription(rssUrl, ENDPOINT_REMOVE,
 						this,TOPIC_DELETE);
 				sendSubscription.start();
 				
 				responseCode = HttpStatus.SC_ACCEPTED;
 			} else {
 				responseCode = HttpStatus.SC_BAD_REQUEST;
 			}
 			break;
 
 		case update:
 			if (rssUrl == null) {
 				responseCode = HttpStatus.SC_BAD_REQUEST;
 				break;
 			}
 
 			FeedEntry feed = readers.get(rssUrl).getLastEntry();
 			try {
 				@SuppressWarnings("unchecked")
 				EndpointDescription edp = getEndpointDescriptionFromJSON(json
 						.fromJSON(feed.content()));
 				if (feed.title().equals("Endpoint added")) {
 					registrations.addEndpoint(rssUrl, edp);
 					sendSubscription = new SendSubscription(edp, ENDPOINT_ADD,
 							this);
 					sendSubscription.start();
 				} else if (feed.title().equals("Endpoint removed")) {
 					registrations.removeEndpoint(rssUrl, edp);
 					sendSubscription = new SendSubscription(edp,
 							ENDPOINT_REMOVE, this);
 					sendSubscription.start();
 				}
 
 				responseCode = HttpStatus.SC_ACCEPTED;
 			} catch (ParseException e) {
 				responseCode = HttpStatus.SC_BAD_REQUEST;
 				e.printStackTrace();
 			}
 			break;
 
 		case subscribe:
 			if ((endpointFilter == null) || (callBackUrl == null)) {
 				responseCode = HttpStatus.SC_BAD_REQUEST;
 			} else {
 				registrations.addSubscrition(callBackUrl, endpointFilter);
 				responseCode = HttpStatus.SC_ACCEPTED;
 				// check if already register an endpoint which match the filter
 
 				sendSubscription = new SendSubscription(callBackUrl,
 						ENDPOINT_ADD, this);
 				sendSubscription.start();
 			}
 
 			break;
 
 		case unsubscribe:
 			if (callBackUrl == null) {
 				responseCode = HttpStatus.SC_BAD_REQUEST;
 				break;
 			}
 			registrations.removeSubscribtion(callBackUrl);
 			responseCode = HttpStatus.SC_ACCEPTED;
 			break;
 			
 		case getAllEndpoints:
 			resp.setContentType("text/html");
 			for (EndpointDescription endpoint : registrations.getAllEndpoints()) {
 				resp.getWriter().append(endpoint.toString()+"<br><br>");
 			}
 			responseCode = HttpStatus.SC_ACCEPTED;
 			break;
 
 		// hub.mode not found
 		default:
 			responseCode = HttpStatus.SC_BAD_REQUEST;
 			break;
 		}
 
 		resp.setStatus(responseCode);
 	}
 
 	@SuppressWarnings("unchecked")
 	private EndpointDescription getEndpointDescriptionFromJSON(
 			Map<String, Object> map) {
 
 		if (map.get(Constants.OBJECTCLASS) instanceof ArrayList<?>) {
 			map.put(Constants.OBJECTCLASS, ((ArrayList<String>) map
 					.get(Constants.OBJECTCLASS)).toArray(new String[0]));
 		}
 
 		if (map.get(RemoteConstants.ENDPOINT_SERVICE_ID) instanceof Integer) {
 			Integer id = (Integer) map
 					.get((RemoteConstants.ENDPOINT_SERVICE_ID));
 			map.put(RemoteConstants.ENDPOINT_SERVICE_ID, id.longValue());
 		}
 		return new EndpointDescription(map);
 	}
 
 	public JSONService json() {
 		return json;
 	}
 
 	public Registrations registrations() {
 		return registrations;
 	}
 
 	private class FeedReaderTracker implements ServiceTrackerCustomizer {
 		private String rss_url;
 
 		public FeedReaderTracker(String rss_url) throws InvalidSyntaxException {
 
			this.rss_url = rss_url;
 
 			String readerFilter = ("(&(" + Constants.OBJECTCLASS + "="
 					+ READER_SERVICE_CLASS + ")(" + READER_FILTER_PROPERTY
 					+ "=" + this.rss_url + "))");
 			feedReaderTracker = new ServiceTracker(context,
 					createFilter(readerFilter), this);
 			feedReaderTracker.open();
 		}
 
 		public Object addingService(ServiceReference reference) {
 			FeedReader reader = (FeedReader) context.getService(reference);
 			readers.put(this.rss_url, reader);
 			return reader;
 		}
 
 		public void modifiedService(ServiceReference reference, Object service) {
 
 		}
 
 		public void removedService(ServiceReference reference, Object service) {
 			readers.remove(this.rss_url);
 
 		}
 	}
 
 	private class FactoryTracker implements ServiceTrackerCustomizer {
 		private String rss_url;
 
 		public FactoryTracker(String rss_url) throws InvalidSyntaxException {
 
			this.rss_url = rss_url;
 
 			instanceDictionary = new Hashtable<String, Object>();
 			instanceDictionary.put("feed.url", this.rss_url);
 			instanceDictionary.put("feed.period", 1);
 			factoryTracker = new ServiceTracker(context,
 					createFilter(FEED_READER_FACTORY_FILTER), this);
 			factoryTracker.open();
 		}
 
 		public Object addingService(ServiceReference reference) {
 			Factory factory = (Factory) context.getService(reference);
 			try {
 				if (!(readers.containsKey(this.rss_url))) {
 					return factory.createComponentInstance(instanceDictionary);
 				}
 			} catch (UnacceptableConfiguration e) {
 				e.printStackTrace();
 			} catch (MissingHandlerException e) {
 				e.printStackTrace();
 			} catch (ConfigurationException e) {
 				e.printStackTrace();
 			}
 			return readers.get(rss_url);
 		}
 
 		public void modifiedService(ServiceReference reference, Object service) {
 		}
 
 		public void removedService(ServiceReference reference, Object service) {
 			readers.remove(this.rss_url);
 		}
 	}
 
 }
