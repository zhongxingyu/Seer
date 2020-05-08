 package at.molindo.elasticsync.jest.internal;
 
 import io.searchbox.client.JestClient;
 import io.searchbox.client.JestResult;
 import io.searchbox.core.Bulk;
 import io.searchbox.core.Delete;
 import io.searchbox.core.Doc;
 import io.searchbox.core.Index;
 import io.searchbox.core.MultiGet;
 import io.searchbox.core.Search;
 import io.searchbox.core.Search.Builder;
 import io.searchbox.core.SearchScroll;
 import io.searchbox.params.Parameters;
 import io.searchbox.params.SearchType;
 
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.slf4j.Logger;
 
 import at.molindo.elasticsync.api.Document;
 import at.molindo.elasticsync.api.ElasticsearchIndex;
 import at.molindo.elasticsync.api.IdAndVersionFactory;
 import at.molindo.elasticsync.api.LogUtils;
 import at.molindo.elasticsync.jest.internal.version.IVersionHelper;
 
 import com.google.gson.JsonArray;
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 
 public class ElasticsearchJestIndex implements ElasticsearchIndex {
 
 	private static final Logger LOG = LogUtils.loggerForThisClass();
 
 	private static final List<Doc> EMPTY_DOC_LIST = Collections.emptyList();
 
 	static final int BATCH_SIZE = 100000; // only ids and versions
 	static final int SCROLL_TIME_IN_MINUTES = 10;
 
 	private long numItems = 0;
 
 	private final JestClient client;
 	private final String indexName;
 	private final String query;
 	private final IdAndVersionFactory idAndVersionFactory;
 	private final IVersionHelper versionHelper;
 
 	public ElasticsearchJestIndex(JestClient client, String indexName, String query,
 			IdAndVersionFactory idAndVersionFactory, IVersionHelper versionHelper) {
 		this.client = client;
 		this.indexName = indexName;
 		this.query = query;
 		this.idAndVersionFactory = idAndVersionFactory;
 		this.versionHelper = versionHelper;
 	}
 
 	@Override
 	public IdAndVersionFactory getIdAndVersionFactory() {
 		return idAndVersionFactory;
 	}
 
 	@Override
 	public void downloadTo(String type, OutputStream outputStream) {
 		long begin = System.currentTimeMillis();
 		doDownloadTo(type, outputStream);
 		LogUtils.infoTimeTaken(LOG, begin, numItems, "Scan & Download completed");
 		numItems = 0;
 	}
 
 	private void doDownloadTo(String type, OutputStream outputStream) {
 		try {
 			ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
 			consumeBatches(objectOutputStream, startScroll(type));
 			objectOutputStream.close();
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	void consumeBatches(ObjectOutputStream objectOutputStream, String initialScrollId) throws Exception {
 
 		String scrollId = initialScrollId;
 
 		JestResult batchSearchResponse = null;
 		do {
 			SearchScroll.Builder builder = new SearchScroll.Builder(scrollId, SCROLL_TIME_IN_MINUTES + "m");
 
 			batchSearchResponse = client.execute(builder.build());
 			scrollId = batchSearchResponse.getJsonObject().get("_scroll_id").getAsString();
 
 		} while (writeSearchResponseToOutputStream(objectOutputStream, batchSearchResponse));
 	}
 
 	boolean writeSearchResponseToOutputStream(ObjectOutputStream objectOutputStream, JestResult searchResponse)
 			throws IOException {
 
 		// "hits":{"total":62319,"max_score":0.0,"hits":[{"_index":"stats","_type":"stats","_id":"MostPopularArtists","_version":1392888398281,"_score":0.0,"_source":{"_id":"MostPopularArtists","_ttl":5
 
 		JsonArray hits = searchResponse.getJsonObject().getAsJsonObject("hits").getAsJsonArray("hits");
 
 		for (JsonElement e : hits) {
 			JsonObject hit = e.getAsJsonObject();
 
 			String id = hit.get("_id").getAsString();
 			long version = hit.get("_version").getAsLong();
 
 			idAndVersionFactory.create(id, version).writeToStream(objectOutputStream);
 			numItems++;
 		}
 		return hits.size() > 0;
 	}
 
 	String startScroll(String type) {
 
 		Builder builder = new Search.Builder(versionHelper.createQuery(query));
 		builder.addIndex(indexName);
 		builder.addType(type);
 		builder.setParameter(Parameters.SIZE, BATCH_SIZE);
 		builder.setParameter(Parameters.EXPLAIN, false);
 		// searchRequestBuilder.setNoFields();
 		builder.setParameter(Parameters.VERSION, true);
 		builder.setSearchType(SearchType.SCAN);
 		builder.setParameter(Parameters.SCROLL, SCROLL_TIME_IN_MINUTES + "m");
 
 		try {
 			JestResult result = client.execute(builder.build());
			if (result.isSucceeded()) {
				return result.getJsonObject().get("_scroll_id").getAsString();
			} else {
				throw new RuntimeException("failed to start scroll operation: " + result.getJsonObject());
			}
 		} catch (Exception e) {
 			throw new RuntimeException("failed to start scroll operation", e);
 		}
 
 	}
 
 	@Override
 	public void load(List<Document> documents, final Runnable listener) {
 
 		final Map<String, Map<String, Document>> map = new HashMap<>();
 
 		MultiGet.Builder.ByDoc builder = new MultiGet.Builder.ByDoc(EMPTY_DOC_LIST);
 
 		for (Document doc : documents) {
 
 			builder.addDoc(new Doc(indexName, doc.getType(), doc.getId()));
 
 			Map<String, Document> typeMap = map.get(doc.getType());
 			if (typeMap == null) {
 				map.put(doc.getType(), typeMap = new HashMap<>());
 			}
 			typeMap.put(doc.getId(), doc);
 		}
 
 		try {
 			JestResult result = client.execute(builder.build());
 
 			JsonArray docs = result.getJsonObject().getAsJsonArray("docs");
 
 			for (JsonElement e : docs) {
 				JsonObject item = e.getAsJsonObject();
 
 				String type = item.get("_type").getAsString();
 				String id = item.get("_id").getAsString();
 
 				Map<String, Document> typeMap = map.get(type);
 				if (typeMap == null) {
 					LOG.warn("unexpected type returned: " + type);
 					continue;
 				}
 
 				Document doc = typeMap.get(id);
 				if (doc == null) {
 					LOG.warn("unexpected id returned: " + type + "/" + id);
 					continue;
 				}
 
 				if (item.get("exists").getAsBoolean()) {
 					long version = item.get("_version").getAsLong();
 					String source = item.get("_source").toString();
 
 					doc.setVersion(version).setSource(source);
 				} else {
 					doc.setDeleted();
 				}
 
 				// }
 			}
 
 			if (listener != null) {
 				listener.run();
 			}
 
 		} catch (Exception e) {
 			LOG.error("loading documents failed", e);
 			if (listener != null) {
 				listener.run();
 			}
 		}
 	}
 
 	@Override
 	public void index(final List<Document> documents, final Runnable listener) {
 		Bulk.Builder builder = new Bulk.Builder();
 		for (Document doc : documents) {
 			if (doc.isDeleted()) {
 				builder.addAction(new Delete.Builder(indexName, doc.getType(), doc.getId())
 						.setParameter(Parameters.VERSION_TYPE, "external")
 						.setParameter(Parameters.VERSION, doc.getVersion() + 1).build());
 			} else {
 				builder.addAction(new Index.Builder(doc.getSource()).index(indexName).type(doc.getType())
 						.id(doc.getId()).setParameter(Parameters.VERSION_TYPE, "external")
 						.setParameter(Parameters.VERSION, doc.getVersion()).build());
 			}
 		}
 
 		try {
 			JestResult result = client.execute(builder.build());
 
 			JsonArray items = result.getJsonObject().getAsJsonArray("items");
 
 			Iterator<Document> docIter = documents.iterator();
 			for (JsonElement e : items) {
 				JsonObject item = e.getAsJsonObject();
 
 				if (item.has("error")) {
 
 					Document document = docIter.next();
 
 					LOG.warn("document {} failed {} ({})", document.isDeleted() ? "delete" : "index", document, item
 							.get("error").getAsString());
 				}
 			}
 
 			if (listener != null) {
 				listener.run();
 			}
 
 		} catch (Exception e) {
 			LOG.error("indexing documents failed", e);
 			if (listener != null) {
 				listener.run();
 			}
 		}
 
 	}
 
 	@Override
 	public void close() {
 		if (client != null) {
 			client.shutdownClient();
 		}
 	}
 
 }
