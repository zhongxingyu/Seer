 package eu.europeana.uim.enrichment.service.impl;
 
 import java.net.URL;
 import java.net.UnknownHostException;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;
 import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocumentList;
 import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
 
 import com.google.code.morphia.Datastore;
 import com.google.code.morphia.Morphia;
 import com.mongodb.Mongo;
 import com.mongodb.MongoException;
import com.sun.jmx.mbeanserver.ModifiableClassLoaderRepository;
 
 import eu.annocultor.converters.europeana.Entity;
 import eu.europeana.corelib.definitions.solr.beans.FullBean;
 import eu.europeana.corelib.tools.lookuptable.Collection;
 import eu.europeana.corelib.tools.lookuptable.CollectionMongoServer;
 import eu.europeana.corelib.tools.lookuptable.EuropeanaId;
 import eu.europeana.corelib.tools.lookuptable.EuropeanaIdMongoServer;
 import eu.europeana.uim.common.BlockingInitializer;
 import eu.europeana.uim.enrichment.service.EnrichmentService;
 import eu.europeana.uim.enrichment.utils.EuropeanaEnrichmentTagger;
 import eu.europeana.uim.enrichment.utils.OsgiEuropeanaIdMongoServer;
 import eu.europeana.uim.enrichment.utils.PropertyReader;
 import eu.europeana.uim.enrichment.utils.UimConfigurationProperty;
 
 public class EnrichmentServiceImpl implements EnrichmentService {
 
 	private final static String PORTALURL = "http://www.europeana.eu/portal/record";
 	//public static EuropeanaEnrichmentTagger tagger;
 	private static HttpSolrServer solrServer;
 	private static HttpSolrServer migrationServer;
 	private static String mongoDB=PropertyReader.getProperty(UimConfigurationProperty.MONGO_DB_EUROPEANA);
 	private static String mongoHost=PropertyReader.getProperty(UimConfigurationProperty.MONGO_HOSTURL);
 	private static String mongoPort= PropertyReader.getProperty(UimConfigurationProperty.MONGO_HOSTPORT);
 	private static String solrUrl= PropertyReader.getProperty(UimConfigurationProperty.SOLR_HOSTURL);
 	private static String solrCore=PropertyReader.getProperty(UimConfigurationProperty.SOLR_CORE);
 	private static String solrCoreMigration = PropertyReader.getProperty(UimConfigurationProperty.SOLR_CORE_MIGRATION);
 	private static CollectionMongoServer cmServer;
 	private  static OsgiEuropeanaIdMongoServer idserver;
 	public EnrichmentServiceImpl(){
 		
 		try {
 		solrServer = new HttpSolrServer(new URL(solrUrl)+solrCore);
 		migrationServer = new HttpSolrServer(new URL(solrUrl)+solrCoreMigration);
 			
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		
 		
 		BlockingInitializer initializer = new BlockingInitializer() {
 			
 			@Override
 			protected void initializeInternal() {
 				try {
 					Morphia morphia = new Morphia();
 					morphia.map(Collection.class);
 					Datastore datastore = morphia.createDatastore(new Mongo(mongoHost,Integer.parseInt(mongoPort)), "collections");
 					cmServer = new CollectionMongoServer();
 					datastore.ensureIndexes();
 					cmServer.setDatastore(datastore);
 					BlockingInitializer colInitializer = new BlockingInitializer() {
 						
 						@Override
 						protected void initializeInternal() {
 							Collection col = new Collection();
 							
 						}
 					};
 					colInitializer.initialize(Collection.class.getClassLoader());
 				} catch (NumberFormatException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (UnknownHostException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				} catch (MongoException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				
 			}
 		}; 
 		initializer.initialize(CollectionMongoServer.class.getClassLoader());
 		
 		
 		
 					try {
 						idserver = new OsgiEuropeanaIdMongoServer(new Mongo(mongoHost,Integer.parseInt(mongoPort)), "EuropeanaId");
 						idserver.createDatastore();
 					} catch (NumberFormatException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					} catch (UnknownHostException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					} catch (MongoException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				
 		
 	}
 	
 
 	
 
 	@Override
 	public  HttpSolrServer getSolrServer() {
 		return solrServer;
 	}
 
 	
 
 	
 	@Override
 	public String getMongoDB() {
 		return mongoDB;
 	}
 
 	public void setMongoDB(String mongoDB) {
 		EnrichmentServiceImpl.mongoDB = mongoDB;
 	}
 
 	public  String getMongoHost() {
 		return mongoHost;
 	}
 
 	public void setMongoHost(String mongoHost) {
 		EnrichmentServiceImpl.mongoHost = mongoHost;
 	}
 
 	public String getMongoPort() {
 		return mongoPort;
 	}
 
 	public void setMongoPort(String mongoPort) {
 		EnrichmentServiceImpl.mongoPort = mongoPort;
 	}
 
 	public String getSolrUrl() {
 		return solrUrl;
 	}
 
 	public void setSolrUrl(String solrUrl) {
 		EnrichmentServiceImpl.solrUrl = solrUrl;
 	}
 
 	public String getSolrCore() {
 		return solrCore;
 	}
 
 	public void setSolrCore(String solrCore) {
 		EnrichmentServiceImpl.solrCore = solrCore;
 	}
 
 	
 
 	
 
 	public void setSolrServer(HttpSolrServer solrServer) {
 		EnrichmentServiceImpl.solrServer = solrServer;
 	}
 
 
 
 	@Override
 	public HttpSolrServer getMigrationServer(){
 		return EnrichmentServiceImpl.migrationServer;
 	}
 
 
 
 
 	@Override
 	public CollectionMongoServer getCollectionMongoServer() {
 		return cmServer;
 	}
 
 	@Override
 	public EuropeanaIdMongoServer getEuropeanaIdMongoServer() {
 		return idserver;
 	}
 
 
 
 
 	@Override
 	public List<EuropeanaId> retrieveEuropeanaIdFromOld(String string) {
 		
 		return idserver.retrieveEuropeanaIdFromOld(string);
 	}
 
 
 
 
 	@Override
 	public void saveEuropeanaId(EuropeanaId europeanaId) {
 		idserver.saveEuropeanaId(europeanaId);
 		
 	}
 	
 	@Override
 	public void createLookupEntry(FullBean fullBean, String collectionId, String hash) {
		/*
 		List<EuropeanaId> ids = idserver.retrieveEuropeanaIdFromOld(PORTALURL+"/"+collectionId+"/"+hash);
 		boolean found = false;
 		for (EuropeanaId id:ids){
 			if (StringUtils.equals(fullBean.getAbout(), id.getNewId())){
 				found=true;
 				break;
 			}
 		}
		
 		if(!found){
 			if(ids.size()>0 && ids.get(0).getOldId()!=null){
 			EuropeanaId id= new EuropeanaId();
 			id.setOldId(ids.get(0).getOldId());
 			id.setLastAccess(0);
 			id.setTimestamp(new Date().getTime());
 			id.setNewId(fullBean.getAbout());
 			saveEuropeanaId(id);
 			}
 		}
*/
		
		ModifiableSolrParams params = new ModifiableSolrParams();
		params. add("q", "europeana_id:"+ClientUtils.escapeQueryChars("/"+collectionId+"/"+hash));
		try {
			SolrDocumentList solrList = solrServer.query(params).getResults();
			if(solrList.size()>0){
				EuropeanaId id= new EuropeanaId();
				id.setOldId("/"+collectionId+"/"+hash);
				id.setLastAccess(0);
				id.setTimestamp(new Date().getTime());
				id.setNewId(fullBean.getAbout());
				saveEuropeanaId(id);
				
			}
			
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 	}
 
 }
