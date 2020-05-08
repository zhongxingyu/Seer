 package nz.ac.vuw.ecs.rprofs.server.db;
 
 import java.util.*;
 
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 import com.mongodb.*;
 import javax.validation.constraints.NotNull;
 import nz.ac.vuw.ecs.rprofs.Context;
 import nz.ac.vuw.ecs.rprofs.server.data.util.*;
 import nz.ac.vuw.ecs.rprofs.server.domain.*;
 import nz.ac.vuw.ecs.rprofs.server.domain.id.*;
 import nz.ac.vuw.ecs.rprofs.server.model.DataObject;
 import nz.ac.vuw.ecs.rprofs.server.model.Id;
 import nz.ac.vuw.ecs.rprofs.server.reports.MapReduce;
 import nz.ac.vuw.ecs.rprofs.server.reports.ReduceStore;
 import nz.ac.vuw.ecs.rprofs.server.reports.Reducer;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Author: Stephen Nelson <stephen@sfnelson.org>
  * Date: 9 September, 2011
  */
 @Singleton
 public class Database {
 
 	private final Logger log = LoggerFactory.getLogger(Database.class);
 
 	private final Mongo mongo;
 	private Map<DatasetId, Dataset> datasets;
 
 	@Inject
 	Database(@NotNull Mongo mongo) {
 		this.mongo = mongo;
 		this.datasets = Maps.newHashMap();
 	}
 
 	public DatasetCreator<?> getDatasetCreator() {
 		return createDatasetBuilder();
 	}
 
 	public DatasetUpdater<?> getDatasetUpdater() {
 		return createDatasetBuilder();
 	}
 
 	public DatasetQuery<?> getDatasetQuery() {
 		return createDatasetBuilder();
 	}
 
 	public ClazzCreator<?> getClazzCreator() {
 		return createClassBuilder();
 	}
 
 	public ClazzUpdater<?> getClazzUpdater() {
 		return createClassBuilder();
 	}
 
 	public ClazzQuery<?> getClazzQuery() {
 		return createClassBuilder();
 	}
 
 	public EventCreator<?> getEventCreater() {
 		return createEventBuilder();
 	}
 
 	public EventUpdater<?> getEventUpdater() {
 		return createEventBuilder();
 	}
 
 	public EventQuery<?> getEventQuery() {
 		return createEventBuilder();
 	}
 
 	public FieldQuery<?> getFieldQuery() {
 		return createFieldQuery();
 	}
 
 	public MethodQuery<?> getMethodQuery() {
 		return createMethodQuery();
 	}
 
 	public InstanceCreator<?> getInstanceCreator() {
 		return createInstanceBuilder();
 	}
 
 	public InstanceQuery<?> getInstanceQuery() {
 		return createInstanceBuilder();
 	}
 
 	public InstanceUpdater<?> getInstanceUpdater() {
 		return createInstanceBuilder();
 	}
 
 	public ClassSummaryCreator<?> getClassSummaryCreator() {
 		return createClassSummaryBuilder();
 	}
 
 	public ClassSummaryQuery<?> getClassSummaryQuery() {
 		return createClassSummaryBuilder();
 	}
 
 	public ClassSummaryUpdater getClassSummaryUpdater() {
 		return createClassSummaryBuilder();
 	}
 
 	public FieldSummaryCreator<?> getFieldSummaryCreator() {
 		return createFieldSummaryBuilder();
 	}
 
 	public FieldSummaryQuery<?> getFieldSummaryQuery() {
 		return createFieldSummaryBuilder();
 	}
 
 	public FieldSummaryUpdater<?> getFieldSummaryUpdater() {
 		return createFieldSummaryBuilder();
 	}
 
 	public RequestCreator<?> getRequestCreator() {
 		return createRequestBuilder();
 	}
 
 	public RequestQuery<?> getRequestQuery() {
 		return createRequestBuilder();
 	}
 
 	public RequestUpdater<?> getRequestUpdater() {
 		return createRequestBuilder();
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<String> findPackages() {
 		DBCollection classes = getCollection(Clazz.class);
 		return (List<String>) classes.distinct("package");
 	}
 
 	public long countPackages() {
 		DBCollection classes = getCollection(Clazz.class);
 		return classes.distinct("package").size();
 	}
 
 	@SuppressWarnings("unchecked")
 	public List<? extends InstanceId> findThreads() {
 		DBCollection events = getCollection(Event.class);
 		List<InstanceId> instances = Lists.newArrayList();
 		for (Long id : (List<Long>) events.distinct("thread")) {
 			instances.add(new InstanceId(id));
 		}
 		return instances;
 	}
 
 	public long countThreads() {
 		DBCollection events = getCollection(Event.class);
 		return events.distinct("thread").size();
 	}
 
 	@SuppressWarnings("unchecked")
 	public <I extends Id<I, T>, T extends DataObject<I, T>> T findEntity(@NotNull I id) {
 		if (Dataset.class.equals(id.getTargetClass())) {
 			if (datasets.containsKey(id)) {
 				Dataset ds = datasets.get(id);
 				ds = updateDataset(ds);
 				return id.getTargetClass().cast(ds);
 			}
 			for (Dataset ds : getDatasets()) {
 				if (ds.getId().equals(id)) {
 					return id.getTargetClass().cast(ds);
 				}
 			}
 			return null;
 		} else {
 			Dataset current = Context.getDataset();
 			DB db = getDatabase(current);
 			DBCollection collection = getCollection(db, id.getTargetClass());
 			DBObject data = collection.findOne(new BasicDBObject("_id", id.getValue()));
 			if (data == null) return null;
 			else return getBuilder(id.getTargetClass()).init(data).get();
 		}
 	}
 
 	public <T extends DataObject> boolean deleteEntity(T entity) {
 
 		Dataset dataset;
 		if (entity.getClass() == Dataset.class) {
 			dataset = (Dataset) entity;
 			datasets.remove(entity.getId());
 		} else {
 			dataset = Context.getDataset();
 		}
 
 		if (dataset == null) throw new RuntimeException("no dataset in context");
 
 		DB database = getDatabase(dataset);
 
 		if (database == null) throw new RuntimeException("invalid dataset provided");
 
 		DBCollection collection = getCollection(database, entity.getClass());
 		collection.remove(new BasicDBObject("_id", entity.getId().getValue()));
 
 		if (entity.getClass() == Dataset.class) {
 			log.info("deleting database {}", ((Dataset) entity).getDatasetHandle());
 			mongo.dropDatabase(((Dataset) entity).getDatasetHandle());
 		}
 
 		return true;
 	}
 
 	public <Iid extends Id<Iid, I>, I extends DataObject<Iid, I>, Oid extends Id<Oid, O>, O extends DataObject<Oid, O>>
 	Runnable createMapReduce(Class<I> input, Class<O> output, MapReduce<I, Oid, O> mr) {
 		return createMapReduce(input, output, mr, null, false);
 	}
 
 	public <Iid extends Id<Iid, I>, I extends DataObject<Iid, I>, Oid extends Id<Oid, O>, O extends DataObject<Oid, O>>
 	Runnable createMapReduce(Class<I> input, Class<O> output, MapReduce<I, Oid, O> mr, String name, boolean lock) {
 		ReduceStore<Oid, O> store = createReducer(output, mr, name, lock);
 		getCollection(output).drop();
 		Query<Iid, I> query = getBuilder(input);
 		return new MongoMapper<I, Oid, O>(query, mr, store);
 	}
 
 	public <I extends Id<I, T>, T extends DataObject<I, T>>
 	ReduceStore<I, T> createReducer(Class<T> type, Reducer<I, T> reducer, String name, boolean lock) {
 		DB db = getDatabase();
 		DBCollection c = getCollection(type);
 		EntityBuilder<?, I, T> builder = getBuilder(type);
 		return new MongoReduceStore<I, T>(this, db, c, builder, reducer, name, lock);
 	}
 
 	public void flush() {
 		mongo.fsync(false);
 	}
 
 	DB getDatabase() {
 		Dataset current = Context.getDataset();
 		if (current != null) return getDatabase(current);
 		else return null;
 	}
 
 	private DB getDatabase(@NotNull Dataset dataset) {
 		return mongo.getDB(dataset.getDatasetHandle());
 	}
 
 	DBCollection getCollection(Class<? extends DataObject> type) {
 		DB root = getDatabase();
 		if (root != null) return getCollection(root, type);
 		else return null;
 	}
 
 	private DBCollection getCollection(Dataset dataset, Class<? extends DataObject> type) {
 		DB root = getDatabase(dataset);
 		if (root != null) return getCollection(root, type);
 		else return null;
 	}
 
 	private DBCollection getCollection(DB root, Class<? extends DataObject> type) {
 		if (type == Dataset.class) {
 			return root.getCollection("properties");
 		} else if (type == Clazz.class) {
 			return root.getCollection("classes");
 		} else if (type == Method.class) {
 			return root.getCollection("methods");
 		} else if (type == Field.class) {
 			return root.getCollection("fields");
 		} else if (type == Instance.class) {
 			return root.getCollection("instances");
 		} else if (type == Event.class) {
 			return root.getCollection("events");
 		} else if (type == ClassSummary.class) {
 			return root.getCollection("class.results");
 		} else if (type == FieldSummary.class) {
 			return root.getCollection("field.results");
 		} else if (type == Request.class) {
 			return root.getCollection("tmp.requests");
 		} else {
 			throw new RuntimeException("type not implemented: " + type);
 		}
 	}
 
 	synchronized boolean _lock(DB root, DBCollection collection, String owner) throws InterruptedException {
 		DBCollection locks = root.getCollection("locks");
 		DBObject query = new BasicDBObjectBuilder()
 				.add("_id", collection.getName())
 				.add("owner", new BasicDBObject("$exists", false)).get();
 		DBObject update = new BasicDBObject("$set", new BasicDBObjectBuilder()
 				.add("owner", owner)
 				.get());
 		DBObject inc = new BasicDBObject("$inc", new BasicDBObject("requests", 1));
 
 		// first, ensure the lock exists
 		locks.findAndModify(new BasicDBObject("_id", collection.getName()), null, null, false, inc, true, true);
 
 		log.debug("{} waiting on {}", owner, collection.getName());
 		int delay = 128;
 		while (true) {
 			DBObject lock = locks.findAndModify(query, null, null, false, update, true, false);
 			if (lock == null) {
 				log.trace("{} still waiting on {}", owner, collection.getName());
				wait(delay);
				delay = Math.min(delay * 2, 16384);
 			} else {
 				log.debug("{} has lock on {}", owner, collection.getName());
 				return true;
 			}
 		}
 	}
 
 	synchronized boolean _unlock(DB root, DBCollection collection, String owner) {
 		DBCollection locks = root.getCollection("locks");
 		DBObject query = new BasicDBObjectBuilder()
 				.add("_id", collection.getName())
 				.add("owner", owner).get();
 		DBObject update = new BasicDBObject("$unset", new BasicDBObject("owner", true));
 
 		DBObject lock = locks.findAndModify(query, null, null, false, update, true, false);
 
 		if (lock == null) return false;
 
 		log.debug("{} has released {}", owner, collection.getName());
 
 		notifyAll();
 
 		return true;
 	}
 
 	private List<Dataset> getDatasets() {
 		List<Dataset> result = Lists.newArrayList();
 		MongoDatasetBuilder builder = createDatasetBuilder();
 		for (String dbname : mongo.getDatabaseNames()) {
 			if (dbname.startsWith("rprof_")) {
 				DBObject properties = mongo.getDB(dbname)
 						.getCollection("properties").findOne();
 				if (properties == null) {
 					String[] args = dbname.split("_");
 					result.add(new Dataset(new DatasetId(Short.valueOf(args[2])),
 							args[1], new Date(), dbname));
 				} else {
 					result.add(builder.init(properties).get());
 				}
 			}
 		}
 		Map<DatasetId, Dataset> newDatasets = Maps.newHashMap();
 		for (Dataset ds : result) {
 			newDatasets.put(ds.getId(), ds);
 		}
 		this.datasets = newDatasets;
 		return result;
 	}
 
 	private Dataset updateDataset(Dataset dataset) {
 		DBObject properties = mongo.getDB(dataset.getDatasetHandle()).getCollection("properties").findOne();
 		return createDatasetBuilder().init(properties).get();
 	}
 
 	DBCollection getRawCollection(String name) {
 		DB db = getDatabase();
 		return db.getCollection(name);
 	}
 
 	@SuppressWarnings("unchecked")
 	private <I extends Id<I, T>, T extends DataObject<I, T>> EntityBuilder<?, I, T> getBuilder(@NotNull Class<T> type) {
 		if (type.equals(Dataset.class)) {
 			return EntityBuilder.class.cast(createDatasetBuilder());
 		} else if (type.equals(Event.class)) {
 			return EntityBuilder.class.cast(createEventBuilder());
 		} else if (type.equals(Clazz.class)) {
 			return EntityBuilder.class.cast(createClassBuilder());
 		} else if (type.equals(Field.class)) {
 			return EntityBuilder.class.cast(createFieldQuery());
 		} else if (type.equals(Method.class)) {
 			return EntityBuilder.class.cast(createMethodQuery());
 		} else if (type.equals(Instance.class)) {
 			return EntityBuilder.class.cast(createInstanceBuilder());
 		} else if (type.equals(ClassSummary.class)) {
 			return EntityBuilder.class.cast(createClassSummaryBuilder());
 		} else if (type.equals(FieldSummary.class)) {
 			return EntityBuilder.class.cast(createFieldSummaryBuilder());
 		} else if (type.equals(Request.class)) {
 			return EntityBuilder.class.cast(createRequestBuilder());
 		} else {
 			log.error("request for unavaible builder: {}", type);
 			return null;
 		}
 	}
 
 	private MongoDatasetBuilder createDatasetBuilder() {
 		return new MongoDatasetBuilder() {
 			@Override
 			DatasetId _createId() {
 				if (b.containsField("_id")) {
 					return new DatasetId((Long) b.get("_id"));
 				} else {
 					long max = 0;
 					for (String dbname : mongo.getDatabaseNames()) {
 						if (dbname.startsWith("rprof_")) {
 							DB db = mongo.getDB(dbname);
 							Scanner sc = new Scanner(dbname);
 							sc.useDelimiter("_");
 							short id;
 							if (sc.hasNext()) sc.next();
 							if (sc.hasNext()) sc.next();
 							if (sc.hasNextShort()) {
 								id = sc.nextShort();
 								if (id > max) max = id;
 							}
 						}
 					}
 					return new DatasetId(++max);
 				}
 			}
 
 			@Override
 			DBCollection _getCollection() {
 				return getCollection(Dataset.class);
 			}
 
 			@Override
 			public void update(DatasetId id) {
 				Context.setDataset(findEntity(id));
 				super.update(id);
 				Context.clear();
 			}
 
 			@Override
 			void _store(DBObject dataset) {
 				String dbname = "rprof_" + dataset.get("benchmark") + "_" + dataset.get("_id");
 				DB db = mongo.getDB(dbname);
 				dataset.put("version", 1);
 				dataset.put("handle", dbname);
 				db.getCollection("properties").insert(dataset);
 			}
 
 			@Override
 			public Cursor<? extends Dataset> find() {
 				final List<Dataset> result = Lists.newArrayList();
 				for (Dataset ds : getDatasets()) {
 					DBCursor c = getCollection(ds, Dataset.class).find(b);
 					if (c.hasNext()) {
 						result.add(ds);
 					}
 					c.close();
 				}
 				return new Cursor<Dataset>() {
 					final Iterator<Dataset> iterator = result.iterator();
 
 					@Override
 					public int count() {
 						return result.size();
 					}
 
 					@Override
 					public void close() {
 						// nothing to do.
 					}
 
 					@Override
 					public boolean hasNext() {
 						return iterator.hasNext();
 					}
 
 					@Override
 					public Dataset next() {
 						return iterator.next();
 					}
 
 					@Override
 					public void remove() {
 						throw new UnsupportedOperationException("not implemented");
 					}
 				};
 			}
 
 			@Override
 			long _count(DBObject query) {
 				return find().count();
 			}
 		};
 	}
 
 	private MongoEventBuilder createEventBuilder() {
 		final DBCollection events = getCollection(Event.class);
 		return new MongoEventBuilder() {
 			@Override
 			DBCollection _getCollection() {
 				return events;
 			}
 
 			@Override
 			EventId _createId() {
 				return new EventId((Long) b.get("_id"));
 			}
 
 			@Override
 			void _store(DBObject event) {
 				events.insert(event);
 			}
 		};
 	}
 
 	private MongoClassBuilder createClassBuilder() {
 		final DBCollection clazzes = getCollection(Clazz.class);
 		return new MongoClassBuilder() {
 			private short methods = 0;
 			private short fields = 0;
 
 			@Override
 			public FieldCreator addField() {
 				return createFieldCreator(this, ++fields);
 			}
 
 			@Override
 			public MethodCreator addMethod() {
 				return createMethodCreator(this, ++methods);
 			}
 
 			@Override
 			DBCollection _getCollection() {
 				return clazzes;
 			}
 
 			@Override
 			ClazzId _createId() {
 				if (b.containsField("_id")) {
 					return new ClazzId((Long) b.get("_id"));
 				} else {
 					// TODO this is not thread-safe!
 					Dataset ds = Context.getDataset();
 					return ClazzId.create(ds, (int) _getCollection().count() + 1);
 				}
 			}
 
 			@Override
 			protected void reset() {
 				super.reset();
 				methods = 0;
 				fields = 0;
 			}
 		};
 	}
 
 	private MongoFieldBuilder createFieldQuery() {
 		final DBCollection fields = getCollection(Field.class);
 		return new MongoFieldBuilder(null) {
 			@Override
 			DBCollection _getCollection() {
 				return fields;
 			}
 
 			@Override
 			FieldId _createId() {
 				return new FieldId((Long) b.get("_id"));
 			}
 		};
 	}
 
 	private MongoFieldBuilder createFieldCreator(MongoClassBuilder classBuilder, final short index) {
 		final DBCollection fields = getCollection(Field.class);
 		return new MongoFieldBuilder(classBuilder) {
 			@Override
 			DBCollection _getCollection() {
 				return fields;
 			}
 
 			@Override
 			FieldId _createId() {
 				Dataset ds = Context.getDataset();
 				ClazzId owner = new ClazzId((Long) b.get("owner"));
 				return FieldId.create(ds, owner, index);
 			}
 		};
 	}
 
 	private MongoMethodBuilder createMethodQuery() {
 		final DBCollection methods = getCollection(Method.class);
 		return new MongoMethodBuilder(null) {
 			@Override
 			DBCollection _getCollection() {
 				return methods;
 			}
 
 			@Override
 			MethodId _createId() {
 				return new MethodId((Long) b.get("_id"));
 			}
 		};
 	}
 
 	private MongoMethodBuilder createMethodCreator(MongoClassBuilder classBuilder, final short index) {
 		final DBCollection methods = getCollection(Method.class);
 		return new MongoMethodBuilder(classBuilder) {
 			@Override
 			DBCollection _getCollection() {
 				return methods;
 			}
 
 			@Override
 			MethodId _createId() {
 				Dataset ds = Context.getDataset();
 				ClazzId owner = new ClazzId((Long) b.get("owner"));
 				return MethodId.create(ds, owner, index);
 			}
 		};
 	}
 
 	private MongoInstanceBuilder createInstanceBuilder() {
 		final DBCollection instances = getCollection(Instance.class);
 		return new MongoInstanceBuilder() {
 			@Override
 			InstanceId _createId() {
 				return new InstanceId((Long) b.get("_id"));
 			}
 
 			@Override
 			DBCollection _getCollection() {
 				return instances;
 			}
 		};
 	}
 
 	private MongoClassSummaryBuilder createClassSummaryBuilder() {
 		final DBCollection results = getCollection(ClassSummary.class);
 		return new MongoClassSummaryBuilder() {
 			@Override
 			DBCollection _getCollection() {
 				return results;
 			}
 
 			@Override
 			ClassSummaryId _createId() {
 				return new ClassSummaryId((Long) b.get("_id"));
 			}
 		};
 	}
 
 	private MongoFieldSummaryBuilder createFieldSummaryBuilder() {
 		final DBCollection results = getCollection(FieldSummary.class);
 		return new MongoFieldSummaryBuilder() {
 			@Override
 			DBCollection _getCollection() {
 				return results;
 			}
 
 			@Override
 			FieldSummaryId _createId() {
 				return new FieldSummaryId((Long) b.get("_id"));
 			}
 		};
 	}
 
 	private MongoRequestBuilder createRequestBuilder() {
 		final DBCollection requests = getCollection(Request.class);
 		return new MongoRequestBuilder() {
 			@Override
 			DBCollection _getCollection() {
 				return requests;
 			}
 
 			@Override
 			RequestId _createId() {
 				if (b.containsField("_id")) {
 					return new RequestId((Long) b.get("_id"));
 				} else {
 					// TODO this is not multi-server safe!
 					Dataset ds = Context.getDataset();
 					return RequestId.create(ds);
 				}
 			}
 		};
 	}
 }
