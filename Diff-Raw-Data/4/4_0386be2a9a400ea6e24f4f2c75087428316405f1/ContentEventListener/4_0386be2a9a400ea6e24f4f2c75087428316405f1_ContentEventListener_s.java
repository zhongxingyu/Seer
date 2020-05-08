 package uk.co.tfd.sm.search.es;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.felix.scr.annotations.Activate;
 import org.apache.felix.scr.annotations.Component;
 import org.apache.felix.scr.annotations.Deactivate;
 import org.apache.felix.scr.annotations.Property;
 import org.apache.felix.scr.annotations.Reference;
 import org.apache.felix.scr.annotations.Service;
 import org.elasticsearch.action.bulk.BulkItemResponse;
 import org.elasticsearch.action.bulk.BulkResponse;
 import org.elasticsearch.client.Client;
 import org.elasticsearch.client.action.bulk.BulkRequestBuilder;
 import org.elasticsearch.client.action.index.IndexRequestBuilder;
 import org.elasticsearch.client.transport.TransportClient;
 import org.elasticsearch.common.settings.ImmutableSettings;
 import org.elasticsearch.common.settings.ImmutableSettings.Builder;
 import org.elasticsearch.common.transport.InetSocketTransportAddress;
 import org.elasticsearch.common.xcontent.XContentBuilder;
 import org.elasticsearch.common.xcontent.XContentFactory;
 import org.elasticsearch.node.Node;
 import org.elasticsearch.node.NodeBuilder;
 import org.osgi.service.event.Event;
 import org.osgi.service.event.EventConstants;
 import org.osgi.service.event.EventHandler;
 import org.sakaiproject.nakamura.api.lite.ClientPoolException;
 import org.sakaiproject.nakamura.api.lite.Repository;
 import org.sakaiproject.nakamura.api.lite.Session;
 import org.sakaiproject.nakamura.api.lite.StorageClientException;
 import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
 import org.sakaiproject.nakamura.api.lite.util.Iterables;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.co.tfd.sm.api.search.IndexingHandler;
 import uk.co.tfd.sm.api.search.InputDocument;
 import uk.co.tfd.sm.api.search.RepositorySession;
 import uk.co.tfd.sm.api.search.TopicIndexer;
 
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 
 /**
  * Indexes content using elastic search. Driven by Events expected to be
  * synchronous inside transactions.
  * 
  * @author ieb
  * 
  */
 @Component(immediate = true, metatype = true)
 @Service(value = { EventHandler.class, TopicIndexer.class })
 public class ContentEventListener implements EventHandler, TopicIndexer {
 
 	@Property(value = { "org/sakaiproject/nakamura/lite/*" }, propertyPrivate = true)
 	static final String TOPICS = EventConstants.EVENT_TOPIC;
 
 	private static final String[] DEFAULT_ESCLUSTER = { "host1:port",
 			"host2:port" };
 
 	@Property(value = { "host1:port", "host2:port" })
 	private static final String PROP_ESCLUSTER = "ec-cluster-hosts";
 
 	@Property(boolValue = true, description = "If set to true, the ES node is embedded and joins the cluster")
 	private static final String PROP_EMBEDED = "embeded";
 
 	private static final String DEFAULT_CLUSTER_NAME = "smc-cluster";
 
 	@Property(value = DEFAULT_CLUSTER_NAME, description = "Name of the elsastic search cluster to join when embedded")
 	private static final String PROP_CLUSTER_NAME = "clustername";
 
 	@Property(boolValue = true, description = "If set to true, the ES node should serve local shards")
 	private static final Object PROP_LOCAL_SHARDS = "local-shards";
 
 	private static final Logger LOGGER = LoggerFactory
 			.getLogger(ContentEventListener.class);
 
 	private Map<String, Collection<IndexingHandler>> handlers = Maps
 			.newConcurrentMap();
 
 	/**
 	 * A protective lock surrounding adding and removing keys from the handlers
 	 * map. THis is there because we could have 2 threads adding to the same key
 	 * at the same time. Its not there to protect the map itself or access to
 	 * iterators on the objects in the map as those changes are still atomic.
 	 * see usage for detail.
 	 */
 	private Object handlersLock = new Object();
 
 	private Client client;
 
 	private Node node;
 
 	@Reference
 	private Repository repository;
 
 	@Activate
 	protected void activate(Map<String, Object> properties) throws IOException,
 			ClientPoolException, StorageClientException, AccessDeniedException {
		boolean embeded = Utils.get(properties.get(PROP_EMBEDED), true);
		boolean withData = Utils.get(properties.get(PROP_LOCAL_SHARDS), true);
 		String clusterName = Utils.get(properties.get(PROP_CLUSTER_NAME),
 				DEFAULT_CLUSTER_NAME);
 		if (client == null) {
 			if (embeded) {
 				node = NodeBuilder.nodeBuilder().data(withData)
 						.clusterName(clusterName).build();
 				client = node.client();
 			} else {
 				Builder settingsBuilder = ImmutableSettings.settingsBuilder();
 				settingsBuilder.put("cluster.name", clusterName);
 				TransportClient tclient = new TransportClient(
 						settingsBuilder.build());
 				String[] clusterHosts = Utils.get(
 						properties.get(PROP_ESCLUSTER), DEFAULT_ESCLUSTER);
 				for (String s : clusterHosts) {
 					String[] h = StringUtils.split(s, ":");
 					tclient.addTransportAddress(new InetSocketTransportAddress(
 							h[0], Integer.parseInt(h[1])));
 				}
 				client = tclient;
 			}
 		}
 
 	}
 
 	@Deactivate
 	protected void deactivate(Map<String, Object> properties)
 			throws IOException {
 		if (client != null) {
 			client.close();
 			client = null;
 		}
 		if (node != null) {
 			node.close();
 			node = null;
 		}
 	}
 
 	/**
 	 * Handles an event from OSGi and indexes it. The indexing operation should
 	 * only index metadata and not bodies. Indexing of bodies is performed by a
 	 * seperate thread.
 	 * 
 	 * @see org.osgi.service.event.EventHandler#handleEvent(org.osgi.service.event.Event)
 	 */
 	public void handleEvent(Event event) {
 		String topic = event.getTopic();
 		Session session = (Session) event.getProperty(Session.class.getName());
 		RepositorySession repositoryRession = null;
 		try {
 			try {
 				repositoryRession = new RepositorySessionImpl(session,
 						repository);
 			} catch (ClientPoolException e1) {
 				LOGGER.error(e1.getMessage(), e1);
 				return;
 			} catch (StorageClientException e1) {
 				LOGGER.error(e1.getMessage(), e1);
 				return;
 			} catch (AccessDeniedException e1) {
 				LOGGER.error(e1.getMessage(), e1);
 				return;
 			}
 
 			LOGGER.debug("Got Event {} {} ", event, handlers);
 			Collection<IndexingHandler> contentIndexHandler = handlers
 					.get(topic);
 			if (contentIndexHandler != null && contentIndexHandler.size() > 0) {
 				BulkRequestBuilder bulk = client.prepareBulk();
 				for (IndexingHandler indexingHandler : contentIndexHandler) {
 					Collection<InputDocument> documents = indexingHandler
 							.getDocuments(repositoryRession, event);
 					for (InputDocument in : documents) {
 						if (in.isDelete()) {
 							bulk.add(client.prepareDelete(in.getIndexName(),
 									in.getDocumentType(), in.getDocumentId()));
 						} else {
 							try {
 								IndexRequestBuilder r = client.prepareIndex(
 										in.getIndexName(),
 										in.getDocumentType(),
 										in.getDocumentId());
 								XContentBuilder d = XContentFactory
 										.jsonBuilder();
 								d = d.startObject();
 								for (Entry<String, Object> e : in.getKeyData()) {
 									d = d.field(e.getKey(), e.getValue());
 								}
 								r.setSource(d.endObject());
 								bulk.add(r);
 							} catch (IOException e) {
 								LOGGER.error(e.getMessage(), e);
 							}
 						}
 					}
 				}
 				BulkResponse resp = bulk.execute().actionGet();
 				if (resp.hasFailures()) {
 					for (BulkItemResponse br : Iterables.adaptTo(resp
 							.iterator())) {
 						if (br.failed()) {
 							LOGGER.error("Failed {} {} ", br.getId(),
 									br.getFailureMessage());
 							// not going to retry at the moment, just log.
 						}
 					}
 				}
 
 			}
 		} finally {
 			if (repositoryRession != null) {
 				repositoryRession.logout();
 			}
 		}
 	}
 
 	public void addHandler(String topic, IndexingHandler handler) {
 		synchronized (handlersLock) {
 			Collection<IndexingHandler> topicHandlers = handlers.get(topic);
 			if (topicHandlers == null) {
 				topicHandlers = Sets.newHashSet();
 			} else {
 				// make a copy to avoid concurrency issues in the topicHandler
 				topicHandlers = Sets.newHashSet(topicHandlers);
 			}
 			topicHandlers.add(handler);
 			handlers.put(topic, topicHandlers);
 		}
 	}
 
 	public void removeHandler(String topic, IndexingHandler handler) {
 		synchronized (handlersLock) {
 			Collection<IndexingHandler> topicHandlers = handlers.get(topic);
 			if (topicHandlers != null && topicHandlers.size() > 0) {
 				topicHandlers = Sets.newHashSet(topicHandlers);
 				topicHandlers.remove(handler);
 				handlers.put(topic, topicHandlers);
 			}
 		}
 	}
 
 }
