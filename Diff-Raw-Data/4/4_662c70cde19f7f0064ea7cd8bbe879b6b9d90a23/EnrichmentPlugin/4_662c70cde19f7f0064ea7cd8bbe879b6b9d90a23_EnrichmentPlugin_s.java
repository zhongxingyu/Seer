 package eu.europeana.uim.enrichment;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.StringReader;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.solr.client.solrj.SolrServerException;
 import org.apache.solr.client.solrj.impl.CommonsHttpSolrServer;
 import org.apache.solr.common.SolrException;
 import org.apache.solr.common.SolrInputDocument;
 import org.jibx.runtime.BindingDirectory;
 import org.jibx.runtime.IBindingFactory;
 import org.jibx.runtime.IMarshallingContext;
 import org.jibx.runtime.IUnmarshallingContext;
 import org.jibx.runtime.JiBXException;
 
 import com.mongodb.Mongo;
 
 import eu.europeana.corelib.definitions.jibx.RDF;
 import eu.europeana.corelib.definitions.model.EdmLabel;
 import eu.europeana.corelib.definitions.solr.beans.FullBean;
 import eu.europeana.corelib.solr.server.impl.EdmMongoServerImpl;
 import eu.europeana.corelib.solr.utils.EseEdmMap;
 import eu.europeana.corelib.solr.utils.MongoConstructor;
 import eu.europeana.corelib.solr.utils.SolrConstructor;
 import eu.europeana.corelib.tools.lookuptable.EuropeanaId;
 import eu.europeana.corelib.tools.lookuptable.EuropeanaIdMongoServer;
 import eu.europeana.corelib.tools.rdf.Solr2Rdf;
 import eu.europeana.corelib.tools.utils.HashUtils;
 import eu.europeana.corelib.tools.utils.PreSipCreatorUtils;
 import eu.europeana.corelib.tools.utils.SipCreatorUtils;
 import eu.europeana.uim.api.AbstractIngestionPlugin;
 import eu.europeana.uim.api.CorruptedMetadataRecordException;
 import eu.europeana.uim.api.ExecutionContext;
 import eu.europeana.uim.api.IngestionPluginFailedException;
 import eu.europeana.uim.common.TKey;
 import eu.europeana.uim.enrichment.service.EnrichmentService;
 import eu.europeana.uim.model.europeana.EuropeanaModelRegistry;
 import eu.europeana.uim.model.europeanaspecific.fieldvalues.ControlledVocabularyProxy;
 import eu.europeana.uim.model.europeanaspecific.fieldvalues.EuropeanaRetrievableField;
 import eu.europeana.uim.store.Collection;
 import eu.europeana.uim.store.MetaDataRecord;
 import eu.europeana.uim.sugarcrm.SugarCrmRecord;
 import eu.europeana.uim.sugarcrm.SugarCrmService;
 
 
 public class EnrichmentPlugin extends AbstractIngestionPlugin {
 	private static String vocabularyDB;
 	private static MongoConstructor mongoConstructor;
 	private static EdmMongoServerImpl edmMongoServer;
 	private static CommonsHttpSolrServer solrServer;
 	private static Mongo mongo;
 	private static String mongoDB;
 	private static String mongoHost;
 	private static String mongoPort;
 	private static String solrUrl;
 	private static String solrCore;
 	private static int recordNumber;
 	private static String europeanaID;
 	private static final int RETRIES = 10;
 	private static String repository;
 	private static SugarCrmService sugarCrmService;
 	private static EnrichmentService enrichmentService;
 	private static String previewsOnlyInPortal;	
 	private static String collections;
 	
 	public EnrichmentPlugin(String name, String description) {
 		super(name, description);
 		// TODO Auto-generated constructor stub
 	}
 	public EnrichmentPlugin() {
 		super("","");
 		// TODO Auto-generated constructor stub
 	}
 	
 	/**
 	 * The parameters used by this WorkflowStart
 	 */
 	private static final List<String> params = new ArrayList<String>() {
 		private static final long serialVersionUID = 1L;
 		
 	};
 	public TKey<?, ?>[] getInputFields() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public TKey<?, ?>[] getOptionalFields() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public TKey<?, ?>[] getOutputFields() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public void initialize() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void shutdown() {
 		// TODO Auto-generated method stub
 
 	}
 
 	public List<String> getParameters() {
 		// TODO Auto-generated method stub
 		return params;
 	}
 
 	public int getPreferredThreadCount() {
 		// TODO Auto-generated method stub
 		return 1;
 	}
 
 	public int getMaximumThreadCount() {
 		// TODO Auto-generated method stub
 		return 1;
 	}
 
 	public <I> void initialize(ExecutionContext<I> context)
 			throws IngestionPluginFailedException {
 		// TODO Auto-generated method stub
 		
 		try {
			
 			solrServer = new CommonsHttpSolrServer(solrUrl+solrCore);
 			 solrServer.setSoTimeout(1000);  // socket read timeout
 			  solrServer.setConnectionTimeout(100);
 			  solrServer.setDefaultMaxConnectionsPerHost(100);
 			  solrServer.setMaxTotalConnections(100);
 			  solrServer.setFollowRedirects(false);
 			@SuppressWarnings("rawtypes")
 			Collection collection = (Collection) context.getExecution().getDataSet();
 			String sugarCrmId = collection
 					.getValue(ControlledVocabularyProxy.SUGARCRMID);
 			SugarCrmRecord sugarCrmRecord = sugarCrmService
 					.retrieveRecord(sugarCrmId);
 			previewsOnlyInPortal = sugarCrmRecord
 					.getItemValue(EuropeanaRetrievableField.PREVIEWS_ONLY_IN_PORTAL);
 
 			mongoConstructor = new MongoConstructor();
 			mongo  = new Mongo(mongoHost, Integer.parseInt(mongoPort));
 			edmMongoServer = new EdmMongoServerImpl(mongo,mongoDB,"","");
 			mongoConstructor.setMongoServer(edmMongoServer);
 			
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public <I> void completed(ExecutionContext<I> context)
 			throws IngestionPluginFailedException {
 		try {
 			solrServer.commit();
 			solrServer.optimize();
 
 		} catch (IOException e) {
 			context.getLoggingEngine().logFailed(
 					Level.SEVERE,
 					this,
 					e,
 					"Input/Output exception occured in Solr with the following message: "
 							+ e.getMessage());
 		} catch (SolrServerException e) {
 			context.getLoggingEngine().logFailed(
 					Level.SEVERE,
 					this,
 					e,
 					"Solr server exception occured in Solr with the following message: "
 							+ e.getMessage());
 		}
 
 	}
 
 	public <I> boolean processRecord(MetaDataRecord<I> mdr,
 			ExecutionContext<I> context) throws IngestionPluginFailedException,
 			CorruptedMetadataRecordException {
 		IBindingFactory bfact;
 		try {
 			bfact = BindingDirectory.getFactory(RDF.class);
 
 			IUnmarshallingContext uctx = bfact.createUnmarshallingContext();
 
 			String value = mdr.getValues(EuropeanaModelRegistry.EDMDEREFERENCEDRECORD).get(
 					0);
 			RDF rdf = (RDF) uctx.unmarshalDocument(new StringReader(value));
 			SolrInputDocument solrInputDocument = enrichmentService.enrich(SolrConstructor.constructSolrDocument(rdf));
 			Solr2Rdf solr2Rdf = new Solr2Rdf();
 			solr2Rdf.initialize();
 			
 			RDF der = (RDF) uctx.unmarshalDocument(new StringReader(solr2Rdf.constructFromSolrDocument(solrInputDocument)));
 			IMarshallingContext marshallingContext = bfact.createMarshallingContext();
 			ByteArrayOutputStream out = new ByteArrayOutputStream();
 			 marshallingContext.marshalDocument(der, "UTF-8", null, out);			
 			FullBean fullBean = mongoConstructor.constructFullBean(der);
 			solrInputDocument.addField(EdmLabel.PREVIEW_NO_DISTRIBUTE.toString(), previewsOnlyInPortal);
			fullBean.setPreviewNoDistribute(previewsOnlyInPortal);
 			String collectionId = (String) mdr.getCollection().getId();
 			
 			String fileName = (String) mdr.getCollection().getName();
 			String hash = hashExists(collectionId, fileName, fullBean);
 			
 			if (StringUtils.isNotEmpty(hash)) {
 				createLookupEntry(fullBean, hash);
 			}
 			
 			
 			if (edmMongoServer.getFullBean(fullBean.getAbout()) == null) {
 				edmMongoServer.getDatastore().save(fullBean);
 			}
 			
 			int retries = 0;
 			while (retries < RETRIES) {
 				try {
 				solrServer.add(solrInputDocument, 1000);
 				retries = RETRIES;
 				recordNumber++;
 				return true;
 			} catch (SolrServerException e) {
 				e.printStackTrace();
 				retries++;
 			} catch (IOException e) {
 				e.printStackTrace();
 				retries++;
 			} catch (SolrException e) {
 				e.printStackTrace();
 				retries++;
 			}
 		}
 
 		} catch (JiBXException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return true;
 	}
 	
 	
 	public MongoConstructor getMongoConstructor() {
 		return mongoConstructor;
 	}
 	public void setMongoConstructor(MongoConstructor mongoConstructor) {
 		EnrichmentPlugin.mongoConstructor = mongoConstructor;
 	}
 	public EdmMongoServerImpl getEdmMongoServer() {
 		return edmMongoServer;
 	}
 	public void setEdmMongoServer(EdmMongoServerImpl edmMongoServer) {
 		EnrichmentPlugin.edmMongoServer = edmMongoServer;
 	}
 	public CommonsHttpSolrServer getSolrServer() {
 		return solrServer;
 	}
 	public void setSolrServer(CommonsHttpSolrServer solrServer) {
 		EnrichmentPlugin.solrServer = solrServer;
 	}
 	public Mongo getMongo() {
 		return mongo;
 	}
 	public void setMongo(Mongo mongo) {
 		EnrichmentPlugin.mongo = mongo;
 	}
 	public void setSugarCrmService(SugarCrmService sugarCrmService) {
 		EnrichmentPlugin.sugarCrmService = sugarCrmService;
 	}
 	
 
 	public String getEuropeanaID() {
 		return europeanaID;
 	}
 
 	public void setEuropeanaID(String europeanaID) {
 		EnrichmentPlugin.europeanaID = europeanaID;
 	}
 	
 	public int getRecords() {
 		return recordNumber;
 	}
 	
 	public String getRepository() {
 		return repository;
 	}
 
 	public void setRepository(String repository) {
 		EnrichmentPlugin.repository = repository;
 	}
 	public void setVocabularyDB(String vocabularyDB) {
 		EnrichmentPlugin.vocabularyDB = vocabularyDB;
 	}
 
 	public String getVocabularyDB() {
 		return vocabularyDB;
 	}
 	public  String getCollections() {
 		return collections;
 	}
 	public  void setCollections(String collections) {
 		EnrichmentPlugin.collections = collections;
 	}
 	public  SugarCrmService getSugarCrmService() {
 		return sugarCrmService;
 	}
 	public  String getMongoDB() {
 		return mongoDB;
 	}
 	public  void setMongoDB(String mongoDB) {
 		EnrichmentPlugin.mongoDB = mongoDB;
 	}
 	public  String getMongoHost() {
 		return mongoHost;
 	}
 	public void setMongoHost(String mongoHost) {
 		EnrichmentPlugin.mongoHost = mongoHost;
 	}
 	public  String getMongoPort() {
 		return mongoPort;
 	}
 	public void setMongoPort(String mongoPort) {
 		EnrichmentPlugin.mongoPort = mongoPort;
 	}
 	public String getSolrUrl() {
 		return solrUrl;
 	}
 	public void setSolrUrl(String solrUrl) {
 		EnrichmentPlugin.solrUrl = solrUrl;
 	}
 	public String getSolrCore() {
 		return solrCore;
 	}
 	public void setSolrCore(String solrCore) {
 		EnrichmentPlugin.solrCore = solrCore;
 	}
 	public EnrichmentService getEnrichmentService() {
 		return enrichmentService;
 	}
 	public void setEnrichmentService(EnrichmentService enrichmentService) {
 		EnrichmentPlugin.enrichmentService = enrichmentService;
 	}
 	private void createLookupEntry(FullBean fullBean, String hash) {
 		EuropeanaIdMongoServer europeanaIdMongoServer = new EuropeanaIdMongoServer(
 				mongo, europeanaID);
 		EuropeanaId europeanaId = europeanaIdMongoServer
 				.retrieveEuropeanaIdFromOld(hash).get(0);
 		europeanaId.setNewId(fullBean.getAbout());
 		europeanaIdMongoServer.saveEuropeanaId(europeanaId);
 
 	}
 
 	private String hashExists(String collectionId, String fileName,
 			FullBean fullBean) {
 		SipCreatorUtils sipCreatorUtils = new SipCreatorUtils();
 		sipCreatorUtils.setRepository(repository);
 		if (sipCreatorUtils.getHashField(collectionId, fileName) != null) {
 			return HashUtils.createHash(EseEdmMap.valueOf(
 					sipCreatorUtils.getHashField(collectionId, fileName))
 					.getEdmValue(fullBean));
 		}
 		PreSipCreatorUtils preSipCreatorUtils = new PreSipCreatorUtils();
 		preSipCreatorUtils.setRepository(repository);
 		if (preSipCreatorUtils.getHashField(collectionId, fileName) != null) {
 			return HashUtils.createHash(EseEdmMap.valueOf(
 					preSipCreatorUtils.getHashField(collectionId, fileName))
 					.getEdmValue(fullBean));
 		}
 		return null;
 	}
 	
 }
