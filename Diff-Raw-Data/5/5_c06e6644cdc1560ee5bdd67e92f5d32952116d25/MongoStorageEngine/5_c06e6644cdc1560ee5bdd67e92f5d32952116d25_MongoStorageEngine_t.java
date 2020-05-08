 /*
  * Copyright 2007 EDL FOUNDATION
  *
  * Licensed under the EUPL, Version 1.1 or - as soon they
  * will be approved by the European Commission - subsequent
  * versions of the EUPL (the "Licence");
  * you may not use this work except in compliance with the
  * Licence.
  * You may obtain a copy of the Licence at:
  *
  * http://ec.europa.eu/idabc/eupl
  *
  * Unless required by applicable law or agreed to in
  * writing, software distributed under the Licence is
  * distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied.
  * See the Licence for the specific language governing
  * permissions and limitations under the Licence.
  */
 package eu.europeana.uim.store.mongo;
 
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import org.bson.types.ObjectId;
 import com.google.code.morphia.Datastore;
 import com.google.code.morphia.Morphia;
 import com.mongodb.DB;
 import com.mongodb.Mongo;
 import eu.europeana.uim.EngineStatus;
 import eu.europeana.uim.orchestration.ExecutionContext;
 import eu.europeana.uim.storage.StorageEngine;
 import eu.europeana.uim.storage.StorageEngineException;
 import eu.europeana.uim.store.Collection;
 import eu.europeana.uim.store.Execution;
 import eu.europeana.uim.store.MetaDataRecord;
 import eu.europeana.uim.store.Provider;
 import eu.europeana.uim.store.Request;
 import eu.europeana.uim.store.UimDataSet;
 import eu.europeana.uim.store.mongo.aggregators.MDRPerCollectionAggregator;
 import eu.europeana.uim.store.mongo.aggregators.MDRPerRequestAggregator;
 import eu.europeana.uim.store.mongo.decorators.MongoCollectionDecorator;
 import eu.europeana.uim.store.mongo.decorators.MongoExecutionDecorator;
 import eu.europeana.uim.store.mongo.decorators.MongoMetadataRecordDecorator;
 import eu.europeana.uim.store.mongo.decorators.MongoProviderDecorator;
 import eu.europeana.uim.store.mongo.decorators.MongoRequestDecorator;
 import gnu.trove.iterator.hash.TObjectHashIterator;
 import gnu.trove.map.hash.THashMap;
 import gnu.trove.set.hash.THashSet;
 
 /**
  * Basic implementation of a StorageEngine based on MongoDB with Morphia.
  * 
  * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
  * @author Georgios Markakis <gwarkx@hotmail.com>
  */
 public class MongoStorageEngine extends AbstractEngine implements
 		StorageEngine<String> {
 
 	private static final String DEFAULT_UIM_DB_NAME = "UIM";
 	private static final String MNEMONICFIELD = "searchMnemonic";
 	private static final String NAMEFIELD = "searchName";
 	private static final String LOCALIDFIELD = "mongoId";
 	private static final String RECORDUIDFIELD = "uniqueID";
 	private static final String REQUESTRECORDS = "requestrecords";
 	private static final String COLLECTIONID = "collectionId";
 	private static final String REQUESTID = "requestId";
 	private static final String SEARCHDATE = "searchDate";
 
 	private static THashMap<String, MongoCollectionDecorator<String>> inmemoryCollections = new THashMap<String, MongoCollectionDecorator<String>>();
 
 	private static THashMap<String, THashSet<String>> inmemoryCollectionRecordIDs = new THashMap<String, THashSet<String>>();
 	private static THashMap<String, THashSet<String>> inmemoryRequestRecordIDs = new THashMap<String, THashSet<String>>();
 
 	Mongo mongo = null;
 	private DB db = null;
 
 	private Datastore ds = null;
 
 	private EngineStatus status = EngineStatus.STOPPED;
 
 	private String dbName;
 
 	/**
 	 * @param dbName
 	 */
 	public MongoStorageEngine(String dbName) {
 		this.dbName = dbName;
 	}
 
 	/**
 	 * Default constructor
 	 */
 	public MongoStorageEngine() {
 		this.dbName = "UIM";
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see eu.europeana.uim.api.StorageEngine#getIdentifier()
 	 */
 	public String getIdentifier() {
 		return MongoStorageEngine.class.getSimpleName();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see eu.europeana.uim.api.StorageEngine#setConfiguration(java.util.Map)
 	 */
 	public void setConfiguration(Map<String, String> config) {
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see eu.europeana.uim.api.StorageEngine#getConfiguration()
 	 */
 	public Map<String, String> getConfiguration() {
 		return new HashMap<String, String>();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see eu.europeana.uim.api.StorageEngine#initialize()
 	 */
 	public void initialize() {
 		try {
 			if (dbName == null) {
 				dbName = DEFAULT_UIM_DB_NAME;
 			}
 			status = EngineStatus.BOOTING;
 			mongo = new Mongo();
 			db = mongo.getDB(dbName);
 			Morphia morphia = new Morphia();
 			morphia.map(MongoProviderDecorator.class)
 					.map(MongoExecutionDecorator.class)
 					.map(MongoCollectionDecorator.class)
 					.map(MongoRequestDecorator.class)
 					.map(MongoMetadataRecordDecorator.class)
 					.map(MDRPerCollectionAggregator.class)
 					.map(MDRPerRequestAggregator.class);
 			ds = morphia.createDatastore(mongo, dbName);
 			ensureIndexes();
 
 			status = EngineStatus.RUNNING;
 
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void ensureIndexes() {
 		// Ensuring indexes through Morphia is not an option as generics in
 		// AbstractEntityBean cause mapping exceptions
 		mongo.getDB(DEFAULT_UIM_DB_NAME)
 				.getCollection("MongoProviderDecorator").ensureIndex(NAMEFIELD);
 		mongo.getDB(DEFAULT_UIM_DB_NAME)
 				.getCollection("MongoProviderDecorator")
 				.ensureIndex(MNEMONICFIELD);
 		mongo.getDB(DEFAULT_UIM_DB_NAME)
 				.getCollection("MongoCollectionDecorator")
 				.ensureIndex(NAMEFIELD);
 		mongo.getDB(DEFAULT_UIM_DB_NAME)
 				.getCollection("MongoCollectionDecorator")
 				.ensureIndex(MNEMONICFIELD);
 		mongo.getDB(DEFAULT_UIM_DB_NAME).getCollection("MongoRequestDecorator")
 				.ensureIndex(SEARCHDATE);
 		mongo.getDB(DEFAULT_UIM_DB_NAME).getCollection("MongoRequestDecorator")
 				.ensureIndex(COLLECTIONID);
 		mongo.getDB(DEFAULT_UIM_DB_NAME)
 				.getCollection("MongoMetadataRecordDecorator")
 				.ensureIndex(COLLECTIONID);
 		mongo.getDB(DEFAULT_UIM_DB_NAME)
 				.getCollection("MongoMetadataRecordDecorator")
 				.ensureIndex(RECORDUIDFIELD);
 		mongo.getDB(DEFAULT_UIM_DB_NAME)
 				.getCollection("MDRPerCollectionAggregator")
 				.ensureIndex(COLLECTIONID);
 		mongo.getDB(DEFAULT_UIM_DB_NAME)
 				.getCollection("MDRPerRequestAggregator")
 				.ensureIndex(REQUESTID);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see eu.europeana.uim.api.StorageEngine#checkpoint()
 	 */
 	@Override
 	public void checkpoint() {
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see eu.europeana.uim.api.StorageEngine#command(java.lang.String)
 	 */
 	@Override
 	public void command(String command) {
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see eu.europeana.uim.api.StorageEngine#completed(eu.europeana.uim.api.
 	 * ExecutionContext)
 	 */
 	@Override
 	public void completed(ExecutionContext<?, String> context) {
		if(context.getDataSet() instanceof Collection){
			Collection<?> coll = (Collection<?>) context.getDataSet();
			flushCollectionMDRS(coll.getMnemonic());
		}
		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see eu.europeana.uim.api.StorageEngine#shutdown()
 	 */
 	public void shutdown() {
 		status = EngineStatus.STOPPED;
 	}
 
 	/**
 	 * @return
 	 */
 	public String getDbName() {
 		return dbName;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see eu.europeana.uim.api.StorageEngine#getStatus()
 	 */
 	public EngineStatus getStatus() {
 		return status;
 	}
 
 	/**
 	 * @return
 	 */
 	public long size() {
 		return 0;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see eu.europeana.uim.api.StorageEngine#createProvider()
 	 */
 	@Override
 	public Provider<String> createProvider() {
 		MongoProviderDecorator<String> p = new MongoProviderDecorator<String>();
 		ds.save(p);
 		return p.getEmbeddedProvider();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * eu.europeana.uim.api.StorageEngine#updateProvider(eu.europeana.uim.store
 	 * .Provider)
 	 */
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	@Override
 	public void updateProvider(Provider<String> provider)
 			throws StorageEngineException {
 
 		provider = (MongoProviderDecorator<String>) ensureConsistency(provider);
 
 		ArrayList<MongoProviderDecorator> allresults = new ArrayList<MongoProviderDecorator>();
 		ArrayList<MongoProviderDecorator> result1 = (ArrayList<MongoProviderDecorator>) ds
 				.find(MongoProviderDecorator.class)
 				.filter(NAMEFIELD, provider.getName()).asList();
 		ArrayList<MongoProviderDecorator> result2 = (ArrayList<MongoProviderDecorator>) ds
 				.find(MongoProviderDecorator.class)
 				.filter(MNEMONICFIELD, provider.getMnemonic()).asList();
 
 		allresults.addAll(result1);
 		allresults.addAll(result2);
 
 		for (MongoProviderDecorator<String> p : allresults) {
 			if (p.getName() != null
 					&& (p.getName().equals(provider.getName()) || p
 							.getMnemonic().equals(provider.getMnemonic()))
 					&& !p.getId().equals(provider.getId())) {
 				throw new StorageEngineException("Provider with name '"
 						+ provider.getName() + "' already exists");
 			}
 			if (p.getMnemonic() != null
 					&& p.getMnemonic().equals(provider.getMnemonic())
 					&& !p.getId().equals(provider.getId())) {
 				throw new StorageEngineException("Provider with mnemonic '"
 						+ provider.getMnemonic() + "' already exists");
 			}
 		}
 
 		for (Provider<String> related : provider.getRelatedOut()) {
 			if (!related.getRelatedIn().contains(provider)) {
 				related.getRelatedIn().add(provider);
 				ds.merge(related);
 			}
 		}
 		for (Provider<String> related : provider.getRelatedIn()) {
 			if (!related.getRelatedOut().contains(provider)) {
 				related.getRelatedOut().add(provider);
 				ds.merge(related);
 			}
 		}
 
 		ds.merge(provider);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see eu.europeana.uim.api.StorageEngine#getProvider(java.lang.Object)
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public Provider<String> getProvider(String id) {
 
 		MongoProviderDecorator<String> prov = ds
 				.find(MongoProviderDecorator.class)
 				.filter(LOCALIDFIELD, new ObjectId(id)).get();
 
 		Provider<String> retprov = prov != null ? prov.getEmbeddedProvider()
 				: null;
 
 		return retprov;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see eu.europeana.uim.api.StorageEngine#findProvider(java.lang.String)
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public Provider<String> findProvider(String mnemonic) {
 		MongoProviderDecorator<String> prov = ds
 				.find(MongoProviderDecorator.class).field(MNEMONICFIELD)
 				.equal(mnemonic).get();
 
 		Provider<String> retprov = prov != null ? prov.getEmbeddedProvider()
 				: null;
 
 		return retprov;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see eu.europeana.uim.api.StorageEngine#getAllProviders()
 	 */
 	@Override
 	public List<Provider<String>> getAllProviders() {
 		List<Provider<String>> res = new ArrayList<Provider<String>>();
 
 		@SuppressWarnings("rawtypes")
 		List<MongoProviderDecorator> decs = ds.find(
 				MongoProviderDecorator.class).asList();
 
 		for (MongoProviderDecorator<String> p : decs) {
 			res.add(p.getEmbeddedProvider());
 		}
 		return res;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * eu.europeana.uim.api.StorageEngine#createCollection(eu.europeana.uim.
 	 * store.Provider)
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public Collection<String> createCollection(Provider<String> provider) {
 		provider = (Provider<String>) ensureConsistency(provider);
 		MongoCollectionDecorator<String> c = new MongoCollectionDecorator<String>(
 				provider);
 		ds.save(c);
 		inmemoryCollections.put(c.getId(), c);
 		return c.getEmbeddedCollection();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * eu.europeana.uim.api.StorageEngine#updateCollection(eu.europeana.uim.
 	 * store.Collection)
 	 */
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	@Override
 	public void updateCollection(Collection<String> collection)
 			throws StorageEngineException {
 
 		collection = (Collection<String>) ensureConsistency(collection);
 
 		ArrayList<MongoCollectionDecorator> allresults = new ArrayList<MongoCollectionDecorator>();
 
 		ArrayList<MongoCollectionDecorator> result1 = (ArrayList<MongoCollectionDecorator>) ds
 				.find(MongoCollectionDecorator.class)
 				.filter(NAMEFIELD, collection.getName()).asList();
 		ArrayList<MongoCollectionDecorator> result2 = (ArrayList<MongoCollectionDecorator>) ds
 				.find(MongoCollectionDecorator.class)
 				.filter(MNEMONICFIELD, collection.getMnemonic()).asList();
 
 		allresults.addAll(result1);
 		allresults.addAll(result2);
 
 		for (Collection<String> c : allresults) {
 			if (c.getName() != null
 					&& (c.getName().equals(collection.getName()))
 					&& !c.getId().equals(collection.getId())) {
 				throw new StorageEngineException("Collection with name '"
 						+ collection.getMnemonic() + "' already exists");
 			}
 			if (c.getMnemonic() != null
 					&& c.getMnemonic().equals(collection.getMnemonic())
 					&& !c.getId().equals(collection.getId())) {
 				throw new StorageEngineException("Collection with mnemonic '"
 						+ collection.getMnemonic() + "' already exists");
 			}
 
 		}
 		inmemoryCollections.put(collection.getId(),
 				(MongoCollectionDecorator<String>) collection);
 		ds.merge(collection);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see eu.europeana.uim.api.StorageEngine#getCollection(java.lang.Object)
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public Collection<String> getCollection(String id) {
 
 		Collection<String> retcoll = null;
 
 		// System.out.println("Size is: " + inmemoryCollections.size());
 		MongoCollectionDecorator<String> coll = inmemoryCollections.get(id);
 
 		if (coll == null) {
 			ObjectId objid = ObjectId.massageToObjectId(id);
 			coll = ds.find(MongoCollectionDecorator.class)
 					.filter(LOCALIDFIELD, objid).get();
 			inmemoryCollections.put(coll.getId(), coll);
 		}
 
 		retcoll = (coll == null) ? null : coll.getEmbeddedCollection();
 
 		return retcoll;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see eu.europeana.uim.api.StorageEngine#findCollection(java.lang.String)
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public Collection<String> findCollection(String mnemonic) {
 		@SuppressWarnings("rawtypes")
 		MongoCollectionDecorator coll = ds.find(MongoCollectionDecorator.class)
 				.filter(MNEMONICFIELD, mnemonic).get();
 
 		Collection<String> retcoll = (coll == null) ? coll : coll
 				.getEmbeddedCollection();
 
 		if (coll != null) {
 			synchronized (inmemoryCollections) {
 				inmemoryCollections.put(coll.getId(), coll);
 			}
 		}
 
 		return retcoll;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * eu.europeana.uim.api.StorageEngine#getCollections(eu.europeana.uim.store
 	 * .Provider)
 	 */
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<Collection<String>> getCollections(Provider<String> provider) {
 		provider = (Provider<String>) ensureConsistency(provider);
 		List<Collection<String>> res = new ArrayList<Collection<String>>();
 		for (MongoCollectionDecorator<String> c : ds
 				.find(MongoCollectionDecorator.class)
 				.filter("provider", provider).asList()) {
 			res.add(c.getEmbeddedCollection());
 		}
 		return res;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see eu.europeana.uim.api.StorageEngine#getAllCollections()
 	 */
 	@Override
 	public List<Collection<String>> getAllCollections() {
 		List<Collection<String>> res = new ArrayList<Collection<String>>();
 		for (MongoCollectionDecorator<String> c : ds.find(
 				MongoCollectionDecorator.class).asList()) {
 			res.add(c.getEmbeddedCollection());
 		}
 		return res;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * eu.europeana.uim.api.StorageEngine#createRequest(eu.europeana.uim.store
 	 * .Collection, java.util.Date)
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public Request<String> createRequest(Collection<String> collection,
 			Date date) throws StorageEngineException {
 		collection = (Collection<String>) ensureConsistency(collection);
 		MongoRequestDecorator<String> r = new MongoRequestDecorator<String>(
 				(MongoCollectionDecorator<String>) collection, date);
 		ds.save(r);
 		return r.getEmbeddedRequest();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * eu.europeana.uim.api.StorageEngine#updateRequest(eu.europeana.uim.store
 	 * .Request)
 	 */
 	@Override
 	public void updateRequest(Request<String> request)
 			throws StorageEngineException {
 
 		@SuppressWarnings("unchecked")
 		MongoRequestDecorator<String> request2 = (MongoRequestDecorator<String>) ensureConsistency(request);
 
 		for (MongoRequestDecorator<?> r : ds.find(MongoRequestDecorator.class)
 				.filter("collectionID", request2.getCollectionID()).asList()) {
 			if (r.getDate().equals(request.getDate())
 					&& !r.getId().equals(request2.getId())) {
 				String unique = "REQUEST/"
 						+ request2.getCollection().getMnemonic() + "/"
 						+ request2.getDate();
 				throw new IllegalStateException(
 						"Duplicate unique key for request: <" + unique + ">");
 			}
 		}
 
 		ds.merge(request2);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * eu.europeana.uim.api.StorageEngine#getRequests(eu.europeana.uim.store
 	 * .Collection)
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<Request<String>> getRequests(Collection<String> collection) {
 		collection = (Collection<String>) ensureConsistency(collection);
 
 		List<Request<String>> res = new ArrayList<Request<String>>();
 		for (MongoRequestDecorator<String> r : ds
 				.find(MongoRequestDecorator.class)
 				.filter("collectionID", collection.getId()).asList()) {
 			res.add(r.getEmbeddedRequest());
 		}
 		return res;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see eu.europeana.uim.api.StorageEngine#getRequest(java.lang.Object)
 	 */
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public Request<String> getRequest(String id) throws StorageEngineException {
 		MongoRequestDecorator<?> req = ds.find(MongoRequestDecorator.class)
 				.filter(LOCALIDFIELD, new ObjectId(id)).get();
 
 		@SuppressWarnings("rawtypes")
 		MongoCollectionDecorator coll = (MongoCollectionDecorator<?>) ensureConsistency(getCollection(req
 				.getCollectionID()));
 
 		req.setCollectionReference(coll);
 
 		return req.getEmbeddedRequest();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * eu.europeana.uim.api.StorageEngine#getRequests(eu.europeana.uim.store
 	 * .MetaDataRecord)
 	 */
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	@Override
 	public List<Request<String>> getRequests(MetaDataRecord<String> mdr)
 			throws StorageEngineException {
 
 		mdr = (MetaDataRecord<String>) ensureConsistency(mdr);
 
 		List<Request<String>> requests = new ArrayList<Request<String>>();
 		List<MongoRequestDecorator> results = ds
 				.find(MongoRequestDecorator.class).filter(REQUESTRECORDS, mdr)
 				.asList();
 		for (MongoRequestDecorator res : results) {
 
 			requests.add(res.getEmbeddedRequest());
 		}
 		return requests;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * eu.europeana.uim.api.StorageEngine#addRequestRecord(eu.europeana.uim.
 	 * store.Request, eu.europeana.uim.store.MetaDataRecord)
 	 */
 	@Override
 	public void addRequestRecord(Request<String> request,
 			MetaDataRecord<String> record) throws StorageEngineException {
 
 		MongoMetadataRecordDecorator<String> rec = (MongoMetadataRecordDecorator<String>) record;
 
 		THashSet<String> registeredRequestrecords = inmemoryRequestRecordIDs
 				.get(request.getId());
 
 		if (registeredRequestrecords == null) {
 			registeredRequestrecords = new THashSet<String>();
 
 			synchronized (inmemoryRequestRecordIDs) {
 				inmemoryRequestRecordIDs.put(request.getId(),
 						registeredRequestrecords);
 			}
 		}
 
 		registeredRequestrecords.add(rec.getId());
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * eu.europeana.uim.api.StorageEngine#createMetaDataRecord(eu.europeana.
 	 * uim.store.Collection, java.lang.String)
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public MetaDataRecord<String> createMetaDataRecord(
 			Collection<String> collection, String identifier)
 			throws StorageEngineException {
 		collection = (Collection<String>) ensureConsistency(collection);
 		MongoMetadataRecordDecorator<String> mdr = new MongoMetadataRecordDecorator<String>(
 				(MongoCollectionDecorator<String>) collection, identifier);
 		return mdr;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * eu.europeana.uim.api.StorageEngine#updateMetaDataRecord(eu.europeana.
 	 * uim.store.MetaDataRecord)
 	 */
 	@Override
 	public void updateMetaDataRecord(MetaDataRecord<String> record)
 			throws StorageEngineException {
 		MongoMetadataRecordDecorator<String> rec = (MongoMetadataRecordDecorator<String>) record;
 		if (rec.getMongoId() == null) {
 			ds.save(record);
 		} else {
 			ds.merge(record);
 		}
 
 		THashSet<String> registeredCollectionrecords = inmemoryCollectionRecordIDs
 				.get(rec.getCollectionID());
 
 		if (registeredCollectionrecords == null) {
 			registeredCollectionrecords = new THashSet<String>();
 			inmemoryCollectionRecordIDs.put(rec.getCollectionID(),
 					registeredCollectionrecords);
 		}
 
 		synchronized (inmemoryCollectionRecordIDs) {
 			registeredCollectionrecords.add(rec.getId());
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * eu.europeana.uim.api.StorageEngine#createExecution(eu.europeana.uim.store
 	 * .UimDataSet, java.lang.String)
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public Execution<String> createExecution(UimDataSet<String> entity,
 			String workflow) throws StorageEngineException {
 		MongoExecutionDecorator<String> me = new MongoExecutionDecorator<String>();
 
 		entity = (UimDataSet<String>) ensureConsistency(entity);
 		me.setDataSet(entity);
 		me.setWorkflow(workflow);
 		ds.save(me);
 		return me.getEmbeddedExecution();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * eu.europeana.uim.api.StorageEngine#updateExecution(eu.europeana.uim.store
 	 * .Execution)
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public void updateExecution(Execution<String> execution)
 			throws StorageEngineException {
 		execution = (MongoExecutionDecorator<String>) ensureConsistency(execution);
 		ds.merge(execution);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see eu.europeana.uim.api.StorageEngine#getAllExecutions()
 	 */
 	@Override
 	public List<Execution<String>> getAllExecutions() {
 		List<Execution<String>> res = new ArrayList<Execution<String>>();
 		for (MongoExecutionDecorator<String> e : ds.find(
 				MongoExecutionDecorator.class).asList()) {
 			res.add(e.getEmbeddedExecution());
 		}
 		return res;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * eu.europeana.uim.api.StorageEngine#getMetaDataRecords(java.util.List)
 	 */
 	@Override
 	public List<MetaDataRecord<String>> getMetaDataRecords(List<String> ids) {
 		ArrayList<MetaDataRecord<String>> res = new ArrayList<MetaDataRecord<String>>();
 
 		for (String id : ids) {
 
 			try {
 				MetaDataRecord<String> mdr = getMetaDataRecord(id);
 				if (mdr != null) {
 					res.add(mdr);
 				}
 			} catch (StorageEngineException e) {
 				e.printStackTrace();
 			}
 		}
 		return res;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * eu.europeana.uim.api.StorageEngine#getMetaDataRecord(java.lang.Object)
 	 */
 	@Override
 	public MetaDataRecord<String> getMetaDataRecord(String id)
 			throws StorageEngineException {
 		@SuppressWarnings("unchecked")
 		MongoMetadataRecordDecorator<String> mdr = ds
 				.find(MongoMetadataRecordDecorator.class)
 				.filter(RECORDUIDFIELD, id).get();
 
 		if (mdr != null) {
 			@SuppressWarnings("unchecked")
 			MongoCollectionDecorator<String> coll = (MongoCollectionDecorator<String>) ensureConsistency(getCollection(mdr
 					.getCollectionID()));
 			mdr.setCollectionDecorator(coll);
 		}
 		return mdr;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * eu.europeana.uim.api.StorageEngine#getByRequest(eu.europeana.uim.store
 	 * .Request)
 	 */
 	@Override
 	public String[] getByRequest(Request<String> request) {
 
 		inmemoryRequestRecordIDs.get(request.getId());
 
 		String[] res = null;
 
 		THashSet<String> idrefs = inmemoryRequestRecordIDs.get(request.getId());
 
 		if (idrefs != null) {
 			res = idrefs.toArray(new String[0]);
 		} else {
 			MDRPerRequestAggregator aggregator = ds
 					.find(MDRPerRequestAggregator.class)
 					.filter("requestId", request.getId()).get();
 
 			if (aggregator != null) {
 				HashSet<String> entries = aggregator.getMdrIDs();
 
 				THashSet<String> idrefsnew = new THashSet<String>(entries);
 				synchronized (inmemoryRequestRecordIDs) {
 					inmemoryRequestRecordIDs.put(request.getId(), idrefsnew);
 				}
 
 				res = idrefsnew.toArray(new String[0]);
 			} else {
 				res = new String[0];
 			}
 
 		}
 
 		return res;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * eu.europeana.uim.api.StorageEngine#getByCollection(eu.europeana.uim.store
 	 * .Collection)
 	 */
 	@Override
 	public String[] getByCollection(Collection<String> collection) {
 
 		String[] res = null;
 
 		THashSet<String> idrefs = inmemoryCollectionRecordIDs.get(collection
 				.getId());
 
 		if (idrefs != null) {
 			res = idrefs.toArray(new String[0]);
 		} else {
 			MDRPerCollectionAggregator aggregator = ds
 					.find(MDRPerCollectionAggregator.class)
 					.filter("collectionId", collection.getId()).get();
 
 			if (aggregator != null) {
 				HashSet<String> entries = aggregator.getMdrIDs();
 
 				THashSet<String> idrefsnew = new THashSet<String>(entries);
 				synchronized (inmemoryCollectionRecordIDs) {
 					inmemoryCollectionRecordIDs.put(collection.getId(),
 							idrefsnew);
 				}
 
 				res = idrefsnew.toArray(new String[0]);
 			} else {
 				res = new String[0];
 			}
 
 		}
 
 		return res;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * eu.europeana.uim.api.StorageEngine#getByProvider(eu.europeana.uim.store
 	 * .Provider, boolean)
 	 */
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	@Override
 	public String[] getByProvider(Provider<String> provider, boolean recursive) {
 		provider = (Provider<String>) ensureConsistency(provider);
 		ArrayList<String> vals = new ArrayList<String>();
 
 		List<MongoCollectionDecorator> mongoCollections = ds
 				.find(MongoCollectionDecorator.class)
 				.filter("provider", provider).asList();
 
 		for (MongoCollectionDecorator<String> p : mongoCollections) {
 			String[] tmp = getByCollection(p);
 
 			for (int i = 0; i < tmp.length; i++) {
 				vals.add(tmp[i]);
 			}
 		}
 
 		if (recursive == true) {
 
 			Set<Provider<String>> relin = provider.getRelatedOut();
 
 			for (Provider<String> relpr : relin) {
 				String[] tmp = getByProvider(relpr, false);
 				for (int i = 0; i < tmp.length; i++) {
 					vals.add(tmp[i]);
 				}
 			}
 		}
 
 		return vals.toArray(new String[vals.size()]);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see eu.europeana.uim.api.StorageEngine#getAllIds()
 	 */
 	@Override
 	public String[] getAllIds() {
 		THashSet<String> results = new THashSet<String>();
 
 		for (MongoCollectionDecorator<String> c : ds.find(
 				MongoCollectionDecorator.class).asList()) {
 
 			String[] res = getByCollection(c);
 			for (int i = 0; i < res.length; i++) {
 				results.add(res[i]);
 			}
 		}
 
 		return results.toArray(new String[0]);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * eu.europeana.uim.api.StorageEngine#getTotalByRequest(eu.europeana.uim
 	 * .store.Request)
 	 */
 	@Override
 	public int getTotalByRequest(Request<String> request) {
 		String[] recs = getByRequest(request);
 		return recs.length;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * eu.europeana.uim.api.StorageEngine#getTotalByCollection(eu.europeana.
 	 * uim.store.Collection)
 	 */
 	@Override
 	public int getTotalByCollection(Collection<String> collection) {
 		String[] recs = getByCollection(collection);
 		return recs.length;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * eu.europeana.uim.api.StorageEngine#getTotalByProvider(eu.europeana.uim
 	 * .store.Provider, boolean)
 	 */
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	@Override
 	public int getTotalByProvider(Provider<String> provider, boolean recursive) {
 		provider = (Provider<String>) ensureConsistency(provider);
 		int recordcounter = 0;
 
 		List<MongoCollectionDecorator> mongoCollections = ds
 				.find(MongoCollectionDecorator.class)
 				.filter("provider", provider).asList();
 
 		for (MongoCollectionDecorator<String> p : mongoCollections) {
 			int tmp = getTotalByCollection(p);
 			recordcounter = recordcounter + tmp;
 		}
 
 		return recordcounter;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see eu.europeana.uim.api.StorageEngine#getTotalForAllIds()
 	 */
 	@Override
 	public int getTotalForAllIds() {
 		int recordcounter = 0;
 
 		@SuppressWarnings("rawtypes")
 		List<MongoCollectionDecorator> mongoCollections = ds.find(
 				MongoCollectionDecorator.class).asList();
 
 		for (MongoCollectionDecorator<String> p : mongoCollections) {
 			int tmp = getTotalByCollection(p);
 			recordcounter = recordcounter + tmp;
 		}
 
 		return recordcounter;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see eu.europeana.uim.api.StorageEngine#getExecution(java.lang.Object)
 	 */
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	@Override
 	public Execution<String> getExecution(String id)
 			throws StorageEngineException {
 		MongoExecutionDecorator exec = ds.find(MongoExecutionDecorator.class,
 				LOCALIDFIELD, new ObjectId(id)).get();
 
 		return exec.getEmbeddedExecution();
 	}
 
 	/**
 	 * @param collectionID
 	 */
 	public void flushCollectionMDRS(String collectionID) {
 
 		THashSet<String> recids;
 		synchronized (inmemoryCollectionRecordIDs) {
 			recids = inmemoryCollectionRecordIDs.get(collectionID);
 		}
 		if (recids != null) {
 			HashSet<String> tmpset = new HashSet<String>();
 			TObjectHashIterator<String> it = recids.iterator();
 			while (it.hasNext()) {
 				tmpset.add(it.next());
 			}
 			MDRPerCollectionAggregator aggregator = ds
 					.find(MDRPerCollectionAggregator.class)
 					.filter("collectionId", collectionID).get();
 			if (aggregator == null) {
 				aggregator = new MDRPerCollectionAggregator();
 				aggregator.setCollectionId(collectionID);
 				aggregator.setMdrIDs(tmpset);
 				ds.save(aggregator);
 			} else {
 				aggregator.setMdrIDs(tmpset);
 				ds.merge(aggregator);
 			}
 		}
 
 	}
 
 	/**
 	 * @param collectionID
 	 */
 	public void flushRequestMDRS(String requestID) {
 		THashSet<String> recids;
 
 		recids = inmemoryRequestRecordIDs.get(requestID);
 		if (recids != null) {
 			HashSet<String> tmpset = new HashSet<String>();
 			TObjectHashIterator<String> it = recids.iterator();
 			while (it.hasNext()) {
 				tmpset.add(it.next());
 			}
 			MDRPerRequestAggregator aggregator = ds
 					.find(MDRPerRequestAggregator.class)
 					.filter("requestId", requestID).get();
 			if (aggregator == null) {
 				aggregator = new MDRPerRequestAggregator();
 				aggregator.setRequestId(requestID);
 				aggregator.setMdrIDs(tmpset);
 				ds.save(aggregator);
 			} else {
 				aggregator.setMdrIDs(tmpset);
 				ds.merge(aggregator);
 			}
 		}
 
 	}
 
 }
