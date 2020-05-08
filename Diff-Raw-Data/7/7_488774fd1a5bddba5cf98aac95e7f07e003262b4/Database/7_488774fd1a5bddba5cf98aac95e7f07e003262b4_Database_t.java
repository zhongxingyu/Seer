 package nz.ac.vuw.ecs.rprofs.server.db;
 
 import com.google.common.annotations.VisibleForTesting;
 import com.google.common.collect.Lists;
 import com.mongodb.*;
 import nz.ac.vuw.ecs.rprofs.server.context.Context;
 import nz.ac.vuw.ecs.rprofs.server.data.util.*;
 import nz.ac.vuw.ecs.rprofs.server.domain.*;
 import nz.ac.vuw.ecs.rprofs.server.domain.id.*;
 import nz.ac.vuw.ecs.rprofs.server.model.DataObject;
 import nz.ac.vuw.ecs.rprofs.server.model.Id;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import javax.validation.constraints.NotNull;
 import java.util.List;
 
 /**
  * Author: Stephen Nelson <stephen@sfnelson.org>
  * Date: 9 September, 2011
  */
 public class Database {
 
 	private final Logger log = LoggerFactory.getLogger(Database.class);
 
 	@NotNull
 	private final Mongo mongo;
 
 	@VisibleForTesting
 	@Autowired(required = true)
 	Context context;
 
 	public Database(@NotNull Mongo mongo) {
 		this.mongo = mongo;
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
 
 	@SuppressWarnings("unchecked")
 	public List<String> findPackages() {
 		DBCollection classes = getCollection(Clazz.class);
 		return (List<String>) classes.distinct("package");
 	}
 
 	public long countPackages() {
 		DBCollection classes = getCollection(Clazz.class);
 		return classes.distinct("package").size();
 	}
 
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
 	public <T extends DataObject<?, T>> T findEntity(@NotNull Id<?, T> id) {
 		if (Dataset.class.equals(id.getTargetClass())) {
 			for (Dataset ds : getDatasets()) {
 				if (ds.getId().equals(id)) {
 					return id.getTargetClass().cast(ds);
 				}
 			}
 			return null;
 		} else {
 			Dataset current = context.getDataset();
 			DB db = getDatabase(current);
 			DBCollection collection = getCollection(db, id.getTargetClass());
 			DBObject data = collection.findOne(new BasicDBObject("_id", id.getValue()));
 			return getBuilder(id.getTargetClass()).init(data).get();
 		}
 	}
 
 	public <T extends DataObject> boolean deleteEntity(T entity) {
 
 		Dataset dataset;
 		if (entity.getClass() == Dataset.class) {
 			dataset = (Dataset) entity;
 		} else {
 			dataset = context.getDataset();
 		}
 
 		if (dataset == null) throw new RuntimeException("no dataset in context");
 
 		DB database = getDatabase(dataset);
 
 		if (database == null) throw new RuntimeException("invalid dataset provided");
 
 		DBCollection collection = getCollection(database, entity.getClass());
 		collection.remove(new BasicDBObject("_id", entity.getId().getValue()));
 
 		if (entity.getClass() == Dataset.class) {
 			mongo.dropDatabase(getDBName((Dataset) entity));
 		}
 
 		return true;
 	}
 
 	private DB getDatabase() {
 		Dataset current = context.getDataset();
 		if (current != null) return getDatabase(current);
 		else return null;
 	}
 
 	private DB getDatabase(@NotNull Dataset dataset) {
 		return mongo.getDB(getDBName(dataset));
 	}
 
 	private DBCollection getCollection(Class<? extends DataObject> type) {
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
 			return root.getCollection("objects");
 		} else if (type == Event.class) {
 			return root.getCollection("events");
 		} else {
 			throw new RuntimeException("type not implemented: " + type);
 		}
 	}
 
 	private List<Dataset> getDatasets() {
 		List<Dataset> result = Lists.newArrayList();
 		MongoDatasetBuilder builder = createDatasetBuilder();
 		for (String dbname : mongo.getDatabaseNames()) {
 			if (dbname.startsWith("rprof_")) {
 				DBObject properties = mongo.getDB(dbname)
 						.getCollection("properties").findOne();
 				result.add(builder.init(properties).get());
 			}
 		}
 		return result;
 	}
 
 	@VisibleForTesting
 	String getDBName(Dataset dataset) {
 		return "rprof_" + dataset.getHandle() + "_" + dataset.getId().getDatasetIndex();
 	}
 
 	@SuppressWarnings("unchecked")
 	private <T extends DataObject<?, T>> EntityBuilder<?, ?, T> getBuilder(@NotNull Class<T> type) {
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
 						if (dbname.startsWith("rprof")) {
 							DB db = mongo.getDB(dbname);
 							DBObject properties = db.getCollection("properties").findOne();
 							short id = ((Long) properties.get("_id")).shortValue();
 							if (id > max) max = id;
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
 				context.setDataset(findEntity(id));
 				super.update(id);
 				context.clear();
 			}
 
 			@Override
 			void _store(DBObject dataset) {
 				String dbname = "rprof_" + dataset.get("handle") + "_" + dataset.get("_id");
 				DB db = mongo.getDB(dbname);
				dataset.put("version", 1);
 				db.getCollection("properties").insert(dataset);
 			}
 
 			@Override
 			public List<Dataset> find() {
 				List<Dataset> result = Lists.newArrayList();
 				for (Dataset ds : getDatasets()) {
 					DBCursor c = getCollection(ds, Dataset.class).find(b);
 					if (c.hasNext()) {
 						result.add(ds);
 					}
 					c.close();
 				}
 				return result;
 			}
 
 			@Override
 			long _count(DBObject query) {
 				return find().size();
 			}
 		};
 	}
 
 	private MongoEventBuilder createEventBuilder() {
 		return new MongoEventBuilder() {
 			final DBCollection events = getCollection(Event.class);
 
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
 				return getCollection(Clazz.class);
 			}
 
 			@Override
 			ClazzId _createId() {
 				if (b.containsField("_id")) {
 					return new ClazzId((Long) b.get("_id"));
 				} else {
 					// TODO this is not thread-safe!
 					Dataset ds = context.getDataset();
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
 		return new MongoFieldBuilder(null) {
 			@Override
 			DBCollection _getCollection() {
 				return getCollection(Field.class);
 			}
 
 			@Override
 			FieldId _createId() {
 				return new FieldId((Long) b.get("_id"));
 			}
 		};
 	}
 
 	private MongoFieldBuilder createFieldCreator(MongoClassBuilder classBuilder, final short index) {
 		return new MongoFieldBuilder(classBuilder) {
 			@Override
 			DBCollection _getCollection() {
 				return getCollection(Field.class);
 			}
 
 			@Override
 			FieldId _createId() {
 				Dataset ds = context.getDataset();
 				ClazzId owner = new ClazzId((Long) b.get("owner"));
 				return FieldId.create(ds, owner, index);
 			}
 		};
 	}
 
 	private MongoMethodBuilder createMethodQuery() {
 		return new MongoMethodBuilder(null) {
 			@Override
 			DBCollection _getCollection() {
 				return getCollection(Method.class);
 			}
 
 			@Override
 			MethodId _createId() {
 				return new MethodId((Long) b.get("_id"));
 			}
 		};
 	}
 
 	private MongoMethodBuilder createMethodCreator(MongoClassBuilder classBuilder, final short index) {
 		return new MongoMethodBuilder(classBuilder) {
 			@Override
 			DBCollection _getCollection() {
 				return getCollection(Method.class);
 			}
 
 			@Override
 			MethodId _createId() {
 				Dataset ds = context.getDataset();
 				ClazzId owner = new ClazzId((Long) b.get("owner"));
 				return MethodId.create(ds, owner, index);
 			}
 		};
 	}
 }
