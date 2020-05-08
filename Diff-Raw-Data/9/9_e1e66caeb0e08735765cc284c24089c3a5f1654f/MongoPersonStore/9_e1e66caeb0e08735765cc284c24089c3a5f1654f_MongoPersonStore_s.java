 package org.atlasapi.persistence.content.mongo;
 
 import static com.metabroadcast.common.persistence.mongo.MongoBuilders.where;
 
 import java.util.List;
 
 import org.atlasapi.media.entity.ContentGroup;
 import org.atlasapi.media.entity.Person;
 import org.atlasapi.persistence.content.PeopleLister;
 import org.atlasapi.persistence.content.PeopleListerListener;
 import org.atlasapi.persistence.content.PeopleResolver;
 import org.atlasapi.persistence.content.people.PersonWriter;
 import org.atlasapi.persistence.media.entity.PersonTranslator;
 import org.joda.time.DateTime;
 
 import com.google.common.base.Function;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Iterables;
 import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
 import com.metabroadcast.common.persistence.mongo.MongoConstants;
 import com.metabroadcast.common.persistence.mongo.MongoQueryBuilder;
 import com.metabroadcast.common.persistence.mongo.MongoSortBuilder;
 import com.metabroadcast.common.time.DateTimeZones;
 import com.mongodb.DBCollection;
 import com.mongodb.DBObject;
 
 public class MongoPersonStore implements PersonWriter, PeopleResolver, PeopleLister {
     
     static final int MONGO_SCAN_BATCH_SIZE = 100;
 
 	private static final MongoSortBuilder SORT_BY_ID = new MongoSortBuilder().ascending(MongoConstants.ID); 
 	
     private final DBCollection collection;
     private final PersonTranslator translator = new PersonTranslator();
 
     public MongoPersonStore(DatabasedMongo db) {
         collection = db.collection("people");
     }
 
     @Override
     public void updatePersonItems(Person person) {
         DBObject idQuery = translator.idQuery(person.getCanonicalUri()).build();
         
         collection.update(idQuery, translator.updateContentUris(person), true, false);
     }
 
     @Override
     public Person person(String uri) {
         List<Person> people = translator.fromDBObjects(translator.idQuery(uri).find(collection));
         return Iterables.getFirst(people, null);
     }
 
     @Override
     public void createOrUpdatePerson(Person person) {
         person.setLastUpdated(new DateTime(DateTimeZones.UTC));
         person.setMediaType(null);
         
         DBObject idQuery = translator.idQuery(person.getCanonicalUri()).build();
         collection.update(idQuery, translator.toDBObject(null, person), true, false);
     }
     
     @Override
 	public void list(PeopleListerListener handler) {
     	String fromId = null;
 		while (true) {
             List<Person> roots = ImmutableList.copyOf(batch(MONGO_SCAN_BATCH_SIZE, fromId));
             if (Iterables.isEmpty(roots)) {
                 break;
             }
             for (Person person : roots) {
            	handler.handle(person);
             }
             ContentGroup last = Iterables.getLast(roots);
             fromId = last.getCanonicalUri();
         }
 	}
 
 	private Iterable<Person> batch(int batchSize, String fromId) {
 		MongoQueryBuilder query = where();
     	if (fromId != null) {
     		query.fieldGreaterThan(MongoConstants.ID, fromId);
     	}
         return Iterables.transform(query.find(collection, SORT_BY_ID, batchSize), TO_PERSON);
 	}
 	
 	private final Function<DBObject, Person> TO_PERSON = new Function<DBObject, Person>() {
 		@Override
 		public Person apply(DBObject input) {
 			return translator.fromDBObject(input, null);
 		}
 	};
 }
